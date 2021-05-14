package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;
import squidpony.squidmath.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class FastNoiseVisualizer extends ApplicationAdapter {

    private FastNoise noise = new FastNoise(1, 0.0625f, FastNoise.CUBIC_FRACTAL, 1);
    private int dim = 1; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 2;
    private float freq = 1f;
    private boolean inverse;
    private ImmediateModeRenderer20 renderer;
    
    private PointHash ph = new PointHash();
    private HastyPointHash hph = new HastyPointHash();
    private IntPointHash iph = new IntPointHash();
    private GoldPointHash gold = new GoldPointHash();
//    private PermPointHash pph = new PermPointHash();
    private FlawedPointHash.RugHash rug = new FlawedPointHash.RugHash(1);
    private FlawedPointHash.QuiltHash quilt = new FlawedPointHash.QuiltHash(1, 16);
    private FlawedPointHash.CubeHash cube = new FlawedPointHash.CubeHash(1, 32);
    private FlawedPointHash.FNVHash fnv = new FlawedPointHash.FNVHash(1);
    private IPointHash[] pointHashes = new IPointHash[] {ph, hph, iph, fnv, gold, rug, quilt, cube};
    private int hashIndex = 7;

    private static final int width = 512, height = 512;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;
    
    private AnimatedGif gif;
    private Array<Pixmap> frames = new Array<>(128);

    public static float basicPrepare(float n)
    {
//        return Math.max(0f, n);
        return n * 0.5f + 0.5f;
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        noise.setPointHash(pointHashes[hashIndex]);
        noise.setFractalType(FastNoise.RIDGED_MULTI);
        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.NONE);
        gif.palette = new PaletteReducer(new int[] {
                0x00000000, 0x010101FF, 0x020202FF, 0x030303FF, 0x040404FF, 0x050505FF, 0x060606FF, 0x070707FF,
                0x080808FF, 0x090909FF, 0x0A0A0AFF, 0x0B0B0BFF, 0x0C0C0CFF, 0x0D0D0DFF, 0x0E0E0EFF, 0x0F0F0FFF,
                0x101010FF, 0x111111FF, 0x121212FF, 0x131313FF, 0x141414FF, 0x151515FF, 0x161616FF, 0x171717FF,
                0x181818FF, 0x191919FF, 0x1A1A1AFF, 0x1B1B1BFF, 0x1C1C1CFF, 0x1D1D1DFF, 0x1E1E1EFF, 0x1F1F1FFF,
                0x202020FF, 0x212121FF, 0x222222FF, 0x232323FF, 0x242424FF, 0x252525FF, 0x262626FF, 0x272727FF,
                0x282828FF, 0x292929FF, 0x2A2A2AFF, 0x2B2B2BFF, 0x2C2C2CFF, 0x2D2D2DFF, 0x2E2E2EFF, 0x2F2F2FFF,
                0x303030FF, 0x313131FF, 0x323232FF, 0x333333FF, 0x343434FF, 0x353535FF, 0x363636FF, 0x373737FF,
                0x383838FF, 0x393939FF, 0x3A3A3AFF, 0x3B3B3BFF, 0x3C3C3CFF, 0x3D3D3DFF, 0x3E3E3EFF, 0x3F3F3FFF,
                0x404040FF, 0x414141FF, 0x424242FF, 0x434343FF, 0x444444FF, 0x454545FF, 0x464646FF, 0x474747FF,
                0x484848FF, 0x494949FF, 0x4A4A4AFF, 0x4B4B4BFF, 0x4C4C4CFF, 0x4D4D4DFF, 0x4E4E4EFF, 0x4F4F4FFF,
                0x505050FF, 0x515151FF, 0x525252FF, 0x535353FF, 0x545454FF, 0x555555FF, 0x565656FF, 0x575757FF,
                0x585858FF, 0x595959FF, 0x5A5A5AFF, 0x5B5B5BFF, 0x5C5C5CFF, 0x5D5D5DFF, 0x5E5E5EFF, 0x5F5F5FFF,
                0x606060FF, 0x616161FF, 0x626262FF, 0x636363FF, 0x646464FF, 0x656565FF, 0x666666FF, 0x676767FF,
                0x686868FF, 0x696969FF, 0x6A6A6AFF, 0x6B6B6BFF, 0x6C6C6CFF, 0x6D6D6DFF, 0x6E6E6EFF, 0x6F6F6FFF,
                0x707070FF, 0x717171FF, 0x727272FF, 0x737373FF, 0x747474FF, 0x757575FF, 0x767676FF, 0x777777FF,
                0x787878FF, 0x797979FF, 0x7A7A7AFF, 0x7B7B7BFF, 0x7C7C7CFF, 0x7D7D7DFF, 0x7E7E7EFF, 0x7F7F7FFF,
                0x808080FF, 0x818181FF, 0x828282FF, 0x838383FF, 0x848484FF, 0x858585FF, 0x868686FF, 0x878787FF,
                0x888888FF, 0x898989FF, 0x8A8A8AFF, 0x8B8B8BFF, 0x8C8C8CFF, 0x8D8D8DFF, 0x8E8E8EFF, 0x8F8F8FFF,
                0x909090FF, 0x919191FF, 0x929292FF, 0x939393FF, 0x949494FF, 0x959595FF, 0x969696FF, 0x979797FF,
                0x989898FF, 0x999999FF, 0x9A9A9AFF, 0x9B9B9BFF, 0x9C9C9CFF, 0x9D9D9DFF, 0x9E9E9EFF, 0x9F9F9FFF,
                0xA0A0A0FF, 0xA1A1A1FF, 0xA2A2A2FF, 0xA3A3A3FF, 0xA4A4A4FF, 0xA5A5A5FF, 0xA6A6A6FF, 0xA7A7A7FF,
                0xA8A8A8FF, 0xA9A9A9FF, 0xAAAAAAFF, 0xABABABFF, 0xACACACFF, 0xADADADFF, 0xAEAEAEFF, 0xAFAFAFFF,
                0xB0B0B0FF, 0xB1B1B1FF, 0xB2B2B2FF, 0xB3B3B3FF, 0xB4B4B4FF, 0xB5B5B5FF, 0xB6B6B6FF, 0xB7B7B7FF,
                0xB8B8B8FF, 0xB9B9B9FF, 0xBABABAFF, 0xBBBBBBFF, 0xBCBCBCFF, 0xBDBDBDFF, 0xBEBEBEFF, 0xBFBFBFFF,
                0xC0C0C0FF, 0xC1C1C1FF, 0xC2C2C2FF, 0xC3C3C3FF, 0xC4C4C4FF, 0xC5C5C5FF, 0xC6C6C6FF, 0xC7C7C7FF,
                0xC8C8C8FF, 0xC9C9C9FF, 0xCACACAFF, 0xCBCBCBFF, 0xCCCCCCFF, 0xCDCDCDFF, 0xCECECEFF, 0xCFCFCFFF,
                0xD0D0D0FF, 0xD1D1D1FF, 0xD2D2D2FF, 0xD3D3D3FF, 0xD4D4D4FF, 0xD5D5D5FF, 0xD6D6D6FF, 0xD7D7D7FF,
                0xD8D8D8FF, 0xD9D9D9FF, 0xDADADAFF, 0xDBDBDBFF, 0xDCDCDCFF, 0xDDDDDDFF, 0xDEDEDEFF, 0xDFDFDFFF,
                0xE0E0E0FF, 0xE1E1E1FF, 0xE2E2E2FF, 0xE3E3E3FF, 0xE4E4E4FF, 0xE5E5E5FF, 0xE6E6E6FF, 0xE7E7E7FF,
                0xE8E8E8FF, 0xE9E9E9FF, 0xEAEAEAFF, 0xEBEBEBFF, 0xECECECFF, 0xEDEDEDFF, 0xEEEEEEFF, 0xEFEFEFFF,
                0xF0F0F0FF, 0xF1F1F1FF, 0xF2F2F2FF, 0xF3F3F3FF, 0xF4F4F4FF, 0xF5F5F5FF, 0xF6F6F6FF, 0xF7F7F7FF,
                0xF8F8F8FF, 0xF9F9F9FF, 0xFAFAFAFF, 0xFBFBFBFF, 0xFCFCFCFF, 0xFDFDFDFF, 0xFEFEFEFF, 0xFFFFFFFF,

//                0x00000000, 0x000000FF, 0x081820FF, 0x132C2DFF, 0x1E403BFF, 0x295447FF, 0x346856FF, 0x497E5BFF,
//                0x5E9463FF, 0x73AA69FF, 0x88C070FF, 0x9ECE88FF, 0xB4DCA0FF, 0xCAEAB8FF, 0xE0F8D0FF, 0xEFFBE7FF,
//                0xFFFFFFFF,

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
                    case W:
                        frames.clear();
                        noise.setFrequency(0x1p-3f);
                        for (int c = 0; c < 256; c++) {
                            Pixmap p = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < 256; x++) {
                                for (int y = 0; y < 256; y++) {
                                    float color = basicPrepare(noise.getConfiguredNoise(x, y, c));
//                                    color *= color * 0.8125f;
                                    p.setColor(color, color, color, 1f);
                                    p.drawPixel(x, y);
                                }
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();
                        gif.write(Gdx.files.local("out/cube.gif"), frames, 12);
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
                case ENTER:
                    noise.setNoiseType((noise.getNoiseType() + 1) % 14);
                    break;
                case M:
                case MINUS:
                    noise.setNoiseType((noise.getNoiseType() + 13) % 14);
                    break;
                case D: //dimension
                    dim = (dim + 1) % 5;
                    break;
                case F: // frequency
//                        noise.setFrequency(NumberTools.sin(freq += 0.125f) * 0.25f + 0.25f + 0x1p-7f);
                    noise.setFrequency((float) Math.exp((System.currentTimeMillis() >>> 9 & 7) - 5));
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
        float bright, c = ctr * 0.5f;
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
                        bright = basicPrepare(noise.getConfiguredNoise(x, y, c));
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
                                (y * 0.6f + x * 0.4f) + ctr * 1.4f,
                                (x * 0.6f - y * 0.4f) + ctr * 1.4f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.4f,
                                (y * 0.7f - x * 0.3f) - ctr * 1.4f,
                                (x * 0.35f - y * 0.25f) * 0.6f - (x * 0.25f - y * 0.35f) * 1.2f));
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
                                (y * 0.6f + x * 0.4f) + ctr * 1.2f, (x * 0.6f - y * 0.4f) + ctr * 1.2f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.2f, (y * 0.7f - x * 0.3f) - ctr * 1.2f,
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
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Test: Hash Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new FastNoiseVisualizer(), config);
    }
}
