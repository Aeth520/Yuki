package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.util.message.LogUtils;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class MythicMobsHook extends AbstractHook {
    @Override
    public void hook() {
        final Plugin mythicMobs = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (PluginLoader.INSTANCE.getConfigManager().isHookMythicMobs() && mythicMobs != null) {
            if (!mythicMobs.getDescription().getVersion().startsWith("5")) {
                LogUtils.consolePrefixed("&cMythicMobs的版本不受支持! (" + mythicMobs.getDescription().getVersion() + ")!");
                return;
            }
            enabled = true;
        }
    }

    @Override
    public void unhook() {
        enabled = false;
    }

    public boolean mythicMobItem(ItemStack item) {
        if (enabled) {
            return MythicBukkit.inst().getItemManager().isMythicItem(item);
        }
        return false;
    }
}
