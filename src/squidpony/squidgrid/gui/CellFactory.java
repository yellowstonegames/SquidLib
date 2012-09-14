package squidpony.squidgrid.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

/**
 * Basic cell factory for creating images that fit in a single cell.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class CellFactory {

    TreeMap<String, BufferedImage> blocks = new TreeMap<String, BufferedImage>();
    int cellHeight = 60;
    int cellWidth = 10;

    public Dimension getCellDimension() {
        return new Dimension(cellWidth, cellHeight);
    }
}
