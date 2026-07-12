package cn.aetheris.yuki.util.inventory;

import cn.aetheris.yuki.util.latency.CompensatedInventory;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public final class EnchantmentHelper {

    public static int getMaximumEnchantLevel(CompensatedInventory inventory, EnchantmentType enchantmentType, ClientVersion clientVersion) {
        int maxEnchantLevel = 0;

        ItemStack helmet = inventory.getHelmet();
        if (helmet != ItemStack.EMPTY) {
            maxEnchantLevel = Math.max(maxEnchantLevel, helmet.getEnchantmentLevel(enchantmentType, clientVersion));
        }

        ItemStack chestplate = inventory.getChestplate();
        if (chestplate != ItemStack.EMPTY) {
            maxEnchantLevel = Math.max(maxEnchantLevel, chestplate.getEnchantmentLevel(enchantmentType, clientVersion));
        }

        ItemStack leggings = inventory.getLeggings();
        if (leggings != ItemStack.EMPTY) {
            maxEnchantLevel = Math.max(maxEnchantLevel, leggings.getEnchantmentLevel(enchantmentType, clientVersion));
        }

        ItemStack boots = inventory.getBoots();
        if (boots != ItemStack.EMPTY) {
            maxEnchantLevel = Math.max(maxEnchantLevel, boots.getEnchantmentLevel(enchantmentType, clientVersion));
        }

        return maxEnchantLevel;
    }
}