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
        byte[] paletteMapping = new byte[1 << 16];
        int[] reverse = new int[256];
        byte[][] ramps = new byte[256][4];
        float[] lumas = new float[256], cbs = new float[256], crs = new float[256];
        String[] names = new String[256];
        final int yLim = 63, cbLim = 31, crLim = 31, shift1 = 6, shift2 = 11;
        int c2;
        double dist;
        for (int i = 1; i < 256; i++) {
            col = DAWNBRINGER_AURORA[i];
            names[i] = col.name;
            reverse[i] =
                    (int) ((lumas[i] = SColor.luma(col)) * yLim)
                            | (int) (((cbs[i] = SColor.chromaB(col)) + 0.5f) * cbLim) << shift1
                            | (int) (((crs[i] = SColor.chromaR(col)) + 0.5f) * crLim) << shift2;
//            if(paletteMapping[reverse[i]] != 0)
//                System.out.println("color #" + i + ", " + col.name + ", overlaps an existing color, " + (int)paletteMapping[reverse[i]] + "!!!");
//            paletteMapping[reverse[i]] = (byte) i;
        }
        //this gives the possibility of storing 64KB of paletteMapping as a 14047-char String.
        try {
            paletteMapping = LZSPlus.decompress("း㫘፤᧚˰尭\u218A\u0588洢甋ǔ\u0BE0\u2D29勭၇⫊\u0095濆䁕朸ݪ怪ĄᇥՌ⊄呉᧒父ᝍᑺ嚣ᴶ㚼㳢⃧䑢ảᜮℹ榒ᐶᓗ㌊埠㍢㛱嶛ጭ䬽\u1A8E㫲矚惍箸㶝ī劮⼎播〣ᇂ㼜ു㶲၈ႊභڹ\u23F9㈑~繿仹ᤆ䤭ך䰡䍙㳕⡁㣘‾ᝊ䣛⠠䍝⺥\u2D74㻍⓻ㆎ榷孊ᓝ度Ą⢜ɔ䵋㟔'ٴᦹ嵕㺗ᜳ瑌ᓎ媢笋Ⰻ曩᪇㍟忻唿!⺏ᐜ姠⌇ΐߌa偁璙ṏ࠽甘也㣠܌⊱䒀᎑͡ぷ┐ᶂ⎧怠ᶜ炴 䍇ᇨ橊睤\u312F㙞㗥爘慖ࠢ⟹洹ᗨ匱_⪼߃ᤚ♈攽䴆ⶾ杪峹➻摋粠\u202Eᧆ樀叠Ļଂ㞤勮緅ᦌ差⌵֕泊嫍ၠҫ㔍甊孶៝窶‣ᩗ奆泀Ћ班䖕嶏嫣ヶὬ橽屠ຫ࿚\u0089抋漖絼忏樯́嗜῭\u1DF1壞ᡬ◩䟂壌縉䥓ᠠ⟓庎㞿曣H橓\u0EE4◲缾\u2E77ᖺ撷Ⲯ\u23FE…岿䟭殜ᮌ㭺垚ሌ\u009B㘟じ窋Z箚\u1DF6叉睛ῗ珥磟\u0DCE垴玑ⲻ㯽羘㸿↳盿ᱏ仅擕㵿㞯‣䴡ቡ窡凹磞敭篞㏫箞壷稞䷨Ӂ偌оࡊ㭡ᡍ学㱂穁㗠㣁ቍڡ䱐箁\u0FEFԶ䙲稱扄䔡㉁硱䩍筱ᩘ堠呢ዚᣆዦ壖ṛ⃮ᾥ棾៦⭆᳥ԍ扆䒮\u1F58椏淧咭ᖘ}嚸΄粰剤墲勘撱召眳勔沷ኤ押厐榷ౌ䖳劌䀰㐌䨎ᎈ湏\u088CㅆႼ㩡䣊ʤ凸㜯猲㾢Ⴂ䒢\u0AC6㔮灂䪭\u0BBA⫦娐≵壒㡹燜͠ஂ山᧒䕤䰄㹼恌ᖫ煦♱㦼╲ㅬ㊩प疣੪㣦\u2BEA䝫䮲癩Ҫ睶ᥪ忣䪬Ṱ悒⣅Ҷᅠ歶⓵᭼㝮瀮㳼㢶煀焞㥹槚⳧ᡦ烫婪䓺嬐ⷮ↮仪㬦\u2BE8媪Ỳ䣒旪䕒㷦ב᧶示ⱉ㬂籇砥\u20C9䕃ф竁ߤ□稣摱Äᖞ忰娊ŏښ杂⒱籂䝱嵎䝥♙ᒡ䝋䟩㳄唡െ䜞仂ĕ㥘㓳㪃➉旆喩癄工㻊㧝⭃Ⱓቍ䳎\u088F✽ᒄ晹䓈⥳ᇄᵓ㱈治⺄ףۅ咕㲎甛ዜ㗳⚅䷓㎅ɯ\u218FΛᾃ㵋㖂㝓⎁㙘极㸕獁尧\u0ADF尵\u2432ප当㠈矓瞝␄綿㈍玀堁瘰瘏糗Ⳏ䏠䐺綃䌁ጃ㴻綳瀲揳昲泓檰∸爺⊫䘱⍍妔珛⊘䋋疔⻜䎓亻璳ᐔ粱澇྿̘缱Ⲍ枴㈼禙ቧ溷匛䬸労愒㋛猂珔溾片嬵Ἳ笒珴喽怛攒弼嬛യ䚶珧樛彛澿⣚䙻橆㍺દ孹烂棺罪簐罃勊櫌僟挝䭭緺\u008Aۋ\u2B9F൲硚偨耀ǰጣ卤ȸᕊ㫝⨍⩦\u052A喡眡焍⎣䡒Ͱ㨤[㋋\u087C榰煱捋嚡ᛁ\u0887⣔⡙䈨⼠桬䌸ᰭ䶖ద丳倧⡠呗䏶乡穐棣\u0EF1झ囚ḷ㫲ᯑལశ债庢ᯝǷ恏啱߂⚧ৃ壼㑡吸\u2E41ᩣ\u08C3䤈⍑撪ᄘ桬₸⦬\u2FEB\u19ACኼຮ匾↑畫桮䆭ᣯ⧑劺值元ᝓ≂ㇶ枤暎䫬瀸ⱥ埲㧔◊j䍇䇴ƀ嚥ʲ仼ⱅ嶀浔娤憀┗⑶䊴皆溍\u1AF5挂㾃秐˻្᭧㘔厎囎杙ᾯ䴕叹㷝奶⩫咦戬ஏ⻇䶌৬奦を䗞灖\u0094枞䮮礇⋋䋘懩㉃䖄㏀㫖ࠃ䝇發ῤ仩檼ྰ犮㓛獉杯⺖㝄ᗜ呆≱澒姢緮㌇珛撜柩๖ᆹ䞴ḵ拼ɨ\u0BA1ⴘ乤ឌ䤕眥甙㡿ןᱜ㷾㴤仨ߘᒂ伵位珞屰缇洓嶣㳷ᤘ䦂秂䛨劣瑞ᩲ䖄➪\u0CDF㳏嬚ⳕ䢿⥌慝獾瓁℮ࠢ步⋈\"歠㦠㠼啀振ᤘ嘨煵ᄢὠ䨬ȵⲊ\u1AD7߀䓏⪭⡊浰刘ዊв⌘Ⱊ䂍⬪師畩ӕస㖆¢ే\u2B7Aೊ\u2452䔰嬪䒗⡱矁窮唊眦㔤も吱⨦姏᪩ຕ䰿ₓ⣐䬦ᬚ峈⒅堐䯉傄溚愲ᕚȔ⫓\u1AE2ᕥ給䔍㚪ヂ欱ᯀӦĐ猫㑳⇦ᱦłၢ剣\u0E89翅燿ς⤿\u0C29唳䪏㜤汗盗剠#渽⩦ᴵቭಳ拇ㄢ夵ᬘ姆䓡▩Ấ瓕²Ҡ榲㡸9寄⤰ヤ挈Ỽ了䆀⧢杳䕜楅啲潳叾瀠ᭇ澆溳楎消揀箽\u09BB媁嫴䀾㬦䪶㭪㤧忬▩ᠧ\u1978⚄枺Ὡ慭䦉↼ᦛម以㉇ኑㄉ攁忭\u2E5B㛙瓧岗ᒘㇾ槨疚⤟晌碒牻促ِ秸オ⽕獧倴¨ু㜹爜⪙乷亏ᴪṸ\u0A61ⓙ䅿ల扌垄\u2B8Eㅛ⸢庫粛峑⾶冝ᝑ捉䧿掾䍖旦㹧⚊┨䝫ㆆ䵹奈沶᭲⩲敘忪㪒❴秫⿇䶙嵈冓玓ṍ旗歫亇杄熵涐ᦨ᭦竰梵䥶䮉㉢䨮\u2BE0㜤ጒ坅䩻᜔嫡䵹\u0A46塺䇍ㆲ皧恢ԅ礯嫑⣕䋝穙嶣斑囒䑖ଘ㔺寈疤㙵ӌ歶嶊▟樦曖䕳\u2BA8粋⫤毀稵\u0DF6ڻ䗖㜆痍䛲㗴㋼䁵席曑对坢䉑刖笊㍭ކ事曭\u0B5B\u0AD9\u2BCB\u20FB籄琴\u170D\u2B60畭棌盯嚛篋ᣖ\u197B䀠 毋矾姦䝾砎恝፩歑猲䁓坚䣡㯁愗ʋ憃㞨ₐ䵄㉢䝷冈㟉厺ㄖ嵔ዓ㹡⒫楔喍巤絨㳬俟㹸പ➬䚅᭠曝\u0FF2Η咛啚便⻇橴斴汭ț梣㻥Δ砧挎ڡ\u1CAEჾು䏖ᮙ㿌塕᛬こ㢞㋫ڬ䷑ಱ毓毯捾\u0BBC䏴㲏⧥㌴璯༥岂䢖㞖Ό㉥搂㌼\u1715④䨷缰ࠠ㴐㶉熐ܠź㇟㺳䅔᎒㍽༚ᶵ\u125F⏫庹粚ᅙᾳՖ抉线㈒糓桮ⴚࢩ᪙䞳ẳ≸獐咆ᑲ年汌枿ਥ县ᄪ糏⌗禧浫ᆕ䫰䯟␓㻹䁸࿀庫ࡀ⽵畇\u17FB䪙㷪琐⺁Ɯុ῾磓ⶩ㱖⎅怠໐翲济㕫΅擺⫁弋⏏忧絋⠑䇯殞ᇮ羺伨淴士㻓縷禴䶔学浊嫖⠠㑊欞㣰\u009C涨\u2EF6栓羷ⷶ斺チ嫂ր\u181E樴䂑汋\u2B8A罇ᷛⅎ毢䰭䢧⦖濠尨პˈⶖᏔ䚽汎䘖⇖ළ淎掖叓傂ᴾ捖竲展沄②ጱP᱾䵚䰠剎ġය埑偓懾戮ⴴ緝˰\u086E\u0A65ໟ沁沂㢔碢ᴼⲮ┬删ᆹ普䲔亷䒤ચᄢ牒᳂↲俲ұ痑栚⍣ọ᳙晌Ẕ㏆ⓩ浰嚗䬩恙洦善㘠ıı烅毲ᖼ䌇焟֫共䄲ऒ䰋䝄啀㙤慤\u2B7Cゎ㑘䑫奼\u0885絕㰊Α翏瑳ㆮፃķ͝㬧ब㜁楛㰚Ǽ朋䱔礗崅圌䌢ᨱ埰།卆䙧䍝篦椗㭇Ꮓ∍眨筮૰؏棘稩墂∊ᣑ䝇咬氇棝㰛䅙Ȋ溅穌㖜氂梻ㆂ䲙ᐆᣦ祬◃\u10C8䉙燷囨媏佝䗌⛜渌╈妡༂朽⾙礲垴⼻㥗䝔∃琁猩碗叝✿碯䜗岝弸ὰ✌㸂琉ԑ祏它ソᾛ䒏䡴翨ⵤ宨Ì \u2063竿夜ဈ羙⫀‥硇䓣Ȳ⼜⒴罌⚕䥍嶂䭃秿卌䀎⒪╵䄢仇ႂ実忍\u1CB4\u2D9F穽庌䀳ᖌѠ决岸㭙尐墥ᦍ䃕Ɋ巔堃擋奀⸌㤨⪈桝尢●䃢ؐ┢㠷咦ݒ㊣犽ℂ\u05FD吡简㮗硨䪣嚻䠲\u05FA㒀　愌倠ڷ槾䳔嗇\u0D5B㓒૱⦦啃嫨䇠ῇ㵛䅏㘀䒉汰⇁䬬止瑉ࡑ䧘\u1C84稪∊ࣰ䡙憧ศ懰媄䝤両ყ⇠㱺\u0880痧Ꭵ歄䣥䴆ᦄ斶愴瓇ǃױ慮පഋ㧭戜䘨桥呜ᆖ\u0FE9Χ夻娉ࠬႁ扞ᅤ攣ឦ䝟\u1289่䋚扎ᅜ懣㙀≉ᣉषᔄ扖㦓亃㯓牀兗䉈䮧欣٧䰨籄㼮Ӎஈ稅橋中ො縅\u07BA兝䄹ユ素+翮璄瀠ӗ\u08B5䐢⫼㶪ກ䒚ᑝ灼འᜡ㐪\u2072ʨᑢ㗃偰Հ砡īସ睴ᗁ㴲\u08BC⽓ᡇ䍬戎ྂ爱㴪ᡰ㤕ᢉ䜂✵\u1C3A\u16F1⒫ᣏ㌫ᤜ⺭媎ᱶ\u169E㗼\u173E㎀⟷峝ޯ挐⒂㟜弴徙ᅌ≫ቷ卆⡹֑渂ӧ恡ūၾ玆⢩㑫䉬籍ᒁ㱌䢻ጏᣁ⢪ɷଦ★䞜碿䓢⛒㵌璶Ⓕត䜲瀏䂢♙㚲ဏⓉ☭嘝癹㕧ᔲ㧪繺犠啭䏪်ᒲę⼝ዀ罩✅婋怱㔘欸ᤜ▃×ᚚ\u2D73㊹䫌哅ᝋ䪱畯⢪⦣ƽ縩姽峊濥畢否㟍宋⫳◝ًㅼ檶噍孲溼梜囦伡冿甇┦❲簮璴摕Ⳉ⍻瓑喊➌傤䳓媸り㲧ᬏ\u244D㗊杢嫢旅㋢ᒧ᪠ȋ\u0ECE᧭厺Ẇ䇹᧳ⵠㆅ博ᧇⵚ䡀纸㧲ೌ僠㸦夨ό厧滗\u218F劚罙ɴ䱵䬚䞆佺⤕䡇㉙筍ⴋ䰼祪䃴\u0D80樐礆夺\u0D77䣨䋁ཉഢ牼歐槺ᆂ痜䵀\u1F4E㊢盉ἇ睋ൄ検櫧䧅剶盗 ㆗ኸ䯫\u1249姏Ꭽ窈㎛ஒ␙\u17ED幈夝Ď寫庇珶ឦᮖ☧屳䉨硊Ⅳ崨嘥䦊䮔寖⏒ຫ䅢ର峍緥\u2B8A\u0AF8圆䱉泌䵗ߍ綊Ū播妅ඈಙ婍咑禪全焍勑纍᭠㰸坾┗㭧宨帍ྂ㲃સ噑㮏孷孭呅丆䉬ኘ夽例冁ᒾἽ掬䜿ଣ吽䅋ᤦ磴必ൊ東婭宽罕榚㯴埘୍\u2B92㫄叅ଋᦛ㦫孽匎奴\u2B4F枅晨]㟚緺ૐ咲㒳疂咹噘Ừ緻滰圙㔝繺䫡嘤ʛ\u13FDἂ⑥⒛畼ᓴ䄗㦚㒳Ỳ墺⏚瀾Ἅ畯㞚篼㕁⚪彌࠷滌垅㗴៳灱瑵Ⳁᚽ漞ট䉲⚻咮堊➳濽⫥哺㷋劼\u0A31垕\u2B8D㵷櫻\u242D⤚䄩嬍唍㠦ഥ\u1AFB啿⊁᭵嫃╀礦筱璥睂ᎋ❽䇈།⾡筽彦昰撊佸Ǽ橽⫓䄄䳀㌲Ἂ䝸竇喽㮀潲洵栨Ⰺऋ竓氝㝢纹戇埊˒⣵䞛\u052Bʓ㽰沽ޓ➼焹戙撣㰕涱洏⊃⇓峽䛲㟂 捆ᵷ㞥姷慚Ẑ块璞刲椤ম\u1C8A凙ቘ\u0B55姿ࠠʾ☼岌㿃庋玃䙬䀎纃䑜ヹ庙漼玍䜋 ✸䱫㼃\u0E5Cৡ㔫⒅沉朷ࠎๆ䞲㜄䳫㏸狱睋㘡⛻ۺ圕㙉⽆৪ฮ䜴擇㛲\u2EFC事\u206C㊌ᣚ䰻⁛໊ⷴ䳔矺䜰䊲䲔滑姿㲾䵗ಫ⩳佪ᑞᓇ䍔仆亇䭜噢皬皮ቚ凵监䱬番ײ䓨珌滚徱拫䜌湫歕༚療◨噩⼐盼崇ݓໜ睼甆䉧ᨙ侔沜傼䓗䱜憇ᢸ㻑璀 ව嬥攔矅㵩⦒笔䑧\u09DḀ桍⡢ఝ檥儕柁嚡⬎帡ᆅே歞䨙埗繏湞䗉熛嵰居洗㠸䭠崒幌ዕ㷇瑡ⵍ㥋㸂ႅ䅏䋔嚶\u05FB㐕榟吲Ⴅ甡᳓緬弞䫸㵈絩ㄺ攕濄嗰ᤡ恲㫓㗝毁丬▅㚋⥅孢や䏄ᮊ䨬怭㘃➥渢る䍝Ⳛ研囎楆妞慙\u0898䍍⻕䷎⛞←䑰唯愤疼ᦕ䌭ة䍢\u0B7A厥滍䐉䤁本?⌻ \u1AE6澁潹䝷䊣䃓筴ሁ\u0FFDⱸ#咭璌掀ö壿删¬佧戊栖䂕牛竌溻᭗姛搠Ÿ宜⏧⡍䞌ஈ据䍍璬撅䱿ˑ攅ᒏ揭妍厏ɉ疽媑挀䮅掳咝➅⇚揕䗑屯䞇ጽ尩瞎ᥣ抎ᾔޭ㲈䷋䵖ᱯ㊞䷻婑獬坶ᡌ毝绁㲒ᎈㅝ忀#㚛碹擡㷗㙓熽廤✶⧮▏纋睠爛㖎Ѫ抟㶛㿿憡\u0DFA㈧埽䓼✺㛞䂽缀把㑳㶌ഈ灰窧⦪⓬ཐ簘禈∉♞㺩悾繹ઐ偓䆸刅ང尛➎椶掐嶍窼瓡ྸ猧噙泣䴬旧瞹爟ᯈ爽么\u177B晤痳扊泩㲎㮓㎸\u1CFB\u0AD6ㄶᮺ㝸䴞㝇毺犥㬔埓㷦槵ྔ烞\u0D5B៎瀠מ㦭䰫⾧Ö新\u2E42砷擐Ể┯㢛Ⳟ䇬͙旯᳖䖸\u2D99篮栫珟ᱢ悚䳝◸ⶠ✳↟珎⸥硵㘭ᖰ彙痯ೕ▾\u2FDA䎛ᖆၘ峂捏ᦑ\u0A7E⺈䨠㦛䯇᷅惎峔\u2BBA債稝\u1DF4ઇἚ㫎慿攖⛵ңૈ毆弢癷㟽ₛ璊绎䛐\u1AF3ⶺ猧庰\u0085ᓾ㕘㜳⦞㛎ᴁ嬶幚泿綉㙱嗕燍ᜓ㵓傍溎継欴ʭ嘧ࡋ㠎嬻磣滀瞧㬱纍掁ᬺ嶔国盂䏟⠠㛟殎紹%殨晗ྟච̺䈏\u1AD1 㱒毎嘐礨朗溞簗⒘Ѐ ㈯傝盧毂 䎅吨瘿⟏ߌ滽粗ⷝ\u0DEE洶琾ᤒⶲ氠\u16F3␀椌匍҄䠠ӑ\u0EFF倻㌳力ᐛ㙉犃傘-塾㔟\u1DF5榮⦖\u18F9ᳶ㳫罦染架氠ᇿ佤㯢㵟㽭掌瑝名⇖⣒絖㰍侀«劜҂彃旛瑝滾\u218F㾛।㻮庝 妷㵛疿ᦪϣ㷤棾㼞˔穾碮洷㟛\u31EB㔭⼞\u0FE4ᶡ磿㼑⧨筡悃゛汢仾᷇ἕ矱ᵻ竿偵䐖ᡁ珿碛ᐟ㻋慟娚␌\u1FF1瀫稓\u09B1侞桇䒕䋫䘢䜯͙瞵᷁滞粟控ᡱ㑜ಙ⾹᱉㉜,夏漍㰆珷ฯ▾ⴁ䜣簿䍔夂㎪㾒㔞堕㾓繖懯䔍⚲㡓嬝堆杻筳呅砟㿋繓媙ࠛ洭涒㦼沬耇箏䦽㺟❫炟甞ဖ䖃ᩫ涵䐠t彛匁\u0BDAᘸ妵汰ໍ⦒嗼ᜨ巁㑫咈ഢ㐭ᗞ⻰Х摫㑰ࠫ╱ᚄ́ぅ䐡瘈〾状Ơە殠䥄&埵旴Ģ㶑秸䂼˺痋妠幹禃ざዑ㧰᪫ർⒶぎ喞ⶢ盠㎡ئᠳ悚ǐᩙ樷㟅Ᏹ䷪㝱ⳟ恡ᴎ㝪堹疮ሠˊᆂ\u1AD4䇲Ѱї㴬統橥㕅ʞ ᇑᘵȮ瓺卡᧘掎ᓜ漚٭ⵠ佰䘪㡓5ˬཐ『叉ⰺ䞳渦塭䆗ޔ┢㡈惏岇▐㜎䏙ሏ箳檠祦暯\u085D⻑\u0DE8ᮐ ݒ㐤ᭇў瓨❢\"䷢༔剁⢦仃\u087Cⅴ稆䶯㙜儐\u086D睢搠ô緳Ⴐ䎒༨\u20C1ߚ戶䞵Ṕ窃䅰瑝礑ᰠ+ൣ̯呋ᄌ䍦ෛ䀠⚑䏧㊚䀠።汛㝧媾㪙䔭梊瓁ᆜ߰猌疱䯧パ䏓〠ᦿド䊙癚䚁ઘ㮀]䡾ᆚ樎埃Ⴕݶ⅏⛢嬨%提䳜&㦜ᢐ矜㴔\u2BFF炵㱎ื報室甫䟐\u0097妋淏㕕没㱅烷㲖ଘ␁⾘縂㞲帠䙻機⛁₨⒮简緁৷帱焚娅硵㻾竂ᓻ㿢䐖剞ᾆ㒣眩另䊻涪摘㻎秇昏౭⢩篱⾊⎙೯亱洛䊄撆烌䕛梥㭂\u1FD5≍վ⊉๏䨒䱘ጠ^硱Ǡ&䚏剞甚寃ᑨ繸䕞\u2DBF㯘筇⩂彔簫疂䝱㥧田濖䢫ዊ\u1C88瀽ᘕǠm疜ự橗⢬᷍ம头 皝യඞ䬙ᴴ瑪丶䕾棟ᤵઔ㦯椗綣ࠠԾ㎗⌕༜䵚䃐挰翜ㄧᯚ\u125F҃⦨侽\u18AC扣੮孑烥䠎厽繈ℚ\u1F1F▽\u19ACݽ棷㣎㆙巜˥倄瑢㆕䔊ჸ丝㠖㙊壸揻ஔ㏑渘㱥₌¢²栟\"ᰬࡇ敕畠\u0D80㸠\u09E5✵\u009Dǌ䨀ᾠ夃ன㽲㢡Бな䏮秦ఱ\u1C8E焴壠ᬠ命嫂Ṍ㴽揇\u0D5C⍎∈⠰容\u2EF4ݾ᳘扌\u242D૨㴱˪呐㲈ጦዦ粘ᙈۨẘ务徭์㢲冼ဟ呎ベ眴႗Ń㥰ጘ樠䄩䅛ĉɠ䳫収כ吠灪aࠞᚠ\u2BA2樮㗲⃫ʆ瑪婴䯋㼰わ燢ȱኀ䤲渮曯坿⒱烂⩖!ᓘ䴞䀠䔺ጲ࿐⇡搢ᢱ箩ৗ㠠ˉኞ↱ڈᠯᜲ焫煀⓭嵾炡ϲ\"㔨䳨≩ϛ⒰⢟湓խ汰劲⩫桍⒴䎲ಢㄠ᪤㒵劐ℭӈᓰ䎍穩㑆ベ䏜౸㌾瘣䨾⡣慩ڠર狡̫呎撦䍒സ㧩泺⒰㊉愾┣欻ݣᴯ\u2BF7攟፳㐪⎁Ⴥ樱䡣⇌穥႗ᇣጮ\u23FD撯\u135B㑸㠾᪙樂䩿䦘箣⇐䃢䌫Ắ\u08AF巾സ\u3101䎦☼㡫㙓҂᧻ᗣ䄖Ɋ哛䊊䭸⪼㺧ḱ䚼ᆏߜᒨ筳ጫ≝अ㴚䣘⠱恦؋㢐構Ѣ\u1C9A➭ߐ磼㓃\u20C6䦗吁姇␃矂椴磍\u1AD4䡂眭䐵ボ∹௺\u2E7A楡ະ䑲ᅱ箲\u1DE8翢㲪\u0FE0嬠<坨.栬嫝㝩揻༲怾亄±绡愺ᒒᆄ䚒ᗨ俲粩ᣢ烵ဠГ͇ྈᖣ煴⏰㭦䔾摪ᅼ䒶ៈ癒બ⩊䴜㌍࿔㽺॥抇\u0C73ᧈ\u2B86ለ晒⥐㟺䴏ㆉ䥔㏾ᝥᦼ晨ૌ攸怠⫨娕禚Ꮣ\u1AC3㓮\u1A5F㾇␃࠶▹嚅䖱\u169F撑槯ވ忂ⶭ⿸ᢥ㌵䨶㭑ᖅ㐊揂件\u218Bᗌ憂竬\u0B59ⴑ㍛璬㦞率㖳噵娍䛓ᑟⷒ搒\u2FE1泧㎃খオᮅ礎Ɑᒮ昖\u1AF8宂ˮ子沯㋀卓峮ᄆ綱ຄ㨎䔻ጟᎃ㟧䝟壤犵乮㩙y炰䂔B ੲ⺉㦳䓾ዘ䴃Ҳ䗻ᙨ嘓䳼◀包䮾⺀ঐ╷ᒞᴂ痨␡㣑䂻䩎◙圇挋庡煽杞ჼ排䷫‥峽拼璀ⴜ刅䞹ʊ\u009D敯Ṽ䴓ᓐ㹑磲崷䰐Ԡᘆ垺湻㦗䚰ᄞ␂ⰫZ撢ኼൎℙ䈇¹粂\u09D8┏\u1CB7⌲☮佚㢹˘\u089Fᰩ笇ⲷ E堄〦幽ƺ֟\u19CA㌣\u0A29睁`\u0B7F亂\u2E4E\u0A46⢸䫒䨆⠘᷼䎣\u2FEAᡙ䒷ፄ䴥乩䬧㢳抚⳪▹Ꭴ娫巫罎Xௐ⺪㩩ᣄ㢿ኁ׆⚰倠˄װ硡ᱰ㶲丩楗ⓚఈ⩧䣩捄掃抓ձÐ团扣宐ᝎa䌌䨁㞥ᙆ桰㉩⤪♉Ự犳ᤐ䣛ཫò䰀\u1941̵ᚥ䩽䤺➥ᤴ玳副ీ僵\u0A34侈⦁ෆ劶䩲㊦ᘴ๔䡲♈+⃰禪ැ畓哿屪侒㺁㬴⪳➣䖞◅\u1774罢湍奃ⓘ≜ⷸⷉ㧆㪽橯⥫♼ዔ痢䍭号操即ㆆϦ梴\u1AB5᩼ᆨ䛭᠂擣୭畈ೠ㎥狦㐱岴ḹҝ庛➭៰䀡漍ᒴ弨䧳弩籋䣪⏞䱷倉ᆴ橸٦ᤳᔂ\u1AB4䠠ཬ㵁漮䨠đᓕ潄≞ࠠ௭㊠ǜ杪ᯨ㋁簠ؕ\u0CC5㔀㎡䲹㎉\u089A↷䒝幬䙆Ò勪䒩ⳁ\u0CF7淖ⴚ㞅⩦礷穸奂撼幒䓪'笂ࠄ湒眑峀䳺㍹ઙ㨅឵挳瞠^塵⎶ᙩ攳ᒃဪ恊ხ柪䳎㐛ƶ㕱焛夹妟ᦩ時帒槫罌㈼傣⋑\u2E74㑞ぴ斵Ὴ缒擛\u1738䷒䣌樥\u0ADF䫱\u2BDA\u20F1㗡ㅵ刭烘嗺\u169FⰍ㫯݂ᳶ⩝候\u2BB5\u1C85㴵౬夶斧ᳪ䭊㧩❊⬊䱮㇙䑙儅堊ᖂ㥬摪垤⩋\u1CCA歾ṳ㋕⸖Ⱶ煴䥸㖇ᗲ嗋Ọ亰嫎櫙泫⨥䮖Ⰵᩁർ禯ㅶ⅚傼廋䫉⫓欃\u2BEB埳䭨㽶ର\u0D80㕚䒚卤ⷋ⇋❜ᬓ⭃⠭\u20F5㱃捼繏秐ПᎺ徒◨བ礃猼倠\u0ADE㳺王䳜↑峵獰ⶅ㨐❷\u19CA㦋ު囟糜氛䷍㸑簅圹溅㔥攆嫺䶋濬盕㤗ᴐญ㎑绵孱浰ृ嗯᳠ᤢ寪㽹紈ŇⰎ㪕偸㼶殣煨⟀勺䄊䷯⻃㫵捂炡➕巴❺嵡畣哮巽\u0CFD֔㽉紇琛Ⱑⶕ毵ཻ㶔䄠ɳ㽸䫗祟斆嶺事ѩ\u1755悰\u0B31〠ټڪኟ\u2E5Fҥ\u1AC2⑻⊒\u09BB䓎否挳䱩⡟䊠ǅᢌ䇊ۭȠֱ煀Yऺ⑉⃕䀩௸⠰塩㷵僽䅶ම㜝椦䄺㱮ᇈ䛒ᩴ䳃㚩㩄ᜰD䀠㖞ᬈ梲⬭ʧ\u2432ᆋฑ墑兦䒪\u0888奏ӑ掘栳㾭礴㱵煭֔ᙑ夰䶫я⣃䛞ᎄ档⧥紷ሰ䁼䖾ᗉ刂侫㑌䥈員ઌⴱ㴾䴴䮋፠\u0090\u08B7∌煫䔠 \u1ABB०䦶㕹头筲 䂥姴䀡Ⴁᐠԥචðӗ掀,्ಘ+ͫ㜁睔㳵⡷樠ǘ屶業℠ʣ\u0AF7५憶㘅ဠ㠵⸍☵ᥐ䑺ᆎ䗙囥㰲桫▉ᓖ乹䮍憭᠙ᘕᯠ禋旐匜䃺ⴉ㼅㡤)瘉⻍仇䘿հ旣ᔼ冐牋ፌ狑拈⅁ྶᗅີ䛿㥤減㛌凒䕫烍䋈㌝䬮⠦㤅㉵\u0D77禃╓ᕣᴬ盝ᵏ㓎狝尖梻⤱බ㙽筱幇㟭怐秺⍊糅㚻嬎欩䈍ᢄ廷㦄渏㖜ᇒ囫後泏盳媞䰧Ⰵ剴㥻㖊⧠嗓ⶪ峔㋩㯶ອ䫁Ⱨ⦹Ꮤ冽⒚ᴸ皃宖偋ᣎ勐卡⬑⿻㋮㺆疷啠ćᕴ䖰E澽᪖槱㦝ᵸ噓塊䀡宂癒婊毊熪珆ᬔ㰙檵ⶍથ⥽▒儠Ƚ᧺呤嵚圊墼䝚猉䩁⺾㨥淇㓞\u2BE2ᔳ㧆巔昚ᅎ據⻊⫐滾⎭楗✍斖▶㕨㥚看夎睛㴍㯖欃⩑⿕㏽䞇㷰睠㕮睛嘒招Ȉ˘ỳ⬴狵㱝\u1776\u23F5ྛ契砓圾䁋熡\u1FF1櫏㰍樗⩕甔巶瞑喚㦦唊憛㬎ោ䫸笣湲兕⬔卹杩㶹璻嶾稣Ⱜ廊笋樠\u074B矏崠J␋㧸嬑窻⪍㲕䁗睻潸ව娯寽च䐋\u0FDC㻹⥇桾㆙\u1F17᮶綂緽歏勞桴氌㛊㻎篛滍㘕㟷\u07FC彬Ő媏农䨻㐉濞㻐尯淟㲡\u05F5Ἴ籢ǯ嘮圚徼瀉⽳㳓毟欼㠝䰔忰í綬堠ॾ\u209F痀ච㷞箋်⿄㼏橐ᴝ㵌ᠭ恚㼵ฐџ帡崚ᗭ˷ۏ歐᭫ܣᰮ使継畓瑿徺甊㢎㇜䈏橨櫀昣敆ᠥ掂น攟呡䦒\"䬨㡁思\u1A8E懗ۜᰐ溰涭剔䡰\\噭簯⑺悏\u0DEF唤ᑼ殢∼\u089C∆ٸẰ炭\u2E44䣷ᄒ䦪ഐ㿀伺ࠪ⧟嫀䉜⥂④崭瑕ᮡ䎦༸㪁榧ڎ燆䚸´湳ね䃕㑘რLٕሀCɃガ摛ყඦ㗩娆䦺ဠ\"⸆澧ʉ⧀ᆺ媢ᤨ獭䛔⋹ओ\u2D29㛑呦䩺⑈旍曷ᩁ狈慃倠,\u05C8惥ᒯ㳺䤉ⶖ␠ئ棺䵕㞔㿐塻憏\u0C51嚨䞺溫ⷍӔ勲䭦ⶳ໙嵑乺ℂャ灜娺Ṩ㧃潖㩺\u2B89ⴽಔ㷱浻ܫׁ囦宦ẫ†穱䟦ᩫ†ᣩ䝙⦸拁䢴ᙘ᭠Aޮ噜箑ⷰ璔ㅖ䀠䅝༌㿱捻ト ዶ绪䜺\u1C92目实ⴧ㚃䷗焨ᮋ◯㕣哒篻Ἳ䏈盾䜆楛″濆ǹ祲ᗷ㖃厶泫慓流ঠƣ⾈ⳙ\u09B4⠁汧㤽mㅞ㭺ᶂ易嬎寚ẍ寢༈⍚⽄漅矗盹礜ᴨ瞝坩䝛ಿ泊亶⚵氷⡽炕澀杼嫄䳫៩埦㒼⏓㋘挞桗\u20FD珔╟⽡ፒ乣ᛎ棛㣏㷀`秇匏毋⧙㮕ᠯそ㪗\u20C6垂刊瓚㓮巛吕᪒構☭溯㦳䥭䯲垐嵸哚㶎䳒県់榨珍Ḕ濳㭭ㇰ㔝ࣴ喉ȹ䀠䠒垦ㄎ䳛嚻䯀+榚Ƹ⚕牥ᱳᥬ汝睯⠉ೠ姹䳦㋩䞚爈⟐⚂杫ὖ⍓ख寳甕ᆙ俗噾添搑峹妶晳毷㯳\u0EEC獕洟嶕䷧宾墊䧋ᚚ庺挽溬梞\u31EF寳枖㶟ᑷ哾䆛玿㏍Ắ枷ᬌ盓痮㝖互㵉瑶㍎卒䮸Ẑ秞㯷᭟☠į῾\u2E60総丗傞椚●࿂㺭簏槐ᤣᐯ➶紛猿䴿帙熋㾻纃†ᘧ䧼㠫\u0FEE\u0FF8ⓘ\u0BC5圯⪞䄧羹䆖嫗昰堾尓䵱䠶䌒\u0980՞㣡䆋塹憑敊ࠐ塽⚫怀缵徒ϐ\u2D6Eṥ䈛ῌ冚䆶\u0088嬂爕ٌᡚ⌛祌⿀㦡栊᰼熋䗓玘ข澫䁌\u2BBA紋\u0BE2ⲩ㩅挡扸ₑȇ檿榲禑Ȗ烴ጐ䬼\u0F70㷥漧㯎䆉秆ឡ㛣⽣㱗Ӟ惧Вධẁ嶢簹ᦛ㺱߫䠠ೂ旫០榐缭挭ࣳ䄜䎡ⶸ㝆礫‾墈䆠ǥ娼\u088D斯ᙌ岰篫ᜭ糔⍰\u2BA1ⵈ㘁弧倥碗纨垒廰怠ㅸ圂峒怠儷ڬṐ怠ᅴ垢徨櫁ᩕቔ䣺␉༅㞵炦䄿斐嘂嚐沨橃籔 新立\u20CC#攐橥ᗊ橜䤃\u2BED⸩姑呶喎撃嗂坐毨矋棗竖歩\u0D49㜭ㆶ壶ⱎ䲋䂊䛊੬䀠攚䜺庈瞭\"秶ṋ㎃㚯㩝䣿⍁㘄㾱僦⌽\u1C9C\u31EB䜖夺窃䪯໗ᤐᰠÆ仞㬍⍯ྙ倠⡴ᰍᗽㄕ盦喉㶀㇜䜮ᴤ攃㣕乓竺ⷷཬ㕱婳䃸ෑ燇䚁庛➳!䉚ᴘ窻\u1AD5噻棾掙\u0FF2㱑穗\u20BE綜爓䙑䧦眻宯ᛉѠO䇕狲⛹᭜乲㘑畖ʿ䏄ᚠŃ勼択䦠✹ᬤ篓⥮ॐ穿ᬢ渾愑䀡ག洒㕩擖後泎⸘⨵屶熳ផ捝ᓻ⑤䲋㢛㛖\u0EFE殔䨇㟩ᰄ窻䅭啗箝\u13FA侽幙涆劽巊冾怾仅ㇻ➌巚呻宿ྺ㪉嫇檼抛\u1A1D䩷ᶬ恛宮Զ坨㏁仳\u1CA9䲗ẽ溊娌晙湮抓Ẕ%㻯⡛⃯ᵙ伎ᘁ䶫匽焆垼ឌ㧻朙济糛筮\u0B5Bద㯉价㸹䀠⠝湶㭎玖ⷹឝ帚曛娎絽ይ南㑽㮂瘯㑝纖\u05FE媅巶盫壎糽㹴嵛Ỡ猫湹桝掖䷾暝㸑盗奖⫨㸎ݞ瓲筋沿㛝笗㖸瞐ả➗盞歹㬎࿔㼂环涯㋩䔖厾亇櫍ၫᮼ杽☏ៗ峿箃䰠猝淊➿\u193F妠ů䡟Ě㽨ཏᱎ棛⠌\u009D侂ݨᴉણ娮㟿寓Ј໕\u1F7E瘧㜎㯰栠\u0089ؾᢗ∟礠̱墕ᶌ\u0BE0ⲡ㨥缙䡹纗秄\u08B8㲁疧瘽翲湷ܨ\u2E41㟣䐶ɟ⁉䎱ཤ㹀犑़⣛懿ް⼖偀\u0E8Bਫ਼㺅稘ᚘ㿿 峞໘孢纫扏㒑懠祊Ῡ㨚憯䙞纜☑ᜈ媒樠畍䒗刀䯚⿸碃唦乜ᦒ☇戠ىᆉ䖻ྔ弝㟧ฯ粑ዣ猰¤稳朮幘⦟▸ڼ䀝†唏簂㡩穫ソ彞Β紪\u2E49㴳憶䉪㦈\u13FC优媂拫 䠲塲翫咼ᝲ\u0AFAչἤ祳吇\u2E7C乛喤噼ᴝ\u2FEBÍ˞䫴䘀\u0084绳吡ᕚ唟㫈噤ᴊ癋磎烲⫹⬦น㥵烶楸㦃䋰Ǧ㢹檇⪾嫛ᨐ䐓ἵ㥁狮\u037F疟嗕乖㷊捋嶿⛔䣰❪㝽䀜䀠糏Ḽ牁䝶嵼禜ڨ䞶㳙猇⫏嚚ᬞ殕ⷬ㰭壯捜㖉喻城廚縇䎼/扺禋ֽ囙ऀ\u181DⰠэ䗱ซ\u1CA2瀠チᝃ⺲炕凷繮Ί䯐㙒㷈楃!ጹ㪦縷寎庐竲ឫ⾄㾵塺 ऒ熫塧䶏⎎ᜌ⸠֯ଝ专䞩㵅怠棜㜡嫅窷ろṐ礝ᝐ濢究祗ᓺ压ᇀ䙾庶挷暏凞ᗤ堌漲硫䳏ᛘ\u0381烥⾡将祷➭喓۫孺平ㅍ䀡憾帔㭍䇎ᆏ劀\u0098悎\u2CF8*ኋ擏淼嬒ᰜ㛁ѵ捻ඌ喓܌\u1B4E嶊煍柧ở䏇毹㛭尭搳⎭䝜ฐ㞐ᱦ画燶碫䜞ᮽ㜽妖移⵿䗖丛圹崚窍䋗⊺➟緖矻彎牗\u197E䮖ⷠïǽ箖༘㭎怒㵭溗ऍ䮙宠⛕ᮍ旛栎\u0097禅垵澣塛徏મ༄㯲☭差癛濔&㺍甛尼㮟ǯ㞥䰮絪暗嫹䭀\u0083縏矝漏ḝ湿㦝璛\u13FE㮗ཪ睅䍯㯝崗⪨㼑篼瞕䶝渗矿⢖⼇矖垞缸『㏿缁箩㭐㤣琯氽⢐帜ޟᘶ㰛弗珻垙߇⍠翽洯瑞侕緵㯉䲟ポ䄾䒞ሜ\u07BC暫㽃竱⪅羕␞\u0FE8㻱牧渮傞䈃溽ᄗ嬡柱橞ₔἇန㱑癧悚■坶㸂ཟ堾摯䉝䔓Ǥཛྷ㺒偣刐䑞ᄕϻ㨴礳皯㩟ྜྷ搂砊㱏䦄㚾犚ਝ䟨ฬ笃纗䡞ㄛ怦✠ȱҐ懢䝌\u1C98砟ᜯ屘纁搄ᝀ弡\u05EF㝜ᢖ懸⼰\u0E98睅撯癞䤃⎿R㤫溧ボ璙北倁\u1F4F吃窯㉛⠡ឰ徲紓楧㓟璗理　⽘煺䋱㦛㨟៏Ẫ窫繫Ἷ紜\u2D6C✤⽍䕷傯禘ᘗ⏚帧桢片Ἵ粒毢溩㴤獷慿榜悀㟡忲㡻巏䘾⪜䯷伳㹭翅羮㿯\u0D0D柮伶缻惇⊿ӕਉ❔⽈㏓䛾䱚䴁⎨彇\u0DC9笏毝ᚒ䨓❧㼽篓傯果変崁߫䱊朏寞\u20FE㨔䎿吠ৢ⍠\u0084傟ȐW᰿惏ᑘდ瀓㻝絺孷䝙紇砐᮶㠥猇\u209Dࣰ7ᐿ䢞−᧥䍇㺽督绯緈紕嗄⾱稙扣忟刘װῸ羃结繞擟ᢆᯊ⾖㺥爇⦼ᆘ䘅Ὀ怅〠\u2B5C杜庁倠᭘张ϑ☐@䌿䮾熜柫ᾰ粲癫砃㛚琑嘚⽊㾥縇纜⋔䗪\u17FFᴫ矫住ጝ䰒濫㼡㷥眷ɾ嘖堉៎㑲燯畏㭘5磫絎ⳛ琂促㾭稷瓿㎟䘐\"\u245F\u2B9E星痠\u0091\u191E熒㗠Ø䔟䶒\u1AE9㝖幊瘭槷漚栉䮶⽾㶕粻羞㶔ࣰ㽜廫㘿䢟ǜ܆Ὸ绣㭕碷滿⎒M堖庽ㄑ皟\u09D9土 㝝㴕怠ࣾ罓㺶江䬟␝\u2FE1㟆庖緻猏ᔮ㠎ᯇ䘠ʜ《ช滛翍濟Ȟ㎐圅㿵紆ほ䆎㴏\u1C93寲盇繚檷狽秐Ḃ罵彆犻瞟滟砃㾿⿓䀕掽⤟域忣俧籗祰幂毘༓㮻绉嶓尗瓿拻劧ࠎ␝怚↳耏翸㯕㝠ự沿㭫㔣渍䕯Ҿ␜怛⠖䅧翺巚嵧㘠?ЦĒȋ巹⟘¨ĳ汫㮀\u0CDD拧卛আ∣䐦澕漄叵ޱ供ế糛㮾烃楧庠挡ᅝ⊙䠨ᯅ⇽q罻⩝岣᪇㞣毠㢠缡ᘣⰥ倭ἂ㏻ߐ㌾Ǔ涏㭹燓桀✧㨾ణ⿺䰁⠵ၖ➵佛㬜Л嫹眳检⽠叔欌ʛ㔐栴廠柁侒Ȗ㶔р瘡档噯־笢ล㐮࠰揵悉伨堶㳜؉瓰ᑧ屯㾡儢࡙䴖зごၯ䃿Ǆ㳂ڒ劓欇住\u18FE⌄\"\u0B52ݸ爓洷婧ಮด嘣̗\u243D寫ၮℑນȰ壥狫摰㕀找仝◨\"ង\u0DE8ᱷ䄝⥾Ü甥䀡ᐽ\u2BE3ၫ₪䆱㸊碔ഈᤃ嚫վအ㴤㬔㐴\u2E69偫⃖䆏䰠Іᘭㆈ毩垫⽚ᙄ緆֭玸\u1AF0㋀勡斢⬥߳夆塜㺫⽥櫠Öֽ熸ᯐむ犮恍ܧᘨ悆塚㞬潈⁷І窕甓材㺀绡埭༦ᘮ㰵ᯧƬ❄ሿͩ窭燋搐⼀佾㼕༧眐Ḋ硓绂←ⅳ᳁ݢ\u098E⸗䜏ᵞ䰬ĚಓЄ䑙↼ὓ師㴶礜ธ\u181FὋᙁ寝椘 珲ࠧ棨ⰿᡞʜ孙ᰖΌ⑁䏞果庴緉祲༛捏䩿㋮穣㒦ᤨЄ柯࿋ὖ⇠ř窒\u0FF9椨\u20FF㿞䄝Ș禖帉៰⢐僺∝⳥Փ盧澯刿⥎ᇍ砺ᖕ洀(䪈\u2BB0绮綜缙ᔮ⨷旲⿔弹㻢䍑穒෴Ὲㇿᇁᇘ⚧䚗ᨺ㑝濞板㹖䊭筊ਯ愈㱟ᗞⵢ㣚渒㐎߱\u1FDC㾉\u2E49粅㬈垄Ḉ㳰皞ᶜ笛瘔ᰏ矩\u0BBD儀缅糍窯犯殏屷\u1C81獼⠘栕嬆ۯ桶㽯㻬嶝竚瑏漋䕰彎㌜戚␒堍⿻ை㝱⺅䐋箟煬\u169F廷ⶁ瞝ఛ憕\u128F俠徯㾕纹巫碯盏政侟⢎䐝ᐘܩ䨻\u0DEE᮶㽥绻䍛竚獿澟䐠洁ଝ尙䠒⸱䁂墛Ⴍ㚟緗߯炖掟岻Ḟϣޥ䰐܍忥嶹烨Ƌ䎡壠\u0CDF朠㞐䐁竽暸―0ӠU琨䇚 㡴ᴫ㢀ೠᗠ㹐ခ⇽ᯚ\u082F怼⁗㯟㽺ἀ≀䒓嚢ᒰ㐐咎ዠ䡥瀫ފ䉍Ҁཇ滆䳐䞡\u0AF4枤◠稁䉂䑥耔䠳Ử䑿Ğ癑氬箁ෂ᱀⎠簱ӭϚ栩瀺䉆\u209E侊ᅔ⎐䜑\u0BA2ᦠ㥠傱䌢≦ė瀸灛䒄䣉ᄬ⏤ٚぐ၇䕓̡\u07FD☥㢨䤺剉綼\u08C5渺⊛8\u0C50᷇儯⦡㌣扤炫儿ᷤ摲睢晋䂤䚘நኀ⥨䆱筂縧⒫㠾灝\u245Cླྀ䇈Ḃфണ拄⣨掱¢\u1A9B㢦Լ⇴ᒀ箏ᅜ⊔ݥ\u087Fܴ㹈刾終癥₫⤼䡞傉⃐廰䏒ܵඤᜨ‰拡᪣㔦⟶ᐶ䡏傞⽻䇥⊒ԙ睊ᅴ㋈坡櫃祥檮☄⩄呯佒冯̺童眈Ꮣ囈盡滂൦窪焎⩔炒୳ำ͎ڭઁ昐⟈擱ޢ¤#擭\u0B58ጳ厀拱䘽䭤嚩搰 啔⎈嫡罜䎙⍢䰶ᩐ呤椌䅇⋶䙜ེ᧔㇏ቁ½ጥ皯洹毼䢃⣬䅧∾䠜ચᴔ☈綮㟃䢥⺬紺\u0BFB璁椟內⏞碍ฦᰨ\u2430䪶ᇙ捚↬䈾ᩎ瑿纊⇕ᴮҵ㗄ᰬ㎈獁゜敘畀爷᯾\u0FDD⢿凥⊥礃ઔቈ㑰十抂ᄛ泑ց柵\u0889჻凗ɣւࣴწ❰獁嚝ⓙ⣶稱♍\u0878⽀懙䌍\u05CAಔፏ徸毁ॣ窦ഩ\u0B3A䱂梜Ὢ件䈠ȟ㌮ط䱁梀僉㼒揥Ӧ\u0FE7悸㏰慰䶜ᥚ紨娿ᑟ⿆壭ㅙ䋅䓖\u0FF6ᝏ勿\u08C1宜!㺹秦ྫྷᛏ勷㊁痢⚧ⶪ栋埯保夛ㅙ䌓\u07BB甯滸\u32FF⤞汼ỦᎨ笳癗汥烠燴峳Үࠠပ乌\u1C98㣆慢簠ɇ\u0BAD᠃๘㿇ト繩戫㤛ဌቻ傐巎㺌⎤漮嶉\u1C4A㡯㾞绥挫䗖ཎᑸㅘ匁䔝\u2BA5殬ܵ㱀㯈炻戓簠ǜ➬丿屁羷㤎爓䍯؏睲ᵟ嶰倁爃Ꮵ➪砎湁翃罁继䌣㩡ਫ਼Ά妘䬑Ỽ㊥Ⴋ眍屑涴磍爄渏㪚瀠֨繗㱯圷滸⏠䔿\u0E5Eᯟ䃗ᰱᐝ\u0865Ⴈ)䐡ᩤⅨ䐎栃俤‒ሁ織ѷ碧ᇡ巄䑗瀡ᐘ㠤䠩涌摧䢬Ȉ繗ᷗ䣯燿扻婀䲂ᬸࠐ䬱ళ汦ߒ儷㉖ɷ礕皤Ꮇ㤖Ơ᥀㺠堩篼幧\u2068缷Ŋ䎽ҡᆷ岸⛯ㆁᮛ䶤硱ᠲ㑆桫缹\u0DF8⺽Ý盨ᶔ䛾瀠ޕ䩝䊖ࣱतገ♨䶃抹䋯‾Բ䉆徑\u08B2兌㦺䓭ᅖ͔✥\u0FDD杂❤墱┲硇灬ᢾᅆ\u2BAF〫၎䷲䒥\u08D1ᦂⱀ炩攳㙄獓礹㗠ᑦ䡳ਉ⋥様䱱ᨂ⛤䎱㽂䯘抩㢼䇢⺥䓰䦚ቘ\u2438䲩᠒又䷱ᘽݧ溪焀4䵔⧏㭱䉌\u2E9A㺮䌰ᡗ璟棧儣⋺䞡㛫扔㣄啑㯂罤ƨ࿂橐㑰棢䧮㴊⟴业ᣲ⧄䭱ႃ祇ᬓ䴺犡㒇⓬ㇽኚ☣\u0866\u1AF2Ⲁ厮巂ᵅ㪪猺⥘和ᣩ䥠拖䙽\u0F98ᇲ㒄嬮檃൦憨㴾⩐䲉愓䅚䢪➌䣩ᦲ㾄䧱䎳ࣦ庫┆寴牶擝儧⌎䠌䧛悬㜄煑\u19DDཅ㙬渂⥉㉯Ⓦ↧ȩ䘳ེᳬ㠿⥑唠哙ṫ粿⥂㑠l嵑㦂⊥䔯ʾ\u07FCಝ⺈⧟ቾ碃৹ᵬ㸴䅉䑲ʦ\u052Aᬼ晈ⱪ夀㙐剩܂䳶ᝐ†ģᓚ⦤助⛴睴ቪ┴䋑岜ᄙ֯ᬵՋઓ何㈊拸㫜眮\u19CCㆴ嵉უ凥奮⊰摓䩾僸我䎵䚂䱵涌ぴ云ᴂ⋆䕬䜷㑁梌壉㸠Đ⊥Ꭺ笽䟥\u1C8D㣄憵䍙竊侗滊❘棉ः㚦㮪䪻๖\u2FDDც燦剳䛇ਾᮋ堻ゑⴝ㛇浨㬸⿳岇㣒爁絛ܺ䤖᰻䳴媑ὲ糺㎫༾啊㱶哄⼍扣☷ཏ揼♟㇉欂ϧஐ㠎乏羸碻懗挏䝮ாᱼ㯘䀠俊榧抯䕦倎ၤ⩗㬁瘂᯦䭪漳濲ɼ碿⺁糋▘䬞መ⧔稑濢⡆偭\u20BE浂㲝眨ॽ剠ل倍ᒜゔ愑旼៦䝫ຸŕѴ磌৳剿䝸仁᧢㠘綩ൂ䁅坨悹䅖篖甇燛北⠀䯁ᒚ㖔権右婅摮亻䅚穽उሐ⊸䝑ೃ椚∗㱩ヂ珇䉩庸㵁䉺睞ग़⋸䞸䤩ᢴ⪤尩恒\u2BE6䃨༎͕τങৰ㊀斤䨣\u1AC2㻤甡尢䅧ȯᄶ煟穦೬ᦐ㏐柉࿇⪹嵈吹≒\u0087循Ⓓ慎扥ഉḺ↗壤䩃Ầ㛨昹㟳撆ޯ碽戦㷛ಮᨎ\u137F▱䪃ၤ⍯\u2069㣃䊄䓭䆸捇抝䓴䇸券㠠ݩᒾᥟ㉿㶌䧺㏔晴䡈ᡰ㷄犹㨼檅㩮璹升⚎䳣䇃ϊ✭\u0B59ᩔ⓬壩㍓筄Ⳬᴵ⥛秗₱䨓\u12B6䗢䪓ᬬ◬嫩㝓̈́\u1777䴷㍔㑫擿ᦞ㍌昼ບᵒ㮈泩\u0882哆哯粶⥊牴ⴅ䅧ፚҌ\u086Bᒆ╬䎹㱲\u0D45\u2E6Aᴻ䥈౬\u2CF6䤻ኅڥ䷊᮶⟬䩹網㝆湯夊\u07FC噧Ⴧ䥾㎬杵䱹ဪヌ湑ዓࣥ㹮粴濲嚃ݼ妙㉎⛜䶦Ἤ㵌桉び䳤冨嬴᭘㙧ⴜ奫ጩ◣ๅῊヴ䡉痂ě䅩㌶㹢沅ᓶ奵㊶断䪈ც㍀獉ᙳ㕚\u0528涹孕\u0A79ᔐ㻢扤ఒ䱫ቖ⇬浉煲䳇ᥩ䬳㭃㚌唃ㅌ䊙❝䱛\u1AF6ㆧⷉᘽ⋆㻬嶻啗白沠㺩掕ֺ䶵泊\u243C䱉孲盆⇬㊼敐屬\u1CB9㻗㌭♦䵕፼⋷⒎漡㛙\u0EEE㨎䝃\u0E98㤑⨐右▦ଯ澮㉔抑㍲┇᭩窱楗梟唔㦡刹╂睭ᱼⳘ䶉⫳竇嵬䚳ൄ翚㤏慼獧߃乭ᗊ\u2FDFÙ䳳䌄歫ஷ湇沞㔖㦪崟笠仼Ꮌ⊔緑睼၆簗⚰#窎\u245F\u0AD9⫲灅箮圶㹊༵ល槠ቇ╯\u08DDጜ㏘秙厒圅惬⺹坘㪑瓕椳删Ͻϫ㮲㝓湠;徉秲\u0BC4ཪີޢ羳崇㧅剣暧䮗ᬛ䉜暙ْ灅揪ຼ䅍㮺甀स፳攧佝ᆚ㐯ᚙᤒφ潨㢼ⅆ\u0081㴕榄⍳旁亝ጚ⍜䒙紲撄㽮眏佐㯁㳳槭玤晟\u0A43榾⏜喙桃帄⑭瞷淵ɴ䂡祡犄旨䷃楾⠬砑䨓㘅毫Ʒ緻➳㲶礼㋘擞煿\u1ADE㱬䒹⿲線㏓䔿䁁㺂糅礵犯旡䯢Ở㪠䘙橒㠆濯㞶\u1F58繩紜槰⤢䗑䦑\u171Eℜ缎簒抄ᓮㆲ爿ښ⣠Ճ獟斁伟ᦠ₧আ⅒㠄恍䁺Î䉲䲸⥇㋦撉亓ᆆ◬曱絒㕅ۮ恳䃂\u169Eʢ֡㐘㮶凊ᨮ㷌榹嫓罤⡎ᖺ⭀偧䳪姎㷐ᜌ乹ឡ㼢哹歒熄\u08D7剢歈皛Ⳝ姼ఎ䛅倓ᆶ㽄仱⡲ᰵ䑌挰Շ劄ⲽᥳ㈺斵砑氜Ѣ凹㟒䨶ᡊぽ\u10C6Ⅳ䞆夼\u0BBC稈ⱹᄶモ煹䖣厄粒㖺ўⅥᴓԶ୮斕䪰勡⻢繁晢΄⫮㡶筁暙ᄗ֏ሠćᘨ涽᭗㚉ᣡ姮\u0A7Cᒭ乻ዖ₴䀠沴䗕ᱢោ\u2FE7ᨪ㞌䑉緒糆扈Ѳ㍇炍᳢⥉લោ⣨微㽴緹ߓ㳄砤⬏䣀ᅤ\u1CAF姥੪឴⣈微⁸惑壢欘砷呿烍癠\u007F䃥᪒ⴶ५㑲\u08D2兵⋄熨犋䙆ो䟱⊼穙憪⌵\u31EE掾\u17FC冄挝⦤獝◓䩬\u1771☌叩⪓䬴噊綷❔ᩨ⋫㦳ஹ攌ⲷṮ⤌啙ⴃ盇䕨ஸ\u2E5C溅岵㥻\u0BCEᓇિ汚⼂囩䀲唆澯ܰ'稩㨘䃙侒圇ⷪ籷\u0C5EҒ甍㤥狷⠋䢽ᘩ∤䢉侒弇㷯嚻㝏᩿岹\u2E4B劕昷䦗ᅜ㠲䘉ྒྷႴ嗨㾺㹈㩬坧㨕犩ᜧ䭯ᆚ⌲晅\u1AF2濆揩垷㗩Ẓ㳉煅爠˽巬䞺\u1F5C㺄ࢤধ刼♇䱄忾゠䦙ℒ粴Վ溸㽔㺇粧礫劋杯䦿ᵉ♲罅祪શ⽮|哄⥸ʤᦻ猱ᖮ乏ቾ♬焙ͫڶ埨侻㵚㹣㴙秷犄撟䥿\u1AC9㸢䈥卪ϙ坨娊䉕Ṫ˅◢䬍ᚚ⪿ḉ\u2453☙ᄒ瀅嵏㩶ፉ繷璷禑≽ᙏ䨬尹㶲漹–₅⣨}ೕᥪ匔\u0558㏼ޏ䧬堹⛲沅祒橇⾢\u2072ⳅ\u197E㌜敊ቐᔔ\u08C7ἱ\u2BB8渥唫䰷㉨ಳ㍐暋\u2CF7Բੴᔵ䤌嶲♌儥ໂ縶瑉㙺㭍妀犮旌\u0AF8ᗁㇰ唒㇂繹槪㎴⡍岽ᇼ埖䊮\u05EB䫇ᐸⶼ冶ⓧ㱹\u18AB䶴ⱏ癸⭙䆈ᓱ䆒੯ᑮ⦌吱⃢嬅ᵒ帴⺨㲻㳗妃⊠9फօ\u1F4E䶲ل䱻猑斗䰂ឞⳙሁ㭬者⊂㸵曯㹴峀冇狐ᔨ卪ᚴ⽨巆≂摥姓ྙ䧬\u2E7Fˀᆂૄᖔ叒Ҵⵢ凑㍂灑沪ᣇ癋ᡷ\u08C4㙡૯数牨喴⭂妱⦸䵥平Ǥᩌ౸䋓皉挄䘃ୖᙒखᳬ\u2E50叹\u1C4B⤄㽉පᣘ\u318F૫娃\u0A46ᕼ⺸娯䉼糥Ϋܷ啮䥼壑䕸抡ᖵઁᔛિ濑≪囩▫剷涮楰;熅ヘ孥\u1AF2穷䗬窻㣍╧Ⓝ䗧ୡ៙⫒娅㘂䕅憒ᮇ瓊⠃磓䖂拙㥠䯼嚢ⴒ嶅㯪秙⡫ၧ糋扰䓀㮧Ⓞ◨䭫▥⢡ᖅ⩊瑵Ⱬ墵⍭䉿ૄ㩹動椠Ǿ⢶兎亹䫕䥭㓵▖犉ᗲⱊ噚ㆲ峅䅪ᢵ⏮\u0BBA哔⥸勁楼䫅ᑒ⸄媾㝲眉Ⅻ䆷嫍枷睛喝⬓╷玅ᜊ\u2BF4夜㓼狅懋啷埭垿Ⓥ⦌務╓⨵ᑍⱔ剕㦊揅❫纴界海擛㖂櫎稑䭶噭ⱚ倹㗲槵㈒ặ്Ᾱ㫐ɿ勉啱䩳ᗽ⤬夕⾬矅䵪⽷ʬ婳㽎畧㲧嘑䭭ឆ⠦岹㜊槵〓彵㵎\u20BCⳉ\u197B糅禿\u2BF3០⬬庄ᄺ璹棫䃵凋䍱ᅐ籄̌㕈\u0B91唑佦妋匢帵໓᥄告\u0E7C糟♡䲩䇘⩚❎⩢喁⺺焅痂\u0CF5䝎㍻⋗ᚂଐᘍ䯷ᒮ⾼卡⸪䭕籊˷ᑈ%䢢御⤄䅡↲临䃋兵ᛗ璌嫾ע欇ᚈ⺑泍㉪对穊Ä㗊\u0B7C糊禌䌃无Ⱅ堓⡂哒㩺巕㦋ၵ䷍㹶硓俟⊣㖦殪䗻\u2E56嶁⋬者䛒ิ巍獷曅⚝\u0AE4ᖾ氍品⩨彍⨪罹ᒃ殥䱈ᑲ໔ᶖ\u20CF㖱殸咤⻩ᆥ⭂嘵汋燵巏㭿⋁䪔嬄䕈\u2B94喇\u2E9Aᔱ⌴底叫ዴ\u08C9㑺⻍冄抪痛㐃禩⩒嗥㝌僥㬋⌴剋\u177F㋄█䫾䗮⩅嚗⺲句㒂籉祋ᬷ൫Ёᝏㅰ⪺疺⪦ᛩⱅᆅ⪚抵┃ệ搔\u1977磞煮㣅産ૢ圢⪅ᄵ\u2D9A涵筊\u0E74ೊ個廝▞竅畧殡ᚿ⣒冩⨲查䰊熤繉彲㋖橱⫿痟樿啟⦞匵\u2E4A栕䞒䞅䝪朷经絩ዃ症橹ᓥ⬞ሊ⽊栭柋䁅₍罵Ṉ經磯ᕌ䭊坻䫄宩Ⲋ䅵㼓؇傋畺Ӟ⦚⬇瘐\u1AFA坵ⴊ哚㚲擅ሻ\u2455翎䵰\u1AD9籫እ嘘\u1AC9៰欺哣《䬭⋋歷່烿\u1ADB㖛ኮ╀㐘㑗䩏\u1715⤊曵悊ሄ䛍烹块幬䛦禒ᩣ朽⾴壃㷊展䳪㵵⻌棱嫘疂䚩ශ䬜➚ⱏ᠉㙲䀠汻\u0D53⫼♈橱儞⋦䫵䋋ჴ䏯壻㇑㺎䛷บ\u1AC7攕⩬吾㯦侭ᥒ晗粏捹㇐䦕㌀凁\u1AE2㜩䢩偹㦬硕⊋ㄦ䊍磷燂奮ನ։ᮢ҄漩喳⇒抹䮊\u23F4䛨/䟂帽\u2BFA䧕痫峴秉湶筒卵⛬䅾氍唛⡜彳ⱺ業㊺⪇᪈ᅲ仍厑媮㗠捧ប渆傥㥚僭憺䎴ᗌ湷ᩚ䕭㫵㕵ᯆ㚋⺮墬⤒滕權囵Ꮝݺ姖厃嫙䵓፬㩌楶嶍㺪瓭澋឴彌\u0870৵浲朓䴤\u2B4E㠝२嶓⻢怵䄊৶毈峴᧒ᵶ嬌畐⬎㔧ⲹ埓ⷆ媕犫桷畯繾Ռ取暽䷁欰圜沮壽㜌䁍夊䏵㺎糾Ỏ浩㫾畈寨垄Ɐ毪⓰纕伋⇷毋拿䗉亐䬂ㅞ扳笒挞嗒も夕爊ೇ\u196F❵◖癬笀㺮Ⱍ䖐҅剝ℚ栅偫⬴⋏⽽◉Η拲㤢孉文࡞墝㘦吕琊៶㟊䫽淠\u2B66䪣甠ᬠ㖅⤘圝㕶嘕畺ଶ嫯⪾䇟綂嚨啔ᭉ᠙\u2EFE嶝⒔砭吲曗䶋ճ嗇斍ڼ唺猁\u16F2⡊怑㣊反浺㡕ⶊ啰罙\u1C81ۛⶦᬽ㖺椯ἅ㝲悍ᛋᒴ碏\u0A7E\u1AC7楣㳈渝⭖坰樺妾㮦俙ᔻ灧ᎈࣶᵊΐ䜃啹宵ᝈ梔嵃♦瀕痊♔ᒎͱⷊ⍤㚧槦ᨼ㞈溑坣⌊䓵夻ᩗ㲏㛴⧔≷㜆ඃ厨㬖洩彻⇖羭⇺⧗l磿\u09DE捬檮浳媬┤栦彛㻦䜹◻套ᣭ\u2EF7ঢ়㮆皡渏匫ᓤ檳ᾳ㾖儹ⷻ䕗䁌捷䧛⟚皤䴶ፏ㟡䡂ṍ⧆祭矻絖⺈歱曓䵷⚪弗\u1AC9咞澝唓㨆吅㞺捗美㍰\u08C8ݨ༇䵟婖㚫⪟'〮瀽Ά答䚈⇻ᩁ㍼ᛆⶏ孡㟁涹喙㎆洶厺囶儍䇴ᧀ率\u1AE3旧ᩨ甁渉幧⊮孭ົ㻵㱍凼䑞䞐㬞䵬㭳囜瀙咒〶偍䚺\u05F7焍燺⣟䝥\u0AF8疯㵘眧⡥娙㪆牍㑋᧵\u218C慵Ꮡ碩ᚧ䷛\u1AF0疩泅僅㔶撽ᴊ柨㔋\u1CF7Ꮮ❮亮\u2D9A㨩㞢䩅峠壮嗕⼋擕楩拿Ք䖇ᛎⷶ㨢呒湕啭㍪掽厪ᙴⴏ㑱ᗆ䭯ᛣⶶ%㕩⩾墥⋶枵煻䆖䎋⫹㗦\u2B6A䴙\u2D2A婽㕊潈垅㹎䉽䍺䦗㴎罺䯀ᮅ囗ⵂ琕㓓丅夻孼玥ᴂ仕\u0ACAᗷා͵⬗疩籰㘖炕堢㛎嵽ᣚ噵Ύ۱痓殘㛖州孃㠚濋寷❖綉㳊䑗ᮌ痴㻏䦑⼘渎媍杦䭞ၻ㕖媍ⴺ᧖㎋ࣺὂ㝠瓜渊嫋㘚湍彃㝲䭵淋ු垫ⷾ密孩㲥淅嬯㒕⪟ᒗ㤎嶭㻺Ꮥ滌盲巛孱㳗布嬷㙝淿浛ブ洍吼瞗垈ϸ㷍捺䚱洶㵳柾殍劕⨖搙濛䏕垈\u23F8䟙ྔị\u0DC8ጂ䒃桩喯㲺箭昻⟕澈援燊ཱẸ㷑\u1AEF㑃棇奛㮖䰍摚\u23F5ĉ䇵線ⵯ盿洰㪰畡淫\u1AEF␖妽㡚ᢕ㧎珵ਹ佼⌞㷭\u1BF9璓渷剏ⷄ佝䫒ᕗ稉燳⧖⽼廌ᷡ㋥瑬氇嫯╾緕㖚͇ཏ௱ៗ⾂\u1716㶔欝咋毃岏⸖䃝㶚粕床ᇻ㧘\u169Fᚻ㵪笸瘫橥唽ㇾ䂽ċٹ洍懾矔❸ᜑᴲ㮻咜沛怿↮䟝供暔\u218Fᝰ㏐➚伃㷔⬍瞉毖壇▪䪽䔚ሕ㴎˷䏘ᾔ媦疎㯣盲澬ᮇ╞䁥ݛ䈖崊柽◀㶜ポ䗎箧坒滏崷㠲巕ㅺ熔▍佻䯙徑䫡\u2D71㮥㟅欏圇Ⳏ圕㭺⸔匏䫿\u2BCA斘拲帑㩇町渾坋⚞洝淛⿴涍嫹\u2BCC\u0382拹巽㪚璵檋峷㮞幽䓋恖㬊痲῁垎囅崦娧䘭涋\u1257㒎睽尻奶ႈ◰懎䦐漇綆嬆瑍模執⛼犑䏳垕\u0B8D嗽ෘ६檻絼\u1ADE畽榛尟㜎翽壺旖䟫䁔浌ĘƳ嵁᮰\u0FEA⧻媕㴡䒹Ц桖墏䏿᷒྇滤涊Ꭸರἧ儯❦牝凊㽷ᮋ϶₁ལǪ㷠箝ញ猑徯ⷖ慝⸧㤗ȩ恟䟃䄅Ǧᦢݡ盳棧噀悡牭⟊ṗ劋灘緒佴✂㵉媉瑹癰㝂ゾ媣穛漗税䯿ϙ璈廍㶪窕癫橐㱏〖奄熚خ娊塑㟔㎂\u0EFBϜ笚㙫潉嵏♾愽朧䒖嬒䟿ろ侙廇Τ搆㒱檯匁⏡敭䜦\u07B6䅪寲矂ქ䇳㷑穣盤᪗吶┆扣眧䬕丌峺\u0FDD㍵狄緩篕瞄ᦣ宰毾䏝压⪔␎ᑜᏒ⾚㺬緥筁㝢歳帷ⷾ剣䙚礮氌姶䗗ύ∜緅ߘ瓄ṏ坿╮䮕猚稖ㆍᑐ䗋澜䫫ᵼ婢睒ࠪ埑≁履楊\u1F16䅌矺ῌ㽲㣾紿毅㠅梶彋㷞䞵\u0CDA㬮▏汓必徊⻔巇ݯ埯棾徟◎猝ț亘࿋罰绑絳窭続媍㙟棇ᦟ㶞磽\u1CCB斖猎竾㿉羀ᇹ締䙟甅澯朩㰞叩䐦偖㸻䃻毄᭮⼉嵣㮼笽榟嶟㮎秙囊䀯ㅌჿ䷒⥲⫌ϕ㮰ೠṴ埗㙊䇽䫛䐬‿ၒ䂐䣼䇼渋獐ರὡ剀繆䳵簦ផ⠿墵撃坯ᇾ⎜䞄࿒Ώ崗⚎椣儦䤖ྊこ罆䃼凞̪䘹瓰䯭嘉ユ屝照矖┸\u2D7C巘箂䇘㶜࠘\u0E90ᵧ卛䀊璣瘧檭┾㻼悄棶䇕⎖䜥๓沔㚯⥡繝ᔦ᪬ᴿ暿傃僰ຸ䏑ڌྐ(㠸搖炃䞛⌗ᰏᡘᢁ愐↿͆ࠅ矪䜬㼸桑抃㒧欔痈\u2457もᤄ\u31EC捞篓\u0EFC欬㞸絑䆃䋦\u052E簾㯺ⲛ⽴⇯ϲ篝甶᱀廧㛑捭竦⤭䔊晓ୈ償∖捱ݛ๔\u1CC8㢓㟁乥䤚演㬿㏴梙❫⇄綝䝯ᐮὈ㏇\u2FDA皝᧓㓗縈乑炅⾗∙䌸⩜疰\u128F嫐网眃䃛㮮㹈秲❞㣢爚䐍竝垌ᴸ㿋⾁掝羂ᆗ⠎\u2BF0㿌継懇䏇篝㝼\u1B7F刘琑穥ؚଯ㼼俽䒕嚔䫟縋窞༙⭋在树扃⏒催ဏ\u0DFDӂ焓憽細䝺杁ῤと愦䠝\u0ADA栗䂹扝撏ࣽ憻縘♩ཀ橕儨眱吳堛᱔ᎎUᯌ䤍৪⎆婁琠ᬁ埗▼栣Чᶗ堼ῶ撍椂ۍ⎬䜬㕲ᦰ㟩㡩緃⟚瑄䔸煖咙捫䚼禂✜╒ṍ嚈痱䢳䤺㢄㒺⁒璑䃺䨕̀箨㧐\u1AB2\u3040狱䂭愦窯格奜㒓椉凣Ꮚی代槔㿈筩禍礦ậⴻ滿ᒗℜ䧒捱ۃඋ\u2D2C㬸漒綃Ⴧⲗጹ♖沂ᔐ⧪捵䙼\u0DE5\u1CB2头煉瑳櫧㒔劻慶䪝ᓨ燬匹ࠜഥᡪ㞴窮档碧奮Џ噐沋筳㇐縋Ε嗆Ị㣸滉䙳㻒㔮樾幒⪁➞⇖叅笝皕ᢼ峴盉涝᧧唬ں幑糚唀熽搄⪓甒棼㝸犑坣㦦䷵场\u2C5F\u0FFC\u0C76∉卋❶乴⽺ᣐ矞昃烆䍬㬸矷㲖ភ㻟ᗛ\u07B4営\u0E7F傽㻑䠃\u2BC7搖ግ繚碝ㄟἈጯٵ㟡ᬢ㴁㾞偃␛坮䂹兜⊋㉮\u0A12ጭ㫁᠀潢イ绶䁃\u0087㡭怏㿺Ғ烱ধⴰ柢嗂ᰠ㱨甹籃系烬䢾≑碌㞒ᧅ巑\u2BEA璀ᱯᐟ㑱撳秺,Թ煛抝⒗⪺㌢☱甩ᠴ㚠測徑祇籭ᶋ⩓ኇ┝ᇮ␜柰㖉Ṳ㑈抬壓⠺婮㲾獚劆⤝⫈涒暬䵋ḽ婤瞡羳⢛䙬戊䡚悖擫ማ\u135B媽໐ᵶ㌄懩澳愧曮紺Ỳ㚑攓刍㏖晓 ਠ ")
                    .getBytes("ISO-8859-1");
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
//                    c2 = cr << shift2 | cb << shift1 | y;
//                    if (paletteMapping[c2] == 0) {
//                        yf = (float) y / yLim;
//                        dist = Double.POSITIVE_INFINITY;
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
        for (int i = 1; i < 256; i++) {
            int rev = reverse[i], y = rev & yLim, match = i;
//            yf = lumas[i];
            cbf = cbs[i];
            crf = crs[i];
            ramps[i][1] = (byte)i;//Color.rgba8888(DAWNBRINGER_AURORA[i]);
            ramps[i][0] = 15;//0xFFFFFFFF; // white
            ramps[i][2] = 1;//0x010101FF; // black
            ramps[i][3] = 1;//0x010101FF; // black
            for (int yy = y + 2, rr = rev + 2; yy <= yLim; yy++, rr++) {
                if ((paletteMapping[rr] & 255) != i) {
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
                if ((paletteMapping[rr] & 255) != i) {
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
                    if ((paletteMapping[rr] & 255) != match) {
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
        System.out.println("byte[][] RAMPS2 = new byte[][]{");
        for (int i = 0; i < 256; i++) {
            System.out.println(
                      "{ " + ramps[i][0]
                    + ", " + ramps[i][1]
                    + ", " + ramps[i][2]
                    + ", " + ramps[i][3] + " },"
            );
        }
        System.out.println("};");

        System.out.println("int[][] RAMP_VALUES2 = new int[][]{");
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
        return (y1 - y2) * (y1 - y2) + ((cb1 - cb2) * (cb1 - cb2) + (cr1 - cr2) * (cr1 - cr2)) * 0.25;
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