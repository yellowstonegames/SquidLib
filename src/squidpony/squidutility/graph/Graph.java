
package squidpony.squidutility.graph;

import java.util.LinkedList;
import java.util.List;
import squidpony.annotation.Beta;

/**
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface Graph {

    /**
     * Adds the two vertices to the graph connected by the given weight. If the
     * vertices are not already in the graph they will be added.
     *
     * @param v1
     * @param v2
     * @param weight
     */
    void addEdge(Vertex v1, Vertex v2, int weight);

    /**
     * Adds the vertex but does not attach it to any other vertices.
     *
     * @param v
     */
    void addVertex(Vertex v);

    List<Edge> getEdges();

    List<Vertex> getVertexes();

    /**
     * Returns a list of the vertices representing the 
     * @param start
     * @param end
     * @return 
     */
    LinkedList<Vertex> getDijkstraPath(Vertex start, Vertex end);
}
