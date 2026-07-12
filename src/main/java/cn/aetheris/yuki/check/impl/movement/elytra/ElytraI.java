package cn.aetheris.yuki.check.impl.movement.elytra;

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

@CheckData(name = "ElytraI", description = "Started gliding in water", type = CheckType.ELYTRA, experimental = true)
public final class ElytraI extends Check implements PostPredictionCheck {
    private boolean setback;

    public ElytraI(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION
                && new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA
                && player.wasTouchingWater
                && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_15)
        ) {
            if (buffer++ > 4) {
                if (flagAndAlert("ticks= " + player.elytraTicks)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                    player.resyncPose();
                    setback = true;
                    buffer = 0;
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (setback) {
            setbackIfAboveSetbackVL();
            setback = false;
            rewardBufferAndVL();
        }
    }
}