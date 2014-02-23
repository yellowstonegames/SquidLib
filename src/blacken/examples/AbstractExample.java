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
package com.googlecode.blacken.examples;

import com.googlecode.blacken.colors.ColorNames;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.swing.SwingTerminal;
import com.googlecode.blacken.terminal.CursesLikeAPI;
import com.googlecode.blacken.terminal.TerminalInterface;

/**
 * An abstract example class.
 * 
 * @author Steven Black
 * @deprecated No longer used.
 */
@Deprecated
public abstract class AbstractExample {
    /**
     * TerminalInterface used by the example
     */
    protected CursesLikeAPI term;
    /**
     * ColorPalette used by the example
     */
    protected ColorPalette palette;
    /**
     * Whether to quit the loop or not
     */
    protected boolean quit;

    /**
     * Tell the loop to quit.
     * 
     * @param quit new quit status
     */
    public void setQuit(boolean quit) {
        this.quit = quit;
    }
    /**
     * Get the quit status.
     * 
     * @return whether we should quit
     */
    public boolean getQuit() {
        return quit;
    }
    
    /**
     * Initialize the example
     * 
     * @param term alternate TerminalInterface to use
     * @param palette alternate ColorPalette to use
     */
    public void init(TerminalInterface term, ColorPalette palette) {
        if (term == null) {
            term = new SwingTerminal();
            term.init("Example Program", 25, 80);
        }
        this.term = new CursesLikeAPI(term);
        if (palette == null) {
            palette = new ColorPalette();
            palette.addAll(ColorNames.XTERM_256_COLORS, false);
            palette.putMapping(ColorNames.SVG_COLORS);
        } 
        this.palette = palette;
        this.term.setPalette(palette);
    }
    /**
     * Quit the application.
     * 
     * <p>This calls quit on the underlying TerminalInterface.</p>
     */
    public void quit() {
        term.quit();
    }

    /**
     * The main loop for this example.
     * 
     * @return whether to quit or not
     */
    public abstract boolean loop();
    /**
     * Display the help text within the window.
     */
    public abstract void help();
    /**
     * @param args command-line arguments
     * @param that example instance
     */
    public static void main(String[] args, AbstractExample that) {
        that.init(null, null);
        that.loop();
        that.quit();
    }

}
