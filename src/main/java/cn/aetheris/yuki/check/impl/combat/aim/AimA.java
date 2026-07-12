package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.vec.Vec2f;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@CheckData(name = "AimA", type = CheckType.AIM, configName = "AimA", decay = 0.75)
public final class AimA extends Check implements RotationCheck {

    private final List<Double> stack = new LinkedList<>();

    public AimA(PlayerData player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (rotationUpdate.isCinematic2()) return;

        if (!player.hasAttackedSince(500L)) return;

        double deltaX = rotationUpdate.getProcessor().getDeltaYaw();
        double deltaY = rotationUpdate.getProcessor().getDeltaPitch();

        if (player.getTarget() != null && player.getTarget() != EntityTypes.PLAYER) {
            return;
        }

        if (deltaY == 0 && deltaX == 0) return;

        if (Math.abs(rotationUpdate.getTo().getPitch()) >= 90) return;


        if (isExempt(ExemptType.TELEPORT, ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE, ExemptType.ELYTRA_FLYING, ExemptType.VEHICLE)) {
            return;
        }

        Vec2f delta = rotationUpdate.getDelta();
        double angle = MathUtil.getAngleInDegrees(delta) % 90;

        if ((deltaY > 1.5 && deltaX > 0.32) || deltaX > 1.5) {
            stack.add(angle);
        }

        if (stack.size() >= 20) {
            List<Float> jiff = MathUtil.getJiffDelta(stack, 1);
            float prev = 999f;
            float prePrev = 999f;

            for (float current : jiff) {
                if (current == 0f && prev == 0f && prePrev == 0f) {
                    if (buffer++ > 5) {
                        if (flagAndAlert("m= " + Arrays.toString(jiff.toArray()))) {
                            player.mitigateDamage();
                        }
                    } else {
                        rewardBufferAndVL();
                    }
                    break;
                }
                prePrev = prev;
                prev = current;
            }

            stack.clear();
        }
    }
}
