package cn.aetheris.yuki.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class Pair2<X, Y> {

    private X x;
    private Y y;
}