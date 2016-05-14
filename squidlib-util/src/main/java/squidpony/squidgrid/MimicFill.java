package squidpony.squidgrid;

import squidpony.squidmath.RNG;

/**
 * A class that imitates patterns in an existing 2D boolean array and uses it to generate a new boolean array with a
 * similar visual style. Useful for creating procedural filler around a desired path or series of known rooms. Can also
 * convert between 2D boolean arrays (samples) and 2D char arrays (maps).
 * Created by Tommy Ettinger on 5/14/2016, porting from https://github.com/mxgmn/ConvChain (public domain)
 */
public class MimicFill {

    private static final int N = 3;

    // We will want predefined patterns at some point
    //public static final boolean[][] something = new boolean[4][4];

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
                    if(sample[x][y] = (map[x][y] == yes[c]))
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
     *
     * @param sample a 2D boolean array to mimic visually; you can use mapToSample() if you have a 2D char array
     * @param size the side length of the square boolean array to generate
     * @param temperature typically 0.2 works well for this, but other numbers between 0 and 1 may work
     * @param iterations typically 3 works well for this; lower numbers may have slight problems with quality,
     *                   and higher numbers make this slower
     * @param random an RNG to use for the random components of this technique
     * @return a new 2D boolean array, width = size, height = size, mimicking the visual style of sample
     */
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
            int result = 0;
            for (int y = 0; y < data.length; y++) {
                for (int x = 0; x < data.length; x++) {
                    result += data[x][y] ? 1 << (y * data.length + x) : 0;
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
    }
}
