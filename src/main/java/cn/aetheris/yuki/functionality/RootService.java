package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.database.ViolationDatabaseManager;
import cn.aetheris.yuki.functionality.crash.CrashManager;
import cn.aetheris.yuki.functionality.moderation.ModerationManager;
import cn.aetheris.yuki.util.AntiCheatUtil;
import cn.aetheris.yuki.util.encrypt.AESUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import org.bukkit.Bukkit;

/**
 * Composition root and lifecycle owner.
 * Startup order: config -> i18n -> infrastructure -> storage -> domain -> listeners -> hooks -> runtime
 * Shutdown order: reverse of startup.
 */
public final class RootService {

    private final Yuki plugin;
    private final long startTime;

    public RootService(Yuki plugin) {
        this.plugin = plugin;
        this.startTime = System.currentTimeMillis();
    }

    // --- Phase 1: Configuration & i18n ---

    private void initConfig() {
        PluginLoader.INSTANCE.configManager = new ConfigManager();
        PluginLoader.INSTANCE.langManager = new LangManager();
        PluginLoader.INSTANCE.featureManager = FeatureManager.getInstance();
        PluginLoader.INSTANCE.featureManager.loadFromConfig();
        PluginLoader.INSTANCE.discordWebhookManager = new DiscordWebhookManager();
        AESUtil.init();
    }

    // --- Phase 2: Infrastructure & storage ---

    private void initInfrastructure() {
        PluginLoader.INSTANCE.databaseManager = new ViolationDatabaseManager();
        PluginLoader.INSTANCE.externalAPI = new AntiCheatUtil();
    }

    // --- Phase 3: Domain services ---

    private void initDomain() {
        PluginLoader.INSTANCE.alertManager = new AlertManagerImpl();
        PluginLoader.INSTANCE.spectateManager = new SpectateManager();
        PluginLoader.INSTANCE.lagManager = new LagManager();
        PluginLoader.INSTANCE.tickManager = new TickManager();
        PluginLoader.INSTANCE.moderationManager = new ModerationManager();
    }

    // --- Phase 4: Player context ---

    private void initPlayerContext() {
        PluginLoader.INSTANCE.playerDataManager = new PlayerDataManager();
    }

    // --- Phase 5: Commands, listeners, hooks, ticks ---

    private void initRuntime() {
        PluginLoader.INSTANCE.initManager = new InitManager();
        PluginLoader.INSTANCE.initManager.init();
        PluginLoader.INSTANCE.initManager.hook();
    }

    // --- Phase 6: Runtime open ---

    private void openRuntime() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            LogUtils.consolePrefixed("&aAntiCheat enabled &7(&3" + (System.currentTimeMillis() - startTime) + "&7ms&a)");
        }, 10L);
    }

    // --- Startup ---

    public void start() {
        initConfig();
        initInfrastructure();
        initDomain();
        initPlayerContext();
        LogUtils.console(generateLogo());
        initRuntime();
        openRuntime();
    }

    // --- Shutdown (reverse order) ---

    public void stop() {
        PluginLoader.INSTANCE.setDisable(true);

        // Phase 5 reverse: unhook and stop
        CrashManager.shutdown();
        if (PluginLoader.INSTANCE.initManager != null) {
            PluginLoader.INSTANCE.initManager.unHook();
            PluginLoader.INSTANCE.initManager.stop();
        }

        // Phase 4 reverse: player context
        // (PlayerDataManager is cleaned up via initManager.stop())

        // Phase 3 reverse: domain services
        // Phase 2 reverse: infrastructure
        // Phase 1 reverse: config

        if (plugin != null) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                Bukkit.getScheduler().cancelTasks(plugin);
                plugin.disablePlugin();
            }, 20L);
            LogUtils.consolePrefixed("&cGoodbye!");
        }
    }

    private String generateLogo() {
        String version = plugin.getDescription().getVersion();
        return "\n" +
                "&3━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "&f  Yuki &7AntiCheat\n" +
                "&7  Version &f" + version + "\n" +
                "&7  Combining &fMatrix&7, &fVulcan&7, &fMedusa&7, &fKarhu&7, &fHawk&7, &fGrimAC&7, &fRaven\n" +
                "&7  Author &fAetheris\n" +
                "&3━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    }
}