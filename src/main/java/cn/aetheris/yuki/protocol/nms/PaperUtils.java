package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class PaperUtils {

    private static final AtomicReference<Double> tpsResult = new AtomicReference<>(null);
    private static final long lastRefreshTime = 0;
    private static volatile double cachedTps = 20.0;
    public static boolean PAPER;
    public static boolean TICK_END_EVENT;
    private static Boolean nativeSupportAdventureApi;

    public static void register() {
        PAPER = ReflectionUtils.hasClass("com.destroystokyo.paper.PaperConfig") || ReflectionUtils.hasClass("io.papermc.paper.configuration.Configuration");
        TICK_END_EVENT = ReflectionUtils.hasClass("com.destroystokyo.paper.event.server.ServerTickEndEvent");
    }

    private static void refreshTps() {
        try {
            double[] tps = Bukkit.getTPS();
            if (tps != null && tps.length > 0) {
                cachedTps = tps[0];
            }
        } catch (NoSuchMethodError ignored) {
        }
    }

    public static double getCachedTps() {
        return cachedTps;
    }


    @SuppressWarnings("unchecked")
    public static boolean registerTickEndEvent(Listener listener, Runnable runnable) {
        if (TICK_END_EVENT) {
            try {
                Class<?> clazz = ReflectionUtils.getClass("com.destroystokyo.paper.event.server.ServerTickEndEvent");
                if (clazz == null) return false;
                Yuki.getInstance().getServer().getPluginManager().registerEvent((Class<? extends Event>) clazz,
                        listener,
                        EventPriority.NORMAL,
                        (l, event) -> runnable.run(), Yuki.getInstance());
                return true;
            } catch (Exception e) {
                LogUtils.exception("Failed to register tick end event", e);
            }
        }
        return false;
    }

    public static void teleport(Player player, Location location) {
        if (PAPER && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_14_4)) {
            player.teleportAsync(location);
        } else {
            player.teleport(location);
        }
    }

    public static CompletableFuture<Boolean> teleportAsync(final Entity entity, final Location location) {
        return PAPER ? entity.teleportAsync(location) : CompletableFuture.completedFuture(entity.teleport(location));
    }

    
    public static boolean isNativeSupportAdventureApi() {
        if (nativeSupportAdventureApi == null) {
            try {
                Class.forName("net.kyori.adventure.text.Component");
                Player.class.getDeclaredMethod("displayName");
                nativeSupportAdventureApi = true;
            } catch (NoSuchMethodError | ClassNotFoundException | NoSuchMethodException e) {
                nativeSupportAdventureApi = false;
            }
        }

        return nativeSupportAdventureApi;
    }

    public static CompletableFuture<Double> getTPS(Location loc, boolean isFolia) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        if (isFolia) {
            Bukkit.getScheduler().runTask(Yuki.getInstance(), () -> {
                try {
                    Method getTPSMethod = Bukkit.getServer().getClass().getMethod(
                            Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_21_1)
                                    ? "getRegionTPS" : "getTPS", Location.class);

                    double[] tps = (double[]) getTPSMethod.invoke(Bukkit.getServer(), loc);

                    if (tps != null && tps.length > 0) {
                        future.complete(tps[0]);
                    }
                } catch (Exception ignored) {
                    double[] bukkitTps = Bukkit.getTPS();
                    if (bukkitTps != null && bukkitTps.length > 0) future.complete(bukkitTps[0]);
                }
            });
        } else {
            double tps = SpigotReflectionUtil.getTPS();
            future.complete(tps);
        }

        return future;
    }

}