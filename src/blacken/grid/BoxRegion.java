/* blacken - a library for Roguelike games
 * Copyright © 2010-2012 Steven Black <yam655@gmail.com>
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
 * This is a box-like region.
 * 
 * @author Steven Black
 */
public class BoxRegion implements Regionlike {

    /**
     * Helper function to perform a simple box-check to see if a position is in
     * a region.
     * 
     * @param self region to check
     * @param y coordinate
     * @param x coordinate
     * @return whether the coordinate is in the region or not
     */
    public static boolean contains(Regionlike self, int y, int x) {
        if (x >= self.getX() && x < self.getX() + self.getWidth()) {
            if (y >= self.getY() && y < self.getY() + self.getHeight()) {
                return true;
            }
        }
        return false;
    }
    /**
     * Helper function to perform a simple box-check to see if a box is in
     * a region.
     * 
     * @param self region to check
     * @param height height
     * @param width width
     * @param y1 upper right Y position
     * @param x1 upper right X position
     * @return whether the box is in the region or not
     */
    static public boolean contains(Regionlike self, 
                                   int height, int width, int y1, int x1) {
        boolean ret = true;
        int y2 = y1 + height -1;
        int x2 = x1 + width -1;
        out:
            for (int y = y1; y <= y2; y++) {
                for (int x = x1; x <= x2; x++) {
                    if (!self.contains(y, x)) {
                        ret = false;
                        break out;
                    }
                }
            }
            return ret;
    }
    
    /**
     * Helper function to perform a simple box-check to see if a region is in
     * a region.
     * 
     * @param self container region to check
     * @param r contained region to check
     * @return whether the box is in the region or not
     */
    public static boolean contains(Regionlike self, Regionlike r) {
        boolean ret = true;
        RegionIterator edge = r.getEdgeIterator();
        int[] p = new int[4];
        boolean[] pattern;
        int segtype;
        
    all_out:
        while (!edge.isDone()) {
            segtype = edge.currentSegment(p);
            if (segtype == RegionIterator.SEG_BORDER_SOLID || 
                    segtype == RegionIterator.SEG_INSIDE_SOLID) {
                for (int y = p[0]; y <= p[2]; y++) {
                    for (int x = p[1]; x <= p[3]; x++) {
                        if (!self.contains(y, x)) {
                            ret = false;
                            break all_out;
                        }
                    }
                }
            } else if (segtype == RegionIterator.SEG_BORDER_PATTERNED || 
                    segtype == RegionIterator.SEG_INSIDE_PATTERNED) {
                int pidx = -1;
                pattern = edge.currentPattern();
                for (int y = p[0]; y <= p[2]; y++) {
                    for (int x = p[1]; x <= p[3]; x++) {
                        pidx++;
                        if (pidx >= pattern.length) pidx = 0;
                        if (!pattern[pidx]) continue;
                        if (!self.contains(y, x)) {
                            ret = false;
                            break all_out;
                        }
                    }
                }
            } else if (segtype == RegionIterator.SEG_COMPLETE) {
                // should never happen, but just in case...
                break;
            }
            edge.next();
        }
        return ret;
    }

