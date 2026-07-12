package cn.aetheris.yuki.util.inventory.menu;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.inventory.Inventory;
import cn.aetheris.yuki.util.inventory.InventoryStorage;
import cn.aetheris.yuki.util.inventory.slot.Slot;
import com.github.retrooper.packetevents.protocol.item.ItemStack;

public final class HopperMenu extends AbstractContainerMenu {
    public HopperMenu(PlayerData player, Inventory playerInventory) {
        super(player, playerInventory);

        InventoryStorage containerStorage = new InventoryStorage(5);
        for (int i = 0; i < 5; i++) {
            addSlot(new Slot(containerStorage, i));
        }

        addFourRowPlayerInventory();
    }

    @Override
    public ItemStack quickMoveStack(int slotID) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotID);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack1 = slot.getItem();
            itemstack = itemStack1.copy();
            if (slotID < 5) {
                if (!this.moveItemStackTo(itemStack1, 5, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack1, 0, 5, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
        }

        return itemstack;
    }

}
