package cn.aetheris.yuki.command.sub;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import cn.aetheris.yuki.data.player.FreezeData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class Freeze extends AbstractCommand {

    public Freeze() {
        super("freeze", "yuki.commands.freeze", false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.freeze.usage"));
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

        if (FreezeData.isFrozen(target)) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.freeze.already-frozen"));
            return;
        }

        FreezeData.setFrozen(target, true);

        MHDFScheduler.getEntityScheduler().runTask(Yuki.getInstance(), target, () -> {
            if (target.getVehicle() != null) target.leaveVehicle();
        }, null);

        sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n(target, "commands.freeze.message"));
        target.sendMessage(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("commands.freeze.target-side"));
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
