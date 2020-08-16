package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.PaletteReducer;
import squidpony.squidmath.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class FastNoiseVisualizer extends ApplicationAdapter {

    private FastNoise noise = new FastNoise(1, 0.0625f, FastNoise.SIMPLEX_FRACTAL, 1);
    private int dim; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 3;
    private float freq = 0.125f;
    private boolean inverse;
    private ImmediateModeRenderer20 renderer;
    
    private int hashIndex = 2;
    private PointHash ph = new PointHash();
    private HastyPointHash hph = new HastyPointHash();
    private IntPointHash iph = new IntPointHash();
    private GoldPointHash gold = new GoldPointHash();
    private FlawedPointHash.RugHash rug = new FlawedPointHash.RugHash(1);
    private FlawedPointHash.QuiltHash quilt = new FlawedPointHash.QuiltHash(1, 16);
    private FlawedPointHash.CubeHash cube = new FlawedPointHash.CubeHash(1, 32);
    private FlawedPointHash.FNVHash fnv = new FlawedPointHash.FNVHash(1);
    private IPointHash[] pointHashes = new IPointHash[] {ph, hph, iph, gold, rug, quilt, cube};

    private static final int width = 512, height = 512;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;
    
    private AnimatedGif gif;
    private Array<Pixmap> frames = new Array<>(64);

    public static float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        noise.setPointHash(pointHashes[hashIndex]);
        noise.setFractalType(FastNoise.FBM);
        gif = new AnimatedGif();
        gif.palette = new PaletteReducer(new int[] {
                0x00000000, 0x000000FF, 0x081820FF, 0x132C2DFF, 0x1E403BFF, 0x295447FF, 0x346856FF, 0x497E5BFF,
                0x5E9463FF, 0x73AA69FF, 0x88C070FF, 0x9ECE88FF, 0xB4DCA0FF, 0xCAEAB8FF, 0xE0F8D0FF, 0xEFFBE7FF,
                0xFFFFFFFF,

//                0x000000ff, 0x000033ff, 0x000066ff, 0x000099ff, 0x0000ccff, 0x0000ffff, 0x003300ff,
//                0x003333ff, 0x003366ff, 0x003399ff, 0x0033ccff, 0x0033ffff, 0x006600ff, 0x006633ff, 0x006666ff,
//                0x006699ff, 0x0066ccff, 0x0066ffff, 0x009900ff, 0x009933ff, 0x009966ff, 0x009999ff, 0x0099ccff,
//                0x0099ffff, 0x00cc00ff, 0x00cc33ff, 0x00cc66ff, 0x00cc99ff, 0x00ccccff, 0x00ccffff, 0x00ff00ff,
//                0x00ff33ff, 0x00ff66ff, 0x00ff99ff, 0x00ffccff, 0x00ffffff, 0x330000ff, 0x330033ff, 0x330066ff,
//                0x330099ff, 0x3300ccff, 0x3300ffff, 0x333300ff, 0x333333ff, 0x333366ff, 0x333399ff, 0x3333ccff,
//                0x3333ffff, 0x336600ff, 0x336633ff, 0x336666ff, 0x336699ff, 0x3366ccff, 0x3366ffff, 0x339900ff,
//                0x339933ff, 0x339966ff, 0x339999ff, 0x3399ccff, 0x3399ffff, 0x33cc00ff, 0x33cc33ff, 0x33cc66ff,
//                0x33cc99ff, 0x33ccccff, 0x33ccffff, 0x33ff00ff, 0x33ff33ff, 0x33ff66ff, 0x33ff99ff, 0x33ffccff,
//                0x33ffffff, 0x660000ff, 0x660033ff, 0x660066ff, 0x660099ff, 0x6600ccff, 0x6600ffff, 0x663300ff,
//                0x663333ff, 0x663366ff, 0x663399ff, 0x6633ccff, 0x6633ffff, 0x666600ff, 0x666633ff, 0x666666ff,
//                0x666699ff, 0x6666ccff, 0x6666ffff, 0x669900ff, 0x669933ff, 0x669966ff, 0x669999ff, 0x6699ccff,
//                0x6699ffff, 0x66cc00ff, 0x66cc33ff, 0x66cc66ff, 0x66cc99ff, 0x66ccccff, 0x66ccffff, 0x66ff00ff,
//                0x66ff33ff, 0x66ff66ff, 0x66ff99ff, 0x66ffccff, 0x66ffffff, 0x990000ff, 0x990033ff, 0x990066ff,
//                0x990099ff, 0x9900ccff, 0x9900ffff, 0x993300ff, 0x993333ff, 0x993366ff, 0x993399ff, 0x9933ccff,
//                0x9933ffff, 0x996600ff, 0x996633ff, 0x996666ff, 0x996699ff, 0x9966ccff, 0x9966ffff, 0x999900ff,
//                0x999933ff, 0x999966ff, 0x999999ff, 0x9999ccff, 0x9999ffff, 0x99cc00ff, 0x99cc33ff, 0x99cc66ff,
//                0x99cc99ff, 0x99ccccff, 0x99ccffff, 0x99ff00ff, 0x99ff33ff, 0x99ff66ff, 0x99ff99ff, 0x99ffccff,
//                0x99ffffff, 0xcc0000ff, 0xcc0033ff, 0xcc0066ff, 0xcc0099ff, 0xcc00ccff, 0xcc00ffff, 0xcc3300ff,
//                0xcc3333ff, 0xcc3366ff, 0xcc3399ff, 0xcc33ccff, 0xcc33ffff, 0xcc6600ff, 0xcc6633ff, 0xcc6666ff,
//                0xcc6699ff, 0xcc66ccff, 0xcc66ffff, 0xcc9900ff, 0xcc9933ff, 0xcc9966ff, 0xcc9999ff, 0xcc99ccff,
//                0xcc99ffff, 0xcccc00ff, 0xcccc33ff, 0xcccc66ff, 0xcccc99ff, 0xccccccff, 0xccccffff, 0xccff00ff,
//                0xccff33ff, 0xccff66ff, 0xccff99ff, 0xccffccff, 0xccffffff, 0xff0000ff, 0xff0033ff, 0xff0066ff,
//                0xff0099ff, 0xff00ccff, 0xff00ffff, 0xff3300ff, 0xff3333ff, 0xff3366ff, 0xff3399ff, 0xff33ccff,
//                0xff33ffff, 0xff6600ff, 0xff6633ff, 0xff6666ff, 0xff6699ff, 0xff66ccff, 0xff66ffff, 0xff9900ff,
//                0xff9933ff, 0xff9966ff, 0xff9999ff, 0xff99ccff, 0xff99ffff, 0xffcc00ff, 0xffcc33ff, 0xffcc66ff,
//                0xffcc99ff, 0xffccccff, 0xffccffff, 0xffff00ff, 0xffff33ff, 0xffff66ff, 0xffff99ff, 0xffffccff,
//                0xffffffff, 
        });

        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case ENTER:
                        frames.clear();
                        for (int c = 0; c < 64; c++) {
                            Pixmap p = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < 128; x++) {
                                for (int y = 0; y < 128; y++) {
                                    float color = basicPrepare(noise.getConfiguredNoise(x, y, c));
                                    color *= color * 0.8125f;
                                    p.setColor(color, color, color, 1f);
                                    p.drawPixel(x, y);
                                }
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();
                        gif.write(Gdx.files.local("out/green.gif"), frames, 12);
                        
                        break;
                    case P: //pause
                        keepGoing = !keepGoing;
                    case C:
                        ctr++;
                        break;
                    case E: //earlier seed
                        noise.setSeed(noise.getSeed() - 1);
                        break;
                    case S: //seed
                        noise.setSeed(noise.getSeed() + 1);
                        break;
                    case N: // noise type
                    case EQUALS:
                        noise.setNoiseType((noise.getNoiseType() + 1) % 12);
                        break;
                    case MINUS:
                        noise.setNoiseType((noise.getNoiseType() + 11) % 12);
                        break;
                    case D: //dimension
                        dim = (dim + 1) % 5;
                        break;
                    case F: // frequency
//                        noise.setFrequency(NumberTools.sin(freq += 0.125f) * 0.25f + 0.25f + 0x1p-7f);
                        noise.setFrequency((float) Math.pow(2f, (System.currentTimeMillis() >>> 9 & 7) - 5));
                        break;
                    case R: // fRactal type
                        noise.setFractalType((noise.getFractalType() + 1) % 3);
                        break;
                    case G: // GLITCH!
                        noise.setPointHash(pointHashes[hashIndex = (hashIndex + 1) % pointHashes.length]);
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        break;
                    case I: // inverse mode
                        if (inverse = !inverse) {
                            noise.setFractalLacunarity(0.5f);
                            noise.setFractalGain(2f);
                        } else {
                            noise.setFractalLacunarity(2f);
                            noise.setFractalGain(0.5f);
                        }
                        break;
                    case K: // sKip
                        ctr += 1000;
                        break;
                    case Q:
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x + ctr, y + ctr));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x, y, ctr));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x, y, ctr, 0x1p-4f * (x + y - ctr)));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 3: {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(
                                (y * 0.6f + x * 0.4f) + ctr * 1.3f, (x * 0.6f - y * 0.4f) + ctr * 1.3f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.3f, (y * 0.7f - x * 0.3f) - ctr * 1.3f,
                                (x * 0.35f - y * 0.25f) * 0.65f - (x * 0.25f + y * 0.35f) * 1.35f));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
            case 4: {
                for (int x = 0; x < width; x++) { 
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(
                                (y * 0.6f + x * 0.4f) + ctr * 1.3f, (x * 0.6f - y * 0.4f) + ctr * 1.3f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.3f, (y * 0.7f - x * 0.3f) - ctr * 1.3f,
                                (x * 0.35f - y * 0.25f) * 0.65f - (x * 0.25f + y * 0.35f) * 1.35f,
                                (y * 0.45f - x * 0.15f) * 0.75f - (y * 0.15f + x * 0.45f) * 1.25f));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
        }
        renderer.end();

    }

    @Override
    public void render() {
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            // standard clear the background routine for libGDX
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            ctr++;
            putMap();
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
        config.title = "SquidLib Test: Hash Visualization";
        config.width = width;
        config.height = height;
        config.foregroundFPS = 0;
        config.vSyncEnabled = false;
        config.resizable = false;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new FastNoiseVisualizer(), config);
    }
}
