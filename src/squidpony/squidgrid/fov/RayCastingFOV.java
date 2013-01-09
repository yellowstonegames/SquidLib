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
 * This algorithm treats transparency as either fully transparent or fully
 * opaque. If a cell's transparency is less than 1 it is treated as transparent.
 *
 * Light will decay, with solid objects being lit if there is a lit cell next to
 * them in the direction of the source point. Such objects will be lit according
 * to the decay so a solid object at the edge of vision will not be lit if a
 * transparent object in the same cell would not be lit.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class RayCastingFOV implements FOVSolver {

    private float gap = 0.4f;//how much gap to leave from the edges when tracing rays
    private float step = 0.1f;//the size of step to take when walking out rays
    private float[][] lightMap;
    private float[][] map;
    private float decay, force, startx, starty;
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
        this.startx = startx + 0.5f;
        this.starty = starty + 0.5f;
        this.rStrat = radiusStrategy;
        width = map.length;
        height = map[0].length;
        lightMap = new float[width][height];

        float maxRadius = force / decay;

        int left = (int) Math.max(0, startx - maxRadius);
        int right = (int) Math.min(width - 1, startx + maxRadius);
        int top = (int) Math.max(0, starty - maxRadius);
        int bottom = (int) Math.min(height - 1, starty + maxRadius);

        lightMap[startx][starty] = force;

        //run rays out to edges
        for (int x = left; x <= right; x++) {
            runLine(startx, starty, x, top);
            runLine(startx, starty, x, bottom);
        }
        for (int y = top; y <= bottom; y++) {
            runLine(startx, starty, left, y);
            runLine(startx, starty, right, y);
        }
        lightObstacles();

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
    private void runLine(int startx, int starty, int endx, int endy) {
        //in order to cover all paths, each of the three nearest corners have to be run to each other
        float x = startx + 0.05f;
        float y = starty + 0.05f;

        //build up arrays of points to run
        float[] x1 = {x, x - gap, x + gap},
                x2 = {endx + 0.5f},
                y1 = {y, y - gap, y + gap},
                y2 = {endy + 0.5f};

        for (int i = 0; i < x1.length; i++) {
            for (int j = 0; j < y1.length; j++) {
                for (int k = 0; k < x2.length; k++) {
                    for (int f = 0; f < y2.length; f++) {
                        runLine(x1[i], y1[j], Math.atan2(x2[k] - x1[i], y2[f] - y1[j]), force);
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
        //check that still in range
        float deltax = startx - x;
        float deltay = starty - y;
        float distance = rStrat.radius(Math.abs(deltax), Math.abs(deltay));
        if (currentLight <= 0) {
            return;//reached edge of vision
        }

        int x2 = (int) Math.round(x);
        int y2 = (int) Math.round(y);
        if (map[x2][y2] < 1f) {
            lightMap[x2][y2] = (float) Math.max(lightMap[x2][y2], getNearLight(x2, y2));
            runLine(x + step * (float) Math.cos(angle), y + step * (float) Math.sin(angle), angle, (force - decay * distance) * (1 - map[x2][y2]));
        }
    }

    /**
     * Find the light let through by the nearest square.
     *
     * @param x
     * @param y
     * @return
     */
    private float getNearLight(int x, int y) {
        int x2 = (int) (x - Math.signum(x - (int) startx));
        int y2 = (int) (y - Math.signum(y - (int) starty));

        //clamp x2 and y2 to bound within map
        x2 = Math.max(0, x2);
        x2 = Math.min(width - 1, x2);
        y2 = Math.max(0, y2);
        y2 = Math.min(height - 1, y2);

        //find largest emmitted light in direction of source
        float light = Math.max(Math.max(lightMap[x2][y] * (1 - map[x2][y]),
                lightMap[x][y2] * (1 - map[x][y2])),
                lightMap[x2][y2] * (1 - map[x2][y2]));

        float distance = rStrat.radius(x, y, x2, y2);//find radius for one square away
        light = light - decay * distance;
        return light;
    }

    /**
     * Ensures that all cells that blocked vision are themselves lit up.
     */
    private void lightObstacles() {
        float[][] wallMap = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map[x][y] > 0f) {//only light up non-transparent objects at the edge of view
                    wallMap[x][y] = getNearLight(x, y);
                }
            }
        }

        //merge wallMap to lightMap
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                lightMap[x][y] = Math.max(wallMap[x][y], lightMap[x][y]);
            }
        }
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        return calculateFOV(map, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }
}
