package squidpony.squidmath;

import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
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

    public static long doubleToLongBits(double value) {
        wda.set(0, value);
        return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
    }

    public static long doubleToRawLongBits(double value) {
        wda.set(0, value);
        return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
    }

    public static double longBitsToDouble(long bits) {
        wia.set(1, (int)(bits >>> 32));
        wia.set(0, (int)(bits & 0xffffffffL));
        return wda.get(0);
    }

    public static int doubleToLowIntBits(double value)
    {
        wda.set(0, value);
        return wia.get(0);
    }
    public static int doubleToHighIntBits(double value)
    {
        wda.set(0, value);
        return wia.get(1);
    }

    public static int floatToIntBits(float value) {
        wfa.set(0, value);
        return wia.get(0);
    }
    public static int floatToRawIntBits(float value) {
        wfa.set(0, value);
        return wia.get(0);
    }

    public static float intBitsToFloat(int bits) {
        wia.set(0, bits);
        return wfa.get(0);
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
