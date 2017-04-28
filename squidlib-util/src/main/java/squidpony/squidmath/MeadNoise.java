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

import static squidpony.squidmath.NumberTools.randomSignedFloat;

/**
 * Experiments with noise functions that use an alternate "honeycomb" or tiling pattern of cells in higher dimensions,
 * but defaults to the simplex honeycomb used by SeededNoise if this can't make an attempt to change it. Currently, only
 * 4D noise methods, such as {@link #noise(float, float, float, float, int)}, use a diferent honeycomb, which turns out
 * to produce a very grid-aligned noise when used with {@link Noise#seamless2D(double[][], int, int, Noise.Noise4D)}.
 * This might seem like a problem, but seamless noise normally has a hard time producing grid patterns intentionally, so
 * if you want a nice circuit-board pattern or something similarly grid-heavy... this might help? The "mead" in the name
 * is a reference to the beverage made from honey, and the extensive work my brain had to do to understand 4D honeycomb
 * patterns. It seems that the grid is a result of using a honeycomb that can be formed from alternating hypercubes,
 * where the hypercubes contribute square faces when projected in some ways.
 * <br>
 * Preview in black and white, being fed to seamless2D: http://i.imgur.com/0UBWDVW.png
 * Preview in color, also being fed to seamless2D but with one call per color channel: http://i.imgur.com/46DI3uz.png
 * <br>
 * All functions can take an int seed that should significantly change the pattern of noise produced. Based on code from
 * Joise; the full library is available at https://github.com/SudoPlayGames/Joise . Joise is derived from the Accidental
 * Noise Library, available in C++ at http://accidentalnoise.sourceforge.net/index.html .
 */
@Beta
public class MeadNoise extends SeededNoise {

    public static final MeadNoise instance = new MeadNoise();

