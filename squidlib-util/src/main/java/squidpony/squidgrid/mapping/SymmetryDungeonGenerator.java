package squidpony.squidgrid.mapping;

import squidpony.squidmath.*;

import java.util.*;

/**
 * A variant on {@link MixedGenerator} that creates bi-radially symmetric maps (basically a yin-yang shape). Useful for
 * strategy games and possibly competitive multi-player games. The Coords passed to constructors as room positions do
 * not necessarily need to be
 *
 * Created by Tommy Ettinger on 11/20/2015.
 */
public class SymmetryDungeonGenerator extends MixedGenerator {

    public static OrderedMap<Coord, List<Coord>> removeSomeOverlap(int width, int height, Collection<Coord> sequence)
    {
        List<Coord> s2 = new ArrayList<>(sequence.size());
        for(Coord c : sequence)
        {
            if(c.x * 1.0 / width + c.y * 1.0 / height <= 1.0)
                s2.add(c);
        }
        return listToMap(s2);
    }
    public static OrderedMap<Coord, List<Coord>> removeSomeOverlap(int width, int height, Map<Coord, List<Coord>> connections) {
        OrderedMap<Coord, List<Coord>> om2 = new OrderedMap<>(connections.size());
        Set<Coord> keyset = connections.keySet(), newkeys = new OrderedSet<>(connections.size());
        for (Coord c : keyset) {
            if (c.x * 1.0 / width + c.y * 1.0 / height <= 1.0) {
                newkeys.add(c);
            }
        }
        Coord[] keys = newkeys.toArray(new Coord[newkeys.size()]);
        for (int i = 0; i < keys.length; i++) {
            Coord c = keys[i];
            if (c.x * 1.0 / width + c.y * 1.0 / height <= 1.0) {
                List<Coord> cs = new ArrayList<>(4);
                for (Coord c2 : connections.get(c)) {
                    if (c2.x * 1.0 / width + c2.y * 1.0 / height <= 1.0) {
                        cs.add(c2);
                    } else if (keys[(i + 1) % keys.length].x * 1.0 / width +
                            keys[(i + 1) % keys.length].y * 1.0 / height <= 1.0) {
                        cs.add(keys[(i + 1) % keys.length]);
                    }

                }
                om2.put(c, cs);
            }
        }
        return om2;
    }
    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses Poisson Disk sampling to generate the points it will draw caves and
     * corridors between, ensuring a minimum distance between points, but it does not ensure that paths between points
     * will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width  the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng    an RNG object to use for random choices; this make a lot of random choices.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public SymmetryDungeonGenerator(int width, int height, IRNG rng) {
        this(width, height, rng, basicPoints(width, height, rng));
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a List of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width    the width of the final map in cells
     * @param height   the height of the final map in cells
     * @param rng      an IRNG, such as an RNG, to use for random choices; this make a lot of random choices.
     * @param sequence a List of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, IRNG rng, List<Coord> sequence) {
        this(width, height, rng, listToMap(sequence), 1f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a List of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width    the width of the final map in cells
     * @param height   the height of the final map in cells
     * @param rng      an IRNG, such as an RNG, to use for random choices; this make a lot of random choices.
     * @param sequence a List of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, IRNG rng, OrderedSet<Coord> sequence) {
        this(width, height, rng, setToMap(sequence), 1f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a LinkedHashMap with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width       the width of the final map in cells
     * @param height      the height of the final map in cells
     * @param rng         an RNG object to use for random choices; this make a lot of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, IRNG rng, OrderedMap<Coord, List<Coord>> connections) {
        this(width, height, rng, connections, 0.8f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a LinkedHashMap with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width              the width of the final map in cells
     * @param height             the height of the final map in cells
     * @param rng                an RNG object to use for random choices; this make a lot of random choices.
     * @param connections        a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @param roomSizeMultiplier a float multiplier that will be applied to each room's width and height
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, IRNG rng, OrderedMap<Coord, List<Coord>> connections, float roomSizeMultiplier) {
        super(width, height, rng, crossConnect(width, height, connections), roomSizeMultiplier);
    }

    protected static OrderedMap<Coord, List<Coord>> listToMap(List<Coord> sequence)
    {
        OrderedMap<Coord, List<Coord>> conns = new OrderedMap<>(sequence.size() - 1);
        for (int i = 0; i < sequence.size() - 1; i++) {
            Coord c1 = sequence.get(i), c2 = sequence.get(i+1);
            List<Coord> cs = new ArrayList<>(1);
            cs.add(c2);
            conns.put(c1, cs);
        }
        return conns;
    }
    protected static OrderedMap<Coord, List<Coord>> setToMap(OrderedSet<Coord> sequence)
    {
        OrderedMap<Coord, List<Coord>> conns = new OrderedMap<>(sequence.size() - 1);
        for (int i = 0; i < sequence.size() - 1; i++) {
            Coord c1 = sequence.getAt(i), c2 = sequence.getAt(i+1);
            List<Coord> cs = new ArrayList<>(1);
            cs.add(c2);
            conns.put(c1, cs);
        }
        return conns;
    }

    protected static OrderedMap<Coord, List<Coord>> crossConnect(int width, int height, Map<Coord, List<Coord>> connections)
    {
        OrderedMap<Coord, List<Coord>> conns = new OrderedMap<>(connections.size());
        for(Map.Entry<Coord, List<Coord>> entry : connections.entrySet())
        {
            conns.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        double lowest1 = 9999, lowest2 = 9999, lowest3 = 9999, lowest4 = 9999;
        Coord l1 = null, l2 = null, l3 = null, l4 = null, r1 = null, r2 = null, r3 = null, r4 = null;
        for(List<Coord> left : connections.values())
        {
            for(List<Coord> right : connections.values())
            {
                for(Coord lc : left)
                {
                    for(Coord rc : right)
                    {
                        Coord rc2 = Coord.get(width - 1 - rc.x, height - 1 - rc.y);
                        double dist = lc.distance(rc2);
                        if(dist < 0.001)
                            continue;
                        if(dist < lowest1)
                        {
                            lowest1 = dist;
                            l1 = lc;
                            r1 = rc2;
                        }
                        else if(dist < lowest2 && !lc.equals(l1) && !rc2.equals(r1))
                        {
                            lowest2 = dist;
                            l2 = lc;
                            r2 = rc2;
                        }
                        else if(dist < lowest3
                                && !lc.equals(l1) && !rc2.equals(r1) && !lc.equals(l2) && !rc2.equals(r2))
                        {
                            lowest3 = dist;
                            l3 = lc;
                            r3 = rc2;
                        }
                        else if(dist < lowest4
                                && !lc.equals(l1) && !rc2.equals(r1)
                                && !lc.equals(l2) && !rc2.equals(r2)
                                && !lc.equals(l3) && !rc2.equals(r3))
                        {
                            lowest4 = dist;
                            l4 = lc;
                            r4 = rc2;
                        }
                    }
                }
            }
        }
        if(l1 != null && r1 != null)
        {
            if(conns.containsKey(l1))
            {
                conns.get(l1).add(r1);
            }
            else if(conns.containsKey(r1))
            {
                conns.get(r1).add(l1);
            }
        }
        if(l2 != null && r2 != null)
        {
            if(conns.containsKey(l2))
            {
                conns.get(l2).add(r2);
            }
            else if(conns.containsKey(r2))
            {
                conns.get(r2).add(l2);
            }
        }
        if(l3 != null && r3 != null)
        {
            if(conns.containsKey(l3))
            {
                conns.get(l3).add(r3);
            }
            else if(conns.containsKey(r3))
            {
                conns.get(r3).add(l3);
            }
        }
        if(l4 != null && r4 != null)
        {
            if(conns.containsKey(l4))
            {
                conns.get(l4).add(r4);
            }
            else if(conns.containsKey(r4))
            {
                conns.get(r4).add(l4);
            }
        }
        return conns;
    }
    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    @Override
    protected boolean mark(int x, int y) {
        return super.mark(x, y) || super.mark(width - 1 - x, height - 1 - y);
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    @Override
    protected void markPiercing(int x, int y) {
        super.markPiercing(x, y);
        super.markPiercing(width - 1 - x, height - 1 - y);
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    @Override
    protected void wallOff(int x, int y) {
        super.wallOff(x, y);
        super.wallOff(width - 1 - x, height - 1 - y);
    }
    /**
     * Internal use. Marks a point's environment type as the appropriate kind of environment.
     * @param x x position to mark
     * @param y y position to mark
     * @param kind an int that should be one of the constants in MixedGenerator for environment types.
     */
    @Override
    protected void markEnvironment(int x, int y, int kind) {
        super.markEnvironment(x, y, kind);
        super.markEnvironment(width - 1 - x, height - 1 - y, kind);
    }
}
