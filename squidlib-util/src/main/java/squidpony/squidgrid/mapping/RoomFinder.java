package squidpony.squidgrid.mapping;

import squidpony.squidmath.Coord;
import squidpony.squidmath.RegionMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static squidpony.squidmath.CoordPacker.*;

/**
 * A small class that can analyze a dungeon or other map and identify areas as being "room" or "corridor" based on how
 * thick the walkable areas are (corridors are at most 2 cells wide at their widest, rooms are anything else). Most
 * methods of this class return 2D char arrays or Lists thereof, with the subset of the map that is in a specific region
 * kept the same, but everything else replaced with '#'.
 * Created by Tommy Ettinger on 2/3/2016.
 */
public class RoomFinder {
    /**
     * A copy of the dungeon map, however it was passed to the constructor.
     */
    public char[][] map,
    /**
     * A simplified version of the dungeon map, using '#' for walls and '.' for floors.
     */
    basic;
    /**
     * Not likely to be used directly, but there may be things you can do with these that are cumbersome using only
     * RoomFinder's simpler API.
     */
    public RegionMap<List<short[]>> rooms,
    /**
     * Not likely to be used directly, but there may be things you can do with these that are cumbersome using only
     * RoomFinder's simpler API.
     */
    corridors,
    /**
     * Not likely to be used directly, but there may be things you can do with these that are cumbersome using only
     * RoomFinder's simpler API. Won't be assigned a value if this class is constructed with a 2D char array; it needs
     * the two-arg constructor using the environment produced by a MixedGenerator, SerpentMapGenerator, or similar.
     */
    caves;
    /**
     * When a RoomFinder is constructed, it stores all points of rooms that are adjacent to another region here.
     */
    public Coord[] connections;
    public int width, height;

    private RoomFinder()
    {

    }

    /**
     * Constructs a RoomFinder given a dungeon map, and finds rooms, corridors, and their connections on the map. Does
     * not find caves; if a collection of caves is requested from this, it will be non-null but empty.
     * @param dungeon a 2D char array that uses '#' for walls.
     */
    public RoomFinder(char[][] dungeon)
    {
        if(dungeon.length <= 0)
            return;
        width = dungeon.length;
        height = dungeon[0].length;
        map = new char[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(dungeon[i], 0, map[i], 0, height);
        }
        rooms = new RegionMap<>(32);
        corridors = new RegionMap<>(32);
        caves = new RegionMap<>(8);
        basic = DungeonUtility.simplifyDungeon(map);
        short[] floors = pack(basic, '.'),
                r = flood(floors, retract(floors, 1, width, height, true), 2, false),
                c = differencePacked(floors, r),
                d = intersectPacked(r, fringe(c, 1, width, height, false));
        connections = allPacked(d);
        List<short[]> rs = split(r), cs = split(c);

        short[][] ra = rs.toArray(new short[rs.size()][]),
                ca = cs.toArray(new short[cs.size()][]);

        for (short[] sep : cs) {
            short[] someDoors = intersectPacked(r, fringe(sep, 1, width, height, false));
            short[] doors = allPackedHilbert(someDoors);
            List<short[]> near = new ArrayList<short[]>(4);
            for (int j = 0; j < doors.length; j++) {
                near.addAll(findManyPackedHilbert(doors[j], ra));
            }
            corridors.put(sep, near);
        }

        for (short[] sep : rs) {
            List<short[]> near = new ArrayList<short[]>(10);
            short[] aroundDoors = intersectPacked(c, fringe(sep, 1, width, height, false));
            short[] doors = allPackedHilbert(aroundDoors);
            for (int j = 0; j < doors.length; j++) {
                near.addAll(findManyPackedHilbert(doors[j], ca));
            }
            rooms.put(sep, near);
        }
    }

