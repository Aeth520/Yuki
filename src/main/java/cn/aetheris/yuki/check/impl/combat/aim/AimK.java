package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(name = "AimK", type = CheckType.AIM, configName = "AimK", description = "Patch generic client rotations", decay = 0.85, experimental = true)
public final class AimK extends Check implements RotationCheck {
    public AimK(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        final float deltaYaw = rotationUpdate.getProcessor().getDeltaYaw();
        final float lastDeltaYaw = rotationUpdate.getProcessor().getLastDeltaYaw();
        final float deltaPitch = rotationUpdate.getProcessor().getDeltaPitch();
        final float lastDeltaPitch = rotationUpdate.getProcessor().getLastDeltaPitch();

        final boolean cinematic = rotationUpdate.isCinematic2();

        final boolean flag = deltaYaw > 0.32f
                && deltaPitch > 0.32f
                && deltaYaw < 20.f
                && deltaPitch < 20.f
                && player.hasAttackedSince(1100L)
                && !cinematic;

        if (flag) {
            final float rotationRound = Math.round(deltaYaw) + Math.round(deltaPitch);
            final float previousRotationRound = Math.round(lastDeltaYaw) + Math.round(lastDeltaPitch);

            if (rotationRound == previousRotationRound
                    && Math.round(deltaYaw) == Math.round(lastDeltaYaw)) {
                if (++buffer > 10) {
                    if (flagAndAlert("r= " + rotationRound + "\npr= " + previousRotationRound + "\ndy= " + rotationUpdate.getProcessor().getDeltaYaw() + "\ndp= " + rotationUpdate.getProcessor().getDeltaPitch())) {
                        player.mitigateDamage();
                        buffer = 0;
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}

