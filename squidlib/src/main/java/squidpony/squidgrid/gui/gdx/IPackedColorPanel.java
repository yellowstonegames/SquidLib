package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.panel.IColoredString;

/**
 * Created by Tommy Ettinger on 8/5/2017.
 */
public interface IPackedColorPanel {
    /**
     * Places a full cell of color at the given x,y position; this may be used as a background or foreground, depending
     * on the implementation. The color is given as a packed float, the kind produced by {@link Color#toFloatBits()}.
     * If the implementation performs color filtering on Color objects, it generally won't on packed float colors.
     * @param x x position of the cell
     * @param y y position of the cell
     * @param color color for the full cell as a packed float, as made by {@link Color#toFloatBits()}
     */
    void put(int x, int y, float color);

    /**
     * Using the existing color at the position x,y, this performs color blending from that existing color to the given
     * color (as a float), using the mixBy parameter to determine how much of the color parameter to use (1f will set
     * the color in this to the parameter, while 0f for mixBy will ignore the color parameter entirely).
     * @param x the x component of the position in this panel to draw the starting color from
     * @param y the y component of the position in this panel to draw the starting color from
     * @param color the new color to mix with the starting color; a packed float, as made by {@link Color#toFloatBits()}
     * @param mixBy the amount by which the new color will affect the old one, between 0 (no effect) and 1 (overwrite)
     */
    void blend(int x, int y, float color, float mixBy);

    /**
     * Places a char in the given color at the given x,y position; if the implementation has a separate background from
     * the foreground characters, this will not affect it. The color is given as a packed float, the kind produced by
     * {@link Color#toFloatBits()}. If the implementation performs color filtering on Color objects, it generally won't
     * on packed float colors.
     * @param x x position of the char
     * @param y y position of the char
     * @param c the char to put at the given cell
     * @param encodedColor the color for the char as a packed float, as made by {@link Color#toFloatBits()}
     */
    void put(int x, int y, char c, float encodedColor);

    /**
     * Puts the character {@code c} at {@code (x, y)}.
     *
     * @param x
     * @param y
     * @param c
     */
    void put(int x, int y, char c);

    /**
     * Puts {@code color} at {@code (x, y)} (in the cell's entirety, i.e. in the
     * background).
     *
     * @param x
     * @param y
     * @param color
     */
    void put(int x, int y, Color color);

    /**
     * Puts the given string horizontally with the first character at the given
     * offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at
     * negative offsets or offsets greater than the grid size) will not be shown
     * but will not cause any malfunctions.
     *
     * @param xOffset
     *            the x coordinate of the first character
     * @param yOffset
     *            the y coordinate of the first character
     * @param string
     *            the characters to be displayed
     * @param foreground
     *            the color to draw the characters
     */
    void put(int xOffset, int yOffset, String string, Color foreground);

    /**
     * Puts the given string horizontally with the first character at the given
     * offset, using the colors that {@code cs} provides.
     *
     * Does not word wrap. Characters that are not renderable (due to being at
     * negative offsets or offsets greater than the grid size) will not be shown
     * but will not cause any malfunctions.
     *
     * @param xOffset
     *            the x coordinate of the first character
     * @param yOffset
     *            the y coordinate of the first character
     * @param cs
     *            The string to display, with its colors.
     */
    void put(int xOffset, int yOffset, IColoredString<? extends Color> cs);

    /**
     * Puts the character {@code c} at {@code (x, y)} with some {@code color}.
     *
     * @param x
     * @param y
     * @param c
     * @param color
     */
    void put(int x, int y, char c, Color color);

    /**
     * @param foregrounds
     *            Can be {@code null}, indicating that only colors must be put.
     * @param colors
     */
    void put(/* @Nullable */ char[][] foregrounds, Color[][] colors);

    /**
     * Removes the contents of this cell, leaving a transparent space.
     *
     * @param x
     * @param y
     */
    void clear(int x, int y);

    /**
     * @return The number of cells that this panel spans, horizontally.
     */
    int gridWidth();

    /**
     * @return The number of cells that this panel spans, vertically.
     */
    int gridHeight();

    /**
     * @return The width of a cell, in number of pixels.
     */
    int cellWidth();

    /**
     * @return The height of a cell, in number of pixels.
     */
    int cellHeight();

    /**
     * @return Returns true if there are animations running when this method is
     *         called.
     */
    boolean hasActiveAnimations();

    /**
     * Sets the default foreground color.
     *
     * @param color
     */
    void setDefaultForeground(Color color);

    /**
     * @return The default foreground color (if none was set with
     *         {@link #setDefaultForeground(Color)}), or the last color set
     *         with {@link #setDefaultForeground(Color)}. Cannot be
     *         {@code null}.
     */
    Color getDefaultForegroundColor();

}
