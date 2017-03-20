package squidpony.squidmath;

/**
 * Performance-oriented "white noise" generator for 1D, 2D, 3D, 4D, and 6D. Produces noise values from -1.0 inclusive
 * to 1.0 exclusive. Implementation is based on {@link CrossHash.Wisp#hash64Alt(double[])}, with a special treatment for
 * the seed when given, since it is an int. This can also be used as a sort of hashing function that produces a double,
 * if you find a need for such a thing, with {@link #hash(double...)}.
 */
public class ValueNoise implements Noise.Noise1D, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {

    public static final ValueNoise instance = new ValueNoise();

    public ValueNoise()
    {
    }
    /*
    @Override
    public double getNoise(double x) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoise(double x, double y) {

        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);    }

    @Override
    public double getNoiseWithSeed(double x, double y, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoise(double x, double y, double z) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * u + u * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * v + v * 0x1.39b4dce80194cp9)));
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, int seed) {
        long a = 0x632BE59BD9B4E019L,
                result = 0x9E3779B97F4A7C94L +
                        (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * x + x * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * y + y * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * z + z * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * w + w * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * u + u * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * v + v * 0x1.39b4dce80194cp9)))
                        + (a ^= 0x8329C6EB9E6AD3E3L * seed);
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }
    */
    @Override
    public double getNoise(final double x) {
        return NumberTools.bounce((0x0.9E3779B9p2 * x)
                + (0x0.85157AF5p1 * x)
                + (0x0.8329C6DFp0 * x)
                + (0x0.9E3779B9p2 + x));
    }
    @Override
    public double getNoiseWithSeed(final double x, final int seed) {
        int seed2 = seed;
        return (NumberTools.bounce(0x0.9E3779B9p-14 * x * (seed2 = PintRNG.determine(seed2)))
                + (0x0.85157AF5p-15 * x * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-16 * x * PintRNG.determine(seed2))
                + (0x0.9E3779B9p2 + x));
    }
    @Override
    public double getNoise(final double x, final double y) {
        return NumberTools.bounce((0x0.9E3779B9p2 * x)
                + (0x0.85157AF5p2 * y)
                + (0x0.8329C6DFp2 * (y-x))
                + ((x+0x0.85157AF5p2) * (y+0x0.9E3779B9p2)));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final int seed) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p5 + y*0x0.8329C6DFp4 - x*0x0.953976F9p3) + (seed & 0xA5A5A5A5))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p5 + x*0x0.9E3779B9p4 - y*0x0.8329C6DFp3) + (seed & 0x5A5A5A5A)))
                        + (LightRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p2 + (seed & 0x3C3C3C3C)))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p4 - x*0x2.8329C6DFp2) + (seed & 0xC3C3C3C3))));

    }
    @Override
    public double getNoise(final double x, final double y, final double z) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p5 + y*0x0.8329C6DFp4 + z*0x0.953976F9p3))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p5 + z*0x0.9E3779B9p4 + x*0x0.8329C6DFp3))
                        ^ LightRNG.determine((long)(z*0x0.8329C6DFp5 + x*0x0.953976F9p4 + y*0x0.9E3779B9p3)))
                        + (LightRNG.determine((long)(x*0x0.85157AF5p4 - y*0x0.9E3779B9p3)
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p4 - z*0x0.8329C6DFp3))
                        ^ LightRNG.determine((long)(z+0x0.85157AF5p4 - x*0x0.953976F9p3)))));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p5 + y*0x0.8329C6DFp4 + z*0x0.953976F9p3) + (seed & 0x24924924))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p5 + z*0x0.9E3779B9p4 + x*0x0.8329C6DFp3) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(z*0x0.8329C6DFp5 + x*0x0.953976F9p4 + y*0x0.9E3779B9p3) + (seed & 0x92492492)))
                        + (LightRNG.determine((long)(x*0x0.85157AF5p4 - y*0x0.9E3779B9p3 + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p4 - z*0x0.8329C6DFp3) + (seed & 0x92492492))
                        ^ LightRNG.determine((long)(z+0x0.85157AF5p4 - x*0x0.953976F9p3) + (seed & 0x24924924)))));
