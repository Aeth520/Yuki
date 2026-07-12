package cn.aetheris.yuki.math.vector;

import jdk.incubator.vector.DoubleVector;
import lombok.Getter;

public class DoubleVectorBridge {
    @Getter
    static boolean canUse = false;

    static {
        try {
            var b = DoubleVector.SPECIES_PREFERRED;
            
            canUse = true;
        } catch (NoClassDefFoundError e) {
            canUse = false;

            
        }
    }

    public static DoubleVector vecWith(double... vecs) {
        return zero().withLane(0, vecs[0]).withLane(1, vecs[1]).withLane(2, vecs[2]);
    }

    public static DoubleVector zero() {
        return DoubleVector.zero(DoubleVector.SPECIES_PREFERRED);
    }

    public static SimdVector3 vecWith(IVector vector) {
        return new SimdVector3(vecWith(vector.getX(), vector.getY(), vector.getZ()));
    }
}
