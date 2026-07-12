package cn.aetheris.yuki.math;

public final class VanillaMath {

    private static final float[] SIN = new float[65536];

    public VanillaMath() {
    }

    public static float sin(float f) {
        return SIN[(int) (f * 10430.378f) & 0xFFFF];
    }

    public static float cos(float f) {
        return SIN[(int) (f * 10430.378f + 16384.0f) & 0xFFFF];
    }

    public static void register() {
        for (int i = 0; i < SIN.length; ++i) {
            SIN[i] = (float) StrictMath.sin(i * Math.PI * 2d / 65536d);
        }
    }
}
