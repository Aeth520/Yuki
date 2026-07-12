

package cn.aetheris.yuki.util.maps;

/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;


@SuppressWarnings("all")
public class ConcurrentReferenceHashMap<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K, V>, Serializable {

    

    static final ReferenceType DEFAULT_KEY_TYPE = ReferenceType.WEAK;

    ;
    static final ReferenceType DEFAULT_VALUE_TYPE = ReferenceType.STRONG;

    ;

    
    
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    
    static final int MAXIMUM_CAPACITY = 1 << 30;
    
    static final int MAX_SEGMENTS = 1 << 16;
    
    static final int RETRIES_BEFORE_LOCK = 2;
    private static final long serialVersionUID = 7249069246763182397L;
    
    final int segmentMask;
    
    final int segmentShift;

    
    
    final Segment<K, V>[] segments;
    boolean identityComparisons;
    transient Set<K> keySet;
    transient Set<Map.Entry<K, V>> entrySet;
    transient Collection<V> values;

    
    public ConcurrentReferenceHashMap(int initialCapacity,
                                      float loadFactor, int concurrencyLevel,
                                      ReferenceType keyType, ReferenceType valueType,
                                      EnumSet<Option> options) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0) {
            throw new IllegalArgumentException();
        }
        if (concurrencyLevel > MAX_SEGMENTS) {
            concurrencyLevel = MAX_SEGMENTS;
        }
        
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;
        this.segments = Segment.newArray(ssize);
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity) {
            ++c;
        }
        int cap = 1;
        while (cap < c) {
            cap <<= 1;
        }
        identityComparisons = options != null && options.contains(Option.IDENTITY_COMPARISONS);
        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i] = new Segment<K, V>(cap, loadFactor, keyType, valueType, identityComparisons);
        }
    }

    
    public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this(initialCapacity, loadFactor, concurrencyLevel, DEFAULT_KEY_TYPE, DEFAULT_VALUE_TYPE, null);
    }

    

    
    public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }

    
    public ConcurrentReferenceHashMap(int initialCapacity,
                                      ReferenceType keyType, ReferenceType valueType) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL,
                keyType, valueType, null);
    }

    
    public ConcurrentReferenceHashMap(ReferenceType keyType, ReferenceType valueType) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL,
                keyType, valueType, null);
    }

    

    
    public ConcurrentReferenceHashMap(ReferenceType keyType, ReferenceType valueType, EnumSet<Option> options) {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL,
                keyType, valueType, options);
    }

    
    public ConcurrentReferenceHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    
    public ConcurrentReferenceHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    
    public ConcurrentReferenceHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                        DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL
        );
        putAll(m);
    }

    
    private static int hash(int h) {
        
        
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        return h ^ (h >>> 16);
    }

    
    private static <T> T checkNotNull(T argument) {
        if (argument == null) {
            throw new NullPointerException();
        }
        return argument;
    }

    
    final Segment<K, V> segmentFor(int hash) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }

    

    protected int hashOf(Object key) {
        return hash(identityComparisons ? System.identityHashCode(key) : key.hashCode());
    }

    
    public boolean isEmpty() {
        final Segment<K, V>[] segments = this.segments;
        
        int[] mc = new int[segments.length];
        int mcsum = 0;
        for (int i = 0; i < segments.length; ++i) {
            if (segments[i].count != 0) {
                return false;
            } else {
                mcsum += mc[i] = segments[i].modCount;
            }
        }
        
        
        
        if (mcsum != 0) {
            for (int i = 0; i < segments.length; ++i) {
                if (segments[i].count != 0 || mc[i] != segments[i].modCount) {
                    return false;
                }
            }
        }
        return true;
    }

    
    public int size() {
        final Segment<K, V>[] segments = this.segments;
        long sum = 0;
        long check = 0;
        int[] mc = new int[segments.length];
        
        
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            check = 0;
            sum = 0;
            int mcsum = 0;
            for (int i = 0; i < segments.length; ++i) {
                sum += segments[i].count;
                mcsum += mc[i] = segments[i].modCount;
            }
            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    check += segments[i].count;
                    if (mc[i] != segments[i].modCount) {
                        
                        check = -1;
                        break;
                    }
                }
            }
            if (check == sum) {
                break;
            }
        }
        if (check != sum) {
            
            sum = 0;
            for (int i = 0; i < segments.length; ++i) {
                segments[i].lock();
            }
            for (int i = 0; i < segments.length; ++i) {
                sum += segments[i].count;
            }
            for (int i = 0; i < segments.length; ++i) {
                segments[i].unlock();
            }
        }
        return sum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
    }

    
    public V get(Object key) {
        int hash = hashOf(key);
        return segmentFor(hash).get(key, hash);
    }

    
    public boolean containsKey(Object key) {
        int hash = hashOf(key);
        return segmentFor(hash).containsKey(key, hash);
    }

    
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        
        final Segment<K, V>[] segments = this.segments;
        int[] mc = new int[segments.length];
        
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            int sum = 0;
            int mcsum = 0;
            for (int i = 0; i < segments.length; ++i) {
                int c = segments[i].count;
                mcsum += mc[i] = segments[i].modCount;
                if (segments[i].containsValue(value)) {
                    return true;
                }
            }
            boolean cleanSweep = true;
            if (mcsum != 0) {
                for (int i = 0; i < segments.length; ++i) {
                    int c = segments[i].count;
                    if (mc[i] != segments[i].modCount) {
                        cleanSweep = false;
                        break;
                    }
                }
            }
            if (cleanSweep) {
                return false;
            }
        }
        
        for (int i = 0; i < segments.length; ++i) {
            segments[i].lock();
        }
        boolean found = false;
        try {
            for (int i = 0; i < segments.length; ++i) {
                if (segments[i].containsValue(value)) {
                    found = true;
                    break;
                }
            }
        } finally {
            for (int i = 0; i < segments.length; ++i) {
                segments[i].unlock();
            }
        }
        return found;
    }

    
    public boolean contains(Object value) {
        return containsValue(value);
    }

    
    public V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hashOf(key);
        return segmentFor(hash).put(key, hash, value, null, false);
    }

    
    public V putIfAbsent(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hashOf(key);
        return segmentFor(hash).put(key, hash, value, null, true);
    }

    
    public V applyIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        checkNotNull(key);
        checkNotNull(mappingFunction);

        int hash = hashOf(key);
        Segment<K, V> segment = segmentFor(hash);
        V v = segment.get(key, hash);
        return v == null ? segment.put(key, hash, null, mappingFunction, true) : v;
    }

    public V applyIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        checkNotNull(key);
        checkNotNull(remappingFunction);

        int hash = hashOf(key);
        Segment<K, V> segment = segmentFor(hash);
        V v = segment.get(key, hash);
        if (v == null) {
            return null;
        }

        return segmentFor(hash).applyIfPresent(key, hash, remappingFunction);
    }

    public V apply(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        checkNotNull(key);
        checkNotNull(remappingFunction);

        int hash = hashOf(key);
        Segment<K, V> segment = segmentFor(hash);
        return segment.apply(key, hash, remappingFunction);
    }

    
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    
    public V remove(Object key) {
        int hash = hashOf(key);
        return segmentFor(hash).remove(key, hash, null, false);
    }

    
    public boolean remove(Object key, Object value) {
        int hash = hashOf(key);
        if (value == null) {
            return false;
        }
        return segmentFor(hash).remove(key, hash, value, false) != null;
    }

    
    public boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null || newValue == null) {
            throw new NullPointerException();
        }
        int hash = hashOf(key);
        return segmentFor(hash).replace(key, hash, oldValue, newValue);
    }

    
    public V replace(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = hashOf(key);
        return segmentFor(hash).replace(key, hash, value);
    }

    
    public void clear() {
        for (int i = 0; i < segments.length; ++i) {
            segments[i].clear();
        }
    }

    
    public void purgeStaleEntries() {
        for (int i = 0; i < segments.length; ++i) {
            segments[i].removeStale();
        }
    }

    
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    
    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet(false));
    }

    public Set<Map.Entry<K, V>> cachedEntrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet(true));
    }

    
    public Enumeration<K> keys() {
        return new KeyIterator();
    }

    
    public Enumeration<V> elements() {
        return new ValueIterator();
    }

    
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        for (int k = 0; k < segments.length; ++k) {
            Segment<K, V> seg = segments[k];
            seg.lock();
            try {
                HashEntry<K, V>[] tab = seg.table;
                for (int i = 0; i < tab.length; ++i) {
                    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        K key = e.key();
                        
                        if (key == null) {
                            continue;
                        }
                        s.writeObject(key);
                        s.writeObject(e.value());
                    }
                }
            } finally {
                seg.unlock();
            }
        }
        s.writeObject(null);
        s.writeObject(null);
    }

    
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        
        for (int i = 0; i < segments.length; ++i) {
            segments[i].setTable(new HashEntry[1]);
        }

        
        while (true) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            if (key == null) {
                break;
            }
            put(key, value);
        }
    }


    
    public static enum ReferenceType {
        
        STRONG,
        
        WEAK,
        
        SOFT
    }

    
    public static enum Option {
        
        IDENTITY_COMPARISONS
    }

    interface KeyReference {
        int keyHash();

        Object keyRef();
    }

    
    static final class WeakKeyReference<K> extends WeakReference<K> implements KeyReference {
        final int hash;

        WeakKeyReference(K key, int hash, ReferenceQueue<Object> refQueue) {
            super(key, refQueue);
            this.hash = hash;
        }

        public final int keyHash() {
            return hash;
        }

        public final Object keyRef() {
            return this;
        }
    }

    
    static final class SoftKeyReference<K> extends SoftReference<K> implements KeyReference {
        final int hash;

        SoftKeyReference(K key, int hash, ReferenceQueue<Object> refQueue) {
            super(key, refQueue);
            this.hash = hash;
        }

        public final int keyHash() {
            return hash;
        }

        public final Object keyRef() {
            return this;
        }
    }

    static final class WeakValueReference<V> extends WeakReference<V> implements KeyReference {
        final Object keyRef;
        final int hash;

        WeakValueReference(V value, Object keyRef, int hash, ReferenceQueue<Object> refQueue) {
            super(value, refQueue);
            this.keyRef = keyRef;
            this.hash = hash;
        }

        public final int keyHash() {
            return hash;
        }

        public final Object keyRef() {
            return keyRef;
        }
    }

    

    static final class SoftValueReference<V> extends SoftReference<V> implements KeyReference {
        final Object keyRef;
        final int hash;

        SoftValueReference(V value, Object keyRef, int hash, ReferenceQueue<Object> refQueue) {
            super(value, refQueue);
            this.keyRef = keyRef;
            this.hash = hash;
        }

        public final int keyHash() {
            return hash;
        }

        public final Object keyRef() {
            return keyRef;
        }
    }

    
    static final class HashEntry<K, V> {
        final Object keyRef;
        final int hash;
        final HashEntry<K, V> next;
        volatile Object valueRef;

        HashEntry(K key, int hash, HashEntry<K, V> next, V value,
                  ReferenceType keyType, ReferenceType valueType,
                  ReferenceQueue<Object> refQueue) {
            this.hash = hash;
            this.next = next;
            this.keyRef = newKeyReference(key, keyType, refQueue);
            this.valueRef = newValueReference(value, valueType, refQueue);
        }

        @SuppressWarnings("unchecked")
        static final <K, V> HashEntry<K, V>[] newArray(int i) {
            return new HashEntry[i];
        }

        final Object newKeyReference(K key, ReferenceType keyType,
                                     ReferenceQueue<Object> refQueue) {
            if (keyType == ReferenceType.WEAK) {
                return new WeakKeyReference<K>(key, hash, refQueue);
            }
            if (keyType == ReferenceType.SOFT) {
                return new SoftKeyReference<K>(key, hash, refQueue);
            }

            return key;
        }

        final Object newValueReference(V value, ReferenceType valueType,
                                       ReferenceQueue<Object> refQueue) {
            if (valueType == ReferenceType.WEAK) {
                return new WeakValueReference<V>(value, keyRef, hash, refQueue);
            }
            if (valueType == ReferenceType.SOFT) {
                return new SoftValueReference<V>(value, keyRef, hash, refQueue);
            }

            return value;
        }

        @SuppressWarnings("unchecked")
        final K key() {
            if (keyRef instanceof KeyReference) {
                return ((Reference<K>) keyRef).get();
            }
            return (K) keyRef;
        }

        final V value() {
            return dereferenceValue(valueRef);
        }

        @SuppressWarnings("unchecked")
        final V dereferenceValue(Object value) {
            if (value instanceof KeyReference) {
                return ((Reference<V>) value).get();
            }
            return (V) value;
        }

        final void setValue(V value, ReferenceType valueType, ReferenceQueue<Object> refQueue) {
            this.valueRef = newValueReference(value, valueType, refQueue);
        }
    }

    
    static final class Segment<K, V> extends ReentrantLock implements Serializable {
        

        private static final long serialVersionUID = 2249069246763182397L;
        
        final float loadFactor;
        final ReferenceType keyType;
        final ReferenceType valueType;
        final boolean identityComparisons;
        
        transient volatile int count;
        
        transient int modCount;
        
        transient int threshold;
        
        transient volatile HashEntry<K, V>[] table;
        
        transient volatile ReferenceQueue<Object> refQueue;

        Segment(int initialCapacity, float lf, ReferenceType keyType,
                ReferenceType valueType, boolean identityComparisons) {
            loadFactor = lf;
            this.keyType = keyType;
            this.valueType = valueType;
            this.identityComparisons = identityComparisons;
            setTable(HashEntry.<K, V>newArray(initialCapacity));
        }

        @SuppressWarnings("unchecked")
        static final <K, V> Segment<K, V>[] newArray(int i) {
            return new Segment[i];
        }

        private boolean keyEq(Object src, Object dest) {
            return identityComparisons ? src == dest : src.equals(dest);
        }

        
        void setTable(HashEntry<K, V>[] newTable) {
            threshold = (int) (newTable.length * loadFactor);
            table = newTable;
            refQueue = new ReferenceQueue<Object>();
        }

        
        HashEntry<K, V> getFirst(int hash) {
            HashEntry<K, V>[] tab = table;
            return tab[hash & (tab.length - 1)];
        }

        HashEntry<K, V> newHashEntry(K key, int hash, HashEntry<K, V> next, V value) {
            return new HashEntry<K, V>(key, hash, next, value, keyType, valueType, refQueue);
        }

        
        V readValueUnderLock(HashEntry<K, V> e) {
            lock();
            try {
                removeStale();
                return e.value();
            } finally {
                unlock();
            }
        }

        

        V get(Object key, int hash) {
            
            if (count != 0) {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && keyEq(key, e.key())) {
                        Object opaque = e.valueRef;
                        if (opaque != null) {
                            return e.dereferenceValue(opaque);
                        }
                        
                        return readValueUnderLock(e);
                    }
                    e = e.next;
                }
            }
            return null;
        }

        boolean containsKey(Object key, int hash) {
            
            if (count != 0) {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && keyEq(key, e.key())) {
                        return true;
                    }
                    e = e.next;
                }
            }
            return false;
        }

        boolean containsValue(Object value) {
            
            if (count != 0) {
                HashEntry<K, V>[] tab = table;
                int len = tab.length;
                for (int i = 0; i < len; i++) {
                    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        Object opaque = e.valueRef;
                        V v;
                        if (opaque == null) {
                            
                            v = readValueUnderLock(e);
                        } else {
                            v = e.dereferenceValue(opaque);
                        }
                        if (value.equals(v)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        boolean replace(K key, int hash, V oldValue, V newValue) {
            lock();
            try {
                return replaceInternal2(key, hash, oldValue, newValue);
            } finally {
                unlock();
            }
        }

        private boolean replaceInternal2(K key, int hash, V oldValue, V newValue) {
            removeStale();
            HashEntry<K, V> e = getFirst(hash);
            while (e != null && (e.hash != hash || !keyEq(key, e.key()))) {
                e = e.next;
            }
            boolean replaced = false;
            if (e != null && oldValue.equals(e.value())) {
                replaced = true;
                e.setValue(newValue, valueType, refQueue);
            }
            return replaced;
        }

        V replace(K key, int hash, V newValue) {
            lock();
            try {
                return replaceInternal(key, hash, newValue);
            } finally {
                unlock();
            }
        }

        private V replaceInternal(K key, int hash, V newValue) {
            removeStale();
            HashEntry<K, V> e = getFirst(hash);
            while (e != null && (e.hash != hash || !keyEq(key, e.key()))) {
                e = e.next;
            }
            V oldValue = null;
            if (e != null) {
                oldValue = e.value();
                e.setValue(newValue, valueType, refQueue);
            }
            return oldValue;
        }

        V applyIfPresent(K key, int hash, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            lock();
            try {
                V oldValue = get(key, hash);
                if (oldValue == null) {
                    return null;
                }

                V newValue = remappingFunction.apply(key, oldValue);

                if (newValue == null) {
                    removeInternal(key, hash, oldValue, false);
                    return null;
                } else {
                    putInternal(key, hash, newValue, null, false);
                    return newValue;
                }
            } finally {
                unlock();
            }
        }

        V apply(K key, int hash, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            lock();
            try {
                V oldValue = get(key, hash);
                V newValue = remappingFunction.apply(key, oldValue);

                if (newValue == null) {
                    
                    if (oldValue != null) {
                        
                        removeInternal(key, hash, oldValue, false);
                        return null;
                    } else {
                        
                        return null;
                    }
                } else {
                    
                    putInternal(key, hash, newValue, null, false);
                    return newValue;
                }
            } finally {
                unlock();
            }
        }


        V merge(K key, V value, int hash, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            lock();
            try {
                V oldValue = get(key, hash);
                V newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);

                if (newValue == null) {
                    removeInternal(key, hash, oldValue, false);
                    return null;
                } else {
                    putInternal(key, hash, newValue, null, false);
                    return newValue;
                }
            } finally {
                unlock();
            }
        }

        
        V put(K key, int hash, V value, Function<? super K, ? extends V> function, boolean onlyIfAbsent) {
            lock();
            try {
                return putInternal(key, hash, value, function, onlyIfAbsent);
            } finally {
                unlock();
            }
        }

        private V putInternal(K key, int hash, V value, Function<? super K, ? extends V> function, boolean onlyIfAbsent) {
            removeStale();
            int c = count;
            
            if (c++ > threshold) {
                int reduced = rehash();
                
                if (reduced > 0) {
                    
                    count = (c -= reduced) - 1;
                }
            }
            HashEntry<K, V>[] tab = table;
            int index = hash & (tab.length - 1);
            HashEntry<K, V> first = tab[index];
            HashEntry<K, V> e = first;
            while (e != null && (e.hash != hash || !keyEq(key, e.key()))) {
                e = e.next;
            }
            V resultValue;
            if (e != null) {
                resultValue = e.value();
                if (!onlyIfAbsent) {
                    e.setValue(getValue(key, value, function), valueType, refQueue);
                }
            } else {
                V v = getValue(key, value, function);
                resultValue = function != null ? v : null;

                if (v != null) {
                    ++modCount;
                    tab[index] = newHashEntry(key, hash, first, v);
                    
                    count = c;
                }
            }
            return resultValue;
        }

        V getValue(K key, V value, Function<? super K, ? extends V> function) {
            return value != null ? value : function.apply(key);
        }

        int rehash() {
            HashEntry<K, V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY) {
                return 0;
            }

            

            HashEntry<K, V>[] newTable = HashEntry.newArray(oldCapacity << 1);
            threshold = (int) (newTable.length * loadFactor);
            int sizeMask = newTable.length - 1;
            int reduce = 0;
            for (int i = 0; i < oldCapacity; i++) {
                
                
                HashEntry<K, V> e = oldTable[i];
                if (e != null) {
                    HashEntry<K, V> next = e.next;
                    int idx = e.hash & sizeMask;
                    
                    if (next == null) {
                        newTable[idx] = e;
                    } else {
                        
                        HashEntry<K, V> lastRun = e;
                        int lastIdx = idx;
                        for (HashEntry<K, V> last = next;
                             last != null;
                             last = last.next) {
                            int k = last.hash & sizeMask;
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;
                        
                        for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                            
                            K key = p.key();
                            if (key == null) {
                                reduce++;
                                continue;
                            }
                            int k = p.hash & sizeMask;
                            HashEntry<K, V> n = newTable[k];
                            newTable[k] = newHashEntry(key, p.hash, n, p.value());
                        }
                    }
                }
            }
            table = newTable;
            return reduce;
        }

        
        V remove(Object key, int hash, Object value, boolean refRemove) {
            lock();
            try {
                return removeInternal(key, hash, value, refRemove);
            } finally {
                unlock();
            }
        }

        private V removeInternal(Object key, int hash, Object value, boolean refRemove) {
            if (!refRemove) {
                removeStale();
            }
            int c = count - 1;
            HashEntry<K, V>[] tab = table;
            int index = hash & (tab.length - 1);
            HashEntry<K, V> first = tab[index];
            HashEntry<K, V> e = first;
            
            while (e != null && key != e.keyRef && (refRemove || hash != e.hash || !keyEq(key, e.key()))) {
                e = e.next;
            }

            V oldValue = null;
            if (e != null) {
                V v = e.value();
                if (value == null || value.equals(v)) {
                    oldValue = v;
                    
                    
                    
                    ++modCount;
                    HashEntry<K, V> newFirst = e.next;
                    for (HashEntry<K, V> p = first; p != e; p = p.next) {
                        K pKey = p.key();
                        
                        if (pKey == null) {
                            c--;
                            continue;
                        }
                        newFirst = newHashEntry(pKey, p.hash, newFirst, p.value());
                    }
                    tab[index] = newFirst;
                    
                    count = c;
                }
            }
            return oldValue;
        }

        final void removeStale() {
            KeyReference ref;
            while ((ref = (KeyReference) refQueue.poll()) != null) {
                remove(ref.keyRef(), ref.keyHash(), null, true);
            }
        }

        void clear() {
            if (count != 0) {
                lock();
                try {
                    HashEntry<K, V>[] tab = table;
                    for (int i = 0; i < tab.length; i++) {
                        tab[i] = null;
                    }
                    ++modCount;
                    
                    refQueue = new ReferenceQueue<Object>();
                    
                    count = 0;
                } finally {
                    unlock();
                }
            }
        }
    }

    
    protected static class SimpleEntry<K, V> implements Entry<K, V>, java.io.Serializable {
        private static final long serialVersionUID = -8499721149061103585L;

        protected final K key;
        protected V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(Entry<? extends K, ? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        private static boolean eq(Object o1, Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            Map.Entry e = (Map.Entry) o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
        }

        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }
    }

    protected static class InitializableEntry<K, V> implements Entry<K, V> {
        private K key;
        private V value;

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        public Entry<K, V> init(K key, V value) {
            this.key = key;
            this.value = value;
            return this;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

    protected abstract class HashIterator {
        int nextSegmentIndex;
        int nextTableIndex;
        HashEntry<K, V>[] currentTable;
        HashEntry<K, V> nextEntry;
        HashEntry<K, V> lastReturned;
        
        K currentKey;

        HashIterator() {
            nextSegmentIndex = segments.length - 1;
            nextTableIndex = -1;
            advance();
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        final void advance() {
            if (nextEntry != null && (nextEntry = nextEntry.next) != null) {
                return;
            }
            while (nextTableIndex >= 0) {
                if ((nextEntry = currentTable[nextTableIndex--]) != null) {
                    return;
                }
            }
            while (nextSegmentIndex >= 0) {
                Segment<K, V> seg = segments[nextSegmentIndex--];
                if (seg.count != 0) {
                    currentTable = seg.table;
                    for (int j = currentTable.length - 1; j >= 0; --j) {
                        if ((nextEntry = currentTable[j]) != null) {
                            nextTableIndex = j - 1;
                            return;
                        }
                    }
                }
            }
        }

        public boolean hasNext() {
            while (nextEntry != null) {
                if (nextEntry.key() != null) {
                    return true;
                }
                advance();
            }
            return false;
        }

        HashEntry<K, V> nextEntry() {
            do {
                if (nextEntry == null) {
                    throw new NoSuchElementException();
                }
                lastReturned = nextEntry;
                currentKey = lastReturned.key();
                advance();
            } while  (currentKey == null);
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            ConcurrentReferenceHashMap.this.remove(currentKey);
            lastReturned = null;
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<K>, Enumeration<K> {
        public K next() {
            return super.nextEntry().key();
        }

        public K nextElement() {
            return super.nextEntry().key();
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V>, Enumeration<V> {
        public V next() {
            return super.nextEntry().value();
        }

        public V nextElement() {
            return super.nextEntry().value();
        }
    }

    
    protected class WriteThroughEntry extends SimpleEntry<K, V> {
        private static final long serialVersionUID = -7900634345345313646L;

        protected WriteThroughEntry(K k, V v) {
            super(k, v);
        }

        
        public V setValue(V value) {
            if (value == null) {
                throw new NullPointerException();
            }
            V v = super.setValue(value);
            ConcurrentReferenceHashMap.this.put(getKey(), value);
            return v;
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Entry<K, V>> {
        public Map.Entry<K, V> next() {
            HashEntry<K, V> e = super.nextEntry();
            return new WriteThroughEntry(e.key(), e.value());
        }
    }

    final class CachedEntryIterator extends HashIterator implements Iterator<Entry<K, V>> {
        private InitializableEntry entry = new InitializableEntry();

        public Map.Entry<K, V> next() {
            HashEntry<K, V> e = super.nextEntry();
            return entry.init(e.key(), e.value());
        }
    }

    

    final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }

        public boolean contains(Object o) {
            return ConcurrentReferenceHashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return ConcurrentReferenceHashMap.this.remove(o) != null;
        }

        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }
    }

    final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }

        public boolean contains(Object o) {
            return ConcurrentReferenceHashMap.this.containsValue(o);
        }

        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }
    }

    

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        private final boolean cached;

        public EntrySet(boolean cached) {
            this.cached = cached;
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return cached ? new CachedEntryIterator() : new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            V v = ConcurrentReferenceHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return ConcurrentReferenceHashMap.this.remove(e.getKey(), e.getValue());
        }

        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }

        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }
    }
}