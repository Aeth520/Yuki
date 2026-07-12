package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.yaml.YamlUtil;
import github.scarsz.configuralize.DynamicConfig;
import github.scarsz.configuralize.Language;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Getter
public final class ConfigManager {

    private final File settingsFile;
    private final File messagesFile;
    private final File checkConfigFile;
    private final File punishFile;
    private final File databaseFile;
    private final List<Pattern> ignoredClientPatterns = new LinkedList<>();
    private final DynamicConfig config;
    private final Language language;

    private int maxPingTransaction = 120;
    private boolean experimentalChecks, ignoreDuplicatePacketRotation, highPingKick;
    private double highPingThreshold;
    private int highPingDuration;

    private boolean mitigateUseItem, mitigateReduceDamage, mitigateNoSlow,
            mitigateNoSlowNormal, mitigateNoSlowInventory, mitigateNoSlowDropItem, mitigateNoSlowChangeSlot,
            mitigateInvalidBreak, mitigatePos, mitigatePosSpoof, mitigatePosTeleport, mitigatePosInvalid,
            mitigateElytra, mitigateGhostElytra, mitigateElytraInvalidAction, mitigateElytraOnGround,
            mitigateElytraSprint, mitigateTrident, mitigateTridentRiptiding, mitigateTridentUse,
            mitigateVelocity, mitigateVelocityJump, mitigateVelocityInvalid, mitigateVisual, mitigateVisualEquipment,
            mitigateVisualMetaData, mitigateAutoTotem, mitigateInventory;

    private boolean hookMythicMobs, hookGeyser, hookGeyserBrand, hookGeyserAPI, hookGeyserUUID,
            hookGeyserBungee, hookFoliaTPS, hookMyPet, hook7yPay, hookWorldGuard;

    public ConfigManager() {
        settingsFile = new File(Yuki.getInstance().getDataFolder(), "settings.yml");
        messagesFile = new File(Yuki.getInstance().getDataFolder(), "messages.yml");
        checkConfigFile = new File(Yuki.getInstance().getDataFolder(), "check.yml");
        punishFile = new File(Yuki.getInstance().getDataFolder(), "punishments.yml");
        databaseFile = new File(Yuki.getInstance().getDataFolder(), "database.yml");

        if (!Yuki.getInstance().getDataFolder().exists()) {
            Yuki.getInstance().getDataFolder().mkdirs();
        }

        Language language = detectLanguage();
        this.language = language;
        config = new DynamicConfig(language);
        config.addSource(Yuki.class, "settings", getSettingsFile());
        config.addSource(Yuki.class, "databases", getDatabaseFile());
        config.addSource(Yuki.class, "messages", getMessagesFile());
        config.addSource(Yuki.class, "punishments", getPunishFile());
        config.addSource(Yuki.class, "checks", getCheckConfigFile());

        reload();
    }

