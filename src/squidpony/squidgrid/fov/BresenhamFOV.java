
package squidpony.squidgrid.fov;

import squidpony.squidgrid.fov.threed.FOVSolver3D;
import squidpony.annotation.Beta;
import squidpony.squidgrid.util.RadiusStrategy;

/**
 * Uses BresenhamLOS to run lines out to the edges.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class BresenhamFOV implements FOVSolver3D{

    @Override
    public float[][][] calculateFOV(float[][] resistanceMap, int startx, int starty, int startz, float force, float decay, RadiusStrategy radiusStrategy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[][][] calculateFOV(float[][] resistanceMap, int startx, int starty, int startz, float radius) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
