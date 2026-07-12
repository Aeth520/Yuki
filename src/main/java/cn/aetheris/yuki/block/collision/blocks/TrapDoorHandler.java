package cn.aetheris.yuki.block.collision.blocks;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.datatypes.CollisionBox;
import cn.aetheris.yuki.block.collision.datatypes.CollisionFactory;
import cn.aetheris.yuki.block.collision.datatypes.NoCollisionBox;
import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Half;

public final class TrapDoorHandler implements CollisionFactory {
    @Override
    public CollisionBox fetch(PlayerData player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        double var2 = 0.1875;

        if (block.isOpen()) {
            switch (block.getFacing()) {
                case SOUTH -> {
                    return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, var2, false);
                }
                case NORTH -> {
                    return new SimpleCollisionBox(0.0, 0.0, 1.0 - var2, 1.0, 1.0, 1.0, false);
                }
                case EAST -> {
                    return new SimpleCollisionBox(0.0, 0.0, 0.0, var2, 1.0, 1.0, false);
                }
                case WEST -> {
                    return new SimpleCollisionBox(1.0 - var2, 0.0, 0.0, 1.0, 1.0, 1.0, false);
                }
            }
        } else {
            if (block.getHalf() == Half.BOTTOM) {
                return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, var2, 1.0, false);
            } else {
                return new SimpleCollisionBox(0.0, 1.0 - var2, 0.0, 1.0, 1.0, 1.0, false);

            }
        }

        return NoCollisionBox.INSTANCE;
    }
}
