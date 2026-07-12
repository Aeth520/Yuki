package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(name = "AimF", type = CheckType.AIM, configName = "AimF", decay = 0.65, setback = 5, experimental = true)
public final class AimF extends Check implements RotationCheck {

    private double minDeltaX;
    private double maxDeltaXAccel;
    private int maxBuffer;
    private double maxRotationAngle;

    public AimF(PlayerData player) {
        super(player);
        buffer = 0;
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        double deltaX = rotationUpdate.getDeltaXRotABS();
        double deltaXAccel = rotationUpdate.getProcessor().getYawAccel();

        if (!(Math.abs(rotationUpdate.getTo().getPitch()) < 90)) {
            return;
        }

        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE)) {
            return;
        }

        if (rotationUpdate.isCinematic2()) {
            return;
        }

        double rotationAngle = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(rotationUpdate.getDeltaYRotABS(), 2));

        double gcd = MathUtil.gcd(rotationAngle, maxRotationAngle);
        rotationAngle = rotationAngle / gcd;

        if (player.hasAttackedSince(150L)) {
            if (rotationAngle < maxRotationAngle && deltaXAccel < maxDeltaXAccel && deltaX > minDeltaX) {
                if (buffer++ > maxBuffer) {
                    if (flagAndAlert("angle= " + rotationAngle + "\ndeltaX= " + deltaX)) {
                        if (isAboveSetbackVl()) {
                            player.mitigateDamage();
                        }
                    }
                }
            }
        } else {
            rewardBufferAndVL();
        }
    }

    @Override
    public void reload() {
        super.reload();
        maxBuffer = getConfig().getIntElse(getConfigName() + ".buffer", 7);
        minDeltaX = getConfig().getDoubleElse(getConfigName() + ".min-deltaX", 0.4D);
        maxDeltaXAccel = getConfig().getDoubleElse(getConfigName() + ".max-deltaXAccel", 0.1D);
        maxRotationAngle = getConfig().getDoubleElse(getConfigName() + ".max-rotation-angle", 5.0D);
    }
}
