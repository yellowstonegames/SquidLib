package squidpony.squidgrid.fov;

/**
 * Simple raytracing algorithm for Field of View. In large areas will be
 * relatively inefficient due to repeated visiting of some cells.
 *
 * Tracing is done from four points near the corners of the cells. The line
 * between points is then traversed to find what cells are intersected. Once the
 * traversal hits an opaque cell, runs out of light (based on decay), or reaches
 * the set radius, that walk is terminated.
 *
 *
 * Light will decay, with solid objects being lit if there is a lit cell next to
 * them in the direction of the source point. Such objects will be lit according
 * to the decay so a solid object at the edge of vision will not be lit if a
 * transparent object in the same cell would not be lit.
 * 
 * Currently a work in progress.
 *
 * @deprecated 
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class RayCastingFOV implements FOVSolver {

    private float gap = 0.4f;//how much gap to leave from the edges when tracing rays
    private float step = 0.1f;//the size of step to take when walking out rays
    private float[][] lightMap;
    private float[][] map;
    private float decay, force, fx, fy;
    private int width, height;
    private RadiusStrategy rStrat;

    /**
     * Builds a new ray tracing fov solver.
     *
     * @param step the length along the ray to traverse in each step
     * @param gap the offset from the center the lines will be traced
     */
    public RayCastingFOV(float step, float gap) {
        this.step = step;
        this.gap = gap;
    }

    /**
     * Builds a new ray tracing fov solver with the default step size.
     */
    public RayCastingFOV() {
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        this.map = map;
        this.force = force;
        this.decay = decay;
        this.fx = startx + 0.5f;
        this.fy = starty + 0.5f;
        this.rStrat = radiusStrategy;
        width = map.length;
        height = map[0].length;
        lightMap = new float[width][height];

        float maxRadius = force / decay + 1;

        int left = (int) Math.max(0, startx - maxRadius);
        int right = (int) Math.min(width - 1, startx + maxRadius);
        int top = (int) Math.max(0, starty - maxRadius);
        int bottom = (int) Math.min(height - 1, starty + maxRadius);

        lightMap[startx][starty] = force;

        //run rays out to edges
        for (int x = left; x <= right; x++) {
            runLineGroup(fx, fy, x, top);
            runLineGroup(fx, fy, x, bottom);
        }
        for (int y = top; y <= bottom; y++) {
            runLineGroup(fx, fy, left, y);
            runLineGroup(fx, fy, right, y);
        }

        return lightMap;
    }

    /**
     * Runs rays from approximately the corners of the cells. This reduces
     * artifacts in the results.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     */
    private void runLineGroup(float startx, float starty, int endx, int endy) {

        //build up arrays of points to run
        float[] x1 = {startx, startx - gap, startx + gap},
                x2 = {endx + 0.5f},
                y1 = {starty, starty - gap, starty + gap},
                y2 = {endy + 0.5f};

        for (int i = 0; i < x1.length; i++) {
            for (int j = 0; j < y1.length; j++) {
                for (int k = 0; k < x2.length; k++) {
                    for (int f = 0; f < y2.length; f++) {
                        double angle = Math.atan2(y2[f] - y1[j], x2[k] - x1[i]);
                        runLine(x1[i], y1[j], angle, force);
                    }
                }
            }
        }
    }

    /**
     * Follows the line and lights each transparent point along the path.
     *
     * @param x
     * @param y
     * @param angle
     * @return true if end point reached
     */
    private void runLine(float x, float y, double angle, float currentLight) {
        if (currentLight <= 0) {
            return;//reached edge of vision
        }

        int x2 = (int) x;
        int y2 = (int) y;
        lightMap[x2][y2] = Math.max(lightMap[x2][y2], currentLight);
        if (map[x2][y2] < 1f) {
            float nextX = x + step * (float) Math.cos(angle);
            float nextY = y + step * (float) Math.sin(angle);
            float bright = currentLight;//start with current amount of light
            if (x2 != (int) nextX || y2 != (int) nextY) {//only change light level if actually moving out of the square
                if (x2 != (int) fx || y2 != (int) fy) {//make sure not on starting point
                    float distance = rStrat.radius(x2, y2, (int) nextX, (int) nextY);
                    bright -= decay * distance;
                    bright = bright * (1 - map[x2][y2]);//decrease it by the resistance of the cell
                }
            }
            runLine(nextX, nextY, angle, bright);
        }
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        return calculateFOV(map, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }
}
