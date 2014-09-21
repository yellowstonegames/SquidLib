package squidpony.squidgrid.fov.edgeaware;

import squidpony.annotation.Beta;
import squidpony.squidgrid.DirectionIntercardinal;

/**
 * A cell which can report resistance values based on edges.
 *
 * Edges includes the center as an "edge"
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface EdgeAwareCell {

    /**
     * Gets the resistance associated with the provided key.
     *
     * @param key the key to check for
     * @param edge the directional edge to check
     * @return the resistance this edge has
     */
    public float resistance(String key, DirectionIntercardinal edge);

    /**
     * Gets the resistance associated with a null key.
     *
     * Provided as a convenience where the FOV is only being used in one specific way.
     *
     * @param edge the edge to check
     * @return the resistance this edge has
     */
    public float resistance(DirectionIntercardinal edge);

    /**
     * Sets the resistance value to associate with the provided key.
     *
     * If a resistance value was previously associated with the key, then that value is overwritten with the new value.
     *
     * @param key the key to check for
     * @param edge the directional edge to check
     * @param resistance the amount of degradation from passing through this edge
     */
    public void setResistance(String key, DirectionIntercardinal edge, float resistance);

    /**
     * Sets the resistance value to associate with a null key.
     *
     * If a resistance value was previously associated with the null key, then that value is overwritten with the new
     * value.
     *
     * @param edge the directional edge to check
     * @param resistance the amount of degradation from passing through this edge
     */
    public void setResistance(DirectionIntercardinal edge, float resistance);
}
