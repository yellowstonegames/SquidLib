package squidpony.squidgrid.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a KeyListener which consumes all key events and then allows them to be retrieved one at a
 * time.
 *
 * Optionally blocks until input received when there are no events waiting.
 *
 * This listener is appropriate for a game loop driven application that regularly checks for user
 * input. If your application is event driven then a standard java.awt.even.KeyListener would be
 * more appropriate to use;
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SquidKey implements KeyListener, Iterable<KeyEvent>, Iterator<KeyEvent> {

    /**
     * Indicates when the capture should occur.
     *
     * If repeat keys should be captured when held down, then DOWN should be used.
     *
     * If complex character resolution is desired, such as 'A' or 'ctrl-alt-G' instead of seeing 'a'
     * and a chain of 'ctrl' 'alt' 'shift' 'g' then TYPED should be used. An alternative is to call
     * one of the methods from InputEvent that KeyEvent inherits, such as isShiftDown(), isAltDown(),
     * isControlDown(), or the more complicated getModifiersEx() . In this case, you will still get
     * keyPressed and keyReleased events for the modifier keys, but your handling of a KeyEvent can
     * ignores those events. Instead, for example, you might see that 'g' was pressed, so you check
     * isShiftDown() on the KeyEvent to see if it was really 'g' or 'G', determining whether the
     * action is to (g)et an item or to (G)rapple a monster.
     *
     * Using UP only captures the individual keys when they are let go and in the order they are let
     * go. This option is included for completion but is in most cases unlikely to have the desired
     * behavior for reading input.
     */
    public enum CaptureType {

        DOWN, UP, TYPED
    };
    private final CaptureType type;
    private final BlockingQueue<KeyEvent> queue = new LinkedBlockingQueue<>();
    private boolean blockOnEmpty, ignoreInput = false;

    /**
     * Creates a new listener which can optionally block when no input is currently available and
     * will capture on key up or key down depending on the parameters.
     *
     * @param blockOnEmpty if true then this object will wait until there is input before returning
     * from a request for the next event
     * @param type
     */
    public SquidKey(boolean blockOnEmpty, CaptureType type) {
        this.blockOnEmpty = blockOnEmpty;
        this.type = type;
    }

    /**
     * Creates a new listener which can optionally block when no input is currently available and
     * will capture on key up or key down depending on the parameters.
     *
     * @param blockOnEmpty if true then this object will wait until there is input before returning
     * from a request for the next event
     * @param type
     */
    public SquidKey(boolean blockOnEmpty, CaptureType type, boolean ignoreInput) {
        this.blockOnEmpty = blockOnEmpty;
        this.type = type;
        this.ignoreInput = ignoreInput;
    }

    /**
     * Will set the blocking behavior to the provided block type.
     * 
     * Flushes anything currently in the queue.
     * 
     * @param block 
     */
    public void blockOnEmpty(boolean block){
        blockOnEmpty = block;
        flush();
    }

    /**
     * Empties the backing queue of data.
     */
    public void flush() {
        queue.clear();
    }

    /**
     * Get the status for whether this should ignore input right now or not. True means this object will ignore and not
     * queue keypresses, false means it should process them normally. Useful to pause processing or delegate it to
     * another object temporarily.
     * @return true if this object currently ignores input, false otherwise.
     */
    public boolean getIgnoreInput() {
        return ignoreInput;
    }

    /**
     * Set the status for whether this should ignore input right now or not. True means this object will ignore and not
     * queue keypresses, false means it should process them normally. Useful to pause processing or delegate it to
     * another object temporarily.
     * @param ignoreInput true if this should object should ignore and not queue input, false otherwise.
     */
    public void setIgnoreInput(boolean ignoreInput) {
        this.ignoreInput = ignoreInput;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (type == CaptureType.TYPED && !ignoreInput) {
            queue.offer(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (type == CaptureType.DOWN && !ignoreInput) {
            queue.offer(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (type == CaptureType.UP && !ignoreInput) {
            queue.offer(e);
        }
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public Iterator<KeyEvent> iterator() {
        return this;
    }

    /**
     * Consumes the KeyEvent that is returned as it is returned.
     *
     * @return the next KeyEvent or null if there are no more
     */
    @Override
    public KeyEvent next() {
        KeyEvent ret = null;
        if (blockOnEmpty) {
            try {
                ret = queue.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(SquidKey.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ret = queue.poll();
        }
        return ret;
    }

    /**
     * The remove operation is not supported by this class.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
