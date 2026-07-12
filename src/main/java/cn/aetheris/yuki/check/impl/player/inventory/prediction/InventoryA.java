package cn.aetheris.yuki.check.impl.player.inventory.prediction;

import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.movement.movementvalidation.MovementValidation;
import cn.aetheris.yuki.check.impl.player.inventory.InventoryG;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.movement.VehicleData;
import cn.aetheris.yuki.util.update.PredictionComplete;

import java.util.StringJoiner;

@CheckData(name = "InventoryA (JumpPrediction)", configName = "InventoryA", setback = 10, decay = 0.25)
public final class InventoryA extends InventoryCheck {

    private int horseJumpVerbose;
    private long lastFlag;

    public InventoryA(PlayerData player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) {
            return;
        }

        if (!player.hasInventoryOpen) {
            return;
        }

        if (shouldExempt()) {
            rewardBufferAndVL();
            return;
        }

        boolean inVehicle = player.inVehicle();
        boolean isJumping;

        if (inVehicle) {
            VehicleData vehicle = player.vehicleData;

            isJumping = vehicle.nextHorseJump > 0 && horseJumpVerbose++ >= 1;
        } else {
            isJumping = player.predictedVelocity.isJump();
        }

        if (!isJumping) {

            return;
        }

        StringJoiner joiner = new StringJoiner(" ");

        if (inVehicle) joiner.add(", isVehicle");

        if (time() - lastFlag < 400L) {
            return;
        }

        if (buffer++ > 2) {
            if (flagAndAlert("Type= isJumping" + joiner) && shouldModifyPackets()) {
                closeInventory();
                rewardBufferAndVL();
            }
            lastFlag = time();
        } else {
            rewardBufferAndVL();
        }
        horseJumpVerbose = 0;
    }

    private boolean shouldExempt() {
        boolean isFence = player.uncertaintyHandler.isSteppingOnFence;
        boolean isRiptid = player.isRiptidePose() || player.sinceRiptideSpinTick < 20 || player.packetStateData.tryingToRiptide;
        boolean serverSentVelocity = player.predictedVelocity.isKnockback() || player.predictedVelocity.isExplosion();
        boolean takingVelocity = player.checkManager.getCheck(InventoryG.class).getVerticalVelocity() < 0
                || player.checkManager.getCheck(InventoryG.class).getHorizontalVelocity() < 0 && player.checkManager.getCheck(MovementValidation.class).getOffset() < 0.005;
        boolean exempt = isExempt(ExemptType.FLYING, ExemptType.CLIMBING, ExemptType.BED, ExemptType.WAS_SWIMMING, ExemptType.SWIMMING, ExemptType.LIQUID, ExemptType.ELYTRA_FLYING);

        return isFence || exempt || isRiptid || serverSentVelocity || takingVelocity;
    }
}