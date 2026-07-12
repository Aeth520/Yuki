package cn.aetheris.yuki.data.util;

import java.util.Objects;

public record Pair<A, B>(A first, B second) {

    public static <T, K> Pair<T, K> of(T a, K b) {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair b)) {
            return false;
        }
        return Objects.equals(this.first, b.first) && Objects.equals(this.second, b.second);
    }
}
