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

import com.googlecode.blacken.grid.Grid;


/**
 * An unbound terminal -- no user-interface attached
 * 
 * @author Steven Black
 */
 public class UnboundTerminal extends AbstractTerminal {

    /**
     * Create and initialize the function at once.
     * 
     * @param name Window name
     * @param rows number of rows (0 is acceptable)
     * @param cols number of columns (0 is acceptable)
     * @param font Font name or path
     * @return new SwingTerminal
     */
    static public UnboundTerminal initialize(String name, int rows, int cols, String font) {
        UnboundTerminal terminal = new UnboundTerminal();
        terminal.init(name, rows, cols, font);
        return terminal;
    }

    /**
     * Create a new terminal
     */
    public UnboundTerminal() {
        super();
    }

    @Override
    public void disableEventNotice(BlackenEventType event) {
        // do nothing
    }
    
    @Override
    public void disableEventNotices() {
        // do nothing
    }

    @Override
    public void enableEventNotice(BlackenEventType event) {
        // do nothing
    }

    @Override
    @Deprecated
    public void enableEventNotices(EnumSet<BlackenEventType> events) {
        // do nothing
    }

    @Override
    public int getch() {
        this.refresh();
        return BlackenKeys.NO_KEY;
    }

    @Override
    public int getch(int millis) {
        this.refresh();
        return BlackenKeys.NO_KEY;
    }

    @Override
    public boolean keyWaiting() {
        return false;
    }

    @Override
    public EnumSet<BlackenModifier> getLockingStates() {
        return null;
    }

    @Override
    public BlackenMouseEvent getmouse() {
        return null;
    }

    @Override
    public String getString(int y, int x, int length) {
        setCursorLocation(y, x);
        return "";
    }

    @Override
    public BlackenWindowEvent getwindow() {
        return null;
    }

    @Override
    public void set(int y, int x, TerminalCellLike tcell) {
        Grid<TerminalCellLike> grid = getGrid();
        grid.get(y, x).set(tcell);
        grid.get(y, x).setDirty(false);
        
    }

    @Override
    public boolean setFullScreen(boolean state) {
        return false;
    }
    @Override
    public boolean getFullScreen() {
        return false;
    }

    @Override
    public void setSize(TerminalScreenSize size) {
        // do nothing (legal)
    }

    @Override
    public void refresh(int y, int x) {
        getGrid().get(y, x).setDirty(false);
    }

    @Override
    public void setEventNotices(EnumSet<BlackenEventType> events) {
        // do nothing
    }

    @Override
    public EnumSet<BlackenEventType> getEventNotices() {
        return EnumSet.noneOf(BlackenEventType.class);
    }

}
