package squidpony.gdx.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
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
//    private static final int width = 420, height = 210; // mimic, elliptical
//    private static final int width = 512, height = 256; // mimic, elliptical
//    private static final int width = 1024, height = 512; // mimic, elliptical
//    private static final int width = 2048, height = 1024; // mimic, elliptical
    private static final int width = 256, height = 256; // space view
//    private static final int width = 1200, height = 400; // squat
    //private static final int width = 256, height = 128;
    //private static final int width = 314 * 4, height = 400;
    //private static final int width = 512, height = 512;

    private static final int LIMIT = 5;
    private static final boolean MEASURE_BOUNDS = false;

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
    private long ttg, worldTime; // time to generate, world starting time
    private WorldMapGenerator world;
    private WorldMapView wmv;
    private AnimatedGif writer;
    
    private String date, path;
    private Noise.Adapted3DFrom5D noise;
//    private double mutationA = NumberTools.cos(0.75) * 3.0, mutationB = NumberTools.sin(0.75) * 3.0;
    public IntMap<int[]> bounds = new IntMap<>(20);
//    private double mutationC = NumberTools.cos(1.5), mutationD = NumberTools.sin(1.5);
    
    /*
    Both of these use FastNoise 5D Simplex (adapted so 3 dimensions can be changed by WorldMapGenerator);
    the first experiments with using Noise.WarpedRidged3D inside WorldMapGenerator,
    while the second uses the normal Ridged3D. They have almost identical results.
    
SpaceViewMutantWarpedRidged
10% (6719 ms)... 20% (13237 ms)... 30% (19842 ms)... 40% (26447 ms)... 50% (33010 ms)... 60% (39540 ms)... 70% (46041 ms)... 80% (52567 ms)... 90% (59130 ms)... 100% (65659 ms)... 
World #1, FibrousBudWillow, completed in 67614 ms
10% (6499 ms)... 20% (13653 ms)... 30% (20148 ms)... 40% (26667 ms)... 50% (33188 ms)... 60% (39721 ms)... 70% (46113 ms)... 80% (52592 ms)... 90% (59133 ms)... 100% (65660 ms)... 
World #2, AgigikFragrantTwig, completed in 69591 ms
10% (6478 ms)... 20% (12887 ms)... 30% (19395 ms)... 40% (25946 ms)... 50% (32497 ms)... 60% (38958 ms)... 70% (45403 ms)... 80% (51897 ms)... 90% (58423 ms)... 100% (64924 ms)... 
World #3, RedSapLotus, completed in 68874 ms
10% (6467 ms)... 20% (12886 ms)... 30% (19383 ms)... 40% (25921 ms)... 50% (32415 ms)... 60% (38861 ms)... 70% (45263 ms)... 80% (51775 ms)... 90% (58282 ms)... 100% (64779 ms)... 
World #4, SugarrootMagnolia, completed in 66622 ms
10% (6477 ms)... 20% (12898 ms)... 30% (19447 ms)... 40% (25963 ms)... 50% (32475 ms)... 60% (38930 ms)... 70% (45362 ms)... 80% (51862 ms)... 90% (58385 ms)... 100% (64902 ms)... 
World #5, SavoryMelonAlder, completed in 66787 ms

SpaceViewMutantRidged
10% (6390 ms)... 20% (12607 ms)... 30% (18922 ms)... 40% (25184 ms)... 50% (31470 ms)... 60% (37695 ms)... 70% (43879 ms)... 80% (50118 ms)... 90% (56421 ms)... 100% (62670 ms)... 
World #1, FibrousBudWillow, completed in 64647 ms
10% (6210 ms)... 20% (12329 ms)... 30% (18827 ms)... 40% (25376 ms)... 50% (31940 ms)... 60% (38497 ms)... 70% (44864 ms)... 80% (51093 ms)... 90% (57406 ms)... 100% (63685 ms)... 
World #2, AgigikFragrantTwig, completed in 65527 ms
10% (6215 ms)... 20% (12430 ms)... 30% (18678 ms)... 40% (24963 ms)... 50% (31235 ms)... 60% (37479 ms)... 70% (43627 ms)... 80% (49879 ms)... 90% (56185 ms)... 100% (62435 ms)... 
World #3, RedSapLotus, completed in 64326 ms
10% (6195 ms)... 20% (12412 ms)... 30% (18681 ms)... 40% (24921 ms)... 50% (31207 ms)... 60% (37417 ms)... 70% (43601 ms)... 80% (49881 ms)... 90% (56204 ms)... 100% (62472 ms)... 
World #4, SugarrootMagnolia, completed in 64317 ms
10% (6251 ms)... 20% (12471 ms)... 30% (18753 ms)... 40% (25067 ms)... 50% (31353 ms)... 60% (37617 ms)... 70% (43748 ms)... 80% (50008 ms)... 90% (56251 ms)... 100% (62497 ms)... 
World #5, SavoryMelonAlder, completed in 64338 ms

     */
    
    
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
//        path = "out/worldsAnimated/" + date + "/SpaceViewMutantClassic/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewMutantFoam/";
        path = "out/worldsAnimated/" + date + "/SpaceViewMutantHoney/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewHoney/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewFoam/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewRidged/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewMutantMaelstrom/";
