package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Simple somewhat-continuous noise functions that use int coordinates instead of the traditional double (this approach
 * works better on a grid). It's called this because it should be possible to replace PerlinNoise with MerlinNoise, and
 * because working with noise functions makes me feel like a wizard.
 */
@Beta
public class MerlinNoise {

    private MerlinNoise() {

    }

    /*
    public static int rawNoise2D(int a, int b) {
        //final int mx = x * 17 ^ ((x ^ 11) + (y ^ 13)), my = y * 29 ^ (7 + x + y),
        int out = -1;
        //final int alpha = (a + b) * 17 + (a * 19 + b * 23) * 29, beta = (a * b) * 11 + (a * 41 ^ b * 31) * 37;
        for (int x = (a ^ 0x7C15) * 0x7F4A - 4; x <= (a ^ 0x7C15) * 0x7F4A + 4; x++) {
            for (int y = (b ^ 0x79B9) * 0x9E37 - 4; y <= (b ^ 0x79B9) * 0x9E37 + 4; y++) {
                //final int
                //        gx = x ^ (x >>> 1), gy = y ^ (y >>> 1),
                //        z = gx + gy, w = gx ^ -gy,
                //        gz = z ^ (z >>> 1), gw = w ^ (w >>> 1),
                //        p = ~gx & gw + gz * gy, q = gw * gz ^ ~gy - gx,
                //        gp = p ^ (p >>> 1), gq = q ^ (q >>> 1);
                out += (x >>> 2 & 7) + (y >>> 2 & 7) - ((x + y) >>> 3 & 3);
            }
        }
        //mx = (x * 127 ^ y * 113) ^ (x * 131 + y * 119), my = (x * 119 ^ y * 127) ^ (x * 113 + y * 131),
        //final int mx = (x ^ y) * 0x7C15 + (y + x) * 0x9E37, my = (y ^ x) * 0xA47F ^ (x + y) * 0x79B9,
        //gx = mx ^ (x >> 1), gy = my ^ (y >> 1),
        //sx = (x + 16) >> 1, sy = (y + 16) >> 1,
        //mx = x * 127 + (x >> 1) * 353 ^ (31 + (x << 1) - y), my = y * 131 + (y >> 1) * 301 ^ (37 + (y << 1) - x),
        //out = ((sx * sy * mx * my) >>> 8 & 0x1ff); //((Integer.bitCount(gx) + Integer.bitCount(gy) & 63) << 3) ^
        out &= 0x1ff;
        return ((out & 0x100) != 0) ? ~out & 0xff : out & 0xff;
    }
    */

    /*
    public static int rawNoise2D(int x, int y) {
        //final int mx = x * 17 ^ ((x ^ 11) + (y ^ 13)), my = y * 29 ^ (7 + x + y),
        final int
                gx = x ^ (x >>> 1), gy = y ^ (y >>> 1),
                z = gx + gy, w = gx ^ -gy,
                gz = z ^ (z >>> 1), gw = w ^ (w >>> 1),
                p = ~gx & gw * gx + gz * gy, q = gz * gw ^ ~gy - gx,
                gp = p ^ (p >>> 1), gq = q ^ (q >>> 1),
                //gcz = Long.bitCount(gz * 0x9E3779B97F4A7C15L), gcw = Long.bitCount(gw * 0x9E3779B97F4A7C15L),
                //gcp = Long.bitCount(gp * 0x9E3779B97F4A7C15L), gcq = Long.bitCount(gq * 0x9E3779B97F4A7C15L),
                //mx = (x * 127 ^ y * 113) ^ (x * 131 + y * 119), my = (x * 119 ^ y * 127) ^ (x * 113 + y * 131),
                //final int mx = (x ^ y) * 0x7C15 + (y + x) * 0x9E37, my = (y ^ x) * 0xA47F ^ (x + y) * 0x79B9,
                //gx = mx ^ (x >> 1), gy = my ^ (y >> 1),
                //sx = (x + 16) >> 1, sy = (y + 16) >> 1,
                //mx = x * 127 + (x >> 1) * 353 ^ (31 + (x << 1) - y), my = y * 131 + (y >> 1) * 301 ^ (37 + (y << 1) - x),
                //out = ((sx * sy * mx * my) >>> 8 & 0x1ff); //((Integer.bitCount(gx) + Integer.bitCount(gy) & 63) << 3) ^
                out = ((gz * 19) ^ (gw * 11) ^ (gp * 13) ^ (gq * 17)) & 0x1ff;
        return ((out & 0x100) != 0) ? ~out & 0xff : out & 0xff;
    }*/

    public static int rawNoise2D(final int x, final int y) {
        return ((x * 0x9E3779B9 ^ 0xC6BC2796) * (y * 0x92B5CC83 + 0xD9B4E019) - (x ^ ~y) * 0x632BE59B * (~x + y) * 0x7F4A7C15) >>> 8 & 0xff;
    }

