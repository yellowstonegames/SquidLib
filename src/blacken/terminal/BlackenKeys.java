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

import java.lang.reflect.Field;

/**
 * Standard Blacken keys.
 * 
 * <p>The Blacken Key Codes map roughly to Curses key codes -- with numerous
 * additions for modifier states and numerous subtractions due to missing
 * keys on modern keyboards.
 * 
 * <p>These fall within Unicode Plane 15 and 16 (Private Use Areas). These 
 * require storage as an int or a surrogate pair in Java -- but they are 
 * valid Unicode characters.
 * 
 * @author yam655
 */
public class BlackenKeys {
    /**
     * The plane for key codes
     */
    static final public int PLANE_KEY_CODES = 15;
    /**
     * The plane for modifier notices
     */
    static final public int PLANE_MODIFIER_NOTICES = 16;
    /**
     * No modifier is set.
     */
    static final public int MODIFIER_CLEAR = 0x100000;
    /**
     * The first of the Blacken key codes. 
     * 
     */
    static final public int KEY_FIRST = 0x0f0000;
    
    /**
     * The last possible Blacken key code.
     * 
     * See the notes about <code>KEY_START</code> as they apply. 
     * 
     * Note that 0xffffe and 0xfffff are reserved in Unicode, so they can't 
     * happen. Also note that this is the last possible key, not the last key
     * actually used.
     */
    static final public int KEY_LAST = 0x0ffffd;
    
    /**
     * A null keycode. Legal but not mapped to a physical key. Do nothing.
     * 
     * We want to drop a keycode, but -- having produced a key event -- we 
     * need to finish processing in a sane manner. This is also produced by 
     * non-blocking calls when there has been no key generated.
     */
    static final public int NO_KEY = KEY_FIRST + 0;
    /**
     * Synonym for NO_KEY.
     */
    static final public int KEY_NO_KEY = NO_KEY;
    
    /** 
     * A mouse event that we are watching for has been generated.
     */
    static final public int MOUSE_EVENT = KEY_FIRST + 1;
    /**
     * An alias for {@link #MOUSE_EVENT}
     */
    static final public int KEY_MOUSE_EVENT = MOUSE_EVENT;

    /**
     * A window resize event has occurred.
     */
    static final public int RESIZE_EVENT = KEY_FIRST + 2;
    /**
     * An alais for {@link #RESIZE_EVENT}
     */
    static final public int KEY_RESIZED_EVENT = RESIZE_EVENT;

    /**
     * An unknown key
     */
    static final public int KEY_UNKNOWN = KEY_FIRST + 3;
    /**
     * A modifier notice
     */
    static final public int KEY_MODIFIER_NOTICE = KEY_FIRST + 4;
    
    /**
     * A window event
     */
    static final public int WINDOW_EVENT = KEY_FIRST + 5;
    /**
     * An alias for {@link #WINDOW_EVENT}
     */
    static final public int KEY_WINDOW_EVENT = WINDOW_EVENT;
    
//    static final public int KEY_ = KEY_FIRST + 6;
//    static final public int KEY_ = KEY_FIRST + 7;
//    static final public int KEY_ = KEY_FIRST + 8;
//    static final public int KEY_ = KEY_FIRST + 9;

    /**
     * Caps lock (on)
     */
    static final public int KEY_CAPS_LOCK = KEY_FIRST + 10;
    /**
     * Caps lock (off)
     */
    static final public int KEY_CAPS_LOCK_OFF = KEY_FIRST + 11;
    /**
     * Kana lock (on)
     */
    static final public int KEY_KANA_LOCK = KEY_FIRST + 12;
    /**
     * Kana lock (off)
     */
    static final public int KEY_KANA_LOCK_OFF = KEY_FIRST + 13;
    /**
     * Number lock (on)
     */
    static final public int KEY_NUM_LOCK = KEY_FIRST + 14;
    /**
     * Number lock (off)
     */
    static final public int KEY_NUM_LOCK_OFF = KEY_FIRST + 15;
    /**
     * Scroll lock (on)
     */
    static final public int KEY_SCROLL_LOCK = KEY_FIRST + 16;
    /**
     * Scroll lock (off)
     */
    static final public int KEY_SCROLL_LOCK_OFF = KEY_FIRST + 17;
//    static final public int KEY_ = KEY_START + 18;
//    static final public int KEY_ = KEY_START + 19;

