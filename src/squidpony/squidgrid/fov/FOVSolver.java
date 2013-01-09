package squidpony.squidgrid.fov;

/**
 * An interface for Field of View algorithms.
 *
 * FIeld of View (FOV) algorithms determine how much area surrounding a point
 * can be seen. They return a two dimensional array of floats, representing the
 * amount of view (typically sight, but perhaps sound, smell, etc.) which the
 * origin has of each cell.
 *
 * The force parameter allows tiles to have a varying resistance to the kind of
 * rays emanating from the source. If a simple radius is desired, set the
 * resistance of your to 0.0f, the force to 1, and the decay to 1 / (the
 * radius). Opaque cells should return a resistance of 1f and transparent ones
 * 0f.
 *
 * The coordinates of the returned structure match those of the input grid.
 *
 * Most solvers expect the edges of the map to have opaque cells. Since there
 * are no bounds checking in the these algorithms, they will fail if the edges
 * are not opaque.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public interface FOVSolver {

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a lightmap for a result where the values represent a
     * percentage of fully lit.
     *
     * In general a value equal to or below 0 means that cell is not in the
     * field of view, whereas a value equal to or above 1 means that cell is
     * fully in the field of view.
     *
     * Note that values below 0 and above 1 may be returned in certain
     * circumstances. In these cases it is up to the calling class to determine
     * how to treat such values.
     *
     * The starting point for the calculations is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * BasicRadiusStrategy.
     *
     * @param map the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @param force the power of the ray
     * @param decay how much the light is reduced for each whole integer step in
     * distance
     * @param radiusStrategy provides a means to calculate the radius as desired
     * @return the computed light grid
     */
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy);

    /**
     * Calculates the Field of View in the same manner as the version with more
     * parameters.
     *
     * Light will extend to the radius value. Uses the implementation's default
     * radius strategy.
     *
     * @param map the grid of cells to calculate on
     * @param startx
     * @param starty
     * @param radius
     * @return
     */
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius);
}
