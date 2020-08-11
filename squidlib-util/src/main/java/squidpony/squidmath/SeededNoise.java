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

import static squidpony.squidmath.HastyPointHash.hash256;
import static squidpony.squidmath.Noise.fastFloor;

/**
 * More advanced noise functions, in 2D, 3D, 4D, and 6D, with the last two as options for generating seamlessly-tiling
 * noise using {@link Noise#seamless2D(double[][], long, int, Noise.Noise4D)} and/or
 * {@link Noise#seamless3D(double[][][], long, int, Noise.Noise6D)}. All functions can take a long seed that should
 * significantly change the pattern of noise produced. Incorporates code from Joise; the full library is available at
 * https://github.com/SudoPlayGames/Joise , and this class adds rather significant optimization in a few methods,
 * especially 6D noise. Joise is derived from the Accidental Noise Library, available in C++ at
 * http://accidentalnoise.sourceforge.net/index.html . Both Joise and ANL have many features that SquidLib has not (yet)
 * incorporated, but now that SquidLib has seamless noise, that's a nice feature that would have needed Joise before.
 */
public class SeededNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {
    
    protected final long defaultSeed;
    public static final SeededNoise instance = new SeededNoise();

    public SeededNoise() {
        defaultSeed = 0x1337BEEF2A22L;
    }
    public SeededNoise(long seed)
    {
        defaultSeed = seed;
    }

