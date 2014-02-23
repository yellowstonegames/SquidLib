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
 * Modifier notices exist on PLANE_MODIFIER_NOTICES, which is guaranteed
 * to be a private unicode plane.
 * 
 * When modifier states are enabled, a modifier notice is sent as a prefix
 * before the normal key. (The normal key may be a standard unicode codepoint
 * or it may be a special keycode.)
 * 
 * Note that the _LOCK modifier states are not normally included. They need
 * to be explicitly requested.
 * 
 * @author yam655
 */
public enum BlackenModifier {
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_ALT(0x0001, "Alt"), //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_ALTGR(0x0002, "AltGr"),  //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_CTRL(0x0004, "Ctrl"),  //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_META(0x0008, "Meta"),  //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_SHIFT(0x0010, "Shift"), //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_CAPS_LOCK(0x0020, "CapsLock"), //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_KANA_LOCK(0x0040, "KanaLock"), //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_NUM_LOCK(0x0080, "NumLock"), //$NON-NLS-1$
    /**
     * Create new modifier state
     */
    MODIFIER_KEY_SCROLL_LOCK(0x0100, "ScrollLock"); //$NON-NLS-1$
    
    private final int bit;
    private String keyname;

    /**
     * Create a modifier
     * @param bit bit to set
     * @param keyname keyname to set
     */
    BlackenModifier(int bit, String keyname) {
        this.bit = bit;
        this.keyname = keyname;
    }

    /*
     * @see Enum#toString()
     */
    @Override
    public String toString() {
        return this.keyname;
    }
    
    /**
     * Set a modifier key on a codepoint.
     * 
     * @param current the current unicode codepoint
     * @param mod the modifier to use
     * @return new codepoint with this flag set.
     */
    public static int setFlag(int current, BlackenModifier mod) {
        if ((current & mod.bit) != 0) {
            return current;
        }
        if (BlackenKeys.findPlane(current) != BlackenKeys.PLANE_MODIFIER_NOTICES) {
            return current;
        }
        return current | mod.bit;
    }
    /**
     * Set a modifier key on a codepoint.
     * 
     * @param current the current unicode codepoint
     * @return new codepoint with this flag set.
     */
    public int setFlag(int current) {
        return BlackenModifier.setFlag(current, this);
    }

    /**
     * Is the bit flag set?
     * 
     * @param current current integer
     * @param mod modifier to check
     * @return true on success; false on failure
     */
    public static boolean hasFlag(int current, BlackenModifier mod) {
        if (BlackenKeys.findPlane(current) != BlackenKeys.PLANE_MODIFIER_NOTICES) {
            return false;
        }
        if ((current & mod.bit) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Does <code>current</code> have <code>this</code> set?
     * @param current integer to check
     * @return true on success; false on failure
     */
    public boolean hasFlag(int current) {
        return BlackenModifier.hasFlag(current, this);
    }
    /**
     * Clear a modifier key from a codepoint.
     * 
     * @param current the current unicode codepoint
     * @param mod the modifier to use
     * @return new codepoint with this flag cleared.
     */
    public static int clearFlag(int current, BlackenModifier mod) {
        if ((current & mod.bit) == 0) {
            return current;
        }
        if (BlackenKeys.findPlane(current) != BlackenKeys.PLANE_MODIFIER_NOTICES) {
            return current;
        }
        return current & (~mod.bit);
    }
    /**
     * Clear a modifier key from a codepoint.
     * 
     * @param current the current unicode codepoint
     * @return new codepoint with this flag cleared.
     */
    public int clearFlag(int current) {
        return BlackenModifier.clearFlag(current, this);
    }

    /**
     * Get a particular BlackenModifier as the unicode codepoint.
     * 
     * @return codepoint
     */
    public int getAsCodepoint() {
        int ret = BlackenKeys.PLANE_MODIFIER_NOTICES << 16;
        ret = ret | this.bit;
        return ret;
    }

    /**
     * Encode the modifiers as a unicode codepoint.
     * 
     * @param set set to check
     * @return NO_KEY if set was null, otherwise valid unicode codepoint.
     */
    public static int getAsCodepoint(EnumSet<BlackenModifier> set) {
        int ret = BlackenKeys.PLANE_MODIFIER_NOTICES << 16;
        if (set == null) {
            return BlackenKeys.NO_KEY;
        }
        for (BlackenModifier m : set) {
            ret = ret | m.bit;
        }
        return ret;
    }
    
    /**
     * Extract the modifiers from an integer in to an EnumSet.
     * 
     * <p>This returns an empty set for BlackenKeys.NO_KEY and <code>null</code>
     * for all other keys outside {@link BlackenKeys#PLANE_MODIFIER_NOTICES}.
     * 
     * @param keycode a unicode codepoint for the modifier set
     * @return null if not modifier codepoint, otherwise EnumSet.
     */
    public static EnumSet<BlackenModifier> getAsSet(int keycode) {
        EnumSet<BlackenModifier> ret = EnumSet.noneOf(BlackenModifier.class);
        if (keycode == BlackenKeys.NO_KEY) {
            return ret;
        }
        if (BlackenKeys.findPlane(keycode) !=  
                BlackenKeys.PLANE_MODIFIER_NOTICES) {
            return null;
        }
        for (BlackenModifier m : EnumSet.allOf(BlackenModifier.class)) {
            if ((keycode & m.bit) != 0) {
                ret.add(m);
            }
        }
        return ret;
    }

    /**
     * Get the modifier strings
     * @param set modifier set
     * @return string containing modifiers
     */
    public static List<String> getModifierStrings(EnumSet<BlackenModifier> set) {
        List<String> ret = new ArrayList<String>();
        for (BlackenModifier m : set) {
            ret.add(m.keyname);
        }
        return ret;
    }
    /**
     * Get the modifier strings
     * @param keycode codepoint with modifiers set
     * @return string containing modifiers
     */
    public static List<String> getModifierStrings(int keycode) {
        if (BlackenKeys.findPlane(keycode) != 
            BlackenKeys.PLANE_MODIFIER_NOTICES) {
            return null;
        }
        return getModifierStrings(BlackenModifier.getAsSet(keycode));
    }
    /**
     * Get the modifier strings
     * @param keycode codepoint with modifiers set
     * @return string buffer containing modifiers
     */
    public static StringBuffer getModifierString(int keycode) {
        List<String> base = getModifierStrings(keycode);
        StringBuffer keybuf = new StringBuffer();
        if (base == null) {
            return keybuf;
        }
        for (String name : base) {
            keybuf.append(name);
            keybuf.append('+');
        }
        return keybuf;
    }

    /**
     * Get the modifier string
     * @param set set of modifiers that are enabled
     * @return string buffer containing modifiers
     */
    public static StringBuffer getModifierString(EnumSet<BlackenModifier> set) {
        List<String> base = getModifierStrings(set);
        if (base == null) {
            return null;
        }
        StringBuffer keybuf = new StringBuffer();
        for (String name : base) {
            keybuf.append(name);
            keybuf.append('+');
        }
        return keybuf;
    }

}
