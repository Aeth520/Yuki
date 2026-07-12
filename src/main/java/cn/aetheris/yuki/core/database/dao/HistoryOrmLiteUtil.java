package cn.aetheris.yuki.core.database.dao;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.database.entity.CheckInfo;
import cn.aetheris.yuki.core.database.interfaces.CheckInfoManager;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.List;

public final class HistoryOrmLiteUtil implements CheckInfoManager {
    private final Dao<CheckInfo, Integer> dao;

    public HistoryOrmLiteUtil(ConnectionSource connectionSource) {
        try {
            this.dao = DaoManager.createDao(connectionSource, CheckInfo.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logAlertSync(OfflinePlayer offlinePlayer, PlayerData player, boolean exp, String verbose, String checkName, int violations, String description, String ping, boolean lagging, boolean moveLagging, String tps, String brand, String version) {
        final String dataType = PluginLoader.INSTANCE.getConfigManager().getConfig().getString("database-manager.data-type");

        CheckInfo entity = new CheckInfo(
                HookInit.getPlaceholderAPIHook().setPlaceholders(offlinePlayer, PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("database-manager.server-name")),
                player.getUniqueId(),
                player.getName(),
                checkName,
                verbose,
                violations,
                description,
                exp,
                System.currentTimeMillis(),
                ping,
                lagging,
                moveLagging,
                tps,
                brand,
                version
        );

        if (dataType == null || dataType.equalsIgnoreCase("sqlite")) {
            try {
                this.dao.createOrUpdate(entity);
            } catch (Exception ex) {
                throw new RuntimeException("ORMLite 插入 CheckInfo 失败", ex);
            }
        } else {
            MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
                try {
                    this.dao.createOrUpdate(entity);
                } catch (Exception ex) {
                    throw new RuntimeException("ORMLite 插入 CheckInfo 失败", ex);
                }
            });
        }
    }

    @Override
    public long getLogCount(OfflinePlayer player) {
        try {
            return this.dao.queryBuilder()
                    .setCountOf(true)
                    .where()
                    .eq("uuid", player.getUniqueId())
                    .countOf();
        } catch (Exception ex) {
            throw new RuntimeException("ORMLite 查询Violation数量失败", ex);
        }
    }

    @Override
    public List<CheckInfo> getViolations(OfflinePlayer player, int page, int limit) {
        try {
            QueryBuilder<CheckInfo, Integer> qb = dao.queryBuilder();

            qb.orderBy("created_at", false);
            qb.where().eq("uuid", player.getUniqueId());

            qb.offset((long) (page - 1) * limit)
                    .limit((long) limit);

            return qb.query();
        } catch (Exception ex) {
            throw new RuntimeException("ORMLite 查询Violation失败", ex);
        }
    }

    @Override
    public void clearLogs(OfflinePlayer player) {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            try {
                DeleteBuilder<CheckInfo, Integer> db = this.dao.deleteBuilder();
                db.where().eq("uuid", player.getUniqueId());
                db.delete();
            } catch (Exception ex) {
                throw new RuntimeException("ORMLite 清除指定玩家日志失败", ex);
            }
        });
    }

    @Override
    public void clearAllLogs() {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            try {
                this.dao.deleteBuilder().delete();
            } catch (Exception ex) {
                throw new RuntimeException("ORMLite 清除全部日志失败", ex);
            }
        });
    }
}
