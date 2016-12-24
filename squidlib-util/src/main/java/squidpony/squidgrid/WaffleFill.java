package squidpony.squidgrid;

import squidpony.StringKit;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * An implementation of the Wave Function Collapse algorithm for placing tiles in a way that satisfies constraints; the
 * name both sounds like Wave Function and loosely describes how the algorithm works. All tiles start off in an
 * indeterminate state, and as tiles are placed the states of their neighbors become more certain; the "Collapse" in
 * Wave Function Collapse refers to the quantum-mechanics term for a decline in unpredictability as a superposition is
 * measured, but here we use the term "Waffle" for its meaning as switching between positions or viewpoints until forced
 * into a concrete state. This is meant to act like {@link DetailedMimic} or maybe {@link MimicFill}, but only requires
 * tiles to be given to it with specific constraints on placement instead of requiring an existing design to mimic.
 * <br>
 * Massive credit goes to ExUtumno/mxgmn for developing the amazing Wave Function Collapse algorithm, which this code is
 * closely based off of, but is not at all a verbatim copy. You can find out more at mxgmn's primary repository for WFC
 * code, https://github.com/mxgmn/WaveFunctionCollapse , and see the incredible things more creative minds than my own
 * have come up with using WFC or modifications on it. This file only implements the Simple Tiled Model for now.
 * Created by Tommy Ettinger on 12/22/2016.
 */
@Beta
public class WaffleFill {

    protected GreasedRegion[] wave;
    protected GreasedRegion changes;
    protected int[] stationary;
    protected int[][] observed;

    protected RNG random;
    protected int FMX, FMY, T, limit;
    protected boolean periodic;

    double[] logProb;
    double logT;

    protected Boolean observe() {
        double min = 1E+3, sum, mainSum, logSum, noise, entropy;
        int argminx = -1, argminy = -1, amount;
        boolean[] w;

        for (int x = 0; x < FMX; x++)
            for (int y = 0; y < FMY; y++) {
                //if (OnBoundary(x, y)) continue;

                //w = wave[x][y];
                amount = 0;
                sum = 0;

                for (int t = 0; t < T; t++)
                    if (wave[t].contains(x, y)) {
                        amount += 1;
                        sum += stationary[t];
                    }

                if (sum == 0) return false;

                noise = random.nextDouble(1E-6);

                if (amount == 1) entropy = 0;
                else if (amount == T) entropy = logT;
                else {
                    mainSum = 0;
                    logSum = Math.log(sum);
                    for (int t = 0; t < T; t++) if (wave[t].contains(x, y)) mainSum += stationary[t] * logProb[t];
                    entropy = logSum - mainSum / sum;
                }

                if (entropy > 0 && entropy + noise < min) {
                    min = entropy + noise;
                    argminx = x;
                    argminy = y;
                }
            }

        if (argminx == -1 && argminy == -1) {
            observed = new int[FMX][FMY];
            for (int x = 0; x < FMX; x++) {
                for (int y = 0; y < FMY; y++)
                    for (int t = 0; t < T; t++)
                        if (wave[t].contains(x, y)) {
                            observed[x][y] = t;
                            break;
                        }
            }

            return true;
        }

        double[] distribution = new double[T];
        for (int t = 0; t < T; t++) {
            distribution[t] = wave[t].contains(argminx, argminy) ? stationary[t] : 0;
        }
        int r = DetailedMimic.weightedRandom(distribution, random.nextDouble());
        for (int t = 0; t < T; t++) {
            wave[t].set(t == r, argminx, argminy);
        }
        changes.insert(argminx, argminy);

        return null;
    }

    public boolean run(RNG rng, int limit) {
        logT = Math.log(T);
        logProb = new double[T];
        for (int t = 0; t < T; t++) logProb[t] = Math.log(stationary[t]);

        clear();

        random = rng;

        for (int l = 0; l < limit || limit == 0; l++) {
            Boolean result = observe();
            if (result != null) return result;
            while (propagate()) ;
        }

        return true;
    }

    protected void clear() {
        for (int t = 0; t < T; t++) wave[t].allOn();
        changes.clear();
    }


