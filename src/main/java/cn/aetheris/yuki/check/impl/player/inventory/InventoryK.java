package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "InventoryK (SLOT)",
        configName = "InventoryK",
        description = "Invalid held-item change",
        type = CheckType.INVENTORY,
        decay = 0.25,
        setback = 6)
public final class InventoryK extends InventoryCheck {

    private long lastTransaction = Long.MAX_VALUE; 

    public InventoryK(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        super.onPacketReceive(event);

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            if (player.hasInventoryOpen) {
                if (buffer++ > 4) {
                    if (this.lastTransaction < player.lastTransactionReceived.get()) {
                        if (flagAndAlert()) {
                            event.setCancelled(true);
                            closeInventory();
                            player.onPacketCancel();
                            player.getInventory().requiresRefresh = true;
                        }
                    }
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
            this.lastTransaction = player.lastTransactionSent.get();
        }
    }
}