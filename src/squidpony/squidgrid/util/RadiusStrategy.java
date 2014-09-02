package squidpony.squidgrid.util;

import java.awt.Point;

/**
 * Indicates which method of dealing with the radius during FOV and LOS solving is preferred.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public interface RadiusStrategy {

    /**
     * Returns the radius between the two points provided.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    public float radius(int startx, int starty, int endx, int endy);

    /**
     * Returns the radius calculated using the two distances provided.
     *
     * @param dx
     * @param dy
     * @return
     */
    public float radius(int dx, int dy);

    /**
     * Returns the radius between the two points provided.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    public float radius(float startx, float starty, float endx, float endy);

    /**
     * Returns the radius calculated based on the two distances provided.
     *
     * @param dx
     * @param dy
     * @return
     */
    public float radius(float dx, float dy);

    /**
     * Returns a random point within the unit distance provided for this radius strategy.
     *
     * @param distance
     * @return
     */
    public Point onUnitShape(double distance);

}
