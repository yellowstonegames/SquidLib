package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.*;
import squidpony.annotation.GwtIncompatible;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/** PNG-8 encoder with compression. An instance can be reused to encode multiple PNGs with minimal allocation.
 *
 * <pre>
 * Copyright (c) 2007 Matthias Mann - www.matthiasmann.de
 * Copyright (c) 2014 Nathan Sweet
 * Copyright (c) 2018 Tommy Ettinger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * </pre>
 * @author Matthias Mann
 * @author Nathan Sweet
 * @author Tommy Ettinger (PNG-8 parts only) */
@GwtIncompatible
public class PNG8 implements Disposable {
    static private final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    static private final int IHDR = 0x49484452, IDAT = 0x49444154, IEND = 0x49454E44,
            PLTE = 0x504C5445, TRNS = 0x74524E53;
    static private final byte COLOR_INDEXED = 3;
    static private final byte COMPRESSION_DEFLATE = 0;
    static private final byte FILTER_NONE = 0;
    static private final byte INTERLACE_NONE = 0;
    static private final byte PAETH = 4;

    private final ChunkBuffer buffer;
    private final Deflater deflater;
    private ByteArray lineOutBytes, curLineBytes, prevLineBytes;
    private boolean flipY = true;
    private int lastLineLen;

    public final byte[] paletteMapping = new byte[0x8000];
    public final int[] paletteArray = new int[256];

    /**
     * A lookup table from 32 possible levels in the red channel to 6 possible values in the red channel.
     */
    private static final int[]
            redLUT =   {
            0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE00003A,
            0xFE00003A, 0xFE00003A, 0xFE00003A, 0xFE00003A, 0xFE00003A, 0xFE00003A, 0xFE000074, 0xFE000074,
            0xFE000074, 0xFE000074, 0xFE000074, 0xFE0000B6, 0xFE0000B6, 0xFE0000B6, 0xFE0000B6, 0xFE0000B6,
            0xFE0000E0, 0xFE0000E0, 0xFE0000E0, 0xFE0000E0, 0xFE0000FF, 0xFE0000FF, 0xFE0000FF, 0xFE0000FF,};
    /**
     * The 6 possible values that can be used in the red channel with {@link #redLUT}.
     */
    private static final byte[] redPossibleLUT = {0x00, 0x3A, 0x74, (byte)0xB6, (byte)0xE0, (byte)0xFF};

    /**
     * A lookup table from 32 possible levels in the green channel to 7 possible values in the green channel.
     */
    private static final int[]
            greenLUT = {
            0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE003800,
            0xFE003800, 0xFE003800, 0xFE003800, 0xFE003800, 0xFE006000, 0xFE006000, 0xFE006000, 0xFE006000,
            0xFE006000, 0xFE009800, 0xFE009800, 0xFE009800, 0xFE009800, 0xFE00C400, 0xFE00C400, 0xFE00C400,
            0xFE00C400, 0xFE00EE00, 0xFE00EE00, 0xFE00EE00, 0xFE00EE00, 0xFE00FF00, 0xFE00FF00, 0xFE00FF00,};
    /**
     * The 7 possible values that can be used in the green channel with {@link #greenLUT}.
     */
    private static final byte[] greenPossibleLUT = {0x00, 0x38, 0x60, (byte)0x98, (byte)0xC4, (byte)0xEE, (byte)0xFF};
    /**
     * A lookup table from 32 possible levels in the blue channel to 6 possible values in the blue channel.
     */
    private static final int[]
            blueLUT =  {
            0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE000000, 0xFE380000,
            0xFE380000, 0xFE380000, 0xFE380000, 0xFE380000, 0xFE380000, 0xFE760000, 0xFE760000, 0xFE760000,
            0xFE760000, 0xFE760000, 0xFE760000, 0xFEAC0000, 0xFEAC0000, 0xFEAC0000, 0xFEAC0000, 0xFEAC0000,
            0xFEEA0000, 0xFEEA0000, 0xFEEA0000, 0xFEEA0000, 0xFEFF0000, 0xFEFF0000, 0xFEFF0000, 0xFEFF0000,};
    /**
     * The 6 possible values that can be used in the blue channel with {@link #blueLUT}.
     */
    private static final byte[] bluePossibleLUT = {0x00, 0x38, 0x76, (byte)0xAC, (byte)0xEA, (byte)0xFF};

