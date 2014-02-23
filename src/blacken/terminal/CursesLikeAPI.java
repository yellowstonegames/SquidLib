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

import com.googlecode.blacken.colors.ColorHelper;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.exceptions.InvalidStringFormatException;
import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.terminal.editing.SingleLine;

/**
 * This is just vaguely similar to the Curses API.
 *
 * <p>This is not called "Curses" or "CursesAPI" because it is a mockery of
 * the glory of the Curses API.</p>
 *
 * @author Steven Black
 */
public class CursesLikeAPI implements TerminalInterface {
    private int curForeground = 0xffffffff;
    private int curBackground = 0xff000000;
    private boolean separateCursor = false;
    private final TerminalInterface terminal;

    public CursesLikeAPI(TerminalInterface terminal) {
        super();
        this.terminal = terminal;
        this.terminal.setCursorLocation(0, 0);
    }

    /**
     * Write a character to the current update location
     * @param what codepoint to add to the screen
     */
    public void addch(int what) {
        Grid<TerminalCellLike> grid = terminal.getGrid();
        if (grid == null) {
            throw new NullPointerException("Must call init() first.");
        }
        Positionable p = terminal.getCursorPosition();
        int updateX = p.getX();
        int updateY = p.getY();
        if (p.getX() == -1 && p.getY() == -1) {
            updateX = updateY = 0;
        }
        if (updateX >= grid.getWidth()) {
            updateX = grid.getWidth() - 1;
        }
        if (updateY >= grid.getHeight()) {
            updateY = grid.getHeight() - 1;
        }
        TerminalCellLike cell;
        if (what == '\n' || what == BlackenKeys.KEY_ENTER || 
                what == BlackenKeys.KEY_NP_ENTER) {
            updateY++;
            updateX = 0;
        } else if (what == '\r') {
            updateX = 0;
        } else if (what == '\b' || what == BlackenKeys.KEY_BACKSPACE) {
            if (updateX > 0) {
                updateX--;
            }
            cell = this.get(updateY, updateX);
            cell.setSequence("\u0000");
            this.set(updateY, updateX, cell);
        } else if (what == '\t' || what == BlackenKeys.KEY_TAB) {
            updateX = updateX + 8;
            updateX -= updateX % 8;
        } else {
            cell = this.get(updateY, updateX);
            cell.setSequence(what);
            cell.setForeground(getCurForeground());
            cell.setBackground(getCurBackground());
            this.set(updateY, updateX, cell);
            updateX++;
        }
        if (updateX >= grid.getWidth()) {
            updateX = 0;
            updateY++;
        }
        if (updateY >= grid.getHeight()) {
            this.moveBlock(grid.getHeight() - 1, grid.getWidth(), 1, 0, 0, 0);
            updateY = grid.getHeight() - 1;
        }
        terminal.setCursorLocation(updateY, updateX);
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.terminal.TerminalInterface#clear()
     */
    @Override
    public void clear() {
        terminal.clear();
        terminal.setCursorLocation(0, 0);
    }

    /**
     * Get the current background
     * @return the current background
     */
    public int getCurBackground() {
        return curBackground;
    }

    /**
     * get the current foreground
     * @return the current foreground
     */
    public int getCurForeground() {
        return curForeground;
    }

    @Override
    @Deprecated
    public int[] getCursorLocation() {
        return terminal.getCursorLocation();
    }

    @Override
    public Positionable getCursorPosition() {
        return terminal.getCursorPosition();
    }

    @Override
    public int getCursorX() {
        return terminal.getCursorX();
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.terminal.TerminalInterface#getCursorY()
     */
    @Override
    public int getCursorY() {
        return terminal.getCursorY();
    }

    public String gets(int length) {
        int x = terminal.getCursorX();
        int y = terminal.getCursorY();
        String ret = getString(y, x, length);
        terminal.setCursorLocation(y, x + ret.length());
        return ret;
    }
    
    @Override
    public String getString(int y, int x, int length) {
        // return SingleLine.getString(this, y, x, length, null);
        return terminal.getString(y, x, length);
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.terminal.TerminalInterface#init()
     */
    public void init() {
        terminal.init("Java", 25, 80);
        terminal.setCursorLocation(0, 0);
    }

    public boolean isSeparateCursor() {
        return separateCursor;
    }

    /**
     * Move the cursor. 
     * <p>
     * To move the visible cursor used for user-input, use the moveCursor 
     * function.
     * 
     * @param y row
     * @param x column
     */
    public void move(int y, int x) {
        setCursorLocation(y, x);
    }

    /**
     * Move the update position and write a character.
     * 
     * @param y new Y location
     * @param x new X location
     * @param what codepoint to write
     */
    public void mvaddch(int y, int x, int what) {
        move(y, x);
        addch(what);
    }

    /**
     * Move the cursor and get a character
     * 
     * <p>Unlike normal Curses, this <em>always</em> echos the character.</p>
     * 
     * @param y cursor's new Y location
     * @param x cursor's new X location
     * @return codepoint we get from the user
     */
    public int mvgetch(int y, int x) {
        setCursorLocation(y, x);
        int ch = getch();
        addch(ch);
        return ch;
    }

    /**
     * Overlay a character on to a specific character position.
     * 
     * @param y row
     * @param x column
     * @param what code point
     */
    public void mvoverlaych(int y, int x, int what) {
        TerminalCellLike c = terminal.getGrid().get(y, x);
        switch (Character.getType(what)) {
            case Character.COMBINING_SPACING_MARK:
            case Character.ENCLOSING_MARK:
            case Character.NON_SPACING_MARK:
                c.addSequence(what);
                break;
            default:
                c.setSequence(what);
        }

    }

    /**
     * Move the update location and write a string
     * 
     * @param y new Y location
     * @param x new X location
     * @param str string to write
     */
    public void mvputs(int y, int x, String str) {
        int[] pos = SingleLine.putString(terminal, y, x, str, this.curForeground,
                this.curBackground);
        move(pos[0], pos[1]);
    }

    /**
     * Overlay a character on to the last character position.
     * 
     * <p>Adds a codepoint to the previous cell (using y, x-1), and 
     * quietly fails if the cursor is now at the beginning of a line.</p>
     *  
     * @param what code point
     */
    public void overlaych(int what) {
        if (terminal.getCursorX() > 0) {
            mvoverlaych(terminal.getCursorY(), terminal.getCursorX() - 1, what);
        }
    }

    /**
     * Write a character sequence to the terminal.
     * 
     * @param what character sequence to write
     */
    public void puts(String what) {
        this.mvputs(terminal.getCursorY(), terminal.getCursorX(), what);
    }

    public void setCurBackground(int c) {
        this.curBackground = c;
        TerminalCellLike empty = this.getEmpty();
        empty.setBackground(curBackground);
        this.setEmpty(empty);
    }

    public void setCurBackground(String c) {
        ColorPalette palette = terminal.getPalette();
        if (palette == null) {
            try {
                this.curBackground = ColorHelper.neverTransparent(ColorHelper.makeColor(c));
            } catch (InvalidStringFormatException e) {
                throw new NullPointerException(String.format("palette is null, and color was invalid: %s", e.getMessage()));
            }
        } else {
            try {
                this.curBackground = palette.indexOfKey(c);
            } catch (NullPointerException e) {
                try {
                    this.curBackground = ColorHelper.neverTransparent(ColorHelper.makeColor(c));
                } catch (InvalidStringFormatException e1) {
                    throw new NullPointerException(String.format("palette is null, and color was invalid: %s", e1.getMessage()));
                }
            }
        }
        setCurBackground(curBackground);
    }

    public void setCurForeground(int c) {
        this.curForeground = c;
        TerminalCellLike empty = this.getEmpty();
        empty.setForeground(curForeground);
        this.setEmpty(empty);
    }

    public void setCurForeground(String c) {
        ColorPalette palette = terminal.getPalette();
        if (palette == null) {
            try {
                this.curForeground = ColorHelper.neverTransparent(ColorHelper.makeColor(c));
            } catch (InvalidStringFormatException e) {
                throw new NullPointerException(String.format("palette is null, and color was invalid: %s", e.getMessage()));
            }
        } else {
            try {
                this.curForeground = palette.indexOfKey(c);
            } catch(NullPointerException e) {
                try {
                    this.curForeground = ColorHelper.neverTransparent(ColorHelper.makeColor(c));
                } catch (InvalidStringFormatException e1) {
                    throw new NullPointerException(String.format("palette is null, and color was invalid: %s", e1.getMessage()));
                }
            }
        }
        setCurForeground(curForeground);
    }

    /**
     * Separate screen update location from cursor location.
     * 
     * @param separateCursor true to throw an exception; false to ignore
     * @deprecated not supported for CursesLikeAPI; automatic for native
     */
    @Deprecated
    public void setSeparateCursor(boolean separateCursor) {
        if (separateCursor) {
            throw new IllegalArgumentException("separateCursor no longer supported");
        }
    }

    @Override
    public void clear(TerminalCellLike empty) {
        terminal.clear(empty);
        terminal.setCursorLocation(0, 0);
    }

    @Override
    public void copyFrom(TerminalViewInterface oterm, int numRows, int numCols, int startY, int startX, int destY, int destX) {
        terminal.copyFrom(oterm, numRows, numCols, startY, startX, destY, destX);
    }

    @Override
    public void disableEventNotice(BlackenEventType event) {
        terminal.disableEventNotice(event);
    }

    /**
     * Curses-like alias for doUpdate.
     */
    public void doupdate() {
        terminal.doUpdate();
    }

    @Override
    public void doUpdate() {
        terminal.doUpdate();
    }

    @Override
    public void disableEventNotices() {
        terminal.disableEventNotices();
    }

    @Override
    public void enableEventNotice(BlackenEventType event) {
        terminal.enableEventNotice(event);
    }

    @Override
    @Deprecated
    public void enableEventNotices(EnumSet<BlackenEventType> events) {
        terminal.enableEventNotices(events);
    }

    @Override
    public TerminalCellLike get(int y, int x) {
        return terminal.get(y, x);
    }

    /**
     * Unlike standard Curses, this <em>never</em> echos the character.
     * @return 
     */
    @Override
    public int getch() {
        return terminal.getch();
    }
    @Override
    public int getch(int millis) {
        return terminal.getch(millis);
    }
    @Override
    public boolean keyWaiting() {
        return terminal.keyWaiting();
    }

    @Override
    public Regionlike getBounds() {
        return terminal.getBounds();
    }

    @Override
    public TerminalCellLike getEmpty() {
        return terminal.getEmpty();
    }

    @Override
    public Grid<TerminalCellLike> getGrid() {
        return terminal.getGrid();
    }

    @Override
    public int getHeight() {
        return terminal.getHeight();
    }

    @Override
    public EnumSet<BlackenModifier> getLockingStates() {
        return terminal.getLockingStates();
    }

    @Override
    public BlackenMouseEvent getmouse() {
        return terminal.getmouse();
    }

    @Override
    public ColorPalette getPalette() {
        return terminal.getPalette();
    }

    @Override
    public int getWidth() {
        return terminal.getWidth();
    }

    @Override
    public BlackenWindowEvent getwindow() {
        return terminal.getwindow();
    }

    @Override
    @Deprecated
    public int gridHeight() {
        return terminal.getHeight();
    }

    @Override
    @Deprecated
    public int gridWidth() {
        return terminal.getWidth();
    }

    @Override
    public void init(String name, int rows, int cols) {
        terminal.init(name, rows, cols);
        terminal.setCursorLocation(0, 0);
    }
    
    @Override
    public void init(String name, int rows, int cols, String... font) {
        terminal.init(name, rows, cols, font);
        terminal.setCursorLocation(0, 0);
    }

    @Override
    public void moveBlock(int numRows, int numCols, int origY, int origX,
            int newY, int newX) {
        terminal.moveBlock(numRows, numCols, origY, origX, newY, newX);
    }

    @Override
    public void quit() {
        terminal.quit();
    }

    @Override
    public void refresh() {
        terminal.refresh();
    }

    @Override
    public void resize(int rows, int cols) {
        terminal.resize(rows, cols);
    }

    @Override
    public void set(int y, int x, String sequence, Integer foreground, Integer background, EnumSet<TerminalStyle> style, EnumSet<CellWalls> walls) {
        terminal.set(y, x, sequence, foreground, background, style, walls);
    }

    @Override
    public void set(int y, int x, TerminalCellLike cell) {
        terminal.set(y, x, cell);
    }

    @Override
    public void set(int y, int x, String sequence, Integer foreground, Integer background) {
        terminal.set(y, x, sequence, foreground, background);
    }

    @Override
    public void setCursorLocation(int y, int x) {
        terminal.setCursorLocation(y, x);
    }

    @Override
    @Deprecated
    public void setCursorLocation(int[] pos) {
        terminal.setCursorLocation(pos);
    }

    @Override
    public void setCursorPosition(Positionable pos) {
        terminal.setCursorPosition(pos);
    }

    @Override
    public void setEmpty(TerminalCellLike empty) {
        terminal.setEmpty(empty);
    }

    @Override
    public void setFont(String font, boolean checkFont) throws FontNotFoundException {
        terminal.setFont(font, checkFont);
    }
    @Override
    public String setFont(String... font) throws FontNotFoundException {
        FontNotFoundException lastEx = null;
        String used = null;
        for (String f : font) {
            try {
                setFont(f);
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
    public ColorPalette setPalette(ColorPalette palette) {
        return terminal.setPalette(palette);
    }

    @Override
    public ColorPalette coerceToPalette(ColorPalette palette, Integer white, Integer black) {
        return terminal.coerceToPalette(palette, white, black);
    }

    @Override
    public ColorPalette coerceToPalette(ColorPalette palette, String white, String black) {
        return terminal.coerceToPalette(palette, white, black);
    }

    @Override
    @Deprecated
    public ColorPalette setPalette(ColorPalette palette, int white, int black) {
        return terminal.coerceToPalette(palette, white, black);
    }

    @Override
    public TerminalCellLike assign(int y, int x, TerminalCellLike cell) {
        return terminal.assign(y, x, cell);
    }

    @Override
    public boolean setFullScreen(boolean state) {
        return terminal.setFullScreen(state);
    }
    @Override
    public boolean getFullScreen() {
        return terminal.getFullScreen();
    }

    @Override
    public void inhibitFullScreen(boolean state) {
        terminal.inhibitFullScreen(state);
    }

    @Override
    public TerminalInterface getBackingTerminal() {
        return terminal;
    }

    @Override
    public void init(String name, int rows, int cols, TerminalScreenSize size, String... font) {
        terminal.init(name, rows, cols, size, font);
        terminal.setCursorLocation(0, 0);
    }

    @Override
    public void init(String name, int rows, int cols, TerminalScreenSize size) {
        terminal.init(name, rows, cols, size);
        terminal.setCursorLocation(0, 0);
    }

    @Override
    public void setSize(TerminalScreenSize size) {
        terminal.setSize(size);
    }

    @Override
    @Deprecated
    public TerminalInterface getBackingTerminalInterface() {
        return terminal;
    }

    @Override
    public TerminalViewInterface getBackingTerminalView() {
        return terminal;
    }

    @Override
    public void setBounds(Regionlike bounds) {
        Regionlike origBounds = getBounds();
        int cursor_y = getCursorY() - origBounds.getY() + bounds.getY();
        int cursor_x = getCursorX() - origBounds.getX() + bounds.getX();
        terminal.setBounds(bounds);
        if (bounds.contains(cursor_y, cursor_x)) {
            move(cursor_y, cursor_x);
        } else {
            move(0, 0);
        }
    }

    @Override
    public void setBounds(int rows, int cols, int y1, int x1) {
        Regionlike origBounds = getBounds();
        int cursor_y = getCursorY() - origBounds.getY() + y1;
        int cursor_x = getCursorX() - origBounds.getX() + x1;
        terminal.setBounds(rows, cols, y1, x1);
        if (new BoxRegion(rows, cols, y1, x1).contains(cursor_y, cursor_x)) {
            move(cursor_y, cursor_x);
        } else {
            move(0, 0);
        }
    }

    @Override
    public void refresh(int y, int x) {
        terminal.refresh(y, x);
    }

    @Override
    public void setEventNotices(EnumSet<BlackenEventType> events) {
        terminal.setEventNotices(events);
    }

    @Override
    public EnumSet<BlackenEventType> getEventNotices() {
        return terminal.getEventNotices();
    }
}
