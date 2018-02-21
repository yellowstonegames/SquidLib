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

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark timings!
 * GdxOM refers to com.badlogic.gdx.utils.OrderedMap.
 * GdxOS refers to com.badlogic.gdx.utils.OrderedSet.
 * SquidOM refers to squidpony.squidmath.OrderedMap.
 * SquidOS refers to squidpony.squidmath.OrderedSet.
 * The LinkedHash data structures are in java.util.
 * Benchmarks not followed by a number use load factor 0.75f, those followed by a 2 use load factor 0.5f, and those
 * followed by a 3 use load factor 0.25f.
 * <br>
 * SquidLib's collections do well here, except on rather large sizes (after about 8000 items, it isn't as fast relative
 * to LinkedHashMap and LinkedHashSet). Considering their added features, this is a good sign. SquidLib's collections
 * can do much better if the contents being inserted are well-understood, since their IHasher values can be changed.
 * <br>
 * <pre>
 * Benchmark                                   (SIZE)  Mode  Cnt       Score        Error  Units
 * DataStructureBenchmark.insertGdxOM            1500  avgt    4   38223.846 ±  13424.129  ns/op
 * DataStructureBenchmark.insertGdxOM            3000  avgt    4   81488.864 ±  22840.893  ns/op
 * DataStructureBenchmark.insertGdxOM            6000  avgt    4  173953.749 ±  41153.628  ns/op
 * DataStructureBenchmark.insertGdxOM           12000  avgt    4  374832.318 ±  92800.369  ns/op
 * DataStructureBenchmark.insertGdxOM2           1500  avgt    4   35528.416 ±   5805.535  ns/op
 * DataStructureBenchmark.insertGdxOM2           3000  avgt    4   78785.561 ±  32597.491  ns/op
 * DataStructureBenchmark.insertGdxOM2           6000  avgt    4  166947.332 ±  59079.304  ns/op
 * DataStructureBenchmark.insertGdxOM2          12000  avgt    4  371062.396 ± 126467.131  ns/op
 * DataStructureBenchmark.insertGdxOM3           1500  avgt    4   42093.620 ±   9376.479  ns/op
 * DataStructureBenchmark.insertGdxOM3           3000  avgt    4   80130.295 ±  24024.720  ns/op
 * DataStructureBenchmark.insertGdxOM3           6000  avgt    4  165168.479 ±  42004.948  ns/op
 * DataStructureBenchmark.insertGdxOM3          12000  avgt    4  345591.959 ±  20866.678  ns/op
 * DataStructureBenchmark.insertGdxOS            1500  avgt    4   17491.454 ±   1531.361  ns/op
 * DataStructureBenchmark.insertGdxOS            3000  avgt    4   49453.289 ±  27550.976  ns/op
 * DataStructureBenchmark.insertGdxOS            6000  avgt    4   98938.636 ±  14057.754  ns/op
 * DataStructureBenchmark.insertGdxOS           12000  avgt    4  227688.018 ±  53812.489  ns/op
 * DataStructureBenchmark.insertGdxOS2           1500  avgt    4   19020.652 ±   7526.830  ns/op
 * DataStructureBenchmark.insertGdxOS2           3000  avgt    4   42130.408 ±  13797.720  ns/op
 * DataStructureBenchmark.insertGdxOS2           6000  avgt    4   92720.878 ±   7412.059  ns/op
 * DataStructureBenchmark.insertGdxOS2          12000  avgt    4  190499.922 ±  32431.179  ns/op
 * DataStructureBenchmark.insertGdxOS3           1500  avgt    4   20918.325 ±   8335.336  ns/op
 * DataStructureBenchmark.insertGdxOS3           3000  avgt    4   45803.509 ±  21906.209  ns/op
 * DataStructureBenchmark.insertGdxOS3           6000  avgt    4   85784.976 ±   7762.536  ns/op
 * DataStructureBenchmark.insertGdxOS3          12000  avgt    4  206220.562 ±  73177.228  ns/op
 * DataStructureBenchmark.insertLinkedHashMap    1500  avgt    4   29011.405 ±  12322.492  ns/op
 * DataStructureBenchmark.insertLinkedHashMap    3000  avgt    4   51287.682 ±  32082.172  ns/op
 * DataStructureBenchmark.insertLinkedHashMap    6000  avgt    4  101284.150 ±  16458.354  ns/op
 * DataStructureBenchmark.insertLinkedHashMap   12000  avgt    4  205865.279 ±  38719.626  ns/op
 * DataStructureBenchmark.insertLinkedHashSet    1500  avgt    4   26524.562 ±   9270.593  ns/op
 * DataStructureBenchmark.insertLinkedHashSet    3000  avgt    4   53896.965 ±  12663.896  ns/op
 * DataStructureBenchmark.insertLinkedHashSet    6000  avgt    4  100259.050 ±  18969.343  ns/op
 * DataStructureBenchmark.insertLinkedHashSet   12000  avgt    4  201787.009 ±  58640.467  ns/op
 * DataStructureBenchmark.insertSquidOM          1500  avgt    4   21269.420 ±   7860.741  ns/op
 * DataStructureBenchmark.insertSquidOM          3000  avgt    4   43932.855 ±  12788.508  ns/op
 * DataStructureBenchmark.insertSquidOM          6000  avgt    4   99356.044 ±  28287.373  ns/op
 * DataStructureBenchmark.insertSquidOM         12000  avgt    4  257278.242 ±  59064.138  ns/op
 * DataStructureBenchmark.insertSquidOM2         1500  avgt    4   21865.323 ±   9418.607  ns/op
 * DataStructureBenchmark.insertSquidOM2         3000  avgt    4   43080.641 ±   6645.845  ns/op
 * DataStructureBenchmark.insertSquidOM2         6000  avgt    4   87484.249 ±  13538.247  ns/op
 * DataStructureBenchmark.insertSquidOM2        12000  avgt    4  227234.158 ±   1840.756  ns/op
 * DataStructureBenchmark.insertSquidOM3         1500  avgt    4   23512.106 ±   7544.661  ns/op
 * DataStructureBenchmark.insertSquidOM3         3000  avgt    4   48626.410 ±   8228.162  ns/op
 * DataStructureBenchmark.insertSquidOM3         6000  avgt    4  112120.954 ±  36337.733  ns/op
 * DataStructureBenchmark.insertSquidOM3        12000  avgt    4  243739.316 ± 115929.008  ns/op
 * DataStructureBenchmark.insertSquidOS          1500  avgt    4   13571.493 ±   5466.484  ns/op
 * DataStructureBenchmark.insertSquidOS          3000  avgt    4   26908.465 ±   7551.977  ns/op
 * DataStructureBenchmark.insertSquidOS          6000  avgt    4   92277.265 ±  49265.949  ns/op
 * DataStructureBenchmark.insertSquidOS         12000  avgt    4  211058.166 ±  69232.068  ns/op
 * DataStructureBenchmark.insertSquidOS2         1500  avgt    4   12080.495 ±   1338.051  ns/op
 * DataStructureBenchmark.insertSquidOS2         3000  avgt    4   24012.246 ±   9015.567  ns/op
 * DataStructureBenchmark.insertSquidOS2         6000  avgt    4   55176.285 ±  16451.906  ns/op
 * DataStructureBenchmark.insertSquidOS2        12000  avgt    4  127613.691 ±  52788.568  ns/op
 * DataStructureBenchmark.insertSquidOS3         1500  avgt    4   12985.633 ±   1353.662  ns/op
 * DataStructureBenchmark.insertSquidOS3         3000  avgt    4   25718.108 ±   3405.242  ns/op
 * DataStructureBenchmark.insertSquidOS3         6000  avgt    4   56471.774 ±  27519.086  ns/op
 * DataStructureBenchmark.insertSquidOS3        12000  avgt    4  132271.929 ±  26147.530  ns/op
 * </pre>
 * <br>
 * Here, FastUtilFair refers to data structures of Integer objects, hashed with an equivalent Strategy to the IHasher
 * used by the Squid data structures. Where FastUtil is used without Fair, it refers to a data structure of int
 * primitives, without a Strategy specified. The Gdx benchmarks test libGDX's OrderedMap and OrderedSet, while the Squid
 * ones test those from SquidLib. LinkedHashMap and LinkedHashSet refer to the JDK collections.
 * This benchmark tests adding 128,000 items, instead of the previous one which tested at most 12,000.
 * NEED_TO_ENLARGE refers to the expected number of times the collection will need to resize to a larger hash table,
 * which can be a very slow step in some cases. If the load factor is very low, resizing may not be needed.
 * <pre>
 * Benchmark                                     (NEED_TO_ENLARGE)  (SIZE)  Mode  Cnt         Score         Error  Units
 * DataStructureBenchmark.insertFastUtilFairOM                   0  128000  avgt    4   5467995.093 ± 1339177.478  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM                   1  128000  avgt    4  11312515.453 ± 1686175.571  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM                   2  128000  avgt    4  13188677.980 ± 2807305.410  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM2                  0  128000  avgt    4   5319175.866 ±  363177.486  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM2                  1  128000  avgt    4   8763144.350 ± 2563476.386  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM2                  2  128000  avgt    4   9895836.612 ± 2415631.524  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM3                  0  128000  avgt    4   6899457.719 ±   92937.718  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM3                  1  128000  avgt    4  15766904.305 ± 4286191.758  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM3                  2  128000  avgt    4  17836668.855 ± 3696935.149  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS                   0  128000  avgt    4   3349275.320 ±  152333.060  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS                   1  128000  avgt    4   7651072.236 ±  275885.610  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS                   2  128000  avgt    4   9179720.446 ±  210312.054  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS2                  0  128000  avgt    4   3359968.366 ±   38791.264  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS2                  1  128000  avgt    4   5612455.472 ±  182716.013  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS2                  2  128000  avgt    4   6424605.370 ±  454747.814  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS3                  0  128000  avgt    4   4050897.540 ±  165372.426  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS3                  1  128000  avgt    4   9181077.648 ± 2057051.896  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS3                  2  128000  avgt    4  10168821.872 ± 1838453.486  ns/op
 * DataStructureBenchmark.insertFastUtilOM                       0  128000  avgt    4   2843187.747 ±  461904.641  ns/op
 * DataStructureBenchmark.insertFastUtilOM                       1  128000  avgt    4   7217269.618 ±  343332.521  ns/op
 * DataStructureBenchmark.insertFastUtilOM                       2  128000  avgt    4   8838026.699 ±  303426.554  ns/op
 * DataStructureBenchmark.insertFastUtilOM2                      0  128000  avgt    4   2820253.860 ±  256510.832  ns/op
 * DataStructureBenchmark.insertFastUtilOM2                      1  128000  avgt    4   5449852.428 ±  241741.941  ns/op
 * DataStructureBenchmark.insertFastUtilOM2                      2  128000  avgt    4   6350411.234 ±  321139.785  ns/op
 * DataStructureBenchmark.insertFastUtilOM3                      0  128000  avgt    4   3840695.602 ±  882810.537  ns/op
 * DataStructureBenchmark.insertFastUtilOM3                      1  128000  avgt    4  12068036.450 ±  452083.622  ns/op
 * DataStructureBenchmark.insertFastUtilOM3                      2  128000  avgt    4  13417298.588 ±  354976.909  ns/op
 * DataStructureBenchmark.insertFastUtilOS                       0  128000  avgt    4   1978642.145 ±   82403.248  ns/op
 * DataStructureBenchmark.insertFastUtilOS                       1  128000  avgt    4   5476050.955 ±  280228.115  ns/op
 * DataStructureBenchmark.insertFastUtilOS                       2  128000  avgt    4   6822465.701 ±  309121.326  ns/op
 * DataStructureBenchmark.insertFastUtilOS2                      0  128000  avgt    4   1981006.096 ±   67538.095  ns/op
 * DataStructureBenchmark.insertFastUtilOS2                      1  128000  avgt    4   4051795.899 ±  139896.872  ns/op
 * DataStructureBenchmark.insertFastUtilOS2                      2  128000  avgt    4   4815448.044 ±  156638.545  ns/op
 * DataStructureBenchmark.insertFastUtilOS3                      0  128000  avgt    4   2246093.383 ±   39487.261  ns/op
 * DataStructureBenchmark.insertFastUtilOS3                      1  128000  avgt    4   6996761.307 ±   75879.648  ns/op
 * DataStructureBenchmark.insertFastUtilOS3                      2  128000  avgt    4   8400894.194 ± 2351854.534  ns/op
 * DataStructureBenchmark.insertGdxOM                            0  128000  avgt    4   5225571.748 ±  264691.616  ns/op
 * DataStructureBenchmark.insertGdxOM                            1  128000  avgt    4   6038371.992 ± 1269893.817  ns/op
 * DataStructureBenchmark.insertGdxOM                            2  128000  avgt    4   6886559.813 ±  962793.819  ns/op
 * DataStructureBenchmark.insertGdxOM2                           0  128000  avgt    4   5110782.399 ±  323951.301  ns/op
 * DataStructureBenchmark.insertGdxOM2                           1  128000  avgt    4   5718981.745 ±  568105.842  ns/op
 * DataStructureBenchmark.insertGdxOM2                           2  128000  avgt    4   6417904.243 ±  226042.131  ns/op
 * DataStructureBenchmark.insertGdxOS                            0  128000  avgt    4   3067600.052 ±   70734.131  ns/op
 * DataStructureBenchmark.insertGdxOS                            1  128000  avgt    4   3628835.068 ±  688683.619  ns/op
 * DataStructureBenchmark.insertGdxOS                            2  128000  avgt    4   4352282.500 ± 1039025.814  ns/op
 * DataStructureBenchmark.insertGdxOS2                           0  128000  avgt    4   3070060.543 ±  133578.431  ns/op
 * DataStructureBenchmark.insertGdxOS2                           1  128000  avgt    4   3579472.495 ±  852239.811  ns/op
 * DataStructureBenchmark.insertGdxOS2                           2  128000  avgt    4   4086231.723 ±   88166.152  ns/op
 * DataStructureBenchmark.insertLinkedHashMap                    0  128000  avgt    4   2824198.463 ±  141931.833  ns/op
 * DataStructureBenchmark.insertLinkedHashMap                    1  128000  avgt    4   3083176.907 ±  102179.093  ns/op
 * DataStructureBenchmark.insertLinkedHashMap                    2  128000  avgt    4   3228603.829 ±  174624.953  ns/op
 * DataStructureBenchmark.insertLinkedHashMap2                   0  128000  avgt    4   2665124.039 ±   62736.229  ns/op
 * DataStructureBenchmark.insertLinkedHashMap2                   1  128000  avgt    4   2817749.486 ±   84338.846  ns/op
 * DataStructureBenchmark.insertLinkedHashMap2                   2  128000  avgt    4   2947217.643 ±  118319.486  ns/op
 * DataStructureBenchmark.insertLinkedHashSet                    0  128000  avgt    4   2685192.331 ±   21647.733  ns/op
 * DataStructureBenchmark.insertLinkedHashSet                    1  128000  avgt    4   2815297.455 ±  190858.449  ns/op
 * DataStructureBenchmark.insertLinkedHashSet                    2  128000  avgt    4   3395624.359 ±  252052.585  ns/op
 * DataStructureBenchmark.insertLinkedHashSet2                   0  128000  avgt    4   2553575.730 ±   93751.570  ns/op
 * DataStructureBenchmark.insertLinkedHashSet2                   1  128000  avgt    4   3094542.163 ±  346972.417  ns/op
 * DataStructureBenchmark.insertLinkedHashSet2                   2  128000  avgt    4   2809429.002 ±   73026.417  ns/op
 * DataStructureBenchmark.insertSquidOM                          0  128000  avgt    4   1438689.367 ±   18701.123  ns/op
 * DataStructureBenchmark.insertSquidOM                          1  128000  avgt    4   1422942.452 ±   68932.347  ns/op
 * DataStructureBenchmark.insertSquidOM                          2  128000  avgt    4   1716015.846 ±   67933.398  ns/op
 * DataStructureBenchmark.insertSquidOM2                         0  128000  avgt    4   1433484.892 ±   42284.081  ns/op
 * DataStructureBenchmark.insertSquidOM2                         1  128000  avgt    4   1419283.013 ±   39392.457  ns/op
 * DataStructureBenchmark.insertSquidOM2                         2  128000  avgt    4   1772556.813 ±  174985.358  ns/op
 * DataStructureBenchmark.insertSquidOM3                         0  128000  avgt    4   1539639.754 ±   48875.378  ns/op
 * DataStructureBenchmark.insertSquidOM3                         1  128000  avgt    4   1488659.014 ±   43647.770  ns/op
 * DataStructureBenchmark.insertSquidOM3                         2  128000  avgt    4   1431233.492 ±   27577.787  ns/op
 * DataStructureBenchmark.insertSquidOS                          0  128000  avgt    4    989373.727 ±   32961.968  ns/op
 * DataStructureBenchmark.insertSquidOS                          1  128000  avgt    4   1330848.204 ±   94841.345  ns/op
 * DataStructureBenchmark.insertSquidOS                          2  128000  avgt    4   1491884.433 ±   48875.128  ns/op
 * DataStructureBenchmark.insertSquidOS2                         0  128000  avgt    4    988347.954 ±   16813.452  ns/op
 * DataStructureBenchmark.insertSquidOS2                         1  128000  avgt    4   1250949.208 ±  113481.707  ns/op
 * DataStructureBenchmark.insertSquidOS2                         2  128000  avgt    4   1347611.678 ±   83986.993  ns/op
 * DataStructureBenchmark.insertSquidOS3                         0  128000  avgt    4   1041831.769 ±   39595.574  ns/op
 * DataStructureBenchmark.insertSquidOS3                         1  128000  avgt    4   1339524.051 ±  117919.416  ns/op
 * DataStructureBenchmark.insertSquidOS3                         2  128000  avgt    4   1444614.897 ±  111541.667  ns/op
 * </pre>
 * On a later run with the same benchmarks, and some hashing methods changed for SquidLib (GWT reasons):
 * <pre>
 * Benchmark                                     (NEED_TO_ENLARGE)  (SIZE)  Mode  Cnt         Score         Error  Units
 * DataStructureBenchmark.insertFastUtilFairOM                   0  128000  avgt    5   6705925.304 ±  113654.457  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM                   1  128000  avgt    5  14288300.744 ± 2667712.652  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM                   2  128000  avgt    5  16211577.329 ± 2043830.277  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM2                  0  128000  avgt    5   6541405.827 ±  177699.102  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM2                  1  128000  avgt    5  10767286.263 ± 1283398.192  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM2                  2  128000  avgt    5  12163605.365 ± 1125253.537  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM3                  0  128000  avgt    5   8141896.012 ±  607153.128  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM3                  1  128000  avgt    5  19379382.696 ± 3233350.467  ns/op
 * DataStructureBenchmark.insertFastUtilFairOM3                  2  128000  avgt    5  21694396.792 ± 3836200.683  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS                   0  128000  avgt    5   4037004.889 ±  153261.470  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS                   1  128000  avgt    5   9230176.805 ±  161981.975  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS                   2  128000  avgt    5  11035649.928 ±  333240.294  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS2                  0  128000  avgt    5   4063432.801 ±  104377.776  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS2                  1  128000  avgt    5   6970981.121 ±  980485.224  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS2                  2  128000  avgt    5   8032550.857 ±  875923.437  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS3                  0  128000  avgt    5   4904090.843 ±  240836.197  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS3                  1  128000  avgt    5  11437529.788 ± 1618202.178  ns/op
 * DataStructureBenchmark.insertFastUtilFairOS3                  2  128000  avgt    5  12767482.992 ± 1562801.822  ns/op
 * DataStructureBenchmark.insertFastUtilOM                       0  128000  avgt    5   3381742.545 ±  562157.763  ns/op
 * DataStructureBenchmark.insertFastUtilOM                       1  128000  avgt    5   8672510.524 ±  412402.460  ns/op
 * DataStructureBenchmark.insertFastUtilOM                       2  128000  avgt    5  10514389.542 ±  255879.917  ns/op
 * DataStructureBenchmark.insertFastUtilOM2                      0  128000  avgt    5   3319967.879 ±  175170.060  ns/op
 * DataStructureBenchmark.insertFastUtilOM2                      1  128000  avgt    5   6611246.488 ±  375186.211  ns/op
 * DataStructureBenchmark.insertFastUtilOM2                      2  128000  avgt    5   7706566.158 ±  337816.404  ns/op
 * DataStructureBenchmark.insertFastUtilOM3                      0  128000  avgt    5   4490331.514 ±  101387.113  ns/op
 * DataStructureBenchmark.insertFastUtilOM3                      1  128000  avgt    5  15289014.287 ± 2283136.589  ns/op
 * DataStructureBenchmark.insertFastUtilOM3                      2  128000  avgt    5  16713749.416 ±  652725.743  ns/op
 * DataStructureBenchmark.insertFastUtilOS                       0  128000  avgt    5   2235031.603 ±  106364.262  ns/op
 * DataStructureBenchmark.insertFastUtilOS                       1  128000  avgt    5   6559982.861 ±  274170.231  ns/op
 * DataStructureBenchmark.insertFastUtilOS                       2  128000  avgt    5   8152969.317 ±  136974.894  ns/op
 * DataStructureBenchmark.insertFastUtilOS2                      0  128000  avgt    5   2193276.076 ±   81610.544  ns/op
 * DataStructureBenchmark.insertFastUtilOS2                      1  128000  avgt    5   4861509.660 ±  134425.696  ns/op
 * DataStructureBenchmark.insertFastUtilOS2                      2  128000  avgt    5   5934351.176 ± 1018198.547  ns/op
 * DataStructureBenchmark.insertFastUtilOS3                      0  128000  avgt    5   2652513.131 ±   80060.454  ns/op
 * DataStructureBenchmark.insertFastUtilOS3                      1  128000  avgt    5   9021731.337 ±  242558.658  ns/op
 * DataStructureBenchmark.insertFastUtilOS3                      2  128000  avgt    5  10328402.420 ±  278577.562  ns/op
 * DataStructureBenchmark.insertGdxOM                            0  128000  avgt    5   6186504.163 ±  293769.587  ns/op
 * DataStructureBenchmark.insertGdxOM                            1  128000  avgt    5   6942276.865 ±  219811.746  ns/op
 * DataStructureBenchmark.insertGdxOM                            2  128000  avgt    5   8196028.006 ±  345940.764  ns/op
 * DataStructureBenchmark.insertGdxOM2                           0  128000  avgt    5   6047140.248 ±  156373.166  ns/op
 * DataStructureBenchmark.insertGdxOM2                           1  128000  avgt    5   6757194.567 ±  221835.100  ns/op
 * DataStructureBenchmark.insertGdxOM2                           2  128000  avgt    5   7634168.953 ±  279019.126  ns/op
 * DataStructureBenchmark.insertGdxOS                            0  128000  avgt    5   3581627.966 ±  113462.373  ns/op
 * DataStructureBenchmark.insertGdxOS                            1  128000  avgt    5   4100811.169 ±  577965.003  ns/op
 * DataStructureBenchmark.insertGdxOS                            2  128000  avgt    5   4731826.160 ±  314012.861  ns/op
 * DataStructureBenchmark.insertGdxOS2                           0  128000  avgt    5   3551511.177 ±   57673.991  ns/op
 * DataStructureBenchmark.insertGdxOS2                           1  128000  avgt    5   4071275.711 ±  631178.680  ns/op
 * DataStructureBenchmark.insertGdxOS2                           2  128000  avgt    5   4532654.707 ±  103299.524  ns/op
 * DataStructureBenchmark.insertLinkedHashMap                    0  128000  avgt    5   3102267.313 ±  149022.213  ns/op
 * DataStructureBenchmark.insertLinkedHashMap                    1  128000  avgt    5   3426531.681 ±   91328.990  ns/op
 * DataStructureBenchmark.insertLinkedHashMap                    2  128000  avgt    5   3549316.860 ±   91528.100  ns/op
 * DataStructureBenchmark.insertLinkedHashMap2                   0  128000  avgt    5   2914079.000 ±  109843.816  ns/op
 * DataStructureBenchmark.insertLinkedHashMap2                   1  128000  avgt    5   3132436.070 ±   75891.115  ns/op
 * DataStructureBenchmark.insertLinkedHashMap2                   2  128000  avgt    5   3271541.400 ±  103513.846  ns/op
 * DataStructureBenchmark.insertLinkedHashSet                    0  128000  avgt    5   3227299.188 ±  132045.398  ns/op
 * DataStructureBenchmark.insertLinkedHashSet                    1  128000  avgt    5   3513571.580 ±  191228.609  ns/op
 * DataStructureBenchmark.insertLinkedHashSet                    2  128000  avgt    5   3375616.764 ±  133747.990  ns/op
 * DataStructureBenchmark.insertLinkedHashSet2                   0  128000  avgt    5   2650941.993 ±   43071.600  ns/op
 * DataStructureBenchmark.insertLinkedHashSet2                   1  128000  avgt    5   2933219.350 ±  126534.857  ns/op
 * DataStructureBenchmark.insertLinkedHashSet2                   2  128000  avgt    5   2982516.439 ±  142959.329  ns/op
 * DataStructureBenchmark.insertSquidOM                          0  128000  avgt    5   1539787.840 ±   49715.958  ns/op
 * DataStructureBenchmark.insertSquidOM                          1  128000  avgt    5   1570655.599 ±  168480.933  ns/op
 * DataStructureBenchmark.insertSquidOM                          2  128000  avgt    5   1857391.934 ±   18273.192  ns/op
 * DataStructureBenchmark.insertSquidOM2                         0  128000  avgt    5   1539690.202 ±   39628.541  ns/op
 * DataStructureBenchmark.insertSquidOM2                         1  128000  avgt    5   1544812.432 ±  106225.938  ns/op
 * DataStructureBenchmark.insertSquidOM2                         2  128000  avgt    5   1908597.057 ±   64626.306  ns/op
 * DataStructureBenchmark.insertSquidOM3                         0  128000  avgt    5   1660065.343 ±   68357.075  ns/op
 * DataStructureBenchmark.insertSquidOM3                         1  128000  avgt    5   1612886.106 ±   73897.829  ns/op
 * DataStructureBenchmark.insertSquidOM3                         2  128000  avgt    5   1531899.892 ±   77311.983  ns/op
 * DataStructureBenchmark.insertSquidOS                          0  128000  avgt    5   1049938.293 ±   43853.053  ns/op
 * DataStructureBenchmark.insertSquidOS                          1  128000  avgt    5   1446225.773 ±   49375.565  ns/op
 * DataStructureBenchmark.insertSquidOS                          2  128000  avgt    5   1583098.974 ±   46981.194  ns/op
 * DataStructureBenchmark.insertSquidOS2                         0  128000  avgt    5   1045367.020 ±   20296.955  ns/op
 * DataStructureBenchmark.insertSquidOS2                         1  128000  avgt    5   1331613.776 ±   40376.117  ns/op
 * DataStructureBenchmark.insertSquidOS2                         2  128000  avgt    5   1449129.470 ±   48967.631  ns/op
 * DataStructureBenchmark.insertSquidOS3                         0  128000  avgt    5   1112148.439 ±   56331.458  ns/op
 * DataStructureBenchmark.insertSquidOS3                         1  128000  avgt    5   1424965.573 ±   84169.386  ns/op
 * DataStructureBenchmark.insertSquidOS3                         2  128000  avgt    5   1543401.423 ±   30843.084  ns/op
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 6)
@Measurement(iterations = 4)
public class DataStructureBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        @Param({/* "2000", "4000", "8000", */"128000"})
        public int SIZE;
        @Param({"0", "1", "2"})
        public int NEED_TO_ENLARGE;
