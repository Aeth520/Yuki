package cn.aetheris.yuki.util.inventory.slot;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.inventory.EquipmentType;
import cn.aetheris.yuki.util.inventory.InventoryStorage;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;

public final class EquipmentSlot extends Slot {
    final EquipmentType type;

    public EquipmentSlot(EquipmentType type, InventoryStorage menu, int slot) {
        super(menu, slot);
        this.type = type;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack p_39746_) {
        return type == EquipmentType.getEquipmentSlotForItem(p_39746_);
    }

    public boolean mayPickup(PlayerData p_39744_) {
        ItemStack itemstack = this.getItem();
        return (itemstack.isEmpty() || p_39744_.gamemode == GameMode.CREATIVE || itemstack.getEnchantmentLevel(EnchantmentTypes.BINDING_CURSE, Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion()) == 0) && super.mayPickup(p_39744_);
    }
}
