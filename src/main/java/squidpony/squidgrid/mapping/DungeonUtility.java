package squidpony.squidgrid.mapping;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.RNG;

import java.awt.Point;

/**
 * A static class that can be used to modify the char[][] dungeons that other generators produce.
 * Includes various utilities for random floor-finding, but also provides ways to take dungeons that use '#'
 * for walls and make a copy that uses unicode box drawing characters.
 * @see squidpony.squidgrid.mapping.DungeonGenerator
 * Created by Tommy Ettinger on 4/1/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DungeonUtility {
    /**
     * The random number generator that will be used for all methods in this class with a random component.
     */
    public static RNG rng = new RNG(new LightRNG());
    /**
     * Finds a random java.awt.Point where the x and y match up to a [x][y] location on map that has '.' as a value.
     * Uses this class' rng field for pseudo-random number generation.
     * @param map
     * @return a Point that corresponds to a '.' in map, or null if a '.' cannot be found or if map is too small.
     */
    public static Point randomFloor(char[][] map)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 3 || height < 3)
            return null;
        Point pt = new Point(rng.nextInt(width), rng.nextInt(height));
        for(int i = 0; i < 20; i++)
        {
            if(map[pt.x][pt.y] == '.')
            {
                return pt;
            }
            else
            {
                pt.x = rng.nextInt(width);
                pt.y = rng.nextInt(height);
            }
        }
        pt.x = 1;
        pt.y = 1;
        if(map[pt.x][pt.y] == '.')
            return pt;

        while(map[pt.x][pt.y] != '.')
        {
            pt.x += 1;
            if(pt.x >= width - 1)
            {
                pt.x = 1;
                pt.y += 1;
            }
            if(pt.y >= height - 1)
                return null;
        }
        return pt;
    }

    /**
     * Gets a random Point that is adjacent to start, validating whether the position can exist on the given map.
     * Adjacency defaults to four-way cardinal directions unless eightWay is true, in which case it uses Chebyshev.
     * This can step into walls, and should NOT be used for movement.  It is meant for things like sound that can
     * exist in walls, or for assigning decor to floors or walls that are adjacent to floors.
     * @param map
     * @param start
     * @param eightWay
     * @return
     */
    public static Point randomStep(char[][] map, Point start, boolean eightWay)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 3 || height < 3 || start.x <= 0 || start.y <= 0 || start.x >= width - 1 || start.y >= height - 1)
            return null;
        Point stepped = new Point(start.x, start.y);

        if(eightWay)
        {
            int mv = rng.nextInt(9);
            stepped.translate((mv % 3) - 1, (mv / 3) - 1);
        }
        else
        {
            int mv = rng.nextInt(5);
            switch (mv)
            {
                case 0: stepped.translate(-1, 0);
                    break;
                case 1: stepped.translate(1, 0);
                    break;
                case 2: stepped.translate(0, -1);
                    break;
                case 3: stepped.translate(0, 1);
                    break;
            }
        }
        return stepped;
    }
    /**
     * Finds a random java.awt.Point where the x and y match up to a [x][y] location on map that has '.' as a value,
     * and a square of cells extending in the positive x and y directions with a side length of size must also have
     * '.' as their values.
     * Uses this class' rng field for pseudo-random number generation.
     * @param map
     * @param size
     * @return a Point that corresponds to a '.' in map, or null if a '.' cannot be found or if map is too small.
     */
    public static Point randomFloorLarge(char[][] map, int size)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 4 || height < 4)
            return null;
        Point pt = new Point(rng.nextInt(width - size), rng.nextInt(height - size));
        CELL:
        for(int i = 0; i < 20; i++, pt.x = rng.nextInt(width - size), pt.y = rng.nextInt(height - size))
        {
            if(map[pt.x][pt.y] == '.')
            {
                for(int x = 0; x < size; x++)
                {
                    for(int y = 0; y < size; y++)
                    {
                        if(map[pt.x + x][pt.y + y] != '.')
                            continue CELL;
                    }
                }
                return pt;
            }
        }
        pt.x = 1;
        pt.y = 1;

        SLOW:
        while(true)
        {
            pt.x += 1;
            if(pt.x >= width - size)
            {
                pt.x = 1;
                pt.y += 1;
            }
            if(pt.y >= height - size)
                return null;
            if(map[pt.x][pt.y] == '.')
            {
                for(int x = 0; x < size; x++)
                {
                    for(int y = 0; y < size; y++)
                    {
                        if(map[pt.x + x][pt.y + y] != '.')
                            continue SLOW;
                    }
                }
                return pt;
            }
        }
    }

    /**
     * Takes a char[][] dungeon map that uses '#' to represent walls, and returns a new char[][] that uses unicode box
     * drawing characters to draw straight, continuous lines for walls, filling regions between walls (that were
     * filled with more walls before) with space characters, ' '. If the lines "point the wrong way," such as having
     * multiple horizontally adjacent vertical lines where there should be horizontal lines, call transposeLines() on
     * the returned map, which will keep the dimensions of the map the same and only change the line chars. You will
     * also need to call transposeLines if you call hashesToLines on a map that already has "correct" line-drawing
     * characters, which means hashesToLines should only be called on maps that use '#' for walls. If you have a
     * jumbled map that contains two or more of the following: "correct" line-drawing characters, "incorrect"
     * line-drawing characters, and '#' characters for walls, you can reset by calling linesToHashes() and then
     * potentially calling hashesToLines() again.
     * @param map
     * @return
     */
    public static char[][] hashesToLines(char[][] map)
    {
        int width = map[0].length+2;
        int height = map.length+2;

        char[][] neo = new char[height][width], dungeon = new char[height][width];
        for(int i = 1; i < height - 1; i++)
        {
            for(int j = 1; j < width - 1; j++)
            {
                dungeon[i][j] = map[i-1][j-1];
            }
        }
        for(int i = 0; i < height; i++)
        {
            neo[i][0] = '\1';
            neo[i][width-1] = '\1';
            dungeon[i][0] = '\1';
            dungeon[i][width-1] = '\1';
        }
        for(int i = 0; i < width; i++)
        {
            neo[0][i] = '\1';
            neo[height-1][i] = '\1';
            dungeon[0][i] = '\1';
            dungeon[height-1][i] = '\1';
        }

        for (int y = 1; y < height - 1; y++)
        {
            for (int x = 1; x < width - 1; x++)
            {
                if (map[y-1][x-1] == '#')
                {
                    int q = 0;
                    q |= (y <= 1 || map[y - 2][x-1] == '#') ? 1 : 0;
                    q |= (y <= 1 || x >= width - 2 || map[y - 2][x + 0] == '#') ? 2 : 0;
                    q |= (x >= width - 2  || map[y - 1][x + 0] == '#') ? 4 : 0;
                    q |= (y >= height - 2 || x >= width - 2 || map[y + 0][x + 0] == '#') ? 8 : 0;
                    q |= (y >= height - 2 || map[y + 0][x-1] == '#') ? 16 : 0;
                    q |= (y >= height - 2 || x <= 1 || map[y + 0][x - 2] == '#') ? 32 : 0;
                    q |= (x <= 1 || map[y - 1][x - 2] == '#') ? 64 : 0;
                    q |= (y <= 1 || x <= 1 || map[y - 2][x - 2] == '#') ? 128 : 0;

                    if (q == 0xff)
                    {
                        neo[y][x] = '\1';
                        dungeon[y][x] = '\1';
                    }
                    else
                    {
                        neo[y][x] = '#';
                    }
                }
                else
                {
                    neo[y][x] = dungeon[y][x];
                }
            }
        }

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                if (dungeon[y][x] == '#')
                {
                    boolean n = (y <= 0 || dungeon[y - 1][x] == '#' || dungeon[y - 1][x] == '+' || dungeon[y - 1][x] == '/');
                    boolean e = (x >= width - 1 || dungeon[y][x + 1] == '#' || dungeon[y][x + 1] == '+' || dungeon[y][x + 1] == '/');
                    boolean s = (y >= height - 1 || dungeon[y + 1][x] == '#' || dungeon[y + 1][x] == '+' || dungeon[y + 1][x] == '/');
                    boolean w = (x <= 0 || dungeon[y][x - 1] == '#' || dungeon[y][x - 1] == '+' || dungeon[y][x - 1] == '/');

                    if (n)
                    {
                        if (e)
                        {
                            if (s)
                            {
                                if (w)
                                {
                                    neo[y][x] = '┼';
                                }
                                else
                                {
                                    neo[y][x] = '├';
                                }
                            }
                            else if (w)
                            {
                                neo[y][x] = '┴';
                            }
                            else
                            {
                                neo[y][x] = '└';
                            }
                        }
                        else if (s)
                        {
                            if (w)
                            {
                                neo[y][x] = '┤';
                            }
                            else
                            {
                                neo[y][x] = '│';
                            }
                        }
                        else if (w)
                        {
                            neo[y][x] = '┘';
                        }
                        else
                        {
                            neo[y][x] = '│';
                        }
                    }
                    else if (e)  // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                    {
                        if (s)
                        {
                            if (w)
                            {
                                neo[y][x] = '┬';
                            }
                            else
                            {
                                neo[y][x] = '┌';
                            }
                        }
                        else if (w)
                        {
                            neo[y][x] = '─';
                        }
                        else
                        {
                            neo[y][x] = '─';
                        }
                    }
                    else if (s)
                    {
                        if (w)
                        {
                            neo[y][x] = '┐';
                        }
                        else
                        {
                            neo[y][x] = '│';
                        }
                    }
                    else if (w)
                    {
                        neo[y][x] = '─';
                    }
                    else
                    {
                        neo[y][x] = '─';
                    }

                }
                else
                {
                    neo[y][x] = dungeon[y][x];
                }
            }
        }
        //vertical crossbar removal
        for (int y = 1; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                if (neo[y][x] == '┼' || neo[y][x] == '├' || neo[y][x] == '┤' || neo[y][x] == '┴')
                {
                    if (neo[y - 1][x] == '┼' || neo[y - 1][x] == '├' || neo[y - 1][x] == '┤' || neo[y - 1][x] == '┬')
                    {
                        if ((x >= width - 1 || dungeon[y - 1][x + 1] == '#' || dungeon[y - 1][x + 1] == '\1' || dungeon[y - 1][x + 1] == '+' || dungeon[y - 1][x + 1] == '/') &&
                                (x <= 0 || dungeon[y - 1][x - 1] == '#' || dungeon[y - 1][x - 1] == '\1' || dungeon[y - 1][x - 1] == '+' || dungeon[y - 1][x - 1] == '/') &&
                                (x >= width - 1 || dungeon[y][x + 1] == '#' || dungeon[y][x + 1] == '\1' || dungeon[y][x + 1] == '+' || dungeon[y][x + 1] == '/') &&
                                (x <= 0 || dungeon[y][x - 1] == '#' || dungeon[y][x - 1] == '\1' || dungeon[y][x - 1] == '+' || dungeon[y][x - 1] == '/'))
                        {
                            switch (neo[y][x])
                            {
                                case '┼':
                                    neo[y][x] = '┬';
                                    break;
                                case '├':
                                    neo[y][x] = '┌';
                                    break;
                                case '┤':
                                    neo[y][x] = '┐';
                                    break;
                                case '┴':
                                    neo[y][x] = '─';
                                    break;
                            }
                            switch (neo[y - 1][x])
                            {
                                case '┼':
                                    neo[y - 1][x] = '┴';
                                    break;
                                case '├':
                                    neo[y - 1][x] = '└';
                                    break;
                                case '┤':
                                    neo[y - 1][x] = '┘';
                                    break;
                                case '┬':
                                    neo[y - 1][x] = '─';
                                    break;

                            }
                        }
                    }
                }
            }
        }
        //horizontal crossbar removal
        for (int y = 0; y < height; y++)
        {
            for (int x = 1; x < width; x++)
            {
                // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                if (neo[y][x] == '┼' || neo[y][x] == '┤' || neo[y][x] == '┬' || neo[y][x] == '┴')
                {
                    if (neo[y][x - 1] == '┼' || neo[y][x - 1] == '├' || neo[y][x - 1] == '┬' || neo[y][x - 1] == '┴')
                    {
                        if ((y >= height - 1 || dungeon[y + 1][x - 1] == '#' || dungeon[y + 1][x - 1] == '\1' || dungeon[y + 1][x - 1] == '+' || dungeon[y + 1][x - 1] == '/') &&
                                (y <= 0 || dungeon[y - 1][x - 1] == '#' || dungeon[y - 1][x - 1] == '\1' || dungeon[y - 1][x - 1] == '+' || dungeon[y - 1][x - 1] == '/') &&
                                (y >= height - 1 || dungeon[y + 1][x] == '#' || dungeon[y + 1][x] == '\1' || dungeon[y + 1][x] == '+' || dungeon[y + 1][x] == '/') &&
                                (y <= 0 || dungeon[y - 1][x] == '#' || dungeon[y - 1][x] == '\1' || dungeon[y - 1][x] == '+' || dungeon[y - 1][x] == '/'))
                        {
                            switch (neo[y][x])
                            {
                                case '┼':
                                    neo[y][x] = '├';
                                    break;
                                case '┤':
                                    neo[y][x] = '│';
                                    break;
                                case '┬':
                                    neo[y][x] = '┌';
                                    break;
                                case '┴':
                                    neo[y][x] = '└';
                                    break;
                            }
                            switch (neo[y][x - 1])
                            {
                                case '┼':
                                    neo[y][x - 1] = '┤';
                                    break;
                                case '├':
                                    neo[y][x - 1] = '│';
                                    break;
                                case '┬':
                                    neo[y][x - 1] = '┐';
                                    break;
                                case '┴':
                                    neo[y][x - 1] = '┘';
                                    break;

                            }
                        }
                    }
                }
            }
        }
        char[][] portion = new char[height-2][width-2];
        for(int i = 1; i < height - 1; i++)
        {
            for(int j = 1; j < width - 1; j++)
            {
                switch (neo[i][j])
                {
                    case '\1':
                        portion[i - 1][j - 1] = ' ';
                        break;
                    default: // ┼┌┘
                        portion[i - 1][j - 1] = neo[i][j];
                }
            }
        }
        return transposeLines(portion);
    }

    /**
     * Reverses most of the effects of hashesToLines(). The only things that will not be reversed are the placement of
     * space characters in unreachable wall-cells-behind-wall-cells, which remain as spaces. This is useful if you
     * have a modified map that contains wall characters of conflicting varieties, as described in hashesToLines().
     * @param map
     * @return
     */
    public static char[][] linesToHashes(char[][] map)
    {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                        portion[i][j] = '#';
                        break;
                    default:
                        portion[i][j] = map[i][j];
                }
            }
        }
        return portion;
    }
    /**
     * If you call hashesToLines() on a map that uses [y][x] conventions instead of [x][y], it will have the lines not
     * connect as you expect. Use this function to change the directions of the box-drawing characters only, without
     * altering the dimensions in any way. This returns a new char[][], instead of modifying the parameter in place.
     * transposeLines is also needed if the lines in a map have become transposed when they were already correct;
     * calling this method on an incorrectly transposed map will change the directions on all of its lines.
     * @param map
     * @return
     */
    public static char[][] transposeLines(char[][] map)
    {

        int width = map[0].length;
        int height = map.length;
        char[][] portion = new char[height][width];
        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                        portion[i][j] = ' ';
                        break;
                    case '├':
                        portion[i][j] = '┬';
                        break;
                    case '┤':
                        portion[i][j] = '┴';
                        break;
                    case '┴':
                        portion[i][j] = '┤';
                        break;
                    case '┬':
                        portion[i][j] = '├';
                        break;
                    case '┐':
                        portion[i][j] = '└';
                        break;
                    case '└':
                        portion[i][j] = '┐';
                        break;
                    case '│':
                        portion[i][j] = '─';
                        break;
                    case '─':
                        portion[i][j] = '│';
                        break;
//                    case '├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─':
                    default: // ┼┌┘
                        portion[i][j] = map[i][j];
                }
            }
        }
        return portion;
    }

    /**
     * When a map is generated by DungeonGenerator with addDoors enabled, different chars are used for vertical and
     * horizontal doors ('+' for vertical and '/' for horizontal).  This makes all doors '+', which is useful if you
     * want '/' to be used for a different purpose and/or to distinguish open and closed doors.
     * @param map
     * @return
     */
    public static char[][] closeDoors(char[][] map)
    {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(map[i][j] == '/') portion[i][j] = '+';
                else portion[i][j] = map[i][j];

            }
        }
        return portion;
    }

    /**
     * Takes a dungeon map with either '#' as the only wall character or the unicode box drawing characters used by
     * hashesToLines(), and returns a new char[][] dungeon map with two characters per cell, mostly filling the spaces
     * next to non-walls with space characters, and only doing anything different if a box-drawing character would
     * continue into an adjacent cell, or if a '#' wall needs another '#' wall next to it. The recommended approach is
     * to keep both the original non-double-width map and the newly-returned double-width map, since the single-width
     * maps can be used more easily for pathfinding. If you need to undo this function, call unDoublewidth().
     * @param map
     * @return
     */
    public static char[][] doubleWidth(char[][] map)
    {
        int width = map.length;
        int height = map[0].length;
        char[][] paired = new char[width*2][height];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0, px = 0; x < width; x++, px += 2)
            {
                paired[px][y] = map[x][y];
                switch (paired[px][y])
                {
                    //                        case '┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─'
                    case '┼':
                    case '├':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '└':
                    case '─':
                        paired[px + 1][y] = '─';
                        break;
                    case '#':
                        paired[px + 1][y] = '#';
                        break;

                    default:
                        paired[px + 1][y] = ' ';
                        break;
                        /*
                    case '.':
                    case '┤':
                    case '┐':
                    case '┘':
                    case '│':
                         */
                }
            }
        }
        return paired;
    }

    /**
     * Takes a dungeon map that uses two characters per cell, and condenses it to use only the left (lower index)
     * character in each cell. This should (probably) only be called on the result of doubleWidth(), and will throw an
     * exception if called on a map with an odd number of characters for width, such as "#...#" .
     * @param map
     * @return
     */
    public static char[][] unDoublewidth(char[][] map)
    {
        int width = map.length;
        int height = map[0].length;
        if(width % 2 != 0)
            throw new IllegalArgumentException("Argument must be a char[width][height] with an even width.");
        char[][] unpaired = new char[width/2][height];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0, px = 0; px < width; x++, px += 2)
            {
                unpaired[x][y] = map[px][y];
            }
        }
        return unpaired;
    }

    /**
     * Produces an int[][] that can be used with any palette of your choice for methods in SquidPanel or for your own
     * rendering method. 1 is used as a default and for tiles with nothing in them; if the background is black, then
     * white would make sense as this default. Other indices used are 2 for walls (this doesn't care if the walls are
     * hashes or lines), 3 for floors (usually '.'), 4 for doors ('+' and '/' in the map), 5 for water, and 6 for traps.
     * @param map
     * @return
     */
    public static int[][] generatePaletteIndices(char[][] map)
    {

        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = 2;
                        break;
                    case '.':
                        portion[i][j] = 3;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = 4;
                        break;
                    case '~':
                        portion[i][j] = 5;
                        break;
                    case '^':
                        portion[i][j] = 6;
                        break;
                    default:
                        portion[i][j] = 1;
                }
            }
        }
        return portion;
    }
    /**
     * Produces an int[][] that can be used with any palette of your choice for methods in SquidPanel or for your own
     * rendering method, but meant for the background palette. This will produce 0 for anything but water (represented
     * by '~'), and will produce 24 for water backgrounds (in the default palette, this is dark blue-green).
     * @param map
     * @return
     */
    public static int[][] generateBGPaletteIndices(char[][] map)
    {

        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = 0;
                        break;
                    case '.':
                        portion[i][j] = 0;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = 0;
                        break;
                    case '~':
                        portion[i][j] = 24;
                        break;
                    case '^':
                        portion[i][j] = 0;
                        break;
                    default:
                        portion[i][j] = 0;
                }
            }
        }
        return portion;
    }
    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors.
     * @param map
     * @return
     */
    public static int[][] generateLightnessModifiers(char[][] map)
    {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = 0;
                        break;
                    case '.':
                        portion[i][j] = 20;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -20;
                        break;
                    case '~':
                        portion[i][j] = (int)(100 * (PerlinNoise.noise(i / 4.0, j / 4.0) / 2.5 - 0.65));
                        break;
                    case '^':
                        portion[i][j] = 40;
                        break;
                    default:
                        portion[i][j] = 0;
                }
            }
        }
        return portion;
    }
    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors, accepting a parameter for
     * animation frame if rippling water using Perlin Noise is desired.
     * @param map
     * @param frame
     * @return
     */
    public static int[][] generateLightnessModifiers(char[][] map, double frame)
    {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = 30;
                        break;
                    case '.':
                        portion[i][j] = 0;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -20;
                        break;
                    case '~':
                        portion[i][j] = (int)(100 * (PerlinNoise.noise(i / 4.0, j / 4.0, frame / 25.0) / 2.5 - 0.65));
                        break;
                    case '^':
                        portion[i][j] = 40;
                        break;
                    default:
                        portion[i][j] = 0;
                }
            }
        }
        return portion;
    }
    /**
     * Given a char[][] for the map, produces a double[][] that can be used with FOV.calculateFOV(). It expects any
     * doors to be represented by '+' if closed or '/' if open (which can be caused by calling
     * DungeonUtility.closeDoors() ), any walls to be '#' or line drawing characters, and it doesn't care what other
     * chars are used (only doors, including open ones, and walls obscure light and thus have a resistance by default).
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map suitable for use with the FOV class
     */
    public static double[][] generateResistances(char[][] map)
    {
        int width = map.length;
        int height = map[0].length;
        double[][] portion = new double[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = 1.0;
                        break;
                    case '.':
                        portion[i][j] = 0.0;
                        break;
                    case '/':
                        portion[i][j] = 0.15;
                        break;
                    case '+':
                        portion[i][j] = 0.95;
                        break;
                    case '~':
                        portion[i][j] = 0.0;
                        break;
                    case '^':
                        portion[i][j] = 0.0;
                        break;
                    default:
                        portion[i][j] = 0.0;
                }
            }
        }
        return portion;
    }
}
