package squidpony.squidgrid.gui.awt;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.util.Direction;

/**
 * Class for creating text blocks.
 *
 * The default characters guaranteed to fit are ASCII 33 through 125, which are the commonly used
 * symbols, numbers, and letters.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class TextCellFactory {

    private int verticalOffset = 0, horizontalOffset = 0;//how far the baseline needs to be moved based on squeezing the cell size
    private Font font;
    private String fitting = "@!#$%^&*()_+1234567890-=~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz;:,'\"{}?/";
    private ArrayList<Integer> largeCharacters;//size on only the largest characters, determined as sizing is performed
    private boolean antialias = false;
    private int leftPadding = 0, rightPadding = 0, topPadding = 0, bottomPadding = 0;
    private int cellHeight = 10;
    private int cellWidth = 10;
    private TreeMap<String, BufferedImage> blocks = new TreeMap<>();

    /**
     * Sets up this factory to ensure ASCII (or UTF-8) characters in the range 33 to 125 all fit and
     * no padding on the cells.
     *
     * After this object is created one of the initialization methods must be called before it can
     * be used.
     */
    public TextCellFactory() {
    }

    /**
     * Creates a new TextCellFactory as a shallow copy of the provided one. If
     *
     * @param other
     */
    public TextCellFactory(TextCellFactory other) {
        antialias = other.antialias;
        blocks = new TreeMap<>(other.blocks);
        bottomPadding = other.bottomPadding;
        cellHeight = other.cellHeight;
        cellWidth = other.cellWidth;
        fitting = other.fitting;
        font = other.font;
        horizontalOffset = other.horizontalOffset;
        leftPadding = other.leftPadding;
        rightPadding = other.rightPadding;
        topPadding = other.topPadding;
        verticalOffset = other.verticalOffset;
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
     * Returns the font used by this factory.
     *
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * Clears out the backing cache. Should be used if a very large number of one-off cells are
     * being made.
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
     * Sets the array of characters that will be checked to ensure they all fit in the text block.
     * One of the provided initialization methods must be called to then make this take effect.
     *
     * @param fit
     */
    public void setFitCharacters(char[] fit) {
        fitting = "";
        for (char c : fit) {
            fitting += c;
        }
        emptyCache();
    }

    /**
     * Sets the characters that will be checked to be the ones in the provided string. One of the
     * provided initialization methods must be called to then make this take effect.
     *
     * @param fit
     */
    public void setFitCharacters(String fit) {
        fitting = fit;
        emptyCache();
    }

    /**
     * Adds to the array of characters that will be checked to ensure they all fit in the text
     * block. One of the provided initialization methods must be called to then make this take
     * effect.
     *
     * @param fit
     */
    public void addFit(char[] fit) {
        for (char c : fit) {
            fitting += c;
        }
        emptyCache();
    }

    /**
     * Adds to the set of characters to be checked for fitting. One of the provided initialization
     * methods must be called to then make this take effect.
     *
     * @param c
     */
    public void addFit(char c) {
        fitting += c;
        emptyCache();
    }

    /**
     * Adds the string to the list of character that will be guaranteed to fit. One of the provided
     * initialization methods must be called to then make this take effect.
     *
     * @param fit
     */
    public void addFit(String fit) {
        fitting += fit;
        emptyCache();
    }

    /**
     * Sets the minimum amount of space between the characters and all four edges.
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
        largeCharacters = new ArrayList<>();
        emptyCache();
        sizeCellByFont();
        emptyCache();
    }

    /**
     * Sets up the factory to provide images based on the cell cellWidth and cellHeight passed in.
     * The font size will be used as a starting point, and reduced as needed to fit within the cell.
     *
     * @param cellWidth
     * @param cellHeight
     * @param font
     */
    public void initializeBySize(int cellWidth, int cellHeight, Font font) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.font = font;
        largeCharacters = new ArrayList<>();
        emptyCache();
        sizeCellByDimension();
        emptyCache();
    }

    /**
     * Makes the cell as small as possible with the basic plane printable ASCII characters still
     * fitting inside.
     *
     * @param whiteSpace true if an extra pixel should be ensured around the largest characters.
     */
    private void sizeCellByFont() {
        cellWidth = 1;
        cellHeight = 1;

        //temporarily remove padding values
        int tempLeftPadding = leftPadding;
        int tempRightPadding = rightPadding;
        int tempTopPadding = topPadding;
        int tempBottomPadding = bottomPadding;
        leftPadding = 0;
        rightPadding = 0;
        topPadding = 0;
        bottomPadding = 0;
        verticalOffset = 0;
        horizontalOffset = cellWidth / 2;

        findSize();

        //restore cell sizes based on padding
        leftPadding = tempLeftPadding;
        rightPadding = tempRightPadding;
        topPadding = tempTopPadding;
        bottomPadding = tempBottomPadding;
        verticalOffset += topPadding;
        horizontalOffset += leftPadding;
        cellWidth += leftPadding + rightPadding;
        cellHeight += topPadding + bottomPadding;
    }

    private void findSize() {
        int left = Integer.MAX_VALUE, right = Integer.MIN_VALUE,
                top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE;
        HashMap<Direction, Integer> larges = new HashMap<>();

        BufferedImage image = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = image.createGraphics();
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (antialias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        FontRenderContext context = g.getFontRenderContext();

        for (int i = 0; i < fitting.length();) {
            int code = fitting.codePointAt(i);
            GlyphVector vect = font.createGlyphVector(context, Character.toChars(code));
            if (vect.getGlyphCode(0) != font.getMissingGlyphCode()
                    && Character.isValidCodePoint(code)
                    && !Character.isISOControl(code)
                    && !Character.isWhitespace(code)) {
                Rectangle rect = vect.getGlyphPixelBounds(0, context, 0, 0);
                if (rect.x < left) {
                    larges.put(Direction.LEFT, code);
                    left = rect.x;
                }
                if (rect.y < top) {
                    larges.put(Direction.UP, code);
                    top = rect.y;
                }
                if (rect.x + rect.width > right) {
                    larges.put(Direction.RIGHT, code);
                    right = rect.x + rect.width;
                }
                if (rect.y + rect.height > bottom) {
                    larges.put(Direction.DOWN, code);
                    bottom = rect.y + rect.height;
                }
            }
            i += Character.charCount(code);
        }
        largeCharacters = new ArrayList<>(larges.values());

        cellWidth = right - left;
        cellWidth *= 2;
        cellHeight = bottom - top;
        cellHeight *= 2;
        horizontalOffset = cellWidth / 2;
        verticalOffset = 0;
        trimCell();
    }

    private void trimCell() {
        BufferedImage image = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_BYTE_GRAY);

        //find best horizontal offset
        int bestHorizontalOffset = Integer.MIN_VALUE;
        for (int c : largeCharacters) {
            int tempHorizontalOffset = horizontalOffset;
            if (visible(c, image)) {//only calculate on printable characters
                while (horizontalOffset > -cellWidth && willFit(c)) {
                    horizontalOffset--;
                }
            }
            bestHorizontalOffset = Math.max(bestHorizontalOffset, Math.min(tempHorizontalOffset, horizontalOffset + 1));//slide back one to the right if we slid left
            horizontalOffset = tempHorizontalOffset;
        }
        horizontalOffset = bestHorizontalOffset;

        //find best vertical offset
        int bestVerticalOffset = Integer.MIN_VALUE;
        for (int code : largeCharacters) {
            int tempVerticalOffset = verticalOffset;
            while (verticalOffset > -cellHeight && willFit(code)) {
                verticalOffset--;
            }
            bestVerticalOffset = Math.max(bestVerticalOffset, Math.min(tempVerticalOffset, verticalOffset + 1));//slide back down one if slid up
            verticalOffset = tempVerticalOffset;
        }
        verticalOffset = bestVerticalOffset;

        //variables to hold value for best fitting cell sizes
        int bestWidth = 1;
        int bestHeight = 1;

        //squeeze back down to rectangle cell
        for (int code : largeCharacters) {

            //squeeze width
            int tempWidth = cellWidth;
            while (cellWidth > 0 && willFit(code)) {//TODO -- determine if single pixel on right edge is a bug here or some other place
                cellWidth--;
            }
            bestWidth = Math.max(bestWidth, cellWidth + 1);//take whatever the largest needed width so far is
            cellWidth = tempWidth;

            //squeeze height
            int tempHeight = cellHeight;
            while (cellHeight > 0 && willFit(code)) {
                cellHeight--;
            }
            bestHeight = Math.max(bestHeight, cellHeight + 1);//take whatever the largest needed height so far is
            cellHeight = tempHeight;
        }

        //set cell sizes based on found best sizes
        cellWidth = bestWidth;
        cellHeight = bestHeight;
    }

    private void sizeCellByDimension() {
        int desiredWidth = cellWidth;
        int desiredHeight = cellHeight;

        //try provided font size first
        initializeByFont(font);

        //try all font sizes and take largest that fits if passed in font is too large
        int fontSize = font.getSize();
        int largeSide = Math.max(cellWidth, cellHeight);
        int largeDesiredSide = Math.max(desiredWidth, desiredHeight);
        fontSize = largeDesiredSide * fontSize / largeSide;//approximately the right ratio
        fontSize *= 1.2;//pad just a bit
        while (cellWidth > desiredWidth || cellHeight > desiredHeight) {
            font = new Font(font.getFontName(), font.getStyle(), fontSize);
            initializeByFont(font);
            fontSize--;
        }

        horizontalOffset += (desiredWidth - cellWidth) / 2;//increase by added width, error on the side of one pixel left of center
        verticalOffset += (desiredHeight - cellHeight) / 2;
        cellWidth = desiredWidth;
        cellHeight = desiredHeight;

        this.emptyCache();
    }

    /**
     * Checks if printing this character will place anything in the square at all.
     *
     * @param code
     * @return
     */
    private boolean visible(int code, BufferedImage i) {
        BufferedImage testImage = makeMonoImage(code, i);
        //work from middle out to maximize chance of finding visilble bit
        int startx = testImage.getWidth() / 2;
        for (int x = 0; x <= startx; x++) {
            for (int y = 0; y < testImage.getHeight(); y++) {
                if (startx + x < testImage.getWidth()) {//make sure no overflow on rounding
                    if (testImage.getRGB(x + startx, y) != Color.WHITE.getRGB() || testImage.getRGB(startx - x, y) != Color.WHITE.getRGB()) {
                        return true;//found a filled in pixel
                    }
                }
            }
        }

        return false;//no pixels found
    }

    private boolean willFit(int c, BufferedImage image) {
        if (Character.isISOControl(c)) {//make sure it's a printable character
            return true;
        }

        for (Direction dir : Direction.CARDINALS) {
            if (!testSlide(c, dir, image)) {
                return false;
            }
        }

        //all the needed space was clear!
        return true;
    }
    /**
     * Returns true if the given character will fit inside the current cell dimensions with the
     * current font. ISO Control characters are considered to fit by definition.
     *
     * @param c
     * @return
     */
    public boolean willFit(char c) {
        BufferedImage image = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_BYTE_GRAY);
        return willFit(c, image);
    }

    /**
     * Returns true if the given character will fit inside the current cell dimensions with the
     * current font.
     * 
     * ISO Control characters, non-printing characters and invalid unicode characters are all
     * considered by definition to fit.
     *
     * @param codepoint
     * @return
     */
    public boolean willFit(int codepoint) {
        if(!Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint)){
            return true;
        }
        
        return willFit(codepoint, new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_BYTE_GRAY));
    }

    /**
     * Slides the character on pixel in the provided direction and test if fully exposed the
     * opposite edge. Returns true if opposite edge was fully exposed.
     *
     * @param code
     * @param dir
     * @return
     */
    private boolean testSlide(int code, Direction dir, BufferedImage image) {
        //set offsets in a direction to test if it cleared the space
        horizontalOffset += dir.deltaX;
        verticalOffset += dir.deltaY;

        BufferedImage testImage = makeMonoImage(code, image);

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
     * Returns the image of the character provided with a transparent background.
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
     * Returns a string representation of the character and the hex value of the foreground and
     * background argb values.
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
     * Returns a string representation of the character and the hex value of the foreground.
     *
     * @param c
     * @param foreground
     * @return
     */
    public String getStringRepresentationOf(char c, Color foreground) {
        return c + " " + foreground.getClass().getSimpleName() + ": " + Integer.toHexString(foreground.getRGB());
    }

    private BufferedImage makeMonoImage(int c, BufferedImage i) {
        Graphics2D g = i.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, cellWidth, cellHeight);
        drawForeground(g, c, Color.BLACK);
        return i;
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
        g.setBackground(SColor.TRANSPARENT);
        drawForeground(g, c, foreground);
        return i;
    }

    private void drawForeground(Graphics2D g, int code, Color foreground) {
        g.setColor(foreground);
        g.setFont(font);

        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (antialias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        int x = horizontalOffset - g.getFontMetrics().charWidth(code) / 2;//start with half of the character's width
        int y = g.getFontMetrics().getMaxAscent() + verticalOffset;
        g.drawString(new String(Character.toChars(code)), x, y);
    }

}