    // static final public int KEY_F = KEY_START + 20;
    /**
     * function key
     */
    static final public int KEY_F01 = KEY_FIRST + 21;
    /**
     * function key
     */
    static final public int KEY_F02 = KEY_FIRST + 22;
    /**
     * function key
     */
    static final public int KEY_F03 = KEY_FIRST + 23;
    /**
     * function key
     */
    static final public int KEY_F04 = KEY_FIRST + 24;
    /**
     * function key
     */
    static final public int KEY_F05 = KEY_FIRST + 25;
    /**
     * function key
     */
    static final public int KEY_F06 = KEY_FIRST + 26;
    /**
     * function key
     */
    static final public int KEY_F07 = KEY_FIRST + 27;
    /**
     * function key
     */
    static final public int KEY_F08 = KEY_FIRST + 28;
    /**
     * function key
     */
    static final public int KEY_F09 = KEY_FIRST + 29;
    /**
     * function key
     */
    static final public int KEY_F10 = KEY_FIRST + 30;
    /**
     * function key
     */
    static final public int KEY_F11 = KEY_FIRST + 31;
    /**
     * function key
     */
    static final public int KEY_F12 = KEY_FIRST + 32;
    /**
     * function key
     */
    static final public int KEY_F13 = KEY_FIRST + 33;
    /**
     * function key
     */
    static final public int KEY_F14 = KEY_FIRST + 34;
    /**
     * function key
     */
    static final public int KEY_F15 = KEY_FIRST + 35;
    /**
     * function key
     */
    static final public int KEY_F16 = KEY_FIRST + 36;
    /**
     * function key
     */
    static final public int KEY_F17 = KEY_FIRST + 37;
    /**
     * function key
     */
    static final public int KEY_F18 = KEY_FIRST + 38;
    /**
     * function key
     */
    static final public int KEY_F19 = KEY_FIRST + 39;
    /**
     * function key
     */
    static final public int KEY_F20 = KEY_FIRST + 40;
    /**
     * function key
     */
    static final public int KEY_F21 = KEY_FIRST + 41;
    /**
     * function key
     */
    static final public int KEY_F22 = KEY_FIRST + 42;
    /**
     * function key
     */
    static final public int KEY_F23 = KEY_FIRST + 43;
    /**
     * function key
     */
    static final public int KEY_F24 = KEY_FIRST + 44;

    /**
     * accept key
     */
    static final public int KEY_ACCEPT = KEY_FIRST + 45;
    /**
     * begin key
     */
    static final public int KEY_BEGIN = KEY_FIRST + 46;
    /**
     * convert key
     */
    static final public int KEY_CONVERT = KEY_FIRST + 48;
    /**
     * code input key
     */
    static final public int KEY_CODE_INPUT = KEY_FIRST + 49;

    /**
     * compose key
     */
    static final public int KEY_COMPOSE = KEY_FIRST + 50;
    /**
     * key
     */
    static final public int KEY_FINAL = KEY_FIRST + 51;
    /**
     * key
     */
    static final public int KEY_PROPS = KEY_FIRST + 52;
    /**
     * key
     */
    static final public int KEY_STOP = KEY_FIRST + 53;
    /**
     * key
     */
    static final public int KEY_CANCEL = KEY_FIRST + 54;
    /**
     * key
     */
    static final public int KEY_AGAIN = KEY_FIRST + 55;
    /**
     * key
     */
    static final public int KEY_COPY = KEY_FIRST + 56;
    /**
     * key
     */
    static final public int KEY_CUT = KEY_FIRST + 57;
    /**
     * key
     */
    static final public int KEY_PASTE = KEY_FIRST + 58;
    /**
     * key
     */
    static final public int KEY_FIND = KEY_FIRST + 59;

