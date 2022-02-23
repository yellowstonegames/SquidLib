/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.SeededNoise.grad2d;

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
     * Range: 0.0 (exclusive) to 1.0 (exclusive)
     * @param state
     * @return
     */
    public static double formTightDouble(long state){
        state = (state = (state ^ state >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L) ^ state >>> 25;
        return NumberTools.longBitsToDouble(1022L - Long.numberOfLeadingZeros(state) << 52 | (state & 0x000FFFFFFFFFFFFFL));
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

    public static double rawNoise(final long seed, double n)
    {
//        n += formDouble(seed);
        final long floor = (n >= 0.0 ? (long) n : (long) n - 1L);
        long z = (0x9E3779B97F4A7C15L ^ seed) + floor * 0xACBD2BDCA2BFF56DL;
        final double start = formTightDouble(z),
                end = formTightDouble(z + 0xACBD2BDCA2BFF56DL);
//        final double start = formDouble((z << 1 ^ 0xD1B54A32D192ED03L) * Long.rotateLeft(z, 14)),
//                end = formDouble(((z += 0xACBD2BDCA2BFF56DL) << 1 ^ 0xD1B54A32D192ED03L) * Long.rotateLeft(z, 14));
//        final double start = formDouble(((z ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L)),
//                end = formDouble(((z + 0xACBD2BDCA2BFF56DL ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L));
//        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
//                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
        n -= floor;
//        x = MathExtras.barronSpline(x - floor, 2.0, 0.5);
        n *= n * n * (n * (n * 6f - 15f) + 10f);
        return (1.0 - n) * start + n * end;
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
        double sx = (swayTight(~seed, formTightDouble(seed) + x)) * 5,
                sy = (swayTight(seed, formTightDouble(seed+1010101010101010101L) + y)) * 5;
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

//        long sx = (NumberTools.doubleToRawLongBits(rawNoise(seed, x)) & 0x000FFFFFFFFFFFFFL);
//        long sy = (NumberTools.doubleToRawLongBits(rawNoise(seed + 1010101010101010101L, y)) & 0x000FFFFFFFFFFFFFL);
//        double tight = NumberTools.longBitsToDouble((sx + sy >>> 1) | 0x3FF0000000000000L) - 1.0;

//        double sd = valueNoise1(seed - (1010101010101010101L * 2), (x - y) * 0.707);
//        double se = valueNoise1(seed - 1010101010101010101L, (x + y) * 0.707);
//        double sx = valueNoise1(seed, x - sd);
//        double sy = valueNoise1(seed + (1010101010101010101L    ), y - se);
//        return (MathExtras.barronSpline((sx * sy + sd * se) * 0.25 + 0.5, 4.0, 0.5) - 0.5) * 2.0;
//        return (MathExtras.barronSpline(tight, 4.0, 0.5) - 0.5) * 2.0;

//        // decent but has strong line artifacts
//        int h0 = seed << 1 & 510;
//        double g0 = rawNoise(h0, grad2d[h0] * x + grad2d[h0+1] * y);
//        int h1 = seed >>> 7 & 510;
//        double g1 = rawNoise(h1, (grad2d[h1]) * x + grad2d[h1+1] * y-g0*3);
//        int h2 = seed >>> 15 & 510;
//        double g2 = rawNoise(h2, (grad2d[h2]) * x + grad2d[h2+1] * y-g1*3);
//        int h3 = seed >>> 23 & 510;
//        double g3 = rawNoise(h3, (grad2d[h3]) * x + grad2d[h3+1] * y-g2*3);
//        return (MathExtras.barronSpline((g0 + g1 + g2 + g3) * 0.25, 3.5, 0.5) - 0.5) * 2.0;
        seed *= 0x9E377;
        int h0 = seed << 1 & 510;
        int h1 = seed >>> 7 & 510;
        double nx = rawNoise(seed ^ 0xC13FA9A902A6328FL, grad2d[h0] * x + grad2d[h0+1] * y);
        double ny = rawNoise(seed ^ 0x91E10DA5C79E7B1DL, grad2d[h1] * y + grad2d[h1+1] * x);
        double cx = rawNoise(seed ^ ~0xC13FA9A902A6328FL, x) - ny;
        double cy = rawNoise(seed ^ ~0x91E10DA5C79E7B1DL, y) - nx;
        return (MathExtras.barronSpline((cx * cy) * 0.5 + 0.5, 8.0, 0.5) - 0.5) * 2.0;
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
