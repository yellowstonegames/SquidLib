package squidpony.examples;

import squidpony.squidmath.*;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by Tommy Ettinger on 7/16/2015.
 */
public class RNGTest {
    private static int[][] ibits = new int[21][32], lbits = new int[21][64];
    private static int[] ibitsTotal = new int[21], lbitsTotal = new int[21];
    private static BigInteger[] counters = new BigInteger[]{ BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
            BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO
    };
    private static String[] kindNames = new String[]{"Light", "Permuted", "Xor",
            "Light+RNG", "Permuted+RNG", "Xor+RNG", "Deck(RNG)",
            "Light+asRandom", "Permuted+asRandom", "Xor+asRandom", "Deck+asRandom",
            "Isaac", "Isaac+asRandom", "LongPeriod", "LongPeriod+asRandom",
            "XoRo", "XoRo+AsRandom", "Sobol(Dissimilar)",
            "Thunder", "Thunder+RNG", "Thunder+asRandom"
    };

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
        long seed = new LightRNG().nextLong();
        LightRNG light = new LightRNG(seed);
        PermutedRNG perm = new PermutedRNG(seed);
        XorRNG xor = new XorRNG(seed);
        ThunderRNG thunder = new ThunderRNG(seed);

        SobolQRNG sbl = new SobolQRNG(2);
        sbl.skipTo(9000);
        RNG lr = new RNG(new LightRNG(seed)),
                pr = new RNG(new PermutedRNG(seed)),
                xr = new RNG(new XorRNG(seed)),
                ir = new RNG(new IsaacRNG()),
                lpr = new RNG(new LongPeriodRNG(seed)),
                xnr = new RNG(new XoRoRNG(seed));
        StatefulRNG tr = new StatefulRNG(new ThunderRNG(seed));
        DeckRNG dr = new DeckRNG(seed);
        Random lrr = lr.asRandom(),
                prr = pr.asRandom(),
                xrr = xr.asRandom(),
                drr = dr.asRandom(),
                irr = ir.asRandom(),
                lprr = lpr.asRandom(),
                xnrr = xnr.asRandom(),
                trr = tr.asRandom();
        int c = 0;
        for(int i = 0; i < 99; i++)
        {
            c = light.nextInt();
            /*System.out.println("l   : " + */ binaryString(c, 0);
            c = perm.nextInt();
            /*System.out.println("p   : " + */ binaryString(c, 1);
            c = xor.nextInt();
            /*System.out.println("x   : " + */ binaryString(c, 2);
            c = lr.nextInt();
            /*System.out.println("LR  : " + */ binaryString(c, 3);
            c = pr.nextInt();
            /*System.out.println("PR  : " + */ binaryString(c, 4);
            c = xr.nextInt();
            /*System.out.println("XR  : " + */ binaryString(c, 5);
            c = dr.nextInt();
            /*System.out.println("DR  : " + */ binaryString(c, 6);
            c = lrr.nextInt();
            /*System.out.println("LRR : " + */ binaryString(c, 7);
            c = prr.nextInt();
            /*System.out.println("PRR : " + */ binaryString(c, 8);
            c = xrr.nextInt();
            /*System.out.println("XRR : " + */ binaryString(c, 9);
            c = drr.nextInt();
            /*System.out.println("DRR : " + */ binaryString(c, 10);
            c = ir.nextInt();
            /*System.out.println("IR  : " + */ binaryString(c, 11);
            c = irr.nextInt();
            /*System.out.println("IRR : " + */ binaryString(c, 12);
            c = lpr.nextInt();
            /*System.out.println("LPR : " + */ binaryString(c, 13);
            c = lprr.nextInt();
            /*System.out.println("LPRR: " + */ binaryString(c, 14);
            c = xnr.nextInt();
            /*System.out.println("XNR : " + */ binaryString(c, 15);
            c = xnrr.nextInt();
            /*System.out.println("XNRR: " + */ binaryString(c, 16);
            c = sbl.next(32);
            /*System.out.println("SBL : " + */ binaryString(c, 17);
            c = thunder.nextInt();
            System.out.println("T   : " + binaryString(c, 18));
            c = tr.nextInt();
            System.out.println("TR  : " + binaryString(c, 19));
            c = trr.nextInt();
            System.out.println("TRR: " + binaryString(c, 20));
            System.out.println();
        }
        for(int k = 0; k < 21; k++) {
            for (int i = 31; i >= 0; i--) {
                System.out.print(String.format("%02d ", ibits[k][i]));
            }
            System.out.println(kindNames[k]);
        }

        for(int k = 0; k < 21; k++) {
            System.out.println(ibitsTotal[k]);
        }

        light = new LightRNG(seed);
        perm = new PermutedRNG(seed);
        xor = new XorRNG(seed);

        lr = new RNG(new LightRNG(seed));
        pr = new RNG(new PermutedRNG(seed));
        xr = new RNG(new XorRNG(seed));
        dr = new DeckRNG(seed);
        ir = new RNG(new IsaacRNG());
        lpr = new RNG(new LongPeriodRNG(seed));
        lrr = lr.asRandom();
        prr = pr.asRandom();
        xrr = xr.asRandom();
        thunder = new ThunderRNG(seed);
        tr = new StatefulRNG(new ThunderRNG(seed));
        trr = tr.asRandom();

