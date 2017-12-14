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

    public static final int DIMENSION = 180;
    public static DungeonGenerator dungeonGen = new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static char[][] map = dungeonGen.generate();
    public static double[][] res = DungeonUtility.generateResistances(map);

    @State(Scope.Benchmark)
    public static class CachedState {
        public FOVCache cache;
        @Setup(Level.Trial)
        public void setup() {
            cache = new FOVCache(map, 30, 50, Radius.SQUARE, 8);
            cache.awaitCache();
        }
        @TearDown(Level.Trial)
        public void tearDown(){
            if(cache != null)
                cache.destroy();
        }

    }
    @State(Scope.Benchmark)
    public static class FreshState {
        public FOV fov;

        @Setup(Level.Trial)
        public void setup() {
            fov = new FOV(FOV.SHADOW);
        }

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public double measureCachedFOV(CachedState state) throws InterruptedException {
        double count = 0L;
        double[][] result;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    result = state.cache.calculateFOV(res, i, j, 16);
                    for (int k = 1; k < DIMENSION - 1; k++) {
                        for (int l = 1; l < DIMENSION - 1; l++) {
                            count += result[k][l];
                        }
                    }
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
    public double measureFOV(FreshState state) throws InterruptedException {
        double count = 0L;
        double[][] result;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    result = state.fov.calculateFOV(res, i, j, 16);
                    for (int k = 1; k < DIMENSION - 1; k++) {
                        for (int l = 1; l < DIMENSION - 1; l++) {
                            count += result[k][l];
                        }
                    }
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
