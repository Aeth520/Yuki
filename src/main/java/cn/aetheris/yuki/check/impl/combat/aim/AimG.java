package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(name = "AimG",
        configName = "AimG",
        decay = 0.86,
        setback = 4,
        type = CheckType.AIM)
public final class AimG extends Check implements RotationCheck {

    int maxBuffer;
    double minDeltaY;
    double maxDeltaYAccel;
    double lastDeltaY;

    public AimG(PlayerData player) {
        super(player);
        lastDeltaY = 0;
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.hasAttackedSince(150L)) {

            double deltaYAccel = rotationUpdate.getProcessor().getPitchAccel();
            double deltaPitch = rotationUpdate.getProcessor().getDeltaPitch();

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

            if (deltaYAccel < maxDeltaYAccel && deltaPitch > minDeltaY) {
                if (buffer++ > maxBuffer) {
                    if (flagAndAlert("dya= " + deltaYAccel
                            + "\ndy= " + deltaPitch)) {
                        buffer = 0;
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
        minDeltaY = getConfig().getDoubleElse(getConfigName() + ".min-deltaY", 0.4D);
        maxDeltaYAccel = getConfig().getDoubleElse(getConfigName() + ".max-deltaY-accel", 0.1D);
    }
}