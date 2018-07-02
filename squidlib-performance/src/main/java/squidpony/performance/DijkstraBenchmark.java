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
import squidpony.squidai.CustomDijkstraMap;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.StatefulRNG;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static squidpony.squidai.DijkstraMap.Measurement.CHEBYSHEV;

/**
 * Times:
 * These benchmark results are split up based on the size of the map, which has major implications for performance.
 * We compare DijkstraMap from SquidLib, CustomDijkstraMap from SquidLib, and IndexedAStarPathFinder from gdx-ai.
 * IndexedAStarPathFinder is a generally excellent performer on many maps, but it's a mixed bag on features. While it
 * allows setting a cost to traverse between cells, it can't reuse an already-scanned map like DijkstraMap can.
 * DijkstraMap technically doesn't use Dijkstra's Pathfinding Algorithm, and is much closer to breadth-first search; it
 * produces what roguelike developers call a Dijkstra map because it is so similar to what Dijkstra's algorithm uses.
 * The map produced by scanning with DijkstraMap is a 2D double array of distances; this can be reused and multiple
 * goals can simultaneously be sought to find the closest. CustomDijkstraMap allows specifying adjacency in unusual ways
 * (like considering facing direction when calculating cost). It is otherwise much like DijkstraMap.
 * <br>
 * For performance on the smaller 64x64 map size, the gdx-ai pathfinding can't be touched. It is between 4 and 5 times
 * faster than DijkstraMap on very short paths (under 9 cells), and about 1.5x faster on longer ones (a random point
 * in the 64x64 dungeon map to another random point). As map size increases, gdx-ai loses its lead; on 128x128 maps it
 * is the slowest option for long paths but is still very much the fastest for short paths (percentage-wise, its lead
 * has improved on short paths despite worsening significantly on long ones). On those 128x128 maps, DijkstraMap is the
 * fastest for long paths. DijkstraMap's margin of improvement on long paths continues on 192x192 maps, though gdx-ai
 * is still #1 on short paths. A key point to note is that at 192x192, gdx-ai takes well over a millisecond per long
 * path, while DijkstraMap is under 0.8 ms; pathfinding too many long paths with any algorithm may be difficult on this
 * large of a map, but DijkstraMap can at least scan once and cheaply calculate a path more than once (where required)
 * with the already-scanned map.
 * <br>
 * (These use JMH's recommendations for benchmarking; older benchmark results, which weren't set up correctly, are
 * available in the Git history of this file. The older benchmarks included repeated initialization and GC of
 * DijkstraMap, CustomDijkstraMap, or other similar objects in the time per benchmark.)
 *
 *                       This is the relevant measurement,
 *                       time in ms to find all paths -> +-------+
 * Map size: 64x64, 1364 paths                           |       |
 * Benchmark                                   Mode  Cnt    Score    Error  Units
 * DijkstraBenchmark.doPathCustomDijkstra      avgt    5  220.851 ± 93.593  ms/op
 * DijkstraBenchmark.doPathDijkstra            avgt    5  153.784 ±  5.654  ms/op
 * DijkstraBenchmark.doPathGDXAStar            avgt    5  107.962 ±  2.949  ms/op
 * DijkstraBenchmark.doScanCustomDijkstra      avgt    5  344.942 ± 22.039  ms/op
 * DijkstraBenchmark.doScanDijkstra            avgt    5  244.537 ± 15.884  ms/op
 * DijkstraBenchmark.doTinyPathCustomDijkstra  avgt    5   16.763 ±  0.254  ms/op
 * DijkstraBenchmark.doTinyPathDijkstra        avgt    5    9.922 ±  0.444  ms/op
 * DijkstraBenchmark.doTinyPathGDXAStar        avgt    5    2.115 ±  0.155  ms/op
 *
 * Map size: 128x128, 4677 paths
 * Benchmark                                   Mode  Cnt     Score      Error  Units
 * DijkstraBenchmark.doPathCustomDijkstra      avgt    5  2106.727 ±   11.773  ms/op
 * DijkstraBenchmark.doPathDijkstra            avgt    5  1713.969 ±   54.823  ms/op
 * DijkstraBenchmark.doPathGDXAStar            avgt    5  2275.877 ±   37.086  ms/op
 * DijkstraBenchmark.doScanCustomDijkstra      avgt    5  4159.586 ± 2221.693  ms/op
 * DijkstraBenchmark.doScanDijkstra            avgt    5  2882.672 ±   25.996  ms/op
 * DijkstraBenchmark.doTinyPathCustomDijkstra  avgt    5   210.557 ±    8.608  ms/op
 * DijkstraBenchmark.doTinyPathDijkstra        avgt    5    80.280 ±    0.264  ms/op
 * DijkstraBenchmark.doTinyPathGDXAStar        avgt    5     7.478 ±    0.168  ms/op
 *
 * Map size: 192x192, 9908 paths
 * Benchmark                                   Mode  Cnt      Score     Error  Units
 * DijkstraBenchmark.doPathCustomDijkstra      avgt    5   8930.620 ±  50.764  ms/op
 * DijkstraBenchmark.doPathDijkstra            avgt    5   7852.002 ±  44.351  ms/op
 * DijkstraBenchmark.doPathGDXAStar            avgt    5  11673.292 ± 181.477  ms/op
 * DijkstraBenchmark.doScanCustomDijkstra      avgt    5  16930.492 ±  65.738  ms/op
 * DijkstraBenchmark.doScanDijkstra            avgt    5  12387.999 ± 172.563  ms/op
 * DijkstraBenchmark.doTinyPathCustomDijkstra  avgt    5   1001.202 ±  20.125  ms/op
 * DijkstraBenchmark.doTinyPathDijkstra        avgt    5    379.848 ±   2.214  ms/op
 * DijkstraBenchmark.doTinyPathGDXAStar        avgt    5     18.619 ±   2.698  ms/op
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class DijkstraBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        public int DIMENSION = 128;
        public DungeonGenerator dungeonGen = new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
        //public SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
        public char[][] map;
        public double[][] astarMap;
        public GreasedRegion floors;
        public int floorCount;
        public Coord[][] nearbyMap;
        public int[] customNearbyMap;
        public Adjacency adj;
        public DijkstraMap dijkstra;
        public CustomDijkstraMap customDijkstra;
        public StatefulRNG srng;
        public GridGraph gg;
        public IndexedAStarPathFinder<Coord> astar;
        public GraphPath<Coord> dgp;
        @Setup(Level.Trial)
        public void setup() {
            Coord.expandPoolTo(DIMENSION, DIMENSION);
            //serpent.putWalledBoxRoomCarvers(1);
            //map = dungeonGen.generate(serpent.generate());
            map = dungeonGen.generate();
            floors = new GreasedRegion(map, '.');
            floorCount = floors.size();
            System.out.println("Floors: " + floorCount);
            System.out.println("Percentage walkable: " + floorCount * 100.0 / (DIMENSION * DIMENSION) + "%");
            astarMap = DungeonUtility.generateAStarCostMap(map, Collections.<Character, Double>emptyMap(), 1);
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
            dijkstra = new DijkstraMap(
                    map, CHEBYSHEV, new StatefulRNG(0x1337BEEF));
            dijkstra.setBlockingRequirement(0);
            customDijkstra = new CustomDijkstraMap(
                    map, adj, new StatefulRNG(0x1337BEEF));
            gg = new GridGraph(floors, map);
            astar = new IndexedAStarPathFinder<>(gg);
            dgp = new DefaultGraphPath<>(128 * 128);
        }

    }

    @Benchmark
    public long doScanDijkstra(BenchmarkState state)
    {
        long scanned = 0;
        DijkstraMap dijkstra = state.dijkstra;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
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
        Coord[] tgts = new Coord[1];
        long scanned = 0;
        DijkstraMap dijkstra = state.dijkstra;
        final int PATH_LENGTH = state.DIMENSION * state.DIMENSION;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                //((StatefulRNG) dijkstra.rng).setState(((x << 20) | (y << 14)) ^ (x * y));
                r = state.floors.singleRandom(state.srng);
                tgts[0] = Coord.get(x, y);
                dijkstra.findPath(PATH_LENGTH, null, null, r, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size();
            }
        }
        return scanned;
    }

    @Benchmark
    public long doTinyPathDijkstra(BenchmarkState state)
    {
        Coord r;
        long scanned = 0;
        Coord[] tgts = new Coord[1];
        DijkstraMap dijkstra = state.dijkstra;
        for (int x = 1; x < state.DIMENSION - 1; x++) {
            for (int y = 1; y < state.DIMENSION - 1; y++) {
                if (state.map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                //((StatefulRNG) dijkstra.rng).setState(((x << 20) | (y << 14)) ^ (x * y));
                r = state.nearbyMap[x][y];
                tgts[0] = Coord.get(x, y);
                //dijkstra.partialScan(r,9, null);
                dijkstra.findPath(9, 9, null, null, r, tgts);
                dijkstra.clearGoals();
                dijkstra.resetMap();
                scanned += dijkstra.path.size();
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
                r = state.floors.singleRandom(state.srng);
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
                state.srng.setState((x << 22) | (y << 16) | (x * y));
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

//    public long doPathAStar()
//    {
//        squidpony.performance.alternate.AStarSearch astar = new squidpony.performance.alternate.AStarSearch(astarMap, squidpony.performance.alternate.AStarSearch.SearchType.CHEBYSHEV);
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
        public Heuristic<Coord> heu = new Heuristic<Coord>() {
            @Override
            public float estimate(Coord node, Coord endNode) {
                return Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y);
            }
        };

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
                r = state.floors.singleRandom(state.srng);
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
                state.srng.setState((x << 22) | (y << 16) | (x * y));
                r = state.nearbyMap[x][y];
                state.dgp.clear();
                if(state.astar.searchNodePath(r, Coord.get(x, y), state.gg.heu, state.dgp))
                    scanned += state.dgp.getCount();
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

