package cn.aetheris.yuki.math.stats;

import java.util.function.DoublePredicate;

/**
 * Self-halving histogram for distribution analysis, ported from Intave.
 * When the sample count exceeds the limit, all bins are halved —
 * an exponential-decaying approximation of a sliding window.
 */
public final class Histogram {
    private final double start;
    private final double end;
    private final double step;
    private final int[] bins;
    private final int limit;
    private double total;

    public Histogram(double start, double end, double step, int limit) {
        if (step <= 0) throw new IllegalArgumentException("step must be positive");
        this.start = start;
        this.end = end;
        this.step = step;
        this.bins = new int[(int) Math.ceil((end - start) / step)];
        this.limit = limit;
        this.total = 0;
    }

    public void add(double value) {
        if (value < start || value > end) {
            return;
        }
        int index = (int) Math.floor((value - start) / step);
        if (index < 0 || index >= bins.length) {
            return;
        }
        bins[index]++;
        total++;

        if (total > limit) {
            total /= 2;
            for (int i = 0; i < bins.length; i++) {
                bins[i] /= 2;
            }
        }
    }

    /**
     * Clamp the value into range and add it.
     */
    public void forceAdd(double value) {
        add(Math.max(start, Math.min(end, value)));
    }

    public double mean() {
        double sum = 0;
        double count = 0;
        for (int i = 0; i < bins.length; i++) {
            sum += bins[i] * (start + i * step);
            count += bins[i];
        }
        return count == 0 ? 0 : sum / count;
    }

    public double variance() {
        double mean = mean();
        double sum = 0;
        double count = 0;
        for (int i = 0; i < bins.length; i++) {
            sum += bins[i] * Math.pow((start + i * step) - mean, 2);
            count += bins[i];
        }
        return count == 0 ? 0 : sum / count;
    }

    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    /**
     * Empirical probability density of a single value's bin.
     */
    public double likelihood(double value) {
        if (value < start || value > end || total == 0) {
            return 0;
        }
        int index = (int) Math.floor((value - start) / step);
        if (index < 0 || index >= bins.length) {
            return 0;
        }
        return bins[index] / total;
    }

    /**
     * Empirical probability that a value falls within [from, to].
     */
    public double likelihood(double from, double to) {
        if (total == 0) return 0;
        double sum = 0;
        int lo = Math.max(0, (int) Math.floor((from - start) / step));
        int hi = Math.min(bins.length - 1, (int) Math.ceil((to - start) / step));
        for (int i = lo; i <= hi; i++) {
            sum += bins[i];
        }
        return sum / total;
    }

    /**
     * Root-mean-square deviation from a uniform distribution over bins
     * matching the filter — low values indicate unnaturally even spread
     * (typical of randomized/botted input).
     */
    public double mseUniform(DoublePredicate binFilter) {
        double meanBinCount = 0;
        double count = 0;
        for (int i = 0; i < bins.length; i++) {
            if (binFilter.test(start + i * step)) {
                meanBinCount += bins[i];
                count++;
            }
        }
        if (count == 0) return 0;
        meanBinCount /= count;
        double sum = 0;
        for (int i = 0; i < bins.length; i++) {
            if (binFilter.test(start + i * step)) {
                sum += Math.pow(bins[i] - meanBinCount, 2);
            }
        }
        return Math.sqrt(sum);
    }

    public int[] bins() {
        return bins;
    }

    public int size() {
        return (int) total;
    }

    public void clear() {
        java.util.Arrays.fill(bins, 0);
        total = 0;
    }
}
