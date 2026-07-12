package cn.aetheris.yuki.check.util.processor.clickprocessor;


import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class ClickProcessor extends Check implements PacketCheck {

    public final EvictingList<Long> samples;
    public int cps;
    public int lastCps;
    public int swings;
    public long lastSwing;
    public long delay;
    private int done;
    private double kurtosis;

    public ClickProcessor(PlayerData player) {
        super(player);
        samples = new EvictingList<>(20);
        lastSwing = time();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            int needTick = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) ? 20 : 19;
            if (done++ >= needTick) {
                if (isExempt(ExemptType.LAGGING, ExemptType.TELEPORT) || player.getPacketStateData().lastPacketWasOnePointSeventeenDuplicate) {
                    done = 0; 
                    swings = 0;
                }
                cps = swings;
                if (time() - lastSwing <= 350L) {
                    lastCps = cps;
                }
                done = 0;
                swings = 0;
            }
        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION
                && !isExempt(ExemptType.INTERACT, ExemptType.LAGGING, ExemptType.PLACING)
                && PluginLoader.INSTANCE.getConfigManager().getConfig().getString("function.click-listener.mode").contains("packet")) {
            ++swings;
            final long now = time();
            if (lastSwing > 0L) {
                delay = now - lastSwing;
                samples.add(delay);
            }
            lastSwing = now;
        }
    }

    
    public double calculateDeviation() {
        double mean = samples.stream().mapToLong(l -> l).average().orElse(0.0);
        return Math.sqrt(samples.stream()
                .mapToDouble(l -> Math.pow(l - mean, 2))
                .average().orElse(0.0));
    }

    
    public double calculateVariance() {
        double mean = samples.stream().mapToLong(l -> l).average().orElse(0.0);
        return samples.stream()
                .mapToDouble(l -> Math.pow(l - mean, 2))
                .average().orElse(0.0);
    }

    
    public double calculateKurtosis() {
        double mean = samples.stream().mapToLong(l -> l).average().orElse(0.0);
        double n = samples.size();
        double sumFourthMoment = samples.stream()
                .mapToDouble(l -> Math.pow(l - mean, 4)).sum();
        return (n * sumFourthMoment) / Math.pow(calculateVariance(), 2);
    }

    
    public double calculateSkewness() {
        double mean = samples.stream().mapToLong(l -> l).average().orElse(0.0);
        double n = samples.size();
        double sumThirdMoment = samples.stream()
                .mapToDouble(l -> Math.pow(l - mean, 3)).sum();
        return (n * sumThirdMoment) / Math.pow(calculateDeviation(), 3);
    }

    
    public boolean checkClickConsistency(double threshold) {
        if (samples.size() < 5) return false;

        double mean = samples.stream().mapToLong(l -> l).average().orElse(0.0);
        long withinThreshold = samples.stream()
                .filter(l -> Math.abs(l - mean) < 5) 
                .count();
        double consistency = (double) withinThreshold / samples.size();
        return consistency >= threshold;
    }

    
    public boolean hasRepeatedPattern(int threshold) {
        if (samples.size() < 3) return false;

        Long first = samples.get(0);
        int repeatCount = 1;

        for (int i = 1; i < samples.size(); i++) {
            if (Math.abs(samples.get(i) - first) < 3) { 
                if (++repeatCount >= threshold) return true;
            } else {
                first = samples.get(i);
                repeatCount = 1;
            }
        }
        return false;
    }

    public boolean hasClickFrequencyTrend(int trendThreshold) {
        if (samples.size() < 3) return false;

        double trendScore = 0;
        for (int i = 1; i < samples.size(); i++) {
            trendScore += Math.abs(samples.get(i) - samples.get(i - 1));
        }

        return trendScore < trendThreshold;
    }

    public boolean hasSmallIntervalRange(int rangeThreshold) {
        if (samples.size() < 2) return false;

        long minInterval = Long.MAX_VALUE;
        long maxInterval = Long.MIN_VALUE;

        for (long sample : samples) {
            if (sample < minInterval) minInterval = sample;
            if (sample > maxInterval) maxInterval = sample;
        }

        return (maxInterval - minInterval) < rangeThreshold;
    }

    public boolean hasClickSpeedSurge(int surgeThreshold) {
        if (samples.size() < 5) return false;

        double averageSpeed = samples.stream().mapToLong(l -> l).average().orElse(0.0);
        double speedChange = 0;
        for (long sample : samples) {
            speedChange += Math.abs(sample - averageSpeed);
        }
        return speedChange > surgeThreshold;
    }

}
