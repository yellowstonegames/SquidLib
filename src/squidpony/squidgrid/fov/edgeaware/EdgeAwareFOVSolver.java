package squidpony.squidgrid.fov.edgeaware;

import squidpony.annotation.Beta;
import squidpony.squidgrid.util.RadiusStrategy;

/**
 * A 2D Field of View solver that understands cells having edges and will return
 * lighting based on them.
 *
 * The returned data structure may optionally be the same as the one passed in,
 * modified to reflect the results. The default behavior is that the same data
 * structure is used. See specific implementations for details on what changes
 * are made.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface EdgeAwareFOVSolver {

    public EdgeAwareCell[][] calculateFOV(EdgeAwareCell[][] resistanceMap, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy);

    public EdgeAwareCell[][] calculateFOV(EdgeAwareCell[][] resistanceMap, int startx, int starty, float radius);

    /**
     * Sets whether the passed in resistance map should be directly modified to
     * reflect results from the solver.
     *
     * Passing in true causes the passed in resistance map to be given the
     * results directly. False causes a new data structure with the results to
     * be returned placed in the new structure.
     *
     * @param write true to overwrite passed in object array
     */
    public void writeToResistanceMap(boolean write);
}
