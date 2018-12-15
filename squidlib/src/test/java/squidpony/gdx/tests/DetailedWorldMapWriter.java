package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.Thesaurus;
import squidpony.squidgrid.gui.gdx.PNG8;
import squidpony.squidgrid.gui.gdx.PaletteReducer;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 */
public class DetailedWorldMapWriter extends ApplicationAdapter {
    public static final int
        Desert                 = 0 ,
        Savanna                = 1 ,
        TropicalRainforest     = 2 ,
        Grassland              = 3 ,
        Woodland               = 4 ,
        SeasonalForest         = 5 ,
        TemperateRainforest    = 6 ,
        BorealForest           = 7 ,
        Tundra                 = 8 ,
        Ice                    = 9 ,
        Beach                  = 10,
        Rocky                  = 11,
        River                  = 12,
        Ocean                  = 13,
        Empty                  = 14;

    //private static final int width = 1920, height = 1080;
//    private static final int width = 1024, height = 512; // elliptical, roundSide, hyper
    //private static final int width = 512, height = 256; // mimic, elliptical
    private static final int width = 1000, height = 1000; // space view
    private static final int LIMIT = 10;
    //private static final int width = 256, height = 128;
    //private static final int width = 314 * 4, height = 400;
    //private static final int width = 512, height = 512;

    private SpriteBatch batch;
    OrderedSet<String> adjective = new OrderedSet<>(256), noun = new OrderedSet<>(256);
    private String makeName()
    {
        String a = adjective.randomItem(rng);
        while (a.contains("'"))
            a = adjective.randomItem(rng);
        String b = noun.randomItem(rng);
        while (b.contains("'"))
            b = noun.randomItem(rng);
        final int al = a.length(), bl = b.length();
        final char[] ch = new char[al + bl];
        a.getChars(1, al, ch, 1);
        b.getChars(1, bl, ch, al+1);
        ch[0] = Character.toUpperCase(a.charAt(0));
        ch[al] = Character.toUpperCase(b.charAt(0));
        return String.valueOf(ch);
    }

    //    private FakeLanguageGen lang = FakeLanguageGen.randomLanguage(-1234567890L).removeAccents()
//            .mix(FakeLanguageGen.SIMPLISH, 0.6);
//    private FakeLanguageGen lang = FakeLanguageGen.mixAll(FakeLanguageGen.SIMPLISH, 6.0, FakeLanguageGen.FANTASY_NAME, 5.0, FakeLanguageGen.JAPANESE_ROMANIZED, 2.0);
    //greasedWorld = new GreasedRegion(width, height);
    //private final int voidCount = 7036;
    //private double earthCount, worldCount, intersectionCount;
    //private long[] earthHash = new long[4], worldHash = new long[4];
    //private int[] workingHash = new int[256];
    private Pixmap pm;
    private Texture pt;
    private int counter = 0;
    private int octaveCounter = 250;
//    private Color tempColor = Color.WHITE.cpy();
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    //private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private long ttg = 0; // time to generate
    private WorldMapGenerator world;
    private WorldMapGenerator.DetailedBiomeMapper dbm;

    private PNG8 writer;
    
    // Biome map colors
    private static float baseIce = SColor.ALICE_BLUE.toFloatBits();
    private static float ice = baseIce;
    private static float lightIce = SColor.FLOAT_WHITE;
    private static float desert = SColor.floatGetI(248, 229, 180);
    private static float savanna = SColor.floatGetI(181, 200, 100);
    private static float tropicalRainforest = SColor.floatGetI(66, 123, 25);
    private static float tundra = SColor.floatGetI(151, 175, 159);
    private static float temperateRainforest = SColor.floatGetI(54, 113, 60);
    private static float grassland = SColor.floatGetI(169, 185, 105);
    private static float seasonalForest = SColor.floatGetI(100, 158, 75);
    private static float borealForest = SColor.floatGetI(75, 105, 45);
    private static float woodland = SColor.floatGetI(122, 170, 90);
    private static float rocky = SColor.floatGetI(171, 175, 145);
    private static float beach = SColor.floatGetI(255, 235, 180);
    private static float emptyColor = SColor.DB_INK.toFloatBits();

