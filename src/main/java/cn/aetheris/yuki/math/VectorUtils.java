package cn.aetheris.yuki.math;

import cn.aetheris.yuki.block.collision.datatypes.SimpleCollisionBox;
import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.util.Vector3d;

public final class VectorUtils {
    public static Vector3dm cutBoxToVector(Vector3dm vectorToCutTo, Vector3dm min, Vector3dm max) {
        SimpleCollisionBox box = new SimpleCollisionBox(min, max).sort();
        return cutBoxToVector(vectorToCutTo, box);
    }

    public static Vector3dm cutBoxToVector(Vector3dm vectorCutTo, SimpleCollisionBox box) {
        return new Vector3dm(MathUtil.clamp(vectorCutTo.getX(), box.minX, box.maxX),
                MathUtil.clamp(vectorCutTo.getY(), box.minY, box.maxY),
                MathUtil.clamp(vectorCutTo.getZ(), box.minZ, box.maxZ));
    }

    
    public static Vector3d clampVector(Vector3d toClamp) {
        double x = MathUtil.clamp(toClamp.getX(), -3.0E7D, 3.0E7D);
        double y = MathUtil.clamp(toClamp.getY(), -2.0E7D, 2.0E7D);
        double z = MathUtil.clamp(toClamp.getZ(), -3.0E7D, 3.0E7D);

        return new Vector3d(x, y, z);
    }
}
