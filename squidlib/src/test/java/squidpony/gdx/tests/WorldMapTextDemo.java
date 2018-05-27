package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.PoliticalMapper;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.WhirlingNoise;

/**
 * Map generator that uses text to show features at a location as well as color.
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 * Currently, clouds are in progress, and look like <a href="http://i.imgur.com/Uq7Whzp.gifv">this preview</a>.
 */
public class WorldMapTextDemo extends ApplicationAdapter {
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
    
    public static final char[]  terrainChars = {
            '¿', //sand
            '„', //lush grass
            '♣', //jungle
            '‚', //barren grass
            '¥', //forest
            '¥', //forest
            '♣', //jungle
            '¥', //forest
            '‚', //barren grass
            '¤', //ice
            '.', //sand
            '∆', //rocky
            '~', //shallow
            '≈', //ocean
            ' ', //empty space
            //'■', //active city
            '□', //empty city
            
            
    };
    //private static final int bigWidth = 314 * 3, bigHeight = 300;
    //private static final int bigWidth = 1024, bigHeight = 512;
    private static final int bigWidth = 512, bigHeight = 256;
    //private static final int bigWidth = 400, bigHeight = 400;
    private static final int cellWidth = 17, cellHeight = 17;
    private static final int shownWidth = 96, shownHeight = 48;
    private SpriteBatch batch;
    private SparseLayers display;//, overlay;
    private SquidInput input;
    private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private Vector3 position, previousPosition, nextPosition;
    private WorldMapGenerator.MimicMap world;
    private PoliticalMapper pm;
    private OrderedMap<Character, FakeLanguageGen> atlas;
    private OrderedMap<Coord, String> cities;
    //private WorldMapGenerator.EllipticalMap world;
    //private final float[][][] cloudData = new float[128][128][128];
    private long counter = 0;
    //private float nation = 0f;
    private long ttg = 0; // time to generate
    private float moveAmount = 0f, moveLength = 1000f;
    private WorldMapGenerator.DetailedBiomeMapper dbm;
    private char[][] political;
    private static float black = SColor.FLOAT_BLACK,
            white = SColor.FLOAT_WHITE;
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

    private static float emptyColor = SColor.DB_INK.toFloatBits();

    // water colors
    private static float deepColor = SColor.floatGetI(0, 42, 88);
    private static float mediumColor = SColor.floatGetI(0, 89, 159);
    private static float shallowColor = SColor.floatGetI(0, 73, 137);
    private static float coastalColor = SColor.lerpFloatColors(shallowColor, white, 0.3f);
    private static float foamColor = SColor.floatGetI(61,  162, 215);

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
    private static final char[] BIOME_CHARS = new char[61];
    private static final float[] NATION_COLORS = new float[144];
    static {
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            BIOME_CHARS[i] = terrainChars[(int)b];
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lerpFloatColors(biomeColors[(int)b], white, diff)
                    : SColor.lerpFloatColors(biomeColors[(int)b], black, -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.lerpFloatColors(b, black, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = emptyColor;
        BIOME_CHARS[60] = ' ';
        for (int i = 0; i < 144; i++) {
            NATION_COLORS[i] =  SColor.COLOR_WHEEL_PALETTE_REDUCED[((i + 1234567) * 13 & 0x7FFFFFFF) % 144].toFloatBits();
        }
    }
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        display = new SparseLayers(bigWidth, bigHeight, cellWidth, cellHeight, DefaultResources.getCrispLeanFont());
        display.font.tweakHeight(18f).tweakWidth(15f);
        view = new StretchViewport(shownWidth * cellWidth, shownHeight * cellHeight);
        stage = new Stage(view, batch);
        seed = 0xDEBACL;
        rng = new StatefulRNG(seed);
        //world = new WorldMapGenerator.TilingMap(seed, bigWidth, bigHeight, WhirlingNoise.instance, 1.25);
        //world = new WorldMapGenerator.EllipticalMap(seed, bigWidth, bigHeight, WhirlingNoise.instance, 0.8);
        world = new WorldMapGenerator.MimicMap(seed, WhirlingNoise.instance, 0.8);
        //world = new WorldMapGenerator.TilingMap(seed, bigWidth, bigHeight, WhirlingNoise.instance, 0.9);
        world.generateRivers = false;
        dbm = new WorldMapGenerator.DetailedBiomeMapper();
        pm = new PoliticalMapper(FakeLanguageGen.SIMPLISH.word(rng, true));
        cities = new OrderedMap<>(96);
        position = new Vector3(bigWidth * cellWidth * 0.5f, bigHeight * cellHeight * 0.5f, 0);
        previousPosition = position.cpy();
        nextPosition = position.cpy();
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setState(seed);
                        break;
                    case SquidInput.DOWN_ARROW:
                        position.add(0, 1, 0);
                        break;
                    case SquidInput.UP_ARROW:
                        position.add(0, -1, 0);
                        break;
                    case SquidInput.LEFT_ARROW:
                        position.add(-1, 0, 0);
                        break;
                    case SquidInput.RIGHT_ARROW:
                        position.add(1, 0, 0);
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                Gdx.graphics.requestRendering();
            }
        }, new SquidMouse(1, 1, bigWidth * cellWidth, bigHeight * cellHeight, 0, 0, new InputAdapter()
        {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                previousPosition.set(position);
                nextPosition.set(screenX, screenY, 0);
                stage.getCamera().unproject(nextPosition);
                nextPosition.set(MathUtils.round(nextPosition.x), MathUtils.round(nextPosition.y), nextPosition.z);
                counter = System.currentTimeMillis();
                moveAmount = 0f;
                return true;
            }
        }));
        generate(seed);
        rng.setState(seed);
        Gdx.input.setInputProcessor(input);
        display.setPosition(0, 0);
        stage.addActor(display);
    }

    public void zoomIn() {
        zoomIn(bigWidth >> 1, bigHeight >> 1);
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
        world.zoomIn(1, zoomX, zoomY);
        dbm.makeBiomes(world);
        //counter = 0L;
        ttg = System.currentTimeMillis() - startTime;
    }
    public void zoomOut()
    {
        zoomOut(bigWidth >>1, bigHeight >>1);
    }
    public void zoomOut(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
        world.zoomOut(1, zoomX, zoomY);
        dbm.makeBiomes(world);
        //counter = 0L;
        ttg = System.currentTimeMillis() - startTime;
    }
    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        System.out.println("Seed used: 0x" + StringKit.hex(seed) + "L");
        world.generate(1.0, 1.125, seed);
        dbm.makeBiomes(world);
        atlas = new OrderedMap<>(80);
        atlas.clear();
        for (int i = 0; i < 64; i++) {
            atlas.put(ArrayTools.letterAt(i), rng.getRandomElement(FakeLanguageGen.romanizedHumanLanguages));
        }
        political = pm.generate(world, atlas, 1.0);
        cities.clear();
        Coord[] points = world.earth.copy().disperse8way().removeEdges().mixedRandomSeparated(0.05, 96, rng.nextLong());
        for (int i = 0; i < points.length; i++) {
            char p = political[points[i].x][points[i].y];
            if(p == '~' || p == '%')
                continue;
            FakeLanguageGen lang = atlas.get(p);
            if(lang != null)
            {
                cities.put(points[i], lang.word(rng, true));
            }
        }
        //counter = 0L;
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        // uncomment next line to generate maps as quickly as possible
        //generate(rng.nextLong());
        ArrayTools.fill(display.backgrounds, -0x1.0p125F);
        int hc, tc, codeA, codeB;
        float shown, mix;
        int[][] heightCodeData = world.heightCodeData;
        double[][] heightData = world.heightData;
        //double xp, yp, zp;
        for (int y = 0; y < bigHeight; y++) {
            PER_CELL:
            for (int x = 0; x < bigWidth; x++) {
                hc = heightCodeData[x][y];
                if(hc == 1000)
                    continue;
                tc = dbm.heatCodeData[x][y];
                if(tc == 0)
                {
                    switch (hc)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            shown = SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)));
