/*
The MIT License(MIT)
Copyright(c) mxgmn 2016.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

The software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the software.
*/
package squidpony.squidgrid;

import squidpony.GwtCompatibility;
import squidpony.squidgrid.mapping.AestheticDifference;
import squidpony.squidmath.IntDoubleOrderedMap;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

/**
 * Similar to MimicFill, this class can be used to imitate the style of an existing piece of data, but this works on
 * more than just booleans; it can produce similar styles of texture (its original use in SynTex), of map, of item
 * placement, and so on by specifying a different technique for differentiating two int values.
 * <br>
 * Two options are now available; the process() method allows the slow analyze() step to be computed once,
 * before the rest of processing is potentially done many times, but the new neoProcess() method produces
 * comparable or better results and is drastically faster even without needing analysis. This means
 * neoProcess(), based on P.F. Harrison's algorithm in SynTex, is strongly recommended now.
 * <br>
 * Example results of neoProcess(), with a procedural dungeon first and the mimic versions after it:
 * https://gist.github.com/tommyettinger/405336fb0fc74838d806c021aabe77da (you may need to click Raw and zoom
 * out somewhat for it to render well).
 * <br>
 * Ported from https://github.com/mxgmn/SynTex by Tommy Ettinger on 6/9/2016.
 */
public class DetailedMimic {

    private DetailedMimic()
    {
    }

    public DetailedMimic(AestheticDifference diff)
    {
        this(diff, new RNG());
    }
    public DetailedMimic(AestheticDifference diff, RNG rng)
    {
        random = rng;
        difference = diff;
        analyzed = null;
    }

    public void analyze(int[] sample, int width, int height, int detailLevel, int proximity, boolean discrete)
    {
        if(detailLevel > 0)
            analysis(sample, width, height, detailLevel, proximity, discrete);
    }

    public int[] process(int[] sample, int sampleWidth, int sampleHeight, int targetWidth, int targetHeight,
                         int detailLevel, int proximity, double temperature, boolean discrete)
    {
        if(detailLevel > 0)
        {
            if(analyzed == null || analyzed.length != sampleWidth || analyzed.length == 0 ||
                    analyzed[0].length != sampleHeight)
                analyze(sample, sampleWidth, sampleHeight, detailLevel, proximity, discrete);
            return coherentSynthesis(sample, sampleWidth, sampleHeight, analyzed, proximity,
                    targetWidth, targetHeight, temperature, discrete);
        }
        else {
            return fullSynthesis(sample, sampleWidth, sampleHeight, proximity,
                    targetWidth, targetHeight, temperature, discrete);
        }
    }

    public int[] neoProcess(int[] sample, int sampleWidth, int sampleHeight, int targetWidth, int targetHeight,
                         int detailLevel, int proximity, boolean discrete)
    {
            return reSynthesis(sample, sampleWidth, sampleHeight, proximity, 20,
                    Math.max(1, detailLevel), discrete, targetWidth, targetHeight);
    }

    public RNG random;
    public AestheticDifference difference;
    private int[][] analyzed;

    private void analysis(int[] bitmap, int width, int height, int K, int N, boolean indexed)
    {
        int area = width * height;
        analyzed = new int[area][];
        OrderedSet<Integer> points = new OrderedSet<>(area);
        for (int i = 0; i < area; i++) points.add(i);

        double[] similarities = new double[area * area];
        for (int i = 0; i < area; i++) for (int j = 0; j < area; j++)
            similarities[i * area + j] = similarities[j * area + i] != 0 ? similarities[j * area + i] :
                    similarity(i, bitmap, width, height, j, bitmap, width, height, N, null, indexed);

        for (int i = 0; i < area; i++)
        {
            analyzed[i] = new int[K];
            OrderedSet<Integer> copy = new OrderedSet<>(points);

            analyzed[i][0] = i;
            copy.remove(i);

            for (int k = 1; k < K; k++)
            {
                double max = -10000;
                int argmax = -1;

                for(Integer p : copy)
                {
                    double s = similarities[i * area + p];
                    if (s > max)
                    {
                        max = s;
                        argmax = p;
                    }
                }

                analyzed[i][k] = argmax;
                copy.remove(argmax);
            }
        }
    }

