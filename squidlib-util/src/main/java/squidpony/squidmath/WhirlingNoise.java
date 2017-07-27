package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.LightRNG.determine;
import static squidpony.squidmath.LightRNG.determineBounded;

/**
 * Another experimental noise class. Extends PerlinNoise and should have similar quality, but can be faster and has less
 * periodic results. This is still considered experimental because the exact output may change in future versions, along
 * with the scale (potentially) and the parameters it takes. In general, {@link #noise(double, double)} and
 * {@link #noise(double, double, double)} should have similar appearance to {@link PerlinNoise#noise(double, double)}
 * and {@link PerlinNoise#noise(double, double, double)}, but are not forced to a zoomed-in scale like PerlinNoise makes
 * its results, are less likely to repeat sections of noise, and are also somewhat faster (a 20% speedup can be expected
 * over PerlinNoise using those two methods). Sound good?
 * <br>
 * Created by Tommy Ettinger on 12/14/2016.
 */
@Beta
public class WhirlingNoise extends PerlinNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D {

    public static final WhirlingNoise instance = new WhirlingNoise();

    private static int fastFloor(double t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }
    private static int fastFloor(float t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }
    protected static final float
            root2 = 1.4142135f,
            root3 = 1.7320508f, root5 = 2.236068f,
            F2f = 0.5f * (root3 - 1f),
            G2f = (3f - root3) * 0.16666667f,
            F3f = 0.33333334f,
            G3f = 0.16666667f,
            F4f = (root5 - 1f) * 0.25f,
            G4f = (5f - root5) * 0.05f,
            unit1_4f =  0.70710678118f, unit1_8f = 0.38268343236f, unit3_8f = 0.92387953251f;

