package cn.aetheris.yuki.platform.bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.platform.Platform;
import cn.aetheris.yuki.util.message.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Bukkit implementation of {@link Platform}.
 */
public final class BukkitPlatform implements Platform {

    private final Yuki plugin;
    private final Thread mainThread;

    public BukkitPlatform(Yuki plugin) {
        this.plugin = plugin;
        this.mainThread = Thread.currentThread();
    }

    @Override
    public Yuki getPlugin() {
        return plugin;
    }

    @Override
    public void console(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorUtils.color(message));
    }

    @Override
    public void disablePlugin() {
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            Bukkit.getOnlinePlayers().forEach(player ->
                    player.kickPlayer(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("kick.maintenance")));
        }
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    @Override
    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public boolean checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        int majorVersion;
        if (javaVersion.startsWith("1.")) {
            majorVersion = Integer.parseInt(javaVersion.split("\\.")[1]);
        } else {
            majorVersion = Integer.parseInt(javaVersion.split("\\.")[0]);
        }

        if (majorVersion < 21) {
            console("&3Yuki &8» &cYou are using Java &e" + javaVersion + "&c. Yuki requires Java 21 or higher.");
            console("&3Yuki &8» &cThe plugin will disable in 3 seconds!");
            return false;
        }
        return true;
    }

    @Override
    public Thread getMainThread() {
        return mainThread;
    }
}
