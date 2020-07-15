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
import squidpony.performance.alternate.EasySimplexNoise;
import squidpony.squidmath.*;

import java.util.concurrent.TimeUnit;

/**
 * Various different noise functions, most variants on Simplex noise. These measurements are per-call, in nanoseconds.
 * The Score column is the most relevant; the score is how much time it takes to complete one call, so lower is better.
 * The Error column can be ignored if it is relatively small, but large Error values may show a measurement inaccuracy.
 * <br>
 * All 2D and higher noise benchmarks, run with Java 11, on October 21, 2018:
 * <pre>
 * Benchmark                                Mode  Cnt    Score    Error  Units
 * NoiseBenchmark.measureClassic2D          avgt    4   38.779 ±  0.258  ns/op
 * NoiseBenchmark.measureClassic3D          avgt    4   75.015 ±  2.274  ns/op
 * NoiseBenchmark.measureClassic4D          avgt    4   86.665 ±  0.476  ns/op
 * NoiseBenchmark.measureFast3Octave3D      avgt    4  126.118 ±  0.532  ns/op // has 3 octaves of 3D noise
 * NoiseBenchmark.measureFast3Octave4D      avgt    4  212.586 ±  9.077  ns/op // has 3 octaves of 4D noise
 * NoiseBenchmark.measureFast5Octave3D      avgt    4  198.581 ±  1.399  ns/op // has 5 octaves of 3D noise
 * NoiseBenchmark.measureFast5Octave4D      avgt    4  368.770 ±  2.516  ns/op // has 5 octaves of 4D noise
 * NoiseBenchmark.measureFastNoise2D        avgt    4   29.546 ±  0.907  ns/op // fastest 2D
 * NoiseBenchmark.measureFastNoise3D        avgt    4   45.995 ±  0.333  ns/op // fastest 3D
 * NoiseBenchmark.measureFastNoise4D        avgt    4   79.852 ±  0.635  ns/op // tie for fastest 4D?
 * NoiseBenchmark.measureJitter2D           avgt    4   43.965 ±  0.181  ns/op
 * NoiseBenchmark.measureJitter3D           avgt    4  100.873 ±  0.305  ns/op
 * NoiseBenchmark.measureJitter4D           avgt    4  150.578 ±  0.841  ns/op
 * NoiseBenchmark.measurePerlin2D           avgt    4   40.355 ±  1.160  ns/op
 * NoiseBenchmark.measurePerlin3D           avgt    4   60.745 ±  0.385  ns/op
 * NoiseBenchmark.measurePerlin4D           avgt    4  118.092 ±  0.773  ns/op
 * NoiseBenchmark.measureSeeded2D           avgt    4   36.989 ±  1.326  ns/op
 * NoiseBenchmark.measureSeeded3D           avgt    4   65.921 ±  1.286  ns/op
 * NoiseBenchmark.measureSeeded4D           avgt    4   79.034 ± 21.792  ns/op // tie for fastest 4D? high error
 * NoiseBenchmark.measureSeeded6D           avgt    4  182.435 ±  4.713  ns/op // only 6D impl, so... fastest 6D
 * NoiseBenchmark.measureWhirling2D         avgt    4   39.140 ±  0.437  ns/op
 * NoiseBenchmark.measureWhirling3D         avgt    4   55.484 ±  0.505  ns/op
 * NoiseBenchmark.measureWhirling3Octave3D  avgt    4  201.752 ±  4.705  ns/op // has 3 octaves of 3D noise
 * NoiseBenchmark.measureWhirling3Octave4D  avgt    4  248.357 ±  3.296  ns/op // has 3 octaves of 4D noise
 * NoiseBenchmark.measureWhirling4D         avgt    4   96.235 ±  0.893  ns/op
 * NoiseBenchmark.measureWhirling5Octave3D  avgt    4  276.712 ±  2.404  ns/op // has 5 octaves of 3D noise
 * NoiseBenchmark.measureWhirling5Octave4D  avgt    4  422.289 ±  0.777  ns/op // has 5 octaves of 4D noise
 * NoiseBenchmark.measureWhirlingAlt2D      avgt    4   39.591 ±  2.450  ns/op
 * NoiseBenchmark.measureWhirlingAlt3D      avgt    4   47.147 ±  9.241  ns/op
 * </pre>
 * The "Octave" benchmarks should be noted. FastNoise doesn't need Noise.Layered3D or Noise.Layered4D to produce
 * multiple octaves of noise; it can do that using just FastNoise methods, and it does so much faster than WhirlingNoise
 * can using the Layered classes nested in Noise. FastNoise can produce 5 octaves of 3D noise in less time than
 * WhirlingNoise takes to produce 3 octaves of 3D noise. These benchmarks aren't the same on Java 8, which suggests some
 * optimizations are occuring only in newer JVM versions (such as dot products being turned into SIMD calls).
 * <br>
 * 1D sway methods, some of which are used in or for noise (Feb 10, 2018):
 * <pre>
 * Benchmark                                   Mode  Cnt    Score    Error  Units
 * NoiseBenchmark.measureSwayBitDouble         avgt    5   11.289 ±  0.774  ns/op // The Bit functions were in SquidLib,
 * NoiseBenchmark.measureSwayBitDoubleTight    avgt    5   10.680 ±  0.811  ns/op // and were replaced with the later
 * NoiseBenchmark.measureSwayBitFloat          avgt    5   11.063 ±  0.163  ns/op // methods that have identical output
 * NoiseBenchmark.measureSwayBitFloatTight     avgt    5   10.256 ±  0.036  ns/op
 * NoiseBenchmark.measureSwayDouble            avgt    5    8.746 ±  0.124  ns/op
 * NoiseBenchmark.measureSwayDoubleTight       avgt    5    8.789 ±  0.290  ns/op
 * NoiseBenchmark.measureSwayFloat             avgt    5    8.614 ±  0.085  ns/op
 * NoiseBenchmark.measureSwayFloatTight        avgt    5    8.471 ±  0.070  ns/op
 * NoiseBenchmark.measureSwayRandomizedDouble  avgt    5   10.334 ±  0.438  ns/op
 * NoiseBenchmark.measureSwayRandomizedFloat   avgt    5   11.258 ±  0.109  ns/op
 * NoiseBenchmark.measureZigzagBitDouble       avgt    5    9.125 ±  0.061  ns/op
 * NoiseBenchmark.measureZigzagBitFloat        avgt    5   10.243 ±  0.144  ns/op
 * NoiseBenchmark.measureZigzagDouble          avgt    5    7.111 ±  0.065  ns/op
 * NoiseBenchmark.measureZigzagFloat           avgt    5    6.889 ±  0.089  ns/op
 * </pre>
 * <br>
 * Just simplex noise classes in this next set: 
 * <pre>
 * Benchmark                            Mode  Cnt    Score   Error  Units
 * NoiseBenchmark.measureFastNoise2D    avgt    5   35.573 ± 0.219  ns/op
 * NoiseBenchmark.measureFastNoise3D    avgt    5   46.687 ± 0.126  ns/op
 * NoiseBenchmark.measureFastNoise4D    avgt    5   84.014 ± 3.109  ns/op
 * NoiseBenchmark.measurePerlin2D       avgt    5   41.912 ± 0.279  ns/op
 * NoiseBenchmark.measurePerlin3D       avgt    5   63.417 ± 0.658  ns/op
 * NoiseBenchmark.measurePerlin4D       avgt    5  125.655 ± 0.710  ns/op
 * NoiseBenchmark.measureSeeded2D       avgt    5   41.759 ± 0.264  ns/op
 * NoiseBenchmark.measureSeeded3D       avgt    5   59.276 ± 0.262  ns/op
 * NoiseBenchmark.measureSeeded4D       avgt    5   85.117 ± 0.383  ns/op
 * NoiseBenchmark.measureSeeded6D       avgt    5  178.206 ± 2.428  ns/op
 * NoiseBenchmark.measureWhirling2D     avgt    5   41.902 ± 0.203  ns/op
 * NoiseBenchmark.measureWhirling3D     avgt    5   63.774 ± 0.538  ns/op
 * NoiseBenchmark.measureWhirling4D     avgt    5   91.431 ± 0.350  ns/op
 * NoiseBenchmark.measureWhirlingAlt2D  avgt    5   42.433 ± 0.268  ns/op
 * NoiseBenchmark.measureWhirlingAlt3D  avgt    5   49.947 ± 0.322  ns/op
 * </pre>
 * And with just 6D noise, including FastNoise now that it implements Noise6D:
 * <pre>
 * Benchmark                          Mode  Cnt    Score   Error  Units
 * NoiseBenchmark.measureFastNoise6D  avgt    4  172.374 ± 0.353  ns/op
 * NoiseBenchmark.measureSeeded6D     avgt    4  185.143 ± 3.575  ns/op
 * </pre>
 * Note that FastNoise is the best for each dimensionality of noise.
 * <br>
 * Also checking OpenSimplexNoise for completeness:
 * <pre>
 * Benchmark                            Mode  Cnt     Score     Error  Units
 * NoiseBenchmark.measureFastNoise2D    avgt    5    35.433 ±   0.388  ns/op
 * NoiseBenchmark.measureFastNoise3D    avgt    5    48.866 ±   1.844  ns/op
 * NoiseBenchmark.measureFastNoise4D    avgt    5    91.408 ±   7.168  ns/op
 * NoiseBenchmark.measureFastNoise6D    avgt    5   179.456 ±   3.873  ns/op
 * NoiseBenchmark.measureOpenSimplex2D  avgt    5    51.852 ±   0.711  ns/op
 * NoiseBenchmark.measureOpenSimplex3D  avgt    5   115.087 ±   5.407  ns/op
 * NoiseBenchmark.measureOpenSimplex4D  avgt    5  2440.526 ± 430.446  ns/op
 * NoiseBenchmark.measurePerlin2D       avgt    5    44.975 ±   1.400  ns/op
 * NoiseBenchmark.measurePerlin3D       avgt    5    64.337 ±   0.819  ns/op
 * NoiseBenchmark.measurePerlin4D       avgt    5    92.932 ±   1.337  ns/op
 * NoiseBenchmark.measureSeeded2D       avgt    5    42.552 ±   1.203  ns/op
 * NoiseBenchmark.measureSeeded3D       avgt    5    59.289 ±   0.679  ns/op
 * NoiseBenchmark.measureSeeded4D       avgt    5    88.440 ±   6.013  ns/op
 * NoiseBenchmark.measureSeeded6D       avgt    5   184.532 ±   4.244  ns/op
 * </pre>
 * As to the 4D performance of OpenSimplex... ouch. It's slower all-around, but ponderously slow in 4D.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class NoiseBenchmark {

    private short x, y, z, w, u, v;
    private final float[] f = new float[1024];
    private final double[] d = new double[1024];
    private final FastNoise fast = new FastNoise(12345),
            fast3 = new FastNoise(12345),
            fast5 = new FastNoise(12345);
    private final Noise.Layered3D whirling3 = new Noise.Layered3D(new WhirlingNoise(12345), 3, 0.03125),
            whirling5 = new Noise.Layered3D(new WhirlingNoise(12345), 5, 0.03125);
    private final Noise.Layered4D whirling3_4 = new Noise.Layered4D(new WhirlingNoise(12345), 3, 0.03125),
            whirling5_4 = new Noise.Layered4D(new WhirlingNoise(12345), 5, 0.03125);
    {
        fast.setFractalOctaves(1);
        fast.setNoiseType(FastNoise.SIMPLEX);
        fast.setFrequency(0.03125f);
        fast3.setNoiseType(FastNoise.SIMPLEX_FRACTAL);
        fast3.setFractalOctaves(3);
        fast5.setNoiseType(FastNoise.SIMPLEX_FRACTAL);
        fast5.setFractalOctaves(5);
    }

    @Setup(Level.Trial)
    public void setup() {
        for (int i = 0; i < 1024; i++) {
            f[i] = (float)(d[i] = NumberTools.randomSignedDouble(2000 - i) * (i + 0.25));
        }
    }
//    public static double swayTightBit(final double value)
//    {
//        final long s = Double.doubleToLongBits(value + (value < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
//                | 0x4000000000000000L) - 2.0);
//        return a * a * a * (a * (a * 6.0 - 15.0) + 10.0);
//    }
//
//    public static float swayTightBit(final float value)
//    {
//        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
//        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
//        return a * a * a * (a * (a * 6f - 15f) + 10f);
//    }
//
//
//    public static double swayBit(final double value)
//    {
//        final long s = Double.doubleToLongBits(value + (value < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
//                | 0x4000000000000000L) - 2.0);
//        return a * a * a * (a * (a * 6.0 - 15.0) + 10.0) * 2.0 - 1.0;
//    }
//
//    public static float swayBit(final float value)
//    {
//        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
//        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
//        return a * a * a * (a * (a * 6f - 15f) + 10f) * 2f - 1f;
//    }
//    @Benchmark
//    public double measureSwayRandomizedDouble() {
//        return NumberTools.swayRandomized(1024L, d[x++ & 1023]);
//    }
//
//    @Benchmark
//    public float measureSwayRandomizedFloat() {
//        return NumberTools.swayRandomized(1024L, f[x++ & 1023]);
//    }
//
//    @Benchmark
//    public double measureSwayDouble() {
//        return NumberTools.sway(d[x++ & 1023]);
//    }
//
//    @Benchmark
//    public float measureSwayFloat() {
//        return NumberTools.sway(f[x++ & 1023]);
//    }
//
//    @Benchmark
//    public double measureSwayBitDouble() {
//        return swayBit(d[x++ & 1023]);
//    }
//
//    @Benchmark
//    public float measureSwayBitFloat() {
//        return swayBit(f[x++ & 1023]);
//    }
//
//    @Benchmark
//    public double measureSwayDoubleTight() {
//        return swayTight(d[x++ & 1023]);
//    }
//
//    @Benchmark
//    public float measureSwayFloatTight() {
//        return swayTight(f[x++ & 1023]);
//    }
//
//    @Benchmark
//    public double measureSwayBitDoubleTight() {
//        return swayTightBit(d[x++ & 1023]);
//    }
//
//    @Benchmark
//    public float measureSwayBitFloatTight() {
//        return swayTightBit(f[x++ & 1023]);
//    }
//
//    public static double zigzagBit(final double value)
//    {
//        final long s = Double.doubleToLongBits(value + (value < 0f ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        return (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
//                | 0x4010000000000000L) - 5.0);
//    }
//
//    public static float zigzagBit(final float value)
//    {
//        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
//        return (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff)
//                | 0x40800000) - 5f);
//    }
//
//    @Benchmark
//    public double measureZigzagDouble() {
//        return zigzag(d[x++ & 1023]);
//    }
//
//    @Benchmark
//    public float measureZigzagFloat() {
//        return zigzag(f[x++ & 1023]);
//    }
//
//    @Benchmark
//    public double measureZigzagBitDouble() {
//        return zigzagBit(d[x++ & 1023]);
//    }
//
//    @Benchmark
//    public float measureZigzagBitFloat() {
//        return zigzagBit(f[x++ & 1023]);
//    }

//    @Benchmark
//    public double measurePerlin2D() {
//        return PerlinNoise.noise(++x * 0.03125, --y * 0.03125);
//    }
//
//    @Benchmark
//    public double measurePerlin3D() {
//        return PerlinNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
//    }
//
//    @Benchmark
//    public double measurePerlin4D() {
//        return PerlinNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
//    }

//    @Benchmark
//    public double measureWhirling2D() {
//        return WhirlingNoise.noise(++x * 0.03125, --y * 0.03125);
//    }
//
//    @Benchmark
//    public double measureWhirling3D() {
//        return WhirlingNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
//    }
//
//    @Benchmark
//    public double measureWhirling4D() {
//        return WhirlingNoise.noise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
//    }
//
//    @Benchmark
//    public float measureWhirlingAlt2D() {
//        return WhirlingNoise.noiseAlt(++x * 0.03125, --y * 0.03125);
//    }
//
//    @Benchmark
//    public float measureWhirlingAlt3D() {
//        return WhirlingNoise.noiseAlt(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
//    }

//    @Benchmark
//    public long measureMerlin2D() {
//        return MerlinNoise.noise2D(++x, --y, 1024L, 16, 8);
//    }
//
//    @Benchmark
//    public long measureMerlin3D() {
//        return MerlinNoise.noise3D(++x, --y, z++, 1024L, 16, 8);
//    }
//

//    @Benchmark
//    public double measureClassicNoise2D() {
//    return ClassicNoise.instance.getNoise(++x * 0.03125, --y * 0.03125);
//}
//    @Benchmark
//    public double measureClassicNoise3D() {
//        return ClassicNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
//    }
//    @Benchmark
//    public double measureClassicNoise4D() {
//        return ClassicNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
//    }
    
//    @Benchmark
//    public double measureJitterNoise2D() {
//        return JitterNoise.instance.getNoise(++x * 0.03125, --y * 0.03125);
//    }
//    @Benchmark
//    public double measureJitterNoise3D() {
//        return JitterNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
//    }
//    @Benchmark
//    public double measureJitterNoise4D() {
//        return JitterNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
//    }

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

    @Benchmark
    public float measureFastNoise2D() {
        return fast.getSimplex(++x, --y);
    }

    @Benchmark
    public float measureFastNoise3D() {
        return fast.getSimplex(++x, --y, z++);
    }

    @Benchmark
    public float measureFastNoise4D() {
        return fast.getSimplex(++x, --y, z++, w--);
    }

    @Benchmark
    public double measureFastNoise6D() {
        return fast.getSimplex(++x, --y, z++, w--, ++u, ++v);
    }



    @Benchmark
    public double measureOpenSimplex2D() {
        return OpenSimplexNoise.instance.getNoise(++x * 0.03125, --y * 0.03125);
    }

    @Benchmark
    public double measureOpenSimplex3D() {
        return OpenSimplexNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
    }

    @Benchmark
    public double measureOpenSimplex4D() {
        return OpenSimplexNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
    }


    @Benchmark
    public double measureEasySimplex2D() {
        return EasySimplexNoise.instance.getNoise(++x * 0.03125, --y * 0.03125);
    }

    @Benchmark
    public double measureEasySimplex3D() {
        return EasySimplexNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125);
    }

    @Benchmark
    public double measureEasySimplex4D() {
        return EasySimplexNoise.instance.getNoise(++x * 0.03125, --y * 0.03125, z++ * 0.03125, w-- * 0.03125);
    }


//    @Benchmark
//    public float measureFast3Octave3D() {
//        return fast3.getSimplexFractal(++x, --y, z++);
//    }
//
//    @Benchmark
//    public float measureFast5Octave3D() {
//        return fast5.getSimplexFractal(++x, --y, z++);
//    }
//
//    @Benchmark
//    public float measureFast3Octave4D() {
//        return fast3.getSimplexFractal(++x, --y, z++, w--);
//    }
//
//    @Benchmark
//    public float measureFast5Octave4D() {
//        return fast5.getSimplexFractal(++x, --y, z++, w--);
//    }
//
//    @Benchmark
//    public double measureWhirling3Octave3D() {
//        return whirling3.getNoise(++x, --y, z++);
//    }
//
//    @Benchmark
//    public double measureWhirling5Octave3D() {
//        return whirling5.getNoise(++x, --y, z++);
//    }
//
//    @Benchmark
//    public double measureWhirling3Octave4D() {
//        return whirling3_4.getNoise(++x, --y, z++, w--);
//    }
//
//    @Benchmark
//    public double measureWhirling5Octave4D() {
//        return whirling5_4.getNoise(++x, --y, z++, w--);
//    }

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
//    public static void main2(String[] args){
//        for (float i = -16f; i <= 16f; i+= 0.0625f) {
//            System.out.printf("Float %f : NumberTools %f , Bit %f\n", i, NumberTools.zigzag(i), zigzagBit(i));
//        }
//        for (double i = -16.0; i <= 16.0; i+= 0.0625) {
//            System.out.printf("Double %f : NumberTools %f , Bit %f\n", i, NumberTools.zigzag(i), zigzagBit(i));
//        }
//    }
}
