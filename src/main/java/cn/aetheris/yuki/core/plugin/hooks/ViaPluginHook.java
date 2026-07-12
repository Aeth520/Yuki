package cn.aetheris.yuki.core.plugin.hooks;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.functionality.AbstractHook;
import cn.aetheris.yuki.util.message.LogUtils;
import com.viaversion.viaversion.api.Via;

public final class ViaPluginHook extends AbstractHook {
    @Override
    public void hook() {
        if (Yuki.getInstance().getServer().getPluginManager().getPlugin("ViaVersion") != null) {
            System.setProperty("com.viaversion.handlePingsAsInvAcknowledgements", "true");
            super.enabled = true;
            if (Via.getConfig().is1_14HitboxFix()
                    && Via.getConfig().getValues().containsKey("change-1_14-hitbox")) {
                LogUtils.consolePrefixed("&c检测到您的 &fViaVersion &c插件中的配置开启了 &f'change-1_14-hitbox' &c!");
                LogUtils.consolePrefixed("&c这很有可能造成绕过和误判,请立刻关闭这个选项!");
            }
            if (Via.getConfig().getValues().containsKey("fix-1_21-placement-rotation")
                    && Via.getConfig().fix1_21PlacementRotation()) {
                LogUtils.consolePrefixed("&c检测到您的 &fViaVersion &c插件中的配置开启了 &f'fix-1_21-placement-rotation' &c!");
                LogUtils.consolePrefixed("&c这很有可能造成绕过和误判,请立刻关闭这个选项!");
            }
        }
    }

    @Override
    public void unhook() {
        super.enabled = false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
