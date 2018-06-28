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
import squidpony.FakeLanguageGen;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.HashCommon;
import squidpony.squidmath.LinnormRNG;
import squidpony.squidmath.RNG;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                          Mode  Cnt    Score    Error  Units
 * HashBenchmark.measureFNV           avgt    3  138.403 ± 13.317  ms/op
 * HashBenchmark.measureFNVInt        avgt    3  150.326 ± 21.640  ms/op
 * HashBenchmark.measureJVMInt        avgt    3   15.855 ± 16.418  ms/op
 * HashBenchmark.measureLightning     avgt    3   21.636 ±  4.122  ms/op
 * HashBenchmark.measureLightningInt  avgt    3   20.268 ±  0.624  ms/op
 * HashBenchmark.measureSip           avgt    3   78.582 ± 25.881  ms/op
 * HashBenchmark.measureSipInt        avgt    3   79.385 ± 30.757  ms/op
 *
 * Benchmark                          Mode  Cnt    Score    Error  Units
 * HashBenchmark.measureFNV           avgt    3  137.832 ± 14.774  ms/op
 * HashBenchmark.measureFNVInt        avgt    3  150.722 ±  5.292  ms/op
 * HashBenchmark.measureJVMInt        avgt    3   14.928 ±  1.250  ms/op
 * HashBenchmark.measureLightning     avgt    3   16.862 ±  6.673  ms/op
 * HashBenchmark.measureLightningInt  avgt    3   18.505 ±  0.817  ms/op
 * HashBenchmark.measureSip           avgt    3   77.700 ±  6.883  ms/op
 * HashBenchmark.measureSipInt        avgt    3   77.576 ±  2.672  ms/op
 *
 * Benchmark                          Mode  Cnt    Score    Error  Units
 * HashBenchmark.measureFNV           avgt    3  137.748 ± 31.812  ms/op
 * HashBenchmark.measureFNVInt        avgt    3  150.993 ± 19.462  ms/op
 * HashBenchmark.measureJVMInt        avgt    3   15.003 ±  2.244  ms/op
 * HashBenchmark.measureLightning     avgt    3   19.766 ±  3.597  ms/op
 * HashBenchmark.measureLightningInt  avgt    3   19.550 ±  7.034  ms/op
 * HashBenchmark.measureSip           avgt    3   78.889 ± 21.236  ms/op
 * HashBenchmark.measureSipInt        avgt    3   77.797 ±  5.196  ms/op
 * HashBenchmark.measureStorm         avgt    3   24.542 ±  2.893  ms/op
 * HashBenchmark.measureStormInt      avgt    3   25.070 ±  3.274  ms/op
 *
 * Benchmark                          Mode  Cnt    Score   Error  Units
 * HashBenchmark.measureControl       avgt    8    1.980 ± 0.015  ms/op
 * HashBenchmark.measureFNV           avgt    8  136.929 ± 1.341  ms/op
 * HashBenchmark.measureFNVInt        avgt    8  150.062 ± 1.248  ms/op
 * HashBenchmark.measureFalcon        avgt    8   15.653 ± 0.249  ms/op
 * HashBenchmark.measureFalconInt     avgt    8   14.999 ± 0.199  ms/op <-- This is important!
 * HashBenchmark.measureJVMInt        avgt    8   15.030 ± 0.111  ms/op <-- Because this is the collision-prone default!
 * HashBenchmark.measureLightning     avgt    8   19.643 ± 0.109  ms/op
 * HashBenchmark.measureLightningInt  avgt    8   19.332 ± 0.154  ms/op
 * HashBenchmark.measureStorm         avgt    8   24.422 ± 0.185  ms/op
 * HashBenchmark.measureStormInt      avgt    8   25.002 ± 0.306  ms/op
 *
 * Benchmark                          Mode  Cnt    Score   Error  Units
 * HashBenchmark.measureControl       avgt    8    2.080 ± 0.009  ms/op
 * HashBenchmark.measureFNV           avgt    8  143.730 ± 0.681  ms/op
 * HashBenchmark.measureFNVInt        avgt    8  157.785 ± 1.505  ms/op
 * HashBenchmark.measureFalcon        avgt    8   16.066 ± 0.205  ms/op
 * HashBenchmark.measureFalconInt     avgt    8   15.321 ± 0.107  ms/op
 * HashBenchmark.measureJVMInt        avgt    8   15.685 ± 0.109  ms/op
 * HashBenchmark.measureLightning     avgt    8   20.617 ± 0.091  ms/op
 * HashBenchmark.measureLightningInt  avgt    8   20.284 ± 0.053  ms/op
 * HashBenchmark.measureStorm         avgt    8   26.013 ± 0.139  ms/op
 * HashBenchmark.measureStormInt      avgt    8   26.278 ± 0.061  ms/op
 * HashBenchmark.measureWisp          avgt    8   11.796 ± 0.034  ms/op <-- This is great! 64-bit hashes are fast!
 * HashBenchmark.measureWispInt       avgt    8   13.046 ± 0.037  ms/op <-- Trying to figure out how to quicken this.
 *
 * Benchmark                          Mode  Cnt    Score   Error  Units
 * HashBenchmark.measureControl       avgt    8    2.082 ± 0.009  ms/op
 * HashBenchmark.measureFNV           avgt    8  143.760 ± 0.523  ms/op
 * HashBenchmark.measureFNVInt        avgt    8  157.110 ± 1.180  ms/op
 * HashBenchmark.measureFalcon        avgt    8   17.178 ± 0.038  ms/op
 * HashBenchmark.measureFalconInt     avgt    8   15.367 ± 0.113  ms/op
 * HashBenchmark.measureJVMInt        avgt    8   15.696 ± 0.071  ms/op
 * HashBenchmark.measureLightning     avgt    8   20.323 ± 0.079  ms/op
 * HashBenchmark.measureLightningInt  avgt    8   20.304 ± 0.101  ms/op
 * HashBenchmark.measureStorm         avgt    8   25.699 ± 0.061  ms/op
 * HashBenchmark.measureStormInt      avgt    8   26.352 ± 0.095  ms/op
 * HashBenchmark.measureWisp          avgt    8   12.780 ± 0.062  ms/op <-- These numbers vary a lot, and may have
 * HashBenchmark.measureWispInt       avgt    8   13.043 ± 0.041  ms/op <-- to do with processor cache availability
 *
 * With some simple changes to the finalization of Wisp to avoid strange artifacts in visual hashing...
 *
 * Benchmark                          Mode  Cnt    Score   Error  Units
 * HashBenchmark.measureSketchInt    avgt    8   23.654 ± 1.395  ms/op
 * HashBenchmark.measureControl       avgt    8    2.295 ± 0.021  ms/op
 * HashBenchmark.measureFNV           avgt    8  155.490 ± 1.308  ms/op
 * HashBenchmark.measureFNVInt        avgt    8  175.354 ± 3.048  ms/op
 * HashBenchmark.measureFalcon        avgt    8   16.321 ± 0.322  ms/op
 * HashBenchmark.measureFalconInt     avgt    8   16.837 ± 0.135  ms/op
 * HashBenchmark.measureJVMInt        avgt    8   17.185 ± 0.198  ms/op
 * HashBenchmark.measureLightning     avgt    8   19.045 ± 0.191  ms/op
 * HashBenchmark.measureLightningInt  avgt    8   19.261 ± 0.225  ms/op
 * HashBenchmark.measureStorm         avgt    8   22.690 ± 0.290  ms/op
 * HashBenchmark.measureStormInt      avgt    8   24.048 ± 0.182  ms/op
 * HashBenchmark.measureWisp          avgt    8   12.761 ± 0.166  ms/op // about the same (good) speed
 * HashBenchmark.measureWispInt       avgt    8   14.122 ± 0.190  ms/op // slightly slower, finalization step probably
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)

