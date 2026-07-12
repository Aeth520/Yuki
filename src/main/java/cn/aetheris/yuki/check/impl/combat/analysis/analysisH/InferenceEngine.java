package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import lombok.Getter;

public final class InferenceEngine {

    private final FeatureExtractor extractor;
    private final FeatureNormalizer normalizer;
    private final LogisticRegressionModel model;

    @Getter
    private double lastProbability;
    @Getter
    private double lastLogit;
    @Getter
    private FeatureVector lastFeatureVector;

    public InferenceEngine(FeatureExtractor extractor, FeatureNormalizer normalizer, LogisticRegressionModel model) {
        this.extractor = extractor;
        this.normalizer = normalizer;
        this.model = model;
    }

    public InferenceResult infer(RotateProcessor rp) {
        FeatureVector vector = extractor.extract(rp);
        lastFeatureVector = vector;

        double[] normalized = normalizer.normalize(vector);
        double logit = model.computeLogit(normalized);
        double probability = model.predictProbability(normalized);

        this.lastLogit = logit;
        this.lastProbability = probability;

        return new InferenceResult(vector, normalized, logit, probability);
    }

    public int sampleCount() {
        return normalizer.sampleCount();
    }

    public void reset() {
        normalizer.reset();
        lastProbability = 0;
        lastLogit = 0;
        lastFeatureVector = null;
    }

    public record InferenceResult(
            FeatureVector featureVector,
            double[] normalizedFeatures,
            double logit,
            double probability
    ) {
    }
}
