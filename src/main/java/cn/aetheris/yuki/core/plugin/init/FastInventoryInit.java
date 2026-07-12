package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import fr.mrmicky.fastinv.FastInvManager;

public class FastInventoryInit implements Init {
    @Override
    public void init() {
        FastInvManager.register(Yuki.getInstance());
    }
}
