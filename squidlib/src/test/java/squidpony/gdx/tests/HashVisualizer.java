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
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.IFilter;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Tommy Ettinger on 8/20/2016.
 */
public class HashVisualizer extends ApplicationAdapter {
    private SpriteBatch batch;
    private SquidColorCenter colorFactory;
    private SquidPanel display, overlay;
    private int width, height;
    private int cellWidth, cellHeight;
    private SquidInput input;
    private static final SColor bgColor = SColor.BLACK;
    private Stage stage;
    private Viewport view;
    private int hashMode = 0, rngMode = 0, noiseMode = 1;
    private CrossHash.Sip sipA;
    private CrossHash.Storm stormA, stormB, stormC;
    private int testType = 4;
    private RandomnessSource fuzzy, random;
    private Random jreRandom;
    private RandomXS128 gdxRandom;
    private long seed;

    public static double toDouble(long n)
    {
        return Double.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
        //return Double.longBitsToDouble(0x3FF0000000000000L | n >>> 12) - 1.0;
    }

    public static float toFloat(int n)
    {
        return (Float.intBitsToFloat(0x3F800000 | n >>> 9) - 1.0f);
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
    public static int rawNoise0(int alpha, int beta)
    {
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
                rawNoise(x + 1, y) + rawNoise(x - 1, y) + rawNoise(x, y + 1) + rawNoise(x, y - 1)/* +
                 + rawNoise(x + 1, y+1) + rawNoise(x - 1, y-1) + rawNoise(x-1, y + 1) + rawNoise(x+1, y - 1)*/
                 /* >> 1) +
                rawNoise(x + 2, y) + rawNoise(x - 2, y) + rawNoise(x, y + 2) + rawNoise(x, y - 2) +
                rawNoise(x + 2, y+2) + rawNoise(x - 2, y-2) + rawNoise(x-2, y + 2) + rawNoise(x+2, y - 2) +
                rawNoise(x + 2, y+1) + rawNoise(x - 2, y+1) + rawNoise(x+1, y + 2) + rawNoise(x+1, y - 2) +
                rawNoise(x + 2, y-1) + rawNoise(x - 2, y-1) + rawNoise(x-1, y + 2) + rawNoise(x-1, y - 2)*/
        ) >> 3;
    }

    public static float discreteNoise(int x, int y, float zoom) {
        //int n = rawNoise(x, y), t = n << 4;
        final float alef = x / zoom, bet = y / zoom;
        final int alpha = (int) (alef), beta = (int) (bet);
        final float aBias = (alef - alpha)+1, bBias = (bet - beta)+1;
        final int a0 = (int) (aBias - 0.75f)-1, a1 = (int) (aBias + 0.75f)-1,
                a2 = (int) (aBias - 0.25f)-1, a3 = (int) (aBias + 0.25f)-1,
                b0 = (int)(bBias - 0.75f)-1, b1 = (int) (bBias + 0.75f)-1,
                b2 = (int)(bBias - 0.25f)-1, b3 = (int) (bBias + 0.25f)-1;
        //midBias = (2f - Math.abs(1f - aBias) - Math.abs(1f - bBias)), //(rawNoise(alpha, beta) << 2) +
        return  (rawNoise(alpha + a0, beta) + rawNoise(alpha + a1, beta) +
                rawNoise(alpha, beta + b0) + rawNoise(alpha, beta + b1) +
                rawNoise(alpha + a2, beta) + rawNoise(alpha + a3, beta) +
                rawNoise(alpha, beta + b2) + rawNoise(alpha, beta + b3)
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
        ) * 0.00048828125f;//0.00078125f;//;//0.000244140625f;//0.001953125f; //0.0009765625f; // 0.00048828125f;
    }

