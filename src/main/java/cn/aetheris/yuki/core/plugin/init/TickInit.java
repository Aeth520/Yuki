package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.functionality.PerformanceMonitor;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.HookedListWrapper;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.protocol.nms.PaperUtils;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;


@SuppressWarnings(value = {"unchecked", "deprecated"})
public final class TickInit implements Init, Listener {

    public TickInit() {
    }

    private void tickRelMove() {
        if (Yuki.getInstance() == null || PluginLoader.INSTANCE.isDisable()) {
            return;
        }

        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(), () -> {
            long startNanos = System.nanoTime();
            for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
                if (!player.bypass) {
                    player.checkManager.getEntityReplication().onEndOfTickEvent();
                }
            }
            PerformanceMonitor.getInstance().recordTickTime(System.nanoTime() - startNanos);
        });
    }

    @Override
    public void init() {
        if (!PluginLoader.INSTANCE.getConfigManager().getConfig().getBooleanElse("function.sender.post", false)) {
            return;
        }
        
        boolean useUnsafe = PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("function.sender.unsafe");

        
        if (!hookTickList(useUnsafe) && !PaperUtils.registerTickEndEvent(this, this::tickAllPlayers)) {
            logUnsupportedCoreError();
        }

        LogUtils.console("&3Yuki &8» &aTickManager Initialized!");
        LogUtils.console("&3Yuki &8» &aAntiCheat Initialized!");
    }

    private void onEndOfTick(PlayerData player) {
        player.checkManager.getEntityReplication().onEndOfTickEvent();
    }

    private void tickAllPlayers() {
        MHDFScheduler.getAsyncScheduler().runTask(Yuki.getInstance(),
                () -> {
                    long startNanos = System.nanoTime();
                    for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
                        if (player.bypass) continue;
                        onEndOfTick(player);
                    }
                    PerformanceMonitor.getInstance().recordTickTime(System.nanoTime() - startNanos);
                });
    }

    private boolean hookTickList(boolean useUnsafe) {
        try {
            Object connection = SpigotReflectionUtil.getMinecraftServerConnectionInstance();
            if (connection == null) return false;

            Field connectionsListField = Reflection.getField(connection.getClass(), List.class, 1);
            connectionsListField.setAccessible(true);
            List<Object> originalList = (List<Object>) connectionsListField.get(connection);

            List<Object> hookedList = Collections.synchronizedList(new HookedListWrapper<>(originalList) {
                @Override
                public void onIterator() {
                    tickRelMove();
                }
            });

            if (useUnsafe) {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                Unsafe unsafe = (Unsafe) unsafeField.get(null);
                unsafe.putObject(connection, unsafe.objectFieldOffset(connectionsListField), hookedList);
            } else {
                connectionsListField.set(connection, hookedList);
            }
            return true;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void logUnsupportedCoreError() {
        for (int i = 0; i < 4; i++) {
            LogUtils.console("&3Yuki &8» &c检测到不支持的服务器核心！");
        }
    }
}