/*
        return NumberTools.bounce(
                LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x1.8329C6DFp2 + z*0x2.85157AF5p1) + (seed & 0x24924924))
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p4 + z*0x1.9E3779B9p2 + x*0x2.8329C6DFp1) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(z*0x0.8329C6DFp4 + x*0x1.85157AF5p2 + y*0x2.9E3779B9p1) + (seed & 0x92492492))
                        ^ LightRNG.determine((long)((x+y+z)*0x0.953976F9p3) + (seed & 0x81818181))
                        ^ LightRNG.determine((long)(x*0x0.8329C6DFp3-y*0x2.85157AF5p1) + (seed & 0x18181818))
                        ^ LightRNG.determine((long)(y*0x0.9E3779B9p3-z*0x2.8329C6DFp1) + (seed & 0x42424242))
                        ^ LightRNG.determine((long)(z*0x0.85157AF5p3-x*0x2.9E3779B9p1) + (seed & 0x24242424)));

 */
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return NumberTools.bounce(
                LightRNG.determine((long)((x + y)*0x0.8329C6DFp5))// + (seed & 0x81818181))
                        ^ LightRNG.determine((long)((y + z)*0x0.9E3779B9p5))// + (seed & 0x18181818))
                        ^ LightRNG.determine((long)((z + w)*0x0.85157AF5p5))// + (seed & 0x42424242))
                        ^ LightRNG.determine((long)((w + x)*0x0.953976F9p5))// + (seed & 0x24242424))
                        );
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p5 + y*0x0.8329C6DFp4 + w*0x0.953976F9p3) + (seed & 0x81818181))
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p5 + z*0x0.9E3779B9p4 + x*0x0.8329C6DFp3) + (seed & 0x18181818))
                        ^ LightRNG.determine((long)(z*0x0.953976F9p5 + w*0x0.85157AF5p4 + y*0x0.9E3779B9p3) + (seed & 0x42424242))
                        ^ (LightRNG.determine((long)(w*0x0.8329C6DFp5 + x*0x0.953976F9p4 + z*0x0.85157AF5p3)+ (seed & 0x24242424)))
                        ));


        /*(0x0.9E3779B9p-15 * x * (seed2 = PintRNG.determine(seed2)))
                + (0x0.85157AF5p-15 * y * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-15 * z * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-15 * w * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-14 * (y-x) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.9E3779B9p-14 * (z-w) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.85157AF5p-14 * (x-z) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-14 * (w-y) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.9E3779B9p-16 * (y+z-w) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.85157AF5p-16 * (x-z+w) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-16 * (x-y+w) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-16 * (y+z-x) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-17 * (x+z+y+w) * PintRNG.determine(seed2))*/
                /*
          return NumberTools.bounce(
                 ((x*y*0x0.8329C6DFp4) + (y*z*0x0.9E3779B9p4) + (z*w*0x0.85157AF5p4) + (w*x*0x0.85157AF5p4)
                        - (x*z*0x0.85157AF5p4) - (y*w*0x0.85157AF5p4) - (seed * 0x0.953976F9p-30)));
                        */
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        double a = 0x0.85157AF5p-30 * (0x14499933 & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (0x14499933 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp2 * x) * (b += 0x0.85157AF5p2 * y) * 0xfcdab719
                + (a += 0x0.632BE5ABp2 * z) * (b += 0x0.85157AF5p2 * w) * 0xa0bfb8b0
                + (a + 0x0.632BE5ABp2 * u) * (b + 0x0.85157AF5p2 * v) * 0x58c8297d);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final int seed) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p5 + y*0x0.8329C6DFp4 + z*0x0.953976F9p3 + w*0x0.85157AF5p2) + (seed & 0x24924924))
                        ^ LightRNG.determine((long)(y*0x0.712BE5ABp5 + z*0x0.9E3779B9p4 + x*0x0.8329C6DFp3 + u*0x0.953976F9p2) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(z*0x0.85157AF5p5 + x*0x0.712BE5ABp4 + y*0x0.9E3779B9p3 + v*0x0.8329C6DFp2) + (seed & 0x92492492))
                        ^ LightRNG.determine((long)(w*0x0.953976F9p5 + u*0x0.85157AF5p4 + v*0x0.712BE5ABp3 + x*0x0.9E3779B9p2) + (seed & 0x24924924))
                        ^ LightRNG.determine((long)(u*0x0.8329C6DFp5 + v*0x0.953976F9p4 + w*0x0.85157AF5p3 + y*0x0.712BE5ABp2) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(v*0x0.9E3779B9p5 + w*0x0.8329C6DFp4 + u*0x0.953976F9p3 + z*0x0.85157AF5p2) + (seed & 0x92492492))));


        /*
        int seed2 = seed;
        return NumberTools.bounce((0x0.9E3779B9p-15 * x * (seed2 = PintRNG.determine(seed2)))
                + (0x0.85157AF5p-15 * y * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-15 * z * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-15 * w * (seed2 = PintRNG.determine(seed2)))
                + (-0x0.9E3779B9p-15 * u * (seed2 = PintRNG.determine(seed2)))
                + (-0x0.85157AF5p-15 * v * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-16 * (y-x) * (seed2 = PintRNG.determine(seed2)))
                + (-0x0.9E3779B9p-16 * (w-z) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-16 * (v-u) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.85157AF5p-16 * (x-z) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-16 * (w-y) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-16 * (x-v) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-16 * (u-w) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-16 * (z-v) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.8329C6DFp-16 * (u-y) * (seed2 = PintRNG.determine(seed2)))
                + (0x0.953976F9p-17 * (x+z+y+w+u+v) * PintRNG.determine(seed2))
                + ((x+0x0.85157AF5p2) * (y+0x0.8329C6DFp2) * (z+0x0.953976F9p2)
                * (w-0x0.9E3779B9p2) * (u-0x0.85157AF5p2) * (v+0x0.9E3779B9p2))
        );
        */
    }

    /*
    @Override
    public double getNoise(final double x, final double y) {
        double a = 0x0.85157AF5p-30 * (0x14499933 & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (0x14499933 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp1 * x) * (b += 0x0.85157AF5p1 * y) * 0xfcdab719
                + (a += 0x0.632BE5ABp1 * y) * (b += 0x0.85157AF5p1 * x) * 0xa0bfb8b0
        );
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final int seed) {
        int seed2 = seed;
        double a = 0x0.85157AF5p-30 * ((seed2 = PintRNG.determine(seed2)) & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (seed2 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp16 * x) * (b += 0x0.85157AF5p16 * y) * (seed2 = PintRNG.determine(seed2))
                + (a + 0x0.632BE5ABp16 * y) * (b + 0x0.85157AF5p16 * x) * PintRNG.determine(seed2));
    }

    @Override
    public double getNoise(final double x, final double y, final double z) {
        double a = 0x0.85157AF5p-30 * (0x14499933 & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (0x14499933 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp2 * x) * (b += 0x0.85157AF5p2 * y) * 0xfcdab719
                + (a += 0x0.632BE5ABp2 * z) *  (b += 0x0.85157AF5p2 * x) * 0xa0bfb8b0
                + (a + 0x0.632BE5ABp2 * y) * (b + 0x0.85157AF5p2 * z) * 0x58c8297d
        );
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
        int seed2 = seed;
        double a = 0x0.85157AF5p-30 * ((seed2 = PintRNG.determine(seed2)) & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (seed2 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp2 * x) * (b += 0x0.85157AF5p2 * y) * (seed2 = PintRNG.determine(seed2))
                + (a += 0x0.632BE5ABp2 * z) *  (b += 0x0.85157AF5p2 * x) * (seed2 = PintRNG.determine(seed2))
                + (a + 0x0.632BE5ABp2 * y) * (b + 0x0.85157AF5p2 * z) * PintRNG.determine(seed2)
        );
    }

    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        double a = 0x0.85157AF5p-30 * (0x14499933 & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (0x14499933 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp2 * x) * (b += 0x0.85157AF5p2 * y) * 0xfcdab719
                + (a += 0x0.632BE5ABp2 * z) * (b += 0x0.85157AF5p2 * w) * 0xa0bfb8b0
                + (b += 0x0.632BE5ABp2 * x) * (a += 0x0.85157AF5p2 * y) * 0x58c8297d
                + (b + 0x0.632BE5ABp2 * z) * (a + 0x0.85157AF5p2 * w) * 0xaaf0e2aa);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
        int seed2 = seed;
        double a = 0x0.85157AF5p-30 * ((seed2 = PintRNG.determine(seed2)) & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (seed2 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp+2 * x) * (b += 0x0.85157AF5p+2 * y) * (seed2 = PintRNG.determine(seed2))
                + (a + 0x0.632BE5ABp+2 * z) * (b + 0x0.85157AF5p+2 * w) * PintRNG.determine(seed2));
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        double a = 0x0.85157AF5p-30 * (0x14499933 & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (0x14499933 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp2 * x) * (b += 0x0.85157AF5p2 * y) * 0xfcdab719
                + (a += 0x0.632BE5ABp2 * z) * (b += 0x0.85157AF5p2 * w) * 0xa0bfb8b0
                + (a + 0x0.632BE5ABp2 * u) * (b + 0x0.85157AF5p2 * v) * 0x58c8297d);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final int seed) {
        int seed2 = seed;
        double a = 0x0.85157AF5p-30 * ((seed2 = PintRNG.determine(seed2)) & 0xABAAAAAB), b = 0x0.8329C6DFp-30 * (seed2 & 0xD55555D5);
        return NumberTools.bounce(0x0.9E3779B9p0
                + (a += 0x0.632BE5ABp2 * x) * (b += 0x0.85157AF5p2 * y) * (seed2 = PintRNG.determine(seed2))
                + (a += 0x0.632BE5ABp2 * z) * (b += 0x0.85157AF5p2 * w) * (seed2 = PintRNG.determine(seed2))
                + (a + 0x0.632BE5ABp2 * u) * (b + 0x0.85157AF5p2 * v) * PintRNG.determine(seed2));
    }
*/

    /**
     * Hashes the input double array or vararg and produces a double with unpredictable value, between -1.0 inclusive
     * and 1.0 exclusive.
     * @param data
     * @return
     */
    public double hash(double... data) {
        if(data == null) return 0.0;
        long a = 0x632BE59BD9B4E019L, result = 0x9E3779B97F4A7C94L;
        double t;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * 0x1.39b4dce80194cp9)));
        }
        return ((result * (a | 1L) ^ (result >>> 27 | result << 37)) & 0x1fffffffffffffL) * 0x1p-53 - (a & 1L);
    }

}