    private int[] coherentSynthesis(int[] sample, int SW, int SH, int[][] sets, int N, int OW, int OH, double t, boolean indexed)
    {
        int[] result = new int[OW * OH];
        Integer[] origins = new Integer[OW * OH];
        boolean[][] mask = new boolean[SW][SH], cleanMask = new boolean[SW][SH];

        for (int i = 0; i < OW * OH; i++)
        {
            int x = i % OW, y = i / OW;
            IntDoubleOrderedMap candidates = new IntDoubleOrderedMap();
            GwtCompatibility.insert2D(cleanMask, mask, 0, 0);

            for (int dy = -1; dy <= 1; dy++){
                for (int dx = -1; dx <= 1; dx++)
                {
                    int sx = (x + dx + OW) % OW, sy = (y + dy + OH) % OH;
                    Integer origin = origins[sy * OW + sx];
                    if ((dx != 0 || dy != 0) && origin != null)
                    {
                        for (int pi = 0, p; pi < sets[origin].length; pi++)
                        {
                            p = sets[origin][pi];
                            int ox = (p % SW - dx + SW) % SW, oy = (p / SW - dy + SH) % SH;
                            double s = similarity(oy * SW + ox, sample, SW, SH, i, result, OW, OH, N, origins, indexed);

                            if (!mask[ox][oy]) candidates.put(ox + oy * SW, Math.pow(100, s / t));
                            mask[ox][oy] = true;
                        }
                    }
                }
            }

            int shifted = candidates.isEmpty() ? random.nextInt(SW) + random.nextInt(SH) * SW : weightedRandom(candidates, random.nextDouble());
            origins[i] = shifted;
            result[i] = sample[shifted];
        }

        return result;
    }

    private int[] fullSynthesis(int[] sample, int SW, int SH, int N, int OW, int OH, double t, boolean indexed)
    {
        int[] result = new int[OW * OH];
        Integer[] origins = new Integer[OW * OH];

        if (!indexed) for (int y = 0; y < OH; y++) {
            for (int x = 0; x < OW; x++){
                if (y + N >= OH)
                {
                    result[x + y * OW] = sample[random.nextInt(SW * SH)];
                    origins[x + y * OW] = -1;
                }
            }
        }

        for (int i = 0; i < OW * OH; i++)
        {
            double[] candidates = new double[SW * SH];
            double max = -10000;
            int argmax = -1;

            for (int j = 0; j < SW * SH; j++)
            {
                double s = similarity(j, sample, SW, SH, i, result, OW, OH, N, origins, indexed);
                if (s > max)
                {
                    max = s;
                    argmax = j;
                }

                if (indexed) candidates[j] = Math.pow(100.0, s / t);
            }

            if (indexed) argmax = weightedRandom(candidates, random.nextDouble());
            result[i] = sample[argmax];
            origins[i] = -1;
        }

        return result;
    }