    public static int rawNoise3D(final int x, final int y, final int z) {
        return (((z * 0xD0E89D2D + 0x311E289F) ^ (x * 0x9E3779B9 ^ 0xC6BC2796) ^ (y * 0x92B5CC83 + 0xD9B4E019)) +
                //(z * 0xD0E89D2D ^ x * 0xC6BC2796 ^ y * 0x92B5CC83 ^
                (x ^ ~y + z) * 0x632BE59B + (z ^ ~x + y) * 0x7F4A7C15 + (y ^ ~z + x) * 0x9E3779B9) >>> 8 & 0xff; //0x311E289F
    }

    /**
     * 2D merlin noise.
     *
     * @param x x input
     * @param y y input
     * @return noise from 0 to 255, inclusive
     */
    public static int noise2D(int x, int y) {
        return (rawNoise2D(x - 1, y - 1) & 43) + (rawNoise2D(x - 1, y + 1) & 43) +
                (rawNoise2D(x + 1, y - 1) & 43) + (rawNoise2D(x + 1, y + 1) & 43) +
                (rawNoise2D(x - 1, y) & 53) + (rawNoise2D(x + 1, y) & 53) +
                (rawNoise2D(x, y - 1) & 53) + (rawNoise2D(x, y - 1) & 53) +
                (rawNoise2D(x, y) & 127) >>> 1;
    }

    /**
     * 3D merlin noise.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @return noise from 0 to 255, inclusive
     */
    public static int noise3D(int x, int y, int z) {
        /*return (rawNoise3D(x-1,y-1,z-1) & 12) + (rawNoise3D(x-1,y+1,z-1) & 12) +
                (rawNoise3D(x+1,y-1,z-1) & 12) + (rawNoise3D(x+1,y+1,z-1) & 12) +
                (rawNoise3D(x-1,y-1,z+1) & 12) + (rawNoise3D(x-1,y+1,z+1) & 12) +
                (rawNoise3D(x+1,y-1,z+1) & 12) + (rawNoise3D(x+1,y+1,z+1) & 12) +

                (rawNoise3D(x-1,y-1,z) & 25) + (rawNoise3D(x+1,y-1,z) & 25) +
                (rawNoise3D(x-1,y+1,z) & 25) + (rawNoise3D(x+1,y+1,z) & 25) +
                (rawNoise3D(x-1,y,z-1) & 25) + (rawNoise3D(x+1,y,z-1) & 25) +
                (rawNoise3D(x-1,y,z+1) & 25) + (rawNoise3D(x+1,y,z+1) & 25) +
                (rawNoise3D(x,y-1,z-1) & 25) + (rawNoise3D(x,y+1,z-1) & 25) +
                (rawNoise3D(x,y-1,z+1) & 25) + (rawNoise3D(x,y+1,z+1) & 25) +

                (rawNoise3D(x-1,y,z) & 62) + (rawNoise3D(x+1,y,z) & 62) +
                (rawNoise3D(x,y-1,z) & 62) + (rawNoise3D(x,y+1,z) & 62) +
                (rawNoise3D(x,y,z-1) & 62) + (rawNoise3D(x,y,z+1) & 62) +
                */
        return  (rawNoise3D(x - 1, y, z) & 55) + (rawNoise3D(x + 1, y, z) & 55) +
                (rawNoise3D(x, y - 1, z) & 55) + (rawNoise3D(x, y + 1, z) & 55) +
                (rawNoise3D(x, y, z - 1) & 55) + (rawNoise3D(x, y, z + 1) & 55) +

                (rawNoise3D(x, y, z) & 181) >>> 1;
    }
    public static int noiseBalanced3D(int x, int y, int z) {
        return  rawNoise3D(x - 1, y, z) + rawNoise3D(x + 1, y, z) +
                rawNoise3D(x, y - 1, z) + rawNoise3D(x, y + 1, z) +
                rawNoise3D(x, y, z - 1) + rawNoise3D(x, y, z + 1) +
                (rawNoise3D(x, y, z) << 1) >>> 3;
    }
                        /*(
                                cellA * (0.5 + absA - absB) +// * (1 - aCoreBias) +
                                cellB * (0.5 + absB - absA)// * (1 - bCoreBias) +
                             //   cellAr * aCoreBias + cellBr * bCoreBias
                        ) * (mx)
                        */
                                        /*(cellAB * cornerPref * 2.0 +
                        cellA * (1.0 + absA - absB - cornerPref) * (1 - aCoreBias) +
                        cellB * (1.0 + absB - absA - cornerPref) * (1 - bCoreBias) +
                        cellAr * (1.0 - cornerPref) * aCoreBias + cellBr * bCoreBias * (1.0 - cornerPref)
                ) * mx * 0.5
                */
/*
                (cellAB * cornerPref * 2.0 +
                                cellA * (1.0 + absA - absB - cornerPref) * (1 - aCoreBias) +
                                cellB * (1.0 + absB - absA - cornerPref) * (1 - bCoreBias) +
                                cellAr * (1.0 - cornerPref) * aCoreBias + cellBr * bCoreBias * (1.0 - cornerPref)
                        ) * mx * 0.5
                */
                /*
                                cellA * (0.5 + absA - absB - cornerPref) +
                                cellB * (0.5 + absB - absA - cornerPref)) * mx

                 */

