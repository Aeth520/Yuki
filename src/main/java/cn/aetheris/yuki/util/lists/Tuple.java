package cn.aetheris.yuki.util.lists;

import lombok.Getter;

@Getter
public class Tuple<X, Y> {
    private final X x;
    private final Y y;

    public Tuple(X var1, Y var2) {
        this.x = var1;
        this.y = var2;
    }

}
