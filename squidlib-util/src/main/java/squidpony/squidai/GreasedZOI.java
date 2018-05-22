package squidpony.squidai;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.*;

import java.io.Serializable;

/**
 * Calculates the Zone of Influence, also known as Zone of Control, for different points on a map.
 * Uses GreasedRegion for faster storage and manipulation of zones; it's suggested if you use this class to be
 * somewhat familiar with the methods for manipulating data in that class, though a GreasedRegion can also be used just
 * like a Collection of Coord values. This class is very similar in API and implementation to {@link ZOI}, but should be
 * slightly faster on large maps at the expense of usually using more memory. The main reason to choose between ZOI and
 * GreasedZOI is whether your existing code uses GreasedRegions, like this class, or uses CoordPacker, like ZOI. If you
 * don't currently use either, GreasedZOI is probably preferable because the {@link #calculate()} method produces a
 * value that can be reasonably consumed by Collection-based APIs, while {@link ZOI#calculate()} produces a
 * harder-to-use {@code short[]} that must be read by CoordPacker; GreasedZOI is probably not significantly faster for
 * most applications, and the memory usage difference is probably under a megabyte.
 * <br>
 * Created by Tommy Ettinger on 1/14/2018.
 */
public class GreasedZOI implements Serializable {
    private static final long serialVersionUID = 1L;
    private DijkstraMap dijkstra;
    private Coord[][] influences;
    private GreasedRegion[] packedGroups;
    private boolean completed = false;
    private Radius radius;
    /**
     * Constructs a Zone of Influence map. Takes a (quite possibly jagged) array of arrays of Coord influences, where
     * the elements of the outer array represent different groups of influencing "factions" or groups that exert control
     * over nearby areas, and the Coord elements of the inner array represent individual spots that are part of those
     * groups and share influence with all Coord in the same inner array. Also takes a char[][] for a map, which can be
     * the simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep
     * water as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * Call calculate() when you want information out of this.
     * @param influences an outer array containing influencing groups, each an array containing Coords that influence
     * @param map a char[][] that is used as a map; should be bounded
     * @param measurement a Radius enum that corresponds to how distance should be measured
     */
    public GreasedZOI(Coord[][] influences, char[][] map, Radius measurement) {
        this.influences = influences;
        packedGroups = new GreasedRegion[influences.length];
        radius = measurement;
        dijkstra = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
    }
    /**
     * Constructs a Zone of Influence map. Takes an arrays of Coord influences, where each Coord is treated as both a
     * one-element group of influencing "factions" or groups that exert control over nearby areas, and the individual
     * spot that makes up one of those groups and spreads influence. Also takes a char[][] for a map, which can be the
     * simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep water
     * as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * Essentially, this is the same as constructing a ZOI with a Coord[][] where each inner array has only one element.
     * <br>
     * Call calculate() when you want information out of this.
     * @param influences an array containing Coords that each have their own independent influence
     * @param map a char[][] that is used as a map; should be bounded
     * @param measurement a Radius enum that corresponds to how distance should be measured
     * @see squidpony.squidmath.PoissonDisk for a good way to generate evenly spaced Coords that can be used here
     */
    public GreasedZOI(Coord[] influences, char[][] map, Radius measurement) {
        this.influences = new Coord[influences.length][];
        for (int i = 0; i < influences.length; i++) {
            this.influences[i] = new Coord[] { influences[i] };
        }
        packedGroups = new GreasedRegion[influences.length];
        radius = measurement;
        dijkstra = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
    }

