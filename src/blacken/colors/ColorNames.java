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

package com.googlecode.blacken.colors;

/**
 * Container class to help create standard color palettes. 
 * 
 * <p>The functions here are used with the {@link ColorPalette} class to get
 * standard color sets.</p>
 * 
 * @author Steven Black
 */
public final class ColorNames {
    /**
     * HTML 3/4 official colors
     * 
     * <p>The first 16 colors are standard VGA colors.</p>
     * 
     * <p>Additions to the standard VGA colors and HTML 3/4 color names
     * include the <code>grey</code> spelling, and the <code>orange</code> 
     * defined in CSS 2.1</p>
     * 
     * <p>Described on http://en.wikipedia.org/wiki/Web_colors</p>
     */
    static public String[] HTML_COLORS = 
    {
     "Black / black -> #000000",
     "Maroon / maroon -> #800000",
     "Green / green -> #008000",
     "Olive / olive -> #808000",
     "Navy / navy -> #000080",
     "Purple / purple -> #800080",
     "Teal / teal -> #008080",
     "Silver / silver -> #c0c0c0",
     "Gray / Grey / gray / grey -> #808080",
     "Red / red -> #ff0000",
     "Lime / lime -> #00ff00",
     "Yellow / yellow -> #ffff00",
     "Blue / blue -> #0000ff",
     "Fuchsia / Magenta / fuchsia / magenta -> #ff00ff",
     "Aqua / Cyan / aqua / cyan -> #00ffff",
     "White / white -> #ffffff",
     "Orange / orange -> #ffa500",
    };