    /**
     * Check to see if a box intersects a region.
     * 
     * <p>To intersect, there must be parts both inside the region as well as 
     * outside.</p>
     * 
     * @param self region to check
     * @param height height
     * @param width width
     * @param y1 upper right Y position
     * @param x1 upper right X position
     * @return whether the box intersects the region or not
     */
    static public boolean intersects(Regionlike self, 
                                     int height, int width, int y1, int x1) {
        boolean does_contain = false;
        boolean does_not_contain = false;
        int y2 = y1 + height -1;
        int x2 = x1 + width -1;
    out:
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (self.contains(y, x)) {
                    does_contain = true;
                    if (does_not_contain) break out;
                } else {
                    does_not_contain = true;
                    if (does_contain) break out;
                }
            }
        }
        return does_contain && does_not_contain;
    }

    /**
     * Helper function to perform a simple box-check to see if a region 
     * intersects a region.
     * 
     * @param self container region to check
     * @param room contained region to check
     * @return whether the box is in the region or not
     */
    public static boolean intersects(Regionlike self, Regionlike room) {
        boolean does_contain = false;
        boolean does_not_contain = false;
        RegionIterator edge = room.getNotOutsideIterator();
        int[] p = new int[4];
        boolean[] pattern;
        int segtype;
        
    all_out:
        while (!edge.isDone()) {
            segtype = edge.currentSegment(p);
            if (segtype == RegionIterator.SEG_BORDER_SOLID || 
                    segtype == RegionIterator.SEG_INSIDE_SOLID) {
                for (int y = p[0]; y <= p[2]; y++) {
                    for (int x = p[1]; x <= p[3]; x++) {
                        if (self.contains(y, x)) {
                            does_contain = true;
                            if (does_not_contain) break all_out;
                        } else {
                            does_not_contain = true;
                            if (does_contain) break all_out;
                        }
                    }
                }
            } else if (segtype == RegionIterator.SEG_BORDER_PATTERNED || 
                    segtype == RegionIterator.SEG_INSIDE_PATTERNED) {
                int pidx = -1;
                pattern = edge.currentPattern();
                for (int y = p[0]; y <= p[2]; y++) {
                    for (int x = p[1]; x <= p[3]; x++) {
                        pidx++;
                        if (pidx >= pattern.length) pidx = 0;
                        if (!pattern[pidx]) continue;
                        if (self.contains(y, x)) {
                            does_contain = true;
                            if (does_not_contain) break all_out;
                        } else {
                            does_not_contain = true;
                            if (does_contain) break all_out;
                        }
                    }
                }
            } else if (segtype == RegionIterator.SEG_COMPLETE) {
                // should never happen, but just in case...
                break;
            }
            edge.next();
        }
        return does_contain && does_not_contain;
    }
    
    private int size_y;
    private int size_x;
    private int start_x;
    private int start_y;

    public BoxRegion(Regionlike region) {
        this.size_x = region.getWidth();
        this.size_y = region.getHeight();
        this.start_x = region.getX();
        this.start_y = region.getY();
    }
    
    /**
     * Create a simple box region.
     * 
     * <p>The starting Y, X coords are 0, 0.</p>
     * 
     * @param height height of box
     * @param width width of box
     */
    public BoxRegion(int height, int width) {
        start_x = 0;
        start_y = 0;
        this.size_y = height;
        this.size_x = width;
    }

    /**
     * Convert a Positionable in to a Regionlike.
     *
     * <p>The position is taken from the Positionable.
     * The size (both y and x) is set to 1.</p>
     *
     * @param point
     */
    public BoxRegion(Positionable point) {
        this.start_x = point.getX();
        this.start_y = point.getY();
        this.size_x = 1;
        this.size_y = 1;
    }

    /**
     * Create a box region.
     * 
     * @param height height of box
     * @param width width of box
     * @param y1 starting Y coordinate
     * @param x1 starting X coordinate
     */
    public BoxRegion(int height, int width, int y1, int x1) {
        this.start_x = x1;
        this.start_y = y1;
        this.size_y = height;
        this.size_x = width;
    }

    @Override
    public boolean contains(int y, int x) {
        return contains(this, y, x);
    }

    @Override
    public boolean contains(int height, int width, int y1, int x1) {
        return BoxRegion.contains(this, height, width, y1, x1);
    }

    @Override
    public boolean contains(Positionable p) {
        return contains(p.getY(), p.getX());
    }

    @Override
    public boolean contains(Regionlike r) {
        return contains(this, r);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BoxRegion)) {
            return false;
        }
        BoxRegion b = (BoxRegion)other;
        if (this.size_x != b.size_x) {
            return false;
        }
        if (this.size_y != b.size_y) {
            return false;
        }
        if (this.start_x != b.start_x) {
            return false;
        }
        if (this.start_y != b.start_y) {
            return false;
        }
        return true;
    }

    @Override
    public Regionlike getBounds() {
        return new BoxRegion(this);
    }

    @Override
    public RegionIterator getEdgeIterator() {
        RegionIterator ret = new BoxRegionIterator(this, true, false);
        return ret;
    }

    @Override
    public int getHeight() {
        return this.size_y;
    }

    @Override
    public RegionIterator getInsideIterator() {
        RegionIterator ret = new BoxRegionIterator(this, false, false);
        return ret;
    }

    @Override
    public RegionIterator getNotOutsideIterator() {
        RegionIterator ret = new BoxRegionIterator(this, false, true);
        return ret;
    }

    @Override
    public int getWidth() {
        return this.size_x;
    }

    @Override
    public int getX() {
        return start_x;
    }

    @Override
    public int getY() {
        return start_y;
    }

    @Override
    public boolean intersects(int height, int width, int y1, int x1) {
        return BoxRegion.intersects(this, height, width, y1, x1);
    }

    @Override
    public int hashCode() {
        int ret = 1;
        ret = ret * 31 + start_y;
        ret = ret * 31 + start_x;
        ret = ret * 31 + size_y;
        ret = ret * 31 + size_x;
        return ret;
    }

    @Override
    public boolean intersects(Regionlike room) {
        return intersects(this, room);
    }

    @Override
    public void setBounds(int height, int width, int y1, int x1) {
        this.setPosition(y1, x1);
        this.setSize(height, width);
    }

    @Override
    public void setBounds(Regionlike r) {
        this.setSize(r);
        this.setPosition(r);
    }

    @Override
    public void setHeight(int height) {
        this.size_y = height;
    }
    
    @Override
    public void setPosition(int y, int x) {
        setY(y);
        setX(x);
    }
    
    @Override
    public void setWidth(int width) {
        this.size_x = width;
    }
    
    @Override
    public void setX(int x) {
        start_x = x;
    }
    
    @Override
    public void setY(int y) {
        start_y = y;
    }

    @Override
    public void setPosition(Positionable point) {
        this.setX(point.getX());
        this.setY(point.getY());
    }

    @Override
    public void setSize(int height, int width) {
        this.setHeight(height);
        this.setWidth(width);        
    }

    @Override
    public void setSize(Sizable size) {
        this.setHeight(size.getHeight());
        this.setWidth(size.getWidth());
    }

    @Override
    public Positionable getPosition() {
        return new Point(this);
    }

    @Override
    public Sizable getSize() {
        return new SimpleSize(this);
    }

    @Override
    public String toString() {
        return String.format("Position: %s,%s; Size: %s,%s", this.start_y,
                this.start_x, this.size_y, this.size_x);
    }
}
