package squidpony.squidgrid.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 * Displays both text and pre-loaded images in a grid pattern.
 *
 * In order to ensure both the images and text are the same size, initialize
 * this panel by cell dimension.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SGTextAndImagePanel extends SGTextPanel {

    protected ImageCellFactory cellFactory = ImageCellFactory.getInstance();

    /**
     * Empty constructor. An initialization method must be used before this
     * panel is ready to be used.
     */
    public SGTextAndImagePanel() {
        super();
    }

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
    public SGTextAndImagePanel(int cellWidth, int cellHeight, int panelWidth, int panelHeight, Font font) {
        initialize(cellWidth, cellHeight, panelWidth, panelHeight, font);
    }

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
    public void placeImage(int x, int y, String key) {
        BufferedImage image = cellFactory.getImage(key);
        if (image == null) {
            image = cellFactory.getNullImage();
        }
        foregroundContents[x][y] = image;
        imageChanged[x][y] = true;
    }

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
    public void placeImage(int x, int y, String key, Color background) {
        BufferedImage image = cellFactory.getImage(key);
        if (image == null) {
            image = cellFactory.getNullImage();
        }
        backgroundContents[x][y] = textFactory.getImageFor(' ', background, background);
        foregroundContents[x][y] = image;
        imageChanged[x][y] = true;
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
    @Override
    public void initialize(int cellWidth, int cellHeight, int panelWidth, int panelHeight, Font font) {
        textFactory.initializeBySize(cellWidth, cellHeight, font);
        doInitialization(panelWidth, panelHeight);
        cellFactory.setDimensions(cellDimension);
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
    @Override
    public void initialize(int panelWidth, int panelHeight, Font font) {
        textFactory.initializeByFont(font);
        doInitialization(panelWidth, panelHeight);
        cellFactory.setDimensions(cellDimension);
    }
}
