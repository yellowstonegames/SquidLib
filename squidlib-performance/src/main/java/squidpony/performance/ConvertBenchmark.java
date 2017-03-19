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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.PintRNG;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ConvertBenchmark {

    private static long seed = 9000;
    private static int iseed = 9000;
    private static float fseed = 9000f;

    public float doControl()
    {
        final PintRNG rng = new PintRNG(NumberTools.floatToIntBits(fseed));

        for (int i = 0; i < 1000000; i++) {
            fseed += 0x20 | rng.next(3);
        }
        return fseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureControl() throws InterruptedException {
        fseed = 9000f;
        doControl();
    }

    public float doJDK()
    {
        final PintRNG rng = new PintRNG(NumberTools.floatToIntBits(fseed));

        for (int i = 0; i < 1000000; i++) {
            fseed += Float.intBitsToFloat(0x02000000 | rng.next(24));
        }
        return fseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureJDK() throws InterruptedException {
        fseed = 9000f;
        doJDK();
    }

    public float doCustom()
    {
        final PintRNG rng = new PintRNG(NumberTools.floatToIntBits(fseed));

        for (int i = 0; i < 1000000; i++) {
            fseed += SColor.intToFloatColor(0x02000000 | rng.next(24));
        }
        return fseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureCustom() throws InterruptedException {
        fseed = 9000f;
        doCustom();
    }

    public float doCustomAlt()
    {
        final PintRNG rng = new PintRNG(NumberTools.floatToIntBits(fseed));

        for (int i = 0; i < 1000000; i++) {
            fseed += SColor.intToFloatColorAlt(0x02000000 | rng.next(24));
        }
        return fseed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureCustomAlt() throws InterruptedException {
        fseed = 9000f;
        doCustomAlt();
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
     *    $ java -jar target/benchmarks.jar ConvertBenchmark -wi 8 -i 8 -f 1 -gc true
     *
     *    (we requested 8 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ConvertBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(60))
                .warmupIterations(8)
                .measurementIterations(8)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
