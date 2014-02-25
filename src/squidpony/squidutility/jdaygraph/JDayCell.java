package squidpony.squidutility.jdaygraph;

import squidpony.annotation.Beta;

/**
 * This interface defines the methods needed to be able to do generic topology calculations.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface JDayCell<T extends CellTraversal> {

    /**
     * Returns the cost to traverse this cell. This is only accurate if all traversals have the same cost.
     *
     * @return
     */
    public float traversalCost();

    /**
     * Returns the cost to traverse this cell using the provided traversal.
     *
     * @param traversal
     * @return
     */
    public float traversalCost(T traversal);
}
