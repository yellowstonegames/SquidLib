package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.Noise.*;
import static squidpony.squidmath.SeededNoise.*;

/**
 * A variant on gradient or Perlin noise, <a href="https://www.shadertoy.com/view/tldSRj">explored by Inigo Quilez
 * here</a> and initially discovered by "robobo1221". It is similar to {@link ClassicNoise} but produces more of its
 * output in the extreme range (close to -1.0 or 1.0) than Perlin ever does.
 * <br>
 * This is still a work in progress.
 */
@Beta
public class WaveNoise implements Noise2D, Noise3D, Noise4D, Noise5D, Noise6D {
    public static final WaveNoise instance = new WaveNoise();
    public long seed;
    public WaveNoise() {
        this(0x1337BEEFCAFEL);
    }

    public WaveNoise(final long seed) {
        this.seed = seed;
    }

    /**
     * Just in case we need a version of {@link NumberTools#zigzag(double)} that outputs 0.0 to 1.0.
     * @param value any double (except NaN)
     * @return a number between 0 and 1, linearly interpolating between the two at each integer peak or valley
     */
    protected static double zigzagTight(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor &= 1L;
        return value * (-floor | 1L) + floor;
    }

    //0xE60E2B722B53AEEBL, 0xCEBD76D9EDB6A8EFL, 0xB9C9AA3A51D00B65L, 0xA6F5777F6F88983FL, 0x9609C71EB7D03F7BL, 0x86D516E50B04AB1BL
    protected static double gradCoord2D(long seed, int x, int y,
                                        double xd, double yd) {
        seed += 0xE60E2B722B53AEEBL * x + 0xCEBD76D9EDB6A8EFL * y;
        return NumberTools.zigzag((xd * ((seed *= seed) >>> 44) + yd * ((seed * seed) >>> 44)) * 0x1p-20);
    }
    protected static double gradCoord3D(long seed, int x, int y, int z, double xd, double yd, double zd) {
        final int hash =
                (int)((seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L * z) * (seed)
                        >>> 59) * 3;
        return (xd * grad3d[hash] + yd * grad3d[hash + 1] + zd * grad3d[hash + 2]);
    }
    protected static double gradCoord4D(long seed, int x, int y, int z, int w,
                                        double xd, double yd, double zd, double wd) {
        final int hash =
                (int) ((seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L * z ^ 0xA6F5777F6F88983FL * w) * (seed)
                        >>> 56) & -4;
        return xd * grad4d[hash] + yd * grad4d[hash + 1] + zd * grad4d[hash + 2] + wd * grad4d[hash + 3];
    }
    protected static double gradCoord5D(long seed, int x, int y, int z, int w, int u,
                                        double xd, double yd, double zd, double wd, double ud) {
        final int hash =
                (int)((seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L * z ^ 0xA6F5777F6F88983FL * w ^ 0x9609C71EB7D03F7BL * u)
                        * (seed) >>> 56) * 5;
        return xd * grad5d[hash] + yd * grad5d[hash + 1] + zd * grad5d[hash + 2]
                + wd * grad5d[hash + 3] + ud * grad5d[hash + 4];
    }
    
    protected static double gradCoord6D(long seed, int x, int y, int z, int w, int u, int v,
                                        double xd, double yd, double zd, double wd, double ud, double vd) {
        final int hash =
                (int)(
                        (seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L *
                                z ^ 0xA6F5777F6F88983FL * w ^ 0x9609C71EB7D03F7BL * u ^ 0x86D516E50B04AB1BL * v) * (seed)
                        >>> 56) * 6;
        return xd * grad6d[hash] + yd * grad6d[hash + 1] + zd * grad6d[hash + 2]
                + wd * grad6d[hash + 3] + ud * grad6d[hash + 4] + vd * grad6d[hash + 5];
    }
    
    
//    protected static double gradCoord2DJitter(long seed, int x, int y,
//                                              double xd, double yd) {
//        final int hash = ((int)(((seed ^= 0xB4C4D * x ^ 0xEE2C3 * y) ^ seed >>> 13) * (seed)));
//        final double[] grad = phiGrad2[hash >>> 24], jitter = phiGrad2[hash >>> 16 & 0xFF];
//        return (xd + jitter[0] * 0.5) * grad[0] + (yd + jitter[1] * 0.5) * grad[1];
//    }

