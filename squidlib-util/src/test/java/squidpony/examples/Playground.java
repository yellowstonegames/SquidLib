package squidpony.examples;

import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
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

    private void go() {
        DungeonUtility.debugPrint(new GreasedRegion(20, 20).insertSeveral(
                Radius.CIRCLE.perimeter(Coord.get(10, 10), 6, false, 20, 20)).toChars());
    }

}
