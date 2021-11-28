package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * A low-quality continuous noise generator with strong artifacts, meant to be used as a building block.
 * This bases its implementation on {@link ValueNoise}, and its static methods are still called
 * {@link #valueNoise(int, double, double)}; this is almost entirely meant as an optimization for value noise.
 * Instead of hashing all 2-to-the-D points, where D is the dimensionality, this hashes half of those points
 * (like the square base of a pyramid) and interpolates that with one point in the center of the cube value
 * noise would have used (like the cap or point of a pyramid).
 * <br>
 * Note: the {@link #valueNoise(int, double, double)} methods in this class return results in the range of 0.0 to 1.0,
 * while the {@link #getNoise(double, double)} and {@link #getNoiseWithSeed(double, double, long)} methods use the Noise
 * class default range of -1.0 to 1.0.
 */
@Beta
public class WeavingNoise implements Noise.Noise2D, Noise.Noise3D,
        Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {
    public static final WeavingNoise instance = new WeavingNoise();

    public int seed = 0xD1CEBEEF;
    public WeavingNoise() {
    }

    public WeavingNoise(int seed) {
        this.seed = seed;
    }

    public WeavingNoise(long seed) {
        this.seed = (int) (seed ^ seed >>> 32);
    }
    /**
     * Produces output in the -0.5 to 0.5 range; otherwise like {@link NumberTools#swayRandomized(int, double)} but
     * using quintic interpolation instead of Hermite.
     * @param seed any int
     * @param value distance of 1D noise to travel
     * @return a noise value between -0.5 and 0.5, both inclusive
     */
    public static double valueNoise(final int seed, double value)
    {
        final int floor = value >= 0.0 ? (int) value : (int) value - 1;
        int z = seed + floor;
        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
        value -= floor;
        value *= value * value * (value * (value * 3f - 7.5f) + 5f);
        return (0.5 - value) * start + value * end;
    }

    public static double valueNoise(int seed, double x, double y)
    {
        double sx = valueNoise(seed, x);
        double sy = valueNoise(seed + 101, y);
        return MathExtras.barronSpline(NumberTools.swayTight((sx + sy - valueNoise(seed - 101, x + y))), 0.333, 0.5);
    }

    public static double valueNoise(int seed, double x, double y, double z)
    {
        double sx = valueNoise(seed, x - z);
        double sy = valueNoise(seed + 101, y - x);
        double sz = valueNoise(seed + 202, z - y);
        return MathExtras.barronSpline(NumberTools.swayTight((sx + sy + sz - valueNoise(seed - 101, x + y + z)))
                , 0.333, 0.5);
    }

    public static double valueNoise(int seed, double x, double y, double z, double w)
    {
        double sx = valueNoise(seed, x - w);
        double sy = valueNoise(seed + 101, y - x);
        double sz = valueNoise(seed + 202, z - y);
        double sw = valueNoise(seed + 303, w - z);
        return MathExtras.barronSpline(NumberTools.swayTight((sx + sy + sz + sw - valueNoise(seed - 101, x + y + z + w)))
                , 0.333, 0.5);
    }

    public static double valueNoise(int seed, double x, double y, double z, double w, double u)
    {
        double sx = valueNoise(seed, x - u);
        double sy = valueNoise(seed + 101, y - x);
        double sz = valueNoise(seed + 202, z - y);
        double sw = valueNoise(seed + 303, w - z);
        double su = valueNoise(seed + 404, u - w);
        return MathExtras.barronSpline(NumberTools.swayTight((sx + sy + sz + sw + su - valueNoise(seed - 101, x + y + z + w + u)))
                , 0.333, 0.5);
    }

    public static double valueNoise(int seed, double x, double y, double z, double w, double u, double v)
    {
        double sx = valueNoise(seed, x - v);
        double sy = valueNoise(seed + 101, y - x);
        double sz = valueNoise(seed + 202, z - y);
        double sw = valueNoise(seed + 303, w - z);
        double su = valueNoise(seed + 404, u - w);
        double sv = valueNoise(seed + 505, v - u);
        return MathExtras.barronSpline(NumberTools.swayTight((sx + sy + sz + sw + su + sv - valueNoise(seed - 101, x + y + z + w + u + v)))
                , 0.333, 0.5);
    }

    @Override
    public double getNoise(double x, double y) {
        return valueNoise(seed, x, y) * 2 - 1;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y) * 2 - 1;
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return valueNoise(seed, x, y, z) * 2 - 1;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z) * 2 - 1;
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return valueNoise(seed, x, y, z, w) * 2 - 1;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w) * 2 - 1;
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u) {
        return valueNoise(seed, x, y, z, w, u) * 2 - 1;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w, u) * 2 - 1;
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return valueNoise(seed, x, y, z, w, u, v) * 2 - 1;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w, u, v) * 2 - 1;
    }
}
