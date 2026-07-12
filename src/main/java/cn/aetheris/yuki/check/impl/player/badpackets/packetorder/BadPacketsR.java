package cn.aetheris.yuki.check.impl.player.badpackets.packetorder;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BadPacketsR (Action)", type = CheckType.BADPACKETS, configName = "BadPacketsR", description = "Sent many action packet", decay = 0.5)
public final class BadPacketsR extends Check implements PostPredictionCheck {

    private int invalid;
    private boolean sent;

    public BadPacketsR(PlayerData player) {
        super(player);
        sent = false;
        invalid = 0;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            if (sent) {
                if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                    if (flagAndAlert()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    invalid++;
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction.Action action = new WrapperPlayClientEntityAction(event).getAction();

            if (action == WrapperPlayClientEntityAction.Action.STOP_SPRINTING
                    || action == WrapperPlayClientEntityAction.Action.START_SPRINTING
                    || action == WrapperPlayClientEntityAction.Action.STOP_SNEAKING
                    || action == WrapperPlayClientEntityAction.Action.START_SNEAKING
            ) {
                sent = true;
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            sent = true;
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) && !player.packetStateData.lastPacketWasTeleport) {
            sent = false;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicksPreVia()) return;

        if (player.isTickingReliablyFor(3) && !player.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(0)) {
            for (; invalid >= 1; invalid--) {
                flagAndAlert();
            }
        }

        invalid = 0;
        sent = false;
    }
}