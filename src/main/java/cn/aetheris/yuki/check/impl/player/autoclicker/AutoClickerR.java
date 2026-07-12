package cn.aetheris.yuki.check.impl.player.autoclicker;

import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AutoClickerR extends Check implements PacketCheck {
    public AutoClickerR(@NotNull PlayerData player) {
        super(player);
    }

    private static class PatternBuffer {
        private final double[] values;
        private final int size;
        private int index = 0;
        private boolean filled = false;

        private double sum = 0;
        private double sumSquares = 0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private int count = 0;

        public PatternBuffer(int size) {
            this.size = size;
            this.values = new double[size];
        }

        public void add(double value) {
            if (filled) {
                double oldValue = values[index];
                sum -= oldValue;
                sumSquares -= oldValue * oldValue;
            }

            values[index] = value;
            sum += value;
            sumSquares += value * value;

            if (value < min) min = value;
            if (value > max) max = value;

            index = (index + 1) % size;
            if (!filled && index == 0) filled = true;

            count = filled ? size : index;
        }

        public double mean() {
            return count > 0 ? sum / count : 0;
        }

        public double deviation() {
            if (count < 2) return 0;
            double mean = mean();
            return Math.sqrt((sumSquares / count) - (mean * mean));
        }

        public double variationCoefficient() {
            double mean = mean();
            return mean > 0 ? deviation() / mean : 0;
        }

        public double entropy() {
            if (count < 2) return 0;

            Map<Integer, Integer> frequency = new HashMap<>();
            for (int i = 0; i < count; i++) {
                int bin = (int) (values[i] / 10);
                frequency.put(bin, frequency.getOrDefault(bin, 0) + 1);
            }

            double entropy = 0;
            for (int count : frequency.values()) {
                double probability = (double) count / this.count;
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }

            return entropy;
        }

        public int detectPattern(int minLength, int maxLength) {
            if (count < maxLength * 2) return 0;

            int maxRepetitions = 0;
            for (int len = minLength; len <= maxLength; len++) {
                Map<String, Integer> patternCount = new HashMap<>();

                for (int start = 0; start <= count - len; start++) {
                    StringBuilder pattern = new StringBuilder();
                    for (int i = 0; i < len; i++) {
                        pattern.append((int) (values[(index - count + start + i) % size] / 5));
                    }

                    String patternStr = pattern.toString();
                    int newCount = patternCount.getOrDefault(patternStr, 0) + 1;
                    patternCount.put(patternStr, newCount);

                    if (newCount > maxRepetitions) {
                        maxRepetitions = newCount;
                    }
                }
            }

            return maxRepetitions;
        }

        public int countConsecutiveIdentical(int threshold) {
            if (count < 2) return 0;

            int maxConsecutive = 0;
            int currentConsecutive = 0;
            double lastValue = values[(index - count) % size];

            for (int i = 1; i < count; i++) {
                double current = values[(index - count + i) % size];
                if (Math.abs(current - lastValue) < 5) {
                    currentConsecutive++;
                    if (currentConsecutive > maxConsecutive) {
                        maxConsecutive = currentConsecutive;
                    }
                } else {
                    currentConsecutive = 0;
                }
                lastValue = current;
            }

            return maxConsecutive >= threshold ? maxConsecutive : 0;
        }
    }
}