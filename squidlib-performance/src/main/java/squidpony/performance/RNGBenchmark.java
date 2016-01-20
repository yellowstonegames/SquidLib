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

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                         Mode  Cnt            Score            Error  Units
 * RNGBenchmark.measureLight         avgt    3   1275059037.000 ±   61737532.875  ns/op
 * RNGBenchmark.measureLightInt      avgt    3   1278703443.000 ±   66201423.790  ns/op
 * RNGBenchmark.measureLightIntR     avgt    3   1427303028.000 ±  200958011.322  ns/op
 * RNGBenchmark.measureLightR        avgt    3   1269081959.667 ±   86190018.925  ns/op
 * RNGBenchmark.measureMT            avgt    3  43085766002.333 ± 2268888793.171  ns/op
 * RNGBenchmark.measureMTInt         avgt    3  22167143778.000 ±  828756142.658  ns/op
 * RNGBenchmark.measureMTIntR        avgt    3  22132403458.000 ±  383655518.387  ns/op
 * RNGBenchmark.measureMTR           avgt    3  43006069307.000 ± 2473850311.634  ns/op
 * RNGBenchmark.measurePermuted      avgt    3   1637032592.333 ±   59199840.006  ns/op
 * RNGBenchmark.measurePermutedInt   avgt    3   1734496732.000 ±   93718940.208  ns/op
 * RNGBenchmark.measurePermutedIntR  avgt    3   1737075300.667 ±  241897619.330  ns/op
 * RNGBenchmark.measurePermutedR     avgt    3   1668389798.667 ±  378429094.045  ns/op
 * RNGBenchmark.measureRandom        avgt    3  22703702167.000 ±  392502237.818  ns/op
 * RNGBenchmark.measureRandomInt     avgt    3  12593739050.667 ±  197683615.906  ns/op
 * RNGBenchmark.measureXor           avgt    3   1384086605.000 ±  174305317.575  ns/op
 * RNGBenchmark.measureXorInt        avgt    3   1276688870.167 ±  133364204.061  ns/op
 * RNGBenchmark.measureXorIntR       avgt    3   1214642941.833 ±   51259344.714  ns/op
 * RNGBenchmark.measureXorR          avgt    3   1346017624.333 ±  151221919.876  ns/op
 */
public class RNGBenchmark {

    private static long seed = 9000;
    private static int iseed = 9000;

    public long doLight()
    {
        LightRNG rng = new LightRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureLight() throws InterruptedException {
        seed = 9000;
        doLight();
    }

    public long doLightInt()
    {
        LightRNG rng = new LightRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureLightInt() throws InterruptedException {
        iseed = 9000;
        doLightInt();
    }
    public long doLightR()
    {
        RNG rng = new RNG(new LightRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureLightR() throws InterruptedException {
        seed = 9000;
        doLightR();
    }

    public long doLightIntR()
    {
        RNG rng = new RNG(new LightRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureLightIntR() throws InterruptedException {
        iseed = 9000;
        doLightIntR();
    }

    public long doRandom()
    {
        Random rng = new Random(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureRandom() throws InterruptedException {
        seed = 9000;
        doRandom();
    }

    public long doRandomInt()
    {
        Random rng = new Random(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureRandomInt() throws InterruptedException {
        iseed = 9000;
        doRandomInt();
    }

    public long doPermuted()
    {
        PermutedRNG rng = new PermutedRNG(seed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measurePermuted() throws InterruptedException {
        seed = 9000;
        doPermuted();
    }

    public long doPermutedInt()
    {
        PermutedRNG rng = new PermutedRNG(iseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measurePermutedInt() throws InterruptedException {
        iseed = 9000;
        doPermutedInt();
    }

    public long doPermutedR()
    {
        RNG rng = new RNG(new PermutedRNG(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measurePermutedR() throws InterruptedException {
        seed = 9000;
        doPermutedR();
    }

    public long doPermutedIntR()
    {
        RNG rng = new RNG(new PermutedRNG(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measurePermutedIntR() throws InterruptedException {
        iseed = 9000;
        doPermutedIntR();
    }


    public long doXor()
    {
        XorRNG rng = new XorRNG(seed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureXor() throws InterruptedException {
        seed = 9000;
        doXor();
    }

    public long doXorInt()
    {
        XorRNG rng = new XorRNG(iseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureXorInt() throws InterruptedException {
        iseed = 9000;
        doXorInt();
    }

    public long doXorR()
    {
        RNG rng = new RNG(new XorRNG(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureXorR() throws InterruptedException {
        seed = 9000;
        doXorR();
    }

    public long doXorIntR()
    {
        RNG rng = new RNG(new XorRNG(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureXorIntR() throws InterruptedException {
        iseed = 9000;
        doXorIntR();
    }





    public long doMT()
    {
        byte[] bseed = new byte[16];
        Arrays.fill(bseed, (byte)seed);
        MersenneTwister rng = new MersenneTwister(bseed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureMT() throws InterruptedException {
        seed = 9000;
        doMT();
    }

    public long doMTInt()
    {
        byte[] bseed = new byte[16];
        Arrays.fill(bseed, (byte)iseed);
        MersenneTwister rng = new MersenneTwister(bseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureMTInt() throws InterruptedException {
        iseed = 9000;
        doMTInt();
    }

    public long doMTR()
    {
        byte[] bseed = new byte[16];
        Arrays.fill(bseed, (byte)seed);
        RNG rng = new RNG(new MersenneTwister(bseed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureMTR() throws InterruptedException {
        seed = 9000;
        doMTR();
    }

    public long doMTIntR()
    {
        byte[] bseed = new byte[16];
        Arrays.fill(bseed, (byte)iseed);
        RNG rng = new RNG(new MersenneTwister(bseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureMTIntR() throws InterruptedException {
        iseed = 9000;
        doMTIntR();
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
     *    $ java -jar target/benchmarks.jar RNGBenchmark -wi 3 -i 5 -f 1
     *
     *    (we requested 5 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RNGBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