        long l = 0;
        for(int i = 0; i < 99; i++)
        {
            l = light.nextLong();
            /*System.out.println("l   : " + */ binaryString(l, 0);
            l = perm.nextLong();
            /*System.out.println("p   : " + */ binaryString(l, 1);
            l = xor.nextLong();
            /*System.out.println("x   : " + */ binaryString(l, 2);
            l = lr.nextLong();
            /*System.out.println("LR  : " + */ binaryString(l, 3);
            l = pr.nextLong();
            /*System.out.println("PR  : " + */ binaryString(l, 4);
            l = xr.nextLong();
            /*System.out.println("XR  : " + */ binaryString(l, 5);
            l = dr.nextLong();
            /*System.out.println("DR  : " + */ binaryString(l, 6);
            l = ir.nextLong();
            /*System.out.println("IR  : " + */ binaryString(l, 11);
            l = lpr.nextLong();
            /*System.out.println("LPR : " + */ binaryString(l, 13);
            l = xnr.nextLong();
            /*System.out.println("XNR : " + */ binaryString(l, 15);
            l = xnrr.nextLong();
            /*System.out.println("XNRR: " + */ binaryString(l, 16);
            l = sbl.nextLong();
            /*System.out.println("SBL : " + */ binaryString(l, 17);
            l = thunder.nextLong();
            System.out.println("T   : " + binaryString(l, 18));
            l = tr.nextLong();
            System.out.println("TR  : " + binaryString(l, 19));
            l = trr.nextLong();
            System.out.println("TRR : " + binaryString(l, 20));
            System.out.println();

            /*l = lrr.nextLong();
            System.out.println("LRR : " + binaryString(l, 6));
            l = prr.nextLong();
            System.out.println("PRR : " + binaryString(l, 7));
            l = xrr.nextLong();
            System.out.println("XRR : " + binaryString(l, 8));
            */
        }
        for(int k = 0; k < 21; k++) {
            for (int i = 63; i >= 0; i--) {
                System.out.print(String.format("%02d ", lbits[k][i]));
            }
            System.out.println(kindNames[k]);
        }

        for(int k = 0; k < 21; k++) {
            System.out.println(kindNames[k] + " : ");
            System.out.println(lbitsTotal[k]);
            System.out.println(counters[k]);
        }

        light = new LightRNG(seed);
        perm = new PermutedRNG(seed);
        xor = new XorRNG(seed);

        lr = new RNG(new LightRNG(seed));
        pr = new RNG(new PermutedRNG(seed));
        xr = new RNG(new XorRNG(seed));
        dr = new DeckRNG(seed);
        ir = new RNG(new IsaacRNG());
        lpr = new RNG(new LongPeriodRNG(seed));
        xnr = new RNG(new XoRoRNG(seed));
        sbl.skipTo(9000);

        lrr = lr.asRandom();
        prr = pr.asRandom();
        xrr = xr.asRandom();
        irr = ir.asRandom();
        lprr = lpr.asRandom();
        xnrr = xnr.asRandom();
        thunder = new ThunderRNG(seed);
        tr = new StatefulRNG(new ThunderRNG(seed));
        trr = tr.asRandom();

        ibits = new int[21][32];
        ibitsTotal = new int[21];
        int d = 0;
        for(int i = 0; i < 99; i++)
        {
            d = (int)((light.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("l   : " + */ binaryString(d, 0);
            d = (int)((perm.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("p   : " + */ binaryString(d, 1);
            d = (int)((xor.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("x   : " + */ binaryString(d, 2);
            d = (int)((lr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("LR  : " + */ binaryString(d, 3);
            d = (int)((pr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("PR  : " + */ binaryString(d, 4);
            d = (int)((xr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("XR  : " + */ binaryString(d, 5);
            d = (int)((dr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("DR  : " + */ binaryString(d, 6);
            d = (int)((lrr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("LRR : " + */ binaryString(d, 7);
            d = (int)((prr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("PRR : " + */ binaryString(d, 8);
            d = (int)((xrr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("XRR : " + */ binaryString(d, 9);
            d = (int)((xrr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("DRR : " + */ binaryString(d, 10);
            d = (int)((ir.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("IR  : " + */ binaryString(d, 11);
            d = (int)((irr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("IRR : " + */ binaryString(d, 12);
            d = (int)((lpr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("LPR : " + */ binaryString(d, 13);
            d = (int)((lprr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("LPRR: " + */ binaryString(d, 14);
            d = (int)((xnr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("XNR : " + */ binaryString(d, 15);
            d = (int)((xnrr.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("XNRR: " + */ binaryString(d, 16);
            d = (int)((sbl.nextDouble() - 0.5) * 0xffffffffL);
            /*System.out.println("SBL : " + */ binaryString(d, 17);
            d = (int)((thunder.nextDouble() - 0.5) * 0xffffffffL);
            System.out.println("T   : " + binaryString(d, 18));
            d = (int)((tr.nextDouble() - 0.5) * 0xffffffffL);
            System.out.println("TR  : " + binaryString(d, 19));
            d = (int)((trr.nextDouble() - 0.5) * 0xffffffffL);
            System.out.println("TRR : " + binaryString(d, 20));
            System.out.println();

            //System.out.println();
        }
        for(int k = 0; k < 21; k++) {
            for (int i = 31; i >= 0; i--) {
                System.out.print(String.format("%02d ", ibits[k][i]));
            }
            System.out.println(kindNames[k]);
        }

        for(int k = 0; k < 21; k++) {
            System.out.println(ibitsTotal[k]);
        }


    }
}
