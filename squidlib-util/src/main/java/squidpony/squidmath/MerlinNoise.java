package squidpony.squidmath;

/**
 * Simple somewhat-continuous noise functions that use int coordinates instead of the traditional double (this approach
 * works better on a grid). It's called this because it should be possible to replace PerlinNoise with MerlinNoise, and
 * because working with noise functions makes me feel like a wizard.
 */
public class MerlinNoise {

    private MerlinNoise()
    {

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
    }


    /**
     * 2D merlin noise.
     *
     * @param x x input
     * @param y y input
     * @return noise from 0 to 255, inclusive
     */
    public static int noise2D(int x, int y) {
        return ((rawNoise2D(x, y) << 2) +
                rawNoise2D(x + 1, y) + rawNoise2D(x - 1, y) + rawNoise2D(x, y + 1) + rawNoise2D(x, y - 1)
        ) >> 3;
    }

    /**
     * 2D merlin noise with a zoom factor.
     *
     * @param x x input
     * @param y y input
     * @param zoom if greater than 1.0, will make the details of the noise larger; if less, will make them smaller
     * @return noise from 0 to 255, inclusive
     */
    public static int noise2D(int x, int y, double zoom) {
        final double alef = x / zoom, bet = y / zoom;
        final int alpha = (int) (alef), beta = (int) (bet);
        final double aBias = (alef - alpha), bBias = (bet - beta),
                absA = Math.abs(aBias - 0.5), absB = Math.abs(bBias - 0.5),
                cornerPref = Math.max(absA + absB - 0.5, 0),
                mx = Math.max(absA, absB) * 3.6 + (absA + absB) * 4.2;
        final int centerCell = rawNoise2D(alpha, beta),
                aCell = rawNoise2D(alpha + (aBias < 0.5 ? -1 : 1), beta),
                bCell = rawNoise2D(alpha, beta + (bBias < 0.5 ? -1 : 1)),
                cornerCell = (rawNoise2D(alpha + (aBias < 0.5 ? -1 : 1), beta + (bBias < 0.5 ? -1 : 1)) * 6 + (aCell + bCell) * 5) >> 4;
        return (int)(( //(centerCell * (1 - absA - absB) + aCell * absA + bCell * absB)
                centerCell * (8 - mx) +
                (cornerCell * cornerPref * 2.0 +
                aCell * (0.5 + absA - absB - cornerPref) +
                bCell * (0.5 + absB - absA - cornerPref)) * mx
        ) * 0.125);
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
