package cn.aetheris.yuki.check.impl.player.impossible;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;

@CheckData(name = "ImpossibleB (HELD)", configName = "ImpossibleB", description = "Invalid Slot", type = CheckType.IMPOSSIBLE)
public final class ImpossibleB extends Check implements PacketCheck {
    public ImpossibleB(PlayerData player) {
        super(player);
    }

    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {

            int slot = new WrapperPlayClientHeldItemChange(event).getSlot();

            if (slot < 0) {
                if (flagAndAlert("slot= " + slot) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}