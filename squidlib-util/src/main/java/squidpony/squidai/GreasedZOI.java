package squidpony.squidai;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.Collection;

/**
 * Calculates the Zone of Influence, also known as Zone of Control, for different points on a map.
 * Uses GreasedRegion for faster storage and manipulation of zones; it's suggested if you use this class to be
 * somewhat familiar with the methods for manipulating data in that class, though a GreasedRegion can also be used just
 * like a Collection of Coord values. This class is very similar in API and implementation to {@link ZOI}, but should be
 * faster on large maps and produces much less garbage. The main reason to choose between ZOI and
 * GreasedZOI is whether your existing code uses GreasedRegions, like this class, or uses CoordPacker, like ZOI. If you
 * don't currently use either, GreasedZOI is probably preferable because the {@link #calculate()} method produces a
 * value that can be reasonably consumed by Collection-based APIs, while {@link ZOI#calculate()} produces a
 * harder-to-use {@code short[]} that must be read by CoordPacker. Repeated operations on CoordPacker's {@code short[]}
 * data will have to keep allocating more arrays and temporary internal data structures, while a GreasedRegion can be
 * reused many times and only rarely allocates more space.
 * <br>
 * Created by Tommy Ettinger on 1/14/2018.
 */
public class GreasedZOI implements Serializable {
    private static final long serialVersionUID = 2L;
    private DijkstraMap dijkstra;
    private Coord[][] influences;
    private GreasedRegion[] groups;
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
     * Call {@link #calculate()} when you want information out of this.
     * @param influences an outer array containing influencing groups, each an array containing Coords that influence
     * @param map a char[][] that is used as an area map; should be bounded
     * @param measurement a Radius enum that corresponds to how distance should be measured
     */
    public GreasedZOI(Coord[][] influences, char[][] map, Radius measurement) {
        this.influences = influences;
        groups = new GreasedRegion[influences.length];
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
     * Call {@link #calculate()} when you want information out of this.
     * @param influences an array containing Coords that each have their own independent influence
     * @param map a char[][] that is used as an area map; should be bounded
     * @param measurement a Radius enum that corresponds to how distance should be measured
     * @see squidpony.squidmath.PoissonDisk PoissonDisk provides a good way to generate evenly spaced Coords
     */
    public GreasedZOI(Coord[] influences, char[][] map, Radius measurement) {
        this.influences = new Coord[influences.length][];
        for (int i = 0; i < influences.length; i++) {
            this.influences[i] = new Coord[] { influences[i] };
        }
        groups = new GreasedRegion[influences.length];
        radius = measurement;
        dijkstra = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
    }
    /**
     * Constructs a Zone of Influence map. Takes a Collection of Coord influences, where each Coord is treated as both a
     * one-element group of influencing "factions" or groups that exert control over nearby areas, and the individual
     * spot that makes up one of those groups and spreads influence. Also takes a char[][] for a map, which can be the
     * simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep water
     * as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * Essentially, this is the same as constructing a ZOI with a Coord[][] where each inner array has only one element,
     * drawn from a Collection. It often makes sense to use a GreasedRegion as the Collection of Coord.
     * <br>
     * Call {@link #calculate()} when you want information out of this.
     * @param influences A Collection of Coord, such as a GreasedRegion, where each Coord has independent influence
     * @param map a char[][] that is used as an area map; should be bounded
     * @param measurement a Radius enum that corresponds to how distance should be measured
     */
    public GreasedZOI(Collection<Coord> influences, char[][] map, Radius measurement) {
        this.influences = new Coord[influences.size()][];
        int i = 0;
        for(Coord c : influences) {
            this.influences[i++] = new Coord[]{c};
        }
        groups = new GreasedRegion[this.influences.length];
        radius = measurement;
        dijkstra = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
    }

    /**
     * Finds the zones of influence for each of the influences (inner arrays of Coord) this was constructed with, and
     * returns all zones as a GreasedRegion array. This has each zone of influence overlap with its neighbors; this
     * is useful to find borders using {@link GreasedRegion#and(GreasedRegion)}, and borders are typically between 1 and
     * 2 cells wide. You can get a different region if you want region A without the overlapping areas it shares with
     * region B by using {@link GreasedRegion#andNot(GreasedRegion)}. Merging two zones A and B can be done with
     * {@link GreasedRegion#or(GreasedRegion)}. You can transform the data into a boolean[][] easily with
     * {@link GreasedRegion#decode()}, where true is contained in the zone and false is not. The methods
     * {@link GreasedRegion#fringe()}, {@link GreasedRegion#expand()}, {@link GreasedRegion#singleRandom(IRNG)}, and
     * {@link GreasedRegion#separatedBlue(double)} are also potentially useful for this sort of data. You should save
     * the {@code GreasedRegion[]} for later use if you want to call
     * {@link #nearestInfluences(GreasedRegion[], Coord)}.
     * <br>
     * The first GreasedRegion in the returned GreasedRegion[] will correspond to the area influenced by the first
     * Coord[] in the nested array passed to the constructor (or the first Coord if a non-nested array was passed); the
     * second will correspond to the second, and so on. The length of the GreasedRegion[] this returns will equal the
     * number of influence groups.
     * @return a GreasedRegion array, with each item storing a zone's area
     */
    public GreasedRegion[] calculate()
    {
        for (int i = 0; i < influences.length; i++) {
            for (int j = 0; j < influences[i].length; j++) {
                dijkstra.setGoal(influences[i][j]);
            }
        }
        dijkstra.scan(null, null);
        final double[][] scannedAll = dijkstra.gradientMap;

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
            groups[i] = increasing(scannedAll, influences[i]);
        }
        completed = true;
        return groups;
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
     * Given the zones resulting from this class' {@link #calculate()} method and a Coord to check, finds the indices of
     * all influencing groups in zones that have the Coord in their area, and returns all such indices as an IntVLA.
     * @param zones a GreasedRegion[] returned by calculate
     * @param point the Coord to test
     * @return an IntVLA where each element is the index of an influencing group in zones
     */
    public IntVLA nearestInfluences(GreasedRegion[] zones, Coord point)
    {
        IntVLA found = new IntVLA(4);
        for (int i = 0; i < zones.length; i++) {
            if(zones[i].contains(point))
                found.add(i);
        }
        return found;
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
        for (short i = 0; i < groups.length; i++) {
            if(groups[i].contains(point))
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
        if(groups.length == influences.length)
        {
            for (int i = 0; i < groups.length; i++) {
                groups[i].clear();
            }
        }
        else
            groups = new GreasedRegion[influences.length];
        this.influences = influences;
        completed = false;
    }
}
