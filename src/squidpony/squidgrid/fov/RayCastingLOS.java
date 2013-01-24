package squidpony.squidgrid.fov;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;
import squidpony.annotation.Beta;

/**
 * Uses a series of rays internal to the start and end point to determine
 * visibility.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class RayCastingLOS implements LOSSolver {

    Queue<Point> path;
    private float gap = 0.4f;//how much gap to leave from the edges when tracing rays
    private float step = 0.1f;//the size of step to take when walking out rays

    /**
     * Creates a new instance of this solver which uses the provided step
     * distance and gap distance when performing calculations.
     *
     * @param step
     * @param gap
     */
    public RayCastingLOS(float step, float gap) {
        this.step = step;
        this.gap = gap;
    }

    /**
     * Creates an instance of this solver which uses the default gap and step.
     */
    public RayCastingLOS() {
    }

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety, float force, float decay, RadiusStrategy radiusStrategy) {
        path = new LinkedList<>();
        float maxRadius = force / decay;
        float x1 = startx + 0.5f;
        float y1 = starty + 0.5f;
        float x2 = targetx + 0.5f;
        float y2 = targety + 0.5f;


        double angle = Math.atan2(y2 - y1, x2 - x1);
        return false;
    }

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety) {
        return isReachable(resistanceMap, startx, starty, targetx, targety, 1, 0, BasicRadiusStrategy.CIRCLE);
    }

    @Override
    public Queue<Point> getLastPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
