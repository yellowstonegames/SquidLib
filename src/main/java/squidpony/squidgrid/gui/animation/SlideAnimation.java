package squidpony.squidgrid.gui.animation;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * This class animates an image sliding directly from one point to another.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SlideAnimation extends AbstractAnimation {

    public SlideAnimation(BufferedImage image, Point start, Point end, long duration) {
        super(image, start, end, duration);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        lastTime = System.currentTimeMillis();
        if (isActive()) {
            float ratio = (float) (lastTime - startTime) / (endTime - startTime);
            ratio = Math.max(ratio, 0.001f);

            float dx = end.x - start.x;
            float dy = end.y - start.y;
            dx *= ratio;
            dy *= ratio;

            label.setLocation(start.x + Math.round(dx), start.y + Math.round(dy));
            label.invalidate();
        }
    }

}
