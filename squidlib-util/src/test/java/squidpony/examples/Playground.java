package squidpony.examples;

import squidpony.squidmath.NumberTools;
import squidpony.squidmath.SeededNoise;
import squidpony.squidmath.ThrustRNG;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    private static float carp(final float x)
    {
        return x * (x * (x - 1) + (1 - x) * (1 - x));
    }
    private static float carp2(final float x) { return x - x * (x * (x - 1) + (1 - x) * (1 - x)); }
    private static float carpMid(final float x) { return carp2(x * 0.5f + 0.5f) * 2f - 1f; }
    public static double determine2(int index)
    {
        int s = (index+1 & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (double)(1 << (32 - leading));
    }
    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float querp(final float start, final float end, float a){
        return (1f - (a *= a * a * (a * (a * 6f - 15f) + 10f))) * start + a * end;
    }

    public static float tabbyNoise(final float x, final float y, final float z, final int seed) {
        final int
                xf = SeededNoise.fastFloor(x),
                yf = SeededNoise.fastFloor(y),
                zf = SeededNoise.fastFloor(z);
        final long sx = ThrustRNG.determine(seed) | 1, sy = sx + 256, sz = sy + 512;
        final float
                lx = querp(
                        NumberTools.formCurvedFloat(NumberTools.splitMix64(xf * sx + 100)),
                        NumberTools.formCurvedFloat(NumberTools.splitMix64((xf+1) * sx + 100)),
                        x - xf),
                ly = querp(
                        NumberTools.formCurvedFloat(NumberTools.splitMix64(yf * sy + 200)),
                        NumberTools.formCurvedFloat(NumberTools.splitMix64((yf+1) * sy + 200)),
                        y - yf),
                lz = querp(
                        NumberTools.formCurvedFloat(NumberTools.splitMix64(zf * sz + 300)),
                        NumberTools.formCurvedFloat(NumberTools.splitMix64((zf+1) * sz + 300)),
                        z - zf);
        return NumberTools.bounce(20f + (lx + ly + lz) * 1.4f);
    }

    private void go() {
        float value = 2f, power = 3f, min = (float)Math.pow(value, -power), scale = 1 / (1 - min);
        System.out.println("min = " + min + "f, scale = " + scale + "f");
        for (float n = 0f; n <= 1f; n += 0.0625f) {
            if (n <= 0.5f) System.out.println(n + ": " + (1 - ((float)Math.pow(value, -power * (n * 2)) - min) * scale) / 2);
            else System.out.println(n + ": " + (1 + (float)Math.pow(value, power * (n * 2 - 2)) - min * 2) * scale / 2);

            //System.out.println(n + ": " + querp(0f, 1f, n));
        }
//        for (float y = 0f; y < 1.1f; y += 0.0625f) {
//            for (float x = 0f; x < 1.1f; x+= 0.0625f) {
//                System.out.printf("%04X ", (int)(tabbyNoise(x, y, 1f, 10) * 0x7FFF + 0x8000));
//            }
//            System.out.println();
//        }
    }

}
