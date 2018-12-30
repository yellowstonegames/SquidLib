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
import squidpony.LZSPlus;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidmath.MathExtras;

import java.io.UnsupportedEncodingException;

import static squidpony.StringKit.safeSubstring;
import static squidpony.squidgrid.gui.gdx.SColor.DAWNBRINGER_AURORA;
import static squidpony.squidgrid.gui.gdx.SColor.floatGet;

/**
 * Created by Tommy Ettinger on 12/27/2016.
 */
public class ColorTest extends ApplicationAdapter {
    /**
     * In number of cells
     */
//    private static int gridWidth = 160;
    private static int gridWidth = 64;
//    private static int gridWidth = 103;
//    private static int gridWidth = 140;
    /**
     * In number of cells
     */
    private static int gridHeight = 32;
//    private static int gridHeight = 27;

    /**
     * The pixel width of a cell
     */
//    private static int cellWidth = 5;
    private static int cellWidth = 10;
    /**
     * The pixel height of a cell
     */
//    private static int cellHeight = 5;
    private static int cellHeight = 25;

    private static int totalWidth = gridWidth * cellWidth, totalHeight = gridHeight * cellHeight;



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
};

    private Stage stage;
    private SpriteBatch batch;
    private Viewport viewport;
    private TextCellFactory tcf;
    private SquidLayers display;
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
    public static float floatGetYCwCm(float luma, float warm, float mild, float opacity) {
        if (mild >= -0.0039f && mild <= 0.0039f && warm >= -0.0039f && warm <= 0.0039f) {
            return floatGet(luma, luma, luma, opacity);
        }

        // the color solid should be:
        //                             > warm >
        //          rose
        //     violet    red
        // blue               orange
        //     cyan      yellow
        //          green
        // \/ mild \/
        
        // so, warm is effectively defined as the lack of blue.
        // and mild is, loosely, presence of green.
        
        // luma is defined as (r * 5 + g * 9 + b * 2) / 16
        // or r * 0x.5p0f + g * 0x.9p0f + b * 0x.2p0f
        // warm is the warm-cool axis, with positive warm between red and yellow and negative warm between violet and cyan
        // warm is defined as (r * 7 + g * 1 + b * -8) / 16
        // or b * 0x.6p0f + g * 0x.2p0f + r * -0x.8p0f
        // mild is the green-purple axis, with positive mild between cyan and yellow, negative full between violet and red
        // mild is defined as (r * -8 + g * 8 + b * 0) / 16
        // or r * -0x0.8p0f + g * 0x0.8p0f
        
        //r = (luma * 4 + warm) * 0x8p-9f + mild * 0x1Bp-9f;
        //g = (luma * 4 + warm) * 0x8p-9f - mild * 0x25p-9f;
        //b = warm * -2 - r * 0xEp-4f - g * 0x2p-4f;
        //
        //

        // by is the diagonal from blue at 0.75 to yellow at -0.75
        // r * -0.25f + g * -0.5f + b * 0.75f
        // should be
        // r * -0.25f + g * -0.25f + b * 0.5f
        // gr is the diagonal from green at 0.75 to red at -0.75
        // r * -0.75f + g * 0.5f + b * 0.25f
        // should be
        // r * -0.5f + g * 0.5f

        //r = t + mild * 0x1Bp-9f;
        //g = t - mild * 0x25p-9f;
        //b = warm * -2 - r * 0xEp-4f - g * 0x2p-4f;
        
//        warm -= luma * 0.5f;

        final float t = (luma * 4 + warm) * 0x8p-5f;
        float r = t + mild * 0x1Bp-5f;//t - cool;//luma * 4 - cool * 0x0.8p0f + full * 0x0.3p0f;
        if(r < 0f || r > 1f) 
            r = MathExtras.clamp(r,0,1);
        //return -0x1.fefefep125F;//SColor.CW_GRAY
        float g = t - mild * 0x25p-5f;//0.5f - full;
        if(g < 0f || g > 1f) 
            g = MathExtras.clamp(g,0,1);
        //return -0x1.fefefep125F;//SColor.CW_GRAY
        float b = luma * 4 - r * 0x5p-2f - g * 0x9p-2f;//t + cool;
        if(b < 0f || b > 1f) 
            b = MathExtras.clamp(b,0,1);
        //return -0x1.fefefep125F;//SColor.CW_GRAY

//        final float t = (luma * 4 + warm) * 0x8p-5f;
//        float r = t + mild * 0x1Bp-5f;//t - cool;//luma * 4 - cool * 0x0.8p0f + full * 0x0.3p0f;
//        if(r < 0f || r > 1f) r = MathExtras.clamp(r,0,1);//return -0x1.fefefep125F;//SColor.CW_GRAY
//        float g = t - mild * 0x25p-5f;//0.5f - full;
//        if(g < 0f || g > 1f) g = MathExtras.clamp(g,0,1);//return -0x1.fefefep125F;//SColor.CW_GRAY
//        float b = luma * 4 - r * 0x5p-2f - g * 0x9p-2f;//t + cool;
//        if(b < 0f || b > 1f) b = MathExtras.clamp(b,0,1);//return -0x1.fefefep125F;//SColor.CW_GRAY

        return floatGet(r, g, b,
                opacity);
    }

    private void ycc(float y, float cb, float cr)
    {
        final byte b = (byte) ((cb + 0.625f) * 64), r = (byte) ((0.625f - cr) * 64);
//        SColor.colorFromFloat(tmp, SColor.floatGetYCbCr(y, cb, cr, 1f));
//        display.putString(b, r, StringKit.hex(b) + "x" + StringKit.hex(r), y < 0.65f ? SColor.WHITE : SColor.BLACK,
//                tmp);
        display.put(b, r, '\0', floatGetYCoCg(y, cb, cr, 1f));
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
    public static float luma(final Color color)
    {
        return  (color.r * 0.2126f) +
                (color.g * 0.7152f) +
                (color.b * 0.0722f);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        tcf = DefaultResources.getCrispSlabFont();//.width(cellWidth).height(cellHeight).initBySize();
        viewport = new StretchViewport(totalWidth, totalHeight);
        display = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight, tcf);//.setTextSize(cellWidth + 1f, cellHeight + 1f);
        stage = new Stage(viewport, batch);
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
        SColor col;// = new SColor(0, 0, 0, 0);
        
        final double threshold = 0.005; // threshold controls the "stark-ness" of color changes; must not be negative.
        byte[] paletteMapping;// = new byte[1 << 15];
        int[] reverse = new int[256];
        byte[][] ramps = new byte[256][4];
        float[] lumas = new float[256], cbs = new float[256], crs = new float[256];
        String[] names = new String[256];
        final int yLim = 31, cbLim = 31, crLim = 31, shift1 = 5, shift2 = 10;
        for (int i = 1; i < 256; i++) {
            col = DAWNBRINGER_AURORA[i];
            names[i] = col.name;
            reverse[i] =
                    (int) ((lumas[i] = luma(col)) * yLim)
                            | (int) (((cbs[i] = SColor.chromaB(col)) + 0.5f) * cbLim) << shift1
                            | (int) (((crs[i] = SColor.chromaR(col)) + 0.5f) * crLim) << shift2;
//            if(paletteMapping[reverse[i]] != 0)
//                System.out.println("color #" + i + ", " + col.name + ", overlaps an existing color, " + (int)paletteMapping[reverse[i]] + "!!!");
//            paletteMapping[reverse[i]] = (byte) i;
        }
        //this gives the possibility of storing 32KB of paletteMapping as a 9046-char String.
        try {
            paletteMapping = LZSPlus.decompress("ဣ呢㑓㈥匭②ⵊ⁽Ǖゖ䆉础❠\u0EF2õ䴢㒒឵㩸㏠繇䳌ᓐᏕ♙⳺䬰勍歯䔺¶圍⍛⤓埍၃\u088Eʠ㨽煕㶳啒淿嘠័疟ೌ㍂立Ⳋ䠭榰 \u0887磓䫦殉矔椩縝ŀ坩Ὀ㻠Qఐࠊ䅍嚑搤婂㽙ᮩਐ̯ᰡ⼅䳫㚝\u1DF4ၰ\u2E4Dᑴ/嗕^綝㇟ᳶ⿵㟖㈡Ꮓ唠ɗ⟏倨ᠾ㬯\u2BE6Ꮃ疹箠῞㣐٦͎Ⱐ㏋S㬔绋㎘殸\u2E64䬖簅⏳欅犚玓堧༝映ቀᤖ㵾憠\u0D5E箭ᾎ䠅尝羀㠺\u2B5C\u0094ヵ劦囃ⱓ垦ᇨ灄劙瘸摘瓄憷\"㍩紱徱ᠩᏱ牴ᥬ卞㩠ۗ攓ӂ㖶␠箂䝓䷆稜惙ี冂䣂\u2E58\u0093䦠໙坪㋆⡺♊䫮╶嚋欍⾻嘋伣⑹✻䛳⳿栓䲶㽰㗻盧⚼瘓⼁⛹瑛\u0EFF➺琠␁痃浵㵁濂᪭瘠⩂倥洦⫧㵺็侄惒ᗆᨛ\u2B5Fཚ禴惜伷㈀瓻体ሜ竓\u2DC7☌村ⵖ䦣\u0DE6卨䱙ࢢᆨ岀䂘戨䈥ಜ厅籁燡或ள\u19AC幉\u2BC3\u0096\u0BA5牱楡ᄎ癳ڔ灹玳絭玿⒈噍㿀憁筡㳘Ꮌ䯭ᰱᅔ䐟泟㠈⦟⩬灒ᙣ崊ឧ䠪ᅬ⤦〡ʊᐴ䈊ಧ椲⋯兾ݪ䊚㮫堰孡框ᴥ㦆\u1DED䌮ࣩㅶ㰩㥖ⷠ⠾⏣倮ᇧ⏦ס墚㌸䨎⬺䑘䣭֑⌽增≯税ȶՑ㙁礸繀斅⥪◩ᙆ\u1311ŋᓽ㩀卙㕎変⪴䓍᥎教ᝅᒝ\u05CE㌅ڸ喵\u31EB㌌䃀玩Ꮎᗌ殺㗭\u07BC搣㎴㘋y琔廨ഋᦅ啣◅珃⋉䷝⎄▻ᑳ\u0C5B㦂璛ቶᑲ乷甒䪎䴪䎀䱪疄Ⱬ\u1C81\u2BDB㩴䵻ㆎ洊絼⬺䮍ⶫ㙴⭋㜅ᄧဂᱷ㤏永ڪ糿⣵㶌㬅ᮇሉ絯܇㳗\u18F6嶿ᨀ㲆爇ᮿⲦ寂Ỹᯟ⫲ᰟၨ̌⇳孎䱩糴\u0C3C憧㦠Ίᨼ篮愧締䏸π垨槔ㅯ籀䁖䏰氿䢤䭣䋊♡稨儶抐洵榱唼⊦ⴿ≄張䛸䢲姘繙ኖ»ጜッ⍒㚱ᐁౕ⟴粱⒑ຶ㎹糈㏹犻卜睑Ⓣ㢱娉ᦰ䰅ᖽ㏙㛈摌婀唭㓒㌉㮷卂拔坅㾾㑂灳犭/᧷湥别滒⦫廉場▔⸊暭填¦\u2FD6海㚕伋嫫滺\u2E64ᦊ㌵㖣峵ᖇ⫍⧐㾖ủ〖䶗⨋凸㮮嵡㑯痛⬬╠湐䎙棚玩沽ᎆ瑏䮜济䮕潽稔潻㎉楎睹㻎ސ每䞙㮎⽽洏ⷹ满⾛狚ὡ泡ϱΚ␊ି叁ț澦⒛ᾐ\u0FE9琈ᰛᤤᨈᡡ∛売\u0866⢊䏉ᑫໃ㢂᪢/㌿ᾯᄬ彼㒁焴㬁ㅢⒷ㇔沅ጻڂ䥪㖂᪉䓟沅匮\u08C4偫⦨ד\u12B7羘\u2064`硄䇄㳾ɀ㾸Ġʯ咺\u052B㯥䉳ᇘ㦅爺╍ክ昌䁻䡡婰䫓\u2B5D឴Ⴭᙆ樌㑝\u0878⻳බ厥᷑䙚曖彥癩䜌᧯䏣䱦押⪛㼬嬰犖⫎䳕↽⇹栠֊唫\u05C8䎬㤒⚯ކ坮椶刜䂂⧢ᳳ嗊呾䞸灀攼癎◤ဠ୲熪ռ焢㒧ᘓ岎榗అ㸱ߎ⌋屭恡䅅嵖侐➤☾⎫ӹȾ缿溡᯽឴\u128E䜡摜笯Β䡏傾稅\u1F1F⩝՛ƪ⸉ឤ\u1AEFᬖᔟߜ璣\u08B0⊝ს儗徛⧘िⵥ彛瀲ш繝ᶰ経綹嵨䢸̴ּ䱱仸ဈ᧿攝朹◞ʻ⁾ݜറ౾㠞犃堻᪢孔Љ怘咩ؼ烀,㨯î䟙亇\u18F8᪣硋▥伱䖲ࠜ㘩糃䳒ȳ㈬ចಧ玞㚥庝٬⫔ヿ䔥枻ⅺ楩ۣ⤽噺慼ᒅ⛖⨵∁呺㜈娫廲洤❻Ѽក先ᕋ㕅ാ▴安哟斝ᵧ呦ᚨ䇁⢳\u09D6ᙶ昙䳃\u0CD3ᱹョ㒑䚖㍋墠灮欲㇖ᅋ燡ᶔ㱷䏃䞘癭㢷\u08C0⢴ナ叽Ⱝ扜⅄囀笮ᾩҠ䲺懩াᔘㅒ氂⎈浺◤䩮䓠䦀\u052F㪹᎙奏ƒ⽆䉳栯ի竦䩢劳ấᥳ䅓㶷䖚搴㙏㍩哈ㄉ縈䶘⏘獐䔗冇剐暆䄶曪ゞ↺≏揥埩䗶㠥㾡捌眱熛䠡櫲䇐Ꮢᡞ琖とᘫ朼凥沨ཙѭǝ埐㝎̎∄渠ɀ撸ᘫь仯\u4DBDɿ\u0604ါ\u2E6D擖᪔垙ᙫف㤹㍥㩴䰪杢䣇䏁♲妏ᚪ㨎ᱣ㗥喋◗姌ೄ䮘㓸澁្粫㖇歑濱◟啪ੋ窠犵⏊䞒䐺㖅ີㅱ凐㗴㕡ߓᱶ潱䝪䦨憿浮㛪ⱘ㒧忁戽ら✫梺䛌檵ⱼί嗴㗋筭樸獷ⶓ౺廏嬜䬩ἷၶ墋廒䞤ᮭ㶗㬺皠竒é獴䧡刊緬劣㣇⍳␦噮〤洭檺ἂ㗆惩ᬀᬐ瞠ñ橨㐹䚍⢋玹㍘䤕ឣ嵊月ۭ澷ᦟ㱓憒澠᭗᧵剺孞寫㶂\u2B93㖌㞋惲☠灦ŏ泅杇㩧〇㚌⸂ɾ仡\u137D剮ᵸ䄨ω媇ㅍṶⰹ⑀物洈穣巳否罁ⱗ⢥楕ⴒ倍Ἒ妒摴՞䍶䂘۹\u1F16ᾁ堠̔ᐠዿვ帼如⣝㘱᮳㟶矊䭝ਗ㔫ᝁ晳缈ᾒ痛㵖䧫妼栶爬༁流昿䇢᭛䁙䍂嶧垞㙨繵孝滅›㸜ṑ≞筳㭦秢☨ᆭᤎ㡱䁟恗ஃ䅕媴㋠湈ແ琾扼ṣ夿ᆫ\u2DA7卼݀看䇀❱笽擪亩垖䐃⩇僿䟙恝窏痴朐䴆侳䆹cҕ፞杆㍮䵁璬溈稧ẓ祛框亓䐪琕Ǯ/⽲ឯᩓ㪄紘屫擜䯊㆙❕㹐ڑ嬗昨䮀嗺彅敧㜒棘僝硥㏺䜟婶ⴇ㙕⸩௪䘐➈糕ᴮ甒伏孁\u0DFCẕ⽮桍໔✽\u086E⭂桚㒿澀淍䬒媫ᵅල湛䱾毞⤚䃔㜳瞌纄㇕䃼㻁夻Ԛᰋ㔖稙㙏㢍௭ኇ䦞⟮\u09BB捪孕禿娇Ὧ\u2067巑㷷䛿涸嚱漖澫\u31EA徐瞋瓝\u2700㫁嗾空翱䧙ᖱ機㐓 搎Ɱ䒧\u2D9Fූ籿⬳ῆ䝞気砑憠#ⴜ〠ϴ濭簠ᵣ紣笹τӟ氛㾯甞ऍ纏炚䀜ṳ䝵䛏缴㤑䠢耇㾄㣱倢䛈ẙ琊䃴〿ᩘذᓴ曊湲㣀㡅Ị朮ᢓ幭堻❼ٗ|䴌枂ճ䴡\u2431㎁㦦䖼㰤簩㯓婼䌇稾㬓䱽挅ᜤ欫䑽ᬅ坥㦐⓬⬂惧ᢋ䔨嬃坽㯴ѣ⬉\u0B96ြᩍ㣪㝿᪖巡叀㞛䆛仕៣䑷煖弰ἀQ椉歴ʚྫ❣緺㩦ැ忨วᛁ痹ƥ\u23F6慦\u0DEF㏡䤱憪ℶᎈ)欁⌮Ʋ㈞\u2BB6⍄疤г撾ᛕ枻\u2C2FⳈ䇔៊奻涾墫㈢兣涄䒮垪∷抾᧐㺡㲒᮱囗疥㲑╾⌖濎絑⇪⃥抹凊\u1F7E䷒ݟ㱷殱瘑࿋洯湞᪫犰㦷揱庯⿕⥗漌渑矄糅\u209E䵠䱿⍌\u2066⸒㒚紞欞క忝紿曦栔严糯懑Ⱀ㿄ϖᔮ㶨\u1C87籮ᒡ㖯\u1C84Ɂ洡〭⁚扖ሡ箫\u09BA搖ሞ㢐c扰ƞ砗徼᱘ඊ浰䵛\u1C8F汲㐮䧖炿Ҡƒ徹ʨს緅䊕ȴ曡ਬ丠崥暩溕㎨ደ䁎ᘯᠻ̀䜰嶒䮪屣\u2BE1㨯炔斜柲昩\u2BA0Ȕ ㆄ婢桚ᛣ\u16F7᧣楙\u2E42\u0D84䀣\u0C5C旒┩ጯ፴\u1DF0\"奪③-ㅐ綥㥙⇡㔫㷨敃→䡴拆䤬慖玫᧪㨣⧳围箤㊡愪⏫㰫籊⧭㎐䞧儻懰䙲ᾥ籀«㭭塣卌䎒儗ᗚ㵆 \u0DE0曚ㅜ㶏㔓\u244BṂ头⢰昘⏂庰瓈敊Ńㆲ潼䔤ⵃ厎㶌䒭妃渏䣶䚒ᝓ榳漻楆⻂疩⣜本㳓㔼儹䚶⊝䄿䩑䛈亂\u3130\u0E6B奌㢂匵ㆎ䛬㽓渊乖撸䏒⌴ㅢ᭨岒⠏䦓祜㎂ဍ墽䟮␃\u0BBC㣰֩䴃伵壭斗ᮒ\u200B㣫䔜⾂殸㣻䞜⸂\u07BE㳈֊弳漼磏䘎⤲ℇ墳\u1A9C℣⼵.܇̜ᴎҤމ䈡徳粦+䀡䩬甄礁\u0CFE礰ⶀ嬤䃋扈 ແኹᕩ桏ڱ䂌报㑌㉳፹ứ帮玶碬淎&欙伪+倴儶Մ䕨†ၓ⑰䏩⩍慪ଈ偉ᙬ\u0A80删҇偱♴\u1AC0!啗✷⾥Ւ䶴擙㕏䖢⠲枦ྥ⧺瀠▭䷵ᭅ祈⩠\u008Cٹ炽ࠊ⍫橴浗⛩⓳媾㒫✬ 卶伸\"杆څ亥㩥䪗⪉潪峗䌼唅ᝎ٧䮍ዪࣩѢ塣湶Ìռ㋽ሉ僌⚘\u2BFA榱䬒ධⰃᙹ䓌碒㌝ᱹ壨敦㋭ኾ䖅♵㱣᳑䓏ឱゃᥱ⛯ⶱ⩛晴ᗔឲ⎶\u1AEAⳫ㺷氠ג欑时\u242D⎵櫎唬Ⓜⶺ⨡䞁䢊⍹ᣑ喁催卸\u1CFE曭㪃■嬕⃮㢒ଽ欁噍㒋ஊ夋攼⼝䮰㪥枾⦓䞽恸Ѝᄊ䜵网卽ら➸㳃暎⣙༰㳛埽㠪Ꭱ竓哜ベ潷窽曾⤐䃻礟啞⚭\u202E\u0083搂⸒㠎ʫ晔䵬烴竓碓尫ၰ䄊❢ら硾杌㛒㙽崆䜃ȁ⩽恸⋖⒒ㄪ碿nᘁ㮪㱽⛞ᕑ㠡Ɒ\u0093⥅丢䖬淂䭔ᣈ▼䶂糚秋䗬溄僗ӑ⇂⳨ᢵ䓃䖺\u2BE2䀡ᢅዎՏ煠䮴剅䰀%䇕橾加ʻළ֊潶囅敀斚⠠ⶪ䮭扐Ώᚱ寴倠暗⛩‡\u0E7A䩐䷗淋㉙を乁\u1CC8洠ڐ祥䫅ᐍ歏ؠ㫧Ⰵ筊㢈懂廅瓋䕴删\u0CDD狨晇㝍墭\u0B00曥ⷳ᧺⻐垅㻌愹␠嘻\u08DBᇺ⻬瞷㵂/巪橷勓ḭ潱ᙷ剜⠲䛨祶毴巽梊奧⫏ᖅ⠠\u0A56綧奵㈍噌ウ勵樊྆㭢凵ሐ.玛䚤\u1ABE唭䖛᷿⪭瑭➛㯷庣瘿⟝\u23F5Ⱙ坸供䍹ị眍䭒墍ᕝ矊ਚ䎰瓗盍\u218B俹洇壝倚㭰Ȟ喌䮠ሠr䰭₀tн嵥橈 \u0A34ྨᨧぐ䇘ീ礋\u1779䆸ീᄧὴ㢳噠烡げ䀼ರ瞒徻竂ཝ㆒眷∑偣→彿䈀䀠ᇏ疋䬚昅懋䍢ᮁ弬⽞๐櫚尡҇䍂\u1ADFᙕ䒓⨠݃یᯀϠ椻絩䳑喭䁮\u0BD2䙐㢀ഺ‾ੑ嗃⊉㊛䖘ῃ爿⣰䘠ે㇜ᐠզ䕔⩆儴1▃㨭⣣Ќ㤃ᑉ㤀嫹ᘃ㴹ㅨř岘$樓⊪曰ᚠº◵䶢⸷#懢㷭欠\u2B8Fᬙ弳暈ⓢ⟉嗍⅋氠ĬⱾ⡌下❆祟\u09B5㛘烆䲷ᓥឋ╫䪹䠺之ⴀ䛼㛗㗛⋺劰䧋㘪⃫燺勇㞬 \u2E73Ἦ罬ⷋ浑栠㷟婴垽㭋妓嫆㨽畋ݤ㩓壓\u2D6AⳠČ捙烿ឮ䇪嚽姇ᘌ磚建⻅䲼牑燼ཨ吷⁊痾璲癜柚㺻璴嘷㵚㽖⻂呷ⴱ䷺滬䐠ഩ斯匙枔ᯏ珩䪥敃↚ଥ瘬堖䛡嶭欥澕ପई庥䘔3㔝樍挃㩚姌嫦澆竩ㅫ䑻ᝩ稦Ὣٻ澙៑㮫ᰍ匑ㇹㄝ\u2E79⽯\u177F堼ᛀૣ窹㎫⑽ଟ㋳ཫࠊ畴埅㵫 䫜埗壋敹䫺囅\u20CB䥾拪嘫ᄣῌ䀠kたȂ\u0DBE⤋⡖䈔\u0CF8拶塝㖽杀甦ٞ㗫⾀熧杷䇗嗈毷囚ᇗ⸣ⵦ仕㘊࿐漋ᱝ\u0090浺柶᱖笎Ǧ桖⽼懠㑨\u0CF7剔ද䙆稻䁹三ඐ桗婐冪\u0378瘻䞤ⷛ\u0E66洝婞ˏ䘈ᅧ噚㗉ဘ灆\u180Fⶠňⲃ揁ẍ侭\u2B9D孮ᨩ皬箅䦝媍䮎粍孼ᢑ恭ʁ፴漽攌琠㬼梕ᑭ㢂䘓尩捭ઓ揢歉䙬㞓䷼沠˕⊀z㢿䔂瘯㛩╋ᔑ睒㧳䯹弉嫽庍ں㓫⛕宍柹ἑ峿㙅⦺式⠆㛍⾎\u0CFC劶㩓ō孠\u0EA0笐㭌堾嬕卓妻洄࿃名➽\u1CFD娠煳疼泦\u0E86㯓ㄏ瓼⫽ᣓ 㰁礆'瀮焾⺔Ẅ㠪廬;椽㩘㈓࿒冓嚢峤ဠȃ㧐瘦➯彐溠ˌ争䜠ࠐ垂㬝ẫ⁾\u177C侄粓╟̄\u16F4织❊ᝥᙡ㦽浞ᴘㆴ狽⡸䉑\u1759㑋䨍挌俔礹㏶㝺ㆌ瞇⇍ዣ亼筇潫Ạ揁㿋⻉秥\u173C甇\u1A7E⢤埜縷⩿秥㍯刷僟ת囬穋ⓜ欇ᝲ礷嵹㶬⺦厷ⵛ旫⡕\u31EB㸊ᘉ㮼瞷糚日⻅㱷剐:㟦⧒疷圽㻶桚6彆灗㗑毭\u2BFA柏\u2D9F殤溰涬໐ⷯ⽚悏殶∉嶺籗塙\u0DE1怀濏㎓∔涣⎎そ瘍昆捗㇜寴浿䀡ᐷੱ䪮嬄矕嵍毵䠽㮮嫭皎⎀R活ᷠą䤌渉浽☎珐燇ఠᩭ眿㫦垍㼏孾ᰛ愍䎚⏠礽硝䞋眳嬑䀠ᒾ絗䠠:儿๒ᷦ価\u2BD7┞䞬㶃罧ⱺ㗼㽾林ԟ㷆Ⓨ揦ᔞ帊㹤挖兝㷋溻↗篔枰䳓怠灏嵝睔檋佷娇彬څⴠʤᓠǁଐ8\u0ADEՀÐ≞⇠ý挗嚠ʡ⊀\u0080\u2BF8Xپ⫿៙㶛ㅾ犲埆ᶋ瀯߮⠽㻁累投䏪ừ熆廻␚ᵑ熯જ矿ᓁ滪.眯碼3洿汛㭠\u009Cਘ妠̛縄玬㨯䢞Ꮓ㎱禯岜+旧\u2E5A⠒㻳罇ᵚࠐ㻪㜓᮲崈佤笿椘៱㹫璿⾉⟪㾼祿䕞埤⫎⎇瑽䟳䭻痧䨊⟶些糇ᬘ⭄ᛁ㱫É称嘠ຕ名ᵷ端ⷼ▲⺥汎俺忩粏狨ჟ名翹湎媐k刟ፆ哤堠㜙繉ゟ㍝䨕纣罝㋜䀗丼猟㍟㿵亜妵湷\u0A7D倓秲\u187E綏啣Î怛䀹㏒乸ムḷ”喐䁃㺞ⶲ牵䦶㠦ⴅ嫐ŀٵ欟⺙䪑\u0DF6ഠƨ涓䃠\u0092䴢 埰᭠凤\"喕混ࠡ㶗\"䄞巈ી↡䘤ῤ恹Ìڰᔠ┣呓灖歛ⰽ\u3100⚎㷛眆㼣ƲӃ䍀勽㎔䛱₩岳㝕务㶨㐲ථ滺֭憞̢ថ㷨督̄\u0DCD嚡㏘氷㼠µ㬥壡ⶭ溻㡿䄯ජฬ㡜実嵤ෛ䜦ᒛ䃃ぷ渮Ҙ椗㙝ᗖ桉欠Ä儫䃴༠䈠ֲ緹ႠȄⴓ䏣\u037Fɾ熨⒖゛䈀䡢ⅰ秄ዯ╝ϒ䀠iዼ䉒䑎挠ëℓ摀僂䋲ඇ䪾䠻䨾⢎Ộ祫捰絢ℑϠfã\u052Aᑏ孶䏢ォ尠\u0EA6樅ឹ帠Ð!ʑ䀠䢅䊦\u0869兾\u2EF9嘁熦帴秄抛⬴&ʓ†ࠩ愦礻檏\u19DC朐ⱁヵ䌅炘㥾㖘㘁垰幂砼Ṱ㇢术ߠ澃㳎ୗ䷺֤嗌塣愠ϱḲ垫憪㯜ጏໜ㬑ᱏάᶭ瀠ࡌ澏㶡ତ㐱婘ḅ\u2FD6㻎笇政⎝䢫ݥ彤簳瀠C徔紣瘏䘖⅚琀P⇞夘射⿉㸽禗期⚝㒫⓶征᱃犄⧞\u171Bఊ⿇㺼䐠᰿⽂潖㝪獁籹๏䲞ԛ᠌忇㻮㩿涟ⵂ㐑῾㽁簠Э橏㽩疵ો丞൦椵಼縠ȿ㠈羲绯砿洇ᝃ㸖ߢ棨ⷍཥ弞学\u0D3C㒋繮䞺᭟ᯂဖ翰棃⋏狏䗱㹻榉瑯绨њ᠈犽Ɨ\u0AFB㶈掗疬⓱畤㠈ⱔǩ䞟樠缜〓⩓ᣝⳃ\u0EEB廱ユ‸䁩㈕㰆Ṹ䈢䙇㋷ᣌʋཬ⎞怠*䪋城㈥䠿殭ǜ҈ᓠ䬣䈨癋墾掆琌≡㰒ܴ\u1C90湃䞰ᙘ斢㰯癟䃉挤ଋ䟎凧㠱\u1C9DǬبឋ܃搭⡑㤍扷ௐ†櫫䑘༠ऑࣹᜲっ簮پ\u197B㘃ᐩṃ悧ɏ\u0FED娑◙簵\u2062䇶ߞẘ嗼ቕ䍧磕˼ࠢⴎ伥㰾\u088A䙗䓍枰导㘪䅓惪㲜ನ☦ᢥ悏梘ẏ㠴\u1CB0嶢⤩\u244DӒ䉐䪡䙁媤炿䡲⇉ܱᩰ䜍ᗐ瑉㜠Ǥㄪ䑒䓊ᐐ䬛䢩㩅娎梇⇘筑\u1AE4笲ᱪో䒠d䑯ృ㬰 µ䥚ー㵭玸⺁媦⢶塠愠R㘲埄䤠đ璱䟞戁ᨼᢐ沲✨7䰐忣猭展ァ䌅眤㕾䡤㘇剰廦⒓⳥⇢!Р\u0A3B牥憟ܻ涐嶲€㥜\u08B5㰠Ʃ㱀\u0088箲⟐%ᬏᾳ㹬ቅ\u08B0匎\u0E78㲱烆嘉\u0A84⧝磢Ⴔ䀠⽥┋栄碜䲭৹ऊ剻瀠ቂ䤒⏩९吱卧㤹ᒐÞ⪵ᣨ惃䢭㉍彆卵ུ⳱揹礰C璉٦⚻࿏⧍祇泈䪜 巊᱔哃㹶㝼㓩叁㖯影姅ᔲ呲槍❚ᕈ䨝ᚯ㩏⢣⍩㌿奱敤ັ䱱槎䚽ᎈ练庩嵉㓛厛癚ⷱ㏆㴿㾽则䝏惔淳憪橍梠¯⃮䋧瓿剾䵿圉䰛ᴲړ㬀暣\u19DDᑓ⦯絀അ刱䱬㤹᳥㴻ಆᧆ䞃ᙋ᪁惫捖ល⎁䩆㚱䫦冲䲏绦䔣ᅸ綃擫晈梿⌫\u0E86⅘ۦ猰♫ㆰܠ ⅇ\u31BD䞖\u1BF8䨃㐮乆㤎嶥䤶ⱹ燤▵ᚐ䇒暫Ὄ䒌綮\u2E52ⴉ控श⧎滤ᜲ室始䕐ᚘ娃㎩\u0B53䃕㌭䯀↡\"猧\u0D56ㇹ琸䜲毞妊מኤ揓⛬倥泫㈷ྲྀ⛹F㰵皒䛞搠\u0C71₍滻䝁᰼揓穐孓䄘Ꮠ㇀⸎䡇₺㚚皓䞠ؼ劒ࡔⅆᴀ牒\u0B8C⏑墈玾\u0DCC⇸♁ᗤ䎣䑪\u1758峲狠佂㶩⩄*Ⴒ⩩ಧᣏ⺖㦵ֺ摄䔲ᱫ㷥峭ኈ䲲㞩⪛ிቸ㦵\u0529\u1AE4䀠把⠔ၐ焓ᩪ緼┝ጬ佽䅉䨆撱㉥䤠Ȓ枲剢祀碉ᧄ暲\"\u0BCFဠァ䥸穅ဠ㉣礠ŇΆ牵䨛朻怠㼪䧁┬ဠ\u1C88ᄠ˥yક秗查ᰠɲ४㽛擗狌䠠ె擥䍒䠠Ṝᒰૠ⦪\u2E49捇⁴⩣禐\u1719შ䈪ࡎ啀\u0090姝㕪僑哴ୡม㐥!⿐⦪㜥ዅ䪷犄槄ᝂᙢ䆜呍商䌆删ʯ⩁䋆\u0A78⣩䓉ዄ㚹憙ל៵ᣴ即é\u08C1䊯勲〠ᨃ⋕પ亱㢥櫆甴\u2FDC䗏\u244D᳂緳ᩏⵚ⊠Ư列橗⢱⏎䰦⸉儷溶䐺䖎䞭ᴫ֫ṉ壔䬠ï泭㵔梵ਫ਼䠬㧚叇ⴹ煥䗯✘怂䁒潬忨ዥ᳅ଚ㋱\u0886㱿㾠G\u0BC5籿煣䕂\u1758儫⧳惬䍓椔䯲⨓孅笵ᑿঞ◁ᜃᐔ楫效捐ഊ㉃\u0B34᤹ᒇ橰楥▵ᘤ庈悑测千ዎ䭉争⽅愚嬠I抅㊆ټᦟ槙\u1774幒疀半䳏\u0CD9䪒ⴹ⡅㊺嬬䰪斋ᚴ嶲䛫ํ剈⳽㋽䪡䀠䌄猵件㶑ᖅ疿ᙵ攨ᢋ០勒睋᳑猋䬣䫶⇹瞵斵㚓攲査ᄌ姒㋨糀泏䬘ล⓹➆乿禜姶李ᚗⷓ㛯ṋ洔ฎ⿶∵㯧格䖌㤹柖Ḫ癋凬ń᳢⩥䢮‵ᱴஶ䕹焣壛ᓷ╋壈$䳪渲㓏Ṉ岪⩃低Ω噷䈆湢℠ư䕸≩\u0A0D䒮ᘼ佋優䑎㴆⫡熎㢙Ҥ%晵†湕\u2B8B䳮㣙䙵╷溟㦓撿ဠ㭂嘇曊凊䩣ᯮ⣺̂狻વⱙ䐅ҰK৵捶䵹劒嘂⒊垜䄌㛌佌\u1CA3琗䬞⠙❴ ᔭ†㘠Ĥ凎ག糋珤伡⃩䔸㊍ţ禍䐠ଜ繾稛攦刺碊 ⓦ唺耓燊À|㠫塍惆ʬ櫓⾞⒥惵䡷ᢈ㗕\u2436偺樫⡏罟糬ો⼱⨥拄᾿ⶑׄᖰ噚弫᷊盟糡ઠⴱ㦥嘴䪰3熕∴䡽慹Ԡe硾⒇㗳哮婂墫䙨棊⊩剧⽽\u2B95樴睻䥡瘓嚈凂磳⟍\u10C6挍檬\u2E71㜕㯴ὰ撇䗨䓸吂怋ᕊ⣉挀䭔ⵚ㲉\u08B6≿呿畞\u177Dᬲ熪ߎӅ拰䭮䠠Ȝ۬䮕ણ☭毵ô䥲\u0D49ᑤ婲穪傉磀\u008B䀺♌Ⓚᢴ䯡䡃℅扗䣿॰╩ᘴ岬䖪䍋凟竃\u0B00檃㚞橗ᆲ⦁ด㞱厦娈协㓓勂᭐渳⦭斴噵ᥤ䵢㛞壒盪֣䧞⚡ᩘ橙ⲅᕖ哷፤䶲㘘倦尜孋䋕ⳒȠɈ峊曠⬣亥㩹\u0C77䅹㎕䵫䕰ဠ㺝Ɩ㝋ᤆ喺佈歝曺̌溶㓭\u07B7㬴獭䶎㒙崪槓䎔拟Ⳑ婼瀓┵恷兼୶ᘑ㕎ᢶ䩊ネ䋖ᛰ孌欙㍍呶楷▊ⶑ㟥埸殻溎⋈囒Ⰾ⪅㳭磗ᥳ₅ⵖԵ孪䄌疋◒狞Ⱉ⦎⛡兴㼷▒䚕㟵巶歋Ⳍ⫀.!᱂唪儀䤳寮狟ⴞ犽⼻⓵ᜄ曼啨嗾嗣淜䀠䒽䟺噠傲忩㫎㲨殹⣻\u2B8D畷㵺⢖禍䘽徊溋䱫᷌᪨橃⩕㙕䵇⛾㕺嗗昉ᰊ䇻箊䫗\u1ADC橽⼭㩕\u18F6⾱㺒嘅堝岺檊ފ曀\u1AFC殣\u2B5Bフ拶䭱㺙㗘睿ᵺ剚䏊曛ᬝ爰⪍ⱕ㛴㭵ᵬ疵堃嵺䄊䕫囍粲੨⬽⌕盶睽ᶌ疂៳屴关䯉⻄㫖㩏\u2B8D㞕䄷䗳㶕㖗含兔嬋ᯉே㫴ଡ䢷⺕⦖㑸嶇疋圊ᇚ䣚撍䇐⋩㩸\u2BDDⵥ߶僵熄හ㘋儂䋲ߌỎ竫⏦乣⳽ࡔヷ絵崮\u16FB囎䚪慊㯀䛷㫽澗㮭䏴屶典◀望冂悺傉ᇐỦ᪘桉⾭㎕⏰ཷฐ㙱偲筫㒈燔䛴穤此֭噗声㍮㷏嘇妈媺ፈ様廝㮀沃␉奕瓴⾑\u0D55瞁彾溻ฌᇚ弉\u1AFB淹\u2D6D⬔ೲ潵╆㜅唆喺 ᑵ奶䁺潉䗛暿ᩇ溋⪝䃕繶㎈紷ᑏ圆匚勪ׄ㻱穗泋㷭ⳗ✺գ丐呕彞䌚ࠈ\u05C8纴宒棓⟭㠺⿹\u2B60\u0099ⓔ䅽உⷁ㑟忞殻〉⋐㚢⬏潟㲝䨗ῷ罠ͅ㘫ᯞ紛Ⳍ濌ǻ㐆湋㢍栭⳾ᮓᗎ㙭嘡峻Έ痗㻇\u1AF0ᩕ㊍᧕替᭰淨་ὶ禺⻏ⷐ䇍媖歵〽尯\u16FD沋济啚妖凋㐽緖皦ܸ᪡㾍䏖異疘ḏ㚝嗡忻㰽竀↡ݡ歧⸍䣶㻵൱Б嗣奺犋囈╀⇚櫣䠠ဏ∔㯾澍♕!㾬᧧⼽秴ݿ孽㕹嘈㏁炊$╘㨺䝚巎ᢐ⋼㭋⾽㨥担歸嶓祚໓噚厧ᔍ滝憯ݵ橗㤕Ɣ&禨桃ᶕ⡺烾甦癤㻂竛䯋䒄䊠Ñ猈廄䛴ਠǤᒁ㒵㪙潨捥\"さ洺㹃з㉝䣮嵻哧剦照䬌ᯂ凕\u0B4A\u1C97⳽گ㗰ྋ⍅รᨎ呦唽\u0D49漇㯒ᣗ◃㚯ٙជ⍈㑧夾翛\u170D䲆漙䙠汸楝洔揶多握瞛嵦䒛䨌寕刋㰆ᡶÝ焔Ɒ=珝椖癞㣹㶊㛁垑䧧㬺ಟㆤᯫ歳✃㞬癖⎇掉痄惾䫪缿岁燃\u0BA1ᴤ檅灯幛\u0F6E\u0D50䱬堑翦Ј⊉䛁㎅䠠ᵎᚿ\u1BF7欿†刢☯涓㦝ᣔ喻䓥絴ܲゞ奆㋨劋绊❗泋⺳\u0C74ᥑ㍾絯瓒㞆瑺治ΐ䧥箟殟㭍㎆ㅟ擥紳琿堞牺㦏㊘Ȝ❅ᦟ㩳ᅯ䡖㍧፴䵪㵉刦抿જ㻑篰ᩴ攵刭掳唊綛瞭尞嘧堸杙Ǚ❧桀梣委䡖℆ፊ晲僶禺$暚㍡揆人ુ䇘㳀ᮔ瑍 ॣᴔ綣อ㡛筠㎲\u0EFAㅪ嶧↺罒ᦥݘᰔ枣甬ኵà䍭㠠ဗ䳽䏡呣刖窇☾ᢕ↠㮻㚡べ椬\u0B54\u2CF7祮ౘㄺ冦减ᚓ懚㩸⨐正端瑘泳䍍䴶媢箆ᘾ四妬歒ᩌ沥⛮䉜ᴘ␅嗄㵎晦ㄼҗᆭ橢ᤇッᒬ㩗㶕⎫嗤㼱䍦㬈㢖咠ş̼ᒜ\u2EF4䟪ᳱ㋃㯯睗\u08E3⎭ไ㈑䥦攻寊凛柦ᦨ珥⟬㝰ᤑ⎏䲬ㅱ帇ᔸ䲗禦䘾ᣜ溃喔䃗㴘ௗ\u0E61〥泧䁽庒刐穃殘秝桌惕瞁☀ⷢ㶥怶Ἅ䆅抿䛆⪗┳攔晔ԉு瓜㕥䜺㑹ᆘἜ穋沂竝䆬壝ⵡ\u3104ⶢ㜩朷䑺粑ዄ♘廢殫慌䅞澟ଢⵡ㊥为抺抇ਕ✕ᩄ梳婯㟷㦐奪⸅垩夛ҿ⦅䧭៩Ỳ犳啎\u0CDA┇䵆ⵕ泅縚᠋⦖䦾❡ⶔ曫㋔╘㌒吏痒㪅溶窼ና⦻箅Ỳ祫祯瓟猁卹㛿唡\u4DB6ⅾ\u008Cᗑ稢怟㛍棎峛猜\u2B6A俠㉊牷墻㦏⧶\u2B4D\u1AD4歋⇔峓ଏ䮾倊㫪䀠碸ཟ剡僊㚾暘᧲噙ᬔ柳⫏⫝̸⃩Ί嘝棵瑶㩻ၜᦨ柝᳛㋋\u0ECF⛿甜㏝⼫\u1BF9惷䍸緔㘊埦尛⭓惮ְⴁ㎚ྦ㝕䒊\u0B7Fᚕ⇏ڂ䱱埋緗囔䴈㍭丮㩕䤇㤎碘ᦳ垛\u1FF0槓ܯ孹⬙㏦\u0E98㹺瞧妾墀姘䜌\u1A9A懣叭ǘ⤙㌾\u0C4E㼚坧唸ẅ㦦䚻ᨦ砻ႏ䗰峭玿乴㴭吇\u0D3C\u0381ข䛪ᢦ琫璍罐'怠\u1BF7杏Ờ搓䢍潐㳦\u1BF9༳㩭䘆䁸沕ඩ䘠ߘ6熭絖㑺ಕ\u05CA㛠媗㖻㾭ა會ᰛಞ㨥䓧䢊ஜ\u09D9ឨ将漓\u0A4E⣕⋱ୢ⽓㥱岻䫼ᆙ\u05FE㞀弗↫䙍旛囡掜⻢㢍仗㡹ネ\u0884慭幤系厭嗛̏ఌ⿻㫑粷ߍঅৌឰ妢搉坯竚ૡ䬮⺲㲅絇˨ޓ䧕ᜬ嶄翻壍瓔\u0EED፧盼帅劶㢿䪟ৈ皥᪒汛唎糑代䰐\u0D65刞炇㧹⦁⦯\u171F澟㑳ጌ⏞狭Ꭵ濻ᔵ吧㧼ހ斢土濊擳嗌寗\u0AF8\u2BE4䶠㦵枖᧪䶇巇ᨠ۫֙䫪嘧巊溍㤄䟛⻥斃汯㕝棷պᖚ娎砚嶺珳㈌#䯾憋仍書ೱ死浉㡕僶\u07FCᾔ㖹婆嵞憛Ɱ㛕嬇号洿㪁盻焍当㸀杮姰枛㠍⻑继㏸ᖿ㰝䏶孺㢏㷳枖廚溽㒹⛗缟簇ⵘ㼭倯☼碖笐༓ᰚ潣#㼁徦搻每彺˺ᬧ䴈㬭䎧Ӽᎈ䐂โᥦ沧ʎ懓∖ᰄ洎㿭猯夹儖䏳朠征絻☼扑曷᯼瀐禙䋗ዽ㎚⏮㘺ῆ箻䚌⧖坹ޢ沓㭃粮\u0CFF焜⸅ཅ庶晻ᮭ槟ᇹ\u1BFAᵤ㯃瓖廹岃␃ဖṱ獻紽繕ㇸ孖Ẹ盆綮眺ᤕ秫㟲ᷖ拧璶\u1C98盩孎浱䀃篖㱹守\u13F9㙜㠩汇引擻燱പὤ狢沖 擄砽咗橹䞆巌⟲㲮獇ᄌ柲仲䮙\u1DF7㛅斖㐍䞗ᐔ研峩縣渗᳓䧺➋直甽憗䠎យ嶲佪㰒惛િ㋲䇏\u2BC9潴秩傖㏼䖉況佳娉糛≯䚐䃨㦯潔甆籶獘ཱྀǁ眉⩹柇明檝Ỡ篮伯㺝狮ᝍ澟㶻砖㶾掛〽㪛䧪❏漏ゝ䋷叻喁縓瞒㠡樇\u05CF䛕欃ί氠ఙ㼗厠ᴠ稝湚罝ⶀ娟倞㹡欛⾿傒ȁ殨Ḇ㴫汏䃞䄖άཧᡡ爷ᑼ憐/瑓㰼\u089A懰ᯏⲰ畎瘮焿挆䰂⿈㸁栻㟎゙☈ଠϞ妐㴍មᴐ督䤇\u0C5B㌓䯇暙㻦緣䤽潑旼⏒ᾐ穫碮䩝\u191Fก\u0E64㢈撻ㄿ碗ᘚ⎄Ὠ獃窮䙝䬌\u2BEA䛕㭱玻Ⴝ婟嗾䝦常篋糗ɘ\u0B8BⰖ㙱ᣱ璻ⴿ\u1C91嗼䷲ᱸ礋厯櫽㤃毋\u0FBD㼕湭#㆝㪩瑇⢬粕盿䟁弻㘳䒏䅞䜋搇㞞\u1CAD擷⼾ㅝ᧺✠Ƈ㎟伆㯹ᶔ皳䩯姜朗ᰒ伢榼㕇⳼玚槻⟏皧㕓巯䗝䴜叚滅䴥⇗䋿\u2B93ⷺ杩湬瓳喎◞眀䨕乚㭖⏗ǿᮜḛ✣ᾮ瑻垎损ⴅ\u0AE5倛㫍禛\u0A0C宐㧱\u2B7BἼ竛嬎篸ኒ珋产姙籛溿溙䷾眷Ẏ痛䨎᧙㾙㸘〖㸙栛宽劙秥枵ⴜ犝䑎㗾䌞玷癡㽅箍ⅷ䆔䘗㯰怜硫䀿\u0FDE嶓痐ᾟ墣暷≿ঘЌྃ乢碧坏峘㌜䯌⺎婗㡓癿妓◶࿄幒煫䄿拚ሆᆲᾩ㸵澧房廞ۮ䞵ཱ筧瘾\u0CDCƝ戄㙅嫃掷㔿ᖖ\u23FA執䳑竧巏㉙⬘䶦ἠ彵画\u037F䔜㗦䞚峺罇盏ટ䤙⟝㠙展綻嵟姙痶䞂徺璋纾绚┕毉䯄㰭灗筼\u1C96㏸㜶㵬嶇炎ໟ娆᮷\u0E76⺓毻䝽ᶐ珵⎊㝹縇䆾纚璀ិἌ翓煗炽Ξఛ䞱㳚磵噆'情灛暎䏚伛㺹挲祫楏㓜㌖ö〖夅瞷婿榜攊㯚⚦㵋溗拼Ꮣ淣➈ᥬ移熏榟嘄㮮伦㥽痏⇾ផ猌睝㿕絛徏\u2FDB☂㶤砆㹽璏嫜⾜廦瞥\u2FEF㋛浾篛ⴚ篥庋屍溗㗿亖弚簋㽭稹暎杙̝㷕⪦滹缇潯云縋毯廾疩怏濞㴔懙尐祝戯䡞জ☋筐㸁眧篿ᢟ挘䯜\u0F70簛琧㔶囑簛\u0FF8㷀箧䟷ᓚ䂈䟶\u2E68稧溯ٽ碖䏡ྡྷ曑瑧䩞椟␘凜Ἅ滧朿岝㦟␟ݬ㶉矧焿磟㎟࿚\u0E91夳繷劝唝ᘝᮌἃ㋯筎જ嬞䷹⾹磵籷ᵝ甘瘆俜㻉竭\u4DBF⌛చ氊㼏\u1AD3端㵞䴔吙便䵙紇狞㺟笚俯ᾼ秓扏\u0D5D嘚௦彾㸻翯燞\u1AFB氐᠓㾜羓燯ষ瘚琜〓శ֏痟会嬞澲彪綋糏㪿ῴ\u2BF3\u2FED漅癷絿ᦟ圄ῴ庆耗盗⫝␛矯⼵㰯㣷坾倽ࠆ‐繚礥ⵟ䆄㐙俠䀝ὥ寵纞缚พ厹湫庯暏㗾殞樞漹峷睗糾珺ᴚ㠅帿㩄焗土㞞瀑㞷弍璛䎞柘砄矃滮禝甗羾\u0B98㰃柈㲰㹺倏䷻缍篜༇縛摵ବȝؔῷ汼禯真ᾝ␄ྣ皀㡥禷䔸∘䠋\u175B罣癧稟碟⣐䟒㽨罧渿晽”䟲ᾘ緃缭䌾愜刊⟶俊Ἶຼ㴩笃叶扯䤿㉞犝䋚⨔ఉ糩ྺ侌ἒ᷄砨瘃摧䅯\u0ADEஜ㊘ဪ厊旱⾶役㹬粍㫒矷摠㕿ड禜ۛ瘖䰍䁇\u009D徕㹲編܉盷榏嫿ዞ嘣䐦ᠩᰈ矶䂍弽㸲汘ӻ玀ሷ巿ⴡ\u175C㺘烐܊㟤ₐ⾏㸩紒禷癯栿剟㢖峜䨥ᐐࠄ\u0FEDῒ⽰纞絓㣈൰\u1CBD䟿⚡囜䘤祖ࠎ偟ⁿ弫ƍ峧㬦ヰᘿ僟\u2B8Eᡵ㨙ᰩ䠆ụ恬匧ȉ籓稛皏櫗勛ࣾ㴝縧簔еᷡ㞻㽰䅕㱛簏\u318F掛䓟㖞㢣烙圖䐷偓㟘⃑繽ⷲү砏柟娇ሎ䫵ᐘ℔ᜊ䟺瞧眷庻縑樿痿樯帿㰾䆄ᾙဖЂ㿲偾ྐ㻯粏礟珃欯䦷␞Ⱍ˫䀓怏㿿偼\u0F6F繠ʚѳ璃戟䈿\u0D5Eᠣ䠛☮ᄁE䮰¥䈘綘㩳癐桃娠嵞嵜⠤㨗䀳塈䟇ýẲ㰸֓爷抐㔀紡抍䊛อ倳៲䂚½ฤ̾\u0590\u0E98ᩱ偧Ӫ昢Ⱖ䐩瀵㟷炆彻䆢Ό㫨\u0D00‐\u2BE7㾾灢ܚ℩㬍я䁳彫༒ˁܐ\u08B0ᮏ婠氶匢䦘\u0C29⠳偛\u088Eტ纊̔܅瑄\u177F墰䚡ᱣ㘤尫㜅い䡪⽒∓㸌܈ೄႀ\u2BE0嶞圣䘚ᰫп濻悇⽄䇱簢ߒਨᅓ埠䱡䔝愤㤩砃㧣恰儝湡υЪো渰➏℞䕣岧Ⱂ搸緰喹潾缂䉺硾灈ვ䣀䵡╣ᷛȒᨴǵ矉儀曭̊ܝ犃瀗傀呞梜洧ἐ琸㫰ぱགྷ继粁禿瞟湐\u2BF0䰞䖣⌤⦔耄ᡘᢐᾉ䆁䌆\u05CCसᘠ㦀猾⠝猧夕8柣τę䆡崉簃瑘ᗧ宀糞殣ᰧ㦖瀺ᗰ塹¶ŀ䎐ќೌ‐㌀戡ૢ缧㠪簸ⱈ\u0864ᄆ成䉭秖ື木♠䒡ྡྷ䜙䐮࠳汃塿ョ䇨䍞ږ༰ῠ⊐埞⼼ҧ⤭圏䑑⁵ᄓŤ䊇桎\u0B7CṨ⎐䬁䙢䄧\u192C渼ⷨ悑䃗Ƨ䏓㬪ऴῨ▐枡㹢㨛ㄬ砱㱞㏅㽟Ŗ߭ͅ烹濗劰䐁慢㏻唩㸸ᑋ⡭答䆚䋟ӷ琣桵峕㑡偃fਨ〃㑟碂㼴Ề緍ٔೄᾈ㷀揁᪢度ȕ稺߱梇ℋ绠Νڲ瓥歐\u2BC0䋡扂㢘̨␁ై䑴チṓȦ\u0560ุሸ⅐䟁擢熦怓ㄵ⟵ひᾋṙ䉘碓皳檁夀掁榜▧ḩा\u2C5F塵モ㺘ˈ磩ௌὄ✿⊱渣疦⇓㰾硒\u089C䣫Ị⍇Ц県ၘ↠橁槢䶧璭氃ш摽愖ƶ⎄䑈໐ᩨ㆐䜎\u0A62ᨥ眬䐲牑摶焇ᅛ䈼䑒\u0A92Ῐ⬐澱嗢䞤椬Կ\u244F撏ႮⅥ汹ےඋ憘㉀杁弣慧ʭ丅\u0A46⦬⣷ᆑ⌿ߌ瑨ᇈ㏨稁䂣㾦犨Ⅎ䩁箷枀ᅒ䋟ץ\u0CD4ᄤ⨨況粣大⨭ℿ⩉Ѩᝊ䅀㸀䞪੫欈⼨䠾⼭᠙犮电䏭梉\u08D7幡䌈䟱\u0B45感䚨䬱䩂紧墯റ≕偣ࣤ憡䎸䓟矂ᷤⲨ䪁\u2E43ᬦ\u0A11焸O䒟࣭兲㶤䟝㓬ᩯ刀䇡㳣䑘欫ᤸ᱑ල椀∟˫ҀଲỸ㨀窱Ղ婧ᬩ椹剞璑䣉懚⊞䞹ਰᐐ⽨䏱ፃ䎥ⲭȱ珱\u0893棱懐Ⲡ ")
                    .getBytes("ISO-8859-1"); // for copy/paste reasons ISO-8859-1 seems to work, but others don't at all
        } catch (UnsupportedEncodingException e) {
            return;
        }
        System.out.println("String[] names = new String[]{ \"" + StringKit.join("\",\"", names) + "\"\n};");
        System.out.println("int[] reverses = new int[]{ "+ StringKit.join(",", reverse) + "\n};");
        System.out.println("float[] lumas = new float[]{ "+ StringKit.join("f,", lumas) + "f\n};");
        System.out.println("float[] cbs = new float[]{ "+ StringKit.join("f,", cbs) + "f\n};");
        System.out.println("float[] crs = new float[]{ "+ StringKit.join("f,", crs) + "f\n};");
        float crf, cbf, yf;
//        for (int cr = 0; cr <= crLim; cr++) {
//            crf = (float) cr / crLim - 0.5f;
//            for (int cb = 0; cb <= cbLim; cb++) {
//                cbf = (float) cb / cbLim - 0.5f;
//                for (int y = 0; y <= yLim; y++) {
//                    final int c2 = cr << shift2 | cb << shift1 | y;
//                    if (paletteMapping[c2] == 0) {
//                        yf = (float) y / yLim;
//                        double dist = Double.POSITIVE_INFINITY;
//                        for (int i = 1; i < 256; i++) {
//                            if (Math.abs(lumas[i] - yf) < 0.1f && dist > (dist = Math.min(dist, difference(lumas[i], cbs[i], crs[i], yf, cbf, crf))))
//                                paletteMapping[c2] = (byte) i;
//                        }
//                    }
//                }
//            }
//        }
//        System.out.println();
//        try {
//            String compressed = LZSPlus.compress(new String(paletteMapping, "ISO-8859-1"));
//            System.out.println('"' + compressed + '"');
//            paletteMapping = LZSPlus.decompress(compressed).getBytes("ISO-8859-1");
//        } catch (UnsupportedEncodingException ignored) {
//        }
//        System.out.println();

        float adj;
        int idx2;
        for (int i = 1; i < 256; i++) {
            int rev = reverse[i], y = rev & yLim, match = i;
            yf = lumas[i];
            cbf = cbs[i];
            crf = crs[i];
            ramps[i][1] = (byte)i;//Color.rgba8888(DAWNBRINGER_AURORA[i]);
            ramps[i][0] = 15;//0xFFFFFFFF; // white
            ramps[i][2] = 1;//0x010101FF; // black
            ramps[i][3] = 1;//0x010101FF; // black
            for (int yy = y + 2, rr = rev + 2; yy <= yLim; yy++, rr++) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cbs[idx2], crs[idx2], yf, cbf, crf) > threshold) {
                    ramps[i][0] = paletteMapping[rr];
                    break;
                }                 
                adj = 1f + ((yLim + 1 >>> 1) - yy) * 0x1p-10f;
                cbf = MathUtils.clamp(cbf * adj, -0.5f, 0.5f);
                crf = MathUtils.clamp(crf * adj, -0.5f, 0.5f);
