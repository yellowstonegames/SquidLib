package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntIntMap;
import squidpony.squidmath.IntVLA;

/**
 * This input processing class can handle mouse and keyboard input, using a squidpony.squidgrid.gui.gdx.SquidMouse for
 * Mouse input and a user implementation of the SquidInput.KeyHandler interface to react to keys represented as chars
 * and the modifiers those keys were pressed with, any of alt, ctrl, and/or shift. Not all keys are representable by
 * default in unicode, so symbolic representations are stored in constants in this class, and are passed to
 * {@link KeyHandler#handle(char, boolean, boolean, boolean)} as chars like DOWN_ARROW or its value, '\u2193'. Shift
 * modifies the input as it would on a QWERTY keyboard, and the exact mapping is documented in
 * {@link #fromCode(int, boolean)} as well. This class handles mouse input immediately, but stores keypresses in a
 * queue, storing all key events and allowing them to be processed one at a time using {@link #next()} or all at once
 * using {@link #drain()}. To have an effect, it needs to be registered by calling
 * {@link Input#setInputProcessor(InputProcessor)}. Note that calling {@link #hasNext()} does more than just check if
 * there are events that can be processed; because hasNext() is expected to be called frequently, it is also the point
 * where this class checks if a key is being held and so the next event should occur. Holding a key only causes the
 * keyDown() method of InputListener to be called once, so this uses hasNext() to see if there should be a next event
 * coming from a held key.
 * <br>
 * This also allows some key remapping, including remapping so a key pressed with modifiers like Ctrl and Shift could
 * act like '?' (which could be used by expert players to avoid accidentally opening a help menu they don't need), and
 * that would free up '?' for some other use that could also be remapped. The remap() methods do much of this, often
 * with help from {@link #combineModifiers(char, boolean, boolean, boolean)}, while the unmap() methods allow removal of
 * any no-longer-wanted remappings.
 * <br>
 * It does not perform the blocking functionality of earlier SquidKey implementations, because this is meant to run
 * in an event-driven libGDX game and should not step on the toes of libGDX's input handling. To block game logic
 * until an event has been received, check hasNext() in the game's render() method and effectively "block" by not
 * running game logic if hasNext() returns false. You can process an event if hasNext() returns true by calling 
 * {@link #next()}. Mouse inputs do not affect hasNext(), and next() will process only key pressed events. Also, see
 * above about the extra behavior of hasNext regarding held keys.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Nathan Sweet
 * @author Tommy Ettinger
 * */
public class SquidInput extends InputAdapter {
    /**
     * A single-method interface used to process "typed" characters, special characters produced by unusual keys, and
     * modifiers that can affect them. SquidInput has numerous static char values that are expected to be passed
     * to handle() in place of the special keys (such as arrow keys) that do not have a standard char value.
     */
    public interface KeyHandler{
        /**
         * The only method you need to implement yourself in KeyHandler, this should react to keys such as
         * 'a' (produced by pressing the A key while not holding Shift), 'E' (produced by pressing the E key while
         * holding Shift), and '\u2190' (left arrow in unicode, also available as a constant in SquidInput, produced by
         * pressing the left arrow key even though that key does not have a default unicode representation). Capital
         * letters will be capitalized when they are passed to this, but they may or may not have the shift argument as
         * true depending on how this method was called. Symbols that may be produced by holding Shift and pressing a
         * number or a symbol key can vary between keyboards (some may require Shift to be held down, others may not).
         * <br>
         * This can react to the input in whatever way you find appropriate for your game.
         * @param key a char of the "typed" representation of the key, such as 'a' or 'E', or if there is no Unicode
         *            character for the key, an appropriate alternate character as documented in SquidInput.fromKey()
         * @param alt true if the Alt modifier was being held while this key was entered, false otherwise.
         * @param ctrl true if the Ctrl modifier was being held while this key was entered, false otherwise.
         * @param shift true if the Shift modifier was being held while this key was entered, false otherwise.
         */
        void handle(char key, boolean alt, boolean ctrl, boolean shift);
    }

    protected KeyHandler keyAction;
    protected boolean numpadDirections = true, ignoreInput = false;
    protected SquidMouse mouse;
    protected final IntVLA queue = new IntVLA();
    protected long lastKeyTime = -1000000L;
    protected int lastKeyCode = -1;
    protected long repeatGapMillis = 220L;
    public final IntIntMap mapping = new IntIntMap(128);
    /**
     * Constructs a new SquidInput that does not respond to keyboard or mouse input. These can be set later by calling
     * setKeyHandler() to allow keyboard handling or setMouse() to allow mouse handling on a grid.
     */
    public SquidInput() {
        keyAction = null;
        mouse = new SquidMouse(12, 12, new InputAdapter());
    }

