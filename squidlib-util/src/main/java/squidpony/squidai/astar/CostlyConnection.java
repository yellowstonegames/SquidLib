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

/** A {@code CostlyConnection} is a {@link Connection} that can have different costs for different instances.
 * These are often used on a {@link DefaultGraph} with {@link squidpony.squidmath.Coord} as {@code N}.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public class CostlyConnection<N> implements Connection<N> {

	protected N fromNode;
	protected N toNode;
	protected double cost;

	public CostlyConnection(N fromNode, N toNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		cost = 1.0;
	}

	public CostlyConnection(N fromNode, N toNode, double cost) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.cost = cost;
	}

	@Override
	public double getCost () {
		return cost;
	}

	@Override
	public N getFromNode () {
		return fromNode;
	}

	@Override
	public N getToNode () {
		return toNode;
	}

}
