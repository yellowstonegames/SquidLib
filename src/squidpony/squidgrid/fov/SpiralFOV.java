package squidpony.squidgrid.fov;

/**
 * Performs FOV by pushing values outwards from the source location.
 *
 * This algorithm does perform bounds checking.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class SpiralFOV implements FOVSolver {

    private float[][] lightMap;
    private float[][] map;
    private float radius, decay;
    private int startx, starty, width, height;
    private boolean simplified;

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

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, boolean simplifiedDiagonals) {
        this.map = map;
        this.decay = decay;
        this.startx = startx;
        this.starty = starty;
        this.simplified = simplifiedDiagonals;
        radius = force / decay;//assume worst case of no resistance in tiles
        width = map.length;
        height = map[0].length;
        lightMap = new float[width][height];

        lightMap[startx][starty] = force;//make the starting space full power

        lightSurroundings(startx, starty);

        return lightMap;
    }

    private void lightSurroundings(int x, int y) {
        if (lightMap[x][y] <= 0) {
            return;//no light to spread
        }

        for (int dx = x - 1; dx <= x + 1; dx++) {
            for (int dy = y - 1; dy <= y + 1; dy++) {
                //ensure in bounds
                if (dx < 0 || dx >= width || dy < 0 || dy >= height) {
                    continue;
                }

                double r2;
                if (simplified) {
                    r2 = Math.sqrt((dx - startx) * (dx - startx) + (dy - starty) * (dy - starty));
                } else {
                    r2 = Math.abs(dx - startx) + Math.abs(dy - starty);
                }
                if (r2 <= radius) {
                    float surroundingLight = getNearLight(dx, dy);
                    if (lightMap[dx][dy] < surroundingLight) {
                        lightMap[dx][dy] = surroundingLight;
                        lightSurroundings(dx, dy);//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        return calculateFOV(map, startx, starty, radius, 1, true);
    }
}
