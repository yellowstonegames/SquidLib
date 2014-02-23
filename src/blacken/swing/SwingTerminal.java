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
package com.googlecode.blacken.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.GraphicAttribute;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import com.googlecode.blacken.colors.ColorHelper;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.terminal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new Terminal using Swing.
 * 
 * @author yam655
 */
public class SwingTerminal extends AbstractTerminal
                    implements ComponentListener, MouseListener {
    static private final Logger LOGGER = LoggerFactory.getLogger(SwingTerminal.class);
    protected BlackenPanel gui;
    protected EventListener listener;
    protected JFrame frame;
    protected DropTarget dropTarget;
    protected String defaultFont = null;
    protected TerminalScreenSize defaultSize = TerminalScreenSize.SIZE_MEDIUM;

    /**
     * Create and initialize the function at once.
     *
     * @param name Window name
     * @param rows number of rows (0 is acceptable)
     * @param cols number of columns (0 is acceptable)
     * @param font Font name or path
     * @return new SwingTerminal
     */
    static public SwingTerminal initialize(String name, int rows, int cols, String font) {
        SwingTerminal terminal = new SwingTerminal();
        terminal.init(name, rows, cols, font);
        return terminal;
    }
    private boolean nowFullscreen = false;
    private Rectangle windowedBounds = null;
    private boolean inhibitFullScreen = false;
    private int lastModifier;
    private boolean windows7VerticalMaxFix = false;
    private int ignoreResize;

    /**
     * Create a new terminal
     */
    public SwingTerminal() {
        super();
    }

    @Override
    public void clear() {
        super.clear();
        gui.clear();
    }

    @Override
    public void setEmpty(TerminalCellLike empty) {
        super.setEmpty(empty);
        AwtCell awtempty = AwtCell.makeAwtFromTerminal(getEmpty());
        awtempty.setDirty(true);
        gui.setEmpty(awtempty);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // do nothing
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        gui.requestFocusInWindow();
        // LOGGER.debug("event: {}", e);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        listener.loadKey(BlackenKeys.RESIZE_EVENT);
        gui.requestFocusInWindow();
        if (this.ignoreResize > 0) {
            this.ignoreResize --;
        } else {
            try {
                if (System.getProperty("os.name", "Other").contains("Win")) {
                    // LOGGER.debug("Preping Windows 7 fix.");
                    this.windows7VerticalMaxFix = true;
                }
            } catch(SecurityException ex) {
                // When in doubt...
                // LOGGER.debug("Preping Windows 7 fix because of unknown OS.");
                this.windows7VerticalMaxFix = true;
            }
        }
    }

    @Override
    public void componentShown(ComponentEvent e) {
        gui.requestFocusInWindow();
        // LOGGER.debug("event: {}", e);
    }

    @Override
    public void copyFrom(TerminalViewInterface oterm, int numRows, int numCols,
                         int startY, int startX, int destY, int destX) {
        if (oterm == this) {
            this.moveBlock(numRows, numCols, startY, startX, destY, destX);
        } else {
            getGrid().copyFrom(oterm.getGrid(), numRows, numCols, startY, startX,
                           destY, destX, new TerminalCell.ResetCell());
            forceRefresh(numRows, numCols, destY, destX);
        }
    }

    @Override
    public void disableEventNotice(BlackenEventType event) {
        listener.unsetEnabled(event);
    }

    @Override
    public void disableEventNotices() {
        listener.clearEnabled();
    }

    @Override
    public void enableEventNotice(BlackenEventType event) {
        listener.setEnabled(event);
    }

    @Override
    @Deprecated
    public void enableEventNotices(EnumSet<BlackenEventType> events) {
        if (events == null) {
            events = EnumSet.allOf(BlackenEventType.class);
        }
        listener.setEnabled(events);
    }

    @Override
    public void setEventNotices(EnumSet<BlackenEventType> events) {
        if (events == null) {
            events = EnumSet.noneOf(BlackenEventType.class);
        }
        listener.setEnabled(events);
    }

    @Override
    public EnumSet<BlackenEventType> getEventNotices() {
        return listener.getEnabled();
    }

    private void forceRefresh(int numRows, int numCols, int startY, int startX) {
        Grid<TerminalCellLike> grid = getGrid();
        for (int y = startY; y < numRows + startY; y++) {
            for (int x = startX; x < numCols + startX; x++) {
                AwtCell acell = AwtCell.makeAwtFromTerminal(grid.get(y, x));
                gui.assign(y, x, acell);
                grid.get(y, x).setDirty(false);
            }
        }
    }

    @Override
    public boolean keyWaiting() {
        if (!gui.hasFocus()) {
            gui.requestFocusInWindow();
        }
        return listener.hasKeys();
    }

    @Override
    public int getch(int millis) {
        if (!gui.hasFocus()) {
            gui.requestFocusInWindow();
        }
        this.refresh();
        int ch;
        if (millis >= 0) {
            ch = listener.blockingPopKey(millis);
        } else {
            try {
                ch = listener.blockingPopKey();
            } catch (InterruptedException e) {
                ch = BlackenKeys.NO_KEY;
            }
        }
        if (ch == BlackenKeys.RESIZE_EVENT) {
            // Debounce RESIZE_EVENTs
            while (listener.peekKey() == BlackenKeys.RESIZE_EVENT) {
                listener.popKey();
            }
        }
        int activeModifier = this.lastModifier;
        if (BlackenKeys.isModifier(ch)) {
            this.lastModifier = ch;
        } else {
            this.lastModifier = BlackenKeys.NO_KEY;
        }
        if (ch == BlackenKeys.RESIZE_EVENT) {
            gui.windowResized();
            getGrid().setBounds(gui.getGridBounds());
        } else if (ch == BlackenKeys.KEY_ENTER) {
            // Set<BlackenModifier> mods = BlackenModifier.getAsSet(activeModifier);
            if (activeModifier == BlackenModifier.MODIFIER_KEY_ALT.getAsCodepoint()) {
                if (!this.inhibitFullScreen) {
                    this.setFullScreen(!this.getFullScreen());
                    ch = BlackenKeys.NO_KEY;
                }
            }
        }
        return ch;
    }

    @Override
    public int getch() {
        return getch(-1);
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.core.TerminalInterface#getLockingStates()
     */
    @Override
    public EnumSet<BlackenModifier> getLockingStates() {
        return listener.getLockingModifiers();
    }

    @Override
    public BlackenMouseEvent getmouse() {
        BlackenMouseEvent e = listener.popMouse();
        if (e == null) {
            return e;
        }
        return e;
    }

    @Override
    public BlackenWindowEvent getwindow() {
        BlackenWindowEvent e = listener.popWindow();
        return e;
    }

    @Override
    public void init(String name, int rows, int cols, TerminalScreenSize size,
            String... fonts) {
        if (this.defaultFont != null && (fonts == null || fonts.length==1 && fonts[0] == null)) {
            fonts = new String[] {defaultFont};
        }
        if (frame != null) {
            try {
                setFont(fonts);
            } catch (FontNotFoundException ex) {
                LOGGER.error("Failed to change font", ex);
            }
            resize(rows, cols);
            setCursorLocation(-1,-1);
            return;
        }
        super.init(name, rows, cols, size, fonts);
        frame = new JFrame(name);
        // frame.setIgnoreRepaint(true);
        frame.setFocusTraversalKeysEnabled(false);

        gui = new BlackenPanel();
        // gui.setIgnoreRepaint(true);
        gui.setDoubleBuffered(true);
        listener = new EventListener(gui);
        gui.setFocusTraversalKeysEnabled(false);
        gui.setRequestFocusEnabled(true);
        gui.setFocusCycleRoot(true);
        gui.addKeyListener(listener);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.setBackground(Color.BLACK);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(gui);
        frame.setCursor(null);
        frame.pack();

        AwtCell empty = AwtCell.makeAwtFromTerminal(null);

        Font fontObj = null;
        for (String font : fonts) {
            try {
                fontObj = findFont(font);
            } catch (FontNotFoundException ex) {
                LOGGER.error("Failed to set font", ex);
            }
        }
        if (fonts != null && (fonts.length == 0 || fonts[0] != null)) {
            try {
                // should never fail
                fontObj = findFont(null);
            } catch (FontNotFoundException ex1) {
                LOGGER.error("Failed to set backup font", ex1);
            }
        }

        if (fontObj == null) {
            // make Java compiler happy
            fontObj = new Font(Font.MONOSPACED, Font.PLAIN, 1);
        }
        gui.init(fontObj, rows, cols, empty);
        setCursorLocation(-1, -1);

        frame.setResizable(true);
        frame.addKeyListener(listener);
        frame.addMouseListener(listener);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(listener);
        frame.addMouseWheelListener(listener);
        frame.addWindowListener(listener);
        frame.addWindowFocusListener(listener);
        frame.addComponentListener(this);
        frame.addInputMethodListener(listener);

        if (size == null) {
            size = this.defaultSize;
            if (size == null) {
                size = TerminalScreenSize.SIZE_MEDIUM;
            }
        }
        switch(size) {
            case SIZE_FULLSCREEN:
            case SIZE_MAX:
                setSize(TerminalScreenSize.SIZE_MEDIUM);
                frame.setLocationRelativeTo(null);
                setSize(size);
                break;
            default:
                setSize(size);
                frame.setLocationRelativeTo(null);
                break;
        }

        frame.setVisible(true);
        gui.requestFocusInWindow();
    }

    @Override
    public void setSize(TerminalScreenSize size) {
        if (frame == null) {
            this.defaultSize = size;
            return;
        }
        Dimension screenSize = frame.getToolkit().getScreenSize();
        Insets insets = frame.getToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        int ys = screenSize.height - insets.top - insets.bottom;
        int xs = screenSize.width - insets.left - insets.right;
        if (xs / ys > 2 || ys / xs > 2) {
            if (xs > ys) {
                xs = ys;
            } else {
                ys = xs;
            }
        }
        Rectangle bounds = frame.getBounds();
        int x1 = bounds.x;
        int y1 = bounds.y;
        switch(size) {
            case SIZE_FULLSCREEN:
                setFullScreen(true);
                break;
            case SIZE_MAX:
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                break;
            default:
                int newX = (int)Math.floor(xs * size.getSize());
                int newY = (int)Math.floor(ys * size.getSize());
                if (x1 + newX > screenSize.width - insets.right) {
                    x1 = screenSize.width - insets.right - newX;
                }
                if (y1 + newY > screenSize.height - insets.top) {
                    y1 = screenSize.height - insets.top - newY;
                }
                frame.setBounds(x1, y1, newX, newY);
                break;
        }
    }

    @Override
    public boolean setFullScreen(boolean state) {
        if (state == this.nowFullscreen) {
            return this.nowFullscreen;
        }
        frame.setIgnoreRepaint(false);
        if (state) {
            this.windowedBounds = frame.getBounds();
            frame.setVisible(false);
            frame.removeNotify();
            frame.setUndecorated(true);
            frame.addNotify();
            frame.setResizable(false);
            frame.setSize(frame.getToolkit().getScreenSize().width, frame.getToolkit().getScreenSize().height);
            try {
                frame.setAlwaysOnTop(true);
            } catch(SecurityException e) {
                // do nothing
            }
            frame.setLocation(0, 0);
            frame.setVisible(true);
        } else {
            frame.setVisible(false);
            frame.removeNotify();
            frame.setUndecorated(false);
            frame.addNotify();
            frame.setResizable(true);
            try {
                frame.setAlwaysOnTop(false);
            } catch(SecurityException e) {
                // do nothing
            }
            frame.setBounds(windowedBounds);
            frame.setVisible(true);
            windowedBounds = null;
        }
        frame.setIgnoreRepaint(true);

        this.nowFullscreen = state;
        return this.nowFullscreen;
    }

    @Override
    public boolean getFullScreen() {
        return this.nowFullscreen;
    }

    @Override
    public void inhibitFullScreen(boolean state) {
        this.inhibitFullScreen = state;
    }

    @Override
    public void moveBlock(int numRows, int numCols, int origY, int origX,
                          int newY, int newX) {
        getGrid().moveBlock(numRows, numCols, origY, origX, newY, newX,
                       new TerminalCell.ResetCell());
        gui.moveBlock(numRows, numCols, origY, origX, newY, newX);
    }

    @Override
    public void quit() {
        if (frame != null && frame.isDisplayable()) {
            frame.dispose();
        }
        super.quit();
        gui = null;
        frame = null;
    }

    @Override
    public void refresh() {
        super.refresh();
        gui.refresh();
    }

    @Override
    public void refresh(int y, int x) {
        TerminalCellLike tcell = get(y, x);
        if (!tcell.isDirty()) {
            return;
        }
        AwtCell acell = AwtCell.makeAwtFromTerminal(tcell);
        gui.assign(y, x, acell);
        tcell.setDirty(false);
    }

    @Override
    public void doUpdate() {
        gui.refresh();
    }

    @Override
    public void resize(int rows, int cols) {
        if (this.getCursorX() >= cols || this.getCursorY() >= rows) {
            setCursorLocation(-1,-1);
        }
        getGrid().setSize(rows, cols);
        gui.resizeGrid(rows, cols);
        gui.windowResized();
    }

    @Override
    public void set(int y, int x, String sequence, Integer foreground,
                    Integer background, EnumSet<TerminalStyle> style, EnumSet<CellWalls> walls) {
        TerminalCellLike tcell = getGrid().get(y,x);
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
        AwtCell acell = AwtCell.makeAwtFromTerminal(tcell);
        gui.assign(y, x, acell);
        tcell.setDirty(false);
    }

    @Override
    public void set(int y, int x, TerminalCellLike tcell) {
        AwtCell acell = AwtCell.makeAwtFromTerminal(tcell);
        gui.assign(y, x, acell);
        Grid<TerminalCellLike> grid = getGrid();
        grid.get(y, x).set(tcell);
        grid.get(y, x).setDirty(false);
    }

    @Override
    public TerminalCellLike assign(int y, int x, TerminalCellLike tcell) {
        AwtCell acell = AwtCell.makeAwtFromTerminal(tcell);
        gui.assign(y, x, acell);
        Grid<TerminalCellLike> grid = getGrid();
        tcell.setDirty(false);
        return grid.set(y, x, tcell);
    }

    @Override
    public void setCursorLocation(int y, int x) {
        super.setCursorLocation(y, x);
        if (gui != null) {
            gui.moveCursor(y, x);
        }
    }

    private Font findFont(String font) throws FontNotFoundException {
        boolean tryInternal = false;
        String expectName = font;
        LOGGER.debug("Setting font to {}", font);
        if (font == null) {
            tryInternal = true;
            font = "resource:/fonts/DejaVuSansMono.ttf";
        }
        Font fontObj = null;
        if (font.contains(":")) {
            expectName = null;
            if (font.startsWith("resource:")) {
                font = font.substring("resource:".length());
            } else {
                throw new FontNotFoundException("Unknown URI: " +
                        font);
            }
            InputStream stream = null;
            try {
                stream = getClass().getResourceAsStream(font);
                fontObj = Font.createFont(Font.TRUETYPE_FONT, stream);
            } catch (FontFormatException ex) {
                if (!tryInternal) {
                    throw new FontNotFoundException("Font is invalid", ex);
                } else {
                    LOGGER.error("Failed to find font: {}", font, ex);
                }
            } catch (IOException ex) {
                if (!tryInternal) {
                    throw new FontNotFoundException("Font is not found", ex);
                } else {
                    LOGGER.error("Failed to find font: {}", font, ex);
                }
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        LOGGER.error("Failed to close font resource", ex);
                    }
                }
            }
        }
        if (fontObj == null) {
            if (tryInternal) {
                font = Font.MONOSPACED;
            }
            fontObj = new Font(font, Font.PLAIN, 1);
        }
        if (expectName != null) {
            // no good way to test the success of the change
            if (fontObj.getName().equals(expectName) || fontObj.getFamily().equals(expectName)) {
                // LOGGER.debug("Found expected font: {}", fontObj);
            } else {
                throw new FontNotFoundException("Font is not found");
            }
        } else {
            // LOGGER.info("Set font to {} / {}", fontObj.getFamily(), fontObj.getName());
        }
        return fontObj;
    }

    @Override
    public void setFont(String font, boolean checkFont) throws FontNotFoundException {
        Font fontObj = findFont(font);
        if (!checkFont) {
            if (gui == null) {
                this.defaultFont = font;
            } else {
                gui.setFont(fontObj);
            }
        }
    }

    @Override
    public ColorPalette setPalette(ColorPalette palette) {
        ColorPalette old = super.setPalette(palette);
        AwtCell.setPalette(palette);
        return old;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (this.nowFullscreen) {
            return;
        }
        if ((frame.getExtendedState()) != Frame.NORMAL) {
            return;
        }
        if (this.windows7VerticalMaxFix) {
            // LOGGER.debug("Tweaking window.");
            Rectangle b = frame.getBounds();
            this.ignoreResize += 2;
            frame.setSize(b.width-1, b.height);
            frame.setSize(b.width, b.height);
            this.windows7VerticalMaxFix = false;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
