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
import squidpony.squidmath.WhirlingNoise;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * Not currently working in full.
 */
public class DetailedWorldMapDemo extends ApplicationAdapter {
    public enum  BiomeType {
        Desert,
        Savanna,
        TropicalRainforest,
        Grassland,
        Woodland,
        SeasonalForest,
        TemperateRainforest,
        BorealForest,
        Tundra,
        Ice
    }
    private SpriteBatch batch;
    private SquidColorCenter colorFactory;
    private SquidPanel display;//, overlay;
    private final int width = 512, height = 512, cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    private static final SColor bgColor = SColor.BLACK;
    private Stage stage;
    private Viewport view;
    private Noise.Noise4D terrain, terrainRidged, disturbed, heat, moisture;
    private long seed;
    private int iseed;
    private StatefulRNG rng;
    private GridData data;
    private double[][] heightData = new double[width][height],
            heatData = new double[width][height],
            moistureData = new double[width][height];
    private int[][] heightCodeData = new int[width][height],
            heatCodeData = new int[width][height],
            moistureCodeData = new int[width][height],
            biomeCodeData = new int[width][height];

    public static final double
            deepWaterLower = -1.0, deepWaterUpper = -0.6,        // -4
            mediumWaterLower = -0.6, mediumWaterUpper = -0.4,    // -3
            shallowWaterLower = -0.4, shallowWaterUpper = -0.2,  // -2
            coastalWaterLower = -0.2, coastalWaterUpper = -0.05, // -1
            sandLower = -0.05, sandUpper = 0.05,                 // 0
            grassLower = 0.05, grassUpper = 0.35,                // 1
            forestLower = 0.35, forestUpper = 0.6,               // 2
            rockLower = 0.6, rockUpper = 0.8,                    // 3
            snowLower = 0.8, snowUpper = 1.0;                    // 4

    public static final double[] lowers = {deepWaterLower, mediumWaterLower, shallowWaterLower, coastalWaterLower,
            sandLower, grassLower, forestLower, rockLower, snowLower},
            differences = {deepWaterUpper - deepWaterLower, mediumWaterUpper - mediumWaterLower,
            shallowWaterUpper - shallowWaterLower, sandUpper - sandLower, grassUpper - grassLower,
                    forestUpper - forestLower, rockUpper - rockLower, snowUpper - snowLower};



    public static final double
            coldestValueLower = 0.0,   coldestValueUpper = 0.05, // 0
            colderValueLower = 0.05,   colderValueUpper = 0.18,  // 1
            coldValueLower = 0.18,     coldValueUpper = 0.4,     // 2
            warmValueLower = 0.4,      warmValueUpper = 0.6,     // 3
            warmerValueLower = 0.6,    warmerValueUpper = 0.8,   // 4
            warmestValueLower = 0.8,   warmestValueUpper = 1.0,  // 5

            driestValuelower = 0.0,    driestValueUpper  = 0.27, // 0
            drierValueLower = 0.27,    drierValueUpper   = 0.4,  // 1
            dryValueLower = 0.4,       dryValueUpper     = 0.6,  // 2
            wetValueLower = 0.6,       wetValueUpper     = 0.8,  // 3
            wetterValueLower = 0.8,    wetterValueUpper  = 0.9,  // 4
            wettestValueLower = 0.9,   wettestValueUpper = 1.0;  // 5

    private static SquidColorCenter squidColorCenter = new SquidColorCenter();

    private static float black = SColor.floatGetI(0, 0, 0),
            white = SColor.floatGet(0xffffffff);


    // Heightmap colors
    private static float deepColor = SColor.floatGetI(0, 27, 72);
    private static float mediumColor = SColor.floatGetI(0, 69, 129);
    private static float shallowColor = SColor.lerpFloatColors(mediumColor, white, 0.1f);
    private static float coastalColor = SColor.lerpFloatColors(mediumColor, white, 0.2f);
    private static float foamColor = SColor.floatGetI(161, 252, 255);

    private static float iceWater = SColor.floatGetI(210, 255, 252);
    private static float coldWater = mediumColor;
    private static float riverWater = shallowColor;

    private static float riverColor = SColor.floatGetI(30, 120, 200);
    private static float sandColor = SColor.floatGetI(240, 240, 64);
    private static float grassColor = SColor.floatGetI(50, 220, 20);
    private static float forestColor = SColor.floatGetI(16, 160, 0);
    private static float rockColor = SColor.floatGetI(127, 127, 127);
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

    // Biome map colors
    
