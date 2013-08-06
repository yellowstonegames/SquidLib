package squidpony.squidgrid.fov;

import static squidpony.squidgrid.fov.TranslucenceWrapperFOV.RayType.*;
import squidpony.squidgrid.util.Direction;

/**
 * Acts as a wrapper which fully respects translucency and lights based on
 * another FOVSolver.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class TranslucenceWrapperFOV implements FOVSolver {

    static enum RayType {

        PRIMARY, SECONDARY, TERTIARY
    };
    private FOVSolver fov = new ShadowFOV();
    private float[][] lightMap, resistanceMap, shadowMap;
    private int width, height, startx, starty;
    private RadiusStrategy rStrat;
    private float decay;

    /**
     * Uses default ShadowFOV to create lit area mapping
     */
    public TranslucenceWrapperFOV() {
    }

    /**
     * Uses provided FOVSolver to create lit area mapping
     *
     * @param fov
     */
    public TranslucenceWrapperFOV(FOVSolver fov) {
        this.fov = fov;
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        width = resistanceMap.length;
        height = resistanceMap[0].length;
        this.startx = startx;
        this.starty = starty;
        this.decay = decay;
        this.resistanceMap = resistanceMap;
        this.rStrat = radiusStrategy;
        lightMap = new float[width][height];
        shadowMap = fov.calculateFOV(resistanceMap, startx, starty, force, decay, radiusStrategy);

        lightMap[startx][starty] = force;//start out at full force
        for (Direction dir : Direction.OUTWARDS) {
            pushLight(startx + dir.deltaX, starty + dir.deltaY, force + decay, dir, dir, PRIMARY);
        }

        return lightMap;
    }

    /**
     * Pushes light outwards based on the light coming into this cell.
     *
     * Tertiary light should only light up opaque objects, with more opaque
     * objects requiring less light to be lit up.
     *
     * @param x
     * @param y
     * @param light the amount of light coming into this tile
     * @param tertiary whether the light coming in is tertiary
     */
    private void pushLight(int x, int y, float light, Direction dir, Direction previous, RayType type) {
        if (light <= 0 || x < 0 || x >= width || y < 0 || y >= height || shadowMap[x][y] <= 0 || lightMap[x][y] >= light) {
            return;//out of light, off the edge, base fov not lit, or already well lit
        }

        lightMap[x][y] = light;//apply passed in light

        if (type == PRIMARY) {
            //push primary ray
            float radius = rStrat.radius(x, y, x + dir.deltaX, y + dir.deltaY);
            float brightness = light;
            brightness *= (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
            brightness -= radius * decay;//reduce by the amount of decay from passing through
            pushLight(x + dir.deltaX, y + dir.deltaY, brightness, dir, dir, PRIMARY);

            Direction pushing = dir.clockwise();
            radius = rStrat.radius(x, y, x + pushing.deltaX, y + pushing.deltaY);
            brightness = light * (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
            brightness -= radius * decay;//reduce by the amount of decay from passing through
            pushLight(x + pushing.deltaX, y + pushing.deltaY, brightness, dir, pushing, SECONDARY);

            pushing = dir.counterClockwise();
            radius = rStrat.radius(x, y, x + pushing.deltaX, y + pushing.deltaY);
            brightness = light * (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
            brightness -= radius * decay;//reduce by the amount of decay from passing through
            pushLight(x + pushing.deltaX, y + pushing.deltaY, brightness, dir, pushing, SECONDARY);
        } else {//type == SECONDARY at this point
            //push pass-through secondary ray
            Direction pushing = previous;//redirect to previous' previous direction
            float radius = rStrat.radius(x, y, x + pushing.deltaX, y + pushing.deltaY);
            float brightness = light * (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
            brightness -= radius * decay;//reduce by the amount of decay from passing through
            pushLight(x + pushing.deltaX, y + pushing.deltaY, brightness, dir, pushing, SECONDARY);

            //now push through tertiary rays, first just continues in direction passed in
            radius = rStrat.radius(x, y, x + dir.deltaX, y + dir.deltaY);
            brightness = light * (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
            brightness -= radius * decay;//reduce by the amount of decay from passing through
            pushLight(x + dir.deltaX, y + dir.deltaY, brightness, dir, pushing, SECONDARY);
            if (previous.clockwise().equals(dir)) {
                pushing = previous.clockwise();
            } else {
                pushing = previous.counterClockwise();
            }

            radius = rStrat.radius(x, y, x + pushing.deltaX, y + pushing.deltaY);
            brightness = light * (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
            brightness -= radius * decay;//reduce by the amount of decay from passing through
            pushLight(x + pushing.deltaX, y + pushing.deltaY, brightness, dir, pushing, SECONDARY);
        }
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        return calculateFOV(resistanceMap, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }
}
