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
package squidpony.squidai.graph;

import squidpony.squidmath.BinaryHeap;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

class AlgorithmImplementations<V> {

    //================================================================================
    // Fields
    //================================================================================

    private final Graph<V> graph;
    private final BinaryHeap<Node<V>> heap;
    private final ArrayDeque<Node<V>> queue;
    private int runID = 0;

    //================================================================================
    // Constructor
    //================================================================================

    AlgorithmImplementations(Graph<V> graph) {
        this.graph = graph;
        heap = new BinaryHeap<>();
        queue = new ArrayDeque<>();
    }

    //================================================================================
    // Util
    //================================================================================

    private void init() {
        runID++;
    }

    int getRunID() {
        return runID;
    }

    //================================================================================
    // Connectivity
    //================================================================================

    boolean isReachable(Node<V> start, Node<V> target) {
        return findShortestPath(start, target, new ArrayList<V>());
    }

    //================================================================================
    // Searches
    //================================================================================

    void breadthFirstSearch(Node<V> vertex, Graph<V> tree, int maxVertices, int maxDepth) {
        if (maxDepth <= 0 ) return;
        init();

        vertex.resetAlgorithmAttributes(runID);
        vertex.visited = true;
        ArrayDeque<Node<V>> queue = this.queue;
        queue.clear();
        queue.addLast(vertex);

        while(!queue.isEmpty()) {
            Node<V> v = queue.poll();
            tree.addVertex(v.object);
            if (v.prev != null) tree.addEdge(v.object, v.prev.object);
            if (v.i == maxDepth) continue;
            if (tree.size() == maxVertices) break;
            int n = v.outEdges.size();
            for (int i = 0; i < n; i++) {
                Connection<V> e = v.outEdges.get(i);
                Node<V> w = e.b;
                w.resetAlgorithmAttributes(runID);
                if (!w.visited) {
                    w.visited = true;
                    w.i = v.i+1;
                    w.prev = v;
                    queue.addLast(w);
                }
            }
        }
    }

    void depthFirstSearch(Node<V> vertex, Graph<V> tree, int maxVertices, int maxDepth) {
        init();

        vertex.resetAlgorithmAttributes(runID);
        ArrayDeque<Node<V>> queue = this.queue;
        queue.clear();
        queue.addLast(vertex);

        while(!queue.isEmpty()) {
            Node<V> v = queue.poll();
            if (!v.visited) {
                tree.addVertex(v.object);
                if (v.prev != null) tree.addEdge(v.object, v.prev.object);
                if (v.i == maxDepth) continue;
                if (tree.size() == maxVertices) break;
                v.visited = true;
                int n = v.outEdges.size();
                for (int i = 0; i < n; i++) {
                    Connection<V> e = v.outEdges.get(i);
                    Node<V> w = e.b;
                    w.resetAlgorithmAttributes(runID);
                    w.i = v.i+1;
                    w.prev = v;
                    queue.addFirst(w);
                }
            }
        }
    }

    //================================================================================
    // Shortest Paths
    //================================================================================

    double findMinimumDistance(Node<V> start, Node<V> target) {
        Node<V> end = aStarSearch(start, target, null);
        if (end==null) return Float.MAX_VALUE;
        else return end.distance;
    }

    ArrayList<V> findShortestPath(Node<V> start, Node<V> target) {
        ArrayList<V> path = new ArrayList<>();
        findShortestPath(start, target, path);
        return path;
    }

    boolean findShortestPath(Node<V> start, Node<V> target, ArrayList<V> path) {
        return findShortestPath(start, target, path, null);
    }

    ArrayList<V> findShortestPath(Node<V> start, Node<V> target, Heuristic<V> heuristic) {
        ArrayList<V> path = new ArrayList<>();
        findShortestPath(start, target, path, heuristic);
        return path;
    }

    boolean findShortestPath(Node<V> start, Node<V> target, ArrayList<V> path, Heuristic<V> heuristic) {
        Node<V> end = aStarSearch(start, target, heuristic);
        if (end==null) {
            return false;
        }
        Node<V> v = end;
        while(v.prev!=null) {
            path.add(v.object);
            v = v.prev;
        }
        path.add(start.object);
        Collections.reverse(path);
        return true;
    }

    private Node<V> aStarSearch(Node<V> start, Node<V> target, Heuristic<V> heuristic) {
        init();

        boolean hasHeuristic = heuristic != null;
        
        start.resetAlgorithmAttributes(runID);
        start.distance = 0;

        heap.add(start);

        while(heap.size != 0) {
            Node<V> u = heap.pop();
            if (u == target) {
                heap.clear();
                return u;
            }
            if (!u.visited) {
                u.visited = true;
                int n = u.outEdges.size();
                for (int i = 0; i < n; i++) {
                    Connection<V> e = u.outEdges.get(i);
                    Node<V> v = e.b;
                    v.resetAlgorithmAttributes(runID);
                    if (!v.visited) {
                        double newDistance = u.distance + e.weight;
                        if (newDistance < v.distance) {
                            v.distance = newDistance;
                            v.prev = u;
                            if (hasHeuristic && !v.seen) {
                                v.estimate = heuristic.estimate(v.object, target.object);
                            }
                            if (!v.seen) {
                                heap.add(v, v.distance + v.estimate);
                            } else {
                                heap.setValue(v, v.distance + v.estimate);
                            }
                            v.seen = true;
                        }
                    }
                }
            }
        }
        heap.clear();
        return null;
    }

    //================================================================================
    // Topological sorting
    //================================================================================

