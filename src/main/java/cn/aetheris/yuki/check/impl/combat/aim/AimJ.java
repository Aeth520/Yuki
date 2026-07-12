package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(name = "AimJ", type = CheckType.AIM, configName = "AimJ", decay = 0.76, experimental = true)
public final class AimJ extends Check implements RotationCheck {

    private int maxBuffer;
    private double minDeltaX, maxDeltaXAccel;


    public AimJ(PlayerData player) {
        super(player);
    }


    @Override
    public void process(final RotationUpdate rotationUpdate) {
        final double deltaYawAccel = rotationUpdate.getProcessor().getYawAccel();
        final double deltaYaw = rotationUpdate.getProcessor().getDeltaYaw();

        if (Math.abs(rotationUpdate.getTo().getPitch()) == 90) {
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

        if (player.getRespawnTick() < 10 || player.getSinceRiptideSpinTick() < 60 || player.packetStateData.horseInteractCausedForcedRotation) {
            return;
        }

        if (deltaYawAccel <= maxDeltaXAccel && deltaYaw >= minDeltaX) {
            if (buffer++ > maxBuffer) {
                if (flagAndAlert("accelX= " + deltaYawAccel + "\nrotX= " + deltaYaw)) {
                    player.mitigateDamage();
                    buffer = 0;
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
        maxDeltaXAccel = getConfig().getDoubleElse(getConfigName() + ".max-deltaX-accel", 0.0001D);
    }
}
