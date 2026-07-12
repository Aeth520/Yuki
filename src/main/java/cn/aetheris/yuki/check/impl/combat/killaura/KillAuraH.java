package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "KillAuraH (Lock)", type = CheckType.KILLAURA, configName = "KillAuraH", description = "Lock Aura", decay = 0.39)
public final class KillAuraH extends Check implements PacketCheck {

    float lastYaw;
    float lastPitch;
    double lastPosX;
    double lastPosZ;
    double lastHorizontalDistance;

    public KillAuraH(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            if (wrapper.hasRotationChanged() && wrapper.hasPositionChanged()) {
                float yaw = wrapper.getLocation().getYaw();
                float pitch = wrapper.getLocation().getPitch();

                double posX = wrapper.getLocation().getX();
                double posZ = wrapper.getLocation().getZ();

                boolean valid = yaw != lastYaw
                        && pitch != lastPitch
                        && posX != lastPosX
                        && posZ != lastPosZ
                        && !player.isTeleporting()
                        && !player.getSetbackTeleportUtil().shouldBlockMovement()
                        && !player.hasAttackedSince(250L);

                if (valid) {

                    float deltaYaw = Math.abs(yaw - lastYaw);
                    float deltaPitch = Math.abs(pitch - lastPitch);

                    double horizontalDistance = MathUtil.hypot(posX - lastPosX, posZ - lastPosZ);
                    double acceleration = Math.abs(horizontalDistance - lastHorizontalDistance);

                    if (deltaYaw > 25.d
                            && deltaPitch > 10.d
                            && acceleration < 1e-04) {
                        if (buffer++ > 3) {
                            if (flagAndAlert("dy= " + deltaYaw
                                    + "\ndp= " + deltaPitch
                                    + "\na= " + acceleration)) {
                                player.mitigateDamage();
                            }
                        }
                    }
                    lastHorizontalDistance = horizontalDistance;
                }

                lastPosX = posX;
                lastPosZ = posZ;
                lastYaw = yaw;
                lastPitch = pitch;
            } else {
                rewardBufferAndVL();
            }
        }
    }
}
