package squidpony.squidmath;

import squidpony.squidai.graph.Heuristic;
import squidpony.squidai.graph.CostlyGraph;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * Performs A* search to find the shortest path between two Coord points.
 * <br>
 * A* is a best-first search algorithm for pathfinding. It uses a heuristic
 * value to reduce the total search space. If the heuristic is too large then
 * the optimal path is not guaranteed to be returned.
 * <br>
 * This implementation is a thin wrapper around {@link squidpony.squidai.graph.CostlyGraph} and the other code in the
 * {@code squidpony.squidai.graph} package; it replaces an older, much-less-efficient A* implementation with one based
 * on code from simple-graphs by earlygrey. The current version is quite fast, typically outpacing gdx-ai's more-complex
 * IndexedAStarPathfinder by a high margin. The only major change in the API is that this version returns an ArrayList
 * of Coord instead of a Queue of Coord. Typical usage of this class involves either the simpler technique of only using
 * {@code '#'} for walls or obstructions and calling {@link #AStarSearch(char[][], SearchType)}, or the more complex
 * technique that allows variable costs for different types of terrain, using
 * {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][], Map, double)} to generate a cost
 * map; if you used the old AStarSearch, then be advised that the default cost is now 1.0 instead of 0.0.
 * @see squidpony.squidai.graph.CostlyGraph the pathfinding class this is based on; CostlyGraph can be used independently
 * @see squidpony.squidai.DijkstraMap a sometimes-faster pathfinding algorithm that can pathfind to multiple goals
 * @see squidpony.squidai.CustomDijkstraMap an alternative to DijkstraMap; faster and supports complex adjacency rules
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - optimized code
 * @author earlygrey - wrote and really optimized simple-graphs, which this uses heavily
 */
public class AStarSearch implements Serializable {
    private static final long serialVersionUID = 11L;
    /**
     * The type of heuristic to use.
     */
    public enum SearchType {

        /**
         * The distance it takes when only the four primary directions can be
         * moved in.
         */
        MANHATTAN(Heuristic.MANHATTAN),
        /**
         * The distance it takes when diagonal movement costs the same as
         * cardinal movement.
         */
        CHEBYSHEV(Heuristic.CHEBYSHEV),
        /**
         * The distance it takes as the crow flies.
         */
        EUCLIDEAN(Heuristic.EUCLIDEAN),
        /**
         * Full space search. Least efficient but guaranteed to return a path if
         * one exists. See also {@link squidpony.squidai.DijkstraMap}.
         */
        DIJKSTRA(Heuristic.DIJKSTRA);
        Heuristic<Coord> heuristic;
        SearchType(Heuristic<Coord> heuristic){
            this.heuristic = heuristic;
        }
    }

    protected int width, height;
    protected Coord start, target;
    protected SearchType type;
    
    protected CostlyGraph graph;
    protected ArrayList<Coord> path;
    
    
    protected AStarSearch()
    {
        width = 0;
        height = 0;
        type = SearchType.MANHATTAN;
    }
    /**
     * Builds a pathing object to run searches on.
     * <br>
     * Values in the map are treated as positive values being legal weights, with higher values being harder to pass
     * through. Any negative value is treated as being an impassible space. A weight of 0 can be moved through at no
     * cost, but this should be used very carefully, if at all. Cost maps are commonly built using the
     * {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][], Map, double)} and
     * {@link  squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][])} methods from a 2D char array.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map the search map, as produced by {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][])}
     * @param type the manner of search
     */
    public AStarSearch(double[][] map, SearchType type) {
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? SearchType.EUCLIDEAN : type;         
        graph = new CostlyGraph(map, (this.type != SearchType.MANHATTAN));
        path = new ArrayList<>(width + height);
    }
    /**
     * Builds a pathing object to run searches on.
     * <br>
     * Values in the map are all considered equally passable unless the char is {@code '#'}, in which case it is
     * considered an impassable wall. The {@link DungeonGenerator#getBareDungeon()} method is a common way to get a map
     * where only '#' is used to mean a wall.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map a 2D char array where only {@code '#'} represents a wall, and anything else is equally passable
     * @param type the manner of search
     */
    public AStarSearch(char[][] map, SearchType type) {
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? SearchType.EUCLIDEAN : type;
        graph = new CostlyGraph(map, (this.type != SearchType.MANHATTAN));
        path = new ArrayList<>(width + height);
    }

    /**
     * Resets this pathing object to use a different map and optionally a different SearchType.
     * <br>
     * Values in the map are treated as positive values being legal weights, with higher values being harder to pass
     * through. Any negative value is treated as being an impassible space. A weight of 0 can be moved through at no
     * cost, but this should be used very carefully, if at all. Cost maps are commonly built using the
     * {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][], Map, double)} and
     * {@link  squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][])} methods from a 2D char array.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map the search map, as produced by {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][])}
     * @param type the manner of search
     */
    public AStarSearch reinitialize(double[][] map, SearchType type){
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? SearchType.EUCLIDEAN : type;
        graph.init(map, this.type != SearchType.MANHATTAN);
        return this;
    }

    /**
     * Resets this pathing object to use a different map and optionally a different SearchType.
     * <br>
     * Values in the map are all considered equally passable unless the char is {@code '#'}, {@code '+'}, or any box
     * drawing character, in which case it is considered an impassable wall. {@link DungeonGenerator#getBareDungeon()}
     * is a common way to get a map where only '#' is used to mean a wall.
     * <br>
     * If the type is Manhattan, only the cardinal directions will be used. All other search types will return a result
     * based on diagonal and cardinal pathing (8-way).
     *
     * @param map a 2D char array where only {@code '#'} represents a wall, and anything else is equally passable
     * @param type the manner of search
     */
    public AStarSearch reinitialize(char[][] map, SearchType type){
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? SearchType.EUCLIDEAN : type;
        graph.init(DungeonUtility.generateAStarCostMap(map), this.type != SearchType.MANHATTAN);
        return this;
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
        if(graph.findShortestPath(start, target, path, type.heuristic)) 
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
        final int w5 = width * 5;
        final char[] cs = graph.show();
        cs[start.y * w5 + start.x * 5] = cs[start.y * w5 + start.x * 5 + 1] =
                cs[start.y * w5 + start.x * 5 + 2] = cs[start.y * w5 + start.x * 5 + 3] = '@';
        cs[target.y * w5 + target.x * 5] = cs[target.y * w5 + target.x * 5 + 1] =
                cs[target.y * w5 + target.x * 5 + 2] = cs[target.y * w5 + target.x * 5 + 3] = '!';
        return String.valueOf(cs);
    }
}
