package squidpony.squidgrid.gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Class for managing tile images. All images must be the same dimensions.
 *
 * @author Eben Howard -- http://squidpony.com - howard@squidpony.com
 */
public class ImageCellMap {

    private final BufferedImage nullImage;
    private final int width, height;
    private final TreeMap<String, BufferedImage> blocks = new TreeMap<>();

    /**
     * Sets width and height to 1. Useful if width, height, and null image is never used.
     */
    public ImageCellMap() {
        width = 1;
        height = 1;
        nullImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    /**
     * A cached image set.
     *
     * @param width
     * @param height
     */
    public ImageCellMap(int width, int height) {
        this.width = width;
        this.height = height;
        nullImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public ImageCellMap(BufferedImage nullImage) {
        this.nullImage = nullImage;
        width = nullImage.getWidth();
        height = nullImage.getHeight();
    }

    /**
     * Returns the default null image intended to be used if no other image is available.
     *
     * @return
     */
    public BufferedImage getNullImage() {
        return nullImage;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * Adds the given image to the set with the given key.
     *
     * @param key
     * @param value
     * @return
     */
    public BufferedImage put(String key, BufferedImage value) {
        return blocks.put(key, value);
    }

    /**
     * Adds blocks of the provided image in order from left to right, wrapping vertically as needed. Will exit once all
     * keys have been assigned or there is no more are of the provided image to break off blocks from.
     *
     * Partial areas not wide or tall enough to make a block on the right or bottom edges are ignored.
     *
     * @param keys
     * @param image
     * @param tileWidth
     * @param tileHeight
     */
    public void putImageBlock(ArrayList<String> keys, BufferedImage image, int tileWidth, int tileHeight) {
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

            put(key, image.getSubimage(x, y, tileWidth, tileHeight));
            x += tileWidth;
        }
    }

    /**
     * Remove all image blocks from the set.
     */
    public void clear() {
        blocks.clear();
    }

    public BufferedImage get(String key) {
        return blocks.get(key);
    }

}
