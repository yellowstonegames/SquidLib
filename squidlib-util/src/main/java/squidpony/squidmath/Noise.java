package squidpony.squidmath;

/**
 * A container class for various interfaces and implementing classes that affect continuous noise, such as that produced
 * by {@link WhirlingNoise} or {@link SeededNoise}, as well as static utility methods used throughout noise code.
 * <br>
 * Created by Tommy Ettinger on 3/17/2017.
 */
public class Noise {
    public static final SeededNoise alternate = new SeededNoise(0xFEEDCAFE);
    /**
     * Like {@link Math#floor}, but returns a long.
     * Doesn't consider weird doubles like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as a long
     */
    public static long longFloor(double t) {
        return t >= 0.0 ? (long) t : (long) t - 1L;
    }
    /**
     * Like {@link Math#floor(double)}, but takes a float and returns a long.
     * Doesn't consider weird floats like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as a long
     */
    public static long longFloor(float t) {
        return t >= 0f ? (long) t : (long) t - 1L;
    }
    /**
     * Like {@link Math#floor(double)} , but returns an int.
     * Doesn't consider weird doubles like INFINITY and NaN.
     * @param t the float to find the floor for
     * @return the floor of t, as an int
     */
    public static int fastFloor(double t) {
        return t >= 0.0 ? (int) t : (int) t - 1;
    }
    /**
     * Like {@link Math#floor(double)}, but takes a float and returns an int.
     * Doesn't consider weird floats like INFINITY and NaN.
     * @param t the float to find the floor for
     * @return the floor of t, as an int
     */
    public static int fastFloor(float t) {
        return t >= 0f ? (int) t : (int) t - 1;
    }
    /**
     * Like {@link Math#ceil(double)}, but returns an int.
     * Doesn't consider weird doubles like INFINITY and NaN.
     * @param t the float to find the ceiling for
     * @return the ceiling of t, as an int
     */
    public static int fastCeil(double t) {
        return t >= 0.0 ? -(int) -t + 1: -(int)-t;
    }
    /**
     * Like {@link Math#ceil(double)}, but takes a float and returns an int.
     * Doesn't consider weird floats like INFINITY and NaN.
     * @param t the float to find the ceiling for
     * @return the ceiling of t, as an int
     */
    public static int fastCeil(float t) {
        return t >= 0f ? -(int) -t + 1: -(int)-t;
    }

    /**
     * Cubic-interpolates between start and end (valid doubles), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively. Somewhat faster than
     * quintic interpolation ({@link #querp(double, double, double)}), but slower (and smoother) than
     * {@link  #lerp(double, double, double)}.
     * @param start a valid double
     * @param end a valid double
     * @param a a double between 0 and 1 inclusive
     * @return a double between start and end inclusive
     */
    public static double cerp(final double start, final double end, double a) {
        return (1.0 - (a *= a * (3.0 - 2.0 * a))) * start + a * end;
    }

    /**
     * Cubic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively. Somewhat faster than
     * quintic interpolation ({@link #querp(float, float, float)}), but slower (and smoother) than
     * {@link #lerp(float, float, float)}.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between start and end inclusive
     */
    public static float cerp(final float start, final float end, float a) {
        return (1f - (a *= a * (3f - 2f * a))) * start + a * end;
    }
    /*
     * Quintic-interpolates between start and end (valid doubles), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid double, as in, not infinite or NaN
     * @param end a valid double, as in, not infinite or NaN
     * @param a a double between 0 and 1 inclusive
     * @return a double between x and y inclusive
     */
    public static double querp(final double start, final double end, double a){
        return (1.0 - (a *= a * a * (a * (a * 6.0 - 15.0) + 10.0))) * start + a * end;
    }
    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float, as in, not infinite or NaN
     * @param end a valid float, as in, not infinite or NaN
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    public static float querp(final float start, final float end, float a){
        return (1f - (a *= a * a * (a * (a * 6f - 15f) + 10f))) * start + a * end;
    }


    /**
     * Linear-interpolates between start and end (valid doubles), with a between 0 (yields start) and 1 (yields end).
     * @param start a valid double, as in, not infinite or NaN
     * @param end a valid double, as in, not infinite or NaN
     * @param a a double between 0 and 1 inclusive
     * @return a double between x and y inclusive
     */
    public static double lerp(final double start, final double end, final double a) {
        return (1.0 - a) * start + a * end;
    }

    /**
     * Linear-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * @param start a valid float, as in, not infinite or NaN
     * @param end a valid float, as in, not infinite or NaN
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    public static float lerp(final float start, final float end, final float a) {
        return (1f - a) * start + a * end;
    }

    /**
     * Given a float {@code a} from 0.0 to 1.0 (both inclusive), this gets a float that adjusts a to be closer to the
     * end points of that range (if less than 0.5, it gets closer to 0.0, otherwise it gets closer to 1.0).
     * @param a a float between 0.0f and 1.0f inclusive
     * @return a float between 0.0f and 1.0f inclusive that is more likely to be near the extremes
     */
    public static double emphasize(final double a)
    {
        return a * a * (3.0 - 2.0 * a);
    }
    /**
     * Given a float {@code a} from -1.0 to 1.0 (both inclusive), this gets a float that adjusts a to be closer to the
     * end points of that range (if less than 0, it gets closer to -1.0, otherwise it gets closer to 1.0).
     * <br>
     * Used by {@link ClassicNoise} and {@link  JitterNoise} to increase the frequency of high and low results, which
     * improves the behavior of {@link Ridged2D} and other Ridged noise when it uses those noise algorithms.
     * @param a a float between -1.0f and 1.0f inclusive
     * @return a float between -1.0f and 1.0f inclusive that is more likely to be near the extremes
     */
    public static double emphasizeSigned(double a)
    {         
        a = a * 0.5 + 0.5;
        return a * a * (6.0 - 4.0 * a) - 1.0;
    }

    /**
     * A group of similar methods for getting hashes of points based on long coordinates in 2, 3, 4, or 6 dimensions and
     * a long for state. This is organized how it is so it can be statically imported without also importing the rest
     * of Noise. Internally, all of the methods here are based on a simplified version of Hive from {@link CrossHash}
     * (the default algorithm in that class); this version does not include the somewhat-involved finalization step.
     * Omitting finalization helps this class be somewhat faster, but also makes it so it doesn't avalanche very well;
     * you can call {@link #avalanche(long)} on the result of a hashAll() call to put the finalization step back.
     */
    public static final class PointHash
    {
        /**
         * If it's important for some usage that one bit of change in any parameters cause roughly 50% of all bits in
         * the result to change, then you can call this on the output of any hashAll() overload to improve the bit
         * avalanche qualities.
         * @param state the output of hashAll() in this class
         * @return a significantly-mixed-around long obtained from state
         */
        public static long avalanche(long state)
        {
            state = (state ^ state >>> 33) * 0xFF51AFD7ED558CCDL;
            return (state ^ state >>> 33) * 0xC4CEB9FE1A85EC53L;
        }
        /**
         *
         * @param x
         * @param y
         * @param state
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(long x, long y, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;
            
//            return ((x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26 ^ 0x9183A1F4F348E683L) * (
//                    ((y = ((y *= 0x6C8E9CF570932BD5L) ^ y >>> 26 ^ 0x9183A1F4F348E683L) * (
//                            state * 0x9E3779B97F4A7C15L 
//                                    | 1L)) ^ y >>> 24) 
//                            | 1L)) ^ x >>> 24);

//            state = (state ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL | 1L;
//            x *= state;
//            y *= state;
//            return state ^ (x << 26 | x >>> 38) + (y << 23 | y >>> 41);

//        return ((x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26 ^ 0x9183A1F4F348E683L) * (
//                ((y = ((y *= 0x6C8E9CF570932BD5L) ^ y >>> 26 ^ 0x9183A1F4F348E683L) * (
//                                        state * 0x9E3779B97F4A7C15L
//                                | 1L)) ^ y >>> 24)
//                        | 1L)) ^ x >>> 24);

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24;
//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31);
            //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((y ^ state) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((x ^ state) | 0xA529L)) ^ (state >>> 22)));
        }

        /**
         *
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24
//                    ^ (z = ((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state) ^ z >>> 24;

//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ;

            //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
        }

        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param state
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24
//                    ^ (z = ((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state) ^ z >>> 24
//                    ^ (w = ((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state) ^ w >>> 24;

//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ;

            //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
        }

        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param u
         * @param v
         * @param state
         * @return 64-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
            return state ^ state >>> 31;

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24
//                    ^ (z = ((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state) ^ z >>> 24
//                    ^ (w = ((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state) ^ w >>> 24
//                    ^ (u = ((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state) ^ u >>> 24
//                    ^ (v = ((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state) ^ v >>> 24;

//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ;

            //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ u) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
        }
        /**
         *
         * @param x
         * @param y
         * @param state
         * @return 8-bit hash of the x,y point with the given state
         */         
        public static int hash256(long x, long y, long state) {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)) >>> 56);

