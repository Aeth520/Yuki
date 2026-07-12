package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.core.plugin.hooks.ViaPipelineEnforcer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public final class PacketEventsManager {
    PacketEventsAPI api;
    private ServerVersion serverVersion;

    public void load() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Yuki.getInstance()));
        PacketEvents.getAPI().getSettings()
                .fullStackTrace(true)
                .kickOnPacketException(true)
                .bStats(false)
                .checkForUpdates(false)
                .reEncodeByDefault(false)
                .debug(false);
        api = PacketEvents.getAPI();
        api.load();
        serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Object channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
                User user = new User(
                        channel,
                        ConnectionState.PLAY,
                        getServerVersion().toClientVersion(),
                        new UserProfile(player.getUniqueId(), player.getName())
                );
                PacketEvents.getAPI().getProtocolManager().setUser(channel, user);
            }
        }

        ViaPipelineEnforcer.enforceAll();
    }

    public void disable() {
        api.terminate();
        api = null;
    }

    public void init() {
        PacketEvents.getAPI().init();
    }
}
