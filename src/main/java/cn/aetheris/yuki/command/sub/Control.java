package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class Control extends AbstractCommand {
    public Control() {
        super(
                "Disable or Enable yuki for player",
                "yuki.commands.control",
                false
        );
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.control.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-found").replace("%player%", args[0]));
            return;
        }

        PlayerData data = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(target);
        if (data == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "not-data-user"));
            return;
        }
        if (!data.bypass) {
            data.bypass = true;
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.control.disable"));
        } else {
            data.bypass = false;
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.control.enable"));
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
        }
        return List.of();
    }
}