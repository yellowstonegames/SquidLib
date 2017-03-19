package squidpony.squidmath;

/**
 * Various numeric functions that are important to performance but need alternate implementations on GWT to obtain it.
 * Super-sourced on GWT, but most things here are direct calls to JDK methods when on desktop or Android.
 */
public class NumberTools {
    public static long doubleToLongBits(double value)
    {
        return Double.doubleToLongBits(value);
    }
    public static long doubleToRawLongBits(double value)
    {
        return Double.doubleToRawLongBits(value);
    }
    public static double longBitsToDouble(long bits)
    {
        return Double.longBitsToDouble(bits);
    }
    public static int doubleToLowIntBits(double value)
    {
        return (int)(Double.doubleToLongBits(value) & 0xffffffffL);
    }
    public static int doubleToHighIntBits(double value)
    {
        return (int)(Double.doubleToLongBits(value) >>> 32);
    }

    public static int floatToIntBits(float value)
    {
        return Float.floatToIntBits(value);
    }
    public static int floatToRawIntBits(float value)
    {
        return Float.floatToRawIntBits(value);
    }
    public static float intBitsToFloat(int bits)
    {
        return Float.intBitsToFloat(bits);
    }
    static int hashWisp(final double[] data)
    {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        double t;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * -0x1.39b4dce80194cp9)));
        }
        return (int)((result = (result * (a | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
    }
}
