package cn.aetheris.yuki.core.tick.impl;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.tick.Tickable;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;

public final class DataTick implements Tickable {
    @Override
    public void tick() {
        if (Yuki.getInstance() == null || PluginLoader.INSTANCE.isDisable()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
                if (!ChannelHelper.isOpen(player.user.getChannel())) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Yuki.getInstance(), () ->
                            PluginLoader.INSTANCE.getPlayerDataManager().onDisconnect(player.user), 8L);
                    continue;
                }
                player.pollData();
            }
        });
    }
}
