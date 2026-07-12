package cn.aetheris.yuki.functionality;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 单例性能监控器，统计每 tick 处理耗时与各检查耗时。
 * 使用 LongAdder / ConcurrentHashMap / 原子数组实现无锁记录，避免影响性能。
 */
public final class PerformanceMonitor {

    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();
    private static final int WINDOW_SIZE = 100;

    // 滑动窗口：最近 100 tick 的总处理时间（纳秒）
    private final long[] tickTimes;
    private final AtomicInteger tickIndex;

    // 按检查名分组的统计
    private final ConcurrentHashMap<String, CheckStats> checkStats;

    // 全局累计
    private final LongAdder totalChecks;
    private final LongAdder totalTickNanos;
    private final LongAdder tickCount;

    private PerformanceMonitor() {
        this.tickTimes = new long[WINDOW_SIZE];
        this.tickIndex = new AtomicInteger(0);
        this.checkStats = new ConcurrentHashMap<>();
        this.totalChecks = new LongAdder();
        this.totalTickNanos = new LongAdder();
        this.tickCount = new LongAdder();
    }

    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }

    /** 记录单个检查的耗时（纳秒） */
    public void recordCheckTime(String checkName, long nanos) {
        if (checkName == null) return;
        checkStats.computeIfAbsent(checkName, k -> new CheckStats()).record(nanos);
        totalChecks.increment();
    }

    /** 记录每 tick 总耗时（纳秒） */
    public void recordTickTime(long nanos) {
        int idx = Math.floorMod(tickIndex.getAndIncrement(), WINDOW_SIZE);
        tickTimes[idx] = nanos;
        totalTickNanos.add(nanos);
        tickCount.increment();
    }

    /** 获取最近 tick 统计（基于滑动窗口） */
    public TickStats getTickStats() {
        long[] snapshot = tickTimes.clone();
        List<Long> valid = new ArrayList<>(WINDOW_SIZE);
        for (long v : snapshot) {
            if (v > 0) valid.add(v);
        }
        if (valid.isEmpty()) {
            return new TickStats(0, 0, 0, 0, 0);
        }
        long sum = 0, max = Long.MIN_VALUE, min = Long.MAX_VALUE;
        for (long v : valid) {
            sum += v;
            if (v > max) max = v;
            if (v < min) min = v;
        }
        double avg = (double) sum / valid.size();
        // 95 分位
        Collections.sort(valid);
        int p95Idx = (int) Math.ceil(valid.size() * 0.95) - 1;
        if (p95Idx < 0) p95Idx = 0;
        long p95 = valid.get(p95Idx);

        return new TickStats(
                valid.size(),
                avg / 1_000_000.0,
                max / 1_000_000.0,
                min / 1_000_000.0,
                p95 / 1_000_000.0
        );
    }

    /** 获取按检查分组的统计（只读视图） */
    public Map<String, CheckStats> getCheckStats() {
        return Collections.unmodifiableMap(checkStats);
    }

    /** 总检查次数 */
    public long getTotalChecks() {
        return totalChecks.sum();
    }

    /** 获取 TPS（优先从 LagManager / Bukkit 获取） */
    public double getTPS() {
        try {
            double[] tps = Bukkit.getTPS();
            if (tps != null && tps.length > 0) return tps[0];
        } catch (NoSuchMethodError ignored) {
        }
        return 20.0;
    }

    /** 获取 MSPT（毫秒/tick），基于自身记录的平均 tick 处理时间 */
    public double getMSPT() {
        long count = tickCount.sum();
        if (count == 0) return 0.0;
        return (totalTickNanos.sum() / (double) count) / 1_000_000.0;
    }

    /** 重置所有统计 */
    public void reset() {
        Arrays.fill(tickTimes, 0);
        tickIndex.set(0);
        checkStats.clear();
        totalChecks.reset();
        totalTickNanos.reset();
        tickCount.reset();
    }

    /** 获取最慢的 N 个检查 */
    public List<Map.Entry<String, CheckStats>> getSlowestChecks(int n) {
        List<Map.Entry<String, CheckStats>> list = new ArrayList<>(checkStats.entrySet());
        list.sort((a, b) -> Long.compare(b.getValue().getAverageNanos(), a.getValue().getAverageNanos()));
        if (list.size() > n) {
            return new ArrayList<>(list.subList(0, n));
        }
        return list;
    }

    /** 单个检查的统计快照 */
    public static final class CheckStats {
        private final LongAdder totalTime = new LongAdder();
        private final LongAdder count = new LongAdder();
        private final AtomicLong maxTime = new AtomicLong(0);

        void record(long nanos) {
            totalTime.add(nanos);
            count.increment();
            maxTime.accumulateAndGet(nanos, Math::max);
        }

        public long getTotalNanos() {
            return totalTime.sum();
        }

        public long getCount() {
            return count.sum();
        }

        public long getAverageNanos() {
            long c = count.sum();
            return c == 0 ? 0 : totalTime.sum() / c;
        }

        public long getMaxNanos() {
            return maxTime.get();
        }

        public double getAverageMs() {
            return getAverageNanos() / 1_000_000.0;
        }
    }

    /** tick 统计快照 */
    public static final class TickStats {
        private final int sampleCount;
        private final double averageMs;
        private final double maxMs;
        private final double minMs;
        private final double p95Ms;

        public TickStats(int sampleCount, double averageMs, double maxMs, double minMs, double p95Ms) {
            this.sampleCount = sampleCount;
            this.averageMs = averageMs;
            this.maxMs = maxMs;
            this.minMs = minMs;
            this.p95Ms = p95Ms;
        }

        public int getSampleCount() { return sampleCount; }
        public double getAverageMs() { return averageMs; }
        public double getMaxMs() { return maxMs; }
        public double getMinMs() { return minMs; }
        public double getP95Ms() { return p95Ms; }
    }
}
