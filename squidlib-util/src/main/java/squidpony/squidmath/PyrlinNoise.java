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
 * A low-quality continuous noise generator with strong artifacts, meant to be used as a building block.
 * This bases its implementation on {@link ClassicNoise}; this is almost entirely meant as an optimization for Classic
 * Perlin noise. Instead of hashing all 2-to-the-D points, where D is the dimensionality, this hashes half of those
 * points (like the square base of a pyramid), gets gradient vectors as ClassicNoise does, and interpolates that with
 * the gradient vector of one point in the center of the cube Classic noise would have used (like the cap or point of a
 * pyramid).
 * <br>
 * Note: the {@link #valueNoise(long, double, double)} methods in this class return results in the range of 0.0 to 1.0,
 * while the {@link #getNoise(double, double)} and {@link #getNoiseWithSeed(double, double, long)} methods use the Noise
 * class default range of -1.0 to 1.0.
 */
@Beta
public class PyrlinNoise extends ClassicNoise implements Noise.Noise2D, Noise.Noise3D,
        Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {
    public static final PyrlinNoise instance = new PyrlinNoise();
    private static final double HR = Math.sqrt(0.5);
    public PyrlinNoise() {
    }

    public PyrlinNoise(long seed) {
        this.seed = seed;
    }

    public static double valueNoise(long seed, double x, double y)
    {
        int xFloor = (Noise.fastFloor(x)) & -2;
        x -= xFloor;
        x *= 0.5;
        int yFloor = (Noise.fastFloor(y)) & -2;
        y -= yFloor;
        y *= 0.5;
        double xd = x - 0.5;
        double yd = y - 0.5;
        double xa = Math.abs(xd);
        double ya = Math.abs(yd);
        double cap = gradCoord2D(seed, xFloor + 1, yFloor + 1, 1.0 - xa - xa, 1.0 - ya - ya);
        if(x == 0.5 && y == 0.5) return cap;
        if(xa < ya){
            // flat base, cap points up or down

//            x = (xd / ya + 1.0) * 0.5;
//            x = 1.0 - x;
            x += ya - 0.5;
            ya += ya;
            x /= ya;
//            x *= x * (3 - 2 * x);
//            ya *= ya * (3 - 2 * ya);
            x = Noise.extreme(x);
            ya = Noise.extreme(ya);
            if(yd >= 0){
                yFloor += 2;
                y--;
            }
//            ya = Math.sqrt(ya);

//            ya = (ya - 0.5);
//            ya = (ya * ya * ya + 0.125) * 4.0;

//            ya = 0.5 - ya;
//            ya *= ya * 4.0;
//            ya = 1.0 - ya;
//            if(x < 0 || x > 1)
//                System.out.println("x is out of bounds in a horizontal-base pyr: " + x);
//            if(ya < 0 || ya > 1)
//                System.out.println("ya is out of bounds in a horizontal-base pyr: " + ya);

            double cc = (1 - ya) * cap;
//            return ((1 - x) * (ya * hashPart1024(xFloor, yFloor, seed) + cc)
//                    + x * (ya * hashPart1024(xFloor + STEPX + STEPX, yFloor, seed) + cc))
//                    * (0x1.0040100401004p-10);
            return (((1 - x) * (ya * gradCoord2D(seed, xFloor, yFloor, x, y) + cc))
                    + x * (ya * gradCoord2D(seed, xFloor + 2, yFloor, x - 1, y) + cc)) * HR;
//            return (ya * ((1 - x) * gradCoord2D(seed, xFloor, yFloor, x, ya) + x * gradCoord2D(seed, xFloor + 2, yFloor, 1 - x, ya))
//                    + (1 - ya) * cap);
//            return (ya * ((1 - x) * hashPart1024(xFloor, yFloor, seed) + x * hashPart1024(xFloor + STEPX + STEPX, yFloor, seed))
//                    + (1 - ya) * cap) * (0x1.0040100401004p-10);
        }
        else {
            // vertical base, cap points left or right
            y += xa - 0.5;
            xa += xa;
            y /= xa;
//            y *= y * (3 - 2 * y);
//            xa *= xa * (3 - 2 * xa);
            y = Noise.extreme(y);
            xa = Noise.extreme(xa);
            if(xd >= 0){
                xFloor += 2;
                x--;
            }

//            xa = Math.sqrt(xa);

//            xa = (xa - 0.5);
//            xa = (xa * xa * xa + 0.125) * 4.0;

//            xa = 0.5 - xa;
//            xa *= xa * 4.0;
//            xa = 1.0 - xa;
//            if(y < 0 || y > 1)
//                System.out.println("y is out of bounds in a vertical-base pyr: " + y);
//            if(xa < 0 || xa > 1)
//                System.out.println("xa is out of bounds in a vertical-base pyr: " + xa);

            double cc = (1 - xa) * cap;
//            return ((1 - y) * (xa * hashPart1024(xFloor, yFloor, seed) + cc)
//                    + y * (xa * hashPart1024(xFloor, yFloor + STEPY + STEPY, seed) + cc)) * (0x1.0040100401004p-10);

            return (((1 - y) * (xa * gradCoord2D(seed, xFloor, yFloor, x, y) + cc))
                    + y * (xa * gradCoord2D(seed, xFloor, yFloor + 2, x, y - 1) + cc)) * HR;
//            return (xa * ((1 - y) * gradCoord2D(seed, xFloor, yFloor, xa, y) + y * gradCoord2D(seed, xFloor, yFloor + 2, xa, y - 1))
//                    + (1 - xa) * cap);
//            return (xa * ((1 - y) * hashPart1024(xFloor, yFloor, seed) + y * hashPart1024(xFloor, yFloor + 2, seed))
//                    + (1 - xa) * cap) * (0x1.0040100401004p-10);
        }
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 3D version of R2, but we only use 2 of the 3 constants.
     * @param x should be premultiplied by 0xD1B55
     * @param y should be premultiplied by 0xABC99
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static double valueNoise(int seed, double x, double y, double z)
    {
        final int STEPX = 0xDB4F1;
        final int STEPY = 0xBBE05;
        final int STEPZ = 0xA0F2F;
        int xFloor = Noise.fastFloor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        int yFloor = Noise.fastFloor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        int zFloor = Noise.fastFloor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        return ((1 - z) *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, seed)))
                + z * 
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, seed)))
                ) * (0x1.0040100401004p-10);
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 4D version of R2, but we only use 3 of the 4 constants.
     * @param x should be premultiplied by 0xDB4F1
     * @param y should be premultiplied by 0xBBE05
     * @param z should be premultiplied by 0xA0F2F
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static double valueNoise(int seed, double x, double y, double z, double w)
    {
        final int STEPX = 0xE19B1;
        final int STEPY = 0xC6D1D;
        final int STEPZ = 0xAF36D;
        final int STEPW = 0x9A695;
        int xFloor = Noise.fastFloor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        int yFloor = Noise.fastFloor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        int zFloor = Noise.fastFloor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        int wFloor = Noise.fastFloor(w);
        w -= wFloor;
        w *= w * (3 - 2 * w);
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, seed))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed)))
                ))) * (0x1.0040100401004p-10);
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 5D version of R2, but we only use 4 of the 5 constants.
     * @param x should be premultiplied by 0xE19B1
     * @param y should be premultiplied by 0xC6D1D
     * @param z should be premultiplied by 0xAF36D
     * @param w should be premultiplied by 0x9A695
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, int s) {
        s += x ^ y ^ z ^ w;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static double valueNoise(int seed, double x, double y, double z, double w, double u)
    {
        final int STEPX = 0xE60E3;
        final int STEPY = 0xCEBD7;
        final int STEPZ = 0xB9C9B;
        final int STEPW = 0xA6F57;
        final int STEPU = 0x9609D;
        int xFloor = Noise.fastFloor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        int yFloor = Noise.fastFloor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        int zFloor = Noise.fastFloor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        int wFloor = Noise.fastFloor(w);
        w -= wFloor;
        w *= w * (3 - 2 * w);
        int uFloor = Noise.fastFloor(u);
        u -= uFloor;
        u *= u * (3 - 2 * u);
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        return ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed)))
                                ))))
        ) * (0x1.0040100401004p-10);
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 6D version of R2, but we only use 5 of the 6 constants.
     * @param x should be premultiplied by 0xE60E3
     * @param y should be premultiplied by 0xCEBD7
     * @param z should be premultiplied by 0xB9C9B
     * @param w should be premultiplied by 0xA6F57
     * @param u should be premultiplied by 0x9609D
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, int s) {
        s += x ^ y ^ z ^ w ^ u;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static double valueNoise(int seed, double x, double y, double z, double w, double u, double v)
    {
        final int STEPX = 0xE95E1;
        final int STEPY = 0xD4BC7;
        final int STEPZ = 0xC1EDB;
        final int STEPW = 0xB0C8B;
        final int STEPU = 0xA127B;
        final int STEPV = 0x92E85;
        int xFloor = Noise.fastFloor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        int yFloor = Noise.fastFloor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        int zFloor = Noise.fastFloor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        int wFloor = Noise.fastFloor(w);
        w -= wFloor;
        w *= w * (3 - 2 * w);
        int uFloor = u >= 0 ? (int) u : (int) u - 1;
        u -= uFloor;
        u *= u * (3 - 2 * u);
        int vFloor = v >= 0 ? (int) v : (int) v - 1;
        v -= vFloor;
        v *= v * (3 - 2 * v);
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        vFloor *= STEPV;
        return ((1 - v) *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed)))
                                )))))
                + (v *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed)))
                                ))))))
        ) * (0x1.0040100401004p-10);
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 7D version of R2, but we only use 6 of the 7 constants.
     * @param x should be premultiplied by 0xE95E1
     * @param y should be premultiplied by 0xD4BC7
     * @param z should be premultiplied by 0xC1EDB
     * @param w should be premultiplied by 0xB0C8B
     * @param u should be premultiplied by 0xA127B
     * @param v should be premultiplied by 0x92E85
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    @Override
    public double getNoise(double x, double y) {
        return valueNoise(seed, x, y);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return valueNoise(seed, x, y);
    }
}
