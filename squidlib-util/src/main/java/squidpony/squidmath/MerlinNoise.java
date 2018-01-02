package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Simple somewhat-continuous noise functions that use long coordinates instead of the traditional double (this approach
 * works better on a grid). It's called this because it should be possible to replace PerlinNoise with MerlinNoise in
 * some cases, and because working with noise functions makes me feel like a wizard.
 */
@Beta
public class MerlinNoise {

    public long seed;
    protected int bits = 56, resolution = 3;
    public MerlinNoise() {
        seed = 1L;
    }
    public MerlinNoise(long seed, int bits, int resolution)
    {
        this.seed = seed;
        this.bits = (-bits & 63);
        this.resolution = resolution & 63;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getBits() {
        return 64 - bits;
    }

    public void setBits(int bits) {
        this.bits = (-bits & 63);
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution & 63;
    }

    private static long lorp(long start, long end, long a, long resolution) {
        a = a * a * ((3L << resolution) - (a << 1));
        end = ((1L << resolution * 3) - a) * start + a * end >> resolution * 3;
        return end;
    }
    /**
     * 2D merlin noise.
     *
     * @param x x input
     * @param y y input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one random number is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise2D(long x, long y, long state, int resolution, int bits) {
        long xb = x >>> resolution, yb = y >>> resolution, xr = (x & ~(-1L << resolution)), yr = (y & ~(-1L << resolution)),
                x0y0 = Noise.PointHash.hashAll(xb, yb, state), x1y0 = Noise.PointHash.hashAll(xb + 1, yb, state),
                x0y1 = Noise.PointHash.hashAll(xb, yb + 1, state), x1y1 = Noise.PointHash.hashAll(xb + 1, yb + 1, state);
//                x0y0 = (x0y0b >> 2) + (ThrustAltRNG.determine(x0y0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y0b + 2) >> 2) + (ThrustAltRNG.determine(x0y0b + 3) >> 2),
//                x1y0 = (x1y0b >> 2) + (ThrustAltRNG.determine(x1y0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y0b + 2) >> 2) + (ThrustAltRNG.determine(x1y0b + 3) >> 2),
//                x0y1 = (x0y1b >> 2) + (ThrustAltRNG.determine(x0y1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y1b + 2) >> 2) + (ThrustAltRNG.determine(x0y1b + 3) >> 2),
//                x1y1 = (x1y1b >> 2) + (ThrustAltRNG.determine(x1y1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y1b + 2) >> 2) + (ThrustAltRNG.determine(x1y1b + 3) >> 2)
        long xly0 = lorp(x0y0, x1y0, xr, resolution), xly1 = lorp(x0y1, x1y1, xr, resolution),
        yl = lorp(xly0, xly1, yr, resolution);
        return yl >> (-bits & 63);
    }

    /**
     * 3D merlin noise.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one random number is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise3D(long x, long y, long z, long state, int resolution, int bits) {
        long xb = x >> resolution, yb = y >> resolution, zb = z >> resolution,
                xr = x & ~(-1L << resolution), yr = y & ~(-1L << resolution), zr = z & ~(-1L << resolution),
                x0y0z0b = Noise.PointHash.hashAll(xb, yb, zb, state), x1y0z0b = Noise.PointHash.hashAll(xb + 1, yb, zb, state),
                x0y1z0b = Noise.PointHash.hashAll(xb, yb + 1, zb, state), x1y1z0b = Noise.PointHash.hashAll(xb + 1, yb + 1, zb, state),
                x0y0z1b = Noise.PointHash.hashAll(xb, yb, zb + 1, state), x1y0z1b = Noise.PointHash.hashAll(xb + 1, yb, zb + 1, state),
                x0y1z1b = Noise.PointHash.hashAll(xb, yb + 1, zb + 1, state), x1y1z1b = Noise.PointHash.hashAll(xb + 1, yb + 1, zb + 1, state),
                x0y0z0 = (x0y0z0b >> 2) + (ThrustAltRNG.determine(x0y0z0b + 1) >> 2)
                        + (ThrustAltRNG.determine(x0y0z0b + 2) >> 2) + (ThrustAltRNG.determine(x0y0z0b + 3) >> 2),
                x1y0z0 = (x1y0z0b >> 2) + (ThrustAltRNG.determine(x1y0z0b + 1) >> 2)
                        + (ThrustAltRNG.determine(x1y0z0b + 2) >> 2) + (ThrustAltRNG.determine(x1y0z0b + 3) >> 2),
                x0y1z0 = (x0y1z0b >> 2) + (ThrustAltRNG.determine(x0y1z0b + 1) >> 2)
                        + (ThrustAltRNG.determine(x0y1z0b + 2) >> 2) + (ThrustAltRNG.determine(x0y1z0b + 3) >> 2),
                x1y1z0 = (x1y1z0b >> 2) + (ThrustAltRNG.determine(x1y1z0b + 1) >> 2)
                        + (ThrustAltRNG.determine(x1y1z0b + 2) >> 2) + (ThrustAltRNG.determine(x1y1z0b + 3) >> 2),
                x0y0z1 = (x0y0z1b >> 2) + (ThrustAltRNG.determine(x0y0z1b + 1) >> 2)
                        + (ThrustAltRNG.determine(x0y0z1b + 2) >> 2) + (ThrustAltRNG.determine(x0y0z1b + 3) >> 2),
                x1y0z1 = (x1y0z1b >> 2) + (ThrustAltRNG.determine(x1y0z1b + 1) >> 2)
                        + (ThrustAltRNG.determine(x1y0z1b + 2) >> 2) + (ThrustAltRNG.determine(x1y0z1b + 3) >> 2),
                x0y1z1 = (x0y1z1b >> 2) + (ThrustAltRNG.determine(x0y1z1b + 1) >> 2)
                        + (ThrustAltRNG.determine(x0y1z1b + 2) >> 2) + (ThrustAltRNG.determine(x0y1z1b + 3) >> 2),
                x1y1z1 = (x1y1z1b >> 2) + (ThrustAltRNG.determine(x1y1z1b + 1) >> 2)
                        + (ThrustAltRNG.determine(x1y1z1b + 2) >> 2) + (ThrustAltRNG.determine(x1y1z1b + 3) >> 2);
        return lorp(lorp(lorp(x0y0z0, x1y0z0, xr, resolution), lorp(x0y1z0, x1y1z0, xr, resolution), yr, resolution),
                lorp(lorp(x0y0z1, x1y0z1, xr, resolution), lorp(x0y1z1, x1y1z1, xr, resolution), yr, resolution), zr, resolution) >> (-bits & 63);
    }

    /**
     * Generates higher-quality continuous-style noise than the other methods, but requires pre-calculating a grid.
     * Does allow taking a seed because internally it uses a ThrustRNG to quickly generate initial white noise before
     * processing it into more continuous noise. This generates a lot of random numbers (at least 1 + 14 * height, or
     * more if width is greater than 64), so ThrustRNG's high speed and fairly good period are both assets here.
     * <br>
     * The 2D int array this produces has ints ranging from 1 to 255, with extreme values very unlikely. Because 0 is
     * impossible for this to generate, it should be fine to use values from this as denominators in division.
     *
     * @param width  the width of the 2D int array to generate
     * @param height the height of the 2D int array to generate
     * @param seed   the RNG seed to use when pseudo-randomly generating the initial white noise this then processes
     * @return a 2D int array where each int should be between 1 and 255, inclusive
     */
    public static int[][] preCalcNoise2D(int width, int height, long seed) {
        ThrustRNG random = new ThrustRNG(seed);
        int w = (width << 1) + 2, h = (height << 1) + 2;
        GreasedRegion[] regions = new GreasedRegion[]{
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3)
        };
        int[][] data = GreasedRegion.bitSum(regions);

        regions = new GreasedRegion[]{
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3)
        };
        int[][] data2 = GreasedRegion.bitSum(regions), data3 = new int[width][height];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                data[x][y] += 128 - data2[x][y];
            }
        }
        for (int x = 0, dx = 1; x < width; x++, dx += 2) {
            for (int y = 0, dy = 1; y < height; y++, dy += 2) {
                data3[x][y] = ((data[dx][dy] << 2) + data[dx - 1][dy] + data[dx + 1][dy] + data[dx][dy + 1] + data[dx][dy - 1]) >>> 3;
            }
        }
        return data3;
        /*
        int[][] data = GreasedRegion.bitSum(regions);
        return GreasedRegion.selectiveNegate(data, new GreasedRegion(random, width, height), 0xff);
        */
    }
}
