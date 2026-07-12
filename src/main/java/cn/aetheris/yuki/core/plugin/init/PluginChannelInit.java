package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.listener.channel.GeyserChannelListener;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import org.bukkit.Bukkit;

public final class PluginChannelInit implements Init {
    @Override
    public void init() {
        if (PluginLoader.INSTANCE.getConfigManager().isHookGeyserBungee()) {
            Bukkit.getMessenger().registerIncomingPluginChannel(
                    Yuki.getInstance(),
                    "yuki:bedrock",
                    new GeyserChannelListener());
        }
    }
}
