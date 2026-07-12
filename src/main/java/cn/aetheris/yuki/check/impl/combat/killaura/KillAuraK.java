package cn.aetheris.yuki.check.impl.combat.killaura;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "KillAuraK (Click)", type = CheckType.KILLAURA, configName = "KillAuraK", description = "Sprinting attack but clickdelay low", decay = 0.55)
public final class KillAuraK extends Check implements PacketCheck {

    public KillAuraK(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                return;
            }
            boolean sprinting = player.isSprinting();
            double deltaXZ = player.getDeltaXZ();
            double acceleration = player.getAcceleration();
            long clickDelay = player.getClickProcessor().getDelay();
            final PacketEntity target = player.getTarget();

            if (target == null) {
                return;
            }

            boolean invalid = acceleration < 0.0025
                    && deltaXZ > 0.22
                    && sprinting
                    && clickDelay < 250L
                    && player.hasAttackedSince(60L)
                    && target.getType() == EntityTypes.PLAYER;

            if (invalid) {
                if (buffer++ > 6) {
                    if (flagAndAlert("acceleration= " + acceleration + "\nclickDelay= " + clickDelay + "\ndeltaXZ= " + deltaXZ)) {
                        player.mitigateDamage();
                        buffer = 0;
                    }
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }
}

