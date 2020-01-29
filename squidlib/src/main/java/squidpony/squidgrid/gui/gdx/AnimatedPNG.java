package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.StreamUtils;
import squidpony.annotation.GwtIncompatible;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Animated full-color (with full alpha) PNG encoder with compression.
 * An instance can be reused to encode multiple animated PNGs with minimal allocation.
 * If you are only using a small number of colors (256 or less), you can use {@link PNG8#write(FileHandle, Array, int)}
 * to write an indexed-color animated PNG instead, which should have a smaller file size.
 * <a href="https://raw.githubusercontent.com/tommyettinger/SquidLib-Demos/master/NorthernLights/SampleAnimatedPNG.png">The full-color, full-alpha animated images can look like this</a>.
 * 
 * <pre>
 * Copyright (c) 2007 Matthias Mann - www.matthiasmann.de
 * Copyright (c) 2014 Nathan Sweet
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
 *
 * @author Matthias Mann
 * @author Nathan Sweet
 * @author Tommy Ettinger
 */
@GwtIncompatible
public class AnimatedPNG implements Disposable {
    static private final byte[] SIGNATURE = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
    static private final int IHDR = 0x49484452, acTL = 0x6163544C,
            fcTL = 0x6663544C, IDAT = 0x49444154,
            fdAT = 0x66644154, IEND = 0x49454E44;
    static private final byte COLOR_ARGB = 6;
    static private final byte COMPRESSION_DEFLATE = 0;
    static private final byte FILTER_NONE = 0;
    static private final byte INTERLACE_NONE = 0;
    static private final byte PAETH = 4;

    private final ChunkBuffer buffer;
    private final Deflater deflater;
    private ByteArray lineOutBytes, curLineBytes, prevLineBytes;
    private boolean flipY = true;
    private int lastLineLen;

    public AnimatedPNG () {
        this(128 * 128);
    }

    public AnimatedPNG (int initialBufferSize) {
        buffer = new ChunkBuffer(initialBufferSize);
        deflater = new Deflater();
    }

    /**
     * If true, the resulting AnimatedPNG is flipped vertically. Default is true.
     */
    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    /**
     * Sets the deflate compression level. Default is {@link Deflater#DEFAULT_COMPRESSION}.
     */
    public void setCompression(int level) {
        deflater.setLevel(level);
    }

    /**
     * Writes an animated PNG file consisting of the given {@code frames} to the given {@code file}, at 60 frames per second.
     * @param file the file location to write to; any existing file with this name will be overwritten
     * @param frames an Array of Pixmap frames to write in order to the animated PNG
     * @throws IOException
     */
    public void write(FileHandle file, Array<Pixmap> frames) throws IOException {
        OutputStream output = file.write(false);
        try {
            write(output, frames, 60);
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }

    /**
     * Writes an animated PNG file consisting of the given {@code frames} to the given {@code file},
     * at {@code fps} frames per second.
     * @param file the file location to write to; any existing file with this name will be overwritten
     * @param frames an Array of Pixmap frames to write in order to the animated PNG
     * @param fps how many frames per second the animated PNG should display
     * @throws IOException
     */
    public void write(FileHandle file, Array<Pixmap> frames, int fps) throws IOException {
        OutputStream output = file.write(false);
        try {
            write(output, frames, fps);
        } finally {
            StreamUtils.closeQuietly(output);
        }
    }

    /**
     * Writes animated PNG data consisting of the given {@code frames} to the given {@code output} stream without
     * closing the stream, at {@code fps} frames per second.
     * @param output the stream to write to; the stream will not be closed
     * @param frames an Array of Pixmap frames to write in order to the animated PNG
     * @param fps how many frames per second the animated PNG should display
     * @throws IOException
     */
    public void write(OutputStream output, Array<Pixmap> frames, int fps) throws IOException {
        Pixmap pixmap = frames.first();
        DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(buffer, deflater);
        DataOutputStream dataOutput = new DataOutputStream(output);
        dataOutput.write(SIGNATURE);
        final int width = pixmap.getWidth();
        final int height = pixmap.getHeight();

        buffer.writeInt(IHDR);
        buffer.writeInt(width);
        buffer.writeInt(height);
        buffer.writeByte(8); // 8 bits per component.
        buffer.writeByte(COLOR_ARGB);
        buffer.writeByte(COMPRESSION_DEFLATE);
        buffer.writeByte(FILTER_NONE);
        buffer.writeByte(INTERLACE_NONE);
        buffer.endChunk(dataOutput);

        buffer.writeInt(acTL);
        buffer.writeInt(frames.size);
        buffer.writeInt(0);
        buffer.endChunk(dataOutput);

        int lineLen = width * 4;
        byte[] lineOut, curLine, prevLine;
        ByteBuffer pixels;
        int oldPosition;
        boolean rgba8888 = pixmap.getFormat() == Pixmap.Format.RGBA8888;
        int seq = 0;
        for (int i = 0; i < frames.size; i++) {

            buffer.writeInt(fcTL);
            buffer.writeInt(seq++);
            buffer.writeInt(width);
            buffer.writeInt(height);
            buffer.writeInt(0);
            buffer.writeInt(0);
            buffer.writeShort(1);
            buffer.writeShort(fps);
            buffer.writeByte(0);
            buffer.writeByte(0);
            buffer.endChunk(dataOutput);

            if (i == 0) {
                buffer.writeInt(IDAT);
            } else {
                pixmap = frames.get(i);
                buffer.writeInt(fdAT);
                buffer.writeInt(seq++);
            }
            deflater.reset();

            if (lineOutBytes == null) {
                lineOut = (lineOutBytes = new ByteArray(lineLen)).items;
                curLine = (curLineBytes = new ByteArray(lineLen)).items;
                prevLine = (prevLineBytes = new ByteArray(lineLen)).items;
            } else {
                lineOut = lineOutBytes.ensureCapacity(lineLen);
                curLine = curLineBytes.ensureCapacity(lineLen);
                prevLine = prevLineBytes.ensureCapacity(lineLen);
                for (int ln = 0, n = lastLineLen; ln < n; ln++)
                    prevLine[ln] = 0;
            }
            lastLineLen = lineLen;

            pixels = pixmap.getPixels();
            oldPosition = pixels.position();
            for (int y = 0; y < height; y++) {
                int py = flipY ? (height - y - 1) : y;
                if (rgba8888) {
                    pixels.position(py * lineLen);
                    pixels.get(curLine, 0, lineLen);
                } else {
                    for (int px = 0, x = 0; px < width; px++) {
                        int pixel = pixmap.getPixel(px, py);
                        curLine[x++] = (byte) ((pixel >> 24) & 0xff);
                        curLine[x++] = (byte) ((pixel >> 16) & 0xff);
                        curLine[x++] = (byte) ((pixel >> 8) & 0xff);
                        curLine[x++] = (byte) (pixel & 0xff);
                    }
                }

                lineOut[0] = (byte) (curLine[0] - prevLine[0]);
                lineOut[1] = (byte) (curLine[1] - prevLine[1]);
                lineOut[2] = (byte) (curLine[2] - prevLine[2]);
                lineOut[3] = (byte) (curLine[3] - prevLine[3]);

                for (int x = 4; x < lineLen; x++) {
                    int a = curLine[x - 4] & 0xff;
                    int b = prevLine[x] & 0xff;
                    int c = prevLine[x - 4] & 0xff;
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
                    lineOut[x] = (byte) (curLine[x] - c);
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
        }
        buffer.writeInt(IEND);
        buffer.endChunk(dataOutput);

        output.flush();
    }

    /**
     * Disposal should probably be done explicitly, especially if using JRE versions after 8.
     * In Java 8 and earlier, you could rely on finalize() doing what this does, but that isn't
     * a safe assumption in Java 9 and later. Note, don't use the same AnimatedPNG object after you call
     * this method; you'll need to make a new one if you need to write again after disposing.
     */
    public void dispose() {
        deflater.end();
    }

}
