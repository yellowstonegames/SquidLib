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

    public static float bounce(final float value)
    {
        wfa.set(0, value);
        int s = wia.get(0) & 0x007fffff, flip = -((s & 0x00400000)>>22);
        wia.set(0, ((s ^ flip) & 0x007fffff) | 0x40800000);
        return wfa.get(0) - 5f;
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

    public static long splitMix64(long state) {
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    public static double randomDouble(int seed)
    {
        long state = seed * 0x9E3779B97F4A7C15L;
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        state ^= (state >>> 31);
        wia.set(0, (int)(state & 0xFFFFFFFF));
        wia.set(1, (int)(state >>> 44) | 0x3ff00000);
        return wda.get(0) - 1.0;
    }
    public static float randomFloat(int seed)
    {
        long state = seed * 0x9E3779B97F4A7C15L;
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        wia.set(0, (int)(state >>> 41) | 0x3f800000);
        return (wfa.get(0) - 1f);
    }

    public static float randomSignedFloat(int seed)
    {
        long state = seed * 0x9E3779B97F4A7C15L;
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        wia.set(0, (int)(state >>> 40) | 0x3f800000);
        return (wfa.get(0) - 1f) * (state >> 63 | 1L);
    }
    public static float randomFloatCurved(int seed)
    {
        long state = seed * 0x9E3779B97F4A7C15L;
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        state ^= (state >>> 31);
        wia.set(0, (int)(state & 0x7FFFFF) | 0x3f800000);
        wia.set(1, (int)(state >>> 40) | 0x3f800000);
        return (wfa.get(0) - 1f) * (wfa.get(1) - 1f) * (state >> 63 | 1L);
    }
    public static float formFloat(final int seed)
    {
        wia.set(0, (seed & 0x7FFFFF) | 0x3f800000);
        return wfa.get(0) - 1f;
    }
    public static float formSignedFloat(final int seed)
    {
        wia.set(0, (seed & 0x7FFFFF) | 0x40000000);
        return wfa.get(0) - 3f;
    }
    public static float formCurvedFloat(final long start) {
        return   (intBitsToFloat((int)start >>> 9 | 0x3F000000)
                + intBitsToFloat(((int)~start & 0x007FFFFF) | 0x3F000000)
                + intBitsToFloat((int) (start >>> 41) | 0x3F000000)
                + intBitsToFloat(((int) (~start >>> 32) & 0x007FFFFF) | 0x3F000000)
                - 3f);
    }
    public static float formCurvedFloat(final int start1, final int start2) {
        return   (intBitsToFloat(start1 >>> 9 | 0x3F000000)
                + intBitsToFloat((~start1 & 0x007FFFFF) | 0x3F000000)
                + intBitsToFloat(start2 >>> 9 | 0x3F000000)
                + intBitsToFloat((~start2 & 0x007FFFFF) | 0x3F000000)
                - 3f);
    }
    public static float formCurvedFloat(final int start) {
        return   (intBitsToFloat(start >>> 9 | 0x3F000000)
                + intBitsToFloat((start & 0x007FFFFF) | 0x3F000000)
                + intBitsToFloat(((start << 18 & 0x007FFFFF) ^ ~start >>> 14) | 0x3F000000)
                + intBitsToFloat(((start << 13 & 0x007FFFFF) ^ ~start >>> 19) | 0x3F000000)
                - 3f);
    }
}