    /**
     * 2D merlin noise with a zoom factor.
     *
     * @param x    x input
     * @param y    y input
     * @param zoom if greater than 1.0, will make the details of the noise larger; if less, will make them smaller
     * @return noise from 0 to 255, inclusive
     */
    /*
    public static int noise2D(int x, int y, double zoom) {
        final double alef = x / (zoom * 8.0), bet = y / (zoom * 8.0);
        final int alpha = (int) ((x >> 3) / zoom), beta = (int) ((y >> 3) / zoom);
        final double aBias = (alef - alpha), bBias = (bet - beta),
                absA = Math.abs(aBias - 0.5), absB = Math.abs(bBias - 0.5),
                //cornerPref = 0.5 * (absA + absB),
                cornerPref = Math.max(absA + absB - 0.5, 0) * 0.5,
                mx = (Math.max(absA, absB))// * 0.5 + (absA + absB) * 0.5),
                //aCoreBias = Math.max(0.25 - absA, 0), bCoreBias = Math.max(0.25 - absB, 0)
                ;
        final int aCond = (aBias < 0.5 ? -1 : 1), bCond = (bBias < 0.5 ? -1 : 1),
                centerCell = rawNoise2D(alpha, beta),
                cellA = rawNoise2D(alpha + aCond, beta),
                cellB = rawNoise2D(alpha, beta + bCond),
                cellAr = rawNoise2D(alpha - aCond, beta),
                cellBr = rawNoise2D(alpha, beta - bCond),
                //cellAB   = (rawNoise2D(alpha + aCond, beta + bCond) + (cellA + cellB)) / 3;
                //cellArB  = (rawNoise2D(alpha - aCond, beta + bCond) * 6 + (cellAr + cellB) * 5) >> 4,
                //cellABr  = (rawNoise2D(alpha + aCond, beta - bCond) * 6 + (cellA + cellBr) * 5) >> 4,
                //cellArBr = (rawNoise2D(alpha - aCond, beta - bCond) * 6 + (cellAr + cellBr) * 5) >> 4;
        return (int)(((centerCell)// * (1 - aCoreBias - bCoreBias) + cellAr * aCoreBias + cellBr * bCoreBias)
                * (1 - mx) +
                ((absA > absB) ? cellA * (1.4 + (absA - absB)) + cellB * (0.6 - (absA - absB)) :
                        cellB * (1.4 + (absB - absA)) + cellA * (0.6 - (absB - absA))) * mx * 0.5)
        );
    }*/
    public static int noise2D(int x, int y, int zoom) {
        if (zoom < 1)
            return 0;
        int v = 0, a = x << 1, b = y << 1;
        for (int s = zoom; s > 0; s--) {
            v += noise2D((a += 379) / s, (b += 379) / s) + noise2D((a) / s, (b + 1) / s) + noise2D((a + 1) / s, (b) / s);
        }
        //return v / zoom;
        /*
        double adj = Math.sin((v / (zoom * 1530.0)) * Math.PI);
        return (int)(adj * adj * 255.5);
        */
        double adj = Math.sin((v / (zoom * 1530.0)) * Math.PI);
        adj *= adj;
        return (int) (Math.pow(adj, 2.0 - 2.0 * adj) * 255.5);
    }

    public static int noise3D(int x, int y, int z, int zoom) {
        if (zoom < 1)
            return 0;
        int v = 0, a = x, b = y, c = z, t = 0;
        for (int s = zoom, u = zoom; s > 0; s--, u++) {
            v += (noiseBalanced3D((a += 379) / s, (b += 379) / s, (c += 379) / s) + noiseBalanced3D((a+1) / s, (b+1) / s, (c+1) / s)) * u;
            //

            //v += noise3D((a+(v&1)) / s, (b+(v>>1&1)) / s, (c+(v>>2&1)) / s) +
            //        noise3D((a-(v>>3&1)) / s, (b-(v>>4&1)) / s, (c-(v>>5&1)) / s);
            //v += t + noise3D((a + (t & 3)) / s, (b + (t >> 2 & 3)) / s, (c + (t >> 4 & 3)) / s);
                    //+ noise3D((a - (t >> 3 & 1)) / s, (b - (t >> 4 & 1)) / s, (c - (t >> 5 & 1)) / s);
                    /*
                    + noise3D((a) / s, (b+1) / s, (c) / s)
                    + noise3D((a+1) / s, (b) / s, (c) / s)
                    + noise3D((a) / s, (b) / s, (c+1) / s);*/
            //v += noise3D((a+(v&3)) / s, (b+(v>>2&3)) / s, (c+(v>>4&3)) / s);
            t += u;
        }
        //return v / zoom;
        /*
        double adj = Math.sin((v / (zoom * 1530.0)) * Math.PI);
        return (int)(adj * adj * 255.5);
        */

        double adj = Math.sin((v / (1020.0 * t)) * Math.PI);
        //return (int)(adj * adj * 255.5);
        adj *= adj;
        return (int) (Math.pow(adj, 2.0 - 2.0 * adj) * 255.5);
    }

