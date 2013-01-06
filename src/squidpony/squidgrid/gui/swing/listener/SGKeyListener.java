package squidpony.squidgrid.gui.swing.listener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a KeyListener which consumes all key events and then allows them to
 * be retrieved one at a time.
 *
 * Optionally blocks until input received when there are no events waiting.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class SGKeyListener implements KeyListener {

    private BlockingQueue<KeyEvent> queue = new LinkedBlockingQueue<KeyEvent>();
    private boolean blockOnEmpty, captureOnKeyDown;

    /**
     * Crates a new listener which can optionally block when no input is
     * currently available and will capture on key up or key down depending on
     * the parameters.
     *
     * @param blockOnEmpty if true then this object will wait until there is
     * input before returning from a request for the next event
     * @param captureOnKeyDown if true then will capture events immediately upon
     * the key being pressed, if false then the key must be released before the
     * event is captured
     */
    public SGKeyListener(boolean blockOnEmpty, boolean captureOnKeyDown) {
        this.blockOnEmpty = blockOnEmpty;
        this.captureOnKeyDown = captureOnKeyDown;
    }

    /**
     * Returns the next KeyEvent. If the event queue is empty then a null is
     * returned if not blocking or when blocking but an interrupt has occured.
     *
     * @return
     */
    public KeyEvent getKeyEvent() {
        KeyEvent ret = null;
        if (blockOnEmpty) {
            try {
                ret = queue.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(SGKeyListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ret = queue.poll();
        }
        return ret;
    }

    /**
     * Empties the backing queue of data.
     */
    public void flush() {
        queue = new LinkedBlockingQueue<KeyEvent>();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //ignores this event type
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (captureOnKeyDown) {
            queue.offer(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!captureOnKeyDown) {
            queue.offer(e);
        }
    }
}