    int[][][] propagator;

    ArrayList<WaffleTile> tiles;
    int tilesize;

    private static final int[]
            aL = new int[]{1, 2, 3, 0}, bL = new int[]{1, 0, 3, 2},
            aT = aL, bT = new int[]{0, 3, 2, 1},
            aI = new int[]{1, 0}, bI = new int[]{0, 1},
            aZ = aI, bZ = aI, aX = new int[]{0}, bX = aX;


    public WaffleFill(String encoded, int width, int height, boolean periodic) {
        this(StringKit.split(encoded, "\uffff"), width, height, periodic);
    }

    public WaffleFill(String[] strings, int width, int height, boolean periodic) {
        this.FMX = width;
        this.FMY = height;
        this.periodic = periodic;
        int total = strings.length, idx = 0, tileCount = Integer.parseInt(strings[idx++]);
        if (strings[idx].equals("size")) {
            tilesize = Integer.parseInt(strings[++idx]);
            idx++;
        } else
            tilesize = 16;
        boolean unique = false;
        if (strings[idx].equals("unique")) {
            unique = !strings[++idx].equals("false");
            idx++;
        }
        tiles = new ArrayList<>(tileCount);
        IntVLA tempStationary = new IntVLA();

        ArrayList<int[]> action = new ArrayList<>();
        Arrangement<String> firstOccurrence = new Arrangement<>();
        int max = tileCount + idx;
        while (idx < max) {
            WaffleTile tile = new WaffleTile(strings[idx++]);
            String tilename = tile.getName();
            //if (subset != null && !subset.contains(tilename)) continue;

            int[] a, b;
            int cardinality;

            char sym = tile.symmetry;
            if (sym == 'L') {
                cardinality = 4;
                a = aL;
                b = bL;
            } else if (sym == 'T') {
                cardinality = 4;
                a = aT;
                b = bT;
            } else if (sym == 'I') {
                cardinality = 2;
                a = aI;
                b = bI;
            } else if (sym == 'Z') {
                cardinality = 2;
                a = aZ;
                b = bZ;
            } else {
                cardinality = 1;
                a = aX;
                b = bX;
            }

            T = action.size();
            firstOccurrence.add(tilename);

            int[][] map = new int[cardinality][];
            for (int t = 0; t < cardinality; t++) {
                map[t] = new int[8];

                map[t][0] = t;
                map[t][1] = a[t];
                map[t][2] = a[a[t]];
                map[t][3] = a[a[a[t]]];
                map[t][4] = b[t];
                map[t][5] = b[a[t]];
                map[t][6] = b[a[a[t]]];
                map[t][7] = b[a[a[a[t]]]];

                for (int s = 0; s < 8; s++) map[t][s] += T;

                action.add(map[t]);
            }

            tiles.add(tile);
            for (int t = 1; t < cardinality; t++) {
                tiles.add(tile.rotateCopy(t));
            }

            for (int t = 0; t < cardinality; t++)
                tempStationary.add(tile.weight);
        }

        T = action.size();
        stationary = tempStationary.toArray();

        GreasedRegion[] tempPropagator = new GreasedRegion[4];
        propagator = new int[4][][];
        for (int d = 0; d < 4; d++) {
            tempPropagator[d] = new GreasedRegion(T, T);
            propagator[d] = new int[T][T];
        }
        wave = new GreasedRegion[T];
        for (int t = 0; t < T; t++) {
            wave[t] = new GreasedRegion(FMX, FMY);
        }


        while (idx < total) {
            String current = strings[idx++];
            int spacePos = current.indexOf(' ');
            String left, right;
            int lParse = -1, rParse = -1;
            if (spacePos < 0)
                left = current;
            else {
                left = current.substring(0, spacePos);
                lParse = current.charAt(spacePos + 1) - '0';
            }
            current = strings[idx++];
            spacePos = current.indexOf(' ');
            if (spacePos < 0)
                right = current;
            else {
                right = current.substring(0, spacePos);
                rParse = current.charAt(spacePos + 1) - '0';
            }

            //if (subset != null && (!subset.Contains(left[0]) || !subset.Contains(right[0]))) continue;

            int L = action.get(firstOccurrence.getInt(left))[lParse < 0 ? 0 : lParse], D = action.get(L)[1];
            int R = action.get(firstOccurrence.getInt(right))[rParse < 0 ? 0 : rParse], U = action.get(R)[1];

            tempPropagator[0].insert(R, L);
            tempPropagator[0].insert(action.get(R)[6], action.get(L)[6]);
            tempPropagator[0].insert(action.get(L)[4], action.get(R)[4]);
            tempPropagator[0].insert(action.get(L)[2], action.get(R)[2]);

            tempPropagator[1].insert(U, D);
            tempPropagator[1].insert(action.get(D)[6], action.get(U)[6]);
            tempPropagator[1].insert(action.get(U)[4], action.get(D)[4]);
            tempPropagator[1].insert(action.get(D)[2], action.get(U)[2]);
        }

        for (int t2 = 0; t2 < T; t2++) {
            for (int t1 = 0; t1 < T; t1++) {
                tempPropagator[2].set(tempPropagator[0].contains(t1, t2), t2, t1);
                tempPropagator[3].set(tempPropagator[1].contains(t1, t2), t2, t1);
            }
        }

        IntVLA[][] sparsePropagator = new IntVLA[4][T];
        for (int d = 0; d < 4; d++) {
            for (int t = 0; t < T; t++) {
                sparsePropagator[d][t] = new IntVLA();
            }
        }

        IntVLA sp;
        GreasedRegion tg;
        for (int d = 0; d < 4; d++) {
            tg = tempPropagator[d];
            for (int t1 = 0; t1 < T; t1++) {
                sp = sparsePropagator[d][t1];

                for (int t2 = 0; t2 < T; t2++) {
                    if (tg.contains(t1, t2)) sp.add(t2);
                }
                propagator[d][t1] = sp.toArray();
            }
        }
    }

