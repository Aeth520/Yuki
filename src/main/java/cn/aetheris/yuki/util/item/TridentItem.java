package cn.aetheris.yuki.util.item;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.latency.CompensatedWorld;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

public class TridentItem extends ItemBehaviour {

    public static TridentItem INSTANCE = new TridentItem();

    @Override
    public boolean canUse(ItemStack item, CompensatedWorld world, PlayerData player, InteractionHand hand) {
        if (this.nextDamageWillBreak(item)) {
            return false;
        }


        return !(item.getEnchantmentLevel(EnchantmentTypes.RIPTIDE) > 0F) || player.isInWaterOrRain();
    }

    private boolean nextDamageWillBreak(ItemStack item) {
        return item.isDamageableItem() && item.getDamageValue() >= item.getMaxDamage() - 1;
    }

}