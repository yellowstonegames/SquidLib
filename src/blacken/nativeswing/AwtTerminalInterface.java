/* blacken - a library for Roguelike games
 * Copyright © 2011 Steven Black <yam655@gmail.com>
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
package com.googlecode.blacken.nativeswing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.font.TextAttribute;
import java.util.EnumSet;
import java.util.Map;

import javax.swing.JFrame;

import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.swing.AwtCell;
import com.googlecode.blacken.terminal.CellWalls;

/**
 * In the future (version 2.0) this will be normalized with the standard
 * TerminalInterface. This class will disappear and the standard
 * TerminalInterface will break binary compatibility.</p>
 *
 * @author Steven Black
 *
 */
public interface AwtTerminalInterface {

    /**
     * 
     * @param what codepoint
     */
    public abstract void addch(int what);

    /**
     * Clear the grid
     */
    public abstract void clear();

    /**
     * Clear
     * @param empty new empty
     */
    public abstract void clear(AwtCell empty);

    /**
     * Copy from another NativeTerminal
     * 
     * @param oterm original terminal
     * @param numRows number of rows to copy
     * @param numCols number of columns to copy
     * @param startY starting Y offset in <code>oterm</code>
     * @param startX starting X offset in <code>oterm</code>
     * @param destY destination Y offset in <code>this</code>
     * @param destX destination X offset in <code>this</code>
     */
    public abstract void copyFrom(TerminalPanel oterm, int numRows,
                                  int numCols, int startY, int startX,
                                  int destY, int destX);

    /**
     * perform an update
     */
    public abstract void doUpdate();

    /**
     * find column for window position
     * @param x window coordinate (X)
     * @return column
     */
    public abstract int findColForWindow(int x);

    /**
     * Find the row and column for a window coordinate
     * @param y window coordinate (Y)
     * @param x window coordinate (X)
     * @return new point mapping to row and column
     */
    public abstract Positionable findPositionForWindow(int y, int x);

    /**
     * find row for window position
     * @param y window coordinate (Y)
     * @return row
     */
    public abstract int findRowForWindow(int y);

    /**
     * Get the AwtCell in a position
     * @param y row
     * @param x column
     * @return cell
     */
    public abstract AwtCell get(int y, int x);

    /**
     * @return current background color
     */
    public abstract Color getCurBackground();

    /**
     * @return current foregound color
     */
    public abstract Color getCurForeground();

    /**
     * Get cursor location
     * @return cursor location [y, x]
     */
    public abstract int[] getCursorLocation();

    /**
     * @return cursor's X position
     */
    public abstract int getCursorX();

    /**
     * @return cursor's Y position
     */
    public abstract int getCursorY();

    /**
     * get the empty cell
     * @return empty cell
     */
    public abstract AwtCell getEmpty();

    /**
     * Get the current font
     * @return the current font
     */
    public abstract Font getFont();

    /**
     * Get the grid size
     * @return the grid size
     */
    public abstract Regionlike getGridBounds();

    /**
     * @return height
     */
    public abstract int gridHeight();

    /**
     * @return width
     */
    public abstract int gridWidth();

    /**
     * Hide the cursor
     */
    public abstract void hideCursor();

    /**
     * Initialize the panel
     * @param font font to use
     * @param rows rows to use
     * @param cols columns to use
     * @param empty empty cell
     */
    public abstract void init(Font font, int rows, int cols, AwtCell empty);

    /**
     * 
     * @return separate cursor?
     */
    public abstract boolean isSeparateCursor();

    /**
     * Move the update position
     * 
     * @param y row
     * @param x column
     */
    public abstract void mv(int y, int x);

    /**
     * This function conflicts with a (deprecated) JPanel function.
     * <p><em>Use {@link #mv(int, int)} instead.</em></p>
     * @Deprecated use 'mv' instead.
     * @param x x coordinate
     * @param y y coordinate
     */
    @Deprecated
    public abstract void move(int x, int y);

