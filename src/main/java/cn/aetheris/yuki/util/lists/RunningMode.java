package cn.aetheris.yuki.util.lists;

import cn.aetheris.yuki.data.util.Pair;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class RunningMode {
    private static final double threshold = 1e-3;
    private final Queue<Double> addList;
    private final Double2IntMap popularityMap;

    @Getter
    private final int maxSize;

    public RunningMode(int maxSize) {
        if (maxSize <= 0) throw new IllegalArgumentException("列表的大小必须大于0！");
        this.addList = new ArrayBlockingQueue<>(maxSize);
        this.popularityMap = new Double2IntOpenHashMap();
        this.maxSize = maxSize;
    }

    public int size() {
        return addList.size();
    }

    public void add(double value) {
        if (addList.size() >= maxSize) {
            removeOldest();
        }

        for (Double2IntMap.Entry entry : popularityMap.double2IntEntrySet()) {
            if (Math.abs(entry.getDoubleKey() - value) < threshold) {
                entry.setValue(entry.getIntValue() + 1);
                addList.add(entry.getDoubleKey());
                return;
            }
        }

        popularityMap.put(value, 1);
        addList.add(value);
    }

    private void removeOldest() {
        double oldest = addList.poll();
        int count = popularityMap.get(oldest);

        if (count == 1) {
            popularityMap.remove(oldest);
        } else {
            popularityMap.put(oldest, count - 1);
        }
    }

    public Pair<Double, Integer> getMode() {
        int maxFrequency = 0;
        Double modeValue = null;

        for (Double2IntMap.Entry entry : popularityMap.double2IntEntrySet()) {
            if (entry.getIntValue() > maxFrequency) {
                maxFrequency = entry.getIntValue();
                modeValue = entry.getDoubleKey();
            }
        }

        return new Pair<>(modeValue, maxFrequency);
    }
}