    public RoomFinder(char[][] dungeon, int environmentKind)
    {
        if(dungeon.length <= 0)
            return;
        width = dungeon.length;
        height = dungeon[0].length;
        map = new char[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(dungeon[i], 0, map[i], 0, height);
        }
        rooms = new RegionMap<>(32);
        corridors = new RegionMap<>(32);
        caves = new RegionMap<>(8);
        basic = DungeonUtility.simplifyDungeon(map);

        if(environmentKind == MixedGenerator.ROOM_FLOOR) {
            short[] floors = pack(basic, '.'),
                    r = flood(floors, retract(floors, 1, width, height, true), 2, false),
                    c = differencePacked(floors, r),
                    d = intersectPacked(r, fringe(c, 1, width, height, false));
            connections = allPacked(d);
            List<short[]> rs = split(r), cs = split(c);

            short[][] ra = rs.toArray(new short[rs.size()][]),
                    ca = cs.toArray(new short[cs.size()][]);

            for (short[] sep : cs) {
                short[] someDoors = intersectPacked(r, fringe(sep, 1, width, height, false));
                short[] doors = allPackedHilbert(someDoors);
                List<short[]> near = new ArrayList<short[]>(4);
                for (int j = 0; j < doors.length; j++) {
                    near.addAll(findManyPackedHilbert(doors[j], ra));
                }
                corridors.put(sep, near);
            }

            for (short[] sep : rs) {
                List<short[]> near = new ArrayList<short[]>(10);
                short[] aroundDoors = intersectPacked(c, fringe(sep, 1, width, height, false));
                short[] doors = allPackedHilbert(aroundDoors);
                for (int j = 0; j < doors.length; j++) {
                    near.addAll(findManyPackedHilbert(doors[j], ca));
                }
                rooms.put(sep, near);
            }
        }
        else
        {
            short[] floors = pack(basic, '.');
            caves.put(floors, new ArrayList<short[]>());
            connections = allPacked(retract(
                    differencePacked(floors, retract(floors, 1, width, height, true)),
                    1, width, height, false));
        }
    }

    /**
     * Constructs a RoomFinder given a dungeon map and an environment map (which currently is only produced by
     * MixedGenerator by the getEnvironment() method after generate() is called, but other classes that use
     * MixedGenerator may also expose that environment, such as SerpentMapGenerator.getEnvironment()), and finds rooms,
     * corridors, caves, and their connections on the map.
     * @param dungeon a 2D char array that uses '#' for walls.
     * @param environment a 2D int array using constants from MixedGenerator; typically produced by a call to
     *                    getEnvironment() in MixedGenerator or SerpentMapGenerator after dungeon generation.
     */
    public RoomFinder(char[][] dungeon, int[][] environment)
    {
        if(dungeon.length <= 0)
            return;
        width = dungeon.length;
        height = dungeon[0].length;
        map = new char[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(dungeon[i], 0, map[i], 0, height);
        }
        rooms = new RegionMap<>(32);
        corridors = new RegionMap<>(32);
        caves = new RegionMap<>(32);

        basic = DungeonUtility.simplifyDungeon(map);
        short[] floors = pack(basic, '.'),
                r = pack(environment, MixedGenerator.ROOM_FLOOR),
                c = pack(environment, MixedGenerator.CORRIDOR_FLOOR),
                v = pack(environment, MixedGenerator.CAVE_FLOOR),
                rc = unionPacked(r, c),
                d = intersectPacked(r, fringe(c, 1, width, height, false)),
                m = intersectPacked(rc, fringe(v, 1, width, height, false));
        connections = allPacked(unionPacked(d, m));
        List<short[]> rs = split(r), cs = split(c), vs = split(v);
        short[][] ra = rs.toArray(new short[rs.size()][]),
                ca = cs.toArray(new short[cs.size()][]),
                va = vs.toArray(new short[vs.size()][]);

        for (short[] sep : cs) {
            short[] someDoors = intersectPacked(r, fringe(sep, 1, width, height, false));
            short[] doors = allPackedHilbert(someDoors);
            List<short[]> near = new ArrayList<short[]>(16);
            for (int j = 0; j < doors.length; j++) {
                near.addAll(findManyPackedHilbert(doors[j], ra));
            }
            someDoors = intersectPacked(v, fringe(sep, 1, width, height, false));
            doors = allPackedHilbert(someDoors);
            for (int j = 0; j < doors.length; j++) {
                near.addAll(findManyPackedHilbert(doors[j], va));
            }
            corridors.put(sep, near);
        }

        for (short[] sep : rs) {
            List<short[]> near = new ArrayList<short[]>(32);
            short[] aroundDoors = intersectPacked(c, fringe(sep, 1, width, height, false));
            short[] doors = allPackedHilbert(aroundDoors);
            for (int j = 0; j < doors.length; j++) {
                near.addAll(findManyPackedHilbert(doors[j], ca));
            }
            aroundDoors = intersectPacked(v, fringe(sep, 1, width, height, false));
            doors = allPackedHilbert(aroundDoors);
            for (int j = 0; j < doors.length; j++) {
                near.addAll(findManyPackedHilbert(doors[j], va));
            }
            rooms.put(sep, near);
        }

        for (short[] sep : vs) {
            List<short[]> near = new ArrayList<short[]>(48);
            short[] aroundMouths = intersectPacked(c, fringe(sep, 1, width, height, false));
            short[] mouths = allPackedHilbert(aroundMouths);
            for (int j = 0; j < mouths.length; j++) {
                near.addAll(findManyPackedHilbert(mouths[j], ca));
            }
            aroundMouths = intersectPacked(r, fringe(sep, 1, width, height, false));
            mouths = allPackedHilbert(aroundMouths);
            for (int j = 0; j < mouths.length; j++) {
                near.addAll(findManyPackedHilbert(mouths[j], ra));
            }
            caves.put(sep, near);
        }

    }

