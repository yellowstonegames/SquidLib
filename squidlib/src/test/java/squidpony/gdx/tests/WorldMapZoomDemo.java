package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.DiverRNG;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.StatefulRNG;

import static squidpony.squidgrid.gui.gdx.SColor.*;

/**
 * An adapted version of {@link DetailedWorldMapDemo} that shows a 4x4 grid of separate zoomed-in views of the same map,
 * a LocalMimicMap of Australia. The grid is shown in bright magenta. Zooming with any WorldMapGenerator can be very
 * confusing, so this is meant to show at least some ways it can be done simply. Most relevant code is in putMap(),
 * which places one of the 16 WorldMapGenerators on the screen.
 */
public class WorldMapZoomDemo extends ApplicationAdapter {
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

    //private static final int width = 314 * 3, height = 300;
//    private static final int width = 1024, height = 512;
//    private static final int width = 512, height = 256;
    private static final int width = 256, height = 256;
//    private static final int width = 400, height = 400; // fast rotations
//    private static final int width = 300, height = 300;
//    private static final int width = 1600, height = 800;
//    private static final int width = 900, height = 900;
//    private static final int width = 700, height = 700;
//    private static final int width = 512, height = 512;
//    private static final int width = 128, height = 128;

    private static final int bigWidth = width << 2, bigHeight = height << 2;
    
    private FilterBatch batch;
//    private SquidPanel display;//, overlay;
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
//    private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    //private WorldMapGenerator.SpaceViewMap world;
//    private WorldMapGenerator.RotatingSpaceMap world;
    //private WorldMapGenerator.MimicMap world;
    //private WorldMapGenerator.EllipticalMap world;
    //private WorldMapGenerator.EllipticalHammerMap world;
    //private WorldMapGenerator.RoundSideMap world;
//    private WorldMapGenerator.HyperellipticalMap world;
//    private WorldMapGenerator.SphereMap world;
    private WorldMapGenerator[][] world = new WorldMapGenerator[4][4];
    private BitmapFont font;
    private Pixmap pm;
    private Texture pt;
    private long ttg; // time to generate
    private WorldMapGenerator.DetailedBiomeMapper[][] dbm = new WorldMapGenerator.DetailedBiomeMapper[4][4];
    
    // Biome map colors
    private static float baseIce = SColor.ALICE_BLUE.toFloatBits();
    private static float ice = SColor.floatGetI(240, 248, 255);
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
    
    private static float desertAlt = SColor.floatGetI(253, 226, 160);

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

