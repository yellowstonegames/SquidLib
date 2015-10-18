package squidpony.squidgrid.mapping;

import squidpony.squidmath.*;


import java.util.Map;

/**
 * A static class that can be used to modify the char[][] dungeons that other generators produce.
 * Includes various utilities for random floor-finding, but also provides ways to take dungeons that use '#'
 * for walls and make a copy that uses unicode box drawing characters.
 * @see squidpony.squidgrid.mapping.DungeonGenerator
 * Created by Tommy Ettinger on 4/1/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DungeonUtility {

    public DungeonUtility()
    {
        rng = new StatefulRNG();
    }
    public DungeonUtility(StatefulRNG rng)
    {
        this.rng = rng;
    }
    public DungeonUtility(RNG rng)
    {
        this.rng = new StatefulRNG(new LightRNG(rng.nextLong()));
    }

    /**
     * The random number generator that will be used for all methods in this class with a random component.
     */
    public StatefulRNG rng;
    /**
     * Finds a random Coord where the x and y match up to a [x][y] location on map that has '.' as a value.
     * Uses this class' rng field for pseudo-random number generation.
     * @param map
     * @return a Coord that corresponds to a '.' in map, or null if a '.' cannot be found or if map is too small.
     */
    public Coord randomFloor(char[][] map)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 3 || height < 3)
            return null;
        int x = rng.nextInt(width - 2) + 1, y = rng.nextInt(height - 2) + 1;
        for(int i = 0; i < 20; i++)
        {
            if(map[x][y] == '.')
            {
                return Coord.get(x, y);
            }
            else
            {
                x = rng.nextInt(width - 2) + 1;
                y = rng.nextInt(height - 2) + 1;
            }
        }
        x = 1;
        y = 1;
        if(map[x][y] == '.')
            return Coord.get(x, y);

        while(map[x][y] != '.')
        {
            x += 1;
            if(x >= width - 1)
            {
                x = 1;
                y += 1;
            }
            if(y >= height - 1)
                return null;
        }
        return Coord.get(x, y);
    }
    /**
     * Finds a random Coord where the x and y match up to a [x][y] location on map that has the same value as the
     * parameter tile. Uses this class' rng field for pseudo-random number generation.
     * @param map
     * @return a Coord that corresponds to a map element equal to tile, or null if tile cannot be found or if map is too small.
     */
    public Coord randomMatchingTile(char[][] map, char tile)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 3 || height < 3)
            return null;
        int x = rng.nextInt(width - 2) + 1, y = rng.nextInt(height - 2) + 1;
        for(int i = 0; i < 30; i++)
        {
            if(map[x][y] == tile)
            {
                return Coord.get(x, y);
            }
            else
            {
                x = rng.nextInt(width - 2) + 1;
                y = rng.nextInt(height - 2) + 1;
            }
        }
        x = 1;
        y = 1;
        if(map[x][y] == tile)
            return Coord.get(x, y);

        while(map[x][y] != tile)
        {
            x += 1;
            if(x >= width - 1)
            {
                x = 1;
                y += 1;
            }
            if(y >= height - 1)
                return null;
        }
        return Coord.get(x, y);
    }

    /**
     * Gets a random Coord that is adjacent to start, validating whether the position can exist on the given map.
     * Adjacency defaults to four-way cardinal directions unless eightWay is true, in which case it uses Chebyshev.
     * This can step into walls, and should NOT be used for movement.  It is meant for things like sound that can
     * exist in walls, or for assigning decor to floors or walls that are adjacent to floors.
     * @param map
     * @param start
     * @param eightWay
     * @return
     */
    public Coord randomStep(char[][] map, Coord start, boolean eightWay)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 3 || height < 3 || start.x <= 0 || start.y <= 0 || start.x >= width - 1 || start.y >= height - 1)
            return null;
        Coord stepped = Coord.get(start.x, start.y);

        if(eightWay)
        {
            int mv = rng.nextInt(9);
            return stepped.translate((mv % 3) - 1, (mv / 3) - 1);
        }
        else
        {
            int mv = rng.nextInt(5);
            switch (mv)
            {
                case 0: return stepped.translate(-1, 0);
                case 1: return stepped.translate(1, 0);
                case 2: return stepped.translate(0, -1);
                case 3: return stepped.translate(0, 1);
                default: return stepped;
            }
        }
    }
    /**
     * Finds a random Coord where the x and y match up to a [x][y] location on map that has '.' as a value,
     * and a square of cells extending in the positive x and y directions with a side length of size must also have
     * '.' as their values.
     * Uses this class' rng field for pseudo-random number generation.
     * @param map
     * @param size
     * @return a Coord that corresponds to a '.' in map, or null if a '.' cannot be found or if map is too small.
     */
    public Coord randomFloorLarge(char[][] map, int size)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 4 || height < 4)
            return null;
        int x = rng.nextInt(width - size), y = rng.nextInt(height - size);
        CELL:
        for(int i = 0; i < 20; i++, x = rng.nextInt(width - size), y = rng.nextInt(height - size))
        {
            if(map[x][y] == '.')
            {
                for(int j = 0; j < size; j++)
                {
                    for(int k = 0; k < size; k++)
                    {
                        if(map[x + j][y + k] != '.')
                            continue CELL;
                    }
                }
                return Coord.get(x, y);
            }
        }
        x = 1;
        y = 1;

        SLOW:
        while(true)
        {
            x += 1;
            if(x >= width - size)
            {
                x = 1;
                y += 1;
            }
            if(y >= height - size)
                return null;
            if(map[x][y] == '.')
            {
                for(int j = 0; j < size; j++)
                {
                    for(int k = 0; k < size; k++)
                    {
                        if(map[x + j][y + k] != '.')
                            continue SLOW;
                    }
                }
                return Coord.get(x, y);
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
     * @param map a char[][] that may have both '+' and '/' for doors
     * @return a char[][] that only uses '+' for all doors
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
     * When a map is generated by DungeonGenerator with addDoors enabled, different chars are used for vertical and
     * horizontal doors ('+' for vertical and '/' for horizontal).  This makes all doors '/', which is useful if you
     * want '+' to be used for a different purpose and/or to distinguish open and closed doors.
     * @param map a char[][] that may have both '+' and '/' for doors
     * @return a char[][] that only uses '/' for all doors
     */
    public static char[][] openDoors(char[][] map)
    {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(map[i][j] == '+') portion[i][j] = '/';
                else portion[i][j] = map[i][j];
            }
        }
        return portion;
    }


    /**
     * Takes a char[][] dungeon map and returns a copy with all box drawing chars, special placeholder chars, or '#'
     * chars changed to '#' and everything else changed to '.' .
     * @param map
     * @return
     */
    public static char[][] simplifyDungeon(char[][] map)
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
                    case '#':
                        portion[i][j] = '#';
                        break;
                    default:
                        portion[i][j] = '.';
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
                    case '"':
                        portion[i][j] = 20;
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
                    case '"':
                        portion[i][j] = 21;
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
                    case '"':
                        portion[i][j] = (int)(80 * (PerlinNoise.noise(i / 4.0, j / 4.0) / 8.0 - 0.5));
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
                    case '"':
                        portion[i][j] = (int)(80 * (PerlinNoise.noise(i / 4.0, j / 4.0, frame / 30.0) / 3.0 - 0.35));
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
                    case '"':
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
    /**
     * Given a char[][] for the map, a Map of Character keys to Double values that will be used to determine costs, and
     * a double value for unhandled characters, produces a double[][] that can be used as a costMap by DijkstraMap. It
     * expects any doors to be represented by '+' if closed or '/' if open (which can be caused by calling
     * DungeonUtility.closeDoors() ) and any walls to be '#' or line drawing characters. In the parameter costs, there
     * does not need to be an entry for '#' or any box drawing characters, but if one is present for '#' it will apply
     * that cost to both '#' and all box drawing characters, and if one is not present it will default to a very high
     * number. For any other entry in costs, a char in the 2D char array that matches the key will correspond
     * (at the same x,y position in the returned 2D double array) to that key's value in costs. If a char is used in the
     * map but does not have a corresponding key in costs, it will be given the value of the parameter defaultValue.
     *
     * The values in costs are multipliers, so should not be negative, should only be 0.0 in cases where you want
     * infinite movement across all adjacent squares of that kind, should be higher than 1.0 for difficult terrain (2.0
     * and 3.0 are reasonable), should be between 0.0 and 1.0 for easy terrain, and should be 1.0 for normal terrain.
     * If a cell should not be possible to enter for this character, 999.0 should be a reasonable value for a cost.
     *
     * An example use for this would be to make a creature unable to enter any non-water cell (like a fish),
     * unable to enter doorways (like some mythological versions of vampires), or to make a wheeled vehicle take more
     * time to move across rubble or rough terrain.
     *
     * A potentially common case that needs to be addressed is NPC movement onto staircases in games that have them;
     * some games may find it desirable for NPCs to block staircases and others may not, but in either case you should
     * give both '&gt;' and '&lt;', the standard characters for staircases, the same value in costs.
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors() .
     * @param costs a Map of Character keys representing possible elements in map, and Double values for their cost.
     * @param defaultValue a double that will be used as the cost for any characters that don't have a key in costs.
     * @return a cost map suitable for use with DijkstraMap
     */
    public static double[][] generateCostMap(char[][] map, Map<Character, Double> costs, double defaultValue)
    {
        int width = map.length;
        int height = map[0].length;
        double[][] portion = new double[width][height];
        char current;
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++) {
                current = map[i][j];
                if (costs.containsKey(current)) {
                    portion[i][j] = costs.get(current);
                } else {
                    switch (current) {
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
                            portion[i][j] = (costs.containsKey('#'))
                                    ? costs.get('#')
                                    : squidpony.squidai.DijkstraMap.WALL;
                            break;
                        default:
                            portion[i][j] = defaultValue;
                    }
                }
            }
        }
        return portion;
    }
}