    /**
     * Gets all the rooms this found during construction, returning them as an ArrayList of 2D char arrays, where an
     * individual room is "masked" so only its contents have normal map chars and the rest have only '#'.
     * @return an ArrayList of 2D char arrays representing rooms.
     */
    public ArrayList<char[][]> findRooms()
    {
        ArrayList<char[][]> rs = new ArrayList<char[][]>(rooms.size);
        for(short[] r : rooms.keys())
        {
            rs.add(mask(map, r, '#'));
        }
        return rs;
    }

    /**
     * Gets all the corridors this found during construction, returning them as an ArrayList of 2D char arrays, where an
     * individual corridor is "masked" so only its contents have normal map chars and the rest have only '#'.
     * @return an ArrayList of 2D char arrays representing corridors.
     */
    public ArrayList<char[][]> findCorridors()
    {
        ArrayList<char[][]> cs = new ArrayList<char[][]>(corridors.size);
        for(short[] c : corridors.keys())
        {
            cs.add(mask(map, c, '#'));
        }
        return cs;
    }

    /**
     * Gets all the caves this found during construction, returning them as an ArrayList of 2D char arrays, where an
     * individual room is "masked" so only its contents have normal map chars and the rest have only '#'. Will only
     * return a non-empty collection if the two-arg constructor was used and the environment contains caves.
     * @return an ArrayList of 2D char arrays representing caves.
     */
    public ArrayList<char[][]> findCaves()
    {
        ArrayList<char[][]> vs = new ArrayList<char[][]>(caves.size);
        for(short[] v : caves.keys())
        {
            vs.add(mask(map, v, '#'));
        }
        return vs;
    }
    /**
     * Gets all the rooms, corridors, and caves this found during construction, returning them as an ArrayList of 2D
     * char arrays, where an individual room or corridor is "masked" so only its contents have normal map chars and the
     * rest have only '#'.
     * @return an ArrayList of 2D char arrays representing rooms, corridors, or caves.
     */
    public ArrayList<char[][]> findRegions()
    {
        ArrayList<char[][]> rs = new ArrayList<char[][]>(rooms.size + corridors.size + caves.size);
        for(short[] r : rooms.keys())
        {
            rs.add(mask(map, r, '#'));
        }
        for(short[] r : corridors.keys()) {
            rs.add(mask(map, r, '#'));
        }
        for(short[] r : caves.keys()) {
            rs.add(mask(map, r, '#'));
        }
        return rs;
    }
    protected static char[][] defaultFill(int width, int height)
    {
        char[][] d = new char[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(d[x], '#');
        }
        return d;
    }

