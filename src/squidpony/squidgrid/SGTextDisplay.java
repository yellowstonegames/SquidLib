package squidpony.squidgrid;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author SquidPony
 */
public interface SGTextDisplay extends SGDisplay {
    /**
     * Initializes the component with the supplied values. The cells will be set
     * to the desired width and height and if the size of the font is too large,
     * it will be shrunk until everything fits.
     *
     * @param width The desired width in pixels.
     * @param height The desired height in pixels.
     * @param rows The specific number of characters to display horizontally
     * @param columns The specific number of characters to display vertically
     * @param font The base font
     */
    public void initialize(int width, int height, int rows, int columns, Font font);

    /**
     * Initializes the component with the supplied number of rows and columns.
     * The size of the display will be adjusted to match the requested font size
     * as closely as possible.
     *
     * @param rows Number of characters horizontally
     * @param columns Number of characters vertically
     * @param font The font to use
     */
    public void initialize(int rows, int columns, Font font);

    /**
     * Resizes the cell dimensions to ensure that all characters will fit within
     * the grid spaces.
     *
     * If whiteSpace is true, then every character is guaranteed to have at
     * least one pixel between it and the edge of the cell. If false, some
     * characters might sit against the edge of the cell, but will not be
     * cropped.
     *
     * This method does not force a redisplay of the grid.
     *
     * @param space
     * @param characters
     */
    public void ensureFits(char[] characters, boolean whiteSpace);

    /**
     * Test if the given character will fit in the current cell dimension using
     * the current Font.
     *
     * If whiteSpace is true then will test if the character will have at least
     * one pixel between it and the edge of the cell. If false, will only check
     * for strictly in the cell with no regard for whitespace.
     *
     * @param character
     * @param whiteSpace
     * @return true if it will fit, false otherwise.
     */
    public boolean willFit(char character, boolean whiteSpace);

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array. Will ignore any portion of the array that is outside the
     * bounds of the component itself. The incoming array must be at least as
     * large in either dimension as the number of rows and columns.
     *
     * The default colors of black foreground and white background will be used.
     *
     * @param chars
     */
    public void setText(char chars[][]);

    /**
     * Sets one specific block to the given character. This is far more
     * efficient if only a few spots change.
     * <code>refresh()</code> should be called after all changes are made.
     *
     * @param x The x coordinate to set
     * @param y The y coordinate to set
     * @param c The character to be displayed
     */
    public void setBlock(int x, int y, char c);

    /**
     * Sets one specific block to the given character with the given foreground
     * and background colors.
     *
     * @param x The x coordinate to set
     * @param y The y coordinate to set
     * @param c The character to be displayed
     * @param fore The foreground color
     * @param back The background color
     */
    public void setBlock(int x, int y, char c, Color fore, Color back);
}
