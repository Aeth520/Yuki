package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.data.FreezeData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public final class UnFreeze extends AbstractCommand {

    public UnFreeze() {
        super("unfreeze", "yuki.commands.unfreeze", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.unfreeze.usage"));
            return;
        }

        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-found").replace("%player%", args[0]));
            return;
        }

        if (!FreezeData.isFrozen(target)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.unfreeze.not-freeze"));
            return;
        }

        FreezeData.setFrozen(target, false);

        sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.unfreeze.message"));
        target.sendMessage(PluginLoader.INSTANCE.getLangManger().i18nWithoutPrefix("commands.unfreeze.target-side"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions;
            String input = args[0].toLowerCase();

            List<String> list = new LinkedList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (FreezeData.isFrozen(player) && player.getName().toLowerCase().startsWith(input)) {
                    String name = player.getName();
                    list.add(name);
                }
            }
            completions = list;

            return completions;
        }
        return null;
    }
}
