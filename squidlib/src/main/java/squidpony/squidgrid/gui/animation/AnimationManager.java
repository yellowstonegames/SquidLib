package squidpony.squidgrid.gui.animation;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * This class manages a collection of animations and handles update drawing to the provided
 * BufferedImage.
 *
 * The delay parameters for this class are minimum values between updates. Because of the way the
 * Event Dispatch Thread works in Swing, there is no guarantee of how much time will actually pass
 * between updates.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class AnimationManager implements Runnable {

    private final ConcurrentHashMap<Animation, Timer> animations = new ConcurrentHashMap<>();
    private final JComponent component;
    private int defaultDelay = 1;

    /**
     * Creates a new AnimationManager that will draw to the provided component and then starts it in
     * a daemon worker thread to manage the animations.
     *
     * @param component the part of the GUI to be drawn on
     * @return the created AnimationManager
     */
    public static AnimationManager startNewAnimationManager(JComponent component) {
        AnimationManager am = new AnimationManager(component);
        Thread thread = new Thread(am);
        thread.setDaemon(true);
        thread.start();
        return am;
    }

    private AnimationManager(JComponent component) {
        this.component = component;
    }

    /**
     * Sets the minimum amount of time between updates for this animation manager.
     *
     * @param delay the minimum update time
     */
    public synchronized void setDefaultUpdateDelay(int delay) {
        this.defaultDelay = delay;
    }

    /**
     * Adds the animation and starts it immediately, with the given speed parameter indicating how
     * many milliseconds to wait between animation steps.
     *
     * Additionally sets the animations component to be this animation manager's component.
     *
     * @param animation the animation object to be added
     */
    public void add(Animation animation) {
        add(animation, defaultDelay);
    }

    /**
     * Adds the animation and starts it immediately, with the given speed parameter indicating how
     * many milliseconds to wait between animation steps.
     *
     * Additionally sets the animations component to be this animation manager's component.
     *
     * @param animation the animation object to be added
     * @param delay the delay specific to this animation
     */
    public void add(Animation animation, int delay) {
        if (!animations.containsKey(animation)) {
            Timer timer = new Timer(delay, animation);
            animations.put(animation, timer);
            animation.setComponent(component);
            timer.start();
        }
    }

    /**
     * Stops the given animation if it is in the current list of running animations.
     *
     * @param animation the animation to be stopped
     */
    public synchronized void stopAnimation(Animation animation) {
        if (animations.containsKey(animation)) {
            animations.remove(animation).stop();
        }
    }

    /**
     * Stops and removes any animations that are no longer active.
     */
    private void trimAnimations() {
        LinkedList<Animation> removes = new LinkedList<>();
        for (Animation anim : animations.keySet()) {
            if (!anim.isActive()) {
                removes.add(anim);
            }
        }

        for (Animation anim : removes) {
            stopAnimation(anim);
        }
    }

    @Override
    public void run() {
        while (true) {
            trimAnimations();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(AnimationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