    /**
     * Constructs a new SquidInput that does not respond to keyboard input, but does take mouse input and passes mouse
     * events along to the given SquidMouse. The SquidMouse, even though it is an InputProcessor on its own, should not
     * be registered by calling Input.setInputProcessor(SquidMouse), and instead this object should be registered by
     * calling Input.setInputProcessor(SquidInput). The keyboard and mouse handling can be changed later by calling
     * setKeyHandler() to allow keyboard handling or setMouse() to change mouse handling.
     * @param mouse a SquidMouse instance that will be used for handling mouse input. Must not be null.
     */
    public SquidInput(SquidMouse mouse) {
        keyAction = null;
        this.mouse = mouse;
    }

    /**
     * Constructs a new SquidInput that does not respond to mouse input, but does take keyboard input and sends keyboard
     * events through some processing before calling keyHandler.handle() on keypresses that can sensibly be processed.
     * Modifier keys do not go through the same processing but are checked for their current state when the key is
     * pressed, and the states of alt, ctrl, and shift are passed to keyHandler.handle() as well.
     * You can use setMouse() to allow mouse handling or change the KeyHandler with setKeyHandler().
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     */
    public SquidInput(KeyHandler keyHandler) {
        keyAction = keyHandler;
        mouse = new SquidMouse(12, 12, new InputAdapter());
    }
    /**
     * Constructs a new SquidInput that does not respond to mouse input, but does take keyboard input and sends keyboard
     * events through some processing before calling keyHandler.handle() on keypresses that can sensibly be processed.
     * Modifier keys do not go through the same processing but are checked for their current state when the key is
     * pressed, and the states of alt, ctrl, and shift are passed to keyHandler.handle() as well.
     * You can use setMouse() to allow mouse handling or change the KeyHandler with setKeyHandler().
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     * @param ignoreInput true if this should ignore input initially, false if it should process input normally.
     */
    public SquidInput(KeyHandler keyHandler, boolean ignoreInput) {
        keyAction = keyHandler;
        mouse = new SquidMouse(12, 12, new InputAdapter());
        this.ignoreInput = ignoreInput;
    }
    /**
     * Constructs a new SquidInput that responds to mouse and keyboard input when given a SquidMouse and a
     * SquidInput.KeyHandler implementation. It sends keyboard events through some processing before calling
     * keyHandler.handle() on keypresses that can sensibly be processed. Modifier keys do not go through the same
     * processing but are checked for their current state when the key is pressed, and the states of alt, ctrl, and
     * shift are passed to keyHandler.handle() as well. The SquidMouse, even though it is an
     * InputProcessor on its own, should not be registered by calling Input.setInputProcessor(SquidMouse), and instead
     * this object should be registered by calling Input.setInputProcessor(SquidInput). You can use setKeyHandler() or
     * setMouse() to change keyboard or mouse handling.
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     * @param mouse a SquidMouse instance that will be used for handling mouse input. Must not be null.
     */
    public SquidInput(KeyHandler keyHandler, SquidMouse mouse) {
        keyAction = keyHandler;
        this.mouse = mouse;
    }

    /**
     * Constructs a new SquidInput that responds to mouse and keyboard input when given a SquidMouse and a
     * SquidInput.KeyHandler implementation, and can be put in an initial state where it ignores input until told
     * otherwise via setIgnoreInput(boolean). It sends keyboard events through some processing before calling
     * keyHandler.handle() on keypresses that can sensibly be processed. Modifier keys do not go through the same
     * processing but are checked for their current state when the key is pressed, and the states of alt, ctrl, and
     * shift are passed to keyHandler.handle() as well. The SquidMouse, even though it is an
     * InputProcessor on its own, should not be registered by calling Input.setInputProcessor(SquidMouse), and instead
     * this object should be registered by calling Input.setInputProcessor(SquidInput). You can use setKeyHandler() or
     * setMouse() to change keyboard or mouse handling.
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     * @param mouse a SquidMouse instance that will be used for handling mouse input. Must not be null.
     * @param ignoreInput true if this should ignore input initially, false if it should process input normally.
     */
    public SquidInput(KeyHandler keyHandler, SquidMouse mouse, boolean ignoreInput) {
        keyAction = keyHandler;
        this.mouse = mouse;
        this.ignoreInput = ignoreInput;
    }

    public void setKeyHandler(KeyHandler keyHandler)
    {
        keyAction = keyHandler;
    }
    public void setMouse(SquidMouse mouse)
    {
        this.mouse = mouse;
    }

    public boolean isUsingNumpadDirections() {
        return numpadDirections;
    }

    public void setUsingNumpadDirections(boolean using) {
        numpadDirections = using;
    }

    public KeyHandler getKeyHandler() {
        return keyAction;
    }

    public SquidMouse getMouse() {
        return mouse;
    }

    /**
     * Get the status for whether this should ignore input right now or not. True means this object will ignore and not
     * queue input, false means it should process them normally. Useful to pause processing or delegate it to
     * another object temporarily.
     * @return true if this object currently ignores input, false otherwise.
     */
    public boolean getIgnoreInput() {
        return ignoreInput;
    }

