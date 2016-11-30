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
import squidpony.performance.alternate.AStarExperiment;
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

public class DijkstraBenchmark {

    public static final int DIMENSION = 40, PATH_LENGTH = (DIMENSION - 2) * (DIMENSION - 2);
    public static DungeonGenerator dungeonGen =
            new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION,
            new StatefulRNG(0x1337BEEFDEAL));
    public static char[][] map;
    public static double[][] astarMap;
    public static GreasedRegion floors;
    public static Coord[][] nearbyMap;
    public static int[] customNearbyMap;
    public static Adjacency adj;
    static {
        serpent.putWalledBoxRoomCarvers(1);
        map = dungeonGen.generate(serpent.generate());
        floors = new GreasedRegion(map, '.');
        System.out.println("Floors: " + floors.size());
        System.out.println("Percentage walkable: " + floors.size() * 100.0 / (DIMENSION * DIMENSION) + "%");
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
                c = tmp.clear().insert(i, j).flood(floors, 8).remove(i, j).singleRandom(srng);
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

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureScanDijkstra() throws InterruptedException {
        doScanDijkstra();
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

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureScanCustomDijkstra() throws InterruptedException {
        doScanCustomDijkstra();
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
        doScanGreased();
    }

    public long doPathDijkstra()
    {
        DijkstraMap dijkstra = new DijkstraMap(
                map, DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(new LightRNG(0x1337BEEF)));
        dijkstra.setBlockingRequirement(0);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = floors.singleRandom(utility.rng);
                dijkstra.findPath(PATH_LENGTH, null, null, r, Coord.get(x, y));
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
    public void measurePathDijkstra() throws InterruptedException {
        System.out.println(doPathDijkstra());
    }

    public long doTinyPathDijkstra()
    {
        DijkstraMap dijkstra = new DijkstraMap(
                map, DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(new LightRNG(0x1337BEEF)));
        dijkstra.setBlockingRequirement(0);
        Coord r;
        long scanned = 0;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = nearbyMap[x][y];
                dijkstra.findPath(1, 9, null, null, r, Coord.get(x, y));
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
        System.out.println(doTinyPathDijkstra());
    }

    public long doPathCustomDijkstra()
    {
        CustomDijkstraMap dijkstra = new CustomDijkstraMap(
                map, adj, new StatefulRNG(new LightRNG(0x1337BEEF)));
        Coord r;
        int p;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = floors.singleRandom(utility.rng);
                p = adj.composite(r.x, r.y, 0, 0);
                dijkstra.findPath(PATH_LENGTH, null, null, p, adj.composite(x, y, 0, 0));
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
        System.out.println(doPathCustomDijkstra());
    }

    public long doTinyPathCustomDijkstra()
    {
        CustomDijkstraMap dijkstra = new CustomDijkstraMap(
                map, adj, new StatefulRNG(new LightRNG(0x1337BEEF)));
        Coord r;
        int p;
        long scanned = 0;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = nearbyMap[x][y];
                p = adj.composite(r.x, r.y, 0, 0);
                dijkstra.findPath(1,  9,null, null, p, adj.composite(x, y, 0, 0));
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
        System.out.println(doTinyPathCustomDijkstra());
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
        doScanBoxedDijkstra();
    }

    public long doPathBoxedDijkstra()
    {
        squidpony.performance.alternate.DijkstraMap dijkstra = new squidpony.performance.alternate.DijkstraMap(
                map, squidpony.performance.alternate.DijkstraMap.Measurement.CHEBYSHEV, new StatefulRNG(new LightRNG(0x1337BEEF)));
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (map[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                ((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
                r = floors.singleRandom(utility.rng);
                dijkstra.findPath(PATH_LENGTH, null, null, r, Coord.get(x, y));
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
        System.out.println(doPathBoxedDijkstra());
    }




    public long doPathAStar()
    {
        AStarSearch astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathAStar() throws InterruptedException {
        System.out.println(doPathAStar());
    }

    public long doPathAStar2()
    {
        AStarExperiment astar = new AStarExperiment(astarMap, AStarExperiment.SearchType.CHEBYSHEV);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
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

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePathAStar2() throws InterruptedException {
        System.out.println(doPathAStar2());
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
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
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
        System.out.println(doPathGDXAStar());
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
        System.out.println(doTinyPathGDXAStar());
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
     *    $ java -jar target/benchmarks.jar DijkstraBenchmark -wi 3 -i 3 -f 1
     *
     *    (we requested 3 warmup/measurement iterations, single fork)
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
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
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

