package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidMouse;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.ClassicNoise;
import squidpony.squidmath.LinnormRNG;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.StatefulRNG;

import static squidpony.squidgrid.gui.gdx.SColor.*;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 * Currently, clouds are in progress, and look like <a href="http://i.imgur.com/Uq7Whzp.gifv">this preview</a>.
 */
public class DetailedWorldMapDemo extends ApplicationAdapter {
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
    private static final int width = 400, height = 400; // fast rotations
//    private static final int width = 300, height = 300;
//    private static final int width = 1600, height = 800;
    ///private static final int width = 1000, height = 1000;
//    private static final int width = 700, height = 700;
//    private static final int width = 512, height = 512;
//    private static final int width = 128, height = 128;
    
    private SpriteBatch batch;
//    private SquidPanel display;//, overlay;
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
//    private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private int mode = 1, maxModes = 4;
    //private WorldMapGenerator.SpaceViewMap world;
//    private WorldMapGenerator.RotatingSpaceMap world;
    //private WorldMapGenerator.MimicMap world;
    //private WorldMapGenerator.EllipticalMap world;
    //private WorldMapGenerator.EllipticalHammerMap world;
    //private WorldMapGenerator.RoundSideMap world;
//    private WorldMapGenerator.HyperellipticalMap world;
//    private WorldMapGenerator.SphereMapAlt world;
    private WorldMapGenerator world;
    //private Noise.Noise4D cloudNoise;
    //private final float[][][] cloudData = new float[128][128][128];

    private Pixmap pm;
    private Texture pt;
    private int counter = 0;
    private float season = 0f;
    private Color tempColor = Color.WHITE.cpy();

    private boolean spinning = false;

    private boolean seasons = true;

    private boolean cloudy = false;
//    private float nation = 0f;
    private long ttg = 0; // time to generate
    private WorldMapGenerator.DetailedBiomeMapper dbm;
//    private FantasyPoliticalMapper fpm;
//    private char[][] political;
    
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
    }, BIOME_COLOR_TABLE = new float[244], BIOME_DARK_COLOR_TABLE = new float[244];
    private static final float[] NATION_COLORS = new float[144];

    static {
        float b, diff;
        for (int i = 0; i < 240; i+=4) {
            for (int j = 0; j < 4; j++) {
                b = BIOME_TABLE[i>>2];
                diff = ((b - (int)b) - 0.48f) * 0.27f;
                BIOME_COLOR_TABLE[i + j] = (b = (diff >= 0)
                        ? SColor.lightenFloat(seasonColors[j][(int) b], diff)
                        : SColor.darkenFloat(seasonColors[j][(int) b], -diff));
                BIOME_DARK_COLOR_TABLE[i + j] = SColor.darkenFloat(b, 0.08f);
            }
        }
        BIOME_COLOR_TABLE[240] = BIOME_COLOR_TABLE[241] = BIOME_COLOR_TABLE[242] = BIOME_COLOR_TABLE[243] =
                BIOME_DARK_COLOR_TABLE[240] = BIOME_DARK_COLOR_TABLE[241] = BIOME_DARK_COLOR_TABLE[242] = BIOME_DARK_COLOR_TABLE[243] = emptyColor;
        for (int i = 0; i < 144; i++) {
            NATION_COLORS[i] =  SColor.COLOR_WHEEL_PALETTE_REDUCED[((i + 1234567) * 13 & 0x7FFFFFFF) % 144].toFloatBits();
        }
    }


