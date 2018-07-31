package squidpony.examples;

import org.junit.Test;
import squidpony.squidmath.LinnormRNG;

import java.math.BigInteger;

/**
 * Created by Tommy Ettinger on 7/28/2018.
 */
public class RangedRandomTest {
    public static final long BOUND = 0x7890ABCDEF123456L;
//    public LinnormRNG r0 = new LinnormRNG(1234567890L),
//            r1 = new LinnormRNG(1234567890L),
//            r2 = new LinnormRNG(1234567890L);

    public LinnormRNG r0 = new LinnormRNG(-1L),
        r1 = new LinnormRNG(-1L),
        r2 = new LinnormRNG(-1L);
    public long mult128(long bound)
    {
        long rand = r0.nextLong();
        if (bound <= 0) return 0;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
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
        return r1.nextLong(-bound);
    }
    @Test
    public void testMult128()
    {
        System.out.println("Mult128");
        double total = 0, optimal = 0.0;
        for (int i = 0; i < 0x1000000; i++) {
            total += mult128(BOUND) * 0x1p-24;
            optimal += (BOUND - 1L)  * 0x1p-25;
        }
        System.out.printf("average:  %1.16a, optimal  %1.16a, difference %02.12f\n", total, optimal, total / optimal);
    }
    @Test
    public void testMult128Signed()
    {
        System.out.println("Mult128Signed");
        double total = 0, optimal = 0.0;
        for (int i = 0; i < 0x1000000; i++) {
            total += mult128Signed(BOUND) * 0x1p-24;
            optimal -= (BOUND - 1L) * 0x1p-25;
        }
        System.out.printf("average: %1.16a, optimal %1.16a, difference %02.12f\n", total, optimal, total / optimal);
    }
    @Test
    public void testBitmask()
    {
        System.out.println("Bitmask");
        double total = 0, optimal = 0.0;
        for (int i = 0; i < 0x1000000; i++) {
            total += bitmask(BOUND) * 0x1p-24;
            optimal += (BOUND - 1L)  * 0x1p-25;
        }
        System.out.printf("average:  %1.16a, optimal  %1.16a, difference %02.12f\n", total, optimal, total / optimal);
    }
    public static long multiplyHigh(long left, long right) {
        final long leftLow = left & 0xFFFFFFFFL;
        final long rightLow = right & 0xFFFFFFFFL;
        left >>= 32;
        right >>= 32;
        final long z = (leftLow * rightLow >> 32);
        final long t = left * rightLow + z;
        return left * right + (t >> 32) + ((t & 0xFFFFFFFFL) + leftLow * right >> 32) - (z >> 63);
    }
    @Test
    public void testMultiplication()
    {
        for (long l = -0x10000000000L; l <= 0x10000000000L; l += 0x100000001L) {
            for (long r = -0x10000000000L; r <= 0x10000000000L; r += 0x100000001L) {
                if(multiplyHigh(l, r) != BigInteger.valueOf(l).multiply(BigInteger.valueOf(r)).shiftRight(64).longValue())
                    System.out.println(l + " " + r);
            }
        }
    }
}
