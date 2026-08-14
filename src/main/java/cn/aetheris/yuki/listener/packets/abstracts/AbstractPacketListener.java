package cn.aetheris.yuki.listener.packets.abstracts;

import org.bukkit.Bukkit;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class AbstractPacketListener extends PacketListenerAbstract {

    private final PacketListenerPriority priority;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public AbstractPacketListener(@NotNull PacketListenerPriority priority) {
        this.priority = priority;
    }

    public AbstractPacketListener() {
        this.priority = PacketListenerPriority.NORMAL;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(Yuki.getInstance(), () -> {
                User user = event.getUser();
                PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(user);
                if (data == null) {
                    return;
                }
                if (!PluginLoader.INSTANCE.getPlayerDataManager().shouldCheck(user)) {
                    return;
                }
                if (playerDataMap.containsKey(user.getUUID())) {
                    return;
                }
                playerDataMap.putIfAbsent(user.getUUID(), data);
            }, 10L);
        }
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        User user = event.getUser();
        if (user == null) {
            return;
        }
        UUID uuid = user.getUUID();
        if (uuid == null) {
            return;
        }
        playerDataMap.remove(uuid);
    }


    protected PlayerData getData(User user) {
        return playerDataMap.get(user.getUUID()) == null ? PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(user) : playerDataMap.get(user.getUUID());
    }
}