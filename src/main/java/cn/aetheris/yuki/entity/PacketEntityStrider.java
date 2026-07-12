package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;

import java.util.UUID;

public final class PacketEntityStrider extends PacketEntityRideable {
    public boolean isShaking = false;

    public PacketEntityStrider(PlayerData player, UUID uuid, EntityType type, double x, double y, double z) {
        super(player, uuid, type, x, y, z);
    }
}
