package squidpony.squidmath;

import squidpony.annotation.Beta;

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
        seedX = ThrustRNG.determine(seed + 0xC6BC279692B5CC83L);
        seedY = ThrustRNG.determine(seedX ^ 0xC7BC279692B5CB83L);
        seedZ = ThrustRNG.determine(seedY ^ 0xC8BC279692B5CA83L);
        seedW = ThrustRNG.determine(seedZ ^ 0xC9BC279692B5C983L);
        seedU = ThrustRNG.determine(seedW ^ 0xCABC279692B5C883L);
        seedV = ThrustRNG.determine(seedU ^ 0xCBBC279692B5C783L);
    }

    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    public static double querp0(final double start, final double end, double a) {
        return (1.0 - (a *= a * a * (a * (a * 6.0 - 15.0) + 10.0))) * start + a * end;
    }

    // actually cerp
    private static double querp(final double start, final double end, double a) {
        return (1.0 - (a *= a * (3.0 - 2.0 * a))) * start + a * end;
    }

    // actually lerp
    private static double lerp(final double start, final double end, double a) {
        return (1.0 - a) * start + a * end;
    }

    /**
     * Like {@link Math#floor}, but returns an int. Doesn't consider weird doubles like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as an int
     */
    public static long longFloor(double t) {
        return t >= 0 ? (long) t : (long) t - 1;
    }

    //    public static double gauss(final long state) {