    /*
    public static int noise2D(int x, int y, double zoom) {
        final double alef = x / (zoom * 8.0), bet = y / (zoom * 8.0);
        final int alpha = (int) ((x >> 3) / zoom), beta = (int) ((y >> 3) / zoom);
        final double aBias = (alef - alpha), bBias = (bet - beta),
                absA = Math.abs(aBias - 0.5), absB = Math.abs(bBias - 0.5),
                cornerPref = Math.max(absA + absB - 0.5, 0),
                mx = Math.max(absA, absB) * 3.6 + (absA + absB) * 4.2;
        final int centerCell = rawNoise2D(alpha, beta),
                aCell = rawNoise2D(alpha + (aBias < 0.5 ? -1 : 1), beta),
                bCell = rawNoise2D(alpha, beta + (bBias < 0.5 ? -1 : 1)),
                cornerCell = (rawNoise2D(alpha + (aBias < 0.5 ? -1 : 1), beta + (bBias < 0.5 ? -1 : 1)) * 6 + (aCell + bCell) * 5) >> 4;
        return (int) ( //(centerCell * (1 - absA - absB) + aCell * absA + bCell * absB)
                centerCell * (8 - mx) +
                        (cornerCell * cornerPref * 2.0 +
                                aCell * (0.5 + absA - absB - cornerPref) +
                                bCell * (0.5 + absB - absA - cornerPref)) * mx
        ) >>> 3;
    }
    */

    /**
     * Generates higher-quality continuous-style noise than the other methods, but requires pre-calculating a grid.
     * Does allow taking a seed because internally it uses a ThunderRNG to quickly generate initial white noise before
     * processing it into more continuous noise. This generates a lot of random numbers (at least 1 + 14 * height, or
     * more if width is greater than 64), so ThunderRNG's high speed and presumed higher-than-2-to-the-64 period are
     * both assets here.
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
        ThunderRNG random = new ThunderRNG(seed);
        int w = (width << 1) + 2, h = (height << 1) + 2;
        GreasedRegion[] regions = new GreasedRegion[]{
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3), new GreasedRegion(random, w, h).retract().expand(3),
                new GreasedRegion(random, w, h).retract().expand(3)
        };
        random.reseed(random.nextLong());
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


    /*
    public static int noise2D(int x, int y, double zoom) {
        final double alef = x / zoom, bet = y / zoom;
        final int alpha = (int) (alef), beta = (int) (bet),
                north = rawNoise2D(alpha, beta - 1), south = rawNoise2D(alpha, beta + 1),
                east = rawNoise2D(alpha + 1, beta), west = rawNoise2D(alpha - 1, beta),
                center = rawNoise2D(alpha, beta);
        final double aBias = (alef - alpha), bBias = (bet - beta);
        return (((aBias - 0.75) < 0 ? west : center) * 3 + ((aBias + 0.75) >= 1 ? east : center) * 3 +
                ((aBias - 0.25) < 0 ? west : center) * 2 + ((aBias + 0.25) >= 1 ? east : center) * 2 +
                ((bBias - 0.75) < 0 ? north : center) * 3 + ((bBias + 0.75) >= 1 ? south : center) * 3 +
                ((bBias - 0.25) < 0 ? north : center) * 2 + ((bBias + 0.25) >= 1 ? south : center) * 2
        ) / 20;
    }*/
                    /*+
                ((aBias - 0.5) < 0 ? west : center) * 17 + ((aBias + 0.5) >= 1 ? east : center) * 17 +
                ((aBias - 0.3) < 0 ? west : center) * 15 + ((aBias + 0.3) >= 1 ? east : center) * 15 +
                ((bBias - 0.5) < 0 ? north : center) * 17 + ((bBias + 0.5) >= 1 ? south : center) * 17 +
                ((bBias - 0.3) < 0 ? north : center) * 15 + ((bBias + 0.3) >= 1 ? south : center) * 15*/

}
