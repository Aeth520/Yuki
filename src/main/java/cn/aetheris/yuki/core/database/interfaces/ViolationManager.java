package cn.aetheris.yuki.core.database.interfaces;

import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.OfflinePlayer;

import java.util.Map;

public interface ViolationManager {

    
    void logAlertSync(OfflinePlayer offlinePlayer, PlayerData player, Map<String, Double> violations);

    
    Map<String, Integer> getViolations(OfflinePlayer player);

}
