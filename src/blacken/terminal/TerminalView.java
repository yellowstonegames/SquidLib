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

package com.googlecode.blacken.terminal;

import java.util.EnumSet;

import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.RegionIterator;
import com.googlecode.blacken.grid.Regionlike;

/**
 * This provides an read/write view to another TerminalInterface.
 * 
 * <p>The idea is that you can pass another function a smaller window in to
 * your screen and know that it will not step on your display.
 *
 * @author Steven Black
 * @since 1.1
 */
public class TerminalView implements TerminalViewInterface {
    private TerminalViewInterface term;
    private Regionlike bounds;
    private TerminalCellLike empty = null;

    public TerminalView(TerminalViewInterface term) {
        this.term = term;
        bounds = term.getBounds();
    }
    /**
     * Alas, this implementation currently does not work for non-rectangular
     * regions. It only uses the boxed bounds of the region.
     *
     * @param term
     * @param bounds
     * @param subregion
     */
    public TerminalView(TerminalViewInterface term, Regionlike bounds) {
        this.term = term;
        if (term.getBounds().contains(bounds)) {
            this.bounds = bounds;
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", bounds, term.getBounds()));
        }
    }
    public TerminalView(TerminalViewInterface term, int rows, int cols, int y1, int x1) {
        this.term = term;
        BoxRegion nBounds = new BoxRegion(rows, cols, y1, x1);
        if (term.getBounds().contains(nBounds)) {
            this.bounds = nBounds;
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", nBounds, term.getBounds()));
        }
    }

    @Override
    public TerminalCellLike assign(int y, int x, TerminalCellLike cell) {
        if (bounds.contains(y, x)) {
            return term.assign(y, x, cell);
        }
        throw new IndexOutOfBoundsException(String.format("%s, %s not within %s", y, x, bounds));
    }

    @Override
    public void clear() {
        if (empty == null) {
            clear(term.getEmpty());
        } else {
            clear(empty);
        }
    }

