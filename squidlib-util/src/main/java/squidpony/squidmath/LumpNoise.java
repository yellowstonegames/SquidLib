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

import static squidpony.squidmath.HappyPointHash.hash256;
import static squidpony.squidmath.Noise.fastFloor;
import static squidpony.squidmath.SeededNoise.*;

/**
 * A variant on the Simplex noise functions in {@link UnifiedNoise}, using the hyperbolic tangent function to reduce the
 * result into the -1 to 1 range. Unlike UnifiedNoise or SeededNoise, you can adjust the {@link #harshness} of LumpNoise
 * to make the results more starkly-high-and-low or more middle-of-the-range.
 */
public class LumpNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {

    /**
     * How sharp the change should be from high to low values and vice versa. This defaults to 1.0; higher values have a
     * steeper transition, while lower values (which should be above 0) have a softer one, and may not reach -1 or 1.
     */
    public double harshness = 1.0;

    public long defaultSeed;
    public static final LumpNoise instance = new LumpNoise();

    public LumpNoise() {
        defaultSeed = 0x1337BEEF2A22L;
    }
    public LumpNoise(long seed)
    {
        defaultSeed = seed;
    }

    protected static double gradCoord2D(long seed, int x, int y, double xd, double yd) {
        final int h = HappyPointHash.hash256(x, y, seed) << 1;
        return xd * grad2d[h] + yd * grad2d[h+1];
    }
    /**
     * Computes the hash for a 3D int point and its dot product with a 3D double point as one step.
     * @return a double between -1.2571 and 1.2571, exclusive
     */
    protected static double gradCoord3D(long seed, int x, int y, int z, double xd, double yd, double zd) {
        final int hash = HappyPointHash.hash32(x, y, z, seed) * 3;
        return xd * grad3d[hash] + yd * grad3d[hash + 1] + zd * grad3d[hash + 2];
    }
    protected static double gradCoord5D(long seed, int x, int y, int z, int w, int u, double xd, double yd, double zd, double wd, double ud) {
        final int hash = HappyPointHash.hash256(x, y, z, w, u, seed) * 5;
        return xd * grad5d[hash] + yd * grad5d[hash + 1] + zd * grad5d[hash + 2] + wd * grad5d[hash + 3] + ud * grad5d[hash + 4];
    }

    @Override
    public double getNoise(final double x, final double y) {
        return noise(x, y, defaultSeed);
    }
    @Override
    public double getNoise(final double x, final double y, final double z) {
        return noise(x, y, z, defaultSeed);
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w) {
        return noise(x, y, z, w, defaultSeed);
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u) {
        return noise(x, y, z, w, u, defaultSeed);
    }
    @Override
    public double getNoise(final double x, final double y, final double z, final double w, final double u, final double v) {
        return noise(x, y, z, w, u, v, defaultSeed);
    }

    @Override
    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        return noise(x, y, seed);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        return noise(x, y, z, seed);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        return noise(x, y, z, w, seed);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final long seed) {
        return noise(x, y, z, w, u, seed);
    }
    @Override
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final long seed) {
        return noise(x, y, z, w, u, v, seed);
    }

    protected static final double F2 = 0.36602540378443864676372317075294,
            G2 = 0.21132486540518711774542560974902,
            H2 = G2 * 2.0,
            F3 = 1.0 / 3.0,
            G3 = 1.0 / 6.0,
            F4 = (Math.sqrt(5.0) - 1.0) * 0.25,
            G4 = (5.0 - Math.sqrt(5.0)) * 0.05,
            F5 = (Math.sqrt(6.0) - 1.0) / 5.0,
            G5 = (6.0 - Math.sqrt(6.0)) / 30.0,
            F6 = (Math.sqrt(7.0) - 1.0) / 6.0,
            G6 = F6 / (1.0 + 6.0 * F6),

            LIMIT2 = 0.5,
            LIMIT3 = 0.6,
            LIMIT4 = 0.62,
            LIMIT5 = 0.7,
            LIMIT6 = 0.775;

