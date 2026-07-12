package cn.aetheris.yuki.core.plugin.stop;

import cn.aetheris.yuki.core.plugin.interfaces.Stop;
import fr.mrmicky.fastinv.FastInvManager;

public class FastInventoryTerminate implements Stop {

    @Override
    public void stop() {
        FastInvManager.closeAll();
    }
}
