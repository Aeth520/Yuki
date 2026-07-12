package cn.aetheris.yuki.block.collision.datatypes;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;

public interface HitBoxFactory {
    CollisionBox fetch(PlayerData data, StateType heldItem, ClientVersion version, WrappedBlockState block, boolean isTargetBlock, int x, int y, int z);
}
