package cn.aetheris.yuki.check.impl.combat.aim;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.lists.Tuple;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

@CheckData(
        name = "AimT",
        configName = "AimT",
        type = CheckType.AIM,
        decay = 0.86,
        experimental = true
)
public final class AimT extends Check implements RotationCheck {

    private final List<Float> samplesYaw = new ArrayList<>();
    private final List<Float> samplesPitch = new ArrayList<>();
    private double buffer2;

    public AimT(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        final boolean invalidSensitivity = player.calculateSensitivity() < 75 || player.calculateSensitivity() > 175;
        if (update.isCinematic2() || invalidSensitivity || !player.hasAttackedSince(600L)) return;
        final RotateProcessor processor = update.getProcessor();
        final float deltaYaw = processor.getDeltaYaw();
        final float lastDeltaYaw = processor.getLastDeltaYaw();
        final float deltaPitch = processor.getDeltaPitch();
        final float lastDeltaPitch = processor.getLastDeltaPitch();

        final float differenceYaw = Math.abs(deltaYaw - lastDeltaYaw);
        final float differencePitch = Math.abs(deltaPitch - lastDeltaPitch);

        final float joltX = Math.abs(deltaYaw - differenceYaw);
        final float joltY = Math.abs(deltaPitch - differencePitch);

        samplesYaw.add((float) MathUtil.roundToPlace(joltX, 2));
        samplesPitch.add((float) MathUtil.roundToPlace(joltY, 2));

        if (samplesYaw.size() + samplesPitch.size() >= 60) {
            if (!(joltX == 0.0 || joltY == 0.0)) {
                final Tuple<List<Double>, List<Double>> outliersYaw = MathUtil.getOutliers(samplesYaw);
                final Tuple<List<Double>, List<Double>> outliersPitch = MathUtil.getOutliers(samplesPitch);

                final int distinctYaw = (int) samplesYaw.stream().distinct().count();
                final int distinctPitch = (int) samplesPitch.stream().distinct().count();
                final int duplicatesX = samplesYaw.size() - distinctYaw;
                final int duplicatesY = samplesPitch.size() - distinctPitch;
                final int duplicatesSum = duplicatesX + duplicatesY;
                final int outliersX = outliersYaw.getX().size() + outliersYaw.getY().size();
                final int outliersY = outliersPitch.getX().size() + outliersPitch.getY().size();
                if (duplicatesSum <= 3 && outliersX < 10 && outliersY < 7) {
                    if (buffer++ > 4) {
                        if (flagAndAlert("d= " + duplicatesSum + "\nox= " + outliersX + "\noy= " + outliersY)) {
                            player.mitigateDamage();
                        }
                    } else {
                        rewardBufferAndVL();
                    }
                } else if ((outliersX == 0 || outliersY == 0) && (outliersX > 1 || outliersY > 1) && duplicatesSum <= 3) {
                    if (buffer2++ > 2) {
                        if (flagAndAlert("d= " + duplicatesSum + "\nox= " + outliersX + "\noy= " + outliersY)) {
                            player.mitigateDamage();
                        }
                    }
                } else {
                    if (buffer2 == 0.0) {
                        rewardVL();
                    } else {
                        buffer2 = Math.max(0, buffer2 - getDecay());
                    }
                }
            }
            samplesYaw.clear();
            samplesPitch.clear();
        }
    }
}