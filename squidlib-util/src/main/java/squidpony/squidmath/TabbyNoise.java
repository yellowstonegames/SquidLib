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
    public static long fastFloor(double t) {
        return t >= 0 ? (long) t : (long) t - 1;
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
        final long
                s = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (seed - s) ^ (s >>> 29 ^ s << 23),
                ry = (seed + rx) ^ (s >>> 17 ^ s << 36);
        final double
                mx = ((((rx & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * x + ((((rx & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * y,
                my = ((((ry & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * y + ((((ry & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * x;

        final long
                xf = fastFloor(mx),
                yf = fastFloor(my);
        return
                NumberTools.bounce(5.875 + 2.0625 * (
                        querp(gauss(xf * 0xAE3779B97F4A7E35L),
                                gauss((xf+1) * 0xAE3779B97F4A7E35L),
                                mx - xf)
                                + querp(gauss(yf * 0xBE3779B97F4A7C55L),
                                gauss((yf+1) * 0xBE3779B97F4A7C55L),
                                my - yf)));
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoiseWithSeed(x, y, z, seed);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
/*        long s = seed ^ (long)~seed << 32;
        final double
                rx = gauss(s += 0x763779B97F4A7C17L),
                ry = gauss(s += 0x663779B97F4A7C17L),
                rz = gauss(s += 0x563779B97F4A7C17L),
                ax = NumberTools.zigzag(x * rx),
                ay = NumberTools.zigzag(y * ry),
                az = NumberTools.zigzag(z * rz),
                dx = ((ay + az) * x),
                dy = ((az + ax) * y),
                dz = ((ax + ay) * z),
                mx = dx + dy * ax - dz * ay * az,
                my = dy + dz * ay - dx * az * ax,
                mz = dz + dx * az - dy * ax * ay
        ;
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
                                */
        final long
                s  = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = s >>> 27 ^ s << 27,
                ry = s >>> 23 ^ s << 23,
                rz = s >>> 29 ^ s << 29;
        final double
                grx = gauss(rx) + 0.75,
                gry = gauss(ry) + 0.75,
                grz = gauss(rz) + 0.75,
                cx = NumberTools.zigzag(x) * (gry * grz + 1.125) * 0x0.93p-1,
                cy = NumberTools.zigzag(y) * (grz * grx + 1.125) * 0x0.93p-1,
                cz = NumberTools.zigzag(z) * (grx * gry + 1.125) * 0x0.93p-1,
                ax = x + (cy * cz),
                ay = y + (cz * cx),
                az = z + (cx * cy),
                mx = ((((rx & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ax + ((((rx & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ay + ((((rx & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * az,
                my = ((((ry & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ay + ((((ry & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * az + ((((ry & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ax,
                mz = ((((rz & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * az + ((((rz & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ax + ((((rz & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ay;

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
        final long
                s = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (seed - s) ^ (s >>> 29 ^ s << 23),
                ry = (seed + rx) ^ (s >>> 17 ^ s << 36),
                rz = (ry - seed) ^ (s >>> 22 ^ s << 31),
                rw = (seed + rz - ry - rx) ^ (s >>> 19 ^ s << 29);
        final double
                mx = ((((rx & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * x + ((((rx & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * w + ((((rx & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * z + ((((rx & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * y,
                my = ((((ry & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * y + ((((ry & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * x + ((((ry & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * w + ((((ry & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * z,
                mz = ((((rz & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * z + ((((rz & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * y + ((((rz & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * x + ((((rz & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * w,
                mw = ((((rw & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * w + ((((rw & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * z + ((((rw & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * y + ((((rw & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * x;

        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw);
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
                                mz - zf)
                                + querp(gauss(wf * 0xDE3779B97F4A7895L),
                                gauss((wf+1) * 0xDE3779B97F4A7895L),
                                mw - wf)));
    }

    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
        final long
                s = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (seed - s) ^ (s >>> 29 ^ s << 23),
                ry = (seed + rx) ^ (s >>> 17 ^ s << 36),
                rz = (ry - seed) ^ (s >>> 22 ^ s << 31),
                rw = (seed + rz - ry * rx) ^ (s >>> 19 ^ s << 29),
                ru = (seed - rw + rz * ry) ^ (s >>> 24 ^ s << 27),
                rv = (seed + ru * rw - rx * rz) ^ (s >>> 21 ^ s << 34);
        final double
                mx = ((((rx & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * x + ((((rx & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * v + ((((rx & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * u + ((((rx & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * w + ((((rx & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * z + ((((rx & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * y,
                my = ((((ry & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * y + ((((ry & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * x + ((((ry & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * v + ((((ry & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * u + ((((ry & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * w + ((((ry & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * z,
                mz = ((((rz & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * z + ((((rz & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * y + ((((rz & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * x + ((((rz & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * v + ((((rz & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * u + ((((rz & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * w,
                mw = ((((rw & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * w + ((((rw & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * z + ((((rw & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * y + ((((rw & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * x + ((((rw & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * v + ((((rw & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * u,
                mu = ((((ru & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * u + ((((ru & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * w + ((((ru & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * z + ((((ru & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * y + ((((ru & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * x + ((((ru & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * v,
                mv = ((((rv & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * v + ((((rv & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * u + ((((rv & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * w + ((((rv & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * z + ((((rv & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * y + ((((rv & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * x;

        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw),
                uf = fastFloor(mu),
                vf = fastFloor(mv);
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
                                mz - zf)
                                + querp(gauss(wf * 0xDE3779B97F4A7895L),
                                gauss((wf+1) * 0xDE3779B97F4A7895L),
                                mw - wf)
                                + querp(gauss(uf * 0xEE3779B97F4A76B5L),
                                gauss((uf+1) * 0xEE3779B97F4A76B5L),
                                mu - uf)
                                + querp(gauss(vf * 0xFE3779B97F4A74D5L),
                                gauss((vf+1) * 0xFE3779B97F4A74D5L),
                                mv - vf)));
    }
}
