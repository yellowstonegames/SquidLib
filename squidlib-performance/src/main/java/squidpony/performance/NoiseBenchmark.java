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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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
public class NoiseBenchmark {

    private static double seed = 9000;
    private static int state = 9999;

    public double doPerlin2D()
    {
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += PerlinNoise.noise(a += 0.0625, a);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePerlin2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doPerlin2D());
    }

    public double doPerlin3D()
    {

        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += PerlinNoise.noise(a += 0.0625, a, a);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePerlin3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doPerlin3D());
    }


    public double doPerlin4D()
    {

        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += PerlinNoise.noise(a += 0.0625, a, a, a);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePerlin4D() throws InterruptedException {
        seed = 9000;
        System.out.println(doPerlin4D());
    }

    public double doMerlin2D()
    {
        int a = 0;
        for (double x = 0.0; x < 64000000.0; x++) {
            seed += MerlinNoise.noise2D(++a, a, 9000L, 16, 8);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMerlin2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doMerlin2D());
    }

    public double doMerlin3D()
    {
        int a = 0;
        for (double x = 0.0; x < 64000000.0; x++) {
            seed += MerlinNoise.noise3D(++a, a, a,9000L, 16, 8);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMerlin3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doMerlin3D());
    }

    public double doWhirling2D()
    {
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += WhirlingNoise.noise(a += 0.0625, a);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirling2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirling2D());
    }

    public double doWhirling3D()
    {
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += WhirlingNoise.noise(a += 0.0625, a, a);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirling3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirling3D());
    }

    public double doWhirling4D()
    {
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += WhirlingNoise.noise(a += 0.0625, a, a, a);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirling4D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirling4D());
    }


    public double doWhirlingAlt2D()
    {
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += WhirlingNoise.noiseAlt(a += 0.0625, a);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirlingAlt2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirlingAlt2D());
    }

    public double doWhirlingAlt3D()
    {
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += WhirlingNoise.noiseAlt(a += 0.0625, a, a);
        }

        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirlingAlt3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirlingAlt3D());
    }

    public double doSeeded2D()
    {

        state = 9999;
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += SeededNoise.noise(a += 0.0625, a, ++state);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSeeded2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doSeeded2D());
    }

    public double doSeeded3D()
    {
        state = 9999;
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += SeededNoise.noise(a += 0.0625, a, a, ++state);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSeeded3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doSeeded3D());
    }

    public double doSeeded4D()
    {
        state = 9999;
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += SeededNoise.noise(a += 0.0625, a, a, a, ++state);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSeeded4D() throws InterruptedException {
        seed = 9000;
        System.out.println(doSeeded4D());
    }

    public double doSeeded6D()
    {
        state = 9999;
        for (double x = 0.0, a = 0.0; x < 64000000.0; x++) {
            seed += SeededNoise.noise(a += 0.0625, a, a, a, a, a, ++state);
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSeeded6D() throws InterruptedException {
        seed = 9000;
        System.out.println(doSeeded6D());
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
                .warmupIterations(4)
                .measurementIterations(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