    /**
     * Merges multiple 2D char arrays where the '#' character means "no value", and combines them so all cells with
     * value are on one map, with '#' filling any other cells. If regions is empty, this uses width and height to
     * construct a blank map, all '#'. It will also use width and height for the size of the returned 2D array.
     * @param regions An ArrayList of 2D char array regions, where '#' is an empty value and all others will be merged
     * @param width the width of any map this returns
     * @param height the height of any map this returns
     * @return a 2D char array that merges all non-'#' areas in regions, and fills the rest with '#'
     */
    public static char[][] merge(ArrayList<char[][]> regions, int width, int height)
    {
        if(regions == null || regions.isEmpty())
            return defaultFill(width, height);
        char[][] first = regions.get(0);
        char[][] dungeon = new char[first.length][first[0].length];
        for (int x = 0; x < first.length; x++) {
            Arrays.fill(dungeon[x], '#');
        }
        for(char[][] region : regions)
        {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(region[x][y] != '#')
                        dungeon[x][y] = region[x][y];
                }
            }
        }
        return dungeon;
    }

    /**
     * Takes an x, y position and finds the room, corridor, or cave at that position, if there is one, returning the
     * same 2D char array format as the other methods.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region is '#'
     */
    public char[][] regionAt(int x, int y)
    {
        ArrayList<short[]> regions = rooms.regionsContaining(x, y);
        regions.addAll(corridors.regionsContaining(x, y));
        regions.addAll(caves.regionsContaining(x, y));
        short[] found;
        if(regions.isEmpty())
            found = ALL_WALL;
        else
            found = regions.get(0);
        return mask(map, found, '#');
    }

    /**
     * Takes an x, y position and finds the room or corridor at that position and the rooms, corridors or caves that it
     * directly connects to, and returns the group as one merged 2D char array.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region or one nearby is '#'
     */
    public char[][] regionsNear(int x, int y)
    {
        ArrayList<short[]> regions = rooms.regionsContaining(x, y);
        regions.addAll(corridors.regionsContaining(x, y));
        regions.addAll(caves.regionsContaining(x, y));
        short[] found;
        if(regions.isEmpty())
            found = ALL_WALL;
        else
        {
            found = regions.get(0);
            ArrayList<List<short[]>> near = rooms.allAt(x, y);
            for (List<short[]> links : near) {
                for(short[] n : links)
                {
                    found = unionPacked(found, n);
                }
            }
            near = corridors.allAt(x, y);
            for (List<short[]> links : near) {
                for(short[] n : links)
                {
                    found = unionPacked(found, n);
                }
            }
            near = caves.allAt(x, y);
            for (List<short[]> links : near) {
                for(short[] n : links)
                {
                    found = unionPacked(found, n);
                }
            }
        }
        return mask(map, found, '#');
    }

    /**
     * Takes an x, y position and finds the rooms or corridors that are directly connected to the room, corridor or cave
     * at that position, and returns the group as an ArrayList of 2D char arrays, one per connecting region.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return an ArrayList of masked 2D char arrays where anything not in a connected region is '#'
     */
    public ArrayList<char[][]> regionsConnected(int x, int y)
    {
        ArrayList<char[][]> regions = new ArrayList<char[][]>(10);
        ArrayList<List<short[]>> near = rooms.allAt(x, y);
        for (List<short[]> links : near) {
            for(short[] n : links)
            {
                regions.add(mask(map, n, '#'));
            }
        }
        near = corridors.allAt(x, y);
        for (List<short[]> links : near) {
            for (short[] n : links) {
                regions.add(mask(map, n, '#'));
            }
        }
        near = caves.allAt(x, y);
        for (List<short[]> links : near) {
            for (short[] n : links) {
                regions.add(mask(map, n, '#'));
            }
        }

        return regions;
    }
}
