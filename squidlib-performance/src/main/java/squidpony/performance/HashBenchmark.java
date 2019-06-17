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
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark results for the competitive hashes:
 * <pre>
 * Benchmark                       Mode  Cnt    Score     Error  Units
 * HashBenchmark.doCharHive32      avgt    4  151.696 ±   6.154  ns/op
 * HashBenchmark.doCharHive64      avgt    4  101.357 ±  15.744  ns/op
 * HashBenchmark.doCharJDK32       avgt    4  100.781 ±   9.000  ns/op
 * HashBenchmark.doCharJDK32Mixed  avgt    4  104.838 ±  12.547  ns/op
 * HashBenchmark.doCharMist32      avgt    4  107.612 ±   7.686  ns/op
 * HashBenchmark.doCharMist64      avgt    4  111.511 ±   5.298  ns/op
 * HashBenchmark.doCharWater32     avgt    4   84.413 ±   7.113  ns/op
 * HashBenchmark.doCharWater64     avgt    4   94.103 ±   3.237  ns/op
 * HashBenchmark.doCharWisp32      avgt    4   80.638 ±  10.628  ns/op
 * HashBenchmark.doCharWisp64      avgt    4  175.968 ± 130.563  ns/op
 * HashBenchmark.doHive32          avgt    4  153.087 ±   2.951  ns/op
 * HashBenchmark.doHive64          avgt    4  105.825 ±   1.682  ns/op
 * HashBenchmark.doIntHive32       avgt    4  346.126 ±  42.932  ns/op
 * HashBenchmark.doIntHive64       avgt    4  193.427 ±  15.995  ns/op
 * HashBenchmark.doIntJDK32        avgt    4  203.276 ±   6.770  ns/op
 * HashBenchmark.doIntJDK32Mixed   avgt    4  202.785 ±   8.355  ns/op
 * HashBenchmark.doIntMist32       avgt    4  217.299 ±  12.623  ns/op
 * HashBenchmark.doIntMist64       avgt    4  206.439 ±  11.843  ns/op
 * HashBenchmark.doIntWater32      avgt    4  165.852 ±  17.634  ns/op
 * HashBenchmark.doIntWater64      avgt    4  169.269 ±  12.453  ns/op
 * HashBenchmark.doIntWisp32       avgt    4  148.828 ±  16.115  ns/op
 * HashBenchmark.doIntWisp64       avgt    4  148.924 ±  10.348  ns/op
 * HashBenchmark.doJDK32           avgt    4  103.804 ±   2.603  ns/op
 * HashBenchmark.doJDK32Mixed      avgt    4  105.949 ±   4.195  ns/op
 * HashBenchmark.doLongHive32      avgt    4  229.837 ±  18.519  ns/op
 * HashBenchmark.doLongHive64      avgt    4  236.280 ±  27.510  ns/op
 * HashBenchmark.doLongJDK32       avgt    4  250.161 ±   8.693  ns/op
 * HashBenchmark.doLongJDK32Mixed  avgt    4  268.061 ±  27.579  ns/op
 * HashBenchmark.doLongMist32      avgt    4  266.050 ±  16.442  ns/op
 * HashBenchmark.doLongMist64      avgt    4  255.740 ±  15.452  ns/op
 * HashBenchmark.doLongWater32     avgt    4  300.337 ±  36.253  ns/op
 * HashBenchmark.doLongWater64     avgt    4  324.545 ±  31.143  ns/op
 * HashBenchmark.doLongWisp32      avgt    4  205.512 ±  12.850  ns/op
 * HashBenchmark.doLongWisp64      avgt    4  177.933 ±  15.899  ns/op
 * HashBenchmark.doMist32          avgt    4  115.300 ±   8.605  ns/op
 * HashBenchmark.doMist64          avgt    4  112.862 ±   5.799  ns/op
 * HashBenchmark.doWater32         avgt    4   92.675 ±   4.073  ns/op
 * HashBenchmark.doWater64         avgt    4   95.059 ±   7.788  ns/op
 * HashBenchmark.doWisp32          avgt    4   84.313 ±   7.153  ns/op
 * HashBenchmark.doWisp64          avgt    4   84.347 ±   5.727  ns/op
 * </pre>
 * Of these, only Water passes the latest SMHasher test suite. Hive comes closer than the others, but still fails quite
 * a few tests. Water is, in the version tested above, rather slow when hashing long arrays, but this was addressed in
 * the current version:
 * <pre>
 * Benchmark                       Mode  Cnt    Score    Error  Units
 * HashBenchmark.doLongHive32      avgt    4  241.644 ± 14.850  ns/op
 * HashBenchmark.doLongHive64      avgt    4  238.928 ± 12.087  ns/op
 * HashBenchmark.doLongJDK32       avgt    4  258.728 ±  7.925  ns/op
 * HashBenchmark.doLongJDK32Mixed  avgt    4  251.463 ±  5.617  ns/op
 * HashBenchmark.doLongWater32     avgt    4  225.227 ±  5.921  ns/op // the current LongWater algo
 * HashBenchmark.doLongWater64     avgt    4  235.472 ±  8.339  ns/op
 * HashBenchmark.doLongWater32Old  avgt    4  296.994 ± 34.286  ns/op // the LongWater algo above
 * HashBenchmark.doLongWater64Old  avgt    4  321.095 ± 10.299  ns/op
 * HashBenchmark.doLongWisp32      avgt    4  188.004 ±  8.008  ns/op
 * HashBenchmark.doLongWisp64      avgt    4  192.939 ±  6.343  ns/op
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
        public CharSequence[] words;
        public char[][] chars;
        public long[][] longs;
        public int[][] ints;
        public int idx;
        private final int[] intInputs = new int[65536];
        private final long[] longInputs = new long[65536];

        @Benchmark
        public long measurePointHash2D()
        {
            return Noise.PointHash.hashAll(longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)]);
        }

        @Benchmark
        public long measurePointHash3D()
        {
            return Noise.PointHash.hashAll(longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)]);
        }

        @Benchmark
        public long measurePointHash4D()
        {
            return Noise.PointHash.hashAll(longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)]);
        }

        @Benchmark
        public long measurePointHash6D()
        {
            return Noise.PointHash.hashAll(longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)], longInputs[(idx++ & 0xFFFF)]);
        }

        @Benchmark
        public long measureHastyPointHash2D()
        {
            return Noise.HastyPointHash.hashAll(intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)]);
        }

        @Benchmark
        public long measureHastyPointHash3D()
        {
            return Noise.HastyPointHash.hashAll(intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)]);
        }

        @Benchmark
        public long measureHastyPointHash4D()
        {
            return Noise.HastyPointHash.hashAll(intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)]);
        }

        @Benchmark
        public long measureHastyPointHash6D()
        {
            return Noise.HastyPointHash.hashAll(intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)], intInputs[(idx++ & 0xFFFF)]);
        }

        @Setup(Level.Trial)
        public void setup() {
            MiniMover64RNG random = new MiniMover64RNG(1000L);
            FakeLanguageGen[] languages = new FakeLanguageGen[16];
            for (int i = 0; i < 16; i++) {
                languages[i] = FakeLanguageGen.randomLanguage(random.nextLong()).addAccents(0.8, 0.6);
            }
            words = new CharSequence[4096];
            chars = new char[4096][];
            longs = new long[4096][];
            ints = new int[4096][];
            for (int i = 0; i < 65536; i++) {
                intInputs[i] = (int)(longInputs[i] = random.nextLong());
            }
            for (int i = 0; i < 4096; i++) {
                String w = languages[i & 15].sentence(random.nextLong(), random.next(3) + 5, random.next(4)+15);
                chars[i] = w.toCharArray();
                words[i] = new StringBuilder(w);
                final int len = (random.next(8)+32);
                long[] lon = new long[len];
                int[] inn = new int[len];
                for (int j = 0; j < len; j++) {
                    inn[j] = (int)(lon[j] = random.nextLong());
                }
                longs[i] = lon;
                ints[i] = inn;
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
    public long doIntWisp64(BenchmarkState state)
    {
        return CrossHash.Wisp.hash64(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntWisp32(BenchmarkState state)
    {
        return CrossHash.Wisp.hash(state.ints[state.idx = state.idx + 1 & 4095]);
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
    public long doIntLightning64(BenchmarkState state)
    {
        return CrossHash.Lightning.hash64(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntLightning32(BenchmarkState state)
    {
        return CrossHash.Lightning.hash(state.ints[state.idx = state.idx + 1 & 4095]);
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
    public long doIntJolt64(BenchmarkState state)
    {
        return CrossHash.Jolt.hash64(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntJolt32(BenchmarkState state)
    {
        return CrossHash.Jolt.hash(state.ints[state.idx = state.idx + 1 & 4095]);
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
    public long doIntMist64(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash64(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntMist32(BenchmarkState state)
    {
        return CrossHash.Mist.mu.hash(state.ints[state.idx = state.idx + 1 & 4095]);
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
    public long doIntHive64(BenchmarkState state)
    {
        return CrossHash.Hive.hash64(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntHive32(BenchmarkState state)
    {
        return CrossHash.Hive.hash(state.ints[state.idx = state.idx + 1 & 4095]);
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
    public long doLongHive64(BenchmarkState state)
    {
        return CrossHash.Hive.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongHive32(BenchmarkState state)
    {
        return CrossHash.Hive.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }
    @Benchmark
    public long doSirocco64(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doSirocco32(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doIntSirocco64(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash64(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntSirocco32(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doCharSirocco64(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharSirocco32(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doLongSirocco64(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongSirocco32(BenchmarkState state)
    {
        return CrossHash.Sirocco.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doWater64(BenchmarkState state)
    {
        return CrossHash.Water.hash64(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doWater32(BenchmarkState state)
    {
        return CrossHash.Water.hash(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doIntWater64(BenchmarkState state)
    {
        return CrossHash.Water.hash64(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntWater32(BenchmarkState state)
    {
        return CrossHash.Water.hash(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doCharWater64(BenchmarkState state)
    {
        return CrossHash.Water.hash64(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doCharWater32(BenchmarkState state)
    {
        return CrossHash.Water.hash(state.chars[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public long doLongWater64(BenchmarkState state)
    {
        return CrossHash.Water.hash64(state.longs[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doLongWater32(BenchmarkState state)
    {
        return CrossHash.Water.hash(state.longs[state.idx = state.idx + 1 & 4095]);
    }

//    @Benchmark
//    public long doLongWater_64(BenchmarkState state)
//    {
//        return CrossHash.Water.hash64_(state.longs[state.idx = state.idx + 1 & 4095]);
//    }
//
//    @Benchmark
//    public int doLongWater_32(BenchmarkState state)
//    {
//        return CrossHash.Water.hash_(state.longs[state.idx = state.idx + 1 & 4095]);
//    }

    @Benchmark
    public int doJDK32(BenchmarkState state)
    {
        return hashCode(state.words[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doJDK32Mixed(BenchmarkState state)
    {
        return HashCommon.mix(hashCode(state.words[state.idx = state.idx + 1 & 4095]));
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
    public int doIntJDK32(BenchmarkState state)
    {
        return Arrays.hashCode(state.ints[state.idx = state.idx + 1 & 4095]);
    }

    @Benchmark
    public int doIntJDK32Mixed(BenchmarkState state)
    {
        return HashCommon.mix(Arrays.hashCode(state.ints[state.idx = state.idx + 1 & 4095]));
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

    /**
     * Acts like {@link Arrays#hashCode(char[])} but works on any CharSequence, including StringBuilder (which doesn't
     * have a hashCode() implementation of its own).
     * @param chars any CharSequence
     * @return a 32-bit hash of {@code chars}
     */
    public static int hashCode(CharSequence chars) {
        if (chars == null) return 0;
        int result = 1;
        final int len = chars.length();
        for (int i = 0; i < len; i++)
            result = 31 * result + chars.charAt(i);
        return result;
    }

}
