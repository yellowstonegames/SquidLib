package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.IFilter;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.Random;

import static squidpony.squidgrid.gui.gdx.Filters.Utility.floatGet;
import static squidpony.squidgrid.gui.gdx.Filters.Utility.floatGetI;

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
    private int width, height;
    private int cellWidth, cellHeight;
    private SquidInput input;
    private static final SColor bgColor = SColor.BLACK;
    private Stage stage;
    private Viewport view;
    private int hashMode = 30, rngMode = 0, noiseMode = 10;
    private CrossHash.Storm storm, stormA, stormB, stormC;
    private CrossHash.Chariot chariot, chariotA, chariotB, chariotC;
    private final int[] coordinates = new int[2];
    private final int[] coordinate = new int[1];

    // 0 commonly used hashes
    // 1 variants on Storm and other hashes
    // 3 artistic visualizations of hash functions
    // 4 noise
    // 5 RNG results
    private int testType = 0;

    private RandomnessSource fuzzy, random;
    private Random jreRandom;
    private RandomXS128 gdxRandom;
    private long seed;
    private int ctr = 0;
    private boolean keepGoing = true;

    public static double toDouble(long n) {
        return Double.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
        //return Double.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
    }

    public static float toFloat(int n) {
        return (Float.intBitsToFloat(0x3F800000 | n >>> 9) - 1.0f);
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


    @Override
    public void create() {
        batch = new SpriteBatch();
        width = 512;
        height = 512;
        cellWidth = 1;
        cellHeight = 1;
        display = new SquidPanel(width, height, cellWidth, cellHeight);
        //overlay = new SquidPanel(16, 8, DefaultResources.getStretchableFont().width(32).height(64).initBySize());
        IFilter<Color> filter0 = new Filters.PaletteFilter(
                new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f},
                new float[]{0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,},
                new float[]{0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f, 0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f},
                new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,}),
                filter1 = new Filters.PaletteFilter(SColor.YELLOW_GREEN_SERIES),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
                filter2 = new Filters.PaletteFilter(new SColor[]{SColor.TREE_PEONY, SColor.NAVAJO_WHITE, SColor.BELLFLOWER, SColor.CAPE_JASMINE, SColor.CELADON, SColor.DAWN, SColor.TEAL}),
                filter3 = new Filters.GrayscaleFilter(),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
                filter4 = new Filters.PaletteFilter(new SColor[]{SColor.NAVAJO_WHITE, SColor.CAPE_JASMINE, SColor.LEMON_CHIFFON, SColor.PEACH_YELLOW}),
                filter5 = new Filters.PaletteFilter(new SColor[]{SColor.CORAL_RED, SColor.MEDIUM_SPRING_GREEN, SColor.PSYCHEDELIC_PURPLE, SColor.EGYPTIAN_BLUE});
        colorFactory = new SquidColorCenter();
        storm = new CrossHash.Storm();
        stormA = CrossHash.Storm.alpha;
        stormB = CrossHash.Storm.beta;
        stormC = CrossHash.Storm.chi;
        chariot = new CrossHash.Chariot();
        chariotA = CrossHash.Chariot.alpha;
        chariotB = CrossHash.Chariot.beta;
        chariotC = CrossHash.Chariot.chi;
        fuzzy = new ThunderRNG(0xBEEFCAFEF00DCABAL);
        view = new ScreenViewport();
        stage = new Stage(view, batch);
        seed = 0xBEEFF00DCAFECABAL;


        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        switch (testType)
                        {
                            case 4:
                                noiseMode++;
                                noiseMode %= 16;
                                break;
                            case 5:
                                rngMode++;
                                rngMode %= 20;
                                break;
                            case 0:
                                hashMode++;
                                hashMode %= 45;
                                break;
                            case 1:
                                hashMode++;
                                hashMode %= 38;
                                break;
                            default:
                                hashMode++;
                                hashMode %= 28;
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
                        seed = fuzzy.nextLong();
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
        display.erase();
        //overlay.erase();
        long code;
        float bright;
        int iBright;
        int xx, yy;
        switch (testType) {
            case 1: {
                switch (hashMode) {
                    case 0:
                        Gdx.graphics.setTitle("Arrays.hashCode");
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
                        Gdx.graphics.setTitle("Storm (alpha)");
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
                        Gdx.graphics.setTitle("Storm (beta)");
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
                        Gdx.graphics.setTitle("Storm (chi)");
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
                        Gdx.graphics.setTitle("Arrays.hashCode");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("Storm (alpha)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("Storm (beta)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 7:
                        Gdx.graphics.setTitle("Storm (chi)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("Storm (alpha)");
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
                        Gdx.graphics.setTitle("Storm (beta)");
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
                        Gdx.graphics.setTitle("Storm (chi)");
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
                        Gdx.graphics.setTitle("Storm (alpha)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 12:
                        Gdx.graphics.setTitle("Storm (beta)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 13:
                        Gdx.graphics.setTitle("Storm (chi)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 14:
                        Gdx.graphics.setTitle("Arrays.hashCode");
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
                        Gdx.graphics.setTitle("Storm (alpha)");
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
                        Gdx.graphics.setTitle("Storm (beta)");
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
                        Gdx.graphics.setTitle("Storm (chi)");
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
                        Gdx.graphics.setTitle("Arrays.hashCode");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 19:
                        Gdx.graphics.setTitle("Storm (alpha)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 20:
                        Gdx.graphics.setTitle("Storm (beta)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 21:
                        Gdx.graphics.setTitle("Storm (chi)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 22:
                        Gdx.graphics.setTitle("Storm (alpha)");
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
                        Gdx.graphics.setTitle("Storm (beta)");
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
                        Gdx.graphics.setTitle("Storm (chi)");
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
                        Gdx.graphics.setTitle("Storm (alpha)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 26:
                        Gdx.graphics.setTitle("Storm (beta)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 27:
                        Gdx.graphics.setTitle("Storm (chi)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 28:
                        Gdx.graphics.setTitle("Arrays.hashCode");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 29:
                        Gdx.graphics.setTitle("Chariot (alpha)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = chariotA.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 30:
                        Gdx.graphics.setTitle("Chariot (beta)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = chariotB.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 31:
                        Gdx.graphics.setTitle("Chariot (chi)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = chariotC.hash(coordinate) & 0xFFFFFF00L | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 32:
                        Gdx.graphics.setTitle("Chariot (alpha)");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = chariotA.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 33:
                        Gdx.graphics.setTitle("Chariot (beta)");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = chariotB.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 34:
                        Gdx.graphics.setTitle("Chariot (chi)");
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = chariotC.hash(coordinates) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 35:
                        Gdx.graphics.setTitle("Chariot (alpha)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = chariotA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 36:
                        Gdx.graphics.setTitle("Chariot (beta)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = chariotB.hash(coordinate) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        break;
                    case 37:
                        Gdx.graphics.setTitle("Chariot (chi)");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = chariotC.hash(coordinate) << 8 | 255L;
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
                        colorFactory.clearCache();
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
                }
            }
            break;
            case 4: { //Noise mode
                switch (noiseMode) {
                    case 0:
                        Gdx.graphics.setTitle("Perlin Noise, x3 zoom at " + Gdx.graphics.getFramesPerSecond() +
                                " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Merlin Noise, no zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Merlin Noise, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Merlin Noise Alt, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Merlin Noise 3D, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = MerlinNoise.noise3D(x, y, ctr, 3);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("Merlin Noise Alt 3D, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = MerlinNoise.noise3D_alt(x, y, ctr, 3);
                                display.put(x, y, floatGetI(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("Merlin Noise Alt 3D Emphasized, x3 smooth zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Perlin 3D Noise, x3 zoom at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Perlin Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Perlin 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Whirling 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Whirling Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Whirling Alt 3D Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Whirling Alt Noise, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Whirling 3D Color Noise, one octave per channel at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        Gdx.graphics.setTitle("Whirling 3D Color Noise, one octave as all channels at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
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
                        jreRandom = new Random(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = (jreRandom.nextInt() << 8) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("java.util.Random at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 1:
                        random = new ThunderRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("ThunderRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 2:
                        random = new LightRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("LightRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 3:
                        random = new XorRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("XorRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 4:
                        random = new XoRoRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("XoRoRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 5:
                        random = new PermutedRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("PermutedRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 6:
                        random = new LongPeriodRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("LongPeriodRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 7:
                        random = new IsaacRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("IsaacRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 8:
                        gdxRandom = new RandomXS128(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = (gdxRandom.nextInt() << 8) | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 9:
                        jreRandom = new Random(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(jreRandom.nextInt());
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("java.util.Random at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 10:
                        random = new ThunderRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("ThunderRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 11:
                        random = new LightRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("LightRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 12:
                        random = new XorRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("XorRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 13:
                        random = new XoRoRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("XoRoRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 14:
                        random = new PermutedRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("PermutedRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 15:
                        random = new LongPeriodRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("LongPeriodRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 16:
                        random = new IsaacRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("IsaacRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 17:
                        gdxRandom = new RandomXS128(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(gdxRandom.nextInt());
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 18:
                        random = new PintRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, floatGet(code));
                            }
                        }
                        Gdx.graphics.setTitle("PintRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                    case 19:
                        random = new PintRNG(ctr);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, floatGet(bright, bright, bright, 1f));
                            }
                        }
                        Gdx.graphics.setTitle("PintRNG at " + Gdx.graphics.getFramesPerSecond()  + " FPS, cache size " + colorFactory.cacheSize());
                        break;
                }
            }
            break;
            default: {
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
        }
        colorFactory.clearCache();
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
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
        this.width = width;
        this.height = height;
        view.update(width, height, true);
        view.apply(true);
        //display = new SquidPanel(this.width, this.height, cellWidth, cellHeight);
        //Gdx.graphics.requestRendering();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Hash Visualization";
        config.width = 512;
        config.height = 512;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new HashVisualizer(), config);
    }
}
