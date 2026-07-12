package cn.aetheris.yuki.predictionengine.predictions.rideable;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.predictionengine.predictions.PredictionEngineNormal;
import cn.aetheris.yuki.data.VectorData;
import cn.aetheris.yuki.math.vector.Vector3dm;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class PredictionEngineHappyGhast extends PredictionEngineNormal {
    private final Vector3dm movementVector;
    private final double multiplier;

    @Override
    public void endOfTick(PlayerData player, double delta) {
        for (VectorData vector : player.getPossibleVelocitiesMinusKnockback()) {
            vector.vector.setX(vector.vector.getX() * multiplier);
            vector.vector.setY(vector.vector.getY() * multiplier);
            vector.vector.setZ(vector.vector.getZ() * multiplier);
        }
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(PlayerData player, Set<VectorData> possibleVectors, float speed) {
        return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(this, movementVector, player, possibleVectors, speed);
    }

    @Override
    public Vector3dm getMovementResultFromInput(PlayerData player, Vector3dm inputVector, float flyingSpeed, float pitch) {
        float sin = player.trigHandler.sin(pitch * 0.017453292f);
        float cos = player.trigHandler.cos(pitch * 0.017453292f);

        double xResult = inputVector.getX() * cos - inputVector.getZ() * sin;
        double zResult = inputVector.getZ() * cos + inputVector.getX() * sin;

        return new Vector3dm(xResult * flyingSpeed, inputVector.getY() * flyingSpeed, zResult * flyingSpeed);
    }

}