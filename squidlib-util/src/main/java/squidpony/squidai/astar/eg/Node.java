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
import squidpony.squidmath.BinaryHeap;
import squidpony.squidmath.OrderedMap;

import java.util.Collection;

@Beta
public class Node<V> extends BinaryHeap.Node {

    //================================================================================
    // Graph structure related members
    //================================================================================

    final Graph<V> graph;
    final int idHash;
    final V object;

    OrderedMap<Node<V>, Connection<V>> neighbors = new OrderedMap<>(8);

    //================================================================================
    // Constructor
    //================================================================================

    Node(V v, Graph<V> graph) {
        super(0.0);
        this.object = v;
        this.graph = graph;
        idHash = System.identityHashCode(this);
    }

    //================================================================================
    // Internal methods
    //================================================================================

    Connection<V> getEdge(Node<V> v) {
        return neighbors.get(v);
    }

    Connection<V> addEdge(Node<V> v, float weight) {
        Connection<V> edge = neighbors.get(v);
        if (edge == null) {
            edge = graph.obtainEdge();
            edge.set(this, v, weight);
            neighbors.put(v, edge);
            return edge;
        } else {
            edge.setWeight(weight);
        }
        return edge;
    }
    Connection<V> removeEdge(Node<V> v) {
        return neighbors.remove(v); 
    }

    void disconnect() {
        neighbors.clear();
    }

    //================================================================================
    // Public Methods
    //================================================================================

    public Collection<Connection<V>> getConnections() {
        return neighbors.values();
    }

    public V getObject() {
        return object;
    }

    //================================================================================
    // Algorithm fields and methods
    //================================================================================

    //util fields for algorithms, don't store data in them
    boolean visited, seen;
    double distance;
    double estimate;
    Node<V> prev;
    int i, lastRunID;

    boolean resetAlgorithmAttribs(int runID) {
        if (runID == this.lastRunID) return false;
        visited = false;
        prev = null;
        distance = Float.POSITIVE_INFINITY;
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
