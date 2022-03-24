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

import static squidpony.squidmath.ValueNoise.valueNoise;

/**
 * Experimenting with something like {@link FoamNoise} but using fewer value noise calls. Work in progress.
 */
@Beta
public class OctopusNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {
    public static final OctopusNoise instance = new OctopusNoise();

    public ValueNoise n;
    public OctopusNoise() {
        n = new ValueNoise();
    }

    public OctopusNoise(int seed) {
        this();
        n.seed = seed;
    }

    public OctopusNoise(long seed) {
        this();
        n.seed = (int) (seed ^ seed >>> 32);
    }

    public double octopusNoise(final double x, final double y, long seed) {
        final double a = 0.333 * UnifiedNoise.noise(x, y, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n.getNoiseWithSeed(x + y + a, x - y + a, a, seed);
        return b;
    }

    public double octopusNoise(final double x, final double y, final double z, long seed) {
        final double a = 0.333 * UnifiedNoise.noise(x, y, z, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n.getNoiseWithSeed(x + y + a, x - y + a, z - a, a, seed);
        return b;
    }

    public double octopusNoise(final double x, final double y, final double z, final double w, long seed) {
        final double a = 0.333 * UnifiedNoise.noise(x, y, z, w, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n.getNoiseWithSeed(x + y + a, x - y + a, z - a, w - a, a, seed);
        return b;
    }
    public double octopusNoise(final double x, final double y, final double z,
                               final double w, final double u, long seed) {
        final double a = 0.333 * UnifiedNoise.noise(x, y, z, w, u, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n.getNoiseWithSeed(x + y + a, x - y + a, z - a, w - a, u - a, a, seed);
        return b;
    }

    public double octopusNoise(final double x, final double y, final double z,
                               final double w, final double u, final double v, long seed) {
        final double a = 0.333 * UnifiedNoise.noise(x, y, z, w, u, v, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n.getNoiseWithSeed(x + y + a, x - y + a, z - a, w - a, u - a, v - a, a, seed);
        return b;
    }

    @Override
    public double getNoise(double x, double y) {
        return octopusNoise(x, y, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return octopusNoise(x, y, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return octopusNoise(x, y, z, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return octopusNoise(x, y, z, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return octopusNoise(x, y, z, w, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return octopusNoise(x, y, z, w, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u) {
        return octopusNoise(x, y, z, w, u, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        return octopusNoise(x, y, z, w, u, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return octopusNoise(x, y, z, w, u, v, n.seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        return octopusNoise(x, y, z, w, u, v, (int) (seed ^ seed >>> 32));
    }
}
