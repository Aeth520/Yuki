package cn.aetheris.yuki.api.storage;

import cn.aetheris.yuki.core.database.entity.CheckInfo;
import cn.aetheris.yuki.core.database.interfaces.CheckInfoManager;
import cn.aetheris.yuki.core.database.interfaces.ViolationManager;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;

public final class CompositeStorage implements Storage {

    private final ViolationManager violationManager;
    private final CheckInfoManager checkInfoManager;

    public CompositeStorage(ViolationManager violationManager, CheckInfoManager checkInfoManager) {
        this.violationManager = violationManager;
        this.checkInfoManager = checkInfoManager;
    }

    public CompositeStorage(CheckInfoManager checkInfoManager) {
        this.violationManager = null;
        this.checkInfoManager = checkInfoManager;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }

    public CheckInfoManager getCheckInfoManager() {
        return checkInfoManager;
    }

    @Override
    public void logAlert(OfflinePlayer offlinePlayer, PlayerData player, boolean exp, String verbose,
                         String checkName, int violations, String description, String ping,
                         boolean lagging, boolean moveLagging, String tps, String brand, String version) {
        if (checkInfoManager != null) {
            checkInfoManager.logAlertSync(offlinePlayer, player, exp, verbose, checkName, violations,
                    description, ping, lagging, moveLagging, tps, brand, version);
        }
    }

    @Override
    public Map<String, Integer> getViolations(OfflinePlayer player) {
        if (violationManager != null) {
            return violationManager.getViolations(player);
        }
        return Map.of();
    }

    @Override
    public long getLogCount(OfflinePlayer player) {
        if (checkInfoManager != null) {
            return checkInfoManager.getLogCount(player);
        }
        return 0;
    }

    @Override
    public List<CheckInfo> getViolationLogs(OfflinePlayer player, int page, int limit) {
        if (checkInfoManager != null) {
            return checkInfoManager.getViolations(player, page, limit);
        }
        return List.of();
    }

    @Override
    public void clearLogs(OfflinePlayer player) {
        if (checkInfoManager != null) {
            checkInfoManager.clearLogs(player);
        }
    }

    @Override
    public void clearAllLogs() {
        if (checkInfoManager != null) {
            checkInfoManager.clearAllLogs();
        }
    }

    @Override
    public void close() {
    }
}
