package squidpony.squidgrid.gui.gdx;

import squidpony.squidmath.NumberTools;
import squidpony.squidmath.SeededNoise;

/**
 * Created by Tommy Ettinger on 6/12/2017.
 */
public class ColorNoise extends SeededNoise {
    public static final ColorNoise instance = new ColorNoise();
    public ColorNoise() {
    }

    public ColorNoise(int seed) {
        super(seed);
    }

    public static int bounce256(final int s) { return (s ^ -((s & 0x100) >> 8)) & 0xff; }

    public static float colorNoise(final double noise)
    {
        return SColor.floatGetHSV(((float) noise + 1f), 0.8f + (float) NumberTools.bounce(noise * 9.0 + 10.0) * 0.2f, 0.8f + (float) NumberTools.bounce(noise * 7.0 + 8.5) * 0.2f, 1f);
//        return NumberUtils.intToFloatColor(0xfe000000 |
//                (bounce256((int) ((noise * 1.29 + 1.39) * (0x3DF9f)) >>> 8) << 16) |
//                (bounce256((int) ((noise * 1.18 + 1.45) * (0x3EB9f)) >>> 8) << 8) |
//                (bounce256((int) ((noise * 1.07 + 1.51) * (0x3E99f)) >>> 8)));
    }

    public static float colorNoise(final float x, final float y, final int seed) {
        final float s = (x + y) * F2;
        final float[] gradient2DLUT = SeededNoise.gradient2DLUT;
        final int i = fastFloor(x + s),
                j = fastFloor(y + s);
        final float t = (i + j) * G2,
                X0 = i - t,
                Y0 = j - t,
                x0 = x - X0,
                y0 = y - Y0;
        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        final float
                x1 = x0 - i1 + G2,
                y1 = y0 - j1 + G2,
                x2 = x0 - 1f + 2f * G2,
                y2 = y0 - 1f + 2f * G2;
        int h0 = hash(i, j, seed),
                h1 = hash(i + i1, j + j1, seed),
                h2 = hash(i + 1, j + 1, seed);
        float n0r, n1r, n2r, n0g, n1g, n2g, n0b, n1b, n2b;
        float t0 = 0.5f - x0 * x0 - y0 * y0;
        if (t0 < 0)
        {
            n0r = n0g = n0b = 0f;
        }
        else {
            t0 *= t0;
            t0 *= t0;
            n0r = t0 * (x0 * gradient2DLUT[h0 << 1] + y0 * gradient2DLUT[h0 << 1 | 1]);
            h0 = h0 * 421 + 61 & 255;
            n0g = t0 * (x0 * gradient2DLUT[h0 << 1] + y0 * gradient2DLUT[h0 << 1 | 1]);
            h0 = h0 * 337 + 61 & 255;
            n0b = t0 * (x0 * gradient2DLUT[h0 << 1] + y0 * gradient2DLUT[h0 << 1 | 1]);
        }
        float t1 = 0.5f - x1 * x1 - y1 * y1;
        if (t1 < 0)
            n1r = n1g = n1b = 0f;
        else {
            t1 *= t1;
            t1 *= t1;
            n1r = t1 * (x1 * gradient2DLUT[h1 << 1] + y1 * gradient2DLUT[h1 << 1 | 1]);
            h1 = h1 * 421 + 61 & 255;
            n1g = t1 * (x1 * gradient2DLUT[h1 << 1] + y1 * gradient2DLUT[h1 << 1 | 1]);
            h1 = h1 * 337 + 61 & 255;
            n1b = t1 * (x1 * gradient2DLUT[h1 << 1] + y1 * gradient2DLUT[h1 << 1 | 1]);
        }
        float t2 = 0.5f - x2 * x2 - y2 * y2;
        if (t2 < 0)
            n2r = n2g = n2b = 0f;
        else {
            t2 *= t2;
            t2 *= t2;
            n2r = t2 * (x2 * gradient2DLUT[h2 << 1] + y2 * gradient2DLUT[h2 << 1 | 1]);
            h2 = h2 * 421 + 61 & 255;
            n2g = t2 * (x2 * gradient2DLUT[h2 << 1] + y2 * gradient2DLUT[h2 << 1 | 1]);
            h2 = h2 * 337 + 61 & 255;
            n2b = t2 * (x2 * gradient2DLUT[h2 << 1] + y2 * gradient2DLUT[h2 << 1 | 1]);
        }
        return SColor.floatGet((35f * (n0r + n1r + n2r)) * 1.42188695f + 0.5005272445f,
                (35f * (n0g + n1g + n2g)) * 1.42188695f + 0.5005272445f,
                (35f * (n0b + n1b + n2b)) * 1.42188695f + 0.5005272445f,
                1f);

    }


