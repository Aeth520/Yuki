package cn.aetheris.yuki.check.impl.player.pingspoof;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(
        name = "PingSpoofC (Delta)",
        configName = "PingSpoofC",
        description = "Advanced ping spoof detection with statistical methods",
        decay = 0.655,
        type = CheckType.PINGSPOOF,
        experimental = true
)
public final class PingSpoofC extends Check implements PacketCheck {

    private final Deque<Double> pingHistory = new ArrayDeque<>();
    private double baseThreshold;
    private double maxThreshold;
    private int maxBuffer;
    private int historySize;
    private double dynamicThreshold;
    private double historicalAverage;
    private long lastFlag;

    public PingSpoofC(PlayerData player) {
        super(player);
        dynamicThreshold = baseThreshold = 100.0;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (isExempt(ExemptType.JOIN, ExemptType.DIED, ExemptType.INVALID_GAMEMODE) ||
                    player.getSetbackTeleportUtil().shouldBlockMovement() ||
                    !player.isMoving()) {
                rewardBufferAndVL();
                return;
            }

            double keepAlivePing = player.getAveragePing();
            double transactionPing = player.getTransactionPing();
            double delta = Math.abs(keepAlivePing - transactionPing);
            double returnTime = time() - player.getTranDelay();

            if (returnTime < 20) {
                return;
            }

            double deltaThreshold = Math.max(dynamicThreshold, historicalAverage * 1.3);
            boolean deltaAnomaly = delta > deltaThreshold;

            if (deltaAnomaly) {
                if (time() - lastFlag >= 1000) {

                    if (buffer++ > maxBuffer) {
                        if (flagAndAlert(String.format(
                                "k= %.2f\nt= %.2f\nd= %.2f\nh= %.2f\na= %.2f\ns= %.2f\nb= %.2f\nl= %s",
                                keepAlivePing, transactionPing, delta, dynamicThreshold,
                                historicalAverage, MathUtil.stdDev(pingHistory), buffer, returnTime
                        )) && shouldModifyPackets()) {
                            dynamicThreshold = Math.min(dynamicThreshold * 1.15, maxThreshold);
                            if (player.hasAttackedSince(1200L)) {
                                player.mitigateDamage();
                            }
                        }
                        player.sendTransaction();
                        lastFlag = time();

                    } else {
                        dynamicThreshold *= 1.05;
                        rewardBufferAndVL();
                    }
                }
            }

            while (pingHistory.size() >= historySize) {
                pingHistory.pollFirst();
            }
            pingHistory.addLast(delta);

            double sum = 0;
            for (double value : pingHistory) sum += value;
            double avg = pingHistory.isEmpty() ? baseThreshold : sum / pingHistory.size();
            historicalAverage = historicalAverage * 0.7 + avg * 0.3;
            dynamicThreshold = Math.min(
                    Math.max(historicalAverage * 1.4, baseThreshold),
                    maxThreshold
            );
        }
    }

    @Override
    public void reload() {
        baseThreshold = getConfig().getDoubleElse("PingSpoof.threshold.base", 100.0);
        maxThreshold = getConfig().getDoubleElse("PingSpoof.threshold.max", 250.0);
        maxBuffer = getConfig().getIntElse("PingSpoof.buffer", 12);
        historySize = getConfig().getIntElse("PingSpoof.history-size", 20);
    }
}
