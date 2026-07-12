package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import lombok.Getter;

@Getter
public final class FeatureVector {
    public static final int SIZE = 8;

    private final double deltaYaw;
    private final double deltaPitch;
    private final double yawAccel;
    private final double pitchAccel;
    private final double sensitivityDiff;
    private final double yawDiff;
    private final double pitchDiff;
    private final double gcdRatio;

    public FeatureVector(double deltaYaw, double deltaPitch, double yawAccel, double pitchAccel,
                         double sensitivityDiff, double yawDiff, double pitchDiff, double gcdRatio) {
        this.deltaYaw = deltaYaw;
        this.deltaPitch = deltaPitch;
        this.yawAccel = yawAccel;
        this.pitchAccel = pitchAccel;
        this.sensitivityDiff = sensitivityDiff;
        this.yawDiff = yawDiff;
        this.pitchDiff = pitchDiff;
        this.gcdRatio = gcdRatio;
    }

    public double[] toArray() {
        return new double[]{
                deltaYaw, deltaPitch, yawAccel, pitchAccel,
                sensitivityDiff, yawDiff, pitchDiff, gcdRatio
        };
    }

    public static String[] featureNames() {
        return new String[]{
                "dY", "dP", "yAccel", "pAccel", "sensDiff", "yDiff", "pDiff", "gcdRatio"
        };
    }
}
