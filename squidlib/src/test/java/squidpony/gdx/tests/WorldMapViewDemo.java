package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidMouse;
import squidpony.squidgrid.gui.gdx.WorldMapView;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.*;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * Biome, moisture, heat, and height maps can all be requested from the generated world.
 * You can press 's' or 'p' to play a spinning animation of the world turning.
 * <a href="https://i.imgur.com/z3rlN53.gifv">Preview GIF of a spinning planet.</a>
 */
public class WorldMapViewDemo extends ApplicationAdapter {

    //private static final int width = 314 * 3, height = 300;
//    private static final int width = 1024, height = 512;
//    private static final int width = 512, height = 256;
//    private static final int width = 256, height = 256;
    private static final int width = 400, height = 400; // fast rotations
//    private static final int width = 300, height = 300;
//    private static final int width = 1600, height = 800;
//    private static final int width = 900, height = 900;
//    private static final int width = 700, height = 700;
//    private static final int width = 512, height = 512;
//    private static final int width = 128, height = 128;
    
    private ImmediateModeRenderer20 batch;
    private SquidInput input;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private WorldMapGenerator world;
    private WorldMapView wmv;

    private int counter = 0;

    private boolean spinning = false;

    private long ttg = 0; // time to generate
    
//    public int noiseCalls = 0, pixels = 0;
    
    public Noise.Noise3D noise;
    
    @Override
    public void create() {
        batch = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new StretchViewport(width, height);
        seed = 0x44B94C6A93EF3D54L;//0xca576f8f22345368L;//0x9987a26d1e4d187dL;//0xDEBACL;
        rng = new StatefulRNG(seed);
        noise = new FastNoise(1337, 1.618f, FastNoise.PERLIN_FRACTAL, 2, 0.8f, 1.25f);
//        {
//            @Override
//            public float singleSimplex(int seed, float x, float y, float z) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y, z);
//            }
//
//            @Override
//            public float singleSimplex(int seed, float x, float y) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y);
//            }
//
//            @Override
//            public float singleSimplex(int seed, float x, float y, float z, float w) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y, z, w);
//            }
//
//            @Override
//            public float singleSimplex(int seed, float x, float y, float z, float w, float u, float v) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y, z, w, u, v);
//            }
//        };
//        world = new WorldMapGenerator.TilingMap(seed, width, height, new FastNoise(1337, 1f), 1.25);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, WhirlingNoise.instance, 0.875);
        //world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, ClassicNoise.instance, 0.75);
//        world = new WorldMapGenerator.MimicMap(seed, new FastNoise(1337, 1f), 0.7);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, ClassicNoise.instance, 0.7);
        world = new WorldMapGenerator.RotatingSpaceMap(seed, width, height, noise, 0.9);
        //world = new WorldMapGenerator.RoundSideMap(seed, width, height, ClassicNoise.instance, 0.8);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 1.2, 0.0625, 2.5);
//        world = new WorldMapGenerator.SphereMap(seed, width, height, new FastNoise(1337, 1f), 0.6);
//        world = new WorldMapGenerator.LocalMimicMap(seed, new FastNoise(1337, 1f), 0.6);
//        world = new WorldMapGenerator.LocalMimicMap(seed, ((WorldMapGenerator.LocalMimicMap) world).earth.not(), new FastNoise(1337, 1f), 0.9);
        
        wmv = new WorldMapView(world);
        wmv.initialize(SColor.CW_FADED_RED, SColor.AURORA_BRICK, SColor.DEEP_SCARLET, SColor.DARK_CORAL,
                SColor.LONG_SPRING, SColor.WATER_PERSIMMON, SColor.AURORA_HOT_SAUCE, SColor.PALE_CARMINE,
                SColor.AURORA_LIGHT_SKIN_3, SColor.AURORA_PINK_SKIN_2,
                SColor.AURORA_PUTTY, SColor.AURORA_PUTTY, SColor.ORANGUTAN, SColor.SILVERED_RED, null);
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
                    case 'P':
                    case 'p':
                    case 'S':
                    case 's':
                        spinning = !spinning;
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        }, new SquidMouse(1, 1, width, height, 0, 0, new InputAdapter()
        {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(button == Input.Buttons.RIGHT)
                {
                    zoomOut(screenX, screenY);
                }
                else
                {
                    zoomIn(screenX, screenY);
                }
                return true;
            }
        }));
        input.setRepeatGap(Long.MAX_VALUE);
        generate(seed);
        rng.setState(seed);
        Gdx.input.setInputProcessor(input);
    }

    public void zoomIn() {
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomIn();
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomIn(1, zoomX<<1, zoomY<<1);
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void zoomOut()
    {
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomOut();
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void zoomOut(int zoomX, int zoomY)
    {
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomOut(1, zoomX<<1, zoomY<<1);
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void generate(final long seed)
    {
        long startTime = System.nanoTime();
        System.out.println("Seed used: 0x" + StringKit.hex(seed) + "L");
//        noiseCalls = 0;
        wmv.generate((int)(seed & 0xFFFFFFFFL), (int) (seed >>> 32),
                2.9 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
//                0.9 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.35 + 0.9);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void rotate()
    {
        long startTime = System.nanoTime();
        world.setCenterLongitude((startTime & 0xFFFFFFFFFFFFL) * 0x1p-32);
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }


    public void putMap() { 
        float[][] cm = wmv.getColorMap();
        batch.begin(view.getCamera().combined, GL20.GL_POINTS);
//        pixels = 0;
        float c;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = cm[x][y];
//                if(c != WorldMapView.emptyColor)
//                    pixels++;
                batch.color(c);
                batch.vertex(x, y, 0f);
            }
        }
        batch.end();
//        if(Gdx.input.isKeyJustPressed(Input.Keys.D)) // debug
//            System.out.println((float) (noiseCalls) / pixels);
    }
    
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(SColor.DB_INK.r, SColor.DB_INK.g, SColor.DB_INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if(spinning) 
            rotate();
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        ++counter;
        Gdx.graphics.setTitle("Took " + ttg + " ms to generate");

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
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
        config.foregroundFPS = 60;
        config.backgroundFPS = -1;
        config.resizable = false;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new WorldMapViewDemo(), config);
    }
}