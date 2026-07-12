package cn.aetheris.yuki.check.impl.player.inventory.prediction;

import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.player.inventory.InventoryG;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.movement.VectorData;
import cn.aetheris.yuki.data.movement.VehicleData;
import cn.aetheris.yuki.util.update.PredictionComplete;

import java.util.StringJoiner;

@CheckData(name = "InventoryB (MovePrediction)", configName = "InventoryB", setback = 10, decay = 0.85, experimental = true)
public final class InventoryB extends InventoryCheck {

    private long lastFlag;

    public InventoryB(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) {
            return;
        }

        if (shouldExempt()) {
            rewardBufferAndVL();
            return;
        }

        if (player.hasInventoryOpen) {
            boolean inVehicle = player.inVehicle();
            boolean isMoving;


            if (inVehicle) {
                VehicleData vehicle = player.vehicleData;

                isMoving = vehicle.nextVehicleForward != 0 || vehicle.nextVehicleHorizontal != 0;
            } else {
                VectorData.MoveVectorData move = findMovement(player.predictedVelocity);

                if (move == null) {
                    return;
                }
                if (player.uncertaintyHandler.lastTeleportTicks.hasOccurredSince(1)) {
                    player.sendTransaction();
                    move.x = 0;
                    move.z = 0;
                    return;
                }


                isMoving = move.x != 0 || move.z != 0;
            }

            if (!isMoving) {
                rewardBufferAndVL();
                return;
            }

            StringJoiner joiner = new StringJoiner(" ");

            if (inVehicle) joiner.add(", isVehicle");

            if (time() - lastFlag < 500L) {
                return;
            }
            if (buffer++ > 5) {
                if (flagAndAlert("Type= isMoving" + joiner) && shouldModifyPackets()) {
                    closeInventory();
                    rewardBufferAndVL();
                }
                lastFlag = time();
            }
        } else {
            rewardBufferAndVL();
        }
    }

    private boolean shouldExempt() {
        boolean isFence = player.uncertaintyHandler.isSteppingOnFence;
        boolean isRiptid = player.packetStateData.tryingToRiptide || player.predictedVelocity.isTrident() || player.isRiptidePose;
        boolean serverSentVelocity = player.predictedVelocity.isKnockback() || player.predictedVelocity.isExplosion();
        boolean takingVelocity = player.checkManager.getCheck(InventoryG.class) != null && (player.checkManager.getCheck(InventoryG.class).getVerticalVelocity() < 0
                || player.checkManager.getCheck(InventoryG.class).getHorizontalVelocity() < 0);
        boolean isElytra = player.isGliding;
        boolean exempt = isExempt(ExemptType.FLYING, ExemptType.CLIMBING, ExemptType.BED, ExemptType.WAS_SWIMMING, ExemptType.SWIMMING, ExemptType.LIQUID);

        return isElytra || isFence || exempt || isRiptid | serverSentVelocity || takingVelocity;
    }
}