    /**
     * DawnBringer's 256-color Aurora palette, version 1.1, with the grayscale values consolidated to make room for one
     * fully transparent color. Meant for use with {@link #buildKnownPalette(int[])}.
     */
    public static final int[] auroraPalette = {
            0x00000000, 0x010101FF, 0x131313FF, 0x252525FF, 0x373737FF, 0x494949FF, 0x5B5B5BFF, 0x6E6E6EFF,
            0x808080FF, 0x929292FF, 0xA4A4A4FF, 0xB6B6B6FF, 0xC9C9C9FF, 0xDBDBDBFF, 0xEDEDEDFF, 0xFFFFFFFF,
            0x007F7FFF, 0x3FBFBFFF, 0x00FFFFFF, 0xBFFFFFFF, 0x8181FFFF, 0x0000FFFF, 0x3F3FBFFF, 0x00007FFF,
            0x0F0F50FF, 0x7F007FFF, 0xBF3FBFFF, 0xF500F5FF, 0xFD81FFFF, 0xFFC0CBFF, 0xFF8181FF, 0xFF0000FF,
            0xBF3F3FFF, 0x7F0000FF, 0x551414FF, 0x7F3F00FF, 0xBF7F3FFF, 0xFF7F00FF, 0xFFBF81FF, 0xFFFFBFFF,
            0xFFFF00FF, 0xBFBF3FFF, 0x7F7F00FF, 0x007F00FF, 0x3FBF3FFF, 0x00FF00FF, 0xAFFFAFFF, 0x00BFFFFF,
            0x007FFFFF, 0x4B7DC8FF, 0xBCAFC0FF, 0xCBAA89FF, 0xA6A090FF, 0x7E9494FF, 0x6E8287FF, 0x7E6E60FF,
            0xA0695FFF, 0xC07872FF, 0xD08A74FF, 0xE19B7DFF, 0xEBAA8CFF, 0xF5B99BFF, 0xF6C8AFFF, 0xF5E1D2FF,
            0x7F00FFFF, 0x573B3BFF, 0x73413CFF, 0x8E5555FF, 0xAB7373FF, 0xC78F8FFF, 0xE3ABABFF, 0xF8D2DAFF,
            0xE3C7ABFF, 0xC49E73FF, 0x8F7357FF, 0x73573BFF, 0x3B2D1FFF, 0x414123FF, 0x73733BFF, 0x8F8F57FF,
            0xA2A255FF, 0xB5B572FF, 0xC7C78FFF, 0xDADAABFF, 0xEDEDC7FF, 0xC7E3ABFF, 0xABC78FFF, 0x8EBE55FF,
            0x738F57FF, 0x587D3EFF, 0x465032FF, 0x191E0FFF, 0x235037FF, 0x3B573BFF, 0x506450FF, 0x3B7349FF,
            0x578F57FF, 0x73AB73FF, 0x64C082FF, 0x8FC78FFF, 0xA2D8A2FF, 0xE1F8FAFF, 0xB4EECAFF, 0xABE3C5FF,
            0x87B48EFF, 0x507D5FFF, 0x0F6946FF, 0x1E2D23FF, 0x234146FF, 0x3B7373FF, 0x64ABABFF, 0x8FC7C7FF,
            0xABE3E3FF, 0xC7F1F1FF, 0xBED2F0FF, 0xABC7E3FF, 0xA8B9DCFF, 0x8FABC7FF, 0x578FC7FF, 0x57738FFF,
            0x3B5773FF, 0x0F192DFF, 0x1F1F3BFF, 0x3B3B57FF, 0x494973FF, 0x57578FFF, 0x736EAAFF, 0x7676CAFF,
            0x8F8FC7FF, 0xABABE3FF, 0xD0DAF8FF, 0xE3E3FFFF, 0xAB8FC7FF, 0x8F57C7FF, 0x73578FFF, 0x573B73FF,
            0x3C233CFF, 0x463246FF, 0x724072FF, 0x8F578FFF, 0xAB57ABFF, 0xAB73ABFF, 0xEBACE1FF, 0xFFDCF5FF,
            0xE3C7E3FF, 0xE1B9D2FF, 0xD7A0BEFF, 0xC78FB9FF, 0xC87DA0FF, 0xC35A91FF, 0x4B2837FF, 0x321623FF,
            0x280A1EFF, 0x401811FF, 0x621800FF, 0xA5140AFF, 0xDA2010FF, 0xD5524AFF, 0xFF3C0AFF, 0xF55A32FF,
            0xFF6262FF, 0xF6BD31FF, 0xFFA53CFF, 0xD79B0FFF, 0xDA6E0AFF, 0xB45A00FF, 0xA04B05FF, 0x5F3214FF,
            0x53500AFF, 0x626200FF, 0x8C805AFF, 0xAC9400FF, 0xB1B10AFF, 0xE6D55AFF, 0xFFD510FF, 0xFFEA4AFF,
            0xC8FF41FF, 0x9BF046FF, 0x96DC19FF, 0x73C805FF, 0x6AA805FF, 0x3C6E14FF, 0x283405FF, 0x204608FF,
            0x0C5C0CFF, 0x149605FF, 0x0AD70AFF, 0x14E60AFF, 0x7DFF73FF, 0x4BF05AFF, 0x00C514FF, 0x05B450FF,
            0x1C8C4EFF, 0x123832FF, 0x129880FF, 0x06C491FF, 0x00DE6AFF, 0x2DEBA8FF, 0x3CFEA5FF, 0x6AFFCDFF,
            0x91EBFFFF, 0x55E6FFFF, 0x7DD7F0FF, 0x08DED5FF, 0x109CDEFF, 0x055A5CFF, 0x162C52FF, 0x0F377DFF,
            0x004A9CFF, 0x326496FF, 0x0052F6FF, 0x186ABDFF, 0x2378DCFF, 0x699DC3FF, 0x4AA4FFFF, 0x90B0FFFF,
            0x5AC5FFFF, 0xBEB9FAFF, 0x786EF0FF, 0x4A5AFFFF, 0x6241F6FF, 0x3C3CF5FF, 0x101CDAFF, 0x0010BDFF,
            0x231094FF, 0x0C2148FF, 0x5010B0FF, 0x6010D0FF, 0x8732D2FF, 0x9C41FFFF, 0xBD62FFFF, 0xB991FFFF,
            0xD7A5FFFF, 0xD7C3FAFF, 0xF8C6FCFF, 0xE673FFFF, 0xFF52FFFF, 0xDA20E0FF, 0xBD29FFFF, 0xBD10C5FF,
            0x8C14BEFF, 0x5A187BFF, 0x641464FF, 0x410062FF, 0x320A46FF, 0x551937FF, 0xA01982FF, 0xC80078FF,
            0xFF50BFFF, 0xFF6AC5FF, 0xFAA0B9FF, 0xFC3A8CFF, 0xE61E78FF, 0xBD1039FF, 0x98344DFF, 0x911437FF,
    };
    
