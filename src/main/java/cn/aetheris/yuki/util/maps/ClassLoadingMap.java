package cn.aetheris.yuki.util.maps;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.util.function.Consumer;
import java.util.function.Function;


public class ClassLoadingMap<T> extends Object2ObjectLinkedOpenHashMap<Class<? extends T>, T> {
    Function<Class<? extends T>, T> classTFunction;

    public ClassLoadingMap(Function<Class<? extends T>, T> ctF) {
        super();
        this.classTFunction = ctF;
    }

    public void forEachValue(Consumer<? super T> consumer) {
        long[] link = this.link;
        T[] value = this.value;
        int i = this.size;
        int next = this.first;

        while (i-- != 0) {
            int curr = next;
            next = (int) link[curr];
            consumer.accept(value[curr]);
        }
    }

    @Override
    public T put(Class<? extends T> tClass, T t) {
        if (t == null) {
            if (classTFunction != null) {
                t = classTFunction.apply(tClass);
            }
        }
        return super.put(tClass, t);
    }
}