    public static int rawNoise(int x, int y) {
        //final int mx = x * 17 ^ ((x ^ 11) + (y ^ 13)), my = y * 29 ^ (7 + x + y),
        final int mx = (x * 0x9E37 ^ y * 0x7C15) + (y * 0xA47F + x * 0x79B9), my = (y * 0xA47F ^ x * 0x79B9) ^ (x * 0x9E37 + y * 0x7C15),
                gx = mx ^ (mx >> 1), gy = my ^ (my >> 1),
                out = ((gx + gy + (gx * gy)) >>> 4 & 0x1ff); //((Integer.bitCount(gx) + Integer.bitCount(gy) & 63) << 3) ^
        return ((out & 0x100) != 0) ? ~out & 0xff : out & 0xff;
    }


    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 512;
        height = 512;
        cellWidth = 1;
        cellHeight = 1;
        display = new SquidPanel(width, height, 1, 1);
        overlay = new SquidPanel(16, 8, DefaultResources.getStretchableFont().width(32).height(64).initBySize());
        IFilter<Color> filter0 = new Filters.PaletteFilter(
                new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,                           0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f},
                new float[]{0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,},
                new float[]{0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f, 0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f},
                new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,}),
        filter1 = new Filters.PaletteFilter(SColor.YELLOW_GREEN_SERIES),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
        filter2 = new Filters.PaletteFilter(new SColor[]{SColor.TREE_PEONY, SColor.NAVAJO_WHITE, SColor.BELLFLOWER, SColor.CAPE_JASMINE, SColor.CELADON, SColor.DAWN, SColor.TEAL}),
        filter3 = new Filters.GrayscaleFilter(),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
        filter4 = new Filters.PaletteFilter(new SColor[]{SColor.NAVAJO_WHITE, SColor.CAPE_JASMINE, SColor.LEMON_CHIFFON, SColor.PEACH_YELLOW}),
        filter5 = new Filters.PaletteFilter(new SColor[]{SColor.CORAL_RED, SColor.MEDIUM_SPRING_GREEN, SColor.PSYCHEDELIC_PURPLE, SColor.EGYPTIAN_BLUE});
        colorFactory = new SquidColorCenter();
        sipA = new CrossHash.Sip();
        stormA = CrossHash.Storm.alpha;
        stormB = CrossHash.Storm.beta;
        stormC = CrossHash.Storm.chi;
        fuzzy = new ThunderRNG(0xBEEFCAFEF00DCABAL);
        view = new ScreenViewport();
        stage = new Stage(view, batch);
        seed = 0xBEEFF00DCAFECABAL;


        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case SquidInput.ENTER:
                        if(testType == 4) {
                            noiseMode++;
                            noiseMode &= 3;
                        }
                        else if(testType == 5) {
                            rngMode++;
                            rngMode %= 18;
                        }
                        else {
                            hashMode++;
                            hashMode %= 28;
                        }
                        putMap();
                        //Gdx.graphics.requestRendering();
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
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                    }
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        overlay.setPosition(0, 0);
        Stack stk = new Stack(display, overlay);
        stage.addActor(stk);
        stk.layout();
        putMap();
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }
    public void putMap()
    {
        display.erase();
        overlay.erase();
        int[] coordinates = new int[2], coordinate = new int[1];
        long code;
        float bright;
        int iBright;
        switch (testType) {
            case 1: {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
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
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                }
            }
            break;
            case 4: { //Noise mode
                switch (noiseMode) {
                    case 0:
                        Gdx.graphics.setTitle("Perlin Noise");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = ((float) PerlinNoise.noise(x, y) + 1.0f) * 0.5f;
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 1:
                        Gdx.graphics.setTitle("Discrete Noise, no zoom");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iBright = discreteNoise(x, y);
                                display.put(x, y, colorFactory.get(iBright, iBright, iBright));
                            }
                        }
                        break;
                    case 2:
                        Gdx.graphics.setTitle("Discrete Noise, x2 zoom");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = discreteNoise(x, y, 2f);
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 3:
                        Gdx.graphics.setTitle("Discrete Noise, x3.7 zoom");
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = discreteNoise(x, y, 3.7f);
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
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
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 3:
                        Gdx.graphics.setTitle("XorRNG");
                        random = new XorRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 4:
                        Gdx.graphics.setTitle("XoRoRNG");
                        random = new XoRoRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("PermutedRNG");
                        random = new PermutedRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("LongPeriodRNG");
                        random = new LongPeriodRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 7:
                        Gdx.graphics.setTitle("IsaacRNG");
                        random = new IsaacRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX");
                        gdxRandom = new RandomXS128(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(gdxRandom.nextInt());
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
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
                        Gdx.graphics.setTitle("java.util.Random");
                        jreRandom = new Random(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = (jreRandom.nextInt() << 8) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 1:
                        Gdx.graphics.setTitle("ThunderRNG");
                        random = new ThunderRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 2:
                        Gdx.graphics.setTitle("LightRNG");
                        random = new LightRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 3:
                        Gdx.graphics.setTitle("XorRNG");
                        random = new XorRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 4:
                        Gdx.graphics.setTitle("XoRoRNG");
                        random = new XoRoRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 5:
                        Gdx.graphics.setTitle("PermutedRNG");
                        random = new PermutedRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 6:
                        Gdx.graphics.setTitle("LongPeriodRNG");
                        random = new LongPeriodRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 7:
                        Gdx.graphics.setTitle("IsaacRNG");
                        random = new IsaacRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = random.next(24) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 8:
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX");
                        gdxRandom = new RandomXS128(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                code = (gdxRandom.nextInt() << 8) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 9:
                        Gdx.graphics.setTitle("java.util.Random");
                        jreRandom = new Random(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(jreRandom.nextInt());
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 10:
                        Gdx.graphics.setTitle("ThunderRNG");
                        random = new ThunderRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 11:
                        Gdx.graphics.setTitle("LightRNG");
                        random = new LightRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 12:
                        Gdx.graphics.setTitle("XorRNG");
                        random = new XorRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 13:
                        Gdx.graphics.setTitle("XoRoRNG");
                        random = new XoRoRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 14:
                        Gdx.graphics.setTitle("PermutedRNG");
                        random = new PermutedRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 15:
                        Gdx.graphics.setTitle("LongPeriodRNG");
                        random = new LongPeriodRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 16:
                        Gdx.graphics.setTitle("IsaacRNG");
                        random = new IsaacRNG(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(random.next(32));
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                    case 17:
                        Gdx.graphics.setTitle("RandomXS128 from LibGDX");
                        gdxRandom = new RandomXS128(seed);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bright = toFloat(gdxRandom.nextInt());
                                display.put(x, y, colorFactory.get(bright, bright, bright, 1f));
                            }
                        }
                        break;
                }
            }
            break;
            default:
            {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = sipA.hash(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = sipA.hash(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = sipA.hash64(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = sipA.hash64(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = sipA.hash(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = sipA.hash(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = sipA.hash64(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinates[0] = (((x + fuzzy.next(2)) >>> 2) << 3);
                                coordinates[1] = (((y + fuzzy.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = sipA.hash64(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + fuzzy.next(2)) >>> 2) << 9) | ((y + fuzzy.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(fuzzy.next(2)), SColor.MIDORI);
                        break;
                }
            }
        }

    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glEnable(GL20.GL_BLEND);
        view.apply(true);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        if(testType == 3)
            putMap();

        // if the user clicked, we have a list of moves to perform.

        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }

        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.width = width;
        this.height = height;
        view.update(width, height, true);
        //display = new SquidPanel(this.width, this.height, cellWidth, cellHeight);
        //Gdx.graphics.requestRendering();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Hash Visualization";
        config.width = 512;
        config.height = 512;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new HashVisualizer(), config);
    }
}
