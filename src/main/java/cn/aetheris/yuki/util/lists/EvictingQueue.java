
package cn.aetheris.yuki.util.lists;

import java.util.ArrayList;
import java.util.Objects;

public class EvictingQueue<K> extends ArrayList<K> {
    private final int maxSize;

    public EvictingQueue(int size) {
        this.maxSize = Math.max(1, size); 
    }

    @Override
    public boolean add(K k) {
        boolean result = super.add(Objects.requireNonNull(k));
        while (size() > maxSize) {
            super.remove(0);
        }
        return result;
    }

    public K getYoungest() {
        return isEmpty() ? null : get(size() - 1);
    }

    public K peek(int index) {
        return (index >= 0 && index < size()) ? get(index) : null;
    }

    public K safePoll() {
        return isEmpty() ? null : remove(0);
    }
}