    /**
     * Constructs a WaffleFill given a WaffleTile array, a String array, the size of a tile in chars, the width and
     * height of the result to generate
     * @param waffleTiles
     * @param neighbors
     * @param tileSize
     * @param width
     * @param height
     * @param periodic
     */
    public WaffleFill(WaffleTile[] waffleTiles, String[] neighbors, int tileSize, int width, int height, boolean periodic) {
        this.FMX = width;
        this.FMY = height;
        this.periodic = periodic;
        int total = neighbors.length, idx = 0, tileCount = waffleTiles.length;
        this.tilesize = tileSize;
        boolean unique = false;
        tiles = new ArrayList<>(tileCount);
        IntVLA tempStationary = new IntVLA();

        ArrayList<int[]> action = new ArrayList<>();
        Arrangement<String> firstOccurrence = new Arrangement<>();
        while (idx < tileCount) {
            WaffleTile tile = new WaffleTile(waffleTiles[idx++]);
            String tilename = tile.getName();
            //if (subset != null && !subset.contains(tilename)) continue;

            int[] a, b;
            int cardinality;

            char sym = tile.symmetry;
            if (sym == 'L') {
                cardinality = 4;
                a = aL;
                b = bL;
            } else if (sym == 'T') {
                cardinality = 4;
                a = aT;
                b = bT;
            } else if (sym == 'I') {
                cardinality = 2;
                a = aI;
                b = bI;
            } else if (sym == 'Z') {
                cardinality = 2;
                a = aZ;
                b = bZ;
            } else {
                cardinality = 1;
                a = aX;
                b = bX;
            }

            T = action.size();
            firstOccurrence.add(tilename);

            int[][] map = new int[cardinality][];
            for (int t = 0; t < cardinality; t++) {
                map[t] = new int[8];

                map[t][0] = t;
                map[t][1] = a[t];
                map[t][2] = a[a[t]];
                map[t][3] = a[a[a[t]]];
                map[t][4] = b[t];
                map[t][5] = b[a[t]];
                map[t][6] = b[a[a[t]]];
                map[t][7] = b[a[a[a[t]]]];

                for (int s = 0; s < 8; s++) map[t][s] += T;

                action.add(map[t]);
            }

            tiles.add(tile);
            for (int t = 1; t < cardinality; t++) {
                tiles.add(tile.rotateCopy(t));
            }

            for (int t = 0; t < cardinality; t++)
                tempStationary.add(tile.weight);
        }

        T = action.size();
        stationary = tempStationary.toArray();

        GreasedRegion[] tempPropagator = new GreasedRegion[4];
        propagator = new int[4][][];
        for (int d = 0; d < 4; d++) {
            tempPropagator[d] = new GreasedRegion(T, T);
            propagator[d] = new int[T][T];
        }
        wave = new GreasedRegion[T];
        for (int t = 0; t < T; t++) {
            wave[t] = new GreasedRegion(FMX, FMY);
        }
        idx = 0;

        while (idx < total - 1) {
            String current = neighbors[idx++];
            int spacePos = current.indexOf(' ');
            String left, right;
            int lParse = -1, rParse = -1;
            if (spacePos < 0)
                left = current;
            else {
                left = current.substring(0, spacePos);
                lParse = current.charAt(spacePos + 1) - '0';
            }
            current = neighbors[idx++];
            spacePos = current.indexOf(' ');
            if (spacePos < 0)
                right = current;
            else {
                right = current.substring(0, spacePos);
                rParse = current.charAt(spacePos + 1) - '0';
            }

            //if (subset != null && (!subset.Contains(left[0]) || !subset.Contains(right[0]))) continue;

            int L = action.get(firstOccurrence.getInt(left))[lParse < 0 ? 0 : lParse], D = action.get(L)[1];
            int R = action.get(firstOccurrence.getInt(right))[rParse < 0 ? 0 : rParse], U = action.get(R)[1];

            tempPropagator[0].insert(R, L);
            tempPropagator[0].insert(action.get(R)[6], action.get(L)[6]);
            tempPropagator[0].insert(action.get(L)[4], action.get(R)[4]);
            tempPropagator[0].insert(action.get(L)[2], action.get(R)[2]);

            tempPropagator[1].insert(U, D);
            tempPropagator[1].insert(action.get(D)[6], action.get(U)[6]);
            tempPropagator[1].insert(action.get(U)[4], action.get(D)[4]);
            tempPropagator[1].insert(action.get(D)[2], action.get(U)[2]);
        }

        for (int t2 = 0; t2 < T; t2++) {
            for (int t1 = 0; t1 < T; t1++) {
                tempPropagator[2].set(tempPropagator[0].contains(t1, t2), t2, t1);
                tempPropagator[3].set(tempPropagator[1].contains(t1, t2), t2, t1);
            }
        }

        IntVLA[][] sparsePropagator = new IntVLA[4][T];
        for (int d = 0; d < 4; d++) {
            for (int t = 0; t < T; t++) {
                sparsePropagator[d][t] = new IntVLA();
            }
        }

        IntVLA sp;
        GreasedRegion tg;
        for (int d = 0; d < 4; d++) {
            tg = tempPropagator[d];
            for (int t1 = 0; t1 < T; t1++) {
                sp = sparsePropagator[d][t1];

                for (int t2 = 0; t2 < T; t2++) {
                    if (tg.contains(t1, t2)) sp.add(t2);
                }
                propagator[d][t1] = sp.toArray();
            }
        }
    }

