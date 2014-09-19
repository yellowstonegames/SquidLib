package squidpony.squidgrid.gui.animation;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * Controls animations of images. Intended to be used with a Timer to activate
 * the animation frames.
 *
 * The component for the animation must be set before the animation will
 * function properly. All coordinates will be relative to the coordinate plane
 * of the provided component.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public interface Animation extends ActionListener {

    /**
     * Returns true if the component should continue to be allowed to animate.
     * Animations on a loop will always return true so some outside mechanism
     * must decide when to terminate them.
     *
     * @return true if the animation is still in action
     */
    public boolean isActive();


    /**
     * Returns the static image associated with this animation. Will typically
     * be the ending image, but this may vary based on the animation style.
     *
     * This method is intended to be called when converting an animation into a
     * static image.
     *
     * @return the image of the animation's final state
     */
    public BufferedImage getImage();

    /**
     * Sets the Component in which this animation will take place. This must be
     * set for the animation to function.
     *
     * @param component the GUI element to display in
     */
    public void setComponent(JComponent component);

    /**
     * Removes this animation from the component it's in.
     */
    public void remove();

    /**
     * Returns the coordinate of the top left corner of the animation.
     *
     * @return the location coordinate
     */
    public Point getLocation();
}