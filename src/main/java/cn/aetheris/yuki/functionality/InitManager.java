package cn.aetheris.yuki.functionality;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.init.*;
import cn.aetheris.yuki.core.plugin.interfaces.Hook;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.core.plugin.interfaces.Stop;
import cn.aetheris.yuki.core.plugin.stop.DatabaseTerminate;
import cn.aetheris.yuki.core.plugin.stop.FastInventoryTerminate;
import cn.aetheris.yuki.core.plugin.stop.LagTrackTerminate;
import cn.aetheris.yuki.core.plugin.stop.PluginChannelTerminate;
import cn.aetheris.yuki.util.AntiCheatUtil;
import cn.aetheris.yuki.util.maps.ClassLoadingMap;

public final class InitManager {
    ClassLoadingMap<Hook> initializersOnHook;
    ClassLoadingMap<Hook> initializersOnUnHook;
    ClassLoadingMap<Init> initializersOnStart;
    ClassLoadingMap<Stop> initializersOnStop;

    public InitManager() {
        initializersOnStart = new ClassLoadingMap<>(null);
        initializersOnHook = new ClassLoadingMap<>(null);
        initializersOnUnHook = new ClassLoadingMap<>(null);
        initializersOnStop = new ClassLoadingMap<>(null);

        initializersOnStart.put(ExemptInit.class, new ExemptInit());
        initializersOnStart.put(LimiterInit.class, new LimiterInit());
        initializersOnStart.put(DatabaseInit.class, new DatabaseInit());
        initializersOnStart.put(CommandInit.class, new CommandInit());
        initializersOnStart.put(PacketListenerInit.class, new PacketListenerInit());
        initializersOnStart.put(ListenerInit.class, new ListenerInit());
        initializersOnStart.put(PluginChannelInit.class, new PluginChannelInit());
        initializersOnStart.put(SyncInit.class, new SyncInit());
        initializersOnStart.put(LagTrackInit.class, new LagTrackInit());
        initializersOnStart.put(FastInventoryInit.class, new FastInventoryInit());
        initializersOnStart.put(AntiCheatAPInit.class, new AntiCheatAPInit());
        initializersOnStart.put(TickInit.class, new TickInit());
        initializersOnStart.put(AntiCheatUtil.class, PluginLoader.INSTANCE.getExternalAPI());
        initializersOnStart.put(SpectateManager.class, new SpectateManager());
        initializersOnStart.put(ExperimentalWarnInit.class, new ExperimentalWarnInit());

        initializersOnHook.put(HookInit.class, new HookInit());

        initializersOnUnHook.put(HookInit.class, new HookInit());

        initializersOnStop.put(PluginChannelTerminate.class, new PluginChannelTerminate());
        initializersOnStop.put(DatabaseTerminate.class, new DatabaseTerminate());
        initializersOnStop.put(LagTrackTerminate.class, new LagTrackTerminate());
        initializersOnStop.put(FastInventoryTerminate.class, new FastInventoryTerminate());
    }

    public void init() {
        initializersOnStart.forEachValue(Init::init);
    }

    public void hook() {
        initializersOnHook.forEachValue(Hook::hook);
    }

    public void unHook() {
        initializersOnUnHook.forEachValue(Hook::unhook);
    }

    public void stop() {
        initializersOnStop.forEachValue(Stop::stop);
    }
}
