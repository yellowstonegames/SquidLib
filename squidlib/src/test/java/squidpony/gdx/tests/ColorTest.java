package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidmath.MathExtras;
import squidpony.squidmath.NumberTools;

import static squidpony.StringKit.safeSubstring;
import static squidpony.squidgrid.gui.gdx.SColor.floatGet;
//import static squidpony.squidgrid.gui.gdx.SColor.floatGetYCwCm;

/**
 * Created by Tommy Ettinger on 12/27/2016.
 */
public class ColorTest extends ApplicationAdapter {
    /**
     * In number of cells
     */
    private static int gridWidth = 160;
//    private static int gridWidth = 256;
//    private static int gridWidth = 103;
//    private static int gridWidth = 140;
    /**
     * In number of cells
     */
    private static int gridHeight = 32;
//    private static int gridHeight = 256;
//    private static int gridHeight = 27;

    /**
     * The pixel width of a cell
     */
//    private static int cellWidth = 2;
    private static int cellWidth = 10;
    /**
     * The pixel height of a cell
     */
//    private static int cellHeight = 2;
    private static int cellHeight = 21;

    private static int totalWidth = 512, totalHeight = 512;
//    private static int totalWidth = gridWidth * cellWidth, totalHeight = gridHeight * cellHeight;



    private static final int[] RINSED = {
            0x00000000, 0x444444ff, 0x000000ff, 0x88ffff00, 0x212121ff, 0x00ff00ff, 0x0000ffff, 0x080808ff,
            0xff574600, 0xffb14600, 0xfffd4600, 0x4bff4600, 0x51bf6c00, 0x4697ff00, 0x9146ff00, 0xff46ae00,
            0xf8f9faff, 0xc4c3c5ff, 0x9c9c9dff, 0x757676ff, 0x616262ff, 0x4c484aff, 0x252626ff, 0x090304ff,
            0xd89789ff, 0xc4877aff, 0xb47b76ff, 0xa36c72ff, 0x905861ff, 0x76454cff, 0x5f3234ff, 0x452327ff,
            0xf9dcb8ff, 0xceb29aff, 0xb29891ff, 0x8f797fff, 0x75636fff, 0x554b67ff, 0x3e3552ff, 0x272340ff,
            0xeaa18dff, 0xcf9180ff, 0xb87c6bff, 0xa06a60ff, 0x905c59ff, 0x73474bff, 0x52383eff, 0x35242aff,
            0xf9ce8fff, 0xeba685ff, 0xc48772ff, 0xa96e5cff, 0x8a5a4bff, 0x7c4b49ff, 0x6a3e3dff, 0x572d2fff,
            0xeed8a1ff, 0xe7b38cff, 0xcc967fff, 0xb6776dff, 0x995a55ff, 0x803d49ff, 0x662139ff, 0x500328ff,
            0xfdfe9cff, 0xfdd7aaff, 0xe9bba4ff, 0xc9a09dff, 0xb7889aff, 0x957088ff, 0x755b7bff, 0x514265ff,
            0xfdf067ff, 0xfdbf60ff, 0xef995aff, 0xcc7148ff, 0xb65549ff, 0xa34547ff, 0x7d303fff, 0x61242fff,
            0xddbba4ff, 0xc0a68fff, 0x9f8871ff, 0x7f6b5cff, 0x6b5755ff, 0x5d464cff, 0x482f3dff, 0x30232dff,
            0xfef5e1ff, 0xe9dfd3ff, 0xcfc5baff, 0xbaafabff, 0xaaa291ff, 0x9a877bff, 0x816f69ff, 0x615d56ff,
            0xfef1a8ff, 0xe4ce85ff, 0xc9ad77ff, 0xb19169ff, 0x957859ff, 0x7b604cff, 0x60463bff, 0x472f2aff,
            0xfefc74ff, 0xe8d861ff, 0xcdad53ff, 0xb2893eff, 0x91672fff, 0x7d4f21ff, 0x693c12ff, 0x562810ff,
            0xfdfcb7ff, 0xfcfa3cff, 0xfad725ff, 0xf5b325ff, 0xd7853cff, 0xb25345ff, 0x8a2b2bff, 0x67160aff,
            0xcbd350ff, 0xb3b24bff, 0x9a9e3aff, 0x808b30ff, 0x647717ff, 0x4b6309ff, 0x305413ff, 0x272a07ff,
            0x8dc655ff, 0x7ba838ff, 0x6c8a37ff, 0x5d733aff, 0x4f633cff, 0x3f5244ff, 0x323d4aff, 0x232a45ff,
            0xadd54bff, 0x80b040ff, 0x599135ff, 0x35761aff, 0x2a621fff, 0x1e5220ff, 0x063824ff, 0x012b1dff,
            0xe8ffefff, 0xa9ddc0ff, 0x95c89cff, 0x91b48eff, 0x759983ff, 0x627f72ff, 0x4c655cff, 0x36514aff,
            0x91e49dff, 0x69c085ff, 0x4f8f62ff, 0x4a7855ff, 0x396044ff, 0x385240ff, 0x31413dff, 0x233631ff,
            0x09efd0ff, 0x07cca2ff, 0x03aa83ff, 0x038d75ff, 0x04726dff, 0x01585aff, 0x05454eff, 0x083142ff,
            0x97d6f9ff, 0x3eb0caff, 0x3c919fff, 0x0a737cff, 0x226171ff, 0x0b505fff, 0x0d3948ff, 0x052935ff,
            0x91fcfcff, 0x68dbfeff, 0x5cb1d5ff, 0x4c8caaff, 0x406883ff, 0x2b4965ff, 0x29324dff, 0x1c1e34ff,
            0x80d1fbff, 0x62b2e7ff, 0x4d96dbff, 0x267db9ff, 0x195f97ff, 0x114776ff, 0x0b355aff, 0x031d41ff,
            0xceeefdff, 0xcdd7feff, 0xa1aed7ff, 0x898caeff, 0x7c7196ff, 0x5e597cff, 0x404163ff, 0x26294cff,
            0x8391c1ff, 0x7181caff, 0x5e71beff, 0x555fa2ff, 0x424c84ff, 0x323b6dff, 0x2b325cff, 0x292349ff,
            0xe3d1fdff, 0xbaabfaff, 0x9f94e2ff, 0x9588d7ff, 0x7b71b3ff, 0x675e9cff, 0x4f4d7cff, 0x333158ff,
            0xd2bafbff, 0xbda5f0ff, 0xab90edff, 0x977ae8ff, 0x745dadff, 0x584481ff, 0x3f314cff, 0x221f24ff,
            0xf7c1e7ff, 0xd791c6ff, 0xbb6faaff, 0xaf6190ff, 0x924b76ff, 0x623155ff, 0x47253fff, 0x2f0e25ff,
            0xfdc7fbff, 0xfc9fc5ff, 0xfb71a9ff, 0xe6497eff, 0xc33c6bff, 0x933255ff, 0x68243fff, 0x3f122aff,
            0xfddddcff, 0xd1abb1ff, 0xb48c9aff, 0x9d7482ff, 0x8b5d6eff, 0x705057ff, 0x583c4bff, 0x421e29ff,
            0xfcd9fbff, 0xfdb8c7ff, 0xfd97aaff, 0xf46e7eff, 0xc65365ff, 0x9e303cff, 0x741b28ff, 0x50071aff,
}, FLESURRECT = {
            0x00000000, 0x1F1833FF, 0x2B2E42FF, 0x3E3546FF,
            0x414859FF, 0x68717AFF, 0x90A1A8FF, 0xB6CBCFFF,
            0xD3E5EDFF, 0xFFFFFFFF, 0x5C3A41FF, 0x826481FF,
            0x966C6CFF, 0x715A56FF, 0xAB947AFF, 0xF68181FF, //0xE3C896FF -> 0x715A56FF
            0xF53333FF, 0x5A0A07FF, 0xAE4539FF, 0x8A503EFF,
            0xCD683DFF, 0xFBA458FF, 0xFB6B1DFF, 0xDDBBA4FF, //0x9F8562FF -> 0xDDBBA4FF
            0xFDD7AAFF, 0xFFA514FF, 0xC29162FF, 0xE8B710FF, //0xF9C79FFF -> 0xFDD7AAFF
            0xFBE626FF, 0xC0B510FF, 0xFBFF86FF, 0xB4D645FF,
            0x729446FF, 0xC8E4BEFF, 0x45F520FF, 0x51C43FFF,
            0x0E4904FF, 0x55F084FF, 0x1EBC73FF, 0x30E1B9FF,
            0x7FE0C2FF, 0xB8FDFFFF, 0x039F78FF, 0x63C2C9FF,
            0x216981FF, 0x7FE8F2FF, 0x5369EFFF, 0x4D9BE6FF,
            0x28306FFF, 0x5C76BFFF, 0x4D44C0FF, 0x180FCFFF,
            0x53207DFF, 0x8657CCFF, 0xA884F3FF, 0x630867FF,
            0xA03EB2FF, 0x881AC4FF, 0xE4A8FAFF, 0xB53D86FF,
            0xF34FE9FF, 0x7A3045FF, 0xF04F78FF, 0xC93038FF, //0xC27182FF -> 0xC29162FF
    };

    private Stage stage;
    private SpriteBatch batch;
    private Viewport viewport;
    private TextCellFactory tcf;
    private SquidLayers display;
    private float[][] colors;
    private int hh = 0;
    private int vv = 0;
    private float luma = 0.5f;
    private Color tmp = new Color();

