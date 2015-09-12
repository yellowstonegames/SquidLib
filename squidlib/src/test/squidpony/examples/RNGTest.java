package squidpony.examples;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PermutedRNG;
import squidpony.squidmath.RNG;
import squidpony.squidmath.XorRNG;

/**
 * Created by Tommy Ettinger on 7/16/2015.
 */
public class RNGTest {
    private static int[][] ibits = new int[6][32], lbits = new int[6][64];
    private static int[] ibitsTotal = new int[6], lbitsTotal = new int[6];
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

        lbitsTotal[kind] += Long.bitCount(n);
        for(int i = 0; i < 63; i++)
        {
            lbits[kind][i] += ((long)(n) & (1L << i)) > 0 ? 1 : 0;
        }
        lbits[kind][63] += (n < 0) ? 1 : 0;
        String text = Long.toBinaryString(n);
        int len = text.length();
        return "0000000000000000000000000000000000000000000000000000000000000000".substring(0,32-len)+text + "  " + n;
    }
    public static void main(String[] args)
    {
        LightRNG light = new LightRNG(0x1337deadbeef0ffal);
        PermutedRNG perm = new PermutedRNG(0x1337deadbeef0ffal);
        XorRNG xor = new XorRNG(0x1337deadbeef0ffal);

        RNG lr = new RNG(new LightRNG(0x1337deadbeef0ffal)),
                pr = new RNG(new PermutedRNG(0x1337deadbeef0ffal)),
                xr = new RNG(new XorRNG(0x1337deadbeef0ffal));
        int c = 0;
        for(int i = 0; i < 99; i++)
        {
            c = light.nextInt(Integer.MAX_VALUE);
            System.out.println("l  : " + binaryString(c, 0));
            c = perm.nextInt(Integer.MAX_VALUE);
            System.out.println("p  : " + binaryString(c, 1));
            c = xor.nextInt(Integer.MAX_VALUE);
            System.out.println("x  : " + binaryString(c, 2));
            c = lr.nextInt(Integer.MAX_VALUE);
            System.out.println("LR : " + binaryString(c, 3));
            c = pr.nextInt(Integer.MAX_VALUE);
            System.out.println("PR : " + binaryString(c, 4));
            c = xr.nextInt(Integer.MAX_VALUE);
            System.out.println("XR : " + binaryString(c, 5));
            System.out.println(Integer.MAX_VALUE);
        }
        for(int k = 0; k < 6; k++) {
            for (int i = 0; i < 32; i++) {
                System.out.print(String.format("%02d ", ibits[k][i]));
            }
            System.out.println();
        }

        for(int k = 0; k < 6; k++) {
            System.out.println(ibitsTotal[k]);
        }

    }
}
