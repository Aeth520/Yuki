package cn.aetheris.yuki.check.impl.combat.reach;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "ReachE (Raytrace)", configName = "ReachE", description = "Check for attack entity")
public class ReachE extends Check implements PacketCheck {

    public ReachE(@NotNull PlayerData player) {
        super(player);
    }
}
