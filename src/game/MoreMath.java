package game;

import java.util.List;

public class MoreMath {
    public static float clamp(float val, float min, float max) {
        float v = Math.max(val, min);
        return Math.min(v, max);
    }

    public static float moveTowards(float val, float desired, float delta) {
        if (Math.abs(val - desired) <= delta) return desired;

        return val + Math.signum(desired - val) * delta;
    }

    public static float lerp(float val, float destination, float delta) {
        return val + (destination - val) * delta;
    }

    public static double lerp(double val, double destination, double delta) {
        return val + (destination - val) * delta;
    }

    public static float[] lerp(float[] val, float[] dest, float delta) {
        if (val.length != dest.length) {
            throw new IllegalArgumentException("Both arrays must be of the same size.");
        }

        float[] res = new float[val.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = lerp(val[i], dest[i], delta);
        }
        return res;
    }

    public static float magnitude(float[] floats) {
        float res = 0;
        for (float f : floats) {
            res += f*f;
        }
        return (float) Math.sqrt(res);
    }

    public static double random(double min, double max) {
        return (Math.random() * (1+max-min)) + min;
    }

    public static boolean isApprox(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static boolean isApprox(float a, float b) {
        return isApprox(a, b, 1e-7f);
    }

    public static <T extends Enum<?>> T pickRandomEnumeration(Class<T> clazz) {
        T[] constants = clazz.getEnumConstants();
        return constants[(int) (Math.random()*constants.length)];
    }

    public static <T> T pick(List<T> list) {
        return list.get((int) (Math.random()*list.size()));
    }
}
