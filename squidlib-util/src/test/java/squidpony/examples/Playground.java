package squidpony.examples;

import squidpony.squidmath.*;

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
    private static float cerp(final float x) { return x * x * (3f - 2f * x); }

    public static double determine2(final int index)
    {
        int s = (index+1 & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (double)(1 << (32 - leading));
    }

    public static double planarDetermine(final long base, final int index)
    {
        //int s = ;//, highest = Integer.highestOneBit(s);
        //return Double.longBitsToDouble((Double.doubleToLongBits(Math.pow(1.6180339887498948482, base + (index+1 & 0x7fffffff))) & 0xfffffffffffffL) | 0x3FFFFFFFFFFFFFFFL);
        
//        return NumberTools.setExponent(Math.cbrt(NumberTools.setExponent((base * index >>> 11) + index, 0x402) + 1.0) - 2.0, 0x3FF) - 1.0; // 8.0 to 16.0
        final double mixed = NumberTools.setExponent((base * index >>> 12 ^ index) * 1.6180339887498948482, 0x400); // 2.0 to 4.0
        return NumberTools.setExponent(Math.pow((mixed + index), mixed * 2.6180339887498948482), 0x3ff) - 1.0;
    }
    // just puts outputs on lines, Marsaglia's Theorem style
    public static double determine4(final long base, final int index)
    {
        return ((base * Integer.reverse(index) << 21) & 0x1fffffffffffffL) * 0x1p-53;
    }
//    public static double planarDetermine(long base, final int index) {
//        return (((base *= Long.reverse(base * index)) ^ (base >>> 5)) >>> 11) * 0x1p-53;
//    }

//    public static double planarDetermine(long base, final int index) {
//        return (Long.reverse((base *= index) ^ Long.rotateLeft(base, 3) ^ Long.rotateLeft(base, 57)) >>> 11) * 0x1p-53; 
//    }

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
    public static double querp(final double start, final double end, double a) {
        return (1.0 - (a *= a * a * (a * (a * 6.0 - 15.0) + 10.0))) * start + a * end;
    }

//    public static float sway(final float value)
//    {
//        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
//        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
//        return a * a * a * (a * (a * 6f - 15f) + 10f) * 2f - 1f;
//    }

    public static float swayOld(float a) { a = Math.abs(Math.abs(a - 1f) % 2f - 1f); return a * a * a * (a * (a * 6f - 15f) + 10f); }

    private int state = 0;
    private int mul = 0xF7910000;
    private float nextFloat(final int salt)
    {
        return (((state >>> 1) * mul ^ ((state += salt) * mul)) & 0xFFFF) * 0x1p-16f;
        /*
        return NumberTools.intBitsToFloat((state = state >>> 1 | (0x400000 & (
                (state << 22) //0
                        ^ (state << 19) //3
                        ^ ((state << 9) & (state << 3)) //13 19
                        ^ ((state << 4) & (state << 3)) //18 19
        ))) | 0x3f800000) - 1f;
        */
    }
    private int nextInt(final int salt)
    {
        final int t = (state += salt) * 0xF7910000;
        return (t >>> 26 | t >>> 10) & 0xFFFF;
        /*
        return NumberTools.intBitsToFloat((state = state >>> 1 | (0x400000 & (
                (state << 22) //0
                        ^ (state << 19) //3
                        ^ ((state << 9) & (state << 3)) //13 19
                        ^ ((state << 4) & (state << 3)) //18 19
        ))) | 0x3f800000) - 1f;
        */
    }
    public static double swayRandomized(final long seed, final double value)
    {
        final long s = Double.doubleToLongBits(value + (value < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m,
                flip = -((sm & 0x8000000000000L)>>51), floor = Noise.longFloor(value) + seed, sb = (s >> 63) ^ flip;
        double a = (Double.longBitsToDouble(((sm ^ flip) & 0xfffffffffffffL)
                | 0x4000000000000000L) - 2.0);
        final double start = NumberTools.randomSignedDouble(floor), end = NumberTools.randomSignedDouble(floor + 1L);
        a = a * a * a * (a * (a * 6.0 - 15.0) + 10.0) * (sb | 1L) - sb;
        return (1.0 - a) * start + a * end;
    }

    public static double asin(double a) { return (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357))) / (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527)); }

    private void go() {
//        double r, a, b;
//        for (int s = 1; s < 100; s++) {
//            final int leading = Integer.numberOfLeadingZeros(s);
//            if((r = Math.abs((a = (Integer.reverse(s) >>> leading) / (double) (1 << (32 - leading))) - (b = (Integer.reverse(s) >>> 1) * 0x1p-31))) > 0.001)
//                System.out.println("UH OH on " + s + ": " + r);
//            System.out.println(s + ": " + a + ", " + b);
//        }

//        long seed = 0x1337DEADBEEFCAFEL;
//        for (double i = 0.0; i <= 17.0; i += 0x1p-4) {
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f  % 3.10f\n", i, NumberTools.sway((float)i), NumberTools.sway(i), NumberTools.swayRandomized(seed, i), NumberTools.swayRandomized(seed+1L, i));
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f  % 3.10f\n", -i, NumberTools.sway((float)-i), NumberTools.sway(-i), NumberTools.swayRandomized(seed,-i), NumberTools.swayRandomized(seed+1L,-i));
//        }
//        System.out.println("NumberTools.sway(Float.POSITIVE_INFINITY)  :  " + NumberTools.sway(Float.POSITIVE_INFINITY));
//        System.out.println("NumberTools.sway(Float.NEGATIVE_INFINITY)  :  " + NumberTools.sway(Float.NEGATIVE_INFINITY));
//        System.out.println("NumberTools.sway(Float.MIN_VALUE)          :  " + NumberTools.sway(Float.MIN_VALUE));
//        System.out.println("NumberTools.sway(Float.MAX_VALUE)          :  " + NumberTools.sway(Float.MAX_VALUE));
//        System.out.println("NumberTools.sway(Float.MIN_NORMAL)         :  " + NumberTools.sway(Float.MIN_NORMAL));
//        System.out.println("NumberTools.sway(Float.NaN)                :  " + NumberTools.sway(Float.NaN));
//        System.out.println();
//        System.out.println("NumberTools.sway(Double.POSITIVE_INFINITY) :  " + NumberTools.sway(Double.POSITIVE_INFINITY));
//        System.out.println("NumberTools.sway(Double.NEGATIVE_INFINITY) :  " + NumberTools.sway(Double.NEGATIVE_INFINITY));
//        System.out.println("NumberTools.sway(Double.MIN_VALUE)         :  " + NumberTools.sway(Double.MIN_VALUE));
//        System.out.println("NumberTools.sway(Double.MAX_VALUE)         :  " + NumberTools.sway(Double.MAX_VALUE));
//        System.out.println("NumberTools.sway(Double.MIN_NORMAL)        :  " + NumberTools.sway(Double.MIN_NORMAL));
//        System.out.println("NumberTools.sway(Double.NaN)               :  " + NumberTools.sway(Double.NaN));
//        System.out.println();
//        System.out.println("swayRandomized(Double.POSITIVE_INFINITY)   :  " + NumberTools.swayRandomized(seed, Double.POSITIVE_INFINITY));
//        System.out.println("swayRandomized(Double.NEGATIVE_INFINITY)   :  " + NumberTools.swayRandomized(seed, Double.NEGATIVE_INFINITY));
//        System.out.println("swayRandomized(Double.MIN_VALUE)           :  " + NumberTools.swayRandomized(seed, Double.MIN_VALUE));
//        System.out.println("swayRandomized(Double.MAX_VALUE)           :  " + NumberTools.swayRandomized(seed, Double.MAX_VALUE));
//        System.out.println("swayRandomized(Double.MIN_NORMAL)          :  " + NumberTools.swayRandomized(seed, Double.MIN_NORMAL));
//        System.out.println("swayRandomized(Double.NaN)                 :  " + NumberTools.swayRandomized(seed, Double.NaN));

        final int SIZE = 0x10000;
        double[] xs = new double[SIZE], ys = new double[SIZE];
        double closest = 999.0, x, y, xx, yy;
//        for (int i = 0; i < SIZE; i++) {
////            xs[i] = x = planarDetermine(0xDE4DBEEF, i + 1); // why do these work so well???
////            ys[i] = y = planarDetermine(0x1337D00D, i + 1); //  now we know, they were forming lines
//            xs[i] = x = planarDetermine(57L, i);
//            ys[i] = y = planarDetermine(73L, i);
//            for (int k = 0; k < i; k++) {
//                xx = x - xs[k];
//                yy = y - ys[k];
//                closest = Math.min(closest, xx * xx + yy * yy);
//            }
//        }
//        System.out.printf("Closest distance with integer-reversal(%d,%d): %.9f\n", 0xDE4DBEEF, 0x1337D00D, Math.sqrt(closest));
        int s, p2;
        for(int p : new int[]{1, 3, 11, 13, 23, 29, 39, 41, 47}) {
//        for (int p = 1; p < 2500; p+=101) {
            closest = 999.0;
            p2 = p * 0x2C9277B5 | 3;
            for (int i = 0; i < SIZE; i++) {
                //s = GreasedRegion.disperseBits(Integer.reverse(i * p + 1));
//                s = GreasedRegion.disperseBits((int) (VanDerCorputQRNG.altDetermine(p, i) * 0x40000000));
                s = GreasedRegion.disperseBits(Integer.reverse(p2 * (i ^ 0xAC564B05)));

                xs[i] = x = (s & 0xffff) * 0x1p-16;
                ys[i] = y = (s >>> 16 & 0xffff) * 0x1p-16;
//                xs[i] = x = (s & 0x7fff) * 0x1p-15;
//                ys[i] = y = (s >>> 16 & 0x7fff) * 0x1p-15;
                for (int k = 0; k < i; k++) {
                    xx = x - xs[k];
                    yy = y - ys[k];
                    closest = Math.min(closest, xx * xx + yy * yy);
                }
            }
            System.out.printf("Closest distance with Z-order on altHalton(%d): %.9f\n", p, Math.sqrt(closest));
        }
        //Closest distance with Halton(2,13): 0.000871727
        //Closest distance with Halton(2,39): 0.001147395
        for(int p : new int[]{3, 11, 13, 23, 29, 39, 41, 47}) {
            closest = 999.0;
            for (int i = 0; i < SIZE; i++) {
                s = i;//(i ^ (i << 7 | i >>> 25) ^ (i << 19 | i >>> 13));
                xs[i] = x = VanDerCorputQRNG.determine2(s);
                ys[i] = y = VanDerCorputQRNG.determine(p, s);
                for (int k = 0; k < i; k++) {
                    xx = x - xs[k];
                    yy = y - ys[k];
                    closest = Math.min(closest, xx * xx + yy * yy);
                }
            }
            System.out.printf("Closest distance with Halton(2,%d): %.9f\n", p, Math.sqrt(closest));
        }
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
