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
package com.googlecode.blacken.swing;

import com.googlecode.blacken.colors.ColorHelper;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.grid.DirtyGridCell;
import com.googlecode.blacken.terminal.CellWalls;
import com.googlecode.blacken.terminal.TerminalCellLike;
import com.googlecode.blacken.terminal.TerminalStyle;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.GraphicAttribute;
import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An AWT cell.
 * 
 * <p>This encapsulates the AWT properties of a cell.</p>
 * 
 * @author yam655
 */
public class AwtCell implements Cloneable {
    static protected HashMap<Integer, Color> swingColor = new HashMap<>();
    static private HashMap<String, GraphicAttribute> replacement = null;
    static private Font globalFont = null;
    static private ColorPalette palette;
    /**
     * Make the AWT cell dirty
     *
     * @author yam655
     */
    static public class ResetCell implements DirtyGridCell<AwtCell> {

        /*
         * (non-Javadoc)
         * @see com.googlecode.blacken.grid.ResetGridCell#reset(com.googlecode.blacken.grid.Copyable)
         */
        @Override
        public void setDirty(AwtCell cell, boolean isDirty) {
            cell.setDirty(true);
        }

    }
    private String sequence;
    private boolean dirty;
    private Map<TextAttribute, Object> attributes = new HashMap<>();
    private EnumSet<CellWalls> cellWalls = EnumSet.noneOf(CellWalls.class);

    /**
     * Make an unset AWT cell.
     */
    public AwtCell() {
        super();
        internalReset();
    }