//        path = "out/worldsAnimated/" + date + "/HyperellipseWrithing/";
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
        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.GRADIENT_NOISE);
        writer.setFlipY(false);
//        writer.palette = new PaletteReducer();
//        writer.palette.setDitherStrength(1.0f);
        rng = new StatefulRNG(CrossHash.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date  
        //rng = new StatefulRNG(0L);
        seed = rng.getState();
        
        thesaurus = new Thesaurus(rng);
//        Noise.Noise3D noise = new FastNoise((int)seed, 2.75f, FastNoise.FOAM_FRACTAL, 2);
//        Noise.Noise3D noise = new Noise.Noise3D() {
//            @Override
//            public double getNoise(double x, double y, double z) {
//                return FoamNoise.foamNoise(x * mutationC + mutationA, y * mutationC + mutationB, z * mutationC + mutationD, 123456789);
//            }
//
//            @Override
//            public double getNoiseWithSeed(double x, double y, double z, long seed) {
//                return FoamNoise.foamNoise(x * mutationC + mutationA, y * mutationC + mutationB, z * mutationC + mutationD, (int) seed);
//            }
//        };

//        final Noise.Noise3D noise = new Noise.Noise3D() {
//            @Override
//            public double getNoise(double x, double y, double z) {
//                return WorldMapGenerator.DEFAULT_NOISE.getNoiseWithSeed(x, y, z, mutationA, mutationB, 123456789);
//            }
//
//            @Override
//            public double getNoiseWithSeed(double x, double y, double z, long seed) {
//                return WorldMapGenerator.DEFAULT_NOISE.getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
//            }
//        };
        
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
        FastNoise fn = new VastNoise((int) seed, 1.25f, FastNoise.HONEY, 1);
        
        noise = new Noise.Adapted3DFrom5D(fn);
//        WorldMapGenerator.DEFAULT_NOISE.setNoiseType(FastNoise.HONEY);
//        WorldMapGenerator.DEFAULT_NOISE.setFrequency(1.25f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalOctaves(1);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalType(FastNoise.BILLOW);
        
        /*
          With these settings and a low octave multiplier of 0.42, the hashes at their widest range span from -20 to 20
          on all axes. A 64x64x64 box, centered on the origin, of random ints could be used instead of calculating a
          hash for each point over and over (or a larger box to permit more octaves).
         */
//        if(MEASURE_BOUNDS)
//        {
//            WorldMapGenerator.DEFAULT_NOISE.setPointHash(new IPointHash.IntImpl() {
//                @Override
//                public int hashWithState(int x, int y, int state) {
//                    int[] b;
//                    if((b = bounds.get(state)) == null)
//                    {
//                        b = new int[4];
//                        bounds.put(state, b);
//                    }
//                    b[0] = Math.min(b[0], x);
//                    b[1] = Math.max(b[1], x);
//                    b[2] = Math.min(b[2], y);
//                    b[3] = Math.max(b[3], y);
//                    return IntPointHash.hashAll(x, y, state);
//                }
//
//                @Override
//                public int hashWithState(int x, int y, int z, int state) {
//                    int[] b;
//                    if((b = bounds.get(state)) == null)
//                    {
//                        b = new int[6];
//                        bounds.put(state, b);
//                    }
//                    b[0] = Math.min(b[0], x);
//                    b[1] = Math.max(b[1], x);
//                    b[2] = Math.min(b[2], y);
//                    b[3] = Math.max(b[3], y);
//                    b[4] = Math.min(b[4], z);
//                    b[5] = Math.max(b[5], z);
//                    return IntPointHash.hashAll(x, y, z, state);
//                }
//
//                @Override
//                public int hashWithState(int x, int y, int z, int w, int state) {
//                    int[] b;
//                    if((b = bounds.get(state)) == null)
//                    {
//                        b = new int[6];
//                        bounds.put(state, b);
//                    }
//                    b[0] = Math.min(b[0], x);
//                    b[1] = Math.max(b[1], x);
//                    b[2] = Math.min(b[2], y);
//                    b[3] = Math.max(b[3], y);
//                    b[4] = Math.min(b[4], z);
//                    b[5] = Math.max(b[5], z);
//                    b[6] = Math.min(b[6], w);
//                    b[7] = Math.max(b[7], w);
//                    return IntPointHash.hashAll(x, y, z, w, state);
//                }
//
//                @Override
//                public int hashWithState(int x, int y, int z, int w, int u, int state) {
//                    return IntPointHash.hashAll(x, y, z, w, u, state);
//                }
//
//                @Override
//                public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
//                    return IntPointHash.hashAll(x, y, z, w, u, v, state);
//                }
//            });
//        }
//        world = new WorldMapGenerator.RotatingSpaceMap(seed, width, height, noise, 1.0);
        
//        world = new WorldMapGenerator.SphereMap(seed, width, height, noise, 1.0);
//        world = new WorldMapGenerator.TilingMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, noise, 1.75);
//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, noise, 0.58);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, noise, 0.5);
//        world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8, 0.03125, 2.5);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.5, 0.03125, 2.5);
//        world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.8, 0.03125, 2.5);
        wmv = new WorldMapView(world);

        //generate(seed);
        rng.setState(seed);
        Gdx.graphics.setContinuousRendering(false);
        for (int i = 0; i < LIMIT; i++) {
            putMap();
        }
        Gdx.app.exit();
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
                1.0625 + DiverRNG.determineDouble(world.seedB * 0x123456789ABL ^ world.seedA) * 0.375);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = makeName(thesaurus);
        while (Gdx.files.local(path + name + ".gif").exists())
            name = makeName(thesaurus);
        long hash = CrossHash.hash64(name);
        worldTime = System.currentTimeMillis();
        if(MEASURE_BOUNDS)
            bounds.clear();
        for (int i = 0; i < pm.length; i++) {
            double angle = (Math.PI * 2.0 / pm.length) * i;

//            mutationC = NumberTools.cos(angle * 5.0) * 0.5;
//            mutationD = NumberTools.sin(angle * 5.0) * 0.5;
//            mutationA = NumberTools.cos(angle) * (mutationC + 2.0);
//            mutationB = NumberTools.sin(angle) * (mutationC + 2.0);
//            mutationC = NumberTools.cos(angle * 3.0 + 1.0) * 0.625 + 2.25;
//            mutationA = NumberTools.cos(angle) * 0.3125;
//            mutationB = NumberTools.sin(angle) * 0.3125;
            noise.w = NumberTools.cos(angle) * 0.25;
            noise.u = NumberTools.sin(angle) * 0.25;
            //            mutation = NumberTools.sin(angle) * 0.918 + NumberTools.cos(angle * 4.0 + 1.618) * 0.307;

            //// this next line should not usually be commented out, but it makes sense not to have it when you can see the whole map.
            world.setCenterLongitude(angle);
            generate(hash);
            wmv.getBiomeMapper().makeBiomes(world);
            float[][] cm = wmv.show();
            pm[i].setColor(SColor.DB_INK);
            pm[i].fill();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pm[i].drawPixel(x, y, NumberTools.floatToReversedIntBits(cm[x][y]));
                }
            }
