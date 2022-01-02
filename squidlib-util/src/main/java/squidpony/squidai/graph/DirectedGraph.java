/*
 * Copyright (c) 2020-2022  Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package squidpony.squidai.graph;

import java.io.Serializable;
import java.util.Collection;
/**
 * A kind of {@link Graph} where all connections between vertices are one-way (but a connection may exist that goes from
 * A to B and another connection may go from B to A), and each connection can have a different cost.
 * @see CostlyGraph The CostlyGraph class supports the common case where V is Coord and all costs are based on the cell being entered.
 * @param <V> the vertex type; often {@link squidpony.squidmath.Coord}
 * @author earlygrey
 */
public class DirectedGraph<V> extends Graph<V> implements Serializable {
    private static final long serialVersionUID = 1L;

    final DirectedGraphAlgorithms<V> algorithms;

    //================================================================================
    // Constructors
    //================================================================================

    public DirectedGraph () {
        super();
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    public DirectedGraph (Collection<V> vertices) {
        super(vertices);
        algorithms = new DirectedGraphAlgorithms<>(this);
    }


    //================================================================================
    // Superclass implementations
    //================================================================================

    @Override
    protected Connection<V> obtainEdge() {
        return new Connection.DirectedConnection<>();
    }

    @Override
    protected Graph<V> createNew() {
        return new DirectedGraph<>();
    }

    @Override
    public DirectedGraphAlgorithms<V> algorithms() {
        return algorithms;
    }

}
