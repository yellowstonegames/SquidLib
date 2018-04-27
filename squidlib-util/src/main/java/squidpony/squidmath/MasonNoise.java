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

import static squidpony.squidmath.Noise.fastFloor;

/**
 * Noise functions that delegate work to the best-suited noise type for the requested dimensionality, plus some extra
 * functions that affect a large multi-dimensional area at once. This will use {@link FastNoise} for 2D and 3D noise,
 * {@link WhirlingNoise} for 4D noise, and {@link SeededNoise} for 6D noise. It uses its own, possibly sub-par
 * implementation for {@link #addNoiseField(float[][], float, float, float, float, long, float, float)} and
 * {@link #addNoiseField(float[][][], float, float, float, float, float, float, long, float, float)}.
 * <br>
 * The name comes from the Freemasons, who as a secret society, are very good at behind-the-scenes work. MasonNoise also
 * sounds a lot like MerlinNoise, which sounds a lot like PerlinNoise and WhirlingNoise.
 */
public class MasonNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {

    protected final long defaultSeed;
    public static final MasonNoise instance = new MasonNoise();

    public MasonNoise() {
        this(0x1337BEEFBE2A22L);
    }
    public MasonNoise(long seed)
    {
        defaultSeed = seed;
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

    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        return noise(x, y, seed);
    }
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        return noise(x, y, z, seed);
    }
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        return noise(x, y, z, w, seed);
    }
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final double u, final double v, final long seed) {
        return noise(x, y, z, w, u, v, seed);
    }

    /*
    protected static final float F2 = 0.36602540378443864676372317075294f,
            G2 = 0.21132486540518711774542560974902f,
            F3 = 1f / 3f,
            G3 = 1f / 6f,
            F4 = (float) (Math.sqrt(5.0) - 1.0) / 4f,
            G4 = (float)(5.0 - Math.sqrt(5.0)) / 20f,
            F6 = (float)(Math.sqrt(7.0) - 1.0) / 6f,
            G6 = F6 / (float)(1.0 + 6.0 * F6),
            LIMIT4 = 0.62f,
            LIMIT6 = 0.86f;*/
            /*
            sideLength = (float)Math.sqrt(6.0) / (6f * F6 + 1f),
            a6 = (float)(Math.sqrt((sideLength * sideLength)
                    - ((sideLength * 0.5) * (sideLength * 0.5f)))),
            cornerFace = (float)Math.sqrt(a6 * a6 + (a6 * 0.5) * (a6 * 0.5)),
            cornerFaceSq = cornerFace * cornerFace,
            valueScaler = 9.5f
             */

    //Math.pow(5.0, -0.5) * (Math.pow(5.0, -3.5) * 100 + 13),

    /**
     * Delegates to {@link FastNoise#getNoiseWithSeed(double, double, long)}.
     * <br>
     * @param x X input
     * @param y Y input
     * @param seed any long; will be used to completely alter the noise
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final long seed) {
        return FastNoise.instance.getNoiseWithSeed(x, y, seed);
    }
//
//    public static double noiseAlt(final double x, final double y, final long seed) {
//        return noiseAlt((float)x, (float)y, seed);
//    }

    public static float randomize(long state, final long jump)
    {
        state = (((state = (state - jump) * jump) >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return NumberTools.intBitsToFloat(((int)(state ^ (state >>> 31)) >>> 9) | 0x40000000) - 3f;

        /*
        state = (state - jump) * jump;
        state ^= state >>> (4 + (state >>> 28));
        return NumberTools.intBitsToFloat(((((state *= 277803737) >>> 22) ^ state) >>> 9) | 0x40000000) - 3f;
        */
    }

    public static int randomInt(long state, final long jump) {
        state = (((state = (state - jump) * jump) >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return (int) (state ^ (state >>> 31));

        /*
        state = (state - jump) * jump;
        state ^= state >>> (4 + (state >>> 28));
        return ((state *= 277803737) >>> 22) ^ state;
        */
    }
    public static int randomInt(long state) {
        state = (((state += 0x9E3779B97F4A7C15L) >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return (int) (state ^ (state >>> 31));
        /*
        state ^= state >>> (4 + (state >>> 28));
        return ((state *= 277803737) >>> 22) ^ state;
        */
    }
    /**
     * Pair of Cubic (Hermite) Flat Interpolations; for x from 0 to 1, returns results from 0 to 0 on an S-curve, with
     * crests and valleys no more than 0.097f above or below 0.
     * @param x a float from 0 to 1
     * @return a float from 0 to 0.97, easing in to 0 at the endpoints on a curve
     */
    private static float carp(final float x) { return x * (x * (x - 1) + (1 - x) * (1 - x)); }
    /**
     * Pair of Cubic (Hermite) Flat Interpolations plus linear interpolation; for x from 0 to 1, returns results from 0
     * to 1 on an S-curve.
     * @param x a float from 0 to 1
     * @return a float from 0 to 1, easing in to 0 or 1 at those endpoints on a curve
     */
    private static float carp2(final float x) { return x - x * (x * (x - 1) + (1 - x) * (1 - x)); }


    private static float arclerp(final float x)
    {
        //(fn [x] (let [inv (bit-or 1 (int (* x -1.9999999999999998))) ix (- (* inv x) (bit-shift-right inv 1))] (- 0.5 (* inv (sqrt (- 0.25 (* ix ix)))))))
        return (x > 0.5f) ? 0.5f + (float) Math.sqrt(0.25f - (1f - x) * (1f - x)) : 0.5f - (float) Math.sqrt(0.25f - x * x);
        //(+ 0.5 (sqrt (- 0.25 (* (- 1.0 x) (- 1.0 x)))))
        //(- 0.5 (sqrt (- 0.25 (* x x))))
    }
    /*
     * Linearly interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float interpolate(final float start, final float end, final float a)
    {
        return (1f - a) * start + a * end;
    }


    private static final long xJump = 0x9E3779BE3779B9L, yJump = 0xBFDAE4FFDAE4F7L, zJump = 0xF35692B35692B5L;
//    public static float noise(final float xPos, final float yPos, final long seed) {
//        float x = xPos * (NumberTools.randomFloatCurved(seed - 1) + 1f) + yPos * (NumberTools.randomSignedFloat(seed & 0xA5A5A5A5A5A5A5A5L)),
//                y = yPos * (NumberTools.randomFloatCurved(seed - 2) + 1f) + xPos * (NumberTools.randomSignedFloat(seed & 0x5A5A5A5A5A5A5A5AL));
//        final long
//                xx0 = longFloor(x),
//                yy0 = longFloor(y),
//                ry0 = LightRNG.determine(yy0 + seed), ry1 = LightRNG.determine(yy0 + 1 + seed);
//        final float
//                dx = carp2(x - xx0), dy = carp2(y - yy0),
//                rx0y0 = LightRNG.determineFloat(xx0 + ry0),
//                rx0y1 = LightRNG.determineFloat(xx0 + ry1),
//                rx1y0 = LightRNG.determineFloat(xx0 + 1 + ry0),
//                rx1y1 = LightRNG.determineFloat(xx0 + 1 + ry1);
//        return Noise.lerp(Noise.lerp(rx0y0, rx1y0, dx), Noise.lerp(rx0y1, rx1y1, dx), dy) * 2f - 1f;
//    }
//    public static float noiseAlt(final float x, final float y, final long seed) {
//        final long
//                xx0 = longFloor(x), yy0 = longFloor(y),
//                ry0 = LightRNG.determine(yy0 + seed), ry1 = LightRNG.determine(yy0 + 1 + seed);
//        final float
//                dx = carp2(x - xx0), dy = carp2(y - yy0),
//                rx0y0 = LightRNG.determineFloat(xx0 + ry0),
//                rx0y1 = LightRNG.determineFloat(xx0 + ry1),
//                rx1y0 = LightRNG.determineFloat(xx0 + 1 + ry0),
//                rx1y1 = LightRNG.determineFloat(xx0 + 1 + ry1);
//        return Noise.cerp(Noise.cerp(rx0y0, rx1y0, dx), Noise.cerp(rx0y1, rx1y1, dx), dy) * 2f - 1f;
//    }

    private static final IntVLA columns = new IntVLA(128), // x
            rows = new IntVLA(128), // y
            layers = new IntVLA(128); // z
            //wos = new IntVLA(128), // w
            //ubs = new IntVLA(128), // u
            //vys = new IntVLA(128); // v
    public static void addNoiseField(final float[][] target, final float startX, final float startY,
                                     final float endX, final float endY, final long seed, final float multiplier, final float shrink) {
        int callWidth = target.length, callHeight = target[0].length,
                alter = randomInt(seed, xJump), alter2 = randomInt(~seed, yJump),
                xx0, yy0, rxx0, ryy0, rxx1, ryy1, cx, cy;
        final float startX2 = startX * shrink, startY2 = startY * shrink, endX2 = endX * shrink, endY2 = endY * shrink,
                stretchX = (1f + randomize(alter, yJump) * 0.25f) * (alter2 >> 31 | 1),
                stretchY = (1f + randomize(alter2, xJump) * 0.25f) * (alter >> 31 | 1),
                stepX = (endX2 - startX2) / callWidth, stepY = (endY2 - startY2) / callHeight,
                minimumX = Math.min(startX2 * stretchX - startY2 * stretchY * 0.25f,
                        Math.min(startX2 * stretchX - endY2 * stretchY * 0.25f,
                                Math.min(endX2 * stretchX - startY2 * stretchY * 0.25f, endX2 * stretchX - endY2 * stretchY * 0.25f))),
                maximumX = Math.max(startX2 * stretchX - startY2 * stretchY * 0.25f,
                        Math.max(startX2 * stretchX - endY2 * stretchY * 0.25f,
                                Math.max(endX2 * stretchX - startY2 * stretchY * 0.25f, endX2 * stretchX - endY2 * stretchY * 0.25f))),
                minimumY = Math.min(startY2 * stretchX - startX2 * stretchY * 0.25f,
                        Math.min(startY2 * stretchX - endX2 * stretchY * 0.25f,
                                Math.min(endY2 * stretchX - startX2 * stretchY * 0.25f, endY2 * stretchX - endX2 * stretchY * 0.25f))),
                maximumY = Math.max(startY2 * stretchX - startX2 * stretchY * 0.25f,
                        Math.max(startY2 * stretchX - endX2 * stretchY * 0.25f,
                                Math.max(endY2 * stretchX - startX2 * stretchY * 0.25f, endY2 * stretchX - endX2 * stretchY * 0.25f)));
        float dx, dy, ax, ay;
        final int startFloorX = fastFloor(minimumX),
                startFloorY = fastFloor(minimumY),
                //bonusWidth = fastFloor(adjEndX - adjX) + 3, bonusHeight = fastFloor(adjEndY - adjY) + 3,
                spaceWidth = fastFloor(maximumX - minimumX) + 4, spaceHeight = fastFloor(maximumY - minimumY) + 4
                ;//bonusWidth = Math.abs(startFloorX - fastFloor(minimumX)), bonusHeight = Math.abs(startFloorY - fastFloor(minimumY));

        columns.clear();
        rows.clear();
        columns.ensureCapacity(spaceWidth);
        rows.ensureCapacity(spaceHeight);
        for (int x = 0, r = startFloorX * alter; x < spaceWidth; x++, r += alter) {
            columns.add(randomInt(r));
        }
        for (int y = 0, r = startFloorY * alter2; y < spaceHeight; y++, r += alter2) {
            rows.add(randomInt(r));
        }
        cx = 0;
        for (float x = startX2; cx < callWidth; x += stepX, cx++) {
            cy = 0;
            for (float y = startY2; cy < callHeight; y += stepY, cy++) {
                ax = x * stretchX - y * stretchY * 0.25f;
                ay = y * stretchX - x * stretchY * 0.25f;
                xx0 = fastFloor(ax);
                yy0 = fastFloor(ay);
                rxx0 = columns.get(xx0 - startFloorX+1);
                ryy0 = rows.get(yy0 - startFloorY+1);
                rxx1 = columns.get(xx0 + 1 - startFloorX+1);
                ryy1 = rows.get(yy0 + 1 - startFloorY+1);
                dx = carp2(ax - xx0);
                dy = carp2(ay - yy0);
                target[cx][cy] += NumberTools.bounce(
                        (rxx0 * ryy0 >> 16) * (1f - dx) * (1f - dy)
                                + (rxx0 * ryy1 >> 16) * (1f - dx) * dy
                                + (rxx1 * ryy0 >> 16) * dx * (1f - dy)
                                + (rxx1 * ryy1 >> 16) * dx * dy
                                + 163840f) * multiplier;
            }
        }
    }
    public static void addNoiseField(final float[][][] target,
                                     final float startX, final float startY, final float startZ,
                                     final float endX, final float endY, final float endZ,
                                     final long seed, final float multiplier, final float shrink) {
        int callWidth = target[0].length, callHeight = target[0][0].length, callDepth = target.length,
                alter = randomInt(seed, xJump), alter2 = randomInt(~seed, yJump), alter3 = randomInt(alter + alter2, zJump),
                xx0, yy0, zz0, rxx0, ryy0, rzz0, rxx1, ryy1, rzz1, cx, cy, cz, vx0y0, vx1y0, vx0y1, vx1y1;
        final float startX2 = startX * shrink, startY2 = startY * shrink, startZ2 = startZ * shrink,
                endX2 = endX * shrink, endY2 = endY * shrink, endZ2 = endZ * shrink,
                stretchX = (1f + randomize(alter, yJump) * 0.25f) * (alter3 >> 31 | 1),
                stretchY = (1f + randomize(alter2, zJump) * 0.25f) * (alter >> 31 | 1),
                stretchZ = (1f + randomize(alter3, xJump) * 0.25f) * (alter2 >> 31 | 1),
                stepX = (endX2 - startX2) / callWidth, stepY = (endY2 - startY2) / callHeight, stepZ = (endZ2 - startZ2) / callDepth,
                minimumX = Math.min(startX2 * stretchX - startY2 * stretchY * 0.15625f + startZ2 * stretchZ * 0.09375f,
                        Math.min(startX2 * stretchX - endY2 * stretchY * 0.15625f + startZ2 * stretchZ * 0.09375f,
                                Math.min(endX2 * stretchX - startY2 * stretchY * 0.15625f + startZ2 *stretchZ * 0.09375f,
                                        Math.min(endX2 * stretchX - endY2 * stretchY * 0.15625f + startZ2 * stretchZ * 0.09375f,
                                                Math.min(startX2 * stretchX - startY2 * stretchY * 0.15625f + endZ2 * stretchZ * 0.09375f,
                                                        Math.min(startX2 * stretchX - endY2 * stretchY * 0.15625f + endZ2 * stretchZ * 0.09375f,
                                                                Math.min(endX2 * stretchX - startY2 * stretchY * 0.15625f + endZ2 *stretchZ * 0.09375f,
                                                                        endX2 * stretchX - endY2 * stretchY * 0.15625f + endZ2 * stretchZ * 0.09375f))))))),
                maximumX = Math.max(startX2 * stretchX - startY2 * stretchY * 0.15625f + startZ2 * stretchZ * 0.09375f,
                        Math.max(startX2 * stretchX - endY2 * stretchY * 0.15625f + startZ2 * stretchZ * 0.09375f,
                                Math.max(endX2 * stretchX - startY2 * stretchY * 0.15625f + startZ2 *stretchZ * 0.09375f,
                                        Math.max(endX2 * stretchX - endY2 * stretchY * 0.15625f + startZ2 * stretchZ * 0.09375f,
                                                Math.max(startX2 * stretchX - startY2 * stretchY * 0.15625f + endZ2 * stretchZ * 0.09375f,
                                                        Math.max(startX2 * stretchX - endY2 * stretchY * 0.15625f + endZ2 * stretchZ * 0.09375f,
                                                                Math.max(endX2 * stretchX - startY2 * stretchY * 0.15625f + endZ2 *stretchZ * 0.09375f,
                                                                        endX2 * stretchX - endY2 * stretchY * 0.15625f + endZ2 * stretchZ * 0.09375f))))))),
                minimumY = Math.min(startY2 * stretchX - startZ2 * stretchY * 0.15625f + startX2 * stretchZ * 0.09375f,
                        Math.min(startY2 * stretchX - endZ2 * stretchY * 0.15625f + startX2 * stretchZ * 0.09375f,
                                Math.min(endY2 * stretchX - startZ2 * stretchY * 0.15625f + startX2 *stretchZ * 0.09375f,
                                        Math.min(endY2 * stretchX - endZ2 * stretchY * 0.15625f + startX2 * stretchZ * 0.09375f,
                                                Math.min(startY2 * stretchX - startZ2 * stretchY * 0.15625f + endX2 * stretchZ * 0.09375f,
                                                        Math.min(startY2 * stretchX - endZ2 * stretchY * 0.15625f + endX2 * stretchZ * 0.09375f,
                                                                Math.min(endY2 * stretchX - startZ2 * stretchY * 0.15625f + endX2 *stretchZ * 0.09375f,
                                                                        endY2 * stretchX - endZ2 * stretchY * 0.15625f + endX2 * stretchZ * 0.09375f))))))),
                maximumY = Math.max(startY2 * stretchX - startZ2 * stretchY * 0.15625f + startX2 * stretchZ * 0.09375f,
                        Math.max(startY2 * stretchX - endZ2 * stretchY * 0.15625f + startX2 * stretchZ * 0.09375f,
                                Math.max(endY2 * stretchX - startZ2 * stretchY * 0.15625f + startX2 *stretchZ * 0.09375f,
                                        Math.max(endY2 * stretchX - endZ2 * stretchY * 0.15625f + startX2 * stretchZ * 0.09375f,
                                                Math.max(startY2 * stretchX - startZ2 * stretchY * 0.15625f + endX2 * stretchZ * 0.09375f,
                                                        Math.max(startY2 * stretchX - endZ2 * stretchY * 0.15625f + endX2 * stretchZ * 0.09375f,
                                                                Math.max(endY2 * stretchX - startZ2 * stretchY * 0.15625f + endX2 *stretchZ * 0.09375f,
                                                                        endY2 * stretchX - endZ2 * stretchY * 0.15625f + endX2 * stretchZ * 0.09375f))))))),
                minimumZ = Math.min(startZ2 * stretchX - startX2 * stretchY * 0.15625f + startY2 * stretchZ * 0.09375f,
                        Math.min(startZ2 * stretchX - endX2 * stretchY * 0.15625f + startY2 * stretchZ * 0.09375f,
                                Math.min(endZ2 * stretchX - startX2 * stretchY * 0.15625f + startY2 *stretchZ * 0.09375f,
                                        Math.min(endZ2 * stretchX - endX2 * stretchY * 0.15625f + startY2 * stretchZ * 0.09375f,
                                                Math.min(startZ2 * stretchX - startX2 * stretchY * 0.15625f + endY2 * stretchZ * 0.09375f,
                                                        Math.min(startZ2 * stretchX - endX2 * stretchY * 0.15625f + endY2 * stretchZ * 0.09375f,
                                                                Math.min(endZ2 * stretchX - startX2 * stretchY * 0.15625f + endY2 *stretchZ * 0.09375f,
                                                                        endZ2 * stretchX - endX2 * stretchY * 0.15625f + endY2 * stretchZ * 0.09375f))))))),
                maximumZ = Math.max(startZ2 * stretchX - startX2 * stretchY * 0.15625f + startY2 * stretchZ * 0.09375f,
                        Math.max(startZ2 * stretchX - endX2 * stretchY * 0.15625f + startY2 * stretchZ * 0.09375f,
                                Math.max(endZ2 * stretchX - startX2 * stretchY * 0.15625f + startY2 *stretchZ * 0.09375f,
                                        Math.max(endZ2 * stretchX - endX2 * stretchY * 0.15625f + startY2 * stretchZ * 0.09375f,
                                                Math.max(startZ2 * stretchX - startX2 * stretchY * 0.15625f + endY2 * stretchZ * 0.09375f,
                                                        Math.max(startZ2 * stretchX - endX2 * stretchY * 0.15625f + endY2 * stretchZ * 0.09375f,
                                                                Math.max(endZ2 * stretchX - startX2 * stretchY * 0.15625f + endY2 *stretchZ * 0.09375f,
                                                                        endZ2 * stretchX - endX2 * stretchY * 0.15625f + endY2 * stretchZ * 0.09375f)))))));


        float dx, dy, dz, ax, ay, az, mx0y0, mx1y0, mx0y1, mx1y1;
        final int startFloorX = fastFloor(minimumX),
                startFloorY = fastFloor(minimumY),
                startFloorZ = fastFloor(minimumZ),
                //bonusWidth = fastFloor(adjEndX - adjX) + 3, bonusHeight = fastFloor(adjEndY - adjY) + 3,
                spaceWidth = fastFloor(maximumX - minimumX) + 4,
                spaceHeight = fastFloor(maximumY - minimumY) + 4,
                spaceDepth = fastFloor(maximumZ - minimumZ) + 4;//bonusWidth = Math.abs(startFloorX - fastFloor(minimumX)), bonusHeight = Math.abs(startFloorY - fastFloor(minimumY));

        columns.clear();
        rows.clear();
        layers.clear();
        columns.ensureCapacity(spaceWidth);
        rows.ensureCapacity(spaceHeight);
        layers.ensureCapacity(spaceDepth);
        for (int x = 0, r = startFloorX * alter; x < spaceWidth; x++, r += alter) {
            columns.add(randomInt(r));
        }
        for (int y = 0, r = startFloorY * alter2; y < spaceHeight; y++, r += alter2) {
            rows.add(randomInt(r));
        }
        for (int z = 0, r = startFloorZ * alter3; z < spaceDepth; z++, r += alter3) {
            layers.add(randomInt(r));
        }
        cz = 0;
        float[][] tt;
        float[] t;
        for (float z = startZ2; cz < callDepth; z+= stepZ, cz++) {
            tt = target[cz];
            cx = 0;
            for (float x = startX2; cx < callWidth; x += stepX, cx++) {
                cy = 0;
                t = tt[cx];
                for (float y = startY2; cy < callHeight; y += stepY, cy++) {
                    ax = x * stretchX - y * stretchY * 0.15625f + z * stretchZ * 0.09375f;
                    ay = y * stretchX - z * stretchY * 0.15625f + x * stretchZ * 0.09375f;
                    az = z * stretchX - x * stretchY * 0.15625f + y * stretchZ * 0.09375f;
                    xx0 = fastFloor(ax);
                    yy0 = fastFloor(ay);
                    zz0 = fastFloor(az);
                    rxx0 = columns.get(xx0 - startFloorX + 1);
                    ryy0 = rows.get(yy0 - startFloorY + 1);
                    rzz0 = layers.get(zz0 - startFloorZ + 1);
                    rxx1 = columns.get(xx0 + 1 - startFloorX + 1);
                    ryy1 = rows.get(yy0 + 1 - startFloorY + 1);
                    rzz1 = layers.get(zz0 + 1 - startFloorZ + 1);
                    dx = carp2(ax - xx0);
                    dy = carp2(ay - yy0);
                    dz = carp2(az - zz0);
                    vx0y0 = rxx0 * ryy0;
                    vx0y1 = rxx0 * ryy1;
                    vx1y0 = rxx1 * ryy0;
                    vx1y1 = rxx1 * ryy1;
                    mx0y0 = (1f - dx) * (1f - dy);
                    mx0y1 = (1f - dx) * dy;
                    mx1y0 = dx * (1f - dy);
                    mx1y1 = dx * dy;
                    t[cy] += NumberTools.bounce(
                            (vx0y0 * rzz0 >> 16) * mx0y0 * (1f - dz)
                                    + (vx0y0 * rzz1 >> 16) * mx0y0 * dz
                                    + (vx0y1 * rzz0 >> 16) * mx0y1 * (1f - dz)
                                    + (vx0y1 * rzz1 >> 16) * mx0y1 * dz
                                    + (vx1y0 * rzz0 >> 16) * mx1y0 * (1f - dz)
                                    + (vx1y0 * rzz1 >> 16) * mx1y0 * dz
                                    + (vx1y1 * rzz0 >> 16) * mx1y1 * (1f - dz)
                                    + (vx1y1 * rzz1 >> 16) * mx1y1 * dz
                                    + 163840f) * multiplier;
                }
            }
        }
    }

    /**
     * Delegates to {@link FastNoise#getNoiseWithSeed(double, double, double, long)}.
     * <br>
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param seed any long; will be used to completely alter the noise
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final double z, final long seed) {
        return FastNoise.instance.getNoiseWithSeed(x, y, z, seed);
    }

    /**
     * Delegates to {@link WhirlingNoise#noise(double, double, double, double, long)}.
     * <br>
     * 4D simplex noise. Unlike {@link PerlinNoise}, uses its parameters verbatim, so the scale of the result will be
     * different when passing the same arguments to {@link PerlinNoise#noise(double, double, double, double)} and this
     * method. Roughly 20-25% faster than the equivalent method in PerlinNoise.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimensional)
     * @param seed any long; will be used to completely alter the noise
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final double z, final double w, final long seed) {
        return WhirlingNoise.noise(x, y, z, w, seed); 
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, double, double, double, long)}.
     * <br>
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimensional)
     * @param u U input (fifth-dimensional)
     * @param v V input (sixth-dimensional)
     * @param seed any long; will be used to completely alter the noise
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final double z,
                               final double w, final double u, final double v, final long seed) {
        return SeededNoise.noise(x, y, z, w, u, v, seed);
    }
}
