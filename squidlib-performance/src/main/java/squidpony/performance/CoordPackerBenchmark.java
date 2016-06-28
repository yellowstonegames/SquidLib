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
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.StatefulRNG;

import java.util.concurrent.TimeUnit;

/**
 *
 Benchmark                              Mode  Cnt           Score           Error     Units
 CoordPackerBenchmark.measureExpand     avgt    3    900228206.833 ±    21968143.986  ns/op
 CoordPackerBenchmark.measureFringe     avgt    3    641602426.833 ±    27212281.762  ns/op
 CoordPackerBenchmark.measureIntersect  avgt    3     43690482.937 ±     5320462.022  ns/op
 CoordPackerBenchmark.measurePack       avgt    3    109464965.333 ±     6017179.639  ns/op
 CoordPackerBenchmark.measureRetract    avgt    3  13193960943.000 ±   242817831.253  ns/op
 CoordPackerBenchmark.measureSurface    avgt    3  13620270404.000 ± 10192036140.599  ns/op
 CoordPackerBenchmark.measureUnion      avgt    3     23350198.759 ±     1748064.376  ns/op

 CoordPackerBenchmark.measureExpand     avgt    3    873295078.833 ±     9816457.477  ns/op
 CoordPackerBenchmark.measureFringe     avgt    3    662366321.333 ±   102458917.511  ns/op
 CoordPackerBenchmark.measureIntersect  avgt    3     42639848.333 ±     1993339.568  ns/op
 CoordPackerBenchmark.measurePack       avgt    3    112526685.556 ±     1660470.101  ns/op
 CoordPackerBenchmark.measureRetract    avgt    3   2898240290.667 ±    70354485.221  ns/op
 CoordPackerBenchmark.measureSurface    avgt    3   2668457224.000 ±   208057266.995  ns/op
 CoordPackerBenchmark.measureUnion      avgt    3     24108143.476 ±     1636907.341  ns/op

 CoordPackerBenchmark.measureExpand     avgt    3    903545748.333 ±    64662153.123  ns/op
 CoordPackerBenchmark.measureFringe     avgt    3    648745341.000 ±    35213867.865  ns/op
 CoordPackerBenchmark.measureIntersect  avgt    3     42401227.361 ±     6245961.734  ns/op
 CoordPackerBenchmark.measurePack       avgt    3    110786137.533 ±     4217588.965  ns/op
 CoordPackerBenchmark.measureRetract    avgt    3   1429771405.667 ±    52878650.572  ns/op
 CoordPackerBenchmark.measureSurface    avgt    3   1440772193.000 ±   102585931.893  ns/op
 CoordPackerBenchmark.measureUnion      avgt    3     23457633.434 ±      394246.228  ns/op

 And now with the GreasedRegion code being tested with an appended G in the test name...
 These use a larger amount of maps (4096), but each map is 64x64 instead of 120x120.
 The 64 for y happens to be an optimal amount for GreasedRegion.

 Benchmark                               Mode  Cnt      Score      Error  Units
 CoordPackerBenchmark.measureExpand      avgt    3    661.192 ±  161.486  ms/op
 CoordPackerBenchmark.measureExpandG     avgt    3      3.936 ±    0.650  ms/op
 CoordPackerBenchmark.measureFringe      avgt    3    586.423 ±   61.605  ms/op
 CoordPackerBenchmark.measureFringeG     avgt    3      5.300 ±    0.745  ms/op
 CoordPackerBenchmark.measureIntersect   avgt    3     63.087 ±   38.368  ms/op
 CoordPackerBenchmark.measureIntersectG  avgt    3      4.115 ±    1.625  ms/op
 CoordPackerBenchmark.measurePack        avgt    3     78.218 ±   18.208  ms/op
 CoordPackerBenchmark.measurePackG       avgt    3     14.176 ±    3.376  ms/op
 CoordPackerBenchmark.measureRetract     avgt    3  19580.121 ± 1914.288  ms/op
 CoordPackerBenchmark.measureRetractG    avgt    3      3.805 ±    0.219  ms/op
 CoordPackerBenchmark.measureSurface     avgt    3  19640.031 ± 3848.563  ms/op
 CoordPackerBenchmark.measureSurfaceG    avgt    3      5.159 ±    2.001  ms/op
 CoordPackerBenchmark.measureUnion       avgt    3     36.260 ±    4.809  ms/op
 CoordPackerBenchmark.measureUnionG      avgt    3      4.054 ±    0.355  ms/op

 This is a relatively more-even measurement that uses 8-way (Chebyshev) distance measurement.
 This causes corners to be included in the "visible walls" packed maps, which helps reduce
 the amount of load the non-G (CoordPacker-based) code has to process.
 GreasedRegion is still massively faster.
 Benchmark                               Mode  Cnt     Score     Error  Units
 CoordPackerBenchmark.measureExpand      avgt    3   676.298 ± 101.887  ms/op
 CoordPackerBenchmark.measureExpandG     avgt    3     4.104 ±   1.221  ms/op
 CoordPackerBenchmark.measureFringe      avgt    3   692.355 ±  66.108  ms/op
 CoordPackerBenchmark.measureFringeG     avgt    3     5.434 ±   0.108  ms/op
 CoordPackerBenchmark.measureIntersect   avgt    3    60.514 ±  12.220  ms/op
 CoordPackerBenchmark.measureIntersectG  avgt    3     4.079 ±   0.542  ms/op
 CoordPackerBenchmark.measurePack        avgt    3    94.342 ±  89.863  ms/op
 CoordPackerBenchmark.measurePackG       avgt    3    12.704 ±   3.615  ms/op
 CoordPackerBenchmark.measureRetract     avgt    3  3055.399 ± 418.414  ms/op
 CoordPackerBenchmark.measureRetractG    avgt    3     4.034 ±   0.577  ms/op
 CoordPackerBenchmark.measureSurface     avgt    3  3820.416 ± 185.474  ms/op
 CoordPackerBenchmark.measureSurfaceG    avgt    3     5.305 ±   1.184  ms/op
 CoordPackerBenchmark.measureUnion       avgt    3    36.320 ±  10.058  ms/op
 CoordPackerBenchmark.measureUnionG      avgt    3     4.078 ±   0.828  ms/op
 */
