package squidpony.squidgrid.util;

import squidpony.squidutility.jdaygraph.Topology;

/**
 * Represents the eight grid directions and the deltaX, deltaY values associated with those
 * directions.
 *
 * The grid referenced has x positive to the right and y positive downwards on screen.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Direction implements Topology {

    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0), UP_LEFT(-1, -1), UP_RIGHT(1, -1), DOWN_LEFT(-1, 1), DOWN_RIGHT(1, 1), NONE(0, 0);
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
    public final int deltaX;
    /**
     * The y coordinate difference for this direction.
     */
    public final int deltaY;

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

    /**
     * Returns the Direction one step clockwise including diagonals.
     *
     * @param dir
     * @return
     */
    public Direction clockwise() {
        switch (this) {
            case UP:
                return UP_RIGHT;
            case DOWN:
                return DOWN_LEFT;
            case LEFT:
                return UP_LEFT;
            case RIGHT:
                return DOWN_RIGHT;
            case UP_LEFT:
                return UP;
            case UP_RIGHT:
                return RIGHT;
            case DOWN_LEFT:
                return LEFT;
            case DOWN_RIGHT:
                return DOWN;
            case NONE:
            default:
                return NONE;
        }
    }

    /**
     * Returns the Direction one step counterclockwise including diagonals.
     *
     * @param dir
     * @return
     */
    public Direction counterClockwise() {
        switch (this) {
            case UP:
                return UP_LEFT;
            case DOWN:
                return DOWN_RIGHT;
            case LEFT:
                return DOWN_LEFT;
            case RIGHT:
                return UP_RIGHT;
            case UP_LEFT:
                return LEFT;
            case UP_RIGHT:
                return UP;
            case DOWN_LEFT:
                return DOWN;
            case DOWN_RIGHT:
                return RIGHT;
            case NONE:
            default:
                return NONE;
        }
    }

    /**
     * Returns the direction directly opposite of this one.
     *
     * @return
     */
    public Direction opposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case UP_LEFT:
                return DOWN_RIGHT;
            case UP_RIGHT:
                return DOWN_LEFT;
            case DOWN_LEFT:
                return UP_RIGHT;
            case DOWN_RIGHT:
                return UP_LEFT;
            case NONE:
            default:
                return NONE;
        }
    }

    private Direction(int x, int y) {
        this.deltaX = x;
        this.deltaY = y;
    }
}
