package squidpony.examples;

import squidpony.StringKit;
import squidpony.squidmath.RNG;

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

    public static double determine2_scrambled(int index)
    {
        int s = ((++index ^ index << 1 ^ index >> 1) & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (double)(1 << (32 - leading));
    }

    private void go() {
        RNG rng = new RNG(1L), rn2 = new RNG(1L), rn3 = new RNG(1L), rn4 = new RNG(1L);
        for (int i = 0; i < 40; i++) {
            System.out.println(StringKit.join(",", rng.shuffle(new Integer[]{0, 1, 2, 3})) + "   " +
                    StringKit.join(",", rn2.randomOrdering(4)) + "   " +
                    StringKit.join(",", rn3.shuffleInPlace(new Integer[]{0, 1, 2, 3})) + "   " +
                    StringKit.join(",", rn4.shuffle(new Integer[]{0, 1, 2, 3}, new Integer[4])));
        }
    }

}
