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

import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark results, going back to the beginning. Most recent results are at the bottom.
 * <br>
 * <pre>
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
 * </pre>
 * <br>
 * Updated measurements to use JMH's recommendations.
 * Now, measurements are in nanoseconds per call, instead of milliseconds per billion calls.
 * <br>
 * <pre>
 * Benchmark                            Mode  Cnt   Score   Error  Units
 * RNGBenchmark.measureFlap             avgt    4   3.711 ± 0.090  ns/op
 * RNGBenchmark.measureFlapInt          avgt    4   3.315 ± 0.386  ns/op
 * RNGBenchmark.measureFlapIntR         avgt    4   3.783 ± 0.047  ns/op
 * RNGBenchmark.measureFlapR            avgt    4   4.623 ± 0.763  ns/op
 * RNGBenchmark.measureGWTRNG           avgt    4   5.991 ± 0.137  ns/op
 * RNGBenchmark.measureGWTRNGInt        avgt    4   4.089 ± 0.164  ns/op
 * RNGBenchmark.measureJDK              avgt    4  24.362 ± 0.097  ns/op
 * RNGBenchmark.measureJDKInt           avgt    4  12.246 ± 0.305  ns/op
 * RNGBenchmark.measureLap              avgt    4   3.336 ± 0.091  ns/op
 * RNGBenchmark.measureLapInt           avgt    4   3.445 ± 0.077  ns/op
 * RNGBenchmark.measureLapIntR          avgt    4   4.000 ± 0.042  ns/op
 * RNGBenchmark.measureLapR             avgt    4   3.795 ± 0.594  ns/op
 * RNGBenchmark.measureLathe32          avgt    4   5.658 ± 0.106  ns/op
 * RNGBenchmark.measureLathe32Int       avgt    4   3.858 ± 0.145  ns/op
 * RNGBenchmark.measureLathe32IntR      avgt    4   4.155 ± 0.121  ns/op
 * RNGBenchmark.measureLathe32R         avgt    4   6.396 ± 0.053  ns/op
 * RNGBenchmark.measureLight            avgt    4   3.865 ± 0.165  ns/op
 * RNGBenchmark.measureLight32          avgt    4   6.181 ± 0.181  ns/op
 * RNGBenchmark.measureLight32Int       avgt    4   3.720 ± 0.071  ns/op
 * RNGBenchmark.measureLight32IntR      avgt    4   4.187 ± 0.072  ns/op
 * RNGBenchmark.measureLight32R         avgt    4   7.183 ± 0.204  ns/op
 * RNGBenchmark.measureLightInt         avgt    4   3.859 ± 0.227  ns/op
 * RNGBenchmark.measureLightIntR        avgt    4   4.440 ± 0.344  ns/op
 * RNGBenchmark.measureLightR           avgt    4   4.397 ± 0.148  ns/op
 * RNGBenchmark.measureLongPeriod       avgt    4   4.916 ± 0.095  ns/op
 * RNGBenchmark.measureLongPeriodInt    avgt    4   5.023 ± 0.248  ns/op
 * RNGBenchmark.measureLongPeriodIntR   avgt    4   5.845 ± 0.102  ns/op
 * RNGBenchmark.measureLongPeriodR      avgt    4   5.527 ± 0.271  ns/op
 * RNGBenchmark.measureMathUtils        avgt    4   4.102 ± 0.128  ns/op
 * RNGBenchmark.measureMathUtilsInt     avgt    4   4.050 ± 0.133  ns/op
 * RNGBenchmark.measureOriole32         avgt    4   6.615 ± 0.429  ns/op
 * RNGBenchmark.measureOriole32Int      avgt    4   4.352 ± 0.065  ns/op
 * RNGBenchmark.measureOriole32IntR     avgt    4   4.944 ± 0.065  ns/op
 * RNGBenchmark.measureOriole32R        avgt    4   7.170 ± 0.057  ns/op
 * RNGBenchmark.measureThreadLocal      avgt    4   4.101 ± 1.138  ns/op
 * RNGBenchmark.measureThreadLocalInt   avgt    4   3.853 ± 0.205  ns/op
 * RNGBenchmark.measureThrust           avgt    4   3.351 ± 0.301  ns/op
 * RNGBenchmark.measureThrustAlt        avgt    4   3.518 ± 0.164  ns/op
 * RNGBenchmark.measureThrustAlt32      avgt    4   6.086 ± 0.299  ns/op
 * RNGBenchmark.measureThrustAlt32Int   avgt    4   3.888 ± 0.073  ns/op
 * RNGBenchmark.measureThrustAlt32IntR  avgt    4   4.366 ± 0.285  ns/op
 * RNGBenchmark.measureThrustAlt32R     avgt    4   7.078 ± 0.140  ns/op
 * RNGBenchmark.measureThrustAltInt     avgt    4   3.490 ± 0.049  ns/op
 * RNGBenchmark.measureThrustAltIntR    avgt    4   3.902 ± 0.216  ns/op
 * RNGBenchmark.measureThrustAltR       avgt    4   5.162 ± 0.254  ns/op
 * RNGBenchmark.measureThrustInt        avgt    4   3.488 ± 0.049  ns/op
 * RNGBenchmark.measureThrustIntR       avgt    4   3.895 ± 0.070  ns/op
 * RNGBenchmark.measureThrustR          avgt    4   3.887 ± 0.088  ns/op
 * RNGBenchmark.measureVortex           avgt    4   3.723 ± 0.242  ns/op
 * RNGBenchmark.measureVortexInt        avgt    4   3.927 ± 0.375  ns/op
 * RNGBenchmark.measureVortexIntR       avgt    4   4.261 ± 0.951  ns/op
 * RNGBenchmark.measureVortexR          avgt    4   4.050 ± 0.305  ns/op
 * RNGBenchmark.measureXoRo             avgt    4   3.735 ± 0.312  ns/op
 * RNGBenchmark.measureXoRoInt          avgt    4   3.765 ± 0.203  ns/op
 * RNGBenchmark.measureXoRoIntR         avgt    4   4.104 ± 0.293  ns/op
 * RNGBenchmark.measureXoRoR            avgt    4   4.178 ± 0.228  ns/op
 * RNGBenchmark.measureZag32            avgt    4   7.819 ± 0.447  ns/op
 * RNGBenchmark.measureZag32Int         avgt    4   5.635 ± 0.149  ns/op
 * RNGBenchmark.measureZag32IntR        avgt    4   5.342 ± 0.105  ns/op
 * RNGBenchmark.measureZag32R           avgt    4   8.668 ± 0.234  ns/op
 * </pre>
 * Revising a few numbers after marking GWTRNG's class and metods as final:
 * <br>
 * <pre>
 * Benchmark                        Mode  Cnt  Score   Error  Units
 * RNGBenchmark.measureGWTRNG       avgt    4  5.678 ± 0.294  ns/op // pretty much identical
 * RNGBenchmark.measureGWTRNGInt    avgt    4  3.842 ± 0.060  ns/op
 * RNGBenchmark.measureLathe32      avgt    4  5.678 ± 0.278  ns/op // pretty much identical
 * RNGBenchmark.measureLathe32Int   avgt    4  3.853 ± 0.105  ns/op
 * RNGBenchmark.measureLathe32IntR  avgt    4  4.176 ± 0.076  ns/op
 * RNGBenchmark.measureLathe32R     avgt    4  6.417 ± 0.437  ns/op
 * </pre>
 * <br>
 * Marking these small methods as final seems to make a substantial difference. The lines that have the comment "pretty
 * much identical" were not identical before, and without the GWTRNG class and methods marked final, GWTRNG was slower.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class RNGBenchmark {
    
    private XoRoRNG XoRo = new XoRoRNG(9999L);
    private RNG XoRoR = new RNG(XoRo);
    @Benchmark
    public long measureXoRo()
    {
        return XoRo.nextLong();
    }

    @Benchmark
    public int measureXoRoInt()
    {
        return XoRo.next(32);
    }
    @Benchmark
    public long measureXoRoR()
    {
        return XoRoR.nextLong();
    }

    @Benchmark
    public int measureXoRoIntR()
    {
        return XoRoR.nextInt();
    }
    
    private LongPeriodRNG LongPeriod = new LongPeriodRNG(9999L);
    private RNG LongPeriodR = new RNG(LongPeriod);
    @Benchmark
    public long measureLongPeriod()
    {
        return LongPeriod.nextLong();
    }
    @Benchmark
    public int measureLongPeriodInt()
    {
        return LongPeriod.next(32);
    }
    @Benchmark
    public long measureLongPeriodR()
    {
        return LongPeriodR.nextLong();
    }
    @Benchmark
    public int measureLongPeriodIntR()
    {
        return LongPeriodR.nextInt();
    }


    private LightRNG Light = new LightRNG(9999L);
    private RNG LightR = new RNG(Light);
    @Benchmark
    public long measureLight()
    {
        return Light.nextLong();
    }
    @Benchmark
    public int measureLightInt()
    {
        return Light.next(32);
    }
    @Benchmark
    public long measureLightR()
    {
        return LightR.nextLong();
    }
    @Benchmark
    public int measureLightIntR()
    {
        return LightR.nextInt();
    }
    
    private FlapRNG Flap = new FlapRNG(9999L);
    private RNG FlapR = new RNG(Flap);
    @Benchmark
    public long measureFlap()
    {
        return Flap.nextLong();
    }
    @Benchmark
    public int measureFlapInt()
    {
        return Flap.next(32);
    }
    @Benchmark
    public long measureFlapR()
    {
        return FlapR.nextLong();
    }
    @Benchmark
    public int measureFlapIntR()
    {
        return FlapR.nextInt();
    }

    private LapRNG Lap = new LapRNG(9999L);
    private RNG LapR = new RNG(Lap);
    @Benchmark
    public long measureLap()
    {
        return Lap.nextLong();
    }
    @Benchmark
    public int measureLapInt()
    {
        return Lap.next(32);
    }
    @Benchmark
    public long measureLapR()
    {
        return LapR.nextLong();
    }
    @Benchmark
    public int measureLapIntR()
    {
        return LapR.nextInt();
    }


    private ThrustRNG Thrust = new ThrustRNG(9999L);
    private RNG ThrustR = new RNG(Thrust);
    @Benchmark
    public long measureThrust()
    {
        return Thrust.nextLong();
    }
    @Benchmark
    public int measureThrustInt()
    {
        return Thrust.next(32);
    }
    @Benchmark
    public long measureThrustR()
    {
        return ThrustR.nextLong();
    }
    @Benchmark
    public int measureThrustIntR()
    {
        return ThrustR.nextInt();
    }

    private ThrustAltRNG ThrustAlt = new ThrustAltRNG(9999L);
    private RNG ThrustAltR = new RNG(ThrustAlt);
    @Benchmark
    public long measureThrustAlt()
    {
        return ThrustAlt.nextLong();
    }
    @Benchmark
    public int measureThrustAltInt()
    {
        return ThrustAlt.next(32);
    }
    @Benchmark
    public long measureThrustAltR()
    {
        return ThrustAltR.nextLong();
    }
    @Benchmark
    public int measureThrustAltIntR()
    {
        return ThrustAltR.nextInt();
    }


    private VortexRNG Vortex = new VortexRNG(9999L);
    private RNG VortexR = new RNG(Vortex);
    @Benchmark
    public long measureVortex()
    {
        return Vortex.nextLong();
    }
    @Benchmark
    public int measureVortexInt()
    {
        return Vortex.next(32);
    }
    @Benchmark
    public long measureVortexR()
    {
        return VortexR.nextLong();
    }
    @Benchmark
    public int measureVortexIntR()
    {
        return VortexR.nextInt();
    }



    //    private Thrust32RNG Thrust32 = new Thrust32RNG(9999);
