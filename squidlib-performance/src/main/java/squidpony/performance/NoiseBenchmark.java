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

/** Generating 64 million points of noise, each run
 * Benchmark                            Mode  Cnt     Score     Error  Units
 * NoiseBenchmark.measureMerlin2D       avgt    4   734.085 ±  58.096  ms/op // different type of result, not smooth
 * NoiseBenchmark.measureMerlin3D       avgt    4  1082.559 ± 143.629  ms/op // different type of result, not smooth
 * NoiseBenchmark.measurePerlin2D       avgt    4  2100.514 ±  55.326  ms/op
 * NoiseBenchmark.measurePerlin3D       avgt    4  3385.798 ±  77.035  ms/op
 * NoiseBenchmark.measureWhirling2D     avgt    4  1609.510 ± 134.049  ms/op // best smooth 2D
 * NoiseBenchmark.measureWhirling3D     avgt    4  2808.307 ± 121.119  ms/op // best smooth 3D
 * NoiseBenchmark.measureWhirlingAlt2D  avgt    4  1758.283 ± 117.308  ms/op
 * NoiseBenchmark.measureWhirlingAlt3D  avgt    4  2895.686 ±  98.599  ms/op
 *
 * Newer (March 29, 2017)
 * Benchmark                            Mode  Cnt     Score     Error  Units
 * NoiseBenchmark.measureMerlin2D       avgt    4  1044.135 ±  82.200  ms/op
 * NoiseBenchmark.measureMerlin3D       avgt    4  1057.640 ± 200.091  ms/op
 * NoiseBenchmark.measurePerlin2D       avgt    4  1986.690 ± 529.819  ms/op
 * NoiseBenchmark.measurePerlin3D       avgt    4  3023.699 ±  63.116  ms/op
 * NoiseBenchmark.measurePerlin4D       avgt    4  3949.164 ± 599.040  ms/op
 * NoiseBenchmark.measureSeeded2D       avgt    4  1610.013 ± 148.908  ms/op
 * NoiseBenchmark.measureSeeded3D       avgt    4  2122.671 ± 314.632  ms/op
 * NoiseBenchmark.measureSeeded4D       avgt    4  3817.848 ± 444.091  ms/op
 * NoiseBenchmark.measureSeeded6D       avgt    4  8295.256 ± 886.270  ms/op
 * NoiseBenchmark.measureWhirling2D     avgt    4  1778.534 ± 153.531  ms/op
 * NoiseBenchmark.measureWhirling3D     avgt    4  2675.181 ± 116.986  ms/op
 * NoiseBenchmark.measureWhirling4D     avgt    4  3988.344 ± 110.043  ms/op
 * NoiseBenchmark.measureWhirlingAlt2D  avgt    4  1874.060 ±  83.351  ms/op
 * NoiseBenchmark.measureWhirlingAlt3D  avgt    4  2842.153 ± 110.335  ms/op
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class NoiseBenchmark {

    private short x = 0, y = 0, z = 0, w = 0, u = 0, v = 0;

    @Benchmark
    public double measureSwayRandomizedDouble() {
        return NumberTools.swayRandomized(1024L, ++x * 0.03125);
    }

    @Benchmark
    public float measureSwayRandomizedFloat() {
        return NumberTools.swayRandomized(1024L, ++x * 0.03125f);
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


}
