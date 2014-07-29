package squidpony.squidgrid.gui.animation;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * This class animates an object by causing it to wiggle semi-randomly without straying too far from
 * it's original point.
 *
 * This can be set to last only a certain amount of time, or be continuous.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class WiggleAnimation extends AbstractAnimation {

    private long stopTime = 0;//0 indicates that it is continuous
    private Point2D.Double location = new Point2D.Double(0, 0);
    private double impulse;//speed to move
    private Point targetMovement = new Point(0, 0);
    private static Random rng = new Random();

    public WiggleAnimation(BufferedImage image, Point start, double impulse, Point maxDistance, long duration) {
        super(image, start, maxDistance, duration);
        stopTime = System.currentTimeMillis() + duration;
        location.x = start.x;
        location.y = start.y;
        this.impulse = impulse;
    }

    @Override
    public boolean isActive() {
        return stopTime > System.currentTimeMillis();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (targetMovement.x == 0) {
            int targetx = rng.nextInt(end.x * 2 + 1) + start.x - end.x;
            targetMovement.x = (int) (targetx - location.x);
        } else if (targetMovement.x < 0) {
            targetMovement.x = Math.min(0, targetMovement.x + (int) (impulse + location.x % 1));
            location.x = Math.max(location.x - impulse, start.x - end.x);
        } else {//targetMovement.x > 0
            targetMovement.x = Math.max(0, targetMovement.x - (int) (impulse + location.x % 1));
            location.x = Math.min(location.x + impulse, start.x + end.x);
        }

        if (targetMovement.y == 0) {
            int targety = rng.nextInt(end.y * 2 + 1) + start.y - end.y;
            targetMovement.y = (int) (targety - location.y);
        } else if (targetMovement.y < 0) {
            targetMovement.y = Math.min(0, targetMovement.y + (int) (impulse + location.y % 1));
            location.y = Math.max(location.y - impulse, start.y - end.y);
        } else {//targetMovement.y > 0
            targetMovement.y = Math.max(0, targetMovement.y - (int) (impulse + location.y % 1));
            location.y = Math.min(location.y + impulse, start.y + end.y);
        }

        label.setLocation((int) location.x, (int) location.y);
        label.invalidate();
    }
}
