package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.Maker;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.OrderedMap;

import static squidpony.StringKit.safeSubstring;

/**
 * Created by Tommy Ettinger on 12/27/2016.
 */
public class ColorTest extends ApplicationAdapter {
    /**
     * In number of cells
     */
    private static int gridWidth = 128;
//    private static int gridWidth = 140;
    /**
     * In number of cells
     */
    private static int gridHeight = 12;
//    private static int gridHeight = 27;

    /**
     * The pixel width of a cell
     */
    private static int cellWidth = 10;
    /**
     * The pixel height of a cell
     */
    private static int cellHeight = 25;

    private static int totalWidth = gridWidth * cellWidth, totalHeight = gridHeight * cellHeight;

    private Stage stage;
    private SpriteBatch batch;
    private Viewport viewport;
    private TextCellFactory tcf;
    private SquidLayers display;
    private int hh = 0;
    private int vv = 0;
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
    @Override
    public void create() {
        batch = new SpriteBatch();
        tcf = DefaultResources.getCrispSlabFont();//.width(cellWidth).height(cellHeight).initBySize();
        viewport = new StretchViewport(totalWidth, totalHeight);
        display = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight, tcf).setTextSize(cellWidth + 1f, cellHeight + 1f);
        stage = new Stage(viewport, batch);
        SquidColorCenter scc = DefaultResources.getSCC();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.Q || keycode == Input.Keys.ESCAPE)
                    Gdx.app.exit();
                return true;
            }
        });
        Gdx.graphics.setTitle("SquidLib Demo: Colors");
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
        for (int i = 15; i >= 8; i--) {
            SColor.colorFromFloat(tmp, SColor.floatGetHSV(0, 0, i / 15f, 1f));
            System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
        }
        System.out.println();
        for (int i = 7; i >= 0; i--) {
            SColor.colorFromFloat(tmp, SColor.floatGetHSV(0, 0, i / 15f, 1f));
            System.out.print("0x" + StringKit.hex(Color.rgba8888(tmp) | 1) + ", ");
        }
        System.out.println();
        OrderedMap<String, Float> smallHues = Maker.makeOM(
                "Red", 0.03125f * 0.5f,
                "Apricot", 0.03125f * 3.05f,
                "Yellow", 0.03125f * 5.3f,
                "Green", 0.03125f * 10.5f,
                "Cyan", 0.03125f * 15.85f,
                "Blue", 0.03125f * 19f,
                "Violet", 0.03125f * 23.5f,
                "Magenta", 0.03125f * 29f);
        for (int i = 0; i < 8; i++) {
            float hue = smallHues.getAt(i);
            if(i == 2)
            {
                show(hue, 0.325f, 1f);
                show(hue, 0.65f, 1f);
                show(hue, 0.775f, 0.95f);
                show(hue, 0.875f, 0.875f);
                show(hue, 0.95f, 0.775f);
                show(hue, 1f, 0.675f);
            }
            else if (i != 4) {
                show(hue, 0.5f, 1f);
                show(hue, 0.725f, 1f);
                show(hue, 0.85f, 0.925f);
                show(hue, 0.95f, 0.8f);
                show(hue, 1f, 0.65f);
                show(hue, 1f, 0.45f);
//                show(hue, 0.6f, 1f);
//                show(hue, 0.725f, 1f);
//                show(hue, 0.85f, 0.95f);
//                show(hue, 0.925f, 0.9f);
//                show(hue, 0.95f, 0.8f);
//                show(hue, 1f, 0.7f);
//                show(hue, 1f, 0.6f);
//                show(hue, 1f, 0.5f);
            }
            else
            {
                float hue2 = 0.03125f * 12f; // green with slightly more blue
                show(hue2, 0.3f, 0.8f);
                show(hue2, 0.575f, 0.75f);
                show(hue2, 0.7f, 0.675f);
                show(hue2, 0.8f, 0.575f);
                show(hue2, 0.875f, 0.475f);
                show(hue2, 0.925f, 0.35f);
            }
            if(i == 0)
            {
                float hue2 = 0.03125f * 1.75f;
                show(hue2, 0.3f, 1f);
                show(hue2, 0.375f, 0.925f);
                show(hue2, 0.475f, 0.8f);
                show(hue2, 0.55f, 0.65f);
                show(hue2, 0.625f, 0.475f);
                show(hue2, 0.7f, 0.3f);
            }
            else if (i != 2) {
                show(hue, 0.25f, 1f);
                show(hue, 0.325f, 0.925f);
                show(hue, 0.425f, 0.8f);
                show(hue, 0.5f, 0.65f);
                show(hue, 0.575f, 0.475f);
                show(hue, 0.65f, 0.3f);

//                show(hue, 0.3f, 1f);
//                show(hue, 0.35f, 0.925f);
//                show(hue, 0.4f, 0.85f);
//                show(hue, 0.45f, 0.775f);
//                show(hue, 0.5f, 0.675f);
//                show(hue, 0.55f, 0.55f);
//                show(hue, 0.6f, 0.425f);
//                show(hue, 0.65f, 0.3f);
            }
            else
            {
                float hue2 = 0.03125f * 3.2f; // between orange and yellow
                show(hue2, 0.175f, 0.975f);
                show(hue2, 0.35f, 0.95f);
                show(hue2, 0.5f, 0.875f);
                show(hue2, 0.625f, 0.75f);
                show(hue2, 0.725f, 0.6f);
                show(hue2, 0.8f, 0.425f);
            }
        }
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
        String templateFull = "/**\n" +
            "* This color constant \"Name\" has RGB code {@code 0xFEDCBA}, red `RED, green `GREEN, blue `BLUE, alpha 1, hue `HUE, saturation `SAT, and value `VAL.\n" +
            "* It can be represented as a packed float with the constant {@code `PACKEDF}.\n" +
            "* <pre>\n" +
            "* <font style='background-color: #FEDCBA;>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FEDCBA; color: #000000'>&nbsp;@&nbsp;</font>\n" +
            "* <font style='background-color: #FEDCBA;>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #FEDCBA; color: #888888'>&nbsp;@&nbsp;</font>\n" +
            "* <font style='background-color: #FEDCBA;>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FEDCBA; color: #ffffff'>&nbsp;@&nbsp;</font>\n" +
            "* </pre>\n" +