    private static float[][] seasonColors = {
            { //winter
                    desert,
                    toEditedFloat(savanna, 0f, 0.08f, -0.05f, 0f),
                    toEditedFloat(tropicalRainforest, 0f, 0.06f, -0.09f, 0f),
                    toEditedFloat(grassland, 0f, -0.2f, 0.12f, 0f),
                    SColor.BAIKO_BROWN.toFloatBits(),                             // woodland
                    SColor.BAIKO_BROWN.toEditedFloat(0f, -0.2f, -0.05f),          // seasonalForest
                    toEditedFloat(temperateRainforest, 0f, 0.03f, -0.05f, 0f),
                    lerpFloatColors(borealForest, FLOAT_WHITE, 0.5f),
                    lerpFloatColors(tundra, FLOAT_WHITE, 0.65f),
                    ice,
                    toEditedFloat(beach, 0f, -0.1f, 0f, 0f),
                    rocky,
                    ice, // shallow water
                    deepColor,
                    emptyColor
            },
            { //spring
                    desert,
                    toEditedFloat(savanna, 0f, 0.08f, 0.1f, 0f),
                    tropicalRainforest,
                    toEditedFloat(grassland, 0f, 0.1f, 0.0f, 0f),
                    woodland,
                    toEditedFloat(seasonalForest, -0.03f, 0.05f, 0.03f, 0f),
                    toEditedFloat(temperateRainforest, 0f, 0.01f, -0.02f, 0f),
                    borealForest,
                    tundra,
                    ice,
                    beach,
                    rocky,
                    foamColor,
                    deepColor,
                    emptyColor
            },
            { //summer
                    desert,
                    toEditedFloat(savanna, 0f, -0.03f, 0.2f, 0f),
                    tropicalRainforest,
                    toEditedFloat(grassland, 0f, 0.03f, 0.05f, 0f),
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
            },
            { //autumn
                    desert,
                    savanna,
                    tropicalRainforest,
                    toEditedFloat(grassland, 0f, -0.05f, 0.07f, 0f),
                    SColor.floatGetI(210, 228, 80), // woodland
                    GOLDEN_FALLEN_LEAVES.toFloatBits(),//seasonalForest,
                    temperateRainforest,
                    toEditedFloat(borealForest, 0f, -0.1f, -0.02f, 0f),
                    tundra,
                    ice,
                    beach,
                    rocky,
                    foamColor,
                    deepColor,
                    emptyColor
            },

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
    
    @Override
    public void create() {
        batch = new FilterBatch();
        view = new StretchViewport(bigWidth*cellWidth, bigHeight*cellHeight);
        font = DefaultResources.getLargeSmoothFont();
        font.setColor(CERISE);
        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGB888);
        pm.setBlending(Pixmap.Blending.None);
        pt = new Texture(pm);
        pt.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        seed = 0x44B94C6A93EF3D54L;//0xca576f8f22345368L;//0x9987a26d1e4d187dL;//0xDEBACL;
        rng = new StatefulRNG(seed);
//        world = new WorldMapGenerator.TilingMap(seed, width, height, FastNoise.instance, 1.25);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, WhirlingNoise.instance, 0.875);
        //world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, ClassicNoise.instance, 0.75);
//        world = new WorldMapGenerator.MimicMap(seed, FastNoise.instance, 0.7);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, ClassicNoise.instance, 0.7);
        //world = new WorldMapGenerator.RotatingSpaceMap(seed, width, height, FastNoise.instance, 0.7);
        //world = new WorldMapGenerator.RoundSideMap(seed, width, height, ClassicNoise.instance, 0.8);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, FastNoise.instance, 0.7, 0.0625, 2.5);
//        world = new WorldMapGenerator.SphereMap(seed, width, height, FastNoise.instance, 0.6);
        FastNoise noise = new FastNoise(1, 2f, FastNoise.SIMPLEX_FRACTAL, 2, 3.2f, 0.3125f);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
//                world[x][y] = new WorldMapGenerator.LocalMimicMap(seed, FastNoise.instance, 0.7);
                world[x][y] = new WorldMapGenerator.LocalMap(seed, 256, 256, noise, 0.5);
                dbm[x][y] = new WorldMapGenerator.DetailedBiomeMapper();
                world[x][y].generate(
                        1.0 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
                        DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.35 + 0.9,
                        seed);
                world[x][y].zoomIn(1, (x >> 1) * 128 + 64, (3 - y >> 1) * 128 + 64);
                world[x][y].zoomIn(1, (x & 1) * 128 + 64, (3 - y & 1) * 128 + 64);
                dbm[x][y].makeBiomes(world[x][y]);
            }
        }
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                        Gdx.app.exit();
                        break;
                    default:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setState(seed);
                        break;
                }
            }
        }, new SquidMouse(1, 1, width, height, 0, 0, new InputAdapter()));
        input.setRepeatGap(Long.MAX_VALUE);
        Gdx.input.setInputProcessor(input);