//        public LinkedHashMap<Integer, Integer> lhm;
//        public OrderedMap<Integer, Integer> squidOM;
//        public com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM;
//        public LinkedHashSet<Integer> lhs;
//        public OrderedSet<Integer> squidOS;
//        public com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS;
//        @Setup(Level.Trial)
//        public void setup() {
//            lhm = new LinkedHashMap<>();
//            squidOM = new OrderedMap<>();
//            gdxOM = new com.badlogic.gdx.utils.OrderedMap<>();
//            lhs = new LinkedHashSet<>();
//            squidOS = new OrderedSet<>();
//            gdxOS = new com.badlogic.gdx.utils.OrderedSet<>();
//        }
    }

    public static final CrossHash.IHasher integerHasher = new CrossHash.IHasher() {
        public int hash(Object data) { return (int)data; }
        public boolean areEqual(Object left, Object right) { return Objects.equals(left, right); }
    };

    public static final Hash.Strategy<Integer> integerStrategy = new Hash.Strategy<Integer>() {
        @Override
        public int hashCode(Integer data) {
            return data;
        }

        @Override
        public boolean equals(Integer a, Integer b) {
            return a.equals(b);
        }
    };

    @Benchmark
    public void insertLinkedHashMap(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashMap<Integer, Integer> lhm = new LinkedHashMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            lhm.put(i, i);
        }
        blackhole.consume(lhm);
    }

    @Benchmark
    public void insertLinkedHashMap2(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashMap<Integer, Integer> lhm = new LinkedHashMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            lhm.put(i, i);
        }
        blackhole.consume(lhm);
    }

    //@Benchmark
    public void insertLinkedHashMap3(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashMap<Integer, Integer> lhm = new LinkedHashMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            lhm.put(i, i);
        }
        blackhole.consume(lhm);
    }
    @Benchmark
    public void insertLinkedHashSet(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            lhs.add(i);
        }
        blackhole.consume(lhs);
    }

    @Benchmark
    public void insertLinkedHashSet2(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            lhs.add(i);
        }
        blackhole.consume(lhs);
    }

    //@Benchmark
    public void insertLinkedHashSet3(BenchmarkState state, Blackhole blackhole)
    {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            lhs.add(i);
        }
        blackhole.consume(lhs);
    }

    @Benchmark
    public void insertGdxOM(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM = new com.badlogic.gdx.utils.OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE,0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOM.put(i, i);
        }
        blackhole.consume(gdxOM);
    }

    @Benchmark
    public void insertGdxOM2(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM = new com.badlogic.gdx.utils.OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOM.put(i, i);
        }
        blackhole.consume(gdxOM);
    }

    //@Benchmark
    public void insertGdxOM3(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedMap<Integer, Integer> gdxOM = new com.badlogic.gdx.utils.OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOM.put(i, i);
        }
        blackhole.consume(gdxOM);
    }
    @Benchmark
    public void insertGdxOS(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS = new com.badlogic.gdx.utils.OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOS.add(i);
        }
        blackhole.consume(gdxOS);
    }
    @Benchmark
    public void insertGdxOS2(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS = new com.badlogic.gdx.utils.OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOS.add(i);
        }
        blackhole.consume(gdxOS);
    }
    //@Benchmark
    public void insertGdxOS3(BenchmarkState state, Blackhole blackhole)
    {
        com.badlogic.gdx.utils.OrderedSet<Integer> gdxOS = new com.badlogic.gdx.utils.OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            gdxOS.add(i);
        }
        blackhole.consume(gdxOS);
    }

    @Benchmark
    public void insertSquidOM(BenchmarkState state, Blackhole blackhole)
    {
        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f, integerHasher);
        for (int i = 0; i < state.SIZE; i++) {
            squidOM.put(i, i);
        }
        blackhole.consume(squidOM);
    }

    @Benchmark
    public void insertSquidOM2(BenchmarkState state, Blackhole blackhole)
    {
        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f, integerHasher);
        for (int i = 0; i < state.SIZE; i++) {
            squidOM.put(i, i);
        }
        blackhole.consume(squidOM);
    }

    @Benchmark
    public void insertSquidOM3(BenchmarkState state, Blackhole blackhole)
    {
        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f, integerHasher);
        for (int i = 0; i < state.SIZE; i++) {
            squidOM.put(i, i);
        }
        blackhole.consume(squidOM);
    }

    @Benchmark
    public void insertSquidOS(BenchmarkState state, Blackhole blackhole)
    {
        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f, integerHasher);
        for (int i = 0; i < state.SIZE; i++) {
            squidOS.add(i);
        }
        blackhole.consume(squidOS);
    }
    @Benchmark
    public void insertSquidOS2(BenchmarkState state, Blackhole blackhole)
    {
        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f, integerHasher);
        for (int i = 0; i < state.SIZE; i++) {
            squidOS.add(i);
        }
        blackhole.consume(squidOS);
    }
    @Benchmark
    public void insertSquidOS3(BenchmarkState state, Blackhole blackhole)
    {
        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f, integerHasher);
        for (int i = 0; i < state.SIZE; i++) {
            squidOS.add(i);
        }
        blackhole.consume(squidOS);
    }