    /**
     * Finds the zones of influence for each of the influences (inner arrays of Coord) this was constructed with, and
     * returns them as packed data (using CoordPacker, which can also be used to unpack the data, merge zones, get
     * shared borders, and all sorts of other tricks). This has each zone of influence overlap with its neighbors; this
     * is useful to find borders using CoordPacker.intersectPacked(), and borders are typically between 1 and 2 cells
     * wide. You can get a different region as packed data if you want region A without the overlapping areas it shares
     * with region B by using {@code short[] different = CoordPacker.differencePacked(A, B)}. Merging two zones A and B
     * can be done with {@code short[] merged = CoordPacker.unionPacked(A, B)} . You can unpack the data
     * into a boolean[][] easily with CoordPacker.unpack(), where true is contained in the zone and false is not.
     * The CoordPacker methods fringe(), expand(), singleRandom(), randomSample(), and randomPortion() are also
     * potentially useful for this sort of data. You should save the short[][] for later use if you want to call
     * nearestInfluences() in this class.
     * <br>
     * The first short[] in the returned short[][] will correspond to the area influenced by the first Coord[] in the
     * nested array passed to the constructor (or the first Coord if a non-nested array was passed); the second will
     * correspond to the second, and so on. The length of the short[][] this returns will equal the number of influence
     * groups.
     * @return an array of short[] storing the zones' areas; each can be used as packed data with CoordPacker
     */
    public GreasedRegion[] calculate()
    {
        for (int i = 0; i < influences.length; i++) {
            for (int j = 0; j < influences[i].length; j++) {
                dijkstra.setGoal(influences[i][j]);
            }
        }
        double[][] scannedAll = dijkstra.scan(null);

        for (int i = 0; i < influences.length; i++) {

            /*
            dijkstra.clearGoals();
            dijkstra.resetMap();
            for (int j = 0; j < influences[i].length; j++) {
                dijkstra.setGoal(influences[i][j]);
            }
            double[][] factionScanned = dijkstra.scan(null);
            for (int y = 0; y < map[0].length; y++) {
                for (int x = 0; x < map.length; x++) {
                    influenced[x][y] = (scannedAll[x][y] < DijkstraMap.FLOOR) &&
                            (factionScanned[x][y] - scannedAll[x][y] <= 1);
                }
            }*/
            packedGroups[i] = increasing(scannedAll, influences[i]);
        }
        completed = true;
        return packedGroups;
    }
    protected GreasedRegion increasing(double[][] dm, Coord[] inf) {
        OrderedSet<Coord> open = new OrderedSet<>(inf), fresh = new OrderedSet<>(64);
        Direction[] dirs = (radius.equals2D(Radius.DIAMOND)) ? Direction.CARDINALS : Direction.OUTWARDS;
        GreasedRegion influenced = new GreasedRegion(dijkstra.width, dijkstra.height);
        final int width = dm.length;
        final int height = width == 0 ? 0 : dm[0].length;

        int numAssigned = open.size();
        double diff;
        while (numAssigned > 0) {
            numAssigned = 0;
            for (Coord cell : open) {
                influenced.insert(cell);
                for (int d = 0; d < dirs.length; d++) {
                    Coord adj = cell.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	/* Outside the map */
                    	continue;
                    if (!open.contains(adj) && dm[adj.x][adj.y] < DijkstraMap.FLOOR && !influenced.contains(adj)) {
                        //h = heuristic(dirs[d]);
                        diff = dm[adj.x][adj.y] - dm[cell.x][cell.y];
                        if (diff <= 1.0 && diff >= 0) {
                            fresh.add(adj);
                            influenced.insert(adj);
                            ++numAssigned;
                        }
                    }
                }
            }

            open = new OrderedSet<>(fresh);
            fresh.clear();
        }

        return influenced;
    }

    /**
     * Given the zones resulting from this class' calculate method and a Coord to check, finds the indices of all
     * influencing groups in zones that have the Coord in their area, and returns all such indices as an int array.
     * @param zones a short[][] returned by calculate; not a multi-packed short[][] from CoordPacker !
     * @param point the Coord to test
     * @return an int[] where each element is the index of an influencing group in zones
     */
    public int[] nearestInfluences(short[][] zones, Coord point)
    {
        ShortVLA found = new ShortVLA(4);
        for (short i = 0; i < zones.length; i++) {
            if(CoordPacker.queryPacked(zones[i], point.x, point.y))
                found.add(i);
        }
        return found.asInts();
    }

    /**
     * Given the zones resulting from this class' calculate method and a Coord to check, finds the indices of all
     * influencing groups in zones that have the Coord in their area, and returns all such indices as an int array.
     * @param zones a short[][] returned by calculate; not a multi-packed short[][] from CoordPacker !
     * @param point the Coord to test
     * @return an int[] where each element is the index of an influencing group in zones
     */
    public int[] nearestInfluences(GreasedRegion[] zones, Coord point)
    {
        IntVLA found = new IntVLA(4);
        for (int i = 0; i < zones.length; i++) {
            if(zones[i].contains(point))
                found.add(i);
        }
        return found.toArray();
    }
    /**
     * This can be given a Coord to check in the results of the latest calculate() call. Finds the indices of all
     * influencing groups in zones that have the Coord in their area, and returns all such indices as an {@link IntVLA}.
     * You can convert the IntVLA to an array with {@link IntVLA#toArray()} if you want to match ZOI's behavior.
     *
     * @param point the Coord to test
     * @return an IntVLA where each element is the index of an influencing group in zones
     */
    public IntVLA nearestInfluences(Coord point)
    {
        if(!completed)
            return new IntVLA(0);
        IntVLA found = new IntVLA(4);
        for (short i = 0; i < packedGroups.length; i++) {
            if(packedGroups[i].contains(point))
                found.add(i);
        }
        return found;
    }

    /**
     * Gets the influencing groups; ideally the result should not be changed without setting it back with setInfluences.
     * @return influences a jagged array of Coord arrays, where the inner arrays are groups of influences
     */
    public Coord[][] getInfluences() {
        return influences;
    }

    /**
     * Changes the influencing groups. This also invalidates the last calculation for the purposes of nearestInfluences,
     * at least for the overload that takes only a Coord.
     * @param influences a jagged array of Coord arrays, where the inner arrays are groups of influences
     */
    public void setInfluences(Coord[][] influences) {
        if(packedGroups.length == influences.length)
        {
            for (int i = 0; i < packedGroups.length; i++) {
                packedGroups[i].clear();
            }
        }
        else
            packedGroups = new GreasedRegion[influences.length];
        this.influences = influences;
        completed = false;
    }
}
