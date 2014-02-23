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

/**
 * A simple one to one mapping factory.
 *
 * <p>This functions as a common factory interface for all parts of the
 * TIM Factory.
 *
 * <p>The advantage of an actual class, compared to a simple Map<D, T>
 * is that:
 *
 * <ul>
 * <li>Returned items can be individually instantiated instead of cloned.
 * <li>An in-game location-based configuration can be taken in to account.
 * <li>A single simple type can result in various generated values.
 * </ul>
 *
 * @author Steven Black
 * @param <D> simple dungeon identifier
 * @param <T> returned type
 */
public interface SimpleFactorylike<D, T> {

    /**
     * Create a 'type' for a 'dungeon identifier'.
     *
     * @param value
     * @return
     */
    public T create(D value);

    /**
     * Specify a location-specific configuration should be used.
     * 
     * @param z depth in a particular dungeon (or generic 'z' coordinate)
     * @param y
     * @param x
     * @param flavor flexible flavor identifier (no predefined meaning)
     */

    public void setConfiguration(int z, int y, int x, int flavor);
}
