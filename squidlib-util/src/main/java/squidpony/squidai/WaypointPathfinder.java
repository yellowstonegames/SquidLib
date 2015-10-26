package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;

import java.util.*;

/**
 * Meant to make DijkstraMap scans less expensive on large maps.
 * Created by Tommy Ettinger on 10/25/2015.
 */
public class WaypointPathfinder {
    private int width;
    private int height;
    private DijkstraMap dm;
    private char[][] map;
    private int[][] expansionMap;
    private RNG rng;
    private LinkedHashMap<Coord, LinkedHashMap<Coord, Edge>> waypoints;

    public WaypointPathfinder(char[][] map, Radius measurement, RNG rng)
    {
        this.rng = rng;
        this.map = map;
        width = map.length;
        height = map[0].length;
        char[][] simplified = DungeonUtility.simplifyDungeon(map);
        ArrayList<Coord> centers = PoissonDisk.sampleMap(simplified,
                Math.min(width, height) * 0.25f, this.rng, '#');
        int centerCount = centers.size();
        expansionMap = new int[width][height];
        waypoints = new LinkedHashMap<Coord, LinkedHashMap<Coord, Edge>>(64);
        dm = new DijkstraMap(simplified, DijkstraMap.Measurement.CHEBYSHEV);

        for (Coord center : centers) {
            dm.clearGoals();
            dm.resetMap();
            Coord c = center;
            dm.setGoal(c);
            dm.scan(null);
            double current;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    current = dm.gradientMap[i][j];
                    if (current >= DijkstraMap.FLOOR)
                        continue;
                    for (int k = -1; k <= 1; k++) {
                        for (int l = -1; l <= 1; l++) {
                            if (dm.gradientMap[i + k][j + l] == current)
                                expansionMap[i][j]++;
                        }
                    }
                }
            }
        }
        LinkedHashSet<Coord> chokes = new LinkedHashSet<Coord>(128);
        for (int i = 0; i < width; i++) {
            ELEMENT_WISE:
            for (int j = 0; j < height; j++) {
                if(expansionMap[i][j] <= 0)
                    continue;
                int current = expansionMap[i][j];
                boolean good = false;
                for (int k = -1; k <= 1; k++) {
                    for (int l = -1; l <= 1; l++) {
                        if (expansionMap[i + k][j + l] > 0 && expansionMap[i + k][j + l] >= current + centerCount - 1) {
                            if(chokes.contains(Coord.get(i + k, j + l)))
                                continue ELEMENT_WISE;
                            if (expansionMap[i - k][j - l] > 0 && expansionMap[i - k][j - l] >= current) {
                                good = true;
                            }
                        }
                    }
                }
                if(good) {
                    Coord chk = Coord.get(i, j);
                    chokes.add(chk);
                    waypoints.put(chk, new LinkedHashMap<Coord, Edge>());
                }
            }
        }

        if(measurement.equals2D(Radius.SQUARE))
            dm = new DijkstraMap(map, DijkstraMap.Measurement.CHEBYSHEV);
        else if(measurement.equals2D(Radius.DIAMOND))
            dm = new DijkstraMap(map, DijkstraMap.Measurement.MANHATTAN);
        else
            dm = new DijkstraMap(map, DijkstraMap.Measurement.EUCLIDEAN);

        int e = 0;
        for(Map.Entry<Coord, LinkedHashMap<Coord, Edge>> n : waypoints.entrySet())
        {
            chokes.remove(n.getKey());
            if(chokes.isEmpty())
                break;
            dm.clearGoals();
            dm.resetMap();
            dm.setGoal(n.getKey());
            dm.scan(null);
            for(Coord c : chokes)
            {
                n.getValue().put(c, new Edge(n.getKey(), c, dm.findPathPreScanned(c), dm.gradientMap[c.x][c.y]));
            }
        }

    }

    public ArrayList<Coord> getKnownPath(Coord self, Coord approximateTarget) {
        ArrayList<Coord> near = dm.findNearestMultiple(approximateTarget, 5, waypoints.keySet());
        Coord me = dm.findNearest(self, waypoints.keySet());
        double bestCost = 999999.0;
        ArrayList<Coord> path = new ArrayList<Coord>();
        if (waypoints.containsKey(me)) {
            Edge[] ed = waypoints.get(me).values().toArray(new Edge[waypoints.get(me).size()]);
            Arrays.sort(ed);
            path = ed[0].path;
        } else {
            for (Coord best : near) {
                if (waypoints.containsKey(best)) {
                    if (waypoints.get(best).get(me).cost < bestCost) {
                        bestCost = waypoints.get(best).get(me).cost;
                        path = new ArrayList<Coord>(waypoints.get(best).get(me).path);
                    }
                }
            }
            Collections.reverse(path);
        }
        return path;
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
            temp = Double.doubleToLongBits(cost);
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
