package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;

@CheckData(name = "BadPacketsM (SpoofItem)", type = CheckType.BADPACKETS, configName = "BadPacketsM", decay = 0.6, description = "Sent impossible use item packet")
public final class BadPacketsM extends Check implements PacketCheck {
    public BadPacketsM(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            final WrapperPlayClientUseItem packet = new WrapperPlayClientUseItem(event);
            final ItemStack held = packet.getHand() == com.github.retrooper.packetevents.protocol.player.InteractionHand.OFF_HAND
                    ? player.getInventory().getOffHand()
                    : player.getInventory().getHeldItem();
            if (isEmpty(held)) {
                if (flagAndAlert("hand= " + packet.getHand()) && shouldModifyPackets()) {
                    player.onPacketCancel();
                    event.setCancelled(true);
                }
            }
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            final WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
            if (packet.getFace() == BlockFace.OTHER) {

                final int expectedY = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8) ? 4095 : 255;

                final boolean failedItemCheck = packet.getItemStack().isPresent() && isEmpty(packet.getItemStack().get())

                        && player.getClientVersion().isOlderThan(ClientVersion.V_1_9);

                final Vector3i pos = packet.getBlockPosition();
                final Vector3f cursor = packet.getCursorPosition();

                if (failedItemCheck
                        || pos.x != -1
                        || pos.y != expectedY
                        || pos.z != -1
                        || cursor.x != 0
                        || cursor.y != 0
                        || cursor.z != 0
                        || packet.getSequence() != 0
                ) {
                    final String verbose = String.format(
                            "xyz= %s %s %s\ncursor= %s | %s | %s\nitem= %s\nsequence= %s",
                            pos.x, pos.y, pos.z, cursor.x, cursor.y, cursor.z, !failedItemCheck, packet.getSequence()
                    );
                    if (flagAndAlert(verbose) && shouldModifyPackets()) {
                        player.onPacketCancel();
                        event.setCancelled(true);
                    }
                }
            } else {
                rewardVL();
            }
        }
    }

    private boolean isEmpty(ItemStack itemStack) {
        return itemStack.getType() == ItemTypes.AIR;
    }
}