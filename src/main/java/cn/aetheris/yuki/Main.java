package cn.aetheris.yuki;

import org.bukkit.Bukkit;


public final class Main {

    public static void start() {
        Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
            PluginLoader.INSTANCE.start();
            Bukkit.getScheduler().runTaskLater(Yuki.getInstance(), () ->
                    Yuki.setEnablePlugin(true), 20L);
        });
    }
}
