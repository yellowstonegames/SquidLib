package squidpony.examples;

import squidpony.squidmath.Noise;
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
    private static float cerp(final float x) { return x * x * (3f - 2f * x); }

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

    private void go() {
//        for (double i = Math.PI / 9.0; i <= 0x1p30; i *= Math.E) {
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f\n", i, NumberTools.sway((float)i), NumberTools.sway(i), swayRandomized(seed, i));
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f\n", -i, NumberTools.sway((float)-i), NumberTools.sway(-i), swayRandomized(seed,-i));
//        }



        long seed = 0x1337DEADBEEFCAFEL;
        System.out.println(0.5 + NumberTools.randomDouble(seed));
        for (double i = 0.0; i <= 17.0; i += 0x1p-4) {
            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f\n", i, NumberTools.sway((float)i), NumberTools.sway(i), swayRandomized(seed, i));
            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f\n", -i, NumberTools.sway((float)-i), NumberTools.sway(-i), swayRandomized(seed,-i));
        }
        System.out.println("NumberTools.sway(Float.POSITIVE_INFINITY)  :  " + NumberTools.sway(Float.POSITIVE_INFINITY));
        System.out.println("NumberTools.sway(Float.NEGATIVE_INFINITY)  :  " + NumberTools.sway(Float.NEGATIVE_INFINITY));
        System.out.println("NumberTools.sway(Float.MIN_VALUE)          :  " + NumberTools.sway(Float.MIN_VALUE));
        System.out.println("NumberTools.sway(Float.MAX_VALUE)          :  " + NumberTools.sway(Float.MAX_VALUE));
        System.out.println("NumberTools.sway(Float.MIN_NORMAL)         :  " + NumberTools.sway(Float.MIN_NORMAL));
        System.out.println("NumberTools.sway(Float.NaN)                :  " + NumberTools.sway(Float.NaN));
        System.out.println();
        System.out.println("NumberTools.sway(Double.POSITIVE_INFINITY) :  " + NumberTools.sway(Double.POSITIVE_INFINITY));
        System.out.println("NumberTools.sway(Double.NEGATIVE_INFINITY) :  " + NumberTools.sway(Double.NEGATIVE_INFINITY));
        System.out.println("NumberTools.sway(Double.MIN_VALUE)         :  " + NumberTools.sway(Double.MIN_VALUE));
        System.out.println("NumberTools.sway(Double.MAX_VALUE)         :  " + NumberTools.sway(Double.MAX_VALUE));
        System.out.println("NumberTools.sway(Double.MIN_NORMAL)        :  " + NumberTools.sway(Double.MIN_NORMAL));
        System.out.println("NumberTools.sway(Double.NaN)               :  " + NumberTools.sway(Double.NaN));
        System.out.println();
        System.out.println("swayRandomized(Double.POSITIVE_INFINITY)   :  " + swayRandomized(seed, Double.POSITIVE_INFINITY));
        System.out.println("swayRandomized(Double.NEGATIVE_INFINITY)   :  " + swayRandomized(seed, Double.NEGATIVE_INFINITY));
        System.out.println("swayRandomized(Double.MIN_VALUE)           :  " + swayRandomized(seed, Double.MIN_VALUE));
        System.out.println("swayRandomized(Double.MAX_VALUE)           :  " + swayRandomized(seed, Double.MAX_VALUE));
        System.out.println("swayRandomized(Double.MIN_NORMAL)          :  " + swayRandomized(seed, Double.MIN_NORMAL));
        System.out.println("swayRandomized(Double.NaN)                 :  " + swayRandomized(seed, Double.NaN));




//        for (int n = 100; n < 120; n++) {
//            long i = ThrustAltRNG.determine(n);
//            System.out.printf("%016X : % 3.10f  % 3.10f\n", i, NumberTools.formFloat((int) (i >>> 32)), NumberTools.formDouble(i));
//            System.out.printf("%016X : % 3.10f  % 3.10f\n", ~i, NumberTools.formFloat((int)(~i >>> 32)), NumberTools.formDouble(~i));
//        }

//        TabbyNoise tabby = TabbyNoise.instance;
//        double v;
//        for(double x : new double[]{0.0, -1.0, 1.0, -10.0, 10.0, -100.0, 100.0, -1000.0, 1000.0, -10000.0, 10000.0}){
//            for(double y : new double[]{0.0, -1.0, 1.0, -10.0, 10.0, -100.0, 100.0, -1000.0, 1000.0, -10000.0, 10000.0}){
//                for(double z : new double[]{0.0, -1.0, 1.0, -10.0, 10.0, -100.0, 100.0, -1000.0, 1000.0, -10000.0, 10000.0}) {
//                    for (double a = -0.75; a <= 0.75; a += 0.375) {
//                        System.out.printf("x=%f,y=%f,z=%f,value=%f\n", x+a, y+a, z+a, tabby.getNoiseWithSeed(x+a, y+a, z+a, 1234567));
//                    }
//                }
//            }
//        }

//        for (float f = 0f; f <= 1f; f += 0.0625f) {
//            System.out.printf("%f: querp: %f, carp2: %f, cerp: %f\n", f, querp(-100, 100, f), carp2(f), cerp(f));
//        }

//        Mnemonic mn = new Mnemonic(1L);
//        String text;
//        long r;
//        for (long i = 1L; i <= 50; i++) {
//            r = ThrustAltRNG.determine(i);
//            System.out.println(r + ": " + (text = mn.toMnemonic(r, true)) + " decodes to " + mn.fromMnemonic(text));
//        }
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