//            if(i % 5 == 4) System.out.println("Finished " + (i + 1) + " frames in " + (System.currentTimeMillis() - worldTime) + " ms");
            if(i % 36 == 35) System.out.print(((i + 1) * 10 / 36) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");
        }
//        Array<Pixmap> apm = new Array<Pixmap>(pm);
//        writer.palette.analyze(apm);
        writer.write(Gdx.files.local(path + name + ".gif"), new Array<Pixmap>(pm), 30);

//        Gdx.graphics.requestRendering();
        
        System.out.println();
        System.out.println("World #" + counter + ", " + name + ", completed in " + (System.currentTimeMillis() - worldTime) + " ms");
        if(MEASURE_BOUNDS) {
            System.out.println(bounds.size);
            for (IntMap.Entry<int[]> e : bounds) {
                System.out.println("0x" + StringKit.hex(e.key) + ": " + StringKit.join(", ", e.value));
            }
        }

//        if(counter >= LIMIT)
//        {
//            Gdx.app.exit();
//        }
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        //Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        batch.begin();
        if(pm[0] != null) 
            pt.draw(pm[0], 0, 0);
        batch.draw(pt, 0, 0, width, height);
        batch.end();
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");
    }

    @Override
    public void resize(int width, int height) {
//        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Animated World Map Writer");
        config.setWindowedMode(width * cellWidth, height * cellHeight);
        config.setResizable(false);
        config.useVsync(true);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new AnimatedWorldMapWriter(), config);
    }
}
