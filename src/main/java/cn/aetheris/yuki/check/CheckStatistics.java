package cn.aetheris.yuki.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Detection-outcome statistics per check, ported from Intave's CheckStatistics.
 * Aggregated globally per check config name across all players, so the memory
 * footprint is bounded by the number of check types, not players.
 *
 * <p>Semantics:
 * <ul>
 *   <li>{@code totalProcessed} — every flag() invocation (total traffic)</li>
 *   <li>{@code totalViolations} — flag() actually increased VL</li>
 *   <li>{@code totalFails} — flag() was gated (exempt/TPS/event-cancel)</li>
 *   <li>{@code totalPassed} — rewardVL() calls (clean behavior observed)</li>
 * </ul>
 * A check with heavy {@code totalFails} but zero violations is likely
 * mis-exempted (config issue); a check with zero traffic is dead code.</p>
 */
public final class CheckStatistics {
    private static final ConcurrentHashMap<String, CheckStatistics> REGISTRY = new ConcurrentHashMap<>();

    private final String name;
    private final LongAdder totalProcessed = new LongAdder();
    private final LongAdder totalPassed = new LongAdder();
    private final LongAdder totalFails = new LongAdder();
    private final LongAdder totalViolations = new LongAdder();

    private CheckStatistics(String name) {
        this.name = name;
    }

    public static CheckStatistics forCheck(String configName) {
        if (configName == null || configName.isEmpty()) return null;
        return REGISTRY.computeIfAbsent(configName, CheckStatistics::new);
    }

    public static Collection<Map.Entry<String, CheckStatistics>> all() {
        return REGISTRY.entrySet();
    }

    public static void resetAll() {
        REGISTRY.clear();
    }

    public void increaseTotal() {
        totalProcessed.increment();
    }

    public void increaseViolations() {
        totalViolations.increment();
    }

    public void increaseFails() {
        totalFails.increment();
    }

    public void increasePasses() {
        totalPassed.increment();
    }

    public String name() {
        return name;
    }

    public long totalProcessed() {
        return totalProcessed.sum();
    }

    public long totalPassed() {
        return totalPassed.sum();
    }

    public long totalFails() {
        return totalFails.sum();
    }

    public long totalViolations() {
        return totalViolations.sum();
    }

    public static List<Map.Entry<String, CheckStatistics>> topByViolations(int limit) {
        List<Map.Entry<String, CheckStatistics>> entries = new ArrayList<>(REGISTRY.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue().totalViolations(), a.getValue().totalViolations()));
        return entries.subList(0, Math.min(limit, entries.size()));
    }

    public static List<Map.Entry<String, CheckStatistics>> topByGated(int limit) {
        List<Map.Entry<String, CheckStatistics>> entries = new ArrayList<>(REGISTRY.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue().totalFails(), a.getValue().totalFails()));
        return entries.subList(0, Math.min(limit, entries.size()));
    }
}
