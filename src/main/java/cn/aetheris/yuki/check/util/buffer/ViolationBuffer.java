package cn.aetheris.yuki.check.util.buffer;

/**
 * Anti-false-positive burst guard for setback decisions, ported from Intave's
 * ViolationBufferStorage pattern.
 *
 * <p>Instead of setting back on every single threshold hit, the check spends
 * "points" from a slowly-regenerating budget. Small isolated deviations are
 * absorbed (no setback), while sustained cheating quickly exhausts the budget
 * and set backs resume at full strength.</p>
 *
 * <p>Lifecycle:
 * <ul>
 *   <li>{@link #checkReset(long)} — restores the full point budget after a
 *       long quiet period.</li>
 *   <li>{@link #trySpendPoint(long, int)} — attempts to consume one point;
 *       returns {@code true} (and consumes) while points remain within the
 *       burst window, {@code false} once the budget is exhausted.</li>
 *   <li>Callers treat {@code true} as "skip the setback this time".</li>
 * </ul>
 */
public final class ViolationBuffer {
    private final int maxPoints;
    private final long burstWindowMillis;
    private final long totalResetMillis;

    private int availablePoints;
    private long lastPointTime;
    private long lastResetTime;

    public ViolationBuffer(int maxPoints, long burstWindowMillis, long totalResetMillis) {
        this.maxPoints = maxPoints;
        this.burstWindowMillis = burstWindowMillis;
        this.totalResetMillis = totalResetMillis;
        this.availablePoints = maxPoints;
        this.lastPointTime = 0;
        this.lastResetTime = System.currentTimeMillis();
    }

    /**
     * Restore the full point budget if no points were spent for
     * {@code totalResetMillis}.
     */
    public void checkReset(long now) {
        if (now - lastResetTime > totalResetMillis) {
            availablePoints = maxPoints;
            lastResetTime = now;
        }
    }

    /**
     * Try to absorb one deviation by spending a point.
     *
     * @param now        current time in millis
     * @param congestion how many points one deviation costs
     * @return {@code true} if a point was spent (caller should skip the setback),
     *         {@code false} if the budget is exhausted (caller should set back)
     */
    public synchronized boolean trySpendPoint(long now, int congestion) {
        if (now - lastPointTime > burstWindowMillis) {
            // outside the burst window: treat as fresh burst, re-arm one point
            availablePoints = Math.min(maxPoints, availablePoints + 1);
        }
        if (availablePoints >= congestion) {
            availablePoints -= congestion;
            lastPointTime = now;
            lastResetTime = now;
            return true;
        }
        lastPointTime = now;
        lastResetTime = now;
        return false;
    }

    public int availablePoints() {
        return availablePoints;
    }

    public void reset() {
        availablePoints = maxPoints;
        lastPointTime = 0;
        lastResetTime = System.currentTimeMillis();
    }
}