    /**
     * The color names defined for SVG (and HTML5 legacy)
     * 
     * <p>Prior to HTML5 there was no HTML standard for named colors. HTML5 
     * specifies the named colors come from the SVG named colors.
     * 
     * <p>These come from http://www.w3.org/TR/SVG/types.html
     *
     * <p>While these colors primarily exist for the names (and in previous
     * versions had an undefined order), this has changed so that first 16
     * colors are VGA-standard in their order, and the 17th is the Orange
     * added for HTML. This means you can go from a 16 color indexed palette
     * to this palette cleanly.
     */
    static public String[] SVG_COLORS = 
    {
        "Black -> #000000",
        "Maroon -> #800000",
        "Green -> #008000",
        "Olive -> #808000",
        "Navy -> #000080",
        "Purple -> #800080",
        "Teal -> #008080",
        "Silver -> #c0c0c0",
        "Gray / Grey -> #808080",
        "Red -> #ff0000",
        "Lime -> #00ff00",
        "Yellow -> #ffff00",
        "Blue -> #0000ff",
        "Fuchsia / Magenta -> #ff00ff",
        "Aqua / Cyan -> #00ffff",
        "White -> #ffffff",
        "Orange -> #ffa500",
        "AliceBlue -> #f0f8ff",
        "AntiqueWhite -> #faebd7",
        "Aquamarine -> #7fffd4",
        "Azure -> #f0ffff",
        "Beige -> #f5f5dc",
        "Bisque -> #ffe4c4",
        "BlanchedAlmond -> #ffebcd",
        "BlueViolet -> #8a2be2",
        "Brown -> #a52a2a",
        "BurlyWood -> #deb887",
        "CadetBlue -> #5f9ea0",
        "Chartreuse -> #7fff00",
        "Chocolate -> #d2691e",
        "Coral -> #ff7f50",
        "CornflowerBlue -> #6495ed",
        "Cornsilk -> #fff8dc",
        "Crimson -> #dc143c",
        "DarkBlue -> #00008b",
        "DarkCyan -> #008b8b",
        "DarkGoldenrod / DarkGoldenRod -> #b8860b",
        "DarkGray / DarkGrey -> #a9a9a9",
        "DarkGreen -> #006400",
        "DarkKhaki -> #bdb76b",
        "DarkMagenta -> #8b008b",
        "DarkOliveGreen -> #556b2f",
        "DarkOrange -> #ff8c00",
        "DarkOrchid -> #9932cc",
        "DarkRed -> #8b0000",
        "DarkSalmon -> #e9967a",
        "DarkSeaGreen -> #8fbc8f",
        "DarkSlateBlue -> #483d8b",
        "DarkSlateGray / DarkSlateGrey -> #2f4f4f",
        "DarkTurquoise -> #00ced1",
        "DarkViolet -> #9400d3",
        "Deeppink -> #ff1493",
        "Deepskyblue -> #00bfff",
        "DimGray / DimGrey -> #696969",
        "DodgerBlue -> #1e90ff",
        "Firebrick / FireBrick -> #b22222",
        "FloralWhite -> #fffaf0",
        "ForestGreen -> #228b22",
        "Gainsboro -> #dcdcdc",
        "GhostWhite -> #f8f8ff",
        "Gold -> #ffd700",
        "Goldenrod / GoldenRod -> #daa520",
        "GreenYellow -> #adff2f",
        "Honeydew / HoneyDew -> #f0fff0",
        "HotPink -> #ff69b4",
        "IndianRed -> #cd5c5c",
        "Indigo -> #4b0082",
        "Ivory -> #fffff0",
        "Khaki -> #f0e68c",
        "Lavender -> #e6e6fa",
        "LavenderBlush -> #fff0f5",
        "LawnGreen -> #7cfc00",
        "LemonChiffon -> #fffacd",
        "LightBlue -> #add8e6",
        "LightCoral -> #f08080",
        "LightCyan -> #e0ffff",
        "LightGoldenrodYellow / LightGoldenRodYellow -> #fafad2",
        "LightGray / LightGrey -> #d3d3d3",
        "LightGreen -> #90ee90",
        "LightPink -> #ffb6c1",
        "LightSalmon -> #ffa07a",
        "LightSeaGreen -> #20b2aa",
        "LightSkyBlue -> #87cefa",
        "LightSlateGray / LightSlateGrey -> #778899",
        "LightSteelBlue -> #b0c4de",
        "LightYellow -> #ffffe0",
        "LimeGreen -> #32cd32",
        "Linen -> #faf0e6",
        "MediumAquaMarine -> #66cdaa",
        "MediumBlue -> #0000cd",
        "MediumOrchid -> #ba55d3",
        "MediumPurple -> #9370db",
        "MediumSeagreen -> #3cb371",
        "MediumSlateBlue -> #7b68ee",
        "MediumSpringGreen -> #00fa9a",
        "MediumTurquoise -> #48d1cc",
        "MediumVioletRed -> #c71585",
        "MidnightBlue -> #191970",
        "MintCream -> #f5fffa",
        "MistyRose -> #ffe4e1",
        "Moccasin -> #ffe4b5",
        "NavajoWhite -> #ffdead",
        "OldLace -> #fdf5e6",
        "OliveDrab -> #6b8e23",
        "OrangeRed -> #ff4500",
        "Orchid -> #da70d6",
        "PaleGoldenrod / PaleGoldenRod -> #eee8aa",
        "PaleGreen -> #98fb98",
        "PaleTurquoise -> #afeeee",
        "PaleVioletRed -> #db7093",
        "PapayaWhip -> #ffefd5",
        "PeachPuff -> #ffdab9",
        "Peru -> #cd853f",
        "Pink -> #ffc0cb",
        "Plum -> #dda0dd",
        "PowderBlue -> #b0e0e6",
        "RosyBrown -> #bc8f8f",
        "RoyalBlue -> #4169e1",
        "SaddleBrown -> #8b4513",
        "Salmon -> #fa8072",
        "SandyBrown -> #f4a460",
        "SeaGreen -> #2e8b57",
        "SeaShell / Seashell -> #fff5ee",
        "Sienna -> #a0522d",
        "SkyBlue -> #87ceeb",
        "SlateBlue -> #6a5acd",
        "SlateGray / SlateGrey -> #708090",
        "Snow -> #fffafa",
        "SpringGreen -> #00ff7f",
        "SteelBlue -> #4682b4",
        "Tan -> #d2b48c",
        "Thistle -> #d8bfd8",
        "Tomato -> #ff6347",
        "Turquoise -> #40e0d0",
        "Violet -> #ee82ee",
        "Wheat -> #f5deb3",
        "WhiteSmoke -> #f5f5f5",
        "YellowGreen -> #9acd32",
    };

