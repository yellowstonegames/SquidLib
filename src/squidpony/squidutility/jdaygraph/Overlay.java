package squidpony.squidutility.jdaygraph;

import squidpony.annotation.Beta;

/**
 * Describes a geometric area in a way useful to the Overlay class.
 *
 * The T parameter indicates what kind of cells are to be held in the specific topology. The C
 * parameter indicates what types of traversal are possible.
 *
 * Implementations should document individually the meaning of return values for cost functions
 * (such as no-path values) and the optimality or uniqueness of returned paths.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface Overlay<T, C extends Topology> {

    /**
     * Returns the number of nodes in the topology.
     *
     * @return
     */
    public int size();

    /**
     * Returns the index appropriate for the given object.
     *
     * @param t
     * @return
     */
    public int indexOf(T t);

    /**
     * Returns the object at the given index.
     *
     * @param index
     * @return
     */
    public T at(int index);

    /**
     * Returns a list of all the neighbors' indexes of the object at the passed in index.
     *
     * @param index
     * @return
     */
    public int[] neighbors(int index);

    /**
     * Returns the index of the neighbor reached from the given index with the given traversal.
     *
     * @param index
     * @param traversal
     * @return
     */
    public int neighbor(int index, C traversal);

    /**
     * The cost of traversing out of this index. This value only makes sense if all traversals have
     * the same cost.
     *
     * @param index
     * @return
     */
    public float traversalCost(int index);

    /**
     * Returns the cost of traversing in the given manner from the given index.
     *
     * @param index
     * @param traversal
     * @return
     */
    public float traversalCost(int index, C traversal);

}
