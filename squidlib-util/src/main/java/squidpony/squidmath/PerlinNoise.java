package squidpony.squidmath;

/**
 * Delegates to {@link ClassicNoise} methods and always uses the same seed (123456789); that means this produces 
 * "Classic Perlin Noise" and not Simplex Noise (both were created by Ken Perlin). ClassicNoise provides more options
 * because it implements {@link squidpony.squidmath.Noise.Noise2D} and other Noise interfaces; you can use Noise2D and
 * its relatives with classes like {@link squidpony.squidmath.Noise.Ridged2D}. You could also use {@link FastNoise} with
 * {@link FastNoise#PERLIN_FRACTAL} as its noiseType, which includes features like adding together multiple octaves or
 * the above ridged noise. This is pretty much here as a bare-bones, basic noise generator.
 * <br>
 * This formerly produced Simplex noise, which was incredibly confusing; if you want that type of noise you should use
 * {@link SeededNoise}. To exactly reproduce the old PerlinNoise methods, you can call
 * {@code return SeededNoise.noise(x * 0.11709966304863834, y * 0.11709966304863834, 123456789)} for 2D,
 * {@code return SeededNoise.noise(x * 0.11709966304863834, y * 0.11709966304863834, z * 0.11709966304863834, 123456789)} for 3D, or
 * {@code return SeededNoise.noise(x * 0.11709966304863834, y * 0.11709966304863834, z * 0.11709966304863834, w * 0.11709966304863834, 123456789)} for 4D.
 * {@code 0.11709966304863834} is just the frequency this uses; it's {@code 1.0 / Math.E / Math.PI}, which is meant to
 * hit an integer multiple very rarely.
 */
public class PerlinNoise {

    /**
     * This class simply calls methods from {@link ClassicNoise} and multiplies the inputs by 0.11709966304863834, or
     * {@code 1.0 / Math.E / Math.PI}. Where a seed is used, it's always 123456789.
     */
    public static final double SCALE = 1.0 / Math.E / Math.PI;

    protected PerlinNoise()
    {

    }
    
    /**
     * Delegates to {@link ClassicNoise#getNoiseWithSeed(double, double, long)}; multiplies its inputs by {@link #SCALE}
     * and uses a seed of 123456789.
     *
     * @param xin x input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin) {
        return ClassicNoise.instance.getNoiseWithSeed(xin * 0.11709966304863834, yin * 0.11709966304863834, 123456789);
    }

    /**
     * Delegates to {@link ClassicNoise#getNoiseWithSeed(double, double, double, long)}; multiplies its inputs by
     * {@link #SCALE} and uses a seed of 123456789.
     *
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin, double zin) {
        return ClassicNoise.instance.getNoiseWithSeed(xin * 0.11709966304863834, yin * 0.11709966304863834, zin * 0.11709966304863834, 123456789);
    }

    /**
     * Delegates to {@link ClassicNoise#getNoiseWithSeed(double, double, double, double, long)}; multiplies its inputs
     * by {@link #SCALE} and uses a seed of 123456789.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param w W position (fourth dimension)
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double x, double y, double z, double w) {
        return ClassicNoise.instance.getNoiseWithSeed(x * 0.11709966304863834, y * 0.11709966304863834, z * 0.11709966304863834, w * 0.11709966304863834, 123456789);
    }
}