    private void show(float hue, float sat, float val)
    {
        display.putString(hh * 8, vv, "          ", SColor.BLACK,
                SColor.colorFromFloat(tmp, SColor.floatGetHSV(hue, sat, val, 1f)));
        System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
        if((vv = (vv + 1) % 6) == 0)
        {
            ++hh;
            System.out.println();
        }
    }
    public static float floatGetYCgCr(float luma, float chromaG, float chromaR, float opacity) {
        if (chromaR >= -0.0039f && chromaR <= 0.0039f && chromaG >= -0.0039f && chromaG <= 0.0039f) {
            return floatGet(luma, luma, luma, opacity);
        }
        float r = luma + chromaR * 1.402f;
        if(r < 0f || r > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        float g = luma + chromaG * 1.772f;
        if(g < 0f || g > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        float b = luma - chromaG * 0.344136f - chromaR * 0.714136f;
        if(b < 0f || b > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        return floatGet(r, g, b, opacity);
    }
    public static float floatGetYCbCr(float luma, float chromaB, float chromaR, float opacity) {
        if (chromaR >= -0.0039f && chromaR <= 0.0039f && chromaB >= -0.0039f && chromaB <= 0.0039f) {
            return floatGet(luma, luma, luma, opacity);
        }
        float r = luma + chromaR * 1.402f;
        if(r < 0f || r > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        float g = luma - chromaB * 0.344136f - chromaR * 0.714136f;
        if(g < 0f || g > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        float b = luma + chromaB * 1.772f;
        if(b < 0f || b > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        return floatGet(r, g, b, opacity);
    }
    public static float floatGetYCoCg(float y, float co, float cg, float opacity) {
        if (co >= -0.0039f && co <= 0.0039f && cg >= -0.0039f && cg <= 0.0039f) {
            return floatGet(y, y, y, opacity);
        }
        final float t = y - cg;
        float r = t + co;
        if(r < 0f || r > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        float g = y + cg;
        if(g < 0f || g > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        float b = t - co;
        if(b < 0f || b > 1f)
            return -0x1.fefefep125F;//SColor.CW_GRAY
        return floatGet(r, g, b,
                opacity);
    }
    public static float floatGetYCcCf(float luma, float cool, float full, float opacity) {
        if (full >= -0.0039f && full <= 0.0039f && cool >= -0.0039f && cool <= 0.0039f) {
            return floatGet(luma, luma, luma, opacity);
        }

        // luma is defined as (r * 4 + g * 8 + b * 4) / 16
        // or r * 0.25f + g * 0.5f + b * 0.25f
        // cool is the warm-cool axis, with positive cool between blue and green, negative cool between red and yellow
        // cool is defined as (r * -8 + b * 8) / 16
        // or b * 0.5f + g * 0f + r * -0.5f
        // full is a somewhat arbitrary axis, with positive full between blue and red, negative full between green and yellow
        // full is defined as (r * 4 - g * 8 + b * 4) / 16
        // or r * 0.25f - g * 0.5f + b * 0.25f

        // by is the diagonal from blue at 0.75 to yellow at -0.75
        // r * -0.25f + g * -0.5f + b * 0.75f
        // should be
        // r * -0.25f + g * -0.25f + b * 0.5f
        // gr is the diagonal from green at 0.75 to red at -0.75
        // r * -0.75f + g * 0.5f + b * 0.25f
        // should be
        // r * -0.5f + g * 0.5f

        // correct cool to
        // (r * -0.25f + g * -0.25f + b * 0.5f) - full
        // (r * -0.75f + g * 0.5f + b * 0.25f) + full
        // r * -0.25f + g * 0.375f - b * 0.125f // ranges from -0.375 to 0.375

        // correct full to
        // (r * -0.25f + g * -0.25f + b * 0.5f) - cool
        // r * 0f + g * -0.625f + b * 0.625f

        // (r * 0.75f + g * -0.5f + b * -0.25f) - cool


        // luma + by == b

        // luma - by
        // r * 0.5f + b * -0.5f

        // luma + gr
        // r * -0.5f + g * 1f + b * 0.5f

        // luma - gr == r

        final float by = (cool + full), gr = (cool - full);//, t = luma + luma + full - 0.5f;
        float r = luma - gr;//t - cool;//luma * 4 - cool * 0x0.8p0f + full * 0x0.3p0f;
        if(r < 0f || r > 1f) return -0x1.fefefep125F;//SColor.CW_GRAY
        float g = luma - full;//0.5f - full;
        if(g < 0f || g > 1f) return -0x1.fefefep125F;//SColor.CW_GRAY
        float b = luma + by;//t + cool;
        if(b < 0f || b > 1f) return -0x1.fefefep125F;//SColor.CW_GRAY
        return floatGet(r, g, b,
                opacity);
    }
//    public static float floatGetYCwCm(float luma, float warm, float mild, float opacity) {
//        // the color solid should be:
//        
//        //                   > warm >
//        // blue    violet     red
//        // cyan     gray      orange
//        // green    neon      yellow
//        //  \/ mild \/
//
//        // so, warm is effectively defined as the presence of red.
//        // and mild is, effectively, presence of green.
//        // negative warm or negative mild will each contribute to blue.
//        // luma is defined as (r * 3 + g * 4 + b) / 8
//        // or r * 0.375f + g * 0.5f + b * 0.125f
//        // warm is the warm-cool axis, with positive warm between red and yellow and negative warm between blue and green
//        // warm is defined as (r - b), with range from -1 to 1
//        // mild is the green-purple axis, with positive mild between green and yellow, negative mild between blue and red
//        // mild is defined as (g - b), with range from -1 to 1
//        
//        //r = (warm * 5 - mild * 4 + luma * 8) / 8; r5 - b5 - g4 + b4 + r3 + g4 + b1
//        //g = (mild * 4 - warm * 3 + luma * 8) / 8; g4 - b4 - r3 + b3 + r3 + g4 + b1
//        //b = (luma * 8 - warm * 3 - mild * 4) / 8; r3 + g4 + b1 - r3 + b3 - g4 + b4
//        final float r = MathExtras.clamp(luma + warm * 0.625f - mild * 0.5f, 0f, 1f),
//        g = MathExtras.clamp(luma + mild * 0.5f - warm * 0.375f, 0f, 1f),
//        b = MathExtras.clamp(luma - warm * 0.375f - mild * 0.5f, 0f, 1f);
//        return floatGet(r, g, b, opacity);
//    }

    /**
     * Gets a color as a packed float given floats representing luma (Y, akin to lightness), chroma warm (Cw, one of two
     * kinds of chroma used here), chroma mild (Cm, the other kind of chroma), and opacity. Luma should be between 0 and
     * 1, inclusive, with 0 used for very dark colors including but not limited to black, and 1 used for very light
     * colors including but not limited to white. The two chroma values range from -1.0 to 1.0, unlike YCbCr and YCoCg,
     * and also unlike those color spaces, there's some aesthetic value in changing just one chroma value. When warm is
     * high and mild is low, the color is more reddish; when both are low it is more bluish, and when mild is high and
     * warm is low, the color tends to be greenish, and when both are high it tends to be brown or yellow. When warm and
     * mild are both near 0.0f, the color is closer to gray. Because chroma values are centered on 0.0f, you can multiply
     * them by a value like 0.5f to halve the colorfulness of the color.
     * <br>
     * This method clamps the resulting color's RGB values, so any values can technically be given to this as luma,
     * warm, and mild, but they will only be reversible from the returned float color to the original Y, Cw, and Cm
     * values if the original values were in the range that {@link SColor#chromaWarm(float)},
     * {@link SColor#chromaMild(float)}, and {@link SColor#lumaYCwCm(float)} return.
     *
     * @param luma       0f to 1f, luma or Y component of YCwCm
     * @param warm       -1f to 1f, "chroma warm" or Cw component of YCwCm, with 1f more red or yellow
     * @param mild       -1f to 1f, "chroma mild" or Cm component of YCwCm, with 1f more green or yellow
     * @param opacity    0f to 1f, 0f is fully transparent and 1f is opaque
     * @return a float encoding a color with the given properties
     */
    public static float floatGetYCwCm(float luma, float warm, float mild, float opacity) {
        // the color solid should be:

        //                   > warm >
        // blue    violet     red
        // cyan     gray      orange
        // green    neon      yellow
        //  \/ mild \/

        // so, warm is effectively defined as the presence of red.
        // and mild is, effectively, presence of green.
        // negative warm with negative mild will each contribute to blue.
        // luma is defined as (r * 3 + g * 4 + b) / 8
        // or r * 0.375f + g * 0.5f + b * 0.125f
        // warm is the warm-cool axis, with positive warm between red and yellow and negative warm between blue and green
        // warm is defined as (r - b), with range from -1 to 1
        // mild is the green-purple axis, with positive mild between green and yellow, negative mild between blue and red
        // mild is defined as (g - b), with range from -1 to 1

        //r = (warm * 5 - mild * 4 + luma * 8) / 8; r5 - b5 - g4 + b4 + r3 + g4 + b1
        //g = (mild * 4 - warm * 3 + luma * 8) / 8; g4 - b4 - r3 + b3 + r3 + g4 + b1
        //b = (luma * 8 - warm * 3 - mild * 4) / 8; r3 + g4 + b1 - r3 + b3 - g4 + b4
        //// used in WarpWriter, not sure if optimal
//        return floatGet(
//                MathExtras.clamp(luma + warm * 0.625f, 0f, 1f),
//                MathExtras.clamp(luma + mild * 0.5f, 0f, 1f),
//                MathExtras.clamp(luma - warm * 0.375f - mild * 0.5f, 0f, 1f), opacity);

        /// was chosen as alternative, has issues because it has to change colors and has no identity state
//        return floatGet(
//                MathExtras.clamp(luma + warm * 0.5f, 0f, 1f),
//                MathExtras.clamp(luma + mild * 0.5f, 0f, 1f),
//                MathExtras.clamp(luma - warm * 0.25f - mild * 0.25f, 0f, 1f), opacity);

        //// original
        return floatGet(
                MathExtras.clamp(luma + warm * 0.625f - mild * 0.5f, 0f, 1f),
                MathExtras.clamp(luma + mild * 0.5f - warm * 0.375f, 0f, 1f),
                MathExtras.clamp(luma - warm * 0.375f - mild * 0.5f, 0f, 1f), opacity);

    }


    private void ycc(float y, float warm, float mild)
    {
        final int b = (int) ((warm + 1f) * 255.5f), r = (int) ((1f - mild) * 255.5f);
//        SColor.colorFromFloat(tmp, SColor.floatGetYCbCr(y, cb, cr, 1f));
//        display.putString(b, r, StringKit.hex(b) + "x" + StringKit.hex(r), y < 0.65f ? SColor.WHITE : SColor.BLACK,
//                tmp);
        float red = luma + warm * 0.625f - mild * 0.5f
          , green = luma + mild * 0.5f - warm * 0.375f
          ,  blue = luma - warm * 0.375f - mild * 0.5f
                ;
//        float red = luma + warm * 0.625f    
//          , green = luma + mild * 0.5f      
//          ,  blue = luma - warm * 0.375f - mild * 0.5f
//                ;
//        float red = luma + warm * 0.5f
//          , green = luma + mild * 0.5f
//          ,  blue = luma - warm * 0.25f - mild * 0.25f
//                ;
//        colors[b][r] = floatGetYCwCm(y, cb, cr, 1f);
        colors[b][r] =
                (red >= 0f && red <= 1f && green >= 0f && green <= 1f && blue >= 0f && blue <= 1f)
                ? floatGet(
                MathExtras.clamp(red, 0f, 1f),
                MathExtras.clamp(green, 0f, 1f),
                MathExtras.clamp(blue, 0f, 1f), 1f)
        : 0f;
//        System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
//        if((vv = ((vv + 1) & 7)) == 0)
//        {
//            ++hh;
//            System.out.println();
//        }
    }
    private void ycc(float[] components)
    {
        display.putString(hh * 8, vv, "          ", SColor.BLACK,
                SColor.colorFromFloat(tmp, SColor.floatGetYCbCr(components[0], components[1], components[2], 1f)));
        System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
        if((vv = ((vv + 1) & 7)) == 0)
        {
            ++hh;
            System.out.println();
        }
    }

    private void hsv(float[] components)
    {
        display.putString(hh * 8, vv, "          ", SColor.BLACK,
                SColor.colorFromFloat(tmp, SColor.floatGetHSV(components[0], components[1], components[2], 1f)));
        System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
        if((vv = ((vv + 1) & 7)) == 0)
        {
            ++hh;
            System.out.println();
        }
    }
    public static float vdc(final int base, final int index)
    {
        if(base <= 2) {
            return (Integer.reverse(index + 1) >>> 8) * 0x1p-24f;
        }
        float denominator = base, res = 0f;
        int n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            res += (n % base) / denominator;
            n /= base;
            denominator *= base;
        }
        return res;
    }
    public static float emphasize(final float a)
    {
        return a * a * (3f - 2f * a);
    }
    
    /*
    Y'= 0.2215*R' + 0.7154*G' + 0.0721*B'
    Cb=-0.1145*R' - 0.3855*G' + 0.5000*B'
    Cr= 0.5016*R' - 0.4556*G' - 0.0459*B'

    R'= Y' + 0.0000*Cb + 1.5701*Cr
    G'= Y' - 0.1870*Cb - 0.4664*Cr
    B'= Y' - 1.8556*Cb + 0.0000*Cr
     */
    public static float luma(final Color color)
    {
        return  (color.r * 0.2125f) +
                (color.g * 0.7154f) +
                (color.b * 0.0721f);
    }
    public static float chromaB(final Color color)
    {
        return (color.r * -0.1145f) +
                (color.g * -0.3855f) +
                (color.b * 0.5f);
    }
    public static float chromaR(final Color color)
    {
        return (color.r * 0.5016f) +
                (color.g * -0.4556f) +
                (color.b * -0.0459f);
    }

    public static float lumaYOG(final Color color)
    {
        return color.r * 0x8.Ap-5f + color.g * 0xF.Fp-5f + color.b * 0x6.1p-5f
                + 0x1.6p-5f - (Math.max(color.r, Math.max(color.g, color.b))
                   - Math.min(color.r, Math.min(color.g, color.b))) * 0x1.6p-5f
                ;
    }

    public static float coYOG(final Color color)
    {
        return color.r * 0x8p-4f /* + color.g * -0x1p-4f */ + color.b * -0x8p-4f;
    }

    public static float cgYOG(final Color color)
    {
        return color.r * -0x4p-4f + color.g * 0x8p-4f + color.b * -0x4p-4f;
    }
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        tcf = DefaultResources.getCrispSlabFont().width(1).height(1).initBySize();
        viewport = new StretchViewport(totalWidth, totalHeight);
        display = new SquidLayers(gridWidth, gridHeight, 1, 1, tcf);//.setTextSize(cellWidth + 1f, cellHeight + 1f);
        stage = new Stage(viewport, batch);
        colors = ArrayTools.fill(SColor.FLOAT_BLACK, 512, 512);
        //SquidColorCenter scc = DefaultResources.getSCC();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE)
                    Gdx.app.exit();
                return true;
            }
        });
        Gdx.graphics.setTitle("SquidLib Demo: Colors");
        SColor col = new SColor(0, 0, 0, 0);
        int[] palette = {
                0x00000000, 0x0F0813FF, 0x31383BFF, 0x64666FFF, 0x888F94FF, 0xD2DEE2FF, 0x9EAAB2FF, 0xD5D5D5FF,
                0x58363DFF, 0x896B88FF, 0x956B6BFF, 0x806965FF, 0xB29B81FF, 0xFF9B9BFF, 0xFF3D3DFF, 0x540300FF,
                0xAC4337FF, 0x894F3DFF, 0xCD673CFF, 0xF9A256FF, 0xC77930FF, 0xD0AE97FF, 0xE2BC8FFF, 0xF39908FF,
                0xAE7D4EFF, 0xF2C11AFF, 0xFFEF2EFF, 0xC0FFA6FF, 0x73F60EFF, 0x9BBD2CFF, 0x146D03FF, 0x819E77FF,
                0x4DFD28FF, 0x44B732FF, 0x8BD1BAFF, 0x52ED81FF, 0x23C178FF, 0x2EDFB7FF, 0x8DEED0FF, 0xC7FFFFFF,
                0x008F68FF, 0x7BDAE1FF, 0x196179FF, 0x68D1DBFF, 0x5D74FAFF, 0x4D9BE6FF, 0x2A3271FF, 0x4F69B2FF,
                0x574ECAFF, 0x140BCBFF, 0x5A2784FF, 0x8354C9FF, 0x9B77E6FF, 0x6A0F6EFF, 0xAF4DC1FF, 0x8113BDFF,
                0xFFC7FFFF, 0xAB337CFF, 0xFF5FFAFF, 0x6F243AFF, 0xE94771FF, 0xD0363EFF, 0xC3A5F4FF, 0x5A00BBFF,
        };
        final int COUNT = palette.length;
        final double THRESHOLD = 0.011; // threshold controls the "stark-ness" of color changes; must not be negative.
        byte[] paletteMapping = new byte[1 << 16];
        int[] reverse = new int[COUNT];
        byte[][] ramps = new byte[COUNT][4];
        float[] lumas = new float[COUNT], cos = new float[COUNT], cgs = new float[COUNT];
        //String[] names = new String[COUNT];
        final int yLim = 63, coLim = 31, cgLim = 31, shift1 = 6, shift2 = 11;
//        final OrderedSet<Color> flesurrectSet = new OrderedSet<>(COUNT);
//        for (int i = 0; i < COUNT; i++) {
//            col.set(FLESURRECT[i]).clamp();
//            flesurrectSet.add(col.cpy());
//        }
//        flesurrectSet.sort(new Comparator<Color>() {
//            @Override
//            public int compare(Color o1, Color o2) {
//                float s1 = SColor.saturation(o1), s2 = SColor.saturation(o2);
//                return s1 < 0.22 && s2 < 0.22 ? (int)Math.signum(SColor.value(o1) - SColor.value(o2))
//                        : s1 < 0.22 ? -1 : SColor.saturation(o2) < 0.22 ? 1
//                        : (int)Math.signum(SColor.hue(o1) - SColor.hue(o2));
//            }
//        }, 1, COUNT);
        for (int i = 1; i < COUNT; i++) {
            //col.set(flesurrectSet.getAt(i));
            //FLESURRECT[i] = Color.rgba8888(col);
            col.set(palette[i]);
            //names[i] = col.name;
            reverse[i] =
                    (int) ((lumas[i] = lumaYOG(col)) * yLim)
                            | (int) (((cos[i] = coYOG(col)) + 0.5f) * coLim) << shift1
                            | (int) (((cgs[i] = cgYOG(col)) + 0.5f) * cgLim) << shift2;
            if(paletteMapping[reverse[i]] != 0)
                System.out.println("color #" + i + ", " + col.name + ", overlaps an existing color, " + (int)paletteMapping[reverse[i]] + "!");
            paletteMapping[reverse[i]] = (byte) i;
        }
//        //this gives the possibility of storing 32KB of paletteMapping as a 9046-char String.
//        try {
//            paletteMapping = LZSPlus.decompress("Ղ屚̀筰咘㈐ᔕ㛀淍〳弐ഈ焣仯ၒ怫䴢嘤幐ଉ䄞坤䴼┷〒㘬紙䕫䦱又潥巇ᝐᣜࠧ笁ശၢ㉴䎆㘽愷ᓋ⽰ޙ嗌⁕懣䤠\u1C9Dᦓ䜵ᔕ烞䦀္困\u1259ᶽ\u2E74\u0379ᆢ䮬梠ɜ☙儣楡ა嶤㉁䋡瘦ƙᒱ䥉唄ښ✫ᘻ䧁䐷斪帠緖氠ᄇ幹⫎嚇㐠㠕\u08BAƓ⎵壁殉疕筋ͣʁ笚燡啩᱉习ƾ洠䲃䋲濥㐱姝\u0C5B㞖䰠棇兤ă稚㽞❍⾎㞛䈋壜潐ᐅ窽✫䁠\u0383成惠ࣸ狙⑱甦娰䟪\u0DE4/㔢ᑨ⏲撆暯≡埑‡㾂ݼ⨷䝒\u009B孍乆㧐斬佸⦴擳ଡ槸捉䯧夼坈䠢澙懟ᅒ努怨儫୮[批ᝫ㌙ㅳ孊䕕㈸暊ᛩ▷呂狾ㅸ筝咠Ⴔ槦然染⾄淖䇂䩗\u0DEE珞䤕㶾哗澴ᔴ䀪ᤌ掣ᄇ性綧懯㟿Р„婞ᣤ嵩劍᪤㐁嚛\u1ACD䨜䆈ṏυ囜崔_᭵㧾⎋ӱ・☠忁旑㛖搖玹浯㦻徰̀᠕䎙業ܣ䤓⼿䜠㠎矞仛ො烛伥㻣〧懎⏍林怎ޠᐙ粟ཇ䅱ṿᵷ焅川䠗䈖乏嘐則冑タ䇐ĉ௮Ѿ㿿稡篩磞㟢՚⊫竡硗с凩݁巵ݞ瑔穡珸᭡ᱏ\u0601翵失托Ӟ+盥备ว䢷䥧ᶊ᭤Ӣᑤ䣧滤㣎ဣф伵䎫㔳∤簴Ꮀ䀣䥲厨ଲⲇ༢劔䳃犂䯢6㣪溬㮓⨊⫐\u0098᭙禉,䥅⓰⊃房ˀ偕 皤⚠p䆐ⷰ\u2455ڱٜ䞓尷䵰ᙟ丩婈⼪兛䖩兤i㹊⛠㙍≫㨴䕫ᆇ䕑ṿӈᨭ䄩ⅱ⺩Ԧ⼐废☰禞⿑ูҩ䕬⊪帶⾡㼿搩१ⴋ捑⪸煜②ର浨⭁۱奖摐笩⎸㼥⁻\u0B7D⋸፳❻洧⋻紵枊䐽柺ʴ濹潆渉獥䋙ᙑט挭橛↹撛宓瀘撰Ⴛָ\u171A催Ꭴ⾏ᕥ䂹甧⊸偅簯剄䣀ᠭƧ盅䊸\u1C85䲮嗴峀⏭ヶᮘ溣ჵⲵ嘔皭ユ⃩嗦磵囔㛔ゕഊ嚇䷗凇泩Ἱ㶶\u08C6拘㠆窧ᣅໆม䇎\u193C⣾冽甞㘶狲㕭槙㎆ᶪ堤㷃ᬜ䷑惽ᮾ廍嶼㝷䜖彍掷ၝ竿Ꭽ摼\u18F7䯊瘝\u087D煢伍掜䯟㛜㼏尖⑬㍤屹ᤖと㿗㟃ખ⁹ম志瞯懝\u08D2⊊㣃㉬\u0BE2ਂ㩯䲉ଣ恥ข籱䡃䪀ल婮侜仏ဂ䙼\u0EEF◦䶂抓㡽\u2E74੭淧㣝瘝獲㷣䰎\u2E6C䶮籨礏揾狓ᶽ亿沓䷳ϡ㫃௱䵓ࠑ䣒噽䭞\u1BFB\u0AFE➾䦘@ᇈ\u0DED\u2063祸\"濟堏̥თ\u20C5梠¯塉⢙ⱳ䔡Ȱ囏䃿搵䥭憸ࠧ⛆\u2BB5ሡ敼唺無႟暴絬喂勉⪨䃵ⅳ䃾嘡ᣎㄘ\u16F4䑂ℊ嬥䡲ܐ\u13F7䕖僚∦\u089AĴ危咺寁暿氝ၧཱྀ⃖\u09CF䣗⇔❷瓮嫂汑Íǵ氶䂱禥ᇣ⍂筨ವኁᒑ棈弫⤊䃱䴹䙧⸩檧甾䃤㈻棂㗴絹㞘҅ғ⏔à\u08A1堻ൃ䞸幗掊㈔⢎䚈䗂\u31E7಼㟧籲宒٤\u0B9B\u0A34冁哧ल兆⥲㕅◐Ⓒ泖玐䦆嚋圐䚲瓵ⰺᵸ牯䞺ὐ䭻⍩\u0CBA⥼悲盵䳙ᛆ扬媞ˑ㙢㙄咹㙅ᄰ疽氻Ѐ䶦煤兆亞亐䆶亾枌ᦡ涩Ὸ\u0DD5夻᳭\u1C95懜獀滫ᒘᗭ㍝単ᚖ枧ᦔ㹨痨䬋ᦧḍ䝄獮⪚䂴㥼泗➈\u1259⽬廻䲼桢屻嘅㼯犌巯岽䲙㴇Ỹ➥䃍壞㰅乳㭧\u2B95澁焎䷥滢₪㏷⒘䷘ᓂ䢂檲挖ጂ㧨ẘ婝ᷤ岨喛㍙窩ᢵጒ⥥؉┷䪅\u20C2㳴䴛垣ᚏஷ椴ⱒ䩴㺧櫄噥⒙㻩禅砪帐㾄\u08C1吣牊勊ᠣ緿䰛₌ѥ㴤Ėߞ终啈ᴜ䈣⫕㰘畯Ⰺ欤\u1AFCഡ㼡ơⷋ㿤䊬Ấ硲埥ᨬႢ凁㝾䩦唝⡒ΝⱢ檠㔀䠫ᒞ୰僴ᅚౣ㫁呪ؠ恊䛄ଭヰ䊐ูᩉ元❸㔒䣤⚵ǍⱣ崨\u009A紵㇒ᝂά儴㪃ᑢ惘殬ృদႺㆆᘐ淂ผ؈䙜䚘篖碿ਘ䮬ᶋࠨṎᏑ俕䵞መ牬ԗ\u0C54䚆वዛ磵䳲㫃⇆眺㥮⺨俩⊰㶕ᑌേ䲹ὅ奅盔緀ْ䲉ㆀ経䔛楍勿⼗䗞瞞瘎㢖䩌暅楶丛ު泱ᐬ奡抬拑⍫油ᡛ䡓᾽珱娆⭙䏜ᐾ૫⬣乌ᓥ癐刪౬૫䗻ẑಱ䟭䋮⪖波ɯᦓ罀\u0A8E㲮㺕ᑜ喅ㆨ⨾۠搈實粀✞∪↖ৎⷫ坮⨢繙勸弙㣅䮣ᜪ攃ඩ碽瞖᠎㉶卸㍼䯰䫈楬⾫॒纶垜ⰲ㡻☔嗅嚚✄仯波乹⨑狫\u1ADEȃ幸㿝ੴ\u2BAF屷㳳䶩Άአ妢⃐ቁ㪄\u2BE2\u2BD9⟮䓃䯅穻䫇䏕棺ǐ\u0ACAօ⩕㍴灔Х\"ⶥԦ嗟砞㫓㕙㹢禙⬪減䶉Ⳛ攢惦\u008Bշ䯐\u0FE1⫌橪揎ⶋ圊倭嫞寡潳異᧷ᐤ䍆燌ᢐ㗦峿囤瀕擴ŋ㒢ऩ䏶檥継˰䥴ℎݭ犫ชˣ呔眀\u2BA4䨸ɨ䚈塌毈淞\u2DA7⏃䝠㬧⢔䮉㔵⇴囶瀛ᯚ䂣ৗ⏞掞投璏棽䴋\u0D5B\u0A0Dゔ嶰䛺獵㭰#\u0E71猊懢矣\u2E6Dᱵ沩\u2BA9㪓匭⓳ߗⴗ䔿䙡䳃⎗\u0DC7ㇳ\u0EC7弈哯㩹猔礫盞渋ᐜ\u209D濼⦤㰲翔⻁!忥栭،೫伋ᘨ搣ᑜ盓俨炃ⲙ䝻秺⛁䂕殻㘹䑔䎯味㭏㍶⡘䬮ྉ攼⧉燃⤎㞾昌ᮧ⚞╙繹獷ଁৱᯣ♈稅洒ᡵ枷џឨ䩧笹㜸縳ⵛ䍚㸿ᶵ䜱㋙\u0CFB曩暓ᦧ仯䙹ኙ砪灠徧Ẩ帒়䳊姒祯⩊ṕ犝噾䨻旭ӫ㻗埨䫉ⳓ漑╂凪pֲ牶勳䦅䭋䗺ၫⓙᾣ烪ᦊᚏ䃫柩䡅䀦攏攙☚\u2B7F䳝帲‡撇矗\u0FE5K畷ଆ喵䕹喺巀堛ℕ㿊9֟䛉Ə棏濴㚃㚟㔡忣绛㚻ϋံ绸ًဤ羬䁕ㅁ⽹ͪƈȻヸ䐬噶嚨㋸ᕱ簠\u05F8ႌ汖漡Á恬䄐\u1ABA㓶⠡ȡ桀Ӏ⚤㒖悊渶硚㔘⤮د䛆涸Ᏼ༣淕㕾涖䢐Ⴘ㗣Ⓔ濒㐴㖆ଖ丩嶯͖樰\u2FD6緖䑻⡺\u0894ᶡ᷌൴悑ϓ㓡\u0890侤⎲ᴞ䞮洩吲楱沂ⲱ棝႑ථ撠叙䉌ᶂ勴撶畑暚⺠叕\u197CẰᇰ紽᰼Ⅼ䪄⍊畅૮䌵仍䘇⬀喔㎳ᶲ┚¤̧\u1C8F⊚矷ѥ璵暦ᖑ\u0BBA帅李֔殭嶵澱䲫庶冪ቱ䢪殿⒕暎⺆♁⌭滨窮䆽巅䨎ቔ哈㤘憆瞐呰擝橸ᾕʿ嶞⿒䄗⦪煨淦枕埆㳓慆⤑䲑䴆ᝆṐ⦢㳸愰椇ិ瀫慦爗ᦫ౫濆ή㞲ニ挪⌖ⲋ\u0CDB旤屲ߛ斎Ộ㜖睙掴扪✑瞾秾ၘ侩勍䦙\u245E㾭妱䠠Ƴԛ䐯䖝事ẟ䟙圩コ厅筯前㐈\u0B34➏䤘㰄径塂㌘⠍㸸嫥䶵㰏弣祯䤝䠀ᔫ礪䮝㡠䮃ሏ䚲勀\\爚䥟昷瓟␦䁿繞⟟ⰒῩ繀ଠ䠚㿦⥟砋㹻翴⨂佋ࠤ⠭缏矋⬢敐⥊ɀ烅ᕑ繵㚟㫇䁕䅦噕ᄥ畕囐ཕ⇅㫼ᓏ熰ਥ䪦䄨Ӕ焤嫭⨰ᔞ㳸\u16F1ū゠甤桊¶䢦Щ䑎楿㗠悅̓䅂\u0F70Ꮚ瑃ɱ䳀䲆瑯≂ခ⤥ⱙ\u192F㒈ॱ⛹皓ѧ㲇倬∂ੀἦ告プ拍Ⴄ䱓ᨘ熐燻ᚥ∅嚍ⶅ㰸慆ྰ幀屜慚\u086D㙡氨唈珐綤\u0E3B๘琑㾆ⱛᩙ囚ᅲḼ㧘恸⾦杚ᇶ\u0E5D㯐#\u0A31キ滓ᱹY䷮\u08B0≒Ἄ溑玤┙涎ᥠ6ሒĭ❭䇸廅ᔵ⤃䇫奄Ⲩ糟昞㤰焱\u085D㥾ㅄ徳⣶㐞〒വ椎䖓ᠡᾳ⤂䕺ਫ瞵᱉㧡└\u087Bᝯ⅁㉰偲ራ䪁⬹嗢㙾㏇唃碏⾞␌バ䈊⍲ᮬ⼭ጸ侖ᨧ庪\u0C70声穱㦃弿睈箑ᬃඦ⎂熧䪪籷卖ኲ䘃㌼瑻䞌➂ሎԐჩ⎂籮礙ᦩ⪫༴碵ᘼ㙍¼ҩ窢ⰳ㩽㺝ᗙ七㜷Ԑ塥堃寭㣘壢㧫痡䓬⟹ⲳ\u0E71溇ᘿಐ簋尺⛲㞵ᯫ攈孩ജ琋䓘ტ\u244C簍⒣稯䞳氎㽦│㒝⋇ଘ唲➵岳擩ᐒ≋珬ᕱ煲㶲湷㉼埲㝨շᔞ尊㼝䪺咠ʭ\u1AC3纕ㆴ砘畗⨕\u2E4B⍶╕纍⤊浵㲲楝⣵㣆啍⥲侊盇䵋ᔎ䫔歸ൌ嚳⥔曅䵅槡䡺䄧翸㘿朴緅⛑榬ˀ峵卽樅۔始瀠»䣬ⶤ泳᧕\u2BD4㙫礳ᖡ㽫曔㉖庹೬㚘㊡慁䤩䡝Ů庹䴫琾ᴸᅹ䫬䡴䉜\u0879攰㙨䐛ឮᛪ;㘓ᕐ孳噱悋⪁㻭⍲㍻ឹ⤦\u2E6E᪬Ὠ⧬Ⱜ犩傺\u05EF屈玌ᅭ⼫ᨻ㖼ᇙ┭າ猹䟙২ẙ᱙庁翮㺒槯ᾂ沷溂⑉滨呏庀⋿ᰥҬ⒉憪ᤲ㦌㹬⍿ằ▏䆝䌲⒥媭繼孚ᐥ嶊憂ଭ孍呎冮寭剥ᕢᑌ崭儥掏繢㲈徍⬇⦣\u0B0D䒥য垾捎╨羫䱣ੳ\u0A45ṍ汰㩾ↅዧݤ㩮\u177E╋ᦂ煴庼ㄋ⟆䪘妾ᵊ楃㴔呅畏䦗ઓ宽ந祢燳園⭊埝捌䝪伐㞡㯬呅祄ᦘ稁ᡞ紊㊼㬗䵤宪䉹䷒巽䑨㦒Ꭺ崅⨊\u0F98爜嘆狏⼳䦲唐哊䊠䧇復䳈牴咲岵繬䖗䫁ᬵ㣌ծ窲徵縋╩\u2BAF噵،啺糘䁵䋍䖴⬀䫩⸋彲⨠\u0DEEɇ眭⡳㠊ᓑ✥\u1CCD⁚ƠÏ䂙繍ⶊ瓆曞橃ⱺ䅹\u008F嗰ᮊ䳶㫎㗜z拇ᛚɊ倊稭۟ǎ仺件\u008FϿ敺䃵悓·⦠㛶盍圃ⲟ㔬梎圌ρ累㛑ၷ⺁䷵滎樝\u2E5A桕ộ痁仛ئፀ⎢ᰔ畹∸疳㑦剔⇗䐁䴭¯ぎ\u0DC9䬱搣ୄ奈檦瑔瀠⍇㕆咥ᒐ⅏㙌控\u2EF5ඪ\u0FDB♗⭙㚕仑岥ᇰ丝乆溅\u19DD姸洌敔℧奔溄⩕凞䶖Ό獖歇㥛倆䇠1炳皍⻊ᨠ࠱䦼摖䮀䳸琣⥓ʓ㢤䨕擘㈒ᥑ䨗ֶ䭄享ⳋ晄Ⓠ狶ᜓ柃䊩㕕ᚭ⅁₸\u1AFFᛂ㮫\u2BFB䣂呢䶪\u05FB囲㜏䙋㝁㶭②佛⒇嵒墷懻\u242C⇽ᦹ䲜!⼽ᦖ煦僀©嶺⦇婐撆ⱗⳢ㬸巓ⷧ洕朣吽ᵇㅧਲ਼常披ৎ抛喒欎ᬢ㯨湜䄏㴁㪫帽掭ᝲ揫向䬊坿㫈䙦㬎⼧㵬怓zޢ禋孆⌕奯ᘓ勽Ȃ䌜䥱㛘䉰⽽䣑㯽㜋挌䳇官$搲㮚勂▷ቿ㬨磝ʉ畯㓩\u1975䪱疿រ\u17FA弅愥\u4DB7᯲㺤喿⽋啴斪⟏⟪ᥱ绯瞪攐俰㼈⟟\u2E76己⬏真\u20CA䲺绶◒⑷峓㊛璅䀡商屵䵵㫉dࠠ\u1CFD圍〡簓ē噀㤣䠸倬ܦ弣ཆ忛厐ヵ\u20C1㬙ⷽ㍶ᛊᄗ坎橣䘸僠㛪恣栗⃦ڟ氹ხ䜁Ɒ㫣啯ㄝⶨ㬹珉感噕⭃栳ヷ囘㴕哻竬θ㬜䴼朐䙕ᘞ狸囤䘠ڪ淉\u0CFA♦ݵ䷗碽ⰻ㽵婤䏖痗橑䀹\u0DF6沌嶎咽ഝゖ䏞渕\u0E00ʗὲ′ન睧疾爋嫖绦ᩚㇼ暃\u243Bᙛ㹂擸盧दড়㧄摠㝸燕溮㷠⟕悚␃㟁磦\u0A12䳾摀宎㇟ృ㛵㟫ਘ䵄楆殱縞斞拆夥\u0A12㔃⻇Ἢϰᴫ㬖傗傾㘶\u0DC7箰G刿ᄖ䶘砜䀠≨瀠䤋ޖ䲺瑻⧵⊤㬯墛䐷㘂!懬㪢䤽औ䝼㭜屧⪕屔㫜紼ᤈ冦涃笽䤔䜠l⳼⎃Ʀ圆旃孯⑇慟ū嫤籧≘᳤䭸嵒㥛冊暪 ∳ῌ泮䊒ᐕὼ端媓变懩淸嗩䑹Ⴙ瓯杒䲍䷫䲐㚓㦫ᮢ斩ㆫ່\u2FD9璬'缫䯭⺆杪ⱥ媖㺔ㅠ堼㡾䌗猫\u1CB2䑌幙\u0BFFᶳ⡼ 䮷\u0D11ᦟ仄嵅⡸⼬ង澷˛ีᙇ梞穽䘄ᚫ廽宪嗭⻉㭷稈拹示碆㉹㗡㳊箷໙จ囅⎄廚䥄嘥㋫⇦併\u2E66箝䍻奏ᙅ㿫\u180F䤻ᚅ㧫\u2E7F嗯\u17EF嗋槜妟\u08D6璝䐁淸瀖烋ע⫵笐䇝奸ᬚ噕㏩〉罭濕㞗婁䖉⪶熫捿▹埾算᧞徏滙㧩䢚䏠Ἴ䐯\u200B羞̰ち筹⏭嚠㐻〻㬛坝㭣擻⚻制劯㰕੦ώ㜋ཽ䄎Ṩ㍡㈼᛫ّ産㐿ܓ㟸ᗕ㓾℅ޢ䥕䓿罎㠈㥻⫿✝綫㕣⳼⛿’烕\u0EFAⶑⵇ㉻廾㏵䝹烣䭩༔廾廊砆眝㞭ᕉ‰䜌䏯㣐㖜⻤ч㉏ᠲ᪕ⲇ㽠#穸涢湟䚳㺮泾\u2BD9竃圽㻾Ϙ㠍治糣堦䨓㼶瑈࿗渞潍唖䣾燎䂮絇敞㟣汞橡䟒㷸俇笰猬䨌㪤繞志㶬㞸溥㮄ά翸⼯䥐\u0080䐿䱊ࠅ᭑犜吇癅緃缄Ⱂ念纄!ɼ\u177F㒿ም䃂㨮❠\\\u1718墑⚚ḁ狞&ி䴝㰋忌㺃䜼┋䜠,瀖→㝥㈜㭭ု綨ᕝȜ缇瓟昝〇໘穧瘘\\㼟ஷᐙᴑྟ簘㾯䃉桮䡷 绑猟堙Ꮔ众硟澸ᐜڔ⧩✣糬睓䕮佚耊忎晼㨠ᵇ〢#䛀ၒᐝ窐㳠➃\u1CAC皠㶆候怺掯仛ᭀᵇఁ⠫ऩ羟䥒瀳嗮卙亀仒\u2E4CР\u0FEA悉țᭀᅽ▔亓弸昳᷼熀㥎櫚㠽悑\u2EF8娕晷\u087D䈨ⷣ圿ᴻ匤䆶\u086Bᆆໜ扣䎥摗ーᶒ犨ᔾ嶚玼Ṏ᠈⬀ᰶ䅃㤈᳗ㅍ᎓ᗼ䭡岦睐⪶䯚ㆆẺ滯棷⁗ᄀ是ᡉ惋尿咞后眦㜏ᮼ䅯㭝拗Ǽᛒ\u1DF8瞊ზ睛厶ἤ沫廛渿㥤´ၢ䋓䑍\u2B8E岲\u0E68\u243E᧘梫\u0883滁؈ᒋ㠔澗䏠\u10C9⸎熵䣁傧∅\u07B2⪐秮ް宸⫕⫭\u2B9A䈺\u0A04䧁ҙ仍㗍囈磃怠㫨懃\u052Cᩯれ夑䃲佄淉短已䆥涎䟉Ṹ箠浐妌礑叻㖛ⶩ煇䖁Ⲙ⨈垼慊砶懯ዅ┕⩧伩㲝㎇偾嶦昈罜㘩宫悏⒍猪ၽ孁㶅烗䁈䧉啂硫ẏٶ挏ៅ弩籏焌涙盘㟾߀帊矌̆礋挞ᓅ弖䉋䂝ഔ⎤㸱燺ᄲ産㘬˻渏✌寓䡮\u08E0簠ܕ扁Ūǵၐ\u08B1ጛỤ矉ሞ竁憳㥃ᒮ毼䅺傍珗䥦䁛吀ᒗ㊍ʡ惐▝淐3楈涜志㉝䢠\u009C犭绫癴糳瀴\u2E46汣⠁濅㺲䞪ᣟវ瘗䩁⣭糋ᙏ嶠㈚檇峛縰䊊ᇿ̜窯橎弭絗癔〖\u0AF2⛮㑪倧祺႐ᔝₒ橞㽝⌟焃嬞ᰛᴲ璃㹭傧∈柂庬簸缤䶇畟崞⃥堁忐Ǘ節᪠嘣堯ㅺᢸ拀ీ㐞君栏\u0090ㄠ߆ᆸ稣妮᳥羑粴畴¡䋥ԌⱯ縷禉梠刣/♓ᤊ扶㊘ᘞ䞊ᠽj仢ڥ滠欢\u2429秼䃀͋ીⶨᨧ⏫䔠燠Pᣠ猣䈯๕䂠Ź\u242E乙硙捫朾圴媹ጪ山伇\u1AAEᙘ眂㮩姠Vἃ䷱ᗧ\u0A37䖋ხ䡡大猂ᯕ仗䛾ဠ㭏焭㩕湪㼢᧳⡞ㄱЌཐ⠑㷛琲炋㩥棂滀榢帮硌მဠԃठN⥈Ϥ呙䬯˔\u0EF0㒎䬤ゼ㽒↱ߍ澰䃼傱⅃圤䌲ࢨ╁⩇∳\u0874窰▄ተ硣⾔ᷥ䔁灹㑈㽶碦ⰳ䢐Ἐ⛑ᡄ傳ᱯ\u0DE6ཧᇪ\u08C8㕁ᑹ墱䢆ⅹڔ\u192F⍢⛓㷩絗͌囈⛁慹斎剦䧽։ᘝຳ䫖緬睿尠Ճ奎攃ፙဠ\u1754攍\u139A\u08CD丠䊚ᘳ㖸憃Ѭ\u1AD0竢唗珠⾄㷜丒⒁দ☶痀⥙嫜ኻৣᚴⱙウ㰢䬭䎁渳抽塣圗Ҋ倄櫢ỐɊ捬卮৪␘ᯈ⸹㢁⦮磆漉\u1ADC未ᛢ潇䎾\u0A58ㆱ斘ࡀ,緉㡦ᶆ⩼幡∜ᅠC«棸夠Ǉᵪ扟䎗㵥畺䳖垤䚶᪈楸䛛怠ۺ楴䖓毻ᰙү寣喁効ஈᒉ准搉᩺槒䚒ሴ㹂眗剋⦑糵\u0F6F䄉揄亶埄儠ƛ庺窌刌䒫氜ⵃత\u124F䣛后丣壒Ⓓ㔰'尹ᵦ┵澧榉ᦝṨ䋂ભ\u052F生劎䠠შഛ反ఔ∹梆᠇ڜᦦ䔺ởㄨ≐䟨ʓ⊂㖩噑䑚熵呬ᨖ䟝ṳ\u173C⓮㩌䀠Ɲ࿕ᩕ䁊㐓ྟ埱ӧ紶㿀ㅋ竆ᘸ朜ᳪ䭛䳣偿珬㗞⽤\"\u0E5F塹ᆆ䬵㿕纻秀ᕸ嘢!䜋᮸糓⸐၅❻⎫ฟ䱹අ━Ɐㇹ䐻ብ\u0BE0䛨9ᔌ掂䶨/伌熂Ꭽ翺猠˛\u0EF6❹ݙ嶩䮱姎㧛ဠ⽇娎杅様猜㚖乆㣬揸䘮㿬儅欴⠠É嗧‰拇妌ޮᗘ冓徯幙 Ĥ旨晖ᜭ禋ॴ\u0D11㝛䏊\u1C81㥥擾ᐘ倃(坌岠Yᡫ䅚ᘷ䔗發 㒱犧䣜␑毥ஶ湯爟䐡ᎄធ㷭睂ஞ犘䓟൪㈇圹傄ऽ㭯᠘崒柧⅐紖ა䫢⊩נ#搞䵪╇䂴≣䆮չ\u0FDC佉߯⅄粳狾猂ጙ\u1AC9哂斻戠ĵ澰炉ਈ⚊楤䘙⾬ѓ㝟爠\u2EFE㺩≺ᢶ⢓জ◄Ằ䱄ᱮᑜ㜤\u0B7E焞⋩ඪ羸⡮溟㨈᭄岳攭惀䓴ਬ䱁⭊捆矇ߙ״⛒拄檲䙭僕攓ቺဂヨ䥄塿㉭䦮ⓣ潢未篢奇睽礘Ⰱ\u31E9ᝆȆ㖰䖶嬥ა瞲㹯՜ㄝ䏂䧧噉᚛抲ੲ䕊ᘅᕐ䚫㌨⣑⋥䈠}ᣗ攝௴\u2B6A㍥䄫㑵牪ἆី屶ᖫၑ壈ᓜ汵甊㏉#朜Ⱁ⧥匴屲䩠⥷߭ῖ᪉幈䵝実જ⺑⇉ڛ㨅⪋⥔।崲䱪Ŋӕ粕匚䪊㧉娻皽ㆱ楧➘匏ᾪ歨哅㒫吚䰘㦾炵岌⥩╸䔘奲乪ᜐ䉑孈⌖䩷亀䚵າ瞠Ó↶⩵穨庌\u1AF0 ᶮ槖稍Ḟ⦝䃨*ㇰὫჯ͂⢽∠ڐ絁\u0CE4㈵ডἱ⦴ƿ埆ᨓ\u17FDგ秪\u0B4Eᓕ㋨㏁伴\u2B85涶攴䙢儠ə▶禊ᥛ♭京䧫❈ੂ犥䨠ޒ˃彵䰑䶦␉俇ষᒄ妏ᒓᏨ凪䁇⍊䲠 ‧⟫梠ý杊˘ર⫁䲆㰵\u0876癶暚绐唒ᯨ篂糩ᨫ梺ℵ䯅†傼⫕౬㻱☘ⅲ\u0C64娋砠ཥ斔娙䠶⚈啋哈΅䫥㐉⼵‵喅洼䲓ภ`䌺\u0C76姗礒只岼%䠋ᾌ厼␒ૈ倷㌉⠠ै欑⩃䢮ㅑ吘\u2DB7ᕢ啓䘧᪠嫋盈櫟Ⳟʕ⣮╰ឆ嶶㎱唦嚺唼樝掭翠!ႋ绩嫓ᤔȽ\u2B75⺐嶄殸癱妹枇ၠ矋⛎䝝泭捇䨭⍕猄\u2BBF㿚Ġī獲扝善䣹䬺毋燉杝㌠ƫ䧎䝜\u176D扻䫼⍕眆┉湹㤠S獰\u2D73爞ザ忸䚓旍\u2E52嫁歋⥬ᛕ㫷᮴\u2E70㖍栞᧼䌓߭曘糓狄䝞⦑爙ጪṦ痣嚱侼沒Ꮜ㛚㳹玟\u087E㆙䱅徱纕礇斟Ῡᬊච滈㲱歨怠ᢥ䃸ૉ〠Ҡ笟搇䮽☙㠆侾㵼灷坾凑ओ牤㻜܈\u0A80\u2BB0⒎㟶惷平祇嗏倚䀺㩨䃂䌈၀溡⺩矵彶䊁禅щḜ䚕㿬䇋܈ᩚ猅匥ံ䒳剺禰\u1771੦怒$ᓑ巀炳羕潽ᙒ୰毡\u2069杋ᣲ捳ഠ5ӱ㮵䢌ᙔᜄ炪⡎⥆ॡᮬ䄳ゥ煔⓼㼼䖪⠀儗∑ઌ敾撡ਡ疱⩭咤㓾斨⳺㑨忢咫⛕䣌撦ଔ侢፭共Ӿ㎎䷦╙ᶆ疻ڏØ炡㶔⥱㕥\u20FA劸ㅷ▻ߜᰆ硳\u2E4E䱆\u3100寜泍巉潇䱻牠3\u08D4抴㆐⦉ᛃ朲恫晌◙ᚰ䯌⪫㩍某历䦏䗑ច唂殪欯㣖挊卂ⵋ㷉墶㉸४極㐠ٓ枱ⶬ䙔僶殌䶊㣒加½Έו⤮媦䩷䰠㺵割᷌◍ᓍ᱖惻猓\u0DC8㓢䬚⥉㩅↵ٲ榋▫㝍奴峲獎͇㤠/ᝏⷜ㓶泖滛㗅牦ᛰB缍䷄ᘏ⬬旓ᖃᛈ姻杏ⷀ7Ώ垈ೃ㌛䨱䲛㼍ិṰ涤斧\u170D\u1DF2濫ㅑ᷋眏媮䵹」宵ǰ祴ᖒ䝜嬒寪 㯊┮柫氦糇犰ñ⽯䄽羴煽㦞ᖻ\u17FC弔晚変揗ຶ㫿摇ℵ嘠䧾῍磈喳删ي̊拐઼⨡⻙ⰵ窗庰3ਅ桷ㇷ䞞ᵾ碓帒䩋㴏珐⺦⩥毥†ぽ⊹侚㙵䦖◰Dࣽ桷ᴲ啷ᕌ琠̥㝨ᩪ瓚幟㉋\u0EF2㭀⻱\u2BE5ⲅ†偓㋕櫷㆞#䄝段㙵愖䤂㝩崠Ţ\u23FEཥ嵼呚姊緒䮨㯀➗\u2B65⡕㷽ऄ㷹佼繞琧倾傚∋߀f↛戏忦Ẩ歕⸢❵嵷嵺癷啞啧ᘌ䴜嬏\u30406㮛⸈曈\u1AB3㭄癏㛵㭵揷剏㕩瓊༺禋Ị⛑弗㈠ܶ俓㣶筇⬏㼑囷孷䶞㔠©୳浪疒枓喼夛⺖\u0EDA媴殇濍◙⛶漲彻㗷皖宺笛氎\u2459㫫殗淌䲝洄ᠴ㾅㕇痯埚唊㏭繖㫖氇ⷭⴕ珴⒩㵰භ狿༺帋⏬翅\u1AF0岟澀⇕ö㽴㶑痟嚠㺚昋⢍弸\u1878٧丽㰭ࡕ㞿䯄秐㒞弦稦㿌\u1F4F笙ዟ咠簥㰯ଫđϤ\u0E61巜䔦ᐼỘ竤ᯨ侲㚭\u1BF6潺ͻί噑幦圦濍佌簿此䤁⬙扗⢮愉ජ㕑庀撦の准䚾ᰈ沃㕣慗㓳彌\u0DEA㚔᧦眺ਿ⇚ѝܤ椳㛣ည\u1CB9捰Ả༹䰜歍ႏ梖懣܌䠠ὀ⋭ዔ亐狣樊呐厇䍮㙨堁䲪Ẍ҃㝹\u1AE4⫳㛣杕⑶玛䵮\u0EF9嗢屧噪ἤ✇᩼洈⧩ᒮ剟ᄜ䔾┙壐徺⌨-㵱䯌ٍ○嚻䍲⼑ⱍㆦ狿䮕⸕ෂ᷂䙻̪㣕凖䘢毈桍㑧ᘅ#ⶭ㞵嶶湺禌\u0C80团婦ṷ岃绔⫰ᤍ⏋Ҕ㲲煺倧\u0D49㈅䜢⦸涃哅䉿媔淅㕕営榪ᕌⷖ㇗⳺漻⻅▛固\u2B60昇།嶑珌掏岀劳䚾ἑ‽ឯ曰箈ᶙᑰޑ櫺ᾈ双皫實倘碅⇕Ř筤ᶿ෴孉ᱚऌ⏏募尞滛⠽ඵ\u2E5Cᮈḗ㞃咩䵸'媲㥒呛翣㷝眛燩澧㊽வ\u2E75䔟液䜂㽮筃Һ㏎⺶㯩ᵄ樽皔楓ᓉᶜ皽壩秪 孳勩嵛椈㏐滇䮩ᶙ▽岗䥐ᔔᷤ疓夎彚攈ூ狉⟉沦㔽㙮煐Sᳳ䎗◴䙲半今㿩惇\u08CE\u2BDB伄\u2B75歷⌵䖄\"T栵ྖ↳睮巙瞺㐹旆ሌ䟛ີ\u2BCD槗⇳䵭Ᏺ瓫區 త䳽ᔾ䲚㨎暚Ἇ窋\u2EFC稣港㡝掕䏵\u0F98㴠ͣ\u0B52啭嵖看ဠⰭ㍵仐 ᱃㍨哋唎䯓䦼窍\u19AC穓\u1AEC杓㾕獛ᕸ㵦眷唾榇►狂始㯋\u19CC紙䛮᭝⾓卮眠༊䀠ዋ䴺僾暚妸矝庠ê禺៘庲穫ᴼ磓ᮗ浽\u0D98ϯ痮㬹缛燍⺙㦣⨠ұ竚\u1CF9窟澏†\u312F暗᧼捕᎕䮸㴕紽嚖從縛冀䂇㼙朋ட㳕旭\u0FF5Ὤ緇矐ᘙ砆᠈\u0093峀ហ察㼝ဆ⽓䄟ቴ擡㼞栚怌筍秄ٟὟ㔣耔*Ꮒ栫娬侰羜䤰ༀ㙞䄦䠏ǌ䗗朗Ⳁ縭㩌じ挛祌\u2E50㔡珀м႒嫱߀⨂粣⸭⡛惫禮⇰ぁ悧䰸䦔⇬\u1AE8滰㉣塔硘ዡ\u0DE5\u0CF5ⳡ縦矌榚懕ڐ淲皙䕍㑟惱䯷咹㻚椓剹⢕↱ᮔ宝╭氶棶ዡঌෙ㎁枦ᐾ碗戏\u16F7捕埫ℬ㜅勠与ⶤ㨵桷ᚍ⧀暸䞂\u1AD0緣༭䉔玂\u2BE9\u0D64㴱产ై斚̝جᴐ澥ハ咰⤁␎㑙㝦究䱊䉴嘌嚲\u1ACA惃咭剑ヤ䥳棑嶱乧㤻㖀J惶ᵸҕ\u070F\u1AC2⬪\u2FED檬曓\u1AF0揣\u0DE5ቶ佧⤑⣚ㆹ垽㞺曍ᆯ͎ᤆ\u2D29㒬ᵑ櫦᭻䲋㘐䪺ᢋ⾃᳖໓㣻䖱㟅ᣨ䰡挻ᗒ㇋嚮峧偍\u1AD4ᩕ灑╯⾼㕑寶攖壘燧䚞壚瀋猵嚾礛歸秼㘑䅪\u20F9痍\u12B7䚺殘碍揎湛ܘᐔ乢㊖䗺兏粇㜜☩Ḥ搢墍懑䇊Ꮏ㟂㒮煪$仳㻴汇ď䊐㛢⚩᥄琳䲎\u0EF7ᦖᰈ䲂㒔絗㓺技\u0EFC❁塤綻沔-㛋\u0CBB溍晷⓹ᶜ瀜光惖ⲹ緀㊠Ō\u1AFF㊋㌑㙅庙㉻䦍浞ⓠV㎎渀M䋻妷旓哨ô湪ㅉ䇆櫺殛ⷓ♍᱗†\u2B8F㚵ᡖ棻㹯啙㛵寢侜崉巖➈7唽取仺˜ಶ❽徖揻⾏㥕ᜏᎊ俓喍䗖┊⋁㧵㨠ݗ枚ᶸ瘡⳯㰹!㘝ᠠᓰᧀ杭᳔磽⤗ㇺᴎᴳ䵣啹傗ᴉᏅ妠Ǵ㷸⚐帜睳ᵬ捓䴗䯙⼖㍙人㞹懛ֹຄ禩枋\u19CC皓斖篟滠篫伯㈉䀠櫻沆ㅝ攗㨏皐㨁权楜甓૭៕ⴌ琕䵡㏮牊緻瞀F縇ྻ㾗㷽癝桞箓圕睜ཡ珇氠Ӏ廭崠Ϫ䂝᳠*䠾杗㻣箝濦匝欆濾㺃縒ᘠʷ℃庿暗ᠠ᩿γ㬯ᱏ⠫!擨㥁缝瀿䂙㶔ްᰱ㊪侖べ㾘㫧映ōㄛ䗹筐㽡攫`炛䇠RĽ棓⋠Ò䱏䢚˵ࠇ曰管琦䑿廘㬖๘㱢殧䰾碗䈀䝢\u1CA8獥䒮\u245A慇ؙᏀ㭲綫㯶㿾懠Å଼ゞ凷獛仡嗃炮≘㆕糌۾癑涫ॎᒚჵ䞏侐爙架ɝ繄⏡ᜌ嵁㳣䀉灕爆䮂\u0E61倠ᔢ➑ẙ㟅灮㑸ᦛ˯ᬼ弲捫坎㋒排❲သᣅ朻⑊䖄时߬審断ཎ瓕⫩➱ⷉ㱜䤓ᕙᖞᗻۿⱪ楋窤ਗ਼㊌䖅ᷜ䮳屶䅽墓収俁益罋歗窟䢒䏍ᷔ牓棯社喔㖳ⷆ㶨癇↿ژ䣭䦽ᵌ绵棶兹瞈嗠囖㼨扥䫍∍⫳䥃ⲕ㢓其ゴ洚名佺弹碋㞾溘ჼ懅㞴㫕扻㍿紖痨嫂欈枋澼回\u0A7A氟ὴ㊕槷¯ᶛ琞学\u0C5A睍\u1AAF⇘؇⎇\u2E5D㰕䯷♎⌀燠㙚ᣥ栃⟌♒竾\u1757ⴢ狴珹\u20FB·\u0C04⼮᮸玃濎ᇚ\u2B60(炎\u2E58䭴ᬤ帲煖捱梽\u1C8Aዶ⾥㥦梻䇔(䒵籷⣆₋ᛪᐗ怪\u17ED湖絏厓唁⽹㻛⟥!穹嶆橷煼懗ᘁ洿㕊璮惗˸欟熽⻥幆琽⦎㧔㘆䕣⡋㾍勗杏ᬕ⸕㚑ᨧ⛷㚏姘㜄宄淣㎍淖紋㬁㊵㟭㫤痻ⶏ奚㔐淾湆稍煆ǜ䮏䧓㙓㻖曻溗䏙༑㭮濳〠᧔㝑嵇㘍劎⧘ܝᯬ澣巄枽㮍⏟䷤匠\u008Aஜⷿ浑嵷㐮嶖\u0DFE简3▏ۿኀ\u0083ㄎ㗞އᷜ盩汎燗➏ᮟᷠº◿䟞\u2EF5㮋巎睽綗ᚻ眇㯞渠߳漘妠潧㥝怠⍳滓廮篛㴍篘ự㬾修系匏䯙⼝㯊瘠ǲڀ滹瞗㼯℗凬 ヌ碗現歔洊笫也㞛䀡䴾俟㷝琎\u2BDFកuඖ缈%⺧䴖䘉漏篠ྠ㹌栯ᮔ(噃盓栾悛ഃ筝浟㭧爯炝澁㮵澰禃緝䱞ㄐ⇰獤 羧央圍ሀ⟺่緞檯栎椗⏪ܿ澏㠃眯⸎纓␜\u0EA4㱜瑧ᕞ礖√絴ᾠ瑇崮Ѿ珑縑ῤ繼涧放墓\u321F䞜‐瑃炮⪙⨍嬒่㣳檧⅝撐䈆㤦㵈犇疮機ᨁ⟋Ẽ㧓滯䤾ᴕ叧䞱Ῠ禼妮䏌稗搉篘㷑獧㽟紛㐝伦\u1FF0㏇枮ᱺ໘䇋\u0E92籩湇䰾瓗珨䝊䀁待嵶䰸䘇Ꮦڒ簞⽏Ҿʐᇠ´曟ᬕ湠µໜ㊛狸䭵㰥㼳幾'ᐭ稅䭿⎘瘓垴埚筽㟏妿瑔ᯰ潼༅㦓䅧掛㘒㏵伺㵒掕ᚾ㚔\u139C漣㺬熓嗯掘ᴓ㠇ᚧ䧻秋嬬༘娒濵ⷕ狓嫮ⰹ㔊矯ܮ缹欇\u1ACF峷㧬溝㹜癵䋯㭛帙㏳彰橙暇\u0DBE䫐߹杞ඬ約䅦潙崕ᆿ忂ඩ樿\u009E牑㎄ỷṃ籕䟯捻ᵡ囡䌜楥樷\u19CC$䓇样恾ᛟ\u2B71ΰࠔ㤫呎ベ后\u0BDA㻮\u1A8Bℋ㱽纐\u05EB‗⻆䇖滧斎琈疯嚩㫑䚷㙿䇝柣ឤ徫盡\u0B4E楏⬌䰄⿅㭥湿䋕㻙R垲緊绋筏繍㶑\u171D⻅㬷澷\u1C8C֗✊垚耋痋巟囘ⷘ㚳㸭㱕氳玜ᥔ圈椾忳㿏竏眞嬑㚩⾽綵桗灬ᗔ嗷淾紛簏翞㫜ࠓ渇㼭娭粻泼嶚㗢敱罧笍沟ஊ✑ᯟ㚳粕毅墕吐䞌矾缶猻沎暼ා“罳㩯捗䳼䎓䷮厭廮狍宎淙緊志㙖⸬滟擼ᰙᜈ睭粻㭻䦟熻Ꮭ忈眿㶍淗#㒷綖涗缟͞⸇孛尷粛椟柜弃啰眽榔柛အ\u0BDE濼罫嵏疟小濜缜榠ƕဝ漇㮣漗㲶怯ဝ侞㷥睶洇㋎吾㏘⏐怌緪羢羕滗紏穟犠ᴟ嘟ࠞ倝⏛䀑者㷹姓厊Ổ㷀碀ຣ椙姗Ⱦ䑝揸夐缋㿽䞴Ç繏簯㰁玠၇墯㨎娽悚䔑刎䁓c༥渠Ļ砤夑㈎揣䞩ý溛岘硰ࠣ敠㢯㚾稼窛甗\u0A04恍䏖笶ŒɸҐ\u0E63榣䮠斾ļ#ᑴӹ砀ṣ䡠玾ポ戤\u242E䈂㏵䏌䃸庑㷰㡹瑳文嵠䚡筜\u245A㔕ċᇥ枣£ṯ洬ې燫憃哠伮՝๘ґ娃㏦恽䂧ǚμ۱瓍⢀㷠䖮Ҽ熛㽖⠷珯悈䜠䆡˪㩵疠昰㥧ѡ䢢疚\u2454☊ᯥ⟊潕庮⅔祭皠捗䉀嵾ҽ䎙㡒娊\u1BF9埞₩幖㘚㣭璝⼀㳀绾ਬ \u2BFAӴ\u0DC8ᝰ㤿⍮⚢᭙欗渄\u1BF4ᗇ\u20CE䅍㸎竍矧椳孇ᦶ៝⨹抖ళ㯩㟇潇ቇ㷆ܹ㋻挅傇⥶憣ޚ籕ⰶ塑矀䎐㹻Іה瞻濐㸿⟾ᖼᮙ䈖ཁ䍯瞴ἤ噷Ͷ\u0603甧栝ᔀ岰➣⾛竑洈姺嗎ὄ㻿㱮ܝ癰溯峋㦺峌䂦ሐ氽㵺低欦㹩㕩禳瓈楯崰神禝Ԛ盕䓈柩侴ᝊ㻴縙磚⇸惶䃿㾪↤⌙䨐.ὥ枕䏥㍎剢泛䤫磂៧俅⎊↼䉹竓炷桏䥿㋞䎜沧䖔爻\u245B䢑ქÚ粍簋琑○宿ゞ㺝䒥ᖒ尀曻拏征⻑紮孛熗杨㐋㝪犔መ仴栋⛭⢋\u1778縠Țᚥ㔬䬈旯䯂嬨繦䉭ۧ狵暈⡟ᛁᄜ䘘⧑率㑉ⶣ僻㹮䈳㥺\u0C4F戈㕟ໆ㺍ේ㐑䠏瑎徴坴溄氠̭旕㠈囦㾧坎纥尠ė匮栋\u0FF0㿂㞁纍㓧笷璿替䚟㴞㓢ᦦ㌩䜋㿨\u19CE\u3101繃絧筗畯澝侽ഞ‣珙匪怅䱜\u0084ç縠ĕ綧笯漆翧㾢ď縳綽㯿㗝暟䉛 ᚹ憓紷窠༌ᮟ䒠縞䇢䎤ྒྷ䀰ϫ羸焋㚳崽歱矮榠⺐紁䗼\u0BDA䀫8⁂\u008A焛络\u0380\u058E\u0D3Cᯘ〟⨡ㅝᠧℓ丶㱜硵缮İϿ㪁畼ម㎐圁剜!棠䖡\u08C3愧岞ᨡ⠌澧弩主ၖₜ䄄ṤΨҰ॰ᙠ➯┡⨢ၦ䌓ᾥ♯₌\u08D7慦̰䗨\u0E62ᗠ㴯⊾㔢樦唑爆⇨₉ཀ䆮ⶏ䆩矰ᆠ㥧⤱Ԣ熛外⠽≊悈䝕ἔ⋈Ցຂᔀ⃯㬱璣渦㰯℉≂恰䞊ᆞ̲ډ睫椳喨玡炢\u2D26ኔ愀!巄㙀粱䛝੧洓堺Ǹ偸䢣Ḻ⎒箴ෲោ❀䵡㪢ᶙਮ\u2431ዺំ䣁䈓㳦Օ眈ᄄ∏Ὦ杂л⬓㤸᯿㞸愑䈖⎚Ң\u0DE0歐㮏⚶⭃䅤䊭Գ⡍ゞ⃘Ⅼෂ䞌\u0DE9擦巨䉱䓃ᾛ嘭┹㼨玠⣪会ശՌ\u0C5B梐Ⰿ⑱侣楧᪕㦅\u07FC呥⣗䇗ɱ秬ර毐⾀危ҝイ㸩啥硟ǃ愐⇟ɦࠌ༊ᐨ㟈瘊绂\u0BE1笐䈼秶࿂⬸↨簠q稨ᐌ窱\u089D嚄ⅈ䋩碂\u0DFAᬫ厁㣦Ţ㹉㨖⌊䑚䢎棬ⅸ䐚䉪\u0AB4 䶰翮ᅣ杧⨗洰埯䢊僿㻌䐎䙝ऴᴔ☈畎⾜撦ᴫ㈽穋䢏梾⇱ℙ妛琶摋凰緞㱽竘䔩樿\u17EC⦲储懐挍硺೦ᖈ⤸糦戹⻛洪䠂㑔梟彬ㆆ䏑䔊\u08BB❩䃰濁㽢ⓥㆪ樰0㸣ᘻ\u1C81嚂ধ\u0D97ࠎ䓪ᶭᣟ㹮䐉䝛炆ဠ\u088E熻\u18F6ㇼ淧犦௭淬⋟\u218E媃暥᎔⠄俴ⱸ壯⇈䱽㫖శ湸⋐暁瓢亦猨⠀濹ⱼコ㙥䉇ۋ冿杘\u2B78宎ᶂ㫧縓瘰᱁沐烴湛䉏㣗牟浟凗ਁ秣斤涩欻籊c墡懏緿篠ण梠⭗┞⡝㎥㶨縱汉㿆ニ態䈣䙛\u0FDCᨼ㰨報ਝ࠘秶刁䁆\u1C89烫繫䌳䟞畼ၘ⎐弎ⷣ宧厯ļ䁗ᷔझủ䊃䙁༜ዧ堐老ሣ㶥䮯\u173CɊ岘ࢫ硈̋䑐\u0BA2ኘ㒨失ΰ⡧儗ℶ偕羿à\u0EF8⎨礗ಐṄ⸐䈱ሃ䡧ᄐ儻䇾羿\u08C9爜·䘨࠳渤╨梱ㅂ灧墑༺ṓ䁯\u08E1\u0EFB拌ױਲႜ⼨掶㕃తబ䤸䉟ឮ䣽ᇑϗ䠘࿓惤⤘向Ⴃ栦岫䔷০摷磟Ỵ≺窨瀠୳şၽ䃓ᅠቼ٩ਪႳ尤皱㭃慦圐#\u2BE1ḣ䤤䡡ဳ⁇偭椰\u1BF4ᑸ⣑底ʐ╬砒\u16F4㧀奡嚢Ṥ⡮礲䩕㑬愂凹㳰▵৪᧰㖤繱櫂給檨布燪揙\u20BE৬ጲ䙕癛懷夀緡串㍧㊭点桐炏䢩刀ᒪ䑴熺\u1757䦀狡䓍ᾛ库䢳⩁≺Ἦ冽⍦㦭ਊၴ▨秾㫃氻刔㔸㯱㒐欵刌ኤⓐ㈾⡔㓤嗾秝獦㪖㢴婐ተⓟᆋ⌺ᨽ\u0E77枨㶰䅁\u0BC3梥纮崶穊⡨坪⇼糁㢪\u09A9ᡨ㱰圠ƍä㺪刽⣤౾椚⇕㗉㭻熦ኈ㠸䡑乣ᝥन㈱ \u2BD4ᤗ刈才݂瓉᧨ⰸ緶͢僤㶒ጼ♙梉ᣫ⇢䉡䟔䫇侬㈸慑⊂瓦䦩ᰀ晆㉣\u18FF內զ☓㍦ᤸ㻿㽩㊃竚晫䲳ƹⱼ椩ㆫᏅ䞛睦\u1AD2⪄䛩妳˥ඩ⬹瑏㊞ᤐ懫\u13F5ؓख़ᕋ䴄秩ள泤䌩㘲䱜殺セㅮ䉑䐬䵙ᑌ☄䗩厃⇘䀠嬳᱈宯壿㆜挣䐮হᡌℴ毩㣢⇛旕珉癆ᱶ壿ㆹ䈠Ζ煮抰&䬪ㇸ檎⎃怙嶮朷滺᱾㣈⧞所ࠂ俅ᑪ㟐檑淣⏘箪㘿ǧ嶾㢳愠ǉ彆楫势╍㢌ᔆ⧌力䒡்掼\u2BB4䴁湲㋄堭9䱗碛瞚⨘名䔧\u0E85ဧ䱴砱毼ϥ\u08AF䄵癖⩬䂠Í☜揦瀖⪾湉䪔碪熺勠䜉珎ጜ⽴崁球爦缯\u1AB5幏䑵响慖厔\u0558\u0E7Eᔊ㧴䯉⸣磄澩漿䏠'ⱔ缑稂嗦灓⤷ɝ碙磱爀\u13F8碦䭰ᜢ㵔䤮℣槥哗᤻啕籩ࣲᆀቐ✉ེࠠ偆粚㓪榘ነ䒡\u0B49殺⢨権倳䗇㒩ᚶⵛ㲃⽪榐ጐ➖䧡ᩧ傤玡糲ᤦ㈬┌ⵕ婹㒠\u0095᪢嗄筩皿浂撃㓑ক㱐Ⓐ䴪ᥚ⪔磾᧳巅測䔁௨䊅䜬䈂Ꮤ䙨䬸ῢ\u2DCFྉ䄳፤利䔰慛䉤䔒ऴፈ♕痡Ểト炩䃂⑄⨮⢷慙埄悶净䉬⒞䯝ᾚ∔嗮䌲⿄汩ᢼ桙䚲䔃榃̡܃牑ᑂ㈬揮崲穇㱔琰ड़چ䔚ং\u0C3A䜈俸ဲ⡄殩ڼ㩩篒氷ㅛ把䎜\u196Eጶ䔱䩃ቦ㷩ް⢃䲦縑ᴿ䑖㖵└䦷⍒⚅ڃῨ⵰物参㍻㺪熽捖剶⓲䦩\u139D碫疉Ẕ♄䋶ᚘउ磬㒶呏犟⓹ᦒ㌩ւഉላ䩬潑㖲ᚤ㓯䲵➥౸擣䤳ጚ\u2439䶉᱒㛍⋩╓捆哮\u1CB6奒\u2BC6攘⥠峹䟅Ꮣ႒㯄掹㕓組峭㦸䕕牳坝䦁䏜枺牙ᦆ┄恹⃓䃅ମ岵ᙜᚄᒵ䥮㐙䓥䨥ᆒ⧐䟩⤌玦㖩劾䱏\u0A84攟䦻ሱ⛥䳫\u177B䐄畉䱲㛥煭喱㑖ઓ擒姪揩◂䬅ᯪ㽌义嫒ೆᥭᖴ歕䱸\u08B6妴勺擪例᷌❸幉敳嶇ը䚳᭟噻ᒩ⦝洽┒䤻\u1DEAヴ應ⱳ榦⛮⪴╀箬油奖制攗㉻\u1758▌櫹㗓殅綪涸ᕐᱠn滹㷒懄浯઼ᓬⁱ洃姇㏮旍䦎ᓊ⬌櫉㯒篘Ứ䚴筘斫\u08BAḭ掶曪䴛ᢊ⠐嗹想寧滨㶽畘旋\u08DF⧲刭ⓐഛᇠ㝨囹哳槆ᇫ\u0ABB\u0D49㱣㤔㧒˶杖俅ቺ\u2454枉ᠲ㎇⍮暸❈于㢢槄⎻ⓖ䬽ᅺ㪼侉窒ആ᧯溶牙婸瓶楊ϧ⒫伽᧮㎔丩㧲\u2FE5秪Ժᵌ㪈峱㦽剅昀䪷ᰢⷠ䈉㳜猆孨\u2BB8坋媒岺ধḋ⓫企᳚㖔仙\u0EF3笅灮溷橊穰祺Ủ猷╎䬝\u10CE㛀狙娳牄⪒\u0EBEᅘっ甖䇂珄╙ᆽᶂ㬬瘉䘲ょè傾ᵇྸ\u0D00福勘♛䱩毂㟼筡ᡣŅル䞵態ᒜ⢿㤯厈柱侗ᔝಬ䦩䞼䂅ܕ枷低䚋䓿ᙥ狔♱䰳⪦⚬偁䬓䅇ⷯ䈴㔹♾䳶⺌㏖䒒୩ᩆ\u2E44榠༓浨穭㌂䥕功㳆唌拲橈צ楆㢜䓩⭓㊅㣩㕥Ὃ嚤㽎䥊䐇晚ࡓᚈ⍬粹ᘒ䅨瓯ᦽ彇㹲糍᧳Վ☃\u0A5Fᐆ㟬伙帓ᕆ֮㴱獖シ䲨瘮㉶◿䭔ᬆ㸴䉉䨒熇⋷沴䭘桱撽秿猡☕亟ᬮ仰箞\u0873瀇湭►♌ƏⳄ⨑㈵寂䶠垶\u2DD7㻞嫒磄೮羻歃ᚓⲪ妀勉┒䫖Ṗ✢湉ୢ ވᝰ\u2FE0岡㘄噹ሪ秤䛫犱⃒䅽̊\u061D䉪晐⪀坻䈛\u0E79ᤃ➇ⶩ喹⃞䪗ʡ慗㏄ᓽ䡀嬒₌懹矒ᾄ⻨灰╒㙣墹ײଡ旭䫎ზ㝘炥攪殄櫭䎸╗墆䋍֠爮摮౻ᓁ▌呙ᶂ☶९ᡳ㭐\u0E70᳭⥅粌ᛐ\u2BD0售ⶌ䀠⻧ؘ厼ᛘⲛ\u19CA㤼塙紪ᄄ敩硵筑憕࣭ס\u0AB1敐㘕ᄱⴼ䣹梪㙦懨玷Œ慡ᴄ㦌⌕Ⓧ䣧ᔮ⁼筙㹃唄槯涰幕⒃ᴗ慃㍩枽䶇ᡎ㊼媡㆓攴恪玻湇乣唈Ỽ劒篟੍ᗎ㭼䋙熓ᯆ歩暰ᵓ\u009A峐祅ଥ攋䩷\u1CCE⪏㐑۲䏇ݨ\u0C73㉉兮岱㨉卍曗䣱ᮺ㘘埙◳恄㙌汶ᝒ湡岳䅦狆ᒻ乘勚Ⲁ槥玪᜵䈕宷⩃\u2E73Ӓ礯卖᠋䡏ᒦ㏼墙洒ህ䷯ຸ䟼穻䓩冑狾ᙇ䩸ᰎ㠲湞㩒悴\u2FE1垶機熉㳠┸㊻杗䫏᭦䤲瘉灔ᙘ壷ޱ磔ᒊ㳬╸䭨撧䣪ᄦ哈橅猒炶䥈ㆲ⣩䙼精䥣ӭ\u0529丏ᤔ㑬旱䰓獫㙖㉰彍㉹純䥋狇摫敦᥆⟄呹䲂簇Ջ羼⥚纔ᣩ◇珧曪⨄姵\u0EEC洙搓ᆥ濪\u0A7Bज़㊈匍⑾㉗摺疦᨞\u2438候䠒⊶䖭妶ୖ㡼˾ㅲ䰚╀ⵋᰶ☜坑Å皶妓喺Á䅫勘奰\u0C0D夨ⷫ\u1AB8㙸竎䨫壄絍灷瓘ઍ፪夰玉家ี渹㜴圥㨫㺴⁍㌼惈ᦊ⊹⨌ளគ䵬孡⩒璅⎂妶䡍ᶻ䳌㚒㋢旲\u0B3A撶⩀刁⟛㚅૪椶告疵惙慡ˢ\u058C戴\u1778\u2B9BῊ◒秹㛪垇᱈塰筇↚㣦敉\u0A7Bᖽ佛ᰁ㺒湉墓⎴屉摶ᕊ㦃咹数琇\u17EE⥐峙⅂嬅杽俻❈噽\u08DB㦎狀䘛䫊ᕄ⼨偙モ柹\u1DEB枷䱎Ѳ\u08CA祴͡焻䩯ោ⻲ኁⵠ䲥㲓⸶䉌·䵄白狯姫劺ᖴ⻇ᾮ♔幥ݲ㦵≉硸ᣈㅶ拖䕓叙攡⢕ᨙⰪ刵ዲ猴䙈瑹㣆公咭㦭੮ᜋ亽ᩑー䓥̢⬴䝪ⅺ㣛㆗拊ᕫ戽曌⧢媑ⴂ以晋扦壊᱾壌䕮峧䗛ପ䠌⢗ᢚ㾪工亢༷乎ॲ扅煨䓨╰䮀敩⡷᱕専䍅繨㢷㘬⥴͆२崅冸䩢☷䣄嬑┲劙徒ᢶ\u0CCEॵ勎抛䃌祪⫟Ⓡ䯄彩㫪䦵ᄲ碷煏她絇ṥӫᕶ⫂墅ሿ\u125Eⶄ䝅ᅒᐅ啌㖦㍟䚊䳥禵Ꮲ吺ⳡ䃉╰勅壊慶啈╸㔩ᖔ㽧᧪䬂埅⧔哋䉛㇅泋媶狈ᒳग़楻北◲卝ᜀ⧴夔⻒瞅ೋ庥⥮犿㓛榄㌂敭\u2BBA囚爬囵\u2B72埅ᰫ絅㡎㩿ⳒƓ坘教䭁⚦ⰺ姡ヒ内⻋嵶偌䵲泋忇ⴐ易Ⰸᙕ⣬噹㉂䢥痊獶ୌ\u2D7D櫉墅ᓶ啓䯙\u243Dⱚ凹✌砥㤪ᝇừ癱\u08C2畼狿\u05F5䪎嚈⬌哵㰨笅㷋帶൮湰峂㚒唊\u05EB䩷ᘍ䨌寁⨊䅥䷫⽶Ǌ\u2E7A᳒祴\u1AF4⚯涾嘮\u2FDC堭㿢䤅䒊tཉ⍳㳄ն\u0AD8㗞ਠǔᇈ幻䛆ղ⋦Ɵ⨰喎ⵆ噭㬺吅柫㤷⧊ᄻ⛚ᅠ\u1AB2㗼毩嘞⣈奭㴪琱\u1A8Bӷ绎ᑻ㣄煻⋵斢橸䓟श嚱ㄪ砵妫摷烋悾䋄▞⋂㔭ଵ啋\u2E76壥\u2D2A淉侣䤴〬歸㕆ㅮ媴ᔴⰕ嘱⣶寱㪂丵届硵痨䮻ዎ᪄拣䖋\u0A44埫䢂卅㮪挡ㅋᬇ㗎ⅸ\u12C7涕嬝Ȟ樣嚛⨮僎⡪疙䘼℧ℐ祳㣌䉸瓀◱橣垲⢤助㝪撙啋ͺ䥋➺狓ᵺԓᕙ樬哙ⶮ命⒤䦕扪旷䯎扶狑ᶇ䫢┶\u2B8B堙ⴙ捞㻬朙伓⚦勌牿\u0ADE⦚紓\u2E4A㊃笗Ⰿ᳞\u2E72冪ⳋ䀴狍ཹ损ᕵ⫞㙓䎪坯ⵊ孝㍬䗱ᅪմᇱ犻⫊䅹⫔嗛⍊吠⬆Ꮭ₊泵࣫畷⤬⵿罊繾㝳畗珯嗵⳺庹∢涪⪂絴䛮▶\u0CDA畱欃嘞䩆咛ෆ喡⥒䛵栊翵ථ\u2D78ǎ\u2D6A泬憏\u2B60㟭\u2E5A哵㞊琭ᗒ䃷悍罷㫏䆈壳啭䯮嘮Ⱖ庣␊埵ཱུࠫ喬硲䇁ֆ\u1AD8\u0D98即ᚮ⬚儕㒦墡㒋棵⢎湶僟ઍː㗎ੇ\u1716\u2BB9⪭☺兕嘺㳷ᕫ;ۚք狣攤氀埫\u2E46僭ㆺ䢭\u1C8A˴绌Ṽᛚ⍽\u1ADF\u05FB䯙咳⠠\u0EFD凒㙾狄㖽\u0AF9坐洶嬕\u2B66亭崺\u0A54⧊䭴㇍禊䜔㘔毬㜈泑報㧢罕ᬺ۷ಈࣺ㇜癳䜍൭ஐ坨⪢刃Ⲃ廥笺⛶䷏\u0B7E㛍军⌝㖾\u1AE2㜊亡嘙⩆埕嶊仴㩍ӹᩝ禎㢳㖆歲㜋\u2E77ἳ⭆䷕区Ỷ䭨ӹ壒浰㴎ᖏ䎨嗜\u2FD6凥㵆有ដ⟤⒬䉰槃䖚㬃ᗍᨮ\u181C⤉嘑\u2BC6䅴䉂\u0DC7\u2E48➱盀፣嫭䕫⌦㚉\u2BAE哜㇚壭⋲䎚\u05EF屵᧟ᶔ曢痥⌴坢ⴘ婎⽚竭㉫䙶\u2BCB\u0CF3姓煻朣䶧᪦㑗⧝ᕽ⟪澵玺ݔ㓊秆(纔⪱疢䯪堗Ⲿ塝㝬䖹㘊岶㟈⽽廆啹窰ป櫧堏䢪师┍㸕\u08D3㼘㫈糹秂瑱竻疱涁㒺⼞坵㢊津ᕫ䣗΅ί㻄皛粿疥䫏垠楥墉ⳟḭ绊ষぉ恹嫍ᦂڨ൩姿坲殀坫㺶䍍瓓睶⫈ٲ惂䎗ᓶ⺈ᮊ摒桺妣⬦壑壪偕咉\u0DBCᗄ\u2B77ᚫ╽䭫ᜰ⤞卙₦杍〻䑗㻋桽燃皋ܙⵕ䯄ᖊ梁尕⁶䁕嘺灖ඏ\u187F⇈沏ڡ煆᮴ᑺ漆崃㎦䆭洪熧ᇋ烵ᇖ\u0D99囬㕺ᮼ㞈洱境\u2EF6䳕皊ᑔ㇌棼痀ᮀ㛗⸂᮴㓃⼩塃〺翍棻㇕掏䓴ᛌ᭺䚿ֱ嫃㐦歭埖㖺嶭⳺๗潋磺䷍捫\u1AF7旀猳㔘汕ẃ⋦䂍図ᨶ㭭㡳䅚宋烽ⷓ䫁昘梹嚳⼼屭\u0CFBᷕ绎⓾⧂厐抵㗊殌㟋Ⱪ嵃ⅺ䩭粺\u16F5\u1A8E皽᷆㮒磰ᖄ猣噎消峛\u2B94晭抺秄積ᓶ⧂㎄暩浼Ⰻ唻⩝廳\u2B96娍愊䱶⾉孳㷒ᶖ盱㥓ᬱᛛ⸉孎↖咕冓ń⾏ታ槜ᶘ曞涋ᰓ啜⩙兑ↆ櫥▻䭔叉⥰坂獤໋䷧᪀皼樤匂㈆曭㾫䗵溉坹䏝㍳暡\u244F\u1AB5㥏ⷳᗞ㸶䘕畒䣗柊ᗈ廈䮈ᛌⷄ掑㛱浥匫↮䐕်篶ᆈά廂㵡ᛞ喐દ哰\u2D6C勧ㄚ繍尋ʕ㕉ድ惁纚⒪⸊䫉㟦\u2BA5嗧⚦棍湻㶇䨑沵⫕\u2B89沮ⶊ㯴瞍Ⰻ\u171D㥐孍ᇓ䋙斈ᙺᗋ㖘囑唴淎✉棌妇⫮䋍孛䚔喋᧺䇍䍡园ⷦ宵䚰澶暕㚺呥⨺㺖翊櫽泄ᮈ\u1CA7ⶖ੍㗝⢅娕㢦淍䘻ℇ筋\u1AF7\u0BD9歵嚹թ㰜瓅橜嗻㊦傭㳻婖䇊烾痃殜㛏ⵅ婸㕰業崃†ⴹ⺰䷐痿ᜥ涱坃㩖䲍䔻槕猍図ᇗ殜猒淮\u1ABA甕涢匷㯖濍⇛秗ⲍ\u0DF0㷜㝮㚦涽㮔㞕涻傳♎瑥䗚䮕㬊ӿ糁皛㛊㖋嫛㒍檻怃㤪擕ンϖ㊉国\u2BCE宝㜟ⷀ㰛㑫䡛嶗⇶杕寺矔瞍滿寈卫滽ⷃ嫹摝湶帑⣆娘焣毕殏㷱篕兮挝洿寒㜻⤻ᆃ同崵\u17FB㿕很ǽ槛兽✏淈竁璫依娯┼昡沚䷄羋揿緁简༜䗠㫩畣桙岧㚆宾㙃傔⏈Ͼ᷑ޝỢ畤特瞇䪈嫓⾆刽汚猧㚋懲勁玚暦㷯ᰈ璢洃嗧㚚坅湪㵷☉勲叕❵༆ᶙ\u2BE7啢渊嫵㢶䛍剺㒗䔎毴參䞊庹ᶔଭ璂\u0873啬ザ橥ࠋ朔⸈㕸ᙞ❤仪ⷖ窇៊盁傇㗮宽ⵛ圗⁌秸ᗈ\u2B77庴时㪹㞉渫⫫㡎惍䭛嚔洋移旀Οᚹⵒ叜瞛欅儷∦䞽椚Ε傋䟶珏噴⼒\u0D49箓瘣\u2BAF儷㉎幽挚⽴㌈◽䯇ឈ墰渜\u1AFA皷氻巭㻎䞝࿋ঔ禌䍲毛歭\u2EFA崶ᩛ眯椻孖㈺潽ᯛ໖ࠊ\u0FFCῑ坡㚷攲ᨳ㙯榱嵟㇖洝稚ఖ⬌替ᯘ䵴溱絋㭊\u175D渽劣≖曽û簗扎۵㯘罼盍文篯璗榻塻⏖歽Ⓔ㷗瀈㿲淗㝾泉淖ᮞ璮樐刟⁖羍纊ᥕލ㿻㿝羑\u2EFD\u0DF1ᭇ㞝漠㟷㎺厭叻ᕕ⪉\u2EFE䋌ཿ漙涃对産殗ᵛ㌡廵翛⟕∊緵㝄྆Șᆕ嫶ᒰᯧ卅㘾慝碚䟗咍㏲₃䖘璢㶴ر睞殇偅⎾瓭㞓ㇵᠹᏰ佖侉拮㵢߉皬殂宯␎惝夊䴕獩珺ⅇę嫟洲ࠋ囬\u0DF5᳀氤绾ᖻ紖慏䡞滙⽰交ⶮ箘癢橷式⽾塽猦緷\u17ED㯸叚愔伝㷂㩌\u0D3B櫐㫋㻾殣Κვ熎᯾矅妇座㶮積ᑵⲐ㎏\u31EE禽愚ℭ䖍篷矀ჴ↹⤶㩣瓙汓傿≁咝Ż峅名߶࿀従ᛞ\u2D69٢皃\u2BE8㻇㍞羽ॳ䨔氍ヷ࿚䅥㼄嶝竲璷殫圿㙢䙣㧒䴯䜉ᑚ懖㽶⺮䌵㬛睏澶唋㚞彣㨺嶵䍭旲每坨懡綃߷琿檸㹟⚁烣䋛䧗專ⱘ翉ヾ\u1ADEීއᘚ瀕嗐溞吝业盔瀈㣱墅᭶懅䎇ߏ瑠᳘㛿⻎皭֧㜭ஏ翶翌捰滷䏬檴㑀Ḹ㠠搞壣猫夵宏䁟㢖\u3100ሇ䌭㮧璫䧛ṱ⠞瀝羧⾕ℾ䁓҂㮜拫嵷ᨾ\u0DF6濟埠琎汭冪ᠬ⸸䏻႔䃹ƳⰤ٩࿈滢嫀缱瑝悛ㄖ࠹୶篌䤇⚸㶯宇㔘楰㯠浡媕䪛ᤗÌ\u23FC䟅ྏᇷ浂犢☨ᴄ㜤②䲣㩧⿔㐺司㷖⤋ƴ͚犬ൊᵞᮍ✾壃忺\u1AAE℈㪳傆摠潕⏊Ⱅ\u0E7A懷庫Ⓤ䲽䮚粖ሽ毲炖惽䈕畎簍ཉ棥夝ⵁ澣㨋⺮㰼߲䢖ᄛ䈔∞筍\u0C70栬㐀愦拍缦纯̏ٔ璜ទ۪㹁䙭瓇港刿⻁拍Ἒᅵ㈹⟳䢔僷切电ܳ盂ㅺ姙⋶䚃⋧㸖⒎㑓⢖儕亦䏳ߋྔᵻᆋⲁ曵皦ต⬿捶墌皋㈃䎔\u0BDB疆\u1C38㋃⟑䚭㽻䜯㘾嫹ᢑ売缎䎃䝃㑮ᥘ㔟⚑廣㖧㉕㒌\u2E5Fῂ垂缏磴䠞啼᥆姘极糹奨箬堍書㲂㤜缗䎇䟛㑾᧱喘甁巣㻂㢮䗊㱔㢘焍成⌰䘡බ潍娘搱灥⑦㾬儻繜碋ㄘ璽巍嬱ఽ溄㼥㤎瑃֧ྕऽ壷ʃ㭽滔⎃ܽ喝涢㟘椡珣尧摶\u193F䓶䊟垍ᇽ⎈竨\u0CF0䫄㬘抎囕祧抯栺㉗䊔㮍Ȗ⏠篅ဈ\u2E5D占瓱堽ħ啖㢹擻㛄⣯ᇖΜ❑ᙫ沀㖯㭱楝ن爮ᔼ\u0A53ᒆ┈䇹Ђ♄仙⸰㳄湱滃焦੬ૂ ")
//                    .getBytes("ISO-8859-1"); // for copy/paste reasons ISO-8859-1 seems to work, but others don't at all
//        } catch (UnsupportedEncodingException e) {
//            return;
//        }
        
//        System.out.println("String[] names = new String[]{ \"" + StringKit.join("\",\"", names) + "\"\n};");

//        StringBuilder sb = new StringBuilder(12 * COUNT);
//        for (int i = 0; i < COUNT; i++) {
//            StringKit.appendHex(sb.append("0x"), FLESURRECT[i]).append(", ");
//            if((i & 3) == 3) sb.append('\n');
//        }
//        System.out.println("int[] FLESURRECT = new int[]{\n"+ sb + "};");
//        System.out.println("int[] reverses = new int[]{ "+ StringKit.join(",", reverse) + "\n};");
//        System.out.println("float[] lumas = new float[]{ "+ StringKit.join("f,", lumas) + "f\n};");
//        System.out.println("float[] cos = new float[]{ "+ StringKit.join("f,", cos) + "f\n};");
//        System.out.println("float[] cgs = new float[]{ "+ StringKit.join("f,", cgs) + "f\n};");
        float cgf, cof, yf;
        for (int cr = 0; cr <= cgLim; cr++) {
            cgf = (float) cr / cgLim - 0.5f;
            for (int cb = 0; cb <= coLim; cb++) {
                cof = (float) cb / coLim - 0.5f;
                for (int y = 0; y <= yLim; y++) {
                    final int c2 = cr << shift2 | cb << shift1 | y;
                    if (paletteMapping[c2] == 0) {
                        yf = (float) y / yLim;
                        double dist = Double.POSITIVE_INFINITY;
                        for (int i = 1; i < COUNT; i++) {
                            if (Math.abs(lumas[i] - yf) < 0.2f && dist > (dist = Math.min(dist, difference(lumas[i], cos[i], cgs[i], yf, cof, cgf))))
                                paletteMapping[c2] = (byte) i;
                        }
//                        if(paletteMapping[c2] == 0)
//                            System.out.println("what gives? y=" + y + ", co=" + cb + ", cg=" + cr);
                    }
                }
            }
        }
//        System.out.println();
//        try {
//            String compressed = LZSPlus.compress(new String(paletteMapping, "ISO-8859-1"));
//            System.out.println('"' + compressed + '"');
//            paletteMapping = LZSPlus.decompress(compressed).getBytes("ISO-8859-1");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        System.out.println();

        float adj;
        int idx2;
        for (int i = 1; i < COUNT; i++) {
            int rev = reverse[i], y = rev & yLim, match = i;
            yf = lumas[i];
            cof = cos[i];
            cgf = cgs[i];
            ramps[i][1] = (byte)i;//Color.rgba8888(DAWNBRINGER_AURORA[i]);
            ramps[i][0] = 9;//15;  //0xFFFFFFFF, white
            ramps[i][2] = 1;//0x010101FF, black
            ramps[i][3] = 1;//0x010101FF, black
            for (int yy = y + 2, rr = rev + 2; yy <= yLim; yy++, rr++) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cos[idx2], cgs[idx2], yf, cof, cgf) > THRESHOLD) {
                    ramps[i][0] = paletteMapping[rr];
                    break;
                }
                adj = 1f + ((yLim + 1 >>> 1) - yy) * 0x1p-10f;
                cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                cgf = MathUtils.clamp(cgf * adj + 0x1.8p-10f, -0.5f, 0.5f);

//                cof = (cof + 0.5f) * 0.984375f - 0.5f;
//                cgf = (cgf - 0.5f) * 0.96875f + 0.5f;
                rr = yy
                        | (int) ((cof + 0.5f) * coLim) << shift1
                        | (int) ((cgf + 0.5f) * cgLim) << shift2;
            }
            cof = cos[i];
            cgf = cgs[i];
            for (int yy = y - 2, rr = rev - 2; yy > 0; rr--) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cos[idx2], cgs[idx2], yf, cof, cgf) > THRESHOLD) {
                    ramps[i][2] = paletteMapping[rr];
                    rev = rr;
                    y = yy;
                    match = paletteMapping[rr] & 255;
                    break;
                }
                adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                cgf = MathUtils.clamp(cgf * adj - 0x1.8p-10f, -0.5f, 0.5f);
                
//                cof = (cof - 0.5f) * 0.984375f + 0.5f;
//                cgf = (cgf + 0.5f) * 0.984375f - 0.5f;
                rr = yy
                        | (int) ((cof + 0.5f) * coLim) << shift1
                        | (int) ((cgf + 0.5f) * cgLim) << shift2;

//                cof = MathUtils.clamp(cof * 0.9375f, -0.5f, 0.5f);
//                cgf = MathUtils.clamp(cgf * 0.9375f, -0.5f, 0.5f);
//                rr = yy
//                        | (int) ((cof + 0.5f) * 63) << 7
//                        | (int) ((cgf + 0.5f) * 63) << 13;
                if (--yy == 0) {
                    match = -1;
                }
            }
            if (match >= 0) {
                for (int yy = y - 3, rr = rev - 3; yy > 0; yy--, rr--) {
                    if ((idx2 = paletteMapping[rr] & 255) != match && difference(lumas[idx2], cos[idx2], cgs[idx2], yf, cof, cgf) > THRESHOLD) {
                        ramps[i][3] = paletteMapping[rr];
                        break;
                    }
                    adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                    cof = MathUtils.clamp(cof * adj, -0.5f, 0.5f);
                    cgf = MathUtils.clamp(cgf * adj - 0x1.8p-10f, -0.5f, 0.5f);

//                    cof = (cof - 0.5f) * 0.96875f + 0.5f;
//                    cgf = (cgf + 0.5f) * 0.96875f - 0.5f;
                    rr = yy
                            | (int) ((cof + 0.5f) * coLim) << shift1
                            | (int) ((cgf + 0.5f) * cgLim) << shift2;

//                    cof = MathUtils.clamp(cof * 0.9375f, -0.5f, 0.5f);
//                    cgf = MathUtils.clamp(cgf * 0.9375f, -0.5f, 0.5f);
//                    rr = yy
//                            | (int) ((cof + 0.5f) * 63) << 7
//                            | (int) ((cgf + 0.5f) * 63) << 13;
                }
            }
        }
        
        System.out.println("public static final byte[][] JUDGE_RAMPS = new byte[][]{");
        for (int i = 0; i < COUNT; i++) {
            System.out.println(
                      "{ " + ramps[i][3]
                    + ", " + ramps[i][2]
                    + ", " + ramps[i][1]
                    + ", " + ramps[i][0] + " },"
            );
        }
        System.out.println("};");

        System.out.println("public static final int[][] JUDGE_RAMP_VALUES = new int[][]{");
        for (int i = 0; i < COUNT; i++) {
            System.out.println("{ 0x" + StringKit.hex(palette[ramps[i][3] & 255])
                    + ", 0x" + StringKit.hex(palette[ramps[i][2] & 255])
                    + ", 0x" + StringKit.hex(palette[ramps[i][1] & 255])
                    + ", 0x" + StringKit.hex(palette[ramps[i][0] & 255]) + " },"
            );
        }
