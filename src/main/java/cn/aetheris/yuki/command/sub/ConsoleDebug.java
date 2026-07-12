package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConsoleDebug extends AbstractCommand {

    public ConsoleDebug() {
        super("Toggle console debug output", "yuki.commands.console-debug", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.console-debug.only-console"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.console-debug.usage"));
            return;
        }

        String debugType = args[0].toLowerCase();
        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-found").replace("%player%", args[1]));
            return;
        }

        PlayerData playerData = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(targetPlayer);
        if (playerData == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(targetPlayer, "not-data-user"));
            return;
        }

        boolean isOutput = false;

        switch (debugType) {
            case "prediction" -> isOutput = playerData.getCheckManager().getMotionDebugHandler().toggleConsoleOutput();
            case "rotation" -> isOutput = playerData.getCheckManager().getRotationDebugHandler().toggleConsoleOutput();
            default -> {
                sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.console-debug.usage"));
                return;
            }
        }

        if (isOutput) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(targetPlayer, "commands.console-debug.enable")
                    .replace("%type%", debugType));
        } else {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(targetPlayer, "commands.console-debug.disable")
                    .replace("%type%", debugType));
        }
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("prediction", "rotation")
                    .filter(type -> type.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            List<String> list = new LinkedList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName();
                if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                    list.add(name);
                }
            }
            return list;
        }
        return List.of();
    }
}