    protected static final float[][] grad2f = {
            {1f, 0f}, {-1f, 0f}, {0f, 1f}, {0f, -1f},
            {unit3_8f, unit1_8f}, {unit3_8f, -unit1_8f}, {-unit3_8f, unit1_8f}, {-unit3_8f, -unit1_8f},
            {unit1_4f, unit1_4f}, {unit1_4f, -unit1_4f}, {-unit1_4f, unit1_4f}, {-unit1_4f, -unit1_4f},
            {unit1_8f, unit3_8f}, {unit1_8f, -unit3_8f}, {-unit1_8f, unit3_8f}, {-unit1_8f, -unit3_8f}};
    protected static final float[][] phiGrad2f = {
            {1, 0}, {(float)Math.cos(phi), (float)Math.sin(phi)},
            {(float)Math.cos(phi*2),  (float)Math.sin(phi*2)},  {(float)Math.cos(phi*3),  (float)Math.sin(phi*3)},
            {(float)Math.cos(phi*4),  (float)Math.sin(phi*4)},  {(float)Math.cos(phi*5),  (float)Math.sin(phi*5)},
            {(float)Math.cos(phi*6),  (float)Math.sin(phi*6)},  {(float)Math.cos(phi*7),  (float)Math.sin(phi*7)},
            {(float)Math.cos(phi*8),  (float)Math.sin(phi*8)},  {(float)Math.cos(phi*9),  (float)Math.sin(phi*9)},
            {(float)Math.cos(phi*10), (float)Math.sin(phi*10)}, {(float)Math.cos(phi*11), (float)Math.sin(phi*11)},
            {(float)Math.cos(phi*13), (float)Math.sin(phi*12)}, {(float)Math.cos(phi*13), (float)Math.sin(phi*13)},
            {(float)Math.cos(phi*14), (float)Math.sin(phi*14)}, {(float)Math.cos(phi*15), (float)Math.sin(phi*15)},
    };
    /**
     * The 92 3D vertices of a pentagonal hexecontahedron. These specific values were taken from Vladimir Bulatov's
     * stellation applet, which has available source but is unlicensed, and is
     * <a href="http://www.bulatov.org/polyhedra/stellation_applet/index.html">available here</a>, but the vertices are
     * mathematical constants so copyright isn't an issue.
     */
    protected static final float[][] grad3f = {
            { 0.098415901494919f,  0.944618547212300f,  0.086889031803113f },
            {-0.098415901494923f,  0.944618547212300f, -0.086889031803107f },
            {-0.340300460626875f,  0.890918172308408f,  0.000000000000003f },
            {-0.371602384173985f,  0.840270596095933f,  0.255727563365808f },
            {-0.000000000000002f,  0.850650808352026f,  0.525731112119124f },
            { 0.432426756331305f,  0.741854694601012f,  0.414967837018052f },
            { 0.512191790880984f,  0.786570221192041f,  0.168838531562698f },
            { 0.340300460626872f,  0.890918172308408f,  0.000000000000003f },
            { 0.371602384173983f,  0.840270596095934f, -0.255727563365803f },
            {-0.000000000000002f,  0.850650808352029f, -0.525731112119120f },
            {-0.432426756331308f,  0.741854694601014f, -0.414967837018047f },
            {-0.512191790880988f,  0.786570221192041f, -0.168838531562693f },
            {-0.850650808352023f,  0.525731112119131f,  0.000000000000001f },
            {-0.573016163038312f,  0.688154319697119f,  0.328078805214941f },
            {-0.550617711681530f,  0.550617711681534f,  0.550617711681529f },
            {-0.328078805214941f,  0.573016163038316f,  0.688154319697115f },
            { 0.168838531562694f,  0.512191790880993f,  0.786570221192036f },
            { 0.414967837018049f,  0.432426756331314f,  0.741854694601007f },
            { 0.550617711681525f,  0.550617711681535f,  0.550617711681530f },
            { 0.741854694601004f,  0.414967837018058f,  0.432426756331309f },
            { 0.850650808352019f,  0.525731112119131f,  0.000000000000003f },
            { 0.573016163038308f,  0.688154319697121f, -0.328078805214937f },
            { 0.550617711681528f,  0.550617711681538f, -0.550617711681525f },
            { 0.328078805214940f,  0.573016163038319f, -0.688154319697111f },
            {-0.168838531562697f,  0.512191790880997f, -0.786570221192035f },
            {-0.414967837018053f,  0.432426756331317f, -0.741854694601006f },
            {-0.550617711681530f,  0.550617711681536f, -0.550617711681527f },
            {-0.741854694601009f,  0.414967837018059f, -0.432426756331305f },
            {-0.840270596095931f,  0.255727563365812f,  0.371602384173985f },
            {-0.688154319697117f,  0.328078805214945f,  0.573016163038312f },
            {-0.525731112119129f,  0.000000000000002f,  0.850650808352025f },
            {-0.255727563365808f,  0.371602384173987f,  0.840270596095931f },
            {-0.000000000000003f,  0.340300460626876f,  0.890918172308407f },
            { 0.086889031803109f,  0.098415901494923f,  0.944618547212299f },
            { 0.525731112119125f,  0.000000000000004f,  0.850650808352025f },
            { 0.786570221192034f,  0.168838531562704f,  0.512191790880989f },
            { 0.890918172308403f,  0.000000000000009f,  0.340300460626876f },
            { 0.944618547212296f,  0.086889031803121f,  0.098415901494923f },
            { 0.840270596095927f,  0.255727563365817f, -0.371602384173981f },
            { 0.688154319697114f,  0.328078805214951f, -0.573016163038307f },
            { 0.525731112119129f,  0.000000000000012f, -0.850650808352020f },
            { 0.255727563365808f,  0.371602384173994f, -0.840270596095926f },
            {-0.000000000000000f,  0.340300460626881f, -0.890918172308404f },
            {-0.086889031803109f,  0.098415901494928f, -0.944618547212299f },
            {-0.525731112119130f,  0.000000000000001f, -0.850650808352026f },
            {-0.786570221192041f,  0.168838531562702f, -0.512191790880986f },
            {-0.890918172308409f,  0.000000000000008f, -0.340300460626871f },
            {-0.944618547212299f,  0.086889031803118f, -0.098415901494920f },
            {-0.944618547212300f, -0.086889031803100f,  0.098415901494925f },
            {-0.890918172308407f,  0.000000000000012f,  0.340300460626874f },
            {-0.786570221192040f, -0.168838531562684f,  0.512191790880988f },
            {-0.086889031803113f, -0.098415901494924f,  0.944618547212299f },
            {-0.000000000000003f, -0.340300460626881f,  0.890918172308406f },
            { 0.255727563365812f, -0.371602384173993f,  0.840270596095928f },
            { 0.688154319697119f, -0.328078805214943f,  0.573016163038308f },
            { 0.840270596095930f, -0.255727563365801f,  0.371602384173987f },
            { 0.850650808352029f, -0.525731112119114f, -0.000000000000006f },
            { 0.944618547212297f, -0.086889031803100f, -0.098415901494920f },
            { 0.890918172308405f,  0.000000000000007f, -0.340300460626872f },
            { 0.786570221192044f, -0.168838531562691f, -0.512191790880981f },
            { 0.086889031803113f, -0.098415901494914f, -0.944618547212299f },
            { 0.000000000000009f, -0.340300460626870f, -0.890918172308406f },
            {-0.255727563365796f, -0.371602384173988f, -0.840270596095931f },
            {-0.688154319697120f, -0.328078805214943f, -0.573016163038309f },
            {-0.840270596095935f, -0.255727563365805f, -0.371602384173982f },
            {-0.850650808352032f, -0.525731112119107f,  0.000000000000012f },
            {-0.741854694601016f, -0.414967837018035f,  0.432426756331311f },
            {-0.550617711681541f, -0.550617711681511f,  0.550617711681534f },
            {-0.414967837018066f, -0.432426756331290f,  0.741854694601011f },
            {-0.168838531562693f, -0.512191790880986f,  0.786570221192041f },
            {-0.000000000000022f, -0.850650808352035f,  0.525731112119117f },
            { 0.328078805214949f, -0.573016163038333f,  0.688154319697101f },
            { 0.550617711681525f, -0.550617711681556f,  0.550617711681518f },
            { 0.573016163038319f, -0.688154319697138f,  0.328078805214909f },
            { 0.741854694601021f, -0.414967837018041f, -0.432426756331300f },
            { 0.550617711681540f, -0.550617711681530f, -0.550617711681523f },
            { 0.414967837018065f, -0.432426756331319f, -0.741854694600999f },
            { 0.168838531562735f, -0.512191790881005f, -0.786570221192023f },
            { 0.000000000000037f, -0.850650808352064f, -0.525731112119095f },
            {-0.328078805214933f, -0.573016163038341f, -0.688154319697099f },
            {-0.550617711681517f, -0.550617711681560f, -0.550617711681519f },
            {-0.573016163038295f, -0.688154319697146f, -0.328078805214925f },
            {-0.512191790880984f, -0.786570221192040f,  0.168838531562715f },
            {-0.432426756331320f, -0.741854694600996f,  0.414967837018059f },
            { 0.371602384173965f, -0.840270596095972f,  0.255727563365750f },
            { 0.340300460626838f, -0.890918172308444f, -0.000000000000082f },
            { 0.512191790880927f, -0.786570221192080f, -0.168838531562769f },
            { 0.432426756331317f, -0.741854694601025f, -0.414967837018036f },
            {-0.371602384173950f, -0.840270596095964f, -0.255727563365787f },
            {-0.340300460626839f, -0.890918172308430f,  0.000000000000014f },
            {-0.098415901494953f, -0.944618547212300f,  0.086889031803118f },
            { 0.098415901494961f, -0.944618547212302f, -0.086889031803149f }
    };
    protected static final float[][] grad4f = new float[368][4];
    static {

        for (int i = 0, ii = 0; i < 92; i++, ii += 4) {
            float x = grad3f[i][0], y = grad3f[i][1], z = grad3f[i][2];
            final float len = 1f / (float)Math.sqrt(x * x + y * y + z * z), len3 = len * root2, len4 = len * root3;
            //final float len = 2f / Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))), len3 = len * 1.5f;
            grad3f[i][0] *= len3;
            grad3f[i][1] *= len3;
            grad3f[i][2] *= len3;
            grad4f[ii+3][3] = grad4f[ii+2][2] = grad4f[ii+1][1] = grad4f[ii][0] = x * len4;
            grad4f[ii+3][2] = grad4f[ii+2][0] = grad4f[ii+1][3] = grad4f[ii][1] = y * len4;
            grad4f[ii+3][1] = grad4f[ii+2][3] = grad4f[ii+1][0] = grad4f[ii][2] = z * len4;
        }
    }
//    protected static final float[][] phiGrad3f = new float[96][3];
//
//    static {
//        final float root2 = 1.2599211f;
//        int i = 0;
//        for (; i < 16; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][0] * root2;
//            phiGrad3f[i][1] = phiGrad2f[i & 15][1] * root2;
//        }
//        for (; i < 32; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][1] * root2;
//            phiGrad3f[i][1] = phiGrad2f[i & 15][0] * root2;
//        }
//        for (; i < 48; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][0] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][1] * root2;
//        }
//        for (; i < 64; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][1] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][0] * root2;
//        }
//        for (; i < 80; i++) {
//            phiGrad3f[i][1] = phiGrad2f[i & 15][0] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][1] * root2;
//        }
//        for (; i < 96; i++) {
//            phiGrad3f[i][1] = phiGrad2f[i & 15][1] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][0] * root2;
//        }
//    }

