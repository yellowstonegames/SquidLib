package squidpony.squidmath;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import squidpony.examples.TestConfiguration;

import static squidpony.squidmath.Noise.fastFloor;
import static squidpony.squidmath.SeededNoise.*;

/**
 * Created by Tommy Ettinger on 5/9/2020.
 */
public class NoiseRangeTest {
    @Test
    @Ignore
    public void testOldSeeded2D() {
        final double threshold = 0.75;
        double min0 = 0.0, max0 = 0.0, min1 = 0.0, max1 = 0.0, min2 = 0.0, max2 = 0.0;
        for (double x = 0; x <= 16; x += 0x1.0001p-6) {
            for (double y = 0; y <= 16; y += 0x1.0001p-6) {
                final double s = (x + y) * F2;
                final int i = fastFloor(x + s),
                        j = fastFloor(y + s);
                final double t = (i + j) * G2,
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
                final double
                        x1 = x0 - i1 + G2,
                        y1 = y0 - j1 + G2,
                        x2 = x0 - 1 + 2 * G2,
                        y2 = y0 - 1 + 2 * G2;
                double n;
                for (int gi0 = 0; gi0 < 256; gi0++) {
                    double t0 = threshold - x0 * x0 - y0 * y0;
                    if (t0 > 0) {
                        t0 *= t0;
                        n = t0 * t0 * (grad2d[gi0][0] * x0 + grad2d[gi0][1] * y0);

                        min0 = Math.min(n, min0);
                        max0 = Math.max(n, max0);
                    }
                }

                for (int gi1 = 0; gi1 < 256; gi1++) {
                    double t1 = threshold - x1 * x1 - y1 * y1;
                    if (t1 > 0) {
                        t1 *= t1;
                        n = t1 * t1 * (grad2d[gi1][0] * x1 + grad2d[gi1][1] * y1);

                        min1 = Math.min(n, min1);
                        max1 = Math.max(n, max1);
                    }
                }

                for (int gi2 = 0; gi2 < 256; gi2++) {
                    double t2 = threshold - x2 * x2 - y2 * y2;
                    if (t2 > 0)  {
                        t2 *= t2;
                        n = t2 * t2 * (grad2d[gi2][0] * x2 + grad2d[gi2][1] * y2);

                        min2 = Math.min(n, min2);
                        max2 = Math.max(n, max2);
                    }
                }
            }
        }
        TestConfiguration.println("Min: " + (min0+min1+min2)*9.125);
        TestConfiguration.println("Max: " + (max0+max1+max2)*9.125);
    }

    @Test
    public void testSeeded2D() {
        double min = 0.0, max = 0.0;
        long seed = DiverRNG.randomize(System.nanoTime());
        for (int i = 0; i < 10; i++) {
            seed = DiverRNG.randomize(seed + System.nanoTime());
            for (double x = 0; x <= 32; x += 0x1.0001p-6) {
                for (double y = 0; y <= 32; y += 0x1.0001p-6) {
                    double n = SeededNoise.noise(x * 123.4567, y * 123.4567, seed);
                    min = Math.min(min, n);
                    max = Math.max(max, n);
                }
            }
        }
//        System.out.println("Min: " + min);
//        System.out.println("Max: " + max);
        TestConfiguration.println("Min: " + min);
        TestConfiguration.println("Max: " + max);
        Assert.assertTrue("min is out of bounds", min >= -1.0 && min <= 1.0);
        Assert.assertTrue("max is out of bounds", max >= -1.0 && max <= 1.0);
    }

}
