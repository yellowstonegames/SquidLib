package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.NumberTools;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A subsection of a (typically modern-day or sci-fi) area map that can be placed by ModularMapGenerator.
 * <br>
 * This is marked Beta because both MapModule and {@link ModularMapGenerator} need improvement to be actually usable,
 * but it might be a while before there's a clear pathway towards how they can be improved. 
 * <br>
 * Created by Tommy Ettinger on 4/4/2016.
 */
@Beta
public class MapModule implements Comparable<MapModule>, Serializable {
    private static final long serialVersionUID = -1273406898212937188L;

    /**
     * The contents of this section of map.
     */
    public char[][] map;
    /**
     * The room/cave/corridor/wall status for each cell of this section of map.
     */
    public int[][] environment;
    /**
     * Stores Coords just outside the contents of the MapModule, where doors are allowed to connect into this.
     * Uses Coord positions that are relative to this MapModule's map field, not whatever this is being placed into.
     */
    public GreasedRegion validDoors;
    /**
     * The minimum point on the bounding rectangle of the room, including walls.
     */
    public Coord min;
    /**
     * The maximum point on the bounding rectangle of the room, including walls.
     */
    public Coord max;

    public ArrayList<Coord> leftDoors, rightDoors, topDoors, bottomDoors;

    public int category;

    private static final char[]
            validPacking = new char[]{'.', ',', '"', '^', '<', '>'},
            doors = new char[]{'+', '/'};
    public MapModule()
    {
        this(DungeonUtility.wallWrap(ArrayTools.fill('.', 8, 8)));
    }

    /**
     * Constructs a MapModule given only a 2D char array as the contents of this section of map. The actual MapModule
     * will use doors in the 2D char array as '+' or '/' if present. Otherwise, the valid locations for doors will be
     * any outer wall adjacent to a floor ('.'), shallow water (','), grass ('"'), trap  ('^'), or staircase (less than
     * or greater than signs). The max and min Coords of the bounding rectangle, including one layer of outer walls,
     * will also be calculated. The map you pass to this does need to have outer walls present in it already.
     * @param map the 2D char array that contains the contents of this section of map
     */
    public MapModule(char[][] map)
    {
        if(map == null || map.length <= 0)
            throw new UnsupportedOperationException("Given map cannot be empty in MapModule");
        this.map = ArrayTools.copy(map);
        environment = ArrayTools.fill(DungeonUtility.ROOM_FLOOR, this.map.length, this.map[0].length);
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if(this.map[x][y] == '#')
                    environment[x][y] = DungeonUtility.ROOM_WALL;
            }
        }
        GreasedRegion pack = new GreasedRegion(this.map, validPacking).fringe();
