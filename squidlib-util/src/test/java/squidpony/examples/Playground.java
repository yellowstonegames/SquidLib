package squidpony.examples;

import squidpony.squidmath.NumberTools;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    public static double weakDetermine(long index)
    {
        return NumberTools.longBitsToDouble(0x3ff0000000000000L | ((index<<1|1) * 0x9E3779B97F4A7C15L * ~index
                - ((index ^ ~(index * 11L)) * 0x632BE59BD9B4E019L)) >>> 12) - 1.0;
        //return NumberTools.setExponent(
        //        (NumberTools.setExponent((index<<1|1) * 0.618033988749895, 0x3ff))
        //                * (0x232BE5 * (~index)), 0x3ff) - 1.0;

    }

    private static float carp(final float x)
    {
        return x * (x * (x - 1) + (1 - x) * (1 - x));
    }
    private static float carp2(final float x) { return x * -(x * (x - 1) + (1 - x) * (1 - x)) + x; }
    private void go() {
        for (float x = 0; x <= 1f; x += 0.0625f * 0.0625f) {
            System.out.println(x + ": " + carp2(x));
        }
    }

}
