package cn.aetheris.yuki.util.item;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.latency.CompensatedWorld;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

public class LegacyItem extends ItemBehaviour {

    public static final LegacyItem INSTANCE = new LegacyItem();

    @Override
    public boolean canUse(ItemStack item, CompensatedWorld world, PlayerData player, InteractionHand hand) {
        return false; 
    }

}