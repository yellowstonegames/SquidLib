package squidpony.squidgrid.gui.animation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

/**
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public abstract class AbstractAnimation implements Animation {

    protected JComponent component;
    protected JLabel label;
    protected long startTime;
    protected long lastTime;
    protected long endTime;
    protected Point start;
    protected Point end;

    public AbstractAnimation(BufferedImage image, Point start, Point end, long duration) {
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

    /**
     * Returns true if the component is still being moved.
     *
     * @return true if animation still operating
     */
    @Override
    public boolean isActive() {
        return endTime > lastTime;
    }

    @Override
    public abstract void actionPerformed(ActionEvent ae);

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
        if (!isActive()){//assume a finished animation is at its expected end point
            return end;
        }
        
        return label.getLocation();
    }

}
