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

import com.badlogic.gdx.ai.pfa.*;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import squidpony.squidmath.AStarSearch;
import squidpony.squidai.CustomDijkstraMap;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.*;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Times:
 * Benchmark                                        Mode  Cnt     Score     Error  Units
 * DijkstraBenchmark.measurePathAStar               avgt    3  5083.488 ± 997.886  ms/op
 * DijkstraBenchmark.measurePathAStar2              avgt    3   485.639 ±  21.579  ms/op
 * DijkstraBenchmark.measurePathCustomDijkstra      avgt    3   104.302 ±  12.242  ms/op
 * DijkstraBenchmark.measurePathDijkstra            avgt    3   113.594 ±   4.293  ms/op
 * DijkstraBenchmark.measurePathGDXAStar            avgt    3    29.972 ±   0.370  ms/op
 * DijkstraBenchmark.measureTinyPathAStar2          avgt    3     7.099 ±   0.192  ms/op
 * DijkstraBenchmark.measureTinyPathCustomDijkstra  avgt    3    12.209 ±  22.552  ms/op
 * DijkstraBenchmark.measureTinyPathDijkstra        avgt    3    25.559 ±   1.930  ms/op
 * DijkstraBenchmark.measureTinyPathGDXAStar        avgt    3     1.119 ±   0.100  ms/op
 *
 * December 21 2016, different hardware:
 * Benchmark                                        Mode  Cnt     Score      Error  Units
 * DijkstraBenchmark.measurePathAStar               avgt    3  5850.311 ± 404.757  ms/op
 * DijkstraBenchmark.measurePathAStar2              avgt    3   560.238 ± 189.030  ms/op
 * DijkstraBenchmark.measurePathBoxedDijkstra       avgt    3   209.980 ±  66.035  ms/op // not tested before
 * DijkstraBenchmark.measurePathCustomDijkstra      avgt    3   121.427 ±  38.127  ms/op
 * DijkstraBenchmark.measurePathDijkstra            avgt    3   130.467 ±  88.321  ms/op
 * DijkstraBenchmark.measurePathGDXAStar            avgt    3    34.108 ±  17.086  ms/op
 * DijkstraBenchmark.measureTinyPathAStar2          avgt    3     8.278 ±   1.149  ms/op
 * DijkstraBenchmark.measureTinyPathCustomDijkstra  avgt    3    14.551 ±  14.218  ms/op
 * DijkstraBenchmark.measureTinyPathDijkstra        avgt    3    28.658 ±  13.810  ms/op
 * DijkstraBenchmark.measureTinyPathGDXAStar        avgt    3     1.245 ±   0.265  ms/op
 *
 * Benchmark                                        Mode  Cnt      Score       Error  Units
 * DijkstraBenchmark.measurePathAStar2              avgt    3  53480.665 ± 18729.945  ms/op
 * DijkstraBenchmark.measurePathCustomDijkstra      avgt    3   2723.753 ±   925.120  ms/op
 * DijkstraBenchmark.measurePathDijkstra            avgt    3   3074.905 ±   540.216  ms/op
 * DijkstraBenchmark.measurePathGDXAStar            avgt    3   1107.468 ±  1023.402  ms/op
 * DijkstraBenchmark.measurePathOptDijkstra         avgt    3   1785.539 ±   292.576  ms/op // used int distances
 * DijkstraBenchmark.measureScanCustomDijkstra      avgt    3   2646.074 ±   265.886  ms/op
 * DijkstraBenchmark.measureScanDijkstra            avgt    3   2934.270 ±   299.965  ms/op
 * DijkstraBenchmark.measureScanOptDijkstra         avgt    3   1681.452 ±   502.217  ms/op // used int distances
 * DijkstraBenchmark.measureTinyPathAStar2          avgt    3     54.409 ±     6.358  ms/op
 * DijkstraBenchmark.measureTinyPathCustomDijkstra  avgt    3    515.103 ±   483.498  ms/op
 * DijkstraBenchmark.measureTinyPathDijkstra        avgt    3    612.636 ±   105.496  ms/op
 * DijkstraBenchmark.measureTinyPathGDXAStar        avgt    3      5.473 ±     1.066  ms/op
 * DijkstraBenchmark.measureTinyPathOptDijkstra     avgt    3    126.647 ±    25.790  ms/op // used int distances
 *
 * Benchmark                                        Mode  Cnt     Score     Error  Units
 * DijkstraBenchmark.measurePathCustomDijkstra      avgt    3  2622.331 ± 398.498  ms/op
 * DijkstraBenchmark.measurePathDijkstra            avgt    3  2890.659 ± 621.237  ms/op
 * DijkstraBenchmark.measurePathGDXAStar            avgt    3  1028.622 ±  80.724  ms/op
 * DijkstraBenchmark.measurePathOptDijkstra         avgt    3  1685.116 ± 138.863  ms/op
 * DijkstraBenchmark.measureScanCustomDijkstra      avgt    3  2559.513 ± 475.556  ms/op
 * DijkstraBenchmark.measureScanDijkstra            avgt    3  2851.652 ± 760.477  ms/op
 * DijkstraBenchmark.measureScanOptDijkstra         avgt    3  1349.320 ± 102.441  ms/op
 * DijkstraBenchmark.measureTinyPathCustomDijkstra  avgt    3   490.634 ±  69.431  ms/op
 * DijkstraBenchmark.measureTinyPathDijkstra        avgt    3   629.138 ± 153.702  ms/op
 * DijkstraBenchmark.measureTinyPathGDXAStar        avgt    3     5.444 ±   0.473  ms/op
 * DijkstraBenchmark.measureTinyPathOptDijkstra     avgt    3   159.189 ±  15.194  ms/op
 *
 * Benchmark                                        Mode  Cnt     Score     Error  Units
 * DijkstraBenchmark.measurePathCustomDijkstra      avgt    3  3004.285 ± 632.953  ms/op
 * DijkstraBenchmark.measurePathDijkstra            avgt    3  1328.862 ± 285.102  ms/op //with "NextDijkstraMap" update
 * DijkstraBenchmark.measurePathGDXAStar            avgt    3  1207.328 ± 441.112  ms/op
 * DijkstraBenchmark.measurePathOldDijkstra         avgt    3  2463.782 ± 534.747  ms/op
 * DijkstraBenchmark.measurePathOptDijkstra         avgt    3  1957.263 ± 553.280  ms/op
 * DijkstraBenchmark.measureScanCustomDijkstra      avgt    3  2909.871 ± 636.707  ms/op
 * DijkstraBenchmark.measureScanDijkstra            avgt    3  1232.099 ± 393.546  ms/op //with "NextDijkstraMap" update
 * DijkstraBenchmark.measureScanOldDijkstra         avgt    3  2270.479 ± 961.130  ms/op
 * DijkstraBenchmark.measureScanOptDijkstra         avgt    3  1566.125 ± 218.588  ms/op
 * DijkstraBenchmark.measureTinyPathCustomDijkstra  avgt    3   516.101 ± 199.369  ms/op
 * DijkstraBenchmark.measureTinyPathDijkstra        avgt    3   189.029 ±  54.750  ms/op //with "NextDijkstraMap" update
 * DijkstraBenchmark.measureTinyPathGDXAStar        avgt    3     5.834 ±   1.558  ms/op
 * DijkstraBenchmark.measureTinyPathOldDijkstra     avgt    3   676.074 ± 138.026  ms/op
 * DijkstraBenchmark.measureTinyPathOptDijkstra     avgt    3   174.504 ±  36.419  ms/op
 *
 * This next section seems slower all around, probably because the group requires the number the calculations return to
 * be printed (preventing the compiler from omitting certain parts as dead code). This should be more accurate.
 *
 * Benchmark                                        Mode  Cnt     Score      Error  Units
 * DijkstraBenchmark.measurePathCustomDijkstra      avgt    3  5578.118 ± 1638.982  ms/op
 * DijkstraBenchmark.measurePathDijkstra            avgt    3  1412.226 ±  116.214  ms/op //woo! (long paths here)
 * DijkstraBenchmark.measurePathGDXAStar            avgt    3  2291.534 ±  526.744  ms/op //not the best any more!
 * DijkstraBenchmark.measurePathOldDijkstra         avgt    3  4269.398 ±  258.163  ms/op
 * DijkstraBenchmark.measurePathOptDijkstra         avgt    3  3608.808 ±  314.844  ms/op
 * DijkstraBenchmark.measureScanCustomDijkstra      avgt    3  2621.239 ±   61.222  ms/op
 * DijkstraBenchmark.measureScanDijkstra            avgt    3  1198.388 ±  245.992  ms/op
 * DijkstraBenchmark.measureScanOldDijkstra         avgt    3  2007.153 ±  713.720  ms/op
 * DijkstraBenchmark.measureScanOptDijkstra         avgt    3  1429.810 ±  293.400  ms/op
 * DijkstraBenchmark.measureTinyPathCustomDijkstra  avgt    3   962.297 ±  168.867  ms/op
 * DijkstraBenchmark.measureTinyPathDijkstra        avgt    3   185.928 ±  110.203  ms/op //not as good on short paths
 * DijkstraBenchmark.measureTinyPathGDXAStar        avgt    3    12.453 ±    3.873  ms/op
 * DijkstraBenchmark.measureTinyPathOldDijkstra     avgt    3  1362.067 ±  176.038  ms/op
 * DijkstraBenchmark.measureTinyPathOptDijkstra     avgt    3   376.765 ±   34.868  ms/op
 */
