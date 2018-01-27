package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Really strange noise functions that typically produce curving black and white shapes when rendered.
 * This technique uses no floating-point math, surprisingly, which helps its performance a little.
 * The shapes this produces <a href="https://i.imgur.com/mp23254.png">look like this in 2D</a> and
 * <a href="https://i.imgur.com/qPLZw0k.gifv">look like this in 3D</a>. MerlinNoise implements 2D and 3D noise
 * interfaces, allowing it to be used with the various support code in Noise like {@link Noise.Layered2D}.
 * <br>
 * This is called Merlin noise because it has a roughly-similar implementation to "classic" Perlin Noise (with hashes
 * per grid point used to blend values), and because working with noise functions makes me feel like a wizard.
 * This was a completely unrelated noise algorithm that also avoided floating-point math, but was really pretty awful.
 */
@Beta
public class MerlinNoise implements Noise.Noise2D, Noise.Noise3D {

    public static final MerlinNoise instance = new MerlinNoise();
    public long seed;
    protected int bits = 8, resolution = 4;
    private long resSize = 1L << resolution;
    /**
     * Constructor for a default MerlinNoise instance with 8-bit output and resolution 3 (yielding 8x8-cell zones that
     * share their corners). The seed can be set at any point, but it will start at 1.
     */
    public MerlinNoise() {
        seed = 1L;
    }

