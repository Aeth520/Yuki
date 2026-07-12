package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.core.plugin.hooks.ViaPipelineEnforcer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;

public final class ViaVersionCompatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ViaPipelineEnforcer.scheduleEnforce(event.getPlayer(), 10L);
        ViaPipelineEnforcer.scheduleEnforce(event.getPlayer(), 30L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        ViaPipelineEnforcer.scheduleEnforce(event.getPlayer(), 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if ("ViaVersion".equals(event.getPlugin().getName())) {
            ViaPipelineEnforcer.enforceAll();
        }
    }
}
