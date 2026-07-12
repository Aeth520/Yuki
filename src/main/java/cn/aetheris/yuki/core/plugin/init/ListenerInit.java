package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.bukkit.*;
import cn.aetheris.yuki.listener.bukkit.hooks.*;
import cn.aetheris.yuki.listener.bukkit.misc.PlayerAsyncChatListener;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public final class ListenerInit implements Init {

    private final Yuki plugin = Yuki.getInstance();

    @Override
    public void init() {
        final ServerVersion serverVersion = plugin.getPacketEventsManager().getServerVersion();

        final List<Listener> listeners = Arrays.asList(
                new PlayerAsyncChatListener(),
                new PlayerAnimationListener(),
                new PlayerCommandListener(),
                new PlayerBlockPlaceListener(),
                new PlayerMovementListener(),
                new PlayerAttackListener(),
                new PlayerJoinQuitListener(),
                new ServerPistonListener(),
                new PlayerChatListener(),
                new PlayerConsumeListener(),
                new PluginDisableListener(),
                new ServerFreezeListener()
        );

        listeners.forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, Yuki.getInstance()));

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            Bukkit.getPluginManager().registerEvents(new PlayerInventoryListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new PlayerElytraListener(), plugin);
        }
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
            Bukkit.getPluginManager().registerEvents(new PlayerUseTridentListener(), plugin);
        }

        if (HookInit.getMythicMobsHook().isEnabled()) {
            Bukkit.getPluginManager().registerEvents(new MythicMobListener(), plugin);
        }

        if (PluginLoader.INSTANCE.getConfigManager().isHookWorldGuard()) {
            Bukkit.getPluginManager().registerEvents(new WorldGuardListener(), plugin);
            LogUtils.console("&3Yuki &8» &c检查到您开启了World Guard的Hook,这可能会造成一些绕过!");
        }

        LogUtils.console("&3Yuki &8» &aBukkitListener Initialized!");
    }
}