    /**
     * Constructor for a MerlinNoise instance that allows specification of all parameters.
     * @param seed the seed to use to alter the generated noise in {@link #noise2D(long, long)} and {@link #noise3D(long, long, long)}
     * @param bits the number of bits to output; typically 8 to produce byte-sized values
     * @param resolution an exponent that determines the size of a "zone" of cells that blend between the values at the zone's corners; commonly 1-6
     */
    public MerlinNoise(long seed, int bits, int resolution)
    {
        this.seed = seed;
        this.bits = bits;
        this.resolution = resolution & 31;
        resSize = 1L << this.resolution;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getBits() {
        return bits;
    }

    /**
     * Sets the number of bits this will output; 8 is common to produce byte-sized values between 0 and 255.
     * This value can be between 1 and 64. If bits is 8, then this should produce values of 255 or 0, plus or minus 1.
     * If bits is some other value, then it may produce more than two values, or only produce one.
     * @param bits the number of bits of output each call should generate.
     */
    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution, which is an exponent that determines the width/height of each zone that shares the same four
     * corners (where only the corners have their own hashed values). If resolution is 1, the size of a zone is 2x2, if
     * it is 2, then the size of a zone is 4x4, if it is 3, then 8x8, and so on by powers of 2. The resolution can be as
     * low as 0 (which won't blend corners' hashes at all) or as high as 31, but cannot easily be increased above that
     * (10 is a really large size for a cell at 1024x1024, and 31 is over 2 billion by 2 billion). This doesn't slow
     * down significantly (or at all) if resolution is particularly high or low, but this is often between 1 and 6.
     * @param resolution an int between 0 and 31
     */
    public void setResolution(int resolution) {
        this.resolution = resolution & 31;
        resSize = 1L << this.resolution;
    }

//    private static long clorp(long start, long end, long a, long resolution) {
//        a = a * a * ((3L << resolution) - (a << 1));
//        end = ((1L << resolution * 3) - a) * start + a * end >> resolution * 3;
//        return end;
//    }

    /**
     * 2D Merlin noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     */
    public long noise2D(long x, long y)
    {
        return noise2D(x, y, seed, resolution, bits);
    }

    /**
     * 2D Merlin noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param seed the seed to use to alter the generated noise
     */
    public long noise2D(long x, long y, long seed)
    {
        return noise2D(x, y, seed, resolution, bits);
    }

    /**
     * 3D Merlin noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     */
    public long noise3D(long x, long y, long z)
    {
        return noise3D(x, y, z, seed, resolution, bits);
    }

    /**
     * 3D Merlin noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param seed the seed to use to alter the generated noise
     */
    public long noise3D(long x, long y, long z, long seed)
    {
        return noise3D(x, y, z, seed, resolution, bits);
    }
    private static long lorp(long start, long end, long a, long resolution) {
        end = ((1L << resolution) - a) * start + a * end >>> resolution;
        return end;
    }
    /**
     * 2D Merlin noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one hashed value is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise2D(long x, long y, long state, int resolution, int bits) {
        long xb = (x >> resolution) + state, yb = (y >> resolution) - state,
                xr = (x & ~(-1L << resolution)), yr = (y & ~(-1L << resolution)),
                x0 = ThrustAltRNG.determine(xb), x1 = ThrustAltRNG.determine(xb + 1),
                y0 = ThrustAltRNG.determine(yb), y1 = ThrustAltRNG.determine(yb + 1),
                x0y0 = (x0 * y0 ^ x0 - y0) >>> resolution, x1y0 = (x1 * y0 ^ x1 - y0) >>> resolution,
                x0y1 = (x0 * y1 ^ x0 - y1) >>> resolution, x1y1 = (x1 * y1 ^ x1 - y1) >>> resolution;

//                x0y0 = Noise.PointHash.hashAll(xb, yb, state) >> resolution, x1y0 = Noise.PointHash.hashAll(xb + 1, yb, state) >> resolution,
//                x0y1 = Noise.PointHash.hashAll(xb, yb + 1, state) >> resolution, x1y1 = Noise.PointHash.hashAll(xb + 1, yb + 1, state) >> resolution;

//                x0y0 = (x0y0b >> 2) + (ThrustAltRNG.determine(x0y0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y0b + 2) >> 2) + (ThrustAltRNG.determine(x0y0b + 3) >> 2),
//                x1y0 = (x1y0b >> 2) + (ThrustAltRNG.determine(x1y0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y0b + 2) >> 2) + (ThrustAltRNG.determine(x1y0b + 3) >> 2),
//                x0y1 = (x0y1b >> 2) + (ThrustAltRNG.determine(x0y1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y1b + 2) >> 2) + (ThrustAltRNG.determine(x0y1b + 3) >> 2),
//                x1y1 = (x1y1b >> 2) + (ThrustAltRNG.determine(x1y1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y1b + 2) >> 2) + (ThrustAltRNG.determine(x1y1b + 3) >> 2)
        return lorp(lorp(x0y0, x1y0, xr, resolution), lorp(x0y1, x1y1, xr, resolution), yr, resolution)
                >>> -resolution - bits; // >> (- bits - resolution & 63)
    }

    /**
     * 3D merlin noise.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one hashed value is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise3D(long x, long y, long z, long state, int resolution, int bits) {
        long xb = (x >> resolution) + state, yb = (y >> resolution) - state, zb = (z >> resolution) + (0x9E3779B97F4A7C15L ^ state),
                xr = x & ~(-1L << resolution), yr = y & ~(-1L << resolution), zr = z & ~(-1L << resolution),
                x0 = ThrustAltRNG.determine(xb), x1 = ThrustAltRNG.determine(xb + 1),
                y0 = ThrustAltRNG.determine(yb), y1 = ThrustAltRNG.determine(yb + 1),
                z0 = ThrustAltRNG.determine(zb), z1 = ThrustAltRNG.determine(zb + 1),
                x0y0z0 = (x0 * y0 * z0 ^ x0 - y0 + (z0 - x0 << 32 | y0 - z0 >>> 32)) >>> resolution, x1y0z0 = (x1 * y0 * z0 ^ x1 - y0 + (z0 - x1 << 32 | y0 - z0 >>> 32)) >>> resolution,
                x0y1z0 = (x0 * y1 * z0 ^ x0 - y1 + (z0 - x0 << 32 | y1 - z0 >>> 32)) >>> resolution, x1y1z0 = (x1 * y1 * z0 ^ x1 - y1 + (z0 - x1 << 32 | y1 - z0 >>> 32)) >>> resolution,
                x0y0z1 = (x0 * y0 * z1 ^ x0 - y0 + (z1 - x0 << 32 | y0 - z1 >>> 32)) >>> resolution, x1y0z1 = (x1 * y0 * z1 ^ x1 - y0 + (z1 - x1 << 32 | y0 - z1 >>> 32)) >>> resolution,
                x0y1z1 = (x0 * y1 * z1 ^ x0 - y1 + (z1 - x0 << 32 | y1 - z1 >>> 32)) >>> resolution, x1y1z1 = (x1 * y1 * z1 ^ x1 - y1 + (z1 - x1 << 32 | y1 - z1 >>> 32)) >>> resolution;

//                x0y0z0 = Noise.PointHash.hashAll(xb, yb, zb, state) >> resolution, x1y0z0 = Noise.PointHash.hashAll(xb + 1, yb, zb, state) >> resolution,
//                x0y1z0 = Noise.PointHash.hashAll(xb, yb + 1, zb, state) >> resolution, x1y1z0 = Noise.PointHash.hashAll(xb + 1, yb + 1, zb, state) >> resolution,
//                x0y0z1 = Noise.PointHash.hashAll(xb, yb, zb + 1, state) >> resolution, x1y0z1 = Noise.PointHash.hashAll(xb + 1, yb, zb + 1, state) >> resolution,
//                x0y1z1 = Noise.PointHash.hashAll(xb, yb + 1, zb + 1, state) >> resolution, x1y1z1 = Noise.PointHash.hashAll(xb + 1, yb + 1, zb + 1, state) >> resolution;

//                x0y0z0 = (x0y0z0b >> 2) + (ThrustAltRNG.determine(x0y0z0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y0z0b + 2) >> 2) + (ThrustAltRNG.determine(x0y0z0b + 3) >> 2),
//                x1y0z0 = (x1y0z0b >> 2) + (ThrustAltRNG.determine(x1y0z0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y0z0b + 2) >> 2) + (ThrustAltRNG.determine(x1y0z0b + 3) >> 2),
//                x0y1z0 = (x0y1z0b >> 2) + (ThrustAltRNG.determine(x0y1z0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y1z0b + 2) >> 2) + (ThrustAltRNG.determine(x0y1z0b + 3) >> 2),
//                x1y1z0 = (x1y1z0b >> 2) + (ThrustAltRNG.determine(x1y1z0b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y1z0b + 2) >> 2) + (ThrustAltRNG.determine(x1y1z0b + 3) >> 2),
//                x0y0z1 = (x0y0z1b >> 2) + (ThrustAltRNG.determine(x0y0z1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y0z1b + 2) >> 2) + (ThrustAltRNG.determine(x0y0z1b + 3) >> 2),
//                x1y0z1 = (x1y0z1b >> 2) + (ThrustAltRNG.determine(x1y0z1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y0z1b + 2) >> 2) + (ThrustAltRNG.determine(x1y0z1b + 3) >> 2),
//                x0y1z1 = (x0y1z1b >> 2) + (ThrustAltRNG.determine(x0y1z1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x0y1z1b + 2) >> 2) + (ThrustAltRNG.determine(x0y1z1b + 3) >> 2),
//                x1y1z1 = (x1y1z1b >> 2) + (ThrustAltRNG.determine(x1y1z1b + 1) >> 2)
//                        + (ThrustAltRNG.determine(x1y1z1b + 2) >> 2) + (ThrustAltRNG.determine(x1y1z1b + 3) >> 2);
//        long xm = lorp(x0y0z0, x1y0z0, xr, resolution),
//                xn = lorp(x0y1z0, x1y1z0, xr, resolution),
//                xo = lorp(x0y0z1, x1y0z1, xr, resolution),
//                xp = lorp(x0y1z1, x1y1z1, xr, resolution),
//                ym = lorp(xm, xn, yr, resolution),
//                yn = lorp(xo, xp, yr, resolution),
//                zm = lorp(ym, yn, zr, resolution);
//         zm >>>= -resolution-bits;
//         return zm;
        return lorp(lorp(lorp(x0y0z0, x1y0z0, xr, resolution), lorp(x0y1z0, x1y1z0, xr, resolution), yr, resolution),
                lorp(lorp(x0y0z1, x1y0z1, xr, resolution), lorp(x0y1z1, x1y1z1, xr, resolution), yr, resolution), zr, resolution)
                >>> -resolution - bits;
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

//    public static void main(String[] args)
//    {
//        long state = 9999L, bits = 32;
//        for (int resolution = 0; resolution < 4; resolution++) {
//            for (int x = 0; x < 10; x++) {
//                for (int y = 0; y < 10; y++) {
//                    long xb = x >>> resolution, yb = y >>> resolution, xr = (x & ~(-1L << resolution)), yr = (y & ~(-1L << resolution)),
//                            x0y0 = Noise.PointHash.hashAll(xb, yb, state) >> resolution, x1y0 = Noise.PointHash.hashAll(xb + 1, yb, state) >> resolution,
//                            x0y1 = Noise.PointHash.hashAll(xb, yb + 1, state) >> resolution, x1y1 = Noise.PointHash.hashAll(xb + 1, yb + 1, state) >> resolution;
//                    long xly0 = lorp(x0y0, x1y0, xr, resolution), xly1 = lorp(x0y1, x1y1, xr, resolution),
//                            yl = lorp(xly0, xly1, yr, resolution);
//                    System.out.printf("x: %d, y: %d, r: %d = %08X\n", x, y, resolution, yl);// >> (- bits - resolution & 63));
//                }
//            }
//        }
//    }

    @Override
    public double getNoise(double x, double y) {
        return 1 - (noise2D(Noise.longFloor(x * resSize), Noise.longFloor(y * resSize), seed, resolution << 1, 1) << 1);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return 1 - (noise2D(Noise.longFloor(x * resSize), Noise.longFloor(y * resSize), seed, resolution << 1, 1) << 1);
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return 1 - (noise3D(Noise.longFloor(x * resSize), Noise.longFloor(y * resSize), Noise.longFloor(z * resSize), seed, resolution << 1, 1) << 1);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return 1 - (noise3D(Noise.longFloor(x * resSize), Noise.longFloor(y * resSize), Noise.longFloor(z * resSize), seed, resolution << 1, 1) << 1);
    }
}
