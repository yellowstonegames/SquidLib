package squidpony.squidmath;

import squidpony.annotation.Beta;
import squidpony.squidgrid.mapping.RoomFinder;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Map-like collection that allows storing subdivisions of a 2D array with names (always Strings) and
 * identifying numbers, then looking up {@link Coord}s to find the associated name and/or number, or or looking up
 * a subdivision with a name or number to get a {@link GreasedRegion} back.
 * Created by Tommy Ettinger on 11/28/2016.
 */
@Beta
public class SectionMap implements Serializable {
    private static final long serialVersionUID = -2322572367863327331L;

    protected int[][] map;
    protected Arrangement<String> names;
    protected ArrayList<GreasedRegion> regions;

    /**
     * This shouldn't usually be used unless you for some reason need to construct a SectionMap before you have access
     * to a dungeon for it to map. If you do need this, then you must call {@link #reinitialize(RoomFinder)} to get any
     * use out of this object.
     * @see #SectionMap(RoomFinder) The preferred constructor, which takes a RoomFinder.
     */
    public SectionMap()
    {
        map = new int[0][0];
        names = new Arrangement<>(0);
        regions = new ArrayList<>(0);
    }

    /**
     * The preferred constructor; takes a RoomFinder (often one already created in dungeon generation and available via
     * {@link SectionDungeonGenerator#finder}) and uses it to give unique String names and identifying numbers to each
     * room, corridor, and cave area that had been identified by that RoomFinder. In the rare but possible chance that
     * a room, corridor, or cave overlaps with another such area, the one given the highest identifying number takes
     * precedence, but this should probably only happen if RoomFinder was subclassed or its internal state was modified.
     * Any cells that aren't a room, corridor, or cave (usually this contains all walls) are given identifying number 0,
     * with the corresponding name "unused0." All other cells will then have positive, non-zero identifying numbers.
     * Rooms are named next, starting at "room1" and going up to "room2" and so on until all rooms are named; the 1 in
     * the name corresponds to the identifying number. After the last room has been found, e.g. "room5", then corridors
     * are named, starting after the last room's number, so in the example that would be "corridor6", followed by
     * "corridor7". The numbers in the names still correspond to identifying numbers. After corridors, caves follow the
     * same pattern; in this example "cave8" would be followed by "cave9".
     * @param rf a RoomFinder object; usually obtained via {@link SectionDungeonGenerator#finder}
     */
    public SectionMap(RoomFinder rf)
    {
        if(rf == null)
        {
            map = new int[0][0];
            names = new Arrangement<>(0);
            regions = new ArrayList<>(0);
            return;
        }
        regions = new ArrayList<>(rf.rooms.size + rf.caves.size + rf.corridors.size);
        names = new Arrangement<>(regions.size());
        reinitialize(rf);
    }

    /**
     * If this SectionMap hasn't been initialized or the map has completely changed (such as if the player went to a
     * different floor of a dungeon), then you can call this method to avoid discarding some of the state from an
     * earlier SectionMap. This does all the same steps {@link #SectionMap(RoomFinder)} does, so refer to that
     * constructor's documentation for the names and numbers this assigns.
     * @param rf a RoomFinder object; usually obtained via {@link SectionDungeonGenerator#finder}
     * @return this for chaining.
     */
    public SectionMap reinitialize(RoomFinder rf)
    {
        if(rf == null)
        {
            map = new int[0][0];
            names = new Arrangement<>(0);
            regions = new ArrayList<>(0);
            return this;
        }
        map = new int[rf.width][rf.height];
        regions.clear();
        names.clear();
        GreasedRegion t, all = new GreasedRegion(map, 0);
        regions.add(all);
        names.add("unused0");
        for (int i = 0; i < rf.rooms.size; i++) {
            t = rf.rooms.keyAt(i);
            regions.add(t);
            all.andNot(t);
            t.writeIntsInto(map, names.size);
            names.add("room"+names.size);
        }
        for (int i = 0; i < rf.corridors.size; i++) {
            t = rf.corridors.keyAt(i);
            regions.add(t);
            all.andNot(t);
            t.writeIntsInto(map, names.size);
            names.add("corridor"+names.size);
        }
        for (int i = 0; i < rf.caves.size; i++) {
            t = rf.caves.keyAt(i);
            regions.add(t);
            all.andNot(t);
            t.writeIntsInto(map, names.size);
            names.add("cave"+names.size);
        }
        return this;
    }

