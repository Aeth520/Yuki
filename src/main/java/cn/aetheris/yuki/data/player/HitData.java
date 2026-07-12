package cn.aetheris.yuki.data.player;

import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public final class HitData {
    private final Vector3i position;
    private final Vector3dm blockHitLocation;
    private final BlockFace closestDirection;
    private final WrappedBlockState state;

    public HitData(
            Vector3i position,
            Vector3dm blockHitLocation,
            BlockFace closestDirection,
            WrappedBlockState state
    ) {
        this.position = position;
        this.blockHitLocation = blockHitLocation;
        this.closestDirection = closestDirection;
        this.state = state;
    }

    public Vector3d getRelativeBlockHitLocation() {
        return new Vector3d(blockHitLocation.getX() - position.getX(), blockHitLocation.getY() - position.getY(), blockHitLocation.getZ() - position.getZ());
    }

    public Vector3i position() {
        return position;
    }

    public Vector3dm blockHitLocation() {
        return blockHitLocation;
    }

    public BlockFace closestDirection() {
        return closestDirection;
    }

    public WrappedBlockState state() {
        return state;
    }
}
