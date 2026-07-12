package cn.aetheris.yuki.check.util.rotation.entity;

import cn.aetheris.yuki.math.MathUtil;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Rotation {
    float yaw, pitch;
    long timestamp;

    public Rotation(float yaw, float pitch) {
        this.yaw = MathUtil.wrapAngleTo180_float(yaw);
        this.pitch = Math.max(Math.min(pitch, 90), -90);
        this.timestamp = System.currentTimeMillis();
    }

    public Rotation(float[] rots) {
        this.yaw = MathUtil.wrapAngleTo180_float(rots[0]);
        this.pitch = Math.max(Math.min(rots[1], 90), -90);
        this.timestamp = System.currentTimeMillis();
    }

    
    public static float getAngleDifference(final float a, final float b) {
        return MathUtil.wrapAngleTo180_float(MathUtil.wrapAngleTo180_float(a) - MathUtil.wrapAngleTo180_float(b));
    }

    
    public static Rotation limitAngleChange(final Rotation currentRotation, final Rotation targetRotation, final float turnSpeed) {
        final float yawDifference = getAngleDifference(targetRotation.getYaw(), currentRotation.getYaw());
        final float pitchDifference = getAngleDifference(targetRotation.getPitch(), currentRotation.getPitch());

        return new Rotation(
                currentRotation.getYaw() + (yawDifference > turnSpeed ? turnSpeed : Math.max(yawDifference, -turnSpeed)),
                currentRotation.getPitch() + (pitchDifference > turnSpeed ? turnSpeed : Math.max(pitchDifference, -turnSpeed))
        );
    }

    
    public static float limitAngleChange(float current, float intended,
                                         float maxChange) {
        maxChange = Math.abs(maxChange);
        float currentWrapped = MathUtil.wrapAngleTo180_float(current);
        float intendedWrapped = MathUtil.wrapAngleTo180_float(intended);

        float change = MathUtil.wrapAngleTo180_float(intendedWrapped - currentWrapped);
        change = MathUtil.clamp(change, -maxChange, maxChange);

        return MathUtil.wrapAngleTo180_float(currentWrapped + change);
    }

    public static Rotation wrapped(float yaw, float pitch) {
        return new Rotation(MathUtil.wrapAngleTo180_float(yaw),
                MathUtil.wrapAngleTo180_float(pitch));
    }

    public double getAngleTo(Rotation other) {
        float yaw1 = MathUtil.wrapAngleTo180_float(yaw);
        float yaw2 = MathUtil.wrapAngleTo180_float(other.yaw);
        float diffYaw = MathUtil.wrapAngleTo180_float(yaw1 - yaw2);

        float pitch1 = MathUtil.wrapAngleTo180_float(pitch);
        float pitch2 = MathUtil.wrapAngleTo180_float(other.pitch);
        float diffPitch = MathUtil.wrapAngleTo180_float(pitch1 - pitch2);

        return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
    }

    public Rotation withYaw(float yaw) {
        return new Rotation(yaw, pitch);
    }

    public Rotation withPitch(float pitch) {
        return new Rotation(yaw, pitch);
    }

    public Rotation multi(float multiplier) {
        yaw *= multiplier;
        pitch *= multiplier;
        return this;
    }
}