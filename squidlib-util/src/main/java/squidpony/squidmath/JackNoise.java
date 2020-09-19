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

import static squidpony.squidmath.HastyPointHash.*;
import static squidpony.squidmath.Noise.fastFloor;

/**
 * Really weird experimental noise meant to be like {@link ValueNoise}, but faster in higher dimensions by using the
 * simplex grid that {@link SeededNoise} uses.
 * <br>
 * This gets its name from the playground toys (jacks) that are shaped like a 3-simplex (a tetrahedron).
 */
@Beta
public class JackNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {
    
    protected final long defaultSeed;
    public static final JackNoise instance = new JackNoise();

    public JackNoise() {
        defaultSeed = 0x1337BEEF2A22L;
    }
    public JackNoise(long seed)
    {
        defaultSeed = seed;
    }
    
    public double getNoise(final double x, final double y) {
        return getNoiseWithSeed(x, y, defaultSeed);
    }
    public double getNoise(final double x, final double y, final double z) {
        return getNoiseWithSeed(x, y, z, defaultSeed);
    }
    public double getNoise(final double x, final double y, final double z, final double w) {
        return getNoiseWithSeed(x, y, z, w, defaultSeed);
    }
    public double getNoise(final double x, final double y, final double z, final double w, final double u) {
        return getNoiseWithSeed(x, y, z, w, u, defaultSeed);
    }
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return getNoiseWithSeed(x, y, z, w, u, v, defaultSeed);
    }

    /**
     * Used by {@link #getNoiseWithSeed(double, double, double, double, long)} to look up the vertices of the 4D triangle analogue.
     */
    protected static final int[] SIMPLEX_4D = {0, 1, 3, 7, 0, 1, 7, 3,
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
    
    protected static final double F2 = (Math.sqrt(3.0) - 1.0) / 2.0,
            G2 = (3.0 - Math.sqrt(3.0)) / (2.0 * 3.0),
            F3 = (Math.sqrt(4.0) - 1.0) / 3.0,
            G3 = (4.0 - Math.sqrt(4.0)) / (3.0 * 4.0),
            F4 = (Math.sqrt(5.0) - 1.0) / 4.0,
            G4 = (5.0 - Math.sqrt(5.0)) / (4.0 * 5.0),
            LIMIT4 = 0.75,
            F5 = (Math.sqrt(6.0) - 1.0) / 5.0,
            G5 = (6.0 - Math.sqrt(6.0)) / (5.0 * 6.0),
            LIMIT5 = 0.7,
            F6 = (Math.sqrt(7.0) - 1.0) / 6.0,
            G6 = (7.0 - Math.sqrt(7.0)) / (6.0 * 7.0),//F6 / (1.0 + 6.0 * F6),
            LIMIT6 = 0.8375;

    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        final double s = (x + y) * F2;
        final int i = fastFloor(x + s),
                j = fastFloor(y + s);
        final double t = (i + j) * G2,
                X0 = i - t,
                Y0 = j - t,
                x0 = x - X0,
                y0 = y - Y0;
        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        final double
                x1 = x0 - i1 + G2,
                y1 = y0 - j1 + G2,
                x2 = x0 - 1 + 2 * G2,
                y2 = y0 - 1 + 2 * G2;
        double n = 0.0;
        // Calculate the contribution from the three corners for 2D gradient
        double t0 = 0.75 - x0 * x0 - y0 * y0;
        if (t0 > 0) {
            t0 *= t0;
            n += t0 * t0 * (hashAll(i, j, seed) >> 10);
        }
        double t1 = 0.75 - x1 * x1 - y1 * y1;
        if (t1 > 0) {
            t1 *= t1;
            n += t1 * t1 * (hashAll(i + i1, j + j1, seed) >> 10);
        }
        double t2 = 0.75 - x2 * x2 - y2 * y2;
        if (t2 > 0)  {
            t2 *= t2;
            n += t2 * t2 * (hashAll(i + 1, j + 1, seed) >> 10);
        }
        // Use sin_, which takes an argument in turns rather than radians, to wrap values
        return NumberTools.sin_(n * 0x1p-53);
//        return 9.11 * n;

//        double n0, n1, n2;
//        double t0 = 0.5 - x0 * x0 - y0 * y0;
//        if (t0 < 0)
//            n0 = 0;
//        else {
//            t0 *= t0;
//            n0 = t0 * t0 * (x0 * gradient2DLUT[h0] + y0 * gradient2DLUT[h0 | 1]);
//        }
//        double t1 = 0.5 - x1 * x1 - y1 * y1;
//        if (t1 < 0)
//            n1 = 0;
//        else {
//            t1 *= t1;
//            n1 = t1 * t1 * (x1 * gradient2DLUT[h1] + y1 * gradient2DLUT[h1 | 1]);
//        }
//        double t2 = 0.5 - x2 * x2 - y2 * y2;
//        if (t2 < 0)
//            n2 = 0;
//        else {
//            t2 *= t2;
//            n2 = t2 * t2 * (x2 * gradient2DLUT[h2] + y2 * gradient2DLUT[h2 | 1]);
//        }
//        return (70 * (n0 + n1 + n2)) * 1.42188695 + 0.001054489;
    }

    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        double n = 0.0;
        final double s = (x + y + z) * F3;
        final int i = fastFloor(x + s),
                j = fastFloor(y + s),
                k = fastFloor(z + s);

        final double t = (i + j + k) * G3;
        final double X0 = i - t, Y0 = j - t, Z0 = k - t,
                x0 = x - X0, y0 = y - Y0, z0 = z - Z0;

        int i1, j1, k1;
        int i2, j2, k2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            }
        }

        double x1 = x0 - i1 + G3; // Offsets for second corner in (x,y,z) coords
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + F3; // Offsets for third corner in (x,y,z) coords
        double y2 = y0 - j2 + F3;
        double z2 = z0 - k2 + F3;
        double x3 = x0 - 0.5; // Offsets for last corner in (x,y,z) coords
        double y3 = y0 - 0.5;
        double z3 = z0 - 0.5;

        // Calculate the contribution from the four corners
        double t0 = 0.75 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 > 0) {
            t0 *= t0;
            n += t0 * t0 * (hashAll(i, j, k, seed) >> 10);
        }
        double t1 = 0.75 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 > 0) {
            t1 *= t1;
            n += t1 * t1 * (hashAll(i + i1, j + j1, k + k1, seed) >> 10);
        }
        double t2 = 0.75 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 > 0) {
            t2 *= t2;
            n += t2 * t2 * (hashAll(i + i2, j + j2, k + k2, seed) >> 10);
        }
        double t3 = 0.75 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 > 0) {
            t3 *= t3;
            n += t3 * t3 * (hashAll(i + 1, j + 1, k + 1, seed) >> 10);
        }
        // Use sin_, which takes an argument in turns rather than radians, to wrap values
        return NumberTools.sin_(n * 0x1p-53); 
    }

    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        double n = 0.0;
        final double s = (x + y + z + w) * F4;
        final int i = fastFloor(x + s), j = fastFloor(y + s), k = fastFloor(z + s), l = fastFloor(w + s);
        final double t = (i + j + k + l) * G4,
                X0 = i - t,
                Y0 = j - t,
                Z0 = k - t,
                W0 = l - t,
                x0 = x - X0,
                y0 = y - Y0,
                z0 = z - Z0,
                w0 = w - W0;
        final int c = (x0 > y0 ? 128 : 0) | (x0 > z0 ? 64 : 0) | (y0 > z0 ? 32 : 0) | (x0 > w0 ? 16 : 0) | (y0 > w0 ? 8 : 0) | (z0 > w0 ? 4 : 0);
        final int i1 = SIMPLEX_4D[c] >>> 2,
                j1 = SIMPLEX_4D[c | 1] >>> 2,
                k1 = SIMPLEX_4D[c | 2] >>> 2,
                l1 = SIMPLEX_4D[c | 3] >>> 2,
                i2 = SIMPLEX_4D[c] >>> 1 & 1,
                j2 = SIMPLEX_4D[c | 1] >>> 1 & 1,
                k2 = SIMPLEX_4D[c | 2] >>> 1 & 1,
                l2 = SIMPLEX_4D[c | 3] >>> 1 & 1,
                i3 = SIMPLEX_4D[c] & 1,
                j3 = SIMPLEX_4D[c | 1] & 1,
                k3 = SIMPLEX_4D[c | 2] & 1,
                l3 = SIMPLEX_4D[c | 3] & 1;
        final double x1 = x0 - i1 + G4,
                y1 = y0 - j1 + G4,
                z1 = z0 - k1 + G4,
                w1 = w0 - l1 + G4,
                x2 = x0 - i2 + 2 * G4,
                y2 = y0 - j2 + 2 * G4,
                z2 = z0 - k2 + 2 * G4,
                w2 = w0 - l2 + 2 * G4,
                x3 = x0 - i3 + 3 * G4,
                y3 = y0 - j3 + 3 * G4,
                z3 = z0 - k3 + 3 * G4,
                w3 = w0 - l3 + 3 * G4,
                x4 = x0 - 1 + 4 * G4,
                y4 = y0 - 1 + 4 * G4,
                z4 = z0 - 1 + 4 * G4,
                w4 = w0 - 1 + 4 * G4;
        double t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            t0 *= t0;
            n += t0 * t0 * (hashAll(i, j, k, l, seed) >> 10);
        }
        double t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            t1 *= t1;
            n += t1 * t1 * (hashAll(i + i1, j + j1, k + k1, l + l1, seed) >> 10);
        }
        double t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            t2 *= t2;
            n += t2 * t2 * (hashAll(i + i2, j + j2, k + k2, l + l2, seed) >> 10);
        }
        double t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            t3 *= t3;
            n += t3 * t3 * (hashAll(i + i3, j + j3, k + k3, l + l3, seed) >> 10);
        }
        double t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            t4 *= t4;
            n += t4 * t4 * (hashAll(i + 1, j + 1, k + 1, l + 1, seed) >> 10);
        }
        //return NumberTools.bounce(5.0 + 41.0 * n);
        return NumberTools.sin_(n * 0x1p-53);
    }

    /**
     * Thanks to Mark A. Ropper for
     * <a href="https://computergraphics.stackexchange.com/questions/6408/what-might-be-causing-these-artifacts-in-5d-6d-simplex-noise">this implementation</a>.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate (4th dimension)
     * @param u u coordinate (5th dimension)
     * @param seed long value that should completely change the noise if it changes even slightly
     * @return a continuous noise value between -1.0 and 1.0, both inclusive
     */
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final long seed) {
        double n0, n1, n2, n3, n4, n5;
        double t = (x + y + z + w + u) * F5;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        int h = fastFloor(u + t);
        t = (i + j + k + l + h) * G5;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double U0 = h - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;
        double u0 = u - U0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;

        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;

        if (w0 > u0) rankw++; else ranku++;

        int i1 = 3 - rankx >>> 31;
        int j1 = 3 - ranky >>> 31;
        int k1 = 3 - rankz >>> 31;
        int l1 = 3 - rankw >>> 31;
        int h1 = 3 - ranku >>> 31;

        int i2 = 2 - rankx >>> 31;
        int j2 = 2 - ranky >>> 31;
        int k2 = 2 - rankz >>> 31;
        int l2 = 2 - rankw >>> 31;
        int h2 = 2 - ranku >>> 31;

        int i3 = 1 - rankx >>> 31;
        int j3 = 1 - ranky >>> 31;
        int k3 = 1 - rankz >>> 31;
        int l3 = 1 - rankw >>> 31;
        int h3 = 1 - ranku >>> 31;

        int i4 = -rankx >>> 31;
        int j4 = -ranky >>> 31;
        int k4 = -rankz >>> 31;
        int l4 = -rankw >>> 31;
        int h4 = -ranku >>> 31;

        double x1 = x0 - i1 + G5;
        double y1 = y0 - j1 + G5;
        double z1 = z0 - k1 + G5;
        double w1 = w0 - l1 + G5;
        double u1 = u0 - h1 + G5;

        double x2 = x0 - i2 + 2 * G5;
        double y2 = y0 - j2 + 2 * G5;
        double z2 = z0 - k2 + 2 * G5;
        double w2 = w0 - l2 + 2 * G5;
        double u2 = u0 - h2 + 2 * G5;

        double x3 = x0 - i3 + 3 * G5;
        double y3 = y0 - j3 + 3 * G5;
        double z3 = z0 - k3 + 3 * G5;
        double w3 = w0 - l3 + 3 * G5;
        double u3 = u0 - h3 + 3 * G5;

        double x4 = x0 - i4 + 4 * G5;
        double y4 = y0 - j4 + 4 * G5;
        double z4 = z0 - k4 + 4 * G5;
        double w4 = w0 - l4 + 4 * G5;
        double u4 = u0 - h4 + 4 * G5;

        double x5 = x0 - 1 + 5 * G5;
        double y5 = y0 - 1 + 5 * G5;
        double z5 = z0 - 1 + 5 * G5;
        double w5 = w0 - 1 + 5 * G5;
        double u5 = u0 - 1 + 5 * G5;

        t = LIMIT5 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0;
        if (t < 0) n0 = 0;
        else
        {
            t *= t;
            n0 = t * t * (hashAll(i, j, k, l, h, seed) >> 10);
        }

        t = LIMIT5 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1;
        if (t < 0) n1 = 0;
        else
        {
            t *= t;
            n1 = t * t * (hashAll(i + i1, j + j1, k + k1, l + l1, h + h1, seed) >> 10);
        }

        t = LIMIT5 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2;
        if (t < 0) n2 = 0;
        else
        {
            t *= t;
            n2 = t * t * (hashAll(i + i2, j + j2, k + k2, l + l2, h + h2, seed) >> 10);
        }

        t = LIMIT5 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3;
        if (t < 0) n3 = 0;
        else
        {
            t *= t;
            n3 = t * t * (hashAll(i + i3, j + j3, k + k3, l + l3, h + h3, seed) >> 10);
        }

        t = LIMIT5 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4;
        if (t < 0) n4 = 0;
        else
        {
            t *= t;
            n4 = t * t * (hashAll(i + i4, j + j4, k + k4, l + l4, h + h4, seed) >> 10);
        }

        t = LIMIT5 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5;
        if (t < 0) n5 = 0;
        else
        {
            t *= t;
            n5 = t * t * (hashAll(i + 1, j + 1, k + 1, l + 1, h + 1, seed) >> 10);
        }

        return NumberTools.sin_((n0 + n1 + n2 + n3 + n4 + n5) * 0x1p-53);
    }
