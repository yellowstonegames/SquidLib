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
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidmath.*;

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

 Using 8-way for both, 64x64 maps, but putting expand, fringe, retract, and surface through much more work,
 and adding flood as an operation. This tests two different amounts for each of those operations (1 and 2 for
 most, 5 and 10 for flood), 4096 times for each amount. Flood does significantly less work because it only
 expands one point at the start, which is (probably) why the CoordPacker version does relatively well there.

 Benchmark                               Mode  Cnt     Score     Error  Units
 CoordPackerBenchmark.measureExpand      avgt    3  1588.435 ± 177.613  ms/op
 CoordPackerBenchmark.measureExpandG     avgt    3     8.763 ±   1.241  ms/op
 CoordPackerBenchmark.measureFlood       avgt    3   116.499 ±   6.779  ms/op
 CoordPackerBenchmark.measureFloodG      avgt    3    25.283 ±   2.088  ms/op
 CoordPackerBenchmark.measureFringe      avgt    3  1770.287 ± 344.991  ms/op
 CoordPackerBenchmark.measureFringeG     avgt    3    11.999 ±   0.963  ms/op
 CoordPackerBenchmark.measureIntersect   avgt    3    58.724 ±   2.901  ms/op
 CoordPackerBenchmark.measureIntersectG  avgt    3     4.061 ±   0.373  ms/op
 CoordPackerBenchmark.measurePack        avgt    3    78.740 ±   2.093  ms/op
 CoordPackerBenchmark.measurePackG       avgt    3    13.853 ±   1.575  ms/op
 CoordPackerBenchmark.measureRetract     avgt    3  7005.192 ± 112.877  ms/op
 CoordPackerBenchmark.measureRetractG    avgt    3     8.999 ±   1.188  ms/op
 CoordPackerBenchmark.measureSurface     avgt    3  7075.360 ± 264.373  ms/op
 CoordPackerBenchmark.measureSurfaceG    avgt    3    11.288 ±   3.861  ms/op
 CoordPackerBenchmark.measureUnion       avgt    3    34.519 ±   8.301  ms/op
 CoordPackerBenchmark.measureUnionG      avgt    3     4.035 ±   0.842  ms/op

 
 New benchmarks, using JMH's recommendations to test one iteration per benchmark run.
 These are in milliseconds, but the test will now use microseconds because there isn't
 enough precision on the very fast GreasedRegion calls. These benchmarks had a lot of
 extra load on the machine from a separate multi-threaded run of PractRand, so they
 probably take more time than they should.
 
 Benchmark                          Mode  Cnt  Score    Error  Units
 CoordPackerBenchmark.doExpand      avgt    3  1.736 ±  0.155  ms/op
 CoordPackerBenchmark.doExpandG     avgt    3  0.003 ±  0.001  ms/op
 CoordPackerBenchmark.doFlood       avgt    3  0.168 ±  0.007  ms/op
 CoordPackerBenchmark.doFloodG      avgt    3  0.033 ±  0.006  ms/op
 CoordPackerBenchmark.doFringe      avgt    3  1.325 ±  0.139  ms/op
 CoordPackerBenchmark.doFringeG     avgt    3  0.004 ±  0.001  ms/op
 CoordPackerBenchmark.doIntersect   avgt    3  0.060 ±  0.001  ms/op
 CoordPackerBenchmark.doIntersectG  avgt    3  0.001 ±  0.002  ms/op
 CoordPackerBenchmark.doPack        avgt    3  0.160 ±  0.013  ms/op
 CoordPackerBenchmark.doPackG       avgt    3  0.060 ±  0.008  ms/op
 CoordPackerBenchmark.doRandom      avgt    3  0.003 ±  0.001  ms/op
 CoordPackerBenchmark.doRandomG     avgt    3  0.002 ±  0.001  ms/op
 CoordPackerBenchmark.doRetract     avgt    3  1.814 ±  0.998  ms/op
 CoordPackerBenchmark.doRetractG    avgt    3  0.002 ±  0.001  ms/op
 CoordPackerBenchmark.doSurface     avgt    3  1.799 ±  0.207  ms/op
 CoordPackerBenchmark.doSurfaceG    avgt    3  0.004 ±  0.001  ms/op
 CoordPackerBenchmark.doUnion       avgt    3  0.041 ±  0.002  ms/op
 CoordPackerBenchmark.doUnionG      avgt    3  0.001 ±  0.001  ms/op
 
 And slightly newer benchmarks for just the code that gets a single random Coord:
 
 Benchmark                       Mode  Cnt  Score   Error  Units
 CoordPackerBenchmark.doRandom   avgt    3  2.669 ± 0.260  us/op
 CoordPackerBenchmark.doRandomA  avgt    3  0.050 ± 0.003  us/op
 CoordPackerBenchmark.doRandomG  avgt    3  1.612 ± 0.072  us/op
 
 Clearly here, the winner is doRandomA, which gets a single random Coord by indexing
 into an array that was already generated and stored. Using doRandomG requires linear-time
 iteration over the full long array in a GreasedRegion (skipping lots of it), and doRandom
 with CoordPacker is linear-time as well but can't skip as easily.
 
 Benchmarking again, still with lots of load, but also testing GreasedRegion.singleRandomAlt:

 Benchmark                          Mode  Cnt  Score   Error  Units
 CoordPackerBenchmark.doRandom      avgt    3  3.321 ± 0.478  us/op
 CoordPackerBenchmark.doRandomA     avgt    3  0.049 ± 0.026  us/op
 CoordPackerBenchmark.doRandomAltG  avgt    3  1.470 ± 0.040  us/op
 CoordPackerBenchmark.doRandomG     avgt    3  1.597 ± 0.066  us/op

 doRandomAltG does better than the original doRandomG, even though they're very close in code.
 More of GreasedRegion will probably switch over to the technique it uses.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 3)
