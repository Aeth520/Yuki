package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractCheck;
import cn.aetheris.yuki.api.events.CheckToggleEvent;
import cn.aetheris.yuki.api.events.CommandExecuteEvent;
import cn.aetheris.yuki.api.events.PunishEvent;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.functionality.code.ExpressCommandInstance;
import cn.aetheris.yuki.functionality.moderation.ModerationManager;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.develop.DevelopUtils;
import cn.aetheris.yuki.util.encrypt.AESUtil;
import cn.aetheris.yuki.util.fake.FakeAntiCheatUtils;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import github.scarsz.configuralize.DynamicConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

/**
 * Coordinates punishment config loading, violation tracking, and alert command execution.
 */
public final class PunishmentManager {

    private final PlayerData player;
    private final ViolationTracker violationTracker;
    private final AlertCommandProcessor alertProcessor;

    public PunishmentManager(PlayerData player) {
        this.player = player;
        this.violationTracker = new ViolationTracker();
        this.alertProcessor = new AlertCommandProcessor(player, violationTracker);
        reload();
    }

    // --- Reload ---

    public void reload() {
        DynamicConfig config = PluginLoader.INSTANCE.getConfigManager().getConfig();
        List<String> punish = config.getStringListElse("Punishments", new ArrayList<>());
        alertProcessor.reload();

        try {
            disableAllChecks();
            List<ViolationTracker.PunishGroup> newGroups = new ArrayList<>();

            for (Object s : punish) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) s;

                @SuppressWarnings("unchecked")
                List<String> checks = (List<String>) map.getOrDefault("checks", new ArrayList<>());
                @SuppressWarnings("unchecked")
                List<String> commands = (List<String>) map.getOrDefault("commands", new ArrayList<>());
                int removeViolationsAfter = (int) map.getOrDefault("remove-violations-after", 300);

                List<ViolationTracker.ParsedCommand> parsed = new ArrayList<>();
                List<AbstractCheck> checksList = new ArrayList<>();
                List<AbstractCheck> excluded = new ArrayList<>();

                for (String command : checks) {
                    command = command.toLowerCase(Locale.ROOT);
                    boolean exclude = false;
                    if (command.startsWith("!")) {
                        exclude = true;
                        command = command.substring(1);
                    }
                    for (AbstractCheck check : player.checkManager.allChecks.values()) {
                        if (check.getCheckName() != null &&
                                (check.getCheckName().toLowerCase(Locale.ROOT).contains(command))) {
                            if (exclude) {
                                excluded.add(check);
                            } else {
                                checksList.add(check);
                                if (!check.isEnabled()) {
                                    check.setEnabled(true);
                                    Bukkit.getPluginManager().callEvent(new CheckToggleEvent(player, check, true));
                                }
                            }
                        }
                    }
                    for (AbstractCheck check : excluded) checksList.remove(check);
                }

                for (String command : commands) {
                    String firstNum = command.substring(0, command.indexOf(":"));
                    String secondNum = command.substring(command.indexOf(":"), command.indexOf(" "));
                    int threshold = Integer.parseInt(firstNum);
                    int interval = Integer.parseInt(secondNum.substring(1));
                    String commandString = command.substring(command.indexOf(" ") + 1);
                    parsed.add(new ViolationTracker.ParsedCommand(threshold, interval, commandString));
                }

                newGroups.add(new ViolationTracker.PunishGroup(checksList, parsed, removeViolationsAfter));
            }
            violationTracker.setGroups(newGroups);
        } catch (Exception e) {
            LogUtils.consolePrefixed("&cPunishment.yml Error" + "\n&c" + e.getMessage());
        }
    }

    private void disableAllChecks() {
        for (AbstractCheck check : player.checkManager.allChecks.values()) {
            if (check.isEnabled()) {
                check.setEnabled(false);
                Bukkit.getPluginManager().callEvent(new CheckToggleEvent(player, check, false));
            }
        }
    }

    // --- Delegation ---

    public void handleViolation(Check check) {
        violationTracker.handleViolation(check);
    }

    public int getViolations(ViolationTracker.PunishGroup group, Check check) {
        return violationTracker.getViolations(group, check);
    }

    public String replaceAlertPlaceholders(String original, int vl, Check check, String alertString, String verbose) {
        return alertProcessor.replaceAlertPlaceholders(original, vl, check, alertString, verbose);
    }

    public String replaceHoverPlaceholders(Check check, String hoverString, String verbose, String... info) {
        return alertProcessor.replaceHoverPlaceholders(check, hoverString, verbose, info);
    }

    // --- Alert & Command Execution ---

    public boolean handleAlert(PlayerData player, String verbose, Check check, String... info) {
        String alertString = PluginLoader.INSTANCE.getLangManager().i18n("output.alerts.format.normal");
        String hoverCMD = alertProcessor.getHoverAction().replace("%player%", player.getName());
        String hoverString = alertProcessor.getHoverMessage();

        boolean testMode = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.testmode", false);
        boolean sentDebug = false;

        if (!hoverCMD.startsWith("/")) {
            hoverCMD = "/" + hoverCMD;
        }

        for (ViolationTracker.PunishGroup group : violationTracker.getGroups()) {
            if (!group.getChecks().contains(check)) continue;

            final int vl = violationTracker.getViolations(group, check);

            for (ViolationTracker.ParsedCommand command : group.getCommands()) {
                String cmd = alertProcessor.replaceAlertPlaceholders(command.getCommand(), vl, check, alertString, verbose);
                String hover = alertProcessor.replaceHoverPlaceholders(check, hoverString, verbose, info);

                TextComponent message = new TextComponent(TextComponent.fromLegacyText(cmd));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, hoverCMD));

                if (!PluginLoader.INSTANCE.getAlertManager().getEnabledVerbose().isEmpty() && command.getCommand().equals("[alert]")) {
                    sentDebug = true;
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (DevelopUtils.isDeveloper(onlinePlayer) || PluginLoader.INSTANCE.getAlertManager().getEnabledVerbose().contains(onlinePlayer.getUniqueId())) {
                            onlinePlayer.spigot().sendMessage(message);
                        }
                    }
                    if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("output.verbose.console", false)) {
                        LogUtils.console(cmd);
                    }
                }

                if (vl >= command.getThreshold()) {
                    boolean inInterval = command.getInterval() == 0 ? (command.getExecuteCount() == 0) : (vl % command.getInterval() == 0);
                    if (!inInterval) continue;

                    CommandExecuteEvent executeEvent = new CommandExecuteEvent(player, check, cmd);
                    Bukkit.getPluginManager().callEvent(executeEvent);
                    if (executeEvent.isCancelled()) continue;

                    if (command.getCommand().startsWith("[code] ")) {
                        executeCodeCommand(command, cmd, vl, check, alertString, verbose);
                    } else {
                        sentDebug |= executeStandardCommand(command, check, cmd, vl, verbose, message, testMode, sentDebug);
                    }
                }
            }
        }
        return sentDebug;
    }

    private void executeCodeCommand(ViolationTracker.ParsedCommand command, String cmd, int vl, Check check, String alertString, String verbose) {
        String code = command.getCommand().substring("[code] ".length());
        if (code.isEmpty()) return;
        ExpressCommandInstance commandInstance = new ExpressCommandInstance();
        try {
            commandInstance.initContext(player, command.getCommand(), vl, check, alertString, verbose);
            if (commandInstance.initScript(code)) {
                commandInstance.run();
            } else {
                LogUtils.console("你的punishment.yml存在不合理的[code]! 内容: " + command.getCommand());
            }
        } catch (Exception e) {
            LogUtils.console("[code]运行错误：" + command.getCommand());
            Yuki.getInstance().getLogger().log(Level.SEVERE, "[code] script error", e);
        }
    }

    private boolean executeStandardCommand(ViolationTracker.ParsedCommand command, Check check, String cmd, int vl, String verbose, TextComponent message, boolean testMode, boolean sentDebug) {
        String rawCmd = command.getCommand();
        switch (rawCmd) {
            case "[alert]" -> {
                if (testMode) {
                    player.user.sendMessage(cmd);
                    return sentDebug;
                }
                Bukkit.getScheduler().runTaskAsynchronously(Yuki.getInstance(), () -> {
                    Bukkit.getOnlinePlayers().stream().filter(onlinePlayer ->
                            DevelopUtils.isDeveloper(onlinePlayer) || PluginLoader.INSTANCE.getAlertManager().getEnabledAlerts().contains(onlinePlayer.getUniqueId())
                    ).forEach(onlinePlayer -> onlinePlayer.spigot().sendMessage(message));
                    if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("output.alerts.console", true)) {
                        LogUtils.console(cmd);
                    }
                });
                return true;
            }
            case "[save]" -> {
                OfflinePlayer offlinePlayer = Yuki.getInstance().getServer().getOfflinePlayer(player.getUniqueId());
                PluginLoader.INSTANCE.getDatabaseManager().getCheckInfoManager().logAlertSync(
                        offlinePlayer, check.getExperimental(), verbose, check.getCheckName(), vl,
                        check.getDescription(), player.getTransactionPing() + "/" + player.getKeepAlivePing(),
                        player.isLagging(), player.isMoveLagging(),
                        String.format("%.2f", player.getTPS()), player.getBrand(), player.getVersionName());
            }
            case "[setback]" -> {
                if (!player.isNoSetbackPermission()) {
                    player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                    LogUtils.setback("&b " + player.getName() + "&7 has been setback for [setback] (&bPunishment&7)");
                }
            }
            default -> {
                sentDebug = executeExtendedCommand(command, rawCmd, cmd, check, testMode, sentDebug);
            }
        }
        return sentDebug;
    }

    private boolean executeExtendedCommand(ViolationTracker.ParsedCommand command, String rawCmd, String cmd, Check check, boolean testMode, boolean sentDebug) {
        if (rawCmd.startsWith("[kick]")) {
            ModerationManager mod = PluginLoader.INSTANCE.getModerationManager();
            String reason = cmd.replace("[alert]", "").replace("[proxy]", "").trim();
            if (player.getBukkitPlayer() != null) {
                mod.kick(player.getBukkitPlayer(), reason);
            }
        } else if (rawCmd.startsWith("[ban]") && !rawCmd.startsWith("[banip]")) {
            ModerationManager mod = PluginLoader.INSTANCE.getModerationManager();
            String[] parts = cmd.replace("[alert]", "").replace("[proxy]", "").trim().split("\\s+", 2);
            String duration = parts.length > 0 ? parts[0] : "permanent";
            String reason = parts.length > 1 ? parts[1] : "Banned by Yuki";
            mod.ban(player.getName(), reason, duration);
        } else if (rawCmd.startsWith("[banip]")) {
            ModerationManager mod = PluginLoader.INSTANCE.getModerationManager();
            if (player.getBukkitPlayer() != null && player.getBukkitPlayer().getAddress() != null) {
                String ip = player.getBukkitPlayer().getAddress().getAddress().getHostAddress();
                String[] parts = cmd.replace("[alert]", "").replace("[proxy]", "").trim().split("\\s+", 2);
                String duration = parts.length > 0 ? parts[0] : "permanent";
                String reason = parts.length > 1 ? parts[1] : "Banned by Yuki";
                mod.banIp(ip, reason, duration);
            }
        } else if (rawCmd.startsWith("[warn]")) {
            ModerationManager mod = PluginLoader.INSTANCE.getModerationManager();
            String warnMsg = cmd.replace("[alert]", "").replace("[proxy]", "").trim();
            if (player.getBukkitPlayer() != null) {
                mod.warn(player.getBukkitPlayer(), warnMsg);
            }
        } else {
            final boolean fixDuplicateCommand = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.fix-duplicate-command-punish", false);
            if (player.isPunish() && fixDuplicateCommand) {
                return sentDebug;
            }
            String randomKey = FakeAntiCheatUtils.getRandomName();
            String colorCode = FakeAntiCheatUtils.getColorCode(randomKey);
            String antiCheatName = FakeAntiCheatUtils.getName(randomKey);
            Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd
                    .replace("%anticheat_color%", colorCode)
                    .replace("%anticheat%", antiCheatName)
                    .replace("%special_random_string%", AESUtil.encrypt(FakeAntiCheatUtils.generateRandomString() + "_" + check.getConfigName()))));

            String broadcast = PluginLoader.INSTANCE.getLangManager()
                    .i18nWithoutPrefix(PluginLoader.INSTANCE.getLangManager().format("broadcast"))
                    .replace("%player%", player.getName());
            String broadcastCmd = cmd;
            if (broadcastCmd.contains(player.getName())) {
                broadcastCmd = broadcastCmd.replace(player.getName(), "");
            }
            if (broadcastCmd.contains("ban") || broadcastCmd.contains("banip") || broadcastCmd.contains("kick")) {
                player.setPunish(fixDuplicateCommand);
                if (player.getBukkitPlayer() == null) return sentDebug;
                PunishEvent event = new PunishEvent(player, check);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    player.setCancelCommand(true);
                    return sentDebug;
                }
                if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.kick-broadcast", false)) {
                    Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
                        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_8_8)) {
                            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(broadcast));
                        } else {
                            Bukkit.broadcastMessage(broadcast);
                        }
                        if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.kick-strike-lightning-effect", false)) {
                            player.getBukkitPlayer().getWorld().strikeLightningEffect(player.getBukkitPlayer().getLocation().clone());
                        }
                        LogUtils.console(broadcast);
                    });
                }
            }
        }
        return sentDebug;
    }
}