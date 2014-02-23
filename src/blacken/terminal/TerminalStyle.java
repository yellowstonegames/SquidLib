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

/**
 * The basic terminal styles
 * 
 * @author yam655
 */
public enum TerminalStyle {
    // Mapped to WEIGHT:
    /**
     * Maps to WEIGHT_EXTRA_LIGHT
     */
    STYLE_LIGHT,
    // STYLE_LIGHT | STYLE_BOLD // WEIGHT_LIGHT
    // *NO* WEIGHT_DEMILIGHT
    // WEIGHT_REGULAR == default
    // *NO* WEIGHT_SEMIBOLD
    // STYLE_LIGHT | STYLE_HEAVY // WEIGHT_MEDIUM
    // *NO* WEIGHT_DEMIBOLD
    /**
     * WEIGHT_BOLD == Font.BOLD
     */
    STYLE_BOLD,
    /**
     * WEIGHT_HEAVY
     */
    STYLE_HEAVY, 
    // *NO* WEIGHT_EXTRABOLD
    // STYLE_BOLD | STYLE_HEAVY // WEIGHT_ULTRABOLD
    // What is STYLE_BOLD | STYLE_HEAVY | STYLE_LIGHT ?
    
    // Mapped to WIDTH
    /**
     * WIDTH_CONDENSED
     */
    STYLE_NARROW,
    // WIDTH_SEMI_CONDENSED
    // WIDTH_REGULAR == default
    // WIDTH_SEMI_EXTENDED
    /**
     * WIDTH_EXTENDED
     */
    STYLE_WIDE,
    // What is STYLE_NARROW | STYLE_WIDE ?
    
    /**
     * Mapped to POSTURE: POSTURE_OBLIQUE
     */
    STYLE_ITALIC,
    
    // Mapped to SUPERSCRIPT (possibly *unclean* mapping)
    /**
     * SUPERSCRIPT_SUPER
     */
    STYLE_SUPERSCRIPT,
    /**
     * SUPERSCRIPT_SUB
     */
    STYLE_SUBSCRIPT,

    /**
     * Mapped to CHAR_REPLACEMENT: glyph references Shape or Image 
     */
    STYLE_REPLACEMENT,
    
    /**
     * Mapped to UNDERLINE: UNDERLINE_ON
     */
    STYLE_UNDERLINE,
    
    /**
     * Mapped to STRIKETHROUGH: STRIKETHROUGH_ON
     */
    STYLE_STRIKETHROUGH,
    
    /**
     * Mapped to SWAP_COLORS: SWAP_COLORS_ON
     */
    STYLE_REVERSE,
        
    /**
     * Handled by adding alpha to the foreground color
     */
    STYLE_DIM,
    /**
     * Handled by directly modifying the glyph
     */
    STYLE_INVISIBLE
}
