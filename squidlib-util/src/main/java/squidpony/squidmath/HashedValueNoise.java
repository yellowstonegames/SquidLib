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

package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * A low-quality continuous noise generator with strong grid artifacts, this is nonetheless useful as a building block.
 * This class is extremely close to {@link ValueNoise}, but allows using an {@link IPointHash} to configure how points
 * are given values (usually to add a glitchy effect). This is almost certainly slower than ValueNoise, but should still
 * be faster than the Cubic mode in {@link FastNoise}, which was (prior to this class being added) the only reasonable
 * way to show the glitch effects of things like {@link FlawedPointHash.CubeHash}.
 * <br>
 * This implements Noise1D, Noise2D, and Noise3D, and should have more dimensionality support added later. It has much
 * lower quality than {@link ClassicNoise}, but is structured similarly in many ways, and won't alter values obtained
 * from a point hash.
 */
@Beta
public class HashedValueNoise implements Noise.Noise1D, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D
//       , Noise.Noise5D, Noise.Noise6D
{
    public static final HashedValueNoise instance = new HashedValueNoise();

    public IPointHash hash;
    public HashedValueNoise() {
        this(12345);
    }

    public HashedValueNoise(int seed) {
        this.hash = new IntPointHash(seed);
    }

    public HashedValueNoise(long seed) {
        this.hash = new HastyPointHash(seed);
    }

    public HashedValueNoise(IPointHash hash){
        this.hash = hash;
    }

//    public static double valueNoise(int seed, double x, double y, double z, double w, double u)
//    {
//        int xFloor = x >= 0 ? (int) x : (int) x - 1;
//        x -= xFloor;
//        x *= x * (3 - 2 * x);
//        int yFloor = y >= 0 ? (int) y : (int) y - 1;
//        y -= yFloor;
//        y *= y * (3 - 2 * y);
//        int zFloor = z >= 0 ? (int) z : (int) z - 1;
//        z -= zFloor;
//        z *= z * (3 - 2 * z);
//        int wFloor = w >= 0 ? (int) w : (int) w - 1;
//        w -= wFloor;
//        w *= w * (3 - 2 * w);
//        int uFloor = u >= 0 ? (int) u : (int) u - 1;
//        u -= uFloor;
//        u *= u * (3 - 2 * u);
//        //0xE60E3, 0xCEBD7, 0xB9C9B, 0xA6F57, 0x9609D, 0x86D51
//        xFloor *= 0xE60E3;
//        yFloor *= 0xCEBD7;
//        zFloor *= 0xB9C9B;
//        wFloor *= 0xA6F57;
//        uFloor *= 0x9609D;
//        return ((1 - u) *
//                        ((1 - w) *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor, uFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor, uFloor, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor, uFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor, seed))))
//                                + (w *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor + 0xA6F57, uFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor, seed)))
//                                )))
//                        + (u *
//                        ((1 - w) *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor, uFloor + 0x9609D, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor, uFloor + 0x9609D, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor, uFloor + 0x9609D, seed))))
//                                + (w *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor, wFloor + 0xA6F57, uFloor + 0x9609D, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed) + x * hashPart1024(xFloor + 0xE60E3, yFloor + 0xCEBD7, zFloor + 0xB9C9B, wFloor + 0xA6F57, uFloor + 0x9609D, seed)))
//                                ))))
//        ) * (0x1.0040100401004p-10);
//    }
//
//    //// constants are the most significant 20 bits of constants from PhantomNoise, incremented if even
//    //// they should normally be used for the 6D version of R2, but we only use 5 of the 6 constants
//    //x should be premultiplied by 0xE60E3
//    //y should be premultiplied by 0xCEBD7
//    //z should be premultiplied by 0xB9C9B
//    //w should be premultiplied by 0xA6F57
//    //u should be premultiplied by 0x9609D
//    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, int s) {
//        s += x ^ y ^ z ^ w ^ u;
//        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
//    }
//
//    public static double valueNoise(int seed, double x, double y, double z, double w, double u, double v)
//    {
//        int xFloor = x >= 0 ? (int) x : (int) x - 1;
//        x -= xFloor;
//        x *= x * (3 - 2 * x);
//        int yFloor = y >= 0 ? (int) y : (int) y - 1;
//        y -= yFloor;
//        y *= y * (3 - 2 * y);
//        int zFloor = z >= 0 ? (int) z : (int) z - 1;
//        z -= zFloor;
//        z *= z * (3 - 2 * z);
//        int wFloor = w >= 0 ? (int) w : (int) w - 1;
//        w -= wFloor;
//        w *= w * (3 - 2 * w);
//        int uFloor = u >= 0 ? (int) u : (int) u - 1;
//        u -= uFloor;
//        u *= u * (3 - 2 * u);
//        int vFloor = v >= 0 ? (int) v : (int) v - 1;
//        v -= vFloor;
//        v *= v * (3 - 2 * v);
//        //0xE95E1, 0xD4BC7, 0xC1EDB, 0xB0C8B, 0xA1279, 0x92E85
//        xFloor *= 0xE95E1;
//        yFloor *= 0xD4BC7;
//        zFloor *= 0xC1EDB;
//        wFloor *= 0xB0C8B;
//        uFloor *= 0xA127B;
//        vFloor *= 0x92E85;
//        return ((1 - v) *
//                ((1 - u) *
//                        ((1 - w) *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))))
//                                + (w *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
//                                )))
//                        + (u *
//                        ((1 - w) *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))))
//                                + (w *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
//                                )))))
//                + (v *
//                ((1 - u) *
//                        ((1 - w) *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))))
//                                + (w *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
//                                )))
//                        + (u *
//                        ((1 - w) *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))))
//                                + (w *
//                                ((1 - z) *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
//                                        + z *
//                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
//                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
//                                ))))))
//        ) * (0x1.0040100401004p-10);
//    }
//
//    //// constants are the most significant 20 bits of constants from PhantomNoise, incremented if even
//    //// they should normally be used for the 7D version of R2, but we only use 6 of the 7 constants
//    //x should be premultiplied by 0xE95E1
//    //y should be premultiplied by 0xD4BC7
//    //z should be premultiplied by 0xC1EDB
//    //w should be premultiplied by 0xB0C8B
//    //u should be premultiplied by 0xA127B
//    //v should be premultiplied by 0x92E85
//    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, int s) {
//        s += x ^ y ^ z ^ w ^ u ^ v;
//        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
//    }

    @Override
    public double getNoise(double x) {
        int xFloor = x >= 0 ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * x * (x * (x * 6.0 - 15.0) + 10.0);
        return ((1 - x) * hash.hash(xFloor, 123) + x * hash.hash(xFloor + 1, 123))
                * (0x1p-31);
    }

    @Override
    public double getNoiseWithSeed(double x, long seed)
    {
        hash.setState(seed);
        return getNoise(x);
    }

    @Override
    public double getNoise(double x, double y) {
        int xFloor = x >= 0 ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * x * (x * (x * 6.0 - 15.0) + 10.0);
        int yFloor = y >= 0 ? (int) y : (int) y - 1;
        y -= yFloor;
        y *= y * y * (y * (y * 6.0 - 15.0) + 10.0);
        return ((1 - y) * ((1 - x) * hash.hash(xFloor, yFloor) + x * hash.hash(xFloor + 1, yFloor))
                + y * ((1 - x) * hash.hash(xFloor, yFloor + 1) + x * hash.hash(xFloor + 1, yFloor + 1)))
                * (0x1p-31);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        hash.setState(seed);
        return getNoise(x, y);
    }
    @Override
    public double getNoise(double x, double y, double z) {
        int xFloor = x >= 0 ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * x * (x * (x * 6.0 - 15.0) + 10.0);
        int yFloor = y >= 0 ? (int) y : (int) y - 1;
        y -= yFloor;
        y *= y * y * (y * (y * 6.0 - 15.0) + 10.0);
        int zFloor = z >= 0 ? (int) z : (int) z - 1;
        z -= zFloor;
        z *= z * z * (z * (z * 6.0 - 15.0) + 10.0);
        return ((1 - z) *
                ((1 - y) * ((1 - x) * hash.hash(xFloor, yFloor, zFloor) + x * hash.hash(xFloor + 1, yFloor, zFloor))
                        + y * ((1 - x) * hash.hash(xFloor, yFloor + 1, zFloor) + x * hash.hash(xFloor + 1, yFloor + 1, zFloor)))
                + z *
                ((1 - y) * ((1 - x) * hash.hash(xFloor, yFloor, zFloor + 1) + x * hash.hash(xFloor + 1, yFloor, zFloor + 1))
                        + y * ((1 - x) * hash.hash(xFloor, yFloor + 1, zFloor + 1) + x * hash.hash(xFloor + 1, yFloor + 1, zFloor + 1))))
                * (0x1p-31);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        hash.setState(seed);
        return getNoise(x, y, z);
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        int xFloor = x >= 0 ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * x * (x * (x * 6.0 - 15.0) + 10.0);
        int yFloor = y >= 0 ? (int) y : (int) y - 1;
        y -= yFloor;
        y *= y * y * (y * (y * 6.0 - 15.0) + 10.0);
        int zFloor = z >= 0 ? (int) z : (int) z - 1;
        z -= zFloor;
        z *= z * z * (z * (z * 6.0 - 15.0) + 10.0);
        int wFloor = w >= 0 ? (int) w : (int) w - 1;
        w -= wFloor;
        w *= w * w * (w * (w * 6.0 - 15.0) + 10.0);
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hash.hash(xFloor, yFloor, zFloor, wFloor) + x * hash.hash(xFloor + 1, yFloor, zFloor, wFloor))
                                + y * ((1 - x) * hash.hash(xFloor, yFloor + 1, zFloor, wFloor) + x * hash.hash(xFloor + 1, yFloor + 1, zFloor, wFloor)))
                        + z *
                        ((1 - y) * ((1 - x) * hash.hash(xFloor, yFloor, zFloor + 1, wFloor) + x * hash.hash(xFloor + 1, yFloor, zFloor + 1, wFloor))
                                + y * ((1 - x) * hash.hash(xFloor, yFloor + 1, zFloor + 1, wFloor) + x * hash.hash(xFloor + 1, yFloor + 1, zFloor + 1, wFloor))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hash.hash(xFloor, yFloor, zFloor, wFloor + 1) + x * hash.hash(xFloor + 1, yFloor, zFloor, wFloor + 1))
                                + y * ((1 - x) * hash.hash(xFloor, yFloor + 1, zFloor, wFloor + 1) + x * hash.hash(xFloor + 1, yFloor + 1, zFloor, wFloor + 1)))
                        + z *
                        ((1 - y) * ((1 - x) * hash.hash(xFloor, yFloor, zFloor + 1, wFloor + 1) + x * hash.hash(xFloor + 1, yFloor, zFloor + 1, wFloor + 1))
                                + y * ((1 - x) * hash.hash(xFloor, yFloor + 1, zFloor + 1, wFloor + 1) + x * hash.hash(xFloor + 1, yFloor + 1, zFloor + 1, wFloor + 1))))))
                * (0x1p-31);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        hash.setState(seed);
        return getNoise(x, y, z, w);
    }
//    @Override
//    public double getNoise(double x, double y, double z, double w, double u) {
//        return valueNoise(seed, x, y, z, w, u) * 2 - 1;
//    }
//    @Override
//    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
//        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w, u) * 2 - 1;
//    }
//    @Override
//    public double getNoise(double x, double y, double z, double w, double u, double v) {
//        return valueNoise(seed, x, y, z, w, u, v) * 2 - 1;
//    }
//    @Override
//    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
//        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w, u, v) * 2 - 1;
//    }
}
