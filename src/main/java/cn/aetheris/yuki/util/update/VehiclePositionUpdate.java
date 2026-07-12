package cn.aetheris.yuki.util.update;

import com.github.retrooper.packetevents.util.Vector3d;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public final class VehiclePositionUpdate {
    private final Vector3d from, to;
    private final float xRot, yRot;
    private final boolean isTeleport;
}
