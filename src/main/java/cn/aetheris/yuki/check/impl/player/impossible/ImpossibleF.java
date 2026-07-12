package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "ImpossibleF (SPECTATE)", configName = "ImpossibleF", description = "Spectate Disabler", type = CheckType.IMPOSSIBLE)
public final class ImpossibleF extends Check implements PacketCheck {
    public ImpossibleF(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.SPECTATE) {
            if (player.gamemode != GameMode.SPECTATOR && player.gamemode != null) {
                if (flagAndAlert()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
