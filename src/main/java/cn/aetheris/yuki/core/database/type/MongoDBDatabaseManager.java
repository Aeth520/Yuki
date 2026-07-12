package cn.aetheris.yuki.core.database.type;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.storage.Backend;
import cn.aetheris.yuki.api.storage.CompositeStorage;
import cn.aetheris.yuki.api.storage.Storage;
import cn.aetheris.yuki.core.database.dao.HistoryMongoDBUtil;
import cn.aetheris.yuki.core.database.interfaces.DatabaseManager;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public final class MongoDBDatabaseManager implements DatabaseManager, Backend {
    private final Plugin plugin;
    private MongoClient mongoClient;
    @Getter
    private MongoCollection<Document> violationsCollection;
    private CompositeStorage storage;
    private boolean started = false;

    public MongoDBDatabaseManager() {
        this.plugin = Yuki.getInstance();
    }

    private void setupMongoDB() {
        String host = PluginLoader.INSTANCE.getConfigManager().getConfig().getString("database-manager.mongodb.host");
        int port = Integer.parseInt(PluginLoader.INSTANCE.getConfigManager().getConfig().getString("database-manager.mongodb.port"));
        String databaseName = PluginLoader.INSTANCE.getConfigManager().getConfig().getString("database-manager.mongodb.database");
        String username = PluginLoader.INSTANCE.getConfigManager().getConfig().getString("database-manager.mongodb.username");
        String password = PluginLoader.INSTANCE.getConfigManager().getConfig().getString("database-manager.mongodb.password");
        String authDatabase = PluginLoader.INSTANCE.getConfigManager().getConfig().getString("database-manager.mongodb.authentication-database");

        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s", username, password, host, port, databaseName, authDatabase);
        mongoClient = MongoClients.create(connectionString);

        MongoDatabase database = mongoClient.getDatabase(databaseName);
        violationsCollection = database.getCollection("violations");

        violationsCollection.createIndex(new Document("uuid", 1));
        storage = new CompositeStorage(new HistoryMongoDBUtil(violationsCollection));
    }


    @Override
    public void start() {
        if (started) return;
        try {
            setupMongoDB();
            started = true;
            if (violationsCollection == null) {
                plugin.getLogger().log(Level.SEVERE, "MongoDB collection 'violations' does not exist.");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "MongoDB connection failed: ", e);
        }
    }

    @Override
    public void stop() {
        if (storage != null) {
            storage.close();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    public String getType() {
        return "mongodb";
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public boolean isAvailable() {
        return started && violationsCollection != null && storage != null;
    }
}
