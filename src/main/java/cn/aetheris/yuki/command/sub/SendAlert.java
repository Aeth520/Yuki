package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.util.message.ColorUtils;
import cn.aetheris.yuki.util.message.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class SendAlert extends AbstractCommand {

    public SendAlert() {
        super("Send an alert message", "yuki.commands.send-alert", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String string, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.send-alert.usage"));
            return;
        }

        String mode = PluginLoader.INSTANCE.getConfigManager().getConfig().getStringElse("commands.send-alert.default-type", "normal");
        String content;
        if (args[0].equalsIgnoreCase("normal") || args[0].equalsIgnoreCase("raw")) {
            mode = args[0].toLowerCase();
            if (args.length < 2) {
                sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.send-alert.usage"));
                return;
            }
            content = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        } else {
            content = String.join(" ", args);
        }

        String message;
        if (mode.equals("normal")) {
            message = PluginLoader.INSTANCE.getLangManger().format("%prefix%" + content);
        } else {
            message = PluginLoader.INSTANCE.getLangManger().format(content);
        }

        for (UUID uuid : PluginLoader.INSTANCE.getAlertManager().getEnabledAlerts()) {
            final Player bukkitPlayer = Bukkit.getPlayer(uuid);
            if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
                bukkitPlayer.sendMessage(ColorUtils.color(message));
            }
        }


        if (PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("output.alerts.console", true)) {
            LogUtils.console(message);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            if ("normal".startsWith(args[0].toLowerCase())) {
                suggestions.add("normal");
            }
            if ("raw".startsWith(args[0].toLowerCase())) {
                suggestions.add("raw");
            }
        }
        return suggestions;
    }
}