    /**
     * Set the status for whether this should ignore input right now or not. True means this object will ignore and not
     * queue input, false means it should process them normally. Useful to pause processing or delegate it to
     * another object temporarily.
     * @param ignoreInput true if this should object should ignore and not queue input, false otherwise.
     */
    public void setIgnoreInput(boolean ignoreInput) {
        this.ignoreInput = ignoreInput;
    }

    /**
     * Remaps a char that could be input and processed by {@link KeyHandler#handle(char, boolean, boolean, boolean)}
     * (possibly with some pressed modifiers) to another char with possible modifiers. When the first key/modifier mix
     * is received, it will be translated to the second group in this method.
     * @param pressedChar a source char that might be used in handling, like 'q', 'A', '7', '(', or {@link #UP_ARROW}.
     * @param pressedAlt true if alt is part of the source combined keypress, false otherwise
     * @param pressedCtrl true if ctrl is part of the source combined keypress, false otherwise
     * @param pressedShift true if shift is part of the source combined keypress, false otherwise
     * @param targetChar a target char that might be used in handling, like 'q', 'A', '7', '(', or {@link #UP_ARROW}.
     * @param targetAlt true if alt is part of the target combined keypress, false otherwise
     * @param targetCtrl true if ctrl is part of the target combined keypress, false otherwise
     * @param targetShift true if shift is part of the target combined keypress, false otherwise
     * @return this for chaining
     */
    public SquidInput remap(char pressedChar, boolean pressedAlt, boolean pressedCtrl, boolean pressedShift,
                            char targetChar, boolean targetAlt, boolean targetCtrl, boolean targetShift)
    {
        mapping.put(combineModifiers(pressedChar, pressedAlt, pressedCtrl, pressedShift),
                combineModifiers(targetChar, targetAlt, targetCtrl, targetShift));
        return this;
    }

    /**
     * Remaps a keypress combination, which is a char and several potential modifiers, to another keypress combination.
     * When the {@code pressed} combination is received, it will be translated to {@code target} in this method.
     * @see #combineModifiers(char, boolean, boolean, boolean) combineModifiers is usually used to make pressed and target
     * @param pressed an int for the source keypress, probably produced by {@link #combineModifiers(char, boolean, boolean, boolean)}
     * @param target an int for the target keypress, probably produced by {@link #combineModifiers(char, boolean, boolean, boolean)}
     * @return this for chaining
     */
    public SquidInput remap(int pressed, int target)
    {
        mapping.put(pressed, target);
        return this;
    }
    /**
     * Remaps many keypress combinations, each of which is a char and several potential modifiers, to other keypress
     * combinations. When the first of a pair of combinations is received, it will be translated to the second
     * combination of the pair.
     * @see #combineModifiers(char, boolean, boolean, boolean) combineModifiers is usually used to make the contents of pairs
     * @param pairs an int array alternating source and target keypresses, each probably produced by {@link #combineModifiers(char, boolean, boolean, boolean)}
     * @return this for chaining
     */
    public SquidInput remap(int[] pairs)
    {
        int len;
        if(pairs == null || (len = pairs.length) <= 1)
            return this;
        for (int i = 0; i < len - 1; i++) {
            mapping.put(pairs[i], pairs[++i]);
        }
        return this;
    }

    /**
     * Removes a keypress combination from the mapping by specifying the char and any modifiers that are part of the
     * keypress combination. This combination will no longer be remapped, but the original handling will be the same.
     * @param pressedChar a char that might be used in handling, like 'q', 'A', '7', '(', or {@link #UP_ARROW}.
     * @param pressedAlt true if alt is part of the combined keypress, false otherwise
     * @param pressedCtrl true if ctrl is part of the combined keypress, false otherwise
     * @param pressedShift true if shift is part of the combined keypress, false otherwise
     * @return this for chaining
     */
    public SquidInput unmap(char pressedChar, boolean pressedAlt, boolean pressedCtrl, boolean pressedShift){
        mapping.remove(combineModifiers(pressedChar, pressedAlt,pressedCtrl,pressedShift), 0);
        return this;
    }
    /**
     * Removes a keypress combination from the mapping by specifying the keypress combination as an int. This
     * combination will no longer be remapped, but the original handling will be the same.
     * @see #combineModifiers(char, boolean, boolean, boolean) combineModifiers is usually used to make pressed
     * @param pressed an int for the source keypress, probably produced by {@link #combineModifiers(char, boolean, boolean, boolean)}
     * @return this for chaining
     */
    public SquidInput unmap(int pressed)
    {
        mapping.remove(pressed, 0);
        return this;
    }

