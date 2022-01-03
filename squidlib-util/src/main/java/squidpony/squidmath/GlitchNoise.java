package squidpony.squidmath;

/**
 * A noise generator for 1D, 2D, 3D, 4D, or 6D noise that should look "glitchy", with waves of changing values moving
 * through triangular shapes. Intended for aesthetic purposes where something needs to look inorganic, unlike Perlin
 * or Simplex noise.
 */
public class GlitchNoise implements Noise.Noise1D, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {

    public static final GlitchNoise instance = new GlitchNoise();
    public static double zigzagRandomized(long seed, double value)
    {
        final long floor = value >= 0.0 ? (long) value : (long) value - 1L;
        final double start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-62,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-62;
        value -= floor;
//        value *= value * (3.0 - 2.0 * value);
        return (1.0 - value) * start + value * end;
    }

    public GlitchNoise()
    {
    }
    @Override
    public double getNoise(final double x) {
        return getNoiseWithSeed(x, 0xAEF17502108EF2D9L);
//        return NumberTools.randomSignedDouble((((long)(x + zigzagRandomized(0xAEF17502108EF2D9L, -x)) * 0x6C8E9CF570932BD5L
//        ) >>> 32));

//        return NumberTools.bounce(
//                TangleRNG.determine((long)(x*0x0.9E3779B9p4 - x*0x0.8329C6DFp3 + x*0x0.953976F9p2))
//                        ^ TangleRNG.determine((long)(x*0x0.953976F9p4 + x*0x0.8329C6DFp3 - x*0x0.9E3779B9p2)));
    }
    @Override
    public double getNoiseWithSeed(final double x, final long seed) {
        return NumberTools.randomSignedDouble((((long)(x + zigzagRandomized(seed, -x)) * 0x6C8E9CF570932BD5L
                ) >>> 32) ^ seed);

//        return NumberTools.bounce(
//                TangleRNG.determine((long)(x*0x0.9E3779B9p4 - x*0x0.8329C6DFp3 + x*0x0.953976F9p2) + (seed & 0xA5A5A5A5))
//                        ^ TangleRNG.determine((long)(x*0x0.953976F9p4 + x*0x0.8329C6DFp3 - x*0x0.9E3779B9p2) + (seed & 0x5A5A5A5A)));
    }
    @Override
    public double getNoise(final double x, final double y) {
        return getNoiseWithSeed(x, y, 0xAEF17502108EF2D9L);//        return NumberTools.bounce(
//                TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 - x*0x0.953976F9p2))
//                        ^ TangleRNG.determine((long)(y*0x0.953976F9p4 + x*0x0.9E3779B9p3 - y*0x0.8329C6DFp2))
//                        ^ TangleRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p2 + x*0x4.953976F9p0))
//                        ^ TangleRNG.determine((long)(y*0x0.953976F9p4 - x*0x2.8329C6DFp2 + y*0x4.85157AF5p0)));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        return NumberTools.randomSignedDouble((((long)(x + zigzagRandomized(seed, y)) * 0x6C8E9CF570932BD5L
                ^ (long)(y + zigzagRandomized(seed + 0xAEF17502108EF2D9L, x)) * 0x9E3779B97F4A7C15L) >>> 32)
                ^ seed);
//        return NumberTools.bounce(
//                TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 - x*0x0.953976F9p2) + (seed & 0xA5A5A5A5))
//                        ^ TangleRNG.determine((long)(y*0x0.953976F9p4 + x*0x0.9E3779B9p3 - y*0x0.8329C6DFp2) + (seed & 0x5A5A5A5A))
//                        ^ TangleRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p2 - x*0x4.953976F9p0) + (seed & 0x3C3C3C3C))
//                        ^ TangleRNG.determine((long)(y*0x0.953976F9p4 - x*0x2.8329C6DFp2 - y*0x4.85157AF5p0) + (seed & 0xC3C3C3C3)));
    }
    @Override
    public double getNoise(final double x, final double y, final double z) {
        return getNoiseWithSeed(x, y, z, 0xAEF17502108EF2D9L);//        return NumberTools.randomSignedDouble(NumberTools.doubleToLongBits(x) * 0x9E3779B97F4A7C15L
//                + NumberTools.doubleToLongBits(y) * 0x6C8E9CF570932BD5L
//                + NumberTools.doubleToLongBits(z) * 0xC6BC279692B5CC85L);
//        return NumberTools.bounce(
//                TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2))
//                        ^ TangleRNG.determine((long)(y*0x0.953976F9p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2))
//                        ^ TangleRNG.determine((long)(z*0x0.8329C6DFp4 + x*0x0.953976F9p3 + y*0x0.9E3779B9p2))
//                        ^ TangleRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p1 - z*0x3.953976F9p0))
//                        ^ TangleRNG.determine((long)(y*0x0.85157AF5p4 - z*0x2.8329C6DFp1 - y*0x3.9E3779B9p0))
//                        ^ TangleRNG.determine((long)(z+0x0.85157AF5p4 - x*0x2.953976F9p1 - y*0x3.8329C6DFp0)));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        return NumberTools.randomSignedDouble((((long)(x + zigzagRandomized(seed, y) + zigzagRandomized(seed, z)) * 0x6C8E9CF570932BD5L
                ^ (long)(y + zigzagRandomized(seed + 0xAEF17502108EF2D9L, x) + zigzagRandomized(seed + 0xAEF17502108EF2D9L, z)) * 0x9E3779B97F4A7C15L
                ^ (long)(z + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, x) + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, y)) * 0xC6BC279692B5CC85L
        ) >>> 32)
                ^ seed);