    /**
     * key
     */
    static final public int KEY_HELP = KEY_FIRST + 60;
    /**
     * key
     */
    static final public int KEY_UNDO = KEY_FIRST + 61;
    /**
     * key
     */
    static final public int KEY_BACKSPACE = KEY_FIRST + 62;
    /**
     * key
     */
    static final public int KEY_CONTEXT_MENU = KEY_FIRST + 63;
    /**
     * key
     */
    static final public int KEY_ESCAPE = KEY_FIRST + 64;
    /**
     * key
     */
    static final public int KEY_ENTER = KEY_FIRST + 65;
    /**
     * key
     */
    static final public int KEY_PAUSE = KEY_FIRST + 66;
    /**
     * key
     */
    static final public int KEY_LOGO = KEY_FIRST + 67;
    /**
     * key
     */
    static final public int KEY_PRINT_SCREEN = KEY_FIRST + 68;
    /**
     * key
     */
    static final public int KEY_TAB = KEY_FIRST + 69;

    /**
     * key
     */
    static final public int KEY_NP_ADD = KEY_FIRST + 70;
    /**
     * key
     */
    static final public int KEY_NP_DIVIDE = KEY_FIRST + 71;
    /**
     * key
     */
    static final public int KEY_NP_SUBTRACT = KEY_FIRST + 72;
    /**
     * key
     */
    static final public int KEY_NP_MULTIPLY = KEY_FIRST + 73;
    /**
     * key
     */
    static final public int KEY_NP_SEPARATOR = KEY_FIRST + 74;
    /**
     * key
     */
    static final public int KEY_NP_ENTER = KEY_FIRST + 75;
//  static final public int KEY_ = KEY_FIRST + 76;
//  static final public int KEY_ = KEY_FIRST + 77;
//  static final public int KEY_ = KEY_FIRST + 78;
//  static final public int KEY_ = KEY_FIRST + 79;

    /**
     * key
     */
    static final public int KEY_NP_0 = KEY_FIRST + 80;
    /**
     * key
     */
    static final public int KEY_NP_1 = KEY_FIRST + 81;
    /**
     * key
     */
    static final public int KEY_NP_2 = KEY_FIRST + 82;
    /**
     * key
     */
    static final public int KEY_NP_3 = KEY_FIRST + 83;
    /**
     * key
     */
    static final public int KEY_NP_4 = KEY_FIRST + 84;
    /**
     * key
     */
    static final public int KEY_NP_5 = KEY_FIRST + 85;
    /**
     * key
     */
    static final public int KEY_NP_6 = KEY_FIRST + 86;
    /**
     * key
     */
    static final public int KEY_NP_7 = KEY_FIRST + 87;
    /**
     * key
     */
    static final public int KEY_NP_8 = KEY_FIRST + 88;
    /**
     * key
     */
    static final public int KEY_NP_9 = KEY_FIRST + 89;

    /**
     * key
     */
    static final public int KEY_UP = KEY_FIRST + 90;
    /**
     * key
     */
    static final public int KEY_DOWN = KEY_FIRST + 91;
    /**
     * key
     */
    static final public int KEY_LEFT = KEY_FIRST + 92;
    /**
     * key
     */
    static final public int KEY_RIGHT = KEY_FIRST + 93;
    /**
     * key
     */
    static final public int KEY_PAGE_DOWN = KEY_FIRST + 94;
    /**
     * key
     */
    static final public int KEY_PAGE_UP = KEY_FIRST + 95;
    /**
     * key
     */
    static final public int KEY_HOME = KEY_FIRST + 96;
    /**
     * key
     */
    static final public int KEY_END = KEY_FIRST + 97;
    /**
     * key
     */
    static final public int KEY_INSERT = KEY_FIRST + 98;
    /**
     * key
     */
    static final public int KEY_DELETE = KEY_FIRST + 99;

