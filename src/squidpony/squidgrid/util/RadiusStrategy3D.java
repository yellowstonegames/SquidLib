package squidpony.squidgrid.util;

import squidpony.annotation.Beta;

/**
 * Indicates which method of dealing with the radius during FOV and LOS solving
 * is preferred.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface RadiusStrategy3D extends RadiusStrategy {

    /**
     * Returns the radius between the two points provided.
     *
     * @param startx
     * @param starty
     * @param startz
     * @param endx
     * @param endy
     * @param endz
     * @return
     */
    public float radius(int startx, int starty, int startz, int endx, int endy, int endz);

    /**
     * Returns the radius calculated using the two distances provided.
     *
     * @param dx
     * @param dy
     * @param dz
     * @return
     */
    public float radius(int dx, int dy, int dz);

    /**
     * Returns the radius between the two points provided.
     *
     * @param startx
     * @param starty
     * @param startz
     * @param endx
     * @param endy
     * @param endz
     * @return
     */
    public float radius(float startx, float starty, float startz, float endx, float endy, float endz);

    /**
     * Returns the radius calculated based on the two distances provided.
     *
     * @param dx
     * @param dy
     * @param dz
     * @return
     */
    public float radius(float dx, float dy, float dz);
}