//        return NumberTools.randomSignedDouble(NumberTools.doubleToLongBits(x) * 0x9E3779B97F4A7C15L
//                + NumberTools.doubleToLongBits(y) * 0x6C8E9CF570932BD5L
//                + NumberTools.doubleToLongBits(z) * 0xC6BC279692B5CC85L 
//                ^ seed);
//        return NumberTools.bounce(
//                TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2) + (seed & 0x24924924))
//                        ^ TangleRNG.determine((long)(y*0x0.953976F9p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2) + (seed & 0x49249249))
//                        ^ TangleRNG.determine((long)(z*0x0.8329C6DFp4 + x*0x0.953976F9p3 + y*0x0.9E3779B9p2) + (seed & 0x92492492))
//                        ^ TangleRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p1 - z*0x3.953976F9p0) + (seed & 0x49249249))
//                        ^ TangleRNG.determine((long)(y*0x0.85157AF5p4 - z*0x2.8329C6DFp1 - y*0x3.9E3779B9p0) + (seed & 0x92492492))
//                        ^ TangleRNG.determine((long)(z+0x0.85157AF5p4 - x*0x2.953976F9p1 - y*0x3.8329C6DFp0) + (seed & 0x24924924)));
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return getNoiseWithSeed(x, y, z, w, 0xAEF17502108EF2D9L);
        //        return NumberTools.bounce(
//                (TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + w*0x0.953976F9p2))
//                        ^ TangleRNG.determine((long)(y*0x0.85157AF5p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2))
//                        ^ TangleRNG.determine((long)(z*0x0.953976F9p4 + w*0x0.85157AF5p3 + y*0x0.9E3779B9p2))
//                        ^ TangleRNG.determine((long)(w*0x0.8329C6DFp4 + x*0x0.953976F9p3 + z*0x0.85157AF5p2))
//                ));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        return NumberTools.randomSignedDouble((((long)(x 
                + zigzagRandomized(seed, y) 
                + zigzagRandomized(seed, z) 
                + zigzagRandomized(seed, w)) * 0x6C8E9CF570932BD5L
                ^ (long)(y
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, x)
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, z)
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, w)) * 0x9E3779B97F4A7C15L
                ^ (long)(z
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, x)
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, y)
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, w)) * 0xC6BC279692B5CC85L
                ^ (long)(w
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, x)
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, y)
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, z)) * 0x352E9CF570932BDDL
        ) >>> 32)
                ^ seed);

//        return NumberTools.bounce(
//                (TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + w*0x0.953976F9p2) + (seed & 0x81818181))
//                        ^ TangleRNG.determine((long)(y*0x0.85157AF5p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2) + (seed & 0x18181818))
//                        ^ TangleRNG.determine((long)(z*0x0.953976F9p4 + w*0x0.85157AF5p3 + y*0x0.9E3779B9p2) + (seed & 0x42424242))
//                        ^ TangleRNG.determine((long)(w*0x0.8329C6DFp4 + x*0x0.953976F9p3 + z*0x0.85157AF5p2) + (seed & 0x24242424))
//                        ));
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return getNoiseWithSeed(x, y, z, w, u, v, 0xAEF17502108EF2D9L);