public class CoordPackerBenchmark {
    @State(Scope.Thread)
    public static class BenchmarkState {
        public int DIMENSION = 128;
        public StatefulRNG srng = new StatefulRNG(0x1337BEEFDEAL);
        public DungeonGenerator dungeonGen = new DungeonGenerator(DIMENSION, DIMENSION, srng);
        public char[][][] maps;
        public short[][] floors, walls, visibleWalls;
        public GreasedRegion[] floorsG, wallsG, visibleWallsG;
        public Coord[][] unpacked;
        public GreasedRegion tmp;
        public char[][] map;
        public int counter = 0;
        @Setup(Level.Trial)
        public void setup() {
            CoordPacker.init();
            map = dungeonGen.generate();
            maps = new char[0x100][][];
            floors = new short[0x100][];
            walls = new short[0x100][];
            visibleWalls = new short[0x100][];
            floorsG = new GreasedRegion[0x100];
            wallsG = new GreasedRegion[0x100];
            visibleWallsG = new GreasedRegion[0x100];
            unpacked = new Coord[0x100][];
            for (int i = 0; i < 0x100; i++) {
                maps[i] = dungeonGen.generate();
                floors[i] = CoordPacker.pack(maps[i], '.');
                walls[i] = CoordPacker.pack(maps[i], '#');
                visibleWalls[i] = CoordPacker.fringe(floors[i], 1, DIMENSION, DIMENSION, true);
                floorsG[i] = new GreasedRegion(maps[i], '.');
                wallsG[i] = new GreasedRegion(maps[i], '#');
                visibleWallsG[i] = new GreasedRegion(floorsG[i]).fringe8way();
                unpacked[i] = floorsG[i].asCoords();
            }
            tmp = new GreasedRegion(floorsG[0]);
        }

    }

