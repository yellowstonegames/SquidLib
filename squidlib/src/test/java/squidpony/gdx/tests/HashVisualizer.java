package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.Random;

import static squidpony.squidgrid.gui.gdx.SColor.floatGet;
import static squidpony.squidgrid.gui.gdx.SColor.floatGetI;

/**
 * Demo to help with visualizing hash/noise functions and RNG types. Most of the hashes work simply by treating the x,y
 * point of a pixel in the window as either one or two ints and running the hash on an array of those ints. This helps
 * find symmetry issues and cases where the range of numbers produced is smaller than it should be. When the term
 * "visual testing" is used in {@link CrossHash}, this class is what it refers to.
 * <br>
 * <b>INSTRUCTIONS</b>
 * <br>
 * Press enter to go to the next test, s once or twice to demo a group of hash functions (hitting s while you're already
 * demoing hashes will change to a different group), n to demo noise functions, r for RNG varieties, and a for artistic
 * interpretations of hashes with limited color palettes. On anything that changes over time, you can hit alt-c to pause
 * or resume (noise, RNG, and artistic demos all allow this), and if paused you can press c to advance one frame.
 * <br>
 * Some points of interest:
 * <li>
 *     <ul>Most of SquidLib's hashes produce what they should, colorful static. If patterns appear in the static, such
 *     as bands of predictable color at specific intervals, that marks a problem.</ul>
 *     <ul>The FNV-1a algorithm, which is what you get if you use CrossHash with no nested class specified, has
 *     significant visual flaws, in addition to being the slowest hash here. With the mapping of ints to colors used in
 *     this class, it generates 3 hues with much greater frequency, and also produces either rhombus or linear patterns,
 *     depending on how the x,y point is encoded into the array that the demo hashes.</ul>
 *     <ul>Java's built-in {@link Arrays#hashCode(int[])} function has abysmal visual quality, mostly looking like the
 *     exact opposite of the colorful static that the hashes should produce.</ul>
 *     <ul>RNG algorithms should mostly produce a lot of frames per second (determined by the efficiency of the
 *     RandomnessSource), without showing repeats in some way (determined by the lowest period of any random bit).</ul>
 *     <ul>Noise functions should have their own individual "flavor" that determines what uses they may be suited for.
 *     While PerlinNoise is a tried-and-true standby for continuous noise, you may instead want a more chaotic/blocky
 *     appearance for the noise, which MerlinNoise accomplishes somewhat. WhirlingNoise is faster variant on PerlinNoise
 *     that tries to avoid certain patterns, particularly in 2D; judge for yourself if it succeeds. There's two colorful
 *     versions of noise here. One samples 3D noise using the current x and y for a point at 3 different z values based
 *     on the number of frames rendered, and uses those 3 numbers as the red, green, and blue channels. Another samples
 *     3D noise only once, and interprets the single value as a 24-bit int representing a color. The first looks good!
 *     It can be previewed at https://dl.dropboxusercontent.com/u/11914692/rainbow-perlin.gif , although the GIF format
 *     reduces the visible color depth. The second doesn't look good at all, but may be handy for spotting quirks.</ul>
 *     <ul>There are also "artistic interpretations" of the otherwise-chaotic hashes. Nice for getting ideas, they're a
 *     sort of Rorschach-test-like concept.</ul>
 * </li>
 * <br>
 * Created by Tommy Ettinger on 8/20/2016.
 */
public class HashVisualizer extends ApplicationAdapter {
    private SpriteBatch batch;
    private SquidColorCenter colorFactory;
    private SquidPanel display;//, overlay;
    private static final int width = 512, height = 512;
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    private static final SColor bgColor = SColor.BLACK;
    private Stage stage;
    private Viewport view;
    private CrossHash.Storm storm, stormA, stormB, stormC;
    private CrossHash.Mist mist, mistA, mistB, mistC;
    private BeardRNG beard = new BeardRNG();
    private BirdRNG bird = new BirdRNG();
    private FlapRNG flap = new FlapRNG();
    private HerdRNG herd = new HerdRNG();
    private HordeRNG horde = new HordeRNG();
    private IsaacRNG isaac = new IsaacRNG();
    private LapRNG lap = new LapRNG();
    private LightRNG light = new LightRNG();
    private LongPeriodRNG longPeriod = new LongPeriodRNG();
    private PermutedRNG permuted = new PermutedRNG();
    private PintRNG pint = new PintRNG();
    private SlapRNG slap = new SlapRNG();
    private ThunderRNG thunder = new ThunderRNG();
    private XoRoRNG xoRo = new XoRoRNG();
    private XorRNG xor = new XorRNG();
    private ZapRNG zap = new ZapRNG();


    private final int[] coordinates = new int[2];
    private final int[] coordinate = new int[1];
    private final double[] doubleCoordinates = new double[2], doubleCoordinate = new double[1];
    private final double[][][][] seamless = new double[3][64][64][64];
    private final SeededNoise seeded = new SeededNoise(0xDEADD00D);
    private final MeadNoise mead = new MeadNoise(); //new Noise.Layered4D(new MeadNoise(0xDEADD00D), 1, 4.55);
    private final Noise.Noise2D mead2D = new Noise.Layered2D(MeadNoise.instance, 3);
    private final Noise.Noise3D mead3D = new Noise.Layered3D(MeadNoise.instance, 3);
    private final Noise.Noise2D layered2D = new Noise.Layered2D(SeededNoise.instance, 2, 1.75);
    private final Noise.Noise3D layered3D = new Noise.Layered3D(SeededNoise.instance, 2, 1.75);
    private final Noise.Noise4D layered4D = new Noise.Layered4D(SeededNoise.instance, 2, 1.75);
    private final Noise.Noise6D layered6D = new Noise.Layered6D(SeededNoise.instance, 2, 1.75);
    private final Noise.Noise2D value2D = new Noise.Layered2D(ValueNoise.instance, 2);
    private final Noise.Noise3D value3D = new Noise.Layered3D(ValueNoise.instance, 2);
    private final Noise.Noise4D value4D = new Noise.Layered4D(ValueNoise.instance, 2);
    private final Noise.Noise6D value6D = new Noise.Layered6D(ValueNoise.instance, 2);
    private final Noise.Noise2D scaled2D = new Noise.Scaled2D(seeded, 1.43, 1.43);
    private final Noise.Noise3D scaled3D = new Noise.Scaled3D(seeded, 1.43, 1.43, 1.43);
    private final Noise.Noise4D scaled4D = new Noise.Scaled4D(seeded, 1.43, 1.43, 1.43, 1.43);
    private final Noise.Noise6D scaled6D = new Noise.Scaled6D(seeded, 1.43, 1.43, 1.43, 1.43, 1.43, 1.43);
    private final Noise.Noise2D ridged2D = new Noise.Ridged2D(SeededNoise.instance, 2, 1.45); // 1.45
    private final Noise.Noise3D ridged3D = new Noise.Ridged3D(SeededNoise.instance, 2, 1.45); // 1.45
    private final Noise.Noise4D ridged4D = new Noise.Ridged4D(SeededNoise.instance, 2, 1.45); // 1.45
    private final Noise.Noise6D ridged6D = new Noise.Ridged6D(SeededNoise.instance, 2, 1.45); // 1.45
                                                                                   
    private final Noise.Noise2D slick2D = new Noise.Slick2D(SeededNoise.instance, Noise.alternate, 1);
    private final Noise.Noise3D slick3D = new Noise.Slick3D(SeededNoise.instance, Noise.alternate, 1);
    private final Noise.Noise4D slick4D = new Noise.Slick4D(SeededNoise.instance, Noise.alternate, 1);
    private final Noise.Noise6D slick6D = new Noise.Slick6D(SeededNoise.instance, Noise.alternate, 1);

    private final Noise.Noise2D turb2D = new Noise.Turbulent2D(mead, ridged2D, 1, 1);
    private final Noise.Noise3D turb3D = new Noise.Turbulent3D(mead, ridged3D, 1, 1);
    private final Noise.Noise4D turb4D = new Noise.Turbulent4D(mead, ridged4D, 1, 1);
    private final Noise.Noise6D turb6D = new Noise.Turbulent6D(mead, ridged6D, 1, 1);
    private final Noise.Noise2D stretchScaled2D = new Noise.Scaled2D(SeededNoise.instance, 0.035, 0.035);
    private final Noise.Noise3D stretchScaled3D = new Noise.Scaled3D(SeededNoise.instance, 0.035, 0.035, 0.035);

    private final double[] turing = TuringPattern.initialize(width, height);
    private final int[][] turingActivate = TuringPattern.offsetsCircle(width, height, 4),
            turingInhibit = TuringPattern.offsetsCircle(width, height, 8);

    private final double[] connections = {0.0, 0.0, 0.0};
    private final CosmicNumbering cosmos = new CosmicNumbering(connections);

    private final float[][][] fillField = new float[3][width][height],
            fillField3DR = new float[1][width][height],
            fillField3DG = new float[1][width][height],
            fillField3DB = new float[1][width][height];

    // 0 commonly used hashes
    // 1 variants on Storm and other hashes
    // 3 artistic visualizations of hash functions and misc. other
    // 4 noise
    // 5 RNG results
    private int testType = 4;
    private int hashMode = 43, rngMode = 30, noiseMode = 57;

    private RandomnessSource fuzzy;
    private Random jreRandom = new Random(0xFEDCBA987654321L);
    private RandomXS128 gdxRandom = new RandomXS128(0xFEDCBA987654321L);
    private MicroRandom mr = new MicroRandom(0xFEDCBA987654321L);
    private CellularAutomaton ca = new CellularAutomaton(512, 512);
    private int ctr = 0;
    private boolean keepGoing = true;

    private double total = 0.0;
    public static double toDouble(long n) {
        return NumberTools.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
        //return Double.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
    }

    public static float toFloat(int n) {
        return (NumberTools.intBitsToFloat(0x3F800000 | n >>> 9) - 1.0f);
    }

    public static int mixHash(final int x, final int y)
    {
        int x2 = 0x9E3779B9 * x, y2 = 0x632BE5AB * y;
        return ((x2 ^ y2) >>> ((x2 & 7) + (y2 & 7))) * 0x85157AF5;
    }

