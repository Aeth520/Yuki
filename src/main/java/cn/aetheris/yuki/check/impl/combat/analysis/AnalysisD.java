package cn.aetheris.yuki.check.impl.combat.analysis;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.HeadRotation;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.vec.Vec2f;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CheckData(name = "AnalysisD", type = CheckType.ANALYSIS, configName = "AnalysisD", experimental = true, decay = 0.92)
public class AnalysisD extends Check implements RotationCheck {

    private static final int RAW_ROTATIONS_THRESHOLD = 100;
    private static final int SCORE_CHUNK_SIZE = 10;
    private final List<Vec2f> rawRotations = new CopyOnWriteArrayList<>();
    private final List<Vec2f> limitedRotations = new CopyOnWriteArrayList<>();
    private final List<Float> longTermAnalysis = new ArrayList<>();
    private double scoreBuffer;
    private double distributionBuffer;
    private double limitBuffer;
    private double outlierBuffer;

    private boolean query = false;

    public AnalysisD(PlayerData player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        if (!isInCombat() || hasExemptions() || !shouldModifyPackets()) {
            reduceAllBuffers(0.80, 0.3);
            rawRotations.clear();
            return;
        }

        if (player.getTarget() == null) {
            return;
        }

        if (hasExemptions()) {
            reduceAllBuffers(0.80, 0.3);
            rawRotations.clear();
            return;
        }

        if (player.standTicks > 20) {
            reduceAllBuffers(0.80, 0.3);

        }

        if (!player.isMoving()) {
            return;
        }

        final HeadRotation now = update.getTo();
        final HeadRotation last = update.getFrom();
        Vec2f delta = new Vec2f(now.getYaw() - last.getYaw(), now.getPitch() - last.getPitch());
        rawRotations.add(delta);

        if (rawRotations.size() >= RAW_ROTATIONS_THRESHOLD) {
            checkRotationData();
        }
        if (player.calculateSensitivity() > 50 && Math.abs(delta.getX()) > 1.35 || Math.abs(delta.getY()) > 1.35 && Math.abs(delta.getX()) > 0.32) {
            this.limitedRotations.add(delta);
            if (this.limitedRotations.size() >= 100) this.checkLimited();
        }
    }

    

    private void checkRotationData() {
        List<Float> yawChanges = new ArrayList<>();
        List<Float> pitchChanges = new ArrayList<>();
        final List<Long> xGcd = new ArrayList<>();

        final int sens = player.calculateSensitivity();
        final float gcdValue = (sens > 0)
                ? MathUtil.getGCDValue(RotateProcessor.getSENSITIVITY_MCP_VALUES()[sens - 1])
                : 0;

        for (Vec2f vec : rawRotations) {
            yawChanges.add(vec.getX());
            pitchChanges.add(vec.getY());
            if (gcdValue != 0) {
                xGcd.add((long) (vec.getX() / gcdValue));
            }
        }

        checkScore(yawChanges);
        checkDistribution(yawChanges, pitchChanges);

        rawRotations.clear();
    }

    
    private void checkScore(List<Float> yawChanges) {
        {
            final List<Float> x = new ArrayList<>(), xAbs = new ArrayList<>(), y = new ArrayList<>();
            final int sens = player.calculateSensitivity();
            for (Vec2f vec2 : this.rawRotations) {
                x.add(vec2.getX());
                xAbs.add(vec2.getX());
                y.add(vec2.getY());
            }
            List<Float> yawStack = new ArrayList<>();
            List<Double> deviations = new ArrayList<>();
            int distinctCount = 0;

            for (final float yaw : x) {
                yawStack.add(yaw);
                if (yawStack.size() >= SCORE_CHUNK_SIZE) {
                    deviations.add(MathUtil.getStandardDeviation(MathUtil.getJiffDelta(yawStack, 5)));
                    distinctCount += MathUtil.getDistinct(MathUtil.getJiffDelta(yawStack, 4));
                    yawStack.clear();
                }
            }

            List<Double> outliers = MathUtil.getZScoreOutliers(deviations, 0.5f);
            float distinctRank = (float) distinctCount / 60;
            if (outliers.isEmpty()) {
                return;
            }
            if ((outliers.size() == 1 && Math.abs(outliers.get(0)) > 18 && Math.abs(outliers.get(0)) < 100)) {
                if (!query) {
                    query = true;
                } else {
                    if (++outlierBuffer > 5) {
                        if (flagAndAlert("o= " + Arrays.toString(outliers.toArray()))) {
                            player.mitigateDamage();
                        }
                    }
                }
            } else {
                query = false;
                outlierBuffer = Math.max(0, outlierBuffer - 0.5);
            }

            final boolean valid = sens > 20 && sens < 140;
            if (distinctRank < 1.0 && distinctRank > 0.7
                    && MathUtil.getAverage(yawChanges) > 1.8
                    && valid) {
                if (++scoreBuffer > 3) {
                    if (flagAndAlert("r= " + distinctRank)) {
                        if (scoreBuffer > 5.6) {
                            player.mitigateDamage();
                        }
                    }
                }
            } else {
                scoreBuffer = Math.max(0, scoreBuffer - 0.5);
            }
        }
    }

    
    private void checkLimited() {
        final List<Float> x = new ArrayList<>();
        final List<Float> xAbs = new ArrayList<>();
        final List<Float> y = new ArrayList<>();
        for (Vec2f vec2 : this.limitedRotations) {
            x.add(vec2.getX());
            xAbs.add(vec2.getX());
            y.add(vec2.getY());
        }

        final List<Float> yawStack = new ArrayList<>();
        int resultDistinct = 0;
        for (float yaw : x) {
            yawStack.add(yaw);
            if (yawStack.size() >= 10) {
                resultDistinct += MathUtil.getDistinct(MathUtil.getJiffDelta(yawStack, 4));
                yawStack.clear();
            }
        }

        final float distinctRank = (float) resultDistinct / 60;
        longTermAnalysis.add(distinctRank);

        if (longTermAnalysis.size() >= 10) {
            double avg = MathUtil.getAverage(longTermAnalysis);
            long normalCount = longTermAnalysis.stream().filter(d -> d > 0.97).count();

            if (avg < 0.95 && normalCount < 4) {
                if (limitBuffer++ > 4) {
                    if (flagAndAlert("(Limit)\navg= " + avg + "\nn= " + normalCount)) {
                        player.mitigateDamage();
                    }
                }

            } else {
                limitBuffer = Math.max(0, limitBuffer - 0.75);
            }

            longTermAnalysis.clear();
        }

        this.limitedRotations.clear();
    }


