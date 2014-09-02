package squidpony.squidgrid.util;

import java.awt.Point;
import squidpony.squidmath.RNG;

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
     * Returns the minimum y value this strategy will return with the given radius distance.
     *
     * @param distance
     * @return
     */
    public double minX(double distance);

    /**
     * Returns the maximum x value this strategy will return with the given radius distance.
     *
     * @param distance
     * @return
     */
    public double maxX(double distance);

    /**
     * Returns the minimum y value this strategy will return with the given radius distance.
     *
     * @param distance
     * @return
     */
    public double minY(double distance);

    /**
     * Returns the maximum y value this strategy will return with the given radius distance.
     *
     * @param distance
     * @return
     */
    public double maxY(double distance);

    /**
     * Returns a random point within the unit distance provided for this radius strategy.
     *
     * @param distance
     * @return
     */
    public Point onUnitShape(double distance);

    /**
     * Returns a random point within the unit distance provided for this radius strategy.
     *
     * @param distance
     * @param random The source of randomness for this operation.
     * @return
     */
    public Point onUnitShape(double distance, RNG random);
}
