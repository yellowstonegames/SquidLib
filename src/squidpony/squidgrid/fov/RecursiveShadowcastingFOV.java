package squidpony.squidgrid.fov;

/**
 * This class performs FOV calculations using the recursive shadowcasting
 * process.
 *
 * Because of the way this algorithm works, light decay over distance is not
 * implemented.
 *
 * Derived from work included in the Blacken Library for roguelike games.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class RecursiveShadowcastingFOV implements FOVSolver {

    private static final int[][] mult = {
        {1, 0, 0, -1, -1, 0, 0, 1},
        {0, 1, -1, 0, 0, -1, 1, 0},
        {0, 1, 1, 0, 0, -1, -1, 0},
        {1, 0, 0, 1, -1, 0, 0, -1},};
    private float[][] lightMap;
    private int width, height;
    private FOVCell[][] map;
    private String key;

    @Override
    public float[][] calculateFOV(FOVCell[][] map, int x, int y, float distance, float decay, boolean simplifiedDiagonals, String key) {
        width = map.length;
        height = map[0].length;
        lightMap = new float[width][height];
        this.map = map;
        this.key = key;

        /* recursive shadow casting */
        for (int oct = 0; oct < 8; oct++) {
            castLight(x, y, 1, 1.0f, 0.0f, (int) distance, (int) (distance * distance), mult[0][oct], mult[1][oct], mult[2][oct], mult[3][oct], 0);
        }

        return lightMap;
    }

    private void castLight(int cx, int cy, int row, float start, float end, int radius, int r2, int xx, int xy, int yx, int yy, int id) {
        int j;
        float new_start = 0.0f;
        if (start < end) {
            return;
        }
        for (j = row; j < radius + 1; j++) {
            int dx = -j - 1;
            int dy = -j;
            boolean blocked = false;
            while (dx <= 0) {
                dx++;
                int x = cx + dx * xx + dy * xy;
                int y = cy + dx * yx + dy * yy;
                if (x < width && y < height) {
                    float l_slope;
                    float r_slope;
                    l_slope = (dx - 0.5f) / (dy + 0.5f);
                    r_slope = (dx + 0.5f) / (dy - 0.5f);
                    if (start < r_slope) {
                        continue;
                    } else if (end > l_slope) {
                        break;
                    }
                    if (dx * dx + dy * dy <= r2 && map[x][y].getTransparency(key) > 0.0f) {
                        lightMap[x][y] = 1.0f;
                    }
                    if (blocked) {
                        if (map[x][y].getTransparency(key) <= 0.0f) {
                            new_start = r_slope;
                            continue;
                        } else {
                            blocked = false;
                            start = new_start;
                        }
                    } else {
                        if (map[x][y].getTransparency(key) <= 0.0f && j < radius) {
                            blocked = true;
                            castLight(cx, cy, j + 1, start, l_slope, radius, r2, xx, xy, yx, yy, id + 1);
                            new_start = r_slope;
                        }
                    }
                }
            }
        }
    }
}
