package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import static squidpony.squidmath.CoordPacker.*;

/**
 * A small class that can analyze a dungeon or other map and identify areas as being "room" or "corridor" based on how
 * thick the walkable areas are (corridors are at most 2 cells wide at their widest, rooms are anything else). Most
 * methods of this class return 2D char arrays or Lists thereof, with the subset of the map that is in a specific region
 * kept the same, but everything else replaced with '#'.
 * Created by Tommy Ettinger on 2/3/2016.
 * 
 * @see RectangleRoomFinder A simpler but faster alternative
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

    public int[][] environment;
    /**
     * Not likely to be used directly, but there may be things you can do with these that are cumbersome using only
     * RoomFinder's simpler API.
     */
    public OrderedMap<GreasedRegion, List<GreasedRegion>> rooms,
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

    public GreasedRegion allRooms, allCaves, allCorridors, allFloors;

    /**
     * When a RoomFinder is constructed, it stores all points of rooms that are adjacent to another region here.
     */
    public Coord[] connections,
    /**
     * Potential doorways, where a room is adjacent to a corridor.
     */
    doorways,
    /**
     * Cave mouths, where a cave is adjacent to another type of terrain.
     */
    mouths;
    public int width, height;

    /**
     * Constructs a RoomFinder given a dungeon map, and finds rooms, corridors, and their connections on the map. Does
     * not find caves; if a collection of caves is requested from this, it will be non-null but empty.
     * @param dungeon a 2D char array that uses '#', box drawing characters, or ' ' for walls.
     */
    public RoomFinder(char[][] dungeon)
    {
        if(dungeon.length <= 0)
            return;
        width = dungeon.length;
        height = dungeon[0].length;
        map = new char[width][height];
        environment = new int[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(dungeon[i], 0, map[i], 0, height);
        }
        rooms = new OrderedMap<>(32);
        corridors = new OrderedMap<>(32);
        caves = new OrderedMap<>(8);
        basic = DungeonUtility.simplifyDungeon(map);
        allFloors = new GreasedRegion(basic, '.');
        allRooms = allFloors.copy().retract8way().flood(allFloors, 2);
        allCorridors = allFloors.copy().andNot(allRooms);

        environment = allCorridors.writeInts(
                allRooms.writeInts(environment, DungeonUtility.ROOM_FLOOR),
                DungeonUtility.CORRIDOR_FLOOR);

        allCaves = new GreasedRegion(width, height);
        GreasedRegion d = allCorridors.copy().fringe().and(allRooms);
        connections = doorways = d.asCoords();
        mouths = new Coord[0];
        List<GreasedRegion> rs = allRooms.split(), cs = allCorridors.split();

        for (GreasedRegion sep : cs) {
            GreasedRegion someDoors = sep.copy().fringe().and(allRooms);
            Coord[] doors = someDoors.asCoords();
            List<GreasedRegion> near = new ArrayList<>(4);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, rs));
            }
            corridors.put(sep, near);
        }

        for (GreasedRegion sep : rs) {
            GreasedRegion aroundDoors = sep.copy().fringe().and(allCorridors);
            Coord[] doors = aroundDoors.asCoords();
            List<GreasedRegion> near = new ArrayList<>(10);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, cs));
            }
            rooms.put(sep, near);
        }
    }

    /**
     * Constructs a RoomFinder given a dungeon map and a general kind of environment for the whole map, then finds
     * rooms, corridors, and their connections on the map. Defaults to treating all areas as cave unless
     * {@code environmentKind} is {@code MixedGenerator.ROOM_FLOOR} (or its equivalent, 1).
     * @param dungeon a 2D char array that uses '#', box drawing characters, or ' ' for walls.
     * @param environmentKind if 1 ({@code MixedGenerator.ROOM_FLOOR}), this will find rooms and corridors, else caves
     */
    public RoomFinder(char[][] dungeon, int environmentKind)
    {
        if(dungeon.length <= 0)
            return;
        width = dungeon.length;
        height = dungeon[0].length;
        map = new char[width][height];
        environment = new int[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(dungeon[i], 0, map[i], 0, height);
        }
        rooms = new OrderedMap<>(32);
        corridors = new OrderedMap<>(32);
        caves = new OrderedMap<>(8);

        basic = DungeonUtility.simplifyDungeon(map);

        if(environmentKind == DungeonUtility.ROOM_FLOOR) {

            allFloors = new GreasedRegion(basic, '.');
            allRooms = allFloors.copy().retract8way().flood(allFloors, 2);
            allCorridors = allFloors.copy().andNot(allRooms);
            allCaves = new GreasedRegion(width, height);

            environment = allCorridors.writeInts(
                    allRooms.writeInts(environment, DungeonUtility.ROOM_FLOOR),
                    DungeonUtility.CORRIDOR_FLOOR);


            GreasedRegion d = allCorridors.copy().fringe().and(allRooms);
            connections = doorways = d.asCoords();
            mouths = new Coord[0];
            List<GreasedRegion> rs = allRooms.split(), cs = allCorridors.split();

            for (GreasedRegion sep : cs) {
                GreasedRegion someDoors = sep.copy().fringe().and(allRooms);
                Coord[] doors = someDoors.asCoords();
                List<GreasedRegion> near = new ArrayList<>(4);
                for (int i = 0; i < doors.length; i++) {
                    near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, rs));
                }
                corridors.put(sep, near);
            }

            for (GreasedRegion sep : rs) {
                GreasedRegion aroundDoors = sep.copy().fringe().and(allCorridors);
                Coord[] doors = aroundDoors.asCoords();
                List<GreasedRegion> near = new ArrayList<>(10);
                for (int i = 0; i < doors.length; i++) {
                    near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, cs));
                }
                rooms.put(sep, near);
            }
        }
        else
        {
            allCaves = new GreasedRegion(basic, '.');
            allFloors = new GreasedRegion(width, height);
            allRooms = new GreasedRegion(width, height);
            allCorridors = new GreasedRegion(width, height);
            caves.put(allCaves, new ArrayList<GreasedRegion>());
            connections = mouths = allCaves.copy().andNot(allCaves.copy().retract8way()).retract().asCoords();
            doorways = new Coord[0];
            environment = allCaves.writeInts(environment, DungeonUtility.CAVE_FLOOR);

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
        this.environment = ArrayTools.copy(environment);
        for (int i = 0; i < width; i++) {
            System.arraycopy(dungeon[i], 0, map[i], 0, height);
        }

        rooms = new OrderedMap<>(32);
        corridors = new OrderedMap<>(32);
        caves = new OrderedMap<>(32);
        basic = DungeonUtility.simplifyDungeon(map);
        allFloors = new GreasedRegion(basic, '.');
        allRooms = new GreasedRegion(environment, DungeonUtility.ROOM_FLOOR);
        allCorridors = new GreasedRegion(environment, DungeonUtility.CORRIDOR_FLOOR);
        allCaves = new GreasedRegion(environment, DungeonUtility.CAVE_FLOOR);
        GreasedRegion d = allCorridors.copy().fringe().and(allRooms),
                m = allCaves.copy().fringe().and(allRooms.copy().or(allCorridors));
        doorways = d.asCoords();
        mouths = m.asCoords();
        connections = new Coord[doorways.length + mouths.length];
        System.arraycopy(doorways, 0, connections, 0, doorways.length);
        System.arraycopy(mouths, 0, connections, doorways.length, mouths.length);

        List<GreasedRegion> rs = allRooms.split(), cs = allCorridors.split(), vs = allCaves.split();

        for (GreasedRegion sep : cs) {
            GreasedRegion someDoors = sep.copy().fringe().and(allRooms);
            Coord[] doors = someDoors.asCoords();
            List<GreasedRegion> near = new ArrayList<>(16);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, rs));
            }
            someDoors.remake(sep).fringe().and(allCaves);
            doors = someDoors.asCoords();
            for (int i = 0; i < doors.length; i++) {
                near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, vs));
            }
            corridors.put(sep, near);
        }

        for (GreasedRegion sep : rs) {
            GreasedRegion aroundDoors = sep.copy().fringe().and(allCorridors);
            Coord[] doors = aroundDoors.asCoords();
            List<GreasedRegion> near = new ArrayList<>(32);
            for (int i = 0; i < doors.length; i++) {
                near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, cs));
            }
            aroundDoors.remake(sep).fringe().and(allCaves);
            doors = aroundDoors.asCoords();
            for (int i = 0; i < doors.length; i++) {
                near.addAll(GreasedRegion.whichContain(doors[i].x, doors[i].y, vs));
            }
            rooms.put(sep, near);
        }
        for (GreasedRegion sep : vs) {
            GreasedRegion aroundMouths = sep.copy().fringe().and(allCorridors);
            Coord[] maws = aroundMouths.asCoords();
            List<GreasedRegion> near = new ArrayList<>(48);
            for (int i = 0; i < maws.length; i++) {
                near.addAll(GreasedRegion.whichContain(maws[i].x, maws[i].y, cs));
            }
            aroundMouths.remake(sep).fringe().and(allRooms);
            maws = aroundMouths.asCoords();
            for (int i = 0; i < maws.length; i++) {
                near.addAll(GreasedRegion.whichContain(maws[i].x, maws[i].y, rs));
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
        ArrayList<char[][]> rs = new ArrayList<>(rooms.size());
        for(GreasedRegion r : rooms.keySet())
        {
            rs.add(r.mask(map, '#'));
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
        ArrayList<char[][]> cs = new ArrayList<>(corridors.size());
        for(GreasedRegion c : corridors.keySet())
        {
            cs.add(c.mask(map, '#'));
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
        ArrayList<char[][]> vs = new ArrayList<>(caves.size());
        for(GreasedRegion v : caves.keySet())
        {
            vs.add(v.mask(map, '#'));
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
        ArrayList<char[][]> rs = new ArrayList<char[][]>(rooms.size() + corridors.size() + caves.size());
        for(GreasedRegion r : rooms.keySet())
        {
            rs.add(r.mask(map, '#'));
        }
        for(GreasedRegion c : corridors.keySet())
        {
            rs.add(c.mask(map, '#'));
        }
        for(GreasedRegion v : caves.keySet())
        {
            rs.add(v.mask(map, '#'));
        }
        return rs;
    }
    private static char[][] defaultFill(int width, int height)
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
        char[][] dungeon = new char[Math.min(width, first.length)][Math.min(height, first[0].length)];
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

        OrderedSet<GreasedRegion> regions = GreasedRegion.whichContain(x, y, rooms.keySet());
        regions.addAll(GreasedRegion.whichContain(x, y, corridors.keySet()));
        regions.addAll(GreasedRegion.whichContain(x, y, caves.keySet()));
        GreasedRegion found;
        if(regions.isEmpty())
            found = new GreasedRegion(width, height);
        else
            found = regions.first();
        return found.mask(map, '#');
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
        OrderedSet<GreasedRegion> atRooms = GreasedRegion.whichContain(x, y, rooms.keySet()),
                atCorridors = GreasedRegion.whichContain(x, y, corridors.keySet()),
                atCaves = GreasedRegion.whichContain(x, y, caves.keySet()),
                regions = new OrderedSet<>(64);
        regions.addAll(atRooms);
        regions.addAll(atCorridors);
        regions.addAll(atCaves);
        GreasedRegion found;
        if(regions.isEmpty())
            found = new GreasedRegion(width, height);
        else
        {
            found = regions.first();
            List<List<GreasedRegion>> near = rooms.getMany(atRooms);
            for (List<GreasedRegion> links : near) {
                for(GreasedRegion n : links)
                {
                    found.or(n);
                }
            }
            near = corridors.getMany(atCorridors);
            for (List<GreasedRegion> links : near) {
                for(GreasedRegion n : links)
                {
                    found.or(n);
                }
            }
            near = caves.getMany(atCaves);
            for (List<GreasedRegion> links : near) {
                for(GreasedRegion n : links)
                {
                    found.or(n);
                }
            }
        }
        return found.mask(map, '#');
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
        ArrayList<char[][]> regions = new ArrayList<>(10);
        List<List<GreasedRegion>> near = rooms.getMany(GreasedRegion.whichContain(x, y, rooms.keySet()));
        for (List<GreasedRegion> links : near) {
            for(GreasedRegion n : links)
            {
                regions.add(n.mask(map, '#'));
            }
        }
        near = corridors.getMany(GreasedRegion.whichContain(x, y, corridors.keySet()));
        for (List<GreasedRegion> links : near) {
            for (GreasedRegion n : links) {
                regions.add(n.mask(map, '#'));
            }
        }
        near = caves.getMany(GreasedRegion.whichContain(x, y, caves.keySet()));
        for (List<GreasedRegion> links : near) {
            for(GreasedRegion n : links)
            {
                regions.add(n.mask(map, '#'));
            }
        }

        return regions;
    }
}