    @Override
    public void clear(TerminalCellLike empty) {
        int[] coords = {0,0,0,0};
        setEmpty(empty);
        if (empty == null) {
            empty = term.getEmpty();
        }
        RegionIterator itr = bounds.getInsideIterator();
        while(!itr.isDone()) {
            int segment = itr.currentSegment(coords);
            int patIdx = 0;
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
                        if (state) {
                            term.set(y1, x1, empty);
                        }
                        if (segment == RegionIterator.SEG_INSIDE_PATTERNED) {
                            state = ptrn[patIdx++];
                            if (patIdx >= ptrn.length) {
                                patIdx = 0;
                            }
                        }
                    }
                }
            } else if (segment == RegionIterator.SEG_COMPLETE) {
                break;
            } else {
                throw new UnsupportedOperationException("Please implement");
            }
        }
        itr = bounds.getEdgeIterator();
        while(!itr.isDone()) {
            int segment = itr.currentSegment(coords);
            itr.next();
            boolean[] ptrn = itr.currentPattern();
            boolean isHorizontal = coords[2] == coords[0] ? true : false;
            int direction = isHorizontal ? (coords[3] > coords[1] ? +1 : -1) : (coords[2] > coords[0] ? +1 : -1);
            int count = isHorizontal ? coords[3] - coords[1] : coords[2] - coords[0];
            if (count < 0) {
                count *= -1;
            }
            count++;
            int x0 = coords[1];
            int y0 = coords[0];
            int patIdx = 0;
            boolean state = true;
            if (segment == RegionIterator.SEG_INSIDE_PATTERNED ||
                    segment == RegionIterator.SEG_BORDER_PATTERNED) {
                state = ptrn[patIdx++];
                if (patIdx >= ptrn.length) {
                    patIdx = 0;
                }
            }
            while(count-- > 0) {
                if (state) {
                    term.set(y0, x0, empty);
                }
                if (isHorizontal) {
                    x0+=direction;
                } else {
                    y0+=direction;
                }
                if (segment == RegionIterator.SEG_INSIDE_PATTERNED ||
                        segment == RegionIterator.SEG_BORDER_PATTERNED) {
                    state = ptrn[patIdx++];
                    if (patIdx >= ptrn.length) {
                        patIdx = 0;
                    }
                }
            }
        }
    }

    @Override
    public void copyFrom(TerminalViewInterface oterm, int numRows, int numCols, int startY, int startX, int destY, int destX) {
        BoxRegion targetRegion = new BoxRegion(numRows, numCols, destY, destX);
        if (bounds.contains(targetRegion)) {
            term.copyFrom(oterm, numRows, numCols, startY, startX, destY, destX);
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", targetRegion, bounds));
        }
    }

    @Override
    public TerminalCellLike get(int y, int x) {
        if (bounds.contains(y, x)) {
            return term.get(y, x);
        }
        throw new IndexOutOfBoundsException(String.format("%s, %s not within %s", y, x, bounds));
    }

    @Override
    public int getch() {
        return term.getch();
    }

    @Override
    public int getch(int millis) {
        return term.getch(millis);
    }

    @Override
    public boolean keyWaiting() {
        return term.keyWaiting();
    }

    @Override
    public Regionlike getBounds() {
        return new BoxRegion(bounds);
    }

    @Override
    public Positionable getCursorPosition() {
        if (bounds.contains(term.getCursorPosition())) {
            return term.getCursorPosition();
        }
        return null;
    }

    @Override
    public int getCursorX() {
        if (bounds.contains(term.getCursorPosition())) {
            return term.getCursorX();
        }
        return -1;
    }

    @Override
    public int getCursorY() {
        if (bounds.contains(term.getCursorPosition())) {
            return term.getCursorY();
        }
        return -1;
    }

    @Override
    public TerminalCellLike getEmpty() {
        if (this.empty == null) {
            empty = term.getEmpty().clone();
        }
        return empty;
    }

    @Override
    public int getHeight() {
        return bounds.getHeight();
    }

    @Override
    public Grid<TerminalCellLike> getGrid() {
        return term.getGrid();
    }

    @Override
    public EnumSet<BlackenModifier> getLockingStates() {
        return term.getLockingStates();
    }

    @Override
    public BlackenMouseEvent getmouse() {
        return term.getmouse();
    }

    @Override
    public String getString(int y, int x, int length) {
        if (bounds.contains(y, x) && bounds.contains(y, x + length)) {
            return term.getString(y, x, length);
        }
        throw new IndexOutOfBoundsException(String.format("%s, %s not within %s", y, x, bounds));
    }

    @Override
    public BlackenWindowEvent getwindow() {
        return term.getwindow();
    }

    @Override
    public int getWidth() {
        return bounds.getWidth();
    }

    @Override
    public void moveBlock(int numRows, int numCols, int origY, int origX, int newY, int newX) {
        if (bounds.contains(numRows, numCols, newY, newX)
                && bounds.contains(numRows, numCols, origY, origX)) {
            term.moveBlock(numRows, numCols, origY, origX, newY, newX);
        } else {
            throw new IndexOutOfBoundsException(String.format("arguments not within %s", bounds));
        }
    }

    @Override
    public void refresh() {
        Grid<TerminalCellLike> grid = getGrid();
        for (int y = bounds.getY(); y < bounds.getHeight() + bounds.getY(); y++) {
            for (int x = bounds.getX(); x < bounds.getWidth() + bounds.getX(); x++) {
                refresh(y, x);
            }
        }
        term.doUpdate();
    }

    public void refresh(int y, int x) {
        if (bounds.contains(y, x)) {
            term.refresh(y, x);
        } else {
            throw new IndexOutOfBoundsException(String.format("%s, %s not within %s", y, x, bounds));
        }
    }


    @Override
    public void set(int y, int x, String sequence, Integer foreground, Integer background, EnumSet<TerminalStyle> style, EnumSet<CellWalls> walls) {
        if (bounds.contains(y, x)) {
            term.set(y, x, sequence, foreground, background, style, walls);
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", new int[] {y, x}, term.getBounds()));
        }
    }

    @Override
    public void set(int y, int x, String sequence, Integer foreground, Integer background) {
        if (bounds.contains(y, x)) {
            term.set(y, x, sequence, foreground, background);
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", new int[] {y, x}, term.getBounds()));
        }
    }

    @Override
    public void set(int y, int x, TerminalCellLike cell) {
        if (bounds.contains(y, x)) {
            term.set(y, x, cell);
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", new int[] {y, x}, term.getBounds()));
        }
    }

    @Override
    public void setBounds(Regionlike bounds) {
        if (term.getBounds().contains(bounds)) {
            this.bounds = bounds;
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", bounds, term.getBounds()));
        }
    }

    @Override
    public void setBounds(int rows, int cols, int y1, int x1) {
        BoxRegion nBounds = new BoxRegion(rows, cols, y1, x1);
        if (term.getBounds().contains(nBounds)) {
            this.bounds = nBounds;
        } else {
            throw new IndexOutOfBoundsException(String.format("%s not within %s", nBounds, term.getBounds()));
        }
    }

    @Override
    public void setCursorLocation(int y, int x) {
        if (bounds.contains(y, x)) {
            term.setCursorLocation(y, x);
        }
    }

    @Override
    public void setCursorPosition(Positionable position) {
        if (bounds.contains(position)) {
            term.setCursorPosition(position);
        }
    }

    @Override
    public void setEmpty(TerminalCellLike empty) {
        this.empty = empty;
    }

    @Override
    public TerminalInterface getBackingTerminal() {
        return term.getBackingTerminal();
    }

    @Override
    public TerminalViewInterface getBackingTerminalView() {
        return term.getBackingTerminalView();
    }

    @Override
    public void doUpdate() {
        term.doUpdate();
    }
}
