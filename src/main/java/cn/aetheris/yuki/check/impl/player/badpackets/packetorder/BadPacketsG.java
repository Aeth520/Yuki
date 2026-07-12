package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.entity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BadPacketsG (Interact)", type = CheckType.BADPACKETS, configName = "BadPacketsG", description = "Missed Interact", decay = 0.45)
public final class BadPacketsG extends Check implements PacketCheck {


    private final boolean exempt = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10) || isExempt(ExemptType.CLIENT_ANTICHEAT);
    private boolean sentInteractAt = false;
    private int requiredEntity;
    private InteractionHand requiredHand;
    private boolean requiredSneaking;

    public BadPacketsG(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (exempt) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);

            final PacketEntity entity = player.compensatedEntities.entityMap.get(packet.getEntityId());

            if (entity == null) {
                return;
            }
            
            
            
            
            if (entity.getType() == EntityTypes.ARMOR_STAND) {
                return;
            }

            final boolean sneaking = packet.isSneaking().orElse(false);

            switch (packet.getAction()) {
                case INTERACT -> {
                    if (!sentInteractAt) {
                        if (flagAndAlert("Skipped Interact-At") && shouldModifyPackets()) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                        }
                    } else if (packet.getEntityId() != requiredEntity || packet.getHand() != requiredHand || sneaking != requiredSneaking) {
                        String verbose = "requiredEntity=" + requiredEntity + "\nentity=" + packet.getEntityId()
                                + "\nrequiredHand=" + requiredHand + "\nhand=" + packet.getHand()
                                + "\nrequiredSneaking=" + requiredSneaking + "\nsneaking=" + sneaking;
                        if (flagAndAlert(verbose) && shouldModifyPackets()) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                            player.mitigateDamage();
                        }
                    }

                    sentInteractAt = false;
                }
                case INTERACT_AT -> {
                    if (sentInteractAt) {
                        if (flagAndAlert("missed interact") && shouldModifyPackets()) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                            player.mitigateDamage();
                        }
                    }

                    requiredHand = packet.getHand();
                    requiredEntity = packet.getEntityId();
                    requiredSneaking = sneaking;
                    sentInteractAt = true;
                }
            }
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (sentInteractAt) {
                sentInteractAt = false;
                if (alert("Skipped Ticking??")) {
                    player.mitigateDamage();
                }
            }
        }
    }
}