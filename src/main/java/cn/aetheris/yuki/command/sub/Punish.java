package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.fake.FakeAntiCheatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class Punish extends AbstractCommand {
    public Punish() {
        super(
                "Punish a player",
                "yuki.commands.punish",
                false
        );
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.punish.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("not-found").replace("%player%", args[0]));
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

        if (data.isBypass()) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "not-data-user"));
            return;
        }

        String randomKey = FakeAntiCheatUtils.getRandomName();
        String colorCode = FakeAntiCheatUtils.getColorCode(randomKey);
        String antiCheatName = FakeAntiCheatUtils.getName(randomKey);
        final String command = PluginLoader.INSTANCE.getExternalAPI().replaceVariables(data,
                PluginLoader.INSTANCE.getConfigManager().getConfig().getString("function.command-punish.command")
                        .replace("%anticheat%", antiCheatName)
                        .replace("%anticheat_color%", colorCode),
                false);

        Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.punish.message"));
            target.getWorld().strikeLightningEffect(target.getLocation().clone());
        });
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