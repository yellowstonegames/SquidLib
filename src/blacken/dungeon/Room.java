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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.blacken.core.Random;
import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.BoxRegionIterator;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.RegionIterator;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.grid.SimpleSize;
import com.googlecode.blacken.grid.Sizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic room-like object.
 * 
 * <p>Rooms exist with dimensions and with stuff. Though the room has stuff,
 * the stuff does not have a position other than simply "inside the room." 
 * 
 * <p>For Roguelike games this primarily is useful when creating the dungeon.
 *
 * <p>A room's floor space is considered to consist of zero or more independent
 * layers which can all contain stuff. The implication is that each of the
 * layers can exist independently, however in practice this is rarely the case.
 * We would rarely want initial placement of a monster to be on a terrain which
 * can kill it, or initial placement of an item on terrain which will destroy
 * it.
 *
 * @param <T> item the room can contain
 * @author Steven Black
 */
public class Room<T> implements Regionlike {
    static private final Logger LOGGER = LoggerFactory.getLogger(Room.class);
    protected int height;
    protected int width;
    protected int x;
    protected int y;
    protected Map<String, Containerlike<T>> stuff;
    protected int floorSpace;
    protected Map<String, T> config = Collections.emptyMap();
    protected Random rng = Random.getInstance();
    private boolean isDug = false;
    private Set<Positionable> doors;

    /**
     * Normally called from a factory so the containers can be taken care of.
     * @param region 
     */
    public Room(Regionlike region) {
        this.height = region.getHeight();
        this.width = region.getWidth();
        this.x = region.getX();
        this.y = region.getY();
        floorSpace = (height - 2) * (width - 2);
    }

    /**
     * Get the current floor space
     *
     * <p>The floor space is the inside of the room. This is not the map area
     * the room fills, as the room walls are not zero-width, so a room (like all
     * {@link Regionlike} objects) consists of an inside, an outside, and an
     * edge. The room walls are the edge. The floor space is the inside.
     *
     * @return number of floor squares present
     */
    public int getFloorSpace() {
        return floorSpace;
    }

    /**
     * This will throw IllegalStateException if it can't be resized.
     * @param floorSpace
     */
    public void setFloorSpace(int floorSpace) {
        this.resizeContainers(floorSpace);
    }

    public Containerlike<T> getContainer(String key) {
        return stuff.get(key);
    }
    public List<T> getAllContents() {
        List<T> ret = new ArrayList<>();
        for (Containerlike<T> box : stuff.values()) {
            ret.addAll(box);
        }
        return ret;
    }

    public void assignContainer(String key, Containerlike<T> box) {
        if (stuff == null) {
            stuff = new LinkedHashMap<>();
        }
        stuff.put(key, box);
        if (box.hasSizeLimit()) {
            box.setSizeLimit(floorSpace);
        }
    }

    public void assignToContainer(T thing ){
        boolean done = false;
        for (Containerlike<T> what : stuff.values()) {
            if (what.canFit(thing)) {
                what.add(thing);
                done = true;
                break;
            }
        }
        if (!done) {
            throw new IllegalArgumentException("Cannot fit thing in to any container.");
        }
    }

    /**
     * setSizeLimit will throw IllegalStateException if it can't be resized.
     * @param floorSpace
     */
    private void resizeContainers(int floorSpace) {
        if (floorSpace != -1) {
            this.floorSpace = floorSpace;
        }
        for (Containerlike<T> what : stuff.values()) {
            if (what.hasSizeLimit()) {
                what.setSizeLimit(floorSpace);
            }
        }
    }

    @Override
    public boolean contains(int y, int x) {
        return BoxRegion.contains(this, y, x);
    }

    @Override
    public boolean contains(Positionable p) {
        return contains(p.getY(), p.getX());
    }
    @Override
    public boolean contains(Regionlike r) {
        return BoxRegion.contains(this, r);
    }

    @Override
    public Regionlike getBounds() {
        return this;
    }

