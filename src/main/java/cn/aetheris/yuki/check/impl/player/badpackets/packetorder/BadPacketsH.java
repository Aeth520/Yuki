package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;

@CheckData(name = "BadPacketsH (Interact)", type = CheckType.BADPACKETS, configName = "BadPacketsH", description = "Spoofed Interact (ViaForge)", experimental = true)
public final class BadPacketsH extends Check implements PacketCheck {
    public BadPacketsH(final PlayerData player) {
        super(player);
    }
}
