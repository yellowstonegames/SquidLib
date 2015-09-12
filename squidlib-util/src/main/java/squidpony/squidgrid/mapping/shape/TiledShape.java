package squidpony.squidgrid.mapping.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import squidpony.annotation.Beta;

/**
 * Represents a generic shape for tile map generation.
 *
 * The toString() method simply prints out the first character in the string of each cell.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class TiledShape {

    private String[][] template;

    /**
     * Builds the tiled shape directly from the provided template.
     *
     * @param template true indicates possible locations for filling
     */
    public TiledShape(String[][] template) {
        this.template = template;
    }

    /**
     * Builds a tiled shape with the given dimensions and all empty spaces.
     *
     * @param width
     * @param height
     */
    public TiledShape(int width, int height) {
        this(new Rectangle(width, height), 0, true, false);
    }

    /**
     * Builds a tiled shape based on the structure of a provided shape.
     *
     * @param shape
     * @param borderThickness
     * @param filled true if the shape should be filled solid
     * @param rounded true if edges should be rounded off
     */
    public TiledShape(Shape shape, double borderThickness, boolean filled, boolean rounded) {
        initialize(shape, borderThickness, filled, rounded);
    }

    /**
     * Creates a new tiled shape and deep copies the template.
     *
     * @param other
     */
    public TiledShape(TiledShape other) {
        template = new String[other.width()][other.height()];
        for (int x = 0; x < other.width(); x++) {
            for (int y = 0; y < other.height(); y++) {
                template[x][y] = other.getStringAt(x, y);
            }
        }
    }

    /**
     * Builds a tiled shape based on the pixel colors of the image passed in. Values are stored as the ARGB values in
     * hex code.
     *
     * @param image
     */
    public TiledShape(BufferedImage image) {
        initialize(image, false);
    }

    public String[][] getTemplate() {
        return template;
    }

    /**
     * @param x the x coordinate to check
     * @param y the y coordinate to check
     * @return the String at the provided location
     */
    public String getStringAt(int x, int y) {
        return template[x][y];
    }

    /**
     * Sets the location provided to contain the given string, replacing any content that may have previously existed.
     *
     * @param x the x coordinate to contain the string
     * @param y the y coordinate to contain the string
     * @param string the string to place
     */
    public void setStringAt(int x, int y, String string) {
        template[x][y] = string;
    }

    public int width() {
        return template.length;
    }

    public int height() {
        return template.length > 0 ? template[0].length : 0;
    }

    /**
     * Overwrites this TiledShape at the given offset with the information in the provided TiledShape in the area where
     * the two shapes overlap.
     *
     * Negative offsets are allowed.
     *
     * @param other the other TiledShape
     * @param xOffset the distance the other shape is shifted on the x axis
     * @param yOffset the distance the other shape is shifted on the y axis
     */
    public void overwrite(TiledShape other, int xOffset, int yOffset) {
        for (int x = Math.max(xOffset, 0); x < width() && x - xOffset < other.width(); x++) {
            for (int y = Math.max(yOffset, 0); y < height() && y - yOffset < other.height(); y++) {
                if (x - xOffset >= 0 && y - yOffset >= 0) {
                    template[x][y] = other.template[x - xOffset][y - yOffset];
                }
            }
        }
    }

    /**
     * Deteriorates the given TiledShape on a per-cell random chance. If a cell is selected to be deteriorated, it's
     * value is set to the provided string. Changes are made directly to the provided shape.
     *
     * @param deteriorationChance the chance for each fillable space to be filled
     * @param deteriorationString the string to place in the deteriorated cell
     */
    public void deteriorate(double deteriorationChance, String deteriorationString) {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (Math.random() < deteriorationChance) {
                    setStringAt(x, y, deteriorationString);
                }
            }
        }
    }

    /**
     * Replaces all cells whose contents match the "find" string with the "replace" string.
     *
     * @param find the string searched for
     * @param replace the replacement string
     */
    public void replaceAll(String find, String replace) {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (template[x][y].equals(find)) {
                    template[x][y] = replace;
                }
            }
        }
    }

    /**
     * Rotates this shape clockwise 90 degrees.
     */
    public void rotateClockwise() {
        String[][] map = new String[height()][width()];
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                map[y][x] = template[x][y];
            }
        }
        template = map;
    }

    @Override
    public String toString() {
        String out = "\n";
        for (int y = 0; y < template[0].length; y++) {
            for (int x = 0; x < template.length; x++) {
                out += template[x][y] == null ? ' ' : template[x][y].charAt(0);
            }
            out += "\n";
        }
        out += "\n";
        return out;
    }

    /**
     * Builds a tiled shape based on a java.awt.Shape object.
     *
     * @param shape java.awt.Shape object to build on
     * @param borderThickness added in the java.awt standard border manner
     * @param filled true if the shape should be filled solid
     * @param rounded true if the ends and joins should be rounded, false if they should be sharp edges
     */
    private void initialize(Shape shape, double borderThickness, boolean filled, boolean rounded) {
        BufferedImage image = new BufferedImage((int) Math.ceil(shape.getBounds2D().getWidth() + borderThickness),
                (int) Math.ceil(shape.getBounds2D().getHeight() + borderThickness), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);//since it's going on a grid and only pure white will count, antialias would mess things up
        graphics.setStroke(new BasicStroke((float) borderThickness, rounded ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT, rounded ? BasicStroke.JOIN_ROUND : BasicStroke.JOIN_MITER));
        graphics.translate(borderThickness / 2.0, borderThickness / 2.0);
        if (filled) {
            graphics.fill(shape);
        }
        graphics.draw(shape);//need to draw no matter what to get border size
        initialize(image, true);
    }

    /**
     * Takes a provided image and builds a tiled shape encoding all colors into their ARGB integer as a string.
     *
     * @param image the image to process
     * @param shrink true if the image should be compressed to just the shape
     */
    private void initialize(BufferedImage image, boolean shrink) {
        int xStart = 0;
        int xEnd = image.getWidth();
        int yStart = 0;
        int yEnd = image.getHeight();

        if (shrink) {
            xStartCheck:
            for (xStart = 0; xStart < image.getWidth(); xStart++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (image.getRGB(xStart, y) == Color.WHITE.getRGB()) {
                        break xStartCheck;
                    }
                }
            }

            xEndCheck:
            for (xEnd = image.getWidth(); xEnd > 0; xEnd--) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if (image.getRGB(xEnd - 1, y) == Color.WHITE.getRGB()) {
                        break xEndCheck;
                    }
                }
            }

            yStartCheck:
            for (yStart = 0; yStart < image.getHeight(); yStart++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (image.getRGB(x, yStart) == Color.WHITE.getRGB()) {
                        break yStartCheck;
                    }
                }
            }

            yEndCheck:
            for (yEnd = image.getHeight(); yEnd > 0; yEnd--) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (image.getRGB(x, yEnd - 1) == Color.WHITE.getRGB()) {
                        break yEndCheck;
                    }
                }
            }
        }

        template = new String[xEnd - xStart][yEnd - yStart];
        for (int x = 0; x < xEnd - xStart; x++) {
            for (int y = 0; y < yEnd - yStart; y++) {
                template[x][y] = "" + image.getRGB(x + xStart, y + yStart);
            }
        }
    }

}
