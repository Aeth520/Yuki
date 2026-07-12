package cn.aetheris.yuki.check.impl.movement.elytra;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.protocol.nms.Collisions;
import cn.aetheris.yuki.util.update.PredictionComplete;
import cn.aetheris.yuki.math.vector.Vector3dm;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

@CheckData(name = "ElytraJ (DOWN)", configName = "ElytraJ", type = CheckType.ELYTRA, description = "client flies up looking down", decay = 0.46, setback = 9, experimental = true)
public final class ElytraJ extends Check implements PostPredictionCheck {

    public ElytraJ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (player.pitch > -30) {
            ObjectArrayList<SimpleCollisionBox> boxes = new ObjectArrayList<>(9);
            Collisions.getCollisionBoxes(player, player.boundingBox, boxes, false);

            if (isRising(player.predictedVelocity.vector, boxes)) {
                if (flagAndAlert("")) {
                    setbackIfAboveSetbackVL();
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }

    private boolean isRising(Vector3dm vector, ObjectArrayList<SimpleCollisionBox> boxes) {
        for (SimpleCollisionBox box : boxes) {
            if (box.isIntersected(player.boundingBox)) {
                return false;
            }
        }
        return player.isGliding && player.compensatedFireworks.getMaxFireworksAppliedPossible() > 0 && vector.getY() > 1.6 && vector.getY() > vector.clone().setY(0).length();
    }
}