package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(
        name = "AimO",
        configName = "AimO",
        description = "Weird rotation",
        type = CheckType.AIM,
        decay = 0.6,
        experimental = true)
public final class AimO extends Check implements RotationCheck {

    private int zeroDeltaTicks;

    public AimO(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        final float deltaYaw = rotationUpdate.getProcessor().getDeltaYaw();
        final float deltaPitch = rotationUpdate.getProcessor().getDeltaPitch();

        final float pitch = rotationUpdate.getProcessor().getPitch();
        final float lastPitch = rotationUpdate.getProcessor().getLastPitch();

        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE)) {
            return;
        }

        if (player.hasAttackedSince(1000L)) {
            if (deltaPitch == 0.0F) {
                zeroDeltaTicks++;
            } else {
                zeroDeltaTicks = 0;
            }

            if (zeroDeltaTicks <= 40
                    || !(deltaYaw > 3.0F)
                    || !(Math.abs(pitch) < 45.0F)
                    || !(player.deltaXZ > 0.08)) {
                buffer *= 0.75;
            } else if (buffer++ > 8.0) {
                if (flagAndAlert("now= " + pitch + "\nlast= " + lastPitch)) {
                    rewardBufferAndVL();
                    player.mitigateDamage();
                }
            }
        }
    }
}