    private int[] reSynthesis(int[] sample, int SW, int SH, int N, int M, int polish, boolean indexed, int OW, int OH)
    {
        IntVLA colors = new IntVLA();
        int[] indexedSample = new int[Math.min(SW * SH, sample.length)];
        for (int j = 0; j < indexedSample.length; j++)
        {
            int color = sample[j];

            int i = 0;
            for (int cn = 0; cn < colors.size; cn++)
            {
                if(colors.get(cn) == color) break;
                i++;
            }

            if (i == colors.size) colors.add(color);
            indexedSample[j] = i;
        }

        int colorsNumber = colors.size;

        double[][] colorMetric = null;
        if (!indexed && colorsNumber <= 1024)
        {
            colorMetric = new double[colorsNumber][colorsNumber];
            for (int x = 0; x < colorsNumber; x++)
            {
                for (int y = 0; y < colorsNumber; y++)
                {
                    int cx = colors.get(x), cy = colors.get(y);
                    colorMetric[x][y] = difference.difference(cx, cy);
                }
            }
        }

        int[] origins = new int[OW * OH];
        for (int i = 0; i < origins.length; i++) origins[i] = -1;

        int[] shuffle = new int[OW * OH];
        for (int i = 0; i < shuffle.length; i++)
        {
            int j = random.nextIntHasty(i + 1);
            if (j != i) shuffle[i] = shuffle[j];
            shuffle[j] = i;
        }

        for (int round = 0; round <= polish; round++) for (int counter = 0; counter < shuffle.length; counter++)
        {
            int f = shuffle[counter];
            int fx = f % OW, fy = f / OW;
            int neighborsNumber = round > 0 ? 8 : Math.min(8, counter);
            int neighborsFound = 0;

            int[] candidates = new int[neighborsNumber + M];

            if (neighborsNumber > 0)
            {
                int[] neighbors = new int[neighborsNumber];
                int[] x = new int[4], y = new int[4];

                for (int radius = 1; neighborsFound < neighborsNumber; radius++)
                {
                    x[0] = fx - radius;
                    y[0] = fy - radius;
                    x[1] = fx - radius;
                    y[1] = fy + radius;
                    x[2] = fx + radius;
                    y[2] = fy + radius;
                    x[3] = fx + radius;
                    y[3] = fy - radius;

                    for (int k = 0; k < 2 * radius; k++)
                    {
                        for (int d = 0; d < 4; d++)
                        {
                            x[d] = (x[d] + 10 * OW) % OW;
                            y[d] = (y[d] + 10 * OH) % OH;

                            if (neighborsFound >= neighborsNumber) continue;
                            int point = x[d] + y[d] * OW;
                            if (origins[point] != -1)
                            {
                                neighbors[neighborsFound] = point;
                                neighborsFound++;
                            }
                        }

                        y[0]++;
                        x[1]++;
                        y[2]--;
                        x[3]--;
                    }
                }


                for (int n = 0; n < neighborsNumber; n++)
                {
                    int cx = (origins[neighbors[n]] + (f - neighbors[n]) % OW + 100 * SW) % SW;
                    int cy = (origins[neighbors[n]] / SW + f / OW - neighbors[n] / OW + 100 * SH) % SH;
                    candidates[n] = cx + cy * SW;
                }
            }

            for (int m = 0; m < M; m++) candidates[neighborsNumber + m] = random.nextIntHasty(SW * SH);

            double max = -1E+10;
            int argmax = -1;

            for (int c = 0; c < candidates.length; c++)
            {
                double sum = random.nextDouble(0.000001);
                int ix = candidates[c] % SW, iy = candidates[c] / SW, jx = f % OW, jy = f / OW;
                int SX, SY, FX, FY, S, F;
                int origin;

                for (int dy = -N; dy <= N; dy++)
                {
                    for (int dx = -N; dx <= N; dx++)
                    {
                        if (dx != 0 || dy != 0)
                        {
                            SX = ix + dx;
                            if (SX < 0) SX += SW;
                            else if (SX >= SW) SX -= SW;

                            SY = iy + dy;
                            if (SY < 0) SY += SH;
                            else if (SY >= SH) SY -= SH;

                            FX = jx + dx;
                            if (FX < 0) FX += OW;
                            else if (FX >= OW) FX -= OW;

                            FY = jy + dy;
                            if (FY < 0) FY += OH;
                            else if (FY >= OH) FY -= OH;

                            S = SX + SY * SW;
                            F = FX + FY * OW;

                            origin = origins[F];
                            if (origin != -1)
                            {
                                if (indexed) sum += sample[origin] == sample[S] ? 1 : -1;
                                else if (colorMetric != null) sum += colorMetric[indexedSample[origin]][indexedSample[S]];
                                else sum += difference.difference(sample[origin], sample[S]);
                            }
                        }
                    }
                }

                if (sum >= max)
                {
                    max = sum;
                    argmax = candidates[c];
                }
            }

            origins[f] = argmax;
        }

        int[] result = new int[OW * OH];
        for (int i = 0; i < result.length; i++) result[i] = sample[origins[i]];
        return result;
    }

    private double similarity(int i1, int[] b1, int w1, int h1, int i2, int[] b2, int w2, int h2, int N, Integer[] origins, boolean indexed)
    {
        double sum = 0;
        int x1 = i1 % w1, y1 = i1 / w1, x2 = i2 % w2, y2 = i2 / w2;

        for (int dy = -N; dy <= 0; dy++) for (int dx = -N; (dy < 0 && dx <= N) || (dy == 0 && dx < 0); dx++)
        {
            int sx1 = (x1 + dx + w1) % w1, sy1 = (y1 + dy + h1) % h1;
            int sx2 = (x2 + dx + w2) % w2, sy2 = (y2 + dy + h2) % h2;

            int c1 = b1[sx1 + sy1 * w1];
            int c2 = b2[sx2 + sy2 * w2];

            if (origins == null || origins[sy2 * w2 + sx2] != null)
            {
                if (indexed)
                    sum += c1 == c2 ? 1 : -1;
                else
                    sum -= difference.difference(c1, c2);
                    /*
                    Color C1 = Color.FromArgb(c1), C2 = Color.FromArgb(c2);
                    sum -= (double)((C1.R - C2.R) * (C1.R - C2.R) + (C1.G - C2.G) * (C1.G - C2.G) + (C1.B - C2.B) * (C1.B - C2.B)) / 65536.0;
                    */
            }
        }

        return sum;
    }

    private static int weightedRandom(double[] array, double r)
    {
        double sum = 0;
        for (int j = 0; j < array.length; j++)
            sum += Math.max(1.0, array[j]);

        for (int j = 0; j < array.length; j++)
            array[j] /= sum;

        int i = 0;
        double x = 0;

        while (i < array.length)
        {
            x += array[i];
            if (r <= x) return i;
            i++;
        }

        return 0;
    }

    private static int weightedRandom(IntDoubleOrderedMap dic, double r) {
        int[] ints = new int[dic.size()];
        double[] doubles = new double[dic.size()];
        dic.keySet().toArray(ints);
        dic.values().toArray(doubles);
        return ints[weightedRandom(doubles, r)];
    }
}