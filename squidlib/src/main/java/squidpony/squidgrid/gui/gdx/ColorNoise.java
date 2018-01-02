package squidpony.squidgrid.gui.gdx;

import squidpony.squidmath.NumberTools;
import squidpony.squidmath.WhirlingNoise;

import static squidpony.squidmath.Noise.PointHash.hashAll;
import static squidpony.squidmath.Noise.fastFloor;

/**
 * Created by Tommy Ettinger on 6/12/2017.
 */
public class ColorNoise extends WhirlingNoise {
    public static final ColorNoise instance = new ColorNoise();
    public ColorNoise() {
    }

    public ColorNoise(int seed) {
        super(seed);
    }

    public static int bounce256(final int s) { return (s ^ -((s & 0x100) >> 8)) & 0xff; }

    public static float colorNoise(final double noise)
    {
        return SColor.floatGetHSV(NumberTools.zigzag((float) (noise * 16.0)), 0.75f + (float) NumberTools.zigzag(noise * 9.0) * 0.25f, 0.7f + (float) NumberTools.zigzag(noise * 7.0 + 0.5) * 0.3f, 1f);
//        return NumberTools.intBitsToFloat(0xfe000000 |
//                (bounce256((int) ((noise * 1.29 + 1.39) * (0x3DF9f)) >>> 8) << 16) |
//                (bounce256((int) ((noise * 1.18 + 1.45) * (0x3EB9f)) >>> 8) << 8) |
//                (bounce256((int) ((noise * 1.07 + 1.51) * (0x3E99f)) >>> 8)));
    }

