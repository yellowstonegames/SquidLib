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
    public static float sway(final float value)
    {
        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * a * (a * (a * 6f - 15f) + 10f) * 2f - 1f;
    }

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
    private void go() {
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

        for (float f = -9f; f <= 9f; f += 0.125f) {
            System.out.printf("%f: %f, %f\n", f, NumberTools.zigzag(f), sway(f));
        }


//        double d;
//        ShortSet s = new ShortSet(1 << 14);
//        SaltyQRNG qrng = new SaltyQRNG(1234);
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X,\n", qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X,\n", qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X,\n", qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X,\n", qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X,\n", qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X,\n", qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X,\n", qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        System.out.printf("%08X, %08X, %08X, %08X, %08X, %08X, %08X, %08X\n",  qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt(), qrng.nextInt());
//        for (int i = 0; i < (1 << 14); i++) {
//            s.add((short) (qrng.nextFloat() * (1 << 15)));//if (!s.add((short) (d * (1 << 12))))
//            //s.add(t);//if (!s.add((short) (d * (1 << 12))))
//        }
//        System.out.println(s.size);

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
