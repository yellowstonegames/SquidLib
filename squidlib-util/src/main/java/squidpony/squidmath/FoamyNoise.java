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
 * A modification of {@link FoamNoise} that allows specifying a Noise implementation (or implementations) to use in
 * place of the value noise that FoamNoise uses. A typical use might be to try Classic Perlin Noise or Simplex Noise in
 * place of Value Noise, by passing a {@link ClassicNoise} or {@link SeededNoise} to the constructor.
 */
@Beta
public class FoamyNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise5D, Noise.Noise6D {
    public static final FoamyNoise instance = new FoamyNoise();

    public long seed = 0xD1CEDBEEFB077L;
    public double sharpness = 0.75;

    public Noise.Noise2D n2;
    public Noise.Noise3D n3;
    public Noise.Noise4D n4;
    public Noise.Noise5D n5;
    public Noise.Noise6D n6;

    public FoamyNoise() {
        this(ClassicNoise.instance);
    }

    public FoamyNoise(ValueNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        seed = noise.seed;
    }


    public FoamyNoise(ClassicNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        seed = noise.seed;
    }

    public FoamyNoise(SeededNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        this.seed = noise.defaultSeed;
    }

    public FoamyNoise(WeavingNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        this.seed = noise.seed;
    }
    public FoamyNoise(FastNoise noise){
        n2 = noise;
        n3 = noise;
        n4 = noise;
        n5 = noise;
        n6 = noise;
        this.seed = noise.getSeed();
    }
    public FoamyNoise(long seed, Noise.Noise2D noise2D, Noise.Noise3D noise3D, Noise.Noise4D noise4D,
                      Noise.Noise5D noise5D, Noise.Noise6D noise6D){
        this.seed = seed;
        n2 = noise2D;
        n3 = noise3D;
        n4 = noise4D;
        n5 = noise5D;
        n6 = noise6D;
    }

    public FoamyNoise(long seed) {
        this.seed = seed;
    }

    public FoamyNoise(long seed, double sharpness) {
        this(seed);
        this.sharpness = 0.75 * sharpness;
        
    }

