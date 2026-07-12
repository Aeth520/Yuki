package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineLava;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineNormal;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineWater;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineWaterLegacy;
import cn.aetheris.yuki.protocol.nms.BlockProperties;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public final class MovementTickerPlayer extends MovementTicker {
    public MovementTickerPlayer(PlayerData player) {
        super(player);
    }

    @Override
    public void doWaterMove(float swimSpeed, boolean isFalling, float swimFriction) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
            new PredictionEngineWater().guessBestMovement(swimSpeed, player, isFalling, player.gravity, swimFriction);
        } else {
            new PredictionEngineWaterLegacy().guessBestMovement(swimSpeed, player, swimFriction);
        }
    }

    @Override
    public void doLavaMove() {
        new PredictionEngineLava().guessBestMovement(0.02F, player);
    }

    @Override
    public void doNormalMove(float blockFriction) {
        new PredictionEngineNormal().guessBestMovement(BlockProperties.getFrictionInfluencedSpeed(blockFriction, player), player);
    }
}