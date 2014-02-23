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
 * This primarily exists as a concrete type usable to test Positionable.
 * 
 * @author Steven Black
 *
 */
public class Point implements Positionable {

    private int x;
    private int y;

    /**
     * Get the center of a boxed region
     * @param region
     * @return the point at the center of the region
     * @since Blacken 1.1
     */
    public static Point centerOfRegion(Regionlike region) {
        int y = region.getY() + region.getHeight() / 2;
        int x = region.getX() + region.getWidth() / 2;
        return new Point(y, x);
    }

    /**
     * A point
     */
    public Point() {
        x = 0;
        y = 0;
    }

    /**
     * A point with a value.
     * @param y coordinate
     * @param x coordinate
     */
    public Point(int y, int x) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * @param point a point to base this one off of
     */
    public Point(Positionable point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
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
    public void setPosition(int y, int x) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void setPosition(Positionable point) {
        this.setX(point.getX());
        this.setY(point.getY());
    }

    @Override
    public String toString() {
        return String.format("Point:(y=%s, x=%s)",
                             this.getY(), this.getX());
    }

    @Override
    public Positionable getPosition() {
        return new Point(this);
    }

    public static boolean samePosition(Positionable p1, Positionable p2) {
        if (p1.getY() != p2.getY()) {
            return false;
        }
        if (p1.getX() != p2.getX()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object point) {
        if (this == point) {
            return true;
        }
        if (!(point instanceof Positionable)) {
            return false;
        }
        Positionable pos = (Positionable)point;
        if (this.getY() != pos.getY()) {
            return false;
        }
        if (this.getX() != pos.getX()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.x;
        hash = 37 * hash + this.y;
        return hash;
    }

    /**
     * Add two positionables together.
     *
     * <p>Typically, one will be a real point and the other will be an offset.</p>
     *
     * @param a
     * @param b
     * @return new positionable
     */
    static public Positionable add(Positionable a, Positionable b) {
        return new Point(a.getY() + b.getY(), a.getX() + b.getX());
    }

    /**
     * Add a positionable to this point.
     * @param b offset to add
     */
    public void add(Positionable b) {
        this.setPosition(getY() + b.getY(), getX() + b.getX());
    }
}
