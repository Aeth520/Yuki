package cn.aetheris.yuki;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.listener.bukkit.PluginLoadListener;
import cn.aetheris.yuki.listener.bukkit.ViaVersionCompatListener;
import cn.aetheris.yuki.functionality.PacketEventsManager;
import cn.aetheris.yuki.platform.Platform;
import cn.aetheris.yuki.platform.bukkit.BukkitPlatform;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@Getter
@Setter
public final class Yuki extends JavaPlugin {

    @Getter
    private static Yuki instance;
    @Getter
    @Setter
    private static boolean enablePlugin;
    private PacketEventsManager packetEventsManager;
    private Platform platform;

    public Yuki() {
    }

    @Override
    public void onDisable() {
        Validate.notNull(instance);
        instance = null;
        Validate.notNull(packetEventsManager);
        HandlerList.unregisterAll(this);
        packetEventsManager.disable();
    }

    @Override
    public void onLoad() {
        instance = this;
        enablePlugin = false;
        platform = new BukkitPlatform(this);

        packetEventsManager = new PacketEventsManager();
        packetEventsManager.load();

        console("&3Yuki &8» &aPacketEvent Initialized!");
    }

    @Override
    public void onEnable() {
        instance = this;

        packetEventsManager.init();

        if (!platform.checkJavaVersion()) {
            MHDFScheduler.getGlobalRegionScheduler().runTaskLater(this, () -> {
                Bukkit.getServer().getPluginManager().disablePlugin(this);
            }, 60L);
            return;
        }

        try {
            Class.forName("cn.aetheris.yuki.Main")
                    .getDeclaredMethod("start")
                    .invoke(null);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to start Yuki", e);
        }
        platform.registerListener(new PluginLoadListener());
        platform.registerListener(new ViaVersionCompatListener());
    }

    public void disablePlugin() {
        setEnablePlugin(false);
        platform.disablePlugin();
    }

    public void console(final String info) {
        platform.console(info);
    }

    public Thread getMainThread() {
        return platform.getMainThread();
    }
}