//                cbf = (cbf + 0.5f) * 0.984375f - 0.5f;
//                crf = (crf - 0.5f) * 0.96875f + 0.5f;
                rr = yy
                        | (int) ((cbf + 0.5f) * cbLim) << shift1
                        | (int) ((crf + 0.5f) * crLim) << shift2;
            }
            cbf = cbs[i];
            crf = crs[i];
            for (int yy = y - 2, rr = rev - 2; yy > 0; rr--) {
                if ((idx2 = paletteMapping[rr] & 255) != i && difference(lumas[idx2], cbs[idx2], crs[idx2], yf, cbf, crf) > threshold) {
                    ramps[i][2] = paletteMapping[rr];
                    rev = rr;
                    y = yy;
                    match = paletteMapping[rr] & 255;
                    break;
                }
                adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                cbf = MathUtils.clamp(cbf * adj, -0.5f, 0.5f);
                crf = MathUtils.clamp(crf * adj, -0.5f, 0.5f);
//                cbf = (cbf - 0.5f) * 0.984375f + 0.5f;
//                crf = (crf + 0.5f) * 0.984375f - 0.5f;
                rr = yy
                        | (int) ((cbf + 0.5f) * cbLim) << shift1
                        | (int) ((crf + 0.5f) * crLim) << shift2;

//                cbf = MathUtils.clamp(cbf * 0.9375f, -0.5f, 0.5f);
//                crf = MathUtils.clamp(crf * 0.9375f, -0.5f, 0.5f);
//                rr = yy
//                        | (int) ((cbf + 0.5f) * 63) << 7
//                        | (int) ((crf + 0.5f) * 63) << 13;
                if (--yy == 0) {
                    match = -1;
                }
            }
            if (match >= 0) {
                for (int yy = y - 3, rr = rev - 3; yy > 0; yy--, rr--) {
                    if ((idx2 = paletteMapping[rr] & 255) != match && difference(lumas[idx2], cbs[idx2], crs[idx2], yf, cbf, crf) > threshold) {
                        ramps[i][3] = paletteMapping[rr];
                        break;
                    }
                    adj = 1f + (yy - (yLim + 1 >>> 1)) * 0x1p-10f;
                    cbf = MathUtils.clamp(cbf * adj, -0.5f, 0.5f);
                    crf = MathUtils.clamp(crf * adj, -0.5f, 0.5f);
//                    cbf = (cbf - 0.5f) * 0.96875f + 0.5f;
//                    crf = (crf + 0.5f) * 0.96875f - 0.5f;
                    rr = yy
                            | (int) ((cbf + 0.5f) * cbLim) << shift1
                            | (int) ((crf + 0.5f) * crLim) << shift2;

//                    cbf = MathUtils.clamp(cbf * 0.9375f, -0.5f, 0.5f);
//                    crf = MathUtils.clamp(crf * 0.9375f, -0.5f, 0.5f);
//                    rr = yy
//                            | (int) ((cbf + 0.5f) * 63) << 7
//                            | (int) ((crf + 0.5f) * 63) << 13;
                }
            }
        }
        
        //0xFF6262, 0xFC3A8C, 0xE61E78, 0xBF3FBF
        System.out.println("byte[][] RAMPS = new byte[][]{");
        for (int i = 0; i < 256; i++) {
            System.out.println(
                      "{ " + ramps[i][0]
                    + ", " + ramps[i][1]
                    + ", " + ramps[i][2]
                    + ", " + ramps[i][3] + " },"
            );
        }
        System.out.println("};");

        System.out.println("int[][] RAMP_VALUES = new int[][]{");
        for (int i = 0; i < 256; i++) {
            System.out.println("{ 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][0] & 255]))
                    + ", 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][1] & 255]))
                    + ", 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][2] & 255]))
                    + ", 0x" + StringKit.hex(Color.rgba8888(DAWNBRINGER_AURORA[ramps[i][3] & 255])) + " },"
            );
        }
        System.out.println("};");
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 4; j++) {
                display.put((i >>> 5) << 3 | j << 1, i & 31, '\0', DAWNBRINGER_AURORA[ramps[i][j] & 255]);
                display.put((i >>> 5) << 3 | j << 1 | 1, i & 31, '\0', DAWNBRINGER_AURORA[ramps[i][j] & 255]);
            }
