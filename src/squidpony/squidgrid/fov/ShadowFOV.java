package squidpony.squidgrid.fov;

import squidpony.squidgrid.util.Direction;

/**
 * Recursive shadowcasting FOV. Uses force * decay for the radius calculation
 * and treats all translucent cells as fully transparent.
 *
 * Performs bounds checking so edges are not required to be opaque.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class ShadowFOV implements FOVSolver {

    private int width, height, startx, starty;
    private float[][] lightMap;
    private float[][] resistanceMap;
    private float force, decay, radius;
    private RadiusStrategy rStrat;

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float force, float decay, RadiusStrategy rStrat) {
        this.startx = startx;
        this.starty = starty;
        this.force = force;
        this.decay = decay;
        this.rStrat = rStrat;
        this.resistanceMap = resistanceMap;

        width = resistanceMap.length;
        height = resistanceMap[0].length;
        lightMap = new float[width][height];
        radius = (force / decay);

        lightMap[startx][starty] = force;//light the starting cell
        for (Direction d : Direction.DIAGONALS) {
            castLight(1, 1.0f, 0.0f, 0, d.deltaX, d.deltaY, 0);
            castLight(1, 1.0f, 0.0f, d.deltaX, 0, 0, d.deltaY);
        }

        return lightMap;
    }

    private void castLight(int row, float start, float end, int xx, int xy, int yx, int yy) {

        float newStart = 0.0f;
        if (start < end) {
            return;
        }
        boolean blocked = false;
        for (int distance = row; distance <= radius && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < this.width && currentY < this.height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                //check if it's within the lightable area and light if needed
                if (rStrat.radius(deltaX, deltaY) <= radius) {
                    float bright = (float) (1 - (decay * rStrat.radius(deltaX, deltaY) / force));
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (resistanceMap[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                        continue;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (resistanceMap[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        castLight(distance + 1, start, leftSlope, xx, xy, yx, yy);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        return calculateFOV(resistanceMap, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }
}
