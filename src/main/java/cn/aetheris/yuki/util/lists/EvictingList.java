package cn.aetheris.yuki.util.lists;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;


@Getter
@Setter
public final class EvictingList<T> extends UnsafeLinkedList<T> {

    double lastAvg = Double.NaN;
    private int maxSize;
    private double lastVariance = 0.01;


    public EvictingList(final int maxSize) {
        this.maxSize = maxSize;
    }


    public EvictingList(final Collection<? extends T> c, final int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T item) {
        if (item instanceof Number) {
            updateVariance(((Number) item).doubleValue());
        }
        boolean add = super.add(item);
        this.trimToMaxSize();
        return add;
    }

    public boolean isFull() {
        return this.size() >= this.getMaxSize();
    }

    
    private boolean isFilled(EvictingList<?> list,
                             int targetSize,
                             double ratioNeeded) {
        if (list == null || targetSize <= 0) return false;
        int current = list.size();
        return current >= (int) Math.ceil(targetSize * ratioNeeded);
    }

    
    private boolean isFilled(EvictingList<?> list, int targetSize) {
        return isFilled(list, targetSize, 0.8);
    }


    public double average() {
        if (isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        long count = 0;
        for (T n : this) {
            if (n instanceof Number) {
                double v = ((Number) n).doubleValue();
                sum += v;
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    public List<T> getAllValues() {
        return isEmpty() ? null : new LinkedList<>(this);
    }

    public List<Double> getAllValuesAsDouble() {
        List<Double> list = new LinkedList<>();
        for (T n : this) {
            if (n instanceof Number) {
                Double doubleValue = ((Number) n).doubleValue();
                list.add(doubleValue);
            }
        }
        return list;
    }

    private void trimToMaxSize() {
        while (size() > maxSize) {
            removeFirst();
        }
    }

    public List<T> last(int count) {
        int start = Math.max(0, size() - count);
        return subList(start, size());
    }

    public List<T> asList() {
        return new LinkedList<>(this);
    }

    public Double getLatest() {
        if (isEmpty()) return null;
        return (Double) getLast();
    }

    public void addAllWithEvict(Collection<? extends T> c) {
        this.addAll(c);
    }

    private void updateVariance(double newValue) {
        if (size() == 0) {
            lastVariance = 0.01;
            return;
        }
        boolean naN = Double.isNaN(lastAvg);
        if (naN) {
            updateAvg(true, newValue);
        }
        double oldMean = lastAvg;

        double newMean = oldMean + (newValue - oldMean) / (size() + 1);
        lastVariance += (newValue - oldMean) * (newValue - newMean);
        if (!naN) {
            updateAvg(false, newValue);
        }
    }

    void updateAvg(boolean firstCheck, double newValue) {
        if (firstCheck) {
            this.lastAvg = average();
        } else {
            double avg = lastAvg * size();
            avg += newValue;
            this.lastAvg = avg / (size() + 1);
        }
    }

    public void evictIf(Predicate<? super T> predicate) {
        this.removeIf(predicate);
    }
}
