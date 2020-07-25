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
     * Given a double {@code a} from 0.0 to 1.0 (both inclusive), this gets a double that adjusts a to be much closer to
     * the end points of that range (if less than 0.5, it gets closer to 0.0, otherwise it gets closer to 1.0).
     * @param a a double between 0.0 and 1.0 inclusive
     * @return a double between 0.0 and 1.0 inclusive that is more likely to be near the extremes
     */
    public static double extreme(final double a)
    {
        return a * a * a * (a * (a * 6 - 15) + 10);
    }

    /**
     * Given a double {@code a} from -1.0 to 1.0 (both inclusive), this gets a double that adjusts a to be much closer
     * to the end points of that range (if less than 0, it gets closer to -1.0, otherwise it gets closer to 1.0).
     * @param a a double between -1.0 and 1.0 inclusive
     * @return a double between -1.0 and 1.0 inclusive that is more likely to be near the extremes
     */
    public static double extremeSigned(double a)
    {
        a = a * 0.5 + 0.5;
        return a * a * a * (a * (a * 12 - 30) + 20) - 1.0;
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

    public interface Noise5D {
        double getNoise(double x, double y, double z, double w, double u);
        double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed);
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
    public static class Layered5D implements Noise5D {
        protected int octaves;
        protected Noise5D basis;
        public double frequency;
        public double lacunarity;
        public Layered5D() {
            this(FoamNoise.instance);
        }

        public Layered5D(Noise5D basis) {
            this(basis, 2);
        }

        public Layered5D(Noise5D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public Layered5D(Noise5D basis, final int octaves, double frequency) {
            this(basis, octaves, frequency, 0.5);
        }
        public Layered5D(Noise5D basis, final int octaves, double frequency, double lacunarity) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z, double w, double u) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                        , w * i_s + (o << 9), u * i_s + (o << 10)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            int s = 1;
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s <<= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s
                        , w * i_s, u * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
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
    public static class InverseLayered5D implements Noise5D {
        protected int octaves;
        protected Noise5D basis;
        public double frequency;
        /**
         * A multiplier that affects how much the frequency changes with each layer; the default is 0.5 .
         */
        public double lacunarity = 0.5;
        public InverseLayered5D() {
            this(FoamNoise.instance, 2);
        }

        public InverseLayered5D(Noise5D basis) {
            this(basis, 2);
        }

        public InverseLayered5D(Noise5D basis, final int octaves) {
            this(basis, octaves, 1.0);
        }
        public InverseLayered5D(Noise5D basis, final int octaves, double frequency) {
            this.basis = basis;
            this.frequency = frequency;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        public InverseLayered5D(Noise5D basis, final int octaves, double frequency, double lacunarity){
            this(basis, octaves, frequency);
            this.lacunarity = lacunarity;
        }

        @Override
        public double getNoise(double x, double y, double z, double w, double u) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= lacunarity) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                        , w * i_s + (o << 9), u * i_s + (o << 10)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            int s = 1 << (octaves - 1);
            double n = 0.0, i_s = 2.0;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoiseWithSeed(x * (i_s *= lacunarity), y * i_s, z * i_s
                        , w * i_s, u * i_s, (seed += 0x9E3779B97F4A7C15L)) * s;
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
        public double[] exp;
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


    public static class Ridged5D implements Noise5D
    {
        protected double[] exp;
        protected int octaves;
        public double frequency, correct;
        public Noise5D basis;
        public Ridged5D()
        {
            this(FoamNoise.instance, 2, 1.25);
        }
        public Ridged5D(Noise5D basis)
        {
            this(basis, 2, 1.25);
        }
        public Ridged5D(Noise5D basis, int octaves, double frequency)
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
        public double getNoise(double x, double y, double z, double w, double u) {
            double sum = 0.0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;

            for (int i = 0; i < octaves; ++i) {
                n = basis.getNoise(x + (i << 6), y + (i << 7), z + (i << 8), w + (i << 9), u + (i << 10));
                n = 1.0 - Math.abs(n);
                sum += n * n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
                u *= 2.0; 
            }
            return sum * correct - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
            double sum = 0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            for (int i = 0; i < octaves; ++i) {
                n = basis.getNoiseWithSeed(x, y, z,
                        w, u, (seed += 0x9E3779B97F4A7C15L));
                n = 1.0 - Math.abs(n);
                sum += n * n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
                u *= 2.0;
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


    public static class Exponential1D implements Noise1D {
        protected Noise1D basis;
        protected double sharpness, adjustment;
        public Exponential1D() {
            this(Basic1D.instance);
        }

        public Exponential1D(Noise1D basis) {
            this(basis, 0.125);
        }
        public Exponential1D(Noise1D basis, double sharpness) {
            this.basis = basis;
            this.sharpness = sharpness - fastFloor(sharpness) - 1.0;
            this.adjustment = 2.0 / Math.log1p(this.sharpness * 0.9999999999999999);
            this.sharpness *= 0.5;
        }


        @Override
        public double getNoise(double x) {
            return Math.log(1.0 + sharpness * (basis.getNoise(x) + 1.0)) * adjustment - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            return Math.log(1.0 + sharpness * (basis.getNoiseWithSeed(x, seed) + 1.0)) * adjustment - 1.0;
        }
    }

    public static class Exponential2D implements Noise2D {
        protected Noise2D basis;
        protected double sharpness, adjustment;
        public Exponential2D() {
            this(SeededNoise.instance);
        }

        public Exponential2D(Noise2D basis) {
            this(basis, 0.125);
        }
        public Exponential2D(Noise2D basis, double sharpness) {
            this.basis = basis;
            this.sharpness = sharpness - fastFloor(sharpness) - 1.0;
            this.adjustment = 2.0 / Math.log1p(this.sharpness * 0.9999999999999999);
            this.sharpness *= 0.5;
        }

        @Override
        public double getNoise(double x, double y) {
            return Math.log(1.0 + sharpness * (basis.getNoise(x, y) + 1.0)) * adjustment - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
            return Math.log(1.0 + sharpness * (basis.getNoiseWithSeed(x, y, seed) + 1.0)) * adjustment - 1.0;
        }
    }
    public static class Exponential3D implements Noise3D {
        protected Noise3D basis;
        protected double sharpness, adjustment;
        public Exponential3D() {
            this(SeededNoise.instance);
        }

        public Exponential3D(Noise3D basis) {
            this(basis, 0.125);
        }
        public Exponential3D(Noise3D basis, double sharpness) {
            this.basis = basis;
            this.sharpness = sharpness - fastFloor(sharpness) - 1.0;
            this.adjustment = 2.0 / Math.log1p(this.sharpness * 0.9999999999999999);
            this.sharpness *= 0.5;
        }

        @Override
        public double getNoise(double x, double y, double z) {
            return Math.log(1.0 + sharpness * (basis.getNoise(x, y, z) + 1.0)) * adjustment - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, long seed) {
            return Math.log(1.0 + sharpness * (basis.getNoiseWithSeed(x, y, z, seed) + 1.0)) * adjustment - 1.0;
        }
    }
    public static class Exponential4D implements Noise4D {
        protected Noise4D basis;
        protected double sharpness, adjustment;
        public Exponential4D() {
            this(SeededNoise.instance);
        }

        public Exponential4D(Noise4D basis) {
            this(basis, 0.125);
        }
        public Exponential4D(Noise4D basis, double sharpness) {
            this.basis = basis;
            this.sharpness = sharpness - fastFloor(sharpness) - 1.0;
            this.adjustment = 2.0 / Math.log1p(this.sharpness * 0.9999999999999999);
            this.sharpness *= 0.5;
        }

        @Override
        public double getNoise(double x, double y, double z, double w) {
            return Math.log(1.0 + sharpness * (basis.getNoise(x, y, z, w) + 1.0)) * adjustment - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
            return Math.log(1.0 + sharpness * (basis.getNoiseWithSeed(x, y, z, w, seed) + 1.0)) * adjustment - 1.0;
        }
    }
    public static class Exponential6D implements Noise6D {
        protected Noise6D basis;
        protected double sharpness, adjustment;
        public Exponential6D() {
            this(SeededNoise.instance);
        }

        public Exponential6D(Noise6D basis) {
            this(basis, 0.125);
        }
        public Exponential6D(Noise6D basis, double sharpness) {
            this.basis = basis;
            this.sharpness = sharpness - fastFloor(sharpness) - 1.0;
            this.adjustment = 2.0 / Math.log1p(this.sharpness * 0.9999999999999999);
            this.sharpness *= 0.5;
        }

        @Override
        public double getNoise(double x, double y, double z, double w, double u, double v) {
            return Math.log(1.0 + sharpness * (basis.getNoise(x, y, z, w, u, v) + 1.0)) * adjustment - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
            return Math.log(1.0 + sharpness * (basis.getNoiseWithSeed(x, y, z, w, u, v, seed) + 1.0)) * adjustment - 1.0;
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

    public static double total;
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
     * If you need to call this very often, consider {@link #seamless3D(double[][][], long, int)}, which re-uses the
     * array instead of re-generating it.
     * @param width the width of the array to produce (the length of the middle layer of arrays)
     * @param height the height of the array to produce (the length of the innermost arrays)
     * @param depth the depth of the array to produce (the length of the outermost layer of arrays)
     * @param seed an int seed that affects the noise produced, with different seeds producing very different noise
     * @param octaves how many runs of differently sized and weighted noise generations to apply to the same area
     * @return a freshly-allocated seamless-bounded array, a {@code double[depth][width][height]}.
     */
    public static double[][][] seamless3D(final int depth, final int width, final int height, final long seed, final int octaves) {
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
    public static double[][][] seamless3D(final double[][][] fill, final long seed, final int octaves) {
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
     * Like {@link #seamless2D(double[][], long, int, Noise4D)}, but this produces 1D noise that "tiles" by repeating
     * its output every {@code sizeX} units that {@code x} increases or decreases by. This doesn't precalculate an
     * array, instead calculating just one value so that later calls with different x will tile seamlessly.
     * <br>
     * Internally, this just samples out of a circle from a source of 2D noise.
     * @param noise a Noise2D implementation such as a {@link SeededNoise} or {@link FastNoise}
     * @param x the x-coordinate to sample
     * @param sizeX the range of x to generate before repeating; must be greater than 0
     * @param seed the noise seed, as a long
     * @return continuous noise from -1.0 to 1.0, inclusive 
     */
    public static double seamless1D(Noise2D noise, double x, double sizeX, long seed)
    {
        x *= 6.283185307179586 / sizeX;
        return noise.getNoiseWithSeed(NumberTools.cos(x), NumberTools.sin(x), seed);
    }

    /**
     * Like {@link #seamless2D(double[][], long, int, Noise4D)}, but this doesn't precalculate noise into an array,
     * instead calculating just one 2D point so that later calls with different x or y will tile seamlessly.
     * @param noise a Noise4D implementation such as a {@link SeededNoise} or {@link FastNoise}
     * @param x the x-coordinate to sample
     * @param y the y-coordinate to sample
     * @param sizeX the range of x to generate before repeating; must be greater than 0
     * @param sizeY the range of y to generate before repeating; must be greater than 0
     * @param seed the noise seed, as a long
     * @return continuous noise from -1.0 to 1.0, inclusive 
     */
    public static double seamless2D(Noise4D noise, double x, double y, double sizeX, double sizeY, long seed)
    {
        x *= 6.283185307179586 / sizeX;
        y *= 6.283185307179586 / sizeY;
        return noise.getNoiseWithSeed(NumberTools.cos(x), NumberTools.sin(x), NumberTools.cos(y), NumberTools.sin(y),
                seed);
    }


    /**
     * Like {@link #seamless3D(double[][][], long, int, Noise6D)}, but this doesn't precalculate noise into an array,
     * instead calculating just one 3D point so that later calls with different x, y, or z will tile seamlessly.
     * @param noise a Noise6D implementation such as a {@link SeededNoise} or {@link FastNoise}
     * @param x the x-coordinate to sample
     * @param y the y-coordinate to sample
     * @param z the z-coordinate to sample
     * @param sizeX the range of x to generate before repeating; must be greater than 0
     * @param sizeY the range of y to generate before repeating; must be greater than 0
     * @param sizeZ the range of z to generate before repeating; must be greater than 0
     * @param seed the noise seed, as a long
     * @return continuous noise from -1.0 to 1.0, inclusive 
     */
    public static double seamless3D(Noise6D noise, double x, double y, double z, double sizeX, double sizeY, double sizeZ, long seed)
    {
        x *= 6.283185307179586 / sizeX;
        y *= 6.283185307179586 / sizeY;
        z *= 6.283185307179586 / sizeZ;
        return noise.getNoiseWithSeed(NumberTools.cos(x), NumberTools.sin(x), NumberTools.cos(y), NumberTools.sin(y),
                NumberTools.cos(z), NumberTools.sin(z), seed);
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
            alter1 = (DiverRNG.determine(seed) >> 11) * 0x1.8p-54;
            alter2 = (DiverRNG.determine(seed + 11111) >> 11) * 0x1p-53;
            alter3 = (DiverRNG.determine(seed + 22222) >> 11) * 0x1.8p-53;
            alter4 = (DiverRNG.determine(seed + 33333) >> 11) * 0x1p-52;
        }
        @Override
        public double getNoise(double x) {
            return (cubicSway(x * alter1) * 0.4375f +
                    cubicSway(x * alter2) * 0.3125f +
                    cubicSway(x * alter3) * 0.1875f +
                    cubicSway(x * alter4) * 0.0625f);
        }

        @Override
        public double getNoiseWithSeed(double x, long seed) {
            if(lastSeed != seed)
            {
                lastSeed = seed;
                alter1 = (DiverRNG.determine(seed) >> 11) * 0x1.8p-54;
                alter2 = (DiverRNG.determine(seed + 11111) >> 11) * 0x1p-53;
                alter3 = (DiverRNG.determine(seed + 22222) >> 11) * 0x1.8p-53;
                alter4 = (DiverRNG.determine(seed + 33333) >> 11) * 0x1p-52;
            }
            return (cubicSway(x * alter1) * 0.4375f +
                    cubicSway(x * alter2) * 0.3125f +
                    cubicSway(x * alter3) * 0.1875f +
                    cubicSway(x * alter4) * 0.0625f);
        }
        public static double cubicSway(double value)
        {
            long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
            value -= floor;
            floor = (-(floor & 1L) | 1L);
            return value * value * (3.0 - 2.0 * value) * (floor << 1) - floor;
        }

        public static double noise(double x, long seed) {
            final double
                    alter1 = (DiverRNG.determine(seed) >> 11) * 0x1.8p-54, 
                    alter2 = (DiverRNG.determine(seed + 11111) >> 11) * 0x1p-53, 
                    alter3 = (DiverRNG.determine(seed + 22222) >> 11) * 0x1.8p-53, 
                    alter4 = (DiverRNG.determine(seed + 33333) >> 11) * 0x1p-52;
            return (cubicSway(x * alter1) * 0.4375f +
                    cubicSway(x * alter2) * 0.3125f +
                    cubicSway(x * alter3) * 0.1875f +
                    cubicSway(x * alter4) * 0.0625f);
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
            seed = 12345L;
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

//            double adjust0 = NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, x * 1.75 + y * -0.25) + 1.,
//                    adjust1 = NumberTools.swayRandomized(seed + 0x8CB92BA72F3D8DD7L, x * 0.25 + y * -1.75) + 1.,
//                    adjust2 = NumberTools.swayRandomized(seed - 0x8CB92BA72F3D8DD7L, x + y) + 1.;
//            return NumberTools.sway(
//                    (NumberTools.swayRandomized(seed + 0xC13FA9A902A6328FL, x * 1.5 + y * 0.5) * adjust0
//                    + NumberTools.swayRandomized(seed + 0xABC98388FB8FAC03L, x * 0.5 + y * 1.5) * adjust1
//                    + NumberTools.swayRandomized(seed + 0x8CB92BA72F3D8DD7L, x - y) * adjust2 
//                    ) * 0.75 + 0.5);
            final long floorX = x >= 0.0 ? (long) x : (long) x - 1L,
                    floorY = y >= 0.0 ? (long) y : (long) y - 1L;
//            long seedX = seed * 0xC13FA9A902A6328FL + floorX * 0x91E10DA5C79E7B1DL,
//                    seedY = seed * 0x91E10DA5C79E7B1DL + floorY * 0xC13FA9A902A6328FL;
//            final long startX = ((seedX ^ (seedX >>> 25)) * (seedX | 0xA529L)),
//                    endX = (((seedX += 0x91E10DA5C79E7B1DL) ^ (seedX >>> 25)) * (seedX | 0xA529L));
//            final long startY = ((seedY ^ (seedY >>> 25)) * (seedY | 0xA529L)),
//                    endY = (((seedY += 0xC13FA9A902A6328FL) ^ (seedY >>> 25)) * (seedY | 0xA529L));
//            final int x0y0 = (int) (startX + startY >>> 56),
//                    x1y0 = (int) (endX + startY >>> 56),
//                    x0y1 = (int) (startX + endY >>> 56),
//                    x1y1 = (int) (endX + endY >>> 56);
            final int x0y0 = HastyPointHash.hash256(floorX, floorY, seed),
                    x1y0 = HastyPointHash.hash256(floorX+1, floorY, seed),
                    x0y1 = HastyPointHash.hash256(floorX, floorY+1, seed),
                    x1y1 = HastyPointHash.hash256(floorX+1, floorY+1, seed);
            x -= floorX;
            y -= floorY;
//            x *= x * (3.0 - 2.0 * x);
//            y *= y * (3.0 - 2.0 * y);
//            x *= x;
//            y *= y;
            final double ix = 1.0 - x, iy = 1.0 - y;
//            final double ix = (1 - x) * (1 - x), iy = (1 - y) * (1 - y);
//            final double ix = x, iy = y;
//            x = 1.0 - x;
//            y = 1.0 - y;
            //            double ret = ((ix * SeededNoise.phiGrad2[x0y0][0] + iy * SeededNoise.phiGrad2[x0y0][1])
//                    + (x * SeededNoise.phiGrad2[x1y0][0] + iy * SeededNoise.phiGrad2[x1y0][1])
//                    + (ix * SeededNoise.phiGrad2[x0y1][0] + y * SeededNoise.phiGrad2[x0y1][1])
//                    + (x * SeededNoise.phiGrad2[x1y1][0] + y * SeededNoise.phiGrad2[x1y1][1])
//            );
            return ((SeededNoise.phiGrad2[x0y0][0] + SeededNoise.phiGrad2[x0y0][1]) * (ix * iy)
                    + (SeededNoise.phiGrad2[x1y0][0] + SeededNoise.phiGrad2[x1y0][1]) * (x * iy)
                    + (SeededNoise.phiGrad2[x0y1][0] + SeededNoise.phiGrad2[x0y1][1]) * (ix * y)
                    + (SeededNoise.phiGrad2[x1y1][0] + SeededNoise.phiGrad2[x1y1][1]) * (x * y)
            ) * 0.7071067811865475;
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

    /**
     * A hybrid between value and gradient noise that may be faster for 1D noise. Every integer value of x given to this
     * will produce a result of 0. This only hashes one coordinate per noise call, unlike most value noise that needs 2
     * hashes in 1D and many more in higher dimensions. Based on <a href="https://www.shadertoy.com/view/3sd3Rs">Inigo
     * Quilez' "Basic Noise"</a>.
     */
    public static class QuilezNoise implements Noise1D, Noise2D {

        public long seed;
        public QuilezNoise() {
            this(0xB0BAFE77L);
        }

        public QuilezNoise(long seed) {
            this.seed = seed;
        }
        @Override
        public double getNoise(double x) { 
            return getNoiseWithSeed(x, seed);
        }
//            xFloor = (xFloor ^ xFloor << 7 ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L;
//            xFloor = (xFloor ^ xFloor >>> 41 ^ xFloor >>> 23) * (xFloor ^ xFloor >> 25 | 1) >>> 32;
//            final double h = xFloor * 0x1p-28 - 4.0;
//            seed = (seed ^ xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L; // XLCG, helps randomize upper bits
//            // this next 'h' part finishes randomizing (not amazing quality, but fast), uses only the upper bits,
//            // and reduces it to a double between -8.0 and almost 8.0.
//            final double h = ((seed ^ seed >>> 26) * (seed | 0xA529) >>> 32) * 0x1p-28 - 8.0;

        @Override
        public double getNoiseWithSeed(double x, final long seed) {
            x += ((seed & 0xFFFFFFFFL) ^ (seed >>> 32)) * 0x1p-24; // offset x by between 0.0 and almost 256.0
            final long xFloor = x >= 0.0 ? (long) x : (long) x - 1, // floor of x as a long
                    rise = 1L - ((x >= 0.0 ? (long) (x * 2.0) : (long) (x * 2.0) - 1) & 2L); // either 1 or -1
            x -= xFloor;
            // and now we flip the switch from "magic" to "more magic..."
            // this directly sets the bits that describe a double. this might seem like it should be slow; it is not.
            // seed and xFloor are XORed to roughly mix them together; adding would work too probably.
            // the two huge longs don't really matter except for their last digits:
            // the one that uses ^ must end in 5 or D (both hex) 
            // the one that uses * must end in 3 or B (both hex)
            // and that makes this a valid "XLCG," a cousin of the commonly used LCG random number generator.
            // it improves the randomness in the upper bits more than the lower ones, where the upper bits will become
            // more significant decimal places in the resulting double.
            // we unsigned-right-shift by 12, which puts the random bits all in the double's mantissa (which is how far
            // it is between the previous power of two and next power of two, roughly).
            // we bitwise OR with 0x4030000000000000L, which is the exponent section for a double between 16.0 and 32.0.
            // we work our magic and convert the bits to double.
            // subtracting 24.0 takes the range to -4.0 to 12.0, where we want it (Quilez used this).
            final double h = NumberTools.longBitsToDouble((seed ^ xFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 12 | 
                      0x4040000000000000L) - 52.0;
//                    0x4030000000000000L) - 20.0;
            // Quilez' quartic curve; uses the "rise" calculated earlier to determine if this is on the rising or
            // the falling side. Uses that complicated "h" as the height of the peak or valley in the middle.
            return rise * x * (x - 1.0) * (h * x * (x - 1.0) - 1.0);
        }

        @Override
        public double getNoise(double x, double y) {
            return getNoiseWithSeed(x, y, seed);
        }

        @Override
        public double getNoiseWithSeed(double x, double y, long seed) {
            double n = 0.0;
            for (int i = 0; i < 4; i++) {
                seed += 0x9E3779B97F4A7C15L;
                double xEdit = x += (((seed & 0x55555555L) | (seed >>> 32 & 0xAAAAAAAAL))) * 0x1p-29 + 0.5698402909980532;
                double yEdit = y += (((seed & 0xAAAAAAAAL) | (seed >>> 32 & 0x55555555L))) * 0x1p-29 + 0.7548776662466927;
                final long
                        xFloor = xEdit >= 0.0 ? (long) xEdit : (long) xEdit - 1,
                        yFloor = yEdit >= 0.0 ? (long) yEdit : (long) yEdit - 1;
                xEdit -= xFloor;
                yEdit -= yFloor;
                xEdit *= (xEdit - 1.0);
                yEdit *= (yEdit - 1.0);
                final double m = (xEdit + yEdit) * 0.5;
                final double h = NumberTools.longBitsToDouble(HastyPointHash.hashAll(xFloor, yFloor, seed) >>> 12 | 0x4040000000000000L) - 52.0;
//            final double i = NumberTools.longBitsToDouble((~seed + yFloor ^ 0x9E3779B97F4A7C15L) * 0xD1B54A32D192ED03L >>> 12 | 0x4030000000000000L) - 24.0;
                n += (m * (h * m - 1.0));
            }
            return n * 0.25;
////                    cerp(
//                    0.5 * (
//                            (xRise * m * (h * m - 1.0)) +
//                            (yRise * m * (i * m - 1.0))
//                    );
//                            //, 0.5 + (y - x) * 0.5);
        }

    }
}
