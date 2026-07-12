package cn.aetheris.yuki.check.impl.combat.analysis;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(
        name = "AnalysisB",
        configName = "AnalysisB",
        type = CheckType.ANALYSIS,
        experimental = true,
        decay = 0.92
)
public class AnalysisB extends Check implements RotationCheck {

    private static final int ANALYSIS_WINDOW = 18;
    private static final double CONSISTENCY_THRESHOLD = 0.78;
    private static final double DYNAMIC_CONSISTENCY_FACTOR = 1.18;
    private static final double DISCRETE_ADAPTIVE_FACTOR = 0.82;
    private final Deque<Double> consistencyScores = new ArrayDeque<>();
    private final Deque<Double> discreteScores = new ArrayDeque<>();
    private long lastFlag;

    public AnalysisB(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        if (!isValidAttackState() || player.getTarget().isDead || player.getTarget().getType() != EntityTypes.PLAYER || !shouldModifyPackets()) {
            resetAnalysisWindow();
            return;
        }

        if (isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE)) {
            resetAnalysisWindow();
            buffer = 0;
            return;
        }

        final RotateProcessor processor = update.getProcessor();

        double consistency = calculateDynamicConsistency(processor);
        double discrete = calculateEnhancedDiscrete(processor);

        updateAnalysisWindow(consistency, discrete);

        if (shouldFlag()) {
            if (time() - lastFlag < 500L) {
                return;
            }
            if (buffer++ > 4) {
                if (flagAndAlert(buildDebugMessage(consistency, discrete))) {
                    rewardBufferAndVL();
                }
                lastFlag = time();
            }
        } else {
            rewardBufferAndVL();
        }
    }

    private boolean isValidAttackState() {
        return player.hasAttackedSince(300L);
    }

    private void resetAnalysisWindow() {
        consistencyScores.clear();
        discreteScores.clear();
    }

    private double calculateDynamicConsistency(RotateProcessor processor) {
        double deltaDotSum = Math.abs(processor.getDeltaDotsX()) + Math.abs(processor.getDeltaDotsY());
        double a = MathUtil.getFractionalPart(deltaDotSum);

        if (a < 0.12 || a > 0.88) {
            return 1.0;
        }
        return (a > 0.35 && a < 0.65) ? 0.4 : 0.0;
    }

    private double calculateEnhancedDiscrete(RotateProcessor processor) {
        double c = Math.abs(processor.getYawAccel() - processor.getPitchAccel());
        double d = Math.log1p(
                Math.abs(processor.getDeltaYaw()) +
                        Math.abs(processor.getDeltaPitch())
        );
        return (c / (0.01 + (processor.getYawAccel() + processor.getPitchAccel()) / 2))
                * d;
    }

    private void updateAnalysisWindow(double consistency, double discrete) {
        consistencyScores.addLast(consistency);
        discreteScores.addLast(discrete);

        int pingFactor = Math.max(1, player.getTransactionPing() / 50);
        while (consistencyScores.size() > ANALYSIS_WINDOW * pingFactor) {
            consistencyScores.removeFirst();
            discreteScores.removeFirst();
        }
    }

    private boolean shouldFlag() {
        if (consistencyScores.size() < ANALYSIS_WINDOW / 2) return false;

        double cMean = MathUtil.exponentialWeightedMean(consistencyScores);
        double dStd = MathUtil.stdDev(discreteScores, MathUtil.mean(discreteScores));

        double timeFactor = 1.0 - ((time() - player.getLastAttack()) / (double) 300L);
        return cMean > (CONSISTENCY_THRESHOLD * (1.0 + timeFactor * DYNAMIC_CONSISTENCY_FACTOR))
                && dStd < (DISCRETE_ADAPTIVE_FACTOR * dStd + 0.15);
    }


    private String buildDebugMessage(double consistency, double discrete) {
        return String.format(
                "C= %.2f (%.2f)\nD= %.2f \nWin= %d",
                consistency,
                MathUtil.exponentialWeightedMean(consistencyScores),
                discrete,
                consistencyScores.size()
        );
    }
}