    private static float ice = SColor.ALICE_BLUE.toFloatBits();
    private static float darkIce = SColor.lerpFloatColors(ice, black, 0.1f);

    private static float desert = SColor.floatGetI(238, 218, 130);
    private static float darkDesert = SColor.lerpFloatColors(desert, black, 0.1f);

    private static float savanna = SColor.floatGetI(177, 209, 110);
    private static float darkSavanna = SColor.lerpFloatColors(savanna, black, 0.1f);

    private static float tropicalRainforest = SColor.floatGetI(66, 123, 25);
    private static float darkTropicalRainforest = SColor.lerpFloatColors(tropicalRainforest, black, 0.1f);

    private static float tundra = SColor.floatGetI(96, 131, 112);
    private static float darkTundra = SColor.lerpFloatColors(tundra, black, 0.1f);

    private static float temperateRainforest = SColor.floatGetI(29, 73, 40);
    private static float darkTemperateRainforest = SColor.lerpFloatColors(temperateRainforest, black, 0.1f);

    private static float grassland = SColor.floatGetI(164, 225, 99);
    private static float darkGrassland = SColor.lerpFloatColors(grassland, black, 0.1f);

    private static float seasonalForest = SColor.floatGetI(73, 100, 35);
    private static float darkSeasonalForest = SColor.lerpFloatColors(seasonalForest, black, 0.1f);

    private static float borealForest = SColor.floatGetI(95, 115, 62);
    private static float darkBorealForest = SColor.lerpFloatColors(borealForest, black, 0.1f);

    private static float woodland = SColor.floatGetI(139, 175, 90);
    private static float darkWoodland = SColor.lerpFloatColors(woodland, black, 0.1f);



    public static int codeHeight(final double high)
    {
        if(high < deepWaterUpper)
            return -4;
        if(high < mediumWaterUpper)
            return -3;
        if(high < shallowWaterUpper)
            return -2;
        if(high < coastalWaterUpper)
            return -1;
        if(high < sandUpper)
            return 0;
        if(high < grassUpper)
            return 1;
        if(high < forestUpper)
            return 2;
        if(high < rockUpper)
            return 3;
        return 4;
    }
    public static int codeHeat(final double hot)
    {
        if(hot < coldestValueUpper)
            return 0;
        if(hot < colderValueUpper)
            return 1;
        if(hot < coldValueUpper)
            return 2;
        if(hot < warmValueUpper)
            return 3;
        if(hot < warmerValueUpper)
            return 4;
        return 5;
    }
    public static int codeMoisture(final double moist)
    {
        if(moist < driestValueUpper)
            return 0;
        if(moist < drierValueUpper)
            return 1;
        if(moist < dryValueUpper)
            return 2;
        if(moist < wetValueUpper)
            return 3;
        if(moist < wetterValueUpper)
            return 4;
        return 5;
    }

    protected final static BiomeType[] BIOME_TABLE = {
            //COLDEST        //COLDER          //COLD                  //HOT                          //HOTTER                       //HOTTEST
            BiomeType.Ice,   BiomeType.Tundra, BiomeType.Grassland,    BiomeType.Desert,              BiomeType.Desert,              BiomeType.Desert,              //DRYEST
            BiomeType.Ice,   BiomeType.Tundra, BiomeType.Grassland,    BiomeType.Grassland,           BiomeType.Desert,              BiomeType.Desert,              //DRYER
            BiomeType.Ice,   BiomeType.Tundra, BiomeType.Woodland,     BiomeType.Woodland,            BiomeType.Savanna,             BiomeType.Savanna,             //DRY
            BiomeType.Ice,   BiomeType.Tundra, BiomeType.BorealForest, BiomeType.SeasonalForest,      BiomeType.Savanna,             BiomeType.Savanna,             //WET
            BiomeType.Ice,   BiomeType.Tundra, BiomeType.BorealForest, BiomeType.SeasonalForest,      BiomeType.TropicalRainforest,  BiomeType.TropicalRainforest,  //WETTER
            BiomeType.Ice,   BiomeType.Tundra, BiomeType.BorealForest, BiomeType.TemperateRainforest, BiomeType.TropicalRainforest,  BiomeType.TropicalRainforest   //WETTEST
    };
    private int heightIndex = -1, heatIndex = -1, moistureIndex = -1;
    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new SquidPanel(width, height, cellWidth, cellHeight);
        //overlay = new SquidPanel(16, 8, DefaultResources.getStretchableFont().width(32).height(64).initBySize());
        colorFactory = new SquidColorCenter();
        view = new StretchViewport(width, height);
        stage = new Stage(view, batch);
        seed = 0xBEEFF00DCAFECABAL;
        rng = new StatefulRNG(seed);
        iseed = rng.nextInt();
        terrain = new Noise.Layered4D(new WhirlingNoise(), 6, 1.25);
        terrainRidged = new Noise.Ridged4D(new SeededNoise(iseed), 6, 1.25);
        disturbed = new Noise.Turbulent4D(terrain, terrainRidged, 1, 1.0);
        heat = new Noise.Layered4D(new Noise.Noise4D() {
            private final double inv_height = 24.0/(height);
            @Override
            public double getNoise(double x, double y, double z, double w) {
                return SeededNoise.noise(x, y, z, w, 0xF00DBEEF) * (1.0 - y * inv_height);

            }

            @Override
            public double getNoiseWithSeed(double x, double y, double z, double w, int seed) {
                return SeededNoise.noise(x, y, z, w, seed) * ((height - y) * inv_height) * (y * inv_height);
            }
        }, 4, 3.0);
        moisture = new Noise.Layered4D(SeededNoise.instance, 4, 3.0);
        data = new GridData(16);

