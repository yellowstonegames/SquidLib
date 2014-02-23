package squidpony.squidutility.newgraph;

import java.util.HashMap;
import java.util.HashSet;
import squidpony.annotation.Beta;

/**
 * An implementation of Dijkstra's algorithm for pathfinding.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class Dijkstra {

    /**
     * Finds the closest node based on the cost in the provided graph. If there is no path from the
     * start node to the end node, the start node is returned.
     *
     * If either node is not in the graph then there is no path so the start node is returned.
     *
     * @param g
     * @param start
     * @return
     */
    public Node closestNode(Graph g, Node start, Node end) {
        if (!g.contains(start, end)) {
            return start;
        }

        Graph h = new Graph();
        HashSet<Node> unvisitedNodes = new HashSet<>();
        unvisitedNodes.addAll(g.nodes);
        h.nodes = g.nodes;

        Node currentNode = start;
        do {
            unvisitedNodes.remove(currentNode);
            Node cheapest = currentNode;
            HashMap<Node, Float> neighbors = g.edges.get(currentNode);
            for (Node n : neighbors.keySet()) {
                if (unvisitedNodes.contains(n) && neighbors.get(n) > currentNode.cost + g.getCost(currentNode, n)) {

                    cheapest = n;
                }
            }

            currentNode = cheapest;
        } while (!unvisitedNodes.isEmpty());

        /*         
         * For the current node, consider all of its unvisited neighbors and calculate their tentative distances. 
         * For example, if the current node A is marked with a distance of 6, and the edge connecting it with 
         * a neighbor B has length 2, then the distance to B (through A) will be 6 + 2 = 8. If this distance is 
         * less than the previously recorded tentative distance of B, then overwrite that distance. 
         * Even though a neighbor has been examined, it is not marked as "visited" at this time, and it remains in the unvisited set.
     
         * When we are done considering all of the neighbors of the current node, mark the current node as visited and remove it from the unvisited set. A visited node will never be checked again.
     
         * If the destination node has been marked visited (when planning a route between two specific nodes) or if the smallest tentative distance among the nodes in the unvisited set is infinity (when planning a complete traversal), then stop. The algorithm has finished.
     
         * Select the unvisited node that is marked with the smallest tentative distance, and set it as the new "current node" then go back to step 3.

         */
        return null;//dummy code to allow compiled testing of other library components
    }
}
