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

/**
 * An arbitrary-dimensional noise generator; it's not suitable for real-time use, but could be very useful when used
 * with unconventional axes, particularly during level generation. It produces smooth, non-artifact-prone gradient noise
 * with lots of rounded "raindrops on a window" shapes. It is very biased toward central results (near 0.0) and only
 * rarely returns results near -1.0 or 1.0, the extremes of its range.
 * <br>
 * <a href="https://i.imgur.com/WR062LY.png">Sample of 2D MitchellNoise</a>.
 * <br>
 * Created by Tommy Ettinger on 11/6/2019 using
 * <a href="https://twitter.com/DonaldM38768041/status/1191771541354078208">code by Donald Mitchell</a>.
 * @see PhantomNoise PhantomNoise also produces arbitrary-dimensional noise, is faster, and is less centrally-biased
 */
public class MitchellNoise {
    public final int MAX_DIM;
    //	 public Vec4[] grad;
    public final Vec4[] coef;
    public final int[] floors;
    public long seed;

    public MitchellNoise() {
        MAX_DIM = 20;
        seed = 0x1234567890ABCDEFL;
//		  grad = new Vec4[MAX_DIM];
        coef = new Vec4[MAX_DIM];
        floors = new int[MAX_DIM];
        for (int i = 0; i < MAX_DIM; i++) {
            coef[i] = new Vec4();
        }
    }

    public MitchellNoise(long seed, int maxDimension) {
        this.seed = seed;
        MAX_DIM = Math.max(1, maxDimension);
//		 grad = new Vec4[MAX_DIM];
        coef = new Vec4[MAX_DIM];
        floors = new int[MAX_DIM];
        for (int i = 0; i < MAX_DIM; i++) {
            coef[i] = new Vec4();
        }
    }

    public static class Vec4 {
        public double x, y, z, w;

        public Vec4() {
            this(0, 0, 0, 0);
        }

        public Vec4(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public double dot(Vec4 o) {
            return x * o.x + y * o.y + z * o.z + w * o.w;
        }

        public double dot(double ox, double oy, double oz, double ow) {
            return x * ox + y * oy + z * oz + w * ow;
        }
    }

    public static long latticeValue(long lat) {
        lat = lat * 0xE95E1DD17D35800DL + 0x9E3779B97F4A7C15L;
        return (lat << lat | lat >>> -lat);
    }

    public double spline(int dim, long lattice) {
        int floor;
        long h0, h1, h2, h3;
        double kx, ky, kz, kw;

        floor = floors[dim];
        h0 = latticeValue(floor - 1L + lattice);
        h1 = latticeValue(floor + lattice);
        h2 = latticeValue(floor + 1L + lattice);
        h3 = latticeValue(floor + 2L + lattice);

        if (dim == 0) {
            kx = h0 >> 13;
            ky = h1 >> 13;
            kz = h2 >> 13;
            kw = h3 >> 13;
        } else {
            --dim;
            kx = spline(dim, h0);
            ky = spline(dim, h1);
            kz = spline(dim, h2);
            kw = spline(dim, h3);
            ++dim;
        }
        return coef[dim].dot(kx, ky, kz, kw);
    }

    public double arbitraryNoise(long seed, double... points) {
        int floor, size = points.length;
        double x, gain = 1.0;
        for (int i = 0, dim = size - 1; i < size; i++, dim--) {
            gain *= 1.27;
            floor = Noise.fastFloor(points[i]);
            x = points[i] - floor;
            floors[dim] = floor;
            coef[dim].x = 1.0/6.0 + x*(x*(0.5 - (1.0/6.0)*x)- 0.5);
            coef[dim].y = 2.0/3.0 + x*x*(0.5 * x - 1.0);
            coef[dim].z = 1.0/6.0 + 0.5*x*(1.0 + x*(1.0 - x));
            coef[dim].w = 1.0/6.0 * x*x*x;
        }
        return gain * spline(size-1, seed) * 0.9 * 0x1p-51;
    }
}