    /**
     * key
     */
    static final public int KEY_ALL_CANDIDATES = KEY_FIRST + 101;
    /**
     * key
     */
    static final public int KEY_PREVIOUS_CANDIDATE = KEY_FIRST + 102;
    /**
     * key
     */
    static final public int KEY_FULL_WIDTH = KEY_FIRST + 103;
    /**
     * key
     */
    static final public int KEY_HALF_WIDTH = KEY_FIRST + 104;
    /**
     * key
     */
    static final public int KEY_ALPHANUMERIC = KEY_FIRST + 105;
    /**
     * key
     */
    static final public int KEY_HIRAGANA = KEY_FIRST + 106;
    /**
     * key
     */
    static final public int KEY_KATAKANA = KEY_FIRST + 107;
    /**
     * key
     */
    static final public int KEY_KANJI = KEY_FIRST + 108;
    /**
     * key
     */
    static final public int KEY_NONCONVERT = KEY_FIRST + 109;
    /**
     * key
     */
    static final public int KEY_ROMAN_CHARACTERS = KEY_FIRST + 110;

    /**
     * key
     */
    static final public int KEY_INPUT_METHOD_TOGGLE = KEY_FIRST + 111;
    /**
     * key
     */
    static final public int KEY_MODECHANGE = KEY_FIRST + 112;
    /**
     * key
     */
    static final public int KEY_JAPANESE_HIRAGANA = KEY_FIRST + 113;
    /**
     * key
     */
    static final public int KEY_JAPANESE_KATAKANA = KEY_FIRST + 114;
    /**
     * key
     */
    static final public int KEY_JAPANESE_ROMAN = KEY_FIRST + 115;
//  static final public int KEY_ = KEY_FIRST + 116;
//  static final public int KEY_ = KEY_FIRST + 117;
//  static final public int KEY_ = KEY_FIRST + 118;
//  static final public int KEY_ = KEY_FIRST + 119;

    /**
     * key
     */
    static final public int KEY_KP_UP = KEY_FIRST + 120;
    /**
     * key
     */
    static final public int KEY_KP_DOWN = KEY_FIRST + 121;
    /**
     * key
     */
    static final public int KEY_KP_LEFT = KEY_FIRST + 122;
    /**
     * key
     */
    static final public int KEY_KP_RIGHT = KEY_FIRST + 123;
    /**
     * key
     */
    static final public int KEY_KP_PAGE_DOWN = KEY_FIRST + 124;
    /**
     * key
     */
    static final public int KEY_KP_PAGE_UP = KEY_FIRST + 125;
    /**
     * key
     */
    static final public int KEY_KP_HOME = KEY_FIRST + 126;
    /**
     * key
     */
    static final public int KEY_KP_END = KEY_FIRST + 127;
    /**
     * key
     */
    static final public int KEY_KP_INSERT = KEY_FIRST + 128;
    /**
     * key
     */
    static final public int KEY_KP_DELETE = KEY_FIRST + 129;
    /**
     * Java calls it CLEAR, Curses calls it B2.
     * It is the center key or the number pad 5 key when numberlock is disabled. 
     */
    static final public int KEY_KP_CLEAR = KEY_FIRST + 130;
    /**
     * key
     */
    static final public int KEY_KP_B2 = KEY_KP_CLEAR;

//  static final public int KEY_ = KEY_FIRST + 10;
//  static final public int KEY_ = KEY_FIRST + 11;
//  static final public int KEY_ = KEY_FIRST + 12;
//  static final public int KEY_ = KEY_FIRST + 13;
//  static final public int KEY_ = KEY_FIRST + 14;
//  static final public int KEY_ = KEY_FIRST + 15;
//  static final public int KEY_ = KEY_FIRST + 16;
//  static final public int KEY_ = KEY_FIRST + 17;
//  static final public int KEY_ = KEY_FIRST + 18;
//  static final public int KEY_ = KEY_FIRST + 19;

    // END OF PHYSICAL KEY SPACE
    // START OF LOGICAL KEY SPACE
    
