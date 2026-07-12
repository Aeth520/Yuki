package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;

@CheckData(name = "InventoryD",
        type = CheckType.INVENTORY,
        description = "Open inventory while placement")
public final class InventoryD extends InventoryCheck {

    public InventoryD(PlayerData player) {
        super(player);
    }

    public void onBlockPlace(final BlockPlace place) {
        if (player.hasInventoryOpen) {

            if (buffer++ > 3) {
                if (shouldModifyPackets() && flagAndAlert()) {
                    place.resync();
                    closeInventory();
                    player.onPacketCancel();
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }
}
