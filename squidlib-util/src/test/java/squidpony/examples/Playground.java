package squidpony.examples;

import java.util.HashSet;

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
//        final double WEYL = Math.cbrt(Math.PI + Math.E - 1.111);//Math.pow(0.5 * Math.sqrt(7.0) + Math.PI, 1.0 / Math.E);
//        double w = WEYL + 1.2357;
//        for (int i = 0; i < 50; i++) {
//            System.out.println((w = w * WEYL % 1.0));
//        }
        for (int i = 0; i < 50; i++) {
            System.out.println(determine2(i) + ", " + determine2_scrambled(i));
        }
        HashSet<Double> doubles = new HashSet<>(65536);
        for (int i = 0; i < 65536; i++) {
            doubles.add(determine2_scrambled(i));
        }
        System.out.println(doubles.size());
    }

}
