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

    private float gap = 0.01f;//how much gap to leave from the edges when tracing rays
    private double step = 0.1f;//the size of step to take when walking out rays
    private float[][] lightMap;
    private float[][] map;
    private float radius, decay, force;
    private int startx, starty, width, height;
    private boolean simplified;

    /**
     * Builds a new ray tracing fov solver.
     *
     * @param step the length along the ray to traverse in each step
     */
    public RayCastingFOV(double step) {
        this.step = step;
    }

    /**
     * Builds a new ray tracing fov solver with the default step size.
     */
    public RayCastingFOV() {
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, boolean simplifiedDiagonals) {
        this.map = map;
        this.force = force;
        this.decay = decay;
        this.startx = startx;
        this.starty = starty;
        this.simplified = simplifiedDiagonals;
        radius = force / decay;//assume worst case of no resistance in tiles
        width = map.length;
        height = map[0].length;
        lightMap = new float[width][height];

        int left = (int) Math.max(0, startx - radius);
        int right = (int) Math.min(width - 1, startx + radius);
        int top = (int) Math.max(0, starty - radius);
        int bottom = (int) Math.min(height - 1, starty + radius);

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
        float[] x1 = {startx + 0.5f, startx - gap, startx + 1 + gap},
                x2 = {endx + 0.5f},
                y1 = {starty + 0.5f, starty - gap, starty + 1 + gap},
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
    private void runLine(double x, double y, double angle, double currentLight) {
        //check that still in range
        double deltax = startx - x;
        double deltay = starty - y;
        double distance = Math.sqrt(deltax * deltax + deltay * deltay);
        if (distance > radius || currentLight <= 0) {
            return;//reached edge of vision
        }

        int x2 = (int) x;
        int y2 = (int) y;
        if (map[x2][y2] < 1f) {
            lightMap[x2][y2] = (float) Math.max(lightMap[x2][y2], getNearLight(x2, y2));
            runLine(x + step * Math.cos(angle), y + step * Math.sin(angle), angle, (force - decay * distance) * (1 - map[x2][y2]));
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
        int x2 = x - (int) Math.signum(x - startx);
        int y2 = y - (int) Math.signum(y - starty);

        //clamp x2 and y2 to bound within map
        x2 = Math.max(0, x2);
        x2 = Math.min(width - 1, x2);
        y2 = Math.max(0, y2);
        y2 = Math.min(height - 1, y2);

        //find largest emmitted light in direction of source
        float light = Math.max(Math.max(lightMap[x2][y] * (1 - map[x2][y]),
                lightMap[x][y2] * (1 - map[x][y2])),
                lightMap[x2][y2] * (1 - map[x2][y2]));

        float distance = 1;
        if (!simplified && x2 != x && y2 != y) {//it's a diagonal
            distance = (float) Math.sqrt(2);
        }

        distance = Math.max(0, distance);
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
        return calculateFOV(map, startx, starty, radius, 1, true);
    }
}
