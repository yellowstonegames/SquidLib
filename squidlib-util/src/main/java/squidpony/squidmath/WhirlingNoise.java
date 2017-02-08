package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.PintRNG.determine;
import static squidpony.squidmath.PintRNG.determineBounded;

/**
 * Another experimental noise class. Extends PerlinNoise and should have similar quality, but can be faster and has less
 * periodic results. This is still considered experimental because the exact output may change in future versions, along
 * with the scale (potentially) and the parameters it takes. In general, {@link #noise(double, double)} and
 * {@link #noise(double, double, double)} should have similar appearance to {@link PerlinNoise#noise(double, double)}
 * and {@link PerlinNoise#noise(double, double, double)}, but are not forced to a zoomed-in scale like PerlinNoise makes
 * its results, are less likely to repeat sections of noise, and are also somewhat faster (a 20% speedup can be expected
 * over PerlinNoise using those two methods). Sound good? This also performs minimal (if any) arithmetic on 64-bit long
 * numbers, instead using double and int, a style that is ideal for GWT (this is true of PerlinNoise too).
 * <br>
 * Created by Tommy Ettinger on 12/14/2016.
 */
@Beta
public class WhirlingNoise extends PerlinNoise {
    private static int fastFloor(double t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }
    private static int fastFloor(float t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }
    protected static final float root3 = 1.7320508f, root5 = 2.236068f,
            F2f = 0.5f * (root3 - 1f),
            G2f = (3f - root3) * 0.16666667f,
            F3f = 0.33333334f,
            G3f = 0.16666667f,
            F4f = (root5 - 1f) * 0.25f,
            G4f = (5f - root5) * 0.05f,
            unit1_4f =  0.70710678118f, unit1_8f = 0.38268343236f, unit3_8f = 0.92387953251f;

    protected static final float[][] grad2f = {
            {1f, 0f}, {-1f, 0f}, {0f, 1f}, {0f, -1f},
            {unit3_8f, unit1_8f}, {unit3_8f, -unit1_8f}, {-unit3_8f, unit1_8f}, {-unit3_8f, -unit1_8f},
            {unit1_4f, unit1_4f}, {unit1_4f, -unit1_4f}, {-unit1_4f, unit1_4f}, {-unit1_4f, -unit1_4f},
            {unit1_8f, unit3_8f}, {unit1_8f, -unit3_8f}, {-unit1_8f, unit3_8f}, {-unit1_8f, -unit3_8f}};

    protected static float dotf(final float g[], final float x, final float y) {
        return g[0] * x + g[1] * y;
    }

