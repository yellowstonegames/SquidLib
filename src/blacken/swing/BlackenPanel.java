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
package com.googlecode.blacken.swing;

import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.grid.SimpleSize;
import com.googlecode.blacken.grid.Sizable;
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
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPanel implementation supporting Blacken.
 * 
 * @author Steven Black
 */
public class BlackenPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlackenPanel.class);
    private static final long serialVersionUID = 1;
    private AwtCell empty = new AwtCell();
    private int minX = 80;
    private int minY = 25;
    private Grid<AwtCell> grid = new Grid<>(empty, minY, minX);

    @Deprecated
    private boolean fontHasDouble;
    @Deprecated
    private int fontAscent;
    @Deprecated
    private int fontSglAdvance;
    @Deprecated
    private int fontDblAdvance;
    @Deprecated
    private int fontHeight;
    @Deprecated
    private Font font;
    @Deprecated
    private FontMetrics metrics;

    public class FontBits {
        public Font font;
        public FontMetrics metrics;
        public int fontAscent;
        public int fontDblAdvance;
        public int fontSglAdvance;
        public boolean fontHasDouble;
        public int fontHeight;
    }

    protected FontBits bits = new FontBits();

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
    private transient boolean refresh_all = true;

    /**
     * Running average of the display speed
     */
    private transient long displaySpeed = 0;
    private int refreshedCnt = 0;
    private int repaintedCnt = 0;
    private Grid<AwtCell> gridView;
    private boolean refreshAlways = false;
    private boolean fontChanged;

    /**
     * Create a new panel.
     */
    public BlackenPanel() {
        super(true);
        checkForWorkarounds();
    }

    /**
     * Create a new panel with a layout manager.
     * @param layout layout manager
     */
    public BlackenPanel(LayoutManager layout) {
        super(layout, true);
        checkForWorkarounds();
    }

    private void checkForWorkarounds() {
        if (System.getProperty("os.name", "Other").contains("Mac")) {
            this.refreshAlways = true;
        }
    }

    /**
     * Clear the screen.
     */
    public void clear() {
        this.refresh_all = true;
        grid.clear(this.empty);
        this.moveCursor(0, 0);
    }

    /**
     * Perform a window update.
     * @deprecated No longer needed
     */
    @Deprecated
    public void doUpdate() {
    }

    /**
     * Find the column number for the window coordinate
     * @param x window coordinate
     * @return column
     */
    public int findColForWindow(int x) {
        Rectangle r = this.getRootPane().getBounds();
        x -= r.x;
        int ret = x / bits.fontSglAdvance;
        return ret;
    }
    /**
     * Find the grid position for a window position.
     *
     * @param y window coordinate
     * @param x window coordinate
     * @return {row, col}
     */
    public int[] findPositionForWindow(int y, int x) {
        Rectangle r = this.getRootPane().getBounds();
        y -= r.y;
        int retY = y / bits.fontHeight;
        x -= r.x;
        int retX = x / bits.fontSglAdvance;
        int[] ret = {retY, retX};
        return ret;
    }
    /**
     * Find the row number for the window coordinate
     * @param y window coordinate
     * @return row
     */
    public int findRowForWindow(int y) {
        Rectangle r = this.getRootPane().getBounds();
        y -= r.y;
        int ret = y / bits.fontHeight;
        return ret;
    }
    /**
     * Get an AWT cell for a position.
     *
     * @param y coordinate
     * @param x coordinate
     * @return the AWT cell
     */
    public AwtCell get(int y, int x) {
        return grid.get(y, x);
    }
    /**
     * Get the best window size.
     *
     * @return window size, as a SimpleSize
     */
    protected Sizable getBestWindowSize() {
        int xsize, ysize;
        Sizable gridSize = grid.getSize();
        xsize = bits.fontSglAdvance * gridSize.getWidth();
        ysize = bits.fontHeight * gridSize.getHeight();
        return new SimpleSize(ysize, xsize);
    }
    /**
     * Get the empty/template cell
     * @return the empty/template
     */
    public AwtCell getEmpty() {
        return empty;
    }

    @Override
    public Font getFont() {
        return AwtCell.getGlobalFont();
    }
    /**
     * Get the grid size
     * @return {ySize, xSize}
     */
    public Regionlike getGridBounds() {
        return grid.getBounds();
    }
    /**
     * Hide the cursor.
     */
    public void hideCursor() {
        moveCursor(-1, -1, null);
    }
    /**
     * Initialize the terminal window.
     *
     * @param font the font to use
     * @param rows the number of rows to use
     * @param cols the columns to use
     * @param empty the template/empty cell
     */
    public void init(Font font, int rows, int cols, AwtCell empty) {
        setCursor(null);
        this.setFocusTraversalKeysEnabled(false);
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        setBounds(0, 0, width, height);
        this.minY = rows;
        this.minX = cols;
        setFont(font);
        grid.reset(rows, cols, empty);
        repaint();
    }

    /**
     * Move a block of cells.
     *
     * @param numRows number of rows to move
     * @param numCols number of columns to move
     * @param origY orignal Y coordinate
     * @param origX orignal X coordinate
     * @param newY new Y coordinate
     * @param newX new X coordinate
     */
    public void moveBlock(int numRows, int numCols, int origY, int origX,
                          int newY, int newX) {
        grid.moveBlock(numRows, numCols, origY, origX, newY, newX,
                       new AwtCell.ResetCell());
    }

    /**
     * Move the cursor.
     *
     * @param y coordinate
     * @param x coordinate
     */
    public void moveCursor(int y, int x) {
        moveCursor(y, x, null);
    }

    /**
     * Move the cursor, and set a new cursor color.
     *
     * @param y coordinate
     * @param x coordinate
     * @param cursorColor new cursor color
     */
    public void moveCursor(int y, int x, Paint cursorColor) {
        if (cursorColor != null) {
            this.cursorColor = cursorColor;
        }
        cursorX = x;
        cursorY = y;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (this.fontChanged) {
            if(this.getGraphics() != null) {
                this.getGraphics().setFont(bits.font);
            }
            AwtCell.setGlobalFont(bits.font);
        }
        AwtCell c;
        boolean need_cursor = false;
        if (!getTopLevelAncestor().getBackground().equals(getEmpty().getBackgroundColor())) {
            getTopLevelAncestor().setBackground(getEmpty().getBackgroundColor());
        }
        Graphics2D graphics = (Graphics2D)g;
        if (AwtCell.getGlobalFont() != null) {
            graphics.setFont(AwtCell.getGlobalFont());
        }

        if (this.refreshAlways) {
            refresh_all = true;
        }
        Grid<AwtCell> grid = null;
        refresh_all = true;
        synchronized(this) {
            if (this.refreshedCnt == this.repaintedCnt) {
                if (this.gridView == null) {
                    return;
                }
                refresh_all = true;
            } else {
                this.repaintedCnt = this.refreshedCnt;
                if (gridView == null || !gridView.getBounds().equals(this.grid.getBounds())) {
                    this.gridView = this.grid.like();
                    refresh_all = true;
                }
                grid = this.grid;
            }
        }

        try {
            long startTime = System.currentTimeMillis();
            if (refresh_all) {
                graphics.setPaint(this.getEmpty().getBackgroundColor());
                graphics.fill(getBounds());
            } else {
                if (lastCursorX != -1 && lastCursorY != -1) {
                    if (cursorX != lastCursorX || cursorY != lastCursorY) {
                        if (gridView.contains(lastCursorY, lastCursorX)) {
                            if (grid != null) {
                                c = grid.get(lastCursorY, lastCursorX);
                            } else {
                                c = gridView.get(lastCursorY, lastCursorX);
                            }
                            c.setDirty(true);
                        }
                    }
                }
            }
            if (cursorX != -1 && cursorY != -1) {
                if (gridView.contains(cursorY, cursorX)) {
                    if (grid != null) {
                        c = grid.get(cursorY, cursorX);
                    } else {
                        c = gridView.get(cursorY, cursorX);
                    }
                    if (c.isDirty()) {
                        need_cursor = true;
                    } else if (cursorX != lastCursorX || cursorY != lastCursorY) {
                        need_cursor = true;
                    }
                } else {
                    need_cursor = true;
                    if (cursorY >= gridView.getHeight() + gridView.getY()) {
                        cursorY = gridView.getHeight() + gridView.getY() -1;
                    }
                    if (cursorX >= gridView.getWidth() + gridView.getX()) {
                        cursorX = gridView.getWidth() + gridView.getX() -1;
                    }
                }
            }
            lastCursorX = cursorX;
            lastCursorY = cursorY;

            for (int y = 0; y < gridView.getHeight(); y++) {
                // We do the background then the foreground so that double-wide
                // characters get the background set reasonably.

                for (int x = 0; x < gridView.getWidth(); x++) {
                    if (grid != null) {
                        c = grid.get(y, x);
                    } else {
                        c = gridView.get(y, x);
                    }
                    if (c.isDirty() || (refresh_all
                            && !c.getBackgroundColor().equals(
                                    getEmpty().getBackgroundColor()))) {
                        graphics.setPaint(c.getBackgroundColor());
                        graphics.fill(new Rectangle(x * bits.fontSglAdvance,
                                                    y * bits.fontHeight,
                                                    bits.fontSglAdvance,
                                                    bits.fontHeight));
                    }
                }
                final int fontHeightD2 = bits.fontHeight / 2;
                final int fontSglAdvanceD2 = bits.fontSglAdvance / 2;

                for (int x = 0; x < gridView.getWidth(); x++) {
                    if (grid != null) {
                        c = grid.get(y, x);
                        if (c.isDirty() || refresh_all) {
                            gridView.set(y, x, c);
                        }
                    } else {
                        c = gridView.get(y, x);
                    }

                    if (!c.isDirty() && !refresh_all) {
                        continue;
                    }
                    String cs = c.getSequence();
                    // c.setFont(this.displayFont);
                    // For double-wide characters, we can safely put a NUL
                    // byte in the second slot and it will never be displayed.
                    if (cs != null && !"\u0000".equals(cs)) {
                        int w = bits.metrics.stringWidth(cs);
                        w = bits.fontSglAdvance - w;
                        if (w < 0) {
                            w = 0;
                        } else {
                            w /= 2;
                        }
                        graphics.setBackground(c.getBackgroundColor());
                        graphics.setColor(c.getForegroundColor());
                        graphics.drawString(c.getSequence(),
                                        x * bits.fontSglAdvance + w,
                                        y * bits.fontHeight + bits.fontAscent);
                    }

                    c.setDirty(false);
                    if (c.getCellWalls() != null && !c.getCellWalls().isEmpty()) {
                        int x1 = x * bits.fontSglAdvance;
                        int y1 = y * bits.fontHeight;
                        graphics.setColor(c.getForegroundColor());
                        if (c.getCellWalls().contains(CellWalls.TOP)) {
                            graphics.drawLine(x1, y1,
                                                x1 + bits.fontSglAdvance -1, y1);
                        }
                        if (c.getCellWalls().contains(CellWalls.LEFT)) {
                            graphics.drawLine(x1, y1, x1,
                                                y1 + bits.fontHeight -1);
                        }
                        if (c.getCellWalls().contains(CellWalls.BOTTOM)) {
                            graphics.drawLine(x1, y1 + bits.fontHeight-1,
                                                x1 + bits.fontSglAdvance -1,
                                                y1 + bits.fontHeight-1);
                        }
                        if (c.getCellWalls().contains(CellWalls.RIGHT)) {
                            graphics.drawLine(x1 + bits.fontSglAdvance-1, y1,
                                                x1 + bits.fontSglAdvance-1,
                                                y1 + bits.fontHeight -1);
                        }
                        if (c.getCellWalls().containsAll(CellWalls.HORIZONTAL)) {
                            graphics.drawLine(x1, y1 + fontHeightD2,
                                                x1 + bits.fontSglAdvance -1,
                                                y1 + fontHeightD2);
                        }
                        if (c.getCellWalls().containsAll(CellWalls.VERTICAL)) {
                            graphics.drawLine(x1 + fontSglAdvanceD2, y1,
                                                x1 + fontSglAdvanceD2,
                                                y1 + bits.fontHeight -1);
                        }
                    }
                }
            }
            if (need_cursor) {
                c = gridView.get(cursorY, cursorX);
                if (cursorColor == null) {
                    graphics.setPaint(c.getForegroundColor());
                } else {
                    graphics.setPaint(cursorColor);
                }
                graphics.fill(new Rectangle(cursorX * bits.fontSglAdvance,
                                            cursorY * bits.fontHeight + bits.fontAscent,
                                            bits.fontSglAdvance -1,
                                            bits.fontHeight - bits.fontAscent -1));
            }
            refresh_all = false;
            long endTime = System.currentTimeMillis();
            if (this.displaySpeed == 0) {
                this.displaySpeed = endTime - startTime;
            } else {
                this.displaySpeed = (displaySpeed + endTime - startTime) / 2;
            }
            LOGGER.info("Panel update speed: {} ms / Average: {} ms",
                     endTime - startTime, displaySpeed);
        } catch(IndexOutOfBoundsException ex) {
            LOGGER.error("grid changed size during an update");
        } finally {
            if (grid != null) {
                synchronized(this) {
                    this.notifyAll();
                }
            }
        }
    }

    /**
     * Recalculate the font bits.
     *
     * <p>The logic this uses totally breaks down if a variable-width font is
     * used.
     *
     * <p>For a variable-width font, you'd need to walk every character you
     * plan to use, track the width, then use the max double-wide width or 2x
     * the max single-wide width... That is, if you plan to do the
     * single-width / double-width logic traditionally found on terminals.
     *
     * <p>If you want variable width fonts, it is probably best not to treat it
     * as a traditional double-wide character and to instead treat it as a
     * large single-width character -- so that <code>fontDblAdvance</code> and
     * <code>fontSglAdvance</code> are the same and <code>fontHasDouble</code>
     * is false.
     */
    protected void recalculateFontBits(Font check, FontBits bits) {
        if (this.getGraphics() == null) {
            return;
        }
        bits.font = check;
        bits.metrics = this.getGraphics().getFontMetrics(check);
        bits.fontAscent = bits.metrics.getMaxAscent();
        bits.fontDblAdvance = bits.metrics.getMaxAdvance();
        bits.fontSglAdvance = bits.metrics.charWidth('W');
        if (bits.fontDblAdvance == -1) {
            bits.fontDblAdvance = bits.fontSglAdvance;
        }
        bits.fontHasDouble = false;
        if (bits.fontDblAdvance >= bits.fontSglAdvance + bits.fontSglAdvance) {
            bits.fontHasDouble = true;
        }
        if (bits.fontHasDouble) {
            int sa = bits.fontDblAdvance / 2;
            if (sa >= bits.fontSglAdvance) {
                bits.fontSglAdvance = sa;
            } else {
                bits.fontDblAdvance = bits.fontSglAdvance * 2;
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
        //bits.fontHeight = bits.metrics.getMaxAscent() + bits.metrics.getMaxDescent()
        //                                    + bits.metrics.getLeading();
        bits.fontHeight = bits.metrics.getHeight() /* + 2 */;
    }
    /**
     * Refresh the window.
     */
    public void refresh() {
        synchronized(this) {
            this.refreshedCnt ++;
            repaint();
            try {
                this.wait();
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
    }

    public void flagFullRefresh() {
        this.refresh_all = true;
    }
    public void forceRefresh() {
        this.refresh_all = true;
        refresh();
    }
    /**
     * Refresh a row/line.
     * @param y the line to refresh
     */
    public void refreshLine(int y) {
        if (y < 0) { y = 0; }
        if (y > grid.getHeight()) { y = grid.getHeight(); }
        for (int x = 0; x < grid.getWidth(); x++) {
            grid.get(y, x).setDirty(true);
        }
    }

    /**
     * Refresh a box on the screen.
     *
     * @param height height of the box
     * @param width width of the box
     * @param y1 coordinate of the box
     * @param x1 coordinate of the box
     */
    public void refreshRegion(int height, int width, int y1, int x1) {
        if (y1 < 0) {
            y1 = 0;
        }
        if (x1 < 0) {
            x1 = 0;
        }

        if (height < 0) {
            height = grid.getHeight();
        }
        if (height + y1 >= grid.getHeight()) {
            height = grid.getHeight() - y1;
        }

        if (width < 0) {
            width = grid.getWidth();
        }
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

    protected FontBits resizeFontToFit(Font font) {
        // Rectangle r = this.getRootPane().getContentPane().getBounds();
        Rectangle r = this.getVisibleRect();
        float fsize = 0.5f;
        int idealAdvance = r.width / this.minX;
        int idealHeight = r.height / this.minY;
        //LOGGER.debug("ideal:{}, height:{}, idealAdvance, idealHeight");
        FontBits fbits = new FontBits();
        recalculateFontBits(font.deriveFont(fsize), fbits);
        if (idealAdvance <= fbits.fontSglAdvance || idealHeight <= fbits.fontHeight) {
            //LOGGER.debug("BOGUS advance:{}; height:{}",
                    //fontSglAdvance, fontHeight);
            // This is a real bogus size, but apparently we can't get
            // anything better
            return fbits;
        }
        FontBits lastBits = fbits;
        //LOGGER.debug("size:{}; advance:{}; height:{}",
        //        new Object[] {fsize, fontSglAdvance, fontHeight});
        while (idealAdvance >= fbits.fontSglAdvance && idealHeight >= fbits.fontHeight) {
            lastBits = fbits;
            fbits = new FontBits();
            recalculateFontBits(lastBits.font.deriveFont(fsize += 0.5f), fbits);
            //LOGGER.debug("size:{}; advance:{}; height:{}",
            //        new Object[] {fsize, fontSglAdvance, fontHeight});
        }
        return fbits;
    }

    /**
     * Resize the frame.
     *
     * @param frame frame to resize
     * @param fontSize font size to use
     * @deprecated
     */
    @Deprecated
    public void resizeFrame(JFrame frame, int fontSize) {
        /*
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
        */
    }
    /**
     * Resize the grid
     *
     * @param rows new rows
     * @param cols new columns
     */
    public void resizeGrid(int rows, int cols) {
        grid.setSize(rows, cols);
    }

    /**
     * Resize the grid to the window.
     */
    protected void resizeGridToWindow() {
        int xsize, ysize;
        Dimension d = this.getSize();
        xsize = d.width / bits.fontSglAdvance;
        ysize = d.height / bits.fontHeight;
        this.grid.setSize(ysize, xsize);
    }

    /**
     * Set a cell.
     *
     * <p>Note: This does nothing to ease the issues inherent in double-wide
     * characters. The background of a double-wide character needs to be set
     * individually, and the second half of the character needs the glyph
     * cleared or it will overwrite. -- This has particular implications
     * when changing an existing double-wide character, as the second-half
     * needs to be marked as dirty separately.
     *
     * @param y row number
     * @param x column number
     * @param cell cell definition
     */
    public void set(int y, int x, AwtCell cell) {
        AwtCell c = grid.get(y, x);
        c.set(cell);
        c.setDirty(true);
    }
    public void assign(int y, int x, AwtCell cell) {
        grid.set(y, x, cell);
    }

    /**
     * Set a cell to some common values.
     *
     * @param y row
     * @param x column
     * @param glyph sequence
     * @param back background color
     * @param fore foreground color
     */
    public void set(int y, int x, int glyph, Color back, Color fore) {
        grid.get(y, x).setCell(glyph, back, fore);
    }

    /**
     * Set a cell to some things.
     *
     * @param y row
     * @param x column
     * @param glyph sequence
     * @param attributes text attributes
     */
    public void set(int y, int x, int glyph,
                    Map<TextAttribute, Object> attributes) {
        grid.get(y, x).setCell(glyph, attributes);
    }

    /**
     * Set the empty/template cell
     * @param empty new empty cell
     */
    public void setEmpty(AwtCell empty) {
        if (empty != null) {
            this.empty = empty;
        }
    }
    /*
     * (non-Javadoc)
     * @see javax.swing.JComponent#setFont(java.awt.Font)
     */
    @Override
    public void setFont(Font font) {
        if (font == null) {
            font = bits.font;
        }
        if (this.getGraphics() != null) {
            FontBits fbits = null;
            fbits = resizeFontToFit(font);
            recalculateFontBits(font, fbits);
            setFont(fbits);
        }
        super.setFont(font);
        this.fontChanged = true;
    }

    private void setFont(FontBits fbits) {
        this.bits = fbits;
        super.setFont(fbits.font);
        Rectangle r = this.getVisibleRect();
        int newRows = r.height / fbits.fontHeight;
        int newCols = r.width / fbits.fontSglAdvance;
        grid.setSize(newRows, newCols);
        this.fontChanged = true;
    }

    /**
     * Process a window resize event.
     */
    public void windowResized() {
        FontBits fbits = resizeFontToFit(bits.font);
        setFont(fbits);
        forceRefresh();
    }

}
