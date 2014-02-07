package squidpony.squidgrid.util;

import squidpony.annotation.Beta;

/**
 * Represents the eight grid directions and the deltaX, deltaY, and deltaZ
 * values associated with those directions.
 *
 * The grid referenced has x positive to the right and y positive downwards on
 * screen with z positive out of the screen away from the viewer. Plus is away
 * from the user in the z axis while Minus is towards the user.
 *
 * This class is in Beta status because not all rotation methods are fully
 * functional.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public enum Direction3D {

    NONE(0, 0, 0),
    UP(0, -1, 0), DOWN(0, 1, 0), LEFT(-1, 0, 0), RIGHT(1, 0, 0),
    UP_LEFT(-1, -1, 0), UP_RIGHT(1, -1, 0), DOWN_LEFT(-1, 1, 0),
    DOWN_RIGHT(1, 1, 0),
    //3D
    PLUS(0, 0, 1), MINUS(0, 0, -1),
    PLUS_UP(0, -1, 1), PLUS_DOWN(0, 1, 1), PLUS_LEFT(-1, 0, 1), PLUS_RIGHT(1, 0, 1),
    PLUS_UP_LEFT(-1, -1, 1), PLUS_UP_RIGHT(1, -1, 1), PLUS_DOWN_LEFT(-1, 1, 1),
    PLUS_DOWN_RIGHT(1, 1, 1),
    MINUS_UP(0, -1, -1), MINUS_DOWN(0, 1, -1), MINUS_LEFT(-1, 0, -1), MINUS_RIGHT(1, 0, -1),
    MINUS_UP_LEFT(-1, -1, -1), MINUS_UP_RIGHT(1, -1, -1), MINUS_DOWN_LEFT(-1, 1, -1),
    MINUS_DOWN_RIGHT(1, 1, -1),;
    /**
     * An array which holds only the six cardinal directions.
     */
    public static final Direction3D[] CARDINALS = {UP, DOWN, LEFT, RIGHT, PLUS, MINUS};
    /**
     * An array which holds only the twelve directions that are diagonal to two
     * axis, but not the third.
     */
    public static final Direction3D[] DIAGONALS = {UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT,
        PLUS_UP, PLUS_DOWN, PLUS_LEFT, PLUS_RIGHT,
        MINUS_UP, MINUS_DOWN, MINUS_LEFT, MINUS_RIGHT};
    /**
     * An array which holds only the eight angles which are diagonal to all
     * axis.
     */
    public static final Direction3D[] SUPER_DIAGONALS = {
        PLUS_UP_LEFT, PLUS_UP_RIGHT, PLUS_DOWN_LEFT, PLUS_DOWN_RIGHT,
        MINUS_UP_LEFT, MINUS_UP_RIGHT, MINUS_DOWN_LEFT, MINUS_DOWN_RIGHT};
    /**
     * The x coordinate difference for this direction.
     */
    public int deltaX;
    /**
     * The y coordinate difference for this direction.
     */
    public int deltaY;
    /**
     * The z coordinate difference for this direction.
     */
    public int deltaZ;

    /**
     * Returns the direction that most closely matches the input.
     *
     * @param x
     * @param y
     * @return
     */
    static public Direction3D getDirection(int x, int y) {
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
    public Direction3D clockwise() {
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
    public Direction3D counterClockwise() {
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
    public Direction3D opposite() {
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

    private Direction3D(int x, int y, int z) {
        this.deltaX = x;
        this.deltaY = y;
        this.deltaZ = z;
    }
}
