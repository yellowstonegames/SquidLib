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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.googlecode.blacken.colors.ColorHelper;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.terminal.editing.SingleLine;

/**
 * An abstract terminal to handle common terminal functions.
 * 
 * @author Steven Black
 */
public abstract class AbstractTerminal implements TerminalInterface {

    private ColorPalette palette = null;
    private Grid<TerminalCellLike> grid = null;
    private TerminalCellLike empty = new TerminalCell();
    private int cursorX = -1;
    private int cursorY = -1;

    /**
     * Create a new abstract terminal
     */
    public AbstractTerminal() {
        super();
    }
    
    @Override
    public void clear() {
        cursorX = -1; cursorY = -1;
        empty.setDirty(false);
        grid.clear(empty);
        empty.setDirty(true);
    }

    @Override
    public void clear(TerminalCellLike empty) {
        this.empty.set(empty);
        clear();
    }
    
    @Override
    public void copyFrom(TerminalViewInterface oterm, int numRows, int numCols, int startY,
            int startX, int destY, int destX) {
        grid.copyFrom(oterm.getGrid(), numRows, numCols, startY, startX, destY, destX,
                new TerminalCell.ResetCell());
    }

    @Override
    public abstract void disableEventNotice(BlackenEventType event);

    @Override
    public abstract void disableEventNotices();

    @Override
    public abstract void enableEventNotice(BlackenEventType event);

    @Override
    public abstract void enableEventNotices(EnumSet<BlackenEventType> events);

    @Override
    public TerminalCellLike get(int y, int x) {
        return grid.get(y, x);
    }

    @Override
    public abstract int getch();

    @Override
    public abstract int getch(int millis);

    @Override
    public abstract boolean keyWaiting();

    @Override
    @Deprecated
    public int[] getCursorLocation() {
        int[] ret = {cursorY, cursorX};
        return ret;
    }

    @Override
    public int getCursorX() {
        return cursorX;
    }

    @Override
    public int getCursorY() {
        return cursorY;
    }

    @Override
    public Regionlike getBounds() {
        if (grid == null) {
            return new BoxRegion(0,0,0,0);
        }
        return new BoxRegion(grid);
    }
    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.terminal.TerminalInterface#getEmpty()
     */
    @Override
    public TerminalCellLike getEmpty() {
        return empty;
    }

    @Override
    public Grid<TerminalCellLike> getGrid() {
        return this.grid;
    }

    @Override
    public abstract EnumSet<BlackenModifier> getLockingStates();

    @Override
    public abstract BlackenMouseEvent getmouse();

    @Override
    public ColorPalette getPalette() {
        return palette;
    }

    @Override
    public String getString(int y, int x, int length) {
        return SingleLine.getString(this, y, x, length, null);
    }

    @Override
    public abstract BlackenWindowEvent getwindow();

    @Override
    public int getHeight() {
        if (grid == null) return 0;
        return grid.getHeight();
    }

    @Override
    public int getWidth() {
        if (grid == null) return 0;
        return grid.getWidth();
    }

    @Override
    @Deprecated
    public int gridHeight() {
        return getHeight();
    }

    @Override
    @Deprecated
    public int gridWidth() {
        return getWidth();
    }
    
    @Override
    public void resize(int rows, int cols) {
        if (this.getCursorX() >= cols || this.getCursorY() >= rows) {
            setCursorLocation(-1,-1);
        }
        getGrid().setSize(rows, cols);
    }

    @Override
    public void init(String name, int rows, int cols) {
        init(name, rows, cols, (TerminalScreenSize)null, (String)null);
    }

    @Override
    public void init(String name, int rows, int cols, TerminalScreenSize size) {
        init(name, rows, cols, size, (String)null);
    }

    @Override
    public void init(String name, int rows, int cols, String... font) {
        init(name, rows, cols, (TerminalScreenSize)null, font);
    }

