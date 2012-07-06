package squidpony.squidgrid.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import squidpony.squidcolor.SColor;

/**
 * This class is a JPanel that will display a text string as a monospaced font
 * regardless of the font's actual spacing.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SGTextPanel extends JPanel {
    private BufferedImage[][] contents;
    private int gridHeight, gridWidth;
    private Dimension cellDimension, panelDimension;
    private BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR);
    private TextBlockFactory factory = TextBlockFactory.getInstance();
    private Color defaultForeground = SColor.BLACK;
    private Color defaultBackground = SColor.WHITE;
    private boolean updating = false;

    /**
     * Builds a new panel with the desired traits. The size of the font will be
     * used unless it's too large to fit, in which case it will be stepped down
     * until the characters fit.
     *
     * @param cellWidth cell width in pixels.
     * @param cellHeight cell height in pixels.
     * @param panelWidth number of cells vertically..
     * @param panelHeight number of cells horizontally.
     * @param font
     */
    public SGTextPanel(int cellWidth, int cellHeight, int panelWidth, int panelHeight, Font font) {
        initialize(cellWidth, cellHeight, panelWidth, panelHeight, font);
    }

    /**
     * Builds a panel with the given Font determining the size of the cell
     * dimensions.
     *
     * @param panelWidth
     * @param panelHeight
     * @param font
     */
    public SGTextPanel(int panelWidth, int panelHeight, Font font) {
        initialize(panelWidth, panelHeight, font);
    }

    /**
     * Empty constructor. One of the initialization methods must be called
     * before this panel is used.
     */
    public SGTextPanel() {
    }

    private void redrawImage() {
        updating = true;
        Graphics2D g2 = image.createGraphics();
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                g2.drawImage(contents[x][y], x * cellDimension.width, y * cellDimension.height, null);
            }
        }
        updating = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array. Will ignore any portion of the array that is outside the
     * bounds of the component itself.
     *
     * The default colors of the foreground and background will be used.
     *
     * @param chars
     */
    public void setText(char[][] chars) {
        placeText(0, 0, chars);
    }

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array, starting at the given offset position.
     *
     * Any content that would be off the screen to the right or down is ignored.
     *
     * @param chars
     * @param xOffset
     * @param yOffset
     */
    public void placeText(int xOffset, int yOffset, char[][] chars) {
        placeText(xOffset, yOffset, chars, defaultForeground, defaultBackground);
    }

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
    public void placeText(int xOffset, int yOffset, char[][] chars, Color foreground, Color background) {
        for (int x = xOffset; x < chars.length; x++) {
            for (int y = yOffset; y < chars[0].length; y++) {
                if (xOffset >= 0 && yOffset >= 0 && xOffset < gridWidth && yOffset < gridHeight) {//check for valid input
                    placeCharacter(x, y, chars[x][y]);
                }
            }
        }
    }

    /**
     * Prints out a string starting at the given offset position. Any portion of
     * the string that would cross the edge is ignored.
     *
     * @param string
     * @param xOffset
     * @param yOffset
     */
    public void placeHorizontalString(int xOffset, int yOffset, String string) {
        placeHorizontalString(xOffset, yOffset, string, defaultForeground, defaultBackground);
    }

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
    public void placeHorizontalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        placeText(xOffset, yOffset, temp, foreground, background);
    }

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
    public void placeVerticalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        placeText(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground, background);
    }

    /**
     * Prints out a string vertically starting at the given offset position and
     * traveling down.
     *
     * @param xOffset
     * @param yOffset
     * @param string
     */
    public void placeVerticalString(int xOffset, int yOffset, String string) {
        placeVerticalString(xOffset, yOffset, string, defaultForeground, defaultBackground);
    }

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
    public void placeCharacter(int x, int y, char c) {
        placeCharacter(x, y, c, defaultForeground, defaultBackground);
    }

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
    public void placeCharacter(int x, int y, char c, Color fore, Color back) {
        if (!updating) {
            contents[x][y] = factory.getImageFor(c, fore, back);
        }
    }

    /**
     * Signals that this component should update its display image.
     */
    public void refresh() {
        redrawImage();
        repaint();
    }

    /**
     * Initializes the component with the supplied values. The cells will be set
     * to the desired width and height and if the size of the font is too large,
     * it will be shrunk until everything fits.
     *
     * @param cellWidth in pixels
     * @param cellHeight in pixels
     * @param panelWidth in cells
     * @param panelHeight in cells
     * @param font
     */
    public void initialize(int cellWidth, int cellHeight, int panelWidth, int panelHeight, Font font) {
        factory.initializeBySize(cellWidth, cellHeight, font);
        doInitialization(panelWidth, panelHeight);
    }

    /**
     * Initializes the component with the supplied number of rows and columns.
     * The size of the display will be adjusted to match the requested font size
     * as closely as possible.
     *
     * @param panelWidth in cells
     * @param panelHeight in cells
     * @param font
     */
    public void initialize(int panelWidth, int panelHeight, Font font) {
        factory.initializeByFont(font);
        doInitialization(panelWidth, panelHeight);
    }

    private void doInitialization(int gridWidth, int gridHeight) {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        contents = new BufferedImage[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                contents[x][y] = factory.getImageFor(' ', SColor.BLACK, SColor.BABY_BLUE);
            }
        }

        cellDimension = factory.getCellDimension();

        int w = gridWidth * cellDimension.width;
        int h = gridHeight * cellDimension.height;
        panelDimension = new Dimension(w, h);

        setSize(panelDimension);
        setMinimumSize(panelDimension);
        setPreferredSize(panelDimension);

        image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        refresh();
    }

    /**
     * Sets the character set that will be guaranteed to fit on the next
     * initialization of the grid.
     *
     * If whiteSpace is true, then every character is guaranteed to have at
     * least one pixel between it and the edge of the cell. If false, some
     * characters might sit against the edge of the cell, but will not be
     * cropped.
     *
     * @param characters
     * @param whiteSpace
     */
    public void ensureFits(char[] characters, boolean whiteSpace) {
        factory.setFitCharacters(characters, whiteSpace);
    }

    /**
     * Test if the given character will fit in the current cell dimension using
     * the current Font.
     *
     * @param character
     * @return true if it will fit, false otherwise.
     */
    public boolean willFit(char character) {
        return factory.willFit(character);
    }

    public void setDefaultBackground(Color defaultBackground) {
        this.defaultBackground = defaultBackground;
    }

    public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

    public Dimension getCellDimension() {
        return cellDimension;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }
}
