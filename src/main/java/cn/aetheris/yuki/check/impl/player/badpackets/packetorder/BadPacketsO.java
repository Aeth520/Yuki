package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;

@CheckData(name = "BadPacketsO", description = "Invalid Transaction Order", experimental = true, type = CheckType.BADPACKETS)
public final class BadPacketsO extends Check implements PacketCheck {

    public BadPacketsO(PlayerData player) {
        super(player);
    }

    public void startFlag(String flag, int skips) {
        if (flagAndAlert(flag)) {
            LogUtils.console("&3Yuki &8» &f" + player.getName() + " Skipped &b" + skips + " transaction!");
        }
    }
}