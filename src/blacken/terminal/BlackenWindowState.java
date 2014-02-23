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
 * Window state
 * 
 * @author yam655
 */
public enum BlackenWindowState {
    /**
     * The window is iconified
     */
    ICONIFIED, 
    /**
     * The window is maximized horizontally
     */
    MAXIMIZED_HORIZ, 
    /**
     * The window is maximized vertically
     */
    MAXIMIZED_VERT;

    /**
     * get the state strings
     * @param set window state
     * @return state strings
     */
    public static List<String> 
    getStateStrings(EnumSet<BlackenWindowState> set) {
        List<String> ret = new ArrayList<String>();
        for (BlackenWindowState s : set) {
            ret.add(s.name());
        }
        return ret;
    }

    /**
     * get the state string
     * @param set window state
     * @return state string
     */
    public static StringBuffer getStateString(EnumSet<BlackenWindowState> set) {
        List<String> base = getStateStrings(set);
        if (base == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for (String name : base) {
            if (buf.length() > 0) {
                buf.append('|');
            }
            buf.append(name);
        }
        if (buf.length() == 0) {
            buf.append("(none)"); //$NON-NLS-1$
        }
        return buf;
    }

}
