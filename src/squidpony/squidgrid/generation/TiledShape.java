package squidpony.squidgrid.generation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import squidpony.annotation.Beta;

/**
 * Represents a generic shape for dungeon generation.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class TiledShape {

    private boolean[][] template;

    /**
     * Builds the tiled shape directly from a boolean map.
     *
     * @param template true indicates possible locations for filling
     */
    public TiledShape(boolean[][] template) {
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
     * Creates a new tiled shape using the same template.
     *
     * @param other
     */
    public TiledShape(TiledShape other) {
        template = other.template;
    }

    /**
     * Builds a tiled shape based on the pixel colors of the image passed in.
     * White pixels indicate filled spaces (ignoring alpha transparency values)
     *
     * @param image
     */
    public TiledShape(BufferedImage image) {
        initialize(image, false);
    }

    public boolean[][] getTemplate() {
        return template;
    }

    /**
     * Returns true if the provided location is considered filled.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean getFilled(int x, int y) {
        return template[x][y];
    }

    public int getWidth() {
        return template.length;
    }

    public int getHeight() {
        return template.length > 0 ? template[0].length : 0;
    }

    /**
     * Merges the other tiled shape onto this one at the given offset,
     * overwriting all information in the area where the two shapes overlap.
     *
     * Negative offsets are allowed.
     *
     * @param other
     * @param xOffset
     * @param yOffset
     */
    public void merge(TiledShape other, int xOffset, int yOffset) {
        for (int x = Math.max(xOffset, 0); x < getWidth() && x - xOffset < other.getWidth(); x++) {
            for (int y = Math.max(yOffset, 0); y < getHeight() && y - yOffset < other.getHeight(); y++) {
                if (x - xOffset >= 0 && y - yOffset >= 0) {
                    template[x][y] = other.template[x - xOffset][y - yOffset];
                }
            }
        }
    }

    /**
     * Builds and returns a mapping based on the template and sparsity of this
     * shape.
     *
     * @param sparsity the chance for each fillable space to be filled
     * @return
     */
    public TiledShape buildSparseShape(double sparsity) {
        boolean[][] map = new boolean[template.length][template[0].length];
        for (int x = 0; x < template.length; x++) {
            for (int y = 0; y < template[0].length; y++) {
                if (template[x][y] && Math.random() < sparsity) {
                    map[x][y] = true;
                }
            }
        }

        return new TiledShape(map);
    }

    @Override
    public String toString() {
        String out = "\n";
        for (int y = 0; y < template[0].length; y++) {
            for (int x = 0; x < template.length; x++) {
                out += template[x][y] ? "#" : "·";
            }
            out += "\n";
        }
        out += "\n";
        return out;
    }

    /**
     * Builds a tiled shape based on a java.awt.Shape object.
     *
     * @param shape
     * @param borderThickness added in the java.awt standard border manner
     * @param filled true if the shape should be filled solid
     * @param rounded true if the ends and joins should be rounded, false if
     * they should be sharp edges
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
     * Takes a provided image and builds a tiled shape where all white pixels
     * are considered to be solid (boolean true) and all other color pixels to
     * be considered empty (boolean false).
     *
     * Does not take alpha transparency values into account.
     *
     * @param image
     * @param shrink true if the image should be compressed to just the shape
     * @return
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

        template = new boolean[xEnd - xStart][yEnd - yStart];
        for (int x = 0; x < xEnd - xStart; x++) {
            for (int y = 0; y < yEnd - yStart; y++) {
                template[x][y] = image.getRGB(x + xStart, y + yStart) == Color.WHITE.getRGB();
            }
        }
    }

}
