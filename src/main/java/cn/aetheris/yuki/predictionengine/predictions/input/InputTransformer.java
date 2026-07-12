package cn.aetheris.yuki.predictionengine.predictions.input;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.protocol.nms.vec.Vec2;
import cn.aetheris.yuki.math.vector.Vector3dm;

/**
 * Transforms raw player input into movement vectors.
 * Different Minecraft versions use different input precision (float vs double)
 * and normalization strategies.
 */
public interface InputTransformer {
    /**
     * Transform raw theoretical input into best possible movement vector.
     */
    Vector3dm transform(PlayerData player, Vector3dm theoreticalInput);

    /**
     * Modify input vector based on player state (sneaking, using item, etc.)
     */
    Vec2 modifyInput(PlayerData player, Vec2 input);
}
