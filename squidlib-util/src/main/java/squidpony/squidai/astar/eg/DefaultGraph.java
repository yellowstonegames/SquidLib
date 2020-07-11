package squidpony.squidai.astar.eg;

import squidpony.squidai.astar.Heuristic;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A default setting for an {@link UndirectedGraph} of Coord vertices where all connections have cost 1. This should be
 * initialized with a 2D (rectangular) char array using the map convention where {@code '#'} is a wall and anything else
 * is passable.
 * <br>
 * Created by Tommy Ettinger on 7/9/2020.
 */
public class DefaultGraph extends UndirectedGraph<Coord> implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * No-op no-arg constructor, present for {@link Serializable}.
	 */
	protected DefaultGraph() {
	}
	/**
	 * The same as constructing a DefaultGraph with {@link #DefaultGraph(char[][], boolean)} and false for the last
	 * parameter (this uses 4-way adjacency).
	 * @param map a 2D char array where {@code '#'} represents an inaccessible area (such as a wall) and anything else is walkable
	 */
	public DefaultGraph(char[][] map) {
		this(map, false);
	}

	/**
	 * Builds a DefaultGraph from a 2D char array that uses {@code '#'} to represent any kind of inaccessible cell, with
	 * all other chars treated as walkable. If {@code eightWay} is true, this builds connections along diagonals as well
	 * as along cardinals, but if {@code eightWay} is false, it only builds connections along cardinal directions.
	 * @param map a 2D char array where {@code '#'} represents an inaccessible area (such as a wall) and anything else is walkable
	 * @param eightWay if true, this will build connections on diagonals as well as cardinal directions; if false, this will only use cardinal connections
	 */
	public DefaultGraph(char[][] map, boolean eightWay) {
		super();
		final int width = map.length, height = map[0].length;
		Coord.expandPoolTo(width, height);
		ArrayList<Coord> vs = new ArrayList<>(width * height >>> 1);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if(map[x][y] != '#')
				{
					Coord pt = Coord.get(x, y);
					vs.add(pt);
					addVertex(pt);
				}
			}
		}
		final int sz = vs.size();
		Coord center, off;
		final Direction[] outer = eightWay ? Direction.CLOCKWISE : Direction.CARDINALS_CLOCKWISE;
		Direction dir;
		for (int i = 0; i < sz; i++) {
			center = vs.get(i);
			for (int j = 0; j < outer.length; j++) {
				dir = outer[j];
				off = center.translate(dir);
				if(off.isWithin(width, height) && map[center.x + dir.deltaX][center.y + dir.deltaY] != '#')
				{
					if(!edgeExists(center, off))
					{
						addEdge(center, off);
					}
				}
			}
		}
	}

	/**
	 * Find a minimum weight spanning tree using Kruskal's algorithm.
	 * @return a Graph object containing a minimum weight spanning tree (if this graph is connected -
	 * in general a minimum weight spanning forest)
	 */
	public Graph<Coord> findMinimumWeightSpanningTree() {
		return algorithms.findMinimumWeightSpanningTree();
	}

	/**
	 * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
	 */
	public ArrayList<Coord> findShortestPath(Coord start, Coord target) {
		return algorithms.findShortestPath(start, target);
	}

	/**
	 * Find the shortest path between the start and target vertices, using the A* search algorithm with the provided heuristic, and implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @param heuristic typically predefined in {@link Heuristic}, this determines how the optimal path will be estimated
	 * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
	 */
	public ArrayList<Coord> findShortestPath(Coord start, Coord target, Heuristic<Coord> heuristic) {
		return algorithms.findShortestPath(start, target, heuristic);
	}

	/**
	 * Find the shortest path between the start and target vertices, using the A* search algorithm with the provided heuristic, and implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @param path the list of vertices to which the path vertices should be added
	 * @param heuristic typically predefined in {@link Heuristic}, this determines how the optimal path will be estimated
	 * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
	 */
	public boolean findShortestPath(Coord start, Coord target, ArrayList<Coord> path, Heuristic<Coord> heuristic) {
		return algorithms.findShortestPath(start, target, path, heuristic);
	}

	/**
	 * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
	 * @param start the starting vertex
	 * @param target the target vertex
	 * @return the sum of the weights in a shortest path from the starting vertex to the target vertex
	 */
	public double findMinimumDistance(Coord start, Coord target) {
		return algorithms.findMinimumDistance(start, target);
	}

	/**
	 * Perform a breadth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @param maxVertices the maximum number of vertices to process before terminating the search
	 * @param maxDepth the maximum edge distance (the number of edges in a shortest path between vertices) a vertex should have to be
	 *                 considered for processing. If a vertex has a distance larger than the maxDepth, it will not be added to the
	 *                 returned graph
	 * @return a Graph object containing all the processed vertices, and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> breadthFirstSearch(Coord coord, int maxVertices, int maxDepth) {
		return algorithms.breadthFirstSearch(coord, maxVertices, maxDepth);
	}

	/**
	 * Perform a breadth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @return a Graph object containing all the processed vertices (all the vertices in this graph), and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> breadthFirstSearch(Coord coord) {
		return algorithms.breadthFirstSearch(coord);
	}

	/**
	 * Perform a depth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @param maxVertices the maximum number of vertices to process before terminating the search
	 * @param maxDepth the maximum edge distance (the number of edges in a shortest path between vertices) a vertex should have to be
	 *                 considered for processing. If a vertex has a distance larger than the maxDepth, it will not be added to the
	 *                 returned graph
	 * @return a Graph object containing all the processed vertices, and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> depthFirstSearch(Coord coord, int maxVertices, int maxDepth) {
		return algorithms.depthFirstSearch(coord, maxVertices, maxDepth);
	}

	/**
	 * Perform a depth first search starting from the specified vertex.
	 * @param coord the vertex at which to start the search
	 * @return a Graph object containing all the processed vertices (all the vertices in this graph), and the edges from which each vertex was encountered.
	 * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
	 * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
	 */
	public Graph<Coord> depthFirstSearch(Coord coord) {
		return algorithms.depthFirstSearch(coord);
	}

	/**
	 * Checks whether there are any cycles in the graph using depth first searches.
	 * @return true if the graph contains a cycle, false otherwise
	 */
	public boolean detectCycle() {
		return algorithms.detectCycle();
	}
}
