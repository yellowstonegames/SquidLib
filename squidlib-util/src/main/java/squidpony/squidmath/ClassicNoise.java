package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.Noise.cerp;
import static squidpony.squidmath.Noise.fastFloor;
import static squidpony.squidmath.PerlinNoise.phiGrad2;
import static squidpony.squidmath.SeededNoise.gradient6DLUT;
import static squidpony.squidmath.WhirlingNoise.grad3d;
import static squidpony.squidmath.WhirlingNoise.grad4;

/**
 * "Classic Perlin" noise, as opposed to the Simplex Noise also created by Ken Perlin (which is in {@link PerlinNoise}).
 * This noise can in theory be scaled up to arbitrary dimensions, but in practice uses unreasonably hefty amounts of
 * memory when dimensionality exceeds 10 or so, since it needs to hash {@code Math.pow(2, dimensionality)} points per
 * sample of noise, which involves over a thousand points in 10 dimensions and over a million points in 20 dimensions.
 * For that reason, it's limited to 6D noise here, and also implements 2D, 3D, and 4D. Its performance is surprisingly
 * good at 2D, 3D, and 4D but trails off quickly at 6D. Its quality is not great in 2D, but it's better than some
 * implementations of classic Perlin noise. This uses different gradient vectors than what was recommended in the
 * "Improved Perlin Noise" paper, since the ones this uses avoid 45-degree angular artifacts.
 */
@Beta
public class ClassicNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    public static final ClassicNoise instance = new ClassicNoise();
    public long seed;

    public ClassicNoise() {
        this(0x1337BEEFCAFEL);
    }

    public ClassicNoise(final long seed) {
        this.seed = seed;
    }

    protected static double gradCoord2D(long seed, int x, int y,
                                        double xd, double yd) {
        final double[] grad = phiGrad2[
                ((int)(((seed ^= 0xB4C4D * x ^ 0xEE2C1 * y) ^ seed >>> 13) * (seed))
                        >>> 24)];
        return xd * grad[0] + yd * grad[1];
    }
    protected static double gradCoord4D(long seed, int x, int y, int z, int w,
                                        double xd, double yd, double zd, double wd) {
        final int hash =
                ((int)(((seed ^= 0xB4C4D * x ^ 0xEE2C1 * y ^ 0xA7E07 * z ^ 0xCD5E9 * w) ^ seed >>> 13) * (seed))
                        >>> 24) & -4;
        return xd * grad4[hash] + yd * grad4[hash + 1] + zd * grad4[hash + 2] + wd * grad4[hash + 3];
    }
    protected static double gradCoord6D(long seed, int x, int y, int z, int w, int u, int v,
                                        double xd, double yd, double zd, double wd, double ud, double vd) {
        final int hash =
                ((int)(((seed ^= 0xB4C4D * x ^ 0xEE2C1 * y ^ 0xA7E07 * z ^ 0xCD5E9 * w ^ 0x94B5B * u ^ 0xD2385 * v)
                        ^ seed >>> 13) * (seed))
                        >>> 24) * 6;
        return xd * gradient6DLUT[hash] + yd * gradient6DLUT[hash + 1] + zd * gradient6DLUT[hash + 2]
                + wd * gradient6DLUT[hash + 3] + ud * gradient6DLUT[hash + 4] + vd * gradient6DLUT[hash + 5];
    }
    protected static double gradCoord3D(long seed, int x, int y, int z, double xd, double yd, double zd) {
        final int hash =
                ((int)(((seed ^= 0xB4C4D * x ^ 0xEE2C1 * y ^ 0xA7E07 * z) ^ seed >>> 13) * (seed))
                        >>> 27) * 3;
        return (xd * grad3d[hash] + yd * grad3d[hash + 1] + zd * grad3d[hash + 2]);
    }

    @Override
    public double getNoise(final double x, final double y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, final long seed) {
        x *= 2.0;
        y *= 2.0;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y);         
