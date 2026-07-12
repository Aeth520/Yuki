package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;

@CheckData(name = "BadPacketsN (Attack)", type = CheckType.BADPACKETS, configName = "BadPacketsN", description = "Interacted with non-existent entity")
public final class BadPacketsN extends Check implements PacketCheck {
    public BadPacketsN(PlayerData player) {
        super(player);
    }
}
