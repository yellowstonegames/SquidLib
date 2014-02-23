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
 * A positional interface.
 * 
 * @author yam655
 */
public interface Positionable {
    /**
     * Get the X coordinate
     * @return the coordinate
     */
    public int getX();
    /**
     * Get the Y coordinate
     * @return the coordinate
     */
    public int getY();
    /**
     * Get a copy of the position in a concise form
     * @return the position
     */
    public Positionable getPosition();
    /**
     * Set the X coordinate
     * @param x the coordinate
     */
    public void setX(int x);
    /**
     * Set the Y coordinate
     * @param y the coordinate
     */
    public void setY(int y);
    /**
     * Set the position.
     * 
     * @param y new coordinate
     * @param x new coordinate
     */
    public void setPosition(int y, int x);
    /**
     * Set the position
     * @param point new point
     */
    public void setPosition(Positionable point);
}
