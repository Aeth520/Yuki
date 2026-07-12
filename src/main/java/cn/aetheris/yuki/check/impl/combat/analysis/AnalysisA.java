package cn.aetheris.yuki.check.impl.combat.analysis;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.RotationCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.check.util.processor.rotateprocessor.RotateProcessor;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.file.LoggerUtil;
import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.util.update.RotationUpdate;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@CheckData(name = "AnalysisA", type = CheckType.ANALYSIS, configName = "AnalysisA", decay = 0.92)
public final class AnalysisA extends Check implements RotationCheck {

    public static final Set<String> DEBUG_PLAYERS = new HashSet<>();
    private static final int FIRST_DERIV_WINDOW = 120, SECOND_DERIV_WINDOW = 120;
    private static final int SUS_HIST_SIZE = 200;
    private static final int LONG_TERM_SMOOTH_WINDOW = 300, SUSPICION_WINDOW = 150;
    private static final int MECH_SMOOTH_WINDOW = 100;
    private static final int SHORT_WINDOW_SIZE = 10, MID_WINDOW_SIZE = 20, LONG_WINDOW_SIZE = 40;
    private static final double MAX_K = 1.2, MIN_STD = 0.5;
    private static final double BIG_TO_SMALL_RATIO = 20.0, BIG_TO_SMALL_MINVAL = 0.4, BIG_TO_SMALL_MAXVAL = 5.0;
    private static final double MECH_SMOOTH_MAX_DIFF = 0.5, MECH_SMOOTH_AVG_THRESHOLD = 3.0;
    private static final double MECH_JITTER_CV_THRESHOLD = 0.20, MECH_JITTER_MEAN_THRESHOLD = 1.0;
    private static final double THR_MIN = 1.0, THR_MAX = 1.20;
    private static final double INC_SMALL_PITCH1 = 0.15, INC_SMALL_PITCH2 = 0.25;
    private final double[] susHistory = new double[200];
    private double postFlagDecay = 0.35;
    private double thresholdStdMult = 2.0;
    private int smoothWindow = 80;
    private double bufferDecay = 0.965;
    private double bufferFlagThreshold = 4.0;

    private double smallMoveAngle = 0.5;
    private int smallPitchWindow = 60;
    private double smallPitchMax = 0.30;
    private double pitchRatio1 = 0.70, pitchRatio2 = 0.85;
    private int jitterWindow = 15, jitterFlipThreshold = 6;
    private EvictingList<Double> yawFirstDeriv, pitchFirstDeriv, yawSecondDeriv, pitchSecondDeriv;
    private EvictingList<Double> suspicionSeries;
    private EvictingList<Double> recentYaw, recentPit, longTermSmooth;

    private int frameIndex;
    private EvictingList<Double> shortYaw, shortPit, midYaw, midPit, longYaw, longPit;
    private double lastYawDelta, lastPitchDelta;
    private double prevYawDiff, prevPitchDiff;
    private int smallMoveFrameCount, yawFlipCount, pitchFlipCount, jitterWindowCounter;
    private int sameDeltaFrameCount;
    private Double lastYawForJitter, lastPitchForJitter;

