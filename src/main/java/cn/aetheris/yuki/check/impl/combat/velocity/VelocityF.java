package cn.aetheris.yuki.check.impl.combat.velocity;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.player.inventory.InventoryG;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import org.jetbrains.annotations.NotNull;

@CheckData(name = "VelocityF (Hor)",
        alternativeName = "VelocityF",
        configName = "VelocityF",
        type = CheckType.VELOCITY,
        setback = 8,
        decay = 0.65,
        experimental = true)
public class VelocityF extends Check implements PostPredictionCheck {

    public VelocityF(@NotNull PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked() || predictionComplete.getData().isTeleport()) return;
        if (player.getVelocitySinceTick() == 1) {
            InventoryG velocityData = player.getCheckManager().getCheck(InventoryG.class);
            if (velocityData != null) {
                final double deltaXZ = player.getDeltaXZ();

                
                if (player.getY() % 1 / 8 == 0) {
                    return;
                }

                final boolean exempt = isExempt(
                        ExemptType.LIQUID,
                        ExemptType.CLIMBING,
                        ExemptType.TELEPORT,
                        ExemptType.WEB
                ) || player.isHorizontalCollision()
                        || player.exemptOnGround()
                        || player.getSetbackTeleportUtil().shouldBlockMovement();

                final double percentage = deltaXZ / velocityData.getHorizontalVelocity();
                final boolean invalid = !exempt
                        && (percentage <= 0.2);

                if (invalid) {
                    if (++buffer > 3) {
                        if (flagAndAlert("p= " + percentage)) {
                            buffer = 0;
                        }
                    }
                } else {
                    buffer = 0;
                }
            }
        }
    }
}