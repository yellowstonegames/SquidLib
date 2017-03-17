package squidpony.squidmath;

/**
 * Created by Tommy Ettinger on 3/17/2017.
 */
public class Noise {
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

    public static Noise1D layerOctaves1D(final Noise1D basis, final int octaves)
    {
        return new Noise1D() {
            @Override
            public double getNoise(final double x) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1);
                for (int o = 0; o < octaves; o++, s>>=1) {
                    n += basis.getNoise(x * (i_s *= 0.5) + o) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }

            @Override
            public double getNoiseWithSeed(final double x, final int seed) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1), seed2 = seed;
                for (int o = 0; o < octaves; o++, s>>=1) {
                    seed2 = PintRNG.determine(seed2);
                    n += basis.getNoiseWithSeed(x * (i_s *= 0.5) + (o << 6), seed2) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }
        };
    }

    public static Noise2D layerOctaves2D(final Noise2D basis, final int octaves)
    {
        return new Noise2D() {
            @Override
            public double getNoise(final double x, final double y) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1);
                for (int o = 0; o < octaves; o++, s>>=1) {
                    n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7)) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }

            @Override
            public double getNoiseWithSeed(final double x, final double y, final int seed) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1), seed2 = seed;
                for (int o = 0; o < octaves; o++, s>>=1) {
                    seed2 = PintRNG.determine(seed2);
                    n += basis.getNoiseWithSeed(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), seed2) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }
        };
    }
    public static Noise3D layerOctaves3D(final Noise3D basis, final int octaves)
    {
        return new Noise3D() {
            @Override
            public double getNoise(final double x, final double y, final double z) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1);
                for (int o = 0; o < octaves; o++, s>>=1) {
                    n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }

            @Override
            public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1), seed2 = seed;
                for (int o = 0; o < octaves; o++, s>>=1) {
                    seed2 = PintRNG.determine(seed2);
                    n += basis.getNoiseWithSeed(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), seed2) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }
        };
    }
    public static Noise4D layerOctaves4D(final Noise4D basis, final int octaves)
    {
        return new Noise4D() {
            @Override
            public double getNoise(final double x, final double y, final double z, final double w) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1);
                for (int o = 0; o < octaves; o++, s>>=1) {
                    n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9)) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }

            @Override
            public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1), seed2 = seed;
                for (int o = 0; o < octaves; o++, s>>=1) {
                    seed2 = PintRNG.determine(seed2);
                    n += basis.getNoiseWithSeed(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8), w * i_s + (o << 9), seed2) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }
        };
    }
    public static Noise6D layerOctaves6D(final Noise6D basis, final int octaves)
    {
        return new Noise6D() {
            @Override
            public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1);
                for (int o = 0; o < octaves; o++, s>>=1) {
                    n += basis.getNoise(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                            , w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11)) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }

            @Override
            public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final int seed) {
                double n = 0.0, i_s = 1.0;

                int s = 1<<(octaves-1), seed2 = seed;
                for (int o = 0; o < octaves; o++, s>>=1) {
                    seed2 = PintRNG.determine(seed2);
                    n += basis.getNoiseWithSeed(x * (i_s *= 0.5) + (o << 6), y * i_s + (o << 7), z * i_s + (o << 8)
                            , w * i_s + (o << 9), u * i_s + (o << 10), v * i_s + (o << 11), seed2) * s;
                }
                return n / ((1<<octaves) - 1.0);
            }
        };
    }
    public static Noise2D ridged2D(final Noise2D basis, final int octaves, final double frequency)
    {
        return new Noise2D() {
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
        };
    }

    public static Noise3D ridged3D(final Noise3D basis, final int octaves, final double frequency)
    {
        return new Noise3D() {
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
        };
    }

    public static Noise4D ridged4D(final Noise4D basis, final int octaves, final double frequency)
    {
        final double[] exp = new double[octaves], correct = new double[octaves];
        double maxvalue = 0.0;
        for (int i = 0; i < octaves; ++i) {
            maxvalue += (exp[i] = Math.pow(2.0, -0.9 * i));
            correct[i] = 2.0 / maxvalue;
        }
        return new Noise4D() {
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
        };
    }
    public static Noise6D ridged6D(final Noise6D basis, final int octaves, final double frequency)
    {
        final double[] exp = new double[octaves], correct = new double[octaves];
        double maxvalue = 0.0;
        for (int i = 0; i < octaves; ++i) {
            maxvalue += (exp[i] = Math.pow(2.0, -0.9 * i));
            correct[i] = 2.0 / maxvalue;
        }
        return new Noise6D() {
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
        };
    }
}
