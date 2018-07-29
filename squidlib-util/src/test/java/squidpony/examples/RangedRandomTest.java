package squidpony.examples;

import org.junit.Test;
import squidpony.squidmath.LinnormRNG;

/**
 * Created by Tommy Ettinger on 7/28/2018.
 */
public class RangedRandomTest {
    public static final long BOUND = 10000000000001L;
    public LinnormRNG r0 = new LinnormRNG(1234567890L),
            r1 = new LinnormRNG(1234567890L),
            r2 = new LinnormRNG(1234567890L);
    public long mult128(long bound)
    {
        long rand = r0.nextLong();
        if (bound <= 0) return 0;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>>= 32;
        final long z = (randLow * boundLow >>> 32);
        long t = rand * boundLow + z;
        final long tLow = t & 0xFFFFFFFFL;
        t >>>= 32;
        return rand * bound + t + (tLow + randLow * bound >> 32);
    }
    public long bitmask(long bound)
    {
        final long mask = -1L >>> Long.numberOfLeadingZeros(--bound|1L);
        long x;
        do {
            x = r2.nextLong() & mask;
        } while (x > bound);
        return x;
    }
    public long mult128Signed(long bound)
    {
        return r1.nextLong(bound);
    }
    @Test
    public void testMult128()
    {
        long total = 0;
        for (int i = 0; i < 1000000; i++) {
            total += mult128(BOUND);
        }
        System.out.println("Mult128");
        System.out.printf("total: %08d, average: %3.12f\n", total, total / 1000000.0);
    }
    @Test
    public void testMult128Signed()
    {
        long total = 0;
        for (int i = 0; i < 1000000; i++) {
            total += mult128Signed(BOUND);
        }
        System.out.println("Mult128Signed");
        System.out.printf("total: %08d, average: %3.12f\n", total, total / 1000000.0);
    }
    @Test
    public void testBitmask()
    {
        long total = 0;
        for (int i = 0; i < 1000000; i++) {
            total += bitmask(BOUND);
        }
        System.out.println("Bitmask");
        System.out.printf("total: %08d, average: %3.12f\n", total, total / 1000000.0);
    }
}