    /**
     * Computes the hash for a 3D int point and its dot product with a 3D double point as one step.
     * @param seed
     * @param x
     * @param y
     * @param z
     * @param xd
     * @param yd
     * @param zd
     * @return a double between -1.2571 and 1.2571, exclusive
     */
    protected static double gradCoord3D(long seed, int x, int y, int z, double xd, double yd, double zd) {
        final int hash = HastyPointHash.hash32(x, y, z, seed) * 3;
        return xd * grad3d[hash] + yd * grad3d[hash + 1] + zd * grad3d[hash + 2];
    }
    protected static double gradCoord5D(long seed, int x, int y, int z, int w, int u, double xd, double yd, double zd, double wd, double ud) {
        final int hash = HastyPointHash.hash256(x, y, z, w, u, seed) * 5;
        return xd * grad5d[hash] + yd * grad5d[hash + 1] + zd * grad5d[hash + 2] + wd * grad5d[hash + 3] + ud * grad5d[hash + 4];
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
    public double getNoise(final double x, final double y, final double z, final double w, final double u) {
        return noise(x, y, z, w, u, defaultSeed);
    }
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return noise(x, y, z, w, u, v, defaultSeed);
    }

    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        return noise(x, y, seed);
    }
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        return noise(x, y, z, seed);
    }
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        return noise(x, y, z, w, seed);
    }
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final long seed) {
        return noise(x, y, z, w, u, seed);
    }
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final long seed) {
        return noise(x, y, z, w, u, v, seed);
    }
    
    /**
     * Used by {@link #noise(double, double, double, double, long)} to look up the vertices of the 4D triangle analogue.
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
    
    protected static final double F2 = 0.36602540378443864676372317075294,
            G2 = 0.21132486540518711774542560974902,
            F3 = 1.0 / 3.0,
            G3 = 1.0 / 6.0,
            F4 = (Math.sqrt(5.0) - 1.0) * 0.25,
            G4 = (5.0 - Math.sqrt(5.0)) * 0.05,
            LIMIT4 = 0.62,
            F5 = (Math.sqrt(6.0) - 1.0) / 5.0,
            G5 = (6.0 - Math.sqrt(6.0)) / 30.0,
            LIMIT5 = 0.7,
            F6 = (Math.sqrt(7.0) - 1.0) / 6.0,
            G6 = F6 / (1.0 + 6.0 * F6),
            LIMIT6 = 0.8375;

    public static double noise(final double x, final double y, final long seed) {
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
        final int
                gi0 = hash256(i, j, seed),
                gi1 = hash256(i + i1, j + j1, seed),
                gi2 = hash256(i + 1, j + 1, seed);
        // Calculate the contribution from the three corners for 2D gradient
        double t0 = 0.75 - x0 * x0 - y0 * y0;
        if (t0 > 0) {
            t0 *= t0;
            n += t0 * t0 * (grad2d[gi0][0] * x0 + grad2d[gi0][1] * y0);
        }
        double t1 = 0.75 - x1 * x1 - y1 * y1;
        if (t1 > 0) {
            t1 *= t1;
            n += t1 * t1 * (grad2d[gi1][0] * x1 + grad2d[gi1][1] * y1);
        }
        double t2 = 0.75 - x2 * x2 - y2 * y2;
        if (t2 > 0)  {
            t2 *= t2;
            n += t2 * t2 * (grad2d[gi2][0] * x2 + grad2d[gi2][1] * y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is clamped to return values in the interval [-1,1].
        return Math.max(-1.0, Math.min(1.0, 9.125f * n));

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

    public static double noise(final double x, final double y, final double z, final long seed) {
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
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 > 0) {
            t0 *= t0;
            n += t0 * t0 * gradCoord3D(seed, i, j, k, x0, y0, z0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 > 0) {
            t1 *= t1;
            n += t1 * t1 * gradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 > 0) {
            t2 *= t2;
            n += t2 * t2 * gradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 > 0) {
            t3 *= t3;
            n += t3 * t3 * gradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is clamped to stay just inside [-1,1]
        return Math.max(-1.0, Math.min(1.0, 31.5 * n));
        //return (32.0 * n) * 1.25086885 + 0.0003194984;
    }

    public static double noise(final double x, final double y, final double z, final double w, final long seed) {
        double n = 0.0;
        final double s = (x + y + z + w) * F4;
        final int i = fastFloor(x + s), j = fastFloor(y + s), k = fastFloor(z + s), l = fastFloor(w + s);
        final double[] gradient4DLUT = grad4d;
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
        final int h0 = (hash256(i, j, k, l, seed) & 0xFC),
                h1 = (hash256(i + i1, j + j1, k + k1, l + l1, seed) & 0xFC),
                h2 = (hash256(i + i2, j + j2, k + k2, l + l2, seed) & 0xFC),
                h3 = (hash256(i + i3, j + j3, k + k3, l + l3, seed) & 0xFC),
                h4 = (hash256(i + 1, j + 1, k + 1, l + 1, seed) & 0xFC);
        double t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            t0 *= t0;
            n += t0 * t0 * (x0 * gradient4DLUT[h0] + y0 * gradient4DLUT[h0 | 1] + z0 * gradient4DLUT[h0 | 2] + w0 * gradient4DLUT[h0 | 3]);
        }
        double t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            t1 *= t1;
            n += t1 * t1 * (x1 * gradient4DLUT[h1] + y1 * gradient4DLUT[h1 | 1] + z1 * gradient4DLUT[h1 | 2] + w1 * gradient4DLUT[h1 | 3]);
        }
        double t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            t2 *= t2;
            n += t2 * t2 * (x2 * gradient4DLUT[h2] + y2 * gradient4DLUT[h2 | 1] + z2 * gradient4DLUT[h2 | 2] + w2 * gradient4DLUT[h2 | 3]);
        }
        double t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            t3 *= t3;
            n += t3 * t3 * (x3 * gradient4DLUT[h3] + y3 * gradient4DLUT[h3 | 1] + z3 * gradient4DLUT[h3 | 2] + w3 * gradient4DLUT[h3 | 3]);
        }
        double t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            t4 *= t4;
            n += t4 * t4 * (x4 * gradient4DLUT[h4] + y4 * gradient4DLUT[h4 | 1] + z4 * gradient4DLUT[h4 | 2] + w4 * gradient4DLUT[h4 | 3]);
        }
        //return NumberTools.bounce(5.0 + 41.0 * n);
        return Math.max(-1.0, Math.min(1.0, 14.75 * n));
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
    public static double noise(final double x, final double y, final double z, final double w, final double u, final long seed) {
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
            n0 = t * t * gradCoord5D(seed, i, j, k, l, h, x0, y0, z0, w0, u0);
        }

        t = LIMIT5 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1;
        if (t < 0) n1 = 0;
        else
        {
            t *= t;
            n1 = t * t * gradCoord5D(seed, i + i1, j + j1, k + k1, l + l1, h + h1, x1, y1, z1, w1, u1);
        }

        t = LIMIT5 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2;
        if (t < 0) n2 = 0;
        else
        {
            t *= t;
            n2 = t * t * gradCoord5D(seed, i + i2, j + j2, k + k2, l + l2, h + h2, x2, y2, z2, w2, u2);
        }

        t = LIMIT5 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3;
        if (t < 0) n3 = 0;
        else
        {
            t *= t;
            n3 = t * t * gradCoord5D(seed, i + i3, j + j3, k + k3, l + l3, h + h3, x3, y3, z3, w3, u3);
        }

        t = LIMIT5 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4;
        if (t < 0) n4 = 0;
        else
        {
            t *= t;
            n4 = t * t * gradCoord5D(seed, i + i4, j + j4, k + k4, l + l4, h + h4, x4, y4, z4, w4, u4);
        }

        t = LIMIT5 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5;
        if (t < 0) n5 = 0;
        else
        {
            t *= t;
            n5 = t * t * gradCoord5D(seed, i + 1, j + 1, k + 1, l + 1, h + 1, x5, y5, z5, w5, u5);
        }

        return  (n0 + n1 + n2 + n3 + n4 + n5) * 10.0;
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
    
    private static final double[] mShared = {0, 0, 0, 0, 0, 0}, cellDistShared = {0, 0, 0, 0, 0, 0};
    private static final int[] distOrderShared = {0, 0, 0, 0, 0, 0}, intLocShared = {0, 0, 0, 0, 0, 0};

    public static double noise(final double x, final double y, final double z,
                               final double w, final double u, final double v, final long seed) {
        final double s = (x + y + z + w + u + v) * F6;

        final int skewX = fastFloor(x + s), skewY = fastFloor(y + s), skewZ = fastFloor(z + s),
                skewW = fastFloor(w + s), skewU = fastFloor(u + s), skewV = fastFloor(v + s);
        final double[] m = mShared, cellDist = cellDistShared, gradient6DLUT = SeededNoise.grad6d;
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
                final int h = hash256(intLoc[0], intLoc[1], intLoc[2], intLoc[3],
                        intLoc[4], intLoc[5], seed) * 6;
                final double gr = gradient6DLUT[h] * m[0] + gradient6DLUT[h + 1] * m[1]
                        + gradient6DLUT[h + 2] * m[2] + gradient6DLUT[h + 3] * m[3]
                        + gradient6DLUT[h + 4] * m[4] + gradient6DLUT[h + 5] * m[5];
                tc *= tc;
                n += gr * tc * tc;
            }
            skewOffset += G6;
        }
        return Math.max(-1.0, Math.min(1.0, 7.5f * n));
    }

    /**
     * 256 2-element gradient vectors formed from the cos and sin of increasing multiples of the inverse of phi, the
     * golden ratio, while also adding increasing multiples of 2/3 of the reciprocal of {@link Math#E}. This produces
     * a sequence with remarkably low overlap possible from nearby angles, distributed nicely around the unit circle.
     * For i from 1 to 256 inclusive, this gets the cosine and sine of an angle in radians of
     * {@code 0.61803398874989484820458683436563811772 * i + (i / (1.5 * 2.7182818284590452354))}. This is expected to
     * be accessed using an 8-bit int (depending on how you got your int, the normal way to get 8 bits would be
     * {@code index & 255}), but smaller numbers should also work down to about 4 bits (typically using
     * {@code index & 15}).
     */
    public static final double[][] grad2d = {
            {0.6499429579167653, 0.759982994187637},
            {-0.1551483029088119, 0.9878911904175052},
            {-0.8516180517334043, 0.5241628506120981},
            {-0.9518580082090311, -0.30653928330368374},
            {-0.38568876701087174, -0.9226289476282616},
            {0.4505066120763985, -0.8927730912586049},
            {0.9712959670388622, -0.23787421973396244},
            {0.8120673355833279, 0.5835637432865366},
            {0.08429892519436613, 0.9964405106232257},
            {-0.702488350003267, 0.7116952424385647},
            {-0.9974536374007479, -0.07131788861160528},
            {-0.5940875849508908, -0.804400361391775},
            {0.2252075529515288, -0.9743108118529653},
            {0.8868317111719171, -0.4620925405802277},
            {0.9275724981153959, 0.373643226540993},
            {0.3189067150428103, 0.9477861083074618},
            {-0.5130301507665112, 0.8583705868705491},
            {-0.9857873824221494, 0.1679977281313266},
            {-0.7683809836504446, -0.6399927061806058},
            {-0.013020236219374872, -0.9999152331316848},
            {0.7514561619680513, -0.6597830223946701},
            {0.9898275175279653, 0.14227257481477412},
            {0.5352066871710182, 0.8447211386057674},
            {-0.29411988281443646, 0.9557685360657266},
            {-0.9175289804081126, 0.39766892022290273},
            {-0.8985631161871687, -0.43884430750324743},
            {-0.2505005588110731, -0.968116454790094},
            {0.5729409678802212, -0.8195966369650838},
            {0.9952584535626074, -0.09726567026534665},
            {0.7207814785200723, 0.6931623620930514},
            {-0.05832476124070039, 0.998297662136006},
            {-0.7965970142012075, 0.6045107087270838},
            {-0.977160478114496, -0.21250270589112422},
            {-0.4736001288089817, -0.8807399831914728},
            {0.36153434093875386, -0.9323587937709286},
            {0.9435535266854258, -0.3312200813348966},
            {0.8649775992346886, 0.5018104750024599},
            {0.1808186720712497, 0.9835164502083277},
            {-0.6299339540895539, 0.7766487066139361},
            {-0.9996609468975833, 0.02603826506945166},
            {-0.6695112313914258, -0.7428019325774111},
            {0.12937272671950842, -0.9915960354807594},
            {0.8376810167470904, -0.5461597881403947},
            {0.959517028911149, 0.28165061908243916},
            {0.4095816551369482, 0.9122734610714476},
            {-0.42710760401484793, 0.9042008043530463},
            {-0.9647728141412515, 0.2630844295924223},
            {-0.8269869890664444, -0.562221059650754},
            {-0.11021592552380209, -0.9939076666174438},
            {0.6837188597775012, -0.72974551782423},
            {0.998972441738333, 0.04532174585508431},
            {0.6148313475439905, 0.7886586169422362},
            {-0.1997618324529528, 0.9798444827088829},
            {-0.8744989400706802, 0.48502742583822706},
            {-0.9369870231562731, -0.3493641630687752},
            {-0.3434772946489506, -0.9391609809082988},
            {0.4905057254335028, -0.8714379687143274},
            {0.9810787787756657, -0.1936089611460388},
            {0.7847847614201463, 0.6197684069414349},
            {0.03905187955516296, 0.9992371844077906},
            {-0.7340217731995672, 0.6791259356474049},
            {-0.9931964444524306, -0.1164509455824639},
            {-0.5570202966000876, -0.830498879695542},
            {0.2691336060685578, -0.9631028512493016},
            {0.9068632806061, -0.4214249521425399},
            {0.9096851999779008, 0.4152984913783901},
            {0.27562369868737335, 0.9612656119522284},
            {-0.5514058359842319, 0.8342371389734039},
            {-0.9923883787916933, 0.12314749546456379},
            {-0.7385858406439617, -0.6741594440488484},
            {0.032311046904542805, -0.9994778618098213},
            {0.7805865154410089, -0.6250477517051506},
            {0.9823623706068018, 0.18698709264487903},
            {0.49637249435561115, 0.8681096398768929},
            {-0.3371347561867868, 0.9414564016304079},
            {-0.9346092156607797, 0.35567627697379833},
            {-0.877750600058892, -0.47911781859606817},
            {-0.20636642697019966, -0.9784747813917093},
            {0.6094977881394418, -0.7927877687333024},
            {0.998644017504346, -0.052058873429796634},
            {0.6886255051458764, 0.7251171723677399},
            {-0.10350942208147358, 0.9946284731196666},
            {-0.8231759450656516, 0.567786371327519},
            {-0.9665253951623188, -0.2565709658288005},
            {-0.43319680340129196, -0.9012993562201753},
            {0.4034189716368784, -0.9150153732716426},
            {0.9575954428121146, -0.28811624026678895},
            {0.8413458575409575, 0.5404971304259356},
            {0.13605818775026976, 0.9907008476558967},
            {-0.664485735550556, 0.7473009482463117},
            {-0.999813836664718, -0.01929487014147803},
            {-0.6351581891853917, -0.7723820781910558},
            {0.17418065221630152, -0.984713714941304},
            {0.8615731658120597, -0.5076334109892543},
            {0.945766171482902, 0.32484819358982736},
            {0.3678149601703667, 0.9298990026206456},
            {-0.4676486851245607, 0.883914423064399},
            {-0.9757048995218635, 0.2190889067228882},
            {-0.8006563717736747, -0.5991238388999518},
            {-0.06505704156910719, -0.9978815467490495},
            {0.716089639712196, -0.6980083293893113},
            {0.9958918787052943, 0.09055035024139549},
            {0.5784561871098056, 0.8157134543418942},
            {-0.24396482815448167, 0.9697840804135497},
            {-0.8955826311865743, 0.4448952131872543},
            {-0.9201904205900768, -0.39147105876968413},
            {-0.3005599364234082, -0.9537629289384008},
            {0.5294967923694863, -0.84831193960148},
            {0.9888453593035162, -0.1489458135829932},
            {0.7558893631265085, 0.6546993743025888},
            {-0.006275422246980369, 0.9999803093439501},
            {-0.764046696121276, 0.6451609459244744},
            {-0.9868981170802014, -0.16134468229090512},
            {-0.5188082666339063, -0.8548906260290385},
            {0.31250655826478446, -0.9499156020623616},
            {0.9250311403279032, -0.3798912863223621},
            {0.889928392754896, 0.45610026942404636},
            {0.2317742435145519, 0.9727696027545563},
            {-0.5886483179573486, 0.8083892365475831},
            {-0.996949901406418, 0.0780441803450664},
            {-0.707272817672466, -0.7069407057042696},
            {0.07757592706207364, -0.9969864470194466},
            {0.8081126726681943, -0.5890279350532263},
            {0.9728783545459001, 0.23131733021125322},
            {0.4565181982253288, 0.8897140746830408},
            {-0.3794567783511009, 0.9252094645881026},
            {-0.9497687200714887, 0.31295267753091066},
            {-0.8551342041690687, -0.5184066867432686},
            {-0.16180818807538452, -0.9868222283024238},
            {0.6448020194233159, -0.7643496292585048},
            {0.9999772516247822, -0.006745089543285545},
            {0.6550543261176665, 0.7555817823601425},
            {-0.14848135899860646, 0.9889152066936411},
            {-0.848063153443784, 0.5298951667745091},
            {-0.9539039899003245, -0.300111942535184},
            {-0.3919032080850608, -0.9200064540494471},
            {0.44447452934057863, -0.8957914895596358},
            {0.9696693887216105, -0.24442028675267172},
            {0.8159850520735595, 0.5780730012658526},
            {0.0910180879994953, 0.9958492394217692},
            {-0.6976719213969089, 0.7164173993520435},
            {-0.9979119924958648, -0.06458835214597858},
            {-0.5994998228898376, -0.8003748886334786},
            {0.2186306161766729, -0.9758076929755208},
            {0.8836946816279001, -0.46806378802740584},
            {0.9300716543684309, 0.36737816720699407},
            {0.32529236260160294, 0.9456134933645286},
            {-0.5072286936943775, 0.8618114946396893},
            {-0.9846317976415725, 0.17464313062106204},
            {-0.7726803123417516, -0.6347953488483143},
            {-0.019764457813331488, -0.9998046640256011},
            {0.7469887719961158, -0.6648366525032559},
            {0.9907646418168752, 0.13559286310672486},
            {0.5408922318074902, 0.8410919055432124},
            {-0.2876664477065717, 0.9577306588304888},
            {-0.9148257956391065, 0.40384868903250853},
            {-0.9015027194859215, -0.4327734358292892},
            {-0.2570248925062563, -0.9664047830139022},
            {0.5673996816983953, -0.8234425306046317},
            {0.9945797473944409, -0.10397656501736473},
            {0.7254405241129018, 0.6882848581617921},
            {-0.05158982732517303, 0.9986683582233687},
            {-0.7925014140531963, 0.609870075281354},
            {-0.9785715990807187, -0.20590683687679034},
            {-0.47953002522651733, -0.8775254725113429},
            {0.35523727306945746, -0.9347761656258549},
            {0.9412979532686209, -0.33757689964259285},
            {0.868342678987353, 0.4959647082697184},
            {0.18744846526420056, 0.9822744386728669},
            {-0.6246810590458048, 0.7808800000444446},
            {-0.9994625758058275, 0.03278047534097766},
            {-0.674506266646887, -0.738269121834361},
            {0.12268137965007223, -0.9924461089082646},
            {0.8339780641890598, -0.5517975973592748},
            {0.9613949601033843, 0.2751721837101493},
            {0.41572570400265835, 0.9094900433932711},
            {-0.42099897262033487, 0.907061114287578},
            {-0.9629763390922247, 0.2695859238694348},
            {-0.8307604078465821, -0.5566301687427484},
            {-0.11691741449967302, -0.9931416405461567},
            {0.6787811074228051, -0.7343406622310046},
            {0.999255415972447, 0.03858255628819732},
            {0.6201369341201711, 0.7844935837468874},
            {-0.19314814942146824, 0.9811696042861612},
            {-0.8712074932224428, 0.4909149659086258},
            {-0.9393222007870077, -0.34303615422962713},
            {-0.3498042060103595, -0.9368228314134226},
            {0.4846166400948296, -0.8747266499559725},
            {0.9797505510481769, -0.20022202106859724},
            {0.7889473022428521, 0.6144608647291752},
            {0.045790935472179155, 0.9989510449609544},
            {-0.7294243101497431, 0.684061529222753},
            {-0.9939593229024027, -0.10974909756074072},
            {-0.562609414602539, -0.8267228354174018},
            {0.26263126874523307, -0.9648962724963078},
            {0.9040001019019392, -0.4275322394408211},
            {0.9124657316291773, 0.4091531358824348},
            {0.28210125132356934, 0.9593846381935018},
            {-0.5457662881946498, 0.8379374431723614},
            {-0.9915351626845509, 0.12983844253579577},
            {-0.7431163048326799, -0.6691622803863227},
            {0.02556874420628532, -0.9996730662170076},
            {0.7763527553119807, -0.6302986588273021},
            {0.9836012681423212, 0.1803567168386515},
            {0.5022166799422209, 0.8647418148718223},
            {-0.330776879188771, 0.9437089891455613},
            {-0.9321888864830543, 0.3619722087639923},
            {-0.8809623252471085, -0.47318641305008735},
            {-0.21296163248563432, -0.9770605626515961},
            {0.604136498566135, -0.7968808512571063},
            {0.9982701582127194, -0.05879363249495786},
            {0.6935008202914851, 0.7204558364362367},
            {-0.09679820929680796, 0.9953040272584711},
            {-0.8193274492343137, 0.5733258505694586},
            {-0.9682340024187017, -0.25004582891994304},
            {-0.4392662937408502, -0.8983569018954422},
            {0.39723793388455464, -0.9177156552457467},
            {0.9556302892322005, -0.2945687530984589},
            {0.8449724198323217, 0.5348098818484104},
            {0.14273745857559722, 0.9897605861618151},
            {-0.6594300077680133, 0.7517659641504648},
            {-0.9999212381512442, -0.01255059735959867},
            {-0.6403535266476091, -0.768080308893523},
            {0.16753470770767478, -0.9858661784001437},
            {0.8581295336101056, -0.5134332513054668},
            {0.9479357869928937, 0.31846152630759517},
            {0.37407884501651706, 0.9273969040875156},
            {-0.461675964944643, 0.8870486477034012},
            {-0.9742049295269273, 0.22566513972130173},
            {-0.8046793020829978, -0.5937097108850584},
            {-0.07178636201352963, -0.9974200309943962},
            {0.7113652211526822, -0.7028225395748172},
            {0.9964799940037152, 0.08383091047075403},
            {0.5839450884626246, 0.8117931594072332},
            {-0.23741799789097484, 0.9714075840127259},
            {-0.8925614000865144, 0.45092587758477687},
            {-0.9228099950981292, -0.38525538665538556},
            {-0.30698631553196837, -0.95171392869712},
            {0.5237628071845146, -0.8518641451605984},
            {0.9878182118285335, -0.15561227580071732},
            {0.7602881737752754, 0.6495859395164404},
            {4.6967723669845613E-4, 0.9999998897016406},
            {-0.7596776469502666, 0.6502998329417794},
            {-0.9879639510809196, -0.15468429579171308},
            {-0.5245627784110601, -0.8513717704420726},
            {0.3060921834538644, -0.9520018777441807},
            {0.9224476966294768, -0.3861220622846781},
            {0.8929845854878761, 0.45008724718774934},
            {0.23833038910266038, 0.9711841358002995},
            {-0.5831822693781987, 0.8123413326200348},
            {-0.9964008074312266, 0.0847669213219385},
            {-0.712025106726807, -0.7021540054650968},
            {0.07084939947717452, -0.9974870237721009},
            {0.8041212432524677, -0.5944653279629567},
            {0.9744164792492415, 0.22474991650168097},
            {0.462509014279733, 0.8866145790082576},
    };
    // these are the exact vertex positions, before scaling has been applied
