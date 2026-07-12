package cn.aetheris.yuki.check.impl.player.timer;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(
        name = "TimerD (AVG)",
        configName = "TimerD",
        decay = 0.85,
        type = CheckType.TIMER
)
public final class TimerD extends Check implements PacketCheck {

    private final EvictingList<Long> samples;
    private double normalSpeedThreshold;
    private double strictSpeedThreshold;
    private double bufferThreshold;
    private int sampleSize;
    private long lastFlagTime;
    private double currentThreshold;

    public TimerD(PlayerData player) {
        super(player);
        sampleSize = 50;
        samples = new EvictingList<>(sampleSize);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isTickPacket(event.getPacketType())) return;

        if (shouldSkipDetection()) {
            samples.clear();
            return;
        }

        if (player.getSetbackTeleportUtil().shouldBlockMovement()) {
            samples.clear();
            return;
        }

        final long currentTime = time();
        final long packetDelay = currentTime - player.lastFlying;

        if (packetDelay < 1L || packetDelay > 150L) {
            buffer *= 0.95;
            return;
        }

        adjustSampleSizeByVersion();

        samples.add(packetDelay);

        if (samples.size() >= sampleSize * 0.7) {
            analyzeMovementPattern();
        }
    }

    private boolean shouldSkipDetection() {
        return isExempt(
                ExemptType.JOIN,
                ExemptType.VEHICLE,
                ExemptType.FLYING,
                ExemptType.MOVE_LAGGING,
                ExemptType.CLIENT_ANTICHEAT
        ) || player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(0) && player.packetStateData.lastPacketWasOnePointSeventeenDuplicate;
    }

    private void adjustSampleSizeByVersion() {
        ClientVersion version = player.getClientVersion();
        sampleSize = version.isNewerThan(ClientVersion.V_1_16_4) ? 70 : 50;
    }

    private void analyzeMovementPattern() {
        double weightedAvg = MathUtil.getWeightedAverage(samples);
        double currentSpeed = 50.0 / weightedAvg;

        currentThreshold = player.isSprinting() ? strictSpeedThreshold : normalSpeedThreshold;

        final double standardDeviation = MathUtil.getStandardDeviation(samples);

        if (standardDeviation > 10.0) {
            currentThreshold *= 1.2;
        }

        double bufferIncrease = (currentSpeed - currentThreshold) * 0.5;

        if (currentSpeed > currentThreshold) {
            buffer += bufferIncrease;

            if (buffer > bufferThreshold) {
                if (time() - lastFlagTime < 500L) return;

                if (confirmAbnormalPattern()) {
                    triggerViolation(currentSpeed, weightedAvg, standardDeviation);
                    lastFlagTime = time();
                }
            }
        } else {
            buffer = Math.max(buffer * 0.65, 0);
        }
    }

    private boolean confirmAbnormalPattern() {
        int abnormalCount = 0;
        for (int i = Math.max(0, samples.size() - 5); i < samples.size(); i++) {
            double speed = 50.0 / samples.get(i);
            if (speed > currentThreshold) abnormalCount++;
        }
        return abnormalCount >= 4;
    }

    private void triggerViolation(double speed, double avgDelay, double stdDeviation) {
        String debugMsg = String.format(
                "s= %.2f\navg= %.1f\nt= %.2f\nstd= %.2f\nb= %.2f",
                speed, avgDelay, currentThreshold, stdDeviation, buffer
        );

        if (flagAndAlert(debugMsg) && shouldModifyPackets()) {
            setbackIfAboveSetbackVL();
        }
        buffer = 0;
    }

    @Override
    public void reload() {
        super.reload();
        boolean isStrictMode = getConfig().getBooleanElse(getConfigName() + ".strict", false);

        normalSpeedThreshold = getConfig().getDoubleElse(
                getConfigName() + ".threshold.normal",
                isStrictMode ? 1.03 : 1.07
        );

        strictSpeedThreshold = getConfig().getDoubleElse(
                getConfigName() + ".threshold.strict",
                normalSpeedThreshold * 1.15
        );

        bufferThreshold = getConfig().getDoubleElse(
                getConfigName() + ".buffer",
                isStrictMode ? 25.0 : 40.0
        );
    }
}
