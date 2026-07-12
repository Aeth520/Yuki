package cn.aetheris.yuki.math;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;

public class GenerateBigRate {

    public static Vector3d generateVector3d() {
        return new Vector3d(generateInvalidDouble(), generateInvalidDouble(), generateInvalidDouble());
    }

    public static Vector3f generateVector3f() {
        return new Vector3f(generateInvalidFloat(), generateInvalidFloat(), generateInvalidFloat());
    }

    public static double generateInvalidDouble() {
        return Double.MAX_VALUE * Math.random();
    }

    public static float generateInvalidFloat() {
        return Float.MAX_VALUE * (float) Math.random();
    }

    public static byte generateFlags() {
        return (byte) (0xFF * Math.random());
    }

    public static int generateTeleportId() {
        return (int) (Integer.MAX_VALUE * Math.random());
    }
}
