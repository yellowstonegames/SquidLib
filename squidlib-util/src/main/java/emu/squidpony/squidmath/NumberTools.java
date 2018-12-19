package squidpony.squidmath;

import com.google.gwt.typedarrays.client.Float64ArrayNative;
import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.client.DataViewNative;
import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.DataView;

/**
 * Various numeric functions that are important to performance but need alternate implementations on GWT to obtain it.
 * Super-sourced on GWT, but most things here are direct calls to JDK methods when on desktop or Android.
 */
public class NumberTools {

    private static final Int8Array wba = Int8ArrayNative.create(8);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float64Array wda = Float64ArrayNative.create(wba.buffer(), 0, 1);
    private static final DataView dv = DataViewNative.create(wba.buffer()); 

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
        wia.set(0, (int)(bits & 0xFFFFFFFF));
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
        final int s = wia.get(1) & 0xfffff, flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, wia.get(0) ^ flip);
        return wda.get(0) - 5.0;
    }

    public static float bounce(final float value)
    {
        wfa.set(0, value);
        final int s = wia.get(0) & 0x007fffff, flip = -((s & 0x00400000)>>22);
        wia.set(0, ((s ^ flip) & 0x007fffff) | 0x40800000);
        return wfa.get(0) - 5f;
    }

    public static double bounce(final long value)
    {
        final int s = (int)(value>>>32&0xfffff), flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, ((int)(value & 0xFFFFFFFF)) ^ flip);
        return wda.get(0) - 5.0;
    }

    public static double bounce(final int valueLow, final int valueHigh)
    {
        final int s = valueHigh & 0xfffff, flip = -((s & 0x80000)>>19);
        wia.set(1, ((s ^ flip) & 0xfffff) | 0x40100000);
        wia.set(0, valueLow ^ flip);
        return wda.get(0) - 5.0;
    }

    public static double zigzag(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * (floor << 1) - floor;
    }

    public static float zigzag(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * (floor << 1) - floor;
    }

    public static double sway(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0) * (floor << 1) - floor;
    }

    public static float sway(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * value * value * (value * (value * 6f - 15f) + 10f) * (floor << 1) - floor;
    }

    public static float swayTight(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor &= 1;
        return value * value * value * (value * (value * 6f - 15f) + 10f) * (-floor | 1) + floor;
    }

    public static double swayTight(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor &= 1L;
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0) * (-floor | 1L) + floor;
    }

    public static double swayRandomized(long seed, double value)
    {
        final long floor = value >= 0.0 ? (long) value : (long) value - 1L;
        final double start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-63,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-63;
        value -= floor;
        value *= value * (3.0 - 2.0 * value);
        return (1.0 - value) * start + value * end;
    }

    public static float swayRandomized(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        final float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.ffffffp-63f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.ffffffbp-63f;
        value -= floor;
        value *= value * (3f - 2f * value);
        return (1f - value) * start + value * end;
    }

    public static int floatToIntBits(final float value) {
        wfa.set(0, value);
        return wia.get(0);
    }
    public static int floatToRawIntBits(final float value) {
        wfa.set(0, value);
        return wia.get(0);
    }

    public static int floatToReversedIntBits(final float value) {
        dv.setFloat32(0, value, true);
        return dv.getInt32(0, false);
    }
    public static float reversedIntBitsToFloat(final int bits) {
        dv.setInt32(0, bits, true);
        return dv.getFloat32(0, false);
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

    public static double randomDouble(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    public static double randomSignedDouble(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)) >> 10) * 0x1p-53;
    }

    public static float randomFloat(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)) & 0xFFFFFF) * 0x1p-24f;
    }

    public static float randomSignedFloat(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)) >> 39) * 0x1p-24f;
    }

    public static float randomFloatCurved(long seed)
    {
        return formCurvedFloat(((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22)));
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

    public static double formDouble(final long seed)
    {
        wia.set(1, (int)(seed >>> 32 & 0xFFFFF) | 0x3ff00000);
        wia.set(0, (int)(seed & 0xFFFFFFFF));
        return wda.get(0) - 1.0;
    }

    public static double formSignedDouble(final long seed)
    {
        wia.set(1, (int)(seed >>> 32 & 0xFFFFF) | 0x40000000);
        wia.set(0, (int)(seed & 0xFFFFFFFF));
        return wda.get(0) - 3.0;
    }
    public static double formCurvedDouble(long start) {
        return    longBitsToDouble((start >>> 12) | 0x3fe0000000000000L)
                + longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *  0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L);
    }
    public static double formCurvedDoubleTight(long start) {
        return  0.5
                + longBitsToDouble((start >>> 12) | 0x3fd0000000000000L)
                + longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L)
                - longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L)
                - longBitsToDouble(((start *  0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L);
    }
    public static float formCurvedFloat(final long start) {
        return    intBitsToFloat((int)start >>> 9 | 0x3F000000)
                + intBitsToFloat((int) (start >>> 41) | 0x3F000000)
                - intBitsToFloat(((int)(start ^ ~start >>> 20) & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(((int) (~start ^ start >>> 30) & 0x007FFFFF) | 0x3F000000)
                ;
    }
    public static float formCurvedFloat(final int start1, final int start2) {
        return    intBitsToFloat(start1 >>> 9 | 0x3F000000)
                + intBitsToFloat((~start1 & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(start2 >>> 9 | 0x3F000000)
                - intBitsToFloat((~start2 & 0x007FFFFF) | 0x3F000000)
                ;
    }
    public static float formCurvedFloat(final int start) {
        return    intBitsToFloat(start >>> 9 | 0x3F000000)
                + intBitsToFloat((start & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(((start << 18 & 0x007FFFFF) ^ ~start >>> 14) | 0x3F000000)
                - intBitsToFloat(((start << 13 & 0x007FFFFF) ^ ~start >>> 19) | 0x3F000000)
                ;
    }
    public static int lowestOneBit(int num)
    {
        return num & ~(num - 1);
    }

    public static long lowestOneBit(long num)
    {
        return num & ~(num - 1L);
    }

    public static double sin(double radians)
    {
        radians = radians * 0.6366197723675814;
        final long floor = (radians >= 0.0 ? (long) radians : (long) radians - 1L) & -2L;
        radians -= floor;
        radians *= 2.0 - radians;
        return radians * (-0.775 - 0.225 * radians) * ((floor & 2L) - 1L);
    }

    public static double cos(double radians)
    {
        radians = radians * 0.6366197723675814 + 1.0;
        final long floor = (radians >= 0.0 ? (long) radians : (long) radians - 1L) & -2L;
        radians -= floor;
        radians *= 2.0 - radians;
        return radians * (-0.775 - 0.225 * radians) * ((floor & 2L) - 1L);
    }

    public static float sin(float radians)
    {
        radians = radians * 0.6366197723675814f;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }

    public static float cos(float radians)
    {
        radians = radians * 0.6366197723675814f + 1f;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }

    public static float sinDegrees(float degrees)
    {
        degrees = degrees * 0.011111111111111112f;
        final int floor = (degrees >= 0.0 ? (int) degrees : (int) degrees - 1) & -2;
        degrees -= floor;
        degrees *= 2f - degrees;
        return degrees * (-0.775f - 0.225f * degrees) * ((floor & 2) - 1);
    }

    public static float cosDegrees(float degrees)
    {
        degrees = degrees * 0.011111111111111112f + 1f;
        final int floor = (degrees >= 0.0 ? (int) degrees : (int) degrees - 1) & -2;
        degrees -= floor;
        degrees *= 2f - degrees;
        return degrees * (-0.775f - 0.225f * degrees) * ((floor & 2) - 1);
    }
    public static double atan2(final double y, final double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        final double ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 1.57079637 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (x < 0.0) ? (y < 0.0) ? -3.14159274 + r : 3.14159274 - r : (y < 0.0) ? -r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (x < 0.0) ? (y < 0.0) ? -3.14159274 + r : 3.14159274 - r : (y < 0.0) ? -r : r;
        }
    }
    public static float atan2(final float y, final float x)
    {
        if(y == 0f && x >= 0f) return 0f;
        final float ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final float a = ax / ay, s = a * a,
                    r = 1.57079637f - (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
            return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
        }
        else {
            final float a = ay / ax, s = a * a,
                    r = (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
            return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
        }
    }
    public static double atan2Rough(final double y, final double x)
    {
        if(y == 0f && x >= 0f) return 0.0;
        final double ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final double a = ax / ay,
                    r = 1.57079637 - (a * (0.7853981633974483 + 0.273 * (1.0 - a)));
            return (x < 0.0) ? (y < 0.0) ? -3.14159274 + r : 3.14159274 - r : (y < 0.0) ? -r : r;
        }
        else {
            final double a = ay / ax,
                    r = (a * (0.7853981633974483 + 0.273 * (1.0 - a)));
            return (x < 0.0) ? (y < 0.0) ? -3.14159274 + r : 3.14159274 - r : (y < 0.0) ? -r : r;
        }
    }
    public static float atan2Rough(final float y, final float x)
    {
        if(y == 0f && x >= 0f) return 0f;
        final float ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final float a = ax / ay,
                    r = 1.57079637f - (a * (0.7853981633974483f + 0.273f * (1f - a)));
            return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
        }
        else {
            final float a = ay / ax,
                    r = (a * (0.7853981633974483f + 0.273f * (1f - a)));
            return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
        }
    }
    public static double atan2_(final double y, final double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        final double ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 0.25 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (x < 0.0) ? (y < 0.0) ? 0.5 + r : 0.5 - r : (y < 0.0) ? 1.0 - r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (x < 0.0) ? (y < 0.0) ? 0.5 + r : 0.5 - r : (y < 0.0) ? 1.0 - r : r;
        }
    }
    public static float atan2_(final float y, final float x)
    {
        if(y == 0.0 && x >= 0.0) return 0f;
        final float ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final float a = ax / ay, s = a * a,
                    r = 0.25f - (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a) * 0.15915494309189535f;
            return (x < 0.0f) ? (y < 0.0f) ? 0.5f + r : 0.5f - r : (y < 0.0f) ? 1f - r : r;
        }
        else {
            final float a = ay / ax, s = a * a,
                    r = (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a) * 0.15915494309189535f;
            return (x < 0.0f) ? (y < 0.0f) ? 0.5f + r : 0.5f - r : (y < 0.0f) ? 1f - r : r;
        }
    }
    public static double atan2Rough_(final double y, final double x)
    {
        if(y == 0f && x >= 0f) return 0.0;
        final double ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final double a = ax / ay,
                    r = 0.25 - (a * (0.7853981633974483 + 0.273 * (1.0 - a))) * 0.15915494309189535;
            return (x < 0.0) ? (y < 0.0) ? 0.5 + r : 0.5 - r : (y < 0.0) ? 1.0 - r : r;
        }
        else {
            final double a = ay / ax,
                    r = (a * (0.7853981633974483 + 0.273 * (1.0 - a))) * 0.15915494309189535;
            return (x < 0.0) ? (y < 0.0) ? 0.5 + r : 0.5 - r : (y < 0.0) ? 1.0 - r : r;
        }
    }
    public static float atan2Rough_(final float y, final float x)
    {
        if(y == 0f && x >= 0f) return 0f;
        final float ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final float a = ax / ay,
                    r = 0.25f - (a * (0.7853981633974483f + 0.273f * (1f - a))) * 0.15915494309189535f;
            return (x < 0f) ? (y < 0f) ? 0.5f + r : 0.5f - r : (y < 0f) ? 1f - r : r;
        }
        else {
            final float a = ay / ax,
                    r = (a * (0.7853981633974483f + 0.273f * (1f - a))) * 0.15915494309189535f;
            return (x < 0f) ? (y < 0f) ? 0.5f + r : 0.5f - r : (y < 0f) ? 1f - r : r;
        }
    }
}
