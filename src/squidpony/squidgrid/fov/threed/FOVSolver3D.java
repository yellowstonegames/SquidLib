package squidpony.squidgrid.fov.threed;

import squidpony.squidgrid.RadiusStrategy;
import squidpony.annotation.Beta;
import squidpony.squidgrid.fov.FOVSolver;

/**
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface FOVSolver3D extends FOVSolver {

    /**
     * Calculates the Field Of View for the provided map from the given x, y, z
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
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal on screen component of the starting location
     * @param starty the vertical on screen component of the starting location
     * @param startz the perpendicular-to-the-screen component of the starting
     * location
     * @param force the power of the ray
     * @param decay how much the light is reduced for each whole integer step in
     * distance
     * @param radiusStrategy provides a means to calculate the radius as desired
     * @return the computed light grid
     */
    public float[][][] calculateFOV(float[][] resistanceMap, int startx, int starty, int startz, float force, float decay, RadiusStrategy radiusStrategy);

    /**
     * Calculates the Field of View in the same manner as the version with more
     * parameters.
     *
     * Light will extend to the radius value. Uses the implementation's default
     * radius strategy.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx
     * @param starty
     * @param startz
     * @param radius
     * @return
     */
    public float[][][] calculateFOV(float[][] resistanceMap, int startx, int starty, int startz, float radius);
}
