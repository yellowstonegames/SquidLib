package squidpony.squidgrid.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

/**
 * Singleton class for creating text blocks.
 *
 * The default characters guaranteed to fit are ASCII 33 through 125, which are
 * the commonly used symbols, numbers, and letters. Whitespace defaults to being
 * used on all characters.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class TextCellFactory extends CellFactory {

    private static TextCellFactory instance = new TextCellFactory();
    private int verticalOffset = 0;//how far the baseline needs to be moved based on squeezing the cell size vertically
    private Font font;
    private char[] fitting;
    private boolean whiteSpace = true;

    private TextCellFactory() {
        fitting = new char[126 - 33];
        for (char c = 33; c <= 125; c++) {
            fitting[c - 33] = c;
        }
    }

    /**
     * Sets the array of characters that will be checked to ensure they all fit
     * in the text block. One of the provided initialization methods must be
     * called to then make this take effect.
     *
     * @param fit
     * @param whiteSpace true if there must be white space around all characters
     */
    public void setFitCharacters(char[] fit, boolean whiteSpace) {
        fitting = fit;
        this.whiteSpace = whiteSpace;
    }

    /**
     * Sets up the factory to provide images based on the font's size.
     *
     * @param font
     */
    public void initializeByFont(Font font) {
        this.font = font;
        blocks = new TreeMap<String, BufferedImage>();
        verticalOffset = 0;
        sizeCellByFont();
        blocks = new TreeMap<String, BufferedImage>();
    }

    /**
     * Sets up the factory to provide images based on the cell cellWidth and
     * cellHeight passed in. The font size will be used as a starting point, and
     * reduced as needed to fit within the cell.
     *
     * @param cellWidth
     * @param cellHeight
     * @param font
     * @param whiteSpace
     */
    public void initializeBySize(int cellWidth, int cellHeight, Font font) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.font = font;
        blocks = new TreeMap<String, BufferedImage>();
        verticalOffset = 0;
        this.sizeCellByDimension();
        blocks = new TreeMap<String, BufferedImage>();
    }

    /**
     * Gets the singleton instance.
     *
     * @return
     */
    public static TextCellFactory getInstance() {
        return instance;
    }

    /**
     * Makes the cell as small as possible with the basic plane printable ASCII
     * characters still fitting inside.
     *
     * @param whiteSpace true if an extra pixel should be ensured around the
     * largest characters.
     */
    private void sizeCellByFont() {
        //build a sample image to get actual font metrics
        Graphics2D graphics = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR).createGraphics();
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();

        //set cellWidth and cellHeight to largest possible values according to the font metrics
        cellWidth = metrics.getMaxAdvance() * 2;
        cellHeight = (metrics.getMaxAscent() + metrics.getMaxDescent()) * 2;

        //set up working variables
        int testWidthLeft = cellWidth - 1;
        int testWidthRight = 0;
        int testHeightTop = cellHeight - 1;
        int testHeightBottom = 0;
        BufferedImage testImage;

        //loop through basic printable characters and outside edges
        for (char c : fitting) {
            if (!Character.isISOControl(c)) {//make sure it's a printable character
                testImage = getImageFor(c, Color.BLACK, Color.WHITE);

                //measure widest spot
                leftCheckLoop:
                for (int x = 0; x < cellWidth; x++) {//check for left side
                    for (int y = 0; y < cellHeight; y++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testWidthLeft = Math.min(x, testWidthLeft);//set farthest left seen
                            break leftCheckLoop;
                        }
                    }
                }

                rightCheckLoop:
                for (int x = cellWidth - 1; x >= 0; x--) {//check for right side
                    for (int y = 0; y < cellHeight; y++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testWidthRight = Math.max(x, testWidthRight);//set farthest right seen
                            break rightCheckLoop;
                        }
                    }
                }

                //measure tallest spot
                topCheckLoop:
                for (int y = 0; y < cellHeight; y++) {//check for top
                    for (int x = 0; x < cellWidth; x++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testHeightTop = Math.min(y, testHeightTop);//set highest seen
                            break topCheckLoop;
                        }
                    }
                }

                //measure lowest spot
                bottomCheckLoop:
                for (int y = cellHeight - 1; y >= 0; y--) {//check for bottom
                    for (int x = 0; x < cellWidth; x++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testHeightBottom = Math.max(y, testHeightBottom);//set farthest down seen
                            break bottomCheckLoop;
                        }
                    }
                }
            }
        }

        //set cellWidth and cellHeight to widest values found
        verticalOffset = testHeightTop;
        cellWidth = testWidthRight - testWidthLeft + 2;//have to add one since right is the index found at
        cellHeight = testHeightBottom - testHeightTop + 2;//have to add one since bottom is the index found at

        if (whiteSpace) {//add enough to guarantee pixel on all four sides even with odd centering
            cellWidth += 4;
            cellHeight += 4;
            verticalOffset -= 2;
        }
    }

    private void sizeCellByDimension() {
        int fontSize = font.getSize();
        boolean rightSize = false;
        do {
            font = new Font(font.getName(), font.getStyle(), fontSize);
            blocks = new TreeMap<String, BufferedImage>();
            verticalOffset = 0;
            for (char c : fitting) {
                rightSize = true;
                if (!willFit(c, whiteSpace)) {
                    //found one that doesn't work, skip to the next step
                    rightSize = false;
                    break;
                }
            }
            fontSize--;
        } while (!rightSize);
    }

    /**
     * Returns true if the given character will fit inside the current cell
     * dimensions with the current font. ISO Control characters are considered
     * to not fit by definition.
     *
     * @param c
     * @return
     */
    public boolean willFit(char c) {
        return willFit(c, whiteSpace);
    }

    private boolean willFit(char c, boolean whiteSpace) {
        if (Character.isISOControl(c)) {//make sure it's a printable character
            return false;
        }

        //set up working variables
        int testWidth, testHeight;
        if (whiteSpace) {
            testWidth = 1;
            testHeight = 1;
        } else {
            testWidth = 0;
            testHeight = 0;
        }

        //the test image has to be slightly larger to ensure outside is clear
        BufferedImage testImage = new BufferedImage(cellWidth + 2 + 2 * testWidth, cellHeight + 2 + 2 * testHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, testImage.getWidth(), testImage.getHeight());
        g.setColor(Color.BLACK);
        g.setFont(font);

        g.drawString("" + c, (cellWidth + 1 + 2 * testWidth - g.getFontMetrics().charWidth(c)) / 2, g.getFontMetrics().getMaxAscent() - verticalOffset + 1 + testHeight);

        //check for font drawn at the edge
        for (int y = 0; y < testImage.getHeight(); y++) {
            if (testImage.getRGB(0, y) != Color.WHITE.getRGB()
                    || testImage.getRGB(testImage.getWidth() - 1, y) != Color.WHITE.getRGB()) {
                return false;
            }
        }

        for (int x = 0; x < testImage.getWidth(); x++) {
            if (testImage.getRGB(x, 0) != Color.WHITE.getRGB()
                    || testImage.getRGB(x, testImage.getHeight() - 1) != Color.WHITE.getRGB()) {
                return false;
            }
        }

        //all the needed space was clear!
        return true;
    }

    /**
     * Returns the image for the given character.
     *
     * @param c
     * @param foreground
     * @param background
     * @return
     */
    public BufferedImage getImageFor(char c, Color foreground, Color background) {
        String search = getStringRepresentationOf(c, foreground, background);
        BufferedImage block = blocks.get(search);

        if (block == null) {
            block = makeImage(c, foreground, background);
            blocks.put(search, block);
        }
        return block;
    }

    /**
     * Returns the image of the character provided with a transparent
     * background.
     *
     * @param c
     * @param foreground
     * @return
     */
    public BufferedImage getImageFor(char c, Color foreground) {
        String search = getStringRepresentationOf(c, foreground);
        BufferedImage block = blocks.get(search);

        if (block == null) {
            block = makeImage(c, foreground);
            blocks.put(search, block);
        }
        return block;
    }

    /**
     * Returns a string representation of the character and the hex value of the
     * foreground and background argb values.
     *
     * @param c
     * @param foreground
     * @param background
     * @return
     */
    public String getStringRepresentationOf(char c, Color foreground, Color background) {
        return c + " " + foreground.getClass().getSimpleName() + ": " + Integer.toHexString(foreground.getRGB())
                + " " + background.getClass().getSimpleName() + ": " + Integer.toHexString(background.getRGB());
    }

    /**
     * Returns a string representation of the character and the hex value of the
     * foreground.
     *
     * @param c
     * @param foreground
     * @param background
     * @return
     */
    public String getStringRepresentationOf(char c, Color foreground) {
        return c + " " + foreground.getClass().getSimpleName() + ": " + Integer.toHexString(foreground.getRGB());
    }

    private BufferedImage makeImage(char c, Color foreground, Color background) {
        BufferedImage i = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = i.createGraphics();
        g.setColor(background);
        g.fillRect(0, 0, cellWidth, cellHeight);
        g.setColor(foreground);
        g.setFont(font);

        g.drawString("" + c, (cellWidth - g.getFontMetrics().charWidth(c)) / 2, g.getFontMetrics().getMaxAscent() - verticalOffset);
        return i;
    }

    private BufferedImage makeImage(char c, Color foreground) {
        BufferedImage i = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = i.createGraphics();
        g.setColor(foreground);
        g.setFont(font);

        g.drawString("" + c, (cellWidth - g.getFontMetrics().charWidth(c)) / 2, g.getFontMetrics().getMaxAscent() - verticalOffset);
        return i;
    }
}
