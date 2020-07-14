/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package squidpony.squidai.graph;

import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A {@code Heuristic} generates estimates of the cost to move from a given node to the goal.
 * <p>
 * With a heuristic function pathfinding algorithms can choose the node that is most likely to lead to the optimal path.
 * The notion of "most likely" is controlled by a heuristic. If the heuristic is accurate, then the algorithm will be
 * efficient. If the heuristic is terrible, then it can perform even worse than other algorithms that don't use any
 * heuristic function such as Dijkstra. SquidLib's {@link squidpony.squidai.DijkstraMap} is specialized for some cases
 * that A* isn't, so there are reasons to prefer DijkstraMap when, for instance, you have multiple goals, or the goal is
 * unchanging for some section of usage but the start point changes often (this is useful for mouse tracking when the
 * path is reversed). The astar package should be significantly faster when paths are short and always have one goal,
 * unless you compare it to DijkstraMap when it can reuse a scan and call
 * {@link squidpony.squidai.DijkstraMap#findPathPreScanned(ArrayList, Coord)}.
 * 
 * @param <V> Type of vertex; this is usually {@link Coord}
 * 
 * @author davebaol */
public interface Heuristic<V> {

	/** Calculates an estimated cost to reach the goal node from the given node.
	 * @param node the start node
	 * @param endNode the end node
	 * @return the estimated cost */
	double estimate(V node, V endNode);

	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing twice as
	 * much as orthogonal movement. This is a good choice for graphs where only four-way movement is used.
	 */
	Heuristic<Coord> MANHATTAN = new Heuristic<Coord>() {
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
	Heuristic<Coord> CHEBYSHEV = new Heuristic<Coord>() {
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
	Heuristic<Coord> EUCLIDEAN = new Heuristic<Coord>() {
		@Override
		public double estimate(Coord node, Coord endNode) {
			return node.distance(endNode);
		}
	};
	/**
	 * A predefined Heuristic for Coord nodes in a 2D plane where the heuristic is not used, and all cells are
	 * considered equivalent regardless of actual distance.
	 */
	Heuristic<Coord> DIJKSTRA = new Heuristic<Coord>() {
		@Override
		public double estimate(Coord node, Coord endNode) {
			return 0.0;
		}
	};
	/**
	 * An unmodifiable List of all the Heuristic implementations in this class.
	 */
	List<Heuristic<Coord>> HEURISTICS = Arrays.asList(MANHATTAN, CHEBYSHEV, EUCLIDEAN, DIJKSTRA);
}
