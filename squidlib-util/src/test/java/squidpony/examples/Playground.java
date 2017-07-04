package squidpony.examples;

import squidpony.squidmath.FlapRNG;
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

    private static float carp(final float x)
    {
        return x * (x * (x - 1) + (1 - x) * (1 - x));
    }
    private static float carp2(final float x) { return x - x * (x * (x - 1) + (1 - x) * (1 - x)); }
    private static float carpMid(final float x) { return carp2(x * 0.5f + 0.5f) * 2f - 1f; }
    private void go() {
        FlapRNG rng = new FlapRNG(0x31337, 0xDEADBEEF);
        for (int i = 0; i < 50; i++) {
            System.out.println(NumberTools.formCurvedFloat(rng.nextInt(), rng.nextInt()));
        }
    }

}
