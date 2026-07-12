package cn.aetheris.yuki.check.impl.combat.reach;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;

@CheckData(name = "ReachC (Miss)", type = CheckType.REACH, configName = "ReachC", description = "Check for miss attack", decay = 1.0)
public final class ReachC extends Check implements PacketCheck {
    public ReachC(PlayerData player) {
        super(player);
    }
}
