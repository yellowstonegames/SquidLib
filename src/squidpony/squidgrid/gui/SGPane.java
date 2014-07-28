package squidpony.squidgrid.gui;

import java.awt.Color;
import java.awt.Font;
import squidpony.annotation.Beta;

/**
 * A basic interface for working with grids.
 *
 * This interface's Beta status is due to the libGDX version potentially
 * becoming different enough that this interface is removed in favor of direct
 * implementation of desired back end panels.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface SGPane {

    /**
     * Returns the height of a single cell.
     *
     * @return
     */
    public int getCellHeight();

    /**
     * Returns the width of a single cell.
     *
     * @return
     */
    public int getCellWidth();

    /**
     * Returns the height of the grid. This is the number of rows in the grid.
     *
     * @return
     */
    public int getGridHeight();

    /**
     * Returns the width of the grid. This is the number of columns in the grid.
     *
     * @return
     */
    public int getGridWidth();


    /**
     * Initializes the component with the supplied values. The cells will be set
     * to the desired width and height and if the size of the font is too large,
     * it will be shrunk until everything fits.
     *
     * @param cellWidth in pixels
     * @param cellHeight in pixels
     * @param gridWidth in cells
     * @param gridHeight in cells
     * @param font
     */
    public void initialize(int cellWidth, int cellHeight, int gridWidth, int gridHeight, Font font);

    /**
     * Initializes the component with the supplied number of rows and columns.
     * The size of the display will be adjusted to match the requested font size
     * as closely as possible.
     *
     * @param gridWidth in cells
     * @param gridHeight in cells
     * @param font
     */
    public void initialize(int gridWidth, int gridHeight, Font font);

    /**
     * Clears the cell at the location given with the default background color.
     *
     * @param x
     * @param y
     */
    public void clear(int x, int y);

    /**
     * Clears the cell at the location given with the provided color.
     *
     * @param x
     * @param y
     * @param color
     */
    public void clear(int x, int y, Color color);

    /**
     * Sets the background to the provided color. Does not change the
     * foreground.
     *
     * @param x
     * @param y
     * @param color
     */
    public void setCellBackground(int x, int y, Color color);

    /**
     * Sets one specific block to the given character.
     *
     * This block is not drawn immediately, refresh() must be called to update
     * display.
     *
     * @param x The x coordinate to set
     * @param y The y coordinate to set
     * @param c The character to be displayed
     */
    public void put(int x, int y, char c);

    /**
     * Sets one specific block to the given character with the given foreground
     * and background colors.
     *
     * This block is not drawn immediately, refresh() must be called to update
     * display.
     *
     * @param x The x coordinate to set
     * @param y The y coordinate to set
     * @param c The character to be displayed
     * @param fore The foreground color
     * @param back The background color
     */
    public void put(int x, int y, char c, Color fore, Color back);

    /**
     * Sets the block at the given coordinates to contain the passed in
     * character drawn with the given foreground color. The default background
     * color will be used.
     *
     * @param x
     * @param y
     * @param c
     * @param fore
     */
    public void put(int x, int y, char c, Color fore);

    /**
     * Prints out a string starting at the given offset position. Any portion of
     * the string that would cross the edge is ignored. The default foreground
     * color is used and the background is not effected.
     *
     * @param string
     * @param x
     * @param y
     */
    public void placeHorizontalString(int x, int y, String string);

    /**
     * Prints out a string vertically starting at the given offset position and
     * traveling down.
     *
     * @param xOffset
     * @param yOffset
     * @param string
     * @param foreground
     * @param background
     */
    public void placeHorizontalString(int xOffset, int yOffset, String string, Color foreground, Color background);

    /**
     * Places the image associated with the provided key at the given
     * coordinates.
     *
     * If the key does not have an associated image, the factory's default null
     * image is used.
     *
     * @param x
     * @param y
     * @param key
     */
    public void placeImage(int x, int y, String key);

    /**
     * Places the image associated with the provided key at the given
     * coordinates.
     *
     * If the key does not have an associated image, the factory's default null
     * image is used.
     *
     * The background will be set to the provided Color, but will only show up
     * if the keyed image has transparency.
     *
     * @param x
     * @param y
     * @param key
     * @param background
     */
    public void placeImage(int x, int y, String key, Color background);

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array, starting at the given offset position. The default
     * foreground color is used and the background is not effected.
     *
     * Any content that would be off the screen to the right or down is ignored.
     *
     * @param chars
     * @param xOffset
     * @param yOffset
     */
    public void put(int xOffset, int yOffset, char[][] chars);

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array, starting at the given offset position.
     *
     * @param xOffset
     * @param yOffset
     * @param chars
     * @param foreground
     * @param background
     */
    public void put(int xOffset, int yOffset, char[][] chars, Color foreground, Color background);

    /**
     * Prints out a string starting at the given offset position. Any portion of
     * the string that would cross the edge is ignored.
     *
     * @param xOffset
     * @param yOffset
     * @param string
     * @param foreground
     * @param background
     */
    public void placeVerticalString(int xOffset, int yOffset, String string, Color foreground, Color background);

    /**
     * Prints out a string vertically starting at the given offset position and
     * traveling down. The default foreground color is used and the background
     * is not effected.
     *
     * @param xOffset
     * @param yOffset
     * @param string
     */
    public void placeVerticalString(int xOffset, int yOffset, String string);

    /**
     * Signals that this component should update its display image.
     */
    public void refresh();

    /**
     * Sets the background color which will be used on all text and transparent
     * tiles when not other color is specified.
     *
     * @param defaultBackground
     */
    public void setDefaultBackground(Color defaultBackground);

    /**
     * Sets the background color which will be used on all text and transparent
     * tiles when not other color is specified.
     *
     * @param defaultForeground
     */
    public void setDefaultForeground(Color defaultForeground);

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array. Will ignore any portion of the array that is outside the
     * bounds of the component itself.
     *
     * The default colors of the foreground and background will be used.
     *
     * @param chars
     */
    public void setText(char[][] chars);

    /**
     * Test if the given character will fit in the current cell dimension using
     * the current Font.
     *
     * @param character
     * @return true if it will fit, false otherwise.
     */
    public boolean willFit(char character);
}
