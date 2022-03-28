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
 * Experimenting with something like {@link FoamNoise} but using fewer value noise calls. Work in progress.
 */
@Beta
public class OctopusNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {
    public static final OctopusNoise instance = new OctopusNoise();

    public FastNoise n;
    public OctopusNoise() {
        this(0x12345678);
    }

    public OctopusNoise(int seed) {
        n = new FastNoise(seed, 0x1p-5f, FastNoise.VALUE, 1);
        n.setInterpolation(FastNoise.CUBIC);
    }

    public OctopusNoise(long seed) {
        n = new FastNoise((int) (seed ^ seed >>> 32), 0x1p-5f, FastNoise.VALUE, 1);
        n.setInterpolation(FastNoise.CUBIC);
    }

    public float octopusNoise(final float x, final float y, int seed) {
        final float a = n.singleValue(seed, x, y) * 0.250f + 0.500f;
        return n.singleValue(seed ^ 0x9E3779BD, x - a, y - a, a);
    }

    public float octopusNoise(final float x, final float y, final float z, int seed) {
        final float a = n.singleValue(seed, x, y, z) * 0.250f + 0.500f;
        return n.singleValue(seed ^ 0x9E3779BD, x - a, y - a, z - a, a);
    }

    public float octopusNoise(final float x, final float y, final float z, final float w, int seed) {
        final float a = n.singleValue(seed, x, y, z, w, seed) * 0.250f + 0.500f;
        return n.singleValue(seed ^ 0x9E3779BD, x - a, y - a, z - a, w - a, a);
    }
    public float octopusNoise(final float x, final float y, final float z,
                               final float w, final float u, int seed) {
        final float a = n.singleValue(seed, x, y, z, w, u) * 0.250f + 0.500f;
        return n.singleValue(seed ^ 0x9E3779BD, x - a, y - a, z - a, w - a, u - a, a);
    }

    public float octopusNoise(final float x, final float y, final float z,
                               final float w, final float u, final float v, int seed) {
        final float a = n.singleValue(seed, x, y, z, w, u, v) * 0.250f + 0.500f;
//        seed = (seed * 0xDAB ^ 0x9E3779BD);
        return n.singleValue(seed ^ 0x9E3779BD, x - a, y - a, z - a, w - a, u - a, v - a, a);
    }

    @Override
    public double getNoise(double x, double y) {
        return octopusNoise((float)x, (float)y, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return octopusNoise((float)x, (float)y, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return octopusNoise((float)x, (float)y, (float)z, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return octopusNoise((float)x, (float)y, (float)z, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return octopusNoise((float)x, (float)y, (float)z, (float)w, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return octopusNoise((float)x, (float)y, (float)z, (float)w, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u) {
        return octopusNoise((float)x, (float)y, (float)z, (float)w, (float)u, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        return octopusNoise((float)x, (float)y, (float)z, (float)w, (float)u, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return octopusNoise((float)x, (float)y, (float)z, (float)w, (float)u, (float)v, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        return octopusNoise((float)x, (float)y, (float)z, (float)w, (float)u, (float)v, (int) (seed ^ seed >>> 32));
    }
}
