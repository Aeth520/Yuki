package cn.aetheris.yuki.core.plugin.init;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;

public class SyncInit implements Init {
    @Override
    public void init() {
        if (Yuki.getInstance() == null || PluginLoader.INSTANCE.isDisable()) {
            return;
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(Yuki.getInstance(),
                () -> PluginLoader.INSTANCE.getTickManager().tickSync(), 0, 1);
        Bukkit.getScheduler().runTaskTimerAsynchronously(Yuki.getInstance(),
                () -> PluginLoader.INSTANCE.getTickManager().tickAsync(), 0, 1);
    }
}

