/*
The MIT License(MIT)
Copyright(c) mxgmn 2016, modified by Tommy Ettinger 2018
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
The software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the software.
*/

package squidpony.squidgrid;

import squidpony.squidmath.*;

/**
 * A port of WaveFunctionCollapse by ExUtumno/mxgmn; takes a single sample of a grid to imitate and produces one or more
 * grids of requested sizes that have a similar layout of cells to the sample. Samples are given as {@code int[][]}
 * where an int is usually an index into an array, list, {@link squidpony.squidmath.Arrangement}, or some similar
 * indexed collection of items (such as char values or colors) that would be used instead of an int directly. The
 * original WaveFunctionCollapse code, <a href="https://github.com/mxgmn/WaveFunctionCollapse">here</a>, used colors in
 * bitmap images, but this uses 2D int arrays that can stand as substitutes for colors or chars.
 * <br>
 * Created by Tommy Ettinger on 3/28/2018. Port of https://github.com/mxgmn/WaveFunctionCollapse
 */
public class MimicWFC {
    private boolean[][] wave;

    private int[][][] propagator;
    private int[][][] compatible;
    private int[] observed;

    private int[] stack;
    private int stacksize;

    public IRNG random;
    private int FMX, FMY, totalOptions;
    private boolean periodic;

    private double[] baseWeights;
    private double[] weightLogWeights;

    private int[] sumsOfOnes;
    private double sumOfWeights, sumOfWeightLogWeights, startingEntropy;
    private double[] sumsOfWeights, sumsOfWeightLogWeights, entropies;


    private int order;
    private int[][] patterns;
    private Arrangement<Integer> choices;
    private int ground;

    public MimicWFC(int[][] itemGrid, int order, int width, int height, boolean periodicInput, boolean periodicOutput, int symmetry, int ground)
    {
        FMX = width;
        FMY = height;

        this.order = order;
        periodic = periodicOutput;

        int SMX = itemGrid.length, SMY = itemGrid[0].length;
        //colors = new List<Color>();
        choices = new Arrangement<>(SMX * SMY);
        int[][] sample = new int[SMX][SMY];
        for (int y = 0; y < SMY; y++) {
            for (int x = 0; x < SMX; x++)
            {
                int color = itemGrid[x][y];
                int i = choices.addOrIndex(color);
                sample[x][y] = i;
            }
        }

        int C = choices.size();
        long W = MathExtras.raiseToPower(C, order * order);



//        Dictionary<long, int> weights = new Dictionary<long, int>();
//        List<long> ordering = new List<long>();
        OrderedMap<int[], Integer> weights = new OrderedMap<>(CrossHash.intHasher);

        for (int y = 0; y < (periodicInput ? SMY : SMY - order + 1); y++) {
            for (int x = 0; x < (periodicInput ? SMX : SMX - order + 1); x++) {
                int[][] ps = new int[8][];

                ps[0] = patternFromSample(x, y, sample, SMX, SMY);
                ps[1] = reflect(ps[0]);
                ps[2] = rotate(ps[0]);
                ps[3] = reflect(ps[2]);
                ps[4] = rotate(ps[2]);
                ps[5] = reflect(ps[4]);
                ps[6] = rotate(ps[4]);
                ps[7] = reflect(ps[6]);

                for (int k = 0; k < symmetry; k++) {
                    int[] ind = ps[k];
                    Integer wt = weights.get(ind);
                    if (wt != null) weights.put(ind, wt + 1);
                    else {
                        weights.put(ind, 1);
                    }
                }
            }
        }

        totalOptions = weights.size();
        this.ground = (ground + totalOptions) % totalOptions;
        patterns = new int[totalOptions][];
        baseWeights = new double[totalOptions];

        for (int w = 0; w < totalOptions; w++) {
            patterns[w] = weights.keyAt(w);
            baseWeights[w] = weights.getAt(w);
        }
        

        propagator = new int[4][][];
        IntVLA list = new IntVLA(totalOptions);
        for (int d = 0; d < 4; d++)
        {
            propagator[d] = new int[totalOptions][];
            for (int t = 0; t < totalOptions; t++)
            {
                list.clear();
                for (int t2 = 0; t2 < totalOptions; t2++) if (agrees(patterns[t], patterns[t2], DX[d], DY[d])) list.add(t2);
                propagator[d][t] = list.toArray();
            }
        }
    }

