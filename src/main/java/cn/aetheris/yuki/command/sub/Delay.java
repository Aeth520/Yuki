package cn.aetheris.yuki.command.sub;

import cn.aetheris.mhdfscheduler.runnable.MHDFRunnable;
import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.delay.usage"));
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

            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.delay.message")
                    .replace("%command%", command)
                    .replace("%delay%", String.valueOf(delay)));
        } catch (NumberFormatException e) {
            sender.sendMessage(PluginLoader.INSTANCE.getLangManger().i18n("commands.delay.invalid"));
        }
    }

    private void startStopRunnable(CommandSender sender, String command, int delay) {
        new MHDFRunnable() {
            private boolean canRun = false;

            @Override
            public void run() {
                if (canRun) {
                    this.cancel();

                    if (sender instanceof Entity entity) {
                        MHDFScheduler.getEntityScheduler().runTask(
                                Yuki.getInstance(),
                                entity,
                                () -> Bukkit.dispatchCommand(sender, command),
                                null
                        );
                    } else {
                        MHDFScheduler.getGlobalRegionScheduler().runTask(
                                Yuki.getInstance(),
                                () -> Bukkit.dispatchCommand(sender, command)
                        );
                    }
                    return;
                }
                canRun = true;
            }
        }.runTaskTimerAsynchronously(Yuki.getInstance(), 0L, delay * 20L);
    }
}