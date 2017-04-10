package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.*;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
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
        River                  = 12;

    private static final int width = 512, height = 512;
    private final double terrainFreq = 1.75, terrainRidgedFreq = 1.1, heatFreq = 4.5, moistureFreq = 4.0, otherFreq = 3.1;
    //riverSize = (0.5 - 10.0 / height) * (0.5 - 10.0 / width), lakeSize = riverSize * 1.1;

    private SpriteBatch batch;
    private SquidColorCenter colorFactory;
    private SquidPanel display;//, overlay;
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    private Stage stage;
    private Viewport view;
    private int zoom = 0;
    private Noise.Noise4D terrain, terrainRidged, heat, moisture, otherRidged;
    private long seed, cachedState;
    private StatefulRNG rng;
    private IntVLA startCacheX = new IntVLA(8), startCacheY = new IntVLA(8);
    //private GridData data;
    private double[][] heightData = new double[width][height],
            heatData = new double[width][height],
            moistureData = new double[width][height],
            shadingData = new double[width][height];
    private GreasedRegion riverData = new GreasedRegion(width, height), lakeData = new GreasedRegion(width, height),
            partialRiverData = new GreasedRegion(width, height), partialLakeData = new GreasedRegion(width, height),
            workingData = new GreasedRegion(width, height);

    private int[][] heightCodeData = new int[width][height],
            heatCodeData = new int[width][height],
            moistureCodeData = new int[width][height],
            biomeUpperCodeData = new int[width][height],
            biomeLowerCodeData = new int[width][height];
    public double waterModifier = 0.0, coolingModifier = 1.0,
            minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY,
            minHeightActual = Double.POSITIVE_INFINITY, maxHeightActual = Double.NEGATIVE_INFINITY,
            minHeat = Double.POSITIVE_INFINITY, maxHeat = Double.NEGATIVE_INFINITY,
            minWet = Double.POSITIVE_INFINITY, maxWet = Double.NEGATIVE_INFINITY;
    private double i_hot = 1.0,
            minHeightActual0 = Double.POSITIVE_INFINITY, maxHeightActual0 = Double.NEGATIVE_INFINITY,
            minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
            minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
            minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

    private long ttg = 0; // time to generate

    public static final double
            deepWaterLower = -1.0, deepWaterUpper = -0.7,        // 0
            mediumWaterLower = -0.7, mediumWaterUpper = -0.3,    // 1
            shallowWaterLower = -0.3, shallowWaterUpper = -0.1,  // 2
            coastalWaterLower = -0.1, coastalWaterUpper = 0.1,   // 3
            sandLower = 0.1, sandUpper = 0.18,                   // 4
            grassLower = 0.18, grassUpper = 0.35,                // 5
            forestLower = 0.35, forestUpper = 0.6,               // 6
            rockLower = 0.6, rockUpper = 0.8,                    // 7
            snowLower = 0.8, snowUpper = 1.0;                    // 8

    public static final double[] lowers = {deepWaterLower, mediumWaterLower, shallowWaterLower, coastalWaterLower,
            sandLower, grassLower, forestLower, rockLower, snowLower},
            uppers = {deepWaterUpper, mediumWaterUpper, shallowWaterUpper, coastalWaterUpper,
                    sandUpper, grassUpper, forestUpper, rockUpper, snowUpper},
            differences = {deepWaterUpper - deepWaterLower, mediumWaterUpper - mediumWaterLower,
            shallowWaterUpper - shallowWaterLower, coastalWaterUpper - coastalWaterLower, sandUpper - sandLower,
                    grassUpper - grassLower, forestUpper - forestLower, rockUpper - rockLower, snowUpper - snowLower};



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

    private static SquidColorCenter squidColorCenter = new SquidColorCenter();

    private static float black = SColor.floatGetI(0, 0, 0),
            white = SColor.floatGet(0xffffffff);
    // Biome map colors

    private static float ice = SColor.ALICE_BLUE.toFloatBits();
    private static float darkIce = SColor.lerpFloatColors(ice, black, 0.15f);
    private static float lightIce = white;

    private static float desert = SColor.floatGetI(248, 229, 180);
    private static float darkDesert = SColor.lerpFloatColors(desert, black, 0.15f);

    private static float savanna = SColor.floatGetI(177, 209, 110);
    private static float darkSavanna = SColor.lerpFloatColors(savanna, black, 0.15f);

    private static float tropicalRainforest = SColor.floatGetI(66, 123, 25);
    private static float darkTropicalRainforest = SColor.lerpFloatColors(tropicalRainforest, black, 0.15f);

    private static float tundra = SColor.floatGetI(151, 175, 159);
    private static float darkTundra = SColor.lerpFloatColors(tundra, black, 0.15f);

    private static float temperateRainforest = SColor.floatGetI(29, 73, 40);
    private static float darkTemperateRainforest = SColor.lerpFloatColors(temperateRainforest, black, 0.15f);

    private static float grassland = SColor.floatGetI(170, 195, 119);
    private static float darkGrassland = SColor.lerpFloatColors(grassland, black, 0.15f);

    private static float seasonalForest = SColor.floatGetI(100, 158, 75);
    private static float darkSeasonalForest = SColor.lerpFloatColors(seasonalForest, black, 0.15f);

    private static float borealForest = SColor.floatGetI(95, 115, 62);
    private static float darkBorealForest = SColor.lerpFloatColors(borealForest, black, 0.15f);

    private static float woodland = SColor.floatGetI(139, 175, 90);
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

    public static int codeHeight(final double high)
    {
        if(high < deepWaterUpper)
            return 0;
        if(high < mediumWaterUpper)
            return 1;
        if(high < shallowWaterUpper)
            return 2;
        if(high < coastalWaterUpper)
            return 3;
        if(high < sandUpper)
            return 4;
        if(high < grassUpper)
            return 5;
        if(high < forestUpper)
            return 6;
        if(high < rockUpper)
            return 7;
        return 8;
    }

    protected final static float[] BIOME_TABLE = {
        //COLDEST   //COLDER      //COLD               //HOT                     //HOTTER                 //HOTTEST
        Ice+0.7f,   Ice+0.65f,    Grassland+0.8f,      Desert+0.75f,             Desert+0.8f,             Desert+0.85f,            //DRYEST
        Ice+0.6f,   Tundra+0.9f,  Grassland+0.6f,      Grassland+0.4f,           Desert+0.65f,            Desert+0.7f,             //DRYER
        Ice+0.5f,   Tundra+0.7f,  Woodland+0.5f,       Woodland+0.6f,            Savanna+0.65f,           Desert+0.6f,             //DRY
        Ice+0.4f,   Tundra+0.5f,  SeasonalForest+0.3f, SeasonalForest+0.5f,      Savanna+0.4f,            Savanna+0.55f,           //WET
        Ice+0.2f,   Tundra+0.3f,  BorealForest+0.35f,  TemperateRainforest+0.4f, TropicalRainforest+0.5f, Savanna+0.3f,            //WETTER
        Ice+0.0f,   BorealForest, BorealForest+0.15f,  TemperateRainforest+0.2f, TropicalRainforest+0.3f, TropicalRainforest+0.1f, //WETTEST
        Rocky+0.9f, Rocky+0.6f,   Beach+0.4f,          Beach+0.55f,              Beach+0.75f,             Beach+0.9f,              //COASTS
        Ice+0.3f,   River+0.8f,   River+0.7f,          River+0.6f,               River+0.5f,              River+0.4f,              //RIVERS
        Ice+0.2f,   River+0.7f,   River+0.6f,          River+0.5f,               River+0.4f,              River+0.3f,              //LAKES
    }, BIOME_COLOR_TABLE = new float[54], BIOME_DARK_COLOR_TABLE = new float[54];

    static {
        float b, diff;
        for (int i = 0; i < 54; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.23f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lerpFloatColors(biomeColors[(int)b], white, diff)
                    : SColor.lerpFloatColors(biomeColors[(int)b], black, -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.lerpFloatColors(b, black, 0.07f);
        }
    }

    protected void codeBiome(int x, int y, double hot, double moist, int heightCode) {
        int hc, mc;
        boolean isLake = partialLakeData.contains(x, y) && heightCode >= 4,
                isRiver = partialRiverData.contains(x, y) && heightCode >= 4;
        if(moist >= (wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2))
        {
            mc = 5;
        }
        else if(moist >= (wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2))
        {
            mc = 4;
        }
        else if(moist >= (wetValueUpper - (dryValueUpper - dryValueLower) * 0.2))
        {
            mc = 3;
        }
        else if(moist >= (dryValueUpper - (drierValueUpper - drierValueLower) * 0.2))
        {
            mc = 2;
        }
        else if(moist >= (drierValueUpper - (driestValueUpper) * 0.2))
        {
            mc = 1;
        }
        else
        {
            mc = 0;
        }

        if(hot >= (warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2) * i_hot)
        {
            hc = 5;
        }
        else if(hot >= (warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2) * i_hot)
        {
            hc = 4;
        }
        else if(hot >= (warmValueUpper - (coldValueUpper - coldValueLower) * 0.2) * i_hot)
        {
            hc = 3;
        }
        else if(hot >= (coldValueUpper - (colderValueUpper - colderValueLower) * 0.2) * i_hot)
        {
            hc = 2;
        }
        else if(hot >= (colderValueUpper - (coldestValueUpper) * 0.2) * i_hot)
        {
            hc = 1;
        }
        else
        {
            hc = 0;
        }

        heatCodeData[x][y] = hc;
        moistureCodeData[x][y] = mc;
        biomeUpperCodeData[x][y] = isLake? hc + 48 : (isRiver ? hc + 42 : ((heightCode == 4) ? hc + 36 : hc + mc * 6));

        if(moist >= (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2))
        {
            mc = 5;
        }
        else if(moist >= (wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.2))
        {
            mc = 4;
        }
        else if(moist >= (dryValueUpper + (wetValueUpper - wetValueLower) * 0.2))
        {
            mc = 3;
        }
        else if(moist >= (drierValueUpper + (dryValueUpper - dryValueLower) * 0.2))
        {
            mc = 2;
        }
        else if(moist >= (driestValueUpper + (drierValueUpper - drierValueLower) * 0.2))
        {
            mc = 1;
        }
        else
        {
            mc = 0;
        }

        if(hot >= (warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2) * i_hot)
        {
            hc = 5;
        }
        else if(hot >= (warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2) * i_hot)
        {
            hc = 4;
        }
        else if(hot >= (coldValueUpper + (warmValueUpper - warmValueLower) * 0.2) * i_hot)
        {
            hc = 3;
        }
        else if(hot >= (colderValueUpper + (coldValueUpper - coldValueLower) * 0.2) * i_hot)
        {
            hc = 2;
        }
        else if(hot >= (coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2) * i_hot)
        {
            hc = 1;
        }
        else
        {
            hc = 0;
        }

        biomeLowerCodeData[x][y] = hc + mc * 6;

        if(isRiver || isLake)
            shadingData[x][y] = //((moist - minWet) / (maxWet - minWet)) * 0.45 + 0.15 - 0.14 * ((hot - minHeat) / (maxHeat - minHeat))
                    + (NumberTools.bounce(heightData[x][y] * (32 + moist * 16 - hot * 4)) * 0.3 + 0.6);
        else
            shadingData[x][y] = //(upperProximityH + upperProximityM - lowerProximityH - lowerProximityM) * 0.1 + 0.2
                    + NumberTools.bounce(heightData[x][y] * (71 + moist * 11 - hot * 5)) * 0.5 + 0.5;
    }


    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new SquidPanel(width, height, cellWidth, cellHeight);
        //overlay = new SquidPanel(16, 8, DefaultResources.getStretchableFont().width(32).height(64).initBySize());
        colorFactory = new SquidColorCenter();
        view = new StretchViewport(width*cellWidth, height*cellHeight);
        stage = new Stage(view, batch);
        seed = 0xDEBACL;
        rng = new StatefulRNG(seed);
        // 1.9, 2.5, 5.5, 5.0, 4.1
        terrain = new Noise.Layered4D(SeededNoise.instance, 10, terrainFreq);
        terrainRidged = new Noise.Ridged4D(SeededNoise.instance, 9, terrainRidgedFreq);
        heat = new Noise.Layered4D(SeededNoise.instance, 4, heatFreq);
        moisture = new Noise.Layered4D(SeededNoise.instance, 5, moistureFreq);
        otherRidged = new Noise.Ridged4D(SeededNoise.instance, 4, otherFreq);
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        seed = rng.nextLong();
                        startCacheX.clear();
                        startCacheY.clear();
                        startCacheX.add((width >> 1) - (width >> zoom+1));
                        startCacheY.add((height >> 1) - (height >> zoom+1));
                        regenerate(seed);
                        rng.setState(seed);
                        break;
                    case '=':
                    case '+':
                        if(zoom < 7)
                        {
                            zoom++;
                            cachedState = rng.getState();

                            if(startCacheX.isEmpty())
                            {
                                startCacheX.add(0);
                                startCacheY.add(0);
                            }
                            else {
                                startCacheX.add(MathUtils.clamp(startCacheX.peek() + (width >> zoom) - (width >> zoom + 1),
                                        0, width - (width >> zoom)));
                                startCacheY.add(MathUtils.clamp(startCacheY.peek() + (height >> zoom) - (height >> zoom + 1),
                                        0, height - (height >> zoom)));
                            }
                            regenerate(startCacheX.peek(), startCacheY.peek(),width >> zoom, height >> zoom, cachedState);
                            /*
                            startCacheX.add((width >> 1) - (width >> zoom+1));
                            startCacheY.add((height >> 1) - (height >> zoom+1));
                            regenerate(cachedState);
                            */
                            rng.setState(cachedState);
                        }
                        break;
                    case '-':
                    case '_':
                        if(zoom > 0) {
                            zoom--;
                            cachedState = rng.getState();
                            startCacheX.pop();
                            startCacheY.pop();
                            startCacheX.add(MathUtils.clamp(startCacheX.pop() + (width >> zoom + 1) - (width >> zoom + 1),
                                    0, width - (width >> zoom)));
                            startCacheY.add(MathUtils.clamp(startCacheY.pop() + (height >> zoom + 1) - (height >> zoom + 1),
                                    0, height - (height >> zoom)));
                            regenerate(startCacheX.peek(), startCacheY.peek(),width >> zoom, height >> zoom, cachedState);
                            /*
                            startCacheX.pop();
                            startCacheY.pop();
                            regenerate(cachedState);
                            */
                            rng.setState(cachedState);
                        }
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
                    if(zoom > 0)
                    {
                        zoom--;
                        cachedState = rng.getState();

                        startCacheX.pop();
                        startCacheY.pop();
                        startCacheX.add(MathUtils.clamp(startCacheX.pop() + (screenX >> zoom + 1) - (width >> zoom + 2),
                                0, width - (width >> zoom)));
                        startCacheY.add(MathUtils.clamp(startCacheY.pop() + (screenY >> zoom + 1) - (height >> zoom + 2),
                                0, height - (height >> zoom)));
                        regenerate(startCacheX.peek(), startCacheY.peek(),width >> zoom, height >> zoom, cachedState);
                        rng.setState(cachedState);

                    }
                }
                else
                {
                    if(zoom < 7)
                    {
                        zoom++;
                        cachedState = rng.getState();
                        if(startCacheX.isEmpty())
                        {
                            startCacheX.add(0);
                            startCacheY.add(0);
                        }
                        else {
                            startCacheX.add(MathUtils.clamp(startCacheX.peek() + (screenX >> zoom - 1) - (width >> zoom + 1),
                                    0, width - (width >> zoom)));
                            startCacheY.add(MathUtils.clamp(startCacheY.peek() + (screenY >> zoom - 1) - (height >> zoom + 1),
                                    0, height - (height >> zoom)));
                        }
                        regenerate(startCacheX.peek(), startCacheY.peek(),width >> zoom, height >> zoom, cachedState);
                        rng.setState(cachedState);
                    }
                }
                return true;
            }
        }));
        cachedState = ~seed;
        startCacheX.add(0);
        startCacheY.add(0);
        regenerate(seed);
        rng.setState(seed);
        Gdx.input.setInputProcessor(input);
        display.setPosition(0, 0);
        stage.addActor(display);
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }
    public void regenerate(final long state) {
        if(cachedState != state)
        {
            zoom = 0;
            startCacheX.clear();
            startCacheY.clear();
            startCacheX.add(0);
            startCacheY.add(0);

        }
        regenerate(startCacheX.peek(), startCacheY.peek(),
                (width >> zoom), (height >> zoom), state);
    }
    public void regenerate(int startX, int startY, int usedWidth, int usedHeight, long state)
    {
        long startTime = System.currentTimeMillis();
        boolean fresh = false;
        if(cachedState != state)
        {
            minHeight = Double.POSITIVE_INFINITY;
            maxHeight = Double.NEGATIVE_INFINITY;
            minHeat0 = Double.POSITIVE_INFINITY;
            maxHeat0 = Double.NEGATIVE_INFINITY;
            minHeat1 = Double.POSITIVE_INFINITY;
            maxHeat1 = Double.NEGATIVE_INFINITY;
            minHeat = Double.POSITIVE_INFINITY;
            maxHeat = Double.NEGATIVE_INFINITY;
            minWet0 = Double.POSITIVE_INFINITY;
            maxWet0 = Double.NEGATIVE_INFINITY;
            minWet = Double.POSITIVE_INFINITY;
            maxWet = Double.NEGATIVE_INFINITY;
            cachedState = state;
            fresh = true;
        }
        rng.setState(state);
        int seedA = rng.nextInt(), seedB = rng.nextInt(), seedC = rng.nextInt(), t;
        waterModifier = rng.nextDouble(0.13)-0.06;
        coolingModifier = rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1;

        double p, q,
                ps, pc,
                qs, qc,
                h, temp,
                i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height,
                xPos = startX, yPos = startY, i_uw = usedWidth / (double)width, i_uh = usedHeight / (double)height;
        double[] trigTable = new double[width << 1];
        for (int x = 0; x < width; x++, xPos += i_uw) {
            p = xPos * i_w;
            trigTable[x<<1]   = Math.sin(p);
            trigTable[x<<1|1] = Math.cos(p);
        }
        for (int y = 0; y < height; y++, yPos += i_uh) {
            q = yPos * i_h;
            qs = Math.sin(q);
            qc = Math.cos(q);
            for (int x = 0, xt = 0; x < width; x++) {
                ps = trigTable[xt++];//Math.sin(p);
                pc = trigTable[xt++];//Math.cos(p);
                h = terrain.getNoiseWithSeed(pc +
                                terrainRidged.getNoiseWithSeed(pc, ps, qc, qs, seedA + seedB),
                        ps, qc, qs, seedA);
                p = Math.signum(h) + waterModifier;
                h *= p * p;
                heightData[x][y] = h;
                heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps, qc
                        + otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedB + seedC)//, seedD + seedC)
                        , qs, seedB));
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qc, qs
                        + otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedC + seedA)//seedD + seedB)
                        , seedC));
                minHeightActual = Math.min(minHeightActual, h);
                maxHeightActual = Math.max(maxHeightActual, h);
                if(fresh) {
                    minHeight = Math.min(minHeight, h);
                    maxHeight = Math.max(maxHeight, h);

                    minHeat0 = Math.min(minHeat0, p);
                    maxHeat0 = Math.max(maxHeat0, p);

                    minWet0 = Math.min(minWet0, temp);
                    maxWet0 = Math.max(maxWet0, temp);

                }
            }
            minHeightActual = Math.min(minHeightActual, minHeight);
            maxHeightActual = Math.max(maxHeightActual, maxHeight);

        }
        double heightDiff = 2.0 / (maxHeightActual - minHeightActual),
                heatDiff = 0.8 / (maxHeat0 - minHeat0),
                wetDiff = 1.0 / (maxWet0 - minWet0),
                hMod,
                halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
        minHeightActual0 = minHeightActual;
        maxHeightActual0 = maxHeightActual;
        yPos = startY;
        ps = Double.POSITIVE_INFINITY;
        pc = Double.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = Math.abs(yPos - halfHeight) * i_half;
            temp *= (2.4 - temp);
            temp = 2.2 - temp;
            for (int x = 0; x < width; x++) {
                heightData[x][y] = (h = (heightData[x][y] - minHeightActual) * heightDiff - 1.0);
                minHeightActual0 = Math.min(minHeightActual0, h);
                maxHeightActual0 = Math.max(maxHeightActual0, h);
                heightCodeData[x][y] = (t = codeHeight(h));
                hMod = 1.0;
                switch (t) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        h = 0.4;
                        hMod = 0.2;
                        break;
                    case 6:
                        h = -0.1 * (h - forestLower - 0.08);
                        break;
                    case 7:
                        h *= -0.25;
                        break;
                    case 8:
                        h *= -0.4;
                        break;
                    default:
                        h *= 0.05;
                }
                heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                if (fresh) {
                    ps = Math.min(ps, h); //minHeat0
                    pc = Math.max(pc, h); //maxHeat0
                }
            }
        }
        if(fresh)
        {
            minHeat1 = ps;
            maxHeat1 = pc;
        }
        heatDiff = coolingModifier / (maxHeat1 - minHeat1);
        qs = Double.POSITIVE_INFINITY;
        qc = Double.NEGATIVE_INFINITY;
        ps = Double.POSITIVE_INFINITY;
        pc = Double.NEGATIVE_INFINITY;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                //(h = Math.pow(h, 2.0 - h * 2.0));
                moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff); // may assign to temp?
                if (fresh) {
                    qs = Math.min(qs, h);
                    qc = Math.max(qc, h);
                    ps = Math.min(ps, temp);
                    pc = Math.max(pc, temp);
                }
            }
        }
        if(fresh)
        {
            minHeat = qs;
            maxHeat = qc;
            minWet = ps;
            maxWet = pc;
            i_hot =  1.0 / qc;
        }
        if(fresh)
        {
            addRivers();
            riverData.connect8way().thin().thin();
            partialRiverData.remake(riverData);
            partialLakeData.remake(lakeData);
        }
        else {
            partialRiverData.remake(riverData);
            partialLakeData.remake(lakeData);
            for (int i = 1; i <= zoom; i++) {
                int stx = (startCacheX.get(i) - startCacheX.get(i-1)) << (i - 1),
                        sty = (startCacheY.get(i) - startCacheY.get(i-1)) << (i - 1);
                if ((i & 3) == 3) {
                    partialRiverData.zoom(stx, sty).connect8way();//.connect8way();//.connect();
                    partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.4));
                    partialLakeData.zoom(stx, sty).connect8way();//.connect8way();//.connect();
                    partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.55));
                }
                else
                {
                    partialRiverData.zoom(stx, sty).connect8way().thin();//.connect8way();//.connect();
                    partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.5));
                    partialLakeData.zoom(stx, sty).connect8way().thin();//.connect8way();//.connect();
                    partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.7));
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                codeBiome(x, y, heatData[x][y], moistureData[x][y], heightCodeData[x][y]);
            }
        }
        /*
        data.putDoubles("height", heightData);
        data.putDoubles("heat", heatData);
        data.putDoubles("moisture", moistureData);
        */
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        //regenerate(0, 0, width, height, rng.getState());
        display.erase();
        int hc, tc;
        for (int y = 0; y < height; y++) {
            PER_CELL:
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                tc = heatCodeData[x][y];
                if(tc == 0)
                {
                    switch (hc)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            display.put(x, y, SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - deepWaterLower) / (coastalWaterUpper - deepWaterLower))));
                            continue PER_CELL;
                        case 4:
                            display.put(x, y, SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - lowers[hc]) / (differences[hc]))));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        display.put(x, y, SColor.lerpFloatColors(deepColor, coastalColor,
                                (float) ((heightData[x][y] - deepWaterLower) / (coastalWaterUpper - deepWaterLower))));
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
                        display.put(x, y, SColor.lerpFloatColors(BIOME_COLOR_TABLE[biomeLowerCodeData[x][y]],
                                BIOME_DARK_COLOR_TABLE[biomeUpperCodeData[x][y]],
                                (float) //(((heightData[x][y] - lowers[hc]) / (differences[hc])) * 11 +
                                        shadingData[x][y]// * 21) * 0.03125f
                                ));
                }
            }
        }
    }
    private int encode(final int x, final int y)
    {
        return width * y + x;
    }
    private int decodeX(final int coded)
    {
        return coded % width;
    }
    private int decodeY(final int coded)
    {
        return coded / width;
    }
    private static final Direction[] reuse = new Direction[9];
    private void appendDirToShuffle(RNG rng) {
        rng.randomPortion(Direction.CARDINALS, reuse);
        reuse[rng.next(2)] = Direction.DIAGONALS[rng.next(2)];
        reuse[4] = Direction.DIAGONALS[rng.next(2)];
        //int diags = rng.next(2);
        /*
        int diags = rng.nextIntHasty(12);
        reuse[rng.next(2)] = Direction.DIAGONALS[diags & 3];
        reuse[4] = Direction.DIAGONALS[(diags & 3) + 1 + (diags >> 2) & 3];
        */
        //reuse[4] = Direction.NONE;
    }

    private void addRivers()
    {
        long rebuildState = rng.nextLong();
        //sortaDijkstra();
        /*int current = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

            }
        }*/
        workingData.empty().insertRectangle(8, 8, width - 16, height - 16);
        riverData.empty().refill(heightCodeData, 6, 100);
        //System.out.println(riverData.show('.', '#'));
        //System.out.println('\n');
        riverData.quasiRandomRegion(0.000003 * Math.max(width, height)).and(workingData);
        //System.out.println(riverData.show('.', '#'));
        //System.out.println('\n');
        int[] starts = riverData.asTightEncoded();
        int len = starts.length, currentPos, choice, adjX, adjY, currX, currY, tcx, tcy, stx, sty, sbx, sby;
        riverData.clear();
        lakeData.clear();
        PER_RIVER:
        for (int i = 0; i < len; i++) {
            workingData.clear();
            currentPos = starts[i];
            stx = tcx = currX = decodeX(currentPos);
            sty = tcy = currY = decodeY(currentPos);
            while (true) {

                double best = 999999;
                choice = -1;
                appendDirToShuffle(rng);

                for (int d = 0; d < 5; d++) {
                    adjX = (currX + reuse[d].deltaX);
                    if (adjX < 0 || adjX >= width)
                    {
                        if(rng.next(4) == 0)
                            riverData.or(workingData);
                        continue PER_RIVER;
                    }
                    adjY = (currY + reuse[d].deltaY);
                    if (adjY < 0 || adjY >= height)
                    {
                        if(rng.next(4) == 0)
                            riverData.or(workingData);
                        continue PER_RIVER;
                    }
                    if (heightData[adjX][adjY] < best && !workingData.contains(adjX, adjY)) {
                        best = heightData[adjX][adjY];
                        choice = d;
                        tcx = adjX;
                        tcy = adjY;
                    }
                }
                currX = tcx;
                currY = tcy;
                if (best >= heightData[stx][sty]) {
                    tcx = rng.next(2);
                    adjX = currX + ((tcx & 1) << 1) - 1;
                    adjY = currY + (tcx & 2) - 1;
                    lakeData.insert(currX, currY);
                    lakeData.insert(currX + 1, currY);
                    lakeData.insert(currX - 1, currY);
                    lakeData.insert(currX, currY + 1);
                    lakeData.insert(currX, currY - 1);
                    if (!(adjY < 0 || adjY >= height || adjX < 0 || adjX >= width))
                    {
                        if(heightCodeData[adjX][adjY] <= 3) {
                            riverData.or(workingData);
                            continue PER_RIVER;
                        }
                        else if((heightData[adjX][adjY] -= 0.0002) < lowers[heightCodeData[adjX][adjY]-1])
                        {
                            if(rng.next(3) == 0)
                                riverData.or(workingData);
                            continue PER_RIVER;
                        }
                    }
                    else
                    {
                        if(rng.next(5) == 0)
                            riverData.or(workingData);
                        continue PER_RIVER;
                    }
                    tcx = rng.next(2);
                    adjX = currX + ((tcx & 1) << 1) - 1;
                    adjY = currY + (tcx & 2) - 1;
                    if (!(adjY < 0 || adjY >= height || adjX < 0 || adjX >= width))
                    {
                        if(heightCodeData[adjX][adjY] <= 3) {
                            riverData.or(workingData);
                            continue PER_RIVER;
                        }
                        else if((heightData[adjX][adjY] -= 0.0002) < lowers[heightCodeData[adjX][adjY]-1])
                        {
                            if(rng.next(3) == 0)
                                riverData.or(workingData);
                            continue PER_RIVER;
                        }
                    }
                    else
                    {
                        if(rng.next(5) == 0)
                            riverData.or(workingData);
                        continue PER_RIVER;
                    }

                }
                if(choice != -1 && reuse[choice].isDiagonal())
                {
                    tcx = currX - reuse[choice].deltaX;
                    tcy = currY - reuse[choice].deltaY;
                    if(heightData[tcx][currY] <= heightData[currX][tcy] && !workingData.contains(tcx, currY))
                    {
                        if(riverData.contains(tcx, currY))
                        {
                            riverData.or(workingData);
                            continue PER_RIVER;
                        }
                        workingData.insert(tcx, currY);
                        if (heightData[tcx][currY] <= 0.075)
                        {
                            riverData.or(workingData);
                            continue PER_RIVER;
                        }
                    }
                    else if(!workingData.contains(currX, tcy))
                    {
                        if(riverData.contains(currX, tcy))
                        {
                            riverData.or(workingData);
                            continue PER_RIVER;
                        }
                        workingData.insert(currX, tcy);
                        if (heightData[currX][tcy] <= 0.075)
                        {
                            riverData.or(workingData);
                            continue PER_RIVER;
                        }

                    }
                }
                if(riverData.contains(currX, currY))
                {
                    riverData.or(workingData);
                    continue PER_RIVER;
                }
                workingData.insert(currX, currY);
                if (heightData[currX][currY] <= 0.075)
                {
                    riverData.or(workingData);
                    continue PER_RIVER;
                }
            }
        }

        GreasedRegion tempData = new GreasedRegion(width, height);
        int riverCount = riverData.size() >> 5, currentMax = riverCount >> 2, idx = 0, prevChoice;
        for (int h = 5; h < 9; h++) { //, currentMax += riverCount / 18
            workingData.empty().refill(heightCodeData, h).and(riverData);
            //System.out.println(workingData.show('.', '#'));
            RIVER:
            for (int j = 0; j < currentMax && idx < riverCount; j++) {
                double vdc = VanDerCorputQRNG.weakDetermine(idx++), best = -999999;
                currentPos = workingData.atFractionTight(vdc);
                if(currentPos < 0)
                    break;
                stx = sbx = tcx = currX = decodeX(currentPos);
                sty = sby = tcy = currY = decodeY(currentPos);
                appendDirToShuffle(rng);
                choice = -1;
                prevChoice = -1;
                for (int d = 0; d < 5; d++) {
                    adjX = (currX + reuse[d].deltaX);
                    if (adjX < 0 || adjX >= width)
                        continue;
                    adjY = (currY + reuse[d].deltaY);
                    if (adjY < 0 || adjY >= height)
                        continue;
                    if (heightData[adjX][adjY] > best) {
                        best = heightData[adjX][adjY];
                        prevChoice = choice;
                        choice = d;
                        sbx = tcx;
                        sby = tcy;
                        tcx = adjX;
                        tcy = adjY;
                    }
                }
                currX = sbx;
                currY = sby;
                if (prevChoice != -1 && heightCodeData[currX][currY] >= 4) {
                    if (reuse[prevChoice].isDiagonal()) {
                        tcx = currX - reuse[prevChoice].deltaX;
                        tcy = currY - reuse[prevChoice].deltaY;
                        if (heightData[tcx][currY] <= heightData[currX][tcy])
                            tempData.insert(tcx, currY);
                        else
                            tempData.insert(currX, tcy);
                    }
                    tempData.insert(currX, currY);
                }

                while (true) {
                    best = -999999;
                    appendDirToShuffle(rng);
                    choice = -1;
                    for (int d = 0; d < 5; d++) {
                        adjX = (currX + reuse[d].deltaX);
                        if (adjX < 0 || adjX >= width)
                            continue;
                        adjY = (currY + reuse[d].deltaY);
                        if (adjY < 0 || adjY >= height)
                            continue;
                        if (heightData[adjX][adjY] > best) {
                            best = heightData[adjX][adjY];
                            choice = d;
                            sbx = adjX;
                            sby = adjY;
                        }
                    }
                    currX = sbx;
                    currY = sby;
                    if (best <= heightData[stx][sty] || heightData[currX][currY] > rng.nextDouble(160.0)
                            //|| riverData.contains(currX, currY)
                            ) {
                        riverData.or(tempData);

                        lakeData.insert(currX, currY);
                        lakeData.insert(currX + 1, currY);
                        lakeData.insert(currX - 1, currY);
                        lakeData.insert(currX, currY + 1);
                        lakeData.insert(currX, currY - 1);
                        sbx = rng.next(2);
                        lakeData.insert(currX+(~(sbx&1)|1), currY+((sbx&2)-1)); // random diagonal
                        tempData.clear();
                        continue RIVER;
                    }
                    if (choice != -1 && heightCodeData[currX][currY] >= 4) {
                        if (reuse[choice].isDiagonal()) {
                            tcx = currX - reuse[choice].deltaX;
                            tcy = currY - reuse[choice].deltaY;
                            if (heightData[tcx][currY] <= heightData[currX][tcy])
                                tempData.insert(tcx, currY);
                            else
                                tempData.insert(currX, tcy);
                        }
                        tempData.insert(currX, currY);
                    }
                }
            }

        }

        rng.setState(rebuildState);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
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
