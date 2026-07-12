package cn.aetheris.yuki.command.sub;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.core.database.entity.CheckInfo;
import cn.aetheris.yuki.core.database.interfaces.CheckInfoManager;
import cn.aetheris.yuki.util.message.ColorUtils;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.time.TimeUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class History extends AbstractCommand {

    public History() {
        super(
                "Show or clear player's violation history",
                "yuki.commands.history",
                false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.history.usage"));
            return;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            if (args.length == 1) {
                if (!sender.hasPermission("yuki.commands.history.clear")) {
                    sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("no-permission"));
                    return;
                }
                clearAllHistory(sender);
            } else if (args.length == 2) {
                OfflinePlayer target = getOfflinePlayerByName(args[1]);
                if (target == null) {
                    sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.history.playernot-found"));
                    return;
                }
                clearPlayerHistory(sender, target);
            } else {
                sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.history.usage"));
            }
            return;
        }

        OfflinePlayer target = getOfflinePlayerByName(args[0]);
        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("not-found").replace("%player%", args[0]));
            return;
        }

        int page = args.length > 1 ? parsePage(args[1]) : 1;

        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            CheckInfoManager checkInfoManager = PluginLoader.INSTANCE.getDatabaseManager().getCheckInfoManager();
            long logCount = checkInfoManager.getLogCount(target);

            List<CheckInfo> logs = checkInfoManager.getViolations(
                    target, page, PluginLoader.INSTANCE.getConfigManager().getConfig().getIntElse("commands.history.pages", 10)
            );

            long maxPages = (long) Math.ceil((float) logCount / 10);

            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target,
                            "commands.history.header")
                    .replace("%page%", String.valueOf(page))
                    .replace("%maxPages%", String.valueOf(maxPages)));

            for (int i = logs.size() - 1; i >= 0; i--) {
                CheckInfo log = logs.get(i);

                String hoverContent = ColorUtils.color(String.join("\n", PluginLoader.INSTANCE.getConfigManager().getConfig().getStringListElse("output.hover", List.of(
                                "&5延迟: &f%ping% &7(&fLag= &c%lagging% &7| &c%movelagging%&7)&f | &5卡顿程度: &f%tps% | &5玩家版本: &f%brand% %version%", "",
                                "&5检查描述:", "&f%description%", "",
                                "&5详细:", "&f%verbose%", "",
                                "&5点击我击毙机构 &f%player% &c!"))).replace("%description%", log.getDescription())
                        .replace("%mix_ping%", String.valueOf(log.getPing()))
                        .replace("%lagging%", String.valueOf(log.isLagging()))
                        .replace("%move_lagging%", String.valueOf(log.isMoveLagging()))
                        .replace("%tps%", String.valueOf(log.getTps()))
                        .replace("%brand%", String.valueOf(log.getBrand()))
                        .replace("%version%", String.valueOf(log.getVersion()))
                        .replace("%verbose%", log.getVerbose())
                        .replace("%player%", Objects.requireNonNull(target.getName())));

                TextComponent checkNameComponent = new TextComponent(ColorUtils.color(log.getCheckName()));
                checkNameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverContent).create()));
                checkNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("hover-command", "/yuki spectate") + " " + target.getName()));
                String[] splitMessage = PluginLoader.INSTANCE.getLangManager().i18n("commands.history.entry")
                        .replace("%player%", args[0])
                        .replace("%exp%", log.isExp() ? PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("output.alerts.expsymbol") : "")
                        .replace("%vl%", String.valueOf(log.getVl()))
                        .replace("%timeago%", TimeUtils.formatRelativeTime(log.getCreatedAt()))
                        .replace("%server%", ColorUtils.color(log.getServer()))
                        .split("%check_name%", 2);

                TextComponent baseMessage = new TextComponent(splitMessage[0]);
                baseMessage.addExtra(checkNameComponent);

                if (splitMessage.length > 1) {
                    baseMessage.addExtra(new TextComponent(splitMessage[1]));
                }

                if (sender instanceof Player player) {
                    player.spigot().sendMessage(baseMessage);
                } else {
                    LogUtils.console(TextComponent.toLegacyText(baseMessage));
                }
            }
        });
    }

    private OfflinePlayer getOfflinePlayerByName(String playerName) {
        Player onlinePlayer = Bukkit.getPlayerExact(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        return offlinePlayer;
    }

    private void clearAllHistory(@NotNull CommandSender sender) {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            CheckInfoManager checkInfoManager = PluginLoader.INSTANCE.getDatabaseManager().getCheckInfoManager();
            checkInfoManager.clearAllLogs();
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.history.clean-all"));
        });
    }

    private void clearPlayerHistory(@NotNull CommandSender sender, OfflinePlayer target) {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            CheckInfoManager checkInfoManager = PluginLoader.INSTANCE.getDatabaseManager().getCheckInfoManager();
            checkInfoManager.clearLogs(target);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.history.clean"));
        });
    }


    private int parsePage(String pageStr) {
        try {
            return Math.max(1, Integer.parseInt(pageStr));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            List<String> list = new LinkedList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName();
                if (name.toLowerCase().startsWith(partialName)) {
                    list.add(name);
                }
            }
            return list;
        } else if (args.length == 2 && args[1].equalsIgnoreCase("clear")) {
            if (args[1].equalsIgnoreCase("clear")) {
                String partialName = args[1].toLowerCase();
                List<String> list = new LinkedList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String name = player.getName();
                    if (name.toLowerCase().startsWith(partialName)) {
                        list.add(name);
                    }
                }
                return list;
            }
        }
        return List.of();
    }
}
