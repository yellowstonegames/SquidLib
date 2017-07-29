package squidpony.examples;

import squidpony.StringKit;
import squidpony.squidmath.GreasedRegion;

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
        int
                a1b1 = GreasedRegion.interleaveBits(1, 1),
                a2b1 = GreasedRegion.interleaveBits(2, 1),
                a1b2 = GreasedRegion.interleaveBits(1, 2),
                a2b2 = GreasedRegion.interleaveBits(2, 2),
                a65535b0 = GreasedRegion.interleaveBits(65535, 0),
                a0b65535 = GreasedRegion.interleaveBits(0, 65535);
        System.out.println("Interleaving...");
        System.out.println("a1b1          : " + StringKit.bin(a1b1));
        System.out.println("a2b1          : " + StringKit.bin(a2b1));
        System.out.println("a1b2          : " + StringKit.bin(a1b2));
        System.out.println("a2b2          : " + StringKit.bin(a2b2));
        System.out.println("a65535b0      : " + StringKit.bin(a65535b0));
        System.out.println("a0b65535      : " + StringKit.bin(a0b65535));
        System.out.println("Dispersing...");
        System.out.println("a1b1          : " + StringKit.bin(GreasedRegion.disperseBits(a1b1)));
        System.out.println("a2b1          : " + StringKit.bin(GreasedRegion.disperseBits(a2b1)));
        System.out.println("a1b2          : " + StringKit.bin(GreasedRegion.disperseBits(a1b2)));
        System.out.println("a2b2          : " + StringKit.bin(GreasedRegion.disperseBits(a2b2)));
        System.out.println("a65535b0      : " + StringKit.bin(GreasedRegion.disperseBits(a65535b0)));
        System.out.println("a0b65535      : " + StringKit.bin(GreasedRegion.disperseBits(a0b65535)));

    }

}
