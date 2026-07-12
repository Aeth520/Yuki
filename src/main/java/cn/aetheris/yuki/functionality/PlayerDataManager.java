package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.combat.analysis.AnalysisA;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.maps.ConcurrentReferenceHashMap;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {

    public final Set<User> exemptUsers = Collections.newSetFromMap(new ConcurrentReferenceHashMap<>());
    @Getter
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    
    public PlayerData getPlayer(@NotNull OfflinePlayer player) {
        if (playerDataMap.containsKey(player.getUniqueId())) {
            return playerDataMap.get(player.getUniqueId());
        } else {
            return playerDataMap.get(new PlayerData(createUser(player)));
        }
    }

    @Nullable
    public PlayerData getPlayer(final @NonNull UUID uuid) {
        Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(uuid);
        User user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        return getPlayer(user);
    }

    
    public boolean shouldCheck(@NotNull User user) {
        if (exemptUsers.contains(user) || !ChannelHelper.isOpen(user.getChannel())) {
            return false;
        }

        if (user.getUUID() != null) {
            Player player = Bukkit.getPlayer(user.getUUID());
            if (player != null && player.hasPermission("yuki.exempt")) {
                exemptUsers.add(user);
                return false;
            }
        }
        return true;
    }

    
    @Nullable
    public PlayerData getPlayer(@NotNull User user) {
        return user.getUUID() != null ? playerDataMap.get(user.getUUID()) : null;
    }

    
    public void addUser(@NotNull User user) {
        if (shouldCheck(user)) {
            if (exemptUsers.contains(user)) {
                return;
            }
            playerDataMap.put(user.getUUID(), new PlayerData(user));
        }
    }

    
    public void remove(@Nullable User user) {
        if (user != null && user.getUUID() != null) {
            playerDataMap.remove(user.getUUID());
        }
    }

    
    public Collection<PlayerData> getEntries() {
        return playerDataMap.values();
    }

    
    public int size() {
        return playerDataMap.size();
    }

    
    public User createUser(@NotNull OfflinePlayer player) {
        final Channel channel = (Channel) PacketEvents.getAPI().getPlayerManager().getChannel(player);
        return new User(
                channel,
                ConnectionState.PLAY,
                Yuki.getInstance().getPacketEventsManager().getServerVersion().toClientVersion(),
                new UserProfile(player.getUniqueId(), player.getName())
        );
    }

    public void onDisconnect(User user) {
        exemptUsers.remove(user);
        remove(user);

        UUID uuid = user.getProfile().getUUID();

        if (uuid == null)
            return;

        PluginLoader.INSTANCE.getAlertManager().handlePlayerQuit(
                uuid
        );

        PluginLoader.INSTANCE.getSpectateManager().onQuit(uuid);

        AnalysisA.DEBUG_PLAYERS.remove(user.getName());
    }
}