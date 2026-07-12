package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Thickness;
import com.github.retrooper.packetevents.protocol.world.states.enums.VerticalDirection;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;

public final class Dripstone {

    public static WrappedBlockState update(PlayerData player, WrappedBlockState toPlace, int x, int y, int z, boolean secondaryUse) {
        VerticalDirection currentDirection = toPlace.getVerticalDirection();
        VerticalDirection oppositeDirection = (currentDirection == VerticalDirection.UP) ? VerticalDirection.DOWN : VerticalDirection.UP;

        WrappedBlockState blockBelow = player.compensatedWorld.getBlock(x, y + (currentDirection == VerticalDirection.UP ? 1 : -1), z);

        if (isPointedDripstoneWithDirection(blockBelow, oppositeDirection)) {
            Thickness newThickness = determineThickness(secondaryUse, blockBelow.getThickness());
            toPlace.setThickness(newThickness);
        } else {
            handleNonDripstoneCase(player, toPlace, x, y, z, currentDirection, oppositeDirection);
        }

        return toPlace;
    }

    private static Thickness determineThickness(boolean secondaryUse, Thickness currentThickness) {
        if (secondaryUse && currentThickness != Thickness.TIP_MERGE) {
            return Thickness.TIP;
        }
        return Thickness.TIP_MERGE;
    }

    private static void handleNonDripstoneCase(PlayerData player, WrappedBlockState toPlace, int x, int y, int z, VerticalDirection currentDirection, VerticalDirection oppositeDirection) {
        WrappedBlockState blockBelow = player.compensatedWorld.getBlock(x, y + (oppositeDirection == VerticalDirection.UP ? 1 : -1), z);
        Thickness currentThickness = blockBelow.getThickness();

        if (currentThickness == Thickness.TIP || currentThickness == Thickness.TIP_MERGE) {
            toPlace.setThickness(Thickness.FRUSTUM);
        } else if (!isPointedDripstoneWithDirection(blockBelow, currentDirection)) {
            toPlace.setThickness(Thickness.TIP);
        } else {
            toPlace.setThickness(Thickness.BASE);
        }
    }

    private static boolean isPointedDripstoneWithDirection(WrappedBlockState blockState, VerticalDirection direction) {
        return blockState.getType() == StateTypes.POINTED_DRIPSTONE && blockState.getVerticalDirection() == direction;
    }
}