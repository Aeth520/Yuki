package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import cn.aetheris.yuki.check.Check;
import github.scarsz.configuralize.DynamicConfig;
import lombok.Getter;

@Getter
public final class AnalysisHConfig {

    private double bufferDecay = 0.94;
    private double flagThreshold = 5.0;
    private double postFlagDecay = 0.40;
    private double meanProbabilityThreshold = 0.68;
    private double peakProbabilityThreshold = 0.85;
    private int normalizationWindow = 60;
    private int probabilityWindow = 40;
    private int minSamples = 15;
    private int minProbabilitySamples = 20;
    private double meanIncrement = 0.35;
    private double peakIncrement = 0.20;
    private double maxIncrement = 0.6;

    public void load(DynamicConfig config, String configName) {
        String p = configName + ".";
        bufferDecay = config.getDoubleElse(p + "buffer.decay", bufferDecay);
        flagThreshold = config.getDoubleElse(p + "buffer.flag-threshold", flagThreshold);
        postFlagDecay = config.getDoubleElse(p + "buffer.post-flag-decay", postFlagDecay);
        meanProbabilityThreshold = config.getDoubleElse(p + "threshold.mean-probability", meanProbabilityThreshold);
        peakProbabilityThreshold = config.getDoubleElse(p + "threshold.peak-probability", peakProbabilityThreshold);
        normalizationWindow = config.getIntElse(p + "windows.normalization", normalizationWindow);
        probabilityWindow = config.getIntElse(p + "windows.probability", probabilityWindow);
        minSamples = config.getIntElse(p + "windows.min-samples", minSamples);
        minProbabilitySamples = config.getIntElse(p + "windows.min-probability-samples", minProbabilitySamples);
        meanIncrement = config.getDoubleElse(p + "increment.mean", meanIncrement);
        peakIncrement = config.getDoubleElse(p + "increment.peak", peakIncrement);
        maxIncrement = config.getDoubleElse(p + "increment.max", maxIncrement);
    }

    public static AnalysisHConfig from(Check check) {
        AnalysisHConfig cfg = new AnalysisHConfig();
        cfg.load(check.getConfig(), check.getConfigName());
        return cfg;
    }
}
