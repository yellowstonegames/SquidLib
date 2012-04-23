package squidpony.squidgrid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

/**
 * Singleton class for creating text blocks. Stores images in a TreeMap to
 * prevent duplicate object creation where not needed. For applications with
 * many repeated text blocks, this is an ideal way to hold and create them.
 *
 * @author SquidPony
 */
public class TextBlockFactory {
    private static TextBlockFactory instance = new TextBlockFactory();
    TreeMap<String, SGImageBlock> blocks = new TreeMap<String, SGImageBlock>();
    int width = 10, height = 60;
    int verticalOffset = 0;//how far the baseline needs to be moved based on squeezing the cell size vertically
    Font font;

    private TextBlockFactory() {
    }

    public void initializeByFont(Font font, boolean whiteSpace) {
        this.font = font;
        blocks = new TreeMap<String, SGImageBlock>();
        verticalOffset = 0;
        sizeCellByFont(whiteSpace);
        blocks = new TreeMap<String, SGImageBlock>();
    }

    /**
     * Sets up the factory to provide images based on the cell width and height
     * passed in. The font size will be used as a starting point, and reduced as
     * needed to fit within the cell.
     *
     * @param width
     * @param height
     * @param font
     * @param whiteSpace
     */
    public void initializeBySize(int width, int height, Font font, boolean whiteSpace) {
        this.width = width;
        this.height = height;
        this.font = font;
        blocks = new TreeMap<String, SGImageBlock>();
        verticalOffset = 0;
        this.sizeCellByDimension(whiteSpace);
        blocks = new TreeMap<String, SGImageBlock>();
    }

    /**
     * Gets the singleton instance.
     *
     * @return
     */
    public static TextBlockFactory getInstance() {
        return instance;
    }

    public Dimension getCellDimension() {
        return new Dimension(width, height);
    }

    /**
     * Allows directly setting the desired cell dimensions.
     *
     * @param dimension
     */
    public void setCellDimension(Dimension dimension) {
        if (width != dimension.width || height != dimension.height) {//make sure resizing has to happen
            width = dimension.width;
            height = dimension.height;
            blocks = new TreeMap<String, SGImageBlock>();//invalidate old treemap
        }
    }

    /**
     * Makes the cell as small as possible with the basic plane printable ASCII
     * characters still fitting inside.
     *
     * @param whiteSpace true if an extra pixel should be ensured around the
     * largest characters.
     */
    private void sizeCellByFont(boolean whiteSpace) {
        //build a sample image to get actual font metrics
        Graphics2D graphics = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).createGraphics();
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();

        //set width and height to largest possible values according to the font metrics
        width = metrics.getMaxAdvance() * 2;
        height = (metrics.getMaxAscent() + metrics.getMaxDescent()) * 2;

        //set up working variables
        int testWidthLeft = width - 1;
        int testWidthRight = 0;
        int testHeightTop = height - 1;
        int testHeightBottom = 0;
        BufferedImage testImage;

        //loop through basic printable characters and outside edges
        for (char c = 33; c <= 125; c++) {
            if (!Character.isISOControl(c)) {//make sure it's a printable character
                testImage = getImageFor(c, Color.BLACK, Color.WHITE);

                //measure widest spot
                leftCheckLoop:
                for (int x = 0; x < width; x++) {//check for left side
                    for (int y = 0; y < height; y++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testWidthLeft = Math.min(x, testWidthLeft);//set farthest left seen
                            break leftCheckLoop;
                        }
                    }
                }

                rightCheckLoop:
                for (int x = width - 1; x >= 0; x--) {//check for right side
                    for (int y = 0; y < height; y++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testWidthRight = Math.max(x, testWidthRight);//set farthest right seen
                            break rightCheckLoop;
                        }
                    }
                }

                //measure tallest spot
                topCheckLoop:
                for (int y = 0; y < height; y++) {//check for top
                    for (int x = 0; x < width; x++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testHeightTop = Math.min(y, testHeightTop);//set highest seen
                            break topCheckLoop;
                        }
                    }
                }

                //measure lowest spot
                bottomCheckLoop:
                for (int y = height - 1; y >= 0; y--) {//check for bottom
                    for (int x = 0; x < width; x++) {
                        if (testImage.getRGB(x, y) != Color.WHITE.getRGB()) {
                            testHeightBottom = Math.max(y, testHeightBottom);//set farthest down seen
                            break bottomCheckLoop;
                        }
                    }
                }
            }
        }

        //set width and height to widest values found
        verticalOffset = testHeightTop;
        width = testWidthRight - testWidthLeft + 2;//have to add one since right is the index found at
        height = testHeightBottom - testHeightTop + 2;//have to add one since bottom is the index found at

        if (whiteSpace) {//add enough to guarantee pixel on all four sides even with odd centering
            width += 4;
            height += 4;
            verticalOffset -= 2;
        }
    }

    private void sizeCellByDimension(boolean whiteSpace) {
        int fontSize = font.getSize();
        boolean rightSize = false;
        do {
            System.out.println("Size: " + fontSize);
            font = new Font(font.getName(), font.getStyle(), fontSize);
            blocks = new TreeMap<String, SGImageBlock>();
            verticalOffset = 0;
            for (char c = 33; c <= 125; c++) {
                rightSize = true;
                if (!willFit(c, whiteSpace)) {//found one that doesn't work, skip to the next step
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
     * @param whiteSpace if true then there must be room for the character c and
     * at least one empty pixel on all sides of the character
     * @return
     */
    public boolean willFit(char c, boolean whiteSpace) {
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
        BufferedImage testImage = new BufferedImage(width + 2 + 2 * testWidth, height + 2 + 2 * testHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = testImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, testImage.getWidth(), testImage.getHeight());
        g.setColor(Color.BLACK);
        g.setFont(font);

        g.drawString("" + c, (width + 1 + 2 * testWidth - g.getFontMetrics().charWidth(c)) / 2, g.getFontMetrics().getMaxAscent() - verticalOffset + 1 + testHeight);

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
     * Returns the image for the given input.
     *
     * @param c
     * @param foreground
     * @param background
     * @return
     */
    public BufferedImage getImageFor(char c, Color foreground, Color background) {
        String search = getStringRepresentationOf(c, foreground, background);
        SGImageBlock block = blocks.get(search);

        if (block == null) {
            block = new SingleImageSGBlock();
            block.setImage(makeImage(c, foreground, background));
            blocks.put(search, block);
        }
        return block.getImage();
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
     * Creates an image based on the input.
     */
    private BufferedImage makeImage(char c, Color foreground, Color background) {
        BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = i.createGraphics();
        g.setColor(background);
        g.fillRect(0, 0, width, height);
        g.setColor(foreground);
        g.setFont(font);

        g.drawString("" + c, (width - g.getFontMetrics().charWidth(c)) / 2, g.getFontMetrics().getMaxAscent() - verticalOffset);
        return i;
    }
}
