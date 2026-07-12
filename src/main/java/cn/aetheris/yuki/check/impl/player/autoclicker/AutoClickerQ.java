package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.HashMap;
import java.util.Map;

@CheckData(name = "AutoClickerQ (Duplicate)", type = CheckType.AUTOCLICKER, configName = "AutoClickerQ", decay = 0.54)
public final class AutoClickerQ extends Check implements PacketCheck {

    final CircularBuffer longTermBuffer = new CircularBuffer(20 * 70);
    int analysisTime = 70 * 1000;

    public AutoClickerQ(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION && !isExempt(ExemptType.INTERACT)) {
            longTermBuffer.add(System.currentTimeMillis());
            cleanOldData(System.currentTimeMillis());

            if (longTermBuffer.size() > 100) {
                double[] intervals = calculateRecentIntervals(longTermBuffer, longTermBuffer.size());
                int repeated = detectRepeatedPatterns(intervals);
                if (repeated > 0) {
                    if (flagAndAlert("rp= " + repeated)) {
                        player.mitigateDamage();
                    }
                }
            }
        }
    }

    private double[] calculateRecentIntervals(CircularBuffer buffer, int count) {
        if (count < 3) return new double[0];
        count = Math.min(count, buffer.size() - 1);
        double[] intervals = new double[count];
        long[] timestamps = buffer.getRecent(count + 1);

        for (int i = 0; i < count; i++) {
            intervals[i] = (timestamps[i + 1] - timestamps[i]) / 1000.0;
        }
        return intervals;
    }

    private void cleanOldData(long currentTime) {
        while (!longTermBuffer.isEmpty() &&
                (currentTime - longTermBuffer.getOldest()) > analysisTime) {
            longTermBuffer.removeOldest();
        }
    }

    private int detectRepeatedPatterns(double[] intervals) {
        if (intervals.length < 15 * 2) return 0;

        RollingHasher hasher = new RollingHasher(15);
        Map<Long, Integer> patternCounts = new HashMap<>();

        for (double interval : intervals) {
            long hash = hasher.add(quantizeInterval(interval));
            int th = hash == 0 ? 0 : patternCounts.merge(hash, 1, Integer::sum);
            if (hash != 0 && th > 12) {
                return th;
            }
        }
        return 0;
    }

    private long quantizeInterval(double interval) {
        return Math.round(interval * 100); 
    }

    private static class CircularBuffer {
        private final long[] buffer;
        private int head = 0;
        private int tail = 0;
        private int size = 0;

        public CircularBuffer(int capacity) {
            this.buffer = new long[capacity];
        }

        public synchronized void add(long value) {
            buffer[head] = value;
            head = (head + 1) % buffer.length;
            if (size < buffer.length) {
                size++;
            } else {
                tail = (tail + 1) % buffer.length;
            }
        }

        public synchronized long[] getRecent(int count) {
            count = Math.min(count, size);
            long[] result = new long[count];

            int start = (head - count + buffer.length) % buffer.length;
            for (int i = 0; i < count; i++) {
                result[i] = buffer[(start + i) % buffer.length];
            }
            return result;
        }

        public synchronized long getNewest() {
            return buffer[(head - 1 + buffer.length) % buffer.length];
        }

        public synchronized long getOldest() {
            return buffer[tail];
        }

        public synchronized void removeOldest() {
            if (size > 0) {
                tail = (tail + 1) % buffer.length;
                size--;
            }
        }

        public synchronized int size() {
            return size;
        }

        public synchronized boolean isEmpty() {
            return size == 0;
        }
    }

    private static class RollingHasher {
        private final int windowSize;
        private final long[] window;
        private final long base = 256;
        private final long mod = 1000000007;
        private int index = 0;
        private boolean filled = false;
        private long hash = 0;
        private long power = 1;

        public RollingHasher(int windowSize) {
            this.windowSize = windowSize;
            this.window = new long[windowSize];
            for (int i = 0; i < windowSize - 1; i++) {
                power = (power * base) % mod;
            }
        }

        public long add(long value) {
            if (filled) {
                hash = (hash - window[index] * power % mod + mod) % mod;
            }

            window[index] = value;
            hash = (hash * base + value) % mod;
            index = (index + 1) % windowSize;

            if (!filled && index == 0) {
                filled = true;
            }

            return filled ? hash : 0;
        }
    }
}