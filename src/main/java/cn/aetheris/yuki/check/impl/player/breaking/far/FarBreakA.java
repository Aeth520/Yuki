package cn.aetheris.yuki.check.impl.player.breaking.far;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.math.VectorUtils;
import cn.aetheris.yuki.util.update.BlockBreak;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;

@CheckData(name = "FarBreakA", configName = "FarBreakA", decay = 0.5, type = CheckType.BREAK)
public final class FarBreakA extends Check implements BlockBreakCheck {
    public FarBreakA(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        
        if (isExempt(ExemptType.TELEPORT, ExemptType.VEHICLE, ExemptType.DIED, ExemptType.FLYING, ExemptType.CLIENT_ANTICHEAT) || blockBreak.action == DiggingAction.CANCELLED_DIGGING)
            return; 

        double min = Double.MAX_VALUE;
        final double[] possibleEyeHeights = player.getPossibleEyeHeights();
        for (double d : possibleEyeHeights) {
            SimpleCollisionBox box = new SimpleCollisionBox(blockBreak.position);
            Vector3dm eyes = new Vector3dm(player.x, player.y + d, player.z);
            Vector3dm best = VectorUtils.cutBoxToVector(eyes, box);
            min = Math.min(min, eyes.distanceSquared(best));
        }

        double maxReach = player.compensatedEntities.getSelf().getAttributeValue(Attributes.PLAYER_BLOCK_INTERACTION_RANGE);
        double threshold = player.getMovementThreshold();
        maxReach += Math.hypot(threshold, threshold);

        if (player.packetStateData.didLastMovementIncludePosition || player.canSkipTicks()) {
            if (min > maxReach * maxReach) {
                if (flagAndAlert("d= " + Math.sqrt(min) + "\nm= " + maxReach) && shouldModifyPackets()) {
                    blockBreak.cancel();
                }
            }
        }
    }
}