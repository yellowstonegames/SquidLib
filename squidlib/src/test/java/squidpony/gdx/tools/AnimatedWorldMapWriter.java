package squidpony.gdx.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;
import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.WorldMapView;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.*;

import java.text.DateFormat;
import java.util.Date;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 */
public class AnimatedWorldMapWriter extends ApplicationAdapter {
//    private static final int width = 1920, height = 1080;
//    private static final int width = 256, height = 256; // localMimic
//    private static final int width = 512, height = 256; // mimic, elliptical
//    private static final int width = 1024, height = 512; // mimic, elliptical
//    private static final int width = 2048, height = 1024; // mimic, elliptical
    private static final int width = 260, height = 260; // space view
//    private static final int width = 1200, height = 400; // squat
    private static final int LIMIT = 5;
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
    private Pixmap[] pm;
    private Texture pt;
    private int counter;
    private static final int cellWidth = 1, cellHeight = 1;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private long ttg; // time to generate
    private WorldMapGenerator world;
    private WorldMapView wmv;
    private AnimatedGif writer;
    
    private String date, path;
    private double mutationW = NumberTools.sin(0.75), mutationU = NumberTools.cos(0.75);
    @Override
    public void create() {
        batch = new FilterBatch();
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        date = DateFormat.getDateInstance().format(new Date());
//        path = "out/worldsAnimated/" + date + "/Sphere/";
//        path = "out/worldsAnimated/" + date + "/SphereQuilt/";
//        path = "out/worldsAnimated/" + date + "/SphereQuilt/";
//        path = "out/worldsAnimated/" + date + "/SphereExpo/";
//        path = "out/worldsAnimated/" + date + "/Ellipse/";
//        path = "out/worldsAnimated/" + date + "/EllipseExpo/";
//        path = "out/worldsAnimated/" + date + "/Mimic/";
//        path = "out/worldsAnimated/" + date + "/SpaceView/";
        path = "out/worldsAnimated/" + date + "/SpaceViewMutant/";
//        path = "out/worldsAnimated/" + date + "/Sphere_Classic/";
//        path = "out/worldsAnimated/" + date + "/Hyperellipse/";
//        path = "out/worldsAnimated/" + date + "/HyperellipseExpo/";
//        path = "out/worldsAnimated/" + date + "/HyperellipseQuilt/";
//        path = "out/worldsAnimated/" + date + "/Tiling/";
//        path = "out/worldsAnimated/" + date + "/RoundSide/";
//        path = "out/worldsAnimated/" + date + "/Local/";
//        path = "out/worldsAnimated/" + date + "/LocalSquat/";
//        path = "out/worldsAnimated/" + date + "/LocalMimic/";
//        path = "out/worldsAnimated/" + date + "/EllipseHammer/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();

        pm = new Pixmap[360];
        for (int i = 0; i < pm.length; i++) {
            pm[i] = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
            pm[i].setBlending(Pixmap.Blending.None);
        }
        pt = new Texture(pm[0]);

        writer = new AnimatedGif();
        writer.setFlipY(false);
//        writer.palette = new PaletteReducer();
//        writer.palette.setDitherStrength(1f);
        rng = new StatefulRNG(CrossHash.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date  
        //rng = new StatefulRNG(0L);
        seed = rng.getState();
        
        thesaurus = new Thesaurus(rng);
//        Noise.Noise3D noise = new FastNoise((int)seed, 2.75f, FastNoise.FOAM_FRACTAL, 2);
        Noise.Noise3D noise = new Noise.Noise3D() {
//            private FastNoise fn = new FastNoise((int)seed, 2.75f, FastNoise.FOAM, 1);
            @Override
            public double getNoise(double x, double y, double z) {
                return FoamNoise.foamNoise(x * 2.75, y * 2.75, z * 2.75, mutationW, mutationU, 123456789);
            }

            @Override
            public double getNoiseWithSeed(double x, double y, double z, long seed) {
                return FoamNoise.foamNoise(x * 2.75, y * 2.75, z * 2.75, mutationW, mutationU, (int) seed);
            }
        };


//        Noise.Noise3D noise = new Noise.Exponential3D(new FastNoise((int)seed, 2.75f, FastNoise.FOAM_FRACTAL, 3));
//        FastNoise noise = new FastNoise((int)seed, 8f, FastNoise.CUBIC_FRACTAL, 1);
//        noise.setPointHash(new FlawedPointHash.CubeHash(seed, 1 << 14));
//        Noise.Noise3D noise = new Noise.Exponential3D(fn);
//        WorldMapGenerator.DEFAULT_NOISE.setNoiseType(FastNoise.FOAM_FRACTAL);
//        WorldMapGenerator.DEFAULT_NOISE.setFrequency(2f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalOctaves(3);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalLacunarity(2f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalGain(0.5f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalLacunarity(0.2f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalGain(5f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalLacunarity(0.8f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalGain(1.25f);

//        world = new WorldMapGenerator.RotatingSpaceMap(seed, width, height, noise, 1.0);
        
//        world = new WorldMapGenerator.SphereMap(seed, width, height, noise, 1.0);
//        world = new WorldMapGenerator.TilingMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, noise, 1.75);
//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, noise, 0.75);
//        world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8, 0.03125, 2.5);
//        world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.8, 0.03125, 2.5);
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
//        wmv.generate();
        wmv.generate(
                //1.45,
                1.0 + NumberTools.formCurvedDouble(world.seedA * 0x123456789ABCDEFL ^ world.seedB) * 0.1875,
                1.0 + DiverRNG.determineDouble(world.seedB * 0x123456789ABL ^ world.seedA) * 0.3125);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = makeName(thesaurus);
        while (Gdx.files.local(path + name + ".gif").exists())
            name = makeName(thesaurus);
        long hash = CrossHash.hash64(name);
        for (int i = 0; i < pm.length; i++) {
            double angle = (Math.PI * 2.0 / pm.length) * i;
            
            mutationW = NumberTools.sin(angle);
            mutationU = NumberTools.cos(angle);
//            mutation = NumberTools.sin(angle) * 0.918 + NumberTools.cos(angle * 4.0 + 1.618) * 0.307;
            generate(hash);
            world.setCenterLongitude(angle);
            wmv.getBiomeMapper().makeBiomes(world);
            float[][] cm = wmv.show();
            pm[i].setColor(SColor.DB_INK);
            pm[i].fill();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pm[i].drawPixel(x, y, NumberTools.floatToReversedIntBits(cm[x][y]));
                }
            }
            if(i % 5 == 4) System.out.println("Finished " + (i + 1) + " frames");
        }
        
        batch.begin();
        pt.draw(pm[0], 0, 0);
        batch.draw(pt, 0, 0, width, height);
        batch.end();

        writer.write(Gdx.files.local(path + name + ".gif"), new Array<Pixmap>(pm), 30);

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
        //config.fullscreen = true;
        config.foregroundFPS = 0;
        //config.fullscreen = true;
        config.backgroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new AnimatedWorldMapWriter(), config);
    }
}
