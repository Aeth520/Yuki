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

@CheckData(name = "AimI",
        configName = "AimI",
        description = "invalid rotation gcd change",
        type = CheckType.AIM,
        experimental = true,
        setback = 8,
        decay = 0.95)
public final class AimI extends Check implements RotationCheck {


    public AimI(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        if (!player.hasAttackedSince(1000L)) {
            return;
        }

        final RotateProcessor processor = update.getProcessor();

        if (update.isCinematic2()) return;

        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE) || player.packetStateData.horseInteractCausedForcedRotation
                || !player.isMoving()) {
            return;
        }

        final float deltaYaw = processor.getDeltaYaw() % 360F;
        final float deltaPitch = processor.getDeltaPitch();

        final float lastDeltaYaw = processor.getLastDeltaYaw() % 360F;
        final float lastDeltaPitch = processor.getLastDeltaPitch();

        final double divisorYaw = MathUtil.getGcd((long) (deltaYaw * MathUtil.EXPANDER), (long) (lastDeltaYaw * MathUtil.EXPANDER));
        final double divisorPitch = MathUtil.getGcd((long) (deltaPitch * MathUtil.EXPANDER), (long) (lastDeltaPitch * MathUtil.EXPANDER));

        final double constantYaw = divisorYaw / MathUtil.EXPANDER;
        final double constantPitch = divisorPitch / MathUtil.EXPANDER;

        final double currentX = deltaYaw / constantYaw;
        final double currentY = deltaPitch / constantPitch;

        final double previousX = lastDeltaYaw / constantYaw;
        final double previousY = lastDeltaPitch / constantPitch;

        if (deltaYaw > 0.0 && deltaPitch > 0.0 && deltaYaw < 20.f && deltaPitch < 20.f) {
            final double moduloX = currentX % previousX;
            final double moduloY = currentY % previousY;

            final double floorModuloX = Math.abs(Math.floor(moduloX) - moduloX);
            final double floorModuloY = Math.abs(Math.floor(moduloY) - moduloY);

            final boolean invalidX = moduloX > 90.d && floorModuloX > 0.1;
            final boolean invalidY = moduloY > 90.d && floorModuloY > 0.1;

            final String info = String.format(
                    "mx= %.2f\nmy= %.2f\nfmx= %.2f\nfmy= %.2f",
                    moduloX, moduloY, floorModuloX, floorModuloY
            );

            if (invalidX && invalidY) {
                if (buffer++ > 6) {
                    if (flagAndAlert(info)) {
                        player.mitigateDamage();
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}
