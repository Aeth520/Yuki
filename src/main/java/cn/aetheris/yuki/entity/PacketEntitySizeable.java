package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;

import java.util.UUID;

public final class PacketEntitySizeable extends PacketEntity {
    public int size = 1; 


    public PacketEntitySizeable(PlayerData player, UUID uuid, EntityType type, double x, double y, double z) {
        super(player, uuid, type, x, y, z);
    }
}
