package squidpony.squidgrid;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * An extension to SGBlock that supports direct image data.
 *
 * @author SquidPony
 */
public interface SGImageBlock extends SGBlock {
    
    /**
     * Sets the image to be the provided one. This will make the block hold only this
     * single image and override all other image data the block previously held.
     * 
     * @param image 
     */
    public void setImage(BufferedImage image);
    
    /**
     * Adds the image to the end of the list of images to be shown.
     * 
     * @param image 
     */
    public void addImage(BufferedImage image);
    
    /**
     * Sets the list of images to be displayed to the provided list. This overwrites
     * any previous image information in the block.
     * 
     * @param images 
     */
    public void setImages(ArrayList<BufferedImage> images);
}
