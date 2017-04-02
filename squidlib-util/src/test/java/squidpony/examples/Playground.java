package squidpony.examples;

import squidpony.squidmath.IntDoubleOrderedMap;
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

    public static double weakDetermine(int index)
    {
        return NumberTools.setExponent(
                (NumberTools.setExponent((index<<1|1) * 0.618033988749895, 0x3ff))
                        * (0x232BE5 * (~index)), 0x3ff) - 1.0;
    }


    /**
     * Get an index from the exponent part of the int {@code n} representing a float with
     * {@code ((n >> 24 & 0x7e) | (n >> 23 & 1))}
     */
    private void go() {
        IntDoubleOrderedMap map = new IntDoubleOrderedMap(256);
        double d;
        for (int i = 0; i < 4096; i++) {
            System.out.println(d = weakDetermine(i));
            if(map.put((int) (d * 4096 * 16), 1.0) != 0.0)
                System.out.println(i + ": " + d);
        }

        System.out.println("\nFinal Map Size: " + map.size());

        /*
        for (int i = 0, n = 0; i <128; i++, n+=0x800000) {
            if((n & 0x1000000) != 0)
                n += 0x1000000;
            if(floatPart(n) != multipliers[i])
                System.out.println("!!! " + i + " !!!");
        }
        */
    }

}
