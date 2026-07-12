package cn.aetheris.yuki.core.plugin.stop;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Stop;

public class LagTrackTerminate implements Stop {
    @Override
    public void stop() {
        PluginLoader.INSTANCE.getLagManager().stop();
    }
}
