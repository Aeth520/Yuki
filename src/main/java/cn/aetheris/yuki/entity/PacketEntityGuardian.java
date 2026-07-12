package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;

import java.util.UUID;

public final class PacketEntityGuardian extends PacketEntity {
    
    
    public boolean isElder;

    public PacketEntityGuardian(PlayerData player, UUID uuid, EntityType type, double x, double y, double z, boolean isElder) {
        super(player, uuid, type, x, y, z);
        this.isElder = isElder;
    }
}