package squidpony.squidgrid;

import squidpony.StringKit;
import squidpony.annotation.Beta;
import squidpony.squidgrid.mapping.DungeonUtility;
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

    int[][][] propagator;

    ArrayList<WaffleTile> tiles;
    int tilesize;

    double[] logProb;
    double logT;
    Router[] routers;
    public boolean initialized = false;
    public transient String encoded;

    private static final int[]
            aL = new int[]{1, 2, 3, 0}, bL = new int[]{1, 0, 3, 2},
            aT = aL, bT = new int[]{0, 3, 2, 1},
            aI = new int[]{1, 0}, bI = new int[]{0, 1},
            aZ = aI, bZ = aI, aX = new int[]{0}, bX = aX;

    public WaffleFill()
    {
        this(16);
    }
    public WaffleFill(int tileSize)
    {
        tilesize = Math.max(1, tileSize);
        routers = new Router[16];
        for (int r = 0; r < 16; r++) {
            if ((r & 3) != 0 && (r & 12) != 0)
                routers[r] = Router.Generator.chain(tilesize, tilesize,
                        Router.Generator.rotate(tilesize, tilesize, r & 3),
                        Router.Generator.flip(tilesize, tilesize, (r & 4) != 0, (r & 8) != 0));
            else if ((r & 3) != 0)
                routers[r] = Router.Generator.rotate(tilesize, tilesize, r & 3);
            else if ((r & 12) != 0)
                routers[r] = Router.Generator.flip(tilesize, tilesize, (r & 4) != 0, (r & 8) != 0);
            else
                routers[r] = Router.Generator.simple(tilesize, tilesize);
        }
    }


    public WaffleFill(String encoded, int width, int height, boolean periodic) {
        this(StringKit.split(encoded, "\uffff"), width, height, periodic);
        this.encoded = encoded;
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

        routers = new Router[16];
        for (int r = 0; r < 16; r++) {
            if ((r & 3) != 0 && (r & 12) != 0)
                routers[r] = Router.Generator.chain(tilesize, tilesize,
                        Router.Generator.rotate(tilesize, tilesize, r & 3),
                        Router.Generator.flip(tilesize, tilesize, (r & 4) != 0, (r & 8) != 0));
            else if ((r & 3) != 0)
                routers[r] = Router.Generator.rotate(tilesize, tilesize, r & 3);
            else if ((r & 12) != 0)
                routers[r] = Router.Generator.flip(tilesize, tilesize, (r & 4) != 0, (r & 8) != 0);
            else
                routers[r] = Router.Generator.simple(tilesize, tilesize);
        }

        /*
        boolean unique = false;
        if (strings[idx].equals("unique")) {
            unique = !strings[++idx].equals("false");
            idx++;
        }*/
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

                map[t][0] = t + T;
                map[t][1] = a[t] + T;
                map[t][2] = a[a[t]] + T;
                map[t][3] = a[a[a[t]]] + T;
                map[t][4] = b[t] + T;
                map[t][5] = b[a[t]] + T;
                map[t][6] = b[a[a[t]]] + T;
                map[t][7] = b[a[a[a[t]]]] + T;

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
        changes = new GreasedRegion(FMX, FMY);
        for (int t = 0; t < T; t++) {
            wave[t] = new GreasedRegion(FMX, FMY);
        }


        while (idx < total) {
            String current = strings[idx++];
            int spacePos = current.indexOf(' ');
            String left, right;
            int lParse = 0, rParse = 0;
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

            int L = action.get(firstOccurrence.getInt(left))[lParse], D = action.get(L)[1];
            int R = action.get(firstOccurrence.getInt(right))[rParse], U = action.get(R)[1];

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
        initialized = true;
    }

    /**
     * Constructs a WaffleFill given a WaffleTile array, a String array, the size of a tile in chars, the width and
     * height of the result to generate
     * @param waffleTiles
     * @param tileSize
     * @param width
     * @param height
     * @param periodic
     */
    public WaffleFill(WaffleTile[] waffleTiles, int tileSize, int width, int height, boolean periodic) {
        this.FMX = width;
        this.FMY = height;
        this.periodic = periodic;
        int idx = 0, tileCount = waffleTiles.length;
        this.tilesize = Math.max(1, tileSize);
        ArrayList<String> builder = new ArrayList<>(64);
        builder.add(Integer.toString(tileCount));
        builder.add("size");
        builder.add(Integer.toString(tilesize));
        routers = new Router[16];
        for (int r = 0; r < 16; r++) {
            if ((r & 3) != 0 && (r & 12) != 0)
                routers[r] = Router.Generator.chain(tilesize, tilesize,
                        Router.Generator.rotate(tilesize, tilesize, r & 3),
                        Router.Generator.flip(tilesize, tilesize, (r & 4) != 0, (r & 8) != 0));
            else if ((r & 3) != 0)
                routers[r] = Router.Generator.rotate(tilesize, tilesize, r & 3);
            else if ((r & 12) != 0)
                routers[r] = Router.Generator.flip(tilesize, tilesize, (r & 4) != 0, (r & 8) != 0);
            else
                routers[r] = Router.Generator.simple(tilesize, tilesize);
        }
        tiles = new ArrayList<>(tileCount);
        IntVLA tempStationary = new IntVLA();

        ArrayList<int[]> action = new ArrayList<>();
        IntVLA firstOccurrence = new IntVLA();
        while (idx < tileCount) {
            WaffleTile tile = new WaffleTile(waffleTiles[idx++]);
            builder.add(tile.serializeToString());
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

            int[][] map = new int[cardinality][];
            for (int t = 0; t < cardinality; t++) {
                map[t] = new int[8];

                map[t][0] = t + T;
                map[t][1] = a[t] + T;
                map[t][2] = a[a[t]] + T;
                map[t][3] = a[a[a[t]]] + T;
                map[t][4] = b[t] + T;
                map[t][5] = b[a[t]] + T;
                map[t][6] = b[a[a[t]]] + T;
                map[t][7] = b[a[a[a[t]]]] + T;

                action.add(map[t]);
            }

            tiles.add(tile);
            firstOccurrence.add(T);
            for (int t = 1; t < cardinality; t++) {
                tiles.add(tile.rotateCopy(t));
                firstOccurrence.add(T);
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
        changes = new GreasedRegion(FMX, FMY);
        for (int t = 0; t < T; t++) {
            wave[t] = new GreasedRegion(FMX, FMY);
        }
        idx = 0;

        /*
        while (idx < total - 1) {
            String current = neighbors[idx++];
            int spacePos = current.indexOf(' ');
            String left, right;
            int lParse = 0, rParse = 0;
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

            int L = action.get(firstOccurrence.getInt(left))[lParse], D = action.get(L)[1];
            int R = action.get(firstOccurrence.getInt(right))[rParse], U = action.get(R)[1];

            tempPropagator[0].insert(R, L);
            tempPropagator[0].insert(action.get(R)[6], action.get(L)[6]);
            tempPropagator[0].insert(action.get(L)[4], action.get(R)[4]);
            tempPropagator[0].insert(action.get(L)[2], action.get(R)[2]);

            tempPropagator[1].insert(U, D);
            tempPropagator[1].insert(action.get(D)[6], action.get(U)[6]);
            tempPropagator[1].insert(action.get(U)[4], action.get(D)[4]);
            tempPropagator[1].insert(action.get(D)[2], action.get(U)[2]);
        }
         */
        WaffleTile leftTile, rightTile;

        int lPos, rPos;
        for (int left = 0; left < T; left++) {
            leftTile = tiles.get(left);
            lPos = firstOccurrence.get(left);
            for (int right = 0; right < T; right++) {
                rightTile = tiles.get(right);
                if(leftTile.rightSide == rightTile.leftSide)
                {
                    rPos = firstOccurrence.get(right);
                    builder.add(leftTile.getName() + " " + (left - lPos));
                    builder.add(rightTile.getName() + " " + (right - rPos));
                    int L = action.get(lPos)[left - lPos], D = action.get(L)[1];
                    int R = action.get(rPos)[right - rPos], U = action.get(R)[1];

                    tempPropagator[0].insert(R, L);
                    tempPropagator[0].insert(action.get(R)[6], action.get(L)[6]);
                    tempPropagator[0].insert(action.get(L)[4], action.get(R)[4]);
                    tempPropagator[0].insert(action.get(L)[2], action.get(R)[2]);

                    tempPropagator[1].insert(U, D);
                    tempPropagator[1].insert(action.get(D)[6], action.get(U)[6]);
                    tempPropagator[1].insert(action.get(U)[4], action.get(D)[4]);
                    tempPropagator[1].insert(action.get(D)[2], action.get(U)[2]);
                }
            }
        }
        for (int t2 = 0; t2 < T; t2++) {
            for (int t1 = 0; t1 < T; t1++) {
                tempPropagator[2].set(tempPropagator[0].contains(t1, t2), t2, t1);
                tempPropagator[3].set(tempPropagator[1].contains(t1, t2), t2, t1);
            }
        }

        DungeonUtility.debugPrint(tempPropagator[0].toChars());
        System.out.println();
        DungeonUtility.debugPrint(tempPropagator[1].toChars());
        System.out.println();
        DungeonUtility.debugPrint(tempPropagator[2].toChars());
        System.out.println();
        DungeonUtility.debugPrint(tempPropagator[3].toChars());
        System.out.println();

        /*
        IntVLA[][] sparsePropagator = new IntVLA[4][T];
        for (int d = 0; d < 4; d++) {
            for (int t = 0; t < T; t++) {
                sparsePropagator[d][t] = new IntVLA();
            }
        }*/

        IntVLA sp = new IntVLA();
        GreasedRegion tg;
        for (int d = 0; d < 4; d++) {
            tg = tempPropagator[d];
            for (int t1 = 0; t1 < T; t1++) {
                sp.clear();
                for (int t2 = 0; t2 < T; t2++) {
                    if (tg.contains(t1, t2)) sp.add(t2);
                }
                propagator[d][t1] = sp.toArray();
            }
        }
        encoded = StringKit.join("\uffff", builder);
        initialized = true;
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

                if (sum == 0)
                    return false;

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
            char c;
            for (int x = 0; x < FMX; x++)
            {
                for (int y = 0; y < FMY; y++)
                {
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

    public WaffleTile tile(char symmetry, int weight, String... map)
    {
        return new WaffleTile(symmetry, weight, map);
    }

    public class WaffleTile implements Serializable {
        private static final long serialVersionUID = 1L;

        public char[] data;
        public Router indexer;
        protected String name = null;
        protected char routerID = 0;
        public int weight = 100;
        public char symmetry;
        public transient int leftSide, rightSide;

        public WaffleTile() {
            symmetry = 'X';
            data = new char[tilesize * tilesize];
            Arrays.fill(data, '.');
            indexer = routers[0];
            leftSide = sideHash(false);
            rightSide = sideHash(true);
        }

        public WaffleTile(WaffleTile other) {
            this(other.data, other.routerID, other.weight, other.symmetry, other.name);
        }

        public WaffleTile(char[] data, char routerID, int weight, char symmetry, String name) {
            this.data = new char[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
            this.weight = weight;
            this.symmetry = symmetry;
            this.name = name;
            this.routerID = routerID;
            this.indexer = routers[this.routerID &= 15];
            leftSide = sideHash(false);
            rightSide = sideHash(true);
        }

        public WaffleTile(String encoded) {
            data = new char[encoded.length() - 10];
            encoded.getChars(0, data.length, data, 0);
            routerID = encoded.charAt(data.length);
            weight = Integer.parseInt(encoded.substring(data.length + 1, data.length + 9), 16);
            symmetry = encoded.charAt(data.length + 9);
            indexer = routers[routerID &= 15];
            leftSide = sideHash(false);
            rightSide = sideHash(true);
        }

        public WaffleTile(char symmetry, int weight, String... map) {
            this.symmetry = symmetry;
            data = new char[tilesize * tilesize];
            for (int x = 0; x < tilesize; x++) {
                for (int y = 0; y < tilesize; y++) {
                    data[x + y * tilesize] = map[y].charAt(x);
                }
            }
            routerID = 0;
            indexer = routers[0];
            leftSide = sideHash(false);
            rightSide = sideHash(true);
            this.weight = weight;
        }


        public char get(int x, int y)
        {
            return data[indexer.reroute(x, y)];
        }

        public int sideHash(final boolean rightSide)
        {
            int z = 0x632BE5AB, result = 1, x = rightSide ? tilesize - 1 : 0;
            for (int y = 1; y < tilesize-1; y++) {
                result += (z ^= data[indexer.reroute(x, y)] * 0x85157AF5) + 0x62E2AC0D;
            }
            return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
        }

        public String getName() {
            return (name != null) ? name : (name = StringKit.hex(CrossHash.Lightning.hash64(data)));
        }

        public void setName(String name) {
            this.name = name;
        }

        public String serializeToString() {
            return String.valueOf(data) + routerID + StringKit.hex(weight) + symmetry;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WaffleTile that = (WaffleTile) o;

            if (routerID != that.routerID) return false;
            if (weight != that.weight) return false;
            if (symmetry != that.symmetry) return false;
            if (!Arrays.equals(data, that.data)) return false;
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(data);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (int) routerID;
            result = 31 * result + weight;
            result = 31 * result + (int) symmetry;
            return result;
        }

        public WaffleTile rotateCopy(int rotation) {
            char id = routerID;
            id &= 12;
            id |= (rotation & 3);
            return new WaffleTile(data, id, weight, symmetry, getName());
        }
    }
}
