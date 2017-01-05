package squidpony.squidgrid.mapping.locks;

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
     * @return whether the symbol is one of the special SWITCH_{ON,OFF} symbols
     */
    public static boolean isSwitchState(final int value) {
        return value == SWITCH_ON || value == SWITCH_OFF;
    }
    
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
        else if (value >= 0 && value < 26)
            return Character.toString((char)((int)'A' + value));
        else
            return Integer.toString(value);

    }
    
}
