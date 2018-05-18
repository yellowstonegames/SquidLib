package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidai.DijkstraMap;
import squidpony.squidmath.*;

import java.util.*;

/**
 * A static class that can be used to modify the char[][] dungeons that other generators produce.
 * Includes various utilities for random floor-finding, but also provides ways to take dungeons that use '#'
 * for walls and make a copy that uses unicode box drawing characters.
 *
 * @author Tommy Ettinger - https://github.com/tommyettinger
 * @see squidpony.squidgrid.mapping.DungeonGenerator DungeonGenerator uses this class a fair amount
 * Created by Tommy Ettinger on 4/1/2015.
 */
public class DungeonUtility {

    public DungeonUtility() {
        rng = new StatefulRNG();
    }

    public DungeonUtility(StatefulRNG rng) {
        this.rng = rng;
    }

    public DungeonUtility(IRNG rng) {
        this.rng = new StatefulRNG(rng.nextLong());
    }

    /**
     * The random number generator that will be used for all methods in this class with a random component.
     */
    public StatefulRNG rng;

    /**
     * Finds a random Coord where the x and y match up to a [x][y] location on map that has '.' as a value.
     * Uses this class' rng field for pseudo-random number generation.
     *
     * @param map a char[][] that should contain a '.' floor tile
     * @return a Coord that corresponds to a '.' in map, or null if a '.' cannot be found or if map is too small
     */
    public Coord randomFloor(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        if (width < 3 || height < 3)
            return null;
        int x = rng.nextInt(width - 2) + 1, y = rng.nextInt(height - 2) + 1;
        for (int i = 0; i < 20; i++) {
            if (map[x][y] == '.') {
                return Coord.get(x, y);
            } else {
                x = rng.nextInt(width - 2) + 1;
                y = rng.nextInt(height - 2) + 1;
            }
        }
        x = 1;
        y = 1;
        if (map[x][y] == '.')
            return Coord.get(x, y);

        while (map[x][y] != '.') {
            x += 1;
            if (x >= width - 1) {
                x = 1;
                y += 1;
            }
            if (y >= height - 1)
                return null;
        }
        return Coord.get(x, y);
    }

    /**
     * Finds a random Coord where the x and y match up to a [x][y] location that is encoded as "on" in packed.
     * This is useful when you have used {@code DungeonUtility.packedFloors(char[][] map)} to encode all floors in map,
     * or {@code CoordPacker.pack(char[][] map, char... yes)} to encode all cells in a char[][] map that match a
     * particular type, like '.' for floors or '~' for deep water, and want to efficiently get one randomly-chosen tile
     * from it. Calling pack() is likely slightly less efficient than using randomFloor(), but it only needs to be done
     * once per map and cell type, and this method should be substantially more efficient when the type of cell is
     * uncommon on the map.
     * Uses this class' rng field for pseudo-random number generation.
     *
     * @param packed a packed array produced by CoordPacker encoding the cells to choose from as "on"
     * @return a Coord that corresponds to a '.' in map, or (-1, -1) if a '.' cannot be found or if map is too small
     */
    public Coord randomCell(short[] packed) {
        CoordPacker.init();
        return CoordPacker.singleRandom(packed, rng);
    }

    /**
     * A convenience wrapper for getting a packed-data representation of all floors ('.') in map, for randomCell().
     * If you want other chars or more chars than just the period, you can use CoordPacker.pack() with a char[][] map
     * and one or more chars to find as the parameters. This is the same as calling {@code CoordPacker.pack(map, '.')}.
     *
     * @param map a char[][] that uses '.' to represent floors
     * @return all floors in map in packed data format (a special short array) that can be given to randomCell()
     */
    public static short[] packedFloors(char[][] map) {
        CoordPacker.init();
        return CoordPacker.pack(map, '.');
    }

    /**
     * Finds a random Coord where the x and y match up to a [x][y] location on map that has the same value as the
     * parameter tile. Uses this class' rng field for pseudo-random number generation.
     *
     * @param map  a char[][] that should contain the desired tile
     * @param tile the char to search for
     * @return a Coord that corresponds to a map element equal to tile, or null if tile cannot be found or if map is too small.
     */
    public Coord randomMatchingTile(char[][] map, char tile) {
        int width = map.length;
        int height = map[0].length;
        if (width < 3 || height < 3)
            return null;
        int x = rng.nextInt(width - 2) + 1, y = rng.nextInt(height - 2) + 1;
        for (int i = 0; i < 30; i++) {
            if (map[x][y] == tile) {
                return Coord.get(x, y);
            } else {
                x = rng.nextInt(width - 2) + 1;
                y = rng.nextInt(height - 2) + 1;
            }
        }
        x = 1;
        y = 1;
        if (map[x][y] == tile)
            return Coord.get(x, y);

        while (map[x][y] != tile) {
            x += 1;
            if (x >= width - 1) {
                x = 1;
                y += 1;
            }
            if (y >= height - 1)
                return null;
        }
        return Coord.get(x, y);
    }

