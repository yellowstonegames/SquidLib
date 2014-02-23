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

import com.googlecode.blacken.terminal.BlackenMouseEvent;
import com.googlecode.blacken.terminal.BlackenWindowEvent;

/**
 *
 * Handle generic case of monitoring codepoints entered in another function
 *
 * <p>The power here is that the returned codepoints may contain (supported)
 * logical key commands instead of just remapping to a different key. As an
 * example, the {@link BlackenKeys#CMD_CMD_END_LOOP} codepoint is used to
 * terminate the function.
 *
 * @author Steven Black
 */
public interface CodepointCallbackInterface {
    /**
     * While processing keys in another function, this function is called
     * first with codepoints so that they can be processed.
     *
     * <p>Returned codepoints that may be processed specially include:
     * <ul>
     * <li> NO_KEY : the original codepoint is ignored
     * <li> KEY_BACKSPACE : a destructive backspace is performed
     * <li> CMD_END_LOOP : end the loop (possibly with data)
     * <li> (other key code) : process normally
     * </ul>
     *
     * @param codepoint Unicode codepoint to process
     * @return codepoint to replace it with
     */
    public int handleCodepoint(int codepoint);
    /**
     *
     * @param mouse
     * @return true if the event is complete; false to keep processing
     */
    public boolean handleMouseEvent(BlackenMouseEvent mouse);
    /**
     *
     * @param window
     * @return true if the event is complete; false to keep processing
     */
    public boolean handleWindowEvent(BlackenWindowEvent window);
    /**
     * This is called for any RESIZE_EVENT.
     */
    public void handleResizeEvent();
}
