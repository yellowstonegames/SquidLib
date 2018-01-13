package squidpony.squidmath;

/**
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
        return t >= 0 ? (long) t : (long) t - 1;
    }
    /**
     * Like {@link Math#floor(double)}, but takes a float and returns a long.
     * Doesn't consider weird floats like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as a long
     */
    public static long longFloor(float t) {
        return t >= 0 ? (long) t : (long) t - 1;
    }
    /**
     * Like {@link Math#floor(double)} , but returns an int.
     * Doesn't consider weird doubles like INFINITY and NaN.
     * @param t the float to find the floor for
     * @return the floor of t, as an int
     */
    public static int fastFloor(double t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }
    /**
     * Like {@link Math#floor(double)}, but takes a float and returns an int.
     * Doesn't consider weird floats like INFINITY and NaN.
     * @param t the float to find the floor for
     * @return the floor of t, as an int
     */
    public static int fastFloor(float t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }
    /**
     * Like {@link Math#ceil(double)}, but returns an int.
     * Doesn't consider weird doubles like INFINITY and NaN.
     * @param t the float to find the ceiling for
     * @return the ceiling of t, as an int
     */
    public static int fastCeil(double t) {
        return t >= 0 ? -(int) -t + 1: -(int)-t;
    }
    /**
     * Like {@link Math#ceil(double)}, but takes a float and returns an int.
     * Doesn't consider weird floats like INFINITY and NaN.
     * @param t the float to find the ceiling for
     * @return the ceiling of t, as an int
     */
    public static int fastCeil(float t) {
        return t >= 0 ? -(int) -t + 1: -(int)-t;
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
     * A group of similar methods for getting hashes of points based on long coordinates in 2, 3, 4, or 6 dimensions and
     * a long for state. This is organized how it is so it can be statically imported without also importing the rest
     * of Noise. Internally, all of the methods here are based on {@link ThrustAltRNG}, but some steps are altered to
     * better mix the point coordinates, and for hash32() and hash256() methods, some small steps can be omitted because
     * they would have no effect on the result.
     */
    public static final class PointHash
    {
        /**
         *
         * @param x
         * @param y
         * @param state
         * @return 64-bit hash of the x,y point with the given state
         */
        public static long hashAll(final long x, final long y, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((y ^ state) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((x ^ state) | 0xA529L)) ^ (state >>> 22)));
        }

        /**
         *
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 64-bit hash of the x,y,z point with the given state
         */
        public static long hashAll(final long x, final long y, final long z, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
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
        public static long hashAll(final long x, final long y, final long z, final long w, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
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
        public static long hashAll(final long x, final long y, final long z, final long w, final long u, final long v, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ u) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
        }
        /**
         *
         * @param x
         * @param y
         * @param state
         * @return 8-bit hash of the x,y point with the given state
         */
        public static int hash256(final long x, final long y, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((y ^ state) | 0xA529L)) ^ (state >>> 22)) ^
                    (((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((x ^ state) | 0xA529L))) >>> 56);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 8-bit hash of the x,y,z point with the given state
         */
        public static int hash256(final long x, final long y, final long z, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
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
        public static int hash256(final long x, final long y, final long z, final long w, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
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
        public static int hash256(final long x, final long y, final long z, final long w, final long u, final long v, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
        }
        /**
         *
         * @param x
         * @param y
         * @param state
         * @return 5-bit hash of the x,y point with the given state
         */
        public static int hash32(final long x, final long y, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)))) >>> 59);
        }
        /**
         *
         * @param x
         * @param y
         * @param z
         * @param state
         * @return 5-bit hash of the x,y,z point with the given state
         */
        public static int hash32(final long x, final long y, final long z, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y - z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z - x) | 0xA529L)) ^ (state >>> 22)) ^
                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x - y) | 0xA529L))) >>> 59);
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
        public static int hash32(final long x, final long y, final long z, final long w, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
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
        public static int hash32(final long x, final long y, final long z, final long w, final long u, final long v, long state)
        {
            //state *= 0x352E9CF570932BDDL;
            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
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
        public Layered1D() {
            this(ValueNoise.instance);
        }

        public Layered1D(Noise1D basis) {
            this(basis, 2);
        }

        public Layered1D(Noise1D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered1D(Noise1D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }


        @Override
        public double getNoise(double x) {
            x *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= 0.5) + (o << 6)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            x *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= 0.5), (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }

    public static class Layered2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis;
        public double frequency;
        public Layered2D() {
            this(SeededNoise.instance, 2);
        }

        public Layered2D(Noise2D basis) {
            this(basis, 2);
        }

        public Layered2D(Noise2D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered2D(Noise2D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(double x, double y) {
            x *= frequency;
            y *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7)) * s;
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
                n += basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis;
        public double frequency;
        public Layered3D() {
            this(SeededNoise.instance, 2);
        }

        public Layered3D(Noise3D basis) {
            this(basis, 2);
        }

        public Layered3D(Noise3D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered3D(Noise3D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(double x, double y, double z) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
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
                n += basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, z * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis;
        public double frequency;
        public Layered4D() {
            this(SeededNoise.instance, 2);
        }

        public Layered4D(Noise4D basis) {
            this(basis, 2);
        }

        public Layered4D(Noise4D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered4D(Noise4D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
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
                n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9)) * s;
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
                n += basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, z * i_s, w * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis;
        public double frequency;
        public Layered6D() {
            this(SeededNoise.instance, 2);
        }

        public Layered6D(Noise6D basis) {
            this(basis, 2);
        }

        public Layered6D(Noise6D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered6D(Noise6D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
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
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
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
                n += basis.getNoiseWithSeed(x * (i_s *= 0.5), y * i_s, z * i_s
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
            this(ValueNoise.instance);
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
            this(ValueNoise.instance);
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
            for (int o = 0; o < octaves; o++) {
                correct += Math.pow(2.0, -o);
            }
            correct = 1.9 / correct;
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
                seed = ThrustRNG.determine(seed);
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

        public Ridged3D(Noise3D basis, int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            setOctaves(octaves);
        }
        public void setOctaves(int octaves)
        {
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            for (int o = 0; o < octaves; o++) {
                correct += Math.pow(2.0, -o);
            }
            correct = 1.45 / correct;
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
                seed = ThrustRNG.determine(seed);
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
                maxvalue += (exp[i] = Math.pow(2.0, -0.9 * i));
            }
            correct = 1.41 / maxvalue;
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
                //seed = ThrustRNG.determine(seed);
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
                maxvalue += (exp[i] = Math.pow(2.0, -0.9 * i));
            }
            correct = 1.28 / maxvalue;
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
                seed = ThrustRNG.determine(seed);
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
        public double frequency = 1.0;
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
        public double frequency = 1.0;
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
        public double frequency = 1.0;
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
        public double frequency = 1.0;
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
        public double frequency = 1.0;

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
        public double frequency = 1.0;

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
        public double frequency = 1.0;

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
        public double frequency = 1.0;
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
                ps = Math.sin(p) * i_s;
                pc = Math.cos(p) * i_s;
                for (int y = 0; y < height; y++) {
                    q = y * i_h;
                    qs = Math.sin(q) * i_s;
                    qc = Math.cos(q) * i_s;
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
                ps = Math.sin(p) * i_s;
                pc = Math.cos(p) * i_s;
                for (int y = 0; y < height; y++) {
                    q = y * i_h;
                    qs = Math.sin(q) * i_s;
                    qc = Math.cos(q) * i_s;
                    for (int z = 0; z < depth; z++) {
                        r = z * i_d;
                        rs = Math.sin(r) * i_s;
                        rc = Math.cos(r) * i_s;
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
                ps = Math.sin(p) * i_s;
                pc = Math.cos(p) * i_s;
                for (int y = 0; y < height; y++) {
                    q = y * i_h;
                    qs = Math.sin(q) * i_s;
                    qc = Math.cos(q) * i_s;
                    for (int z = 0; z < depth; z++) {
                        r = z * i_d;
                        rs = Math.sin(r) * i_s;
                        rc = Math.cos(r) * i_s;
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
        public Basic1D()
        {
        }
        @Override
        public double getNoise(double x) {
            final long x0 = longFloor(x);
            return cerp(NumberTools.randomFloatCurved(x0), NumberTools.randomFloatCurved(x0 + 1L), x - x0);
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            x += NumberTools.bounce(seed + x);
            final long x0 = longFloor(x);
            return cerp(NumberTools.randomFloatCurved(x0 + seed), NumberTools.randomFloatCurved(x0 + 1L + seed), x - x0);
        }

        public static double noise(double x, long seed) {
            //x += NumberTools.sway(seed * 0x1.61p-16 * x);
            final long x0 = longFloor(x);
            return cerp(NumberTools.randomFloatCurved(x0 + seed), NumberTools.randomFloatCurved(x0 + 1L + seed), x - x0);
        }
    }
}
