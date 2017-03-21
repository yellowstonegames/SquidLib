package squidpony.squidmath;

/**
 * Performance-oriented "white noise" generator for 1D, 2D, 3D, 4D, and 6D. Produces noise values from -1.0 inclusive
 * to 1.0 exclusive. Implementation is based on {@link CrossHash.Wisp#hash64Alt(double[])}, multiplying the doubles
 * given as input by medium-sized doubles (between 100 and 200, with a large section after the decimal point), then
 * flooring them and doing the rest of the math with longs. Finishes by passing the top 52 bits as a significand to
 * {@link NumberTools#longBitsToDouble(long)}, using an exponent that allows this to produce numbers between -1.0 and
 * 1.0. Has special treatment for the seed when present (since it is an int).
 * <br>
 * This can also be used as a sort of hashing function that produces a double, if you find a need for such a thing, with
 * {@link #hash(double...)}.
 */
public class ValueNoise implements Noise.Noise1D, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    public static final ValueNoise instance = new ValueNoise();

    public ValueNoise() {
    }

    @Override
    public double getNoise(double x) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3));
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

    @Override
    public double getNoiseWithSeed(double x, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

    @Override
    public double getNoise(double x, double y) {

        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x11.8329C6DFp3));
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L) - 3.0;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x11.8329C6DFp3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

    @Override
    public double getNoise(double x, double y, double z) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x11.8329C6DFp3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (z*0x11.85157AF5p3));
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x11.8329C6DFp3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (z*0x11.85157AF5p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x11.953976F9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (z*0x11.85157AF5p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (w*0x11.8329C6DFp3));
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x11.953976F9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (z*0x11.85157AF5p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (w*0x11.8329C6DFp3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x17.08329C6DFp2))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (z*0x29.085157AF5p1))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (w*0x11.85157AF5p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (u*0x17.09E3779B9p2))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (v*0x29.08329C6DFp1));
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * (long) (x*0x11.9E3779B9p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (y*0x17.08329C6DFp2))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (z*0x29.085157AF5p1))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (w*0x11.85157AF5p3))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (u*0x17.09E3779B9p2))
                        + (a ^= 0x8329C6EB9E6AD3E3L * (long) (v*0x29.08329C6DFp1))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }
    /**
     * Hashes the input double array or vararg and produces a double with unpredictable value, between -1.0 inclusive
     * and 1.0 exclusive.
     * @param data
     * @return
     */
    public static double hash(double... data) {
        if(data == null) return 0.0;
        long a = 0x632BE59BD9B4E019L, result = 0x9E3779B97F4A7C94L;
        double t;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * 0x1.39b4dce80194cp9)));
        }
        return NumberTools.longBitsToDouble(((result * (a | 1L) ^ (result >>> 27 | result << 37)) >>> 12) | 0x4000000000000000L)  - 3.0;
    }

}
