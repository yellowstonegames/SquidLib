package squidpony.examples;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PermutedRNG;
import squidpony.squidmath.RNG;
import squidpony.squidmath.XorRNG;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by Tommy Ettinger on 7/16/2015.
 */
public class RNGTest {
    private static int[][] ibits = new int[9][32], lbits = new int[9][64];
    private static int[] ibitsTotal = new int[9], lbitsTotal = new int[9];
    private static BigInteger[] counters = new BigInteger[]{ BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO};
    private static String[] kindNames = new String[]{"Light", "Permuted", "Xor",
            "Light+RNG", "Permuted+RNG", "Xor+RNG",
            "Light+asRandom", "Permuted+asRandom", "Xor+asRandom"};

    public static String binaryString(int n, int kind)
    {
        ibitsTotal[kind] += Integer.bitCount(n);
        for(int i = 0; i < 32; i++)
        {
            ibits[kind][i] += ((long)(n) & (1L << i)) > 0 ? 1 : 0;
        }
        String text = Integer.toBinaryString(n);
        int len = text.length();
        return "00000000000000000000000000000000".substring(0,32-len)+text + "  " + n;
    }
    public static String binaryString(long n, int kind)
    {
        counters[kind] = counters[kind].add(BigInteger.valueOf(n));
        lbitsTotal[kind] += Long.bitCount(n);
        for(int i = 0; i < 63; i++)
        {
            lbits[kind][i] += (n & (1L << i)) > 0 ? 1 : 0;
        }
        lbits[kind][63] += (n < 0) ? 1 : 0;
        String text = Long.toBinaryString(n);
        int len = text.length();
        return "0000000000000000000000000000000000000000000000000000000000000000".substring(0,64-len)+text + "  " + n;
    }
    public static void main(String[] args)
    {
        LightRNG light = new LightRNG(0xDADA157);
        PermutedRNG perm = new PermutedRNG(0xDADA157);
        XorRNG xor = new XorRNG(0xDADA157);

        RNG lr = new RNG(new LightRNG(0xDADA157)),
                pr = new RNG(new PermutedRNG(0xDADA157)),
                xr = new RNG(new XorRNG(0xDADA157));
        Random lrr = lr.asRandom(),
               prr = pr.asRandom(),
               xrr = xr.asRandom();
        int c = 0, bound = 1 << 16;
        for(int i = 0; i < 99; i++)
        {
            c = light.nextInt(bound);
            System.out.println("l   : " + binaryString(c, 0));
            c = perm.nextInt(bound);
            System.out.println("p   : " + binaryString(c, 1));
            c = xor.nextInt(bound);
            System.out.println("x   : " + binaryString(c, 2));
            c = lr.nextInt(bound);
            System.out.println("LR  : " + binaryString(c, 3));
            c = pr.nextInt(bound);
            System.out.println("PR  : " + binaryString(c, 4));
            c = xr.nextInt(bound);
            System.out.println("XR  : " + binaryString(c, 5));
            c = lrr.nextInt(bound);
            System.out.println("LRR : " + binaryString(c, 6));
            c = prr.nextInt(bound);
            System.out.println("PRR : " + binaryString(c, 7));
            c = xrr.nextInt(bound);
            System.out.println("XRR : " + binaryString(c, 8));
            System.out.println();
        }
        for(int k = 0; k < 9; k++) {
            for (int i = 31; i >= 0; i--) {
                System.out.print(String.format("%02d ", ibits[k][i]));
            }
            System.out.println();
        }

        for(int k = 0; k < 9; k++) {
            System.out.println(ibitsTotal[k]);
        }

        light = new LightRNG(0xDADA157);
        perm = new PermutedRNG(0xDADA157);
        xor = new XorRNG(0xDADA157);

        lr = new RNG(new LightRNG(0xDADA157));
        pr = new RNG(new PermutedRNG(0xDADA157));
        xr = new RNG(new XorRNG(0xDADA157));
        lrr = lr.asRandom();
        prr = pr.asRandom();
        xrr = xr.asRandom();

        long l = 0, longBound = 1L << 48;
        for(int i = 0; i < 99; i++)
        {
            l = light.nextLong(longBound);
            System.out.println("l   : " + binaryString(l, 0));
            l = perm.nextLong(longBound);
            System.out.println("p   : " + binaryString(l, 1));
            l = xor.nextLong(longBound);
            System.out.println("x   : " + binaryString(l, 2));
            l = lr.nextLong(longBound);
            System.out.println("LR  : " + binaryString(l, 3));
            l = pr.nextLong(longBound);
            System.out.println("PR  : " + binaryString(l, 4));
            l = xr.nextLong(longBound);
            System.out.println("XR  : " + binaryString(l, 5));
            /*l = lrr.nextLong(longBound);
            System.out.println("LRR : " + binaryString(l, 6));
            l = prr.nextLong(longBound);
            System.out.println("PRR : " + binaryString(l, 7));
            l = xrr.nextLong(longBound);
            System.out.println("XRR : " + binaryString(l, 8));
            */
            System.out.println(longBound);
        }
        for(int k = 0; k < 6; k++) {
            for (int i = 63; i >= 0; i--) {
                System.out.print(String.format("%02d ", lbits[k][i]));
            }
            System.out.println();
        }

        for(int k = 0; k < 6; k++) {
            System.out.println(kindNames[k] + " : ");
            System.out.println(lbitsTotal[k]);
            System.out.println(counters[k].toString());
        }
        

    }
}