//        final double res =
        return 
                cerp(cerp(gradCoord2D(seed, x0, y0, x - x0, y - y0), gradCoord2D(seed, x0+1, y0, x - x0 - 1, y - y0), x - x0),
                                cerp(gradCoord2D(seed, x0, y0+1, x - x0, y - y0-1), gradCoord2D(seed, x0+1, y0+1, x - x0 - 1, y - y0 - 1), x - x0),
                                y - y0) * 1.4142;
//        if(res < -1.0 || res > 1.0) System.out.println(res);
//        return res;
    }

    @Override
    public double getNoise(final double x, final double y, final double z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, final long seed) {
        x *= 2.0;
        y *= 2.0;
        z *= 2.0;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z);
//        final double res =
         return 
                 cerp(cerp(cerp(gradCoord3D(seed, x0, y0, z0, x - x0, y - y0, z - z0), gradCoord3D(seed, x0+1, y0, z0, x - x0 - 1, y - y0, z - z0), x - x0),
                cerp(gradCoord3D(seed, x0, y0+1, z0, x - x0, y - y0-1, z - z0), gradCoord3D(seed, x0+1, y0+1, z0, x - x0 - 1, y - y0 - 1, z - z0), x - x0),
                y - y0),
                cerp(cerp(gradCoord3D(seed, x0, y0, z0+1, x - x0, y - y0, z - z0-1), gradCoord3D(seed, x0+1, y0, z0+1, x - x0 - 1, y - y0, z - z0-1), x - x0),
                        cerp(gradCoord3D(seed, x0, y0+1, z0+1, x - x0, y - y0-1, z - z0-1), gradCoord3D(seed, x0+1, y0+1, z0+1, x - x0 - 1, y - y0 - 1, z - z0-1), x - x0),
                        y - y0), z - z0) * 1.0625;
//        if(res < -1 || res > 1) System.out.println(res);
//        return res;
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, final long seed) {
        x *= 2.0;
        y *= 2.0;
        z *= 2.0;
        w *= 2.0;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w);
//        final double res =
        return 
                cerp(cerp(cerp(cerp(gradCoord4D(seed, x0, y0, z0, w0, x - x0, y - y0, z - z0, w - w0), gradCoord4D(seed, x0+1, y0, z0, w0, x - x0 - 1, y - y0, z - z0, w - w0), x - x0),
                        cerp(gradCoord4D(seed, x0, y0+1, z0, w0, x - x0, y - y0-1, z - z0, w - w0), gradCoord4D(seed, x0+1, y0+1, z0, w0, x - x0 - 1, y - y0 - 1, z - z0, w - w0), x - x0),
                        y - y0),
                        cerp(cerp(gradCoord4D(seed, x0, y0, z0+1, w0, x - x0, y - y0, z - z0-1, w - w0), gradCoord4D(seed, x0+1, y0, z0+1, w0, x - x0 - 1, y - y0, z - z0-1, w - w0), x - x0),
                                cerp(gradCoord4D(seed, x0, y0+1, z0+1, w0, x - x0, y - y0-1, z - z0-1, w - w0), gradCoord4D(seed, x0+1, y0+1, z0+1, w0, x - x0 - 1, y - y0 - 1, z - z0-1, w - w0), x - x0),
                                y - y0), 
                        z - z0),
                        cerp(cerp(cerp(gradCoord4D(seed, x0, y0, z0, w0+1, x - x0, y - y0, z - z0, w - w0 - 1), gradCoord4D(seed, x0+1, y0, z0, w0+1, x - x0 - 1, y - y0, z - z0, w - w0 - 1), x - x0),
                                cerp(gradCoord4D(seed, x0, y0+1, z0, w0+1, x - x0, y - y0-1, z - z0, w - w0 - 1), gradCoord4D(seed, x0+1, y0+1, z0, w0+1, x - x0 - 1, y - y0 - 1, z - z0, w - w0 - 1), x - x0),
                                y - y0),
                                cerp(cerp(gradCoord4D(seed, x0, y0, z0+1, w0+1, x - x0, y - y0, z - z0-1, w - w0 - 1), gradCoord4D(seed, x0+1, y0, z0+1, w0+1, x - x0 - 1, y - y0, z - z0-1, w - w0 - 1), x - x0),
                                        cerp(gradCoord4D(seed, x0, y0+1, z0+1, w0+1, x - x0, y - y0-1, z - z0-1, w - w0 - 1), gradCoord4D(seed, x0+1, y0+1, z0+1, w0+1, x - x0 - 1, y - y0 - 1, z - z0-1, w - w0 - 1), x - x0),
                                        y - y0),
                                z - z0),
                        w - w0) * 0.555;