//                return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                    ^ ((((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 56);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((y ^ state) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((x ^ state) | 0xA529L))) >>> 56);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 8-bit hash of the x,y,z point with the given state
         */
        public static int hash256(long x, long y, long z, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//            ) >>> 56);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 56);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param state
         * @return 8-bit hash of the x,y,z,w point with the given state
         */
        public static int hash256(long x, long y, long z, long w, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//            ) >>> 56);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param u
         * @param v
         * @param state
         * @return 8-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static int hash256(long x, long y, long z, long w, long u, long v, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//                    ^ (((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state)
//                    ^ (((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state)
//            ) >>> 56);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 56);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
        }
        /**
         *
         * @param x
         * @param y
         * @param state
         * @return 5-bit hash of the x,y point with the given state
         */
        public static int hash32(long x, long y, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);
            
//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 5-bit hash of the x,y,z point with the given state
         */
        public static int hash32(long x, long y, long z, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y - z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z - x) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x - y) | 0xA529L))) >>> 59);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param state
         * @return 5-bit hash of the x,y,z,w point with the given state
         */
        public static int hash32(long x, long y, long z, long w, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param u
         * @param v
         * @param state
         * @return 5-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static int hash32(long x, long y, long z, long w, long u, long v, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//                    ^ (((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state)
//                    ^ (((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
        }

        /**
         *
         * @param x
         * @param y
         * @param state
         * @return 6-bit hash of the x,y point with the given state
         */
        public static int hash64(long x, long y, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);


            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 58);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 6-bit hash of the x,y,z point with the given state
         */
        public static int hash64(long x, long y, long z, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y - z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z - x) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x - y) | 0xA529L))) >>> 58);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param state
         * @return 6-bit hash of the x,y,z,w point with the given state
         */
        public static int hash64(long x, long y, long z, long w, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 58);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param w
         * @param u
         * @param v
         * @param state
         * @return 6-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static int hash64(long x, long y, long z, long w, long u, long v, long state)
        {
            state *= 0x9E3779B97F4A7C15L;
            long other = 0x60642E2A34326F15L;
            state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            state = (state << 54 | state >>> 10);
            state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//                    ^ (((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state)
//                    ^ (((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);

            //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 58);
        }

    }

    /**
     * A group of similar methods for getting hashes of points based on long coordinates in 2, 3, 4, or 6 dimensions and
     * a long for state; like {@link PointHash} but optimized for speed rather than quality. This implementation has
     * high enough quality to be useful as a source of random numbers based on positions, but would likely not be a good
     * option in a hash table (or at least not as good as the tailored implementation of {@link Coord#hashCode()}, for
     * instance). At low dimensions, this is a little faster than {@link PointHash}, but this class doesn't slow down
     * much at all as more dimensions are used, while PointHash and most other implementations do slow down.
     * <br>
     * Also includes permutation tables for noise implementations that don't mind wrapping after 256 integer positions
     * have been hashed; these tables are based on <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.140.3594&rep=rep1&type=pdf">this paper</a>,
     * with credit to Andrew Kensler, Aaron Knoll and Peter Shirley.
     */
    public static final class HastyPointHash
    {
        // The 6 permutation tables were created with the code in the commented static block after these definitions.
        public static final int[]
                perm_x = {59, 146, 27, 99, 226, 210, 44, 129, 102, 237, 2, 107, 157, 173, 159, 16, 128, 41, 228, 114, 63, 105, 241, 144, 187, 116, 223, 122, 234, 52, 96, 35, 213, 176, 177, 141, 132, 240, 194, 163, 0, 3, 168, 133, 55, 203, 53, 50, 42, 79, 130, 156, 209, 135, 151, 178, 85, 154, 117, 148, 140, 82, 6, 69, 127, 214, 95, 175, 46, 30, 104, 197, 170, 33, 70, 167, 217, 233, 219, 84, 196, 109, 40, 190, 123, 165, 61, 212, 255, 184, 19, 182, 38, 112, 172, 103, 25, 244, 245, 201, 192, 60, 14, 231, 68, 71, 236, 193, 115, 7, 113, 118, 110, 131, 198, 216, 29, 195, 211, 246, 153, 222, 185, 208, 200, 158, 66, 137, 179, 26, 147, 235, 106, 90, 164, 9, 238, 101, 138, 227, 21, 37, 23, 152, 8, 161, 108, 250, 183, 225, 121, 24, 51, 252, 87, 242, 98, 188, 232, 171, 93, 56, 57, 5, 12, 120, 74, 43, 136, 139, 32, 13, 191, 67, 189, 186, 162, 199, 10, 20, 89, 15, 31, 58, 221, 18, 253, 28, 4, 218, 142, 205, 247, 94, 215, 39, 166, 150, 224, 77, 34, 169, 206, 47, 81, 97, 83, 220, 76, 229, 160, 54, 243, 45, 181, 92, 119, 48, 155, 62, 174, 248, 36, 239, 145, 124, 125, 65, 72, 180, 134, 111, 204, 207, 100, 73, 251, 143, 249, 254, 230, 11, 78, 80, 149, 75, 91, 126, 17, 86, 49, 88, 64, 22, 202, 1},
                perm_y = {189, 111, 17, 214, 57, 208, 191, 225, 241, 152, 145, 71, 2, 141, 183, 218, 66, 178, 34, 161, 198, 47, 200, 180, 134, 239, 162, 18, 155, 216, 192, 173, 219, 9, 51, 124, 95, 122, 217, 135, 31, 50, 179, 237, 32, 39, 209, 112, 96, 92, 68, 79, 228, 193, 234, 90, 164, 137, 196, 184, 185, 114, 226, 67, 249, 163, 85, 26, 125, 28, 251, 45, 61, 220, 213, 139, 70, 201, 243, 22, 142, 246, 102, 229, 10, 107, 4, 240, 194, 35, 230, 86, 223, 20, 12, 233, 23, 77, 119, 176, 147, 182, 21, 195, 91, 118, 247, 33, 100, 99, 29, 188, 172, 144, 136, 131, 40, 13, 38, 150, 224, 205, 8, 252, 253, 190, 46, 143, 53, 231, 153, 94, 177, 88, 55, 105, 121, 16, 25, 207, 138, 5, 63, 82, 202, 58, 170, 41, 78, 167, 64, 60, 14, 103, 42, 154, 19, 80, 72, 37, 83, 129, 187, 244, 215, 242, 81, 15, 151, 186, 59, 101, 168, 175, 89, 248, 232, 212, 204, 199, 108, 73, 98, 210, 44, 1, 76, 48, 49, 250, 106, 203, 113, 43, 221, 146, 245, 148, 115, 165, 181, 84, 93, 3, 206, 65, 123, 158, 6, 126, 238, 109, 130, 227, 140, 120, 74, 171, 110, 222, 87, 156, 132, 97, 159, 197, 255, 56, 27, 62, 157, 75, 211, 254, 127, 169, 236, 235, 149, 52, 36, 24, 0, 11, 160, 133, 174, 30, 104, 69, 128, 116, 117, 54, 166, 7},
                perm_z = {253, 212, 4, 237, 36, 182, 213, 233, 147, 239, 226, 41, 74, 65, 68, 165, 70, 231, 217, 116, 113, 193, 162, 112, 228, 254, 183, 176, 151, 80, 17, 60, 155, 246, 174, 3, 202, 208, 127, 7, 57, 1, 132, 79, 224, 99, 238, 195, 236, 9, 115, 154, 23, 227, 76, 158, 130, 16, 89, 214, 61, 114, 187, 90, 49, 24, 64, 33, 96, 242, 25, 37, 215, 35, 46, 109, 134, 141, 136, 225, 138, 43, 21, 184, 189, 13, 230, 188, 40, 50, 243, 244, 211, 156, 85, 120, 223, 58, 234, 71, 6, 28, 179, 67, 125, 69, 192, 131, 44, 175, 34, 15, 32, 77, 191, 222, 83, 47, 128, 218, 198, 84, 149, 26, 121, 190, 255, 150, 117, 92, 140, 101, 172, 62, 93, 97, 27, 103, 106, 161, 194, 201, 204, 45, 206, 111, 81, 252, 249, 73, 42, 248, 108, 118, 63, 56, 31, 216, 153, 180, 19, 126, 38, 139, 66, 88, 247, 143, 177, 137, 12, 199, 104, 235, 102, 75, 100, 129, 251, 18, 159, 107, 196, 22, 10, 152, 209, 94, 181, 250, 51, 210, 185, 144, 200, 169, 232, 122, 145, 173, 95, 171, 166, 229, 14, 5, 0, 105, 2, 163, 157, 48, 53, 133, 110, 52, 160, 186, 123, 124, 91, 20, 221, 240, 87, 178, 98, 207, 142, 148, 59, 203, 245, 205, 72, 11, 164, 39, 170, 135, 168, 197, 55, 86, 219, 167, 8, 82, 78, 220, 29, 146, 241, 54, 119, 30},
                perm_w = {57, 1, 140, 48, 61, 156, 230, 173, 2, 231, 12, 214, 142, 242, 255, 195, 198, 220, 157, 139, 194, 99, 247, 248, 155, 178, 29, 41, 23, 193, 0, 30, 95, 171, 174, 222, 91, 54, 8, 67, 32, 129, 46, 124, 172, 148, 17, 105, 228, 118, 191, 33, 224, 5, 25, 158, 185, 92, 63, 199, 53, 107, 34, 180, 125, 69, 200, 116, 121, 216, 42, 233, 70, 43, 72, 26, 202, 62, 51, 15, 10, 16, 217, 207, 14, 175, 59, 52, 223, 246, 89, 109, 83, 13, 68, 90, 147, 239, 234, 18, 151, 114, 76, 143, 100, 197, 106, 176, 232, 208, 85, 165, 40, 186, 251, 101, 44, 65, 93, 218, 253, 144, 123, 11, 113, 167, 102, 240, 177, 137, 4, 184, 181, 20, 110, 37, 138, 111, 132, 94, 6, 122, 119, 75, 78, 84, 21, 3, 74, 235, 127, 112, 19, 58, 149, 161, 159, 73, 136, 150, 215, 35, 38, 86, 211, 190, 128, 203, 168, 9, 166, 244, 36, 28, 153, 225, 108, 254, 55, 169, 104, 141, 145, 22, 49, 212, 183, 79, 189, 227, 170, 60, 245, 205, 64, 252, 241, 80, 162, 97, 206, 163, 192, 146, 66, 182, 187, 135, 130, 152, 81, 71, 134, 39, 179, 188, 87, 126, 209, 229, 219, 133, 204, 210, 27, 103, 98, 154, 31, 250, 196, 7, 236, 77, 226, 56, 96, 88, 221, 45, 160, 50, 115, 237, 164, 201, 213, 82, 117, 24, 243, 131, 249, 47, 238, 120},
                perm_u = {132, 148, 19, 244, 162, 163, 194, 37, 4, 250, 198, 154, 170, 137, 6, 60, 123, 73, 138, 41, 145, 92, 61, 82, 251, 175, 57, 207, 153, 50, 113, 105, 106, 242, 253, 94, 128, 9, 164, 143, 234, 80, 160, 252, 136, 239, 232, 150, 89, 167, 100, 131, 127, 178, 31, 188, 217, 5, 27, 33, 119, 152, 83, 195, 72, 88, 223, 176, 110, 111, 134, 233, 200, 190, 130, 86, 102, 69, 202, 240, 63, 13, 70, 229, 93, 24, 241, 22, 191, 99, 245, 139, 85, 254, 53, 45, 46, 182, 185, 26, 76, 197, 104, 67, 174, 20, 108, 184, 68, 171, 172, 90, 29, 107, 32, 79, 59, 126, 211, 112, 157, 201, 215, 237, 51, 84, 23, 135, 12, 28, 155, 124, 42, 43, 74, 173, 140, 114, 78, 18, 34, 1, 142, 180, 243, 193, 2, 161, 25, 212, 181, 218, 115, 39, 177, 71, 17, 186, 249, 225, 226, 122, 117, 214, 8, 129, 44, 7, 98, 216, 40, 116, 0, 103, 96, 30, 209, 47, 236, 11, 247, 58, 151, 52, 81, 141, 147, 169, 255, 16, 219, 75, 192, 208, 87, 56, 230, 231, 14, 97, 64, 54, 10, 222, 238, 205, 66, 120, 183, 133, 206, 109, 213, 144, 121, 158, 55, 235, 125, 3, 221, 118, 189, 165, 166, 62, 49, 146, 196, 77, 224, 203, 38, 156, 228, 48, 204, 35, 36, 210, 149, 227, 168, 199, 179, 246, 91, 248, 21, 65, 95, 101, 187, 220, 159, 15},
                perm_v = {2, 9, 4, 237, 219, 73, 247, 203, 228, 220, 46, 229, 61, 156, 170, 75, 223, 144, 81, 252, 172, 208, 76, 218, 177, 103, 123, 244, 14, 39, 255, 90, 168, 43, 174, 3, 113, 107, 145, 233, 130, 254, 192, 11, 211, 190, 68, 105, 117, 178, 251, 18, 66, 242, 230, 248, 95, 137, 29, 26, 164, 65, 153, 120, 70, 77, 64, 33, 23, 133, 59, 7, 40, 16, 106, 41, 121, 216, 238, 135, 19, 212, 157, 48, 232, 28, 128, 22, 245, 171, 183, 56, 74, 99, 51, 150, 236, 111, 234, 71, 189, 167, 213, 37, 198, 50, 12, 79, 31, 250, 136, 165, 185, 246, 55, 86, 142, 62, 42, 52, 147, 205, 89, 94, 224, 141, 221, 180, 138, 129, 140, 101, 83, 193, 127, 67, 108, 84, 166, 109, 181, 20, 34, 195, 87, 24, 217, 116, 36, 88, 196, 82, 57, 239, 243, 124, 134, 175, 119, 210, 32, 163, 38, 139, 249, 227, 25, 97, 10, 118, 72, 131, 91, 54, 204, 225, 253, 58, 115, 154, 202, 122, 110, 112, 215, 1, 149, 146, 44, 201, 17, 240, 206, 197, 200, 169, 159, 13, 179, 143, 160, 152, 226, 161, 241, 80, 102, 15, 155, 92, 21, 184, 96, 148, 8, 158, 125, 35, 63, 176, 194, 235, 187, 30, 100, 231, 98, 207, 53, 47, 93, 173, 78, 186, 132, 199, 151, 114, 0, 45, 49, 126, 191, 222, 6, 182, 162, 188, 27, 69, 209, 214, 104, 5, 85, 60};

//    static {
//        int s = 1;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 53 + 3 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 7;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 61 + 11 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 31;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 29 + 111 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 127;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 101 + 31 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 15;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 37 + 97 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 63;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 109 + 47 & 255) ^ s >>> 4) + ", ");
//        }
//
//    }



        /**
         * Gets a 64-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param s the state/seed; any long
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(long x, long y, long s) {
            y += s * 0xD1B54A32D192ED03L;
            x += y * 0xABC98388FB8FAC03L;
            s += x * 0x8CB92BA72F3D8DD7L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param s the state/seed; any long
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(long x, long y, long z, long s) {
            z += s * 0xDB4F0B9175AE2165L;
            y += z * 0xBBE0563303A4615FL;
            x += y * 0xA0F2EC75A1FE1575L;
            s += x * 0x89E182857D9ED689L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param s the state; any long
         * @return 64-bit hash of the x,y,z,w point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long s) {
            w += s * 0xE19B01AA9D42C633L;
            z += w * 0xC6D1D6C8ED0C9631L;
            y += z * 0xAF36D01EF7518DBBL;
            x += y * 0x9A69443F36F710E7L;
            s += x * 0x881403B9339BD42DL;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }

        /**
         * Gets a 64-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
         * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param u u position; any long
         * @param v v position; any long
         * @param s the state; any long
         * @return 64-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static long hashAll(long x, long y, long z, long w, long u, long v, long s) {
            v += s * 0xE95E1DD17D35800DL;
            u += v * 0xD4BC74E13F3C782FL;
            w += u * 0xC1EDBC5B5C68AC25L;
            z += w * 0xB0C8AC50F0EDEF5DL;
            y += z * 0xA127A31C56D1CDB5L;
            x += y * 0x92E852C80D153DB3L;
            s += x * 0x85EB75C3024385C3L;
            return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
        }
        /**
         * Gets an 8-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param s the state/seed; any long
         * @return 8-bit hash of the x,y point with the given state
         */
        public static int hash256(long x, long y, long s) {
            y += s * 0xD1B54A32D192ED03L;
            x += y * 0xABC98388FB8FAC03L;
            s += x * 0x8CB92BA72F3D8DD7L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
        }

        /**
         * Gets an 8-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param s the state/seed; any long
         * @return 8-bit hash of the x,y,z point with the given state
         */
        public static int hash256(long x, long y, long z, long s) {
            z += s * 0xDB4F0B9175AE2165L;
            y += z * 0xBBE0563303A4615FL;
            x += y * 0xA0F2EC75A1FE1575L;
            s += x * 0x89E182857D9ED689L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
        }

        /**
         * Gets an 8-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param s the state; any long
         * @return 8-bit hash of the x,y,z,w point with the given state
         */
        public static int hash256(long x, long y, long z, long w, long s) {
            w += s * 0xE19B01AA9D42C633L;
            z += w * 0xC6D1D6C8ED0C9631L;
            y += z * 0xAF36D01EF7518DBBL;
            x += y * 0x9A69443F36F710E7L;
            s += x * 0x881403B9339BD42DL;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
        }

        /**
         * Gets an 8-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
         * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param u u position; any long
         * @param v v position; any long
         * @param s the state; any long
         * @return 8-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static int hash256(long x, long y, long z, long w, long u, long v, long s) {
            v += s * 0xE95E1DD17D35800DL;
            u += v * 0xD4BC74E13F3C782FL;
            w += u * 0xC1EDBC5B5C68AC25L;
            z += w * 0xB0C8AC50F0EDEF5DL;
            y += z * 0xA127A31C56D1CDB5L;
            x += y * 0x92E852C80D153DB3L;
            s += x * 0x85EB75C3024385C3L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
        }
        /**
         * Gets a 5-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param s the state/seed; any long
         * @return 5-bit hash of the x,y point with the given state
         */
        public static int hash32(long x, long y, long s) {
            y += s * 0xD1B54A32D192ED03L;
            x += y * 0xABC98388FB8FAC03L;
            s += x * 0x8CB92BA72F3D8DD7L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
        }

        /**
         * Gets a 5-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param s the state/seed; any long
         * @return 5-bit hash of the x,y,z point with the given state
         */
        public static int hash32(long x, long y, long z, long s) {
            z += s * 0xDB4F0B9175AE2165L;
            y += z * 0xBBE0563303A4615FL;
            x += y * 0xA0F2EC75A1FE1575L;
            s += x * 0x89E182857D9ED689L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
        }

        /**
         * Gets a 5-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param s the state; any long
         * @return 5-bit hash of the x,y,z,w point with the given state
         */
        public static int hash32(long x, long y, long z, long w, long s) {
            w += s * 0xE19B01AA9D42C633L;
            z += w * 0xC6D1D6C8ED0C9631L;
            y += z * 0xAF36D01EF7518DBBL;
            x += y * 0x9A69443F36F710E7L;
            s += x * 0x881403B9339BD42DL;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
        }

        /**
         * Gets a 5-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
         * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param u u position; any long
         * @param v v position; any long
         * @param s the state; any long
         * @return 5-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static int hash32(long x, long y, long z, long w, long u, long v, long s) {
            v += s * 0xE95E1DD17D35800DL;
            u += v * 0xD4BC74E13F3C782FL;
            w += u * 0xC1EDBC5B5C68AC25L;
            z += w * 0xB0C8AC50F0EDEF5DL;
            y += z * 0xA127A31C56D1CDB5L;
            x += y * 0x92E852C80D153DB3L;
            s += x * 0x85EB75C3024385C3L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
        }
        
        /**
         * Gets a 6-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param s the state/seed; any long
         * @return 6-bit hash of the x,y point with the given state
         */
        public static int hash64(long x, long y, long s) {
            y += s * 0xD1B54A32D192ED03L;
            x += y * 0xABC98388FB8FAC03L;
            s += x * 0x8CB92BA72F3D8DD7L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
        }

        /**
         * Gets a 6-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param s the state/seed; any long
         * @return 6-bit hash of the x,y,z point with the given state
         */
        public static int hash64(long x, long y, long z, long s) {
            z += s * 0xDB4F0B9175AE2165L;
            y += z * 0xBBE0563303A4615FL;
            x += y * 0xA0F2EC75A1FE1575L;
            s += x * 0x89E182857D9ED689L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
        }

        /**
         * Gets a 6-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
         * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param s the state; any long
         * @return 6-bit hash of the x,y,z,w point with the given state
         */
        public static int hash64(long x, long y, long z, long w, long s) {
            w += s * 0xE19B01AA9D42C633L;
            z += w * 0xC6D1D6C8ED0C9631L;
            y += z * 0xAF36D01EF7518DBBL;
            x += y * 0x9A69443F36F710E7L;
            s += x * 0x881403B9339BD42DL;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
        }

        /**
         * Gets a 6-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
         * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
         * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
         * <br>
         * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
         * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
         * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
         * generalized ratio. See
         * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
         * for some more information on how he uses this, but we do things differently because we want random-seeming
         * results instead of separated sub-random results.
         * @param x x position; any long
         * @param y y position; any long
         * @param z z position; any long
         * @param w w position; any long
         * @param u u position; any long
         * @param v v position; any long
         * @param s the state; any long
         * @return 6-bit hash of the x,y,z,w,u,v point with the given state
         */
        public static int hash64(long x, long y, long z, long w, long u, long v, long s) {
            v += s * 0xE95E1DD17D35800DL;
            u += v * 0xD4BC74E13F3C782FL;
            w += u * 0xC1EDBC5B5C68AC25L;
            z += w * 0xB0C8AC50F0EDEF5DL;
            y += z * 0xA127A31C56D1CDB5L;
            x += y * 0x92E852C80D153DB3L;
            s += x * 0x85EB75C3024385C3L;
            return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
        }

        public static int hash256_alt(final long x, final long y, final long z, final long w, final long seed) {
            long a = 0x632BE59BD9B4E019L;
            return (int) ((0x9E3779B97F4A7C15L
                    + (a ^= 0x85157AF5L * seed + x)
                    + (a ^= 0x85157AF5L * x + y)
                    + (a ^= 0x85157AF5L * y + z)
                    + (a ^= 0x85157AF5L * z + w)
                    + (a ^= 0x85157AF5L * w + seed)) * a >>> 56);
        }

    }

    public static final class IntPointHash {
        /**
         * A 32-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 32-bit hash of the x,y point with the given state s
         */
        public static int hashAll(int x, int y, int s) {
            s ^= 0x1827F5 * (x ^ y * 0x123C21);
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
        }
        /**
         * A 32-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 32-bit hash of the x,y,z point with the given state s
         */
        public static int hashAll(int x, int y, int z, int s) {
            s ^= 0x1A36A9 * (x ^ 0x157931 * (y ^ z * 0x119725));
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
        }

        /**
         * A 32-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 32-bit hash of the x,y,z,w point with the given state s
         */
        public static int hashAll(int x, int y, int z, int w, int s) {
            s ^= 0x1B69E1 * (x ^ 0x177C0B * (y ^ 0x141E5D * (z ^ w * 0x113C31)));
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
        }

        /**
         * A 32-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
         * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
         * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
         * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
         * hash used here has been stripped down heavily, both for speed and because unless points are selected
         * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
         * rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param u u position, as an int
         * @param v v position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point 
         * @return 32-bit hash of the x,y,z,w,u,v point with the given state s
         */
        public static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
            s ^= 0x1CC1C5 * (x ^ 0x19D7AF * (y ^ 0x173935 * (z ^ 0x14DEAF * (w ^ 0x12C139 * (u ^ v * 0x10DAA3)))));
            return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ s >>> 15;
        }

        /**
         * A 8-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 8-bit hash of the x,y point with the given state s
         */
        public static int hash256(int x, int y, int s) {
            s ^= 0x1827F5 * (x ^ y * 0x123C21);
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 24;
        }
        /**
         * A 8-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 8-bit hash of the x,y,z point with the given state s
         */
        public static int hash256(int x, int y, int z, int s) {
            s ^= 0x1A36A9 * (x ^ 0x157931 * (y ^ z * 0x119725));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 24;
        }

        /**
         * A 8-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 8-bit hash of the x,y,z,w point with the given state s
         */
        public static int hash256(int x, int y, int z, int w, int s) {
            s ^= 0x1B69E1 * (x ^ 0x177C0B * (y ^ 0x141E5D * (z ^ w * 0x113C31)));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 24;
        }

        /**
         * A 8-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
         * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
         * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
         * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
         * hash used here has been stripped down heavily, both for speed and because unless points are selected
         * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
         * rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param u u position, as an int
         * @param v v position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point 
         * @return 8-bit hash of the x,y,z,w,u,v point with the given state s
         */
        public static int hash256(int x, int y, int z, int w, int u, int v, int s) {
            s ^= 0x1CC1C5 * (x ^ 0x19D7AF * (y ^ 0x173935 * (z ^ 0x14DEAF * (w ^ 0x12C139 * (u ^ v * 0x10DAA3)))));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 24;
        }

        /**
         * A 6-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 6-bit hash of the x,y point with the given state s
         */
        public static int hash64(int x, int y, int s) {
            s ^= 0x1827F5 * (x ^ y * 0x123C21);
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 26;
        }
        /**
         * A 6-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 6-bit hash of the x,y,z point with the given state s
         */
        public static int hash64(int x, int y, int z, int s) {
            s ^= 0x1A36A9 * (x ^ 0x157931 * (y ^ z * 0x119725));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 26;
        }

        /**
         * A 6-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 6-bit hash of the x,y,z,w point with the given state s
         */
        public static int hash64(int x, int y, int z, int w, int s) {
            s ^= 0x1B69E1 * (x ^ 0x177C0B * (y ^ 0x141E5D * (z ^ w * 0x113C31)));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 26;
        }

        /**
         * A 6-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
         * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
         * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
         * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
         * hash used here has been stripped down heavily, both for speed and because unless points are selected
         * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
         * rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param u u position, as an int
         * @param v v position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point 
         * @return 6-bit hash of the x,y,z,w,u,v point with the given state s
         */
        public static int hash64(int x, int y, int z, int w, int u, int v, int s) {
            s ^= 0x1CC1C5 * (x ^ 0x19D7AF * (y ^ 0x173935 * (z ^ 0x14DEAF * (w ^ 0x12C139 * (u ^ v * 0x10DAA3)))));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 26;
        }
        /**
         * A 5-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 5-bit hash of the x,y point with the given state s
         */
        public static int hash32(int x, int y, int s) {
            s ^= 0x1827F5 * (x ^ y * 0x123C21);
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 27;
        }
        /**
         * A 5-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 5-bit hash of the x,y,z point with the given state s
         */
        public static int hash32(int x, int y, int z, int s) {
            s ^= 0x1A36A9 * (x ^ 0x157931 * (y ^ z * 0x119725));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 27;
        }

        /**
         * A 5-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
         * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
         * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
         * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
         * been stripped down heavily, both for speed and because unless points are selected specifically to target
         * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point
         * @return 5-bit hash of the x,y,z,w point with the given state s
         */
        public static int hash32(int x, int y, int z, int w, int s) {
            s ^= 0x1B69E1 * (x ^ 0x177C0B * (y ^ 0x141E5D * (z ^ w * 0x113C31)));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 27;
        }

        /**
         * A 5-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
         * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
         * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
         * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
         * hash used here has been stripped down heavily, both for speed and because unless points are selected
         * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
         * rrxmrrxmsx_0 has.
         * @param x x position, as an int
         * @param y y position, as an int
         * @param z z position, as an int
         * @param w w position, as an int
         * @param u u position, as an int
         * @param v v position, as an int
         * @param s any int, a seed to be able to produce many hashes for a given point 
         * @return 5-bit hash of the x,y,z,w,u,v point with the given state s
         */
        public static int hash32(int x, int y, int z, int w, int u, int v, int s) {
            s ^= 0x1CC1C5 * (x ^ 0x19D7AF * (y ^ 0x173935 * (z ^ 0x14DEAF * (w ^ 0x12C139 * (u ^ v * 0x10DAA3)))));
            return (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17 >>> 27;
        }


    }
    
    public interface Noise1D {
        double getNoise(double x);
        double getNoiseWithSeed(double x, long seed);
    }

    public interface Noise2D {
        double getNoise(double x, double y);
        double getNoiseWithSeed(double x, double y, long seed);
    }

    public interface Noise3D {
        double getNoise(double x, double y, double z);
        double getNoiseWithSeed(double x, double y, double z, long seed);
    }

    public interface Noise4D {
        double getNoise(double x, double y, double z, double w);
        double getNoiseWithSeed(double x, double y, double z, double w, long seed);
    }

    public interface Noise6D {
        double getNoise(double x, double y, double z, double w, double u, double v);
        double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed);
    }

    public static class Layered1D implements Noise1D {
        protected int octaves;
        protected Noise1D basis;
        public double frequency;
        public double lacunarity;
        public Layered1D() {
            this(Basic1D.instance);
        }

        public Layered1D(Noise1D basis) {
            this(basis, 2);
        }

        public Layered1D(Noise1D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered1D(Noise1D basis, final int octaves, double frequency) {
            this(basis, octaves, frequency, 0.5);
        }
        public Layered1D(Noise1D basis, final int octaves, double frequency, double lacunarity) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
            this.lacunarity = lacunarity;
        }


        @Override
        public double getNoise(double x) {
            x *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            x *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }

    public static class Layered2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis;
        public double frequency;
        public double lacunarity;
        public Layered2D() {
            this(SeededNoise.instance);
        }

        public Layered2D(Noise2D basis) {
            this(basis, 2);
        }

        public Layered2D(Noise2D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered2D(Noise2D basis, final int octaves, double frequency) {
            this(basis, octaves, frequency, 0.5);
        }
        public Layered2D(Noise2D basis, final int octaves, double frequency, double lacunarity) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y) {
            x *= frequency;
            y *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
            x *= frequency;
            y *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis;
        public double frequency;
        public double lacunarity;
        public Layered3D() {
            this(SeededNoise.instance);
        }

        public Layered3D(Noise3D basis) {
            this(basis, 2);
        }

        public Layered3D(Noise3D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered3D(Noise3D basis, final int octaves, double frequency) {
            this(basis, octaves, frequency, 0.5);
        }
        public Layered3D(Noise3D basis, final int octaves, double frequency, double lacunarity) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis;
        public double frequency;
        public double lacunarity;
        public Layered4D() {
            this(SeededNoise.instance);
        }

        public Layered4D(Noise4D basis) {
            this(basis, 2);
        }

        public Layered4D(Noise4D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered4D(Noise4D basis, final int octaves, double frequency) {
            this(basis, octaves, frequency, 0.5);
        }
        public Layered4D(Noise4D basis, final int octaves, double frequency, double lacunarity) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z, double w) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s, w * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis;
        public double frequency;
        public double lacunarity;
        public Layered6D() {
            this(SeededNoise.instance);
        }

        public Layered6D(Noise6D basis) {
            this(basis, 2);
        }

        public Layered6D(Noise6D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered6D(Noise6D basis, final int octaves, double frequency) {
            this(basis, octaves, frequency, 0.5);
        }
        public Layered6D(Noise6D basis, final int octaves, double frequency, double lacunarity) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z, double w, double u, double v) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                        , w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s
                        , w * i_s, u * i_s, v * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class InverseLayered1D implements Noise1D {
        protected int octaves;
        protected Noise1D basis;
        public double frequency;
        /**
         * A multiplier that affects how much the frequency changes with each layer; the default is 0.5 .
         */
        public double lacunarity = 0.5;
        public InverseLayered1D() {
            this(Basic1D.instance);
        }

        public InverseLayered1D(Noise1D basis) {
            this(basis, 2);
        }

        public InverseLayered1D(Noise1D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public InverseLayered1D(Noise1D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        public InverseLayered1D(Noise1D basis, final int octaves, double frequency, double lacunarity){
            this(basis, octaves, frequency);
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x) {
            x *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            x *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }

    public static class InverseLayered2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis;
        public double frequency;
        /**
         * A multiplier that affects how much the frequency changes with each layer; the default is 0.5 .
         */
        public double lacunarity = 0.5;
        public InverseLayered2D() {
            this(SeededNoise.instance, 2);
        }

        public InverseLayered2D(Noise2D basis) {
            this(basis, 2);
        }

        public InverseLayered2D(Noise2D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public InverseLayered2D(Noise2D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        public InverseLayered2D(Noise2D basis, final int octaves, double frequency, double lacunarity){
            this(basis, octaves, frequency);
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y) {
            x *= frequency;
            y *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
            x *= frequency;
            y *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class InverseLayered3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis;
        public double frequency;
        /**
         * A multiplier that affects how much the frequency changes with each layer; the default is 0.5 .
         */
        public double lacunarity = 0.5;
        public InverseLayered3D() {
            this(SeededNoise.instance, 2);
        }

        public InverseLayered3D(Noise3D basis) {
            this(basis, 2);
        }

        public InverseLayered3D(Noise3D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public InverseLayered3D(Noise3D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        public InverseLayered3D(Noise3D basis, final int octaves, double frequency, double lacunarity){
            this(basis, octaves, frequency);
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class InverseLayered4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis;
        public double frequency;
        /**
         * A multiplier that affects how much the frequency changes with each layer; the default is 0.5 .
         */
        public double lacunarity = 0.5;
        public InverseLayered4D() {
            this(SeededNoise.instance, 2);
        }

        public InverseLayered4D(Noise4D basis) {
            this(basis, 2);
        }

        public InverseLayered4D(Noise4D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public InverseLayered4D(Noise4D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        public InverseLayered4D(Noise4D basis, final int octaves, double frequency, double lacunarity){
            this(basis, octaves, frequency);
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z, double w) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s, w * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class InverseLayered6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis;
        public double frequency;
        /**
         * A multiplier that affects how much the frequency changes with each layer; the default is 0.5 .
         */
        public double lacunarity = 0.5;
        public InverseLayered6D() {
            this(SeededNoise.instance, 2);
        }

        public InverseLayered6D(Noise6D basis) {
            this(basis, 2);
        }

        public InverseLayered6D(Noise6D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public InverseLayered6D(Noise6D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        public InverseLayered6D(Noise6D basis, final int octaves, double frequency, double lacunarity){
            this(basis, octaves, frequency);
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z, double w, double u, double v) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                        , w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s
                        , w * i_s, u * i_s, v * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }


    public static class Scaled1D implements Noise1D {
        protected double scaleX;
        protected Noise1D basis;

        public Scaled1D() {
            this(Basic1D.instance);
        }

        public Scaled1D(Noise1D basis) {
            this(basis, 2.0);
        }

        public Scaled1D(Noise1D basis, final double scaleX) {
            this.basis = basis;
            this.scaleX = scaleX;
        }

        @Override
        public double getNoise(final double x) {
            return basis.getNoise(x * scaleX);
        }

        @Override
        public double getNoiseWithSeed(final double x, long seed) {
            return basis.getNoiseWithSeed(x * scaleX, seed);
        }
    }

    public static class Scaled2D implements Noise2D {
        protected double scaleX, scaleY;
        protected Noise2D basis;

        public Scaled2D() {
            this(SeededNoise.instance);
        }

        public Scaled2D(Noise2D basis) {
            this(basis, 2.0, 2.0);
        }

        public Scaled2D(Noise2D basis, double scale) {
            this(basis, scale, scale);
        }

        public Scaled2D(Noise2D basis, final double scaleX, final double scaleY) {
            this.basis = basis;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
        }

        @Override
        public double getNoise(final double x, final double y) {
            return basis.getNoise(x * scaleX, y * scaleY);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, long seed) {
            return basis.getNoiseWithSeed(x * scaleX, y * scaleY, seed);
        }
    }
    public static class Scaled3D implements Noise3D {
        protected double scaleX, scaleY, scaleZ;
        protected Noise3D basis;

        public Scaled3D() {
            this(SeededNoise.instance);
        }

        public Scaled3D(Noise3D basis) {
            this(basis, 2.0, 2.0, 2.0);
        }

        public Scaled3D(Noise3D basis, double scale) {
            this(basis, scale, scale, scale);
        }

        public Scaled3D(Noise3D basis, final double scaleX, final double scaleY, final double scaleZ) {
            this.basis = basis;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
        }

        @Override
        public double getNoise(final double x, final double y, final double z) {
            return basis.getNoise(x * scaleX, y * scaleY, z * scaleZ);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, long seed) {
            return basis.getNoiseWithSeed(x * scaleX, y * scaleY, z * scaleZ, seed);
        }
    }
    public static class Scaled4D implements Noise4D {
        protected double scaleX, scaleY, scaleZ, scaleW;
        protected Noise4D basis;

        public Scaled4D() {
            this(SeededNoise.instance);
        }

        public Scaled4D(Noise4D basis) {
            this(basis, 2.0, 2.0, 2.0, 2.0);
        }

        public Scaled4D(Noise4D basis, double scale) {
            this(basis, scale, scale, scale, scale);
        }

        public Scaled4D(Noise4D basis, final double scaleX, final double scaleY, final double scaleZ, final double scaleW) {
            this.basis = basis;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
            this.scaleW = scaleW;
        }

        @Override
        public double getNoise(final double x, final double y, final double z, final double w) {
            return basis.getNoise(x * scaleX, y * scaleY, z * scaleZ, w * scaleW);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, long seed) {
            return basis.getNoiseWithSeed(x * scaleX, y * scaleY, z * scaleZ, w * scaleW, seed);
        }
    }
    public static class Scaled6D implements Noise6D {
        protected double scaleX, scaleY, scaleZ, scaleW, scaleU, scaleV;
        protected Noise6D basis;

        public Scaled6D() {
            this(SeededNoise.instance);
        }

        public Scaled6D(Noise6D basis) {
            this(basis, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0);
        }

        public Scaled6D(Noise6D basis, double scale) {
            this(basis, scale, scale, scale, scale, scale, scale);
        }

        public Scaled6D(Noise6D basis, final double scaleX, final double scaleY, final double scaleZ,
                        final double scaleW, final double scaleU, final double scaleV) {
            this.basis = basis;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
            this.scaleW = scaleW;
            this.scaleU = scaleU;
            this.scaleV = scaleV;
        }

        @Override
        public double getNoise(final double x, final double y, final double z, final double w,
                               final double u, final double v) {
            return basis.getNoise(x * scaleX, y * scaleY, z * scaleZ, w * scaleW, u * scaleU, v * scaleV);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w,
                                       final double u, final double v, long seed) {
            return basis.getNoiseWithSeed(x * scaleX, y * scaleY, z * scaleZ, w * scaleW, u * scaleU,
                    v * scaleV, seed);
        }
    }

    public static class Ridged2D implements Noise2D {
        protected int octaves;
        public double frequency;
        protected double correct;
        protected Noise2D basis;

        public Ridged2D() {
            this(SeededNoise.instance, 2, 1.25);
        }

        public Ridged2D(Noise2D basis) {
            this(basis, 2, 1.25);
        }

        public Ridged2D(Noise2D basis, int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            setOctaves(octaves);
        }

        public void setOctaves(int octaves)
        {
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            correct = 1.0;
            for (int o = 1; o < octaves; o++) {
                correct += Math.pow(2.0, -o);
            }
            correct = 2.0 / correct;
        }


        @Override
        public double getNoise(double x, double y) {
            double sum = 0, amp = 1.0;
            x *= frequency;
            y *= frequency;
            for (int i = 0; i < octaves; ++i) {
                double n = basis.getNoise(x + (i << 6), y + (i << 7));
                n = 1.0 - Math.abs(n);
                sum += amp * n;
                amp *= 0.5;
                x *= 2.0;
                y *= 2.0;
            }
            return sum * correct - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
            double sum = 0, amp = 1.0;
            x *= frequency;
            y *= frequency;
            for (int i = 0; i < octaves; ++i) {
                double n = basis.getNoiseWithSeed(x, y, (seed += 0x9E3779B97F4A7C15L));
                n = 1.0 - Math.abs(n);
                sum += amp * n;
                amp *= 0.5;
                x *= 2.0;
                y *= 2.0;
            }
            return sum * correct - 1.0;
        }
    }

    public static class Ridged3D implements Noise3D {
        protected int octaves;
        public double frequency;
        protected double correct;
        protected Noise3D basis;
        public Ridged3D() {
            this(SeededNoise.instance, 2, 1.25);
        }

        public Ridged3D(Noise3D basis) {
            this(basis, 2, 1.25);
        }

        public Ridged3D(Noise3D basis, int octaves) {
            this(basis, octaves, 1.25);
        }
        public Ridged3D(Noise3D basis, int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            setOctaves(octaves);
        }
        public void setOctaves(int octaves)
        {
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            correct = 1.0;
            for (int o = 1; o < octaves; o++) {
                correct += Math.pow(2.0, -o);
            }
            correct = 2.0 / correct;
        }

        @Override
        public double getNoise(double x, double y, double z) {
            double sum = 0, amp = 1.0;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            for (int i = 0; i < octaves; ++i) {
                double n = basis.getNoise(x + (i << 6), y + (i << 7), z + (i << 8));
                n = 1.0 - Math.abs(n);
                sum += amp * n;
                amp *= 0.5;
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
            }
            return sum * correct - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, long seed) {
            double sum = 0, amp = 1.0;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            for (int i = 0; i < octaves; ++i) {
                double n = basis.getNoiseWithSeed(x, y, z, (seed += 0x9E3779B97F4A7C15L));
                n = 1.0 - Math.abs(n);
                sum += amp * n;
                amp *= 0.5;
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
            }
            return sum * correct - 1.0;
        }
    }


    public static class Ridged4D implements Noise4D {
        public double exp[];
        protected int octaves;
        public double frequency, correct;
        public Noise4D basis;

        public Ridged4D() {
            this(SeededNoise.instance, 2, 1.25);
        }

        public Ridged4D(Noise4D basis) {
            this(basis, 2, 1.25);
        }

        public Ridged4D(Noise4D basis, int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            setOctaves(octaves);
        }
        public void setOctaves(int octaves)
        {
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            exp = new double[octaves];
            double maxvalue = 0.0;
            for (int i = 0; i < octaves; ++i) {
                maxvalue += (exp[i] = Math.pow(2.0, -i));
            }
            correct = 2.0 / maxvalue;
        }

        @Override
        public double getNoise(double x, double y, double z, double w) {
            double sum = 0.0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            for (int i = 0; i < octaves; ++i) {
                n = basis.getNoise(x + (i << 6), y + (i << 7), z + (i << 8), w + (i << 9));
                n = 1.0 - Math.abs(n);
                sum += n * n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
            }
            return sum * correct - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
            double sum = 0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            for (int i = 0; i < octaves; ++i) {
                n = basis.getNoiseWithSeed(x, y, z, w, (seed += 0x9E3779B97F4A7C15L));
                n = 1.0 - Math.abs(n);
                sum += n * n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
            }
            return sum * correct - 1.0;
        }
    }


    public static class Ridged6D implements Noise6D
    {
        protected double[] exp;
        protected int octaves;
        public double frequency, correct;
        public Noise6D basis;
        public Ridged6D()
        {
            this(SeededNoise.instance, 2, 1.25);
        }
        public Ridged6D(Noise6D basis)
        {
            this(basis, 2, 1.25);
        }
        public Ridged6D(Noise6D basis, int octaves, double frequency)
        {
            this.basis = basis;
            this.frequency = frequency;
            setOctaves(octaves);
        }
        public void setOctaves(int octaves)
        {
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            exp = new double[octaves];
            double maxvalue = 0.0;
            for (int i = 0; i < octaves; ++i) {
                maxvalue += (exp[i] = Math.pow(2.0, -i));
            }
            correct = 2.0 / maxvalue;
        }

        @Override
        public double getNoise(double x, double y, double z, double w, double u, double v) {
            double sum = 0.0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;

            for (int i = 0; i < octaves; ++i) {
                n = basis.getNoise(x + (i << 6), y + (i << 7), z + (i << 8), w + (i << 9), u + (i << 10), v + (i << 11));
                n = 1.0 - Math.abs(n);
                sum += n * n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
                u *= 2.0;
                v *= 2.0;
            }
            return sum * correct - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
            double sum = 0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            for (int i = 0; i < octaves; ++i) {
                n = basis.getNoiseWithSeed(x, y, z,
                        w, u, v, (seed += 0x9E3779B97F4A7C15L));
                n = 1.0 - Math.abs(n);
                sum += n * n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
                u *= 2.0;
                v *= 2.0;
            }
            return sum * correct - 1.0;
        }
    }

    public static class Turbulent2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis, disturbance;
        public double frequency;
        public final double correct;

        public Turbulent2D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent2D(Noise2D basis, Noise2D disturb) {
            this(basis, disturb, 1);
        }

        public Turbulent2D(Noise2D basis, Noise2D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Turbulent2D(Noise2D basis, Noise2D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
            correct = 1.0 / ((1 << this.octaves) - 1.0);
        }

        @Override
        public double getNoise(double x, double y) {
            x *= frequency;
            y *= frequency;
            x += disturbance.getNoise(x, y);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7)) * s;
            }
            return n * correct;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
            x *= frequency;
            y *= frequency;
            x += disturbance.getNoiseWithSeed(x, y, seed);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoiseWithSeed(x * i_s, y * i_s, seed += 0x9E3779B97F4A7C15L) * s;
            }
            return n * correct;
        }
    }
    public static class Turbulent3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis, disturbance;
        public double frequency;
        public final double correct;

        public Turbulent3D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent3D(Noise3D basis, Noise3D disturb) {
            this(basis, disturb, 1);
        }

        public Turbulent3D(Noise3D basis, Noise3D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Turbulent3D(Noise3D basis, Noise3D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
            correct = 1.0 / ((1 << this.octaves) - 1.0);
        }

        @Override
        public double getNoise(double x, double y, double z) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            x += disturbance.getNoise(x, y, z);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
            }
            return n * correct;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            x += disturbance.getNoiseWithSeed(x, y, z, seed);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoiseWithSeed(x * i_s, y * i_s, z * i_s, seed += 0x9E3779B97F4A7C15L) * s;
            }
            return n * correct;
        }
    }

    public static class Turbulent4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis, disturbance;
        public double frequency;
        public final double correct;
        public Turbulent4D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent4D(Noise4D basis, Noise4D disturb) {
            this(basis, disturb, 1);
        }


        public Turbulent4D(Noise4D basis, Noise4D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Turbulent4D(Noise4D basis, Noise4D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
            correct = 1.0 / ((1 << this.octaves) - 1.0);
        }

        @Override
        public double getNoise(double x, double y, double z, double w) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            x += disturbance.getNoise(x, y, z, w);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9)) * s;
            }
            return n * correct;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            x += disturbance.getNoiseWithSeed(x, y, z, w, seed);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoiseWithSeed(x * i_s, y * i_s, z * i_s, w * i_s, seed += 0x9E3779B97F4A7C15L) * s;
            }
            return n * correct;
        }
    }
    public static class Turbulent6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis, disturbance;
        public double frequency;
        public final double correct;
        public Turbulent6D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent6D(Noise6D basis, Noise6D disturb) {
            this(basis, disturb, 1);
        }

        public Turbulent6D(Noise6D basis, Noise6D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Turbulent6D(Noise6D basis, Noise6D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
            correct = 1.0 / ((1 << this.octaves) - 1.0);
        }
        @Override
        public double getNoise(double x, double y, double z, double w, double u, double v) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            x += disturbance.getNoise(x, y, z, w, u, v);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoise(x * i_s + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11)) * s;
            }
            return n * correct;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            x += disturbance.getNoiseWithSeed(x, y, z, w, u, v, seed);
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                i_s *= 0.5;
                n += basis.getNoiseWithSeed(x * i_s, y * i_s, z * i_s, w * i_s, u * i_s, v * i_s, seed += 0x9E3779B97F4A7C15L) * s;
            }
            return n * correct;
        }
    }

    public static class Viny2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis, disturbance;
        public double frequency;

        public Viny2D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Viny2D(Noise2D basis, Noise2D disturb) {
            this(basis, disturb, 1);
        }

        public Viny2D(Noise2D basis, Noise2D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Viny2D(Noise2D basis, Noise2D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(double x, double y) {
            x *= frequency;
            y *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy;
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                n += basis.getNoise(xx, yy) * s + disturbance.getNoise(xx, yy) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
            x *= frequency;
            y *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy;
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                n += basis.getNoiseWithSeed(xx, yy, seed) * s + disturbance.getNoiseWithSeed(xx, yy, seed) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }
    }
    public static class Viny3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis, disturbance;
        public double frequency;

        public Viny3D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Viny3D(Noise3D basis, Noise3D disturb) {
            this(basis, disturb, 1);
        }

        public Viny3D(Noise3D basis, Noise3D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Viny3D(Noise3D basis, Noise3D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(double x, double y, double z) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy, zz;
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                n += basis.getNoise(xx, yy, zz) * s + disturbance.getNoise(xx, yy, zz) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy, zz;
            for (int o = 0; o < octaves; o++) {
                seed += 0x9E3779B97F4A7C15L;
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                n += basis.getNoiseWithSeed(xx, yy, zz, seed) * s + disturbance.getNoiseWithSeed(xx, yy, zz, seed) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }
    }

    public static class Viny4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis, disturbance;
        public double frequency;

        public Viny4D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Viny4D(Noise4D basis, Noise4D disturb) {
            this(basis, disturb, 1);
        }


        public Viny4D(Noise4D basis, Noise4D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Viny4D(Noise4D basis, Noise4D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        @Override
        public double getNoise(double x, double y, double z, double w) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww;
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                n += basis.getNoise(xx, yy, zz, ww) * s + disturbance.getNoise(xx, yy, zz, ww) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww;
            for (int o = 0; o < octaves; o++) {
                seed += 0x9E3779B97F4A7C15L;
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                n += basis.getNoiseWithSeed(xx, yy, zz, ww, seed) * s + disturbance.getNoiseWithSeed(xx, yy, zz, ww, seed) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }
    }
    public static class Viny6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis, disturbance;
        public double frequency;
        public Viny6D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Viny6D(Noise6D basis, Noise6D disturb) {
            this(basis, disturb, 1);
        }

        public Viny6D(Noise6D basis, Noise6D disturb, final int octaves) {
            this(basis, disturb, octaves, 1.0);
        }
        public Viny6D(Noise6D basis, Noise6D disturb, final int octaves, final double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        @Override
        public double getNoise(double x, double y, double z, double w, double u, double v) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww, uu, vv;
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                uu = u * i_s + (o << 10);
                vv = v * i_s + (o << 11);
                n += basis.getNoise(xx, yy, zz, ww, uu, vv) * s + disturbance.getNoise(xx, yy, zz, ww, uu, vv) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            int s = 2 << (octaves - 1);
            double n = 0.0, i_s = 1.0 / s, xx, yy, zz, ww, uu, vv;
            for (int o = 0; o < octaves; o++) {
                seed += 0x9E3779B97F4A7C15L;
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                uu = u * i_s + (o << 10);
                vv = v * i_s + (o << 11);
                n += basis.getNoiseWithSeed(xx, yy, zz, ww, uu, vv, seed) * s + disturbance.getNoiseWithSeed(xx, yy, zz, ww, uu, vv, seed) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }
    }


    public static class Slick2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis, disturbance;

        public Slick2D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Slick2D(Noise2D basis, Noise2D disturb) {
            this(basis, disturb, 1);
        }

        public Slick2D(Noise2D basis, Noise2D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y) {
            double n = 0.0, i_s = 1.0, xx, yy;
            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                n += basis.getNoise(xx + disturbance.getNoise(x, y), yy) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, long seed) {
            double n = 0.0, i_s = 1.0, xx, yy;

            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed += 0x9E3779B97F4A7C15L;
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, seed), yy, seed) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Slick3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis, disturbance;

        public Slick3D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Slick3D(Noise3D basis, Noise3D disturb) {
            this(basis, disturb, 1);
        }

        public Slick3D(Noise3D basis, Noise3D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y, final double z) {
            double n = 0.0, i_s = 1.0, xx, yy, zz;
            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                n += basis.getNoise(xx + disturbance.getNoise(x, y, z), yy, zz) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, long seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz;
            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed += 0x9E3779B97F4A7C15L;
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, z, seed), yy, zz, seed) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Slick4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis, disturbance;

        public Slick4D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Slick4D(Noise4D basis, Noise4D disturb) {
            this(basis, disturb, 1);
        }

        public Slick4D(Noise4D basis, Noise4D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y, final double z, final double w) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww;
            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                n += basis.getNoise(xx + disturbance.getNoise(x, y, z, w), yy, zz, ww) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, long seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww;
            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed += 0x9E3779B97F4A7C15L;
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, z, w, seed), yy, zz, ww, seed) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Slick6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis, disturbance;

        public Slick6D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Slick6D(Noise6D basis, Noise6D disturb) {
            this(basis, disturb, 1);
        }

        public Slick6D(Noise6D basis, Noise6D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww, uu, vv;
            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                uu = u * i_s + (o << 10);
                vv = v * i_s + (o << 11);
                n += basis.getNoise(xx + disturbance.getNoise(x, y, z, w, u, v), yy, zz, ww, uu, vv) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, long seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww, uu, vv;
            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed += 0x9E3779B97F4A7C15L;
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                uu = u * i_s + (o << 10);
                vv = v * i_s + (o << 11);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, z, w, u, v, seed), yy, zz, ww, uu, vv, seed) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }

    /**
     * Produces a 2D array of noise with values from -1.0 to 1.0 that is seamless on all boundaries.
     * Uses (x,y) order. Allows a seed to change the generated noise.
     * If you need to call this very often, consider {@link #seamless2D(double[][], int, int)}, which re-uses the array.
     * @param width the width of the array to produce (the length of the outer layer of arrays)
     * @param height the height of the array to produce (the length of the inner arrays)
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return a freshly-allocated seamless-bounded array, a {@code double[width][height]}.
     */
    public static double[][] seamless2D(final int width, final int height, final int seed, final int octaves) {
        return seamless2D(new double[width][height], seed, octaves);
    }

    /**
     * Fills the given 2D array (modifying it) with noise, using values from -1.0 to 1.0, that is seamless on all
     * boundaries. This overload doesn't care what you use for x or y axes, it uses the exact size of fill fully.
     * Allows a seed to change the generated noise.
     * @param fill a 2D array of double; must be rectangular, so it's a good idea to create with {@code new double[width][height]} or something similar
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][] seamless2D(final double[][] fill, final int seed, final int octaves) {
        return seamless2D(fill, seed, octaves, SeededNoise.instance);
    }

    public static double total = 0.0;
    /**
     * Fills the given 2D array (modifying it) with noise, using values from -1.0 to 1.0, that is seamless on all
     * boundaries. This overload doesn't care what you use for x or y axes, it uses the exact size of fill fully.
     * Allows a seed to change the generated noise. DOES NOT clear the values in fill, so if it already has non-zero
     * elements, the result will be different than if it had been cleared beforehand. That does allow you to utilize
     * this method to add multiple seamless noise values on top of each other, though that allows values to go above or
     * below the normal minimum and maximum (-1.0 to 1.0).
     * @param fill a 2D array of double; must be rectangular, so it's a good idea to create with {@code new double[width][height]} or something similar
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][] seamless2D(final double[][] fill, long seed, final int octaves, final Noise.Noise4D generator) {
        final int height, width;
        if (fill == null || (width = fill.length) <= 0 || (height = fill[0].length) <= 0
                || octaves <= 0 || octaves >= 63)
            return fill;
        final double i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height;
        int s = 1 << (octaves - 1);
        total = 0.0;
        double p, q,
                ps, pc,
                qs, qc,
                i_s = 0.5 / s;
        for (int o = 0; o < octaves; o++, s >>= 1) {
            seed += 0x9E3779B97F4A7C15L;
            i_s *= 2.0;
            for (int x = 0; x < width; x++) {
                p = x * i_w;
                ps = NumberTools.sin(p) * i_s;
                pc = NumberTools.cos(p) * i_s;
                for (int y = 0; y < height; y++) {
                    q = y * i_h;
                    qs = NumberTools.sin(q) * i_s;
                    qc = NumberTools.cos(q) * i_s;
                    fill[x][y] += generator.getNoiseWithSeed(pc, ps, qc, qs, seed) * s;
                }
            }
        }
        i_s = 1.0 / ((1 << octaves) - 1.0);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                total += (fill[x][y] *= i_s);
            }
        }
        return fill;
    }

    /**
     * Produces a 3D array of noise with values from -1.0 to 1.0 that is seamless on all boundaries.
     * Allows a seed to change the generated noise.
     * Because most games that would use this would use it for maps, and maps are often top-down, the returned 3D array
     * uses the order (z,x,y), which allows a 2D slice of x and y to be taken as an element from the top-level array.
     * If you need to call this very often, consider {@link #seamless3D(double[][][], int, int)}, which re-uses the
     * array instead of re-generating it.
     * @param width the width of the array to produce (the length of the middle layer of arrays)
     * @param height the height of the array to produce (the length of the innermost arrays)
     * @param depth the depth of the array to produce (the length of the outermost layer of arrays)
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return a freshly-allocated seamless-bounded array, a {@code double[depth][width][height]}.
     */
    public static double[][][] seamless3D(final int depth, final int width, final int height, final int seed, final int octaves) {
        return seamless3D(new double[depth][width][height], seed, octaves);
    }

    /**
     * Fills the given 3D array (modifying it) with noise, using values from -1.0 to 1.0, that is seamless on all
     * boundaries. This overload doesn't care what you use for x, y, or z axes, it uses the exact size of fill fully.
     * Allows a seed to change the generated noise.
     * @param fill a 3D array of double; must be rectangular, so it's a good idea to create with {@code new double[depth][width][height]} or something similar
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][][] seamless3D(final double[][][] fill, final int seed, final int octaves) {
        return seamless3D(fill, seed, octaves, SeededNoise.instance);
    }

    /**
     * Fills the given 3D array (modifying it) with noise, using values from -1.0 to 1.0, that is seamless on all
     * boundaries. This overload doesn't care what you use for x, y, or z axes, it uses the exact size of fill fully.
     * Allows a seed to change the generated noise.
     * @param fill a 3D array of double; must be rectangular, so it's a good idea to create with {@code new double[depth][width][height]} or something similar
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static double[][][] seamless3D(final double[][][] fill, long seed, final int octaves, final Noise.Noise6D generator) {
        final int depth, height, width;
        if(fill == null || (depth = fill.length) <= 0 || (width = fill[0].length) <= 0 || (height = fill[0][0].length) <= 0
                || octaves <= 0 || octaves >= 63)
            return fill;
        final double i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height, i_d = 6.283185307179586 / depth;
        int s = 1<<(octaves-1);
        total = 0.0;
        double p, q, r,
                ps, pc,
                qs, qc,
                rs, rc, i_s = 0.5 / s;
        for (int o = 0; o < octaves; o++, s>>=1) {
            seed += 0x9E3779B97F4A7C15L;
            i_s *= 2.0;
            for (int x = 0; x < width; x++) {
                p = x * i_w;
                ps = NumberTools.sin(p) * i_s;
                pc = NumberTools.cos(p) * i_s;
                for (int y = 0; y < height; y++) {
                    q = y * i_h;
                    qs = NumberTools.sin(q) * i_s;
                    qc = NumberTools.cos(q) * i_s;
                    for (int z = 0; z < depth; z++) {
                        r = z * i_d;
                        rs = NumberTools.sin(r) * i_s;
                        rc = NumberTools.cos(r) * i_s;
                        fill[z][x][y] += generator.getNoiseWithSeed(pc, ps, qc, qs, rc, rs, seed) * s;
                    }
                }
            }
        }
        i_s = 1.0 / ((1<<octaves) - 1.0);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    total += (fill[z][x][y] *= i_s);
                }
            }
        }
        return fill;
    }

    /**
     * Fills the given 3D array (modifying it) with noise, using values from -1.0 to 1.0, that is seamless on all
     * boundaries. This overload doesn't care what you use for x, y, or z axes, it uses the exact size of fill fully.
     * Allows a seed to change the generated noise.
     * @param fill a 3D array of double; must be rectangular, so it's a good idea to create with {@code new double[depth][width][height]} or something similar
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return {@code fill}, after assigning it with seamless-bounded noise
     */
    public static float[][][] seamless3D(final float[][][] fill, long seed, final int octaves) {
        final int depth, height, width;
        if(fill == null || (depth = fill.length) <= 0 || (width = fill[0].length) <= 0 || (height = fill[0][0].length) <= 0
                || octaves <= 0 || octaves >= 63)
            return fill;
        final double i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height, i_d = 6.283185307179586 / depth;
        int s = 1<<(octaves-1);
        double p, q, r,
                ps, pc,
                qs, qc,
                rs, rc, i_s = 0.5 / s;
        for (int o = 0; o < octaves; o++, s>>=1) {
            seed += 0x9E3779B97F4A7C15L;
            i_s *= 2.0;
            for (int x = 0; x < width; x++) {
                p = x * i_w;
                ps = NumberTools.sin(p) * i_s;
                pc = NumberTools.cos(p) * i_s;
                for (int y = 0; y < height; y++) {
                    q = y * i_h;
                    qs = NumberTools.sin(q) * i_s;
                    qc = NumberTools.cos(q) * i_s;
                    for (int z = 0; z < depth; z++) {
                        r = z * i_d;
                        rs = NumberTools.sin(r) * i_s;
                        rc = NumberTools.cos(r) * i_s;
                        fill[z][x][y] += SeededNoise.noise(pc, ps, qc, qs, rc, rs, seed) * s;
                    }
                }
            }
        }
        i_s = 1.0 / ((1<<octaves) - 1.0);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    fill[z][x][y] *= i_s;
                }
            }
        }
        return fill;
    }

    /**
     * A very simple 1D noise implementation, because a full-blown Perlin or Simplex noise implementation is probably
     * overkill for 1D noise. This does produce smoothly sloping lines, like Simplex noise does for higher dimensions.
     * The shape of the line varies over time, but <a href="https://i.imgur.com/83R3WLN.png">can look like this</a>.
     * If you give this a seed with {@link #getNoiseWithSeed(double, long)} instead of using {@link #getNoise(double)},
     * it will use a small extra step to adjust the spacing of peaks and valleys based on the seed, so getNoiseWithSeed
     * is slower than getNoise. If you use any Noise classes like {@link Noise.Layered1D}, they should use a seed anyway
     * because different octaves won't have different enough shapes otherwise.
     */
    public static class Basic1D implements Noise1D
    {
        public static final Basic1D instance = new Basic1D();
        public double alter1, alter2, alter3, alter4;
        public long lastSeed;
        public Basic1D()
        {
            this(1L);
        }
        public Basic1D(long seed)
        {
            lastSeed = seed;
            alter1 = (LinnormRNG.determine(seed) >>> 11) * 0x1.5p-54 + 0.25;
            alter2 = (LinnormRNG.determine(seed + 1) >>> 11) * 0x1.5p-54 + 0.25;
            alter3 = (LinnormRNG.determine(seed + 2) >>> 11) * 0x1.5p-54 + 0.25;
            alter4 = (LinnormRNG.determine(seed + 3) >>> 11) * 0x1.5p-54 + 0.25;
        }
        @Override
        public double getNoise(double x) {

            return (cubicSway(alter2 + x * alter1) +
                    cubicSway(alter3 - x * alter2) +
                    cubicSway(alter4 + x * alter3) +
                    cubicSway(alter1 - x * alter4)) * 0.25f;
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            if(lastSeed != seed)
            {
                lastSeed = seed;
                alter1 = (LinnormRNG.determine(seed) >>> 11) * 0x1.5p-54 + 0.25;
                alter2 = (LinnormRNG.determine(seed + 1) >>> 11) * 0x1.5p-54 + 0.25;
                alter3 = (LinnormRNG.determine(seed + 2) >>> 11) * 0x1.5p-54 + 0.25;
                alter4 = (LinnormRNG.determine(seed + 3) >>> 11) * 0x1.5p-54 + 0.25;
            }
            return (cubicSway(alter2 + x * alter1) +
                    cubicSway(alter3 - x * alter2) +
                    cubicSway(alter4 + x * alter3) +
                    cubicSway(alter1 - x * alter4)) * 0.25f;
        }
        public static double cubicSway(double value)
        {
            long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
            value -= floor;
            floor = (-(floor & 1L) | 1L);
            return value * value * (3.0 - 2.0 * value) * (floor << 1) - floor;
        }

        public static double noise(double x, long seed) {
            final double alter1 = (LinnormRNG.determine(seed) >>> 11) * 0x1.5p-54 + 0.25,
                    alter2 = (LinnormRNG.determine(seed + 1) >>> 11) * 0x1.5p-54 + 0.25,
                    alter3 = (LinnormRNG.determine(seed + 2) >>> 11) * 0x1.5p-54 + 0.25, 
                    alter4 = (LinnormRNG.determine(seed + 3) >>> 11) * 0x1.5p-54 + 0.25;                    
            return (cubicSway(alter2 + x * alter1) +
                    cubicSway(alter3 - x * alter2) +
                    cubicSway(alter4 + x * alter3) +
                    cubicSway(alter1 - x * alter4)) * 0.25f;
        }
    }

    public static class Sway1D implements Noise1D
    {
        public static final Sway1D instance = new Sway1D();
        public long seed;
        public Sway1D()
        {
            seed = 0L;
        }
        public Sway1D(long seed)
        {
            this.seed = seed;
        }

        @Override
        public double getNoise(double x) {
            return NumberTools.swayRandomized(seed, x);
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            return NumberTools.swayRandomized(seed, x);
        }
    }
    public static class Sway2D implements Noise2D
    {
        public static final Sway2D instance = new Sway2D();
        public long seed;
        public Sway2D()
        {
            seed = 0L;
        }
        public Sway2D(long seed)
        {
            this.seed = seed;
        }

        @Override
        public double getNoise(double x, double y) {
            return getNoiseWithSeed(x, y, seed);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
//            double xx = NumberTools.swayRandomized(seed - 0xC13FA9A902A6328FL, x + y) * 0.75,
//                    yy = NumberTools.swayRandomized(seed - 0xABC98388FB8FAC03L, y - x) * 0.75;
//            return NumberTools.sway((NumberTools.swayRandomized(seed, x + yy) +
//                    NumberTools.swayRandomized(0x8CB92BA72F3D8DD7L - seed, y + xx)) * 1.25 + 0.5);

//            return NumberTools.swayRandomized(seed,
//                    (NumberTools.swayRandomized(seed + 0x8CB92BA72F3D8DD7L, (x + y))
//                    + NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, x * 0.5 - y * 1.5)
//                    + NumberTools.swayRandomized(seed + 0xABC98388FB8FAC03L, x * 1.5 - y * 0.5)) * 4.0);

            double adjust0 = NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, x * 1.75 + y * -0.25) + 1.,
                    adjust1 = NumberTools.swayRandomized(seed + 0x8CB92BA72F3D8DD7L, x * 0.25 + y * -1.75) + 1.,
                    adjust2 = NumberTools.swayRandomized(seed - 0x8CB92BA72F3D8DD7L, x + y) + 1.;
            return NumberTools.sway(
                    (NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, x * 1.5 + y * 0.5) * adjust0
                    + NumberTools.swayRandomized(seed + 0xABC98388FB8FAC03L, x * 0.5 + y * 1.5) * adjust1
                    + NumberTools.swayRandomized(seed + 0x8CB92BA72F3D8DD7L, x - y) * adjust2 
                    ) * 0.75 + 0.5);

