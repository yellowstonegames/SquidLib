package squidpony.performance;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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
 * Created by Tommy Ettinger on 9/18/2016.
 */
public class LOSBenchmark {
    public static final int WIDTH = 102, HEIGHT = 102, END_X = WIDTH - 2, END_Y = HEIGHT - 2;
    public static final char[][] map;
    static
    {
        DungeonGenerator dg = new DungeonGenerator(WIDTH, HEIGHT, new StatefulRNG(2252637788195L));
        map = dg.generate(TilesetType.OPEN_AREAS);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
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
