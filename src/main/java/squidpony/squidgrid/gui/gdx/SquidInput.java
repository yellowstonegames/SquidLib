package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.IntArray;

/**
 * This input processing class can handle mouse and keyboard input, using a squidpony.squidgrid.gui.gdx.SquidMouse for
 * Mouse input and a user implementation of the SquidInput.KeyHandler interface to react to keys represented as chars
 * and the modifiers those keys were pressed with, any of alt, ctrl, and/or shift. Not all keys are representable by
 * default in unicode, so symbolic representations are stored in constants in this class, and are passed to
 * KeyHandler.handle() as chars like DOWN_ARROW or its value, '\u2193'. Shift modifies the input as it would on a
 * QWERTY keyboard, and the exact mapping is documented in fromKey() as well. This class handles mouse input
 * immediately, but stores keypresses in a queue, storing all key events and allowing them to be processed one at a time
 * using next() or all at once using drain(). To have an effect, it needs to be registered by calling
 * Input.setInputProcessor(SquidInput).
 *
 * It does not perform the blocking functionality of the non-GDX SquidKey implementation, because this is meant to run
 * in an event-driven libGDX game and should not step on the toes of libGDX's input handling. To block game logic
 * until an event has been received, check hasNext() in the game's render() method and effectively "block" by not
 * running game logic if hasNext() returns false. You can process an event if hasNext() returns true by calling next().
 * Mouse inputs do not affect hasNext(), and next() will process only key pressed events.
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
         *
         * This can react to the input in whatever way you find appropriate for your game.
         * @param key a char of the "typed" representation of the key, such as 'a' or 'E', or if there is no Unicode
         *            character for the key, an appropriate alternate character as documented in SquidInput.fromKey()
         * @param alt true if the Alt modifier was being held while this key was entered, false otherwise.
         * @param ctrl true if the Ctrl modifier was being held while this key was entered, false otherwise.
         * @param shift true if the Shift modifier was being held while this key was entered, false otherwise.
         */
        void handle(char key, boolean alt, boolean ctrl, boolean shift);
    }

    protected java.awt.Toolkit tk;
    protected KeyHandler keyAction;
    protected boolean numpadDirections = true, ignoreInput = false;
    protected SquidMouse mouse;
    protected final CharArray queue = new CharArray();
    protected final CharArray processingQueue = new CharArray();

    /**
     * Constructs a new SquidInput that does not respond to keyboard or mouse input. These can be set later by calling
     * setKeyHandler() to allow keyboard handling or setMouse() to allow mouse handling on a grid.
     */
    public SquidInput() {
        tk = java.awt.Toolkit.getDefaultToolkit();
        keyAction = null;
        this.mouse = new SquidMouse(12, 12, new InputAdapter());
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
        tk = java.awt.Toolkit.getDefaultToolkit();
        keyAction = null;
        this.mouse = mouse;
    }


    /**
     * Constructs a new SquidInput that does not respond to mouse input, but does take keyboard input and sends keyboard
     * events through some processing before calling keyHandler.handle() on keypresses that can sensibly be processed.
     * Modifier keys do not go through the same processing but are checked for their current state when the key is
     * pressed, and the states of alt, ctrl, and shift (respecting caps lock) are passed to keyHandler.handle() as well.
     * You can use setMouse() to allow mouse handling or change the KeyHandler with setKeyHandler().
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     */
    public SquidInput(KeyHandler keyHandler) {
        tk = java.awt.Toolkit.getDefaultToolkit();
        keyAction = keyHandler;
        mouse = new SquidMouse(12, 12, new InputAdapter());
    }
    /**
     * Constructs a new SquidInput that does not respond to mouse input, but does take keyboard input and sends keyboard
     * events through some processing before calling keyHandler.handle() on keypresses that can sensibly be processed.
     * Modifier keys do not go through the same processing but are checked for their current state when the key is
     * pressed, and the states of alt, ctrl, and shift (respecting caps lock) are passed to keyHandler.handle() as well.
     * You can use setMouse() to allow mouse handling or change the KeyHandler with setKeyHandler().
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     * @param ignoreInput true if this should ignore input initially, false if it should process input normally.
     */
    public SquidInput(KeyHandler keyHandler, boolean ignoreInput) {
        tk = java.awt.Toolkit.getDefaultToolkit();
        keyAction = keyHandler;
        mouse = new SquidMouse(12, 12, new InputAdapter());
        this.ignoreInput = ignoreInput;
    }
    /**
     * Constructs a new SquidInput that responds to mouse and keyboard input when given a SquidMouse and a
     * SquidInput.KeyHandler implementation. It sends keyboard events through some processing before calling
     * keyHandler.handle() on keypresses that can sensibly be processed. Modifier keys do not go through the same
     * processing but are checked for their current state when the key is pressed, and the states of alt, ctrl, and
     * shift (respecting caps lock) are passed to keyHandler.handle() as well. The SquidMouse, even though it is an
     * InputProcessor on its own, should not be registered by calling Input.setInputProcessor(SquidMouse), and instead
     * this object should be registered by calling Input.setInputProcessor(SquidInput). You can use setKeyHandler() or
     * setMouse() to change keyboard or mouse handling.
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     * @param mouse a SquidMouse instance that will be used for handling mouse input. Must not be null.
     */
    public SquidInput(KeyHandler keyHandler, SquidMouse mouse) {
        tk = java.awt.Toolkit.getDefaultToolkit();
        keyAction = keyHandler;
        this.mouse = mouse;
    }

    /**
     * Constructs a new SquidInput that responds to mouse and keyboard input when given a SquidMouse and a
     * SquidInput.KeyHandler implementation, and can be put in an initial state where it ignores input until told
     * otherwise via setIgnoreInput(boolean). It sends keyboard events through some processing before calling
     * keyHandler.handle() on keypresses that can sensibly be processed. Modifier keys do not go through the same
     * processing but are checked for their current state when the key is pressed, and the states of alt, ctrl, and
     * shift (respecting caps lock) are passed to keyHandler.handle() as well. The SquidMouse, even though it is an
     * InputProcessor on its own, should not be registered by calling Input.setInputProcessor(SquidMouse), and instead
     * this object should be registered by calling Input.setInputProcessor(SquidInput). You can use setKeyHandler() or
     * setMouse() to change keyboard or mouse handling.
     * @param keyHandler must implement the SquidInput.KeyHandler interface so it can handle() key input.
     * @param mouse a SquidMouse instance that will be used for handling mouse input. Must not be null.
     * @param ignoreInput true if this should ignore input initially, false if it should process input normally.
     */
    public SquidInput(KeyHandler keyHandler, SquidMouse mouse, boolean ignoreInput) {
        tk = java.awt.Toolkit.getDefaultToolkit();
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
        this.numpadDirections = using;
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
     * Processes all events queued up, passing them through this object's key processing and then to keyHandler. Mouse
     * events are not queued and are processed when they come in.
     */
    public void drain () {
        CharArray q = processingQueue;
        synchronized (this) {
            if (keyAction == null || queue.size < 2) {
                queue.clear();
                return;
            }
            q.addAll(queue);
            queue.clear();
        }
        for (int i = 0, n = q.size; i < n;)
        {
            char c = q.get(i++), mods = q.get(i++);
            keyAction.handle(c, (mods & 1) != 0, (mods & 2) != 0, (mods & 4) != 0);
        }
            /**
             case KEY_UP:
             keyProcessor.keyUp(q.get(i++));
             break;
             */

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
     * Processes the first key event queued up, passing it to this object's InputProcessor. Mouse events are not
     * queued and are processed when they come in.
     */
    public void next()
    {
        CharArray q = processingQueue;
        synchronized (this) {
            if (keyAction == null || queue.size < 2) {
                queue.clear();
                return;
            }
            q.addAll(queue, 0, 2);
            queue.removeRange(0, 1);
        }
        if(q.size >= 2)
        {
            char c = q.get(0), mods = q.get(1);
            keyAction.handle(c, (mods & 1) != 0, (mods & 2) != 0, (mods & 4) != 0);
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
        boolean alt = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT),
                ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT),
                // affected by caps lock. AWT uses 20 as the code for caps lock, LWJGL (so libGDX too) doesn't have a code.
                shift = tk.getLockingKeyState(20) ^ (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
        char c = fromCode(keycode, shift);
        char mods = 0;
        if(c != '\0') {
            queue.add(c);
            mods |= (alt) ? 1 : 0;
            mods |= (ctrl) ? 2 : 0;
            mods |= (shift) ? 4 : 0;
            queue.add(mods);
        }
        return false;
    }

    public synchronized boolean keyUp (int keycode) {
//        queue.add(KEY_UP);
//        queue.add(keycode);
        return false;
    }

    public synchronized boolean keyTyped (char character) {
        return false;
    }

    public synchronized boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if(ignoreInput) return false;
        mouse.touchDown(screenX, screenY, pointer, button);
        return false;
    }

    public synchronized boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if(ignoreInput) return false;
        mouse.touchUp(screenX, screenY, pointer, button);
        return false;
    }

    public synchronized boolean touchDragged (int screenX, int screenY, int pointer) {
        if(ignoreInput) return false;
        mouse.touchDragged(screenX, screenY, pointer);
        return false;
    }

    public synchronized boolean mouseMoved (int screenX, int screenY) {
        if(ignoreInput) return false;
        mouse.mouseMoved(screenX, screenY);
        return false;
    }

    public synchronized boolean scrolled (int amount) {
        if(ignoreInput) return false;
        mouse.scrolled(amount);
        return false;
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
     * Keys 'a' to 'z' report 'A' to 'Z' when shift is held. Non-Latin characters are not supported, since most
     * keyboards would be unable to send keys for a particular language and A-Z are very common.
     *
     * Top row numbers map as follows:
     *
     * '1' to '!', '2' to '@', '3' to '#', '4' to '$', '5' to '%',
     * '6' to '^', '7' to '&amp;', '8' to '*', '9' to '(', '0' to ')'
     *
     * Numpad numbers will report a SquidInput constant such as UP_LEFT_ARROW for Numpad 7, but only if numpadDirections
     * is true; otherwise they send the number (here, 7). Numpad 0 sends VERTICAL_ARROW or 0.
     *
     * Most symbol keys are mapped to a single unicode char as a constant in SquidInput and disregard Shift. The
     * constant is usually the same as the name of the char; possible exceptions are Backspace (on PC) or Delete (on
     * Mac) mapping to BACKSPACE, Delete (on PC) mapping to FORWARD_DELETE, Esc mapping to ESCAPE, and Enter (on PC) or
     * Return (on Mac) mapping to ENTER.
     *
     * Caps Lock and Num Lock do not map to anything if pressed, but Caps Lock affects the Shift key's status when a
     * keypress is handled, as would be expected. Num Lock is tricky and has not been tested for its effects on
     * different keyboards regarding the numpad; assume it should be on to get any input from the numpad.
     *
     * ':', '*', '#', '@', and space keys, if present, always map to themselves, regardless of Shift.
     *
     * Other characters map as follows when Shift is held, as they would on a QWERTY keyboard:
     *
     * ',' to '&lt;'
     *
     * '.' to '&gt;'
     *
     * '/' to '?'
     *
     * ';' to ':'
     *
     * '\'' to '&quot;'
     *
     * '[' to '{'
     *
     * ']' to '}'
     *
     * '|' to '\\'
     *
     * '-' to '_'
     *
     * '+' to '='
     *
     * '`' to '~'
     *
     * @param keycode a keycode as passed by LibGDX
     * @param shift true if Shift key is being held. Caps Lock inverts shift before it gets here.
     * @return
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
