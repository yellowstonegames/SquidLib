package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.LightRNG.determine;

/**
 * Another experimental noise class. Extends PerlinNoise and should look similar, but have less periodic results.
 * Created by Tommy Ettinger on 12/14/2016.
 */
@Beta
public class WhirlingNoise extends PerlinNoise {

    private static int fastFloor(double t) {
        return t > 0 ? (int) t : (int) t - 1;
    }

    private static double lerp(double s, double v1, double v2) {
        return v1 + s * (v2 - v1);
    }

    public static long rawNoise(final int x, final int y, long state)
    {
        long inter = 0x632BE59BD9B4E019L;
        state += ((inter ^= x * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L)
                + ((inter ^= y * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L);
        return state ^ ((inter ^ state) >>> 16) * 0x9E3779B97F4A7C15L;
    }
    public static long rawNoise(final int x, final int y, final int z, long state)
    {
        long inter = 0x632BE59BD9B4E019L;
        state += ((inter ^= x * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L)
                + ((inter ^= y * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L)
                + ((inter ^= z * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L);
        return state ^ ((inter ^ state) >>> 16) * 0x9E3779B97F4A7C15L;
    }
    public static long rawNoise(final int x, final int y, final int z, final int w, long state)
    {
        long inter = 0x632BE59BD9B4E019L;
        state += ((inter ^= x * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L)
                + ((inter ^= y * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L)
                + ((inter ^= z * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L)
                + ((inter ^= w * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L);
        return state ^ ((inter ^ state) >>> 16) * 0x9E3779B97F4A7C15L;
    }
    public static double interpolate(double t)
    {
        //debug
        //return 0;
        //linear
        //return t;
        //hermite
        //return t * t * (3 - 2 * t);
        //quintic
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /*
    public static double interpolate(double t, double low, double high)
    {
        //debug
        //return 0;
        //linear
        //return t;
        //hermite
        //return t * t * (3 - 2 * t);
        //quintic
        //return t * t * t * (t * (t * 6 - 15) + 10);

        //t = (t + low + 0.5 - high) * 0.5;
        //t = (t < 0.5) ? t * low : 1.0 - ((1.0 - t) * high);
        //t = Math.pow(t, 1.0 + high - low);

        //return (t + 0.5 + high - low) * 0.5;
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    public static double noise(final double x, final double y, final long seed) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;


        long v1 = rawNoise(x0, y0, seed);
        long v2 = rawNoise(x1, y0, seed);
        double d1 = Float.intBitsToFloat(0x40000000 | (int)(v1 & 0x7FFFFF)) - 3.0; //-1 to 1
        double d2 = Float.intBitsToFloat(0x40000000 | (int)(v2 & 0x7FFFFF)) - 3.0; //-1 to 1
        double p1 = Float.intBitsToFloat(0x3F800000 | (int)(v1 >>> 32 & 0x7FFFFF)) - 1.0; //0 to 1.0
        double p2 = Float.intBitsToFloat(0x3F800000 | (int)(v2 >>> 32 & 0x7FFFFF)) - 1.0; //0 to 1.0
        double xs = interpolate(x - x0, p1, p2);
        double xy1 = lerp(xs, d1, d2);
        v1 = rawNoise(x0, y1, seed);
        v2 = rawNoise(x1, y1, seed);
        p2 = Float.intBitsToFloat(0x3F800000 | (int)(v1 >>> 32 & 0x7FFFFF)) - 1.0; //0 to 1.0
        d1 = Float.intBitsToFloat(0x40000000 | (int)(v1 & 0x7FFFFF)) - 3.0;
        d2 = Float.intBitsToFloat(0x40000000 | (int)(v2 & 0x7FFFFF)) - 3.0;
        double xy2 = lerp(xs, d1, d2);

        return lerp(interpolate(y - y0, p1, p2), xy1, xy2);
    }
    public static double noise(final double x, final double y, final double z, final long seed) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        long v1 = rawNoise(x0, y0, z0, seed);
        long v2 = rawNoise(x1, y0, z0, seed);
        double d1 = Float.intBitsToFloat(0x40000000 | (int)(v1 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        double d2 = Float.intBitsToFloat(0x40000000 | (int)(v2 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        double pa = Float.intBitsToFloat(0x3F800000 | (int)(v1 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        double pb = Float.intBitsToFloat(0x3F800000 | (int)(v2 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        double xs = interpolate(x - x0);
        double xy1 = lerp(xs, d1*pa, d2*pb);
        double weight = lerp(xs, pa, pb);
        v1 = rawNoise(x0, y1, z0, seed);
        v2 = rawNoise(x1, y1, z0, seed);
        d1 = Float.intBitsToFloat(0x40000000 | (int)(v1 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        d2 = Float.intBitsToFloat(0x40000000 | (int)(v2 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        pa = Float.intBitsToFloat(0x3F800000 | (int)(v1 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        pb = Float.intBitsToFloat(0x3F800000 | (int)(v2 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        double xy2 = lerp(xs, d1*pa, d2*pb);
        double ys = interpolate(y - y0);

        weight = lerp(ys, weight, lerp(xs, pa, pb));
        double xyz1 = lerp(ys, xy1, xy2);


        v1 = rawNoise(x0, y0, z1, seed);
        v2 = rawNoise(x1, y0, z1, seed);
        d1 = Float.intBitsToFloat(0x40000000 | (int)(v1 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        d2 = Float.intBitsToFloat(0x40000000 | (int)(v2 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        pa = Float.intBitsToFloat(0x3F800000 | (int)(v1 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        pb = Float.intBitsToFloat(0x3F800000 | (int)(v2 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        xy1 = lerp(xs, d1*pa, d2*pb);
        double weight2 = lerp(xs, pa, pb);

        v1 = rawNoise(x0, y1, z1, seed);
        v2 = rawNoise(x1, y1, z1, seed);
        d1 = Float.intBitsToFloat(0x40000000 | (int)(v1 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        d2 = Float.intBitsToFloat(0x40000000 | (int)(v2 & 0x7FFFFF)) - 2.0; //0.0 to 2.0
        pa = Float.intBitsToFloat(0x3F800000 | (int)(v1 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        pb = Float.intBitsToFloat(0x3F800000 | (int)(v2 >>> 32 & 0x7FFFFF)); //1.0 to 2.0
        xy2 = lerp(xs, d1*pa, d2*pb);
        weight2 = lerp(ys, weight2, lerp(xs, pa, pb));
        double xyz2 = lerp(ys, xy1, xy2);
        double zs = interpolate(z - z0);
        return lerp(zs, xyz1, xyz2) / (lerp(zs, weight, weight2)) - 1.0;
    }
    */


    protected static double dot(double g0, double g1, double x, double y) {
        return g0 * x + g1 * y;
    }

    protected static double dot(double g0, double g1, double g2, double x, double y, double z) {
        return g0 * x + g1 * y + g2 * z;
    }

    protected static double dot(double g0, double g1, double g2, double g3, double x, double y, double z, double w) {
        return g0 * x + g1 * y + g2 * z + g3 * w;
    }


    public static double noise(double xin, double yin) {
        return noiseSeeded(xin, yin, 1L);
    }
    /**
     * 2D simplex noise.
     * This doesn't use its parameters verbatim; xin and yin are both effectively divided by
     * ({@link Math#E} * {@link Math#PI}), because without a step like that, any integer parameters would return 0 and
     * only doubles with a decimal component would produce actual noise. This step allows integers to be passed in a
     * arguments, and changes the cycle at which 0 is repeated to multiples of (E*PI).
     *
     * @param xin x input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noiseSeeded(double xin, double yin, long seed) {
        xin *= epi;
        yin *= epi;
        double noise0, noise1, noise2; // from the three corners
        // Skew the input space to determine which simplex cell we're in
        double skew = (xin + yin) * F2; // Hairy factor for 2D
        int i = fastFloor(xin + skew);
        int j = fastFloor(yin + skew);
        double t = (i + j) * G2;
        double X0 = i - t; // Unskew the cell origin back to (x,y) space
        double Y0 = j - t;
        double x0 = xin - X0; // The x,y distances from the cell origin
        double y0 = yin - Y0;
        // For the 2D case, the simplex shape is an equilateral triangle.
        // Determine which simplex we are in.
        int i1, j1; // Offsets for second (middle) corner of simplex in (i,j)
        // coords
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } // lower triangle, XY order: (0,0)->(1,0)->(1,1)
        else {
            i1 = 0;
            j1 = 1;
        } // upper triangle, YX order: (0,0)->(0,1)->(1,1)
        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y),
        // where
        // c = (3-sqrt(3))/6
        double x1 = x0 - i1 + G2; // Offsets for middle corner in (x,y)
        // unskewed coords
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2; // Offsets for last corner in (x,y)
        // unskewed coords
        double y2 = y0 - 1.0 + 2.0 * G2;
        // Work out the hashed gradient indices of the three simplex corners
        /*
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii + perm[jj]] & 15;
        int gi1 = perm[ii + i1 + perm[jj + j1]] & 15;
        int gi2 = perm[ii + 1 + perm[jj + 1]] & 15;
        */
        /*
        int hash = (int) rawNoise(i + (j * 0x9E3779B9),
                i + i1 + ((j + j1) * 0x9E3779B9),
                i + 1 + ((j + 1) * 0x9E3779B9),
                seed);
        int gi0 = hash & 15;
        int gi1 = (hash >>>= 4) & 15;
        int gi2 = (hash >>> 4) & 15;
        */
        int gi0 = (int) determine(i + determine(j)) & 15;
        int gi1 = (int) determine(i + i1 + determine(j + j1)) & 15;
        int gi2 = (int) determine(i + 1 + determine(j + 1)) & 15;

        // Calculate the contribution from the three corners
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0.0;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dot(phiGrad2[gi0], x0, y0); // (x,y) of grad3 used
            // for 2D gradient
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0.0;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dot(phiGrad2[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0.0;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dot(phiGrad2[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70.0 * (noise0 + noise1 + noise2);
    }

    /**
     * 3D simplex noise.
     *
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin, double zin) {
        return noiseSeeded(xin, yin, zin, 1L);
    }
    /**
     * 3D simplex noise.
     *
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noiseSeeded(double xin, double yin, double zin, long seed) {
        xin *= epi;
        yin *= epi;
        zin *= epi;
        double n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to determine which simplex cell we're in
        double s = (xin + yin + zin) * F3; // Very nice and simple skew
        // factor for 3D
        int i = (int) Math.floor(xin + s);
        int j = (int) Math.floor(yin + s);
        int k = (int) Math.floor(zin + s);
        double t = (i + j + k) * G3;
        double X0 = i - t; // Unskew the cell origin back to (x,y,z) space
        double Y0 = j - t;
        double Z0 = k - t;
        double x0 = xin - X0; // The x,y,z distances from the cell origin
        double y0 = yin - Y0;
        double z0 = zin - Z0;
        // For the 3D case, the simplex shape is a slightly irregular
        // tetrahedron.
        // Determine which simplex we are in.
        int i1, j1, k1; // Offsets for second corner of simplex in (i,j,k)
        // coords
        int i2, j2, k2; // Offsets for third corner of simplex in (i,j,k)
        // coords
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // X Y Z order
            else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // X Z Y order
            else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // Z X Y order
        } else { // x0<y0
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Z Y X order
            else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Y Z X order
            else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // Y X Z order
        }
        // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in
        // (x,y,z),
        // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in
        // (x,y,z), and
        // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in
        // (x,y,z), where
        // c = 1/6.
        double x1 = x0 - i1 + G3; // Offsets for second corner in (x,y,z)
        // coords
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0 * G3; // Offsets for third corner in
        // (x,y,z) coords
        double y2 = y0 - j2 + 2.0 * G3;
        double z2 = z0 - k2 + 2.0 * G3;
        double x3 = x0 - 1.0 + 3.0 * G3; // Offsets for last corner in
        // (x,y,z) coords
        double y3 = y0 - 1.0 + 3.0 * G3;
        double z3 = z0 - 1.0 + 3.0 * G3;
        // Work out the hashed gradient indices of the four simplex corners

        /*
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;

        int gi0 = perm[ii + perm[jj + perm[kk]]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12;
        int gi2 = perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12;
        int gi3 = perm[ii + 1 + perm[jj + 1 + perm[kk + 1]]] % 12;
        */


        int gi0 = determine(seed + i + determine(j + determine(k)), 12);
        int gi1 = determine(seed + i + i1 + determine(j + j1 + determine(k + k1)), 12);
        int gi2 = determine(seed + i + i2 + determine(j + j2 + determine(k + k2)), 12);
        int gi3 = determine(seed + i + 1 + determine(j + 1 + determine(k + 1)), 12);

        /*
        int hash = (int) rawNoise(i + ((j + k * 0x632BE5AB) * 0x9E3779B9),
                i + i1 + ((j + j1 + (k + k1) * 0x632BE5AB) * 0x9E3779B9),
                i + i2 + ((j + j2 + (k + k2) * 0x632BE5AB) * 0x9E3779B9),
                i + 1 + ((j + 1 + ((k + 1) * 0x632BE5AB)) * 0x9E3779B9),
                seed);
        int gi0 = (hash >>>= 4) % 12;
        int gi1 = (hash >>>= 4) % 12;
        int gi2 = (hash >>>= 4) % 12;
        int gi3 = (hash >>> 4) % 12;
        */

        //int hash = (int) rawNoise(i, j, k, seed);
        //int gi0 = (hash >>>= 4) % 12, gi1 = (hash >>>= 4) % 12, gi2 = (hash >>>= 4) % 12, gi3 = (hash >>>= 4) % 12;
        // Calculate the contribution from the four corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0, z0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1, z1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2, z2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad3[gi3], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        return 32.0 * (n0 + n1 + n2 + n3);
    }


    public static void main(String[] args)
    {
        long hash;
        for (int x = -8; x < 8; x++) {
            for (int y = -8; y < 8; y++) {
                hash = rawNoise(x, y, 1);
                System.out.println("x=" + x + " y=" +  y);
                System.out.println("normal=" +
                        (Float.intBitsToFloat(0x3F800000 | (int)(hash & 0x7FFFFF)) - 1.0));
                System.out.println("tweaked=" +
                        (Float.intBitsToFloat(0x40000000 | (int)(hash & 0x7FFFFF)) - 3.0));
                System.out.println("half=" +
                        (Float.intBitsToFloat(0x3F000000 | (int)(hash & 0x7FFFFF)) - 0.5));
            }
        }
        /*
        for (float f = -2.0f; f <= 2.0f; f+= 0.125f) {
            System.out.printf("%+08f: %08X\n", f, Float.floatToIntBits(f));
        }*/
    }
}