    public AnalysisA(PlayerData player) {
        super(player);
        initWindows();

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
        boolean validSensitivity = sens >= 50 && sens <= 150 && sensTemp >= 60 && sensTemp < 150;

        if (!validSensitivity) {
            buffer *= 0.95;
            return;
        }

        RotateProcessor rp = up.getProcessor();
        double dY = rp.getDeltaYaw(), dP = rp.getDeltaPitch();

        if (Math.abs(dY) >= 18 || Math.abs(dP) >= 20 || Math.abs(dP) > 8
                || player.predictedVelocity.isJump()) {
            buffer *= 0.85;
            return;
        }

        if (player.getTarget() == null) {
            return;
        }

        if (player.getTarget().getPossibleCollisionBoxes().distance(player.boundingBox) > 0.85) {
            return;
        }

        double[] d2 = updateDerivatives(dY, dP);
        double smooth = updateSmoothScore(dY, dP);

        double inc = combineIncrements(dY, dP, d2[0], d2[1], smooth)
                + checkSmallPitchDominance()
                + checkMechanicalSmoothing()
                + checkMechanicalJitter();

        if (buffer > bufferFlagThreshold - 0.25) inc += 0.04;
        buffer = buffer * bufferDecay + Math.min(0.5, inc);

        if (buffer > bufferFlagThreshold && flagAndAlert(debugLine(dY, dP, d2, smooth)))
            buffer *= postFlagDecay;

        if (DEBUG_PLAYERS.contains(player.getName())) {
            String dbg = debugLine(dY, dP, d2, smooth).replace("\n", "");
            for (UUID uuid : PluginLoader.INSTANCE.getAlertManager().getEnabledVerbose()) {
                final Player bukkitPlayer = Bukkit.getPlayer(uuid);
                if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
                    bukkitPlayer.sendMessage(dbg);
                }
            }
            LoggerUtil.log(player.getName(), dbg, "analysis");
        }
    }

    private double[] updateDerivatives(double dY, double dP) {
        double dy2 = lastYawDelta != 0 ? Math.abs(dY - lastYawDelta) : 0;
        double dp2 = lastPitchDelta != 0 ? Math.abs(dP - lastPitchDelta) : 0;
        yawFirstDeriv.add(dY);
        pitchFirstDeriv.add(dP);
        yawSecondDeriv.add(dy2);
        pitchSecondDeriv.add(dp2);
        lastYawDelta = dY;
        lastPitchDelta = dP;
        return new double[]{dy2, dp2};
    }

    private double updateSmoothScore(double dY, double dP) {
        recentYaw.add(Math.abs(dY));
        recentPit.add(Math.abs(dP));
        double sm = MathUtil.stdDev(recentYaw.getAllValuesAsDouble())
                + MathUtil.stdDev(recentPit.getAllValuesAsDouble());
        longTermSmooth.add(Double.isFinite(sm) ? sm : 0);
        prevYawDiff = dY;
        prevPitchDiff = dP;
        return sm;
    }


    private double combineIncrements(double dY, double dP, double y2, double p2, double sm) {
        List<Double> l = new ArrayList<>();
        l.add(checkMaxMin(shortYaw, shortPit, SHORT_WINDOW_SIZE));
        l.add(checkMaxMin(midYaw, midPit, MID_WINDOW_SIZE) * 0.8);
        l.add(checkMaxMin(longYaw, longPit, LONG_WINDOW_SIZE) * 0.6);
        l.add(checkJitterAndSmallMove(dY, dP));

        double yawStd = Math.max(MathUtil.stdDev(yawFirstDeriv.getAllValuesAsDouble()), MIN_STD);
        double pitStd = Math.max(MathUtil.stdDev(pitchFirstDeriv.getAllValuesAsDouble()), MIN_STD);
        double sus = 0.6 * (MathUtil.sigmoid(Math.min(y2 / yawStd, MAX_K) * 2.5)
                + MathUtil.sigmoid(Math.min(p2 / pitStd, MAX_K) * 2.5));

        suspicionSeries.add(sus);
        susHistory[frameIndex % SUS_HIST_SIZE] = sus;
        frameIndex++;
        double thr = calcDynThr();
        if (sus > thr) {
            double diff = sus - thr;
            l.add(diff > 0.15 ? 0.25 : diff > 0.07 ? 0.15 : 0.08);
        }
        return MathUtil.combineIncrements(l);
    }


    private double checkSmallPitchDominance() {
        if (recentPit.size() < smallPitchWindow) return 0;
        int yawFrames = 0, silent = 0;
        for (int i = recentPit.size() - smallPitchWindow; i < recentPit.size(); i++) {
            double p = recentPit.get(i), y = recentYaw.get(i);
            if (Math.abs(y) > 1) {
                yawFrames++;
                if (Math.abs(p) < smallPitchMax) silent++;
            }
        }
        if (yawFrames == 0) return 0;
        double ratio = silent / (double) yawFrames;
        if (ratio >= pitchRatio2) return INC_SMALL_PITCH2;
        if (ratio >= pitchRatio1) return INC_SMALL_PITCH1;
        return 0;
    }

    private double checkMechanicalSmoothing() {
        if (longTermSmooth.size() < MECH_SMOOTH_WINDOW) return 0;
        List<Double> sub = tail(longTermSmooth);
        double max = Collections.max(sub), min = Collections.min(sub), avg = MathUtil.mean(sub);
        return (max - min) < MECH_SMOOTH_MAX_DIFF && avg > MECH_SMOOTH_AVG_THRESHOLD ? 0.15 : 0;
    }

    private double checkMechanicalJitter() {
        if (shortYaw.size() < SHORT_WINDOW_SIZE || shortPit.size() < SHORT_WINDOW_SIZE) return 0;
        double mY = MathUtil.mean(shortYaw.getAllValuesAsDouble()), mP = MathUtil.mean(shortPit.getAllValuesAsDouble());
        double sY = MathUtil.stdDev(shortYaw.getAllValuesAsDouble()), sP = MathUtil.stdDev(shortPit.getAllValuesAsDouble());
        double cvY = sY / mY, cvP = sP / mP;
        double inc = 0;
        if (mY > MECH_JITTER_MEAN_THRESHOLD && cvY < MECH_JITTER_CV_THRESHOLD) inc += 0.15;
        if (mP > MECH_JITTER_MEAN_THRESHOLD && cvP < MECH_JITTER_CV_THRESHOLD) inc += 0.15;
        return inc;
    }


    private double checkJitterAndSmallMove(double dY, double dP) {
        double inc = 0;
        boolean small = Math.abs(dY) < smallMoveAngle && Math.abs(dP) < smallMoveAngle;
        if (small && ++smallMoveFrameCount == 15) inc += 0.20;
        if (!small) smallMoveFrameCount = 0;

        boolean flipYaw = Math.signum(dY) != Math.signum(prevYawDiff);
        boolean flipPit = Math.signum(dP) != Math.signum(prevPitchDiff);
        if (small && flipYaw) yawFlipCount++;
        if (small && flipPit) pitchFlipCount++;
        if (++jitterWindowCounter >= jitterWindow) {
            if (yawFlipCount >= jitterFlipThreshold) inc += 0.15;
            if (pitchFlipCount >= jitterFlipThreshold) inc += 0.15;
            yawFlipCount = pitchFlipCount = jitterWindowCounter = 0;
        }

        if (lastYawForJitter != null && lastPitchForJitter != null) {
            if (MathUtil.almostEqual(dY, lastYawForJitter, 1e-4)
                    && MathUtil.almostEqual(dP, lastPitchForJitter, 1e-4)) {
                if (++sameDeltaFrameCount >= 4) {
                    inc += 0.25;
                    sameDeltaFrameCount = 0;
                }
            } else sameDeltaFrameCount = 0;
        }
        lastYawForJitter = dY;
        lastPitchForJitter = dP;
        prevYawDiff = dY;
        prevPitchDiff = dP;
        return inc;
    }


    private double checkMaxMin(EvictingList<Double> y, EvictingList<Double> p, int sz) {
        if (y.size() < sz || p.size() < sz) return 0;
        List<Double> m = new LinkedList<>(y.getAllValuesAsDouble());
        m.addAll(p.getAllValuesAsDouble());
        m.removeIf(v -> v <= 1e-6);
        if (m.size() < 2) return 0;
        double max = Collections.max(m), min = Collections.min(m);
        if (min < BIG_TO_SMALL_MINVAL || max < BIG_TO_SMALL_MAXVAL) return 0;
        return max / (min + 1e-9) > BIG_TO_SMALL_RATIO ? 0.15 : 0;
    }

    private double calcDynThr() {
        int n = Math.min(frameIndex, SUS_HIST_SIZE);
        if (n < 2) return THR_MIN;
        List<Double> l = new ArrayList<>(n);
        for (int i = 0; i < n; i++) l.add(susHistory[i]);
        double avg = MathUtil.mean(l), std = MathUtil.stdDev(l, avg);
        return Math.max(THR_MIN, Math.min(avg + thresholdStdMult * std, THR_MAX));
    }


    private String debugLine(double dY, double dP, double[] d2, double sm) {
        return String.format("dY= %.2f\ndP= %.2f\ny2= %.2f\np2= %.2f\nsm= %.2f\ns= %s\nbuf=%.2f",
                dY, dP, d2[0], d2[1], sm, player.calculateSensitivity(), buffer);
    }

    private List<Double> tail(EvictingList<Double> src) {
        int n = src.size();
        return new ArrayList<>(src.getAllValuesAsDouble().subList(Math.max(0, n - AnalysisA.MECH_SMOOTH_WINDOW), n));
    }

    private boolean hasExemptions() {
        return isExempt(ExemptType.TELEPORT, ExemptType.SERVER_SENT_PULLBACK, ExemptType.SERVER_SENT_ROTATE,
                ExemptType.ELYTRA_FLYING, ExemptType.VEHICLE)
                || player.packetStateData.horseInteractCausedForcedRotation
                || player.getTarget().getType() != EntityTypes.PLAYER;
    }

    private void initWindows() {
        yawFirstDeriv = new EvictingList<>(FIRST_DERIV_WINDOW);
        pitchFirstDeriv = new EvictingList<>(FIRST_DERIV_WINDOW);
        yawSecondDeriv = new EvictingList<>(SECOND_DERIV_WINDOW);
        pitchSecondDeriv = new EvictingList<>(SECOND_DERIV_WINDOW);
        recentYaw = new EvictingList<>(smoothWindow);
        recentPit = new EvictingList<>(smoothWindow);
        longTermSmooth = new EvictingList<>(LONG_TERM_SMOOTH_WINDOW);
        shortYaw = new EvictingList<>(SHORT_WINDOW_SIZE);
        shortPit = new EvictingList<>(SHORT_WINDOW_SIZE);
        midYaw = new EvictingList<>(MID_WINDOW_SIZE);
        midPit = new EvictingList<>(MID_WINDOW_SIZE);
        longYaw = new EvictingList<>(LONG_WINDOW_SIZE);
        longPit = new EvictingList<>(LONG_WINDOW_SIZE);
        suspicionSeries = new EvictingList<>(SUSPICION_WINDOW);
    }

    @Override
    public void reload() {
        super.reload();
        String p = getConfigName() + ".";
        bufferDecay = getConfig().getDoubleElse(p + "buffer.decay", bufferDecay);
        bufferFlagThreshold = getConfig().getDoubleElse(p + "buffer.flag-threshold", bufferFlagThreshold);
        postFlagDecay = getConfig().getDoubleElse(p + "buffer.post-flag-decay", postFlagDecay);
        thresholdStdMult = getConfig().getDoubleElse(p + "dynamic-threshold.std-mult", thresholdStdMult);

        smoothWindow = getConfig().getIntElse(p + "windows.smooth", smoothWindow);
        smallPitchWindow = getConfig().getIntElse(p + "windows.small-pitch", smallPitchWindow);

        smallPitchMax = getConfig().getDoubleElse(p + "pitch-lock.small-pitch-max", smallPitchMax);
        pitchRatio1 = getConfig().getDoubleElse(p + "pitch-lock.ratio1", pitchRatio1);
        pitchRatio2 = getConfig().getDoubleElse(p + "pitch-lock.ratio2", pitchRatio2);

        smallMoveAngle = getConfig().getDoubleElse(p + "jitter.small-angle", smallMoveAngle);
        jitterWindow = getConfig().getIntElse(p + "jitter.window", jitterWindow);
        jitterFlipThreshold = getConfig().getIntElse(p + "jitter.flip-threshold", jitterFlipThreshold);

        initWindows();
        if (susHistory != null) {
            Arrays.fill(susHistory, 0);
        }
        frameIndex = 0;
        buffer = 0;
    }
}