    // water colors
    private static float baseDeepColor = SColor.floatGetI(0, 42, 88);
    private static float baseShallowColor = SColor.floatGetI(0, 73, 137);
    private static float baseCoastalColor = SColor.lightenFloat(baseShallowColor, 0.3f);
    private static float baseFoamColor = SColor.floatGetI(61,  162, 215);

    private static float deepColor = baseDeepColor;
    private static float shallowColor = baseShallowColor;
    private static float coastalColor = baseCoastalColor;
    private static float foamColor = baseFoamColor;
    
    private static float[] biomeColors = {
            desert,
            savanna,
            tropicalRainforest,
            grassland,
            woodland,
            seasonalForest,
            temperateRainforest,
            borealForest,
            tundra,
            ice,
            beach,
            rocky,
            foamColor,
            deepColor,
            emptyColor
    };

    protected final static float[] BIOME_TABLE = {
            //COLDEST   //COLDER      //COLD               //HOT                     //HOTTER                 //HOTTEST
            Ice+0.7f,   Ice+0.65f,    Grassland+0.9f,      Desert+0.75f,             Desert+0.8f,             Desert+0.85f,            //DRYEST
            Ice+0.6f,   Tundra+0.9f,  Grassland+0.6f,      Grassland+0.3f,           Desert+0.65f,            Desert+0.7f,             //DRYER
            Ice+0.5f,   Tundra+0.7f,  Woodland+0.4f,       Woodland+0.6f,            Savanna+0.8f,           Desert+0.6f,              //DRY
            Ice+0.4f,   Tundra+0.5f,  SeasonalForest+0.3f, SeasonalForest+0.5f,      Savanna+0.6f,            Savanna+0.4f,            //WET
            Ice+0.2f,   Tundra+0.3f,  BorealForest+0.35f,  TemperateRainforest+0.4f, TropicalRainforest+0.6f, Savanna+0.2f,            //WETTER
            Ice+0.0f,   BorealForest, BorealForest+0.15f,  TemperateRainforest+0.2f, TropicalRainforest+0.4f, TropicalRainforest+0.2f, //WETTEST
            Rocky+0.9f, Rocky+0.6f,   Beach+0.4f,          Beach+0.55f,              Beach+0.75f,             Beach+0.9f,              //COASTS
            Ice+0.3f,   River+0.8f,   River+0.7f,          River+0.6f,               River+0.5f,              River+0.4f,              //RIVERS
            Ice+0.2f,   River+0.7f,   River+0.6f,          River+0.5f,               River+0.4f,              River+0.3f,              //LAKES
            Ocean+0.9f, Ocean+0.75f,  Ocean+0.6f,          Ocean+0.45f,              Ocean+0.3f,              Ocean+0.15f,             //OCEANS
            Empty                                                                                                                      //SPACE
    }, BIOME_COLOR_TABLE = new float[61], BIOME_DARK_COLOR_TABLE = new float[61];
    private static final float[] NATION_COLORS = new float[144];
    private static void randomizeColors(long seed)
    {
        float b, diff, alt, hue = NumberTools.randomSignedFloat(seed);
        int bCode;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            bCode = (int)b;
            alt = SColor.toEditedFloat(biomeColors[bCode],
                    hue,
                    NumberTools.randomSignedFloat(seed * 3L + bCode) * 0.45f - 0.1f,
                    NumberTools.randomSignedFloat(seed * 5L + bCode) * 0.5f,
                    0f);
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lightenFloat(alt, diff)
                    : SColor.darkenFloat(alt, -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        float sat = NumberTools.randomSignedFloat(seed * 3L - 1L) * 0.4f,
                value = NumberTools.randomSignedFloat(seed * 5L - 1L) * 0.3f;

        deepColor = SColor.toEditedFloat(baseDeepColor, hue, sat, value, 0f);
        shallowColor = SColor.toEditedFloat(baseShallowColor, hue, sat, value, 0f);
        coastalColor = SColor.toEditedFloat(baseCoastalColor, hue, sat, value, 0f);
        foamColor = SColor.toEditedFloat(baseFoamColor, hue, sat, value, 0f);
        ice = SColor.toEditedFloat(baseIce, hue, sat * 0.3f, value * 0.2f, 0f);
    }

    static {
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = emptyColor;
    }

    private String date, path;
    private final float emphasize(final float a)
    {
        return a * a * (3f - 2f * a);
    }
    private final float extreme(final float a)
    {
        return a * a * a * (a * (a * 6f - 15f) + 10f);
    }

    @Override
    public void create() {
//        earth.perceptualHashQuick(earthHash, workingHash);
//        earthCount = earth.size() - voidCount;
        batch = new SpriteBatch();
        //display = new SquidPanel(width, height, cellWidth, cellHeight);
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        //stage = new Stage(view, batch);
        date = DateFormat.getDateInstance().format(new Date());
        //path = "out/worlds/Sphere " + date + "/";
        //path = "out/worlds/Tiling " + date + "/";
        //path = "out/worlds/AltSphere " + date + "/";
        //path = "out/worlds/Ellipse " + date + "/";
        //path = "out/worlds/Mimic " + date + "/";
        //path = "out/worlds/Dump " + date + "/";
        path = "out/worlds/SpaceView " + date + "/";
        //path = "out/worlds/RoundSide " + date + "/";
//        path = "out/worlds/Hyperellipse " + date + "/";
//        path = "out/worlds/EllipseHammer " + date + "/";
//        path = "out/worlds/SpaceCompare " + date + "/";
//        path = "out/worlds/HyperCompare " + date + "/";
//        path = "out/worlds/EllipseCompare " + date + "/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        //Gdx.files.local(path + "Earth.txt").writeString(StringKit.hex(earthHash), false);

        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pt = new Texture(pm);

        writer = new PNG8((int)(pm.getWidth() * pm.getHeight() * 1.5f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
        writer.palette = new PaletteReducer();
        writer.palette.setDitherStrength(1.75f);
        rng = new StatefulRNG(CrossHash.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date  
        //rng = new StatefulRNG(0L);
        seed = rng.getState();
        ///world = new WorldMapGenerator.SphereMap(seed, width, height, WhirlingNoise.instance, 0.9);
        //world = new WorldMapGenerator.TilingMap(seed, width, height, WhirlingNoise.instance, 1.625);
        //world = new WorldMapGenerator.SphereMap(seed, width, height, WhirlingNoise.instance, 1.625);
        //world = new WorldMapGenerator.EllipticalMap(seed, width, height, ClassicNoise.instance, 1.5);
        //world = new WorldMapGenerator.MimicMap(seed, ClassicNoise.instance, 1.5);
        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, FastNoise.instance, 1.25);
        //world = new WorldMapGenerator.RoundSideMap(seed, width, height, ClassicNoise.instance, 0.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, ClassicNoise.instance, 0.5, 0.0625, 2.5);
//        world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, ClassicNoise.instance, 0.75);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, ClassicNoise.instance, 0.5);
        dbm = new WorldMapGenerator.DetailedBiomeMapper();

        for (int i = 0; i < Thesaurus.adjective.size(); i++) {
            adjective.addAll(Thesaurus.adjective.getAt(i));
        }
        for (int i = 0; i < Thesaurus.noun.size(); i++) {
            noun.addAll(Thesaurus.noun.getAt(i));
        }


        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setState(seed);
                        break;
                    /*
                    case '=':
                    case '+':
                        zoomIn();
                        break;
                    case '-':
                    case '_':
                        zoomOut();
                        break;
                    */
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                Gdx.graphics.requestRendering();
            }
        }/*, new SquidMouse(1, 1, width, height, 0, 0, new InputAdapter()
        {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(button == Input.Buttons.RIGHT)
                {
                    zoomOut(screenX, screenY);
                    Gdx.graphics.requestRendering();
                }
                else
                {
                    zoomIn(screenX, screenY);
                    Gdx.graphics.requestRendering();
                }
                return true;
            }
        })*/);
        generate(seed);
        rng.setState(seed);
        //Gdx.input.setInputProcessor(input);
        //display.setPosition(0, 0);
        //stage.addActor(display);
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }

