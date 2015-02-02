package squidpony.squidgrid;

/**
 * Represents the eight grid directions and the deltaX, deltaY values associated
 * with those directions.
 *
 * The grid referenced has x positive to the right and y positive downwards on
 * screen.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Direction {

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
     * This can be used to get the primary magnitude intercardinal direction
     * from an origin point to an event point, such as a mouse click on a grid.
     *
     * If the point given is exactly on a boundary between directions then the
     * direction clockwise is returned.
     *
     * @param x
     * @param y
     * @return
     */
    static public Direction getDirection(int x, int y) {
        if (x == 0 && y == 0) {
            return NONE;
        }

        double angle = Math.atan2(y, x);
        double degree = Math.toDegrees(angle);
        degree += 450;//rotate to all positive and 0 is up
        degree %= 360;//normalize
        if (degree < 22.5) {
            return UP;
        } else if (degree < 67.5) {
            return UP_RIGHT;
        } else if (degree < 112.5) {
            return RIGHT;
        } else if (degree < 157.5) {
            return DOWN_RIGHT;
        } else if (degree < 202.5) {
            return DOWN;
        } else if (degree < 247.5) {
            return DOWN_LEFT;
        } else if (degree < 292.5) {
            return LEFT;
        } else if (degree < 337.5) {
            return UP_LEFT;
        } else {
            return UP;
        }
    }

    /**
     * Returns the direction that most closely matches the input.
     *
     * This can be used to get the primary magnitude cardinal direction from an
     * origin point to an event point, such as a mouse click on a grid.
     *
     * If the point given is directly diagonal then the direction clockwise is
     * returned.
     *
     * @param x
     * @param y
     * @return
     */
    static public Direction getCardinalDirection(int x, int y) {
        if (x == 0 && y == 0) {
            return NONE;
        }

        int absx = Math.abs(x);

        if (y > absx) {
            return UP;
        }

        int absy = Math.abs(y);

        if (absy > absx) {
            return DOWN;
        }

        if (x > 0) {
            if (-y == x) {//on diagonal
                return DOWN;
            }
            return RIGHT;
        }

        if (y == x) {//on diagonal
            return UP;
        }
        return LEFT;

    }

    /**
     * Returns the Direction one step clockwise including diagonals.
     *
     * If considering only Cardinal directions, calling this twice will get the
     * next clockwise cardinal direction.
     *
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
     * If considering only Cardinal directions, calling this twice will get the
     * next counterclockwise cardinal direction.
     *
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