//    private static void randomizeColors(long seed)
//    {
//        float b, diff, alt, hue = NumberTools.randomSignedFloat(seed);
//        int bCode;
//        for (int i = 0; i < 60; i++) {
//            b = BIOME_TABLE[i];
//            bCode = (int)b;
//            alt = SColor.toEditedFloat(biomeColors[bCode],
//                    hue,
//                    NumberTools.randomSignedFloat(seed * 3L + bCode) * 0.45f - 0.1f,
//                    NumberTools.randomSignedFloat(seed * 5L + bCode) * 0.5f,
//                    0f);
//            diff = ((b % 1.0f) - 0.48f) * 0.27f;
//            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
//                    ? SColor.lightenFloat(alt, diff)
//                    : SColor.darkenFloat(alt, -diff));
//            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
//        }
//        float sat = NumberTools.randomSignedFloat(seed * 3L - 1L) * 0.4f, 
//                value = NumberTools.randomSignedFloat(seed * 5L - 1L) * 0.3f;
//        
//        deepColor = SColor.toEditedFloat(baseDeepColor, hue, sat, value, 0f);
//        shallowColor = SColor.toEditedFloat(baseShallowColor, hue, sat, value, 0f);
//        coastalColor = SColor.toEditedFloat(baseCoastalColor, hue, sat, value, 0f);
//        foamColor = SColor.toEditedFloat(baseFoamColor, hue, sat, value, 0f);
//        ice = SColor.toEditedFloat(baseIce, hue, sat * 0.3f, value * 0.2f, 0f);
//    }

//    protected void makeBiomes() {
//        final WorldMapGenerator world = this.world;
//        final int[][] heightCodeData = world.heightCodeData;
//        final double[][] heatData = world.heatData, moistureData = world.moistureData, heightData = world.heightData;
//        int hc, mc, heightCode;
//        double hot, moist, high, i_hot = 1.0 / this.world.maxHeat, fresh;
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//
//                heightCode = heightCodeData[x][y];
//                if(heightCode == 1000) {
//                    biomeUpperCodeData[x][y] = biomeLowerCodeData[x][y] = 54;
//                    shadingData[x][y] = 0.0;
//                    continue;
//                }
//                hot = heatData[x][y];
//                moist = moistureData[x][y];
//                high = heightData[x][y];
//                fresh = world.freshwaterData[x][y];
//                boolean isLake = world.generateRivers && heightCode >= 4 && fresh > 0.65 && fresh + moist * 2.35 > 2.75,//world.partialLakeData.contains(x, y) && heightCode >= 4,
//                        isRiver = world.generateRivers && !isLake && heightCode >= 4 && fresh > 0.55 && fresh + moist * 2.2 > 2.15;//world.partialRiverData.contains(x, y) && heightCode >= 4;
//                if (moist >= (wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2)) {
//                    mc = 5;
//                } else if (moist >= (wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2)) {
//                    mc = 4;
//                } else if (moist >= (wetValueUpper - (dryValueUpper - dryValueLower) * 0.2)) {
//                    mc = 3;
//                } else if (moist >= (dryValueUpper - (drierValueUpper - drierValueLower) * 0.2)) {
//                    mc = 2;
//                } else if (moist >= (drierValueUpper - (driestValueUpper) * 0.2)) {
//                    mc = 1;
//                } else {
//                    mc = 0;
//                }
//
//                if (hot >= (warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
//                    hc = 5;
//                } else if (hot >= (warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
//                    hc = 4;
//                } else if (hot >= (warmValueUpper - (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
//                    hc = 3;
//                } else if (hot >= (coldValueUpper - (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
//                    hc = 2;
//                } else if (hot >= (colderValueUpper - (coldestValueUpper) * 0.2) * i_hot) {
//                    hc = 1;
//                } else {
//                    hc = 0;
//                }
//
//                heatCodeData[x][y] = hc;
//                moistureCodeData[x][y] = mc;
//                biomeUpperCodeData[x][y] = isLake ? hc + 48 : (isRiver ? hc + 42 : ((heightCode == 4) ? hc + 36 : hc + mc * 6));
//
//                if (moist >= (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2)) {
//                    mc = 5;
//                } else if (moist >= (wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.2)) {
//                    mc = 4;
//                } else if (moist >= (dryValueUpper + (wetValueUpper - wetValueLower) * 0.2)) {
//                    mc = 3;
//                } else if (moist >= (drierValueUpper + (dryValueUpper - dryValueLower) * 0.2)) {
//                    mc = 2;
//                } else if (moist >= (driestValueUpper + (drierValueUpper - drierValueLower) * 0.2)) {
//                    mc = 1;
//                } else {
//                    mc = 0;
//                }
//
//                if (hot >= (warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2) * i_hot) {
//                    hc = 5;
//                } else if (hot >= (warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
//                    hc = 4;
//                } else if (hot >= (coldValueUpper + (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
//                    hc = 3;
//                } else if (hot >= (colderValueUpper + (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
//                    hc = 2;
//                } else if (hot >= (coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
//                    hc = 1;
//                } else {
//                    hc = 0;
//                }
//
//                biomeLowerCodeData[x][y] = hc + mc * 6;
//
//                if (isRiver || isLake)
//                    shadingData[x][y] = //((moist - minWet) / (maxWet - minWet)) * 0.45 + 0.15 - 0.14 * ((hot - minHeat) / (maxHeat - minHeat))
//                            (moist * 0.35 + 0.6);
//                else
//                    shadingData[x][y] = //(upperProximityH + upperProximityM - lowerProximityH - lowerProximityM) * 0.1 + 0.2
//                            (heightCode == 4) ? (0.18 - high) / (0.08) :
//                                    NumberTools.bounce((high + moist) * (4.1 + high - hot)) * 0.5 + 0.5; // * (7.5 + moist * 1.9 - hot * 0.9)
//            }
//        }
//        counter = ThrustAltRNG.determine(seed) >>> 48;
//        //Noise.seamless3D(cloudData, seedC, 3);
//    }


    @Override
    public void create() {
        batch = new SpriteBatch();
//        display = new SquidPanel(width, height, cellWidth, cellHeight);
        //display.getTextCellFactory().font().getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        view = new StretchViewport(width*cellWidth, height*cellHeight);
        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGB888);
        pm.setBlending(Pixmap.Blending.None);
        pt = new Texture(pm);
        pt.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
