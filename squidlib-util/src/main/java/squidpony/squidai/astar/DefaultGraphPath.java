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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/** Default implementation of a {@link GraphPath} that uses an internal {@link ArrayList} to store nodes or connections.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public class DefaultGraphPath<N> implements GraphPath<N> {
	public final ArrayList<N> nodes;

	/** Creates a {@code DefaultGraphPath} with no nodes. */
	public DefaultGraphPath () {
		this(new ArrayList<N>());
	}

	/** Creates a {@code DefaultGraphPath} with the given capacity and no nodes. */
	public DefaultGraphPath (int capacity) {
		this(new ArrayList<N>(capacity));
	}

	/** Creates a {@code DefaultGraphPath} with the given nodes. */
	public DefaultGraphPath (ArrayList<N> nodes) {
		this.nodes = nodes;
	}

	@Override
	public void clear () {
		nodes.clear();
	}

	@Override
	public int getCount () {
		return nodes.size();
	}

	@Override
	public void add (N node) {
		nodes.add(node);
	}

	@Override
	public N get (int index) {
		return nodes.get(index);
	}

	@Override
	public void reverse () {
		Collections.reverse(nodes);
	}

	@Override
	public Iterator<N> iterator () {
		return nodes.iterator();
	}
}
