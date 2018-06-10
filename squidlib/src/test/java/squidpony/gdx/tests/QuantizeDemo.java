package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;

import java.nio.ByteBuffer;

/**
 * Demo for various ways to quantize images to fit in fewer bits per pixel.
 * Uses a public-domain image of the painting "Étang en Ile de France", by Henri Biva, obtained from Wikimedia Commons:
 * <a href="https://commons.wikimedia.org/wiki/Category:Henri_Biva#/media/File:Henri_Biva,_Étang_en_Ile_de_France,_oil_on_canvas,_54_x_65_cm.jpg">See here</a>.
 * Shows the original, a way of quantizing to 8 bits with 3 different lookup tables (LUTs) for the RGB channels, a way
 * of quantizing to 15 bits by using only the 5 most significant bits of each channel, and a way of quantizing to 8 bits
 * by reducing the possible values per channel from 256 to 6.
 */
public class QuantizeDemo extends ApplicationAdapter {
    private static int width = 574, height = 480;
    private SpriteBatch batch;
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    private Viewport view;
    private int mode = 0, maxModes = 4;
    private Pixmap pm, original;
    private ByteBuffer pixels;
    private Texture pt;
    private Color tempColor = Color.WHITE.cpy();
    
    private long ttg = 0; // time to generate
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new StretchViewport(width*cellWidth, height*cellHeight);
        original = new Pixmap(Gdx.files.internal("special/Painting_by_Henri_Biva.jpg"));
        original.setBlending(Pixmap.Blending.None);
        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.drawPixmap(original, 0, 0);
        pixels = pm.getPixels();
        pt = new Texture(pm);
        pt.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                    default:
                        mode = (mode + 1) % maxModes;
                        generate();
                        break;
                }
            }
        });
        input.setRepeatGap(Long.MAX_VALUE);
        Gdx.input.setInputProcessor(input);
        Gdx.graphics.setContinuousRendering(false);
        generate();
    }

    public void generate()
    {
        long startTime = System.currentTimeMillis();
        int color, pos;
        pm.drawPixmap(original, 0, 0);
        switch (mode)
        {
            case 1:
                Gdx.graphics.setTitle("(Custom LUT) Étang en Ile de France by Henri Biva");
                while (pixels.remaining() >= 4) {
                    pos = pixels.position();
                    color = (redLUT[pixels.get() >>> 3 & 31] | greenLUT[pixels.get() >>> 3 & 31] | blueLUT[pixels.get() >>> 3 & 31] | 255) & (pixels.get() >> 31);
                    pixels.putInt(pos, color);
                    pixels.position(pos+4);
                }
                pixels.rewind();
                break;
            case 2:
                Gdx.graphics.setTitle("(32-value channels) Étang en Ile de France by Henri Biva");
                while (pixels.remaining() >= 4)
                {
                    color = (pixels.getInt() & 0xF8F8F8FF) | 0xFF;
                    pixels.putInt(pixels.position() - 4, color);
                }
                pixels.rewind();
                break;
            case 3:
                Gdx.graphics.setTitle("(6-value channels) Étang en Ile de France by Henri Biva");
                while (pixels.remaining() >= 4) {
                    pos = pixels.position();
                    color = (((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAAAA & 0xFF000000)
                            | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAA & 0xFF0000)
                            | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA & 0xFF00)
                            | 255) & (pixels.get() >> 31);
                    pixels.putInt(pos, color);
                    pixels.position(pos+4);
                }
                pixels.rewind();
                break;
                default:
                    Gdx.graphics.setTitle("(Original) Étang en Ile de France by Henri Biva");
                    break;
        }
        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0, width, height);
        batch.end();
        Gdx.graphics.requestRendering();
        ttg = System.currentTimeMillis() - startTime;
    }
    
    private final float emphasize(final float a)
    {
        return a * a * (3f - 2f * a);
    }
    private final float extreme(final float a)
    {
        return a * a * a * (a * (a * 6f - 15f) + 10f);
    }
    private static final int[]
            redLUT =   {
            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
            0x4C000000, 0x4C000000, 0x4C000000, 0x4C000000, 0x4C000000, 0x4C000000, 0x7A000000, 0x7A000000,
            0x7A000000, 0x7A000000, 0x7A000000, 0x7A000000, 0xB0000000, 0xB0000000, 0xB0000000, 0xB0000000,
            0xB0000000, 0xE2000000, 0xE2000000, 0xE2000000, 0xE2000000, 0xFF000000, 0xFF000000, 0xFF000000,},
            greenLUT = {
            0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000,
            0x300000, 0x300000, 0x300000, 0x300000, 0x300000, 0x720000, 0x720000, 0x720000,
            0x720000, 0x720000, 0x980000, 0x980000, 0x980000, 0x980000, 0x980000, 0xC40000,
            0xC40000, 0xC40000, 0xC40000, 0xE40000, 0xE40000, 0xE40000, 0xFF0000, 0xFF0000,},
            blueLUT =  {
            0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x3800,
            0x3800, 0x3800, 0x3800, 0x3800, 0x3800, 0x6A00, 0x6A00, 0x6A00,
            0x6A00, 0x6A00, 0xA000, 0xA000, 0xA000, 0xA000, 0xA000, 0xD600, 
            0xD600, 0xD600, 0xD600, 0xD600, 0xFF00, 0xFF00, 0xFF00, 0xFF00,};
    public int quantize(Color color)
    {
        // Full 8-bit RGBA channels. No limits on what colors can be displayed.         
        //return Color.rgba8888(color);

        // Limits red, green, and blue channels to only use 5 bits (32 values) instead of 8 (256 values).
        //return Color.rgba8888(color) & 0xFCFCFCFF;

        // 253 possible colors, including one all-zero transparent color. 6 possible red values (not bits), 7 possible
        // green values, 6 possible blue values, and the aforementioned fully-transparent black. White is 0xFFFFFFFF and
        // not some off-white value, but other than black (0x000000FF), grayscale values have non-zero saturation.
        // Could be made into a palette, and images that use this can be saved as GIF or in PNG-8 indexed mode.
        //return ((0xFF000000 & (int)(color.r*6) * 0x2AAAAAAA) | (0xFF0000 & (int)(color.g*7) * 0x249249) | (0xFF00 & (int)(color.b*6) * 0x2AAA) | 255) & -(int)(color.a + 0.5f);
        return (redLUT[(int)(color.r*31.999f)] | greenLUT[(int)(color.g*31.999f)] | blueLUT[(int)(color.b*31.999f)] | 255) & -(int)(color.a + 0.5f);
    }
    
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(SColor.DB_INK.r, SColor.DB_INK.g, SColor.DB_INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        generate();
        
        //Gdx.graphics.setTitle("Art! Took " + ttg + " ms to generate");

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        // stage has its own batch and must be explicitly told to draw().
//        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Color Quantization";
        config.width = width;
        config.height = height;
        config.foregroundFPS = 30;
        config.vSyncEnabled= true;
        //config.fullscreen = true;
        config.backgroundFPS = -1;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new QuantizeDemo(), config);
    }
}