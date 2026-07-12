package cn.aetheris.yuki.core.plugin.init;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.AbstractAPI;
import cn.aetheris.yuki.core.plugin.interfaces.Init;
import cn.aetheris.yuki.util.message.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public final class AntiCheatAPInit implements Init {

    @Override
    public void init() {
        Bukkit.getServicesManager().register(AbstractAPI.class, PluginLoader.INSTANCE.getExternalAPI(), Yuki.getInstance(), ServicePriority.Normal);
        LogUtils.console("&3Yuki &8» &aPlayerAPI Initialized!");
    }
}
