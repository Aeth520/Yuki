package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(name = "AimH", type = CheckType.AIM, configName = "AimH", decay = 0.65, experimental = true)
public final class AimH extends Check implements RotationCheck {

    int maxBuffer;
    double minDeltaY;
    double maxDeltaX;
    double dynamicMaxDeltaX;
    double dynamicMinDeltaY;

    public AimH(PlayerData player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        double deltaX = rotationUpdate.getProcessor().getDeltaYaw();
        double deltaY = rotationUpdate.getProcessor().getDeltaPitch();

        if (!(Math.abs(rotationUpdate.getTo().getPitch()) < 90)) return;

        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE)) {
            return;
        }

        dynamicMaxDeltaX = calculateDynamicMaxDeltaX();
        dynamicMinDeltaY = calculateDynamicMinDeltaY();

        if (!player.hasAttackedSince(250L)) {
            return;
        }

        if (deltaX < dynamicMaxDeltaX && deltaY > dynamicMinDeltaY && shouldModifyPackets()) {
            if (buffer++ > maxBuffer) {
                if (flagAndAlert("deltaX= " + deltaX
                        + "\ndeltaY= " + deltaY)) {
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
        minDeltaY = getConfig().getDoubleElse(getConfigName() + ".min-deltaY", 1D);
        maxDeltaX = getConfig().getDoubleElse(getConfigName() + ".max-deltaX", 0.0001D);
    }

    private double calculateDynamicMaxDeltaX() {
        return maxDeltaX * (1 + Math.sin(time() / 1000.0));
    }

    private double calculateDynamicMinDeltaY() {
        return minDeltaY * (1 + Math.cos(time() / 1000.0));
    }
}