//            LIMIT2 = 0.6,
//            LIMIT3 = 0.7,
//            LIMIT4 = 0.72,
//            LIMIT5 = 0.8,
//            LIMIT6 = 0.9375;

    public double noise(final double x, final double y, final long seed) {
        double t = (x + y) * F2;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);

        t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;

        double x0 = x - X0;
        double y0 = y - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1 + H2;
        double y2 = y0 - 1 + H2;

        double n0, n1, n2;

        n0 = LIMIT2 - x0 * x0 - y0 * y0;
        if (n0 > 0) {
            n0 *= n0;
            n0 *= n0 * gradCoord2D(seed, i, j, x0, y0);
        }
        else n0 = 0.0;

        n1 = LIMIT2 - x1 * x1 - y1 * y1;
        if (n1 > 0) {
            n1 *= n1;
            n1 *= n1 * gradCoord2D(seed, i + i1, j + j1, x1, y1);
        }
        else n1 = 0.0;

        n2 = LIMIT2 - x2 * x2 - y2 * y2;
        if (n2 > 0)  {
            n2 *= n2;
            n2 *= n2 * gradCoord2D(seed, i + 1, j + 1, x2, y2);
        }
        else n2 = 0.0;

        double ex = Math.exp((n0 + n1 + n2) * 40.0 * harshness);
        return (ex - 1.0) / (ex + 1.0);
    }

    public double noise(final double x, final double y, final double z, final long seed) {
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
        double n0 = LIMIT3 - x0 * x0 - y0 * y0 - z0 * z0;
        if (n0 > 0) {
            n0 *= n0;
            n0 *= n0 * gradCoord3D(seed, i, j, k, x0, y0, z0);
        }
        else n0 = 0.0;

        double n1 = LIMIT3 - x1 * x1 - y1 * y1 - z1 * z1;
        if (n1 > 0) {
            n1 *= n1;
            n1 *= n1 * gradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1);
        }
        else n1 = 0.0;

        double n2 = LIMIT3 - x2 * x2 - y2 * y2 - z2 * z2;
        if (n2 > 0) {
            n2 *= n2;
            n2 *= n2 * gradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2);
        }
        else n2 = 0.0;
        double n3 = LIMIT3 - x3 * x3 - y3 * y3 - z3 * z3;
        if (n3 > 0) {
            n3 *= n3;
            n3 *= n3 * gradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3);
        }
        else n3 = 0.0;

        double ex = Math.exp((n0 + n1 + n2 + n3) * 18.0 * harshness);
        return (ex - 1.0) / (ex + 1.0);
    }

    public double noise(final double x, final double y, final double z, final double w, final long seed) {
        double n0, n1, n2, n3, n4;
        double[] gradient4DLUT = grad4d;
        double t = (x + y + z + w) * F4;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        t = (i + j + k + l) * G4;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;

        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;

        if (z0 > w0) rankz++; else rankw++;

        int i1 = 2 - rankx >>> 31;
        int j1 = 2 - ranky >>> 31;
        int k1 = 2 - rankz >>> 31;
        int l1 = 2 - rankw >>> 31;

        int i2 = 1 - rankx >>> 31;
        int j2 = 1 - ranky >>> 31;
        int k2 = 1 - rankz >>> 31;
        int l2 = 1 - rankw >>> 31;

        int i3 = -rankx >>> 31;
        int j3 = -ranky >>> 31;
        int k3 = -rankz >>> 31;
        int l3 = -rankw >>> 31;

        double x1 = x0 - i1 + G4;
        double y1 = y0 - j1 + G4;
        double z1 = z0 - k1 + G4;
        double w1 = w0 - l1 + G4;

        double x2 = x0 - i2 + 2 * G4;
        double y2 = y0 - j2 + 2 * G4;
        double z2 = z0 - k2 + 2 * G4;
        double w2 = w0 - l2 + 2 * G4;

        double x3 = x0 - i3 + 3 * G4;
        double y3 = y0 - j3 + 3 * G4;
        double z3 = z0 - k3 + 3 * G4;
        double w3 = w0 - l3 + 3 * G4;

        double x4 = x0 - 1 + 4 * G4;
        double y4 = y0 - 1 + 4 * G4;
        double z4 = z0 - 1 + 4 * G4;
        double w4 = w0 - 1 + 4 * G4;

        double t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            final int h0 = (hash256(i, j, k, l, seed) & 0xFC);
            t0 *= t0;
            n0 = t0 * t0 * (x0 * gradient4DLUT[h0] + y0 * gradient4DLUT[h0 | 1] + z0 * gradient4DLUT[h0 | 2] + w0 * gradient4DLUT[h0 | 3]);
        }
        else n0 = 0;
        double t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            final int h1 = (hash256(i + i1, j + j1, k + k1, l + l1, seed) & 0xFC);
            t1 *= t1;
            n1 = t1 * t1 * (x1 * gradient4DLUT[h1] + y1 * gradient4DLUT[h1 | 1] + z1 * gradient4DLUT[h1 | 2] + w1 * gradient4DLUT[h1 | 3]);
        }
        else n1 = 0;
        double t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            final int h2 = (hash256(i + i2, j + j2, k + k2, l + l2, seed) & 0xFC);
            t2 *= t2;
            n2 = t2 * t2 * (x2 * gradient4DLUT[h2] + y2 * gradient4DLUT[h2 | 1] + z2 * gradient4DLUT[h2 | 2] + w2 * gradient4DLUT[h2 | 3]);
        }
        else n2 = 0;
        double t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            final int h3 = (hash256(i + i3, j + j3, k + k3, l + l3, seed) & 0xFC);
            t3 *= t3;
            n3 = t3 * t3 * (x3 * gradient4DLUT[h3] + y3 * gradient4DLUT[h3 | 1] + z3 * gradient4DLUT[h3 | 2] + w3 * gradient4DLUT[h3 | 3]);
        }
        else n3 = 0;
        double t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            final int h4 = (hash256(i + 1, j + 1, k + 1, l + 1, seed) & 0xFC);
            t4 *= t4;
            n4 = t4 * t4 * (x4 * gradient4DLUT[h4] + y4 * gradient4DLUT[h4 | 1] + z4 * gradient4DLUT[h4 | 2] + w4 * gradient4DLUT[h4 | 3]);
        }
        else n4 = 0;

        // debug code, for finding what constant should be used for 14.75
