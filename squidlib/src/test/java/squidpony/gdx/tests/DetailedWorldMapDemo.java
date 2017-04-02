package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidColorCenter;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidPanel;
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
    private final double terrainFreq = 1.9, terrainRidgedFreq = 2.5, heatFreq = 5.5, moistureFreq = 5.0, otherFreq = 4.1;

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
    private int iseed;
    private StatefulRNG rng;
    //private GridData data;
    private double[][] heightData = new double[width][height],
            heatData = new double[width][height],
            moistureData = new double[width][height],
            biomeDifferenceData = new double[width][height];
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
            minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
            minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
            minHeat = Double.POSITIVE_INFINITY, maxHeat = Double.NEGATIVE_INFINITY,
            minWet = Double.POSITIVE_INFINITY, maxWet = Double.NEGATIVE_INFINITY;
    private double i_hot = 1.0;
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

    private static float rocky = SColor.floatGetI(177, 167, 157);
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
        Ice+0.1f,   River+0.6f,   River+0.5f,          River+0.4f,               River+0.3f,              River+0.2f,              //LAKES
    }, BIOME_COLOR_TABLE = new float[54], BIOME_DARK_COLOR_TABLE = new float[54];

    static {
        float b, diff;
        for (int i = 0; i < 54; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.45f) * 0.3f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lerpFloatColors(biomeColors[(int)b], white, diff)
                    : SColor.lerpFloatColors(biomeColors[(int)b], black, -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.lerpFloatColors(b, black, 0.1f);
        }
    }
    private void codeBiome(int x, int y, double hot, double moist, int heightCode) {
        int hc, mc;
        double upperProximityH, upperProximityM, lowerProximityH, lowerProximityM, bound, prevBound;
        boolean isLake = partialLakeData.contains(x, y), isRiver = partialRiverData.contains(x, y);
        if(moist >= (bound = (wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2)))
        {
            mc = 5;
            upperProximityM = (moist - bound) / (maxWet - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2)))
        {
            mc = 4;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (wetValueUpper - (dryValueUpper - dryValueLower) * 0.2)))
        {
            mc = 3;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (dryValueUpper - (drierValueUpper - drierValueLower) * 0.2)))
        {
            mc = 2;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (drierValueUpper - (driestValueUpper) * 0.2)))
        {
            mc = 1;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else
        {
            mc = 0;
            upperProximityM = (moist) / (bound);
        }

        if(hot >= (bound = (warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2) * i_hot))
        {
            hc = 5;
            upperProximityH = (hot - bound) / (maxHeat - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2) * i_hot))
        {
            hc = 4;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (warmValueUpper - (coldValueUpper - coldValueLower) * 0.2) * i_hot))
        {
            hc = 3;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (coldValueUpper - (colderValueUpper - colderValueLower) * 0.2) * i_hot))
        {
            hc = 2;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (colderValueUpper - (coldestValueUpper) * 0.2) * i_hot))
        {
            hc = 1;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else
        {
            hc = 0;
            upperProximityH = (hot) / (bound);
        }

        heatCodeData[x][y] = hc;
        moistureCodeData[x][y] = mc;
        biomeUpperCodeData[x][y] = isLake? hc + 48 : (isRiver ? hc + 42 : ((heightCode == 4) ? hc + 36 : hc + mc * 6));

        if(moist >= (bound = (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2)))
        {
            mc = 5;
            lowerProximityM = (moist - bound) / (maxWet - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.55)))
        {
            mc = 4;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (dryValueUpper + (wetValueUpper - wetValueLower) * 0.2)))
        {
            mc = 3;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (drierValueUpper + (dryValueUpper - dryValueLower) * 0.2)))
        {
            mc = 2;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = (driestValueUpper + (drierValueUpper - drierValueLower) * 0.2)))
        {
            mc = 1;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else
        {
            mc = 0;
            lowerProximityM = (moist) / (bound);
        }

        if(hot >= (bound = (warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2) * i_hot))
        {
            hc = 5;
            lowerProximityH = (hot - bound) / (maxHeat - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2) * i_hot))
        {
            hc = 4;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (coldValueUpper + (warmValueUpper - warmValueLower) * 0.2) * i_hot))
        {
            hc = 3;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (colderValueUpper + (coldValueUpper - coldValueLower) * 0.2) * i_hot))
        {
            hc = 2;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = (coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2) * i_hot))
        {
            hc = 1;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else
        {
            hc = 0;
            lowerProximityH = (hot) / (bound);
        }

        biomeLowerCodeData[x][y] = hc + mc * 6;
        //biomeDifferenceData[x][y] = (Math.max(upperProximityH, upperProximityM) + Math.max(lowerProximityH, lowerProximityM)) * 0.5;
        if(isRiver || isLake)
            biomeDifferenceData[x][y] = (upperProximityH + upperProximityM) * 0.5;
        else
            biomeDifferenceData[x][y] = (upperProximityH + upperProximityM + lowerProximityH + lowerProximityM) * 0.25;
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
        // 1.9, 2.3, 5.5, 5.0, 4.1
        terrain = new Noise.Layered4D(SeededNoise.instance, 7, terrainFreq);
        terrainRidged = new Noise.Ridged4D(SeededNoise.instance, 8, terrainRidgedFreq);
        heat = new Noise.Layered4D(SeededNoise.instance, 4, heatFreq);
        moisture = new Noise.Layered4D(SeededNoise.instance, 5, moistureFreq);
        otherRidged = new Noise.Ridged4D(SeededNoise.instance, 6, otherFreq);
        //data = new GridData(16);
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        seed = rng.nextLong();
                        regenerate(seed);
                        rng.setState(seed);
                        break;
                    case '=':
                    case '+':
                        if(zoom < 7)
                        {
                            zoom++;
                            cachedState = rng.getState();
                            regenerate(cachedState);
                            rng.setState(cachedState);
                        }
                        break;
                    case '-':
                    case '_':
                        if(zoom > 0)
                            zoom--;
                        cachedState = rng.getState();
                        regenerate(cachedState);
                        rng.setState(cachedState);
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        });
        cachedState = ~seed;
        regenerate(seed);
        rng.setState(seed);
        Gdx.input.setInputProcessor(input);
        display.setPosition(0, 0);
        stage.addActor(display);
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }
    public void regenerate(final long state) {
        if(zoom != 0 && cachedState != state)
            zoom = 0;
        regenerate((width >> 1) - (width >> zoom+1), (height >> 1) - (height >> zoom+1),
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
            minWet = Double.POSITIVE_INFINITY;
            maxWet = Double.NEGATIVE_INFINITY;
            cachedState = state;
            fresh = true;
        }
        rng.setState(state);
        int seedA = rng.nextInt(), seedB = rng.nextInt(), seedC = rng.nextInt(), t;
        waterModifier = rng.nextDouble(0.15)-0.06;
        coolingModifier = NumberTools.randomDoubleCurved(rng.nextInt()) * 0.5 + 1.0;

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
                if(fresh) {
                    minHeight = Math.min(minHeight, h);
                    maxHeight = Math.max(maxHeight, h);

                    minHeat0 = Math.min(minHeat0, p);
                    maxHeat0 = Math.max(maxHeat0, p);

                    minWet = Math.min(minWet, temp);
                    maxWet = Math.max(maxWet, temp);

                }
            }
        }
        double heightDiff = 2.0 / (maxHeight - minHeight),
                heatDiff = 0.8 / (maxHeat0 - minHeat0),
                wetDiff = 1.0 / (maxWet - minWet),
                hMod,
                halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
        yPos = startY;
        ps = Double.POSITIVE_INFINITY;
        pc = Double.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = Math.abs(yPos - halfHeight) * i_half;
            temp *= (2.4 - temp);
            temp = 2.2 - temp;
            for (int x = 0; x < width; x++) {
                heightData[x][y] = (h = (heightData[x][y] - minHeight) * heightDiff - 1.0);
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
        //ps = Double.POSITIVE_INFINITY;
        //pc = Double.NEGATIVE_INFINITY;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                //(h = Math.pow(h, 2.0 - h * 2.0));
                moistureData[x][y] = (moistureData[x][y] - minWet) * wetDiff; // may assign to temp?
                if (fresh) {
                    qs = Math.min(qs, h);
                    qc = Math.max(qc, h);
                    //ps = Math.min(ps, temp);
                    //pc = Math.max(pc, temp);
                }
            }
        }
        if(fresh)
        {
            minHeat = qs;
            maxHeat = qc;
            i_hot =  1.0 / qc;
        }
        if(fresh)
        {
            addRivers();
            partialRiverData.remake(riverData);
            partialLakeData.remake(lakeData);
        }
        else {
            partialRiverData.remake(riverData);
            partialLakeData.remake(lakeData);
            for (int i = 1; i <= zoom; i++) {
                if ((i & 1) == 0) {

                    partialRiverData.zoom((width >> 2), (height >> 2)).connect8way();
                    partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.4)).connect8way();
                    partialLakeData.zoom((width >> 2), (height >> 2)).connect8way();
                    partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.45)).connect8way();

                }
                else
                {
                    partialRiverData.zoom((width >> 2), (height >> 2)).connect8way();
                    partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.17)).connect8way();
                    partialLakeData.zoom((width >> 2), (height >> 2)).connect8way();
                    partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.2)).connect8way();
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
                        display.put(x, y, SColor.lerpFloatColors(BIOME_COLOR_TABLE[biomeUpperCodeData[x][y]],
                                BIOME_DARK_COLOR_TABLE[biomeLowerCodeData[x][y]],
                                (float) (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 19
                                        + biomeDifferenceData[x][y] * 13) * 0.03125f
                                ));
                }
            }
        }
    }
    private static int encode(final int x, final int y)
    {
        return width * y + x;
    }
    private static int decodeX(final int coded)
    {
        return coded % width;
    }
    private static int decodeY(final int coded)
    {
        return coded / width;
    }
    private static final Direction[] reuse = new Direction[9];
    private void appendDirToShuffle(RNG rng) {
        rng.randomPortion(Direction.CARDINALS, reuse);
        int diags = rng.next(2);
        reuse[rng.next(2)] = Direction.DIAGONALS[diags];
        /*
        int diags = rng.nextIntHasty(12);
        reuse[3] = Direction.DIAGONALS[diags & 3];
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
        riverData.quasiRandomRegion(0.002).and(workingData);
        //System.out.println(riverData.show('.', '#'));
        //System.out.println('\n');
        int[] starts = riverData.asTightEncoded();
        int len = starts.length, currentPos, adj, adjX, adjY, currX, currY, tcx, tcy, stx, sty, sbx, sby;
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
                appendDirToShuffle(rng);

                for (int d = 0; d < 4; d++) {
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
        int riverCount = riverData.size() >> 4, currentMax = riverCount >> 2, idx = 0;
        for (int h = 5; h < 9; h++) { //, currentMax += riverCount / 18
            workingData.empty().refill(heightCodeData, h).and(riverData);
            //System.out.println(workingData.show('.', '#'));
            RIVER:
            for (int j = 0; j < currentMax && idx < riverCount; j++) {
                double vdc = VanDerCorputQRNG.weakDetermine(idx++);
                currentPos = workingData.atFractionTight(vdc);
                if(currentPos < 0) break;
                stx = sbx = tcx = currX = decodeX(currentPos);
                sty = sby = tcy = currY = decodeY(currentPos);
                while (true) {
                    double best = -999999;
                    appendDirToShuffle(rng);

                    for (int d = 0; d < 4; d++) {
                        adjX = (currX + reuse[d].deltaX);
                        if (adjX < 0 || adjX >= width)
                            continue;
                        adjY = (currY + reuse[d].deltaY);
                        if (adjY < 0 || adjY >= height)
                            continue;
                        if (heightData[adjX][adjY] > best && !riverData.contains(adjX, adjY)) {
                            best = heightData[adjX][adjY];
                            sbx = tcx;
                            sby = tcy;
                            tcx = adjX;
                            tcy = adjY;
                        }
                    }
                    currX = sbx;
                    currY = sby;
                    if (best <= heightData[stx][sty] || heightData[currX][currY] > rng.nextDouble(160.0)) {
                        riverData.or(workingData);
                        continue RIVER;
                    }if (heightCodeData[currX][currY] >= 4)
                        workingData.insert(currX, currY);
                }
            }

        }

        /*
        riverData.refill(heightData, 0.05, 0.1);
        riverData.deteriorate(rng, 0.015625).deteriorate(rng, 0.03125);
        IntVLA starts = new IntVLA(riverData.asTightEncoded());
        int[] xOffsets = {0, 1, 0, -1}, yOffsets = {1, 0, -1, 0};
        int len = starts.size * 20, currentPos, adjX, adjY, currX, currY, tcx, tcy, stx, sty;
        riverData.clear();
        lakeData.clear();
        PER_RIVER:
        for (int i = 0; i < starts.size && i < len; i++) {
            currentPos = starts.get(i);
            stx = tcx = currX = decodeX(currentPos);
            sty = tcy = currY = decodeY(currentPos);
            while(true) {
                double best = -999999;
                appendDirToShuffle(rng);

                for (int d = 0; d < 4; d++) {
                    adjX = (currX + reuse[d].deltaX);
                    if(adjX < 0 || adjX >= width)
                        continue;
                    adjY = (currY + reuse[d].deltaY);
                    if(adjY < 0 || adjY >= height)
                        continue;
                    if (heightData[adjX][adjY] > best && !riverData.contains(adjX, adjY)) {
                        best = heightData[adjX][adjY];
                        tcx = adjX;
                        tcy = adjY;
                    }
                }
                currX = tcx;
                currY = tcy;
                if (best <= heightData[stx][sty]
                        || rng.next(8) == 0) // 1 in 256 chance
                {
                    if(heightCodeData[currX][currY] < 4)
                        continue PER_RIVER;
                    tcx = rng.nextIntHasty(12);
                    tcy = tcx & 3;
                    tcx = ((tcx >>> 2) + 1 + tcy) & 3;
                    adjX = currX+xOffsets[tcy];
                    adjY = currY+yOffsets[tcy];
                    if(adjY >= 0 && adjY < height && adjX >= 0 && adjX < width && heightCodeData[adjX][adjY] >= 4)
                        riverData.insert(adjX, adjY);
                    adjX += xOffsets[tcy];
                    adjY += yOffsets[tcy];
                    if(adjY >= 0 && adjY < height && adjX >= 0 && adjX < width && heightCodeData[adjX][adjY] >= 4)
                        riverData.insert(adjX, adjY);
                    tcy = tcy+(rng.next(2)|1) & 3; // add either 1 or 3; ensures a 90 degree turn
                    adjX += xOffsets[tcy];
                    adjY += yOffsets[tcy];
                    if(adjY >= 0 && adjY < height && adjX >= 0 && adjX < width)
                        starts.add(encode(adjX, adjY));
                    adjX = currX+xOffsets[tcx];
                    adjY = currY+yOffsets[tcx];
                    if(adjY >= 0 && adjY < height && adjX >= 0 && adjX < width && heightCodeData[adjX][adjY] >= 4)
                        riverData.insert(adjX, adjY);
                    adjX += xOffsets[tcx];
                    adjY += yOffsets[tcx];
                    if(adjY >= 0 && adjY < height && adjX >= 0 && adjX < width && heightCodeData[adjX][adjY] >= 4)
                        riverData.insert(adjX, adjY);
                    tcx = tcx+(rng.next(2)|1) & 3; // add either 1 or 3; ensures a 90 degree turn
                    adjX += xOffsets[tcx];
                    adjY += yOffsets[tcx];
                    if(adjY >= 0 && adjY < height && adjX >= 0 && adjX < width)
                        starts.add(encode(adjX, adjY));

                    riverData.insert(currX, currY);

                    //if((heightData[adjX][adjY] += 0.001) > uppers[heightCodeData[adjX][adjY]])
                    //    continue PER_RIVER;
                    continue PER_RIVER;
                }
                if(heightCodeData[currX][currY] >= 4)
                    riverData.insert(currX, currY);
                if(heightData[currX][currY] > rng.nextDouble(150.0))
                    continue PER_RIVER;
            }
        }
        */
                /*
                double best = workingData[encode(currX, currY)];
                final Direction[] dirs = appendDirToShuffle(rng);
                for (int d = 0; d < 4; d++) {
                    adjX = (currX + dirs[d].deltaX);
                    if(adjX < 0 || adjX >= width)
                        continue;
                    adjY = (currY + dirs[d].deltaY);
                    if(adjY < 0 || adjY >= height)
                        continue;
                    adj = encode(adjX, adjY);
                    if (workingData[adj] < best) {
                        best = workingData[adj];
                        tcx = adjX;
                        tcy = adjY;
                    }
                }
                currX = tcx;
                currY = tcy;
                if (best >= workingData[starts[i]] || riverData.contains(currX, currY)) {
                    continue PER_RIVER;
                }
                */
        /*
        GreasedRegion heightZone = new GreasedRegion(width, height);
        for (int i = 4; i < 9; i++) {
            heightZone.empty().refill(heightCodeData, i);
            riverData.or(heightZone.and(riverData).fringe8way().deteriorate(rng, 1.35 - 0.1 * i));
        }*/
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
