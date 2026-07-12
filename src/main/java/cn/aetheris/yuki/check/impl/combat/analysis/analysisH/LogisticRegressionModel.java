package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import cn.aetheris.yuki.math.MathUtil;

public final class LogisticRegressionModel implements InferenceModel {

    private static final double[] DEFAULT_WEIGHTS = {
            1.35,
            0.90,
            1.10,
            0.75,
            0.60,
            0.85,
            0.70,
            1.00
    };
    private static final double DEFAULT_BIAS = -4.20;

    private final double[] weights;
    private final double bias;

    public LogisticRegressionModel(double[] weights, double bias) {
        this.weights = weights;
        this.bias = bias;
    }

    @Override
    public double[] getWeights() {
        return weights;
    }

    @Override
    public double getBias() {
        return bias;
    }

    @Override
    public int featureCount() {
        return weights.length;
    }

    public static double[] getDefaultWeights() {
        return DEFAULT_WEIGHTS.clone();
    }

    public static double getDefaultBias() {
        return DEFAULT_BIAS;
    }

    public double predictProbability(double[] normalizedFeatures) {
        double logit = computeLogit(normalizedFeatures);
        return MathUtil.sigmoid(logit);
    }
}
