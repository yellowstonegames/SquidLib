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
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;

import static squidpony.squidgrid.gui.gdx.SColor.*;

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
    private static final int width = 574, width1 = width, width2 = 404, height = 600, height1 = 480;
    private SpriteBatch batch;
    private SquidInput input;
    private Viewport view;
    private int mode = 17, maxModes = 27;
    private Pixmap edit, bivaOriginal, monaOriginal, colors;
    private ByteBuffer pixels;
    private Texture pt;
    private PNG8 png8;
    private PixmapIO.PNG png;
    private PaletteReducer auroraPalette, monaPalette, bivaPalette, colorsPalette;
    /**
     * This palette was given along with the Unseven palette
     * <a href="https://www.deviantart.com/foguinhos/art/Unseven-Full-541514728">in this set of swatches</a>, but it's
     * unclear if Unseven made it, or if this palette was published in some other medium. It's a nice palette, with 8
     * levels of lightness ramp for 30 ramps with different hues. It seems meant for pixel art that includes human
     * characters, and doesn't lack for skin tones like Unseven does. It has a generally good selection of light brown
     * colors, and has been adjusted to add some dark brown colors, as well as vividly saturated purple. Many ramps also
     * become more purple as they go into darker shades.
     * <br>
     * This is organized so the colors from index 1 to index 232 inclusive are sorted by hue, from red to orange to
     * yellow to green to blue to purple, while still being organized in blocks of 8 colors at a time from bright to
     * dark. Some almost-grayscale blocks are jumbled in the middle, but they do have a hue and it is always at the
     * point where they are in the sort. A block of colors that are practically true grayscale are at indices 1-8,
     * inclusive.
     * <br>
     * This can be passed in to {@link PaletteReducer#PaletteReducer(int[])} to quantize to this palette.
     */
    public static final int[] RINSED = {
            0x00000000, 
            0xF8F9FAFF, 0xC4C3C5FF, 0x9C9C9DFF, 0x757676FF, 0x616262FF, 0x4C484AFF, 0x252626FF, 0x090304FF,
            0xD89789FF, 0xC4877AFF, 0xB47B76FF, 0xA36C72FF, 0x905861FF, 0x76454CFF, 0x5F3234FF, 0x452327FF,
            0xF9DCB8FF, 0xCEB29AFF, 0xB29891FF, 0x8F797FFF, 0x75636FFF, 0x554B67FF, 0x3E3552FF, 0x272340FF,
            0xEAA18DFF, 0xCF9180FF, 0xB87C6BFF, 0xA06A60FF, 0x905C59FF, 0x73474BFF, 0x52383EFF, 0x35242AFF,
            0xBEAE97FF, 0xB0968AFF, 0x89756EFF, 0x6E5A54FF, 0x4F413CFF, 0x413534FF, 0x2F2525FF, 0x1C1415FF,
            0xEED8A1FF, 0xE7B38CFF, 0xCC967FFF, 0xB6776DFF, 0x995A55FF, 0x803D49FF, 0x662139FF, 0x500328FF,
            0xFDFE9CFF, 0xFDD7AAFF, 0xE9BBA4FF, 0xC9A09DFF, 0xB7889AFF, 0x957088FF, 0x755B7BFF, 0x514265FF,
            0xFDF067FF, 0xFDBF60FF, 0xEF995AFF, 0xCC7148FF, 0xB65549FF, 0xA34547FF, 0x7D303FFF, 0x61242FFF,
            0xDDBBA4FF, 0xC0A68FFF, 0x9F8871FF, 0x7F6B5CFF, 0x6B5755FF, 0x5D464CFF, 0x482F3DFF, 0x30232DFF,
            0xFEF5E1FF, 0xE9DFD3FF, 0xCFC5BAFF, 0xBAAFABFF, 0xAAA291FF, 0x9A877BFF, 0x816F69FF, 0x615D56FF,
            0xFEF1A8FF, 0xE4CE85FF, 0xC9AD77FF, 0xB19169FF, 0x957859FF, 0x7B604CFF, 0x60463BFF, 0x472F2AFF,
            0xFEFC74FF, 0xE8D861FF, 0xCDAD53FF, 0xB2893EFF, 0x91672FFF, 0x7D4F21FF, 0x693C12FF, 0x562810FF,
            0xFDFCB7FF, 0xFCFA3CFF, 0xFAD725FF, 0xF5B325FF, 0xD7853CFF, 0xB25345FF, 0x8A2B2BFF, 0x67160AFF,
            0xCBD350FF, 0xB3B24BFF, 0x9A9E3AFF, 0x808B30FF, 0x647717FF, 0x4B6309FF, 0x305413FF, 0x272A07FF,
            0x8DC655FF, 0x7BA838FF, 0x6C8A37FF, 0x5D733AFF, 0x4F633CFF, 0x3F5244FF, 0x323D4AFF, 0x232A45FF,
            0xADD54BFF, 0x80B040FF, 0x599135FF, 0x35761AFF, 0x2A621FFF, 0x1E5220FF, 0x063824FF, 0x012B1DFF,
            0xE8FFEFFF, 0xA9DDC0FF, 0x95C89CFF, 0x91B48EFF, 0x759983FF, 0x627F72FF, 0x4C655CFF, 0x36514AFF,
            0x91E49DFF, 0x69C085FF, 0x4F8F62FF, 0x4A7855FF, 0x396044FF, 0x385240FF, 0x31413DFF, 0x233631FF,
            0x09EFD0FF, 0x07CCA2FF, 0x03AA83FF, 0x038D75FF, 0x04726DFF, 0x01585AFF, 0x05454EFF, 0x083142FF,
            0x97D6F9FF, 0x3EB0CAFF, 0x3C919FFF, 0x0A737CFF, 0x226171FF, 0x0B505FFF, 0x0D3948FF, 0x052935FF,
            0x91FCFCFF, 0x68DBFEFF, 0x5CB1D5FF, 0x4C8CAAFF, 0x406883FF, 0x2B4965FF, 0x29324DFF, 0x1C1E34FF,
            0x80D1FBFF, 0x62B2E7FF, 0x4D96DBFF, 0x267DB9FF, 0x195F97FF, 0x114776FF, 0x0B355AFF, 0x031D41FF,
            0xCEEEFDFF, 0xCDD7FEFF, 0xA1AED7FF, 0x898CAEFF, 0x7C7196FF, 0x5E597CFF, 0x404163FF, 0x26294CFF,
            0x8391C1FF, 0x7181CAFF, 0x5E71BEFF, 0x555FA2FF, 0x424C84FF, 0x323B6DFF, 0x2B325CFF, 0x292349FF,
            0xE3D1FDFF, 0xBAABFAFF, 0x9F94E2FF, 0x9588D7FF, 0x7B71B3FF, 0x675E9CFF, 0x4F4D7CFF, 0x333158FF,
            0xA570FFFF, 0x9462FFFF, 0x814EFFFF, 0x6C39FCFF, 0x582DC1FF, 0x472195FF, 0x412160FF, 0x2E1F38FF,
            0xF7C1E7FF, 0xD791C6FF, 0xBB6FAAFF, 0xAF6190FF, 0x924B76FF, 0x623155FF, 0x47253FFF, 0x2F0E25FF,
            0xFDC7FBFF, 0xFC9FC5FF, 0xFB71A9FF, 0xE6497EFF, 0xC33C6BFF, 0x933255FF, 0x68243FFF, 0x3F122AFF,
            0xFDDDDCFF, 0xD1ABB1FF, 0xB48C9AFF, 0x9D7482FF, 0x8B5D6EFF, 0x705057FF, 0x583C4BFF, 0x421E29FF,
            0xFCD9FBFF, 0xFDB8C7FF, 0xFD97AAFF, 0xF46E7EFF, 0xC65365FF, 0x9E303CFF, 0x741B28FF, 0x50071AFF,
    };

    @Override
    public void create() {
        batch = new SpriteBatch();
        view = new StretchViewport(width, height);
        bivaOriginal = new Pixmap(Gdx.files.internal("special/Painting_by_Henri_Biva.jpg"));
        bivaOriginal.setBlending(Pixmap.Blending.None);
        monaOriginal = new Pixmap(Gdx.files.internal("special/Mona_Lisa_404x600.jpg"));
        monaOriginal.setBlending(Pixmap.Blending.None);
        colors = new Pixmap(512, 512, Pixmap.Format.RGBA8888);
//        SColor[] full = FULL_PALETTE;
        OrderedSet<? extends Color> full = new OrderedSet<>(FULL_PALETTE);
        full.sort(new Comparator<Color>() {
            @Override
            public int compare(Color c1, Color c2) {
                float diff = SColor.hue(c1) - SColor.hue(c2);
                if(diff != 0)
                    return NumberTools.floatToIntBits(diff);
                return NumberTools.floatToIntBits(SColor.luma(c1) - SColor.luma(c2));
            }
        });
        //int idx = 0;
        int len = full.size();
        CoordPacker.init();
        for (int h = 0; h < 65536; h++) {
//            colors.setColor((CoordPacker.hilbert3X[h] * 73 << 23 & 0xFF000000)
//                    | (CoordPacker.hilbert3Y[h] * 73 << 15 & 0xFF0000)
//                    | (CoordPacker.hilbert3Z[h] * 73 << 7) | 0xFF);
            colors.setColor(full.getAt(h % len));
            colors.fillRectangle(CoordPacker.hilbertX[h] << 1, CoordPacker.hilbertY[h] << 1, 2, 2);
        }
//        for (int x = 0; x < 256; x++) {
//            for (int y = 0; y < 256; y++) {
//                colors.drawPixel(x, y, idx=(y+256>>>1)<<16|(x+255>>>1)<<8|0x880000FF);
////                colors.drawPixel(x+256, y, idx|0xAA0000FF);
////                colors.drawPixel(x, y+256, idx|0xCC0000FF);
////                colors.drawPixel(x+256, y+256, idx|0xEE0000FF);
//            }
//        }
        edit = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        edit.setBlending(Pixmap.Blending.None);
        edit.setFilter(Pixmap.Filter.NearestNeighbour);
        edit.drawPixmap(bivaOriginal, 0, 0,  width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
        pixels = edit.getPixels();
        pt = new Texture(edit);
        pt.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        png8 = new PNG8((int)(monaOriginal.getWidth() * monaOriginal.getHeight() * 1.5f));
        png = new PixmapIO.PNG((int)(monaOriginal.getWidth() * monaOriginal.getHeight() * 1.5f));
        png8.setFlipY(false);
        png.setFlipY(false);
        png8.setCompression(6);
        png.setCompression(6);
        auroraPalette = new PaletteReducer(new int[]{0x000000ff, 0x111111ff, 0x222222ff, 0x333333ff, 0x444444ff, 0x555555ff, 0x666666ff, 0x777777ff,
                0x888888ff, 0x999999ff, 0xaaaaaaff, 0xbbbbbbff, 0xccccccff, 0xddddddff, 0xeeeeeeff, 0xffffffff,
                0x007f7fff, 0x3fbfbfff, 0x00ffffff, 0xbfffffff, 0x8181ffff, 0x0000ffff, 0x3f3fbfff, 0x00007fff,
                0x0f0f50ff, 0x7f007fff, 0xbf3fbfff, 0xf500f5ff, 0xfd81ffff, 0xffc0cbff, 0xff8181ff, 0xff0000ff,
                0xbf3f3fff, 0x7f0000ff, 0x551414ff, 0x7f3f00ff, 0xbf7f3fff, 0xff7f00ff, 0xffbf81ff, 0xffffbfff,
                0xffff00ff, 0xbfbf3fff, 0x7f7f00ff, 0x007f00ff, 0x3fbf3fff, 0x00ff00ff, 0xafffafff, 0x00bfffff,
                0x007fffff, 0x4b7dc8ff, 0xbcafc0ff, 0xcbaa89ff, 0xa6a090ff, 0x7e9494ff, 0x6e8287ff, 0x7e6e60ff,
                0xa0695fff, 0xc07872ff, 0xd08a74ff, 0xe19b7dff, 0xebaa8cff, 0xf5b99bff, 0xf6c8afff, 0xf5e1d2ff,
                0x7f00ffff, 0x573b3bff, 0x73413cff, 0x8e5555ff, 0xab7373ff, 0xc78f8fff, 0xe3ababff, 0xf8d2daff,
                0xe3c7abff, 0xc49e73ff, 0x8f7357ff, 0x73573bff, 0x3b2d1fff, 0x414123ff, 0x73733bff, 0x8f8f57ff,
                0xa2a255ff, 0xb5b572ff, 0xc7c78fff, 0xdadaabff, 0xededc7ff, 0xc7e3abff, 0xabc78fff, 0x8ebe55ff,
                0x738f57ff, 0x587d3eff, 0x465032ff, 0x191e0fff, 0x235037ff, 0x3b573bff, 0x506450ff, 0x3b7349ff,
                0x578f57ff, 0x73ab73ff, 0x64c082ff, 0x8fc78fff, 0xa2d8a2ff, 0xe1f8faff, 0xb4eecaff, 0xabe3c5ff,
                0x87b48eff, 0x507d5fff, 0x0f6946ff, 0x1e2d23ff, 0x234146ff, 0x3b7373ff, 0x64ababff, 0x8fc7c7ff,
                0xabe3e3ff, 0xc7f1f1ff, 0xbed2f0ff, 0xabc7e3ff, 0xa8b9dcff, 0x8fabc7ff, 0x578fc7ff, 0x57738fff,
                0x3b5773ff, 0x0f192dff, 0x1f1f3bff, 0x3b3b57ff, 0x494973ff, 0x57578fff, 0x736eaaff, 0x7676caff,
                0x8f8fc7ff, 0xababe3ff, 0xd0daf8ff, 0xe3e3ffff, 0xab8fc7ff, 0x8f57c7ff, 0x73578fff, 0x573b73ff,
                0x3c233cff, 0x463246ff, 0x724072ff, 0x8f578fff, 0xab57abff, 0xab73abff, 0xebace1ff, 0xffdcf5ff,
                0xe3c7e3ff, 0xe1b9d2ff, 0xd7a0beff, 0xc78fb9ff, 0xc87da0ff, 0xc35a91ff, 0x4b2837ff, 0x321623ff,
                0x280a1eff, 0x401811ff, 0x621800ff, 0xa5140aff, 0xda2010ff, 0xd5524aff, 0xff3c0aff, 0xf55a32ff,
                0xff6262ff, 0xf6bd31ff, 0xffa53cff, 0xd79b0fff, 0xda6e0aff, 0xb45a00ff, 0xa04b05ff, 0x5f3214ff,
                0x53500aff, 0x626200ff, 0x8c805aff, 0xac9400ff, 0xb1b10aff, 0xe6d55aff, 0xffd510ff, 0xffea4aff,
                0xc8ff41ff, 0x9bf046ff, 0x96dc19ff, 0x73c805ff, 0x6aa805ff, 0x3c6e14ff, 0x283405ff, 0x204608ff,
                0x0c5c0cff, 0x149605ff, 0x0ad70aff, 0x14e60aff, 0x7dff73ff, 0x4bf05aff, 0x00c514ff, 0x05b450ff,
                0x1c8c4eff, 0x123832ff, 0x129880ff, 0x06c491ff, 0x00de6aff, 0x2deba8ff, 0x3cfea5ff, 0x6affcdff,
                0x91ebffff, 0x55e6ffff, 0x7dd7f0ff, 0x08ded5ff, 0x109cdeff, 0x055a5cff, 0x162c52ff, 0x0f377dff,
                0x004a9cff, 0x326496ff, 0x0052f6ff, 0x186abdff, 0x2378dcff, 0x699dc3ff, 0x4aa4ffff, 0x90b0ffff,
                0x5ac5ffff, 0xbeb9faff, 0x786ef0ff, 0x4a5affff, 0x6241f6ff, 0x3c3cf5ff, 0x101cdaff, 0x0010bdff,
                0x231094ff, 0x0c2148ff, 0x5010b0ff, 0x6010d0ff, 0x8732d2ff, 0x9c41ffff, 0xbd62ffff, 0xb991ffff,
                0xd7a5ffff, 0xd7c3faff, 0xf8c6fcff, 0xe673ffff, 0xff52ffff, 0xda20e0ff, 0xbd29ffff, 0xbd10c5ff,
                0x8c14beff, 0x5a187bff, 0x641464ff, 0x410062ff, 0x320a46ff, 0x551937ff, 0xa01982ff, 0xc80078ff,
                0xff50bfff, 0xff6ac5ff, 0xfaa0b9ff, 0xfc3a8cff, 0xe61e78ff, 0xbd1039ff, 0x98344dff, 0x911437ff,
        });
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
        bivaPalette = new PaletteReducer(bivaOriginal, 400);
        monaPalette = new PaletteReducer(monaOriginal, 400);
        colorsPalette = new PaletteReducer(colors, 400);
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
        byte r, g, b;
        edit.setColor(0);
        edit.fill();
        switch (mode)
        {
            case 0:
                Gdx.graphics.setTitle("(Original) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                break;
            case 1:
                Gdx.graphics.setTitle("(64-value channels) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                while (pixels.remaining() >= 4)
                {
                    color = (pixels.getInt() & 0xFCFCFCFF);
                    color |= color >>> 6 & 0x03030300;
                    pixels.putInt(pixels.position() - 4, color);
                }
                pixels.rewind();
                break;
            case 2:
                Gdx.graphics.setTitle("(Adaptive Palette, Sierra Lite dither) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                bivaPalette.reduce(edit);
                break;
            case 3:
                Gdx.graphics.setTitle("(Adaptive Palette, Burkes dither) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                bivaPalette.reduceBurkes(edit);
                break;
            case 4:
                Gdx.graphics.setTitle("(Adaptive Palette, Noise-Based dither) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                bivaPalette.reduceWithNoise(edit);
                break;
            case 5:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Sierra Lite dither)  Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                auroraPalette.reduce(edit);
                break;
            case 6:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Burkes dither) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                auroraPalette.reduce(edit);
                break;
            case 7:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Noise-Based dither)  Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                auroraPalette.reduceWithNoise(edit);
                break;
            case 8:
                Gdx.graphics.setTitle("(6-value channels) Étang en Ile de France by Henri Biva");
                edit.drawPixmap(bivaOriginal, 0, 0, width1, height1, width - width1 >> 1, height - height1 >> 1, width1, height1);
                switch (edit.getFormat()) {
                    case RGBA8888: {
                        while (pixels.remaining() >= 4) {
                            pos = pixels.position();
                            color = (((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAAAA & 0xFF000000)
                                    | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAA & 0xFF0000)
                                    | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA & 0xFF00)
                                    | 255) & (pixels.get() >> 31);
                            pixels.putInt(pos, color);
                            pixels.position(pos + 4);
                        }
                    }
                    break;
                    case RGB888: {
                        while (pixels.remaining() >= 3) {
                            pos = pixels.position();
                            pixels.put(pos, (byte) ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.put(pos + 1, (byte)((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.put(pos + 2, (byte) ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.position(pos + 3);
                        }
                    }
                    break;
                }
                pixels.rewind();
                break;
            case 9:
                Gdx.graphics.setTitle("(Original) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                break;
            case 10:
                Gdx.graphics.setTitle("(64-value channels) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                while (pixels.remaining() >= 4)
                {
                    color = (pixels.getInt() & 0xFCFCFCFF);
                    color |= color >>> 6 & 0x03030300;
                    pixels.putInt(pixels.position() - 4, color);
                }
                pixels.rewind();
                break;
            case 11:
                Gdx.graphics.setTitle("(Adaptive Palette, Sierra Lite dither) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                monaPalette.reduce(edit);
                break;
            case 12:
                Gdx.graphics.setTitle("(Adaptive Palette, Burkes dither) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                monaPalette.reduceBurkes(edit);
                break;
            case 13:
                Gdx.graphics.setTitle("(Adaptive Palette, Noise-Based dither) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                monaPalette.reduce(edit);
                break;
            case 14:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Sierra Lite dither) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                auroraPalette.reduce(edit);
                break;
            case 15:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Burkes dither) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                auroraPalette.reduceBurkes(edit);
                break;
            case 16:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Noise-Based dither) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                auroraPalette.reduceWithNoise(edit);
                break;
            case 17:
                Gdx.graphics.setTitle("(6-value channels) Mona Lisa by Leonardo da Vinci (remastered)");
                edit.drawPixmap(monaOriginal, width - width2 >> 1, 0);
                switch (edit.getFormat()) {
                    case RGBA8888: {
                        while (pixels.remaining() >= 4) {
                            pos = pixels.position();
                            color = (((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAAAA & 0xFF000000)
                                    | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAA & 0xFF0000)
                                    | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA & 0xFF00)
                                    | 255) & (pixels.get() >> 31);
                            pixels.putInt(pos, color);
                            pixels.position(pos + 4);
                        }
                    }
                    break;
                    case RGB888: {
                        while (pixels.remaining() >= 3) {
                            pos = pixels.position();
                            pixels.put(pos, (byte) ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.put(pos + 1, (byte)((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.put(pos + 2, (byte) ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.position(pos + 3);
                        }
                    }
                    break;
                }
                pixels.rewind();
                break;
            case 18:
                Gdx.graphics.setTitle("(Original) Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                break;
            case 19:
                Gdx.graphics.setTitle("(64-value channels) Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                while (pixels.remaining() >= 4)
                {
                    color = (pixels.getInt() & 0xFCFCFCFF);
                    color |= color >>> 6 & 0x03030300;
                    pixels.putInt(pixels.position() - 4, color);
                }
                pixels.rewind();
                break;
            case 20:
                Gdx.graphics.setTitle("(Adaptive Palette, Sierra Lite dither) Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                colorsPalette.reduce(edit);
                break;
            case 21:
                Gdx.graphics.setTitle("(Adaptive Palette, Burkes dither) Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                colorsPalette.reduceBurkes(edit);
                break;
            case 22:
                Gdx.graphics.setTitle("(Adaptive Palette, Noise-Based dither) Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                colorsPalette.reduceWithNoise(edit);
                break;
            case 23:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Sierra Lite dither)  Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                auroraPalette.reduce(edit);
                break;
            case 24:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Burkes dither) Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                auroraPalette.reduce(edit);
                break;
            case 25:
                Gdx.graphics.setTitle("(DawnBringer Aurora, Noise-Based dither)  Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                auroraPalette.reduceWithNoise(edit);
                break;
            case 26:
                Gdx.graphics.setTitle("(6-value channels) Hilbert Hues");
                edit.drawPixmap(colors, 0, 0);
                switch (edit.getFormat()) {
                    case RGBA8888: {
                        while (pixels.remaining() >= 4) {
                            pos = pixels.position();
                            color = (((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAAAA & 0xFF000000)
                                    | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAAAA & 0xFF0000)
                                    | ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA & 0xFF00)
                                    | 255) & (pixels.get() >> 31);
                            pixels.putInt(pos, color);
                            pixels.position(pos + 4);
                        }
                    }
                    break;
                    case RGB888: {
                        while (pixels.remaining() >= 3) {
                            pos = pixels.position();
                            pixels.put(pos, (byte) ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.put(pos + 1, (byte)((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.put(pos + 2, (byte) ((((pixels.get() & 0xFF) + 21) * 6 >>> 8) * 0x2AAA >>> 8));
                            pixels.position(pos + 3);
                        }
                    }
                    break;
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