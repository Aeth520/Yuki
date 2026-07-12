package cn.aetheris.yuki.entity;

import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;

import java.util.UUID;


public class PacketEntityTrackYaw extends PacketEntity {
    public float packetYaw;
    public float interpYaw;
    public int steps = 0;

    public PacketEntityTrackYaw(PlayerData player, UUID uuid, EntityType type, double x, double y, double z, float xRot) {
        super(player, uuid, type, x, y, z);
        this.packetYaw = xRot;
        this.interpYaw = xRot;
    }

    @Override
    public void onMovement(boolean highBound) {
        super.onMovement(highBound);
        if (steps > 0) {
            interpYaw = interpYaw + ((packetYaw - interpYaw) / steps--);
        }
    }
}
