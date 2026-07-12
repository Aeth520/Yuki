package cn.aetheris.yuki.check.impl.player.badpackets;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "BadPacketsF (Skipping?)", type = CheckType.BADPACKETS, configName = "BadPacketsF", decay = 0.56)
public final class BadPacketsF extends Check implements PostPredictionCheck {

    private boolean sentMainHand;
    private int requiredEntity;
    private boolean requiredSneaking;

    public BadPacketsF(PlayerData player) {
        super(player);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            final WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            WrapperPlayClientInteractEntity.InteractAction action = packet.getAction();
            if (action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final boolean sneaking = packet.isSneaking().orElse(false);
                final int entity = packet.getEntityId();

                if (packet.getHand() == InteractionHand.OFF_HAND) {
                    if (action == WrapperPlayClientInteractEntity.InteractAction.INTERACT) {
                        if (!sentMainHand) {
                            if (flagAndAlert("Skipped Mainhand") && shouldModifyPackets()) {
                                event.setCancelled(true);
                                player.onPacketCancel();
                            }
                        }
                        sentMainHand = false;
                    } else if (sneaking != requiredSneaking || entity != requiredEntity) {
                        String verbose = "requiredEntity=" + requiredEntity + "\nentity=" + entity
                                + "\nrequiredSneaking=" + requiredSneaking + "\nsneaking=" + sneaking;
                        if (flagAndAlert(verbose) && shouldModifyPackets()) {
                            event.setCancelled(true);
                            player.onPacketCancel();
                            rewardBufferAndVL();
                        }
                    }
                } else {
                    requiredEntity = entity;
                    requiredSneaking = sneaking;
                    sentMainHand = true;
                    rewardBufferAndVL();
                }
            }
        }

        if (isTickPacket(event.getPacketType())) {
            sentMainHand = false;
        }
    }
}