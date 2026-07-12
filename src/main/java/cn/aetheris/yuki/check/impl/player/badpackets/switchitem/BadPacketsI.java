package cn.aetheris.yuki.check.impl.player.badpackets.switchitem;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BadPacketsI (Slots)", type = CheckType.BADPACKETS, configName = "BadPacketsI", description = "check for big switchitem", decay = 0.25)
public final class BadPacketsI extends Check implements PacketCheck {

    private int slots;
    private int buffer;

    public BadPacketsI(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            slots++;
        }
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            int max = player.getClientVersion().isNewerThan(ClientVersion.V_1_8) ? 12 : 10;

            if (slots > max) {
                if (++buffer > 1) {
                    if (flagAndAlert("s= " + slots)) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                    buffer = 0;
                }
            } else {
                buffer = Math.max(0, buffer - 1);
            }
            slots = 0;
        }
    }
}