package squidpony.squidgrid.gui.swing.animation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Queue;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Point3D;

/**
 * This class animates an image sliding directly from one point to another.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SlideAnimation implements Animation {
    private Queue<Point3D> moves;
    private JComponent component;
    private JLabel label;
    private int delay;

    /**
     * Creates a sliding animation from the start point to the end point.
     * 
     * @param image
     * @param start
     * @param end
     * @param delay 
     */
    public SlideAnimation(BufferedImage image, Point start, Point end, int delay) {
        moves = Bresenham.line2D(start.x, start.y, end.x, end.y);
        this.delay = delay;

        //set up JLabel to animate
        label = new JLabel(new ImageIcon(image));
        label.setBorder(null);
        label.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        label.setSize(label.getPreferredSize());
        label.setLocation(start.x,start.y);
        label.setVisible(true);
    }

    /**
     * Returns true if the component is still being moved.
     * 
     * @return 
     */
    @Override
    public boolean isActive() {
        return !moves.isEmpty();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!moves.isEmpty()) {
            label.setLocation(moves.poll());
            label.invalidate();
        }
    }

    @Override
    public int getDelay() {
        return delay;
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
    }

    @Override
    public Point getLocation() {
        return label.getLocation();
    }
}
