package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.Edge;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.Room;
import squidpony.squidgrid.mapping.locks.util.Rect2I;
import squidpony.squidmath.*;

import java.util.*;

/**
 * A dungeon generator that can use a mix of techniques to have part-cave, part-room dungeons. Not entirely intended for
 * normal use outside of this library, though it can be very useful when you want to make a dungeon match a specific
 * path and existing generators that use MixedGenerator aren't sufficient. You may want to use a simpler generator based
 * on this, like SerpentMapGenerator, which generates a long, winding path that loops around on itself. This supports
 * the getEnvironment() method, which can be used in conjunction with RoomFinder to find where separate room, corridor,
 * and cave areas have been placed.
 * <br>
 * Based on Michael Patraw's excellent Drunkard's Walk dungeon generator.
 * http://mpatraw.github.io/libdrunkard/
 * @see squidpony.squidgrid.mapping.SerpentMapGenerator a normal use for MixedGenerator that makes winding dungeons
 * @see squidpony.squidgrid.mapping.SerpentDeepMapGenerator uses MixedGenerator as it makes a multi-level dungeon
 * Created by Tommy Ettinger on 10/22/2015.
 */
public class MixedGenerator implements IDungeonGenerator {
    public enum CarverType
    {
        CAVE,
        BOX,
        ROUND,
        BOX_WALLED,
        ROUND_WALLED
    }

    /**
     * Constant for environment tiles that are not near a cave, room, or corridor. Value is 0.
     * Used by several classes that distinguish types of dungeon environment, like {@link SectionDungeonGenerator}.
     * Present here for compatibility; you should prefer {@link DungeonUtility#UNTOUCHED}.
     */
    public static final int UNTOUCHED = 0;
    /**
     * Constant for environment tiles that are floors for a room. Value is 1.
     * Used by several classes that distinguish types of dungeon environment, like {@link SectionDungeonGenerator}.
     * Present here for compatibility; you should prefer {@link DungeonUtility#ROOM_FLOOR}.
     */
    public static final int ROOM_FLOOR = 1;
    /**
     * Constant for environment tiles that are walls near a room. Value is 2.
     * Used by several classes that distinguish types of dungeon environment, like {@link SectionDungeonGenerator}.
     * Present here for compatibility; you should prefer {@link DungeonUtility#ROOM_WALL}.
     */
    public static final int ROOM_WALL = 2;
    /**
     * Constant for environment tiles that are floors for a cave. Value is 3.
     * Used by several classes that distinguish types of dungeon environment, like {@link SectionDungeonGenerator}.
     * Present here for compatibility; you should prefer {@link DungeonUtility#CAVE_FLOOR}.
     */
    public static final int CAVE_FLOOR = 3;
    /**
     * Constant for environment tiles that are walls near a cave. Value is 4.
     * Used by several classes that distinguish types of dungeon environment, like {@link SectionDungeonGenerator}.
     * Present here for compatibility; you should prefer {@link DungeonUtility#CAVE_WALL}.
     */
    public static final int CAVE_WALL = 4;
    /**
     * Constant for environment tiles that are floors for a corridor. Value is 5.
     * Used by several classes that distinguish types of dungeon environment, like {@link SectionDungeonGenerator}.
     * Present here for compatibility; you should prefer {@link DungeonUtility#CORRIDOR_FLOOR}.
     */
    public static final int CORRIDOR_FLOOR = 5;
    /**
     * Constant for environment tiles that are walls near a corridor. Value is 6.
     * Used by several classes that distinguish types of dungeon environment, like {@link SectionDungeonGenerator}.
     * Present here for compatibility; you should prefer {@link DungeonUtility#CORRIDOR_WALL}.
     */
    public static final int CORRIDOR_WALL = 6;

    protected EnumMap<CarverType, Integer> carvers;
    protected int width, height;
    protected float roomWidth, roomHeight;
    public IRNG rng;
    protected char[][] dungeon;
    protected boolean generated = false;
    protected int[][] environment;
    protected boolean[][] marked, walled, fixedRooms;
    protected IntVLA points;
    protected int totalPoints;

