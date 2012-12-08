package squidpony.squidgrid.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Singleton class for managing tile images. All images must be the same
 * dimensions.
 *
 * @author Eben Howard -- http://squidpony.com
 */
public class ImageCellFactory extends CellFactory {

    private static ImageCellFactory instance = new ImageCellFactory();
    private BufferedImage nullImage = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR);

    /**
     * Prevent any other instances being created.
     */
    private ImageCellFactory() {
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return
     */
    public static ImageCellFactory getInstance() {
        return instance;
    }

    /**
     * Sets the size of a single cell.
     *
     * @param dimension
     */
    public void setDimensions(Dimension dim) {
        cellWidth = dim.width;
        cellHeight = dim.height;
    }

    /**
     * Returns the image associated with the provided key. If there is no image
     * associated with the key then returns null.
     *
     * @param key
     * @return
     */
    public BufferedImage getImage(String key) {
        if (blocks.containsKey(key)) {
            return (BufferedImage) blocks.get(key);
        } else {
            return null;
        }
    }

    /**
     * Returns the default null image intended to be used if no other image is
     * available.
     *
     * @return
     */
    public BufferedImage getNullImage() {
        return nullImage;
    }

    /**
     * Sets the image intended to be used if no other image is available.
     *
     * @param nullImage
     */
    public void setNullImage(BufferedImage nullImage) {
        this.nullImage = nullImage;
    }

    /**
     * Adds the provided image if they key does not yet exist. If the key does
     * exist, then the provided image overrides the previous one.
     *
     * @param key
     * @param image
     */
    public void addImage(String key, BufferedImage image) {
        blocks.put(key, image);
    }

    /**
     * Adds blocks of the provided image in order from left to right, wrapping
     * vertically as needed. Will exit once all keys have been assigned or there
     * is no more are of the provided image to break off blocks from.
     *
     * Partial areas not wide or tall enough to make a block on the right or
     * bottom edges are ignored.
     *
     * @param keys
     * @param image
     * @param tileWidth
     * @param tileHeight
     */
    public void addImageBlock(ArrayList<String> keys, BufferedImage image, int tileWidth, int tileHeight) {
        int x = 0;
        int y = 0;

        //validate at least wide and tall enough to break off one block
        if (tileWidth > image.getWidth() || tileHeight > image.getHeight()) {
            return;
        }

        for (String key : keys) {
            if (x + tileWidth > image.getWidth()) {
                x = 0;//wrap to next line
                y += tileHeight;

                if (y + tileHeight > image.getHeight()) {
                    return;//no more room to break off another block
                }
            }

            blocks.put(key, image.getSubimage(x, y, tileWidth, tileHeight));
            x += tileWidth;
        }
    }
}
