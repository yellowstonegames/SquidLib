/* blacken - a library for Roguelike games
 * Copyright © 2011 Steven Black <yam655@gmail.com>
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
 * @author yam655
 *
 */
public interface Sizable {
    /**
     * Get the height
     * @return height
     */
    public int getHeight();
    /**
     * Get the width.
     * @return width
     */
    public int getWidth();
    /**
     * Get a concise copy of the size
     * @return size
     */
    public Sizable getSize();
    /**
     * Set the height.
     * @param height the height
     */
    public void setHeight(int height);
    /**
     * Set the width.
     * @param width the width
     */
    public void setWidth(int width);
    /**
     * Set the size using a width and a height
     * @param height the height
     * @param width the width
     */
    public void setSize(int height, int width);
    /**
     * Set the size, using another Sizable object
     * @param size the size
     */
    public void setSize(Sizable size);
}
