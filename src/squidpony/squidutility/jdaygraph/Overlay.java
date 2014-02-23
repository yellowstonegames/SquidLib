package squidpony.squidutility.jdaygraph;

import squidpony.annotation.Beta;

/**
 * Utility class that stores a graph and performs calculations on it.
 *
 * Adapted from posts at http://paleoludic.com/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class Overlay {

    private final JDayCell cells[];

    public Overlay(Topology topology, JDayCell cellType) {
        cells = new JDayCell[topology.size()];
    }

}
