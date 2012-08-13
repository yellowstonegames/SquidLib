package squidpony.squidgrid.gui.animation;

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
 * Animates an object moving smoothly in a direction and then bouncing back.
 *
 * @author Eben Howard http://squidpony.com
 */
public class BumpAnimation implements Animation {
    private Queue<Point3D> moves;
    private JComponent component;
    private JLabel label;
    private Point start;

    /**
     * Creates a bump animation that will travel one cell in the given direction.
     * 
     * @param image
     * @param start
     * @param cellSize
     * @param direction 
     */
    public BumpAnimation(BufferedImage image, Point start, Dimension cellSize, Point direction) {
        this(image, start, new Point(start.x + direction.x * cellSize.width, start.y + direction.y * cellSize.height));
    }

    /**
     * Creates a bump animation that will travel from the start to the end point and then back.
     * 
     * @param image
     * @param start
     * @param end 
     */
    public BumpAnimation(BufferedImage image, Point start, Point end) {
        this.start = start;
        moves = Bresenham.line2D(start, end);
        moves.addAll(Bresenham.line2D(end, start));

        //set up JLabel to animate
        label = new JLabel(new ImageIcon(image));
        label.setBorder(null);
        label.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        label.setSize(label.getPreferredSize());
        label.setLocation(start.x, start.y);
        label.setVisible(true);
    }

    @Override
    public boolean isActive() {
        return !moves.isEmpty();
    }

    @Override
    public int getDelay() {
        return 3;
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
        return start;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!moves.isEmpty()) {
            label.setLocation(moves.poll());
            label.invalidate();
        }
    }
}
