package cn.aetheris.yuki.check.impl.combat.reach;


import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;

@CheckData(name = "ReachD (Entity)", configName = "ReachD", decay = 1.25, description = "Check for attack entity", experimental = true)
public final class ReachD extends Check implements PacketCheck {
    public ReachD(PlayerData player) {
        super(player);
    }
}
