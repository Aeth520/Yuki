package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class Profile extends AbstractCommand {

    public Profile() {
        super(
                "Show player profile",
                "yuki.commands.profile",
                false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.profile.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-found").replace("%player%", args[0]));
            return;
        }

        PlayerData playerData = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(target);
        if (playerData == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "not-data-user"));
            return;
        }

        String profileMessage = PluginLoader.INSTANCE.getLangManger().i18nWithoutPrefix("commands.profile.message");
        profileMessage = HookInit.getPlaceholderAPIHook().setPlaceholders(playerData.getBukkitPlayer(), profileMessage);
        String formattedMessage = PluginLoader.INSTANCE.getExternalAPI().replaceVariables(playerData, profileMessage, true);
        sender.sendMessage(formattedMessage);
    }


    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new LinkedList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName();
                if (name.toLowerCase().startsWith(args[0].toLowerCase())) {
                    list.add(name);
                }
            }
            return list;
        }
        return List.of();
    }
}