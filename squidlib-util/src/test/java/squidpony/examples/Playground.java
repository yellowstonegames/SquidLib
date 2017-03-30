package squidpony.examples;

import squidpony.StringKit;
import squidpony.squidmath.PintRNG;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    public static float floatPart(final int bits)
    {
        return ((bits & 0x40000000) == 0
                        ? (((bits >> 29 & 1) == 1 ? 1 : 0x1.0p-64f) * ((bits >> 28 & 1) == 1 ? 1 : 0x1.0p-32f) * ((bits >> 27 & 1) == 1 ? 1 : 0x1.0p-16f) * ((bits >> 26 & 1) == 1 ? 1 : 0x1.0p-8f) * ((bits >> 25 & 1) == 1 ? 1 : 0x1.0p-4f) * 0x1.0p-2f * ((bits >> 23 & 1) == 1 ? 1 : 0x1.0p-1f))
                        : (((bits >> 29 & 1) == 1 ? 0x1.0p64f : 1) * ((bits >> 28 & 1) == 1 ? 0x1.0p32f : 1) * ((bits >> 27 & 1) == 1 ? 0x1.0p16f : 1) * ((bits >> 26 & 1) == 1 ? 0x1.0p8f : 1) * ((bits >> 25 & 1) == 1 ? 0x1.0p4f : 1) * 0x1.0p2f * ((bits >> 23 & 1) == 1 ? 1 : 0x1.0p-1f))
                );
    }

    /**
     * Get an index from the exponent part of the int {@code n} representing a float with
     * {@code ((n >> 24 & 0x7e) | (n >> 23 & 1))}
     */
    private static final float[] multipliers = {
            0x0.8p-126f, 0x1.0p-126f, 0x1.0p-123f, 0x1.0p-122f, 0x1.0p-119f, 0x1.0p-118f, 0x1.0p-115f, 0x1.0p-114f,
            0x1.0p-111f, 0x1.0p-110f, 0x1.0p-107f, 0x1.0p-106f, 0x1.0p-103f, 0x1.0p-102f, 0x1.0p-99f, 0x1.0p-98f,
            0x1.0p-95f, 0x1.0p-94f, 0x1.0p-91f, 0x1.0p-90f, 0x1.0p-87f, 0x1.0p-86f, 0x1.0p-83f, 0x1.0p-82f,
            0x1.0p-79f, 0x1.0p-78f, 0x1.0p-75f, 0x1.0p-74f, 0x1.0p-71f, 0x1.0p-70f, 0x1.0p-67f, 0x1.0p-66f,
            0x1.0p-63f, 0x1.0p-62f, 0x1.0p-59f, 0x1.0p-58f, 0x1.0p-55f, 0x1.0p-54f, 0x1.0p-51f, 0x1.0p-50f,
            0x1.0p-47f, 0x1.0p-46f, 0x1.0p-43f, 0x1.0p-42f, 0x1.0p-39f, 0x1.0p-38f, 0x1.0p-35f, 0x1.0p-34f,
            0x1.0p-31f, 0x1.0p-30f, 0x1.0p-27f, 0x1.0p-26f, 0x1.0p-23f, 0x1.0p-22f, 0x1.0p-19f, 0x1.0p-18f,
            0x1.0p-15f, 0x1.0p-14f, 0x1.0p-11f, 0x1.0p-10f, 0x1.0p-7f, 0x1.0p-6f, 0x1.0p-3f, 0x1.0p-2f,
            0x1.0p1f, 0x1.0p2f, 0x1.0p5f, 0x1.0p6f, 0x1.0p9f, 0x1.0p10f, 0x1.0p13f, 0x1.0p14f,
            0x1.0p17f, 0x1.0p18f, 0x1.0p21f, 0x1.0p22f, 0x1.0p25f, 0x1.0p26f, 0x1.0p29f, 0x1.0p30f,
            0x1.0p33f, 0x1.0p34f, 0x1.0p37f, 0x1.0p38f, 0x1.0p41f, 0x1.0p42f, 0x1.0p45f, 0x1.0p46f,
            0x1.0p49f, 0x1.0p50f, 0x1.0p53f, 0x1.0p54f, 0x1.0p57f, 0x1.0p58f, 0x1.0p61f, 0x1.0p62f,
            0x1.0p65f, 0x1.0p66f, 0x1.0p69f, 0x1.0p70f, 0x1.0p73f, 0x1.0p74f, 0x1.0p77f, 0x1.0p78f,
            0x1.0p81f, 0x1.0p82f, 0x1.0p85f, 0x1.0p86f, 0x1.0p89f, 0x1.0p90f, 0x1.0p93f, 0x1.0p94f,
            0x1.0p97f, 0x1.0p98f, 0x1.0p101f, 0x1.0p102f, 0x1.0p105f, 0x1.0p106f, 0x1.0p109f, 0x1.0p110f,
            0x1.0p113f, 0x1.0p114f, 0x1.0p117f, 0x1.0p118f, 0x1.0p121f, 0x1.0p122f, 0x1.0p125f, 0x1.0p126f};
    private void go() {

        int seed2 = 0xBEEFDEED;
        System.out.println("0x" + StringKit.hex(seed2));
        System.out.println("0x" + StringKit.hex(seed2 = PintRNG.determine(seed2)));
        System.out.println("0x" + StringKit.hex(seed2 = PintRNG.determine(seed2)));
        System.out.println("0x" + StringKit.hex(seed2 = PintRNG.determine(seed2)));
        System.out.println("0x" + StringKit.hex(seed2 = PintRNG.determine(seed2)));
        System.out.println("0x" + StringKit.hex(seed2 = PintRNG.determine(seed2)));
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
