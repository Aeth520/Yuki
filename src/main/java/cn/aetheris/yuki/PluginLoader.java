package cn.aetheris.yuki;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.functionality.*;
import cn.aetheris.yuki.core.database.ViolationDatabaseManager;
import cn.aetheris.yuki.functionality.moderation.ModerationManager;
import cn.aetheris.yuki.functionality.crash.CrashManager;
import cn.aetheris.yuki.util.AntiCheatUtil;
import cn.aetheris.yuki.util.encrypt.AESUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
public final class PluginLoader {

    public static final PluginLoader INSTANCE = new PluginLoader();

    private final String version = Yuki.getInstance().getDescription().getVersion();
    private AlertManagerImpl alertManager;
    private SpectateManager spectateManager;
    private PlayerDataManager playerDataManager;
    private TickManager tickManager;
    private AntiCheatUtil externalAPI;
    private InitManager initManager;
    private LagManager lagManager;
    private ViolationDatabaseManager databaseManager;
    private ConfigManager configManager;
    private LangManager langManager;
    private DiscordWebhookManager discordWebhookManager;
    private ModerationManager moderationManager;
    private FeatureManager featureManager;
    @Setter
    private GeyserManager geyserManager;
    @Setter
    private boolean disable = false;

    public void start() {
        long startTime = System.currentTimeMillis();
        configManager = new ConfigManager();
        langManager = new LangManager();
        discordWebhookManager = new DiscordWebhookManager();
        featureManager = FeatureManager.getInstance();
        featureManager.loadFromConfig();
        AESUtil.init();
        databaseManager = new ViolationDatabaseManager();
        alertManager = new AlertManagerImpl();
        spectateManager = new SpectateManager();
        lagManager = new LagManager();
        playerDataManager = new PlayerDataManager();
        tickManager = new TickManager();
        externalAPI = new AntiCheatUtil();
        moderationManager = new ModerationManager();
        initManager = new InitManager();

        LogUtils.console(generateLogo());

        initManager.init();
        initManager.hook();

        MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {
            LogUtils.console("&3Yuki &8» &aAntiCheat enabled &7(&3" + (System.currentTimeMillis() - startTime) + "&7ms&a)");
        }, 10L);
    }


    private String generateLogo() {
        return "\n" +
                "&3━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "&f  Yuki &7AntiCheat\n" +
                "&7  Version &f" + version + "\n" +
                "&7  Combining &fMatrix&7, &fVulcan&7, &fMedusa&7, &fKarhu&7, &fHawk&7, &fGrimAC&7, &fRaven\n" +
                "&7  Author &fAetheris\n" +
                "&3━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    }

    public void stop() {
        setDisable(true);

        CrashManager.shutdown();

        if (initManager != null) {
            initManager.unHook();
            initManager.stop();
        }

        final Yuki plugin = Yuki.getInstance();

        if (plugin != null) {
            MHDFScheduler.getAsyncScheduler().runTaskLater(plugin, () -> {
                MHDFScheduler.cancel(plugin);
                plugin.disablePlugin();
            }, 20L);
            LogUtils.console("&3Yuki &8» &cGoodbye!");
        }
    }
}