    @Override
    public RegionIterator getEdgeIterator() {
        RegionIterator ret = new BoxRegionIterator(this, true, false);
        return ret;
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
    public boolean intersects(Regionlike room) {
        return BoxRegion.intersects(this, room);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
    @Override
    public boolean contains(int height, int width, int y1, int x1) {
        return BoxRegion.contains(this, height, width, y1, x1);
    }

    @Override
    public boolean intersects(int height, int width, int y1, int x1) {
        return BoxRegion.intersects(this, height, width, y1, x1);
    }
    @Override
    public void setHeight(int height) {
        this.height = height;
    }
    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setBounds(Regionlike r) {
        this.setPosition(r);
        this.setSize(r);
    }

    @Override
    public void setBounds(int height, int width, int y, int x) {
        this.setPosition(y, x);
        this.setSize(height, width);
    }

    @Override
    public Positionable getPosition() {
        return new Point(y, x);
    }

    @Override
    public void setPosition(int y, int x) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setPosition(Positionable point) {
        this.y = point.getY();
        this.x = point.getX();
    }

    @Override
    public Sizable getSize() {
        return new SimpleSize(height, width);
    }

    @Override
    public void setSize(int height, int width) {
        this.height = height;
        this.width = width;
    }

    @Override
    public void setSize(Sizable size) {
        this.height = size.getHeight();
        this.width = size.getWidth();
    }

    public void setConfig(Map<String, T> config) {
        this.config = config;
    }

    public Map<String, T> getConfig() {
        return config;
    }

    public Positionable findBestDoorPosition(Regionlike region) {
        return findBestDoorPosition(Point.centerOfRegion(region));
    }

    public Positionable findBestDoorPosition(Point point) {
        int x1 = this.getX();
        int y1 = this.getY();
        int y2 = this.getY() + this.getHeight() -1; // inclusive
        int x2 = this.getX() + this.getWidth() -1;
        int xa = point.getX();
        int ya = point.getY();
        int xd = 0;
        int yd = 0;
        if (xa < x1) {
            xd = xa - x1;
        } else if (xa > x2) {
            xd = xa - x2;
        }
        if (ya < y1) {
            yd = ya - y1;
        } else if (ya > y2) {
            yd = ya - y2;
        }
        if (yd == 0 && xd == 0) {
            // it is already inside.
            return null;
        }
        BoxRegion ret;
        if (Math.abs(yd) > Math.abs(xd)) {
            if (yd > 0) {
                ret = new BoxRegion(y2, x1+1, 1, width-2);
            } else {
                ret = new BoxRegion(y1, x1+1, 1, width-2);
            }
        } else {
            if (xd > 0) {
                ret = new BoxRegion(y1+1, x2, height-2, 1);
            } else {
                ret = new BoxRegion(y1+1, x1, height-2, 1);
            }
        }
        for (Positionable door : this.doors) {
            if (ret.contains(door)) {
                return new BoxRegion(door);
            }
        }
        return Point.centerOfRegion(ret);
    }

    public boolean dig(Grid<T> grid) {
        boolean intr = false;
        int[] coords = {0,0,0,0};
        RegionIterator itr = getInsideIterator();
        int patIdx = 0;
        while(!itr.isDone()) {
            int segment = itr.currentSegment(coords);
            boolean[] ptrn = itr.currentPattern();
            if (segment == RegionIterator.SEG_INSIDE_SOLID || segment == RegionIterator.SEG_INSIDE_PATTERNED) {
                boolean state = true;
                if (segment == RegionIterator.SEG_INSIDE_PATTERNED) {
                    state = ptrn[patIdx++];
                    if (patIdx >= ptrn.length) {
                        patIdx = 0;
                    }
                }
                for(int y1 = coords[0]; y1 <= coords[2]; y1++) {
                    for (int x1 = coords[1]; x1 <= coords[3]; x1++) {
                        T cell = grid.get(y1, x1);
                        if ((cell == config.get("diggable") || (cell != null && cell.equals(config.get("diggable")))) ||
                                (cell == config.get("hall:wall") || (cell != null && cell.equals(config.get("hall:wall"))))) {
                            if (state) {
                                grid.setCopy(y1, x1, config.get("room:floor"));
                            } else {
                                grid.setCopy(y1, x1, config.get("room:wall"));
                            }
                        } else if ((cell == config.get("room:wall") || (cell != null && cell.equals(config.get("room:wall")))) ||
                                (cell == config.get("hall:floor") || (cell != null && cell.equals(config.get("hall:floor"))))) {
                            grid.setCopy(y1, x1, config.get("room:floor"));
                            intr = true;
                        }
                    }
                }
            } else if (segment == RegionIterator.SEG_COMPLETE) {
                break;
            } else {
                throw new UnsupportedOperationException("Please implement");
            }
        }
        for (T something : getAllContents()) {
            this.placeIt(grid, config.get("room:floor"), something);
        }
        itr = getEdgeIterator();
        int side;
        while(!itr.isDone()) {
            itr.currentSegment(coords);
            itr.next();
            //boolean[] ptrn = itr.currentPattern();
            boolean isHorizontal = coords[2] == coords[0] ? true : false;
            int direction = isHorizontal ? (coords[3] > coords[1] ? +1 : -1) : (coords[2] > coords[0] ? +1 : -1);
            int count = isHorizontal ? coords[3] - coords[1] : coords[2] - coords[0];
            if (count < 0) {
                count *= -1;
            }
            count++;
            int x0 = coords[1];
            int y0 = coords[0];
            boolean lastDoor = false;
            boolean lastFloor = false;
            T roomWall;
            //LOGGER.debug("Coords: {}, isHoriz?: {}", coords, isHorizontal);
            if (coords[2] == coords[0] && coords[3] == coords[1]) {
                side = -1;
                roomWall = config.get("room:wall");
            } else if (coords[2] == coords[0]) {
                if (coords[0] == y) {
                    side = 0;
                    roomWall = config.get("room:wall:top");
                } else {
                    roomWall = config.get("room:wall:bottom");
                    side = 2;
                }
            } else {
                if (coords[1] == x) {
                    side = 1;
                    roomWall = config.get("room:wall:left");
                } else {
                    side = 3;
                    roomWall = config.get("room:wall:right");
                }
            }
            boolean firstOrLast = true;
            while(count-- > 0) {
                if (count == 0) {
                    firstOrLast = true;
                }
                //if (!isHorizontal) {
                //    LOGGER.debug("Position: {},{}", y0, x0);
                //}
                T cell = grid.get(y0, x0);
                if ((cell == config.get("diggable") || (cell != null && cell.equals(config.get("diggable")))) ||
                        (cell == config.get("hall:wall") || (cell != null && cell.equals(config.get("hall:wall"))))) {
                    /*
                    if (count == 0) {
                        // cell = config.get("diggable"); // still set
                    } else if (isHorizontal) {
                        cell = grid.get(y0, x0 + direction);
                    } else {
                        cell = grid.get(y0 + direction, x0);
                    }
                    if ((cell == config.get("hall:floor") || (cell != null && cell.equals(config.get("hall:floor")))) ||
                            (cell == config.get("room:floor") || (cell != null && cell.equals(config.get("room:floor")))) ||
                            (cell == config.get("hall:door") || (cell != null && cell.equals(config.get("hall:door")))) ||
                            (cell == config.get("room:door") || (cell != null && cell.equals(config.get("room:door"))))) {
                        if (lastFloor || lastDoor) {
                            grid.setCopy(y0, x0, config.get("room:floor"));
                            if (lastDoor) {
                                Positionable lastPos;
                                if (isHorizontal) {
                                    lastPos = new Point(y0, x0 + (direction*-1));
                                } else {
                                    lastPos = new Point(y0 + (direction*-1), x0);
                                }
                                grid.setCopy(lastPos, config.get("room:floor"));
                                lastFloor = true;
                                lastDoor = false;
                            }
                        } else {
                            grid.setCopy(y0, x0, config.get("room:door"));
                            lastDoor = true;
                        }
                        grid.setCopy(y0, x0, config.get("room:floor"));
                        intr = true;
                    } else {*/
                        // LOGGER.debug("Position: {},{}", y0, x0);
                        if (side == 0 && firstOrLast) {
                            if (count == 0) {
                                grid.setCopy(y0, x0, config.get("room:wall:top-right"));
                            } else {
                                grid.setCopy(y0, x0, config.get("room:wall:top-left"));
                            }
                        } else if (side == 2 && firstOrLast) {
                            if (count == 0) {
                                grid.setCopy(y0, x0, config.get("room:wall:bottom-right"));
                            } else {
                                grid.setCopy(y0, x0, config.get("room:wall:bottom-left"));
                            }
                        } else {
                            grid.setCopy(y0, x0, roomWall);
                        }
                    //}
                } else if ((cell == config.get("hall:floor") || (cell != null && cell.equals(config.get("hall:floor")))) ||
                        (cell == config.get("room:floor") || (cell != null && cell.equals(config.get("room:floor"))))) {
                    if (lastFloor || lastDoor) {
                        grid.setCopy(y0, x0, config.get("room:floor"));
                        if (lastDoor) {
                            Positionable lastPos;
                            if (isHorizontal) {
                                lastPos = new Point(y0, x0 + (direction*-1));
                            } else {
                                lastPos = new Point(y0 + (direction*-1), x0);
                            }
                            grid.setCopy(lastPos, config.get("room:floor"));
                            lastFloor = true;
                            lastDoor = false;
                        }
                    } else {
                        grid.setCopy(y0, x0, config.get("room:door"));
                        lastDoor = true;
                    }
                    intr = true;
                }
                if (isHorizontal) {
                    x0+=direction;
                } else {
                    y0+=direction;
                }
                firstOrLast = false;
            }
        }
        this.isDug = true;
        return intr;

    }

    public Positionable findLocation(Grid<T> grid, Set<T> empties) {
        Positionable placement = null;
        for (int t=0; t < 10 && placement == null; t++) {
            int x1 = rng.nextInt(getWidth()) + getX();
            int y1 = rng.nextInt(getHeight()) + getY();
            T cell = grid.get(y1, x1);
            if (empties.contains(cell)) {
                placement = new Point(y1, x1);
            }
        }
        if (placement == null) {
            Point rowadd;
            Point coladd;
            int x0 = rng.nextInt(getWidth()) + getX();
            int y0 = rng.nextInt(getHeight()) + getY();
            if (rng.nextBoolean()) {
                if (rng.nextBoolean()) {
                    rowadd = new Point(1, 0);
                    coladd = new Point(0, 1);
                } else {
                    rowadd = new Point(-1, 0);
                    coladd = new Point(0, -1);
                }
            } else {
                if (rng.nextBoolean()) {
                    rowadd = new Point(0, 1);
                    coladd = new Point(1, 0);
                } else {
                    rowadd = new Point(0, -1);
                    coladd = new Point(-1, 0);
                }
            }
            Point start = new Point(y0, x0);
            Point cur = new Point(start);
            int y2 = y + height-1;
            int x2 = x + width-1;
            do {
                cur.add(coladd);
                // column wrap
                if (cur.getY() > y2) {
                    cur.add(rowadd);
                    cur.setY(y);
                } else if (cur.getX() > x2) {
                    cur.add(rowadd);
                    cur.setX(x);
                } else if (cur.getY() < y) {
                    cur.add(rowadd);
                    cur.setY(y2);
                } else if (cur.getX() < x) {
                    cur.add(rowadd);
                    cur.setX(x2);
                }
                // row wrap
                if (cur.getY() > y2) {
                    cur.setY(y);
                } else if (cur.getX() > x2) {
                    cur.setX(x);
                } else if (cur.getY() < y) {
                    cur.setY(y2);
                } else if (cur.getX() < x) {
                    cur.setX(x2);
                }
                T cell = grid.get(cur);
                LOGGER.debug("Checking {} in {}", cell, empties);
                if (empties.contains(cell)) {
                    placement = new Point(cur);
                }
            } while(placement == null && !cur.equals(start));
        }
        return placement;
    }

    /**
     * Place a thing and throw an exception if there's not space for it.
     * @param grid
     * @param empty
     * @param what
     * @return location used
     */
    public Positionable placeThing(Grid<T> grid, T empty, T what) {
        if (!isDug) {
            throw new RuntimeException("room must be dug first");
        }
        this.assignToContainer(what);
        return this.placeIt(grid, empty, what);
    }

    /**
     * Place a thing -- slowly if need be -- and return null if impossible.
     * @param grid
     * @param empty
     * @param what
     * @return location used
     */
    private Positionable placeIt(Grid<T> grid, T empty, T what) {
        Set<T> empties = new HashSet<>(1);
        empties.add(empty);

        Positionable placement = this.findLocation(grid, empties);
        if (placement == null) {
            throw new RuntimeException(String.format("It took too long to place. (Found %s; looking for %s)", grid.get(y+1, x+1), empty));
        } else {
            grid.setCopy(placement, what);
        }
        return placement;
    }

    public Random getRandom() {
        return rng;
    }

    public void setRandom(Random rng) {
        this.rng = rng;
    }

    public boolean hasDoor(Positionable p) {
        return this.doors.contains(p);
    }

    public boolean addDoor(Positionable p) {
        if (!this.contains(p)) {
            return false;
        }
        return this.doors.add(new Point(p));
    }

    public boolean addDoor(Positionable door, Grid<T> grid) {
        if (!this.addDoor(door)) {
            return false;
        }
        Set<T> walls = SimpleDungeonConfig.findRoomWalls(config);
        if (this.isDug) {
            T spot = grid.get(door);
            if (walls.contains(spot)) {
                grid.set(door, spot);
            } else if (!spot.equals(config.get("room:door"))) {
                this.doors.remove(door);
                return false;
            }
        }
        return true;
    }
}
