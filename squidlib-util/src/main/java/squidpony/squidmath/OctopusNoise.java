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

    public long seed = 0xD1CEDBEEFCAFEL;
    
    public Noise.Noise2D n2;
    public Noise.Noise3D n3;
    public Noise.Noise4D n4;
    public Noise.Noise5D n5;
    public Noise.Noise6D n6;

    public OctopusNoise() {
        this(ValueNoise.instance);
    }

    public OctopusNoise(ValueNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        seed = noise.seed;
    }


    public OctopusNoise(ClassicNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        seed = noise.seed;
    }

    public OctopusNoise(UnifiedNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        this.seed = noise.defaultSeed;
    }

    public OctopusNoise(WeavingNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        this.seed = noise.seed;
    }
    public OctopusNoise(FastNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        this.seed = noise.getSeed();
    }
    public OctopusNoise(int seed, Noise.Noise2D noise2D, Noise.Noise3D noise3D, Noise.Noise4D noise4D,
                      Noise.Noise5D noise5D, Noise.Noise6D noise6D){
        this.seed = seed;
        n2 = noise2D;
        n3 = noise3D;
        n4 = noise4D;
        n5 = noise5D;
        n6 = noise6D;
    }


    public OctopusNoise(int seed) {
        this();
        this.seed = seed;
    }

    public OctopusNoise(long seed) {
        this();
        this.seed = (int) (seed ^ seed >>> 32);
    }

    public double octopusNoise(final double x, final double y, long seed) {
        final double a = n2.getNoiseWithSeed(x, y, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n2.getNoiseWithSeed(x + y + a, x - y + a, seed);
        return b;
    }
    
    public double octopusNoise(final double x, final double y, final double z, long seed) {
        final double a = n3.getNoiseWithSeed(x, y, z, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n3.getNoiseWithSeed(x + y + a, x - y + a, z - a, seed);
        return b;
    }

    public double octopusNoise(final double x, final double y, final double z, final double w, long seed) {
        final double a = n4.getNoiseWithSeed(x, y, z, w, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n4.getNoiseWithSeed(x + y + a, x - y + a, z - a, w - a, seed);
        return b;
    }
    public double octopusNoise(final double x, final double y, final double z,
                               final double w, final double u, long seed) {
        final double a = n5.getNoiseWithSeed(x, y, z, w, u, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n5.getNoiseWithSeed(x + y + a, x - y + a, z - a, w - a, u - a, seed);
        return b;
    }

    public double octopusNoise(final double x, final double y, final double z,
                               final double w, final double u, final double v, long seed) {
        final double a = n6.getNoiseWithSeed(x, y, z, w, u, v, seed);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = n6.getNoiseWithSeed(x + y + a, x - y + a, z - a, w - a, u - a, v - a, seed);
        return b;
    }

    @Override
    public double getNoise(double x, double y) {
        return octopusNoise(x, y, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return octopusNoise(x, y, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return octopusNoise(x, y, z, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return octopusNoise(x, y, z, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return octopusNoise(x, y, z, w, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return octopusNoise(x, y, z, w, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u) {
        return octopusNoise(x, y, z, w, u, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        return octopusNoise(x, y, z, w, u, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return octopusNoise(x, y, z, w, u, v, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        return octopusNoise(x, y, z, w, u, v, (int) (seed ^ seed >>> 32));
    }
}