//        stage = new Stage(view, batch);
        seed = 0xca576f8f22345368L;//0x9987a26d1e4d187dL;//0xDEBACL;
        rng = new StatefulRNG(seed);
        //world = new WorldMapGenerator.TilingMap(seed, width, height, WhirlingNoise.instance, 1.25);
        //world = new WorldMapGenerator.SphereMap(seed, width, height, WhirlingNoise.instance, 0.8);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, WhirlingNoise.instance, 0.875);
        //world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, ClassicNoise.instance, 0.75);
        //world = new WorldMapGenerator.MimicMap(seed, WhirlingNoise.instance, 0.8);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, ClassicNoise.instance, 0.7);
        world = new WorldMapGenerator.RotatingSpaceMap(seed, width, height, ClassicNoise.instance, 0.55);
        //world = new WorldMapGenerator.RoundSideMap(seed, width, height, ClassicNoise.instance, 0.8);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, ClassicNoise.instance, 0.7, 0.0625, 2.5);
        //cloudNoise = new Noise.Turbulent4D(WhirlingNoise.instance, new Noise.Ridged4D(SeededNoise.instance, 2, 3.7), 3, 5.9);
        //cloudNoise = new Noise.Layered4D(WhirlingNoise.instance, 2, 3.2);
        //cloudNoise2 = new Noise.Ridged4D(SeededNoise.instance, 3, 6.5);
        //world = new WorldMapGenerator.TilingMap(seed, width, height, WhirlingNoise.instance, 0.9);
        dbm = new WorldMapGenerator.DetailedBiomeMapper();
//        fpm = new FantasyPoliticalMapper();
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setState(seed);
                        break;
                    case '=':
                    case '+':
                        zoomIn();
                        break;
                    case '-':
                    case '_':
                        zoomOut();
                        break;
                    case 'C':
                    case 'c':
                        cloudy = !cloudy;
                        break;
                    case 'P':
                    case 'p':
                        spinning = !spinning;
                        break;
                    case 'S':
                    case 's':
                        seasons = !seasons;
                        break;
                    case 'M':
                    case 'm':
                        mode = (mode + 1) % maxModes;
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        }, new SquidMouse(1, 1, width, height, 0, 0, new InputAdapter()
        {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(button == Input.Buttons.RIGHT)
                {
                    zoomOut(screenX, screenY);
//                    Gdx.graphics.requestRendering();
                }
                else
                {
                    zoomIn(screenX, screenY);
//                    Gdx.graphics.requestRendering();
                }
                return true;
            }
        }));
        input.setRepeatGap(Long.MAX_VALUE);
        generate(seed);
        rng.setState(seed);
        Gdx.input.setInputProcessor(input);
