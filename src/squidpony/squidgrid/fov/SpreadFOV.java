package squidpony.squidgrid.fov;

import squidpony.squidgrid.RadiusStrategy;
import squidpony.squidgrid.BasicRadiusStrategy;
import squidpony.annotation.Beta;

/**
 * Performs FOV by pushing values outwards from the source location. It will
 * spread around edges like smoke or water. This may not be the desired behavior
 * for a strict sight area, but may be appropriate for a sound map.
 *
 * This algorithm does perform bounds checking.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class SpreadFOV implements FOVSolver {

    private float[][] lightMap;
    private float[][] map;
    private float radius, decay;
    private int startx, starty, width, height;
    private RadiusStrategy rStrat;

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
        float light = Math.max(Math.max(lightMap[x2][y] - map[x2][y],
                lightMap[x][y2] - map[x][y2]),
                lightMap[x2][y2] - map[x2][y2]);

        float distance = rStrat.radius(x, y, x2, y2);
        light -= decay * distance;
        return light;
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy rStrat) {
        this.map = map;
        this.decay = decay;
        this.startx = startx;
        this.starty = starty;
        this.rStrat = rStrat;
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

                double r2 = rStrat.radius(startx, starty, dx, dy);
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
        return calculateFOV(map, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }
}
