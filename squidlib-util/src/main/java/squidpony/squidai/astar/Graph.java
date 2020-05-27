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

package squidpony.squidai.astar;

import java.util.List;

/**
 * A graph is a collection of nodes, each one having a collection of outgoing {@link Connection connections}.
 * Because this works with indexed A*, each node must also have a unique index.
 * <br>
 * Many users will be fine with {@link DefaultGraph} as the only kind of Graph they need to use.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public interface Graph<N> {

	/** Returns the connections outgoing from the given node.
	 * @param fromNode the node whose outgoing connections will be returned
	 * @return the array of connections outgoing from the given node. */
	List<Connection<N>> getConnections(N fromNode);
	
	/** Returns the unique index of the given node.
	 * @param node the node whose index will be returned
	 * @return the unique index of the given node. */
	int getIndex(N node);

	/** Returns the number of nodes in this graph. */
	int getNodeCount();

}
