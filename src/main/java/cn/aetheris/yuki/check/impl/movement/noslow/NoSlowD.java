package cn.aetheris.yuki.check.impl.movement.noslow;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.PredictionComplete;

@CheckData(name = "NoSlowD (Blocking)", type = CheckType.NOSLOW, configName = "NoSlowD", setback = 8, description = "Invalid Blocking", experimental = true)
public final class NoSlowD extends Check implements PostPredictionCheck {

    public boolean didSlotChangeLastTick;
    private boolean flaggedLastTick = false;
    private long lastFlag;

    public NoSlowD(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) return;

        if (!player.packetStateData.isSlowedByUsingItem()) {
            flaggedLastTick = false;
            return;
        }

        if (isExempt(ExemptType.SWIMMING, ExemptType.VEHICLE_SWITCH, ExemptType.INVALID_GAMEMODE,
                ExemptType.TELEPORT, ExemptType.FLYING, ExemptType.ELYTRA_FLYING,
                ExemptType.JOIN, ExemptType.LAGGING, ExemptType.MOVE_LAGGING)) {
            return;
        }

        if ((player.isSprinting || player.lastSprinting) && player.isMoving()) {
            if (time() - lastFlag < 400L) {
                return;
            }
            if (flaggedLastTick) {
                if (buffer++ > 3) {
                    if (flagAndAlertWithSetback("")) {
                        shuffleAboveSetbackVL();
                        resetPlayerUseItem(player.getBukkitPlayer());
                        buffer = 0.0;
                    }
                }
                if (PluginLoader.INSTANCE.getConfigManager().isMitigateNoSlowChangeSlot()) {
                    resetPlayerUseItem(player.getBukkitPlayer());
                }
            }
            lastFlag = time();
            flaggedLastTick = true;
        } else {
            rewardBufferAndVL();
            flaggedLastTick = false;
        }
    }
}