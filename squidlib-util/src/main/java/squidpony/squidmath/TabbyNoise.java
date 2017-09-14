package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * A different kind of noise that has spotted and striped areas, like a tabby cat.
 * Highly experimental and expected to change; currently has significant spiral-shaped artifacts from stretching.
 * Created by Tommy Ettinger on 9/2/2017.
 */
@Beta
public class TabbyNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    public static final TabbyNoise instance = new TabbyNoise();
    public int seed;
    public TabbyNoise()
    {
        seed = 0x1337BEEF;
    }
    public TabbyNoise(final int seed)
    {
        this.seed = seed;
    }
    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    public static double querp(final double start, final double end, double a){
        return (1.0 - (a *= a * a * (a * (a * 6.0 - 15.0) + 10.0))) * start + a * end;
    }
    /**
     * Like {@link Math#floor}, but returns an int. Doesn't consider weird doubles like INFINITY and NaN.
     * @param t the double to find the floor for
     * @return the floor of t, as an int
     */
    public static int fastFloor(double t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }
    public static double gauss(final long state) {
        final long s1 = state + 0x9E3779B97F4A7C15L,
                s2 = s1 + 0x9E3779B97F4A7C15L,
                y = (s1 ^ s1 >>> 30) * 0x5851F42D4C957F2DL,
                z = (s2 ^ s2 >>> 30) * 0x5851F42D4C957F2DL;
        return ((((y ^ y >>> 28) & 0x7FFFFFL) + ((y ^ y >>> 28) >>> 41))
                + (((z ^ z >>> 28) & 0x7FFFFFL) + ((z ^ z >>> 28) >>> 41))) * 0x1p-24 - 1.0;
    }
    @Override
    public double getNoise(double x, double y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, int seed) {
        final int sx = seed | 5, sy = sx + 22222;
        final long
                rx = NumberTools.splitMix64(sx),
                ry = NumberTools.splitMix64(sy);
        final double
                mx = ((((rx & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * x + ((((rx & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.89p-8) * y,
                my = ((((ry & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * y + ((((ry & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.89p-8) * x;
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my);
        return NumberTools.bounce(5.5 + 2.5 *
                (
                        querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(xf * sx + 100)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((xf+1) * sx + 100)),
                                mx - xf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(yf * sy + 200)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((yf+1) * sy + 200)),
                                my - yf)));
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoiseWithSeed(x, y, z, seed);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
        final long
                s = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (seed - s) ^ (s >>> 29 ^ s << 23),
                ry = (seed + rx) ^ (s >>> 17 ^ s << 36),
                rz = (ry - seed) ^ (s >>> 22 ^ s << 31);
        final double
        /*
                mx = ((((rx & 0x1FL) << 4 | 47L) - 255.5) * 0x1.4p-7) * x + ((((rx & 0x1F00L) >>> 4 | 47L) - 255.5) * 0x0.89p-7) * y + ((((rx & 0x1F0000L) >>> 12 | 47L) - 255.5) * 0x0.57p-7) * z,
                my = ((((ry & 0x1FL) << 4 | 47L) - 255.5) * 0x1.4p-7) * y + ((((ry & 0x1F00L) >>> 4 | 47L) - 255.5) * 0x0.89p-7) * z + ((((ry & 0x1F0000L) >>> 12 | 47L) - 255.5) * 0x0.57p-7) * x,
                mz = ((((rz & 0x1FL) << 4 | 47L) - 255.5) * 0x1.4p-7) * z + ((((rz & 0x1F00L) >>> 4 | 47L) - 255.5) * 0x0.89p-7) * x + ((((rz & 0x1F0000L) >>> 12 | 47L) - 255.5) * 0x0.57p-7) * y;
        */

                mx = ((((rx & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * x + ((((rx & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * y + ((((rx & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.7p-24) * z,
                my = ((((ry & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * y + ((((ry & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * z + ((((ry & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.7p-24) * x,
                mz = ((((rz & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * z + ((((rz & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * x + ((((rz & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.7p-24) * y;
                //mx = dx + dy, my = dy + dz, mz = dx + dz;

        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz);
        return
                NumberTools.bounce(5.875 + 2.0625 * (
                                querp(gauss(xf * 0xAE3779B97F4A7E35L),
                                gauss((xf+1) * 0xAE3779B97F4A7E35L),
                                mx - xf)
                                + querp(gauss(yf * 0xBE3779B97F4A7C55L),
                                gauss((yf+1) * 0xBE3779B97F4A7C55L),
                                my - yf)
                                + querp(gauss(zf * 0xCE3779B97F4A7A75L),
                                gauss((zf+1) * 0xCE3779B97F4A7A75L),
                                mz - zf)));
    }

    @Override
    public double getNoise(double x, double y, double z, double w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
        final int sx = seed | 5, sy = sx + 22222, sz = sy + 33333, sw = sz + 44444;
        final long
                rx = NumberTools.splitMix64(sx),
                ry = NumberTools.splitMix64(sy),
                rz = NumberTools.splitMix64(sz),
                rw = NumberTools.splitMix64(sw);
        final double
                mx = ((((rx & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * x + ((((rx & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.79p-8) * w + ((((rx & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.47p-8) * z + ((((rx & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.21p-8) * y,
                my = ((((ry & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * y + ((((ry & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.79p-8) * x + ((((ry & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.47p-8) * w + ((((ry & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.21p-8) * z,
                mz = ((((rz & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * z + ((((rz & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.79p-8) * y + ((((rz & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.47p-8) * x + ((((rz & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.21p-8) * w,
                mw = ((((rw & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * w + ((((rw & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.79p-8) * z + ((((rw & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.47p-8) * y + ((((rw & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.21p-8) * x;
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw);
        return NumberTools.bounce(5.5f + 2.5f *
                (
                        querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(xf * sx + 100)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((xf+1) * sx + 100)),
                                mx - xf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(yf * sy + 200)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((yf+1) * sy + 200)),
                                my - yf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(zf * sz + 300)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((zf+1) * sz + 300)),
                                mz - zf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(wf * sw + 400)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((wf+1) * sw + 400)),
                                mw - wf)));
    }

    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
        final int sx = seed | 5, sy = sx + 22222, sz = sy + 33333, sw = sz + 44444, su = sw + 55555, sv = su + 66666;

        final long
                rx = NumberTools.splitMix64(sx),
                ry = NumberTools.splitMix64(sy),
                rz = NumberTools.splitMix64(sz),
                rw = NumberTools.splitMix64(sw),
                ru = NumberTools.splitMix64(su),
                rv = NumberTools.splitMix64(sv);
        final double
                mx = ((((rx & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * x + ((((rx & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.67p-8) * v + ((((rx & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.41p-8) * u + ((((rx & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.17p-8) * w + ((((rx & 0x1F00000000L) >>> 28 | 15L) - 255.5) * 0x0.13p-8) * z + ((((rx & 0x1F0000000000L) >>> 36 | 15L) - 255.5) * 0x0.07p-8) * y,
                my = ((((ry & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * y + ((((ry & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.67p-8) * x + ((((ry & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.41p-8) * v + ((((ry & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.17p-8) * u + ((((ry & 0x1F00000000L) >>> 28 | 15L) - 255.5) * 0x0.13p-8) * w + ((((ry & 0x1F0000000000L) >>> 36 | 15L) - 255.5) * 0x0.07p-8) * z,
                mz = ((((rz & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * z + ((((rz & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.67p-8) * y + ((((rz & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.41p-8) * x + ((((rz & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.17p-8) * v + ((((rz & 0x1F00000000L) >>> 28 | 15L) - 255.5) * 0x0.13p-8) * u + ((((rz & 0x1F0000000000L) >>> 36 | 15L) - 255.5) * 0x0.07p-8) * w,
                mw = ((((rw & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * w + ((((rw & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.67p-8) * z + ((((rw & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.41p-8) * y + ((((rw & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.17p-8) * x + ((((rw & 0x1F00000000L) >>> 28 | 15L) - 255.5) * 0x0.13p-8) * v + ((((rw & 0x1F0000000000L) >>> 36 | 15L) - 255.5) * 0x0.07p-8) * u,
                mu = ((((ru & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * u + ((((rz & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.67p-8) * w + ((((rz & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.41p-8) * z + ((((rz & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.17p-8) * y + ((((rz & 0x1F00000000L) >>> 28 | 15L) - 255.5) * 0x0.13p-8) * x + ((((rz & 0x1F0000000000L) >>> 36 | 15L) - 255.5) * 0x0.07p-8) * v,
                mv = ((((rv & 0x1FL) << 4 | 15L) - 255.5) * 0x1.4p-8) * v + ((((rw & 0x1F00L) >>> 4 | 15L) - 255.5) * 0x0.67p-8) * u + ((((rw & 0x1F0000L) >>> 12 | 15L) - 255.5) * 0x0.41p-8) * w + ((((rw & 0x1F000000L) >>> 20 | 15L) - 255.5) * 0x0.17p-8) * z + ((((rw & 0x1F00000000L) >>> 28 | 15L) - 255.5) * 0x0.13p-8) * y + ((((rw & 0x1F0000000000L) >>> 36 | 15L) - 255.5) * 0x0.07p-8) * x;
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw),
                uf = fastFloor(mu),
                vf = fastFloor(mv);
        return NumberTools.bounce(5.5f + 2.5f *
                (
                        querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(xf * sx + 100)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((xf+1) * sx + 100)),
                                mx - xf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(yf * sy + 200)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((yf+1) * sy + 200)),
                                my - yf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(zf * sz + 300)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((zf+1) * sz + 300)),
                                mz - zf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(wf * sw + 400)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((wf+1) * sw + 400)),
                                mw - wf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(uf * su + 500)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((uf+1) * su + 500)),
                                mu - uf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(vf * sv + 600)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((vf+1) * sv + 600)),
                                mv - vf)));
    }
}