    /**
     * Removes any remappings to key bindings that were in use in this SquidInput.
     * @return this for chaining
     */
    public SquidInput clearMapping()
    {
        mapping.clear();
        return this;
    }
    /**
     * Combines the key (as it would be given to {@link KeyHandler#handle(char, boolean, boolean, boolean)}) with the
     * three booleans for the alt, ctrl, and shift modifier keys, returning an int that can be used with the internal
     * queue of ints or the public {@link #mapping} of received inputs to actual inputs the program can process.
     * @param key a char that might be used in handling, like 'q', 'A', '7', '(', or {@link #UP_ARROW}.
     * @param alt true if alt is part of this combined keypress, false otherwise
     * @param ctrl true if ctrl is part of this combined keypress, false otherwise
     * @param shift true if shift is part of this combined keypress, false otherwise
     * @return an int that contains the information to represent the key with any modifiers as one value
     */
    public static int combineModifiers(char key, boolean alt, boolean ctrl, boolean shift) {
        int c = alt ? (key | 0x10000) : key;
        c |= ctrl ? 0x20000 : 0;
        c |= shift ? 0x40000 : 0;
        return c;
    }

    /**
     * Gets the current key remapping as a String, which can be saved in a file and read back with
     * {@link #keyMappingFromString(String)}. This will allow input with any of Shift, Alt, and Ctl modifiers in any
     * script supported by the Unicode Basic Multilingual Plane (the first 65536 chars of Unicode).
     * @return a String that stores four chars per remapping.
     */
    public String keyMappingToString()
    {
        char[] cs = new char[mapping.size << 2];
        int i = 0;
        for(IntIntMap.Entry ent : mapping)
        {
            cs[i++] = (char)(ent.key);
            cs[i++] = (char)((ent.key>>>16) + 64);
            cs[i++] = (char)(ent.value);
            cs[i++] = (char)((ent.value>>>16) + 64);
        }
        return String.valueOf(cs);
    }

    /**
     * Reads in a String (almost certainly produced by {@link #keyMappingToString()}) to set the current key remapping.
     * This will allow input with any of Shift, Alt, and Ctl modifiers in any script supported by the Unicode Basic
     * Multilingual Plane (the first 65536 chars of Unicode). You may want to call {@link #clearMapping()} before
     * calling this if you have already set the mapping and want the String's contents to be used as the only mapping.
     * @param keymap a String that was probably produced by {@link #keyMappingToString()}
     * @return this for chaining
     */
    public SquidInput keyMappingFromString(String keymap)
    {
        int len, k, v;
        if(keymap == null || (len = keymap.length()) < 4)
            return this;
        for (int i = 0; i < len - 3; i++) {
            k = keymap.charAt(i);
            k |= (keymap.charAt(++i) - 64) << 16;
            v = keymap.charAt(++i);
            v |= (keymap.charAt(++i) - 64) << 16;
            mapping.put(k, v);
        }
        return this;
    }

    /**
     * Processes all events queued up, passing them through this object's key processing and then to keyHandler. Mouse
     * events are not queued and are processed when they come in.
     */
    public void drain () {
        IntVLA qu = queue;

        if (keyAction == null || qu.size <= 0) {
            qu.clear();
            return;
        }

        for (int i = 0, n = qu.size, t; i < n; ) {
            t = qu.get(i++);
            t = mapping.get(t, t);
            keyAction.handle((char)t, (t & 0x10000) != 0, (t & 0x20000) != 0, (t & 0x40000) != 0);
        }

        qu.clear();
    }

    /**
     * Gets the amount of milliseconds of holding a key this requires to count as a key repeat. The default is 220. 
     * @return how long a key needs to be held before this will count it as a key repeat, as a long in milliseconds
     */
    public long getRepeatGap() {
        return repeatGapMillis;
    }

    /**
     * Sets the amount of milliseconds of holding a key this requires to count as a key repeat. The default is 220.
     * @param time how long a key needs to be held before this will count it as a key repeat, as a positive long in milliseconds
     */
    public void setRepeatGap(long time) {
        repeatGapMillis = time;
    }

