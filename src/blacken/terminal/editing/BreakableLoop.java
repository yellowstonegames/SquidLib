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

import com.googlecode.blacken.terminal.BlackenKeys;
import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;
import com.googlecode.blacken.terminal.TerminalViewInterface;

/**
 * This is a helper class for when you want to perform some sort of looping
 * operation but not lock out user-input.
 *
 * @author Steven Black
 */
public class BreakableLoop {
    /**
     * The default amount of time to wait for keystrokes before continuing.
     */
    public static final int DEFAULT_FREQUENCY_MILLIS = 200;
    private TerminalViewInterface term;
    private CodepointCallbackInterface callback = DefaultSingleLineCallback.getInstance();
    private Steppable stepper;
    private int millis = DEFAULT_FREQUENCY_MILLIS;
    /**
     * Create a new BreakableLoop with the default frequency.
     *
     * @param term terminal to check for keystrokes
     * @param stepper action to be performed
     */
    public BreakableLoop(TerminalViewInterface term, Steppable stepper) {
        this.term = term;
        this.stepper = stepper;
    }
    /**
     * Create a BreakableLoop with an explicit frequency and key handler.
     *
     * @param term
     * @param frequencyMillis
     * @param callback
     * @param stepper
     */
    public BreakableLoop(TerminalViewInterface term, int frequencyMillis,
            CodepointCallbackInterface callback, Steppable stepper) {
        this.term = term;
        this.stepper = stepper;
        if (callback != null) {
            this.callback = callback;
        }
        if (frequencyMillis > -1) {
            this.millis = frequencyMillis;
        }
    }

    /**
     * The BreakableLoop can be run interactively or driven by the program.
     *
     * <p>This is the same as:
     * <pre>
     * drive(codePoint, null, null);
     * </pre>
     *
     * @param codePoint
     * @see #drive(int, BlackenMouseEvent, BlackenWindowEvent)
     * @return
     */
    public boolean drive(int codePoint) {
        return drive(codePoint, null, null);
    }

    /**
     * The BreakableLoop can be run interactively or driven by the program.
     *
     * @param codePoint
     * @param mouseEvent only used for KEY_MOUSE_EVENT
     * @param windowEvent only used for KEY_WINDOW_EVENT
     * @see TerminalViewInterface#getmouse()}
     * @see TerminalViewInterface#getwindow()}
     * @return
     */
    public boolean drive(int codePoint, BlackenMouseEvent mouseEvent,
            BlackenWindowEvent windowEvent) {
        boolean doQuit = false;
        int ec = BlackenKeys.NO_KEY;
        if (codePoint == BlackenKeys.KEY_MOUSE_EVENT) {
            if (mouseEvent == null) {
                throw new NullPointerException("KEY_MOUSE_EVENT requires a BlackenMouseEvent object");
            }
            callback.handleMouseEvent(mouseEvent);
        } else if (codePoint == BlackenKeys.KEY_WINDOW_EVENT) {
            if (windowEvent == null) {
                throw new NullPointerException("KEY_WINDOW_EVENT requires a BlackenWindowEvent object");
            }
            callback.handleWindowEvent(term.getwindow());
        } else if (codePoint == BlackenKeys.RESIZE_EVENT) {
            callback.handleResizeEvent();
        } else {
            ec = callback.handleCodepoint(codePoint);
        }
        switch (ec) {
            case BlackenKeys.CMD_END_LOOP:
                doQuit = true;
                break;
            case BlackenKeys.RESIZE_EVENT:
                // I have no idea why this would be returned
                callback.handleResizeEvent();
                break;
        }
        stepper.step();
        return doQuit;
    }
    /**
     * Run the stepper, checking for keystrokes.
     *
     * <p>This function has two purposes:
     * <ul>
     * <li>Call {@link Steppable#step()} to perform action.
     * <li>Watch for keycodes, calling {@link CodepointCallbackInterface} as
     *     needed
     * </ul>
     *
     * <p>This returns when either the call to step() returns false or when
     * the CodepointCallbackInterface sends a {@link BlackenKeys#CMD_END_LOOP}
     * command.
     */
    public void run() {
        int cp;
        int i = 0;
        boolean doQuit = false;
        boolean firstPass = true;
        while (!doQuit && !stepper.isComplete()) {
            if (firstPass) {
                // Get some action before the getch() sleep
                stepper.step();
                firstPass = false;
            }
            cp = term.getch(millis);
            BlackenMouseEvent mouseEvent = null;
            BlackenWindowEvent windowEvent = null;
            if (cp == BlackenKeys.KEY_MOUSE_EVENT) {
                mouseEvent = term.getmouse();
            } else if (cp == BlackenKeys.KEY_WINDOW_EVENT) {
                windowEvent = term.getwindow();
            }
            doQuit = drive(cp, mouseEvent, windowEvent);
        }

    }
}
