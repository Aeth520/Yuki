package cn.aetheris.yuki.check.impl.movement.sprint;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "SprintA (Water)", type = CheckType.SPRINT, configName = "SprintA", description = "Sprinting while in water", experimental = true)
public class SprintA extends Check implements PostPredictionCheck {
    private boolean lastChecked;

    public SprintA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (player.wasTouchingWater && player.wasWasTouchingWater && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13) && lastChecked && predictionComplete.isChecked()) {
            if (player.isSprinting() && !player.isSwimming) {
                if (buffer++ > 3) {
                    if (flagAndAlert("")) {
                        player.mitigateDamage();
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }

        lastChecked = predictionComplete.isChecked();
    }
}