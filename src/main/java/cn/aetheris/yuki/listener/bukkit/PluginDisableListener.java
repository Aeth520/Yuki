package cn.aetheris.yuki.listener.bukkit;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.bukkit.abstracts.AbstractListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;

public final class PluginDisableListener extends AbstractListener {

    @EventHandler()
    private void onDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().contains("Yuki") && !PluginLoader.INSTANCE.isDisable()) {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("服务器维护,稍后再试!"));
            }
            PluginLoader.INSTANCE.stop();
        }
    }
}
