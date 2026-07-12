package cn.aetheris.yuki.check.impl.player.badpackets.switchitem;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;

@CheckData(name = "BadPacketsA", type = CheckType.BADPACKETS, configName = "BadPacketsA", description = "Sent duplicate slot id")
public final class BadPacketsA extends Check implements PacketCheck {

    int lastSlot = -1;
    boolean invalid;

    public BadPacketsA(final PlayerData player) {
        super(player);
        invalid = false;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
            final WrapperPlayServerHeldItemChange wrapper = new WrapperPlayServerHeldItemChange(event);
            player.setLastServerChangeSlot(wrapper.getSlot());
            invalid = true;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);

            int slot = wrapper.getSlot();

            if (player.getLastServerChangeSlot() != slot) {
                wrapper.setSlot(player.getLastServerChangeSlot());
                player.setLastServerChangeSlot(slot);
            }

            if (isExempt(ExemptType.RESPAWN) || invalid) {
                return;
            }

            if (slot == lastSlot) {
                if (flagAndAlert("slot=" + slot)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
            lastSlot = slot;
        }
    }
}