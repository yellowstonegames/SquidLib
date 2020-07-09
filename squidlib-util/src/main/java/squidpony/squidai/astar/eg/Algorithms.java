/*
MIT License

Copyright (c) 2020 earlygrey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package squidpony.squidai.astar.eg;

import squidpony.annotation.Beta;
import squidpony.squidai.astar.Heuristic;

import java.util.ArrayList;
import java.util.List;

@Beta
public class Algorithms<V> {

    final Graph<V> graph;
    final AlgorithmImplementations<V> implementations;

    Algorithms(Graph<V> graph) {
        this.graph = graph;
        implementations = new AlgorithmImplementations<>(graph);
    }

    //--------------------
    //  Shortest Path
    //--------------------

    /**
     * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
     * @param start the starting vertex
     * @param target the target vertex
     * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
     */
    public List<V> findShortestPath(V start, V target) {
        return findShortestPath(start, target, null);
    }

    /**
     * Find the shortest path between the start and target vertices, using the A* search algorithm with the provided heuristic, and implemented with a priority queue.
     * @param start the starting vertex
     * @param target the target vertex
     * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
     */
    public List<V> findShortestPath(V start, V target, Heuristic<V> heuristic) {
        List<V> list = new ArrayList<>();
        findShortestPath(start, target, list, heuristic);
        return list;
    }

    /*public boolean findShortestPath(V start, V target, List<V> path) {
        return findShortestPath(start, target, path, null);
    }*/

    /**
     * Find the shortest path between the start and target vertices, using the A* search algorithm with the provided heuristic, and implemented with a priority queue.
     * @param start the starting vertex
     * @param target the target vertex
     * @param path the list of vertices to which the path vertices should be added
     * @return a list of vertices from start to target containing the ordered vertices of a shortest path, including both the start and target vertices
     */
    public boolean findShortestPath(V start, V target, List<V> path, Heuristic<V> heuristic) {
        path.clear();
        Node<V> startNode = graph.getNode(start);
        Node<V> targetNode = graph.getNode(target);
        if (startNode==null || targetNode==null) throw new IllegalArgumentException("At least one vertex is not in the graph");
        implementations.findShortestPath(startNode, targetNode, path, heuristic);
        return !path.isEmpty();
    }

    /**
     * Find the shortest path between the start and target vertices, using Dijkstra's algorithm implemented with a priority queue.
     * @param start the starting vertex
     * @param target the target vertex
     * @return the sum of the weights in a shortest path from the starting vertex to the target vertex
     */
    public double findMinimumDistance(V start, V target) {
        return implementations.findMinimumDistance(graph.getNode(start), graph.getNode(target));
    }

    //--------------------
    // Graph Searching
    //--------------------

    /**
     * Perform a breadth first search starting from the specified vertex.
     * @param v the vertex at which to start the search
     * @param maxVertices the maximum number of vertices to process before terminating the search
     * @param maxDepth the maximum edge distance (the number of edges in a shortest path between vertices) a vertex should have to be
     *                 considered for processing. If a vertex has a distance larger than the maxDepth, it will not be added to the
     *                 returned graph
     * @return a Graph object containing all the processed vertices, and the edges from which each vertex was encountered.
     * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
     * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
     */
    public Graph<V> breadthFirstSearch(V v, int maxVertices, int maxDepth) {
        Node<V> node = graph.getNode(v);
        if (node==null) throw new IllegalArgumentException("At least one vertex is not in the graph");
        Graph<V> tree = graph.createNew();
        implementations.breadthFirstSearch(node, tree, maxVertices, maxDepth);
        return tree;
    }

    /**
     * Perform a breadth first search starting from the specified vertex.
     * @param v the vertex at which to start the search
     * @return a Graph object containing all the processed vertices (all the vertices in this graph), and the edges from which each vertex was encountered.
     * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
     * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
     */
    public Graph<V> breadthFirstSearch(V v) {
        return breadthFirstSearch(v, graph.size(), graph.size());
    }

    /**
     * Perform a depth first search starting from the specified vertex.
     * @param v the vertex at which to start the search
     * @param maxVertices the maximum number of vertices to process before terminating the search
     * @param maxDepth the maximum edge distance (the number of edges in a shortest path between vertices) a vertex should have to be
     *                 considered for processing. If a vertex has a distance larger than the maxDepth, it will not be added to the
     *                 returned graph
     * @return a Graph object containing all the processed vertices, and the edges from which each vertex was encountered.
     * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
     * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
     */
    public Graph<V> depthFirstSearch(V v, int maxVertices, int maxDepth) {
        Node<V> node = graph.getNode(v);
        if (node==null) throw new IllegalArgumentException("At least one vertex is not in the graph");
        Graph<V> tree = graph.createNew();
        implementations.depthFirstSearch(node, tree, maxVertices, maxDepth);
        return tree;
    }

    /**
     * Perform a depth first search starting from the specified vertex.
     * @param v the vertex at which to start the search
     * @return a Graph object containing all the processed vertices (all the vertices in this graph), and the edges from which each vertex was encountered.
     * The vertices and edges in the returned graph will be in the order they were encountered in the search, and this will be
     * reflected in the iteration order of the collections returned by {@link Graph#getVertices()} and {@link Graph#getEdges()}.
     */
    public Graph<V> depthFirstSearch(V v) {
        return depthFirstSearch(v, graph.size(), graph.size());
    }


    //--------------------
    //  Structures
    //--------------------

    /**
     * Checks whether there are any cycles in the graph using depth first searches.
     * @return true if the graph contains a cycle, false otherwise
     */
    public boolean detectCycle() {
        return implementations.containsCycle(graph);
    }

}
