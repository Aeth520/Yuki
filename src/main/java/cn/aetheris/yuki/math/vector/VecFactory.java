package cn.aetheris.yuki.math.vector;

public class VecFactory {
    public static IVector vecWith(IVector vector) {
        if (DoubleVectorBridge.isCanUse()) {
            return DoubleVectorBridge.vecWith(vector);
        } else {
            return new CompatibleVector3(vector.getX(), vector.getY(), vector.getZ());
        }
    }

    public static IVector vecWith(double... pos) {
        if (DoubleVectorBridge.isCanUse()) {
            return new SimdVector3(DoubleVectorBridge.vecWith(pos));
        } else {
            return new CompatibleVector3(pos[0], pos[1], pos[2]);
        }
    }

}
