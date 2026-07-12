package cn.aetheris.yuki.util.update;

import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.player.HeadRotation;
import cn.aetheris.yuki.protocol.nms.vec.Vec2f;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class RotationUpdate {
    private final PlayerData data;
    private HeadRotation from, to;
    private RotateProcessor processor;
    private boolean isCinematic;
    private boolean isCinematic2;
    private float deltaPitch, deltaYaw;

    public RotationUpdate(PlayerData data
            , HeadRotation from, HeadRotation to, float deltaXRot, float deltaPitch) {
        this.data = data;
        this.from = from;
        this.to = to;
        this.deltaYaw = deltaXRot;
        this.deltaPitch = deltaPitch;
    }

    public float getDeltaXRotABS() {
        return Math.abs(deltaYaw);
    }

    public float getDeltaYRotABS() {
        return Math.abs(deltaPitch);
    }

    public Vec2f getDelta() {
        return new Vec2f(to.getYaw() - from.getYaw(), to.getPitch() - from.getPitch());
    }

    public long getTick() {
        return data.getLastFlying() / 50;
    }
}
