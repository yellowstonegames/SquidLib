package squidpony.examples;

import org.junit.Ignore;
import org.junit.Test;
import squidpony.squidmath.LinnormRNG;

import java.math.BigInteger;

/**
 * Created by Tommy Ettinger on 7/28/2018.
 */
@Ignore
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
        long rand = r1.nextLong();
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
    }
    @Test
    public void testMult128()
    {
        r0.setState(-1L);
        System.out.println("Mult128");
        double total = 0, optimal = 0.0;
        for (int i = 0; i < 0x1000000; i++) {
            total += mult128(BOUND) * 0x1p-24;
            optimal += (BOUND - 1L)  * 0x1p-25;
        }
        System.out.printf("average: %1.16a, optimal %1.16a, difference %+02.12f\n", total, optimal, (total / optimal - 1.0));
    }
    @Test
    public void testMult128Signed()
    {
        r1.setState(-1L);
        System.out.println("Mult128Signed");
        double total = 0.0, optimal = 0.0;
        for (int i = 0; i < 0x1000000; i++) {
            total += mult128Signed(BOUND) * 0x1p-24;
            optimal += (BOUND - 1L) * 0x1p-25;
        }
        System.out.printf("average: %1.16a, optimal %1.16a, difference %+02.12f\n", total, optimal, (total / optimal - 1.0));
    }
    @Test
    public void testBitmask()
    {
        r2.setState(-1L);
        System.out.println("Bitmask");
        double total = 0.0, optimal = 0.0;
        for (int i = 0; i < 0x1000000; i++) {
            total += bitmask(BOUND) * 0x1p-24;
            optimal += (BOUND - 1L)  * 0x1p-25;
        }
        System.out.printf("average: %1.16a, optimal %1.16a, difference %+02.12f\n", total, optimal, (total / optimal - 1.0));
    }
    public static long multiplyHigh(long x1, long y1) {
        final long x2 = x1 & 0xFFFFFFFFL;
        final long y2 = y1 & 0xFFFFFFFFL;
        x1 >>= 32;
        y1 >>= 32;
        final long z = (x2 * y2 >> 32);
        final long t = x1 * y2 + z;
        return x1 * y1 + (t >> 32) + ((t & 0xFFFFFFFFL) + x2 * y1 >> 32) - (z >> 63);
    }
    public static long multiplyHighJDK(long x, long y)
    {
        long x2 = x & 0xFFFFFFFFL;
        long y2 = y & 0xFFFFFFFFL;
        x >>= 32;
        y >>= 32;
        final long t = x * y2 + (x2 * y2 >>> 32);
        return x * y + (t >> 32) + (x2 * y + (t & 0xFFFFFFFFL) >> 32);
    }
    
    public static long multiplyHighUnsigned(long rand, long bound)
    {
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>>= 32;
        final long a = rand * bound;
        final long b = randLow * boundLow;
        return (((b >>> 32) + (rand + randLow) * (bound + boundLow) - a - b) >>> 32) + a;
    }
    
    @Test
    public void testMultiplication()
    {
        for (long l = -0x10000000000L; l <= 0x10000000000L; l += 0x11111111L) {
            for (long r = -0x10000000000L; r <= 0x10000000000L; r += 0x11111111L) {
//                if(multiplyHigh(l, r) != Math.multiplyHigh(l, r))
                if(multiplyHigh(l, r) != BigInteger.valueOf(l).multiply(BigInteger.valueOf(r)).shiftRight(64).longValue())
                    System.out.println(l + " " + r);
            }
        }
    }
    @Test
    public void testMultiplicationJDK()
    {
//        for (long l = -0x10000000000L; l <= 0x10000000000L; l += 0x11111111L) {
//            for (long r = -0x10000000000L; r <= 0x10000000000L; r += 0x11111111L) {
////                if(multiplyHigh(l, r) != Math.multiplyHigh(l, r))
//                if(multiplyHighJDK(l, r) != BigInteger.valueOf(l).multiply(BigInteger.valueOf(r)).shiftRight(64).longValue())
//                    System.out.println(l + " " + r);
//            }
//        }
        System.out.println(multiplyHigh(0xFFFFFFFFL, 0xFFFFFFFFL));
        System.out.printf("%016X\n", (0xFFFFFFFFL * 0xFFFFFFFFL));
        System.out.printf("%016X\n", (0xFFFFFFFFL * 0xFFFFFFFFL >>> 32));
        System.out.printf("%016X\n", (0xFFFFFFFFL * 0xFFFFFFFFL >> 32));
        System.out.println(multiplyHighJDK(0xFFFFFFFFL, 0xFFFFFFFFL));
        r0.setState(0L);
        r1.setState(0L);
        for (int i = 0; i < 20000; i++) {
            long l = r0.nextLong();
            for (int j = 0; j < 20000; j++) {
                long r = r1.nextLong();
                if(multiplyHighJDK(l, r) != multiplyHigh(l, r))
//                if(multiplyHighJDK(l, r) != BigInteger.valueOf(l).multiply(BigInteger.valueOf(r)).shiftRight(64).longValue())
                    System.out.println(l + " " + r);
            }
        }
    }
}