//        if(res < -1 || res > 1) System.out.println(res);
//        return res;
    }


    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        x *= 2.0;
        y *= 2.0;
        z *= 2.0;
        w *= 2.0;
        u *= 2.0;
        v *= 2.0;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w),
                u0 = fastFloor(u),
                v0 = fastFloor(v);
        final double xd = x - x0, yd = y - y0, zd = z - z0, wd = w - w0, ud = u - u0, vd = v - v0; 
//        final double res =
        return 
        cerp(cerp(cerp(
                cerp(
                        cerp(
                                cerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0, xd, yd, zd, wd, ud, vd),
                                        gradCoord6D(seed, x0+1, y0, z0, w0, u0, v0, xd-1, yd, zd, wd, ud, vd), xd),
                                cerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0, v0, xd, yd-1, zd, wd, ud, vd),
                                        gradCoord6D(seed, x0+1, y0+1, z0, w0, u0, v0, xd-1, yd-1, zd, wd, ud, vd), xd),
                                yd),
                        cerp(
                                cerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0, v0, xd, yd, zd-1, wd, ud, vd),
                                        gradCoord6D(seed, x0+1, y0, z0+1, w0, u0, v0, xd-1, yd, zd-1, wd, ud, vd), xd),
                                cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0, v0, xd, yd-1, zd-1, wd, ud, vd),
                                        gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0, v0, xd-1, yd-1, zd-1, wd, ud, vd), xd),
                                yd),
                        zd),
                cerp(
                        cerp(
                                cerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0, v0, xd, yd, zd, wd-1, ud, vd),
                                        gradCoord6D(seed, x0+1, y0, z0, w0+1, u0, v0, xd-1, yd, zd, wd-1, ud, vd), xd),
                                cerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0, v0, xd, yd-1, zd, wd-1, ud, vd),
                                        gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0, v0, xd-1, yd-1, zd, wd-1, ud, vd), xd),
                                yd),
                        cerp(
                                cerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0, v0, xd, yd, zd-1, wd-1, ud, vd),
                                        gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0, v0, xd-1, yd, zd-1, wd-1, ud, vd), xd),
                                cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0, v0, xd, yd-1, zd-1, wd-1, ud, vd),
                                        gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0, v0, xd-1, yd-1, zd-1, wd-1, ud, vd), xd),
                                yd),
                        zd),
                wd),
                cerp(
                        cerp(
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0, w0, u0+1, v0, xd, yd, zd, wd, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0, z0, w0, u0+1, v0, xd-1, yd, zd, wd, ud-1, vd), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0+1, v0, xd, yd-1, zd, wd, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0+1, z0, w0, u0+1, v0, xd-1, yd-1, zd, wd, ud-1, vd), xd),
                                        yd),
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0+1, v0, xd, yd, zd-1, wd, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0, z0+1, w0, u0+1, v0, xd-1, yd, zd-1, wd, ud-1, vd), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0+1, v0, xd, yd-1, zd-1, wd, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0+1, v0, xd-1, yd-1, zd-1, wd, ud-1, vd), xd),
                                        yd),
                                zd),
                        cerp(
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0+1, v0, xd, yd, zd, wd-1, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0, z0, w0+1, u0+1, v0, xd-1, yd, zd, wd-1, ud-1, vd), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0+1, v0, xd, yd-1, zd, wd-1, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0+1, v0, xd-1, yd-1, zd, wd-1, ud-1, vd), xd),
                                        yd),
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0+1, v0, xd, yd, zd-1, wd-1, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0+1, v0, xd-1, yd, zd-1, wd-1, ud-1, vd), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0+1, v0, xd, yd-1, zd-1, wd-1, ud-1, vd),
                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0+1, v0, xd-1, yd-1, zd-1, wd-1, ud-1, vd), xd),
                                        yd),
                                zd),
                        wd),
                ud),
                cerp(
                        cerp(
                        cerp(
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0+1, xd, yd, zd, wd, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0, z0, w0, u0, v0+1, xd-1, yd, zd, wd, ud, vd-1), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0, v0+1, xd, yd-1, zd, wd, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0+1, z0, w0, u0, v0+1, xd-1, yd-1, zd, wd, ud, vd-1), xd),
                                        yd),
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0, v0+1, xd, yd, zd-1, wd, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0, z0+1, w0, u0, v0+1, xd-1, yd, zd-1, wd, ud, vd-1), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0, v0+1, xd, yd-1, zd-1, wd, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0, v0+1, xd-1, yd-1, zd-1, wd, ud, vd-1), xd),
                                        yd),
                                zd),
                        cerp(
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0, v0+1, xd, yd, zd, wd-1, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0, z0, w0+1, u0, v0+1, xd-1, yd, zd, wd-1, ud, vd-1), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0, v0+1, xd, yd-1, zd, wd-1, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0, v0+1, xd-1, yd-1, zd, wd-1, ud, vd-1), xd),
                                        yd),
                                cerp(
                                        cerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0, v0+1, xd, yd, zd-1, wd-1, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0, v0+1, xd-1, yd, zd-1, wd-1, ud, vd-1), xd),
                                        cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0, v0+1, xd, yd-1, zd-1, wd-1, ud, vd-1),
                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0, v0+1, xd-1, yd-1, zd-1, wd-1, ud, vd-1), xd),
                                        yd),
                                zd),
                        wd),
                        cerp(
                                cerp(
                                        cerp(
                                                cerp(gradCoord6D(seed, x0, y0, z0, w0, u0+1, v0+1, xd, yd, zd, wd, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0, z0, w0, u0+1, v0+1, xd-1, yd, zd, wd, ud-1, vd-1), xd),
                                                cerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0+1, v0+1, xd, yd-1, zd, wd, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0+1, z0, w0, u0+1, v0+1, xd-1, yd-1, zd, wd, ud-1, vd-1), xd),
                                                yd),
                                        cerp(
                                                cerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0+1, v0+1, xd, yd, zd-1, wd, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0, z0+1, w0, u0+1, v0+1, xd-1, yd, zd-1, wd, ud-1, vd-1), xd),
                                                cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0+1, v0+1, xd, yd-1, zd-1, wd, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0+1, v0+1, xd-1, yd-1, zd-1, wd, ud-1, vd-1), xd),
                                                yd),
                                        zd),
                                cerp(
                                        cerp(
                                                cerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0+1, v0+1, xd, yd, zd, wd-1, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0, z0, w0+1, u0+1, v0+1, xd-1, yd, zd, wd-1, ud-1, vd-1), xd),
                                                cerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0+1, v0+1, xd, yd-1, zd, wd-1, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0+1, v0+1, xd-1, yd-1, zd, wd-1, ud-1, vd-1), xd),
                                                yd),
                                        cerp(
                                                cerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0+1, v0+1, xd, yd, zd-1, wd-1, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0+1, v0+1, xd-1, yd, zd-1, wd-1, ud-1, vd-1), xd),
                                                cerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0+1, v0+1, xd, yd-1, zd-1, wd-1, ud-1, vd-1),
                                                        gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0+1, v0+1, xd-1, yd-1, zd-1, wd-1, ud-1, vd-1), xd),
                                                yd),
                                        zd),
                                wd),
                        ud),
                vd) * 1.875;
//        if(res < -1 || res > 1) System.out.println(res);
//        return res;
    }
}
