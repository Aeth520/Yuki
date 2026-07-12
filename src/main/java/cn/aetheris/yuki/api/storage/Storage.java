package cn.aetheris.yuki.api.storage;

import cn.aetheris.yuki.core.database.entity.CheckInfo;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;

public interface Storage {

    void logAlert(OfflinePlayer offlinePlayer, PlayerData player, boolean exp, String verbose,
                  String checkName, int violations, String description, String ping,
                  boolean lagging, boolean moveLagging, String tps, String brand, String version);

    Map<String, Integer> getViolations(OfflinePlayer player);

    long getLogCount(OfflinePlayer player);

    List<CheckInfo> getViolationLogs(OfflinePlayer player, int page, int limit);

    void clearLogs(OfflinePlayer player);

    void clearAllLogs();

    void close();
}
