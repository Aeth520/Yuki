package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.player.HeadRotation;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;

import java.util.LinkedList;
import java.util.List;

@CheckData(name = "BadPacketsS (Rotation)", type = CheckType.BADPACKETS, configName = "BadPacketsS", description = "Rotation in use item packet did not match tick rotation", decay = 0.76, experimental = true)
public final class BadPacketsS extends Check implements PostPredictionCheck {
    private final List<HeadRotation> rotations = new LinkedList<>();

    public BadPacketsS(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isExempt(ExemptType.INVALID_GAMEMODE)) {
            rotations.clear();
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21)
                && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21)) {
            WrapperPlayClientUseItem packet = new WrapperPlayClientUseItem(event);
            rotations.add(new HeadRotation(packet.getYaw(), packet.getPitch()));
        }

        if (isTickPacket(event.getPacketType())) {
            
            boolean allowLast = player.canSkipTicksPreVia() && (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION);
            for (HeadRotation rotation : rotations) {
                if (rotation.getYaw() == player.yaw && rotation.getPitch() == player.pitch) {
                    allowLast = false;
                    continue;
                }

                if (rotation.getYaw() == player.lastYaw && rotation.getPitch() == player.lastPitch && allowLast) {
                    continue;
                }

                if (flagAndAlert()) {
                    player.mitigateDamage();
                }
            }

            rotations.clear();
        }
    }
}