package squidpony.squidgrid.fov;

/**
 * Defines a set of methods for determining the distance between points in various
 * geometries.
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
}
