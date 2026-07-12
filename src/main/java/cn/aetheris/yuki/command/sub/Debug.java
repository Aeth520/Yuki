package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.check.impl.combat.analysis.AnalysisA;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class Debug extends AbstractCommand {

    public Debug() {
        super(
                "Toggle various debug types",
                "yuki.commands.debug",
                true
        );
    }

    @Override
    public void execute(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.usage"));
            return;
        }

        if (!PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.develop.enable", false)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.no-develop"));
            return;
        }

        String debugType = args[0].toLowerCase();

        switch (debugType) {
            case "normal":
                toggleDebug(sender);
                break;
            case "mitigate":
                toggleMitigateDebug(sender);
                break;
            case "setback":
                toggleSetbackDebug(sender);
                break;
            case "packetcancel":
                togglePacketCancelDebug(sender);
                break;
            case "sync":
                toggleSyncDebug(sender);
                break;
            case "prediction":
                togglePredictionDebug(sender, args);
                break;
            case "rotating":
                toggleRotationDebug(sender, args);
                break;
            case "analysis":
                toggleAnalysisDebug(sender, args);
                break;
            default:
                sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.usage"));
        }
    }


    private void toggleDebug(Player sender) {
        PluginLoader.INSTANCE.getAlertManager().toggleDebug(sender.getUniqueId());
    }

    private void toggleMitigateDebug(Player sender) {
        PluginLoader.INSTANCE.getAlertManager().toggleMitigateDebug(sender.getUniqueId());
    }


    private void toggleSetbackDebug(Player sender) {
        PluginLoader.INSTANCE.getAlertManager().toggleSetbackDebug(sender.getUniqueId());
    }

    private void togglePacketCancelDebug(Player sender) {
        PluginLoader.INSTANCE.getAlertManager().togglePacketCancelDebug(sender.getUniqueId());
    }

    private void toggleSyncDebug(Player sender) {
        PluginLoader.INSTANCE.getAlertManager().toggleSyncDebug(sender.getUniqueId());
    }

    
    private void togglePredictionDebug(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.usage"));
            return;
        }
        Player targetPlayer = getTargetPlayer(sender, args[1]);
        if (targetPlayer != null) {
            PlayerData playerData = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(targetPlayer);
            if (playerData != null) {
                playerData.getCheckManager().getMotionDebugHandler().toggleListener(sender);
            }
        }
    }

    
    private void toggleRotationDebug(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.usage"));
            return;
        }
        Player targetPlayer = getTargetPlayer(sender, args[1]);
        if (targetPlayer != null) {
            PlayerData playerData = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(targetPlayer);
            if (playerData != null) {
                playerData.getCheckManager().getRotationDebugHandler().toggleListener(sender);
            }
        }
    }

    
    private void toggleAnalysisDebug(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.usage"));
            return;
        }
        Player targetPlayer = getTargetPlayer(sender, args[1]);
        if (targetPlayer != null) {
            toggleAnalysisForPlayer(sender, targetPlayer);
        }
    }

    private Player getTargetPlayer(Player sender, String playerName) {
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-found").replace("%player%", playerName));
        }
        return targetPlayer;
    }

    private void toggleAnalysisForPlayer(Player sender, Player targetPlayer) {
        if (!AnalysisA.DEBUG_PLAYERS.contains(targetPlayer.getName())) {
            AnalysisA.DEBUG_PLAYERS.add(targetPlayer.getName());
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.enable").replace("%type%", "analysis"));
        } else {
            AnalysisA.DEBUG_PLAYERS.remove(targetPlayer.getName());
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.debug.disable").replace("%type%", "analysis"));
        }
    }

    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender,
                                     @NotNull String label,
                                     @NotNull String[] args) {
        List<String> subCommands = List.of(
                "normal", "mitigate", "setback", "packetcancel", "sync", "prediction", "rotating", "analysis"
        );

        if (args.length == 0) {
            return new LinkedList<>(subCommands);
        }

        if (args.length == 1) {
            return filterSubCommands(subCommands, args[0]);
        }

        if (args.length == 2 && requiresPlayer(args[0])) {
            return getOnlinePlayers(args[1]);
        }

        return new LinkedList<>();
    }

    private List<String> filterSubCommands(List<String> subCommands, String input) {
        List<String> list = new ArrayList<>();
        for (String s : subCommands) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                list.add(s);
            }
        }
        return list;
    }

    private boolean requiresPlayer(String debugType) {
        return "rotating".equalsIgnoreCase(debugType) || "analysis".equalsIgnoreCase(debugType) || "prediction".equalsIgnoreCase(debugType);
    }

    private List<String> getOnlinePlayers(String input) {
        List<String> list = new LinkedList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            if (name.toLowerCase().startsWith(input.toLowerCase())) {
                list.add(name);
            }
        }
        return list;
    }
}