    /**
     * Returns true if at least one event is queued, but also will call {@link #keyDown(int)} if a key is being held but
     * there is a failure to process the repeated event. The conditions this checks:
     * <ul>
     *     <li>Is a key is currently being held?</li>
     *     <li>Are no modifier keys being held (shift, control, alt)? (without this check, the modifier key counts as a
     *     repeat of the key it modifies, which is probably never the intended behavior)</li>
     *     <li>Has {@link #keyDown(int)} already been called at least once?</li>
     *     <li>Have there been at least {@link #getRepeatGap()} milliseconds between the last key being received and this call?</li>
     * </ul>
     * If all of these conditions are true, keyDown() is called again with the last key it had received, and if this has
     * an effect (the key can be handled), then generally an event should be queued, so this will have a next event and
     * should return true. You can change the amount of time required to hold a key to cause key repeats with
     * {@link #setRepeatGap(long)}, but the default of 220 ms is usually suitable. Too low of a value can cause normal
     * key presses to be counted twice or more, and too high of a value may delay an expected key repeat.
     * @return true if there is an event queued, false otherwise
     */
    public boolean hasNext()
    {
        if(Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)
                && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
                && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
                && !Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
                && !Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)
                && lastKeyCode >= 0
                && System.currentTimeMillis() - lastKeyTime > repeatGapMillis // defaults to 220 ms
                )
        {
            keyDown(lastKeyCode);
        }
        return queue.size > 0;
    }

    /**
     * Processes the first key event queued up, passing it to this object's InputProcessor. Mouse events are not
     * queued and are processed when they come in.
     */
    public void next() {
        IntVLA qu = queue;
        if (keyAction == null) {
            qu.clear(); // Accidentally captured a keypress when keys aren't meant to be handled
            return;
        }
        if (qu.isEmpty()) {
            return;
        }
        int t = qu.removeIndex(0);
        t = mapping.get(t, t);
        keyAction.handle((char)t, (t & 0x10000) != 0, (t & 0x20000) != 0, (t & 0x40000) != 0);
    }

    /**
     * Empties the backing queue of data.
     */
    public void flush()
    {
        queue.clear();
    }

    @Override
	public boolean keyDown (int keycode) {
        if (ignoreInput || keyAction == null) {
            return false;
        }
        lastKeyTime = System.currentTimeMillis();
        lastKeyCode = keycode;
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        int c = fromCode(keycode, shift);
        if(c != '\0') {
            c |= (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
                    ? 0x10000 : 0;
            c |= (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
                    ? 0x20000 : 0;
            c |= (shift)
                    ? 0x40000 : 0;
            queue.add(c);
        }
        return false;
    }

    @Override
	public boolean keyUp (int keycode) {
//        queue.add(KEY_UP);
//        queue.add(keycode);
        return false;
    }

    @Override
	public boolean keyTyped (char character) {
        return false;
    }

    @Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if(ignoreInput) return false;
        return mouse.touchDown(screenX, screenY, pointer, button);
    }

    @Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if(ignoreInput) return false;
        return mouse.touchUp(screenX, screenY, pointer, button);
    }

    @Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
        if(ignoreInput) return false;
        return mouse.touchDragged(screenX, screenY, pointer);
    }

    @Override
	public boolean mouseMoved (int screenX, int screenY) {
        if(ignoreInput) return false;
        return mouse.mouseMoved(screenX, screenY);
    }

    @Override
	public boolean scrolled (int amount) {
        if(ignoreInput) return false;
        return mouse.scrolled(amount);
    }

    /**
     * Maps keycodes to unicode chars, sometimes depending on whether the Shift key is held.
     *
     * It is strongly recommended that you refer to key combinations regarding non-alphabet keys by using, for example,
     * Ctrl-Shift-; instead of Ctrl-:, that is to use the unshifted key with Shift instead of assuming that all
     * keyboards will use the QWERTY layout. Pressing shift while pressing just about any representable symbol will map
     * to the shifted version as if on a QWERTY keyboard, and if you don't have a QWERTY keyboard, the mappings are
     * documented in full below.
     *
     * Keys 'a' to 'z' report 'A' to 'Z' when shift is held. Non-ASCII-Latin characters do not have this behavior, since
     * most keyboards would be unable to send keys for a particular language and A-Z are very common. You can still
     * allow a response to, e.g. 'ä' and 'Ä' separately by checking for whether Shift was pressed in conjunction with
     * 'ä' on a keyboard with that key, which can be useful when users can configure their own keyboard layouts.
     *
     * Top row numbers map as follows:
     *
     * {@literal '1' to '!', '2' to '@', '3' to '#', '4' to '$', '5' to '%',}
     * {@literal '6' to '^', '7' to '&amp;', '8' to '*', '9' to '(', '0' to ')'}
     *
     * Numpad numbers will report a SquidInput constant such as UP_LEFT_ARROW for Numpad 7, but only if numpadDirections
     * is true; otherwise they send the number (here, 7). Numpad 0 sends VERTICAL_ARROW or 0.
     *
     * Most symbol keys are mapped to a single unicode char as a constant in SquidInput and disregard Shift. The
     * constant is usually the same as the name of the char; possible exceptions are Backspace (on PC) or Delete (on
     * Mac) mapping to BACKSPACE, Delete (on PC) mapping to FORWARD_DELETE, Esc mapping to ESCAPE, and Enter (on PC) or
     * Return (on Mac) mapping to ENTER.
     *
     * {@literal ':', '*', '#', '@'}, and space keys, if present, always map to themselves, regardless of Shift.
     *
     * Other characters map as follows when Shift is held, as they would on a QWERTY keyboard:
     * <ul>
     * <li>{@code ','} to {@code '<'}</li>
     * <li>{@code '.'} to {@code '>'}</li>
     * <li>{@code '/'} to {@code '?'}</li>
     * <li>{@code ';'} to {@code ':'}</li>
     * <li>{@code '\''} to {@code '"'}</li>
     * <li>{@code '['} to <code>'{'</code></li>
     * <li>{@code ']'} to <code>'}'</code></li>
     * <li>{@code '|'} to {@code '\\'}</li>
     * <li>{@code '-'} to {@code '_'}</li>
     * <li>{@code '+'} to {@code '='}</li>
     * <li>{@code '`'} to {@code '~'} (note, this key produces no event on the GWT backend)</li>
     * </ul>
     * @param keycode a keycode as passed by LibGDX
     * @param shift true if Shift key is being held.
     * @return a char appropriate to the given keycode; often uses shift to capitalize or change a char, but not for keys like the arrow keys that normally don't produce chars
     */
    public char fromCode(int keycode, boolean shift)
    {
        switch (keycode) {
            case Input.Keys.HOME:
                return HOME;
            case Input.Keys.FORWARD_DEL:
                return FORWARD_DELETE;
            case Input.Keys.ESCAPE:
                return ESCAPE;
            case Input.Keys.END:
                return END;

            case Input.Keys.UP:
                return UP_ARROW;
            case Input.Keys.DOWN:
                return DOWN_ARROW;
            case Input.Keys.LEFT:
                return LEFT_ARROW;
            case Input.Keys.RIGHT:
                return RIGHT_ARROW;
            case Input.Keys.CENTER:
                return CENTER_ARROW;

            case Input.Keys.NUM_0:
                return (shift) ? ')' : '0';
            case Input.Keys.NUM_1:
                return (shift) ? '!' : '1';
            case Input.Keys.NUM_2:
                return (shift) ? '@' : '2';
            case Input.Keys.NUM_3:
                return (shift) ? '#' : '3';
            case Input.Keys.NUM_4:
                return (shift) ? '$' : '4';
            case Input.Keys.NUM_5:
                return (shift) ? '%' : '5';
            case Input.Keys.NUM_6:
                return (shift) ? '^' : '6';
            case Input.Keys.NUM_7:
                return (shift) ? '&' : '7';
            case Input.Keys.NUM_8:
                return (shift) ? '*' : '8';
            case Input.Keys.NUM_9:
                return (shift) ? '(' : '9';
            case Input.Keys.NUMPAD_0:
                return (numpadDirections) ? VERTICAL_ARROW : '0';
            case Input.Keys.NUMPAD_1:
                return (numpadDirections) ? DOWN_LEFT_ARROW : '1';
            case Input.Keys.NUMPAD_2:
                return (numpadDirections) ? DOWN_ARROW : '2';
            case Input.Keys.NUMPAD_3:
                return (numpadDirections) ? DOWN_RIGHT_ARROW : '3';
            case Input.Keys.NUMPAD_4:
                return (numpadDirections) ? LEFT_ARROW : '4';
            case Input.Keys.NUMPAD_5:
                return (numpadDirections) ? CENTER_ARROW : '5';
            case Input.Keys.NUMPAD_6:
                return (numpadDirections) ? RIGHT_ARROW : '6';
            case Input.Keys.NUMPAD_7:
                return (numpadDirections) ? UP_LEFT_ARROW : '7';
            case Input.Keys.NUMPAD_8:
                return (numpadDirections) ? UP_ARROW : '8';
            case Input.Keys.NUMPAD_9:
                return (numpadDirections) ? UP_RIGHT_ARROW : '9';
            case Input.Keys.COLON:
                return ':';
            case Input.Keys.STAR:
                return '*';
            case Input.Keys.POUND:
                return '#';
            case Input.Keys.A:
                return (shift) ? 'A' : 'a';
            case Input.Keys.B:
                return (shift) ? 'B' : 'b';
            case Input.Keys.C:
                return (shift) ? 'C' : 'c';
            case Input.Keys.D:
                return (shift) ? 'D' : 'd';
            case Input.Keys.E:
                return (shift) ? 'E' : 'e';
            case Input.Keys.F:
                return (shift) ? 'F' : 'f';
            case Input.Keys.G:
                return (shift) ? 'G' : 'g';
            case Input.Keys.H:
                return (shift) ? 'H' : 'h';
            case Input.Keys.I:
                return (shift) ? 'I' : 'i';
            case Input.Keys.J:
                return (shift) ? 'J' : 'j';
            case Input.Keys.K:
                return (shift) ? 'K' : 'k';
            case Input.Keys.L:
                return (shift) ? 'L' : 'l';
            case Input.Keys.M:
                return (shift) ? 'M' : 'm';
            case Input.Keys.N:
                return (shift) ? 'N' : 'n';
            case Input.Keys.O:
                return (shift) ? 'O' : 'o';
            case Input.Keys.P:
                return (shift) ? 'P' : 'p';
            case Input.Keys.Q:
                return (shift) ? 'Q' : 'q';
            case Input.Keys.R:
                return (shift) ? 'R' : 'r';
            case Input.Keys.S:
                return (shift) ? 'S' : 's';
            case Input.Keys.T:
                return (shift) ? 'T' : 't';
            case Input.Keys.U:
                return (shift) ? 'U' : 'u';
            case Input.Keys.V:
                return (shift) ? 'V' : 'v';
            case Input.Keys.W:
                return (shift) ? 'W' : 'w';
            case Input.Keys.X:
                return (shift) ? 'X' : 'x';
            case Input.Keys.Y:
                return (shift) ? 'Y' : 'y';
            case Input.Keys.Z:
                return (shift) ? 'Z' : 'z';
            case Input.Keys.COMMA:
                return (shift) ? '<' : ',';
            case Input.Keys.PERIOD:
                return (shift) ? '>' :'.';
            case Input.Keys.TAB:
                return TAB;
            case Input.Keys.SPACE:
                return ' ';
            case Input.Keys.ENTER:
                return ENTER;
            case Input.Keys.BACKSPACE:
                return BACKSPACE; // also DEL
            case Input.Keys.GRAVE:
                return (shift) ? '~' : '`';
            case Input.Keys.MINUS:
                return (shift) ? '_' : '-';
            case Input.Keys.EQUALS:
                return (shift) ? '+' :'=';
            case Input.Keys.LEFT_BRACKET:
                return (shift) ? '{' :'[';
            case Input.Keys.RIGHT_BRACKET:
                return (shift) ? '}' :']';
            case Input.Keys.BACKSLASH:
                return (shift) ? '|' :'\\';
            case Input.Keys.SEMICOLON:
                return (shift) ? ':' :';';
            case Input.Keys.APOSTROPHE:
                return (shift) ? '"' :'\'';
            case Input.Keys.SLASH:
                return (shift) ? '?' :'/';
            case Input.Keys.AT:
                return '@';
            case Input.Keys.PAGE_UP:
                return PAGE_UP;
            case Input.Keys.PAGE_DOWN:
                return PAGE_DOWN;
            case Input.Keys.BUTTON_A:
                return GAMEPAD_A;
            case Input.Keys.BUTTON_B:
                return GAMEPAD_B;
            case Input.Keys.BUTTON_C:
                return GAMEPAD_C;
            case Input.Keys.BUTTON_X:
                return GAMEPAD_X;
            case Input.Keys.BUTTON_Y:
                return GAMEPAD_Y;
            case Input.Keys.BUTTON_Z:
                return GAMEPAD_Z;
            case Input.Keys.BUTTON_L1:
                return GAMEPAD_L1;
            case Input.Keys.BUTTON_R1:
                return GAMEPAD_R1;
            case Input.Keys.BUTTON_L2:
                return GAMEPAD_L2;
            case Input.Keys.BUTTON_R2:
                return GAMEPAD_R2;
            case Input.Keys.BUTTON_THUMBL:
                return GAMEPAD_LEFT_THUMB;
            case Input.Keys.BUTTON_THUMBR:
                return GAMEPAD_RIGHT_THUMB;
            case Input.Keys.BUTTON_START:
                return GAMEPAD_START;
            case Input.Keys.BUTTON_SELECT:
                return GAMEPAD_SELECT;
            case Input.Keys.INSERT:
                return INSERT;

            case Input.Keys.F1:
                return F1;
            case Input.Keys.F2:
                return F2;
            case Input.Keys.F3:
                return F3;
            case Input.Keys.F4:
                return F4;
            case Input.Keys.F5:
                return F5;
            case Input.Keys.F6:
                return F6;
            case Input.Keys.F7:
                return F7;
            case Input.Keys.F8:
                return F8;
            case Input.Keys.F9:
                return F9;
            case Input.Keys.F10:
                return F10;
            case Input.Keys.F11:
                return F11;
            case Input.Keys.F12:
                return F12;
            default:
                return '\0';
        }

    }

    /**
     * Left arrow key. If numpadDirections is enabled, this will also be sent by Numpad 4.
     */
    public static final char LEFT_ARROW = '\u2190';
    /**
     * Up arrow key. If numpadDirections is enabled, this will also be sent by Numpad 8.
     */
    public static final char UP_ARROW = '\u2191';
    /**
     * Down arrow key. If numpadDirections is enabled, this will also be sent by Numpad 6.
     */
    public static final char RIGHT_ARROW = '\u2192';
    /**
     * Down arrow key. If numpadDirections is enabled, this will also be sent by Numpad 2.
     */
    public static final char DOWN_ARROW = '\u2193';
    /**
     * Not typically a dedicated key, but if numpadDirections is enabled, this will be sent by Numpad 1.
     */
    public static final char DOWN_LEFT_ARROW = '\u2199';
    /**
     * Not typically a dedicated key, but if numpadDirections is enabled, this will be sent by Numpad 3.
     */
    public static final char DOWN_RIGHT_ARROW = '\u2198';
    /**
     * Not typically a dedicated key, but if numpadDirections is enabled, this will be sent by Numpad 9.
     */
    public static final char UP_RIGHT_ARROW = '\u2197';
    /**
     * Not typically a dedicated key, but if numpadDirections is enabled, this will be sent by Numpad 7.
     */
    public static final char UP_LEFT_ARROW = '\u2196';
    /**
     * Not typically a dedicated key, but if numpadDirections is enabled, this will be sent by Numpad 5.
     */
    public static final char CENTER_ARROW = '\u21BA';
    /**
     * Not typically a dedicated key, but if numpadDirections is enabled, this will be sent by Numpad 0.
     *
     * Intended for games that might need up a jump or crouch button on the numpad that supplants movement.
     */
    public static final char VERTICAL_ARROW = '\u2195';
    /**
     * Enter key, also called Return key. Used to start a new line of text or confirm entries in forms.
     */
    public static final char ENTER = '\u21B5';
    /**
     * Tab key. Used for entering horizontal spacing, such as indentation, but also often to cycle between menu items.
     */
    public static final char TAB = '\u21B9';
    /**
     * Backspace key on most PC keyboards; Delete key on Mac keyboards. Used to delete the previous character.
     */
    public static final char BACKSPACE = '\u2280';
    /**
     * Delete key on most PC keyboards; no equivalent on some (all?) Mac keyboards. Used to delete the next character.
     *
     * Not present on some laptop keyboards and some (all?) Mac keyboards.
     */
    public static final char FORWARD_DELETE = '\u2281';
    /**
     * Insert key. Not recommended for common use because it could affect other application behavior.
     *
     * Not present on some laptop keyboards.
     */
    public static final char INSERT = '\u2208';
    /**
     * Page Down key.
     *
     * Not present on some laptop keyboards.
     */
    public static final char PAGE_DOWN = '\u22A4';
    /**
     * Page Up key.
     *
     * Not present on some laptop keyboards.
     */
    public static final char PAGE_UP = '\u22A5';
    /**
     * Home key (commonly used for moving a cursor to start of line).
     *
     * Not present on some laptop keyboards.
     */
    public static final char HOME = '\u2302';
    /**
     * End key (commonly used for moving a cursor to end of line).
     *
     * Not present on some laptop keyboards.
     */
    public static final char END = '\u2623';
    /**
     * Esc or Escape key
     */
    public static final char ESCAPE = '\u2620';

    /**
     * Function key F1
     */
    public static final char F1 = '\u2460';
    /**
     * Function key F2
     */
    public static final char F2 = '\u2461';
    /**
     * Function key F3
     */
    public static final char F3 = '\u2462';
    /**
     * Function key F4
     */
    public static final char F4 = '\u2463';
    /**
     * Function key F5
     */
    public static final char F5 = '\u2464';
    /**
     * Function key F6
     */
    public static final char F6 = '\u2465';
    /**
     * Function key F7
     */
    public static final char F7 = '\u2466';
    /**
     * Function key F8
     */
    public static final char F8 = '\u2467';
    /**
     * Function key F9
     */
    public static final char F9 = '\u2468';
    /**
     * Function key F10
     */
    public static final char F10 = '\u2469';
    /**
     * Function key F11
     */
    public static final char F11 = '\u246A';
    /**
     * Function key F12
     */
    public static final char F12 = '\u246B';

    /**
     * Gamepad A button.
     */
    public static final char GAMEPAD_A = '\u24b6';
    /**
     * Gamepad B button.
     */
    public static final char GAMEPAD_B = '\u24b7';
    /**
     * Gamepad C button.
     */
    public static final char GAMEPAD_C = '\u24b8';
    /**
     * Gamepad X button.
     */
    public static final char GAMEPAD_X = '\u24cd';
    /**
     * Gamepad Y button.
     */
    public static final char GAMEPAD_Y = '\u24ce';
    /**
     * Gamepad Z button.
     */
    public static final char GAMEPAD_Z = '\u24cf';

    /**
     * Gamepad L1 button.
     */
    public static final char GAMEPAD_L1 = '\u24c1';
    /**
     * Gamepad L2 button.
     */
    public static final char GAMEPAD_L2 = '\u24db';
    /**
     * Gamepad R1 button.
     */
    public static final char GAMEPAD_R1 = '\u24c7';
    /**
     * Gamepad R2 button.
     */
    public static final char GAMEPAD_R2 = '\u24e1';
    /**
     * Gamepad Left Thumb button.
     */
    public static final char GAMEPAD_LEFT_THUMB = '\u24a7';
    /**
     * Gamepad Right Thumb button.
     */
    public static final char GAMEPAD_RIGHT_THUMB = '\u24ad';
    /**
     * Gamepad Start button.
     */
    public static final char GAMEPAD_START = '\u2713';
    /**
     * Gamepad Select button.
     */
    public static final char GAMEPAD_SELECT = '\u261C';


}
