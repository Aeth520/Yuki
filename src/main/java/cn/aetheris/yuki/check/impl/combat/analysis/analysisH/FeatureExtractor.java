package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;

public final class FeatureExtractor {

    public FeatureVector extract(RotateProcessor rp) {
        double dY = Math.abs(rp.getDeltaYaw());
        double dP = Math.abs(rp.getDeltaPitch());
        double yAccel = rp.getYawAccel();
        double pAccel = rp.getPitchAccel();
        double sensDiff = rp.getSensitivityDiff();
        double yDiff = rp.getYawDiff();
        double pDiff = rp.getPitchDiff();

        double dotsX = Math.abs(rp.getDeltaDotsX());
        double dotsY = Math.abs(rp.getDeltaDotsY());
        double gcdRatio = dotsY > 1e-6 ? dotsX / dotsY : 0.0;

        return new FeatureVector(dY, dP, yAccel, pAccel, sensDiff, yDiff, pDiff, gcdRatio);
    }
}
