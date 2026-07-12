package cn.aetheris.yuki.check.impl.movement.noslow;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.enums.Pose;
import cn.aetheris.yuki.util.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

@CheckData(name = "NoSlowC (Sneak)", type = CheckType.NOSLOW, configName = "NoSlowC", setback = 12, description = "Invalid Walk While Sneaking")
public final class NoSlowC extends Check implements PostPredictionCheck {

    private long lastFlag;

    public NoSlowC(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) return;

        if (player.isSlowMovement && player.sneakingSpeedMultiplier < 0.8f) {
            ClientVersion version = player.getClientVersion();

            
            if (version.isNewerThanOrEquals(ClientVersion.V_1_14_2) && version != ClientVersion.V_1_21_4) {
                return;
            }
            
            if (version.isNewerThanOrEquals(ClientVersion.V_1_14) && player.wasFlying && player.lastPose == Pose.FALL_FLYING && !player.isGliding) {
                return;
            }

            if (version == ClientVersion.V_1_21_4 && player.exemptOnGround()) {
                return;
            }

            if (player.isSprinting && (!player.wasTouchingWater || version.isOlderThan(ClientVersion.V_1_13))) {
                if (time() - lastFlag < 500L) {
                    return;
                }
                if (buffer++ > 3) {
                    if (flagAndAlert("s= " + player.sneakingSpeedMultiplier)) {
                        flagWithSetback();
                        lastFlag = time();
                        buffer = 0.0;
                    }
                }
            } else {
                rewardBufferAndVL();
            }
        }
    }
}
