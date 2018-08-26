package squidpony.performance.alternate;


import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Performs A* search.
 *
 * A* is a best-first search algorithm for pathfinding. It uses a heuristic
 * value to reduce the total search space. If the heuristic is too large then
 * the optimal path is not guaranteed to be returned.
 * <br>
 * NOTE: Due to implementation problems, this class is atypically slow for A* and 34x slower than DijkstraMap on the
 * same inputs. It may be improved in future versions, but for now you should strongly prefer DijkstraMap.
 * @see squidpony.squidai.DijkstraMap a faster pathfinding algorithm with more features.
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class AStarSearch implements Serializable{
    private static final long serialVersionUID = -6315495888417856297L;
    /**
     * The type of heuristic to use.
     */
    public enum SearchType {

        /**
         * The distance it takes when only the four primary directions can be
         * moved in.
         */
        MANHATTAN,
        /**
         * The distance it takes when diagonal movement costs the same as
         * cardinal movement.
         */
        CHEBYSHEV,
        /**
         * The distance it takes as the crow flies.
         */
        EUCLIDEAN,
        /**
         * Full space search. Least efficient but guaranteed to return a path if
         * one exists. See also DijkstraMap class.
         */
        DIJKSTRA
    }

    protected final double[][] map;
    protected final HashSet<Coord> open = new HashSet<>();
    protected final int width, height;
    protected Coord[][] parent;
    protected Coord start, target;
    protected final SearchType type;

    protected AStarSearch()
    {
        width = 0;
        height = 0;
        type = SearchType.MANHATTAN;
        map = new double[width][height];
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
        this.map = map;
        width = map.length;
        height = width == 0 ? 0 : map[0].length;
        if (type == null)
        	throw new NullPointerException("SearchType should not be null when building an AStarSearch");
        this.type = type;
    }

    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null.
     *
     * @param startx the x coordinate of the start location
     * @param starty the y coordinate of the start location
     * @param targetx the x coordinate of the target location
     * @param targety the y coordinate of the target location
     * @return the shortest path, or null
     */
    public Queue<Coord> path(int startx, int starty, int targetx, int targety) {
        return path(Coord.get(startx, starty), Coord.get(targetx, targety));
    }
    /**
     * Finds an A* path to the target from the start. If no path is possible,
     * returns null.
     *
     * @param start the start location
     * @param target the target location
     * @return the shortest path, or null
     */
    public Queue<Coord> path(Coord start, Coord target) {
        this.start = start;
        this.target = target;
        open.clear();
        boolean[][] finished = new boolean[width][height];
        parent = new Coord[width][height];

        Direction[] dirs;
        switch (type) {
            case MANHATTAN:
                dirs = Direction.CARDINALS;
                break;
            case CHEBYSHEV:
            case EUCLIDEAN:
            case DIJKSTRA:
            default:
                dirs = Direction.OUTWARDS;
                break;
        }

        Coord p = start;
        open.add(p);

        while (!p.equals(target)) {
            finished[p.x][p.y] = true;
            open.remove(p);
            for (Direction dir : dirs) {

                int x = p.x + dir.deltaX;
                if (x < 0 || x >= width) {
                    continue;//out of bounds so skip ahead
                }

                int y = p.y + dir.deltaY;
                if (y < 0 || y >= height) {
                    continue;//out of bounds so skip ahead
                }

                if (!finished[x][y]) {
                    Coord test = Coord.get(x, y);
                    if (open.contains(test)) {
                        double parentG = g(parent[x][y].x, parent[x][y].y);
                        if (parentG < 0) {
                            continue;//not a valid point so skip ahead
                        }
                        double g = g(p.x, p.y);
                        if (g < 0) {
                            continue;//not a valid point so skip ahead
                        }
                        if (parent[x][y] == null || parentG > g) {
                            parent[x][y] = p;
                        }
                    } else {
                        open.add(test);
                        parent[x][y] = p;
                    }
                }
            }
            p = smallestF();
            if (p == null) {
                return null;//no path possible
            }
        }

        /* Not using Deque nor ArrayDeque, they aren't Gwt compatible */
        final LinkedList<Coord> deq = new LinkedList<>();
        while (!p.equals(start)) {
            deq.addFirst(p);
            p = parent[p.x][p.y];
        }
        return deq;
    }

    /**
     * Finds the g value (start to current) for the given location.
     *
     * If the given location is not valid or not attached to the pathfinding
     * then -1 is returned.
     *
     * @param x coordinate
     * @param y coordinate
     */
    protected double g(int x, int y) {
        if (x == start.x && y == start.y) {
            return 0;
        }
        if (x < 0 || y < 0 || x >= width || y >= height || map[x][y] < 0 || parent[x][y] == null) {
            return -1;//not a valid location
        }

        double parentG = g(parent[x][y].x, parent[x][y].y);
        if (parentG < 0) {
            return -1;//if any part of the path is not valid, this part is not valid
        }

        return map[x][y] + parentG + 1;//follow path back to start
    }

    /**
     * Returns the heuristic distance from the current cell to the goal location\
     * using the current calculation type.
     *
     * @param x coordinate
     * @param y coordinate
     * @return distance
     */
    protected double h(int x, int y) {
        switch (type) {
            case MANHATTAN:
                return Math.abs(x - target.x) + Math.abs(y - target.y);
            case CHEBYSHEV:
                return Math.max(Math.abs(x - target.x), Math.abs(y - target.y));
            case EUCLIDEAN:
                int xDist = Math.abs(x - target.x);
                xDist *= xDist;
                int yDist = Math.abs(y - target.y);
                yDist *= yDist;
                return Math.sqrt(xDist + yDist);
            case DIJKSTRA:
            default:
                return 0;

        }
    }

    /**
     * Combines g and h to get the estimated distance from start to goal going on the current route.
	 * @param x coordinate
	 * @param y coordinate
	 * @return The current known shortest distance to the start position from
	 *         the given position. If the current position cannot reach the
	 *         start position or is invalid, -1 is returned.
	 */
    protected double f(int x, int y) {
        double foundG = g(x, y);
        if (foundG < 0) {
            return -1;
        }
        return h(x, y) + foundG;
    }

    /**
     * @return the current open point with the smallest F
     */
    protected Coord smallestF() {
        Coord smallest = null;
        double smallF = Double.POSITIVE_INFINITY;
        double f;
        for (Coord p : open) {
            f = f(p.x, p.y);
            if (f < 0) {
                continue;//current tested point is not valid so skip it
            }
            if (smallest == null || f < smallF) {
                smallest = p;
                smallF = f;
            }
        }

        return smallest;
    }

	@Override
	public String toString() {
		final int width = map.length;
		final int height = width == 0 ? 0 : map[0].length;
		final StringBuilder result = new StringBuilder(width * height);
		int maxLen = 0;
		/*
		 * First we compute the longest (String-wise) entry, so that we can
		 * "indent" shorter cells, so that the output looks good (and is hereby
		 * readable).
		 */
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final String output = String.valueOf(Math.round(map[x][y]));
				final int locLen = output.length();
				if (maxLen < locLen)
					maxLen = locLen;
			}
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final long v = Math.round(map[x][y]);
				final String s = String.valueOf(v);
				final int slen = s.length();
				assert slen <= maxLen;
				int diff = maxLen - slen;
				while (0 < diff) {
					result.append(" ");
					diff--;
				}
				result.append(s);
			}
			if (y < height - 1)
				result.append('\n');
		}
		return result.toString();
	}

}
