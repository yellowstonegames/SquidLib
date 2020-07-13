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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * A mutual relative of {@link FibonacciHeap.Entry} that also stores a reference to the parent Graph,
 * a vertex object of type {@code V}, a Map of neighbor Nodes to the appropriate {@link Connection} per Node, an extra
 * List of those same Connections for faster iteration, and a lot of internal data used by algorithms in this package.
 * @param <V> the vertex type; often {@link squidpony.squidmath.Coord}
 * @author earlygrey
 */
public class Node<V> implements Serializable {
    private static final long serialVersionUID = 1L;


    //================================================================================
    // Graph structure related members
    //================================================================================

    protected final Graph<V> graph;
    protected final int idHash;
    protected final V object;
    protected FibonacciHeap.Entry<Node<V>> entry = null;
    protected HashMap<Node<V>, Connection<V>> neighbors = new HashMap<>();
    protected ArrayList<Connection<V>> outEdges = new ArrayList<>(); // List for fast iteration

    //================================================================================
    // Constructor
    //================================================================================

    protected Node(V v, Graph<V> graph) {
        this.object = v;
        this.graph = graph;
        idHash = System.identityHashCode(this);
    }

    //================================================================================
    // Internal methods
    //================================================================================

    protected Connection<V> getEdge(Node<V> v) {
        return neighbors.get(v);
    }

    protected Connection<V> addEdge(Node<V> v, float weight) {
        Connection<V> edge = neighbors.get(v);
        if (edge == null) {
            edge = graph.obtainEdge();
            edge.set(this, v, weight);
            neighbors.put(v, edge);
            outEdges.add(edge);
            return edge;
        } else {
            edge.setWeight(weight);
        }
        return edge;
    }
    protected Connection<V> removeEdge(Node<V> v) {
        Connection<V> edge = neighbors.remove(v);
        if (edge == null) return null;
        // loop backwards to make Graph#removeNode faster
        for (int j = outEdges.size()-1; j >= 0; j--) {
            Connection<V> connection = outEdges.get(j);
            if (connection.equals(edge)) {
                outEdges.remove(j);
                break;
            }
        }
        return edge;
    }

    protected void disconnect() {
        neighbors.clear();
        outEdges.clear();
    }

    //================================================================================
    // Public Methods
    //================================================================================

    public Collection<Connection<V>> getConnections() {
        return outEdges;
    }

    public V getObject() {
        return object;
    }

    //================================================================================
    // Algorithm fields and methods
    //================================================================================

    /**
     * Internal; tracking bit for whether this Node has already been visited during the current algorithm.
     */
    protected boolean visited;
    /**
     * Internal; tracking bit for whether this Node has been checked during the current algorithm.
     */
    protected boolean seen;
    /**
     * Internal; confirmed distance so far to get to this Node from the start.
     */
    protected double distance;
    /**
     * Internal; estimated distance to get from this Node to the goal.
     */
    protected double estimate;
    /**
     * Internal; a reference to the previous Node in a heap.
     */
    protected Node<V> prev;
    /**
     * Internal; a utility field used to store depth in some algorithms.
     */
    protected int i;
    /**
     * Internal; a utility field used to distinguish which algorithm last used this Node.
     */
    protected int lastRunID;

    /**
     * If {@code runID} is not equal to {@link #lastRunID}, this resets the internal fields {@link #visited},
     * {@link #seen}, {@link #distance}, {@link #estimate}, {@link #prev}, and {@link #i}, then sets {@link #lastRunID}
     * to {@code runID}.
     * @param runID an int that identifies which run of an algorithm is currently active
     * @return true if anything was reset, or false if {@code runID} is equal to {@link #lastRunID}
     */
    protected boolean resetAlgorithmAttributes(int runID) {
        if (runID == this.lastRunID) return false;
        visited = false;
        prev = null;
        distance = Float.MAX_VALUE;
        estimate = 0;
        i = 0;
        seen = false;
        this.lastRunID = runID;
        return true;
    }
    
    //================================================================================
    // Misc
    //================================================================================


    @Override
    public boolean equals(Object o) {
        return o == this;
    }


    @Override
    public int hashCode() {
        return idHash;
    }

    @Override
    public String toString() {
        return "["+object+"]";
    }
}
