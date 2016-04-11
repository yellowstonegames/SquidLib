package squidpony.squidgrid.mapping;

import squidpony.GwtCompatibility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;

/**
 * A subsection of a (typically modern-day or sci-fi) area map that can be placed by ModularMapGenerator.
 * Created by Tommy Ettinger on 4/4/2016.
 */
public class MapModule {
    /**
     * The contents of this section of map.
     */
    public char[][] map;
    /**
     * Stores Coords just outside the contents of the MapModule, where doors are allowed to connect into this.
     * Uses Coord positions that are relative to this MapModule's map field, not whatever this is being placed into.
     */
    public Coord[] validDoors;
    /**
     * The minimum point on the bounding rectangle of the room, including walls.
     */
    public Coord min;
    /**
     * The maximum point on the bounding rectangle of the room, including walls.
     */
    public Coord max;
    private static final char[] validPacking = new char[]{'.', ',', '"', '^', '<', '>'};
    public MapModule()
    {
        this(CoordPacker.unpackChar(CoordPacker.rectangle(8, 8), 8, 8, '.', '#'));
    }

    /**
     * Constructs a MapModule given only a 2D char array as the contents of this section of map. The actual MapModule will
     * use a slightly larger 2D array to ensure walls can be drawn around it, and the valid locations for doors will be
     * any outer wall adjacent to a floor ('.'), shallow water (','), grass ('"'), trap  ('^'), or staircase (less than
     * or greater than signs). The max and min Coords of the bounding rectangle, including one layer of outer walls, will
     * also be calculated. Notably, the map you pass to this does not need to have outer walls present in it already.
     * @param map the 2D char array that contains the contents of this section of map
     */
    public MapModule(char[][] map)
    {
        if(map == null || map.length <= 0)
            throw new UnsupportedOperationException("Given map cannot be empty in MapModule");
        this.map = new char[map.length + 2][map[0].length + 2];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(map[i], 0, this.map[i+1], 1, map[i].length);
        }
        DungeonUtility.wallWrap(this.map);
        short[] pk = CoordPacker.fringe(
                CoordPacker.pack(this.map, validPacking),
                1, this.map.length, this.map[0].length, false);
        validDoors = CoordPacker.allPacked(pk);
        Coord[] tmp = CoordPacker.bounds(pk);
        min = tmp[0];
        max = tmp[1];
    }
    /**
     * Constructs a MapModule given only a short array of packed data (as produced by CoordPacker and consumed or produced
     * by several other classes) that when unpacked will yield the contents of this section of map. The actual MapModule
     * will use a slightly larger 2D array than the given width and height to ensure walls can be drawn around the floors,
     * and the valid locations for doors will be any outer wall adjacent to an "on" coordinate in packed. The max and min
     * Coords of the bounding rectangle, including one layer of outer walls, will also be calculated. Notably, the packed
     * data you pass to this does not need to have a gap between floors and the edge of the map to make walls.
     * @param packed the short array, as packed data from CoordPacker, that contains the contents of this section of map
     */
    public MapModule(short[] packed, int width, int height)
    {
        this(CoordPacker.unpackChar(packed, width, height, '.', '#'));
    }
    /**
     * Constructs a MapModule given a 2D char array as the contents of this section of map and a 2D boolean array that
     * represents viable locations to place doors (hopefully in walls, though technically they can be anywhere). The
     * actual MapModule will use a slightly larger 2D array than map to ensure walls can be drawn around it. The max and
     * min Coords of the bounding rectangle, including one layer of outer walls, will also be calculated. Notably, the
     * map you pass to this does not need to have outer walls present in it already, but unlike the constructor that takes
     * only a 2D char array, there should be some.
     * @param map the 2D char array that contains the contents of this section of map
     * @param possibleDoors a 2D boolean array that should match map's dimensions, where true means a possible door
     */
    public MapModule(char[][] map, boolean[][] possibleDoors)
    {

        if(map == null || map.length <= 0 || possibleDoors == null || possibleDoors.length <= 0)
            throw new UnsupportedOperationException("Given map and possibleDoors cannot be empty in MapModule");
        this.map = new char[map.length + 2][map[0].length + 2];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(map[i], 0, this.map[i+1], 1, map[i].length);
        }
        DungeonUtility.wallWrap(this.map);
        short[] pk = CoordPacker.translate(CoordPacker.pack(possibleDoors), 1, 1, this.map.length, this.map[0].length);
        validDoors = CoordPacker.allPacked(pk);
        Coord[] tmp = CoordPacker.bounds(pk);
        if(tmp[0].x < 0) // negative coordinates from bounds mean there were no possible doors
            throw new UnsupportedOperationException("No possible doors; MapModule is probably invalid");
        min = tmp[0];
        max = tmp[1];
    }

    /**
     * Constructs a MapModule from the given arguments without modifying them, copying map without changing its size,
     * copying validDoors, and using the same min and max (which are immutable, so they can be reused).
     * @param map the 2D char array that contains the contents of this section of map; will be copied exactly
     * @param validDoors a Coord array that stores viable locations to place doors in map; will be cloned
     * @param min the minimum Coord of this MapModule's bounding rectangle
     * @param max the maximum Coord of this MapModule's bounding rectangle
     */
    public MapModule(char[][] map, Coord[] validDoors, Coord min, Coord max)
    {
        this.map = new char[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(map[i], 0, this.map[i], 0, map[i].length);
        }
        this.validDoors = GwtCompatibility.cloneCoords(validDoors);
        this.min = min;
        this.max = max;
    }

    /**
     * Copies another MapModule and uses it to construct a new one.
     * @param other an already-constructed MapModule that this will copy
     */
    public MapModule(MapModule other)
    {
        this(other.map, other.validDoors, other.min, other.max);
    }

    /**
     * Rotates a copy of this MapModule by the given number of 90-degree turns. Describing the turns as clockwise or
     * counter-clockwise depends on whether the y-axis "points up" or "points down." If higher values for y are toward the
     * bottom of the screen (the default for when 2D arrays are printed), a turn of 1 is clockwise 90 degrees, but if the
     * opposite is true and higher y is toward the top, then a turn of 1 is counter-clockwise 90 degrees.
     * @param turns the number of 90 degree turns to adjust this by
     * @return a new MapModule (copied from this one) that has been rotated by the given amount
     */
    public MapModule rotate(int turns)
    {
        turns %= 4;
        char[][] map2;
        Coord[] doors2;
        Coord min2, max2;
        int xSize = map.length - 1, ySize = map[0].length - 1;
        switch (turns)
        {
            case 1:
                map2 = new char[map[0].length][map.length];
                for (int i = 0; i < map.length; i++) {
                    for (int j = 0; j < map[0].length; j++) {
                        map2[ySize - j][i] = map[i][j];
                    }
                }
                doors2 = new Coord[validDoors.length];
                for (int i = 0; i < validDoors.length; i++) {
                    doors2[i] = Coord.get(ySize - validDoors[i].y, validDoors[i].x);
                }
                min2 = Coord.get(ySize - max.y, min.x);
                max2 = Coord.get(ySize - min.y, max.x);
                return new MapModule(map2, doors2, min2, max2);
            case 2:
                map2 = new char[map[0].length][map.length];
                for (int i = 0; i < map.length; i++) {
                    for (int j = 0; j < map[0].length; j++) {
                        map2[xSize - i][ySize - j] = map[i][j];
                    }
                }
                doors2 = new Coord[validDoors.length];
                for (int i = 0; i < validDoors.length; i++) {
                    doors2[i] = Coord.get(xSize - validDoors[i].x, ySize - validDoors[i].y);
                }
                min2 = Coord.get(xSize - min.x, ySize - max.y);
                max2 = Coord.get(xSize - max.x, ySize - min.y);
                return new MapModule(map2, doors2, min2, max2);
            case 3:
                map2 = new char[map[0].length][map.length];
                for (int i = 0; i < map.length; i++) {
                    for (int j = 0; j < map[0].length; j++) {
                        map2[j][xSize - i] = map[i][j];
                    }
                }
                doors2 = new Coord[validDoors.length];
                for (int i = 0; i < validDoors.length; i++) {
                    doors2[i] = Coord.get(validDoors[i].y, xSize - validDoors[i].x);
                }
                min2 = Coord.get(min.y, xSize - max.x);
                max2 = Coord.get(max.y, xSize - min.x);
                return new MapModule(map2, doors2, min2, max2);
            default:
                return new MapModule(map, validDoors, min, max);
        }
    }

    public MapModule flip(boolean flipLeftRight, boolean flipUpDown)
    {
        if(!flipLeftRight && !flipUpDown)
            return new MapModule(map, validDoors, min, max);
        char[][] map2 = new char[map.length][map[0].length];
        Coord[] doors2 = new Coord[validDoors.length];
        Coord min2, max2;
        int xSize = map.length - 1, ySize = map[0].length - 1;
        if(flipLeftRight && flipUpDown)
        {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    map2[xSize - i][ySize - j] = map[i][j];
                }
            }
            for (int i = 0; i < validDoors.length; i++) {
                doors2[i] = Coord.get(xSize - validDoors[i].x, ySize - validDoors[i].y);
            }
            min2 = Coord.get(xSize - max.x, ySize - max.y);
            max2 = Coord.get(xSize - min.x, xSize - min.y);
        }
        else if(flipLeftRight)
        {
            for (int i = 0; i < map.length; i++) {
                System.arraycopy(map[i], 0, map2[xSize - i], 0, map[0].length);
            }
            for (int i = 0; i < validDoors.length; i++) {
                doors2[i] = Coord.get(xSize - validDoors[i].x, validDoors[i].y);
            }
            min2 = Coord.get(xSize - max.x, min.y);
            max2 = Coord.get(xSize - min.x, max.y);
        }
        else
        {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    map2[i][ySize - j] = map[i][j];
                }
            }
            for (int i = 0; i < validDoors.length; i++) {
                doors2[i] = Coord.get(validDoors[i].x, ySize - validDoors[i].y);
            }
            min2 = Coord.get(min.x, ySize - max.y);
            max2 = Coord.get(max.x, xSize - min.y);
        }
        return new MapModule(map2, doors2, min2, max2);
    }
}
