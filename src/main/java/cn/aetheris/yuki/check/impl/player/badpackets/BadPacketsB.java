package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "BadPacketsB (Interact)", type = CheckType.BADPACKETS, configName = "BadPacketsB", description = "Interacted with self", decay = 1.0, setback = 5)
public final class BadPacketsB extends Check implements PacketCheck {
    public BadPacketsB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);

            if (packet.getEntityId() == player.entityID || packet.getEntityId() < 0) {

                if (buffer++ > 3) {
                    if (flagAndAlert("e= " + packet.getEntityId()) && shouldModifyPackets()) {
                        player.getSetbackTeleportUtil().executeNonSimulatingSetback();
                        player.mitigateDamage();
                        event.setCancelled(true);
                        player.onPacketCancel();
                        buffer = 0;
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}