//        for (int i = 0; i < COUNT; i++) {
//            System.out.println("{ 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][0] & 255]))
//                    + ", 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][1] & 255]))
//                    + ", 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][2] & 255]))
//                    + ", 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][3] & 255])) + " },"
//            );
//        }
        System.out.println("};");
        for (int i = 0; i < COUNT; i++) {
            col.set(palette[i & 255]).clamp();
            display.putString(i >>> 1 & 0xF0, i & 31, String.format("   %08X   ", palette[i]), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
        }
            //            for (int j = 0; j < 4; j++) {
                //float cf = col.set(FLESURRECT[ramps[i][j] & 255]).clamp().toFloatBits();
//                display.put((i >>> 3) << 3 | j << 1, i & 7, '\0', cf);
//                display.put((i >>> 3) << 3 | j << 1 | 1, i & 7, '\0', cf);
//                display.putString((i >>> 5) * 20, i & 31, "  " + StringKit.padRightStrict(col.name.substring(7), ' ', 18), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            }
            
//            for (int j = 0; j < 4; j++) {
//                display.put((i >>> 5) << 3 | j << 1, i & 31, '\0', DAWNBRINGER_AURORA[ramps[i][j] & 255]);
//                display.put((i >>> 5) << 3 | j << 1 | 1, i & 31, '\0', DAWNBRINGER_AURORA[ramps[i][j] & 255]);
//            }

//            col = SColor.DAWNBRINGER_AURORA[i];
//            display.putString((i >>> 5) * 20, i & 31, "  " + StringKit.padRightStrict(col.name.substring(7), ' ', 18), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            display.putString(i >>> 2 & 0xF8, i & 31, String.format("   %02X   ", i), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            SColor col = SColor.DAWNBRINGER_AURORA[i];
//        for (int i = 0; i < 48; i++) {
//            Color.rgba8888ToColor(col, RINSED[i]);
//            col.clamp();
//            display.putString(i >>> 3 & 0xFC, i & 31, String.format("%3d ", i), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            int cf = NumberTools.floatToReversedIntBits(col.toFloatBits());
//            System.out.printf("0x%08X, ", cf | (cf & 2) >>> 1);
//            if((i & 7) == 7)
//                System.out.println();
//        }
//        for (int i = 48; i < 56; i++) {
//            Color.rgba8888ToColor(col, RINSED[i]);
//            Color.abgr8888ToColor(col, col.toEditedFloat(0f, -0.22f, -0.23f));
//            col.clamp();
//            display.putString(i >>> 3 & 0xFC, i & 31, String.format("%3d ", i), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            int cf = NumberTools.floatToReversedIntBits(col.toFloatBits());
//            System.out.printf("0x%08X, ", cf | (cf & 2) >>> 1);
//            if((i & 7) == 7)
//                System.out.println();
//        }
//        for (int i = 56; i < 216; i++) {
//            Color.rgba8888ToColor(col, RINSED[i]);
//            col.clamp();
//            display.putString(i >>> 3 & 0xFC, i & 31, String.format("%3d ", i), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            int cf = NumberTools.floatToReversedIntBits(col.toFloatBits());
//            System.out.printf("0x%08X, ", cf | (cf & 2) >>> 1);
//            if((i & 7) == 7)
//                System.out.println();
//        }
//
//        for (int i = 216; i < 224; i++) {
//            Color.rgba8888ToColor(col, RINSED[i]);
//            Color.abgr8888ToColor(col, col.toEditedFloat(0f, 0.3f, 0.08f));
//            col.clamp();
//            display.putString(i >>> 3 & 0xFC, i & 31, String.format("%3d ", i), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            int cf = NumberTools.floatToReversedIntBits(col.toFloatBits());
//            System.out.printf("0x%08X, ", cf | (cf & 2) >>> 1);
//            if((i & 7) == 7)
//                System.out.println();
//        }
//
//        for (int i = 224; i < 256; i++) {
//            Color.rgba8888ToColor(col, RINSED[i]);
//            col.clamp();
//            display.putString(i >>> 3 & 0xFC, i & 31, String.format("%3d ", i), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            int cf = NumberTools.floatToReversedIntBits(col.toFloatBits());
//            System.out.printf("0x%08X, ", cf | (cf & 2) >>> 1);
//            if((i & 7) == 7)
//                System.out.println();
//        }

//        float[][] random = new float[32][3];
//        random[0][0] = 0.1f;
//        random[0][1] = 0.05f;
//        random[0][2] = 0.04f;
//        random[1][0] = 0.45f;
//        random[1][1] = 0.03f;
//        random[1][2] = 0.03f;
//        random[2][0] = 0.75f;
//        random[2][1] = -0.02f;
//        random[2][2] = 0.03f;
//        random[3][0] = 0.95f;
//        random[3][1] = -0.01f;
//        random[3][2] = -0.03f;
//        for (int i = 4; i < random.length; i++) {
//            random[i][0] = vdc(2, i);
//            final float rad = vdc(13, i) * 6.2831855f, adj = NumberTools.sin(random[i][0] * 3.14159265358979323846f) * 0.4f + 0.1f;
//            random[i][1] = NumberTools.cos(rad) * adj;
//            random[i][2] = NumberTools.sin(rad) * adj;
//        }
//        Arrays.sort(random, new Comparator<float[]>() {
//            @Override
//            public int compare(float[] o1, float[] o2) {
//                return (int)Math.signum(o1[0] - o2[0]);
//            }
//        });
//        for (int i = 0; i < 32; i++) {
//            ycc(random[i]);
//        }
        
//        for (int i = 0; i < 32; i++) {
//            SColor db = SColor.DAWNBRINGER_32[i];
//            display.putString(0, i, "                                ", db, db);
//            display.putString(1, i, db.name, db.value() < 0.7f ? SColor.WHITE : SColor.BLACK, db);
//        }

//        for (int h = 0; h < 7; h++) {
//            for (int v = 0; v < 27; v++) {
//                SColor cw = SColor.COLOR_WHEEL_PALETTE[h * 27 + v];
//                display.putString(h * 20, v, StringKit.padRightStrict(cw.name.substring(3), 20), cw.value() < 0.7f ? SColor.WHITE : SColor.BLACK, cw);
//                //display.put(h, v, scc.getHSV(h * (1f / gridWidth), 0.75f, (8 - v) / 8f));
//            }
//        }
//        for (int i = 15; i >= 8; i--) {
//            SColor.colorFromFloat(tmp, SColor.floatGetHSV(0, 0, i / 15f, 1f));
//            System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
//        }
//        System.out.println();
//        for (int i = 7; i >= 0; i--) {
//            SColor.colorFromFloat(tmp, SColor.floatGetHSV(0, 0, i / 15f, 1f));
//            System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
//        }
//        System.out.println();
//        OrderedMap<String, Float> smallHues = Maker.makeOM(
//                "Red", 0.03125f * 0.5f,
//                "Apricot", 0.03125f * 3.05f,
//                "Yellow", 0.03125f * 5.3f,
//                "Green", 0.03125f * 10.5f,
//                "Cyan", 0.03125f * 15.85f,
//                "Blue", 0.03125f * 19f,
//                "Violet", 0.03125f * 23.5f,
//                "Magenta", 0.03125f * 29f);
//        for (int i = 0; i < 8; i++) {
//            float hue = smallHues.getAt(i);
//            if(i == 2)
//            {
//                show(hue, 0.325f, 1f);
//                show(hue, 0.65f, 1f);
//                show(hue, 0.775f, 0.95f);
//                show(hue, 0.875f, 0.875f);
//                show(hue, 0.95f, 0.775f);
//                show(hue, 1f, 0.675f);
//            }
//            else if (i != 4) {
//                show(hue, 0.5f, 1f);
//                show(hue, 0.725f, 1f);
//                show(hue, 0.85f, 0.925f);
//                show(hue, 0.95f, 0.8f);
//                show(hue, 1f, 0.65f);
//                show(hue, 1f, 0.45f);
////                show(hue, 0.6f, 1f);
////                show(hue, 0.725f, 1f);
////                show(hue, 0.85f, 0.95f);
////                show(hue, 0.925f, 0.9f);
////                show(hue, 0.95f, 0.8f);
////                show(hue, 1f, 0.7f);
////                show(hue, 1f, 0.6f);
////                show(hue, 1f, 0.5f);
//            }
//            else
//            {
//                float hue2 = 0.03125f * 12f; // green with slightly more blue
//                show(hue2, 0.3f, 0.8f);
//                show(hue2, 0.575f, 0.75f);
//                show(hue2, 0.7f, 0.675f);
//                show(hue2, 0.8f, 0.575f);
//                show(hue2, 0.875f, 0.475f);
//                show(hue2, 0.925f, 0.35f);
//            }
//            if(i == 0)
//            {
//                float hue2 = 0.03125f * 1.75f;
//                show(hue2, 0.3f, 1f);
//                show(hue2, 0.375f, 0.925f);
//                show(hue2, 0.475f, 0.8f);
//                show(hue2, 0.55f, 0.65f);
//                show(hue2, 0.625f, 0.475f);
//                show(hue2, 0.7f, 0.3f);
//            }
//            else if (i != 2) {
//                show(hue, 0.25f, 1f);
//                show(hue, 0.325f, 0.925f);
//                show(hue, 0.425f, 0.8f);
//                show(hue, 0.5f, 0.65f);
//                show(hue, 0.575f, 0.475f);
//                show(hue, 0.65f, 0.3f);
//
////                show(hue, 0.3f, 1f);
////                show(hue, 0.35f, 0.925f);
////                show(hue, 0.4f, 0.85f);
////                show(hue, 0.45f, 0.775f);
////                show(hue, 0.5f, 0.675f);
////                show(hue, 0.55f, 0.55f);
////                show(hue, 0.6f, 0.425f);
////                show(hue, 0.65f, 0.3f);
//            }
//            else
//            {
//                float hue2 = 0.03125f * 3.2f; // between orange and yellow
//                show(hue2, 0.175f, 0.975f);
//                show(hue2, 0.35f, 0.95f);
//                show(hue2, 0.5f, 0.875f);
//                show(hue2, 0.625f, 0.75f);
//                show(hue2, 0.725f, 0.6f);
//                show(hue2, 0.8f, 0.425f);
//            }
//        }
        //stage.addActor(display);

        //This block, when uncommented, will generate the color wheel palette code for SColor and print it to stdout.
//        String template = "NAME\tFEDCBA\tName";
//        // 0 red, 1 brown, 2 orange, 3 apricot, 4 gold, 5 yellow, 6 chartreuse, 7 lime, 8 honeydew, 10 green, 12 jade,
//        // 14 seafoam, 16 cyan, 17 azure, 19 blue, 21 sapphire, 23 indigo, 24 violet, 26 purple, 28 magenta, 30 rose
//        String[] names = {"Red", "Orange", "Brown", "Apricot", "Gold", "Yellow", "Chartreuse", "Lime", "Honeydew", null,
//                "Green", null, null, "Jade", "Seafoam", null, "Cyan", "Azure", null, "Blue", null, "Sapphire",
//                null, "Indigo", "Violet", null, "Purple", null, "Magenta", null, "Rose", null};
//        OrderedMap<String, Float> hues = Maker.makeOM("Red", 0.03125f * 0f,
//                "Orange", 0.03125f * 2.3f,
//                "Brown", 0.03125f * 2.5f,
//                "Apricot", 0.03125f * 3.15f,
//                "Gold", 0.03125f * 4.5f,
//                "Yellow", 0.03125f * 5.3f,
//                "Chartreuse", 0.03125f * 6.5f,
//                "Lime", 0.03125f * 7f,
//                "Honeydew", 0.03125f * 8f,
//                "Green", 0.03125f * 10.05f,
//                "Jade", 0.03125f * 11.9f,
//                "Seafoam", 0.03125f * 14.1f,
//                "Cyan", 0.03125f * 15.85f,
//                "Azure", 0.03125f * 17.1f,
//                "Blue", 0.03125f * 19f,
//                "Sapphire", 0.03125f * 20.8f,
//                "Indigo", 0.03125f * 23f,
//                "Violet", 0.03125f * 24.2f,
//                "Purple", 0.03125f * 25.7f,
//                "Magenta", 0.03125f * 27.8f,
//                "Rose", 0.03125f * 29.7f);
//        OrderedMap<String, Float> satMods = Maker.makeOM("Red", 0f,
//                "Orange", 0.025f,
//                "Brown", -0.22f,
//                "Apricot", -0.05f,
//                "Gold", 0.05f,
//                "Yellow", 0.02f,
//                "Chartreuse", -0.02f,
//                "Lime", 0.1f,
//                "Honeydew", -0.17f,
//                "Green", 0f,
//                "Jade", -0.13f,
//                "Seafoam", -0.05f,
//                "Cyan", 0.075f,
//                "Azure", -0.05f,
//                "Blue", 0.01f,
//                "Sapphire", -0.04f,
//                "Indigo", 0.09f,
//                "Violet", -0.01f,
//                "Purple", -0.05f,
//                "Magenta", 0.04f,
//                "Rose", 0.06f);
//        OrderedMap<String, Float> valMods = Maker.makeOM("Red", 0.01f,
//                "Orange", 0.02f,
//                "Brown", -0.12f,
//                "Apricot", 0.05f,
//                "Gold", -0.005f,
//                "Yellow", 0.06f,
//                "Chartreuse", 0.02f,
//                "Lime", -0.06f,
//                "Honeydew", 0.04f,
//                "Green", -0.025f,
//                "Jade", -0.04f,
//                "Seafoam", 0.03f,
//                "Cyan", -0.01f,
//                "Azure", -0.03f,
//                "Blue", -0.01f,
//                "Sapphire", -0.015f,
//                "Indigo", -0.05f,
//                "Violet", -0.02f,
//                "Purple", -0.01f,
//                "Magenta", -0.02f,
//                "Rose", -0.03f);
//        for (int i = 0; i < 32; i++) {
//            String nm = names[i];
//            if(nm == null)
//                continue;
//            Color baseColor = scc.getHSV(hues.getOrDefault(nm, i * 0.03125f), 0.825f + satMods.getOrDefault(nm, 0f), 0.925f + valMods.getOrDefault(nm, 0f));
//            System.out.println(template.replace("Name", "CW " + nm)
//                    .replace("NAME", "CW_" + nm.toUpperCase())
//                    .replace("FEDCBA", baseColor.toString().substring(0, 6).toUpperCase()));
//            System.out.println(template.replace("Name", "CW Faded " + nm)
//                    .replace("NAME", "CW_FADED_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.desaturate(scc.light(baseColor, 0.15f), 0.5f).toString().substring(0, 6).toUpperCase()));
//            System.out.println(template.replace("Name", "CW Flush " + nm)
//                    .replace("NAME", "CW_FLUSH_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.saturate(scc.dim(baseColor, 0.05f), 0.5f).toString().substring(0, 6).toUpperCase()));
//
//            System.out.println(template.replace("Name", "CW Light " + nm)
//                    .replace("NAME", "CW_LIGHT_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.desaturate(scc.light(baseColor, 0.4f), 0.1f).toString().substring(0, 6).toUpperCase()));
//            System.out.println(template.replace("Name", "CW Pale " + nm)
//                    .replace("NAME", "CW_PALE_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.desaturate(scc.light(baseColor, 0.55f), 0.3f).toString().substring(0, 6).toUpperCase()));
//            System.out.println(template.replace("Name", "CW Bright " + nm)
//                    .replace("NAME", "CW_BRIGHT_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.saturate(scc.light(baseColor, 0.35f), 0.5f).toString().substring(0, 6).toUpperCase()));
//
//            System.out.println(template.replace("Name", "CW Dark " + nm)
//                    .replace("NAME", "CW_DARK_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.saturate(scc.dim(baseColor, 0.325f), 0.2f).toString().substring(0, 6).toUpperCase()));
//            System.out.println(template.replace("Name", "CW Drab " + nm)
//                    .replace("NAME", "CW_DRAB_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.desaturate(scc.dim(baseColor, 0.2f), 0.4f).toString().substring(0, 6).toUpperCase()));
//            System.out.println(template.replace("Name", "CW Rich " + nm)
//                    .replace("NAME", "CW_RICH_" + nm.toUpperCase())
//                    .replace("FEDCBA", scc.saturate(scc.dim(baseColor, 0.2f), 0.5f).toString().substring(0, 6).toUpperCase()));
//        }
//        System.out.println();

        // This block, when uncommented, will read in color names and values from ColorData.txt and produce a formatted
        // block of partial Java source as ColorOutput.txt , to be put in SColor.java .
//        String templateFull = "/**\n" +
//            "* This color constant \"Name\" has RGB code {@code 0xFEDCBA}, red `RED, green `GREEN, blue `BLUE, alpha 1, hue `HUE, saturation `SAT, and value `VAL.\n" +
//            "* It can be represented as a packed float with the constant {@code `PACKEDF}.\n" +
//            "* <pre>\n" +
//            "* <font style='background-color: #FEDCBA;>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FEDCBA; color: #000000'>&nbsp;@&nbsp;</font>\n" +
//            "* <font style='background-color: #FEDCBA;>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #FEDCBA; color: #888888'>&nbsp;@&nbsp;</font>\n" +
//            "* <font style='background-color: #FEDCBA;>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FEDCBA; color: #ffffff'>&nbsp;@&nbsp;</font>\n" +
//            "* </pre>\n" +
////            "* <br>\n" +
////            "* <font style='background-color: #ff0000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00ff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000ff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #964b00; color: #000000'>&nbsp;&nbsp;&nbsp;</font>\n" +
////            "* <font style='background-color: #ff0000; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #ffff00; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #00ff00; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #0000ff; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #964b00; color: #FEDCBA'>&nbsp;@&nbsp;</font>\n" +
////            "* <font style='background-color: #ff0000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00ff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000ff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #964b00; color: #000000'>&nbsp;&nbsp;&nbsp;</font></pre>\n" +
//            "*/\n" +
//        "public static final SColor NAME = new SColor(0xFEDCBA, \"Name\");\n\n";
//        String data = Gdx.files.classpath("special/ColorData.txt").readString();
//        String[] lines = StringKit.split(data, "\n"), rec = new String[3];
//        Color c = new Color();
//        StringBuilder sb = new StringBuilder(100000);
//        for (int i = 0; i < lines.length; i++) {
//            tabSplit(rec, lines[i]);
//            Color.argb8888ToColor(c, Integer.parseInt(rec[1], 16) | 0xFF000000);
//            sb.append(templateFull.replace("Name", rec[2])
//                    .replace("NAME", rec[0])
//                    .replace("FEDCBA", rec[1].toUpperCase())
//                    .replace("`RED", Float.toString(c.r))
//                    .replace("`GREEN", Float.toString(c.g))
//                    .replace("`BLUE", Float.toString(c.b))
//                    .replace("`HUE", Float.toString(scc.getHue(c)))
//                    .replace("`SAT", Float.toString(scc.getSaturation(c)))
//                    .replace("`VAL", Float.toString(scc.getValue(c)))
//                    .replace("`PACKED", Float.toHexString(c.toFloatBits()))
//            );
//            //System.out.println("Processed " + i);
//        }
//        Gdx.files.local("ColorOutput.txt").writeString(sb.toString(), false);
    }

    private static double difference(float y1, float cb1, float cr1, float y2, float cb2, float cr2) {
//        float angle1 = NumberTools.atan2_(cb1, cr1);
//        float angle2 = NumberTools.atan2_(cb2, cr2);
        return (y1 - y2) * (y1 - y2) + ((cb1 - cb2) * (cb1 - cb2) + (cr1 - cr2) * (cr1 - cr2)) * 0.325;
                //+ ((angle1 - angle2) % 0.5f + 0.5f) % 0.5f;
    }

    public static void tabSplit(String[] receiving, String source) {
        int dl = 1, idx = -1, idx2;
        for (int i = 0; i < 2; i++) {
            receiving[i] = safeSubstring(source, idx+dl, idx = source.indexOf('\t', idx+dl));
        }
        if((idx2 = source.indexOf('\t', idx+dl)) < 0)
        {
            receiving[2] = safeSubstring(source, idx+dl, source.length());
        }
        else
        {
            receiving[2] = safeSubstring(source, idx+dl, idx2);
        }
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        luma = NumberTools.zigzag((System.nanoTime() >>> 27 & 0xFFFL) * 0x1.4p-7f) * 0.5f + 0.5f;
        Gdx.gl.glClearColor(1f - luma, 1f - luma, 1f - luma, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.graphics.setTitle("Current luma: " + luma);

//        Gdx.graphics.setTitle("YCwCm demo at 66% luma");
//        for (float cb = -0.625f; cb <= 0.625f; cb += 0x1p-6f) {
//            for (float cr = -0.626f; cr <= 0.625f; cr += 0x1p-6f) {
//                ycc(luma, cb, cr);
//            }
//        }
        
        for (float cb = -1f; cb <= 1f; cb += 0x1p-8f) {
            for (float cr = -1f; cr <= 1f; cr += 0x1p-8f) {
                ycc(luma, cb, cr);
            }
        }
//        stage.draw();
        
        viewport.update(totalWidth, totalHeight, true);
        viewport.apply(true);
        batch.begin();
        tcf.draw(batch, colors, 0, 0);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        totalWidth = width;
        totalHeight = height;
        stage.getViewport().update(width, height, true);
    }
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Colors";
        config.width = totalWidth;
        config.height = totalHeight;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new ColorTest(), config);
    }

}