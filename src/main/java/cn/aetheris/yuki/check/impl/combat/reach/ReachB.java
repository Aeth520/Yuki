package cn.aetheris.yuki.check.impl.combat.reach;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;

@CheckData(name = "ReachB (Entity)", type = CheckType.REACH, configName = "ReachB", decay = 0.95, description = "Check for attack entity")
public final class ReachB extends Check implements PacketCheck {
    public ReachB(PlayerData player) {
        super(player);
    }
}
