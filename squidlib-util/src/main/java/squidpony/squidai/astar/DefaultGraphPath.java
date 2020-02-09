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

/** Default implementation of a {@link GraphPath} that extends {@link ArrayList} to store nodes or connections.
 * 
 * @param <N> Type of node
 * 
 * @author davebaol */
public class DefaultGraphPath<N> extends ArrayList<N> implements GraphPath<N> {
	/** Creates a {@code DefaultGraphPath} with no nodes. */
	public DefaultGraphPath () {
		super();
	}

	/** Creates a {@code DefaultGraphPath} with the given capacity and no nodes. */
	public DefaultGraphPath (int capacity) {
		super(capacity);
	}

	/** Creates a {@code DefaultGraphPath} with the given nodes. */
	public DefaultGraphPath (ArrayList<N> nodes) {
		super(nodes);
	}

	@Override
	public void reverse () {
		Collections.reverse(this);
	}
}
