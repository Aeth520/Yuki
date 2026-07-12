package cn.aetheris.yuki.predictionengine.movementtick;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.predictions.rideable.PredictionEngineRideableLava;
import cn.aetheris.yuki.predictionengine.predictions.rideable.PredictionEngineRideableNormal;
import cn.aetheris.yuki.predictionengine.predictions.rideable.PredictionEngineRideableWater;
import cn.aetheris.yuki.predictionengine.predictions.rideable.PredictionEngineRideableWaterLegacy;
import cn.aetheris.yuki.protocol.nms.BlockProperties;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

public class MovementTickerLivingVehicle extends MovementTicker {
    Vector3dm movementInput = new Vector3dm();

    public MovementTickerLivingVehicle(PlayerData player) {
        super(player);
    }

    @Override
    public void doWaterMove(float swimSpeed, boolean isFalling, float swimFriction) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
            new PredictionEngineRideableWater(movementInput).guessBestMovement(swimSpeed, player, isFalling, player.gravity, swimFriction);
        } else {
            new PredictionEngineRideableWaterLegacy(movementInput).guessBestMovement(swimSpeed, player, swimFriction);
        }
    }

    @Override
    public void doLavaMove() {
        new PredictionEngineRideableLava(movementInput).guessBestMovement(0.02F, player);
    }

    @Override
    public void doNormalMove(float blockFriction) {
        new PredictionEngineRideableNormal(movementInput).guessBestMovement(BlockProperties.getFrictionInfluencedSpeed(blockFriction, player), player);
    }
}