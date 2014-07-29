package squidpony.squidgrid.gui.animation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * Animates an object moving smoothly in a direction and then bouncing back.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class BumpAnimation extends AbstractAnimation {

    /**
     * Creates a bump animation that will travel one cell in the given direction and return to the
     * starting position in the given time, in milliseconds.
     *
     * @param image
     * @param start
     * @param cellSize
     * @param direction
     * @param duration
     */
    public BumpAnimation(BufferedImage image, Point start, Dimension cellSize, Point direction, long duration) {
        this(image, start, new Point(start.x + direction.x * cellSize.width, start.y + direction.y * cellSize.height), duration);
    }

    public BumpAnimation(BufferedImage image, Point start, Point end, long duration) {
        super(image, start, end, duration);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        lastTime = System.currentTimeMillis();
        if (isActive()) {
            float ratio = (lastTime - startTime) / (endTime - startTime);
            ratio = ratio > 0.5f ? 1.0f - ratio : Math.max(ratio, 0.001f);

            float dx = end.x - start.x;
            float dy = end.y - start.y;
            dx *= ratio;
            dy *= ratio;

            label.setLocation(start.x + Math.round(dx), start.y + Math.round(dy));

            label.invalidate();
        }
    }
}
