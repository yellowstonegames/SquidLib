package squidpony.squidutility.newgraph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import squidpony.annotation.Beta;

/**
 * A basic graph class that contains nodes and edges and can provide basic cost
 * information for directly connected nodes.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class Graph {

    public TreeMap<Node, HashMap<Node, Float>> edges = new TreeMap<>();
    public HashSet<Node> nodes = new HashSet<>();

    /**
     * Adds all of the provided nodes to the graph, without adding edges between
     * them.
     *
     * @param nodeList
     */
    public void addNodes(Node... nodeList) {
        nodes.addAll(Arrays.asList(nodeList));
    }

    /**
     * Returns true if all of the provided nodes are in the graph.
     *
     * @param nodeList
     * @return
     */
    public boolean contains(Node... nodeList) {
        for (Node n : nodeList) {
            if (!nodes.contains(n)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds the given edge to the graph. If the edge was previously added then
     * the cost is updated to the provided cost.
     *
     * @param start
     * @param end
     * @param cost
     */
    public void addEdge(Node start, Node end, Float cost) {
        addNodes(start, end);
        if (!edges.containsKey(start)) {
            edges.put(start, new HashMap<Node, Float>());
        }

        edges.get(start).put(end, cost);
    }

    /**
     * Returns the cost to travel from the start node to the end node.
     *
     * If there is no edge leading from the start node to the endo node, then
     * infinity is returned.
     *
     * @param start
     * @param end
     * @return
     */
    public float getCost(Node start, Node end) {
        HashMap<Node, Float> map = edges.get(start);
        if (map != null) {
            return map.get(end);
        }

        return Float.POSITIVE_INFINITY;
    }
}