//        short[] pk = CoordPacker.fringe(
//                CoordPacker.pack(this.map, validPacking),
//                1, this.map.length, this.map[0].length, false, true);
//        Coord[] tmp = CoordPacker.bounds(pk);
        min = Coord.get(pack.xBound(true), pack.yBound(true));
        max = Coord.get(pack.xBound(false), pack.yBound(false));
        category = categorize(Math.max(max.x, max.y));
        validDoors = new GreasedRegion(this.map, doors);
        if(validDoors.size() < 2)
        {
            validDoors.remake(pack).quasiRandomRegion(0.2);
        }
        initSides();
    }

    /**
     * Constructs a MapModule from the given arguments without modifying them, copying map without changing its size,
     * copying validDoors, and using the same min and max (which are immutable, so they can be reused).
     * @param map the 2D char array that contains the contents of this section of map; will be copied exactly
     * @param validDoors a Coord array that stores viable locations to place doors in map; will be cloned
     * @param min the minimum Coord of this MapModule's bounding rectangle
     * @param max the maximum Coord of this MapModule's bounding rectangle
     */
    public MapModule(char[][] map, GreasedRegion validDoors, Coord min, Coord max)
    {
        this.map = ArrayTools.copy(map);
        environment = ArrayTools.fill(DungeonUtility.ROOM_FLOOR, this.map.length, this.map[0].length);
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if(this.map[x][y] == '#')
                    environment[x][y] = DungeonUtility.ROOM_WALL;
            }
        }
        this.min = min;
        this.max = max;
        category = categorize(Math.max(max.x, max.y));
        
        this.validDoors = new GreasedRegion(map, doors);
        if(this.validDoors.isEmpty()) this.validDoors.remake(validDoors);
        
        initSides();
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
        turns &= 3;
        char[][] map2;
        Coord min2, max2;
        GreasedRegion doors2;
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
                doors2 = new GreasedRegion(validDoors.height, validDoors.width);
                for (int i = 0; i < validDoors.size(); i++) {
                    Coord n = validDoors.nth(i);
                    doors2.insert(ySize - n.y, n.x);
                }
                min2 = Coord.get(ySize - max.y, min.x);
                max2 = Coord.get(ySize - min.y, max.x);
                return new MapModule(map2, doors2, min2, max2);
            case 2:
                map2 = new char[map.length][map[0].length];
                for (int i = 0; i < map.length; i++) {
                    for (int j = 0; j < map[0].length; j++) {
                        map2[xSize - i][ySize - j] = map[i][j];
                    }
                }
                doors2 = new GreasedRegion(validDoors.width, validDoors.height);
                for (int i = 0; i < validDoors.size(); i++) {
                    Coord n = validDoors.nth(i);
                    doors2.insert(xSize - n.x, ySize - n.y);
                }
                min2 = Coord.get(xSize - max.x, ySize - max.y);
                max2 = Coord.get(xSize - min.x, ySize - min.y);
                return new MapModule(map2, doors2, min2, max2);
            case 3:
                map2 = new char[map[0].length][map.length];
                for (int i = 0; i < map.length; i++) {
                    for (int j = 0; j < map[0].length; j++) {
                        map2[j][xSize - i] = map[i][j];
                    }
                }
                doors2 = new GreasedRegion(validDoors.height, validDoors.width);
                for (int i = 0; i < validDoors.size(); i++) {
                    Coord n = validDoors.nth(i);
                    doors2.insert(n.y, xSize - n.x);
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
        GreasedRegion doors2 = new GreasedRegion(map.length, map[0].length);
        Coord min2, max2;
        int xSize = map.length - 1, ySize = map[0].length - 1;
        if(flipLeftRight && flipUpDown)
        {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    map2[xSize - i][ySize - j] = map[i][j];
                }
            }
            for (int i = 0; i < validDoors.size(); i++) {
                Coord n = validDoors.nth(i);
                doors2.insert(xSize - n.x, ySize - n.y);
            }
            min2 = Coord.get(xSize - max.x, ySize - max.y);
            max2 = Coord.get(xSize - min.x, xSize - min.y);
        }
        else if(flipLeftRight)
        {
            for (int i = 0; i < map.length; i++) {
                System.arraycopy(map[i], 0, map2[xSize - i], 0, map[0].length);
            }
            for (int i = 0; i < validDoors.size(); i++) {
                Coord n = validDoors.nth(i);
                doors2.insert(xSize - n.x, n.y);
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
            for (int i = 0; i < validDoors.size(); i++) {
                Coord n = validDoors.nth(i);
                doors2.insert(n.x, ySize - n.y);
            }
            min2 = Coord.get(min.x, ySize - max.y);
            max2 = Coord.get(max.x, xSize - min.y);
        }
        return new MapModule(map2, doors2, min2, max2);
    }

    private static int categorize(int n)
    {
        int highest = Integer.highestOneBit(n);
        return Math.max(4, (highest == NumberTools.lowestOneBit(n)) ? highest : highest << 1);
    }
    private void initSides()
    {
        leftDoors = new ArrayList<>(8);
        rightDoors = new ArrayList<>(8);
        topDoors = new ArrayList<>(8);
        bottomDoors = new ArrayList<>(8);
        for(Coord dr : validDoors)
        {
            if(dr.x * max.y < dr.y * max.x && dr.y * max.x < (max.x - dr.x) * max.y)
                leftDoors.add(dr);
            else if(dr.x * max.y > dr.y * max.x && dr.y * max.x > (max.x - dr.x) * max.y)
                rightDoors.add(dr);
            else if(dr.x * max.y > dr.y * max.x && dr.y * max.x < (max.x - dr.x) * max.y)
                topDoors.add(dr);
            else if(dr.x * max.y < dr.y * max.x && dr.y * max.x > (max.x - dr.x) * max.y)
                bottomDoors.add(dr);
        }
    }

    @Override
    public int compareTo(MapModule o) {
        if(o == null) return 1;
        return category - o.category;
    }
}