    public static int oldHash(final int x, final int y)
    {
        int hash = 7;
        hash = 113 * hash + x;
        hash = 113 * hash + y;
        return hash;
    }

    /**
     * A miniature version of LapRNG that can be quickly copied into a project.
     * Sometimes used here to prototype changes to LapRNG's algorithm.
     */
    public static class MicroRandom implements RandomnessSource {
        public long state0, state1, inc = 0x9E3779B97F4A7C15L, mul = 0x632AE59B69B3C209L;

        public MicroRandom()
        {
            this((long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                    ^ (long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
        }

        public MicroRandom(final long seed) {
            state0 = seed * 0x62E2AC0DL + 0x85157AF5;
            state1 = seed * 0x85157AF5L - 0x62E2AC0DL;
        }

        public void setState(final long seed)
        {
            state0 = seed * 0x62E2AC0DL + 0x85157AF5;
            state1 = seed * 0x85157AF5L - 0x62E2AC0DL;
        }

        @Override
        public final long nextLong() {
            //good one
            //return (state1 += ((state0 += 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L);
            return (state1 += ((state0 += inc) >> 24) * mul);
        }

        @Override
        public final int next(final int bits) {
            return (int) (nextLong() >>> (64 - bits));
        }

        /**
         * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
         * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
         * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
         *
         * @return a copy of this RandomnessSource
         */
        @Override
        public RandomnessSource copy() {
            MicroRandom mr = new MicroRandom(1L);
            mr.state0 = state0;
            mr.state1 = state1;
            mr.mul = mul;
            mr.inc = inc;
            return mr;
        }
    }

        /*
        public static float determine(float alpha, float beta)
        {
            //final int a = ((x * 0x92B5CC83) << 16) ^ x, b = (((y * 0xD9B4E019) << 16) ^ y) | 1;

            //final int x = (~alpha << 15) * ~beta + ((alpha >> 1) << 1) + ((alpha >> 2) << 2) + (beta >> ((alpha + ~beta) & 3)),
            //        y = (alpha + ~beta << 17) + ((beta >> 1) << 1) + ((beta >> 2) << 2) + (alpha >> (beta & 3));

            final float x = (alpha + 1.4051f) * (beta + 0.9759f),
                    y = (beta + 2.3757f) * (alpha + 0.7153f) + x * 0.7255f;

            //final float x = (alpha + 1.875371971f) * (beta + 0.875716533f),
            //        y = (beta + 3.875716533f) * (alpha + 0.6298371981f);

                    //a = (((x >> 1) * y + x) ^ (~((x >> 1) * y + x) << 15)) + y,
                    //b = ((y + (y >> 1) * a) ^ (~(y + (y >> 1) * a) << 14)) + x;
            //return toFloat(x ^ (0xCC83 * ((x + y & (y + 0xCD7FE75E)) >> 6)));
            //return toFloat(x * y);
            return ((x % 0.29f) + (y % 0.3f) + alpha * 11.138421537629f % 0.22f + beta * 9.3751649568f % 0.21f); // & 8388607
        }
    */
    public static int rawNoise0(int alpha, int beta) {
        // int x = a * 0x1B + b * 0xB9, y = (a * 0x6F ^ b * 0x53), z = x * 0x2D + y * 0xE5, w = (z ^ x) + y * 0xF1,
        // x = a * 0x1B + b * 0x29, y = (a * 0x2F ^ b * 0x13), z = x * 0x3D + y * 0x45, w = (z ^ x) + y * 0x37,
        // near = (x * 0xB9 ^ y * 0x1B) + (x * 0x57 ^ z * 0x6F) + (y * 0x57 ^ z * 0xB9 ) + (x * 0x2D ^ w * 0xE5) + (y  * 0xA7 ^ w * 0xF1);
        // x = a * 11 + b * 10, y = (a * 13 ^ b * 14), z = x * 4 + y * 5, w = (z * 8 ^ x * 9) + y * 7,
        // out = (x ^ y) + (x ^ z) + (y ^ z) + (x ^ w) + (y ^ w);
        final int a = alpha + ((alpha >> 1) << 2) + (beta >> 1),// + ((alpha >> 2) << 4) + (beta >> 2),
                b = beta + ((beta >> 1) << 2) + (alpha >> 1),// + ((beta >> 2) << 4) + (alpha >> 2),
                a2 = a * 31 ^ a - b, b2 = b * 29 ^ b - a,
                x = a2 + b2, y = (a2 ^ b2), z = x + y, w = (z ^ x) + y,
                out = (x + y + z + w) ^ (a2 + b) * b2 ^ (b2 + a) * a2;
        return ((out & 0x100) != 0) ? ~out & 0xff : out & 0xff;
    }

    public static int discreteNoise(int x, int y) {
        //int n = rawNoise(x, y), t = n << 4;
        return ((rawNoise(x, y) << 2) +
                rawNoise(x + 1, y) + rawNoise(x - 1, y) + rawNoise(x, y + 1) + rawNoise(x, y - 1)
        ) >> 3;
    }

    public static float discreteNoise(int x, int y, float zoom) {
        //int n = rawNoise(x, y), t = n << 4;
        final float alef = x / zoom, bet = y / zoom;
        final int alpha = (int) (alef), beta = (int) (bet),
                north = rawNoise(alpha, beta - 1), south = rawNoise(alpha, beta + 1),
                east = rawNoise(alpha + 1, beta), west = rawNoise(alpha - 1, beta),
                center = rawNoise(alpha, beta);
        final float aBias = (alef - alpha), bBias = (bet - beta);
        return (((aBias - 0.75f) < 0 ? west : center) * 0.6f + ((aBias + 0.75f) >= 1 ? east : center) * 0.6f +
                ((aBias - 0.25f) < 0 ? west : center) * 0.4f + ((aBias + 0.2f) >= 1 ? east : center) * 0.4f +
                ((bBias - 0.75f) < 0 ? north : center) * 0.6f + ((bBias + 0.75f) >= 1 ? south : center) * 0.6f +
                ((bBias - 0.25f) < 0 ? north : center) * 0.4f + ((bBias + 0.25f) >= 1 ? south : center) * 0.4f
        ) * 0.0009765625f;
    }
        /*
        ((aBias - 0.75f) < 1 ? west : center) + ((aBias + 0.75f) >= 2 ? east : center) +
                ((aBias - 0.25f) < 1 ? west : center) + ((aBias + 0.25f) >= 2 ? east : center) +
                ((bBias - 0.75f) < 1 ? north : center) + ((bBias + 0.75f) >= 2 ? south : center) +
                ((bBias - 0.25f) < 1 ? north : center) + ((bBias + 0.25f) >= 2 ? south : center)
         */

        //midBias = (2f - Math.abs(1f - aBias) - Math.abs(1f - bBias)), //(rawNoise(alpha, beta) << 2) +
        /*
                rawNoise(alpha + 1, beta) * aBias + rawNoise(alpha - 1, beta) * (1 - aBias) +
                rawNoise(alpha, beta + 1) * bBias + rawNoise(alpha, beta - 1) * (1 - bBias) +

                rawNoise(alpha + 1, beta+1) * aBias * bBias + rawNoise(alpha - 1, beta-1) * (1 - aBias) * (1 - bBias) +
                rawNoise(alpha-1, beta + 1) * (1 - aBias) * bBias + rawNoise(alpha+1, beta - 1) * aBias * (1 - bBias)/* +
                 + rawNoise(x + 1, y+1) + rawNoise(x - 1, y-1) + rawNoise(x-1, y + 1) + rawNoise(x+1, y - 1)*/
                 /* >> 1) +
                rawNoise(x + 2, y) + rawNoise(x - 2, y) + rawNoise(x, y + 2) + rawNoise(x, y - 2) +
                rawNoise(x + 2, y+2) + rawNoise(x - 2, y-2) + rawNoise(x-2, y + 2) + rawNoise(x+2, y - 2) +
                rawNoise(x + 2, y+1) + rawNoise(x - 2, y+1) + rawNoise(x+1, y + 2) + rawNoise(x+1, y - 2) +
                rawNoise(x + 2, y-1) + rawNoise(x - 2, y-1) + rawNoise(x-1, y + 2) + rawNoise(x-1, y - 2)*/
        //0.00078125f;//;//0.000244140625f;//0.001953125f; //0.0009765625f; // 0.00048828125f;


    public static int rawNoise(int x, int y) {
        //final int mx = x * 17 ^ ((x ^ 11) + (y ^ 13)), my = y * 29 ^ (7 + x + y),
        final int mx = (x * 0x9E37 ^ y * 0x7C15) + (y * 0xA47F + x * 0x79B9), my = (y * 0xA47F ^ x * 0x79B9) ^ (x * 0x9E37 + y * 0x7C15),
                //gx = mx ^ (mx >> 1), gy = my ^ (my >> 1),
                out = ((mx + my + (mx * my)) >>> 4 & 0x1ff); //((Integer.bitCount(gx) + Integer.bitCount(gy) & 63) << 3) ^
        return ((out & 0x100) != 0) ? ~out & 0xff : out & 0xff;
    }
    public static int fastFloor(double t) {
        return t >= 0 ? (int) t : (int) t - 1;
    }

    public static double trigNoise(final double x, final double y, final int seed)
    {
        return NumberTools.bounce(Math.sin(x * 3 - y + seed) + Math.sin(y * 3 - x + seed) + Math.cos(x + y + seed));
    }
    public static double trigNoise(final double ox, final double oy, final double oz, final int seed)
    {
        final double
                x = (NumberTools.randomFloat(seed - 0x9E3779B9) * 0.5 + 0.75) * ox,
                y = (NumberTools.randomFloat(seed + 0x632BE5AB) * 0.5 + 0.75) * oy,
                z = (NumberTools.randomFloat(seed + 0x9E3779B9) * 0.5 + 0.75) * oz,
                sx = (Math.sin(x) + 1) * 0.5, ix = 1.0 - sx,
                sy = (Math.sin(y) + 1) * 0.5, iy = 1.0 - sy,
                sz = (Math.sin(z) + 1) * 0.5, iz = 1.0 - sz,
                a0 = Math.cos(Math.sin(x * sz + y * iz + z) + Math.sin(y * sz + x * iz - z) + Math.sin((x - y) * 0.5)),
                a1 = Math.cos(Math.sin(x * sy + z * iy + y) + Math.sin(z * sy + x * iy - y) + Math.sin((z - x) * 0.5)),
                a2 = Math.cos(Math.sin(z * sx + y * ix + x) + Math.sin(y * sx + z * ix - x) + Math.sin((y - z) * 0.5));
        return (Math.sin((a0 + a1 + a2) * 7.1));
    }

    public static double veryPatternedTrigNoise(final double ox, final double oy, final double oz, final int seed)
    {
        final double r = NumberTools.randomFloat(seed) + 1.75,
                x = r * ox,
                y = r * oy,
                z = r * oz;
        return NumberTools.bounce(5
                + (Math.sin(x) * Math.sin(y))
                + (Math.sin(y) * Math.sin(z))
                + (Math.sin(z) * Math.sin(x))
                + Math.cos(x + y + z + seed));
    }
    public static double wavetrigNoise(final double ox, final double oy, final double oz, final int seed)
    {
        final double r = Math.sin(NumberTools.randomDouble(seed - 0x9E3779B9) * ox
                + NumberTools.randomDouble(seed) * oy
                + NumberTools.randomDouble(seed + 0x9E3779B9) * oz) * 0.2,
                x = (r + 0.5) * ox + oy + r * oz,
                y = (r + 0.55) * oy + oz + r * ox,
                z = (r + 0.6) * oz + ox + r * oy,
                sx = Math.sin(x * r * 3.1 * z + y * r * 2.3 + z * r),
                sy = Math.sin(y * r * 3.1 * x + z * r * 2.3 + x * r),
                sz = Math.sin(z * r * 3.1 * y + x * r * 2.3 + y * r);
        return (Math.sin((sx + sy + sz) * 7.1));
    }
    public static double paternedTrigNoise(final double x, final double y, final double z, final int seed)
    {
        //0x9E3779B9  0x632BE5AB
        final double r = NumberTools.randomFloat(seed) + 1.67,
                //ra = NumberTools.randomSignedFloat(seed - 0x9E3779B9) + 0.85,
                //rb = NumberTools.randomSignedFloat(seed + 0x9E3779B9) + 0.85,
                //rc = NumberTools.randomSignedFloat(seed + 0x632BE5AB) + 0.85,
                sx = Math.sin(x), sy = Math.sin(y), sz = Math.sin(z),
                a0 = Math.cos((y * 3 + z) * r) * (sx + r) * 0.03,
                a1 = Math.cos((z * 3 + x) * r) * (sy + r) * 0.03,
                a2 = Math.cos((x - y) * 2 * r) * (sz + r) * 0.03;
        return //NumberTools.bounce(5 +
                Math.sin((Math.cos(sx * 2 + sy) + Math.cos(sy * 2 + sz) + Math.cos(sz * 2 + sx)
                        + Math.sin(Math.sin(x * a0) * (a1 * 3 - a2) * 5 + Math.sin(y * a1) * (a2 * 3 - a0) * 3 - Math.sin(z * a2) * (a0 * 3 - a1) * 2)
                        + Math.sin(Math.sin(y * a0) * (a1 * 3 - a2) * 5 + Math.sin(z * a1) * (a2 * 3 - a0) * 3 - Math.sin(x * a2) * (a0 * 3 - a1) * 2)
                        + Math.sin(Math.sin(z * a0) * (a1 * 3 - a2) * 5 + Math.sin(x * a1) * (a2 * 3 - a0) * 3 - Math.sin(y * a2) * (a0 * 3 - a1) * 2)) * 8.0
                        //+ Math.atan2(Math.sin(y * rr + x * r), Math.cos(r * y - rr * z))
                        //+ Math.atan2(Math.sin(z * rr + y * r), Math.cos(r * z - rr * x))
                        /*+ Math.sin(y * rr + x * r - z)
                        + Math.sin(z * rr + y * r - x)*/
                );
    }
    public static double bendyTrigNoise(final double x, final double y, final double z, final int seed)
    {
        final double t0 = Math.cos(x + z - y) * 1.6, r0 = Math.cos(t0 + x - z) + 2.9,
                t1 = Math.cos(y + x - z) * 1.75, r1 = Math.cos(t1 + y - x) + 2.6,
                t2 = Math.cos(z + y - x) * 1.9, r2 = Math.cos(t2 + z - y) + 2.3;
        return //NumberTools.bounce(5 +
                Math.sin(
                        //Math.cos(x + y + z)
                        + Math.sin(x * r0 + z * r1 - y * r2 + seed)
                                + Math.sin(y * r0 + x * r1 - z * r2 + seed)
                                + Math.sin(z * r0 + y * r1 - x * r2 + seed)
                );
    }
    public static int prepareSeed(final int seed)
    {
        return ((seed >>> 19 | seed << 13) ^ 0x13A5BA1D);
    }
//    public static float tabbyNoise(final float ox, final float oy, final float oz, final int seed) {
//        final float skew = (ox + oy + oz) / 128f,
//                c = (float) Math.cos(ox + oy + oz) * 0.125f,
//                s = (float) Math.sin(ox - oy - oz) * 0.125f,
//                x = ((ox - oz) * 1.3f + oy * 0.8f * s - oz * c * 0.5f) * skew,
//                y = ((oy - ox) * 1.3f + oz * 0.8f * s - ox * c * 0.5f) * skew,
//                z = ((oz - oy) * 1.3f + ox * 0.8f * s - oy * c * 0.5f) * skew;
//
//        final int
//                xf = SeededNoise.fastFloor(x),
//                yf = SeededNoise.fastFloor(y),
//                zf = SeededNoise.fastFloor(z);
//
//        final float
//                dx = (x - xf),
//                dy = (y - yf),
//                dz = (z - zf);
//        final int
//                mx = SeededNoise.fastFloor(dx * 3 - 1),
//                my = SeededNoise.fastFloor(dy * 3 - 1),
//                mz = SeededNoise.fastFloor(dz * 3 - 1);
//        final float
//                xrl = NumberTools.randomSignedFloat(prepareSeed(xf + seed * 65537)),
//                yrl = NumberTools.randomSignedFloat(prepareSeed(yf + seed * 31)),
//                zrl = NumberTools.randomSignedFloat(prepareSeed(zf + seed * 421)),
//                spot = NumberTools.randomSignedFloat(prepareSeed(xf + yf + zf - seed)) * 0.75f + s + c,
//                ax = (dx - 0.5f) * (dx - 0.5f) * 2f,
//                ay = (dy - 0.5f) * (dy - 0.5f) * 2f,
//                az = (dz - 0.5f) * (dz - 0.5f) * 2f;
//
//        return NumberTools.bounce(5f +
//                (xrl * (1f - ax)
//                        + yrl * (1f - ay)
//                        + zrl * (1f - az)
//                        + ((mx == 0) ? spot : NumberTools.randomSignedFloat(prepareSeed(xf + mx + seed * 65537))) * ax
//                        + ((my == 0) ? spot : NumberTools.randomSignedFloat(prepareSeed(yf + my + seed * 31   ))) * ay
//                        + ((mz == 0) ? spot : NumberTools.randomSignedFloat(prepareSeed(zf + mz + seed * 421  ))) * az
//                ));
//
//
//        /*final double spot = (Math.sin(x + y + z) - (c + s) * 6f) * 0.0625f;
//        return Math.sin(
//                +
//              ( Math.cos(ox - oy + y + z - x * spot)
//              + Math.cos(x + oy - oz + z - y * spot)
//              + Math.cos(x + oz - ox + y - z * spot)
//              ));
//              */
//    }
    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float querp(final float start, final float end, float a){
        return (1f - (a *= a * a * (a * (a * 6f - 15f) + 10f))) * start + a * end;
    }
    /*
     * Linearly interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float interpolate(final float start, final float end, final float a)
    {
        return (1f - a) * start + a * end;
    }

    public static float tabbyNoise(final float x, final float y, final float z, final int seed) {
        final int
                xf = SeededNoise.fastFloor(x),
                yf = SeededNoise.fastFloor(y),
                zf = SeededNoise.fastFloor(z);
        final long sx = ThrustRNG.determine(seed) | 1, sy = sx + 22256, sz = sy + 55512;
        final float
                x0 = NumberTools.formCurvedFloat(NumberTools.splitMix64(xf * sx + 100)),
                x1 = NumberTools.formCurvedFloat(NumberTools.splitMix64((xf+1) * sx + 100)),
                y0 = NumberTools.formCurvedFloat(NumberTools.splitMix64(yf * sy + 200)),
                y1 = NumberTools.formCurvedFloat(NumberTools.splitMix64((yf+1) * sy + 200)),
                z0 = NumberTools.formCurvedFloat(NumberTools.splitMix64(zf * sz + 300)),
                z1 = NumberTools.formCurvedFloat(NumberTools.splitMix64((zf+1) * sz + 300)),
                lx = querp(
                        x0,
                        x1,
                        x - xf),
                ly = querp(
                        y0,
                        y1,
                        y - yf),
                lz = querp(
                        z0,
                        z1,
                        z - zf);
        return NumberTools.bounce(5f + 2.4f * (lx + ly + lz));
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new SquidPanel(width, height, cellWidth, cellHeight);
//        IFilter<Color> filter0 = new Filters.PaletteFilter(
//                new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f},
//                new float[]{0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,},
//                new float[]{0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f, 0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f},
//                new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,}),
//                filter1 = new Filters.PaletteFilter(SColor.YELLOW_GREEN_SERIES),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
//                filter2 = new Filters.PaletteFilter(new SColor[]{SColor.TREE_PEONY, SColor.NAVAJO_WHITE, SColor.BELLFLOWER, SColor.CAPE_JASMINE, SColor.CELADON, SColor.DAWN, SColor.TEAL}),
//                filter3 = new Filters.GrayscaleFilter(),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
//                filter4 = new Filters.PaletteFilter(new SColor[]{SColor.NAVAJO_WHITE, SColor.CAPE_JASMINE, SColor.LEMON_CHIFFON, SColor.PEACH_YELLOW}),
//                filter5 = new Filters.PaletteFilter(new SColor[]{SColor.CORAL_RED, SColor.MEDIUM_SPRING_GREEN, SColor.PSYCHEDELIC_PURPLE, SColor.EGYPTIAN_BLUE});
        //colorFactory = new SquidColorCenter();
        storm = new CrossHash.Storm();
        stormA = CrossHash.Storm.alpha;
        stormB = CrossHash.Storm.beta;
        stormC = CrossHash.Storm.chi;
        mist = new CrossHash.Mist();
        mistA = CrossHash.Mist.alpha;
        mistB = CrossHash.Mist.beta;
        mistC = CrossHash.Mist.chi;
        fuzzy = new ThunderRNG(0xBEEFCAFEF00DCABAL);
        view = new ScreenViewport();
        stage = new Stage(view, batch);

        Noise.seamless3D(seamless[0], 1337, 1);
        Noise.seamless3D(seamless[1], 123456, 1);
        Noise.seamless3D(seamless[2], -9999, 1);
        /*
        Noise.seamless3D(seamless[0], 1337, 1, SeededNoise.instance);
        Noise.seamless3D(seamless[1], 123456, 1, SeededNoise.instance);
        Noise.seamless3D(seamless[2], -9999, 1, SeededNoise.instance);
        */

        ArrayTools.fill(display.colors, -0x1.fffffep126f); // white as a float
        ca.current.insert(250, 250).insert(250, 251).insert(249, 250)
                .insert(250, 249).insert(251, 249)
                .insert(125, 125).insert(125, 126).insert(124, 125)
                .insert(125, 124).insert(126, 124)
                .insert(375, 375).insert(375, 376).insert(374, 375)
                .insert(375, 374).insert(376, 374)
                .insert(125, 375).insert(125, 376).insert(124, 375)
                .insert(125, 374).insert(126, 374)
                .insert(375, 125).insert(375, 126).insert(374, 125)
                .insert(375, 124).insert(376, 124);

        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case 'u':
                    case 'U':
                    case SquidInput.ENTER:
                        switch (testType)
                        {
                            case 4:
                                if(key == SquidInput.ENTER) {
                                    noiseMode++;
                                    noiseMode %= 82;
                                }
                                switch (noiseMode)
                                {
                                    case 16:
                                    case 60:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);

                                        Noise.seamless3D(seamless[0], 1337, 1);
                                        Noise.seamless3D(seamless[1], 123456, 1);
                                        Noise.seamless3D(seamless[2], -9999, 1);
                                        /*
                                        Noise.seamless2D(seamless[0][0], 1337, 1, mead);
                                        Noise.seamless2D(seamless[1][0], 123456, 1, mead);
                                        Noise.seamless2D(seamless[2][0], -9999, 1, mead);
                                        */
                                        break;
                                    case 17:
                                    case 61:
                                        ArrayTools.fill(seamless[0], 0.0);

                                        Noise.seamless3D(seamless[0], -31337, 1);

                                        //Noise.seamless2D(seamless[0][0], -31337, 1, mead);
                                        break;
                                    case 20:
                                    case 64:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1);
                                        Noise.seamless2D(seamless[1][0], 123456, 1);
                                        Noise.seamless2D(seamless[2][0], -9999, 1);
                                        break;
                                    case 21:
                                    case 65:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1);
                                        break;
                                    case 32:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless3D(seamless[0], 1337, 1, turb6D);
                                        Noise.seamless3D(seamless[1], 123456, 1, turb6D);
                                        Noise.seamless3D(seamless[2], -9999, 1, turb6D);
                                        break;
                                    case 33:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless3D(seamless[0], -31337, 1, turb6D);
                                        total = Noise.total;
                                        break;
                                    case 34:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1, turb4D);
                                        Noise.seamless2D(seamless[1][0], 123456, 1, turb4D);
                                        Noise.seamless2D(seamless[2][0], -9999, 1, turb4D);
                                        break;
                                    case 35:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1, turb4D);
                                        total = Noise.total;
                                        break;
                                    case 40:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless3D(seamless[0], 1337, 1, slick6D);
                                        Noise.seamless3D(seamless[1], 123456, 1, slick6D);
                                        Noise.seamless3D(seamless[2], -9999, 1, slick6D);
                                        break;
                                    case 41:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless3D(seamless[0], -31337, 1, slick6D);
                                        break;
                                    case 42:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1, slick4D);
                                        Noise.seamless2D(seamless[1][0], 123456, 1, slick4D);
                                        Noise.seamless2D(seamless[2][0], -9999, 1, slick4D);
                                        break;
                                    case 43:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1, slick4D);
                                        break;
                                    case 48:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless3D(seamless[0], 1337, 1, ridged6D);
                                        Noise.seamless3D(seamless[1], 123456, 1, ridged6D);
                                        Noise.seamless3D(seamless[2], -9999, 1, ridged6D);
                                        break;
                                    case 49:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless3D(seamless[0], -31337, 1, ridged6D);
                                        break;
                                    case 50:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1, ridged4D);
                                        Noise.seamless2D(seamless[1][0], 123456, 1, ridged4D);
                                        Noise.seamless2D(seamless[2][0], -9999, 1, ridged4D);
                                        break;
                                    case 51:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1, ridged4D);
                                        break;
                                    case 52:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless3D(seamless[0], 1337, 1, SeededNoise.instance);
                                        Noise.seamless3D(seamless[1], 123456, 1, SeededNoise.instance);
                                        Noise.seamless3D(seamless[2], -9999, 1, SeededNoise.instance);
                                        break;
                                    case 53:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless3D(seamless[0], -31337, 1, SeededNoise.instance);
                                        break;
                                    case 56:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        ArrayTools.fill(seamless[1], 0.0);
                                        ArrayTools.fill(seamless[2], 0.0);
                                        Noise.seamless2D(seamless[0][0], 1337, 1, SeededNoise.instance);
                                        Noise.seamless2D(seamless[1][0], 123456, 1, SeededNoise.instance);
                                        Noise.seamless2D(seamless[2][0], -9999, 1, SeededNoise.instance);
                                        break;
                                    case 57:
                                        ArrayTools.fill(seamless[0], 0.0);
                                        Noise.seamless2D(seamless[0][0], -31337, 1, SeededNoise.instance);
                                        break;
                                    case 68:
                                        TuringPattern.initializeInto(turing, ctr);
                                        break;
                                    case 69:
                                        TuringPattern.offsetsCircleInto(turingActivate, width, height, 4);
                                        TuringPattern.offsetsCircleInto(turingInhibit, width, height, 8);
                                        TuringPattern.initializeInto(turing, width, height, stretchScaled2D, ctr);
                                        break;
                                }
                                break;
                            case 5:
                                mr.mul = 0x632AE59B69B3C209L;
                                rngMode++;
                                rngMode %= 38;
                                break;
                            case 0:
                                hashMode++;
                                hashMode %= 61; // 45
                                break;
                            case 1:
                                hashMode++;
                                hashMode %= 66;
                                break;
                            case 2:
                                hashMode++;
                                hashMode %= 28;
                            default:
                                ArrayTools.fill(display.colors, -0x1.fffffep126f); // white as a float
                                ca.current.insert(250, 250).insert(250, 251).insert(249, 250)
                                        .insert(250, 249).insert(251, 249)
                                        .insert(125, 125).insert(125, 126).insert(124, 125)
                                        .insert(125, 124).insert(126, 124)
                                        .insert(375, 375).insert(375, 376).insert(374, 375)
                                        .insert(375, 374).insert(376, 374)
                                        .insert(125, 375).insert(125, 376).insert(124, 375)
                                        .insert(125, 374).insert(126, 374)
                                        .insert(375, 125).insert(375, 126).insert(374, 125)
                                        .insert(375, 124).insert(376, 124);
                                //hashMode++;
                                //hashMode %= 29;
                        }
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'C':
                    case 'c':
                        ctr++;
                        if(alt) keepGoing = !keepGoing;
                        putMap();
                        break;
                    case 'S':
                    case 's':
                        testType = (testType + 1) & 1;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'A':
                    case 'a':
                        testType = 3;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'N':
                    case 'n':
                        testType = 4;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'R':
                    case 'r':
                        testType = 5;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'K': // sKip
                    case 'k':
                        ctr += 1000;
                        mr.mul -= 2;
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        //overlay.setPosition(0, 0);
        //Stack stk = new Stack(display, overlay);
        //stage.addActor(stk);
        //stk.layout();
        stage.addActor(display);
        putMap();
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }

    public void putMap() {
        //display.erase();
        //overlay.erase();
        long code;
        float bright;
        int iBright;
        int xx, yy;
        switch (testType) {
            case 1: {
                switch (hashMode) {
                    case 0:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 1:
                        Gdx.graphics.setTitle("Storm (alpha) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 2:
                        Gdx.graphics.setTitle("Storm (beta) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 3:
                        Gdx.graphics.setTitle("Storm (chi) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 4:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("Storm (alpha) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("Storm (beta) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 7:
                        Gdx.graphics.setTitle("Storm (chi) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("Storm (alpha) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 9:
                        Gdx.graphics.setTitle("Storm (beta) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 10:
                        Gdx.graphics.setTitle("Storm (chi) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 11:
                        Gdx.graphics.setTitle("Storm (alpha) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 12:
                        Gdx.graphics.setTitle("Storm (beta) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 13:
                        Gdx.graphics.setTitle("Storm (chi) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 14:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 15:
                        Gdx.graphics.setTitle("Storm (alpha) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 16:
                        Gdx.graphics.setTitle("Storm (beta) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 17:
                        Gdx.graphics.setTitle("Storm (chi) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 18:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 19:
                        Gdx.graphics.setTitle("Storm (alpha) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 20:
                        Gdx.graphics.setTitle("Storm (beta) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 21:
                        Gdx.graphics.setTitle("Storm (chi) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 22:
                        Gdx.graphics.setTitle("Storm (alpha) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 23:
                        Gdx.graphics.setTitle("Storm (beta) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 24:
                        Gdx.graphics.setTitle("Storm (chi) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 25:
                        Gdx.graphics.setTitle("Storm (alpha) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 26:
                        Gdx.graphics.setTitle("Storm (beta) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 27:
                        Gdx.graphics.setTitle("Storm (chi) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 28:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 29:
                        Gdx.graphics.setTitle("Mist (default) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 30:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 31:
                        Gdx.graphics.setTitle("Mist (default) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 32:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mist.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 33:
                        Gdx.graphics.setTitle("Mist (default) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 34:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 35:
                        Gdx.graphics.setTitle("Mist (default) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 36:
                        Gdx.graphics.setTitle("Mist (default) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mist.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 37:
                        Gdx.graphics.setTitle("Mist (semi-random) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Mist.predefined[Light32RNG.determine(x + Light32RNG.determine(y)) & 31].hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                        
                        
                        
                    case 38:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 39:
                        Gdx.graphics.setTitle("Mist (alpha) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 40:
                        Gdx.graphics.setTitle("Mist (beta) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 41:
                        Gdx.graphics.setTitle("Mist (chi) on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 42:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 43:
                        Gdx.graphics.setTitle("Mist (alpha) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 44:
                        Gdx.graphics.setTitle("Mist (beta) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 45:
                        Gdx.graphics.setTitle("Mist (chi) on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 46:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 47:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 48:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 2, low bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 49:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 50:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 51:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 1, low bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 52:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 53:
                        Gdx.graphics.setTitle("Mist (alpha) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 54:
                        Gdx.graphics.setTitle("Mist (beta) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 55:
                        Gdx.graphics.setTitle("Mist (chi) on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 56:
                        Gdx.graphics.setTitle("Arrays.hashCode on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 57:
                        Gdx.graphics.setTitle("Mist (alpha) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 58:
                        Gdx.graphics.setTitle("Mist (beta) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 59:
                        Gdx.graphics.setTitle("Mist (chi) on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 60:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistA.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 61:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistB.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 62:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 2, high bits");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = mistC.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 63:
                        Gdx.graphics.setTitle("Mist (alpha) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistA.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 64:
                        Gdx.graphics.setTitle("Mist (beta) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistB.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 65:
                        Gdx.graphics.setTitle("Mist (chi) 64 on length 1, high bits");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = mistC.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;

                }
            }
            break;
            case 0: {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 2, low bits");
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 2, low bits");
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = storm.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash on length 2, low bits");
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 2, low bits");
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 1, low bits");
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 1, low bits");
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = storm.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash on length 1, low bits");
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 1, low bits");
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 2, low bits");
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = storm.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash64 on length 2, low bits");
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 2, low bits");
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 1, low bits");
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = storm.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash64 on length 1, low bits");
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 1, low bits");
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 2, high bits");
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 2, high bits");
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = storm.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash on length 2, high bits");
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 2, high bits");
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("JDK, hash on length 1, high bits");
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash on length 1, high bits");
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = storm.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash on length 1, high bits");
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash on length 1, high bits");
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 2, high bits");
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = storm.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash64 on length 2, high bits");
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 2, high bits");
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FNV, hash64 on length 1, high bits");
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = storm.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Storm, hash64 on length 1, high bits");
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Lightning, hash64 on length 1, high bits");
                        break;
                    case 28:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Falcon.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash on length 2, high bits");
                        break;
                    case 29:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Falcon.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash on length 1, high bits");
                        break;
                    case 30:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Falcon.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash64 on length 2, high bits");
                        break;
                    case 31:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Falcon.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash64 on length 1, high bits");
                        break;
                    case 32:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Wisp.hash(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash on length 2, high bits");
                        break;
                    case 33:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Wisp.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash on length 1, high bits");
                        break;
                    case 34:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Wisp.hash64(coordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash64 on length 2, high bits");
                        break;
                    case 35:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Wisp.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash64 on length 1, high bits");
                        break;
                    case 36:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = (mixHash(x, y) << 8) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("mixHash");
                        break;
                    case 37:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Wisp.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash on length 1, low bits");
                        break;
                    case 38:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Wisp.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash64 on length 1, low bits");
                        break;
                    case 39:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Wisp.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash on length 2, low bits");
                        break;
                    case 40:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Wisp.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp, hash64 on length 2, low bits");
                        break;
                    case 41:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Falcon.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash on length 2, low bits");
                        break;
                    case 42:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Falcon.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash on length 1, low bits");
                        break;
                    case 43:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Falcon.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash64 on length 2, low bits");
                        break;
                    case 44:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Falcon.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Falcon, hash64 on length 1, low bits");
                        break;

                    case 45:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash(doubleCoordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash on length 2, high bits");
                        break;
                    case 46:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash(doubleCoordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash on length 1, high bits");
                        break;
                    case 47:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash64(doubleCoordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash64 on length 2, high bits");
                        break;
                    case 48:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash64(doubleCoordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash64 on length 1, high bits");
                        break;
                    case 49:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash(doubleCoordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash on length 1, low bits");
                        break;
                    case 50:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash64(doubleCoordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash64 on length 1, low bits");
                        break;
                    case 51:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash(doubleCoordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash on length 2, low bits");
                        break;
                    case 52:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash64(doubleCoordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash64 on length 2, low bits");
                        break;

                    case 53:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash32(doubleCoordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash32 on length 2, high bits");
                        break;
                    case 54:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash32(doubleCoordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash32 on length 1, high bits");
                        break;
                    case 55:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash32(doubleCoordinates) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double Alt, hash32 on length 2, high bits");
                        break;
                    case 56:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash32(doubleCoordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash32 on length 1, high bits");
                        break;
                    case 57:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash32(doubleCoordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash32 on length 1, low bits");
                        break;
                    case 58:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                doubleCoordinate[0] = Math.sqrt((x << 9) | y);
                                code = CrossHash.Wisp.hash32(doubleCoordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash32 on length 1, low bits");
                        break;
                    case 59:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash32(doubleCoordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash32 on length 2, low bits");
                        break;
                    case 60:
                        for (int x = 0; x < width; x++) {
                            doubleCoordinates[0] = Math.sqrt(x);
                            for (int y = 0; y < height; y++) {
                                doubleCoordinates[1] = Math.sqrt(y);
                                code = CrossHash.Wisp.hash32(doubleCoordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("Wisp Double, hash32 on length 2, low bits");
                        break;
                }
            }
            break;
            case 4: { //Noise mode
                switch (noiseMode) {
                    case 0:
                        Gdx.graphics.setTitle("Perlin Noise, x3 zoom at " + Gdx.graphics.getFramesPerSecond() +
                                " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = (float)
                                        (//PerlinNoise.noise(xx / 16.0, yy / 16.0) * 16 +
                                        //PerlinNoise.noise(xx / 8.0, yy / 8.0) * 8 +
                                        PerlinNoise.noise(xx * 0.25, yy * 0.25) * 4 +
                                        PerlinNoise.noise(xx * 0.5, yy * 0.5) * 2 +
                                        PerlinNoise.noise(xx, yy)
                                        + 7f) / 14f;
                                        //+ 15f) / 30f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 1:
                        Gdx.graphics.setTitle("Merlin Noise, no zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                iBright = MerlinNoise.noise2D(xx, yy);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 2:
                        Gdx.graphics.setTitle("Merlin Noise, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                iBright = MerlinNoise.noise2D(xx, yy, 3);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 3:
                        Gdx.graphics.setTitle("Merlin Noise Alt, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                /*
                                iBright = (MerlinNoise.noise2D(x, y, 8) * 10
                                        + MerlinNoise.noise2D(x + 333 * 3, y + 333 * 3, 5) * 3
                                        + MerlinNoise.noise2D(x + 333 * 4, y + 333 * 4, 3) * 2
                                        + MerlinNoise.noise2D(x + 333 * 5, y + 333 * 5)) >> 4;*/
                                iBright = MerlinNoise.noise2D_alt(xx, yy, 3);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 4:
                        Gdx.graphics.setTitle("Merlin Noise 3D, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = MerlinNoise.noise3D(x, y, ctr, 3);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("Merlin Noise Alt 3D, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = MerlinNoise.noise3D_alt(x, y, ctr, 3);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("Merlin Noise Alt 3D Emphasized, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = MerlinNoise.noise3D_emphasized(x, y, ctr, 3);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    /*case 6:
                        Gdx.graphics.setTitle("Merlin Precalc Noise, seed 0");
                        map = MerlinNoise.preCalcNoise2D(width, height, 0);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = map[x][y];
                                display.put(x, y, floatGet(iBright, iBright, iBright));
                            }
                        }
                        break;
                        */
                    /*
                    case 7:
                        Gdx.graphics.setTitle("Merlin Precalc Noise, seed 65535");
                        map = MerlinNoise.preCalcNoise2D(width, height, 65535);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = map[x][y];
                                display.put(x, y, floatGet(iBright, iBright, iBright));
                            }
                        }
                        break;*/
                    case 7:
                        Gdx.graphics.setTitle("Perlin 3D Noise, x3 zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)
                                        (//PerlinNoise.noise(x / 8.0, y / 8.0, ctr * 0.125) * 8 +
                                                PerlinNoise.noise(x * 0.25, y * 0.25, ctr * 0.3) * 4 +
                                                        PerlinNoise.noise(x * 0.5, y * 0.5, ctr * 0.3) * 2 +
                                                        PerlinNoise.noise(x, y, ctr * 0.3)
                                                        + 7f) / 14f;
                                //+ 15.0f) / 30f;

                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("Perlin Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = (float)
                                        (//PerlinNoise.noise(xx / 16.0, yy / 16.0) * 16 +
                                                //PerlinNoise.noise(xx / 8.0, yy / 8.0) * 8 +
                                                //PerlinNoise.noise(xx / 4.0, yy / 4.0) * 4 +
                                                //PerlinNoise.noise(xx / 2.0, yy / 2.0) * 2 +
                                                PerlinNoise.noise(xx, yy)
                                                        + 1f) / 2f;
                                //+ 15f) / 30f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 9:
                        Gdx.graphics.setTitle("Perlin 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)
                                        (//PerlinNoise.noise(x / 8.0, y / 8.0, ctr * 0.125) * 8 +
                                                //PerlinNoise.noise(x / 4.0, y / 4.0, ctr * 0.125) * 4 +
                                                //PerlinNoise.noise(x / 2.0, y / 2.0, ctr * 0.125) * 2 +
                                                PerlinNoise.noise(x, y, ctr * 0.3)
                                                        + 1f) / 2f;
                                //+ 15.0f) / 30f;

                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 10:
                        Gdx.graphics.setTitle("Whirling 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)
                                        (//PerlinNoise.noise(x / 8.0, y / 8.0, ctr * 0.125) * 8 +
                                                //PerlinNoise.noise(x / 4.0, y / 4.0, ctr * 0.125) * 4 +
                                                //PerlinNoise.noise(x / 2.0, y / 2.0, ctr * 0.125) * 2 +
                                                WhirlingNoise.noise(x * 0.125, y * 0.125, ctr  * 0.0375)
                                                        + 1f) / 2f;
                                //+ 15.0f) / 30f;

                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 11:
                        Gdx.graphics.setTitle("Whirling Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = (float)
                                        (//PerlinNoise.noise(xx / 16.0, yy / 16.0) * 16 +
                                                //PerlinNoise.noise(xx / 8.0, yy / 8.0) * 8 +
                                                //PerlinNoise.noise(xx / 4.0, yy / 4.0) * 4 +
                                                //PerlinNoise.noise(xx / 2.0, yy / 2.0) * 2 +
                                                WhirlingNoise.noise(xx * 0.125, yy * 0.125)
                                                        + 1f) / 2f;
                                //+ 15f) / 30f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 12:
                        Gdx.graphics.setTitle("Whirling Alt 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (//PerlinNoise.noise(x / 8.0, y / 8.0, ctr * 0.125) * 8 +
                                                //PerlinNoise.noise(x / 4.0, y / 4.0, ctr * 0.125) * 4 +
                                                //PerlinNoise.noise(x / 2.0, y / 2.0, ctr * 0.125) * 2 +
                                                WhirlingNoise.noiseAlt(x * 0.125, y * 0.125, ctr  * 0.0375)
                                                        + 1f) / 2f;
                                //+ 15.0f) / 30f;

                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 13:
                        Gdx.graphics.setTitle("Whirling Alt Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            xx = x + ctr;
                            for (int y = 0; y < height; y++) {
                                yy = y + ctr;
                                bright = (//PerlinNoise.noise(xx / 16.0, yy / 16.0) * 16 +
                                                //PerlinNoise.noise(xx / 8.0, yy / 8.0) * 8 +
                                                //PerlinNoise.noise(xx / 4.0, yy / 4.0) * 4 +
                                                //PerlinNoise.noise(xx / 2.0, yy / 2.0) * 2 +
                                                WhirlingNoise.noiseAlt(xx * 0.125, yy * 0.125)
                                                        + 1f) / 2f;
                                //+ 15f) / 30f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                        //You can preview this at https://dl.dropboxusercontent.com/u/11914692/rainbow-perlin.gif
                    case 14:
                        Gdx.graphics.setTitle("Whirling 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y, floatGet(
                                        (WhirlingNoise.noiseAlt(x * 0.0625, y * 0.0625, ctr  * 0.125) + 1f) * 0.5f,
                                        (WhirlingNoise.noiseAlt(x * 0.0625, y * 0.0625, ctr  * 0.125 + 234.5) + 1f) * 0.5f,
                                        (WhirlingNoise.noiseAlt(x * 0.0625, y * 0.0625, ctr  * 0.125 + 678.9) + 1f) * 0.5f,
                                        1f));
                            }
                        }
                        break;
                    case 15:
                        Gdx.graphics.setTitle("Whirling 3D Color Noise, one octave as all channels at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        NumberUtils.intToFloatColor(0xFE000000 |
                                                (int)((WhirlingNoise.noise(x * 0.0625, y * 0.0625, ctr  * 0.125)
                                                        + 1.0) * 8388607.5)));
                            }
                        }
                        break;
                                                /*
                        double p, q, r = ctr * 0.03125 * 0.0625,
                                dx_div_2pi = 2.0 * 0.15915494309189535,
                                dy_div_2pi = 2.0 * 0.15915494309189535,
                                dz_div_2pi = 16.0 * 0.15915494309189535,
                                ps, pc,
                                qs, qc,
                                rs = Math.sin(r * 6.283185307179586) * dz_div_2pi, rc = Math.cos(r * 6.283185307179586) * dz_div_2pi;
                        int idx = 0;
                        for (int x = 0; x < 64; x++) {
                            p = x * 0.015625;
                            ps = Math.sin(p * 6.283185307179586) * dx_div_2pi;
                            pc = Math.cos(p * 6.283185307179586) * dx_div_2pi;
                            for (int y = 0; y < 64; y++) {
                                q = y * 0.015625;
                                qs = Math.sin(q * 6.283185307179586) * dy_div_2pi;
                                qc = Math.cos(q * 6.283185307179586) * dy_div_2pi;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 1234) * 0.5) + 0.5f;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 54321) * 0.5) + 0.5f;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 1234321) * 0.5) + 0.5f;
                            }
                        }
                        */
                        /*
                        double p, q, r = ctr * 0.03125 * 0.0625,
                                dx_div_2pi = 2.0 * 0.15915494309189535,
                                dy_div_2pi = 2.0 * 0.15915494309189535,
                                dz_div_2pi = 16.0 * 0.15915494309189535,
                                ps, pc,
                                qs, qc,
                                rs = Math.sin(r * 6.283185307179586) * dz_div_2pi, rc = Math.cos(r * 6.283185307179586) * dz_div_2pi;
                        int idx = 0;
                        for (int x = 0; x < 64; x++) {
                            p = x * 0.015625;
                            ps = Math.sin(p * 6.283185307179586) * dx_div_2pi;
                            pc = Math.cos(p * 6.283185307179586) * dx_div_2pi;
                            for (int y = 0; y < 64; y++) {
                                q = y * 0.015625;
                                qs = Math.sin(q * 6.283185307179586) * dy_div_2pi;
                                qc = Math.cos(q * 6.283185307179586) * dy_div_2pi;
                                seamless[idx++] = (float) (SeededNoise.noise(pc, ps, qc, qs, rc, rs, 123456) * 0.5) + 0.5f;
                            }
                        }
                        */


                    case 16:
                    case 60:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f));

                            }
                        }
                        break;
                    case 17:
                    case 61:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));

                            }
                        }
                        break;
                    case 18:
                    case 62:
                        Gdx.graphics.setTitle("Seeded 6D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 0.0, 0.0, 0.0,1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 0.0, 0.0, 0.0,54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 0.0, 0.0, 0.0,1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 19:
                    case 63:
                        Gdx.graphics.setTitle("Seeded 6D as 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        0.0, 0.0, 0.0, 123456) * 0.50f) + 0.50f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 20:
                    case 64:
                        Gdx.graphics.setTitle("Seeded Seamless 2D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                    break;
                    case 21:
                    case 65:
                        Gdx.graphics.setTitle("Seeded Seamless 2D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                    break;
                    case 22:
                    case 66:
                        Gdx.graphics.setTitle("Seeded 4D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 0,1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 0,54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 0,1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 23:
                    case 67:
                        Gdx.graphics.setTitle("Seeded 4D as 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        0.0,123456) * 0.50f) + 0.50f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 24:
                        Gdx.graphics.setTitle("Seeded 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 25:
                        Gdx.graphics.setTitle("Seeded 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        123456) * 0.50f) + 0.50f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 26:
                        Gdx.graphics.setTitle("Seeded 2D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float)(SeededNoise.noise(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(SeededNoise.noise(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(SeededNoise.noise(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 27:
                        Gdx.graphics.setTitle("Seeded 2D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125 + ctr  * 0.05125, y * 0.03125 + ctr  * 0.05125,
                                        123456) * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 28:
                        Gdx.graphics.setTitle("Seeded Ridged 3D Color Noise, two octaves per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                ((float)ridged3D.getNoiseWithSeed(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 1234) * 0.50f) + 0.50f,
                                                ((float)ridged3D.getNoiseWithSeed(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 54321) * 0.50f) + 0.50f,
                                                ((float)ridged3D.getNoiseWithSeed(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 29:
                        Gdx.graphics.setTitle("Seeded Ridged 3D Noise, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */ridged3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        123456) * 0.50f) + 0.50f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 30:
                        Gdx.graphics.setTitle("Seeded Ridged 2D Color Noise, two octaves per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float)(ridged2D.getNoiseWithSeed(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(ridged2D.getNoiseWithSeed(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(ridged2D.getNoiseWithSeed(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 31:
                        Gdx.graphics.setTitle("Seeded Ridged 2D Noise, two octaves at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */ridged2D.getNoiseWithSeed(x * 0.03125 + ctr  * 0.05125, y * 0.03125 + ctr  * 0.05125,
                                        123456) * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 32:
                        Gdx.graphics.setTitle("Seeded Turbulent Seamless 3D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 33:
                        //Gdx.graphics.setTitle("Seeded Turbulent Seamless 3D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("Turb6D Seamless at " + Gdx.graphics.getFramesPerSecond()  + " FPS, total " + total);
                        break;
                    case 34:
                        Gdx.graphics.setTitle("Seeded Turbulent Seamless 2D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 35:
                        //Gdx.graphics.setTitle("Seeded Turbulent Seamless 2D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("Turb4D Seamless at " + Gdx.graphics.getFramesPerSecond()  + " FPS, total " + total);
                        break;
                    case 36:
                        Gdx.graphics.setTitle("Seeded Turbulent 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                ((float)turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234) * 0.50f) + 0.50f,
                                                ((float)turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 54321) * 0.50f) + 0.50f,
                                                ((float)turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 37:
                        total = 0.0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */turb3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125,
                                        123456) * 0.5f);
                                total += bright;
                                bright += 0.5f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("Turb3D at " + Gdx.graphics.getFramesPerSecond()  + " FPS, total " + total);
                        break;
                    case 38:
                        Gdx.graphics.setTitle("Seeded Turbulent 2D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float)(turb2D.getNoiseWithSeed(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(turb2D.getNoiseWithSeed(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(turb2D.getNoiseWithSeed(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 39:
                        total = 0.0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */turb2D.getNoiseWithSeed(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                        123456) * 0.5);
                                total += bright;
                                bright += 0.5f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("Turb2D at " + Gdx.graphics.getFramesPerSecond()  + " FPS, total " + total);
                        break;
                    case 40:
                        Gdx.graphics.setTitle("Seeded Slick Seamless 3D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 41:
                        Gdx.graphics.setTitle("Seeded Slick Seamless 3D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 42:
                        Gdx.graphics.setTitle("Seeded Slick Seamless 2D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 43: {
                        Gdx.graphics.setTitle("Seeded Slick Seamless 2D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                    }
                    break;
                    case 44:
                        Gdx.graphics.setTitle("Seeded Slick 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                ((float)slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234) * 0.50f) + 0.50f,
                                                ((float)slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 54321) * 0.50f) + 0.50f,
                                                ((float)slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125, 1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 45:
                        Gdx.graphics.setTitle("Seeded Slick 3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */slick3D.getNoiseWithSeed(x * 0.03125, y * 0.03125, ctr * 0.05125,
                                        123456) * 0.50f) + 0.50f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 46:
                        Gdx.graphics.setTitle("Seeded Slick 2D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float)(slick2D.getNoiseWithSeed(x * 0.03125 + 20 + ctr * 0.05125, y * 0.03125 + 30 + ctr * 0.05125, 1234) * 0.5 + 0.5),
                                                (float)(slick2D.getNoiseWithSeed(x * 0.03125 + 30 + ctr * 0.05125, y * 0.03125 + 10 + ctr * 0.05125, 54321) * 0.5 + 0.5),
                                                (float)(slick2D.getNoiseWithSeed(x * 0.03125 + 10 + ctr * 0.05125, y * 0.03125 + 20 + ctr * 0.05125, 1234321) * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 47:
                        Gdx.graphics.setTitle("Seeded Slick 2D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        slick2D.getNoiseWithSeed(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                        123456) * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 48:
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 3D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 49:
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 3D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 50:
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 2D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 51: {
                        Gdx.graphics.setTitle("Seeded Ridged Seamless 2D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                    }
                    break;

                    case 52:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][ctr & 63][x & 63][y & 63] * 0.5 + 0.5),
                                                1.0f));

                            }
                        }
                        break;
                    case 53:
                        Gdx.graphics.setTitle("Seeded Seamless 3D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][ctr & 63][x & 63][y & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));

                            }
                        }
                        break;
                    case 54:
                        Gdx.graphics.setTitle("Seeded 6D as 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 1.0, 3.0, 2.0,1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 1.0, 3.0, 2.0,54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 1.0, 3.0, 2.0,1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 55:
                        Gdx.graphics.setTitle("Seeded 6D as 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */SeededNoise.noise(x * 0.03125, y * 0.03125, ctr  * 0.05125,
                                        1.0, 3.0, 2.0, 123456) * 0.50f) + 0.50f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 56:
                        Gdx.graphics.setTitle("Seeded Seamless 2D Color Noise, three octaves per channel at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[1][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                (float) (seamless[2][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5),
                                                1.0f));
                            }
                        }
                        break;
                    case 57: {
                        Gdx.graphics.setTitle("Seeded Seamless 2D Noise, three octaves at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float) (seamless[0][0][x+ctr & 63][y+ctr & 63] * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                    }
                    break;
                    case 58:
                        Gdx.graphics.setTitle("Tabby 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                /*
                                display.put(x, y,
                                        floatGet(
                                                ((float)SeededNoise.noise(x * 0.03125 + 20, y * 0.03125 + 30, ctr * 0.05125 + 10, 3,1234) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 30, y * 0.03125 + 10, ctr * 0.05125 + 20, 3,54321) * 0.50f) + 0.50f,
                                                ((float)SeededNoise.noise(x * 0.03125 + 10, y * 0.03125 + 20, ctr * 0.05125 + 30, 3,1234321) * 0.50f) + 0.50f,
                                                1.0f));*/
                                display.put(x, y,
                                        floatGet(
                                                (tabbyNoise(x * 0.11125f + 20, y * 0.11125f + 30, ctr * 0.11125f + 10, 1234) * 0.50f) + 0.50f,
                                                (tabbyNoise(x * 0.11125f + 30, y * 0.11125f + 10, ctr * 0.11125f + 20, 54321) * 0.50f) + 0.50f,
                                                (tabbyNoise(x * 0.11125f + 10, y * 0.11125f + 20, ctr * 0.11125f + 30, 1234321) * 0.50f) + 0.50f,
                                                1.0f));
                            }
                        }
                        break;
                    case 59:
                        Gdx.graphics.setTitle("Tabby 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(/*Noise.seamless3D(x * 0.0625, y * 0.0625, ctr  * 0.05125,
                                        20.0, 20.0, 20.0, 12) * 0.5
                                        + Noise.seamless3D(x * 0.125, y * 0.125, ctr  * 0.05125,
                                        40.0, 40.0, 20.0, 1234)
                                        + */tabbyNoise(x * 0.11125f, y * 0.11125f, ctr  * 0.11125f,
                                        123456) * 0.50f) + 0.50f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 68:
                        Gdx.graphics.setTitle("Turing Pattern at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        TuringPattern.distort(turingActivate, width, height, stretchScaled3D, ctr * 5, 778899);
                        TuringPattern.distort(turingInhibit, width, height, stretchScaled3D, ctr * 5, 556677);
                        TuringPattern.step(turing, turingActivate, 0.2, turingInhibit, -0.2);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(turing[x * height + y]) * 0.5f + 0.5f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 69:
                        Gdx.graphics.setTitle("Turing Pattern from SeededNoise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        TuringPattern.step(turing, turingActivate, 0.1, turingInhibit, -0.1);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(turing[x * height + y]) * 0.5f + 0.5f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 70:
                        Gdx.graphics.setTitle("Mason 2D Standard Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        MasonNoise.noise(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                                123456) * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        /*
                        Gdx.graphics.setTitle("Mason 2D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField[0], 0f);
                        ArrayTools.fill(fillField[1], 0f);
                        ArrayTools.fill(fillField[2], 0f);
                        MasonNoise.addNoiseField(fillField[0], 20f + ctr * 0.05125f, 30f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, -1234, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField[1], 30f + ctr * 0.05125f, 10f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 54321, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField[2], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 15951, 1f, 1.375f);

                        MasonNoise.addNoiseField(fillField[0], 20f + ctr * 0.05125f, 30f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, 1234, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField[1], 30f + ctr * 0.05125f, 10f + ctr * 0.05125f, 512f * 0.0625f + 30f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, -321, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField[2], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, -951, 1f, 0.625f);

                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(fillField[0][x][y] * 0.25f + 0.5f, fillField[1][x][y] * 0.25f + 0.5f, fillField[2][x][y] * 0.25f + 0.5f, 1f)
                                );
                            }
                        }*/
                        break;
                    case 71:
                        Gdx.graphics.setTitle("Mason 2D Arc Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)(
                                        MasonNoise.noiseArc(x * 0.03125 + ctr * 0.05125, y * 0.03125 + ctr * 0.05125,
                                                123456) * 0.5 + 0.5);
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        /*
                        Gdx.graphics.setTitle("Mason 2D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField[0], 0f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, -12345,  3.5f, 1.08f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 123456, 2.625f, 1.44f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, -54321, 1.25f, 1.87f);
                        MasonNoise.addNoiseField(fillField[0], 10f + ctr * 0.05125f, 20f + ctr * 0.05125f, 512f * 0.0625f + 10f + ctr * 0.05125f, 512f * 0.0625f + 20f + ctr * 0.05125f, 654321, 0.625f, 2.19f);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = fillField[0][x][y] * 0.0625f + 0.5f;
                                display.put(x, y,
                                        floatGet(bright, bright, bright, 1f)
                                );
                            }
                        }
                        */
                        break;
                    case 72:
                        Gdx.graphics.setTitle("Mason 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField3DR[0], 0f);
                        ArrayTools.fill(fillField3DG[0], 0f);
                        ArrayTools.fill(fillField3DB[0], 0f);
                        MasonNoise.addNoiseField(fillField3DR,20f, 30f, 10f + ctr * 0.05125f,
                                512f * 0.0625f + 20f, 512f * 0.0625f + 30f, 10f + ctr * 0.05125f,
                                -1234, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField3DG, 30f, 10f, 20f + ctr * 0.05125f,
                                512f * 0.0625f + 30f, 512f * 0.0625f + 10f, 20f + ctr * 0.05125f,
                                54321, 1f, 1.375f);
                        MasonNoise.addNoiseField(fillField3DB,10f, 20f, 30f + ctr * 0.05125f,
                                512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f,
                                15951, 1f, 1.375f);

                        MasonNoise.addNoiseField(fillField3DR, 20f, 30f, 10f + ctr * 0.05125f,
                                512f * 0.0625f + 20f, 512f * 0.0625f + 30f, 10f + ctr * 0.05125f,
                                7123, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField3DG,30f, 10f, 20f + ctr * 0.05125f,
                                512f * 0.0625f + 30f, 512f * 0.0625f + 10f, 20f + ctr * 0.05125f,
                                -321, 1f, 0.625f);
                        MasonNoise.addNoiseField(fillField3DB, 10f, 20f, 30f + ctr * 0.05125f,
                                512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f,
                                -951, 1f, 0.625f);

                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(fillField3DR[0][x][y] * 0.25f + 0.5f, fillField3DG[0][x][y] * 0.25f + 0.5f, fillField3DB[0][x][y] * 0.25f + 0.5f, 1f)
                                );
                            }
                        }
                        break;
                    case 73:
                        Gdx.graphics.setTitle("Mason 3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        ArrayTools.fill(fillField3DG[0], 0f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, -12345,  3.5f, 1.08f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, 123456, 2.625f, 1.44f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, -54321, 1.25f, 1.87f);
                        MasonNoise.addNoiseField(fillField3DG, 10f, 20f, 30f + ctr * 0.05125f, 512f * 0.0625f + 10f, 512f * 0.0625f + 20f, 30f + ctr * 0.05125f, 654321, 0.625f, 2.19f);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = fillField3DG[0][x][y] * 0.0625f + 0.5f;
                                display.put(x, y,
                                        floatGet(bright, bright, bright, 1f)
                                );
                            }
                        }
                        break;
                    case 74:
                        Gdx.graphics.setTitle("Mead 2D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float)mead2D.getNoiseWithSeed(x * 0.03125 + 20 + ctr * 0.045, y * 0.03125 + 30 + ctr * 0.045, 1234) * 0.5f + 0.5f,
                                                (float)mead2D.getNoiseWithSeed(x * 0.03125 + 30 + ctr * 0.045, y * 0.03125 + 10 + ctr * 0.045, 54321) * 0.5f + 0.5f,
                                                (float)mead2D.getNoiseWithSeed(x * 0.03125 + 10 + ctr * 0.045, y * 0.03125 + 20 + ctr * 0.045, 1234321) * 0.5f + 0.5f,
                                                1.0f));
                            }
                        }
                        break;
                    case 75:
                        Gdx.graphics.setTitle("Mead 2D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)mead2D.getNoiseWithSeed(x * 0.03125 + ctr * 0.045, y * 0.03125 + ctr * 0.045,123456)
                                        * 0.5f + 0.5f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    case 76:
                        Gdx.graphics.setTitle("Mead 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        floatGet(
                                                (float)mead3D.getNoiseWithSeed(x * 0.03125f + 20f, y * 0.03125f + 30f, ctr * 0.045f, 1234) * 0.5f + 0.5f,
                                                (float)mead3D.getNoiseWithSeed(x * 0.03125f + 30f, y * 0.03125f + 10f, ctr * 0.045f, 54321) * 0.5f + 0.5f,
                                                (float)mead3D.getNoiseWithSeed(x * 0.03125f + 10f, y * 0.03125f + 20f, ctr * 0.045f, 1234321) * 0.5f + 0.5f,
                                                1.0f));
                            }
                        }
                        break;
                    case 77:
                        Gdx.graphics.setTitle("Mead 3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = (float)mead3D.getNoiseWithSeed(x * 0.03125f, y * 0.03125f, ctr * 0.045f, 123456) * 0.5f + 0.5f;
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 78:
                        Gdx.graphics.setTitle("ColorNoise 2D at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                                ColorNoise.colorNoise(x * 0.03125f + 20f + ctr * 0.045f, y * 0.03125f + 30f + ctr * 0.045f, 1234));
                            }
                        }
                        break;
                    case 79:
                        Gdx.graphics.setTitle("ColorNoise 3D at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                display.put(x, y,
                                        ColorNoise.colorNoise(x * 0.03125f + 20f, y * 0.03125f + 30f, ctr * 0.045f, 1234));
                            }
                        }
                        break;
                    case 80:
                        Gdx.graphics.setTitle("Cosmic 3D Color Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                connections[2] = ctr * 0.017;
                                connections[0] = x * 0.123 - y * 0.083 + ctr * 0.011;
                                connections[1] = y * 0.123 - x * 0.083 + ctr * 0.011;
//                                connections[0] = NumberTools.bounce(cosmos.getDoubleBase() * 1.3 + 5.0) + 1.8;
//                                connections[1] = NumberTools.bounce(cosmos.getDoubleBase() * 1.7 + 5.0) + 1.6;
//                                connections[2] = NumberTools.bounce(cosmos.getDoubleBase() * 1.9 + 5.0) + 1.2;
                                iBright = cosmos.getInt() >> 5;
                                connections[0] = NumberTools.intBitsToFloat(((iBright * 61) >>> 9) | 0x40000000);
                                connections[1] = NumberTools.intBitsToFloat(((iBright * 52) >>> 9) | 0x40000000);
                                connections[2] = NumberTools.intBitsToFloat(((iBright * 33) >>> 9) | 0x40000000);
                                //bright = (float)cosmos.getDouble();
                                display.put(x, y,
                                        floatGet(
//                                                NumberTools.intBitsToFloat((iBright * 5 >>> 9) | 0x40000000) * 0.5f + 0.5f,
//                                                NumberTools.intBitsToFloat((iBright * 6 >>> 9) | 0x40000000) * 0.5f + 0.5f,
//                                                NumberTools.intBitsToFloat((iBright * 7 >>> 9) | 0x40000000) * 0.5f + 0.5f,
//                                                NumberTools.bounce(bright * 1.2233f) * 0.5f + 0.5f,
//                                                NumberTools.bounce(bright * 3.2435f) * 0.5f + 0.5f,
//                                                NumberTools.bounce(bright * 5.2637f) * 0.5f + 0.5f,
                                                (float)connections[0],
                                                (float)connections[1],
                                                (float)connections[2],
                                                1.0f));
                            }
                        }
                        break;
                    case 81:
                        Gdx.graphics.setTitle("Cosmic 3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                connections[2] = ctr * 0.017;
                                connections[0] = x * 0.123 - y * 0.083 + ctr * 0.011;
                                connections[1] = y * 0.123 - x * 0.083 + ctr * 0.011;
                                connections[2] = 1.09 * (connections[1] = 1.03 * (connections[0] = 0.98 * (bright =
                                        NumberTools.intBitsToFloat((((cosmos.getInt() >> 5) * 41) >>> 9) | 0x40000000))));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                    /*
                                        case 2:
                        Gdx.graphics.setTitle("LightRNG");
                        random = new LightRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 3:
                        Gdx.graphics.setTitle("XorRNG");
                        random = new XorRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 4:
                        Gdx.graphics.setTitle("XoRoRNG");
                        random = new XoRoRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("PermutedRNG");
                        random = new PermutedRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("LongPeriodRNG");
                        random = new LongPeriodRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 7:
                        Gdx.graphics.setTitle("IsaacRNG");
                        random = new IsaacRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX");
                        gdxRandom = new RandomXS128(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(gdxRandom.nextInt());
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        break;

                     */
                }
            }
            break;
            case 5: { //RNG mode
                switch (rngMode) {
                    case 0:
                        jreRandom.setSeed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = jreRandom.nextInt() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("java.util.Random at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 1:
                        thunder.reseed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = thunder.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("ThunderRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 2:
                        light.setState(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = light.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("LightRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 3:
                        xor.setSeed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = xor.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("XorRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 4:
                        xoRo.setSeed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = xoRo.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("XoRoRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 5:
                        permuted.setState(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = permuted.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("PermutedRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 6:
                        longPeriod.reseed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = longPeriod.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("LongPeriodRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 7:
                        isaac.regen();
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = isaac.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("IsaacRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 8:
                        gdxRandom.setSeed(ctr * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = gdxRandom.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 9:
                        jreRandom.setSeed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = jreRandom.nextInt() >>> 24;
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("java.util.Random at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 10:
                        thunder.reseed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = thunder.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("ThunderRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 11:
                        light.setState(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = light.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("LightRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 12:
                        xor.setSeed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = xor.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("XorRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 13:
                        xoRo.setSeed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = xoRo.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("XoRoRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 14:
                        permuted.setState(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = permuted.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("PermutedRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 15:
                        longPeriod.reseed(ctr * 0xD0666BE7L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = longPeriod.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("LongPeriodRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 16:
                        isaac.regen();
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = isaac.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("IsaacRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 17:
                        gdxRandom.setSeed(ctr * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = gdxRandom.nextInt() >>> 24;
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 18:
                        pint.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = pint.nextInt() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("PintRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 19:
                        pint.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = pint.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("PintRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 20:
                        lap.setState0(ctr * 0x9E3779B9L + 0xAC8C0FE02D14624DL);
                        lap.setState1((~ctr * 0xAC8C0FE02D14624DL) * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = lap.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("LapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 21:
                        lap.setState0(ctr * 0x9E3779B9L + 0xAC8C0FE02D14624DL);
                        lap.setState1((~ctr * 0xAC8C0FE02D14624DL) * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = lap.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("LapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 22:
                        flap.setState(ctr * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = flap.nextInt() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("FlapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 23:
                        flap.setState(ctr * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = flap.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("FlapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 24:
                        mr.setState(ctr);
                        //mr.mul += 2;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = (mr.nextLong() & 0xFFFFFF00L) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("MicroRandom (edited) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 25:
                        mr.setState(ctr);
                        //mr.mul += 2;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = (int)(mr.nextLong() & 0xFF);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("MicroRandom (edited) at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 26:
                        slap.setState(ctr * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = slap.nextInt() | 255L; // (FlapRNG.determine(state += 0x9E3779B9 ^ (state << 1))) << 8 | 255L
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("SlapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 27:
                        slap.setState(ctr * 0x9E3779B9L);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = slap.nextInt() >>> 24;//toFloat(FlapRNG.determine(state += 0x9E3779B9 ^ (state << 1)));
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("SlapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 28:
                        horde.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = horde.nextLong() | 255L; // (FlapRNG.determine(state += 0x9E3779B9 ^ (state << 1))) << 8 | 255L
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("HordeRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 29:
                        horde.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = horde.next(8);//toFloat(FlapRNG.determine(state += 0x9E3779B9 ^ (state << 1)));
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("HordeRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 30:
                        herd.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = herd.nextInt() | 255L; // (FlapRNG.determine(state += 0x9E3779B9 ^ (state << 1))) << 8 | 255L
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("HerdRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 31:
                        herd.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = herd.nextInt() >>> 24;//toFloat(FlapRNG.determine(state += 0x9E3779B9 ^ (state << 1)));
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("HerdRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 32:
                        zap.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = zap.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("ZapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 33:
                        zap.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = zap.next(8);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("ZapRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 34:
                        bird.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = bird.nextInt() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("BirdRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 35:
                        bird.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = bird.nextInt() >>> 24;
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("BirdRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 36:
                        beard.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = beard.nextLong() | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("BeardRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                    case 37:
                        beard.setState(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = beard.nextInt() >>> 24;
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        Gdx.graphics.setTitle("BeardRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                        break;
                }
            }
            break;
            case 2: {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = storm.hash(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = storm.hash(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = storm.hash64(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = storm.hash64(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 7L;
                                code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = storm.hash(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = storm.hash(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = storm.hash64(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = storm.hash64(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 1792L;
                                code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                }
            }
            break;
            default: // artistic, just one
            {
                Gdx.graphics.setTitle("Conway's Game Of Life at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                ca.current.intoChars(display.contents, ' ', '\0');
                ca.runGameOfLife();
            }
        }
        //colorFactory.clearCache();
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        if (testType == 3 || keepGoing) {
            ctr++;
            putMap();
        }
        // if the user clicked, we have a list of moves to perform.

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        // stage has its own batch and must be explicitly told to draw().
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
        //display = new SquidPanel(this.width, this.height, cellWidth, cellHeight);
        //Gdx.graphics.requestRendering();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Hash Visualization";
        config.width = width;
        config.height = height;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new HashVisualizer(), config);
    }
}
