package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.gui.gdx.PNG8;
import squidpony.squidgrid.gui.gdx.PaletteReducer;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;

import java.io.IOException;
import java.nio.ByteBuffer;

import static squidpony.squidgrid.gui.gdx.SColor.blueLUT;
import static squidpony.squidgrid.gui.gdx.SColor.greenLUT;
import static squidpony.squidgrid.gui.gdx.SColor.redLUT;

/**
 * Demo for various ways to quantize images to fit in fewer bits per pixel.
 * Uses a public-domain image of the painting "Étang en Ile de France", by Henri Biva, obtained from Wikimedia Commons:
 * <a href="https://commons.wikimedia.org/wiki/Category:Henri_Biva#/media/File:Henri_Biva,_Étang_en_Ile_de_France,_oil_on_canvas,_54_x_65_cm.jpg">See here</a>,
 * and a color-remastered version of the famous (also public domain) "Mona Lisa", by Leonardo da Vinci:
 * <a href="https://commons.wikimedia.org/wiki/File:Leonardo_da_Vinci_-_Mona_Lisa_(Louvre,_Paris)FXD.tif">See here</a>.
 * Shows the original, a way of quantizing to 8 bits with 3 different lookup tables (LUTs) for the RGB channels, to 15
 * bits by using only the 5 most significant bits of each channel, to 9 bits by using only the 3 most significant bits
 * of each channel, and a way of quantizing to under 8 bits by reducing the possible values per channel from 256 to 6.
 * Shows all of the Biva variations before showing the da Vinci variations.
 */