    public MeadNoise() {
        super(0x1337BEEF);
    }
    public MeadNoise(int seed)
    {
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

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that only generates 8 bits.
     * @param x an int to incorporate into the hash
     * @param y an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int between 0 and 255, inclusive on both
     */
    //0x89 0x95 0xA3 0xB3 0xC5 0xD3 0xE3
    /*
    public static int hash(final int x, final int y, final int seed) {
        return   ((x    ^ 0x9E3779B9 * (seed + y))
                + (y    ^ 0x632BE5AB * (x + seed))
                + (seed ^ 0x632BE5AB * (y + x))) >>> 24;
    }
    */
    public static int hash(final int x, final int y, final int seed) {
        int a = 0x632BE5AB;
        return  (0x9E3779B9
                + (a ^= 0x85157AF5 * seed + x)
                + (a ^= 0x85157AF5 * x + y)
                + (a ^= 0x85157AF5 * y + seed)) * a >>> 24;
    }

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that only generates 8 bits.
     * @param x an int to incorporate into the hash
     * @param y an int to incorporate into the hash
     * @param z an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int between 0 and 255, inclusive on both
     */
    //0x89 0x95 0xA3 0xB3 0xC5 0xD3 0xE3
    /*
    public static int hash(final int x, final int y, final int z, final int seed) {
        return   (z    + 0xD3 * (seed + y)
                ^ seed + 0xB5 * (x + z   )
                ^ x    + 0xC1 * (y + seed)
                ^ y    + 0xE3 * (z + x   )) & 255;
    }
    */
    public static int hash(final int x, final int y, final int z, final int seed) {
        int a = 0x632BE5AB;
        return  (0x9E3779B9
                + (a ^= 0x85157AF5 * seed + x)
                + (a ^= 0x85157AF5 * x + y)
                + (a ^= 0x85157AF5 * y + z)
                + (a ^= 0x85157AF5 * z + seed)) * a >>> 24;
    }
    
    /**
     * Possibly useful outside SeededNoise. A customized 5-input hash that mixes around all inputs fairly well, and
     * produces 8 bits.
     * @param x an int to incorporate into the hash
     * @param y an int to incorporate into the hash
     * @param z an int to incorporate into the hash
     * @param w an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int between 0 and 255, inclusive on both
     */
    //0x89 0x95 0xA3 0xB3 0xC5 0xD3 0xE3
    public static int hash(final int x, final int y, final int z, final int w, final int seed) {
        return (
                z    + 0xD3 * (seed + y)
              ^ w    + 0xB5 * (x + z   )
              ^ seed + 0xC1 * (y + w   )
              ^ x    + 0x95 * (z + seed)
              ^ y    + 0xA3 * (w + x   )) & 255;
    }
    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that generates 32 bits.
     * @param x an int to incorporate into the hash
     * @param y an int to incorporate into the hash
     * @param z an int to incorporate into the hash
     * @param w an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int with the full range of possible values
     */
    public static int hashAlt(final int x, final int y, final int z, final int w, final int seed) {
        int a = 0x632BE5AB, result = (0x9E3779B9
                + (a ^= 0x85157AF5 * seed + x)
                + (a ^= 0x85157AF5 * x + y)
                + (a ^= 0x85157AF5 * y + z)
                + (a ^= 0x85157AF5 * z + w)
                + (a ^= 0x85157AF5 * w + seed));
        return (result * (a | 1) ^ (result >>> 11 | result << 21));
    }
    /**
     * Possibly useful outside SeededNoise. A customized 5-input hash that mixes around all inputs fairly well, and
     * produces 8 bits.
     * @param x an int to incorporate into the hash
     * @param y an int to incorporate into the hash
     * @param z an int to incorporate into the hash
     * @param w an int to incorporate into the hash
     * @param u an int to incorporate into the hash
     * @param v an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int between 0 and 255, inclusive on both
     */
    //0x89 0x95 0xA3 0xB3 0xC5 0xD3 0xE3
    public static int hash(final int x, final int y, final int z, final int w, final int u, final int v, final int seed) {
        return (
                z    + 0x89 * (seed + x)
              ^ w    + 0xC1 * (x + y)
              ^ u    + 0x95 * (y + z)
              ^ v    + 0xD3 * (z + w)
              ^ seed + 0xA3 * (w + u)
              ^ x    + 0xE3 * (u + v)
              ^ y    + 0xB5 * (v + seed)) & 255;
    }

    /**
     * Possibly useful outside SeededNoise. An unrolled version of CrossHash.Wisp that only generates 8 bits.
     * @param x an int to incorporate into the hash
     * @param y an int to incorporate into the hash
     * @param z an int to incorporate into the hash
     * @param w an int to incorporate into the hash
     * @param u an int to incorporate into the hash
     * @param v an int to incorporate into the hash
     * @param seed an int to incorporate into the hash
     * @return a pseudo-random-like int between 0 and 255, inclusive on both
     */
    public static int hashAlt(final int x, final int y, final int z, final int w, final int u, final int v, final int seed) {
        int a = 0x632BE5AB;
        return (0x9E3779B9
                + (a ^= 0x85157AF5 * seed + x)
                + (a ^= 0x85157AF5 * x + y)
                + (a ^= 0x85157AF5 * y + z)
                + (a ^= 0x85157AF5 * z + w)
                + (a ^= 0x85157AF5 * w + u)
                + (a ^= 0x85157AF5 * u + v)
                + (a ^= 0x85157AF5 * v + seed)) * a >>> 24;
    }

    /*
    private static int hash(final int x, final int y, final int z, final int w, final int u, final int v, final int seed) {
        final int result = 191 * x + 151 * y + 181 * w + 139 * z + 179 * u + 149 * v + 167 * seed;
        return 0xFF & (result ^ (result >>> 7));
    }
    */
//    /**
//     * Possibly useful outside SeededNoise. A fast, low-to-mid-quality hash that generates 8 bits.
//     * @param x an int to incorporate into the hash
//     * @param y an int to incorporate into the hash
//     * @param seed an int to incorporate into the hash
//     * @return a pseudo-random-like int between 0 and 255, inclusive on both
//     */
//    public static int hash(final int x, final int y, int seed) {
//        return ((seed = x * 0x5a34f ^ y * 0xc29cb ^ seed * 0x63413) ^ ((seed >>> 31 - (seed & 30)) | seed << 1 + (seed & 30))) & 255;
//    }
//
//    /**
//     * Possibly useful outside SeededNoise. A fast, low-to-mid-quality hash that generates 8 bits.
//     * @param x an int to incorporate into the hash
//     * @param y an int to incorporate into the hash
//     * @param z an int to incorporate into the hash
//     * @param seed an int to incorporate into the hash
//     * @return a pseudo-random-like int between 0 and 255, inclusive on both
//     */
//    public static int hash(final int x, final int y, final int z, int seed) {
//        return ((seed = x * 0x5a34f ^ y * 0xc29cb ^ z ^ 0x13333 ^ seed * 0x63413)
//                ^ ((seed >>> 31 - (seed & 30)) | seed << 1 + (seed & 30))) & 255;
//    }
//    /**
//     * Possibly useful outside SeededNoise. A fast, low-to-mid-quality hash that generates 8 bits.
//     * @param x an int to incorporate into the hash
//     * @param y an int to incorporate into the hash
//     * @param z an int to incorporate into the hash
//     * @param w an int to incorporate into the hash
//     * @param seed an int to incorporate into the hash
//     * @return a pseudo-random-like int between 0 and 255, inclusive on both
//     */
//    public static int hash(final int x, final int y, final int z, final int w, int seed) {
//        return ((seed = x * 0x5a34f ^ y * 0xc29cb ^ z ^ 0x13333 ^ w * 0x42023 ^ seed * 0x63413)
//                ^ ((seed >>> 31 - (seed & 30)) | seed << 1 + (seed & 30))) & 255;
//    }
//    /**
//     * Possibly useful outside SeededNoise. A fast, low-to-mid-quality hash that generates 8 bits.
//     * @param x an int to incorporate into the hash
//     * @param y an int to incorporate into the hash
//     * @param z an int to incorporate into the hash
//     * @param w an int to incorporate into the hash
//     * @param u an int to incorporate into the hash
//     * @param v an int to incorporate into the hash
//     * @param seed an int to incorporate into the hash
//     * @return a pseudo-random-like int between 0 and 255, inclusive on both
//     */
//    public static int hash(final int x, final int y, final int z, final int w, final int u, final int v, int seed) {
//        return ((seed = x * 0x5a34f ^ y * 0xc29cb ^ z ^ 0x13333 ^ w * 0x42023 ^ u * 0xb34eb ^ v * 0x2feb7 ^ seed * 0x63413)
//                ^ ((seed >>> 31 - (seed & 30)) | seed << 1 + (seed & 30))) & 255;
//    }

    public static double noise(final double x, final double y, final double z, final double w, final int seed) {
        return noise((float)x, (float)y, (float)z, (float)w, seed);
    }
    public static double noise(final float x, final float y, final float z, final float w, final int seed) {
        double n = 0.0;
        //final float s = (x + y + z + w) * F4;
        final int seed2 = (seed >>> 21) ^ (seed & 0x7ff) - (seed >>> 11 & 0x3ff),
                i = fastFloor(x * 2.430f + seed2),
                j = fastFloor(y * 2.430f + seed2),
                k = fastFloor(z * 2.430f + seed2),
                l = fastFloor(w * 2.430f + seed2);
        final float
                x0 = x * 2.430f + seed2 - i, x05 = x0 - 0.5f,
                y0 = y * 2.430f + seed2 - j, y05 = y0 - 0.5f,
                z0 = z * 2.430f + seed2 - k, z05 = z0 - 0.5f,
                w0 = w * 2.430f + seed2 - l, w05 = w0 - 0.5f;
        final float dist = x05 * x05 + y05 * y05 + z05 * z05 + w05 * w05,
        xc, yc, zc, wc;
        final boolean isCentral = (dist <= 0.35);
        final int xx, yy, zz, ww;
        if(isCentral)
        {
            xx = i * 2;
            yy = j * 2;
            zz = k * 2;
            ww = l * 2;
            xc = x0;
            yc = y0;
            zc = z0;
            wc = w0;
        }
        else
        {
            xx = i * 2 + (x0 < 0.5f ? -1 : 1);
            yy = j * 2 + (y0 < 0.5f ? -1 : 1);
            zz = k * 2 + (z0 < 0.5f ? -1 : 1);
            ww = l * 2 + (w0 < 0.5f ? -1 : 1);
            xc = (x0 < 0.5f ? 0.5f + x0 : x0 - 0.5f);
            yc = (y0 < 0.5f ? 0.5f + y0 : y0 - 0.5f);
            zc = (z0 < 0.5f ? 0.5f + z0 : z0 - 0.5f);
            wc = (w0 < 0.5f ? 0.5f + w0 : w0 - 0.5f);
        }
        /*
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
                w4 = w0 - 1f + 4f * G4;*/
        int h;
        float t;
        h = hashAlt(xx+1, yy, zz, ww, seed);
        t = xc - 0.4f;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05+1) * randomSignedFloat(h += 0x9E3779B9) + (y05) * randomSignedFloat(h += 0x9E3779B9) + (z05) * randomSignedFloat(h += 0x9E3779B9) + (w05) * randomSignedFloat(h + 0x9E3779B9));
        }
        h = hashAlt(xx-1, yy, zz, ww, seed);
        t = 0.6f - xc;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05-1) * randomSignedFloat(h += 0x9E3779B9) + (y05) * randomSignedFloat(h += 0x9E3779B9) + (z05) * randomSignedFloat(h += 0x9E3779B9) + (w05) * randomSignedFloat(h + 0x9E3779B9));
        }
        h = hashAlt(xx, yy+1, zz, ww, seed);
        t = yc - 0.4f;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05) * randomSignedFloat(h += 0x9E3779B9) + (y05+1) * randomSignedFloat(h += 0x9E3779B9) + (z05) * randomSignedFloat(h += 0x9E3779B9) + (w05) * randomSignedFloat(h + 0x9E3779B9));
        }
        h = hashAlt(xx, yy-1, zz, ww, seed);
        t = 0.6f - yc;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05) * randomSignedFloat(h += 0x9E3779B9) + (y05-1) * randomSignedFloat(h += 0x9E3779B9) + (z05) * randomSignedFloat(h += 0x9E3779B9) + (w05) * randomSignedFloat(h + 0x9E3779B9));
        }
        h = hashAlt(xx, yy, zz+1, ww, seed);
        t = zc - 0.4f;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05) * randomSignedFloat(h += 0x9E3779B9) + (y05) * randomSignedFloat(h += 0x9E3779B9) + (z05+1) * randomSignedFloat(h += 0x9E3779B9) + (w05) * randomSignedFloat(h + 0x9E3779B9));
        }
        h = hashAlt(xx, yy, zz-1, ww, seed);
        t = 0.6f - zc;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05) * randomSignedFloat(h += 0x9E3779B9) + (y05) * randomSignedFloat(h += 0x9E3779B9) + (z05-1) * randomSignedFloat(h += 0x9E3779B9) + (w05) * randomSignedFloat(h + 0x9E3779B9));
        }
        h = hashAlt(xx, yy, zz, ww+1, seed);
        t = wc - 0.4f;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05) * randomSignedFloat(h += 0x9E3779B9) + (y05) * randomSignedFloat(h += 0x9E3779B9) + (z05) * randomSignedFloat(h += 0x9E3779B9) + (w05+1) * randomSignedFloat(h + 0x9E3779B9));
        }
        h = hashAlt(xx, yy, zz, ww-1, seed);
        t = 0.6f - wc;
        if (t >= 0f)
        {
            t *= t;
            n += t * t * ((x05) * randomSignedFloat(h += 0x9E3779B9) + (y05) * randomSignedFloat(h += 0x9E3779B9) + (z05) * randomSignedFloat(h += 0x9E3779B9) + (w05-1) * randomSignedFloat(h + 0x9E3779B9));
        }
        return 4 * n;
    }
}
