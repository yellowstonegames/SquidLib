package squidpony.squidai.astar.eg;

import squidpony.squidai.astar.Heuristic;
import squidpony.squidmath.Coord;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Tommy Ettinger on 7/9/2020.
 */
public final class Heuristics {
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing twice as
	 * much as orthogonal movement. This is a good choice for graphs where only four-way movement is used.
	 */
	public static final Heuristic<Coord> MANHATTAN = new Heuristic<Coord>() {
		@Override
		public double estimate(Coord node, Coord endNode) {
			return Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y);
		}
	};
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing the same as
	 * orthogonal movement. This is only suggested for graphs where eight-way movement is used, and it may produce
	 * erratic paths compared to {@link #EUCLIDEAN}.
	 */
	public static final Heuristic<Coord> CHEBYSHEV = new Heuristic<Coord>() {
		@Override
		public double estimate(Coord node, Coord endNode) {
			return Math.max(Math.abs(node.x - endNode.x), Math.abs(node.y - endNode.y));
		}
	};
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where all movement is calculated "as-the-crow-flies," using
	 * the standard Pythagorean formula for distance as in the real world. This does not make diagonal connections, if
	 * they are allowed, actually cost more or less, but they won't be preferred if an orthogonal route can be taken.
	 * This is recommended for graphs where eight-way movement is used.
	 */
	public static final Heuristic<Coord> EUCLIDEAN = new Heuristic<Coord>() {
		@Override
		public double estimate(Coord node, Coord endNode) {
			return node.distance(endNode);
		}
	};
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where the heuristic is not used, and all cells are
	 * considered equivalent regardless of actual distance.
	 */
	public static final Heuristic<Coord> DIJKSTRA = new Heuristic<Coord>() {
		@Override
		public double estimate(Coord node, Coord endNode) {
			return 1.0;
		}
	};

	/**
	 * An unmodifiable List of all the Heuristic implementations in this class.
	 */
	public static final List<Heuristic<Coord>> HEURISTICS = Collections.unmodifiableList(Arrays.asList(MANHATTAN, CHEBYSHEV, EUCLIDEAN, DIJKSTRA));
}
