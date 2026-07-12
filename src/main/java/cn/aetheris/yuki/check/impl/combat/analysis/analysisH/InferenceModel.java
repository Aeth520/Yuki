package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

public interface InferenceModel {

    double[] getWeights();

    double getBias();

    int featureCount();

    default double computeLogit(double[] normalizedFeatures) {
        double[] weights = getWeights();
        double logit = getBias();
        for (int i = 0; i < featureCount(); i++) {
            logit += weights[i] * normalizedFeatures[i];
        }
        return logit;
    }
}