    public void build253Palette()
    {
        Arrays.fill(paletteArray, 0);
        int i = 0, j, rl, gl, bl, rMin, rMax=0, gMin, gMax, bMin, bMax;
        for (int r = 0; r < 6; r++) {
            rl = SColor.redPossibleLUT[r] & 0xFF;
            rMin=rMax;
            for (j = rMin; j < 32 && (SColor.redLUT[j] & 0xFF) == rl; j++) { }
            rMax=j;
            gMax = 0;
            for (int g = 0; g < 7; g++) {
                gl = SColor.greenPossibleLUT[g] & 0xFF;
                gMin=gMax;
                for (j = gMin; j < 32 && (SColor.greenLUT[j] >> 8 & 0xFF) == gl; j++) { }
                gMax=j;
                bMax = 0;
                for (int b = 0; b < 6; b++) {
                    bl = SColor.bluePossibleLUT[b] & 0xFF;
                    bMin=bMax;
                    for (j = bMin; j < 32 && (SColor.blueLUT[j] >> 16 & 0xFF) == bl; j++) { }
                    bMax=j;
                    paletteArray[++i] =
                            (rl << 24
                                    | (gl << 16 & 0xFF0000)
                                    | (bl << 8 & 0xFF00) | 0xFE);
                    for (int rm = rMin; rm < rMax; rm++) {
                        for (int gm = gMin; gm < gMax; gm++) {
                            Arrays.fill(paletteMapping, (rm << 10) + (gm << 5) + (bMin), (rm << 10) + (gm << 5) + (bMax), (byte)i);
                        }
                    }
                }
            }
        }
    }
    private int quickDistance(final int r, final int g, final int b)
    {
        return r * r + g * g + b * b;
    }
    private int quickDistance(final int color, final int r, final int g, final int b)
    {
        return quickDistance((color >>> 27 & 0x1F) - r, (color >>> 19 & 0x1F) - g, (color >>> 11 & 0x1F) - b);
    }
    public void buildKnownPalette(int[] rgbaPalette)
    {
        if(rgbaPalette == null || rgbaPalette.length < 2)
        {
            build253Palette();
            return;
        }
        Arrays.fill(paletteArray, 0);
        Arrays.fill(paletteMapping, (byte) 0);
        final int plen = Math.min(256, rgbaPalette.length);
        int color, c2, dist;
        for (int i = 0; i < plen; i++) {
            color = rgbaPalette[i];
            paletteArray[i] = color;
            paletteMapping[(color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F)] = (byte) i;
        }
        for (int r = 0; r < 32; r++) {
            for (int g = 0; g < 32; g++) {
                for (int b = 0; b < 32; b++) {
                    c2 = r << 10 | g << 5 | b;
                    if(paletteMapping[c2] == 0)
                    {
                        dist = 0x7FFFFFFF;
                        for (int i = 1; i < 256; i++) {
                            if(dist > (dist = Math.min(dist, quickDistance(paletteArray[i], r, g, b))))
                                paletteMapping[c2] = (byte)i;
                        }
                    }
                }
            }
        }
    }
    public void buildComputedPalette(Pixmap pixmap) {
        Arrays.fill(paletteArray, 0);
        int color;
        final ByteBuffer pixels = pixmap.getPixels();
        IntIntMap counts = new IntIntMap(256);
        int hasTransparent = 0;
        switch (pixmap.getFormat()) {
            case RGBA8888: {
                while (pixels.remaining() >= 4) {
                    color = (pixels.getInt() & 0xF8F8F880);
                    if ((color & 0x80) != 0) {
                        color |= (color >>> 5 & 0x07070700) | 0xFE;
                        counts.getAndIncrement(color, 0, 1);
                    }
                    else
                    {
                        hasTransparent = 1;
                    }
                }
                if(counts.size + hasTransparent <= 256)
                {
                    int idx = hasTransparent;
                    IntIntMap.Keys ks = counts.keys();
                    while(ks.hasNext())
                    {
                        color = ks.next();
                        paletteArray[idx] = color;
                        paletteMapping[(color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F)] = (byte) idx++;
                    }
                }
                else
                {
                    IntIntMap.Entries es = counts.entries();
                    SortedIntList<IntIntMap.Entry> sil = new SortedIntList<>();
                    while (es.hasNext())
                    {
                        IntIntMap.Entry ent = es.next(), ent2 = new IntIntMap.Entry();
                        ent2.key = ent.key;
                        ent2.value = ent.value;
                        sil.insert(-ent.value, ent2);
                    }
                    Arrays.fill(paletteMapping, (byte) 0);
                    Iterator<SortedIntList.Node<IntIntMap.Entry>> it = sil.iterator();
                    int[] reds = new int[256], greens = new int[256], blues = new int[256]; 
                    for (int i = 1; i < 256 && it.hasNext(); i++) {
                        color = it.next().value.key;
                        paletteArray[i] = color;
                        color = (color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F);
                        paletteMapping[color] = (byte) i;
                        reds[i] = color >>> 10;
                        greens[i] = color >>> 5 & 31;
                        blues[i] = color & 31;
                    }
                    int c2, dist;
                    for (int r = 0; r < 32; r++) {
                        for (int g = 0; g < 32; g++) {
                            for (int b = 0; b < 32; b++) {
                                c2 = r << 10 | g << 5 | b;
                                if(paletteMapping[c2] == 0)
                                {
                                    dist = 0x7FFFFFFF;
                                    for (int i = 1; i < 256; i++) {
                                        if(dist > (dist = Math.min(dist, quickDistance(reds[i] - r, greens[i] - g, blues[i] - b))))
                                            paletteMapping[c2] = (byte)i;
                                    }
                                }
                            }
                        }
                    }
                }

            }
            case RGB888: {
                while (pixels.remaining() >= 6) {
                    color = (pixels.getInt() & 0xF8F8F800);
                    color |= (color >>> 5 & 0x07070700) | 0xFE;
                    pixels.position(pixels.position() - 1);
                    counts.getAndIncrement(color, 0, 1);
                }
                if (pixels.remaining() >= 3) {
                    color = ((pixels.get() & 0xF8) << 24 | (pixels.get() & 0xF8) << 16 | (pixels.get() & 0xF8) << 8);
                    color |= (color >>> 5 & 0x07070700) | 0xFE;
                    counts.getAndIncrement(color, 0, 1);
                }
                if(counts.size <= 256)
                {
                    int idx = 0;
                    IntIntMap.Keys ks = counts.keys();
                    while(ks.hasNext())
                    {
                        color = ks.next();
                        paletteArray[idx] = color;
                        paletteMapping[(color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F)] = (byte) idx++;
                    }
                }
                else
                {
                    IntIntMap.Entries es = counts.entries();
                    SortedIntList<IntIntMap.Entry> sil = new SortedIntList<>();
                    while (es.hasNext())
                    {
                        IntIntMap.Entry ent = es.next(), ent2 = new IntIntMap.Entry();
                        ent2.key = ent.key;
                        ent2.value = ent.value;
                        sil.insert(-ent.value, ent2);
                    }
                    Arrays.fill(paletteMapping, (byte) 0);
                    Iterator<SortedIntList.Node<IntIntMap.Entry>> it = sil.iterator();
                    int[] reds = new int[256], greens = new int[256], blues = new int[256];
                    for (int i = 1; i < 256 && it.hasNext(); i++) {
                        color = it.next().value.key;
                        paletteArray[i] = color;
                        color = (color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F);
                        paletteMapping[color] = (byte) i;
                        reds[i] = color >>> 10;
                        greens[i] = color >>> 5 & 31;
                        blues[i] = color & 31;
                    }
                    int c2, dist;
                    for (int r = 0; r < 32; r++) {
                        for (int g = 0; g < 32; g++) {
                            for (int b = 0; b < 32; b++) {
                                c2 = r << 10 | g << 5 | b;
                                if(paletteMapping[c2] == 0)
                                {
                                    dist = 0x7FFFFFFF;
                                    for (int i = 1; i < 256; i++) {
                                        if(dist > (dist = Math.min(dist, quickDistance(reds[i] - r, greens[i]- g, blues[i] - b))))
                                            paletteMapping[c2] = (byte)i;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        pixels.rewind();
        //compressed = (color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F);
    }


    public PNG8() {
        this(128 * 128);
    }

    public PNG8(int initialBufferSize) {
        buffer = new ChunkBuffer(initialBufferSize);
        deflater = new Deflater();
        build253Palette();
    }

    /** If true, the resulting PNG is flipped vertically. Default is true. */
    public void setFlipY (boolean flipY) {
        this.flipY = flipY;
    }

    /** Sets the deflate compression level. Default is {@link Deflater#DEFAULT_COMPRESSION}. */
    public void setCompression (int level) {
        deflater.setLevel(level);
    }

    /**
     * Writes the given Pixmap to the requested FileHandle, computing an 8-bit palette from the most common colors in
     * pixmap. If there are 256 or less colors and none are transparent, this will use 256 colors in its palette exactly
     * with no transparent entry, but if there are more than 256 colors or any are transparent, then one color will be
     * used for "fully transparent" and 255 opaque colors will be used.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given file
     * @throws IOException if file writing fails for any reason
     */
    public void write (FileHandle file, Pixmap pixmap) throws IOException {
        write(file, pixmap, true);
    }

    /**
     * Writes the given Pixmap to the requested FileHandle, optionally computing an 8-bit palette from the most common
     * colors in pixmap. When computePalette is true, if there are 256 or less colors and none are transparent, this
     * will use 256 colors in its palette exactly with no transparent entry, but if there are more than 256 colors or
     * any are transparent, then one color will be used for "fully transparent" and 255 opaque colors will be used. When
     * computePalette is false, this uses the last palette this had computed, or a 253-color bold palette with one
     * fully-transparent color if no palette had been computed yet.
     * @param file a FileHandle that must be writable, and will have the given Pixmap written as a PNG-8 image
     * @param pixmap a Pixmap to write to the given file
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     * @throws IOException if file writing fails for any reason
     */
    public void write (FileHandle file, Pixmap pixmap, boolean computePalette) throws IOException {
        OutputStream output = file.write(false);
        try {
            write(output, pixmap, computePalette);
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }

    /** Writes the pixmap to the stream without closing the stream and computes an 8-bit palette from the Pixmap.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     */
    public void write (OutputStream output, Pixmap pixmap) throws IOException {
        write(output, pixmap, true);
    }

    /**
     * Writes the pixmap to the stream without closing the stream,
     * optionally computing an 8-bit palette from the Pixmap.
     * @param output an OutputStream that will not be closed
     * @param pixmap a Pixmap to write to the given output stream
     * @param computePalette if true, this will analyze the Pixmap and use the most common colors
     */
    public void write (OutputStream output, Pixmap pixmap, boolean computePalette) throws IOException {
        DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
        if(computePalette) 
            buildComputedPalette(pixmap);
        DataOutputStream dataOutput = new DataOutputStream(output);
        dataOutput.write(SIGNATURE);

        buffer.writeInt(IHDR);
        buffer.writeInt(pixmap.getWidth());
        buffer.writeInt(pixmap.getHeight());
        buffer.writeByte(8); // 8 bits per component.
        buffer.writeByte(COLOR_INDEXED);
        buffer.writeByte(COMPRESSION_DEFLATE);
        buffer.writeByte(FILTER_NONE);
        buffer.writeByte(INTERLACE_NONE);
        buffer.endChunk(dataOutput);
        
        buffer.writeInt(PLTE);
        for (int i = 0; i < paletteArray.length; i++) {
            int p = paletteArray[i];
            buffer.write(p>>>24);
            buffer.write(p>>>16);
            buffer.write(p>>>8);
        }
        buffer.endChunk(dataOutput);
        if(paletteArray[0] == 0) {
            buffer.writeInt(TRNS);
            buffer.write(0);
            buffer.endChunk(dataOutput);
        }
        buffer.writeInt(IDAT);
        deflater.reset();

        int lineLen = pixmap.getWidth();
        byte[] lineOut, curLine, prevLine;
        if (lineOutBytes == null) {
            lineOut = (lineOutBytes = new ByteArray(lineLen)).items;
            curLine = (curLineBytes = new ByteArray(lineLen)).items;
            prevLine = (prevLineBytes = new ByteArray(lineLen)).items;
        } else {
            lineOut = lineOutBytes.ensureCapacity(lineLen);
            curLine = curLineBytes.ensureCapacity(lineLen);
            prevLine = prevLineBytes.ensureCapacity(lineLen);
            for (int i = 0, n = lastLineLen; i < n; i++)
                prevLine[i] = 0;
        }
        lastLineLen = lineLen;

        ByteBuffer pixels = pixmap.getPixels();
        int oldPosition = pixels.position(), color;
        final int w = pixmap.getWidth();
        for (int y = 0, h = pixmap.getHeight(); y < h; y++) {
            int py = flipY ? (h - y - 1) : y;
            for (int px = 0; px < w; px++) {
                color = pixmap.getPixel(px, py);
                if ((color & 0x80) == 0)
                    curLine[px] = 0;
                else {
                    curLine[px] = paletteMapping[(color >>> 17 & 0x7C00) | (color >>> 14 & 0x3E0) | (color >>> 11 & 0x1F)];
                }
            }
                
            lineOut[0] = (byte)(curLine[0] - prevLine[0]);
            
            //Paeth
            for (int x = 1; x < lineLen; x++) {
                int a = curLine[x - 1] & 0xff;
                int b = prevLine[x] & 0xff;
                int c = prevLine[x - 1] & 0xff;
                int p = a + b - c;
                int pa = p - a;
                if (pa < 0) pa = -pa;
                int pb = p - b;
                if (pb < 0) pb = -pb;
                int pc = p - c;
                if (pc < 0) pc = -pc;
                if (pa <= pb && pa <= pc)
                    c = a;
                else if (pb <= pc) //
                    c = b;
                lineOut[x] = (byte)(curLine[x] - c);
            }

            deflaterOutput.write(PAETH);
            deflaterOutput.write(lineOut, 0, lineLen);

            byte[] temp = curLine;
            curLine = prevLine;
            prevLine = temp;
        }
        pixels.position(oldPosition);
        deflaterOutput.finish();
        buffer.endChunk(dataOutput);

        buffer.writeInt(IEND);
        buffer.endChunk(dataOutput);

        output.flush();
    }

    /** Disposal will happen automatically in {@link #finalize()} but can be done explicitly if desired. */
    public void dispose () {
        deflater.end();
    }

    static class ChunkBuffer extends DataOutputStream {
        final ByteArrayOutputStream buffer;
        final CRC32 crc;

        ChunkBuffer (int initialSize) {
            this(new ByteArrayOutputStream(initialSize), new CRC32());
        }

        private ChunkBuffer (ByteArrayOutputStream buffer, CRC32 crc) {
            super(new CheckedOutputStream(buffer, crc));
            this.buffer = buffer;
            this.crc = crc;
        }

        public void endChunk (DataOutputStream target) throws IOException {
            flush();
            target.writeInt(buffer.size() - 4);
            buffer.writeTo(target);
            target.writeInt((int)crc.getValue());
            buffer.reset();
            crc.reset();
        }
    }
}
