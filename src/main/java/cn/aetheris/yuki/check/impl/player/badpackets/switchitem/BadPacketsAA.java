package cn.aetheris.yuki.check.impl.player.badpackets.switchitem;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;

@CheckData(name = "BadPacketsAA (Switch)", type = CheckType.BADPACKETS, configName = "BadPacketsAA", description = "Fast-Switch", decay = 0.25)
public final class BadPacketsAA extends Check implements PacketCheck {

    boolean invalid;

    public BadPacketsAA(PlayerData player) {
        super(player);
        invalid = false;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
            invalid = true;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final int slot = new WrapperPlayClientHeldItemChange(event).getSlot();

            if (slot > 8 || slot < 0 && !invalid) {
                if (flagAndAlert("slot= " + slot)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}