package squidpony.examples;

import org.junit.Assert;
import org.junit.Test;
import squidpony.StringKit;
import squidpony.squidmath.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by Tommy Ettinger on 7/7/2017.
 */
public class RNGFeatureTest {
    public static ThrustAltRNG tar = new ThrustAltRNG(CrossHash.hash64("Testing all the while..."));
    public static RNG rng = new RNG(tar);
    public static CriticalRNG crng = new CriticalRNG(0x123456789ABCDEF0L);
    public static final boolean PRINTING = false;
    @Test
    public void testUniqueCells(){
        int width = 21, height = 16;
        Coord[] unique = rng.getRandomUniqueCells(0, 0, width, height, width * height / 4);
        OrderedSet<Coord> set = new OrderedSet<>(unique);
        int ctr = 0;
        for (Coord coord : unique) {
            assertTrue("Non-unique Coord: " + coord, coord.isWithin(width, height));
            if(PRINTING) {
                System.out.printf("(%02d,%02d)", coord.x, coord.y);
                if (++ctr == width) {
                    System.out.println();
                    ctr = 0;
                }
            }
        }
        // using a Set to test for uniqueness
        assertTrue(set.size() == width * height / 4);
    }
    @Test
    public void testCriticalHits()
    {
        double total;
        int c;
        for(float lc : new float[]{0f, 0.25f, -0.25f, 0.5f, -0.5f, 0.8f, -0.8f, 1f, -1f, 1.5f, -1.5f})
        {
            total = 0.0;
            crng.luck = lc;
            if(PRINTING) System.out.println("Luck is " + lc);
            for (int j = 0; j < 32; j++) {
                c = crng.nextInt(20);
                if(PRINTING) System.out.print(c + " ");
                total += c * 0x1p-5;
            }
            if(PRINTING) System.out.println("\n" + total + "\n");
        }
    }
    @Test
    public void testHasty() {
        for (int i = 0; i < 100; i++) {
            System.out.println(StringKit.hex(rng.nextIntHasty(0x80000000)));
        }
    }
    @Test
    public void testOrdering() {
        tar.setState(111L);
        int[] ord1 = rng.randomOrdering(20);
        tar.setState(111L);

        int[] ord2 = new int[20];
        rng.randomOrdering(20, ord2);
        if (PRINTING) {
            System.out.println(StringKit.join(", ", ord1));
            System.out.println(StringKit.join(", ", ord2));
        }
        Assert.assertArrayEquals(ord1, ord2);
    }
}
