package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.Noise.cerp;
import static squidpony.squidmath.Noise.longFloor;

/**
 * A different kind of noise that has spotted and striped areas, like a tabby cat.
 * Highly experimental and expected to change; currently has significant linear artifacts, though they do wiggle.
 * Created by Tommy Ettinger on 9/2/2017.
 */
@Beta
public class MummyNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    public static final MummyNoise instance = new MummyNoise();
    public long seedX, seedY, seedZ, seedW, seedU, seedV;

    public MummyNoise() {
        this(0x1337BEEF);
    }

    public MummyNoise(final int seed) {
        seedX = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seed + 0xC6BC279692B5CC83L);
        seedY = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedX ^ 0xC7BC279692B5CB83L);
        seedZ = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedY ^ 0xC8BC279692B5CA83L);
        seedW = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedZ ^ 0xC9BC279692B5C983L);
        seedU = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedW ^ 0xCABC279692B5C883L);
        seedV = 0x9E3779B97F4A7C15L * LinnormRNG.determine(seedU ^ 0xCBBC279692B5C783L);
    }

    /**
     * The same as {@link LinnormRNG#determine(long)}, except this assumes state has already been multiplied by
     * 0x632BE59BD9B4E019L.
     * @param state a long that should change in increments of 0x632BE59BD9B4E019L
     * @return a pseudo-random permutation of state
     */
    public static long determine(long state)
    {
        return (state = ((state = ((state ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    //    public static double gauss(final long state) {
//        final long s1 = state + 0x9E3779B97F4A7C15L,
//                s2 = s1 + 0x9E3779B97F4A7C15L,
//                y = (s1 ^ s1 >>> 26) * 0x2545F4914F6CDD1DL,
//                z = (s2 ^ s2 >>> 26) * 0x2545F4914F6CDD1DL;
//        return ((((y ^ y >>> 28) & 0x7FFFFFL) + ((y ^ y >>> 28) >>> 41))
//                + (((z ^ z >>> 28) & 0x7FFFFFL) + ((z ^ z >>> 28) >>> 41))) * 0x1p-24 - 1.0;
//    }
    public static double signedDouble(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) >> 12) * 0x1p-52;
    }

    @Override
    public double getNoise(double x, double y) {
        return getNoiseWithSeeds(x, y, seedX, seedY);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        final long
                rs = LinnormRNG.determine(seed ^ ~seed << 32),
                rx = 0x9E3779B97F4A7C15L * (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = 0x9E3779B97F4A7C15L * (rx >>> 23 ^ rx << 23) * (rx | 1L);
        return getNoiseWithSeeds(x, y, rx, ry);
    }

    public static double getNoiseWithSeeds(final double x, final double y,
                                    final long seedX, final long seedY) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bx1 = bx0+seedX,
                by1 = by0+seedY;
        return
                NumberTools.sway(
                        cerp(cerp(determine(bx0 + by0) * 0x1.25p-62, determine(bx1 + by0) * 0x1.25p-62,
                                x - xf),
                                cerp(determine(bx0 + by1) * 0x1.25p-62, determine(bx1 + by1) * 0x1.25p-62, x - xf),
                                y - yf));
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoiseWithSeeds(x, y, z, seedX, seedY, seedZ);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        final long
                rs = LinnormRNG.determine(seed ^ ~seed << 32),
                rx = 0x9E3779B97F4A7C15L * (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = 0x9E3779B97F4A7C15L * (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = 0x9E3779B97F4A7C15L * (ry >>> 23 ^ ry << 23) * (ry | 1L);
        return getNoiseWithSeeds(x, y, z, rx, ry, rz);
    }

    public static double getNoiseWithSeeds(final double x, final double y, final double z,
                                    final long seedX, final long seedY, final long seedZ) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bz0 = zf * seedZ,
                bx1 = bx0+seedX,
                by1 = by0+seedY,
                bz1 = bz0+seedZ;
        return NumberTools.sway(
                cerp(
                        cerp(
                                cerp(determine(bx0 + by0 + bz0) * 0x1.25p-62, determine(bx1 + by0 + bz0) * 0x1.25p-62, x - xf),
                                cerp(determine(bx0 + by1 + bz0) * 0x1.25p-62, determine(bx1 + by1 + bz0) * 0x1.25p-62, x - xf),
                                y - yf),
                        cerp(
                                cerp(determine(bx0 + by0 + bz1) * 0x1.25p-62, determine(bx1 + by0 + bz1) * 0x1.25p-62, x - xf),
                                cerp(determine(bx0 + by1 + bz1) * 0x1.25p-62, determine(bx1 + by1 + bz1) * 0x1.25p-62, x - xf),
                                y - yf),
                        z - zf));
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return getNoiseWithSeeds(x, y, z, w, seedX, seedY, seedZ, seedW);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        final long
                rs = LinnormRNG.determine(seed ^ ~seed << 32),
                rx = 0x9E3779B97F4A7C15L * (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = 0x9E3779B97F4A7C15L * (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = 0x9E3779B97F4A7C15L * (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = 0x9E3779B97F4A7C15L * (rz >>> 23 ^ rz << 23) * (rz | 1L);
        return getNoiseWithSeeds(x, y, z, w, rx, ry, rz, rw);
    }

    public static double getNoiseWithSeeds(final double x, final double y, final double z, final double w,
                                    final long seedX, final long seedY, final long seedZ, final long seedW) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                wf = longFloor(w),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bz0 = zf * seedZ,
                bw0 = wf * seedW,
                bx1 = bx0+seedX,
                by1 = by0+seedY,
                bz1 = bz0+seedZ,
                bw1 = bw0+seedW;
        return NumberTools.sway(
                cerp(
                        cerp(
                                cerp(
                                        cerp(determine(bx0 + by0 + bz0 + bw0) * 0x1.25p-62, determine(bx1 + by0 + bz0 + bw0) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz0 + bw0) * 0x1.25p-62, determine(bx1 + by1 + bz0 + bw0) * 0x1.25p-62, x - xf),
                                        y - yf),
                                cerp(
                                        cerp(determine(bx0 + by0 + bz1 + bw0) * 0x1.25p-62, determine(bx1 + by0 + bz1 + bw0) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz1 + bw0) * 0x1.25p-62, determine(bx1 + by1 + bz1 + bw0) * 0x1.25p-62, x - xf),
                                        y - yf),
                                z - zf),
                        cerp(
                                cerp(
                                        cerp(determine(bx0 + by0 + bz0 + bw1) * 0x1.25p-62, determine(bx1 + by0 + bz0 + bw1) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz0 + bw1) * 0x1.25p-62, determine(bx1 + by1 + bz0 + bw1) * 0x1.25p-62, x - xf),
                                        y - yf),
                                cerp(
                                        cerp(determine(bx0 + by0 + bz1 + bw1) * 0x1.25p-62, determine(bx1 + by0 + bz1 + bw1) * 0x1.25p-62, x - xf),
                                        cerp(determine(bx0 + by1 + bz1 + bw1) * 0x1.25p-62, determine(bx1 + by1 + bz1 + bw1) * 0x1.25p-62, x - xf),
                                        y - yf),
                                z - zf),
                        w - wf));
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return getNoiseWithSeeds(x, y, z, w, u, v,
                seedX, seedY, seedZ, seedW, seedU, seedV);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, long seed) {
        final long
                rs = LinnormRNG.determine(seed ^ ~seed << 32),
                rx = 0x9E3779B97F4A7C15L * (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = 0x9E3779B97F4A7C15L * (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = 0x9E3779B97F4A7C15L * (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = 0x9E3779B97F4A7C15L * (rz >>> 23 ^ rz << 23) * (rz | 1L),
                ru = 0x9E3779B97F4A7C15L * (rw >>> 23 ^ rw << 23) * (rw | 1L),
                rv = 0x9E3779B97F4A7C15L * (ru >>> 23 ^ ru << 23) * (ru | 1L);
        return getNoiseWithSeeds(x, y, z, w, u, v, rx, ry, rz, rw, ru, rv);
    }

    public static double getNoiseWithSeeds(final double x, final double y, final double z,
                                    final double w, final double u, final double v,
                                    final long seedX, final long seedY, final long seedZ,
                                    final long seedW, final long seedU, final long seedV) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                wf = longFloor(w),
                uf = longFloor(u),
                vf = longFloor(v),
                bx0 = xf * seedX,
                by0 = yf * seedY,
                bz0 = zf * seedZ,
                bw0 = wf * seedW,
                bu0 = uf * seedU,
                bv0 = vf * seedV,
                bx1 = bx0+seedX,
                by1 = by0+seedY,
                bz1 = bz0+seedZ,
                bw1 = bw0+seedW,
                bu1 = bu0+seedU,
                bv1 = bv0+seedV;
        return NumberTools.sway(
                cerp(
                        cerp(
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu0 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu1 + bv0) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                u - uf),

                        cerp(
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu0 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                cerp(
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw0 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        cerp(
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz0 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                cerp(
                                                        cerp(determine(bx0 + by0 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by0 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        cerp(determine(bx0 + by1 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, determine(bx1 + by1 + bz1 + bw1 + bu1 + bv1) * 0x1.12bp-61, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                u - uf),
                        v - vf));
    }
    private transient long[] scratch3;
    private transient double[] scratch;
    private transient int lastLen = -1;
    private transient double lastEffect = 0x1.12bp-61;
    public final double arbitraryNoise(long seed, double... coordinates) {
        final int len = coordinates.length, upper = 1 << len;
        final double effect;
        if(len != lastLen)
        {
            lastLen = len;
            lastEffect = effect = 0x1.81p-62 * Math.pow(1.1875, len);
        }
        else
            effect = lastEffect;
        if(scratch3 == null || scratch3.length < len * 3)
            scratch3 = new long[len * 3];
        if(scratch == null || scratch.length < upper)
            scratch = new double[upper];
        for (int i = 0; i < len; i++) {
            seed = LinnormRNG.determine(seed + 0xC6BC279692B5CC83L ^ ~seed << 32);
            scratch3[i * 3 + 1] = (scratch3[i * 3] = (scratch3[i * 3 + 2] = longFloor(coordinates[i])) * seed) + seed;
        }
        long working;
        for (int i = 0; i < upper; i++) {
            working = 0L;
            for (int j = 0; j < len; j++) {
                working += scratch3[j * 3 + (i >> j & 1)];
            }
            scratch[i] = determine(working) * effect;
        }
        for (int i = 0; i < len; ++i) {
            for (int j = 0, t = upper >> i; j < t; j += 2) {
                scratch[j >> 1] = cerp(scratch[j], scratch[j + 1], coordinates[i] - scratch3[i * 3 + 2]);
            }
        }
        return NumberTools.sway(scratch[0]);
    }

}
