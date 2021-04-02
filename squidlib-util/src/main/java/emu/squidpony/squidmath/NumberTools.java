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
        wia.set(0, (int)(bits & 0xffffffffL));
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
    public static double swayCubic(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * value * (3.0 - value * 2.0) * (floor << 1) - floor;
    }

    public static float swayCubic(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * value * (3f - value * 2f) * (floor << 1) - floor;
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
    public static double swayRandomized(final int seed, double value)
    {
        final int floor = value >= 0.0 ? (int) value : (int) value - 1;
        int z = seed + floor;
        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
        value -= floor;
        value *= value * (3.0 - 2.0 * value);
        return (1.0 - value) * start + value * end;
    }
    public static float swayRandomized(final int seed, float value)
    {
        final int floor = value >= 0f ? (int) value : (int) value - 1;
        int z = seed + floor;
        final float start = (((z = (z ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f,
                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f;
        value -= floor;
        value *= value * (3 - 2 * value);
        return (1 - value) * start + value * end;
    }
    public static float swayAngleRandomized(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f;
        value -= floor;
        value *= value * (3f - 2f * value);
        end = end - start + 1.5f;
        end -= (long)end + 0.5f;
        start += end * value + 1;
        return (start - (long)start) * 6.283185307179586f;
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

    public static double sin_(double turns)
    {
        turns *= 4.0;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2.0 - turns;
        return turns * (-0.775 - 0.225 * turns) * ((floor & 2L) - 1L);
    }
    public static double cos_(double turns)
    {
        turns = turns * 4.0 + 1.0;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2.0 - turns;
        return turns * (-0.775 - 0.225 * turns) * ((floor & 2L) - 1L);
    }
    public static float sin_(float turns)
    {
        turns *= 4f;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2f - turns;
        return turns * (-0.775f - 0.225f * turns) * ((floor & 2L) - 1L);
    }
    public static float cos_(float turns)
    {
        turns = turns * 4f + 1f;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2f - turns;
        return turns * (-0.775f - 0.225f * turns) * ((floor & 2L) - 1L);
    }
    public static double atan(final double i) {
        final double n = Math.min(Math.abs(i), Double.MAX_VALUE);
        final double c = (n - 1.0) / (n + 1.0);
        final double c2 = c * c;
        final double c3 = c * c2;
        final double c5 = c3 * c2;
        final double c7 = c5 * c2;
        return Math.copySign(0.7853981633974483 +
                (0.999215 * c - 0.3211819 * c3 + 0.1462766 * c5 - 0.0389929 * c7), i);
    }
    public static float atan(final float i) {
        final float n = Math.min(Math.abs(i), Float.MAX_VALUE);
        final float c = (n - 1f) / (n + 1f);
        final float c2 = c * c;
        final float c3 = c * c2;
        final float c5 = c3 * c2;
        final float c7 = c5 * c2;
        return Math.copySign(0.7853981633974483f +
                (0.999215f * c - 0.3211819f * c3 + 0.1462766f * c5 - 0.0389929f * c7), i);
    }
    private static double atn(final double i) {
        final double n = Math.abs(i);
        final double c = (n - 1.0) / (n + 1.0);
        final double c2 = c * c;
        final double c3 = c * c2;
        final double c5 = c3 * c2;
        final double c7 = c5 * c2;
        return Math.copySign(0.7853981633974483 +
                (0.999215 * c - 0.3211819 * c3 + 0.1462766 * c5 - 0.0389929 * c7), i);
    }
    private static float atn(final float i) {
        final float n = Math.abs(i);
        final float c = (n - 1f) / (n + 1f);
        final float c2 = c * c;
        final float c3 = c * c2;
        final float c5 = c3 * c2;
        final float c7 = c5 * c2;
        return Math.copySign(0.7853981633974483f +
                (0.999215f * c - 0.3211819f * c3 + 0.1462766f * c5 - 0.0389929f * c7), i);
    }
    public static double atan2(final double y, double x) {
        double n = y / x;
        if(n != n) n = (y == x ? 1.0 : -1.0); // if both y and x are infinite, n would be NaN
        else if(n - n != n - n) x = 0.0; // if n is infinite, y is infinitely larger than x.
        if(x > 0)
            return atn(n);
        else if(x < 0) {
            if(y >= 0)
                return atn(n) + 3.14159265358979323846;
            else
                return atn(n) - 3.14159265358979323846;
        }
        else if(y > 0) return x + 1.5707963267948966;
        else if(y < 0) return x - 1.5707963267948966;
        else return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }
    public static float atan2(final float y, float x) {
        float n = y / x;
        if(n != n) n = (y == x ? 1f : -1f); // if both y and x are infinite, n would be NaN
        else if(n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if(x > 0)
            return atn(n);
        else if(x < 0) {
            if(y >= 0)
                return atn(n) + 3.14159265358979323846f;
            else
                return atn(n) - 3.14159265358979323846f;
        }
        else if(y > 0) return x + 1.5707963267948966f;
        else if(y < 0) return x - 1.5707963267948966f;
        else return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }
    private static double atn_(final double v) {
        final double n = Math.abs(v);
        final double c = (n - 1.0) / (n + 1.0);
        final double c2 = c * c;
        final double c3 = c * c2;
        final double c5 = c3 * c2;
        final double c7 = c5 * c2;
        return Math.copySign(0.125 + 0.1590300064615682 * c - 0.051117687016646825 * c3 + 0.02328064394867594 * c5
                - 0.006205912780487965 * c7, v);
    }
    private static float atn_(final float v) {
        final float n = Math.abs(v);
        final float c = (n - 1f) / (n + 1f);
        final float c2 = c * c;
        final float c3 = c * c2;
        final float c5 = c3 * c2;
        final float c7 = c5 * c2;
        return Math.copySign(0.125f + 0.1590300064615682f * c - 0.051117687016646825f * c3 + 0.02328064394867594f * c5
                - 0.006205912780487965f * c7, v);
    }
    public static double atan2_(final double y, double x) {
        double n = y / x;
        if(n != n) n = (y == x ? 1f : -1f); // if both y and x are infinite, n would be NaN
        else if(n - n != n - n) x = 0.0; // if n is infinite, y is infinitely larger than x.
        if(x > 0) {
            if(y >= 0)
                return atn_(n);
            else
                return atn_(n) + 1.0;
        }
        else if(x < 0) {
            return atn_(n) + 0.5;
        }
        else if(y > 0) return x + 0.25;
        else if(y < 0) return x + 0.75;
        else return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }
    public static float atan2_(final float y, float x) {
        float n = y / x;
        if(n != n) n = (y == x ? 1f : -1f); // if both y and x are infinite, n would be NaN
        else if(n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
        if(x > 0) {
            if(y >= 0)
                return atn_(n);
            else
                return atn_(n) + 1f;
        }
        else if(x < 0) {
            return atn_(n) + 0.5f;
        }
        else if(y > 0) return x + 0.25f;
        else if(y < 0) return x + 0.75f;
        else return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
    }

    public static double atan2Degrees(double y, double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        double ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        double z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636)  * z) - (19.670500543533855) ) * z - (0.9295667268202475) ) * z + (57.51573801063304) ) * z - (0.009052733163067006) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return Math.copySign(z, y);
    }
    public static float atan2Degrees(float y, float x)
    {
        if(y == 0f && x >= 0f) return 0f;
        float ax = Math.abs(x), ay = Math.abs(y);
        boolean invert = ay > ax;
        float z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636f)  * z) - (19.670500543533855f) ) * z - (0.9295667268202475f) ) * z + (57.51573801063304f) ) * z - (0.009052733163067006f) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return Math.copySign(z, y);
    }
    public static double atan2Degrees360(double y, double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        double ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        double z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636)  * z) - (19.670500543533855) ) * z - (0.9295667268202475) ) * z + (57.51573801063304) ) * z - (0.009052733163067006) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return y < 0 ? 360 - z : z;
    }
    public static float atan2Degrees360(float y, float x)
    {
        if(y == 0f && x >= 0f) return 0f;
        float ax = Math.abs(x), ay = Math.abs(y);
        boolean invert = ay > ax;
        float z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636f)  * z) - (19.670500543533855f) ) * z - (0.9295667268202475f) ) * z + (57.51573801063304f) ) * z - (0.009052733163067006f) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return y < 0 ? 360 - z : z;
    }

    public static float asin(final float x) {
        final float x2 = x * x;
        final float x3 = x * x2;
        if (x >= 0f) {
            return 1.5707963267948966f - (float) Math.sqrt(1f - x) *
                    (1.5707288f - 0.2121144f * x + 0.0742610f * x2 - 0.0187293f * x3);
        }
        else {
            return -1.5707964f + (float) Math.sqrt(1f + x) *
                    (1.5707288f + 0.2121144f * x + 0.0742610f * x2 + 0.0187293f * x3);
        }
    }

    public static float acos(final float x) {
        final float x2 = x * x;
        final float x3 = x * x2;
        if (x >= 0f) {
            return (float) Math.sqrt(1f - x) * (1.5707288f - 0.2121144f * x + 0.0742610f * x2 - 0.0187293f * x3);
        }
        else {
            return 3.14159265358979323846f - (float) Math.sqrt(1f + x) * (1.5707288f + 0.2121144f * x + 0.0742610f * x2 + 0.0187293f * x3);
        }
    }

    public static double asin(final double x) {
        final double x2 = x * x;
        final double x3 = x * x2;
        if (x >= 0.0) {
            return 1.5707963267948966 - Math.sqrt(1.0 - x) *
                    (1.5707288 - 0.2121144 * x + 0.0742610 * x2 - 0.0187293 * x3);
        }
        else {
            return -1.5707963267948966 + Math.sqrt(1.0 + x) *
                    (1.5707288 + 0.2121144 * x + 0.0742610 * x2 + 0.0187293 * x3);
        }
    }
    public static double acos(final double x) {
        final double x2 = x * x;
        final double x3 = x * x2;
        if (x >= 0.0) {
            return Math.sqrt(1.0 - x) * (1.5707288 - 0.2121144 * x + 0.0742610 * x2 - 0.0187293 * x3);
        }
        else {
            return 3.14159265358979323846 - Math.sqrt(1.0 + x) * (1.5707288 + 0.2121144 * x + 0.0742610 * x2 + 0.0187293 * x3);
        }
    }
    public static double asin_(final double x) {
        final double x2 = x * x;
        final double x3 = x * x2;
        if (x >= 0.0) {
            return 0.25 - Math.sqrt(1.0 - x) *
                    (0.24998925277680106 - 0.033759055260971525 * x + 0.01181900522894724 * x2 - 0.0029808606756510357 * x3);
        }
        else {
            return 0.75 + Math.sqrt(1.0 + x) *
                    (0.24998925277680106 + 0.033759055260971525 * x + 0.01181900522894724 * x2 + 0.0029808606756510357 * x3);
        }
    }
    public static double acos_(final double x) {
        final double x2 = x * x;
        final double x3 = x * x2;
        if (x >= 0.0) {
            return Math.sqrt(1.0 - x) * (0.24998925277680106 - 0.033759055260971525 * x + 0.01181900522894724 * x2 - 0.0029808606756510357 * x3);
        }
        else {
            return 0.5 - Math.sqrt(1.0 + x) * (0.24998925277680106 + 0.033759055260971525 * x + 0.01181900522894724 * x2 + 0.0029808606756510357 * x3);
        }
    }
    public static float asin_(final float x) {
        final float x2 = x * x;
        final float x3 = x * x2;
        if (x >= 0f) {
            return 0.25f - (float) Math.sqrt(1f - x) *
                    (0.24998925277680106f - 0.033759055260971525f * x + 0.01181900522894724f * x2 - 0.0029808606756510357f * x3);
        }
        else {
            return 0.75f + (float) Math.sqrt(1f + x) *
                    (0.24998925277680106f + 0.033759055260971525f * x + 0.01181900522894724f * x2 + 0.0029808606756510357f * x3);
        }
    }
    public static float acos_(final float x) {
        final float x2 = x * x;
        final float x3 = x * x2;
        if (x >= 0f) {
            return (float) Math.sqrt(1f - x) * (0.24998925277680106f - 0.033759055260971525f * x + 0.01181900522894724f * x2 - 0.0029808606756510357f * x3);
        }
        else {
            return 0.5f - (float) Math.sqrt(1f + x) * (0.24998925277680106f + 0.033759055260971525f * x + 0.01181900522894724f * x2 + 0.0029808606756510357f * x3);
        }
    }
}
