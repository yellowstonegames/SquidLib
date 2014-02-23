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
 * A region iterator.
 * 
 * @author Steven Black
 */
public abstract class RegionIterator {
    // XXX Should be easy to make this a real Iterator.
    /**
     * This indicates you have seen all the segments.
     * 
     * The p1,p2 locations will be undefined.
     */
    public static final int SEG_COMPLETE = -1;
    /**
     * A solid line segment in cells that are border cells.
     * 
     * When a region is bordered every cell in the region can belong to one of
     * three states: outside, inside, or border. A border cell is neither inside
     * the region nor is it outside the region.
     */
    public static final int SEG_BORDER_SOLID = 0;
    /**
     * A patterned line segment in cells that are border cells.
     * 
     * Patterned cells have an repeating array of booleans indicating whether 
     * the cells in the line segment belong or not. To get this pattern you 
     * must call {@link #currentPattern()}.
     * 
     * @see #currentPattern()
     */
    public static final int SEG_BORDER_PATTERNED = 1;
    /**
     * An solid line segment in cells that is inside a region.
     * 
     * Unbordered regions have every cell belonging to one of two states: 
     * inside and outside. This value indicates that the edge cells are also
     * interior cells. Any border that might be visible is zero cells wide.
     * 
     * Again, this means that these cells will also be returned if you ask for
     * inside of a region.
     */
    public static final int SEG_INSIDE_SOLID = 0;
    /**
     * A patterned line segment in cells that that are border cells.
     * 
     * @see #SEG_INSIDE_SOLID
     * @see #SEG_BORDER_PATTERNED
     */
    public static final int SEG_INSIDE_PATTERNED = 1;
    protected RegionIterator() {
        // do nothing
    }
    /**
     * Get the current pattern.
     * 
     * @return the current pattern.
     */
    public abstract boolean[] currentPattern();
    /**
     * Return the current segment.
     * 
     * The return value is the type of segment. All segments are composed of
     * a pair of (y, x) coordinates. All segments are straight lines, so
     * either x1 will equal x2 or y1 will equal y2.
     * 
     * @see #SEG_BORDER_PATTERNED
     * @see #SEG_BORDER_SOLID
     * @see #SEG_INSIDE_PATTERNED
     * @see #SEG_INSIDE_SOLID
     * @param coords int[4] containing y1, x1, y2, x2
     * @return segment type
     */
    public abstract int currentSegment(int[] coords);
    /**
     * Are we done?
     * @return done status
     */
    public abstract boolean isDone();
    /**
     * Move to next segment.
     */
    public abstract void next();
}