    protected boolean propagate() {
        boolean change = false, b;
        for (int x2 = 0; x2 < FMX; x2++) {
            for (int y2 = 0; y2 < FMY; y2++) {
                for (int d = 0; d < 4; d++) {
                    int x1 = x2, y1 = y2;
                    if (d == 0) {
                        if (x2 == 0) {
                            if (!periodic) continue;
                            else x1 = FMX - 1;
                        } else x1 = x2 - 1;
                    } else if (d == 1) {
                        if (y2 == FMY - 1) {
                            if (!periodic) continue;
                            else y1 = 0;
                        } else y1 = y2 + 1;
                    } else if (d == 2) {
                        if (x2 == FMX - 1) {
                            if (!periodic) continue;
                            else x1 = 0;
                        } else x1 = x2 + 1;
                    } else {
                        if (y2 == 0) {
                            if (!periodic) continue;
                            else y1 = FMY - 1;
                        } else y1 = y2 - 1;
                    }

                    if (!changes.contains(x1, y1)) continue;

                    for (int t2 = 0; t2 < T; t2++) {
                        if (!wave[t2].contains(x2, y2)) continue;

                        b = false;
                        int[] prop = propagator[d][t2];
                        for (int i1 = 0; i1 < prop.length && !b; i1++) {
                            b = wave[prop[i1]].contains(x1, y1);
                        }

                        if (!b) {
                            changes.insert(x2, y2);
                            change = true;
                            wave[t2].remove(x2, y2);
                        }
                    }
                }
            }
        }

        return change;
    }

