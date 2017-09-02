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

    @Override
    public double getNoise(double x, double y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, int seed) {
        final int sx = seed | 5, sy = sx + 22222;
        final double
                mx = x + y * 0.89f * NumberTools.randomFloat(sx) + 0.6,
                my = y + x * 0.89f * NumberTools.randomFloat(sy) + 0.6;
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my);
        return NumberTools.bounce(5f + 2.4f *
                (querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(xf * sx + 100)),
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
        final int sx = seed | 5, sy = sx + 22222, sz = sy + 33333;
        final double
                mx = x + (NumberTools.randomFloat(sx) * 1.4 + 0.6) * (y * 0.89 + z * 0.57),
                my = y + (NumberTools.randomFloat(sy) * 1.4 + 0.6) * (z * 0.89 + x * 0.57),
                mz = z + (NumberTools.randomFloat(sz) * 1.4 + 0.6) * (x * 0.89 + y * 0.57);
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz);
        return NumberTools.bounce(5.0 + 2.4 *
                (
                        querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(xf * sx + 100)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((xf+1) * sx + 100)),
                                mx - xf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(yf * sy + 200)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((yf+1) * sy + 200)),
                                my - yf)
                                + querp(NumberTools.formCurvedFloat(NumberTools.splitMix64(zf * sz + 300)),
                                NumberTools.formCurvedFloat(NumberTools.splitMix64((zf+1) * sz + 300)),
                                mz - zf)));
    }

    @Override
    public double getNoise(double x, double y, double z, double w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
        final int sx = seed | 5, sy = sx + 22222, sz = sy + 33333, sw = sz + 44444;
        final double
                mx = x + (NumberTools.randomFloat(sx) + 0.6) * (w * 0.79 + z * 0.47 + y * 0.21),
                my = y + (NumberTools.randomFloat(sy) + 0.6) * (x * 0.79 + w * 0.47 + z * 0.21),
                mz = z + (NumberTools.randomFloat(sz) + 0.6) * (y * 0.79 + x * 0.47 + w * 0.21),
                mw = w + (NumberTools.randomFloat(sw) + 0.6) * (z * 0.79 + y * 0.47 + x * 0.21);
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw);
        return NumberTools.bounce(5f + 2.4f *
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
        final double
                mx = x + (NumberTools.randomFloat(sx) + 0.6) * (v * 0.67 + u * 0.41 + w * 0.17 + z * 0.13 + y * 0.07),
                my = y + (NumberTools.randomFloat(sy) + 0.6) * (x * 0.67 + v * 0.41 + u * 0.17 + w * 0.13 + z * 0.07),
                mz = z + (NumberTools.randomFloat(sz) + 0.6) * (y * 0.67 + x * 0.41 + v * 0.17 + u * 0.13 + w * 0.07),
                mw = w + (NumberTools.randomFloat(sw) + 0.6) * (z * 0.67 + y * 0.41 + x * 0.17 + v * 0.13 + u * 0.07),
                mu = u + (NumberTools.randomFloat(su) + 0.6) * (w * 0.67 + z * 0.41 + y * 0.17 + x * 0.13 + v * 0.07),
                mv = v + (NumberTools.randomFloat(sv) + 0.6) * (u * 0.67 + w * 0.41 + z * 0.17 + y * 0.13 + x * 0.07);
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw),
                uf = fastFloor(mu),
                vf = fastFloor(mv);
        return NumberTools.bounce(5f + 2.4f *
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
