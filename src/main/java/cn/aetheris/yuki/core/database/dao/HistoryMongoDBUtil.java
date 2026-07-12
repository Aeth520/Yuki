package cn.aetheris.yuki.core.database.dao;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.database.entity.CheckInfo;
import cn.aetheris.yuki.core.database.interfaces.CheckInfoManager;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HistoryMongoDBUtil implements CheckInfoManager {
    private final MongoCollection<Document> violationsCollection;

    public HistoryMongoDBUtil(MongoCollection<Document> violationsCollection) {
        this.violationsCollection = violationsCollection;
    }

    @Override
    public void logAlertSync(OfflinePlayer offlinePlayer, PlayerData player, boolean exp, String verbose, String checkName, int violations, String description, String ping, boolean lagging, boolean moveLagging, String tps, String brand, String version) {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            try {
                Document document = new Document()
                        .append("server", HookInit.getPlaceholderAPIHook().setPlaceholders(offlinePlayer, PluginLoader.INSTANCE.getLangManger().i18nWithoutPrefix("database-manager.server-name")))
                        .append("uuid", player.getUniqueId())
                        .append("player_name", player.getName())
                        .append("check_name", checkName)
                        .append("exp", exp)
                        .append("verbose", verbose)
                        .append("vl", violations)
                        .append("description", description)
                        .append("created_at", System.currentTimeMillis())
                        .append("ping", ping)
                        .append("lagging", lagging)
                        .append("move_lagging", moveLagging)
                        .append("tps", tps)
                        .append("brand", brand)
                        .append("version", version);

                this.violationsCollection.insertOne(document);
            } catch (Exception ex) {
                throw new RuntimeException("MongoDB 插入Violation失败", ex);
            }
        });
    }

    @Override
    public long getLogCount(OfflinePlayer player) {
        try {
            return violationsCollection.countDocuments(
                    new Document("uuid", player.getUniqueId())
            );
        } catch (Exception ex) {
            throw new RuntimeException("MongoDB 查询Violation数量失败", ex);
        }
    }

    @Override
    public List<CheckInfo> getViolations(OfflinePlayer player, int page, int limit) {
        try {
            List<CheckInfo> checkInfos = new ArrayList<>();

            Document document = new Document()
                    .append("uuid", player.getUniqueId());

            for (Document doc : violationsCollection.find(document)
                    .sort(new Document("created_at", -1))
                    .skip((page - 1) * limit)
                    .limit(limit)) {
                checkInfos.add(new CheckInfo(
                        doc.getString("server"),
                        UUID.fromString(doc.getString("uuid")),
                        doc.getString("player_name"),
                        doc.getString("check_name"),
                        doc.getString("verbose"),
                        doc.getInteger("vl"),
                        doc.getString("description"),
                        doc.getBoolean("exp"),
                        doc.getLong("created_at"),
                        doc.getString("ping"),
                        doc.getBoolean("lagging"),
                        doc.getBoolean("move_lagging"),
                        doc.getString("tps"),
                        doc.getString("brand"),
                        doc.getString("version")
                ));
            }

            return checkInfos;
        } catch (Exception ex) {
            throw new RuntimeException("MongoDB 查询Violation失败", ex);
        }
    }

    @Override
    public void clearLogs(OfflinePlayer player) {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            try {
                Document document = new Document()
                        .append("uuid", player.getUniqueId());

                violationsCollection.deleteMany(document);
            } catch (Exception ex) {
                throw new RuntimeException("MongoDB 清除指定玩家日志失败", ex);
            }
        });
    }

    @Override
    public void clearAllLogs() {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            try {
                violationsCollection.deleteMany(new Document());
            } catch (Exception ex) {
                throw new RuntimeException("MongoDB 清除全部日志失败", ex);
            }
        });
    }
}
