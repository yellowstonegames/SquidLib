package squidpony.squidgrid.util;

/**
 * Represents the eight grid directions and the deltaX, deltaY values associated
 * with those directions.
 *
 * The grid referenced has x positive to the right and y positive downwards.
 *
 * @author Eben Howard - http://squidpony.com
 */
public enum Direction {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0),
    UP_LEFT(-1, -1), UP_RIGHT(1, -1), DOWN_LEFT(-1, 1), DOWN_RIGHT(1, 1),
    NONE(0, 0);
    
    /**
     * An array which holds only the four cardinal directions.
     */
    public static final Direction[] cardinals = new Direction[]{UP, DOWN, LEFT, RIGHT};
    
    /**
     * The x coordinate difference for this direction.
     */
    public int deltaX;
    
    /**
     * The y coordinate difference for this direction.
     */
    public int deltaY;

    private Direction(int x, int y) {
        this.deltaX = x;
        this.deltaY = y;
    }
}
