package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidMouse;
import squidpony.squidgrid.gui.gdx.SquidPanel;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.*;

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
        Empty                  = 13;

    //private static final int width = 314 * 5, height = 500;
    private static final int width = 1000, height = 500;
    //private static final int width = 650, height = 650;

    private SpriteBatch batch;
    private SquidPanel display;//, overlay;
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private WorldMapGenerator.EllipticalMap world;
    private final double[][] shadingData = new double[width][height];
    private final int[][]
            heatCodeData = new int[width][height],
            moistureCodeData = new int[width][height],
            biomeUpperCodeData = new int[width][height],
            biomeLowerCodeData = new int[width][height];
    private Noise.Noise4D cloudNoise;
    //private final float[][][] cloudData = new float[128][128][128];
    private long counter = 0;
    private boolean cloudy = false;
    private long ttg = 0; // time to generate
    public static final double
            coldestValueLower = 0.0,   coldestValueUpper = 0.15, // 0
            colderValueLower = 0.15,   colderValueUpper = 0.31,  // 1
            coldValueLower = 0.31,     coldValueUpper = 0.5,     // 2
            warmValueLower = 0.5,      warmValueUpper = 0.69,     // 3
            warmerValueLower = 0.69,    warmerValueUpper = 0.85,   // 4
            warmestValueLower = 0.85,   warmestValueUpper = 1.0,  // 5

            driestValueLower = 0.0,    driestValueUpper  = 0.27, // 0
            drierValueLower = 0.27,    drierValueUpper   = 0.4,  // 1
            dryValueLower = 0.4,       dryValueUpper     = 0.6,  // 2
            wetValueLower = 0.6,       wetValueUpper     = 0.8,  // 3
            wetterValueLower = 0.8,    wetterValueUpper  = 0.9,  // 4
            wettestValueLower = 0.9,   wettestValueUpper = 1.0;  // 5

    private static float black = SColor.floatGetI(0, 0, 0),
            white = SColor.floatGet(0xffffffff);
    // Biome map colors

    private static float ice = SColor.ALICE_BLUE.toFloatBits();
    private static float darkIce = SColor.lerpFloatColors(ice, black, 0.15f);
    private static float lightIce = white;

    private static float desert = SColor.floatGetI(248, 229, 180);
    private static float darkDesert = SColor.lerpFloatColors(desert, black, 0.15f);

    private static float savanna = SColor.floatGetI(181, 200, 100);
    private static float darkSavanna = SColor.lerpFloatColors(savanna, black, 0.15f);

    private static float tropicalRainforest = SColor.floatGetI(66, 123, 25);
    private static float darkTropicalRainforest = SColor.lerpFloatColors(tropicalRainforest, black, 0.15f);

    private static float tundra = SColor.floatGetI(151, 175, 159);
    private static float darkTundra = SColor.lerpFloatColors(tundra, black, 0.15f);

    private static float temperateRainforest = SColor.floatGetI(54, 113, 60);
    private static float darkTemperateRainforest = SColor.lerpFloatColors(temperateRainforest, black, 0.15f);

    private static float grassland = SColor.floatGetI(169, 185, 105);
    private static float darkGrassland = SColor.lerpFloatColors(grassland, black, 0.15f);

    private static float seasonalForest = SColor.floatGetI(100, 158, 75);
    private static float darkSeasonalForest = SColor.lerpFloatColors(seasonalForest, black, 0.15f);

    private static float borealForest = SColor.floatGetI(75, 105, 45);
    private static float darkBorealForest = SColor.lerpFloatColors(borealForest, black, 0.15f);

    private static float woodland = SColor.floatGetI(122, 170, 90);
    private static float darkWoodland = SColor.lerpFloatColors(woodland, black, 0.15f);

    private static float rocky = SColor.floatGetI(171, 175, 145);
    private static float darkRocky = SColor.lerpFloatColors(rocky, black, 0.15f);

    private static float beach = SColor.floatGetI(255, 235, 180);
    private static float darkBeach = SColor.lerpFloatColors(beach, black, 0.15f);

    // water colors
    private static float deepColor = SColor.floatGetI(0, 68, 128);
    private static float darkDeepColor = SColor.lerpFloatColors(deepColor, black, 0.15f);
    private static float mediumColor = SColor.floatGetI(0, 89, 159);
    private static float darkMediumColor = SColor.lerpFloatColors(mediumColor, black, 0.15f);
    private static float shallowColor = SColor.floatGetI(0, 123, 167);
    private static float darkShallowColor = SColor.lerpFloatColors(shallowColor, black, 0.15f);
    private static float coastalColor = SColor.lerpFloatColors(shallowColor, white, 0.3f);
    private static float darkCoastalColor = SColor.lerpFloatColors(coastalColor, black, 0.15f);
    private static float foamColor = SColor.floatGetI(61,  162, 215);
    private static float darkFoamColor = SColor.lerpFloatColors(foamColor, black, 0.15f);
    private static float emptyColor = SColor.DB_INK.toFloatBits();
    private static float iceWater = SColor.floatGetI(210, 255, 252);
    private static float coldWater = mediumColor;
    private static float riverWater = shallowColor;

    private static float riverColor = SColor.floatGetI(30, 120, 200);
    private static float sandColor = SColor.floatGetI(240, 240, 64);
    private static float grassColor = SColor.floatGetI(50, 220, 20);
    private static float forestColor = SColor.floatGetI(16, 160, 0);
    private static float rockColor = SColor.floatGetI(177, 167, 157);
    private static float snowColor = SColor.floatGetI(255, 255, 255);

    // Heat map colors
    private static float coldest = SColor.floatGetI(0, 255, 255);
    private static float colder = SColor.floatGetI(170, 255, 255);
    private static float cold = SColor.floatGetI(0, 229, 133);
    private static float warm = SColor.floatGetI(255, 255, 100);
    private static float warmer = SColor.floatGetI(255, 100, 0);
    private static float warmest = SColor.floatGetI(241, 12, 0);

    // Moisture map colors
    private static float dryest = SColor.floatGetI(255, 139, 17);
    private static float dryer = SColor.floatGetI(245, 245, 23);
    private static float dry = SColor.floatGetI(80, 255, 0);
    private static float wet = SColor.floatGetI(85, 255, 255);
    private static float wetter = SColor.floatGetI(20, 70, 255);
    private static float wettest = SColor.floatGetI(0, 0, 100);

    private static float cloudFull = SColor.floatGet(0xffffffff);

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
            foamColor//SColor.floatGetI(255, 40, 80)
    }, biomeDarkColors = {
            darkDesert,
            darkSavanna,
            darkTropicalRainforest,
            darkGrassland,
            darkWoodland,
            darkSeasonalForest,
            darkTemperateRainforest,
            darkBorealForest,
            darkTundra,
            darkIce,
            darkBeach,
            darkRocky,
            darkFoamColor//SColor.floatGetI(225, 10, 20)
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
        Empty
    }, BIOME_COLOR_TABLE = new float[55], BIOME_DARK_COLOR_TABLE = new float[55];

    static {
        float b, diff;
        for (int i = 0; i < 54; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lerpFloatColors(biomeColors[(int)b], white, diff)
                    : SColor.lerpFloatColors(biomeColors[(int)b], black, -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.lerpFloatColors(b, black, 0.08f);
        }
        BIOME_COLOR_TABLE[54] = BIOME_DARK_COLOR_TABLE[54] = emptyColor;
    }

    protected void makeBiomes() {
        final WorldMapGenerator world = this.world;
        final int[][] heightCodeData = world.heightCodeData;
        final double[][] heatData = world.heatData, moistureData = world.moistureData, heightData = world.heightData;
        int hc, mc, heightCode;
        double hot, moist, high, i_hot = 1.0 / this.world.maxHeat, fresh;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                heightCode = heightCodeData[x][y];
                if(heightCode == 1000) {
                    biomeUpperCodeData[x][y] = biomeLowerCodeData[x][y] = 54;
                    shadingData[x][y] = 0.0;
                    continue;
                }
                hot = heatData[x][y];
                moist = moistureData[x][y];
                high = heightData[x][y];
                fresh = world.freshwaterData[x][y];
                boolean isLake = world.generateRivers && heightCode >= 4 && fresh > 0.65 && fresh + moist * 2.35 > 2.75,//world.partialLakeData.contains(x, y) && heightCode >= 4,
                        isRiver = world.generateRivers && !isLake && heightCode >= 4 && fresh > 0.55 && fresh + moist * 2.2 > 2.15;//world.partialRiverData.contains(x, y) && heightCode >= 4;
                if (moist >= (wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2)) {
                    mc = 5;
                } else if (moist >= (wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2)) {
                    mc = 4;
                } else if (moist >= (wetValueUpper - (dryValueUpper - dryValueLower) * 0.2)) {
                    mc = 3;
                } else if (moist >= (dryValueUpper - (drierValueUpper - drierValueLower) * 0.2)) {
                    mc = 2;
                } else if (moist >= (drierValueUpper - (driestValueUpper) * 0.2)) {
                    mc = 1;
                } else {
                    mc = 0;
                }

                if (hot >= (warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
                    hc = 5;
                } else if (hot >= (warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
                    hc = 4;
                } else if (hot >= (warmValueUpper - (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
                    hc = 3;
                } else if (hot >= (coldValueUpper - (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
                    hc = 2;
                } else if (hot >= (colderValueUpper - (coldestValueUpper) * 0.2) * i_hot) {
                    hc = 1;
                } else {
                    hc = 0;
                }

                heatCodeData[x][y] = hc;
                moistureCodeData[x][y] = mc;
                biomeUpperCodeData[x][y] = isLake ? hc + 48 : (isRiver ? hc + 42 : ((heightCode == 4) ? hc + 36 : hc + mc * 6));

                if (moist >= (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2)) {
                    mc = 5;
                } else if (moist >= (wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.2)) {
                    mc = 4;
                } else if (moist >= (dryValueUpper + (wetValueUpper - wetValueLower) * 0.2)) {
                    mc = 3;
                } else if (moist >= (drierValueUpper + (dryValueUpper - dryValueLower) * 0.2)) {
                    mc = 2;
                } else if (moist >= (driestValueUpper + (drierValueUpper - drierValueLower) * 0.2)) {
                    mc = 1;
                } else {
                    mc = 0;
                }

                if (hot >= (warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2) * i_hot) {
                    hc = 5;
                } else if (hot >= (warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
                    hc = 4;
                } else if (hot >= (coldValueUpper + (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
                    hc = 3;
                } else if (hot >= (colderValueUpper + (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
                    hc = 2;
                } else if (hot >= (coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
                    hc = 1;
                } else {
                    hc = 0;
                }

                biomeLowerCodeData[x][y] = hc + mc * 6;

                if (isRiver || isLake)
                    shadingData[x][y] = //((moist - minWet) / (maxWet - minWet)) * 0.45 + 0.15 - 0.14 * ((hot - minHeat) / (maxHeat - minHeat))
                            (moist * 0.35 + 0.6);
                else
                    shadingData[x][y] = //(upperProximityH + upperProximityM - lowerProximityH - lowerProximityM) * 0.1 + 0.2
                            (heightCode == 4) ? (0.18 - high) / (0.08) :
                                    NumberTools.bounce((high + moist) * (4.1 + high - hot)) * 0.5 + 0.5; // * (7.5 + moist * 1.9 - hot * 0.9)
            }
        }
        long seedA = ThrustAltRNG.determine(seed),
                seedB = ThrustAltRNG.determine(seed + seedA),
                seedC = ThrustAltRNG.determine(seed + seedA + seedB);
        counter = ThrustAltRNG.determine(seed + seedA + seedB + seedC) >>> 48;
        //Noise.seamless3D(cloudData, seedC, 3);
    }


    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new SquidPanel(width, height, cellWidth, cellHeight);
        view = new StretchViewport(width*cellWidth, height*cellHeight);
        stage = new Stage(view, batch);
        seed = 0xDEBACL;
        rng = new StatefulRNG(seed);
        //world = new WorldMapGenerator.SphereMapAlt(seed, width, height, WhirlingNoise.instance, 0.8);
        world = new WorldMapGenerator.EllipticalMap(seed, width, height, WhirlingNoise.instance, 0.8);
        //cloudNoise = new Noise.Turbulent4D(WhirlingNoise.instance, new Noise.Ridged4D(SeededNoise.instance, 2, 3.7), 3, 5.9);
        cloudNoise = new Noise.Layered4D(WhirlingNoise.instance, 2, 3.2);
        //cloudNoise2 = new Noise.Ridged4D(SeededNoise.instance, 3, 6.5);
        //world = new WorldMapGenerator.TilingMap(seed, width, height, WhirlingNoise.instance, 0.9);
        world.generateRivers = false;
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
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                Gdx.graphics.requestRendering();
            }
        }, new SquidMouse(1, 1, width, height, 0, 0, new InputAdapter()
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
        }));
        generate(seed);
        rng.setState(seed);
        Gdx.input.setInputProcessor(input);
        display.setPosition(0, 0);
        stage.addActor(display);
        Gdx.graphics.setContinuousRendering(true);
        Gdx.graphics.requestRendering();
    }

    public void zoomIn() {
        zoomIn(width >> 1, height >> 1);
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
        world.zoomIn(1, zoomX, zoomY);
        makeBiomes();
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
        makeBiomes();
        ttg = System.currentTimeMillis() - startTime;
    }
    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        world.generate(seed);
        makeBiomes();
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        // uncomment next line to generate maps as quickly as possible
        //generate(rng.nextLong());
        int hc, tc;
        int[][] heightCodeData = world.heightCodeData;
        double[][] heightData = world.heightData;
        double xp, yp, zp;
        float cloud = 0f, shown, cloudLight = 1f;
        for (int y = 0; y < height; y++) {
            PER_CELL:
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if(hc == 1000)
                    continue;
                tc = heatCodeData[x][y];
                //cloud = (float) cloudNoise2.getNoiseWithSeed(xp = world.xPositions[x][y], yp = world.yPositions[x][y],
                //        zp = world.zPositions[x][y], counter * 0.04, (int) seed) * 0.06f;
                if(cloudy) {
                    cloud = (float) Math.min(1f, (cloudNoise.getNoiseWithSeed(world.xPositions[x][y], world.yPositions[x][y], world.zPositions[x][y], counter * 0.0125, seed) * (0.75 + world.moistureData[x][y]) - 0.07));
                    cloudLight = 0.65f + NumberTools.swayTight(cloud * 1.4f + 0.55f) * 0.35f;
                    cloudLight = SColor.floatGet(cloudLight, cloudLight, cloudLight, 1f);
                }
                /*
                cloud = Math.min(1f,
                        cloudData[(int) (world.xPositions[x][y] * 109 + counter * 1.7) & 127]
                        [(int) (world.yPositions[x][y] * 109) & 127]
                        [(int) (world.zPositions[x][y] * 109) & 127] * 1.1f +
                        cloudData[(int) (world.xPositions[x][y] * 119) & 127]
                                [(int) (world.yPositions[x][y] * 119 + counter * 1.7) & 127]
                                [(int) (world.zPositions[x][y] * 119) & 127] * 1.4f);
                cloudLight = Math.min(1f, 1f +
                        cloudData[(int) (world.xPositions[x][y] * 233) & 127]
                                [(int) (world.yPositions[x][y] * 233) & 127]
                                [(int) (world.zPositions[x][y] * 233 + counter * 2.3) & 127] * 0.64f);
                cloudLight = SColor.floatGet(cloudLight, cloudLight, cloudLight, 1f);
                                */
                if(tc == 0)
                {
                    switch (hc)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            shown = SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0)));
                            if(cloud > 0.0)
                                shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
                            display.put(x, y, shown);
                            continue PER_CELL;
                        case 4:
                            shown = SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - 0.1) / (0.18 - 0.1)));
                            if(cloud > 0.0)
                                shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
                            display.put(x, y, shown);
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        shown = SColor.lerpFloatColors(deepColor, coastalColor,
                                (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0)));
                        if(cloud > 0.0)
                            shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
                        display.put(x, y, shown);
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
                        shown = SColor.lerpFloatColors(BIOME_COLOR_TABLE[biomeLowerCodeData[x][y]],
                                BIOME_DARK_COLOR_TABLE[biomeUpperCodeData[x][y]],
                                (float) shadingData[x][y]);
                        if(cloud > 0.0)
                            shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
                        display.put(x, y, shown);

                        //display.put(x, y, SColor.lerpFloatColors(darkTropicalRainforest, desert, (float) (heightData[x][y])));
                }
            }
        }
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(SColor.DB_INK.r, SColor.DB_INK.g, SColor.DB_INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        ++counter;
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");

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
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Detailed World Map";
        config.width = width * cellWidth;
        config.height = height * cellHeight;
        config.foregroundFPS = 60;
        //config.fullscreen = true;
        config.backgroundFPS = -1;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DetailedWorldMapDemo(), config);
    }
}