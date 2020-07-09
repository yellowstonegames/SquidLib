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

import java.util.List;

@Beta
public class DirectedGraphAlgorithms<V> extends Algorithms<V> {

    DirectedGraphAlgorithms(DirectedGraph<V> graph) {
        super(graph);
    }

    /**
     * Sort the vertices of this graph in topological order. That is, for every edge from vertex u to vertex v, u comes before v in the ordering.
     * This is reflected in the iteration order of the collection returned by {@link Graph#getVertices()}.
     * Note that the graph cannot contain any cycles for a topological order to exist. If a cycle exists, this method will do nothing.
     * @return true if the sort was successful, false if the graph contains a cycle
     */
    public boolean topologicalSort() {
        return implementations.topologicalSort();
    }

    /**
     * Perform a topological sort on the graph, and puts the sorted vertices in the supplied list.
     * That is, for every edge from vertex u to vertex v, u will come before v in the supplied list.
     * Note that the graph cannot contain any cycles for a topological order to exist. If a cycle exists, the sorting procedure will
     * terminate and the supplied list will only contain the vertices up until the point of termination.
     * @return true if the sort was successful, false if the graph contains a cycle
     */
    public boolean topologicalSort(List<V> sortedVertices) {
        return implementations.topologicalSort(sortedVertices);
    }


}
