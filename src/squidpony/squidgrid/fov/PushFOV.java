package squidpony.squidgrid.fov;

import squidpony.annotation.Beta;
import static squidpony.squidgrid.fov.PushFOV.RayType.*;
import squidpony.squidgrid.util.Direction;
import static squidpony.squidgrid.util.Direction.*;

/**
 * Uses slight permissiveness based on transparency to allows solid objects to
 * be more easily seen at slight angles.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class PushFOV implements FOVSolver {

    static enum RayType {

        PRIMARY, SECONDARY, TERTIARY
    };
    private FOVSolver fov = new ShadowFOV();
    private float[][] lightMap, resistanceMap, shadowMap;
    private int width, height, startx, starty;
    private RadiusStrategy rStrat;
    private float decay;
    private float threshhold = 0.1f;

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
        if (light <= 0 || x < 0 || x >= width || y < 0 || y >= height) {
            return;//ran off the edge or ran out of light
        }

        if (type != TERTIARY && shadowMap[x][y] <= 0) {
            return;//if shadow didn't light it, it's not lit unless it's a tertiary ray
        }

        if (type == TERTIARY && resistanceMap[x][y] < 1 - light) {//if the light is strong enough even transparent tiles will be lit
            return;//not opaque enough to be lit by tertiary light
        }

        if ((type == SECONDARY || type == TERTIARY) && lightMap[x][y] >= light) {
            return;//already well lit
        }

        lightMap[x][y] = Math.max(lightMap[x][y], light);//apply passed in light

        if (type == TERTIARY) {
            return;//tertiary light isn't passed on
        }

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
            RayType pushType;
            if (shadowMap[x][y] > threshhold) {
                pushType = SECONDARY;
            } else {
                pushType = TERTIARY;
            }
            radius = rStrat.radius(x, y, x + dir.deltaX, y + dir.deltaY);
            brightness = light * (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
            brightness -= radius * decay;//reduce by the amount of decay from passing through
            pushLight(x + dir.deltaX, y + dir.deltaY, brightness, dir, pushing, pushType);
            if (previous == UP || previous == RIGHT || previous == LEFT || previous == DOWN) {//came from an edge, push further along general flow
                if (previous.clockwise().equals(dir)) {
                    pushing = previous.clockwise();
                } else {
                    pushing = previous.counterClockwise();
                }

                radius = rStrat.radius(x, y, x + pushing.deltaX, y + pushing.deltaY);
                brightness = light * (1 - resistanceMap[x][y]);//light is reduced by the portion of the square passed through
                brightness -= radius * decay;//reduce by the amount of decay from passing through
                pushLight(x + pushing.deltaX, y + pushing.deltaY, brightness, dir, pushing, pushType);
            }
        }
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        return calculateFOV(resistanceMap, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }
}
