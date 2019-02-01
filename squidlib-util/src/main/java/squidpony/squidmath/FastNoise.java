// This file was originally from https://github.com/Auburns/FastNoise_Java as:
// FastNoise.java
//
// MIT License
//
// Copyright(c) 2017 Jordan Peck
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// The developer's email is jorzixdan.me2@gzixmail.com (for great email, take
// off every 'zix'.)
//

package squidpony.squidmath;

import java.io.Serializable;

import static squidpony.squidmath.Noise.IntPointHash.*;
import static squidpony.squidmath.WhirlingNoise.grad4f;

/**
 * A wide range of noise functions that can all be called from one configurable object. Originally from Jordan Peck's
 * FastNoise library, hence the name (these functions are sometimes, but not always, very fast). This implements
 * Noise2D, Noise3D, and Noise4D, and while 2D and 3D noise are very fast, the 4D noise is unusually slow, and normally
 * you should use WhirlingNoise for 4D noise (for whatever reason, it's the fastest despite also taking steps to ensure
 * higher quality). Though it doesn't implement an interface for them, you can also use this to get ridged-multi simplex
 * noise (the same type as {@link Noise.Ridged2D}) with {@link #ridged2D(float, float, int, int)},
 * {@link #ridged3D(float, float, float, int, int)}, or any of the overloads that allow specifying alternate lacunarity
 * and gain.
 */
