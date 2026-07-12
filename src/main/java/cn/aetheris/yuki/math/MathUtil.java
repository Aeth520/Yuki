package cn.aetheris.yuki.math;

import cn.aetheris.yuki.util.lists.EvictingList;
import cn.aetheris.yuki.util.lists.Tuple;
import cn.aetheris.yuki.util.location.PacketLocation;
import cn.aetheris.yuki.protocol.nms.vec.Vec2f;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.DoubleStream;

@UtilityClass
public class MathUtil {

    public final double MINIMUM_DIVISOR = ((Math.pow(0.2f, 3) * 8) * 0.15) - 1e-3;

    public final double EXPANDER = Math.pow(2.0, 24.0);

    private final float DEGREES_TO_RADIANS = (float) Math.PI / 180f;

    private static final double INV_LN_2 = 1.4426950408889634;

    private static final double[] LOG2_CACHE = new double[1000];
    private static final double CACHE_STEP = 0.001;

    static {
        for (int i = 0; i < LOG2_CACHE.length; i++) {
            double x = i * CACHE_STEP;
            LOG2_CACHE[i] = Math.log(x + CACHE_STEP) * INV_LN_2;
        }
    }

    
    public double hypot(final double x, final double z) {
        return Math.sqrt(x * x + z * z);
    }

    
    public float radians(float degrees) {
        return degrees * DEGREES_TO_RADIANS;
    }

    
    public int floor(double d) {
        return (int) Math.floor(d);
    }

    
    public int ceil(double d) {
        return (int) Math.ceil(d);
    }

    
    public double lerp(double t, double start, double end) {
        return start + t * (end - start);
    }

    
    public double frac(double value) {
        return value - lfloor(value);
    }

    
    public long lfloor(double value) {
        long l = (long) value;
        return value < l ? l - 1L : l;
    }

    
    
    

    
    public double angleOf(double minX, double minZ, double maxX, double maxZ) {
        double dy = (minZ - maxZ);
        double dx = (maxX - minX);
        double deg = Math.toDegrees(Math.atan2(dy, dx));
        return deg < 0 ? 360d + deg : deg;
    }

    
    public double getDistanceBetweenAngles360(double alpha, double beta) {
        double diff = Math.abs((alpha % 360.0) - (beta % 360.0));
        return Math.min(360.0 - diff, diff);
    }

    
    public BlockFace getFacing(float x, float y, float z) {
        BlockFace best = BlockFace.NORTH;
        float maxDot = Float.MIN_VALUE;
        for (BlockFace face : BlockFace.values()) {
            float dot = x * face.getModX() + y * face.getModY() + z * face.getModZ();
            if (dot > maxDot) {
                maxDot = dot;
                best = face;
            }
        }
        return best;
    }

    
    public BlockFace getFacing(double x, double y, double z) {
        return getFacing((float) x, (float) y, (float) z);
    }

    
    public BlockFace getFacing(Vector3d vec) {
        return getFacing(vec.x, vec.y, vec.z);
    }

    
    
    

    
    public int getSectionCoord(int coord) {
        return coord >> 4;
    }

    
    public int getSectionCoord(double coord) {
        return getSectionCoord(floor(coord));
    }

    
    public long asLong(int x, int y, int z) {
        long l = 0L;
        l |= ((long) x & 4194303L) << 42;
        l |= ((long) y & 1048575L);
        return l | ((long) z & 4194303L) << 20;
    }

    
    public int unpackY(long packed) {
        return (int) (packed << 44 >> 44);
    }

    
    public int unpackZ(long packed) {
        return (int) (packed << 22 >> 42);
    }

    
    
    

    
    public int clamp(int num, int min, int max) {
        if (num < min) return min;
        return Math.min(num, max);
    }

    
    public double clamp(double num, double min, double max) {
        if (num < min) return min;
        return Math.min(num, max);
    }

    
    public float clamp(float num, float min, float max) {
        if (num < min) return min;
        return Math.min(num, max);
    }

    
    
    

    
    public double sum(DoubleStream stream) {
        double total = 0.0;
        PrimitiveIterator.OfDouble it = stream.iterator();
        while (it.hasNext()) {
            total += it.nextDouble();
        }
        return total;
    }

    
    public double getSkewness(List<Double> data) {
        double mean = mean(data);
        double std = stdDev(data);
        if (std == 0) return 0.0;
        double acc = 0;
        for (double v : data) {
            acc += Math.pow((v - mean) / std, 3);
        }
        return acc / data.size();
    }

    
    public double getSkewness(final Collection<? extends Number> data) {
        List<Double> list = new ArrayList<>(data.size());
        double sum = 0;
        for (Number n : data) {
            double d = n.doubleValue();
            list.add(d);
            sum += d;
        }
        Collections.sort(list);
        int size = list.size();
        double mean = sum / size;
        double median = (size % 2 != 0) ? list.get(size / 2) : (list.get((size - 1) / 2) + list.get(size / 2)) / 2.0;
        double variance = getVariance(data);
        return variance == 0 ? 0 : 3 * (mean - median) / variance;
    }

    
    public <T> List<T> lastN(List<T> data, int n) {
        int from = Math.max(0, data.size() - n);
        return data.subList(from, data.size());
    }

    
    public double mean(Collection<?> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = 0.0;
        int cnt = 0;
        for (Object v : values) {
            sum += ((Number) v).doubleValue();
            cnt++;
        }
        return sum / cnt;
    }

    
    public double mean(List<Double> list) {
        if (list.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Double d : list) {
            double v = d;
            sum += v;
        }
        return sum / list.size();
    }


    
    public double lowest(Iterable<? extends Number> numbers) {
        double min = Double.MAX_VALUE;
        for (Number n : numbers) {
            double d = n.doubleValue();
            if (d < min) min = d;
        }
        return min;
    }

    
    public double highest(Iterable<? extends Number> numbers) {
        double max = Double.MIN_VALUE;
        for (Number n : numbers) {
            double d = n.doubleValue();
            if (d > max) max = d;
        }
        return max;
    }

    
    public double gcd(double a, double b) {
        if (a == 0) return 0;
        if (a < b) {
            double tmp = a;
            a = b;
            b = tmp;
        }
        while (b > MINIMUM_DIVISOR) {
            double tmp = a - Math.floor(a / b) * b;
            a = b;
            b = tmp;
        }
        return a;
    }

    
    public double gcd_eac(final double a, final double b) {
        try {
            if (a < b) return gcd(b, a);
            if (Math.abs(b) < 0.001) return a;
            return gcd(b, a - Math.floor(a / b) * b);
        } catch (StackOverflowError ignored) {
            return 0;
        }
    }

    
    public long gcd_eac(final long current, final long previous) {
        return (previous <= 16384L) ? current : gcd_eac(previous, current % previous);
    }

    
    public long hashCode(double x, int y, double z) {
        long l = (long) (x * 3129871) ^ (long) z * 116129781L ^ y;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    
    public double getMedian(final List<Double> data) {
        int size = data.size();
        return (size % 2 == 0) ? (data.get(size / 2) + data.get(size / 2 - 1)) / 2.0 : data.get(size / 2);
    }


    
    public int getOutliers(Deque<Integer> list) {
        int cnt = 0;
        for (Integer v : list) {
            if (v > 3) cnt++;
        }
        return cnt;
    }

    
    public Tuple<List<Double>, List<Double>> getOutliers(final Collection<? extends Number> collection) {
        List<Double> vals = new ArrayList<>();
        for (Number n : collection) vals.add(n.doubleValue());
        Collections.sort(vals);
        double q1 = getMedian(vals.subList(0, vals.size() / 2));
        double q3 = getMedian(vals.subList(vals.size() / 2, vals.size()));
        double iqr = Math.abs(q1 - q3);
        double low = q1 - 1.5 * iqr;
        double high = q3 + 1.5 * iqr;
        Tuple<List<Double>, List<Double>> tuple = new Tuple<>(new ArrayList<>(), new ArrayList<>());
        for (double v : vals) {
            if (v < low) tuple.getX().add(v);
            else if (v > high) tuple.getY().add(v);
        }
        return tuple;
    }

    
    public <T extends Number> double getDuplicates(final Collection<T> entry) {
        Set<T> seen = new HashSet<>(entry);
        return entry.size() - seen.size();
    }

    
    public double getAverage(Collection<? extends Number> values) {
        double sum = 0.0;
        int cnt = 0;
        for (Number n : values) {
            sum += n.doubleValue();
            cnt++;
        }
        return cnt == 0 ? 0.0 : sum / cnt;
    }

    
    public double getAverage(List<Double> data) {
        double sum = 0;
        for (double v : data) sum += v;
        return data.isEmpty() ? 0.0 : sum / data.size();
    }

    
    public double[] dequeTranslator(Collection<? extends Number> numbers) {
        double[] arr = new double[numbers.size()];
        int i = 0;
        for (Number n : numbers) arr[i++] = n.doubleValue();
        return arr;
    }

    
    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("places<0");
        return roundToPlace(value, places);
    }

    
    public double getStandardDeviation(Collection<? extends Number> doubles) {
        double sq = getVariance(doubles);
        return Math.sqrt(sq / doubles.size());
    }

    
    public double getVariance(final Collection<? extends Number> data) {
        double mean = getAverage(data);
        double var = 0.0;
        for (Number n : data) {
            double diff = n.doubleValue() - mean;
            var += diff * diff;
        }
        return var;
    }

    
    public double getVariance(List<Double> data) {
        if (data.isEmpty()) return 0.0;
        double mean = getAverage(data);
        double var = 0.0;
        for (double v : data) {
            var += Math.pow(v - mean, 2);
        }
        return var / data.size();
    }

    
    public double getVariance(List<Double> data, boolean sample) {
        if (data.size() < 2) return 0.0;
        double mean = getAverage(data);
        double var = 0.0;
        for (double v : data) var += Math.pow(v - mean, 2);
        return var / (data.size() - (sample ? 1 : 0));
    }

    
    public double getSDeviation(final Collection<? extends Number> data) {
        return Math.sqrt(getVariance(data));
    }

    
    public double getKurtosis(List<Double> data, boolean sample) {
        if (data.size() < 4) return 0.0;
        double variance = getVariance(data, sample);
        if (variance == 0) return 0.0;
        double mean = getAverage(data);
        double m4 = 0.0;
        for (double v : data) m4 += Math.pow(v - mean, 4);
        return (m4 / data.size()) / Math.pow(variance, 2) - 3;
    }

    
    public double getKurtosis(final Collection<? extends Number> data) {
        int n = data.size();
        if (n < 3) return 0.0;
        double mean = getAverage(data);
        double m2 = 0, m4 = 0;
        for (Number num : data) {
            double diff = mean - num.doubleValue();
            m2 += diff * diff;
            m4 += diff * diff * diff * diff;
        }
        return (n * (n + 1.0) * m4 / (m2 * m2) - 3.0 * (n - 1.0)) / ((n - 1.0) * (n - 2.0) * (n - 3.0) / (double) (n * n));
    }

    
    public boolean isExponentiallySmall(final Number number) {
        return number.doubleValue() < 1 && String.valueOf(number.doubleValue()).contains("E");
    }

    
    public double getAverage(Iterable<? extends Number> iterable) {
        double sum = 0;
        int cnt = 0;
        for (Number n : iterable) {
            sum += n.doubleValue();
            cnt++;
        }
        return cnt == 0 ? 0.0 : sum / cnt;
    }

    
    public double getHorizontalDistance(PacketLocation from, PacketLocation to) {
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();
        return Math.hypot(dx, dz);
    }


    
    public double getHorizontalDistance(Vector3d from, Vector3d to) {
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();
        return Math.hypot(dx, dz);
    }

    
    public double getWeightedAverage(EvictingList<Long> samples) {
        if (samples.isEmpty()) return 0.0;
        double wSum = 0, sum = 0;
        for (int i = 0; i < samples.size(); i++) {
            double w = i + 1;
            sum += samples.get(i) * w;
            wSum += w;
        }
        return sum / wSum;
    }

    
    public double calculateDistance(PacketLocation from, PacketLocation to) {
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    
    public double diff(double a, double b) {
        return Math.abs(a - b);
    }

    
    public double getFractionalPart(double value) {
        return Math.abs(value - Math.floor(value + 1e-6));
    }

    
    public List<Double> insertSort(List<Double> data) {
        List<Double> arr = new ArrayList<>(data);
        for (int i = 1; i < arr.size(); i++) {
            double key = arr.get(i);
            int j = i - 1;
            while (j >= 0 && arr.get(j) > key) {
                arr.set(j + 1, arr.get(j));
                j--;
            }
            arr.set(j + 1, key);
        }
        return arr;
    }

    
    public double computeJerkThreshold(List<Double> jerkHistory) {
        if (jerkHistory.isEmpty()) return 8.0;
        for (double v : jerkHistory) if (Math.abs(v) > 100) return 100.0;
        double median = getMedian(insertSort(jerkHistory));
        double sum = 0;
        for (double v : jerkHistory) sum += Math.abs(v - median);
        double mad = sum / jerkHistory.size();
        return Math.max(median + 6 * mad, 10.0);
    }

    public double getStandardDeviation(List<Double> data) {
        return stdDev(data);
    }

    
    public double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    
    public List<Double> computeDerivatives(List<Double> data) {
        List<Double> diffs = new ArrayList<>(Math.max(0, data.size() - 1));
        for (int i = 0; i < data.size() - 1; i++) {
            diffs.add(data.get(i + 1) - data.get(i));
        }
        return diffs;
    }

    
    public double calculatePeriodicity(List<Double> data) {
        if (data.size() < 10) return 0.0;
        double sumSq = 0;
        for (double v : data) sumSq += v * v;
        double main = data.get(0) * data.get(0) + data.get(1) * data.get(1);
        return main / (sumSq + 1e-6);
    }

    
    public double calculateAutocorrelation(List<Double> data, int lag) {
        if (data.size() < lag * 2) return 0;
        double mean = getAverage(data);
        double num = 0, den = 0;
        for (int i = lag; i < data.size(); i++) {
            num += (data.get(i) - mean) * (data.get(i - lag) - mean);
            den += Math.pow(data.get(i) - mean, 2);
        }
        return num / (den + 1e-6);
    }


    
    public double stdDev(Collection values, double mean) {
        int size = values.size();
        if (size < 2) return 0.0;
        double sumSq = 0;
        for (Object v : values) {
            double diff = ((Number) v).doubleValue() - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / (size - 1));
    }

    public double stdDev(Collection values) {
        return stdDev(values, mean(values));
    }


    public double stdDev(double average, Iterable<? extends Number> numbers) {
        double stdDev = 0.0;
        int i = 0;

        for (Number number : numbers) {
            stdDev += Math.pow(number.doubleValue() - average, 2.0);
            ++i;
        }

        stdDev /= i;
        return Math.sqrt(stdDev);
    }

    
    public double stdDev(double sum, double squareSum, int validSamples) {
        if (validSamples <= 0) return 0.0;
        double mean = sum / validSamples;
        return Math.sqrt(squareSum / validSamples - mean * mean);
    }

    
    public double stdDevQuick(List<Double> values) {
        int n = values.size();
        double sum = 0, sumSq = 0;
        for (double v : values) {
            sum += v;
            sumSq += v * v;
        }
        sum /= n;
        sumSq /= n;
        return Math.sqrt(sumSq - sum * sum);
    }

    
    public int sign(double x) {
        return x == 0 ? 0 : (x > 0 ? 1 : -1);
    }

    
    public List<Double> getZScoreOutliers(final Collection<? extends Number> data, double threshold) {
        List<Double> out = new ArrayList<>();
        double mean = getAverage(data);
        double std = getStandardDeviation(data);
        if (std == 0) return out;
        for (Number n : data) {
            double z = (n.doubleValue() - mean) / std;
            if (Math.abs(z) > threshold) out.add(n.doubleValue());
        }
        return out;
    }

    
    public double exponentialWeightedMean(Deque<Double> data) {
        double sum = 0, wSum = 0, decay = 0.6;
        int idx = 0;
        for (Double v : data) {
            double w = Math.pow(decay, idx++);
            sum += v * w;
            wSum += w;
        }
        return sum / wSum;
    }

    
    public float square(float value) {
        return value * value;
    }

    
    public double square(double value) {
        return value * value;
    }

    
    public double getAverageDouble(final Collection<Double> nums) {
        return nums.isEmpty() ? 0 : getSumDouble(nums) / nums.size();
    }


    
    public double getSumDouble(final Collection<Double> nums) {
        double sum = 0D;
        for (double v : nums) sum += v;
        return sum;
    }

    
    public double calculatePercentile(final Collection<? extends Number> data, double percentile) {
        if (data.isEmpty()) throw new IllegalArgumentException("empty collection");
        List<Double> sorted = new ArrayList<>(data.size());
        for (Number n : data) sorted.add(n.doubleValue());
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        if (index < 0) index = 0;
        if (index >= sorted.size()) index = sorted.size() - 1;
        return sorted.get(index);
    }

    
    public double getIQR(final Collection<? extends Number> data) {
        List<Double> sorted = new ArrayList<>(data.size());
        for (Number n : data) sorted.add(n.doubleValue());
        Collections.sort(sorted);
        return calculatePercentile(sorted, 75) - calculatePercentile(sorted, 25);
    }

    public double getIQR(List<Double> data) {
        if (data.isEmpty()) return 0;
        List<Double> sorted = new ArrayList<>(data);
        Collections.sort(sorted);

        int size = sorted.size();
        double q1 = sorted.get((int) (size * 0.25));
        double q3 = sorted.get((int) (size * 0.75));
        return q3 - q1;
    }


    
    public float gcdRational(final float a, final float b) {
        if (a == 0) return b;
        int q = getIntQuotient(b, a);
        float r = ((b / a) - q) * a;
        if (Math.abs(r) < Math.max(a, b) * 1E-3F) r = 0;
        return gcdRational(r, a);
    }

    public int getIntQuotient(final float dividend, final float divisor) {
        float ans = dividend / divisor;
        float err = Math.max(dividend, divisor) * 1E-3F;
        return (int) (ans + err);
    }

    
    public double getCps(final Collection<? extends Number> data) {
        return 20 / getAverage(data);
    }

    
    public boolean isExponentiallyLarge(final Number number) {
        return number.doubleValue() > 10000 && String.valueOf(number.doubleValue()).contains("E");
    }

    
    public long getGcd(final long current, final long previous) {
        return (previous <= 16384L) ? current : getGcd(previous, current % previous);
    }

    
    public double getGcd(final double a, final double b) {
        if (a == b) return 0;
        if (a < b) return getGcd(b, a);
        if (Math.abs(b) < 1E-5) return a;
        return getGcd(b, a - Math.floor(a / b) * b);
    }

    
    public double getMin(final Collection<? extends Number> collection) {
        double min = Double.MAX_VALUE;
        for (Number n : collection) min = Math.min(min, n.doubleValue());
        return min;
    }

    
    public double getMax(final Collection<? extends Number> collection) {
        double max = Double.MIN_VALUE;
        for (Number n : collection) max = Math.max(max, n.doubleValue());
        return max;
    }

    
    public long getAbsoluteGcd(final double current, final double last) {
        long a = (long) (current * EXPANDER);
        long b = (long) (last * EXPANDER);
        return getGcd(a, b);
    }

    public long getAbsoluteGcd(final float current, final float last) {
        return getAbsoluteGcd((double) current, last);
    }

    
    public double percentile(List<Float> sorted, double perc) {
        if (sorted.isEmpty()) return 0.0;
        double idx = perc / 100.0 * (sorted.size() - 1);
        int low = (int) Math.floor(idx);
        int high = (int) Math.ceil(idx);
        if (low == high) return sorted.get(low);
        float vLow = sorted.get(low);
        float vHigh = sorted.get(high);
        return vLow + (vHigh - vLow) * (idx - low);
    }

    
    public double iqr(List<Float> list) {
        if (list.size() < 4) return 0.0;
        List<Float> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        return percentile(sorted, 75) - percentile(sorted, 25);
    }

    
    public int stableCount(Collection<Float> vals, float tolerance) {
        if (vals.isEmpty()) return 0;
        double mean = getAverage(vals);
        int cnt = 0;
        for (Float f : vals) if (Math.abs(f - mean) <= tolerance) cnt++;
        return cnt;
    }

    
    public double computeShannonEntropy(List<Float> list) {
        if (list.isEmpty()) return 0;
        Map<Integer, Integer> freq = new HashMap<>();
        for (Float f : list) {
            int bin = (int) (f * 10);
            freq.put(bin, freq.getOrDefault(bin, 0) + 1);
        }
        double ent = 0, size = list.size();
        for (int c : freq.values()) {
            double p = c / size;
            ent -= p * (Math.log(p) / Math.log(2));
        }
        return ent;
    }

    
    public boolean almostEqual(double a, double b, double tol) {
        return Math.abs(a - b) < tol;
    }

    
    public double combineIncrements(List<Double> increments) {
        double max = 0, sum = 0;
        for (double inc : increments) {
            if (inc > max) {
                sum += max;
                max = inc;
            } else {
                sum += inc;
            }
        }
        return max + sum * 0.4;
    }

    
    public double getDifference(double a, double b) {
        return Math.abs(Math.abs(a) - Math.abs(b));
    }

    
    public double roundToPlace(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    
    public double getKireikoGeneric(final Collection<? extends Number> collection) {
        return (getKurtosis(collection) + getVariance(collection) * 3.0) / 20.0;
    }

    
    public double getPearsonCorrelation(final List<? extends Number> x, final List<? extends Number> y) {
        if (x.size() != y.size() || x.isEmpty()) return 0.0;
        double meanX = getAverage(x);
        double meanY = getAverage(y);
        double sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < x.size(); i++) {
            double dx = x.get(i).doubleValue() - meanX;
            double dy = y.get(i).doubleValue() - meanY;
            sumXY += dx * dy;
            sumX2 += dx * dx;
            sumY2 += dy * dy;
        }
        return sumX2 == 0 || sumY2 == 0 ? 0 : sumXY / Math.sqrt(sumX2 * sumY2);
    }

    
    public double getShannonEntropy(final Collection<? extends Number> data) {
        Map<Double, Long> freq = new HashMap<>();
        for (Number n : data) {
            double v = n.doubleValue();
            freq.put(v, freq.getOrDefault(v, 0L) + 1);
        }
        double total = data.size();
        double entropy = 0.0;
        for (long c : freq.values()) {
            double p = c / total;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    
    public double getRSquared(final List<? extends Number> actual, final List<? extends Number> predicted) {
        if (actual.size() != predicted.size() || actual.isEmpty()) return 0.0;
        double mean = getAverage(actual);
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < actual.size(); i++) {
            double diffTot = actual.get(i).doubleValue() - mean;
            double diffRes = actual.get(i).doubleValue() - predicted.get(i).doubleValue();
            ssTot += diffTot * diffTot;
            ssRes += diffRes * diffRes;
        }
        return ssTot == 0 ? 0 : 1 - ssRes / ssTot;
    }

    
    public double calculateAimSuspicionIndex(final List<? extends Number> aimDeltas, final List<? extends Number> reactionTimes) {
        double zDeltaSum = 0, zReactionSum = 0;
        double meanD = getAverage(aimDeltas);
        double stdD = getStandardDeviation(aimDeltas);
        for (Number d : aimDeltas) zDeltaSum += stdD == 0 ? 0 : Math.abs((d.doubleValue() - meanD) / stdD);
        double meanR = getAverage(reactionTimes);
        double stdR = getStandardDeviation(reactionTimes);
        for (Number r : reactionTimes) zReactionSum += stdR == 0 ? 0 : Math.abs((r.doubleValue() - meanR) / stdR);
        return zDeltaSum / aimDeltas.size() * 0.6 + zReactionSum / reactionTimes.size() * 0.4;
    }

    
    public List<Double> kalmanFilterPredict(final List<? extends Number> measurements, double q, double r) {
        int n = measurements.size();
        List<Double> preds = new ArrayList<>(n);
        double est = measurements.get(0).doubleValue();
        double p = 1;
        preds.add(est);
        for (int i = 1; i < n; i++) {
            double pred = est;
            double pPred = p + q;
            double z = measurements.get(i).doubleValue();
            double k = pPred / (pPred + r);
            est = pred + k * (z - pred);
            p = (1 - k) * pPred;
            preds.add(est);
        }
        return preds;
    }

    
    public List<Float> getJiffDelta(List<? extends Number> data, int depth) {
        List<Float> result = new ArrayList<>();
        for (Number n : data) result.add(n.floatValue());
        for (int d = 0; d < depth; d++) {
            List<Float> next = new ArrayList<>();
            float prev = result.get(0);
            for (int i = 1; i < result.size(); i++) {
                float cur = result.get(i);
                next.add(Math.abs(Math.abs(cur) - Math.abs(prev)));
                prev = cur;
            }
            result = next;
        }
        return result;
    }

    
    public List<Double> getRanks(List<? extends Number> data) {
        List<Double> sorted = new ArrayList<>();
        for (Number n : data) sorted.add(n.doubleValue());
        Collections.sort(sorted);
        List<Double> ranks = new ArrayList<>();
        for (Number n : data) ranks.add((double) (sorted.indexOf(n.doubleValue()) + 1));
        return ranks;
    }

    
    public int getDistinct(final Collection<? extends Number> data) {
        Set<Number> set = new HashSet<>(data);
        return set.size();
    }

    
    public double getDistanceBetweenAngles360Raw(double alpha, double beta) {
        return Math.abs(alpha % 360 - beta % 360);
    }

    public float getGCDValue(double s) {
        return getGCD(s) * 0.15F;
    }

    public float getGCD(double s) {
        float f1 = (float) ((float) s * 0.6 + 0.2);
        return f1 * f1 * f1 * 8.0F;
    }

    
    public Tuple<List<Double>, List<Double>> getAnalyzeOutliers(final Collection<? extends Number> collection) {
        return getOutliers(collection);
    }

    public int mojangFloor(double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    public boolean equal(double first, double second) {
        return Math.abs(second - first) < 1.0E-5F;
    }

    public double calculateTruePercentage(List<Boolean> booleanList) {
        int trueCount = 0;
        for (Boolean bool : booleanList) {
            if (bool) {
                trueCount++;
            }
        }
        return (double) trueCount / booleanList.size() * 100;
    }

    public double getRollingStdDev(List<Integer> data, int window) {
        if (data.size() < window || window < 1) return 0.0;

        double sum = 0.0;
        double squareSum = 0.0;

        for (int i = 0; i < window; i++) {
            sum += data.get(i);
            squareSum += Math.pow(data.get(i), 2);
        }

        double maxStd = 0.0;
        for (int i = window; i <= data.size(); i++) {
            double variance = (squareSum - Math.pow(sum, 2) / window) / window;
            double std = Math.sqrt(variance);
            if (std > maxStd) maxStd = std;

            if (i < data.size()) {
                sum += data.get(i) - data.get(i - window);
                squareSum += Math.pow(data.get(i), 2) - Math.pow(data.get(i - window), 2);
            }
        }
        return maxStd;
    }

    public double getMovingAverage(List<Double> data, int window) {
        if (data.size() < window) return 0.0;
        double sum = 0.0;
        for (int i = data.size() - window; i < data.size(); i++) {
            sum += data.get(i);
        }
        return sum / window;
    }

    public double getStabilityIndex(List<Integer> data) {
        double sum = 0.0;
        for (int i = 1; i < data.size(); i++) {
            sum += Math.abs(data.get(i) - data.get(i - 1));
        }
        double avgDiff = sum / (data.size() - 1);
        double stdDev = getStandardDeviation(data);
        return 1.0 / (1.0 + avgDiff + stdDev / 2.0);
    }

    public double getEntropy(List<Double> window) {
        Map<String, Integer> directionCount = new HashMap<>();
        directionCount.put("flat", 0);
        directionCount.put("up", 0);
        directionCount.put("down", 0);

        for (int i = 1; i < window.size(); i++) {
            double diff = window.get(i) - window.get(i - 1);
            if (Math.abs(diff) < 5) {
                directionCount.put("flat", directionCount.get("flat") + 1);
            } else if (diff > 0) {
                directionCount.put("up", directionCount.get("up") + 1);
            } else {
                directionCount.put("down", directionCount.get("down") + 1);
            }
        }

        double entropy = 0.0;
        int total = window.size() - 1;
        for (int count : directionCount.values()) {
            if (count == 0) continue;
            double p = (double) count / total;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    public double getMicroChangeEntropy(List<Double> data) {
        Map<Integer, Integer> changeFreq = new HashMap<>();
        for (int i = 1; i < data.size(); i++) {
            int delta = (int) Math.round(data.get(i) - data.get(i - 1));
            changeFreq.put(delta, changeFreq.getOrDefault(delta, 0) + 1);
        }

        double entropy = 0.0;
        for (int count : changeFreq.values()) {
            double p = (double) count / (data.size() - 1);
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    public float calculateVarianceFloat(List<Float> data) {
        if (data.size() < 2) return 0;

        double mean = calculateMean(data);
        double sum = 0;
        for (float num : data) {
            sum += Math.pow(num - mean, 2);
        }
        return (float) (sum / (data.size() - 1)); 
    }

    public double calculateVarianceDouble(List<Double> values) {
        if (values.size() < 2) return 0;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double sum = 0.0;
        for (Double v : values) {
            double pow = Math.pow(v - mean, 2);
            sum += pow;
        }
        return sum / values.size();
    }


    
    public float calculateRange(List<Float> data) {
        float max = Collections.max(data);
        float min = Collections.min(data);
        return max - min;
    }

    public float calculateMean(List<Float> data) {
        return (float) data.stream().mapToDouble(Float::doubleValue).average().orElse(0);
    }

    public double calculateAcceleration(Deque<Long> timestamps) {
        if (timestamps.size() < 3) {
            return 0;
        }

        long[] intervals = new long[timestamps.size() - 1];
        long prev = -1;
        int i = 0;
        for (Long ts : timestamps) {
            if (prev != -1) {
                intervals[i++] = ts - prev;
            }
            prev = ts;
        }

        double acceleration = 0;
        for (int j = 1; j < intervals.length; j++) {
            long deltaTime = intervals[j] - intervals[j - 1];
            if (deltaTime > 0) {
                acceleration += (deltaTime) / (double) deltaTime;
            }
        }

        return acceleration / (intervals.length - 1);
    }

    public double calculateStandardDeviation(Deque<Long> timestamps) {
        if (timestamps.size() < 2) {
            return 0;
        }
        int count = timestamps.size() - 1;
        double[] intervals = new double[count];
        Long prev = null;
        int i = 0;
        for (Long ts : timestamps) {
            if (prev != null) {
                intervals[i++] = ts - prev;
            }
            prev = ts;
        }
        double sum = 0;
        for (double interval : intervals) {
            sum += interval;
        }
        double mean = sum / count;
        double variance = 0;
        for (double interval : intervals) {
            variance += Math.pow(interval - mean, 2);
        }
        return Math.sqrt(variance / count);
    }

    public double calculateMeanInterval(Deque<Long> timestamps) {
        if (timestamps.size() < 2) {
            return 0;
        }
        int count = timestamps.size() - 1;
        double sum = 0;
        Long prev = null;
        for (Long ts : timestamps) {
            if (prev != null) {
                sum += (ts - prev);
            }
            prev = ts;
        }
        return sum / count;
    }

    public float getDistanceBetweenAngles(float angle1, float angle2) {
        float distance = Math.abs(angle1 - angle2) % 360.0f;
        if (distance > 180.0f) {
            distance = 360.0f - distance;
        }
        return distance;
    }

    public double calculateVariance(double[] data) {
        if (data.length == 0) return 0;

        double mean = 0;
        for (double value : data) {
            mean += value;
        }
        mean /= data.length;

        double variance = 0;
        for (double value : data) {
            variance += Math.pow(value - mean, 2);
        }
        return variance / data.length;
    }

    
    public double log2(double x) {
        
        if (Double.isNaN(x) || x <= 0) return Double.NaN;
        if (x == 1.0) return 0.0;

        
        if (x > CACHE_STEP && x <= 1.0) {
            int index = (int) ((x - CACHE_STEP) / CACHE_STEP);
            return LOG2_CACHE[index];
        }

        
        return Math.log(x) * INV_LN_2;
    }

    
    public int log2(int n) {
        if (n <= 0) return Integer.MIN_VALUE;
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    
    public double calculateEntropy(double[] data, int bins, boolean normalize, double logBase) {
        if (data == null || data.length == 0) return 0.0;

        double[] processedData = processData(data, bins, normalize);
        return calculateShannonEntropy(processedData, logBase);
    }

    
    public double calculateJointEntropy(double[] dataX, double[] dataY,
                                        int binsX, int binsY, double logBase) {
        if (dataX == null || dataY == null || dataX.length != dataY.length)
            throw new IllegalArgumentException("输入数据无效");

        double[] processedX = processData(dataX, binsX, true);
        double[] processedY = processData(dataY, binsY, true);

        Map<String, Integer> jointCount = new HashMap<>();
        int total = processedX.length;

        for (int i = 0; i < total; i++) {
            String key = processedX[i] + "," + processedY[i];
            jointCount.put(key, jointCount.getOrDefault(key, 0) + 1);
        }

        double entropy = 0.0;
        double logBaseFactor = 1.0 / Math.log(logBase);

        for (int count : jointCount.values()) {
            double p = (double) count / total;
            entropy -= p * (Math.log(p) * logBaseFactor);
        }

        return entropy;
    }

    
    public double calculateConditionalEntropy(double[] dataX, double[] dataY,
                                              int binsX, int binsY, double logBase) {
        double jointEntropy = calculateJointEntropy(dataX, dataY, binsX, binsY, logBase);
        double entropyX = calculateEntropy(dataX, binsX, true, logBase);
        return jointEntropy - entropyX;
    }

    
    public double calculateKLDivergence(double[] p, double[] q, double logBase) {
        if (p.length != q.length)
            throw new IllegalArgumentException("分布长度必须相同");

        double divergence = 0.0;
        double logBaseFactor = 1.0 / Math.log(logBase);

        for (int i = 0; i < p.length; i++) {
            if (p[i] > 0 && q[i] > 0) {
                divergence += p[i] * (Math.log(p[i] / q[i]) * logBaseFactor);
            }
        }

        return divergence;
    }


    private double[] processData(double[] data, int bins, boolean normalize) {
        if (bins <= 0) return data;

        double[] processed = Arrays.copyOf(data, data.length);

        if (normalize) {
            normalizeData(processed);
        }

        return discretizeData(processed, bins);
    }

    private void normalizeData(double[] data) {
        double min = Arrays.stream(data).min().orElse(0);
        double max = Arrays.stream(data).max().orElse(1);
        double range = max - min;

        if (range == 0) return; 

        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] - min) / range;
        }
    }

    private double[] discretizeData(double[] data, int bins) {
        double[] discretized = new double[data.length];
        double binSize = 1.0 / bins;

        for (int i = 0; i < data.length; i++) {
            discretized[i] = Math.floor(data[i] / binSize);
        }

        return discretized;
    }

    
    private double calculateShannonEntropy(double[] data, double logBase) {
        Map<Double, Integer> frequencyMap = new HashMap<>();
        int total = data.length;

        
        for (double value : data) {
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }

        
        double entropy = 0.0;
        double logBaseFactor = 1.0 / Math.log(logBase);

        for (int count : frequencyMap.values()) {
            double probability = (double) count / total;
            entropy -= probability * (Math.log(probability) * logBaseFactor);
        }

        return entropy;
    }

    public double calculateEntropy(List<Float> data, int bins) {
        if (data.isEmpty()) return 0.0;

        
        float min = Collections.min(data);
        float max = Collections.max(data);
        float range = max - min;

        
        if (range == 0) return 0.0;

        
        int[] binCounts = new int[bins];
        float binSize = range / bins;

        for (float value : data) {
            int binIndex = (int) ((value - min) / binSize);
            
            if (binIndex >= bins) binIndex = bins - 1;
            binCounts[binIndex]++;
        }

        
        double entropy = 0.0;
        int total = data.size();

        for (int count : binCounts) {
            if (count > 0) {
                double probability = (double) count / total;
                entropy -= probability * (Math.log(probability) / Math.log(2));
            }
        }

        return entropy;
    }

    public double calculatePatternConsistency(List<Float> data) {
        int sameSignCount = 0;
        for (int i = 1; i < data.size(); i++) {
            if (Math.signum(data.get(i)) == Math.signum(data.get(i - 1))) {
                sameSignCount++;
            }
        }
        return (double) sameSignCount / (data.size() - 1);
    }


    
    public float getAngleDifference(final float a, final float b) {
        return wrapAngleTo180_float(wrapAngleTo180_float(a) - wrapAngleTo180_float(b));
    }

    public float getGCDValueStatistic(double s) {
        return getGCD(s) * 0.15F;
    }

    public double calculateNEntropy(List<Float> data) {
        Map<String, Long> map = new HashMap<>();
        for (Float v : data) {
            map.merge(String.format("%.1f", v), 1L, Long::sum);
        }
        double sum = 0.0;
        for (Long c : map
                .values()) {
            double p = (double) c / data.size();
            double v = -p * (Math.log(p) / Math.log(2));
            sum += v;
        }
        return sum;
    }

    public float wrapAngleTo180_float(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

    public double getOscillation(Iterable<? extends Number> samples) {
        return highest(samples) - lowest(samples);
    }

    
    public double pearsonCorrelation(double[] x, double[] y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("输入数组不能为null");
        }
        if (x.length != y.length) {
            throw new IllegalArgumentException("数组长度必须相等");
        }
        if (x.length < 2) {
            throw new IllegalArgumentException("数组长度至少为2");
        }

        int n = x.length;
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;

        
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        
        double numerator = sumXY - (sumX * sumY / n);
        double denominatorX = Math.sqrt(sumX2 - (sumX * sumX / n));
        double denominatorY = Math.sqrt(sumY2 - (sumY * sumY / n));

        
        if (denominatorX == 0 || denominatorY == 0) {
            return 0.0;
        }

        return numerator / (denominatorX * denominatorY);
    }

    
    public double entropy(List<Double> probabilities) {
        double entropy = 0.0;
        for (double p : probabilities) {
            if (p > 0) {
                entropy -= p * (Math.log(p) / Math.log(2)); 
            }
        }
        return entropy;
    }

    public double getAngleInDegrees(Vec2f delta) {
        double angleInRadians = Math.atan2(delta.getX(), delta.getY());
        double angleInDegrees = Math.toDegrees(angleInRadians);

        if (angleInDegrees < 0) {
            angleInDegrees += 360;
        }

        return angleInDegrees;
    }

    public float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public double variance(Collection<? extends Number> collection) {
        double mean = MathUtil.getAverage(collection);
        double variance = 0.0;
        for (Number num : collection) {
            variance += Math.pow(num.doubleValue() - mean, 2);
        }
        return variance / collection.size();
    }
}
