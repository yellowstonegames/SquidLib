package squidpony.squidgrid.fov;

/**
 * Calculates Field of View using SpiralFOV with both square and circle radius
 * strategies and composites the results.
 * 
 * Because of the composition it ignores any radiusStrategy passed in so null is
 * acceptable.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class CompositeSpiralFOV implements FOVSolver{

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