public class QuantizeDemo extends ApplicationAdapter {
    private static int width = 574, width1 = width, width2 = 345, height = 512, height1 = 480, height2 = 512;
    private SpriteBatch batch;
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    private Viewport view;
    private int mode = 0, maxModes = 10;
    private Pixmap edit, bivaOriginal, monaOriginal;
    private ByteBuffer pixels;
    private Texture pt;
    private PNG8 png8;
    private PixmapIO.PNG png;
    private PaletteReducer auroraPalette, monaPalette, bivaPalette;
    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new StretchViewport(width, height);
        bivaOriginal = new Pixmap(Gdx.files.internal("special/Painting_by_Henri_Biva.jpg"));
        bivaOriginal.setBlending(Pixmap.Blending.None);
        monaOriginal = new Pixmap(Gdx.files.internal("special/Mona_Lisa.jpg"));
        monaOriginal.setBlending(Pixmap.Blending.None);
        edit = new Pixmap(width << 1, height << 1, Pixmap.Format.RGBA8888);
        edit.setBlending(Pixmap.Blending.None);
        edit.setFilter(Pixmap.Filter.NearestNeighbour);
        edit.drawPixmap(bivaOriginal, 0, 0,  width1, height1, 0, height - height1, width1 << 1, height1 << 1);
        pixels = edit.getPixels();
        pt = new Texture(edit);
        pt.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        png8 = new PNG8((int)(monaOriginal.getWidth() * monaOriginal.getHeight() * 1.5f));
        png = new PixmapIO.PNG((int)(monaOriginal.getWidth() * monaOriginal.getHeight() * 1.5f));
        png8.setFlipY(false);
        png.setFlipY(false);
        png8.setCompression(6);
        png.setCompression(6);
        auroraPalette = new PaletteReducer();
        png8.palette = auroraPalette;
        try {
            png8.write(Gdx.files.local("out/Painting_by_Henri_Biva_PNG8_Aurora.png"), bivaOriginal, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Painting_by_Henri_Biva_PNG8_Aurora.png", ex);
        }
        try {
            png8.write(Gdx.files.local("out/Mona_Lisa_PNG8_Aurora.png"), monaOriginal, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Mona_Lisa_PNG8_Aurora.png", ex);
        }
        // use computed palette
        try {
            png.write(Gdx.files.local("out/Painting_by_Henri_Biva_PNG32.png"), bivaOriginal);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Painting_by_Henri_Biva_PNG32.png", ex);
        }
        try {
            png.write(Gdx.files.local("out/Mona_Lisa_PNG32.png"), monaOriginal);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Mona_Lisa_PNG32.png", ex);
        }
        bivaPalette = new PaletteReducer(bivaOriginal);
        monaPalette = new PaletteReducer(monaOriginal);
        png8.palette = bivaPalette;
        try {
            png8.write(Gdx.files.local("out/Painting_by_Henri_Biva_PNG8.png"), bivaOriginal, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Painting_by_Henri_Biva_PNG8.png", ex);
        }
        png8.palette = monaPalette;
        try {
            png8.write(Gdx.files.local("out/Mona_Lisa_PNG8.png"), monaOriginal, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Mona_Lisa_PNG8.png", ex);
        }
        try {
            png8.write(Gdx.files.local("out/Painting_by_Henri_Biva_PNG8_Mona.png"), bivaOriginal, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Painting_by_Henri_Biva_PNG8_Mona.png", ex);
        }
        png8.palette = bivaPalette;
        try {
            png8.write(Gdx.files.local("out/Mona_Lisa_PNG8_Biva.png"), monaOriginal, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: out/Mona_Lisa_PNG8_Biva.png", ex);
        }

//        pt.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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
        int color, pos;
        edit.setColor(0x222222FF);
        edit.fill();
        switch (mode)
        {
            case 0:
                Gdx.graphics.setTitle("(Original) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0,  width1, height1, 0, height - height1, width1 << 1, height1 << 1);
                break;
            case 1:
                Gdx.graphics.setTitle("(64-value channels) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0,  width1, height1, 0, height - height1, width1 << 1, height1 << 1);
                while (pixels.remaining() >= 4)
                {
                    color = (pixels.getInt() & 0xFCFCFCFF) | 0xFF;
                    color |= color >>> 6 & 0x03030300;
                    pixels.putInt(pixels.position() - 4, color);
                }
                pixels.rewind();
                break;
            case 2:
                Gdx.graphics.setTitle("(DawnBringer Aurora)  Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0,  width1, height1, 0, height - height1, width1 << 1, height1 << 1);
//                while (pixels.remaining() >= 4)
//                {
//                    color = (pixels.getInt() & 0xF8F8F8FF) | 0xFF;
//                    color |= color >>> 5 & 0x07070700;
//                    pixels.putInt(pixels.position() - 4, color);
//                }
//                pixels.rewind();
                auroraPalette.reduce(edit);
                break;
            case 3:
                Gdx.graphics.setTitle("(Adaptive Palette) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0,  width1, height1, 0, height - height1, width1 << 1, height1 << 1);
//                while (pixels.remaining() >= 4) {
//                    pos = pixels.position();
//                    color = Integer.reverseBytes((redLUT[pixels.get() >>> 3 & 31] | greenLUT[pixels.get() >>> 3 & 31] | blueLUT[pixels.get() >>> 3 & 31]) & (pixels.get() >> 31));
//                    pixels.putInt(pos, color);
//                    pixels.position(pos+4);
//                }
//                pixels.rewind();
                bivaPalette.reduce(edit);
                break;
            case 4:
                Gdx.graphics.setTitle("(6-value channels) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0,  width1, height1, 0, height - height1, width1 << 1, height1 << 1);
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
            case 5:
                Gdx.graphics.setTitle("(Original) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2, 0);
                break;
            case 6:
                Gdx.graphics.setTitle("(64-value channels) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2, 0);
                while (pixels.remaining() >= 4)
                {
                    color = (pixels.getInt() & 0xFCFCFCFF) | 0xFF;
                    color |= color >>> 6 & 0x03030300;
                    pixels.putInt(pixels.position() - 4, color);
                }
                pixels.rewind();
                break;
            case 7:
                Gdx.graphics.setTitle("(DawnBringer Aurora) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2, 0);
//                while (pixels.remaining() >= 4)
//                {
//                    color = (pixels.getInt() & 0xF8F8F8FF) | 0xFF;
//                    color |= color >>> 5 & 0x07070700;
//                    pixels.putInt(pixels.position() - 4, color);
//                }
//                pixels.rewind();
                auroraPalette.reduce(edit);
                break;
            case 8:
                Gdx.graphics.setTitle("(Adaptive Palette) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2, 0);
//                while (pixels.remaining() >= 4) {
//                    pos = pixels.position();
//                    color = Integer.reverseBytes((redLUT[pixels.get() >>> 3 & 31] | greenLUT[pixels.get() >>> 3 & 31] | blueLUT[pixels.get() >>> 3 & 31]) & (pixels.get() >> 31));
//                    pixels.putInt(pos, color);
//                    pixels.position(pos+4);
//                }
//                pixels.rewind();
                monaPalette.reduce(edit);
                break;
            case 9:
                Gdx.graphics.setTitle("(6-value channels) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2, 0);
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
        }
        batch.begin();
        pt.draw(edit, 0, 0);
        batch.draw(pt, 0, 0, width, height);
        batch.end();
        Gdx.graphics.requestRendering();
    }
    
    private final float emphasize(final float a)
    {
        return a * a * (3f - 2f * a);
    }
    private final float extreme(final float a)
    {
        return a * a * a * (a * (a * 6f - 15f) + 10f);
    }
//    private static final int[]
//            redLUT =   {
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
//            0x4C000000, 0x4C000000, 0x4C000000, 0x4C000000, 0x4C000000, 0x4C000000, 0x4C000000, 0x7A000000,
//            0x7A000000, 0x7A000000, 0x7A000000, 0x7A000000, 0xB0000000, 0xB0000000, 0xB0000000, 0xB0000000,
//            0xDC000000, 0xDC000000, 0xDC000000, 0xDC000000, 0xFF000000, 0xFF000000, 0xFF000000, 0xFF000000,},
//            greenLUT = {
//            0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x380000,
//            0x380000, 0x380000, 0x380000, 0x380000, 0x380000, 0x600000, 0x600000, 0x600000,
//            0x600000, 0x600000, 0x980000, 0x980000, 0x980000, 0x980000, 0x980000, 0xC40000,
//            0xC40000, 0xC40000, 0xC40000, 0xE40000, 0xE40000, 0xE40000, 0xFF0000, 0xFF0000,},
//            blueLUT =  {
//            0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x3800, 0x3800,
//            0x3800, 0x3800, 0x3800, 0x3800, 0x3800, 0x6A00, 0x6A00, 0x6A00,
//            0x6A00, 0x6A00, 0x6A00, 0x6A00, 0xA000, 0xA000, 0xA000, 0xA000, 
//            0xA000, 0xD000, 0xD000, 0xD000, 0xD000, 0xFF00, 0xFF00, 0xFF00,};
    public int quantize(Color color)
    {
        // Full 8-bit RGBA channels. No limits on what colors can be displayed.         
        //return Color.rgba8888(color);

        // Limits red, green, and blue channels to only use 5 bits (32 values) instead of 8 (256 values).
        //return Color.rgba8888(color) & 0xF8F8F8FF;

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