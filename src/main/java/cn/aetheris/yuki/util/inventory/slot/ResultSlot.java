package cn.aetheris.yuki.util.inventory.slot;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.inventory.InventoryStorage;
import com.github.retrooper.packetevents.protocol.item.ItemStack;

public final class ResultSlot extends Slot {

    public ResultSlot(InventoryStorage container, int slot) {
        super(container, slot);
    }

    @Override
    public boolean mayPlace(ItemStack p_40178_) {
        return false;
    }

    @Override
    public void onTake(PlayerData player, ItemStack p_150639_) {
        
    }
}
