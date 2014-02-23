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
package com.googlecode.blacken.terminal;

import java.util.EnumSet;

import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.Regionlike;

/**
 * The interface for terminal-like views.
 * 
 * @author Steven Black
 */
public interface TerminalInterface extends TerminalViewInterface {
    @Override
    public TerminalCellLike assign(int y, int x, TerminalCellLike cell);
    @Override
    public void clear();
    @Override
    public void clear(TerminalCellLike empty);
    @Override
    public void copyFrom(TerminalViewInterface oterm, int numRows, int numCols,
                  int startY, int startX, int destY, int destX);
    /**
     * Disable a type of event notice.
     * 
     * <p>Only <code>event</code> is disabled, other events remain in current 
     * state.</p>
     * 
     * @param event Event type to disable
     */
    public void disableEventNotice(BlackenEventType event);
    /**
     * Disable all event notices
     */
    public void disableEventNotices();
    /**
     * Enable a specific event notice
     * 
     * <p>Only <code>event</code> is enabled, other events remain in current 
     * state.</p>
     * 
     * @param event new event type to enable
     */
    public void enableEventNotice(BlackenEventType event);
    /**
     * Set specific event notices
     * 
     * <p>This changes all event notices to match <code>events</code>.</p>
     *  
     * @param events new event mask; null for <em>all</em> notices
     * @deprecated Use {@link #setEventNotices(java.util.EnumSet)} instead.
     */
    public void enableEventNotices(EnumSet<BlackenEventType> events);
    /**
     * Set specific event notices
     *
     * <p>This changes all event notices to match <code>events</code>.</p>
     *
     * @param events new event mask; null for <em>no</em> notices
     */
    public void setEventNotices(EnumSet<BlackenEventType> events);
    /**
     * Get the current set of event notices.
     * @return current notices
     */
    public EnumSet<BlackenEventType> getEventNotices();
    @Override
    public TerminalCellLike get(int y, int x);
    @Override
    public int getch();
    @Override
    public int getch(int millis);
    @Override
    public boolean keyWaiting();
    @Override
    public Regionlike getBounds();
    /**
     * Get the cursor location
     * 
     * @return cursor location
     * @deprecated migrating to {@link #getCursorPosition()}
     */
    @Deprecated
    public int[] getCursorLocation();

    @Override
    public Positionable getCursorPosition();
    @Override
    public int getCursorX();
    @Override
    public int getCursorY();
    @Override
    public TerminalCellLike getEmpty();
    @Override
    public int getHeight();
    @Override
    public Grid<TerminalCellLike> getGrid();

    @Override
    public EnumSet<BlackenModifier> getLockingStates();

    @Override
    public BlackenMouseEvent getmouse();
    /**
     * Get a copy of the palette used.
     * 
     * @return a valid ColorPalette object
     */
    public ColorPalette getPalette();
    @Override
    public String getString(int y, int x, int length);

    @Override
    public BlackenWindowEvent getwindow();

    @Override
    public int getWidth();

    /**
     * Get the current terminal max Y size
     * 
     * @return terminal's max Y size
     * @deprecated Use getHeight() instead.
     */
    @Deprecated
    public int gridHeight();
    /**
     * Get the current terminal max X size
     * 
     * @return terminal's max X size
     * @deprecated Use getWidth() instead.
     */
    @Deprecated
    public int gridWidth();

    /**
     * Initialize the terminal with a specific window name and size.
     * 
     * @param name window name
     * @param rows terminal rows
     * @param cols terminal columns
     */
    public void init(String name, int rows, int cols);

    /**
     * Initialize the terminal with a specific window name, size, and font
     * 
     * @param name window name
     * @param rows terminal rows
     * @param cols terminal columns
     * @param font the fonts to try
     */
    public void init(String name, int rows, int cols, String... font);
    public void init(String name, int rows, int cols, TerminalScreenSize size, String... font);
    public void init(String name, int rows, int cols, TerminalScreenSize size);

    @Override
    public void moveBlock(int numRows, int numCols, int origY, int origX, 
                          int newY, int newX);
    
    /**
     * Quit the backing window and the application if it was the last one.
     */
    public void quit();
    @Override
    public void refresh();
    @Override
    public void refresh(int y, int x);
    /**
     * Resize the terminal without resizing the window
     * @param rows
     * @param cols 
     */
    public void resize(int rows, int cols);
    @Override
    public void set(int y, int x, String sequence, 
                    Integer foreground, Integer background,
                    EnumSet<TerminalStyle> style, EnumSet<CellWalls> walls);

    @Override
    public void set(int y, int x, String sequence, Integer foreground, 
            Integer background);

