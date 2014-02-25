package squidpony.squidutility.jdaygraph;

import squidpony.squidgrid.util.Direction;

/**
 * This is a cell that can be traversed in only the four cardinal directions.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Grid4Cell implements JDayCell<Direction> {

    @Override
    public float traversalCost() {
        return 1f;
    }

    /**
     * Returns 0 for no movement, 1 for movement in the cardinal directions, and
     * Float.POSITIVE_INFINITY for non-valid direcitons.
     *
     * @param traversal
     * @return
     */
    @Override
    public float traversalCost(Direction traversal) {
        switch (traversal) {
            case NONE:
                return 0f;
            case UP:
            case LEFT:
            case RIGHT:
            case DOWN:
                return 1f;
            default:
                return Float.POSITIVE_INFINITY;
        }
    }

}