public class CoordPackerBenchmark {

    public static final int DIMENSION = 64;
    public static DungeonGenerator dungeonGen =
            new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION,
            new StatefulRNG(0x1337BEEFDEAL));

    public static final char[][][] maps;
    public static final short[][] floors, walls, visibleWalls;
    public static GreasedRegion[] floorsG, wallsG, visibleWallsG;
    static {
        serpent.putWalledBoxRoomCarvers(1);
        maps = new char[0x1000][][];
        floors = new short[0x1000][];
        walls = new short[0x1000][];
        visibleWalls = new short[0x1000][];
        floorsG = new GreasedRegion[0x1000];
        wallsG = new GreasedRegion[0x1000];
        visibleWallsG = new GreasedRegion[0x1000];
        for (int i = 0; i < 0x1000; i++) {
            maps[i] = dungeonGen.generate(serpent.generate());
            floors[i] = CoordPacker.pack(maps[i], '.');
            walls[i] = CoordPacker.pack(maps[i], '#');
            visibleWalls[i] = CoordPacker.fringe(floors[i], 1, DIMENSION, DIMENSION, true);
            floorsG[i] = new GreasedRegion(maps[i], '.');
            wallsG[i] = new GreasedRegion(maps[i], '#');
            visibleWallsG[i] = new GreasedRegion(floorsG[i]).fringe8way();
        }
    }

    public long doPack()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.pack(maps[i], '.'));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePack() throws InterruptedException {
        long l = doPack();
    }

    public long doUnion()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.unionPacked(floors[i], visibleWalls[i]));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureUnion() throws InterruptedException {
        long l = doUnion();
    }

    public long doIntersect()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.intersectPacked(walls[i], visibleWalls[i]));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureIntersect() throws InterruptedException {
        long l = doIntersect();
    }

    public long doFringe()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.fringe(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureFringe() throws InterruptedException {
        long l = doFringe();
    }

    public long doExpand()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.expand(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureExpand() throws InterruptedException {
        long l = doExpand();
    }

    public long doSurface()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.surface(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSurface() throws InterruptedException {
        long l = doSurface();
    }
    public long doRetract()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.retract(floors[i], 1, DIMENSION, DIMENSION, true));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureRetract() throws InterruptedException {
        long l = doRetract();
    }
/*
    public long doRetract1()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.differencePacked(floors[i], CoordPacker.fringe(CoordPacker.negatePacked(floors[i]), 1, DIMENSION, DIMENSION, true, true)));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureRetract1() throws InterruptedException {
        System.out.print("Retract 1: ");
        System.out.println(doRetract1());
    }

    public long doRetract2()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.differencePacked(floors[i], CoordPacker.expand(CoordPacker.negatePacked(floors[i]), 1, DIMENSION, DIMENSION, true)));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureRetract2() throws InterruptedException {
        System.out.print("Retract 2: ");
        System.out.println(doRetract2());
    }

    public long doSurface1()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.intersectPacked(floors[i], CoordPacker.fringe(CoordPacker.negatePacked(floors[i]), 1, DIMENSION, DIMENSION, true, true)));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSurface1() throws InterruptedException {
        System.out.print("Surface 1: ");
        System.out.println(doSurface1());
    }

    public long doSurface2()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += CrossHash.hash(CoordPacker.intersectPacked(floors[i], CoordPacker.expand(CoordPacker.negatePacked(floors[i]), 1, DIMENSION, DIMENSION, true)));
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSurface2() throws InterruptedException {
        System.out.print("Surface 2: ");
        System.out.println(doSurface2());
    }
*/


    public long doPackG()
    {
        long l = 0;
        for (int i = 0; i < 0x1000; i++) {
            l += new GreasedRegion(maps[i], '.').hashCode();
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePackG() throws InterruptedException {
        long l = doPackG();
    }

    public long doUnionG()
    {
        long l = 0;
        GreasedRegion tmp = new GreasedRegion(floorsG[0]);
        for (int i = 0; i < 0x1000; i++) {
            l += tmp.remake(floorsG[i]).or(visibleWallsG[i]).hashCode();
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureUnionG() throws InterruptedException {
        long l = doUnionG();
    }

    public long doIntersectG()
    {
        long l = 0;
        GreasedRegion tmp = new GreasedRegion(floorsG[0]);
        for (int i = 0; i < 0x1000; i++) {
            l += tmp.remake(wallsG[i]).and(visibleWallsG[i]).hashCode();
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureIntersectG() throws InterruptedException {
        long l = doIntersectG();
    }

    public long doFringeG()
    {
        long l = 0;
        GreasedRegion tmp = new GreasedRegion(floorsG[0]);
        for (int i = 0; i < 0x1000; i++) {
            l += tmp.remake(floorsG[i]).fringe8way().hashCode();
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureFringeG() throws InterruptedException {
        long l = doFringeG();
    }

    public long doExpandG()
    {
        long l = 0;
        GreasedRegion tmp = new GreasedRegion(floorsG[0]);
        for (int i = 0; i < 0x1000; i++) {
            l += tmp.remake(floorsG[i]).expand8way().hashCode();
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureExpandG() throws InterruptedException {
        long l = doExpandG();
    }

    public long doSurfaceG()
    {
        long l = 0;
        GreasedRegion tmp = new GreasedRegion(floorsG[0]);
        for (int i = 0; i < 0x1000; i++) {
            l += tmp.remake(floorsG[i]).surface8way().hashCode();
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureSurfaceG() throws InterruptedException {
        long l = doSurfaceG();
    }
    public long doRetractG()
    {
        long l = 0;
        GreasedRegion tmp = new GreasedRegion(floorsG[0]);
        for (int i = 0; i < 0x1000; i++) {
            l += tmp.remake(floorsG[i]).retract8way().hashCode();
        }
        return l;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureRetractG() throws InterruptedException {
        long l = doRetractG();
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
                .measurementIterations(3)
                .forks(1)
                .build();


        new Runner(opt).run();
    }


}
