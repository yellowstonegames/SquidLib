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
 * An iterator for box regions.
 * 
 * See {@link BoxRegion} for the region this goes to.
 * 
 * @author yam655
 */
public class BoxRegionIterator extends RegionIterator {

    private Regionlike box;
    private boolean border;
    private boolean unbordered_region;
    int segment;

    /**
     * Create an edge/inside iterator for box-like regions.
     * 
     * @param box region
     * @param border Do we want the border? (or the inside?)
     * @param unbordered_region Is the region unbordered? (no border cells)
     */
    public BoxRegionIterator(Regionlike box, boolean border, boolean unbordered_region) {
        this.box = box;
        this.border = border;
        this.unbordered_region = unbordered_region;
        this.segment = 0;
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.RegionIterator#currentPattern()
     */
    @Override
    public boolean[] currentPattern() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.RegionIterator#currentSegment(int[])
     */
    @Override
    public int currentSegment(int[] coords) {
        if (segment == -1) {
            return SEG_COMPLETE;
        }
        int ret;
        int y1, x1;
        y1 = box.getY();
        x1 = box.getX();
        int x2 = x1 + box.getWidth() -1;
        if (border) {
            int y2 = y1 + box.getHeight() -1;
            switch(segment) {
            case 0: 
                coords[0] = y1; coords[2] = y1; 
                coords[1] = x1; coords[3] = x2;
                break;
            case 1:
                coords[0] = y1 +1; coords[2] = y2-1; 
                coords[1] = x1; coords[3] = x1;
                break;
            case 2: 
                coords[0] = y2; coords[2] = y2; 
                coords[1] = x1; coords[3] = x2;
                break;
            case 3:
                coords[0] = y1 +1; coords[2] = y2-1; 
                coords[1] = x2; coords[3] = x2;
                break;
            default:
                // should never get here -- see segment = -1 in case 3.
                return SEG_COMPLETE;
            }
            if (unbordered_region) {
                ret = SEG_INSIDE_SOLID;
            } else {
                ret = SEG_BORDER_SOLID;
            }
        } else {
            int height = box.getHeight();
            int width = box.getWidth();
            ret = SEG_INSIDE_SOLID;
            if (segment == 0) {
                if (unbordered_region) {
                    coords[0] = y1; coords[2] = y1 + height -1; 
                    coords[1] = x1; coords[3] = x1 + width -1;
                } else if (height > 2 && x1 + 1 < x2 - 1){
                    coords[0] = y1 +1; coords[2] = y1 + height -2; 
                    coords[1] = x1 +1; coords[3] = x1 + width -2;
                } else {
                    ret = SEG_COMPLETE;
                }
                segment = -1;
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.RegionIterator#isDone()
     */
    @Override
    public boolean isDone() {
        return segment == -1;
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.RegionIterator#next()
     */
    @Override
    public void next() {
        int y1;
        y1 = box.getY();
        if (border) {
            int y2 = y1 + box.getHeight() -1;
            if (segment >= 3) {
                segment = -1;
            }
            if (segment == 0 && y1 == y2) {
                segment = -1;
            }
            if (y1 + 1 == y2 && (segment == 1 || segment == 3)) segment++;
            if (segment != -1) {
                segment++;
            }
        }
    }

}