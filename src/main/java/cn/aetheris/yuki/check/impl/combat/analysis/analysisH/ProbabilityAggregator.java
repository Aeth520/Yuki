package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class ProbabilityAggregator {

    private final EvictingList<Double> probabilityWindow;

    @Getter
    private double lastMean;
    @Getter
    private double lastPeak;

    public ProbabilityAggregator(int windowSize) {
        this.probabilityWindow = new EvictingList<>(windowSize);
    }

    public void add(double probability) {
        probabilityWindow.add(probability);
    }

    public boolean hasEnoughSamples(int minSamples) {
        return probabilityWindow.size() >= minSamples;
    }

    public double aggregate() {
        if (probabilityWindow.isEmpty()) {
            lastMean = 0;
            lastPeak = 0;
            return 0;
        }
        List<Double> values = new ArrayList<>(probabilityWindow.getAllValuesAsDouble());
        lastMean = MathUtil.mean(values);
        lastPeak = peak(values);
        return lastMean;
    }

    private double peak(List<Double> values) {
        double max = Double.NEGATIVE_INFINITY;
        for (double v : values) {
            if (v > max) max = v;
        }
        return max;
    }

    public double computeIncrement(AnalysisHConfig config, double probability) {
        double inc = 0;
        double meanThr = config.getMeanProbabilityThreshold();
        double peakThr = config.getPeakProbabilityThreshold();

        if (lastMean > meanThr) {
            inc += config.getMeanIncrement() * (lastMean - meanThr) / (1.0 - meanThr);
        }
        if (lastPeak > peakThr) {
            inc += config.getPeakIncrement() * (lastPeak - peakThr) / (1.0 - peakThr);
        }
        return Math.min(config.getMaxIncrement(), inc);
    }

    public void reset() {
        probabilityWindow.clear();
        lastMean = 0;
        lastPeak = 0;
    }

    public int size() {
        return probabilityWindow.size();
    }
}
