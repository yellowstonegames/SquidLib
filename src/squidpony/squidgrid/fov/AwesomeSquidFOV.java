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
public class AwesomeSquidFOV implements FOVSolver {

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

        //clamp x2 and y2 to bound within map
        x2 = Math.max(0, x2);
        x2 = Math.min(width - 1, x2);
        y2 = Math.max(0, y2);
        y2 = Math.min(height - 1, y2);

        int lit = 0;//track the number of nearby cells which are lit

        //add points to be checked
        Point p1, p2;
        p1 = new Point(x, y2);
        p2 = new Point(x2, y);
        if (x2 == x) {//add one up and one down
            p1 = new Point(Math.max(0, x - 1), y2);
            p2 = new Point(Math.min(width - 1, x + 1), y2);
            lit++;//from straight on only need one more side visible
        }
        if (y2 == y) {//add one up and one down
            p1 = new Point(x2, Math.max(0, y - 1));
            p2 = new Point(x2, Math.min(height - 1, y + 1));
            lit++;//from straight on only need one more side visible
        }

        //find largest emmitted light in direction of source
        float light = 0f;
        int close = rStrat.radius(startx, starty, x, y) <= 1 ? 0 : 1; //if next to start cell, don't apply start cell's resistance
        if (map[x2][y2] < 1f && lightMap[x2][y2] > 0) {
            light = Math.max(light, lightMap[x2][y2] * (1 - close * map[x2][y2]));
            lit++;
        }
        if (map[p1.x][p1.y] < 1f && lightMap[p1.x][p1.y] > 0) {
            light = Math.max(light, lightMap[p1.x][p1.y] * (1 - close * map[p1.x][p1.y]));
            lit++;
            if (xDominant < 0) {
                lit++;
            }
        }
        if (map[p2.x][p2.y] < 1f && lightMap[p2.x][p2.y] > 0) {
            light = Math.max(light, lightMap[p2.x][p2.y] * (1 - close * map[p2.x][p2.y]));
            lit++;
            if (xDominant > 0) {
                lit++;
            }
        }

        //make sure there's enough light, but allow an exception if closest cell is clear and both neighbors are walls
        if (lit < 2 //not enought light
                && !(map[x2][y2] < 1f && map[x2][y] >= 1f && map[x][y2] >= 1f)) {
            light = 0;
        }

        float distance = rStrat.radius(x, y, x2, y2);
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
