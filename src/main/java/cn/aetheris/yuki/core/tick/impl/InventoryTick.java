package cn.aetheris.yuki.core.tick.impl;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.tick.Tickable;
import cn.aetheris.yuki.player.PlayerData;

public final class InventoryTick implements Tickable {
    @Override
    public void tick() {
        if (Yuki.getInstance() == null || PluginLoader.INSTANCE.isDisable()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
                player.getInventory().inventory.getInventoryStorage().tickWithBukkit();
            }
        });
    }
}
