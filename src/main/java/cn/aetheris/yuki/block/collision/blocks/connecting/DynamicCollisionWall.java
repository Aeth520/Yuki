package cn.aetheris.yuki.block.collision.blocks.connecting;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.block.collision.CollisionData;
import cn.aetheris.yuki.block.collision.datatypes.*;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.East;
import com.github.retrooper.packetevents.protocol.world.states.enums.North;
import com.github.retrooper.packetevents.protocol.world.states.enums.South;
import com.github.retrooper.packetevents.protocol.world.states.enums.West;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;

public final class DynamicCollisionWall extends DynamicConnecting implements CollisionFactory {
    
    
    private final CollisionBox[] COLLISION_BOXES = makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, false, 1);
    private final boolean isNewServer = Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThan(ServerVersion.V_1_12_2);


    
    @Override
    public CollisionBox fetch(PlayerData player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        boolean isNewClient = version.isNewerThan(ClientVersion.V_1_12_2);

        
        if (isNewServer && isNewClient) {
            boolean north = block.getNorth() != North.NONE;
            boolean south = block.getSouth() != South.NONE;
            boolean west = block.getWest() != West.NONE;
            boolean east = block.getEast() != East.NONE;

            return block.isUp()
                    ? COLLISION_BOXES[getAABBIndex(north, east, south, west)].copy().union(new HexCollisionBox(4, 0, 4, 12, 24, 12))
                    : COLLISION_BOXES[getAABBIndex(north, east, south, west)].copy();
        }

        
        boolean north = isNewServer ? block.getNorth() != North.NONE : connectsTo(player, version, x, y, z, BlockFace.NORTH);
        boolean south = isNewServer ? block.getSouth() != South.NONE : connectsTo(player, version, x, y, z, BlockFace.SOUTH);
        boolean west = isNewServer ? block.getWest() != West.NONE : connectsTo(player, version, x, y, z, BlockFace.WEST);
        boolean east = isNewServer ? block.getEast() != East.NONE : connectsTo(player, version, x, y, z, BlockFace.EAST);

        
        if (!isNewServer && isNewClient) {
            boolean up = connectsTo(player, version, x, y, z, BlockFace.UP);

            if (!up) {
                WrappedBlockState currBlock = player.compensatedWorld.getBlock(x, y, z);
                StateType currType = currBlock.getType();

                boolean selfNorth = currType == player.compensatedWorld.getBlock(x, y, z + 1).getType();
                boolean selfSouth = currType == player.compensatedWorld.getBlock(x, y, z - 1).getType();
                boolean selfWest = currType == player.compensatedWorld.getBlock(x - 1, y, z).getType();
                boolean selfEast = currType == player.compensatedWorld.getBlock(x + 1, y, z).getType();

                up = (!selfNorth || !selfSouth || selfWest || selfEast) &&
                        (!selfWest || !selfEast || selfNorth || selfSouth);
                return up
                        ? COLLISION_BOXES[getAABBIndex(north, east, south, west)].copy().union(new HexCollisionBox(4, 0, 4, 12, 24, 12))
                        : COLLISION_BOXES[getAABBIndex(north, east, south, west)].copy();
            }
        }

        
        float f = 0.25F;
        float f1 = 0.75F;
        float f2 = 0.25F;
        float f3 = 0.75F;

        if (north) f2 = 0.0F;
        if (south) f3 = 1.0F;
        if (west) f = 0.0F;
        if (east) f1 = 1.0F;

        if (north && south && !west && !east) {
            f = 0.3125F;
            f1 = 0.6875F;
        } else if (!north && !south && west && east) {
            f2 = 0.3125F;
            f3 = 0.6875F;
        }

        return new SimpleCollisionBox(f, 0.0F, f2, f1, 1.5, f3);
    }

    @Override
    public boolean checkCanConnect(PlayerData player, WrappedBlockState state, StateType one, StateType two, BlockFace direction) {
        return BlockTags.WALLS.contains(one) || CollisionData.getData(one).getMovementCollisionBox(player, player.getClientVersion(), state, 0, 0, 0).isSideFullBlock(direction);
    }
}
