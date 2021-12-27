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

    public static double swayTight(long seed, double x)
    {
//        x += seed * 0x1.9E377p-24;
//        long floor = (x >= 0.0 ? (long) x : (long) x - 1L);
//        x -= floor;
//        floor &= 1L;
////        return (MathExtras.barronSpline(x, 1.5, 0.5) * (-floor | 1L) + floor) * 2 - 1;
//        x *= x * x * (x * (x * 6f - 15f) + 10f);
//        return (x * (-floor | 1L) + floor) * 2 - 1;
//        x += seed * 0x1p-24;
        final long xFloor = Noise.longFloor(x), rise = 1L - (Noise.longFloor(x + x) & 2L);
        long bits = (seed ^ xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L;
        x -= xFloor;
        x *= x - 1.0;
//        final double h = NumberTools.longBitsToDouble(1026L - Long.numberOfTrailingZeros(bits) << 52 | (bits & 0x800FFFFFFFFFFFFFL)) - 4.0;
        final double h = NumberTools.longBitsToDouble(bits >>> 12 | 0x4040000000000000L) - 52.0;
        return Math.copySign(x * (h * x - 1.0), rise);
    }

    /**
     * Range: -1 to 1 exclusive, with a hole around 0.0 (just barely).
     * @param state
     * @return
     */
    public static double formDouble(long state){
//        return ((bits += (bits ^ bits >>> 32) * 0xD1B54A32D192ED03L) ^ bits >>> 29) * 0x1p-63;
        state = (state = (state ^ state >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L) ^ state >>> 25;
//        state = DiverRNG.determine(state);
        return NumberTools.longBitsToDouble(1022L - Long.numberOfTrailingZeros(state) << 52 | (state & 0x800FFFFFFFFFFFFFL));
    }

    /**
     * Range: -255.9999999999999 to 255.9999999999999 .
     * @param state
     * @return
     */
    public static double formLargeDouble(long state){
        state = (state = (state ^ state >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L) ^ state >>> 25;
        return NumberTools.longBitsToDouble(1023L - Long.numberOfTrailingZeros(state) << 52 | (state & 0x800FFFFFFFFFFFFFL));
    }

    /**
     * Produces output in the -1 to 1 range; otherwise like {@link NumberTools#swayRandomized(int, double)} but
     * using quintic interpolation instead of Hermite.
     * @param seed any int
     * @param x distance of 1D noise to travel
     * @return a noise value between -1 and 1, both inclusive
     */
    public static double valueNoise(final long seed, double x)
    {
        x += seed * 0x1p-58;
        final int floor = x >= 0.0 ? (int) x : (int) x - 1;
        long z = (seed + floor) * 0xACBD2BDCA2BFF56DL;

        final double start = formDouble(z),
                end = formDouble(z + 0xACBD2BDCA2BFF56DL);
        x -= floor;
        x *= x * x * (x * (x * 6f - 15f) + 10f);
        return (1.0 - x) * start + x * end;

//        final int floor = x >= 0.0 ? (int) x : (int) x - 1;
//        long z = (seed + floor) * 0xACBD2BDCA2BFF56DL;
////        final double start = NumberTools.longBitsToDouble(((z ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 12) | 0x4000000000000000L) - 3.0,
////                end = NumberTools.longBitsToDouble(((z + 1 ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 12) | 0x4000000000000000L) - 3.0;
//        final double start = formDouble((z ^ z >>> 29) * 0xD1B54A32D192ED03L),
//                end = formDouble(((z += 0xACBD2BDCA2BFF56DL) ^ z >>> 29) * 0xD1B54A32D192ED03L);
////        final double start = formDouble((z << 1 ^ 0xD1B54A32D192ED03L) * Long.rotateLeft(z, 14)),
////                end = formDouble(((z += 0xACBD2BDCA2BFF56DL) << 1 ^ 0xD1B54A32D192ED03L) * Long.rotateLeft(z, 14));
////        final double start = formDouble(((z ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L)),
////                end = formDouble(((z + 0xACBD2BDCA2BFF56DL ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L));
////        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
////                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
//        x -= floor;
//        x *= x * x * (x * (x * 3f - 7.5f) + 5f);
//        return (0.5 - x) * start + x * end;
    }

    /**
     * Produces output in the -1 to 1 range; otherwise like {@link NumberTools#swayRandomized(int, double)} but
     * using quintic interpolation instead of Hermite.
     * @param seed any int
     * @param x distance of 1D noise to travel
     * @return a noise value between -0.5 and 0.5, both inclusive
     */
    public static double valueNoise1(long seed, double x)
    {
        x += seed * 0x1p-58;
        final int floor = x >= 0.0 ? (int) x : (int) x - 1;
        long z = (seed + floor) * 0xACBD2BDCA2BFF56DL;

        final double start = formDouble(z),
                end = formDouble(z + 0xACBD2BDCA2BFF56DL);
        x -= floor;
        x *= x * x * (x * (x * 6f - 15f) + 10f);
        return (1.0 - x) * start + x * end;
    }

    public static double rawNoise(final int seed, double x)
    {
        final int floor = x >= 0.0 ? (int) x : (int) x - 1;
        long z = seed * 0xD1B54A32D192ED03L + floor * 0xACBD2BDCA2BFF56DL;
        final double start = formLargeDouble(z),
                end = formLargeDouble(z + 0xACBD2BDCA2BFF56DL);
//        final double start = formDouble((z << 1 ^ 0xD1B54A32D192ED03L) * Long.rotateLeft(z, 14)),
//                end = formDouble(((z += 0xACBD2BDCA2BFF56DL) << 1 ^ 0xD1B54A32D192ED03L) * Long.rotateLeft(z, 14));
//        final double start = formDouble(((z ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L)),
//                end = formDouble(((z + 0xACBD2BDCA2BFF56DL ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L));
//        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
//                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
        x -= floor;
//        x = MathExtras.barronSpline(x - floor, 2.0, 0.5);
        x *= x * x * (x * (x * 6f - 15f) + 10f);
        return (1.0 - x) * start + x * end;
    }

//    public static double valueNoise(final int seed, double x)
//    {
//        final int floor = x >= 0.0 ? (int) x : (int) x - 1;
//        int z = seed + floor;
//        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
//                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
//        x -= floor;
//        x *= x * x * (x * (x * 3f - 7.5f) + 5f);
//        return (0.5 - x) * start + x * end;
//    }

//    public static double valueNoise(final int seed, double x) {
//        x += seed * 0x1p-24;
//        final long xFloor = Noise.longFloor(x), rise = 1L - (Noise.longFloor(x + x) & 2L);
//        x -= xFloor;
//        x *= x - 1.0;
//        final double h = NumberTools.longBitsToDouble((seed ^ xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 12 | 0x4030000000000000L) - 26.0;
//        return rise * x * (h * x - 0.5);
////        final double h = NumberTools.longBitsToDouble((seed ^ xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 12 | 0x4040000000000000L) - 52.0;
////        return rise * x * (h * x - 1.0);
//    }

    /*
    vec3 swayRandomized(vec3 seed, vec3 value)
{
    return sin(seed.xyz + value.zxy - cos(seed.zxy + value.yzx) + cos(seed.yzx + value.xyz));
}
     */

    public static double wobble(int seed, double x, double y){
        double sx = (swayTight(~seed, formLargeDouble(seed) + x)) * 5,
                sy = (swayTight(seed, formLargeDouble(seed+1010101010101010101L) + y)) * 5;
        return (MathExtras.barronSpline(valueNoise(seed - 1010101010101010101L, (sx * sy)) * 0.5 + 0.5, 2.222, 0.5) - 0.5) * 2.0;
//        double sx = formLargeDouble(seed), sy = formLargeDouble(seed+1010101010101010101L);
//        return valueNoise(seed, sx + sy + x + y - valueNoise(~seed, sy - x + y) + valueNoise(~seed, sx - y + x));
    }

    public static double valueNoise(int seed, double x, double y)
    {
//        double sx = rawNoise(seed, x);
//        double sy = rawNoise(seed + 1010101, y);
////        return valueNoise(seed - 1010101, 4 * (sx + x - sy + y)) + valueNoise(seed - 2020202, 4 * (sy + x - sx - y));
//        return //MathExtras.barronSpline(
//                NumberTools.sway(sx + sy)// * (2.0 / Math.PI)
////                //, 0.333, 0.5)
//                ;
//        return wobble(seed, x, y);
//        return wobble(seed, x + wobble(~seed, y, -x), y - wobble(~seed, -y, x));
        double sd = valueNoise1(seed - 1010101010101010101L, x + y);
        double sx = valueNoise1(seed, x - sd);
        double sy = valueNoise1(seed + (1010101010101010101L    ), y - sx);
        return valueNoise(~seed, (sx + sy - valueNoise1(seed ^ 1010101010101010101L, sd - x - y)));
    }

    public static double valueNoise(int seed, double x, double y, double z)
    {
        double sx = valueNoise1(seed, x - z * 0.618);
        double sy = valueNoise1(seed + (1010101010101010101L    ), y - x * 0.618);
        double sz = valueNoise1(seed + (1010101010101010101L * 2), z - y * 0.618);
        return valueNoise(~seed, (sx + sy + sz) - valueNoise1(seed - 1010101010101010101L, x + y + z));
    }

    public static double valueNoise(int seed, double x, double y, double z, double w)
    {
        double sx = valueNoise1(seed, x - w * 0.618);
        double sy = valueNoise1(seed + (1010101010101010101L    ), y - x * 0.618);
        double sz = valueNoise1(seed + (1010101010101010101L * 2), z - y * 0.618);
        double sw = valueNoise1(seed + (1010101010101010101L * 3), w - z * 0.618);
        return valueNoise(~seed, (sx + sy + sz + sw - valueNoise1(seed - 1010101010101010101L, x + y + z + w)));
    }

    public static double valueNoise(int seed, double x, double y, double z, double w, double u)
    {
        double sx = valueNoise1(seed, x - u * 0.618);
        double sy = valueNoise1(seed + (1010101010101010101L    ), y - x * 0.618);
        double sz = valueNoise1(seed + (1010101010101010101L * 2), z - y * 0.618);
        double sw = valueNoise1(seed + (1010101010101010101L * 3), w - z * 0.618);
        double su = valueNoise1(seed + (1010101010101010101L * 4), u - w * 0.618);
        return valueNoise1(~seed, (sx + sy + sz + sw + su - valueNoise1(seed - 1010101010101010101L, x + y + z + w + u)));
    }

    public static double valueNoise(int seed, double x, double y, double z, double w, double u, double v)
    {
        double sx = valueNoise1(seed, x - v * 0.618);
        double sy = valueNoise1(seed + (1010101010101010101L    ), y - x * 0.618);
        double sz = valueNoise1(seed + (1010101010101010101L * 2), z - y * 0.618);
        double sw = valueNoise1(seed + (1010101010101010101L * 3), w - z * 0.618);
        double su = valueNoise1(seed + (1010101010101010101L * 4), u - w * 0.618);
        double sv = valueNoise1(seed + (1010101010101010101L * 5), v - u * 0.618);
        return valueNoise(~seed, (sx + sy + sz + sw + su + sv - valueNoise1(seed - 1010101010101010101L, x + y + z + w + u + v)));
    }

    @Override
    public double getNoise(double x, double y) {
        return valueNoise(seed, x, y) /* * 2 - 1 */;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y) /* * 2 - 1 */;
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return valueNoise(seed, x, y, z) /* * 2 - 1 */;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z) /* * 2 - 1 */;
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return valueNoise(seed, x, y, z, w) /* * 2 - 1 */;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w) /* * 2 - 1 */;
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u) {
        return valueNoise(seed, x, y, z, w, u) /* * 2 - 1 */;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w, u) /* * 2 - 1 */;
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return valueNoise(seed, x, y, z, w, u, v) /* * 2 - 1 */;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w, u, v) /* * 2 - 1 */;
    }
}