//            "* <br>\n" +
//            "* <font style='background-color: #ff0000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00ff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000ff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #964b00; color: #000000'>&nbsp;&nbsp;&nbsp;</font>\n" +
//            "* <font style='background-color: #ff0000; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #ffff00; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #00ff00; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #0000ff; color: #FEDCBA'>&nbsp;@&nbsp;</font><font style='background-color: #964b00; color: #FEDCBA'>&nbsp;@&nbsp;</font>\n" +
//            "* <font style='background-color: #ff0000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00ff00; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000ff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #964b00; color: #000000'>&nbsp;&nbsp;&nbsp;</font></pre>\n" +
            "*/\n" +
        "public static final SColor NAME = new SColor(0xFEDCBA, \"Name\");\n\n";
        String data = Gdx.files.classpath("ColorData.txt").readString();
        String[] lines = StringKit.split(data, "\n"), rec = new String[3];
        Color c = new Color();
        StringBuilder sb = new StringBuilder(100000);
        for (int i = 0; i < lines.length; i++) {
            tabSplit(rec, lines[i]);
            Color.argb8888ToColor(c, Integer.parseInt(rec[1], 16) | 0xFF000000);
            sb.append(templateFull.replace("Name", rec[2])
                    .replace("NAME", rec[0])
                    .replace("FEDCBA", rec[1].toUpperCase())
                    .replace("`RED", Float.toString(c.r))
                    .replace("`GREEN", Float.toString(c.g))
                    .replace("`BLUE", Float.toString(c.b))
                    .replace("`HUE", Float.toString(scc.getHue(c)))
                    .replace("`SAT", Float.toString(scc.getSaturation(c)))
                    .replace("`VAL", Float.toString(scc.getValue(c)))
                    .replace("`PACKED", Float.toHexString(c.toFloatBits()))
            );
        }
        Gdx.files.local("ColorOutput.txt").writeString(sb.toString(), false);
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