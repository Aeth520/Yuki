package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.functionality.AbstractHook;
import org.bukkit.Bukkit;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public final class FloodgateHook extends AbstractHook {

    public FloodgateApi api;

    @Override
    public void hook() {
        if (Bukkit.getPluginManager().getPlugin("floodgate") != null) {
            api = FloodgateApi.getInstance();
            enabled = true;
        }
    }

    @Override
    public void unhook() {
        api = null;
        enabled = false;
    }

    public boolean isFloodgateUser(UUID uuid) {
        if (api == null || !enabled) {
            return false;
        }
        return api.isFloodgatePlayer(uuid);
    }
}