//    @Benchmark
//    public void insertSquidCustomOM(BenchmarkState state, Blackhole blackhole)
//    {
//        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>( state.SIZE >> state.NEED_TO_ENLARGE, 0.75f, integerHasher);
//        for (int i = 0; i < state.SIZE; i++) {
//            squidOM.put(i, i);
//        }
//        blackhole.consume(squidOM);
//    }
//
//    @Benchmark
//    public void insertSquidCustomOM2(BenchmarkState state, Blackhole blackhole)
//    {
//        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f, integerHasher);
//        for (int i = 0; i < state.SIZE; i++) {
//            squidOM.put(i, i);
//        }
//        blackhole.consume(squidOM);
//    }
//
//    @Benchmark
//    public void insertSquidCustomOM3(BenchmarkState state, Blackhole blackhole)
//    {
//        OrderedMap<Integer, Integer> squidOM = new OrderedMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f, integerHasher);
//        for (int i = 0; i < state.SIZE; i++) {
//            squidOM.put(i, i);
//        }
//        blackhole.consume(squidOM);
//    }
//
//    @Benchmark
//    public void insertSquidCustomOS(BenchmarkState state, Blackhole blackhole)
//    {
//        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f, integerHasher);
//        for (int i = 0; i < state.SIZE; i++) {
//            squidOS.add(i);
//        }
//        blackhole.consume(squidOS);
//    }
//    @Benchmark
//    public void insertSquidCustomOS2(BenchmarkState state, Blackhole blackhole)
//    {
//        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f, integerHasher);
//        for (int i = 0; i < state.SIZE; i++) {
//            squidOS.add(i);
//        }
//        blackhole.consume(squidOS);
//    }
//    @Benchmark
//    public void insertSquidCustomOS3(BenchmarkState state, Blackhole blackhole)
//    {
//        OrderedSet<Integer> squidOS = new OrderedSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f, integerHasher);
//        for (int i = 0; i < state.SIZE; i++) {
//            squidOS.add(i);
//        }
//        blackhole.consume(squidOS);
//    }

    @Benchmark
    public void insertFastUtilOM(BenchmarkState state, Blackhole blackhole)
    {
        Int2IntLinkedOpenHashMap fastutilOM = new Int2IntLinkedOpenHashMap(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOM.put(i, i);
        }
        blackhole.consume(fastutilOM);
    }

    @Benchmark
    public void insertFastUtilOM2(BenchmarkState state, Blackhole blackhole)
    {
        Int2IntLinkedOpenHashMap fastutilOM = new Int2IntLinkedOpenHashMap(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOM.put(i, i);
        }
        blackhole.consume(fastutilOM);
    }

    @Benchmark
    public void insertFastUtilOM3(BenchmarkState state, Blackhole blackhole)
    {
        Int2IntLinkedOpenHashMap fastutilOM = new Int2IntLinkedOpenHashMap(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOM.put(i, i);
        }
        blackhole.consume(fastutilOM);
    }

    @Benchmark
    public void insertFastUtilOS(BenchmarkState state, Blackhole blackhole)
    {
        IntLinkedOpenHashSet fastutilOS = new IntLinkedOpenHashSet(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOS.add(i);
        }
        blackhole.consume(fastutilOS);
    }
    @Benchmark
    public void insertFastUtilOS2(BenchmarkState state, Blackhole blackhole)
    {
        IntLinkedOpenHashSet fastutilOS = new IntLinkedOpenHashSet(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOS.add(i);
        }
        blackhole.consume(fastutilOS);
    }
    @Benchmark
    public void insertFastUtilOS3(BenchmarkState state, Blackhole blackhole)
    {
        IntLinkedOpenHashSet fastutilOS = new IntLinkedOpenHashSet(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOS.add(i);
        }
        blackhole.consume(fastutilOS);
    }

    @Benchmark
    public void insertFastUtilFairOM(BenchmarkState state, Blackhole blackhole)
    {
        Object2ObjectLinkedOpenCustomHashMap<Integer, Integer> fastutilOM = new Object2ObjectLinkedOpenCustomHashMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f, integerStrategy);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOM.put(i, i);
        }
        blackhole.consume(fastutilOM);
    }

    @Benchmark
    public void insertFastUtilFairOM2(BenchmarkState state, Blackhole blackhole)
    {
        Object2ObjectLinkedOpenCustomHashMap<Integer, Integer> fastutilOM = new Object2ObjectLinkedOpenCustomHashMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f, integerStrategy);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOM.put(i, i);
        }
        blackhole.consume(fastutilOM);
    }

    @Benchmark
    public void insertFastUtilFairOM3(BenchmarkState state, Blackhole blackhole)
    {
        Object2ObjectLinkedOpenCustomHashMap<Integer, Integer> fastutilOM = new Object2ObjectLinkedOpenCustomHashMap<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f, integerStrategy);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOM.put(i, i);
        }
        blackhole.consume(fastutilOM);
    }

    @Benchmark
    public void insertFastUtilFairOS(BenchmarkState state, Blackhole blackhole)
    {
        ObjectLinkedOpenCustomHashSet<Integer> fastutilOS = new ObjectLinkedOpenCustomHashSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.75f, integerStrategy);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOS.add(i);
        }
        blackhole.consume(fastutilOS);
    }

    @Benchmark
    public void insertFastUtilFairOS2(BenchmarkState state, Blackhole blackhole)
    {
        ObjectLinkedOpenCustomHashSet<Integer> fastutilOS = new ObjectLinkedOpenCustomHashSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.5f, integerStrategy);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOS.add(i);
        }
        blackhole.consume(fastutilOS);
    }

    @Benchmark
    public void insertFastUtilFairOS3(BenchmarkState state, Blackhole blackhole)
    {
        ObjectLinkedOpenCustomHashSet<Integer> fastutilOS = new ObjectLinkedOpenCustomHashSet<>(state.SIZE >> state.NEED_TO_ENLARGE, 0.25f, integerStrategy);
        for (int i = 0; i < state.SIZE; i++) {
            fastutilOS.add(i);
        }
        blackhole.consume(fastutilOS);
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
     *    $ java -jar target/benchmarks.jar DataStructureBenchmark -wi 5 -i 5 -f 1 -gc true
     *
     *    (we requested 3 warmup/measurement iterations, single fork, garbage collect between benchmarks)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DataStructureBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .shouldDoGC(true)
                .build();
        new Runner(opt).run();
    }
}
