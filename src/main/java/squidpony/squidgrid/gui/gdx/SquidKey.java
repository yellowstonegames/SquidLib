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
 * running game logic if hasNext() returns false.
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
    private long currentEventTime;

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
            currentEventTime = (long)q.get(i++) << 32 | q.get(i++) & 0xFFFFFFFFL;
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
        return queue.size >= 4;
    }

    /**
     * Processes the first event queued up, passing it to this object's InputProcessor.
     */
    public void next()
    {
        IntArray q = processingQueue;
        synchronized (this) {
            if (processor == null || queue.size < 4) {
                queue.clear();
                return;
            }
            q.addAll(queue, 0, 4);
            queue.removeRange(0, 3);
        }
        if(q.size >= 4)
        {
            int t0 = q.get(0), t1 = q.get(1), e = q.get(2), n = q.get(3);
            currentEventTime = (long)t0 << 32 | t1 & 0xFFFFFFFFL;
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

    private void queueTime () {
        long time = TimeUtils.nanoTime();
        queue.add((int)(time >> 32));
        queue.add((int)time);
    }

    public synchronized boolean keyDown (int keycode) {
        queueTime();
        queue.add(KEY_DOWN);
        queue.add(keycode);
        return false;
    }

    public synchronized boolean keyUp (int keycode) {
        queueTime();
        queue.add(KEY_UP);
        queue.add(keycode);
        return false;
    }

    public synchronized boolean keyTyped (char character) {
        queueTime();
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

    public long getCurrentEventTime () {
        return currentEventTime;
    }
}
