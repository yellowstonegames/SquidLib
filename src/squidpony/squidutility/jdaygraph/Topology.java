package squidpony.squidutility.jdaygraph;

import java.awt.Point;
import squidpony.annotation.Beta;

/**
 * Describes a geometric area in a way useful to the Overlay class.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface Topology {

    /**
     * Returns the number of nodes in the topology.
     *
     * @return
     */
    public int size();

    /**
     * Returns the index appropriate for the given point.
     *
     * @param p
     * @return
     */
    public int getIndex(Point p);

    /**
     * Returns the Point that should match the given index.
     *
     * @param index
     * @return
     */
    public Point getCoordinate(int index);

}
