package squidpony.squidmath;

import java.io.Serializable;

public class CrossHash {
    private static final Int8Array wba = Int8ArrayNative.create(8);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float64Array wfa = Float64ArrayNative.create(wba.buffer(), 0, 1);
    public static class Wisp {
        public static int hashAlt(final double[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                wfa.set(0, data[i]);
                result += (a ^= 0x85157AF5 * wia.get(0)) + (a ^= 0x85157AF5 * wia.get(1));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }
    }
}