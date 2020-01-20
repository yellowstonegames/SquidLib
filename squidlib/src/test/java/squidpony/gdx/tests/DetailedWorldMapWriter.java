package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;
import squidpony.squidgrid.gui.gdx.*;
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
//    private static final int width = 1920, height = 1080;
//    private static final int width = 256, height = 256; // localMimic
//    private static final int width = 512, height = 256; // mimic, elliptical
    private static final int width = 1024, height = 512; // mimic, elliptical
//    private static final int width = 1000, height = 1000; // space view
    private static final int LIMIT = 6;
    //private static final int width = 256, height = 128;
    //private static final int width = 314 * 4, height = 400;
    //private static final int width = 512, height = 512;

    private FilterBatch batch;
//    private OrderedSet<String> adjective = new OrderedSet<>(256), noun = new OrderedSet<>(256);
    private Thesaurus thesaurus;
    private String makeName(final Thesaurus thesaurus)
    {
        return StringKit.capitalize(thesaurus.makePlantName(FakeLanguageGen.MALAY).replaceAll("'s", "")).replaceAll("\\W", "");
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
    private static final int cellWidth = 1, cellHeight = 1;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private long ttg = 0; // time to generate
    private WorldMapGenerator world;
    private WorldMapView wmv;
    private PixmapIO.PNG writer;
    
    private String date, path;

    @Override
    public void create() {
//        earth.perceptualHashQuick(earthHash, workingHash);
//        earthCount = earth.size() - voidCount;
        batch = new FilterBatch();
        //display = new SquidPanel(width, height, cellWidth, cellHeight);
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        //stage = new Stage(view, batch);
        date = DateFormat.getDateInstance().format(new Date());
//        path = "out/worlds/" + date + "/Sphere/";
//        path = "out/worlds/" + date + "/Ellipse/";
//        path = "out/worlds/" + date + "/Mimic/";
//        path = "out/worlds/" + date + "/SpaceView/";
//        path = "out/worlds/" + date + "/Sphere_Classic/";
//        path = "out/worlds/" + date + "/Hyperellipse/";
        path = "out/worlds/" + date + "/HyperellipseFoam/";
//        path = "out/worlds/" + date + "/Tiling/";
//        path = "out/worlds/" + date + "/RoundSide/";
//        path = "out/worlds/" + date + "/Local/";
//        path = "out/worlds/" + date + "/LocalMimic/";
//        path = "out/worlds/" + date + "/EllipseHammer/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        //Gdx.files.local(path + "Earth.txt").writeString(StringKit.hex(earthHash), false);

        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pt = new Texture(pm);

        writer = new PixmapIO.PNG((int)(pm.getWidth() * pm.getHeight() * 1.5f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
//        writer.palette = new PaletteReducer();
//        writer.palette.setDitherStrength(1.75f);
        rng = new StatefulRNG(CrossHash.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date  
        //rng = new StatefulRNG(0L);
        seed = rng.getState();
        
        thesaurus = new Thesaurus(rng);

        WorldMapGenerator.DEFAULT_NOISE.setNoiseType(FastNoise.SIMPLEX_FRACTAL);
//        WorldMapGenerator.DEFAULT_NOISE.setFrequency(1.5f);
        WorldMapGenerator.DEFAULT_NOISE.setFractalOctaves(3);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalLacunarity(2f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalGain(0.5f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalLacunarity(0.2f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalGain(5f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalLacunarity(0.8f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalGain(1.25f);
        
//        world = new WorldMapGenerator.SphereMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.TilingMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75, 0.03125, 2.5);
//        world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, new Noise.Layered3D(FoamNoise.instance, 2, 3.5, 0.625), 0.375, 0.03125, 2.5);
        wmv = new WorldMapView(world);

        //generate(seed);
        rng.setState(seed);
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
//        world.generate(0.95 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.15,
//                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.2 + 1.0, seed);
//        dbm.makeBiomes(world);
        world.rng.setState(seed);
        world.seedA = world.rng.stateA;
        world.seedB = world.rng.stateB;
        wmv.generate();
//        wmv.generate(1.0 + NumberTools.formCurvedDouble((world.seedA ^ 0x123456789ABCDL) * 0x12345689ABL ^ world.seedB) * 0.25,
//                DiverRNG.determineDouble(world.seedB * 0x12345L + 0x54321L ^ world.seedA) * 0.25 + 1.0);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = makeName(thesaurus);
        while (Gdx.files.local(path + name + ".png").exists())
            name = makeName(thesaurus);
        generate(CrossHash.hash64(name));
        
//        greasedWorld.refill(heightCodeData, 4, 10001).perceptualHashQuick(worldHash, workingHash);
//        worldCount = greasedWorld.size() - voidCount;
//        intersectionCount = greasedWorld.and(earth).size() - voidCount;
//        double jaccard = intersectionCount / (earthCount + worldCount - intersectionCount);
//        if(jaccard < 0.3)
//            return;
        float[][] cm = wmv.show();
        pm.setColor(SColor.DB_INK);
        pm.fill();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pm.drawPixel(x, y, NumberTools.floatToReversedIntBits(cm[x][y]));
            }
        }

        //writer.palette.analyze(pm);
        //writer.palette.reduceFloydSteinberg(pm);

        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0, width >> 1, height >> 1);
        batch.end();

        //        PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);
        try {
            writer.write(Gdx.files.local(path + name + ".png"), pm); // , false);
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
        config.width = width * cellWidth >> 1;
        config.height = height * cellHeight >> 1;
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
