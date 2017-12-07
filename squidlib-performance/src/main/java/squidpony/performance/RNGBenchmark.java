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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidmath.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                         Mode  Cnt            Score            Error  Units
 * RNGBenchmark.measureLight         avgt    3   1275059037.000 ±   61737532.875  ns/op
 * RNGBenchmark.measureLightInt      avgt    3   1278703443.000 ±   66201423.790  ns/op
 * RNGBenchmark.measureLightIntR     avgt    3   1427303028.000 ±  200958011.322  ns/op
 * RNGBenchmark.measureLightR        avgt    3   1269081959.667 ±   86190018.925  ns/op
 * RNGBenchmark.measureMT            avgt    3  43085766002.333 ± 2268888793.171  ns/op
 * RNGBenchmark.measureMTInt         avgt    3  22167143778.000 ±  828756142.658  ns/op
 * RNGBenchmark.measureMTIntR        avgt    3  22132403458.000 ±  383655518.387  ns/op
 * RNGBenchmark.measureMTR           avgt    3  43006069307.000 ± 2473850311.634  ns/op
 * RNGBenchmark.measurePermuted      avgt    3   1637032592.333 ±   59199840.006  ns/op
 * RNGBenchmark.measurePermutedInt   avgt    3   1734496732.000 ±   93718940.208  ns/op
 * RNGBenchmark.measurePermutedIntR  avgt    3   1737075300.667 ±  241897619.330  ns/op
 * RNGBenchmark.measurePermutedR     avgt    3   1668389798.667 ±  378429094.045  ns/op
 * RNGBenchmark.measureRandom        avgt    3  22703702167.000 ±  392502237.818  ns/op
 * RNGBenchmark.measureRandomInt     avgt    3  12593739050.667 ±  197683615.906  ns/op
 * RNGBenchmark.measureXor           avgt    3   1384086605.000 ±  174305317.575  ns/op
 * RNGBenchmark.measureXorInt        avgt    3   1276688870.167 ±  133364204.061  ns/op
 * RNGBenchmark.measureXorIntR       avgt    3   1214642941.833 ±   51259344.714  ns/op
 * RNGBenchmark.measureXorR          avgt    3   1346017624.333 ±  151221919.876  ns/op
 *
 * RNGBenchmark.measureLight                 avgt    3   1271.746 ±  155.345  ms/op
 * RNGBenchmark.measureLightInt              avgt    3   1271.499 ±   64.098  ms/op
 * RNGBenchmark.measureLightIntR             avgt    3   1426.202 ±  132.951  ms/op
 * RNGBenchmark.measureLightR                avgt    3   1271.037 ±   69.143  ms/op
 * RNGBenchmark.measureMT                    avgt    3  42625.239 ±  969.951  ms/op
 * RNGBenchmark.measureMTInt                 avgt    3  22143.479 ± 2771.177  ms/op
 * RNGBenchmark.measureMTIntR                avgt    3  22322.939 ± 1853.463  ms/op
 * RNGBenchmark.measureMTR                   avgt    3  43003.067 ± 8246.183  ms/op
 * RNGBenchmark.measurePermuted              avgt    3   1650.486 ±   24.306  ms/op
 * RNGBenchmark.measurePermutedInt           avgt    3   1746.945 ±  102.116  ms/op
 * RNGBenchmark.measurePermutedIntR          avgt    3   1746.471 ±  133.611  ms/op
 * RNGBenchmark.measurePermutedR             avgt    3   1662.623 ±  154.331  ms/op
 * RNGBenchmark.measureRandom                avgt    3  22601.739 ±  277.755  ms/op
 * RNGBenchmark.measureRandomInt             avgt    3  12685.072 ±   75.535  ms/op
 * RNGBenchmark.measureXor                   avgt    3   1382.533 ±   50.650  ms/op
 * RNGBenchmark.measureXorInt                avgt    3   1288.620 ±   74.813  ms/op
 * RNGBenchmark.measureXorIntR               avgt    3   1229.695 ±   85.585  ms/op
 * RNGBenchmark.measureXorR                  avgt    3   1358.552 ±   78.095  ms/op
 * RNGBenchmark.measureThreadLocalRandom     avgt    3   1518.164 ±   63.002  ms/op
 * RNGBenchmark.measureThreadLocalRandomInt  avgt    3   1387.081 ±   26.544  ms/op
 *
 * Benchmark                                 Mode  Cnt       Score      Error  Units
 * RNGBenchmark.measureChaosR                avgt    3    1991.710 ±   25.528  ms/op
 * RNGBenchmark.measureChaosRInt             avgt    3    2013.044 ±   45.370  ms/op
 * RNGBenchmark.measureLight                 avgt    3    1270.195 ±   25.362  ms/op
 * RNGBenchmark.measureLightInt              avgt    3    1268.299 ±   51.405  ms/op
 * RNGBenchmark.measureLightIntR             avgt    3    1430.807 ±   33.260  ms/op
 * RNGBenchmark.measureLightR                avgt    3    1275.100 ±  108.047  ms/op
 * RNGBenchmark.measurePermuted              avgt    3    1646.291 ±   15.124  ms/op
 * RNGBenchmark.measurePermutedInt           avgt    3    1747.967 ±   75.774  ms/op
 * RNGBenchmark.measurePermutedIntR          avgt    3    1749.495 ±   61.203  ms/op
 * RNGBenchmark.measurePermutedR             avgt    3    1662.216 ±   30.412  ms/op
 * RNGBenchmark.measureSecureRandom          avgt    3  162726.751 ± 8173.061  ms/op
 * RNGBenchmark.measureSecureRandomInt       avgt    3   81390.982 ±  471.706  ms/op
 * RNGBenchmark.measureThreadLocalRandom     avgt    3    1463.199 ±  164.716  ms/op
 * RNGBenchmark.measureThreadLocalRandomInt  avgt    3    1395.997 ±  186.706  ms/op
 * RNGBenchmark.measureXor                   avgt    3    1389.147 ±  128.362  ms/op
 * RNGBenchmark.measureXorInt                avgt    3    1286.873 ±  152.577  ms/op
 * RNGBenchmark.measureXorIntR               avgt    3    1228.443 ±  280.454  ms/op
 * RNGBenchmark.measureXorR                  avgt    3    1355.535 ±   74.150  ms/op
 *
 * Benchmark                         Mode  Cnt     Score     Error  Units
 * RNGBenchmark.measureLight         avgt    3  1251.677 ± 138.161  ms/op
 * RNGBenchmark.measureLightInt      avgt    3  1245.465 ±  30.920  ms/op
 * RNGBenchmark.measureLightIntR     avgt    3  1405.867 ±  71.977  ms/op
 * RNGBenchmark.measureLightR        avgt    3  1249.536 ±  17.589  ms/op
 * RNGBenchmark.measurePermuted      avgt    3  1618.965 ± 191.034  ms/op
 * RNGBenchmark.measurePermutedInt   avgt    3  1719.651 ±  81.618  ms/op
 * RNGBenchmark.measurePermutedIntR  avgt    3  1724.723 ± 361.353  ms/op
 * RNGBenchmark.measurePermutedR     avgt    3  1631.643 ± 224.490  ms/op
 * RNGBenchmark.measureXoRo          avgt    3  1215.819 ± 123.754  ms/op
 * RNGBenchmark.measureXoRoInt       avgt    3  1377.244 ±  82.096  ms/op
 * RNGBenchmark.measureXoRoIntR      avgt    3  1446.973 ±  61.399  ms/op
 * RNGBenchmark.measureXoRoR         avgt    3  1274.790 ± 114.618  ms/op
 * RNGBenchmark.measureXor           avgt    3  1362.249 ±  24.538  ms/op
 * RNGBenchmark.measureXorInt        avgt    3  1263.790 ±  41.724  ms/op
 * RNGBenchmark.measureXorIntR       avgt    3  1210.991 ± 105.103  ms/op
 * RNGBenchmark.measureXorR          avgt    3  1331.630 ±  77.693  ms/op
 *
 * Benchmark                               Mode  Cnt     Score     Error  Units
 * RNGBenchmark.measureLight               avgt    3  1269.143 ±  85.918  ms/op
 * RNGBenchmark.measureLightBetweenHastyR  avgt    3  1617.676 ± 316.244  ms/op
 * RNGBenchmark.measureLightBetweenR       avgt    3  2920.878 ± 169.583  ms/op
 * RNGBenchmark.measureLightInt            avgt    3  1267.969 ±  47.884  ms/op
 * RNGBenchmark.measureLightIntR           avgt    3  1425.842 ±  89.710  ms/op
 * RNGBenchmark.measureLightR              avgt    3  1270.877 ±  62.054  ms/op
 * RNGBenchmark.measurePermuted            avgt    3  1647.609 ±  22.511  ms/op
 * RNGBenchmark.measurePermutedInt         avgt    3  1749.033 ± 147.920  ms/op
 * RNGBenchmark.measurePermutedIntR        avgt    3  1744.506 ±  77.704  ms/op
 * RNGBenchmark.measurePermutedR           avgt    3  1679.043 ± 733.835  ms/op
 * RNGBenchmark.measureXoRo                avgt    3  1234.455 ± 112.165  ms/op
 * RNGBenchmark.measureXoRoInt             avgt    3  1400.915 ±  12.242  ms/op
 * RNGBenchmark.measureXoRoIntR            avgt    3  1471.615 ±  12.909  ms/op
 * RNGBenchmark.measureXoRoR               avgt    3  1298.212 ±  13.077  ms/op
 * RNGBenchmark.measureXor                 avgt    3  1392.523 ±  74.491  ms/op
 * RNGBenchmark.measureXorInt              avgt    3  1286.622 ±  17.861  ms/op
 * RNGBenchmark.measureXorIntR             avgt    3  1229.620 ±  50.388  ms/op
 * RNGBenchmark.measureXorR                avgt    3  1356.388 ±  61.536  ms/op
 *
 * Benchmark                               Mode  Cnt     Score     Error  Units
 * RNGBenchmark.measureGDX                 avgt    3  1387.068 ±  39.887  ms/op
 * RNGBenchmark.measureGDXInt              avgt    3  1340.047 ±  12.284  ms/op
 * RNGBenchmark.measureIsaac               avgt    3  5895.743 ± 673.415  ms/op
 * RNGBenchmark.measureIsaacInt            avgt    3  5910.345 ± 372.230  ms/op
 * RNGBenchmark.measureIsaacR              avgt    3  6063.574 ± 276.814  ms/op
 * RNGBenchmark.measureLight               avgt    3  1265.901 ± 145.178  ms/op
 * RNGBenchmark.measureLightBetweenHastyR  avgt    3  1605.859 ±  36.246  ms/op
 * RNGBenchmark.measureLightBetweenR       avgt    3  2986.241 ± 140.256  ms/op
 * RNGBenchmark.measureLightInt            avgt    3  1277.823 ± 199.616  ms/op
 * RNGBenchmark.measureLightIntR           avgt    3  1424.500 ±  31.110  ms/op
 * RNGBenchmark.measureLightR              avgt    3  1271.564 ±  73.357  ms/op\
 * RNGBenchmark.measurePermuted            avgt    3  1647.924 ±  52.709  ms/op
 * RNGBenchmark.measurePermutedInt         avgt    3  1747.788 ±  47.732  ms/op
 * RNGBenchmark.measurePermutedIntR        avgt    3  1749.924 ±  85.835  ms/op
 * RNGBenchmark.measurePermutedR           avgt    3  1649.223 ±  28.546  ms/op
 * RNGBenchmark.measureXoRo                avgt    3  1228.254 ±  16.915  ms/op
 * RNGBenchmark.measureXoRoInt             avgt    3  1395.978 ±  80.767  ms/op
 * RNGBenchmark.measureXoRoIntR            avgt    3  1475.439 ±  30.060  ms/op
 * RNGBenchmark.measureXoRoR               avgt    3  1297.531 ±  32.635  ms/op
 * RNGBenchmark.measureXor                 avgt    3  1386.555 ±  41.859  ms/op
 * RNGBenchmark.measureXorInt              avgt    3  1286.369 ±  45.825  ms/op
 * RNGBenchmark.measureXorIntR             avgt    3  1227.971 ±  23.930  ms/op
 * RNGBenchmark.measureXorR                avgt    3  1354.662 ±  83.443  ms/op
 *
 * Benchmark                        Mode  Cnt     Score     Error  Units
 * RNGBenchmark.measureGDX          avgt    3  1335.200 ±  55.131  ms/op
 * RNGBenchmark.measureGDXInt       avgt    3  1284.587 ± 115.753  ms/op
 * RNGBenchmark.measureLight        avgt    3  1220.308 ± 152.938  ms/op
 * RNGBenchmark.measureLightInt     avgt    3  1215.762 ±  88.660  ms/op
 * RNGBenchmark.measureLightIntR    avgt    3  1365.114 ±  99.494  ms/op
 * RNGBenchmark.measureLightR       avgt    3  1221.436 ±  69.999  ms/op
 * RNGBenchmark.measurePermuted     avgt    3  1599.886 ± 628.296  ms/op
 * RNGBenchmark.measurePermutedInt  avgt    3  1672.186 ± 110.668  ms/op
 * RNGBenchmark.measureThunder      avgt    3   761.156 ±  26.884  ms/op
 * RNGBenchmark.measureThunderInt   avgt    3   846.351 ± 315.138  ms/op
 * RNGBenchmark.measureThunderIntR  avgt    3   918.034 ± 223.494  ms/op
 * RNGBenchmark.measureThunderR     avgt    3   838.914 ±  62.472  ms/op
 * RNGBenchmark.measureXoRo         avgt    3  1179.352 ±  44.233  ms/op
 * RNGBenchmark.measureXoRoInt      avgt    3  1342.901 ±  21.042  ms/op
 * RNGBenchmark.measureXoRoIntR     avgt    3  1415.585 ±  71.514  ms/op
 * RNGBenchmark.measureXoRoR        avgt    3  1245.577 ±  39.306  ms/op
 *
 * Benchmark                        Mode  Cnt     Score     Error  Units
 * RNGBenchmark.measureGDX          avgt    3  1349.265 ±  58.767  ms/op
 * RNGBenchmark.measureGDXInt       avgt    3  1313.436 ± 275.429  ms/op
 * RNGBenchmark.measureLight        avgt    3  1268.311 ±  54.113  ms/op
 * RNGBenchmark.measureLightInt     avgt    3  1268.068 ±  67.096  ms/op
 * RNGBenchmark.measureLightIntR    avgt    3  1430.136 ±  64.875  ms/op
 * RNGBenchmark.measureLightR       avgt    3  1272.734 ±  79.132  ms/op
 * RNGBenchmark.measurePermuted     avgt    3  1649.275 ±  55.005  ms/op
 * RNGBenchmark.measurePermutedInt  avgt    3  1773.061 ± 973.661  ms/op
 * RNGBenchmark.measureThunder      avgt    3   714.949 ±  41.154  ms/op
 * RNGBenchmark.measureThunderInt   avgt    3   793.242 ±  12.410  ms/op
 * RNGBenchmark.measureThunderIntR  avgt    3   793.398 ±  41.674  ms/op
 * RNGBenchmark.measureThunderR     avgt    3   715.476 ±  84.131  ms/op
 * RNGBenchmark.measureXoRo         avgt    3  1233.067 ±  17.727  ms/op
 * RNGBenchmark.measureXoRoInt      avgt    3  1407.854 ± 111.257  ms/op
 * RNGBenchmark.measureXoRoIntR     avgt    3  1470.919 ±  57.782  ms/op
 * RNGBenchmark.measureXoRoR        avgt    3  1303.842 ± 116.414  ms/op
 *
 * Benchmark                        Mode  Cnt     Score     Error  Units
 * RNGBenchmark.measureGDX          avgt    3  1330.069 ±  82.401  ms/op
 * RNGBenchmark.measureGDXInt       avgt    3  1272.507 ± 103.801  ms/op
 * RNGBenchmark.measureLight        avgt    3  1233.945 ± 653.550  ms/op
 * RNGBenchmark.measureLightInt     avgt    3  1211.301 ±  79.526  ms/op
 * RNGBenchmark.measureLightIntR    avgt    3  1392.179 ± 552.502  ms/op
 * RNGBenchmark.measureLightR       avgt    3  1207.975 ± 107.328  ms/op
 * RNGBenchmark.measureThunder      avgt    3   848.403 ±  55.360  ms/op
 * RNGBenchmark.measureThunderInt   avgt    3   920.933 ±  55.772  ms/op
 * RNGBenchmark.measureThunderIntR  avgt    3  1074.927 ± 119.307  ms/op
 * RNGBenchmark.measureThunderR     avgt    3   916.797 ±  58.557  ms/op
 * RNGBenchmark.measureXoRo         avgt    3  1182.115 ± 146.899  ms/op
 * RNGBenchmark.measureXoRoInt      avgt    3  1382.232 ±  68.132  ms/op
 * RNGBenchmark.measureXoRoIntR     avgt    3  1458.964 ±  10.307  ms/op
 * RNGBenchmark.measureXoRoR        avgt    3  1249.812 ± 450.441  ms/op
 *
 * Benchmark                        Mode  Cnt     Score     Error  Units
 * RNGBenchmark.measureGDX          avgt    5  1443.180 ±  21.341  ms/op
 * RNGBenchmark.measureGDXInt       avgt    5  1389.316 ±  28.694  ms/op
 * RNGBenchmark.measureLight        avgt    5  1322.170 ±  17.512  ms/op
 * RNGBenchmark.measureLightInt     avgt    5  1321.347 ±   8.074  ms/op
 * RNGBenchmark.measureLightIntR    avgt    5  1497.184 ±  63.809  ms/op
 * RNGBenchmark.measureLightR       avgt    5  1319.403 ±  16.077  ms/op
 * RNGBenchmark.measurePermuted     avgt    5  1678.603 ±  19.053  ms/op
 * RNGBenchmark.measurePermutedInt  avgt    5  1759.054 ± 253.032  ms/op
 * RNGBenchmark.measurePint         avgt    5  4433.747 ±  12.294  ms/op
 * RNGBenchmark.measurePintInt      avgt    5  1788.244 ±  14.589  ms/op
 * RNGBenchmark.measurePintIntR     avgt    5  1791.904 ±  36.237  ms/op
 * RNGBenchmark.measurePintR        avgt    5  4581.224 ±  77.325  ms/op
 * RNGBenchmark.measureThunder      avgt    5   914.464 ±   6.066  ms/op
 * RNGBenchmark.measureThunderInt   avgt    5  1075.635 ±  19.411  ms/op
 * RNGBenchmark.measureThunderIntR  avgt    5  1156.366 ±  10.684  ms/op
 * RNGBenchmark.measureThunderR     avgt    5   992.199 ±   5.749  ms/op
 * RNGBenchmark.measureXoRo         avgt    5  1353.115 ±  14.770  ms/op
 * RNGBenchmark.measureXoRoInt      avgt    5  1440.937 ±  26.151  ms/op
 * RNGBenchmark.measureXoRoIntR     avgt    5  1551.285 ±  45.324  ms/op
 * RNGBenchmark.measureXoRoR        avgt    5  1415.288 ±  28.783  ms/op
 *
 * Benchmark                          Mode  Cnt     Score    Error  Units
 * RNGBenchmark.a__measureThrust      avgt    5   958.039 ± 13.834  ms/op // a__ is just to put it first in sort order
 * RNGBenchmark.a__measureThrustInt   avgt    5  1038.220 ± 13.306  ms/op
 * RNGBenchmark.a__measureThrustIntR  avgt    5  1137.896 ± 33.956  ms/op
 * RNGBenchmark.a__measureThrustR     avgt    5  1053.179 ± 32.774  ms/op
 * RNGBenchmark.measureGDX            avgt    5  1505.992 ± 19.182  ms/op
 * RNGBenchmark.measureGDXInt         avgt    5  1460.561 ±  6.722  ms/op
 * RNGBenchmark.measureLight          avgt    5  1391.907 ± 27.542  ms/op
 * RNGBenchmark.measureLightInt       avgt    5  1391.948 ± 18.301  ms/op
 * RNGBenchmark.measureLightIntR      avgt    5  1565.566 ± 18.779  ms/op
 * RNGBenchmark.measureLightR         avgt    5  1391.884 ± 30.517  ms/op
 * RNGBenchmark.measureMathUtils      avgt    5  1461.594 ± 13.090  ms/op
 * RNGBenchmark.measureMathUtilsInt   avgt    5  1408.281 ± 11.862  ms/op
 * RNGBenchmark.measureThunder        avgt    5   959.073 ± 37.573  ms/op
 * RNGBenchmark.measureThunderInt     avgt    5  1151.022 ± 65.215  ms/op
 * RNGBenchmark.measureThunderIntR    avgt    5  1268.665 ± 95.532  ms/op
 * RNGBenchmark.measureThunderR       avgt    5  1062.441 ± 34.203  ms/op
 * RNGBenchmark.measureXoRo           avgt    5  1458.820 ± 66.584  ms/op
 * RNGBenchmark.measureXoRoInt        avgt    5  1550.647 ± 52.001  ms/op
 * RNGBenchmark.measureXoRoIntR       avgt    5  1642.572 ± 39.190  ms/op
 * RNGBenchmark.measureXoRoR          avgt    5  1480.749 ± 63.941  ms/op
 */