//        display.setPosition(0, 0);
//        stage.addActor(display);
//        Gdx.graphics.setContinuousRendering(true);
//        Gdx.graphics.requestRendering();
    }

    public void zoomIn() {
        zoomIn(width>>1, height>>1);
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        long startTime = System.nanoTime();
        world.zoomIn(1, zoomX<<1, zoomY<<1);
        dbm.makeBiomes(world);
        //political = fpm.adjustZoom();//.generate(seed + 1000L, world, dbm, null, 50, 1.0);
//        System.out.println(StringKit.hex(CrossHash.hash64(world.heightCodeData)) + " " + StringKit.hex(CrossHash.hash64(dbm.biomeCodeData)));
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void zoomOut()
    {
        zoomOut(width>>1, height>>1);
    }
    public void zoomOut(int zoomX, int zoomY)
    {
        long startTime = System.nanoTime();
        world.zoomOut(1, zoomX<<1, zoomY<<1);
        dbm.makeBiomes(world);
        //political = fpm.adjustZoom();//.generate(seed + 1000L, world, dbm, null, 50, 1.0);
//        System.out.println(StringKit.hex(CrossHash.hash64(world.heightCodeData)) + " " + StringKit.hex(CrossHash.hash64(dbm.biomeCodeData)));
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void generate(final long seed)
    {
        long startTime = System.nanoTime();
        System.out.println("Seed used: 0x" + StringKit.hex(seed) + "L");
        //world.setCenterLongitude((System.currentTimeMillis() & 0xFFFFFFF) * 0.0002);
        //world.setCenterLongitude(++counter * 0.02);
        world.generate(1.0 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
                LinnormRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.35 + 0.9, seed);
        dbm.makeBiomes(world);
        //randomizeColors(seed);
        //political = fpm.generate(seed + 1000L, world, dbm, null, 50, 1.0);
//        System.out.println(StringKit.hex(CrossHash.hash64(world.heightCodeData)) + " " + StringKit.hex(CrossHash.hash64(dbm.biomeCodeData)));
        //counter = 0L;
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void rotate()
    {
        long startTime = System.nanoTime();
        world.setCenterLongitude((startTime & 0xFFFFFFFFFFFFL) * 0x1.1p-32);
        //world.setCenterLongitude(++counter * 0.02);
        world.generate(world.landModifier, world.coolingModifier, seed);
        dbm.makeBiomes(world);
        //political = fpm.generate(seed + 1000L, world, dbm, null, 50, 1.0);
//        System.out.println(StringKit.hex(CrossHash.hash64(world.heightCodeData)) + " " + StringKit.hex(CrossHash.hash64(dbm.biomeCodeData)));
        ttg = System.nanoTime() - startTime >> 20;
    }

//    public void putMap() {
//        // uncomment next line to generate maps as quickly as possible
//        //generate(rng.nextLong());
//        ArrayTools.fill(display.colors, -0x1.0p125F);
//        int hc, tc;
//        int[][] heightCodeData = world.heightCodeData;
//        double[][] heightData = world.heightData;
//        //double xp, yp, zp;
//        float cloud = 0f, shown, cloudLight = 1f;
//        for (int y = 0; y < height; y++) {
//            PER_CELL:
//            for (int x = 0; x < width; x++) {
//                hc = heightCodeData[x][y];
//                if(hc == 1000)
//                    continue;
//                tc = dbm.heatCodeData[x][y];
//                //cloud = (float) cloudNoise2.getNoiseWithSeed(xp = world.xPositions[x][y], yp = world.yPositions[x][y],
//                //        zp = world.zPositions[x][y], counter * 0.04, (int) seed) * 0.06f;
////                if(cloudy) {
////                    cloud = (float) Math.min(1f, (cloudNoise.getNoiseWithSeed(world.xPositions[x][y], world.yPositions[x][y], world.zPositions[x][y], counter * 0.0125, seed) * (0.75 + world.moistureData[x][y]) - 0.07));
////                    cloudLight = 0.65f + NumberTools.swayTight(cloud * 1.4f + 0.55f) * 0.35f;
////                    cloudLight = SColor.floatGet(cloudLight, cloudLight, cloudLight, 1f);
////                }
//                /*
//                cloud = Math.min(1f,
//                        cloudData[(int) (world.xPositions[x][y] * 109 + counter * 1.7) & 127]
//                        [(int) (world.yPositions[x][y] * 109) & 127]
//                        [(int) (world.zPositions[x][y] * 109) & 127] * 1.1f +
//                        cloudData[(int) (world.xPositions[x][y] * 119) & 127]
//                                [(int) (world.yPositions[x][y] * 119 + counter * 1.7) & 127]
//                                [(int) (world.zPositions[x][y] * 119) & 127] * 1.4f);
//                cloudLight = Math.min(1f, 1f +
//                        cloudData[(int) (world.xPositions[x][y] * 233) & 127]
//                                [(int) (world.yPositions[x][y] * 233) & 127]
//                                [(int) (world.zPositions[x][y] * 233 + counter * 2.3) & 127] * 0.64f);
//                cloudLight = SColor.floatGet(cloudLight, cloudLight, cloudLight, 1f);
//                                */
//                if(tc == 0)
//                {
//                    switch (hc)
//                    {
//                        case 0:
//                        case 1:
//                        case 2:
//                        case 3:
//                            shown = SColor.lerpFloatColors(shallowColor, ice,
//                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0))
//                            );
////                            if(cloud > 0.0)
////                                shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
//                            display.put(x, y, shown);
//                            continue PER_CELL;
//                        case 4:
//                            shown = SColor.lerpFloatColors(lightIce, ice,
//                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)));
////                            if(cloud > 0.0)
////                                shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
//                            display.put(x, y, shown);
//                            continue PER_CELL;
//                    }
//                }
//                switch (hc) {
//                    case 0:
//                    case 1:
//                    case 2:
//                    case 3:
//                        shown = SColor.lerpFloatColors(deepColor, coastalColor,
//                                (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0))
//                        );
////                        if(cloud > 0.0)
////                            shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
//                        display.put(x, y, shown);
//                        break;
//                    default:
//                        /*
//                        if(partialLakeData.contains(x, y))
//                            System.out.println("LAKE  x=" + x + ",y=" + y + ':' + (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 19
//                                    + shadingData[x][y] * 13) * 0.03125f);
//                        else if(partialRiverData.contains(x, y))
//                            System.out.println("RIVER x=" + x + ",y=" + y + ':' + (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 19
//                                    + shadingData[x][y] * 13) * 0.03125f);
//                        */
//                        int bc = dbm.biomeCodeData[x][y];
//                        shown = SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)],
//                                BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)], 
//                                dbm.extractMixAmount(bc));
////                        if(cloud > 0.0)
////                            shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
//                        //shown = SColor.lerpFloatColors(shown, NATION_COLORS[political[x][y] & 127], nation);
//                        display.put(x, y, shown);
//
//                        //display.put(x, y, SColor.lerpFloatColors(darkTropicalRainforest, desert, (float) (heightData[x][y])));
//                }
//            }
//        }
//    }

    public void putMap() {
        int hc, tc, bc;
        int[][] heightCodeData = world.heightCodeData;
        double[][] heightData = world.heightData;
        int[][] heatCodeData = dbm.heatCodeData;
        int[][] biomeCodeData = dbm.biomeCodeData;
        pm.setColor(quantize(SColor.DB_INK));
        pm.fill();
        int s = (int) season, ns = s + 1 & 3;
        float sa = season - s;
        final float ih = 2f / (height >> 1);
        for (int y = 0; y < height; y++) {
            if(y == (height >> 1))
            {
                s = s + 2 & 3;
                ns = s + 1 & 3;
            }
            float latitudeAdjust = Math.min(1f, Math.abs(((height >> 1) - y) * ih));
            //latitudeAdjust = latitudeAdjust * latitudeAdjust;
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
                            Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0))));
//                        pm.setColor(tempColor);
//                        pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                            pm.drawPixel(x, y, quantize(tempColor));//Color.rgba8888(tempColor));
                            //display.put(x, y, SColor.lerpFloatColors(shallowColor, ice,
                            //        (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0))));
                            continue PER_CELL;
                        case 4:
                            Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower))));
