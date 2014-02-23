/* blacken - a library for Roguelike games
 * Copyright © 2010, 2011 Steven Black <yam655@gmail.com>
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
package com.googlecode.blacken.grid;

/**
 * Reset the dirty state of a cell.
 * 
 * @author yam655
 *
 * @param <Z> cell type
 */
public interface DirtyGridCell <Z> {
    /**
     * Dirty a cell.
     * 
     * <p>This is used when cells are copied <em>en masse</em> and they need
     * to be refreshed.</p>
     * 
     * @param cell cell to dirty
     * @param dirty true to dirty; false to clean
     */
    public void setDirty(Z cell, boolean dirty);
}
