package squidpony.squidgrid.mapping.locks;

import squidpony.ArrayTools;

/**
 * Represents a single key or lock within the lock-and-key puzzle.
 * <p>
 * Each Symbol has a 'value'. Two Symbols are equivalent iff they have the same
 * 'value'.
 */
public class Symbol {
    /**
     * Symbol map with special meanings.
     * <p>
     * Certain items (such as START, GOAL, BOSS) serve no purpose in the puzzle
     * other than as markers for the client of the library to place special game
     * objects.
     * <p>
     * The SWITCH_ON and SWITCH_OFF symbols do not appear in rooms, only in
     * {@link Condition}s and {@link Edge}s.
     * <p>
     * The SWITCH item does not give the player the SWITCH symbol, instead the
     * player may choose to either
     * <ul>
     * <li>lose the SWITCH_OFF symbol (if they have it), and gain the SWITCH_ON
     *      symbol; or
     * <li>lose the SWITCH_ON symbol (if they have it), and gain the SWITCH_OFF
     *      symbol.
     * <ul>
     * <p>
     */
    public static final int
            NOTHING = Integer.MIN_VALUE,
            START = -1,
            GOAL = -2,
            BOSS = -3,
            SWITCH_ON = -4,     // used as a condition (lock)
            SWITCH_OFF = -5,    // used as a condition (lock)
            SWITCH = -6;        // used as an item (key) within a room
    
    /**
     * @return whether the symbol is the special START symbol
     */
    public static boolean isStart(final int value) {
        return value == START;
    }
    
    /**
     * @return whether the symbol is the special GOAL symbol
     */
    public static boolean isGoal(final int value) {
        return value == GOAL;
    }
    
    /**
     * @return whether the symbol is the special BOSS symbol
     */
    public static boolean isBoss(final int value) {
        return value == BOSS;
    }

    /**
     * @return whether the symbol is the special SWITCH symbol
     */
    public static boolean isSwitch(final int value) {
        return value == SWITCH;
    }

    /**
     * @return whether the symbol is the special NOTHING symbol
     */
    public static boolean isNothing(final int value) {
        return value == NOTHING;
    }

    /**
     * @return whether the symbol is one of the special SWITCH_{ON,OFF} symbols
     */
    public static boolean isSwitchState(final int value) {
        return value == SWITCH_ON || value == SWITCH_OFF;
    }

    /**
     * Like {@link #toString(int)}, but returns one char instead, sometimes using some roguelike map conventions:
     * <ul>
     *     <li>Symbol.START maps to {@code '<'}</li>
     *     <li>Symbol.GOAL maps to {@code '>'}</li>
     *     <li>Symbol.BOSS maps to {@code '!'}</li>
     *     <li>Symbol.SWITCH maps to {@code '&'}</li>
     *     <li>Symbol.SWITCH_ON maps to {@code '1'}</li>
     *     <li>Symbol.SWITCH_OFF maps to {@code '0'}</li>
     *     <li>Symbol.NOTHING maps to {@code ' '}</li>
     *     <li>Any number between 0 and 255 inclusive is mapped to a letter using {@link ArrayTools#letterAt(int)}</li>
     *     <li>Anything else maps to {@code '*'}</li>
     * </ul>
     * @param value a symbol int that should be less than 256, and if negative should equal one of the Symbol constants
     * @return a single char that can be used to identify value
     */
    public static char asChar(final int value) {
        if (value == START)
            return '<';
        else if (value == GOAL)
            return '>';
        else if (value == BOSS)
            return '!';
        else if (value == SWITCH_ON)
            return '1';
        else if (value == SWITCH_OFF)
            return '0';
        else if (value == SWITCH)
            return '&';
        else if (value == NOTHING)
            return ' ';
        else if (value >= 0 && value < 256)
            return ArrayTools.letterAt(value);
        else
            return '*';
    }

    /**
     * Gets a printable String representation of the int (the parameter value) that represents a symbol.
     * START, GOAL, and BOSS map to "Start", "Goal", and "Boss", respectively. SWITCH, SWITCH_ON, and SWITCH_OFF map to
     * "SW", "ON", and "OFF", respectively. NOTHING maps to "NO". Any number between 0 and 255 inclusive is mapped to a
     * letter using {@link ArrayTools#letterAt(int)}, and that becomes the returned String. Any other number is simply
     * printed as a normal integer.
     * @param value a symbol int that should be less than 256, and if negative should equal one of the Symbol constants
     * @return a printable String that should match the meaning of value
     */
    public static String toString(final int value) {
        if (value == START)
            return "Start";
        else if (value == GOAL)
            return "Goal";
        else if (value == BOSS)
            return "Boss";
        else if (value == SWITCH_ON)
            return "ON";
        else if (value == SWITCH_OFF)
            return "OFF";
        else if (value == SWITCH)
            return "SW";
        else if (value == NOTHING)
            return "NO";
        else if (value >= 0 && value < 256)
            return String.valueOf(ArrayTools.letterAt(value));
        else
            return Integer.toString(value);
    }

}