//            col = SColor.DAWNBRINGER_AURORA[i];
//            display.putString((i >>> 5) * 20, i & 31, "  " + StringKit.padRightStrict(col.name.substring(7), ' ', 18), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
//            display.putString(i >>> 2 & 0xF8, i & 31, String.format("   %02X   ", i), col.value() < 0.7f ? SColor.WHITE : SColor.BLACK, col);
        }
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
        stage.addActor(display);

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

    private double difference(float y1, float cb1, float cr1, float y2, float cb2, float cr2) {
//        float angle1 = NumberTools.atan2_(cb1, cr1);
//        float angle2 = NumberTools.atan2_(cb2, cr2);
        return (y1 - y2) * (y1 - y2) + ((cb1 - cb2) * (cb1 - cb2) + (cr1 - cr2) * (cr1 - cr2)) * 0.375;
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
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        luma = NumberTools.zigzag((System.nanoTime() >>> 27 & 0xfff) * 0x1p-7f) * 0.5f + 0.5f;
//        Gdx.graphics.setTitle("Current luma: " + luma);
//        for (float cb = -0.625f; cb <= 0.625f; cb += 0x1p-6f) {
//            for (float cr = -0.626f; cr <= 0.625f; cr += 0x1p-6f) {
//                ycc(luma, cb, cr);
//            }
//        }
        stage.getViewport().update(totalWidth, totalHeight, true);
        stage.getViewport().apply(true);
        stage.draw();
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