    boolean topologicalSort(ArrayList<V> sortedVertices) {
        sortedVertices.clear();
        init();
        OrderedSet<Node<V>> set = new OrderedSet<>(graph.vertexMap.values());
        boolean success = true;
        while (success && !set.isEmpty()) {
            success = recursiveTopologicalSort(sortedVertices, set.first(), set);
        }
        if (success) {
            Collections.reverse(sortedVertices);
        }

        return success;
    }

    boolean topologicalSort() {
        ArrayList<V> sortedVertices = new ArrayList<>();
        init();
        OrderedSet<Node<V>> set = new OrderedSet<>(graph.vertexMap.values());
        boolean success = true;
        while (success && !set.isEmpty()) {
            success = recursiveTopologicalSort(sortedVertices, set.first(), set);
        }
        if (success) {
            for (int i = sortedVertices.size()-1; i >= 0; i--) {
                graph.vertexMap.getAndMoveToLast(sortedVertices.get(i));
            }
        }
        return success;
    }

    private boolean recursiveTopologicalSort(ArrayList<V> sortedVertices, Node<V> v, Set<Node<V>> set) {
        v.resetAlgorithmAttributes(runID);

        if (v.visited) return true;
        if (v.seen) {
            // not a DAG
            return false;
        }
        v.seen = true;
        int n = v.outEdges.size();
        for (int i = 0; i < n; i++) {
            Connection<V> e = v.outEdges.get(i);
            boolean success = recursiveTopologicalSort(sortedVertices, e.b, set);
            if (!success) return false;
        }
        v.seen = false;
        v.visited = true;
        sortedVertices.add(v.object);
        set.remove(v);
        return true;
    }

    //================================================================================
    // Minimum spanning trees
    //================================================================================

    final Comparator<Connection<V>> weightComparator = new Comparator<Connection<V>>() {
        @Override
        public int compare(Connection<V> o1, Connection<V> o2) {
            return NumberTools.floatToIntBits(o1.weight - o2.weight);
        }
    };

    final Comparator<Connection<V>> reverseWeightComparator = new Comparator<Connection<V>>() {
        @Override
        public int compare(Connection<V> o1, Connection<V> o2) {
            return NumberTools.floatToIntBits(o2.weight - o1.weight);
        }
    };
    
    // adapted from https://www.baeldung.com/java-spanning-trees-kruskal

    Graph<V> kruskalsMinimumWeightSpanningTree(boolean minSpanningTree) {
        init();

        Graph<V> spanningTree = graph.createNew();

        spanningTree.addVertices(graph.vertexMap.keySet());

        ArrayList<Connection<V>> edgeList = new ArrayList<>(graph.edgeMap.values());

        Collections.sort(edgeList, minSpanningTree ? weightComparator : reverseWeightComparator);

        int maxNodes = graph.size() - 1;
        final int totalEdges = edgeList.size();
        int edgeCount = 0;

        for (int i = 0; i < totalEdges; i++) {
            final Connection<V> edge = edgeList.get(i);
            if (doesEdgeCreateCycle(edge.a, edge.b)) {
                continue;
            }
            spanningTree.addConnection(edge.a, edge.b, edge.weight);
            edgeCount++;
            if (edgeCount == maxNodes) {
                break;
            }
        }

        return spanningTree;
    }

    private void unionByRank(Node<V> rootU, Node<V> rootV) {
        if (rootU.i < rootV.i) {
            rootU.prev = rootV;
        } else {
            rootV.prev = rootU;
            if (rootU.i == rootV.i) rootU.i++;
        }
    }

    private Node<V> find(Node<V> node) {
        if (node.prev.equals(node)) {
            return node;
        } else {
            return find(node.prev);
        }
    }
    private Node<V> pathCompressionFind(Node<V> node) {
        if (node.prev.equals(node)) {
            return node;
        } else {
            Node<V> parentNode = find(node.prev);
            node.prev = parentNode;
            return parentNode;
        }
    }

    private boolean doesEdgeCreateCycle(Node<V> u, Node<V> v) {
        if (u.resetAlgorithmAttributes(runID)) u.prev = u;
        if (v.resetAlgorithmAttributes(runID)) v.prev = v;
        Node<V> rootU = pathCompressionFind(u);
        Node<V> rootV = pathCompressionFind(v);
        if (rootU.equals(rootV)) {
            return true;
        }
        unionByRank(rootU, rootV);
        return false;
    }

    //================================================================================
    // Cycle detection
    //================================================================================

    boolean containsCycle(Graph<V> graph) {
        if (graph.size() < 3 || graph.getEdgeCount() < 3) return false;
        init();
        HashSet<Node<V>> set =  new HashSet<Node<V>>();
        for (Node<V> v : graph.getNodes()) {
            v.resetAlgorithmAttributes(runID);
            if (detectCycleDFS(v, null, set)) {
                init();
                return true;
            }
            set.clear();
        }
        return false;
    }

    private boolean detectCycleDFS(Node<V> v, Node<V> parent, Set<Node<V>> recursiveStack) {
        v.visited = true;
        recursiveStack.add(v);
        int n = v.outEdges.size();
        for (int i = 0; i < n; i++) {
            Connection<V> e = v.outEdges.get(i);
            Node<V> u = e.b;
            if (!graph.isDirected() && u.equals(parent)) continue;
            u.resetAlgorithmAttributes(runID);
            if (recursiveStack.contains(u)) {
                return true;
            }
            if (!u.visited) {
                if (detectCycleDFS(u, v, recursiveStack)) return true;
            }
        }
        recursiveStack.remove(v);
        return false;
    }
}

