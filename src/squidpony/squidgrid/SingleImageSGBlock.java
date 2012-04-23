package squidpony.squidgrid;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author SquidPony
 */
public class SingleImageSGBlock implements SGImageBlock {
    BufferedImage image;

    @Override
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void addImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void setImages(ArrayList<BufferedImage> images) {
        if (!images.isEmpty()) {
            image = images.get(0);
        }
    }

    @Override
    public BufferedImage getNextImage() {
        return image;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public Dimension getSize() {
        if (image != null) {
            return new Dimension(image.getWidth(), image.getHeight());
        } else {
            return new Dimension(0, 0);
        }
    }
}
