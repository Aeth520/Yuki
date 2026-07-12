package cn.aetheris.yuki.core.plugin.stop;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.core.plugin.interfaces.Stop;
import org.bukkit.Bukkit;

public final class PluginChannelTerminate implements Stop {

    @Override
    public void stop() {
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(Yuki.getInstance());
    }
}
