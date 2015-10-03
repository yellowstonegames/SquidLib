package squidpony.examples;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PermutedRNG;
import squidpony.squidmath.RNG;
import squidpony.squidmath.XorRNG;

import java.math.BigInteger;

/**
 * Created by Tommy Ettinger on 7/16/2015.
 */
public class RNGTest {
    private static int[][] ibits = new int[6][32], lbits = new int[6][64];
    private static int[] ibitsTotal = new int[6], lbitsTotal = new int[6];
    private static BigInteger[] counters = new BigInteger[]{ BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO};
    private static String[] kindNames = new String[]{"Light", "Permuted", "Xor",
                                           "Light+RNG", "Permuted+RNG", "Xor+RNG"};

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
        LightRNG light = new LightRNG(2L);
        PermutedRNG perm = new PermutedRNG(2L);
        XorRNG xor = new XorRNG(2L);

        RNG lr = new RNG(new LightRNG(2L)),
                pr = new RNG(new PermutedRNG(2L)),
                xr = new RNG(new XorRNG(2L));
        int c = 0;
        for(int i = 0; i < 99; i++)
        {
            c = light.nextInt();
            System.out.println("l  : " + binaryString(c, 0));
            c = perm.nextInt();
            System.out.println("p  : " + binaryString(c, 1));
            c = xor.nextInt();
            System.out.println("x  : " + binaryString(c, 2));
            c = lr.nextInt();
            System.out.println("LR : " + binaryString(c, 3));
            c = pr.nextInt();
            System.out.println("PR : " + binaryString(c, 4));
            c = xr.nextInt();
            System.out.println("XR : " + binaryString(c, 5));
            System.out.println();
        }
        for(int k = 0; k < 6; k++) {
            for (int i = 31; i >= 0; i--) {
                System.out.print(String.format("%02d ", ibits[k][i]));
            }
            System.out.println();
        }

        for(int k = 0; k < 6; k++) {
            System.out.println(ibitsTotal[k]);
        }

        light = new LightRNG(2L);
        perm = new PermutedRNG(2L);
        xor = new XorRNG(2L);

        lr = new RNG(new LightRNG(2L));
        pr = new RNG(new PermutedRNG(2L));
        xr = new RNG(new XorRNG(2L));
        long l = 0;
        for(int i = 0; i < 99; i++)
        {
            l = light.nextLong();
            System.out.println("l  : " + binaryString(l, 0));
            l = perm.nextLong();
            System.out.println("p  : " + binaryString(l, 1));
            l = xor.nextLong();
            System.out.println("x  : " + binaryString(l, 2));
            l = lr.nextLong();
            System.out.println("LR : " + binaryString(l, 3));
            l = pr.nextLong();
            System.out.println("PR : " + binaryString(l, 4));
            l = xr.nextLong();
            System.out.println("XR : " + binaryString(l, 5));
            System.out.println();
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
