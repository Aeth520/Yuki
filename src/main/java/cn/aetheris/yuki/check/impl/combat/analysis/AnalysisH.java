package cn.aetheris.yuki.check.impl.combat.analysis;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisH.AnalysisHConfig;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisH.FeatureExtractor;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisH.FeatureNormalizer;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisH.InferenceEngine;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisH.LogisticRegressionModel;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisH.ModelLoader;
import cn.aetheris.yuki.check.impl.combat.analysis.analysisH.ProbabilityAggregator;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;

@CheckData(
        name = "AnalysisH",
        configName = "AnalysisH",
        type = CheckType.ANALYSIS,
        experimental = true,
        decay = 0.90
)
public final class AnalysisH extends Check implements RotationCheck {

    private AnalysisHConfig config;
    private FeatureExtractor extractor;
    private FeatureNormalizer normalizer;
    private LogisticRegressionModel model;
    private InferenceEngine engine;
    private ProbabilityAggregator aggregator;

    public AnalysisH(PlayerData player) {
        super(player);
        rebuildPipeline();
    }

    private void rebuildPipeline() {
        config = AnalysisHConfig.from(this);
        extractor = new FeatureExtractor();
        normalizer = new FeatureNormalizer(8, config.getNormalizationWindow());
        model = ModelLoader.loadAnalysisH();
        engine = new InferenceEngine(extractor, normalizer, model);
        aggregator = new ProbabilityAggregator(config.getProbabilityWindow());
    }

    @Override
    public void process(RotationUpdate up) {
        if (player.getDeltaXZ() < 0.02 || !player.hasAttackedSince(650L)
                || hasExemptions() || !shouldModifyPackets()) {
            buffer *= 0.80;
            return;
        }

        final int sens = player.calculateSensitivity();
        final int sensTemp = player.getRotateProcessor().totalSensitivityClient;
        if (sens < 50 || sens > 150 || sensTemp < 60 || sensTemp >= 150) {
            buffer *= 0.95;
            return;
        }

        if (player.getTarget() == null
                || player.getTarget().getType() != EntityTypes.PLAYER) {
            return;
        }

        if (player.getTarget().getPossibleCollisionBoxes().distance(player.boundingBox) > 0.85) {
            return;
        }

        RotateProcessor rp = up.getProcessor();

        InferenceEngine.InferenceResult result = engine.infer(rp);

        if (engine.sampleCount() < config.getMinSamples()) return;

        aggregator.add(result.probability());

        if (!aggregator.hasEnoughSamples(config.getMinProbabilitySamples())) return;

        aggregator.aggregate();

        double inc = aggregator.computeIncrement(config, result.probability());

        if (inc > 0) {
            buffer = buffer * config.getBufferDecay() + Math.min(config.getMaxIncrement(), inc);
        } else {
            buffer *= config.getBufferDecay();
        }

        if (buffer > config.getFlagThreshold()
                && flagAndAlert(buildDebug(result))) {
            buffer *= config.getPostFlagDecay();
        }
    }

    private String buildDebug(InferenceEngine.InferenceResult result) {
        return String.format(
                "p= %.3f\nmean= %.3f\npeak= %.3f\nlogit= %.2f\nbuf= %.2f",
                result.probability(),
                aggregator.getLastMean(),
                aggregator.getLastPeak(),
                result.logit(),
                buffer
        );
    }

    private boolean hasExemptions() {
        return isExempt(ExemptType.TELEPORT, ExemptType.SERVER_SENT_PULLBACK, ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING, ExemptType.VEHICLE)
                || player.packetStateData.horseInteractCausedForcedRotation;
    }

    @Override
    public void reload() {
        super.reload();
        rebuildPipeline();
        buffer = 0;
    }
}
