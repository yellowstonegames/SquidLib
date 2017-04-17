package squidpony.squidmath;

import com.google.gwt.typedarrays.client.Float64ArrayNative;
import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;

/**
 * Various numeric functions that are important to performance but need alternate implementations on GWT to obtain it.
 * Super-sourced on GWT, but most things here are direct calls to JDK methods when on desktop or Android.
 */
public class NumberTools {

    private static final Int8Array wba = Int8ArrayNative.create(8);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float64Array wda = Float64ArrayNative.create(wba.buffer(), 0, 1);

    public static long doubleToLongBits(final double value) {
        wda.set(0, value);
        return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
    }

    public static long doubleToRawLongBits(final double value) {
        wda.set(0, value);
        return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
    }

    public static double longBitsToDouble(final long bits) {
        wia.set(1, (int)(bits >>> 32));
        wia.set(0, (int)bits);
        return wda.get(0);
    }

    public static int doubleToLowIntBits(final double value)
    {
        wda.set(0, value);
        return wia.get(0);
    }
    public static int doubleToHighIntBits(final double value)
    {
        wda.set(0, value);
        return wia.get(1);
    }
    public static int doubleToMixedIntBits(final double value)
    {
        wda.set(0, value);
        return wia.get(0) ^ wia.get(1);
    }

    public static double setExponent(final double value, final int exponentBits)
    {
        wda.set(0, value);
        wia.set(1, (wia.get(1) & 0xfffff) | exponentBits << 20);
        return wda.get(0);
    }

    public static double bounce(final double value)
    {
        wda.set(0, value);
        int s = wia.get(1) & 0xfffff, flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, wia.get(0) ^ flip);
        return wda.get(0) - 5.0;
    }

    public static double bounce(final long value)
    {
        int s = (int)(value>>>32&0xfffff), flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, ((int)value) ^ flip);
        return wda.get(0) - 5.0;
    }

    public static double bounce(final int valueLow, final int valueHigh)
    {
        int s = valueHigh & 0xfffff, flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, valueLow ^ flip);
        return wda.get(0) - 5.0;
    }

    public static int floatToIntBits(final float value) {
        wfa.set(0, value);
        return wia.get(0);
    }
    public static int floatToRawIntBits(final float value) {
        wfa.set(0, value);
        return wia.get(0);
    }

    public static float intBitsToFloat(final int bits) {
        wia.set(0, bits);
        return wfa.get(0);
    }

    public static byte getSelectedByte(final double value, final int whichByte)
    {
        wda.set(0, value);
        return wba.get(whichByte & 7);
    }

    public static double setSelectedByte(final double value, final int whichByte, final byte newValue)
    {
        wda.set(0, value);
        wba.set(whichByte & 7, newValue);
        return wda.get(0);
    }

    public static byte getSelectedByte(final float value, final int whichByte)
    {
        wfa.set(0, value);
        return wba.get(whichByte & 3);
    }

    public static float setSelectedByte(final float value, final int whichByte, final byte newValue)
    {
        wfa.set(0, value);
        wba.set(whichByte & 3, newValue);
        return wfa.get(0);
    }

    public static double randomDouble(int seed)
    {
        seed ^= seed >>> (4 + (seed >>> 28));
        wia.set(0, ((seed *= 277803737) >>> 22) ^ seed);
        seed += 0x9E3779B9;
        seed ^= seed >>> (4 + (seed >>> 28));
        wia.set(1, ((((seed *= 277803737) >>> 22) ^ seed) & 0xfffff) | 0x3ff00000);
        return wda.get(0) - 1.0;
    }
    public static float randomFloat(int seed)
    {
        seed ^= seed >>> (4 + (seed >>> 28));
        wia.set(0, (((seed *= 0x108EF2D9) >>> 22 ^ seed) >>> 9) | 0x3f800000);
        return (wfa.get(0) - 1f);
    }

    /**
     * Generates a pseudo-random float between -1.0f (exclusive) and 1.0f (exclusive) using the given int seed, passing
     * it once through the (very high-quality and rather fast) {@link PintRNG} algorithm, derived from PCG-Random. This
     * produces a random int, which this produces a float from using {@link #intBitsToFloat(int)} (long)} or something
     * functionally equivalent on GWT. The sign bit of the result is determined by data that is not used by the float
     * otherwise, and keeps the results almost linear in distribution between -1.0 and 1.0, exclusive for both (0 shows
     * up twice as often as any single other result, but this shouldn't affect the odds very strongly; it's about a 1 in
     * 8 million chance of exactly 0 occurring vs. a 1 in 16 million of any other specific float this can produce).
     * <br>
     * Consider calling this with {@code NumberTools.randomSignedFloat(seed += 0x9E3779B9)} for an optimal period of 2
     * to the 32 when repeatedly called, but {@code NumberTools.randomSignedFloat(++seed)} will also work just fine.
     * @param seed any int to be used as a seed
     * @return a pseudo-random float from -1.0f (exclusive) to 1.0f (exclusive)
     */
    public static float randomSignedFloat(int seed)
    {
        seed ^= seed >>> (4 + (seed >>> 28));
        wia.set(0, (((seed *= 0x108EF2D9) >>> 22 ^ seed) >>> 9) | 0x3f800000);
        return (wfa.get(0) - 1f) * (seed >> 31 | 1);
    }


    public static double randomDoubleCurved(int seed)
    {
        seed ^= seed >>> (4 + (seed >>> 28));
        wia.set(0, ((((seed *= 277803737) >>> 22) ^ seed) >>> 9) | 0x3f800000);
        seed += 0x9E3779B9;
        seed ^= seed >>> (4 + (seed >>> 28));
        wia.set(1, ((((seed *= 277803737) >>> 22) ^ seed) >>> 9) | 0x3f800000);
        return (wfa.get(0) - 1.0) * (wfa.get(1) - 1.0) * (seed >> 31 | 1);
    }

    static int hashWisp(final float[] data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        double t;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x85157AF5 * ((int) (-0xD0E8.9D2D311E289Fp-25f * (t = data[i]) + t * -0x1.39b4dce80194cp9f)));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    static int hashWisp(final double[] data)
    {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            wda.set(0, data[i]);
            result += (a ^= 0x85157AF5 * wia.get(0)) + (a ^= 0x85157AF5 * wia.get(1));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }
}
