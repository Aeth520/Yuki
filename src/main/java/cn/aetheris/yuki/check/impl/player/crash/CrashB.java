package cn.aetheris.yuki.check.impl.player.crash;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;

@CheckData(name = "CrashB", configName = "CrashB", type = CheckType.CRASH, description = "Spoofed Creative Packet")
public final class CrashB extends Check implements PacketCheck {
    public CrashB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            if (player.gamemode != GameMode.CREATIVE && player.gamemode != null) {
                if (flagAndAlert("mode= " + player.gamemode)) {
                    player.getSetbackTeleportUtil().executeViolationSetback();
                    event.setCancelled(true);
                    player.onPacketCancel();
                    kickPlayer();
                }
            }
        }
    }
}
