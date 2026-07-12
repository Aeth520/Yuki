package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.enums.InventoryDesyncStatus;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "InventoryE (BadPackets)", configName = "InventoryE", description = "Sent a click window packet without a open inventory", setback = 8)
public final class InventoryE extends InventoryCheck {

    public InventoryE(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
                || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) return;

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            if (!player.hasInventoryOpen && player.inventoryDesyncStatus == InventoryDesyncStatus.NOT_DESYNCED) {
                if (buffer++ > 3) {
                    if (shouldModifyPackets() && flagAndAlert()) {
                        event.setCancelled(true);
                        setbackIfAboveSetbackVL();
                        player.onPacketCancel();
                    }
                    closeInventory();
                    player.getInventory().requiresRefresh = true;
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}
