/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.googlecode.blacken.dungeon;

import java.util.Map;

/**
 * Wrap a Map in a SimpleFactorylike.
 *
 * <p>While in the general case, a custom SimpleFactorylike will be best, in other
 * cases you can comfortably use an exact copy. These would be cases where you
 * use some sort of when-readonly-clone logic or when you just don't care if
 * you're dealing with the same reference in different locations (such as for
 * some terrain).
 *
 * <p>Note that this class does not clone the values -- it returns an exact
 * reference to them. It is only suitable for simple types and read-only
 * objects.
 *
 * @author Steven Black
 */
public class SimpleFactoryMap<D, T> implements SimpleFactorylike<D, T> {
    private Map<D, T> store = null;

    public SimpleFactoryMap(Map<D, T> backing) {
        this.store = backing;
    }

    @Override
    public T create(D value) {
        return store.get(value);
    }

    @Override
    public void setConfiguration(int z, int y, int x, int flavor) {
        // do nothing
    }

}
