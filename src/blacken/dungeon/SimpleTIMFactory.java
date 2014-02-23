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
 * A factory to convert simple dungeon cells to terrain-item-monster (TIM) cells.
 * 
 * <p>The underlying logic behind this is a set of {@link SimpleFactorylike} 
 * factories. This includes a convenience function accepting a Map instead of
 * a SimpleFactorylike which -- using {@link SimpleFactoryMap} -- wraps the
 * map automatically for you.
 *
 * @param <D> simple/flat dungeon type
 * @param <T> terrain type
 * @param <I> item type
 * @param <M> monster type
 * @author Steven Black
 */
public class SimpleTIMFactory<D,T,I,M> {
    private SimpleFactorylike<D,T> tFactory = null;
    private SimpleFactorylike<D,I> iFactory = null;
    private SimpleFactorylike<D,M> mFactory = null;
    private int x = 0;
    private int y = 0;
    private int z = 0;
    private int f = 0;

    /**
     * Create a new TIM factory.
     */
    public SimpleTIMFactory() {
        // do nothing
    }

    /**
     * Create and configure a new TIM factory.
     *
     * @param z depth in dungeon (as appropriate)
     * @param y
     * @param x
     * @param flavor generic flavor identifier (no default meaning)
     */

    public SimpleTIMFactory(int z, int y, int x, int flavor) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.f = flavor;
    }

    /**
     * Specify a configuration for the factories to use.
     *
     * @param z depth in dungeon (or location on Z axis)
     * @param y
     * @param x
     * @param flavor generic flavor identifier (no default meaning)
     */
    public void setConfiguration(int z, int y, int x, int flavor) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.f = flavor;
        if (tFactory != null) {
            tFactory.setConfiguration(z, y, x, flavor);
        }
        if (mFactory != null) {
            mFactory.setConfiguration(z, y, x, flavor);
        }
        if (iFactory != null) {
            iFactory.setConfiguration(z, y, x, flavor);
        }
    }

    /**
     * Get the current terrain factory.
     * @return null
     */
    public SimpleFactorylike<D, T> getTerrainFactory() {
        return tFactory;
    }

    /**
     * Convenience to set a simple map-based factory.
     * 
     * <p>Note that this calls {@link SimpleFactoryMap} and as such the items
     * returned will be references to the items stored in the map and not 
     * clones or other copies.
     * 
     * @param tMap mapping to use
     */
    public void setTerrainFactory(Map<D, T> tMap) {
        this.tFactory = new SimpleFactoryMap<>(tMap);
    }

    /**
     * Set the terrain factory.
     * @param tFactory
     */
    public void setTerrainFactory(SimpleFactorylike<D, T> tFactory) {
        tFactory.setConfiguration(z, y, x, f);
        this.tFactory = tFactory;
    }

    /**
     * Get the current monster factory.
     * @return
     */
    public SimpleFactorylike<D, M> getMonsterFactory() {
        return mFactory;
    }

    /**
     * Set the monster factory.
     * @param mFactory null clears the setting
     */
    public void setMonsterFactory(SimpleFactorylike<D, M> mFactory) {
        mFactory.setConfiguration(z, y, x, f);
        this.mFactory = mFactory;
    }

    /**
     * Get the current item factory.
     * @return
     */
    public SimpleFactorylike<D, I> getItemFactory() {
        return iFactory;
    }

    /**
     * Set the item factory.
     *
     * @param iFactory null clears the setting
     */
    public void setItemFactory(SimpleFactorylike<D, I> iFactory) {
        if (iFactory != null) {
            iFactory.setConfiguration(z, y, x, f);
        }
        this.iFactory = iFactory;
    }

    public TIMCell<T, I, M> create(D what) {
        T terrain = null;
        I item = null;
        M monster = null;
        if (tFactory != null) {
            terrain = tFactory.create(what);
        }
        if (iFactory != null) {
            item = iFactory.create(what);
        }
        if (mFactory != null) {
            monster = mFactory.create(what);
        }
        return new TIMCell<>(terrain, item, monster);
    }
}