public class DijkstraBenchmark {

    public static final int DIMENSION = 128, PATH_LENGTH = (DIMENSION - 2) * (DIMENSION - 2);
    public static DungeonGenerator dungeonGen =
            new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION,
            new StatefulRNG(0x1337BEEFDEAL));
    public static char[][] map;
    public static double[][] astarMap;
    public static GreasedRegion floors;
    public static double floorCount;
    public static Coord[][] nearbyMap;
    public static int[] customNearbyMap;
    public static Adjacency adj;
    static {
        serpent.putWalledBoxRoomCarvers(1);
        map = dungeonGen.generate(serpent.generate());
        floors = new GreasedRegion(map, '.');
        floorCount = floors.size();
        System.out.println("Floors: " + floorCount);
        System.out.println("Percentage walkable: " + floorCount * 100.0 / (DIMENSION * DIMENSION) + "%");
        astarMap = DungeonUtility.generateAStarCostMap(map, Collections.<Character, Double>emptyMap(), 1);
        nearbyMap = new Coord[DIMENSION][DIMENSION];
        customNearbyMap = new int[DIMENSION * DIMENSION];
        GreasedRegion tmp = new GreasedRegion(DIMENSION, DIMENSION);
        adj = new Adjacency.BasicAdjacency(DIMENSION, DIMENSION, DijkstraMap.Measurement.CHEBYSHEV);
        adj.blockingRule = 2;
        StatefulRNG srng = new StatefulRNG(0x1337BEEF1337CA77L);
        Coord c;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if(map[i][j] == '#')
                    continue;
                c = tmp.empty().insert(i, j).flood(floors, 8).remove(i, j).singleRandom(srng);
                nearbyMap[i][j] = c;
                customNearbyMap[adj.composite(i, j, 0, 0)] = adj.composite(c.x, c.y, 0, 0);
            }
        }
    }
    public long doScanDijkstra()
    {
        DijkstraMap dijkstra = new DijkstraMap(
                map, DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(0x1337BEEF));
        dijkstra.setBlockingRequirement(0);

        long scanned = 0;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                dijkstra.setGoal(x, y);
                dijkstra.scan(null);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned++;
            }
        }
        return scanned;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureScanDijkstra() throws InterruptedException {
        System.out.println(doScanDijkstra());
    }

    public long doScanCustomDijkstra()
    {
        CustomDijkstraMap dijkstra = new CustomDijkstraMap(
                map, adj, new StatefulRNG(0x1337BEEF));

        long scanned = 0;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                dijkstra.setGoal(adj.composite(x, y, 0, 0));
                dijkstra.scan(null);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned++;
            }
        }
        return scanned;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureScanCustomDijkstra() throws InterruptedException {
        System.out.println(doScanCustomDijkstra());
    }

    public long doScanGreased()
    {
        Coord[] goals = new Coord[1];
        long scanned = 0;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                goals[0] = Coord.get(x, y);
                scanned += GreasedRegion.dijkstraScan8way(map, goals).length;
            }
        }
        return scanned / DIMENSION;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureScanGreased() throws InterruptedException {
        System.out.println(doScanGreased());
    }

    public long doPathDijkstra()
    {
        DijkstraMap dijkstra = new DijkstraMap(
                map, DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(0x1337BEEF));
        dijkstra.setBlockingRequirement(0);
        Coord r;
        Coord[] tgts = new Coord[1];
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = floors.singleRandom(utility.rng);
                tgts[0] = Coord.get(x, y);
                dijkstra.findPath(PATH_LENGTH, null, null, r, tgts);
                dijkstra.clearGoals();
                scanned += dijkstra.path.size();
            }
        }
        return scanned;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathDijkstra() throws InterruptedException {
        System.out.println(doPathDijkstra() / floorCount);
        doPathDijkstra();
    }

    public long doTinyPathDijkstra()
    {
        DijkstraMap dijkstra = new DijkstraMap(
                map, DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(0x1337BEEF));
        dijkstra.setBlockingRequirement(0);
        Coord r;
        long scanned = 0;
        Coord[] tgts = new Coord[1];
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = nearbyMap[x][y];
                tgts[0] = Coord.get(x, y);
                dijkstra.findPath(9, 9, null, null, r, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size();
            }
        }
        return scanned;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureTinyPathDijkstra() throws InterruptedException {
        System.out.println(doTinyPathDijkstra() / floorCount);
        doTinyPathDijkstra();
    }

    public long doPathCustomDijkstra()
    {
        CustomDijkstraMap dijkstra = new CustomDijkstraMap(
                map, adj, new StatefulRNG(0x1337BEEF));
        Coord r;
        int p;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        int[] tgts = new int[1];
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = floors.singleRandom(utility.rng);
                p = adj.composite(r.x, r.y, 0, 0);
                tgts[0] = adj.composite(x, y, 0, 0);
                dijkstra.findPath(PATH_LENGTH, null, null, p, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size;
            }
        }
        return scanned;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathCustomDijkstra() throws InterruptedException {
        System.out.println(doPathCustomDijkstra() / floorCount);
        doPathCustomDijkstra();
    }

    public long doTinyPathCustomDijkstra()
    {
        CustomDijkstraMap dijkstra = new CustomDijkstraMap(
                map, adj, new StatefulRNG(0x1337BEEF));
        Coord r;
        int p;
        long scanned = 0;
        int[] tgts = new int[1];
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = nearbyMap[x][y];
                p = adj.composite(r.x, r.y, 0, 0);
                tgts[0] = adj.composite(x, y, 0, 0);
                dijkstra.findPath(1,  9,null, null, p, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size;
            }
        }
        return scanned;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureTinyPathCustomDijkstra() throws InterruptedException {
        System.out.println(doTinyPathCustomDijkstra() / floorCount);
        doTinyPathCustomDijkstra();
    }

    public long doScanBoxedDijkstra()
    {
        squidpony.performance.alternate.DijkstraMap dijkstra = new squidpony.performance.alternate.DijkstraMap(
                map, squidpony.performance.alternate.DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(0x1337BEEF));

        long scanned = 0;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                dijkstra.setGoal(x, y);
                dijkstra.scan(null);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned++;
            }
        }
        return scanned;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureScanBoxedDijkstra() throws InterruptedException {
        System.out.println(doScanBoxedDijkstra());
    }

    public long doPathBoxedDijkstra()
    {
        squidpony.performance.alternate.DijkstraMap dijkstra = new squidpony.performance.alternate.DijkstraMap(
                map, squidpony.performance.alternate.DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(0x1337BEEF));
        Coord r;
        long scanned = 0;
        Coord[] tgts = new Coord[1];
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = floors.singleRandom(utility.rng);
                tgts[0] = Coord.get(x, y);
                dijkstra.findPath(PATH_LENGTH, null, null, r, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size();
            }
        }
        return scanned;
    }
    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathBoxedDijkstra() throws InterruptedException {
        //System.out.println(doPathBoxedDijkstra());
        doPathBoxedDijkstra();
    }




    public long doPathAStar()
    {
        squidpony.performance.alternate.AStarSearch astar = new squidpony.performance.alternate.AStarSearch(astarMap, squidpony.performance.alternate.AStarSearch.SearchType.CHEBYSHEV);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        Queue<Coord> latestPath;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                r = floors.singleRandom(utility.rng);
                latestPath = astar.path(r, Coord.get(x, y));
                scanned+= latestPath.size();
            }
        }
        return scanned;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathAStar() throws InterruptedException {
        System.out.println(doPathAStar() / floorCount);
        doPathAStar();
    }

    public long doPathAStar2()
    {
        AStarSearch astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        Queue<Coord> latestPath;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                r = floors.singleRandom(utility.rng);
                latestPath = astar.path(r, Coord.get(x, y));
                scanned+= latestPath.size();
            }
        }
        return scanned;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathAStar2() throws InterruptedException {
        //System.out.println(doPathAStar2());
        doPathAStar2();
    }

    public long doTinyPathAStar2()
    {
        AStarSearch astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        Queue<Coord> latestPath;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                r = nearbyMap[x][y];
                latestPath = astar.path(r, Coord.get(x, y));
                scanned += latestPath.size();
            }
        }
        return scanned;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureTinyPathAStar2() throws InterruptedException {
        System.out.println(doTinyPathAStar2() / floorCount);
        doTinyPathAStar2();
    }

    class GridGraph implements IndexedGraph<Coord>
    {
        public ObjectIntMap<Coord> points = new ObjectIntMap<>(DIMENSION * DIMENSION);

        public GridGraph(Coord[] pts)
        {
            for (int i = 0; i < pts.length; i++) {
                points.put(pts[i], i);
            }
        }
        @Override
        public int getIndex(Coord node) {
            return points.get(node, -1);
        }

        @Override
        public int getNodeCount() {
            return points.size;
        }

        @Override
        public Array<Connection<Coord>> getConnections(Coord fromNode) {
            Array<Connection<Coord>> conn = new Array<>(false, 8);
            if(map[fromNode.x][fromNode.y] != '.')
                return conn;
            Coord t;
            for (int i = 0; i < 8; i++) {
                t = fromNode.translate(Direction.OUTWARDS[i]);
                if (t.isWithin(DIMENSION, DIMENSION) && map[t.x][t.y] == '.')
                    conn.add(new DefaultConnection<>(fromNode, t));
            }
            return conn;
        }
    }

    public long doPathGDXAStar()
    {
        IndexedAStarPathFinder<Coord> astar = new IndexedAStarPathFinder<Coord>(new GridGraph(floors.asCoords()));
        GraphPath<Coord> dgp = new DefaultGraphPath<Coord>(PATH_LENGTH);
        Heuristic<Coord> heu = new Heuristic<Coord>() {
            @Override
            public float estimate(Coord node, Coord endNode) {
                return Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y);
            }
        };
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                r = floors.singleRandom(utility.rng);
                dgp.clear();
                if(astar.searchNodePath(r, Coord.get(x, y), heu, dgp))
                    scanned+= dgp.getCount();
            }
        }
        return scanned;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathGDXAStar() throws InterruptedException {
        System.out.println(doPathGDXAStar() / floorCount);
        doPathGDXAStar();
    }

    public long doTinyPathGDXAStar()
    {
        IndexedAStarPathFinder<Coord> astar = new IndexedAStarPathFinder<Coord>(new GridGraph(floors.asCoords()));
        GraphPath<Coord> dgp = new DefaultGraphPath<Coord>(9);
        Heuristic<Coord> heu = new Heuristic<Coord>() {
            @Override
            public float estimate(Coord node, Coord endNode) {
                return Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y);
            }
        };
        Coord r;
        long scanned = 0;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                r = nearbyMap[x][y];
                dgp.clear();
                if(astar.searchNodePath(r, Coord.get(x, y), heu, dgp))
                    scanned+= dgp.getCount();
            }
        }
        return scanned;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureTinyPathGDXAStar() throws InterruptedException {
        System.out.println(doTinyPathGDXAStar() / floorCount);
        doTinyPathGDXAStar();
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
     *    $ java -jar target/benchmarks.jar DijkstraBenchmark -wi 3 -i 3 -f 1 -gc true
     *
     *    (we requested 3 warmup/measurement iterations, single fork, garbage collect between benchmarks)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DijkstraBenchmark.class.getSimpleName())
                .warmupIterations(3)
                .measurementIterations(3)
                .forks(1)
                .shouldDoGC(true)
                .build();
        new Runner(opt).run();
    }
}

/*
    public long doPathPlannedAStar()
    {
        PlannedAStar astar = new PlannedAStar(astarMap, AStarSearch.SearchType.CHEBYSHEV);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                r = utility.randomCell(floors);
                astar.path(r, Coord.get(x, y));
                scanned++;
            }
        }
        return scanned;
    }
    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathPlannedAStar() throws InterruptedException {
        System.out.println(doPathPlannedAStar());
    }
    */

