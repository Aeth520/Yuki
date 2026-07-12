package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.time.TimeUtils;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.Deque;
import java.util.LinkedList;

@CheckData(name = "AutoClickerL (Sqrt)", type = CheckType.AUTOCLICKER, configName = "AutoClickerL", decay = 0.15)
public final class AutoClickerL extends Check implements PacketCheck {

    final Deque<Long> clickSamples;
    long lastSwing;
    double lastDeviation;
    double lastAverage;

    public AutoClickerL(PlayerData player) {
        super(player);
        clickSamples = new LinkedList<>();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            long now = time();
            long delay = now - lastSwing;

            int cps = player.getCps();

            boolean valid = !TimeUtils.elapsed(player.lastBlockDig, 250L) || isExempt(ExemptType.INTERACT);

            if (valid) {
                return;
            }

            if (delay > 1L
                    && cps > 6
                    && delay < 200L
                    && clickSamples.add(delay)
                    && clickSamples.size() == 30) {

                double average = clickSamples.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);

                double stdDeviation = 0.0;

                for (Long click : clickSamples) {
                    stdDeviation += Math.pow(click.doubleValue() - average, 2);
                }

                stdDeviation /= clickSamples.size();

                double sqrtDeviation = Math.sqrt(stdDeviation);

                double deltaDeviation = Math.abs(sqrtDeviation - lastDeviation);
                double deltaAverage = Math.abs(average - lastAverage);

                double delta = Math.abs(deltaDeviation - deltaAverage);

                if (sqrtDeviation < 30.d
                        && deltaDeviation <= 4
                        && deltaAverage < 11.d
                        && delta > 0.5) {
                    if (++buffer > 3) {
                        if (flagAndAlert("sd= " + sqrtDeviation
                                + "\ndd= " + deltaDeviation
                                + "\nda= " + deltaAverage
                                + "\nv= " + valid)) {
                            rewardBufferAndVL();
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }

                lastDeviation = sqrtDeviation;
                lastAverage = average;
                clickSamples.clear();
            }

            lastSwing = now;
        }
    }
}
