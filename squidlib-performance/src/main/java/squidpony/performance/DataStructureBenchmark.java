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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark timings!
 * GdxOM refers to com.badlogic.gdx.utils.OrderedMap.
 * GdxOS refers to com.badlogic.gdx.utils.OrderedSet.
 * SquidOM refers to squidpony.squidmath.OrderedMap.
 * SquidOS refers to squidpony.squidmath.OrderedSet.
 * The LinkedHash data structures are in java.util.
 * Benchmarks use the default load factor, except for SquidOS and any benchmark followed by a 2 or 3.
 * Benchmarks followed by a 2 use load factor 0.5f, and those followed by a 3 use load factor 0.25f.
 * SquidOS uses load factor 0.75f, the same as GdxOM (it's the default in OrderedMap, not OrderedSet).
 * <br>
 * SquidLib's collections do well here, except on rather large sizes (after about 8000 items, it isn't as fast relative
 * to LinkedHashMap and LinkedHashSet). Considering their added features, this is a good sign.
 * <br>
 * <pre>
 * Benchmark                                   (SIZE)  Mode  Cnt       Score        Error  Units
 * DataStructureBenchmark.insertGdxOM            1500  avgt    4   38223.846 ±  13424.129  ns/op
 * DataStructureBenchmark.insertGdxOM            3000  avgt    4   81488.864 ±  22840.893  ns/op
 * DataStructureBenchmark.insertGdxOM            6000  avgt    4  173953.749 ±  41153.628  ns/op
 * DataStructureBenchmark.insertGdxOM           12000  avgt    4  374832.318 ±  92800.369  ns/op
 * DataStructureBenchmark.insertGdxOM2           1500  avgt    4   35528.416 ±   5805.535  ns/op
 * DataStructureBenchmark.insertGdxOM2           3000  avgt    4   78785.561 ±  32597.491  ns/op
 * DataStructureBenchmark.insertGdxOM2           6000  avgt    4  166947.332 ±  59079.304  ns/op
 * DataStructureBenchmark.insertGdxOM2          12000  avgt    4  371062.396 ± 126467.131  ns/op
 * DataStructureBenchmark.insertGdxOM3           1500  avgt    4   42093.620 ±   9376.479  ns/op
 * DataStructureBenchmark.insertGdxOM3           3000  avgt    4   80130.295 ±  24024.720  ns/op
 * DataStructureBenchmark.insertGdxOM3           6000  avgt    4  165168.479 ±  42004.948  ns/op
 * DataStructureBenchmark.insertGdxOM3          12000  avgt    4  345591.959 ±  20866.678  ns/op
 * DataStructureBenchmark.insertGdxOS            1500  avgt    4   17491.454 ±   1531.361  ns/op
 * DataStructureBenchmark.insertGdxOS            3000  avgt    4   49453.289 ±  27550.976  ns/op
 * DataStructureBenchmark.insertGdxOS            6000  avgt    4   98938.636 ±  14057.754  ns/op
 * DataStructureBenchmark.insertGdxOS           12000  avgt    4  227688.018 ±  53812.489  ns/op
 * DataStructureBenchmark.insertGdxOS2           1500  avgt    4   19020.652 ±   7526.830  ns/op
 * DataStructureBenchmark.insertGdxOS2           3000  avgt    4   42130.408 ±  13797.720  ns/op
 * DataStructureBenchmark.insertGdxOS2           6000  avgt    4   92720.878 ±   7412.059  ns/op
 * DataStructureBenchmark.insertGdxOS2          12000  avgt    4  190499.922 ±  32431.179  ns/op
 * DataStructureBenchmark.insertGdxOS3           1500  avgt    4   20918.325 ±   8335.336  ns/op
 * DataStructureBenchmark.insertGdxOS3           3000  avgt    4   45803.509 ±  21906.209  ns/op
 * DataStructureBenchmark.insertGdxOS3           6000  avgt    4   85784.976 ±   7762.536  ns/op
 * DataStructureBenchmark.insertGdxOS3          12000  avgt    4  206220.562 ±  73177.228  ns/op
 * DataStructureBenchmark.insertLinkedHashMap    1500  avgt    4   29011.405 ±  12322.492  ns/op
 * DataStructureBenchmark.insertLinkedHashMap    3000  avgt    4   51287.682 ±  32082.172  ns/op
 * DataStructureBenchmark.insertLinkedHashMap    6000  avgt    4  101284.150 ±  16458.354  ns/op
 * DataStructureBenchmark.insertLinkedHashMap   12000  avgt    4  205865.279 ±  38719.626  ns/op
 * DataStructureBenchmark.insertLinkedHashSet    1500  avgt    4   26524.562 ±   9270.593  ns/op
 * DataStructureBenchmark.insertLinkedHashSet    3000  avgt    4   53896.965 ±  12663.896  ns/op
 * DataStructureBenchmark.insertLinkedHashSet    6000  avgt    4  100259.050 ±  18969.343  ns/op
 * DataStructureBenchmark.insertLinkedHashSet   12000  avgt    4  201787.009 ±  58640.467  ns/op
 * DataStructureBenchmark.insertSquidOM          1500  avgt    4   21269.420 ±   7860.741  ns/op
 * DataStructureBenchmark.insertSquidOM          3000  avgt    4   43932.855 ±  12788.508  ns/op
 * DataStructureBenchmark.insertSquidOM          6000  avgt    4   99356.044 ±  28287.373  ns/op
 * DataStructureBenchmark.insertSquidOM         12000  avgt    4  257278.242 ±  59064.138  ns/op
 * DataStructureBenchmark.insertSquidOM2         1500  avgt    4   21865.323 ±   9418.607  ns/op
 * DataStructureBenchmark.insertSquidOM2         3000  avgt    4   43080.641 ±   6645.845  ns/op
 * DataStructureBenchmark.insertSquidOM2         6000  avgt    4   87484.249 ±  13538.247  ns/op
 * DataStructureBenchmark.insertSquidOM2        12000  avgt    4  227234.158 ±   1840.756  ns/op
 * DataStructureBenchmark.insertSquidOM3         1500  avgt    4   23512.106 ±   7544.661  ns/op
 * DataStructureBenchmark.insertSquidOM3         3000  avgt    4   48626.410 ±   8228.162  ns/op
 * DataStructureBenchmark.insertSquidOM3         6000  avgt    4  112120.954 ±  36337.733  ns/op
 * DataStructureBenchmark.insertSquidOM3        12000  avgt    4  243739.316 ± 115929.008  ns/op
 * DataStructureBenchmark.insertSquidOS          1500  avgt    4   13571.493 ±   5466.484  ns/op
 * DataStructureBenchmark.insertSquidOS          3000  avgt    4   26908.465 ±   7551.977  ns/op
 * DataStructureBenchmark.insertSquidOS          6000  avgt    4   92277.265 ±  49265.949  ns/op
 * DataStructureBenchmark.insertSquidOS         12000  avgt    4  211058.166 ±  69232.068  ns/op
 * DataStructureBenchmark.insertSquidOS2         1500  avgt    4   12080.495 ±   1338.051  ns/op
 * DataStructureBenchmark.insertSquidOS2         3000  avgt    4   24012.246 ±   9015.567  ns/op
 * DataStructureBenchmark.insertSquidOS2         6000  avgt    4   55176.285 ±  16451.906  ns/op
 * DataStructureBenchmark.insertSquidOS2        12000  avgt    4  127613.691 ±  52788.568  ns/op
 * DataStructureBenchmark.insertSquidOS3         1500  avgt    4   12985.633 ±   1353.662  ns/op
 * DataStructureBenchmark.insertSquidOS3         3000  avgt    4   25718.108 ±   3405.242  ns/op
 * DataStructureBenchmark.insertSquidOS3         6000  avgt    4   56471.774 ±  27519.086  ns/op
 * DataStructureBenchmark.insertSquidOS3        12000  avgt    4  132271.929 ±  26147.530  ns/op
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 6)
@Measurement(iterations = 4)
public class DataStructureBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        @Param({"1500", "3000", "6000", "12000"})
        public int SIZE;
