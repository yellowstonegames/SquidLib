package squidpony.performance;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.StatefulRNG;

import java.util.concurrent.TimeUnit;

/**
 * Created by Tommy Ettinger on 10/14/2015.
 */
public class FOVCacheBenchmark {

    public static final int DIMENSION = 60;

    @State(Scope.Benchmark)
    public static class StateHolder {
        public DungeonGenerator dungeonGen =
                new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
        public char[][] map = dungeonGen.generate();
        public double[][] res = DungeonUtility.generateResistances(map);

        public FOVCache cache;
        public FOV fov = new FOV(FOV.SHADOW);

        @Setup(Level.Trial)
        public void setup() {
            dungeonGen = new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
            map = dungeonGen.generate();
            res = DungeonUtility.generateResistances(map);

            cache = new FOVCache(map, 30, 50, Radius.SQUARE, 8);
            cache.awaitCache();
        }
        @TearDown
        public void tearDown(){
            if(cache != null)
                cache.destroy();
        }

    }

    public void doCachedFOV()
    {
        //double total = 0.0;
        //double[][] calculated;
        //System.out.println("FOVCache: " + total);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public long measureCachedFOV(StateHolder state) throws InterruptedException {
        long count = 0L;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (state.map[i][j] != '#') {
                    count += state.cache.calculateFOV(state.res, i, j, 16).length;
                }
            }
        }
        return count;
    }

    public void doFOV()
    {
        //double total = 0.0;
        //double[][] calculated;
        //System.out.println("FOV     : " + total);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public long measureFOV(StateHolder state) throws InterruptedException {
        long count = 0L;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (state.map[i][j] != '#') {
                    count += state.fov.calculateFOV(state.res, i, j, 16).length;
                }
            }
        }
        return count;
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FOVCacheBenchmark.class.getSimpleName())
                .warmupIterations(10)
                .measurementIterations(10)
                .build();

        new Runner(opt).run();
    }
}
