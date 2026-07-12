package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.mhdfscheduler.scheduler.MHDFScheduler;
import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.util.message.LogUtils;

public class ExperimentalWarnInit implements Init {
    @Override
    public void init() {
        if (!PluginLoader.INSTANCE.getConfigManager().getConfig().getBoolean("function.experimental")) {
            MHDFScheduler.getAsyncScheduler().runTaskLater(Yuki.getInstance(), () -> {
                LogUtils.consolePrefixed("&c检测到您没有开启实验性模式，这可能会少很多有用的检测");
                LogUtils.consolePrefixed("&c这些检测因为还需要长久的测试来保证其的稳定性，所以您不开启就可能会造成一些绕过!");
                LogUtils.consolePrefixed("&c方法 -> settings.yml 内搜索experimental保证其为true即可!");
            }, 20L);
        }
    }
}
