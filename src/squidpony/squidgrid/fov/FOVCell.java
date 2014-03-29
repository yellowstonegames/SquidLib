package squidpony.squidgrid.fov;

import squidpony.annotation.Beta;
import squidpony.squidgrid.util.DirectionIntercardinal;

/**
 * A cell which contains the information needed to process FOV and LOS.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface FOVCell {

    /**
     * Returns the general resistance to the provided key.
     *
     * @param key
     * @return
     */
    public float resistance(String key);

    /**
     * Returns the resistance to the provided key to rays passing through the
     * given edge, corner, or center.
     *
     * If the Direction is NONE, then the cell's overall translucency is used.
     *
     * @param key
     * @param direction
     * @return
     */
    public float resistance(String key, DirectionIntercardinal direction);

    /**
     * Allows the FOV algorithm to store the result of its computations directly
     * into the relevant cell.
     *
     * @param key
     * @param value
     */
    public void setFOVResult(String key, float value);

    /**
     * Allows the FOV algorithm to store the result of its computations directly
     * into the relevant cell.
     *
     * @param key
     * @param direction
     * @param value
     */
    public void setFOVResult(String key, DirectionIntercardinal direction, float value);
}
