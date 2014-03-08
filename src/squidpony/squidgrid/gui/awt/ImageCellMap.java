package squidpony.squidgrid.gui.awt;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class for managing tile images. All images must be the same dimensions.
 *
 * @author Eben Howard -- http://squidpony.com - howard@squidpony.com
 */
public class ImageCellMap implements Map {

    private BufferedImage nullImage;
    int cellWidth, cellHeight;
    TreeMap<String, BufferedImage> blocks = new TreeMap<>();

    /**
     * A cached image set.
     * @param size
     */
    public ImageCellMap(Dimension size) {
        cellWidth = size.width;
        cellHeight = size.height;
        nullImage = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_4BYTE_ABGR);
    }

    /**
     * Sets the size of a single cell.
     *
     * @param dim
     */
    public void setDimensions(Dimension dim) {
        cellWidth = dim.width;
        cellHeight = dim.height;
        clear();
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
        if (!blocks.containsValue(image)) {
            blocks.put(key, image);
        }
    }

    /**
     * Returns the image associated with the key.
     *
     * This is preferred to get() since it returns a BufferedImage directly.
     *
     * @param key
     * @return
     */
    public BufferedImage getImage(String key) {
        return blocks.get(key);
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

            addImage(key, image.getSubimage(x, y, tileWidth, tileHeight));
            x += tileWidth;
        }
    }

    @Override
    public int size() {
        return blocks.size();
    }

    @Override
    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    @Override
    public void clear() {
        blocks.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return blocks.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return blocks.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        if (key instanceof String && blocks.containsKey((String) key)) {
            return (BufferedImage) blocks.get(key);
        } else {
            return null;
        }
    }

    @Override
    public Object put(Object key, Object value) {
        if (key instanceof String && value instanceof BufferedImage) {
            BufferedImage ret = getImage((String) key);
            addImage((String) key, (BufferedImage) value);
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public Object remove(Object key) {
        if (key instanceof String) {
            return blocks.remove((String) key);
        }
        return null;
    }

    @Override
    public void putAll(Map m) {
        for (Object k : m.keySet()) {
            Object v = m.get(k);
            if (k instanceof String && v instanceof BufferedImage) {
                addImage((String) k, (BufferedImage) v);
            }
        }
    }

    @Override
    public Set keySet() {
        return blocks.keySet();
    }

    @Override
    public Collection values() {
        return blocks.values();
    }

    @Override
    public Set entrySet() {
        return blocks.entrySet();
    }
}
