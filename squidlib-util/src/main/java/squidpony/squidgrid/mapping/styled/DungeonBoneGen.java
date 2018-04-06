package squidpony.squidgrid.mapping.styled;

import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.RNG;

import java.util.Random;

/**
 * Generate a dungeon using Sean T. Barrett's Herringbone Wang Tiles method. http://nothings.org/gamedev/herringbone/
 * Created by Tommy Ettinger on 3/10/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DungeonBoneGen {

    /**
     * The current {@link IRNG}, a random number generator that can be seeded initially, and is usually an {@link RNG}.
     */
    public IRNG rng;
    private int[][] c_color, h_color, v_color;
    private int wide = 20;
    private int high = 20;
    /**
     * A GreasedRegion that, after {@link #generate(TilesetType, int, int)} has been called, will hold the floor cells
     * in its data as "on" cells and walls as "off" cells. This can be useful for inter-operating with code that expects
     * a GreasedRegion, which can be for various reasons.
     */
    public GreasedRegion region = new GreasedRegion(1, 1);
    /**
     * Not recommended for general usage; a GreasedRegion that is frequently modified by this generator and is kept
     * in a field so this and potentially other classes can avoid allocating new GreasedRegions with
     * {@link GreasedRegion#remake(GreasedRegion)} or the various refill methods in GreasedRegion.
     */
    public transient GreasedRegion workingRegion = new GreasedRegion(1, 1);

    /**
     * Gets the current RNG.
     * @return
     */
    public IRNG getRng() {
        return rng;
    }

    /**
     * Sets the current RNG.
     * @param rng
     */
    public void setRng(RNG rng) {
        this.rng = rng;
    }

    /**
     * Returns the width, used as the first coordinate in any char[][] in this class.
     * @return
     */
    public int getWidth() {
        return wide;
    }

    /**
     * Returns the height, used as the second coordinate in any char[][] in this class.
     * @return
     */
    public int getHeight() {
        return high;
    }

    /**
     * Get the char[][] dungeon that was last returned by generate(), or null if generate() or setDungeon have not been
     * called. Uses x,y indexing.
     * @return
     */
    public char[][] getDungeon() {
        return dungeon;
    }

    /**
     * Change the stored char[][] dungeon, using x,y indexing.
     * @param dungeon
     */
    public void setDungeon(char[][] dungeon) {
        this.dungeon = dungeon;
        wide = dungeon.length;
        high = dungeon[0].length;
    }

    /**
     * Gets the char at a given x,y position.
     * @param x
     * @param y
     * @return
     */
    public char get(int x, int y) {
        return dungeon[x][y];
    }

    /**
     * Sets the char at the given x,y position, storing it in this object. The dungeon this modifies is accessible with
     * getDungeon() and can be set all at once with setDungeon().
     * @param elem
     * @param x
     * @param y
     */
    public void put(char elem, int x, int y) {
        dungeon[x][y] = elem;
    }

    /**
     * The latest result of calling this class' generate() method.
     */
    private char[][] dungeon;

    /**
     * Constructs a DungeonBoneGen that uses the given java.util.Random .
     *
     * @param random A Random number generator to be used during the dungeon generation; it will
     *               be used to generate a seed for the internal RNG this class uses.
     */
    public DungeonBoneGen(Random random) {
        this(new RNG(random.nextLong()));
    }
    /**
     * Constructs a DungeonBoneGen that uses the given squidpony.squidmath.RNG.
     *
     * @param random A squidpony.squidmath.RNG to be used during the dungeon generation.
     */
    public DungeonBoneGen(IRNG random) {
        rng = random;
        c_color = new int[1][1];
		h_color = new int[1][1];
		v_color = new int[1][1];
    }

    /**
     * Constructs a DungeonBoneGen that uses a default RNG, randomly seeded.
     */
    public DungeonBoneGen() {
        this(new RNG());
    }

    /*
    private char[][] insert(char[][] mat, String[] items, int coord1, int coord2) {
        if (mat.length == 0 || items.length == 0 || items[0].length() == 0)
            return mat;

        for (int i = coord1, i1 = 0; i1 < items.length; i++, i1++) {
            char[] car = items[i1].toCharArray();
            for (int j = coord2, j2 = 0; j2 < car.length; j++, j2++) {
                if (i < 0 || j < 0 || i >= mat.length || j >= mat[i].length)
                    continue;
                mat[i][j] = car[j2];
            }
        }
        return mat;

    }
    */
    private Tile chooseTile(Tile[] list, int numlist, int[] y_positions, int[] x_positions) {
        int a = c_color[y_positions[0]][x_positions[0]];
        int b = c_color[y_positions[1]][x_positions[1]];
        int c = c_color[y_positions[2]][x_positions[2]];
        int d = c_color[y_positions[3]][x_positions[3]];
        int e = c_color[y_positions[4]][x_positions[4]];
        int f = c_color[y_positions[5]][x_positions[5]];
        int i, n, match = Integer.MAX_VALUE, pass;
        for (pass = 0; pass < 2; ++pass) {
            n = 0;
            // pass #1:
            //   count number of variants that match this partial set of constraints
            // pass #2:
            //   stop on randomly selected match
            for (i = 0; i < numlist; ++i) {
                Tile tile = list[i];
                if ((a < 0 || a == tile.a_constraint) &&
                        (b < 0 || b == tile.b_constraint) &&
                        (c < 0 || c == tile.c_constraint) &&
                        (d < 0 || d == tile.d_constraint) &&
                        (e < 0 || e == tile.e_constraint) &&
                        (f < 0 || f == tile.f_constraint)) {
                    n += 1;
                    if (n > match) {
                        // use list[i]
                        // update constraints to reflect what we placed
                        c_color[y_positions[0]][x_positions[0]] = tile.a_constraint;
                        c_color[y_positions[1]][x_positions[1]] = tile.b_constraint;
                        c_color[y_positions[2]][x_positions[2]] = tile.c_constraint;
                        c_color[y_positions[3]][x_positions[3]] = tile.d_constraint;
                        c_color[y_positions[4]][x_positions[4]] = tile.e_constraint;
                        c_color[y_positions[5]][x_positions[5]] = tile.f_constraint;
                        return tile;
                    }
                }
            }
            if (n == 0) {
                return null;
            }
            match = rng.nextInt(n);
        }
        return null;
    }

    private Tile chooseTile(Tile[] list, int numlist, boolean upright, int[] y_positions, int[] x_positions) {
        int a, b, c, d, e, f;
        if (upright) {
            a = h_color[y_positions[0]][x_positions[0]];
            b = v_color[y_positions[1]][x_positions[1]];
            c = v_color[y_positions[2]][x_positions[2]];
            d = v_color[y_positions[3]][x_positions[3]];
            e = v_color[y_positions[4]][x_positions[4]];
            f = h_color[y_positions[5]][x_positions[5]];
        } else {
            a = h_color[y_positions[0]][x_positions[0]];
            b = h_color[y_positions[1]][x_positions[1]];
            c = v_color[y_positions[2]][x_positions[2]];
            d = v_color[y_positions[3]][x_positions[3]];
            e = h_color[y_positions[4]][x_positions[4]];
            f = h_color[y_positions[5]][x_positions[5]];
        }
        int i, n, match = Integer.MAX_VALUE, pass;
        for (pass = 0; pass < 2; ++pass) {
            n = 0;
            // pass #1:
            //   count number of variants that match this partial set of constraints
            // pass #2:
            //   stop on randomly selected match
            for (i = 0; i < numlist; ++i) {
                Tile tile = list[i];
                if ((a < 0 || a == tile.a_constraint) &&
                        (b < 0 || b == tile.b_constraint) &&
                        (c < 0 || c == tile.c_constraint) &&
                        (d < 0 || d == tile.d_constraint) &&
                        (e < 0 || e == tile.e_constraint) &&
                        (f < 0 || f == tile.f_constraint)) {
                    n += 1;
                    if (n > match) {
                        // use list[i]
                        // update constraints to reflect what we placed
                        if (upright) {
                            h_color[y_positions[0]][x_positions[0]] = tile.a_constraint;
                            v_color[y_positions[1]][x_positions[1]] = tile.b_constraint;
                            v_color[y_positions[2]][x_positions[2]] = tile.c_constraint;
                            v_color[y_positions[3]][x_positions[3]] = tile.d_constraint;
                            v_color[y_positions[4]][x_positions[4]] = tile.e_constraint;
                            h_color[y_positions[5]][x_positions[5]] = tile.f_constraint;
                        } else {
                            h_color[y_positions[0]][x_positions[0]] = tile.a_constraint;
                            h_color[y_positions[1]][x_positions[1]] = tile.b_constraint;
                            v_color[y_positions[2]][x_positions[2]] = tile.c_constraint;
                            v_color[y_positions[3]][x_positions[3]] = tile.d_constraint;
                            h_color[y_positions[4]][x_positions[4]] = tile.e_constraint;
                            h_color[y_positions[5]][x_positions[5]] = tile.f_constraint;
                        }
                        return tile;
                    }
                }
            }
            if (n == 0) {
                return null;
            }
            match = rng.nextInt(n);
        }
        return null;
    }

    /**
     * Generate a dungeon given a TilesetType enum.
     * The main way of generating dungeons with DungeonBoneGen.
     * Consider using DungeonBoneGen.wallWrap to surround the edges with walls.
     * Assigns the returned result to a member of this class, 'dungeon'.
     *
     * @param tt A TilesetType enum; try lots of these out to see how they look.
     * @param w  Width of the dungeon to generate in chars.
     * @param h  Height of the dungeon to generate in chars.
     * @return A row-major char[][] with h rows and w columns; it will be filled with '#' for walls and '.' for floors.
     */
    public char[][] generate(TilesetType tt, int w, int h) {
    	return generate(tt.getTileset(), w, h);
    }

    /**
     * Changes the outer edge of a char[][] to the wall char, '#'.
     *
     * @param map A char[][] that stores map data.
     * @return
     */
    public static char[][] wallWrap(char[][] map) {
        int upperY = map[0].length - 1;
        int upperX = map.length - 1;
        for (int i = 0; i < map.length; i++) {
            map[i][0] = '#';
            map[i][upperY] = '#';
        }
        for (int i = 0; i < map[0].length; i++) {
            map[0][i] = '#';
            map[upperX][i] = '#';
        }
        return map;
    }

    /**
     * Changes the outer edge of this dungeon to the wall char, '#'.
     *
     * @return The modified dungeon, a char[][].
     */
    public char[][] wallWrap() {
        int upperY = high - 1;
        int upperX = wide - 1;
        for (int i = 0; i < wide; i++) {
            dungeon[i][0] = '#';
            dungeon[i][upperY] = '#';
        }
        for (int i = 0; i < high; i++) {
            dungeon[0][i] = '#';
            dungeon[upperX][i] = '#';
        }
        return dungeon;
    }

    private boolean matchingAdjacent(int y, int x)
    {
        return c_color[y][x] == c_color[y + 1][x + 1];
    }

    private int changeColor(int old_color, int num_options) {

        int offset = 1 + rng.nextInt(num_options - 1);
        return (old_color + offset) % num_options;
    }

    /**
     * Generate a dungeon given a Tileset.
     * If you have your own Tileset gained by parsing your own JSON, use
     * this to generate a dungeon using it. Consider using DungeonBoneGen.wallWrap
     * to surround the edges with walls. Assigns the returned result to a member
     * of this class, 'dungeon'.
     *
     * @param ts A Tileset; if you don't have one of these available, use a TilesetType enum instead to select a predefined one.
     * @param h  Height of the dungeon to generate in chars.
     * @param w  Width of the dungeon to generate in chars.
     * @return A row-major char[][] with h rows and w columns; it will be filled with '#' for walls and '.' for floors.
     */
    public char[][] generate(Tileset ts, int w, int h) {
        wide = Math.max(1, w);
        high = Math.max(1, h);
        region.resizeAndEmpty(wide, high);
        workingRegion.resizeAndEmpty(wide, high);
        int sidelen = ts.config.short_side_length;
        int xmax = (wide / sidelen) + 6;
        int ymax = (high / sidelen) + 6;
        if (xmax > 1006) {
            return null;
        }
        if (ymax > 1006) {
            return null;
        }
        if (ts.config.is_corner) {
            c_color = new int[ymax][xmax];
            int i = 0, j = 0, ypos = -1 * sidelen;
            int[] cc = ts.config.num_colors;

            for (j = 0; j < ymax; ++j) {
                for (i = 0; i < xmax; ++i) {
                    c_color[j][i] = rng.nextInt(cc[(i - j + 1) & 3]); // select from cc based on corner type
                }
            }

            // Repetition reduction
            // now go back through and make sure we don't have adjacent 3x2 vertices that are identical,
            // to avoid really obvious repetition (which happens easily with extreme weights)
            for (j = 0; j < ymax - 3; ++j) {
                for (i = 0; i < xmax - 3; ++i) {
                    int p = (i - j + 1) & 3; // corner type
                    if (i + 3 >= 1006) {
                        return null;
                    }
                    if (j + 3 >= 1006) {
                        return null;
                    }
                    if (matchingAdjacent(j, i) && matchingAdjacent(j + 1, i) && matchingAdjacent(j + 2, i)
                            && matchingAdjacent(j, i + 1) && matchingAdjacent(j + 1, i + 1) && matchingAdjacent(j + 2, i + 1)) {
                        p = ((i + 1) - (j + 1) + 1) & 3;
                        if (cc[p] > 1)
                            c_color[j + 1][i + 1] = changeColor(c_color[j + 1][i + 1], cc[p]);
                    }

                    if (matchingAdjacent(j, i) && matchingAdjacent(j, i + 1) && matchingAdjacent(j, i + 2)
                            && matchingAdjacent(j + 1, i) && matchingAdjacent(j + 1, i + 1) && matchingAdjacent(j + 1, i + 2)) {
                        p = ((i + 2) - (j + 1) + 1) & 3;
                        if (cc[p] > 1)
                            c_color[j + 1][i + 2] = changeColor(c_color[j + 1][i + 2], cc[p]);
                    }
                }
            }


            for (j = -1; ypos < high; ++j) {
                // a general herringbone row consists of:
                //    horizontal left block, the bottom of a previous vertical, the top of a new vertical
                int phase = j & 3;
                // displace horizontally according to pattern
                if (phase == 0) {
                    i = 0;
                } else {
                    i = phase - 4;
                }
                for (; ; i += 4) {
                    int xpos = i * sidelen;
                    if (xpos >= wide)
                        break;
                    // horizontal left-block
                    if (xpos + sidelen * 2 >= 0 && ypos >= 0) {
                        Tile t = chooseTile(
                                ts.h_tiles, ts.h_tiles.length,
                                new int[]{j + 2, j + 2, j + 2, j + 3, j + 3, j + 3},
                                new int[]{i + 2, i + 3, i + 4, i + 2, i + 3, i + 4});

                        if (t == null)
                            return null;

                        //trans_output = insert(trans_output, t.data, ypos, xpos);
                        region.or(workingRegion.refill(t.data, t.width, t.height, wide, high).translate(xpos, ypos));
                    }
                    xpos += sidelen * 2;
                    // now we're at the end of a previous vertical one
                    xpos += sidelen;
                    // now we're at the start of a new vertical one
                    if (xpos < wide) {
                        Tile t = chooseTile(
                                ts.v_tiles, ts.v_tiles.length,
                                new int[]{j + 2, j + 3, j + 4, j + 2, j + 3, j + 4},
                                new int[]{i + 5, i + 5, i + 5, i + 6, i + 6, i + 6});

                        if (t == null)
                            return null;
                        //trans_output = insert(trans_output, t.data, ypos, xpos);
                        region.or(workingRegion.refill(t.data, t.width, t.height, wide, high).translate(xpos, ypos));
                    }
                }
                ypos += sidelen;
            }
        } else {
            int i, j, ypos;
            v_color = new int[ymax][xmax];
            h_color = new int[ymax][xmax];
            for (int yy = 0; yy < ymax; yy++) {
                for (int xx = 0; xx < xmax; xx++) {
                    v_color[yy][xx] = -1;
                    h_color[yy][xx] = -1;
                }
            }

            ypos = -1 * sidelen;
            for (j = -1; ypos < high; ++j) {
                // a general herringbone row consists of:
                //    horizontal left block, the bottom of a previous vertical, the top of a new vertical
                int phase = j & 3;
                // displace horizontally according to pattern
                if (phase == 0) {
                    i = 0;
                } else {
                    i = phase - 4;
                }
                for (; ; i += 4) {
                    int xpos = i * sidelen;
                    if (xpos >= wide)
                        break;
                    // horizontal left-block
                    if (xpos + sidelen * 2 >= 0 && ypos >= 0) {
                        Tile t = chooseTile(
                                ts.h_tiles, ts.h_tiles.length, false,
                                new int[]{j + 2, j + 2, j + 2, j + 2, j + 3, j + 3},
                                new int[]{i + 2, i + 3, i + 2, i + 4, i + 2, i + 3});

                        if (t == null)
                            return null;
                        //trans_output = insert(trans_output, t.data, ypos, xpos);
                        region.or(workingRegion.refill(t.data, t.width, t.height, wide, high).translate(xpos, ypos));
                    }
                    xpos += sidelen * 2;
                    // now we're at the end of a previous vertical one
                    xpos += sidelen;
                    // now we're at the start of a new vertical one
                    if (xpos < wide) {
                        Tile t = chooseTile(
                                ts.v_tiles, ts.v_tiles.length, true,
                                new int[]{j + 2, j + 2, j + 2, j + 3, j + 3, j + 4},
                                new int[]{i + 5, i + 5, i + 6, i + 5, i + 6, i + 5});

                        if (t == null)
                            return null;
                        //trans_output = insert(trans_output, t.data, ypos, xpos);
                        region.or(workingRegion.refill(t.data, t.width, t.height, wide, high).translate(xpos, ypos));
                    }
                }
                ypos += sidelen;
            }
        }
        /*for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                output[x][y] = trans_output[y][x];
            }
        }*/
        dungeon = region.toChars();
        return dungeon;
    }

    /**
     * Provides a string representation of the latest generated dungeon.
     *
     * @return
     */
    @Override
	public String toString() {
        char[][] trans = new char[high][wide];
        for (int x = 0; x < wide; x++) {
            for (int y = 0; y < high; y++) {
                trans[y][x] = dungeon[x][y];
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int row = 0; row < high; row++) {
            sb.append(trans[row]);
            sb.append('\n');
        }
        return sb.toString();
    }

    /*
     * Gets an array of all herringbone tiles associated with a TilesetType enum.
     *
     * @param tt a TilesetType enum
     * @return an array of 2D char arrays representing tiles
     * /
    public String[][] getTiles(TilesetType tt) {
        final Tileset ts = tt.getTileset();

        String[][] result = new String[ts.h_tiles.length + ts.v_tiles.length][];
        for (int i = 0; i < ts.h_tiles.length; i++) {
            result[i] = ts.h_tiles[i].data;
        }
        for (int i = 0; i < ts.v_tiles.length; i++) {
            result[ts.h_tiles.length + i] = ts.v_tiles[i].data;
        }
        return result;
    }*/
}
