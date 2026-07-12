package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(
        name = "AutoClickerE (Kurtosis)",
        type = CheckType.AUTOCLICKER,
        configName = "AutoClickerE",
        decay = 0.75
)
public final class AutoClickerE extends Check implements PacketCheck {
    private final EvictingList<Double> samples = new EvictingList<>(25);
    private long lastFlag;
    private int minCps;
    private double maxKurtosis;
    private boolean shouldCancel;
    private int buffer2;

    public AutoClickerE(PlayerData player) {
        super(player);
    }

    private static double[] calculateStats(Double[] intervals) {
        double sum = 0;
        double sumSquares = 0;
        double sumCubes = 0;
        double sumQuads = 0;

        for (double interval : intervals) {
            sum += interval;
            sumSquares += interval * interval;
            sumCubes += interval * interval * interval;
            sumQuads += interval * interval * interval * interval;
        }

        double mean = sum / intervals.length;
        double variance = (sumSquares / intervals.length) - (mean * mean);
        double stdDev = Math.sqrt(variance);
        double kurtosis = 0;

        if (variance > 0) {
            double meanSq = mean * mean;
            double m4 = (sumQuads / intervals.length)
                    - 4 * mean * (sumCubes / intervals.length)
                    + 6 * meanSq * (sumSquares / intervals.length)
                    - 3 * meanSq * meanSq;
            kurtosis = m4 / (variance * variance);
        }

        return new double[]{stdDev, kurtosis};
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.ANIMATION) return;

        double delay = player.clickProcessor.getDelay() / 1000.0;
        double cps = player.getCps();

        if (isExempt(ExemptType.INTERACT)) {
            return;
        }

        if (player.hasAttackedSince(1000L) || delay > 5000L) {
            samples.clear();
            return;
        }


        samples.add(delay);
        if (!samples.isFull()) return;

        Double[] intervalArray = samples.toArray(new Double[0]);
        double[] stats = calculateStats(intervalArray);
        double stdDev = stats[0];
        double kurtosis = stats[1];

        player.clickProcessor.setKurtosis(kurtosis);
        samples.clear();

        if (stdDev < 0.02) {
            if (buffer2++ > 3) {
                flagAndAlert("(Change)\nstd= " + stdDev + "\nc= " + cps);
                player.mitigateDamage();
            }
        } else {
            buffer2 = Math.max(buffer2 - 1, 0);
        }

        if (kurtosis < maxKurtosis && cps > minCps) {
            if (time() - lastFlag < 1000L) return;

            if (buffer++ > 5) {
                if (flagAndAlert("(Highest)\nk= " + kurtosis + "\nc= " + cps)) {
                    player.mitigateDamage();
                    player.onPacketCancel();
                    if (shouldCancel && getViolations() > 4) {
                        event.setCancelled(true);
                    }
                    buffer *= 0.85;
                    lastFlag = time();
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }

    @Override
    public void reload() {
        minCps = getConfig().getIntElse(getConfigName() + ".min-cps", 16);
        maxKurtosis = getConfig().getDoubleElse(getConfigName() + ".max-kurtosis", 4);
        shouldCancel = getConfig().getBooleanElse(getConfigName() + ".should-cancel", false);
    }
}
