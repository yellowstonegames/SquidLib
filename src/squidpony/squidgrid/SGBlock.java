package squidpony.squidgrid;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * This interface guarantees access to certain common elements for data blocks
 * associated with a monospaced display. 
 * 
 * Subclasses of this interface should specify how information is placed
 * into the block.
 *
 * @author Eben
 */
public interface SGBlock {
    /**
     * Gets the next image to be shown. If this block contains only one image it
     * will return that one image.
     * 
     * @return 
     */
    public BufferedImage getNextImage();

    /**
     * Gets the primary image to be shown. If this block has multiple images, it
     * is up to the implementation to determine which image is considered primary.
     * 
     * @return 
     */
    public BufferedImage getImage();

    /**
     * @return the size of the Block in pixels.
     */
    public Dimension getSize();
}
