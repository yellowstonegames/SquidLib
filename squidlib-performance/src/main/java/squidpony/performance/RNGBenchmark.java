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
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PermutedRNG;
import squidpony.squidmath.RNG;
import squidpony.squidmath.XorRNG;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *
 * Benchmark                         Mode  Cnt            Score           Error  Units
 * RNGBenchmark.measureLight         avgt    5   1277240378.000 ±  34812997.664  ns/op
 * RNGBenchmark.measureLightInt      avgt    5   1276836850.400 ±  13889082.122  ns/op
 * RNGBenchmark.measureLightIntR     avgt    5   1432702326.200 ±  14138978.267  ns/op
 * RNGBenchmark.measureLightR        avgt    5   1280120494.200 ±  17439614.691  ns/op
 * RNGBenchmark.measurePermuted      avgt    5   1655878391.400 ±  14836230.911  ns/op
 * RNGBenchmark.measurePermutedInt   avgt    5   1735990246.400 ±  70822131.289  ns/op
 * RNGBenchmark.measurePermutedIntR  avgt    5   1735206027.400 ±  65551227.406  ns/op
 * RNGBenchmark.measurePermutedR     avgt    5   1661871519.600 ±  18076393.095  ns/op
 * RNGBenchmark.measureRandom        avgt    5  22797439764.000 ± 487694768.260  ns/op
 * RNGBenchmark.measureRandomInt     avgt    5  12602026661.500 ± 210395645.743  ns/op
 * RNGBenchmark.measureXor           avgt    5   1380311138.800 ±  20607362.908  ns/op
 * RNGBenchmark.measureXorInt        avgt    5   1273759930.900 ±  16671583.022  ns/op
 * RNGBenchmark.measureXorIntR       avgt    5   1217313410.900 ±  15963882.599  ns/op
 * RNGBenchmark.measureXorR          avgt    5   1339543649.600 ±  11389540.760  ns/op
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