public class RNGBenchmark {

    private static long seed = 9000;
    private static int iseed = 9000;

//    public long doPint()
//    {
//        PintRNG rng = new PintRNG(seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePint() throws InterruptedException {
//        seed = 9000;
//        doPint();
//    }
//
//    public long doPintInt()
//    {
//        PintRNG rng = new PintRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePintInt() throws InterruptedException {
//        iseed = 9000;
//        doPintInt();
//    }
//    public long doPintR()
//    {
//        RNG rng = new RNG(new PintRNG(seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePintR() throws InterruptedException {
//        seed = 9000;
//        doPintR();
//    }
//
//    public long doPintIntR()
//    {
//        RNG rng = new RNG(new PintRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePintIntR() throws InterruptedException {
//        iseed = 9000;
//        doPintIntR();
//    }

    public long doThunder()
    {
        ThunderRNG rng = new ThunderRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureThunder() throws InterruptedException {
        seed = 9000;
        doThunder();
    }

    public long doThunderInt()
    {
        ThunderRNG rng = new ThunderRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureThunderInt() throws InterruptedException {
        iseed = 9000;
        doThunderInt();
    }
    public long doThunderR()
    {
        RNG rng = new RNG(new ThunderRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureThunderR() throws InterruptedException {
        seed = 9000;
        doThunderR();
    }

    public long doThunderIntR()
    {
        RNG rng = new RNG(new ThunderRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureThunderIntR() throws InterruptedException {
        iseed = 9000;
        doThunderIntR();
    }

    public long doXoRo()
    {
        XoRoRNG rng = new XoRoRNG(seed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXoRo() throws InterruptedException {
        seed = 9000;
        doXoRo();
    }

    public long doXoRoInt()
    {
        XoRoRNG rng = new XoRoRNG(iseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXoRoInt() throws InterruptedException {
        iseed = 9000;
        doXoRoInt();
    }

    public long doXoRoR()
    {
        RNG rng = new RNG(new XoRoRNG(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXoRoR() throws InterruptedException {
        seed = 9000;
        doXoRoR();
    }

    public long doXoRoIntR()
    {
        RNG rng = new RNG(new XoRoRNG(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXoRoIntR() throws InterruptedException {
        iseed = 9000;
        doXoRoIntR();
    }

    public long doLight()
    {
        LightRNG rng = new LightRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureLight() throws InterruptedException {
        seed = 9000;
        doLight();
    }

    public long doLightInt()
    {
        LightRNG rng = new LightRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureLightInt() throws InterruptedException {
        iseed = 9000;
        doLightInt();
    }
    public long doLightR()
    {
        RNG rng = new RNG(new LightRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureLightR() throws InterruptedException {
        seed = 9000;
        doLightR();
    }

    public long doLightIntR()
    {
        RNG rng = new RNG(new LightRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureLightIntR() throws InterruptedException {
        iseed = 9000;
        doLightIntR();
    }

    public long doThrust()
    {
        ThrustRNG rng = new ThrustRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrust() throws InterruptedException {
        seed = 9000;
        doThrust();
    }

    public long doThrustInt()
    {
        ThrustRNG rng = new ThrustRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrustInt() throws InterruptedException {
        iseed = 9000;
        doThrustInt();
    }

    public long doThrustR()
    {
        RNG rng = new RNG(new ThrustRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrustR() throws InterruptedException {
        seed = 9000;
        doThrustR();
    }

    public long doThrustIntR()
    {
        RNG rng = new RNG(new ThrustRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void a__measureThrustIntR() throws InterruptedException {
        iseed = 9000;
        doThrustIntR();
    }

//    public long doPermuted()
//    {
//        PermutedRNG rng = new PermutedRNG(seed);
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePermuted() throws InterruptedException {
//        seed = 9000;
//        doPermuted();
//    }
//
//    public long doPermutedInt()
//    {
//        PermutedRNG rng = new PermutedRNG(iseed);
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePermutedInt() throws InterruptedException {
//        iseed = 9000;
//        doPermutedInt();
//    }
//
//    public long doPermutedR()
//    {
//        RNG rng = new RNG(new PermutedRNG(seed));
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePermutedR() throws InterruptedException {
//        seed = 9000;
//        doPermutedR();
//    }
//
//    public long doPermutedIntR()
//    {
//        RNG rng = new RNG(new PermutedRNG(iseed));
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measurePermutedIntR() throws InterruptedException {
//        iseed = 9000;
//        doPermutedIntR();
//    }
//
//
//    public long doLFSR()
//    {
//        LFSR rng = new LFSR(seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLFSR() throws InterruptedException {
//        seed = 9000;
//        doLFSR();
//    }
//
//    public long doLFSRInt()
//    {
//        LFSR rng = new LFSR(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLFSRInt() throws InterruptedException {
//        iseed = 9000;
//        doLFSRInt();
//    }
//    public long doLFSRR()
//    {
//        RNG rng = new RNG(new LFSR(seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLFSRR() throws InterruptedException {
//        seed = 9000;
//        doLFSRR();
//    }
//
//    public long doLFSRIntR()
//    {
//        RNG rng = new RNG(new LFSR(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureLFSRIntR() throws InterruptedException {
//        iseed = 9000;
//        doLFSRIntR();
//    }
//
//
//    public long doFlap()
//    {
//        FlapRNG rng = new FlapRNG(seed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlap() throws InterruptedException {
//        seed = 9000;
//        doFlap();
//    }
//
//    public long doFlapInt()
//    {
//        FlapRNG rng = new FlapRNG(iseed);
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.next(32);
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlapInt() throws InterruptedException {
//        iseed = 9000;
//        doFlapInt();
//    }
//
//    public long doFlapR()
//    {
//        RNG rng = new RNG(new FlapRNG(seed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            seed += rng.nextLong();
//        }
//        return seed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlapR() throws InterruptedException {
//        seed = 9000;
//        doFlapR();
//    }
//
//    public long doFlapIntR()
//    {
//        RNG rng = new RNG(new FlapRNG(iseed));
//
//        for (int i = 0; i < 1000000000; i++) {
//            iseed += rng.nextInt();
//        }
//        return iseed;
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    public void measureFlapIntR() throws InterruptedException {
//        iseed = 9000;
//        doFlapIntR();
//    }

    public long doGDX()
    {
        RandomXS128 rng = new RandomXS128(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureGDX() throws InterruptedException {
        seed = 9000;
        doGDX();
    }

    public long doGDXInt()
    {
        RandomXS128 rng = new RandomXS128(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureGDXInt() throws InterruptedException {
        iseed = 9000;
        doGDXInt();
    }

    public long doMathUtils()
    {
        Random rng = MathUtils.random;

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureMathUtils() throws InterruptedException {
        seed = 9000;
        doMathUtils();
    }

    public long doMathUtilsInt()
    {
        Random rng = MathUtils.random;

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)
    public void measureMathUtilsInt() throws InterruptedException {
        iseed = 9000;
        doMathUtilsInt();
    }


    /*
    public long doXor()
    {
        XorRNG rng = new XorRNG(seed);
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXor() throws InterruptedException {
        seed = 9000;
        doXor();
    }

    public long doXorInt()
    {
        XorRNG rng = new XorRNG(iseed);
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXorInt() throws InterruptedException {
        iseed = 9000;
        doXorInt();
    }

    public long doXorR()
    {
        RNG rng = new RNG(new XorRNG(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXorR() throws InterruptedException {
        seed = 9000;
        doXorR();
    }

    public long doXorIntR()
    {
        RNG rng = new RNG(new XorRNG(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureXorIntR() throws InterruptedException {
        iseed = 9000;
        doXorIntR();
    }

    public long doChaosR()
    {
        RNG rng = new RNG(new ChaosRNG());
        //rng.setSeed(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureChaosR() throws InterruptedException {
        seed = 9000;
        doChaosR();
    }

    public long doChaosRInt()
    {
        RNG rng = new RNG(new ChaosRNG());
        //rng.setSeed(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureChaosRInt() throws InterruptedException {
        iseed = 9000;
        doChaosRInt();
    }
    public long doThreadLocalRandom()
    {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        //rng.setSeed(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureThreadLocalRandom() throws InterruptedException {
        seed = 9000;
        doThreadLocalRandom();
    }

    public long doThreadLocalRandomInt()
    {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        //rng.setSeed(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureThreadLocalRandomInt() throws InterruptedException {
        iseed = 9000;
        doThreadLocalRandomInt();
    }
    public long doRandom()
    {
        Random rng = new Random(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureRandom() throws InterruptedException {
        seed = 9000;
        doRandom();
    }

    public long doRandomInt()
    {
        Random rng = new Random(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    //@Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureRandomInt() throws InterruptedException {
        iseed = 9000;
        doRandomInt();
    }


    /*
    public long doIsaac()
    {
        IsaacRNG rng = new IsaacRNG(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureIsaac() throws InterruptedException {
        seed = 9000;
        doIsaac();
    }

    public long doIsaacInt()
    {
        IsaacRNG rng = new IsaacRNG(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.next(32);
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureIsaacInt() throws InterruptedException {
        iseed = 9000;
        doIsaacInt();
    }
    public long doIsaacR()
    {
        RNG rng = new RNG(new IsaacRNG(seed));

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureIsaacR() throws InterruptedException {
        seed = 9000;
        doIsaacR();
    }

    public long doIsaacIntR()
    {
        RNG rng = new RNG(new IsaacRNG(iseed));

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    */

/*
    public long doSecureRandom()
    {
        SecureRandom rng = new SecureRandom();
        //rng.setSeed(seed);

        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureSecureRandom() throws InterruptedException {
        seed = 9000;
        doSecureRandom();
    }

    public long doSecureRandomInt()
    {
        SecureRandom rng = new SecureRandom();
        //rng.setSeed(iseed);

        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }

    @Benchmark @BenchmarkMode(Mode.AverageTime) @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5) @Measurement(iterations = 5) @Fork(1)

    public void measureSecureRandomInt() throws InterruptedException {
        iseed = 9000;
        doSecureRandomInt();
    }
*/


    public long doMT()
    {
        RNG rng = new RNG(new MersenneTwister(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMT() throws InterruptedException {
        seed = 9000;
        doMT();
    }

    public long doMTInt()
    {
        RNG rng = new RNG(new MersenneTwister(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMTInt() throws InterruptedException {
        iseed = 9000;
        doMTInt();
    }

    public long doMTR()
    {
        RNG rng = new RNG(new MersenneTwister(seed));
        for (int i = 0; i < 1000000000; i++) {
            seed += rng.nextLong();
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMTR() throws InterruptedException {
        seed = 9000;
        doMTR();
    }

    public long doMTIntR()
    {
        RNG rng = new RNG(new MersenneTwister(iseed));
        for (int i = 0; i < 1000000000; i++) {
            iseed += rng.nextInt();
        }
        return iseed;
    }
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMTIntR() throws InterruptedException {
        iseed = 9000;
        doMTIntR();
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
     *    $ java -jar target/benchmarks.jar RNGBenchmark -wi 3 -i 3 -f 1
     *
     *    (we requested 5 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RNGBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
