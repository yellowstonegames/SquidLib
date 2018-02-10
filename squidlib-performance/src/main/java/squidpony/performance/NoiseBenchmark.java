/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package squidpony.performance;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidmath.*;

import java.util.concurrent.TimeUnit;

import static squidpony.squidmath.NumberTools.swayTight;
import static squidpony.squidmath.NumberTools.zigzag;

/**
 * Various different noise functions, most variants on Simplex noise. These measurements are per-call, in nanoseconds.
 * 2D and higher noise methods, before WhirlingNoise refactor (Feb 7, 2018):
 * Benchmark                            Mode  Cnt    Score   Error  Units
 * NoiseBenchmark.measureMerlin2D       avgt    4   13.131 ± 0.174  ns/op // produces longs, not continuous like Simplex
 * NoiseBenchmark.measureMerlin3D       avgt    4   33.318 ± 0.628  ns/op // produces longs, not continuous like Simplex
 * NoiseBenchmark.measurePerlin2D       avgt    4   41.864 ± 0.978  ns/op // PerlinNoise doesn't take a seed
 * NoiseBenchmark.measurePerlin3D       avgt    4   63.460 ± 1.059  ns/op
 * NoiseBenchmark.measurePerlin4D       avgt    4  117.402 ± 2.109  ns/op
 * NoiseBenchmark.measureSeeded2D       avgt    4   44.431 ± 1.367  ns/op // SeededNoise takes a seed
 * NoiseBenchmark.measureSeeded3D       avgt    4   64.741 ± 2.399  ns/op
 * NoiseBenchmark.measureSeeded4D       avgt    4  101.149 ± 2.251  ns/op
 * NoiseBenchmark.measureSeeded6D       avgt    4  192.837 ± 4.507  ns/op
 * NoiseBenchmark.measureWhirling2D     avgt    4   40.474 ± 1.329  ns/op // WhirlngNoise takes a seed and uses some
 * NoiseBenchmark.measureWhirling3D     avgt    4   63.542 ± 4.781  ns/op //   unusual gradient vectors
 * NoiseBenchmark.measureWhirling4D     avgt    4  116.715 ± 1.456  ns/op
 * NoiseBenchmark.measureWhirlingAlt2D  avgt    4   41.742 ± 1.181  ns/op // WhirlingAlt returns a float, not a double
 * NoiseBenchmark.measureWhirlingAlt3D  avgt    4   63.035 ± 1.447  ns/op
 *
 * 2D and higher noise methods, after WhirlingNoise refactor (Feb 9, 2018):
 * Benchmark                           Mode  Cnt    Score    Error  Units
 * NoiseBenchmark.measureMerlin2D      avgt    4   13.047 ±  0.097  ns/op // produces longs, not continuous like Simplex
 * NoiseBenchmark.measureMerlin3D      avgt    4   33.038 ±  0.362  ns/op // produces longs, not continuous like Simplex
 * NoiseBenchmark.measurePerlin2D      avgt    4   42.458 ±  1.041  ns/op // PerlinNoise doesn't take a seed
 * NoiseBenchmark.measurePerlin3D      avgt    4   63.157 ±  3.749  ns/op
 * NoiseBenchmark.measurePerlin4D      avgt    4  117.386 ± 12.889  ns/op
 * NoiseBenchmark.measureSeeded2D      avgt    4   43.947 ±  1.036  ns/op // SeededNoise takes a seed
 * NoiseBenchmark.measureSeeded3D      avgt    4   64.346 ±  1.071  ns/op
 * NoiseBenchmark.measureSeeded4D      avgt    4  100.341 ±  1.994  ns/op
 * NoiseBenchmark.measureSeeded6D      avgt    4  191.420 ±  4.424  ns/op
 * NoiseBenchmark.measureWhirling2D    avgt    4   41.706 ±  0.784  ns/op // WhirlngNoise takes a seed and uses some
 * NoiseBenchmark.measureWhirling3D    avgt    4   51.797 ±  0.594  ns/op //   unusual gradient vectors, as well as
 * NoiseBenchmark.measureWhirling4D    avgt    4   96.988 ±  2.970  ns/op //   different point hashes
 * NoiseBenchmark.measureWhirlingAlt2D avgt    4   42.176 ±  0.880  ns/op // WhirlingAlt returns a float, not a double
 * NoiseBenchmark.measureWhirlingAlt3D avgt    4   49.488 ±  0.789  ns/op
 *
 * 1D sway methods, some of which are used in or for noise (Feb 7, 2018):
 * NoiseBenchmark.measureSwayBitDouble         avgt    4  14.477 ± 0.514  ns/op // The Bit functions were in SquidLib,
 * NoiseBenchmark.measureSwayBitDoubleTight    avgt    4  10.433 ± 0.168  ns/op //   and were replaced with the later
 * NoiseBenchmark.measureSwayBitFloat          avgt    4  12.605 ± 0.270  ns/op //   methods that have identical output
 * NoiseBenchmark.measureSwayBitFloatTight     avgt    4  10.359 ± 0.461  ns/op
 * NoiseBenchmark.measureSwayDouble            avgt    4   8.803 ± 0.255  ns/op
 * NoiseBenchmark.measureSwayDoubleTight       avgt    4  10.136 ± 0.658  ns/op
 * NoiseBenchmark.measureSwayFloat             avgt    4   8.594 ± 0.437  ns/op
 * NoiseBenchmark.measureSwayFloatTight        avgt    4   8.665 ± 1.478  ns/op
 * NoiseBenchmark.measureSwayRandomizedDouble  avgt    4  10.583 ± 0.870  ns/op
 * NoiseBenchmark.measureSwayRandomizedFloat   avgt    4  11.219 ± 0.189  ns/op
 *
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class NoiseBenchmark {

    private short x = 0, y = 0, z = 0, w = 0, u = 0, v = 0;
    private float[] f = new float[1024];
    private double[] d = new double[1024];

    @Setup(Level.Trial)
    public void setup() {
        for (int i = 0; i < 1024; i++) {
            f[i] = (float)(d[i] = NumberTools.randomSignedDouble(2000 - i) * (i + 0.25));
        }
    }
    public static double swayTightBit(final double value)
    {
        final long s = Double.doubleToLongBits(value + (value < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4000000000000000L) - 2.0);
        return a * a * a * (a * (a * 6.0 - 15.0) + 10.0);
    }

    public static float swayTightBit(final float value)
    {
        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * a * (a * (a * 6f - 15f) + 10f);
    }


    public static double swayBit(final double value)
    {
        final long s = Double.doubleToLongBits(value + (value < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4000000000000000L) - 2.0);
        return a * a * a * (a * (a * 6.0 - 15.0) + 10.0) * 2.0 - 1.0;
    }

    public static float swayBit(final float value)
    {
        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * a * (a * (a * 6f - 15f) + 10f) * 2f - 1f;
    }
    @Benchmark
    public double measureSwayRandomizedDouble() {
        return NumberTools.swayRandomized(1024L, d[x++ & 1023]);
    }

    @Benchmark
    public float measureSwayRandomizedFloat() {
        return NumberTools.swayRandomized(1024L, f[x++ & 1023]);
    }

    @Benchmark
    public double measureSwayDouble() {
        return NumberTools.sway(d[x++ & 1023]);
    }

    @Benchmark
    public float measureSwayFloat() {
        return NumberTools.sway(f[x++ & 1023]);
    }

    @Benchmark
    public double measureSwayBitDouble() {
        return swayBit(d[x++ & 1023]);
    }

    @Benchmark
    public float measureSwayBitFloat() {
        return swayBit(f[x++ & 1023]);
    }

    @Benchmark
    public double measureSwayDoubleTight() {
        return swayTight(d[x++ & 1023]);
    }

    @Benchmark
    public float measureSwayFloatTight() {
        return swayTight(f[x++ & 1023]);
    }

    @Benchmark
    public double measureSwayBitDoubleTight() {
        return swayTightBit(d[x++ & 1023]);
    }

    @Benchmark
    public float measureSwayBitFloatTight() {
        return swayTightBit(f[x++ & 1023]);
    }

    public static double zigzagBit(final double value)
    {
        final long s = Double.doubleToLongBits(value + (value < 0f ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        return (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4010000000000000L) - 5.0);
    }

    public static float zigzagBit(final float value)
    {
        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        return (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff)
                | 0x40800000) - 5f);
    }

    @Benchmark
    public double measureZigzagDouble() {
        return zigzag(d[x++ & 1023]);
    }

    @Benchmark
    public float measureZigzagFloat() {
        return zigzag(f[x++ & 1023]);
    }

    @Benchmark
    public double measureZigzagBitDouble() {
        return zigzagBit(d[x++ & 1023]);
    }

    @Benchmark
    public float measureZigzagBitFloat() {
        return zigzagBit(f[x++ & 1023]);
    }

    @Benchmark
    public double measurePerlin2D() {
        return PerlinNoise.noise(++x * 0.03125, --y * 0.03125);
    }

    @Benchmark
    public double measurePerlin3D() {
        return PerlinNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
    }

    @Benchmark
    public double measurePerlin4D() {
        return PerlinNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
    }

    @Benchmark
    public double measureWhirling2D() {
        return WhirlingNoise.noise(++x * 0.03125, --y * 0.03125);
    }

    @Benchmark
    public double measureWhirling3D() {
        return WhirlingNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
    }

    @Benchmark
    public double measureWhirling4D() {
        return WhirlingNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
    }

    @Benchmark
    public float measureWhirlingAlt2D() {
        return WhirlingNoise.noiseAlt(++x * 0.03125, --y * 0.03125);
    }

    @Benchmark
    public float measureWhirlingAlt3D() {
        return WhirlingNoise.noiseAlt(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
    }

    @Benchmark
    public long measureMerlin2D() {
        return MerlinNoise.noise2D(++x, --y, 1024L, 16, 8);
    }

    @Benchmark
    public long measureMerlin3D() {
        return MerlinNoise.noise3D(++x, --y, z++, 1024L, 16, 8);
    }

    @Benchmark
    public double measureSeeded2D() {
        return SeededNoise.noise(++x * 0.03125, --y * 0.03125, 1024L);
    }

    @Benchmark
    public double measureSeeded3D() {
        return SeededNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, 1024L);
    }

    @Benchmark
    public double measureSeeded4D() {
        return SeededNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125, 1024L);
    }

    @Benchmark
    public double measureSeeded6D() {
        return SeededNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125, ++u * 0.03125, ++v * 0.03125, 1024L);
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *
     * You are expected to see the different run modes for the same benchmark.
     * Note the units are different, scores are consistent with each other.
     *
     * You can run this test:
     *
     * a) Via the command line from the squidlib-performance module's root folder:
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar NoiseBenchmark -wi 4 -i 4 -f 1
     *
     *    (we requested 5 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NoiseBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
    public static void main2(String[] args){
        for (float i = -16f; i <= 16f; i+= 0.0625f) {
            System.out.printf("Float %f : NumberTools %f , Bit %f\n", i, NumberTools.zigzag(i), zigzagBit(i));
        }
        for (double i = -16.0; i <= 16.0; i+= 0.0625) {
            System.out.printf("Double %f : NumberTools %f , Bit %f\n", i, NumberTools.zigzag(i), zigzagBit(i));
        }
    }
}
