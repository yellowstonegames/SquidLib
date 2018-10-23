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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.ClassicNoise;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.SeededNoise;
import squidpony.squidmath.WhirlingNoise;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for WorldMapGenerator.
 * <br>
 * HyperellipticalMap (200x100) using ClassicNoise and using FastNoise, run with Java 11, on October 21, 2018:
 * <pre>
 * Benchmark                                Mode  Cnt   Score   Error  Units
 * WorldBenchmark.measureHyperelliptical    avgt    5  43.047 ± 0.789  ms/op
 * WorldBenchmark.measureHyperellipticalFN  avgt    5  35.127 ± 0.314  ms/op
 * </pre>
 * <br>
 * HyperellipticalMap (1000x500), all using FastNoise, FN using the fractal features of FastNoise, Main using the
 * current WorldMapGenerator in squidlib-util, run with Java 11, on October 22, 2018:
 * <pre>
 * Benchmark                                  Mode  Cnt     Score    Error  Units
 * WorldBenchmark.measureHyperelliptical      avgt    5  1355.337 ± 12.837  ms/op
 * WorldBenchmark.measureHyperellipticalFN    avgt    5  1151.881 ± 41.068  ms/op
 * WorldBenchmark.measureMainHyperelliptical  avgt    5  1304.524 ± 25.856  ms/op // doesn't generate rivers at all
 * </pre>
 * <br>
 * RotatingSpaceMap (1000x500) with different noise implementations, all using the current WorldMapGenerator in
 * squidlib-util, run with Java 11, on October 22, 2018:
 * <pre>
 * Benchmark                       Mode  Cnt     Score    Error  Units
 * WorldBenchmark.measureClassic   avgt    5  3637.492 ± 17.837  ms/op
 * WorldBenchmark.measureFast      avgt    5  3079.364 ± 13.935  ms/op
 * WorldBenchmark.measureSeeded    avgt    5  3411.478 ± 65.173  ms/op
 * WorldBenchmark.measureWhirling  avgt    5  3758.161 ± 50.559  ms/op
 * </pre>
 * <br>
 * HyperellipticalMap (300x150) with different noise implementations, all using the current WorldMapGenerator in
 * squidlib-util, run with Java 11, on October 22, 2018:
 * <pre>
 * Benchmark                       Mode  Cnt    Score   Error  Units
 * WorldBenchmark.measureClassic   avgt    5  103.107 ± 1.034  ms/op
 * WorldBenchmark.measureFast      avgt    5   92.987 ± 0.836  ms/op
 * WorldBenchmark.measureSeeded    avgt    5   93.227 ± 0.369  ms/op
 * WorldBenchmark.measureWhirling  avgt    5  113.174 ± 1.501  ms/op
 * </pre>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 5)
public class WorldBenchmark {
    private WorldMapGenerator whi = new WorldMapGenerator.HyperellipticalMap(0x1337L, 300, 150, WhirlingNoise.instance, 1.1);
    private WorldMapGenerator cla = new WorldMapGenerator.HyperellipticalMap(0x1337L, 300, 150, ClassicNoise.instance, 1.1);
    private WorldMapGenerator fas = new WorldMapGenerator.HyperellipticalMap(0x1337L, 300, 150, FastNoise.instance, 1.1);
    private WorldMapGenerator see = new WorldMapGenerator.HyperellipticalMap(0x1337L, 300, 150, SeededNoise.instance, 1.1);

    @Benchmark
    public void measureWhirling(Blackhole blackhole)
    {
        whi.generate();
        blackhole.consume(whi);
    }

    @Benchmark
    public void measureClassic(Blackhole blackhole)
    {
        cla.generate();
        blackhole.consume(cla);
    }
    @Benchmark
    public void measureFast(Blackhole blackhole)
    {
        fas.generate();
        blackhole.consume(fas);
    }

    @Benchmark
    public void measureSeeded(Blackhole blackhole)
    {
        see.generate();
        blackhole.consume(see);
    }

//    private WMG.HyperellipticalMap hem = new WMG.HyperellipticalMap(0x1337L, 1000, 500, FastNoise.instance, 1.5, 0.125, 2.5);
//    private WMG.HyperellipticalFNMap fnm = new WMG.HyperellipticalFNMap(0x1337L, 1000, 500, 1.5, 0.125, 2.5);
//    private WorldMapGenerator.HyperellipticalMap mai = new WorldMapGenerator.HyperellipticalMap(0x1337L, 1000, 500, FastNoise.instance, 1.5, 0.125, 2.5);
//
//    @Benchmark
//    public void measureMainHyperelliptical(Blackhole blackhole)
//    {
//        mai.generate();
//        blackhole.consume(mai);
//    }
//    @Benchmark
//    public void measureHyperelliptical(Blackhole blackhole)
//    {
//        hem.generate();
//        blackhole.consume(hem);
//    }
//    
//    @Benchmark
//    public void measureHyperellipticalFN(Blackhole blackhole)
//    {
//        fnm.generate();
//        blackhole.consume(fnm);
//    }

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
     *    $ java -jar target/benchmarks.jar NoiseBenchmark -wi 4 -i 4 -f 1
     *
     *    (we requested 5 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(WorldBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
