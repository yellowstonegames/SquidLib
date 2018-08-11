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
 * <pre>
 * Earlier benchmark, doesn't have Hive
 * 
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
 * HashBenchmark.doJDK32Mixed       avgt    2   5.363          ns/op // still has poor collision rates
 * HashBenchmark.doJolt32           avgt    2  39.895          ns/op // decent quality, passes SMHasher on bytes
 * HashBenchmark.doJolt64           avgt    2  37.076          ns/op
 * HashBenchmark.doLightning32      avgt    2  40.284          ns/op // probably OK quality, but not as fast...
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
 * 
 * Later benchmark, adding Hive (using the older 32-bit version without GWT opts)
 * 
 * Benchmark                        Mode  Cnt   Score    Error  Units
 * HashBenchmark.doCharFalcon32     avgt    3  32.103 ±  1.852  ns/op
 * HashBenchmark.doCharFalcon64     avgt    3  33.103 ±  0.616  ns/op
 * HashBenchmark.doCharHive32       avgt    3  33.759 ±  2.330  ns/op
 * HashBenchmark.doCharHive64       avgt    3  32.721 ±  2.190  ns/op
 * HashBenchmark.doCharJDK32        avgt    3  27.323 ±  0.933  ns/op
 * HashBenchmark.doCharJDK32Mixed   avgt    3  28.166 ±  1.365  ns/op
 * HashBenchmark.doCharJolt32       avgt    3  33.811 ±  1.733  ns/op
 * HashBenchmark.doCharJolt64       avgt    3  31.215 ±  1.198  ns/op
 * HashBenchmark.doCharLightning32  avgt    3  34.610 ±  1.201  ns/op
 * HashBenchmark.doCharLightning64  avgt    3  38.527 ±  1.033  ns/op
 * HashBenchmark.doCharMist32       avgt    3  35.108 ±  1.093  ns/op
 * HashBenchmark.doCharMist64       avgt    3  33.587 ±  0.818  ns/op
 * HashBenchmark.doCharWisp32       avgt    3  30.725 ±  4.333  ns/op
 * HashBenchmark.doCharWisp64       avgt    3  30.279 ± 10.238  ns/op
 * HashBenchmark.doFalcon32         avgt    3   5.313 ±  0.232  ns/op // uses String.hashCode(), collision issues
 * HashBenchmark.doFalcon64         avgt    3   5.581 ±  0.342  ns/op
 * HashBenchmark.doHive32           avgt    3  41.309 ±  4.635  ns/op // passes all of SMHasher, even on longs
 * HashBenchmark.doHive64           avgt    3  39.676 ±  1.762  ns/op // 20% slower than Wisp but no major failure cases
 * HashBenchmark.doJDK32            avgt    3   4.346 ±  0.088  ns/op // built-in, uses native code, vulnerable   
 * HashBenchmark.doJDK32Mixed       avgt    3   4.703 ±  0.340  ns/op // still has poor collision rates           
 * HashBenchmark.doJolt32           avgt    3  38.139 ±  1.391  ns/op // decent quality, passes SMHasher on bytes 
 * HashBenchmark.doJolt64           avgt    3  36.132 ±  0.943  ns/op // does not pass SMHasher on chars, ints, longs...
 * HashBenchmark.doLightning32      avgt    3  40.116 ±  1.045  ns/op // probably OK quality, but not as fast...
 * HashBenchmark.doLightning64      avgt    3  39.314 ±  1.150  ns/op // ... as Jolt or as high-quality as Hive
 * HashBenchmark.doLongFalcon32     avgt    3  58.298 ±  3.065  ns/op
 * HashBenchmark.doLongFalcon64     avgt    3  56.623 ±  5.100  ns/op
 * HashBenchmark.doLongHive32       avgt    3  61.675 ±  4.431  ns/op
 * HashBenchmark.doLongHive64       avgt    3  61.317 ±  3.192  ns/op
 * HashBenchmark.doLongJDK32        avgt    3  64.978 ±  5.369  ns/op
 * HashBenchmark.doLongJDK32Mixed   avgt    3  64.259 ± 23.908  ns/op
 * HashBenchmark.doLongJolt32       avgt    3  57.426 ±  7.412  ns/op
 * HashBenchmark.doLongJolt64       avgt    3  57.814 ±  4.278  ns/op
 * HashBenchmark.doLongLightning32  avgt    3  70.000 ±  2.104  ns/op
 * HashBenchmark.doLongLightning64  avgt    3  71.063 ±  2.261  ns/op
 * HashBenchmark.doLongMist32       avgt    3  69.608 ±  7.269  ns/op
 * HashBenchmark.doLongMist64       avgt    3  69.899 ±  2.901  ns/op
 * HashBenchmark.doLongWisp32       avgt    3  51.467 ±  5.783  ns/op
 * HashBenchmark.doLongWisp64       avgt    3  51.904 ±  4.352  ns/op
 * HashBenchmark.doMist32           avgt    3  36.884 ±  0.482  ns/op // allows salting the hash, not cryptographically
 * HashBenchmark.doMist64           avgt    3  36.927 ±  0.881  ns/op
 * HashBenchmark.doWisp32           avgt    3  34.424 ±  2.186  ns/op // has collision properties between JDK and Hive
 * HashBenchmark.doWisp64           avgt    3  33.002 ±  2.128  ns/op // tends to do relatively badly on cramped tables
 * 
 * Most recent benchmark, with Hive's GWT opts on 32-bit hashes
 * 
 * Benchmark                        Mode  Cnt   Score    Error  Units
 * HashBenchmark.doCharFalcon32     avgt    3  32.412 ±  1.474  ns/op
 * HashBenchmark.doCharFalcon64     avgt    3  34.526 ±  1.010  ns/op
 * HashBenchmark.doCharHive32       avgt    3  37.073 ±  0.669  ns/op
 * HashBenchmark.doCharHive64       avgt    3  33.945 ±  1.841  ns/op
 * HashBenchmark.doCharJDK32        avgt    3  27.728 ±  0.663  ns/op
 * HashBenchmark.doCharJDK32Mixed   avgt    3  28.900 ±  1.476  ns/op
 * HashBenchmark.doCharJolt32       avgt    3  34.020 ±  1.815  ns/op
 * HashBenchmark.doCharJolt64       avgt    3  31.728 ±  0.391  ns/op
 * HashBenchmark.doCharLightning32  avgt    3  35.559 ±  0.595  ns/op
 * HashBenchmark.doCharLightning64  avgt    3  40.171 ±  0.774  ns/op
 * HashBenchmark.doCharMist32       avgt    3  33.631 ±  1.477  ns/op
 * HashBenchmark.doCharMist64       avgt    3  33.650 ±  2.132  ns/op
 * HashBenchmark.doCharWisp32       avgt    3  32.850 ±  0.899  ns/op
 * HashBenchmark.doCharWisp64       avgt    3  31.341 ±  3.558  ns/op
 * HashBenchmark.doFalcon32         avgt    3   5.323 ±  0.118  ns/op // uses String.hashCode(), collision issues
 * HashBenchmark.doFalcon64         avgt    3   5.625 ±  0.771  ns/op
 * HashBenchmark.doHive32           avgt    3  42.816 ±  1.667  ns/op // passes all of SMHasher, even on longs
 * HashBenchmark.doHive64           avgt    3  40.518 ±  0.566  ns/op // 15% slower than Wisp but no major failure cases
 * HashBenchmark.doJDK32            avgt    3   4.480 ±  0.168  ns/op // built-in, uses native code, vulnerable
 * HashBenchmark.doJDK32Mixed       avgt    3   4.812 ±  0.022  ns/op // still has poor collision rates
 * HashBenchmark.doJolt32           avgt    3  37.936 ±  2.398  ns/op // decent quality, passes SMHasher on bytes
 * HashBenchmark.doJolt64           avgt    3  37.776 ±  1.303  ns/op // does not pass SMHasher on chars, ints, longs...
 * HashBenchmark.doLightning32      avgt    3  40.515 ±  1.394  ns/op // probably OK quality, but not as fast...
 * HashBenchmark.doLightning64      avgt    3  40.372 ±  0.413  ns/op // ... as Jolt or as high-quality as Hive
 * HashBenchmark.doLongFalcon32     avgt    3  57.092 ±  1.949  ns/op
 * HashBenchmark.doLongFalcon64     avgt    3  58.244 ±  1.584  ns/op
 * HashBenchmark.doLongHive32       avgt    3  65.977 ±  1.703  ns/op
 * HashBenchmark.doLongHive64       avgt    3  63.985 ±  6.991  ns/op
 * HashBenchmark.doLongJDK32        avgt    3  66.086 ± 29.066  ns/op
 * HashBenchmark.doLongJDK32Mixed   avgt    3  65.903 ±  5.635  ns/op
 * HashBenchmark.doLongJolt32       avgt    3  58.028 ±  2.726  ns/op
 * HashBenchmark.doLongJolt64       avgt    3  59.674 ±  4.583  ns/op
 * HashBenchmark.doLongLightning32  avgt    3  73.822 ±  4.383  ns/op
 * HashBenchmark.doLongLightning64  avgt    3  74.363 ± 42.518  ns/op
 * HashBenchmark.doLongMist32       avgt    3  71.415 ±  0.310  ns/op
 * HashBenchmark.doLongMist64       avgt    3  69.465 ±  3.750  ns/op
 * HashBenchmark.doLongWisp32       avgt    3  53.084 ±  3.495  ns/op
 * HashBenchmark.doLongWisp64       avgt    3  51.862 ±  2.621  ns/op
 * HashBenchmark.doMist32           avgt    3  36.967 ±  1.028  ns/op // allows salting the hash, not cryptographically
 * HashBenchmark.doMist64           avgt    3  36.757 ±  2.558  ns/op
 * HashBenchmark.doWisp32           avgt    3  35.252 ±  0.812  ns/op // has collision properties between JDK and Hive
 * HashBenchmark.doWisp64           avgt    3  35.825 ±  0.700  ns/op // tends to do relatively badly on cramped tables
 * 
 * 
 * Subset that compares Hive with MetroHash, or at least SquidLib's implementation. Metro has been removed.
 * 
 * Benchmark                    Mode  Cnt   Score   Error  Units
 * HashBenchmark.doCharHive64   avgt    3  33.247 ± 2.049  ns/op
 * HashBenchmark.doCharMetro64  avgt    3  37.730 ± 0.866  ns/op
 * HashBenchmark.doHive64       avgt    3  38.920 ± 1.364  ns/op
 * HashBenchmark.doLongHive64   avgt    3  64.345 ± 2.985  ns/op
 * HashBenchmark.doLongMetro64  avgt    3  66.551 ± 5.205  ns/op
 * HashBenchmark.doMetro64      avgt    3  47.327 ± 2.836  ns/op
 * </pre>
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
        return CrossHash.Wisp.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doWisp32(BenchmarkState state)
    {
        return CrossHash.Wisp.hash(state.words[state.idx = state.idx + 1 & 4095]);
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
    public long doHive64(BenchmarkState state)
    {
        return CrossHash.Hive.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doHive32(BenchmarkState state)
    {
        return CrossHash.Hive.hash(state.words[state.idx = state.idx + 1 & 4095]);
    }






    @Benchmark
    public long doCharWisp64(BenchmarkState state)
    {
        return CrossHash.Wisp.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharWisp32(BenchmarkState state)
    {
        return CrossHash.Wisp.hash(state.chars[state.idx = state.idx + 1 & 4095]);
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
    public long doCharHive64(BenchmarkState state)
    {
        return CrossHash.Hive.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharHive32(BenchmarkState state)
    {
        return CrossHash.Hive.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }





    @Benchmark
    public long doLongWisp64(BenchmarkState state)
    {
        return CrossHash.Wisp.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongWisp32(BenchmarkState state)
    {
        return CrossHash.Wisp.hash(state.longs[state.idx = state.idx + 1 & 4095]);
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

    @Benchmark
    public long doLongHive64(BenchmarkState state)
    {
        return CrossHash.Hive.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongHive32(BenchmarkState state)
    {
        return CrossHash.Hive.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }



//    @Benchmark
//    public long doMetro64(BenchmarkState state)
//    {
//        return CrossHash.Metro.hash64(state.words[state.idx = state.idx + 1 & 4095]);
//    }
//    @Benchmark
//    public long doCharMetro64(BenchmarkState state)
//    {
//        return CrossHash.Metro.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
//    }
//    @Benchmark
//    public long doLongMetro64(BenchmarkState state)
//    {
//        return CrossHash.Metro.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
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
