package cn.aetheris.yuki.util.item;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.latency.CompensatedWorld;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

public class UnsupportedItem extends ItemBehaviour {

    public static final UnsupportedItem INSTANCE = new UnsupportedItem();

    @Override
    public boolean canUse(ItemStack item, CompensatedWorld world, PlayerData player, InteractionHand hand) {
        return false;
    }

}