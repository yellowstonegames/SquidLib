package squidpony.squidgrid.gui.libgdx;

import com.badlogic.gdx.graphics.Pixmap;
import java.awt.image.BufferedImage;

/**
 * Provides methods for converting images to Pixmaps
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class PixmapFactory {

    /**
     * Builds and returns a Pixmap based on the input image.
     *
     * @param image
     * @return
     */
    public static Pixmap createPixmap(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pix.drawPixel(x, y, getRGBA(image.getRGB(x, y)));
            }
        }

        return pix;
    }

    /**
     * Shifts java.awt.Color ARGB values to gdx RGBA values.
     *
     * @param rgb
     * @return
     */
    public static int getRGBA(int rgb) {
        int a = rgb >> 24;
        int rest = rgb & 0x00ffffff;//mask out the alpha channel
        rest <<= 8;
        rest += a;
        return rest;
    }
}
