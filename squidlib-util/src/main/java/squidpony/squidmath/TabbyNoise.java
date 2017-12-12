package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.Noise.fastFloor;
import static squidpony.squidmath.Noise.querp;

/**
 * A different kind of noise that has spotted and striped areas, like a tabby cat.
 * Highly experimental and expected to change; currently has significant linear artifacts, though they do wiggle.
 * Created by Tommy Ettinger on 9/2/2017.
 */
@Beta
public class TabbyNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    public static final TabbyNoise instance = new TabbyNoise();
    public long seedX, seedY, seedZ, seedW, seedU, seedV;
    public double randX, randY, randZ, randW, randU, randV;
    public TabbyNoise()
    {
        this(0x1337BEEF);
    }
    public TabbyNoise(final int seed)
    {
        randX = gauss(seedX = ThrustRNG.determine(seed  + 0xC6BC279692B5CC83L)) + 0.625;
        randY = gauss(seedY = ThrustRNG.determine(seedX + 0xC7BC279692B5CC83L)) + 0.625;
        randZ = gauss(seedZ = ThrustRNG.determine(seedY + 0xC8BC279692B5CC83L)) + 0.625;
        randW = gauss(seedW = ThrustRNG.determine(seedZ + 0xC9BC279692B5CC83L)) + 0.625;
        randU = gauss(seedU = ThrustRNG.determine(seedW + 0xCABC279692B5CC83L)) + 0.625;
        randV = gauss(seedV = ThrustRNG.determine(seedU + 0xCBBC279692B5CC83L)) + 0.625;

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
    state = (state ^ state >>> 26) * 0x2545F4914F6CDD1DL;
    return (state ^ state >>> 28) * 0x1.5p-64;
}

    @Override
    public double getNoise(double x, double y) {
        return getNoiseWithSeeds(x, y, seedX, seedY, randX, randY);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        final long
                rs  = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L);
        return getNoiseWithSeeds(x, y, rx, ry, gauss(rx) + 0.625, gauss(ry) + 0.625);
    }

    public double getNoiseWithSeeds(final double x, final double y,
                                    final long seedX, final long seedY,
                                    final double randX, final double randY) {
        final double
                grx = randX * 0.625,
                gry = randY * 0.625,
                cx = NumberTools.sway(x) * (gry + 1.125) * 0x0.93p-1,
                cy = NumberTools.sway(y) * (grx + 1.125) * 0x0.93p-1,
                ax = x + (cy * (0.35 + gry)),
                ay = y + (cx * (0.35 + grx)),
                mx = ((((seedX & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ax + ((((seedX & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ay,
                my = ((((seedY & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ay + ((((seedY & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ax;
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my);
        return
                //NumberTools.bounce(5.875 + 2.0625 * (
                NumberTools.sway( 2.1625 * (
                        querp(gauss(xf * 0xAE3779B97F4A7E35L),
                                gauss((xf+1) * 0xAE3779B97F4A7E35L),
                                mx - xf)
                                + querp(gauss(yf * 0xBE3779B97F4A7C55L),
                                gauss((yf+1) * 0xBE3779B97F4A7C55L),
                                my - yf)));

    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoiseWithSeeds(x, y, z, seedX, seedY, seedZ, randX, randY, randZ);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        final long
                rs  = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = (ry >>> 23 ^ ry << 23) * (ry | 1L);
        return getNoiseWithSeeds(x, y, z, rx, ry, rz,
                gauss(rx) + 0.625, gauss(ry) + 0.625, gauss(rz) + 0.625);
    }
    public double getNoiseWithSeeds(final double x, final double y, final double z,
                                    final long seedX, final long seedY, final long seedZ,
                                    final double randX, final double randY, final double randZ) {
        final double
                cx = NumberTools.sway(x) * (randY * randZ + 1.125) * 0x0.93p-1,
                cy = NumberTools.sway(y) * (randZ * randX + 1.125) * 0x0.93p-1,
                cz = NumberTools.sway(z) * (randX * randY + 1.125) * 0x0.93p-1,
                ax = x + (cy * cz),
                ay = y + (cz * cx),
                az = z + (cx * cy),
                mx = ((((seedX & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ax + ((((seedX & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ay + ((((seedX & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * az,
                my = ((((seedY & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ay + ((((seedY & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * az + ((((seedY & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ax,
                mz = ((((seedZ & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * az + ((((seedZ & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ax + ((((seedZ & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ay;

        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz);
        return
                //NumberTools.bounce(5.875 + 2.0625 * (
                NumberTools.sway( 2.1625 * (
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
    public double getNoise(final double x, final double y, final double z, final double w) {
        return getNoiseWithSeeds(x, y, z, w, seedX, seedY, seedZ, seedW, randX, randY, randZ, randW);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        final long
                rs  = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = (rz >>> 23 ^ rz << 23) * (rz | 1L);
        return getNoiseWithSeeds(x, y, z, w, rx, ry, rz, rw,
                gauss(rx) + 0.625, gauss(ry) + 0.625, gauss(rz) + 0.625, gauss(rw) + 0.625);
    }
    public double getNoiseWithSeeds(final double x, final double y, final double z, final double w,
                                    final long seedX, final long seedY, final long seedZ, final long seedW,
                                    final double randX, final double randY, final double randZ, final double randW) {
        final double
                cx = NumberTools.sway(x) * (randY * randZ + 1.125) * 0x0.93p-1,
                cy = NumberTools.sway(y) * (randZ * randW + 1.125) * 0x0.93p-1,
                cz = NumberTools.sway(z) * (randW * randX + 1.125) * 0x0.93p-1,
                cw = NumberTools.sway(w) * (randX * randY + 1.125) * 0x0.93p-1,
                ax = x + (cz * cw),
                ay = y + (cw * cx),
                az = z + (cx * cy),
                aw = w + (cy * cz),
                mx = ((((seedX & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ax + ((((seedX & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * aw + ((((seedX & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * az + ((((seedX & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * ay,
                my = ((((seedY & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ay + ((((seedY & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ax + ((((seedY & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * aw + ((((seedY & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * az,
                mz = ((((seedZ & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * az + ((((seedZ & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ay + ((((seedZ & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ax + ((((seedZ & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * aw,
                mw = ((((seedW & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * aw + ((((seedW & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * az + ((((seedW & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ay + ((((seedW & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * ax;
        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw);
        return
                //NumberTools.bounce(5.875 + 2.0625 * (
                NumberTools.sway( 2.1625 * (
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
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return getNoiseWithSeeds(x, y, z, w, u, v,
                seedX, seedY, seedZ, seedW, seedU, seedV,
                randX, randY, randZ, randW, randU, randV);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, long seed) {
        final long
                rs  = ThrustRNG.determine(seed ^ (long)~seed << 32),
                rx = (rs >>> 23 ^ rs << 23) * (rs | 1L),
                ry = (rx >>> 23 ^ rx << 23) * (rx | 1L),
                rz = (ry >>> 23 ^ ry << 23) * (ry | 1L),
                rw = (rz >>> 23 ^ rz << 23) * (rz | 1L),
                ru = (rw >>> 23 ^ rw << 23) * (rw | 1L),
                rv = (ru >>> 23 ^ ru << 23) * (ru | 1L);
        return getNoiseWithSeeds(x, y, z, w, u, v, rx, ry, rz, rw, ru, rv,
                gauss(rx) + 0.625, gauss(ry) + 0.625, gauss(rz) + 0.625,
                gauss(rw) + 0.625, gauss(ru) + 0.625, gauss(rv) + 0.625);
    }
    public double getNoiseWithSeeds(final double x, final double y, final double z,
                                    final double w, final double u, final double v,
                                    final long seedX, final long seedY, final long seedZ,
                                    final long seedW, final long seedU, final long seedV,
                                    final double randX, final double randY, final double randZ,
                                    final double randW, final double randU, final double randV) {
        final double
                cx = NumberTools.sway(x) * (randY * randZ + 1.125) * 0x0.93p-1,
                cy = NumberTools.sway(y) * (randZ * randW + 1.125) * 0x0.93p-1,
                cz = NumberTools.sway(z) * (randW * randU + 1.125) * 0x0.93p-1,
                cw = NumberTools.sway(w) * (randU * randV + 1.125) * 0x0.93p-1,
                cu = NumberTools.sway(u) * (randV * randX + 1.125) * 0x0.93p-1,
                cv = NumberTools.sway(v) * (randX * randY + 1.125) * 0x0.93p-1,
                ax = x + (cw * cu),
                ay = y + (cu * cv),
                az = z + (cv * cx),
                aw = w + (cx * cy),
                au = u + (cy * cz),
                av = v + (cz * cw),
                mx = ((((seedX & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ax + ((((seedX & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * av + ((((seedX & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * au + ((((seedX & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * aw + ((((seedX & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * az + ((((seedX & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * ay,
                my = ((((seedY & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * ay + ((((seedY & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ax + ((((seedY & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * av + ((((seedY & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * au + ((((seedY & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * aw + ((((seedY & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * az,
                mz = ((((seedZ & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * az + ((((seedZ & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * ay + ((((seedZ & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ax + ((((seedZ & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * av + ((((seedZ & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * au + ((((seedZ & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * aw,
                mw = ((((seedW & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * aw + ((((seedW & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * az + ((((seedW & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * ay + ((((seedW & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * ax + ((((seedW & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * av + ((((seedW & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * au,
                mu = ((((seedU & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * au + ((((seedU & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * aw + ((((seedU & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * az + ((((seedU & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * ay + ((((seedU & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * ax + ((((seedU & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * av,
                mv = ((((seedV & 0x1F0L) | 0x2FL) - 0x1FFp-1) * 0x1.4p-8) * av + ((((seedV & 0x1F000L) | 0x2F00L) - 0x1FF00p-1) * 0x1.1p-16) * au + ((((seedV & 0x1F00000L) | 0x2F0000L) - 0x1FF0000p-1) * 0x0.Bp-24) * aw + ((((seedV & 0x1F0000000L) | 0x2F000000L) - 0x1FF000000p-1) * 0x0.7p-32) * az + ((((seedV & 0x1F000000000L) | 0x2F00000000L) - 0x1FF00000000p-1) * 0x0.4p-40) * ay + ((((seedV & 0x1F00000000000L) | 0x2F0000000000L) - 0x1FF0000000000p-1) * 0x0.2p-48) * ax;

        final long
                xf = fastFloor(mx),
                yf = fastFloor(my),
                zf = fastFloor(mz),
                wf = fastFloor(mw),
                uf = fastFloor(mu),
                vf = fastFloor(mv);
        return
                //NumberTools.bounce(5.875 + 2.0625 * (
                NumberTools.sway( 2.1625 * (
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
