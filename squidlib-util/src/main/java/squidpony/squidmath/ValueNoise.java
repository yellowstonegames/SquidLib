package squidpony.squidmath;

/**
 * Performance-oriented "white noise" generator for 1D, 2D, 3D, 4D, and 6D. Produces noise values from -1.0 inclusive
 * to 1.0 exclusive. Implementation is based on {@link CrossHash.Wisp#hash64Alt(double[])}, with a special treatment for
 * the seed when given, since it is an int. This can also be used as a sort of hashing function that produces a double,
 * if you find a need for such a thing, with {@link #hash(double...)}.
 */
public class ValueNoise implements Noise.Noise1D, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    
    public ValueNoise()
    {
    }
    
    @Override
    public double getNoise(double x) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoise(double x, double y) {

        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);    }

    @Override
    public double getNoiseWithSeed(double x, double y, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoise(double x, double y, double z) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * u + u * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * v + v * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9))) 
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * u + u * 0x1.39b4dce80194cp9))) 
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * v + v * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    /**
     * Hashes the input double array or vararg and produces a double with unpredictable value, between -1.0 inclusive
     * and 1.0 exclusive.
     * @param data
     * @return
     */
    public double hash(double... data) {
        if(data == null) return 0.0;
        long a = 0x632BE59BD9B4E019L, result = 0x9E3779B97F4A7C94L;
        double t;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * 0x1.39b4dce80194cp9)));
        }
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

}