    protected static float dotf(final float g[], final float x, final float y) {
        return g[0] * x + g[1] * y;
    }

    protected static float dotf(final int g[], final float x, final float y, final float z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }
    protected static float dotf(final float g[], final float x, final float y, final float z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    protected static double dot(final float g[], final double x, final double y, final double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }
    protected static double dot(final float g[], final double x, final double y, final double z, final double w) {
        return g[0] * x + g[1] * y + g[2] * z + g[3] * w;
    }

    /*
    // makes the noise appear muddled and murky; probably not ideal
    protected static float dotterize(final float x, final float y, final int i, final int j)
    {
        return ZapRNG.randomSignedFloat(i * 0x9E3779B97F4A7C15L, j * 0xC6BC279692B5C483L) * x + ZapRNG.randomSignedFloat(j * 0x8E3779B97F4A7C15L, i * 0x632AE59B69B3C209L) * y;
    }
    */

    /*
    public static double interpolate(double t, double low, double high)
    {
        //debug
        //return 0;
        //linear
        //return t;
        //hermite
        //return t * t * (3 - 2 * t);
        //quintic
        //return t * t * t * (t * (t * 6 - 15) + 10);

        //t = (t + low + 0.5 - high) * 0.5;
        //t = (t < 0.5) ? t * low : 1.0 - ((1.0 - t) * high);
        //t = Math.pow(t, 1.0 + high - low);

        //return (t + 0.5 + high - low) * 0.5;
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    */

    /**
     * 2D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double)} and this method. Roughly
     * 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks because
     * it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather good distribution
     * and is fast) instead of a number chosen by hash from a single 256-element array.
     *
     * @param x X input; works well if between 0.0 and 1.0, but anything is accepted
     * @param y Y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoise(final double x, final double y) {
        return noise(x, y);
    }

    /**
     * Identical to {@link #getNoise(double, double)}; ignores seed.
     * @param x X input; works well if between 0.0 and 1.0, but anything is accepted
     * @param y Y input; works well if between 0.0 and 1.0, but anything is accepted
     * @param seed ignored entirely.
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoiseWithSeed(final double x, final double y, final int seed) {
        return noise(x, y, seed);
    }
    /**
     * 3D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double)} and this method.
     * Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks
     * because it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather good
     * distribution and is fast) instead of a number chosen by hash from a single 256-element array.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoise(final double x, final double y, final double z) {
        return noise(x, y, z);
    }
    /**
     * Identical to {@link #getNoise(double, double, double)}; ignores seed.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param seed ignored entirely.
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
        return noise(x, y, z, seed);
    }

    /**
     * 4D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double)} and this method.
     * Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks
     * because it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather good
     * distribution and is fast) instead of a number chosen by hash from a single 256-element array.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimension)
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoise(final double x, final double y, final double z, final double w) {
        return noise(x, y, z, w);
    }
    /**
     * Identical to {@link #getNoise(double, double, double, double)}; ignores seed.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimension)
     * @param seed ignored entirely.
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
        return noise(x, y, z, w, seed);
    }


    /**
     * 2D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double)} and this method. Roughly
     * 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks because
     * it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather good distribution
     * and is fast) instead of a number chosen by hash from a single 256-element array.
     *
     * @param xin X input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin Y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin){
        return noise(xin, yin, 123456789);
    }

    /**
     * 2D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double)} and this method. Roughly
     * 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks because
     * it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather good distribution
     * and is fast) instead of a number chosen by hash from a single 256-element array.
     *
     * @param xin X input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin Y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin, final int seed) {
        //xin *= epi;
        //yin *= epi;
        double noise0, noise1, noise2; // from the three corners
        // Skew the input space to figure out which simplex cell we're in
        double skew = (xin + yin) * F2; // Hairy factor for 2D
        int i = fastFloor(xin + skew);
        int j = fastFloor(yin + skew);
        double t = (i + j) * G2;
        double X0 = i - t; // Unskew the cell origin back to (x,y) space
        double Y0 = j - t;
        double x0 = xin - X0; // The x,y distances from the cell origin
        double y0 = yin - Y0;
        // For the 2D case, the simplex shape is an equilateral triangle.
        // determine which simplex we are in.
        int i1, j1; // Offsets for second (middle) corner of simplex in (i,j)
        // coords
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } // lower triangle, XY order: (0,0)->(1,0)->(1,1)
        else {
            i1 = 0;
            j1 = 1;
        } // upper triangle, YX order: (0,0)->(0,1)->(1,1)
        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y),
        // where
        // c = (3-sqrt(3))/6
        double x1 = x0 - i1 + G2; // Offsets for middle corner in (x,y)
        // unskewed coords
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2; // Offsets for last corner in (x,y)
        // unskewed coords
        double y2 = y0 - 1.0 + 2.0 * G2;
        // Work out the hashed gradient indices of the three simplex corners
        /*
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii + perm[jj]] & 15;
        int gi1 = perm[ii + i1 + perm[jj + j1]] & 15;
        int gi2 = perm[ii + 1 + perm[jj + 1]] & 15;
        */
        /*
        int hash = (int) rawNoise(i + (j * 0x9E3779B9),
                i + i1 + ((j + j1) * 0x9E3779B9),
                i + 1 + ((j + 1) * 0x9E3779B9),
                seed);
        int gi0 = hash & 15;
        int gi1 = (hash >>>= 4) & 15;
        int gi2 = (hash >>> 4) & 15;
        */
        int gi0 = determineBounded(seed + i + determine(j), 16);
        int gi1 = determineBounded(seed + i + i1 + determine(j + j1), 16);
        int gi2 = determineBounded(seed + i + 1 + determine(j + 1), 16);

        // Calculate the contribution from the three corners
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0.0;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dot(phiGrad2[gi0], x0, y0);
            // for 2D gradient
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0.0;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dot(phiGrad2[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0.0;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dot(phiGrad2[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70.0 * (noise0 + noise1 + noise2);
    }


    /**
     * 2D simplex noise returning a float; extremely similar to {@link #noise(double, double)}, but this may be slightly
     * faster or slightly slower. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result
     * will be different when passing the same arguments to {@link PerlinNoise#noise(double, double)} and this method.
     *
     * @param x x input; works well if between 0.0 and 1.0, but anything is accepted
     * @param y y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static float noiseAlt(double x, double y) {
        //xin *= epi;
        //yin *= epi;
        float noise0, noise1, noise2; // from the three corners
        float xin = (float)x, yin = (float)y;
        // Skew the input space to figure out which simplex cell we're in
        float skew = (xin + yin) * F2f; // Hairy factor for 2D
        int i = fastFloor(xin + skew);
        int j = fastFloor(yin + skew);
        float t = (i + j) * G2f;
        float X0 = i - t; // Unskew the cell origin back to (x,y) space
        float Y0 = j - t;
        float x0 = xin - X0; // The x,y distances from the cell origin
        float y0 = yin - Y0;
        // For the 2D case, the simplex shape is an equilateral triangle.
        // determine which simplex we are in.
        int i1, j1; // Offsets for second (middle) corner of simplex in (i,j)
        // coords
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } // lower triangle, XY order: (0,0)->(1,0)->(1,1)
        else {
            i1 = 0;
            j1 = 1;
        } // upper triangle, YX order: (0,0)->(0,1)->(1,1)
        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y),
        // where
        // c = (3-sqrt(3))/6
        float x1 = x0 - i1 + G2f; // Offsets for middle corner in (x,y)
        // unskewed coords
        float y1 = y0 - j1 + G2f;
        float x2 = x0 - 1f + 2f * G2f; // Offsets for last corner in (x,y)
        // unskewed coords
        float y2 = y0 - 1f + 2f * G2f;
        // Work out the hashed gradient indices of the three simplex corners
        /*
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii + perm[jj]] & 15;
        int gi1 = perm[ii + i1 + perm[jj + j1]] & 15;
        int gi2 = perm[ii + 1 + perm[jj + 1]] & 15;
        */
        /*
        int hash = (int) rawNoise(i + (j * 0x9E3779B9),
                i + i1 + ((j + j1) * 0x9E3779B9),
                i + 1 + ((j + 1) * 0x9E3779B9),
                seed);
        int gi0 = hash & 15;
        int gi1 = (hash >>>= 4) & 15;
        int gi2 = (hash >>> 4) & 15;
        */
        int gi0 = determineBounded(i + determine(j), 16);
        int gi1 = determineBounded(i + i1 + determine(j + j1), 16);
        int gi2 = determineBounded(i + 1 + determine(j + 1), 16);

        // Calculate the contribution from the three corners
        float t0 = 0.5f - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0f;
        } else {
            t0 *= t0;
            //noise0 = t0 * t0 * dotterize(x0, y0, i, j);
            noise0 = t0 * t0 * dotf(phiGrad2f[gi0], x0, y0);
        }
        float t1 = 0.5f - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0f;
        } else {
            t1 *= t1;
            //noise1 = t1 * t1 * dotterize(x1, y1, i + i1, j + j1);
            noise1 = t1 * t1 * dotf(phiGrad2f[gi1], x1, y1);
        }
        float t2 = 0.5f - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0f;
        } else {
            t2 *= t2;
            //noise2 = t2 * t2 * dotterize(x2, y2, i+1, j+1);
            noise2 = t2 * t2 * dotf(phiGrad2f[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70f * (noise0 + noise1 + noise2);
    }

    /**
     * 3D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double)} and this method.
     * Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks
     * because it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather
     * good distribution and is fast) instead of a number chosen by hash from a single 256-element array.
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin, final double zin){
        return noise(xin, yin, zin, 123456789);
    }

    /**
     * 3D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double)} and this method.
     * Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in chunks
     * because it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather
     * good distribution and is fast) instead of a number chosen by hash from a single 256-element array.
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin, final double zin, final int seed){
        //xin *= epi;
        //yin *= epi;
        //zin *= epi;
        double n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to figure out which simplex cell we're in
        double s = (xin + yin + zin) * F3; // Very nice and simple skew
        // factor for 3D
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        int k = fastFloor(zin + s);
        double t = (i + j + k) * G3;
        double X0 = i - t; // Unskew the cell origin back to (x,y,z) space
        double Y0 = j - t;
        double Z0 = k - t;
        double x0 = xin - X0; // The x,y,z distances from the cell origin
        double y0 = yin - Y0;
        double z0 = zin - Z0;
        // For the 3D case, the simplex shape is a slightly irregular
        // tetrahedron.
        // determine which simplex we are in.
        int i1, j1, k1; // Offsets for second corner of simplex in (i,j,k)
        // coords
        int i2, j2, k2; // Offsets for third corner of simplex in (i,j,k)
        // coords
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // X Y Z order
            else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // X Z Y order
            else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // Z X Y order
        } else { // x0<y0
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Z Y X order
            else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Y Z X order
            else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // Y X Z order
        }
        // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in
        // (x,y,z),
        // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in
        // (x,y,z), and
        // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in
        // (x,y,z), where
        // c = 1/6.
        double x1 = x0 - i1 + G3; // Offsets for second corner in (x,y,z)
        // coords
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + F3; // Offsets for third corner in
        // (x,y,z) coords
        double y2 = y0 - j2 + F3;
        double z2 = z0 - k2 + F3;
        double x3 = x0 - 0.5; // Offsets for last corner in
        // (x,y,z) coords
        double y3 = y0 - 0.5;
        double z3 = z0 - 0.5;
        // Work out the hashed gradient indices of the four simplex corners

        /*
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;

        int gi0 = perm[ii + perm[jj + perm[kk]]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12;
        int gi2 = perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12;
        int gi3 = perm[ii + 1 + perm[jj + 1 + perm[kk + 1]]] % 12;
        */
        int gi0 = determineBounded(seed + i + determine(j + determine(k)), 92);
        int gi1 = determineBounded(seed + i + i1 + determine(j + j1 + determine(k + k1)), 92);
        int gi2 = determineBounded(seed + i + i2 + determine(j + j2 + determine(k + k2)), 92);
        int gi3 = determineBounded(seed + i + 1 + determine(j + 1 + determine(k + 1)), 92);

        /*
        int hash = (int) rawNoise(i + ((j + k * 0x632BE5AB) * 0x9E3779B9),
                i + i1 + ((j + j1 + (k + k1) * 0x632BE5AB) * 0x9E3779B9),
                i + i2 + ((j + j2 + (k + k2) * 0x632BE5AB) * 0x9E3779B9),
                i + 1 + ((j + 1 + ((k + 1) * 0x632BE5AB)) * 0x9E3779B9),
                seed);
        int gi0 = (hash >>>= 4) % 12;
        int gi1 = (hash >>>= 4) % 12;
        int gi2 = (hash >>>= 4) % 12;
        int gi3 = (hash >>> 4) % 12;
        */

        //int hash = (int) rawNoise(i, j, k, seed);
        //int gi0 = (hash >>>= 4) % 12, gi1 = (hash >>>= 4) % 12, gi2 = (hash >>>= 4) % 12, gi3 = (hash >>>= 4) % 12;
        // Calculate the contribution from the four corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3f[gi0], x0, y0, z0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3f[gi1], x1, y1, z1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3f[gi2], x2, y2, z2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad3f[gi3], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        return 32.0 * (n0 + n1 + n2 + n3);
    }

    /**
     * 3D simplex noise returning a float; extremely similar to {@link #noise(double, double, double)}, but this may
     * be slightly faster or slightly slower. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of
     * the result will be different when passing the same arguments to {@link PerlinNoise#noise(double, double, double)}
     * and this method.
     *
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static float noiseAlt(double x, double y, double z) {
        //xin *= epi;
        //yin *= epi;
        //zin *= epi;
        float xin = (float)x, yin = (float)y, zin = (float)z;
        float n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to figure out which simplex cell we're in
        float s = (xin + yin + zin) * F3f; // Very nice and simple skew
        // factor for 3D
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        int k = fastFloor(zin + s);
        float t = (i + j + k) * G3f;
        float X0 = i - t; // Unskew the cell origin back to (x,y,z) space
        float Y0 = j - t;
        float Z0 = k - t;
        float x0 = xin - X0; // The x,y,z distances from the cell origin
        float y0 = yin - Y0;
        float z0 = zin - Z0;
        // For the 3D case, the simplex shape is a slightly irregular
        // tetrahedron.
        // determine which simplex we are in.
        int i1, j1, k1; // Offsets for second corner of simplex in (i,j,k)
        // coords
        int i2, j2, k2; // Offsets for third corner of simplex in (i,j,k)
        // coords
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // X Y Z order
            else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // X Z Y order
            else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // Z X Y order
        } else { // x0<y0
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Z Y X order
            else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Y Z X order
            else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // Y X Z order
        }
        // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in
        // (x,y,z),
        // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in
        // (x,y,z), and
        // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in
        // (x,y,z), where
        // c = 1/6.
        float x1 = x0 - i1 + G3f; // Offsets for second corner in (x,y,z)
        // coords
        float y1 = y0 - j1 + G3f;
        float z1 = z0 - k1 + G3f;
        float x2 = x0 - i2 + F3f; // Offsets for third corner in
        // (x,y,z) coords
        float y2 = y0 - j2 + F3f;
        float z2 = z0 - k2 + F3f;
        float x3 = x0 - 0.5f; // Offsets for last corner in
        // (x,y,z) coords
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;
        // Work out the hashed gradient indices of the four simplex corners

        /*
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;

        int gi0 = perm[ii + perm[jj + perm[kk]]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12;
        int gi2 = perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12;
        int gi3 = perm[ii + 1 + perm[jj + 1 + perm[kk + 1]]] % 12;
        */

        int gi0 = determineBounded(i + determine(j + determine(k)), 92);
        int gi1 = determineBounded(i + i1 + determine(j + j1 + determine(k + k1)), 92);
        int gi2 = determineBounded(i + i2 + determine(j + j2 + determine(k + k2)), 92);
        int gi3 = determineBounded(i + 1 + determine(j + 1 + determine(k + 1)), 92);

        /*
        int hash = (int) rawNoise(i + ((j + k * 0x632BE5AB) * 0x9E3779B9),
                i + i1 + ((j + j1 + (k + k1) * 0x632BE5AB) * 0x9E3779B9),
                i + i2 + ((j + j2 + (k + k2) * 0x632BE5AB) * 0x9E3779B9),
                i + 1 + ((j + 1 + ((k + 1) * 0x632BE5AB)) * 0x9E3779B9),
                seed);
        int gi0 = (hash >>>= 4) % 12;
        int gi1 = (hash >>>= 4) % 12;
        int gi2 = (hash >>>= 4) % 12;
        int gi3 = (hash >>> 4) % 12;
        */

        //int hash = (int) rawNoise(i, j, k, seed);
        //int gi0 = (hash >>>= 4) % 12, gi1 = (hash >>>= 4) % 12, gi2 = (hash >>>= 4) % 12, gi3 = (hash >>>= 4) % 12;
        // Calculate the contribution from the four corners
        float t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0f;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dotf(grad3f[gi0], x0, y0, z0);
        }
        float t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0f;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dotf(grad3f[gi1], x1, y1, z1);
        }
        float t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0f;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dotf(grad3f[gi2], x2, y2, z2);
        }
        float t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0f;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dotf(grad3f[gi3], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        return 32f * (n0 + n1 + n2 + n3);
    }

    /**
     * 4D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double, double)} and this
     * method. Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in
     * chunks because it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather
     * good distribution and is fast) instead of a number chosen by hash from a single 256-element array.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimensional)
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final double z, final double w) {
        return noise(x, y, z, w, 123456789);
    }

    /**
     * 4D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double, double)} and this
     * method. Roughly 20-25% faster than the equivalent method in PerlinNoise, plus it has less chance of repetition in
     * chunks because it uses a pseudo-random function (curiously, {@link LightRNG#determine(long)}, which has rather
     * good distribution and is fast) instead of a number chosen by hash from a single 256-element array.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimensional)
     * @param seed any int; will be used to completely alter the noise
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final double z, final double w, final int seed)
    {
        // The skewing and unskewing factors are hairy again for the 4D case

        // Skew the (x,y,z,w) space to figure out which cell of 24 simplices
        // we're in
        double s = (x + y + z + w) * F4; // Factor for 4D skewing
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        int k = fastFloor(z + s);
        int l = fastFloor(w + s);
        double t = (i + j + k + l) * G4; // Factor for 4D unskewing
        double X0 = i - t; // Unskew the cell origin back to (x,y,z,w) space
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double x0 = x - X0; // The x,y,z,w distances from the cell origin
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;
        // For the 4D case, the simplex is a 4D shape I won't even try to
        // describe.
        // To find out which of the 24 possible simplices we're in, we need
        // to figure out the magnitude ordering of x0, y0, z0 and w0.
        // The method below is a good way of finding the ordering of x,y,z,w
        // and
        // then find the correct traversal order for the simplex we’re in.
        // First, six pair-wise comparisons are performed between each
        // possible pair
        // of the four coordinates, and the results are used to add up binary
        // bits
        // for an integer index.
        int c = (x0 > y0 ? 32 : 0) | (x0 > z0 ? 16 : 0) | (y0 > z0 ? 8 : 0) |
                (x0 > w0 ? 4 : 0) | (y0 > w0 ? 2 : 0) | (z0 > w0 ? 1 : 0);

        // simplex[c] is a 4-vector with the numbers 0, 1, 2 and 3 in some
        // order.
        // Many values of c will never occur, since e.g. x>y>z>w makes x<z,
        // y<w and x<w
        // impossible. Only the 24 indices which have non-zero entries make
        // any sense.
        // We use a thresholding to set the coordinates in turn from the
        // largest magnitude.
        // The number 3 in the "simplex" array is at the position of the
        // largest coordinate.

        // The integer offsets for the second simplex corner
        int i1 = simplex[c][0] >= 3 ? 1 : 0;
        int j1 = simplex[c][1] >= 3 ? 1 : 0;
        int k1 = simplex[c][2] >= 3 ? 1 : 0;
        int l1 = simplex[c][3] >= 3 ? 1 : 0;
        // The number 2 in the "simplex" array is at the second largest
        // coordinate.

        // The integer offsets for the third simplex corner
        int i2 = simplex[c][0] >= 2 ? 1 : 0;
        int j2 = simplex[c][1] >= 2 ? 1 : 0;
        int k2 = simplex[c][2] >= 2 ? 1 : 0;
        int l2 = simplex[c][3] >= 2 ? 1 : 0;
        // The number 1 in the "simplex" array is at the second smallest
        // coordinate.

        // The integer offsets for the fourth simplex corner
        int i3 = simplex[c][0] >= 1 ? 1 : 0;
        int j3 = simplex[c][1] >= 1 ? 1 : 0;
        int k3 = simplex[c][2] >= 1 ? 1 : 0;
        int l3 = simplex[c][3] >= 1 ? 1 : 0;
        // The fifth corner has all coordinate offsets = 1, so no need to
        // look that up.
        double x1 = x0 - i1 + G4; // Offsets for second corner in (x,y,z,w) coords
        double y1 = y0 - j1 + G4;
        double z1 = z0 - k1 + G4;
        double w1 = w0 - l1 + G4;
        double x2 = x0 - i2 + 2.0 * G4; // Offsets for third corner in (x,y,z,w) coords
        double y2 = y0 - j2 + 2.0 * G4;
        double z2 = z0 - k2 + 2.0 * G4;
        double w2 = w0 - l2 + 2.0 * G4;
        double x3 = x0 - i3 + 3.0 * G4; // Offsets for fourth corner in (x,y,z,w) coords
        double y3 = y0 - j3 + 3.0 * G4;
        double z3 = z0 - k3 + 3.0 * G4;
        double w3 = w0 - l3 + 3.0 * G4;
        double x4 = x0 - 1.0 + 4.0 * G4; // Offsets for last corner in (x,y,z,w) coords
        double y4 = y0 - 1.0 + 4.0 * G4;
        double z4 = z0 - 1.0 + 4.0 * G4;
        double w4 = w0 - 1.0 + 4.0 * G4;

        int gi0 = determineBounded(seed + i + determine(j + determine(k + determine(l))), 32);
        int gi1 = determineBounded(seed + i + i1 + determine(j + j1 + determine(k + k1 + determine(l + l1))), 32);
        int gi2 = determineBounded(seed + i + i2 + determine(j + j2 + determine(k + k2 + determine(l + l2))), 32);
        int gi3 = determineBounded(seed + i + i3 + determine(j + j3 + determine(k + k3 + determine(l + l3))), 32);
        int gi4 = determineBounded(seed + i + 1 + determine(j + 1 + determine(k + 1 + determine(l + 1))), 32);

        // Noise contributions from the five corners are n0 to n4

        // Calculate the contribution from the five corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0, n0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad4f[gi0], x0, y0, z0, w0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1, n1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad4f[gi1], x1, y1, z1, w1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2,  n2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad4f[gi2], x2, y2, z2, w2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3, n3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad4f[gi3], x3, y3, z3, w3);
        }
        double t4 = 0.6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4, n4;
        if (t4 < 0) {
            n4 = 0.0;
        } else {
            t4 *= t4;
            n4 = t4 * t4 * dot(grad4f[gi4], x4, y4, z4, w4);
        }
        // Sum up and scale the result to cover the range [-1,1]
        return 27.0 * (n0 + n1 + n2 + n3 + n4);
    }

    /*
    public static void main(String[] args)
    {
        long hash;
        for (int x = -8; x < 8; x++) {
            for (int y = -8; y < 8; y++) {
                hash = rawNoise(x, y, 1);
                System.out.println("x=" + x + " y=" +  y);
                System.out.println("normal=" +
                        (Float.intBitsToFloat(0x3F800000 | (int)(hash & 0x7FFFFF)) - 1.0));
                System.out.println("tweaked=" +
                        (Float.intBitsToFloat(0x40000000 | (int)(hash & 0x7FFFFF)) - 3.0));
                System.out.println("half=" +
                        (Float.intBitsToFloat(0x3F000000 | (int)(hash & 0x7FFFFF)) - 0.5));
            }
        }
    }
    */
}