    /**
     * 2D simplex noise that produces a color, as a packed float.
     *
     * @param xin X input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin Y input; works well if between 0.0 and 1.0, but anything is accepted
     * @param seed a seed that will change how and when any colors will be produced
     * @return noise in the form of a packed float color
     */
    public static float colorNoise(final float xin, final float yin, final int seed) {
        float noise0, noise1, noise2; // from the three corners
        // Skew the input space to figure out which simplex cell we're in
        float skew = (xin + yin) * F2f; // Hairy factor for 2D
        int i = fastFloor(xin + skew);
        int j = fastFloor(yin + skew);
        float t = (i + j) * G2f;
        float X0 = i - t; // Unskew the cell origin back to (x,y) space
        float Y0 = j - t;
        float x0 = xin - X0; // The x,y distances from the cell origin
        float y0 = yin - Y0;
        // For the 2D case, the simplex shape is an equilateral triangle.
        // determine which simplex we are in.
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
//        int gi0 = (int)(determine(seed + i + determine(j)) >>> 16);
//        int gi1 = (int)(determine(seed + i + i1 + determine(j + j1)) >>> 16);
//        int gi2 = (int)(determine(seed + i + 1 + determine(j + 1)) >>> 16);
        int gi0 = (int)(hashAll(i, j, seed) & 0xFFFFFFL);
        int gi1 = (int)(hashAll(i + i1, j + j1, seed) & 0xFFFFFFL);
        int gi2 = (int)(hashAll(i + 1, j + 1, seed) & 0xFFFFFFL);

        float red, green, blue, t0, t1, t2;
        // Calculate the contribution from the three corners
        t0 = 0.75f - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0f;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dotf(phiGrad2f[gi0 & 255], x0, y0);
        }
        t1 = 0.75f - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0f;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dotf(phiGrad2f[gi1 & 255], x1, y1);
        }
        t2 = 0.75f - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0f;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dotf(phiGrad2f[gi2 & 255], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        red = 4.5625f * (noise0 + noise1 + noise2) + 0.5f;
        gi0 >>>= 8;
        gi1 >>>= 8;
        gi2 >>>= 8;

        // Calculate the contribution from the three corners
        t0 = 0.75f - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0f;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dotf(phiGrad2f[gi0 & 255], x0, y0);
        }
        t1 = 0.75f - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0f;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dotf(phiGrad2f[gi1 & 255], x1, y1);
        }
        t2 = 0.75f - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0f;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dotf(phiGrad2f[gi2 & 255], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        green = 4.5625f * (noise0 + noise1 + noise2) + 0.5f;
        gi0 >>>= 8;
        gi1 >>>= 8;
        gi2 >>>= 8;
        // Calculate the contribution from the three corners
        t0 = 0.75f - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0f;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dotf(phiGrad2f[gi0 & 255], x0, y0);
        }
        t1 = 0.75f - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0f;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dotf(phiGrad2f[gi1 & 255], x1, y1);
        }
        t2 = 0.75f - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0f;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dotf(phiGrad2f[gi2 & 255], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        blue = 4.5625f * (noise0 + noise1 + noise2) + 0.5f;
        return SColor.floatGet(red, green, blue, 1f);
    }



    /**
     * 3D simplex noise that produces a color, as a packed float.
     *
     * @param xin X input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin Y input; works well if between 0.0 and 1.0, but anything is accepted
     * @param zin Z input; works well if between 0.0 and 1.0, but anything is accepted
     * @param seed a seed that will change how and when any colors will be produced
     * @return noise in the form of a packed float color
     */
    public static float colorNoise(final float xin, final float yin, final float zin, final int seed) {
        float n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to figure out which simplex cell we're in
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
        // determine which simplex we are in.
        int i1, j1, k1; // Offsets for second corner of simplex in (i,j,k) coords
        int i2, j2, k2; // Offsets for third corner of simplex in (i,j,k) coords
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
        // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in (x,y,z),
        // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in (x,y,z), and
        // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in (x,y,z), where c = 1/6.
        float x1 = x0 - i1 + G3f; // Offsets for second corner in (x,y,z) coords
        float y1 = y0 - j1 + G3f;
        float z1 = z0 - k1 + G3f;
        float x2 = x0 - i2 + F3f; // Offsets for third corner in (x,y,z) coords
        float y2 = y0 - j2 + F3f;
        float z2 = z0 - k2 + F3f;
        float x3 = x0 - 0.5f; // Offsets for last corner in (x,y,z) coords
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;
        // Work out the hashed gradient indices of the four simplex corners
        int gi0 = (int)(hashAll(i, j , k, seed) & 0xFFFFFFFFL);
        int gi1 = (int)(hashAll(i + i1, j + j1 , k + k1, seed) & 0xFFFFFFFFL);
        int gi2 = (int)(hashAll(i + i2, j + j2 , k + k2, seed) & 0xFFFFFFFFL);
        int gi3 = (int)(hashAll(i + 1, j + 1 , k + 1, seed) & 0xFFFFFFFFL);
        float red, green, blue, t0, t1, t2, t3;
        // Calculate the contribution from the four corners
        t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0f;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dotf(grad3f[gi0 & 31], x0, y0, z0);
        }
        t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0f;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dotf(grad3f[gi1 & 31], x1, y1, z1);
        }
        t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0f;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dotf(grad3f[gi2 & 31], x2, y2, z2);
        }
        t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0f;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dotf(grad3f[gi3 & 31], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        red = 15.75f * (n0 + n1 + n2 + n3) + 0.5f;
        gi0 >>>= 8;
        gi1 >>>= 8;
        gi2 >>>= 8;
        gi3 >>>= 8;
        // Calculate the contribution from the four corners
        t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0f;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dotf(grad3f[gi0 & 31], x0, y0, z0);
        }
        t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0f;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dotf(grad3f[gi1 & 31], x1, y1, z1);
        }
        t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0f;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dotf(grad3f[gi2 & 31], x2, y2, z2);
        }
        t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0f;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dotf(grad3f[gi3 & 31], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        green = 15.75f * (n0 + n1 + n2 + n3) + 0.5f;
        gi0 >>>= 8;
        gi1 >>>= 8;
        gi2 >>>= 8;
        gi3 >>>= 8;
        // Calculate the contribution from the four corners
        t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0f;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dotf(grad3f[gi0 & 31], x0, y0, z0);
        }
        t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0f;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dotf(grad3f[gi1 & 31], x1, y1, z1);
        }
        t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0f;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dotf(grad3f[gi2 & 31], x2, y2, z2);
        }
        t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0f;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dotf(grad3f[gi3 & 31], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        blue = 15.75f * (n0 + n1 + n2 + n3) + 0.5f;
        return SColor.floatGet(red, green, blue, 1f);
    }
}
