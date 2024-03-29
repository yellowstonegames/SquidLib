/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony;

import squidpony.annotation.Beta;
import squidpony.squidmath.CoordPacker;

/**
 * Very early way of additional compression that can be applied to 2D double and byte arrays. This doesn't compress a
 * well as using {@link LZSEncoding} on a simply-serialized String produced by {@link Converters#convertArrayDouble2D},
 * but you can use LZSEncoding on the results of this class to significantly reduce output size. Testing on a heat map
 * of byte values from -128 to 127 from a world map:
 * <pre>
 * Base size   : 1143142 // this is an uncompressed String produced by Converters
 * LZS size    : 89170   // this uses LZSEncoding alone on the above Base string
 * Custom size : 216209  // this uses GridCompression alone
 * Both size   : 43120   // this uses GridCompression followed by LZSEncoding
 * </pre>
 * <br>
 * This class is marked Beta because it still has some improvement that can be done on it, like having more support for
 * other array types.
 * <br>
 * Created by Tommy Ettinger on 4/27/2020.
 */
@Beta
public class GridCompression {
    public static String compress(double[][] grid)
    {
        return compress(doubleToByteGrid(grid));
    }
    public static String compress(byte[][] grid)
    {
        CoordPacker.init();
        final int width = grid.length, height = grid[0].length;
        StringBuilder[] packs = new StringBuilder[8];
        for (int i = 0; i < 8; i++) {
            packs[i] = new StringBuilder(width * height >> 3);
        }
        StringBuilder packing = new StringBuilder(width * height + 128);
        StringKit.appendHex(packing, width);
        StringKit.appendHex(packing, height);
        final int chunksX = width + 255 >> 8, chunksY = height + 127 >> 7;
        short[] skip = new short[8];
        boolean[] on = new boolean[8];
        for (int bigX = 0, baseX = 0; bigX < chunksX; bigX++, baseX += 256) {
            for (int bigY = 0, baseY = 0; bigY < chunksY; bigY++, baseY += 128) {
                for (int p = 0; p < 8; p++) {
                    packs[p].append(';');
                    skip[p] = 0;
                    on[p] = false;
                }
                boolean current;
                short hx, hy;
                int xSize = Math.min(256, width - baseX), ySize = Math.min(128, height - baseY),
                        limit = 0x8000, mapLimit = xSize * ySize, code;
                if (xSize <= 128) {
                    limit >>= 1;
                    if (xSize <= 64) {
                        limit >>= 1;
                        if (ySize <= 64) {
                            limit >>= 1;
                            if (ySize <= 32) {
                                limit >>= 1;
                                if (xSize <= 32) {
                                    limit >>= 1;
                                }
                            }
                        }
                    }
                }
                for (int i = 0, ml = 0; i < limit && ml < mapLimit; i++) {
                    hx = CoordPacker.hilbertX[i];
                    hy = CoordPacker.hilbertY[i];
                    if (hx >= xSize || hy >= ySize) {
                        for (int p = 0; p < 8; p++) {
                            if (on[p]) {
                                on[p] = false;
                                packs[p].append((char) (skip[p] + 256));
                                skip[p] = 0;
                            }
                        }
                        for (int p = 0; p < 8; p++) {
                            skip[p]++;
                        }
                        continue;
                    }
                    ml++;
                    code = (grid[baseX + hx][baseY + hy] & 255);
                    code ^= code >>> 1; // gray code; ensures only one bit changes between consecutive bytes
                    for (int p = 0; p < 8; p++) {
                        current = (code >>> p & 1) == 1;
                        if (current != on[p]) {
                            packs[p].append((char) (skip[p] + 256));
                            skip[p] = 0;
                            on[p] = current;
                        }
                    }
                    for (int p = 0; p < 8; p++) {
                        skip[p]++;
                    }
                }
                for (int p = 0; p < 8; p++) {
                    if (on[p])
                    {
                        packs[p].append((char) (skip[p] + 256));
                    }
                }
            }
        }
        packing.append(packs[0]);
        for (int p = 1; p < 8; p++) {
            packing.append('.').append(packs[p]);
        }
        return packing.toString();
    }
    public static byte[][] decompress(String compressed)
    {
        CoordPacker.init();
        final int width = StringKit.intFromHex(compressed), height = StringKit.intFromHex(compressed, 8, 16);
        byte[][] target = new byte[width][height];
        final int chunksX = width + 255 >> 8, chunksY = height + 127 >> 7;
        int startPack = 16, endPack, endLayer, idx;
        boolean on;
        for (int b = 0; b < 8; b++) {
            endLayer = compressed.indexOf('.', startPack+1);
            if (endLayer < 0) endLayer = compressed.length();
            for (int bigX = 0, baseX = 0; bigX < chunksX; bigX++, baseX += 256) {
                for (int bigY = 0, baseY = 0; bigY < chunksY; bigY++, baseY += 128) {
                    ++startPack;
                    endPack = Math.min(endLayer, compressed.indexOf(';', startPack));
                    if (endPack < 0) endPack = endLayer;
                    on = false;
                    idx = 0;
                    for (int p = startPack; p < endPack; p++, on = !on) {
                        if (on) {
                            for (int toSkip = idx + (compressed.charAt(p) - 256); idx < toSkip && idx < 0x8000; idx++) {
                                target[CoordPacker.hilbertX[idx] + baseX][CoordPacker.hilbertY[idx] + baseY] |= 1 << b;
                            }
                        } else {
                            idx += compressed.charAt(p) - 256;
                        }
                    }
                    startPack = endPack;
                }
            }
            ++startPack;
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int b = target[x][y] & 0xFF;
                b ^= b >>> 1;
                b ^= b >>> 2;
                target[x][y] = (byte)(b ^ b >>> 4);
            }
        }
        return target;
    }
    public static double[][] byteToDoubleGrid(byte[][] bytes) {
        return byteToDoubleGrid(bytes, new double[bytes.length][bytes[0].length]);
    }
    public static double[][] byteToDoubleGrid(byte[][] bytes, double[][] doubles) {
        final int width = Math.min(bytes.length, doubles.length),
                height = Math.min(bytes[0].length, doubles[0].length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                doubles[x][y] = (bytes[x][y] & 0xFF) * 0x1p-8;
            }
        }
        return doubles;
    }
    public static byte[][] doubleToByteGrid(double[][] doubles) {
        return doubleToByteGrid(doubles, new byte[doubles.length][doubles[0].length]);
    }
    public static byte[][] doubleToByteGrid(double[][] doubles, byte[][] bytes) {
        final int width = Math.min(doubles.length, bytes.length),
                height = Math.min(doubles[0].length, bytes[0].length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bytes[x][y] = (byte) (doubles[x][y] * 256);
            }
        }
        return bytes;
    }
    public static byte[] doubleGridToByteArray(double[][] doubles) {
        final int width = doubles.length, height = doubles[0].length;
        byte[] bytes = new byte[width * height + 8];
        bytes[0] = (byte) (width >>> 24);
        bytes[1] = (byte) (width >>> 16);
        bytes[2] = (byte) (width >>> 8);
        bytes[3] = (byte) (width);
        bytes[4] = (byte) (height >>> 24);
        bytes[5] = (byte) (height >>> 16);
        bytes[6] = (byte) (height >>> 8);
        bytes[7] = (byte) (height);
        int i = 8;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bytes[i++] = (byte)(doubles[x][y] * 256);
            }
        }
        return bytes;
    }
    public static double[][] byteArrayToDoubleGrid(byte[] bytes) {
        final int width = bytes[0] << 24 | (bytes[1] << 16 & 0xFF0000) | (bytes[2] << 8 & 0xFF00) | (bytes[3] & 0xFF),
                height = bytes[4] << 24 | (bytes[5] << 16 & 0xFF0000) | (bytes[6] << 8 & 0xFF00) | (bytes[7] & 0xFF);
        double[][] doubles = new double[width][height];
        int i = 8;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                doubles[x][y] = (bytes[i++] & 0xFF) * 0x1p-8;
            }
        }
        return doubles;
    }

    public static byte[] byteGridToByteArray(byte[][] grid) {
        final int width = grid.length, height = grid[0].length;
        byte[] bytes = new byte[width * height + 8];
        bytes[0] = (byte) (width >>> 24);
        bytes[1] = (byte) (width >>> 16);
        bytes[2] = (byte) (width >>> 8);
        bytes[3] = (byte) (width);
        bytes[4] = (byte) (height >>> 24);
        bytes[5] = (byte) (height >>> 16);
        bytes[6] = (byte) (height >>> 8);
        bytes[7] = (byte) (height);
        int i = 8;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bytes[i++] = grid[x][y];
            }
        }
        return bytes;
    }
    public static byte[][] byteArrayToByteGrid(byte[] bytes) {
        final int width = bytes[0] << 24 | (bytes[1] << 16 & 0xFF0000) | (bytes[2] << 8 & 0xFF00) | (bytes[3] & 0xFF),
                height = bytes[4] << 24 | (bytes[5] << 16 & 0xFF0000) | (bytes[6] << 8 & 0xFF00) | (bytes[7] & 0xFF);
        byte[][] grid = new byte[width][height];
        int i = 8;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = bytes[i++];
            }
        }
        return grid;
    }

}
