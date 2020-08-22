package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.WildMap;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.MathExtras;
import squidpony.squidmath.SilkRNG;
import squidpony.squidmath.StatefulRNG;

public class WildMapDemo extends ApplicationAdapter {
    //private static final int bigWidth = 314 * 3, bigHeight = 300;
//    private static final int bigWidth = 256, bigHeight = 256;
//    private static final int bigWidth = 1024, bigHeight = 512;
    private static final int bigWidth = 128, bigHeight = 128;
//    private static final int bigWidth = 2048, bigHeight = 1024;
    //private static final int bigWidth = 400, bigHeight = 400;
    private static final int cellWidth = 8, cellHeight = 16;
    private static final int shownWidth = 96, shownHeight = 48;
    private FilterBatch batch;
    private SparseLayers display;//, overlay;
    private SquidInput input;
    private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private Vector3 position, previousPosition, nextPosition;
//    private WorldMapGenerator.MimicMap world;
    private WildMap.MixedWildMap wild;
    private WildMapView wmv;
    private long counter;
    private long ttg; // time to generate
    private float moveAmount;
    
    @Override
    public void create() {
        batch = new FilterBatch();
        TextCellFactory tcf = DefaultResources.getStretchableSlabFont();
        display = new SparseLayers(bigWidth, bigHeight, cellWidth, cellHeight, tcf);
        //display.font.tweakHeight(13f).tweakWidth(13f).initBySize();
        view = new StretchViewport(shownWidth * cellWidth, shownHeight * cellHeight);
        stage = new Stage(view, batch);
        seed = 1234567890L;
        rng = new StatefulRNG(seed);
//// you can use whatever map you have instead of fantasy_map.png, where white means land and black means water
//        Pixmap pix = new Pixmap(Gdx.files.internal("special/fantasy_map.png"));
//        final int bigWidth = pix.getWidth() / 4, bigHeight = pix.getHeight() / 4;
//        GreasedRegion basis = new GreasedRegion(bigWidth, bigHeight);
//        for (int x = 0; x < bigWidth; x++) {
//            for (int y = 0; y < bigHeight; y++) {
//                if(pix.getPixel(x * 4, y * 4) < 0) // only counts every fourth row and every fourth column
//                    basis.insert(x, y);
//            }
//        }
//        basis = WorldMapGenerator.MimicMap.reprojectToElliptical(basis);
//// at this point you could get the GreasedRegion as a String, and save the compressed output to a file:
//// Gdx.files.local("map.txt").writeString(LZSPlus.compress(basis.serializeToString()), false, "UTF16");
//// you could reload basis without needing the original map image with
//// basis = GreasedRegion.deserializeFromString(LZSPlus.decompress(Gdx.files.local("map.txt").readString("UTF16")));
//// it's also possible to store the compressed map as a String in code, but you need to be careful about escaped chars.
//        world = new WorldMapGenerator.LocalMimicMap(seed, basis, FastNoise.instance, 0.8);
//        pix.dispose();

//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 0.8); // uses a map of Australia for land
        
        //wild = new WildMap(128, 128, 21, new SilkRNG("SquidLib!"));
        final SilkRNG wrng = new SilkRNG("SquidLib!");
        //21, 27, 26, 20
        wild = new WildMap.MixedWildMap(new WildMap(128, 128, 21, wrng), new WildMap(128, 128, 27, wrng),
                new WildMap(128, 128, 28, wrng), new WildMap(128, 128, 22, wrng), wrng);
//        wild = new WildMap(128, 128, 21, new SilkRNG("SquidLib!"), WildMap.makeVegetation(rng, 70, 0.2, FakeLanguageGen.SIMPLISH));
//        HashMap<String, ICellVisible> viewer = new HashMap<>(3 + wild.contentTypes.size(), 0.25f);
//        viewer.put("dirt", new Basic('.', CLOVE_BROWN));
//        viewer.put("leaves", new Basic('…', CHINESE_TEA_YELLOW));
//        viewer.put("grass", new Basic('"', AURORA_DUSTY_GREEN.toEditedFloat(0f, 0.1f, -0.25f)));
//        for (int i = 0; i < wild.contentTypes.size(); i++) {
//            viewer.put(wild.contentTypes.get(i), new Basic('¥', AURORA_AVOCADO.toEditedFloat(wild.rng.nextFloat(0.35f) - 0.25f, wild.rng.nextFloat(0.4f) - 0.2f, wild.rng.nextFloat(0.3f) - 0.45f)));
//        }
        wmv = new WildMapView(wild);
        //world = new WorldMapGenerator.TilingMap(seed, bigWidth, bigHeight, WhirlingNoise.instance, 0.9);
        //wild = wmv.wildMap;
        position = new Vector3(bigWidth * cellWidth * 0.5f, bigHeight * cellHeight * 0.5f, 0);
        previousPosition = position.cpy();
        nextPosition = position.cpy();
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        seed = rng.nextLong();
                        wmv.viewer = WildMapView.defaultViewer();
                        wild.biome = rng.nextSignedInt(42);
                        int wx = rng.nextSignedInt(6), wy = rng.nextSignedInt(7);
                        wmv.wildMap = wild = new WildMap.MixedWildMap(
                                new WildMap(wild.width, wild.height, wx + wy * 6, wrng),
                                new WildMap(wild.width, wild.height, wx + MathExtras.clamp(wy + rng.next(1), 0, 5) * 6, wrng),
                                new WildMap(wild.width, wild.height, wx + MathExtras.clamp(wy + rng.next(1), 0, 5) * 6, wrng),
                                new WildMap(wild.width, wild.height, MathExtras.clamp(wx + rng.next(1), 0, 6) + wy * 6, wrng),
                                wrng);
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

//    public void zoomIn() {
//        zoomIn(bigWidth >> 1, bigHeight >> 1);
//    }
//    public void zoomIn(int zoomX, int zoomY)
//    {
//        long startTime = System.currentTimeMillis();
//        world.zoomIn(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
//        //counter = 0L;
//        ttg = System.currentTimeMillis() - startTime;
//    }
//    public void zoomOut()
//    {
//        zoomOut(bigWidth >>1, bigHeight >>1);
//    }
//    public void zoomOut(int zoomX, int zoomY)
//    {
//        long startTime = System.currentTimeMillis();
//        world.zoomOut(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
//        //counter = 0L;
//        ttg = System.currentTimeMillis() - startTime;
//    }
    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        System.out.println("Seed used: 0x" + StringKit.hex(seed) + "L");
        wild.rng.setState(seed);
        wmv.generate();
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        wmv.show(display);
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
        Gdx.graphics.setTitle(WorldMapGenerator.DetailedBiomeMapper.biomeTable[wild.biome] + " Map! Took " + ttg + " ms to generate");

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
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Wild!");
        config.useVsync(false);
        config.setWindowedMode(shownWidth * cellWidth, shownHeight * cellHeight);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new WildMapDemo(), config);
    }
}
