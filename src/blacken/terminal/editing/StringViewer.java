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

package com.googlecode.blacken.terminal.editing;

import com.googlecode.blacken.colors.ColorHelper;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.terminal.BlackenEventType;
import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenMouseButton;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.TerminalCellLike;
import com.googlecode.blacken.terminal.TerminalViewInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steven Black
 */
public class StringViewer implements Steppable, CodepointCallbackInterface {
    static private final Logger LOGGER = LoggerFactory.getLogger(StringViewer.class);
    private TerminalViewInterface term;
    private CodepointCallbackInterface secondaryCallback = null;
    private int startLine = 0;
    private int foreground;
    private int background;
    private String[] lines;
    private Integer maxCol = null;
    public StringViewer(TerminalViewInterface term, String message) {
        this.term = term;
        this.lines = message.split("\n");
        background = term.getEmpty().getBackground();
        foreground = ColorHelper.makeVisible(background);
    }
    public StringViewer(TerminalViewInterface term, String message, CodepointCallbackInterface callback) {
        this.term = term;
        this.lines = message.split("\n");
        this.secondaryCallback = callback;
        background = term.getEmpty().getBackground();
        foreground = ColorHelper.makeVisible(background);
    }
    public int getLines() {
        return lines.length;
    }
    /**
     * This gets the approximate maximum column.
     *
     * <p>This ignores the actual number of codepoints in a string (the true
     * length of the string) in favor of the number of <code>char</code> units
     * in the String. This means it is off by one for every codepoint in the
     * string that is outside of the Basic Multilingual Plane.
     *
     * <p>It is probably good enough in practice. If Java cached the actual
     * size of the string (in codepoints) instead of just the number of UTF-16
     * 'char' values we could get a more accurate value. Java conveniently
     * highlights the fact that this is <em>not cached</em> by lacking a
     * zero-argument String.codePointCount function.
     *
     * @return size of the largest line in UTF-16 units
     */
    public int getMaxColumn() {
        if (maxCol == null) {
            maxCol = 0;
            for (String line : lines) {
                if (maxCol < line.length()) {
                    maxCol = line.length();
                }
            }
        }
        return maxCol;
    }
    public void setColor(int foreground, int background) {
        this.foreground = foreground;
        this.background = background;
        TerminalCellLike cell = term.getEmpty().clone();
        cell.setBackground(background);
        cell.setForeground(foreground);
        term.setEmpty(cell);
    }
    public void setColor(String foreground, String background) {
        ColorPalette palette = term.getBackingTerminal().getPalette();
        int fg = palette.getColorOrIndex(foreground);
        int bg = palette.getColorOrIndex(background);
        setColor(fg, bg);
    }
    public void run() {
        BreakableLoop looper = new BreakableLoop(term, BreakableLoop.DEFAULT_FREQUENCY_MILLIS, this, this);
        looper.run();
    }

    @Override
    public int getStepCount() {
        return -1;
    }

    @Override
    public int getCurrentStep() {
        return -1;
    }

    @Override
    public void step() {
        term.clear();
        Regionlike bounds = term.getBounds();
        if (startLine < 0) {
            startLine = 0;
        } else if (startLine >= lines.length) {
            startLine = lines.length -1;
        }
        for (int y = 0; y < term.getHeight(); y++) {
            String msg = "";
            if (lines.length > startLine + y) {
                msg = lines[startLine + y];
            }
            if (msg.length() > term.getWidth()) {
                // XXX Perhaps add column offset, too?
                msg = msg.substring(0, term.getWidth());
            }
            SingleLine.putString(term, y  + bounds.getY(), bounds.getX(),
                    msg, foreground, background);
        }
    }

    @Override
    public int handleCodepoint(int codepoint) {
        if (this.secondaryCallback != null) {
            codepoint = secondaryCallback.handleCodepoint(codepoint);
        }
        switch (codepoint) {
            case ' ':
            case BlackenKeys.KEY_PAGE_DOWN:
            case BlackenKeys.KEY_KP_PAGE_DOWN:
            case BlackenKeys.KEY_NP_3:
                startLine += term.getHeight();
                codepoint = BlackenKeys.NO_KEY;
                break;
            case BlackenKeys.KEY_BACKSPACE:
            case BlackenKeys.KEY_KP_PAGE_UP:
            case BlackenKeys.KEY_PAGE_UP:
            case BlackenKeys.KEY_NP_9:
                startLine -= term.getHeight();
                codepoint = BlackenKeys.NO_KEY;
                break;
            case BlackenKeys.KEY_KP_DOWN:
            case BlackenKeys.KEY_DOWN:
            case BlackenKeys.KEY_NP_2:
            case BlackenKeys.KEY_ENTER:
                startLine ++;
                codepoint = BlackenKeys.NO_KEY;
                break;
            case BlackenKeys.KEY_KP_UP:
            case BlackenKeys.KEY_UP:
            case BlackenKeys.KEY_NP_8:
                startLine --;
                codepoint = BlackenKeys.NO_KEY;
                break;
            case BlackenKeys.KEY_KP_HOME:
            case BlackenKeys.KEY_HOME:
            case BlackenKeys.KEY_NP_7:
                startLine = 0;
                codepoint = BlackenKeys.NO_KEY;
                break;
            case BlackenKeys.KEY_KP_END:
            case BlackenKeys.KEY_END:
            case BlackenKeys.KEY_NP_1:
                startLine = lines.length - term.getHeight();
                codepoint = BlackenKeys.NO_KEY;
                break;
            case 'q':
            case 'Q':
            case BlackenKeys.KEY_ESCAPE:
                codepoint = BlackenKeys.CMD_END_LOOP;
                break;
        }
        return codepoint;
    }

    @Override
    public boolean handleMouseEvent(BlackenMouseEvent mouse) {
        boolean ret = false;
        if (this.secondaryCallback != null) {
            ret = secondaryCallback.handleMouseEvent(mouse);
        }
        if (!ret) {
            if (mouse.getType() == BlackenEventType.MOUSE_WHEEL) {
                //LOGGER.debug("Mouse Event: {}", mouse);
                if (mouse.getActingButton() == BlackenMouseButton.WHEEL_UP) {
                    this.startLine -= mouse.getClickCount();
                } else if (mouse.getActingButton() == BlackenMouseButton.WHEEL_DOWN) {
                    this.startLine += mouse.getClickCount();
                }
            }
        }
        return ret;
    }

    @Override
    public boolean handleWindowEvent(BlackenWindowEvent window) {
        boolean ret = false;
        if (this.secondaryCallback != null) {
            ret = secondaryCallback.handleWindowEvent(window);
        }
        return ret;
    }

    @Override
    public void handleResizeEvent() {
        if (this.secondaryCallback != null) {
            secondaryCallback.handleResizeEvent();
        }
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
