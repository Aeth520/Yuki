package cn.aetheris.yuki.predictionengine.predictions.input;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.vec.Vec2;
import cn.aetheris.yuki.math.vector.Vector3dm;

/**
 * Input transformer for Minecraft 1.21.5+.
 * Uses precise Vec2 normalization and the modern modifyInput pipeline
 * instead of the legacy Math.round-based float clamping.
 */
public class ModernInputTransformer implements InputTransformer {

    @Override
    public Vector3dm transform(PlayerData player, Vector3dm theoreticalInput) {
        Vec2 moveVector = new Vec2((float) theoreticalInput.getX(), (float) theoreticalInput.getZ()).normalized();
        Vec2 input = modifyInput(player, moveVector);
        return new Vector3dm(input.x(), 0, input.y());
    }

    @Override
    public Vec2 modifyInput(PlayerData player, Vec2 moveVector) {
        return doModifyInput(player, moveVector);
    }

    /**
     * Shared static implementation of the modern modifyInput logic.
     * Reused by {@link FloatInputTransformer} and {@link DoubleInputTransformer}
     * so that external callers (e.g. {@code PacketPlayerSteer}) get identical
     * behaviour regardless of the version-specific transformer selected.
     */
    static Vec2 doModifyInput(PlayerData player, Vec2 moveVector) {
        if (moveVector.lengthSquared() == 0.0F) {
            return moveVector;
        } else {
            Vec2 input = moveVector.scale(0.98F);
            if (player.packetStateData.isSlowedByUsingItem() && !player.inVehicle()) {
                input = input.scale(0.2F);
            }

            if (player.isSlowMovement) {
                input = input.scale(player.sneakingSpeedMultiplier);
            }

            return modifyInputSpeedForSquareMovement(input);
        }
    }

    private static Vec2 modifyInputSpeedForSquareMovement(Vec2 input) {
        float length = input.length();
        if (length <= 0.0F) {
            return input;
        } else {
            Vec2 multiplied = input.scale(1.0F / length);
            float distance = distanceToUnitSquare(multiplied);
            float min = Math.min(length * distance, 1.0F);
            return multiplied.scale(min);
        }
    }

    private static float distanceToUnitSquare(Vec2 input) {
        float x = Math.abs(input.x());
        float z = Math.abs(input.y());
        float additional = z > x ? x / z : z / x;
        return MathUtil.sqrt(1.0F + MathUtil.square(additional));
    }
}