    /**
     * Standard Xterm 16-color definitions.
     * 
     * <p>To use:
     * <pre>
     *       palette.addAll(XTERM_16_COLORS, false);
     * </pre></p>
     * 
     * <p>From xterm, these are defined as:
     * <ol>
     *  <li>black
     *  <li>red3
     *  <li>green3
     *  <li>yellow3
     *  <li>blue2
     *  <li>magenta3
     *  <li>cyan3
     *  <li>gray90
     *  <li>gray50
     *  <li>red
     *  <li>green
     *  <li>yellow
     *  <li>rgb:0x5c5cff
     *  <li>magenta
     *  <li>cyan
     *  <li>white
     * </ol>
     */
    static public int[] XTERM_16_COLORS = 
    {
     0x000000, 0xcd0000, 0x00cd00, 0xcdcd00,
     0x0000ee, 0xcd00cd, 0x00cdcd, 0xe5e5e5,
     0x7f7f7f, 0xff0000, 0x00ff00, 0xffff00,
     0x5c5cff, 0xff00ff, 0x00ffff, 0xffffff,
    };

    /**
     * Standard xterm 88-color definitions.
     * 
     * <p>To use:
     * <pre>
     *       palette.addAll(XTERM_88_COLORS, false);
     * </pre></p>
     * 
     * <p>The first 16 colors are all the same.
     * However the later colors are computed by a formula.
     * (They define a color cube, then a gradient.)</p>
     */
    static public int[] XTERM_88_COLORS = 
    {
     0x000000, 0xcd0000, 0x00cd00, 0xcdcd00, 0x0000ee, 0xcd00cd, 0x00cdcd,
     0xe5e5e5, 0x7f7f7f, 0xff0000, 0x00ff00, 0xffff00, 0x5c5cff, 0xff00ff,
     0x00ffff, 0xffffff, 0x000000, 0x00008b, 0x0000cd, 0x0000ff, 0x008b00,
     0x008b8b, 0x008bcd, 0x008bff, 0x00cd00, 0x00cd8b, 0x00cdcd, 0x00cdff,
     0x00ff00, 0x00ff8b, 0x00ffcd, 0x00ffff, 0x8b0000, 0x8b008b, 0x8b00cd,
     0x8b00ff, 0x8b8b00, 0x8b8b8b, 0x8b8bcd, 0x8b8bff, 0x8bcd00, 0x8bcd8b,
     0x8bcdcd, 0x8bcdff, 0x8bff00, 0x8bff8b, 0x8bffcd, 0x8bffff, 0xcd0000,
     0xcd008b, 0xcd00cd, 0xcd00ff, 0xcd8b00, 0xcd8b8b, 0xcd8bcd, 0xcd8bff,
     0xcdcd00, 0xcdcd8b, 0xcdcdcd, 0xcdcdff, 0xcdff00, 0xcdff8b, 0xcdffcd,
     0xcdffff, 0xff0000, 0xff008b, 0xff00cd, 0xff00ff, 0xff8b00, 0xff8b8b,
     0xff8bcd, 0xff8bff, 0xffcd00, 0xffcd8b, 0xffcdcd, 0xffcdff, 0xffff00,
     0xffff8b, 0xffffcd, 0xffffff, 0x2e2e2e, 0x5c5c5c, 0x737373, 0x8b8b8b,
     0xa2a2a2, 0xb9b9b9, 0xd0d0d0, 0xe7e7e7,
    };