    /**
     * Gets a random Coord that is adjacent to start, validating whether the position can exist on the given map.
     * Adjacency defaults to four-way cardinal directions unless eightWay is true, in which case it uses Chebyshev.
     * This can step into walls, and should NOT be used for movement.  It is meant for things like sound that can
     * exist in walls, or for assigning decor to floors or walls that are adjacent to floors.
     *
     * @param map      a char[][] map that this will only use for its width and height; contents are ignored
     * @param start    the starting position
     * @param eightWay true to choose a random orthogonal or diagonal direction; false to only choose from orthogonal
     * @return a Coord that is adjacent to start on the map, or null if start is off the map or the map is very small
     */
    public Coord randomStep(char[][] map, Coord start, boolean eightWay) {
        int width = map.length;
        int height = map[0].length;
        if (width < 3 || height < 3 || start.x < 0 || start.y < 0 || start.x > width - 1 || start.y > height - 1)
            return null;
        Coord stepped = Coord.get(start.x, start.y);

        if (eightWay) {
            int mv = rng.nextInt(9);
            return Coord.get(Math.min(Math.max(0, stepped.x + (mv % 3) - 1), height - 1),
                    Math.min(Math.max(0, stepped.y + (mv / 3) - 1), height - 1));
        } else {
            int mv = rng.nextInt(5);
            switch (mv) {
                case 0:
                    return Coord.get(Math.min(Math.max(0, stepped.x - 1), height - 1),
                            stepped.y);
                case 1:
                    return Coord.get(Math.min(Math.max(0, stepped.x + 1), height - 1),
                            stepped.y);
                case 2:
                    return Coord.get(stepped.x,
                            Math.min(Math.max(0, stepped.y - 1), height - 1));
                case 3:
                    return Coord.get(stepped.x,
                            Math.min(Math.max(0, stepped.y + 1), height - 1));
                default:
                    return stepped;
            }
        }
    }

