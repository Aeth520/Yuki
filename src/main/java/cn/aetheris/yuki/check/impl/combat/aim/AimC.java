package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

@CheckData(
        name = "AimC (Anomaly)",
        type = CheckType.AIM,
        configName = "AimC",
        decay = 0.25,
        experimental = true
)
public final class AimC extends Check implements RotationCheck {

    private double unnaturalBuffer;

    public AimC(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (!player.hasAttackedSince(800L)) return;

        RotateProcessor processor = rotationUpdate.getProcessor();
        float deltaYaw = processor.getDeltaYaw();
        float deltaPitch = processor.getDeltaPitch();
        float pitch = processor.getPitch();
        float yaw = processor.getYaw();
        float lastYaw = processor.getLastYaw();

        if (player.getTarget() == null || player.getTarget().getType() == EntityTypes.PLAYER) return;

        if (player.getTarget() != player.getLastTarget()) {
            buffer = Math.max(0, buffer - getDecay());
            unnaturalBuffer = Math.max(0, unnaturalBuffer - getDecay());
            return;
        }

        double threshold = player.calculateSensitivity() > 130 ? 7 : 3;

        if (deltaYaw > threshold && deltaPitch < 0.01 && Math.abs(pitch) < 89) {
            buffer++;
            unnaturalBuffer = Math.max(0, unnaturalBuffer - getDecay());

            if (buffer > 3) {
                if (flagAndAlert(String.format("(Sync)\ndy= %.2f\ndp= %.2f\nthreshold= %.1f",
                        deltaYaw, deltaPitch, threshold))) {
                    player.mitigateDamage();
                }
            }

            if (deltaYaw > 170 && Math.abs(yaw - lastYaw) < 1) {
                if (flagAndAlert("(Change)\ndy= " + deltaYaw)) {
                    player.mitigateDamage();
                }
            }
        } else if (deltaYaw < 0.1 && deltaPitch > threshold) {
            unnaturalBuffer++;
            buffer = Math.max(0, buffer - getDecay());

            if (unnaturalBuffer > 3) {
                if (flagAndAlert("(Unnatural)\ndp= " + deltaPitch)) {
                    player.mitigateDamage();
                }
            }
        } else {
            buffer = Math.max(0, buffer - getDecay());
            unnaturalBuffer = Math.max(0, unnaturalBuffer - getDecay());
        }
    }
}
