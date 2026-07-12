package cn.aetheris.mhdfscheduler.runnable;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class MHDFRunnable {

    private BukkitTask task;

    public abstract void run();

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void runTask(Plugin plugin) {
        task = org.bukkit.Bukkit.getScheduler().runTask(plugin, this::run);
    }

    public void runTaskLater(Plugin plugin, long delayTicks) {
        task = org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, this::run, delayTicks);
    }

    public void runTaskTimer(Plugin plugin, long delayTicks, long periodTicks) {
        task = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, this::run, delayTicks, periodTicks);
    }

    public void runTaskAsynchronously(Plugin plugin) {
        MHDFScheduler.getAsyncScheduler().runTask(plugin, this::run);
    }

    public void runTaskLaterAsynchronously(Plugin plugin, long delayTicks) {
        MHDFScheduler.getAsyncScheduler().runTaskLater(plugin, this::run, delayTicks);
    }

    public void runTaskTimerAsynchronously(Plugin plugin, long delayTicks, long periodTicks) {
        MHDFScheduler.getAsyncScheduler().runTaskTimer(plugin, this::run, delayTicks, periodTicks);
    }
}
