package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;

import java.util.UUID;

public final class PacketEntityArmorStand extends PacketEntity {

    public boolean isMarker = false;

    public PacketEntityArmorStand(PlayerData player, UUID uuid, EntityType type, double x, double y, double z, int extraData) {
        super(player, uuid, type, x, y, z);
    }

    @Override
    public boolean canHit() {
        return !isMarker && super.canHit();
    }
}