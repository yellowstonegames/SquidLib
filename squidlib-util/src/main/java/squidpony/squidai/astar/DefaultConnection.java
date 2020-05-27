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

/** A {@code DefaultConnection} is a {@link Connection} whose cost is 1.
 * These are often used on a {@link DefaultGraph} with {@link squidpony.squidmath.Coord} as {@code N}.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public class DefaultConnection<N> implements Connection<N> {

	protected N fromNode;
	protected N toNode;

	public DefaultConnection (N fromNode, N toNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
	}

	@Override
	public float getCost () {
		return 1f;
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
