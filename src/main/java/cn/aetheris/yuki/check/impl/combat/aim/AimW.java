package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;
import org.jetbrains.annotations.NotNull;

/**
 * Absolute sensitivity-grid rotation validation (Karhu-style).
 *
 * <p>Vanilla mouse rotation deltas are always integer multiples of the
 * sensitivity quantum: {@code step = ((s * 0.6 + 0.2)^3 * 8) * 0.15} degrees,
 * because the same fixed camera offset cancels out in deltas. Once the
 * player's sensitivity has been reverse-engineered by RotateProcessor, every
 * mouse-driven rotation delta must lie on that grid.</p>
 *
 * <p>Unlike AimI (relative GCD between consecutive deltas), this validates
 * against the <b>absolute</b> grid, catching aimbots whose rotations are
 * internally consistent but never multiples of any real sensitivity quantum
 * (e.g. smooth/interpolated aimbots).</p>
 */
@CheckData(
        name = "AimW",
        description = "Rotation delta not aligned to the reverse-engineered sensitivity grid",
        configName = "AimW",
        type = CheckType.AIM,
        decay = 0.82,
        experimental = true
)
public final class AimW extends Check implements RotationCheck {

    private double buffer;
    private double lastSensitivityMcp = -1;

    public AimW(PlayerData player) {
        super(player);
    }

    @Override
    public void process(@NotNull RotationUpdate u) {
        final RotateProcessor processor = u.getProcessor();

        // Sensitivity must be reverse-engineered with confidence
        final double sensitivityMcp = processor.totalSensitivity;
        if (sensitivityMcp <= 0) {
            buffer = Math.max(0, buffer - 0.5);
            return;
        }
        // Grid re-learned: reset evidence built against the old grid
        if (lastSensitivityMcp > 0 && Math.abs(sensitivityMcp - lastSensitivityMcp) > 1e-6) {
            buffer = 0;
        }
        lastSensitivityMcp = sensitivityMcp;

        // Gates identical to the other rotation checks
        if (updateIsInvalid(u)) {
            buffer = Math.max(0, buffer - 0.25);
            return;
        }

        final float deltaPitch = Math.abs(processor.deltaPitch);
        final float deltaYaw = Math.abs(processor.deltaYaw % 360F);

        // One mouse-count rotation quantum for this sensitivity
        final double step = MathUtil.getGCDValue(sensitivityMcp);
        // Below one quantum the grid carries no information
        final double minDelta = Math.max(step, MathUtil.MINIMUM_DIVISOR);
        if (deltaPitch < minDelta && deltaYaw < minDelta) {
            return;
        }

        // Tolerances: float yaw storage + rounding of the strict sample matching
        final double eps = Math.max(1.0E-3, step * 0.02);

        boolean pitchOff = false;
        boolean yawOff = false;
        if (deltaPitch >= minDelta) {
            pitchOff = !isOnGrid(deltaPitch, step, eps);
        }
        if (deltaYaw >= minDelta) {
            yawOff = !isOnGrid(deltaYaw, step, eps);
        }

        if (pitchOff && yawOff) {
            buffer += 1;
            if (buffer > 8) {
                if (flagAndAlert(String.format(
                        "step= %.5f dYaw= %.4f dPitch= %.4f sens= %.4f",
                        step, deltaYaw, deltaPitch, sensitivityMcp))) {
                    player.mitigateDamage();
                    buffer = 5; // keep some pressure after a flag
                }
            }
        } else {
            rewardBufferAndVL();
            buffer = Math.max(0, buffer - 0.75);
        }
    }

    /**
     * A vanilla mouse delta must satisfy {@code |delta - round(delta / step) * step| < eps}
     * (near 0 or near step after modulo).
     */
    private static boolean isOnGrid(double delta, double step, double eps) {
        final double remainder = delta % step;
        return remainder < eps || (step - remainder) < eps;
    }

    private boolean updateIsInvalid(RotationUpdate u) {
        if (u.isCinematic2()) return true;
        if (Math.abs(u.getTo().getPitch()) >= 89.9F) return true;
        if (!player.hasAttackedSince(1000L)) return true;
        if (!player.isMoving()) return true;
        return isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE);
    }
}