    public void zoomIn() {
        zoomIn(width >> 1, height >> 1);
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
        world.zoomIn(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }
    public void zoomOut()
    {
        zoomOut(width>>1, height>>1);
    }
    public void zoomOut(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
        world.zoomOut(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }
    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        //randomizeColors(seed);
//        world.generate(1, 1.125, seed); // mimic of Earth favors too-cold planets
//        dbm.makeBiomes(world);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, FastNoise.instance, octaveCounter * 0.001);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, ClassicNoise.instance, octaveCounter * 0.001, 0.0625, 2.5);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, ClassicNoise.instance, octaveCounter * 0.001);
        world.generate(0.95 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.15,
                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.2 + 1.0, seed);
        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = makeName();
        while (Gdx.files.local(path + name + ".png").exists())
            name = makeName();
//        String name = octaveCounter + "_" + "Fast";
//        String name = octaveCounter + "_" + lang.word(rng, true); //, Math.min(3 - rng.next(1), rng.betweenWeighted(1, 5, 4))
//        while (Gdx.files.local(path + name + ".png").exists())
//            name = octaveCounter + "_" + lang.word(rng, true);
//        generate(0x1337BEEFCAFEL);
        generate(CrossHash.hash64(name));
//        generate(CrossHash.hash64(path));
        octaveCounter += 100;

        //display.erase();
        int hc, tc, bc;
//        int[][] heightCodeData = world.heightCodeData;
//        greasedWorld.refill(heightCodeData, 4, 10001).perceptualHashQuick(worldHash, workingHash);
//        worldCount = greasedWorld.size() - voidCount;
//        intersectionCount = greasedWorld.and(earth).size() - voidCount;
//        double jaccard = intersectionCount / (earthCount + worldCount - intersectionCount);
//        if(jaccard < 0.3)
//            return;
//        int hc;
        final int[][] heightCodeData = world.heightCodeData;
//        final double[][] moistureData = world.moistureData, heatData = world.heatData, heightData = world.heightData;
        double elevation, heat, moisture;
        boolean icy;
        int t;
        double[][] heightData = world.heightData;
        int[][] heatCodeData = dbm.heatCodeData;
        int[][] biomeCodeData = dbm.biomeCodeData;
        //pm.setColor(SColor.quantize253I(SColor.DB_INK));
        pm.setColor(SColor.DB_INK);
        pm.fill();
        for (int y = 0; y < height; y++) {
            PER_CELL:
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if(hc == 1000)
                    continue;
                tc = heatCodeData[x][y];
                bc = biomeCodeData[x][y];
                if(tc == 0)
                {
                    switch (hc)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)))));
                            continue PER_CELL;
                        case 4:
                            pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)))));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(
                                BIOME_COLOR_TABLE[56], coastalColor,
                                (MathUtils.clamp((float) (((heightData[x][y] + 0.06) * 8.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f)))));
                        break;
                    default:
