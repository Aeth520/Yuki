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

@CheckData(name = "ElytraA (Gliding)", configName = "ElytraA", type = CheckType.ELYTRA, experimental = true)
public final class ElytraA extends Check implements PostPredictionCheck {

    private boolean setback;

    public ElytraA(PlayerData player) {
        super(player);
    }

    public void onStartGliding(PacketReceiveEvent event) {
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
            return;
        }

        if (player.isGliding && flagAndAlert()) {
            setback = true;
            event.setCancelled(true);
            player.onPacketCancel();
        } else if (player.wasGliding
                && player.isSprinting
                && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_4)) {

            if (shouldModifyPackets()) {
                event.setCancelled(true);
                player.onPacketCancel();
                player.resyncPose();
                setback = true;
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15) && event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION
                && new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA
        ) onStartGliding(event);
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (setback) {
            setbackIfAboveSetbackVL();
            setback = false;
        }
    }
}