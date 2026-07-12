package cn.aetheris.yuki.core.database;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.storage.Backend;
import cn.aetheris.yuki.api.storage.BackendRegistry;
import cn.aetheris.yuki.api.storage.CompositeStorage;
import cn.aetheris.yuki.api.storage.Storage;
import cn.aetheris.yuki.core.database.interfaces.CheckInfoManager;
import cn.aetheris.yuki.core.database.interfaces.DatabaseManager;
import cn.aetheris.yuki.core.database.interfaces.ViolationManager;
import cn.aetheris.yuki.core.database.type.MongoDBDatabaseManager;
import cn.aetheris.yuki.core.database.type.OrmLiteDatabaseManager;
import cn.aetheris.yuki.util.message.LogUtils;
import lombok.Getter;

public final class ViolationDatabaseManager {
    private final String dataManagerType;
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private CheckInfoManager checkInfoManager;
    @Getter
    private ViolationManager violationManager;
    @Getter
    private Storage storage;
    private Backend backend;

    public ViolationDatabaseManager() {
        this.dataManagerType = PluginLoader.INSTANCE.getConfigManager().getConfig()
                .getStringElse("database-manager.data-type", "sqlite")
                .toLowerCase();
    }

    public void start() {
        LogUtils.console("&3Yuki &8» &aDetects that &b" + dataManagerType + "&a is being used as the database schema.");
        LogUtils.console("&3Yuki &8» &aInitialising &b" + dataManagerType + "&a in database...");

        BackendRegistry registry = BackendRegistry.getInstance();
        registry.clear();
        registry.register(new OrmLiteDatabaseManager());
        registry.register(new MongoDBDatabaseManager());

        String resolvedType = resolveType(dataManagerType);
        backend = registry.getOrDefault(resolvedType, "sqlite");
        if (backend == null) {
            LogUtils.console("&3Yuki &8» &cNo storage backend available, defaulting to SQLite.");
            backend = registry.get("sqlite");
        }

        if (backend != null) {
            backend.start();
            this.databaseManager = (DatabaseManager) backend;
            this.storage = backend.getStorage();
            if (storage instanceof CompositeStorage composite) {
                this.checkInfoManager = composite.getCheckInfoManager();
                this.violationManager = composite.getViolationManager();
            }
        }

        LogUtils.console("&3Yuki &8» &aDatabase Initialized!");
    }

    private String resolveType(String type) {
        return switch (type) {
            case "mongodb" -> "mongodb";
            case "h2", "sqlite", "mysql", "mariadb" -> "sqlite";
            default -> {
                LogUtils.console("&3Yuki &8» &c" + type + " &cis unknown database type, default is SQLite.");
                yield "sqlite";
            }
        };
    }

    public void stop() {
        if (backend != null) {
            backend.stop();
        }
    }
}
