package squidpony.examples;

import squidpony.squidmath.NumberTools;
import squidpony.squidmath.ShortSet;
import squidpony.squidmath.ThrustRNG;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    private static float carp(final float x)
    {
        return x * (x * (x - 1) + (1 - x) * (1 - x));
    }
    private static float carp2(final float x) { return x - x * (x * (x - 1) + (1 - x) * (1 - x)); }
    private static float carpMid(final float x) { return carp2(x * 0.5f + 0.5f) * 2f - 1f; }
    public static double determine2(final int index)
    {
        int s = (index+1 & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (double)(1 << (32 - leading));
    }

    public static double determine3(final double base, final int index)
    {
        //int s = ;//, highest = Integer.highestOneBit(s);
        //return Double.longBitsToDouble((Double.doubleToLongBits(Math.pow(1.6180339887498948482, base + (index+1 & 0x7fffffff))) & 0xfffffffffffffL) | 0x3FFFFFFFFFFFFFFFL);
        return NumberTools.setExponent(Math.pow(1.6180339887498948482, base + (index+1 & 0x7fffffff)), 0x3ff) - 1.0;
    }
    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float querp(final float start, final float end, float a){
        return (1f - (a *= a * a * (a * (a * 6f - 15f) + 10f))) * start + a * end;
    }
    private int state = 0;
    private float nextFloat()
    {
        return (((state += 0x4) * 0xDE45) & 0xFFFF) * 0x1p-16f;
        /*
        return NumberTools.intBitsToFloat((state = state >>> 1 | (0x400000 & (
                (state << 22) //0
                        ^ (state << 19) //3
                        ^ ((state << 9) & (state << 3)) //13 19
                        ^ ((state << 4) & (state << 3)) //18 19
        ))) | 0x3f800000) - 1f;
        */
    }
    private void go() {
        double d;
        ShortSet s = new ShortSet(1 << 14);
        /*
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                System.out.print((i << 4 | j) + ": " + (d = determine2(i << 4 | j)) + ", ");
                s.add((short) (d * 512));
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(s.size);
        s.clear();
        */
//        System.out.println("\n");
//        for (int r = 0; r < 50; r++) {
//            double b = NumberTools.randomDouble(r * 181 + 421);
//            double p = NumberTools.setExponent(1.6180339887498948482 * b, 0x3ff);
//            System.out.println("With base number " + b + "...");
//            for (int i = 0; i < 32; i++) {
//                for (int j = 0; j < 16; j++) {
//                    //d = determine3(b, i << 4 | j);
//                    d = (p = NumberTools.setExponent(p + p * 1.6180339887498948482,0x3ff)) - 1.0;
//                    //System.out.print((i << 4 | j) + ": " + d + ", ");
//                    if (!s.add((short) (d * 1024)))
//                        System.out.println("!!!" + (i << 4 | j) + "!!! "  + d);
//                }
//                //System.out.println();
//            }
//            System.out.println(s.size);
//            if(s.size >= 500) System.out.println("yay!");
//            s.clear();
//        }

        for (int r = 0; r < 50; r++) {
            state = ((int)NumberTools.splitMix64(r + 2000)) & 0xFFFF;
            System.out.printf("With state %04X ...", state);
            for (int i = 0; i < (1 << 14); i++) {
//                for (int j = 0; j < 32; j++) {
                    //d = determine3(b, i << 4 | j);
                    d = nextFloat();
                    //System.out.print((i << 4 | j) + ": " + d + ", ");
                s.add((short) (d * (1 << 15)));//if (!s.add((short) (d * (1 << 12))))
                    //    System.out.println("!!!" + (i << 4 | j) + "!!! "  + d);
//                }
                //System.out.println();
            }
            System.out.println(s.size);
            if(s.size >= 16000) System.out.println("yay!");
            else System.out.println(":(");
            s.clear();
        }

//        long r;
//        for (int n = 0; n < 300000; n++) {
//            r = ThrustRNG.determine(n) | 1L;
//            for (int i = 0; i <= 48; i++) {
//                attempt(i, r);
//            }
//        }

//        success! for n1 = 34, n2 = E42C704AFCC70CAD, mod = 99B8E9033B2C36B7
//        success! for n1 = 26, n2 = 7037A25CB4E3DD3B, mod = 842B004F1C21D779
//        success! for n1 = 14, n2 = 58268ACE58A97443, mod = 97590321F6CBBD29
//        success! for n1 = 11, n2 = 776F88484745B1B5, mod = E45987F493A64B01
//        success! for n1 = 19, n2 = 72C3CEF0500AD6BD, mod = 224CD4624D29457B
//        success! for n1 = 29, n2 = ABBFD2CDF746FB4F, mod = 360B5AA8B907CC87
//        success! for n1 = 24, n2 = C3E330451D11722B, mod = 7A0E5E25EE8DA86B
//        success! for n1 = 15, n2 = D9DDD531B229649B, mod = 92B7393F131B64DD
//        success! for n1 = 21, n2 = 2BC9455837841C09, mod = A95D5DDF1BE402A9

//        attempt(34, 0xE42C704AFCC70CADL);
//        attempt(26, 0x7037A25CB4E3DD3BL);
//        attempt(14, 0x58268ACE58A97443L);
//        attempt(11, 0x776F88484745B1B5L);
//        attempt(19, 0x72C3CEF0500AD6BDL);
//        attempt(29, 0xABBFD2CDF746FB4FL);
//        attempt(24, 0xC3E330451D11722BL);
//        attempt(15, 0xD9DDD531B229649BL);
//        attempt(21, 0x2BC9455837841C09L);

//        attempt(48, 0xAB8FBC077C6CAA89L); // 0xBD8D43568B50B7D5L
//        attempt(48, 0xEB8D064529701193L); // 0xBEF16A28CA6CF4C3L
//        attempt(45, 0x0B2396DF813B328FL); // 0x0A981ECBDA908457L
//        attempt(36, -2449083123473110739L);
//        attempt(18, -6927978282019717123L);
//        attempt(45, -8133571295495752315L);

//        attempt(26, 1705668810058498499L);
//        attempt(33, -4917821408861526747L);
//        attempt(34, 8611101187267021387L);
//        attempt(43, -7934225439578050895L);

    }

    private static long rand(final long z, final long mod, final long n2)
    {
        return (z * mod >> 8) + ((z + mod) * n2);
    }

    private void attempt(int n1, long n2)
    {
        ShortSet sset = new ShortSet(65536);
        short s;
        long mod = ThrustRNG.determine(n2 + n1) | 1L, state;
        for (int i = 0; i < 65536; i++) {
            //s = (short)(i * 0x9E37 + 0xDE4D);
            //s = (short) ((state = i * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) + (state >> n2) >>> n1);
            s = (short) (rand(i, mod, n2) >>> n1);

            System.out.print((s & 0xFFFF) + " ");
            if((i & 31) == 31)
                System.out.println();
            if(!sset.add(s))
            {
                //System.out.println("already contains " + s + " at index " + i);
                return;
            }
        }
        System.out.printf("success! for n1 = %d, n2 = %016X, mod = %016X\n", n1, n2, mod);
    }

}
