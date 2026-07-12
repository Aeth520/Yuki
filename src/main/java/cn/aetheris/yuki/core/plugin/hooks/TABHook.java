package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.Bukkit;

public final class TABHook extends AbstractHook {
    @Override
    public void hook() {
        if (Bukkit.getPluginManager().getPlugin("TAB") != null
                && HookInit.getViaPluginHook().isEnabled()
                && Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
            LogUtils.consolePrefixed("&c如果您的 &fTAB &c插件中的配置关闭了 &f'compensate-for-packetevents-bug' &c!");
            LogUtils.consolePrefixed("&c这很有可能造成绕过和误判,请立刻关闭这个选项!");
            super.enabled = true;
        }
    }

    @Override
    public void unhook() {
        super.enabled = false;
    }
}
