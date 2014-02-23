/* blacken - a library for Roguelike games
 * Copyright © 2011, 2012 Steven Black <yam655@gmail.com>
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
 * This is an implementation of Sizable so it can be returned by things.
 * 
 * @author Steven Black
 */
public class SimpleSize implements Sizable {
    private int height;
    private int width;
    /**
     * Crate a new simple size
     * 
     * @param size_y the height
     * @param size_x the width
     */
    public SimpleSize(int size_y, int size_x) {
        this.height = size_y;
        this.width = size_x;
    }

    public SimpleSize(Sizable size) {
        this.height = size.getHeight();
        this.width = size.getWidth();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleSize other = (SimpleSize) obj;
        if (this.getHeight() != other.getHeight()) {
            return false;
        }
        if (this.getWidth() != other.getWidth()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.height;
        hash = 29 * hash + this.width;
        return hash;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
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
    public void setSize(int height, int width) {
        this.height = height;
        this.width = width;
    }

    @Override
    public void setSize(Sizable size) {
        this.height = size.getHeight();
        this.width = size.getWidth();
    }

    @Override
    public String toString() {
        return String.format("Size:(h: %s, w: %s)", height, width);
    }

    @Override
    public Sizable getSize() {
        return new SimpleSize(this);
    }
}
