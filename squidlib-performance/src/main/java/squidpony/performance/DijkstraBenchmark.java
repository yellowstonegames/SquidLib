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
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import space.earlygrey.simplegraphs.UndirectedGraph;
import squidpony.squidai.CustomDijkstraMap;
import squidpony.squidai.DijkstraMap;
import squidpony.squidai.astar.DefaultGraph;
import squidpony.squidai.astar.Pathfinder;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.AStarSearch;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static squidpony.squidgrid.Measurement.CHEBYSHEV;

/**
 * Times:
 * These benchmark results are split up based on the size of the map, which has major implications for performance.
 * We compare DijkstraMap from SquidLib, CustomDijkstraMap from SquidLib, Pathfinder from SquidLib (called IndexedAStar
 * here), AStarSearch from SquidLib, and IndexedAStarPathFinder from gdx-ai (called GDXAStar here). Pathfinder is
 * closely related to libGDX's IndexedAStarPathFinder, and AStarSearch mostly just wraps SquidLib's Pathfinder. All are
 * generally excellent performers on many maps, but features are a mixed bag. While AStar allows setting a cost to
 * traverse between cells, it can't reuse an already-scanned map like DijkstraMap can. DijkstraMap technically doesn't
 * use Dijkstra's Pathfinding Algorithm, and is much closer to breadth-first search; it produces what roguelike
 * developers call a Dijkstra map because it is so similar to what Dijkstra's algorithm uses. The map produced by
 * scanning with DijkstraMap is a 2D double array of distances; this can be reused and multiple goals can simultaneously
 * be sought to find the closest. CustomDijkstraMap allows specifying adjacency in unusual ways (like considering facing
 * direction when calculating cost). It is otherwise much like DijkstraMap, but slower.
 * <br>
 * SquidLib's Pathfinder class does best here, outpacing the IndexedAStarPathFinder from gdx-ai it is based on. In
 * between the two is AStarSearch, which is just a tiny bit slower than Pathfinder; since AStarSearch is just a wrapper
 * around Pathfinder, that's expected. Behind all of the AStar implementations is DijkstraMap, and way behind that is
 * CustomDijkstraMap. It's somewhat surprising that DijkstraMap does as well as it does, despite being technically an
 * inferior algorithm for raw speed. CustomDijkstraMap has not fared well over the years, and it's the slowest.
 * There's still strong reasons to use DijkstraMap for all sorts of situations; it allows multiple goals where AStar
 * does not; it supports fleeing and keep-minimum-distance pathfinding while AStar struggles to do so, etc.
 * <br>
 * (These use JMH's recommendations for benchmarking; older benchmark results, which weren't set up correctly, are
 * available in the Git history of this file. The older benchmarks included repeated initialization and GC of
 * DijkstraMap, CustomDijkstraMap, or other similar objects in the time per benchmark.)
 * <pre>
 * OpenJDK 8, HotSpot, Windows 7, 6th generation i7 mobile processor:
 *                       This is the relevant measurement,
 *                       time in ms to find all paths -> +-------+
 * Map size: 64x64, 2328 paths                           |       |
 * Benchmark                                   Mode  Cnt    Score    Error  Units
 * DijkstraBenchmark.doPathAStarSearch         avgt    3  183.527 ±  6.912  ms/op
 * DijkstraBenchmark.doPathCustomDijkstra      avgt    3  522.064 ± 45.927  ms/op
 * DijkstraBenchmark.doPathDijkstra            avgt    3  271.181 ±  1.418  ms/op
 * DijkstraBenchmark.doPathGDXAStar            avgt    3  241.030 ± 52.309  ms/op
 * DijkstraBenchmark.doPathIndexedAStar        avgt    3  175.329 ±  4.674  ms/op
 * DijkstraBenchmark.doScanCustomDijkstra      avgt    3  837.541 ± 17.253  ms/op
 * DijkstraBenchmark.doScanDijkstra            avgt    3  531.719 ± 10.016  ms/op
 * DijkstraBenchmark.doTinyPathAStarSearch     avgt    3    5.082 ±  0.041  ms/op
 * DijkstraBenchmark.doTinyPathCustomDijkstra  avgt    3   32.134 ±  0.805  ms/op
 * DijkstraBenchmark.doTinyPathDijkstra        avgt    3   12.706 ±  0.357  ms/op
 * DijkstraBenchmark.doTinyPathGDXAStar        avgt    3    6.393 ±  0.085  ms/op
 * DijkstraBenchmark.doTinyPathIndexedAStar    avgt    3    4.930 ±  0.088  ms/op
 * 
 * OpenJDK 8, HotSpot, Manjaro Linux, 8th generation i7 mobile processor:
 * 
 * Map size: 64x64, 2328 paths
 * Benchmark                                   Mode  Cnt    Score    Error  Units
 * DijkstraBenchmark.doPathAStarSearch         avgt    3  140.095 ±  9.482  ms/op
 * DijkstraBenchmark.doPathCustomDijkstra      avgt    3  420.551 ± 15.983  ms/op
 * DijkstraBenchmark.doPathDijkstra            avgt    3  216.742 ±  0.740  ms/op
 * DijkstraBenchmark.doPathGDXAStar            avgt    3  177.419 ±  4.942  ms/op
 * DijkstraBenchmark.doPathIndexedAStar        avgt    3  135.960 ± 11.782  ms/op
 * DijkstraBenchmark.doScanCustomDijkstra      avgt    3  775.545 ±  6.987  ms/op
 * DijkstraBenchmark.doScanDijkstra            avgt    3  395.923 ±  1.715  ms/op
 * DijkstraBenchmark.doTinyPathAStarSearch     avgt    3    3.744 ±  0.143  ms/op
 * DijkstraBenchmark.doTinyPathCustomDijkstra  avgt    3   18.260 ±  0.211  ms/op
 * DijkstraBenchmark.doTinyPathDijkstra        avgt    3    9.405 ±  0.157  ms/op
 * DijkstraBenchmark.doTinyPathGDXAStar        avgt    3    4.951 ±  0.133  ms/op
 * DijkstraBenchmark.doTinyPathIndexedAStar    avgt    3    3.654 ±  0.204  ms/op
 *
 * Map size: 128x128, 9641 paths
 * Benchmark                                   Mode  Cnt      Score     Error  Units
 * DijkstraBenchmark.doPathAStarSearch         avgt    3   2294.640 ±  24.989  ms/op
 * DijkstraBenchmark.doPathCustomDijkstra      avgt    3   6868.222 ± 698.193  ms/op
 * DijkstraBenchmark.doPathDijkstra            avgt    3   4578.575 ±  62.808  ms/op
 * DijkstraBenchmark.doPathGDXAStar            avgt    3   2855.066 ± 187.862  ms/op
 * DijkstraBenchmark.doPathIndexedAStar        avgt    3   2236.199 ±  34.554  ms/op
 * DijkstraBenchmark.doScanCustomDijkstra      avgt    3  14734.464 ± 206.248  ms/op
 * DijkstraBenchmark.doScanDijkstra            avgt    3   8404.663 ±  88.652  ms/op
 * DijkstraBenchmark.doTinyPathAStarSearch     avgt    3     17.165 ±   1.250  ms/op
 * DijkstraBenchmark.doTinyPathCustomDijkstra  avgt    3    614.386 ±  10.816  ms/op
 * DijkstraBenchmark.doTinyPathDijkstra        avgt    3    107.820 ±  16.759  ms/op
 * DijkstraBenchmark.doTinyPathGDXAStar        avgt    3     22.112 ±   0.248  ms/op
 * DijkstraBenchmark.doTinyPathIndexedAStar    avgt    3     15.985 ±   0.823  ms/op
 * 
 * Map size: 192x192, 21952 paths
 * Benchmark                                   Mode  Cnt      Score      Error  Units
 * DijkstraBenchmark.doPathAStarSearch         avgt    3  11109.066 ±  180.136  ms/op
 * DijkstraBenchmark.doPathCustomDijkstra      avgt    3  39066.989 ±  868.797  ms/op
 * DijkstraBenchmark.doPathDijkstra            avgt    3  23162.759 ±  280.902  ms/op
 * DijkstraBenchmark.doPathGDXAStar            avgt    3  14047.876 ±  411.378  ms/op
 * DijkstraBenchmark.doPathIndexedAStar        avgt    3  10894.056 ±  306.873  ms/op
 * DijkstraBenchmark.doScanCustomDijkstra      avgt    3  72477.940 ± 1990.736  ms/op
 * DijkstraBenchmark.doScanDijkstra            avgt    3  45851.880 ±  222.875  ms/op
 * DijkstraBenchmark.doTinyPathAStarSearch     avgt    3     41.106 ±    4.068  ms/op
 * DijkstraBenchmark.doTinyPathCustomDijkstra  avgt    3   3342.474 ±   43.997  ms/op
 * DijkstraBenchmark.doTinyPathDijkstra        avgt    3    592.185 ±   33.866  ms/op
 * DijkstraBenchmark.doTinyPathGDXAStar        avgt    3     53.976 ±    1.257  ms/op
 * DijkstraBenchmark.doTinyPathIndexedAStar    avgt    3     37.959 ±    0.801  ms/op
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 4)
@Measurement(iterations = 3)
public class DijkstraBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        public int DIMENSION = 64;
        public DungeonGenerator dungeonGen = new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
        //public SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
        public char[][] map;
        public double[][] astarMap;
        public GreasedRegion floors;
        public int floorCount;
        public Coord[] floorArray;
        public Coord[][] nearbyMap;
        public int[] customNearbyMap;
        public Adjacency adj;
        public DijkstraMap dijkstra;
        public CustomDijkstraMap customDijkstra;
        public StatefulRNG srng;
        public GridGraph gg;
        public DefaultGraph dg;
        public IndexedAStarPathFinder<Coord> astar;
        public Pathfinder<Coord> iasSquid;
        public AStarSearch as;
        public GraphPath<Coord> dgp;
        public ArrayList<Coord> path;

        public UndirectedGraph<Coord> simpleGraph;
        public space.earlygrey.simplegraphs.Heuristic<Coord> simpleHeu;
        @Setup(Level.Trial)
        public void setup() {
            Coord.expandPoolTo(DIMENSION, DIMENSION);
            //serpent.putWalledBoxRoomCarvers(1);
            //map = dungeonGen.generate(serpent.generate());
            map = dungeonGen.generate();
            floors = new GreasedRegion(map, '.');
            floorCount = floors.size();
            floorArray = floors.asCoords();
            System.out.println("Floors: " + floorCount);
            System.out.println("Percentage walkable: " + floorCount * 100.0 / (DIMENSION * DIMENSION) + "%");
            astarMap = DungeonUtility.generateAStarCostMap(map, Collections.<Character, Double>emptyMap(), 1);
            as = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
            nearbyMap = new Coord[DIMENSION][DIMENSION];
            customNearbyMap = new int[DIMENSION * DIMENSION];
            GreasedRegion tmp = new GreasedRegion(DIMENSION, DIMENSION);
            adj = new Adjacency.BasicAdjacency(DIMENSION, DIMENSION, CHEBYSHEV);
            adj.blockingRule = 0;
            srng = new StatefulRNG(0x1337BEEF1337CA77L);
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
            dijkstra = new DijkstraMap(map, CHEBYSHEV, new StatefulRNG(0x1337BEEF));
            dijkstra.setBlockingRequirement(0);
            customDijkstra = new CustomDijkstraMap(map, adj, new StatefulRNG(0x1337BEEF));
            gg = new GridGraph(floors, map);
            astar = new IndexedAStarPathFinder<>(gg, false);
            dgp = new DefaultGraphPath<>(DIMENSION << 2);
            dg = new DefaultGraph(map, true);
            iasSquid = new Pathfinder<>(dg, false);
            path = new ArrayList<>(DIMENSION << 2);
            
            simpleGraph = new UndirectedGraph<>(floors);
            simpleHeu = new space.earlygrey.simplegraphs.Heuristic<Coord>() {
                @Override
                public float getEstimate(Coord currentNode, Coord targetNode) {
                    return Math.max(currentNode.x - targetNode.x, currentNode.y - targetNode.y);
                }
            };
            Coord center;
            Direction[] outer = Direction.CLOCKWISE;
            Direction dir;
            for (int i = floorCount - 1; i >= 0; i--) {
                center = floorArray[i];
                for (int j = 0; j < 8; j++) {
                    dir = outer[j];
                    if(floors.contains(center.x + dir.deltaX, center.y + dir.deltaY))
                        simpleGraph.addEdge(center, center.translate(dir));
                }
            }
        }

    }

    @Benchmark
    public long doScanDijkstra(BenchmarkState state)
    {
        long scanned = 0;
        final DijkstraMap dijkstra = state.dijkstra;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                dijkstra.setGoal(x, y);
                dijkstra.scan(null, null);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned++;
            }
        }
        return scanned;
    }

 
    @Benchmark
    public long doScanCustomDijkstra(BenchmarkState state)
    {
        CustomDijkstraMap dijkstra = state.customDijkstra;
        long scanned = 0;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                dijkstra.setGoal(state.adj.composite(x, y, 0, 0));
                dijkstra.scan(null);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned++;
            }
        }
        return scanned;
    }

//    @Benchmark
//    public long doScanGreased(BenchmarkState state)
//    {
//        Coord[] goals = new Coord[1];
//        long scanned = 0;
//        for (int x = 1; x < state.DIMENSION - 1; x++) {
//            for (int y = 1; y < state.DIMENSION - 1; y++) {
//                if (state.map[x][y] == '#')
//                    continue;
//                goals[0] = Coord.get(x, y);
//                scanned += GreasedRegion.dijkstraScan8way(state.map, goals).length;
//            }
//        }
//        return scanned;
//    }

    @Benchmark
    public long doPathDijkstra(BenchmarkState state)
    {
        Coord r;
        final Coord[] tgts = new Coord[1];
        long scanned = 0;
        final DijkstraMap dijkstra = state.dijkstra;
        final int PATH_LENGTH = state.DIMENSION * state.DIMENSION;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                //((StatefulRNG) dijkstra.rng).setState(((x << 20) | (y << 14)) ^ (x * y));
                r = state.srng.getRandomElement(state.floorArray);
                tgts[0] = Coord.get(x, y);
                state.path.clear();
                dijkstra.findPath(state.path, PATH_LENGTH, -1, null, null, r, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += state.path.size();
            }
        }
        return scanned;
    }
 
    @Benchmark
    public long doTinyPathDijkstra(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        final Coord[] tgts = new Coord[1];
        final DijkstraMap dijkstra = state.dijkstra;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                //state.srng.setState((x << 22) | (y << 16) | (x * y));
                //((StatefulRNG) dijkstra.rng).setState(((x << 20) | (y << 14)) ^ (x * y));
                r = state.nearbyMap[x][y];
                tgts[0] = Coord.get(x, y);
                //dijkstra.partialScan(r,9, null);
                state.path.clear();
                dijkstra.findPath(state.path, 9, 9, null, null, r, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += state.path.size();
            }
        }
        return scanned;
    }
    
    @Benchmark
    public long doPathCustomDijkstra(BenchmarkState state)
    {
        Coord r;
        int[] tgts = new int[1];
        long scanned = 0;
        int p;
        CustomDijkstraMap dijkstra = state.customDijkstra;
        final int PATH_LENGTH = state.DIMENSION * state.DIMENSION;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                //((StatefulRNG) dijkstra.rng).setState(((x << 20) | (y << 14)) ^ (x * y));
                r = state.srng.getRandomElement(state.floorArray);
                p = state.adj.composite(r.x, r.y, 0, 0);
                tgts[0] = state.adj.composite(x, y, 0, 0);
                dijkstra.findPath(PATH_LENGTH, null, null, p, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size;
            }
        }
        return scanned;
    }
    @Benchmark
    public long doTinyPathCustomDijkstra(BenchmarkState state)
    {
        Coord r;
        int[] tgts = new int[1];
        long scanned = 0;
        int p;
        CustomDijkstraMap dijkstra = state.customDijkstra;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                //state.srng.setState((x << 22) | (y << 16) | (x * y));
                //((StatefulRNG) dijkstra.rng).setState(((x << 20) | (y << 14)) ^ (x * y));
                r = state.nearbyMap[x][y];
                p = state.adj.composite(r.x, r.y, 0, 0);
                tgts[0] = state.adj.composite(x, y, 0, 0);
                dijkstra.findPath(9,  9,null, null, p, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size;
            }
        }
        return scanned;
    }





    @Benchmark
    public long doPathAStarSearch(BenchmarkState state)
    {
        Coord r;
        Coord tgt;
        long scanned = 0;
        final AStarSearch aStarSearch = state.as;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                //((StatefulRNG) dijkstra.rng).setState(((x << 20) | (y << 14)) ^ (x * y));
                r = state.srng.getRandomElement(state.floorArray);
                tgt = Coord.get(x, y);
                state.path.clear();
                state.path.addAll(aStarSearch.path(r, tgt));
                scanned += state.path.size();
            }
        }
        return scanned;
    }

    @Benchmark
    public long doTinyPathAStarSearch(BenchmarkState state)
    {
        Coord r;
        Coord tgt;
        long scanned = 0;
        final AStarSearch aStarSearch = state.as;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                r = state.nearbyMap[x][y];
                tgt = Coord.get(x, y);
                state.path.clear();
                state.path.addAll(aStarSearch.path(r, tgt));
                scanned += state.path.size();
            }
        }
        return scanned;
    }



//    public long doPathAStar()
//    {
//        AStarSearch astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
//        Coord r;
//        long scanned = 0;
//        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
//        Queue<Coord> latestPath;
//        for (int x = 1; x < DIMENSION - 1; x++) {
//            for (int y = 1; y < DIMENSION - 1; y++) {
//                if (map[x][y] == '#')
//                    continue;
//                // this should ensure no blatant correlation between R and W
//                utility.rng.setState((x << 22) | (y << 16) | (x * y));
//                r = floors.singleRandom(utility.rng);
//                latestPath = astar.path(r, Coord.get(x, y));
//                scanned+= latestPath.size();
//            }
//        }
//        return scanned;
//    }
//
//    //@Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePathAStar() throws InterruptedException {
//        System.out.println(doPathAStar() / floorCount);
//        doPathAStar();
//    }
//
//    public long doPathAStar2()
//    {
//        AStarSearch astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
//        Coord r;
//        long scanned = 0;
//        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
//        Queue<Coord> latestPath;
//        for (int x = 1; x < DIMENSION - 1; x++) {
//            for (int y = 1; y < DIMENSION - 1; y++) {
//                if (map[x][y] == '#')
//                    continue;
//                // this should ensure no blatant correlation between R and W
//                utility.rng.setState((x << 22) | (y << 16) | (x * y));
//                r = floors.singleRandom(utility.rng);
//                latestPath = astar.path(r, Coord.get(x, y));
//                scanned+= latestPath.size();
//            }
//        }
//        return scanned;
//    }
//
//    //@Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePathAStar2() throws InterruptedException {
//        //System.out.println(doPathAStar2());
//        doPathAStar2();
//    }
//
//    public long doTinyPathAStar2()
//    {
//        AStarSearch astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
//        Coord r;
//        long scanned = 0;
//        DungeonUtility utility = new DungeonUtility(new StatefulRNG(0x1337BEEFDEAL));
//        Queue<Coord> latestPath;
//        for (int x = 1; x < DIMENSION - 1; x++) {
//            for (int y = 1; y < DIMENSION - 1; y++) {
//                if (map[x][y] == '#')
//                    continue;
//                // this should ensure no blatant correlation between R and W
//                utility.rng.setState((x << 22) | (y << 16) | (x * y));
//                r = nearbyMap[x][y];
//                latestPath = astar.path(r, Coord.get(x, y));
//                scanned += latestPath.size();
//            }
//        }
//        return scanned;
//    }
//
//    //@Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureTinyPathAStar2() throws InterruptedException {
//        System.out.println(doTinyPathAStar2() / floorCount);
//        doTinyPathAStar2();
//    }

    static class GridGraph implements IndexedGraph<Coord>
    {
        public ObjectIntMap<Coord> points = new ObjectIntMap<>(128 * 128);
        public char[][] map;
//        public Heuristic<Coord> heu = new Heuristic<Coord>() {
//            @Override
//            public float estimate(Coord node, Coord endNode) {
//                return (Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y));
//            }
//        };
        public Heuristic<Coord> heu = new Heuristic<Coord>() {
            @Override
            public float estimate(Coord node, Coord endNode) {
                return Math.max(Math.abs(node.x - endNode.x), Math.abs(node.y - endNode.y));
            }
        };
//        public Heuristic<Coord> heu = new Heuristic<Coord>() {
//            @Override
//            public float estimate(Coord node, Coord endNode) {
//                return (float)node.distance(endNode);
//            }
//        };

        public GridGraph(GreasedRegion floors, char[][] map)
        {
            this.map = map;
            int floorCount = floors.size();
            for (int i = 0; i < floorCount; i++) {
                points.put(floors.nth(i), i);
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
                if (t.isWithin(map.length, map[0].length) && map[t.x][t.y] == '.')
                    conn.add(new DefaultConnection<>(fromNode, t));
            }
            return conn;
        }
    }

    @Benchmark
    public long doPathGDXAStar(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                r = state.srng.getRandomElement(state.floorArray);
                state.dgp.clear();
                if(state.astar.searchNodePath(r, Coord.get(x, y), state.gg.heu, state.dgp))
                    scanned+= state.dgp.getCount();
            }
        }
        return scanned;
    }

    @Benchmark
    public long doTinyPathGDXAStar(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                //state.srng.setState((x << 22) | (y << 16) | (x * y));
                r = state.nearbyMap[x][y];
                state.dgp.clear();
                if(state.astar.searchNodePath(r, Coord.get(x, y), state.gg.heu, state.dgp))
                    scanned += state.dgp.getCount();
            }
        }
        return scanned;
    }

    @Benchmark
    public long doPathIndexedAStar(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                r = state.srng.getRandomElement(state.floorArray);
                state.path.clear();
                if(state.iasSquid.searchNodePath(r, Coord.get(x, y), DefaultGraph.CHEBYSHEV, state.path))
                    scanned += state.path.size();
            }
        }
        return scanned;
    }

    @Benchmark
    public long doTinyPathIndexedAStar(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                //state.srng.setState((x << 22) | (y << 16) | (x * y));
                r = state.nearbyMap[x][y];
                state.path.clear();
                if(state.iasSquid.searchNodePath(r, Coord.get(x, y), DefaultGraph.CHEBYSHEV, state.path))
                    scanned += state.path.size();
            }
        }
        return scanned;
    }

    @Benchmark
    public long doPathSimple(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                r = state.srng.getRandomElement(state.floorArray);
                state.path.clear();
                if(state.simpleGraph.findShortestPath(r, Coord.get(x, y), state.path, state.simpleHeu))
                    scanned += state.path.size();
            }
        }
        return scanned;
    }

    @Benchmark
    public long doTinyPathSimple(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                //state.srng.setState((x << 22) | (y << 16) | (x * y));
                r = state.nearbyMap[x][y];
                state.path.clear();
                if(state.simpleGraph.findShortestPath(r, Coord.get(x, y), state.path, state.simpleHeu))
                    scanned += state.path.size();
            }
        }
        return scanned;
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
     *    $ java -jar target/benchmarks.jar DijkstraBenchmark -wi 3 -i 3 -f 1 -gc true -w 25 -r 25
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

