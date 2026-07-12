package cn.aetheris.yuki.predictionengine.predictions.rideable;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineLava;
import cn.aetheris.yuki.data.movement.VectorData;
import cn.aetheris.yuki.math.vector.Vector3dm;

import java.util.List;
import java.util.Set;

public final class PredictionEngineRideableLava extends PredictionEngineLava {

    Vector3dm movementVector;

    public PredictionEngineRideableLava(Vector3dm movementVector) {
        this.movementVector = movementVector;
    }

    @Override
    public void addJumpsToPossibilities(PlayerData player, Set<VectorData> existingVelocities) {
        PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(PlayerData player, Set<VectorData> possibleVectors, float speed) {
        return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(movementVector, player, possibleVectors, speed);
    }
}