    /**
     * We are done with whatever command was processing.
     */
    static final public int CMD_END_LOOP = KEY_FIRST + 1000;

    
    // END OF LOGICAL KEY SPACE

    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_NUL = 0x00;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SOH = 0x01;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_STX = 0x02;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_ETX = 0x03;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_EOT = 0x04;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_ENQ = 0x05;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_ACK = 0x06;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_BEL = 0x07;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_BS = 0x08;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_HT = 0x09;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_LF = 0x0a;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_VT = 0x0b;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_FF = 0x0c;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_CR = 0x0d;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SO = 0x0e;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SI = 0x0f;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_DLE = 0x10;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_DC1 = 0x11;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_DC2 = 0x12;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_DC3 = 0x13;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_DC4 = 0x14;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_NAK = 0x15;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SYN = 0x16;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_ETB = 0x17;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_CAN = 0x18;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_EM = 0x19;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SUB = 0x1a;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_ESC = 0x1b;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_FS = 0x1c;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_GS = 0x1d;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_RS = 0x1e;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_US = 0x1f;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SP = 0x20;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_DEL = 0x7f;

    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_PAD = 0x80;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_HOP = 0x81;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SPH = 0x82;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_NBH = 0x83;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_IND = 0x84;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_NEL = 0x85;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SSA = 0x86;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_ESA = 0x87;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_HTS = 0x88;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_HTJ = 0x89;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_VTS = 0x8a;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_PLD = 0x8b;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_PLU = 0x8c;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_RI = 0x8d;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SS2 = 0x8e;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SS3 = 0x8f;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_DCS = 0x90;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_PU1 = 0x91;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_PU2 = 0x92;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_STS = 0x93;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_CCH = 0x94;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_MW = 0x95;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SPA = 0x96;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_EPA = 0x97;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SOS = 0x98;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SGCI = 0x99;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_SCI = 0x9a;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_CSI = 0x9b;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_ST = 0x9c;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_OSC = 0x9d;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_PM = 0x9e;
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_APC = 0x9f;

    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_NBSP = 0xa0;

    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_GRAVE_ACCENT = '\u0300';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_ACUTE_ACCENT = '\u0301';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_CIRCUMFLEX_ACCENT = '\u0302';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_TILDE = '\u0303';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_MACRON = '\u0304';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_OVERLINE = '\u0305';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_BREVE = '\u0306';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DOT_ABOVE = '\u0307';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DIAERESIS = '\u0308';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_HOOK_ABOVE = '\u0309';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_RING_ABOVE = '\u030A';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DOUBLE_ACUTE_ACCENT = '\u030B';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_CARON = '\u030C';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_VERTICAL_LINE_ABOVE = '\u030D';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DOUBLE_VERTICAL_LINE_ABOVE = '\u030E';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DOUBLE_GRAVE_ACCENT = '\u030F';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_CANDRABINDU = '\u0310';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_INVERTED_BREVE = '\u0311';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_TURNED_COMMA_ABOVE = '\u0312';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_COMMA_ABOVE = '\u0313';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_REVERSED_COMMA_ABOVE = '\u0314';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_COMMA_ABOVE_RIGHT = '\u0315';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_GRAVE_ACCENT_BELOW = '\u0316';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_ACUTE_ACCENT_BELOW = '\u0317';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_LEFT_TACK_BELOW = '\u0318';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_RIGHT_TACK_BELOW = '\u0319';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_LEFT_ANGLE_ABOVE = '\u031A';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_HORN = '\u031B';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_LEFT_HALF_RING_BELOW = '\u031C';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_UP_TACK_BELOW = '\u031D';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DOWN_TACK_BELOW = '\u031E';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_PLUS_SIGN_BELOW = '\u031F';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_MINUS_SIGN_BELOW = '\u0320';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_PALATALIZED_HOOK_BELOW = '\u0321';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_RETROFLEX_HOOK_BELOW = '\u0322';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DOT_BELOW = '\u0323';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_DIAERESIS_BELOW = '\u0324';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_RING_BELOW = '\u0325';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_COMMA_BELOW = '\u0326';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_CEDILLA = '\u0327';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_OGONEK = '\u0328';
    /**
     * @deprecated Moved to BlackenCodePoints
     */
    @Deprecated
    static final public int CODEPOINT_COMBINING_GREEK_YPOGEGRAMMENI = '\u0345';

