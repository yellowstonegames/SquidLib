package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.utils.NumberUtils;
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
        return NumberUtils.intToFloatColor(0xfe000000 |
                (bounce256((int) ((noise * 1.29 + 1.39) * (0x3DF9f)) >>> 8) << 16) |
                (bounce256((int) ((noise * 1.18 + 1.45) * (0x3EB9f)) >>> 8) << 8) |
                (bounce256((int) ((noise * 1.07 + 1.51) * (0x3E99f)) >>> 8)));
        //return NumberUtils.intToFloatColor(0xfe000000 | ((blu >>> 8) + (red >>> 10) + (grn >>> 10) & 0xff0000) |
        //        ((grn >>> 16) + (red >>> 18) + (blu >>> 18) & 0xff00) | ((red >>> 24) + (blu >>> 26) + (grn >>> 26) & 0xff));
    }

    public static float colorNoise(final double x, final double y, final int seed) {
        return colorNoise(noise(x, y, seed));
    }

    public static float colorNoise(final double x, final double y, final double z, final int seed) {
        return colorNoise(noise(x, y, z, seed));
    }

    public static float colorNoise(final double x, final double y, final double z,  final double w, final int seed) {
        return colorNoise(noise(x, y, z, w, seed));
    }

    public static float colorNoise(final double x, final double y, final double z,  final double w, final double u, final double v, final int seed) {
        return colorNoise(noise(x, y, z, w, u, v, seed));
    }

    /*
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

        final int
                r0 = determineBounded(i + determine(j), 256) << 1                  ,
                r1 = determineBounded(i + i1 + determine(j + j1), 256) << 1  ,
                r2 = determineBounded(i + 1 + determine(j + 1), 256) << 1    ,
                g0 = determineBounded(i + determine(j) + r0, 256) << 1                  ,
                g1 = determineBounded(i + i1 + determine(j + j1) + r1, 256) << 1  ,
                g2 = determineBounded(i + 1 + determine(j + 1) + r2, 256) << 1    ,
                b0 = determineBounded(i + determine(j) + g0, 256) << 1                  ,
                b1 = determineBounded(i + i1 + determine(j + j1) + g1, 256) << 1  ,
                b2 = determineBounded(i + 1 + determine(j + 1) + g2, 256) << 1
        ;
        int rr = 0, gg = 0, bb = 0;
        float rOff = NumberTools.bounce(r0 + r1 + r2 + -160f) * 0.125f + 0.25f,
                gOff = NumberTools.bounce(g0 + g1 + g2 + -160f) * 0.125f + 0.25f,
                bOff = NumberTools.bounce(b0 + b1 + b2 + -160f) * 0.125f + 0.25f,
                tt = 0.5f - x0 * x0 - y0 * y0 + rOff;
        if (tt > 0) {
            tt *= tt * tt * tt;
            rr += tt * Math.abs(x0 * (gradient2DLUT[r0]) + y0 * (gradient2DLUT[r0 | 1])) * 1000;
        }
        tt = 0.5f - x0 * x0 - y0 * y0 + gOff;
        if (tt > 0) {
            tt *= tt * tt * tt;
            gg += tt * Math.abs(x0 * (gradient2DLUT[g0]) + y0 * (gradient2DLUT[g0 | 1])) * 1000;
        }
        tt = 0.5f - x0 * x0 - y0 * y0 + bOff;
        if (tt > 0) {
            tt *= tt * tt * tt;
            bb += tt * Math.abs(x0 * (gradient2DLUT[b0]) + y0 * (gradient2DLUT[b0 | 1])) * 1000;
        }
        tt = 0.5f - x1 * x1 - y1 * y1 + rOff;
        if (tt > 0) {
            tt *= tt * tt * tt;
            rr += tt * Math.abs(x1 * (gradient2DLUT[r1]) + y1 * (gradient2DLUT[r1 | 1])) * 1000;
        }
        tt = 0.5f - x1 * x1 - y1 * y1 + gOff;
        if (tt > 0) {
            tt *= tt * tt * tt;
            gg += tt * Math.abs(x1 * (gradient2DLUT[g1]) + y1 * (gradient2DLUT[g1 | 1])) * 1000;
        }
        tt = 0.5f - x1 * x1 - y1 * y1 + bOff;
        if (tt > 0) {
            tt *= tt * tt * tt;
            bb += tt * Math.abs(x1 * (gradient2DLUT[b1]) + y1 * (gradient2DLUT[b1 | 1])) * 1000;
        }
        tt = 0.5f - x2 * x2 - y2 * y2 + rOff;
        if (tt > 0) {
            tt *= tt * tt * tt;
            rr += tt * Math.abs(x2 * (gradient2DLUT[r2]) + y2 * (gradient2DLUT[r2 | 1])) * 1000;
        }
        tt = 0.5f - x2 * x2 - y2 * y2 + tt;
        if (tt > 0) {
            tt *= tt * tt * tt;
            gg += tt * Math.abs(x2 * (gradient2DLUT[g2]) + y2 * (gradient2DLUT[g2 | 1])) * 1000;
        }
        tt = 0.5f - x2 * x2 - y2 * y2 + tt;
        if (tt > 0) {
            tt *= tt * tt * tt;
            bb += tt * Math.abs(x2 * (gradient2DLUT[b2]) + y2 * (gradient2DLUT[b2 | 1])) * 1000;
        }
        return SColor.floatGetI(bounce256(rr * 5 + gg + bb), bounce256(gg * 5 + rr + bb), bounce256(bb * 5 + rr + gg));

     */
}
