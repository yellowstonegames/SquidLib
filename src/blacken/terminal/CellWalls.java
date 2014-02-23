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
package com.googlecode.blacken.terminal;

import java.util.EnumSet;

/**
 * The cell walls.
 * 
 * @author yam655
 */
public enum CellWalls {
    /**
     * The top cell wall
     */
    TOP,
    /**
     * the left cell wall
     */
    LEFT,
    /**
     * The bottom cell wall
     */
    BOTTOM,
    /**
     * The right cell wall
     */
    RIGHT,
    /**
     * Cell wall
     */
    CENTER_TO_TOP,
    /**
     * Cell wall
     */
    CENTER_TO_LEFT,
    /**
     * Cell wall
     */
    CENTER_TO_BOTTOM,
    /**
     * Cell wall
     */
    CENTER_TO_RIGHT;
    /**
     * Cell wall
     */
    public static final EnumSet<CellWalls> HORIZONTAL = 
        EnumSet.of(CENTER_TO_LEFT, CENTER_TO_RIGHT);
    /**
     * Cell wall
     */
    public static final EnumSet<CellWalls> VERTICAL = 
        EnumSet.of(CENTER_TO_TOP, CENTER_TO_BOTTOM);
    /**
     * Box a cell completely
     */
    public static final EnumSet<CellWalls> BOX =
        EnumSet.of(TOP, LEFT, BOTTOM, RIGHT);
}