    /**
     * Gets the identifying number of the area that contains the given x, y position.
     * @param x the x-coordinate to find the identifying number for; should be within bounds of the map
     * @param y the y-coordinate to find the identifying number for; should be within bounds of the map
     * @return the corresponding identifying number, or -1 if the parameters are invalid
     */
    public int positionToNumber(int x, int y)
    {
        if(x < 0 || y < 0 || x >= map.length || y >= map[x].length)
            return -1;
        return map[x][y];
    }

    /**
     * Gets the identifying number of the area that contains the given position.
     * @param position the Coord to find the identifying number for; should be within bounds of the map and non-null
     * @return the corresponding identifying number, or -1 if position is invalid or null
     */
    public int positionToNumber(Coord position)
    {
        if(position == null)
            return -1;
        return positionToNumber(position.x, position.y);
    }

    /**
     * Gets the name of the area that contains the given x, y position.
     * @param x the x-coordinate to find the name for; should be within bounds of the map
     * @param y the y-coordinate to find the name for; should be within bounds of the map
     * @return the corresponding name as a String, or null if the parameters are invalid
     */
    public String positionToName(int x, int y)
    {
        return numberToName(positionToNumber(x, y));
    }

    /**
     * Gets the name of the area that contains the given position.
     * @param position a Coord that should be within bounds of the map and non-null
     * @return the corresponding name as a String, or null if position is invalid or null
     */
    public String positionToName(Coord position)
    {
        if(position == null)
            return null;
        return numberToName(positionToNumber(position));
    }

    /**
     * Gets the identifying number corresponding to the given name.
     * @param name the name to look up, like "room1"
     * @return the corresponding identifying number, or -1 if no such name exists
     */
    public int nameToNumber(String name)
    {
        return names.getInt(name);
    }

    /**
     * Gets the name that corresponds to the given identifying number.
     * @param number the number to look up, like 1
     * @return the corresponding name as a String, or null if no such number is used
     */
    public String numberToName(int number)
    {
        return names.keyAt(number);
    }

    /**
     * Gets the GreasedRegion that has the given identifying number.
     * @param number the number to look up, like 1
     * @return the corresponding GreasedRegion, or null if no such number is used
     */
    public GreasedRegion numberToRegion(int number)
    {
        if(number < 0 || number >= regions.size())
            return null;
        return regions.get(number);
    }

    /**
     * Gets the GreasedRegion that has the given name.
     * @param name the name to look up, like "room1"
     * @return the corresponding GreasedRegion, or null if no such name exists
     */
    public GreasedRegion nameToRegion(String name)
    {
        return numberToRegion(nameToNumber(name));
    }

    /**
     * Gets the GreasedRegion (a group of points as made by the constructor) that contains the given x, y point.
     * @param x the x-coordinate to find the containing region for; should be within bounds of the map
     * @param y the y-coordinate to find the containing region for; should be within bounds of the map
     * @return the GreasedRegion containing the given point, or null if the parameters are invalid
     */
    public GreasedRegion positionToContaining(int x, int y)
    {
        return numberToRegion(positionToNumber(x, y));
    }
    /**
     * Gets the GreasedRegion (a group of points as made by the constructor) that contains the given x, y point.
     * @param position the Coord to find the containing region for; should be within bounds of the map and non-null
     * @return the GreasedRegion containing the given Coord, or null if position is invalid or null
     */
    public GreasedRegion positionToContaining(Coord position)
    {
        if(position == null)
            return null;
        return numberToRegion(positionToNumber(position));
    }
    public boolean contains(String name)
    {
        return names.containsKey(name);
    }
    public boolean contains(int number)
    {
        return number >= 0 && number < names.size;
    }
    public boolean contains(int x, int y)
    {
        return x >= 0 && x < map.length && y >= 0 && y < map[x].length;
    }
    public boolean contains(Coord position)
    {
        return position != null && contains(position.x, position.y);
    }
}