    private void checkDistribution(List<Float> yawChanges, List<Float> pitchChanges) {
        double distinctX = MathUtil.getDistinct(yawChanges);
        double maxYawAbs = Math.abs(MathUtil.getMax(yawChanges));
        double kurtosis = MathUtil.getKurtosis(yawChanges);
        double pearson = MathUtil.getPearsonCorrelation(yawChanges, pitchChanges);
        int spikeCount = MathUtil.getZScoreOutliers(yawChanges, 1.0f).size()
                + MathUtil.getZScoreOutliers(pitchChanges, 1.0f).size();

        if (maxYawAbs > 8 && pearson < 0.25
                && distinctX < 85 && distinctX > 65
                && kurtosis > 0 && spikeCount >= 40) {
            if (++distributionBuffer > 4) {
                if (flagAndAlert("d= " + distinctX
                        + "\np= " + pearson
                        + "\nmax= " + maxYawAbs
                        + "\ns= " + spikeCount)) {
                    player.mitigateDamage();
                }
            }
        } else {
            distributionBuffer = Math.max(0, distributionBuffer - 0.5);
        }
    }

    private void reduceAllBuffers(double factor, double minCutoff) {
        scoreBuffer *= factor;
        distributionBuffer *= factor;
        outlierBuffer *= factor;
        limitBuffer *= factor;
        if (limitBuffer < minCutoff) {
            limitBuffer = 0;
        }
        if (scoreBuffer < minCutoff) {
            scoreBuffer = 0;
        }
        if (outlierBuffer < minCutoff) {
            outlierBuffer = 0;
        }
        if (distributionBuffer < minCutoff) {
            distributionBuffer = 0;
        }
    }

    private boolean isInCombat() {
        return player.hasAttackedSince(1500L);
    }

    private boolean hasExemptions() {
        return isExempt(
                ExemptType.TELEPORT,
                ExemptType.SERVER_SENT_PULLBACK,
                ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING,
                ExemptType.VEHICLE
        ) || player.packetStateData.horseInteractCausedForcedRotation ||
                player.getTarget().getType() != EntityTypes.PLAYER
                || (player.getLastTarget() != null && !player.getTarget().getUuid().equals(player.getLastTarget().getUuid()));
    }
}