//                            if(cloud > 0.0)
//                                shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
                            display.put(x, y, '~', SColor.lerpFloatColors(ice, black, 0.35f), shown);
                            continue PER_CELL;
                        case 4:
                            shown = SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)));
//                            if(cloud > 0.0)
//                                shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
                            display.put(x, y, '¤', SColor.lerpFloatColors(ice, black, 0.25f), shown);
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                        shown = SColor.lerpFloatColors(deepColor, coastalColor,
                                (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)));
                        display.put(x, y, '≈', SColor.lerpFloatColors(foamColor, white, 0.3f), shown);
                        break;
                    case 3:
                        shown = SColor.lerpFloatColors(deepColor, coastalColor,
                                (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)));
                        display.put(x, y, '~', SColor.lerpFloatColors(foamColor, white, 0.3f), shown);
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
                        int bc = dbm.biomeCodeData[x][y];
                        shown = SColor.lerpFloatColors(BIOME_COLOR_TABLE[codeB = dbm.extractPartB(bc)],
                                BIOME_DARK_COLOR_TABLE[codeA = dbm.extractPartA(bc)], mix = dbm.extractMixAmount(bc));
//                        if(cloud > 0.0)
//                            shown = SColor.lerpFloatColors(shown, cloudLight, cloud);
                        if(mix >= 0.5) 
                            display.put(x, y, BIOME_CHARS[codeA], SColor.lerpFloatColors(BIOME_COLOR_TABLE[codeB], black, 0.3f), shown);
                        else
                            display.put(x, y, BIOME_CHARS[codeB], SColor.lerpFloatColors(BIOME_COLOR_TABLE[codeA], black, 0.3f), shown);

                        //display.put(x, y, SColor.lerpFloatColors(darkTropicalRainforest, desert, (float) (heightData[x][y])));
                }
            }
        }
        for (int i = 0; i < cities.size(); i++) {
            Coord ct = cities.keyAt(i);
            String cname = cities.getAt(i);
            display.put(ct.x, ct.y, '□', SColor.SOOTY_WILLOW_BAMBOO);
            display.put(ct.x - (cname.length() >> 1), ct.y - 1, cname, SColor.WHITE, SColor.SOOTY_WILLOW_BAMBOO);
        }
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(SColor.DB_INK.r, SColor.DB_INK.g, SColor.DB_INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if(!nextPosition.epsilonEquals(previousPosition)) {
            moveAmount = (System.currentTimeMillis() - counter) * 0.001f;
            if (moveAmount <= 1f) {
                position.set(previousPosition).lerp(nextPosition, moveAmount);
                position.set(MathUtils.round(position.x), MathUtils.round(position.y), position.z);
            }
            else {
                previousPosition.set(position);
                nextPosition.set(position);
                nextPosition.set(MathUtils.round(nextPosition.x), MathUtils.round(nextPosition.y), nextPosition.z);
                moveAmount = 0f;
                counter = System.currentTimeMillis();
            }
        }
        stage.getCamera().position.set(position);
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
        config.width = shownWidth * cellWidth;
        config.height = shownHeight * cellHeight;
        config.foregroundFPS = 60;
        //config.fullscreen = true;
        config.backgroundFPS = -1;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new WorldMapTextDemo(), config);
    }
}