//    private RNG Thrust32R = new RNG(Thrust32);
//
//    @Benchmark
//    public long measureThrust32()
//    {
//        return Thrust32.nextLong();
//    }
//
//    @Benchmark
//    public long measureThrust32Int()
//    {
//        return Thrust32.next(32);
//    }
//    @Benchmark
//    public long measureThrust32R()
//    {
//        return Thrust32R.nextLong();
//    }
//
//    @Benchmark
//    public long measureThrust32IntR()
//    {
//        return Thrust32R.nextInt();
//    }
//
//
    private ThrustAlt32RNG ThrustAlt32 = new ThrustAlt32RNG(9999);
    private RNG ThrustAlt32R = new RNG(ThrustAlt32);
    @Benchmark
    public long measureThrustAlt32()
    {
        return ThrustAlt32.nextLong();
    }
    @Benchmark
    public int measureThrustAlt32Int()
    {
        return ThrustAlt32.next(32);
    }
    @Benchmark
    public long measureThrustAlt32R() { return ThrustAlt32R.nextLong(); }
    @Benchmark
    public int measureThrustAlt32IntR()
    {
        return ThrustAlt32R.nextInt();
    }

    private Light32RNG Light32 = new Light32RNG(9999);
    private RNG Light32R = new RNG(Light32);
    @Benchmark
    public long measureLight32()
    {
        return Light32.nextLong();
    }
    @Benchmark
    public int measureLight32Int()
    {
        return Light32.next(32);
    }
    @Benchmark
    public long measureLight32R()
    {
        return Light32R.nextLong();
    }
    @Benchmark
    public int measureLight32IntR()
    {
        return Light32R.nextInt();
    }

    private Zag32RNG Zag32 = new Zag32RNG(9999L);
    private RNG Zag32R = new RNG(Zag32);
    @Benchmark
    public long measureZag32()
    {
        return Zag32.nextLong();
    }
    @Benchmark
    public int measureZag32Int()
    {
        return Zag32.next(32);
    }
    @Benchmark
    public long measureZag32R()
    {
        return Zag32R.nextLong();
    }
    @Benchmark
    public int measureZag32IntR()
    {
        return Zag32R.nextInt();
    }
    
    private Oriole32RNG Oriole32 = new Oriole32RNG(9999, 999, 99);
    private RNG Oriole32R = new RNG(Oriole32);
    @Benchmark
    public long measureOriole32()
    {
        return Oriole32.nextLong();
    }

    @Benchmark
    public int measureOriole32Int()
    {
        return Oriole32.next(32);
    }
    @Benchmark
    public long measureOriole32R()
    {
        return Oriole32R.nextLong();
    }

    @Benchmark
    public int measureOriole32IntR()
    {
        return Oriole32R.nextInt();
    }

    private Lathe32RNG Lathe32 = new Lathe32RNG(9999, 999);
    private RNG Lathe32R = new RNG(Lathe32);
    @Benchmark
    public long measureLathe32() { return Lathe32.nextLong(); }
    @Benchmark
    public int measureLathe32Int() { return Lathe32.next(32); }
    @Benchmark
    public long measureLathe32R() { return Lathe32R.nextLong(); }
    @Benchmark
    public int measureLathe32IntR()
    {
        return Lathe32R.nextInt();
    }
    
    private GWTRNG gwtrng = new GWTRNG(9999, 99);

    @Benchmark
    public long measureGWTRNG() { return gwtrng.nextLong(); }
    @Benchmark
    public int measureGWTRNGInt()
    {
        return gwtrng.nextInt();
    }

    private Random JDK = new Random(9999L);
    @Benchmark
    public long measureJDK()
    {
        return JDK.nextLong();
    }
    @Benchmark
    public int measureJDKInt()
    {
        return JDK.nextInt();
    }

    private Random mathUtils = MathUtils.random;
    @Benchmark
    public long measureMathUtils()
    {
        return mathUtils.nextLong();
    }
    @Benchmark
    public int measureMathUtilsInt()
    {
        return mathUtils.nextInt();
    }

    private ThreadLocalRandom tlr = ThreadLocalRandom.current();
    @Benchmark
    public long measureThreadLocal()
    {
        return tlr.nextLong();
    }
    @Benchmark
    public int measureThreadLocalInt()
    {
        return tlr.nextInt();
    }
    
    
    /*
mvn clean install
java -jar target/benchmarks.jar RNGBenchmark -wi 4 -i 4 -f 1 -gc true
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
