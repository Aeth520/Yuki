package cn.aetheris.yuki.block.place;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockPlace;

public interface BlockPlaceFactory {
    void applyBlockPlaceToWorld(PlayerData player, BlockPlace place);
}
