package cn.aetheris.yuki.core.database.type;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.storage.Backend;
import cn.aetheris.yuki.api.storage.CompositeStorage;
import cn.aetheris.yuki.api.storage.Storage;
import cn.aetheris.yuki.core.database.dao.HistoryOrmLiteUtil;
import cn.aetheris.yuki.core.database.dao.ViolationOrmLiteUtil;
import cn.aetheris.yuki.core.database.entity.CheckInfo;
import cn.aetheris.yuki.core.database.entity.Violation;
import cn.aetheris.yuki.core.database.interfaces.DatabaseManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;

public final class OrmLiteDatabaseManager implements DatabaseManager, Backend {
    private final Plugin plugin;
    @Getter
    private ConnectionSource connectionSource;
    private HikariDataSource hikariDataSource;
    private CompositeStorage storage;

    public OrmLiteDatabaseManager() {
        this.plugin = Yuki.getInstance();
    }

    private static boolean isLegacySQL(Class<?> driverClass) {
        String version = driverClass.getPackage().getImplementationVersion();

        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);

        return (major < 8) ||
                (major == 8 && minor == 0 && patch < 28);
    }

    @Override
    public void start() {
        try {
            String dataType = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("database-manager.data-type", "sqlite").toLowerCase();

            if (dataType.equals("mariadb")) {
                dataType = "mysql";
            }

            String jdbcUrl;
            HikariConfig config = new HikariConfig();

            switch (dataType) {
                case "mysql":
                    String host = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("database-manager.mysql.host", "localhost");
                    int port = PluginLoader.INSTANCE.getConfigManager().getConfig().getIntElse("database-manager.mysql.port", 3306);
                    String database = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("database-manager.mysql.database", "test");
                    String username = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("database-manager.mysql.username", "root");
                    String password = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("database-manager.mysql.password", "");
                    boolean ssl = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("database-manager.mysql.ssl", false);
                    String sslParam = "sslMode=DISABLED";
                    try {

                        Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver");
                        final boolean isLegacy = isLegacySQL(driverClass);

                        if (isLegacy) {
                            sslParam = "useSSL=false";
                        }
                    } catch (Exception e) {
                        sslParam = "useSSL=false";
                    }

                    jdbcUrl = ssl ? String.format(
                            "jdbc:mysql://%s:%d/%s",
                            host,
                            port,
                            database
                    ) : String.format(
                            "jdbc:mysql://%s:%d/%s?%s",
                            host,
                            port,
                            database,
                            sslParam
                    );
                    config.setJdbcUrl(jdbcUrl);
                    config.setUsername(username);
                    config.setPassword(password);
                    config.addDataSourceProperty("serverTimezone", TimeZone.getDefault().getID());
                    config.addDataSourceProperty("useSSL", "false");
                    break;

                case "sqlite":
                    try {
                        Class.forName("org.sqlite.JDBC");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("数据库驱动加载失败");
                    }
                    File dbFile = new File(plugin.getDataFolder(), "data.sqlite");
                    jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                    config.setJdbcUrl(jdbcUrl);
                    config.setConnectionTestQuery("SELECT 1");
                    break;

                case "h2":
                default:
                    try {
                        Class.forName("org.h2.Driver");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("数据库驱动加载失败");
                    }
                    File h2FileDefault = new File(plugin.getDataFolder(), "h2/");
                    jdbcUrl = "jdbc:h2:file:" + h2FileDefault.getAbsolutePath();
                    config.setJdbcUrl(jdbcUrl);
                    break;
            }

            config.setPoolName("Yuki-" + dataType + "-Pool");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.addDataSourceProperty("useUnicode", "true");
            config.addDataSourceProperty("characterEncoding", "utf8");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("autoReconnect", "true");
            hikariDataSource = new HikariDataSource(config);
            connectionSource = new DataSourceConnectionSource(hikariDataSource, jdbcUrl);
            TableUtils.createTableIfNotExists(connectionSource, CheckInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, Violation.class);
            storage = new CompositeStorage(
                    new ViolationOrmLiteUtil(connectionSource),
                    new HistoryOrmLiteUtil(connectionSource)
            );

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "数据库连接失败: ", ex);
        }
    }

    @Override
    public void stop() {
        if (storage != null) {
            storage.close();
        }
        if (connectionSource != null) {
            try {
                connectionSource.close();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "关闭连接源失败", e);
            }
        }
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
        }
    }

    @Override
    public String getType() {
        return "sqlite";
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public boolean isAvailable() {
        return connectionSource != null && storage != null;
    }
}
