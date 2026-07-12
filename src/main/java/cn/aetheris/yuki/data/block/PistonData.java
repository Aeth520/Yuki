package cn.aetheris.yuki.data.block;

import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import com.github.retrooper.packetevents.protocol.world.BlockFace;

import java.util.List;

public final class PistonData {
    public final boolean isPush;
    public final boolean hasSlimeBlock;
    public final boolean hasHoneyBlock;
    public final BlockFace direction;
    public final int lastTransactionSent;

    
    public int ticksOfPistonBeingAlive = 0;

    
    public List<SimpleCollisionBox> boxes;

    public PistonData(BlockFace direction, List<SimpleCollisionBox> pushedBlocks, int lastTransactionSent, boolean isPush, boolean hasSlimeBlock, boolean hasHoneyBlock) {
        this.direction = direction;
        this.boxes = pushedBlocks;
        this.lastTransactionSent = lastTransactionSent;
        this.isPush = isPush;
        this.hasSlimeBlock = hasSlimeBlock;
        this.hasHoneyBlock = hasHoneyBlock;
    }

    
    
    
    public boolean tickIfGuaranteedFinished() {
        return ++ticksOfPistonBeingAlive >= 10;
    }
}
