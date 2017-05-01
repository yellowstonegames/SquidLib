package squidpony.examples;

import squidpony.StringKit;
import squidpony.squidmath.CoordPacker;
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

    private void go() {
        for (byte i = 0; i < 64; i++) {
            System.out.printf("%02d (%s): %s\n", i, StringKit.bin(i), CoordPacker.mortonDecode3D(i).toString());

        }
    }

}
