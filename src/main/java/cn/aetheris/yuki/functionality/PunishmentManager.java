package cn.aetheris.yuki.functionality;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
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
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PunishmentManager {

    private final ConcurrentMap<String, Integer> maxVLCache = new ConcurrentHashMap<>();
    List<PunishGroup> groups = new ArrayList<>();
    PlayerData player;
    String experimentalSymbol = "*";
    String hoverAction;
    String hoverMessage;

    public PunishmentManager(PlayerData player) {
        this.player = player;
        reload();
    }

    
    public void reload() {
        maxVLCache.clear();
        DynamicConfig config = PluginLoader.INSTANCE.getConfigManager().getConfig();
        List<String> punish = config.getStringListElse("Punishments", new ArrayList<>());
        experimentalSymbol = config.getStringElse("output.alerts.expsymbol", "*");
        hoverAction = config.getString("commands.action");
        hoverMessage = config.getString("output.hover");

        try {
            groups.clear();

            disableAllChecks();

            for (Object s : punish) {
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) s;

                List<String> checks = (List<String>) map.getOrDefault("checks", new ArrayList<>());
                List<String> commands = (List<String>) map.getOrDefault("commands", new ArrayList<>());
                int removeViolationsAfter = (int) map.getOrDefault("remove-violations-after", 300);

                List<ParsedCommand> parsed = new ArrayList<>();
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

                    parsed.add(new ParsedCommand(threshold, interval, commandString));
                }

                groups.add(new PunishGroup(checksList, parsed, removeViolationsAfter));
            }
        } catch (Exception e) {
            LogUtils.console("&3Yuki &8» &cPunishment.yml Error" + "\n&c" + e.getMessage());
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

    
    public String replaceAlertPlaceholders(String original, int vl, Check check, String alertString, String verbose) {
        original = PluginLoader.INSTANCE.getLangManager().format(original
                .replace("[alert]", alertString)
                .replace("[proxy]", alertString)
                .replace("%check_name%", check.getCheckName())
                .replace("%max_vl%", check.getMaxVL() + "")
                .replace("%vl%", Integer.toString(vl))
                .replace("%add%", String.format("%.1f", Math.abs(check.lastViolations - check.violations)))
                .replace("%verbose%", verbose)
                .replace("%description%", check.getDescription())
                .replace("%exp%", check.getExperimental() ? experimentalSymbol : ""));

        return PluginLoader.INSTANCE.getExternalAPI().replaceVariables(player, original, true);
    }

    
    public String replaceHoverPlaceholders(Check check, String hoverString, String verbose, String... info) {
        String line = String.join("\n", hoverString);
        String details = String.join("\n", info);
        line = line.replace("%description%", check.getDescription())
                .replace("%check_name%", check.getCheckName())
                .replace("%info%", details.isEmpty() ? "None provided" : details)
                .replace("%verbose%", verbose);

        return PluginLoader.INSTANCE.getExternalAPI().replaceVariables(player, line, true);
    }


    
    public boolean handleAlert(PlayerData player, String verbose, Check check, String... info) {
        String alertString = PluginLoader.INSTANCE.getLangManager().i18n("output.alerts.format.normal");
        String hoverCMD = hoverAction.replace("%player%", player.getName());
        String hoverString = hoverMessage;


        boolean testMode = PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.testmode", false);
        boolean sentDebug = false;

        if (!hoverCMD.startsWith("/")) {
            hoverCMD = "/" + hoverCMD;
        }

        for (PunishGroup group : groups) {
            if (group.getChecks().contains(check)) {
                
                final int vl = getViolations(group, check);
                
                for (ParsedCommand command : group.getCommands()) {
                    String cmd = replaceAlertPlaceholders(command.getCommand(), vl, check, alertString, verbose);
                    String hover = replaceHoverPlaceholders(check, hoverString, verbose, info);

                    TextComponent message = new TextComponent(TextComponent.fromLegacyText(cmd));
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, hoverCMD));

                    if (!PluginLoader.INSTANCE.getAlertManager().getEnabledVerbose().isEmpty() && command.command.equals("[alert]")) {
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
                        boolean inInterval = command.getInterval() == 0 ? (command.executeCount == 0) : (vl % command.getInterval() == 0);
                        if (inInterval) {
                            CommandExecuteEvent executeEvent = new CommandExecuteEvent(player, check, cmd);
                            Bukkit.getPluginManager().callEvent(executeEvent);
                            if (executeEvent.isCancelled()) {
                                continue;
                            }
                            
                            if (command.command.startsWith("[code] ")) {
                                String code = command.command.substring("[code] ".length());
                                if (!code.isEmpty()) {
                                    ExpressCommandInstance commandInstance = new ExpressCommandInstance();
                                    try {
                                        commandInstance.initContext(player, command.getCommand(), vl, check, alertString, verbose);
                                        if (commandInstance.initScript(code)) {
                                            commandInstance.run();
                                        } else {
                                            LogUtils.console("你的punishment.yml存在不合理的[code]! 内容: " + command.command);
                                        }
                                    } catch (Exception e) {
                                        LogUtils.console("[code]运行错误：" + command.command);
                                        Yuki.getInstance().getLogger().log(Level.SEVERE, "[code] script error", e);
                                    }
                                }
                            } else switch (command.command) {
                                case "[alert]" -> {
                                    sentDebug = true;
                                    if (testMode) {
                                        player.user.sendMessage(cmd);
                                        continue;
                                    }
                                    MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
                                        Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> DevelopUtils.isDeveloper(onlinePlayer) || PluginLoader.INSTANCE.getAlertManager().getEnabledAlerts().contains(onlinePlayer.getUniqueId())).forEach(onlinePlayer -> onlinePlayer.spigot().sendMessage(message));
                                        if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("output.alerts.console", true)) {
                                            LogUtils.console(cmd);
                                        }
                                    });
                                }
                                case "[save]" -> {
                                    OfflinePlayer offlinePlayer = Yuki.getInstance().getServer().getOfflinePlayer(player.getUniqueId());
                                    PluginLoader.INSTANCE.getDatabaseManager().getCheckInfoManager().logAlertSync(
                                            offlinePlayer,
                                            player,
                                            check.getExperimental(),
                                            verbose,
                                            check.getCheckName(),
                                            vl,
                                            check.getDescription(),
                                            player.getTransactionPing() + "/" + player.getKeepAlivePing(),
                                            player.isLagging(),
                                            player.isMoveLagging(),
                                            String.format("%.2f", player.getTPS()),
                                            player.getBrand(),
                                            player.getVersionName()
                                    );
                                }

                                case "[setback]" -> {
                                    if (!player.isNoSetbackPermission()) {
                                        player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                                        LogUtils.setback("&b " + player.getName() + "&7 has been setback for [setback] (&bPunishment&7)");
                                    }
                                }
                                default -> {
                                    String rawCmd = command.command;
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
                                        String ip = player.getBukkitPlayer() != null && player.getBukkitPlayer().getAddress() != null
                                                ? player.getBukkitPlayer().getAddress().getAddress().getHostAddress() : null;
                                        if (ip != null) {
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
                                        return false;
                                    }
                                    String randomKey = FakeAntiCheatUtils.getRandomName();
                                    String colorCode = FakeAntiCheatUtils.getColorCode(randomKey);
                                    String antiCheatName = FakeAntiCheatUtils.getName(randomKey);
                                    MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd
                                            .replace("%anticheat_color%", colorCode)
                                            .replace("%anticheat%", antiCheatName)
                                            .replace("%special_random_string%", AESUtil.encrypt(FakeAntiCheatUtils.generateRandomString() + "_" + check.getConfigName())))
                                    );
                                    String broadcast = PluginLoader.INSTANCE.getLangManager()
                                            .i18nWithoutPrefix(PluginLoader.INSTANCE.getLangManager().format("broadcast")
                                            ).replace("%player%", player.getName());
                                    String broadcastCmd = cmd;
                                    if (broadcastCmd.contains(player.getName())) {
                                        broadcastCmd = broadcastCmd
                                                .replace(player.getName(), "");
                                    }
                                    if (broadcastCmd.contains("ban") || broadcastCmd.contains("banip") || broadcastCmd.contains("kick")) {
                                        player.setPunish(fixDuplicateCommand);
                                        if (player.getBukkitPlayer() == null) return false;
                                        PunishEvent event = new PunishEvent(player, check);
                                        Bukkit.getPluginManager().callEvent(event);
                                        if (event.isCancelled()) {
                                            player.setCancelCommand(true);
                                            return false;
                                        }
                                        if (PluginLoader.INSTANCE.getConfigManager().getConfig()
                                                .getBooleanElse("function.kick-broadcast", false)) {
                                            MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () -> {
                                                if (Yuki.getInstance().getPacketEventsManager()
                                                        .getServerVersion().isNewerThan(ServerVersion.V_1_8_8)) {
                                                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(broadcast));
                                                } else {
                                                    Bukkit.broadcastMessage(broadcast);
                                                }
                                                if (PluginLoader.INSTANCE.getConfigManager().getConfig()
                                                        .getBooleanElse("function.kick-strike-lightning-effect", false)) {
                                                    player.getBukkitPlayer().getWorld().strikeLightningEffect(player.getBukkitPlayer().getLocation().clone());
                                                }
                                                LogUtils.console(broadcast);
                                            });
                                        }
                                    }
                                    }
                                }
                            }
                            if (!player.isPunish()) {
                                if (player.isCancelCommand()) {
                                    player.setCancelCommand(false);
                                    return false;
                                }
                                command.setExecuteCount(command.getExecuteCount() + 1);
                            }
                        }
                    }
                }
            }
        }
        return sentDebug;
    }

    
    public void handleViolation(Check check) {
        for (PunishGroup group : groups) {
            if (group.getChecks().contains(check)) {
                long currentTime = System.currentTimeMillis();
                group.violations.put(currentTime, check);

                List<Long> keysToRemove = new ArrayList<>();
                for (Map.Entry<Long, Check> entry : group.violations.entrySet()) {
                    if (currentTime - entry.getKey() > group.removeViolationsAfter) {
                        keysToRemove.add(entry.getKey());
                    }
                }

                for (Long key : keysToRemove) {
                    group.violations.remove(key);
                }
            }
        }
    }


    
    private int getViolations(PunishGroup group, Check check) {
        int vl = 0;
        for (Check value : group.violations.values()) {
            if (value == check) {
                return (int) value.getViolations();
            }
        }
        return vl;
    }































































    @Getter
    public static class PunishGroup {
        public Map<Long, Check> violations = new ConcurrentHashMap<>();
        List<AbstractCheck> checks;
        List<ParsedCommand> commands;
        int removeViolationsAfter;

        public PunishGroup(List<AbstractCheck> checks, List<ParsedCommand> commands, int removeViolationsAfter) {
            this.checks = checks;
            this.commands = commands;
            this.removeViolationsAfter = removeViolationsAfter * 1000;
        }
    }

    @Getter
    static class ParsedCommand {
        int threshold;
        int interval;
        @Setter
        int executeCount;
        String command;

        public ParsedCommand(int threshold, int interval, String command) {
            this.threshold = threshold;
            this.interval = interval;
            this.command = command;
        }
    }
}