//                        if((t = dbm.extractMixAmount(bc)) < 0f || t > 1f)
//                            System.out.println(t);
//                        t = SColor.floatToInt(SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)],
//                                BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)], dbm.extractMixAmount(bc)));
//                        //System.out.printf("Color: 0x%08X, Biome: %s", t, dbm.extractBiomeA(bc));
//                        pm.drawPixel(x, y, t);
                        pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)],
                                BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)], dbm.extractMixAmount(bc))));

                }
            }
        }
        
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                hc = heightCodeData[x][y];
//                if (hc == 1000)
//                    continue;
//                moisture = moistureData[x][y];
//                heat = heatData[x][y];
//                elevation = heightData[x][y];
//                icy = heat - elevation * 0.25 < 0.16;
//                if(hc < 4) {
//                    pm.drawPixel(x, y, SColor.floatToInt(
//                            heat < 0.26 ? SColor.lerpFloatColors(shallowColor, ice,
//                                    (float)((elevation + 1.0) / (WorldMapGenerator.sandLower+1.0)))
//                                    : SColor.lerpFloatColors(
//                                    BIOME_COLOR_TABLE[56], coastalColor,
//                                    (MathUtils.clamp((float) (((elevation + 0.06) * 16.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f)))));
//                }
//                else if(hc == 4)
//                    pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(icy ? BIOME_COLOR_TABLE[0] : SColor.lerpFloatColors(BIOME_DARK_COLOR_TABLE[36], BIOME_COLOR_TABLE[41],
//                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))),
//                            SColor.lerpFloatColors(icy ? ice : SColor.lerpFloatColors(rocky, desertAlt,
//                                    (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))),
//                                    icy ? lightIce : SColor.lerpFloatColors(woodland, BIOME_COLOR_TABLE[35],
//                                            ((float)heat)),
//                                    (extreme((float) (moisture)))),
//                            (float) ((elevation - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)))));
//                else
//                    pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(icy ? ice : SColor.lerpFloatColors(rocky, desertAlt,
//                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))),
//                            icy ? lightIce : SColor.lerpFloatColors(woodland, BIOME_COLOR_TABLE[35],
//                                    ((float)heat)),
//                            (extreme((float) (moisture))))));
//            }
//        }

