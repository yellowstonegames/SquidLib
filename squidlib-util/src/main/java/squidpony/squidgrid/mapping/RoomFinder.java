package squidpony.squidgrid.mapping;

import squidpony.squidmath.Coord;
import squidpony.squidmath.RegionMap;

import java.util.ArrayList;
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
    corridors;
    /**
     * When a RoomFinder is constructed, it stores all points of rooms that are adjacent to another region here.
     */
    public Coord[] connections;
    public int width, height;

    private RoomFinder()
    {

    }

    /**
     * Constructs a RoomFinder given a dungeon map, and finds rooms, corridors, and their connections on the map.
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
     * Gets all the rooms and corridors this found during construction, returning them as an ArrayList of 2D char
     * arrays, where an individual room or corridor is "masked" so only its contents have normal map chars and the rest
     * have only '#'.
     * @return an ArrayList of 2D char arrays representing rooms or corridors.
     */
    public ArrayList<char[][]> findRegions()
    {
        ArrayList<char[][]> rs = new ArrayList<char[][]>(rooms.size + corridors.size);
        for(short[] r : rooms.keys())
        {
            rs.add(mask(map, r, '#'));
        }
        for(short[] r : corridors.keys()) {
            rs.add(mask(map, r, '#'));
        }
        return rs;
    }

    /**
     * Takes an x, y position and finds the room or corridor at that position, if there is one, returning the same 2D
     * char array format as the other methods.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region is '#'
     */
    public char[][] regionAt(int x, int y)
    {
        ArrayList<short[]> regions = rooms.regionsContaining(x, y);
        regions.addAll(corridors.regionsContaining(x, y));
        short[] found;
        if(regions.isEmpty())
            found = ALL_WALL;
        else
            found = regions.get(0);
        return mask(map, found, '#');
    }

    /**
     * Takes an x, y position and finds the room or corridor at that position and the rooms or corridors that it
     * directly connects to, and returns the group as one merged 2D char array.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return a masked 2D char array where anything not in the current region or one nearby is '#'
     */
    public char[][] regionsNear(int x, int y)
    {
        ArrayList<short[]> regions = rooms.regionsContaining(x, y);
        regions.addAll(corridors.regionsContaining(x, y));
        short[] found;
        if(regions.isEmpty())
            found = ALL_WALL;
        else
        {
            found = regions.get(0);
            Iterable<List<short[]>> near = rooms.allAt(x, y);
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
        }
        return mask(map, found, '#');
    }

    /**
     * Takes an x, y position and finds the rooms or corridors that are directly connected to the room or corridor at
     * that position, and returns the group as an ArrayList of 2D char arrays, one per connecting region.
     * @param x the x coordinate of a position that should be in a room or corridor
     * @param y the y coordinate of a position that should be in a room or corridor
     * @return an ArrayList of masked 2D char arrays where anything not in a connected region is '#'
     */
    public ArrayList<char[][]> regionsConnected(int x, int y)
    {
        ArrayList<char[][]> regions = new ArrayList<char[][]>(10);
        Iterable<List<short[]>> near = rooms.allAt(x, y);
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

        return regions;
    }
}
