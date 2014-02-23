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
 * A simple cell for terrain, item, monster separation.
 *
 * @param <T> terrain type
 * @param <I> item type
 * @param <M> monster type
 * @author Steven Black
 */
public class TIMCell<T, I, M> implements Cloneable {
    private T terrain = null;
    private I item = null;
    private M monster = null;

    /**
     * Create a new empty TIM cell.
     */
    public TIMCell() {
    }

    /**
     * Create a new TIM cell, specifying the three ingredients.
     * @param terrain
     * @param item
     * @param monster
     */
    public TIMCell(T terrain, I item, M monster) {
        this.terrain = terrain;
        this.item = item;
        this.monster = monster;
    }

    /**
     * Create a new TIM cell, based upon an old.
     * @param old
     */
    public TIMCell(TIMCell<T, I, M> old) {
        this.item = old.getItem();
        this.monster = old.getMonster();
        this.terrain = old.getTerrain();
    }

    @Override
    public TIMCell<T,I,M> clone() {
        TIMCell<T,I,M> n = new TIMCell<>(this);
        return n;
    }

    public I getItem() {
        return item;
    }

    public void setItem(I item) {
        this.item = item;
    }

    public M getMonster() {
        return monster;
    }

    public void setMonster(M monster) {
        this.monster = monster;
    }

    public T getTerrain() {
        return terrain;
    }

    public void setTerrain(T terrain) {
        this.terrain = terrain;
    }
}
