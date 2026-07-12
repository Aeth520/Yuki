package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;

@CheckData(name = "BadPacketsK", type = CheckType.BADPACKETS)
public final class BadPacketsK extends Check implements PacketCheck {

    public BadPacketsK(PlayerData player) {
        super(player);
    }

}
