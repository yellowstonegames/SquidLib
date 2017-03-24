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
import squidpony.squidgrid.GridData;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidColorCenter;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidPanel;
import squidpony.squidmath.Noise;
import squidpony.squidmath.SeededNoise;
import squidpony.squidmath.StatefulRNG;

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
        Rocky                  = 11;

    private static final int width = 800, height = 800;

    private SpriteBatch batch;
    private SquidColorCenter colorFactory;
    private SquidPanel display;//, overlay;
    private int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    private static final SColor bgColor = SColor.BLACK;
    private Stage stage;
    private Viewport view;
    private Noise.Noise4D terrain, terrainRidged, heat, moisture, otherRidged;
    private long seed;
    private int iseed;
    private StatefulRNG rng;
    private GridData data;
    private double[][] heightData = new double[width][height],
            heatData = new double[width][height],
            moistureData = new double[width][height],
            biomeDifferenceData = new double[width][height];
    private int[][] heightCodeData = new int[width][height],
            heatCodeData = new int[width][height],
            moistureCodeData = new int[width][height],
            biomeUpperCodeData = new int[width][height],
            biomeLowerCodeData = new int[width][height];
    public double waterModifier = 0.0, coolingModifier = 1.0,
            minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY,
            minHeat = Double.POSITIVE_INFINITY, maxHeat = Double.NEGATIVE_INFINITY,
            minHeat2 = Double.POSITIVE_INFINITY, maxHeat2 = Double.NEGATIVE_INFINITY,
            minWet = Double.POSITIVE_INFINITY, maxWet = Double.NEGATIVE_INFINITY;
    ;

    public static final double
            deepWaterLower = -1.0, deepWaterUpper = -0.7,        // -4
            mediumWaterLower = -0.7, mediumWaterUpper = -0.3,    // -3
            shallowWaterLower = -0.3, shallowWaterUpper = -0.1,  // -2
            coastalWaterLower = -0.1, coastalWaterUpper = 0.1,   // -1
            sandLower = 0.1, sandUpper = 0.18,                   // 0
            grassLower = 0.18, grassUpper = 0.35,                // 1
            forestLower = 0.35, forestUpper = 0.6,               // 2
            rockLower = 0.6, rockUpper = 0.8,                    // 3
            snowLower = 0.8, snowUpper = 1.0;                    // 4

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
    private static float shallowColor = SColor.CERULEAN.toFloatBits();
    private static float darkShallowColor = SColor.lerpFloatColors(shallowColor, black, 0.15f);
    private static float coastalColor = SColor.lerpFloatColors(shallowColor, white, 0.3f);
    private static float darkCoastalColor = SColor.lerpFloatColors(coastalColor, black, 0.15f);
    private static float foamColor = SColor.floatGetI(161, 252, 255);
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
            rocky
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
            darkRocky
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
        Ice+0.7f,   Ice+0.65f,    Grassland+0.8f,      Desert+0.65f,             Desert+0.75f,            Desert+0.8f,        //DRYEST
        Ice+0.6f,   Tundra+0.9f,  Grassland+0.6f,      Grassland+0.4f,           Desert+0.6f,             Desert+0.65f,       //DRYER
        Ice+0.5f,   Tundra+0.7f,  Woodland+0.5f,       Woodland+0.6f,            Savanna+0.6f,            Desert+0.5f,        //DRY
        Ice+0.4f,   Tundra+0.5f,  SeasonalForest+0.3f, SeasonalForest+0.5f,      Savanna+0.4f,            Savanna+0.3f,       //WET
        Ice+0.2f,   Tundra+0.3f,  BorealForest+0.2f,   TemperateRainforest+0.4f, TropicalRainforest+0.3f, Savanna+0.1f,       //WETTER
        Ice+0.0f,   BorealForest, BorealForest+0.0f,   TemperateRainforest+0.2f, TropicalRainforest+0.1f, TropicalRainforest, //WETTEST
        Rocky+0.9f, Rocky+0.6f,   Beach+0.4f,          Beach+0.55f,              Beach+0.75f,             Beach+0.9f          //COASTS
    }, BIOME_COLOR_TABLE = new float[42], BIOME_DARK_COLOR_TABLE = new float[42];

    static {
        float b, diff;
        for (int i = 0; i < 42; i++) {
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
        if(moist >= (bound = wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2))
        {
            mc = 5;
            upperProximityM = (moist - bound) / (1.0 - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2))
        {
            mc = 4;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = wetValueUpper - (dryValueUpper - dryValueLower) * 0.2))
        {
            mc = 3;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = dryValueUpper - (drierValueUpper - drierValueLower) * 0.2))
        {
            mc = 2;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = drierValueUpper - (driestValueUpper) * 0.2))
        {
            mc = 1;
            upperProximityM = (moist - bound) / (prevBound - bound);
        }
        else
        {
            mc = 0;
            upperProximityM = (moist) / (bound);
        }

        if(hot >= (bound = warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2))
        {
            hc = 5;
            upperProximityH = (hot - bound) / (1.0 - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2))
        {
            hc = 4;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = warmValueUpper - (coldValueUpper - coldValueLower) * 0.2))
        {
            hc = 3;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = coldValueUpper - (colderValueUpper - colderValueLower) * 0.2))
        {
            hc = 2;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = colderValueUpper - (coldestValueUpper) * 0.2))
        {
            hc = 1;
            upperProximityH = (hot - bound) / (prevBound - bound);
        }
        else
        {
            hc = 0;
            upperProximityH = (hot) / (bound);
        }

        /*
        if(hot < coldestValueUpper)
        {
            hc = 0;
            upperProximityH = (hot - coldestValueLower) / (coldestValueUpper - coldestValueLower);
        }
        else if(hot < colderValueUpper)
        {
            hc = 1;
            upperProximityH = (hot - colderValueLower) / (colderValueUpper - colderValueLower);
        }
        else if(hot < coldValueUpper)
        {
            hc = 2;
            upperProximityH = (hot - coldValueLower) / (coldValueUpper - coldValueLower);
        }
        else if(hot < warmValueUpper)
        {
            hc = 3;
            upperProximityH = (hot - warmValueLower) / (warmValueUpper - warmValueLower);
        }
        else if(hot < warmerValueUpper)
        {
            hc = 4;
            upperProximityH = (hot - warmerValueLower) / (warmerValueUpper - warmerValueLower);
        }
        else
        {
            hc = 5;
            upperProximityH = (hot - warmestValueLower) / (warmestValueUpper - warmestValueLower);
        }

        if(moist < driestValueUpper)
        {
            mc = 0;
            upperProximityM = (moist - driestValueLower) / (driestValueUpper - driestValueLower);
        }
        else if(moist < drierValueUpper)
        {
            mc = 1;
            upperProximityM = (moist - drierValueLower) / (drierValueUpper - drierValueLower);
        }
        else if(moist < dryValueUpper)
        {
            mc = 2;
            upperProximityM = (moist - dryValueLower) / (dryValueUpper - dryValueLower);
        }
        else if(moist < wetValueUpper)
        {
            mc = 3;
            upperProximityM = (moist - wetValueLower) / (wetValueUpper - wetValueLower);
        }
        else if(moist < wetterValueUpper)
        {
            mc = 4;
            upperProximityM = (moist - wetterValueLower) / (wetterValueUpper - wetterValueLower);
        }
        else
        {
            mc = 5;
            upperProximityM = (moist - wettestValueLower) / (wettestValueUpper - wettestValueLower);
        }
        */

        heatCodeData[x][y] = hc;
        moistureCodeData[x][y] = mc;
        biomeUpperCodeData[x][y] = (heightCode == 4) ? hc + 36 : hc + mc * 6;

        if(moist >= (bound = wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2))
        {
            mc = 5;
            lowerProximityM = (moist - bound) / (1.0 - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.55))
        {
            mc = 4;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = dryValueUpper + (wetValueUpper - wetValueLower) * 0.2))
        {
            mc = 3;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = drierValueUpper + (dryValueUpper - dryValueLower) * 0.2))
        {
            mc = 2;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || moist >= (bound = driestValueUpper + (drierValueUpper - drierValueLower) * 0.2))
        {
            mc = 1;
            lowerProximityM = (moist - bound) / (prevBound - bound);
        }
        else
        {
            mc = 0;
            lowerProximityM = (moist) / (bound);
        }

        if(hot >= (bound = warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2))
        {
            hc = 5;
            lowerProximityH = (hot - bound) / (1.0 - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2))
        {
            hc = 4;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = coldValueUpper + (warmValueUpper - warmValueLower) * 0.2))
        {
            hc = 3;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = colderValueUpper + (coldValueUpper - coldValueLower) * 0.2))
        {
            hc = 2;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else if((prevBound = bound) == -1 || hot >= (bound = coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2))
        {
            hc = 1;
            lowerProximityH = (hot - bound) / (prevBound - bound);
        }
        else
        {
            hc = 0;
            lowerProximityH = (hot) / (bound);
        }

        biomeLowerCodeData[x][y] = (hc + mc * 6);
        biomeDifferenceData[x][y] = (Math.max(upperProximityH, upperProximityM) + Math.max(lowerProximityH, lowerProximityM)) * 0.5;
        //biomeDifferenceData[x][y] = (upperProximityH + upperProximityM + lowerProximityH + lowerProximityM) * 0.25;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new SquidPanel(width, height, cellWidth, cellHeight);
        //overlay = new SquidPanel(16, 8, DefaultResources.getStretchableFont().width(32).height(64).initBySize());
        colorFactory = new SquidColorCenter();
        view = new StretchViewport(width, height);
        stage = new Stage(view, batch);
        seed = 0xBEEFF00DCAFECABAL;
        rng = new StatefulRNG(seed); //seed
        terrain = new Noise.Layered4D(SeededNoise.instance, 7, 2.0);
        terrainRidged = new Noise.Ridged4D(SeededNoise.instance, 8, 2.3);
        heat = new Noise.Layered4D(SeededNoise.instance, 4, 5.5);
        moisture = new Noise.Layered4D(SeededNoise.instance, 4, 4.5);
        otherRidged = new Noise.Ridged4D(SeededNoise.instance, 5, 3.7);
        data = new GridData(16);
        input = new SquidInput(new SquidInput.KeyHandler() {
            int zoom = 0;
            int half_w = width >> 1, half_h = height >> 1;
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        rng.nextLong();
                        regenerate(half_w - (half_w >> zoom), half_h - (half_h >> zoom), (width >> zoom), (height >> zoom), seed = rng.getState());
                        rng.setState(seed);
                        //putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case '=':
                    case '+':
                        if(zoom < 7)
                        {
                            zoom++;
                            regenerate(half_w - (half_w >> zoom), half_h - (half_h >> zoom), (width >> zoom), (height >> zoom), seed = rng.getState());
                            rng.setState(seed);
                        }
                        break;
                    case '-':
                    case '_':
                        if(zoom > 0)
                        {
                            zoom--;
                            regenerate(half_w - (half_w >> zoom), half_h - (half_h >> zoom), (width >> zoom), (height >> zoom), seed = rng.getState());
                            rng.setState(seed);
                        }
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        });
        regenerate(0, 0, width, height, seed);
        rng.setState(seed);
        Gdx.input.setInputProcessor(input);
        display.setPosition(0, 0);
        stage.addActor(display);
        //putMap();
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }
    public void regenerate() {
        regenerate(0, 0, width, height, rng.getState());
    }
    public void regenerate(int startX, int startY, int usedWidth, int usedHeight, long state)
    {
        rng.setState(state);
        int seedA = rng.nextInt(), seedB = rng.nextInt(), seedC = rng.nextInt(), seedD = rng.nextInt(), t;
        waterModifier = rng.nextDouble(0.15)-0.06;
        coolingModifier = (rng.nextDouble(0.75) - rng.nextDouble(0.75) + 1.0);

        double p, q,
                ps, pc,
                qs, qc,
                h, temp,
                i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height,
                xPos, yPos = startY, i_uw = usedWidth / (double)width, i_uh = usedHeight / (double)height;
        for (int y = 0; y < height; y++, yPos += i_uh) {
            q = yPos * i_h;
            qs = Math.sin(q);
            qc = Math.cos(q);
            xPos = startX;
            for (int x = 0; x < width; x++, xPos += i_uw) {
                p = xPos * i_w;
                ps = Math.sin(p);
                pc = Math.cos(p);
                h = terrain.getNoiseWithSeed(pc +
                                terrainRidged.getNoiseWithSeed(pc, ps, qc, qs, seedC),
                        ps, qc, qs, seedA);
                p = Math.signum(h) + waterModifier;
                h *= p * p;
                heightData[x][y] = h;
                minHeight = Math.min(minHeight, h);
                maxHeight = Math.max(maxHeight, h);
                heatData[x][y] = (h = heat.getNoiseWithSeed(pc, ps, qc
                        + otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedD + seedC), qs, seedB));
                minHeat = Math.min(minHeat, h);
                maxHeat = Math.max(maxHeat, h);
                moistureData[x][y] = (h = moisture.getNoiseWithSeed(pc, ps, qc, qs
                        + otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedD + seedB), seedC));
                minWet = Math.min(minWet, h);
                maxWet = Math.max(maxWet, h);
            }
        }
        double heightDiff = 2.0 / (maxHeight - minHeight),
                heatDiff = 0.8 / (maxHeat - minHeat),
                wetDiff = 1.0 / (maxWet - minWet),
                hMod,
                halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
        yPos = startY;
        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = Math.abs(yPos - halfHeight) * i_half;
            temp *= (2.4 - temp);
            for (int x = 0; x < width; x++) {
                heightData[x][y] = (h = (heightData[x][y] - minHeight) * heightDiff - 1.0);
                heightCodeData[x][y] = (t = codeHeight(h));
                hMod = 1.0;
                switch (t){
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        h = 0.4;
                        hMod = 0.2;
                        break;
                    case 6: h = -0.1 * (h - forestLower - 0.08);
                        break;
                    case 7: h *= -0.25;
                        break;
                    case 8: h *= -0.4;
                        break;
                    default: h *= 0.05;
                }
                heatData[x][y] = (h = (((heatData[x][y] - minHeat) * heatDiff * hMod) + h + 0.6) * (2.2 - temp));
                minHeat2 = Math.min(minHeat2, h);
                maxHeat2 = Math.max(maxHeat2, h);
            }
        }
        heatDiff = coolingModifier / (maxHeat2 - minHeat2);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                t = heightCodeData[x][y];
                h = ((heatData[x][y] - minHeat2) * heatDiff);
                heatData[x][y] = h;//(h = Math.pow(h, 2.0 - h * 2.0));
                moistureData[x][y] = (q = (moistureData[x][y] - minWet) * wetDiff);
                codeBiome(x, y, h, q, t);
            }
           
        }
        /*
        data.putDoubles("height", heightData);
        data.putDoubles("heat", heatData);
        data.putDoubles("moisture", moistureData);
        */
    }

    public void putMap() {
        //regenerate();
        display.erase();
        int hc, tc;
        double tmp;
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
                                (float) (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 21 + biomeDifferenceData[x][y] * 11) * 0.03125f));
                        /*
                        switch (bc) {
                            case 0: //Desert
                                display.put(x, y, SColor.lerpFloatColors(darkDesert, desert,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 1: //Savanna
                                display.put(x, y, SColor.lerpFloatColors(darkSavanna, savanna,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 2: //TropicalRainforest
                                display.put(x, y, SColor.lerpFloatColors(darkTropicalRainforest, tropicalRainforest,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 3: //Grassland
                                display.put(x, y, SColor.lerpFloatColors(darkGrassland, grassland,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 4: //Woodland
                                display.put(x, y, SColor.lerpFloatColors(darkWoodland, woodland,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 5: //SeasonalForest
                                display.put(x, y, SColor.lerpFloatColors(darkSeasonalForest, seasonalForest,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 6: //TemperateRainforest
                                display.put(x, y, SColor.lerpFloatColors(darkTemperateRainforest, temperateRainforest,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 7: //BorealForest
                                display.put(x, y, SColor.lerpFloatColors(darkBorealForest, borealForest,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 8: //Tundra
                                display.put(x, y, SColor.lerpFloatColors(darkTundra, tundra,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                            case 9: //Ice
                                display.put(x, y, SColor.lerpFloatColors(darkIce, ice,
                                        (float) ((h - lowers[hc]) / (differences[hc]))));
                                break;
                        }
                        */
                }
            }
        }
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle("SquidLib Demo: Detailed World Map, at " + Gdx.graphics.getFramesPerSecond() + " FPS");
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.

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
        config.width = width;
        config.height = height;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DetailedWorldMapDemo(), config);
    }
}