public class HashBenchmark {
    @State(Scope.Thread)
    public static class BenchmarkState {
        public String[] words;
        public char[][] chars;
        public int idx;
        @Setup(Level.Trial)
        public void setup() {
            FakeLanguageGen[] languages = new FakeLanguageGen[16];
            for (int i = 0; i < 16; i++) {
                languages[i] = FakeLanguageGen.randomLanguage(LinnormRNG.determine(i));
            }
            RNG random = new RNG(1000L);
            words = new String[4096];
            chars = new char[4096][];
            for (int i = 0; i < 4096; i++) {
                chars[i] = (words[i] = languages[i & 15].word(random, random.nextBoolean(), random.next(3)+1)).toCharArray();
            }
            idx = 0;
        }

    }

    @Benchmark
    public long doWisp64(BenchmarkState state)
    {
        return CrossHash.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doWisp32(BenchmarkState state)
    {
        return CrossHash.hash(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doLightning64(BenchmarkState state)
    {
        return CrossHash.Lightning.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLightning32(BenchmarkState state)
    {
        return CrossHash.Lightning.hash(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doFalcon64(BenchmarkState state)
    {
        return CrossHash.Falcon.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doFalcon32(BenchmarkState state)
    {
        return CrossHash.Falcon.hash(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doMist64(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doMist32(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doJDK32(BenchmarkState state)
    {
        return state.words[state.idx = state.idx + 1 & 4095].hashCode();
    }

    @Benchmark
    public int doJDK32Mixed(BenchmarkState state)
    {
        return HashCommon.mix(state.words[state.idx = state.idx + 1 & 4095].hashCode());
    }



    @Benchmark
    public long doCharWisp64(BenchmarkState state)
    {
        return CrossHash.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharWisp32(BenchmarkState state)
    {
        return CrossHash.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doCharLightning64(BenchmarkState state)
    {
        return CrossHash.Lightning.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharLightning32(BenchmarkState state)
    {
        return CrossHash.Lightning.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doCharFalcon64(BenchmarkState state)
    {
        return CrossHash.Falcon.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharFalcon32(BenchmarkState state)
    {
        return CrossHash.Falcon.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doCharMist64(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharMist32(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharJDK32(BenchmarkState state)
    {
        return Arrays.hashCode(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharJDK32Mixed(BenchmarkState state)
    {
        return HashCommon.mix(Arrays.hashCode(state.chars[state.idx = state.idx + 1 & 4095]));
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
     *    $ java -jar target/benchmarks.jar HashBenchmark -wi 8 -i 8 -f 1 -gc true
     *
     *    (we requested 8 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HashBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(60))
                .warmupIterations(8)
                .measurementIterations(8)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
