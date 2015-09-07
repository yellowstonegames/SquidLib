package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * This wraps an InputProcessor, storing all key events and allowing them to be processed one at a time using next() or
 * all at once using drain(). To have an effect, it needs to be registered by calling Input.setInputProcessor(SquidKey).
 *
 * It does not perform the blocking functionality of the non-GDX SquidKey implementation, because this is meant to run
 * in an event-driven libGDX game and should not step on the toes of libGDX's input handling. To block game logic
 * until an event has been received, check hasNext() in the game's render() method and effectively "block" by not
 * running game logic if hasNext() returns false. You can get an event if hasNext() returns true by calling next().
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Nathan Sweet
 * @author Tommy Ettinger
 * */
public class SquidKey implements InputProcessor {
    static private final int KEY_DOWN = 0;
    static private final int KEY_UP = 1;
    static private final int KEY_TYPED = 2;

    private InputProcessor processor;
    private final IntArray queue = new IntArray();
    private final IntArray processingQueue = new IntArray();
    private boolean ignoreInput = false;

    /**
     * Constructs a SquidKey with no InputProcessor; for this to do anything, setProcessor() must be called.
     */
    public SquidKey () {
    }

    /**
     * Constructs a SquidKey with the given InputProcessor.
     * @param processor An InputProcessor that will handle keyDown(), keyUp(), and keyTyped() events
     */
    public SquidKey (InputProcessor processor) {
        this.processor = processor;
    }

    /**
     * Constructs a SquidKey with the given InputProcessor.
     * @param processor An InputProcessor that will handle keyDown(), keyUp(), and keyTyped() events
     * @param ignoreInput the starting value for the ignore status; true to ignore input, false to process it.
     */
    public SquidKey (InputProcessor processor, boolean ignoreInput) {
        this.processor = processor;
        this.ignoreInput = ignoreInput;
    }

    /**
     * Sets the InputProcessor that this object will use to make sense of Key events.
     * @param processor An InputProcessor that will handle keyDown(), keyUp(), and keyTyped() events
     */
    public void setProcessor (InputProcessor processor) {
        this.processor = processor;
    }

    /**
     * Gets this object's InputProcessor.
     * @return
     */
    public InputProcessor getProcessor () {
        return processor;
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

    /**
     * Processes all events queued up, passing them to this object's InputProcessor.
     */
    public void drain () {
        IntArray q = processingQueue;
        synchronized (this) {
            if (processor == null) {
                queue.clear();
                return;
            }
            q.addAll(queue);
            queue.clear();
        }
        for (int i = 0, n = q.size; i < n;) {
            switch (q.get(i++)) {
                case KEY_DOWN:
                    processor.keyDown(q.get(i++));
                    break;
                case KEY_UP:
                    processor.keyUp(q.get(i++));
                    break;
                case KEY_TYPED:
                    processor.keyTyped((char)q.get(i++));
                    break;
            }
        }
        q.clear();
    }

    /**
     * Returns true if at least one event is queued.
     * @return true if there is an event queued, false otherwise.
     */
    public boolean hasNext()
    {
        return queue.size >= 2;
    }

    /**
     * Processes the first event queued up, passing it to this object's InputProcessor.
     */
    public void next()
    {
        IntArray q = processingQueue;
        synchronized (this) {
            if (processor == null || queue.size < 2) {
                queue.clear();
                return;
            }
            q.addAll(queue, 0, 2);
            queue.removeRange(0, 1);
        }
        if(q.size >= 2)
        {
            int e = q.get(0), n = q.get(1);
            switch (e) {
                case KEY_DOWN:
                    processor.keyDown(n);
                    break;
                case KEY_UP:
                    processor.keyUp(n);
                    break;
                case KEY_TYPED:
                    processor.keyTyped((char)n);
                    break;
            }
        }
        q.clear();
    }

    /**
     * Empties the backing queue of data.
     */
    public void flush()
    {
        queue.clear();
    }

    public synchronized boolean keyDown (int keycode) {
        if(ignoreInput) return false;
        queue.add(KEY_DOWN);
        queue.add(keycode);
        return false;
    }

    public synchronized boolean keyUp (int keycode) {
        if(ignoreInput) return false;
        queue.add(KEY_UP);
        queue.add(keycode);
        return false;
    }

    public synchronized boolean keyTyped (char character) {
        if(ignoreInput) return false;
        queue.add(KEY_TYPED);
        queue.add(character);
        return false;
    }

    public synchronized boolean touchDown (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public synchronized boolean touchUp (int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public synchronized boolean touchDragged (int screenX, int screenY, int pointer) {
        return false;
    }

    public synchronized boolean mouseMoved (int screenX, int screenY) {
        return false;
    }

    public synchronized boolean scrolled (int amount) {
        return false;
    }

}
