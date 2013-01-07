package squidpony.squidgrid.gui.awt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.TreeMap;
import squidpony.squidgrid.util.Direction;

/**
 * Class for creating text blocks.
 *
 * The default characters guaranteed to fit are ASCII 33 through 125, which are
 * the commonly used symbols, numbers, and letters.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class TextCellFactory implements Cloneable {

    private int verticalOffset = 0, horizontalOffset = 0;//how far the baseline needs to be moved based on squeezing the cell size
    private Font font;
    private char[] fitting;
    private boolean antialias = false;
    private int leftPadding = 0, rightPadding = 0, topPadding = 0, bottomPadding = 0;
    int cellHeight = 10;
    int cellWidth = 10;
    TreeMap<String, BufferedImage> blocks = new TreeMap<String, BufferedImage>();

    /**
     * Sets up this factory to ensure ASCII (or UTF-8) characters in the range
     * 33 to 125 all fit and no padding on the cells.
     *
     * After this object is created one of the initialization methods must be
     * called before it can be used.
     */
    public TextCellFactory() {
        fitting = new char[126 - 33];
        for (char c = 33; c <= 125; c++) {
            fitting[c - 33] = c;
        }
    }

    /**
     * Returns the dimension of a single grid cell.
     *
     * @return
     */
    public Dimension getCellDimension() {
        return new Dimension(cellWidth, cellHeight);
    }

    /**
     * Clears out the backing cache. Should be used if a very large number of
     * one-off cells are being made.
     */
    public void emptyCache() {
        blocks.clear();
    }

    /**
     * Sets whether or not characters should be drawn with antialiasing.
     *
     * @param set
     */
    public void setAntialias(boolean set) {
        if (set == antialias) {
            return;//nothing to do
        }

        emptyCache();//since rendering is changed everything has to be cleared out
        antialias = set;
    }

    /**
     * Sets the array of characters that will be checked to ensure they all fit
     * in the text block. One of the provided initialization methods must be
     * called to then make this take effect.
     *
     * @param fit
     */
    public void setFitCharacters(char[] fit) {
        fitting = fit;
        emptyCache();
    }

    /**
     * Sets the minimum amount of space between the characters and all four
     * edges.
     *
     * @param pad
     */
    public void setPadding(int pad) {
        leftPadding = pad;
        rightPadding = pad;
        topPadding = pad;
        bottomPadding = pad;
        emptyCache();
    }

    /**
     * Sets the minimum amount of space between the characters and the edges.
     *
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void setPadding(int left, int right, int top, int bottom) {
        leftPadding = left;
        rightPadding = right;
        topPadding = top;
        bottomPadding = bottom;
        emptyCache();
    }

    /**
     * Sets up the factory to provide images based on the font's size.
     *
     * @param font
     */
    public void initializeByFont(Font font) {
        this.font = font;
        emptyCache();
        verticalOffset = 0;
        horizontalOffset = 0;
        sizeCellByFont();
        emptyCache();
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
        emptyCache();
        verticalOffset = 0;
        horizontalOffset = 0;
        sizeCellByDimension();
        emptyCache();
    }

    /**
     * Makes the cell as small as possible with the basic plane printable ASCII
     * characters still fitting inside.
     *
     * @param whiteSpace true if an extra pixel should be ensured around the
     * largest characters.
     */
    private void sizeCellByFont() {//TODO -- refactor to size starting small and increasing, using willfit to get exact size needed
        cellWidth = font.getSize();
        cellHeight = font.getSize();

        //temporarily remove padding values
        int tempLeftPadding = leftPadding;
        int tempRightPadding = rightPadding;
        int tempTopPadding = topPadding;
        int tempBottomPadding = bottomPadding;
        leftPadding = 0;
        rightPadding = 0;
        topPadding = 0;
        bottomPadding = 0;
        horizontalOffset = 0;
        verticalOffset = 0;

        //size up with square cells
        for (char c : fitting) {
            while (!willFit(c)) {
                cellWidth++;
                cellHeight++;
            }
        }

        //find best horizontal offset
        int bestHorizontalOffset = -cellWidth;//worst case, offset by an entire cell
        for (char c : fitting) {
            int tempHorizontalOffset = horizontalOffset;
            while (horizontalOffset > -cellWidth && willFit(c)) {
                horizontalOffset--;
            }
            bestHorizontalOffset = Math.max(bestHorizontalOffset, horizontalOffset + 1);
            horizontalOffset = tempHorizontalOffset;
        }
        horizontalOffset = bestHorizontalOffset;

        //find best vertical offset
        int bestVerticalOffset = -cellHeight;
        for (char c : fitting) {
            int tempVerticalOffset = verticalOffset;
            while (verticalOffset > -cellHeight && willFit(c)) {
                verticalOffset--;
            }
            bestVerticalOffset = Math.max(bestVerticalOffset, verticalOffset + 1);
            verticalOffset = tempVerticalOffset;
        }
        verticalOffset = bestVerticalOffset;

        //variables to hold value for best fitting cell sizes
        int bestWidth = 1;
        int bestHeight = 1;

        //squeeze back down to rectangle cell
        for (char c : fitting) {

            //squeeze width
            int tempWidth = cellWidth;
            int tempHorizontalOffset = horizontalOffset;
            while (cellWidth > 0 && willFit(c)) {
                cellWidth--;
                if (cellWidth % 2 == 1) {
                    horizontalOffset++;
                }
            }
            bestWidth = Math.max(bestWidth, cellWidth + 1);//take whatever the largest needed width so far is
            cellWidth = tempWidth;
            horizontalOffset = tempHorizontalOffset;

            //squeeze height
            int tempHeight = cellHeight;
            while (cellHeight > 0 && willFit(c)) {
                cellHeight--;
            }
            bestHeight = Math.max(bestHeight, cellHeight + 1);//take whatever the largest needed height so far is
            cellHeight = tempHeight;
        }

        //set cell sizes based on found best sizes
        horizontalOffset += Math.ceil((double) (cellWidth - bestWidth + leftPadding) / 2.0);//adjust based on horizontal squeeze
        cellWidth = bestWidth;
        cellHeight = bestHeight;

        //restore cell sizes based on padding
        leftPadding = tempLeftPadding;
        rightPadding = tempRightPadding;
        topPadding = tempTopPadding;
        bottomPadding = tempBottomPadding;
        verticalOffset += topPadding;
        cellWidth += leftPadding + rightPadding;
        cellHeight += topPadding + bottomPadding;
    }

    private void sizeCellByDimension() {
        int fontSize = font.getSize();
        boolean rightSize = false;
        int trueWidth = cellWidth;
        int trueHeight = cellHeight;
        cellWidth = cellWidth - leftPadding - rightPadding;
        cellHeight = cellHeight - topPadding - bottomPadding;
        do {
            font = new Font(font.getName(), font.getStyle(), fontSize);
            blocks = new TreeMap<String, BufferedImage>();
            verticalOffset = 0;
            for (char c : fitting) {
                rightSize = true;
                if (!willFit(c)) {
                    //found one that doesn't work, skip to the next step
                    rightSize = false;
                    break;
                }
            }
            fontSize--;
        } while (!rightSize);

        verticalOffset = topPadding;
        horizontalOffset = leftPadding;
        cellWidth = trueWidth;
        cellHeight = trueHeight;
    }

    /**
     * Returns true if the given character will fit inside the current cell
     * dimensions with the current font. ISO Control characters are considered
     * to fit by definition.
     *
     * @param c
     * @return
     */
    public boolean willFit(char c) {//TODO -- make it "wiggle" one space and see if that made a clear edge
        if (Character.isISOControl(c)) {//make sure it's a printable character
            return true;
        }

        for (Direction dir : Direction.cardinals) {
            //set offsets in a direction to test if it cleared the space
            horizontalOffset += dir.deltaX;
            verticalOffset += dir.deltaY;

            BufferedImage testImage = makeImage(c, Color.BLACK, Color.WHITE);

            //reset offsets to actual values
            horizontalOffset -= dir.deltaX;
            verticalOffset -= dir.deltaY;

            int startx = 0, starty = 0, endx = 0, endy = 0;//end points should be included in check
            switch (dir) {//set values to check edge opposite of movement
                case RIGHT:
                    endy = cellHeight - 1;
                    break;
                case LEFT:
                    startx = cellWidth - 1;
                    endx = startx;
                    endy = cellHeight - 1;
                    break;
                case UP:
                    endx = cellWidth - 1;
                    starty = cellHeight - 1;
                    endy = starty;
                    break;
                case DOWN:
                    endx = cellWidth - 1;
            }

            //test for edge hit
            for (int x = startx; x <= endx; x++) {
                for (int y = starty; y <= endy; y++) {
                    if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                        return false;//found an edge that would normally be printed off the cell
                    }
                }
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
        BufferedImage block = (BufferedImage) blocks.get(search);

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
        BufferedImage block = (BufferedImage) blocks.get(search);

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
        drawForeground(g, c, foreground);
        return i;
    }

    private BufferedImage makeImage(char c, Color foreground) {
        BufferedImage i = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = i.createGraphics();
        drawForeground(g, c, foreground);
        return i;
    }

    private void drawForeground(Graphics2D g, char c, Color foreground) {
        g.setColor(foreground);
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (antialias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        int x = cellWidth / 2 - g.getFontMetrics().charWidth(c) / 2;//start with half of the cell size and half the character's width
        x += horizontalOffset;
        int y = g.getFontMetrics().getMaxAscent() + verticalOffset;
        g.drawString(String.valueOf(c), x, y);
    }

    @Override
    public TextCellFactory clone() {
        TextCellFactory ret = new TextCellFactory();
        ret.antialias = antialias;
        ret.blocks = new TreeMap<String, BufferedImage>(blocks);
        ret.bottomPadding = bottomPadding;
        ret.cellHeight = cellHeight;
        ret.cellWidth = cellWidth;
        ret.fitting = fitting;
        ret.font = font;
        ret.horizontalOffset = horizontalOffset;
        ret.leftPadding = leftPadding;
        ret.rightPadding = rightPadding;
        ret.topPadding = topPadding;
        ret.verticalOffset = verticalOffset;
        return ret;
    }
}