//        final long s1 = state + 0x9E3779B97F4A7C15L,
//                s2 = s1 + 0x9E3779B97F4A7C15L,
//                y = (s1 ^ s1 >>> 30) * 0x5851F42D4C957F2DL,
//                z = (s2 ^ s2 >>> 30) * 0x5851F42D4C957F2DL;
//        return ((((y ^ y >>> 28) & 0x7FFFFFL) + ((y ^ y >>> 28) >>> 41))
//                + (((z ^ z >>> 28) & 0x7FFFFFL) + ((z ^ z >>> 28) >>> 41))) * 0x1p-24 - 1.0;
//    }
    public static double gauss(long state) {
        state = (state ^ state >>> 30) * 0x5851F42D4C957F2DL;
        return (state ^ state >>> 28) * 0x1p-63;
    }

    @Override
    public double getNoise(double x, double y) {
        return getNoiseWithSeeds(x, y, seedX, seedY);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final int seed) {
        final long
                rs = ThrustRNG.determine(seed ^ 0x5851F42D4C957F2DL),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L);
        return getNoiseWithSeeds(x, y, rx, ry);
    }

    public double getNoiseWithSeeds(final double x, final double y,
                                    final long seedX, final long seedY) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                bx0 = ThrustRNG.determine(xf ^ seedX),
                by0 = ThrustRNG.determine(yf ^ seedY),
                bx1 = ThrustRNG.determine(xf + 1 ^ seedX),
                by1 = ThrustRNG.determine(yf + 1 ^ seedY);
        return
                NumberTools.sway(
                        querp(querp((bx0 * by0) * 0x1.5p-62, (bx1 * by0) * 0x1.5p-62,
                                x - xf),
                                querp((bx0 * by1) * 0x1.5p-62, (bx1 * by1) * 0x1.5p-62, x - xf),
                                y - yf));
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoiseWithSeeds(x, y, z, seedX, seedY, seedZ);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
        final long
                rs = ThrustRNG.determine(seed ^ (long) ~seed << 32),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = (ry >>> 23 ^ ry << 23) * (ry | 1L);
        return getNoiseWithSeeds(x, y, z, rx, ry, rz);
    }

    public double getNoiseWithSeeds(final double x, final double y, final double z,
                                    final long seedX, final long seedY, final long seedZ) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                bx0 = ThrustRNG.determine(xf ^ seedX),
                by0 = ThrustRNG.determine(yf ^ seedY),
                bz0 = ThrustRNG.determine(zf ^ seedZ),
                bx1 = ThrustRNG.determine(xf + 1 ^ seedX),
                by1 = ThrustRNG.determine(yf + 1 ^ seedY),
                bz1 = ThrustRNG.determine(zf + 1 ^ seedZ);
        return NumberTools.sway(
                querp(
                        querp(
                                querp((bx0 * by0 * bz0) * 0x1.5p-62, (bx1 * by0 * bz0) * 0x1.5p-62,
                                        x - xf),
                                querp((bx0 * by1 * bz0) * 0x1.5p-62, (bx1 * by1 * bz0) * 0x1.5p-62, x - xf),
                                y - yf),
                        querp(
                                querp((bx0 * by0 * bz1) * 0x1.5p-62, (bx1 * by0 * bz1) * 0x1.5p-62,
                                        x - xf),
                                querp((bx0 * by1 * bz1) * 0x1.5p-62, (bx1 * by1 * bz1) * 0x1.5p-62, x - xf),
                                y - yf),
                        z - zf));
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return getNoiseWithSeeds(x, y, z, w, seedX, seedY, seedZ, seedW);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
        final long
                rs = ThrustRNG.determine(seed ^ (long) ~seed << 32),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = (rz >>> 23 ^ rz << 23) * (rz | 1L);
        return getNoiseWithSeeds(x, y, z, w, rx, ry, rz, rw);
    }

    public double getNoiseWithSeeds(final double x, final double y, final double z, final double w,
                                    final long seedX, final long seedY, final long seedZ, final long seedW) {
        final long
                xf = longFloor(x),
                yf = longFloor(y),
                zf = longFloor(z),
                wf = longFloor(w),
                bx0 = ThrustRNG.determine(xf ^ seedX),
                by0 = ThrustRNG.determine(yf ^ seedY),
                bz0 = ThrustRNG.determine(zf ^ seedZ),
                bw0 = ThrustRNG.determine(wf ^ seedW),
                bx1 = ThrustRNG.determine(xf + 1 ^ seedX),
                by1 = ThrustRNG.determine(yf + 1 ^ seedY),
                bz1 = ThrustRNG.determine(zf + 1 ^ seedZ),
                bw1 = ThrustRNG.determine(wf + 1 ^ seedW);
        return NumberTools.sway(
                querp(
                        querp(
                                querp(
                                        querp((bx0 * by0 * bz0 * bw0) * 0x1.5p-62, (bx1 * by0 * bz0 * bw0) * 0x1.5p-62,
                                                x - xf),
                                        querp((bx0 * by1 * bz0 * bw0) * 0x1.5p-62, (bx1 * by1 * bz0 * bw0) * 0x1.5p-62, x - xf),
                                        y - yf),
                                querp(
                                        querp((bx0 * by0 * bz1 * bw0) * 0x1.5p-62, (bx1 * by0 * bz1 * bw0) * 0x1.5p-62,
                                                x - xf),
                                        querp((bx0 * by1 * bz1 * bw0) * 0x1.5p-62, (bx1 * by1 * bz1 * bw0) * 0x1.5p-62, x - xf),
                                        y - yf),
                                z - zf),
                        querp(
                                querp(
                                        querp((bx0 * by0 * bz0 * bw1) * 0x1.5p-62, (bx1 * by0 * bz0 * bw1) * 0x1.5p-62,
                                                x - xf),
                                        querp((bx0 * by1 * bz0 * bw1) * 0x1.5p-62, (bx1 * by1 * bz0 * bw1) * 0x1.5p-62, x - xf),
                                        y - yf),
                                querp(
                                        querp((bx0 * by0 * bz1 * bw1) * 0x1.5p-62, (bx1 * by0 * bz1 * bw1) * 0x1.5p-62,
                                                x - xf),
                                        querp((bx0 * by1 * bz1 * bw1) * 0x1.5p-62, (bx1 * by1 * bz1 * bw1) * 0x1.5p-62, x - xf),
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
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, int seed) {
        final long
                rs = ThrustRNG.determine(seed ^ (long) ~seed << 32),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = (rz >>> 23 ^ rz << 23) * (rz | 1L),
                ru = (rw >>> 23 ^ rw << 23) * (rw | 1L),
                rv = (ru >>> 23 ^ ru << 23) * (ru | 1L);
        return getNoiseWithSeeds(x, y, z, w, u, v, rx, ry, rz, rw, ru, rv);
    }

    public double getNoiseWithSeeds(final double x, final double y, final double z,
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
                bx0 = ThrustRNG.determine(xf ^ seedX),
                by0 = ThrustRNG.determine(yf ^ seedY),
                bz0 = ThrustRNG.determine(zf ^ seedZ),
                bw0 = ThrustRNG.determine(wf ^ seedW),
                bu0 = ThrustRNG.determine(uf ^ seedU),
                bv0 = ThrustRNG.determine(vf ^ seedV),
                bx1 = ThrustRNG.determine(xf + 1 ^ seedX),
                by1 = ThrustRNG.determine(yf + 1 ^ seedY),
                bz1 = ThrustRNG.determine(zf + 1 ^ seedZ),
                bw1 = ThrustRNG.determine(wf + 1 ^ seedW),
                bu1 = ThrustRNG.determine(uf + 1 ^ seedU),
                bv1 = ThrustRNG.determine(vf + 1 ^ seedV);
        return NumberTools.sway(
                querp(
                        querp(
                                querp(
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw0 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw0 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw0 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw0 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw0 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw0 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw0 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw0 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw1 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw1 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw1 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw1 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw1 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw1 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw1 * bu0 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw1 * bu0 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                querp(
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw0 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw0 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw0 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw0 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw0 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw0 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw0 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw0 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw1 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw1 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw1 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw1 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw1 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw1 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw1 * bu1 * bv0) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw1 * bu1 * bv0) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                u - uf),

                        querp(
                                querp(
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw0 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw0 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw0 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw0 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw0 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw0 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw0 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw0 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw1 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw1 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw1 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw1 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw1 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw1 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw1 * bu0 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw1 * bu0 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                querp(
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw0 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw0 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw0 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw0 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw0 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw0 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw0 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw0 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        querp(
                                                querp(
                                                        querp((bx0 * by0 * bz0 * bw1 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz0 * bw1 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz0 * bw1 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz0 * bw1 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                querp(
                                                        querp((bx0 * by0 * bz1 * bw1 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by0 * bz1 * bw1 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        querp((bx0 * by1 * bz1 * bw1 * bu1 * bv1) * 0x1.Bp-62, (bx1 * by1 * bz1 * bw1 * bu1 * bv1) * 0x1.Bp-62, x - xf),
                                                        y - yf),
                                                z - zf),
                                        w - wf),
                                u - uf),
                        v - vf));
    }
}
