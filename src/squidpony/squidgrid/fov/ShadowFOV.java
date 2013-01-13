package squidpony.squidgrid.fov;

/**
 * Recursive shadowcasting FOV. Uses force * decay for the radius calculation
 * and treats all translucent cells as fully transparent.
 *
 * Performs bounds checking so edges are not required to be opaque.
 *
 * Does not function properly with strategies other than Circle.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class ShadowFOV implements FOVSolver {

    private int width, height;
    final private static int[][] MULT = {{1, 0, 0, -1, -1, 0, 0, 1},
        {0, 1, -1, 0, 0, -1, 1, 0}, {0, 1, 1, 0, 0, -1, -1, 0},
        {1, 0, 0, 1, -1, 0, 0, -1}};
    private float[][] light;
    private float[][] map;
    private float force, decay, radius;
    private RadiusStrategy rStrat;

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy rStrat) {
        width = map.length;
        height = map[0].length;
        this.force = force;
        this.decay = decay;
        this.rStrat = rStrat;
        light = new float[width][height];
        this.map = map;

        radius = (force / decay);

        // shadow casting each octant
        for (int oct = 0; oct < 8; oct++) {
            castLight(startx, starty, 1, 1.0f, 0.0f,
                    MULT[0][oct], MULT[1][oct], MULT[2][oct], MULT[3][oct], 0);
        }
        light[startx][starty] = force;

        return light;
    }

    private void castLight(int cx, int cy, int row, float start, float end,
            int xx, int xy, int yx, int yy, int id) {
        float new_start = 0.0f;
        if (start < end) {
            return;
        }
        for (int distance = row; distance <= radius; distance++) {
            int dx = -distance - 1;
            int dy = -distance;
            boolean blocked = false;
            while (dx <= 0) {
                int currentX, currentY;
                dx++;
                currentX = cx + dx * xx + dy * xy;
                currentY = cy + dx * yx + dy * yy;
                if (currentX >= 0 && currentY >= 0 && currentX < this.width && currentY < this.height) {
                    float l_slope, r_slope;
                    l_slope = (dx - 0.5f) / (dy + 0.5f);
                    r_slope = (dx + 0.5f) / (dy - 0.5f);
                    if (start < r_slope) {
                        continue;
                    } else if (end > l_slope) {
                        break;
                    }
                    if (rStrat.radius(dx, dy) <= radius) {
                        float bright = (float) (1 - (decay * rStrat.radius(dx, dy) / force));
                        light[currentX][currentY] = bright;
                    }
                    if (blocked) {
                        if (map[currentX][currentY] >= 1) {
                            new_start = r_slope;
                            continue;
                        } else {
                            blocked = false;
                            start = new_start;
                        }
                    } else {
                        if (map[currentX][currentY] >= 1 && distance < radius) {
                            blocked = true;
                            castLight(cx, cy, distance + 1, start, l_slope,
                                    xx, xy, yx, yy, id + 1);
                            new_start = r_slope;
                        }
                    }
                }
            }
            if (blocked) {
                break;
            }
        }
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        return calculateFOV(map, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }
}
