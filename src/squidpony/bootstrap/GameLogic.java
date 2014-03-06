package squidpony.bootstrap;

import squidpony.annotation.Beta;
import squidpony.squidgrid.util.Direction;

/**
 * This interface indicates the ability to perform the game logic associated with GUI input.
 * Intended for use with the bootsrap SFrame.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface GameLogic {

    /**
     * Sends a single character that was pressed. Accounts for the shift key state.
     *
     * Will not send a character for arrow keys or numbers on the numpad as those are sent through
     * the acceptDirectionInput() method.
     *
     * @param key the key that was pressed
     */
    public void acceptKeyboardInput(char key);

    /**
     * Sends a single direction that was pressed. This is from the arrow keys and the numpad arrow
     * keys. Ignores numpad lock state so number mode will still return directions.
     *
     * @param dir
     */
    public void acceptDirectionInput(Direction dir);

    /**
     * Sends the grid coordinates of a mouse click. Will not send clicks that originated in the
     * message and stat panels.
     *
     * @param x
     * @param y
     */
    public void acceptMoustInput(int x, int y);

    /**
     * Called once the GUI is completely displayed and ready for input.
     *
     * The initial visual representation of the game should be created here. After this all visual
     * updates will need to be in response to mouse or keyboard input.
     */
    public void beginGame();
}
