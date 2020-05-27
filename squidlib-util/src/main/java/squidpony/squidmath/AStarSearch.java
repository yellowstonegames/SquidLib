package squidpony.squidmath;

import squidpony.squidai.astar.DefaultGraph;
import squidpony.squidai.astar.Heuristic;
import squidpony.squidai.astar.Pathfinder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * Performs A* search.
 *
 * A* is a best-first search algorithm for pathfinding. It uses a heuristic
 * value to reduce the total search space. If the heuristic is too large then
 * the optimal path is not guaranteed to be returned.
 * <br>
 * This implementation is a thin wrapper around {@link Pathfinder} and the other code in the
 * {@code squidpony.squidai.astar} package; it replaces an older, much-less-efficient A* implementation with one based
 * on GDX-AI's IndexedAStarPathFinder class. The only major change in the API is that this version returns an ArrayList
 * of Coord instead of a Queue of Coord. Typical usage of this class involves
 * {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][], Map, double)} to generate the cost
 * map; if you used the old AStarSearch, then be advised that the default cost is now 1.0 instead of 0.0.
 * @see squidpony.squidai.astar.Pathfinder the pathfinding class this is based on; Pathfinder can be used independently
 * @see squidpony.squidai.DijkstraMap a sometimes-faster pathfinding algorithm that can pathfind to multiple goals
 * @see squidpony.squidai.CustomDijkstraMap an alternative to DijkstraMap; faster and supports complex adjacency rules
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - optimized code
 */
public class AStarSearch implements Serializable {
    private static final long serialVersionUID = 10L;
    /**
     * The type of heuristic to use.
     */
    public enum SearchType {

        /**
         * The distance it takes when only the four primary directions can be
         * moved in.
         */
        MANHATTAN(DefaultGraph.MANHATTAN),
        /**
         * The distance it takes when diagonal movement costs the same as
         * cardinal movement.
         */
        CHEBYSHEV(DefaultGraph.CHEBYSHEV),
        /**
         * The distance it takes as the crow flies.
         */
        EUCLIDEAN(DefaultGraph.EUCLIDEAN),
        /**
         * Full space search. Least efficient but guaranteed to return a path if
         * one exists. See also DijkstraMap class.
         */
        DIJKSTRA(DefaultGraph.DIJKSTRA);
        Heuristic<Coord> heuristic;
        SearchType(Heuristic<Coord> heuristic){
            this.heuristic = heuristic;
        }
    }

    protected final int width, height;
    protected Coord start, target;
    protected final SearchType type;
    
    protected DefaultGraph graph;
    protected Pathfinder<Coord> pathfinder;
    protected ArrayList<Coord> path;
    
    
    protected AStarSearch()
    {
        width = 0;
        height = 0;
        type = SearchType.MANHATTAN;
    }
    /**
     * Builds a pathing object to run searches on.
     *
     * Values in the map are treated as positive values (and 0) being legal
     * weights, with higher values being harder to pass through. Any negative
     * value is treated as being an impassible space.
     *
     * If the type is Manhattan, only the cardinal directions will be used. All
     * other search types will return results based on intercardinal and
     * cardinal pathing.
     *
     * @param map
     *            the search map. It is not modified by this class, hence you can
     *            share this map among multiple instances.
     * @param type the manner of search
     */
    public AStarSearch(double[][] map, SearchType type) {
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? SearchType.DIJKSTRA : type;         
        graph = new DefaultGraph(map, (this.type != SearchType.MANHATTAN));
        pathfinder = new Pathfinder<>(graph, true);
        path = new ArrayList<>(width + height);
    }

    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null.
     *
     * @param startx the x coordinate of the start location
     * @param starty the y coordinate of the start location
     * @param targetx the x coordinate of the target location
     * @param targety the y coordinate of the target location
     * @return the shortest path, or null if no path is possible
     */
    public ArrayList<Coord> path(int startx, int starty, int targetx, int targety) {
        return path(Coord.get(startx, starty), Coord.get(targetx, targety));
    }
    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null.
     *
     * @param start the start location
     * @param target the target location
     * @return the shortest path, or null if no path is possible
     */
    public ArrayList<Coord> path(Coord start, Coord target) {
        path.clear();
        this.start = start;
        this.target = target;
        if(pathfinder.searchNodePath(start, target, type.heuristic, path)) 
            return path;
        else
            return null;
    }

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
        int cellSize = (int)Math.log10(Math.round(pathfinder.metrics.maxCost)) + 2;
        StringBuilder result = new StringBuilder((width * cellSize + 1) * height);
        graph.show(result, pathfinder, pathfinder.metrics);
        for (int i = 0; i + 1 < cellSize; i++) {
            result.setCharAt((width * cellSize + 1) * start.y + cellSize * start.x + i, '@');
            result.setCharAt((width * cellSize + 1) * target.y + cellSize * target.x + i, '!');
        }
        return result.toString();
    }
    /*
    public static final int DIMENSION = 40, PATH_LENGTH = (DIMENSION - 2) * (DIMENSION - 2);
    public static DungeonGenerator dungeonGen =
            new DungeonGenerator(DIMENSION, DIMENSION, new StatefulRNG(0x1337BEEFDEAL));
    public static SerpentMapGenerator serpent = new SerpentMapGenerator(DIMENSION, DIMENSION,
            new StatefulRNG(0x1337BEEFDEAL));
    public static char[][] mp;
    public static double[][] astarMap;
    public static GreasedRegion floors;
    public static void main(String[] args)
    {
        serpent.putWalledBoxRoomCarvers(1);
        mp = dungeonGen.generate(serpent.generate());
        floors = new GreasedRegion(mp, '.');
        astarMap = DungeonUtility.generateAStarCostMap(mp, Collections.<Character, Double>emptyMap(), 1);
        long time = System.currentTimeMillis(), len;
        len = doPathAStar2();
        System.out.println(System.currentTimeMillis() - time);
        System.out.println(len);
    }
    public static long doPathAStar2()
    {
        AStarSearch astar = new AStarSearch(astarMap, AStarSearch.SearchType.CHEBYSHEV);
        Coord r;
        long scanned = 0;
        DungeonUtility utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEFDEAL)));
        Queue<Coord> latestPath;
        for (int x = 1; x < DIMENSION - 1; x++) {
            for (int y = 1; y < DIMENSION - 1; y++) {
                if (mp[x][y] == '#')
                    continue;
                // this should ensure no blatant correlation between R and W
                utility.rng.setState((x << 22) | (y << 16) | (x * y));
                r = floors.singleRandom(utility.rng);
                latestPath = astar.path(r, Coord.get(x, y));
                scanned+= latestPath.size();
            }
        }
        return scanned;
    }
    */
}
