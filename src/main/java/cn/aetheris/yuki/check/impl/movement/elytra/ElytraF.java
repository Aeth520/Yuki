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

@CheckData(name = "ElytraF", configName = "ElytraF", type = CheckType.ELYTRA, description = "Started gliding while on ground")
public final class ElytraF extends Check implements PostPredictionCheck {
    private boolean setback;
    private int onGroundTick;

    public ElytraF(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {

            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                return;
            }

            onGroundTick = player.clientClaimsLastOnGround ? onGroundTick + 1 : 0;

            if (new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA
                    && onGroundTick > 7
                    && flagAndAlert()
            ) {
                event.setCancelled(true);
                player.onPacketCancel();
                player.resyncPose();
                setback = true;
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (setback) {
            setbackIfAboveSetbackVL();
            setback = false;
        }
    }
}
