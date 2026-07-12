package cn.aetheris.yuki.core.plugin.stop;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Stop;

public final class DatabaseTerminate implements Stop {

    @Override
    public void stop() {
        PluginLoader.INSTANCE.getDatabaseManager().stop();
    }
}