    public double foamNoise(final double x, final double y, long seed) {
        final double p0 = x;
        final double p1 = x * -0.5 + y * 0.8660254037844386;
        final double p2 = x * -0.5 + y * -0.8660254037844387;

        double xin = p1;
        double yin = p2;
        //double xin = x * 0.540302 + y * 0.841471; // sin and cos of 1
        //double yin = x * -0.841471 + y * 0.540302;
        final double a = n2.getNoiseWithSeed(xin, yin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p2;
        yin = p0;
        //xin = x * -0.989992 + y * 0.141120; // sin and cos of 3
        //yin = x * -0.141120 + y * -0.989992;
        final double b = n2.getNoiseWithSeed(xin + a * 0.5, yin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        //xin = x * 0.283662 + y * -0.958924; // sin and cos of 5
        //yin = x * 0.958924 + y * 0.283662;
        final double c = n2.getNoiseWithSeed(xin + b * 0.5, yin, seed);
        final double result = (a + b + c) * (0.3333333333333333 * 0.5) + 0.5;
//        return result * result * (6.0 - 4.0 * result) - 1.0;
//        return (result <= 0.5)
//                ? (result * result * 4.0) - 1.0
//                : 1.0 - ((result - 1.0) * (result - 1.0) * 4.0);
        final double sharp = sharpness * 2.2;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;
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


    public double foamNoise(final double x, final double y, final double z, long seed) {
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
        final double a = n3.getNoiseWithSeed(xin, yin, zin, seed);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        final double b = n3.getNoiseWithSeed(xin + a * 0.5, yin, zin, seed);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        final double c = n3.getNoiseWithSeed(xin + b * 0.5, yin, zin, seed);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        final double d = n3.getNoiseWithSeed(xin + c * 0.5, yin, zin, seed);

        final double result = (a + b + c + d) * 0.125 + 0.5;
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
//        return (result <= 0.5)
//                ? Math.pow(result * 2, 3.0) - 1.0
//                : Math.pow((result - 1) * 2, 3.0) + 1.0;
        final double sharp = sharpness * 3.3;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;

    }

    public double foamNoise(final double x, final double y, final double z, final double w, long seed) {
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
        final double a = n4.getNoiseWithSeed(xin, yin, zin, win, seed);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final double b = n4.getNoiseWithSeed(xin + a * 0.5, yin, zin, win, seed);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final double c = n4.getNoiseWithSeed(xin + b * 0.5, yin, zin, win, seed);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final double d = n4.getNoiseWithSeed(xin + c * 0.5, yin, zin, win, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final double e = n4.getNoiseWithSeed(xin + d * 0.5, yin, zin, win, seed);

        final double result = (a + b + c + d + e) * 0.1 + 0.5;
//        return (result <= 0.5)
//                ? Math.pow(result * 2, 4.0) - 1.0
//                : 1.0 - Math.pow((result - 1) * 2, 4.0);
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
        final double sharp = sharpness * 4.4;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;
    }
    public double foamNoise(final double x, final double y, final double z,
                                   final double w, final double u, long seed) {
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
        final double a = n5.getNoiseWithSeed(xin, yin, zin, win, uin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        final double b = n5.getNoiseWithSeed(xin + a * 0.5, yin, zin, win, uin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        final double c = n5.getNoiseWithSeed(xin + b * 0.5, yin, zin, win, uin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        final double d = n5.getNoiseWithSeed(xin + c * 0.5, yin, zin, win, uin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        final double e = n5.getNoiseWithSeed(xin + d * 0.5, yin, zin, win, uin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        final double f = n5.getNoiseWithSeed(xin + e * 0.5, yin, zin, win, uin, seed);

        final double result = (a + b + c + d + e + f) * (0.16666666666666666 * 0.5) + 0.5;
//        return (result <= 0.5)
//                ? 32.0 * result * result * result * result * result - 1.0
//                : 32.0 * (result - 1) * (result - 1) * (result - 1) * (result - 1) * (result - 1) + 1.0;
        final double sharp = sharpness * 5.5;
        final double diff = 0.5 - result;
        final int sign = NumberTools.doubleToHighIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Double.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1.0;
    }

    public double foamNoise(final double x, final double y, final double z,
                                   final double w, final double u, final double v, long seed) {
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
        final double a = n6.getNoiseWithSeed(xin, yin, zin, win, uin, vin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        vin = p6;
        final double b = n6.getNoiseWithSeed(xin + a * 0.5, yin, zin, win, uin, vin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        vin = p6;
        final double c = n6.getNoiseWithSeed(xin + b * 0.5, yin, zin, win, uin, vin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        vin = p6;
        final double d = n6.getNoiseWithSeed(xin + c * 0.5, yin, zin, win, uin, vin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        vin = p6;
        final double e = n6.getNoiseWithSeed(xin + d * 0.5, yin, zin, win, uin, vin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p6;
        final double f = n6.getNoiseWithSeed(xin + e * 0.5, yin, zin, win, uin, vin, seed);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p5;
        final double g = n6.getNoiseWithSeed(xin + f * 0.5, yin, zin, win, uin, vin, seed);

        final double result = (a + b + c + d + e + f + g) * (0.14285714285714285 * 0.5) + 0.5;
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
        return foamNoise(x, y, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return foamNoise(x, y, seed);
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return foamNoise(x, y, z, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return foamNoise(x, y, z, seed);
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return foamNoise(x, y, z, w, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return foamNoise(x, y, z, w, seed);
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u) {
        return foamNoise(x, y, z, w, u, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, long seed) {
        return foamNoise(x, y, z, w, u, seed);
    }
    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return foamNoise(x, y, z, w, u, v, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        return foamNoise(x, y, z, w, u, v, seed);
    }
}