//            long xf = x >= 0.0 ? (long) x : (long) x - 1L;
//            long yf = y >= 0.0 ? (long) y : (long) y - 1L;
//            long s = ((0x91E10DA5C79E7B1DL ^ seed ^ yf)) * 0xC13FA9A902A6328FL, s2 = ((0x91E10DA5C79E7B1DL ^ seed ^ yf + 1L)) * 0xC13FA9A902A6328FL;
//            double xSmall = x - xf;
//            //, 0xABC98388FB8FAC03L, 0x8CB92BA72F3D8DD7L
//            double start = (((s += xf * 0x6C8E9CF570932BD5L) ^ (s >>> 25)) * (s | 0xA529L)) * 0x0.fffffffffffffbp-63,
//                    start2 = (((s2 += xf * 0x6C8E9CF570932BD5L) ^ (s2 >>> 25)) * (s2 | 0xA529L)) * 0x0.fffffffffffffbp-63,
//                    end = (((s += 0x6C8E9CF570932BD5L) ^ (s >>> 25)) * (s | 0xA529L)) * 0x0.fffffffffffffbp-63,
//                    end2 = (((s2 += 0x6C8E9CF570932BD5L) ^ (s2 >>> 25)) * (s2 | 0xA529L)) * 0x0.fffffffffffffbp-63;
////            double x0y0 = HastyPointHash.hashAll(xf, yf, seed) * 0x0.fffffffffffffbp-63,
////                    x1y0 = HastyPointHash.hashAll(xf+1L, yf, seed) * 0x0.fffffffffffffbp-63,
////                    x0y1 = HastyPointHash.hashAll(xf, yf+1L, seed) * 0x0.fffffffffffffbp-63,
////                    x1y1 = HastyPointHash.hashAll(xf+1L, yf+1L, seed) * 0x0.fffffffffffffbp-63, y0, y1;
//            xSmall = xSmall * xSmall * (3.0 - 2.0 * xSmall);
////            double a2 = xSmall * xSmall, a4 = a2 * a2, a6 = a4 * a2;
////            xSmall = 0x1.c71c71c71c71cp-2 * a6 + -0x1.e38e38e38e38ep0 * a4 + 0x1.38e38e38e38e4p1 * a2;
////            y0 = (1.0 - xSmall) * x0y0 + xSmall * x1y0;
////            y1 = (1.0 - xSmall) * x0y1 + xSmall * x1y1;
//            double ySmall = y - yf;
//            ySmall = ySmall * ySmall * (3.0 - 2.0 * ySmall);
////            a2 = ySmall * ySmall;
////            a4 = a2 * a2;
////            a6 = a4 * a2;
////            ySmall = 0x1.c71c71c71c71cp-2 * a6 + -0x1.e38e38e38e38ep0 * a4 + 0x1.38e38e38e38e4p1 * a2;
//            return (1.0 - ySmall) * ((1.0 - xSmall) * start + xSmall * end) + ySmall * ((1.0 - xSmall) * start2 + xSmall * end2);
////            x1 = (1.0 - xSmall) * start2 + xSmall * end2;
////            s = HastyPointHash.hashAll(xf, yf, seed);//((0xC13FA9A902A6328FL ^ seed)) * 0x91E10DA5C79E7B1DL;
////            start = (((s += yf * 0x6C8E9CF570932BD5L) ^ (s >>> 25)) * (s | 0xA529L)) * 0x0.fffffffffffffbp-63;
//////            start2 = (((s2 = s * 0xD1B54A32D192ED03L) ^ (s2 >>> 25)) * (s2 | 0xA529L)) * 0x0.fffffffffffffbp-63;
////            end = (((s += 0x6C8E9CF570932BD5L) ^ (s >>> 25)) * (s | 0xA529L)) * 0x0.fffffffffffffbp-63;
//////            end2 = (((s2 = s * 0xD1B54A32D192ED03L) ^ (s2 >>> 25)) * (s2 | 0xA529L)) * 0x0.fffffffffffffbp-63;
////            y0 = (1.0 - ySmall) * start + ySmall * end;
//////            y1 = (1.0 - ySmall) * start2 + ySmall * end2;
////            return NumberTools.sway(x0 + y0 + 0.5);
        }
    }
}
