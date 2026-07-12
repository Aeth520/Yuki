package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.data.MainSupportingBlockData;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class MainSupportingBlockPosFinder {

    public static MainSupportingBlockData findMainSupportingBlockPos(PlayerData player, MainSupportingBlockData lastSupportingBlock, Vector3d lastMovement, SimpleCollisionBox maxPose, boolean isOnGround) {
        if (!isOnGround) {
            return new MainSupportingBlockData(null, false);
        }

        SimpleCollisionBox slightlyBelowPlayer = new SimpleCollisionBox(maxPose.minX, maxPose.minY - 1.0E-6D, maxPose.minZ, maxPose.maxX, maxPose.minY, maxPose.maxZ);

        Optional<Vector3i> supportingBlock = findSupportingBlock(player, slightlyBelowPlayer);
        if (supportingBlock.isEmpty() && (!lastSupportingBlock.lastOnGroundAndNoBlock())) {
            if (lastMovement != null) {
                SimpleCollisionBox aabb2 = slightlyBelowPlayer.offset(-lastMovement.x, 0.0D, -lastMovement.z);
                supportingBlock = findSupportingBlock(player, aabb2);
                return new MainSupportingBlockData(supportingBlock.orElse(null), true);
            }
        } else {
            return new MainSupportingBlockData(supportingBlock.orElse(null), true);
        }

        return new MainSupportingBlockData(null, true);
    }

    private static Optional<Vector3i> findSupportingBlock(PlayerData player, SimpleCollisionBox searchBox) {
        Vector3d playerPos = new Vector3d(player.x, player.y, player.z);

        AtomicReference<Vector3i> bestBlockPos = new AtomicReference<>();
        AtomicDouble blockPosDistance = new AtomicDouble(Double.MAX_VALUE);

        Collisions.forEachCollisionBox(player, searchBox, (pos) -> {
            Vector3i blockPos = pos.toVector3i();

            Vector3d blockPosAsVector3d = new Vector3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
            double distance = playerPos.distanceSquared(blockPosAsVector3d);

            if (distance < blockPosDistance.get() || distance == blockPosDistance.get() && (bestBlockPos.get() == null || firstHasPriorityOverSecond(blockPos, bestBlockPos.get()))) {
                bestBlockPos.set(blockPos);
                blockPosDistance.set(distance);
            }
        });


        return Optional.ofNullable(bestBlockPos.get());
    }

    private static boolean firstHasPriorityOverSecond(Vector3i first, Vector3i second) {
        
        
        
        
        
        
        
        
        
        
        
        
        if (first.getY() < second.getY()) return true;

        double sumX = second.getX() - first.getX();
        double sumY = second.getZ() - first.getZ();

        double horizontalSumTotal = sumX + sumY;
        if (horizontalSumTotal == 0) {
            
            return sumX < 0;
        }

        
        return horizontalSumTotal < 0;
    }
}
