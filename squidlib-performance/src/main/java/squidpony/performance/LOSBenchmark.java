package squidpony.performance;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.StatefulRNG;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * Benchmark                      Mode  Cnt     Score    Error  Units
 * LOSBenchmark.measureBresenham  avgt    5  1357.256 ± 39.672  ms/op
 * LOSBenchmark.measureDDA        avgt    5  1040.524 ± 31.607  ms/op
 * LOSBenchmark.measureElias      avgt    5  3082.180 ± 65.809  ms/op
 * LOSBenchmark.measureOrtho      avgt    5  1352.923 ± 16.653  ms/op
 * LOSBenchmark.measureRay        avgt    5  1183.928 ± 35.387  ms/op
 * </pre>
 * <br>
 * Surprisingly, DDA has sped up to first place on this sixth-gen i7 laptop,
 * though the bottleneck before may have been object creation with the old
 * benchmarking setup. Notably, Elias is now a possibility for a LOS you can
 * actually use, where it really wasn't before. It's sped up by quite a lot;
 * since it tends to produce a fairly high-quality LOS result, the maybe-3x
 * slowdown relative to DDA or Bresenham may be worth it. Elias also doesn't
 * use threads any more, so it should be fine on GWT.
 * <br>
 * Created by Tommy Ettinger on 9/18/2016.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class LOSBenchmark {
    public static final int WIDTH = 102, HEIGHT = 102, END_X = WIDTH - 2, END_Y = HEIGHT - 2;
    public static final char[][] map;
    static
    {
        DungeonGenerator dg = new DungeonGenerator(WIDTH, HEIGHT, new StatefulRNG(2252637788195L));
        map = dg.generate(TilesetType.DEFAULT_DUNGEON);
    }

    @Benchmark
    public void measureBresenham() throws InterruptedException {
        LOS los = new LOS(LOS.BRESENHAM);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                los.isReachable(map, x, y, 1, 1);
                los.isReachable(map, x, y, 1, END_Y);
                los.isReachable(map, x, y, END_X, 1);
                los.isReachable(map, x, y, END_X, END_Y);
            }
        }
    }

    @Benchmark
    public void measureDDA() throws InterruptedException {
        LOS los = new LOS(LOS.DDA);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                los.isReachable(map, x, y, 1, 1);
                los.isReachable(map, x, y, 1, END_Y);
                los.isReachable(map, x, y, END_X, 1);
                los.isReachable(map, x, y, END_X, END_Y);
            }
        }
    }

    @Benchmark
    public void measureElias() throws InterruptedException {
        LOS los = new LOS(LOS.ELIAS);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                los.isReachable(map, x, y, 1, 1);
                los.isReachable(map, x, y, 1, END_Y);
                los.isReachable(map, x, y, END_X, 1);
                los.isReachable(map, x, y, END_X, END_Y);
            }
        }
    }

    @Benchmark
    public void measureRay() throws InterruptedException {
        LOS los = new LOS(LOS.RAY);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                los.isReachable(map, x, y, 1, 1);
                los.isReachable(map, x, y, 1, END_Y);
                los.isReachable(map, x, y, END_X, 1);
                los.isReachable(map, x, y, END_X, END_Y);
            }
        }
    }

    @Benchmark
    public void measureOrtho() throws InterruptedException {
        LOS los = new LOS(LOS.ORTHO);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                los.isReachable(map, x, y, 1, 1);
                los.isReachable(map, x, y, 1, END_Y);
                los.isReachable(map, x, y, END_X, 1);
                los.isReachable(map, x, y, END_X, END_Y);
            }
        }
    }
    public static void main(String[] args) throws RunnerException{

        Options opt = new OptionsBuilder()
                .include(LOSBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(120))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();

    }
}
