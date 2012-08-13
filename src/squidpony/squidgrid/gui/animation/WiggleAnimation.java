package squidpony.squidgrid.gui.animation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

/**
 * This class animates an object by causing it to wiggle semi-randomly without straying
 * too far from it's original point.
 * 
 * This can be set to last only a certain amount of time, or be continuous.
 *
 * @author Eben Howard http://squidpony.com
 */
public class WiggleAnimation implements Animation {
    private long stopTime = 0;//0 indicates that it is continuous
    private Point origin;//starting point
    private Point2D.Double location = new Point2D.Double(0, 0);
    private double impulse;//speed to move
    private Point maxDistance;
    private Point targetMovement = new Point(0, 0);
    private JComponent component;
    private JLabel label;
    private static Random rng = new Random();

    public WiggleAnimation(BufferedImage image, Point origin, double impulse, Point maxDistance, long duration) {
        stopTime = System.currentTimeMillis() + duration;
        this.origin = origin;
        location.x = origin.x;
        location.y = origin.y;
        this.impulse = impulse;
        this.maxDistance = maxDistance;

        //set up JLabel to animate
        label = new JLabel(new ImageIcon(image));
        label.setBorder(null);
        label.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        label.setSize(label.getPreferredSize());
        label.setLocation(origin);
        label.setVisible(true);
    }

    @Override
    public boolean isActive() {
        return stopTime > System.currentTimeMillis();
    }

    @Override
    public int getDelay() {
        return 0;//no delay for this type of animation
    }

    @Override
    public BufferedImage getImage() {
        return (BufferedImage) ((ImageIcon) label.getIcon()).getImage();
    }

    @Override
    public void setComponent(JComponent component) {
        this.component = component;
        if (component instanceof JLayeredPane) {
            component.add(label, JLayeredPane.DRAG_LAYER);
        } else {
            component.add(label);
        }
    }

    @Override
    public void remove() {
        component.remove(label);
        stopTime = System.currentTimeMillis();
    }

    @Override
    public Point getLocation() {
        return origin;//TODO -- decide if this should be location instead
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (targetMovement.x == 0) {
            int targetx = rng.nextInt(maxDistance.x * 2 + 1) + origin.x - maxDistance.x;
            targetMovement.x = (int) (targetx - location.x);
        } else if (targetMovement.x < 0) {
            targetMovement.x = Math.min(0, targetMovement.x + (int) (impulse + location.x % 1));
            location.x = Math.max(location.x - impulse, origin.x - maxDistance.x);
        } else {//targetMovement.x > 0
            targetMovement.x = Math.max(0, targetMovement.x - (int) (impulse + location.x % 1));
            location.x = Math.min(location.x + impulse, origin.x + maxDistance.x);
        }

        if (targetMovement.y == 0) {
            int targety = rng.nextInt(maxDistance.y * 2 + 1) + origin.y - maxDistance.y;
            targetMovement.y = (int) (targety - location.y);
        } else if (targetMovement.y < 0) {
            targetMovement.y = Math.min(0, targetMovement.y + (int) (impulse + location.y % 1));
            location.y = Math.max(location.y - impulse, origin.y - maxDistance.y);
        } else {//targetMovement.y > 0
            targetMovement.y = Math.max(0, targetMovement.y - (int) (impulse + location.y % 1));
            location.y = Math.min(location.y + impulse, origin.y + maxDistance.y);
        }

        label.setLocation((int) location.x, (int) location.y);
        label.invalidate();
    }
}