public class FastNoise implements Serializable, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    private static final long serialVersionUID = 1L;
    public static final FastNoise instance = new FastNoise();
    public static final int VALUE = 0, VALUE_FRACTAL = 1, PERLIN = 2, PERLIN_FRACTAL = 3,
            SIMPLEX = 4, SIMPLEX_FRACTAL = 5, CELLULAR = 6, WHITE_NOISE = 7, CUBIC = 8, CUBIC_FRACTAL = 9;

    public static final int LINEAR = 0, HERMITE = 1, QUINTIC = 2;

    public static final int FBM = 0, BILLOW = 1, RIDGED_MULTI = 2;

    public static final int EUCLIDEAN = 0, MANHATTAN = 1, NATURAL = 2;

    public static final int CELL_VALUE = 0, NOISE_LOOKUP = 1, DISTANCE = 2, DISTANCE_2 = 3,
            DISTANCE_2_ADD = 4, DISTANCE_2_SUB = 5, DISTANCE_2_MUL = 6, DISTANCE_2_DIV = 7;

    private int seed = 1337;
    private float frequency = 0.03125f;
    private int interpolation = HERMITE;
    private int noiseType = SIMPLEX;

    private int octaves = 3;
    private float lacunarity = 2f;
    private float gain = 0.5f;
    private int fractalType = FBM;

    private float fractalBounding;

    private int cellularDistanceFunction = EUCLIDEAN;
    private int cellularReturnType = CELL_VALUE;
    private FastNoise cellularNoiseLookup = null;

    private float gradientPerturbAmp = 1f / 0.45f;

    public FastNoise() {
        this(1337);
    }

    public FastNoise(int seed) {
        this.seed = seed;
        calculateFractalBounding();
    }
    public FastNoise(int seed, float frequency)
    {
        this(seed, frequency, SIMPLEX, 3, 2f, 0.5f);
    }
    public FastNoise(int seed, float frequency, int noiseType)
    {
        this(seed, frequency, noiseType, 3, 2f, 0.5f);
    }
    public FastNoise(int seed, float frequency, int noiseType, int octaves)
    {
        this(seed, frequency, noiseType, octaves, 2f, 0.5f);
    }

    /**
     * A constructor that takes a lot of parameters to specify the FastNoise from the start. An example call to this
     * would be {@code new FastNoise(1337, 0.02f, FastNoise.SIMPLEX_FRACTAL, 4, 0.5f, 2f)}, which makes noise with a
     * lower frequency, 4 octaves of Simplex noise, and the "inverse" effect on how those octaves work (which makes
     * the extra added octaves be more significant to the final result and also have a lower frequency, while normally
     * added octaves have a higher frequency and tend to have a minor effect on the large-scale shape of the noise).
     * @param seed the int seed for the noise, which should significantly affect the produced noise
     * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
     * @param noiseType the noiseType, which should be a constant from this class (see {@link #setNoiseType(int)})
     * @param octaves how many octaves of noise to use when the noiseType is one of the _FRACTAL types
     * @param lacunarity typically 2.0, or 0.5 to change how extra octaves work (inverse mode) 
     * @param gain typically 0.5, or 2.0 to change how extra octaves work (inverse mode)
     */
    public FastNoise(int seed, float frequency, int noiseType, int octaves, float lacunarity, float gain)
    {
        this.seed = seed;
        this.frequency = frequency;
        this.noiseType = noiseType;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
        calculateFractalBounding();
    }

    /**
     * @return Returns the seed used by this object
     */
    public int getSeed() {
        return seed;
    }

    // Sets
    // Default: 1337L

    /**
     * Sets the seed used for all noise types, as a long.
     * If this is not called, defaults to 1337L.
     * @param seed a seed as a long
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * Sets the frequency for all noise types. If this is not called, it defaults to 0.03125f (or 1f/32f).
     * @param frequency the frequency for all noise types, as a positive non-zero float
     */
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    /**
     * Gets the frequency for all noise types. The default is 0.03125f, or 1f/32f.
     * @return the frequency for all noise types, which should be a positive non-zero float
     */
    public float getFrequency()
    {
        return frequency;
    }

    /**
     * Changes the interpolation method used to smooth between noise values, using on of the following constants from
     * this class (lowest to highest quality): {@link #LINEAR} (0), {@link #HERMITE} (1), or {@link #QUINTIC} (2). If
     * this is not called, it defaults to HERMITE. This is used in Value, Gradient Noise and Position Perturbing.
     * @param interpolation an int (0, 1, or 2) corresponding  to a constant from this class for an interpolation level
     */
    public void setInterpolation(int interpolation) {
        this.interpolation = interpolation;
    }

    /**
     * Sets the default type of noise returned by {@link #getConfiguredNoise(float, float)}, using one of the following constants
     * in this class:
     * {@link #VALUE} (0), {@link #VALUE_FRACTAL} (1), {@link #PERLIN} (2), {@link #PERLIN_FRACTAL} (3),
     * {@link #SIMPLEX} (4), {@link #SIMPLEX_FRACTAL} (5), {@link #CELLULAR} (6), {@link #WHITE_NOISE} (7),
     * {@link #CUBIC} (8), or {@link #CUBIC_FRACTAL} (9). If this isn't called, getConfiguredNoise() will default to SIMPLEX.
     * @param noiseType an int from 0 to 9 corresponding to a constant from this class for a noise type
     */
    public void setNoiseType(int noiseType) {
        this.noiseType = noiseType;
    }

    /**
     * Gets the default type of noise returned by {@link #getConfiguredNoise(float, float)}, using one of the following constants
     * in this class:
     * {@link #VALUE} (0), {@link #VALUE_FRACTAL} (1), {@link #PERLIN} (2), {@link #PERLIN_FRACTAL} (3),
     * {@link #SIMPLEX} (4), {@link #SIMPLEX_FRACTAL} (5), {@link #CELLULAR} (6), {@link #WHITE_NOISE} (7),
     * {@link #CUBIC} (8), or {@link #CUBIC_FRACTAL} (9). The default is SIMPLEX.
     * @return the noise type as a code, from 0 to 9 inclusive
     */
    public int getNoiseType()
    {
        return noiseType;
    }

    /**
     * Sets the octave count for all fractal noise types.
     * If this isn't called, it will default to 3.
     * @param octaves the number of octaves to use for fractal noise types, as a positive non-zero int
     */
    public void setFractalOctaves(int octaves) {
        this.octaves = octaves;
        calculateFractalBounding();
    }

    /**
     * Gets the octave count for all fractal noise types. The default is 3.
     * @return the number of octaves to use for fractal noise types, as a positive non-zero int
     */
    public int getFractalOctaves()
    {
        return octaves;
    }

    /**
     * Sets the octave lacunarity for all fractal noise types.
     * Lacunarity is a multiplicative change to frequency between octaves. If this isn't called, it defaults to 2.
     * @param lacunarity a non-0 float that will be used for the lacunarity of fractal noise types; commonly 2.0 or 0.5
     */
    public void setFractalLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    /**
     * Sets the octave gain for all fractal noise types.
     * If this isn't called, it defaults to 0.5.
     * @param gain the gain between octaves, as a float
     */
    public void setFractalGain(float gain) {
        this.gain = gain;
        calculateFractalBounding();
    }

    /**
     * Sets the method for combining octaves in all fractal noise types, allowing an int argument corresponding to one
     * of the following constants from this class: {@link #FBM} (0), {@link #BILLOW} (1), or {@link #RIDGED_MULTI} (2).
     * If this hasn't been called, it will use FBM.
     * @param fractalType an int (0, 1, or 2) that corresponds to a constant like {@link #FBM} or {@link #RIDGED_MULTI}
     */
    public void setFractalType(int fractalType) {
        this.fractalType = fractalType;
    }

    /**
     * Gets the method for combining octaves in all fractal noise types, allowing an int argument corresponding to one
     * of the following constants from this class: {@link #FBM} (0), {@link #BILLOW} (1), or {@link #RIDGED_MULTI} (2).
     * The default is FBM.     
     * @return the fractal type as a code; 0, 1, or 2
     */
    public int getFractalType()
    {
        return fractalType;
    }
    /**
     * Sets the distance function used in cellular noise calculations, allowing an int argument corresponding to one of
     * the following constants from this class: {@link #EUCLIDEAN} (0), {@link #MANHATTAN} (1), or {@link #NATURAL} (2).
     * If this hasn't been called, it will use EUCLIDEAN.
     * @param cellularDistanceFunction an int that can be 0, 1, or 2, corresponding to a constant from this class
     */
    public void setCellularDistanceFunction(int cellularDistanceFunction) {
        this.cellularDistanceFunction = cellularDistanceFunction;
    }
    // Sets
    // Note: NoiseLookup requires another FastNoise object be set with setCellularNoiseLookup() to function
    // Default: CellValue

    /**
     * Sets the return type from cellular noise calculations, allowing an int argument corresponding to one of the
     * following constants from this class: {@link #CELL_VALUE} (0), {@link #NOISE_LOOKUP} (1), {@link #DISTANCE} (2),
     * {@link #DISTANCE_2} (3), {@link #DISTANCE_2_ADD} (4), {@link #DISTANCE_2_SUB} (5), {@link #DISTANCE_2_MUL} (6),
     * or {@link #DISTANCE_2_DIV} (7). If this isn't called, it will use CELL_VALUE.
     * @param cellularReturnType
     */
    public void setCellularReturnType(int cellularReturnType) {
        this.cellularReturnType = cellularReturnType;
    }

    // Noise used to calculate a cell value if cellular return type is NoiseLookup
    // The lookup value is acquired through getConfiguredNoise() so ensure you setNoiseType() on the noise lookup, value, gradient or simplex is recommended
    public void setCellularNoiseLookup(FastNoise noise) {
        cellularNoiseLookup = noise;
    }

    // Sets the maximum perturb distance from original location when using GradientPerturb{Fractal}(...)
    // Default: 1.0
    public void setGradientPerturbAmp(float gradientPerturbAmp) {
        this.gradientPerturbAmp = gradientPerturbAmp / (float) 0.45;
    }

    @Override
    public double getNoise(double x, double y) {
        return singleSimplex(seed, (float)x, (float)y);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return singleSimplex((int) (seed ^ seed >>> 32), (float)x, (float)y);
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return singleSimplex(seed, (float)x, (float)y, (float)z);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return singleSimplex((int) (seed ^ seed >>> 32), (float)x, (float)y, (float)z);
    }

    @Override
    public double getNoise(double x, double y, double z, double w) {
        return singleSimplex(seed, (float)x, (float)y, (float)z, (float)w);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return singleSimplex((int) (seed ^ seed >>> 32), (float)x, (float)y, (float)z, (float)w);
    }

    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return singleSimplex(seed, (float)x, (float)y, (float)z, (float)w, (float)u, (float)v);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        return singleSimplex((int) (seed ^ seed >>> 32), (float)x, (float)y, (float)z, (float)w, (float)u, (float)v);
    }

    public float getNoiseWithSeed(float x, float y, int seed) {
        return singleSimplex(seed, x, y);
    }
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        return singleSimplex(seed, x, y, z);
    }
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        return singleSimplex(seed, x, y, z, w);
    }
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        return singleSimplex(seed, x, y, z, w, u, v);
    }
    static class Float2 {
        public final float x, y;

        public Float2(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Float3 {
        public final float x, y, z;

        public Float3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    static final Float2[] GRAD_2D = {
            new Float2(-1, -1), new Float2(1, -1), new Float2(-1, 1), new Float2(1, 1),
            new Float2(0, -1), new Float2(-1, 0), new Float2(0, 1), new Float2(1, 0),
    };

    static final Float3[] GRAD_3D =
            {
            new Float3(-0.448549002408981f,  1.174316525459290f,  0.000000000000001f  ),
            new Float3(0.000000000000001f,  1.069324374198914f,  0.660878777503967f   ),
            new Float3(0.448549002408981f,  1.174316525459290f,  0.000000000000001f   ),
            new Float3(0.000000000000001f,  1.069324374198914f, -0.660878777503967f   ),
            new Float3(-0.725767493247986f,  0.725767493247986f, -0.725767493247986f  ),
            new Float3(-1.069324374198914f,  0.660878777503967f,  0.000000000000001f  ),
            new Float3(-0.725767493247986f,  0.725767493247986f,  0.725767493247986f  ),
            new Float3(0.725767493247986f,  0.725767493247986f,  0.725767493247986f   ),
            new Float3(1.069324374198914f,  0.660878777503967f,  0.000000000000000f   ),
            new Float3(0.725767493247986f,  0.725767493247986f, -0.725767493247986f   ),
            new Float3(-0.660878777503967f,  0.000000000000003f, -1.069324374198914f  ),
            new Float3(-1.174316525459290f,  0.000000000000003f, -0.448549002408981f  ),
            new Float3(0.000000000000000f,  0.448549002408981f, -1.174316525459290f   ),
            new Float3(-0.660878777503967f,  0.000000000000001f,  1.069324374198914f  ),
            new Float3(0.000000000000001f,  0.448549002408981f,  1.174316525459290f   ),
            new Float3(-1.174316525459290f,  0.000000000000001f,  0.448549002408981f  ),
            new Float3(0.660878777503967f,  0.000000000000001f,  1.069324374198914f   ),
            new Float3(1.174316525459290f,  0.000000000000001f,  0.448549002408981f   ),
            new Float3(0.660878777503967f,  0.000000000000001f, -1.069324374198914f   ),
            new Float3(1.174316525459290f,  0.000000000000001f, -0.448549002408981f   ),
            new Float3(-0.725767493247986f, -0.725767493247986f, -0.725767493247986f  ),
            new Float3(-1.069324374198914f, -0.660878777503967f, -0.000000000000001f  ),
            new Float3(-0.000000000000001f, -0.448549002408981f, -1.174316525459290f  ),
            new Float3(-0.000000000000001f, -0.448549002408981f,  1.174316525459290f  ),
            new Float3(-0.725767493247986f, -0.725767493247986f,  0.725767493247986f  ),
            new Float3(0.725767493247986f, -0.725767493247986f,  0.725767493247986f   ),
            new Float3(1.069324374198914f, -0.660878777503967f,  0.000000000000001f   ),
            new Float3(0.725767493247986f, -0.725767493247986f, -0.725767493247986f   ),
            new Float3(-0.000000000000004f, -1.069324374198914f, -0.660878777503967f  ),
            new Float3(-0.448549002408981f, -1.174316525459290f, -0.000000000000003f  ),
            new Float3(-0.000000000000003f, -1.069324374198914f,  0.660878777503967f  ),
            new Float3(0.448549002408981f, -1.174316525459290f,  0.000000000000003f   ),
    };


    private static final Float2[] CELL_2D =
            {
                    new Float2(-0.4313539279f, 0.1281943404f), new Float2(-0.1733316799f, 0.415278375f), new Float2(-0.2821957395f, -0.3505218461f), new Float2(-0.2806473808f, 0.3517627718f), new Float2(0.3125508975f, -0.3237467165f), new Float2(0.3383018443f, -0.2967353402f), new Float2(-0.4393982022f, -0.09710417025f), new Float2(-0.4460443703f, -0.05953502905f),
                    new Float2(-0.302223039f, 0.3334085102f), new Float2(-0.212681052f, -0.3965687458f), new Float2(-0.2991156529f, 0.3361990872f), new Float2(0.2293323691f, 0.3871778202f), new Float2(0.4475439151f, -0.04695150755f), new Float2(0.1777518f, 0.41340573f), new Float2(0.1688522499f, -0.4171197882f), new Float2(-0.0976597166f, 0.4392750616f),
                    new Float2(0.08450188373f, 0.4419948321f), new Float2(-0.4098760448f, -0.1857461384f), new Float2(0.3476585782f, -0.2857157906f), new Float2(-0.3350670039f, -0.30038326f), new Float2(0.2298190031f, -0.3868891648f), new Float2(-0.01069924099f, 0.449872789f), new Float2(-0.4460141246f, -0.05976119672f), new Float2(0.3650293864f, 0.2631606867f),
                    new Float2(-0.349479423f, 0.2834856838f), new Float2(-0.4122720642f, 0.1803655873f), new Float2(-0.267327811f, 0.3619887311f), new Float2(0.322124041f, -0.3142230135f), new Float2(0.2880445931f, -0.3457315612f), new Float2(0.3892170926f, -0.2258540565f), new Float2(0.4492085018f, -0.02667811596f), new Float2(-0.4497724772f, 0.01430799601f),
                    new Float2(0.1278175387f, -0.4314657307f), new Float2(-0.03572100503f, 0.4485799926f), new Float2(-0.4297407068f, -0.1335025276f), new Float2(-0.3217817723f, 0.3145735065f), new Float2(-0.3057158873f, 0.3302087162f), new Float2(-0.414503978f, 0.1751754899f), new Float2(-0.3738139881f, 0.2505256519f), new Float2(0.2236891408f, -0.3904653228f),
                    new Float2(0.002967775577f, -0.4499902136f), new Float2(0.1747128327f, -0.4146991995f), new Float2(-0.4423772489f, -0.08247647938f), new Float2(-0.2763960987f, -0.355112935f), new Float2(-0.4019385906f, -0.2023496216f), new Float2(0.3871414161f, -0.2293938184f), new Float2(-0.430008727f, 0.1326367019f), new Float2(-0.03037574274f, -0.4489736231f),
                    new Float2(-0.3486181573f, 0.2845441624f), new Float2(0.04553517144f, -0.4476902368f), new Float2(-0.0375802926f, 0.4484280562f), new Float2(0.3266408905f, 0.3095250049f), new Float2(0.06540017593f, -0.4452222108f), new Float2(0.03409025829f, 0.448706869f), new Float2(-0.4449193635f, 0.06742966669f), new Float2(-0.4255936157f, -0.1461850686f),
                    new Float2(0.449917292f, 0.008627302568f), new Float2(0.05242606404f, 0.4469356864f), new Float2(-0.4495305179f, -0.02055026661f), new Float2(-0.1204775703f, 0.4335725488f), new Float2(-0.341986385f, -0.2924813028f), new Float2(0.3865320182f, 0.2304191809f), new Float2(0.04506097811f, -0.447738214f), new Float2(-0.06283465979f, 0.4455915232f),
                    new Float2(0.3932600341f, -0.2187385324f), new Float2(0.4472261803f, -0.04988730975f), new Float2(0.3753571011f, -0.2482076684f), new Float2(-0.273662295f, 0.357223947f), new Float2(0.1700461538f, 0.4166344988f), new Float2(0.4102692229f, 0.1848760794f), new Float2(0.323227187f, -0.3130881435f), new Float2(-0.2882310238f, -0.3455761521f),
                    new Float2(0.2050972664f, 0.4005435199f), new Float2(0.4414085979f, -0.08751256895f), new Float2(-0.1684700334f, 0.4172743077f), new Float2(-0.003978032396f, 0.4499824166f), new Float2(-0.2055133639f, 0.4003301853f), new Float2(-0.006095674897f, -0.4499587123f), new Float2(-0.1196228124f, -0.4338091548f), new Float2(0.3901528491f, -0.2242337048f),
                    new Float2(0.01723531752f, 0.4496698165f), new Float2(-0.3015070339f, 0.3340561458f), new Float2(-0.01514262423f, -0.4497451511f), new Float2(-0.4142574071f, -0.1757577897f), new Float2(-0.1916377265f, -0.4071547394f), new Float2(0.3749248747f, 0.2488600778f), new Float2(-0.2237774255f, 0.3904147331f), new Float2(-0.4166343106f, -0.1700466149f),
                    new Float2(0.3619171625f, 0.267424695f), new Float2(0.1891126846f, -0.4083336779f), new Float2(-0.3127425077f, 0.323561623f), new Float2(-0.3281807787f, 0.307891826f), new Float2(-0.2294806661f, 0.3870899429f), new Float2(-0.3445266136f, 0.2894847362f), new Float2(-0.4167095422f, -0.1698621719f), new Float2(-0.257890321f, -0.3687717212f),
                    new Float2(-0.3612037825f, 0.2683874578f), new Float2(0.2267996491f, 0.3886668486f), new Float2(0.207157062f, 0.3994821043f), new Float2(0.08355176718f, -0.4421754202f), new Float2(-0.4312233307f, 0.1286329626f), new Float2(0.3257055497f, 0.3105090899f), new Float2(0.177701095f, -0.4134275279f), new Float2(-0.445182522f, 0.06566979625f),
                    new Float2(0.3955143435f, 0.2146355146f), new Float2(-0.4264613988f, 0.1436338239f), new Float2(-0.3793799665f, -0.2420141339f), new Float2(0.04617599081f, -0.4476245948f), new Float2(-0.371405428f, -0.2540826796f), new Float2(0.2563570295f, -0.3698392535f), new Float2(0.03476646309f, 0.4486549822f), new Float2(-0.3065454405f, 0.3294387544f),
                    new Float2(-0.2256979823f, 0.3893076172f), new Float2(0.4116448463f, -0.1817925206f), new Float2(-0.2907745828f, -0.3434387019f), new Float2(0.2842278468f, -0.348876097f), new Float2(0.3114589359f, -0.3247973695f), new Float2(0.4464155859f, -0.0566844308f), new Float2(-0.3037334033f, -0.3320331606f), new Float2(0.4079607166f, 0.1899159123f),
                    new Float2(-0.3486948919f, -0.2844501228f), new Float2(0.3264821436f, 0.3096924441f), new Float2(0.3211142406f, 0.3152548881f), new Float2(0.01183382662f, 0.4498443737f), new Float2(0.4333844092f, 0.1211526057f), new Float2(0.3118668416f, 0.324405723f), new Float2(-0.272753471f, 0.3579183483f), new Float2(-0.422228622f, -0.1556373694f),
                    new Float2(-0.1009700099f, -0.4385260051f), new Float2(-0.2741171231f, -0.3568750521f), new Float2(-0.1465125133f, 0.4254810025f), new Float2(0.2302279044f, -0.3866459777f), new Float2(-0.3699435608f, 0.2562064828f), new Float2(0.105700352f, -0.4374099171f), new Float2(-0.2646713633f, 0.3639355292f), new Float2(0.3521828122f, 0.2801200935f),
                    new Float2(-0.1864187807f, -0.4095705534f), new Float2(0.1994492955f, -0.4033856449f), new Float2(0.3937065066f, 0.2179339044f), new Float2(-0.3226158377f, 0.3137180602f), new Float2(0.3796235338f, 0.2416318948f), new Float2(0.1482921929f, 0.4248640083f), new Float2(-0.407400394f, 0.1911149365f), new Float2(0.4212853031f, 0.1581729856f),
                    new Float2(-0.2621297173f, 0.3657704353f), new Float2(-0.2536986953f, -0.3716678248f), new Float2(-0.2100236383f, 0.3979825013f), new Float2(0.3624152444f, 0.2667493029f), new Float2(-0.3645038479f, -0.2638881295f), new Float2(0.2318486784f, 0.3856762766f), new Float2(-0.3260457004f, 0.3101519002f), new Float2(-0.2130045332f, -0.3963950918f),
                    new Float2(0.3814998766f, -0.2386584257f), new Float2(-0.342977305f, 0.2913186713f), new Float2(-0.4355865605f, 0.1129794154f), new Float2(-0.2104679605f, 0.3977477059f), new Float2(0.3348364681f, -0.3006402163f), new Float2(0.3430468811f, 0.2912367377f), new Float2(-0.2291836801f, -0.3872658529f), new Float2(0.2547707298f, -0.3709337882f),
                    new Float2(0.4236174945f, -0.151816397f), new Float2(-0.15387742f, 0.4228731957f), new Float2(-0.4407449312f, 0.09079595574f), new Float2(-0.06805276192f, -0.444824484f), new Float2(0.4453517192f, -0.06451237284f), new Float2(0.2562464609f, -0.3699158705f), new Float2(0.3278198355f, -0.3082761026f), new Float2(-0.4122774207f, -0.1803533432f),
                    new Float2(0.3354090914f, -0.3000012356f), new Float2(0.446632869f, -0.05494615882f), new Float2(-0.1608953296f, 0.4202531296f), new Float2(-0.09463954939f, 0.4399356268f), new Float2(-0.02637688324f, -0.4492262904f), new Float2(0.447102804f, -0.05098119915f), new Float2(-0.4365670908f, 0.1091291678f), new Float2(-0.3959858651f, 0.2137643437f),
                    new Float2(-0.4240048207f, -0.1507312575f), new Float2(-0.3882794568f, 0.2274622243f), new Float2(-0.4283652566f, -0.1378521198f), new Float2(0.3303888091f, 0.305521251f), new Float2(0.3321434919f, -0.3036127481f), new Float2(-0.413021046f, -0.1786438231f), new Float2(0.08403060337f, -0.4420846725f), new Float2(-0.3822882919f, 0.2373934748f),
                    new Float2(-0.3712395594f, -0.2543249683f), new Float2(0.4472363971f, -0.04979563372f), new Float2(-0.4466591209f, 0.05473234629f), new Float2(0.0486272539f, -0.4473649407f), new Float2(-0.4203101295f, -0.1607463688f), new Float2(0.2205360833f, 0.39225481f), new Float2(-0.3624900666f, 0.2666476169f), new Float2(-0.4036086833f, -0.1989975647f),
                    new Float2(0.2152727807f, 0.3951678503f), new Float2(-0.4359392962f, -0.1116106179f), new Float2(0.4178354266f, 0.1670735057f), new Float2(0.2007630161f, 0.4027334247f), new Float2(-0.07278067175f, -0.4440754146f), new Float2(0.3644748615f, -0.2639281632f), new Float2(-0.4317451775f, 0.126870413f), new Float2(-0.297436456f, 0.3376855855f),
                    new Float2(-0.2998672222f, 0.3355289094f), new Float2(-0.2673674124f, 0.3619594822f), new Float2(0.2808423357f, 0.3516071423f), new Float2(0.3498946567f, 0.2829730186f), new Float2(-0.2229685561f, 0.390877248f), new Float2(0.3305823267f, 0.3053118493f), new Float2(-0.2436681211f, -0.3783197679f), new Float2(-0.03402776529f, 0.4487116125f),
                    new Float2(-0.319358823f, 0.3170330301f), new Float2(0.4454633477f, -0.06373700535f), new Float2(0.4483504221f, 0.03849544189f), new Float2(-0.4427358436f, -0.08052932871f), new Float2(0.05452298565f, 0.4466847255f), new Float2(-0.2812560807f, 0.3512762688f), new Float2(0.1266696921f, 0.4318041097f), new Float2(-0.3735981243f, 0.2508474468f),
                    new Float2(0.2959708351f, -0.3389708908f), new Float2(-0.3714377181f, 0.254035473f), new Float2(-0.404467102f, -0.1972469604f), new Float2(0.1636165687f, -0.419201167f), new Float2(0.3289185495f, -0.3071035458f), new Float2(-0.2494824991f, -0.3745109914f), new Float2(0.03283133272f, 0.4488007393f), new Float2(-0.166306057f, -0.4181414777f),
                    new Float2(-0.106833179f, 0.4371346153f), new Float2(0.06440260376f, -0.4453676062f), new Float2(-0.4483230967f, 0.03881238203f), new Float2(-0.421377757f, -0.1579265206f), new Float2(0.05097920662f, -0.4471030312f), new Float2(0.2050584153f, -0.4005634111f), new Float2(0.4178098529f, -0.167137449f), new Float2(-0.3565189504f, -0.2745801121f),
                    new Float2(0.4478398129f, 0.04403977727f), new Float2(-0.3399999602f, -0.2947881053f), new Float2(0.3767121994f, 0.2461461331f), new Float2(-0.3138934434f, 0.3224451987f), new Float2(-0.1462001792f, -0.4255884251f), new Float2(0.3970290489f, -0.2118205239f), new Float2(0.4459149305f, -0.06049689889f), new Float2(-0.4104889426f, -0.1843877112f),
                    new Float2(0.1475103971f, -0.4251360756f), new Float2(0.09258030352f, 0.4403735771f), new Float2(-0.1589664637f, -0.4209865359f), new Float2(0.2482445008f, 0.3753327428f), new Float2(0.4383624232f, -0.1016778537f), new Float2(0.06242802956f, 0.4456486745f), new Float2(0.2846591015f, -0.3485243118f), new Float2(-0.344202744f, -0.2898697484f),
                    new Float2(0.1198188883f, -0.4337550392f), new Float2(-0.243590703f, 0.3783696201f), new Float2(0.2958191174f, -0.3391033025f), new Float2(-0.1164007991f, 0.4346847754f), new Float2(0.1274037151f, -0.4315881062f), new Float2(0.368047306f, 0.2589231171f), new Float2(0.2451436949f, 0.3773652989f), new Float2(-0.4314509715f, 0.12786735f),
            };

    private static final Float3[] CELL_3D =
            {
                    new Float3(0.1453787434f, -0.4149781685f, -0.0956981749f), new Float3(-0.01242829687f, -0.1457918398f, -0.4255470325f), new Float3(0.2877979582f, -0.02606483451f, -0.3449535616f), new Float3(-0.07732986802f, 0.2377094325f, 0.3741848704f), new Float3(0.1107205875f, -0.3552302079f, -0.2530858567f), new Float3(0.2755209141f, 0.2640521179f, -0.238463215f), new Float3(0.294168941f, 0.1526064594f, 0.3044271714f), new Float3(0.4000921098f, -0.2034056362f, 0.03244149937f),
                    new Float3(-0.1697304074f, 0.3970864695f, -0.1265461359f), new Float3(-0.1483224484f, -0.3859694688f, 0.1775613147f), new Float3(0.2623596946f, -0.2354852944f, 0.2796677792f), new Float3(-0.2709003183f, 0.3505271138f, -0.07901746678f), new Float3(-0.03516550699f, 0.3885234328f, 0.2243054374f), new Float3(-0.1267712655f, 0.1920044036f, 0.3867342179f), new Float3(0.02952021915f, 0.4409685861f, 0.08470692262f), new Float3(-0.2806854217f, -0.266996757f, 0.2289725438f),
                    new Float3(-0.171159547f, 0.2141185563f, 0.3568720405f), new Float3(0.2113227183f, 0.3902405947f, -0.07453178509f), new Float3(-0.1024352839f, 0.2128044156f, -0.3830421561f), new Float3(-0.3304249877f, -0.1566986703f, 0.2622305365f), new Float3(0.2091111325f, 0.3133278055f, -0.2461670583f), new Float3(0.344678154f, -0.1944240454f, -0.2142341261f), new Float3(0.1984478035f, -0.3214342325f, -0.2445373252f), new Float3(-0.2929008603f, 0.2262915116f, 0.2559320961f),
                    new Float3(-0.1617332831f, 0.006314769776f, -0.4198838754f), new Float3(-0.3582060271f, -0.148303178f, -0.2284613961f), new Float3(-0.1852067326f, -0.3454119342f, -0.2211087107f), new Float3(0.3046301062f, 0.1026310383f, 0.314908508f), new Float3(-0.03816768434f, -0.2551766358f, -0.3686842991f), new Float3(-0.4084952196f, 0.1805950793f, 0.05492788837f), new Float3(-0.02687443361f, -0.2749741471f, 0.3551999201f), new Float3(-0.03801098351f, 0.3277859044f, 0.3059600725f),
                    new Float3(0.2371120802f, 0.2900386767f, -0.2493099024f), new Float3(0.4447660503f, 0.03946930643f, 0.05590469027f), new Float3(0.01985147278f, -0.01503183293f, -0.4493105419f), new Float3(0.4274339143f, 0.03345994256f, -0.1366772882f), new Float3(-0.2072988631f, 0.2871414597f, -0.2776273824f), new Float3(-0.3791240978f, 0.1281177671f, 0.2057929936f), new Float3(-0.2098721267f, -0.1007087278f, -0.3851122467f), new Float3(0.01582798878f, 0.4263894424f, 0.1429738373f),
                    new Float3(-0.1888129464f, -0.3160996813f, -0.2587096108f), new Float3(0.1612988974f, -0.1974805082f, -0.3707885038f), new Float3(-0.08974491322f, 0.229148752f, -0.3767448739f), new Float3(0.07041229526f, 0.4150230285f, -0.1590534329f), new Float3(-0.1082925611f, -0.1586061639f, 0.4069604477f), new Float3(0.2474100658f, -0.3309414609f, 0.1782302128f), new Float3(-0.1068836661f, -0.2701644537f, -0.3436379634f), new Float3(0.2396452163f, 0.06803600538f, -0.3747549496f),
                    new Float3(-0.3063886072f, 0.2597428179f, 0.2028785103f), new Float3(0.1593342891f, -0.3114350249f, -0.2830561951f), new Float3(0.2709690528f, 0.1412648683f, -0.3303331794f), new Float3(-0.1519780427f, 0.3623355133f, 0.2193527988f), new Float3(0.1699773681f, 0.3456012883f, 0.2327390037f), new Float3(-0.1986155616f, 0.3836276443f, -0.1260225743f), new Float3(-0.1887482106f, -0.2050154888f, -0.353330953f), new Float3(0.2659103394f, 0.3015631259f, -0.2021172246f),
                    new Float3(-0.08838976154f, -0.4288819642f, -0.1036702021f), new Float3(-0.04201869311f, 0.3099592485f, 0.3235115047f), new Float3(-0.3230334656f, 0.201549922f, -0.2398478873f), new Float3(0.2612720941f, 0.2759854499f, -0.2409749453f), new Float3(0.385713046f, 0.2193460345f, 0.07491837764f), new Float3(0.07654967953f, 0.3721732183f, 0.241095919f), new Float3(0.4317038818f, -0.02577753072f, 0.1243675091f), new Float3(-0.2890436293f, -0.3418179959f, -0.04598084447f),
                    new Float3(-0.2201947582f, 0.383023377f, -0.08548310451f), new Float3(0.4161322773f, -0.1669634289f, -0.03817251927f), new Float3(0.2204718095f, 0.02654238946f, -0.391391981f), new Float3(-0.1040307469f, 0.3890079625f, -0.2008741118f), new Float3(-0.1432122615f, 0.371614387f, -0.2095065525f), new Float3(0.3978380468f, -0.06206669342f, 0.2009293758f), new Float3(-0.2599274663f, 0.2616724959f, -0.2578084893f), new Float3(0.4032618332f, -0.1124593585f, 0.1650235939f),
                    new Float3(-0.08953470255f, -0.3048244735f, 0.3186935478f), new Float3(0.118937202f, -0.2875221847f, 0.325092195f), new Float3(0.02167047076f, -0.03284630549f, -0.4482761547f), new Float3(-0.3411343612f, 0.2500031105f, 0.1537068389f), new Float3(0.3162964612f, 0.3082064153f, -0.08640228117f), new Float3(0.2355138889f, -0.3439334267f, -0.1695376245f), new Float3(-0.02874541518f, -0.3955933019f, 0.2125550295f), new Float3(-0.2461455173f, 0.02020282325f, -0.3761704803f),
                    new Float3(0.04208029445f, -0.4470439576f, 0.02968078139f), new Float3(0.2727458746f, 0.2288471896f, -0.2752065618f), new Float3(-0.1347522818f, -0.02720848277f, -0.4284874806f), new Float3(0.3829624424f, 0.1231931484f, -0.2016512234f), new Float3(-0.3547613644f, 0.1271702173f, 0.2459107769f), new Float3(0.2305790207f, 0.3063895591f, 0.2354968222f), new Float3(-0.08323845599f, -0.1922245118f, 0.3982726409f), new Float3(0.2993663085f, -0.2619918095f, -0.2103333191f),
                    new Float3(-0.2154865723f, 0.2706747713f, 0.287751117f), new Float3(0.01683355354f, -0.2680655787f, -0.3610505186f), new Float3(0.05240429123f, 0.4335128183f, -0.1087217856f), new Float3(0.00940104872f, -0.4472890582f, 0.04841609928f), new Float3(0.3465688735f, 0.01141914583f, -0.2868093776f), new Float3(-0.3706867948f, -0.2551104378f, 0.003156692623f), new Float3(0.2741169781f, 0.2139972417f, -0.2855959784f), new Float3(0.06413433865f, 0.1708718512f, 0.4113266307f),
                    new Float3(-0.388187972f, -0.03973280434f, -0.2241236325f), new Float3(0.06419469312f, -0.2803682491f, 0.3460819069f), new Float3(-0.1986120739f, -0.3391173584f, 0.2192091725f), new Float3(-0.203203009f, -0.3871641506f, 0.1063600375f), new Float3(-0.1389736354f, -0.2775901578f, -0.3257760473f), new Float3(-0.06555641638f, 0.342253257f, -0.2847192729f), new Float3(-0.2529246486f, -0.2904227915f, 0.2327739768f), new Float3(0.1444476522f, 0.1069184044f, 0.4125570634f),
                    new Float3(-0.3643780054f, -0.2447099973f, -0.09922543227f), new Float3(0.4286142488f, -0.1358496089f, -0.01829506817f), new Float3(0.165872923f, -0.3136808464f, -0.2767498872f), new Float3(0.2219610524f, -0.3658139958f, 0.1393320198f), new Float3(0.04322940318f, -0.3832730794f, 0.2318037215f), new Float3(-0.08481269795f, -0.4404869674f, -0.03574965489f), new Float3(0.1822082075f, -0.3953259299f, 0.1140946023f), new Float3(-0.3269323334f, 0.3036542563f, 0.05838957105f),
                    new Float3(-0.4080485344f, 0.04227858267f, -0.184956522f), new Float3(0.2676025294f, -0.01299671652f, 0.36155217f), new Float3(0.3024892441f, -0.1009990293f, -0.3174892964f), new Float3(0.1448494052f, 0.425921681f, -0.0104580805f), new Float3(0.4198402157f, 0.08062320474f, 0.1404780841f), new Float3(-0.3008872161f, -0.333040905f, -0.03241355801f), new Float3(0.3639310428f, -0.1291284382f, -0.2310412139f), new Float3(0.3295806598f, 0.0184175994f, -0.3058388149f),
                    new Float3(0.2776259487f, -0.2974929052f, -0.1921504723f), new Float3(0.4149000507f, -0.144793182f, -0.09691688386f), new Float3(0.145016715f, -0.0398992945f, 0.4241205002f), new Float3(0.09299023471f, -0.299732164f, -0.3225111565f), new Float3(0.1028907093f, -0.361266869f, 0.247789732f), new Float3(0.2683057049f, -0.07076041213f, -0.3542668666f), new Float3(-0.4227307273f, -0.07933161816f, -0.1323073187f), new Float3(-0.1781224702f, 0.1806857196f, -0.3716517945f),
                    new Float3(0.4390788626f, -0.02841848598f, -0.09435116353f), new Float3(0.2972583585f, 0.2382799621f, -0.2394997452f), new Float3(-0.1707002821f, 0.2215845691f, 0.3525077196f), new Float3(0.3806686614f, 0.1471852559f, -0.1895464869f), new Float3(-0.1751445661f, -0.274887877f, 0.3102596268f), new Float3(-0.2227237566f, -0.2316778837f, 0.3149912482f), new Float3(0.1369633021f, 0.1341343041f, -0.4071228836f), new Float3(-0.3529503428f, -0.2472893463f, -0.129514612f),
                    new Float3(-0.2590744185f, -0.2985577559f, -0.2150435121f), new Float3(-0.3784019401f, 0.2199816631f, -0.1044989934f), new Float3(-0.05635805671f, 0.1485737441f, 0.4210102279f), new Float3(0.3251428613f, 0.09666046873f, -0.2957006485f), new Float3(-0.4190995804f, 0.1406751354f, -0.08405978803f), new Float3(-0.3253150961f, -0.3080335042f, -0.04225456877f), new Float3(0.2857945863f, -0.05796152095f, 0.3427271751f), new Float3(-0.2733604046f, 0.1973770973f, -0.2980207554f),
                    new Float3(0.219003657f, 0.2410037886f, -0.3105713639f), new Float3(0.3182767252f, -0.271342949f, 0.1660509868f), new Float3(-0.03222023115f, -0.3331161506f, -0.300824678f), new Float3(-0.3087780231f, 0.1992794134f, -0.2596995338f), new Float3(-0.06487611647f, -0.4311322747f, 0.1114273361f), new Float3(0.3921171432f, -0.06294284106f, -0.2116183942f), new Float3(-0.1606404506f, -0.358928121f, -0.2187812825f), new Float3(-0.03767771199f, -0.2290351443f, 0.3855169162f),
                    new Float3(0.1394866832f, -0.3602213994f, 0.2308332918f), new Float3(-0.4345093872f, 0.005751117145f, 0.1169124335f), new Float3(-0.1044637494f, 0.4168128432f, -0.1336202785f), new Float3(0.2658727501f, 0.2551943237f, 0.2582393035f), new Float3(0.2051461999f, 0.1975390727f, 0.3484154868f), new Float3(-0.266085566f, 0.23483312f, 0.2766800993f), new Float3(0.07849405464f, -0.3300346342f, -0.2956616708f), new Float3(-0.2160686338f, 0.05376451292f, -0.3910546287f),
                    new Float3(-0.185779186f, 0.2148499206f, 0.3490352499f), new Float3(0.02492421743f, -0.3229954284f, -0.3123343347f), new Float3(-0.120167831f, 0.4017266681f, 0.1633259825f), new Float3(-0.02160084693f, -0.06885389554f, 0.4441762538f), new Float3(0.2597670064f, 0.3096300784f, 0.1978643903f), new Float3(-0.1611553854f, -0.09823036005f, 0.4085091653f), new Float3(-0.3278896792f, 0.1461670309f, 0.2713366126f), new Float3(0.2822734956f, 0.03754421121f, -0.3484423997f),
                    new Float3(0.03169341113f, 0.347405252f, -0.2842624114f), new Float3(0.2202613604f, -0.3460788041f, -0.1849713341f), new Float3(0.2933396046f, 0.3031973659f, 0.1565989581f), new Float3(-0.3194922995f, 0.2453752201f, -0.200538455f), new Float3(-0.3441586045f, -0.1698856132f, -0.2349334659f), new Float3(0.2703645948f, -0.3574277231f, 0.04060059933f), new Float3(0.2298568861f, 0.3744156221f, 0.0973588921f), new Float3(0.09326603877f, -0.3170108894f, 0.3054595587f),
                    new Float3(-0.1116165319f, -0.2985018719f, 0.3177080142f), new Float3(0.2172907365f, -0.3460005203f, -0.1885958001f), new Float3(0.1991339479f, 0.3820341668f, -0.1299829458f), new Float3(-0.0541918155f, -0.2103145071f, 0.39412061f), new Float3(0.08871336998f, 0.2012117383f, 0.3926114802f), new Float3(0.2787673278f, 0.3505404674f, 0.04370535101f), new Float3(-0.322166438f, 0.3067213525f, 0.06804996813f), new Float3(-0.4277366384f, 0.132066775f, 0.04582286686f),
                    new Float3(0.240131882f, -0.1612516055f, 0.344723946f), new Float3(0.1448607981f, -0.2387819045f, 0.3528435224f), new Float3(-0.3837065682f, -0.2206398454f, 0.08116235683f), new Float3(-0.4382627882f, -0.09082753406f, -0.04664855374f), new Float3(-0.37728353f, 0.05445141085f, 0.2391488697f), new Float3(0.1259579313f, 0.348394558f, 0.2554522098f), new Float3(-0.1406285511f, -0.270877371f, -0.3306796947f), new Float3(-0.1580694418f, 0.4162931958f, -0.06491553533f),
                    new Float3(0.2477612106f, -0.2927867412f, -0.2353514536f), new Float3(0.2916132853f, 0.3312535401f, 0.08793624968f), new Float3(0.07365265219f, -0.1666159848f, 0.411478311f), new Float3(-0.26126526f, -0.2422237692f, 0.2748965434f), new Float3(-0.3721862032f, 0.252790166f, 0.008634938242f), new Float3(-0.3691191571f, -0.255281188f, 0.03290232422f), new Float3(0.2278441737f, -0.3358364886f, 0.1944244981f), new Float3(0.363398169f, -0.2310190248f, 0.1306597909f),
                    new Float3(-0.304231482f, -0.2698452035f, 0.1926830856f), new Float3(-0.3199312232f, 0.316332536f, -0.008816977938f), new Float3(0.2874852279f, 0.1642275508f, -0.304764754f), new Float3(-0.1451096801f, 0.3277541114f, -0.2720669462f), new Float3(0.3220090754f, 0.0511344108f, 0.3101538769f), new Float3(-0.1247400865f, -0.04333605335f, -0.4301882115f), new Float3(-0.2829555867f, -0.3056190617f, -0.1703910946f), new Float3(0.1069384374f, 0.3491024667f, -0.2630430352f),
                    new Float3(-0.1420661144f, -0.3055376754f, -0.2982682484f), new Float3(-0.250548338f, 0.3156466809f, -0.2002316239f), new Float3(0.3265787872f, 0.1871229129f, 0.2466400438f), new Float3(0.07646097258f, -0.3026690852f, 0.324106687f), new Float3(0.3451771584f, 0.2757120714f, -0.0856480183f), new Float3(0.298137964f, 0.2852657134f, 0.179547284f), new Float3(0.2812250376f, 0.3466716415f, 0.05684409612f), new Float3(0.4390345476f, -0.09790429955f, -0.01278335452f),
                    new Float3(0.2148373234f, 0.1850172527f, 0.3494474791f), new Float3(0.2595421179f, -0.07946825393f, 0.3589187731f), new Float3(0.3182823114f, -0.307355516f, -0.08203022006f), new Float3(-0.4089859285f, -0.04647718411f, 0.1818526372f), new Float3(-0.2826749061f, 0.07417482322f, 0.3421885344f), new Float3(0.3483864637f, 0.225442246f, -0.1740766085f), new Float3(-0.3226415069f, -0.1420585388f, -0.2796816575f), new Float3(0.4330734858f, -0.118868561f, -0.02859407492f),
                    new Float3(-0.08717822568f, -0.3909896417f, -0.2050050172f), new Float3(-0.2149678299f, 0.3939973956f, -0.03247898316f), new Float3(-0.2687330705f, 0.322686276f, -0.1617284888f), new Float3(0.2105665099f, -0.1961317136f, -0.3459683451f), new Float3(0.4361845915f, -0.1105517485f, 0.004616608544f), new Float3(0.05333333359f, -0.313639498f, -0.3182543336f), new Float3(-0.05986216652f, 0.1361029153f, -0.4247264031f), new Float3(0.3664988455f, 0.2550543014f, -0.05590974511f),
                    new Float3(-0.2341015558f, -0.182405731f, 0.3382670703f), new Float3(-0.04730947785f, -0.4222150243f, -0.1483114513f), new Float3(-0.2391566239f, -0.2577696514f, -0.2808182972f), new Float3(-0.1242081035f, 0.4256953395f, -0.07652336246f), new Float3(0.2614832715f, -0.3650179274f, 0.02980623099f), new Float3(-0.2728794681f, -0.3499628774f, 0.07458404908f), new Float3(0.007892900508f, -0.1672771315f, 0.4176793787f), new Float3(-0.01730330376f, 0.2978486637f, -0.3368779738f),
                    new Float3(0.2054835762f, -0.3252600376f, -0.2334146693f), new Float3(-0.3231994983f, 0.1564282844f, -0.2712420987f), new Float3(-0.2669545963f, 0.2599343665f, -0.2523278991f), new Float3(-0.05554372779f, 0.3170813944f, -0.3144428146f), new Float3(-0.2083935713f, -0.310922837f, -0.2497981362f), new Float3(0.06989323478f, -0.3156141536f, 0.3130537363f), new Float3(0.3847566193f, -0.1605309138f, -0.1693876312f), new Float3(-0.3026215288f, -0.3001537679f, -0.1443188342f),
                    new Float3(0.3450735512f, 0.08611519592f, 0.2756962409f), new Float3(0.1814473292f, -0.2788782453f, -0.3029914042f), new Float3(-0.03855010448f, 0.09795110726f, 0.4375151083f), new Float3(0.3533670318f, 0.2665752752f, 0.08105160988f), new Float3(-0.007945601311f, 0.140359426f, -0.4274764309f), new Float3(0.4063099273f, -0.1491768253f, -0.1231199324f), new Float3(-0.2016773589f, 0.008816271194f, -0.4021797064f), new Float3(-0.07527055435f, -0.425643481f, -0.1251477955f),
            };


    private static int fastFloor(float f) {
        return (f >= 0 ? (int) f : (int) f - 1);
    }


    private static int fastRound(float f) {
        return (f >= 0) ? (int) (f + 0.5f) : (int) (f - 0.5f);
    }


    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }


    private static float hermiteInterpolator(float t) {
        return t * t * (3 - 2 * t);
    }


    private static float quinticInterpolator(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }


    private static float cubicLerp(float a, float b, float c, float d, float t) {
        float p = (d - c) - (a - b);
        return t * (t * t * p + t * ((a - b) - p) + (c - a)) + b;
    }

    private void calculateFractalBounding() {
        float amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        fractalBounding = 1 / ampFractal;
    }

    // Hashing
    private final static int X_PRIME = 0xB4C4D;
    private final static int Y_PRIME = 0xEE2C1;
    private final static int Z_PRIME = 0xA7E07;
    private final static int W_PRIME = 0x8F19B;
    //public static int randomize8(final int state) {return Integer.rotateLeft((state ^ state >>> 13) * ((state & 0xFFFF8) ^ 0x277B5), 7) - state >>> 24;}
    //public static int randomize6(final int state) {return Integer.rotateLeft((state ^ state >>> 13) * ((state & 0xFFFF8) ^ 0x277B5), 7) - state >>> 26;}
    //public static int randomize5(final int state) {return Integer.rotateLeft((state ^ state >>> 13) * ((state & 0xFFFF8) ^ 0x277B5), 7) - state >>> 27;}
    //public static int randomize4(final int state) {return Integer.rotateLeft((state ^ state >>> 13) * ((state & 0xFFFF8) ^ 0x277B5), 7) - state >>> 28;}

    private static int hash2D(int seed, int x, int y) {
        return Integer.rotateLeft(((seed ^= X_PRIME * x ^ Y_PRIME * y) ^ seed >>> 13) * ((seed & 0xFFFF8) ^ 0x277B5), 7) - seed >>> 24;
//        return (seed = ((seed ^= X_PRIME * x ^ Y_PRIME * y) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22);
    }

    private static int hash3D(int seed, int x, int y, int z) {
        return Integer.rotateLeft(((seed ^= X_PRIME * x ^ Y_PRIME * y ^ Z_PRIME * z) ^ seed >>> 13) * ((seed & 0xFFFF8) ^ 0x277B5), 7) - seed >>> 24;
//        return (seed = ((seed ^= X_PRIME * x ^ Y_PRIME * y ^ Z_PRIME * z) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22);
    }

    private static int hash4D(int seed, int x, int y, int z, int w) {
        return Integer.rotateLeft(((seed ^= X_PRIME * x ^ Y_PRIME * y ^ Z_PRIME * z ^ W_PRIME * w) ^ seed >>> 13) * ((seed & 0xFFFF8) ^ 0x277B5), 7) - seed >>> 24;
//        return (seed = ((seed ^= X_PRIME * x ^ Y_PRIME * y ^ Z_PRIME * z ^ W_PRIME * w) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 22);
    }

    private static float valCoord2D(int seed, int x, int y) {
        return (hashAll(x, y, seed) & 0xFFFFFF) * 0x1.0p-24f;
//        final int n = (seed ^ X_PRIME * x ^ Y_PRIME * y) >> 12;
//        return ((n * n ^ n * 0xEC4D ^ seed) & 0xFFFFFF) * 0x1.0p-24f;
    }

    private static float valCoord3D(int seed, int x, int y, int z) {
        return (hashAll(x, y, z, seed) & 0xFFFFFF) * 0x1.0p-24f;
//        final int n = (seed ^ X_PRIME * x ^ Y_PRIME * y ^ Z_PRIME * z) >> 12;
//        return ((n * n ^ n * 0xEC4D ^ seed) & 0xFFFFFF) * 0x1.0p-24f;
    }

    private static float valCoord4D(int seed, int x, int y, int z, int w) {
        return (hashAll(x, y, z, w, seed) & 0xFFFFFF) * 0x1.0p-24f;
//        final int n = (seed ^ X_PRIME * x ^ Y_PRIME * y ^ Z_PRIME * z ^ W_PRIME * w) >> 12;
//        return ((n * n ^ n * 0xEC4D ^ seed) & 0xFFFFFF) * 0x1.0p-24f;
    }

    private static float gradCoord2D(int seed, int x, int y, float xd, float yd) {
        //Float2 g = GRAD_2D[((seed ^= X_PRIME * x ^ Y_PRIME * y) ^ seed >>> 13) & 7];
        final float[] g = WhirlingNoise.phiGrad2f[hash256(x, y, seed)];
        //Float2 g = GRAD_2D[((seed ^= X_PRIME * x ^ Y_PRIME * y) ^ seed >>> 13) * ((seed & 0xFFFF8) ^ 0x277B5) >>> 29];
        return xd * g[0] + yd * g[1];
    }

    private static float gradCoord3D(int seed, int x, int y, int z, float xd, float yd, float zd) {
//        seed ^= 0xB4C4D * x ^ 0xEE2C1 * y ^ 0xA7E07 * z;
//        seed = seed * seed * seed * 60493;
//        Float3 g = GRAD_3D[(seed ^ (seed >>> 13)) & 31];
        final Float3 g = GRAD_3D[hash32(x, y, z, seed)];
        //Float3 g = GRAD_3D[((seed ^= X_PRIME * x ^ Y_PRIME * y ^ Z_PRIME * z) ^ seed >>> 13) * ((seed & 0xFFFF8) ^ 0x277B5) >>> 27];
        return xd * g.x + yd * g.y + zd * g.z;
    }

    private static float gradCoord4D(int seed, int x, int y, int z, int w, float xd, float yd, float zd, float wd) {
        final int hash = hash256(x, y, z, w, seed) & 0xFC;
        return xd * grad4f[hash] + yd * grad4f[hash + 1] + zd * grad4f[hash + 2] + wd * grad4f[hash + 3];
//        final int hash = hash32(x, y, z, w, seed); 
//        float a = yd, b = zd, c = wd;            // X,Y,Z
//        switch (hash >> 3) {          // OR, DEPENDING ON HIGH ORDER 2 BITS:
//            case 1:
//                a = wd;
//                b = xd;
//                c = yd;
//                break;     // W,X,Y
//            case 2:
//                a = zd;
//                b = wd;
//                c = xd;
//                break;     // Z,W,X
//            case 3:
//                a = yd;
//                b = zd;
//                c = wd;
//                break;     // Y,Z,W
//        }
//        return ((hash & 4) == 0 ? -a : a) + ((hash & 2) == 0 ? -b : b) + ((hash & 1) == 0 ? -c : c);
    }
    /**
     * After being configured with the setters in this class, such as {@link #setNoiseType(int)},
     * {@link #setFrequency(float)}, {@link #setFractalOctaves(int)}, and {@link #setFractalType(int)}, among others,
     * you can call this method to get the particular variety of noise you specified, in 3D.
     * @param x
     * @param y
     * @param z
     * @return noise as a float from -1f to 1f
     */
    public float getConfiguredNoise(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (noiseType) {
            case VALUE:
                return singleValue(seed, x, y, z);
            case VALUE_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleValueFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleValueFractalRidgedMulti(x, y, z);
                    default:
                        return singleValueFractalFBM(x, y, z);
                }
            case PERLIN:
                return singlePerlin(seed, x, y, z);
            case PERLIN_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singlePerlinFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singlePerlinFractalRidgedMulti(x, y, z);
                    default:
                        return singlePerlinFractalFBM(x, y, z);
                }
            case SIMPLEX_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleSimplexFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleSimplexFractalRidgedMulti(x, y, z);
                    default:
                        return singleSimplexFractalFBM(x, y, z);
                }
            case CELLULAR:
                switch (cellularReturnType) {
                    case CELL_VALUE:
                    case NOISE_LOOKUP:
                    case DISTANCE:
                        return singleCellular(x, y, z);
                    default:
                        return singleCellular2Edge(x, y, z);
                }
            case WHITE_NOISE:
                return getWhiteNoise(x, y, z);
            case CUBIC:
                return singleCubic(seed, x, y, z);
            case CUBIC_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleCubicFractalBillow(x, y, z);
                    case RIDGED_MULTI:
                        return singleCubicFractalRigidMulti(x, y, z);
                    default:
                        return singleCubicFractalFBM(x, y, z);
                }
            default:
                return singleSimplex(seed, x, y, z);
        }
    }

    /**
     * After being configured with the setters in this class, such as {@link #setNoiseType(int)},
     * {@link #setFrequency(float)}, {@link #setFractalOctaves(int)}, and {@link #setFractalType(int)}, among others,
     * you can call this method to get the particular variety of noise you specified, in 2D.
     * @param x
     * @param y
     * @return noise as a float from -1f to 1f
     */
    public float getConfiguredNoise(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (noiseType) {
            case VALUE:
                return singleValue(seed, x, y);
            case VALUE_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleValueFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleValueFractalRidgedMulti(x, y);
                    default:
                        return singleValueFractalFBM(x, y);
                }
            case PERLIN:
                return singlePerlin(seed, x, y);
            case PERLIN_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singlePerlinFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singlePerlinFractalRidgedMulti(x, y);
                    default:
                        return singlePerlinFractalFBM(x, y);
                }
            case SIMPLEX_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleSimplexFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleSimplexFractalRidgedMulti(x, y);
                    default:
                        return singleSimplexFractalFBM(x, y);
                }
            case CELLULAR:
                switch (cellularReturnType) {
                    case CELL_VALUE:
                    case NOISE_LOOKUP:
                    case DISTANCE:
                        return singleCellular(x, y);
                    default:
                        return singleCellular2Edge(x, y);
                }
            case WHITE_NOISE:
                return getWhiteNoise(x, y);
            case CUBIC:
                return singleCubic(seed, x, y);
            case CUBIC_FRACTAL:
                switch (fractalType) {
                    case BILLOW:
                        return singleCubicFractalBillow(x, y);
                    case RIDGED_MULTI:
                        return singleCubicFractalRigidMulti(x, y);
                    default:
                        return singleCubicFractalFBM(x, y);
                }
            default:
                return singleSimplex(seed, x, y);
        }
    }

    // White Noise

    private int FloatCast2Int(final float f) {
        final int i = NumberTools.floatToIntBits(f);
        return i ^ (i >> 16);
    }

    public float getWhiteNoise(float x, float y, float z, float w) {
        int xi = FloatCast2Int(x);
        int yi = FloatCast2Int(y);
        int zi = FloatCast2Int(z);
        int wi = FloatCast2Int(w);

        return valCoord4D(seed, xi, yi, zi, wi);
    }

    public float getWhiteNoise(float x, float y, float z) {
        int xi = FloatCast2Int(x);
        int yi = FloatCast2Int(y);
        int zi = FloatCast2Int(z);

        return valCoord3D(seed, xi, yi, zi);
    }

    public float getWhiteNoise(float x, float y) {
        int xi = FloatCast2Int(x);
        int yi = FloatCast2Int(y);

        return valCoord2D(seed, xi, yi);
    }

    public float getWhiteNoiseInt(int x, int y, int z, int w) {
        return valCoord4D(seed, x, y, z, w);
    }

    public float getWhiteNoiseInt(int x, int y, int z) {
        return valCoord3D(seed, x, y, z);
    }

    public float getWhiteNoiseInt(int x, int y) {
        return valCoord2D(seed, x, y);
    }

    // Value Noise
    public float getValueFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case BILLOW:
                return singleValueFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleValueFractalRidgedMulti(x, y, z);
            default:
                return singleValueFractalFBM(x, y, z);
        }
    }

    private float singleValueFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleValue(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleValue(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleValueFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleValue(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleValue(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleValueFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singleValue(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleValue(++seed, x, y, z))) * amp;
        }

        return sum;
    }

    public float getValue(float x, float y, float z) {
        return singleValue(seed, x * frequency, y * frequency, z * frequency);
    }

    private float singleValue(int seed, float x, float y, float z) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        float xs, ys, zs;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                zs = hermiteInterpolator(z - z0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                zs = quinticInterpolator(z - z0);
                break;
        }

        float xf00 = lerp(valCoord3D(seed, x0, y0, z0), valCoord3D(seed, x1, y0, z0), xs);
        float xf10 = lerp(valCoord3D(seed, x0, y1, z0), valCoord3D(seed, x1, y1, z0), xs);
        float xf01 = lerp(valCoord3D(seed, x0, y0, z1), valCoord3D(seed, x1, y0, z1), xs);
        float xf11 = lerp(valCoord3D(seed, x0, y1, z1), valCoord3D(seed, x1, y1, z1), xs);

        float yf0 = lerp(xf00, xf10, ys);
        float yf1 = lerp(xf01, xf11, ys);

        return lerp(yf0, yf1, zs);
    }

    public float getValueFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleValueFractalFBM(x, y);
            case BILLOW:
                return singleValueFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleValueFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    private float singleValueFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleValue(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleValue(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleValueFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleValue(seed, x, y)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            amp *= gain;
            sum += (Math.abs(singleValue(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleValueFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singleValue(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleValue(++seed, x, y))) * amp;
        }

        return sum;
    }

    public float getValue(float x, float y) {
        return singleValue(seed, x * frequency, y * frequency);
    }

    private float singleValue(int seed, float x, float y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float xs, ys;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                break;
        }

        float xf0 = lerp(valCoord2D(seed, x0, y0), valCoord2D(seed, x1, y0), xs);
        float xf1 = lerp(valCoord2D(seed, x0, y1), valCoord2D(seed, x1, y1), xs);

        return lerp(xf0, xf1, ys);
    }

    // Gradient Noise
    public float getPerlinFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singlePerlinFractalFBM(x, y, z);
            case BILLOW:
                return singlePerlinFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singlePerlinFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    private float singlePerlinFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singlePerlin(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singlePerlin(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    private float singlePerlinFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singlePerlin(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singlePerlin(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    private float singlePerlinFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singlePerlin(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singlePerlin(++seed, x, y, z))) * amp;
        }

        return sum;
    }

    public float getPerlin(float x, float y, float z) {
        return singlePerlin(seed, x * frequency, y * frequency, z * frequency);
    }

    private float singlePerlin(int seed, float x, float y, float z) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int z0 = fastFloor(z);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        float xs, ys, zs;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                zs = z - z0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                zs = hermiteInterpolator(z - z0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                zs = quinticInterpolator(z - z0);
                break;
        }

        final float xd0 = x - x0;
        final float yd0 = y - y0;
        final float zd0 = z - z0;
        final float xd1 = xd0 - 1;
        final float yd1 = yd0 - 1;
        final float zd1 = zd0 - 1;

        final float xf00 = lerp(gradCoord3D(seed, x0, y0, z0, xd0, yd0, zd0), gradCoord3D(seed, x1, y0, z0, xd1, yd0, zd0), xs);
        final float xf10 = lerp(gradCoord3D(seed, x0, y1, z0, xd0, yd1, zd0), gradCoord3D(seed, x1, y1, z0, xd1, yd1, zd0), xs);
        final float xf01 = lerp(gradCoord3D(seed, x0, y0, z1, xd0, yd0, zd1), gradCoord3D(seed, x1, y0, z1, xd1, yd0, zd1), xs);
        final float xf11 = lerp(gradCoord3D(seed, x0, y1, z1, xd0, yd1, zd1), gradCoord3D(seed, x1, y1, z1, xd1, yd1, zd1), xs);

        final float yf0 = lerp(xf00, xf10, ys);
        final float yf1 = lerp(xf01, xf11, ys);

        return lerp(yf0, yf1, zs);
    }

    public float getPerlinFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singlePerlinFractalFBM(x, y);
            case BILLOW:
                return singlePerlinFractalBillow(x, y);
            case RIDGED_MULTI:
                return singlePerlinFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    private float singlePerlinFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singlePerlin(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singlePerlin(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    private float singlePerlinFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singlePerlin(seed, x, y)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singlePerlin(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    private float singlePerlinFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singlePerlin(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singlePerlin(++seed, x, y))) * amp;
        }

        return sum;
    }

    public float getPerlin(float x, float y) {
        return singlePerlin(seed, x * frequency, y * frequency);
    }

    private float singlePerlin(int seed, float x, float y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float xs, ys;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = x - x0;
                ys = y - y0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(x - x0);
                ys = hermiteInterpolator(y - y0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(x - x0);
                ys = quinticInterpolator(y - y0);
                break;
        }

        float xd0 = x - x0;
        float yd0 = y - y0;
        float xd1 = xd0 - 1;
        float yd1 = yd0 - 1;

        float xf0 = lerp(gradCoord2D(seed, x0, y0, xd0, yd0), gradCoord2D(seed, x1, y0, xd1, yd0), xs);
        float xf1 = lerp(gradCoord2D(seed, x0, y1, xd0, yd1), gradCoord2D(seed, x1, y1, xd1, yd1), xs);

        return lerp(xf0, xf1, ys);
    }

    // Simplex Noise
    public float getSimplexFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y, z);
            case BILLOW:
                return singleSimplexFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y, z);
            default:
                return 0;
        }
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5) in 3D.
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves)
    {
        x *= 0.03125f;
        y *= 0.03125f;
        z *= 0.03125f;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y, z))) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5) in 3D.
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves, float frequency)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }
    /**
     * Generates layered simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (0.5) in 3D.
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @param frequency
     * @param lacunarity
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= 0.5f;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    /**
     * Generates layered simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (loosely, how much to emphasize lower-frequency octaves) in 3D.
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @param frequency
     * @param lacunarity
     * @param gain
     * @return noise as a float between -1f and 1f
     */
    public float layered3D(float x, float y, float z, int seed, int octaves, float frequency, float lacunarity, float gain)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    private float singleSimplexFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y, z);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleSimplexFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y, z)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(seed + i, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5).
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged3D(float x, float y, float z, int seed, int octaves)
    {
        x *= 0.03125f;
        y *= 0.03125f;
        z *= 0.03125f;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y, z))) * amp;
        }
        return sum;
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves, specified frequency, and the default
     * lacunarity (2) and gain (0.5).
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged3D(float x, float y, float z, int seed, int octaves, float frequency)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y, z))) * amp;
        }
        return sum;
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (0.5).
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged3D(float x, float y, float z, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y, z))) * amp;
        }
        return sum;
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (loosely, how much to emphasize lower-frequency octaves).
     * @param x
     * @param y
     * @param z
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged3D(float x, float y, float z, int seed, int octaves, float frequency, float lacunarity, float gain)
    {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y, z))) * amp;
        }
        return sum;
    }

    private float singleSimplexFractalRidgedMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singleSimplex(seed, x, y, z));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y, z))) * amp;
        }

        return sum;
    }

    public float getSimplex(float x, float y, float z) {
        return singleSimplex(seed, x * frequency, y * frequency, z * frequency);
    }

    private final static float F3 = (1f / 3f);
    private final static float G3 = (1f / 6f);
    private final static float G33 = -0.5f;

    private float singleSimplex(int seed, float x, float y, float z) {
        float t = (x + y + z) * F3;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);

        t = (i + j + k) * G3;
        float x0 = x - (i - t);
        float y0 = y - (j - t);
        float z0 = z - (k - t);

        int i1, j1, k1;
        int i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else // x0 < z0
            {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else // x0 < y0
        {
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else // x0 >= z0
            {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }

        float x1 = x0 - i1 + G3;
        float y1 = y0 - j1 + G3;
        float z1 = z0 - k1 + G3;
        float x2 = x0 - i2 + F3;
        float y2 = y0 - j2 + F3;
        float z2 = z0 - k2 + F3;
        float x3 = x0 + G33;
        float y3 = y0 + G33;
        float z3 = z0 + G33;

        float n = 0;

        t = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord3D(seed, i, j, k, x0, y0, z0);
        }

        t = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1);
        }

        t = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2);
        }

        t = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t > 0)  {
            t *= t;
            n += t * t * gradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3);
        }

        return 31.5f * n;
    }

    public float getSimplexFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y);
            case BILLOW:
                return singleSimplexFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y);
            default:
                return 0;
        }
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5) in 2D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves)
    {
        x *= 0.03125f;
        y *= 0.03125f;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y))) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5) in 2D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves, float frequency)
    {
        x *= frequency;
        y *= frequency;

        float sum = singleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum += singleSimplex(seed + i, x, y) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }
    /**
     * Generates layered simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (0.5) in D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;

        float sum = singleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= 0.5f;
            sum += singleSimplex(seed + i, x, y) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    /**
     * Generates layered simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (loosely, how much to emphasize lower-frequency octaves) in 2D.
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float layered2D(float x, float y, int seed, int octaves, float frequency, float lacunarity, float gain)
    {
        x *= frequency;
        y *= frequency;

        float sum = singleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y) * amp;
        }
        amp = gain;
        float ampFractal = 1;
        for (int i = 1; i < octaves; i++) {
            ampFractal += amp;
            amp *= gain;
        }
        return sum / ampFractal;
    }

    private float singleSimplexFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleSimplexFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5).
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged2D(float x, float y, int seed, int octaves)
    {
        x *= 0.03125f;
        y *= 0.03125f;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y))) * amp;
        }
        return sum;
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and default frequency (0.03125), lacunarity
     * (2) and gain (0.5).
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged2D(float x, float y, int seed, int octaves, float frequency)
    {
        x *= frequency;
        y *= frequency;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y))) * amp;
        }
        return sum;
    }
    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (0.5).
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged2D(float x, float y, int seed, int octaves, float frequency, float lacunarity)
    {
        x *= frequency;
        y *= frequency;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= 0.5f;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y))) * amp;
        }
        return sum;
    }

    /**
     * Generates ridged-multi simplex noise with the given amount of octaves and specified lacunarity (the amount of
     * frequency change between octaves) and gain (loosely, how much to emphasize lower-frequency octaves).
     * @param x
     * @param y
     * @param seed
     * @param octaves
     * @return noise as a float between -1f and 1f
     */
    public float ridged2D(float x, float y, int seed, int octaves, float frequency, float lacunarity, float gain)
    {
        x *= frequency;
        y *= frequency;

        float sum = 1 - Math.abs(singleSimplex(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y))) * amp;
        }
        return sum;
    }

    private float singleSimplexFractalRidgedMulti(float x, float y) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singleSimplex(seed, x, y));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleSimplex(++seed, x, y))) * amp;
        }

        return sum;
    }

    public float getSimplex(float x, float y) {
        return singleSimplex(seed, x * frequency, y * frequency);
    }

    private final static float F2 = 0.5f;
    private final static float G2 = 0.25f;

    private float singleSimplex(int seed, float x, float y) {
        float t = (x + y) * WhirlingNoise.F2f;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);

        t = (i + j) * WhirlingNoise.G2f;
        float X0 = i - t;
        float Y0 = j - t;

        float x0 = x - X0;
        float y0 = y - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        float x1 = x0 - i1 + WhirlingNoise.G2f;
        float y1 = y0 - j1 + WhirlingNoise.G2f;
        float x2 = x0 - 1 + WhirlingNoise.H2f;
        float y2 = y0 - 1 + WhirlingNoise.H2f;

        float n = 0f;

        t = 0.75f - x0 * x0 - y0 * y0;
        if (t >= 0) {
            t *= t;
            n += t * t * gradCoord2D(seed, i, j, x0, y0);
        }

        t = 0.75f - x1 * x1 - y1 * y1;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord2D(seed, i + i1, j + j1, x1, y1);
        }

        t = 0.75f - x2 * x2 - y2 * y2;
        if (t > 0)  {
            t *= t;
            n += t * t * gradCoord2D(seed, i + 1, j + 1, x2, y2);
        }

        return 9.125f * n;
    }

    public float getSimplex(float x, float y, float z, float w) {
        return singleSimplex(seed, x * frequency, y * frequency, z * frequency, w * frequency);
    }
    
    private final static float F4 = (float) ((2.23606797 - 1.0) / 4.0);
    private final static float G4 = (float) ((5.0 - 2.23606797) / 20.0);

    private float singleSimplex(int seed, float x, float y, float z, float w) {
        float n = 0f;
        float t = (x + y + z + w) * F4;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        t = (i + j + k + l) * G4;
        final float X0 = i - t;
        final float Y0 = j - t;
        final float Z0 = k - t;
        final float W0 = l - t;
        final float x0 = x - X0;
        final float y0 = y - Y0;
        final float z0 = z - Z0;
        final float w0 = w - W0;

        final int[] SIMPLEX_4D = WhirlingNoise.SIMPLEX_4D;
        final int c = (x0 > y0 ? 128 : 0) | (x0 > z0 ? 64 : 0) | (y0 > z0 ? 32 : 0) | (x0 > w0 ? 16 : 0) | (y0 > w0 ? 8 : 0) | (z0 > w0 ? 4 : 0);
        final int i1 = SIMPLEX_4D[c] >>> 2,
                j1 = SIMPLEX_4D[c | 1] >>> 2,
                k1 = SIMPLEX_4D[c | 2] >>> 2,
                l1 = SIMPLEX_4D[c | 3] >>> 2,
                i2 = SIMPLEX_4D[c] >>> 1 & 1,
                j2 = SIMPLEX_4D[c | 1] >>> 1 & 1,
                k2 = SIMPLEX_4D[c | 2] >>> 1 & 1,
                l2 = SIMPLEX_4D[c | 3] >>> 1 & 1,
                i3 = SIMPLEX_4D[c] & 1,
                j3 = SIMPLEX_4D[c | 1] & 1,
                k3 = SIMPLEX_4D[c | 2] & 1,
                l3 = SIMPLEX_4D[c | 3] & 1;

        final float x1 = x0 - i1 + G4;
        final float y1 = y0 - j1 + G4;
        final float z1 = z0 - k1 + G4;
        final float w1 = w0 - l1 + G4;
        final float x2 = x0 - i2 + 2 * G4;
        final float y2 = y0 - j2 + 2 * G4;
        final float z2 = z0 - k2 + 2 * G4;
        final float w2 = w0 - l2 + 2 * G4;
        final float x3 = x0 - i3 + 3 * G4;
        final float y3 = y0 - j3 + 3 * G4;
        final float z3 = z0 - k3 + 3 * G4;
        final float w3 = w0 - l3 + 3 * G4;
        final float x4 = x0 - 1 + 4 * G4;
        final float y4 = y0 - 1 + 4 * G4;
        final float z4 = z0 - 1 + 4 * G4;
        final float w4 = w0 - 1 + 4 * G4;

        t = 0.62f - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if (t > 0) {
            t *= t;
            n = t * t * gradCoord4D(seed, i, j, k, l, x0, y0, z0, w0);
        }
        t = 0.62f - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord4D(seed, i + i1, j + j1, k + k1, l + l1, x1, y1, z1, w1);
        }
        t = 0.62f - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord4D(seed, i + i2, j + j2, k + k2, l + l2, x2, y2, z2, w2);
        }
        t = 0.62f - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord4D(seed, i + i3, j + j3, k + k3, l + l3, x3, y3, z3, w3);
        }
        t = 0.62f - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t > 0) {
            t *= t;
            n += t * t * gradCoord4D(seed, i + 1, j + 1, k + 1, l + 1, x4, y4, z4, w4);
        }

        return 14.75f * n;
    }
    
    // Simplex Noise
    public float getSimplexFractal(float x, float y, float z, float w) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        
        switch (fractalType) {
            case FBM:
                return singleSimplexFractalFBM(x, y, z, w);
            case BILLOW:
                return singleSimplexFractalBillow(x, y, z, w);
            case RIDGED_MULTI:
                return singleSimplexFractalRidgedMulti(x, y, z, w);
            default:
                return 0;
        }
    }

    private float singleSimplexFractalFBM(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = singleSimplex(seed, x, y, z, w);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += singleSimplex(seed + i, x, y, z, w) * amp;
        }

        return sum * fractalBounding;
    }
    private float singleSimplexFractalRidgedMulti(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singleSimplex(seed, x, y, z, w));
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleSimplex(seed + i, x, y, z, w))) * amp;
        }

        return sum;
    }

    private float singleSimplexFractalBillow(float x, float y, float z, float w) {
        int seed = this.seed;
        float sum = Math.abs(singleSimplex(seed, x, y, z, w)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            w *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleSimplex(seed + i, x, y, z, w)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }
    
    // 6D Simplex

    protected static final float[] gradient6DLUT = {
            0.31733186658157f, 0.043599150809166f, -0.63578104939541f,
            0.60224147484783f, -0.061995657882187f, 0.35587048501823f,
            -0.54645425808647f, -0.75981513883963f, -0.035144342454363f,
            0.13137365402959f, 0.29650029456531f, 0.13289887942467f,
            0.72720729277573f, -0.0170513084554f, 0.10403853926717f, 0.57016794579524f,
            0.10006650294475f, -0.35348266879289f,
            0.0524867271859f, 0.16599786784909f, -0.49406271077513f, 0.51847470894887f,
            0.63927166664011f, -0.21933445140234f,
            -0.57224122530978f, -0.089985946187774f, 0.44829955643248f,
            0.53836681748476f, -0.051299333576026f, -0.41352093713992f,
            -0.35034584363296f, -0.37367516013323f, -0.52676009109159f,
            0.12379417201967f, 0.42566489477591f, 0.51345191723381f,
            0.40936909283115f, 0.33036021753157f, 0.46771483894695f, 0.15073372728805f,
            0.51541333179083f, -0.46491971651678f,
            -0.64339751231027f, -0.29341468636474f, -0.50841617762291f,
            -0.080659811936781f, -0.46873502824317f, -0.12345817650503f,
            0.46950904113222f, 0.41685007896275f, -0.33378791988356f,
            -0.39617029121348f, 0.54659770033168f, 0.19662896748851f,
            -0.49213884108338f, 0.50450587466563f, -0.0073247243900323f,
            0.57958418990163f, 0.39591449230465f, 0.10272980841415f,
            0.34572956497624f, 0.62770109739866f, 0.12165109216674f, 0.35267248385686f,
            0.34842369637704f, -0.47527514024373f,
            0.076282233884284f, 0.56461194794873f, -0.392426730607f,
            -0.20639693057567f, 0.33197602170266f, 0.60711436994661f,
            0.46792592791359f, -0.38434666353171f, -0.46719345820863f,
            -0.40169520060432f, -0.061343490026986f, 0.49993117813162f,
            -0.25398819915038f, -0.82255018555745f, 0.40372967512401f,
            0.21051604195389f, 0.020384827146984f, 0.22621006002887f,
            0.23269489013955f, -0.42234243708413f, -0.18886779174866f,
            0.44290933725703f, -0.40895242871151f, 0.60695810498111f,
            -0.13615585122038f, 0.26142849716038f, 0.68738606675966f,
            0.42914965171764f, 0.26332301994884f, 0.43256061294487f,
            0.06145597366231f, -0.25432792035414f, 0.65050463165568f,
            0.35622065678761f, -0.52670947710524f, -0.32259598080167f,
            -0.28027055313228f, 0.30275296247348f, 0.39083872911587f,
            0.17564171472763f, 0.25278203996272f, 0.76307625890429f,
            -0.62937098181034f, -0.24958587788613f, 0.11855057687171f,
            0.52714220921895f, 0.47759151204224f, -0.14687496867489f,
            0.68607574135496f, 0.28465344118508f, 0.57132493696771f, 0.11365238375433f,
            -0.32111327299854f, -0.076352560636185f,
            0.42669573845021f, -0.1643996530281f, -0.54881376863042f,
            -0.56551221465284f, 0.4027156095588f, -0.087880721039792f,
            -0.30211042220321f, -0.47278547361731f, 0.050137867251391f,
            0.46804387457884f, -0.39450159355792f, 0.55497099667426f,
            0.31255895138908f, 0.034478918459459f, -0.079232996020732f,
            0.39803160685016f, 0.82281399721198f, 0.24369695191021f,
            -0.5524321671417f, 0.49350231710234f, 0.52530668244467f, 0.253625789825f,
            0.26218499242504f, -0.20557247282514f,
            0.060763010271891f, -0.023938406391206f, 0.36557410300471f,
            0.55368747615095f, 0.25557899769702f, -0.70014279913759f,
            0.36398574324757f, 0.049110464042478f, -0.2428951164628f,
            -0.18733973495522f, 0.020130805835303f, 0.87784000694654f,
            -0.62385490124849f, 0.020947599003133f, -0.44548631925386f,
            -0.21069894502123f, -0.60559127508405f, 0.027809382425643f,
            0.51562840479369f, -0.27416131751628f, -0.14365580420426f,
            -0.46525735490594f, 0.16338488557607f, 0.62862302132303f,
            0.52085189275139f, 0.51359303425374f, 0.021844789421786f,
            0.53521775458267f, -0.23767218281397f, -0.34858599348565f,
            0.12263603513069f, 0.53912951801629f, 0.57550729534804f,
            -0.10335514143554f, 0.57524709075397f, 0.14662748040551f,
            0.40942178494947f, 0.17197663954561f, -0.025238012475873f,
            -0.20104824969996f, -0.60303014654018f, 0.63094779803243f,
            0.051685704973311f, 0.23577798459204f, -0.19154992327678f,
            -0.67743578708385f, -0.51070301615526f, 0.43047548181493f,
            0.21373839204543f, -0.44348268823586f, 0.34347986958921f,
            -0.49945694096162f, 0.45888698118478f, -0.42382317871053f,
            -0.60376535923059f, -0.065300874745824f, 0.49448067868339f,
            0.12358559784007f, 0.58623743735263f, -0.16656623971303f,
            0.44140930948322f, -0.41692548571374f, -0.23774988226818f,
            -0.27542786466885f, 0.39264397083621f, 0.58717642823542f,
            -0.67860697457746f, 0.2070991391515f, -0.12832398784247f,
            -0.58381216132288f, 0.24050209342748f, 0.2854077401022f,
            -0.021324501342617f, 0.0098658783730532f, 0.2694901128571f,
            0.42580554353158f, -0.82903198308789f, -0.24128534823695f,
            -0.20344882384938f, 0.51719618805529f, 0.24379623299129f,
            0.11303683173372f, -0.46058654895958f, -0.63777957124993f,
            0.15686479897897f, -0.67777169905813f, -0.04974608057712f,
            0.51313211803344f, 0.49928667286231f, -0.030863149692696f,
            0.53527130791104f, -0.50102597915466f, -0.60754472649714f,
            -0.25235098830686f, 0.13490559284448f, 0.10708155847142f,
            -0.20613512232544f, 0.39533044356843f, -0.34422306275706f,
            0.4792145528465f, -0.19178040223502f, -0.64521804411898f,
            0.3304779611047f, 0.49148538926455f, -0.30004348427342f, 0.33473309391851f,
            0.31079743137844f, 0.59208027276116f,
            -0.52688857216953f, 0.40250311061529f, 0.38833191043333f,
            0.50432308135853f, -0.33327489215794f, -0.21015252001231f,
            -0.30306420816123f, -0.34460825415019f, -0.26894228639121f,
            -0.58579646837355f, -0.51178483212848f, 0.33464319317466f,
            -0.20258582390514f, -0.29195675136034f, 0.11887973573086f,
            0.91211540292822f, 0.034118810787236f, -0.16269371903027f,
            0.61207678339522f, -0.21883722070929f, -0.23415725333464f,
            0.0041447691596985f, -0.34019274152454f, 0.6378827339521f,
            0.11272999861808f, -0.54780877011146f, -0.62497664375172f,
            -0.41373740141301f, 0.33306010353229f, 0.12039112788093f,
            0.24918468395037f, -0.068734287809286f, -0.42234580029763f,
            0.12235329631887f, -0.26545138767734f, 0.81815148205875f,
            0.32048708659406f, -0.40233908147851f, 0.24633289057781f,
            -0.37087758270512f, -0.55466799718133f, -0.47908728788262f,
            -0.33748729653627f, -0.45507986822699f, -0.50597645316527f,
            -0.2863701644881f, -0.5404199724601f, -0.22120318557996f,
            -0.23520314824941f, 0.82195093398991f, -0.22661283339659f,
            0.16382454786402f, -0.41400232366734f, -0.13959354720703f,
            -0.30495751902889f, -0.47964557116121f, -0.68490238495876f,
            -0.4324077675155f, -0.13521732523742f, -0.050887702629247f,
            -0.56629250538137f, 0.19768903044f, -0.080075220953828f,
            -0.29952637623112f, 0.095974426142512f, -0.73136356489112f,
            -0.21316607993139f, 0.47585902758173f, -0.49429850443227f,
            -0.24146904800157f, 0.45631329089651f, 0.46610972545109f,
            0.12647584748018f, -0.10203700758813f, 0.20801341293098f,
            0.66418891258418f, -0.65219775460192f, -0.2526141453282f,
            -0.69345279552921f, 0.30149980453822f, -0.46870940095961f,
            0.20092958919922f, -0.21817920622376f, 0.34721422759447f,
            -0.69001417476102f, 0.09722776919634f, -0.37852252163632f,
            -0.24995374433763f, 0.24829304775112f, 0.4970126640943f,
            -0.82278510972964f, 0.050748830242865f, -0.3934733016285f,
            0.00029980431140623f, -0.34677214869339f, -0.21301870187776f,
            -0.51821811089111f, -0.22147302694699f, 0.53524316281446f,
            0.12892242816244f, -0.5543955478928f, -0.26821451961648f,
            -0.21006612796354f, 0.26079212570498f, -0.021870637510645f,
            0.72402587064608f, -0.27651658712238f, 0.53544979218311f,
            -0.099744280251479f, -0.4534212871731f, 0.71954978543864f,
            -0.31082396323078f, -0.26933824624449f, 0.31233586755618f,
            -0.48121951222937f, -0.43051247772929f, -0.5038415181805f,
            0.12342710418307f, 0.037467829082858f, -0.55909965468017f,
            -0.51180831908824f, -0.079955485578946f, -0.53046702060975f,
            0.48748209854708f, 0.16148937559829f, -0.43191028009105f,
            -0.38131649706702f, 0.46242477534251f, 0.46416075424014f,
            -0.20634110277567f, -0.53778490132009f, 0.30582118902172f,
            0.6245043069106f, 0.14316692963071f, -0.1436103838143f, 0.27519251589203f,
            -0.60467865310212f, -0.35708047307373f,
            0.52425890739441f, -0.20390682829262f, -0.33609142609195f,
            0.51803372559413f, 0.28921536255925f, 0.46756035964091f,
            -0.4455164148456f, 0.31831805515328f, 0.24217750314789f, 0.49821219078654f,
            -0.47209418708575f, 0.41285649844363f,
            -0.015857310429397f, -0.45214512052441f, -0.14591363373753f,
            0.74070676188619f, 0.0098874230592725f, -0.47463489014478f,
            0.24260837156464f, 0.44639366601915f, 0.31528570191456f, 0.45334773303464f,
            -0.47964168123625f, -0.45484996397296f,
            0.47123463487178f, 0.64525048646519f, -0.064257637508608f,
            -0.18737730572971f, -0.11735335340515f, -0.55549853319118f,
            -0.025197229767488f, -0.257963271803f, 0.26277107860996f,
            -0.58236203161499f, -0.41893538667715f, 0.59086294196016f,
            -0.48940330017687f, 0.33728563842186f, -0.057634928591543f,
            0.44862021996899f, -0.40048256377746f, 0.53080564921806f,
            0.73350664260388f, -0.021482988114587f, 0.016568147533453f,
            0.0021905972927896f, 0.49384961731337f, 0.46619710394628f,
            -0.25151229880228f, -0.62009962583403f, -0.26948657433033f,
            0.31711936293198f, -0.35081923073755f, 0.50592112116981f,
            0.0094298597779172f, -0.35925999444899f, 0.47529205807388f,
            -0.26709475088579f, -0.53352146543694f, 0.53754630836074f,
            -0.5948549517534f, -0.53195924881292f, -0.094383768924555f,
            -0.41704491211939f, -0.41397531920841f, -0.09463944474724f,
            -0.74917126125127f, -0.24166385705367f, 0.22864554725283f,
            0.31721357549513f, 0.06066292638611f, -0.47303041351952f,
            -0.3300396030254f, -0.08758658200966f, -0.096726092930468f,
            -0.39607089556472f, 0.55566932028997f, 0.63906648027271f,
            -0.58933068378397f, -0.38176870540341f, 0.46748019640554f,
            -0.061358837959321f, 0.36268480315292f, -0.39127879224432f,
            -0.066556695042975f, -0.73863083674701f, -0.32153946998935f,
            0.57454599361106f, -0.090856896694743f, -0.09082394033963f,
            -0.36335404704287f, -0.41643677881158f, -0.57839830999334f,
            -0.030959887755637f, 0.5989792522053f, -0.016582566905843f,
            0.23126668855143f, 0.2107790785413f, -0.14272193312959f,
            -0.29232225134991f, -0.48451339172564f, -0.74934159314943f,
            0.48188197979627f, -0.040214759215399f, -0.15667971883369f,
            0.16054853668069f, -0.6083975436752f, -0.58796308779952f,
            0.31319356064062f, -0.19280657835646f, 0.76136690598738f,
            -0.084506239097717f, 0.4768786755523f, -0.22472488900872f,
            0.67504537519138f, 0.36920158913876f, 0.40321048682396f,
            0.034436041975613f, -0.29332731631919f, 0.39774172001359f,
            -0.1459159803857f, -0.59726183207777f, -0.036384224081948f,
            -0.65093487874945f, 0.39515711468056f, -0.20198429937477f,
            0.60092128630869f, 0.18110182176699f, 0.2579491954112f, -0.39594768022975f,
            0.15112959843347f, 0.59995268930018f,
            -0.42310244265976f, -0.26937197256148f, 0.074700012546319f,
            0.53119510349465f, 0.41614374632783f, 0.53618944036115f,
            0.0071605427687482f, -0.69599782505338f, -0.053138604739257f,
            -0.00054500262230378f, 0.69533871546989f, 0.1709263483943f,
            0.12447149375466f, 0.33265313001972f, 0.35070015349473f, 0.53879932284829f,
            0.37648083373421f, 0.56463759722353f,
            0.29540077719054f, 0.04954124873475f, -0.48345087234985f,
            0.72758494948264f, 0.070069102610626f, 0.377186640377f,
            0.4882414260383f, 0.45135801463006f, 0.48450857902353f, -0.26042407965644f,
            -0.4251358047458f, 0.2731053563007f,
            -0.49806371818291f, -0.4719759672029f, 0.029647087810764f,
            -0.13788472163255f, -0.45346141932978f, -0.5510470160674f,
            -0.5359511936033f, -0.53585470245895f, 0.1771036246335f, -0.4537763243703f,
            0.41838964069644f, 0.11527149720722f,
            -0.36846431808379f, -0.46533180802325f, 0.65800816763703f,
            -0.28691297783558f, 0.31521457275327f, 0.18178647457201f,
            -0.29243126901345f, -0.4352956525447f, -0.58895978125929f,
            -0.49649471729812f, 0.29271342931272f, 0.21433587621517f,
            0.056256690265475f, -0.50387710054371f, 0.48145041862725f,
            0.44723671964597f, -0.55771174894027f, -0.0092449146014199f,
            -0.40973125164006f, -0.73147173623276f, -0.094076302480945f,
            0.43033451471976f, 0.014334271843521f, -0.32066459724334f,
            0.26752725373294f, 0.50477344684769f, 0.065069516529324f,
            0.36001097578267f, 0.59393393889869f, -0.43247366096278f,
            0.48945720845334f, 0.6043315650632f, 0.12458128550608f, -0.48327805813458f,
            -0.25681943056744f, 0.28316179557217f,
            -0.45182760404001f, 0.21574002665039f, -0.31462623994251f,
            0.25279349500371f, 0.44865729380505f, -0.62058075048081f,
            0.44017304540101f, 0.43789555905674f, 0.58423563606269f, 0.41842994331139f,
            -0.26836655962348f, 0.16143005677844f,
            -0.67897032028819f, -0.32730885869255f, -0.0243997359109f,
            0.40649244381227f, 0.47711065295824f, -0.19596475712206f,
            0.57441588138131f, 0.09386994843744f, 0.28400793066375f, 0.59394229842661f,
            0.45349906020748f, 0.14881354725974f,
            -0.3393739967757f, -0.54929055652002f, 0.26209493900588f, 0.0733800373509f,
            0.56557076402003f, 0.43492125584075f,
            0.050007991188197f, 0.74652764513134f, -0.36432144611385f,
            -0.20993543754239f, -0.1352041047841f, 0.49508839805322f,
            -0.041332158875019f, -0.20655741061568f, 0.52511282214888f,
            0.047248635933477f, -0.6276121766011f, -0.5326844609727f,
            -0.1889491176448f, 0.05188976739355f, -0.45677123586268f,
            0.42884456750344f, 0.61612085530435f, -0.43526216197988f,
            -0.65873541163911f, -0.094770059351695f, 0.40844030815782f,
            0.35536013391048f, -0.16940065827957f, 0.48506226422661f,
            -0.45779281442862f, -0.46052673126242f, 0.34138050378631f,
            -0.54943270263121f, 0.37140594702643f, -0.14826175595089f,
            -0.069378715405383f, -0.14845488608058f, -0.73991837897813f,
            0.41519184526768f, -0.11098464009855f, -0.49088356499611f,
            0.46422563805447f, 0.46130716873201f, -0.44207791495441f,
            0.12050605352899f, 0.34969556083561f, -0.4893349322843f,
            -0.35482925073362f, 0.28146983672487f, -0.35356606227648f,
            -0.38774754218768f, 0.35979702647173f, -0.62454776976122f,
            -0.48343191508515f, 0.41492185792886f, -0.50175316406656f,
            0.21953122931153f, -0.54083165333237f, 0.041040952107647f,
            -0.51280508048852f, -0.54131124436697f, -0.0099287129207481f,
            0.23788701199175f, 0.4350333223576f, 0.44505087885649f,
            0.2253837335044f, -0.30117119745248f, 0.46587685049056f,
            -0.46672901001472f, -0.59182069765377f, 0.27086737661249f,
            0.43015756480475f, -0.067851118947538f, -0.26917802105288f,
            -0.57731860676632f, -0.53950120703807f, -0.33696522367557f,
            0.20858352742161f, 0.63695057987625f, 0.49453142202915f,
            -0.046235371593379f, -0.54436247241885f, -0.088075720520231f,
            -0.35626464703623f, 0.067539543974725f, -0.18142793486226f,
            -0.49044207117167f, 0.5542388249925f, 0.53654796190017f,
            0.52238539932434f, 0.55175875223621f, 0.29070268774296f,
            -0.14119026819648f, -0.55841587206055f, -0.080029639759127f,
            -0.025988002903175f, 0.46612949273683f, -0.56880970348453f,
            -0.44824563336003f, -0.030000490931808f, 0.50663523727173f,
            0.047284583258099f, -0.26595723160738f, 0.21032033434131f,
            0.52986834914146f, -0.52245334572957f, -0.5736534757312f,
            -0.31924244568277f, -0.13888420092891f, 0.30725800370737f,
            0.49792332552544f, 0.61035592292817f, -0.40487771982263f,
            0.038758575627018f, -0.53813545398707f, -0.56167256912901f,
            0.46815373895572f, -0.14142713486975f, 0.39276248966752f,
            -0.19936871608885f, 0.12488860648831f, -0.62990029833727f,
            -0.29296146144627f, 0.49734531468753f, 0.46335923993672f,
            -0.078826705546604f, -0.15548800857414f, 0.57456768467721f,
            0.5558854465212f, -0.56893054194692f, -0.082408823513622f,
            0.11678856295109f, 0.53358760166951f, 0.49302489382249f,
            -0.53981846952046f, -0.237913367643f, -0.33251226509871f,
            0.39126928439834f, -0.39416116630681f, -0.35778844984527f,
            -0.39395609960567f, 0.50270356681194f, -0.39448759513757f,
            -0.17961290695406f, 0.34239532682819f, -0.21870225043453f,
            -0.23322835296688f, 0.75997835134209f, 0.41317237364121f,
            0.29699501400111f, 0.17195435585404f, -0.34903627841034f,
            -0.31751884057854f, -0.59661546358767f, 0.55102732418683f,
            -0.2237291316445f, -0.51254305965518f, -0.31277318571798f,
            0.54270199705442f, -0.34885011313806f, 0.41616819064585f,
            0.53534023676892f, 0.45905986582643f, -0.20308675275303f,
            0.019523641323632f, 0.3378580580099f, 0.58898336258938f,
            -0.045038463119119f, -0.52553334288797f, -0.6098545897634f,
            0.46226027841702f, -0.36069029000651f, 0.077984430434637f,
            -0.40129033029845f, 0.39526722066586f, -0.20379584931963f,
            0.45466492237669f, 0.46504795737483f, -0.46712669863522f,
            -0.43845831945339f, -0.59284534057943f, 0.050241908216277f,
            -0.36494839821973f, 0.32363879325018f, 0.46458051299488f,
            -0.46057360356064f, -0.34584626825548f, -0.12264748451482f,
            0.48835437094478f, 0.21102526990984f, 0.60843919401837f,
            -0.086047549693024f, -0.16981605114589f, -0.37222833669973f,
            0.45158609930017f, -0.55710254634126f, 0.55759406480139f,
            0.54697451263099f, -0.45070837355303f, 0.032962522247893f,
            -0.48584332140086f, -0.28055687213837f, 0.42642516953676f,
            0.34061925303691f, 0.38443007758012f, 0.61614808332652f,
            -0.55774172327958f, -0.075660378162998f, 0.19938218730551f,
            0.30626924920956f, -0.057939049897675f, -0.10461119704504f,
            -0.4395638756485f, -0.57307193269415f, 0.60849886616281f,
            -0.52519951444608f, -0.42567534157254f, -0.19896500097138f,
            0.48819483593271f, 0.12539008064447f, 0.49932157157064f,
            -0.10173361116951f, -0.07873850987854f, 0.3713554090283f,
            0.65889542748449f, 0.63411890875068f, 0.096414235519521f,
            0.60342393773609f, 0.057617370697663f, 0.35558841250938f,
            0.20766418929404f, 0.030670189501999f, -0.67974377143949f,
            -0.071971052874019f, -0.44567383014704f, 0.65917594080871f,
            0.44113802003588f, -0.29627117199757f, 0.28160739274962f,
            0.38284479693596f, 0.43552320173998f, -0.4282368470258f,
            -0.54809258921772f, -0.27202273485667f, 0.32551612927831f,
            -0.74755699288716f, -0.20979308948438f, 0.19268299390085f,
            0.27864013929953f, -0.39085278833717f, 0.36001727246301f,
            -0.64575536737195f, 0.59253747557756f, 0.040885512266333f,
            -0.20167391777406f, -0.43481684011627f, -0.02212841779644f,
            0.45874103754271f, -0.0066587566394561f, -0.30494054091993f,
            0.52731059172348f, -0.64443887148677f, 0.056264275617853f,
            0.61573773369959f, -0.00074622703454316f, 0.25455659350429f,
            0.30670278147618f, -0.18573195942296f, 0.65383825999316f,
            -0.089919562456316f, -0.28968403215216f, -0.60618287937171f,
            0.53370861364121f, 0.37921556323246f, -0.33450055738044f,
            -0.47481167613763f, 0.3899274103573f, -0.1047963185367f, 0.45545456567005f,
            0.12142073778317f, 0.62397625076847f,
            0.59154225785278f, -0.10812441303593f, -0.4685834521013f,
            -0.36007270807588f, -0.1012374701199f, 0.52812407295968f,
            -0.01292122984647f, -0.23607532114711f, -0.57680411110671f,
            -0.44955815301222f, -0.31913443306122f, -0.55448100298376f,
            0.54231398466289f, -0.31845386154668f, -0.38636423612049f,
            0.22187979539931f, -0.6346425853783f, -0.056599490898788f,
            -0.41950690366157f, -0.4578028963184f, 0.31139813874057f,
            0.39787962066193f, -0.20885901240181f, 0.56172180435883f,
            -0.031404881097728f, 0.56267475273157f, -0.5556815383811f,
            0.33075363850824f, 0.39071115867626f, 0.3340294973255f,
            -0.51485161085589f, -0.34037011091125f, -0.46826090820473f,
            -0.60086679836276f, -0.075069409610657f, 0.18202033570633f,
            -0.49669644859095f, 0.13236483793072f, 0.53440735955877f, 0.4720120049858f,
            -0.05992551666341f, -0.47306929861073f,
            -0.32796852486185f, 0.65593302097807f, 0.20800030327303f,
            -0.38965914824176f, -0.51564565153044f, -0.034636725857177f,
            -0.30473794783797f, 0.12584230588041f, 0.63911213518179f,
            0.11269477188219f, 0.62944339013855f, 0.27191006392352f,
            -0.53642197294029f, 0.50742224701512f, -0.22907820767928f,
            0.47022559371179f, -0.1914125650624f, 0.38019261684316f,
            -0.28865425091309f, 0.76169672032907f, -0.36166127667225f,
            -0.30555403321368f, -0.12541657537884f, -0.31081403770203f,
            0.0025978417989835f, 0.3737146483793f, -0.3151511957077f,
            0.62032810853005f, 0.60524642517936f, -0.09939888944988f,
            -0.40019833530022f, 0.15931480693456f, -0.61653030345628f,
            -0.49479441153976f, -0.021517911098538f, -0.43481713333933f,
            -0.26445143166732f, -0.48401155081335f, 0.27737058096082f,
            -0.12537486208624f, -0.46956235249512f, 0.61859207953377f,
            -0.49776294425122f, 0.6509513246149f, -0.20147785800704f,
            0.26022926925791f, 0.39526195830317f, -0.25288299425858f,
            0.20792543895216f, 0.6725599557329f, 0.013296712014115f,
            0.069082404776847f, -0.37233547685047f, 0.60070560947898f,
            -0.60329265885108f, 0.40708027238668f, -0.17229997007444f,
            -0.52997954496878f, 0.22211745651394f, -0.33229784433365f,
            0.61826884506104f, -0.62582169643111f, 0.33820439950773f,
            0.23870919720066f, -0.20670655096227f, -0.10953969425599f,
            -0.63678168786213f, -0.51101649337563f, -0.19131817442969f,
            -0.49493417544846f, -0.22614515287593f, 0.025828539221376f,
            0.7068462559507f, 0.072932806612059f, -0.30827034359477f,
            -0.52659704221432f, -0.33954839093364f, 0.086145323573817f,
            -0.52429050496975f, 0.39091424683727f, 0.52819210715237f,
            -0.16569162349745f, 0.447191673089f, 0.25667977984796f,
            0.85033978527922f, -0.37311666188152f, -0.031585518143925f,
            -0.063546921071094f, -0.35026506762952f, 0.099923633151172f,
            -0.43149574251927f, 0.16017753208259f, -0.36624037246965f,
            0.49372029676385f, -0.60067103922455f, 0.2223896202103f,
            -0.43599537393092f, -0.360658355506f, -0.42475053011196f,
            -0.52301759011739f, 0.039454536357949f, 0.47362064109658f,
            -0.35793170214797f, -0.43917817788312f, -0.49072242572643f,
            -0.32880277826743f, -0.38509560837703f, -0.42636724894184f,
            -0.043679644403255f, 0.74697226557232f, -0.40732954428872f,
            -0.48088968590275f, 0.18029290312902f, -0.10220931735307f,
            -0.058902573502295f, 0.0082595236590186f, 0.7136596141971f,
            -0.53043791172483f, 0.22906331492979f, 0.39155822265168f,
            0.43459649233879f, 0.18964470832196f, 0.15217427204218f, 0.59694624534505f,
            0.053786588105393f, 0.62671041756872f,
            -0.48833575031057f, 0.068909881680922f, 0.60168404074737f,
            -0.055455043023162f, -0.62426261497771f, -0.044461939113733f,
            -0.71822145541427f, 0.054494951105527f, 0.25733756171599f,
            -0.42706881935297f, -0.44024663347316f, 0.19687748949208f,
            0.4723221071836f, 0.63009683957253f, 0.2166256995021f, 0.31063720960745f,
            0.079455887335627f, 0.47974409023622f,
            -0.39506538843406f, 0.42517729990346f, 0.29375773990216f,
            0.044503633424429f, -0.46173213926286f, 0.60139575234582f,
            -0.40354126620316f, 0.41304136826673f, -0.29533980868045f,
            -0.45300699221804f, 0.23702354154238f, -0.56385297528377f,
            -0.62315380378984f, -0.42397903326965f, 0.53044082394843f,
            0.37874432092957f, 0.054922713129263f, 0.063952196248596f,
            0.41959045692314f, -0.83420441875842f, -0.25505372502578f,
            0.25012310515014f, 0.010974237503127f, 0.017675743681809f,
            -0.25231575134089f, -0.17034034508503f, -0.0022254428444259f,
            -0.4967771056787f, 0.43184899693064f, -0.68850194407078f,
            -0.1852812882862f, -0.48330898597592f, 0.13528868642679f,
            0.15202104844417f, 0.57661281495368f, -0.59848767913131f,
            0.64287473226568f, -0.30923674494923f, 0.22234318117192f,
            0.099248962994541f, 0.64370450011427f, 0.13206961744112f,
            -0.49018899717866f, 0.68654120859156f, -0.27238863334662f,
            -0.085832423495263f, 0.44161945604453f, 0.10856057983467f,
            0.48795432482822f, 0.42184193883513f, -0.43797315744756f,
            0.35186997012044f, -0.46483432791096f, 0.22857392808385f,
            0.52970834834669f, -0.50684486922008f, -0.39782161731912f,
            -0.3932709335414f, -0.34863027587322f, 0.16748196501934f,
            -0.46048505533f, -0.3887126918161f, -0.68287320410729f, -0.18448530888361f,
            -0.25358256326157f, 0.26870280714361f,
            0.6889557358588f, -0.3101022706485f, -0.35882194962822f, 0.30088738418801f,
            -0.039139540883101f, -0.45646277242166f,
            -0.21954767479275f, 0.40838837410593f, 0.23284186868997f,
            0.30349649888064f, 0.57233263099925f, 0.55778817953937f,
            0.57731035290905f, 0.091218309942656f, 0.70670016667131f,
            0.016358033634041f, 0.3939245235472f, -0.059352634867484f,
            0.50055570130024f, -0.021749790970703f, 0.56767851040093f,
            0.50580176326624f, 0.34691320957643f, 0.22478399991032f,
            -0.37901911159632f, 0.53804099887537f, -0.46780195460858f,
            0.51497346779204f, -0.27981005467588f, 0.067278440906787f,
            0.67241900483514f, 0.074099582737f, 0.43138117954806f, 0.054567519697911f,
            -0.37927768894619f, 0.45764946429346f,
            0.14529189179172f, -0.23854982910384f, 0.45401647091062f,
            0.25466539906731f, 0.46182069803887f, -0.66160446396375f,
            -0.15570980059397f, -0.38476787034627f, 0.37322840954917f,
            -0.43977613626294f, -0.61243005550684f, -0.34631643815896f,
            -0.19590302894013f, 0.42065974653653f, 0.43447548638809f,
            -0.10575548452794f, 0.70439951675651f, -0.29754920754254f,
            -0.13558865796725f, 0.1427073453776f, 0.49647494823192f,
            -0.65533234019218f, -0.11714854214663f, 0.5211321311867f,
            -0.6228374766114f, 0.20812698103217f, -0.16205154548883f,
            0.20384566967497f, -0.59321895467652f, 0.38604941246779f,
            0.44487837128099f, -0.37224943035393f, -0.22188447638327f,
            0.48921538939858f, 0.41432418029434f, -0.45087099253189f,
            0.66422841315008f, 0.21517761068003f, 0.094012579794123f,
            -0.4358159040875f, 0.22245680154647f, -0.51404116085847f,
            -0.11369362736032f, 0.32284689991698f, -0.38818285117689f,
            0.49680024166881f, 0.047684866166158f, -0.69503480904222f,
            -0.5137200731924f, -0.50673230867252f, 0.32715252974108f,
            -0.26799714004956f, -0.47616510509846f, 0.27153195326233f,
            -0.47315177716491f, -0.45711495983609f, -0.31178280842352f,
            -0.51697763052226f, -0.14302372043059f, -0.42689944315384f,
            -0.050442035795027f, 0.23609184251469f, 0.38634880236106f,
            0.56012774305243f, 0.38963669840218f, -0.57174382424149f,
            -0.15472134925391f, -0.15333579424307f, -0.14189768300467f,
            0.032279269476252f, -0.66054298438621f, -0.70360180527557f,
            -0.10345191679557f, -0.30503725808375f, 0.31038263802383f,
            0.36878846502877f, -0.76824774853417f, 0.2714830658427f,
            -0.060212868606223f, -0.4172755444983f, 0.39199300681258f,
            -0.44040104260082f, 0.24955102139032f, -0.64215903203727f,
            0.25443195353315f, -0.013789583113498f, 0.44365000614699f,
            0.53296203342425f, -0.55057750350733f, -0.38867053403178f,
            -0.36068564301268f, -0.65616661625162f, -0.48495997865466f,
            0.24088316031012f, -0.18080297655217f, -0.33682435258394f,
            -0.53824550487673f, -0.096728907851005f, -0.5208619866167f,
            0.33195321221408f, -0.032263947064791f, 0.56427315050798f,
            0.40151657866643f, -0.44825725748635f, -0.54910020122855f,
            -0.095936272447708f, 0.5719563905078f, 0.00097783623607218f,
            0.21961099467771f, 0.62823723408945f, -0.010045934028323f,
            -0.6610564872634f, -0.17161595423903f, -0.30089924032373f,
            0.27961471530636f, 0.054523395513076f, 0.61485903249347f,
            0.11958885677663f, -0.61032561244673f, -0.39241856813031f,
            -0.30223065341134f, -0.23605925177166f, -0.09697276975263f,
            -0.46458104180761f, -0.37853464945647f, 0.69599203908657f,
            0.0023635513043496f, 0.62702100484886f, 0.49658954056984f,
            -0.20369645124455f, -0.56457560315907f, 0.00021299797811461f,
            -0.64198493892962f, 0.59676262320476f, 0.46274573284143f,
            0.088421912306785f, 0.098029994490406f, -0.012953072012707f,
            -0.053965435026011f, 0.13439533803278f, -0.33103493780685f,
            0.55991756423782f, -0.58127599631056f, -0.46696041830103f,
            -0.43965993689353f, 0.07544961763381f, 0.1509639518808f,
            -0.38868406689028f, -0.0033436054452783f, -0.79191533434483f,
            -0.21743914630025f, -0.32019630124298f, -0.56067107727615f,
            0.027284914419519f, -0.49444926389798f, -0.53908992599417f,
            -0.36492599248168f, 0.52529904803377f, 0.18002253442693f,
            0.14829474115897f, 0.17212619314998f, -0.71194315827942f,
            0.0051876209353066f, 0.50490293404098f, 0.24361032552454f,
            0.13688117617809f, -0.61381291176911f, -0.5386997104485f,
            0.66421180843392f, 0.21833854629637f, -0.087909936660014f,
            0.15624552502148f, -0.68780724971724f, 0.077015056461268f,
            0.52710630558705f, -0.42143671471468f, -0.069964559463205f,
            -0.24196341534187f, -0.68814841622245f, 0.08695091377684f,
            0.62392249806692f, -0.23663281560035f, -0.59058622185178f,
            0.22685863859977f, -0.36683948058558f, -0.14105848121323f,
            0.18069852004855f, -0.083828559172887f, 0.66240167877879f,
            0.16722813432165f, -0.25503640214793f, -0.65462662498637f,
            -0.37112528006203f, 0.43100319401562f, -0.11342774633614f,
            0.14418808646988f, 0.5753326931164f, 0.55842502411684f,
            0.55378724068611f, 0.21098160548047f, -0.3224976646632f, 0.31268307369255f,
            -0.37624695517597f, -0.55269271266764f,
            0.2601465870231f, 0.56373458886982f, -0.21638357910201f, 0.41216916619413f,
            -0.25078072187299f, -0.57873208070982f,
            0.11217864148346f, 0.54196554704815f, -0.31989128683717f,
            0.54691221598945f, 0.24062434044524f, 0.48409277788476f,
            0.087564423746579f, -0.12083081671284f, 0.69931172084498f,
            0.35220575672909f, 0.28770484569954f, -0.53091668762919f,
            0.3395702120398f, 0.042520943289575f, -0.30935928261896f,
            0.61022210846475f, 0.54650816974112f, 0.34079124619266f,
            0.32746112891934f, 0.32095220193351f, -0.61142534799442f,
            0.32197324480666f, -0.38236071343678f, 0.40749411210419f,
            0.58741915356593f, -0.30916030490652f, -0.57642977381104f,
            -0.038846190358607f, 0.047926713761208f, -0.4725265742377f,
            0.026224389898652f, 0.031768907187292f, -0.12510902263321f,
            0.36102734397001f, -0.72217212865059f, 0.57513252722531f,
            -0.27510374152496f, -0.5153402145828f, 0.025774022629799f,
            0.59201067073603f, 0.40728366085253f, -0.37645913420642f,
            -0.29983338495183f, -0.61017291361195f, -0.18551919513643f,
            0.50515945610161f, 0.18206593801497f, -0.46372136367049f,
            -0.64290893575119f, -0.34887011406157f, -0.55318606770362f,
            -0.21230198963112f, -0.19828983785672f, 0.2730419816548f,
            -0.32778879906348f, -0.094317293167129f, 0.57811170538439f,
            0.54346692190204f, 0.17699503497579f, -0.47197676839855f,
            -0.075738705663962f, 0.53381750682665f, -0.13406342524856f,
            0.71765386263773f, 0.34271060834977f, 0.24259408122628f,
            -0.30574273227855f, 0.17419449782542f, -0.78861555508124f,
            0.43305678368813f, 0.064853328282818f, 0.25003806266734f,
            0.4397035983709f, -0.51651518914239f, -0.3972346186176f,
            -0.34513492086703f, 0.32129829777342f, -0.39965829527563f,
            -0.25184899643619f, -0.35937572373004f, 0.15273239148905f,
            -0.51640931868766f, 0.4218715745627f, -0.58261460582976f,
            -0.57396000790758f, 0.1912786199605f, 0.45995634753032f,
            -0.43664716984512f, 0.4601630113166f, 0.14146310231856f,
            0.11500068018889f, 0.05112652754666f, -0.25672855859366f,
            -0.54715738035577f, 0.67669928552409f, 0.40118355777989f,
            -0.45252668004418f, -0.40809988524453f, -0.064931545867856f,
            0.19116562077283f, 0.76523014995576f, 0.048337406798767f,
            -0.080075651760374f, 0.75305314115418f, 0.34797424409913f,
            0.29104493928016f, 0.0040185919664457f, -0.46977598520425f,
            -0.3890257668276f, 0.49100041230416f, -0.17812126809985f,
            -0.43787557151231f, -0.46923187878333f, 0.40489108352503f,
            0.37433236324043f, -0.29441766760791f, -0.066285137006724f,
            0.33217472508825f, 0.73917165688328f, 0.33479099915638f,
            -0.02973230696179f, -0.51371026289118f, 0.34133522703692f,
            -0.41361792362786f, -0.51561746819514f, -0.4263412462482f,
            0.51057171220039f, -0.23740201245544f, 0.26673587003088f, 0.5521767379032f,
            0.16849318602455f, 0.52774964064755f,
    };

    private final float[] mShared = {0, 0, 0, 0, 0, 0}, cellDistShared = {0, 0, 0, 0, 0, 0};
    private final int[] distOrderShared = {0, 0, 0, 0, 0, 0}, intLocShared = {0, 0, 0, 0, 0, 0};
    
    private static final float
            F6 = (float) ((Math.sqrt(7.0) - 1.0) / 6.0),
            G6 = F6 / (1f + 6f * F6),
            LIMIT6 = 0.75f;


    private float singleSimplex(int seed, float x, float y, float z, float w, float u, float v) {
        final float s = (x + y + z + w + u + v) * F6;

        final int skewX = fastFloor(x + s), skewY = fastFloor(y + s), skewZ = fastFloor(z + s),
                skewW = fastFloor(w + s), skewU = fastFloor(u + s), skewV = fastFloor(v + s);
        final float[] m = mShared, cellDist = cellDistShared, gradient6DLUT = FastNoise.gradient6DLUT;
        final int[] distOrder = distOrderShared,
                intLoc = intLocShared;
        intLoc[0] = skewX;
        intLoc[1] = skewY;
        intLoc[2] = skewZ;
        intLoc[3] = skewW;
        intLoc[4] = skewU;
        intLoc[5] = skewV;

        final float unskew = (skewX + skewY + skewZ + skewW + skewU + skewV) * G6;
        cellDist[0] = x - skewX + unskew;
        cellDist[1] = y - skewY + unskew;
        cellDist[2] = z - skewZ + unskew;
        cellDist[3] = w - skewW + unskew;
        cellDist[4] = u - skewU + unskew;
        cellDist[5] = v - skewV + unskew;

        int o0 = (cellDist[0]<cellDist[1]?1:0)+(cellDist[0]<cellDist[2]?1:0)+(cellDist[0]<cellDist[3]?1:0)+(cellDist[0]<cellDist[4]?1:0)+(cellDist[0]<cellDist[5]?1:0);
        int o1 = (cellDist[1]<=cellDist[0]?1:0)+(cellDist[1]<cellDist[2]?1:0)+(cellDist[1]<cellDist[3]?1:0)+(cellDist[1]<cellDist[4]?1:0)+(cellDist[1]<cellDist[5]?1:0);
        int o2 = (cellDist[2]<=cellDist[0]?1:0)+(cellDist[2]<=cellDist[1]?1:0)+(cellDist[2]<cellDist[3]?1:0)+(cellDist[2]<cellDist[4]?1:0)+(cellDist[2]<cellDist[5]?1:0);
        int o3 = (cellDist[3]<=cellDist[0]?1:0)+(cellDist[3]<=cellDist[1]?1:0)+(cellDist[3]<=cellDist[2]?1:0)+(cellDist[3]<cellDist[4]?1:0)+(cellDist[3]<cellDist[5]?1:0);
        int o4 = (cellDist[4]<=cellDist[0]?1:0)+(cellDist[4]<=cellDist[1]?1:0)+(cellDist[4]<=cellDist[2]?1:0)+(cellDist[4]<=cellDist[3]?1:0)+(cellDist[4]<cellDist[5]?1:0);
        int o5 = 15-(o0+o1+o2+o3+o4);

        distOrder[o0]=0;
        distOrder[o1]=1;
        distOrder[o2]=2;
        distOrder[o3]=3;
        distOrder[o4]=4;
        distOrder[o5]=5;

        float n = 0;
        float skewOffset = 0;

        for (int c = -1; c < 6; c++) {
            if (c != -1) intLoc[distOrder[c]]++;

            m[0] = cellDist[0] - (intLoc[0] - skewX) + skewOffset;
            m[1] = cellDist[1] - (intLoc[1] - skewY) + skewOffset;
            m[2] = cellDist[2] - (intLoc[2] - skewZ) + skewOffset;
            m[3] = cellDist[3] - (intLoc[3] - skewW) + skewOffset;
            m[4] = cellDist[4] - (intLoc[4] - skewU) + skewOffset;
            m[5] = cellDist[5] - (intLoc[5] - skewV) + skewOffset;

            float tc = LIMIT6;

            for (int d = 0; d < 6; d++) {
                tc -= m[d] * m[d];
            }

            if (tc > 0) {
                final int h = hash256(intLoc[0], intLoc[1], intLoc[2], intLoc[3],
                        intLoc[4], intLoc[5], seed) * 6;
                double gr = 0;
                for (int d = 0; d < 6; d++) {
                    gr += gradient6DLUT[h + d] * m[d];
                }

                n += gr * tc * tc * tc * tc;
            }
            skewOffset += G6;
        }
        return 13.5f * n;

    }

    // Cubic Noise
    public float getCubicFractal(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (fractalType) {
            case FBM:
                return singleCubicFractalFBM(x, y, z);
            case BILLOW:
                return singleCubicFractalBillow(x, y, z);
            case RIDGED_MULTI:
                return singleCubicFractalRigidMulti(x, y, z);
            default:
                return 0;
        }
    }

    private float singleCubicFractalFBM(float x, float y, float z) {
        int seed = this.seed;
        float sum = singleCubic(seed, x, y, z);
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += singleCubic(++seed, x, y, z) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleCubicFractalBillow(float x, float y, float z) {
        int seed = this.seed;
        float sum = Math.abs(singleCubic(seed, x, y, z)) * 2 - 1;
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleCubic(++seed, x, y, z)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleCubicFractalRigidMulti(float x, float y, float z) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singleCubic(seed, x, y, z));
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleCubic(++seed, x, y, z))) * amp;
        }

        return sum;
    }

    public float getCubic(float x, float y, float z) {
        return singleCubic(seed, x * frequency, y * frequency, z * frequency);
    }

    private final static float CUBIC_3D_BOUNDING = 1 / (float) (1.5 * 1.5 * 1.5);

    private float singleCubic(int seed, float x, float y, float z) {
        int x1 = fastFloor(x);
        int y1 = fastFloor(y);
        int z1 = fastFloor(z);

        int x0 = x1 - 1;
        int y0 = y1 - 1;
        int z0 = z1 - 1;
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;
        int x3 = x1 + 2;
        int y3 = y1 + 2;
        int z3 = z1 + 2;

        float xs = x - (float) x1;
        float ys = y - (float) y1;
        float zs = z - (float) z1;

        return cubicLerp(
                cubicLerp(
                        cubicLerp(valCoord3D(seed, x0, y0, z0), valCoord3D(seed, x1, y0, z0), valCoord3D(seed, x2, y0, z0), valCoord3D(seed, x3, y0, z0), xs),
                        cubicLerp(valCoord3D(seed, x0, y1, z0), valCoord3D(seed, x1, y1, z0), valCoord3D(seed, x2, y1, z0), valCoord3D(seed, x3, y1, z0), xs),
                        cubicLerp(valCoord3D(seed, x0, y2, z0), valCoord3D(seed, x1, y2, z0), valCoord3D(seed, x2, y2, z0), valCoord3D(seed, x3, y2, z0), xs),
                        cubicLerp(valCoord3D(seed, x0, y3, z0), valCoord3D(seed, x1, y3, z0), valCoord3D(seed, x2, y3, z0), valCoord3D(seed, x3, y3, z0), xs),
                        ys),
                cubicLerp(
                        cubicLerp(valCoord3D(seed, x0, y0, z1), valCoord3D(seed, x1, y0, z1), valCoord3D(seed, x2, y0, z1), valCoord3D(seed, x3, y0, z1), xs),
                        cubicLerp(valCoord3D(seed, x0, y1, z1), valCoord3D(seed, x1, y1, z1), valCoord3D(seed, x2, y1, z1), valCoord3D(seed, x3, y1, z1), xs),
                        cubicLerp(valCoord3D(seed, x0, y2, z1), valCoord3D(seed, x1, y2, z1), valCoord3D(seed, x2, y2, z1), valCoord3D(seed, x3, y2, z1), xs),
                        cubicLerp(valCoord3D(seed, x0, y3, z1), valCoord3D(seed, x1, y3, z1), valCoord3D(seed, x2, y3, z1), valCoord3D(seed, x3, y3, z1), xs),
                        ys),
                cubicLerp(
                        cubicLerp(valCoord3D(seed, x0, y0, z2), valCoord3D(seed, x1, y0, z2), valCoord3D(seed, x2, y0, z2), valCoord3D(seed, x3, y0, z2), xs),
                        cubicLerp(valCoord3D(seed, x0, y1, z2), valCoord3D(seed, x1, y1, z2), valCoord3D(seed, x2, y1, z2), valCoord3D(seed, x3, y1, z2), xs),
                        cubicLerp(valCoord3D(seed, x0, y2, z2), valCoord3D(seed, x1, y2, z2), valCoord3D(seed, x2, y2, z2), valCoord3D(seed, x3, y2, z2), xs),
                        cubicLerp(valCoord3D(seed, x0, y3, z2), valCoord3D(seed, x1, y3, z2), valCoord3D(seed, x2, y3, z2), valCoord3D(seed, x3, y3, z2), xs),
                        ys),
                cubicLerp(
                        cubicLerp(valCoord3D(seed, x0, y0, z3), valCoord3D(seed, x1, y0, z3), valCoord3D(seed, x2, y0, z3), valCoord3D(seed, x3, y0, z3), xs),
                        cubicLerp(valCoord3D(seed, x0, y1, z3), valCoord3D(seed, x1, y1, z3), valCoord3D(seed, x2, y1, z3), valCoord3D(seed, x3, y1, z3), xs),
                        cubicLerp(valCoord3D(seed, x0, y2, z3), valCoord3D(seed, x1, y2, z3), valCoord3D(seed, x2, y2, z3), valCoord3D(seed, x3, y2, z3), xs),
                        cubicLerp(valCoord3D(seed, x0, y3, z3), valCoord3D(seed, x1, y3, z3), valCoord3D(seed, x2, y3, z3), valCoord3D(seed, x3, y3, z3), xs),
                        ys),
                zs) * CUBIC_3D_BOUNDING;
    }


    public float getCubicFractal(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (fractalType) {
            case FBM:
                return singleCubicFractalFBM(x, y);
            case BILLOW:
                return singleCubicFractalBillow(x, y);
            case RIDGED_MULTI:
                return singleCubicFractalRigidMulti(x, y);
            default:
                return 0;
        }
    }

    private float singleCubicFractalFBM(float x, float y) {
        int seed = this.seed;
        float sum = singleCubic(seed, x, y);
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += singleCubic(++seed, x, y) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleCubicFractalBillow(float x, float y) {
        int seed = this.seed;
        float sum = Math.abs(singleCubic(seed, x, y)) * 2 - 1;
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum += (Math.abs(singleCubic(++seed, x, y)) * 2 - 1) * amp;
        }

        return sum * fractalBounding;
    }

    private float singleCubicFractalRigidMulti(float x, float y) {
        int seed = this.seed;
        float sum = 1 - Math.abs(singleCubic(seed, x, y));
        float amp = 1;
        int i = 0;

        while (++i < octaves) {
            x *= lacunarity;
            y *= lacunarity;

            amp *= gain;
            sum -= (1 - Math.abs(singleCubic(++seed, x, y))) * amp;
        }

        return sum;
    }

    public float getCubic(float x, float y) {
        x *= frequency;
        y *= frequency;

        return singleCubic(0, x, y);
    }

    private final static float CUBIC_2D_BOUNDING = 1 / 2.25f;

    private float singleCubic(int seed, float x, float y) {
        int x1 = fastFloor(x);
        int y1 = fastFloor(y);

        int x0 = x1 - 1;
        int y0 = y1 - 1;
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int x3 = x1 + 2;
        int y3 = y1 + 2;

        float xs = x - (float) x1;
        float ys = y - (float) y1;

        return cubicLerp(
                cubicLerp(valCoord2D(seed, x0, y0), valCoord2D(seed, x1, y0), valCoord2D(seed, x2, y0), valCoord2D(seed, x3, y0),
                        xs),
                cubicLerp(valCoord2D(seed, x0, y1), valCoord2D(seed, x1, y1), valCoord2D(seed, x2, y1), valCoord2D(seed, x3, y1),
                        xs),
                cubicLerp(valCoord2D(seed, x0, y2), valCoord2D(seed, x1, y2), valCoord2D(seed, x2, y2), valCoord2D(seed, x3, y2),
                        xs),
                cubicLerp(valCoord2D(seed, x0, y3), valCoord2D(seed, x1, y3), valCoord2D(seed, x2, y3), valCoord2D(seed, x3, y3),
                        xs),
                ys) * CUBIC_2D_BOUNDING;
    }

    // Cellular Noise
    public float getCellular(float x, float y, float z) {
        x *= frequency;
        y *= frequency;
        z *= frequency;

        switch (cellularReturnType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return singleCellular(x, y, z);
            default:
                return singleCellular2Edge(x, y, z);
        }
    }

    private float singleCellular(float x, float y, float z) {
        int xr = fastRound(x);
        int yr = fastRound(y);
        int zr = fastRound(z);

        float distance = 999999;
        int xc = 0, yc = 0, zc = 0;

        switch (cellularDistanceFunction) {
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            Float3 vec = CELL_3D[hash3D(seed, xi, yi, zi)];

                            float vecX = xi - x + vec.x;
                            float vecY = yi - y + vec.y;
                            float vecZ = zi - z + vec.z;

                            float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                            if (newDistance < distance) {
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            }
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            Float3 vec = CELL_3D[hash3D(seed, xi, yi, zi)];

                            float vecX = xi - x + vec.x;
                            float vecY = yi - y + vec.y;
                            float vecZ = zi - z + vec.z;

                            float newDistance = Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ);

                            if (newDistance < distance) {
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            }
                        }
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            Float3 vec = CELL_3D[hash3D(seed, xi, yi, zi)];

                            float vecX = xi - x + vec.x;
                            float vecY = yi - y + vec.y;
                            float vecZ = zi - z + vec.z;

                            float newDistance = (Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

                            if (newDistance < distance) {
                                distance = newDistance;
                                xc = xi;
                                yc = yi;
                                zc = zi;
                            }
                        }
                    }
                }
                break;
        }

        switch (cellularReturnType) {
            case CELL_VALUE:
                return valCoord3D(0, xc, yc, zc);

            case NOISE_LOOKUP:
                Float3 vec = CELL_3D[hash3D(seed, xc, yc, zc)];
                return cellularNoiseLookup.getConfiguredNoise(xc + vec.x, yc + vec.y, zc + vec.z);

            case DISTANCE:
                return distance - 1;
            default:
                return 0;
        }
    }

    private float singleCellular2Edge(float x, float y, float z) {
        int xr = fastRound(x);
        int yr = fastRound(y);
        int zr = fastRound(z);

        float distance = 999999;
        float distance2 = 999999;

        switch (cellularDistanceFunction) {
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            Float3 vec = CELL_3D[hash3D(seed, xi, yi, zi)];

                            float vecX = xi - x + vec.x;
                            float vecY = yi - y + vec.y;
                            float vecZ = zi - z + vec.z;

                            float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                            distance2 = Math.max(Math.min(distance2, newDistance), distance);
                            distance = Math.min(distance, newDistance);
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            Float3 vec = CELL_3D[hash3D(seed, xi, yi, zi)];

                            float vecX = xi - x + vec.x;
                            float vecY = yi - y + vec.y;
                            float vecZ = zi - z + vec.z;

                            float newDistance = Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ);

                            distance2 = Math.max(Math.min(distance2, newDistance), distance);
                            distance = Math.min(distance, newDistance);
                        }
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        for (int zi = zr - 1; zi <= zr + 1; zi++) {
                            Float3 vec = CELL_3D[hash3D(seed, xi, yi, zi)];

                            float vecX = xi - x + vec.x;
                            float vecY = yi - y + vec.y;
                            float vecZ = zi - z + vec.z;

                            float newDistance = (Math.abs(vecX) + Math.abs(vecY) + Math.abs(vecZ)) + (vecX * vecX + vecY * vecY + vecZ * vecZ);

                            distance2 = Math.max(Math.min(distance2, newDistance), distance);
                            distance = Math.min(distance, newDistance);
                        }
                    }
                }
                break;
            default:
                break;
        }

        switch (cellularReturnType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return distance2 + distance - 1;
            case DISTANCE_2_SUB:
                return distance2 - distance - 1;
            case DISTANCE_2_MUL:
                return distance2 * distance - 1;
            case DISTANCE_2_DIV:
                return distance / distance2 - 1;
            default:
                return 0;
        }
    }

    public float getCellular(float x, float y) {
        x *= frequency;
        y *= frequency;

        switch (cellularReturnType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return singleCellular(x, y);
            default:
                return singleCellular2Edge(x, y);
        }
    }

    private float singleCellular(float x, float y) {
        int xr = fastRound(x);
        int yr = fastRound(y);

        float distance = 999999;
        int xc = 0, yc = 0;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        Float2 vec = CELL_2D[hash2D(seed, xi, yi)];

                        float vecX = xi - x + vec.x;
                        float vecY = yi - y + vec.y;

                        float newDistance = vecX * vecX + vecY * vecY;

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        }
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        Float2 vec = CELL_2D[hash2D(seed, xi, yi)];

                        float vecX = xi - x + vec.x;
                        float vecY = yi - y + vec.y;

                        float newDistance = (Math.abs(vecX) + Math.abs(vecY));

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        }
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        Float2 vec = CELL_2D[hash2D(seed, xi, yi)];

                        float vecX = xi - x + vec.x;
                        float vecY = yi - y + vec.y;

                        float newDistance = (Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY);

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi;
                            yc = yi;
                        }
                    }
                }
                break;
        }

        switch (cellularReturnType) {
            case CELL_VALUE:
                return valCoord2D(0, xc, yc);

            case NOISE_LOOKUP:
                Float2 vec = CELL_2D[hash2D(seed, xc, yc)];
                return cellularNoiseLookup.getConfiguredNoise(xc + vec.x, yc + vec.y);

            case DISTANCE:
                return distance - 1;
            default:
                return 0;
        }
    }

    private float singleCellular2Edge(float x, float y) {
        int xr = fastRound(x);
        int yr = fastRound(y);

        float distance = 999999;
        float distance2 = 999999;

        switch (cellularDistanceFunction) {
            default:
            case EUCLIDEAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        Float2 vec = CELL_2D[hash2D(seed, xi, yi)];

                        float vecX = xi - x + vec.x;
                        float vecY = yi - y + vec.y;

                        float newDistance = vecX * vecX + vecY * vecY;

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
                break;
            case MANHATTAN:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        Float2 vec = CELL_2D[hash2D(seed, xi, yi)];

                        float vecX = xi - x + vec.x;
                        float vecY = yi - y + vec.y;

                        float newDistance = Math.abs(vecX) + Math.abs(vecY);

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
                break;
            case NATURAL:
                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        Float2 vec = CELL_2D[hash2D(seed, xi, yi)];

                        float vecX = xi - x + vec.x;
                        float vecY = yi - y + vec.y;

                        float newDistance = (Math.abs(vecX) + Math.abs(vecY)) + (vecX * vecX + vecY * vecY);

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
                break;
        }

        switch (cellularReturnType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return distance2 + distance - 1;
            case DISTANCE_2_SUB:
                return distance2 - distance - 1;
            case DISTANCE_2_MUL:
                return distance2 * distance - 1;
            case DISTANCE_2_DIV:
                return distance / distance2 - 1;
            default:
                return 0;
        }
    }

    public void GradientPerturb3(float[] v3) {
        SingleGradientPerturb3(seed, gradientPerturbAmp, frequency, v3);
    }

    public void GradientPerturbFractal3(float[] v3) {
        int seed = this.seed;
        float amp = gradientPerturbAmp * fractalBounding;
        float freq = frequency;

        SingleGradientPerturb3(seed, amp, frequency, v3);

        for (int i = 1; i < octaves; i++) {
            freq *= lacunarity;
            amp *= gain;
            SingleGradientPerturb3(++seed, amp, freq, v3);
        }
    }

    private void SingleGradientPerturb3(int seed, float perturbAmp, float frequency, float[] v3) {
        float xf = v3[0] * frequency;
        float yf = v3[1] * frequency;
        float zf = v3[2] * frequency;

        int x0 = fastFloor(xf);
        int y0 = fastFloor(yf);
        int z0 = fastFloor(zf);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        float xs, ys, zs;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = xf - x0;
                ys = yf - y0;
                zs = zf - z0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(xf - x0);
                ys = hermiteInterpolator(yf - y0);
                zs = hermiteInterpolator(zf - z0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(xf - x0);
                ys = quinticInterpolator(yf - y0);
                zs = quinticInterpolator(zf - z0);
                break;
        }

        Float3 vec0 = CELL_3D[hash3D(seed, x0, y0, z0)];
        Float3 vec1 = CELL_3D[hash3D(seed, x1, y0, z0)];

        float lx0x = lerp(vec0.x, vec1.x, xs);
        float ly0x = lerp(vec0.y, vec1.y, xs);
        float lz0x = lerp(vec0.z, vec1.z, xs);

        vec0 = CELL_3D[hash3D(seed, x0, y1, z0)];
        vec1 = CELL_3D[hash3D(seed, x1, y1, z0)];

        float lx1x = lerp(vec0.x, vec1.x, xs);
        float ly1x = lerp(vec0.y, vec1.y, xs);
        float lz1x = lerp(vec0.z, vec1.z, xs);

        float lx0y = lerp(lx0x, lx1x, ys);
        float ly0y = lerp(ly0x, ly1x, ys);
        float lz0y = lerp(lz0x, lz1x, ys);

        vec0 = CELL_3D[hash3D(seed, x0, y0, z1)];
        vec1 = CELL_3D[hash3D(seed, x1, y0, z1)];

        lx0x = lerp(vec0.x, vec1.x, xs);
        ly0x = lerp(vec0.y, vec1.y, xs);
        lz0x = lerp(vec0.z, vec1.z, xs);

        vec0 = CELL_3D[hash3D(seed, x0, y1, z1)];
        vec1 = CELL_3D[hash3D(seed, x1, y1, z1)];

        lx1x = lerp(vec0.x, vec1.x, xs);
        ly1x = lerp(vec0.y, vec1.y, xs);
        lz1x = lerp(vec0.z, vec1.z, xs);

        v3[0] += lerp(lx0y, lerp(lx0x, lx1x, ys), zs) * perturbAmp;
        v3[1] += lerp(ly0y, lerp(ly0x, ly1x, ys), zs) * perturbAmp;
        v3[2] += lerp(lz0y, lerp(lz0x, lz1x, ys), zs) * perturbAmp;
    }

    public void GradientPerturb2(float[] v2) {
        SingleGradientPerturb2(seed, gradientPerturbAmp, frequency, v2);
    }

    public void GradientPerturbFractal2(float[] v2) {
        int seed = this.seed;
        float amp = gradientPerturbAmp * fractalBounding;
        float freq = frequency;

        SingleGradientPerturb2(seed, amp, frequency, v2);

        for (int i = 1; i < octaves; i++) {
            freq *= lacunarity;
            amp *= gain;
            SingleGradientPerturb2(++seed, amp, freq, v2);
        }
    }

    private void SingleGradientPerturb2(int seed, float perturbAmp, float frequency, float[] v2) {
        float xf = v2[0] * frequency;
        float yf = v2[1] * frequency;

        int x0 = fastFloor(xf);
        int y0 = fastFloor(yf);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        float xs, ys;
        switch (interpolation) {
            default:
            case LINEAR:
                xs = xf - x0;
                ys = yf - y0;
                break;
            case HERMITE:
                xs = hermiteInterpolator(xf - x0);
                ys = hermiteInterpolator(yf - y0);
                break;
            case QUINTIC:
                xs = quinticInterpolator(xf - x0);
                ys = quinticInterpolator(yf - y0);
                break;
        }

        Float2 vec0 = CELL_2D[hash2D(seed, x0, y0)];
        Float2 vec1 = CELL_2D[hash2D(seed, x1, y0)];

        float lx0x = lerp(vec0.x, vec1.x, xs);
        float ly0x = lerp(vec0.y, vec1.y, xs);

        vec0 = CELL_2D[hash2D(seed, x0, y1)];
        vec1 = CELL_2D[hash2D(seed, x1, y1)];

        float lx1x = lerp(vec0.x, vec1.x, xs);
        float ly1x = lerp(vec0.y, vec1.y, xs);

        v2[0] += lerp(lx0x, lx1x, ys) * perturbAmp;
        v2[1] += lerp(ly0x, ly1x, ys) * perturbAmp;
    }

}