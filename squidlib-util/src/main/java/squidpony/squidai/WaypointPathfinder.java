package squidpony.squidai;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static squidpony.squidmath.CoordPacker.*;

/**
 * Pathfind to known connections between rooms or other "chokepoints" without needing full-map Dijkstra scans.
 * Pre-calculates a path either from or to any given chokepoint to each other chokepoint.
 * Created by Tommy Ettinger on 10/25/2015.
 */
public class WaypointPathfinder {
    private int width;
    private int height;
    private DijkstraMap dm;
    private int[][] expansionMap;
    public StatefulRNG rng;
    private OrderedMap<Coord, OrderedMap<Coord, Edge>> waypoints;

    /**
     * Calculates and stores the doors and doors-like connections ("chokepoints") on the given map as waypoints.
     * Will use the given Radius enum to determine how to handle DijkstraMap measurement in future pathfinding.
     * Uses a new RNG for all random choices, which will be seeded with {@code rng.nextLong()}, or unseeded if
     * the parameter is null.
     * @param map a char[][] that stores a "complete" dungeon map, with any chars as features that pathfinding needs.
     * @param measurement a Radius that should correspond to how you want path distance calculated.
     * @param rng an RNG object or null (this will always use a new RNG, but it may be seeded by a given RNG's next result)
     */
    public WaypointPathfinder(char[][] map, Radius measurement, RNG rng)
    {
        if(rng == null)
            this.rng = new StatefulRNG();
        else
            this.rng = new StatefulRNG(rng.nextLong());
        width = map.length;
        height = map[0].length;
        char[][] simplified = DungeonUtility.simplifyDungeon(map);
        OrderedSet<Coord> centers = PoissonDisk.sampleMap(simplified,
                Math.min(width, height) * 0.4f, this.rng, '#');
        int centerCount = centers.size();
        expansionMap = new int[width][height];
        waypoints = new OrderedMap<>(64);
        dm = new DijkstraMap(simplified, DijkstraMap.Measurement.MANHATTAN);

        for (Coord center : centers) {
            dm.clearGoals();
            dm.resetMap();
            dm.setGoal(center);
            dm.scan(null, null);
            double current;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    current = dm.gradientMap[i][j];
                    if (current >= DijkstraMap.FLOOR)
                        continue;
                    if (center.x == i && center.y == j)
                        expansionMap[i][j]++;
                    for (Direction dir : Direction.CARDINALS) {
                        if (dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current + 1 ||
                                dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current - 1)
                            expansionMap[i][j]++;
                    }
                }
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                expansionMap[i][j] /= centerCount;
            }
        }

        OrderedSet<Coord> chokes = new OrderedSet<>(128);
        for (int i = 0; i < width; i++) {
            ELEMENT_WISE:
            for (int j = 0; j < height; j++) {
                if(expansionMap[i][j] <= 0)
                    continue;
                int current = expansionMap[i][j];
                boolean good = false;
                for(Direction dir : Direction.CARDINALS) {
                    if (chokes.contains(Coord.get(i + dir.deltaX, j + dir.deltaY)))
                        continue ELEMENT_WISE;
                    if (expansionMap[i + dir.deltaX][j + dir.deltaY] > 0 && expansionMap[i + dir.deltaX][j + dir.deltaY] > current + 1 ||
                            (expansionMap[i + dir.deltaX][j + dir.deltaY] > current && expansionMap[i][j] <= 2)) {
                        if (expansionMap[i - dir.deltaX][j - dir.deltaY] > 0 && expansionMap[i - dir.deltaX][j - dir.deltaY] >= current) {
                            good = true;
                        }
                    }
                }

                if(good) {
                    Coord chk = Coord.get(i, j);
                    chokes.add(chk);
                    waypoints.put(chk, new OrderedMap<Coord, Edge>());
                }
            }
        }
        
        dm = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));

        for(Map.Entry<Coord, OrderedMap<Coord, Edge>> n : waypoints.entrySet())
        {
            chokes.remove(n.getKey());
            if(chokes.isEmpty())
                break;
            dm.clearGoals();
            dm.resetMap();
            dm.setGoal(n.getKey());
            dm.scan(null, null);
            for(Coord c : chokes)
            {
                n.getValue().put(c, new Edge(n.getKey(), c, dm.findPathPreScanned(c), dm.gradientMap[c.x][c.y]));
            }
        }

    }
    /**
     * Calculates and stores the doors and doors-like connections ("chokepoints") on the given map as waypoints.
     * Will use the given Radius enum to determine how to handle DijkstraMap measurement in future pathfinding.
     * Uses a new RNG for all random choices, which will be seeded with {@code rng.nextLong()}, or unseeded if
     * the parameter is null.
     * @param map a char[][] that stores a "complete" dungeon map, with any chars as features that pathfinding needs.
     * @param measurement a Radius that should correspond to how you want path distance calculated.
     * @param rng an RNG object or null (this will always use a new RNG, but it may be seeded by a given RNG's next result)
     * @param thickCorridors true if most chokepoints on the map are 2 cells wide instead of 1
     */
    public WaypointPathfinder(char[][] map, Radius measurement, RNG rng, boolean thickCorridors)
    {
        if(rng == null)
            this.rng = new StatefulRNG();
        else
            this.rng = new StatefulRNG(rng.nextLong());
        width = map.length;
        height = map[0].length;
        char[][] simplified = DungeonUtility.simplifyDungeon(map);
        expansionMap = new int[width][height];
        waypoints = new OrderedMap<>(64);
        OrderedSet<Coord> chokes = new OrderedSet<>(128);

        if(thickCorridors)
        {
            short[] floors = pack(simplified, '.'),
                    rooms = flood(floors, retract(floors, 1, 60, 60, true), 2, false),
                    corridors = differencePacked(floors, rooms),
                    doors = intersectPacked(rooms, fringe(corridors, 1, 60, 60, false));
            Coord[] apart = apartPacked(doors, 1);
            Collections.addAll(chokes, apart);
            for (int i = 0; i < apart.length; i++) {
                waypoints.put(apart[i], new OrderedMap<Coord, Edge>());
            }
        }
        else {
            OrderedSet<Coord> centers = PoissonDisk.sampleMap(simplified,
                    Math.min(width, height) * 0.4f, this.rng, '#');
            int centerCount = centers.size();
            dm = new DijkstraMap(simplified, DijkstraMap.Measurement.MANHATTAN);

            for (Coord center : centers) {
                dm.clearGoals();
                dm.resetMap();
                dm.setGoal(center);
                dm.scan(null, null);
                double current;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        current = dm.gradientMap[i][j];
                        if (current >= DijkstraMap.FLOOR)
                            continue;
                        if (center.x == i && center.y == j)
                            expansionMap[i][j]++;
                        for (Direction dir : Direction.CARDINALS) {
                            if (dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current + 1 ||
                                    dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current - 1)
                                expansionMap[i][j]++;
                        }
                    }
                }
            }

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    expansionMap[i][j] /= centerCount;
                }
            }

            for (int i = 0; i < width; i++) {
                ELEMENT_WISE:
                for (int j = 0; j < height; j++) {
                    if (expansionMap[i][j] <= 0)
                        continue;
                    int current = expansionMap[i][j];
                    boolean good = false;
                    for (Direction dir : Direction.CARDINALS) {
                        if (chokes.contains(Coord.get(i + dir.deltaX, j + dir.deltaY)))
                            continue ELEMENT_WISE;
                        if (expansionMap[i + dir.deltaX][j + dir.deltaY] > 0 && expansionMap[i + dir.deltaX][j + dir.deltaY] > current + 1 ||
                                (expansionMap[i + dir.deltaX][j + dir.deltaY] > current && expansionMap[i][j] <= 2)) {
                            if (expansionMap[i - dir.deltaX][j - dir.deltaY] > 0 && expansionMap[i - dir.deltaX][j - dir.deltaY] >= current) {
                                good = true;
                            }
                        }
                    }

                    if (good) {
                        Coord chk = Coord.get(i, j);
                        chokes.add(chk);
                        waypoints.put(chk, new OrderedMap<Coord, Edge>());
                    }
                }
            }
        }

        dm = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));

        int e = 0;
        for(Map.Entry<Coord, OrderedMap<Coord, Edge>> n : waypoints.entrySet())
        {
            chokes.remove(n.getKey());
            if(chokes.isEmpty())
                break;
            dm.clearGoals();
            dm.resetMap();
            dm.setGoal(n.getKey());
            dm.scan(null, null);
            for(Coord c : chokes)
            {
                n.getValue().put(c, new Edge(n.getKey(), c, dm.findPathPreScanned(c), dm.gradientMap[c.x][c.y]));
            }
        }

    }

    /**
     * Calculates and stores the specified fraction of walkable points from map as waypoints. Does not perform any
     * analysis of chokepoints and acts as a more brute-force solution when maps may be unpredictable. The lack of an
     * analysis step may mean this could have drastically less of a penalty to startup time than the other constructors,
     * and with the right fraction parameter (29 seems ideal), may perform better as well. Will use the given Radius
     * enum to determine how to handle DijkstraMap measurement in future pathfinding.
     * Uses a new RNG for all random choices, which will be seeded with {@code rng.nextLong()}, or unseeded if
     * the parameter is null.
     * <br>
     * Remember, a fraction value of 29 works well!
     * @param map a char[][] that stores a "complete" dungeon map, with any chars as features that pathfinding needs.
     * @param measurement a Radius that should correspond to how you want path distance calculated.
     * @param rng an RNG object or null (this will always use a new RNG, but it may be seeded by a given RNG's next result)
     * @param fraction the fractional denominator of passable cells to assign as waypoints; use 29 if you aren't sure
     */
    public WaypointPathfinder(char[][] map, Radius measurement, RNG rng, int fraction)
    {
        if(rng == null)
            this.rng = new StatefulRNG();
        else
            this.rng = new StatefulRNG(rng.nextLong());
        width = map.length;
        height = map[0].length;
        char[][] simplified = DungeonUtility.simplifyDungeon(map);
        expansionMap = new int[width][height];
        waypoints = new OrderedMap<>(64);
        OrderedSet<Coord> chokes = new OrderedSet<>(128);

        short[] floors = pack(simplified, '.');
        Coord[] apart = fractionPacked(floors, fraction);
        Collections.addAll(chokes, apart);
        for (int i = 0; i < apart.length; i++) {
            waypoints.put(apart[i], new OrderedMap<Coord, Edge>());
        }

        dm = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));

        int e = 0;
        for(Map.Entry<Coord, OrderedMap<Coord, Edge>> n : waypoints.entrySet())
        {
            chokes.remove(n.getKey());
            if(chokes.isEmpty())
                break;
            dm.clearGoals();
            dm.resetMap();
            dm.setGoal(n.getKey());
            dm.scan(null, null);
            for(Coord c : chokes)
            {
                n.getValue().put(c, new Edge(n.getKey(), c, dm.findPathPreScanned(c), dm.gradientMap[c.x][c.y]));
            }
        }

    }

    /**
     * Calculates and stores the doors and doors-like connections ("chokepoints") on the given map as waypoints.
     * Will use the given DijkstraMap for pathfinding after construction (and during some initial calculations).
     * The dijkstra parameter will be mutated by this class, so it should not be reused elsewhere.
     * Uses a new RNG for all random choices, which will be seeded with {@code rng.nextLong()}, or unseeded if
     * the parameter is null.
     * @param map a char[][] that stores a "complete" dungeon map, with any chars as features that pathfinding needs
     * @param dijkstra a DijkstraMap that will be used to find paths; may have costs but they will not be used
     * @param rng an RNG object or null (this will always use a new RNG, but it may be seeded by a given RNG's next result)
     */
    public WaypointPathfinder(char[][] map, DijkstraMap dijkstra, RNG rng)
    {
        if(rng == null)
            this.rng = new StatefulRNG();
        else
            this.rng = new StatefulRNG(rng.nextLong());
        width = map.length;
        height = map[0].length;
        char[][] simplified = DungeonUtility.simplifyDungeon(map);
        OrderedSet<Coord> centers = PoissonDisk.sampleMap(simplified,
                Math.min(width, height) * 0.4f, this.rng, '#');
        int centerCount = centers.size();
        expansionMap = new int[width][height];
        waypoints = new OrderedMap<>(64);
        dm = new DijkstraMap(simplified, DijkstraMap.Measurement.MANHATTAN);

        for (Coord center : centers) {
            dm.clearGoals();
            dm.resetMap();
            dm.setGoal(center);
            dm.scan(null, null);
            double current;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    current = dm.gradientMap[i][j];
                    if (current >= DijkstraMap.FLOOR)
                        continue;
                    if (center.x == i && center.y == j)
                        expansionMap[i][j]++;
                    for (Direction dir : Direction.CARDINALS) {
                        if (dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current + 1 ||
                                dm.gradientMap[i + dir.deltaX][j + dir.deltaY] == current - 1)
                            expansionMap[i][j]++;
                    }
                }
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                expansionMap[i][j] /= centerCount;
            }
        }

        OrderedSet<Coord> chokes = new OrderedSet<>(128);
        for (int i = 0; i < width; i++) {
            ELEMENT_WISE:
            for (int j = 0; j < height; j++) {
                if(expansionMap[i][j] <= 0)
                    continue;
                int current = expansionMap[i][j];
                boolean good = false;
                for(Direction dir : Direction.CARDINALS) {
                    if (chokes.contains(Coord.get(i + dir.deltaX, j + dir.deltaY)))
                        continue ELEMENT_WISE;
                    if (expansionMap[i + dir.deltaX][j + dir.deltaY] > 0 && expansionMap[i + dir.deltaX][j + dir.deltaY] > current + 1 ||
                            (expansionMap[i + dir.deltaX][j + dir.deltaY] > current && expansionMap[i][j] <= 2)) {
                        if (expansionMap[i - dir.deltaX][j - dir.deltaY] > 0 && expansionMap[i - dir.deltaX][j - dir.deltaY] >= current) {
                            good = true;
                        }
                    }
                }
                if(good) {
                    Coord chk = Coord.get(i, j);
                    chokes.add(chk);
                    waypoints.put(chk, new OrderedMap<Coord, Edge>());
                }
            }
        }
        dm = dijkstra;
        int e = 0;
        for(Map.Entry<Coord, OrderedMap<Coord, Edge>> n : waypoints.entrySet())
        {
            chokes.remove(n.getKey());
            if(chokes.isEmpty())
                break;
            dm.clearGoals();
            dm.resetMap();
            dm.setGoal(n.getKey());
            dm.scan(null, null);
            for(Coord c : chokes)
            {
                n.getValue().put(c, new Edge(n.getKey(), c, dm.findPathPreScanned(c), dm.gradientMap[c.x][c.y]));
            }
        }

    }

    /**
     * Finds the appropriate one of the already-calculated, possibly-long paths this class stores to get from a waypoint
     * to another waypoint, then quickly finds a path to get on the long path, and returns the total path. This does
     * not need to perform any full-map scans with DijkstraMap.
     * @param self the pathfinder's position
     * @param approximateTarget the Coord that represents the approximate area to pathfind to; will be randomized if
     *                          it is not walkable.
     * @return an ArrayList of Coord that will go from a cell adjacent to self to a waypoint near approximateTarget
     */
    public ArrayList<Coord> getKnownPath(Coord self, Coord approximateTarget) {
        ArrayList<Coord> near = dm.findNearestMultiple(approximateTarget, 5, waypoints.keySet());
        Coord me = dm.findNearest(self, waypoints.keySet());
        double bestCost = 999999.0;
        ArrayList<Coord> path = new ArrayList<>();
        /*if (waypoints.containsKey(me)) {
            Edge[] ed = waypoints.get(me).values().toArray(new Edge[waypoints.get(me).size()]);
            Arrays.sort(ed);
            path = ed[0].path;
        */
        boolean reversed = false;
        for (Coord test : near) {
            if (waypoints.containsKey(test)) {
                Edge ed;
                if(waypoints.get(test).containsKey(me)) {
                    ed = waypoints.get(test).get(me);
                    reversed = true;
                }
                else if(waypoints.containsKey(me) && waypoints.get(me).containsKey(test))
                    ed = waypoints.get(me).get(test);
                else
                    continue;
                if (ed.cost < bestCost) {
                    bestCost = ed.cost;
                    path = new ArrayList<>(ed.path);
                }
            }
        }
        if(path.isEmpty())
            return path;
        if(reversed)
            Collections.reverse(path);
        ArrayList<Coord> getToPath = dm.findShortcutPath(self, path.toArray(new Coord[0]));
        if (getToPath.size() > 0)
        {
            getToPath.remove(getToPath.size() - 1);
            getToPath.addAll(path);
            path = getToPath;
        }
        return path;
    }

    /**
     * If a creature is interrupted or obstructed on a "highway" path, it may need to travel off the path to its goal.
     * This method gets a straight-line path back to the path to goal. It does not contain the "highway" path, only the
     * "on-ramp" to enter the ideal path.
     * @param currentPosition the current position of the pathfinder, which is probably not on the ideal path
     * @param path the ideal path, probably returned by getKnownPath
     * @return an ArrayList of Coord that go from a cell adjacent to currentPosition to a Coord on or adjacent to path.
     */
    public ArrayList<Coord> goBackToPath(Coord currentPosition, ArrayList<Coord> path)
    {
        return dm.findShortcutPath(currentPosition, path.toArray(new Coord[0]));
    }

    public OrderedSet<Coord> getWaypoints()
    {
        return waypoints.keysAsOrderedSet();
    }

    private static class Edge implements Comparable<Edge>
    {
        public Coord from;
        public Coord to;
        public ArrayList<Coord> path;
        public double cost;
        public Edge(Coord from, Coord to, ArrayList<Coord> path, double cost)
        {
            this.from = from;
            this.to = to;
            this.path = path;
            this.cost = cost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Edge edge = (Edge) o;

            if (Double.compare(edge.cost, cost) != 0) return false;
            if (!from.equals(edge.from)) return false;
            return to.equals(edge.to);

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = from.hashCode();
            result = 31 * result + to.hashCode();
            temp = NumberTools.doubleToLongBits(cost);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * Note: this class has a natural ordering that is
         * inconsistent with equals.
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(Edge o) {
            return (cost - o.cost > 0) ? 1 : (cost - o.cost < 0) ? -1 : 0;
        }
    }

}