    private long index(byte[] p, long C)
    {
        long result = 0, power = 1;
        for (int i = 0; i < p.length; i++)
        {
            result += p[p.length - 1 - i] * power;
            power *= C;
        }
        return result;
    }

    private byte[] patternFromIndex(long ind, long power, long C)
    {
        long residue = ind;
        byte[] result = new byte[order * order];

        for (int i = 0; i < result.length; i++)
        {
            power /= C;
            int count = 0;

            while (residue >= power)
            {
                residue -= power;
                count++;
            }

            result[i] = (byte)count;
        }

        return result;
    }

//    int[] pattern (Func<int, int, byte> f)
//    {
//        int[] result = new int[order * order];
//        for (int y = 0; y < order; y++) {
//            for (int x = 0; x < order; x++){
//                result[x + y * order] = f(x, y);
//            }
//        }
//        return result;
//    }

    private int[] patternFromSample(int x, int y, int[][] sample, int SMX, int SMY) {
        int[] result = new int[order * order];
        for (int dy = 0; dy < order; dy++) {
            for (int dx = 0; dx < order; dx++) {
                result[dx + dy * order] = sample[(x + dx) % SMX][(y + dy) % SMY];
            }
        }
        return result;
    }
    private int[] rotate(int[] p)
    {
        int[] result = new int[order * order];
        for (int y = 0; y < order; y++) {
            for (int x = 0; x < order; x++){
                result[x + y * order] = p[order - 1 - y + x * order];
            }
        }
        return result;
    }
    private int[] reflect(int[] p)
    {
        int[] result = new int[order * order];
        for (int y = 0; y < order; y++) {
            for (int x = 0; x < order; x++){
                result[x + y * order] = p[order - 1 - x + y * order];
            }
        }
        return result;
    }
    private boolean agrees(int[] p1, int[] p2, int dx, int dy)
    {
        int xmin = dx < 0 ? 0 : dx, xmax = dx < 0 ? dx + order : order,
                ymin = dy < 0 ? 0 : dy, ymax = dy < 0 ? dy + order : order;
        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                if (p1[x + order * y] != p2[x - dx + order * (y - dy)])
                    return false;
            }
        }
        return true;
    }

    private void init()
    {
        wave = new boolean[FMX * FMY][];
        compatible = new int[wave.length][][];
        for (int i = 0; i < wave.length; i++)
        {
            wave[i] = new boolean[totalOptions];
            compatible[i] = new int[totalOptions][];
            for (int t = 0; t < totalOptions; t++) compatible[i][t] = new int[4];
        }

        weightLogWeights = new double[totalOptions];
        sumOfWeights = 0;
        sumOfWeightLogWeights = 0;

        for (int t = 0; t < totalOptions; t++)
        {
            weightLogWeights[t] = baseWeights[t] * Math.log(baseWeights[t]);
            sumOfWeights += baseWeights[t];
            sumOfWeightLogWeights += weightLogWeights[t];
        }

        startingEntropy = Math.log(sumOfWeights) - sumOfWeightLogWeights / sumOfWeights;

        sumsOfOnes = new int[FMX * FMY];
        sumsOfWeights = new double[FMX * FMY];
        sumsOfWeightLogWeights = new double[FMX * FMY];
        entropies = new double[FMX * FMY];

        stack = new int[wave.length * totalOptions << 1];
        stacksize = 0;
    }

    private Boolean observe()
    {
        double min = 1E+3;
        int argmin = -1;

        for (int i = 0; i < wave.length; i++)
        {
            if (onBoundary(i % FMX, i / FMX)) continue;

            int amount = sumsOfOnes[i];
            if (amount == 0) return false;

            double entropy = entropies[i];
            if (amount > 1 && entropy <= min)
            {
                double noise = 1E-6 * random.nextDouble();
                if (entropy + noise < min)
                {
                    min = entropy + noise;
                    argmin = i;
                }
            }
        }

        if (argmin == -1)
        {
            observed = new int[FMX * FMY];
            for (int i = 0; i < wave.length; i++) {
                for (int t = 0; t < totalOptions; t++) {
                    if (wave[i][t]) { 
                        observed[i] = t;
                        break;
                    }
                }
            }
            return true;
        }

        double[] distribution = new double[totalOptions];
        double sum = 0.0, x = 0.0;
        for (int t = 0; t < totalOptions; t++)
        {
            sum += (distribution[t] = wave[argmin][t] ? baseWeights[t] : 0);
        }
        int r = 0;
        sum = random.nextDouble(sum);
        for (; r < totalOptions; r++) {
            if((x += distribution[r]) > sum)
                break;
        }

        boolean[] w = wave[argmin];
        for (int t = 0; t < totalOptions; t++){
            if (w[t] != (t == r))
                ban(argmin, t);
        }

        return null;
    }

    private void propagate()
    {
        while (stacksize > 0)
        {
            int i1 = stack[stacksize - 2], e2 = stack[stacksize - 1];
            stacksize -= 2;
            int x1 = i1 % FMX, y1 = i1 / FMX;

            for (int d = 0; d < 4; d++)
            {
                int dx = DX[d], dy = DY[d];
                int x2 = x1 + dx, y2 = y1 + dy;
                if (onBoundary(x2, y2)) continue;

                if (x2 < 0) x2 += FMX;
                else if (x2 >= FMX) x2 -= FMX;
                if (y2 < 0) y2 += FMY;
                else if (y2 >= FMY) y2 -= FMY;

                int i2 = x2 + y2 * FMX;
                int[] p = propagator[d][e2];
                int[][] compat = compatible[i2];

                for (int l = 0; l < p.length; l++)
                {
                    int t2 = p[l];
                    int[] comp = compat[t2];

                    comp[d]--;
                    if (comp[d] == 0) ban(i2, t2);
                }
            }
        }
    }

    public boolean run(long seed, int limit)
    {
        if (wave == null) init();

        clear();
        random = new RNG(seed);

        for (int l = 0; l < limit || limit == 0; l++)
        {
            Boolean result = observe();
            if (result != null) return result;
            propagate();
        }

        return true;
    }

    public boolean run(IRNG rng, int limit)
    {
        if (wave == null) init();

        clear();
        random = rng;

        for (int l = 0; l < limit || limit == 0; l++)
        {
            Boolean result = observe();
            if (result != null) return result;
            propagate();
        }

        return true;
    }

    private void ban(int i, int t)
    {
        wave[i][t] = false;

        int[] comp = compatible[i][t];
        for (int d = 0; d < 4; d++) comp[d] = 0;
        stack[stacksize++] = i;
        stack[stacksize++] = t;

        double sum = sumsOfWeights[i];
        entropies[i] += sumsOfWeightLogWeights[i] / sum - Math.log(sum);

        sumsOfOnes[i] -= 1;
        sumsOfWeights[i] -= baseWeights[t];
        sumsOfWeightLogWeights[i] -= weightLogWeights[t];

        sum = sumsOfWeights[i];
        entropies[i] -= sumsOfWeightLogWeights[i] / sum - Math.log(sum);
    }


    private boolean onBoundary(int x, int y) {
        return !periodic && (x + order > FMX || y + order > FMY || x < 0 || y < 0);
    }

    public int[][] result()
    {
        int[][] result = new int[FMX][FMY];

        if (observed != null)
        {
            for (int y = 0; y < FMY; y++)
            {
                int dy = y < FMY - order + 1 ? 0 : order - 1;
                for (int x = 0; x < FMX; x++)
                {
                    int dx = x < FMX - order + 1 ? 0 : order - 1;
                    result[x][y] = choices.keyAt(patterns[observed[x - dx + (y - dy) * FMX]][dx + dy * order]);
                }
            }
        }
        return result;
    }

    private void clear()
    {
        for (int i = 0; i < wave.length; i++)
        {
            for (int t = 0; t < totalOptions; t++)
            {
                wave[i][t] = true;
                for (int d = 0; d < 4; d++) compatible[i][t][d] = propagator[OPPOSITE[d]][t].length;
            }

            sumsOfOnes[i] = baseWeights.length;
            sumsOfWeights[i] = sumOfWeights;
            sumsOfWeightLogWeights[i] = sumOfWeightLogWeights;
            entropies[i] = startingEntropy;
        }


        if (ground != 0)
        {
            for (int x = 0; x < FMX; x++)
            {
                for (int t = 0; t < totalOptions; t++) if (t != ground) ban(x + (FMY - 1) * FMX, t);
                for (int y = 0; y < FMY - 1; y++) ban(x + y * FMX, ground);
            }

            propagate();
        }
    }
    private static int[] DX = { -1, 0, 1, 0 };
    private static int[] DY = { 0, 1, 0, -1 };
    private static int[] OPPOSITE = { 2, 3, 0, 1 };

}
