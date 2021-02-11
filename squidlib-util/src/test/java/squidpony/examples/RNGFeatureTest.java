package squidpony.examples;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import squidpony.StringKit;
import squidpony.squidmath.*;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static squidpony.examples.TestConfiguration.PRINTING;

/**
 * Created by Tommy Ettinger on 7/7/2017.
 */
public class RNGFeatureTest {
    public static ThrustAltRNG tar = new ThrustAltRNG(CrossHash.hash64("Testing all the while..."));
    public static RNG rng = new RNG(tar);
    public static CriticalRNG crng = new CriticalRNG(0x123456789ABCDEF0L);
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
        assertEquals(set.size(), width * height / 4);
    }
    @Test
    @Ignore
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
    @Ignore
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
        TestConfiguration.println(StringKit.join(", ", ord1));
        TestConfiguration.println(StringKit.join(", ", ord2));
        Assert.assertArrayEquals(ord1, ord2);
    }

    @Test
    public void testAbstractNextLong() {
        SpecifiedRandomness r = new SpecifiedRandomness(0L, Long.MAX_VALUE, Long.MIN_VALUE, -1L,
                0xFFFFFFFF7FFFFFFFL, 0xFFFFFFFF00000000L, 0x00000000FFFFFFFFL, 0x80000000L);
        AbstractRNG arng = new AbstractRNG() {
            @Override
            public int next(int bits) {
                return r.next(bits);
            }

            @Override
            public int nextInt() {
                return r.next(32);
            }

            @Override
            public long nextLong() {
                return r.nextLong();
            }

            @Override
            public boolean nextBoolean() {
                return r.nextLong() < 0L;
            }

            @Override
            public double nextDouble() {
                return (r.nextLong() >>> 11) * 0x1p-53;
            }

            @Override
            public float nextFloat() {
                return r.next(24) * 0x1p-24f;
            }

            @Override
            public IRNG copy() {
                return new StatefulRNG(new SpecifiedRandomness(r));
            }

            @Override
            public Serializable toSerializable() {
                return this;
            }
        };
        IRNG srng = arng.copy();
        for (int i = 0; i < 8; i++) {
            long a = arng.between(Integer.MIN_VALUE, Long.MAX_VALUE);
//            System.out.printf("a: %016X\n", a);
            Assert.assertTrue("AbstractRNG went out-of-bounds: on " + i + ", a was " + a, a >= Integer.MIN_VALUE && a < Long.MAX_VALUE);
            long s = srng.between(Integer.MIN_VALUE, Long.MAX_VALUE);
//            System.out.printf("s: %016X\n", s);
            Assert.assertTrue("StatefulRNG incorrectly went out-of-bounds", s >= Integer.MIN_VALUE && s < Long.MAX_VALUE);
        }
        for (int i = 0; i < 8; i++) {
            long a = arng.between(Long.MAX_VALUE, Integer.MIN_VALUE);
//            System.out.printf("a: %016X\n", a);
            Assert.assertTrue("AbstractRNG went out-of-bounds: on " + i + ", a was " + a, a > Integer.MIN_VALUE);
            long s = srng.between(Long.MAX_VALUE, Integer.MIN_VALUE);
//            System.out.printf("s: %016X\n", s);
            Assert.assertTrue("StatefulRNG incorrectly went out-of-bounds", s > Integer.MIN_VALUE);
        }
//        System.out.println(arng.between(0L, -2L));
//        System.out.println(arng.between(-2L, 0L));
    }
}
