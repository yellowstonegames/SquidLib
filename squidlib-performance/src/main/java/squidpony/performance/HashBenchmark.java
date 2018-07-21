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
 * Benchmark                        Mode  Cnt   Score   Error  Units
 * HashBenchmark.doCharFalcon32     avgt    2  32.171          ns/op
 * HashBenchmark.doCharFalcon64     avgt    2  34.606          ns/op
 * HashBenchmark.doCharJDK32        avgt    2  29.077          ns/op
 * HashBenchmark.doCharJDK32Mixed   avgt    2  29.480          ns/op
 * HashBenchmark.doCharJolt32       avgt    2  34.272          ns/op
 * HashBenchmark.doCharJolt64       avgt    2  33.294          ns/op
 * HashBenchmark.doCharLightning32  avgt    2  35.778          ns/op
 * HashBenchmark.doCharLightning64  avgt    2  39.767          ns/op
 * HashBenchmark.doCharMist32       avgt    2  34.643          ns/op
 * HashBenchmark.doCharMist64       avgt    2  35.525          ns/op
 * HashBenchmark.doCharWisp32       avgt    2  30.615          ns/op
 * HashBenchmark.doCharWisp64       avgt    2  31.357          ns/op
 * HashBenchmark.doFalcon32         avgt    2   5.648          ns/op // uses String.hashCode(), collision issues
 * HashBenchmark.doFalcon64         avgt    2   6.025          ns/op
 * HashBenchmark.doJDK32            avgt    2   4.611          ns/op // built-in, uses native code, vulnerable
 * HashBenchmark.doJDK32Mixed       avgt    2   5.363          ns/op
 * HashBenchmark.doJolt32           avgt    2  39.895          ns/op // strongest quality, passes all of SMHasher
 * HashBenchmark.doJolt64           avgt    2  37.076          ns/op
 * HashBenchmark.doLightning32      avgt    2  40.284          ns/op // probably decent quality, but not as fast...
 * HashBenchmark.doLightning64      avgt    2  41.025          ns/op // ...or as high-quality as Jolt
 * HashBenchmark.doLongFalcon32     avgt    2  59.824          ns/op
 * HashBenchmark.doLongFalcon64     avgt    2  61.595          ns/op
 * HashBenchmark.doLongJDK32        avgt    2  68.882          ns/op
 * HashBenchmark.doLongJDK32Mixed   avgt    2  70.991          ns/op
 * HashBenchmark.doLongJolt32       avgt    2  58.750          ns/op
 * HashBenchmark.doLongJolt64       avgt    2  59.937          ns/op
 * HashBenchmark.doLongLightning32  avgt    2  73.237          ns/op
 * HashBenchmark.doLongLightning64  avgt    2  76.494          ns/op
 * HashBenchmark.doLongMist32       avgt    2  71.279          ns/op
 * HashBenchmark.doLongMist64       avgt    2  70.675          ns/op
 * HashBenchmark.doLongWisp32       avgt    2  53.677          ns/op
 * HashBenchmark.doLongWisp64       avgt    2  50.808          ns/op
 * HashBenchmark.doMist32           avgt    2  38.417          ns/op // allows salting the hash, not cryptographically
 * HashBenchmark.doMist64           avgt    2  38.838          ns/op
 * HashBenchmark.doWisp32           avgt    2  36.223          ns/op // has collision properties between JDK and Jolt 
 * HashBenchmark.doWisp64           avgt    2  33.515          ns/op // tends to do relatively badly on cramped tables
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
        public long[][] longs;
        public int idx;
        @Setup(Level.Trial)
        public void setup() {
            FakeLanguageGen[] languages = new FakeLanguageGen[16];
            for (int i = 0; i < 16; i++) {
                languages[i] = FakeLanguageGen.randomLanguage(LinnormRNG.determine(i)).addAccents(0.8, 0.6);
            }
            RNG random = new RNG(1000L);
            words = new String[4096];
            chars = new char[4096][];
            longs = new long[4096][];
            for (int i = 0; i < 4096; i++) {
                chars[i] = (words[i] = languages[i & 15].word(random, random.nextBoolean(), random.next(3)+1)).toCharArray();
                final int len = (random.next(6)+9);
                long[] lon = new long[len];
                for (int j = 0; j < len; j++) {
                    lon[j] = random.nextLong();
                }
                longs[i] = lon;
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
    public long doJolt64(BenchmarkState state)
    {
        return CrossHash.Jolt.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }
    
    @Benchmark
    public int doJolt32(BenchmarkState state)
    {
        return CrossHash.Jolt.hash(state.words[state.idx = state.idx + 1 & 4095]);
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

    @Benchmark
    public long doCharJolt64(BenchmarkState state)
    {
        return CrossHash.Jolt.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharJolt32(BenchmarkState state)
    {
        return CrossHash.Jolt.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }





    @Benchmark
    public long doLongWisp64(BenchmarkState state)
    {
        return CrossHash.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongWisp32(BenchmarkState state)
    {
        return CrossHash.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doLongLightning64(BenchmarkState state)
    {
        return CrossHash.Lightning.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongLightning32(BenchmarkState state)
    {
        return CrossHash.Lightning.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doLongFalcon64(BenchmarkState state)
    {
        return CrossHash.Falcon.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongFalcon32(BenchmarkState state)
    {
        return CrossHash.Falcon.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doLongMist64(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongMist32(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongJDK32(BenchmarkState state)
    {
        return Arrays.hashCode(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongJDK32Mixed(BenchmarkState state)
    {
        return HashCommon.mix(Arrays.hashCode(state.longs[state.idx = state.idx + 1 & 4095]));
    }

    @Benchmark
    public long doLongJolt64(BenchmarkState state)
    {
        return CrossHash.Jolt.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongJolt32(BenchmarkState state)
    {
        return CrossHash.Jolt.hash(state.longs[state.idx = state.idx + 1 & 4095]);
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
     *    $ java -jar target/benchmarks.jar HashBenchmark -wi 5 -i 4 -f 1
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