    public static float colorNoise(final float x, final float y, final float z, final int seed) {
        float n0r, n1r, n2r, n3r, n0g, n1g, n2g, n3g, n0b, n1b, n2b, n3b;
        final float[] gradient3DLUT = SeededNoise.gradient3DLUT;
        final float s = (x + y + z) * F3;
        final int i = fastFloor(x + s),
                j = fastFloor(y + s),
                k = fastFloor(z + s);

        final float t = (i + j + k) * G3;
        final float X0 = i - t, Y0 = j - t, Z0 = k - t,
                x0 = x - X0, y0 = y - Y0, z0 = z - Z0;

        int i1, j1, k1;
        int i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }

        float x1 = x0 - i1 + G3,
                y1 = y0 - j1 + G3,
                z1 = z0 - k1 + G3,
                x2 = x0 - i2 + 2f * G3,
                y2 = y0 - j2 + 2f * G3,
                z2 = z0 - k2 + 2f * G3,
                x3 = x0 - 1f + 3f * G3,
                y3 = y0 - 1f + 3f * G3,
                z3 = z0 - 1f + 3f * G3;

        int h0 = hash(i, j, k, seed),
                h1 = hash(i + i1, j + j1, k + k1, seed),
                h2 = hash(i + i2, j + j2, k + k2, seed),
                h3 = hash(i + 1, j + 1, k + 1, seed);

        float t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0.0f)
        {
            n0r = n0g = n0b = 0.0f;
        }
        else {
            t0 *= t0;
            t0 *= t0;
            n0r = t0 * (x0 * gradient3DLUT[h0 * 3] + y0 * gradient3DLUT[h0 * 3 + 1] + z0 * gradient3DLUT[h0 * 3 + 2]);
            h0 = h0 * 421 + 61 & 255;
            n0g = t0 * (x0 * gradient3DLUT[h0 * 3] + y0 * gradient3DLUT[h0 * 3 + 1] + z0 * gradient3DLUT[h0 * 3 + 2]);
            h0 = h0 * 337 + 61 & 255;
            n0b = t0 * (x0 * gradient3DLUT[h0 * 3] + y0 * gradient3DLUT[h0 * 3 + 1] + z0 * gradient3DLUT[h0 * 3 + 2]);
        }

        float t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0.0f)
            n1r = n1g = n1b = 0.0f;
        else {
            t1 *= t1;
            t1 *= t1;
            n1r = t1 * (x1 * gradient3DLUT[h1 * 3] + y1 * gradient3DLUT[h1 * 3 + 1] + z1 * gradient3DLUT[h1 * 3 + 2]);
            h1 = h1 * 421 + 61 & 255;
            n1g = t1 * (x1 * gradient3DLUT[h1 * 3] + y1 * gradient3DLUT[h1 * 3 + 1] + z1 * gradient3DLUT[h1 * 3 + 2]);
            h1 = h1 * 337 + 61 & 255;
            n1b = t1 * (x1 * gradient3DLUT[h1 * 3] + y1 * gradient3DLUT[h1 * 3 + 1] + z1 * gradient3DLUT[h1 * 3 + 2]);
        }

        float t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0.0f)
            n2r = n2g = n2b = 0.0f;
        else {
            t2 *= t2;
            t2 *= t2;
            n2r = t2 * (x2 * gradient3DLUT[h2 * 3] + y2 * gradient3DLUT[h2 * 3 + 1] + z2 * gradient3DLUT[h2 * 3 + 2]);
            h2 = h2 * 421 + 61 & 255;
            n2g = t2 * (x2 * gradient3DLUT[h2 * 3] + y2 * gradient3DLUT[h2 * 3 + 1] + z2 * gradient3DLUT[h2 * 3 + 2]);
            h2 = h2 * 337 + 61 & 255;
            n2b = t2 * (x2 * gradient3DLUT[h2 * 3] + y2 * gradient3DLUT[h2 * 3 + 1] + z2 * gradient3DLUT[h2 * 3 + 2]);
        }

        float t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0.0f)
            n3r = n3g = n3b = 0.0f;
        else {
            t3 *= t3;
            t3 *= t3;
            n3r = t3 * (x3 * gradient3DLUT[h3 * 3] + y3 * gradient3DLUT[h3 * 3 + 1] + z3 * gradient3DLUT[h3 * 3 + 2]);
            h3 = h3 * 421 + 61 & 255;
            n3g = t3 * (x3 * gradient3DLUT[h3 * 3] + y3 * gradient3DLUT[h3 * 3 + 1] + z3 * gradient3DLUT[h3 * 3 + 2]);
            h3 = h3 * 337 + 61 & 255;
            n3b = t3 * (x3 * gradient3DLUT[h3 * 3] + y3 * gradient3DLUT[h3 * 3 + 1] + z3 * gradient3DLUT[h3 * 3 + 2]);
        }