    /**
     * Finds a random Coord where the x and y match up to a [x][y] location on map that has '.' as a value,
     * and a square of cells extending in the positive x and y directions with a side length of size must also have
     * '.' as their values.
     * Uses this class' rng field for pseudo-random number generation.
     *
     * @param map  a char[][] that should contain at least one floor represented by '.'
     * @param size the side length of a square that must be completely filled with floors for this to return it
     * @return a Coord that corresponds to a '.' in map, or null if a '.' cannot be found or if map is too small.
     */
    public Coord randomFloorLarge(char[][] map, int size) {
        int width = map.length;
        int height = map[0].length;
        if (width < size + 2 || height < size + 2)
            return null;
        int x = rng.nextInt(width - size), y = rng.nextInt(height - size);
        CELL:
        for (int i = 0; i < 20; i++, x = rng.nextInt(width - size), y = rng.nextInt(height - size)) {
            if (map[x][y] == '.') {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < size; k++) {
                        if (map[x + j][y + k] != '.')
                            continue CELL;
                    }
                }
                return Coord.get(x, y);
            }
        }
        x = 1;
        y = 1;

        SLOW:
        while (true) {
            x += 1;
            if (x >= width - size) {
                x = 1;
                y += 1;
            }
            if (y >= height - size)
                return null;
            if (map[x][y] == '.') {
                for (int j = 0; j < size; j++) {
                    for (int k = 0; k < size; k++) {
                        if (map[x + j][y + k] != '.')
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
     *
     * @param map a 2D char array indexed with x,y that uses '#' for walls
     * @return a copy of the map passed as an argument with box-drawing characters replacing '#' walls
     */
    public static char[][] hashesToLines(char[][] map) {
        return hashesToLines(map, false);
    }

    private static final char[] wallLookup = new char[]
            {
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '┴', '┐', '┤', '┬', '┤',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '─', '┴',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '┴', '┐', '┤', '─', '┘',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '─', '┐', '┤', '┬', '┬',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '─', '┐', '┤', '┬', '┐',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '│', '┬', '├',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '─', '┐', '│', '┬', '┌',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '│', '─', '└',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '─', '┐', '│', '─', '\1'
            };

    /**
     * Takes a char[][] dungeon map that uses '#' to represent walls, and returns a new char[][] that uses unicode box
     * drawing characters to draw straight, continuous lines for walls, filling regions between walls (that were
     * filled with more walls before) with space characters, ' '. If keepSingleHashes is true, then '#' will be used if
     * a wall has no orthogonal wall neighbors; if it is false, then a horizontal line will be used for stand-alone
     * wall cells. If the lines "point the wrong way," such as having multiple horizontally adjacent vertical lines
     * where there should be horizontal lines, call transposeLines() on the returned map, which will keep the dimensions
     * of the map the same and only change the line chars. You will also need to call transposeLines if you call
     * hashesToLines on a map that already has "correct" line-drawing characters, which means hashesToLines should only
     * be called on maps that use '#' for walls. If you have a jumbled map that contains two or more of the following:
     * "correct" line-drawing characters, "incorrect" line-drawing characters, and '#' characters for walls, you can
     * reset by calling linesToHashes() and then potentially calling hashesToLines() again.
     *
     * @param map              a 2D char array indexed with x,y that uses '#' for walls
     * @param keepSingleHashes true if walls that are not orthogonally adjacent to other walls should stay as '#'
     * @return a copy of the map passed as an argument with box-drawing characters replacing '#' walls
     */
    public static char[][] hashesToLines(char[][] map, boolean keepSingleHashes) {
        int width = map.length + 2;
        int height = map[0].length + 2;

        char[][] dungeon = new char[width][height];
        for (int i = 1; i < width - 1; i++) {
            System.arraycopy(map[i - 1], 0, dungeon[i], 1, height - 2);
        }
        for (int i = 0; i < width; i++) {
            dungeon[i][0] = '\1';
            dungeon[i][height - 1] = '\1';
        }
        for (int i = 0; i < height; i++) {
            dungeon[0][i] = '\1';
            dungeon[width - 1][i] = '\1';
        }
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (map[x - 1][y - 1] == '#') {
                    int q = 0;
                    q |= (y <= 1 || map[x - 1][y - 2] == '#' || map[x - 1][y - 2] == '+' || map[x - 1][y - 2] == '/') ? 1 : 0;
                    q |= (x >= width - 2 || map[x][y - 1] == '#' || map[x][y - 1] == '+' || map[x][y - 1] == '/') ? 2 : 0;
                    q |= (y >= height - 2 || map[x - 1][y] == '#' || map[x - 1][y] == '+' || map[x - 1][y] == '/') ? 4 : 0;
                    q |= (x <= 1 || map[x - 2][y - 1] == '#' || map[x - 2][y - 1] == '+' || map[x - 2][y - 1] == '/') ? 8 : 0;

                    q |= (y <= 1 || x >= width - 2 || map[x][y - 2] == '#' || map[x][y - 2] == '+' || map[x][y - 2] == '/') ? 16 : 0;
                    q |= (y >= height - 2 || x >= width - 2 || map[x][y] == '#' || map[x][y] == '+' || map[x][y] == '/') ? 32 : 0;
                    q |= (y >= height - 2 || x <= 1 || map[x - 2][y] == '#' || map[x - 2][y] == '+' || map[x - 2][y] == '/') ? 64 : 0;
                    q |= (y <= 1 || x <= 1 || map[x - 2][y - 2] == '#' || map[x - 2][y - 2] == '+' || map[x - 2][y - 2] == '/') ? 128 : 0;
                    if (!keepSingleHashes && wallLookup[q] == '#') {
                        dungeon[x][y] = '─';
                    } else {
                        dungeon[x][y] = wallLookup[q];
                    }
                }
            }
        }
        char[][] portion = new char[width - 2][height - 2];
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                switch (dungeon[i][j]) {
                    case '\1':
                        portion[i - 1][j - 1] = ' ';
                        break;
                    default: // ┼┌┘
                        portion[i - 1][j - 1] = dungeon[i][j];
                }
            }
        }
        return portion;
    }

    /**
     * Reverses most of the effects of hashesToLines(). The only things that will not be reversed are the placement of
     * space characters in unreachable wall-cells-behind-wall-cells, which remain as spaces. This is useful if you
     * have a modified map that contains wall characters of conflicting varieties, as described in hashesToLines().
     *
     * @param map a 2D char array indexed with x,y that uses box-drawing characters for walls
     * @return a copy of the map passed as an argument with '#' replacing box-drawing characters for walls
     */
    public static char[][] linesToHashes(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
     *
     * @param map a 2D char array indexed with y,x that uses box-drawing characters for walls
     * @return a copy of map that uses box-drawing characters for walls that will be correct when indexed with x,y
     */
    public static char[][] transposeLines(char[][] map) {

        int width = map[0].length;
        int height = map.length;
        char[][] portion = new char[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                switch (map[i][j]) {
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
     *
     * @param map a char[][] that may have both '+' and '/' for doors
     * @return a char[][] that only uses '+' for all doors
     */
    public static char[][] closeDoors(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] == '/') portion[i][j] = '+';
                else portion[i][j] = map[i][j];

            }
        }
        return portion;
    }

    /**
     * When a map is generated by DungeonGenerator with addDoors enabled, different chars are used for vertical and
     * horizontal doors ('+' for vertical and '/' for horizontal).  This makes all doors '/', which is useful if you
     * want '+' to be used for a different purpose and/or to distinguish open and closed doors.
     *
     * @param map a char[][] that may have both '+' and '/' for doors
     * @return a char[][] that only uses '/' for all doors
     */
    public static char[][] openDoors(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] == '+') portion[i][j] = '/';
                else portion[i][j] = map[i][j];
            }
        }
        return portion;
    }


    /**
     * Takes a char[][] dungeon map and returns a copy with all box drawing chars, special placeholder chars, or '#'
     * chars changed to '#' and everything else changed to '.' .
     *
     * @param map a char[][] with different characters that can be simplified to "wall" or "floor"
     * @return a copy of map with all box-drawing, placeholder, wall or space characters as '#' and everything else '.'
     */
    public static char[][] simplifyDungeon(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ' ':
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
     * maps can be used more easily for pathfinding. If you need to undo this function, call unDoubleWidth().
     *
     * @param map a char[][] that uses either '#' or box-drawing characters for walls, but one per cell
     * @return a widened copy of map that uses two characters for every cell, connecting box-drawing chars correctly
     */
    public static char[][] doubleWidth(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        char[][] paired = new char[width * 2][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0, px = 0; x < width; x++, px += 2) {
                paired[px][y] = map[x][y];
                switch (paired[px][y]) {
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
     *
     * @param map a char[][] that has been widened by doubleWidth()
     * @return a copy of map that uses only one char per cell
     */
    public static char[][] unDoubleWidth(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        if (width % 2 != 0)
            throw new IllegalArgumentException("Argument must be a char[width][height] with an even width.");
        char[][] unpaired = new char[width / 2][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0, px = 0; px < width; x++, px += 2) {
                unpaired[x][y] = map[px][y];
            }
        }
        return unpaired;
    }

    /**
     * Produces an int[][] that can be used with any palette of your choice for methods in SquidPanel or for your own
     * rendering method. 1 is used as a default and for tiles with nothing in them; if the background is black, then
     * white would make sense as this default. Other indices used are 2 for walls (this doesn't care if the walls are
     * hashes or lines), 3 for floors (usually '.'), 4 for doors ('+' and '/' in the map), 5 for water, 6 for traps, and
     * 20 for grass.
     *
     * @param map a char[][] containing foreground characters that you want foreground palette indices for
     * @return a 2D array of ints that can be used as indices into a palette; palettes are available in related modules
     */
    public static int[][] generatePaletteIndices(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ':':
                        portion[i][j] = 3;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = 4;
                        break;
                    case ',':
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
     * rendering method. 1 is used as a default and for tiles with nothing in them; if the background is black, then
     * white would make sense as this default. Other indices used are 2 for walls (this doesn't care if the walls are
     * hashes or lines), 3 for floors (usually '.'), 4 for doors ('+' and '/' in the map), 5 for water, 6 for traps, and
     * 20 for grass.
     *
     * @param map a char[][] containing foreground characters that you want foreground palette indices for
     * @return a 2D array of ints that can be used as indices into a palette; palettes are available in related modules
     */
    public static int[][] generatePaletteIndices(char[][] map, char deepChar, int deepIndex,
                                                 char shallowChar, int shallowIndex) {

        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ':':
                        portion[i][j] = 3;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = 4;
                        break;
                    case ',':
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
                        if (map[i][j] == deepChar)
                            portion[i][j] = deepIndex;
                        else if (map[i][j] == shallowChar)
                            portion[i][j] = shallowIndex;
                        else portion[i][j] = 1;
                }
            }
        }
        return portion;
    }


    /**
     * Produces an int[][] that can be used with any palette of your choice for methods in SquidPanel or for your own
     * rendering method, but meant for the background palette. This will produce 0 for most characters, but deep water
     * (represented by '~') will produce 24 (in the default palette, this is dark blue-green), shallow water
     * (represented by ',') will produce 23 (medium blue-green), and grass (represented by '"') will produce 21 (dark
     * green). If you use SquidLayers, you can cause the lightness of water and grass to vary as if currents or wind
     * are moving their surface using getLightnessModifiers() and a frame count argument.
     *
     * @param map a char[][] containing foreground characters that you want background palette indices for
     * @return a 2D array of ints that can be used as indices into a palette; palettes are available in related modules
     */
    public static int[][] generateBGPaletteIndices(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ':':
                        portion[i][j] = 35;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = 0;
                        break;
                    case ',':
                        portion[i][j] = 23;
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
     * Produces an int[][] that can be used with any palette of your choice for methods in SquidPanel or for your own
     * rendering method, but meant for the background palette. This will produce 0 for most characters, but deep water
     * (represented by '~') will produce 24 (in the default palette, this is dark blue-green), shallow water
     * (represented by ',') will produce 23 (medium blue-green), and grass (represented by '"') will produce 21 (dark
     * green). If you use SquidLayers, you can cause the lightness of water and grass to vary as if currents or wind
     * are moving their surface using getLightnessModifiers() and a frame count argument.
     *
     * @param map a char[][] containing foreground characters that you want background palette indices for
     * @return a 2D array of ints that can be used as indices into a palette; palettes are available in related modules
     */
    public static int[][] generateBGPaletteIndices(char[][] map, char deepChar, int deepIndex,
                                                   char shallowChar, int shallowIndex) {

        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ':':
                        portion[i][j] = 35;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = 0;
                        break;
                    case ',':
                        portion[i][j] = 23;
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
                        if (map[i][j] == deepChar)
                            portion[i][j] = deepIndex;
                        else if (map[i][j] == shallowChar)
                            portion[i][j] = shallowIndex;
                        else portion[i][j] = 0;
                }
            }
        }
        return portion;
    }

    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors.
     *
     * @param map a char[][] that you want to be find background lightness modifiers for
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ':':
                        portion[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -10;
                        break;
                    case ',':
                        portion[i][j] = (int) (70 * (PerlinNoise.noise(i* 1.5, j* 1.5) * 0.4 - 0.45));
                        break;
                    case '~':
                        portion[i][j] = (int) (100 * (PerlinNoise.noise(i* 1.5, j* 1.5) * 0.4 - 0.65));
                        break;
                    case '"':
                        portion[i][j] = (int) (75 * (PerlinNoise.noise(i* 1.5, j* 1.5) * 0.25 - 1.5));
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
     * animation frame if rippling water and waving grass using Perlin Noise are desired.
     *
     * @param map   a char[][] that you want to be find background lightness modifiers for
     * @param frame a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *              water and grass move more
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map, double frame) {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ':':
                        portion[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -10;
                        break;
                    case ',':
                        portion[i][j] = (int) (70 * (PerlinNoise.noise(i* 1.5, j* 1.5, frame * 0.4) * 0.4 - 0.45));
                        break;
                    case '~':
                        portion[i][j] = (int) (100 * (PerlinNoise.noise(i* 1.5, j* 1.5, frame * 0.4) * 0.4 - 0.65));
                        break;
                    case '"':
                        portion[i][j] = (int) (75 * (PerlinNoise.noise(i* 1.5, j* 1.5, frame * 0.45) * 0.25 - 1.5));
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
     * animation frame if rippling water and waving grass using Perlin Noise are desired. Also allows additional chars
     * to be treated like deep and shallow water regarding the ripple animation.
     *
     * @param map           a char[][] that you want to be find background lightness modifiers for
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more
     * @param deepLiquid    a char that will be treated like deep water when animating ripples
     * @param shallowLiquid a char that will be treated like shallow water when animating ripples
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map, double frame, char deepLiquid, char shallowLiquid) {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case ':':
                        portion[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -10;
                        break;
                    case ',':
                        portion[i][j] = (int) (70 * (PerlinNoise.noise(i* 1.5, j* 1.5, frame * 0.4) * 0.4 - 0.45));
                        break;
                    case '~':
                        portion[i][j] = (int) (100 * (PerlinNoise.noise(i* 1.5, j* 1.5, frame * 0.4) * 0.4 - 0.65));
                        break;
                    case '"':
                        portion[i][j] = (int) (95 * (PerlinNoise.noise(i* 1.5, j* 1.5, frame * 0.45) * 0.3 - 1.5));
                        break;
                    case '^':
                        portion[i][j] = 40;
                        break;
                    default:
                        if (map[i][j] == deepLiquid)
                            portion[i][j] = (int) (180 * (PerlinNoise.noise(i * 4.2, j * 4.2, frame * 0.5) * 0.45 - 0.7));
                        else if (map[i][j] == shallowLiquid)
                            portion[i][j] = (int) (110 * (PerlinNoise.noise(i* 3.1, j* 3.1, frame * 0.25) * 0.4 - 0.65));
                        else portion[i][j] = 0;
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
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map suitable for use with the FOV class
     */
    public static double[][] generateResistances(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        double[][] portion = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case '/':
                    case '"':
                        portion[i][j] = 0.15;
                        break;
                    case '+':
                        portion[i][j] = 0.95;
                        break;
                    case '.':
                    case ',':
                    case '~':
                    case '^':
                    default:
                        portion[i][j] = 0.0;
                }
            }
        }
        return portion;
    }
    /**
     * Given a char[][] for the map, produces a double[][] that can be used with FOV.calculateFOV(), but does not treat
     * any cells as partly transparent, only fully-blocking or fully-permitting light. This is mainly useful if you
     * expect the FOV radius to be very high or (effectively) infinite, since anything less than complete blockage would
     * be passed through by infinite-radius FOV. This expects any doors to be represented by '+' if closed or '/' if
     * open (most door placement defaults to a mix of '+' and '/', so by calling
     * {@link DungeonUtility#closeDoors(char[][])} you can close all doors at the start), and any walls to be '#' or
     * line drawing characters. This will assign 1.0 resistance to walls and closed doors or 0.0 for any other cell.
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map suitable for use with the FOV class, but with no partially transparent cells
     */
    public static double[][] generateSimpleResistances(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        double[][] portion = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
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
                    case '+':
                        portion[i][j] = 1.0;
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
     * <p/>
     * The values in costs are multipliers, so should not be negative, should only be 0.0 in cases where you want
     * infinite movement across all adjacent squares of that kind, should be higher than 1.0 for difficult terrain (2.0
     * and 3.0 are reasonable), should be between 0.0 and 1.0 for easy terrain, and should be 1.0 for normal terrain.
     * If a cell should not be possible to enter for this character, 999.0 should be a reasonable value for a cost.
     * <p/>
     * An example use for this would be to make a creature unable to enter any non-water cell (like a fish),
     * unable to enter doorways (like some mythological versions of vampires), or to make a wheeled vehicle take more
     * time to move across rubble or rough terrain.
     * <p/>
     * A potentially common case that needs to be addressed is NPC movement onto staircases in games that have them;
     * some games may find it desirable for NPCs to block staircases and others may not, but in either case you should
     * give both '&gt;' and '&lt;', the standard characters for staircases, the same value in costs.
     *
     * @param map          a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors() .
     * @param costs        a Map of Character keys representing possible elements in map, and Double values for their cost.
     * @param defaultValue a double that will be used as the cost for any characters that don't have a key in costs.
     * @return a cost map suitable for use with DijkstraMap
     */
    public static double[][] generateCostMap(char[][] map, Map<Character, Double> costs, double defaultValue) {
        int width = map.length;
        int height = map[0].length;
        double[][] portion = new double[width][height];
        char current;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
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

    /**
     * Given a char[][] for the map, a Map of Character keys to Double values that will be used to determine costs, and
     * a double value for unhandled characters, produces a double[][] that can be used as a map by AStarSearch. It
     * expects any doors to be represented by '+' if closed or '/' if open (which can be caused by calling
     * DungeonUtility.closeDoors() ) and any walls to be '#' or line drawing characters. In the parameter costs, there
     * does not need to be an entry for '#' or any box drawing characters, but if one is present for '#' it will apply
     * that cost to both '#' and all box drawing characters, and if one is not present it will default to a negative
     * number, meaning it is impassable for AStarSearch. For any other entry in costs, a char in the 2D char array that
     * matches the key will correspond (at the same x,y position in the returned 2D double array) to that key's value in
     * costs. If a char is used in the map but does not have a corresponding key in costs, it will be given the value of
     * the parameter defaultValue, which is typically 0 unless a creature is limited to only moving in some terrain.
     * <p/>
     * The values in costs are different from those expected for DijkstraMap; negative numbers are impassable, 0 is the
     * cost for a normal walkable tile, and higher numbers are harder to enter.
     * <p/>
     * An example use for this would be to make a creature unable to enter any non-water cell (like a fish),
     * unable to enter doorways (like some mythological versions of vampires), or to make a wheeled vehicle take more
     * time to move across rubble or rough terrain.
     * <p/>
     * A potentially common case that needs to be addressed is NPC movement onto staircases in games that have them;
     * some games may find it desirable for NPCs to block staircases and others may not, but in either case you should
     * give both '&gt;' and '&lt;', the standard characters for staircases, the same value in costs.
     *
     * @param map          a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors() .
     * @param costs        a Map of Character keys representing possible elements in map, and Double values for their cost.
     * @param defaultValue a double that will be used as the cost for any characters that don't have a key in costs.
     * @return a cost map suitable for use with AStarSearch
     */
    public static double[][] generateAStarCostMap(char[][] map, Map<Character, Double> costs, double defaultValue) {
        int width = map.length;
        int height = map[0].length;
        double[][] portion = new double[width][height];
        char current;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
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
                                    : -1;
                            break;
                        default:
                            portion[i][j] = defaultValue;
                    }
                }
            }
        }
        return portion;
    }

    public static double[][] translateAStarToDijkstra(double[][] astar) {
        if (astar == null) return null;
        if (astar.length <= 0 || astar[0].length <= 0)
            return new double[0][0];
        double[][] dijkstra = new double[astar.length][astar[0].length];
        for (int x = 0; x < astar.length; x++) {
            for (int y = 0; y < astar[x].length; y++) {
                if (astar[x][y] < 0)
                    dijkstra[x][y] = DijkstraMap.WALL;
                else
                    dijkstra[x][y] = DijkstraMap.FLOOR;
            }
        }
        return dijkstra;
    }

    public static double[][] translateDijkstraToAStar(double[][] dijkstra) {
        if (dijkstra == null) return null;
        if (dijkstra.length <= 0 || dijkstra[0].length <= 0)
            return new double[0][0];
        double[][] astar = new double[dijkstra.length][dijkstra[0].length];
        for (int x = 0; x < dijkstra.length; x++) {
            for (int y = 0; y < dijkstra[x].length; y++) {
                if (dijkstra[x][y] > DijkstraMap.FLOOR)
                    astar[x][y] = -1;
                else
                    astar[x][y] = 1;
            }
        }
        return astar;
    }

    /**
     * @param rng
     * @param map
     * @param acceptable
     * @param frustration The number of trials that this method can do. Usually 16 or
     *                    32.
     * @return A random cell in {@code map} whose symbol is in
     * {@code acceptable}. Or {@code null} if not found.
     */
    public static /* @Nullable */Coord getRandomCell(IRNG rng, char[][] map, Set<Character> acceptable,
                                                     int frustration) {
        if (frustration < 0)
            throw new IllegalStateException("Frustration should not be negative");
        final int width = map.length;
        final int height = width == 0 ? 0 : map[0].length;
        if (width == 0 || height == 0)
            throw new IllegalStateException("Map must be non-empty to get a cell from it");
        int i = 0;
        while (i < frustration) {
            final int x = rng.nextInt(width);
            final int y = rng.nextInt(height);
            if (acceptable.contains(map[x][y]))
                return Coord.get(x, y);
            i++;
        }
        return null;
    }

    /**
     * @param level dungeon/map level as 2D char array. x,y indexed
     * @param c     Coord to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(char[][] level, Coord c) {
        return inLevel(level, c.x, c.y);
    }

    /**
     * @param level dungeon/map level as 2D char array. x,y indexed
     * @param x     x coordinate to check
     * @param y     y coordinate to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(char[][] level, int x, int y) {
        return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
    }

    /**
     * @param level dungeon/map level as 2D double array. x,y indexed
     * @param c     Coord to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(double[][] level, Coord c) {
        return inLevel(level, c.x, c.y);
    }

    /**
     * @param level dungeon/map level as 2D double array. x,y indexed
     * @param x     x coordinate to check
     * @param y     y coordinate to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(double[][] level, int x, int y) {
        return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
    }

    /**
     * @param level a dungeon/map level as 2D array. x,y indexed
     * @param c     Coord to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static <T> boolean inLevel(T[][] level, Coord c) {
        return inLevel(level, c.x, c.y);
    }

    /**
     * @param level a dungeon/map level as 2D array. x,y indexed
     * @param x     x coordinate to check
     * @param y     y coordinate to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static <T> boolean inLevel(T[][] level, int x, int y) {
        return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
    }
    
    /**
     * Quickly counts the number of char elements in level that are equal to match.
     *
     * @param level the 2D char array to count cells in
     * @param match the char to search for
     * @return the number of cells that matched
     */
    public static int countCells(char[][] level, char match) {
        if (level == null || level.length == 0)
            return 0;
        int counter = 0;
        for (int x = 0; x < level.length; x++) {
            for (int y = 0; y < level[x].length; y++) {
                if (level[x][y] == match) counter++;
            }
        }
        return counter;
    }

    /**
     * For when you want to print a 2D char array. Prints on multiple lines, with a trailing newline.
     *
     * @param level a 2D char array to print with a trailing newline
     */
    public static void debugPrint(char[][] level) {
        if (level == null || level.length == 0 || level[0].length == 0)
            System.out.println("INVALID DUNGEON LEVEL");
        else {
            for (int y = 0; y < level[0].length; y++) {
                for (int x = 0; x < level.length; x++) {
                    System.out.print(level[x][y]);
                }
                System.out.println();

            }
        }
    }

    /**
     * Changes the outer edge of a char[][] to the wall char, '#'.
     *
     * @param map A char[][] that stores map data; will be modified in place
     * @return the modified-in-place map with its edge replaced with '#'
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
    public static ArrayList<Coord> pointPath(int width, int height, IRNG rng) {
        if (width <= 2 || height <= 2)
            throw new IllegalArgumentException("width and height must be greater than 2");
        CoordPacker.init();
        long columnAlterations = (rng.nextLong() & 0xFFFFFFFFFFFFL);
        float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
        long rowAlterations = (rng.nextLong() & 0xFFFFFFFFFFFFL);
        float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);

        int[] columns = new int[16], rows = new int[16];
        int csum = 0, rsum = 0;
        long b = 7;
        for (int i = 0; i < 16; i++, b <<= 3) {
            columns[i] = csum + (int) (columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
            csum += (int) (columnBase * (3 + Long.bitCount(columnAlterations & b)));
            rows[i] = rsum + (int) (rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
            rsum += (int) (rowBase * (3 + Long.bitCount(rowAlterations & b)));
        }
        int cs = width - csum;
        int rs = height - rsum;
        int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
        for (int i = 0; i <= 7; i++) {
            cs2 = cs2 * i / 7;
            rs2 = rs2 * i / 7;
            columns[i] -= cs2;
            rows[i] -= rs2;
        }
        for (int i = 15; i >= 8; i--) {
            cs3 = cs3 * (i - 8) >> 3;
            rs3 = rs3 * (i - 8) >> 3;
            columns[i] += cs3;
            rows[i] += rs3;
        }

        ArrayList<Coord> points = new ArrayList<>(80);
        int m = rng.nextInt(64);
        Coord temp = CoordPacker.mooreToCoord(m), next;
        temp = Coord.get(columns[temp.x], rows[temp.y]);
        for (int i = 0, r; i < 256; r = rng.between(4, 12), i += r, m += r) {
            next = CoordPacker.mooreToCoord(m);
            next = Coord.get(columns[next.x], rows[next.y]);
            points.addAll(OrthoLine.line(temp, next));
            temp = next;
        }
        points.add(points.get(0));
        return points;
    }

    /**
     * Ensures a path exists in a rough ring around the map by first creating the path (using
     * {@link #pointPath(int, int, IRNG)} with the given IRNG), then finding chars in blocking that are on that path and
     * replacing them with replacement. Modifies map in-place (!) and returns an ArrayList of Coord points that will
     * always be on the path.
     *
     * @param map         a 2D char array, x then y, etc. that will be modified directly; this is the "returned map"
     * @param rng         used for random factors in the path choice
     * @param replacement the char that will fill be used where a path needs to be carved out; usually '.'
     * @param blocking    an array or vararg of char that are considered blocking for the path and will be replaced if
     *                    they are in the way
     * @return the ArrayList of Coord points that are on the carved path, including existing non-blocking cells; will be empty if any parameters are invalid
     */
    public static ArrayList<Coord> ensurePath(char[][] map, IRNG rng, char replacement, char... blocking) {
        if (map == null || map.length <= 0 || blocking == null || blocking.length <= 0)
            return new ArrayList<Coord>(0);
        int width = map.length, height = map[0].length;
        ArrayList<Coord> points = pointPath(width, height, rng);
        char[] blocks = new char[blocking.length];
        System.arraycopy(blocking, 0, blocks, 0, blocking.length);
        Arrays.sort(blocks);
        for (Coord c : points) {
            if (c.x >= 0 && c.x < width && c.y >= 0 && c.y < height && Arrays.binarySearch(blocks, map[c.x][c.y]) >= 0) {
                map[c.x][c.y] = replacement;
            }
        }
        return points;
    }

    public static ArrayList<Coord> allMatching(char[][] map, char... matching) {
        if (map == null || map.length <= 0 || matching == null || matching.length <= 0)
            return new ArrayList<Coord>(0);
        int width = map.length, height = map[0].length;
        char[] matches = new char[matching.length];
        System.arraycopy(matching, 0, matches, 0, matching.length);
        Arrays.sort(matches);
        ArrayList<Coord> points = new ArrayList<Coord>(map.length * 4);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Arrays.binarySearch(matches, map[x][y]) >= 0)
                    points.add(Coord.get(x, y));
            }
        }
        return points;
    }

    /**
     * Gets a List of Coord that are within radius distance of (x,y), and appends them to buf if it is non-null or makes
     * a fresh List to append to otherwise. Returns buf if non-null, else the fresh List of Coord. May produce Coord
     * values that are not within the boundaries of a map, such as (-5,-4), if the center is too close to the edge or
     * radius is too high. You can use {@link squidpony.squidgrid.Radius#inCircle(int, int, int, boolean, int, int, List)}
     * with surpassEdges as false if you want to limit Coords to within the map, or the more general
     * {@link squidpony.squidgrid.Radius#pointsInside(int, int, int, boolean, int, int, List)} on a Radius.SQUARE or
     * Radius.DIAMOND enum value if you want a square or diamond shape.
     *
     * @param x      center x of the circle
     * @param y      center y of the circle
     * @param radius inclusive radius to extend from the center; radius 0 gives just the center
     * @param buf    Where to add the coordinates, or null for this method to
     *               allocate a fresh list.
     * @return The coordinates of a circle centered {@code (x, y)}, whose
     * diameter is {@code (radius * 2) + 1}.
     * @see squidpony.squidgrid.Radius#inCircle(int, int, int, boolean, int, int, List) if you want to keep the Coords within the bounds of the map
     */
    public static List<Coord> circle(int x, int y, int radius, /* @Nullable */ List<Coord> buf) {
        final List<Coord> result = buf == null ? new ArrayList<Coord>() : buf;
        radius = Math.max(0, radius);
        for (int dx = -radius; dx <= radius; ++dx) {
            final int high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy) {
                result.add(Coord.get(x + dx, y + dy));
            }
        }
        return result;
    }

    /**
     * Fills {@code array2d} with {@code value}; delegates to ArrayTools, and using ArrayTools is preferred.
     * @param array2d a 2D array that will be modified in-place
     * @param value the value to fill all of array2D with
     * @deprecated Use {@link ArrayTools#fill(boolean[][], boolean)} instead
     */
    @Deprecated
    public static void fill(boolean[][] array2d, boolean value) {
        ArrayTools.fill(array2d, value);
    }
    /**
     * Fills {@code array2d} with {@code value}; delegates to ArrayTools, and using ArrayTools is preferred.
     * @param array2d a 2D array that will be modified in-place
     * @param value the value to fill all of array2D with
     * @deprecated Use {@link ArrayTools#fill(char[][], char)} instead
     */
    @Deprecated
    public static void fill(char[][] array2d, char value) {
        ArrayTools.fill(array2d, value);
    }
    /**
     * Fills {@code array2d} with {@code value}; delegates to ArrayTools, and using ArrayTools is preferred.
     * @param array2d a 2D array that will be modified in-place
     * @param value the value to fill all of array2D with
     * @deprecated Use {@link ArrayTools#fill(int[][], int)} instead
     */
    @Deprecated
    public static void fill(int[][] array2d, int value) {
        ArrayTools.fill(array2d, value);
    }
    /**
     * Fills {@code array2d} with {@code value}; delegates to ArrayTools, and using ArrayTools is preferred.
     * @param array2d a 2D array that will be modified in-place
     * @param value the value to fill all of array2D with
     * @deprecated Use {@link ArrayTools#fill(double[][], double)} instead
     */
    @Deprecated
    public static void fill(double[][] array2d, double value) {
        ArrayTools.fill(array2d, value);
    }
}