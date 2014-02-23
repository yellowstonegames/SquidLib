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
package com.googlecode.blacken.nativeswing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.blacken.core.ListMap;
import com.googlecode.blacken.colors.ColorHelper;
import com.googlecode.blacken.colors.ColorNames;
import com.googlecode.blacken.exceptions.InvalidStringFormatException;

/**
 * A named and indexed color palette.
 * 
 * <p>This leverages {@link ListMap} to create a color palette that supports 
 * both the common use-cases of index-based access as well as name-based 
 * access. See {@link ColorNames} for standard palettes to use with this.</p>
 * 
 * <p>The greatest flexibility is found in the {@link #addMapping(String[])}
 * and {@link #putMapping(String[])} functions.<p>
 * 
 * @author Steven Black
 */
public class AwtPalette extends ListMap<String, Color> {

    /**
     * serial ID
     */
    private static final long serialVersionUID = 3453955962929981683L;

    /**
     * Create a new empty palette.
     */
    public AwtPalette() {
        super();
    }

    /**
     * Create a new color palette based upon an collection of color values.
     * 
     * @param c simple color list
     */
    public AwtPalette(Collection<Color> c) {
        super(c);
    }

    /**
     * Create a new empty palette.
     * 
     * @param initialCapacity initial capacity
     */
    public AwtPalette(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Create a palette based upon an existing ListMap/palette.
     * 
     * @param colors previous ColorPalette-like ListMap
     */
    public AwtPalette(ListMap<String, Color> colors) {
        super(colors.size());
        addAll(colors);
    }

    /**
     * Create a palette based upon an existing palette and an explicit order.
     * 
     * <p>This is used to change the order of a palette.</p>
     * 
     * <p>Note that due to the use of {@link #putAll(ListMap, String[])}
     * items in the <code>existingPalette</code> which do not exist in the
     * <code>order</code> will not exist in the returning palette.</p> 
     * 
     * @param existingPalette existing palette
     * @param order explicit order
     */
    public AwtPalette(ListMap<String, Color> existingPalette, 
                        String[] order) {
        super(order.length);
        putAll(existingPalette, order);
    }

    /**
     * Create a palette based upon a simple Map.
     * 
     * <p>The index will be inconsistent if the Map does not have a known 
     * order, such as by use of {@link LinkedHashMap}.</p>
     * 
     * @param colors name to color value mapping
     */
    public AwtPalette(Map<String, Color> colors) {
        super(colors.size());
        addAll(colors.entrySet());
    }

    /**
     * Create a palette with the given named colors.
     * 
     * @param colors name to color value mapping
     * @param order order of the palette
     */
    public AwtPalette(Map<String, Color> colors, String[] order) {
        super(order.length);
        if (order.length > 0) {
            putAll(colors, order);
        } else {
            putAll(colors);
        }
    }

    /**
     * Add both an indexed color, as well as a name for the color. 
     * 
     * <p>This makes no attempt to prevent duplication in the index or in 
     * overwriting the existing value in the nameMap.</p>
     * 
     * @param name The name of the color
     * @param rgba The RGB/RGBA color value (never transparent)
     * @return index of color
     */
    public int add(String name, int rgba) {
        rgba = ColorHelper.neverTransparent(rgba);
        return this.add(name, rgba, false);
    }

    /**
     * Add both an indexed color, as well as a name for the color. 
     * 
     * <p>This currently makes no attempt to prevent duplication in the index 
     * or in overwriting the existing value in the nameMap.</p>
     * 
     * @param name The name of the color
     * @param rgba The color value
     * @param full_alpha when true, neverTransparent is not in effect
     * @return index of color
     */
    public int add(String name, int rgba, boolean full_alpha) {
        if (!full_alpha) {
            rgba = ColorHelper.neverTransparent(rgba);
        }
        int idx = this.size();
        this.add(name, new Integer(rgba));
        put(name, new Color(rgba));
        return idx;
    }

    /**
     * Add both an indexed color, as well as a name for the color. This makes
     * no attempt to prevent duplication in the index or in overwriting the
     * existing value in the nameMap.
     * 
     * <p>This includes a call to {@link ColorHelper#neverTransparent(int)} so
     * \#112233 will show the same as 0xFF112233 -- as you would expect.</p>
     * 
     * @param name The name of the color
     * @param colordef The text color definition (#RRGGBB, 0xAARRGGBB, etc)
     * @return true on success
     * @throws InvalidStringFormatException 
     *          <code>colordef</code> provided was illegal.
     */
    public int add(String name, String colordef) 
    throws InvalidStringFormatException {
        final Integer color = ColorHelper.makeColor(colordef);
        this.add(name, color);
        return indexOfKey(name);
    }

    /**
     * This is designed as a helper to load a palette based upon color
     * values. It does not wipe the existing palette.
     * 
     * <p>It is optimized for reuse of the Integer objects, if available. This
     * means it will walk your indexed palette as well as your nameMap, as
     * there is never any guarantee that they will have the same colors.
     * (However it walks these only once.)</p>
     * 
     * @param colors an array colors stored as ints (0xRRGGBB or 0xAARRGGBB)
     * @param has_alpha indicates whether a valid alpha channel is present
     * @return true if the Collection was modified
     */
    public boolean addAll(int[] colors, boolean has_alpha) {
        if (colors.length == 0) {
            return false;
        }
        for (int c : colors) {
            if (!has_alpha) {
                c = ColorHelper.neverTransparent(c);
                // regardless of has_alpha, c now contains RGBA data
            }
            add(new Color(c));
        }
        return true;
    }

    /**
     * This command accepts an array of strings and turns them as additional
     * names in a color palette. The format of the strings are as follows:
     * 
     * <pre>
     * color -> #RRGGBB
     * name with spaces -> #00aa22
     * name / alternate name -> #ffffff
     * Name / Case Sensitive -> #a98765
     * remember hex works, too -> 0x12ab9f
     * hex supports alpha -> 0x11335577
     * space/is->needed -> #098abd
     * web shorthand -> #39f
     * </pre>
     * 
     * <p>All the above are valid (except #RRGGBB is not -- it only indicates
     * these are standard red-green-blue hex values), but only
     * "name / alternate name" produced more than one name mapping. (It
     * produces two names for the same color.) The spaces are required
     * before/after the "->" and the "/" for them to have meaning. The
     * behavior is very much like the load(Strings[]) function, except this
     * does not clear the palette, and it does not put the colors on the list
     * of colors available by index position.</p>
     * 
     * <p>Note that hex has full support for alpha -- if you specify 8 hex 
     * digits it will allow you to set zero (0) alpha making it fully 
     * transparent. If you only specify 6 digits it performs the standard
     * {@link ColorHelper#neverTransparent(int)} logic.</p>
     * 
     * @param colors
     *            array of strings containing color mapping information
     * @return true if the colors were set, false otherwise
     */
    public boolean addMapping(String[] colors) {
        return processMapping(colors, true);
    }
    /**
     * Helper function to bypass the palette if it wasn't used.
     * 
     * <p>To support environments where setting a palette is optional,
     * we use this function to check to see if a color value is a valid
     * color index -- if it is not we consider it a 0xAARRGGBB color.<p>
     * 
     * <p>Note: Due to the implementation of this function, and the fact that
     * is performs an educated guess, if you use fully transparent colors
     * (with an Alpha of 0) this can make a wrong guess, particularly for dark
     * blue colors or if index zero is not black. The work-around is to use an 
     * Alpha of 1.</p>
     * 
     * @param indexOrColor an index in the palette or 0xAARRGGBB color
     * @return a 0xAARRGGBB color
     */
    public Color getColor(int indexOrColor) {
        Color ret = null;
        if (indexOrColor < size() && indexOrColor >= 0) {
            // This is near guaranteed correct.
            ret = get(indexOrColor);
        } else {
            ret = new Color(indexOrColor);
        }
        return ret;
    }

    /**
     * Create a mapping array from the palette
     * 
     * @return mapping array
     */
    public String[] getMapping() {
        List<String> ret = new ArrayList<String>(size());
        Map<Integer, ConsolidatedListMapEntry<String, Color>> ce = 
            new HashMap<Integer, ConsolidatedListMapEntry<String, Color>>();
        this.consolidateEntries(ce );
        for (Integer i = 0; i < ce.size(); i++) {
            ConsolidatedListMapEntry<String, Color> e = ce.get(i);
            StringBuffer buf = new StringBuffer();
            List<String> sortKeys = new ArrayList<String>(e.getKeys());
            Collections.sort(sortKeys);
            for (String key : sortKeys) {
                if (buf.length() > 0) {
                    buf.append(" / "); //$NON-NLS-1$
                }
                buf.append(key);
            }
            if (buf.length() > 0) {
                buf.append(" -> "); //$NON-NLS-1$
            }
            buf.append(String.format("%#08x", e.getValue())); //$NON-NLS-1$
            ret.add(buf.toString());
        }
        return ret.toArray(new String[0]);
    }

    /**
     * Process both the add and put mapping commands
     * @param colors color mapping array
     * @param onlyAdd true: only do add; false: do put or add
     * @return true if change, false otherwise
     */
    private boolean processMapping(String[] colors, boolean onlyAdd) {
        if (colors == null || colors.length == 0) {
            return false;
        }
        for (final String color : colors) {
            final String s[] = color.split("[ \t]+->[ \t]+", 2); //$NON-NLS-1$
            final String names[];
            final String colorDef;
            if (s.length == 1) {
                if (ColorHelper.isColorDefinition(s[0])) {
                    colorDef = s[0];
                    names = new String[0];
                } else {
                    throw new RuntimeException(String.format
                                           ("Color format lacked '->': %s",  //$NON-NLS-1$
                                            color));
                }
            } else {
                names = s[0].split("[ \t]+/[ \t]+"); //$NON-NLS-1$
                colorDef = s[1];
            }
            Integer colr;
            try {
                colr = ColorHelper.makeColor(colorDef);
            } catch (InvalidStringFormatException e) {
                throw new RuntimeException(e);
            }
            Integer idx = -1;
            if (!onlyAdd) {
                for (final String n : names) {
                    if (this.containsKey(n)) {
                        idx = this.indexOfKey(n);
                        break;
                    }
                }
            }
            if (idx == -1) {
                if (names.length == 0) {
                    add(new Color(colr));
                } else {
                    add(names, new Color(colr));
                }
            } else {
                this.set(idx, new Color(colr));
                putKey(names, idx);
            }
        }
        return true;
    }

    /**
     * Update both an indexed color, as well as a name for the color. This will
     * overwrite an existing value.
     * 
     * <p>This includes a call to {@link ColorHelper#neverTransparent(int)} so
     * \#112233 will show the same as 0xFF112233 -- as you would expect.</p>
     * 
     * @param name The name of the color
     * @param colordef The text color definition (#RRGGBB, 0xAARRGGBB, etc)
     * @return true on success
     * @throws InvalidStringFormatException 
     *          <code>colordef</code> provided was illegal.
     */
    public int put(String name, String colordef) 
    throws InvalidStringFormatException {
        final Integer color = ColorHelper.makeColor(colordef);
        this.put(name, new Color(color));
        return indexOfKey(name);
    }
    
    /**
     * This command accepts an array of strings and turns them in to a color
     * palette. The format of the strings are as follows:
     * 
     * <pre>
     * color -> #RRGGBB
     * name with spaces -> #00aa22
     * name / alternate name -> #ffffff
     * Name / Case Sensitive -> #a98765
     * remember hex works, too -> 0x12ab9f
     * hex supports alpha -> 0x11335577
     * space/is->needed -> #098abd
     * web shorthand -> #39f
     * </pre>
     * 
     * <p>All the above are valid (except #RRGGBB is not -- it only indicates
     * these are standard red-green-blue hex values), but only
     * "name / alternate name" produced more than one name mapping. (It
     * produces two names for the same color.) The spaces are required
     * before/after the "->" and the "/" for them to have meaning.</p>
     * 
     * <p>Note that hex has full support for alpha -- if you specify 8 hex 
     * digits it will allow you to set zero (0) alpha making it fully 
     * transparent. If you only specify 6 digits it performs the standard
     * {@link ColorHelper#neverTransparent(int)} logic.</p>
     * 
     * @param colors
     *            array of strings containing color mapping information
     * @return true if the colors were set, false otherwise
     */
    public boolean putMapping(String[] colors) {
        return processMapping(colors, false);
    }
}