//        public LinkedHashMap<Integer, Integer> lhm;
//        public OrderedMap<Integer, Integer> squidOM;
//        public com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM;
//        public LinkedHashSet<Integer> lhs;
//        public OrderedSet<Integer> squidOS;
//        public com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS;
//        @Setup(Level.Trial)
//        public void setup() {
//            lhm = new LinkedHashMap<>();
//            squidOM = new OrderedMap<>();
//            gdxOM = new com.badlogic.gdx.utils.OrderedMap<>();
//            lhs = new LinkedHashSet<>();
//            squidOS = new OrderedSet<>();
//            gdxOS = new com.badlogic.gdx.utils.OrderedSet<>();
//        }

    }

    @Benchmark
    public void insertLinkedHashMap(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashMap<Integer, Integer> lhm = new LinkedHashMap<>(state.SIZE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            lhm.put(i, i);
        }
        blackhole.consume(lhm);
    }

    @Benchmark
    public void insertLinkedHashMap2(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashMap<Integer, Integer> lhm = new LinkedHashMap<>(state.SIZE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            lhm.put(i, i);
        }
        blackhole.consume(lhm);
    }

    @Benchmark
    public void insertLinkedHashMap3(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashMap<Integer, Integer> lhm = new LinkedHashMap<>(state.SIZE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            lhm.put(i, i);
        }
        blackhole.consume(lhm);
    }

    @Benchmark
    public void insertSquidOM(BenchmarkState state, Blackhole blackhole)
    {
        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            squidOM.put(i, i);
        }
        blackhole.consume(squidOM);
    }

    @Benchmark
    public void insertSquidOM2(BenchmarkState state, Blackhole blackhole)
    {
        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            squidOM.put(i, i);
        }
        blackhole.consume(squidOM);
    }

    @Benchmark
    public void insertSquidOM3(BenchmarkState state, Blackhole blackhole)
    {
        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            squidOM.put(i, i);
        }
        blackhole.consume(squidOM);
    }

    @Benchmark
    public void insertGdxOM(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM = new com.badlogic.gdx.utils.OrderedMap<>(state.SIZE,0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOM.put(i, i);
        }
        blackhole.consume(gdxOM);
    }

    @Benchmark
    public void insertGdxOM2(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM = new com.badlogic.gdx.utils.OrderedMap<>(state.SIZE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOM.put(i, i);
        }
        blackhole.consume(gdxOM);
    }

    @Benchmark
    public void insertGdxOM3(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM = new com.badlogic.gdx.utils.OrderedMap<>(state.SIZE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOM.put(i, i);
        }
        blackhole.consume(gdxOM);
    }

    @Benchmark
    public void insertLinkedHashSet(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<>(state.SIZE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            lhs.add(i);
        }
        blackhole.consume(lhs);
    }

    @Benchmark
    public void insertLinkedHashSet2(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<>(state.SIZE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            lhs.add(i);
        }
        blackhole.consume(lhs);
    }

    @Benchmark
    public void insertLinkedHashSet3(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<>(state.SIZE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            lhs.add(i);
        }
        blackhole.consume(lhs);
    }

    @Benchmark
    public void insertSquidOS(BenchmarkState state, Blackhole blackhole)
    {
        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            squidOS.add(i);
        }
        blackhole.consume(squidOS);
    }
    @Benchmark
    public void insertSquidOS2(BenchmarkState state, Blackhole blackhole)
    {
        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            squidOS.add(i);
        }
        blackhole.consume(squidOS);
    }
    @Benchmark
    public void insertSquidOS3(BenchmarkState state, Blackhole blackhole)
    {
        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            squidOS.add(i);
        }
        blackhole.consume(squidOS);
    }
    @Benchmark
    public void insertGdxOS(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS = new com.badlogic.gdx.utils.OrderedSet<>(state.SIZE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOS.add(i);
        }
        blackhole.consume(gdxOS);
    }
    @Benchmark
    public void insertGdxOS2(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS = new com.badlogic.gdx.utils.OrderedSet<>(state.SIZE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOS.add(i);
        }
        blackhole.consume(gdxOS);
    }
    @Benchmark
    public void insertGdxOS3(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS = new com.badlogic.gdx.utils.OrderedSet<>(state.SIZE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOS.add(i);
        }
        blackhole.consume(gdxOS);
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
     *    $ java -jar target/benchmarks.jar DataStructureBenchmark -wi 5 -i 5 -f 1 -gc true
     *
     *    (we requested 3 warmup/measurement iterations, single fork, garbage collect between benchmarks)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DataStructureBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .shouldDoGC(true)
                .build();
        new Runner(opt).run();
    }
}