    /**
     * Standard xterm 256-color definitions.
     * 
     * <p>To use:
     * <pre>
     *       palette.addAll(XTERM_256_COLORS, false);
     * </pre>
     * 
     * <p>The first 16 colors are all the same.
     * However the later colors are computed by a formula.
     * (They define a color cube, then a gradient.)</p>
     */
    static public int[] XTERM_256_COLORS = 
    {
     0x000000, 0xcd0000, 0x00cd00, 0xcdcd00, 0x0000ee, 0xcd00cd, 0x00cdcd,
     0xe5e5e5, 0x7f7f7f, 0xff0000, 0x00ff00, 0xffff00, 0x5c5cff, 0xff00ff,
     0x00ffff, 0xffffff, 0x000000, 0x00005f, 0x000087, 0x0000af, 0x0000d7,
     0x0000ff, 0x005f00, 0x005f5f, 0x005f87, 0x005faf, 0x005fd7, 0x005fff,
     0x008700, 0x00875f, 0x008787, 0x0087af, 0x0087d7, 0x0087ff, 0x00af00,
     0x00af5f, 0x00af87, 0x00afaf, 0x00afd7, 0x00afff, 0x00d700, 0x00d75f,
     0x00d787, 0x00d7af, 0x00d7d7, 0x00d7ff, 0x00ff00, 0x00ff5f, 0x00ff87,
     0x00ffaf, 0x00ffd7, 0x00ffff, 0x5f0000, 0x5f005f, 0x5f0087, 0x5f00af,
     0x5f00d7, 0x5f00ff, 0x5f5f00, 0x5f5f5f, 0x5f5f87, 0x5f5faf, 0x5f5fd7,
     0x5f5fff, 0x5f8700, 0x5f875f, 0x5f8787, 0x5f87af, 0x5f87d7, 0x5f87ff,
     0x5faf00, 0x5faf5f, 0x5faf87, 0x5fafaf, 0x5fafd7, 0x5fafff, 0x5fd700,
     0x5fd75f, 0x5fd787, 0x5fd7af, 0x5fd7d7, 0x5fd7ff, 0x5fff00, 0x5fff5f,
     0x5fff87, 0x5fffaf, 0x5fffd7, 0x5fffff, 0x870000, 0x87005f, 0x870087,
     0x8700af, 0x8700d7, 0x8700ff, 0x875f00, 0x875f5f, 0x875f87, 0x875faf,
     0x875fd7, 0x875fff, 0x878700, 0x87875f, 0x878787, 0x8787af, 0x8787d7,
     0x8787ff, 0x87af00, 0x87af5f, 0x87af87, 0x87afaf, 0x87afd7, 0x87afff,
     0x87d700, 0x87d75f, 0x87d787, 0x87d7af, 0x87d7d7, 0x87d7ff, 0x87ff00,
     0x87ff5f, 0x87ff87, 0x87ffaf, 0x87ffd7, 0x87ffff, 0xaf0000, 0xaf005f,
     0xaf0087, 0xaf00af, 0xaf00d7, 0xaf00ff, 0xaf5f00, 0xaf5f5f, 0xaf5f87,
     0xaf5faf, 0xaf5fd7, 0xaf5fff, 0xaf8700, 0xaf875f, 0xaf8787, 0xaf87af,
     0xaf87d7, 0xaf87ff, 0xafaf00, 0xafaf5f, 0xafaf87, 0xafafaf, 0xafafd7,
     0xafafff, 0xafd700, 0xafd75f, 0xafd787, 0xafd7af, 0xafd7d7, 0xafd7ff,
     0xafff00, 0xafff5f, 0xafff87, 0xafffaf, 0xafffd7, 0xafffff, 0xd70000,
     0xd7005f, 0xd70087, 0xd700af, 0xd700d7, 0xd700ff, 0xd75f00, 0xd75f5f,
     0xd75f87, 0xd75faf, 0xd75fd7, 0xd75fff, 0xd78700, 0xd7875f, 0xd78787,
     0xd787af, 0xd787d7, 0xd787ff, 0xd7af00, 0xd7af5f, 0xd7af87, 0xd7afaf,
     0xd7afd7, 0xd7afff, 0xd7d700, 0xd7d75f, 0xd7d787, 0xd7d7af, 0xd7d7d7,
     0xd7d7ff, 0xd7ff00, 0xd7ff5f, 0xd7ff87, 0xd7ffaf, 0xd7ffd7, 0xd7ffff,
     0xff0000, 0xff005f, 0xff0087, 0xff00af, 0xff00d7, 0xff00ff, 0xff5f00,
     0xff5f5f, 0xff5f87, 0xff5faf, 0xff5fd7, 0xff5fff, 0xff8700, 0xff875f,
     0xff8787, 0xff87af, 0xff87d7, 0xff87ff, 0xffaf00, 0xffaf5f, 0xffaf87,
     0xffafaf, 0xffafd7, 0xffafff, 0xffd700, 0xffd75f, 0xffd787, 0xffd7af,
     0xffd7d7, 0xffd7ff, 0xffff00, 0xffff5f, 0xffff87, 0xffffaf, 0xffffd7,
     0xffffff, 0x080808, 0x121212, 0x1c1c1c, 0x262626, 0x303030, 0x3a3a3a,
     0x444444, 0x4e4e4e, 0x585858, 0x626262, 0x6c6c6c, 0x767676, 0x808080,
     0x8a8a8a, 0x949494, 0x9e9e9e, 0xa8a8a8, 0xb2b2b2, 0xbcbcbc, 0xc6c6c6,
     0xd0d0d0, 0xdadada, 0xe4e4e4, 0xeeeeee,
    };

