package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import com.j256.ormlite.logger.Level;

public final class DatabaseInit implements Init {

    @Override
    public void init() {
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.OFF);
        PluginLoader.INSTANCE.getDatabaseManager().start();
    }
}

