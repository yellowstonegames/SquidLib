package squidpony.squidmath;

/**
 * This is Ken Perlin's third revision of his noise function. It is sometimes
 * referred to as "Simplex Noise". Results are bound by (-1, 1) inclusive.
 *
 *
 * It is significantly faster than his earlier versions. This particular version
 * was originally from Stefan Gustavson. This is much preferred to the earlier
 * versions of Perlin Noise due to the reasons noted in the articles:
 * <ul>
 * <li>http://www.itn.liu.se/~stegu/simplexnoise/simplexnoise.pdf</li>
 * <li>http://webstaff.itn.liu.se/~stegu/TNM022-2005/perlinnoiselinks/ch02.pdf</li>
 * </ul>
 * But, Gustavson's paper is not without its own issues, particularly for 2D noise.
 * More detail is noted here,
 * http://stackoverflow.com/questions/18885440/why-does-simplex-noise-seem-to-have-more-artifacts-than-classic-perlin-noise#21568753
 * and some changes have been made to 2D noise generation to reduce angular artifacts.
 * Specifically for the 2D gradient table, code based on Gustavson's paper used 12
 * points, with some duplicates, and not all on the unit circle. In this version,
 * points are used on the unit circle starting at (1,0) and moving along the circle
 * in increments of 1.61803398875 radians, that is, the golden ratio phi, getting the
 * sin and cosine of 15 points after the starting point and storing them as constants.
 * This definitely doesn't have a noticeable 45 degree angle artifact, though it does
 * have, to a lesser extent, some other minor artifacts.
 * <br>
 * You can also consider {@link WhirlingNoise} as an alternative, which can be faster
 * and also reduces the likelihood of angular artifacts. WhirlingNoise does not scale its
 * input (it doesn't need to), so it won't produce the same results as PerlinNoise for the
 * same inputs, but it will produce similar shape, density, and aesthetic quality of noise.
 * @see WhirlingNoise A subclass that has a faster implementation and some different qualities.
 */
public class PerlinNoise {

    public static final double SCALE = 1.0 / Math.E / Math.PI;
    //phi = 1.61803398875, unit1_4 = 0.70710678118, unit1_8 = 0.38268343236, unit3_8 = 0.92387953251;

    protected PerlinNoise()
    {

    }
    
    /**
     * 2D simplex noise.
     * This doesn't use its parameters verbatim; xin and yin are both effectively divided by
     * ({@link Math#E} * {@link Math#PI}), because without a step like that, any integer parameters would return 0 and
     * only doubles with a decimal component would produce actual noise. This step allows integers to be passed in a
     * arguments, and changes the cycle at which 0 is repeated to multiples of (E*PI).
     *
     * @param xin x input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin) {
        return SeededNoise.noise(xin * SCALE, yin * SCALE, 123456789);
    }

    /**
     * 3D simplex noise.
     *
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin, double zin) {
        return SeededNoise.noise(xin * SCALE, yin * SCALE, zin * SCALE, 123456789);
    }

    /**
     * 4D simplex noise.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param w Fourth-dimensional position. It is I, the Fourth-Dimensional Ziltoid the Omniscient!
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double x, double y, double z, double w) {
        return SeededNoise.noise(x * SCALE, y * SCALE, z * SCALE, w * SCALE, 123456789);
    }
}
