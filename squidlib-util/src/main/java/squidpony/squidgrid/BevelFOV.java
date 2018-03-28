package squidpony.squidgrid;

import squidpony.ArrayTools;
import squidpony.GwtCompatibility;
import squidpony.annotation.Beta;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.NumberTools;

/**
 * Use FOV instead, probably with type SHADOW; this has the same results as shadowcasting FOV but is slower, currently.
 * This will probably be rewritten in a very different way.
 * <br>
 * A different kind of FOV implementation that pre-processes the map to better analyze the locations of corners.
 * Based loosely on Adam Milazzo's FOV, http://www.adammil.net/blog/v125_Roguelike_Vision_Algorithms.html , which in
 * turn is based on the diamond walls technique (on the same page).
 */
@Beta
public class BevelFOV extends FOV {

    protected GreasedRegion map = null;
    private double[][] resMapCache = null;

    /**
     * Creates a BevelFOV solver.
     */
    public BevelFOV() {
    }

    /**
     * Included for compatibility; ignores the parameter and calls the default parameter with no argument.
     *
     * @param type ignored
     */
    public BevelFOV(int type) {
        this();
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     * <p>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations. The light will be treated as having infinite possible
     * radius.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startx        the horizontal component of the starting location
     * @param starty        the vertical component of the starting location
     * @return the computed light grid
     */
    @Override
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty) {
        return calculateFOV(resistanceMap, startx, starty, Integer.MAX_VALUE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     * <p>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startx        the horizontal component of the starting location
     * @param starty        the vertical component of the starting location
     * @param radius        the distance the light will extend to
     * @return the computed light grid
     */
    @Override
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty, double radius) {
        return calculateFOV(resistanceMap, startx, starty, radius, Radius.CIRCLE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     * <p>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.
     *
     * @param resistanceMap   the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startX          the horizontal component of the starting location
     * @param startY          the vertical component of the starting location
     * @param radius          the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @return the computed light grid
     */
    @Override
    public double[][] calculateFOV(double[][] resistanceMap, int startX, int startY, double radius, Radius radiusTechnique) {
        double rad = Math.max(1, radius);
        double decay = 1.0 / rad;

        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        if (light == null)
            light = new double[width][height];
        else {
            if (light.length != width || light[0].length != height)
                light = new double[width][height];
            else {
                ArrayTools.fill(light, 0.0);
            }
        }
        light[startX][startY] = 1;//make the starting space full power
        maybeCache(resistanceMap);
        for (Direction d : Direction.DIAGONALS) {
            shadowCast(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0, rad, startX, startY, decay, radiusTechnique);
            shadowCast(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY, rad, startX, startY, decay, radiusTechnique);
        }
        return light;
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     * <p>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy. A conical section of FOV is lit by this method if
     * span is greater than 0.
     *
     * @param resistanceMap   the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startX          the horizontal component of the starting location
     * @param startY          the vertical component of the starting location
     * @param radius          the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @param angle           the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param span            the angle in degrees that measures the full arc contained in the FOV cone
     * @return the computed light grid
     */
    @Override
    public double[][] calculateFOV(double[][] resistanceMap, int startX, int startY, double radius, Radius radiusTechnique, double angle, double span) {
        double rad = Math.max(1, radius);

        double decay = 1.0 / rad;

        double angle2 = Math.toRadians((angle > 360.0 || angle < 0.0)
                ? GwtCompatibility.IEEEremainder(angle + 720.0, 360.0) : angle);
        double span2 = Math.toRadians(span);
        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        if (light == null)
            light = new double[width][height];
        else {
            if (light.length != width || light[0].length != height)
                light = new double[width][height];
            else {
                ArrayTools.fill(light, 0.0);
            }
        }
        light[startX][startY] = 1;//make the starting space full power
        maybeCache(resistanceMap);

        int ctr = 0;
        boolean started = false;
        for (Direction d : ccw) {
            ctr %= 4;
            ++ctr;
            if (angle <= Math.PI / 2.0 * ctr + span / 2.0)
                started = true;
            if (started) {
                if (ctr < 4 && angle < Math.PI / 2.0 * (ctr - 1) - span / 2.0)
                    break;
                light = shadowCastLimited(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0, rad, startX, startY, decay, radiusTechnique, angle2, span2);
                light = shadowCastLimited(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY, rad, startX, startY, decay, radiusTechnique, angle2, span2);
            }
        }
        return light;
    }

    /**
     * Calculates what cells are visible from (startX,startY) using the given resistanceMap; this can be given to
     * mixVisibleFOVs() to limit extra light sources to those visible from the starting point.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startX the center of the LOS map; typically the player's x-position
     * @param startY the center of the LOS map; typically the player's y-position
     * @return an LOS map with the given starting point
     */
    public double[][] calculateLOSMap(double[][] resistanceMap, int startX, int startY)
    {
        if(resistanceMap == null || resistanceMap.length == 0)
            return new double[0][0];
        return calculateFOV(resistanceMap, startX, startY, resistanceMap.length + resistanceMap[0].length, Radius.SQUARE);
    }

    private void maybeCache(final double[][] resistanceMap)
    {
        if(resMapCache != resistanceMap)
        {
            storeMap(resistanceMap);
        }
    }

    /**
     * Pre-calculates some time-intensive calculations that generally only need to be done once per map. You should pass
     * the same exact value for resistanceMap to {@link #calculateFOV(double[][], int, int)} or its overloads if you
     * want to avoid re-calculating this map analysis. If an element of resistanceMap changes, you should call this
     * method again before you call calculateFOV, though it can wait until you've made all the changes you want to
     * resistanceMap's elements. This class does not have a way to tell when the array has been modified in user code,
     * so you should take care if you change resistanceMap to call this method.
     * @param resistanceMap a 2D double array, as produced by {@link squidpony.squidgrid.mapping.DungeonUtility#generateResistances(char[][])}
     */
    public void storeMap(final double[][] resistanceMap)
    {
        if(resistanceMap == null || resistanceMap.length <= 0 || resistanceMap[0].length <= 0)
            throw new IllegalArgumentException("resistanceMap cannot be null or empty");
        if(resMapCache == null
                || resMapCache.length != resistanceMap.length
                || resMapCache[0].length != resistanceMap[0].length)
        {
            resMapCache = resistanceMap;
            if(map == null)
                map = new GreasedRegion(resistanceMap, 1.0, Double.POSITIVE_INFINITY, 3).removeCorners();
            else
                map.refill(resistanceMap, 1.0, Double.POSITIVE_INFINITY, 3).removeCorners();
        }
        else
        {
            for (int x = 0; x < resistanceMap.length; x++) {
                for (int y = 0; y < resistanceMap[x].length; y++) {
                    if(resMapCache[x][y] != resistanceMap[x][y])
                    {
                        resMapCache = resistanceMap;
                        if(map == null)
                            map = new GreasedRegion(resistanceMap, 1.0, Double.POSITIVE_INFINITY, 3).removeCorners();
                        else
                            map.refill(resistanceMap, 1.0, Double.POSITIVE_INFINITY, 3).removeCorners();
                        return;
                    }
                }
            }
        }
    }

    private double[][] shadowCast(int row, double start, double end, int xx, int xy, int yx, int yy,
                                         double radius, int startx, int starty, double decay, Radius radiusStrategy) {
        double newStart = 0;
        if (start < end) {
            return light;
        }
        int width = light.length;
        int height = light[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < (width + height) && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double deltaRadius = radiusStrategy.radius(deltaX, deltaY);
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius) {
                    light[currentX][currentY] = 1 - decay * deltaRadius;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map.contains(currentX * 3 + 1 + xx + xy, currentY * 3 + 1 + yx + yy)) { // hit a wall
                        newStart = rightSlope;
                    } else if(!map.contains(currentX * 3 + 1, currentY * 3 + 1)){
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (distance < radius && map.contains(currentX * 3 + 1, currentY * 3 + 1))
                    { // hit a wall within sight line
                        blocked = true;
                        light = shadowCast(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, radiusStrategy);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return light;
    }
    private double[][] shadowCastLimited(int row, double start, double end, int xx, int xy, int yx, int yy,
                                                double radius, int startx, int starty, double decay,
                                                Radius radiusStrategy, double angle, double span) {
        double newStart = 0;
        if (start < end) {
            return light;
        }
        int width = light.length;
        int height = light[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < (width + height) && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double newAngle = NumberTools.atan2(currentY - starty, currentX - startx) + Math.PI * 2;
                if (Math.abs(GwtCompatibility.IEEEremainder(angle - newAngle + Math.PI * 8, Math.PI * 2)) > span / 2.0)
                    continue;

                double deltaRadius = radiusStrategy.radius(deltaX, deltaY);
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius) {
                    light[currentX][currentY] = 1 - decay * deltaRadius;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map.contains(currentX * 3 + 1 + xx + xy, currentY * 3 + 1 + yx + yy)) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map.contains(currentX * 3 + 1 + xx + xy, currentY * 3 + 1 + yx + yy) && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        light = shadowCastLimited(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, radiusStrategy, angle, span);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return light;
    }

}
