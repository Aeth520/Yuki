package cn.aetheris.yuki.check;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.api.enums.MitigationStrategy;
import cn.aetheris.yuki.api.events.FlagEvent;
import cn.aetheris.yuki.api.events.MitigateEvent;
import cn.aetheris.yuki.api.events.Reaction;
import cn.aetheris.yuki.api.events.SetbackEvent;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.functionality.ConfigManager;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.gc.GCUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.protocol.nms.NMSUtils;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import github.scarsz.configuralize.DynamicConfig;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Check implements AbstractCheck {
    @Getter
    @NotNull
    protected final PlayerData player;
    @Getter
    public double lastViolations;
    public double violations;
    private int maxVL;

    private static final Map<String, Integer> maxVLCache = new ConcurrentHashMap<>();

    public static void clearMaxVLCache() {
        maxVLCache.clear();
    }

    @Getter
    @Setter
    public double buffer;
    @Getter
    private CheckType checkType;
    private double decay;
    private double setbackVL;

    private String checkName;
    private String configName;
    private String description;
    private boolean experimental;
    private @Setter boolean isEnabled;
    private boolean utilityClass;
    private double maxTps;
    private double maxMspt;
    private MitigationStrategy mitigationStrategy = MitigationStrategy.CAREFUL;
    private Reaction lastFlagReaction = Reaction.INTERRUPT_AND_REPORT;

    @Getter
    private boolean exempted;

    public Check(final @NotNull PlayerData player) {
        this.player = Objects.requireNonNull(player);

        final CheckData checkData = this.getClass().getAnnotation(CheckData.class);
        if (checkData != null) {
            this.checkName = checkData.name();
            this.configName = checkData.configName();
            
            if (this.configName.equals("DEFAULT")) this.configName = this.checkName;
            this.decay = checkData.decay();
            this.setbackVL = checkData.setback();
            this.maxVL = getMaxVLFromConfig(this.configName);
            this.experimental = checkData.experimental();
            this.description = checkData.description();
            this.checkType = checkData.type();
            this.utilityClass = checkData.utilityClass();
            this.mitigationStrategy = checkData.mitigation();
        }

        this.maxTps = getConfig().getDouble("function.limit.max-tps");

        reload();
    }

    private int getMaxVLFromConfig(String name) {
        if (name == null) return 0;
        if (maxVLCache.containsKey(name)) {
            return maxVLCache.get(name);
        }

        ConfigManager configManager = PluginLoader.INSTANCE.getConfigManager();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configManager.getPunishFile());

        for (String s : Objects.requireNonNull(config.getConfigurationSection("Punishments")).getKeys(false)) {
            List<String> checks = config.getStringList("Punishments." + s + ".checks");
            if (!checks.contains(name) && !checks.contains("!" + name)) continue;

            List<String> cmds = config.getStringList("Punishments." + s + ".commands");
            final int[] maxVL = {0};
            cmds.stream().map(cmd -> cmd.split(" ")[0]).map(cmd -> cmd.split(":")[0]).map(Integer::parseInt).max(Comparator.naturalOrder()).ifPresent(vl -> maxVL[0] = vl);

            maxVLCache.put(name, maxVL[0]);

            return maxVL[0];
        }
        return 0;
    }

    public static boolean isTransaction(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.PONG ||
                packetType == PacketType.Play.Client.WINDOW_CONFIRMATION;
    }

    public boolean shouldModifyPackets() {
        return player.exemptProcessor.canProcess(this) && !exempted;
    }

    public void updateExempted() {
        if (player.bukkitPlayer == null || configName == null) {
            return;
        }

        Bukkit.getScheduler().runTask(Yuki.getInstance(),
                () -> {
                    boolean hasPermission = player.bukkitPlayer.hasPermission("yuki.exempt." + configName.toLowerCase());
                    if (hasPermission != exempted) {
                        exempted = hasPermission;
                    }
                });
    }

    public final boolean flagAndAlert(String verbose) {
        if (flag()) {
            alert(verbose);
            return true;
        }
        return false;
    }

    public final boolean flagAndAlert() {
        return flagAndAlert("");
    }

    public final boolean flag() {
        if (!canFlagOrAlert()) {
            return false;
        }

        FlagEvent event = new FlagEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        lastFlagReaction = event.getReaction();
        if (lastFlagReaction.shouldSkipVL()) {
            return false;
        }

        lastViolations = violations;
        violations++;

        if (!lastFlagReaction.shouldSkipPunish()) {
            player.punishmentManager.handleViolation(this);
        }
        return true;
    }

    public final boolean flagWithSetback() {
        if (flag()) {
            setbackIfAboveSetbackVL();
            return true;
        }
        return false;
    }

    public final boolean flagAndAlertWithSetback() {
        return flagAndAlertWithSetback("");
    }

    public final boolean flagAndAlertWithSetback(String verbose) {
        if (flagAndAlert(verbose)) {
            setbackIfAboveSetbackVL();
            return true;
        }
        return false;
    }

    public final void rewardVL() {
        violations = Math.max(0, violations - decay);
    }

    public void rewardBufferAndVL() {
        if (buffer == 0.0) {
            rewardVL();
        } else {
            buffer = Math.max(0, buffer - decay);
        }
    }

    public void reload() {
        updateExempted();
        Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
            if (configName == null) return;
            decay = getConfig().getDoubleElse(configName + ".decay", decay);
            setbackVL = getConfig().getDoubleElse(configName + ".setback-vl", setbackVL);
            description = getConfig().getStringElse(configName + ".description", description);
            if (setbackVL == -1) setbackVL = Double.MAX_VALUE;
            maxVL = getMaxVLFromConfig(configName);
            maxTps = getConfig().getDoubleElse(configName + ".max-tps",
                    getConfig().getDouble("function.limit.max-tps"));
            maxMspt = getConfig().getDoubleElse(configName + ".max-mspt", 0);
            String mitigationStr = getConfig().getStringElse(configName + ".mitigation", null);
            if (mitigationStr != null) {
                try {
                    mitigationStrategy = MitigationStrategy.valueOf(mitigationStr.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }
        });
    }

    public boolean alert(String verbose) {
        if (!canFlagOrAlert()) return false;

        if (lastFlagReaction.shouldSkipAlert()) return false;

        return player.punishmentManager.handleAlert(player, verbose, this);
    }

    private boolean canFlagOrAlert() {
        if (!player.exemptProcessor.canFlag(this)) return false;

        if (exempted) return false;

        if (experimental && !PluginLoader.INSTANCE.getConfigManager().isExperimentalChecks()) return false;

        if (PluginLoader.INSTANCE.getLagManager().isLagging()) {
            String message = PluginLoader.INSTANCE.getLangManager().i18n("function.lag-track.message")
                    .replace("%player%", player.getName())
                    .replace("%time%", PluginLoader.INSTANCE.getLagManager().getLaggingTime(time()) + "")
                    .replace("%time2%", PluginLoader.INSTANCE.getLagManager().getLaggingTime2() + "")
                    .replace("%time3%", PluginLoader.INSTANCE.getLagManager().getGCPauseTime() + "")
                    .replace("%sum%", GCUtil.getTotalGcPauseMillis() + "")
                    .replace("%check_name%", getCheckName());
            LogUtils.console(message);
            if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.lag-track.print-stacktrace", false)) {
                StackTraceElement[] stackTrace = Yuki.getInstance().getMainThread().getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    LogUtils.console(element + "");
                }
            }
            return false;
        }

        return true;
    }

    public DynamicConfig getConfig() {
        return PluginLoader.INSTANCE.getConfigManager().getConfig();
    }

    public void resetPlayerUseItem(Player bukkitPlayer) {
        if (!PluginLoader.INSTANCE.getConfigManager().isMitigateUseItem()) {
            return;
        }
        if (!mitigationStrategy.allowsResetUseItem()) return;

        MitigateEvent mitigateEvent = new MitigateEvent(player, this);
        Bukkit.getPluginManager().callEvent(mitigateEvent);
        if (mitigateEvent.isCancelled()) {
            return;
        }
        NMSUtils.resetItemUsage(bukkitPlayer);
        LogUtils.mitigate("&b" + player.getName() + "&7 has been reset useitem &7(&b" + (getCheckName() != null ? getCheckName() : "Nulled") + "&7)");
    }

    public void shuffleHotbar() {
        if (!mitigationStrategy.allowsShuffle()) return;

        MitigateEvent mitigateEvent = new MitigateEvent(player, this);
        Bukkit.getPluginManager().callEvent(mitigateEvent);
        if (mitigateEvent.isCancelled()) {
            return;
        }

        int randomSlot = ThreadLocalRandom.current().nextInt(9);

        WrapperPlayServerHeldItemChange slotPacket = new WrapperPlayServerHeldItemChange(randomSlot);
        player.user.writePacket(slotPacket);

        player.packetStateData.lastSlotSelected = randomSlot;
        player.getInventory().inventory.selected = randomSlot;
        player.packetStateData.setSlowedByUsingItem(false);

        if (player.getBukkitPlayer() != null) {
            player.getInventory().inventory.setHeldItem(SpigotConversionUtil.fromBukkitItemStack(player.getBukkitPlayer().getInventory().getItem(randomSlot)));
            player.getBukkitPlayer().getInventory().setHeldItemSlot(randomSlot); 
            player.getBukkitPlayer().updateInventory();
        }

        player.getInventory().requiresRefresh = true;
        resetPlayerUseItem(player.getBukkitPlayer());
        player.user.flushPackets();

        LogUtils.debug("&b" + player.getName() + "&7 shuffled slot to " + randomSlot);
    }

    public boolean setbackIfAboveSetbackVL() {
        if (!mitigationStrategy.allowsSetback()) return false;
        if (isAboveSetbackVl()) {
            SetbackEvent setbackEvent = new SetbackEvent(player, this);
            Bukkit.getPluginManager().callEvent(setbackEvent);
            if (setbackEvent.isCancelled()) {
                return false;
            }
            return player.getSetbackTeleportUtil().executeViolationSetback();
        }
        return false;
    }

    public void shuffleAboveSetbackVL() {
        if (isAboveSetbackVl()) {
            shuffleHotbar();
        }
    }

    public boolean isAboveSetbackVl() {
        return violations > setbackVL;
    }

    public void kickPlayer() {
        LogUtils.consolePrefixed("&cKick by &b" + getConfigName() + " p= " + player.getName());
        player.disconnect(Component.translatable(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("kick.default")));
    }

    public String formatOffset(double offset) {
        return offset > 0.001 ? String.format("%.5f", offset) : String.format("%.2E", offset);
    }

    public boolean isFlying(PacketTypeCommon packetType) {
        return WrapperPlayClientPlayerFlying.isFlying(packetType);
    }

    public boolean isUpdate(PacketTypeCommon packetType) {
        return isFlying(packetType)
                || packetType == PacketType.Play.Client.CLIENT_TICK_END
                || isTransaction(packetType);
    }

    public boolean isTickPacket(PacketTypeCommon packetType) {
        if (isTickPacketIncludingNonMovement(packetType)) {
            if (isFlying(packetType)) {
                return !player.packetStateData.lastPacketWasTeleport && !player.packetStateData.lastPacketWasOnePointSeventeenDuplicate;
            }
            return true;
        }
        return false;
    }

    public boolean getExperimental() {
        return this.experimental;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }


    public boolean isTickPacketIncludingNonMovement(PacketTypeCommon packetType) {
        
        
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)
                && !player.packetStateData.didSendMovementBeforeTickEnd) {
            if (packetType == PacketType.Play.Client.CLIENT_TICK_END) {
                return true;
            }
        }

        return isFlying(packetType);
    }


    @Override
    public void setViolations(double violations) {
        this.violations = violations;
    }

    @Override
    public int getMaxVL() {
        return maxVL;
    }

    public long time() {
        return System.currentTimeMillis();
    }

    protected boolean isExempt(final ExemptType exemptType) {
        return player.exemptProcessor.isExempt(exemptType);
    }

    protected boolean isExempt(final ExemptType... exemptTypes) {
        return player.exemptProcessor.isExempt(exemptTypes);
    }

    @Override
    public double getMaxTps() {
        return maxTps;
    }

    @Override
    public void setMaxTps(double maxTps) {
        this.maxTps = maxTps;
    }

    @Override
    public double getMaxMspt() {
        return maxMspt;
    }

    @Override
    public void setMaxMspt(double maxMspt) {
        this.maxMspt = maxMspt;
    }

    @Override
    public boolean isUtilityClass() {
        return utilityClass;
    }

    @Override
    public MitigationStrategy getMitigationStrategy() {
        return mitigationStrategy;
    }

    @Override
    public void setMitigationStrategy(MitigationStrategy strategy) {
        this.mitigationStrategy = strategy;
    }


}