    private void internalReset() {
        attributes.clear();
        cellWalls = EnumSet.noneOf(CellWalls.class);
        sequence = "\u0000";
        attributes.put(TextAttribute.BACKGROUND, Color.BLACK);
        attributes.put(TextAttribute.FOREGROUND, Color.WHITE);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (sequence == null) {
            buf.append("null");
        } else {
            buf.append('"');
            for (char c : sequence.toCharArray()) {
                if (c < ' ' || c > 127) {
                    buf.append(String.format("\\u04x", c));
                } else {
                    buf.append(c);
                }
            }
            buf.append('"');
        }
        Color clr = getForegroundColor();
        if (clr == null) {
            buf.append(", null");
        } else {
            buf.append(String.format(", 0x%08x", clr.getRGB()));
        }
        clr = getBackgroundColor();
        if (clr == null) {
            buf.append(", null");
        } else {
            buf.append(String.format(", 0x%08x", clr.getRGB()));
        }
        if (dirty) {
            buf.append(", DIRTY");
        } else {
            buf.append(", clean");
        }
        if (attributes == null || attributes.isEmpty()) {
            buf.append(", {}");
        } else {
            buf.append(", {");
            for (TextAttribute att : attributes.keySet()) {
                if (att == TextAttribute.WEIGHT){
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.WEIGHT_BOLD)) {
                        buf.append("WEIGHT_BOLD");
                    } else if (value.equals(TextAttribute.WEIGHT_DEMIBOLD)) {
                        buf.append("WEIGHT_DEMIBOLD");
                    } else if (value.equals(TextAttribute.WEIGHT_DEMILIGHT)) {
                        buf.append("WEIGHT_DEMILIGHT");
                    } else if (value.equals(TextAttribute.WEIGHT_EXTRA_LIGHT)) {
                        buf.append("WEIGHT_EXTRA_LIGHT");
                    } else if (value.equals(TextAttribute.WEIGHT_EXTRABOLD)) {
                        buf.append("WEIGHT_EXTRABOLD");
                    } else if (value.equals(TextAttribute.WEIGHT_HEAVY)) {
                        buf.append("WEIGHT_HEAVY");
                    } else if (value.equals(TextAttribute.WEIGHT_LIGHT)) {
                        buf.append("WEIGHT_LIGHT");
                    } else if (value.equals(TextAttribute.WEIGHT_MEDIUM)) {
                        buf.append("WEIGHT_MEDIUM");
                    } else if (value.equals(TextAttribute.WEIGHT_REGULAR)) {
                        buf.append("WEIGHT_REGULAR");
                    } else if (value.equals(TextAttribute.WEIGHT_SEMIBOLD)) {
                        buf.append("WEIGHT_SEMIBOLD");
                    } else if (value.equals(TextAttribute.WEIGHT_ULTRABOLD)) {
                        buf.append("WEIGHT_ULTRABOLD");
                    } else {
                        float f = (Float)value;
                        buf.append(String.format("WEIGHT:%f", f));
                    }
                } else if (att == TextAttribute.WIDTH) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.WIDTH_CONDENSED)) {
                        buf.append("WIDTH_CONDENSED");
                    } else if (value.equals(TextAttribute.WIDTH_EXTENDED)) {
                        buf.append("WIDTH_EXTENDED");
                    } else if (value.equals(TextAttribute.WIDTH_REGULAR)) {
                        buf.append("WIDTH_REGULAR");
                    } else if (value.equals(TextAttribute.WIDTH_SEMI_CONDENSED)) {
                        buf.append("WIDTH_SEMI_CONDENSED");
                    } else if (value.equals(TextAttribute.WIDTH_SEMI_EXTENDED)) {
                        buf.append("WIDTH_SEMI_EXTENDED");
                    }
                } else if (att == TextAttribute.KERNING) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.KERNING_ON)) {
                        buf.append("KERNING_ON");
                    } else {
                        Integer v = (Integer)value;
                        buf.append(String.format("KERNING:%d", v));
                    }
                } else if (att == TextAttribute.LIGATURES) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.LIGATURES_ON)) {
                        buf.append("LIGATURES_ON");
                    } else {
                        Integer v = (Integer)value;
                        buf.append(String.format("LIGATURES:%d", v));
                    }
                } else if (att == TextAttribute.POSTURE) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.POSTURE_OBLIQUE)) {
                        buf.append("POSTURE_OBLIQUE");
                    } else if (value.equals(TextAttribute.POSTURE_REGULAR)) {
                        buf.append("POSTURE_REGULAR");
                    } else {
                        Float f = (Float)value;
                        buf.append(String.format("POSTURE:%f", f));
                    }
                } else if (att == TextAttribute.STRIKETHROUGH) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.STRIKETHROUGH_ON)) {
                        buf.append("STRIKETHROUGH_ON");
                    } else {
                        Integer v = (Integer)value;
                        buf.append(String.format("STRIKETHROUGH:%d", v));
                    }
                } else if (att == TextAttribute.SUPERSCRIPT) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.SUPERSCRIPT_SUB)) {
                        buf.append("SUPERSCRIPT_SUB");
                    } else if (value.equals(TextAttribute.SUPERSCRIPT_SUPER)) {
                        buf.append("SUPERSCRIPT_SUPER");
                    } else {
                        Integer v = (Integer)value;
                        buf.append(String.format("SUPERSCRIPT:%d", v));
                    }
                } else if (att == TextAttribute.SWAP_COLORS) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.SWAP_COLORS_ON)) {
                        buf.append("SWAP_COLORS_ON");
                    } else {
                        Boolean v = (Boolean)value;
                        buf.append(String.format("SWAP_COLORS:%s", v.toString()));
                    }
                } else if (att == TextAttribute.TRACKING) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.TRACKING_LOOSE)) {
                        buf.append("TRACKING_LOOSE");
                    } else if (value.equals(TextAttribute.TRACKING_TIGHT)) {
                        buf.append("TRACKING_TIGHT");
                    } else {
                        Float f = (Float)value;
                        buf.append(String.format("TRACKING:%f", f));
                    }
                } else if (att == TextAttribute.UNDERLINE) {
                    Object value = attributes.get(att);
                    if (value == null) {
                        continue;
                    } else if (value.equals(TextAttribute.UNDERLINE_LOW_DASHED)) {
                        buf.append("UNDERLINE_LOW_DASHED");
                    } else if (value.equals(TextAttribute.UNDERLINE_LOW_DOTTED)) {
                        buf.append("UNDERLINE_LOW_DOTTED");
                    } else if (value.equals(TextAttribute.UNDERLINE_LOW_GRAY)) {
                        buf.append("UNDERLINE_LOW_GRAY");
                    } else if (value.equals(TextAttribute.UNDERLINE_LOW_ONE_PIXEL)) {
                        buf.append("UNDERLINE_LOW_ONE_PIXEL");
                    } else if (value.equals(TextAttribute.UNDERLINE_LOW_TWO_PIXEL)) {
                        buf.append("UNDERLINE_LOW_TWO_PIXEL");
                    } else if (value.equals(TextAttribute.UNDERLINE_ON)) {
                        buf.append("UNDERLINE_ON");
                    } else {
                        Integer i = (Integer)value;
                        buf.append(String.format("UNDERLING:%d", i));
                    }
                } else if (att == TextAttribute.NUMERIC_SHAPING) {
                    NumericShaper ns = (NumericShaper)attributes.get(att);
                    if (ns == null) {
                        continue;
                    }
                    buf.append(ns.toString());
                } else if (att == TextAttribute.CHAR_REPLACEMENT) {
                    GraphicAttribute ga = (GraphicAttribute)attributes.get(att);
                    if (ga == null) {
                        continue;
                    }
                    buf.append(ga.toString());
                } else if (att == TextAttribute.FONT) {
                    Font f = (Font)attributes.get(att);
                    if (f == null) {
                        continue;
                    }
                    buf.append(f.toString());
                }
                buf.append(", ");
            }
            buf.append("}");
        }
        if (cellWalls == null || cellWalls.isEmpty()) {
            buf.append(", {}");
        } else {
            buf.append(", {");
            for (CellWalls wall : cellWalls) {
                buf.append(wall.name());
                buf.append(", ");
            }
            buf.append("}");

        }
        return buf.toString();
    }

    /**
     * Create a new simple AWT cell
     *
     * @param glyph the character sequence
     * @param background the background color
     * @param foreground the foreground color
     * @param dirty the dirty status
     */
    public AwtCell(String glyph, Color background, Color foreground,
                   boolean dirty) {
        super();
        setCell(glyph, background, foreground);
        this.dirty = dirty;
    }

    /**
     * Create a new AWT cell based upon an existing cell.
     *
     * @param source source cell
     * @deprecated Use set(AwtCell) or clone() instead.
     */
    public AwtCell(AwtCell source) {
        super();
        set(source);
    }
    /**
     * Add a character sequence to a cell
     * @param glyph a character sequence
     */
    public void addGlyph(int glyph) {
        this.sequence += String.copyValueOf(Character.toChars(glyph));
        dirty = true;
    }
    /**
     * Add a character sequence to a cell
     * @param glyph a character sequence
     */
    public void addGlyph(String glyph) {
        this.sequence += glyph;
        dirty = true;
    }

    /**
     * Clear the text attributes, but not the colors.
     *
     * <p>While generally we treat the foreground and background color as
     * simply attributes, this function avoids clearing them. This allows
     * us to hope that the character has a better chance of remaining
     * visible.</p>
     */
    public void clearTextAttributes() {
        Color background = (Color)attributes.get(TextAttribute.BACKGROUND);
        Color foreground = (Color)attributes.get(TextAttribute.FOREGROUND);
        Font f = (Font)attributes.get(TextAttribute.FONT);
        attributes.clear();
        attributes.put(TextAttribute.BACKGROUND, background);
        attributes.put(TextAttribute.FOREGROUND, foreground);
        attributes.put(TextAttribute.FONT, f);
        dirty = true;
    }
    /**
     * Clear the cell walls.
     */
    public void clearCellWalls() {
        this.cellWalls = EnumSet.noneOf(CellWalls.class);
    }

    @Override
    public AwtCell clone() {
        AwtCell ret = new AwtCell();
        ret.set(this);
        ret.setDirty(this.dirty);
        return ret;
    }
    /**
     * Get an attributed string of the character sequence.
     * @return attributed string
     */
    public AttributedString getAttributedString() {
        AttributedString ret = new AttributedString(sequence, attributes);
        return ret;
    }
    /**
     * Get the attributes
     * @return attributes
     */
    public Map<TextAttribute, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Get the background color
     * @return background color
     */
    public Color getBackgroundColor() {
        return (Color)attributes.get(TextAttribute.BACKGROUND);
    }

    /**
     * Get the AWT cell.
     * @return the AWT cell.
     */
    public AwtCell getCell() {
        return this;
    }
    /**
     * Get the cell walls
     * @return the cell walls
     */
    public Set<CellWalls> getCellWalls() {
        return Collections.unmodifiableSet(cellWalls);
    }
    /**
     * Get the font.
     * @return the font
     */
    public Font getFont() {
        return (Font)attributes.get(TextAttribute.FONT);
    }
    /**
     * Get the foreground color
     * @return foreground color
     */
    public Color getForegroundColor() {
        return (Color)attributes.get(TextAttribute.FOREGROUND);
    }

    /**
     * Get the character sequence.
     * @return the character sequence
     */
    public String getSequence() {
        return sequence;
    }
    /**
     * Is the cell dirty?
     * @return dirty status
     */
    public boolean isDirty() {
        return dirty;
    }
    /**
     * Unset a text attribute
     * @param key text attribute key
     * @return previous value
     */
    public Object unsetAttribute(TextAttribute key) {
        if (attributes.containsKey(key)) {
            dirty = true;
            return attributes.remove(key);
        }
        return null;
    }
    /**
     * Set a text attribute
     *
     * @param key the key
     * @param value the value
     * @return the previous value
     */
    public Object setTextAttribute(TextAttribute key,
                               Object value) {
        dirty = true;
        if (value == null) {
            return attributes.remove(key);
        }
        return attributes.put(key, value);
    }
    /**
     * Set the text attributes to an existing map
     * @param attributes new text attributes
     */
    public void setTextAttributes(Map<TextAttribute, Object> attributes) {
        if (attributes != null) { // && !attributes.equals(this.attributes)) {
            clearTextAttributes();
            dirty = true;
            this.attributes.putAll(attributes);
        }
    }
    /**
     * Set the background color
     * @param background new background color
     */
    public void setBackgroundColor(Color background) {
        if (background != null) {
            if (!background.equals(attributes.get(TextAttribute.BACKGROUND))) {
                attributes.put(TextAttribute.BACKGROUND, background);
                dirty = true;
            }
        }
    }

    /**
     * Set the cell to an existing cell
     * @param cell new cell
     * @deprecated use 'set' instead.
     */
    @Deprecated
    public void setCell(AwtCell cell) {
        this.set(cell);
    }

    /**
     * Set the cell to an existing cell
     * @param cell new cell
     */
    public void set(AwtCell cell) {
        if (cell == null) {
            setCell("\u0000", Color.BLACK, Color.WHITE);
            clearTextAttributes();
            clearCellWalls();
        } else {
            this.sequence = cell.sequence;
            setTextAttributes(cell.attributes);
            setCellWalls(cell.cellWalls);
        }
        dirty = true;
    }

    /**
     * Set some common parts of a cell.
     *
     * @param glyph the character sequence
     * @param background the background
     * @param foreground the foreground
     */
    public void setCell(int glyph, Color background, Color foreground) {
        setSequence(glyph);
        setForegroundColor(foreground);
        setBackgroundColor(background);
    }
    /**
     * Set the core components of a cell.
     * @param glyph the character sequence
     * @param attributes the text attributes
     */
    public void setCell(int glyph, Map<TextAttribute, Object> attributes) {
        setSequence(glyph);
        if (attributes != null) {
            setTextAttributes(attributes);
        }
    }
    /**
     * Set the common parts of a cell
     * @param glyph a character sequence
     * @param background the background
     * @param foreground the foreground
     */
    public void setCell(String glyph, Color background, Color foreground) {
        setSequence(glyph);
        setForegroundColor(foreground);
        setBackgroundColor(background);
    }

    /**
     * Set the cell using the better common form
     *
     * @param glyph the character sequence
     * @param attributes the text attributes
     */
    public void setCell(String glyph, Map<TextAttribute, Object> attributes) {
        this.sequence = glyph;
        setTextAttributes(attributes);
    }

    /**
     * Set all of a cell.
     *
     * @param sequence the character sequence
     * @param attributes the text attributes
     * @param walls the cell walls
     */
    public void setCell(String sequence, Map<TextAttribute, Object> attributes,
                        EnumSet<CellWalls> walls) {
        setSequence(sequence);
        setTextAttributes(attributes);
        setCellWalls(walls);
    }
    /**
     * Set a single cell wall
     * @param walls wall to set
     */
    public void setCellWalls(CellWalls walls) {
        if (walls != null && !walls.equals(this.cellWalls)) {
            this.cellWalls = EnumSet.of(walls);
            dirty = true;
        }
    }
    /**
     * Set the cell walls
     * @param walls complete set cell walls
     */
    public void setCellWalls(Set<CellWalls> walls) {
        if (walls != null && !walls.equals(this.cellWalls)) {
            if (walls.isEmpty()) {
                cellWalls = EnumSet.noneOf(CellWalls.class);
            } else {
                cellWalls = EnumSet.copyOf(walls);
            }
            dirty = true;
        }
    }
    /**
     * Set the dirty state of the cell
     * @param dirty dirty state
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    /**
     * Set the font to use.
     * @param font the font to use
     */
    public void setFont(Font font) {
        if (font != null) {
            attributes.put(TextAttribute.FONT, font);
            dirty = true;
        }
    }
    /**
     * Set the foreground color
     * @param foreground color
     */
    public void setForegroundColor(Color foreground) {
        if (foreground != null) {
            attributes.put(TextAttribute.FOREGROUND, foreground);
            dirty = true;
        }
    }
    /**
     * Set the sequence
     * @param sequence new sequence
     */
    public void setSequence(int sequence) {
        this.sequence = String.copyValueOf(Character.toChars(sequence));
        dirty = true;
    }
    /**
     * Set the sequence
     * @param sequence new sequence
     */
    public void setSequence(String sequence) {
        if (sequence == null || sequence.length() == 0) {
            sequence = "\u0000";
        }
        if (this.sequence == null) {
            this.sequence = sequence;
        } else if (!this.sequence.equals(sequence)) {
            this.sequence = sequence;
            dirty = true;
        }
    }

    public static AwtCell makeAwtFromTerminal(Class<? extends AwtCell> clazz, final TerminalCellLike term) {
        AwtCell awt;
        try {
            awt = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        return setAwtFromTerminal(awt, term);
    }

    public static AwtCell makeAwtFromTerminal(final TerminalCellLike term) {
        return setAwtFromTerminal(new AwtCell(), term);
    }

    public static AwtCell setAwtFromTerminal(AwtCell awt, final TerminalCellLike term) {
        if (term == null) {
            awt.attributes.put(TextAttribute.FONT, globalFont);
            return awt;
        }
        awt.dirty = true;
        String sequence = term.getSequence();
        if (sequence == null || sequence.length() == 0) {
            sequence = "\u0000";
        }
        awt.sequence = sequence;
        Set<CellWalls> w = term.getCellWalls();
        EnumSet<CellWalls> walls;
        if (w == null || w.isEmpty()) {
            walls = EnumSet.noneOf(CellWalls.class);
        } else {
            walls = EnumSet.copyOf(w);
        }
        awt.cellWalls = walls;
        awt.attributes.clear();

        Set<TerminalStyle> styles = term.getStyle();
        int fore = term.getForeground();
        int back = term.getBackground();
        if (styles.contains(TerminalStyle.STYLE_REVERSE)) {
            int r = fore;
            fore = back;
            back = r;
        }
        if (styles.contains(TerminalStyle.STYLE_DIM)) {
            fore = makeDim(fore);
        }
        Map<TextAttribute, Object> attrs = awt.attributes;
        attrs.put(TextAttribute.BACKGROUND, AwtCell.getSwingColor(back));
        attrs.put(TextAttribute.FOREGROUND, AwtCell.getSwingColor(fore));
        // attrs.put(TextAttribute.FAMILY, Font.MONOSPACED);
        if (styles.contains(TerminalStyle.STYLE_LIGHT)) {
            if (styles.contains(TerminalStyle.STYLE_BOLD)) {
                if (styles.contains(TerminalStyle.STYLE_HEAVY)) {
                    // STYLE_LIGHT | STYLE_BOLD | STYLE_HEAVY
                    // This is currently undefined.
                } else {
                    // STYLE_LIGHT | STYLE_BOLD
                    attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_LIGHT);
                }
            } else if (styles.contains(TerminalStyle.STYLE_HEAVY)) {
                // STYLE_LIGHT | STYLE_HEAVY
                attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_MEDIUM);
            } else {
                // STYLE_LIGHT
                attrs.put(TextAttribute.WEIGHT,
                          TextAttribute.WEIGHT_EXTRA_LIGHT);
            }
        } else if (styles.contains(TerminalStyle.STYLE_BOLD)) {
            if (styles.contains(TerminalStyle.STYLE_HEAVY)) {
                // STYLE_BOLD | STYLE_HEAVY
                attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_ULTRABOLD);
            } else {
                // STYLE_BOLD
                attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            }

        } else if (styles.contains(TerminalStyle.STYLE_HEAVY)) {
            // STYLE_HEAVY
            attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_HEAVY);
        }
        for(TerminalStyle style : styles) {
            switch (style) {
            case STYLE_LIGHT:
            case STYLE_BOLD:
            case STYLE_HEAVY:
                break; // handled elsewhere

            case STYLE_NARROW:
                attrs.put(TextAttribute.WIDTH, TextAttribute.WIDTH_CONDENSED);
                break;
            case STYLE_WIDE:
                attrs.put(TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED);
                break;
            // What is STYLE_NARROW | STYLE_WIDE ?

            case STYLE_ITALIC:
                attrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
                break;

            // Mapped to SUPERSCRIPT (possibly *unclean* mapping)
            case STYLE_SUPERSCRIPT: // SUPERSCRIPT_SUPER
                attrs.put(TextAttribute.SUPERSCRIPT,
                          TextAttribute.SUPERSCRIPT_SUPER);
                break;
            case STYLE_SUBSCRIPT: // SUPERSCRIPT_SUB
                attrs.put(TextAttribute.SUPERSCRIPT,
                          TextAttribute.SUPERSCRIPT_SUB);
                break;
            // Is there a STYLE_SUPERSCRIPT | STYLE_SUBSCRIPT ?

            case STYLE_INVISIBLE:
                awt.sequence = "\u0000";
                break;
            case STYLE_REPLACEMENT:
                awt.sequence = "\uFFFC";
                String s = term.getSequence();
                if (replacement != null && replacement.containsKey(s)) {
                    attrs.put(TextAttribute.CHAR_REPLACEMENT, replacement.get(s));
                }
                break;

            // Mapped to UNDERLINE
            case STYLE_UNDERLINE: // UNDERLINE_ON
                attrs.put(TextAttribute.UNDERLINE,
                          TextAttribute.UNDERLINE_ON);
                break;

            // Mapped to STRIKETHROUGH
            case STYLE_STRIKETHROUGH: // STRIKETHROUGH_ON
                attrs.put(TextAttribute.STRIKETHROUGH,
                          TextAttribute.STRIKETHROUGH_ON);
                break;

            case STYLE_REVERSE:
                /* handled elsewhere */
                break;
            case STYLE_DIM:
                /* handled elsewhere */
                break;
            }
        }
        return awt;
    }

    public static HashMap<String, GraphicAttribute> getReplacement() {
        return replacement;
    }

    public static void setReplacement(HashMap<String, GraphicAttribute> replacement) {
        AwtCell.replacement = replacement;
    }

    public static Font getGlobalFont() {
        return globalFont;
    }

    public static void setGlobalFont(Font globalFont) {
        AwtCell.globalFont = globalFont;
    }


    /**
     * We do not cache the entire dim palette at palette-load as it isn't
     * expected that many applications will make use of it.
     *
     * @param color standard (opaque) color in an
     * @return
     */
    protected static int makeDim(final int color) {
        return ColorHelper.increaseAlpha(color, -0.20);
    }


    protected static Color getSwingColor(int c) {
        Color clr;
        ColorPalette palette = getPalette();
        if (palette != null) {
            c = palette.getColor(c);
        }
        if (swingColor.containsKey(c)) {
            clr = swingColor.get(c);
        } else {
            clr = new Color(c);
            swingColor.put(c, clr);
        }
        return clr;
    }

    public static ColorPalette getPalette() {
        return palette;
    }

    public static ColorPalette setPalette(ColorPalette palette) {
        ColorPalette old = AwtCell.palette;
        AwtCell.palette = palette;
        swingColor.clear();
        if (palette != null) {
            for (int c : palette) {
                swingColor.put(c, new Color(c));
            }
        }
        return old;
    }

}
