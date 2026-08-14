package cn.aetheris.yuki.command.sub;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public final class Delay extends AbstractCommand {
    public Delay() {
        super(
                "Delay a command",
                "yuki.commands.delay",
                false);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.delay.usage"));
            return;
        }

        try {
            int delay = Integer.parseInt(args[0]);
            StringBuilder commandBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                commandBuilder.append(args[i]).append(" ");
            }
            final String command = commandBuilder.toString().trim();
            startStopRunnable(sender, command, delay);

            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.delay.message")
                    .replace("%command%", command)
                    .replace("%delay%", String.valueOf(delay)));
        } catch (NumberFormatException e) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManager().i18n("commands.delay.invalid"));
        }
    }

    private void startStopRunnable(CommandSender sender, String command, int delay) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Yuki.getInstance(), new BukkitRunnable() {
            private boolean canRun = false;

            @Override
            public void run() {
                if (canRun) {
                    this.cancel();

                    if (sender instanceof Entity entity) {
                        Bukkit.getScheduler().runTask(
                                Yuki.getInstance(),
                                () -> Bukkit.dispatchCommand(sender, command)
                        );
                    } else {
                        Bukkit.getScheduler().runTask(
                                Yuki.getInstance(),
                                () -> Bukkit.dispatchCommand(sender, command)
                        );
                    }
                    return;
                }
                canRun = true;
            }
        }, 0L, delay * 20L);
    }
}