//        writer.palette.analyze(pm);
//        writer.palette.reduceWithNoise(pm);

        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0);
        batch.end();

        //        PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);
        try {
            writer.writePrecisely(Gdx.files.local(path + name + ".png"), pm, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + path + name + ".png", ex);
        }

//PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);
//        int dist = Long.bitCount(earthHash[0] ^ worldHash[0]) + Long.bitCount(earthHash[1] ^ worldHash[1])
//                + Long.bitCount(earthHash[2] ^ worldHash[2]) + Long.bitCount(earthHash[3] ^ worldHash[3]);
//        Gdx.files.local(path + StringKit.bin((long)(jaccard * 0x100000000000L)) + " " + name +".txt").writeString(
//                "Perceptual Hash: " + StringKit.hex(worldHash) +
//                        "\nJaccard similarity as double: " + jaccard +
//                        "\nHamming distance between hashes of Earth and " + name + ": " + dist +
//                        "\nNon-water cells: " + greasedWorld.size() +
//                        "\nGuessed empty cells: " + voidCount +
//                        "\nActual empty cells: " + greasedWorld.refill(world.heightCodeData, 1000).size() +
//                        "\nJaccard similarity as ratio: " + (int)intersectionCount + "/" + (int)(earthCount + worldCount - intersectionCount) +
//                        "\nSeed: 0x" + StringKit.hex(CrossHash.hash64(name)) + "L", false);

