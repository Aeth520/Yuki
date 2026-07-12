package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

@CheckData(name = "AimQ", type = CheckType.AIM, configName = "AimQ", decay = 0.85, experimental = true, description = "rotate like aimassist")
public final class AimQ extends Check implements RotationCheck {

    private double buffer2;
    private double buffer3;
    private float lastDeltaYaw;

    public AimQ(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {
        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.RESPAWN,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE)
                || player.predictedVelocity.isKnockback()) {
            return;
        }

        if (Math.abs(rotationUpdate.getTo().getPitch()) == 90) {
            return;
        }

        if (player.getTarget() == null || player.getLastTarget() == null) {
            return;
        }

        double distanceNow = player.getTarget().getPossibleCollisionBoxes().distance(player.getBoundingBox());
        double distancePrev = player.getLastTarget().getPossibleCollisionBoxes().distance(player.getBoundingBox());
        if (distancePrev < distanceNow) {
            return;
        }

        if (player.getTarget().getType() != EntityTypes.PLAYER) {
            return;
        }

        float deltaPitch = Math.abs(player.getPitch() - player.getLastPitch());
        float deltaYaw = Math.abs(player.getYaw() - player.getLastYaw());
        float lastDeltaPitch = rotationUpdate.getProcessor().getLastDeltaPitch();
        
        float pitchDifference = Math.abs(lastDeltaPitch - deltaPitch);
        float yawDifference = Math.abs(lastDeltaYaw - deltaYaw);

        
        if (deltaYaw > yawDifference
                && yawDifference > 0.3
                && deltaPitch > 0
                && player.calculateSensitivity() > 48
                && deltaPitch <= pitchDifference
                && pitchDifference < 0.1
                && player.hasAttackedSince(500L)) {
            if (buffer++ > 7.5) {
                if (flagAndAlert("(InvalidMouse)\ndiffx= " + deltaYaw + "\ndiffy= " + deltaPitch + "\ndp= " + deltaPitch)) {
                    player.mitigateDamage();
                }
            } else {
                rewardBufferAndVL();
            }
        }

        if ((deltaPitch > 1.5f || deltaYaw > 3.0f)
                && !rotationUpdate.isCinematic2()
                && (player.getPitch() == 0 || player.getPitch() % 0.01f == 0)
                && player.hasAttackedSince(600L)
                && player.calculateSensitivity() > 50) {
            if (buffer2++ > 3) {
                if (flagAndAlert("(Randomizer-Flaw)\ndp= " + deltaPitch + "\ndy= " + deltaYaw)) {
                    if (isAboveSetbackVl()) player.mitigateDamage();
                }
            } else {
                buffer2 = Math.max(buffer2 - getDecay(), 0);
            }
        }

        
        if (deltaYaw > yawDifference
                && yawDifference > 0.0
                && deltaPitch > 0
                && deltaPitch < 0.02
                && pitchDifference > deltaPitch * 2
                && !rotationUpdate.isCinematic2()
                && player.hasAttackedSince(400L)) {
            if (buffer3++ > 5) {
                if (flagAndAlert("(Radomization-Extremerly)\ndiffx= " + deltaYaw + "\ndiffy= " + deltaPitch + "\ndy= " + deltaYaw)) {
                    buffer3 = 0;
                }
            } else {
                buffer3 = Math.max(buffer3 - getDecay(), 0);
            }
        }
        lastDeltaYaw = deltaYaw;
    }
}