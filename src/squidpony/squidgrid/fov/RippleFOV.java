package squidpony.squidgrid.fov;

import squidpony.squidgrid.util.RadiusStrategy;
import squidpony.squidgrid.util.BasicRadiusStrategy;
import squidpony.annotation.Beta;

/**
 * Performs FOV by pushing values outwards from the source location. It will
 * only go around corners slightly.
 *
 * This algorithm does perform bounds checking.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class RippleFOV implements FOVSolver {

    private float[][] lightMap;
    private float[][] map;
    private boolean[][] indirect;//marks when a tile is only indirectly lit
    private float radius, decay;
    private int startx, starty, width = 1, height = 1;
    private RadiusStrategy rStrat;

    public RippleFOV() {
    }

    /**
     * Find the light let through by the nearest square.
     *
     * @param x
     * @param y
     * @return
     */
    private float getNearLight(int x, int y) {
        if (Math.abs(startx - x) <= 1 && Math.abs(starty - y) <= 1) {//if next to start cell, get full light
            return lightMap[startx][starty];
        }

        if (indirect[x][y]) {
            return 0f;//no light if this one was only indirectly lit
        }

        int x2 = x - (int) Math.signum(x - startx);
        int y2 = y - (int) Math.signum(y - starty);

        int xDominance = Math.abs(x - startx) - Math.abs(y - starty);
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
        if (!indirect[x2][y2] && map[x2][y2] < 1f && lightMap[x2][y2] > 0) {
            light = Math.max(light, lightMap[x2][y2]  - map[x2][y2]);
            mainLit = true;
        }

        //check neighbors
        if (x2 == x) {//add one left and right
            int dx1 = Math.max(0, x - 1);
            int dx2 = Math.min(width - 1, x + 1);
            int dy = y + (int) Math.signum(y - starty);//move one step further away from the source
            dy = Math.max(dy, 0);
            dy = Math.min(dy, height - 1);
            if (!indirect[dx2][dy] && map[dx2][dy] < 1f && lightMap[dx2][dy] > 0) {
                light = Math.max(light, lightMap[dx2][dy]  - map[dx2][dy]);
                sideALit = true;
            }
            if (!indirect[dx1][dy] && map[dx1][dy] < 1f && lightMap[dx1][dy] > 0) {
                light = Math.max(light, lightMap[dx1][dy] - map[dx1][dy]);
                sideBLit = true;
            }
        } else if (y2 == y) {//add one up and one down
            int dy1 = Math.max(0, y - 1);
            int dy2 = Math.min(height - 1, y + 1);
            int dx = x + (int) Math.signum(x - startx);//move one step further away from the source
            dx = Math.max(dx, 0);
            dx = Math.min(dx, width - 1);
            if (!indirect[dx][dy1] && map[dx][dy1] < 1f && lightMap[dx][dy1] > 0) {
                light = Math.max(light, lightMap[dx][dy1]  - map[dx][dy1]);
                sideALit = true;
            }
            if (!indirect[dx][dy2] && map[dx][dy2] < 1f && lightMap[dx][dy2] > 0) {
                light = Math.max(light, lightMap[dx][dy2]  - map[dx][dy2]);
                sideBLit = true;
            }
        } else {
            if (!indirect[x2][y] && xDominance > 0 && map[x2][y] < 1f && lightMap[x2][y] > 0) {
                float tempLight = lightMap[x2][y];
                if (tempLight > 0) {
                    light = Math.max(light, tempLight  - map[x2][y]);
                    sideALit = true;
                }
            } else if (!indirect[x][y2] && xDominance < 0 && map[x][y2] < 1f && lightMap[x][y2] > 0) {
                float tempLight = lightMap[x][y2];
                if (tempLight > 0) {
                    light = Math.max(light, tempLight  - map[x][y2]);
                    sideBLit = true;
                }
            } else if (!indirect[x2][y2] && xDominance == 0 && (map[x2][y2] < 1f || (map[x][y2] < 1f && map[x2][y] < 1f))) {//on a diagonal 
                float tempLight = Math.max(lightMap[x2][y2]  - map[x2][y2],
                        Math.max(lightMap[x2][y]  - map[x2][y], lightMap[x][y2]  - map[x][y2]));
                if (tempLight > 0) {
                    light = Math.max(light, tempLight);
                    mainLit = true;//really it might be that both sides are lit, but that counts the same
                }
            }
        }

        //make sure there's at either the most direct tile or both other tiles lit, but allow an exception if closest cell is clear and both neighbors are walls
        boolean killLight = true;//broken out into steps for debugging
        if (mainLit || (sideALit && sideBLit) || corner) {
            killLight = false;
            if (!mainLit) {
                indirect[x][y] = true;
            }
        }
        if (killLight) {//not lit at all counts as indirectly lit
            light = 0;
        }

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

        if (map.length != width || map[0].length != height) {
            width = map.length;
            height = map[0].length;
            lightMap = new float[width][height];
            indirect = new boolean[width][height];
        } else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    lightMap[x][y] = 0f;//mark as unlit
                    indirect[x][y] = false;
                }
            }
        }

        lightMap[startx][starty] = force;//make the starting space full power

        lightSurroundings(startx, starty);

        return lightMap;
    }

    private void lightSurroundings(int x, int y) {
        if (lightMap[x][y] <= 0 || indirect[x][y]) {
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
