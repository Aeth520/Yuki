package cn.aetheris.yuki.predictionengine.predictions.input;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.vec.Vec2;
import cn.aetheris.yuki.math.vector.Vector3dm;

/**
 * Input transformer for Minecraft versions below 1.21.5.
 * Uses float-precision clamping via {@code Math.round} and the legacy
 * 0.98F multiplier applied directly to the input vector. State-based
 * modifiers (slowMovement, isSlowedByUsingItem) are applied inline rather
 * than through {@code modifyInput}.
 */
public class FloatInputTransformer implements InputTransformer {

    @Override
    public Vector3dm transform(PlayerData player, Vector3dm theoreticalInput) {
        float bestPossibleX;
        float bestPossibleZ;


        if (player.isSlowMovement) {
            bestPossibleX = (float) (theoreticalInput.getX() * player.sneakingSpeedMultiplier);
            bestPossibleZ = (float) (theoreticalInput.getZ() * player.sneakingSpeedMultiplier);
        } else {
            bestPossibleX = Math.min(Math.max(-1f, Math.round(theoreticalInput.getX())), 1f);
            bestPossibleZ = Math.min(Math.max(-1f, Math.round(theoreticalInput.getZ())), 1f);
        }

        if (player.packetStateData.isSlowedByUsingItem()) {
            bestPossibleX *= 0.2F;
            bestPossibleZ *= 0.2F;
        }

        Vector3dm inputVector = new Vector3dm(bestPossibleX, 0, bestPossibleZ);
        inputVector.multiply(0.98F);


        inputVector = new Vector3dm((float) inputVector.getX(), (float) inputVector.getY(), (float) inputVector.getZ());

        if (inputVector.lengthSquared() > 1) {
            double d0 = Math.sqrt(inputVector.getX() * inputVector.getX() + inputVector.getY() * inputVector.getY() + inputVector.getZ() * inputVector.getZ());
            inputVector = new Vector3dm(inputVector.getX() / d0, inputVector.getY() / d0, inputVector.getZ() / d0);
        }

        return inputVector;
    }

    @Override
    public Vec2 modifyInput(PlayerData player, Vec2 input) {
        // The float path does not use modifyInput internally. Delegate to the
        // shared modern logic so external callers receive consistent behaviour.
        return ModernInputTransformer.doModifyInput(player, input);
    }
}
