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
package com.googlecode.blacken.fonts;

/**
 * Font helper class.
 * 
 * @author Steven Black
 */
public class FontHelper {
    /**
     * Starting point for US-ASCII full-width chunk
     */
    static public final int FULLWIDTH_ASCII_VARIANTS=0xff00;
    /**
     * The 7-bit ASCII characters (minus space, so 0x20 > c > 0x7f) have 
     * variations a couple places in the Unicode specification. This will
     * convert from standard ASCII to one of these other ranges.
     * 
     * @param sequence character sequence
     * @param newBase new code-point base offset
     * @return new character sequence
     */
    public String shiftAscii(String sequence, int newBase) {
        StringBuilder out = new StringBuilder(sequence.length());
        for(int i = 0; i < sequence.codePointCount(0, sequence.length()); i++) {
            int cp = sequence.codePointAt(i);
            if (cp > 0x20 && cp < 0x7f) {
                cp = cp + newBase - 0x20;
            }
            out.append(Character.toChars(cp));
        }
        return out.toString();
    }
    /**
     * Return a sequence to US-ASCII.
     * 
     * @param sequence character sequence
     * @return new character sequence
     */
    public String returnToAscii(String sequence) {
        StringBuilder out = new StringBuilder(sequence.length());
        for(int i = 0; i < sequence.codePointCount(0, sequence.length()); i++) {
            int cp = sequence.codePointAt(i);
            if (cp > FULLWIDTH_ASCII_VARIANTS && 
                    cp < FULLWIDTH_ASCII_VARIANTS + 0x5f) {
                cp = cp - FULLWIDTH_ASCII_VARIANTS + 0x20;
            }
            out.append(Character.toChars(cp));
        }
        return out.toString();
    }
    /**
     * Make characters half-width (normal)
     * 
     * @param sequence character sequence
     * @return new character sequence
     */
    public String toHalfWidth(String sequence) {
        StringBuilder out = new StringBuilder(sequence.length());
        for(int i = 0; i < sequence.codePointCount(0, sequence.length()); i++) {
            int cp = sequence.codePointAt(i);
            if (cp > FULLWIDTH_ASCII_VARIANTS && 
                    cp < FULLWIDTH_ASCII_VARIANTS + 0x5f) {
                cp = cp - FULLWIDTH_ASCII_VARIANTS + 0x20;
            }
            out.append(Character.toChars(cp));
        }
        return out.toString();
    }
    /**
     * Convert available characters to double-wide (or full-width) versions
     * 
     * This converts more than just the ASCII range.
     * 
     * @param sequence character sequence
     * @return converted sequence
     */
    public String toDoubleWide(String sequence) {
        sequence = shiftAscii(sequence, FULLWIDTH_ASCII_VARIANTS);
        StringBuilder out = new StringBuilder(sequence.length());
        for(int i = 0; i < sequence.codePointCount(0, sequence.length()); i++) {
            int cp = sequence.codePointAt(i);
            switch (cp) {
            case 0x00A2: cp = 0xFFE0; break;
            case 0x00A3: cp = 0xFFE1; break;
            case 0x00AC: cp = 0xFFE2; break;
            case 0x00AF: cp = 0xFFE3; break;
            case 0x00A6: cp = 0xFFE4; break;
            case 0x00A5: cp = 0xFFE5; break;
            case 0x00A9: cp = 0xFFE6; break;
            case 0x2E28: cp = 0xFF5F; break;
            case 0x2986: cp = 0xFF60; break;
            case 0xFF61: cp = 0x3002; break;
            case 0xFF62: cp = 0x300C; break;
            case 0xFF63: cp = 0x300D; break;
            case 0xFF64: cp = 0x3001; break;
            case 0xFF65: cp = 0x30FB; break;
            case 0xFF66: cp = 0x30F2; break;
            case 0xFF67: cp = 0x30A1; break;
            case 0xFF68: cp = 0x30A3; break;
            case 0xFF69: cp = 0x30A5; break;
            case 0xFF6A: cp = 0x30A7; break;
            case 0xFF6B: cp = 0x30A9; break;
            case 0xFF6C: cp = 0x30E3; break;
            case 0xFF6D: cp = 0x30E5; break;
            case 0xFF6E: cp = 0x30C3; break;
            case 0xFF6F: cp = 0x30FC; break;
            case 0xFF70: cp = 0x30FC; break;
            case 0xFF71: cp = 0x30A2; break;
            case 0xFF72: cp = 0x30A4; break;
            case 0xFF73: cp = 0x30A6; break;
            case 0xFF74: cp = 0x30A8; break;
            case 0xFF75: cp = 0x30AA; break;
            case 0xFF76: cp = 0x30AB; break;
            case 0xFF77: cp = 0x30AD; break;
            case 0xFF78: cp = 0x30AF; break;
            case 0xFF79: cp = 0x30B1; break;
            case 0xFF7A: cp = 0x30B3; break;
            case 0xFF7B: cp = 0x30B5; break;
            case 0xFF7C: cp = 0x30B7; break;
            case 0xFF7D: cp = 0x30B9; break;
            case 0xFF7E: cp = 0x30BB; break;
            case 0xFF7F: cp = 0x30BD; break;
            case 0xFF80: cp = 0x30BF; break;
            case 0xFF81: cp = 0x30C1; break;
            case 0xFF82: cp = 0x30C4; break;
            case 0xFF83: cp = 0x30C6; break;
            case 0xFF84: cp = 0x30C8; break;
            case 0xFF85: cp = 0x30CA; break;
            case 0xFF86: cp = 0x30CB; break;
            case 0xFF87: cp = 0x30CC; break;
            case 0xFF88: cp = 0x30CD; break;
            case 0xFF89: cp = 0x30CE; break;
            case 0xFF8A: cp = 0x30CF; break;
            case 0xFF8B: cp = 0x30D2; break;
            case 0xFF8C: cp = 0x30D5; break;
            case 0xFF8D: cp = 0x30D8; break;
            case 0xFF8E: cp = 0x30DB; break;
            case 0xFF8F: cp = 0x30DE; break;
            case 0xFF90: cp = 0x30DF; break;
            case 0xFF91: cp = 0x30E0; break;
            case 0xFF92: cp = 0x30E1; break;
            case 0xFF93: cp = 0x30E2; break;
            case 0xFF94: cp = 0x30E4; break;
            case 0xFF95: cp = 0x30E6; break;
            case 0xFF96: cp = 0x30E8; break;
            case 0xFF97: cp = 0x30E9; break;
            case 0xFF98: cp = 0x30EA; break;
            case 0xFF99: cp = 0x30EB; break;
            case 0xFF9A: cp = 0x30EC; break;
            case 0xFF9B: cp = 0x30ED; break;
            case 0xFF9C: cp = 0x30EF; break;
            case 0xFF9D: cp = 0x30F3; break;
            case 0xFF9E: cp = 0x3099; break;
            case 0xFF9F: cp = 0x309A; break;
            case 0xFFC2: cp = 0x304F; break;
            case 0xFFC3: cp = 0x3050; break;
            case 0xFFC4: cp = 0x3051; break;
            case 0xFFC5: cp = 0x3052; break;
            case 0xFFC6: cp = 0x3053; break;
            case 0xFFC7: cp = 0x3054; break;
            case 0xFFCA: cp = 0x3055; break;
            case 0xFFCB: cp = 0x3056; break;
            case 0xFFCC: cp = 0x3057; break;
            case 0xFFCD: cp = 0x3058; break;
            case 0xFFCE: cp = 0x3059; break;
            case 0xFFCF: cp = 0x305A; break;
            case 0xFFD2: cp = 0x305B; break;
            case 0xFFD3: cp = 0x305C; break;
            case 0xFFD4: cp = 0x305D; break;
            case 0xFFD5: cp = 0x305E; break;
            case 0xFFD6: cp = 0x305F; break;
            case 0xFFD7: cp = 0x3060; break;
            case 0xFFDA: cp = 0x3061; break;
            case 0xFFDB: cp = 0x3062; break;
            case 0xFFDC: cp = 0x3063; break;
            default:
                if (cp >= 0xFFA1 && cp <= 0xFFBE) {
                    cp = cp - 0xFFA1 + 0x3131;
                }
                // XXX incomplete: http://unicode.org/charts/PDF/UFF00.pdf
            }
            out.append(Character.toChars(cp));
        }
        return out.toString();
    }

}
