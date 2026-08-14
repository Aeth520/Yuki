package cn.aetheris.yuki;

import cn.aetheris.yuki.functionality.*;
import cn.aetheris.yuki.core.database.ViolationDatabaseManager;
import cn.aetheris.yuki.functionality.moderation.ModerationManager;
import cn.aetheris.yuki.util.AntiCheatUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * Singleton service locator and lifecycle adapter.
 * Delegates startup/shutdown to {@link RootService}.
 */
@Getter
public final class PluginLoader {

    public static final PluginLoader INSTANCE = new PluginLoader();

    private final String version = Yuki.getInstance().getDescription().getVersion();

    // --- Services (public for RootService wiring, accessed via getters by consumers) ---
    public ConfigManager configManager;
    public LangManager langManager;
    public DiscordWebhookManager discordWebhookManager;
    public FeatureManager featureManager;
    public ViolationDatabaseManager databaseManager;
    public AntiCheatUtil externalAPI;
    public AlertManagerImpl alertManager;
    public SpectateManager spectateManager;
    public LagManager lagManager;
    public TickManager tickManager;
    public ModerationManager moderationManager;
    public PlayerDataManager playerDataManager;
    public InitManager initManager;
    @Setter
    GeyserManager geyserManager;
    @Setter
    private boolean disable = false;

    private PluginLoader() {}

    public void start() {
        new RootService(Yuki.getInstance()).start();
    }

    public void stop() {
        new RootService(Yuki.getInstance()).stop();
    }
}