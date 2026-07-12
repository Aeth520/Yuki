package cn.aetheris.yuki.data.movement;

import cn.aetheris.yuki.math.vector.Vector3dm;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SetBackData {
    TeleportData teleportData;
    float yaw, pitch;
    Vector3dm velocity;
    boolean vehicle;
    boolean isComplete = false;
    
    boolean isPlugin = false;
    int ticksComplete = 0;

    public SetBackData(TeleportData teleportData, float yaw, float pitch, Vector3dm velocity, boolean vehicle, boolean isPlugin) {
        this.teleportData = teleportData;
        this.yaw = yaw;
        this.pitch = pitch;
        this.velocity = velocity;
        this.vehicle = vehicle;
        this.isPlugin = isPlugin;
    }

    public void tick() {
        if (isComplete) ticksComplete++;
    }
}
