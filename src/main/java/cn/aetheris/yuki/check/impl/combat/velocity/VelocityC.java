package cn.aetheris.yuki.check.impl.combat.velocity;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.time.Watch;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

@CheckData(name = "VelocityC (Jump)",
        configName = "VelocityC",
        type = CheckType.VELOCITY,
        description = "check for auto jumpreset",
        decay = 0.86,
        experimental = true
)
public final class VelocityC extends Check implements PostPredictionCheck {
    private final List<Integer> jTicks = new LinkedList<>();
    private final Watch velocityTime = new Watch();
    private final Watch flyingTime = new Watch();
    double jumpVelocityThreshold = 1.0;
    private int jVBuffer = 0;
    private boolean jump = false;

    public VelocityC(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(event);
            if (velocity.getEntityId() != player.getEntityID()) {
                return;
            }
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                velocityTime.reset();
                if (isExempt(ExemptType.JOIN)) {
                    
                    
                    
                    
                    return;
                }
                if (flyingTime.hasTimeElapsed(150) && !player.isTeleporting() && !player.isWorldChange() && !player.isRespawn() && shouldModifyPackets()) {
                    player.getSetbackTeleportUtil().executeForceResync();
                    LogUtils.sync("&b " + player.getName() + "&7 has been the velocity of the asynchronous server &7(&bVelocitySync t= " + flyingTime.getTime() + " &7)");
                }
            });
        }

    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {
            flyingTime.reset();

            if (player.hasAttackedSince(1000)) {
                if (player.velocitySinceTick < 10) {
                    double jumpMotion = 0.42;
                    if (Math.abs(player.getLastDeltaY() - jumpMotion) < 0.05) {
                        jump = true;
                        jTicks.add(player.velocitySinceTick);
                    }
                } else {
                    if (jump) {
                        jump = false;
                        jVBuffer++;
                    } else {
                        jVBuffer -= 2;
                        if (jVBuffer < 0) jVBuffer = 0;
                    }
                    if (jVBuffer == 0) {
                        jTicks.clear();
                    }
                    if (jVBuffer > 5 * jumpVelocityThreshold) {
                        jVBuffer--;
                        jTicks.clear();
                        if (flagAndAlert("(Jump)\nb= " + jVBuffer)) {
                            player.mitigateDamage();
                        }
                    }
                    if (jTicks.size() >= 5) {
                        while (jTicks.size() > 5) {
                            jTicks.remove(0);
                        }
                        double stdDev = MathUtil.getStandardDeviation(jTicks);
                        if (stdDev < 1) {
                            flagAndAlert("(Jump#2)\nstd= " + stdDev);
                        }
                    }
                }
            }
        }
    }
}