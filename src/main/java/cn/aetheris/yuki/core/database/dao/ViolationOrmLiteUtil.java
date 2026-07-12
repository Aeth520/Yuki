package cn.aetheris.yuki.core.database.dao;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.database.entity.Violation;
import cn.aetheris.yuki.core.database.interfaces.ViolationManager;
import cn.aetheris.yuki.player.PlayerData;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import lombok.SneakyThrows;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ViolationOrmLiteUtil implements ViolationManager {

    private final Dao<Violation, Integer> dao;

    public ViolationOrmLiteUtil(ConnectionSource connectionSource) {
        try {
            this.dao = DaoManager.createDao(connectionSource, Violation.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logAlertSync(OfflinePlayer offlinePlayer, PlayerData player, Map<String, Double> violations) {
        UUID playerId = offlinePlayer.getUniqueId();
        String uuidStr = playerId.toString();
        String dataType = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("database-manager.data-type", "sqlite").toLowerCase();
        String ip = getPlayerIp(player);
        if (dataType.equals("sqlite")) {

            deleteExistingViolations(uuidStr);

            violations.forEach((checkName, severity) -> {
                Violation violation = new Violation(severity.intValue(), playerId, checkName, ip);
                createOrUpdateViolation(violation);
            });
        } else {
            MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
                deleteExistingViolations(uuidStr);

                violations.forEach((checkName, severity) -> {
                    Violation violation = new Violation(severity.intValue(), playerId, checkName, ip);
                    createOrUpdateViolation(violation);
                });
            });
        }
    }

    private String getPlayerIp(PlayerData player) {
        try {
            if (player.getUser() != null && player.getUser().getAddress() != null) {
                return player.getUser().getAddress().getHostString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SneakyThrows
    private void deleteExistingViolations(String uuid) {
        dao.delete(dao.queryBuilder()
                .where().eq("uuid", uuid).query());
    }

    private void createOrUpdateViolation(Violation violation) {
        try {
            dao.createOrUpdate(violation);
        } catch (SQLException e) {
            throw new RuntimeException("保存违规记录失败: " + violation.getCheckName(), e);
        }
    }

    @Override
    public Map<String, Integer> getViolations(OfflinePlayer player) {
        Map<String, Integer> result = new HashMap<>();
        String uuid = player.getUniqueId().toString();

        try {
            List<Violation> violations = dao.queryBuilder()
                    .where().eq("uuid", uuid).query();

            violations.forEach(v ->
                    result.put(v.getCheckName(), v.getVl())
            );
        } catch (SQLException e) {
            throw new RuntimeException("获取违规记录失败: " + uuid, e);
        }
        return result;
    }
}