//        final double ret =  (n0 + n1 + n2 + n3 + n4) * (14.7279);
//        if(ret < -1 || ret > 1) {
//            System.out.println(ret + " is out of bounds! seed=" + seed + ", x=" + x + ", y=" + y + ", z=" + z + ", w=" + w);
//            return ret * -0.5;
//        }
//        return ret;
        // normal return code
        double ex = Math.exp((n0 + n1 + n2 + n3 + n4) * 6.0 * harshness);
        return (ex - 1.0) / (ex + 1.0);

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
    public double noise(final double x, final double y, final double z, final double w, final double u, final long seed) {
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

        double ex = Math.exp((n0 + n1 + n2 + n3 + n4 + n5) * 6.0 * harshness);
        return (ex - 1.0) / (ex + 1.0);
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

    public double noise(final double x, final double y, final double z,
                               final double w, final double u, final double v, final long seed) {
        double[] gradient6DLUT = grad6d;
        double n0, n1, n2, n3, n4, n5, n6;
        double t = (x + y + z + w + u + v) * F6;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);
        int k = fastFloor(z + t);
        int l = fastFloor(w + t);
        int h = fastFloor(u + t);
        int g = fastFloor(v + t);
        t = (i + j + k + l + h + g) * G6;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double U0 = h - t;
        double V0 = g - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;
        double u0 = u - U0;
        double v0 = v - V0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;
        int rankv = 0;

        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;
        if (x0 > v0) rankx++; else rankv++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;
        if (y0 > v0) ranky++; else rankv++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;
        if (z0 > v0) rankz++; else rankv++;

        if (w0 > u0) rankw++; else ranku++;
        if (w0 > v0) rankw++; else rankv++;

        if (u0 > v0) ranku++; else rankv++;

        int i1 = 4 - rankx >>> 31;
        int j1 = 4 - ranky >>> 31;
        int k1 = 4 - rankz >>> 31;
        int l1 = 4 - rankw >>> 31;
        int h1 = 4 - ranku >>> 31;
        int g1 = 4 - rankv >>> 31;

        int i2 = 3 - rankx >>> 31;
        int j2 = 3 - ranky >>> 31;
        int k2 = 3 - rankz >>> 31;
        int l2 = 3 - rankw >>> 31;
        int h2 = 3 - ranku >>> 31;
        int g2 = 3 - rankv >>> 31;

        int i3 = 2 - rankx >>> 31;
        int j3 = 2 - ranky >>> 31;
        int k3 = 2 - rankz >>> 31;
        int l3 = 2 - rankw >>> 31;
        int h3 = 2 - ranku >>> 31;
        int g3 = 2 - rankv >>> 31;

        int i4 = 1 - rankx >>> 31;
        int j4 = 1 - ranky >>> 31;
        int k4 = 1 - rankz >>> 31;
        int l4 = 1 - rankw >>> 31;
        int h4 = 1 - ranku >>> 31;
        int g4 = 1 - rankv >>> 31;

        int i5 = -rankx >>> 31;
        int j5 = -ranky >>> 31;
        int k5 = -rankz >>> 31;
        int l5 = -rankw >>> 31;
        int h5 = -ranku >>> 31;
        int g5 = -rankv >>> 31;

        double x1 = x0 - i1 + G6;
        double y1 = y0 - j1 + G6;
        double z1 = z0 - k1 + G6;
        double w1 = w0 - l1 + G6;
        double u1 = u0 - h1 + G6;
        double v1 = v0 - g1 + G6;

        double x2 = x0 - i2 + 2 * G6;
        double y2 = y0 - j2 + 2 * G6;
        double z2 = z0 - k2 + 2 * G6;
        double w2 = w0 - l2 + 2 * G6;
        double u2 = u0 - h2 + 2 * G6;
        double v2 = v0 - g2 + 2 * G6;

        double x3 = x0 - i3 + 3 * G6;
        double y3 = y0 - j3 + 3 * G6;
        double z3 = z0 - k3 + 3 * G6;
        double w3 = w0 - l3 + 3 * G6;
        double u3 = u0 - h3 + 3 * G6;
        double v3 = v0 - g3 + 3 * G6;

        double x4 = x0 - i4 + 4 * G6;
        double y4 = y0 - j4 + 4 * G6;
        double z4 = z0 - k4 + 4 * G6;
        double w4 = w0 - l4 + 4 * G6;
        double u4 = u0 - h4 + 4 * G6;
        double v4 = v0 - g4 + 4 * G6;

        double x5 = x0 - i5 + 5 * G6;
        double y5 = y0 - j5 + 5 * G6;
        double z5 = z0 - k5 + 5 * G6;
        double w5 = w0 - l5 + 5 * G6;
        double u5 = u0 - h5 + 5 * G6;
        double v5 = v0 - g5 + 5 * G6;

        double x6 = x0 - 1 + 6 * G6;
        double y6 = y0 - 1 + 6 * G6;
        double z6 = z0 - 1 + 6 * G6;
        double w6 = w0 - 1 + 6 * G6;
        double u6 = u0 - 1 + 6 * G6;
        double v6 = v0 - 1 + 6 * G6;

        n0 = LIMIT6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0 - v0 * v0;
        if (n0 <= 0.0) n0 = 0.0;
        else
        {
            final int hash = hash256(i, j, k, l, h, g, seed) * 6;
            n0 *= n0;
            n0 *= n0 * (gradient6DLUT[hash] * x0 + gradient6DLUT[hash + 1] * y0 + gradient6DLUT[hash + 2] * z0 +
                    gradient6DLUT[hash + 3] * w0 + gradient6DLUT[hash + 4] * u0 + gradient6DLUT[hash + 5] * v0);
        }

        n1 = LIMIT6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1 - v1 * v1;
        if (n1 <= 0.0) n1 = 0.0;
        else
        {
            final int hash = hash256(i + i1, j + j1, k + k1, l + l1, h + h1, g + g1, seed) * 6;
            n1 *= n1;
            n1 *= n1 * (gradient6DLUT[hash] * x1 + gradient6DLUT[hash + 1] * y1 + gradient6DLUT[hash + 2] * z1 +
                    gradient6DLUT[hash + 3] * w1 + gradient6DLUT[hash + 4] * u1 + gradient6DLUT[hash + 5] * v1);
        }
        
        n2 = LIMIT6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2 - v2 * v2;
        if (n2 <= 0.0) n2 = 0.0;
        else
        {
            final int hash = hash256(i + i2, j + j2, k + k2, l + l2, h + h2, g + g2, seed) * 6;
            n2 *= n2;
            n2 *= n2 * (gradient6DLUT[hash] * x2 + gradient6DLUT[hash + 1] * y2 + gradient6DLUT[hash + 2] * z2 +
                    gradient6DLUT[hash + 3] * w2 + gradient6DLUT[hash + 4] * u2 + gradient6DLUT[hash + 5] * v2);
        }

        n3 = LIMIT6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3 - v3 * v3;
        if (n3 <= 0.0) n3 = 0.0;
        else
        {
            final int hash = hash256(i + i3, j + j3, k + k3, l + l3, h + h3, g + g3, seed) * 6;
            n3 *= n3;
            n3 *= n3 * (gradient6DLUT[hash] * x3 + gradient6DLUT[hash + 1] * y3 + gradient6DLUT[hash + 2] * z3 +
                    gradient6DLUT[hash + 3] * w3 + gradient6DLUT[hash + 4] * u3 + gradient6DLUT[hash + 5] * v3);
        }

        n4 = LIMIT6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4 - v4 * v4;
        if (n4 <= 0.0) n4 = 0.0;
        else
        {
            final int hash = hash256(i + i4, j + j4, k + k4, l + l4, h + h4, g + g4, seed) * 6;
            n4 *= n4;
            n4 *= n4 * (gradient6DLUT[hash] * x4 + gradient6DLUT[hash + 1] * y4 + gradient6DLUT[hash + 2] * z4 +
                    gradient6DLUT[hash + 3] * w4 + gradient6DLUT[hash + 4] * u4 + gradient6DLUT[hash + 5] * v4);
        }

        n5 = LIMIT6 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5 - v5 * v5;
        if (n5 <= 0.0) n5 = 0.0;
        else
        {
            final int hash = hash256(i + i5, j + j5, k + k5, l + l5, h + h5, g + g5, seed) * 6;
            n5 *= n5;
            n5 *= n5 * (gradient6DLUT[hash] * x5 + gradient6DLUT[hash + 1] * y5 + gradient6DLUT[hash + 2] * z5 +
                    gradient6DLUT[hash + 3] * w5 + gradient6DLUT[hash + 4] * u5 + gradient6DLUT[hash + 5] * v5);
        }

        n6 = LIMIT6 - x6 * x6 - y6 * y6 - z6 * z6 - w6 * w6 - u6 * u6 - v6 * v6;
        if (n6 <= 0.0) n6 = 0.0;
        else
        {
            final int hash = hash256(i + 1, j + 1, k + 1, l + 1, h + 1, g + 1, seed) * 6;
            n6 *= n6;
            n6 *= n6 * (gradient6DLUT[hash] * x6 + gradient6DLUT[hash + 1] * y6 + gradient6DLUT[hash + 2] * z6 +
                    gradient6DLUT[hash + 3] * w6 + gradient6DLUT[hash + 4] * u6 + gradient6DLUT[hash + 5] * v6);
        }

        double ex = Math.exp((n0 + n1 + n2 + n3 + n4 + n5 + n6) * 10.0 * harshness);
        return (ex - 1.0) / (ex + 1.0);

    }
}