//        t0 = (16f * (n0 + n1 - n2 - n3)) * 1.25086885f + 0.5003194984f;
//        t1 = (16f * (n1 + n2 - n3 - n0)) * 1.25086885f + 0.5003194984f;
//        t2 = (16f * (n2 + n3 - n0 - n1)) * 1.25086885f + 0.5003194984f;
        return SColor.floatGet((16f * (n0r + n1r + n2r + n3r)) * 1.25086885f + 0.5001597492f,
                (16f * (n0g + n1g + n2g + n3g)) * 1.25086885f + 0.5001597492f,
                (16f * (n0b + n1b + n2b + n3b)) * 1.25086885f + 0.5001597492f,
                1f);
//
//        t0 = NumberTools.bounce((n0 + n1 + n2 + n3) * 40f + 5f);
//        t1 = NumberTools.bounce((n0 + n1 + n2 + n3 + t0 * 0.0555f) * 40f + 5f);
//        t2 = NumberTools.bounce((n0 + n1 + n2 + n3 + t1 * 0.0555f) * 40f + 5f);
//        return SColor.floatGet(t0 * 0.5f + 0.5f, t1 * 0.5f + 0.5f, t2 * 0.5f + 0.5f, 1f);
//        return SColor.floatGetHSV(NumberTools.bounce((t0 + n0 + n1 * n2) * 0.8125f + 1.375f) * 0.5f + 0.5f,
//                NumberTools.bounce((t0 + n1 + n2 * n3) * 0.6875f + 2.375f) * 0.5f + 0.5f,
//                NumberTools.bounce((t0 + n2 + n3 * n0) * 0.9375f + 2.25f) * 0.5f + 0.5f, 1f);
    }

    public static float colorNoise(final float x, final float y, final float z, final float w, final int seed) {
        float n0, n1, n2, n3, n4;
        final float s = (x + y + z + w) * F4;
        final int i = fastFloor(x + s), j = fastFloor(y + s), k = fastFloor(z + s), l = fastFloor(w + s);
        final float[] gradient4DLUT = SeededNoise.gradient4DLUT;
        final float t = (i + j + k + l) * G4,
                X0 = i - t,
                Y0 = j - t,
                Z0 = k - t,
                W0 = l - t,
                x0 = x - X0,
                y0 = y - Y0,
                z0 = z - Z0,
                w0 = w - W0;
        final int c = (x0 > y0 ? 128 : 0) | (x0 > z0 ? 64 : 0) | (y0 > z0 ? 32 : 0) | (x0 > w0 ? 16 : 0) | (y0 > w0 ? 8 : 0) | (z0 > w0 ? 4 : 0);
        final int i1 = SIMPLEX[c] >>> 2,
                j1 = SIMPLEX[c | 1] >>> 2,
                k1 = SIMPLEX[c | 2] >>> 2,
                l1 = SIMPLEX[c | 3] >>> 2,
                i2 = SIMPLEX[c] >>> 1 & 1,
                j2 = SIMPLEX[c | 1] >>> 1 & 1,
                k2 = SIMPLEX[c | 2] >>> 1 & 1,
                l2 = SIMPLEX[c | 3] >>> 1 & 1,
                i3 = SIMPLEX[c] & 1,
                j3 = SIMPLEX[c | 1] & 1,
                k3 = SIMPLEX[c | 2] & 1,
                l3 = SIMPLEX[c | 3] & 1;
        final float x1 = x0 - i1 + G4,
                y1 = y0 - j1 + G4,
                z1 = z0 - k1 + G4,
                w1 = w0 - l1 + G4,
                x2 = x0 - i2 + 2f * G4,
                y2 = y0 - j2 + 2f * G4,
                z2 = z0 - k2 + 2f * G4,
                w2 = w0 - l2 + 2f * G4,
                x3 = x0 - i3 + 3f * G4,
                y3 = y0 - j3 + 3f * G4,
                z3 = z0 - k3 + 3f * G4,
                w3 = w0 - l3 + 3f * G4,
                x4 = x0 - 1f + 4f * G4,
                y4 = y0 - 1f + 4f * G4,
                z4 = z0 - 1f + 4f * G4,
                w4 = w0 - 1f + 4f * G4;
        final int h0 = hash(i, j, k, l, seed) << 2,
                h1 = hash(i + i1, j + j1, k + k1, l + l1, seed) << 2,
                h2 = hash(i + i2, j + j2, k + k2, l + l2, seed) << 2,
                h3 = hash(i + i3, j + j3, k + k3, l + l3, seed) << 2,
                h4 = hash(i + 1, j + 1, k + 1, l + 1, seed) << 2;
        float t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            t0 *= t0;
            n0 = t0 * t0 * (x0 * gradient4DLUT[h0] + y0 * gradient4DLUT[h0 | 1] + z0 * gradient4DLUT[h0 | 2] + w0 * gradient4DLUT[h0 | 3]);
        }
        else n0 = 0f;
        float t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            t1 *= t1;
            n1 = t1 * t1 * (x1 * gradient4DLUT[h1] + y1 * gradient4DLUT[h1 | 1] + z1 * gradient4DLUT[h1 | 2] + w1 * gradient4DLUT[h1 | 3]);
        }
        else n1 = 0f;
        float t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            t2 *= t2;
            n2 = t2 * t2 * (x2 * gradient4DLUT[h2] + y2 * gradient4DLUT[h2 | 1] + z2 * gradient4DLUT[h2 | 2] + w2 * gradient4DLUT[h2 | 3]);
        }
        else n2 = 0f;
        float t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            t3 *= t3;
            n3 = t3 * t3 * (x3 * gradient4DLUT[h3] + y3 * gradient4DLUT[h3 | 1] + z3 * gradient4DLUT[h3 | 2] + w3 * gradient4DLUT[h3 | 3]);
        }
        else n3 = 0f;
        float t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            t4 *= t4;
            n4 = t4 * t4 * (x4 * gradient4DLUT[h4] + y4 * gradient4DLUT[h4 | 1] + z4 * gradient4DLUT[h4 | 2] + w4 * gradient4DLUT[h4 | 3]);
        }
        else n4 = 0f;
        t0 = NumberTools.bounce(5.0f + 41.0f * (n0 + n1 + n2 - n3 - n4)) * 0.5f + 0.5f;
        t1 = NumberTools.bounce(5.0f + 41.0f * (n3 + n4 + n0 - n1 - n2)) * 0.5f + 0.5f;
        t2 = NumberTools.bounce(5.0f + 41.0f * (n1 + n2 + n3 - n4 - n0)) * 0.5f + 0.5f;
        return SColor.floatGetHSV(t0, t1, t2, 1f);
    }

    public static float colorNoise(final float x, final float y, final float z, final float w, final float u, final float v, final int seed) {

        final float s = (x + y + z + w + u + v) * F6;

        final int skewX = fastFloor(x + s), skewY = fastFloor(y + s), skewZ = fastFloor(z + s),
                skewW = fastFloor(w + s), skewU = fastFloor(u + s), skewV = fastFloor(v + s);
        final float[] m = new float[6], gradient6DLUT = SeededNoise.gradient6DLUT;
        final int[] distOrder = new int[6],
                intLoc = {skewX, skewY, skewZ, skewW, skewU, skewV};

        final float unskew = (skewX + skewY + skewZ + skewW + skewU + skewV) * G6;
        final float[] cellDist = {
                x - skewX + unskew,
                y - skewY + unskew,
                z - skewZ + unskew,
                w - skewW + unskew,
                u - skewU + unskew,
                v - skewV + unskew
        };

        int o0 = (cellDist[0]<cellDist[1]?1:0)+(cellDist[0]<cellDist[2]?1:0)+(cellDist[0]<cellDist[3]?1:0)+(cellDist[0]<cellDist[4]?1:0)+(cellDist[0]<cellDist[5]?1:0);
        int o1 = (cellDist[1]<=cellDist[0]?1:0)+(cellDist[1]<cellDist[2]?1:0)+(cellDist[1]<cellDist[3]?1:0)+(cellDist[1]<cellDist[4]?1:0)+(cellDist[1]<cellDist[5]?1:0);
        int o2 = (cellDist[2]<=cellDist[0]?1:0)+(cellDist[2]<=cellDist[1]?1:0)+(cellDist[2]<cellDist[3]?1:0)+(cellDist[2]<cellDist[4]?1:0)+(cellDist[2]<cellDist[5]?1:0);
        int o3 = (cellDist[3]<=cellDist[0]?1:0)+(cellDist[3]<=cellDist[1]?1:0)+(cellDist[3]<=cellDist[2]?1:0)+(cellDist[3]<cellDist[4]?1:0)+(cellDist[3]<cellDist[5]?1:0);
        int o4 = (cellDist[4]<=cellDist[0]?1:0)+(cellDist[4]<=cellDist[1]?1:0)+(cellDist[4]<=cellDist[2]?1:0)+(cellDist[4]<=cellDist[3]?1:0)+(cellDist[4]<cellDist[5]?1:0);
        int o5 = 15-(o0+o1+o2+o3+o4);

        distOrder[o0]=0;
        distOrder[o1]=1;
        distOrder[o2]=2;
        distOrder[o3]=3;
        distOrder[o4]=4;
        distOrder[o5]=5;

        float[] n = new float[7];
        float skewOffset = 0f;

        for (int c = -1; c < 6; c++) {
            if (c != -1) intLoc[distOrder[c]]++;

            m[0] = cellDist[0] - (intLoc[0] - skewX) + skewOffset;
            m[1] = cellDist[1] - (intLoc[1] - skewY) + skewOffset;
            m[2] = cellDist[2] - (intLoc[2] - skewZ) + skewOffset;
            m[3] = cellDist[3] - (intLoc[3] - skewW) + skewOffset;
            m[4] = cellDist[4] - (intLoc[4] - skewU) + skewOffset;
            m[5] = cellDist[5] - (intLoc[5] - skewV) + skewOffset;

            float tc = LIMIT6;

            for (int d = 0; d < 6; d++) {
                tc -= m[d] * m[d];
            }

            if (tc > 0f) {
                final int h = hash(intLoc[0], intLoc[1], intLoc[2], intLoc[3],
                        intLoc[4], intLoc[5], seed) * 6;
                float gr = 0f;
                for (int d = 0; d < 6; d++) {
                    gr += gradient6DLUT[h + d] * m[d];
                }

                n[c+1] = gr * tc * tc * tc * tc;
            }
            skewOffset += G6;
        }
        //return NumberTools.bounce(5.0 + 13.5 * n);
        m[0] = NumberTools.bounce(10.0f + 16.25f * (n[0] + n[1] + n[2] + n[3] - n[4] - n[5] - n[6])) * 0.5f + 0.5f;
        m[1] = NumberTools.bounce(10.0f + 16.25f * (n[2] + n[3] + n[4] + n[5] - n[6] - n[0] - n[1])) * 0.5f + 0.5f;
        m[2] = NumberTools.bounce(10.0f + 16.25f * (n[4] + n[5] + n[6] + n[0] - n[1] - n[2] - n[3])) * 0.5f + 0.5f;

        return SColor.floatGetHSV(m[0], m[1], m[2], 1f);
    }
}