    @Override
    public double getNoise(final double x, final double y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, final long seed) {
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y);
        final double xf = x - x0, yf = y - y0;
        final double xa = Noise.emphasize(xf), ya = Noise.emphasize(yf);
//        final double res =
        return Noise.emphasizeSigned(
                lerp(
                        lerp(
                                gradCoord2D(seed, x0, y0, x, y),
                                gradCoord2D(seed, x0 + 1, y0, x, y),
                                xa),
                        lerp(
                                gradCoord2D(seed, x0, y0 + 1, x, y),
                                gradCoord2D(seed, x0 + 1, y0 + 1, x, y),
                                xa),
                        ya));//* 0.875;// * 1.4142;
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
        final double xf = x - x0, yf = y - y0, zf = z - z0;
        final double xa = Noise.emphasize(xf), ya = Noise.emphasize(yf), za = Noise.emphasize(zf);
//        final double res =
         return
                 Noise.emphasizeSigned(
                         lerp(
                                 lerp(
                                         lerp(
                                                 gradCoord3D(seed, x0, y0, z0, xf, yf, zf),
                                                 gradCoord3D(seed, x0+1, y0, z0, xf - 1, yf, zf),
                                                 xa),
                                         lerp(
                                                 gradCoord3D(seed, x0, y0+1, z0, xf, yf-1, zf),
                                                 gradCoord3D(seed, x0+1, y0+1, z0, xf - 1, yf - 1, zf),
                                                 xa),
                                         ya),
                                 lerp(
                                         lerp(
                                                 gradCoord3D(seed, x0, y0, z0+1, xf, yf, zf-1),
                                                 gradCoord3D(seed, x0+1, y0, z0+1, xf - 1, yf, zf-1),
                                                 xa),
                                         lerp(
                                                 gradCoord3D(seed, x0, y0+1, z0+1, xf, yf-1, zf-1),
                                                 gradCoord3D(seed, x0+1, y0+1, z0+1, xf - 1, yf - 1, zf-1),
                                                 xa),
                                         ya),
                                 za) * 1.0625);
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
        final double xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0;
        final double xa = Noise.emphasize(xf), ya = Noise.emphasize(yf), za = Noise.emphasize(zf), wa = Noise.emphasize(wf);
//        final double res =
        return
                Noise.emphasizeSigned(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0, xf, yf, zf, wf),
                                                        gradCoord4D(seed, x0+1, y0, z0, w0, xf - 1, yf, zf, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0, w0, xf, yf-1, zf, wf),
                                                        gradCoord4D(seed, x0+1, y0+1, z0, w0, xf - 1, yf - 1, zf, wf),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+1, w0, xf, yf, zf-1, wf),
                                                        gradCoord4D(seed, x0+1, y0, z0+1, w0, xf - 1, yf, zf-1, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0+1, w0, xf, yf-1, zf-1, wf),
                                                        gradCoord4D(seed, x0+1, y0+1, z0+1, w0, xf - 1, yf - 1, zf-1, wf),
                                                        xa),
                                                ya),
                                        za),
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0+1, xf, yf, zf, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0, z0, w0+1, xf - 1, yf, zf, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0, w0+1, xf, yf-1, zf, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0+1, z0, w0+1, xf - 1, yf - 1, zf, wf - 1),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+1, w0+1, xf, yf, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0, z0+1, w0+1, xf - 1, yf, zf-1, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0+1, w0+1, xf, yf-1, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0+1, z0+1, w0+1, xf - 1, yf - 1, zf-1, wf - 1),
                                                        xa),
                                                ya),
                                        za),
                                wa) * 0.555);
//        if(res < -1 || res > 1) System.out.println(res);
//        return res;
    }


    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        x *= 2.0;
        y *= 2.0;
        z *= 2.0;
        w *= 2.0;
        u *= 2.0;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w),
                u0 = fastFloor(u);
        final double xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0;
        //ad * ad * ad * (ad * (ad * 6.0 - 15.0) + 10.0)
        final double xa = xf * xf * xf * (xf * (xf * 6.0 - 15.0) + 10.0);
        final double ya = yf * yf * yf * (yf * (yf * 6.0 - 15.0) + 10.0);
        final double za = zf * zf * zf * (zf * (zf * 6.0 - 15.0) + 10.0);
        final double wa = wf * wf * wf * (wf * (wf * 6.0 - 15.0) + 10.0);
        final double ua = uf * uf * uf * (uf * (uf * 6.0 - 15.0) + 10.0);
