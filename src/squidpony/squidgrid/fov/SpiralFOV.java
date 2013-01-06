package squidpony.squidgrid.fov;

/**
 * Performs FOV by pushing values outwards from the source location.
 *
 * This algorithm does validate map boundaries.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class SpiralFOV implements FOVSolver {

    private float[][] lightMap;
    private FOVCell[][] map;
    private String key;
    private float radius, decay, force;
    private int startx, starty, width, height, left, right, top, bottom;

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

        //find largest emmitted light in direction of source
        float light = Math.max(Math.max(lightMap[x2][y] * (1 - map[x2][y].getResistance(key)),
                lightMap[x][y2]) * (1 - map[x][y2].getResistance(key)),
                lightMap[x2][y2]) * (1 - map[x2][y2].getResistance(key));

        return light - decay;
    }

    @Override
    public float[][] calculateFOV(FOVCell[][] map, int startx, int starty, float force, float decay, boolean simplifiedDiagonals, String key) {
        this.map = map;
        this.key = key;
        this.force = force;
        this.decay = decay;
        this.startx = startx;
        this.starty = starty;
        radius = force / decay;//assume worst case of no resistance in tiles
        width = map.length;
        height = map[0].length;
        lightMap = new float[width][height];

        left = (int) Math.max(0, startx - radius);
        right = (int) Math.min(width - 1, startx + radius);
        top = (int) Math.max(0, starty - radius);
        bottom = (int) Math.min(height - 1, starty + radius);

        lightMap[startx][starty] = force;//make the starting space full power

        lightSurroundings(startx, starty);

        return lightMap;
    }

    private void lightSurroundings(int x, int y) {
        if (lightMap[x][y] * (1 - map[x][y].getResistance(key)) - decay <= 0) {
            return;//no light to spread
        }

        for (int dx = x - 1; dx <= x + 1; dx++) {
            for (int dy = y - 1; dy <= y + 1; dy++) {
                if (dx >= left && dx <= right && dy >= top && dy <= bottom && (dx != x || dy != y)) {
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
    public float[][] calculateFOV(FOVCell[][] map, int startx, int starty, float radius) {
        return calculateFOV(map, startx, starty, radius, 1, true, "");
    }
}