//        t = (n0 + n1 + n2 + n3 + n4 + n5) * 10.0;
//        if(t < -1.0) {
//            System.out.println(t);
//            return -1.0;
//        }
//        if(t > 1.0) {
//            System.out.println(t);
//            return 1.0;
//        }
//        return t;
    
    private final double[] mShared = {0, 0, 0, 0, 0, 0}, cellDistShared = {0, 0, 0, 0, 0, 0};
    private final int[] distOrderShared = {0, 0, 0, 0, 0, 0}, intLocShared = {0, 0, 0, 0, 0, 0};

    public double getNoiseWithSeed(final double x, final double y, final double z,
                               final double w, final double u, final double v, final long seed) {
        final double s = (x + y + z + w + u + v) * F6;

        final int skewX = fastFloor(x + s), skewY = fastFloor(y + s), skewZ = fastFloor(z + s),
                skewW = fastFloor(w + s), skewU = fastFloor(u + s), skewV = fastFloor(v + s);
        final double[] m = mShared, cellDist = cellDistShared;
        final int[] distOrder = distOrderShared,
                intLoc = intLocShared;
        intLoc[0] = skewX;
        intLoc[1] = skewY;
        intLoc[2] = skewZ;
        intLoc[3] = skewW;
        intLoc[4] = skewU;
        intLoc[5] = skewV;

        final double unskew = (skewX + skewY + skewZ + skewW + skewU + skewV) * G6;
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

        double n = 0;
        double skewOffset = 0;

        for (int c = -1; c < 6; c++) {
            if (c != -1) intLoc[distOrder[c]]++;

            m[0] = cellDist[0] - (intLoc[0] - skewX) + skewOffset;
            m[1] = cellDist[1] - (intLoc[1] - skewY) + skewOffset;
            m[2] = cellDist[2] - (intLoc[2] - skewZ) + skewOffset;
            m[3] = cellDist[3] - (intLoc[3] - skewW) + skewOffset;
            m[4] = cellDist[4] - (intLoc[4] - skewU) + skewOffset;
            m[5] = cellDist[5] - (intLoc[5] - skewV) + skewOffset;

            double tc = LIMIT6;

            for (int d = 0; d < 6; d++) {
                tc -= m[d] * m[d];
            }

            if (tc > 0) {
                tc *= tc;
                n += tc * tc * (hashAll(intLoc[0], intLoc[1], intLoc[2], intLoc[3], intLoc[4], intLoc[5], seed) >> 10);
            }
            skewOffset += G6;
        }
        return NumberTools.sin_(n * 0x1p-53);
    }
}
