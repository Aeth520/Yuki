package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "InventoryH (Attack)",
        configName = "InventoryH",
        type = CheckType.INVENTORY,
        description = "Open inventory while attack",
        setback = 7)
public final class InventoryH extends InventoryCheck {


    public InventoryH(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                if (player.hasInventoryOpen || player.packetActionProcessor.isOpeningInventory()) {

                    if (buffer++ > 3) {

                        if (shouldModifyPackets() && flagAndAlert()) {
                            event.setCancelled(true);
                            closeInventory();
                            player.onPacketCancel();
                            player.getInventory().requiresRefresh = true;
                        }
                    }
                }
            } else {
                rewardVL();
            }
        }
    }
}