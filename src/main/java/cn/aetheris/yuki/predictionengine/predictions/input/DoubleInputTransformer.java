package cn.aetheris.yuki.predictionengine.predictions.input;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.protocol.nms.vec.Vec2;
import cn.aetheris.yuki.math.vector.Vector3dm;

/**
 * Input transformer reserved for future Minecraft versions that switch to
 * double-precision input. Current behaviour mirrors {@link ModernInputTransformer}
 * (Vec2 is still float-backed) but this class is the extension point for
 * double-precision movement math when it becomes necessary.
 */
public class DoubleInputTransformer implements InputTransformer {

    @Override
    public Vector3dm transform(PlayerData player, Vector3dm theoreticalInput) {
        Vec2 moveVector = new Vec2((float) theoreticalInput.getX(), (float) theoreticalInput.getZ()).normalized();
        Vec2 input = modifyInput(player, moveVector);
        return new Vector3dm(input.x(), 0, input.y());
    }

    @Override
    public Vec2 modifyInput(PlayerData player, Vec2 input) {
        return ModernInputTransformer.doModifyInput(player, input);
    }
}