//        StringBuilder csv = new StringBuilder((width + 50) * height * 5);
//        csv.append("public static final String[] BIOME_TABLE = {\n" +
//                "    //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST\n" +
//                "    \"Ice\",    \"Ice\",          \"Grassland\",      \"Desert\",              \"Desert\",             \"Desert\",             //DRYEST\n" +
//                "    \"Ice\",    \"Tundra\",       \"Grassland\",      \"Grassland\",           \"Desert\",             \"Desert\",             //DRYER\n" +
//                "    \"Ice\",    \"Tundra\",       \"Woodland\",       \"Woodland\",            \"Savanna\",            \"Desert\",             //DRY\n" +
//                "    \"Ice\",    \"Tundra\",       \"SeasonalForest\", \"SeasonalForest\",      \"Savanna\",            \"Savanna\",            //WET\n" +
//                "    \"Ice\",    \"Tundra\",       \"BorealForest\",   \"TemperateRainforest\", \"TropicalRainforest\", \"Savanna\",            //WETTER\n" +
//                "    \"Ice\",    \"BorealForest\", \"BorealForest\",   \"TemperateRainforest\", \"TropicalRainforest\", \"TropicalRainforest\", //WETTEST\n" +
//                "    \"Rocky\",  \"Rocky\",        \"Beach\",          \"Beach\",               \"Beach\",              \"Beach\",              //COASTS\n" +
//                "    \"Ice\",    \"River\",        \"River\",          \"River\",               \"River\",              \"River\",              //RIVERS\n" +
//                "    \"Ice\",    \"River\",        \"River\",          \"River\",               \"River\",              \"River\",              //LAKES\n" +
//                "    \"Ocean\",  \"Ocean\",        \"Ocean\",          \"Ocean\",               \"Ocean\",              \"Ocean\",              //OCEAN\n" +
//                "};\n\n" +
//                "public static float extractFloat(String[] mapData, int x, int y) {\n" +
//                "    return (mapData[y].codePointAt(x) - 93) * 0x1p-10f;\n" +
//                "}\n\n" +
//                "public static int extractInt(String[] mapData, int x, int y) {\n" +
//                "    return mapData[y].codePointAt(x) - 93;\n" +
//                "}\n\n" +
//                "public static String extractBiome(String[] mapData, int x, int y) {\n" +
//                "    return BIOME_TABLE[mapData[y].codePointAt(x) - 93];\n" +
//                "}\n\n");
//        csv.append("/** Use with extractFloat() or extractInt() to get the height of this area, either between 0.0 and 1.0 or between 0 and 1023 */\npublic String[] heightMap = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            for (int x = 0; x < width; x++) {
//                csv.append((char)((1.0 + world.heightData[x][y]) * 512 + 93));
//            }
//            csv.append("\",\n");
//        }
//        csv.append("};\n\n");
//        csv.append("/** Use with extractFloat() to get a heat level between 0.0 and 1.0, as some kind of yearly average*/\n" +
//                "public String[] heatMap = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            for (int x = 0; x < width; x++) {
//                csv.append((char)(world.heatData[x][y] * 1024 + 93));
//            }
//            csv.append("\",\n");
//        }
//        csv.append("};\n\n");
//        csv.append("/** Use with extractFloat() to get moisture level between 0.0 and 1.0, as some kind of yearly average */\n" +
//                "public String[] moistureMap = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            for (int x = 0; x < width; x++) {
//                csv.append((char)(world.moistureData[x][y] * 1024 + 93));
//            }
//            csv.append("\",\n");
//        }
//        csv.append("};\n\n");
//        StringBuilder csv2 = new StringBuilder(width * height + 50);
//        StringBuilder csv3 = new StringBuilder(width * height + 50);
//        StringBuilder csv4 = new StringBuilder(width * height + 50);
//        csv.append("/** Use with extractBiome() to get a biome name or extractInt() to get an index (0-59, inclusive); this is biome A */\n" +
//                "public String[] biomeMapA = new String[] {\n");
//        csv2.append("/** Use with extractBiome() to get a biome name or extractInt() to get an index (0-59, inclusive); this is biome B */\n" +
//                "public String[] biomeMapB = new String[] {\n");
//        csv3.append("/** Use with extractFloat(); how much biome A affects this area. */\npublic String[] biomePortionMapA = new String[] {\n");
//        csv4.append("/** Use with extractFloat(); how much biome B affects this area. */\npublic String[] biomePortionMapB = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            csv2.append('"');
//            csv3.append('"');
//            csv4.append('"');
//            for (int x = 0; x < width; x++) {
//                int biome = dbm.biomeCodeData[x][y];
//                float mix = dbm.extractMixAmount(biome);
//                csv.append((char)(dbm.extractPartA(biome) + 93));
//                csv2.append((char)(dbm.extractPartB(biome) + 93));
//                csv3.append((char)(93 + 1024.0 * mix));
//                csv4.append((char)(1024 + 93 - 1024.0 * mix));
//            }
//            csv.append("\",\n");
//            csv2.append("\",\n");
//            csv3.append("\",\n");
//            csv4.append("\",\n");
//        }
//        csv.append("};\n\n");
//        csv2.append("};\n\n");
//        csv3.append("};\n\n");
//        csv4.append("};\n\n");
//        csv.append(csv2).append(csv3).append(csv4);
//        Gdx.files.local(path + name + ".java").writeString(csv.toString(), false);
        //if(counter >= 1000000 || jaccard >= 0.4)
        if(counter >= LIMIT)
                Gdx.app.exit();
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        //Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        // stage has its own batch and must be explicitly told to draw().
        //stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        writer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Detailed World Map";
        config.width = width * cellWidth;
        config.height = height * cellHeight;
        //config.fullscreen = true;
        config.foregroundFPS = 0;
        //config.fullscreen = true;
        config.backgroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DetailedWorldMapWriter(), config);
    }
}
