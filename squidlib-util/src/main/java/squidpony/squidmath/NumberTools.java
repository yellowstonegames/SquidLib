package squidpony.squidmath;

/**
 * Various numeric functions that are important to performance but need alternate implementations on GWT to obtain it.
 * Super-sourced on GWT, but most things here are direct calls to JDK methods when on desktop or Android.
 */
public class NumberTools {
    public static long doubleToLongBits(final double value)
    {
        return Double.doubleToLongBits(value);
    }
    public static long doubleToRawLongBits(final double value)
    {
        return Double.doubleToRawLongBits(value);
    }
    public static double longBitsToDouble(final long bits)
    {
        return Double.longBitsToDouble(bits);
    }
    public static int doubleToLowIntBits(final double value)
    {
        return (int)(Double.doubleToLongBits(value) & 0xffffffffL);
    }
    public static int doubleToHighIntBits(final double value)
    {
        return (int)(Double.doubleToLongBits(value) >>> 32);
    }
    public static int doubleToMixedIntBits(final double value)
    {
        final long l = Double.doubleToLongBits(value);
        return (int)l ^ (int)(l >>> 32);
    }

    /**
     * Makes a modified version of value that uses the specified bits (up to 12) for its exponent and sign.
     * Meant for some specific cases, like adjusting the exponent on an unknown double to the 1.0 to 2.0 range (which
     * would pass 0x3ff for exponentBits). If you have a double from 1.0 to 2.0, you can subtract 1.0 from it to get the
     * often-desirable 0.0-1.0 range. Other common cases are 0x400, which adjusts to between 2.0 and 4.0 (subtracting
     * 3.0 from this gives the -1.0 to 1.0 range, useful for noise), and 0xBFF, which adjusts to between -2.0 and -1.0.
     * For the last case, you might think that -0x3ff would work, but sadly it doesn't. You can use
     * {@code exponentBits |= 0x800} to set the sign bit to negative, or {@code exponentBits &= 0x7ff} for positive.
     * @param value a double that will have its sign and exponent set to the specified bits
     * @param exponentBits the bits to use for the sign and exponent section of the returned modification of value
     * @return the double produced by keeping the significand of value but changing its exponent and sign as given
     */
    public static double setExponent(final double value, final int exponentBits)
    {
        return Double.longBitsToDouble((Double.doubleToLongBits(value) & 0xfffffffffffffL) | ((long) exponentBits << 52));
    }

    /**
     * Very limited-use. Takes any double and produces a double in the -1.0 to 1.0 range, with similar inputs producing
     * close to a consistent rate of up and down through the range. This is meant for noise, where it may be useful to
     * limit the amount of change between nearby points' noise values and prevent sudden "jumps" in noise value.
     * @param value any double
     * @return a double from -1.0 (inclusive) to 1.0 (exclusive)
     */
    public static double bounce(final double value)
    {
        long s = Double.doubleToLongBits(value) & 0xfffffffffffffL;
        return Double.longBitsToDouble(((s ^ -((s & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4010000000000000L) - 5.0;
    }
    /**
     * Very limited-use. Takes the significand bits of a double, represented as a long of which this uses 52 bits, and
     * produces a double in the -1.0 to 1.0 range, with similar inputs producing close to a consistent rate of up and
     * down through the range. This is meant for noise, where it may be useful to limit the amount of change between
     * nearby points' noise values and prevent sudden "jumps" in noise value.
     * @param value any long; only the lower 52 bits will be used
     * @return a double from -1.0 (inclusive) to 1.0 (exclusive)
     */
    public static double bounce(final long value)
    {
        long s = value & 0xfffffffffffffL;
        return Double.longBitsToDouble(((s ^ -((s & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4010000000000000L) - 5.0;
    }
    public static int floatToIntBits(final float value)
    {
        return Float.floatToIntBits(value);
    }
    public static int floatToRawIntBits(final float value)
    {
        return Float.floatToRawIntBits(value);
    }
    public static float intBitsToFloat(final int bits)
    {
        return Float.intBitsToFloat(bits);
    }

    static int hashWisp(final float[] data)
    {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * Float.floatToIntBits(data[i]));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
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
