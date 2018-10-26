package squidpony.performance;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class FOVCacheBenchmark {

    public static final int DIMENSION = 100;
    public static DungeonGenerator dungeonGen = new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static char[][] map = dungeonGen.generate();
    public static double[][] res = DungeonUtility.generateResistances(map);
    
    @State(Scope.Benchmark)
    public static class CachedState {
        public FOVCache cache;
        @Setup(Level.Trial)
        public void setup() {
            cache = new FOVCache(map, 20, 40, Radius.CIRCLE, 4);
            cache.awaitCache();
        }
        @TearDown(Level.Trial)
        public void tearDown(){
            if(cache != null)
                cache.destroy();
            cache = null;
        }

    }
    @State(Scope.Benchmark)
    public static class FreshState {
        public FOV fov;
        public double[][] lighting = new double[DIMENSION][DIMENSION];

        @Setup(Level.Trial)
        public void setup() {
            fov = new FOV(FOV.SHADOW);
        }

    }

    @Benchmark
    public void measureCachedFOV(CachedState state, Blackhole hole) {
        double[][] result;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    result = state.cache.calculateFOV(res, i, j, 3);
                    hole.consume(result);
                }
            }
        }
    }

    @Benchmark
    public void measureFOV(FreshState state, Blackhole hole) {
        double[][] result;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    result = state.fov.calculateFOV(res, i, j, 3, Radius.CIRCLE);
                    hole.consume(result);
                }
            }
        }
    }

    @Benchmark
    public void measureReuseFOV(FreshState state, Blackhole hole) {
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    FOV.reuseFOV(res, state.lighting, i, j, 3, Radius.CIRCLE);
                    hole.consume(state.lighting);
                }
            }
        }
    }

//    @Benchmark
//    public void measureReuseFOVSquare(FreshState state, Blackhole hole) {
//        for (int i = 1; i < DIMENSION - 1; i++) {
//            for (int j = 1; j < DIMENSION - 1; j++) {
//                if (map[i][j] != '#') {
//                    FOV.reuseFOV(res, state.lighting, i, j, 3, Radius.SQUARE);
//                    hole.consume(state.lighting);
//                }
//            }
//        }
//    }
//
//    @Benchmark
//    public void measureReuseFOVRough(FreshState state, Blackhole hole) {
//        for (int i = 1; i < DIMENSION - 1; i++) {
//            for (int j = 1; j < DIMENSION - 1; j++) {
//                if (map[i][j] != '#') {
//                    FOV.reuseFOV(res, state.lighting, i, j, 3, Radius.ROUGH_CIRCLE);
//                    hole.consume(state.lighting);
//                }
//            }
//        }
//    }


    @Benchmark
    public void measureCachedFOV16(CachedState state, Blackhole hole) {
        double[][] result;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    result = state.cache.calculateFOV(res, i, j, 16);
                    hole.consume(result);
                }
            }
        }
    }

    @Benchmark
    public void measureFOV16(FreshState state, Blackhole hole) {
        double[][] result;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    result = state.fov.calculateFOV(res, i, j, 16, Radius.CIRCLE);
                    hole.consume(result);
                }
            }
        }
    }

    @Benchmark
    public void measureReuseFOV16(FreshState state, Blackhole hole) {
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    FOV.reuseFOV(res, state.lighting, i, j, 16, Radius.CIRCLE);
                    hole.consume(state.lighting);
                }
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FOVCacheBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .build();

        new Runner(opt).run();
    }
}
