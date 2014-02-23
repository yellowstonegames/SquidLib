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
package com.googlecode.blacken.terminal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * The window event
 * 
 * @author yam655
 */
public class BlackenWindowEvent {
    private BlackenEventType type;
    private String name;
    private String oppositeName;
    private EnumSet<BlackenWindowState> newState;
    private EnumSet<BlackenWindowState> oldState;
    
    /**
     * Get the window state strings
     * @param set window state
     * @return list of window states
     */
    public static List<String> 
    getWindowStateStrings(EnumSet<BlackenWindowState> set) {
        List<String> ret = new ArrayList<String>();
        for (BlackenWindowState s : set) {
            ret.add(s.name());
        }
        return ret;
    }

    /**
     * Get the window state strings
     * @param set window state
     * @return buffer containing window states
     */
    public static StringBuffer 
    getWindowStateString(EnumSet<BlackenWindowState> set) {
        List<String> base = getWindowStateStrings(set);
        if (base == null) {
            return null;
        }
        StringBuffer keybuf = new StringBuffer();
        for (String name : base) {
            if (keybuf.length() != 0) {
                keybuf.append('+');
            }
            keybuf.append(name);
        }
        if (keybuf.length() == 0) {
            keybuf.append("(none)"); //$NON-NLS-1$
        }
        return keybuf;
    }
    
    /**
     * Describe the window event
     */
    @Override
    public String toString() {
        return String.format("Window: %s now: %s (was: %s) now: %s (was: %s)",  //$NON-NLS-1$
                             type.name(), name, oppositeName,
                             getWindowStateString(newState),
                             getWindowStateString(oldState));
    }

    /**
     * Create a new window event
     * @param type event type
     */
    public BlackenWindowEvent(BlackenEventType type) {
        this.type = type;
    }

    /**
     * Set the name
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the opposite name
     * @param oppositeName the opposite name
     */
    public void setOppositeName(String oppositeName) {
        this.oppositeName = oppositeName;
    }

    /**
     * Set the new state
     * @param set the new state
     */
    public void setNewState(EnumSet<BlackenWindowState> set) {
        this.newState = set;
    }

    /**
     * Set the old state
     * @param set the old state
     */
    public void setOldState(EnumSet<BlackenWindowState> set) {
        this.oldState = set;
    }

    /**
     * Get the event type
     * @return the event type
     */
    public BlackenEventType getType() {
        return type;
    }

    /**
     * Set the event type
     * @param type the event type
     */
    public void setType(BlackenEventType type) {
        this.type = type;
    }

    /**
     * Get the name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the opposite name
     * @return the opposite name
     */
    public String getOppositeName() {
        return oppositeName;
    }

    /**
     * Get the new state
     * @return the new state
     */
    public EnumSet<BlackenWindowState> getNewState() {
        return newState;
    }

    /**
     * Get the old state
     * @return the old state
     */
    public EnumSet<BlackenWindowState> getOldState() {
        return oldState;
    }

}