    /**
     * Mainly for internal use; this is used by {@link #MixedGenerator(int, int, IRNG)} to get its room positions.
     * This is (and was) the default for generating a List of Coord if no other collection of Coord was supplied to the
     * constructor. For some time this was not the default, and {@link #cleanPoints(int, int, IRNG)} was used instead.
     * Both are still options, but this technique seems to keep corridors shorter and makes connections between rooms
     * more relevant to the current area.
     * <br>
     * <a href="https://gist.githubusercontent.com/tommyettinger/be0ed51858cb492bc7e8cda43a04def1/raw/dae9d8e4f45dd3a3577bdd5f58b419ea5f9ed570/PoissonDungeon.txt">Preview map.</a>
     * @param width dungeon width in cells
     * @param height dungeon height in cells
     * @param rng rng to use
     * @return evenly spaced Coord points in a list made by PoissonDisk, trimmed down so they aren't all used
     * @see PoissonDisk used to make the list
     */
    public static OrderedSet<Coord> basicPoints(int width, int height, IRNG rng)
    {
        return PoissonDisk.sampleRectangle(Coord.get(2, 2), Coord.get(width - 3, height - 3),
                8.5f * (width + height) / 120f, width, height, 35, rng);
    }
    /**
     * Mainly for internal use; this was used by {@link #MixedGenerator(int, int, IRNG)} to get its room positions, and
     * you can choose to use it with {@code new MixedGenerator(width, height, rng, cleanPoints(width, height, rng))}.
     * It produces a fairly rigid layout of rooms that should have less overlap between rooms and corridors; the
     * downside to this is that corridors can become extremely long. The exact technique used here is to get points from
     * a Halton-like sequence, formed using {@link VanDerCorputQRNG} to get a van der Corput sequence, for the x axis
     * and a scrambled van der Corput sequence for the y axis. MixedGenerator will connect these points in pairs. The
     * current method is much better at avoiding "clumps" of closely-positioned rooms in the center of the map.
     * <br>
     * <a href="https://gist.githubusercontent.com/tommyettinger/2745e6fc16fc2acebe2fc959fb4e4c2e/raw/77e1f4dbc844d8892c1a686754535c02cadaa270/TenDungeons.txt">Preview maps, with and without box drawing characters.</a>
     * Note that one of the dungeons in that gist was produced as a fallback by {@link DungeonGenerator} with no
     * arguments (using DEFAULT_DUNGEON as the dungeon style), because the map this method produced in one case had too
     * few floor cells to be used.
     * @param width dungeon width in cells
     * @param height dungeon height in cells
     * @param rng rng to use
     * @return erratically-positioned but generally separated Coord points to pass to a MixedGenerator constructor
     * @see VanDerCorputQRNG used to get separated positions
     */
    public static List<Coord> cleanPoints(int width, int height, IRNG rng)
    {
        width -= 2;
        height -= 2;
        //float mx = rng.nextFloat() * 0.2f, my = rng.nextFloat() * 0.2f;
        int blocks = width * height / 80 >> 3, sz = blocks << 3, index = 0,
                seed = rng.nextInt()|1, seed2 = rng.nextInt()|1;
        //System.out.println("mx: " + mx + ", my: " + my + ", index: " + index);
        List<Coord> list = new ArrayList<>(sz);
        for (int i = 0; i < blocks; i++) {
            Coord area = VanDerCorputQRNG.haltoid(seed2, width * 3 >> 2, height * 3 >> 2, 1, 1, i);
            for (int j = 0; j < 4; j++) {
                list.add(VanDerCorputQRNG.haltoid(seed, width >> 2, height >> 2, area.x, area.y, index++));
            }
        }
        return list;
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a sub-random point sequence to generate the points it will draw caves and
     * corridors between, helping to ensure a minimum distance between points, but it does not ensure that paths between
     * points  will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect
     * what the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to
     * only caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public MixedGenerator(int width, int height, IRNG rng) {
        this(width, height, rng, cleanPoints(width, height, rng));
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a List of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @param sequence a List of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(int width, int height, IRNG rng, List<Coord> sequence) {
        this.width = width;
        this.height = height;
        this.roomWidth = width / 64.0f;
        this.roomHeight = height / 64.0f;
        if(width <= 2 || height <= 2)
            throw new IllegalStateException("width and height must be greater than 2");
        this.rng = rng;
        dungeon = new char[width][height];
        environment = new int[width][height];
        marked = new boolean[width][height];
        walled = new boolean[width][height];
        fixedRooms = new boolean[width][height];
        Arrays.fill(dungeon[0], '#');
        Arrays.fill(environment[0], DungeonUtility.UNTOUCHED);
        for (int i = 1; i < width; i++) {
            System.arraycopy(dungeon[0], 0, dungeon[i], 0, height);
            System.arraycopy(environment[0], 0, environment[i], 0, height);
        }
        totalPoints = sequence.size() - 1;
        points = new IntVLA(totalPoints);
        for (int i = 0; i < totalPoints; i++) {
            Coord c1 = sequence.get(i), c2 = sequence.get(i + 1);
            points.add(((c1.x & 0xff) << 24) | ((c1.y & 0xff) << 16) | ((c2.x & 0xff) << 8) | (c2.y & 0xff));
        }
        carvers = new EnumMap<>(CarverType.class);
    }
    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given IRNG.
     * This version of the constructor uses a Map with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an IRNG object to use for random choices; this make a lot of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(int width, int height, IRNG rng, Map<Coord, List<Coord>> connections)
    {
        this(width, height, rng, connections, 0.8f);
    }
    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given IRNG.
     * This version of the constructor uses a Map with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an IRNG object to use for random choices; this make a lot of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @param roomSizeMultiplier a float multiplier that will be applied to each room's width and height
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(int width, int height, IRNG rng, Map<Coord, List<Coord>> connections,
                          float roomSizeMultiplier) {
        this.width = width;
        this.height = height;
        roomWidth = (width / 64.0f) * roomSizeMultiplier;
        roomHeight = (height / 64.0f) * roomSizeMultiplier;
        if(width <= 2 || height <= 2)
            throw new IllegalStateException("width and height must be greater than 2");
        this.rng = rng;
        dungeon = new char[width][height];
        environment = new int[width][height];
        marked = new boolean[width][height];
        walled = new boolean[width][height];
        fixedRooms = new boolean[width][height];
        Arrays.fill(dungeon[0], '#');
        Arrays.fill(environment[0], DungeonUtility.UNTOUCHED);
        for (int i = 1; i < width; i++) {
            System.arraycopy(dungeon[0], 0, dungeon[i], 0, height);
            System.arraycopy(environment[0], 0, environment[i], 0, height);
        }
        totalPoints = 0;
        for(List<Coord> vals : connections.values())
        {
            totalPoints += vals.size();
        }
        points = new IntVLA(totalPoints);
        for (Map.Entry<Coord, List<Coord>> kv : connections.entrySet()) {
            Coord c1 = kv.getKey();
            for (Coord c2 : kv.getValue()) {
                points.add(((c1.x & 0xff) << 24) | ((c1.y & 0xff) << 16) | ((c2.x & 0xff) << 8) | (c2.y & 0xff));
            }
        }
        carvers = new EnumMap<>(CarverType.class);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given IRNG.
     * This version of the constructor uses an {@link squidpony.squidgrid.mapping.locks.IRoomLayout} to set up rooms,
     * almost always produced by {@link squidpony.squidgrid.mapping.locks.generators.LayoutGenerator}. This method does
     * alter the individual Room objects inside layout, making the center of each room match where that center is placed
     * in the dungeon this generates. You call the different carver-adding methods to affect what the dungeon will look
     * like, i.e. {@link #putCaveCarvers(int)}, {@link #putBoxRoomCarvers(int)} , {@link #putRoundRoomCarvers(int)},
     * {@link #putWalledBoxRoomCarvers(int)}, and {@link #putWalledRoundRoomCarvers(int)}, defaulting to only caves if
     * none are called (using rooms is recommended for this constructor). You call generate() after adding carvers,
     * which returns a char[][] for a map and sets the environment to be fetched with {@link #getEnvironment()}, which
     * is usually needed for {@link SectionDungeonGenerator} to correctly place doors and various other features.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an IRNG object to use for random choices; this make a lot of random choices.
     * @param layout an IRoomLayout that will almost always be produced by LayoutGenerator; the rooms will be altered
     * @param roomSizeMultiplier a float multiplier that will be applied to each room's width and height
     */
    public MixedGenerator(int width, int height, IRNG rng, IRoomLayout layout,
                          float roomSizeMultiplier) {
        this.width = width;
        this.height = height;
        Rect2I bounds = layout.getExtentBounds();
        int offX = bounds.getBottomLeft().x, offY = bounds.getBottomLeft().y;
        float rw = (width) / (bounds.width+1f), rh = (height) / (bounds.height+1f);
        this.roomWidth = roomSizeMultiplier * rw * 0.125f;
        this.roomHeight = roomSizeMultiplier * rh * 0.125f;
        if(width <= 2 || height <= 2)
            throw new IllegalStateException("width and height must be greater than 2");
        this.rng = rng;
        dungeon = new char[width][height];
        environment = new int[width][height];
        marked = new boolean[width][height];
        walled = new boolean[width][height];
        fixedRooms = new boolean[width][height];
        ArrayTools.fill(dungeon, '#');
        ArrayTools.fill(environment, DungeonUtility.UNTOUCHED);
        totalPoints = layout.roomCount();
        points = new IntVLA(totalPoints);
        Coord c2;
        Set<Room> rooms = layout.getRooms(), removing = new HashSet<>(rooms);
        Room t;
        for (Room room : rooms) {
            Coord c1 = room.getCenter();
            if (!bounds.contains(c1)) {
                removing.remove(room);
            }
            else {
                room.setCenter(Coord.get(
                        (int) ((c1.x - offX + 0.75f) * (rw)) & 0xff,
                        (int) ((c1.y - offY + 0.75f) * (rh)) & 0xff));
            }
        }

        for (Room room : rooms) {
            Coord c1 = room.getCenter();
            for (Edge e : room.getEdges()) {
                if (removing.contains(t = layout.get(e.getTargetRoomId()))) {
                    c2 = t.getCenter();
                    points.add((c1.x << 24)
                            | (c1.y << 16)
                            | (c2.x << 8)
                            | c2.y);
                }
            }
            removing.remove(room);
        }
        totalPoints = points.size;
        carvers = new EnumMap<>(CarverType.class);
    }

    /**
     * Changes the number of "carvers" that will create caves from one room to the next. If count is 0 or less, no caves
     * will be made. If count is at least 1, caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted during generation. Since typically
     * about 30-40 rooms are carved, large totals for carver count aren't really needed; aiming for a total of 10
     * between the count of putCaveCarvers(), putBoxRoomCarvers(), putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and
     * putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making caves between rooms; only matters in relation to other carvers
     */
    public void putCaveCarvers(int count)
    {
        carvers.put(CarverType.CAVE, count);
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putBoxRoomCarvers(int count)
    {
        carvers.put(CarverType.BOX, count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putRoundRoomCarvers(int count)
    {
        carvers.put(CarverType.ROUND, count);
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one, enforcing
     * the presence of walls around the rooms even if another room is already there or would be placed there. Corridors
     * can always pass through enforced walls, but caves will open at most one cell in the wall. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledBoxRoomCarvers(int count)
    {
        carvers.put(CarverType.BOX_WALLED, count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one, enforcing the presence of walls around the rooms even if another room is already there or would be placed
     * there. Corridors can always pass through enforced walls, but caves will open at most one cell in the wall. If
     * count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledRoundRoomCarvers(int count)
    {
        carvers.put(CarverType.ROUND_WALLED, count);
    }

    /**
     * Uses the added carvers (or just makes caves if none were added) to carve from point to point in sequence, if it
     * was provided by the constructor, or evenly-spaced randomized points if it was not. This will never carve out
     * cells on the very edge of the map. Uses the numbers of the various kinds of carver that were added relative to
     * each other to determine how frequently to use a given carver type.
     * @return a char[][] where '#' is a wall and '.' is a floor or corridor; x first y second
     */
    public char[][] generate()
    {
        CarverType[] carvings = carvers.keySet().toArray(new CarverType[carvers.size()]);
        int[] carvingsCounters = new int[carvings.length];
        int totalLength = 0;
        for (int i = 0; i < carvings.length; i++) {
            carvingsCounters[i] = carvers.get(carvings[i]);
            totalLength += carvingsCounters[i];
        }
        CarverType[] allCarvings = new CarverType[totalLength];

        for (int i = 0, c = 0; i < carvings.length; i++) {
            for (int j = 0; j < carvingsCounters[i]; j++) {
                allCarvings[c++] = carvings[i];
            }
        }
        if(allCarvings.length == 0)
        {
            allCarvings = new CarverType[]{CarverType.CAVE};
            totalLength = 1;
        }
        else
            allCarvings = rng.shuffle(allCarvings, new CarverType[allCarvings.length]);

        for (int p = 0, c = 0; p < totalPoints; p++, c = (c+1) % totalLength) {
            int pair = points.get(p);
            Coord start = Coord.get(pair >>> 24 & 0xff, pair >>> 16 & 0xff),
                  end   = Coord.get(pair >>> 8 & 0xff, pair & 0xff);
            CarverType ct = allCarvings[c];
            Direction dir;
            switch (ct)
            {
                case CAVE:
                    markPiercing(end);
                    markEnvironmentCave(end.x, end.y);
                    store();
                    double weight = 0.75;
                    do {
                        Coord cent = markPlusCave(start);
                        if(cent != null)
                        {
                            markPiercingCave(cent);
                            markPiercingCave(cent.translate(1, 0));
                            markPiercingCave(cent.translate(-1, 0));
                            markPiercingCave(cent.translate(0, 1));
                            markPiercingCave(cent.translate(0, -1));
                            weight = 0.95;
                        }
                        dir = stepWobbly(start, end, weight);
                        start = start.translate(dir);
                    }while (dir != Direction.NONE);
                    break;
                case BOX:
                    markRectangle(end, rng.between(1, 5), rng.between(1, 5));
                    markRectangle(start, rng.between(1, 4), rng.between(1, 4));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markRectangle(start, 1, 1);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
                case BOX_WALLED:
                    markRectangleWalled(end, rng.between(1, 5), rng.between(1, 5));
                    markRectangleWalled(start, rng.between(1, 4), rng.between(1, 4));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markRectangleWalled(start, 1, 1);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
                case ROUND:
                    markCircle(end, rng.between(2, 6));
                    markCircle(start, rng.between(2, 6));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markCircle(start, 2);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
                case ROUND_WALLED:
                    markCircleWalled(end, rng.between(2, 6));
                    markCircleWalled(start, rng.between(2, 6));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markCircleWalled(start, 2);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
            }
            store();
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(fixedRooms[x][y])
                    markPiercingRoom(x, y);
            }
        }
        store();
        markEnvironmentWalls();
        generated = true;
        return dungeon;
    }

    public char[][] getDungeon() {
        return dungeon;
    }

    public int[][] getEnvironment()
    {
        return environment;
    }

    public boolean hasGenerated()
    {
        return generated;
    }

    public boolean[][] getFixedRooms() {
        return fixedRooms;
    }

    public void setFixedRooms(boolean[][] fixedRooms) {
        this.fixedRooms = fixedRooms;
    }

    /**
     * Internal use. Takes cells that have been previously marked and permanently stores them as floors in the dungeon.
     */
    protected void store()
    {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(marked[i][j])
                {
                    dungeon[i][j] = '.';
                    marked[i][j] = false;
                }
            }
        }
    }

    /**
     * Internal use. Finds all floor cells by environment and marks untouched adjacent (8-way) cells as walls, using the
     * appropriate type for the nearby floor.
     */
    protected void markEnvironmentWalls()
    {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(environment[i][j] == DungeonUtility.UNTOUCHED)
                {
                    boolean allWalls = true;
                    //lowest precedence, also checks for any floors
                    for (int x = Math.max(0, i - 1); x <= Math.min(width - 1, i + 1); x++) {

                        for (int y = Math.max(0, j - 1); y <= Math.min(height - 1, j + 1); y++) {
                            if (environment[x][y] == DungeonUtility.CORRIDOR_FLOOR) {
                                markEnvironment(i, j, DungeonUtility.CORRIDOR_WALL);
                            }
                            if(dungeon[x][y] == '.')
                                allWalls = false;
                        }
                    }
                    //if there are no floors we don't need to check twice again.
                    if(allWalls)
                        continue;
                    //more precedence
                    for (int x = Math.max(0, i - 1); x <= Math.min(width - 1, i + 1); x++) {

                        for (int y = Math.max(0, j - 1); y <= Math.min(height - 1, j + 1); y++) {
                            if (environment[x][y] == DungeonUtility.CAVE_FLOOR) {
                                markEnvironment(i, j, DungeonUtility.CAVE_WALL);
                            }
                        }
                    }
                    //highest precedence
                    for (int x = Math.max(0, i - 1); x <= Math.min(width - 1, i + 1); x++) {

                        for (int y = Math.max(0, j - 1); y <= Math.min(height - 1, j + 1); y++) {
                            if (environment[x][y] == DungeonUtility.ROOM_FLOOR) {
                                markEnvironment(i, j, DungeonUtility.ROOM_WALL);
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param x x position to mark
     * @param y y position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    protected boolean mark(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1 && !walled[x][y]) {
            marked[x][y] = true;
            return false;
        }
        else return x > 0 && x < width - 1 && y > 0 && y < height - 1 && walled[x][y];
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markPiercing(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
            marked[x][y] = true;
        }
    }
    /**
     * Internal use. Marks a point's environment type as the appropriate kind of environment.
     * @param x x position to mark
     * @param y y position to mark
     * @param kind an int that should be one of the constants in MixedGenerator for environment types.
     */
    protected void markEnvironment(int x, int y, int kind) {
        environment[x][y] = kind;
    }

    /**
     * Internal use. Marks a point's environment type as a corridor floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentCorridor(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1 && environment[x][y] != DungeonUtility.ROOM_FLOOR && environment[x][y] != DungeonUtility.CAVE_FLOOR) {
            markEnvironment(x, y, DungeonUtility.CORRIDOR_FLOOR);
        }
    }

    /**
     * Internal use. Marks a point's environment type as a room floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentRoom(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
            markEnvironment(x, y, DungeonUtility.ROOM_FLOOR);
        }
    }

    /**
     * Internal use. Marks a point's environment type as a cave floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentCave(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1 && environment[x][y] != DungeonUtility.ROOM_FLOOR) {
            markEnvironment(x, y, DungeonUtility.CAVE_FLOOR);
        }
    }
    /**
     * Internal use. Marks a point to be made into floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void wallOff(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
            walled[x][y] = true;
        }
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param pos position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    protected boolean mark(Coord pos)
    {
        return mark(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor, piercing walls.
     * @param pos position to mark
     */
    protected void markPiercing(Coord pos)
    {
        markPiercing(pos.x, pos.y);
    }
    /**
     * Internal use. Marks a point to be made into floor, piercing walls, and also marks the point as a cave floor.
     * @param pos position to mark
     */
    protected void markPiercingCave(Coord pos)
    {
        markPiercing(pos.x, pos.y);
        markEnvironmentCave(pos.x, pos.y);
    }
    /**
     * Internal use. Marks a point to be made into floor, piercing walls, and also marks the point as a room floor.
     * @param x x coordinate of position to mark
     * @param y y coordinate of position to mark
     */
    protected void markPiercingRoom(int x, int y)
    {
        markPiercing(x, y);
        markEnvironmentCave(x, y);
    }

    /**
     * Internal use. Marks a point and the four cells orthogonally adjacent to it.
     * @param pos center position to mark
     * @return null if the center of the plus shape wasn't blocked by wall, otherwise the Coord of the center
     */
    private Coord markPlus(Coord pos) {
        Coord block = null;
        if (mark(pos.x, pos.y))
            block = pos;
        mark(pos.x + 1, pos.y);
        mark(pos.x - 1, pos.y);
        mark(pos.x, pos.y + 1);
        mark(pos.x, pos.y - 1);
        return block;
    }

    /**
     * Internal use. Marks a point and the four cells orthogonally adjacent to it, and also marks any cells that weren't
     * blocked as cave floors.
     * @param pos center position to mark
     * @return null if the center of the plus shape wasn't blocked by wall, otherwise the Coord of the center
     */
    private Coord markPlusCave(Coord pos) {
        Coord block = null;
        if (mark(pos.x, pos.y))
            block = pos;
        else
            markEnvironmentCave(pos.x, pos.y);
        if(!mark(pos.x + 1, pos.y))
            markEnvironmentCave(pos.x + 1, pos.y);
        if(!mark(pos.x - 1, pos.y))
            markEnvironmentCave(pos.x - 1, pos.y);
        if(!mark(pos.x, pos.y + 1))
            markEnvironmentCave(pos.x, pos.y + 1);
        if(!mark(pos.x, pos.y - 1))
            markEnvironmentCave(pos.x, pos.y - 1);
        return block;
    }
    /**
     * Internal use. Marks a rectangle of points centered on pos, extending halfWidth in both x directions and
     * halfHeight in both vertical directions. Marks all cells in the rectangle as room floors.
     * @param pos center position to mark
     * @param halfWidth the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null if no points in the rectangle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markRectangle(Coord pos, int halfWidth, int halfHeight)
    {
        halfWidth = Math.max(1, Math.round(halfWidth * roomWidth));
        halfHeight = Math.max(1, Math.round(halfHeight * roomHeight));
        Coord block = null;
        for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
            for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
                if(mark(i, j))
                    block = Coord.get(i, j);
                else
                    markEnvironmentRoom(i, j);
            }
        }
        return block;
    }
    /**
     * Internal use. Marks a rectangle of points centered on pos, extending halfWidth in both x directions and
     * halfHeight in both vertical directions. Also considers the area just beyond each wall, but not corners, to be
     * a blocking wall that can only be passed by corridors and small cave openings. Marks all cells in the rectangle as
     * room floors.
     * @param pos center position to mark
     * @param halfWidth the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null if no points in the rectangle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markRectangleWalled(Coord pos, int halfWidth, int halfHeight)
    {
        halfWidth = Math.max(1, Math.round(halfWidth * roomWidth));
        halfHeight = Math.max(1, Math.round(halfHeight * roomHeight));
        Coord block = null;
        for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
            for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
                markPiercing(i, j);
                markEnvironmentRoom(i, j);
            }
        }
        for (int i = Math.max(0, pos.x - halfWidth - 1); i <= Math.min(width - 1, pos.x + halfWidth + 1); i++) {
            for (int j = Math.max(0, pos.y - halfHeight - 1); j <= Math.min(height - 1, pos.y + halfHeight + 1); j++)
            {
                wallOff(i, j);
            }
        }
        return block;
    }

    /**
     * Internal use. Marks a circle of points centered on pos, extending out to radius in Euclidean measurement. Marks
     * all cells in the circle as room floors.
     * @param pos center position to mark
     * @param radius radius to extend in all directions from center
     * @return null if no points in the circle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markCircle(Coord pos, int radius)
    {
        Coord block = null;
        int high;
        radius = Math.max(1, Math.round(radius * Math.min(roomWidth, roomHeight)));
        for (int dx = -radius; dx <= radius; ++dx)
        {
            high = (int)Math.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy)
            {
                if(mark(pos.x + dx, pos.y + dy))
                    block = pos.translate(dx, dy);
                else
                    markEnvironmentRoom(pos.x + dx, pos.y + dy);
            }
        }
        return block;
    }
    /**
     * Internal use. Marks a circle of points centered on pos, extending out to radius in Euclidean measurement.
     * Also considers the area just beyond each wall, but not corners, to be a blocking wall that can only be passed by
     * corridors and small cave openings. Marks all cells in the circle as room floors.
     * @param pos center position to mark
     * @param radius radius to extend in all directions from center
     * @return null if no points in the circle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markCircleWalled(Coord pos, int radius)
    {
        Coord block = null;
        int high;
        radius = Math.max(1, Math.round(radius * Math.min(roomWidth, roomHeight)));
        for (int dx = -radius; dx <= radius; ++dx)
        {
            high = (int)Math.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy)
            {
                markPiercing(pos.x + dx, pos.y + dy);
                markEnvironmentRoom(pos.x + dx, pos.y + dy);
            }
        }
        for (int dx = -radius; dx <= radius; ++dx)
        {
            high = (int)Math.floor(Math.sqrt(radius * radius - dx * dx));
            int dx2 = Math.max(1, Math.min(pos.x + dx, width - 2));
            for (int dy = -high; dy <= high; ++dy)
            {
                int dy2 = Math.max(1, Math.min(pos.y + dy, height - 2));

                wallOff(dx2, dy2-1);
                wallOff(dx2+1, dy2-1);
                wallOff(dx2-1, dy2-1);
                wallOff(dx2, dy2);
                wallOff(dx2+1, dy2);
                wallOff(dx2-1, dy2);
                wallOff(dx2, dy2+1);
                wallOff(dx2+1, dy2+1);
                wallOff(dx2-1, dy2+1);

            }
        }
        return block;
    }

    /**
     * Internal use. Drunkard's walk algorithm, single step. Based on Michael Patraw's C code, used for cave carving.
     * http://mpatraw.github.io/libdrunkard/
     * @param current the current point
     * @param target the point to wobble towards
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random caves, 0.9 is almost a straight line.
     * @return a Direction, either UP, DOWN, LEFT, or RIGHT if we should move, or NONE if we have reached our target
     */
    private Direction stepWobbly(Coord current, Coord target, double weight)
    {
        int dx = target.x - current.x;
        int dy = target.y - current.y;

        if (dx >  1) dx = 1;
        if (dx < -1) dx = -1;
        if (dy >  1) dy = 1;
        if (dy < -1) dy = -1;

        double r = rng.nextDouble();
        Direction dir;
        if (dx == 0 && dy == 0)
        {
            return Direction.NONE;
        }
        else if (dx == 0 || dy == 0)
        {
            int dx2 = (dx == 0) ? dx : dy, dy2 = (dx == 0) ? dy : dx;
            if (r >= (weight * 0.5))
            {
                r -= weight * 0.5;
                if (r < weight * (1.0 / 6) + (1 - weight) * (1.0 / 3))
                {
                    dx2 = -1;
                    dy2 = 0;
                }
                else if (r < weight * (2.0 / 6) + (1 - weight) * (2.0 / 3))
                {
                    dx2 = 1;
                    dy2 = 0;
                }
                else
                {
                    dx2 = 0;
                    dy2 *= -1;
                }
            }
            dir = Direction.getCardinalDirection(dx2, -dy2);

        }
        else
        {
            if (r < weight * 0.5)
            {
                dy = 0;
            }
            else if (r < weight)
            {
                dx = 0;
            }
            else if (r < weight + (1 - weight) * 0.5)
            {
                dx *= -1;
                dy = 0;
            }
            else
            {
                dx = 0;
                dy *= -1;
            }
            dir = Direction.getCardinalDirection(dx, -dy);
        }
        if(current.x + dir.deltaX <= 0 || current.x + dir.deltaX >= width - 1) {
            if (current.y < target.y) dir = Direction.DOWN;
            else if (current.y > target.y) dir = Direction.UP;
        }
        else if(current.y + dir.deltaY <= 0 || current.y + dir.deltaY >= height - 1) {
            if (current.x < target.x) dir = Direction.RIGHT;
            else if (current.x > target.x) dir = Direction.LEFT;
        }
        return dir;
    }

}