    public char[][] output()
    {
        char[][] result = new char[FMX * tilesize][FMY * tilesize];

        if (observed != null)
        {
            for (int x = 0; x < FMX; x++) {
                for (int y = 0; y < FMY; y++)
                {
                    WaffleTile tile = tiles.get(observed[x][y]);
                    for (int yt = 0; yt < tilesize; yt++) {
                        for (int xt = 0; xt < tilesize; xt++)
                        {
                            result[x * tilesize + xt][y * tilesize + yt] = tile.get(xt, yt);
                        }
                    }
                }
            }
        }
        else
        {
            double[] weights = new double[T];
            int[][] sums = GreasedRegion.sum(wave);
            char c;
            for (int x = 0; x < FMX; x++)
            {
                for (int y = 0; y < FMY; y++)
                {
                    int amount = sums[x][y];
                    double ssum = 0.0;
                    for (int i = 0; i < T; i++) {
                        if(wave[i].contains(x, y)) ssum += stationary[i];
                    }
                    double lambda = 1.0 / (ssum);

                    for (int yt = 0; yt < tilesize; yt++)
                    {
                        for (int xt = 0; xt < tilesize; xt++) {
                            c = '#';
                            Arrays.fill(weights, 0.0);
                            for (int t = 0; t < T; t++) {
                                if (wave[t].contains(x, y)) {
                                    weights[t] = stationary[t] * lambda;
                                    c = '.';
                                }
                            }
                            if(c == '.')
                                c = tiles.get(DetailedMimic.weightedRandom(weights, random.nextDouble())).get(xt, yt);
                            result[x * tilesize + xt][y * tilesize + yt] = c;
                        }
                    }
                }
            }
        }

        return result;
    }


    public static class WaffleTile implements Serializable {
        private static final long serialVersionUID = 1L;

        public char[] data;
        public Router indexer;
        protected String name = null;
        protected char routerID = 0;
        protected int w, h;
        public int weight = 100;
        public char symmetry;

        public WaffleTile() {
            w = 1;
            h = 1;
            symmetry = 'X';
            data = new char[]{'.'};
            indexer = Router.Generator.simple(1, 1);
        }

        public WaffleTile(WaffleTile other) {
            this(other.data, other.routerID, other.w, other.h, other.weight, other.symmetry, other.name);
        }

