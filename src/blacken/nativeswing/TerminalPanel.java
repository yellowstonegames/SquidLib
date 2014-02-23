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
package com.googlecode.blacken.nativeswing;

import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.grid.SimpleSize;
import com.googlecode.blacken.grid.Sizable;
import com.googlecode.blacken.swing.AwtCell;
import com.googlecode.blacken.terminal.CellWalls;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.font.GraphicAttribute;
import java.awt.font.TextAttribute;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This is (most of) the TerminalInterface that is event agnostic.
 * 
 * <p>It should allow you to implement a Panel which totally stand-alone, but
 * behaves the same as a BlackenPanel.</p>
 * 
 * <p><em>Warning:</em> This doesn't behave <em>exactly</em> like it would if
 * you were using a real TerminalInterface. JPanel already had a function
 * (thankfully deprecated) that was called {@link #move(int, int)}. As such,
 * the function in this class is called simply {@link #mv(int, int)}.</p>
 * 
 * @author yam655
 */
public class TerminalPanel extends JPanel implements AwtTerminalInterface {
    private static final long serialVersionUID = 7500042639340765473L;
    private AwtCell empty = new AwtCell();
    private int minX = 80;
    private int minY = 25;
    private Grid<AwtCell> grid = new Grid<>(empty, minY, minX);
    
    // private transient Image image = null;
    private transient Graphics2D graphics = null;

    private boolean fontHasDouble = true;
    private int fontAscent;
    private int fontSglAdvance;
    private int fontDblAdvance;
    private int fontHeight;
    private Font font;
    private FontMetrics metrics;

    /**
     * X Position where the cursor will be when image is updated
     */
    private int cursorX;
    /**
     * Y Position where the cursor will be when image is updated
     */
    private int cursorY;

    /**
     * X Position where the cursor is in the image
     */
    private int lastCursorX;
    /**
     * Y Position where the cursor is in the image
     */
    private int lastCursorY;

    /**
     * The cursor's color
     */
    private Paint cursorColor;

    /**
     * Flag to ignore cell-specific refresh and refresh everything.
     */
    private boolean refresh_all;
    private int updateX = 0;
    private int updateY = 0;
    private Color curForeground = new Color(0xffffffff);
    private Color curBackground = new Color(0xff000000);
    private boolean separateCursor = false;
    protected HashMap<String, GraphicAttribute> replacement = null;

    /**
     * Create a new panel.
     */
    public TerminalPanel() {
        super(true);
    }
    
    /**
     * Create a new panel with a layout manager.
     * @param layout layout manager
     */
    public TerminalPanel(LayoutManager layout) {
        super(layout, true);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#addch(int)
     */
    @Override
    public void addch(int what) {
        if (updateX >= grid.getWidth()) {
            updateX = grid.getWidth() -1;
        }
        if (updateY >= grid.getHeight()) {
            updateY = grid.getHeight() -1;
        }
        AwtCell cell;

        if (what == '\n') {
            updateY++;
            updateX = 0;
        } else if (what == '\r') {
            updateX = 0;
        } else if (what == '\b') {
            if (updateX > 0) updateX --;
            cell = this.get(updateY, updateX);
            cell.setSequence("\u0000");
            this.set(updateY, updateX, cell);
        } else if (what == '\t') {
            updateX = updateX + 8;
            updateX -= updateX % 8;
        } else {
            cell = this.get(updateY, updateX);
            cell.setSequence(what);
            cell.setForegroundColor(curForeground);
            cell.setBackgroundColor(curBackground);
            this.set(updateY, updateX, cell);
            updateX++;
        }
        
        if (updateX >= grid.getWidth()) {
            updateX = 0;
            updateY++;
        }
        if (updateY >= grid.getHeight()) {
            this.moveBlock(grid.getHeight() -1, grid.getWidth(), 1, 0, 0, 0);
            updateY = grid.getHeight() -1;
        }
        if (!separateCursor) setCursorLocation(updateY, updateX);
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#clear()
     */
    @Override
    public void clear() {
        clear(this.empty);
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#clear(com.googlecode.blacken.swing.AwtCell)
     */
    @Override
    public void clear(AwtCell empty) {
        cursorX = 0; cursorY = 0;
        updateX = 0; updateY = 0;
        empty.setDirty(false);
        grid.clear(empty);
        empty.setDirty(true);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#copyFrom(com.googlecode.blacken.nativeswing.TerminalPanel, int, int, int, int, int, int)
     */
    @Override
    public void 
    copyFrom(TerminalPanel oterm, int numRows, int numCols, int startY,
             int startX, int destY, int destX) {
        if (oterm == this) {
            this.moveBlock(numRows, numCols, startY, startX, destY, destX);
        } else {
            getGrid().copyFrom(oterm.getGrid(), numRows, numCols, startY, startX, 
                           destY, destX, new AwtCell.ResetCell());
            forceRefresh(numRows, numCols, destY, destX);
        }
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#doUpdate()
     */
    @Override
    public void doUpdate() {
        this.paintComponent(getGraphics());
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#findColForWindow(int)
     */
    @Override
    public int findColForWindow(int x) {
        Rectangle r = this.getRootPane().getBounds();
        x -= r.x;
        int ret = x / this.fontSglAdvance;
        return ret;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#findPositionForWindow(int, int)
     */
    @Override
    public Positionable findPositionForWindow(int y, int x) {
        Rectangle r = this.getRootPane().getBounds();
        y -= r.y;
        int retY = y / this.fontHeight;
        x -= r.x;
        int retX = x / this.fontSglAdvance;
        return new Point(retY, retX);
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#findRowForWindow(int)
     */
    @Override
    public int findRowForWindow(int y) {
        Rectangle r = this.getRootPane().getBounds();
        y -= r.y;
        int ret = y / this.fontHeight;
        return ret;
    }
    
    private void 
    forceRefresh(int numRows, int numCols, int startY, int startX) {
        for (int y = startY; y < numRows + startY; y++) {
            for (int x = startX; x < numCols + startX; x++) {
                grid.get(y, x).setDirty(true);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#get(int, int)
     */
    @Override
    public AwtCell get(int y, int x) {
        return grid.get(y, x);
    }
    /**
     * Get the best window size.
     * 
     * @return best size
     */
    protected Sizable getBestWindowSize() {
        int xsize, ysize;
        Regionlike gridBounds = grid.getBounds();
        xsize = fontSglAdvance * gridBounds.getWidth();
        ysize = fontHeight * gridBounds.getHeight();
        return new SimpleSize(ysize, xsize);
    }
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#getCurBackground()
     */
    @Override
    public Color getCurBackground() {
        return this.curBackground;
    }
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#getCurForeground()
     */
    @Override
    public Color getCurForeground() {
        return this.curForeground;
    }
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#getCursorLocation()
     */
    @Override
    public int[] getCursorLocation() {
        int[] ret = {cursorY, cursorX};
        return ret;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#getCursorX()
     */
    @Override
    public int getCursorX() {
        return cursorX;
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#getCursorY()
     */
    @Override
    public int getCursorY() {
        return cursorY;
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#getEmpty()
     */
    @Override
    public AwtCell getEmpty() {
        return empty;
    }

    /*
     * (non-Javadoc)
     * @see java.awt.Component#getFont()
     */
    @Override
    public Font getFont() {
        return super.getFont();
    }
    
    /**
     * This is used by {@link #copyFrom(TerminalPanel, int, int, int, int, int, int)}
     * @return internal grid
     */
    private Grid<AwtCell> getGrid() {
        return grid;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#getGridBounds()
     */
    @Override
    public Regionlike getGridBounds() {
        return grid.getBounds();
    }
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#gridHeight()
     */
    @Override
    public int gridHeight() {
        if (grid == null) return 0;
        return grid.getWidth();
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#gridWidth()
     */
    @Override
    public int gridWidth() {
        if (grid == null) return 0;
        return grid.getWidth();
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#hideCursor()
     */
    @Override
    public void hideCursor() {
        moveCursor(-1, -1, null);
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#init(java.awt.Font, int, int, com.googlecode.blacken.swing.AwtCell)
     */
    @Override
    public void init(Font font, int rows, int cols, AwtCell empty) {
        setCursor(null);
        // int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        // int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        // setBounds(0, 0, width, height);
        Dimension maxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.setMaximumSize(maxSize);
        graphics = (Graphics2D) getGraphics();
        this.minY = rows;
        this.minX = cols;
        setFont(font, false);
        grid.reset(rows, cols, empty);
        // repaint();
    }
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#isSeparateCursor()
     */
    @Override
    public boolean isSeparateCursor() {
        return separateCursor;
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#mv(int, int)
     */
    @Override
    public void mv(int y, int x) {
        updateX = x; updateY = y;
        if (!separateCursor) setCursorLocation(y, x);
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#move(int, int)
     */
    @Override
    @Deprecated
    public void move(int x, int y) {
        super.move(x, y);
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#moveBlock(int, int, int, int, int, int)
     */
    @Override
    public void moveBlock(int numRows, int numCols, int origY, int origX,
                          int newY, int newX) {
        grid.moveBlock(numRows, numCols, origY, origX, newY, newX, 
                       new AwtCell.ResetCell());
    }
    
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#moveCursor(int, int)
     */
    @Override
    public void moveCursor(int y, int x) {
        moveCursor(y, x, null);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#moveCursor(int, int, java.awt.Paint)
     */
    @Override
    public void moveCursor(int y, int x, Paint cursorColor) {
        if (cursorColor != null) {
            this.cursorColor = cursorColor;
        }
        cursorX = x;
        cursorY = y;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#mvaddch(int, int, int)
     */
    @Override
    public void mvaddch(int y, int x, int what) {
        mv(y, x);
        addch(what);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#mvoverlaych(int, int, int)
     */
    @Override
    public void mvoverlaych(int y, int x, int what) {
        AwtCell c = grid.get(y, x);
        c.addGlyph(what);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#mvputs(int, int, java.lang.String)
     */
    @Override
    public void mvputs(int y, int x, String str) {
        mv(y, x);
        puts(str);
    }
    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#overlaych(int)
     */
    @Override
    public void overlaych(int what) {
        if (updateX > 0) {
            mvoverlaych(updateY, updateX-1, what);
        }
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.swing.PanelInterface#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        AwtCell c;
        boolean need_cursor = false;

        if (refresh_all) {
            graphics.setPaint(Color.BLACK);
            graphics.fill(getBounds());
        }

        if (lastCursorX != -1 && lastCursorY != -1) {
            if (cursorX != lastCursorX || cursorY != lastCursorY) {
                if (grid.contains(lastCursorY, lastCursorX)) {
                    c = grid.get(lastCursorY, lastCursorX);
                    c.setDirty(true);
                }
            }
        }
        if (cursorX != -1 && cursorY != -1) {
            if (grid.contains(cursorY, cursorX)) {
                c = grid.get(cursorY, cursorX);
                if (c.isDirty()) {
                    need_cursor = true;
                } else if (cursorX != lastCursorX || cursorY != lastCursorY) {
                    need_cursor = true;
                }
            } else {
                need_cursor = true;
                if (cursorY >= grid.getHeight() + grid.getY()) {
                    cursorY = grid.getHeight() + grid.getY() -1;
                }
                if (cursorX >= grid.getWidth() + grid.getX()) {
                    cursorX = grid.getWidth() + grid.getX() -1;
                }
            }
        }
        lastCursorX = cursorX;
        lastCursorY = cursorY;
        
        for (int y = 0; y < grid.getHeight(); y++) {
            // We do the background then the foreground so that double-wide 
            // characters get the background set reasonably.
            
            for (int x = 0; x < grid.getWidth(); x++) {
                c = grid.get(y, x);
                if (c.isDirty() || refresh_all) {
                    graphics.setPaint(c.getBackgroundColor());
                    graphics.fill(new Rectangle(x * fontSglAdvance, 
                                                y * fontHeight, 
                                                fontSglAdvance, 
                                                fontHeight));
                }
            }
            // I'm not sure whether this will be enough to get the "complex"
            // characters to work. -- We may need to do it by code-point.
            int fontHeightD2 = fontHeight / 2;
            int fontSglAdvanceD2 = fontSglAdvance / 2;
            for (int x = 0; x < grid.getWidth(); x++) {
                c = grid.get(y, x);
                if (c.isDirty() || refresh_all) {
                    String cs = c.getSequence();
                    c.setFont(this.font);
                    // For double-wide characters, we can safely put a NUL
                    // byte in the second slot and it will never be displayed.
                    if (cs != null && !"\u0000".equals(cs)) {
                        int w = metrics.stringWidth(cs);
                        w = fontSglAdvance - w;
                        if (w < 0) w = 0;
                        else w /= 2;
                        graphics.drawString(c.getAttributedString().getIterator(), 
                                        x * fontSglAdvance + w, 
                                        y * fontHeight + fontAscent);
                    }
                    c.setDirty(false);
                    if (c.getCellWalls() != null && !c.getCellWalls().isEmpty()) {
                        int x1 = x * fontSglAdvance;
                        int y1 = y * fontHeight;
                        graphics.setColor(c.getForegroundColor());
                        if (c.getCellWalls().contains(CellWalls.TOP)) {
                            graphics.drawLine(x1, y1, 
                                              x1 + fontSglAdvance -1, y1);
                        }
                        if (c.getCellWalls().contains(CellWalls.LEFT)) {
                            graphics.drawLine(x1, y1, x1, 
                                              y1 + fontHeight -1);
                        }
                        if (c.getCellWalls().contains(CellWalls.BOTTOM)) {
                            graphics.drawLine(x1, y1 + fontHeight-1, 
                                              x1 + fontSglAdvance -1, 
                                              y1 + fontHeight-1);
                        }
                        if (c.getCellWalls().contains(CellWalls.RIGHT)) {
                            graphics.drawLine(x1 + fontSglAdvance-1, y1, 
                                              x1 + fontSglAdvance-1, 
                                              y1 + fontHeight -1);
                        }
                        if (c.getCellWalls().containsAll(CellWalls.HORIZONTAL)) {
                            graphics.drawLine(x1, y1 + fontHeightD2, 
                                              x1 + fontSglAdvance -1, 
                                              y1 + fontHeightD2);
                        }
                        if (c.getCellWalls().containsAll(CellWalls.VERTICAL)) {
                            graphics.drawLine(x1 + fontSglAdvanceD2, y1, 
                                              x1 + fontSglAdvanceD2, 
                                              y1 + fontHeight -1);
                        }
                    }
                }
            }
        }
        if (need_cursor) {
            c = grid.get(cursorY, cursorX);
            if (cursorColor == null) {
                graphics.setPaint(c.getForegroundColor());
            } else {
                graphics.setPaint(cursorColor);
            }
            graphics.fill(new Rectangle(cursorX * fontSglAdvance, 
                                        cursorY * fontHeight + fontAscent,
                                        fontSglAdvance -1, 
                                        fontHeight - fontAscent -1));
        }
        refresh_all = false;
        // g.drawImage(image, 0, 0, null);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#puts(java.lang.String)
     */
    @Override
    public void puts(String what) {
        int cp;
        int lastUpX = updateX-1, lastUpY = updateY-1;
        for (int i = 0; i < what.codePointCount(0, what.length()); i++) {
            cp = what.codePointAt(i);
            switch (Character.getType(cp)) {
            case Character.COMBINING_SPACING_MARK:
            case Character.ENCLOSING_MARK:
            case Character.NON_SPACING_MARK:
                if (lastUpX >= 0 && lastUpY >= 0) {
                    mvoverlaych(lastUpY, lastUpX, cp);
                }
                break;
            default:
                lastUpX = updateX; lastUpY = updateY;
                addch(cp);
            }
        }
    }

    /**
     * Recalculate the font bits.
     * 
     * <p>The logic this uses totally breaks down if a variable-width font is 
     * used.</p>
     * 
     * <p>For a variable-width font, you'd need to walk every character you
     * plan to use, track the width, then use the max double-wide width or 2x 
     * the max single-wide width... That is, if you plan to do the 
     * single-width / double-width logic traditionally found on terminals.</p>
     *  
     * <p>If you want variable width fonts, it is probably best not to treat it 
     * as a traditional double-wide character and to instead treat it as a 
     * large single-width character -- so that <code>fontDblAdvance</code> and 
     * <code>fontSglAdvance</code> are the same and <code>fontHasDouble</code>
     * is false.</p> 
     */
    protected void recalculateFontBits() {
        if (this.getGraphics() == null) {
            return;
        }
        metrics = this.getGraphics().getFontMetrics(this.font);
        fontAscent = metrics.getMaxAscent()+1;
        fontDblAdvance = metrics.getMaxAdvance();
        fontSglAdvance = metrics.charWidth('W');
        if (fontDblAdvance == -1) fontDblAdvance = fontSglAdvance;
        if (fontDblAdvance >= fontSglAdvance + fontSglAdvance) {
            fontHasDouble = true;
        } else fontHasDouble = false;
        if (fontHasDouble) {
            int sa = fontDblAdvance / 2;
            if (sa >= fontSglAdvance) {
                fontSglAdvance = sa;
            } else {
                fontDblAdvance = fontSglAdvance * 2;
            }
        }
        /* XXX: Here's the issue:
         * <ul>
         * <li>The font metrics are based upon the base-line location.
         * <li>Any font can have multiple base-line locations.
         * <li>More-over, any reasonably complete font <i>will</i> have 
         *     multiple baseline locations.
         * <li>FontMetrics.getHeight() does not claim to use the MaxAscent and 
         *     MaxDescent.
         * <li>getAscent and getDescent explicitly state that some glyphs will
         *     fall outside the ascent and descent lines that they describe.
         * <li>The font height, as it is all based upon distance away the 
         *     baseline, will be wrong if we just use getMaxAscent and
         *     getMaxDescent. (Just imagine that ROMAN_BASELINE has glyphs
         *     spanning far above the baseline, and the same font has 
         *     HANGING_BASELINE glyphs which hang far below the baseline.
         *     They could even be the same height!)
         * <li>LineMetrics (which can provide baseline offsets) does not 
         *     appear to support any supplementary 
         *     characters.
         * <li>Our use-case doesn't require consistent baseline between glyphs
         *     which use a different baseline. We much prefer to maximize the
         *     visible cell. This is consistent with what you'd expect in a
         *     terminal application with a fixed-point font.
         * </ul>
         * 
         * The best solution, then, would be to keep separate metrics for
         * font regions which use ROMAN_BASELINE, HANGING_BASELINE, and
         * CENTER_BASELINE glyphs. Unfortunately, there doesn't seem to be a
         * way to track what sort of baseline is used by any particular glyph.
         * The system wants you to check the metrics for each glyph. (As you
         * can, in fact, have fonts which are composed of other fonts, so
         * things can have the same baseline and still have different metrics.)
         * <p>
         * I think, for us, the best approach would be to require a consistent
         * font within a Unicode range, and to allow for custom fonts for 
         * specific code ranges. We could then track the metrics for the Unicode
         * code ranges instead of for each font.
         * <p>
         * For details on the various code ranges, and the glyphs supported
         * by each: http://unicode.org/charts/
         */
        //fontHeight = metrics.getMaxAscent() + metrics.getMaxDescent() 
        //                                    + metrics.getLeading();
        fontHeight = metrics.getHeight() +2;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#refresh()
     */
    @Override
    public void refresh() {
        refresh_all = true;
        doUpdate();
        paintComponent(getGraphics());
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#refreshLine(int)
     */
    @Override
    public void refreshLine(int y) {
        if (y < 0) { y = 0; }
        if (y > grid.getHeight()) { y = grid.getHeight(); } 
        for (int x = 0; x < grid.getWidth(); x++) {
            grid.get(y, x).setDirty(true);
        }
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#refreshRegion(int, int, int, int)
     */
    @Override
    public void refreshRegion(int height, int width, int y1, int x1) {
        if (y1 < 0) y1 = 0;
        if (x1 < 0) x1 = 0;

        if (height < 0) height = grid.getHeight();
        if (height + y1 >= grid.getHeight()) {
            height = grid.getHeight() - y1;
        }

        if (width < 0) width = grid.getWidth();
        if (width + y1 >= grid.getWidth()) {
            width = grid.getWidth() - y1;
        }

        int y2 = y1 + height -1;
        int x2 = x1 + width -1;

        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                grid.get(y, x).setDirty(true);
            }
        }
    }

    protected void resizeFontToFit() {
        // Rectangle r = this.getRootPane().getContentPane().getBounds();
        Rectangle r = this.getVisibleRect();
        float fsize = 0.5f;
        int idealAdvance = r.width / this.minX;
        int idealHeight = r.height / this.minY;
        //System.out.printf("DEBUG(font2fit): ideal:%d height:%d\n", 
        //                  idealAdvance, idealHeight);
        Font f = this.font.deriveFont(fsize);
        setFontNoUpdate(f);
        if (idealAdvance <= fontSglAdvance || idealHeight <= fontHeight) {
            //System.out.printf("DEBUG(font2fit): BOGUS advance:%d height:%d\n", 
            //                  fontSglAdvance, fontHeight);
            // This is a real bogus size, but apparently we can't get 
            // anything better
            return;
        }
        Font lastFont = f;
        // System.out.printf("DEBUG(font2fit): size:%f advance:%d height:%d\n", 
        //                  fsize, fontSglAdvance, fontHeight);
        while (idealAdvance >= fontSglAdvance && idealHeight >= fontHeight) {
            lastFont = f;
            f = lastFont.deriveFont(fsize += 0.5f); 
            setFontNoUpdate(f);
            // System.out.printf("DEBUG(font2fit): size:%f advance:%d height:%d\n", fsize, fontSglAdvance, fontHeight);
        }
        setFont(lastFont, false);
        int newRows = r.height / fontHeight;
        int newCols = r.width / fontSglAdvance;
        grid.setSize(newRows, newCols);
        //System.out.printf("DEBUG(font2fit): grid: ys:%d; xs:%d\n", newRows, newCols);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#resizeFrame(javax.swing.JFrame, int)
     */
    @Override
    public void resizeFrame(JFrame frame, int fontSize) {
        Font f;
        if (fontSize > 0) {
            f = this.font;
            if (f == null) {
                f = new Font("Monospace", Font.PLAIN, fontSize);
            } else {
                f = f.deriveFont(fontSize);
            }
            setFont(f, false);
        }
        Sizable sizes = getBestWindowSize();
        frame.setSize(sizes.getWidth(), sizes.getHeight());
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#resizeGrid(int, int)
     */
    @Override
    public void resizeGrid(int rows, int cols) {
        grid.setSize(rows, cols);
    }

    /**
     * Resize the grid to the window.
     */
    protected void resizeGridToWindow() {
        int xsize, ysize;
        Dimension d = this.getSize();
        xsize = d.width / fontSglAdvance;
        ysize = d.height / fontHeight;
        this.grid.setSize(ysize, xsize);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#set(int, int, com.googlecode.blacken.swing.AwtCell)
     */
    @Override
    public void set(int y, int x, AwtCell cell) {
        AwtCell c = grid.get(y, x);
        c.setCell(cell);
        c.setDirty(true);
    }


    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#set(int, int, int, java.awt.Color, java.awt.Color)
     */
    @Override
    public void set(int y, int x, int codepoint, Color back, Color fore) {
        grid.get(y, x).setCell(codepoint, back, fore);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#set(int, int, int, java.awt.Color, java.awt.Color, java.util.Map, java.util.EnumSet)
     */
    @Override
    public void set(int y, int x, int codepoint, Color foreground, 
                    Color background, Map<TextAttribute, Object> attributes,
                    EnumSet<CellWalls> walls) {
        AwtCell cell = grid.get(y, x);
        cell.setSequence(codepoint);
        cell.setTextAttributes(attributes);
        cell.setBackgroundColor(background);
        cell.setForegroundColor(foreground);
        cell.setCellWalls(walls);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#set(int, int, int, java.util.Map)
     */
    @Override
    public void set(int y, int x, int glyph, 
                    Map<TextAttribute, Object> attributes) {
        grid.get(y, x).setCell(glyph, attributes);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#set(int, int, int, java.util.Map, java.util.EnumSet)
     */
    @Override
    public void set(int y, int x, int codepoint, 
                    Map<TextAttribute, Object> attributes,
                    EnumSet<CellWalls> walls) {
        AwtCell cell = grid.get(y, x);
        cell.setSequence(codepoint);
        cell.setTextAttributes(attributes);
        cell.setCellWalls(walls);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#setCurBackground(java.awt.Color)
     */
    @Override
    public void setCurBackground(Color colr) {
        this.curBackground = colr;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#setCurForeground(java.awt.Color)
     */
    @Override
    public void setCurForeground(Color colr) {
        this.curForeground = colr;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#setCursorLocation(int, int)
     */
    @Override
    public void setCursorLocation(int y, int x) {
        this.cursorX = x;
        this.cursorY = y;
        moveCursor(y, x);
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#setEmpty(com.googlecode.blacken.swing.AwtCell)
     */
    @Override
    public void setEmpty(AwtCell empty) {
        if (this.empty != empty) {
            this.empty.setCell(empty);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.JComponent#setFont(java.awt.Font)
     */
    @Override
    public void setFont(Font font) {
        // super.setFont(font); // called later
        setFont(font, true);
    }

    /**
     * Set the font, optionally recalculating the grid
     * @param font new font
     * @param recalc will we recalculate?
     */
    private void setFont(Font font, boolean recalc) {
        setFontNoUpdate(font);
        if (recalc && graphics != null) {
            resizeFontToFit();
        }
        if (empty != null && this.font != null) {
            if (empty.getFont() == null || 
                    !empty.getFont().equals(this.font)) {
                empty.setFont(this.font);
                for(int y = 0; y < grid.getHeight(); y++) {
                    for(int x = 0; x < grid.getWidth(); x++) {
                        grid.get(y, x).setFont(this.font);
                    }
                }
            }
        }
    }

    /**
     * Set the font and do not update
     * @param font new font
     */
    private void setFontNoUpdate(Font font) {
        if (font != null) {
            this.font = font;
        }
        super.setFont(this.font);
        if(graphics != null) {
            graphics.setFont(this.font);
            this.getGraphics().setFont(this.font);
        }
        recalculateFontBits();
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#setSeparateCursor(boolean)
     */
    @Override
    public void setSeparateCursor(boolean separateCursor) {
        this.separateCursor = separateCursor;
    }

    /* (non-Javadoc)
     * @see com.googlecode.blacken.nativeswing.AwtTerminalInterface#windowResized()
     */
    @Override
    public void windowResized() {
        resizeFontToFit();
        refresh();
    }

}
