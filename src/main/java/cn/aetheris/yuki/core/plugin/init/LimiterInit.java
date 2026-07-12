package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;

public final class LimiterInit implements Init {
    @Override
    public void init() {
        LogUtils.console("&3Yuki &8» &aPacketLimiter Initialized!");
        MHDFScheduler.getAsyncScheduler().runTaskTimer(Yuki.getInstance(), () -> {
            for (PlayerData player : PluginLoader.INSTANCE.getPlayerDataManager().getEntries()) {
                player.cancelledPackets.set(0);
            }
        }, 1, 40);
    }
}
