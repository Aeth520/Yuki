package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(
        name = "AimE",
        configName = "AimE",
        type = CheckType.AIM,
        description = "Checks for rotations that have been made with wrongful randomization",
        decay = 0.05,
        experimental = true
)
public final class AimE extends Check implements RotationCheck {

    public AimE(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (player.hasAttackedSince(500L)) {

            if (isExempt(ExemptType.VEHICLE, ExemptType.VEHICLE_SWITCH, ExemptType.TELEPORT)) {
                buffer = 0;
            }

            float deltaYaw = rotationUpdate.getProcessor().getDeltaYaw();
            float optimalYaw = rotationUpdate.getProcessor().getOptimalYaw();
            float lastYaw = rotationUpdate.getProcessor().getLastYaw();
            float lastOptimalYaw = rotationUpdate.getProcessor().getLastOptimalYaw();

            float distOld = MathUtil.getAngleDifference(lastYaw, lastOptimalYaw);
            float dist = MathUtil.getAngleDifference(rotationUpdate.getTo().getYaw(), optimalYaw);

            if (deltaYaw > 40 && distOld > 26 && dist < 15) {
                if (buffer++ > 3) {
                    if (flagAndAlert("old= " + distOld + "\nnow= " + dist)) {
                        player.mitigateDamage();
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}