//        display.setPosition(0, 0);
//        stage.addActor(display);
//        Gdx.graphics.setContinuousRendering(true);
//        Gdx.graphics.requestRendering();
    }

    public void generate(final long seed)
    {
        long startTime = System.nanoTime();
        System.out.println("Seed used: 0x" + StringKit.hex(seed) + "L");
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                world[x][y].generate(
                        1.0 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
                        DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.35 + 0.9,
                        seed);
                world[x][y].zoomIn(1, (x >> 1) * 128 + 64, (3 - y >> 1) * 128 + 64);
                world[x][y].zoomIn(1, (x & 1) * 128 + 64, (3 - y & 1) * 128 + 64);
                dbm[x][y].makeBiomes(world[x][y]);
            }
        }
        ttg = System.nanoTime() - startTime >> 20;
    }

    public void putMap(int offsetX, int offsetY, WorldMapGenerator world, WorldMapGenerator.DetailedBiomeMapper dbm) {
        int hc, tc, bc;
        int[][] heightCodeData = world.heightCodeData;
        double[][] heightData = world.heightData;
        int[][] heatCodeData = dbm.heatCodeData;
        int[][] biomeCodeData = dbm.biomeCodeData;
        pm.setColor(SColor.DB_INK);
        pm.fill();
        for (int y = 0; y < height; y++) {
            PER_CELL:
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if (hc == 1000)
                    continue;
                tc = heatCodeData[x][y];
                bc = biomeCodeData[x][y];
                if (tc == 0) {
                    switch (hc) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            pm.drawPixel(x, y, NumberTools.floatToReversedIntBits(SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)))));
                            continue PER_CELL;
                        case 4:
                            pm.drawPixel(x, y, NumberTools.floatToReversedIntBits(SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)))));//Color.rgba8888(tempColor));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        pm.drawPixel(x, y, NumberTools.floatToReversedIntBits(SColor.lerpFloatColors(
                                BIOME_COLOR_TABLE[56], coastalColor,
                                (MathUtils.clamp((float) (((heightData[x][y] + 0.06) * 8.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f)))));//Color.rgba8888(tempColor));
                        break;
                    default:
                        // seasonal color generation, with deciduous forests
//                                SColor.lerpFloatColors(
//                                SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)<<2|2],
//                                        BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)<<2|2], dbm.extractMixAmount(bc)),
//                                SColor.lerpFloatColors(
//                                SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)<<2|s],
//                                        BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)<<2|s], dbm.extractMixAmount(bc)),
//                                SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)<<2|ns],
//                                        BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)<<2|ns], dbm.extractMixAmount(bc)), sa),
//                                        latitudeAdjust)
                        pm.drawPixel(x, y, NumberTools.floatToReversedIntBits(
                                SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)],
                                BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)], dbm.extractMixAmount(bc))));
                }
            }
        }
        // comment out the next two lines to remove the bright lines between zoomed maps.
        pm.setColor(HOT_MAGENTA);
        pm.drawRectangle(0, 0, width, height);
        // end bright line code
        pt.draw(pm, 0, 0);
        batch.begin();
        // this is the tricky part.
        // offsetX and offsetY are given as multiples of width and height, respectively.
        // we divide offsetX by 2 (using a right shift) because we also divide the shown width by 2, to smooth the map.
        // offsetY was given with WorldMapGenerator (and SquidLib in general) conventions that y points down, but...
        // the batch uses y pointing up, so we need to subtract our offsetY from the highest value it can take, which...
        // is bigHeight - height, or equivalently height * 3 . offsetY and height are also divided by 2.
        batch.draw(pt, offsetX >> 1, offsetY >> 1, width >> 1, height >> 1);
        batch.end();
    }
    @Override
    public void render() {
        Gdx.gl.glClearColor(SColor.DB_INK.r, SColor.DB_INK.g, SColor.DB_INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                putMap(x * width, y * height, world[x][y], dbm[x][y]);
            }
        }
        batch.begin();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                font.draw(batch, "x:" + x + ",y:" + y, x * width + 20 >> 1, y * height - 28 + height >> 1);
            }
        }
        batch.end();
        Gdx.graphics.setTitle("Took " + ttg + " ms to generate");

        if (input.hasNext()) {
            input.next();
        }
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
        config.width = bigWidth >> 1;
        config.height = bigHeight >> 1;
        config.foregroundFPS = 0;
        config.backgroundFPS = -1;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new WorldMapZoomDemo(), config);
    }
}
