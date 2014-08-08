package squidpony.squidgrid.gui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.util.DirectionIntercardinal;

/**
 * Class for creating text blocks.
 *
 * The size of the Font used during construction is considered an upper limit. If needed, the font size will be reduced
 * until all characters can fit within the specified cell size. If all character fit in the specified cell size at the
 * passed in Font size, no change to the Font size will occur.
 *
 * The default characters guaranteed to fit are ASCII 33 through 125, which are the commonly used symbols, numbers, and
 * letters.
 *
 * In order to easily support Unicode, code points are used.
 *
 * All images have transparent backgrounds.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class TextCellFactory {

    /**
     * The commonly used symbols in roguelike games.
     */
    public static final String DEFAULT_FITTING = "@!#$%^&*()_+1234567890-=~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz;:,'\"{}?/\\";

    private int verticalOffset = 0, horizontalOffset = 0;//how far the baseline needs to be moved based on squeezing the cell size
    private Font font;
    private String fitting = DEFAULT_FITTING;
    private ArrayList<Integer> largeCharacters;//size on only the largest characters, determined as sizing is performed
    private boolean antialias = false;
    private int leftPadding = 0, rightPadding = 0, topPadding = 0, bottomPadding = 0;
    private int cellWidth = 1, cellHeight = 1;
    private final int width, height;
    private ImageCellMap map;

    /**
     * Builds the factory with the font fitting in the given size cell. Sizes down the font if needed to fit, but does
     * not size up.
     *
     * The font will be rendered using antialias hinting. The default set of characters common to roguelikes will be
     * used as the ones which will be guaranteed to fit.
     *
     * @param font the Font that the cells will be based on
     * @param width the width of each cell
     * @param height the height of each cell
     */
    public TextCellFactory(Font font, int width, int height) {
        this(font, width, height, true);
    }

    /**
     * Builds the factory with the font fitting in the given size cell. Sizes down the font if needed to fit, but does
     * not size up.
     *
     * The default set of characters common to roguelikes will be used as the ones which will be guaranteed to fit.
     *
     * @param font the Font that the cells will be based on
     * @param width the width of each cell
     * @param height the height of each cell
     * @param antialias true for using antialias hints during rendering
     */
    public TextCellFactory(Font font, int width, int height, boolean antialias) {
        this(font, width, height, antialias, 0);
    }

    /**
     * * Builds the factory with the font fitting in the given size cell. Sizes down the font if needed to fit, but
     * does not size up.
     *
     * @param font the Font that the cells will be based on
     * @param width the width of each cell
     * @param height the height of each cell
     * @param antialias true for using antialias hints during rendering
     * @param padding the minimum number of empty pixels on all sides of the cell
     */
    public TextCellFactory(Font font, int width, int height, boolean antialias, int padding) {
        this(font, width, height, antialias, padding, DEFAULT_FITTING);
    }

    /**
     * * Builds the factory with the font fitting in the given size cell. Sizes down the font if needed to fit, but
     * does not size up.
     *
     * @param font the Font that the cells will be based on
     * @param width the width of each cell
     * @param height the height of each cell
     * @param antialias true for using antialias hints during rendering
     * @param padding the minimum number of empty pixels on all sides of the cell
     * @param fitting a String containing all characters that will be guaranteed to fit
     */
    public TextCellFactory(Font font, int width, int height, boolean antialias, int padding, String fitting) {
        this(font, width, height, antialias, padding, padding, padding, padding, fitting);

    }

    /**
     * * Builds the factory with the font fitting in the given size cell. Sizes down the font if needed to fit, but
     * does not size up.
     *
     * @param font the Font that the cells will be based on
     * @param width the width of each cell
     * @param height the height of each cell
     * @param antialias true for using antialias hints during rendering
     * @param topPadding the minimum number of empty pixels at the top of the cell
     * @param bottomPadding the minimum number of empty pixels at the bottom of the cell
     * @param leftPadding the minimum number of empty pixels at the left of the cell
     * @param rightPadding the minimum number of empty pixels at the right of the cell
     */
    public TextCellFactory(Font font, int width, int height, boolean antialias, int topPadding, int bottomPadding, int leftPadding, int rightPadding) {
        this(font, width, height, antialias, topPadding, bottomPadding, leftPadding, rightPadding, DEFAULT_FITTING);
    }

    /**
     * * Builds the factory with the font fitting in the given size cell. Sizes down the font if needed to fit, but
     * does not size up.
     *
     * @param font the Font that the cells will be based on
     * @param width the width of each cell
     * @param height the height of each cell
     * @param antialias true for using antialias hints during rendering
     * @param topPadding the minimum number of empty pixels at the top of the cell
     * @param bottomPadding the minimum number of empty pixels at the bottom of the cell
     * @param leftPadding the minimum number of empty pixels at the left of the cell
     * @param rightPadding the minimum number of empty pixels at the right of the cell
     * @param fitting a String containing all characters that will be guaranteed to fit
     */
    public TextCellFactory(Font font, int width, int height, boolean antialias, int topPadding, int bottomPadding, int leftPadding, int rightPadding, String fitting) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.antialias = antialias;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
        this.fitting = fitting;
        map = new ImageCellMap(width, height);
        initializeBySize(width, height, font);
    }

    /**
     * Returns the font used by this factory.
     *
     * @return
     */
    public Font font() {
        return font;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private void initializeByFont(Font font) {
        this.font = font;
        largeCharacters = new ArrayList<>();
        map.clear();
        sizeCellByFont();
        map.clear();
    }

    private void initializeBySize(int cellWidth, int cellHeight, Font font) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.font = font;
        largeCharacters = new ArrayList<>();
        map.clear();
        sizeCellByDimension();
        map.clear();
    }

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
        HashMap<DirectionIntercardinal, Integer> larges = new HashMap<>();

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
                    larges.put(DirectionIntercardinal.LEFT, code);
                    left = rect.x;
                }
                if (rect.y < top) {
                    larges.put(DirectionIntercardinal.UP, code);
                    top = rect.y;
                }
                if (rect.x + rect.width > right) {
                    larges.put(DirectionIntercardinal.RIGHT, code);
                    right = rect.x + rect.width;
                }
                if (rect.y + rect.height > bottom) {
                    larges.put(DirectionIntercardinal.DOWN, code);
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
        if (cellWidth > 0 && cellHeight > 0) {
            trimCell();
        }
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
        if (largeSide > 0) {
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
        }
        map.clear();
    }

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

    private boolean willFit(int code, BufferedImage image) {
        if (Character.isISOControl(code)) {//make sure it's a printable character
            return true;
        }

        for (DirectionIntercardinal dir : DirectionIntercardinal.CARDINALS) {
            if (!testSlide(code, dir, image)) {
                return false;
            }
        }

        //all the needed space was clear!
        return true;
    }

    /**
     * Returns true if the given character will fit inside the current cell dimensions with the current font.
     *
     * ISO Control characters, non-printing characters and invalid unicode characters are all considered by definition
     * to fit.
     *
     * @param codepoint
     * @return
     */
    public boolean willFit(int codepoint) {
        if (!Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint)) {
            return true;
        }

        return willFit(codepoint, new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_BYTE_GRAY));
    }

    /**
     * Slides the character on pixel in the provided direction and test if fully exposed the opposite edge. Returns true
     * if opposite edge was fully exposed.
     *
     * @param code
     * @param dir
     * @return
     */
    private boolean testSlide(int code, DirectionIntercardinal dir, BufferedImage image) {
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
     * Returns the image of the character represented by the passed in code point.
     *
     * @param code
     * @param color
     * @return
     */
    public BufferedImage get(int code, Color color) {
        String search = new String(Character.toChars(code)) + " " + color.getClass().getSimpleName() + ": " + Integer.toHexString(color.getRGB());
        BufferedImage block = map.get(search);

        if (block == null) {
            block = makeImage(code, color);
            map.put(search, block);
        }
        return block;
    }

    /**
     * Returns a solid block of the provided color.
     *
     * @param color
     * @return
     */
    public BufferedImage getSolid(SColor color) {
        String search = "Solid " + color.getClass().getSimpleName() + ": " + Integer.toHexString(color.getRGB());
        BufferedImage block = map.get(search);

        if (block == null) {
            block = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = block.createGraphics();
            g.setColor(color);
            g.fillRect(0, 0, block.getWidth(), block.getHeight());
            map.put(search, block);
        }
        return block;
    }

    private BufferedImage makeMonoImage(int code, BufferedImage i) {
        Graphics2D g = i.createGraphics();
        g.setColor(SColor.WHITE);
        g.fillRect(0, 0, i.getWidth(), i.getHeight());
        drawForeground(g, code, Color.BLACK);
        return i;
    }

    private BufferedImage makeImage(int code, Color color) {
        BufferedImage i = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = i.createGraphics();
        drawForeground(g, code, color);
        return i;
    }

    private void drawForeground(Graphics2D g, int code, Color color) {
        g.setColor(color);
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
