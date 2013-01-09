package squidpony.squidgrid.fov;

import java.awt.Point;

/**
 * Performs FOV by pushing values outwards from the source location. It will
 * only go around corners slightly.
 *
 * This algorithm does perform bounds checking.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class RippleFOV implements FOVSolver {

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

        int xDominant = Math.abs(x - startx) - Math.abs(y - starty);
        float distance = rStrat.radius(x, y, x2, y2);
        boolean corner = false;

        //clamp x2 and y2 to bound within map
        x2 = Math.max(0, x2);
        x2 = Math.min(width - 1, x2);
        y2 = Math.max(0, y2);
        y2 = Math.min(height - 1, y2);

        if (map[x2][y2] < 1f && map[x][y2] >= 1f && map[x2][y] >= 1f) {
            corner = true;
        }

        boolean mainLit = false, sideALit = false, sideBLit = false;

        //find largest emmitted light in direction of source
        float light = 0f;
        int close = rStrat.radius(startx, starty, x, y) <= 1 ? 0 : 1; //if next to start cell, don't apply start cell's resistance
        if (map[x2][y2] < 1f && lightMap[x2][y2] > 0) {
            light = Math.max(light, lightMap[x2][y2] * (1 - close * map[x2][y2]));
            mainLit = true;
            if (xDominant == 0 || x2 == x || y2 == y) {//on a diagonal or axis
//                lit++;
            }
        }

        //check neighbors
        if (x2 == x) {//add one left and right
            int dx1 = Math.max(0, x - 1);
            int dx2 = Math.min(width - 1, x + 1);
            int dy = y2;
            if (map[dx2][dy] < 1f && lightMap[dx2][dy] > 0) {
                light = Math.max(light, lightMap[dx2][dy] * (1 - close * map[dx2][dy]));
                sideALit = true;
            }
            if (map[dx1][dy] < 1f && lightMap[dx1][dy] > 0) {
                light = Math.max(light, lightMap[dx1][dy] * (1 - close * map[dx1][dy]));
                sideBLit = true;
            }
        } else if (y2 == y) {//add one up and one down
            int dy1 = Math.max(0, y - 1);
            int dy2 = Math.min(height - 1, y + 1);
            int dx = x2;
            if (map[dx][dy1] < 1f && lightMap[dx][dy1] > 0) {
                light = Math.max(light, lightMap[dx][dy1] * (1 - close * map[dx][dy1]));
                sideALit = true;
            }
            if (map[dx][dy2] < 1f && lightMap[dx][dy2] > 0) {
                light = Math.max(light, lightMap[dx][dy2] * (1 - close * map[dx][dy2]));
                sideBLit = true;
            }
        } else {
            if (map[x2][y] < 1f && lightMap[x2][y] > 0) {
                float tempLight = lightMap[x2][y];
                if (tempLight > 0) {
                    if (xDominant > 0 && map[x2][y2] >= 1f) {
//                        lit++;
                    }
                    light = Math.max(light, tempLight * (1 - close * map[x2][y]));
                    sideALit = true;
                }
            }
            if (map[x][y2] < 1f && lightMap[x][y2] > 0) {
                float tempLight = lightMap[x][y2];
                if (tempLight > 0) {
                    if (xDominant < 0 && map[x2][y2] >= 1f) {
//                        lit++;
                    }
                    light = Math.max(light, tempLight * (1 - close * map[x][y2]));
                    sideBLit = true;
                }
            }
        }

        //make sure there's at either the most direct tile or both other tiles lit, but allow an exception if closest cell is clear and both neighbors are walls
        boolean killLight = true;//broken out into steps for debugging
        if (mainLit) {
            killLight = false;
        }
        if (sideALit && sideBLit) {
            killLight = false;
        }
        if (corner) {
            killLight = false;
        }
        if (killLight) {
            light = 0;
        }

        light = light - decay * distance;
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

                double r = rStrat.radius(startx, starty, dx, dy);
                if (r <= radius) {
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
