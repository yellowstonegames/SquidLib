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

/** 
 * A connection between two nodes of the {@link Graph}. The connection has a non-negative cost that often represents
 * time or distance. However, the cost can be anything you want, for instance a combination of time, distance, and other
 * factors. Possible such factors could be how dangerous or unpleasant it seems to go between two nodes, how much it
 * costs to make payments to use toll roads, whether a specific path crosses an international border, and so on.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public interface Connection<N> {

	/** Returns the non-negative cost of this connection */
	double getCost();

	/** Returns the node that this connection came from */
	N getFromNode();

	/** Returns the node that this connection leads to */
	N getToNode();

}
