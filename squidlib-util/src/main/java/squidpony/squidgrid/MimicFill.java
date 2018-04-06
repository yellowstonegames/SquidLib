package squidpony.squidgrid;

import squidpony.ArrayTools;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.LightRNG;

/**
 * A class that imitates patterns in an existing 2D boolean array and uses it to generate a new boolean array with a
 * similar visual style. Useful for creating procedural filler around a desired path or series of known rooms. Can also
 * convert between 2D boolean arrays (samples) and 2D char arrays (maps).
 * Created by Tommy Ettinger on 5/14/2016, porting from https://github.com/mxgmn/ConvChain (public domain)
 */
public class MimicFill {

    private static final int N = 3;

    private MimicFill()
    {

    }
    /**
     * Converts a 2D char array map to a 2D boolean array, where any chars in the array or vararg yes will result in
     * true in the returned array at that position and any other chars will result in false. The result can be given to
     * fill() as its sample parameter.
     * @param map a 2D char array that you want converted to a 2D boolean array
     * @param yes an array or vararg of the chars to consider true in map
     * @return a 2D boolean array that can be given to fill()
     */
    public static boolean[][] mapToSample(char[][] map, char... yes)
    {
        if(map == null || map.length == 0)
            return new boolean[0][0];
        boolean[][] sample = new boolean[map.length][map[0].length];
        if(yes == null || yes.length == 0)
            return sample;
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                for (int c = 0; c < yes.length; c++) {
                    if(sample[x][y] = map[x][y] == yes[c])
                        break;
                }
            }
        }
        return sample;
    }

    /**
     * Comverts a 2D boolean array to a 2D char array, where false means the parameter no and true the parameter yes.
     * @param sample a 2D boolean array that you want converted to a 2D char array
     * @param yes true in sample will be mapped to this char; usually '.'
     * @param no false in sample will be mapped to this char; usually '#'
     * @return a 2D char array containing only the chars yes and no
     */
    public static char[][] sampleToMap(boolean[][] sample, char yes, char no)
    {

        if(sample == null || sample.length == 0)
            return new char[0][0];
        char[][] map = new char[sample.length][sample[0].length];
        for (int x = 0; x < sample.length; x++) {
            for (int y = 0; y < sample[0].length; y++) {
                map[x][y] = (sample[x][y]) ? yes : no;
            }
        }
        return map;
    }

    /**
     * Comverts a 2D boolean array to a 2D char array, where false means the parameter no and true the parameter yes.
     * @param sample a 1D boolean array that you want converted to a 2D char array
     * @param width  the desired width of the 2D char array
     * @param height the desired height of the 2D char array
     * @param yes true in sample will be mapped to this char; usually '.'
     * @param no false in sample will be mapped to this char; usually '#'
     * @return a 2D char array containing only the chars yes and no
     */
    public static char[][] sampleToMap(boolean[] sample, int width, int height, char yes, char no)
    {

        if(sample == null || sample.length == 0 || width <= 0 || height <= 0)
            return new char[0][0];
        char[][] map = new char[width][height];
        for (int x = 0, idx = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = sample[idx++] ? yes : no;
                if(idx >= sample.length)
                    return map;
            }
        }
        return map;
    }

    /**
     * Runs a logical OR on each pair of booleans at the same position in left and right, and returns the result of all
     * the OR operations as a 2D boolean array (using the minimum dimensions shared between left and right).
     * @param left a 2D boolean array
     * @param right another 2D boolean array
     * @return the 2D array resulting from a logical OR on each pair of booleans at a point shared by left and right
     */
    public static boolean[][] orSamples(boolean[][] left, boolean[][] right)
    {
        if(left == null && right == null) return new boolean[0][0];
        else if(left == null || left.length <= 0) return ArrayTools.copy(right);
        else if(right == null || right.length <= 0) return ArrayTools.copy(left);

        int width = Math.min(left.length, right.length),
                height = Math.min(left[0].length, right[0].length);
        boolean[][] done = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                done[x][y] = left[x][y] || right[x][y];
            }
        }
        return done;
    }


    /**
     * Runs a logical AND on each pair of booleans at the same position in left and right, and returns the result of all
     * the AND operations as a 2D boolean array (using the minimum dimensions shared between left and right).
     * @param left a 2D boolean array
     * @param right another 2D boolean array
     * @return the 2D array resulting from a logical AND on each pair of booleans at a point shared by left and right
     */
    public static boolean[][] andSamples(boolean[][] left, boolean[][] right)
    {
        if(left == null || right == null || left.length <= 0 || right.length <= 0) return new boolean[0][0];

        int width = Math.min(left.length, right.length),
                height = Math.min(left[0].length, right[0].length);
        boolean[][] done = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                done[x][y] = left[x][y] && right[x][y];
            }
        }
        return done;
    }

    /**
     * Given a 2D boolean array sample (usually a final product of this class' fill() method) and an Iterable of Coord
     * (such as a List or Set of Coord, but a Region can also work), copies sample, then marks every Coord in points as
     * true if it is in-bounds, and returns the modified copy of sample. Does not alter the original sample. You may
     * want to use this with techniques like drawing a long line with Bresenham, OrthoLine, or WobblyLine, potentially
     * widening the line with Radius.expand(), and then passing the result as the Iterable of Coord. You could then make
     * the start and end of the line into an entrance and exit and be sure the player can get from one to the other
     * (except for Bresenham and other lines that may make diagonal movements, if the player cannot move diagonally).
     * @param sample a 2D boolean array; will be copied, not modified directly
     * @param points an Iterable (such as a List or Set) of Coord that will be marked as true if in-bounds
     * @return a copy of sample with the Coords in points marked as true
     */
    public static boolean[][] markSample(boolean[][] sample, Iterable<Coord> points)
    {
        if(sample == null || sample.length <= 0)
            return new boolean[0][0];
        boolean[][] sample2 = ArrayTools.copy(sample);
        int width = sample2.length, height = sample2[0].length;
        for(Coord c : points)
        {
            if(c.x >= 0 && c.x < width && c.y >= 0 && c.y < height)
            {
                sample2[c.x][c.y] = true;
            }
        }
        return sample2;
    }

    /*
    public static boolean[][] fill(boolean[][] sample, int size, double temperature, int iterations, RNG random) {
        boolean[][] field = new boolean[size][size];
        double[] weights = new double[1 << (N * N)];

        for (int x = 0; x < sample.length; x++) {
            for (int y = 0; y < sample[x].length; y++) {
                Pattern[] p = new Pattern[8];

                p[0] = new Pattern(sample, x, y, N);
                p[1] = p[0].rotate();
                p[2] = p[1].rotate();
                p[3] = p[2].rotate();
                p[4] = p[0].reflect();
                p[5] = p[1].reflect();
                p[6] = p[2].reflect();
                p[7] = p[3].reflect();

                for (int k = 0; k < 8; k++) {
                    weights[p[k].index()]++;
                }
            }
        }

        for (int k = 0; k < weights.length; k++) {
            if (weights[k] <= 0)
                weights[k] = 0.1;
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                field[x][y] = random.nextBoolean();
            }
        }

        int i, j;
        double p, q;
        for (int k = 0; k < iterations * size * size; k++) {
            i = random.nextInt(size);
            j = random.nextInt(size);

            p = 1.0;
            for (int y = j - N + 1; y <= j + N - 1; y++)
                for (int x = i - N + 1; x <= i + N - 1; x++) p *= weights[Pattern.index(field, x, y, N)];

            field[i][j] = !field[i][j];

            q = 1.0;
            for (int y = j - N + 1; y <= j + N - 1; y++)
                for (int x = i - N + 1; x <= i + N - 1; x++) q *= weights[Pattern.index(field, x, y, N)];


            if (Math.pow(q / p, 1.0 / temperature) < random.nextDouble())
                field[i][j] = !field[i][j];
        }
        return field;
    }
    */
    /**
     * The main part of MimicFill; generates a 2D boolean array that mimics the patterns present in the 2D boolean array
     * sample, but can produce a larger or smaller output 2D array than the sample.
     * Takes a 2D boolean array as a "sample" (often one of the constants in this class), a size parameter for the
     * generated square data (same width and height) and several other parameters that affect how closely the output
     * will match the input. The temperature is how "agitated" or "chaotic" the changes to the map can be; 0.2 is
     * definitely recommended for map-like data but other values (between 0 and 1, both exclusive) may be better fits
     * for certain kinds of output. The iterations parameter should usually be between 3 and 5, with higher values
     * taking longer but fitting more closely; values over 5 are barely different from 5 here. This also takes an RNG,
     * and because it queries it very frequently, a fast RNG is ideal; the default RandomnessSource used by RNG
     * ({@link LightRNG}) is fine, and in most cases {@link squidpony.squidmath.ThrustAltRNG} will be reasonably good
     * for quality while probably being slightly faster. On GWT, {@link squidpony.squidmath.Zag32RNG} is a good choice
     * due to how few operations on {@code long} values it needs to do, and GWT has a hard time with longs.
     * @param sample a 2D boolean array to mimic visually; you can use mapToSample() if you have a 2D char array
     * @param size the side length of the square boolean array to generate
     * @param temperature typically 0.2 works well for this, but other numbers between 0 and 1 may work
     * @param iterations typically 3-5 works well for this; lower numbers may have slight problems with quality,
     *                   and higher numbers make this slightly slower
     * @param random an RNG to use for the random components of this technique
     * @return a new 2D boolean array, width = size, height = size, mimicking the visual style of sample
     */
    public static boolean[][] fill(boolean[][] sample, int size, double temperature, int iterations, IRNG random)
    {
        boolean[][] field = new boolean[size][size];
        double[] weights = new double[1 << (N * N)];
        for (int x = 0; x < sample.length; x++) {
            for (int y = 0; y < sample[x].length; y++) {
                weights[Pattern.index(sample, x, y, N, false, false, false)]++;
                weights[Pattern.index(sample, x, y, N, false, true, true)]++;
                weights[Pattern.index(sample, x, y, N, true, true, false)]++;
                weights[Pattern.index(sample, x, y, N, true, false, true)]++;
                weights[Pattern.index(sample, x, y, N, true, false, false)]++;
                weights[Pattern.index(sample, x, y, N, true, true, true)]++;
                weights[Pattern.index(sample, x, y, N, false, true, false)]++;
                weights[Pattern.index(sample, x, y, N, false, false, true)]++;
            }
        }

        for (int k = 0; k < weights.length; k++)
        {
            if (weights[k] <= 0)
                weights[k] = 0.1;
        }
        long randomBits;
        for (int o = (size * size) + 63 >> 6, x = 0, y = 0; o > 0; o--) {
            randomBits = random.nextLong();
            for (long a = 1; a != 0; a <<= 1) {
                field[x][y++] = (randomBits & a) == 0;
                if(y >= size)
                {
                    if(++x >= size) break;
                    y = 0;
                }
            }
        }
        /*
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                field[x][y] = random.nextBoolean();
            }
        }*/
        for (int k = 0; k < iterations * size * size; k++)
        {
            int x = random.nextInt(size), y = random.nextInt(size);

            double q = 1;
            for (int sy = y - N + 1; sy <= y + N - 1; sy++)
            {
                for (int sx = x - N + 1; sx <= x + N - 1; sx++)
                {
                    int ind = 0, difference = 0;
                    for (int dy = 0; dy < N; dy++)
                    {
                        for (int dx = 0; dx < N; dx++)
                        {
                            int X = sx + dx;
                            if (X < 0) X += size;
                            else if (X >= size) X -= size;

                            int Y = sy + dy;
                            if (Y < 0) Y += size;
                            else if (Y >= size) Y -= size;

                            boolean value = field[X][Y];
                            int power = 1 << (dy * N + dx);
                            ind += value ? power : 0;
                            if (X == x && Y == y) difference = value ? power : -power;
                        }
                    }

                    q *= weights[ind - difference] / weights[ind];
                }
            }

            if (q >= 1) { field[x][y] ^= true; continue; }
            if (temperature != 1) q = Math.pow(q, 1.0 / temperature);
            field[x][y] ^= (q > random.nextDouble());
        }

        return field;
    }

    /**
     * The main part of MimicFill; generates a 1D boolean array that, when used correctly, mimics the patterns present
     * in the 2D boolean array sample, but can produce a larger or smaller output 1D array than the sample. Using the
     * output correctly mostly involves passing the 1D boolean array with the correct width and height settings to
     * {@link #sampleToMap(boolean[], int, int, char, char)} to get a 2D char array or producing a GreasedRegion with it
     * using {@link squidpony.squidmath.GreasedRegion#GreasedRegion(boolean[], int, int)}. Both the width and height
     * used to interpret the 1D array as a 2D array should be equal to the size parameter passed here. The main reason
     * you would prefer this method over {@link #fill(boolean[][], int, double, int, IRNG)} is that this overload is
     * somewhat faster due to slightly less-frequent RNG calls and a lot less nested array reading and writing.
     * <br>
     * Takes a 2D boolean array as a "sample" (often one of the constants in this class), a size parameter for the
     * generated square data (same width and height) and several other parameters that affect how closely the output
     * will match the input. The temperature is how "agitated" or "chaotic" the changes to the map can be; 0.2 is
     * definitely recommended for map-like data but other values (between 0 and 1, both exclusive) may be better fits
     * for certain kinds of output. The iterations parameter should usually be between 3 and 5, with higher values
     * taking longer but fitting more closely; values over 5 are barely different from 5 here. This also takes an RNG,
     * and because it queries it very frequently, a fast RNG is ideal; the default RandomnessSource used by RNG
     * ({@link LightRNG}) is fine, and in most cases {@link squidpony.squidmath.ThrustAltRNG} will be reasonably good
     * for quality while probably being slightly faster. On GWT, {@link squidpony.squidmath.Zag32RNG} is a good choice
     * due to how few operations on {@code long} values it needs to do, and GWT has a hard time with longs.
     * @param sample a 2D boolean array to mimic visually; you can use mapToSample() if you have a 2D char array
     * @param size the side length of the square boolean array to generate
     * @param temperature typically 0.2 works well for this, but other numbers between 0 and 1 may work
     * @param iterations typically 3-5 works well for this; lower numbers may have slight problems with quality,
     *                   and higher numbers make this slightly slower
     * @param random an RNG to use for the random components of this technique
     * @return a new 1D boolean array, length = size * size, mimicking the visual style of sample when used as 2D data
     */
    public static boolean[] fillSolo(boolean[][] sample, int size, double temperature, int iterations, IRNG random)
    {
        int fieldLen = size*size;
        boolean[] field = new boolean[fieldLen];
        double[] weights = new double[1 << (N * N)];
        for (int x = 0; x < sample.length; x++) {
            for (int y = 0; y < sample[x].length; y++) {
                weights[Pattern.index(sample, x, y, N, false, false, false)]++;
                weights[Pattern.index(sample, x, y, N, false, true, true)]++;
                weights[Pattern.index(sample, x, y, N, true, true, false)]++;
                weights[Pattern.index(sample, x, y, N, true, false, true)]++;
                weights[Pattern.index(sample, x, y, N, true, false, false)]++;
                weights[Pattern.index(sample, x, y, N, true, true, true)]++;
                weights[Pattern.index(sample, x, y, N, false, true, false)]++;
                weights[Pattern.index(sample, x, y, N, false, false, true)]++;
            }
        }

        for (int k = 0; k < weights.length; k++)
        {
            if (weights[k] <= 0)
                weights[k] = 0.1;
        }
        long randomBits;
        for (int o = fieldLen + 63 >> 6, idx = 0; o > 0; o--) {
            randomBits = random.nextLong();
            for (long a = 1; a != 0; a <<= 1) {
                field[idx++] = (randomBits & a) == 0;
            }
        }
        for (int k = 0; k < iterations * fieldLen; k++)
        {
            final int r = random.nextInt(fieldLen);
            final int x = r % size, y = r / size;


            double q = 1;
            for (int sy = y - N + 1; sy <= y + N - 1; sy++)
            {
                for (int sx = x - N + 1; sx <= x + N - 1; sx++)
                {
                    int ind = 0, difference = 0;
                    for (int dy = 0; dy < N; dy++)
                    {
                        for (int dx = 0; dx < N; dx++)
                        {
                            int X = sx + dx;
                            if (X < 0) X += size;
                            else if (X >= size) X -= size;

                            int Y = sy + dy;
                            if (Y < 0) Y += size;
                            else if (Y >= size) Y -= size;

                            boolean value = field[X + size * Y];
                            int power = 1 << (dy * N + dx);
                            ind += value ? power : 0;
                            if (X == x && Y == y) difference = value ? power : -power;
                        }
                    }

                    q *= weights[ind - difference] / weights[ind];
                }
            }

            if (q >= 1) { field[r] ^= true; continue; }
            if (temperature != 1) q = Math.pow(q, 1.0 / temperature);
            field[r] ^= q > random.nextDouble();
        }

        return field;
    }
    private static class Pattern {
        public boolean[][] data;


        Pattern(boolean[][] exact) {
            data = exact;
        }

        Pattern(boolean[][] field, int x, int y, int size) {
            data = new boolean[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    data[i][j] =
                            field[(x + i + field.length) % field.length][(y + j + field[0].length) % field[0].length];
                }
            }
        }

        Pattern rotate() {
            boolean[][] next = new boolean[data.length][data.length];
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data.length; y++) {
                    next[data.length - 1 - y][x] = data[x][y];
                }
            }
            return new Pattern(next);
        }

        Pattern reflect() {
            boolean[][] next = new boolean[data.length][data.length];
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data.length; y++) {
                    next[data.length - 1 - x][y] = data[x][y];
                }
            }
            return new Pattern(next);
        }

        int index() {
            int result = 0, power = 1;
            for (int y = 0; y < data.length; y++) {
                for (int x = 0; x < data.length; x++) {
                    result += data[data.length - 1 - x][data.length - 1 - y] ? power : 0;
                    power <<= 1;
                }
            }
            return result;
        }

        static int index(boolean[][] field, int x, int y, int size) {
            int result = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (field[(x + i + field.length) % field.length][(y + j + field[0].length) % field[0].length])
                        result += 1 << (j * size + i);
                }
            }
            return result;
        }
        
        static int index(boolean[][] field, int x, int y, int size, boolean flipX, boolean flipY, boolean swap) {
            int result = 0;
            int width = field.length, height = field[0].length;
            if(swap)
            {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if(field[(height * 2 + (flipY ? -(y + j) : (y + j))) % height]
                                [(width * 2 + (flipX ? -(x + i) : (x + i))) % width])
                            result += 1 << (j * size + i);
                    }
                }
            }
            else 
            {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if(field[(width * 2 + (flipX ? -(x + i) : (x + i))) % width]
                                [(height * 2 + (flipY ? -(y + j) : (y + j))) % height])
                            result += 1 << (j * size + i);
                    }
                }
            }
            return result;
        }
    }

    /**
     * Predefined sample; many small separate squares.
     */
    public static final boolean[][] boulders = new boolean[][]{
            new boolean[]{true,true,true,true,true,true,true,true,true,true,true,false,false,true,true,true},
            new boolean[]{true,true,false,false,true,true,false,false,true,true,true,false,false,true,true,true},
            new boolean[]{true,true,false,false,true,true,false,false,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,true,false,false,true,true,false,false,true},
            new boolean[]{true,true,true,false,false,true,false,false,true,false,false,true,true,false,false,true},
            new boolean[]{true,true,true,false,false,true,false,false,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,true,true,false,false,true,false,false,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,false,false,true,false,false,true,true,true,false,false,true,true},
            new boolean[]{true,false,false,true,true,true,true,true,true,true,true,true,false,false,true,true},
            new boolean[]{true,false,false,true,false,false,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,false,false,true,true,true,false,false,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,true,false,false,true,false,false,true,true},
            new boolean[]{true,false,false,true,true,true,false,false,true,true,true,true,false,false,true,true},
            new boolean[]{true,false,false,true,true,true,false,false,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true}
    };

    /**
     * Predefined sample; a large, enclosed, organic space that usually makes large cave-like rooms,
     */
    public static final boolean[][] cave = new boolean[][]{
            new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            new boolean[]{false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false},
            new boolean[]{false, false, false, true, true, true, false, false, true, true, true, true, true, true, false, false},
            new boolean[]{false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false},
            new boolean[]{false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false},
            new boolean[]{false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, false},
            new boolean[]{false, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false},
            new boolean[]{false, false, false, false, true, true, true, true, true, true, true, true, false, false, false, false},
            new boolean[]{false, false, false, false, true, true, true, true, true, true, true, true, true, false, false, false},
            new boolean[]{false, false, false, true, true, true, true, true, true, true, true, true, true, true, false, false},
            new boolean[]{false, false, true, true, true, true, true, true, true, true, true, true, true, true, false, false},
            new boolean[]{false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, false},
            new boolean[]{false, false, false, true, true, true, true, true, false, false, true, true, true, true, true, false},
            new boolean[]{false, false, false, false, true, true, true, false, false, false, false, true, true, false, false, false},
            new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
            new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false}
    };

    /**
     * Predefined sample; several medium-sized organic spaces that usually make tight, chaotic tunnels.
     */
    public static final boolean[][] caves = new boolean[][]{
            new boolean[]{false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,false,false,true,true,false,false,false},
            new boolean[]{false,false,true,true,true,false,false,false,false,false,false,true,true,false,false,false},
            new boolean[]{false,true,true,true,true,true,false,false,false,false,true,true,true,true,false,false},
            new boolean[]{false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true},
            new boolean[]{false,false,true,true,true,true,true,false,false,false,true,true,true,true,false,false},
            new boolean[]{false,false,false,true,true,true,false,false,false,false,false,true,true,false,false,false},
            new boolean[]{false,false,false,false,true,true,false,false,false,false,false,true,false,false,false,false},
            new boolean[]{false,false,false,false,true,false,false,false,false,false,true,true,true,false,false,false},
            new boolean[]{false,false,true,true,true,true,false,false,false,true,true,true,true,true,false,false},
            new boolean[]{false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,true,true,true,true,false,false,true,true,true,true,true,true,true},
            new boolean[]{false,true,true,true,true,true,false,false,false,true,true,true,true,true,false,false},
            new boolean[]{false,false,true,true,true,true,false,false,false,false,true,true,true,true,false,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false}
    };

    /**
     * Predefined sample; a checkerboard pattern that typically loses recognition as such after generation.
     */
    public static final boolean[][] chess = new boolean[][]{
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true},
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true},
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true},
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true},
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true},
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true},
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true},
            new boolean[]{true,false,true,false,true,false,true,false,true,false,true,false,true,false,true,false},
            new boolean[]{false,true,false,true,false,true,false,true,false,true,false,true,false,true,false,true}
    };

    /**
     * Predefined sample; produces rectangular rooms with small corridors between them.
     */
    public static final boolean[][] lessRooms = new boolean[][]{
            new boolean[]{false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,false,false,false,false,false,false,true,true,true,true,true,true,true},
            new boolean[]{false,false,true,false,false,false,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,false,true,false,false,false,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false},
            new boolean[]{false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,false,false,false,true,false,false,false,false},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,false,true,true,true,true,true},
            new boolean[]{false,false,true,true,true,true,true,true,true,true,true,true,true,false,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,false,false,false,false,true,false,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,false,false,false,false,true,false,false,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false}
    };

    /**
     * Predefined sample; produces a suitable filler for a maze (but it is unlikely to connect both ends like a maze).
     */
    public static final boolean[][] maze = new boolean[][]{
            new boolean[]{true,true,true,true,true,false,false,false,false,true,true,true,false,true,true,false},
            new boolean[]{true,false,false,false,true,false,true,true,false,true,false,false,false,false,false,false},
            new boolean[]{true,false,true,true,true,false,true,false,false,false,false,true,true,true,false,true},
            new boolean[]{true,false,false,false,false,false,true,false,true,true,true,true,false,true,false,false},
            new boolean[]{true,true,true,false,true,true,true,false,false,false,false,true,false,false,false,true},
            new boolean[]{false,false,false,false,false,false,true,true,true,true,false,true,false,true,false,false},
            new boolean[]{true,true,true,true,true,false,true,true,false,true,false,true,false,true,false,true},
            new boolean[]{false,false,false,false,false,false,true,true,false,true,true,true,false,true,false,true},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,false,false,false,true,false,false},
            new boolean[]{false,false,false,true,false,false,false,true,true,false,true,true,true,true,false,true},
            new boolean[]{true,true,false,true,false,true,true,true,false,false,false,false,false,false,false,false},
            new boolean[]{true,false,false,true,false,true,false,true,false,true,true,false,true,false,true,true},
            new boolean[]{true,true,true,true,false,false,false,true,false,false,true,false,true,false,false,true},
            new boolean[]{false,false,true,true,false,true,true,true,true,true,true,false,true,true,false,false},
            new boolean[]{true,false,false,false,false,false,false,false,false,true,true,false,false,true,true,false},
            new boolean[]{true,true,true,true,true,true,true,true,false,true,true,true,false,true,true,false}
    };

    /**
     * Predefined sample; produces weird, large areas of "true" and "false" that suddenly change to the other.
     */
    public static final boolean[][] quarterBlack = new boolean[][]{
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true}
    };

    /**
     * Predefined sample; produces multiple directions of flowing, river-like shapes made of "false".
     */
    public static final boolean[][] river = new boolean[][]{
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,false,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,false,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,false,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,false,false,false,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,false,false,false,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true}
    };

    /**
     * Predefined sample; produces rectangular rooms with a dense packing.
     */
    public static final boolean[][] rooms = new boolean[][]{
            new boolean[]{false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,true,true,true,true,true,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,true,true,true,true,true,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true},
            new boolean[]{false,true,true,true,true,true,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,false,true,false,false,false,false,false,false,true,true,true,true,true,true,false},
            new boolean[]{false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false},
            new boolean[]{false,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,false,false,true,true,true,true,false,false},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,true,true,true,true,true,true},
            new boolean[]{false,false,true,true,true,true,true,true,true,true,true,true,true,true,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,false,false,true,true,true,true,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,false,false,false,false,true,false,false,false},
            new boolean[]{false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,false}
    };

    /**
     * Predefined sample; produces an uncanny imitation of a maze with a tiny sample size.
     */
    public static final boolean[][] simpleMaze = new boolean[][]{
            new boolean[]{true,true,true,true},
            new boolean[]{true,false,false,false},
            new boolean[]{true,false,true,false},
            new boolean[]{true,false,false,false}
    };

    /**
     * Predefined sample; produces mostly rectangular rooms with very few corridor-like areas.
     */
    public static final boolean[][] simpleRooms = new boolean[][]{
            new boolean[]{false,false,false,false,false,true,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,true,false,false,false,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,true,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,true,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,true,false,false},
            new boolean[]{true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,true,true,true,true,true,true,true,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,true,false,false},
            new boolean[]{false,false,true,true,true,true,true,true,true,false,false},
            new boolean[]{false,false,false,false,false,true,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,true,false,false,false,false,false}
    };

    /**
     * Predefined sample; produces largely rectangular rooms with a good amount of thin corridors.
     */
    public static final boolean[][] thickWalls = new boolean[][]{
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,false,false,false,false},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,false,false,false,false},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,false,false,false,false},
            new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,false,false,false,false},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,false,false,false,false},
            new boolean[]{false,false,false,false,true,true,true,true,true,true,true,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,false,false,false,false,false,false,false}
    };

    public static final boolean[][] ruins = new boolean[][]{
            new boolean[]{false,true,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true},
            new boolean[]{false,true,false,false,false,false,false,true,true,false,false,false,false,false,false,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true},
            new boolean[]{false,true,false,false,false,false,false,false,true,false,false,false,false,false,false,true,true,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,true,true,true,true,true,false,false,true,false,false,false,false,false,false,true,true,true,true,true,true,false,false,false,false,false,false,false,true,false,false,true},
            new boolean[]{false,true,true,true,true,true,true,false,true,false,false,false,false,false,false,true,false,false,false,true,false,false,false,false,true,true,true,true,true,false,false,true},
            new boolean[]{false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,true,false,false,false,true,true,true,true,true,true,false,false,true},
            new boolean[]{true,true,true,true,true,true,false,false,false,false,true,true,true,true,true,true,true,true,true,true,false,false,false,false,true,true,true,true,true,true,true,true},
            new boolean[]{false,true,false,false,true,true,true,true,true,true,true,false,false,true,true,true,true,true,true,true,false,false,false,false,true,true,true,true,false,false,false,false},
            new boolean[]{false,true,false,false,false,false,false,false,false,false,true,true,false,true,true,true,true,true,true,false,false,false,false,false,false,false,true,true,false,false,false,false},
            new boolean[]{false,true,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,false,false,false,false},
            new boolean[]{true,true,true,false,false,false,true,true,true,true,true,true,true,true,false,false,true,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true},
            new boolean[]{true,true,true,false,false,false,false,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false},
            new boolean[]{true,true,true,false,false,false,false,true,true,true,true,true,true,true,false,false,false,false,true,true,false,false,false,false,false,true,true,true,false,false,false,false},
            new boolean[]{true,true,true,false,false,false,false,true,true,true,true,true,true,false,false,false,false,false,false,true,false,false,false,false,false,false,false,true,false,false,true,true},
            new boolean[]{false,false,false,false,false,false,false,true,true,true,true,false,true,false,false,true,false,false,false,true,false,false,false,false,false,false,false,true,true,true,true,true},
            new boolean[]{false,false,false,false,false,false,false,false,false,true,false,false,true,false,false,true,false,false,false,true,false,false,false,false,false,false,true,true,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,false,false,true,false,true,true,false,false,true,false,false,false,true,true,true,true,true,true,true,true,true,false,false,false,false},
            new boolean[]{false,false,false,false,true,false,false,false,false,true,false,false,true,false,false,true,false,false,false,true,true,true,false,true,true,true,true,true,false,false,false,false},
            new boolean[]{false,false,false,true,true,false,false,false,false,true,false,false,true,false,false,true,true,false,false,false,true,true,false,true,false,false,true,false,false,false,false,false},
            new boolean[]{false,false,false,true,true,false,false,false,false,true,false,false,true,false,false,true,true,false,false,false,false,true,false,true,false,false,true,false,false,false,false,false},
            new boolean[]{false,false,false,false,true,false,false,true,false,true,false,false,true,false,false,false,true,false,false,false,false,true,false,true,false,false,true,false,false,false,true,false},
            new boolean[]{false,true,false,false,true,true,true,true,true,true,true,false,true,false,false,false,true,false,false,false,false,true,false,true,true,true,true,false,false,false,true,false},
            new boolean[]{true,true,true,true,true,true,true,false,false,true,false,false,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,false,true,false,false,true,false,false,false,true,true,true,true,true,true,false,false,false,false,false,true,true,true,true,true},
            new boolean[]{false,false,false,false,false,false,true,false,false,true,false,false,true,false,false,false,false,true,false,false,true,true,false,false,false,false,false,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,true,false,false,true,true,true,true,true,false,false,false,false,false,false,true,true,false,false,false,false,true,true,true,false,false,false},
            new boolean[]{false,false,false,false,false,false,true,false,false,false,false,true,true,true,false,false,false,false,false,false,true,true,false,false,false,false,true,true,true,false,false,false},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,false,true,true,true,false,false,false,false,false,false,true,true,true,false,false,false,true,true,true,true,true,true},
            new boolean[]{false,false,false,false,false,false,true,true,true,true,true,true,true,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,false,false,true,false},
            new boolean[]{false,false,false,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,true,true,true,false,false,false},
            new boolean[]{false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,true,true,true,false,false,false},
            new boolean[]{false,true,false,false,false,false,false,true,true,false,false,false,false,false,false,true,true,true,true,false,false,false,false,false,true,true,true,true,true,true,true,true},
    };

    public static final boolean[][] openRooms = new boolean[][]{
            new boolean[]{true,true,true,true,true,true,false,true,true,true,true,true,true,false,true,true,true,true,true,true,false,false,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,false,false,false,true,true,true,false,false,true,true,true,true,true,true,true,true,true,true,false,true,true,false,true,true,true,false,true,true},
            new boolean[]{true,true,false,false,false,false,false,true,true,true,true,false,true,true,true,true,true,false,false,false,false,false,false,true,true,false,true,true,true,false,true,true},
            new boolean[]{true,true,true,true,true,false,false,false,false,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,true,false,true,true,true,false,false,false},
            new boolean[]{true,true,true,true,true,false,false,false,false,true,true,false,false,false,false,true,true,true,true,true,true,true,true,true,true,false,true,true,true,true,false,false},
            new boolean[]{true,true,true,true,true,false,true,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true,false,false,false,false,false,false,true,true,false,false},
            new boolean[]{true,true,true,true,true,false,true,true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,false,true,true,true,true,false,false,false,false,false},
            new boolean[]{true,true,true,true,true,false,true,true,false,false,true,true,true,true,false,false,false,true,true,true,true,true,false,true,true,true,true,false,false,false,false,false},
            new boolean[]{true,true,true,true,true,false,true,true,false,false,false,false,false,true,true,true,true,true,true,true,true,true,false,true,true,true,true,false,false,false,false,false},
            new boolean[]{true,true,true,true,true,false,true,true,false,false,false,false,false,true,true,true,true,true,true,true,true,true,false,true,true,true,true,false,false,false,false,false},
            new boolean[]{false,false,false,false,false,false,true,true,false,true,true,true,false,false,false,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true,false,false},
            new boolean[]{true,true,true,true,true,true,true,true,false,true,true,true,false,false,false,false,true,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,false,false,false,false,false,true,true,false,true,true,true,true,true,true,true,true,true},
            new boolean[]{false,true,true,true,true,true,false,false,false,true,true,true,true,true,true,false,false,false,false,false,true,true,false,false,false,false,false,false,false,true,true,false},
            new boolean[]{false,true,true,false,false,false,false,false,true,true,true,true,true,true,true,false,true,true,false,false,false,false,false,false,false,false,false,false,false,true,true,false},
            new boolean[]{false,true,true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,true,true,true,true,true,true,true,true,false},
            new boolean[]{false,true,true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,true,true,false,true,true,true,true,true,true,true,true,false},
            new boolean[]{false,false,false,false,false,true,true,true,true,true,true,true,false,true,true,true,true,true,false,true,true,true,false,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,false,true,true,true,true,true,false,false,false,false,false,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false},
            new boolean[]{true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,false,false,false,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,true,false,false,false,false,true,true,true,true,true,false,true,true,true,true,true,true,false,false,false,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,true,false,false,false,false,true,true,true,true,true,false,true,true,true,false,false,false,false,false,false,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,false,true,true,true,false,true,true,true,true,false,true,true,true,true},
            new boolean[]{true,true,true,true,true,true,true,false,false,false,true,true,true,true,true,true,true,true,false,true,true,true,false,true,true,true,true,false,false,false,true,true},
            new boolean[]{true,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,true,false,false,false,false,false,true,true,true,true,false,false,false,true,true},
            new boolean[]{true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,false,false,false,false,false,false,false,false,true,true},
            new boolean[]{true,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,true,true,true,true,true,true,true,true,true,true},
            new boolean[]{true,false,false,false,false,false,false,true,true,true,true,true,true,false,false,false,false,false,true,true,false,false,true,true,true,true,true,true,true,true,true,true},
    };

    public static final boolean[][][] samples = new boolean[][][]{
            boulders, cave, caves, chess, lessRooms, maze, quarterBlack,
            river, rooms, simpleMaze, simpleRooms, thickWalls, ruins, openRooms
    };

    /*
    // this would throw an ArrayIndexOutOfBoundsException before a fix added after beta 8
    public static void main(String[] args) {
        boolean[][] binarySample = new boolean[20][20];
        int length = 100;
        boolean[][] binaryOut = MimicFill.fill(binarySample, length, .01, 5, new RNG());
        squidpony.squidgrid.mapping.DungeonUtility.debugPrint(
                squidpony.squidgrid.mapping.DungeonUtility.hashesToLines(sampleToMap(binaryOut, '.', '#')));
    }
    */
}