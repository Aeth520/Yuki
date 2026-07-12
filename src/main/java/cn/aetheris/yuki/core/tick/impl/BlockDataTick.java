package cn.aetheris.yuki.core.tick.impl;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.tick.Tickable;
import cn.aetheris.yuki.player.PlayerData;

public final class BlockDataTick implements Tickable {

    @Override
    public void tick() {
        if (Yuki.getInstance() == null || PluginLoader.INSTANCE.isDisable()) {
            return;
        }
        for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
            player.blockHistory.cleanup(PluginLoader.INSTANCE.getTickManager().currentTick - 2);
        }
    }
}