//        return NumberTools.bounce(
//                (TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2 + w*0x0.85157AF5p1))
//                        ^ TangleRNG.determine((long)(y*0x0.712BE5ABp4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2 + u*0x0.953976F9p1))
//                        ^ TangleRNG.determine((long)(z*0x0.85157AF5p4 + x*0x0.712BE5ABp3 + y*0x0.9E3779B9p2 + v*0x0.8329C6DFp1))
//                        ^ TangleRNG.determine((long)(w*0x0.953976F9p4 + u*0x0.85157AF5p3 + v*0x0.712BE5ABp2 + x*0x0.9E3779B9p1))
//                        ^ TangleRNG.determine((long)(u*0x0.8329C6DFp4 + v*0x0.953976F9p3 + w*0x0.85157AF5p2 + y*0x0.712BE5ABp1))
//                        ^ TangleRNG.determine((long)(v*0x0.9E3779B9p4 + w*0x0.8329C6DFp3 + u*0x0.953976F9p2 + z*0x0.85157AF5p1))));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final long seed) {
//        return NumberTools.randomSignedDouble((((long)(x + zigzagRandomized(seed, y)) * 0x6C8E9CF570932BD5L
//                ^ (long)(y + zigzagRandomized(seed + 0xAEF17502108EF2D9L, z)) * 0x9E3779B97F4A7C15L
//                ^ (long)(z + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, w)) * 0xC6BC279692B5CC85L
//                ^ (long)(w + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, u)) * 0x352E9CF570932BDDL
//                ^ (long)(u + zigzagRandomized(seed + 0xBBC5D408423BCB64L, v)) * 0x5851F42D4C957F2DL
//                ^ (long)(v + zigzagRandomized(seed + 0x6AB7490A52CABE3DL, x)) * 0x2545F4914F6CDD1DL
//        ) >>> 32)
//                ^ seed);
        return NumberTools.randomSignedDouble((((long)(x
                + zigzagRandomized(seed, y)
                + zigzagRandomized(seed, z)
                + zigzagRandomized(seed, w)
                + zigzagRandomized(seed, u)
                + zigzagRandomized(seed, v)) * 0x6C8E9CF570932BD5L
                ^ (long)(y
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, x)
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, z)
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, w)
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, u)
                + zigzagRandomized(seed + 0xAEF17502108EF2D9L, v)) * 0x9E3779B97F4A7C15L
                ^ (long)(z
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, x)
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, y)
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, w)
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, u)
                + zigzagRandomized(seed + 0x5DE2EA04211DE5B2L, v)) * 0xC6BC279692B5CC85L
                ^ (long)(w
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, x)
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, y)
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, z)
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, u)
                + zigzagRandomized(seed + 0x0CD45F0631ACD88BL, v)) * 0x352E9CF570932BDDL
                ^ (long)(u
                + zigzagRandomized(seed + 0xBBC5D408423BCB64L, x)
                + zigzagRandomized(seed + 0xBBC5D408423BCB64L, y)
                + zigzagRandomized(seed + 0xBBC5D408423BCB64L, z)
                + zigzagRandomized(seed + 0xBBC5D408423BCB64L, w)
                + zigzagRandomized(seed + 0xBBC5D408423BCB64L, v)) * 0x5851F42D4C957F2DL
                ^ (long)(v
                + zigzagRandomized(seed + 0x6AB7490A52CABE3DL, x)
                + zigzagRandomized(seed + 0x6AB7490A52CABE3DL, y)
                + zigzagRandomized(seed + 0x6AB7490A52CABE3DL, z)
                + zigzagRandomized(seed + 0x6AB7490A52CABE3DL, w)
                + zigzagRandomized(seed + 0x6AB7490A52CABE3DL, u)) * 0x2545F4914F6CDD1DL
        ) >>> 32)
                ^ seed);

//        return NumberTools.bounce(
//                (TangleRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2 + w*0x0.85157AF5p1) + (seed & 0x24924924))
//                        ^ TangleRNG.determine((long)(y*0x0.712BE5ABp4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2 + u*0x0.953976F9p1) + (seed & 0x49249249))
//                        ^ TangleRNG.determine((long)(z*0x0.85157AF5p4 + x*0x0.712BE5ABp3 + y*0x0.9E3779B9p2 + v*0x0.8329C6DFp1) + (seed & 0x92492492))
//                        ^ TangleRNG.determine((long)(w*0x0.953976F9p4 + u*0x0.85157AF5p3 + v*0x0.712BE5ABp2 + x*0x0.9E3779B9p1) + (seed & 0x24924924))
//                        ^ TangleRNG.determine((long)(u*0x0.8329C6DFp4 + v*0x0.953976F9p3 + w*0x0.85157AF5p2 + y*0x0.712BE5ABp1) + (seed & 0x49249249))
//                        ^ TangleRNG.determine((long)(v*0x0.9E3779B9p4 + w*0x0.8329C6DFp3 + u*0x0.953976F9p2 + z*0x0.85157AF5p1) + (seed & 0x92492492))));
    }
}
