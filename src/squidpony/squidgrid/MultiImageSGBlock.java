package squidpony.squidgrid;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**Contains the data within a cell.
 *
 * @author Eben
 */
public class MultiImageSGBlock implements SGImageBlock {
    private BufferedImage data[];//stored as an array so that multiple tiles can be in each block
    private int index = 0;//currently displayed image

    public MultiImageSGBlock(BufferedImage image) {
        this(new BufferedImage[]{image});
    }

    public MultiImageSGBlock(BufferedImage image[]) {
        data = image;
    }

    @Override
    public BufferedImage getImage() {
        return data[index];
    }

    @Override
    public BufferedImage getNextImage() {
        if (data.length > 1) {//check if there is more than one image
            index++;
            if (index >= data.length) {
                index = 0;
            }
        }
        return getImage();
    }

    @Override
    public Dimension getSize() {
        if (data.length > 0) {
            return new Dimension(data[0].getWidth(), data[0].getHeight());
        } else {
            return new Dimension(0, 0);
        }
    }

    @Override
    public void setImage(BufferedImage image) {
        data = new BufferedImage[]{image};
    }

    @Override
    public void addImage(BufferedImage image) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setImages(ArrayList<BufferedImage> images) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