        public WaffleTile(char[] data, char routerID, int w, int h, int weight, char symmetry, String name) {
            this.data = new char[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
            this.w = w;
            this.h = h;
            this.weight = weight;
            this.symmetry = symmetry;
            this.name = name;
            this.routerID = routerID;

            if ((routerID & 3) != 0 && (routerID & 12) != 0)
                indexer = Router.Generator.chain(w, h,
                        Router.Generator.rotate(w, h, routerID & 3),
                        Router.Generator.flip(w, h, (routerID & 4) != 0, (routerID & 8) != 0));
            else if ((routerID & 3) != 0)
                indexer = Router.Generator.rotate(w, h, routerID & 3);
            else if ((routerID & 12) != 0)
                indexer = Router.Generator.flip(w, h, (routerID & 4) != 0, (routerID & 8) != 0);
            else
                indexer = Router.Generator.simple(w, h);

        }

        public WaffleTile(String encoded) {
            data = new char[encoded.length() - 26];
            encoded.getChars(0, data.length, data, 0);
            routerID = encoded.charAt(data.length);
            w = Integer.parseInt(encoded.substring(data.length + 1, data.length + 9), 16);
            h = Integer.parseInt(encoded.substring(data.length + 9, data.length + 17), 16);
            weight = Integer.parseInt(encoded.substring(data.length + 17, data.length + 25), 16);
            symmetry = encoded.charAt(data.length + 25);

            if ((routerID & 3) != 0 && (routerID & 12) != 0)
                indexer = Router.Generator.chain(w, h,
                        Router.Generator.rotate(w, h, routerID & 3),
                        Router.Generator.flip(w, h, (routerID & 4) != 0, (routerID & 8) != 0));
            else if ((routerID & 3) != 0)
                indexer = Router.Generator.rotate(w, h, routerID & 3);
            else if ((routerID & 12) != 0)
                indexer = Router.Generator.flip(w, h, (routerID & 4) != 0, (routerID & 8) != 0);
            else
                indexer = Router.Generator.simple(w, h);
        }

        public WaffleTile(char[][] map, char symmetry) {
            w = map.length;
            h = map[0].length;
            this.symmetry = symmetry;
            data = new char[w * h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    data[x + y * w] = map[x][y];
                }
            }
            indexer = Router.Generator.simple(w, h);
        }

        public WaffleTile(char[][] map, char symmetry, int rotation) {
            w = map.length;
            h = map[0].length;
            this.symmetry = symmetry;
            data = new char[w * h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    data[x + y * w] = map[x][y];
                }
            }
            indexer = Router.Generator.rotate(w, h, rotation);
            routerID = (char) (rotation & 3);
        }

        public WaffleTile(char[][] map, char symmetry, boolean flipX, boolean flipY) {
            w = map.length;
            h = map[0].length;
            this.symmetry = symmetry;
            data = new char[w * h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    data[x + y * w] = map[x][y];
                }
            }
            indexer = Router.Generator.flip(w, h, flipX, flipY);
            if (flipX)
                routerID |= 4;
            if (flipY)
                routerID |= 8;
        }

        public WaffleTile(char[][] map, char symmetry, int rotation, boolean flipX, boolean flipY) {
            w = map.length;
            h = map[0].length;
            this.symmetry = symmetry;
            data = new char[w * h];
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    data[x + y * w] = map[x][y];
                }
            }
            if ((rotation & 3) != 0 && (flipX || flipY)) {
                indexer = Router.Generator.chain(w, h,
                        Router.Generator.rotate(w, h, rotation),
                        Router.Generator.flip(w, h, flipX, flipY));
            } else if ((rotation & 3) != 0) {
                indexer = Router.Generator.rotate(w, h, rotation);
            } else if (flipX || flipY) {
                indexer = Router.Generator.flip(w, h, flipX, flipY);
            } else {
                indexer = Router.Generator.simple(w, h);
            }
            routerID = (char) (rotation & 3);
            if (flipX)
                routerID |= 4;
            if (flipY)
                routerID |= 8;
        }

        public WaffleTile(char[][] map, char symmetry, int rotation, boolean flipX, boolean flipY, int weight) {
            this(map, symmetry, rotation, flipX, flipY);
            this.weight = weight;
        }

        public char get(int x, int y)
        {
            return data[indexer.reroute(x, y)];
        }

        public String getName() {
            return (name != null) ? name : (name = StringKit.hex(CrossHash.Lightning.hash64(data)));
        }

        public void setName(String name) {
            this.name = name;
        }

        public String encode() {
            return String.valueOf(data) + routerID + StringKit.hex(w) + StringKit.hex(h) + StringKit.hex(weight) + symmetry;
        }

        public WaffleTile rotateCopy(int rotation) {
            char id = routerID;
            id &= 12;
            id |= (rotation & 3);
            return new WaffleTile(data, id, w, h, weight, symmetry, getName());
        }
    }
}