    /*
    @Override
    public abstract TerminalInterface getGlass();

    @Override
    public TerminalInterface initGlass(int rows, int cols) {
        return initGlass(rows, cols, null);
    }

    @Override
    public abstract TerminalInterface initGlass(int rows, int cols, String font);
    */

    @Override
    public void init(String name, int rows, int cols, TerminalScreenSize size, String... font) {
        if (grid == null) {
            grid = new Grid<>(this.empty, rows, cols);
        } else {
            grid.reset(rows, cols, this.empty);
            
        }
        setCursorLocation(-1,-1);
    }

    @Override
    public void moveBlock(int numRows, int numCols, int origY, int origX, 
                          int newY, int newX) {
        grid.moveBlock(numRows, numCols, origY, origX, newY, newX, 
                       new TerminalCell.ResetCell());
    }

    @Override
    public void quit() {
        /* do nothing */;
    }

    @Override
    public void refresh() {
        Grid<TerminalCellLike> grid = getGrid();
        for (int y = grid.getY(); y < grid.getHeight() + grid.getY(); y++) {
            for (int x = grid.getX(); x < grid.getWidth() + grid.getX(); x++) {
                refresh(y, x);
            }
        }
    }

    @Override
    abstract public void refresh(int y, int x);

    @Override
    public void set(int y, int x, String sequence, 
                    Integer foreground, Integer background, 
                    EnumSet<TerminalStyle> style, EnumSet<CellWalls> walls) {
        TerminalCellLike tcell = grid.get(y,x);
        if (walls != null) {
            tcell.setCellWalls(walls);
        }
        if (style != null) {
            tcell.setStyle(style);
        }
        if (foreground != null) {
            tcell.setForeground(foreground);
        }
        if (background != null) {
            tcell.setBackground(background);
        }
        if (sequence != null) {
            tcell.setSequence(sequence);
        }
        tcell.setDirty(true);
    }

    @Override
    public void setCursorLocation(int y, int x) {
        cursorX = x; cursorY = y;
    }

    @Override
    public void setEmpty(TerminalCellLike empty) {
        this.empty = empty;
    }
    
    @Override
    public void setFont(String font, boolean checkFont) throws FontNotFoundException {
        throw new FontNotFoundException("Fonts unsupported here");
    }
    @Override
    public String setFont(String... font) throws FontNotFoundException {
        FontNotFoundException lastEx = null;
        String used = null;
        for (String f : font) {
            try {
                setFont(f, false);
                used = f;
                break;
            } catch(FontNotFoundException ex) {
                lastEx = ex;
            }
        }
        if (used == null) {
            throw new FontNotFoundException("None of the requested fonts were found", lastEx);
        }
        return used;
    }

    @Override
    @Deprecated
    public ColorPalette setPalette(ColorPalette palette, int white, int black) {
        return this.coerceToPalette(palette, white, black);
    }

