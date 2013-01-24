package squidpony.squidgrid.util;

/**
 * Represents the eight grid directions and the deltaX, deltaY values associated
 * with those directions.
 *
 * The grid referenced has x positive to the right and y positive downwards.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Direction {

    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0),
    UP_LEFT(-1, -1), UP_RIGHT(1, -1), DOWN_LEFT(-1, 1), DOWN_RIGHT(1, 1),
    NONE(0, 0);
    /**
     * An array which holds only the four cardinal directions.
     */
    public static final Direction[] CARDINALS = {UP, DOWN, LEFT, RIGHT};
    /**
     * An array which holds only the four diagonal directions.
     */
    public static final Direction[] DIAGONALS = {UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    /**
     * An array which holds all eight OUTWARDS directions.
     */
    public static final Direction[] OUTWARDS = {UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    /**
     * The x coordinate difference for this direction.
     */
    public int deltaX;
    /**
     * The y coordinate difference for this direction.
     */
    public int deltaY;

    /**
     * Returns the direction that most closely matches the input.
     *
     * @param x
     * @param y
     * @return
     */
    static public Direction getDirection(int x, int y) {
        if (x < 0) {
            if (y < 0) {
                return UP_LEFT;
            } else if (y == 0) {
                return LEFT;
            } else {
                return DOWN_LEFT;
            }
        } else if (x == 0) {
            if (y < 0) {
                return UP;
            } else if (y == 0) {
                return NONE;
            } else {
                return DOWN;
            }
        } else {
            if (y < 0) {
                return UP_RIGHT;
            } else if (y == 0) {
                return RIGHT;
            } else {
                return DOWN_RIGHT;
            }
        }
    }

    private Direction(int x, int y) {
        this.deltaX = x;
        this.deltaY = y;
    }
}
