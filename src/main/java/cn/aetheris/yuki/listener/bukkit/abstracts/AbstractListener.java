package cn.aetheris.yuki.listener.bukkit.abstracts;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractListener implements Listener {

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {
            PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(player);
            if (data == null) {
                return;
            }
            if (!PluginLoader.INSTANCE.getPlayerDataManager().shouldCheck(data.getUser())) {
                return;
            }
            if (playerDataMap.containsKey(player.getUniqueId())) {
                return;
            }
            playerDataMap.putIfAbsent(player.getUniqueId(), data);
        }, 20L);
    }

    protected PlayerData getData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    protected PlayerData getData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        playerDataMap.remove(event.getPlayer().getUniqueId());
    }
}