    /**
     * Move a block
     * @param numRows number of rows
     * @param numCols number of columns
     * @param origY original Y
     * @param origX original X
     * @param newY new Y
     * @param newX new X
     */
    public abstract void moveBlock(int numRows, int numCols, int origY,
                                   int origX, int newY, int newX);

    /**
     * Move the cursor
     * @param y row
     * @param x column
     */
    public abstract void moveCursor(int y, int x);

    /**
     * Move the cursor
     * @param y row
     * @param x column
     * @param cursorColor cursor color
     */
    public abstract void moveCursor(int y, int x, Paint cursorColor);

    /**
     * Move and add
     * @param y row
     * @param x column
     * @param what codepoint
     */
    public abstract void mvaddch(int y, int x, int what);

    /**
     * 
     * @param y row
     * @param x column
     * @param what codepoint
     */
    public abstract void mvoverlaych(int y, int x, int what);

    /**
     * 
     * @param y row
     * @param x column
     * @param str string
     */
    public abstract void mvputs(int y, int x, String str);

    /**
     * @param what codepoint
     */
    public abstract void overlaych(int what);

    /**
     * 
     * @param what string
     */
    public abstract void puts(String what);

    /**
     * refresh the window
     */
    public abstract void refresh();

    /**
     * refresh a line
     * @param y row
     */
    public abstract void refreshLine(int y);

    /**
     * refresh a region
     * @param height height
     * @param width width
     * @param y1 y1
     * @param x1 x1
     */
    public abstract void refreshRegion(int height, int width, int y1, int x1);

    /**
     * Resize the frame
     * 
     * <p>This function will only have the expected effect if it is the only
     * panel on the frame.</p>
     * 
     * @param frame frame (presumably the one we belong to)
     * @param fontSize the font size you want
     */
    public abstract void resizeFrame(JFrame frame, int fontSize);

    /**
     * resize the grid
     * @param rows number of rows
     * @param cols number of columns
     */
    public abstract void resizeGrid(int rows, int cols);

    /**
     * set a cell
     * @param y row
     * @param x column
     * @param cell cell
     */
    public abstract void set(int y, int x, AwtCell cell);

    /**
     * Set a cell
     * @param y row
     * @param x column
     * @param codepoint codepoint
     * @param back background
     * @param fore foreground
     */
    public abstract void set(int y, int x, int codepoint, Color back, Color fore);

    /**
     * Set a cell
     * @param y row
     * @param x column
     * @param codepoint the codepoint
     * @param foreground foreground color
     * @param background background color
     * @param attributes attribute mapping
     * @param walls cell walls
     */
    public abstract void set(int y, int x, int codepoint, Color foreground,
                             Color background,
                             Map<TextAttribute, Object> attributes,
                             EnumSet<CellWalls> walls);

    /**
     * set a cell
     * @param y row
     * @param x column
     * @param glyph glyph
     * @param attributes attributes
     */
    public abstract void set(int y, int x, int glyph,
                             Map<TextAttribute, Object> attributes);

    /**
     * Set a cell
     * @param y row
     * @param x column
     * @param codepoint the codepoint
     * @param attributes attribute mapping
     * @param walls cell walls
     */
    public abstract void set(int y, int x, int codepoint,
                             Map<TextAttribute, Object> attributes,
                             EnumSet<CellWalls> walls);

    /**
     * Set the background color
     * @param colr new color
     */
    public abstract void setCurBackground(Color colr);

    /**
     * Set the foreground color
     * @param colr new color
     */
    public abstract void setCurForeground(Color colr);

    /**
     * 
     * @param y row
     * @param x column
     */
    public abstract void setCursorLocation(int y, int x);

    /**
     * Set the empty cell
     * @param empty empty cell
     */
    public abstract void setEmpty(AwtCell empty);

    /**
     * Set the font
     * @param font the font
     */
    public abstract void setFont(Font font);

    /**
     * 
     * @param separateCursor new state
     */
    public abstract void setSeparateCursor(boolean separateCursor);

    /**
     * The window has been resized
     */
    public abstract void windowResized();

}
