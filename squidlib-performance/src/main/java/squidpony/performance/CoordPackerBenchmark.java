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
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.StatefulRNG;

import java.util.concurrent.TimeUnit;

public class CoordPackerBenchmark {

    public static final int DIMENSION = 120;
    public static DungeonGenerator dungeonGen =
            new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION,
            new StatefulRNG(0x1337BEEFDEAL));

    public static final char[][][] maps;
    public static final short[][] floors, walls, visibleWalls;
    static {
        serpent.putWalledBoxRoomCarvers(1);
        maps = new char[1024][][];
        floors = new short[1024][];
        walls = new short[1024][];
        visibleWalls = new short[1024][];
        for (int i = 0; i < 1024; i++) {
            maps[i] = dungeonGen.generate(serpent.generate());
            floors[i] = CoordPacker.pack(maps[i], '.');
            walls[i] = CoordPacker.pack(maps[i], '#');
            visibleWalls[i] = CoordPacker.fringe(floors[i], 1, DIMENSION, DIMENSION, true);
        }
    }

    public long doPack()
    {
        long l = 0;
        for (int i = 0; i < 1024; i++) {
            l += CrossHash.hash(CoordPacker.pack(maps[i], '.'));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measurePack() throws InterruptedException {
        long l = doPack();
    }

    public long doUnion()
    {
        long l = 0;
        for (int i = 0; i < 1024; i++) {
            l += CrossHash.hash(CoordPacker.unionPacked(floors[i], visibleWalls[i]));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureUnion() throws InterruptedException {
        long l = doUnion();
    }

    public long doIntersect()
    {
        long l = 0;
        for (int i = 0; i < 1024; i++) {
            l += CrossHash.hash(CoordPacker.intersectPacked(walls[i], visibleWalls[i]));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureIntersect() throws InterruptedException {
        long l = doIntersect();
    }

    public long doFringe()
    {
        long l = 0;
        for (int i = 0; i < 1024; i++) {
            l += CrossHash.hash(CoordPacker.fringe(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureFringe() throws InterruptedException {
        long l = doFringe();
    }

    public long doExpand()
    {
        long l = 0;
        for (int i = 0; i < 1024; i++) {
            l += CrossHash.hash(CoordPacker.expand(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureExpand() throws InterruptedException {
        long l = doExpand();
    }

    public long doSurface()
    {
        long l = 0;
        for (int i = 0; i < 1024; i++) {
            l += CrossHash.hash(CoordPacker.surface(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureSurface() throws InterruptedException {
        long l = doSurface();
    }
    public long doRetract()
    {
        long l = 0;
        for (int i = 0; i < 1024; i++) {
            l += CrossHash.hash(CoordPacker.retract(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureRetract() throws InterruptedException {
        long l = doRetract();
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
     *    $ java -jar target/benchmarks.jar CoordPackerBenchmark -wi 3 -i 3 -f 1
     *
     *    (we requested 3 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CoordPackerBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(1)
                .build();


        new Runner(opt).run();
    }


}
