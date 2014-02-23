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

package com.googlecode.blacken.terminal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Provide easier access to important codepoints.
 *
 * <p>This provides access to Unicode points almost by name. In some cases the
 * standard Unicode name is long and it has been shortened.
 *
 * <p>Part of the unittests include verifying that Unicode name maps cleanly
 * to the field name. In cases where we have a shortened field name there is
 * a "LongName" annotation with the actual Unicode name in it.
 *
 * <p>Eventually this will move to a "blacken-optional" Maven package. This 
 * means the {@link #getCodepointName(int)} here is the only function that can
 * pull out the codepoint field name as well the {@link BlackenKeys} and
 * {@link BlackenModifier} information.
 *
 * @author Steven Black
 */
public class BlackenCodePoints {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface LongName {
        String value();
    }
    @LongName("NULL")
    static final public int CODEPOINT_NUL = 0x00;
    @LongName("START OF HEADING")
    static final public int CODEPOINT_SOH = 0x01;
    @LongName("START OF TEXT")
    static final public int CODEPOINT_STX = 0x02;
    @LongName("END OF TEXT")
    static final public int CODEPOINT_ETX = 0x03;
    @LongName("END OF TRANSMISSION")
    static final public int CODEPOINT_EOT = 0x04;
    @LongName("ENQUIRY")
    static final public int CODEPOINT_ENQ = 0x05;
    @LongName("ACKNOWLEDGE")
    static final public int CODEPOINT_ACK = 0x06;
    @LongName("BELL")
    static final public int CODEPOINT_BEL = 0x07;
    @LongName("BACKSPACE")
    static final public int CODEPOINT_BS = 0x08;
    @LongName("CHARACTER TABULATION")
    static final public int CODEPOINT_HT = 0x09;
    @LongName("LINE FEED (LF)")
    static final public int CODEPOINT_LF = 0x0a;
    @LongName("LINE TABULATION")
    static final public int CODEPOINT_VT = 0x0b;
    @LongName("FORM FEED (FF)")
    static final public int CODEPOINT_FF = 0x0c;
    @LongName("CARRIAGE RETURN (CR)")
    static final public int CODEPOINT_CR = 0x0d;
    @LongName("SHIFT OUT")
    static final public int CODEPOINT_SO = 0x0e;
    @LongName("SHIFT IN")
    static final public int CODEPOINT_SI = 0x0f;
    @LongName("DATA LINK ESCAPE")
    static final public int CODEPOINT_DLE = 0x10;
    @LongName("DEVICE CONTROL ONE")
    static final public int CODEPOINT_DC1 = 0x11;
    @LongName("DEVICE CONTROL TWO")
    static final public int CODEPOINT_DC2 = 0x12;
    @LongName("DEVICE CONTROL THREE")
    static final public int CODEPOINT_DC3 = 0x13;
    @LongName("DEVICE CONTROL FOUR")
    static final public int CODEPOINT_DC4 = 0x14;
    @LongName("NEGATIVE ACKNOWLEDGE")
    static final public int CODEPOINT_NAK = 0x15;
    @LongName("SYNCHRONOUS IDLE")
    static final public int CODEPOINT_SYN = 0x16;
    @LongName("END OF TRANSMISSION BLOCK")
    static final public int CODEPOINT_ETB = 0x17;
    @LongName("CANCEL")
    static final public int CODEPOINT_CAN = 0x18;
    @LongName("END OF MEDIUM")
    static final public int CODEPOINT_EM = 0x19;
    @LongName("SUBSTITUTE")
    static final public int CODEPOINT_SUB = 0x1a;
    @LongName("ESCAPE")
    static final public int CODEPOINT_ESC = 0x1b;
    @LongName("INFORMATION SEPARATOR FOUR")
    static final public int CODEPOINT_FS = 0x1c;
    @LongName("INFORMATION SEPARATOR THREE")
    static final public int CODEPOINT_GS = 0x1d;
    @LongName("INFORMATION SEPARATOR TWO")
    static final public int CODEPOINT_RS = 0x1e;
    @LongName("INFORMATION SEPARATOR ONE")
    static final public int CODEPOINT_US = 0x1f;
    @LongName("SPACE")
    static final public int CODEPOINT_SP = 0x20;
    @LongName("DELETE")
    static final public int CODEPOINT_DEL = 0x7f;

    @LongName("LATIN 1 SUPPLEMENT 80")
    static final public int CODEPOINT_PAD = 0x80;
    @LongName("LATIN 1 SUPPLEMENT 81")
    static final public int CODEPOINT_HOP = 0x81;
    @LongName("BREAK PERMITTED HERE")
    static final public int CODEPOINT_SPH = 0x82;
    @LongName("NO BREAK HERE")
    static final public int CODEPOINT_NBH = 0x83;
    @LongName("LATIN 1 SUPPLEMENT 84")
    static final public int CODEPOINT_IND = 0x84;
    @LongName("NEXT LINE (NEL)")
    static final public int CODEPOINT_NEL = 0x85;
    @LongName("START OF SELECTED AREA")
    static final public int CODEPOINT_SSA = 0x86;
    @LongName("END OF SELECTED AREA")
    static final public int CODEPOINT_ESA = 0x87;
    @LongName("CHARACTER TABULATION SET")
    static final public int CODEPOINT_HTS = 0x88;
    @LongName("CHARACTER TABULATION WITH JUSTIFICATION")
    static final public int CODEPOINT_HTJ = 0x89;
    @LongName("LINE TABULATION SET")
    static final public int CODEPOINT_VTS = 0x8a;
    @LongName("PARTIAL LINE FORWARD")
    static final public int CODEPOINT_PLD = 0x8b;
    @LongName("PARTIAL LINE BACKWARD")
    static final public int CODEPOINT_PLU = 0x8c;
    @LongName("REVERSE LINE FEED")
    static final public int CODEPOINT_RI = 0x8d;
    @LongName("SINGLE SHIFT TWO")
    static final public int CODEPOINT_SS2 = 0x8e;
    @LongName("SINGLE SHIFT THREE")
    static final public int CODEPOINT_SS3 = 0x8f;
    @LongName("DEVICE CONTROL STRING")
    static final public int CODEPOINT_DCS = 0x90;
    @LongName("PRIVATE USE ONE")
    static final public int CODEPOINT_PU1 = 0x91;
    @LongName("PRIVATE USE TWO")
    static final public int CODEPOINT_PU2 = 0x92;
    @LongName("SET TRANSMIT STATE")
    static final public int CODEPOINT_STS = 0x93;
    @LongName("CANCEL CHARACTER")
    static final public int CODEPOINT_CCH = 0x94;
    @LongName("MESSAGE WAITING")
    static final public int CODEPOINT_MW = 0x95;
    @LongName("START OF GUARDED AREA")
    static final public int CODEPOINT_SPA = 0x96;
    @LongName("END OF GUARDED AREA")
    static final public int CODEPOINT_EPA = 0x97;
    @LongName("START OF STRING")
    static final public int CODEPOINT_SOS = 0x98;
    @LongName("LATIN 1 SUPPLEMENT 99")
    static final public int CODEPOINT_SGCI = 0x99;
    @LongName("SINGLE CHARACTER INTRODUCER")
    static final public int CODEPOINT_SCI = 0x9a;
    @LongName("CONTROL SEQUENCE INTRODUCER")
    static final public int CODEPOINT_CSI = 0x9b;
    @LongName("STRING TERMINATOR")
    static final public int CODEPOINT_ST = 0x9c;
    @LongName("OPERATING SYSTEM COMMAND")
    static final public int CODEPOINT_OSC = 0x9d;
    @LongName("PRIVACY MESSAGE")
    static final public int CODEPOINT_PM = 0x9e;
    @LongName("APPLICATION PROGRAM COMMAND")
    static final public int CODEPOINT_APC = 0x9f;

    @LongName("NO-BREAK SPACE")
    static final public int CODEPOINT_NBSP = 0xa0;

    static final public int CODEPOINT_COMBINING_GRAVE_ACCENT = '\u0300';
    static final public int CODEPOINT_COMBINING_ACUTE_ACCENT = '\u0301';
    static final public int CODEPOINT_COMBINING_CIRCUMFLEX_ACCENT = '\u0302';
    static final public int CODEPOINT_COMBINING_TILDE = '\u0303';
    static final public int CODEPOINT_COMBINING_MACRON = '\u0304';
    static final public int CODEPOINT_COMBINING_OVERLINE = '\u0305';
    static final public int CODEPOINT_COMBINING_BREVE = '\u0306';
    static final public int CODEPOINT_COMBINING_DOT_ABOVE = '\u0307';
    static final public int CODEPOINT_COMBINING_DIAERESIS = '\u0308';
    static final public int CODEPOINT_COMBINING_HOOK_ABOVE = '\u0309';
    static final public int CODEPOINT_COMBINING_RING_ABOVE = '\u030A';
    static final public int CODEPOINT_COMBINING_DOUBLE_ACUTE_ACCENT = '\u030B';
    static final public int CODEPOINT_COMBINING_CARON = '\u030C';
    static final public int CODEPOINT_COMBINING_VERTICAL_LINE_ABOVE = '\u030D';
    static final public int CODEPOINT_COMBINING_DOUBLE_VERTICAL_LINE_ABOVE = '\u030E';
    static final public int CODEPOINT_COMBINING_DOUBLE_GRAVE_ACCENT = '\u030F';
    static final public int CODEPOINT_COMBINING_CANDRABINDU = '\u0310';
    static final public int CODEPOINT_COMBINING_INVERTED_BREVE = '\u0311';
    static final public int CODEPOINT_COMBINING_TURNED_COMMA_ABOVE = '\u0312';
    static final public int CODEPOINT_COMBINING_COMMA_ABOVE = '\u0313';
    static final public int CODEPOINT_COMBINING_REVERSED_COMMA_ABOVE = '\u0314';
    static final public int CODEPOINT_COMBINING_COMMA_ABOVE_RIGHT = '\u0315';
    static final public int CODEPOINT_COMBINING_GRAVE_ACCENT_BELOW = '\u0316';
    static final public int CODEPOINT_COMBINING_ACUTE_ACCENT_BELOW = '\u0317';
    static final public int CODEPOINT_COMBINING_LEFT_TACK_BELOW = '\u0318';
    static final public int CODEPOINT_COMBINING_RIGHT_TACK_BELOW = '\u0319';
    static final public int CODEPOINT_COMBINING_LEFT_ANGLE_ABOVE = '\u031A';
    static final public int CODEPOINT_COMBINING_HORN = '\u031B';
    static final public int CODEPOINT_COMBINING_LEFT_HALF_RING_BELOW = '\u031C';
    static final public int CODEPOINT_COMBINING_UP_TACK_BELOW = '\u031D';
    static final public int CODEPOINT_COMBINING_DOWN_TACK_BELOW = '\u031E';
    static final public int CODEPOINT_COMBINING_PLUS_SIGN_BELOW = '\u031F';
    static final public int CODEPOINT_COMBINING_MINUS_SIGN_BELOW = '\u0320';
    static final public int CODEPOINT_COMBINING_PALATALIZED_HOOK_BELOW = '\u0321';
    static final public int CODEPOINT_COMBINING_RETROFLEX_HOOK_BELOW = '\u0322';
    static final public int CODEPOINT_COMBINING_DOT_BELOW = '\u0323';
    static final public int CODEPOINT_COMBINING_DIAERESIS_BELOW = '\u0324';
    static final public int CODEPOINT_COMBINING_RING_BELOW = '\u0325';
    static final public int CODEPOINT_COMBINING_COMMA_BELOW = '\u0326';
    static final public int CODEPOINT_COMBINING_CEDILLA = '\u0327';
    static final public int CODEPOINT_COMBINING_OGONEK = '\u0328';
    static final public int CODEPOINT_COMBINING_GREEK_YPOGEGRAMMENI = '\u0345';

    static final public int CODEPOINT_LIGHT_SHADE = '\u2591';
    static final public int CODEPOINT_MEDIUM_SHADE = '\u2592';
    static final public int CODEPOINT_DARK_SHADE = '\u2593';

    static final public int CODEPOINT_BLACK_SQUARE = '\u25a0';
    static final public int CODEPOINT_WHITE_SQUARE = '\u25a1';
    static final public int CODEPOINT_BLACK_SMALL_SQUARE = '\u25aa';
    static final public int CODEPOINT_WHITE_SMALL_SQUARE = '\u25ab';
    
    private static Map<Integer, String> shortMap = null;
    
    private static void createShortMap() {
        if (shortMap != null) {
            return;
        }
        shortMap = new HashMap<>();
        for (Field f : BlackenCodePoints.class.getFields()) {
            String name = f.getName();
            if (!name.startsWith("CODEPOINT_")) {
                continue;
            }
            LongName ln = f.getAnnotation(LongName.class);
            if (ln == null) {
                continue;
            }
            try {
                shortMap.put(f.getInt(null), name);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException("Should never happen.");
            }
        }
    }

    /**
     * This gets the modifier string, Blacken Key name, CodePoint field name,
     * or Unicode name. This is the only function that can pull the name this
     * way as this class will eventually be pushed to a "blacken-optional"
     * Maven package, so these values will be missing from
     * {@link BlackenKeys#toString(int)}.
     * @param codepoint key codepoint
     * @return string describing codepoint
     */
    public static String getCodepointName(int codepoint) {
        int plane = BlackenKeys.findPlane(codepoint);
        if (plane == 16) {
            return BlackenModifier.getModifierString(codepoint).toString();
        } else if (plane == 15) {
            return BlackenKeys.getKeyName(codepoint);
        } else {
            String ret = getName(codepoint);
            if (ret == null) {
                ret = Character.getName(codepoint);
            }
            if (ret == null) {
                ret = String.format("\\U%08x", codepoint);
            }
            return ret;
        }
    }

    /**
     * Get the constant field name for a codepoint.
     *
     * <p>Java is missing the capability of getting a character by name.
     * To work around that lack, this class has named constants for a number
     * of codepoints.
     *
     * <p>The first time this runs it builds a cache to assist with the
     * shortened field names. Expect that to be a slow operation. 
     * After the first call, because of the cache, it should be pretty fast.
     *
     * <p>This returns the name of the constant within this class or 
     * <code>null</code> if none present. To always get a string describing
     * a codepoint, use {@link #getCodepointName(int)}.
     *
     * @param codepoint 
     * @return name of the constant or null if not present
     */
    public static String getName(int codepoint) {
        createShortMap();
        if (shortMap.containsKey(codepoint)) {
            return shortMap.get(codepoint);
        }
        String name = Character.getName(codepoint);
        name = "CODEPOINT_" + name.replaceAll("[ ()-]+", "_");
        try {
            BlackenCodePoints.class.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException ex) {
            name = null;
        }
        return name;
    }

    public static String asString(int codepoint) {
        return String.copyValueOf(Character.toChars(codepoint));
    }
}
