package cn.aetheris.yuki.check.impl.player.breaking.position;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;

import java.util.Collections;

@CheckData(name = "PositionBreakA (Action)", type = CheckType.BREAK, configName = "PositionBreakA", decay = 0.25, experimental = true)
public final class PositionBreakA extends Check implements BlockBreakCheck {

    public PositionBreakA(PlayerData player) {
        super(player);
    }


    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (player.inVehicle()
                || blockBreak.action == DiggingAction.CANCELLED_DIGGING
                || blockBreak.action == DiggingAction.START_DIGGING
                || blockBreak.block.getType() == StateTypes.REDSTONE_WIRE
                || blockBreak.block.getType() == StateTypes.PINK_PETALS
                || blockBreak.block.getType().getName().contains("carpet")
                || blockBreak.block.getType().getName().contains("leaf")
                || blockBreak.block.getType().getName().contains("flower")
        ) return;


        if (Collections.max(player.uncertaintyHandler.pistonX) != 0
                || Collections.max(player.uncertaintyHandler.pistonY) != 0
                || Collections.max(player.uncertaintyHandler.pistonZ) != 0) {
            return;
        }

        SimpleCollisionBox combined = blockBreak.getCombinedBox();







        final double[] possibleEyeHeights = player.getPossibleEyeHeights();
        double minEyeHeight = Double.MAX_VALUE;
        double maxEyeHeight = Double.MIN_VALUE;
        for (double height : possibleEyeHeights) {
            minEyeHeight = Math.min(minEyeHeight, height);
            maxEyeHeight = Math.max(maxEyeHeight, height);
        }


        double movementThreshold = !player.packetStateData.didLastMovementIncludePosition || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? player.getMovementThreshold() : 0;

        SimpleCollisionBox eyePositions = new SimpleCollisionBox(player.x, player.y + minEyeHeight, player.z, player.x, player.y + maxEyeHeight, player.z);
        eyePositions.expand(movementThreshold);


        if (eyePositions.isIntersected(combined)) {
            return;
        }


        boolean flag = switch (blockBreak.face) {
            case NORTH -> eyePositions.minZ > combined.minZ;
            case SOUTH -> eyePositions.maxZ < combined.maxZ;
            case EAST -> eyePositions.maxX < combined.maxX;
            case WEST -> eyePositions.minX > combined.minX;
            case UP -> eyePositions.maxY < combined.maxY;
            case DOWN -> eyePositions.minY > combined.minY;
            default -> false;
        };

        if (flag && flagAndAlert("action= " + blockBreak.action)) {
            blockBreak.cancel();
            player.mitigateDamage();
        }
    }
}
