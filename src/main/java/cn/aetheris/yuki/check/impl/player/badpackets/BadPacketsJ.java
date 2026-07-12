package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action;

@CheckData(name = "BadPacketsJ", description = "Spoofed Jump Boost", decay = 0.45, type = CheckType.BADPACKETS)
public final class BadPacketsJ extends Check implements PacketCheck {
    public BadPacketsJ(final PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);

            if (wrapper.getJumpBoost() < 0
                    && player.compensatedEntities.getSelf().getRiding().getType() == EntityTypes.CAMEL) return;

            if (isExempt(ExemptType.JOIN)) return; 

            if (player.getSetbackTeleportUtil().insideUnloadedChunk()) return;

            if (Math.abs(wrapper.getJumpBoost()) > 100
                    || wrapper.getEntityId() != player.entityID
                    || wrapper.getAction() != Action.START_JUMPING_WITH_HORSE && wrapper.getJumpBoost() != 0) {
                if (wrapper.getJumpBoost() != 0) {

                    if (flagAndAlert("b= " + wrapper.getJumpBoost()
                            + "\na= " + wrapper.getAction()
                            + "\ne= " + wrapper.getEntityId()) && shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    rewardVL();
                }
            }
        }
    }
}
