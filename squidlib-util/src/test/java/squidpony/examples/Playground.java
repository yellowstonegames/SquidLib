package squidpony.examples;

import squidpony.squidmath.IntDoubleOrderedMap;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.VanDerCorputQRNG;

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

    private void go() {
        IntDoubleOrderedMap map = new IntDoubleOrderedMap(256);
        double d;
        for (int i = 0; i < 4096; i++) {
            System.out.println(d = VanDerCorputQRNG.determineMixed(i));
            if(map.put((int) (d * 4096 * 64), 1.0) != 0.0)
                System.out.println(i + ": " + d);
        }

        System.out.println("\nCollisions: " + (4096 - map.size()));
        map.clear();
        for (int i = 0; i < 4096; i++) {
            //System.out.println(d = NumberTools.randomFloat((i ^ 0xD0E89D2D) >>> 19 | (i ^ 0xD0E89D2D) << 13));
            System.out.println(d = VanDerCorputQRNG.weakDetermine(i));
            if(map.put((int) (d * 4096 * 64), 1.0) != 0.0)
                System.out.println(i + ": " + d);
        }

        System.out.println("\nCollisions: " + (4096 - map.size()));

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