    @Benchmark
    public long doPack(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.pack(state.maps[state.counter = state.counter + 1 & 0xFF], '.'));
    }
    @Benchmark
    public long doUnion(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.unionPacked(state.floors[state.counter = state.counter + 1 & 0xFF],
                    state.visibleWalls[state.counter]));
    }

    @Benchmark
    public long doIntersect(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.unionPacked(state.walls[state.counter = state.counter + 1 & 0xFF],
                state.visibleWalls[state.counter]));
    }

    @Benchmark
    public long doFringe(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.fringe(state.floors[state.counter = state.counter + 1 & 0xFF], 1, state.DIMENSION, state.DIMENSION, true));
    }

    @Benchmark
    public long doExpand(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.expand(state.floors[state.counter = state.counter + 1 & 0xFF], 1, state.DIMENSION, state.DIMENSION, true));
    }

    
    @Benchmark
    public long doSurface(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.surface(state.floors[state.counter = state.counter + 1 & 0xFF], 1, state.DIMENSION, state.DIMENSION, true));
    }

    @Benchmark
    public long doRetract(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.retract(state.floors[state.counter = state.counter + 1 & 0xFF], 1, state.DIMENSION, state.DIMENSION, true));
    }

    @Benchmark
    public long doFlood(BenchmarkState state)
    {
        return CrossHash.hash64(CoordPacker.flood(state.floors[state.counter = state.counter + 1 & 0xFF],
                    CoordPacker.packOne(CoordPacker.singleRandom(state.floors[state.counter], state.srng)), 10, true));
    }
    
    @Benchmark
    public int doRandom(BenchmarkState state)
    {
        return CoordPacker.singleRandom(state.floors[state.counter = state.counter + 1 & 0xFF], state.srng).hashCode();
    }
    
    @Benchmark
    public long doPackG(BenchmarkState state)
    {
        return new GreasedRegion(state.maps[state.counter = state.counter + 1 & 0xFF], '.').hash64();
    }

    @Benchmark
    public long doUnionG(BenchmarkState state)
    { 
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).or(state.visibleWallsG[state.counter]).hash64();
    }

    @Benchmark
    public long doIntersectG(BenchmarkState state)
    {
        return state.tmp.remake(state.wallsG[state.counter = state.counter + 1 & 0xFF]).and(state.visibleWallsG[state.counter]).hash64();
    }
    
    @Benchmark
    public long doFringeG(BenchmarkState state)
    {
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).fringe8way().hash64();
    }
    
    @Benchmark
    public long doExpandG(BenchmarkState state)
    {         
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).expand8way().hash64();
    }
    
    @Benchmark
    public long doSurfaceG(BenchmarkState state)
    {
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).surface8way().hash64();
    }

    @Benchmark
    public long doRetractG(BenchmarkState state)
    {
         return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).retract8way().hash64();
    }

    @Benchmark
    public long doFloodG(BenchmarkState state)
    {         
        return state.tmp.empty().insert(state.floorsG[state.counter = state.counter + 1 & 0xFF].singleRandom(state.srng)).flood8way(state.floorsG[state.counter], 10).hash64();
    }

    @Benchmark
    public int doRandomG(BenchmarkState state)
    {
        return state.floorsG[state.counter = state.counter + 1 & 0xFF].singleRandom(state.srng).hashCode();
    }

    @Benchmark
    public void measureMixedSeparatedG(BenchmarkState state, Blackhole blackhole)
    {
        blackhole.consume(state.floorsG[state.counter = state.counter + 1 & 0xFF].mixedRandomSeparated(0.1, 64, 123L));
    }


    @Benchmark
    public void measureMixedSeparatedAltG(BenchmarkState state, Blackhole blackhole)
    {
        blackhole.consume(state.floorsG[state.counter = state.counter + 1 & 0xFF].mixedRandomSeparatedAlt(0.1, 64, 123L));
    }


    @Benchmark
    public long measureMixedRegionG(BenchmarkState state)
    {
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).mixedRandomRegion(0.1, 64, 123L).hash64();
    }

    @Benchmark
    public long measureZCurveRegionG(BenchmarkState state)
    {
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).separatedRegionZCurve(0.1, 64).hash64();
    }

    @Benchmark
    public long measureQuasiRegionG(BenchmarkState state)
    {
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).quasiRandomRegion(0.1, 64).hash64();
    }

    @Benchmark
    public long measureQuasiRegionAltG(BenchmarkState state)
    {
        return state.tmp.remake(state.floorsG[state.counter = state.counter + 1 & 0xFF]).quasiRandomRegionAlt(0.1, 64).hash64();
    }



    @Benchmark
    public int doRandomA(BenchmarkState state)
    {
        return state.srng.getRandomElement(state.unpacked[state.counter = state.counter + 1 & 0xFF]).hashCode();
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
