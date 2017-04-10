/*
 * Derived from Joise, which is derived from the Accidental Noise Library.
 * Licenses for these projects are as follows
 * 
 * ============================================================================
 * | Joise
 * ============================================================================
 * 
 * Copyright (C) 2016 Jason Taylor
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ============================================================================
 * | Accidental Noise Library
 * | --------------------------------------------------------------------------
 * | Joise is a derivative work based on Josua Tippetts' C++ library:
 * | http://accidentalnoise.sourceforge.net/index.html
 * ============================================================================
 * 
 * Copyright (C) 2011 Joshua Tippetts
 * 
 *   This software is provided 'as-is', without any express or implied
 *   warranty.  In no event will the authors be held liable for any damages
 *   arising from the use of this software.
 * 
 *   Permission is granted to anyone to use this software for any purpose,
 *   including commercial applications, and to alter it and redistribute it
 *   freely, subject to the following restrictions:
 * 
 *   1. The origin of this software must not be misrepresented; you must not
 *      claim that you wrote the original software. If you use this software
 *      in a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *   2. Altered source versions must be plainly marked as such, and must not be
 *      misrepresented as being the original software.
 *   3. This notice may not be removed or altered from any source distribution.
 */
package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Noise functions that try to conceal undesirable patterns, in 2D, 3D, 4D, and 6D, with the last two as options for
 * generating seamlessly-tiling noise using {@link Noise#seamless2D(double[][], int, int, Noise.Noise4D)} and/or
 * {@link Noise#seamless3D(double[][][], int, int, Noise.Noise6D)}. All functions can take an int seed that should
 * significantly change the pattern of noise produced. Incorporates code from Joise; the full library is available at
 * https://github.com/SudoPlayGames/Joise , and this class adds rather significant optimization in a few methods,
 * especially 6D noise. Joise is derived from the Accidental Noise Library, available in C++ at
 * http://accidentalnoise.sourceforge.net/index.html . Both Joise and ANL have many features that SquidLib has not (yet)
 * incorporated, but now that SquidLib has seamless noise, that's a nice feature that would have needed Joise before.
 * <br>
 * The name comes from the Freemasons, who as a secret society, are very good at concealing things. MasonNoise also
 * sounds a lot like MerlinNoise, which sounds a lot like PerlinNoise and WhirlingNoise. This class is very close in
 * implementation to WhirlingNoise.
 */
@Beta
public class MasonNoise extends SeededNoise {
    public static final MasonNoise instance = new MasonNoise();

    public MasonNoise() {
        this(0x1337BEEF);
    }

    public MasonNoise(int seed) {
        super(seed);

    }

    public double getNoise(final double x, final double y) {
        return noise(x, y, defaultSeed);
    }

    public double getNoise(final double x, final double y, final double z) {
        return noise(x, y, z, defaultSeed);
    }

    public double getNoise(final double x, final double y, final double z, final double w) {
        return noise(x, y, z, w, defaultSeed);
    }

    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return noise(x, y, z, w, u, v, defaultSeed);
    }

    public double getNoiseWithSeed(final double x, final double y, final int seed) {
        return noise(x, y, seed);
    }

    public double getNoiseWithSeed(final double x, final double y, final double z, final int seed) {
        return noise(x, y, z, seed);
    }

    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final int seed) {
        return noise(x, y, z, w, seed);
    }

    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final int seed) {
        return noise(x, y, z, w, u, v, seed);
    }

    //protected static final double[] gradient2DLUT = {0, 1, 0, -1, 1, 0, -1, 0};

    //protected static final double[] gradient3DLUT = {0, 0, 1, 0, 0, -1, 0, 1, 0, 0, -1, 0, 1, 0, 0, -1, 0, 0};

    protected static final int[] SIMPLEX = {0, 1, 3, 7, 0, 1, 7, 3,
            0, 0, 0, 0, 0, 3, 7, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 1, 3, 7, 0, 0, 3, 1, 7, 0, 0, 0, 0,
            0, 7, 1, 3, 0, 7, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 1, 7, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 0, 7, 0, 0, 0, 0,
            1, 7, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            3, 7, 0, 1, 3, 7, 1, 0, 1, 0, 3, 7, 1, 0, 7, 3,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 7, 1,
            0, 0, 0, 0, 3, 1, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 1, 7, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 1, 3, 7, 0, 3, 1,
            0, 0, 0, 0, 7, 1, 3, 0, 3, 1, 0, 7, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 7, 1, 0, 3, 0, 0, 0, 0,
            7, 3, 0, 1, 7, 3, 1, 0};

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that only generates numbers between 0
     * (inclusive) and 4 (exclusive).
     *
     * @param x    an int to incorporate into the hash
     * @param y    an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int in the full range an int can hold
     */
    public static int hash(final int x, final int y, final int seed) {
        int a = 0x632BE5AB,
                result = 0x9E3779B9 + (a ^= 0x85157AF5 * x)
                        + (a ^= 0x85157AF5 * y)
                        + (a ^= 0x85157AF5 * seed);
        return (result * (a | 1) ^ (result >>> 11 | result << 21)) >>> 24;
    }

    /**
     * Meant to generate a somewhat-random (at least, unpredictable) int from multiple inputs. The int should probably
     * be fed as a seed into a better RNG function.
     *
     * @param x    an int to incorporate into the mix
     * @param y    an int to incorporate into the mix
     * @param seed an int to incorporate into the mix
     * @return a pseudo-random-like int that can have any bits set
     */
    public static int mix(int x, int y, int seed) {
        return (seed = x * 0x5a34f ^ y * 0xc29cb ^ seed * 0x63413) ^ ((seed >>> 31 - (x = seed & 30)) | seed << 1 + x);
    }

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that only generates numbers between 0
     * (inclusive) and 12 (exclusive).
     *
     * @param x    an int to incorporate into the hash
     * @param y    an int to incorporate into the hash
     * @param z    an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int in the full range an int can hold
     */
    public static int hash(final int x, final int y, final int z, final int seed) {
        int a = 0x632BE5AB,
                result = 0x9E3779B9 + (a ^= 0x85157AF5 * x)
                        + (a ^= 0x85157AF5 * y)
                        + (a ^= 0x85157AF5 * z)
                        + (a ^= 0x85157AF5 * seed);
        return (result * (a | 1) ^ (result >>> 11 | result << 21)) >>> 24;
    }

    /**
     * Meant to generate a somewhat-random (at least, unpredictable) int from multiple inputs. The int should probably
     * be fed as a seed into a better RNG function.
     *
     * @param x    an int to incorporate into the mix
     * @param y    an int to incorporate into the mix
     * @param z    an int to incorporate into the mix
     * @param seed an int to incorporate into the mix
     * @return a pseudo-random-like int that can have any bits set
     */
    public static int mix(int x, int y, int z, int seed) {
        return (seed = x * 0x5a34f ^ y * 0xc29cb ^ z ^ 0x13333 ^ seed * 0x63413)
                ^ ((seed >>> 31 - (x = seed & 30)) | seed << 1 + x);
    }

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that generates all 32 bits.
     *
     * @param x    an int to incorporate into the hash
     * @param y    an int to incorporate into the hash
     * @param z    an int to incorporate into the hash
     * @param w    an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int in the full range an int can hold
     */
    public static int hash(final int x, final int y, final int z, final int w, final int seed) {
        int a = 0x632BE5AB,
                result = 0x9E3779B9 + (a ^= 0x85157AF5 * x)
                        + (a ^= 0x85157AF5 * y)
                        + (a ^= 0x85157AF5 * z)
                        + (a ^= 0x85157AF5 * w)
                        + (a ^= 0x85157AF5 * seed);
        return (result * (a | 1) ^ (result >>> 11 | result << 21)) >>> 24;
    }
    /**
     * Meant to generate a somewhat-random (at least, unpredictable) int from multiple inputs. The int should probably
     * be fed as a seed into a better RNG function.
     *
     * @param x    an int to incorporate into the mix
     * @param y    an int to incorporate into the mix
     * @param z    an int to incorporate into the mix
     * @param w    an int to incorporate into the mix
     * @param seed an int to incorporate into the mix
     * @return a pseudo-random-like int that can have any bits set
     */
    public static int mix(int x, int y, int z, int w, int seed) {
        return (seed = x * 0x5a34f ^ y * 0xc29cb ^ z ^ 0x13333 ^ w * 0x42023 ^ seed * 0x63413)
                ^ ((seed >>> 31 - (x = seed & 30)) | seed << 1 + x);
    }

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that generates all 32 bits.
     *
     * @param x    an int to incorporate into the hash
     * @param y    an int to incorporate into the hash
     * @param z    an int to incorporate into the hash
     * @param w    an int to incorporate into the hash
     * @param u    an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int in the full range an int can hold
     */
    public static int hash(final int x, final int y, final int z, final int w, final int u, final int seed) {
        int a = 0x632BE5AB,
                result = 0x9E3779B9 + (a ^= 0x85157AF5 * seed + x)
                        + (a ^= 0x85157AF5 * x + y)
                        + (a ^= 0x85157AF5 * y + z)
                        + (a ^= 0x85157AF5 * z + w)
                        + (a ^= 0x85157AF5 * w + u)
                        + (a ^= 0x85157AF5 * u + seed);
        return (result * a ^ (result >>> 11 | result << 21)) >>> 24;
    }

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that generates all 32 bits.
     *
     * @param x    an int to incorporate into the hash
     * @param y    an int to incorporate into the hash
     * @param z    an int to incorporate into the hash
     * @param w    an int to incorporate into the hash
     * @param u    an int to incorporate into the hash
     * @param v    an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int in the full range an int can hold
     */
    public static int hash(final int x, final int y, final int z, final int w, final int u, final int v, final int seed) {
        int a = 0x632BE5AB,
                result = 0x9E3779B9
                        + (a ^= 0x85157AF5 * (seed + z))
                        + (a ^= 0x85157AF5 * (x + w))
                        + (a ^= 0x85157AF5 * (y + u))
                        + (a ^= 0x85157AF5 * (z + v))
                        + (a ^= 0x85157AF5 * (w + seed))
                        + (a ^= 0x85157AF5 * (u + x))
                        + (a ^= 0x85157AF5 * (v + y));
        return (result * a ^ (result >>> 11 | result << 21)) >>> 24;
    }

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that generates all 32 bits.
     *
     * @param x    an int to incorporate into the hash
     * @param y    an int to incorporate into the hash
     * @param z    an int to incorporate into the hash
     * @param w    an int to incorporate into the hash
     * @param u    an int to incorporate into the hash
     * @param v    an int to incorporate into the hash
     * @param $    an int to incorporate into the hash (ran out of letters)
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int in the full range an int can hold
     */
    public static int hash(final int x, final int y, final int z, final int w, final int u, final int v, final int $, final int seed) {
        int a = 0x632BE5AB,
                result = 0x9E3779B9 + (a ^= 0x85157AF5 * seed + x)
                        + (a ^= 0x85157AF5 * x + y)
                        + (a ^= 0x85157AF5 * y + z)
                        + (a ^= 0x85157AF5 * z + w)
                        + (a ^= 0x85157AF5 * w + u)
                        + (a ^= 0x85157AF5 * u + v)
                        + (a ^= 0x85157AF5 * v + $)
                        + (a ^= 0x85157AF5 * $ + seed);
        return (result * a ^ (result >>> 11 | result << 21));
    }

    /**
     * Meant to generate a somewhat-random (at least, unpredictable) int from multiple inputs. The int should probably
     * be fed as a seed into a better RNG function.
     *
     * @param x    an int to incorporate into the mix
     * @param y    an int to incorporate into the mix
     * @param z    an int to incorporate into the mix
     * @param w    an int to incorporate into the mix
     * @param seed an int to incorporate into the mix
     * @return a pseudo-random-like int that can have any bits set
     */
    public static int mix(int x, int y, int z, int w, int u, int v, int seed) {
        return (seed = x * 0x5a34f ^ y * 0xc29cb ^ z ^ 0x13333 ^ w * 0x42023 ^ seed * 0x63413 ^ u * 0xb34eb ^ v * 0x2feb7)
                ^ ((seed >>> 31 - (x = seed & 30)) | seed << 1 + x);
    }
    /*
    private static int hash(final int x, final int y, final int z, final int w, final int u, final int v, final int seed) {
        final int result = 191 * x + 151 * y + 181 * w + 139 * z + 179 * u + 149 * v + 167 * seed;
        return 0xFF & (result ^ (result >>> 7));
    }
    */

    protected static final float
            LIMIT4 = 0.62f,
            LIMIT6 = 0.777f;
    private static final float[] m = {0, 0, 0, 0, 0, 0}, cellDist = {0, 0, 0, 0, 0, 0};
    private static final int[] distOrder = {0, 0, 0, 0, 0, 0},
            newDistOrder = new int[]{-1, 0, 0, 0, 0, 0, 0},
            intLoc = {0, 0, 0, 0, 0, 0};
    public static double noise(final double x, final double y, final double z, final double w, final int seed) {
        return noise((float)x, (float)y, (float)z, (float)w, seed);
    }
    public static double noise(final float x, final float y, final float z, final float w, final int seed) {
        double n0, n1, n2, n3, n4;
        final float s = (x + y + z + w) * F4;
        final int i = fastFloor(x + s), j = fastFloor(y + s), k = fastFloor(z + s), l = fastFloor(w + s);
        final float[] gradient4DLUT = SeededNoise.gradient4DLUT;
        final float t = (i + j + k + l) * G4,
                X0 = i - t,
                Y0 = j - t,
                Z0 = k - t,
                W0 = l - t,
                x0 = x - X0,
                y0 = y - Y0,
                z0 = z - Z0,
                w0 = w - W0;
        final int c = (x0 > y0 ? 128 : 0) | (x0 > z0 ? 64 : 0) | (y0 > z0 ? 32 : 0) | (x0 > w0 ? 16 : 0) | (y0 > w0 ? 8 : 0) | (z0 > w0 ? 4 : 0);
        final int i1 = SIMPLEX[c] >>> 2,
                j1 = SIMPLEX[c | 1] >>> 2,
                k1 = SIMPLEX[c | 2] >>> 2,
                l1 = SIMPLEX[c | 3] >>> 2,
                i2 = SIMPLEX[c] >>> 1 & 1,
                j2 = SIMPLEX[c | 1] >>> 1 & 1,
                k2 = SIMPLEX[c | 2] >>> 1 & 1,
                l2 = SIMPLEX[c | 3] >>> 1 & 1,
                i3 = SIMPLEX[c] & 1,
                j3 = SIMPLEX[c | 1] & 1,
                k3 = SIMPLEX[c | 2] & 1,
                l3 = SIMPLEX[c | 3] & 1;
        final float x1 = x0 - i1 + G4,
                y1 = y0 - j1 + G4,
                z1 = z0 - k1 + G4,
                w1 = w0 - l1 + G4,
                x2 = x0 - i2 + 2f * G4,
                y2 = y0 - j2 + 2f * G4,
                z2 = z0 - k2 + 2f * G4,
                w2 = w0 - l2 + 2f * G4,
                x3 = x0 - i3 + 3f * G4,
                y3 = y0 - j3 + 3f * G4,
                z3 = z0 - k3 + 3f * G4,
                w3 = w0 - l3 + 3f * G4,
                x4 = x0 - 1f + 4f * G4,
                y4 = y0 - 1f + 4f * G4,
                z4 = z0 - 1f + 4f * G4,
                w4 = w0 - 1f + 4f * G4;
        final int h0 = hash(i, j, k, l, seed) << 2,
                h1 = hash(i + i1, j + j1, k + k1, l + l1, seed) << 2,
                h2 = hash(i + i2, j + j2, k + k2, l + l2, seed) << 2,
                h3 = hash(i + i3, j + j3, k + k3, l + l3, seed) << 2,
                h4 = hash(i + 1, j + 1, k + 1, l + 1, seed) << 2;
        float t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if (t0 < 0)
            n0 = 0.0;
        else {
            t0 *= t0;
            n0 = t0 * t0 * (x0 * gradient4DLUT[h0] + y0 * gradient4DLUT[h0 | 1] + z0 * gradient4DLUT[h0 | 2] + w0 * gradient4DLUT[h0 | 3]);
        }
        float t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 < 0)
            n1 = 0.0;
        else {
            t1 *= t1;
            n1 = t1 * t1 * (x1 * gradient4DLUT[h1] + y1 * gradient4DLUT[h1 | 1] + z1 * gradient4DLUT[h1 | 2] + w1 * gradient4DLUT[h1 | 3]);
        }
        float t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 < 0)
            n2 = 0.0;
        else {
            t2 *= t2;
            n2 = t2 * t2 * (x2 * gradient4DLUT[h2] + y2 * gradient4DLUT[h2 | 1] + z2 * gradient4DLUT[h2 | 2] + w2 * gradient4DLUT[h2 | 3]);
        }
        float t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 < 0)
            n3 = 0.0;
        else {
            t3 *= t3;
            n3 = t3 * t3 * (x3 * gradient4DLUT[h3] + y3 * gradient4DLUT[h3 | 1] + z3 * gradient4DLUT[h3 | 2] + w3 * gradient4DLUT[h3 | 3]);
        }
        float t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 < 0)
            n4 = 0.0;
        else {
            t4 *= t4;
            n4 = t4 * t4 * (x4 * gradient4DLUT[h4] + y4 * gradient4DLUT[h4 | 1] + z4 * gradient4DLUT[h4 | 2] + w4 * gradient4DLUT[h4 | 3]);
        }
        return NumberTools.bounce(5 + 41 * (n0 + n1 + n2 + n3 + n4));
    }

    public static double noise(final double x, final double y, final double z,
                               final double w, final double u, final double v, final int seed) {
        return noise((float)x,(float)y,(float)z,(float)w,(float)u,(float)v,seed);
/*
        return noise(
                (float)(x * 1.41 + u - 0.597 * z),
                (float)(y * 1.41 + v - 0.597 * w),
                (float)(z * 1.41 + x - 0.597 * u),
                (float)(w * 1.41 + y - 0.597 * v),
                (float)(u * 1.41 + z - 0.597 * x),
                (float)(v * 1.41 + w - 0.597 * y),
                seed);

 */
    }

    public static double noise(final float x, final float y, final float z, final float w, final float u, final float v, final int seed) {

        final float s = (x + y + z + w + u + v) * F6;

        final int skewX = fastFloor(x + s), skewY = fastFloor(y + s), skewZ = fastFloor(z + s),
                skewW = fastFloor(w + s), skewU = fastFloor(u + s), skewV = fastFloor(v + s);
        final int[] intLoc = MasonNoise.intLoc, distOrder = MasonNoise.distOrder;
        intLoc[0] = skewX;
        intLoc[1] = skewY;
        intLoc[2] = skewZ;
        intLoc[3] = skewW;
        intLoc[4] = skewU;
        intLoc[5] = skewV;

        final float unskew = (skewX + skewY + skewZ + skewW + skewU + skewV) * G6;
        final float[] cellDist = MasonNoise.cellDist, gradient6DLUT = SeededNoise.gradient6DLUT;
        cellDist[0] = x - skewX + unskew;
        cellDist[1] = y - skewY + unskew;
        cellDist[2] = z - skewZ + unskew;
        cellDist[3] = w - skewW + unskew;
        cellDist[4] = u - skewU + unskew;
        cellDist[5] = v - skewV + unskew;

        int o0 = (cellDist[0]<cellDist[1]?1:0)+(cellDist[0]<cellDist[2]?1:0)+(cellDist[0]<cellDist[3]?1:0)+(cellDist[0]<cellDist[4]?1:0)+(cellDist[0]<cellDist[5]?1:0);
        int o1 = (cellDist[1]<=cellDist[0]?1:0)+(cellDist[1]<cellDist[2]?1:0)+(cellDist[1]<cellDist[3]?1:0)+(cellDist[1]<cellDist[4]?1:0)+(cellDist[1]<cellDist[5]?1:0);
        int o2 = (cellDist[2]<=cellDist[0]?1:0)+(cellDist[2]<=cellDist[1]?1:0)+(cellDist[2]<cellDist[3]?1:0)+(cellDist[2]<cellDist[4]?1:0)+(cellDist[2]<cellDist[5]?1:0);
        int o3 = (cellDist[3]<=cellDist[0]?1:0)+(cellDist[3]<=cellDist[1]?1:0)+(cellDist[3]<=cellDist[2]?1:0)+(cellDist[3]<cellDist[4]?1:0)+(cellDist[3]<cellDist[5]?1:0);
        int o4 = (cellDist[4]<=cellDist[0]?1:0)+(cellDist[4]<=cellDist[1]?1:0)+(cellDist[4]<=cellDist[2]?1:0)+(cellDist[4]<=cellDist[3]?1:0)+(cellDist[4]<cellDist[5]?1:0);
        int o5 = 15-(o0+o1+o2+o3+o4);

        distOrder[o0]=0;
        distOrder[o1]=1;
        distOrder[o2]=2;
        distOrder[o3]=3;
        distOrder[o4]=4;
        distOrder[o5]=5;

        float n = 0f;
        float skewOffset = 0f;

        for (int c = -1; c < 6; c++) {
            if (c != -1) intLoc[distOrder[c]]++;

            m[0] = cellDist[0] - (intLoc[0] - skewX) + skewOffset;
            m[1] = cellDist[1] - (intLoc[1] - skewY) + skewOffset;
            m[2] = cellDist[2] - (intLoc[2] - skewZ) + skewOffset;
            m[3] = cellDist[3] - (intLoc[3] - skewW) + skewOffset;
            m[4] = cellDist[4] - (intLoc[4] - skewU) + skewOffset;
            m[5] = cellDist[5] - (intLoc[5] - skewV) + skewOffset;

            float tc = LIMIT6;

            for (int d = 0; d < 6; d++) {
                tc -= m[d] * m[d];
            }

            if (tc > 0f) {
                final int h = hash(intLoc[0], intLoc[1], intLoc[2], intLoc[3],
                        intLoc[4], intLoc[5], seed);
                float gr = 0f;
                for (int d = 0; d < 6; d++) {
                    gr += gradient6DLUT[h * 6 + d] * m[d];
                }

                n += gr * tc * tc * tc * tc;
            }
            skewOffset += G6;
        }
        return NumberTools.bounce(5 + 13.5 * n);
        //return n * valueScaler;
    }
}
