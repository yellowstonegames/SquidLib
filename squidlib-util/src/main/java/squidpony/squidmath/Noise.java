package squidpony.squidmath;

/**
 * Created by Tommy Ettinger on 3/17/2017.
 */
public class Noise {
    public static final SeededNoise alternate = new SeededNoise(0xFEEDCAFE);

    public interface Noise1D {
        double getNoise(double x);
        double getNoiseWithSeed(double x, int seed);
    }

    public interface Noise2D {
        double getNoise(double x, double y);
        double getNoiseWithSeed(double x, double y, int seed);
    }

    public interface Noise3D {
        double getNoise(double x, double y, double z);
        double getNoiseWithSeed(double x, double y, double z, int seed);
    }

    public interface Noise4D {
        double getNoise(double x, double y, double z, double w);
        double getNoiseWithSeed(double x, double y, double z, double w, int seed);
    }

    public interface Noise6D {
        double getNoise(double x, double y, double z, double w, double u, double v);
        double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed);
    }

    public static class Layered1D implements Noise1D {
        protected int octaves;
        protected Noise1D basis;

        public Layered1D() {
            throw new UnsupportedOperationException("No known 1D noise implementation present in the library");
        }

        public Layered1D(Noise1D basis) {
            this(basis, 2);
        }

        public Layered1D(Noise1D basis, final int octaves) {
            this.basis = basis;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= 2.0) + o) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final int seed) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                n += basis.getNoiseWithSeed(x * (i_s *= 2.0) + (o << 6), seed2) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }

    public static class Layered2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis;

        public Layered2D() {
            this(SeededNoise.instance, 2);
        }

        public Layered2D(Noise2D basis) {
            this(basis, 2);
        }

        public Layered2D(Noise2D basis, final int octaves) {
            this.basis = basis;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final int seed) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                n += basis.getNoiseWithSeed(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7), seed2) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis;

        public Layered3D() {
            this(SeededNoise.instance, 2);
        }

        public Layered3D(Noise3D basis) {
            this(basis, 2);
        }

        public Layered3D(Noise3D basis, final int octaves) {
            this.basis = basis;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y, final double z) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                n += basis.getNoiseWithSeed(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), seed2) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis;

        public Layered4D() {
            this(SeededNoise.instance, 2);
        }

        public Layered4D(Noise4D basis) {
            this(basis, 2);
        }

        public Layered4D(Noise4D basis, final int octaves) {
            this.basis = basis;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y, final double z, final double w) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                n += basis.getNoiseWithSeed(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9), seed2) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }
    public static class Layered6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis;

        public Layered6D() {
            this(SeededNoise.instance, 2);
        }

        public Layered6D(Noise6D basis) {
            this(basis, 2);
        }

        public Layered6D(Noise6D basis, final int octaves) {
            this.basis = basis;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1);
            for (int o = 0; o < octaves; o++, s >>= 1) {
                n += basis.getNoise(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                        , w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11)) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final int seed) {
            double n = 0.0, i_s = 1.0;

            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                n += basis.getNoiseWithSeed(x * (i_s *= 2.0) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                        , w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11), seed2) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }

    public static class Ridged2D implements Noise2D {
        protected int octaves;
        protected double frequency;
        protected Noise2D basis;

        public Ridged2D() {
            this(SeededNoise.instance, 2, 1.25);
        }

        public Ridged2D(Noise2D basis) {
            this(basis, 2, 1.25);
        }

        public Ridged2D(Noise2D basis, int octaves, double frequency) {
            this.basis = basis;
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            this.frequency = frequency;
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
            return sum;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, int seed) {
            double sum = 0, amp = 1.0;
            x *= frequency;
            y *= frequency;
            for (int i = 0; i < octaves; ++i) {
                seed = PintRNG.determine(seed);
                double n = basis.getNoiseWithSeed(x + (i << 6), y + (i << 7), seed);
                n = 1.0 - Math.abs(n);
                sum += amp * n;
                amp *= 0.5;
                x *= 2.0;
                y *= 2.0;
            }
            return sum;
        }
    }

    public static class Ridged3D implements Noise3D {
        protected int octaves;
        protected double frequency;
        protected Noise3D basis;

        public Ridged3D() {
            this(SeededNoise.instance, 2, 1.25);
        }

        public Ridged3D(Noise3D basis) {
            this(basis, 2, 1.25);
        }

        public Ridged3D(Noise3D basis, int octaves, double frequency) {
            this.basis = basis;
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            this.frequency = frequency;
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
            return sum;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, int seed) {
            double sum = 0, amp = 1.0;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            for (int i = 0; i < octaves; ++i) {
                seed = PintRNG.determine(seed);
                double n = basis.getNoiseWithSeed(x + (i << 6), y + (i << 7), z + (i << 8), seed);
                n = 1.0 - Math.abs(n);
                sum += amp * n;
                amp *= 0.5;
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
            }
            return sum;
        }
    }


    public static class Ridged4D implements Noise4D {
        protected double[] exp, correct;
        protected int octaves;
        protected double frequency;
        public Noise4D basis;

        public Ridged4D() {
            this(SeededNoise.instance, 2, 1.25);
        }

        public Ridged4D(Noise4D basis) {
            this(basis, 2, 1.25);
        }

        public Ridged4D(Noise4D basis, int octaves, double frequency) {
            this.basis = basis;
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            this.frequency = frequency;
            exp = new double[octaves];
            correct = new double[octaves];
            double maxvalue = 0.0;
            for (int i = 0; i < octaves; ++i) {
                maxvalue += (exp[i] = Math.pow(2.0, -0.9 * i));
                correct[i] = 2.0 / maxvalue;
            }
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
                n *= n;
                sum += n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
            }
            return sum * correct[octaves - 1] - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
            double sum = 0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            for (int i = 0; i < octaves; ++i) {
                seed = PintRNG.determine(seed);
                n = basis.getNoiseWithSeed(x + (i << 6), y + (i << 7), z + (i << 8), w + (i << 9), seed);
                n = 1.0 - Math.abs(n);
                n *= n;
                sum += n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
            }
            return sum * correct[octaves - 1] - 1.0;
        }
    }


    public static class Ridged6D implements Noise6D
    {
        protected double[] exp, correct;
        protected int octaves;
        protected double frequency;
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
            this.octaves = (octaves = Math.max(1, Math.min(63, octaves)));
            this.frequency = frequency;
            exp = new double[octaves];
            correct = new double[octaves];
            double maxvalue = 0.0;
            for (int i = 0; i < octaves; ++i) {
                maxvalue += (exp[i] = Math.pow(2.0, -0.9 * i));
                correct[i] = 2.0 / maxvalue;
            }
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
                n *= n;
                sum += n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
            }
            return sum * correct[octaves - 1] - 1.0;
        }

        @Override
        public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
            double sum = 0, n;
            x *= frequency;
            y *= frequency;
            z *= frequency;
            w *= frequency;
            u *= frequency;
            v *= frequency;
            for (int i = 0; i < octaves; ++i) {
                seed = PintRNG.determine(seed);
                n = basis.getNoiseWithSeed(x + (i << 6), y + (i << 7), z + (i << 8),
                        w + (i << 9), u + (i << 10), v + (i << 11), seed);
                n = 1.0 - Math.abs(n);
                n *= n;
                sum += n * exp[i];
                x *= 2.0;
                y *= 2.0;
                z *= 2.0;
                w *= 2.0;
                u *= 2.0;
                v *= 2.0;
            }
            return sum * correct[octaves - 1] - 1.0;
        }

    }
    public static class Turbulent2D implements Noise2D {
        protected int octaves;
        protected Noise2D basis, disturbance;

        public Turbulent2D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent2D(Noise2D basis, Noise2D disturb) {
            this(basis, disturb, 1);
        }

        public Turbulent2D(Noise2D basis, Noise2D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }

        @Override
        public double getNoise(final double x, final double y) {
            double n = 0.0, i_s = 1.0, xx, yy;
            int s = 2 << (octaves - 1);
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                n += basis.getNoise(xx, yy) * s + disturbance.getNoise(xx * s, yy * s) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy;

            int s = 2 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                n += basis.getNoiseWithSeed(xx, yy, seed2) * s + disturbance.getNoiseWithSeed(xx * s, yy * s, seed2) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }
    }
    public static class Turbulent3D implements Noise3D {
        protected int octaves;
        protected Noise3D basis, disturbance;

        public Turbulent3D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent3D(Noise3D basis, Noise3D disturb) {
            this(basis, disturb, 1);
        }

        public Turbulent3D(Noise3D basis, Noise3D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        @Override
        public double getNoise(final double x, final double y, final double z) {
            double n = 0.0, i_s = 1.0, xx, yy, zz;
            int s = 2 << (octaves - 1);
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                n += basis.getNoise(xx, yy, zz) * s + disturbance.getNoise(xx * s, yy * s, zz * s) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz;
            int s = 2 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                n += basis.getNoiseWithSeed(xx, yy, zz, seed2) * s + disturbance.getNoiseWithSeed(xx * s, yy * s, zz * s, seed2) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }
    }
    public static class Turbulent4D implements Noise4D {
        protected int octaves;
        protected Noise4D basis, disturbance;

        public Turbulent4D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent4D(Noise4D basis, Noise4D disturb) {
            this(basis, disturb, 1);
        }

        public Turbulent4D(Noise4D basis, Noise4D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        @Override
        public double getNoise(final double x, final double y, final double z, final double w) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww;
            int s = 2 << (octaves - 1);
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                n += basis.getNoise(xx, yy, zz, ww) * s + disturbance.getNoise(xx * s, yy * s, zz * s, ww * s) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww;
            int s = 2 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                n += basis.getNoiseWithSeed(xx, yy, zz, ww, seed2) * s + disturbance.getNoiseWithSeed(xx * s, yy * s, zz * s, ww * s, seed2) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }
    }
    public static class Turbulent6D implements Noise6D {
        protected int octaves;
        protected Noise6D basis, disturbance;

        public Turbulent6D() {
            this(SeededNoise.instance, alternate, 1);
        }

        public Turbulent6D(Noise6D basis, Noise6D disturb) {
            this(basis, disturb, 1);
        }

        public Turbulent6D(Noise6D basis, Noise6D disturb, final int octaves) {
            this.basis = basis;
            disturbance = disturb;
            this.octaves = Math.max(1, Math.min(63, octaves));
        }
        @Override
        public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww, uu, vv;
            int s = 2 << (octaves - 1);
            for (int o = 0; o < octaves; o++) {
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                uu = u * i_s + (o << 10);
                vv = v * i_s + (o << 11);
                n += basis.getNoise(xx, yy, zz, ww, uu, vv) * s + disturbance.getNoise(xx * s, yy * s, zz * s, ww * s, uu * s, vv * s) * (s >>= 1);
            }
            return n / ((3 << octaves) - 3.0);
        }

        @Override
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww, uu, vv;
            int s = 2 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 2.0) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                uu = u * i_s + (o << 10);
                vv = v * i_s + (o << 11);
                n += basis.getNoiseWithSeed(xx, yy, zz, ww, uu, vv, seed2) * s + disturbance.getNoiseWithSeed(xx * s, yy * s, zz * s, ww * s, uu * s, vv * s, seed2) * (s >>= 1);
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
        public double getNoiseWithSeed(final double x, final double y, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy;

            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, seed2), yy, seed2) * s;
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
        public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz;
            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, z, seed2), yy, zz, seed2) * s;
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
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww;
            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, z, w, seed2), yy, zz, ww, seed2) * s;
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
        public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final int seed) {
            double n = 0.0, i_s = 1.0, xx, yy, zz, ww, uu, vv;
            int s = 1 << (octaves - 1), seed2 = seed;
            for (int o = 0; o < octaves; o++, s >>= 1) {
                seed2 = PintRNG.determine(seed2);
                xx = x * (i_s *= 0.5) + (o << 6);
                yy = y * i_s + (o << 7);
                zz = z * i_s + (o << 8);
                ww = w * i_s + (o << 9);
                uu = u * i_s + (o << 10);
                vv = v * i_s + (o << 11);
                n += basis.getNoiseWithSeed(xx + disturbance.getNoiseWithSeed(x, y, z, w, u, v, seed2), yy, zz, ww, uu, vv, seed2) * s;
            }
            return n / ((1 << octaves) - 1.0);
        }
    }

}