//            {
//            {-0.324919696232904f,  0.850650808352036f,  0.000000000000001f },
//            { 0.000000000000001f,  0.850650808352035f,  0.525731112119131f },
//            { 0.324919696232906f,  0.850650808352036f,  0.000000000000001f },
//            { 0.000000000000001f,  0.850650808352036f, -0.525731112119131f },
//            {-0.525731112119131f,  0.525731112119132f, -0.525731112119130f },
//            {-0.850650808352035f,  0.525731112119132f,  0.000000000000001f },
//            {-0.525731112119130f,  0.525731112119131f,  0.525731112119132f },
//            { 0.525731112119132f,  0.525731112119131f,  0.525731112119131f },
//            { 0.850650808352036f,  0.525731112119132f,  0.000000000000000f },
//            { 0.525731112119132f,  0.525731112119132f, -0.525731112119131f },
//            {-0.525731112119132f,  0.000000000000002f, -0.850650808352036f },
//            {-0.850650808352036f,  0.000000000000002f, -0.324919696232905f },
//            { 0.000000000000000f,  0.324919696232906f, -0.850650808352037f },
//            {-0.525731112119131f,  0.000000000000001f,  0.850650808352037f },
//            { 0.000000000000001f,  0.324919696232905f,  0.850650808352037f },
//            {-0.850650808352037f,  0.000000000000001f,  0.324919696232905f },
//            { 0.525731112119133f,  0.000000000000001f,  0.850650808352036f },
//            { 0.850650808352037f,  0.000000000000001f,  0.324919696232905f },
//            { 0.525731112119132f,  0.000000000000001f, -0.850650808352038f },
//            { 0.850650808352038f,  0.000000000000001f, -0.324919696232906f },
//            {-0.525731112119134f, -0.525731112119130f, -0.525731112119133f },
//            {-0.850650808352038f, -0.525731112119130f, -0.000000000000001f },
//            {-0.000000000000001f, -0.324919696232905f, -0.850650808352038f },
//            {-0.000000000000001f, -0.324919696232905f,  0.850650808352038f },
//            {-0.525731112119132f, -0.525731112119131f,  0.525731112119133f },
//            { 0.525731112119133f, -0.525731112119131f,  0.525731112119134f },
//            { 0.850650808352039f, -0.525731112119130f,  0.000000000000001f },
//            { 0.525731112119132f, -0.525731112119134f, -0.525731112119133f },
//            {-0.000000000000003f, -0.850650808352038f, -0.525731112119134f },
//            {-0.324919696232908f, -0.850650808352038f, -0.000000000000002f },
//            {-0.000000000000002f, -0.850650808352042f,  0.525731112119130f },
//            { 0.324919696232902f, -0.850650808352041f,  0.000000000000002f }
//    };
    protected static final double[] grad3d =
            {
                    -0.448549002408981,  1.174316525459290,  0.000000000000001,
                    0.000000000000001,  1.069324374198914,  0.660878777503967,
                    0.448549002408981,  1.174316525459290,  0.000000000000001,
                    0.000000000000001,  1.069324374198914, -0.660878777503967,
                    -0.725767493247986,  0.725767493247986, -0.725767493247986,
                    -1.069324374198914,  0.660878777503967,  0.000000000000001,
                    -0.725767493247986,  0.725767493247986,  0.725767493247986,
                    0.725767493247986,  0.725767493247986,  0.725767493247986,
                    1.069324374198914,  0.660878777503967,  0.000000000000000,
                    0.725767493247986,  0.725767493247986, -0.725767493247986,
                    -0.660878777503967,  0.000000000000003, -1.069324374198914,
                    -1.174316525459290,  0.000000000000003, -0.448549002408981,
                    0.000000000000000,  0.448549002408981, -1.174316525459290,
                    -0.660878777503967,  0.000000000000001,  1.069324374198914,
                    0.000000000000001,  0.448549002408981,  1.174316525459290,
                    -1.174316525459290,  0.000000000000001,  0.448549002408981,
                    0.660878777503967,  0.000000000000001,  1.069324374198914,
                    1.174316525459290,  0.000000000000001,  0.448549002408981,
                    0.660878777503967,  0.000000000000001, -1.069324374198914,
                    1.174316525459290,  0.000000000000001, -0.448549002408981,
                    -0.725767493247986, -0.725767493247986, -0.725767493247986,
                    -1.069324374198914, -0.660878777503967, -0.000000000000001,
                    -0.000000000000001, -0.448549002408981, -1.174316525459290,
                    -0.000000000000001, -0.448549002408981,  1.174316525459290,
                    -0.725767493247986, -0.725767493247986,  0.725767493247986,
                    0.725767493247986, -0.725767493247986,  0.725767493247986,
                    1.069324374198914, -0.660878777503967,  0.000000000000001,
                    0.725767493247986, -0.725767493247986, -0.725767493247986,
                    -0.000000000000004, -1.069324374198914, -0.660878777503967,
                    -0.448549002408981, -1.174316525459290, -0.000000000000003,
                    -0.000000000000003, -1.069324374198914,  0.660878777503967,
                    0.448549002408981, -1.174316525459290,  0.000000000000003,
            };
    protected static final double[] grad4d =
            {
                    -0.5875167, 1.4183908, 1.4183908, 1.4183908,
                    -0.5875167, 1.4183908, 1.4183908, -1.4183908,
                    -0.5875167, 1.4183908, -1.4183908, 1.4183908,
                    -0.5875167, 1.4183908, -1.4183908, -1.4183908,
                    -0.5875167, -1.4183908, 1.4183908, 1.4183908,
                    -0.5875167, -1.4183908, 1.4183908, -1.4183908,
                    -0.5875167, -1.4183908, -1.4183908, 1.4183908,
                    -0.5875167, -1.4183908, -1.4183908, -1.4183908,
                    1.4183908, -0.5875167, 1.4183908, 1.4183908,
                    1.4183908, -0.5875167, 1.4183908, -1.4183908,
                    1.4183908, -0.5875167, -1.4183908, 1.4183908,
                    1.4183908, -0.5875167, -1.4183908, -1.4183908,
                    -1.4183908, -0.5875167, 1.4183908, 1.4183908,
                    -1.4183908, -0.5875167, 1.4183908, -1.4183908,
                    -1.4183908, -0.5875167, -1.4183908, 1.4183908,
                    -1.4183908, -0.5875167, -1.4183908, -1.4183908,
                    1.4183908, 1.4183908, -0.5875167, 1.4183908,
                    1.4183908, 1.4183908, -0.5875167, -1.4183908,
                    1.4183908, -1.4183908, -0.5875167, 1.4183908,
                    1.4183908, -1.4183908, -0.5875167, -1.4183908,
                    -1.4183908, 1.4183908, -0.5875167, 1.4183908,
                    -1.4183908, 1.4183908, -0.5875167, -1.4183908,
                    -1.4183908, -1.4183908, -0.5875167, 1.4183908,
                    -1.4183908, -1.4183908, -0.5875167, -1.4183908,
                    1.4183908, 1.4183908, 1.4183908, -0.5875167,
                    1.4183908, 1.4183908, -1.4183908, -0.5875167,
                    1.4183908, -1.4183908, 1.4183908, -0.5875167,
                    1.4183908, -1.4183908, -1.4183908, -0.5875167,
                    -1.4183908, 1.4183908, 1.4183908, -0.5875167,
                    -1.4183908, 1.4183908, -1.4183908, -0.5875167,
                    -1.4183908, -1.4183908, 1.4183908, -0.5875167,
                    -1.4183908, -1.4183908, -1.4183908, -0.5875167,
                    0.5875167, 1.4183908, 1.4183908, 1.4183908,
                    0.5875167, 1.4183908, 1.4183908, -1.4183908,
                    0.5875167, 1.4183908, -1.4183908, 1.4183908,
                    0.5875167, 1.4183908, -1.4183908, -1.4183908,
                    0.5875167, -1.4183908, 1.4183908, 1.4183908,
                    0.5875167, -1.4183908, 1.4183908, -1.4183908,
                    0.5875167, -1.4183908, -1.4183908, 1.4183908,
                    0.5875167, -1.4183908, -1.4183908, -1.4183908,
                    1.4183908, 0.5875167, 1.4183908, 1.4183908,
                    1.4183908, 0.5875167, 1.4183908, -1.4183908,
                    1.4183908, 0.5875167, -1.4183908, 1.4183908,
                    1.4183908, 0.5875167, -1.4183908, -1.4183908,
                    -1.4183908, 0.5875167, 1.4183908, 1.4183908,
                    -1.4183908, 0.5875167, 1.4183908, -1.4183908,
                    -1.4183908, 0.5875167, -1.4183908, 1.4183908,
                    -1.4183908, 0.5875167, -1.4183908, -1.4183908,
                    1.4183908, 1.4183908, 0.5875167, 1.4183908,
                    1.4183908, 1.4183908, 0.5875167, -1.4183908,
                    1.4183908, -1.4183908, 0.5875167, 1.4183908,
                    1.4183908, -1.4183908, 0.5875167, -1.4183908,
                    -1.4183908, 1.4183908, 0.5875167, 1.4183908,
                    -1.4183908, 1.4183908, 0.5875167, -1.4183908,
                    -1.4183908, -1.4183908, 0.5875167, 1.4183908,
                    -1.4183908, -1.4183908, 0.5875167, -1.4183908,
                    1.4183908, 1.4183908, 1.4183908, 0.5875167,
                    1.4183908, 1.4183908, -1.4183908, 0.5875167,
                    1.4183908, -1.4183908, 1.4183908, 0.5875167,
                    1.4183908, -1.4183908, -1.4183908, 0.5875167,
                    -1.4183908, 1.4183908, 1.4183908, 0.5875167,
                    -1.4183908, 1.4183908, -1.4183908, 0.5875167,
                    -1.4183908, -1.4183908, 1.4183908, 0.5875167,
                    -1.4183908, -1.4183908, -1.4183908, 0.5875167,
            };
    /**
     * This gradient vector array was pseudo-randomly generated after a lot of rejection sampling. Each gradient should
     * have a magnitude of 2.0, matching the magnitude of the center of an edge of a 5D hypercube.
     * This may bias slightly in some directions. The sums of the x, y, z, w, and u components of all 256 vectors are:
     * <br>
     * x: +0.66677894631043, y: -0.67457046130430, z: -0.86203306900659, w: +0.63575181967243, u: +0.47384803638072
     * <br>
     * Most of the rejected sets of 256 vectors had some component sums at 10 or more, so having all of these close to
     * 0.0 is good.
     */
    protected static final double[] grad5d = {
            -0.8135037701, -1.4093324762, +0.4981674194, +1.0221341212, -0.2430321956,
            -1.0472578593, +1.5035195198, -0.3859795921, -0.6042103597, +0.3586497251,
            +1.2588681037, -1.1621986474, -0.8123094051, +0.5944339059, +0.2265991249,
            -0.2960358350, +0.0170047461, -1.8165726173, -0.4794051151, +0.6183108318,
            -0.3365697234, +1.0025399140, -0.3342898515, -1.2201787939, -1.1318341523,
            -1.1798657213, +0.3878857844, -0.0047051864, -0.5091701000, -1.4826277916,
            -0.0489109302, +1.2981950452, -0.7649713084, -0.3691832721, -1.2612771125,
            -0.0612615129, +1.4820797101, -0.1072664451, +1.2446745792, -0.4888413461,
            -0.6621120988, +0.1384623861, -0.5196699469, -0.4864615611, +1.7423357977,
            -0.5783656744, +0.6783668545, -0.7055237483, -1.3714609220, -0.9091989537,
            +0.7351444842, +0.7923092131, +0.1905953342, +1.1881160783, +1.1763767684,
            +0.7692669119, -0.5722145680, -1.6379784624, -0.2688003492, +0.5705890250,
            +0.0796703277, +0.5758477138, -0.6676893639, -0.3441795362, -1.7600521042,
            +0.0202501603, +0.7104597787, -0.8290042697, +1.6662766142, -0.1763831049,
            -0.3263675211, -0.4703927051, +1.1022210297, +1.3426936983, -0.8090101225,
            -1.2955279282, -1.0884490171, -0.4371549231, -0.6865473487, -0.6887920119,
            +0.5157780595, +0.7680918222, -1.1694371005, +1.2256868318, -0.5235614617,
            +1.4466690387, -0.6576881996, -1.0596724686, -0.2693364586, +0.5283436911,
            +0.1232897549, +0.0877975959, -1.1404266324, +0.1884320885, +1.6251189687,
            -0.8555904107, +1.4296013143, -0.8463954489, -0.5503002325, +0.4527576939,
            +0.0234155836, -0.9837219063, -0.6821029381, -0.8476569318, +1.3593955382,
            +1.7892428919, +0.5220227406, +0.0110223541, +0.7207562621, +0.0805670558,
            -0.4293868153, +1.0344832480, -1.2893123840, +1.0403632804, +0.0280925932,
            +0.7916575492, +0.0033153855, -1.0930829739, +1.0304678656, -1.0566801424,
            +0.6760136637, -0.3493488323, -1.2095929483, +0.8896749291, +1.0799649717,
            +1.1140270928, -0.4273503201, +0.9011244868, +0.5287852709, -1.2184728709,
            -0.6455453736, +0.2148296014, +0.1113957520, -1.4486139903, +1.1942478413,
            -0.7885750939, +1.4996205936, +0.6310893695, +0.5934480559, +0.6154941181,
            +0.7293084741, +1.2427236824, +0.8400151653, +0.7745575708, +0.7862455662,
            +1.0321795514, +0.1997656305, +0.5800043270, -1.5979448440, +0.0697590333,
            +0.6972026170, +1.6829318840, +0.5275668404, -0.2871990368, -0.5664262766,
            +1.5776132644, -0.4511467598, +1.0432759097, -0.4633993608, -0.0666288052,
            +0.1097217287, +0.2476558079, -0.4818135273, -1.5250099293, -1.1699693088,
            -1.3816067831, +0.3764648739, -0.4118151086, +0.9033974500, -0.9816915296,
            +1.1883662133, -0.8073547121, +1.3034256479, -0.2213720052, +0.4336359389,
            +1.3667617011, -0.3219864189, -1.1046135188, +0.1341811806, -0.8888822101,
            +0.8429523414, +1.4333214898, +0.8005151149, +0.7696009955, +0.0437117320,
            -0.4629311478, +0.3129604106, +0.5959353074, -0.0440924216, -1.8250116442,
            +0.9931164017, -1.1621808031, -1.1919233319, +0.2114379458, -0.4445990997,
            -0.4694361590, -0.0720812980, -1.0504569965, +0.2412132219, +1.6164127750,
            +0.2255783496, -0.2656159811, +0.1836243267, +1.0247734750, -1.6717308367,
            -1.1249246028, -1.5370863231, -0.1225300230, +0.1846111151, -0.5681684636,
            +1.7969542831, -0.2398689157, +0.2637113045, +0.1468158723, +0.7888723947,
            +1.4759978631, -0.5282519730, +0.9824708882, -0.6592134457, +0.3775827700,
            +0.7822637006, +0.3953055238, -1.7940565963, +0.0934530762, +0.0665168960,
            -0.1895305589, +0.8138331320, +0.1133728725, +0.8730224243, +1.5895698283,
            +0.3659882348, -0.1532017028, -1.3927442803, -1.1285714875, +0.7932033899,
            +1.3994232629, -1.3493129158, -0.2458528093, +0.1300296689, +0.3789694814,
            +0.7205921486, -0.0063786412, +0.1639523403, -1.7029676103, +0.7441284948,
            +1.4394387723, -1.1109356044, -0.0084571960, -0.7562201284, +0.3491385069,
            -1.3728884000, +0.6803626642, -0.2367573292, -0.9912534233, -0.7833560520,
            +0.6660160012, -0.5734084579, -1.1364022383, +0.5303648945, +1.2864402271,
            -1.1118911670, +0.7518442270, -0.6193249159, -0.6971011180, +1.1527857437,
            +0.4697132699, +0.1800452980, -0.7519527299, +0.8922366145, -1.5444850438,
            +0.9129657523, -1.3462955184, +0.8962598490, -0.5850859544, +0.4564806911,
            +0.4353207304, -0.8947986429, +1.0223003929, -0.2880732642, -1.3717678198,
            -1.3570198893, -1.2149228730, +0.4864509822, -0.6542550666, -0.1333236003,
            -0.8429268378, +1.5487100476, +0.6728404933, +0.5411148811, +0.3813815554,
            +1.1542818894, +0.5161191350, -0.0106170109, -1.5482653183, -0.0633730362,
            -0.3028841894, +0.5637115661, -0.6357795737, +1.6484778594, +0.6846864382,
            -0.7554941457, -0.3792273201, -0.9524406706, -1.3427742039, +0.7584388188,
            -0.6516571437, -0.8096739074, +1.2403868463, -0.9420548462, +0.7026693882,
            +1.1282889166, -0.2046286773, -0.5585557914, +0.1515500267, +1.5330163869,
            -1.8754512728, -0.2443753413, +0.6267313678, +0.1501249582, -0.0873699336,
            -0.7954420378, -0.0011730104, +1.2243206689, +1.3461079784, +0.2372821079,
            -0.2416801963, -0.0096300449, +1.2563518812, +0.0597570611, -1.5360686799,
            +1.0551343881, -0.8474288199, +0.6453925651, -1.2404048956, +0.4619739711,
            -0.7100307233, -1.5954783091, -0.8002979289, +0.2605585040, -0.4918717597,
            -0.6468051663, +1.4624421482, -0.1024406877, -1.1968226377, -0.0052458877,
            -0.0559435798, -1.0845699098, +0.3345624715, -1.2829424922, +1.0308758126,
            +0.2693015563, +0.6548401802, +1.1578288561, -0.2511868960, +1.4474109622,
            +0.5639618509, -1.2979663293, -0.0869327731, +0.9603401159, +1.0331601974,
            +1.7368208447, +0.0640245222, +0.8669874340, -0.2677451350, -0.3949677783,
            -0.0944818319, -0.7689572828, +0.0969776328, -0.3967748508, -1.7980386362,
            -1.0081008566, +0.1401741032, +1.5948365089, +0.1046299384, +0.6400257568,
            +0.7906457652, -0.8588089302, +1.3480070219, -0.5822062372, +0.6937142507,
            -1.5838247464, +0.4117180083, +1.1093312660, +0.1203095141, +0.2773034754,
            +1.1485752079, -0.3057436499, +1.0038566858, +0.2569800630, -1.2302555889,
            -0.4643362753, +0.0527486340, -0.5267816051, -1.7479894609, +0.6698084729,
            +0.6511653464, +0.1627163528, -0.6243065129, -0.8848824566, -1.5416651699,
            +0.2478327185, -1.6090011830, +0.2855396793, -0.7650677400, +0.8263368451,
            -0.4679093746, +0.3517391490, +1.6504647049, +0.2121612740, +0.9424936285,
            +1.1524992600, +0.4245453907, +0.4176501076, +0.9808398701, +1.1640567871,
            -0.6634415859, +1.6550211652, +0.7874326604, +0.4415148931, +0.0759250242,
            -0.0412327914, -0.3505368928, +0.8558498721, -0.5848297084, +1.6735946201,
            +0.3547189382, -1.7184881162, +0.5007184287, +0.4490445315, -0.6845532363,
            -0.3834315751, +0.3428730656, -0.3351234278, -1.7627873621, +0.7181164893,
            -1.0454290166, -0.5639739397, -1.3417921576, -0.8849215875, -0.0742910254,
            +1.1680066160, -0.3827933313, -1.4825659627, -0.4967358337, -0.2109063492,
            -1.5454371752, -1.0726924123, -0.2517537762, +0.2452762614, -0.5808739259,
            +0.5836685680, +0.7000533029, -0.7779803099, -1.5848776079, -0.2283987327,
            -0.7096420818, +1.5584823825, -0.7594797447, -0.6018790930, +0.3584311009,
            +1.0114275797, -0.6676161775, -0.6188288462, +0.2346621257, +1.4468197664,
            -0.6736922510, -0.4841886117, -1.5995845157, +0.5783311944, +0.6469640995,
            -0.6149414658, +0.7811060917, -1.6622776308, +0.4457867877, -0.2232207099,
            +0.9733737437, -0.4277073646, +1.2689422722, +0.0669243740, -1.1202306027,
            +0.1285525134, +1.3602300828, +0.7937181153, +0.4154354789, -1.1535481305,
            +1.1258201069, +0.7265045253, +0.7727828593, +1.2638082317, -0.1015660740,
            +0.2576028495, +0.4466047871, -1.6315521044, +0.1664361243, -1.0220184359,
            -1.2637043813, -0.2854498187, +0.7746532753, +1.1819207514, +0.5696887555,
            -1.7722327289, -0.3228176294, -0.4590453215, -0.1981963132, -0.7106163149,
            +0.4246192449, -0.8445679528, +0.2395992643, -1.1808116599, +1.2863434558,
            -0.1905644446, +1.0893507679, +1.2445824260, -1.0623776423, +0.3152275795,
            +0.0918870228, -1.3938039147, -0.7082701148, -0.9973240838, +0.7433475223,
            -0.9802998477, -1.2974708212, -0.6676406432, +0.8408168026, +0.4504050984,
            -0.4437627839, -1.4265839857, -1.3233439994, -0.1067615287, +0.0727692141,
            -0.7368457934, +1.4234177323, -1.1363011787, -0.2584157530, -0.2701502654,
            +1.2818557990, +0.5352751228, -0.1330287798, -0.2587493132, -1.4091410114,
            -0.5750307464, -0.8488049316, +1.1038871297, +1.2590686262, +0.3808532896,
            -1.0836557104, +0.9700975741, +1.3697765418, +0.0254178194, -0.0875623095,
            -0.4609184892, +1.0631869587, +1.1152084273, -0.1590172959, -1.1782237905,
            +1.3780658247, +0.1221620032, -0.8855727725, -1.1375752240, +0.0877183100,
            +0.2169356842, -0.7257425078, +0.5277865751, +1.7387476451, -0.3527530000,
            +0.5797869767, -0.4024220452, -1.5338493739, +0.5186057185, +0.9382205317,
            +1.3846709393, -0.0923161271, -0.9242707797, +1.0781388520, -0.2398004677,
            +1.5761774632, -0.7200510919, +0.7882079724, +0.5982383931, -0.1342760095,
            -0.0042997054, -1.1394100050, -0.7785351130, +0.7081662713, +1.2625806758,
            +0.2997533240, -1.4170869215, -0.5788158053, +0.3519792904, -1.2012890761,
            -0.3398151012, +0.5791467632, +1.5781017121, +0.4058255292, +0.9455238495,
            +1.1105596738, +0.7447802839, +0.6355979079, -1.2596018785, -0.4705081791,
            +0.0974575499, +0.2372195493, -1.8826219023, +0.0288000719, +0.6238062522,
            -0.1670458275, +1.3998296887, -1.0693753008, +0.6130269221, +0.7022869733,
            -1.5897952191, -1.0894693285, -0.2517800038, -0.3047327680, +0.3596561043,
            -0.4548162700, -1.4395992274, +0.1747274977, -1.2709436621, +0.2736215166,
            +0.7047777372, +0.3204704899, -0.2744422464, -0.0872377649, -1.8214439415,
            -0.0835945116, -0.6039148549, +1.2165255757, -0.5432353406, +1.3613448109,
            -0.5580119339, +0.7700707950, +1.0434424225, +1.3552807019, -0.4123782029,
            -1.3236047038, +0.0222110721, -0.2091127948, +0.7389365969, +1.2875642902,
            +1.4606909234, +0.9324375707, +0.6558211411, -0.7518544646, -0.0394423370,
            -0.5778226562, +0.8127182901, -1.4201531204, -0.8399052400, -0.5322915191,
            +1.8937623000, +0.4754836653, -0.2486242181, +0.3544516598, +0.0113865826,
            -1.3415568820, -1.1014814961, -0.8105272843, +0.2020732697, +0.5377504637,
            -0.2583280661, +1.2566661182, +1.0309321158, -1.1358076933, +0.0343035591,
            -1.2748727280, -0.0862238606, +1.0312513127, +0.5525218687, +0.9992523645,
            +0.6004095193, -0.7697495350, -0.0279612153, +1.4385785345, -0.9882833768,
            +0.1791096940, -0.3122400084, -1.1238723610, -1.2955239535, +0.9638228554,
            -0.8344845959, -0.9577728108, +1.4291249551, -0.4439256733, -0.3831952002,
            +0.0361279687, +1.3825482465, +1.2573509521, -0.3200313309, +0.6355341427,
            -0.4899748397, +1.4002632000, +0.2640288623, -0.5729557095, -1.1837221558,
            +0.0220928428, +0.8453539300, -0.6964338665, +1.5613479737, +0.6017150602,
            +1.1591317831, +1.4962667848, +0.5055630374, -0.4024925586, -0.0022301895,
            -0.8808171225, -0.2380538291, -0.1263889335, -1.2270624956, +1.2829010252,
            +1.1659012920, -0.2023339878, +1.4469222005, +0.4910369010, +0.5148145708,
            -0.5585879333, -0.1892166684, -0.1118926136, +1.7707799312, -0.7099260886,
            +1.2181692145, -0.2162856459, +0.5090497515, +1.3798097233, +0.5534236734,
            +1.5861219677, +0.0473259571, +0.0759487853, -0.4800094936, -1.1161541227,
            +0.2754784559, -1.5604203593, -0.0792565330, -1.2125350672, -0.1125923412,
            +1.5249430370, -0.4856862525, -0.2349726680, -0.3760169641, +1.1144759693,
            -1.0864471403, +0.7017300011, +1.4502098874, -0.2616387685, +0.3945174954,
            +0.1282960259, +1.6051913595, +0.8504986684, +0.7193102303, +0.4076096620,
            -1.1673756381, -0.9865782759, -0.8127574831, +0.9995403808, -0.0651285341,
            +1.0597568163, -1.0427712976, +0.4841266562, -0.1613319818, -1.2365827443,
            +1.1069876626, +1.0689568330, +0.6883391790, +1.0532097608, -0.2210157876,
            +1.6336972026, -0.1169131536, +0.3796970340, -0.2244835027, -1.0596235578,
            -1.6882298535, -0.4026774875, +0.5790348642, +0.4319404661, -0.6825517289,
            -1.0420891019, +0.4703792971, -1.2614194784, +1.0475006975, +0.0660061275,
            -1.1115203350, +0.6219642641, -0.6643952149, +0.2573941971, +1.3674831717,
            +0.6104410011, +0.1848728647, -0.2275884976, -0.6851337535, +1.7527062000,
            -0.6274920022, +1.5045483400, +0.2212603180, -1.0514866173, -0.4335987143,
            -0.1894075701, -0.0764645670, +0.1043714973, +1.1604740125, -1.6126638209,
            -0.4793458989, +0.5247143922, -0.1485283477, +0.6136056483, -1.7596390977,
            +0.4593772039, -1.5807876036, +0.7433875464, +0.7443767102, -0.4282071997,
            +0.3334185668, +1.0526333947, +1.0550636144, -0.1768323052, -1.2792052615,
            -0.5013801734, -1.7297765796, -0.5680502301, -0.6449752959, -0.1334792494,
            -0.7384873298, +1.1948407251, +1.1294421482, -0.4306189235, -0.7522764665,
            +1.9623456580, -0.0601015262, -0.3443093306, -0.0240035994, -0.1626721764,
            -0.6462341030, -1.5495209515, +0.4772598306, +0.4724930194, -0.8545991491,
            -0.9271592486, -0.9164331857, -0.8762201985, +0.1053094866, +1.2335615184,
            -1.0452875145, +0.3323893576, -0.8370427499, +1.3683840682, -0.4730494727,
            -0.1584241227, -0.2455324631, -1.4932560684, +0.8009539349, +1.0214082021,
            +0.1797586888, +0.7739372059, -0.3812911277, +1.7411792905, +0.4377439539,
            +0.0959518921, -0.0847042684, +0.7671399835, +0.4417224083, +1.7888532587,
            -0.2714200077, +1.3897209289, +0.1735228453, +0.0681501032, -1.4000901060,
            +0.0040328233, +0.1419446850, -0.9740310106, -1.3729349811, +1.0705832852,
            +0.4545660210, -0.3265565076, -1.1245342375, +1.0226134198, -1.1732071950,
            -0.9494741062, -0.9504645175, -1.1734348225, -0.3130958988, +0.8486093319,
            -0.9757942081, -1.2454940243, -1.0070638432, -0.2479547287, -0.6487766696,
            -1.6540480422, -0.5949040117, +0.5762559401, +0.7375154865, -0.1849710504,
            +0.0560458683, -0.7051589996, +0.6606256343, +1.7145557805, +0.3513999041,
            +1.1001204177, -1.2698509474, +0.4939122258, +0.4061097718, +0.8765496017,
            -0.3898506647, +0.6059446156, +1.4988160749, -0.1422571316, +1.1018896788,
            +0.9499030630, +0.8716820975, -0.5062858946, +1.4268365292, -0.2136974581,
            -1.0301948936, +0.6549076890, +1.1512985254, -0.3826209039, +1.0187773797,
            +0.4584098506, +1.5987153387, -0.3904534202, +0.2519468527, +1.0089789813,
            +0.1501195597, +1.5640833736, -1.2134794420, -0.0935125052, -0.2232271790,
            +0.4403494696, +0.0752774911, -1.1975144186, +0.7860559026, -1.3223089575,
            +1.0343032480, -1.0975003922, -0.0454887395, +1.1314336302, -0.6659567518,
            -0.5678182331, -0.2424223239, -0.6977802175, -1.2897175137, -1.2118355391,
            -0.7144746825, +1.5232671234, +0.0796805545, -0.5856307259, +0.9054672059,
            -0.5625313586, +0.1505111648, -1.5929247566, +1.0385433844, +0.2119509813,
            +0.0270820047, -1.5047499736, -0.8772608735, -0.7672576468, -0.6137777653,
            +0.0131821757, +0.0635589002, +1.0647702299, +1.6087180835, +0.5235236209,
            +0.7186411462, -1.2888842493, -1.2047413453, +0.2381133731, -0.5605645434,
            +0.7106190891, +1.0216290234, -1.3274996515, +0.6249542030, +0.5463255147,
            -0.6335303582, -1.0831647314, +0.0584281040, +0.8455934917, +1.3065034456,
            -1.5240780986, -0.2782384039, +0.5520485628, +0.5155589686, +1.0145002091,
            -0.1257538508, +0.6064705500, +1.8625078562, -0.0013785925, +0.3839818041,
            +0.3462236094, -0.1415978256, -1.5682994840, -1.0201269076, -0.5998808957,
            -0.6090050173, -1.4450581856, +0.5417620803, -0.2677155856, +1.0843163480,
            -1.9182244224, -0.3276618638, +0.4501860713, -0.0999395123, +0.0199339790,
            +0.6810523845, +0.8027080105, -0.4199528020, +1.5863979078, -0.4458798285,
            -1.2916869201, -0.4277976767, +0.5072462222, +1.3317191886, +0.3431607802,
            +0.7533258512, +0.2242712142, +0.1338178896, +1.6788518044, -0.7387502796,
            +1.0205904762, +1.1544839971, +0.3374252423, -1.1827357661, +0.3359197725,
            -0.7131855003, +0.3503287566, +0.3784595687, -1.4270209021, -1.0904200587,
            -0.1275177062, -0.2421649159, -1.6204124232, +0.8562826789, +0.7524220497,
            +0.7941341241, +0.0629760671, +0.8980825906, -0.8506830337, -1.3546848507,
            -0.5876950706, +0.4013438684, -1.7865930292, +0.2870135017, -0.4682373340,
            -0.4794200043, -0.5245048470, +1.7373417449, +0.3384354292, +0.6017941898,
            +1.4511852036, +0.6887478693, -0.6235454849, -0.9903300913, +0.2238866140,
            -0.3644728320, -0.8687821535, -1.5824285834, -0.1157396583, +0.7712984083,
            -0.4843077904, -0.1071181712, +1.4772407071, -0.8383016657, +0.9321919716,
            +0.8989098311, +0.2914427417, +1.2725678231, +0.5502129989, -1.0885123038,
            -0.9941628672, -1.0791837589, -1.0938689305, -0.6550078935, -0.4705507734,
            +0.1699911365, -0.9656946644, +1.5648879845, -0.6644284248, -0.3849640150,
            -1.2640949187, +0.4335092900, -0.9750943149, +0.5730971291, -0.9668942495,
            -0.6467090571, +0.2977390881, -0.6333990112, +1.6638545894, -0.5687815300,
            -0.9149155132, +1.1998957973, +0.6536737263, -1.0888266025, -0.3321851423,
            +0.4004313182, -0.5589116322, +1.6760503230, +0.8183645559, +0.2200166246,
            -0.5554008162, -0.4349796728, -0.1912259551, -0.8048637094, +1.6786749718,
            -1.6510751672, +0.2960916800, -0.3150855653, +0.7172724023, +0.7566517671,
            -1.1765872572, -0.8507361063, -1.2441093486, +0.5634132344, -0.1632420279,
            -0.4745943513, +0.1683270003, +1.3824965728, +0.4881271903, -1.2636697729,
            -0.5550915001, -1.3102458921, +1.0331188650, -0.6601011541, -0.6870669589,
            +0.5812359172, +1.1212174690, +1.1582096388, +0.0930332695, +1.0270985538,
            -0.8132909314, -0.1437797735, -0.3229643523, -0.9135736798, -1.5423885361,
            -0.8662848703, +0.8307177232, +0.1594864617, +1.5570483757, -0.3310936606,
            -0.4213354539, -0.2701383112, -1.2786889460, +0.5386076899, -1.3506879965,
            +0.5210062774, -1.1315988348, +0.2626475701, +0.8988107062, +1.2534720197,
            -0.9040609290, +0.6401866861, +0.9930216830, +1.3268745007, -0.1616998454,
            -0.7136823678, -1.7912481355, +0.2785563894, -0.1955421696, -0.4077464811,
            +1.0734387218, -0.6849915564, -0.8318877853, -0.9954208212, -0.8340359582,
            +0.0916720939, -0.1927318785, +1.8899311775, -0.6153445263, -0.0629436930,
            -0.0514322848, -0.4115639024, -0.8535647225, -1.5107838575, +0.9038413990,
            -1.1682773077, -0.4769562158, +0.9188385715, -0.9085976256, -0.8589685296,
            +0.3957663286, +0.1905419951, +0.0205733212, +0.6092913823, -1.8534841545,
            -0.0907209671, +0.2279849483, +1.8308445032, +0.3477107133, -0.6832995202,
            -1.4224056098, +1.0506697457, +0.5249650109, -0.7331538144, -0.2444434259,
            +0.0674495494, -0.9452784408, -0.9900939215, -0.5251212194, -1.3586246572,
            -0.1416211791, -0.7210993805, +1.6431192773, -0.7187777982, -0.4934335237,
            +0.5068054340, +1.0856574782, +0.4160995491, -0.8331199486, +1.3027925424,
            +0.5230127406, +0.5023771082, +1.0477755451, +1.4380103293, -0.5553085756,
            +0.0071295269, -0.0980358256, -0.3375033833, -1.8052530775, -0.7858059170,
            +0.1325145240, -1.2441319218, +1.2539962010, -0.9277348610, -0.0371108830,
            +0.5038157423, +0.9398537932, -1.4363552688, -0.7665671129, +0.4605463585,
            +0.0462506548, -1.2605374333, +0.2297368266, -0.6131821042, -1.4071726809,
            -0.5279286088, -1.0321320554, -0.2377081990, +1.5064596492, -0.5745162676,
            +1.4631104055, +0.8151109118, +0.7827198919, +0.5493474740, -0.5295933029,
            +0.3951846604, +1.7775474688, +0.3153040997, -0.7608688357, +0.0762628357,
            -0.6884692740, +1.7626834622, +0.3550760758, +0.0887588207, -0.5338538406,
            +1.2191299513, +0.0437135079, -0.3743082719, -1.5394642074, +0.0418875011,
            +0.7173640461, +1.0593713650, +0.8926403386, -0.0394645360, +1.2509024393,
            +1.1694715935, +0.5655488307, -1.0900453450, -1.0270843522, +0.2634190415,
            +0.6121350796, +0.3996470676, -1.0639956757, -0.9973656864, +1.1570426765,
            -0.0328687534, -1.6257066420, +0.9740209148, -0.0336034884, -0.6373002608,
            +0.8082412311, +1.0257462935, +0.6017522292, -1.3364946739, +0.3824485521,
    };
    protected static final double[] grad6d = {
            0.31733186658157, 0.043599150809166, -0.63578104939541,
            0.60224147484783, -0.061995657882187, 0.35587048501823,
            -0.54645425808647, -0.75981513883963, -0.035144342454363,
            0.13137365402959, 0.29650029456531, 0.13289887942467,
            0.72720729277573, -0.0170513084554, 0.10403853926717, 0.57016794579524,
            0.10006650294475, -0.35348266879289,
            0.0524867271859, 0.16599786784909, -0.49406271077513, 0.51847470894887,
            0.63927166664011, -0.21933445140234,
            -0.57224122530978, -0.089985946187774, 0.44829955643248,
            0.53836681748476, -0.051299333576026, -0.41352093713992,
            -0.35034584363296, -0.37367516013323, -0.52676009109159,
            0.12379417201967, 0.42566489477591, 0.51345191723381,
            0.40936909283115, 0.33036021753157, 0.46771483894695, 0.15073372728805,
            0.51541333179083, -0.46491971651678,
            -0.64339751231027, -0.29341468636474, -0.50841617762291,
            -0.080659811936781, -0.46873502824317, -0.12345817650503,
            0.46950904113222, 0.41685007896275, -0.33378791988356,
            -0.39617029121348, 0.54659770033168, 0.19662896748851,
            -0.49213884108338, 0.50450587466563, -0.0073247243900323,
            0.57958418990163, 0.39591449230465, 0.10272980841415,
            0.34572956497624, 0.62770109739866, 0.12165109216674, 0.35267248385686,
            0.34842369637704, -0.47527514024373,
            0.076282233884284, 0.56461194794873, -0.392426730607,
            -0.20639693057567, 0.33197602170266, 0.60711436994661,
            0.46792592791359, -0.38434666353171, -0.46719345820863,
            -0.40169520060432, -0.061343490026986, 0.49993117813162,
            -0.25398819915038, -0.82255018555745, 0.40372967512401,
            0.21051604195389, 0.020384827146984, 0.22621006002887,
            0.23269489013955, -0.42234243708413, -0.18886779174866,
            0.44290933725703, -0.40895242871151, 0.60695810498111,
            -0.13615585122038, 0.26142849716038, 0.68738606675966,
            0.42914965171764, 0.26332301994884, 0.43256061294487,
            0.06145597366231, -0.25432792035414, 0.65050463165568,
            0.35622065678761, -0.52670947710524, -0.32259598080167,
            -0.28027055313228, 0.30275296247348, 0.39083872911587,
            0.17564171472763, 0.25278203996272, 0.76307625890429,
            -0.62937098181034, -0.24958587788613, 0.11855057687171,
            0.52714220921895, 0.47759151204224, -0.14687496867489,
            0.68607574135496, 0.28465344118508, 0.57132493696771, 0.11365238375433,
            -0.32111327299854, -0.076352560636185,
            0.42669573845021, -0.1643996530281, -0.54881376863042,
            -0.56551221465284, 0.4027156095588, -0.087880721039792,
            -0.30211042220321, -0.47278547361731, 0.050137867251391,
            0.46804387457884, -0.39450159355792, 0.55497099667426,
            0.31255895138908, 0.034478918459459, -0.079232996020732,
            0.39803160685016, 0.82281399721198, 0.24369695191021,
            -0.5524321671417, 0.49350231710234, 0.52530668244467, 0.253625789825,
            0.26218499242504, -0.20557247282514,
            0.060763010271891, -0.023938406391206, 0.36557410300471,
            0.55368747615095, 0.25557899769702, -0.70014279913759,
            0.36398574324757, 0.049110464042478, -0.2428951164628,
            -0.18733973495522, 0.020130805835303, 0.87784000694654,
            -0.62385490124849, 0.020947599003133, -0.44548631925386,
            -0.21069894502123, -0.60559127508405, 0.027809382425643,
            0.51562840479369, -0.27416131751628, -0.14365580420426,
            -0.46525735490594, 0.16338488557607, 0.62862302132303,
            0.52085189275139, 0.51359303425374, 0.021844789421786,
            0.53521775458267, -0.23767218281397, -0.34858599348565,
            0.12263603513069, 0.53912951801629, 0.57550729534804,
            -0.10335514143554, 0.57524709075397, 0.14662748040551,
            0.40942178494947, 0.17197663954561, -0.025238012475873,
            -0.20104824969996, -0.60303014654018, 0.63094779803243,
            0.051685704973311, 0.23577798459204, -0.19154992327678,
            -0.67743578708385, -0.51070301615526, 0.43047548181493,
            0.21373839204543, -0.44348268823586, 0.34347986958921,
            -0.49945694096162, 0.45888698118478, -0.42382317871053,
            -0.60376535923059, -0.065300874745824, 0.49448067868339,
            0.12358559784007, 0.58623743735263, -0.16656623971303,
            0.44140930948322, -0.41692548571374, -0.23774988226818,
            -0.27542786466885, 0.39264397083621, 0.58717642823542,
            -0.67860697457746, 0.2070991391515, -0.12832398784247,
            -0.58381216132288, 0.24050209342748, 0.2854077401022,
            -0.021324501342617, 0.0098658783730532, 0.2694901128571,
            0.42580554353158, -0.82903198308789, -0.24128534823695,
            -0.20344882384938, 0.51719618805529, 0.24379623299129,
            0.11303683173372, -0.46058654895958, -0.63777957124993,
            0.15686479897897, -0.67777169905813, -0.04974608057712,
            0.51313211803344, 0.49928667286231, -0.030863149692696,
            0.53527130791104, -0.50102597915466, -0.60754472649714,
            -0.25235098830686, 0.13490559284448, 0.10708155847142,
            -0.20613512232544, 0.39533044356843, -0.34422306275706,
            0.4792145528465, -0.19178040223502, -0.64521804411898,
            0.3304779611047, 0.49148538926455, -0.30004348427342, 0.33473309391851,
            0.31079743137844, 0.59208027276116,
            -0.52688857216953, 0.40250311061529, 0.38833191043333,
            0.50432308135853, -0.33327489215794, -0.21015252001231,
            -0.30306420816123, -0.34460825415019, -0.26894228639121,
            -0.58579646837355, -0.51178483212848, 0.33464319317466,
            -0.20258582390514, -0.29195675136034, 0.11887973573086,
            0.91211540292822, 0.034118810787236, -0.16269371903027,
            0.61207678339522, -0.21883722070929, -0.23415725333464,
            0.0041447691596985, -0.34019274152454, 0.6378827339521,
            0.11272999861808, -0.54780877011146, -0.62497664375172,
            -0.41373740141301, 0.33306010353229, 0.12039112788093,
            0.24918468395037, -0.068734287809286, -0.42234580029763,
            0.12235329631887, -0.26545138767734, 0.81815148205875,
            0.32048708659406, -0.40233908147851, 0.24633289057781,
            -0.37087758270512, -0.55466799718133, -0.47908728788262,
            -0.33748729653627, -0.45507986822699, -0.50597645316527,
            -0.2863701644881, -0.5404199724601, -0.22120318557996,
            -0.23520314824941, 0.82195093398991, -0.22661283339659,
            0.16382454786402, -0.41400232366734, -0.13959354720703,
            -0.30495751902889, -0.47964557116121, -0.68490238495876,
            -0.4324077675155, -0.13521732523742, -0.050887702629247,
            -0.56629250538137, 0.19768903044, -0.080075220953828,
            -0.29952637623112, 0.095974426142512, -0.73136356489112,
            -0.21316607993139, 0.47585902758173, -0.49429850443227,
            -0.24146904800157, 0.45631329089651, 0.46610972545109,
            0.12647584748018, -0.10203700758813, 0.20801341293098,
            0.66418891258418, -0.65219775460192, -0.2526141453282,
            -0.69345279552921, 0.30149980453822, -0.46870940095961,
            0.20092958919922, -0.21817920622376, 0.34721422759447,
            -0.69001417476102, 0.09722776919634, -0.37852252163632,
            -0.24995374433763, 0.24829304775112, 0.4970126640943,
            -0.82278510972964, 0.050748830242865, -0.3934733016285,
            0.00029980431140623, -0.34677214869339, -0.21301870187776,
            -0.51821811089111, -0.22147302694699, 0.53524316281446,
            0.12892242816244, -0.5543955478928, -0.26821451961648,
            -0.21006612796354, 0.26079212570498, -0.021870637510645,
            0.72402587064608, -0.27651658712238, 0.53544979218311,
            -0.099744280251479, -0.4534212871731, 0.71954978543864,
            -0.31082396323078, -0.26933824624449, 0.31233586755618,
            -0.48121951222937, -0.43051247772929, -0.5038415181805,
            0.12342710418307, 0.037467829082858, -0.55909965468017,
            -0.51180831908824, -0.079955485578946, -0.53046702060975,
            0.48748209854708, 0.16148937559829, -0.43191028009105,
            -0.38131649706702, 0.46242477534251, 0.46416075424014,
            -0.20634110277567, -0.53778490132009, 0.30582118902172,
            0.6245043069106, 0.14316692963071, -0.1436103838143, 0.27519251589203,
            -0.60467865310212, -0.35708047307373,
            0.52425890739441, -0.20390682829262, -0.33609142609195,
            0.51803372559413, 0.28921536255925, 0.46756035964091,
            -0.4455164148456, 0.31831805515328, 0.24217750314789, 0.49821219078654,
            -0.47209418708575, 0.41285649844363,
            -0.015857310429397, -0.45214512052441, -0.14591363373753,
            0.74070676188619, 0.0098874230592725, -0.47463489014478,
            0.24260837156464, 0.44639366601915, 0.31528570191456, 0.45334773303464,
            -0.47964168123625, -0.45484996397296,
            0.47123463487178, 0.64525048646519, -0.064257637508608,
            -0.18737730572971, -0.11735335340515, -0.55549853319118,
            -0.025197229767488, -0.257963271803, 0.26277107860996,
            -0.58236203161499, -0.41893538667715, 0.59086294196016,
            -0.48940330017687, 0.33728563842186, -0.057634928591543,
            0.44862021996899, -0.40048256377746, 0.53080564921806,
            0.73350664260388, -0.021482988114587, 0.016568147533453,
            0.0021905972927896, 0.49384961731337, 0.46619710394628,
            -0.25151229880228, -0.62009962583403, -0.26948657433033,
            0.31711936293198, -0.35081923073755, 0.50592112116981,
            0.0094298597779172, -0.35925999444899, 0.47529205807388,
            -0.26709475088579, -0.53352146543694, 0.53754630836074,
            -0.5948549517534, -0.53195924881292, -0.094383768924555,
            -0.41704491211939, -0.41397531920841, -0.09463944474724,
            -0.74917126125127, -0.24166385705367, 0.22864554725283,
            0.31721357549513, 0.06066292638611, -0.47303041351952,
            -0.3300396030254, -0.08758658200966, -0.096726092930468,
            -0.39607089556472, 0.55566932028997, 0.63906648027271,
            -0.58933068378397, -0.38176870540341, 0.46748019640554,
            -0.061358837959321, 0.36268480315292, -0.39127879224432,
            -0.066556695042975, -0.73863083674701, -0.32153946998935,
            0.57454599361106, -0.090856896694743, -0.09082394033963,
            -0.36335404704287, -0.41643677881158, -0.57839830999334,
            -0.030959887755637, 0.5989792522053, -0.016582566905843,
            0.23126668855143, 0.2107790785413, -0.14272193312959,
            -0.29232225134991, -0.48451339172564, -0.74934159314943,
            0.48188197979627, -0.040214759215399, -0.15667971883369,
            0.16054853668069, -0.6083975436752, -0.58796308779952,
            0.31319356064062, -0.19280657835646, 0.76136690598738,
            -0.084506239097717, 0.4768786755523, -0.22472488900872,
            0.67504537519138, 0.36920158913876, 0.40321048682396,
            0.034436041975613, -0.29332731631919, 0.39774172001359,
            -0.1459159803857, -0.59726183207777, -0.036384224081948,
            -0.65093487874945, 0.39515711468056, -0.20198429937477,
            0.60092128630869, 0.18110182176699, 0.2579491954112, -0.39594768022975,
            0.15112959843347, 0.59995268930018,
            -0.42310244265976, -0.26937197256148, 0.074700012546319,
            0.53119510349465, 0.41614374632783, 0.53618944036115,
            0.0071605427687482, -0.69599782505338, -0.053138604739257,
            -0.00054500262230378, 0.69533871546989, 0.1709263483943,
            0.12447149375466, 0.33265313001972, 0.35070015349473, 0.53879932284829,
            0.37648083373421, 0.56463759722353,
            0.29540077719054, 0.04954124873475, -0.48345087234985,
            0.72758494948264, 0.070069102610626, 0.377186640377,
            0.4882414260383, 0.45135801463006, 0.48450857902353, -0.26042407965644,
            -0.4251358047458, 0.2731053563007,
            -0.49806371818291, -0.4719759672029, 0.029647087810764,
            -0.13788472163255, -0.45346141932978, -0.5510470160674,
            -0.5359511936033, -0.53585470245895, 0.1771036246335, -0.4537763243703,
            0.41838964069644, 0.11527149720722,
            -0.36846431808379, -0.46533180802325, 0.65800816763703,
            -0.28691297783558, 0.31521457275327, 0.18178647457201,
            -0.29243126901345, -0.4352956525447, -0.58895978125929,
            -0.49649471729812, 0.29271342931272, 0.21433587621517,
            0.056256690265475, -0.50387710054371, 0.48145041862725,
            0.44723671964597, -0.55771174894027, -0.0092449146014199,
            -0.40973125164006, -0.73147173623276, -0.094076302480945,
            0.43033451471976, 0.014334271843521, -0.32066459724334,
            0.26752725373294, 0.50477344684769, 0.065069516529324,
            0.36001097578267, 0.59393393889869, -0.43247366096278,
            0.48945720845334, 0.6043315650632, 0.12458128550608, -0.48327805813458,
            -0.25681943056744, 0.28316179557217,
            -0.45182760404001, 0.21574002665039, -0.31462623994251,
            0.25279349500371, 0.44865729380505, -0.62058075048081,
            0.44017304540101, 0.43789555905674, 0.58423563606269, 0.41842994331139,
            -0.26836655962348, 0.16143005677844,
            -0.67897032028819, -0.32730885869255, -0.0243997359109,
            0.40649244381227, 0.47711065295824, -0.19596475712206,
            0.57441588138131, 0.09386994843744, 0.28400793066375, 0.59394229842661,
            0.45349906020748, 0.14881354725974,
            -0.3393739967757, -0.54929055652002, 0.26209493900588, 0.0733800373509,
            0.56557076402003, 0.43492125584075,
            0.050007991188197, 0.74652764513134, -0.36432144611385,
            -0.20993543754239, -0.1352041047841, 0.49508839805322,
            -0.041332158875019, -0.20655741061568, 0.52511282214888,
            0.047248635933477, -0.6276121766011, -0.5326844609727,
            -0.1889491176448, 0.05188976739355, -0.45677123586268,
            0.42884456750344, 0.61612085530435, -0.43526216197988,
            -0.65873541163911, -0.094770059351695, 0.40844030815782,
            0.35536013391048, -0.16940065827957, 0.48506226422661,
            -0.45779281442862, -0.46052673126242, 0.34138050378631,
            -0.54943270263121, 0.37140594702643, -0.14826175595089,
            -0.069378715405383, -0.14845488608058, -0.73991837897813,
            0.41519184526768, -0.11098464009855, -0.49088356499611,
            0.46422563805447, 0.46130716873201, -0.44207791495441,
            0.12050605352899, 0.34969556083561, -0.4893349322843,
            -0.35482925073362, 0.28146983672487, -0.35356606227648,
            -0.38774754218768, 0.35979702647173, -0.62454776976122,
            -0.48343191508515, 0.41492185792886, -0.50175316406656,
            0.21953122931153, -0.54083165333237, 0.041040952107647,
            -0.51280508048852, -0.54131124436697, -0.0099287129207481,
            0.23788701199175, 0.4350333223576, 0.44505087885649,
            0.2253837335044, -0.30117119745248, 0.46587685049056,
            -0.46672901001472, -0.59182069765377, 0.27086737661249,
            0.43015756480475, -0.067851118947538, -0.26917802105288,
            -0.57731860676632, -0.53950120703807, -0.33696522367557,
            0.20858352742161, 0.63695057987625, 0.49453142202915,
            -0.046235371593379, -0.54436247241885, -0.088075720520231,
            -0.35626464703623, 0.067539543974725, -0.18142793486226,
            -0.49044207117167, 0.5542388249925, 0.53654796190017,
            0.52238539932434, 0.55175875223621, 0.29070268774296,
            -0.14119026819648, -0.55841587206055, -0.080029639759127,
            -0.025988002903175, 0.46612949273683, -0.56880970348453,
            -0.44824563336003, -0.030000490931808, 0.50663523727173,
            0.047284583258099, -0.26595723160738, 0.21032033434131,
            0.52986834914146, -0.52245334572957, -0.5736534757312,
            -0.31924244568277, -0.13888420092891, 0.30725800370737,
            0.49792332552544, 0.61035592292817, -0.40487771982263,
            0.038758575627018, -0.53813545398707, -0.56167256912901,
            0.46815373895572, -0.14142713486975, 0.39276248966752,
            -0.19936871608885, 0.12488860648831, -0.62990029833727,
            -0.29296146144627, 0.49734531468753, 0.46335923993672,
            -0.078826705546604, -0.15548800857414, 0.57456768467721,
            0.5558854465212, -0.56893054194692, -0.082408823513622,
            0.11678856295109, 0.53358760166951, 0.49302489382249,
            -0.53981846952046, -0.237913367643, -0.33251226509871,
            0.39126928439834, -0.39416116630681, -0.35778844984527,
            -0.39395609960567, 0.50270356681194, -0.39448759513757,
            -0.17961290695406, 0.34239532682819, -0.21870225043453,
            -0.23322835296688, 0.75997835134209, 0.41317237364121,
            0.29699501400111, 0.17195435585404, -0.34903627841034,
            -0.31751884057854, -0.59661546358767, 0.55102732418683,
            -0.2237291316445, -0.51254305965518, -0.31277318571798,
            0.54270199705442, -0.34885011313806, 0.41616819064585,
            0.53534023676892, 0.45905986582643, -0.20308675275303,
            0.019523641323632, 0.3378580580099, 0.58898336258938,
            -0.045038463119119, -0.52553334288797, -0.6098545897634,
            0.46226027841702, -0.36069029000651, 0.077984430434637,
            -0.40129033029845, 0.39526722066586, -0.20379584931963,
            0.45466492237669, 0.46504795737483, -0.46712669863522,
            -0.43845831945339, -0.59284534057943, 0.050241908216277,
            -0.36494839821973, 0.32363879325018, 0.46458051299488,
            -0.46057360356064, -0.34584626825548, -0.12264748451482,
            0.48835437094478, 0.21102526990984, 0.60843919401837,
            -0.086047549693024, -0.16981605114589, -0.37222833669973,
            0.45158609930017, -0.55710254634126, 0.55759406480139,
            0.54697451263099, -0.45070837355303, 0.032962522247893,
            -0.48584332140086, -0.28055687213837, 0.42642516953676,
            0.34061925303691, 0.38443007758012, 0.61614808332652,
            -0.55774172327958, -0.075660378162998, 0.19938218730551,
            0.30626924920956, -0.057939049897675, -0.10461119704504,
            -0.4395638756485, -0.57307193269415, 0.60849886616281,
            -0.52519951444608, -0.42567534157254, -0.19896500097138,
            0.48819483593271, 0.12539008064447, 0.49932157157064,
            -0.10173361116951, -0.07873850987854, 0.3713554090283,
            0.65889542748449, 0.63411890875068, 0.096414235519521,
            0.60342393773609, 0.057617370697663, 0.35558841250938,
            0.20766418929404, 0.030670189501999, -0.67974377143949,
            -0.071971052874019, -0.44567383014704, 0.65917594080871,
            0.44113802003588, -0.29627117199757, 0.28160739274962,
            0.38284479693596, 0.43552320173998, -0.4282368470258,
            -0.54809258921772, -0.27202273485667, 0.32551612927831,
            -0.74755699288716, -0.20979308948438, 0.19268299390085,
            0.27864013929953, -0.39085278833717, 0.36001727246301,
            -0.64575536737195, 0.59253747557756, 0.040885512266333,
            -0.20167391777406, -0.43481684011627, -0.02212841779644,
            0.45874103754271, -0.0066587566394561, -0.30494054091993,
            0.52731059172348, -0.64443887148677, 0.056264275617853,
            0.61573773369959, -0.00074622703454316, 0.25455659350429,
            0.30670278147618, -0.18573195942296, 0.65383825999316,
            -0.089919562456316, -0.28968403215216, -0.60618287937171,
            0.53370861364121, 0.37921556323246, -0.33450055738044,
            -0.47481167613763, 0.3899274103573, -0.1047963185367, 0.45545456567005,
            0.12142073778317, 0.62397625076847,
            0.59154225785278, -0.10812441303593, -0.4685834521013,
            -0.36007270807588, -0.1012374701199, 0.52812407295968,
            -0.01292122984647, -0.23607532114711, -0.57680411110671,
            -0.44955815301222, -0.31913443306122, -0.55448100298376,
            0.54231398466289, -0.31845386154668, -0.38636423612049,
            0.22187979539931, -0.6346425853783, -0.056599490898788,
            -0.41950690366157, -0.4578028963184, 0.31139813874057,
            0.39787962066193, -0.20885901240181, 0.56172180435883,
            -0.031404881097728, 0.56267475273157, -0.5556815383811,
            0.33075363850824, 0.39071115867626, 0.3340294973255,
            -0.51485161085589, -0.34037011091125, -0.46826090820473,
            -0.60086679836276, -0.075069409610657, 0.18202033570633,
            -0.49669644859095, 0.13236483793072, 0.53440735955877, 0.4720120049858,
            -0.05992551666341, -0.47306929861073,
            -0.32796852486185, 0.65593302097807, 0.20800030327303,
            -0.38965914824176, -0.51564565153044, -0.034636725857177,
            -0.30473794783797, 0.12584230588041, 0.63911213518179,
            0.11269477188219, 0.62944339013855, 0.27191006392352,
            -0.53642197294029, 0.50742224701512, -0.22907820767928,
            0.47022559371179, -0.1914125650624, 0.38019261684316,
            -0.28865425091309, 0.76169672032907, -0.36166127667225,
            -0.30555403321368, -0.12541657537884, -0.31081403770203,
            0.0025978417989835, 0.3737146483793, -0.3151511957077,
            0.62032810853005, 0.60524642517936, -0.09939888944988,
            -0.40019833530022, 0.15931480693456, -0.61653030345628,
            -0.49479441153976, -0.021517911098538, -0.43481713333933,
            -0.26445143166732, -0.48401155081335, 0.27737058096082,
            -0.12537486208624, -0.46956235249512, 0.61859207953377,
            -0.49776294425122, 0.6509513246149, -0.20147785800704,
            0.26022926925791, 0.39526195830317, -0.25288299425858,
            0.20792543895216, 0.6725599557329, 0.013296712014115,
            0.069082404776847, -0.37233547685047, 0.60070560947898,
            -0.60329265885108, 0.40708027238668, -0.17229997007444,
            -0.52997954496878, 0.22211745651394, -0.33229784433365,
            0.61826884506104, -0.62582169643111, 0.33820439950773,
            0.23870919720066, -0.20670655096227, -0.10953969425599,
            -0.63678168786213, -0.51101649337563, -0.19131817442969,
            -0.49493417544846, -0.22614515287593, 0.025828539221376,
            0.7068462559507, 0.072932806612059, -0.30827034359477,
            -0.52659704221432, -0.33954839093364, 0.086145323573817,
            -0.52429050496975, 0.39091424683727, 0.52819210715237,
            -0.16569162349745, 0.447191673089, 0.25667977984796,
            0.85033978527922, -0.37311666188152, -0.031585518143925,
            -0.063546921071094, -0.35026506762952, 0.099923633151172,
            -0.43149574251927, 0.16017753208259, -0.36624037246965,
            0.49372029676385, -0.60067103922455, 0.2223896202103,
            -0.43599537393092, -0.360658355506, -0.42475053011196,
            -0.52301759011739, 0.039454536357949, 0.47362064109658,
            -0.35793170214797, -0.43917817788312, -0.49072242572643,
            -0.32880277826743, -0.38509560837703, -0.42636724894184,
            -0.043679644403255, 0.74697226557232, -0.40732954428872,
            -0.48088968590275, 0.18029290312902, -0.10220931735307,
            -0.058902573502295, 0.0082595236590186, 0.7136596141971,
            -0.53043791172483, 0.22906331492979, 0.39155822265168,
            0.43459649233879, 0.18964470832196, 0.15217427204218, 0.59694624534505,
            0.053786588105393, 0.62671041756872,
            -0.48833575031057, 0.068909881680922, 0.60168404074737,
            -0.055455043023162, -0.62426261497771, -0.044461939113733,
            -0.71822145541427, 0.054494951105527, 0.25733756171599,
            -0.42706881935297, -0.44024663347316, 0.19687748949208,
            0.4723221071836, 0.63009683957253, 0.2166256995021, 0.31063720960745,
            0.079455887335627, 0.47974409023622,
            -0.39506538843406, 0.42517729990346, 0.29375773990216,
            0.044503633424429, -0.46173213926286, 0.60139575234582,
            -0.40354126620316, 0.41304136826673, -0.29533980868045,
            -0.45300699221804, 0.23702354154238, -0.56385297528377,
            -0.62315380378984, -0.42397903326965, 0.53044082394843,
            0.37874432092957, 0.054922713129263, 0.063952196248596,
            0.41959045692314, -0.83420441875842, -0.25505372502578,
            0.25012310515014, 0.010974237503127, 0.017675743681809,
            -0.25231575134089, -0.17034034508503, -0.0022254428444259,
            -0.4967771056787, 0.43184899693064, -0.68850194407078,
            -0.1852812882862, -0.48330898597592, 0.13528868642679,
            0.15202104844417, 0.57661281495368, -0.59848767913131,
            0.64287473226568, -0.30923674494923, 0.22234318117192,
            0.099248962994541, 0.64370450011427, 0.13206961744112,
            -0.49018899717866, 0.68654120859156, -0.27238863334662,
            -0.085832423495263, 0.44161945604453, 0.10856057983467,
            0.48795432482822, 0.42184193883513, -0.43797315744756,
            0.35186997012044, -0.46483432791096, 0.22857392808385,
            0.52970834834669, -0.50684486922008, -0.39782161731912,
            -0.3932709335414, -0.34863027587322, 0.16748196501934,
            -0.46048505533, -0.3887126918161, -0.68287320410729, -0.18448530888361,
            -0.25358256326157, 0.26870280714361,
            0.6889557358588, -0.3101022706485, -0.35882194962822, 0.30088738418801,
            -0.039139540883101, -0.45646277242166,
            -0.21954767479275, 0.40838837410593, 0.23284186868997,
            0.30349649888064, 0.57233263099925, 0.55778817953937,
            0.57731035290905, 0.091218309942656, 0.70670016667131,
            0.016358033634041, 0.3939245235472, -0.059352634867484,
            0.50055570130024, -0.021749790970703, 0.56767851040093,
            0.50580176326624, 0.34691320957643, 0.22478399991032,
            -0.37901911159632, 0.53804099887537, -0.46780195460858,
            0.51497346779204, -0.27981005467588, 0.067278440906787,
            0.67241900483514, 0.074099582737, 0.43138117954806, 0.054567519697911,
            -0.37927768894619, 0.45764946429346,
            0.14529189179172, -0.23854982910384, 0.45401647091062,
            0.25466539906731, 0.46182069803887, -0.66160446396375,
            -0.15570980059397, -0.38476787034627, 0.37322840954917,
            -0.43977613626294, -0.61243005550684, -0.34631643815896,
            -0.19590302894013, 0.42065974653653, 0.43447548638809,
            -0.10575548452794, 0.70439951675651, -0.29754920754254,
            -0.13558865796725, 0.1427073453776, 0.49647494823192,
            -0.65533234019218, -0.11714854214663, 0.5211321311867,
            -0.6228374766114, 0.20812698103217, -0.16205154548883,
            0.20384566967497, -0.59321895467652, 0.38604941246779,
            0.44487837128099, -0.37224943035393, -0.22188447638327,
            0.48921538939858, 0.41432418029434, -0.45087099253189,
            0.66422841315008, 0.21517761068003, 0.094012579794123,
            -0.4358159040875, 0.22245680154647, -0.51404116085847,
            -0.11369362736032, 0.32284689991698, -0.38818285117689,
            0.49680024166881, 0.047684866166158, -0.69503480904222,
            -0.5137200731924, -0.50673230867252, 0.32715252974108,
            -0.26799714004956, -0.47616510509846, 0.27153195326233,
            -0.47315177716491, -0.45711495983609, -0.31178280842352,
            -0.51697763052226, -0.14302372043059, -0.42689944315384,
            -0.050442035795027, 0.23609184251469, 0.38634880236106,
            0.56012774305243, 0.38963669840218, -0.57174382424149,
            -0.15472134925391, -0.15333579424307, -0.14189768300467,
            0.032279269476252, -0.66054298438621, -0.70360180527557,
            -0.10345191679557, -0.30503725808375, 0.31038263802383,
            0.36878846502877, -0.76824774853417, 0.2714830658427,
            -0.060212868606223, -0.4172755444983, 0.39199300681258,
            -0.44040104260082, 0.24955102139032, -0.64215903203727,
            0.25443195353315, -0.013789583113498, 0.44365000614699,
            0.53296203342425, -0.55057750350733, -0.38867053403178,
            -0.36068564301268, -0.65616661625162, -0.48495997865466,
            0.24088316031012, -0.18080297655217, -0.33682435258394,
            -0.53824550487673, -0.096728907851005, -0.5208619866167,
            0.33195321221408, -0.032263947064791, 0.56427315050798,
            0.40151657866643, -0.44825725748635, -0.54910020122855,
            -0.095936272447708, 0.5719563905078, 0.00097783623607218,
            0.21961099467771, 0.62823723408945, -0.010045934028323,
            -0.6610564872634, -0.17161595423903, -0.30089924032373,
            0.27961471530636, 0.054523395513076, 0.61485903249347,
            0.11958885677663, -0.61032561244673, -0.39241856813031,
            -0.30223065341134, -0.23605925177166, -0.09697276975263,
            -0.46458104180761, -0.37853464945647, 0.69599203908657,
            0.0023635513043496, 0.62702100484886, 0.49658954056984,
            -0.20369645124455, -0.56457560315907, 0.00021299797811461,
            -0.64198493892962, 0.59676262320476, 0.46274573284143,
            0.088421912306785, 0.098029994490406, -0.012953072012707,
            -0.053965435026011, 0.13439533803278, -0.33103493780685,
            0.55991756423782, -0.58127599631056, -0.46696041830103,
            -0.43965993689353, 0.07544961763381, 0.1509639518808,
            -0.38868406689028, -0.0033436054452783, -0.79191533434483,
            -0.21743914630025, -0.32019630124298, -0.56067107727615,
            0.027284914419519, -0.49444926389798, -0.53908992599417,
            -0.36492599248168, 0.52529904803377, 0.18002253442693,
            0.14829474115897, 0.17212619314998, -0.71194315827942,
            0.0051876209353066, 0.50490293404098, 0.24361032552454,
            0.13688117617809, -0.61381291176911, -0.5386997104485,
            0.66421180843392, 0.21833854629637, -0.087909936660014,
            0.15624552502148, -0.68780724971724, 0.077015056461268,
            0.52710630558705, -0.42143671471468, -0.069964559463205,
            -0.24196341534187, -0.68814841622245, 0.08695091377684,
            0.62392249806692, -0.23663281560035, -0.59058622185178,
            0.22685863859977, -0.36683948058558, -0.14105848121323,
            0.18069852004855, -0.083828559172887, 0.66240167877879,
            0.16722813432165, -0.25503640214793, -0.65462662498637,
            -0.37112528006203, 0.43100319401562, -0.11342774633614,
            0.14418808646988, 0.5753326931164, 0.55842502411684,
            0.55378724068611, 0.21098160548047, -0.3224976646632, 0.31268307369255,
            -0.37624695517597, -0.55269271266764,
            0.2601465870231, 0.56373458886982, -0.21638357910201, 0.41216916619413,
            -0.25078072187299, -0.57873208070982,
            0.11217864148346, 0.54196554704815, -0.31989128683717,
            0.54691221598945, 0.24062434044524, 0.48409277788476,
            0.087564423746579, -0.12083081671284, 0.69931172084498,
            0.35220575672909, 0.28770484569954, -0.53091668762919,
            0.3395702120398, 0.042520943289575, -0.30935928261896,
            0.61022210846475, 0.54650816974112, 0.34079124619266,
            0.32746112891934, 0.32095220193351, -0.61142534799442,
            0.32197324480666, -0.38236071343678, 0.40749411210419,
            0.58741915356593, -0.30916030490652, -0.57642977381104,
            -0.038846190358607, 0.047926713761208, -0.4725265742377,
            0.026224389898652, 0.031768907187292, -0.12510902263321,
            0.36102734397001, -0.72217212865059, 0.57513252722531,
            -0.27510374152496, -0.5153402145828, 0.025774022629799,
            0.59201067073603, 0.40728366085253, -0.37645913420642,
            -0.29983338495183, -0.61017291361195, -0.18551919513643,
            0.50515945610161, 0.18206593801497, -0.46372136367049,
            -0.64290893575119, -0.34887011406157, -0.55318606770362,
            -0.21230198963112, -0.19828983785672, 0.2730419816548,
            -0.32778879906348, -0.094317293167129, 0.57811170538439,
            0.54346692190204, 0.17699503497579, -0.47197676839855,
            -0.075738705663962, 0.53381750682665, -0.13406342524856,
            0.71765386263773, 0.34271060834977, 0.24259408122628,
            -0.30574273227855, 0.17419449782542, -0.78861555508124,
            0.43305678368813, 0.064853328282818, 0.25003806266734,
            0.4397035983709, -0.51651518914239, -0.3972346186176,
            -0.34513492086703, 0.32129829777342, -0.39965829527563,
            -0.25184899643619, -0.35937572373004, 0.15273239148905,
            -0.51640931868766, 0.4218715745627, -0.58261460582976,
            -0.57396000790758, 0.1912786199605, 0.45995634753032,
            -0.43664716984512, 0.4601630113166, 0.14146310231856,
            0.11500068018889, 0.05112652754666, -0.25672855859366,
            -0.54715738035577, 0.67669928552409, 0.40118355777989,
            -0.45252668004418, -0.40809988524453, -0.064931545867856,
            0.19116562077283, 0.76523014995576, 0.048337406798767,
            -0.080075651760374, 0.75305314115418, 0.34797424409913,
            0.29104493928016, 0.0040185919664457, -0.46977598520425,
            -0.3890257668276, 0.49100041230416, -0.17812126809985,
            -0.43787557151231, -0.46923187878333, 0.40489108352503,
            0.37433236324043, -0.29441766760791, -0.066285137006724,
            0.33217472508825, 0.73917165688328, 0.33479099915638,
            -0.02973230696179, -0.51371026289118, 0.34133522703692,
            -0.41361792362786, -0.51561746819514, -0.4263412462482,
            0.51057171220039, -0.23740201245544, 0.26673587003088, 0.5521767379032,
            0.16849318602455, 0.52774964064755,
    };
}
