package cn.aetheris.yuki.core.database.interfaces;

import cn.aetheris.yuki.core.database.entity.CheckInfo;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface CheckInfoManager {

    void logAlertSync(OfflinePlayer offlinePlayer, boolean exp, String verbose, String checkName, int violations, String description, String ping, boolean lagging, boolean moveLagging, String tps, String brand, String version);

    
    long getLogCount(OfflinePlayer player);

    
    List<CheckInfo> getViolations(OfflinePlayer player, int page, int limit);

    
    void clearLogs(OfflinePlayer player);

    
    void clearAllLogs();
}
