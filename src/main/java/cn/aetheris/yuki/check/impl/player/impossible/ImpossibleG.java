package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

import java.util.Locale;

@CheckData(name = "ImpossibleG (Dig)", configName = "ImpossibleG", description = "Checks for invalid release use item packet", type = CheckType.IMPOSSIBLE)
public class ImpossibleG extends Check implements PacketCheck {

    public ImpossibleG(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);

            if (packet.getAction() == DiggingAction.START_DIGGING
                    || packet.getAction() == DiggingAction.FINISHED_DIGGING
                    || packet.getAction() == DiggingAction.CANCELLED_DIGGING) {
                return;
            }

            final BlockFace expectedFace = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10) && packet.getAction() == DiggingAction.RELEASE_USE_ITEM
                    ? BlockFace.SOUTH : BlockFace.DOWN;

            if (packet.getBlockFace() != expectedFace
                    || packet.getBlockPosition().getX() != 0
                    || packet.getBlockPosition().getY() != 0
                    || packet.getBlockPosition().getZ() != 0
                    || packet.getSequence() != 0) {

                if (packet.getAction() == DiggingAction.RELEASE_USE_ITEM) return;

                if (buffer++ > 4) {
                    if (flagAndAlert("p= " + packet.getBlockPosition().getX() + " | " + packet.getBlockPosition().getY() + " | " + packet.getBlockPosition().getZ()
                            + "\ns= " + packet.getSequence()
                            + "\na= " + packet.getAction().toString().toLowerCase(Locale.ROOT)
                    )) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
    }
}