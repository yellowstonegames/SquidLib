package squidpony.squidgrid.mapping;

import squidpony.squidmath.LightRNG;

import java.awt.Point;

/**
 * Created by Tommy Ettinger on 4/1/2015.
 */
public class DungeonUtility {
    /**
     * The random number generator that will be used for all methods in this class with a random component. You can use
     * the setState(long seed) method at any point to fix the seed/state of this RNG, or getState() if you want to store
     * it for some reason (maybe serialization).
     */
    public static LightRNG rng = new LightRNG();
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
        int Width = map[0].length+2;
        int Height = map.length+2;

        char[][] neo = new char[Height][Width], dungeon = new char[Height][Width];
        for(int i = 1; i < Height - 1; i++)
        {
            for(int j = 1; j < Width - 1; j++)
            {
                dungeon[i][j] = map[i-1][j-1];
            }
        }
        for(int i = 0; i < Height; i++)
        {
            neo[i][0] = '\1';
            neo[i][Width-1] = '\1';
            dungeon[i][0] = '\1';
            dungeon[i][Width-1] = '\1';
        }
        for(int i = 0; i < Width; i++)
        {
            neo[0][i] = '\1';
            neo[Height-1][i] = '\1';
            dungeon[0][i] = '\1';
            dungeon[Height-1][i] = '\1';
        }

        for (int y = 1; y < Height - 1; y++)
        {
            for (int x = 1; x < Width - 1; x++)
            {
                if (map[y-1][x-1] == '#')
                {
                    int q = 0;
                    q |= (y <= 1 || map[y - 2][x-1] == '#') ? 1 : 0;
                    q |= (y <= 1 || x >= Width - 2 || map[y - 2][x + 0] == '#') ? 2 : 0;
                    q |= (x >= Width - 2  || map[y - 1][x + 0] == '#') ? 4 : 0;
                    q |= (y >= Height - 2 || x >= Width - 2 || map[y + 0][x + 0] == '#') ? 8 : 0;
                    q |= (y >= Height - 2 || map[y + 0][x-1] == '#') ? 16 : 0;
                    q |= (y >= Height - 2 || x <= 1 || map[y + 0][x - 2] == '#') ? 32 : 0;
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

        for (int y = 0; y < Height; y++)
        {
            for (int x = 0; x < Width; x++)
            {
                if (dungeon[y][x] == '#')
                {
                    boolean n = (y <= 0 || dungeon[y - 1][x] == '#');
                    boolean e = (x >= Width - 1 || dungeon[y][x + 1] == '#');
                    boolean s = (y >= Height - 1 || dungeon[y + 1][x] == '#');
                    boolean w = (x <= 0 || dungeon[y][x - 1] == '#');

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
        for (int y = 1; y < Height; y++)
        {
            for (int x = 0; x < Width; x++)
            {
                // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                if (neo[y][x] == '┼' || neo[y][x] == '├' || neo[y][x] == '┤' || neo[y][x] == '┴')
                {
                    if (neo[y - 1][x] == '┼' || neo[y - 1][x] == '├' || neo[y - 1][x] == '┤' || neo[y - 1][x] == '┬')
                    {
                        if ((x >= Width - 1 || dungeon[y - 1][x + 1] == '#' || dungeon[y - 1][x + 1] == '\1') &&
                                (x <= 0 || dungeon[y - 1][x - 1] == '#' || dungeon[y - 1][x - 1] == '\1') &&
                                (x >= Width - 1 || dungeon[y][x + 1] == '#' || dungeon[y][x + 1] == '\1') &&
                                (x <= 0 || dungeon[y][x - 1] == '#' || dungeon[y][x - 1] == '\1'))
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
        for (int y = 0; y < Height; y++)
        {
            for (int x = 1; x < Width; x++)
            {
                // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                if (neo[y][x] == '┼' || neo[y][x] == '┤' || neo[y][x] == '┬' || neo[y][x] == '┴')
                {
                    if (neo[y][x - 1] == '┼' || neo[y][x - 1] == '├' || neo[y][x - 1] == '┬' || neo[y][x - 1] == '┴')
                    {
                        if ((y >= Height - 1 || x >= Width - 1 || dungeon[y + 1][x - 1] == '#' || dungeon[y + 1][x - 1] == '\1') &&
                                (y <= 0 || dungeon[y - 1][x - 1] == '#' || dungeon[y - 1][x - 1] == '\1') &&
                                (y >= Height - 1 || dungeon[y + 1][x] == '#' || dungeon[y + 1][x] == '\1') &&
                                (y <= 0 || dungeon[y - 1][x] == '#' || dungeon[y - 1][x] == '\1'))
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
        char[][] portion = new char[Height-2][Width-2];
        for(int i = 1; i < Height - 1; i++)
        {
            for(int j = 1; j < Width - 1; j++)
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
     * Reverses most of the effects of linesToHashes(). The only things that will not be reversed are the placement of
     * space characters in unreachable wall-cells-behind-wall-cells, which remain as spaces. This is useful if you
     * have a modified map that contains wall characters of conflicting varieties, as described in hashesToLines().
     * @param map
     * @return
     */
    public static char[][] linesToHashes(char[][] map)
    {

        int Width = map.length;
        int Height = map[0].length;
        char[][] portion = new char[Width][Height];
        for(int i = 0; i < Width; i++)
        {
            for(int j = 0; j < Height; j++)
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

        int Width = map[0].length;
        int Height = map.length;
        char[][] portion = new char[Height][Width];
        for(int i = 0; i < Height; i++)
        {
            for(int j = 0; j < Width; j++)
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
     * Takes a dungeon map with either '#' as the only wall character or the unicode box drawing characters used by
     * hashesToLines(), and returns a new char[][] dungeon map with two characters per cell, mostly filling the spaces
     * next to non-walls with space characters, and only doing anything different if a box-drawing character would
     * continue into an adjacent cell, or if a '#' wall needs another '#' wall next to it. The recommended approach is
     * to keep both the original non-double-width map and the newly-returned double-width map, since the single-width
     * maps can be used more easily for pathfinding. If you need to undo this function, call unDoubleWidth().
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
    public static char[][] unDoubleWidth(char[][] map)
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
}
