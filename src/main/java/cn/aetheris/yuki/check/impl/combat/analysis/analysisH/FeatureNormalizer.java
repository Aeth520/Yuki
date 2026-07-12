package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class FeatureNormalizer {

    private final int featureCount;
    private final int windowSize;
    private final EvictingList<Double>[] buffers;

    public FeatureNormalizer(int featureCount, int windowSize) {
        this.featureCount = featureCount;
        this.windowSize = windowSize;
        this.buffers = new EvictingList[featureCount];
        for (int i = 0; i < featureCount; i++) {
            buffers[i] = new EvictingList<>(windowSize);
        }
    }

    public double[] normalize(FeatureVector vector) {
        double[] raw = vector.toArray();
        double[] normalized = new double[featureCount];
        for (int i = 0; i < featureCount; i++) {
            buffers[i].add(raw[i]);
            normalized[i] = zScore(buffers[i], raw[i]);
        }
        return normalized;
    }

    private double zScore(EvictingList<Double> buffer, double value) {
        if (buffer.size() < 2) return 0.0;
        List<Double> values = new ArrayList<>(buffer.getAllValuesAsDouble());
        double mean = MathUtil.mean(values);
        double std = MathUtil.stdDev(values, mean);
        return std > 1e-6 ? (value - mean) / std : 0.0;
    }

    public int sampleCount() {
        return buffers[0].size();
    }

    public void reset() {
        for (EvictingList<Double> buffer : buffers) {
            buffer.clear();
        }
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public int getWindowSize() {
        return windowSize;
    }
}