        final double i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height;
        int seedA = rng.nextInt(), seedB = rng.nextInt(), seedC = rng.nextInt(), s, t;
        double p, q,
                ps, pc,
                qs, qc,
                h;
        for (int x = 0; x < width; x++) {
            p = x * i_w;
            ps = Math.sin(p);
            pc = Math.cos(p);
            for (int y = 0; y < height; y++) {
                q = y * i_h;
                qs = Math.sin(q);
                qc = Math.cos(q);
                heightData[x][y] = (h = disturbed.getNoiseWithSeed(pc, ps, qc, qs, seedA));
                heightCodeData[x][y] = (t = codeHeight(h));
                switch (t){
                    case 2: h = -0.1 * (h- forestLower - 0.08);
                    break;
                    case 3: h *= -0.25;
                    break;
                    case 4: h *= -0.4;
                    break;
                    default: h *= 0.01;
                }
                heatData[x][y] = (h = (heat.getNoiseWithSeed(pc, ps, qc, qs, seedB) * 0.25) + 0.65 + h);
                heatCodeData[x][y] = (s = codeHeat(h));
                moistureData[x][y] = (q = moisture.getNoiseWithSeed(pc, ps, qc, qs, seedC) * 0.5 + 0.5);
                moistureCodeData[x][y] = (t = codeMoisture(q));
                biomeCodeData[x][y] = BIOME_TABLE[s + t * 6].ordinal();
            }
        }
        heightIndex = data.putDoubles("height", heightData);
        heatIndex = data.putDoubles("heat", heatData);
        moistureIndex = data.putDoubles("moisture", moistureData);

        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        putMap();
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        stage.addActor(display);
        putMap();
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }

    public void putMap() {
        display.erase();
        int hc, bc;
        double h;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                h = heightData[x][y];
                hc = heightCodeData[x][y] + 4;
                bc = biomeCodeData[x][y];
                switch (bc)
                {
                    case 0: //Desert
                        display.put(x, y, SColor.lerpFloatColors(desert, darkDesert,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                    break;
                    case 1: //Savanna
                        display.put(x, y, SColor.lerpFloatColors(savanna, darkSavanna,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 2: //TropicalRainforest
                        display.put(x, y, SColor.lerpFloatColors(tropicalRainforest, darkTropicalRainforest,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 3: //Grassland
                        display.put(x, y, SColor.lerpFloatColors(grassland, darkGrassland,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 4: //Woodland
                        display.put(x, y, SColor.lerpFloatColors(woodland, darkWoodland,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 5: //SeasonalForest
                        display.put(x, y, SColor.lerpFloatColors(seasonalForest, darkSeasonalForest,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 6: //TemperateRainforest
                        display.put(x, y, SColor.lerpFloatColors(temperateRainforest, darkTemperateRainforest,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 7: //BorealForest
                        display.put(x, y, SColor.lerpFloatColors(borealForest, darkBorealForest,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 8: //Tundra
                        display.put(x, y, SColor.lerpFloatColors(tundra, darkTundra,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
                    case 9: //Ice
                        display.put(x, y, SColor.lerpFloatColors(ice, darkIce,
                                (float) ((h - lowers[hc])/(differences[hc]))));
                        break;
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
        config.title = "SquidLib Test: Detailed World Map";
        config.width = 512;
        config.height = 512;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DetailedWorldMapDemo(), config);
    }
}
