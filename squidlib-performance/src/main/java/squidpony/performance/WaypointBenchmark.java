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
import squidpony.squidai.WaypointPathfinder;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.StatefulRNG;

import java.util.concurrent.TimeUnit;

public class WaypointBenchmark {

    public static final int DIMENSION = 60, PATH_LENGTH = (DIMENSION - 2) * (DIMENSION - 2);
    public static DungeonGenerator dungeonGen =
            new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION,
            new StatefulRNG(0x1337BEEFDEAL));

    public static final char[][] map;
    public static final short[] floors;
    static {
        serpent.putRoundRoomCarvers(1);
        map = dungeonGen.generate(serpent.generate());
        System.out.println(dungeonGen.toString());
        floors = CoordPacker.pack(map, '.');
    }

    public WaypointPathfinder waypoint()
    {
        return new WaypointPathfinder(
                map, Radius.SQUARE, new StatefulRNG(0x1337BEEF), false);
    }

    public WaypointPathfinder waypoint2()
    {
        return new WaypointPathfinder(
                map, Radius.SQUARE, new StatefulRNG(0x1337BEEF), true);
    }

    public WaypointPathfinder waypoint3()
    {
        return new WaypointPathfinder(
                map, Radius.SQUARE, new StatefulRNG(0x1337BEEF), 29);
    }
    public long doPath()
    {
        WaypointPathfinder way = waypoint();
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) way.rng).setState((x << 20) | (y << 14) | (x * y));
                r = utility.randomCell(floors);
                way.getKnownPath(r, Coord.get(x, y));
                scanned++;
            }
        }
        return scanned;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePath() throws InterruptedException {
        System.out.println(doPath());
    }

    public long doPath2()
    {
        WaypointPathfinder way = waypoint2();
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) way.rng).setState((x << 20) | (y << 14) | (x * y));
                r = utility.randomCell(floors);
                way.getKnownPath(r, Coord.get(x, y));
                scanned++;
            }
        }
        return scanned;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePath2() throws InterruptedException {
        System.out.println(doPath2());
    }


    public long doPath3()
    {
        WaypointPathfinder way = waypoint3();
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) way.rng).setState((x << 20) | (y << 14) | (x * y));
                r = utility.randomCell(floors);
                way.getKnownPath(r, Coord.get(x, y));
                scanned++;
            }
        }
        return scanned;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePath3() throws InterruptedException {
        System.out.println(doPath3());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureWaypoint() throws InterruptedException {
        WaypointPathfinder w = waypoint();
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureWaypoint2() throws InterruptedException {
        WaypointPathfinder w = waypoint2();
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureWaypoint3() throws InterruptedException {
        WaypointPathfinder w = waypoint3();
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void countWaypoint() throws InterruptedException {
        WaypointPathfinder w = waypoint();
        System.out.println("Dijkstra chokepoint count: " + w.getWaypoints().size());
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void countWaypoint2() throws InterruptedException {
        WaypointPathfinder w = waypoint2();
        System.out.println("Packer chokepoint count: " + w.getWaypoints().size());
    }
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void countWaypoint3() throws InterruptedException {
        WaypointPathfinder w = waypoint3();
        System.out.println("Apart chokepoint count: " + w.getWaypoints().size());
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
     *    $ java -jar target/benchmarks.jar WaypointBenchmark -wi 5 -i 5 -f 1
     *
     *    (we requested 5 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WaypointBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
