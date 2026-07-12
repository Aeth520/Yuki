package cn.aetheris.yuki.data.movement;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import lombok.Data;

@Data
public class TrackerData {

    private double x, y, z;
    private float yaw, pitch;
    private EntityType entityType;
    private int lastTransactionHung;
    private int legacyPointEightMountedUpon;

    public TrackerData(double x, double y, double z, float yaw, float pitch, EntityType entityType, int lastTransactionHung) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.entityType = entityType;
        this.lastTransactionHung = lastTransactionHung;
    }
}