    @Override
    public ColorPalette setPalette(ColorPalette palette) {
        ColorPalette oldPalette = this.palette;
        Map<Integer, Integer> inversePalette = null;
        if (palette != null) {
            this.palette = palette;
            inversePalette = new HashMap<>();
            for (int i = 0; i < palette.size(); i++) {
                inversePalette.put(palette.get(i), i);
            }
        } else {
            return oldPalette;
        }
        int psize = palette.size();
        Set<Integer> changedColors = null;
        if (oldPalette != null) {
            changedColors = new HashSet<>();
            for (int c = 0; c < oldPalette.size(); c++) {
                if (c >= palette.size()) {
                    changedColors.add(c);
                } else if (palette.get(c) != oldPalette.get(c)) {
                    changedColors.add(c);
                }
            }
        }
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                TerminalCellLike cell = get(y, x);
                int b = cell.getBackground();
                int f = cell.getForeground();
                boolean change = false;
                if (oldPalette != null) {
                    /// turn an oldPalette index in to a color
                    if (b > psize) {
                        if (changedColors.contains(b)) {
                            change = true;
                            b = oldPalette.get(b);
                        }
                    }
                    if (f > psize) {
                        if (changedColors.contains(f)) {
                            change = true;
                            f = oldPalette.get(f);
                        }
                    }
                }
                if (inversePalette != null) {
                    // turn a color in to a current index
                    if (b > psize) {
                        if (inversePalette.containsKey(b)) {
                            change = true;
                            b = inversePalette.get(b);
                        }
                    }
                    if (f > psize) {
                        if (inversePalette.containsKey(f)) {
                            change = true;
                            f = inversePalette.get(f);
                        }
                    }
                }
                if (change) {
                    this.set(y, x, null, f, b);
                } else if (changedColors != null) {
                    if (changedColors.contains(f) || 
                            changedColors.contains(b)) {
                        this.set(y, x, null, null, null);
                    }
                }
            }
        }
        return oldPalette;
    }

    @Override
    public ColorPalette coerceToPalette(ColorPalette palette, String white,
            String black) {
        Integer w = null;
        Integer b = null;
        if (white != null) {
            w = palette.indexOfKey(white);
        }
        if (black != null) {
            b = palette.indexOfKey(black);
        }
        return coerceToPalette(palette, w, b);
    }

    @Override
    public ColorPalette coerceToPalette(ColorPalette palette, Integer white,
            Integer black) {
        ColorPalette oldPalette = this.palette;
        if (palette == null) {
            palette = this.palette;
        } else {
            this.palette = palette;
        }
        boolean paletteTruncation = false;
        Map<Integer, Integer> inversePalette = new HashMap<>();
        for (int i = 0; i < palette.size(); i++) {
            inversePalette.put(palette.get(i), i);
        }
        if (white == null) {
            for (int i = 15; i >= 0; i--) {
                white = inversePalette.get(ColorHelper.colorFromComponents(240 + i, 240 + i, 240 + i, null));
                if (white != null) { break; }
                if (i == 15) {
                    continue;
                }
                white = inversePalette.get(ColorHelper.colorFromComponents(240 + i, 240 + i, 255, null));
                if (white != null) { break; }
                white = inversePalette.get(ColorHelper.colorFromComponents(240 + i, 255, 240 + i, null));
                if (white != null) { break; }
                white = inversePalette.get(ColorHelper.colorFromComponents(240 + i, 255, 255, null));
                if (white != null) { break; }
                white = inversePalette.get(ColorHelper.colorFromComponents(255, 240 + i, 240 + i, null));
                if (white != null) { break; }
                white = inversePalette.get(ColorHelper.colorFromComponents(255, 240 + i, 255, null));
                if (white != null) { break; }
                white = inversePalette.get(ColorHelper.colorFromComponents(255, 255, 240 + i, null));
                if (white != null) { break; }
            }
            if(white == null) {
                white = 1;
            }
        }
        if (black == null) {
            for (int i = 0; i <= 15; i++) {
                black = inversePalette.get(ColorHelper.colorFromComponents(0 + i, 0 + i, 0 + i, null));
                if (black != null) { break; }
                if (i == 0) {
                    continue;
                }
                black = inversePalette.get(ColorHelper.colorFromComponents(0 + i, 0 + i, 0, null));
                if (black != null) { break; }
                black = inversePalette.get(ColorHelper.colorFromComponents(0 + i, 0, 0 + i, null));
                if (black != null) { break; }
                black = inversePalette.get(ColorHelper.colorFromComponents(0 + i, 0, 0, null));
                if (black != null) { break; }
                black = inversePalette.get(ColorHelper.colorFromComponents(0, 0 + i, 0 + i, null));
                if (black != null) { break; }
                black = inversePalette.get(ColorHelper.colorFromComponents(0, 0 + i, 0, null));
                if (black != null) { break; }
                black = inversePalette.get(ColorHelper.colorFromComponents(0, 0, 0 + i, null));
                if (black != null) { break; }
            }
            if(black == null) {
                black = 0;
            }
        }
        if (white > palette.size()) {
            throw new IllegalArgumentException(
                    "argument (white) out of bounds");
        }
        if (black > palette.size()) {
            throw new IllegalArgumentException(
                    "argument (black) out of bounds");
        }
        Set<Integer> changedColors = null;
        if (oldPalette != null) {
            changedColors = new HashSet<>();
            for (int c = 0; c < oldPalette.size(); c++) {
                if (c >= palette.size()) {
                    changedColors.add(c);
                } else if (palette.get(c) != oldPalette.get(c)) {
                    changedColors.add(c);
                }
            }
            if (palette.size() < oldPalette.size()) {
                paletteTruncation = true;
            }
        }

        int psize = palette.size();
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                TerminalCellLike cell = get(y, x);
                Integer b = cell.getBackground();
                Integer f = cell.getForeground();
                boolean change = false;
                if (oldPalette != null) {
                    /// turn an oldPalette index in to a color
                    if (b > psize) {
                        if (changedColors.contains(b)) {
                            change = true;
                            b = oldPalette.get(b);
                        }
                    }
                    if (f > psize) {
                        if (changedColors.contains(f)) {
                            change = true;
                            f = oldPalette.get(f);
                        }
                    }
                }
                if (inversePalette != null) {
                    // turn a color in to a current index
                    if (b > psize) {
                        if (inversePalette.containsKey(b)) {
                            change = true;
                            b = inversePalette.get(b);
                        }
                    }
                    if (f > psize) {
                        if (inversePalette.containsKey(f)) {
                            change = true;
                            f = inversePalette.get(f);
                        }
                    }
                }

                if (b > psize) {
                    int fore = f;
                    if (f <= psize) {
                        fore = palette.get(f);
                    }
                    b = ColorHelper.makeVisible(fore, 66, white, black);
                    change = true;
                }
                if (f > psize) {
                    int back = b;
                    if (b <= psize) {
                        back = palette.get(b);
                    }
                    f = ColorHelper.makeVisible(back, 66, white, black);
                    change = true;
                }
                
                if (change) {
                    this.set(y, x, null, f, b);
                }
            }
        }
        return oldPalette;
    }

    @Override
    public TerminalCellLike assign(int y, int x, TerminalCellLike tcell) {
        return this.getGrid().set(y, x, tcell);
    }

    @Override
    public void set(int y, int x, String sequence, Integer foreground, Integer background) {
        this.set(y, x, sequence, foreground, background, null, null);
    }

    @Override
    @Deprecated
    public void setCursorLocation(int[] lastCursor) {
        this.setCursorLocation(lastCursor[0], lastCursor[1]);
    }

    @Override
    public Positionable getCursorPosition() {
        return new Point(cursorY, cursorX);
    }

    @Override
    public void setCursorPosition(Positionable position) {
        this.setCursorLocation(position.getY(), position.getX());
    }

    @Override
    public boolean setFullScreen(boolean state) {
        return getFullScreen();
    }

    @Override
    public abstract boolean getFullScreen();

    @Override
    public void inhibitFullScreen(boolean state) {
        if (state) {
            // by default we do nothing -- but we don't want IDEs to complain
        }
    }

    @Override
    @Deprecated
    public TerminalInterface getBackingTerminalInterface() {
        return this;
    }

    @Override
    public TerminalInterface getBackingTerminal() {
        return this;
    }

    @Override
    public TerminalViewInterface getBackingTerminalView() {
        return this;
    }

    @Override
    public void setSize(TerminalScreenSize size) {
        if (size == null) {
            // don't let Netbeans complain
        }
        // doing nothing is safe/valid for the default
    }
    @Override
    public void setBounds(Regionlike bounds) {
        this.resize(bounds.getHeight(), bounds.getWidth());
        this.getGrid().setPosition(bounds.getY(), bounds.getY());
    }

    @Override
    public void setBounds(int rows, int cols, int y1, int x1) {
        this.resize(rows, cols);
        this.getGrid().setPosition(y1, x1);
    }

    @Override
    public void doUpdate() {
        int i = 0;
        if (i > 0) {
            // The default implementation should do nothing.
            // Silence the NetBeans warning.
        }
    }
}
