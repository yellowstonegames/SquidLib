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
    private float force, decay;
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

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                light[i][j] = 0f;
            }
        }

        int maxRadius = (int) (force * decay);
        int oct, r2;

        if (maxRadius <= 0) {
            int max_radius_x = this.width - startx;
            int max_radius_y = this.height - starty;
            max_radius_x = Math.max(max_radius_x, startx);
            max_radius_y = Math.max(max_radius_y, starty);
            maxRadius = (int) (Math.sqrt(max_radius_x * max_radius_x
                    + max_radius_y * max_radius_y)) + 1;
        }
        r2 = maxRadius * maxRadius;

        /* recursive shadow casting */
        for (oct = 0; oct < 8; oct++) {
            castLight(startx, starty, 1, 1.0f, 0.0f, maxRadius, r2,
                    MULT[0][oct], MULT[1][oct], MULT[2][oct], MULT[3][oct], 0,
                    true);
        }
        light[startx][starty] = force;

        return light;
    }

    private void castLight(int cx, int cy, int row, float start, float end,
            int radius, int r2, int xx, int xy, int yx, int yy, int id, boolean light_walls) {
        int j;
        float new_start = 0.0f;
        if (start < end) {
            return;
        }
        for (j = row; j <= radius; j++) {
            int dx = -j - 1;
            int dy = -j;
            boolean blocked = false;
            while (dx <= 0) {
                int X, Y;
                dx++;
                X = cx + dx * xx + dy * xy;
                Y = cy + dx * yx + dy * yy;
                if (X >= 0 && Y >= 0 && X < this.width && Y < this.height) {
                    float l_slope, r_slope;
                    l_slope = (dx - 0.5f) / (dy + 0.5f);
                    r_slope = (dx + 0.5f) / (dy - 0.5f);
                    if (start < r_slope) {
                        continue;
                    } else if (end > l_slope) {
                        break;
                    }
                    if (dx * dx + dy * dy <= r2 && (light_walls || map[X][Y] < 1)) {
                        float bright = (float) (1 - (decay * rStrat.radius(dx, dy) / force));
                        light[X][Y] = bright;
                    }
                    if (blocked) {
                        if (map[X][Y] >= 1) {
                            new_start = r_slope;
                            continue;
                        } else {
                            blocked = false;
                            start = new_start;
                        }
                    } else {
                        if (map[X][Y] >= 1 && j < radius) {
                            blocked = true;
                            castLight(cx, cy, j + 1, start, l_slope, radius,
                                    r2, xx, xy, yx, yy, id + 1, light_walls);
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