    /**
     * Find the plane that a codepoint is on.
     * 
     * @param codepoint The codepoint to check.
     * @return -1 if codepoint isn't a legal codepoint (as of Unicode 5.2)
     */
    public static int findPlane(int codepoint) {
        if (codepoint < 0) {
            return -1;
        }
        codepoint >>= 16;
        if (codepoint > 16) {
            codepoint = -1;
        }
        return codepoint;
    }

    /**
     * Is the codepoint a keycode or a modifier notice?
     * 
     * @param codepoint the codepoint to check
     * @return true if it is a keycode or modifer notice, false otherwise
     */
    public static boolean isSpecial(int codepoint) {
        int plane = findPlane(codepoint);
        if (plane == PLANE_KEY_CODES || plane == PLANE_MODIFIER_NOTICES) {
            return true;
        }
        return false;
    }
    
    /**
     * Is the codepoint a keycode?
     * 
     * Keycodes exist on Unicode plane PLANE_KEY_CODES. They are guaranteed
     * to exist on a private plane. They are not modifier notices.
     * 
     * @param codepoint the codepoint to check
     * @return true if a keycode, false otherwise
     */
    public static boolean isKeyCode(int codepoint) {
        if (findPlane(codepoint) == BlackenKeys.PLANE_KEY_CODES) {
            return true;
        }
        return false;
    }
    
    /**
     * Is the keycode a modifier key?
     * 
     * @param codepoint keycode to check
     * @return true if modifier, false otherwise
     */
    public static boolean isModifier(int codepoint) {
        if (findPlane(codepoint) == BlackenKeys.PLANE_MODIFIER_NOTICES) {
            return true;
        }
        return false;
    }

    /**
     * Squash any modifier keycode.
     * 
     * @param codepoint modifier key
     * @return new keycode
     */
    public static int removeModifier(int codepoint) {
        if (findPlane(codepoint) == BlackenKeys.PLANE_MODIFIER_NOTICES) {
            return BlackenKeys.NO_KEY;
        }
        return codepoint;
    }

    /**
     * Turn a keycode in to a string.
     *
     * <p>This turns a keycode in to 7-bit encoding using "\\u0000" or
     * "\\U0000000" notation. It makes zero attempt to get the name of anything.
     *
     * @param keycode codepoint
     * @return string
     */
    public static String toString(int keycode) {
        StringBuilder keybuf = new StringBuilder();
        if (findPlane(keycode) != 0) {
            keybuf.append(String.format("\\U%08x", keycode));
        } else if (keycode < 0x20 || (keycode >= 0x7f && keycode < 0xa0)) {
            keybuf.append(String.format("\\u%04x", keycode));
        } else {
            keybuf.appendCodePoint(keycode);
        }
        return keybuf.toString();
    }
    
    /**
     * Get the key name
     * @param keycode codepoint
     * @return string containing key name
     */
    public static String getKeyName(int keycode) {
        StringBuilder keybuf = new StringBuilder();
        int plane = findPlane(keycode);
        if (plane == 16) {
            keybuf.append(BlackenModifier.getModifierString(keycode));
        } else if (plane == 15) {
            String name = String.format("\\U%08x", keycode);
            for (Field f : BlackenKeys.class.getDeclaredFields()) {
                String fieldName = f.getName();
                if ("KEY_FIRST".equals(fieldName)) {
                    // suppress this -- we want to print KEY_NO_KEY
                    continue;
                }
                try {
                    if (f.getInt(null) == keycode) {
                        name = f.getName();
                        break;
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    continue;
                }
            }
            keybuf.append(name);
        } else {
            keybuf.appendCodePoint(keycode);
        }
        return keybuf.toString();
    }
}