//
//        final double xa = xf * xf * (3.0 - 2.0 * xf);
//        final double ya = yf * yf * (3.0 - 2.0 * yf);
//        final double za = zf * zf * (3.0 - 2.0 * zf);
//        final double wa = wf * wf * (3.0 - 2.0 * wf);
//        final double ua = uf * uf * (3.0 - 2.0 * uf);
//
//        final double res =
        return
                Noise.emphasizeSigned(
                lerp(lerp(
                        lerp(
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0, w0, u0, xf, yf, zf, wf, uf),
                                                gradCoord5D(seed, x0+1, y0, z0, w0, u0, xf-1, yf, zf, wf, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0, w0, u0, xf, yf-1, zf, wf, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0, w0, u0, xf-1, yf-1, zf, wf, uf), xa),
                                        ya),
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0+1, w0, u0, xf, yf, zf-1, wf, uf),
                                                gradCoord5D(seed, x0+1, y0, z0+1, w0, u0, xf-1, yf, zf-1, wf, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0, u0, xf, yf-1, zf-1, wf, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0+1, w0, u0, xf-1, yf-1, zf-1, wf, uf), xa),
                                        ya),
                                za),
                        lerp(
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0, w0+1, u0, xf, yf, zf, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0, z0, w0+1, u0, xf-1, yf, zf, wf-1, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0, w0+1, u0, xf, yf-1, zf, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0, w0+1, u0, xf-1, yf-1, zf, wf-1, uf), xa),
                                        ya),
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0+1, w0+1, u0, xf, yf, zf-1, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0, z0+1, w0+1, u0, xf-1, yf, zf-1, wf-1, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0+1, u0, xf, yf-1, zf-1, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0+1, w0+1, u0, xf-1, yf-1, zf-1, wf-1, uf), xa),
                                        ya),
                                za),
                        wa),
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0, w0, u0+1, xf, yf, zf, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0, w0, u0+1, xf-1, yf, zf, wf, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0, w0, u0+1, xf, yf-1, zf, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0, w0, u0+1, xf-1, yf-1, zf, wf, uf-1), xa),
                                                ya),
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0+1, w0, u0+1, xf, yf, zf-1, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0+1, w0, u0+1, xf-1, yf, zf-1, wf, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0, u0+1, xf, yf-1, zf-1, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0+1, w0, u0+1, xf-1, yf-1, zf-1, wf, uf-1), xa),
                                                ya),
                                        za),
                                lerp(
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0, w0+1, u0+1, xf, yf, zf, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0, w0+1, u0+1, xf-1, yf, zf, wf-1, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0, w0+1, u0+1, xf, yf-1, zf, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0, w0+1, u0+1, xf-1, yf-1, zf, wf-1, uf-1), xa),
                                                ya),
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0+1, w0+1, u0+1, xf, yf, zf-1, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0+1, w0+1, u0+1, xf-1, yf, zf-1, wf-1, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0+1, u0+1, xf, yf-1, zf-1, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0+1, w0+1, u0+1, xf-1, yf-1, zf-1, wf-1, uf-1), xa),
                                                ya),
                                        za),
                                wa),
                        ua) * 0.7777777 );
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
        final double xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0, vf = v - v0;
        final double xa = xf * xf * xf * (xf * (xf * 6.0 - 15.0) + 10.0);
        final double ya = yf * yf * yf * (yf * (yf * 6.0 - 15.0) + 10.0);
        final double za = zf * zf * zf * (zf * (zf * 6.0 - 15.0) + 10.0);
        final double wa = wf * wf * wf * (wf * (wf * 6.0 - 15.0) + 10.0);
        final double ua = uf * uf * uf * (uf * (uf * 6.0 - 15.0) + 10.0);
        final double va = vf * vf * vf * (vf * (vf * 6.0 - 15.0) + 10.0);
//        final double res =
        return Noise.emphasizeSigned(
                lerp(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0, xf, yf, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0, v0, xf - 1, yf, zf, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0, v0, xf, yf - 1, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0, v0, xf - 1, yf - 1, zf, wf, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0, v0, xf, yf, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0, v0, xf - 1, yf, zf - 1, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0, v0, xf, yf - 1, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0, v0, xf - 1, yf - 1, zf - 1, wf, uf, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0, v0, xf, yf, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0, v0, xf - 1, yf, zf, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0, v0, xf, yf - 1, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0, v0, xf - 1, yf - 1, zf, wf - 1, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0, v0, xf, yf, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0, v0, xf - 1, yf, zf - 1, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0, v0, xf, yf - 1, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0 + 1, v0, xf, yf, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0 + 1, v0, xf - 1, yf, zf, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0 + 1, v0, xf, yf - 1, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0 + 1, v0, xf - 1, yf - 1, zf, wf, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0 + 1, v0, xf, yf, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0 + 1, v0, xf - 1, yf, zf - 1, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0 + 1, v0, xf, yf - 1, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, v0, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0 + 1, v0, xf, yf, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0 + 1, v0, xf - 1, yf, zf, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0 + 1, v0, xf, yf - 1, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, v0, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0 + 1, v0, xf, yf, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, v0, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0 + 1, xf, yf, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0, v0 + 1, xf - 1, yf, zf, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0, v0 + 1, xf, yf - 1, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0, v0 + 1, xf - 1, yf - 1, zf, wf, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0, v0 + 1, xf, yf, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0, v0 + 1, xf - 1, yf, zf - 1, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0, v0 + 1, xf, yf - 1, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0, v0 + 1, xf - 1, yf - 1, zf - 1, wf, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0, v0 + 1, xf, yf, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0, v0 + 1, xf - 1, yf, zf, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0, v0 + 1, xf, yf - 1, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0, v0 + 1, xf - 1, yf - 1, zf, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0, v0 + 1, xf, yf, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0, v0 + 1, xf - 1, yf, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0, v0 + 1, xf, yf - 1, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, v0 + 1, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0 + 1, v0 + 1, xf, yf, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0 + 1, v0 + 1, xf - 1, yf, zf, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0 + 1, v0 + 1, xf, yf - 1, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0 + 1, v0 + 1, xf - 1, yf - 1, zf, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0 + 1, v0 + 1, xf, yf, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0 + 1, v0 + 1, xf - 1, yf, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0 + 1, v0 + 1, xf, yf - 1, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, v0 + 1, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0 + 1, v0 + 1, xf, yf, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0 + 1, v0 + 1, xf, yf - 1, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf, yf, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        va) * 1.61);
//        if(res < -1 || res > 1) System.out.println(res);
//        return res;
    }
}
