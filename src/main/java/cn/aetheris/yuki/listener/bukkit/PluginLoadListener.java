package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.Yuki;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PluginLoadListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!Yuki.isEnablePlugin()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "服务器加载中...");
        }
    }
}
