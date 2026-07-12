package cn.aetheris.yuki.util.change;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Data;


@Data
public final class BlockModification {
    private final WrappedBlockState oldBlockContents;
    private final WrappedBlockState newBlockContents;
    private final Vector3i location;
    private final int tick;
    private final Cause cause;

    
    public BlockModification(WrappedBlockState oldBlockContents, WrappedBlockState newBlockContents,
                             Vector3i location, int tick, Cause cause) {
        this.oldBlockContents = oldBlockContents;
        this.newBlockContents = newBlockContents;
        this.location = location;
        this.tick = tick;
        this.cause = cause;
    }

    public WrappedBlockState oldBlockContents() {
        return oldBlockContents;
    }

    public WrappedBlockState newBlockContents() {
        return newBlockContents;
    }

    public Vector3i location() {
        return location;
    }

    public int tick() {
        return tick;
    }

    public Cause cause() {
        return cause;
    }


    public enum Cause {
        START_DIGGING,
        APPLY_BLOCK_CHANGES,
        HANDLE_NETTY_SYNC_TRANSACTION,
        OTHER
    }
}