    /**
     * The standard VGA 16-color palette, with a number of useful names.
     * 
     * <p>While a number of names are supported, the order is the Curses/xterm
     * order.</p>
     * 
     * <p>Name formats include:
     *  <ul>
     *    <li>HTML 3/4 color names: maroon, lime, etc. (including alternates)
     *    <li>old fashioned VGA names - 8 color names with light/dark variations
     *    <li>CURSES-throwback - COLOR_-prefixed; bright with |A_BOLD suffixed  
     *  </ul>
     * 
     * <p>The VGA color names all require "light" or "dark" as otherwise they
     * would conflict with HTML color names. This makes them not quite 
     * traditional, but close to it.<p>
     *  
     * <p>The order of the values is the same as Curses/HTML/Windows (red is
     * at index 1), but is different than CGA/EGA/VGA (where blue is at index 
     * 1). If you want these colors and names with the CGA/EGA/VGA color order,
     * use {@code new ColorPalette(addStandard16Palette(null), 
     *     [0,  4,  2,  6,  1,  5,  3,  7, 
     *      8, 12, 10, 14,  9, 13, 11, 15])}
     * </p>
     *  
     */
    static public String[] STANDARD_16_COLORS = 
    {
     "COLOR_BLACK / black -> #000000",
     "COLOR_RED / dark red / maroon -> #800000",
     "COLOR_GREEN / dark green / green -> #008000",     	
     "COLOR_YELLOW / brown / olive -> #808000",     	
     "COLOR_BLUE / dark blue / navy -> #000080",     	
     "COLOR_MAGENTA / dark magenta / purple -> #800080",     	
     "COLOR_CYAN / dark cyan / teal -> #008080",     	
     "COLOR_WHITE / light grey / light gray / silver -> #C0C0C0",
     "COLOR_BLACK|A_BOLD / dark grey / dark gray / grey / gray -> #808080",
     "COLOR_RED|A_BOLD / light red / red -> #FF0000",
     "COLOR_GREEN|A_BOLD / light green / lime -> #00FF00",
     "COLOR_YELLOW|A_BOLD / yellow -> #FFFF00", 
     "COLOR_BLUE|A_BOLD / light blue / blue -> #0000FF",
     "COLOR_MAGENTA|A_BOLD / light magenta / fuchsia / magenta -> #FF00FF",
     "COLOR_CYAN|A_BOLD / light cyan / aqua / cyan -> #00FFFF",
     "COLOR_WHITE|A_BOLD / white -> #FFFFFF",
    };

    /**
     * The standard CGA color palette.
     * 
     * <p>We support two naming formats:
     * <ul>
     *  <li>traditional VGA names - 8 color names with light/dark variations
     *  <li>CURSES-throwback - COLOR_-prefixed; bright with |A_BOLD suffixed  
     * </ul></p>
     * 
     * <p><em>Important</em> This does not use the same color definitions as
     * {@link #STANDARD_16_COLORS}. Specifically:
     * <ul> 
     *   <li> It uses the same name for totally different color values. (Ex. 
     *     CGA "magenta" is a dark color, while HTML "magenta" is a bright 
     *     color.)</li>
     *   <li> It never uses the same color values. (Ex. While "green"
     *     is a dark green in both palettes, the CGA "green" uses 0x00AA00 and
     *     the HTML "green" uses 0x008000)</li>
     *   <li> It uses a different index ordering. (Ex. index 1 is "dark red"
     *     for HTML/Standard/xterm palettes and is "dark blue" for CGA.)</li>
     * </ul></p>
     * 
     * <p>It case it wasn't clear earlier, while the 
     * {@link #STANDARD_16_COLORS} needs
     * the traditional names to have "light"/"dark" prefixes to not conflict
     * with HTML color names, this function does not set HTML color names so
     * there is no way it can conflict and the "dark" prefix is normally 
     * optional.</p>
     * 
     */
    static public String[] CGA_16_COLORS = 
    {
     "COLOR_BLACK / black -> #000000",
     "COLOR_BLUE / blue / dark blue -> #0000AA",
     "COLOR_GREEN / green / dark green -> #00AA00",
     "COLOR_CYAN / cyan / dark cyan -> #00AAAA",
     "COLOR_RED / red / dark red -> #AA0000",
     "COLOR_MAGENTA / magenta / dark magenta -> #AA00AA",
     "COLOR_YELLOW / brown -> #AA5500",
     "COLOR_WHITE / light grey / light gray -> #AAAAAA",
     "COLOR_BLACK|A_BOLD / grey / gray / dark grey / dark gray -> #555555",
     "COLOR_BLUE|A_BOLD / light blue -> #5555FF",
     "COLOR_GREEN|A_BOLD / light green -> #55FF55",
     "COLOR_CYAN|A_BOLD / light cyan -> #55FFFF",
     "COLOR_RED|A_BOLD / light red -> #FF5555",
     "COLOR_MAGENTA|A_BOLD / light magenta -> #FF55FF",
     "COLOR_YELLOW|A_BOLD / yellow -> #FFFF55",
     "COLOR_WHITE|A_BOLD / white -> #FFFFFF",
    };

