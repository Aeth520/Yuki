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
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "TimerC (SLOWDOWN)",
        configName = "TimerC",
        decay = 0.25,
        description = "Checks for slow down game timer",
        type = CheckType.TIMER,
        experimental = true)
public final class TimerC extends Check implements PacketCheck {

    private final EvictingList<Long> samples;
    private long lastFlying;

    public TimerC(PlayerData player) {
        super(player);
        samples = new EvictingList<>(40);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {

            if (isExempt(ExemptType.JOIN, ExemptType.TELEPORT, ExemptType.FLYING, ExemptType.DIED, ExemptType.VEHICLE, ExemptType.VOID))
                return;

            if (player.getSetbackTeleportUtil().insideUnloadedChunk() || player.uncertaintyHandler.lastPointThree.hasOccurredSince(2))
                return;

            if (lastFlying != 0L && player.deltaXZ > 0.0) {
                long delta = time() - lastFlying;
                samples.add(delta);
                if (samples.isFull()) {
                    double average = MathUtil.getAverage(samples);
                    double speed = 50.0 / average;
                    double deviation = MathUtil.getStandardDeviation(samples);
                    if (speed <= 0.75 && deviation < 50.0) {
                        if (buffer++ > 4) {
                            if (flagAndAlert("s= " + speed
                                    + "\nstd= " + deviation)) {
                                rewardBufferAndVL();
                                player.mitigateDamage();
                            }
                            lastFlying = time();
                        }
                    }
                } else {
                    rewardVL();
                }
            }
        }
    }
}