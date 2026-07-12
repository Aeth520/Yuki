package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static net.kyori.adventure.text.event.ClickEvent.Action.RUN_COMMAND;
import static net.kyori.adventure.text.event.ClickEvent.clickEvent;
import static net.kyori.adventure.text.event.HoverEvent.showText;

public final class Spectate extends AbstractCommand {

    public Spectate() {
        super("Spectate a player", "yuki.commands.spectate", true);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            player.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.spectate.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-found").replace("%player%", args[1]));
            return;
        }

        if (target == player) {
            player.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("not-my-self"));
            return;
        }

        if (PluginLoader.INSTANCE.getSpectateManager().enable(player)) {

            PlayerData playerData = PluginLoader.INSTANCE.getPlayerDataManager().getPlayer(player);
            if (playerData == null) {
                player.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "not-data-user"));
                return;
            }

            Component formattedMessage = Component.text(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.spectate.return"))
                    .clickEvent(clickEvent(RUN_COMMAND, "/yuki stopspectating"))
                    .hoverEvent(showText(Component.text("/yuki stopspectating")));
            playerData.getUser().sendMessage(formattedMessage);
            player.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n(target, "commands.spectate.message"));
        }

        player.setGameMode(GameMode.SPECTATOR);
        PaperUtils.teleport(player, target.getLocation());
    }


    @Override
    public List<String> tabCompleter(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new LinkedList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(sender)) {
                    String name = p.getName();
                    if (name.toLowerCase().startsWith(args[0].toLowerCase())) {
                        list.add(name);
                    }
                }
            }
            return list;
        }
        return List.of();
    }
}
