package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.vec.Vec2f;
import cn.aetheris.yuki.util.ray.RayUtils;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

import java.util.List;

@CheckData(name = "AimD",
        configName = "AimD",
        type = CheckType.AIM,
        decay = 0.95,
        experimental = true)
public final class AimD extends Check implements RotationCheck {

    private final List<Double> stack = new EvictingList<>(3);
    private boolean lastIsNoRotation = false;
    private double lastHash = 0;
    private float buffer = 0;
    private int ticksToReset = 0;

    public AimD(PlayerData player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (rotationUpdate.isCinematic2()) {
            stack.clear();
            buffer = 0;
            return;
        }
        if (player.hasAttackedSince(800L)) {

            if (player.getTarget() != null && player.getTarget().getType() == EntityTypes.PLAYER) {
                if (rotationUpdate.getDelta().getY() == 0 && rotationUpdate.getDelta().getX() == 0) {
                    if (!lastIsNoRotation) stack.add(0.0);
                    check();
                    lastIsNoRotation = true;
                } else {
                    Vec2f delta = rotationUpdate.getDelta();
                    stack.add(RayUtils.scaleVal(delta.getX(), 2));
                    check();
                    lastIsNoRotation = false;
                }
            }
        }
    }

    private void check() {
        if (isExempt(ExemptType.RESPAWN, ExemptType.TELEPORT)) {
            stack.clear();
        }
        if (player.getRotateProcessor().getDeltaYaw() > 40 || player.getRotateProcessor().getDeltaPitch() > 45) {
            return;
        }
        if (stack.size() != 3) return;
        double hash = stack.get(0) + stack.get(1) + stack.get(2);
        if (hash == lastHash) return;
        double centre = stack.get(1);
        boolean hugeRotation = centre > 35;
        if (hugeRotation && centre != 360.0f) {
            double compare = 110;
            boolean invalid = (stack.get(0) < compare && stack.get(2) < compare)

                    || MathUtil.getMax(stack) > 70 && MathUtil.getMin(stack) < compare && MathUtil.getDistinct(stack) != 3;
            if (invalid) {
                float localVl = (centre > 160) ? 3 : (centre < 60) ? 1 : 2;
                buffer += localVl;
                if (buffer >= 8) {
                    if (alert(("(" + centre + "/" + RayUtils.scaleVal(stack.get(0) + stack.get(2), 2) + ")"))) {
                        player.mitigateDamage();
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        } else {
            ticksToReset++;
            if (ticksToReset >= 2500) {
                ticksToReset = 0;
                buffer = 0;
            }
        }
        lastHash = hash;
    }
}