    protected static float dotf(final int g[], float x, final float y, final float z) {
        return g[0] * x + g[1] * y + g[2] * z;
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

    /**
     * 2D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double)} and this method. Roughly
     * 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks because
     * it uses a pseudo-random function (curiously, {@link PintRNG#determine(int)}, which is optimized for the
     * limitations of GWT but is rather fast here) instead of a number chosen from a single 256-element array.
     *
     * @param xin x input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin) {
        //xin *= epi;
        //yin *= epi;
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
        int gi0 = determine(i + determine(j)) & 15;
        int gi1 = determine(i + i1 + determine(j + j1)) & 15;
        int gi2 = determine(i + 1 + determine(j + 1)) & 15;

        // Calculate the contribution from the three corners
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0.0;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dot(grad2[gi0], x0, y0);
            // for 2D gradient
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0.0;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dot(grad2[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0.0;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dot(grad2[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70.0 * (noise0 + noise1 + noise2);
    }


    /**
     * 2D simplex noise returning a float; extremely similar to {@link #noise(double, double)}, but this may be slightly
     * faster or slightly slower. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result
     * will be different when passing the same arguments to {@link PerlinNoise#noise(double, double)} and this method.
     *
     * @param x x input; works well if between 0.0 and 1.0, but anything is accepted
     * @param y y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static float noiseAlt(double x, double y) {
        //xin *= epi;
        //yin *= epi;
        float noise0, noise1, noise2; // from the three corners
        float xin = (float)x, yin = (float)y;
        // Skew the input space to determine which simplex cell we're in
        float skew = (xin + yin) * F2f; // Hairy factor for 2D
        int i = fastFloor(xin + skew);
        int j = fastFloor(yin + skew);
        float t = (i + j) * G2f;
        float X0 = i - t; // Unskew the cell origin back to (x,y) space
        float Y0 = j - t;
        float x0 = xin - X0; // The x,y distances from the cell origin
        float y0 = yin - Y0;
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
        float x1 = x0 - i1 + G2f; // Offsets for middle corner in (x,y)
        // unskewed coords
        float y1 = y0 - j1 + G2f;
        float x2 = x0 - 1f + 2f * G2f; // Offsets for last corner in (x,y)
        // unskewed coords
        float y2 = y0 - 1f + 2f * G2f;
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
        int gi0 = PintRNG.determine(i + PintRNG.determine(j)) & 15;
        int gi1 = PintRNG.determine(i + i1 + PintRNG.determine(j + j1)) & 15;
        int gi2 = PintRNG.determine(i + 1 + PintRNG.determine(j + 1)) & 15;

        // Calculate the contribution from the three corners
        float t0 = 0.5f - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0f;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dotf(grad2f[gi0], x0, y0);
            // for 2D gradient
        }
        float t1 = 0.5f - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0f;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dotf(grad2f[gi1], x1, y1);
        }
        float t2 = 0.5f - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0f;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dotf(grad2f[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70f * (noise0 + noise1 + noise2);
    }

    /**
     * 3D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double)} and this method.
     * Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks
     * because it uses a pseudo-random function (curiously, {@link PintRNG#determine(int)}, which is optimized for the
     * limitations of GWT but is rather fast here) instead of a number chosen from a single 256-element array.
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin, double zin) {
        //xin *= epi;
        //yin *= epi;
        //zin *= epi;
        double n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to determine which simplex cell we're in
        double s = (xin + yin + zin) * F3; // Very nice and simple skew
        // factor for 3D
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        int k = fastFloor(zin + s);
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
        double x2 = x0 - i2 + F3; // Offsets for third corner in
        // (x,y,z) coords
        double y2 = y0 - j2 + F3;
        double z2 = z0 - k2 + F3;
        double x3 = x0 - 0.5; // Offsets for last corner in
        // (x,y,z) coords
        double y3 = y0 - 0.5;
        double z3 = z0 - 0.5;
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

        int gi0 = determineBounded(i + determine(j + determine(k)), 12);
        int gi1 = determineBounded(i + i1 + determine(j + j1 + determine(k + k1)), 12);
        int gi2 = determineBounded(i + i2 + determine(j + j2 + determine(k + k2)), 12);
        int gi3 = determineBounded(i + 1 + determine(j + 1 + determine(k + 1)), 12);

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

    /**
     * 3D simplex noise returning a float; extremely similar to {@link #noise(double, double, double)}, but this may
     * be slightly faster or slightly slower. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of
     * the result will be different when passing the same arguments to {@link PerlinNoise#noise(double, double, double)}
     * and this method.
     *
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static float noiseAlt(double x, double y, double z) {
        //xin *= epi;
        //yin *= epi;
        //zin *= epi;
        float xin = (float)x, yin = (float)y, zin = (float)z;
        float n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to determine which simplex cell we're in
        float s = (xin + yin + zin) * F3f; // Very nice and simple skew
        // factor for 3D
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        int k = fastFloor(zin + s);
        float t = (i + j + k) * G3f;
        float X0 = i - t; // Unskew the cell origin back to (x,y,z) space
        float Y0 = j - t;
        float Z0 = k - t;
        float x0 = xin - X0; // The x,y,z distances from the cell origin
        float y0 = yin - Y0;
        float z0 = zin - Z0;
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
        float x1 = x0 - i1 + G3f; // Offsets for second corner in (x,y,z)
        // coords
        float y1 = y0 - j1 + G3f;
        float z1 = z0 - k1 + G3f;
        float x2 = x0 - i2 + F3f; // Offsets for third corner in
        // (x,y,z) coords
        float y2 = y0 - j2 + F3f;
        float z2 = z0 - k2 + F3f;
        float x3 = x0 - 0.5f; // Offsets for last corner in
        // (x,y,z) coords
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;
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

        int gi0 = PintRNG.determineBounded(i + PintRNG.determine(j + PintRNG.determine(k)), 12);
        int gi1 = PintRNG.determineBounded(i + i1 + PintRNG.determine(j + j1 + PintRNG.determine(k + k1)), 12);
        int gi2 = PintRNG.determineBounded(i + i2 + PintRNG.determine(j + j2 + PintRNG.determine(k + k2)), 12);
        int gi3 = PintRNG.determineBounded(i + 1 + PintRNG.determine(j + 1 + PintRNG.determine(k + 1)), 12);

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
        float t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0f;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dotf(grad3[gi0], x0, y0, z0);
        }
        float t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0f;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dotf(grad3[gi1], x1, y1, z1);
        }
        float t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0f;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dotf(grad3[gi2], x2, y2, z2);
        }
        float t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0f;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dotf(grad3[gi3], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        return 32f * (n0 + n1 + n2 + n3);
    }

    /**
     * 4D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double, double)} and this
     * method. Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in
     * chunks because it uses a pseudo-random function (curiously, {@link PintRNG#determine(int)}, which is optimized
     * for the limitations of GWT but is rather fast here) instead of a number chosen from a single 256-element array.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimensional)
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double x, double y, double z, double w) {
        // The skewing and unskewing factors are hairy again for the 4D case

        // Skew the (x,y,z,w) space to determine which cell of 24 simplices
        // we're in
        double s = (x + y + z + w) * F4; // Factor for 4D skewing
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        int k = fastFloor(z + s);
        int l = fastFloor(w + s);
        double t = (i + j + k + l) * G4; // Factor for 4D unskewing
        double X0 = i - t; // Unskew the cell origin back to (x,y,z,w) space
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double x0 = x - X0; // The x,y,z,w distances from the cell origin
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;
        // For the 4D case, the simplex is a 4D shape I won't even try to
        // describe.
        // To find out which of the 24 possible simplices we're in, we need
        // to
        // determine the magnitude ordering of x0, y0, z0 and w0.
        // The method below is a good way of finding the ordering of x,y,z,w
        // and
        // then find the correct traversal order for the simplex we’re in.
        // First, six pair-wise comparisons are performed between each
        // possible pair
        // of the four coordinates, and the results are used to add up binary
        // bits
        // for an integer index.
        int c = (x0 > y0 ? 32 : 0) | (x0 > z0 ? 16 : 0) | (y0 > z0 ? 8 : 0) |
                (x0 > w0 ? 4 : 0) | (y0 > w0 ? 2 : 0) | (z0 > w0 ? 1 : 0);

        // simplex[c] is a 4-vector with the numbers 0, 1, 2 and 3 in some
        // order.
        // Many values of c will never occur, since e.g. x>y>z>w makes x<z,
        // y<w and x<w
        // impossible. Only the 24 indices which have non-zero entries make
        // any sense.
        // We use a thresholding to set the coordinates in turn from the
        // largest magnitude.
        // The number 3 in the "simplex" array is at the position of the
        // largest coordinate.

        // The integer offsets for the second simplex corner
        int i1 = simplex[c][0] >= 3 ? 1 : 0;
        int j1 = simplex[c][1] >= 3 ? 1 : 0;
        int k1 = simplex[c][2] >= 3 ? 1 : 0;
        int l1 = simplex[c][3] >= 3 ? 1 : 0;
        // The number 2 in the "simplex" array is at the second largest
        // coordinate.

        // The integer offsets for the third simplex corner
        int i2 = simplex[c][0] >= 2 ? 1 : 0;
        int j2 = simplex[c][1] >= 2 ? 1 : 0;
        int k2 = simplex[c][2] >= 2 ? 1 : 0;
        int l2 = simplex[c][3] >= 2 ? 1 : 0;
        // The number 1 in the "simplex" array is at the second smallest
        // coordinate.

        // The integer offsets for the fourth simplex corner
        int i3 = simplex[c][0] >= 1 ? 1 : 0;
        int j3 = simplex[c][1] >= 1 ? 1 : 0;
        int k3 = simplex[c][2] >= 1 ? 1 : 0;
        int l3 = simplex[c][3] >= 1 ? 1 : 0;
        // The fifth corner has all coordinate offsets = 1, so no need to
        // look that up.
        double x1 = x0 - i1 + G4; // Offsets for second corner in (x,y,z,w) coords
        double y1 = y0 - j1 + G4;
        double z1 = z0 - k1 + G4;
        double w1 = w0 - l1 + G4;
        double x2 = x0 - i2 + 2.0 * G4; // Offsets for third corner in (x,y,z,w) coords
        double y2 = y0 - j2 + 2.0 * G4;
        double z2 = z0 - k2 + 2.0 * G4;
        double w2 = w0 - l2 + 2.0 * G4;
        double x3 = x0 - i3 + 3.0 * G4; // Offsets for fourth corner in (x,y,z,w) coords
        double y3 = y0 - j3 + 3.0 * G4;
        double z3 = z0 - k3 + 3.0 * G4;
        double w3 = w0 - l3 + 3.0 * G4;
        double x4 = x0 - 1.0 + 4.0 * G4; // Offsets for last corner in (x,y,z,w) coords
        double y4 = y0 - 1.0 + 4.0 * G4;
        double z4 = z0 - 1.0 + 4.0 * G4;
        double w4 = w0 - 1.0 + 4.0 * G4;

        int gi0 = determine(i + determine(j + determine(k + determine(l)))) & 31;
        int gi1 = determine(i + i1 + determine(j + j1 + determine(k + k1 + determine(l + l1)))) & 31;
        int gi2 = determine(i + i2 + determine(j + j2 + determine(k + k2 + determine(l + l2)))) & 31;
        int gi3 = determine(i + i3 + determine(j + j3 + determine(k + k3 + determine(l + l3)))) & 31;
        int gi4 = determine(i + 1 + determine(j + 1 + determine(k + 1 + determine(l + 1)))) & 31;

        // Noise contributions from the five corners are n0 to n4

        // Calculate the contribution from the five corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0, n0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad4[gi0], x0, y0, z0, w0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1, n1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad4[gi1], x1, y1, z1, w1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2,  n2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad4[gi2], x2, y2, z2, w2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3, n3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad4[gi3], x3, y3, z3, w3);
        }
        double t4 = 0.6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4, n4;
        if (t4 < 0) {
            n4 = 0.0;
        } else {
            t4 *= t4;
            n4 = t4 * t4 * dot(grad4[gi4], x4, y4, z4, w4);
        }
        // Sum up and scale the result to cover the range [-1,1]
        return 27.0 * (n0 + n1 + n2 + n3 + n4);
    }

    /*
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
    }
    */
}
