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

/**
 * Default callback used by the 
 * {@link SingleLine#getString(TerminalInterface, int, int, int, CodepointCallbackInterface)}
 * function.
 *
 * @author Steven Black
 */
public class DefaultSingleLineCallback implements CodepointCallbackInterface {
    private static DefaultSingleLineCallback instance = new DefaultSingleLineCallback();
    private DefaultSingleLineCallback() {
        // do nothing
    }
    static public DefaultSingleLineCallback getInstance() {
        return instance;
    }

    @Override
    public int handleCodepoint(int codepoint) {
        if (codepoint == '\r' || codepoint == BlackenKeys.KEY_NP_ENTER ||
                codepoint == BlackenKeys.KEY_ENTER) {
            codepoint = BlackenKeys.CMD_END_LOOP;
        } else if (codepoint == '\t' || codepoint == BlackenKeys.KEY_TAB) {
            codepoint = BlackenKeys.CMD_END_LOOP;
        } else if (codepoint == '\b') {
            codepoint = BlackenKeys.KEY_BACKSPACE;
        }
        return codepoint;
    }

    @Override
    public boolean handleMouseEvent(BlackenMouseEvent mouse) {
        return false;
    }

    @Override
    public boolean handleWindowEvent(BlackenWindowEvent window) {
        return false;
    }

    @Override
    public void handleResizeEvent() {
        // do nothing
    }

}
