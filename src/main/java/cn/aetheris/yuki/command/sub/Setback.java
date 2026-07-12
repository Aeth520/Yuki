package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class Setback extends AbstractCommand {

    private static final List<String> MODES = Arrays.asList("sync", "normal", "nonsimulating");

    public Setback() {
        super(
                "Setback a player",
                "yuki.commands.setback",
                false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        String mode;
        String playerName;

        if (args.length == 1) {
            playerName = args[0];
            mode = PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("commands.setback.default-type").toLowerCase();
        } else if (args.length == 2) {
            playerName = args[0];
            mode = args[1].toLowerCase();
            if (!MODES.contains(mode)) {
                sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.setback.no-type"));
                return;
            }
        } else {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.setback.usage"));
            return;
        }

        String permission = "yuki.commands.setback." + mode;
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("no-permission"));
            return;
        }

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("not-found").replace("%player%", playerName));
            return;
        }

        if (target.hasPermission("yuki.nosetback")) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.setback.cannot"));
            return;
        }

        if (target == sender) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("not-my-self"));
            return;
        }

        PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(target);
        if (data == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "not-data-user"));
            return;
        }

        switch (mode) {
            case "sync" -> setBackSync(data, target, mode, sender);
            case "normal" -> setBackNormal(data, target, mode, sender);
            case "nonsimulating" -> setBackNonSimulating(data, target, mode, sender);
            default -> sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.setback.no-type"));
        }
    }

    private void setBackSync(PlayerData data, Player target, String mode, CommandSender sender) {
        data.getSetbackTeleportUtil().executeForceResync();
        BlockUtils.refreshBlocksAroundPlayer(data, target.getLocation());
        sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.setback.message").replace("%mode%", mode));
    }

    private void setBackNormal(PlayerData data, Player target, String mode, CommandSender sender) {
        data.getSetbackTeleportUtil().executeViolationSetback();
        sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.setback.message").replace("%mode%", mode));
    }

    private void setBackNonSimulating(PlayerData data, Player target, String mode, CommandSender sender) {
        data.getSetbackTeleportUtil().executeNonSimulatingSetback();
        sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.setback.message").replace("%mode%", mode));
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
        } else if (args.length == 2) {
            String partial = args[1].toLowerCase();
            List<String> list = new LinkedList<>();
            for (String mode : MODES) {
                if (mode.startsWith(partial) && sender.hasPermission("yuki.commands.setback." + mode)) {
                    list.add(mode);
                }
            }
            return list;
        }
        return List.of();
    }
}