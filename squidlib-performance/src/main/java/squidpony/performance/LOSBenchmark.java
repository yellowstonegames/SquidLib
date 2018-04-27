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
 * Benchmark results for an older measurement, a 100x100 area mixing
 * wallls and floors where a LOS line is tried from all points to the
 * four corners of the area (40000 fairly-long lines):
 * <pre>
 * Benchmark                      Mode  Cnt     Score    Error  Units
 * LOSBenchmark.measureBresenham  avgt    5  1357.256 ± 39.672  ms/op
 * LOSBenchmark.measureDDA        avgt    5  1040.524 ± 31.607  ms/op
 * LOSBenchmark.measureElias      avgt    5  3082.180 ± 65.809  ms/op
 * LOSBenchmark.measureOrtho      avgt    5  1352.923 ± 16.653  ms/op
 * LOSBenchmark.measureRay        avgt    5  1183.928 ± 35.387  ms/op
 * </pre>
 * <br>
 * With the current measurement, a 30x30 area mixing walls and floors
 * where a LOS line is tried between all points to all points (810000
 * lines that are often very short and never very long):
 * <br>
 * <pre>
 * Benchmark                      Mode  Cnt     Score     Error  Units
 * LOSBenchmark.measureBresenham  avgt    5  1953.945 ± 120.900  ms/op
 * LOSBenchmark.measureDDA        avgt    5  1951.520 ±  97.674  ms/op
 * LOSBenchmark.measureElias      avgt    5  5417.685 ± 251.570  ms/op
 * LOSBenchmark.measureOrtho      avgt    5  2033.008 ± 114.753  ms/op
 * LOSBenchmark.measureRay        avgt    5  1889.998 ±  74.969  ms/op
 * </pre>
 * <br>
 * The benchmarks test different things, but also use different types of map.
 * The first uses TilesetType.DEFAULT_DUNGEON, which causes lines to terminate
 * in a wall frequently, while the second uses TilesetType.SIMPLE_CAVES, which
 * has a more-open layout. Even though the newer benchmark draws 21x the lines,
 * they average at maybe 10 cells long, instead of 50 for the first benchmark.
 * <br>
 * Surprisingly, DDA sometimes speeds up to first place on this sixth-gen i7
 * laptop, but other times is slightly slower than Bresenham. Although, the
 * bottleneck before may have been object creation with the old benchmarking
 * setup. Notably, Elias is now a possibility for a LOS you can actually use,
 * where it really wasn't before. It's sped up by quite a lot; since it tends
 * to produce a fairly high-quality LOS result, the almost-3x slowdown
 * relative to DDA or Bresenham may be worth it. Elias also doesn't use
 * threads any more, so it should be fine on GWT.
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
    public static final int WIDTH = 32, HEIGHT = 32, END_X = WIDTH - 2, END_Y = HEIGHT - 2;
    public static final char[][] map;
    static
    {
        DungeonGenerator dg = new DungeonGenerator(WIDTH, HEIGHT, new StatefulRNG(2252637788195L));
        map = dg.generate(TilesetType.SIMPLE_CAVES);
    }

    @Benchmark
    public void measureBresenham() throws InterruptedException {
        LOS los = new LOS(LOS.BRESENHAM);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                for (int xx = 1; xx <= END_X; xx++) {
                    for (int yy = 1; yy <= END_Y; yy++) {
                        los.isReachable(map, x, y, xx, yy);
                    }
                }
            }
        }
    }

    @Benchmark
    public void measureDDA() throws InterruptedException {
        LOS los = new LOS(LOS.DDA);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                for (int xx = 1; xx <= END_X; xx++) {
                    for (int yy = 1; yy <= END_Y; yy++) {
                        los.isReachable(map, x, y, xx, yy);
                    }
                }
            }
        }
    }

    @Benchmark
    public void measureElias() throws InterruptedException {
        LOS los = new LOS(LOS.ELIAS);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                for (int xx = 1; xx <= END_X; xx++) {
                    for (int yy = 1; yy <= END_Y; yy++) {
                        los.isReachable(map, x, y, xx, yy);
                    }
                }
            }
        }
    }

    @Benchmark
    public void measureRay() throws InterruptedException {
        LOS los = new LOS(LOS.RAY);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                for (int xx = 1; xx <= END_X; xx++) {
                    for (int yy = 1; yy <= END_Y; yy++) {
                        los.isReachable(map, x, y, xx, yy);
                    }
                }
            }
        }
    }

    @Benchmark
    public void measureOrtho() throws InterruptedException {
        LOS los = new LOS(LOS.ORTHO);
        for (int x = 1; x <= END_X; x++) {
            for (int y = 1; y <= END_Y; y++) {
                for (int xx = 1; xx <= END_X; xx++) {
                    for (int yy = 1; yy <= END_Y; yy++) {
                        los.isReachable(map, x, y, xx, yy);
                    }
                }
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