    static public String[] LIBTCOD_COLORS = {
        "black -> 0,0,0",
        "white -> 255,255,255",
        // standard
        "desaturatedRed -> 128,64,64", "lightestRed -> 255,191,191",
        "lighterRed -> 255,166,166", "lightRed -> 255,115,115",
        "red -> 255,0,0", "darkRed -> 191,0,0",
        "darkerRed -> 128,0,0", "darkestRed -> 64,0,0",
        "desaturatedFlame -> 128,80,64", "lightestFlame -> 255,207,191",
        "lighterFlame -> 255,188,166", "lightFlame -> 255,149,115",
        "flame -> 255,63,0", "darkFlame -> 191,47,0",
        "darkerFlame -> 128,32,0", "darkestFlame -> 64,16,0",
        "desaturatedOrange -> 128,96,64", "lightestOrange -> 255,223,191",
        "lighterOrange -> 255,210,166","lightOrange -> 255,185,115",
        "orange -> 255,127,0","darkOrange -> 191,95,0",
        "darkerOrange -> 128,64,0","darkestOrange -> 64,32,0",
        "desaturatedAmber -> 128,112,64", "lightestAmber -> 255,239,191",
        "lighterAmber -> 255,233,166","lightAmber -> 255,220,115",
        "amber -> 255,191,0","darkAmber -> 191,143,0",
        "darkerAmber -> 128,96,0","darkestAmber -> 64,48,0",
        "desaturatedYellow -> 128,128,64", "lightestYellow -> 255,255,191",
        "lighterYellow -> 255,255,166","lightYellow -> 255,255,115",
        "yellow -> 255,255,0","darkYellow -> 191,191,0",
        "darkerYellow -> 128,128,0","darkestYellow -> 64,64,0",
        "desaturatedLime -> 112,128,64", "lightestLime -> 239,255,191",
        "lighterLime -> 233,255,166","lightLime -> 220,255,115",
        "lime -> 191,255,0","darkLime -> 143,191,0",
        "darkerLime -> 96,128,0","darkestLime -> 48,64,0",
        "desaturatedChartreuse -> 96,128,64", "lightestChartreuse -> 223,255,191",
        "lighterChartreuse -> 210,255,166","lightChartreuse -> 185,255,115",
        "chartreuse -> 127,255,0","darkChartreuse -> 95,191,0",
        "darkerChartreuse -> 64,128,0","darkestChartreuse -> 32,64,0",
        "desaturatedGreen -> 64,128,64", "lightestGreen -> 191,255,191",
        "lighterGreen -> 166,255,166","lightGreen -> 115,255,115",
        "green -> 0,255,0","darkGreen -> 0,191,0",
        "darkerGreen -> 0,128,0","darkestGreen -> 0,64,0",
        "desaturatedSea -> 64,128,96", "lightestSea -> 191,255,223",
        "lighterSea -> 166,255,210","lightSea -> 115,255,185",
        "sea -> 0,255,127","darkSea -> 0,191,95",
        "darkerSea -> 0,128,64","darkestSea -> 0,64,32",
        "desaturatedTurquoise -> 64,128,112", "lightestTurquoise -> 191,255,239",
        "lighterTurquoise -> 166,255,233","lightTurquoise -> 115,255,220",
        "turquoise -> 0,255,191","darkTurquoise -> 0,191,143",
        "darkerTurquoise -> 0,128,96","darkestTurquoise -> 0,64,48",
        "desaturatedCyan -> 64,128,128", "lightestCyan -> 191,255,255",
        "lighterCyan -> 166,255,255","lightCyan -> 115,255,255",
        "cyan -> 0,255,255","darkCyan -> 0,191,191",
        "darkerCyan -> 0,128,128","darkestCyan -> 0,64,64",
        "desaturatedSky -> 64,112,128", "lightestSky -> 191,239,255",
        "lighterSky -> 166,233,255","lightSky -> 115,220,255",
        "sky -> 0,191,255","darkSky -> 0,143,191",
        "darkerSky -> 0,96,128","darkestSky -> 0,48,64",
        "desaturatedAzure -> 64,96,128", "lightestAzure -> 191,223,255",
        "lighterAzure -> 166,210,255","lightAzure -> 115,185,255",
        "azure -> 0,127,255","darkAzure -> 0,95,191",
        "darkerAzure -> 0,64,128","darkestAzure -> 0,32,64",
        "desaturatedBlue -> 64,64,128", "lightestBlue -> 191,191,255",
        "lighterBlue -> 166,166,255","lightBlue -> 115,115,255",
        "blue -> 0,0,255","darkBlue -> 0,0,191",
        "darkerBlue -> 0,0,128","darkestBlue -> 0,0,64",
        "desaturatedHan -> 80,64,128", "lightestHan -> 207,191,255",
        "lighterHan -> 188,166,255","lightHan -> 149,115,255",
        "han -> 63,0,255","darkHan -> 47,0,191",
        "darkerHan -> 32,0,128","darkestHan -> 16,0,64",
        "desaturatedViolet -> 96,64,128", "lightestViolet -> 223,191,255",
        "lighterViolet -> 210,166,255","lightViolet -> 185,115,255",
        "violet -> 127,0,255","darkViolet -> 95,0,191",
        "darkerViolet -> 64,0,128","darkestViolet -> 32,0,64",
        "desaturatedPurple -> 111,64,128", "lightestPurple -> 239,191,255",
        "lighterPurple -> 233,166,255","lightPurple -> 220,115,255",
        "purple -> 191,0,255","darkPurple -> 143,0,191",
        "darkerPurple -> 95,0,128","darkestPurple -> 48,0,64",
        "desaturatedFuchsia -> 128,64,128", "lightestFuchsia -> 255,191,255",
        "lighterFuchsia -> 255,166,255","lightFuchsia -> 255,115,255",
        "fuchsia -> 255,0,255","darkFuchsia -> 191,0,191",
        "darkerFuchsia -> 128,0,128","darkestFuchsia -> 64,0,64",
        "desaturatedMagenta -> 128,64,111", "lightestMagenta -> 255,191,239",
        "lighterMagenta -> 255,166,233","lightMagenta -> 255,115,220",
        "magenta -> 255,0,191","darkMagenta -> 191,0,143",
        "darkerMagenta -> 128,0,95","darkestMagenta -> 64,0,48",
        "desaturatedPink -> 128,64,96", "lightestPink -> 255,191,223",
        "lighterPink -> 255,166,210","lightPink -> 255,115,185",
        "pink -> 255,0,127","darkPink -> 191,0,95",
        "darkerPink -> 128,0,64","darkestPink -> 64,0,32",
        "desaturatedCrimson -> 128,64,79", "lightestCrimson -> 255,191,207",
        "lighterCrimson -> 255,166,188","lightCrimson -> 255,115,149",
        "crimson -> 255,0,63","darkCrimson -> 191,0,47",
        "darkerCrimson -> 128,0,31","darkestCrimson -> 64,0,16",
        // metallic
        "brass -> 191,151,96", "copper -> 196,136,124",
        "gold -> 229,191,0", "silver -> 203,203,203",
        // miscellaneous
        "celadon -> 172,255,175", "peach -> 255,159,127",
        // grey/gray scale
        "lightestGrey / lightestGray -> 223,223,223",
        "lighterGrey / lighterGray -> 191,191,191",
        "lightGrey / lightGray -> 159,159,159","grey -> 127,127,127",
        "darkGrey / darkGray -> 95,95,95",
        "darkerGrey / darkerGray -> 63,63,63",
        "darkestGrey / darkestGray -> 31,31,31",
        // sepia
        "lightestSepia -> 222,211,195","lighterSepia -> 191,171,143",
        "lightSepia -> 158,134,100","sepia -> 127,101,63",
        "darkSepia -> 94,75,47","darkerSepia -> 63,50,31",
        "darkestSepia -> 31,24,15"
        };
}
