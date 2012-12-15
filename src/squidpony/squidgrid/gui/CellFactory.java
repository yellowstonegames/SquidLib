package squidpony.squidgrid.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

/**
 * Basic cell factory for creating images that fit in a single cell.
 *
 * @author Eben Howard - http://squidpony.com
 */
public abstract class CellFactory {

    int cellHeight = 60;
    int cellWidth = 10;
    TreeMap<String, BufferedImage> blocks = new TreeMap<String, BufferedImage>();

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
        blocks = new TreeMap<String, BufferedImage>();
    }
}
