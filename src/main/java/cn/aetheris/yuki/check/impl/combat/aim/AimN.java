package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.HeadRotation;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(name = "AimN", configName = "AimN", type = CheckType.AIM, description = "Check for Consistent rotate", decay = 0.55)
public final class AimN extends Check implements RotationCheck {
    private float lastDeltaPitch;
    private float lastDeltaYaw;
    private int streak;

    public AimN(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        final HeadRotation to = rotationUpdate.getTo();
        final HeadRotation from = rotationUpdate.getFrom();
        float deltaPitch = Math.abs(to.getPitch() - from.getPitch());
        float deltaYaw = Math.abs(to.getYaw() - from.getYaw());

        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE)) {
            return;
        }

        if (player.hasAttackedSince(250L)
                && (double) deltaYaw > 0.001
                && deltaYaw <= 5.0F
                && lastDeltaYaw <= 5.0F && Math.abs(to.getPitch()) <= 80.0F) {
            double gcdYAW = MathUtil.getGcd(deltaYaw, lastDeltaYaw);
            if (gcdYAW < 0.009 && !rotationUpdate.isCinematic()) {
                double gcdPITCH = MathUtil.getGcd(deltaPitch, lastDeltaPitch);
                if (deltaPitch > 0.0F && gcdPITCH < 0.009) {
                    streak = 0;
                    buffer = 0.0;
                }

                if (++streak > 20 && lastDeltaPitch == 0.0F && buffer++ > 20.0) {
                    if (flagAndAlert("gcdY= " + gcdYAW + "\ngcdP= " + gcdPITCH)) {
                        player.mitigateDamage();
                        buffer = 0.0;
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }

        lastDeltaPitch = deltaPitch;
        lastDeltaYaw = deltaYaw;
    }
}
