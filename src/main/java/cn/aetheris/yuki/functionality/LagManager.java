package cn.aetheris.yuki.functionality;

import cn.aetheris.mhdfscheduler.runnable.MHDFRunnable;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import lombok.Getter;

@Getter
public class LagManager extends MHDFRunnable {
    private long lastTick;
    private long tick;

    public void start() {
        this.runTaskTimer(Yuki.getInstance(), 0L, 1L);
    }

    public void stop() {
        this.cancel();
    }

    public boolean isLagging(final long now) {
        final long waitTick = PluginLoader.INSTANCE.getConfigManager().getConfig().getLong("function.lag-track.tick");
        return now - tick > waitTick || tick - lastTick > waitTick;
    }

    public boolean isLagging() {
        return isLagging(System.currentTimeMillis());
    }

    public long getLaggingTime(final long now) {
        return now - lastTick;
    }

    public long getLaggingTime2() {
        return tick - lastTick;
    }

    public long getGCPauseTime() {
        return cn.aetheris.yuki.util.gc.GCUtil.getTotalGcPauseMillis();
    }

    @Override
    public void run() {
        lastTick = tick;
        tick = System.currentTimeMillis();
    }
}
