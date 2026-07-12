package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;

public class LagTrackInit implements Init {

    @Override
    public void init() {
        PluginLoader.INSTANCE.getLagManager().start();
    }
}