    @Override
    public void set(int y, int x, TerminalCellLike cell);
    @Override
    public void setCursorLocation(int y, int x);
    /**
     * Set the cursor position.
     *
     * @param position [y, x]
     * @deprecated migrating to {@link #setCursorPosition(com.googlecode.blacken.grid.Positionable)}
     */
    @Deprecated
    public void setCursorLocation(int[] position);
    @Override
    public void setCursorPosition(Positionable position);
    @Override
    public void setEmpty(TerminalCellLike empty);
    /**
     * Set the font to a name or a path
     *
     * <p>This can be called before calling init, but if you do and you specify
     * a different font to init, that font will be used instead. (If you pass
     * (String)null or use one of the init forms without a font parameter it
     * will use the last found font.
     *
     * @param font name or path; default MONOSPACE if null
     * @param checkFont when true, do not actually set the font; just check font presence
     * @throws FontNotFoundException requested font was not found
      */
    public void setFont(String font, boolean checkFont) throws FontNotFoundException;
    /**
     * Set the font to a name or a path
     *
     * <p>This can be called before calling init, but if you do and you specify
     * a different font to init, that font will be used instead. (If you pass
     * (String)null or use one of the init forms without a font parameter it
     * will use the last found font.
     *
     * @param font a set of fonts to try in order; default if null
     * @return the font used.
     * @throws FontNotFoundException requested font was not found
      */
    public String setFont(String... font) throws FontNotFoundException;

    /**
     * Set the palette, performing any additional implementation-specific
     * backing logic as needed.
     * 
     * <p>If the passed in palette is null, it does not clear out the palette,
     * but instead reparses the existing palette for changes. Implementations
     * may extend this reparsing to perform back-end clean-up. This means that
     * if you modify an in-use palette, you may not see changes until you
     * <code>setPalette(null)</code>.</p>
     * 
     * <p>This will try to find raw colors and assign them to the palette.
     * When shrinking the palette, it will either try to find the color in a
     * different location in the new palette or it will convert the indexed
     * color in to a raw color.</p>
     * 
     * @param palette new palette or null
     * @return old palette
     */
    public ColorPalette setPalette(ColorPalette palette);
    /**
     * Use {@link #coerceToPalette(ColorPalette, int, int)} instead.
     * 
     * <p>This does a lot more than just set the palette. We made that more 
     * explicit by changing the name.</p>
     * 
     * @param palette
     * @param white
     * @param black
     * @deprecated Use {@link #coerceToPalette(ColorPalette, Integer, Integer)} instead
     * @return 
     */
    @Deprecated
    public ColorPalette setPalette(ColorPalette palette, int white, 
            int black);
    /**
     * Set the palette, converting existing colors to the palette or to
     * white or black.
     * 
     * <p>This makes sure that any color present is guaranteed to be a part
     * of the palette.
     * 
     * @param palette new palette
     * @param white index to use for white or null
     * @param black index to use for black or null
     * @return old palette
     * @since 1.1
     */
    public ColorPalette coerceToPalette(ColorPalette palette, Integer white,
            Integer black);

    /**
     * Set the palette, converting existing colors to the palette or to
     * white or black.
     *
     * <p>This makes sure that any color present is guaranteed to be a part
     * of the palette.<p>
     *
     * @param palette new palette
     * @param white name of white or null
     * @param black name of black or null
     * @return old palette
     * @since 1.1
     */
    public ColorPalette coerceToPalette(ColorPalette palette, String white,
            String black);

    /**
     * This attempts to switch to full-screen mode.
     *
     * <p>Not all TerminalInterface implementations can support FullScreen mode.
     * Some TerminalInterfaces are implicitly FullScreen. The return value is
     * a flag indicating the current FullScreen mode state. It is not an
     * error (or even an exceptional condition) to ignore your request.</p>
     *
     * @param state true to request full-screen; false to request windowed
     * @return true if now full-screen; false if now windowed
     * @since 1.1
     */
    public boolean setFullScreen(boolean state);

    /**
     * Get the current full-screen state.
     * @return true if now full-screen; false if now windowed
     * @since 1.1
     */
    public boolean getFullScreen();
    /**
     * Normally, Alt-Enter (or the like) automatically triggers full-screen
     * mode without application intervention. This prohibits that behavior.
     *
     * <p>Not all TerminalInterfaces may be able to support this behavior.</p>
     *
     * @param state true to inhibit Alt-Enter; false to support it.
     * @since 1.1
     */
    public void inhibitFullScreen(boolean state);

    @Deprecated
    public TerminalInterface getBackingTerminalInterface();

    @Override
    public TerminalInterface getBackingTerminal();

    @Override
    TerminalViewInterface getBackingTerminalView();

    /**
     * Set the physical window size (if possible).
     *
     * <p>It is legal to call this before calling init().
     *
     * @param size size of the window
     */
    public void setSize(TerminalScreenSize size);

    @Override
    public void doUpdate();

}
