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
        return (((z * 0xD0E89D2D + 0x311E289F) ^ x * 0x9E3779B9 ^ 0xC6BC2796 ^ (y * 0x92B5CC83 + 0xD9B4E019)) +
                //(z * 0xD0E89D2D ^ x * 0xC6BC2796 ^ y * 0x92B5CC83 ^
                (x ^ ~y + z) * 0x632BE59B + (z ^ ~x + y) * 0x7F4A7C15 + (y ^ ~z + x) * 0x9E3779B9) >>> 8 & 0xff; //0x311E289F
    }
    public static int rawNoise2D_alt(final int x, final int y) {

        int s,
                result = (s = 0x632BE5AB ^ x * 0x85157AF5) + 0xC5C5581A // equal to 0x62E2AC0D + 0x62E2AC0D
                        + (s ^= y * 0xA1687A2F);
        return (result ^ ((s ^ result) >>> 8) * 0x9E3779B9) >>> 24;
    }
    public static int rawNoise3D_alt(final int x, final int y, final int z) {

        int s,
                result = (s = 0x632BE5AB ^ x * 0x85157AF5) + 0x28A80427 // equal to 0x62E2AC0D + 0x62E2AC0D + 0x62E2AC0D
                        + (s ^= y * 0x92B5CC85)
                        + (s ^= z * 0xA1687A2F); //0x7F4A7C1F
        return (result ^ ((s ^ result) >>> 8) * 0x9E3779B9) & 255; // >>> 24
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
    }/**
     * 2D merlin noise.
     *
     * @param x x input
     * @param y y input
     * @return noise from 0 to 255, inclusive
     */
    public static int noise2D_alt(int x, int y) {
        return

                (rawNoise2D_alt(x - 1, y + 1) + rawNoise2D_alt(x + 1, y + 1) +
                rawNoise2D_alt(x + 1, y - 1) + rawNoise2D_alt(x - 1, y - 1) +
                rawNoise2D_alt(x - 1, y) + rawNoise2D_alt(x + 1, y) +
                rawNoise2D_alt(x, y - 1) + rawNoise2D_alt(x, y - 1)) * 3
                + (rawNoise2D_alt(x, y) << 3) >>> 5;
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
    public static int noiseBalanced3D_alt(int x, int y, int z) {
        return  rawNoise3D_alt(x - 1, y, z) + rawNoise3D_alt(x + 1, y, z) +
                rawNoise3D_alt(x, y - 1, z) + rawNoise3D_alt(x, y + 1, z) +
                rawNoise3D_alt(x, y, z - 1) + rawNoise3D_alt(x, y, z + 1) +
                /*
                (rawNoise3D_alt(x - 1, y - 1, z) + rawNoise3D_alt(x + 1, y - 1, z) +
                rawNoise3D_alt(x, y - 1, z - 1) + rawNoise3D_alt(x, y + 1, z - 1) +
                rawNoise3D_alt(x - 1, y, z - 1) + rawNoise3D_alt(x - 1, y, z + 1) +
                rawNoise3D_alt(x - 1, y + 1, z) + rawNoise3D_alt(x + 1, y + 1, z) +
                rawNoise3D_alt(x, y - 1, z + 1) + rawNoise3D_alt(x, y + 1, z + 1) +
                rawNoise3D_alt(x + 1, y, z - 1) + rawNoise3D_alt(x + 1, y, z + 1) << 4) +
                */
                (rawNoise3D_alt(x, y, z) << 1);
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
            v += noise2D((a += 379) / s, (b += 379) / s) + noise2D(a / s, (b + 1) / s) + noise2D((a + 1) / s, b / s);
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
    public static int noise2D_alt(final int x, final int y, final int zoom) {
        if (zoom < 1)
            return 0;
        int v = 0, a = x, b = y, t = 0, m;
        for (int s = zoom, u = 1; s > 0; s--, u++) {
            v += ((m = noise2D_alt((a += 76379) >>> s, (b += 76379) >>> s))
                    + noise2D_alt((a+(m&2)-1) >>> s, (b+((m&1)<<1)-1) >>> s)
            ) << u;
            t += 1 << u;
        }
        return lookup[(v / (t << 1))];
    }
    public static int noise2D_emphasized(final int x, final int y, final int zoom) {
        if (zoom < 1)
            return 0;
        int v = 0, a = x, b = y, t = 0, m;
        for (int s = zoom, u = 1; s > 0; s--, u++) {
            v += ((m = noise2D_alt((a += 76379) >>> s, (b += 76379) >>> s))
                    + noise2D_alt((a+(m&2)-1) >>> s, (b+((m&1)<<1)-1) >>> s)
            ) << u;
            t += 1 << u;
        }
        return lookup_extreme[(v / (t << 1))];
    }
    public static int noise3D(int x, int y, int z, int zoom) {
        if (zoom < 1)
            return 0;
        int v = 0, a = x, b = y, c = z, t = 0;
        for (int s = zoom, u = zoom; s > 0; s--, u++) {
            v += (noiseBalanced3D((a += 379) / s, (b += 379) / s, (c += 379) / s) + noiseBalanced3D((a+1) / s, (b+1) / s, (c+1) / s)) * u;
            t += u;
        }
        //v += noise3D((a+(v&1)) / s, (b+(v>>1&1)) / s, (c+(v>>2&1)) / s) +
        //        noise3D((a-(v>>3&1)) / s, (b-(v>>4&1)) / s, (c-(v>>5&1)) / s);
        //v += t + noise3D((a + (t & 3)) / s, (b + (t >> 2 & 3)) / s, (c + (t >> 4 & 3)) / s);
        //+ noise3D((a - (t >> 3 & 1)) / s, (b - (t >> 4 & 1)) / s, (c - (t >> 5 & 1)) / s);
                    /*
                    + noise3D((a) / s, (b+1) / s, (c) / s)
                    + noise3D((a+1) / s, (b) / s, (c) / s)
                    + noise3D((a) / s, (b) / s, (c+1) / s);*/
        //v += noise3D((a+(v&3)) / s, (b+(v>>2&3)) / s, (c+(v>>4&3)) / s);

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

    private static final int[] lookup_extreme = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1,
            1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 9, 9, 10, 11,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 25, 26, 27, 29,
            30, 31, 33, 35, 36, 38, 40, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59,
            61, 63, 65, 68, 70, 72, 75, 77, 79, 82, 84, 87, 89, 92, 94, 97, 100,
            102, 105, 108, 110, 113, 115, 118, 121, 123, 126, 129, 132, 134, 137,
            139, 142, 145, 147, 150, 153, 155, 158, 160, 163, 165, 168, 170, 172,
            175, 177, 179, 182, 184, 186, 188, 191, 193, 195, 197, 199, 201, 203,
            205, 206, 208, 210, 212, 213, 215, 217, 218, 220, 221, 223, 224, 225,
            227, 228, 229, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241,
            241, 242, 243, 244, 244, 245, 246, 246, 247, 247, 248, 248, 249, 249,
            250, 250, 251, 251, 251, 252, 252, 252, 253, 253, 253, 253, 253, 254,
            254, 254, 254, 254, 254, 255, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 256, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 254, 254, 254, 254, 254, 254, 253, 253, 253, 253,
            253, 252, 252, 252, 251, 251, 251, 250, 250, 249, 249, 248, 248, 247,
            247, 246, 246, 245, 244, 244, 243, 242, 241, 241, 240, 239, 238, 237,
            236, 235, 234, 233, 232, 231, 229, 228, 227, 225, 224, 223, 221, 220,
            218, 217, 215, 213, 212, 210, 208, 206, 205, 203, 201, 199, 197, 195,
            193, 191, 188, 186, 184, 182, 179, 177, 175, 172, 170, 168, 165, 163,
            160, 158, 155, 153, 150, 147, 145, 142, 139, 137, 134, 132, 129, 126,
            123, 121, 118, 115, 113, 110, 108, 105, 102, 100, 97, 94, 92, 89, 87,
            84, 82, 79, 77, 75, 72, 70, 68, 65, 63, 61, 59, 57, 55, 53, 51, 49,
            47, 45, 43, 41, 40, 38, 36, 35, 33, 31, 30, 29, 27, 26, 25, 23, 22,
            21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 11, 10, 9, 9, 8, 7, 7, 6,
            6, 5, 5, 5, 4, 4, 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            },
            lookup = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3,
            4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 11, 11, 12, 13, 13, 14, 15,
            15, 16, 17, 18, 19, 20, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 47, 48, 49,
            50, 52, 53, 54, 55, 57, 58, 59, 61, 62, 63, 65, 66, 68, 69, 70, 72,
            73, 75, 76, 78, 79, 81, 82, 83, 85, 86, 88, 89, 91, 92, 94, 96, 97,
            99, 100, 102, 103, 105, 106, 108, 109, 111, 113, 114, 116, 117, 119,
            120, 122, 124, 125, 127, 128, 130, 131, 133, 135, 136, 138, 139, 141,
            142, 144, 146, 147, 149, 150, 152, 153, 155, 156, 158, 159, 161, 163,
            164, 166, 167, 169, 170, 172, 173, 174, 176, 177, 179, 180, 182, 183,
            185, 186, 187, 189, 190, 191, 193, 194, 196, 197, 198, 200, 201, 202,
            203, 205, 206, 207, 208, 210, 211, 212, 213, 214, 215, 217, 218, 219,
            220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233,
            234, 235, 235, 236, 237, 238, 239, 240, 240, 241, 242, 242, 243, 244,
            244, 245, 246, 246, 247, 247, 248, 248, 249, 249, 250, 250, 251, 251,
            252, 252, 252, 253, 253, 253, 254, 254, 254, 254, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 254, 254, 254, 254, 253, 253, 253, 252, 252, 252, 251,
            251, 250, 250, 249, 249, 248, 248, 247, 247, 246, 246, 245, 244, 244,
            243, 242, 242, 241, 240, 240, 239, 238, 237, 236, 235, 235, 234, 233,
            232, 231, 230, 229, 228, 227, 226, 225, 224, 223, 222, 221, 220, 219,
            218, 217, 215, 214, 213, 212, 211, 210, 208, 207, 206, 205, 203, 202,
            201, 200, 198, 197, 196, 194, 193, 191, 190, 189, 187, 186, 185, 183,
            182, 180, 179, 177, 176, 174, 173, 172, 170, 169, 167, 166, 164, 163,
            161, 159, 158, 156, 155, 153, 152, 150, 149, 147, 146, 144, 142, 141,
            139, 138, 136, 135, 133, 131, 130, 128, 127, 125, 124, 122, 120, 119,
            117, 116, 114, 113, 111, 109, 108, 106, 105, 103, 102, 100, 99, 97,
            96, 94, 92, 91, 89, 88, 86, 85, 83, 82, 81, 79, 78, 76, 75, 73, 72,
            70, 69, 68, 66, 65, 63, 62, 61, 59, 58, 57, 55, 54, 53, 52, 50, 49,
            48, 47, 45, 44, 43, 42, 41, 40, 38, 37, 36, 35, 34, 33, 32, 31, 30,
            29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 20, 19, 18, 17, 16, 15, 15,
            14, 13, 13, 12, 11, 11, 10, 9, 9, 8, 8, 7, 7, 6, 6, 5, 5, 4, 4, 3, 3,
            3, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            };

    public static int noise3D_alt(final int x, final int y, final int z, final int zoom) {
        if (zoom < 1)
            return 0;
        int v = 0, a = x, b = y, c = z, t = 0, m;
        for (int s = zoom, u = 1; s > 0; s--, u++) {
            v += ((m = noiseBalanced3D_alt((a += 76379) >>> s, (b += 76379) >>> s, (c += 76379) >>> s)) * 5
                    + noiseBalanced3D_alt((a+((m&2))-1) >>> s, (b+((m&1)<<1)-1) >>> s, (c+((m&4)>>>1)-1) >>> s) * 3
                    >>> 5
                    //+ (noiseBalanced3D_alt((a+((m&2048)>>>10)-1) >>> s, (b+((m&4096)>>>11)-1) >>> s, (c+((m&8192)>>>12)-1) >>> s) & 127)
            ) << u;
            t += 2 << u;
        }
        return lookup[v / t];
    }
    public static int noise3D_emphasized(final int x, final int y, final int z, final int zoom) {
        if (zoom < 1)
            return 0;
        int v = 0, a = x, b = y, c = z, t = 0, m;
        for (int s = zoom, u = 1; s > 0; s--, u++) {
            v += ((m = noiseBalanced3D_alt((a += 76379) >>> s, (b += 76379) >>> s, (c += 76379) >>> s)) * 5
                    + noiseBalanced3D_alt((a+((m&2))-1) >>> s, (b+((m&1)<<1)-1) >>> s, (c+((m&4)>>>1)-1) >>> s) * 3
                    >>> 5
            /*
            v += (((m = noiseBalanced3D_alt((a += 76379) >>> s, (b += 76379) >>> s, (c += 76379) >>> s)) & 255)
                    + (noiseBalanced3D_alt((a+((m&2))-1) >>> s, (b+((m&4)>>>1)-1) >>> s, (c+((m&8)>>>2)-1) >>> s) & 255)
                    */
                    //+ (noiseBalanced3D_alt((a+((m&2048)>>>10)-1) >>> s, (b+((m&4096)>>>11)-1) >>> s, (c+((m&8192)>>>12)-1) >>> s) & 127)
            ) << u;
            t += 2 << u;
        }
        return lookup_extreme[v / t];
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