//                        pm.setColor(tempColor);
//                        pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                            pm.drawPixel(x, y, quantize(tempColor));//Color.rgba8888(tempColor));
                            //display.put(x, y, SColor.lerpFloatColors(lightIce, ice,
                            //        (float) ((heightData[x][y] - 0.1) / (0.18 - 0.1))));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(
                                BIOME_COLOR_TABLE[56<<2], coastalColor,
                                (MathUtils.clamp((float) (((heightData[x][y] + 0.06) * 8.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f))));
//                        Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(deepColor, coastalColor,
//                                (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0))));
//                    pm.setColor(tempColor);
//                    pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                        pm.drawPixel(x, y, quantize(tempColor));//Color.rgba8888(tempColor));
                        //display.put(x, y, SColor.lerpFloatColors(deepColor, coastalColor,
                        //        (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0))));
                        break;
                    default:
                        /*
                        if(partialLakeData.contains(x, y))
                            System.out.println("LAKE  x=" + x + ",y=" + y + ':' + (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 19
                                    + shadingData[x][y] * 13) * 0.03125f);
                        else if(partialRiverData.contains(x, y))
                            System.out.println("RIVER x=" + x + ",y=" + y + ':' + (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 19
                                    + shadingData[x][y] * 13) * 0.03125f);
                        */

                        Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(

                                SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)<<2|2],
                                        BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)<<2|2], dbm.extractMixAmount(bc)),
                                SColor.lerpFloatColors(
                                SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)<<2|s],
                                        BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)<<2|s], dbm.extractMixAmount(bc)),
                                SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)<<2|ns],
                                        BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)<<2|ns], dbm.extractMixAmount(bc)), sa),
                                        latitudeAdjust));
