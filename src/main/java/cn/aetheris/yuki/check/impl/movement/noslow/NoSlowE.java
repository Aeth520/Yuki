package cn.aetheris.yuki.check.impl.movement.noslow;

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

import static com.github.retrooper.packetevents.protocol.potion.PotionTypes.BLINDNESS;

@CheckData(name = "NoSlowE (Potion)", type = CheckType.NOSLOW, configName = "NoSlowE", description = "Ignore Blindness Potion", decay = 0.18)
public final class NoSlowE extends Check implements PostPredictionCheck {

    public boolean startedSprintingBeforeBlind = false;

    public NoSlowE(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            if (new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                startedSprintingBeforeBlind = false;
            }

        }
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) {
            return;
        }

        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) {
            return;
        }

        
        
        
        if (player.getRespawnTick() < 60L) {
            return;
        }

        if (predictionComplete.getData().isTeleport() || player.getSetbackTeleportUtil().shouldBlockMovement()) {
            return;
        }

        if (player.compensatedEntities.getSelf().hasPotionEffect(BLINDNESS)) {
            if (player.isSprinting && !startedSprintingBeforeBlind) {
                if (flagAndAlert("")) setbackIfAboveSetbackVL();
            } else rewardVL();
        }
    }
}