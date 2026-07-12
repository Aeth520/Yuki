package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public final class PaperMCHook extends AbstractHook {
    @Override
    public void hook() {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_11_2)
                && !Boolean.getBoolean("paper.explicit-flush")) {
            LogUtils.console("&3Yuki &8» &c您需要为您的服务器启动参数添加 &e-Dpaper.explicit-flush=true &c参数才能获取Reach/HitBoxs额外的精准度!");
            super.enabled = true;
        }
    }

    @Override
    public void unhook() {
        super.enabled = false;

    }
}
