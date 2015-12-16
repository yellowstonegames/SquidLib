package squidpony.performance;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.StatefulRNG;

import java.util.concurrent.TimeUnit;

/**
 * Created by Tommy Ettinger on 10/14/2015.
 */
public class FOVCacheBenchmark {

    public static final int DIMENSION = 60;
    public static DungeonGenerator dungeonGen =
            new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
    public static final char[][] map = dungeonGen.generate();
    public static final double[][] res = DungeonUtility.generateResistances(map);
    public static FOVCache cache = new FOVCache(map, 16, 50, Radius.SQUARE, 8);
    public static FOV fov = new FOV(FOV.RIPPLE);
    static {
        cache.awaitCache();
    }

    public void doCachedFOV()
    {
        //double total = 0.0;
        //double[][] calculated;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    cache.calculateGradedFOV(res, i, j, 16);

                    /*for (int k = 1; k < DIMENSION - 1; k++) {
                        for (int l = 1; l < DIMENSION - 1; l++) {
                            total += calculated[k][l];
                        }
                    }
                    */
                }
            }
        }
        //System.out.println("FOVCache: " + total);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureCachedFOV() throws InterruptedException {
        doCachedFOV();
    }

    public void doFOV()
    {
        //double total = 0.0;
        //double[][] calculated;
        for (int i = 1; i < DIMENSION - 1; i++) {
            for (int j = 1; j < DIMENSION - 1; j++) {
                if (map[i][j] != '#') {
                    fov.calculateFOV(res, i, j, 16);
                    /*
                    for (int k = 1; k < DIMENSION - 1; k++) {
                        for (int l = 1; l < DIMENSION - 1; l++) {
                            total += calculated[k][l];
                        }
                    }*/
                }
            }
        }
        //System.out.println("FOV     : " + total);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureFOV() throws InterruptedException {
        doFOV();
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