//                    pm.setColor(tempColor);
//                    pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                        pm.drawPixel(x, y, quantize(tempColor));//Color.rgba8888(tempColor));
                        //display.put(x, y, SColor.lerpFloatColors(BIOME_COLOR_TABLE[biomeLowerCodeData[x][y]],
                        //        BIOME_DARK_COLOR_TABLE[biomeUpperCodeData[x][y]],
                        //        (float) //(((heightData[x][y] - lowers[hc]) / (differences[hc])) * 11 +
                        //                shadingData[x][y]// * 21) * 0.03125f
                        //        ));

                        //display.put(x, y, SColor.lerpFloatColors(darkTropicalRainforest, desert, (float) (heightData[x][y])));
                }
            }
        }
        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0, width >> 1, height >> 1);
        batch.end();
    }
    public void putHeatMap() {
        int hc;
        int[][] heightCodeData = world.heightCodeData;
        double[][] heatData = world.heatData;
        double heat;
        pm.setColor(quantize(SColor.DB_INK));
        pm.fill();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if (hc == 1000)
                    continue;
                heat = heatData[x][y];
                if(hc < 4)
                    Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(ice, deepColor,
                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))));
                else
                    Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(-0x1.5bbf5ap126F, // SColor.MOSS_GREEN
                             -0x1.8081fep125F, // SColor.CORAL_RED
                        (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))));
                pm.drawPixel(x, y, quantize(tempColor));
            }
        }
        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0, width >> 1, height >> 1);
        batch.end();
    }
    public void putMoistureMap() {
        int hc;
        int[][] heightCodeData = world.heightCodeData;
        double[][] moistureData = world.moistureData;
        double moisture;
        pm.setColor(quantize(SColor.DB_INK));
        pm.fill();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if (hc == 1000)
                    continue;
                moisture = moistureData[x][y];
                if(hc < 4)
                    Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(shallowColor, deepColor,
                            (float) ((moisture - world.minWet) / (world.maxWet - world.minWet + 0.001))));
                else
                    Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(desert, tropicalRainforest,
                            (float) ((moisture - world.minWet) / (world.maxWet - world.minWet + 0.001))));
                pm.drawPixel(x, y, quantize(tempColor));
            }
        }
        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0, width >> 1, height >> 1);
        batch.end();
    }
    private final float emphasize(final float a)
    {
        return a * a * (3f - 2f * a);
    }
    private final float extreme(final float a)
    {
        return a * a * a * (a * (a * 6f - 15f) + 10f);
    }
    public void putExperimentMap() {
        int hc;
        final int[][] heightCodeData = world.heightCodeData;
        final double[][] moistureData = world.moistureData, heatData = world.heatData, heightData = world.heightData;
        double elevation, heat, moisture;
        boolean icy;
        pm.setColor(quantize(SColor.DB_INK));
        pm.fill();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if (hc == 1000)
                    continue;
                moisture = moistureData[x][y];
                heat = heatData[x][y];
                elevation = heightData[x][y];
                icy = heat - elevation * 0.25 < 0.16;
                if(hc < 4) {
                    float a = (MathUtils.clamp((float) (((elevation + 0.06) * 16.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f));
                    Color.abgr8888ToColor(tempColor,
                            heat < 0.26 ? SColor.lerpFloatColors(shallowColor, ice,
                                    (float)((elevation + 1.0) / (WorldMapGenerator.sandLower+1.0)))
                                    : SColor.lerpFloatColors(
                            BIOME_COLOR_TABLE[56<<2], coastalColor,
                            a));
                }
                else if(hc == 4)
                    Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(icy ? BIOME_COLOR_TABLE[0] : SColor.lerpFloatColors(BIOME_DARK_COLOR_TABLE[34<<2], BIOME_COLOR_TABLE[41<<2],
                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat))),
                            SColor.lerpFloatColors(icy ? ice : SColor.lerpFloatColors(rocky, desertAlt,
                                    (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat))),
                                    icy ? lightIce : SColor.lerpFloatColors(woodland, BIOME_COLOR_TABLE[28<<2],
                                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat))),
                                    (extreme((float) (moisture)))),
                            (float) ((elevation - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower))));
                else
                    Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(icy ? ice : SColor.lerpFloatColors(rocky, desertAlt,
                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat))),
                            icy ? lightIce : SColor.lerpFloatColors(woodland, BIOME_COLOR_TABLE[28<<2],
                                    (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat))),
                            (extreme((float) (moisture)))));
                pm.drawPixel(x, y, quantize(tempColor));
            }
        }
        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0, width >> 1, height >> 1);
        batch.end();
    }
    
    public int quantize(Color color)
    {
        // Full 8-bit RGBA channels. No limits on what colors can be displayed.
        //if((mode & 1) == 0) 
            return Color.rgba8888(color);

        // Limits red, green, and blue channels to only use 5 bits (32 values) instead of 8 (256 values).
        //return Color.rgba8888(color) & 0xF8F8F8FF;

        // 253 possible colors, including one all-zero transparent color. 6 possible red values (not bits), 7 possible
        // green values, 6 possible blue values, and the aforementioned fully-transparent black. White is 0xFFFFFFFF and
        // not some off-white value, but other than black (0x000000FF), grayscale values have non-zero saturation.
        // Could be made into a palette, and images that use this can be saved as GIF or in PNG-8 indexed mode.
        //return ((0xFF000000 & (int)(color.r*6) * 0x2AAAAAAA) | (0xFF0000 & (int)(color.g*7) * 0x249249) | (0xFF00 & (int)(color.b*6) * 0x2AAA) | 255) & -(int)(color.a + 0.5f);
        //return SColor.quantize253I(color);
        //return (redLUT[(int)(color.r*31.999f)] | greenLUT[(int)(color.g*31.999f)] | blueLUT[(int)(color.b*31.999f)] | 255) & -(int)(color.a + 0.5f);
    }
    
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(SColor.DB_INK.r, SColor.DB_INK.g, SColor.DB_INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        season = ((System.currentTimeMillis() & 0xfffff) * 0x1p-11f) % 4f;
        if(spinning) 
            rotate();
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        switch (mode)
        {
            /*
            case 3: putHeatMap();
            break;
            case 2: putMoistureMap();
            break;
            */
            case 2:
            case 3: putExperimentMap();
            break;
            default: putMap();
            break;
        }
        ++counter;//nation = NumberTools.swayTight(++counter * 0.0125f);
        Gdx.graphics.setTitle("Took " + ttg + " ms to generate");

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        // stage has its own batch and must be explicitly told to draw().
//        stage.draw();
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
        config.width = width>> 1;
        config.height = height>> 1;
        config.foregroundFPS = 40;
        //config.fullscreen = true;
        config.backgroundFPS = -1;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DetailedWorldMapDemo(), config);
    }
}