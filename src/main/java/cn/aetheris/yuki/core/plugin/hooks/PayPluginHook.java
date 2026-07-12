package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.util.message.LogUtils;
import org.bukkit.Bukkit;

public final class PayPluginHook extends AbstractHook {
    @Override
    public void hook() {
        final String serverName = HookInit.getPlaceholderAPIHook().setPlaceholders(null, PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("database-manager.server-name"));

        if (Bukkit.getPluginManager().getPlugin("7yPay") != null
                && PluginLoader.INSTANCE.getConfigManager().isHook7yPay()) {

            if (!serverName.contains("hub")
                    && !serverName.contains("大厅")
                    && !serverName.contains("lobby")
                    && !serverName.contains("主城")) {
                LogUtils.console("&3Yuki &8» &c请你将DataManager配置中的ServerName改为带有hub或者大厅,否则该Hook将不会生效!");
            return;
        }
        LogUtils.console("&3Yuki &8» &c检查到您开启了7yPay的Hook,这可能会造成服务器崩溃或者刷物品的漏洞!");
            enabled = true;
        }
    }

    @Override
    public void unhook() {
        enabled = false;
    }
}
