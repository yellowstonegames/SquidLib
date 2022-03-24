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

    public int seed = 0xD1CEBEEF;
    public double sharpness = 0.75;
    public OctopusNoise() {
    }

    public OctopusNoise(int seed) {
        this.seed = seed;
    }

    public OctopusNoise(long seed) {
        this.seed = (int) (seed ^ seed >>> 32);
    }

    public OctopusNoise(long seed, double sharpness) {
        this(seed);
        this.sharpness = 0.75 * sharpness;
        
    }

    public double octopusNoise(final double x, final double y, int seed) {
        final double a = valueNoise(seed, x, y);
        seed = (seed * 0xDAB ^ 0x9E3779BD);
        final double b = valueNoise(seed, x + y + a, x - y + a);
        return a - b;
//        final double result = (a + b) * 0.5f;
//        final double sharp = sharpness * 2.2;
//        final double diff = 0.5 - result;
//        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
//        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;
    }
    
    /*
x * -0.185127 + y * -0.791704 + z * -0.582180;
x * -0.776796 + y * 0.628752 + z * -0.035464;
x * 0.822283 + y * 0.467437 + z * -0.324582;

x * 0.139640 + y * -0.304485 + z * 0.942226;
x * -0.776796 + y * 0.628752 + z * -0.035464;
x * 0.822283 + y * 0.467437 + z * -0.324582;

x * 0.139640 + y * -0.304485 + z * 0.942226;
x * -0.185127 + y * -0.791704 + z * -0.582180;
x * 0.822283 + y * 0.467437 + z * -0.324582;

x * 0.139640 + y * -0.304485 + z * 0.942226;
x * -0.185127 + y * -0.791704 + z * -0.582180;
x * -0.776796 + y * 0.628752 + z * -0.035464;
     */


    public double octopusNoise(final double x, final double y, final double z, int seed) {
        final double p0 = x;
        final double p1 = x * -0.3333333333333333 + y * 0.9428090415820634;
        final double p2 = x * -0.3333333333333333 + y * -0.4714045207910317 + z * 0.816496580927726;
        final double p3 = x * -0.3333333333333333 + y * -0.4714045207910317 + z * -0.816496580927726;

        //final double p0 = (x + y + z);// * 0.5;
        //final double p1 = x - (y + z);// * 0.25;
        //final double p2 = y - (x + z);// * 0.25;
        //final double p3 = z - (x + y);// * 0.25;
////rotated version of above points on a tetrahedron; the ones below are "more correct" but more complex (and slower?)       
//        final double p0 = x * 0.139640 + y * -0.304485 + z * 0.942226;
//        final double p1 = x * -0.185127 + y * -0.791704 + z * -0.582180;
//        final double p2 = x * -0.776796 + y * 0.628752 + z * -0.035464;
//        final double p3 = x * 0.822283 + y * 0.467437 + z * -0.324582;
        double xin = p1;
        double yin = p2;
        double zin = p3;
        final double a = valueNoise(seed, xin, yin, zin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        final double b = valueNoise(seed, xin + a, yin, zin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        final double c = valueNoise(seed, xin + b, yin, zin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        final double d = valueNoise(seed, xin + c, yin, zin);

        final double result = (a + b + c + d) * 0.25;
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
//        return (result <= 0.5)
//                ? Math.pow(result * 2, 3.0) - 1.0
//                : Math.pow((result - 1) * 2, 3.0) + 1.0;
        final double sharp = sharpness * 3.3;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;

    }

    public double octopusNoise(final double x, final double y, final double z, final double w, int seed) {
        final double p0 = x;
        final double p1 = x * -0.25 + y * 0.9682458365518543;
        final double p2 = x * -0.25 + y * -0.3227486121839514 + z * 0.9128709291752769;
        final double p3 = x * -0.25 + y * -0.3227486121839514 + z * -0.45643546458763834 + w * 0.7905694150420949;
        final double p4 = x * -0.25 + y * -0.3227486121839514 + z * -0.45643546458763834 + w * -0.7905694150420947;
////orthogonal 5-cell, like a right triangle; probably incorrect
        //final double p0 = (x + y + z + w);
        //final double p1 = x - (y + z + w);
        //final double p2 = y - (x + z + w);
        //final double p3 = z - (x + y + w);
        //final double p4 = w - (x + y + z);
////rotated version of above points on a 5-cell; the ones below are "more correct" but more complex (and slower?)       
//        final double p0 = x * 0.139640 + y * -0.304485 + z * 0.942226;
//        final double p1 = x * -0.185127 + y * -0.791704 + z * -0.582180;
//        final double p2 = x * -0.776796 + y * 0.628752 + z * -0.035464;
//        final double p3 = x * 0.822283 + y * 0.467437 + z * -0.324582;
        double xin = p1;
        double yin = p2;
        double zin = p3;
        double win = p4;
        final double a = valueNoise(seed, xin, yin, zin, win);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final double b = valueNoise(seed, xin + a, yin, zin, win);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final double c = valueNoise(seed, xin + b, yin, zin, win);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final double d = valueNoise(seed, xin + c, yin, zin, win);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final double e = valueNoise(seed, xin + d, yin, zin, win);

        final double result = (a + b + c + d + e) * 0.2;
//        return (result <= 0.5)
//                ? Math.pow(result * 2, 4.0) - 1.0
//                : 1.0 - Math.pow((result - 1) * 2, 4.0);
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
        final double sharp = sharpness * 4.4;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;
    }
    public double octopusNoise(final double x, final double y, final double z,
                               final double w, final double u, int seed) {
        final double p0 = x * 0.8157559148337911 + y * 0.5797766823136037;
        final double p1 = x * -0.7314923478726791 + y * 0.6832997137249108;
        final double p2 = x * -0.0208603044412437 + y * -0.3155296974329846 + z * 0.9486832980505138;
        final double p3 = x * -0.0208603044412437 + y * -0.3155296974329846 + z * -0.316227766016838 + w * 0.8944271909999159;
        final double p4 = x * -0.0208603044412437 + y * -0.3155296974329846 + z * -0.316227766016838 + w * -0.44721359549995804 + u * 0.7745966692414833;
        final double p5 = x * -0.0208603044412437 + y * -0.3155296974329846 + z * -0.316227766016838 + w * -0.44721359549995804 + u * -0.7745966692414836;

        double xin = p1;
        double yin = p2;
        double zin = p3;
        double win = p4;
        double uin = p5;
        final double a = valueNoise(seed, xin, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        final double b = valueNoise(seed, xin + a, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        final double c = valueNoise(seed, xin + b, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        final double d = valueNoise(seed, xin + c, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        final double e = valueNoise(seed, xin + d, yin, zin, win, uin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        final double f = valueNoise(seed, xin + e, yin, zin, win, uin);

        final double result = (a + b + c + d + e + f) * 0.16666666666666666;
//        return (result <= 0.5)
//                ? 32.0 * result * result * result * result * result - 1.0
//                : 32.0 * (result - 1) * (result - 1) * (result - 1) * (result - 1) * (result - 1) + 1.0;
        final double sharp = sharpness * 5.5;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;
    }

    public double octopusNoise(final double x, final double y, final double z,
                               final double w, final double u, final double v, int seed) {
        final double p0 = x;
        final double p1 = x * -0.16666666666666666 + y * 0.9860132971832694;
        final double p2 = x * -0.16666666666666666 + y * -0.19720265943665383 + z * 0.9660917830792959;
        final double p3 = x * -0.16666666666666666 + y * -0.19720265943665383 + z * -0.24152294576982394 + w * 0.9354143466934853;
        final double p4 = x * -0.16666666666666666 + y * -0.19720265943665383 + z * -0.24152294576982394 + w * -0.31180478223116176 + u * 0.8819171036881969;
        final double p5 = x * -0.16666666666666666 + y * -0.19720265943665383 + z * -0.24152294576982394 + w * -0.31180478223116176 + u * -0.4409585518440984 + v * 0.7637626158259734;
        final double p6 = x * -0.16666666666666666 + y * -0.19720265943665383 + z * -0.24152294576982394 + w * -0.31180478223116176 + u * -0.4409585518440984 + v * -0.7637626158259732;
        double xin = p1;
        double yin = p2;
        double zin = p3;
        double win = p4;
        double uin = p5;
        double vin = p6;
        final double a = valueNoise(seed, xin, yin, zin, win, uin, vin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        vin = p6;
        final double b = valueNoise(seed, xin + a, yin, zin, win, uin, vin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        vin = p6;
        final double c = valueNoise(seed, xin + b, yin, zin, win, uin, vin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        vin = p6;
        final double d = valueNoise(seed, xin + c, yin, zin, win, uin, vin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        vin = p6;
        final double e = valueNoise(seed, xin + d, yin, zin, win, uin, vin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p6;
        final double f = valueNoise(seed, xin + e, yin, zin, win, uin, vin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p5;
        final double g = valueNoise(seed, xin + f, yin, zin, win, uin, vin);

        final double result = (a + b + c + d + e + f + g) * 0.14285714285714285;
//        return (result <= 0.5)
//                ? Math.pow(result * 2, 6.0) - 1.0
//                : 1.0 - Math.pow((result - 1) * 2, 6.0);
        final double sharp = sharpness * 6.6;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;
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
