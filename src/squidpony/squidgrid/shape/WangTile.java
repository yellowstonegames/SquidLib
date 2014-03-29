package squidpony.squidgrid.shape;

import squidpony.annotation.Beta;
import squidpony.squidgrid.util.DirectionIntercardinal;

/**
 * Represents a tile that is edge and corner (which are considered edges
 * centered at the corner) aware for matching with other tiles.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface WangTile {

    /**
     * Returns the patter of the edge at the given direction.
     *
     * @param dir
     * @return
     */
    public WangEdge getEdge(DirectionIntercardinal dir);
}