    private Language detectLanguage() {
        if (settingsFile.exists()) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(settingsFile);
                String lang = yaml.getString("language", "zh").toLowerCase();
                if ("en".equals(lang)) return Language.EN;
            } catch (Exception ignored) {
            }
        }
        return Language.ZH;
    }

    private String getLanguageCode() {
        return language == Language.EN ? "en" : "zh";
    }

    public void reload() {
        try {
            config.saveAllDefaults(false);
        } catch (IOException e) {
            throw new RuntimeException("保存默认配置文件失败", e);
        }

        try {
            config.loadAll();
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件失败", e);
        }

        String lang = getLanguageCode();
        YamlUtil.updateConfig(getSettingsFile(), "settings/" + lang + ".yml");
        YamlUtil.updateConfig(getMessagesFile(), "messages/" + lang + ".yml");
        YamlUtil.updateConfig(getCheckConfigFile(), "checks/" + lang + ".yml");
        YamlUtil.updateConfig(getDatabaseFile(), "databases/" + lang + ".yml");
        if (getConfig().getBooleanElse("function.config-refresh.punishments", false)) {
            YamlUtil.updateConfig(getPunishFile(), "punishments/" + lang + ".yml");
        }

        final int configuredMaxTransactionTime = config.getIntElse("limit.transaction-timeout", 60);
        maxPingTransaction = MathUtil.clamp(configuredMaxTransactionTime, 1, 180);
        if (maxPingTransaction != configuredMaxTransactionTime) {
            Yuki.getInstance().getLogger().warning("检测到无效的最大事务时间！该值已被限制在 1 到 180 之间以防止问题。"
                    + "更改: " + configuredMaxTransactionTime + " -> " + maxPingTransaction);
            Yuki.getInstance().getLogger().warning("尝试禁用或设置过高可能导致内存使用问题。");
        }

        ignoredClientPatterns.clear();
        for (String string : config.getStringList("output.client-brand.ignores")) {
            try {
                ignoredClientPatterns.add(Pattern.compile(string));
            } catch (PatternSyntaxException e) {
                throw new RuntimeException("编译客户端品牌正则表达式失败", e);
            }
        }

        experimentalChecks = config.getBooleanElse("function.experimental", false);
        ignoreDuplicatePacketRotation = config.getBooleanElse("mitigates.duplicate.ignore", false);

        highPingKick = config.getBooleanElse("function.transaction.kick-high-ping.enable", false);
        highPingThreshold = config.getDoubleElse("function.transaction.kick-high-ping.max-ping", 300);
        highPingDuration = config.getIntElse("function.transaction.kick-high-ping.duration", 8);

        hookMythicMobs = config.getBooleanElse("hooks.mythicmobs", false);
        hookGeyserBrand = config.getBooleanElse("hooks.geyser.brand", false);
        hookGeyserAPI = config.getBooleanElse("hooks.geyser.api", false);
        hookGeyserUUID = config.getBooleanElse("hooks.geyser.uuid", false);
        hookGeyserBungee = config.getBooleanElse("hooks.geyser.bungee", false);
        hookGeyser = config.getBooleanElse("hooks.geyser.enable", false);
        hookFoliaTPS = config.getBooleanElse("hooks.luminol", false);
        hookMyPet = config.getBooleanElse("hooks.mypet", false);
        hook7yPay = config.getBooleanElse("hooks.7-pay", false);
        hookWorldGuard = config.getBooleanElse("hooks.world-guard", false);

        mitigateUseItem = config.getBooleanElse("mitigates.attack.use-item", false);
        mitigateReduceDamage = config.getBooleanElse("mitigates.attack.reduce-damage", false);
        mitigateNoSlow = config.getBooleanElse("mitigates.no-slow.enable", false);
        mitigateNoSlowNormal = config.getBooleanElse("mitigates.no-slow.normal", false) && mitigateNoSlow;
        mitigateNoSlowInventory = config.getBooleanElse("mitigates.no-slow.inventory", false) && mitigateNoSlow;
        mitigateNoSlowDropItem = config.getBooleanElse("mitigates.no-slow.drop-item", false) && mitigateNoSlow;
        mitigateNoSlowChangeSlot = config.getBooleanElse("mitigates.no-slow.change-slot", false) && mitigateNoSlow;
        mitigateInvalidBreak = config.getBooleanElse("mitigates.invalid-break.enable", false);
        mitigatePos = config.getBooleanElse("mitigates.invalid-pos.enable", false);
        mitigatePosSpoof = config.getBooleanElse("mitigates.invalid-pos.spoof", false) && mitigatePos;
        mitigatePosTeleport = config.getBooleanElse("mitigates.invalid-pos.teleport.enable", false) && mitigatePos;
        mitigatePosInvalid = config.getBooleanElse("mitigates.invalid-pos.invalid.enable", false) && mitigatePos;
        mitigateElytra = config.getBooleanElse("mitigates.elyra.enable", false);
        mitigateGhostElytra = config.getBooleanElse("mitigates.elytra.ghost", false) && mitigateElytra;
        mitigateElytraInvalidAction = config.getBooleanElse("mitigates.elyra.action", false) && mitigateElytra;
        mitigateElytraOnGround = config.getBooleanElse("mitigates.elyra.ground", false) && mitigateElytra;
        mitigateElytraSprint = config.getBooleanElse("mitigates.elyra.sprint", false);
        mitigateTrident = config.getBooleanElse("mitigates.trident.enable", false);
        mitigateTridentRiptiding = config.getBooleanElse("mitigates.trident.riptiding", false) && mitigateTrident;
        mitigateTridentUse = config.getBooleanElse("mitigates.trident.limit-use.enable", false) && mitigateTrident;
        mitigateVelocity = config.getBooleanElse("mitigates.velocity.enable", false);
        mitigateVelocityInvalid = config.getBooleanElse("mitigates.velocity.invalid", false) && mitigateVelocity;
        mitigateVelocityJump = config.getBooleanElse("mitigates.velocity.jump", false) && mitigateVelocity;
        mitigateVisual = config.getBooleanElse("mitigates.visual.enable", false);
        mitigateVisualEquipment = config.getBooleanElse("mitigates.visual.equipment.enable", false) && mitigateVisual;
        mitigateVisualMetaData = config.getBooleanElse("mitigates.visual.meta.enable", false) && mitigateVisual;
        mitigateAutoTotem = config.getBooleanElse("mitigates.auto-totem", false);
        mitigateInventory = config.getBooleanElse("mitigates.inventory", false);
    }

    public boolean isIgnoredClient(String brand) {
        for (Pattern pattern : ignoredClientPatterns) {
            if (pattern.matcher(brand).find()) return true;
        }
        return false;
    }
}
