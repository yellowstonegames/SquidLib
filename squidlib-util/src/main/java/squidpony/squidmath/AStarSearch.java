package squidpony.squidmath;

import squidpony.squidai.astar.DefaultGraph;
import squidpony.squidai.astar.Heuristic;
import squidpony.squidai.astar.Pathfinder;
import squidpony.squidgrid.mapping.DungeonGenerator;

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
 * of Coord instead of a Queue of Coord. Typical usage of this class involves either the simpler technique of only using
 * {@code '#'} for walls or obstructions and calling {@link #AStarSearch(char[][], SearchType)}, or the more complex
 * technique that allows variable costs for different types of terrain, using
 * {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][], Map, double)} to generate a cost
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
        graph = new DefaultGraph(map, (this.type != SearchType.MANHATTAN));
        pathfinder = new Pathfinder<>(graph, true);
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
        graph = new DefaultGraph(map, (this.type != SearchType.MANHATTAN));
        pathfinder = new Pathfinder<>(graph, true);
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
        pathfinder = new Pathfinder<>(graph, true);
        return this;
    }

    /**
     * Resets this pathing object to use a different map and optionally a different SearchType.
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
    public AStarSearch reinitialize(char[][] map, SearchType type){
        if (map == null)
            throw new NullPointerException("map should not be null when building an AStarSearch");
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        this.type = type == null ? SearchType.EUCLIDEAN : type;
        graph.init(map, this.type != SearchType.MANHATTAN);
        pathfinder = new Pathfinder<>(graph, true);
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
}
