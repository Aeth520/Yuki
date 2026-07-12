package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.entity.Player;

@CheckData(name = "KillAuraI (Accel)", type = CheckType.KILLAURA, configName = "KillAuraI", description = "Invalid accel when player attacking", decay = 0.010)
public final class KillAuraI extends Check implements PacketCheck {

    public KillAuraI(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

            if (flying.hasPositionChanged() && player.lastAttack < 3) {

                double acceleration = player.acceleration;
                double deltaXZ = player.deltaXZ;

                float deltaYaw = Math.abs(player.yaw - player.lastYaw);
                float deltaPitch = Math.abs(player.pitch - player.lastPitch);

                boolean shouldExempt = !isExempt(ExemptType.CLIENT_ANTICHEAT);
                boolean validTarget = player.getTarget() != null && player.getTarget() instanceof Player;
                boolean invalid = acceleration < 0.001
                        && deltaYaw > 10.0f
                        && deltaPitch > 26.5
                        && shouldExempt
                        && validTarget
                        && deltaXZ > 0.0;

                if (invalid && flagAndAlert("accel= " + acceleration
                        + "\ndp= " + deltaPitch
                        + "\ndy= " + deltaYaw
                        + "\npitch= " + deltaYaw)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                    setbackIfAboveSetbackVL();
                }
            }
        } else {
            rewardVL();
        }
    }
}



