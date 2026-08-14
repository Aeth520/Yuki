package cn.aetheris.yuki.core.plugin.init;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;

public final class LimiterInit implements Init {
    @Override
    public void init() {
        LogUtils.consolePrefixed("&aPacketLimiter Initialized!");
        Bukkit.getScheduler().runTaskTimerAsynchronously(Yuki.getInstance(), () -> {
            for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
                player.cancelledPackets.set(0);
            }
        }, 1, 40);
    }
}
