package cn.aetheris.yuki;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;

public final class Main {

    public static void start() {
        MHDFScheduler.getGlobalRegionScheduler().runTask(Yuki.getInstance(), () -> {
            PluginLoader.INSTANCE.start();
            MHDFScheduler.getGlobalRegionScheduler().runTaskLater(Yuki.getInstance(), () ->
                    Yuki.setEnablePlugin(true), 20L);
        });
    }
}
