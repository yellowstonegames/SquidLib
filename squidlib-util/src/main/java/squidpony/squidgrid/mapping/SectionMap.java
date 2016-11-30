package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IntVLA;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A Map-like collection that allows storing subdivisions of a 2D array with names (always Strings) and
 * identifying numbers, then looking up {@link Coord}s to find the associated name and/or number, or or looking up
 * a subdivision with a name or number to get a {@link GreasedRegion} back. This also stores connections between
 * sections, which can be useful as part of a graph-like algorithm. It is fed the information it needs by a
 * {@link RoomFinder} instance passed to the constructor or to {@link #reinitialize(RoomFinder)}. A RoomFinder is ready
 * for usage after generating any dungeon with {@link SectionDungeonGenerator} or one of its subclasses; the field
 * {@link SectionDungeonGenerator#finder} should usually be all that needs to be given to this class. If you don't use
 * SectionDungeonGenerator, there's no reason you can't construct a RoomFinder independently and pass that (they can
 * take a little time to construct on large or very complex maps, but shouldn't be heavy after construction).
 * <br>
 * If your code uses the {@link squidpony.squidgrid.zone.Zone} interface, then the {@link GreasedRegion} objects this
 * can return do implement Zone, {@link squidpony.squidgrid.zone.MutableZone}, and {@link Iterable} of Coord.
 * GreasedRegion is significantly faster than the alternatives ({@link squidpony.squidmath.CoordPacker} and manual
 * Lists of Coord) for the spatial manipulations that RoomFinder needs to do to find room-like shapes, and this just
 * gets its GreasedRegion values from RoomFinder directly.
 * <br>
 * Not to be confused with {@link squidpony.squidmath.RegionMap}, which has different functionality and a different
 * pupose; RegionMap simply is a slight extension on OrderedMap to conveniently handle regions as short arrays produced
 * by {@link squidpony.squidmath.CoordPacker}, while this class offers additional features for labeling and looking up
 * sections of a map that were found by a {@link RoomFinder}.
 * <br>
 * Created by Tommy Ettinger on 11/28/2016.
 */
@Beta
public class SectionMap implements Serializable {
    private static final long serialVersionUID = -2322572367863327331L;

    protected int[][] map;
    protected Arrangement<String> names;
    protected ArrayList<GreasedRegion> regions;
    protected ArrayList<IntVLA> connections;

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
        connections = new ArrayList<>(0);
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
            connections = new ArrayList<>(0);
            return;
        }
        regions = new ArrayList<>(rf.rooms.size() + rf.caves.size() + rf.corridors.size());
        names = new Arrangement<>(regions.size());
        connections = new ArrayList<>(regions.size());
        reinitialize(rf);
    }

    /**
     * Copy constructor; takes an already-initialized SectionMap and deep-copies each element into this one. Allows null
     * for {@code other}, which will make an empty SectionMap. This shouldn't be needed very often because SectionMap
     * values are immutable, though their contents can in some cases be mutated independently, and this would allow one
     * SectionMap to be copied and then have its items changed without changing the original.
     * @param other a SectionMap to deep-copy into this one
     */
    public SectionMap(SectionMap other)
    {
        if(other == null) {
            map = new int[0][0];
            names = new Arrangement<>(0);
            regions = new ArrayList<>(0);
            connections = new ArrayList<>(0);
            return;
        }
        map = ArrayTools.copy(other.map);
        names = new Arrangement<>(other.names);
        regions = new ArrayList<>(other.regions.size());
        connections = new ArrayList<>(other.connections.size());
        for (int i = 0; i < other.connections.size(); i++) {
            regions.add(new GreasedRegion(other.regions.get(i)));
            connections.add(new IntVLA(other.connections.get(i)));
        }
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
            connections = new ArrayList<>(0);
            return this;
        }
        map = new int[rf.width][rf.height];
        regions.clear();
        names.clear();
        connections.clear();
        GreasedRegion t, all = new GreasedRegion(map, 0);
        regions.add(all);
        names.add("unused0");
        connections.add(new IntVLA(0));
        for (int i = 0; i < rf.rooms.size(); i++) {
            t = rf.rooms.keyAt(i);
            regions.add(t);
            all.andNot(t);
            t.writeIntsInto(map, names.size());
            names.add("room"+names.size());
            connections.add(new IntVLA(rf.rooms.getAt(i).size()));
        }
        for (int i = 0; i < rf.corridors.size(); i++) {
            t = rf.corridors.keyAt(i);
            regions.add(t);
            all.andNot(t);
            t.writeIntsInto(map, names.size());
            names.add("corridor"+names.size());
            connections.add(new IntVLA(rf.corridors.getAt(i).size()));
        }
        for (int i = 0; i < rf.caves.size(); i++) {
            t = rf.caves.keyAt(i);
            regions.add(t);
            all.andNot(t);
            t.writeIntsInto(map, names.size());
            names.add("cave"+names.size());
            connections.add(new IntVLA(rf.caves.getAt(i).size()));
        }
        int ls = 1;
        List<GreasedRegion> connected;
        IntVLA iv;
        for (int i = 0; i < rf.rooms.size(); i++, ls++) {
            connected = rf.rooms.getAt(i);
            iv = connections.get(ls);
            for (int j = 0; j < connected.size(); j++) {
                iv.add(positionToNumber(connected.get(j).first()));
            }
        }
        for (int i = 0; i < rf.corridors.size(); i++, ls++) {
            connected = rf.corridors.getAt(i);
            iv = connections.get(ls);
            for (int j = 0; j < connected.size(); j++) {
                iv.add(positionToNumber(connected.get(j).first()));
            }
        }
        for (int i = 0; i < rf.caves.size(); i++, ls++) {
            connected = rf.caves.getAt(i);
            iv = connections.get(ls);
            for (int j = 0; j < connected.size(); j++) {
                iv.add(positionToNumber(connected.get(j).first()));
            }
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

    /**
     * Gets the list of connected sections (by their identifying numbers) given an identifying number of a section.
     * @param number an identifying number; should be non-negative and less than {@link #size()}
     * @return an IntVLA storing the identifying numbers of connected sections, or null if given an invalid parameter
     */
    public IntVLA numberToConnections(int number)
    {
        if(number < 0 || number >= connections.size())
            return null;
        return connections.get(number);
    }
    /**
     * Gets the list of connected sections (by their identifying numbers) given a name of a section.
     * @param name a String name; should be present in this SectionMap or this will return null
     * @return an IntVLA storing the identifying numbers of connected sections, or null if given an invalid parameter
     */
    public IntVLA nameToConnections(String name)
    {
        return numberToConnections(nameToNumber(name));
    }

    /**
     * Gets the list of connected sections (by their identifying numbers) given a position inside that section.
     * @param x the x-coordinate of the position to look up; should be within bounds
     * @param y the y-coordinate of the position to look up; should be within bounds
     * @return an IntVLA storing the identifying numbers of connected sections, or null if given invalid parameters
     */
    public IntVLA positionToConnections(int x, int y)
    {
        return numberToConnections(positionToNumber(x, y));
    }

    /**
     * Gets the list of connected sections (by their identifying numbers) given a position inside that section.
     * @param position the Coord position to look up; should be within bounds and non-null
     * @return an IntVLA storing the identifying numbers of connected sections, or null if given an invalid parameter
     */
    public IntVLA positionToConnections(Coord position)
    {
        return numberToConnections(positionToNumber(position));
    }

    /**
     * The number of regions this knows about; includes an entry for "unused cells" so this may be one larger than the
     * amount of GreasedRegions present in a RoomFinder used to construct this.
     * @return the size of this SectionMap
     */
    public int size()
    {
        return names.size();
    }

    /**
     * Checks if this contains the given name.
     * @param name the name to check
     * @return true if this contains the name, false otherwise
     */
    public boolean contains(String name)
    {
        return names.containsKey(name);
    }

    /**
     * Checks if this contains the given identifying number.
     * @param number the number to check
     * @return true if this contains the identifying number, false otherwise
     */
    public boolean contains(int number)
    {
        return number >= 0 && number < names.size();
    }

    /**
     * Checks if this contains the given position (that is, x and y are within map bounds).
     * @param x the x-coordinate of the position to check
     * @param y the y-coordinate of the position to check
     * @return true if the given position is in bounds, false otherwise
     */
    public boolean contains(int x, int y)
    {
        return x >= 0 && x < map.length && y >= 0 && y < map[x].length;
    }
    /**
     * Checks if this contains the given position (that is, it is within map bounds).
     * @param position the position to check
     * @return true if position is non-null and is in bounds, false otherwise
     */
    public boolean contains(Coord position)
    {
        return position != null && contains(position.x, position.y);
    }
}
