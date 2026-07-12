package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.functionality.AbstractHook;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class PlaceholderAPIHook extends AbstractHook {

    @Override
    public void hook() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            super.enabled = true;
        }
    }

    @Override
    public void unhook() {
        super.enabled = false;
    }

    public String setPlaceholders(OfflinePlayer player, String string) {
        return enabled
                ? PlaceholderAPI.setPlaceholders(player, string) : string;
    }
}
