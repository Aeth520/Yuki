package cn.aetheris.mhdfscheduler.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Scheduler abstraction backed by the Bukkit scheduler.
 */
public final class MHDFScheduler {

    private static final GlobalRegionScheduler GLOBAL = new GlobalRegionScheduler();
    private static final AsyncScheduler ASYNC = new AsyncScheduler();
    private static final RegionScheduler REGION = new RegionScheduler();
    private static final EntityScheduler ENTITY = new EntityScheduler();

    private MHDFScheduler() {
    }

    public static boolean isFolia() {
        return false;
    }

    public static GlobalRegionScheduler getGlobalRegionScheduler() {
        return GLOBAL;
    }

    public static AsyncScheduler getAsyncScheduler() {
        return ASYNC;
    }

    public static RegionScheduler getRegionScheduler() {
        return REGION;
    }

    public static EntityScheduler getEntityScheduler() {
        return ENTITY;
    }

    public static void cancel(Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    public static final class GlobalRegionScheduler {
        private GlobalRegionScheduler() {
        }

        public void runTask(Plugin plugin, Runnable runnable) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }

        public void runTaskLater(Plugin plugin, Runnable runnable, long delayTicks) {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }

        public void runTaskTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
            Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        }
    }

    public static final class AsyncScheduler {
        private AsyncScheduler() {
        }

        public void runTask(Plugin plugin, Runnable runnable) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }

        public void runTaskLater(Plugin plugin, Runnable runnable, long delayTicks) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
        }

        public void runTaskTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
        }
    }

    public static final class RegionScheduler {
        private RegionScheduler() {
        }

        public void runTask(Plugin plugin, World world, int chunkX, int chunkZ, Runnable runnable) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }

        public void runTask(Plugin plugin, Location location, Runnable runnable) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }

        public void runTaskLater(Plugin plugin, Location location, Runnable runnable, long delayTicks) {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static final class EntityScheduler {
        private EntityScheduler() {
        }

        public void runTask(Plugin plugin, Entity entity, Runnable runnable) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }

        public void runTask(Plugin plugin, Entity entity, Runnable runnable, Runnable retired) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
}
