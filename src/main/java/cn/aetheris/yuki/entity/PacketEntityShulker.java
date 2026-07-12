package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;

import java.util.UUID;

public final class PacketEntityShulker extends PacketEntity {

    public BlockFace facing = BlockFace.DOWN;

    public PacketEntityShulker(PlayerData player, UUID uuid, EntityType type, double x, double y, double z) {
        super(player, uuid, type, x, y, z);
    }
}