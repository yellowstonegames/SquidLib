package squidpony.squidmath;

/**
 * A noise generator for 1D, 2D, 3D, 4D, or 6D noise that should look "glitchy", with waves of changing values moving
 * through triangular shapes. Intended for aesthetic purposes where something needs to look inorganic, unlike Perlin
 * or Simplex noise.
 */
public class GlitchNoise implements Noise.Noise1D, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {

    public static final GlitchNoise instance = new GlitchNoise();

    public GlitchNoise()
    {
    }
    @Override
    public double getNoise(final double x) {
        return NumberTools.bounce(
                LightRNG.determine((long)(x*0x0.9E3779B9p4 - x*0x0.8329C6DFp3 + x*0x0.953976F9p2))
                        ^ LightRNG.determine((long)(x*0x0.953976F9p4 + x*0x0.8329C6DFp3 - x*0x0.9E3779B9p2)));
    }
    @Override
    public double getNoiseWithSeed(final double x, final long seed) {
        return NumberTools.bounce(
                LightRNG.determine((long)(x*0x0.9E3779B9p4 - x*0x0.8329C6DFp3 + x*0x0.953976F9p2) + (seed & 0xA5A5A5A5))
                        ^ LightRNG.determine((long)(x*0x0.953976F9p4 + x*0x0.8329C6DFp3 - x*0x0.9E3779B9p2) + (seed & 0x5A5A5A5A)));
    }
    @Override
    public double getNoise(final double x, final double y) {

        return NumberTools.bounce(
                LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 - x*0x0.953976F9p2))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p4 + x*0x0.9E3779B9p3 - y*0x0.8329C6DFp2))
                        ^ LightRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p2 + x*0x4.953976F9p0))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p4 - x*0x2.8329C6DFp2 + y*0x4.85157AF5p0)));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        return NumberTools.bounce(
                LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 - x*0x0.953976F9p2) + (seed & 0xA5A5A5A5))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p4 + x*0x0.9E3779B9p3 - y*0x0.8329C6DFp2) + (seed & 0x5A5A5A5A))
                        ^ LightRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p2 - x*0x4.953976F9p0) + (seed & 0x3C3C3C3C))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p4 - x*0x2.8329C6DFp2 - y*0x4.85157AF5p0) + (seed & 0xC3C3C3C3)));
    }
    @Override
    public double getNoise(final double x, final double y, final double z) {
        return NumberTools.bounce(
                LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2))
                        ^ LightRNG.determine((long)(z*0x0.8329C6DFp4 + x*0x0.953976F9p3 + y*0x0.9E3779B9p2))
                        ^ LightRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p1 - z*0x3.953976F9p0))
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p4 - z*0x2.8329C6DFp1 - y*0x3.9E3779B9p0))
                        ^ LightRNG.determine((long)(z+0x0.85157AF5p4 - x*0x2.953976F9p1 - y*0x3.8329C6DFp0)));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        return NumberTools.bounce(
                LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2) + (seed & 0x24924924))
                        ^ LightRNG.determine((long)(y*0x0.953976F9p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(z*0x0.8329C6DFp4 + x*0x0.953976F9p3 + y*0x0.9E3779B9p2) + (seed & 0x92492492))
                        ^ LightRNG.determine((long)(x*0x0.85157AF5p4 - y*0x2.9E3779B9p1 - z*0x3.953976F9p0) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p4 - z*0x2.8329C6DFp1 - y*0x3.9E3779B9p0) + (seed & 0x92492492))
                        ^ LightRNG.determine((long)(z+0x0.85157AF5p4 - x*0x2.953976F9p1 - y*0x3.8329C6DFp0) + (seed & 0x24924924)));
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + w*0x0.953976F9p2))
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2))
                        ^ LightRNG.determine((long)(z*0x0.953976F9p4 + w*0x0.85157AF5p3 + y*0x0.9E3779B9p2))
                        ^ LightRNG.determine((long)(w*0x0.8329C6DFp4 + x*0x0.953976F9p3 + z*0x0.85157AF5p2))
                ));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + w*0x0.953976F9p2) + (seed & 0x81818181))
                        ^ LightRNG.determine((long)(y*0x0.85157AF5p4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2) + (seed & 0x18181818))
                        ^ LightRNG.determine((long)(z*0x0.953976F9p4 + w*0x0.85157AF5p3 + y*0x0.9E3779B9p2) + (seed & 0x42424242))
                        ^ LightRNG.determine((long)(w*0x0.8329C6DFp4 + x*0x0.953976F9p3 + z*0x0.85157AF5p2) + (seed & 0x24242424))
                        ));
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2 + w*0x0.85157AF5p1))
                        ^ LightRNG.determine((long)(y*0x0.712BE5ABp4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2 + u*0x0.953976F9p1))
                        ^ LightRNG.determine((long)(z*0x0.85157AF5p4 + x*0x0.712BE5ABp3 + y*0x0.9E3779B9p2 + v*0x0.8329C6DFp1))
                        ^ LightRNG.determine((long)(w*0x0.953976F9p4 + u*0x0.85157AF5p3 + v*0x0.712BE5ABp2 + x*0x0.9E3779B9p1))
                        ^ LightRNG.determine((long)(u*0x0.8329C6DFp4 + v*0x0.953976F9p3 + w*0x0.85157AF5p2 + y*0x0.712BE5ABp1))
                        ^ LightRNG.determine((long)(v*0x0.9E3779B9p4 + w*0x0.8329C6DFp3 + u*0x0.953976F9p2 + z*0x0.85157AF5p1))));
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final long seed) {
        return NumberTools.bounce(
                (LightRNG.determine((long)(x*0x0.9E3779B9p4 + y*0x0.8329C6DFp3 + z*0x0.953976F9p2 + w*0x0.85157AF5p1) + (seed & 0x24924924))
                        ^ LightRNG.determine((long)(y*0x0.712BE5ABp4 + z*0x0.9E3779B9p3 + x*0x0.8329C6DFp2 + u*0x0.953976F9p1) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(z*0x0.85157AF5p4 + x*0x0.712BE5ABp3 + y*0x0.9E3779B9p2 + v*0x0.8329C6DFp1) + (seed & 0x92492492))
                        ^ LightRNG.determine((long)(w*0x0.953976F9p4 + u*0x0.85157AF5p3 + v*0x0.712BE5ABp2 + x*0x0.9E3779B9p1) + (seed & 0x24924924))
                        ^ LightRNG.determine((long)(u*0x0.8329C6DFp4 + v*0x0.953976F9p3 + w*0x0.85157AF5p2 + y*0x0.712BE5ABp1) + (seed & 0x49249249))
                        ^ LightRNG.determine((long)(v*0x0.9E3779B9p4 + w*0x0.8329C6DFp3 + u*0x0.953976F9p2 + z*0x0.85157AF5p1) + (seed & 0x92492492))));
    }
}
