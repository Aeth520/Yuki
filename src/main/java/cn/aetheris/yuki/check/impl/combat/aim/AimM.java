package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.EvictingQueue;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;

@CheckData(name = "AimM", type = CheckType.AIM, configName = "AimM", decay = 0.85)
public final class AimM extends Check implements RotationCheck {

    private EvictingQueue<Boolean> invalidDivisorList;
    private EvictingQueue<Double> rotationList;
    private double minAverageRot;
    private double maxInvalidRows;
    private double minRowLength;
    private double maxBuffer;
    private int sampleSize;
    private int mitigateVL;

    public AimM(PlayerData player) {
        super(player);
    }


    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.hasAttackedSince(150L)) {
            double divisorY = rotationUpdate.getProcessor().getDivisorY();
            double deltaY = rotationUpdate.getProcessor().getDeltaPitch();

            if (Math.abs(rotationUpdate.getTo().getPitch()) == 90) {
                return;
            }

            if (isExempt(
                    ExemptType.TELEPORT,
                    ExemptType.SERVER_SENT_PULLBACK,
                    ExemptType.SERVER_SENT_ROTATE,
                    ExemptType.ELYTRA_FLYING,
                    ExemptType.VEHICLE) || player.packetStateData.horseInteractCausedForcedRotation
                    || !player.isMoving()) {
                return;
            }


            invalidDivisorList.add(divisorY < MathUtil.MINIMUM_DIVISOR);
            rotationList.add(deltaY);
            if (invalidDivisorList.size() >= sampleSize) {
                double averageRot = MathUtil.getAverageDouble(rotationList);
                if (getRowCount() > maxInvalidRows && averageRot > minAverageRot) {
                    if (buffer++ > maxBuffer) {
                        if (flagAndAlert(String.format("r= %d\na= %.2f", getRowCount(), averageRot))) {
                            buffer *= 0.65;
                            if (getViolations() > mitigateVL) {
                                player.mitigateDamage();
                            }
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
            }
        }
    }

    private int getRowCount() {

        int rowCount = 0;
        int currentTrueCount = 0;

        for (Boolean b : invalidDivisorList) {
            if (b) {
                currentTrueCount++;
            } else {
                if (currentTrueCount >= minRowLength) {
                    rowCount++;
                }
                currentTrueCount = 0;
            }
        }


        if (currentTrueCount >= minRowLength) {
            rowCount++;
        }

        return rowCount;
    }

    @Override
    public void reload() {
        super.reload();
        maxBuffer = getConfig().getIntElse(getConfigName() + ".buffer", 5);
        sampleSize = getConfig().getIntElse(getConfigName() + ".sample-size", 25);
        maxInvalidRows = getConfig().getDoubleElse(getConfigName() + ".max-invalid-rows", 2);
        minRowLength = getConfig().getDoubleElse(getConfigName() + ".min-row-length", 3);
        minAverageRot = getConfig().getDoubleElse(getConfigName() + ".min-average-rot", 0.4D);
        mitigateVL = getConfig().getIntElse(getConfigName() + ".mitigate-vl", 6);
        invalidDivisorList = new EvictingQueue<>(sampleSize);
        rotationList = new EvictingQueue<>(sampleSize);
    }
}
