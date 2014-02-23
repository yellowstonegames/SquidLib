package squidpony.squidgrid.gui.swing.animation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

/**
 * Animates an object moving smoothly in a direction and then bouncing back.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class BumpAnimation implements Animation {

    private JComponent component;
    private JLabel label;
    private Point start, end;
    private long startTime, lastTime, endTime;

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

    /**
     * Creates a bump animation that will travel from the start to the end point and then back. With
     * the entire animation taking the given time, in milliseconds.
     *
     * @param image
     * @param start
     * @param end
     * @param duration
     */
    public BumpAnimation(BufferedImage image, Point start, Point end, long duration) {
        this.start = start;
        this.end = end;

        //set up JLabel to animate
        label = new JLabel(new ImageIcon(image));
        label.setBorder(null);
        label.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        label.setSize(label.getPreferredSize());
        label.setLocation(start.x, start.y);
        label.setVisible(true);

        startTime = System.currentTimeMillis();
        lastTime = startTime;
        endTime = startTime + duration;
    }

    @Override
    public boolean isActive() {
        return endTime > lastTime;
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
        if (isActive()) {
            lastTime = System.currentTimeMillis();
            float ratio = (endTime - startTime) / (float) (lastTime - startTime);
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
