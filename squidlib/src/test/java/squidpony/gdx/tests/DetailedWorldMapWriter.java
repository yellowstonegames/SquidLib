package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.*;

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
        Ocean                  = 13;

    private static final int width = 1920, height = 1080;
    //private static final int width = 314 * 4, height = 400;
    //private static final int width = 512, height = 512;

    private SpriteBatch batch;
    //private SquidPanel display;//, overlay;
//    private FakeLanguageGen lang = FakeLanguageGen.randomLanguage(-1234567890L).removeAccents()
//            .mix(FakeLanguageGen.SIMPLISH, 0.6);
    private FakeLanguageGen lang = FakeLanguageGen.SIMPLISH
            .mix(FakeLanguageGen.FANTASY_NAME, 0.7);
    private Pixmap pm;
    private Texture pt;
    private int counter = 0;
    private Color tempColor = Color.WHITE.cpy();
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    //private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private WorldMapGenerator world;
    private WorldMapGenerator.DetailedBiomeMapper dbm;
/*    private final double[][] shadingData = new double[width][height];
    private final int[][]
            heatCodeData = new int[width][height],
            moistureCodeData = new int[width][height],
            biomeUpperCodeData = new int[width][height],
            biomeLowerCodeData = new int[width][height];
            */
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
    private static float lightIce = white;

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

    // water colors
    private static float deepColor = SColor.floatGetI(0, 68, 128);
    private static float mediumColor = SColor.floatGetI(0, 89, 159);
    private static float shallowColor = SColor.floatGetI(0, 123, 167);
    private static float coastalColor = SColor.lerpFloatColors(shallowColor, white, 0.3f);
    private static float foamColor = SColor.floatGetI(61,  162, 215);

    // Heat map colors
//    private static float coldest = SColor.floatGetI(0, 255, 255);
//    private static float colder = SColor.floatGetI(170, 255, 255);
//    private static float cold = SColor.floatGetI(0, 229, 133);
//    private static float warm = SColor.floatGetI(255, 255, 100);
//    private static float warmer = SColor.floatGetI(255, 100, 0);
//    private static float warmest = SColor.floatGetI(241, 12, 0);
//
//    // Moisture map colors
//    private static float dryest = SColor.floatGetI(255, 139, 17);
//    private static float dryer = SColor.floatGetI(245, 245, 23);
//    private static float dry = SColor.floatGetI(80, 255, 0);
//    private static float wet = SColor.floatGetI(85, 255, 255);
//    private static float wetter = SColor.floatGetI(20, 70, 255);
//    private static float wettest = SColor.floatGetI(0, 0, 100);

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
            foamColor
    }
//    , biomeDarkColors = {
//            darkDesert,
//            darkSavanna,
//            darkTropicalRainforest,
//            darkGrassland,
//            darkWoodland,
//            darkSeasonalForest,
//            darkTemperateRainforest,
//            darkBorealForest,
//            darkTundra,
//            darkIce,
//            darkBeach,
//            darkRocky,
//            darkFoamColor
//    }
    ;

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
    }, BIOME_COLOR_TABLE = new float[54], BIOME_DARK_COLOR_TABLE = new float[54];

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
    }

    /*
    protected void makeBiomes() {
        final WorldMapGenerator world = this.world;
        final int[][] heightCodeData = world.heightCodeData;
        final double[][] heatData = world.heatData, moistureData = world.moistureData, heightData = world.heightData;
        int hc, mc, heightCode;
        double hot, moist, high, i_hot = 1.0 / this.world.maxHeat;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                heightCode = heightCodeData[x][y];
                hot = heatData[x][y];
                moist = moistureData[x][y];
                high = heightData[x][y];
                boolean isLake = heightCode >= 4 && world.partialLakeData.contains(x, y),
                        isRiver = heightCode >= 4 && world.partialRiverData.contains(x, y);
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
    }
    */

    private String date, path;

    @Override
    public void create() {
        batch = new SpriteBatch();
        //display = new SquidPanel(width, height, cellWidth, cellHeight);
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        //stage = new Stage(view, batch);
        date = DateFormat.getDateInstance().format(new Date());
        //path = "out/worlds/Sphere " + date + "/";
        //path = "out/worlds/Tiling " + date + "/";
        path = "out/worlds/AltSphere " + date + "/";
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGB888);
        pm.setBlending(Pixmap.Blending.None);
        pt = new Texture(pm);
        rng = new StatefulRNG(CrossHash.hash64(date));
        seed = rng.getState();
        ///world = new WorldMapGenerator.SphereMap(seed, width, height, WhirlingNoise.instance, 0.9);
        //world = new WorldMapGenerator.TilingMap(seed, width, height, WhirlingNoise.instance, 1.625);
        world = new WorldMapGenerator.SphereMapAlt(seed, width, height, WhirlingNoise.instance, 1.625);
        dbm = new WorldMapGenerator.DetailedBiomeMapper();
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
        dbm.makeBiomes(world);
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
        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }
    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        world.generate(seed);
        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        String name = lang.word(rng, true); //, Math.min(3 - rng.next(1), rng.betweenWeighted(1, 5, 4))
        while (Gdx.files.local(path + name + ".png").exists())
            name = lang.word(rng, true);

        generate(CrossHash.hash64(name));
        //display.erase();
        int hc, tc, bc;
        int[][] heightCodeData = world.heightCodeData;
        double[][] heightData = world.heightData;
        int[][] heatCodeData = dbm.heatCodeData;
        int[][] biomeCodeData = dbm.biomeCodeData;
        for (int y = 0; y < height; y++) {
            PER_CELL:
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
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
                            Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0))));
                            pm.setColor(tempColor);
                            pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
//                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
                            //display.put(x, y, SColor.lerpFloatColors(shallowColor, ice,
                            //        (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0))));
                            continue PER_CELL;
                        case 4:
                            Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - 0.1) / (0.18 - 0.1))));
                            pm.setColor(tempColor);
                            pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
//                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
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
                        Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(deepColor, coastalColor,
                                (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0))));
                        pm.setColor(tempColor);
                        pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
//                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
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

                        Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)],
                                BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)], dbm.extractMixAmount(bc)));
                        pm.setColor(tempColor);
                        pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
//                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
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
        batch.draw(pt, 0, 0);
        batch.end();
        PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);

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
        if(++counter >= 4)
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
        config.fullscreen = true;
        config.foregroundFPS = 0;
        //config.fullscreen = true;
        config.backgroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DetailedWorldMapWriter(), config);
    }
}
