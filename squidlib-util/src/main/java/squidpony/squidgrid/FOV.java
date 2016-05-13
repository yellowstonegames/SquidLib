package squidpony.squidgrid;

import squidpony.GwtCompatibility;
import squidpony.squidmath.Coord;

import java.util.*;

/**
 * This class provides methods for calculating Field of View in grids. Field of
 * View (FOV) algorithms determine how much area surrounding a point can be
 * seen. They return a two dimensional array of doubles, representing the amount
 * of view (typically sight, but perhaps sound, smell, etc.) which the origin
 * has of each cell.
 *
 * After a calculation has been run, the resulting light map is saved in the
 * class. This allows the values to be checked on individual cells without being
 * required to work with the returned light map. If no calculation has been
 * performed, all value checking methods will return their version of "unlit".
 *
 * The input resistanceMap is considered the percent of opacity. This resistance
 * is on top of the resistance applied from the light spreading out.
 *
 * The returned light map is considered the percent of light in the cells.
 *
 * Not all implementations are required to provide percentage levels of light.
 * In such cases the returned values will be 0 for no light and 1.0 for fully
 * lit. Implementations that return this way note so in their documentation.
 *
 * All solvers perform bounds checking so solid borders in the map are not
 * required.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOV {

    public static final int
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will go around corners a bit.
             */
            RIPPLE = 1,
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will spread around edges like smoke or water, but maintain a
             * tendency to curl towards the start position when going around
             * edges.
             */
            RIPPLE_LOOSE = 2,
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will only go around corners slightly.
             */
            RIPPLE_TIGHT = 3,
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will go around corners massively.
             */
            RIPPLE_VERY_LOOSE = 4,
            /**
             * Uses Shadow Casting FOV algorithm. Treats all translucent cells
             * as fully transparent. Returns a percentage from 1.0 (center of
             * FOV) to 0.0 (outside of FOV).
             */
    SHADOW = 5;
    private int type = SHADOW;
    private static final Direction[] ccw = new Direction[]
            {Direction.UP_RIGHT, Direction.UP_LEFT, Direction.DOWN_LEFT, Direction.DOWN_RIGHT, Direction.UP_RIGHT},
            ccw_full = new Direction[]{Direction.RIGHT, Direction.UP_RIGHT, Direction.UP, Direction.UP_LEFT,
            Direction.LEFT, Direction.DOWN_LEFT, Direction.DOWN, Direction.DOWN_RIGHT};

    /**
     * Creates a solver which will use the default SHADOW solver.
     */
    public FOV() {
    }

    /**
     * Creates a solver which will use the provided FOV solver type.
     *
     * @param type
     */
    public FOV(int type) {
        this.type = type;
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations. The light will be treated as having infinite possible
     * radius.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty) {
        return calculateFOV(resistanceMap, startx, starty, Integer.MAX_VALUE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidian
     * calculations.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty, double radius) {
        return calculateFOV(resistanceMap, startx, starty, radius, Radius.CIRCLE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startX, int startY, double radius, Radius radiusTechnique) {

        double rad = Math.max(1, radius);
        double decay = 1.0 / rad;

        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        double[][] lightMap = new double[width][height];
        lightMap[startX][startY] = 1;//make the starting space full power

        boolean[][] nearLight = new boolean[width][height];
        switch (type) {
            case RIPPLE:
                lightMap = doRippleFOV(lightMap, 2, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique);
                break;
            case RIPPLE_LOOSE:
                lightMap = doRippleFOV(lightMap, 3, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique);
                break;
            case RIPPLE_TIGHT:
                lightMap = doRippleFOV(lightMap, 1, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique);
                break;
            case RIPPLE_VERY_LOOSE:
                lightMap = doRippleFOV(lightMap, 6, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique);
                break;
            case SHADOW:
               	// hotfix for too large radius -> set to longest possible straight-line Manhattan distance instead
                // does not cause problems with brightness falloff because shadowcasting is on/off

                // this should be fixed now, sorta. the distance is checked in the method this calls, so it doesn't ever
                // run through more than 512 iterations of the radius-related loop (which seemed to be the only problem,
                // running through billions of iterations when Integer/MAX_VALUE is given as a radius).
                //if (rad > width + height){
                //    rad = width + height;
                //}
                for (Direction d : Direction.DIAGONALS) {
                    shadowCast(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0, rad, startX, startY, decay, lightMap, resistanceMap, radiusTechnique);
                    shadowCast(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY, rad, startX, startY, decay, lightMap, resistanceMap, radiusTechnique);
                }
                break;
        }

        return lightMap;
    }


    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy. A conical section of FOV is lit by this method if
     * span is greater than 0.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param span the angle in degrees that measures the full arc contained in the FOV cone
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startX, int startY, double radius,
                                   Radius radiusTechnique, double angle, double span) {

        double rad = Math.max(1, radius);

        double decay = 1.0 / rad;

		double angle2 = Math.toRadians((angle > 360.0 || angle < 0.0)
				? GwtCompatibility.IEEEremainder(angle + 720.0, 360.0) : angle);
        double span2 = Math.toRadians(span);
        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        double[][] lightMap = new double[width][height];
        lightMap[startX][startY] = 1;//make the starting space full power

        boolean[][] nearLight = new boolean[width][height];
        switch (type) {
            case RIPPLE:
                lightMap = doRippleFOV(lightMap, 2, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique, angle2, span2);
                break;
            case RIPPLE_LOOSE:
                lightMap = doRippleFOV(lightMap, 3, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique, angle2, span2);
                break;
            case RIPPLE_TIGHT:
                lightMap = doRippleFOV(lightMap, 1, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique, angle2, span2);
                break;
            case RIPPLE_VERY_LOOSE:
                lightMap = doRippleFOV(lightMap, 6, startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique, angle2, span2);
                break;
            case SHADOW:
                // hotfix for too large radius -> set to longest possible straight-line Manhattan distance instead
                // does not cause problems with brightness falloff because shadowcasting is on/off

                // this should be fixed now, sorta. the distance is checked in the method this calls, so it doesn't ever
                // run through more than 512 iterations of the radius-related loop (which seemed to be the only problem,
                // running through billions of iterations when Integer/MAX_VALUE is given as a radius).
                //if (rad > width + height){
                //    rad = width + height;
                //}
                int ctr = 0;
                boolean started = false;
                for (Direction d : ccw) {
                    ctr %= 4;
                    ++ctr;
                    if (angle <= Math.PI / 2.0 * ctr + span / 2.0)
                        started = true;
                    if (started) {
                        if(ctr < 4 && angle < Math.PI / 2.0 * (ctr - 1) - span / 2.0)
                            break;
                        lightMap = shadowCastLimited(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0, rad, startX, startY, decay, lightMap, resistanceMap, radiusTechnique, angle2, span2);
                        lightMap = shadowCastLimited(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY, rad, startX, startY, decay, lightMap, resistanceMap, radiusTechnique, angle2, span2);
                    }
                }
                break;
        }

        return lightMap;
    }


    private double[][] doRippleFOV(double[][] lightMap, int ripple, int x, int y, int startx, int starty, double decay, double radius, double[][] map, boolean[][] indirect, Radius radiusStrategy) {
    	/* Not using Deque's interface, it isn't GWT compatible */
        final LinkedList<Coord> dq = new LinkedList<>();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.offer(Coord.get(x, y));
        while (!dq.isEmpty()) {
            Coord p = dq.removeFirst();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (Direction dir : Direction.OUTWARDS) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(startx, starty, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }

                double surroundingLight = nearRippleLight(x2, y2, ripple, startx, starty, decay, lightMap, map, indirect, radiusStrategy);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(Coord.get(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
        return lightMap;
    }



    private double[][] doRippleFOV(double[][] lightMap, int ripple, int x, int y, int startx, int starty, double decay, double radius, double[][] map, boolean[][] indirect, Radius radiusStrategy, double angle, double span) {
    	/* Not using Deque's interface, it isn't GWT compatible */
        final LinkedList<Coord> dq = new LinkedList<>();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.offer(Coord.get(x, y));
        while (!dq.isEmpty()) {
            Coord p = dq.removeFirst();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (Direction dir : ccw_full) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(startx, starty, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }
                double newAngle = Math.atan2(y2 - starty, x2 - startx) + Math.PI * 2;
				if (Math.abs(GwtCompatibility.IEEEremainder(angle - newAngle + Math.PI * 8, Math.PI * 2)) > span / 2.0)
					continue;

                double surroundingLight = nearRippleLight(x2, y2, ripple, startx, starty, decay, lightMap, map, indirect, radiusStrategy );
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(Coord.get(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
        return lightMap;
    }

    private double nearRippleLight(int x, int y, int rippleNeighbors, int startx, int starty, double decay, double[][] lightMap, double[][] map, boolean[][] indirect, Radius radiusStrategy) {
        if (x == startx && y == starty) {
            return 1;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;
        List<Coord> neighbors = new ArrayList<>();
        double tmpDistance = 0, testDistance;
        Coord c;
        for (Direction di : Direction.OUTWARDS) {
            int x2 = x + di.deltaX;
            int y2 = y + di.deltaY;
            if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
                tmpDistance = radiusStrategy.radius(startx, starty, x2, y2);
                int idx = 0;
                for(int i = 0; i < neighbors.size() && i <= rippleNeighbors; i++)
                {
                    c = neighbors.get(i);
                    testDistance = radiusStrategy.radius(startx, starty, c.x, c.y);
                    if(tmpDistance < testDistance) {
                        break;
                    }
                    idx++;
                }
                neighbors.add(idx, Coord.get(x2, y2));
            }
        }

        if (neighbors.isEmpty()) {
            return 0;
        }
        neighbors = neighbors.subList(0, rippleNeighbors);
/*
        while (neighbors.size() > rippleNeighbors) {
            Coord p = neighbors.remove(0);
            double dist = radiusStrategy.radius(startx, starty, p.x, p.y);
            double dist2 = 0;
            for (Coord p2 : neighbors) {
                dist2 = Math.max(dist2, radiusStrategy.radius(startx, starty, p2.x, p2.y));
            }
            if (dist < dist2) {//not the largest, put it back
                neighbors.add(p);
            }
        }
*/
        double light = 0;
        int lit = 0, indirects = 0;
        for (Coord p : neighbors) {
            if (lightMap[p.x][p.y] > 0) {
                lit++;
                if (indirect[p.x][p.y]) {
                    indirects++;
                }
                double dist = radiusStrategy.radius(x, y, p.x, p.y);
                light = Math.max(light, lightMap[p.x][p.y] - dist * decay - map[p.x][p.y]);
            }
        }

        if (map[x][y] >= 1 || indirects >= lit) {
            indirect[x][y] = true;
        }
        return light;
    }

    private double[][] shadowCast(int row, double start, double end, int xx, int xy, int yx, int yy,
                                  double radius, int startx, int starty, double decay, double[][] lightMap,
                                  double[][] map, Radius radiusStrategy) {
        double newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
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
                    double bright = 1 - decay * deltaRadius;
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        lightMap = shadowCast(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, radiusStrategy);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }
    private double[][] shadowCastLimited(int row, double start, double end, int xx, int xy, int yx, int yy,
                                         double radius, int startx, int starty, double decay, double[][] lightMap,
                                         double[][] map, Radius radiusStrategy, double angle, double span) {
        double newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
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
                double newAngle = Math.atan2(currentY - starty, currentX - startx) + Math.PI * 2;
				if (Math.abs(GwtCompatibility.IEEEremainder(angle - newAngle + Math.PI * 8, Math.PI * 2)) > span / 2.0)
					continue;

                double deltaRadius = radiusStrategy.radius(deltaX, deltaY);
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius) {
                    double bright = 1 - decay * deltaRadius;
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        lightMap = shadowCastLimited(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, radiusStrategy, angle, span);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }

    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0.
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays passed, using the dimensions of the first if they don't all match
     */
    public static double[][] addFOVs(double[][]... maps)
    {
        if(maps == null || maps.length == 0)
            return new double[0][0];
        double[][] map = GwtCompatibility.copy2D(maps[0]);
        for(int i = 1; i < maps.length; i++)
        {
            for (int x = 0; x < map.length && x < maps[i].length; x++) {
                for (int y = 0; y < map[x].length && y < maps[i][x].length; y++) {
                    map[x][y] += maps[i][x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }

    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0.
     * @param maps an Iterable of 2D double arrays (most collections implement Iterable),
     *             each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays passed, using the dimensions of the first if they don't all match
     */
    public static double[][] addFOVs(Iterable<double[][]> maps)
    {
        if(maps == null)
            return new double[0][0];
        Iterator<double[][]> it = maps.iterator();
        if(!it.hasNext())
            return new double[0][0];
        double[][] map = GwtCompatibility.copy2D(it.next()), t;
        while (it.hasNext())
        {
            t = it.next();
            for (int x = 0; x < map.length && x < t.length; x++) {
                for (int y = 0; y < map[x].length && y < t[x].length; y++) {
                    map[x][y] += t[x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }

}
